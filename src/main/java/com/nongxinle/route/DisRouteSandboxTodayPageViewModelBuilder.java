package com.nongxinle.route;

import com.nongxinle.dto.route.*;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 构建 GET /dispatch/sandbox/today 页面 ViewModel（完整 Hero / 指标 / 分区 / timeline）。
 * sections / mapOverview 共用 {@link VisibleDriverRouteSnapshot}。
 */
public final class DisRouteSandboxTodayPageViewModelBuilder {

    private static final String TOP_METRICS_SCOPE = "PENDING_DISPATCH_PAGE";
    private static final String CARD_TYPE_CUSTOMER = "CUSTOMER";
    /** 待分配客户：正式 pageViewModel 专用，字段比 CUSTOMER 更收缩。 */
    private static final String CARD_TYPE_UNASSIGNED_CUSTOMER = "UNASSIGNED_CUSTOMER";
    public static final String HERO_IMAGE_TRUCK = "TRUCK";

    private static final String SECTION_SUGGESTED = "SUGGESTED_DRIVER_ROUTES";
    private static final String SECTION_CONFIRMED = "CONFIRMED_DRIVER_ROUTES";
    private static final String SECTION_UNASSIGNED = "UNASSIGNED";
    private static final String SECTION_INVALID = "INVALID";

    private DisRouteSandboxTodayPageViewModelBuilder() {
    }

    public static SandboxTodayPageViewModel build(SandboxTodayPageBuildContext ctx) {
        Set<Integer> pendingRoutedDriverIds = collectPendingRoutedDriverIds(ctx);
        Set<Integer> unavailableDriverIds = collectUnavailableDriverIds(ctx);
        SandboxTodayPageViewModel viewModel = new SandboxTodayPageViewModel();
        viewModel.setTopMetricsScope(TOP_METRICS_SCOPE);
        viewModel.setPageHeader(buildPageHeader(ctx));
        viewModel.setTopMetrics(buildTopMetrics(ctx, pendingRoutedDriverIds, unavailableDriverIds));
        viewModel.setSections(buildSections(ctx));
        viewModel.setAvailableDrivers(buildIdleAvailableDrivers(ctx, unavailableDriverIds));
        SandboxTodayMapOverviewDto mapOverview = DisRouteSandboxTodayMapOverviewBuilder.build(ctx);
        viewModel.setMapOverview(mapOverview != null ? mapOverview : DisRouteSandboxTodayMapOverviewBuilder.emptyOverview());
        return viewModel;
    }

    /** 装车页 ViewModel：仅展示已进入装车流程的路线，并提供撤销回今日派单动作。 */
    public static SandboxTodayPageViewModel buildLoadingPage(SandboxTodayPageBuildContext ctx) {
        SandboxTodayPageViewModel viewModel = new SandboxTodayPageViewModel();
        viewModel.setTopMetricsScope("LOADING_PAGE");
        viewModel.setPageHeader(buildLoadingPageHeader(ctx));
        viewModel.setTopMetrics(buildLoadingTopMetrics(ctx));
        viewModel.setSections(buildLoadingSections(ctx));
        viewModel.setAvailableDrivers(Collections.emptyList());
        return viewModel;
    }

    /** 配送任务页 ViewModel：仅展示已出发 execution 路线。 */
    public static SandboxTodayPageViewModel buildDeliveryPage(SandboxTodayPageBuildContext ctx) {
        SandboxTodayPageViewModel viewModel = new SandboxTodayPageViewModel();
        viewModel.setTopMetricsScope("DELIVERY_PAGE");
        viewModel.setPageHeader(buildDeliveryPageHeader(ctx));
        viewModel.setTopMetrics(buildDeliveryTopMetrics(ctx));
        viewModel.setSections(buildDeliverySections(ctx));
        viewModel.setAvailableDrivers(Collections.emptyList());
        return viewModel;
    }

    private static SandboxTodayPageHeaderDto buildDeliveryPageHeader(SandboxTodayPageBuildContext ctx) {
        SandboxTodayPageHeaderDto header = new SandboxTodayPageHeaderDto();
        String routeDate = ctx.getRouteDateLabel() != null ? ctx.getRouteDateLabel()
                : (ctx.getRouteDate() != null ? ctx.getRouteDate() : "");
        header.setTitle("配送 · " + routeDate);
        int driverCount = countExecutionDrivers(ctx);
        int customerCount = sizeOf(ctx.getExecutionStops());
        String subtitle = driverCount > 0
                ? driverCount + " 名司机配送中 · " + customerCount + " 个客户"
                : (customerCount > 0 ? customerCount + " 个客户配送中" : "暂无配送路线");
        header.setProgressLine(subtitle);
        header.setSubtitle(subtitle);
        header.setOperationHint(driverCount > 0 ? "司机已出发，请关注配送进度" : "");
        header.setStatusLabel(driverCount > 0 ? "配送中" : "暂无配送");
        header.setStatusTone(driverCount > 0 ? "info" : "info");
        header.setScheduleBannerLine(ctx.getScheduleBannerLine());
        header.setHeroImageType(HERO_IMAGE_TRUCK);
        header.setNextActions(Collections.emptyList());
        return header;
    }

