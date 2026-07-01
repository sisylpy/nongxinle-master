package com.nongxinle.dispatch.core.view;

import com.nongxinle.dispatch.core.domain.DispatchPageMode;
import com.nongxinle.dispatch.core.domain.DispatchTenantRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 将 {@link DispatchPageViewModel} 序列化为 API JSON Map（与 Nx pageViewModel 契约对齐）。 */
public final class DispatchPageViewModelMaps {

    private DispatchPageViewModelMaps() {
    }

    public static Map<String, Object> toMap(DispatchPageViewModel vm) {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        if (vm == null) {
            return root;
        }
        if (vm.getPageMode() != null) {
            root.put("pageMode", vm.getPageMode().name());
        }
        if (vm.getTenant() != null) {
            root.put("communityId", vm.getTenant().getTenantId());
            root.put("tenant", toTenantMap(vm.getTenant()));
        }
        putIfNotNull(root, "routeDate", vm.getRouteDate());
        putIfNotNull(root, "topMetricsScope", vm.getTopMetricsScope());
        root.put("pageHeader", toHeaderMap(vm.getPageHeader()));
        root.put("topMetrics", toTopMetricsMap(vm.getTopMetrics()));
        root.put("sections", toSectionsList(vm.getSections()));
        root.put("availableDrivers", toAvailableDriversList(vm.getAvailableDrivers()));
        root.put("mapOverview", toMapOverviewMap(vm.getMapOverview()));
        putIfNotNull(root, "showDepartAction", vm.getShowDepartAction());
        putIfNotNull(root, "departActionEnabled", vm.getDepartActionEnabled());
        putIfNotNull(root, "departActionLabel", vm.getDepartActionLabel());
        putIfNotNull(root, "driverRouteId", vm.getDriverRouteId());
        return root;
    }

