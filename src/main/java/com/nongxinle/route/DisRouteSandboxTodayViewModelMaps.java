package com.nongxinle.route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** pageViewModel Map 正式契约裁剪（DispatchPageAssembler 输出后 contract）。 */
public final class DisRouteSandboxTodayViewModelMaps {

    private static final String MISSING_DRIVER_NAME = "司机资料缺失";
    private static final String MISSING_DRIVER_NAME_REASON = "未找到司机账号昵称，请检查司机资料";

    private DisRouteSandboxTodayViewModelMaps() {
    }

    /** 正式今日派单页契约：只保留页面展示与动作所需字段。 */
    public static Map<String, Object> toDispatchPageMap(Map<String, Object> fullPageViewModel) {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        if (fullPageViewModel == null || fullPageViewModel.isEmpty()) {
            return root;
        }
        putIfNotNull(root, "topMetricsScope", fullPageViewModel.get("topMetricsScope"));
        root.put("pageHeader", contractHeaderMap(castMap(fullPageViewModel.get("pageHeader"))));
        root.put("topMetrics", contractTopMetricsMap(castMap(fullPageViewModel.get("topMetrics"))));
        root.put("sections", contractSectionsList(castList(fullPageViewModel.get("sections"))));
        root.put("availableDrivers", contractAvailableDriversList(
                castList(fullPageViewModel.get("availableDrivers"))));
        root.put("mapOverview", ensureMapOverviewContract(
                contractMapOverviewMap(castMap(fullPageViewModel.get("mapOverview")))));
        return root;
    }

    /** 今日派单正式契约缺省 mapOverview（字段始终齐全）。 */
    public static Map<String, Object> defaultDispatchMapOverview() {
        return defaultMapOverviewContractMap();
    }

    /** 正式装车页契约：与今日派单页相同字段结构（含 mapOverview），sections 仅含装车路线。 */
    public static Map<String, Object> toLoadingPageMap(Map<String, Object> fullPageViewModel) {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        if (fullPageViewModel == null || fullPageViewModel.isEmpty()) {
            return root;
        }
        putIfNotNull(root, "topMetricsScope", fullPageViewModel.get("topMetricsScope"));
        root.put("pageHeader", contractHeaderMap(castMap(fullPageViewModel.get("pageHeader"))));
        root.put("topMetrics", contractTopMetricsMap(castMap(fullPageViewModel.get("topMetrics"))));
        root.put("sections", contractSectionsList(castList(fullPageViewModel.get("sections"))));
        root.put("availableDrivers", contractAvailableDriversList(
                castList(fullPageViewModel.get("availableDrivers"))));
        root.put("mapOverview", ensureMapOverviewContract(
                contractMapOverviewMap(castMap(fullPageViewModel.get("mapOverview")))));
        return root;
    }

    /** 正式配送任务页契约：sections 含 EXECUTION_DRIVER_ROUTES + 统一 driverCard。 */
    public static Map<String, Object> toDeliveryPageMap(Map<String, Object> fullPageViewModel) {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        if (fullPageViewModel == null || fullPageViewModel.isEmpty()) {
            return root;
        }
        putIfNotNull(root, "topMetricsScope", fullPageViewModel.get("topMetricsScope"));
        root.put("pageHeader", contractDeliveryHeaderMap(castMap(fullPageViewModel.get("pageHeader"))));
        root.put("topMetrics", contractTopMetricsMap(castMap(fullPageViewModel.get("topMetrics"))));
        root.put("sections", contractDeliverySectionsList(castList(fullPageViewModel.get("sections"))));
        root.put("availableDrivers", Collections.emptyList());
        root.put("mapOverview", ensureMapOverviewContract(
                contractMapOverviewMap(castMap(fullPageViewModel.get("mapOverview")))));
        return root;
    }

    private static Map<String, Object> contractDeliveryHeaderMap(Map<String, Object> header) {
        Map<String, Object> map = contractHeaderMap(header);
        if (header != null) {
            copyIfPresent(header, map, "subtitle");
        }
        return map;
    }

