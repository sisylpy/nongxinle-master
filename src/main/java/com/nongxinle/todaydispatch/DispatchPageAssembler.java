package com.nongxinle.todaydispatch;

import com.nongxinle.route.DisRouteDispatchLabels;
import com.nongxinle.route.DisRouteSandboxPageHeroImage;
import com.nongxinle.route.DisRouteSandboxTodayTimelineBuilder;
import com.nongxinle.route.DisRouteSandboxTodayViewModelMaps;
import com.nongxinle.route.DisRouteTemporalHelper;
import com.nongxinle.route.SandboxTodayMapPolylineLineTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** TodayDispatchResult → pageViewModel Map（字段对齐 DisRouteSandboxTodayViewModelMaps 契约）。 */
@Component
public class DispatchPageAssembler {

    @Autowired
    private TodayDispatchMapOverviewEnricher todayDispatchMapOverviewEnricher;

    private static final String SECTION_SUGGESTED = "SUGGESTED_DRIVER_ROUTES";
    private static final String SECTION_LOADING = "LOADING_DRIVER_ROUTES";
    private static final String SECTION_EXECUTION = "EXECUTION_DRIVER_ROUTES";
    private static final String SECTION_UNASSIGNED = "UNASSIGNED";
    private static final String TOP_METRICS_DISPATCH = "DISPATCH_SANDBOX";
    private static final String TOP_METRICS_LOADING = "LOADING_PAGE";
    private static final String TOP_METRICS_DELIVERY = "DELIVERY_PAGE";
    private static final String CARD_DRIVER_ROUTE = "DRIVER_ROUTE";
    private static final String CARD_UNASSIGNED = "UNASSIGNED_CUSTOMER";

    private static final String[] DRIVER_COLORS = {
            "#2563EB", "#DC2626", "#059669", "#D97706", "#7C3AED", "#0891B2"
    };
    private static final String[] DRIVER_COLOR_KEYS = {
            "blue", "red", "green", "orange", "purple", "cyan"
    };

    public Map<String, Object> assemble(TodayDispatchResult result) {
        Map<String, Object> raw = buildRawPageViewModel(result);
        Map<String, Object> contracted;
        if (isDeliveryPage(result)) {
            contracted = DisRouteSandboxTodayViewModelMaps.toDeliveryPageMap(raw);
        } else if (isLoadingPage(result)) {
            contracted = DisRouteSandboxTodayViewModelMaps.toLoadingPageMap(raw);
        } else {
            contracted = DisRouteSandboxTodayViewModelMaps.toDispatchPageMap(raw);
        }
        if (!contracted.containsKey("mapOverview")) {
            contracted.put("mapOverview", DisRouteSandboxTodayViewModelMaps.defaultDispatchMapOverview());
        }
        return contracted;
    }