    public static Map<String, Object> defaultMapOverview() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("summary", defaultSummaryMap());
        map.put("depot", Collections.emptyMap());
        map.put("summaryText", "");
        map.put("markers", Collections.emptyList());
        map.put("polylines", Collections.emptyList());
        map.put("legend", Collections.emptyList());
        map.put("missingCoordinateStops", Collections.emptyList());
        map.put("emptyHint", "暂无地图数据");
        return map;
    }

    private static Map<String, Object> defaultSummaryMap() {
        Map<String, Object> summary = new LinkedHashMap<String, Object>();
        summary.put("customerStopCount", 0);
        summary.put("routeCount", 0);
        summary.put("unassignedCount", 0);
        return summary;
    }

    private static Map<String, Object> toTenantMap(DispatchTenantRef tenant) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (tenant.getTenantType() != null) {
            map.put("tenantType", tenant.getTenantType().name());
        }
        map.put("tenantId", tenant.getTenantId());
        putIfNotNull(map, "batchCode", tenant.getBatchCode());
        return map;
    }

    private static Map<String, Object> toHeaderMap(DispatchPageViewModel.DispatchPageHeader header) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (header == null) {
            map.put("progress", defaultProgressMap());
            return map;
        }
        putIfNotNull(map, "title", header.getTitle());
        putIfNotNull(map, "subtitle", header.getSubtitle());
        putIfNotNull(map, "routeDateLabel", header.getRouteDateLabel());
        putIfNotNull(map, "depotName", header.getDepotName());
        putIfNotNull(map, "depotAddress", header.getDepotAddress());
        if (header.getProgress() != null) {
            Map<String, Object> progress = new LinkedHashMap<String, Object>();
            putIfNotNull(progress, "mainLine", header.getProgress().getMainLine());
            putIfNotNull(progress, "highlightText", header.getProgress().getHighlightText());
            map.put("progress", progress);
        } else {
            map.put("progress", defaultProgressMap());
        }
        return map;
    }

    private static Map<String, Object> defaultProgressMap() {
        Map<String, Object> progress = new LinkedHashMap<String, Object>();
        progress.put("mainLine", "暂无待确认站点");
        return progress;
    }

    private static Map<String, Object> toTopMetricsMap(DispatchPageViewModel.DispatchTopMetrics metrics) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (metrics == null) {
            return map;
        }
        putIfNotNull(map, "unassignedStopCount", metrics.getUnassignedStopCount());
        putIfNotNull(map, "assignedStopCount", metrics.getAssignedStopCount());
        putIfNotNull(map, "activeRouteCount", metrics.getActiveRouteCount());
        putIfNotNull(map, "availableDriverCount", metrics.getAvailableDriverCount());
        if (metrics.getExtra() != null && !metrics.getExtra().isEmpty()) {
            map.putAll(metrics.getExtra());
        }
        return map;
    }

    private static List<Map<String, Object>> toSectionsList(List<DispatchPageViewModel.DispatchPageSection> sections) {
        if (sections == null || sections.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (DispatchPageViewModel.DispatchPageSection section : sections) {
            if (section == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            putIfNotNull(item, "sectionKey", section.getSectionKey());
            putIfNotNull(item, "title", section.getTitle());
            putIfNotNull(item, "description", section.getDescription());
            item.put("cards", toCardsList(section.getCards()));
            list.add(item);
        }
        return list;
    }

    private static List<Map<String, Object>> toCardsList(List<DispatchSectionCard> cards) {
        if (cards == null || cards.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (DispatchSectionCard card : cards) {
            if (card == null) {
                continue;
            }
            if (card instanceof DispatchRouteCard) {
                list.add(toRouteCardMap((DispatchRouteCard) card));
            } else if (card instanceof DispatchStopCard) {
                list.add(toStopCardMap((DispatchStopCard) card));
            }
        }
        return list;
    }

    private static Map<String, Object> toStopCardMap(DispatchStopCard card) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        putIfNotNull(map, "cardType", card.getCardType());
        putIfNotNull(map, "cardKey", card.getCardKey());
        putIfNotNull(map, "badgeLabel", card.getBadgeLabel());
        putIfNotNull(map, "driverLabel", card.getDriverLabel());
        putIfNotNull(map, "customerName", card.getCustomerName());
        putIfNotNull(map, "goodsSummary", card.getGoodsSummary());
        putIfNotNull(map, "legText", card.getLegText());
        putIfNotNull(map, "distanceText", card.getDistanceText());
        putIfNotNull(map, "durationText", card.getDurationText());
        putIfNotNull(map, "plannedArrivalLabel", card.getPlannedArrivalLabel());
        putIfNotNull(map, "plannedDepartureLabel", card.getPlannedDepartureLabel());
        putIfNotNull(map, "serviceDurationLabel", card.getServiceDurationLabel());
        putIfNotNull(map, "arrivalStatusLabel", card.getArrivalStatusLabel());
        putIfNotNull(map, "arrivalStatusTone", card.getArrivalStatusTone());
        putIfNotNull(map, "customerWindowLabel", card.getCustomerWindowLabel());
        putIfNotNull(map, "windowRequirementLabel", card.getWindowRequirementLabel());
        if (Boolean.TRUE.equals(card.getWindowRequirementModified())) {
            map.put("windowRequirementModified", Boolean.TRUE);
        }
        putIfNotNull(map, "addressText", card.getAddressText());
        putIfNotNull(map, "statusLabel", card.getStatusLabel());
        putIfNotNull(map, "sandboxStopKey", card.getSandboxStopKey());
        putIfNotNull(map, "addressId", card.getAddressId());
        putIfNotNull(map, "suggestedDriverUserId", card.getSuggestedDriverUserId());
        putIfNotNull(map, "orderCount", card.getOrderCount());
        if (card.getOrderIds() != null && !card.getOrderIds().isEmpty()) {
            map.put("orderIds", card.getOrderIds());
        }
        if (card.getPrimaryAction() != null) {
            map.put("primaryAction", toPrimaryActionMap(card.getPrimaryAction()));
        }
        return map;
    }

    private static Map<String, Object> toRouteCardMap(DispatchRouteCard card) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        putIfNotNull(map, "cardType", card.getCardType());
        putIfNotNull(map, "cardKey", card.getCardKey());
        putIfNotNull(map, "driverRouteId", card.getDriverRouteId());
        putIfNotNull(map, "driverUserId", card.getDriverUserId());
        putIfNotNull(map, "driverName", card.getDriverName());
        putIfNotNull(map, "driverStatusLabel", card.getRouteStatusLabel());
        putIfNotNull(map, "driverStatusTone", card.getDriverStatusTone());
        putIfNotNull(map, "customerStopCount", card.getStopCount());
        putIfNotNull(map, "routeSummary", card.getRouteSummary());
        putIfNotNull(map, "routeStatsLine", card.getRouteStatsLine());
        putIfNotNull(map, "plannedDepartLabel", card.getPlannedDepartLabel());
        putIfNotNull(map, "plannedReturnLabel", card.getPlannedReturnLabel());
        putIfNotNull(map, "actualDepartAtLabel", card.getActualDepartAtLabel());
        putIfNotNull(map, "actualDepartStatusLabel", card.getActualDepartStatusLabel());
        putIfNotNull(map, "actualDepartStatusTone", card.getActualDepartStatusTone());
        putIfNotNull(map, "totalDistanceText", card.getTotalDistanceText());
        putIfNotNull(map, "totalDurationText", card.getTotalDurationText());
        putIfNotNull(map, "totalRoundTripDistanceText", card.getTotalRoundTripDistanceText());
        putIfNotNull(map, "totalRoundTripDurationText", card.getTotalRoundTripDurationText());
        putIfNotNull(map, "firstStopPlannedArrivalTimeLabel", card.getFirstStopPlannedArrivalTimeLabel());
        putIfNotNull(map, "firstStopArrivalStatusLabel", card.getFirstStopArrivalStatusLabel());
        putIfNotNull(map, "firstStopArrivalStatusTone", card.getFirstStopArrivalStatusTone());
        putIfNotNull(map, "routeHeadlineLine", card.getRouteHeadlineLine());
        putIfNotNull(map, "routeRoundTripSummaryLine", card.getRouteRoundTripSummaryLine());
        putIfNotNull(map, "deliveryProgressPercent", card.getDeliveryProgressPercent());
        putIfNotNull(map, "deliveryProgressLine", card.getDeliveryProgressLine());
        map.put("timeline", toTimelineList(card.getTimeline()));
        if (card.getRouteEditAction() != null) {
            map.put("routeEditAction", toPrimaryActionMap(card.getRouteEditAction()));
        }
        if (card.getPrimaryAction() != null) {
            map.put("primaryAction", toPrimaryActionMap(card.getPrimaryAction()));
        }
        return map;
    }

    private static List<Map<String, Object>> toTimelineList(List<DispatchTimelineItem> timeline) {
        if (timeline == null || timeline.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (DispatchTimelineItem item : timeline) {
            if (item == null) {
                continue;
            }
            Map<String, Object> node = new LinkedHashMap<String, Object>();
            putIfNotNull(node, "type", item.getType());
            putIfNotNull(node, "seq", item.getRouteSeq());
            putIfNotNull(node, "legText", item.getLegText());
            putIfNotNull(node, "legDistanceLabel", item.getLegDistanceLabel());
            putIfNotNull(node, "legRole", item.getLegRole());
            putIfNotNull(node, "marker", item.getMarker());
            putIfNotNull(node, "name", item.getName());
            putIfNotNull(node, "timeRight", item.getTimeRight());
            putIfNotNull(node, "distanceText", item.getDistanceText());
            putIfNotNull(node, "durationText", item.getDurationText());
            putIfNotNull(node, "plannedArrivalLabel", item.getPlannedArrivalLabel());
            putIfNotNull(node, "plannedDepartureLabel", item.getPlannedDepartureLabel());
            putIfNotNull(node, "arrivalFieldLabel", item.getArrivalFieldLabel());
            putIfNotNull(node, "departureFieldLabel", item.getDepartureFieldLabel());
            putIfNotNull(node, "serviceDurationLabel", item.getServiceDurationLabel());
            putIfNotNull(node, "arrivalStatusLabel", item.getArrivalStatusLabel());
            putIfNotNull(node, "arrivalStatusTone", item.getArrivalStatusTone());
            putIfNotNull(node, "customerWindowLabel", item.getCustomerWindowLabel());
            putIfNotNull(node, "windowRequirementLabel", item.getWindowRequirementLabel());
            if (Boolean.TRUE.equals(item.getWindowRequirementModified())) {
                node.put("windowRequirementModified", Boolean.TRUE);
            }
            putIfNotNull(node, "customerName", item.getTitle());
            putIfNotNull(node, "goodsSummary", item.getSubtitle());
            putIfNotNull(node, "statusLabel", item.getStatusLabel());
            putIfNotNull(node, "statusToneClass", item.getStatusToneClass());
            putIfNotNull(node, "cardToneClass", item.getCardToneClass());
            putIfNotNull(node, "stopDone", item.getStopDone());
            putIfNotNull(node, "showComplete", item.getShowComplete());
            putIfNotNull(node, "showNav", item.getShowNav());
            putIfNotNull(node, "showException", item.getShowException());
            putIfNotNull(node, "deliveryStopId", item.getDeliveryStopId());
            putIfNotNull(node, "stopId", item.getStopId());
            putIfNotNull(node, "stopKey", item.getStopKey());
            putIfNotNull(node, "addressId", item.getAddressId());
            putIfNotNull(node, "navLat", item.getNavLat());
            putIfNotNull(node, "navLng", item.getNavLng());
            putIfNotNull(node, "navAddress", item.getNavAddress());
            if (item.getPrimaryAction() != null) {
                node.put("primaryAction", toPrimaryActionMap(item.getPrimaryAction()));
            }
            list.add(node);
        }
        return list;
    }

    private static Map<String, Object> toPrimaryActionMap(DispatchPrimaryAction action) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        putIfNotNull(map, "actionType", action.getActionType());
        putIfNotNull(map, "label", action.getLabel());
        map.put("enabled", action.isEnabled());
        putIfNotNull(map, "disabledReason", action.getDisabledReason());
        putIfNotNull(map, "toneClass", action.getToneClass());
        if (action.getPayload() != null && !action.getPayload().isEmpty()) {
            map.put("payload", new LinkedHashMap<String, Object>(action.getPayload()));
        }
        return map;
    }

    private static List<Map<String, Object>> toAvailableDriversList(
            List<DispatchPageViewModel.DispatchAvailableDriver> drivers) {
        if (drivers == null || drivers.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (DispatchPageViewModel.DispatchAvailableDriver driver : drivers) {
            if (driver == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            putIfNotNull(map, "driverUserId", driver.getDriverUserId());
            putIfNotNull(map, "driverName", driver.getDriverName());
            putIfNotNull(map, "driverPhone", driver.getDriverPhone());
            putIfNotNull(map, "dutyStatus", driver.getDutyStatus());
            putIfNotNull(map, "dispatchPhaseLabel", driver.getDispatchPhaseLabel());
            putIfNotNull(map, "statusLabel", driver.getStatusLabel());
            putIfNotNull(map, "badgeLabel", driver.getBadgeLabel());
            map.put("selectable", driver.isSelectable());
            if (driver.getRouteEditAction() != null) {
                map.put("routeEditAction", toPrimaryActionMap(driver.getRouteEditAction()));
            }
            list.add(map);
        }
        return list;
    }

    private static Map<String, Object> toMapOverviewMap(DispatchMapOverview overview) {
        if (overview == null) {
            return defaultMapOverview();
        }
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        putIfNotNull(map, "summaryText", overview.getSummaryText());
        map.put("summary", toSummaryMap(overview.getSummary()));
        map.put("depot", toDepotMap(overview.getDepot()));
        putIfNotNull(map, "centerLat", overview.getCenterLat());
        putIfNotNull(map, "centerLng", overview.getCenterLng());
        putIfNotNull(map, "suggestedScale", overview.getSuggestedScale());
        putIfNotNull(map, "zoomLevel", overview.getZoomLevel());
        map.put("markers", toMarkerList(overview.getMarkers()));
        map.put("polylines", toPolylineList(overview.getPolylines()));
        map.put("legend", toLegendList(overview.getLegend()));
        map.put("missingCoordinateStops", toMissingStopList(overview.getMissingCoordinateStops()));
        if (overview.getEmptyHint() != null) {
            map.put("emptyHint", overview.getEmptyHint());
        } else if ((overview.getMarkers() == null || overview.getMarkers().isEmpty())
                && (overview.getPolylines() == null || overview.getPolylines().isEmpty())
                && overview.getCenterLat() == null) {
            map.put("emptyHint", "暂无地图数据");
        }
        return map;
    }

    private static Map<String, Object> toSummaryMap(DispatchMapOverview.DispatchMapSummary summary) {
        if (summary == null) {
            return defaultSummaryMap();
        }
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        putIfNotNull(map, "customerStopCount", summary.getCustomerStopCount());
        putIfNotNull(map, "routeCount", summary.getRouteCount());
        putIfNotNull(map, "unassignedCount", summary.getUnassignedCount());
        return map;
    }

    private static Map<String, Object> toDepotMap(DispatchMapOverview.DispatchMapDepot depot) {
        if (depot == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        putIfNotNull(map, "lat", depot.getLat());
        putIfNotNull(map, "lng", depot.getLng());
        putIfNotNull(map, "name", depot.getName());
        putIfNotNull(map, "address", depot.getAddress());
        putIfNotNull(map, "colorKey", depot.getColorKey());
        putIfNotNull(map, "color", depot.getColor());
        return map;
    }

    private static List<Map<String, Object>> toMarkerList(List<DispatchMapOverview.DispatchMapMarker> markers) {
        if (markers == null || markers.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (DispatchMapOverview.DispatchMapMarker marker : markers) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            putIfNotNull(map, "markerType", marker.getMarkerType());
            putIfNotNull(map, "markerKey", marker.getMarkerKey());
            putIfNotNull(map, "title", marker.getTitle());
            putIfNotNull(map, "subtitle", marker.getSubtitle());
            putIfNotNull(map, "lat", marker.getLat());
            putIfNotNull(map, "lng", marker.getLng());
            putIfNotNull(map, "toneClass", marker.getToneClass());
            putIfNotNull(map, "colorKey", marker.getColorKey());
            putIfNotNull(map, "color", marker.getColor());
            putIfNotNull(map, "driverRouteId", marker.getDriverRouteId());
            putIfNotNull(map, "driverUserId", marker.getDriverUserId());
            putIfNotNull(map, "driverName", marker.getDriverName());
            putIfNotNull(map, "sandboxStopKey", marker.getSandboxStopKey());
            putIfNotNull(map, "customerName", marker.getCustomerName());
            putIfNotNull(map, "displayTitle", marker.getDisplayTitle());
            putIfNotNull(map, "assignmentLabel", marker.getAssignmentLabel());
            putIfNotNull(map, "badgeText", marker.getBadgeText());
            putIfNotNull(map, "stopSeq", marker.getStopSeq());
            list.add(map);
        }
        return list;
    }

    private static List<Map<String, Object>> toPolylineList(List<DispatchMapOverview.DispatchMapPolyline> polylines) {
        if (polylines == null || polylines.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (DispatchMapOverview.DispatchMapPolyline polyline : polylines) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            putIfNotNull(map, "polylineKey", polyline.getPolylineKey());
            putIfNotNull(map, "routeKey", polyline.getRouteKey());
            putIfNotNull(map, "driverRouteId", polyline.getDriverRouteId());
            putIfNotNull(map, "driverUserId", polyline.getDriverUserId());
            putIfNotNull(map, "driverName", polyline.getDriverName());
            putIfNotNull(map, "toneClass", polyline.getToneClass());
            putIfNotNull(map, "colorKey", polyline.getColorKey());
            putIfNotNull(map, "color", polyline.getColor());
            putIfNotNull(map, "kind", polyline.getKind());
            putIfNotNull(map, "lineStyle", polyline.getLineStyle());
            putIfNotNull(map, "lineType", polyline.getLineType());
            map.put("points", toPointList(polyline.getPoints()));
            list.add(map);
        }
        return list;
    }

    private static List<Map<String, Object>> toPointList(List<DispatchMapOverview.DispatchMapPoint> points) {
        if (points == null || points.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (DispatchMapOverview.DispatchMapPoint point : points) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            putIfNotNull(map, "lat", point.getLat());
            putIfNotNull(map, "lng", point.getLng());
            list.add(map);
        }
        return list;
    }

    private static List<Map<String, Object>> toLegendList(List<DispatchMapOverview.DispatchMapLegendItem> legend) {
        if (legend == null || legend.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (DispatchMapOverview.DispatchMapLegendItem item : legend) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            putIfNotNull(map, "kind", item.getKind());
            putIfNotNull(map, "label", item.getLabel());
            putIfNotNull(map, "toneClass", item.getToneClass());
            putIfNotNull(map, "colorKey", item.getColorKey());
            putIfNotNull(map, "color", item.getColor());
            putIfNotNull(map, "lineStyle", item.getLineStyle());
            putIfNotNull(map, "driverUserId", item.getDriverUserId());
            list.add(map);
        }
        return list;
    }

    private static List<Map<String, Object>> toMissingStopList(
            List<DispatchMapOverview.DispatchMapMissingStop> stops) {
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (DispatchMapOverview.DispatchMapMissingStop stop : stops) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            putIfNotNull(map, "customerName", stop.getCustomerName());
            putIfNotNull(map, "addressId", stop.getAddressId());
            putIfNotNull(map, "sandboxStopKey", stop.getSandboxStopKey());
            map.put("unassigned", stop.isUnassigned());
            list.add(map);
        }
        return list;
    }

    public static DispatchPageMode parsePageMode(String pageMode) {
        if (pageMode == null || pageMode.trim().isEmpty()) {
            return null;
        }
        try {
            return DispatchPageMode.valueOf(pageMode.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
