package com.nongxinle.route;

import com.nongxinle.dto.route.*;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisShipmentTaskStatus.ASSIGNED;
import static com.nongxinle.route.DisShipmentTaskStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.READY_TO_GO;

/** DRIVER_ROUTE 卡片组装（含 stopCards / timeline / 路线摘要）。 */
public final class DisRouteSandboxTodayDriverRouteCardBuilder {

    private static final String CARD_TYPE_DRIVER_ROUTE = "DRIVER_ROUTE";

    /** 按站点状态过滤后再组装路线卡，避免同一 stop 出现在多个 section。 */
    public interface DispatchStopFilter {
        boolean accept(NxDisRouteStopEntity stop, NxDisDriverRouteEntity route);
    }

    private DisRouteSandboxTodayDriverRouteCardBuilder() {
    }

    public static boolean isSuggestedDispatchStop(NxDisRouteStopEntity stop, NxDisDriverRouteEntity route) {
        return isPendingDispatchStop(stop);
    }

    public static boolean isConfirmedDispatchStop(NxDisRouteStopEntity stop, NxDisDriverRouteEntity route) {
        if (stop == null || isPendingDispatchStop(stop)) {
            return false;
        }
        return DisRouteLoadingGateHelper.isConfirmedAssignedStopForView(stop, route);
    }