    private static List<Map<String, Object>> contractDeliverySectionsList(List<Map<String, Object>> sections) {
        if (sections == null || sections.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> section : sections) {
            if (section == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            copyIfPresent(section, item, "sectionKey", "title", "description");
            item.put("cards", contractDeliveryCardsList(castList(section.get("cards"))));
            list.add(item);
        }
        return list;
    }

    private static List<Map<String, Object>> contractDeliveryCardsList(List<Map<String, Object>> cards) {
        if (cards == null || cards.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> card : cards) {
            if (card == null) {
                continue;
            }
            if ("DRIVER_ROUTE".equals(card.get("cardType"))) {
                list.add(contractDeliveryDriverRouteCardMap(card));
            } else {
                list.add(card);
            }
        }
        return list;
    }

    private static Map<String, Object> contractDeliveryDriverRouteCardMap(Map<String, Object> card) {
        Map<String, Object> map = contractDriverRouteCardMap(card);
        copyIfPresent(card, map,
                "dispatchStage", "dispatchStageLabel",
                "plannedDepartureLabel", "estimatedReturnLabel",
                "totalDistanceText", "totalDurationText",
                "customerCount", "completedStopCount", "pendingStopCount",
                "driverNameResolveStatus", "driverNameMissingReason",
                "plannedDepartLabel", "plannedReturnLabel",
                "firstStopPlannedArrivalTimeLabel", "firstStopArrivalStatusLabel", "firstStopArrivalStatusTone",
                "totalRoundTripDistanceText", "totalRoundTripDurationText",
                "deliveryProgressLine", "actualDepartStatusLabel", "actualDepartStatusTone",
                "deliveryProgressPercent");
        if (card.get("driverCard") != null) {
            map.put("driverCard", card.get("driverCard"));
        }
        if (card.get("routeViewAction") != null) {
            map.put("routeViewAction", card.get("routeViewAction"));
        }
        if (!map.containsKey("driverName") || map.get("driverName") == null
                || String.valueOf(map.get("driverName")).trim().isEmpty()) {
            map.put("driverName", MISSING_DRIVER_NAME);
            map.put("driverNameResolveStatus", "MISSING");
            map.put("driverNameMissingReason", MISSING_DRIVER_NAME_REASON);
        }
        map.put("timeline", contractDeliveryTimelineList(castList(card.get("timeline"))));
        return map;
    }

    private static List<Map<String, Object>> contractDeliveryTimelineList(List<Map<String, Object>> timeline) {
        if (timeline == null || timeline.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> node : timeline) {
            if (node == null) {
                continue;
            }
            String type = node.get("type") != null ? String.valueOf(node.get("type")) : null;
            if ("start".equals(type)) {
                continue;
            }
            if ("leg".equals(type) && "RETURN".equals(String.valueOf(node.get("legRole")))) {
                continue;
            }
            List<Map<String, Object>> contracted = contractTimelineList(Collections.singletonList(node));
            if (contracted.isEmpty()) {
                continue;
            }
            Map<String, Object> item = contracted.get(0);
            if ("stop".equals(type)) {
                copyIfPresent(node, item,
                        "customerTimeWindow", "systemEta", "manualTimeConstraint",
                        "distanceText", "durationText",
                        "deliveryProgressState", "stopDone", "cardToneClass",
                        "arrivalFieldLabel", "departureFieldLabel",
                        "showCancelDelivery", "showMarkException",
                        "windowRequirementLabel", "windowRequirementModified", "pointStatusLabel");
                if (node.get("exceptionAction") != null) {
                    item.put("exceptionAction", node.get("exceptionAction"));
                }
                if (!item.containsKey("customerTimeWindow") && node.get("windowLabel") != null) {
                    item.put("customerTimeWindow", node.get("windowLabel"));
                }
            }
            list.add(item);
        }
        return list;
    }

    private static Map<String, Object> contractHeaderMap(Map<String, Object> header) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (header == null) {
            return map;
        }
        copyIfPresent(header, map,
                "title", "operationHint", "statusLabel", "statusTone",
                "scheduleBannerLine", "heroImageType");
        Map<String, Object> progress = contractProgressMap(castMap(header.get("progress")));
        map.put("progress", progress);
        return map;
    }