    private static int countExecutionDrivers(SandboxTodayPageBuildContext ctx) {
        NxDisRoutePlanEntity plan = ctx != null ? ctx.getMergedPlan() : null;
        if (plan != null && plan.getExecutionDriverRoutes() != null && !plan.getExecutionDriverRoutes().isEmpty()) {
            return plan.getExecutionDriverRoutes().size();
        }
        if (ctx == null || ctx.getExecutionStops() == null) {
            return 0;
        }
        Set<Integer> driverIds = new HashSet<Integer>();
        for (NxDisRouteStopEntity stop : ctx.getExecutionStops()) {
            if (stop == null) {
                continue;
            }
            Integer driverId = stop.getSuggestedDriverUserId();
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (driverId == null && task != null) {
                driverId = task.getNxDstAssignedDriverUserId();
            }
            if (driverId != null) {
                driverIds.add(driverId);
            }
        }
        return driverIds.size();
    }

    private static SandboxTodayTopMetricsDto buildDeliveryTopMetrics(SandboxTodayPageBuildContext ctx) {
        SandboxTodayTopMetricsDto metrics = new SandboxTodayTopMetricsDto();
        NxDisRoutePlanEntity plan = ctx != null ? ctx.getMergedPlan() : null;
        long[] routeTotals = new long[2];
        if (plan != null) {
            accumulateRouteTotals(plan.getExecutionDriverRoutes(), routeTotals);
        }
        if (routeTotals[0] <= 0L && routeTotals[1] <= 0L && ctx != null) {
            accumulateStopLegTotals(ctx.getExecutionStops(), routeTotals);
        }
        metrics.setDriverCount(countExecutionDrivers(ctx));
        metrics.setCustomerStopCount(sizeOf(ctx != null ? ctx.getExecutionStops() : null));
        metrics.setTotalDistanceM(routeTotals[0]);
        metrics.setTotalDurationS(routeTotals[1]);
        metrics.setTotalDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(routeTotals[0]));
        metrics.setTotalDurationText(DisRouteSandboxDisplayFormatHelper.formatDurationText(routeTotals[1]));
        return metrics;
    }

    private static List<SandboxTodaySectionDto> buildDeliverySections(SandboxTodayPageBuildContext ctx) {
        List<SandboxTodaySectionDto> sections = new ArrayList<SandboxTodaySectionDto>();
        List<SandboxTodaySectionCardDto> executionCards = new ArrayList<SandboxTodaySectionCardDto>();
        NxDisRoutePlanEntity plan = ctx != null ? ctx.getMergedPlan() : null;
        if (plan != null && plan.getExecutionDriverRoutes() != null && !plan.getExecutionDriverRoutes().isEmpty()) {
            DisRouteDispatchDriverNameHelper.enrichRoutesDriverNames(
                    plan.getExecutionDriverRoutes(), ctx.getDrivers());
            executionCards.addAll(DisRouteSandboxTodayDriverRouteCardBuilder.buildFromPlanRoutes(
                    plan.getExecutionDriverRoutes(), DisRouteSandboxTodayRouteKind.EXECUTION, ctx));
        }
        if (executionCards.isEmpty() && ctx != null && ctx.getExecutionStops() != null
                && !ctx.getExecutionStops().isEmpty()) {
            executionCards.addAll(DisRouteSandboxTodayDriverRouteCardBuilder.buildFromStopsGrouped(
                    ctx.getExecutionStops(), DisRouteSandboxTodayRouteKind.EXECUTION, ctx));
        }
        if (!executionCards.isEmpty()) {
            sections.add(section("EXECUTION_DRIVER_ROUTES", "配送中",
                    "司机已出发，配送执行中", executionCards));
        }
        return sections;
    }

    private static SandboxTodayPageHeaderDto buildLoadingPageHeader(SandboxTodayPageBuildContext ctx) {
        SandboxTodayPageHeaderDto header = new SandboxTodayPageHeaderDto();
        String batchLabel = ctx.getDispatchBatchLabel() != null ? ctx.getDispatchBatchLabel() : "今日装车";
        String routeDate = ctx.getRouteDate() != null ? ctx.getRouteDate() : "";
        header.setTitle(batchLabel + " · " + routeDate);
        int loadingStopCount = sizeOf(ctx.getLoadingStops());
        header.setProgressLine(loadingStopCount > 0
                ? "装车中 " + loadingStopCount + " 站 · 出发前可撤销回今日派单"
                : "暂无装车路线");
        header.setSubtitle(header.getProgressLine());
        header.setOperationHint(loadingStopCount > 0 ? "请完成装车后再确认出发" : "");
        header.setStatusLabel(loadingStopCount > 0 ? "装车中" : "暂无装车");
        header.setStatusTone(loadingStopCount > 0 ? "ok" : "info");
        header.setScheduleBannerLine(ctx.getScheduleBannerLine());
        header.setHeroImageType(HERO_IMAGE_TRUCK);
        header.setNextActions(Collections.emptyList());
        return header;
    }

    private static SandboxTodayTopMetricsDto buildLoadingTopMetrics(SandboxTodayPageBuildContext ctx) {
        SandboxTodayTopMetricsDto metrics = new SandboxTodayTopMetricsDto();
        NxDisRoutePlanEntity plan = ctx.getMergedPlan();
        long[] routeTotals = new long[2];
        if (plan != null) {
            accumulateRouteTotals(plan.getLoadingDriverRoutes(), routeTotals);
        }
        if (routeTotals[0] <= 0L && routeTotals[1] <= 0L) {
            accumulateStopLegTotals(ctx.getLoadingStops(), routeTotals);
        }
        int stopCount = sizeOf(ctx.getLoadingStops());
        metrics.setDriverCount(plan != null && plan.getLoadingDriverRoutes() != null
                ? plan.getLoadingDriverRoutes().size() : 0);
        metrics.setCustomerStopCount(stopCount);
        metrics.setTotalDistanceM(routeTotals[0]);
        metrics.setTotalDurationS(routeTotals[1]);
        metrics.setTotalDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(routeTotals[0]));
        metrics.setTotalDurationText(DisRouteSandboxDisplayFormatHelper.formatDurationText(routeTotals[1]));
        return metrics;
    }

    private static List<SandboxTodaySectionDto> buildLoadingSections(SandboxTodayPageBuildContext ctx) {
        List<SandboxTodaySectionDto> sections = new ArrayList<SandboxTodaySectionDto>();
        List<SandboxTodaySectionCardDto> loadingCards = new ArrayList<SandboxTodaySectionCardDto>();
        NxDisRoutePlanEntity plan = ctx.getMergedPlan();
        if (plan != null && plan.getLoadingDriverRoutes() != null && !plan.getLoadingDriverRoutes().isEmpty()) {
            loadingCards.addAll(DisRouteSandboxTodayDriverRouteCardBuilder.buildFromPlanRoutes(
                    plan.getLoadingDriverRoutes(), DisRouteSandboxTodayRouteKind.LOADING, ctx));
        } else if (ctx.getLoadingStops() != null && !ctx.getLoadingStops().isEmpty()) {
            loadingCards.addAll(DisRouteSandboxTodayDriverRouteCardBuilder.buildFromStopsGrouped(
                    ctx.getLoadingStops(), DisRouteSandboxTodayRouteKind.LOADING, ctx));
        }
        if (!loadingCards.isEmpty()) {
            sections.add(section("LOADING_DRIVER_ROUTES", "装车中路线",
                    "司机未出发前，可撤销回今日派单", loadingCards));
        }
        return sections;
    }

    private static SandboxTodayPageHeaderDto buildPageHeader(SandboxTodayPageBuildContext ctx) {
        SandboxTodayPageHeaderDto header = new SandboxTodayPageHeaderDto();
        String batchLabel = ctx.getDispatchBatchLabel() != null ? ctx.getDispatchBatchLabel() : "今日派车";
        String routeDate = ctx.getRouteDate() != null ? ctx.getRouteDate() : "";
        header.setTitle(batchLabel + " · " + routeDate);

        String progressLine = buildProgressLine(ctx);
        header.setProgressLine(progressLine);
        header.setSubtitle(progressLine);
        header.setProgress(buildProgress(ctx));

        DispatchWorkbenchDto workbench = ctx.getWorkbench();
        if (workbench != null && workbench.getOperationHint() != null && !workbench.getOperationHint().trim().isEmpty()) {
            header.setOperationHint(workbench.getOperationHint().trim());
        } else if (countPendingCustomerStops(ctx) > 0) {
            header.setOperationHint("可按沙盘建议确认装车");
        } else {
            header.setOperationHint("");
        }

        if (workbench != null && workbench.getStatusLabel() != null && !workbench.getStatusLabel().trim().isEmpty()) {
            header.setStatusLabel(workbench.getStatusLabel().trim());
        } else if (countPendingCustomerStops(ctx) > 0) {
            header.setStatusLabel("可派车");
        } else {
            header.setStatusLabel("暂无客户");
        }

        header.setStatusTone(resolveHeaderStatusTone(workbench, ctx.getFeasibilityStatus()));
        header.setScheduleBannerLine(ctx.getScheduleBannerLine());
        header.setHeroImageType(HERO_IMAGE_TRUCK);
        header.setNextActions(buildNextActions(workbench));
        return header;
    }

    private static String buildProgressLine(SandboxTodayPageBuildContext ctx) {
        int total = countPendingCustomerStops(ctx);
        if (total <= 0) {
            return "暂无待派客户";
        }
        int suggested = countSuggestedCustomerStops(ctx);
        int unassigned = Math.max(0, total - suggested);
        return "分派中 " + total + " 站 · 已建议 " + suggested + " 站 · 待分配 " + unassigned + " 站";
    }

    private static SandboxTodayPageProgressDto buildProgress(SandboxTodayPageBuildContext ctx) {
        SandboxTodayPageProgressDto progress = new SandboxTodayPageProgressDto();
        int total = countPendingCustomerStops(ctx);
        if (total <= 0) {
            progress.setMainLine("暂无待确认站点");
            return progress;
        }
        int suggested = countSuggestedCustomerStops(ctx);
        int unassigned = Math.max(0, total - suggested);
        String highlightText = String.valueOf(total);
        progress.setHighlightText(highlightText);
        progress.setMainLine("分派中 " + total + " 站 · 已建议 " + suggested
                + " 站 · 待分配 " + unassigned + " 站");
        return progress;
    }

    private static int countPendingCustomerStops(SandboxTodayPageBuildContext ctx) {
        return DisRouteSandboxUnassignedStopHelper.countUniqueCustomerStops(
                collectVisibleSuggestedStops(ctx),
                ctx.getUnassignedStops());
    }

    private static int countSuggestedCustomerStops(SandboxTodayPageBuildContext ctx) {
        return DisRouteSandboxUnassignedStopHelper.countUniqueCustomerStops(
                collectVisibleSuggestedStops(ctx));
    }

    private static List<NxDisRouteStopEntity> collectVisibleSuggestedStops(SandboxTodayPageBuildContext ctx) {
        List<NxDisRouteStopEntity> stops = new ArrayList<NxDisRouteStopEntity>();
        if (ctx == null || ctx.getVisibleDriverRoutes() == null) {
            return stops;
        }
        for (VisibleDriverRouteSnapshot route : ctx.getVisibleDriverRoutes()) {
            if (route == null
                    || !VisibleDriverRouteSnapshotBuilder.SECTION_SUGGESTED.equals(route.getSectionKey())
                    || route.getStops() == null) {
                continue;
            }
            for (VisibleDriverRouteStopSnapshot stop : route.getStops()) {
                if (stop != null && stop.getSourceStop() != null) {
                    stops.add(stop.getSourceStop());
                }
            }
        }
        return stops;
    }

    private static int sizeOf(List<?> list) {
        return list != null ? list.size() : 0;
    }

    private static String resolveHeaderStatusTone(DispatchWorkbenchDto workbench, String feasibilityStatus) {
        if (workbench != null && workbench.getSeverity() != null) {
            String severity = workbench.getSeverity().trim().toUpperCase();
            if ("ERROR".equals(severity)) {
                return "danger";
            }
            if ("WARN".equals(severity) || "WARNING".equals(severity)) {
                return "warning";
            }
        }
        if (DisRouteFeasibilityStatus.INFEASIBLE.equals(feasibilityStatus)
                || DisRouteFeasibilityStatus.NO_AVAILABLE_DRIVER.equals(feasibilityStatus)) {
            return "danger";
        }
        if (DisRouteFeasibilityStatus.HAS_LATE.equals(feasibilityStatus)) {
            return "warning";
        }
        return "ok";
    }

    private static List<SandboxTodayPageActionDto> buildNextActions(DispatchWorkbenchDto workbench) {
        if (workbench == null || workbench.getNextActions() == null || workbench.getNextActions().isEmpty()) {
            return Collections.emptyList();
        }
        List<SandboxTodayPageActionDto> actions = new ArrayList<SandboxTodayPageActionDto>();
        for (DispatchWorkbenchActionDto src : workbench.getNextActions()) {
            if (src == null) {
                continue;
            }
            SandboxTodayPageActionDto dto = new SandboxTodayPageActionDto();
            dto.setActionType(src.getAction());
            dto.setLabel(src.getLabel());
            dto.setEnabled(src.getEnabled());
            dto.setDisabledReason(Boolean.TRUE.equals(src.getEnabled()) ? "" : src.getHint());
            dto.setPayload(new LinkedHashMap<String, Object>());
            actions.add(dto);
        }
        return actions;
    }

    private static SandboxTodayTopMetricsDto buildTopMetrics(SandboxTodayPageBuildContext ctx,
                                                             Set<Integer> pendingRoutedDriverIds,
                                                             Set<Integer> unavailableDriverIds) {
        SandboxTodayTopMetricsDto metrics = new SandboxTodayTopMetricsDto();
        int customerStopCount = countPendingCustomerStops(ctx);
        long[] routeTotals = sumPendingRouteTotals(ctx);
        long totalDistanceM = routeTotals[0];
        long totalDurationS = routeTotals[1];

        int idleCount = countIdleDrivers(ctx, unavailableDriverIds);
        metrics.setDriverCount(pendingRoutedDriverIds.size() + idleCount);
        metrics.setAvailableDriverCount(idleCount);
        metrics.setCustomerStopCount(customerStopCount);
        metrics.setTotalDistanceM(totalDistanceM);
        metrics.setTotalDurationS(totalDurationS);
        metrics.setTotalDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(totalDistanceM));
        metrics.setTotalDurationText(DisRouteSandboxDisplayFormatHelper.formatDurationText(totalDurationS));
        return metrics;
    }

    private static long[] sumPendingRouteTotals(SandboxTodayPageBuildContext ctx) {
        long[] totals = new long[2];
        if (ctx != null && ctx.getVisibleDriverRoutes() != null) {
            for (VisibleDriverRouteSnapshot route : ctx.getVisibleDriverRoutes()) {
                if (route == null
                        || !VisibleDriverRouteSnapshotBuilder.SECTION_SUGGESTED.equals(route.getSectionKey())) {
                    continue;
                }
                if (route.getTotalDistanceM() != null) {
                    totals[0] += route.getTotalDistanceM();
                }
                if (route.getTotalDurationS() != null) {
                    totals[1] += route.getTotalDurationS();
                }
            }
        }
        accumulateStopLegTotals(ctx != null ? ctx.getUnassignedStops() : null, totals);
        return totals;
    }

    private static void accumulateRouteTotals(List<com.nongxinle.entity.NxDisDriverRouteEntity> routes,
                                              long[] totals) {
        if (routes == null) {
            return;
        }
        for (com.nongxinle.entity.NxDisDriverRouteEntity route : routes) {
            if (route == null) {
                continue;
            }
            if (route.getNxDdrTotalDistanceM() != null && route.getNxDdrTotalDistanceM() > 0L) {
                totals[0] += route.getNxDdrTotalDistanceM();
            }
            if (route.getNxDdrTotalDurationS() != null && route.getNxDdrTotalDurationS() > 0L) {
                totals[1] += route.getNxDdrTotalDurationS();
            }
        }
    }

    private static void accumulateStopLegTotals(List<NxDisRouteStopEntity> stops, long[] totals) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Long distanceM = DisRouteSandboxDisplayFormatHelper.resolveLegDistanceM(stop);
            Long durationS = DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop);
            if (distanceM != null) {
                totals[0] += distanceM;
            }
            if (durationS != null) {
                totals[1] += durationS;
            }
        }
    }

    private static List<SandboxTodaySectionDto> buildSections(SandboxTodayPageBuildContext ctx) {
        List<SandboxTodaySectionDto> sections = new ArrayList<SandboxTodaySectionDto>();

        NxDisRoutePlanEntity plan = ctx.getMergedPlan();

        List<SandboxTodaySectionCardDto> suggestedCards = buildSuggestedSectionCards(ctx, plan);
        if (!suggestedCards.isEmpty()) {
            sections.add(section(SECTION_SUGGESTED, "建议派车",
                    "系统已匹配司机，请编辑路线确认分派", suggestedCards));
        }

        List<SandboxTodaySectionCardDto> confirmedCards = buildConfirmedSectionCards(ctx, plan);
        if (!confirmedCards.isEmpty()) {
            sections.add(section(SECTION_CONFIRMED, "已确认待装车",
                    "全部站点已确认，可让司机进入装车", confirmedCards));
        }

        List<SandboxTodaySectionCardDto> unassignedCards = buildUnassignedCustomerCards(ctx);
        if (!unassignedCards.isEmpty()) {
            sections.add(section(SECTION_UNASSIGNED, "待分配客户",
                    "暂无建议司机，需要人工处理", unassignedCards));
        }

        List<SandboxTodaySectionCardDto> invalidCards = buildInvalidSectionCards(ctx.getInvalidStops());
        if (!invalidCards.isEmpty()) {
            sections.add(section(SECTION_INVALID, "异常客户",
                    "缺地址、缺司机或不可配送", invalidCards));
        }

        return sections;
    }

    /** 未确认站点 → 建议派车；已有确认站点的司机不再单独出卡（合并到已确认区）。 */
    private static List<SandboxTodaySectionCardDto> buildSuggestedSectionCards(
            SandboxTodayPageBuildContext ctx, NxDisRoutePlanEntity plan) {
        return buildDriverRouteCardsFromSnapshots(ctx, VisibleDriverRouteSnapshotBuilder.SECTION_SUGGESTED);
    }

    /** 已确认、未进入装车 → 已确认待装车。 */
    private static List<SandboxTodaySectionCardDto> buildConfirmedSectionCards(
            SandboxTodayPageBuildContext ctx, NxDisRoutePlanEntity plan) {
        return buildDriverRouteCardsFromSnapshots(ctx, VisibleDriverRouteSnapshotBuilder.SECTION_CONFIRMED);
    }

    private static List<SandboxTodaySectionCardDto> buildDriverRouteCardsFromSnapshots(
            SandboxTodayPageBuildContext ctx,
            String sectionKey) {
        if (ctx == null || ctx.getVisibleDriverRoutes() == null || ctx.getVisibleDriverRoutes().isEmpty()) {
            return Collections.emptyList();
        }
        List<SandboxTodaySectionCardDto> cards = new ArrayList<SandboxTodaySectionCardDto>();
        for (VisibleDriverRouteSnapshot routeSnapshot : ctx.getVisibleDriverRoutes()) {
            if (routeSnapshot == null || !sectionKey.equals(routeSnapshot.getSectionKey())) {
                continue;
            }
            SandboxTodaySectionCardDto card = DisRouteSandboxTodayDriverRouteCardBuilder
                    .buildFromVisibleRouteSnapshot(routeSnapshot, ctx);
            if (card != null) {
                cards.add(card);
            }
        }
        return cards;
    }

    private static Integer resolveDriverIdFromStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        Integer driverId = stop.getSuggestedDriverUserId();
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (driverId == null && task != null) {
            driverId = task.getNxDstAssignedDriverUserId() != null
                    ? task.getNxDstAssignedDriverUserId()
                    : (task.getNxDstSuggestedDriverUserId() != null
                    ? task.getNxDstSuggestedDriverUserId() : task.getSuggestedDriverUserId());
        }
        return driverId;
    }

    private static SandboxTodaySectionDto section(String key,
                                                  String title,
                                                  String description,
                                                  List<SandboxTodaySectionCardDto> cards) {
        SandboxTodaySectionDto section = new SandboxTodaySectionDto();
        section.setSectionKey(key);
        section.setTitle(title);
        section.setDescription(description);
        section.setCards(cards);
        return section;
    }

    private static List<SandboxTodaySectionCardDto> buildUnassignedCustomerCards(SandboxTodayPageBuildContext ctx) {
        List<NxDisRouteStopEntity> stops = ctx != null ? ctx.getUnassignedStops() : null;
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        List<NxDisRouteStopEntity> consolidated =
                DisRouteSandboxUnassignedStopHelper.consolidateByDepartment(stops);
        List<SandboxTodaySectionCardDto> cards = new ArrayList<SandboxTodaySectionCardDto>();
        Date serverNow = ctx.getServerNow();
        for (NxDisRouteStopEntity stop : consolidated) {
            if (stop == null) {
                continue;
            }
            cards.add(buildUnassignedCustomerCard(stop, ctx, serverNow));
        }
        return cards;
    }

    private static SandboxTodaySectionCardDto buildUnassignedCustomerCard(NxDisRouteStopEntity stop,
                                                                          SandboxTodayPageBuildContext ctx,
                                                                          Date serverNow) {
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        SandboxTodaySectionCardDto card = new SandboxTodaySectionCardDto();
        card.setCardType(CARD_TYPE_UNASSIGNED_CUSTOMER);
        card.setCustomerName(resolveCustomerName(stop, task));
        card.setGoodsSummary(DisRouteSandboxDisplayFormatHelper.buildGoodsSummary(task));
        card.setDriverLabel("无司机 / 未分配");
        card.setStatusLabel("无司机 / 未分配");
        card.setBadgeLabel("未分配");
        card.setPlannedArrivalLabel(
                DisRouteSandboxTodayStopScheduleHelper.resolvePlannedArrivalLabel(stop, serverNow));
        card.setPlannedDepartureLabel(
                DisRouteSandboxTodayStopScheduleHelper.resolvePlannedDepartureLabel(stop, serverNow));
        card.setCustomerWindowLabel(
                DisRouteSandboxTodayStopScheduleHelper.resolveCustomerWindowLabel(stop, serverNow));
        card.setServiceDurationLabel(
                DisRouteSandboxTodayStopScheduleHelper.resolveServiceDurationLabel(stop));
        card.setDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(
                DisRouteSandboxDisplayFormatHelper.resolveLegDistanceM(stop)));
        card.setDurationText(DisRouteSandboxDisplayFormatHelper.formatDurationText(
                DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop)));
        card.setTimeLabel(buildUnassignedTimeLabel(card));
        card.setPrimaryAction(buildUnassignedManualDispatchAction(stop, task, ctx));
        return card;
    }

    private static String buildUnassignedTimeLabel(SandboxTodaySectionCardDto card) {
        if (card.getPlannedArrivalLabel() != null && !card.getPlannedArrivalLabel().trim().isEmpty()) {
            String arrival = card.getPlannedArrivalLabel().trim();
            if (card.getDurationText() != null && !card.getDurationText().trim().isEmpty()) {
                return "约 " + card.getDurationText() + " · " + arrival;
            }
            return arrival;
        }
        if (card.getDurationText() != null && !card.getDurationText().trim().isEmpty()) {
            return "约 " + card.getDurationText();
        }
        return null;
    }

    private static Map<String, Object> buildUnassignedManualDispatchAction(NxDisRouteStopEntity stop,
                                                                           NxDisShipmentTaskEntity task,
                                                                           SandboxTodayPageBuildContext ctx) {
        if (ctx == null || !hasOnDutyDriverForManualDispatch(ctx)) {
            return ManualDispatchPrimaryActionMaps.disabledStartManualDispatch("当前没有上岗司机");
        }
        Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
        String sandboxStopKey = stop.getSandboxStopKey();
        if (sandboxStopKey == null && depId != null) {
            sandboxStopKey = DisRouteSandboxStopKeyUtils.build(depId);
        }
        return ManualDispatchPrimaryActionMaps.enabledStartManualDispatch(
                "人工调度",
                ManualDispatchPrimaryActionMaps.buildPayload(
                        ctx.getDisId(),
                        ctx.getRouteDate(),
                        ctx.getBatchCode(),
                        ctx.getOperatorUserId(),
                        depId,
                        sandboxStopKey,
                        DisRouteSandboxTodayDriverRouteCardBuilder.collectLiveOrderIds(task)));
    }

    private static boolean hasOnDutyDriverForManualDispatch(SandboxTodayPageBuildContext ctx) {
        if (ctx.getDrivers() == null || ctx.getDrivers().getDrivers() == null) {
            return false;
        }
        for (DriverDispatchCandidateDto driver : ctx.getDrivers().getDrivers()) {
            if (driver != null
                    && com.nongxinle.route.DisDriverDutyStatus.ON_DUTY.equals(driver.getDutyStatus())) {
                return true;
            }
        }
        return false;
    }

    private static List<SandboxTodaySectionCardDto> buildFlatCustomerCards(List<NxDisRouteStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        List<SandboxTodaySectionCardDto> cards = new ArrayList<SandboxTodaySectionCardDto>();
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            SandboxTodaySectionCardDto card = new SandboxTodaySectionCardDto();
            card.setCardType(CARD_TYPE_CUSTOMER);
            card.setCardKey(resolveCardKey(stop));
            card.setDepartmentId(stop.getNxDrsDepartmentId());
            card.setCustomerName(resolveCustomerName(stop, task));
            card.setGoodsSummary(DisRouteSandboxDisplayFormatHelper.buildGoodsSummary(task));
            card.setDriverLabel("无司机 / 未分配");
            card.setStatusLabel("无司机 / 未分配");
            card.setBadgeLabel("未分配");
            cards.add(card);
        }
        return cards;
    }

    private static List<SandboxTodaySectionCardDto> buildInvalidSectionCards(List<InvalidDispatchStopDto> invalidStops) {
        if (invalidStops == null || invalidStops.isEmpty()) {
            return Collections.emptyList();
        }
        List<SandboxTodaySectionCardDto> cards = new ArrayList<SandboxTodaySectionCardDto>();
        for (InvalidDispatchStopDto invalid : invalidStops) {
            if (invalid == null) {
                continue;
            }
            SandboxTodaySectionCardDto card = new SandboxTodaySectionCardDto();
            card.setCardType(CARD_TYPE_CUSTOMER);
            card.setCardKey(invalid.getDepFatherId() != null
                    ? DisRouteSandboxStopKeyUtils.build(invalid.getDepFatherId())
                    : (invalid.getTaskId() != null ? "task:" + invalid.getTaskId() : "invalid"));
            card.setDepartmentId(invalid.getDepFatherId());
            card.setCustomerName(invalid.getDepName());
            card.setDriverLabel("不可派车");
            card.setStatusLabel(invalid.getInvalidReason());
            card.setBadgeLabel("异常");
            cards.add(card);
        }
        return cards;
    }

    private static Set<Integer> collectPendingRoutedDriverIds(SandboxTodayPageBuildContext ctx) {
        Set<Integer> ids = new HashSet<Integer>();
        if (ctx == null || ctx.getVisibleDriverRoutes() == null) {
            return ids;
        }
        for (VisibleDriverRouteSnapshot route : ctx.getVisibleDriverRoutes()) {
            if (route != null
                    && route.getDriverUserId() != null
                    && VisibleDriverRouteSnapshotBuilder.SECTION_SUGGESTED.equals(route.getSectionKey())) {
                ids.add(route.getDriverUserId());
            }
        }
        return ids;
    }

    /**
     * 已有建议/确认/装车/在途配送路线的司机不可进入 availableDrivers。
     * 仅 DELIVERED 的历史 execution 路线不算占用（已完成可再派）。
     */
    private static Set<Integer> collectUnavailableDriverIds(SandboxTodayPageBuildContext ctx) {
        Set<Integer> ids = collectPendingRoutedDriverIds(ctx);
        collectDriverIdsFromStops(ctx.getConfirmedStops(), ids);
        collectDriverIdsFromStops(ctx.getLoadingStops(), ids);
        collectBlockingExecutionDriverIdsFromStops(ctx.getExecutionStops(), ids);
        NxDisRoutePlanEntity plan = ctx.getMergedPlan();
        if (plan != null) {
            collectDriverIdsFromRoutes(plan.getLoadingDriverRoutes(), ids);
            collectBlockingExecutionDriverIdsFromRoutes(plan.getExecutionDriverRoutes(), ids);
        }
        return ids;
    }

    private static void collectBlockingExecutionDriverIdsFromStops(List<NxDisRouteStopEntity> stops,
                                                                   Set<Integer> ids) {
        if (stops == null || ids == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null || !DisRouteSandboxDriverDispatchStateHelper.isBlockingExecutionTaskStop(stop)) {
                continue;
            }
            Integer driverId = resolveDriverIdFromStop(stop);
            if (driverId != null) {
                ids.add(driverId);
            }
        }
    }

    private static void collectBlockingExecutionDriverIdsFromRoutes(
            List<com.nongxinle.entity.NxDisDriverRouteEntity> routes,
            Set<Integer> ids) {
        if (routes == null || ids == null) {
            return;
        }
        for (com.nongxinle.entity.NxDisDriverRouteEntity route : routes) {
            if (route == null || route.getNxDdrDriverUserId() == null) {
                continue;
            }
            if (DisRouteSandboxDriverDispatchStateHelper.blocksAvailableIdleSlot(route)) {
                ids.add(route.getNxDdrDriverUserId());
            }
        }
    }


    private static void collectDriverIdsFromStops(List<NxDisRouteStopEntity> stops, Set<Integer> ids) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Integer driverId = stop.getSuggestedDriverUserId();
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (driverId == null && task != null) {
                driverId = task.getNxDstAssignedDriverUserId() != null
                        ? task.getNxDstAssignedDriverUserId()
                        : (task.getNxDstSuggestedDriverUserId() != null
                        ? task.getNxDstSuggestedDriverUserId() : task.getSuggestedDriverUserId());
            }
            if (driverId != null) {
                ids.add(driverId);
            }
        }
    }

    private static void collectDriverIdsFromRoutes(List<com.nongxinle.entity.NxDisDriverRouteEntity> routes,
                                                   Set<Integer> ids) {
        if (routes == null) {
            return;
        }
        for (com.nongxinle.entity.NxDisDriverRouteEntity route : routes) {
            if (route != null && route.getNxDdrDriverUserId() != null) {
                ids.add(route.getNxDdrDriverUserId());
            }
        }
    }

    private static int countIdleDrivers(SandboxTodayPageBuildContext ctx, Set<Integer> routedDriverIds) {
        int count = 0;
        if (ctx.getDrivers() == null || ctx.getDrivers().getDrivers() == null) {
            return 0;
        }
        for (DriverDispatchCandidateDto driver : ctx.getDrivers().getDrivers()) {
            if (driver == null || !Boolean.TRUE.equals(driver.getBatchEligible())) {
                continue;
            }
            if (routedDriverIds.contains(driver.getDriverUserId())) {
                continue;
            }
            count++;
        }
        return count;
    }

    private static List<SandboxTodayAvailableDriverDto> buildIdleAvailableDrivers(
            SandboxTodayPageBuildContext ctx,
            Set<Integer> routedDriverIds) {
        if (ctx.getDrivers() == null || ctx.getDrivers().getDrivers() == null) {
            return Collections.emptyList();
        }
        List<SandboxTodayAvailableDriverDto> result = new ArrayList<SandboxTodayAvailableDriverDto>();
        for (DriverDispatchCandidateDto driver : ctx.getDrivers().getDrivers()) {
            if (driver == null || !Boolean.TRUE.equals(driver.getBatchEligible())) {
                continue;
            }
            if (routedDriverIds.contains(driver.getDriverUserId())) {
                continue;
            }
            SandboxTodayAvailableDriverDto dto = new SandboxTodayAvailableDriverDto();
            dto.setDriverUserId(driver.getDriverUserId());
            dto.setDriverName(driver.getDriverName());
            if (driver.getDriverAvatarUrl() != null && !driver.getDriverAvatarUrl().trim().isEmpty()) {
                dto.setDriverAvatarUrl(driver.getDriverAvatarUrl().trim());
            }
            dto.setStatusLabel("空闲 · 当前可派");
            dto.setBadgeLabel("空闲 · 当前可派");
            dto.setRouteHint("暂无分派客户");
            dto.setOperationHint("可随时分派客户任务");
            result.add(dto);
        }
        return result;
    }

    private static String resolveCardKey(NxDisRouteStopEntity stop) {
        if (stop.getSandboxStopKey() != null && !stop.getSandboxStopKey().trim().isEmpty()) {
            return stop.getSandboxStopKey().trim();
        }
        if (stop.getNxDrsDepartmentId() != null) {
            return DisRouteSandboxStopKeyUtils.build(stop.getNxDrsDepartmentId());
        }
        return "stop";
    }

    private static String resolveCustomerName(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        if (stop.getNxDrsDepartmentName() != null && !stop.getNxDrsDepartmentName().trim().isEmpty()) {
            return stop.getNxDrsDepartmentName().trim();
        }
        if (task != null && task.getNxDstDepName() != null && !task.getNxDstDepName().trim().isEmpty()) {
            return task.getNxDstDepName().trim();
        }
        return stop.getNxDrsDepartmentId() != null ? "客户 " + stop.getNxDrsDepartmentId() : "未知客户";
    }
}