    public static List<SandboxTodaySectionCardDto> buildFromPlanRoutesFiltered(
            List<NxDisDriverRouteEntity> routes,
            DisRouteSandboxTodayRouteKind kind,
            DispatchStopFilter stopFilter,
            SandboxTodayPageBuildContext ctx) {
        if (routes == null || routes.isEmpty() || stopFilter == null) {
            return Collections.emptyList();
        }
        List<SandboxTodaySectionCardDto> cards = new ArrayList<SandboxTodaySectionCardDto>();
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null || route.getNxDdrDriverUserId() == null) {
                continue;
            }
            if (shouldSkipIneligibleDriverRoute(route.getNxDdrDriverUserId(), kind, ctx)) {
                continue;
            }
            List<NxDisRouteStopEntity> stops = route.getStops();
            if (stops == null || stops.isEmpty()) {
                continue;
            }
            List<NxDisRouteStopEntity> filtered = filterStops(stops, route, stopFilter);
            if (filtered.isEmpty()) {
                continue;
            }
            cards.add(buildDriverRouteCard(
                    route.getNxDdrDriverUserId(),
                    filtered,
                    route,
                    kind,
                    ctx));
        }
        sortDriverCards(cards);
        return cards;
    }

    private static List<NxDisRouteStopEntity> filterStops(List<NxDisRouteStopEntity> stops,
                                                          NxDisDriverRouteEntity route,
                                                          DispatchStopFilter stopFilter) {
        List<NxDisRouteStopEntity> filtered = new ArrayList<NxDisRouteStopEntity>();
        for (NxDisRouteStopEntity stop : stops) {
            if (stop != null && stopFilter.accept(stop, route)) {
                filtered.add(stop);
            }
        }
        return filtered;
    }

    public static List<SandboxTodaySectionCardDto> buildFromPlanRoutes(
            List<NxDisDriverRouteEntity> routes,
            DisRouteSandboxTodayRouteKind kind,
            SandboxTodayPageBuildContext ctx) {
        if (routes == null || routes.isEmpty()) {
            return Collections.emptyList();
        }
        List<SandboxTodaySectionCardDto> cards = new ArrayList<SandboxTodaySectionCardDto>();
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null || route.getNxDdrDriverUserId() == null) {
                continue;
            }
            List<NxDisRouteStopEntity> stops = route.getStops();
            if (stops == null || stops.isEmpty()) {
                continue;
            }
            cards.add(buildDriverRouteCard(
                    route.getNxDdrDriverUserId(),
                    stops,
                    route,
                    kind,
                    ctx));
        }
        sortDriverCards(cards);
        return cards;
    }

    public static List<SandboxTodaySectionCardDto> buildFromStopsGrouped(
            List<NxDisRouteStopEntity> stops,
            DisRouteSandboxTodayRouteKind kind,
            SandboxTodayPageBuildContext ctx) {
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, List<NxDisRouteStopEntity>> byDriver = groupStopsByDriver(stops);
        List<SandboxTodaySectionCardDto> cards = new ArrayList<SandboxTodaySectionCardDto>();
        for (Map.Entry<Integer, List<NxDisRouteStopEntity>> entry : byDriver.entrySet()) {
            if (shouldSkipIneligibleDriverRoute(entry.getKey(), kind, ctx)) {
                continue;
            }
            NxDisDriverRouteEntity route = findPlanRoute(ctx.getMergedPlan(), entry.getKey(), kind);
            cards.add(buildDriverRouteCard(entry.getKey(), entry.getValue(), route, kind, ctx));
        }
        sortDriverCards(cards);
        return cards;
    }

    private static SandboxTodaySectionCardDto buildDriverRouteCard(
            Integer driverUserId,
            List<NxDisRouteStopEntity> stops,
            NxDisDriverRouteEntity route,
            DisRouteSandboxTodayRouteKind kind,
            SandboxTodayPageBuildContext ctx) {
        Date serverNow = ctx.getServerNow();
        Map<Integer, DriverDispatchCandidateDto> driverIndex = indexDrivers(ctx.getDrivers());

        long totalDistanceM = 0L;
        long totalDurationS = 0L;
        List<SandboxTodayStopCardDto> stopCards = new ArrayList<SandboxTodayStopCardDto>();
        int seq = 0;
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            seq++;
            Long legDistanceM = DisRouteSandboxDisplayFormatHelper.resolveLegDistanceM(stop);
            Long legDurationS = DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop);
            if (legDistanceM != null) {
                totalDistanceM += legDistanceM;
            }
            if (legDurationS != null) {
                totalDurationS += legDurationS;
            }
            stopCards.add(buildStopCard(stop, driverUserId, kind, seq, serverNow, ctx));
        }

        if (route != null) {
            if (route.getNxDdrTotalDistanceM() != null && route.getNxDdrTotalDistanceM() > 0L) {
                totalDistanceM = route.getNxDdrTotalDistanceM();
            }
            if (route.getNxDdrTotalDurationS() != null && route.getNxDdrTotalDurationS() > 0L) {
                totalDurationS = route.getNxDdrTotalDurationS();
            }
        }

        String driverName = resolveDriverName(driverUserId, stops, route, driverIndex);
        DriverDispatchCandidateDto driverMeta = driverIndex.get(driverUserId);
        String totalDistanceText = DisRouteSandboxDisplayFormatHelper.formatDistanceText(totalDistanceM);
        String totalDurationText = DisRouteSandboxDisplayFormatHelper.formatDurationText(totalDurationS);

        SandboxTodaySectionCardDto card = new SandboxTodaySectionCardDto();
        card.setCardType(CARD_TYPE_DRIVER_ROUTE);
        card.setDriverUserId(driverUserId);
        card.setDriverName(driverName);
        card.setDriverAvatarUrl(resolveDriverAvatarUrl(driverMeta));
        card.setDriverStatusLabel(resolveDriverStatusLabel(kind, route, driverMeta));
        card.setDriverStatusTone(resolveDriverStatusTone(kind, route, driverMeta));
        card.setBadgeLabel(resolveRouteBadgeLabel(kind));
        card.setScheduleHeadline(resolveScheduleHeadline(route, stopCards, ctx));
        card.setCustomerStopCount(stopCards.size());
        card.setTotalDistanceM(totalDistanceM);
        card.setTotalDurationS(totalDurationS);
        card.setTotalDistanceText(totalDistanceText);
        card.setTotalDurationText(totalDurationText);
        card.setRouteSummary(buildRouteSummary(stopCards.size(), totalDistanceText, totalDurationText));
        card.setRouteStatsLine(buildRouteStatsLine(stopCards.size(), totalDistanceText, totalDurationText));
        card.setStopCards(stopCards);
        card.setTimeline(DisRouteSandboxTodayTimelineBuilder.build(route, stopCards, ctx.getDepotName()));
        Map<String, Object> routePrimaryAction = buildRoutePrimaryAction(
                route, stops, driverUserId, driverName, kind, ctx);
        if (routePrimaryAction != null) {
            card.setPrimaryAction(routePrimaryAction);
        }
        if (kind == DisRouteSandboxTodayRouteKind.EXECUTION) {
            applyExecutionDriverCardEnrichment(card, route, driverMeta);
        }
        return card;
    }

    private static void applyExecutionDriverCardEnrichment(SandboxTodaySectionCardDto card,
                                                           NxDisDriverRouteEntity route,
                                                           DriverDispatchCandidateDto driverMeta) {
        if (card == null) {
            return;
        }
        String resolvedName = DisRouteDispatchDriverNameHelper.resolveRouteDriverName(route, driverMeta);
        card.setDriverName(resolvedName);
        if (!DisRouteDispatchDriverNameHelper.isResolvedName(resolvedName)) {
            card.setDriverNameResolveStatus("MISSING");
            card.setDriverNameMissingReason(DisRouteDispatchDriverNameHelper.MISSING_REASON);
        }
        DispatchDriverCardDto driverCardDto = DisRouteDispatchCardTemplateBuilder.buildDeliveryExecutionDriverCard(
                route, driverMeta);
        card.setDriverCard(driverCardDto);
        if (driverCardDto != null) {
            card.setDispatchStage(driverCardDto.getDispatchStage());
            card.setDispatchStageLabel(driverCardDto.getDispatchStageLabel());
            card.setPlannedDepartureLabel(firstNonBlank(
                    driverCardDto.getPlannedDepartureLabel(), driverCardDto.getPlannedDepartLabel()));
            card.setEstimatedReturnLabel(firstNonBlank(
                    driverCardDto.getEstimatedReturnLabel(), driverCardDto.getPlannedReturnLabel()));
            card.setCustomerCount(driverCardDto.getCustomerCount());
            card.setCompletedStopCount(driverCardDto.getCompletedStopCount());
            card.setPendingStopCount(driverCardDto.getPendingStopCount());
            if (driverCardDto.getRouteSummary() != null && !driverCardDto.getRouteSummary().trim().isEmpty()) {
                card.setRouteSummary(driverCardDto.getRouteSummary().trim());
            }
        }
        card.setRouteStatsLine(buildExecutionRouteStatsLine(
                card.getTotalDistanceText(), card.getTotalDurationText()));
    }

    private static String buildExecutionRouteStatsLine(String distanceText, String durationText) {
        StringBuilder sb = new StringBuilder();
        if (distanceText != null && !distanceText.trim().isEmpty()) {
            sb.append("全程约 ").append(distanceText.trim());
        }
        if (durationText != null && !durationText.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append("预计约 ").append(durationText.trim());
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static Map<String, Object> buildRoutePrimaryAction(NxDisDriverRouteEntity route,
                                                               List<NxDisRouteStopEntity> stops,
                                                               Integer driverUserId,
                                                               String driverName,
                                                               DisRouteSandboxTodayRouteKind kind,
                                                               SandboxTodayPageBuildContext ctx) {
        if (kind == DisRouteSandboxTodayRouteKind.LOADING) {
            return buildLoadingRouteReturnAction(route, driverUserId, ctx);
        }
        if (kind == DisRouteSandboxTodayRouteKind.EXECUTION) {
            return null;
        }
        if (stops == null || stops.isEmpty()) {
            return null;
        }
        if (route != null && DisRouteDriverDepartPolicy.isRouteDeparted(route)) {
            return null;
        }
        if (isRouteInDeliveryOrCompleted(route)) {
            return null;
        }

        String label = resolveGoLoadingLabel(driverName);

        if (kind == DisRouteSandboxTodayRouteKind.SUGGESTED) {
            return null;
        }

        if (kind == DisRouteSandboxTodayRouteKind.CONFIRMED) {
            if (!isDriverBatchEligibleForDispatch(driverUserId, ctx)) {
                return null;
            }
            RouteDispatchOperationDecision goLoadingDecision =
                    DisRouteLoadingGateHelper.evaluateGoLoadingPrimaryAction(route, stops);
            if (!goLoadingDecision.isAllowed()) {
                return null;
            }
            Integer driverRouteId = DisRouteLoadingGateHelper.resolveDriverRouteId(route, stops);
            return SandboxTodayPrimaryActionMaps.enabledGoLoading(label,
                    SandboxTodayPrimaryActionMaps.buildGoLoadingPayload(
                            ctx != null ? ctx.getDisId() : null,
                            ctx != null ? ctx.getRouteDate() : null,
                            ctx != null ? ctx.getBatchCode() : null,
                            ctx != null ? ctx.getOperatorUserId() : null,
                            driverUserId,
                            driverRouteId,
                            resolvePlanId(route, stops, ctx)));
        }

        if (hasPendingDispatchStopsForDriver(ctx, driverUserId)) {
            return null;
        }

        if (shouldHideGoLoadingForLoadingPhase(route, stops)) {
            return null;
        }

        Boolean canLoad = route != null ? route.getCanLoad() : inferRouteCanLoad(stops);
        if (Boolean.TRUE.equals(canLoad)) {
            if (!isDriverBatchEligibleForDispatch(driverUserId, ctx)) {
                return null;
            }
            return SandboxTodayPrimaryActionMaps.enabledGoLoading(label,
                    SandboxTodayPrimaryActionMaps.buildGoLoadingPayload(
                            ctx != null ? ctx.getDisId() : null,
                            ctx != null ? ctx.getRouteDate() : null,
                            ctx != null ? ctx.getBatchCode() : null,
                            ctx != null ? ctx.getOperatorUserId() : null,
                            driverUserId,
                            route != null ? route.getNxDdrId() : null,
                            resolvePlanId(route, stops, ctx)));
        }

        String blockedReason = route != null ? route.getLoadBlockedReason() : null;
        if (shouldHideGoLoadingWhenBlocked(blockedReason)) {
            return null;
        }
        return null;
    }

    private static Map<String, Object> buildLoadingRouteReturnAction(NxDisDriverRouteEntity route,
                                                                     Integer driverUserId,
                                                                     SandboxTodayPageBuildContext ctx) {
        RouteDispatchOperationDecision decision = DisRouteLoadingGateHelper.evaluateReturnToDispatch(route);
        String label = DisRouteLoadingGateHelper.RETURN_TO_DISPATCH_LABEL;
        Map<String, Object> payload = SandboxTodayPrimaryActionMaps.buildReturnToDispatchPayload(
                ctx != null ? ctx.getDisId() : null,
                ctx != null ? ctx.getRouteDate() : null,
                ctx != null ? ctx.getBatchCode() : null,
                ctx != null ? ctx.getOperatorUserId() : null,
                driverUserId,
                DisRouteLoadingGateHelper.resolveDriverRouteId(route, route != null ? route.getStops() : null),
                resolvePlanId(route, route != null ? route.getStops() : null, ctx));
        if (decision.isAllowed()) {
            return SandboxTodayPrimaryActionMaps.enabledReturnToDispatch(label, payload);
        }
        return null;
    }

    private static boolean shouldSkipIneligibleDriverRoute(Integer driverUserId,
                                                           DisRouteSandboxTodayRouteKind kind,
                                                           SandboxTodayPageBuildContext ctx) {
        if (driverUserId == null || kind == null) {
            return false;
        }
        if (kind == DisRouteSandboxTodayRouteKind.LOADING
                || kind == DisRouteSandboxTodayRouteKind.EXECUTION) {
            return false;
        }
        return !isDriverBatchEligibleForDispatch(driverUserId, ctx);
    }

    private static boolean isDriverBatchEligibleForDispatch(Integer driverUserId,
                                                          SandboxTodayPageBuildContext ctx) {
        if (driverUserId == null || ctx == null || ctx.getDrivers() == null
                || ctx.getDrivers().getDrivers() == null) {
            return false;
        }
        DriverDispatchCandidateDto meta = indexDrivers(ctx.getDrivers()).get(driverUserId);
        return meta != null && Boolean.TRUE.equals(meta.getBatchEligible());
    }

    private static String resolveGoLoadingLabel(String driverName) {
        if (driverName != null && !driverName.trim().isEmpty() && !driverName.startsWith("司机 ")) {
            return "通知" + driverName.trim() + "装货";
        }
        return "进入装车";
    }

    private static Integer resolvePlanId(NxDisDriverRouteEntity route,
                                         List<NxDisRouteStopEntity> stops,
                                         SandboxTodayPageBuildContext ctx) {
        if (route != null && route.getNxDdrPlanId() != null) {
            return route.getNxDdrPlanId();
        }
        if (stops != null) {
            for (NxDisRouteStopEntity stop : stops) {
                if (stop == null || stop.getShipmentTask() == null) {
                    continue;
                }
                if (stop.getShipmentTask().getNxDstPlanId() != null) {
                    return stop.getShipmentTask().getNxDstPlanId();
                }
            }
        }
        if (ctx != null && ctx.getMergedPlan() != null) {
            return ctx.getMergedPlan().getNxDrpId();
        }
        return null;
    }

    private static Integer resolvePlanId(NxDisDriverRouteEntity route, SandboxTodayPageBuildContext ctx) {
        return resolvePlanId(route, null, ctx);
    }

    private static boolean hasPendingDispatchStopsForDriver(SandboxTodayPageBuildContext ctx,
                                                            Integer driverUserId) {
        if (ctx == null || driverUserId == null) {
            return false;
        }
        if (hasPendingDispatchStopInList(ctx.getSuggestedStops(), driverUserId)) {
            return true;
        }
        NxDisRoutePlanEntity plan = ctx.getMergedPlan();
        if (plan == null || plan.getDriverRoutes() == null) {
            return false;
        }
        for (NxDisDriverRouteEntity driverRoute : plan.getDriverRoutes()) {
            if (driverRoute == null || !driverUserId.equals(driverRoute.getNxDdrDriverUserId())
                    || driverRoute.getStops() == null) {
                continue;
            }
            for (NxDisRouteStopEntity stop : driverRoute.getStops()) {
                if (isPendingDispatchStop(stop)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasPendingDispatchStopInList(List<NxDisRouteStopEntity> stops,
                                                          Integer driverUserId) {
        if (stops == null || driverUserId == null) {
            return false;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null || !driverUserId.equals(resolveSuggestedDriverUserId(stop))) {
                continue;
            }
            if (isPendingDispatchStop(stop)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPendingDispatchStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        if (Boolean.TRUE.equals(stop.getCanConfirmCustomer())) {
            return true;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && Boolean.TRUE.equals(task.getCanConfirmCustomer())) {
            return true;
        }
        return task != null && Boolean.TRUE.equals(task.getConfirmViaSandbox());
    }

    private static boolean isRouteInDeliveryOrCompleted(NxDisDriverRouteEntity route) {
        if (route == null) {
            return false;
        }
        String routeStatus = route.getRouteStatus();
        if (routeStatus == null) {
            routeStatus = route.getNxDdrRouteStatus();
        }
        if (routeStatus == null) {
            return false;
        }
        String normalized = routeStatus.trim().toUpperCase();
        return "IN_DELIVERY".equals(normalized)
                || "DELIVERED".equals(normalized)
                || "COMPLETED".equals(normalized)
                || "EXECUTION".equals(normalized);
    }

    private static boolean shouldHideGoLoadingForLoadingPhase(NxDisDriverRouteEntity route,
                                                              List<NxDisRouteStopEntity> stops) {
        if (route != null && DisRouteLoadingGateHelper.isRouteEnteredLoading(route)) {
            return true;
        }
        if (route != null && Boolean.TRUE.equals(route.getCanDepart())) {
            return true;
        }
        String blockedReason = route != null ? route.getLoadBlockedReason() : null;
        if (shouldHideGoLoadingWhenBlocked(blockedReason)) {
            return true;
        }
        return !hasAssignedStopNeedingLoad(stops);
    }

    private static boolean hasAssignedStopNeedingLoad(List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return false;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null || stop.getShipmentTask() == null) {
                continue;
            }
            if (DisShipmentTaskStatus.ASSIGNED.equals(stop.getShipmentTask().getNxDstStatus())) {
                return true;
            }
        }
        return false;
    }

    private static Boolean inferRouteCanLoad(List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return Boolean.FALSE;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null || stop.getShipmentTask() == null) {
                continue;
            }
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (DisShipmentTaskStatus.ASSIGNED.equals(task.getNxDstStatus())
                    && task.getNxDstAssignedDriverUserId() != null) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    private static boolean shouldHideGoLoadingWhenBlocked(String blockedReason) {
        if (blockedReason == null || blockedReason.trim().isEmpty()) {
            return false;
        }
        String reason = blockedReason.trim();
        return reason.contains("没有可装车")
                || reason.contains("已装车")
                || reason.contains("已出发")
                || reason.contains("配送中")
                || reason.contains("已完成")
                || reason.contains("装车页");
    }

    private static SandboxTodayStopCardDto buildStopCard(NxDisRouteStopEntity stop,
                                                         Integer driverUserId,
                                                         DisRouteSandboxTodayRouteKind kind,
                                                         int stopSeq,
                                                         Date serverNow,
                                                         SandboxTodayPageBuildContext ctx) {
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        SandboxTodayStopCardDto card = new SandboxTodayStopCardDto();
        card.setCardKey(resolveCardKey(stop));
        card.setStopSeq(stop.getNxDrsStopSeq() != null && stop.getNxDrsStopSeq() > 0
                ? stop.getNxDrsStopSeq() : stopSeq);
        card.setCustomerName(resolveCustomerName(stop, task));
        card.setDepartmentId(stop.getNxDrsDepartmentId());
        card.setGoodsSummary(DisRouteSandboxDisplayFormatHelper.buildGoodsSummary(task));
        card.setItems(buildCardItems(task));
        card.setPlannedArrivalLabel(DisRouteSandboxTodayStopScheduleHelper.resolvePlannedArrivalLabel(stop, serverNow));
        card.setPlannedDepartureLabel(DisRouteSandboxTodayStopScheduleHelper.resolvePlannedDepartureLabel(stop, serverNow));
        card.setCustomerWindowLabel(DisRouteSandboxTodayStopScheduleHelper.resolveCustomerWindowLabel(stop, serverNow));
        card.setServiceDurationLabel(DisRouteSandboxTodayStopScheduleHelper.resolveServiceDurationLabel(stop));
        card.setDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(
                DisRouteSandboxDisplayFormatHelper.resolveLegDistanceM(stop)));
        card.setDurationText(DisRouteSandboxDisplayFormatHelper.formatDurationText(
                DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop)));
        card.setTimeLabel(resolveTimeLabel(stop, serverNow));
        card.setStatusLabel(resolveStopStatusLabel(stop, task, kind, driverUserId, ctx));
        Map<String, Object> primaryAction = buildStopPrimaryAction(stop, task, driverUserId, kind, ctx);
        if (primaryAction != null) {
            card.setPrimaryAction(primaryAction);
        }
        return card;
    }

    private static Map<String, Object> buildStopPrimaryAction(NxDisRouteStopEntity stop,
                                                              NxDisShipmentTaskEntity task,
                                                              Integer driverUserId,
                                                              DisRouteSandboxTodayRouteKind kind,
                                                              SandboxTodayPageBuildContext ctx) {
        if (kind == DisRouteSandboxTodayRouteKind.EXECUTION) {
            return SandboxTodayPrimaryActionMaps.statusOnly("配送中", "路线已出发");
        }
        if (kind == DisRouteSandboxTodayRouteKind.LOADING
                || kind == DisRouteSandboxTodayRouteKind.CONFIRMED) {
            return buildConfirmedStopPrimaryAction(stop, task, driverUserId, ctx);
        }
        return buildSuggestedStopPrimaryAction(stop, task, driverUserId, ctx);
    }

    private static Map<String, Object> buildSuggestedStopPrimaryAction(NxDisRouteStopEntity stop,
                                                                       NxDisShipmentTaskEntity task,
                                                                       Integer driverUserId,
                                                                       SandboxTodayPageBuildContext ctx) {
        Boolean enabled = stop != null ? stop.getCanConfirmCustomer() : null;
        if (enabled == null && task != null) {
            enabled = task.getCanConfirmCustomer();
        }
        String label = buildConfirmAssignLabel(driverUserId, stop, task, ctx);
        String blockedReason = stop != null ? stop.getConfirmCustomerBlockedReason() : null;
        if (blockedReason == null && task != null) {
            blockedReason = task.getConfirmCustomerBlockedReason();
        }
        Map<String, Object> payload = SandboxTodayPrimaryActionMaps.buildConfirmPayload(
                ctx != null ? ctx.getDisId() : null,
                ctx != null ? ctx.getRouteDate() : null,
                ctx != null ? ctx.getBatchCode() : null,
                ctx != null ? ctx.getOperatorUserId() : null,
                resolveCardKey(stop),
                stop.getNxDrsDepartmentId(),
                collectLiveOrderIds(task),
                driverUserId);
        if (Boolean.TRUE.equals(enabled)) {
            return SandboxTodayPrimaryActionMaps.enabledConfirmSandboxStop(label, payload);
        }
        return null;
    }

    private static Map<String, Object> buildConfirmedStopPrimaryAction(NxDisRouteStopEntity stop,
                                                                       NxDisShipmentTaskEntity task,
                                                                       Integer driverUserId,
                                                                       SandboxTodayPageBuildContext ctx) {
        Boolean canReturn = stop != null ? stop.getCanReturnToSandbox() : null;
        if (canReturn == null && task != null) {
            canReturn = task.getCanReturnToSandbox();
        }
        if (Boolean.TRUE.equals(canReturn)) {
            Integer deliveryStopId = resolveDeliveryStopId(stop, task);
            if (deliveryStopId != null) {
                String cancelLabel = resolveReturnActionLabel(stop, task);
                Map<String, Object> payload = SandboxTodayPrimaryActionMaps.buildReturnPayload(
                        ctx != null ? ctx.getDisId() : null,
                        ctx != null ? ctx.getRouteDate() : null,
                        ctx != null ? ctx.getBatchCode() : null,
                        ctx != null ? ctx.getOperatorUserId() : null,
                        deliveryStopId,
                        "取消确认",
                        resolveReturnConfirmMessage(stop, task));
                return SandboxTodayPrimaryActionMaps.enabledReturnToSandbox(cancelLabel, payload);
            }
            canReturn = Boolean.FALSE;
        }

        return null;
    }

    private static String buildConfirmAssignLabel(Integer driverUserId,
                                                  NxDisRouteStopEntity stop,
                                                  NxDisShipmentTaskEntity task,
                                                  SandboxTodayPageBuildContext ctx) {
        String driverName = resolveDriverNameForStop(driverUserId, stop, task, ctx);
        if (driverName != null && !driverName.trim().isEmpty()) {
            return "确认分派给" + driverName.trim();
        }
        return "确认分派给司机";
    }

    private static String resolveReturnActionLabel(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        if (stop != null && stop.getReturnToSandboxActionLabel() != null
                && !stop.getReturnToSandboxActionLabel().trim().isEmpty()) {
            String label = stop.getReturnToSandboxActionLabel().trim();
            if ("返回沙盘".equals(label)) {
                return "取消确认";
            }
            return label;
        }
        if (task != null && task.getReturnToSandboxActionLabel() != null
                && !task.getReturnToSandboxActionLabel().trim().isEmpty()) {
            String label = task.getReturnToSandboxActionLabel().trim();
            if ("返回沙盘".equals(label)) {
                return "取消确认";
            }
            return label;
        }
        return "取消确认";
    }

    private static String resolveReturnConfirmMessage(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        if (stop != null && stop.getReturnToSandboxConfirmMessage() != null
                && !stop.getReturnToSandboxConfirmMessage().trim().isEmpty()) {
            return appendReturnWarning(stop.getReturnToSandboxConfirmMessage().trim(), stop, task);
        }
        if (task != null && task.getReturnToSandboxConfirmMessage() != null
                && !task.getReturnToSandboxConfirmMessage().trim().isEmpty()) {
            return appendReturnWarning(task.getReturnToSandboxConfirmMessage().trim(), stop, task);
        }
        if (task != null) {
            return appendReturnWarning(DisRouteReturnToSandboxPolicy.buildConfirmDialogMessage(task), stop, task);
        }
        return "取消后，该店会回到动态沙盘，系统会重新计算司机和路线。订单和配送单不会删除。";
    }

    private static String appendReturnWarning(String base,
                                              NxDisRouteStopEntity stop,
                                              NxDisShipmentTaskEntity task) {
        String warning = stop != null ? stop.getReturnToSandboxWarning() : null;
        if ((warning == null || warning.trim().isEmpty()) && task != null) {
            warning = task.getReturnToSandboxWarning();
        }
        if (warning == null || warning.trim().isEmpty()) {
            return base;
        }
        return base + warning.trim();
    }

    private static Integer resolveDeliveryStopId(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        if (task != null && task.getNxDstId() != null) {
            return task.getNxDstId();
        }
        if (stop != null && stop.getNxDrsShipmentTaskId() != null) {
            return stop.getNxDrsShipmentTaskId();
        }
        return null;
    }

    private static String resolveDriverNameForStop(Integer driverUserId,
                                                     NxDisRouteStopEntity stop,
                                                     NxDisShipmentTaskEntity task,
                                                     SandboxTodayPageBuildContext ctx) {
        if (stop != null && stop.getSuggestedDriverName() != null
                && !stop.getSuggestedDriverName().trim().isEmpty()) {
            return stop.getSuggestedDriverName().trim();
        }
        if (task != null && task.getSuggestedDriverName() != null
                && !task.getSuggestedDriverName().trim().isEmpty()) {
            return task.getSuggestedDriverName().trim();
        }
        if (ctx != null && driverUserId != null) {
            DriverDispatchCandidateDto meta = indexDrivers(ctx.getDrivers()).get(driverUserId);
            if (meta != null && meta.getDriverName() != null && !meta.getDriverName().trim().isEmpty()) {
                return meta.getDriverName().trim();
            }
        }
        if (driverUserId != null) {
            return "司机 " + driverUserId;
        }
        return "司机";
    }

    private static String resolveScheduleHeadline(NxDisDriverRouteEntity route,
                                                  List<SandboxTodayStopCardDto> stopCards,
                                                  SandboxTodayPageBuildContext ctx) {
        if (route != null) {
            if (route.getRouteScheduleSummaryLabel() != null
                    && !route.getRouteScheduleSummaryLabel().trim().isEmpty()) {
                return route.getRouteScheduleSummaryLabel().trim();
            }
            String depart = route.getPlannedDepartLabel();
            String finish = route.getPlannedReturnLabel() != null ? route.getPlannedReturnLabel()
                    : route.getPlannedFinishLabel();
            if (depart != null && finish != null) {
                return depart.trim() + " · " + finish.trim();
            }
            if ("现在可送".equals(depart) && stopCards != null && !stopCards.isEmpty()) {
                String arrivalPart = resolveHeadlineArrivalPart(stopCards.get(0));
                if (arrivalPart != null) {
                    return "现在可送 · " + arrivalPart;
                }
            }
            if (depart != null) {
                return depart.trim();
            }
            if (finish != null) {
                return finish.trim();
            }
        }
        if (ctx != null && ctx.getScheduleBannerLine() != null
                && !ctx.getScheduleBannerLine().trim().isEmpty()) {
            return ctx.getScheduleBannerLine().trim();
        }
        if (stopCards != null && !stopCards.isEmpty()) {
            String arrivalPart = resolveHeadlineArrivalPart(stopCards.get(0));
            if (arrivalPart != null) {
                return "现在可送 · " + arrivalPart;
            }
        }
        return null;
    }

    private static String resolveHeadlineArrivalPart(SandboxTodayStopCardDto stopCard) {
        if (stopCard == null) {
            return null;
        }
        if (stopCard.getPlannedArrivalLabel() != null && !stopCard.getPlannedArrivalLabel().trim().isEmpty()) {
            String label = stopCard.getPlannedArrivalLabel().trim();
            if (label.startsWith("约 ") && label.endsWith("到")) {
                return label.substring(0, label.length() - 1);
            }
            return label;
        }
        if (stopCard.getTimeLabel() != null && !stopCard.getTimeLabel().trim().isEmpty()) {
            String label = stopCard.getTimeLabel().trim();
            if (label.startsWith("预计最快 ")) {
                label = label.substring("预计最快 ".length());
            }
            if (label.endsWith("到")) {
                label = label.substring(0, label.length() - 1);
            }
            return label;
        }
        return null;
    }

    private static String resolveDriverStatusLabel(DisRouteSandboxTodayRouteKind kind,
                                                   NxDisDriverRouteEntity route,
                                                   DriverDispatchCandidateDto driverMeta) {
        if (kind == DisRouteSandboxTodayRouteKind.EXECUTION) {
            if (route != null && route.getRouteStatusLabel() != null && !route.getRouteStatusLabel().trim().isEmpty()) {
                return route.getRouteStatusLabel().trim();
            }
            return "配送中";
        }
        if (kind == DisRouteSandboxTodayRouteKind.LOADING) {
            return "装车中";
        }
        if (driverMeta != null && Boolean.FALSE.equals(driverMeta.getBatchEligible())
                && driverMeta.getIneligibleReasonLabel() != null) {
            return driverMeta.getIneligibleReasonLabel().trim();
        }
        return "当前可派";
    }

    private static String resolveDriverStatusTone(DisRouteSandboxTodayRouteKind kind,
                                                  NxDisDriverRouteEntity route,
                                                  DriverDispatchCandidateDto driverMeta) {
        if (kind == DisRouteSandboxTodayRouteKind.EXECUTION) {
            return "info";
        }
        if (kind == DisRouteSandboxTodayRouteKind.LOADING) {
            return "ok";
        }
        if (driverMeta != null && Boolean.FALSE.equals(driverMeta.getBatchEligible())) {
            return "warn";
        }
        return "ok";
    }

    private static String resolveRouteBadgeLabel(DisRouteSandboxTodayRouteKind kind) {
        if (kind == DisRouteSandboxTodayRouteKind.EXECUTION) {
            return "配送中";
        }
        if (kind == DisRouteSandboxTodayRouteKind.LOADING) {
            return "装车中";
        }
        if (kind == DisRouteSandboxTodayRouteKind.CONFIRMED) {
            return "待装车";
        }
        return "建议路线";
    }

    private static String buildRouteStatsLine(int stopCount, String distanceText, String durationText) {
        StringBuilder sb = new StringBuilder();
        sb.append("共 ").append(stopCount).append(" 站");
        if (distanceText != null && !distanceText.isEmpty()) {
            sb.append(" · 路线里程 ").append(distanceText);
        }
        if (durationText != null && !durationText.isEmpty()) {
            sb.append(" · 预计 ").append(durationText);
        }
        return sb.toString();
    }

    private static String buildRouteSummary(int stopCount, String distanceText, String durationText) {
        StringBuilder sb = new StringBuilder();
        sb.append(stopCount).append(" 个客户");
        if (distanceText != null && !distanceText.isEmpty()) {
            sb.append(" · ").append(distanceText);
        }
        if (durationText != null && !durationText.isEmpty()) {
            sb.append(" · ").append(durationText);
        }
        return sb.toString();
    }

    private static NxDisDriverRouteEntity findPlanRoute(NxDisRoutePlanEntity plan,
                                                        Integer driverUserId,
                                                        DisRouteSandboxTodayRouteKind kind) {
        if (plan == null || driverUserId == null) {
            return null;
        }
        if (kind == DisRouteSandboxTodayRouteKind.EXECUTION) {
            return findRouteByDriver(plan.getExecutionDriverRoutes(), driverUserId);
        }
        if (kind == DisRouteSandboxTodayRouteKind.LOADING) {
            return findRouteByDriver(plan.getLoadingDriverRoutes(), driverUserId);
        }
        if (kind == DisRouteSandboxTodayRouteKind.CONFIRMED) {
            return findRouteByDriver(plan.getDriverRoutes(), driverUserId);
        }
        return findRouteByDriver(plan.getDriverRoutes(), driverUserId);
    }

    private static NxDisDriverRouteEntity findRouteByDriver(List<NxDisDriverRouteEntity> routes,
                                                            Integer driverUserId) {
        if (routes == null) {
            return null;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route != null && driverUserId.equals(route.getNxDdrDriverUserId())) {
                return route;
            }
        }
        return null;
    }

    private static Map<Integer, List<NxDisRouteStopEntity>> groupStopsByDriver(List<NxDisRouteStopEntity> stops) {
        Map<Integer, List<NxDisRouteStopEntity>> byDriver = new LinkedHashMap<Integer, List<NxDisRouteStopEntity>>();
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Integer driverId = resolveSuggestedDriverUserId(stop);
            if (driverId == null) {
                continue;
            }
            List<NxDisRouteStopEntity> bucket = byDriver.get(driverId);
            if (bucket == null) {
                bucket = new ArrayList<NxDisRouteStopEntity>();
                byDriver.put(driverId, bucket);
            }
            bucket.add(stop);
        }
        return byDriver;
    }

    private static void sortDriverCards(List<SandboxTodaySectionCardDto> cards) {
        Collections.sort(cards, new Comparator<SandboxTodaySectionCardDto>() {
            @Override
            public int compare(SandboxTodaySectionCardDto a, SandboxTodaySectionCardDto b) {
                String nameA = a.getDriverName() != null ? a.getDriverName() : "";
                String nameB = b.getDriverName() != null ? b.getDriverName() : "";
                return nameA.compareTo(nameB);
            }
        });
    }

    private static Map<Integer, DriverDispatchCandidateDto> indexDrivers(DriverDispatchListResponse drivers) {
        Map<Integer, DriverDispatchCandidateDto> index = new LinkedHashMap<Integer, DriverDispatchCandidateDto>();
        if (drivers == null || drivers.getDrivers() == null) {
            return index;
        }
        for (DriverDispatchCandidateDto driver : drivers.getDrivers()) {
            if (driver != null && driver.getDriverUserId() != null) {
                index.put(driver.getDriverUserId(), driver);
            }
        }
        return index;
    }

    private static String resolveDriverName(Integer driverUserId,
                                            List<NxDisRouteStopEntity> stops,
                                            NxDisDriverRouteEntity route,
                                            Map<Integer, DriverDispatchCandidateDto> driverIndex) {
        if (route != null && route.getDriverName() != null && !route.getDriverName().trim().isEmpty()) {
            return route.getDriverName().trim();
        }
        DriverDispatchCandidateDto meta = driverIndex.get(driverUserId);
        if (meta != null && meta.getDriverName() != null && !meta.getDriverName().trim().isEmpty()) {
            return meta.getDriverName().trim();
        }
        if (stops != null) {
            for (NxDisRouteStopEntity stop : stops) {
                if (stop != null && stop.getSuggestedDriverName() != null
                        && !stop.getSuggestedDriverName().trim().isEmpty()) {
                    return stop.getSuggestedDriverName().trim();
                }
            }
        }
        return "司机 " + driverUserId;
    }

    private static String resolveDriverAvatarUrl(DriverDispatchCandidateDto driverMeta) {
        if (driverMeta == null || driverMeta.getDriverAvatarUrl() == null) {
            return null;
        }
        String url = driverMeta.getDriverAvatarUrl().trim();
        return url.isEmpty() ? null : url;
    }

    private static Integer resolveSuggestedDriverUserId(NxDisRouteStopEntity stop) {
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

    private static String resolveCardKey(NxDisRouteStopEntity stop) {
        if (stop.getSandboxStopKey() != null && !stop.getSandboxStopKey().trim().isEmpty()) {
            return stop.getSandboxStopKey().trim();
        }
        if (stop.getNxDrsDepartmentId() != null) {
            return DisRouteSandboxStopKeyUtils.build(stop.getNxDrsDepartmentId());
        }
        if (stop.getNxDrsShipmentTaskId() != null) {
            return "task:" + stop.getNxDrsShipmentTaskId();
        }
        return "stop";
    }

    private static String resolveCustomerName(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        if (stop.getNxDrsDepartmentName() != null && !stop.getNxDrsDepartmentName().trim().isEmpty()) {
            return stop.getNxDrsDepartmentName().trim();
        }
        if (stop.getLiveDepartmentName() != null && !stop.getLiveDepartmentName().trim().isEmpty()) {
            return stop.getLiveDepartmentName().trim();
        }
        if (task != null && task.getNxDstDepName() != null && !task.getNxDstDepName().trim().isEmpty()) {
            return task.getNxDstDepName().trim();
        }
        if (stop.getNxDrsDepartmentId() != null) {
            return "客户 " + stop.getNxDrsDepartmentId();
        }
        return "未知客户";
    }

    private static String resolveTimeLabel(NxDisRouteStopEntity stop, Date serverNow) {
        if (stop.getFastestArrivalLabel() != null && !stop.getFastestArrivalLabel().trim().isEmpty()) {
            return stop.getFastestArrivalLabel().trim();
        }
        if (stop.getNxDrsPlannedArrivalAt() != null && serverNow != null) {
            String label = DisRouteSandboxScheduleLabelHelper.formatFastestArrivalLabel(
                    stop.getNxDrsPlannedArrivalAt(), serverNow, stop.getScheduleMode());
            if (label != null) {
                return label;
            }
        }
        Long legDurationS = DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop);
        if (legDurationS != null && legDurationS > 0L) {
            long minutes = Math.max(1L, Math.round(legDurationS / 60.0));
            return "预计最快 约 " + minutes + " 分钟后到";
        }
        return null;
    }

    private static String resolveStopStatusLabel(NxDisRouteStopEntity stop,
                                                 NxDisShipmentTaskEntity task,
                                                 DisRouteSandboxTodayRouteKind kind,
                                                 Integer driverUserId,
                                                 SandboxTodayPageBuildContext ctx) {
        if (kind == DisRouteSandboxTodayRouteKind.CONFIRMED) {
            return "已分派 / 可装车";
        }
        if (stop.getOperationStatusLabel() != null && !stop.getOperationStatusLabel().trim().isEmpty()) {
            return stop.getOperationStatusLabel().trim();
        }
        if (kind == DisRouteSandboxTodayRouteKind.EXECUTION) {
            if (task != null && task.getNxDstStatusLabel() != null) {
                return task.getNxDstStatusLabel().trim();
            }
            return "配送中";
        }
        return "待确认分派";
    }

    public static List<SandboxTodayCardItemDto> buildCardItemsForView(NxDisShipmentTaskEntity task) {
        return buildCardItems(task);
    }

    public static List<Integer> collectLiveOrderIds(NxDisShipmentTaskEntity task) {
        return collectLiveOrderIdsInternal(task);
    }

    private static List<SandboxTodayCardItemDto> buildCardItems(NxDisShipmentTaskEntity task) {
        if (task == null || task.getItems() == null || task.getItems().isEmpty()) {
            return Collections.emptyList();
        }
        List<SandboxTodayCardItemDto> items = new ArrayList<SandboxTodayCardItemDto>();
        for (NxDisShipmentTaskItemEntity item : task.getItems()) {
            if (item == null) {
                continue;
            }
            SandboxTodayCardItemDto dto = new SandboxTodayCardItemDto();
            dto.setLiveOrderId(item.getNxDstiLiveOrderId());
            dto.setGoodsName(item.getNxDstiGoodsName());
            dto.setStandard(item.getNxDstiStandard());
            dto.setQuantity(item.getNxDstiQuantity());
            dto.setStatus(item.getNxDstiItemStatus());
            items.add(dto);
        }
        return items;
    }

    private static List<Integer> collectLiveOrderIdsInternal(NxDisShipmentTaskEntity task) {
        if (task == null || task.getItems() == null || task.getItems().isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> liveOrderIds = new ArrayList<Integer>();
        for (NxDisShipmentTaskItemEntity item : task.getItems()) {
            if (item != null && item.getNxDstiLiveOrderId() != null) {
                liveOrderIds.add(item.getNxDstiLiveOrderId());
            }
        }
        return liveOrderIds;
    }
}