    public Map<String, Object> filterDeliveryPageViewModelByDriver(Map<String, Object> pageViewModel,
                                                                   Integer driverUserId) {
        if (pageViewModel == null || driverUserId == null) {
            return pageViewModel;
        }
        Map<String, Object> filtered = new LinkedHashMap<String, Object>(pageViewModel);
        List<Map<String, Object>> sections = castSectionList(pageViewModel.get("sections"));
        List<Map<String, Object>> filteredSections = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> section : sections) {
            if (section == null) {
                continue;
            }
            List<Map<String, Object>> cards = castSectionList(section.get("cards"));
            List<Map<String, Object>> filteredCards = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> card : cards) {
                if (card == null) {
                    continue;
                }
                Object cardDriverId = card.get("driverUserId");
                if (cardDriverId != null && driverUserId.equals(Integer.valueOf(String.valueOf(cardDriverId)))) {
                    filteredCards.add(card);
                }
            }
            if (!filteredCards.isEmpty()) {
                Map<String, Object> sectionCopy = new LinkedHashMap<String, Object>(section);
                sectionCopy.put("cards", filteredCards);
                filteredSections.add(sectionCopy);
            }
        }
        filtered.put("sections", filteredSections);
        return filtered;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castSectionList(Object value) {
        if (value instanceof List) {
            return (List<Map<String, Object>>) value;
        }
        return Collections.emptyList();
    }

    private static boolean isLoadingPage(TodayDispatchResult result) {
        return result != null && TodayDispatchResult.PAGE_MODE_LOADING.equals(result.getPageMode());
    }

    private static boolean isDeliveryPage(TodayDispatchResult result) {
        return result != null && TodayDispatchResult.PAGE_MODE_DELIVERY.equals(result.getPageMode());
    }

    private Map<String, Object> buildRawPageViewModel(TodayDispatchResult result) {
        Map<String, Object> page = new LinkedHashMap<String, Object>();
        if (result == null) {
            return page;
        }
        page.put("topMetricsScope", resolveTopMetricsScope(result));
        page.put("pageHeader", buildPageHeader(result));
        page.put("topMetrics", buildTopMetrics(result));
        page.put("sections", buildSections(result));
        page.put("availableDrivers", isLoadingPage(result)
                ? Collections.<Map<String, Object>>emptyList()
                : result.getAvailableDrivers());
        page.put("mapOverview", buildMapOverview(result));
        return page;
    }

    private static String resolveTopMetricsScope(TodayDispatchResult result) {
        if (isDeliveryPage(result)) {
            return TOP_METRICS_DELIVERY;
        }
        if (isLoadingPage(result)) {
            return TOP_METRICS_LOADING;
        }
        return TOP_METRICS_DISPATCH;
    }

    private Map<String, Object> buildPageHeader(TodayDispatchResult result) {
        Map<String, Object> header = new LinkedHashMap<String, Object>();
        if (isDeliveryPage(result)) {
            header.put("title", "配送 · " + formatRouteDateLabel(result));
            header.put("scheduleBannerLine", buildScheduleBanner(result));
            header.put("operationHint", countDeliveryStops(result) > 0 ? "司机已出发，请关注配送进度" : "");
            header.put("statusLabel", countDeliveryDrivers(result) > 0 ? "配送中" : "暂无配送");
            header.put("statusTone", "info");
            header.put("heroImageType", "TRUCK");
            header.put("progress", buildDeliveryProgress(result));
            return header;
        }
        boolean loadingPage = isLoadingPage(result);
        header.put("title", loadingPage ? "今日派单 · 装车中" : "今日派单 · 分派中");
        header.put("scheduleBannerLine", buildScheduleBanner(result));
        header.put("operationHint", loadingPage
                ? "点击站点可调整时间窗"
                : "点击站点可调整时间窗；确认分派后进入装车");
        header.put("statusLabel", loadingPage ? "装车中" : "分派中");
        header.put("statusTone", "info");
        header.put("heroImageType", DisRouteSandboxPageHeroImage.SANDBOX);
        header.put("progress", buildProgress(result));
        return header;
    }

    private Map<String, Object> buildProgress(TodayDispatchResult result) {
        Map<String, Object> progress = new LinkedHashMap<String, Object>();
        int stopCount = countCustomerStops(result);
        if (stopCount <= 0) {
            progress.put("mainLine", isLoadingPage(result) ? "暂无装车路线" : "暂无待确认站点");
            return progress;
        }
        progress.put("mainLine", isLoadingPage(result)
                ? "装车中 " + stopCount + " 个客户站点"
                : "待确认 " + stopCount + " 个客户站点");
        progress.put("highlightText", String.valueOf(stopCount));
        return progress;
    }

    private Map<String, Object> buildDeliveryProgress(TodayDispatchResult result) {
        Map<String, Object> progress = new LinkedHashMap<String, Object>();
        int driverCount = countDeliveryDrivers(result);
        int stopCount = countDeliveryStops(result);
        if (driverCount <= 0 && stopCount <= 0) {
            progress.put("mainLine", "暂无配送路线");
            return progress;
        }
        if (driverCount > 0) {
            progress.put("mainLine", driverCount + " 名司机配送中 · " + stopCount + " 个客户");
        } else {
            progress.put("mainLine", stopCount + " 个客户配送中");
        }
        if (stopCount > 0) {
            progress.put("highlightText", String.valueOf(stopCount));
        }
        return progress;
    }

    private static String formatRouteDateLabel(TodayDispatchResult result) {
        return DisRouteTemporalHelper.formatRouteDateLabel(
                result.getRouteDate(), result.getServerNow());
    }

    private static int countDeliveryDrivers(TodayDispatchResult result) {
        return result != null && result.getSuggestedRoutes() != null
                ? result.getSuggestedRoutes().size() : 0;
    }

    private static int countDeliveryStops(TodayDispatchResult result) {
        return countCustomerStops(result);
    }

    private String buildScheduleBanner(TodayDispatchResult result) {
        String dateLabel = DisRouteTemporalHelper.formatRouteDateLabel(
                result.getRouteDate(), result.getServerNow());
        String batchLabel = DisRouteDispatchLabels.label(result.getBatchCode());
        return dateLabel + " · " + batchLabel;
    }

    private Map<String, Object> buildTopMetrics(TodayDispatchResult result) {
        Map<String, Object> metrics = new LinkedHashMap<String, Object>();
        int routeCount = result.getSuggestedRoutes() != null ? result.getSuggestedRoutes().size() : 0;
        metrics.put("driverCount", routeCount);
        metrics.put("customerStopCount", countCustomerStops(result));
        long totalDistanceM = 0L;
        long totalDurationS = 0L;
        if (result.getSuggestedRoutes() != null) {
            for (DriverRoutePlan route : result.getSuggestedRoutes()) {
                if (route == null) {
                    continue;
                }
                if (route.getTotalDistanceM() != null) {
                    totalDistanceM += route.getTotalDistanceM();
                }
                if (route.getTotalDurationS() != null) {
                    totalDurationS += route.getTotalDurationS();
                }
            }
        }
        if (totalDistanceM > 0L) {
            metrics.put("totalDistanceText",
                    com.nongxinle.route.DisRouteSandboxDisplayFormatHelper.formatDistanceText(totalDistanceM));
        }
        if (totalDurationS > 0L) {
            metrics.put("totalDurationText",
                    com.nongxinle.route.DisRouteSandboxDisplayFormatHelper.formatDurationText(totalDurationS));
        }
        return metrics;
    }

    private List<Map<String, Object>> buildSections(TodayDispatchResult result) {
        List<Map<String, Object>> sections = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> routeCards = buildDriverRouteCards(result);
        if (!routeCards.isEmpty()) {
            Map<String, Object> routeSection = new LinkedHashMap<String, Object>();
            if (isDeliveryPage(result)) {
                routeSection.put("sectionKey", SECTION_EXECUTION);
                routeSection.put("title", "配送中");
                routeSection.put("description", "司机已出发，配送执行中");
            } else {
                routeSection.put("sectionKey", isLoadingPage(result) ? SECTION_LOADING : SECTION_SUGGESTED);
                routeSection.put("title", "建议派车路线");
            }
            routeSection.put("cards", routeCards);
            sections.add(routeSection);
        }
        if (isLoadingPage(result) || isDeliveryPage(result)) {
            return sections;
        }
        List<Map<String, Object>> unassignedCards = buildUnassignedCards(result);
        if (!unassignedCards.isEmpty()) {
            Map<String, Object> unassigned = new LinkedHashMap<String, Object>();
            unassigned.put("sectionKey", SECTION_UNASSIGNED);
            unassigned.put("title", "待分配客户");
            unassigned.put("description", "以下客户暂未分配司机，可人工调度");
            unassigned.put("cards", unassignedCards);
            sections.add(unassigned);
        }
        return sections;
    }

    private List<Map<String, Object>> buildDriverRouteCards(TodayDispatchResult result) {
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        if (result.getSuggestedRoutes() == null) {
            return cards;
        }
        for (DriverRoutePlan route : result.getSuggestedRoutes()) {
            if (route == null || route.getDriverUserId() == null) {
                continue;
            }
            Map<String, Object> card = new LinkedHashMap<String, Object>();
            card.put("cardType", CARD_DRIVER_ROUTE);
            card.put("driverUserId", route.getDriverUserId());
            card.put("driverName", route.getDriverName());
            if (isDeliveryPage(result)) {
                card.put("driverStatusLabel", "配送中");
                card.put("driverStatusTone", "execution");
                card.put("badgeLabel", route.getBadgeLabel() != null ? route.getBadgeLabel() : "配送中");
            } else {
                card.put("driverStatusLabel", "建议路线");
                card.put("driverStatusTone", "suggested");
                card.put("badgeLabel", route.getBadgeLabel() != null ? route.getBadgeLabel() : "建议路线");
            }
            card.put("customerStopCount", route.getStops() != null ? route.getStops().size() : 0);
            card.put("scheduleHeadline", route.getScheduleHeadline());
            card.put("routeSummary", route.getRouteSummary());
            card.put("routeStatsLine", route.getRouteStatsLine());
            card.put("routeHeadlineLine", route.getRouteHeadlineLine());
            card.put("routeRoundTripSummaryLine", route.getRouteRoundTripSummaryLine());
            card.put("totalDistanceText", route.getTotalRoundTripDistanceText());
            card.put("totalDurationText", route.getTotalRoundTripDurationText());
            card.put("outboundDistanceText", route.getOutboundDistanceText());
            card.put("returnDistanceText", route.getReturnDistanceText());
            card.put("totalRoundTripDistanceText", route.getTotalRoundTripDistanceText());
            card.put("totalRoundTripDurationText", route.getTotalRoundTripDurationText());
            card.put("plannedDepartLabel", route.getPlannedDepartLabel());
            card.put("plannedReturnLabel", route.getPlannedReturnLabel());
            card.put("recommendedDepartLabel", route.getRecommendedDepartLabel());
            card.put("routeSuggestedDepartLabel", route.getRouteSuggestedDepartLabel());
            card.put("firstStopWindowLabel", route.getFirstStopWindowLabel());
            card.put("firstStopPlannedArrivalLabel", route.getFirstStopPlannedArrivalLabel());
            card.put("firstStopPlannedArrivalTimeLabel", route.getFirstStopPlannedArrivalTimeLabel());
            card.put("firstStopArrivalStatusLabel", route.getFirstStopArrivalStatusLabel());
            card.put("firstStopArrivalStatusTone", route.getFirstStopArrivalStatusTone());
            card.put("firstStopWindowStartS", route.getFirstStopWindowStartS());
            card.put("firstStopWindowEndS", route.getFirstStopWindowEndS());
            card.put("depotName", result.getDepotName());
            card.put("depotAddress", result.getDepotAddress());
            card.put("timeline", buildRouteTimeline(route, result));
            if (route.getPrimaryAction() != null) {
                card.put("primaryAction", route.getPrimaryAction());
            }
            if (route.getRouteEditAction() != null) {
                card.put("routeEditAction", route.getRouteEditAction());
            }
            cards.add(card);
        }
        return cards;
    }

    private List<Map<String, Object>> buildRouteTimeline(DriverRoutePlan route, TodayDispatchResult result) {
        List<Map<String, Object>> timeline = new ArrayList<Map<String, Object>>();

        if (route.getStops() != null) {
            for (CustomerStopPlan stop : route.getStops()) {
                if (stop == null) {
                    continue;
                }
                Map<String, Object> stopNode = buildStopTimelineNode(stop);
                String legText = joinLegText(stop.getDistanceText(), stop.getDurationText());
                if (legText != null) {
                    stopNode.put("legText", legText);
                }
                timeline.add(stopNode);
            }
        }

        Map<String, Object> end = new LinkedHashMap<String, Object>();
        end.put("type", "end");
        end.put("marker", "终");
        end.put("name", "回仓");
        end.put("timeRight", route.getPlannedReturnLabel());
        if (route.getReturnLegLabel() != null && !route.getReturnLegLabel().trim().isEmpty()) {
            end.put("legText", route.getReturnLegLabel().trim());
        }
        if (isDeliveryPage(result) && areAllStopsDelivered(route)) {
            end.put("pointStatusLabel", "返仓完成");
        }
        timeline.add(end);

        DisRouteSandboxTodayTimelineBuilder.enrichStopLegPresentation(timeline);
        for (Map<String, Object> node : timeline) {
            if (node == null || !"stop".equals(node.get("type"))) {
                continue;
            }
            if (node.get("legText") == null && node.get("legDistanceLabel") != null) {
                node.put("legText", node.get("legDistanceLabel"));
            }
        }
        return timeline;
    }

    private static boolean areAllStopsDelivered(DriverRoutePlan route) {
        if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
            return false;
        }
        for (CustomerStopPlan stop : route.getStops()) {
            if (stop == null) {
                continue;
            }
            String status = stop.getStatusLabel();
            if (status == null || status.trim().isEmpty()) {
                return false;
            }
            String normalized = status.trim();
            if (!normalized.contains("送达") && !"已完成".equals(normalized)) {
                return false;
            }
        }
        return true;
    }

    private static String formatTimelineDepartRight(String plannedDepartLabel) {
        if (plannedDepartLabel == null || plannedDepartLabel.trim().isEmpty()) {
            return "现在 出发";
        }
        String trimmed = plannedDepartLabel.trim();
        if (trimmed.endsWith("出发")) {
            return trimmed;
        }
        return trimmed + " 出发";
    }

    private Map<String, Object> buildStopTimelineNode(CustomerStopPlan stop) {
        Map<String, Object> node = DispatchStopCardTemplate.buildStoreStopMap(stop);
        node.put("type", "stop");
        node.put("seq", stop.getSequence());
        putIfNotNull(node, "estimateArriveTime", stop.getPlannedArrivalLabel());
        putIfNotNull(node, "estimateLeaveTime", stop.getPlannedDepartureLabel());
        putIfNotNull(node, "customerTimeWindow", stop.getCustomerWindowLabel());
        putIfNotNull(node, "systemEta", stop.getPlannedArrivalLabel());
        node.put("name", stop.getCustomerName());
        return node;
    }

    private List<Map<String, Object>> buildUnassignedCards(TodayDispatchResult result) {
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        if (result.getUnassignedStops() == null) {
            return cards;
        }
        for (CustomerStopPlan stop : result.getUnassignedStops()) {
            if (stop == null) {
                continue;
            }
            Map<String, Object> card = DispatchStopCardTemplate.buildStoreStopMap(stop);
            card.put("cardType", CARD_UNASSIGNED);
            card.put("badgeLabel", "未分配");
            card.put("driverLabel", "无司机");
            putIfNotNull(card, "unassignedDriverHint", stop.getUnassignedDriverHint());
            if (stop.getPrimaryAction() != null) {
                card.put("primaryAction", stop.getPrimaryAction());
            }
            cards.add(card);
        }
        return cards;
    }

    private Map<String, Object> buildMapOverview(TodayDispatchResult result) {
        Map<String, Object> overview = new LinkedHashMap<String, Object>();
        Map<String, Object> summary = new LinkedHashMap<String, Object>();
        summary.put("customerStopCount", countCustomerStops(result));
        summary.put("routeCount", result.getSuggestedRoutes() != null ? result.getSuggestedRoutes().size() : 0);
        summary.put("unassignedCount", result.getUnassignedStops() != null ? result.getUnassignedStops().size() : 0);
        overview.put("summary", summary);

        Map<String, Object> depot = buildDepotMarker(result);
        overview.put("depot", depot);

        List<Map<String, Object>> markers = buildMarkers(result);
        List<Map<String, Object>> polylines = buildPolylines(result, markers);
        overview.put("markers", markers);
        overview.put("polylines", polylines);
        overview.put("legend", buildLegend(result, markers));
        overview.put("missingCoordinateStops", buildMissingCoordinateStops(result));

        applyViewport(overview, depot, markers);
        overview.put("hasMap", Boolean.valueOf(!markers.isEmpty() || !polylines.isEmpty()));
        putIfNotNull(overview, "subkey", result.getMapSubkey());
        putIfNotNull(overview, "layerStyle", result.getMapLayerStyle());
        if (todayDispatchMapOverviewEnricher != null) {
            todayDispatchMapOverviewEnricher.enrich(overview);
        }
        return overview;
    }

    private Map<String, Object> buildDepotMarker(TodayDispatchResult result) {
        Map<String, Object> depot = new LinkedHashMap<String, Object>();
        if (result.getDepotLat() != null) {
            depot.put("lat", result.getDepotLat());
        }
        if (result.getDepotLng() != null) {
            depot.put("lng", result.getDepotLng());
        }
        depot.put("name", result.getDepotName() != null ? result.getDepotName() : "市场");
        depot.put("address", result.getDepotAddress());
        depot.put("colorKey", "depot");
        depot.put("color", "#111827");
        return depot;
    }

    private List<Map<String, Object>> buildMarkers(TodayDispatchResult result) {
        List<Map<String, Object>> markers = new ArrayList<Map<String, Object>>();
        int colorIdx = 0;
        if (result.getSuggestedRoutes() == null) {
            return markers;
        }
        for (DriverRoutePlan route : result.getSuggestedRoutes()) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            String color = DRIVER_COLORS[colorIdx % DRIVER_COLORS.length];
            String colorKey = DRIVER_COLOR_KEYS[colorIdx % DRIVER_COLOR_KEYS.length];
            colorIdx++;
            int seq = 0;
            for (CustomerStopPlan stop : route.getStops()) {
                if (stop == null || stop.getLat() == null || stop.getLng() == null) {
                    continue;
                }
                seq++;
                Map<String, Object> marker = new LinkedHashMap<String, Object>();
                marker.put("lat", stop.getLat());
                marker.put("lng", stop.getLng());
                marker.put("title", stop.getCustomerName());
                marker.put("customerName", stop.getCustomerName());
                marker.put("driverUserId", route.getDriverUserId());
                marker.put("driverName", route.getDriverName());
                marker.put("markerType", "CUSTOMER_STOP");
                marker.put("colorKey", colorKey);
                marker.put("color", color);
                marker.put("stopSeq", seq);
                marker.put("depFatherId", stop.getDepFatherId());
                marker.put("displayTitle", compactMapCustomerName(stop.getCustomerName()));
                marker.put("assignmentLabel", route.getDriverName());
                marker.put("badgeText", String.valueOf(seq));
                putIfNotNull(marker, "arrivalLabel", stop.getPlannedArrivalLabel());
                putIfNotNull(marker, "displaySubtitle", stop.getPlannedArrivalLabel());
                markers.add(marker);
            }
        }
        if (result.getUnassignedStops() != null) {
            for (CustomerStopPlan stop : result.getUnassignedStops()) {
                if (stop == null || stop.getLat() == null || stop.getLng() == null) {
                    continue;
                }
                Map<String, Object> marker = new LinkedHashMap<String, Object>();
                marker.put("lat", stop.getLat());
                marker.put("lng", stop.getLng());
                marker.put("title", stop.getCustomerName());
                marker.put("customerName", stop.getCustomerName());
                marker.put("markerType", "UNASSIGNED_CUSTOMER");
                marker.put("colorKey", "unassigned");
                marker.put("color", "#9CA3AF");
                marker.put("depFatherId", stop.getDepFatherId());
                marker.put("displayTitle", compactMapCustomerName(stop.getCustomerName()));
                marker.put("assignmentLabel", "未分配");
                marker.put("badgeText", "?");
                markers.add(marker);
            }
        }
        return markers;
    }

    private List<Map<String, Object>> buildPolylines(TodayDispatchResult result,
                                                     List<Map<String, Object>> markers) {
        List<Map<String, Object>> polylines = new ArrayList<Map<String, Object>>();
        if (result.getSuggestedRoutes() == null) {
            return polylines;
        }
        int colorIdx = 0;
        for (DriverRoutePlan route : result.getSuggestedRoutes()) {
            if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
                continue;
            }
            String color = DRIVER_COLORS[colorIdx % DRIVER_COLORS.length];
            String colorKey = DRIVER_COLOR_KEYS[colorIdx % DRIVER_COLOR_KEYS.length];
            colorIdx++;
            List<Map<String, Object>> points = new ArrayList<Map<String, Object>>();
            if (result.getDepotLat() != null && result.getDepotLng() != null) {
                points.add(point(result.getDepotLat(), result.getDepotLng()));
            }
            for (CustomerStopPlan stop : route.getStops()) {
                if (stop != null && stop.getLat() != null && stop.getLng() != null) {
                    points.add(point(stop.getLat(), stop.getLng()));
                }
            }
            if (result.getDepotLat() != null && result.getDepotLng() != null && points.size() > 1) {
                points.add(point(result.getDepotLat(), result.getDepotLng()));
            }
            if (points.size() < 2) {
                continue;
            }
            Map<String, Object> polyline = new LinkedHashMap<String, Object>();
            polyline.put("routeKey", "driver-" + route.getDriverUserId());
            polyline.put("driverUserId", route.getDriverUserId());
            polyline.put("driverName", route.getDriverName());
            polyline.put("colorKey", colorKey);
            polyline.put("color", color);
            polyline.put("kind", "DRIVER");
            polyline.put("lineStyle", "solid");
            polyline.put("lineType", SandboxTodayMapPolylineLineTypes.STRAIGHT);
            polyline.put("points", points);
            polylines.add(polyline);
        }
        return polylines;
    }

    private List<Map<String, Object>> buildLegend(TodayDispatchResult result,
                                                  List<Map<String, Object>> markers) {
        List<Map<String, Object>> legend = new ArrayList<Map<String, Object>>();
        if (result.getDepotLat() != null && result.getDepotLng() != null) {
            Map<String, Object> depotItem = new LinkedHashMap<String, Object>();
            depotItem.put("kind", "DEPOT");
            depotItem.put("colorKey", "depot");
            depotItem.put("color", "#111827");
            depotItem.put("label", result.getDepotName() != null ? result.getDepotName() : "市场");
            depotItem.put("lineStyle", "solid");
            legend.add(depotItem);
        }
        if (result.getSuggestedRoutes() != null) {
            int colorIdx = 0;
            for (DriverRoutePlan route : result.getSuggestedRoutes()) {
                if (route == null || route.getDriverUserId() == null) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<String, Object>();
                item.put("kind", "DRIVER");
                item.put("colorKey", DRIVER_COLOR_KEYS[colorIdx % DRIVER_COLOR_KEYS.length]);
                item.put("color", DRIVER_COLORS[colorIdx % DRIVER_COLORS.length]);
                item.put("label", route.getDriverName());
                item.put("lineStyle", "solid");
                item.put("driverUserId", route.getDriverUserId());
                legend.add(item);
                colorIdx++;
            }
        }
        if (!isLoadingPage(result) && !isDeliveryPage(result)) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("kind", "UNASSIGNED");
            item.put("colorKey", "unassigned");
            item.put("color", "#9CA3AF");
            item.put("label", "未分配");
            item.put("lineStyle", "solid");
            legend.add(item);
        }
        return legend;
    }

    private List<Map<String, Object>> buildMissingCoordinateStops(TodayDispatchResult result) {
        List<Map<String, Object>> missing = new ArrayList<Map<String, Object>>();
        collectMissing(result.getSuggestedRoutes(), missing);
        if (result.getUnassignedStops() != null) {
            for (CustomerStopPlan stop : result.getUnassignedStops()) {
                if (stop == null || (stop.getLat() != null && stop.getLng() != null)) {
                    continue;
                }
                missing.add(missingStop(stop, null, true));
            }
        }
        return missing;
    }

    private void collectMissing(List<DriverRoutePlan> routes, List<Map<String, Object>> missing) {
        if (routes == null) {
            return;
        }
        for (DriverRoutePlan route : routes) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            for (CustomerStopPlan stop : route.getStops()) {
                if (stop == null || (stop.getLat() != null && stop.getLng() != null)) {
                    continue;
                }
                missing.add(missingStop(stop, route, false));
            }
        }
    }

    private Map<String, Object> missingStop(CustomerStopPlan stop,
                                            DriverRoutePlan route,
                                            boolean unassigned) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("customerName", stop.getCustomerName());
        item.put("departmentId", stop.getDepFatherId());
        item.put("depFatherId", stop.getDepFatherId());
        if (route != null) {
            item.put("driverName", route.getDriverName());
            item.put("driverUserId", route.getDriverUserId());
            item.put("assignmentLabel", route.getDriverName());
        } else if (unassigned) {
            item.put("assignmentLabel", "未分配");
        }
        return item;
    }

    private void applyViewport(Map<String, Object> overview,
                               Map<String, Object> depot,
                               List<Map<String, Object>> markers) {
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        int count = 0;
        if (markers != null) {
            for (Map<String, Object> marker : markers) {
                if (marker == null) {
                    continue;
                }
                Double lat = toDouble(marker.get("lat"));
                Double lng = toDouble(marker.get("lng"));
                if (lat == null || lng == null) {
                    continue;
                }
                minLat = Math.min(minLat, lat);
                maxLat = Math.max(maxLat, lat);
                minLng = Math.min(minLng, lng);
                maxLng = Math.max(maxLng, lng);
                count++;
            }
        }
        if (count <= 0 && depot != null) {
            Double lat = toDouble(depot.get("lat"));
            Double lng = toDouble(depot.get("lng"));
            if (lat != null && lng != null) {
                minLat = maxLat = lat;
                minLng = maxLng = lng;
                count = 1;
            }
        }
        if (count <= 0) {
            return;
        }
        double latSpan = Math.max(maxLat - minLat, 0.012);
        double lngSpan = Math.max(maxLng - minLng, 0.012);
        overview.put("centerLat", (minLat + maxLat) / 2.0);
        overview.put("centerLng", (minLng + maxLng) / 2.0);
        overview.put("suggestedScale", suggestScale(latSpan, lngSpan));
    }

    private static int suggestScale(double latSpan, double lngSpan) {
        double span = Math.max(latSpan, lngSpan);
        if (span <= 0.004) {
            return 14;
        }
        if (span <= 0.018) {
            return 13;
        }
        if (span <= 0.045) {
            return 12;
        }
        return 11;
    }

    private static Map<String, Object> point(double lat, double lng) {
        Map<String, Object> p = new LinkedHashMap<String, Object>();
        p.put("lat", lat);
        p.put("lng", lng);
        return p;
    }

    private static String joinLegText(String distanceText, String durationText) {
        if (distanceText != null && durationText != null) {
            return distanceText + " · " + durationText;
        }
        if (distanceText != null) {
            return distanceText;
        }
        return durationText;
    }

    private static String compactMapCustomerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        String trimmed = name.trim();
        return trimmed.length() <= 8 ? trimmed : trimmed.substring(0, 8);
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private static Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.valueOf(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static int countCustomerStops(TodayDispatchResult result) {
        int count = 0;
        if (result.getSuggestedRoutes() != null) {
            for (DriverRoutePlan route : result.getSuggestedRoutes()) {
                if (route != null && route.getStops() != null) {
                    count += route.getStops().size();
                }
            }
        }
        if (result.getUnassignedStops() != null) {
            count += result.getUnassignedStops().size();
        }
        return count;
    }

    /** 路线编辑页 driver 摘要：与分派中司机卡三列指标 + 往返 footer 同口径。 */
    public Map<String, Object> buildRouteEditDriverSummary(DriverRoutePlan route) {
        Map<String, Object> driver = new LinkedHashMap<String, Object>();
        if (route == null) {
            return driver;
        }
        driver.put("driverUserId", route.getDriverUserId());
        driver.put("driverName", route.getDriverName());
        int stopCount = route.getStops() != null ? route.getStops().size() : 0;
        driver.put("customerStopCount", stopCount);
        driver.put("totalDistanceText", route.getTotalRoundTripDistanceText());
        driver.put("totalDurationText", route.getTotalRoundTripDurationText());
        driver.put("totalRoundTripDistanceText", route.getTotalRoundTripDistanceText());
        driver.put("totalRoundTripDurationText", route.getTotalRoundTripDurationText());
        driver.put("plannedDepartLabel", route.getPlannedDepartLabel());
        driver.put("plannedReturnLabel", route.getPlannedReturnLabel());
        driver.put("firstStopPlannedArrivalTimeLabel", route.getFirstStopPlannedArrivalTimeLabel());
        driver.put("firstStopArrivalStatusLabel", route.getFirstStopArrivalStatusLabel());
        driver.put("firstStopArrivalStatusTone", route.getFirstStopArrivalStatusTone());
        driver.put("routeSummary", route.getRouteSummary());
        return driver;
    }

    /** 路线编辑页：单司机路线 + 可添加未分配站点的地图。 */
    public Map<String, Object> buildRouteEditMapOverview(TodayDispatchResult context,
                                                          DriverRoutePlan route,
                                                          List<CustomerStopPlan> addableStops) {
        TodayDispatchResult slice = copyMapContext(context);
        slice.setSuggestedRoutes(route != null
                ? Collections.singletonList(route) : Collections.<DriverRoutePlan>emptyList());
        slice.setUnassignedStops(addableStops != null
                ? addableStops : Collections.<CustomerStopPlan>emptyList());
        return buildMapOverview(slice);
    }

    /** 路线编辑页 timeline（与分派中司机卡同口径）。 */
    public List<Map<String, Object>> buildRouteEditTimeline(DriverRoutePlan route,
                                                            TodayDispatchResult context) {
        if (route == null) {
            return Collections.emptyList();
        }
        return buildRouteTimeline(route, context);
    }

    private static TodayDispatchResult copyMapContext(TodayDispatchResult context) {
        TodayDispatchResult slice = new TodayDispatchResult();
        if (context == null) {
            return slice;
        }
        slice.setPageMode(context.getPageMode());
        slice.setDisId(context.getDisId());
        slice.setRouteDate(context.getRouteDate());
        slice.setBatchCode(context.getBatchCode());
        slice.setDepotLat(context.getDepotLat());
        slice.setDepotLng(context.getDepotLng());
        slice.setDepotName(context.getDepotName());
        slice.setDepotAddress(context.getDepotAddress());
        slice.setMapSubkey(context.getMapSubkey());
        slice.setMapLayerStyle(context.getMapLayerStyle());
        return slice;
    }
}