    private static Map<String, Object> contractProgressMap(Map<String, Object> progress) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (progress != null) {
            copyIfPresent(progress, map, "mainLine", "highlightText");
        }
        if (!map.containsKey("mainLine")) {
            map.put("mainLine", "暂无待确认站点");
        }
        return map;
    }

    private static Map<String, Object> contractTopMetricsMap(Map<String, Object> metrics) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (metrics == null) {
            return map;
        }
        copyIfPresent(metrics, map,
                "driverCount", "customerStopCount", "totalDistanceText", "totalDurationText");
        return map;
    }

    private static List<Map<String, Object>> contractSectionsList(List<Map<String, Object>> sections) {
        if (sections == null || sections.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> section : sections) {
            if (section == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            copyIfPresent(section, item, "sectionKey", "title", "description");
            item.put("cards", contractCardsList(castList(section.get("cards"))));
            list.add(item);
        }
        return list;
    }

    private static List<Map<String, Object>> contractCardsList(List<Map<String, Object>> cards) {
        if (cards == null || cards.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> card : cards) {
            if (card == null) {
                continue;
            }
            if ("DRIVER_ROUTE".equals(card.get("cardType"))) {
                list.add(contractDriverRouteCardMap(card));
            } else if ("UNASSIGNED_CUSTOMER".equals(card.get("cardType"))) {
                list.add(contractUnassignedCustomerCardMap(card));
            } else {
                list.add(contractCustomerCardMap(card));
            }
        }
        return list;
    }

    private static Map<String, Object> contractDriverRouteCardMap(Map<String, Object> card) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        copyIfPresent(card, map,
                "cardType", "driverUserId", "driverName",
                "driverStatusLabel", "driverStatusTone", "badgeLabel",
                "scheduleHeadline", "routeSummary", "routeStatsLine",
                "customerStopCount",
                "recommendedDepartLabel", "routeSuggestedDepartLabel",
                "firstStopPlannedArrivalLabel", "firstStopPlannedArrivalTimeLabel",
                "firstStopArrivalStatusLabel", "firstStopArrivalStatusTone",
                "routeHeadlineLine", "routeRoundTripSummaryLine",
                "firstStopWindowLabel",
                "firstStopWindowStartS", "firstStopWindowEndS",
                "depotName", "depotAddress",
                "outboundDistanceText", "returnDistanceText", "totalRoundTripDistanceText",
                "plannedDepartLabel", "plannedReturnLabel", "totalRoundTripDurationText",
                "totalDistanceText", "totalDurationText");
        putIfNonBlank(map, "driverAvatarUrl", card.get("driverAvatarUrl"));
        map.put("timeline", contractTimelineList(castList(card.get("timeline"))));
        if (card.get("primaryAction") != null) {
            map.put("primaryAction", card.get("primaryAction"));
        }
        if (card.get("routeEditAction") != null) {
            map.put("routeEditAction", card.get("routeEditAction"));
        }
        return map;
    }

    private static Map<String, Object> contractCustomerCardMap(Map<String, Object> card) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        copyIfPresent(card, map,
                "cardType", "cardKey", "customerName", "goodsSummary",
                "driverLabel", "statusLabel", "badgeLabel");
        if (card.get("primaryAction") != null) {
            map.put("primaryAction", card.get("primaryAction"));
        }
        return map;
    }

    /** 待分配客户卡：与 timeline stop 同字段 + section 头信息。 */
    private static Map<String, Object> contractUnassignedCustomerCardMap(Map<String, Object> card) {
        Map<String, Object> map = contractStoreStopMap(card);
        copyIfPresent(card, map, "cardType", "badgeLabel", "driverLabel", "unassignedDriverHint");
        if (card.get("primaryAction") != null) {
            map.put("primaryAction", card.get("primaryAction"));
        }
        return map;
    }

    /** 门店卡统一字段（timeline stop / 未分配 / storeCard 共用）。 */
    private static Map<String, Object> contractStoreStopMap(Map<String, Object> card) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        copyIfPresent(card, map,
                "cardKey", "customerName", "customerShortName",
                "departmentId", "depFatherId", "depId", "customerId", "sandboxStopKey",
                "goodsSummary", "distanceText", "durationText", "legText", "legDistanceLabel",
                "plannedArrivalLabel", "plannedDepartureLabel",
                "arrivalLabel", "departureLabel",
                "customerWindowLabel", "windowLabel", "deliveryWindowLabel",
                "windowRequirementLabel", "windowRequirementModified",
                "windowStatusLabel", "windowTone",
                "serviceDurationLabel", "unloadDurationText",
                "timeLabel", "nextLegHint",
                "arrivalStatusLabel", "arrivalStatusTone", "arrivalStatusType",
                "statusLabel", "taskId", "deliveryStopId");
        return map;
    }

    private static List<Map<String, Object>> contractTimelineList(List<Map<String, Object>> timeline) {
        if (timeline == null || timeline.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> node : timeline) {
            if (node == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            String type = node.get("type") != null ? String.valueOf(node.get("type")) : null;
            item.put("type", type);
            if ("start".equals(type)) {
                continue;
            }
            if ("leg".equals(type) && "RETURN".equals(String.valueOf(node.get("legRole")))) {
                continue;
            }
            if ("start".equals(type) || "end".equals(type)) {
                copyIfPresent(node, item, "marker", "name", "timeRight", "depotName", "depotAddress",
                        "legText", "pointStatusLabel");
            } else if ("leg".equals(type)) {
                copyIfPresent(node, item, "legRole", "legText", "isNextStopLeg");
            } else if ("stop".equals(type)) {
                copyIfPresent(node, item,
                        "seq", "cardKey", "name", "customerName",
                        "arrivalLabel", "departureLabel", "plannedArrivalLabel", "plannedDepartureLabel",
                        "windowLabel", "deliveryWindowLabel", "windowRequirementLabel", "windowRequirementModified",
                        "windowStatusLabel",
                        "arrivalStatusLabel", "arrivalStatusTone",
                        "serviceDurationLabel", "timeLabel",
                        "goodsSummary", "statusLabel",
                        "taskId", "deliveryStopId", "nextLegHint", "windowTone",
                        "legText", "legDistanceLabel", "distanceText", "durationText");
                if (node.get("primaryAction") != null) {
                    item.put("primaryAction", node.get("primaryAction"));
                }
                if (node.get("viewOrderDetail") != null) {
                    item.put("viewOrderDetail", node.get("viewOrderDetail"));
                }
            } else {
                copyIfPresent(node, item,
                        "marker", "name", "timeRight", "legRole", "legText", "seq", "cardKey", "customerName",
                        "arrivalLabel", "departureLabel", "windowLabel",
                        "serviceDurationLabel", "timeLabel", "goodsSummary", "statusLabel");
                if (node.get("primaryAction") != null) {
                    item.put("primaryAction", node.get("primaryAction"));
                }
            }
            list.add(item);
        }
        return list;
    }

    private static List<Map<String, Object>> contractAvailableDriversList(List<Map<String, Object>> drivers) {
        if (drivers == null || drivers.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> driver : drivers) {
            if (driver == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            copyIfPresent(driver, map, "driverUserId", "driverName", "statusLabel");
            putIfNonBlank(map, "driverAvatarUrl", driver.get("driverAvatarUrl"));
            if (driver.get("routeEditAction") != null) {
                map.put("routeEditAction", driver.get("routeEditAction"));
            }
            list.add(map);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castList(Object value) {
        if (value instanceof List) {
            return (List<Map<String, Object>>) value;
        }
        return null;
    }

    private static void copyIfPresent(Map<String, Object> from, Map<String, Object> to, String... keys) {
        if (from == null || to == null || keys == null) {
            return;
        }
        for (String key : keys) {
            putIfNotNull(to, key, from.get(key));
        }
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private static void putIfNonBlank(Map<String, Object> map, String key, Object value) {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value).trim();
        if (!text.isEmpty()) {
            map.put(key, text);
        }
    }

    private static Map<String, Object> contractMapOverviewMap(Map<String, Object> overview) {
        if (overview == null || overview.isEmpty()) {
            return defaultMapOverviewContractMap();
        }
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("summary", contractSummaryMap(castMap(overview.get("summary"))));
        Map<String, Object> depot = castMap(overview.get("depot"));
        map.put("depot", depot != null ? depot : Collections.emptyMap());
        List<Map<String, Object>> markers = castList(overview.get("markers"));
        map.put("markers", markers != null ? markers : Collections.emptyList());
        List<Map<String, Object>> polylines = castList(overview.get("polylines"));
        map.put("polylines", polylines != null ? polylines : Collections.emptyList());
        List<Map<String, Object>> legend = castList(overview.get("legend"));
        map.put("legend", legend != null ? legend : Collections.emptyList());
        List<Map<String, Object>> missing = castList(overview.get("missingCoordinateStops"));
        map.put("missingCoordinateStops", missing != null ? missing : Collections.emptyList());
        putIfNotNull(map, "centerLat", overview.get("centerLat"));
        putIfNotNull(map, "centerLng", overview.get("centerLng"));
        putIfNotNull(map, "suggestedScale", overview.get("suggestedScale"));
        putIfNotNull(map, "subkey", overview.get("subkey"));
        putIfNotNull(map, "layerStyle", overview.get("layerStyle"));
        putIfNotNull(map, "emptyHint", overview.get("emptyHint"));
        return ensureMapOverviewContract(map);
    }

    /** 正式契约：mapOverview 字段始终存在且含固定子键，避免 FastJSON 空对象被省略。 */
    private static Map<String, Object> ensureMapOverviewContract(Map<String, Object> map) {
        Map<String, Object> contract = map != null ? new LinkedHashMap<String, Object>(map) : new LinkedHashMap<String, Object>();
        if (!contract.containsKey("summary")) {
            contract.put("summary", defaultSummaryMap());
        } else if (contract.get("summary") == null) {
            contract.put("summary", defaultSummaryMap());
        }
        if (!contract.containsKey("depot")) {
            contract.put("depot", Collections.emptyMap());
        }
        if (!contract.containsKey("markers")) {
            contract.put("markers", Collections.emptyList());
        }
        if (!contract.containsKey("polylines")) {
            contract.put("polylines", Collections.emptyList());
        }
        if (!contract.containsKey("legend")) {
            contract.put("legend", Collections.emptyList());
        }
        if (!contract.containsKey("missingCoordinateStops")) {
            contract.put("missingCoordinateStops", Collections.emptyList());
        }
        if (hasDisplayableMapContent(contract)) {
            contract.remove("emptyHint");
        } else if (!contract.containsKey("emptyHint") || contract.get("emptyHint") == null
                || String.valueOf(contract.get("emptyHint")).trim().isEmpty()) {
            contract.put("emptyHint", "暂无地图数据");
        }
        return contract;
    }

    private static boolean hasDisplayableMapContent(Map<String, Object> contract) {
        List<Map<String, Object>> markers = castList(contract.get("markers"));
        if (markers != null && !markers.isEmpty()) {
            return true;
        }
        List<Map<String, Object>> polylines = castList(contract.get("polylines"));
        return polylines != null && !polylines.isEmpty();
    }

    private static Map<String, Object> defaultMapOverviewContractMap() {
        return ensureMapOverviewContract(new LinkedHashMap<String, Object>());
    }

    private static Map<String, Object> contractSummaryMap(Map<String, Object> summary) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (summary == null || summary.isEmpty()) {
            return defaultSummaryMap();
        }
        putIfNotNull(map, "customerStopCount", summary.get("customerStopCount"));
        putIfNotNull(map, "routeCount", summary.get("routeCount"));
        putIfNotNull(map, "unassignedCount", summary.get("unassignedCount"));
        return map;
    }

    private static Map<String, Object> defaultSummaryMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("customerStopCount", 0);
        map.put("routeCount", 0);
        map.put("unassignedCount", 0);
        return map;
    }
}
