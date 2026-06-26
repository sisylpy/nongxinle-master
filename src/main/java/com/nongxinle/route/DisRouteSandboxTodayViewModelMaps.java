package com.nongxinle.route;

import com.nongxinle.dto.route.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 将 pageViewModel 转为显式 Map，避免 FastJSON NotWriteDefaultValue 丢 nested 字段。 */
public final class DisRouteSandboxTodayViewModelMaps {

    private DisRouteSandboxTodayViewModelMaps() {
    }

    public static Map<String, Object> toMap(SandboxTodayPageViewModel viewModel) {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        if (viewModel == null) {
            return root;
        }
        root.put("topMetricsScope", viewModel.getTopMetricsScope());
        root.put("pageHeader", toHeaderMap(viewModel.getPageHeader()));
        root.put("topMetrics", toTopMetricsMap(viewModel.getTopMetrics()));
        root.put("sections", toSectionsMap(viewModel.getSections()));
        root.put("availableDrivers", toAvailableDriversMap(viewModel.getAvailableDrivers()));
        root.put("mapOverview", ensureMapOverviewContract(toMapOverviewMap(viewModel.getMapOverview())));
        return root;
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

    public static Map<String, Object> toDispatchPageMap(SandboxTodayPageViewModel viewModel) {
        return toDispatchPageMap(toMap(viewModel));
    }

    /** 今日派单正式契约缺省 mapOverview（字段始终齐全）。 */
    public static Map<String, Object> defaultDispatchMapOverview() {
        return defaultMapOverviewContractMap();
    }

    /** 正式装车页契约：与今日派单页相同字段结构（不含 mapOverview），sections 仅含装车路线。 */
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
        return root;
    }

    public static Map<String, Object> toLoadingPageMap(SandboxTodayPageViewModel viewModel) {
        return toLoadingPageMap(toMap(viewModel));
    }

    /** 司机终端装车/配送页：保留 timeline 上的距离与司机动作字段。 */
    public static Map<String, Object> toDriverTerminalPageMap(Map<String, Object> fullPageViewModel) {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        if (fullPageViewModel == null || fullPageViewModel.isEmpty()) {
            return root;
        }
        putIfNotNull(root, "topMetricsScope", fullPageViewModel.get("topMetricsScope"));
        root.put("pageHeader", contractHeaderMap(castMap(fullPageViewModel.get("pageHeader"))));
        root.put("topMetrics", contractTopMetricsMap(castMap(fullPageViewModel.get("topMetrics"))));
        root.put("sections", contractDriverTerminalSectionsList(castList(fullPageViewModel.get("sections"))));
        root.put("availableDrivers", contractAvailableDriversList(
                castList(fullPageViewModel.get("availableDrivers"))));
        return root;
    }

    public static Map<String, Object> toDriverTerminalPageMap(SandboxTodayPageViewModel viewModel) {
        return toDriverTerminalPageMap(toMap(viewModel));
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
        return root;
    }

    public static Map<String, Object> toDeliveryPageMap(SandboxTodayPageViewModel viewModel) {
        return toDeliveryPageMap(toMap(viewModel));
    }

    public static Map<String, Object> filterDeliveryPageViewModelByDriver(Map<String, Object> pageViewModel,
                                                                          Integer driverUserId) {
        if (pageViewModel == null || driverUserId == null) {
            return pageViewModel;
        }
        Map<String, Object> filtered = new LinkedHashMap<String, Object>(pageViewModel);
        List<Map<String, Object>> sections = contractDeliverySectionsList(castList(pageViewModel.get("sections")));
        List<Map<String, Object>> filteredSections = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> section : sections) {
            if (section == null) {
                continue;
            }
            List<Map<String, Object>> cards = castList(section.get("cards"));
            List<Map<String, Object>> filteredCards = new ArrayList<Map<String, Object>>();
            if (cards != null) {
                for (Map<String, Object> card : cards) {
                    if (card == null) {
                        continue;
                    }
                    Object cardDriverId = card.get("driverUserId");
                    if (cardDriverId != null && driverUserId.equals(Integer.valueOf(String.valueOf(cardDriverId)))) {
                        filteredCards.add(card);
                    }
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
                "driverNameResolveStatus", "driverNameMissingReason");
        if (card.get("driverCard") != null) {
            map.put("driverCard", card.get("driverCard"));
        }
        if (!map.containsKey("driverName") || map.get("driverName") == null
                || String.valueOf(map.get("driverName")).trim().isEmpty()) {
            map.put("driverName", DisRouteDispatchDriverNameHelper.MISSING_DRIVER_NAME);
            map.put("driverNameResolveStatus", "MISSING");
            map.put("driverNameMissingReason", DisRouteDispatchDriverNameHelper.MISSING_REASON);
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
            Map<String, Object> item = contractTimelineList(Collections.singletonList(node)).get(0);
            String type = node.get("type") != null ? String.valueOf(node.get("type")) : null;
            if ("stop".equals(type)) {
                copyIfPresent(node, item,
                        "customerTimeWindow", "systemEta", "manualTimeConstraint",
                        "distanceText", "durationText");
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

    private static List<Map<String, Object>> contractDriverTerminalSectionsList(List<Map<String, Object>> sections) {
        if (sections == null || sections.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> section : sections) {
            if (section == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            copyIfPresent(section, map, "sectionKey", "title", "description");
            List<Map<String, Object>> cards = castList(section.get("cards"));
            if (cards != null && !cards.isEmpty()) {
                List<Map<String, Object>> cardList = new ArrayList<Map<String, Object>>();
                for (Map<String, Object> card : cards) {
                    if (card == null) {
                        continue;
                    }
                    cardList.add(contractDriverTerminalDriverRouteCardMap(card));
                }
                map.put("cards", cardList);
            } else {
                map.put("cards", Collections.emptyList());
            }
            list.add(map);
        }
        return list;
    }

    private static Map<String, Object> contractDriverTerminalDriverRouteCardMap(Map<String, Object> card) {
        Map<String, Object> map = contractDriverRouteCardMap(card);
        map.put("timeline", contractDriverTerminalTimelineList(castList(card.get("timeline"))));
        return map;
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

    /** 待分配客户卡：保留市场→客户 leg 与排程展示字段。 */
    private static Map<String, Object> contractUnassignedCustomerCardMap(Map<String, Object> card) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        copyIfPresent(card, map,
                "cardType", "customerName", "goodsSummary",
                "driverLabel", "statusLabel", "badgeLabel",
                "customerWindowLabel", "plannedArrivalLabel", "plannedDepartureLabel",
                "serviceDurationLabel", "distanceText", "durationText", "timeLabel");
        if (card.get("primaryAction") != null) {
            map.put("primaryAction", card.get("primaryAction"));
        }
        return map;
    }

    private static List<Map<String, Object>> contractDriverTerminalTimelineList(List<Map<String, Object>> timeline) {
        if (timeline == null || timeline.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> node : timeline) {
            if (node == null) {
                continue;
            }
            Map<String, Object> item = contractTimelineList(Collections.singletonList(node)).get(0);
            String type = node.get("type") != null ? String.valueOf(node.get("type")) : null;
            if ("leg".equals(type)) {
                copyIfPresent(node, item, "isNextStopLeg");
            } else if ("stop".equals(type)) {
                copyIfPresent(node, item,
                        "legDistanceLabel", "distanceText", "durationText", "nextLegHint", "nextLegHintLabel",
                        "distanceLabel", "arrivalFieldLabel", "departureFieldLabel", "cardToneClass",
                        "deliveryProgressState",
                        "statusToneClass", "showComplete", "showException", "showNav",
                        "navLat", "navLng", "navAddress");
                if (node.get("viewOrderDetail") != null) {
                    item.put("viewOrderDetail", node.get("viewOrderDetail"));
                }
            }
            list.add(item);
        }
        return list;
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
            if ("start".equals(type) || "end".equals(type)) {
                copyIfPresent(node, item, "marker", "name", "timeRight", "depotName", "depotAddress");
            } else if ("leg".equals(type)) {
                copyIfPresent(node, item, "legRole", "legText", "isNextStopLeg");
            } else if ("stop".equals(type)) {
                copyIfPresent(node, item,
                        "seq", "cardKey", "name", "customerName",
                        "arrivalLabel", "departureLabel", "plannedArrivalLabel", "plannedDepartureLabel",
                        "windowLabel", "deliveryWindowLabel", "windowRequirementLabel", "windowStatusLabel",
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

    private static Map<String, Object> toHeaderMap(SandboxTodayPageHeaderDto header) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (header == null) {
            return map;
        }
        putIfNotNull(map, "title", header.getTitle());
        putIfNotNull(map, "subtitle", header.getSubtitle());
        putIfNotNull(map, "progressLine", header.getProgressLine());
        map.put("progress", toProgressMap(header.getProgress()));
        putIfNotNull(map, "operationHint", header.getOperationHint());
        putIfNotNull(map, "statusLabel", header.getStatusLabel());
        putIfNotNull(map, "statusTone", header.getStatusTone());
        putIfNotNull(map, "scheduleBannerLine", header.getScheduleBannerLine());
        map.put("heroImageType", header.getHeroImageType() != null
                ? header.getHeroImageType() : DisRouteSandboxTodayPageViewModelBuilder.HERO_IMAGE_TRUCK);
        map.put("nextActions", toNextActionsMap(header.getNextActions()));
        return map;
    }

    private static List<Map<String, Object>> toNextActionsMap(List<SandboxTodayPageActionDto> actions) {
        if (actions == null || actions.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (SandboxTodayPageActionDto action : actions) {
            if (action == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            putIfNotNull(item, "actionType", action.getActionType());
            putIfNotNull(item, "label", action.getLabel());
            item.put("enabled", action.getEnabled());
            putIfNotNull(item, "disabledReason", action.getDisabledReason());
            item.put("payload", action.getPayload() != null ? action.getPayload() : Collections.emptyMap());
            list.add(item);
        }
        return list;
    }

    private static Map<String, Object> toProgressMap(SandboxTodayPageProgressDto progress) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (progress == null) {
            map.put("mainLine", "暂无待确认站点");
            return map;
        }
        putIfNotNull(map, "mainLine", progress.getMainLine());
        putIfNotNull(map, "highlightText", progress.getHighlightText());
        if (!map.containsKey("mainLine")) {
            map.put("mainLine", "暂无待确认站点");
        }
        return map;
    }

    private static Map<String, Object> toTopMetricsMap(SandboxTodayTopMetricsDto metrics) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (metrics == null) {
            return map;
        }
        map.put("driverCount", metrics.getDriverCount());
        map.put("availableDriverCount", metrics.getAvailableDriverCount());
        map.put("customerStopCount", metrics.getCustomerStopCount());
        map.put("totalDistanceM", metrics.getTotalDistanceM());
        map.put("totalDurationS", metrics.getTotalDurationS());
        putIfNotNull(map, "totalDistanceText", metrics.getTotalDistanceText());
        putIfNotNull(map, "totalDurationText", metrics.getTotalDurationText());
        return map;
    }

    private static List<Map<String, Object>> toSectionsMap(List<SandboxTodaySectionDto> sections) {
        if (sections == null || sections.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (SandboxTodaySectionDto section : sections) {
            if (section == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            putIfNotNull(item, "sectionKey", section.getSectionKey());
            putIfNotNull(item, "title", section.getTitle());
            putIfNotNull(item, "description", section.getDescription());
            item.put("cards", toCardsMap(section.getCards()));
            list.add(item);
        }
        return list;
    }

    private static List<Map<String, Object>> toCardsMap(List<SandboxTodaySectionCardDto> cards) {
        if (cards == null || cards.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (SandboxTodaySectionCardDto card : cards) {
            if (card == null) {
                continue;
            }
            if ("DRIVER_ROUTE".equals(card.getCardType())) {
                list.add(toDriverRouteCardMap(card));
            } else if ("UNASSIGNED_CUSTOMER".equals(card.getCardType())) {
                list.add(toUnassignedCustomerCardMap(card));
            } else {
                list.add(toCustomerCardMap(card));
            }
        }
        return list;
    }

    private static Map<String, Object> toDriverRouteCardMap(SandboxTodaySectionCardDto card) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("cardType", "DRIVER_ROUTE");
        map.put("driverUserId", card.getDriverUserId());
        putIfNotNull(map, "driverName", card.getDriverName());
        putIfNonBlank(map, "driverAvatarUrl", card.getDriverAvatarUrl());
        putIfNotNull(map, "driverStatusLabel", card.getDriverStatusLabel());
        putIfNotNull(map, "driverStatusTone", card.getDriverStatusTone());
        putIfNotNull(map, "badgeLabel", card.getBadgeLabel());
        putIfNotNull(map, "scheduleHeadline", card.getScheduleHeadline());
        putIfNotNull(map, "routeSummary", card.getRouteSummary());
        putIfNotNull(map, "routeStatsLine", card.getRouteStatsLine());
        map.put("customerStopCount", card.getCustomerStopCount());
        putIfNotNull(map, "recommendedDepartLabel", card.getRecommendedDepartLabel());
        putIfNotNull(map, "routeSuggestedDepartLabel", card.getRouteSuggestedDepartLabel());
        putIfNotNull(map, "firstStopPlannedArrivalLabel", card.getFirstStopPlannedArrivalLabel());
        putIfNotNull(map, "firstStopPlannedArrivalTimeLabel", card.getFirstStopPlannedArrivalTimeLabel());
        putIfNotNull(map, "firstStopArrivalStatusLabel", card.getFirstStopArrivalStatusLabel());
        putIfNotNull(map, "firstStopArrivalStatusTone", card.getFirstStopArrivalStatusTone());
        putIfNotNull(map, "routeHeadlineLine", card.getRouteHeadlineLine());
        putIfNotNull(map, "routeRoundTripSummaryLine", card.getRouteRoundTripSummaryLine());
        putIfNotNull(map, "firstStopWindowLabel", card.getFirstStopWindowLabel());
        putIfNotNull(map, "firstStopWindowStartS", card.getFirstStopWindowStartS());
        putIfNotNull(map, "firstStopWindowEndS", card.getFirstStopWindowEndS());
        putIfNotNull(map, "depotName", card.getDepotName());
        putIfNotNull(map, "depotAddress", card.getDepotAddress());
        putIfNotNull(map, "outboundDistanceText", card.getOutboundDistanceText());
        putIfNotNull(map, "returnDistanceText", card.getReturnDistanceText());
        putIfNotNull(map, "totalRoundTripDistanceText", card.getTotalRoundTripDistanceText());
        putIfNotNull(map, "plannedDepartLabel", card.getPlannedDepartLabel());
        putIfNotNull(map, "plannedReturnLabel", card.getPlannedReturnLabel());
        putIfNotNull(map, "totalRoundTripDurationText", card.getTotalRoundTripDurationText());
        map.put("totalDistanceM", card.getTotalDistanceM());
        map.put("totalDurationS", card.getTotalDurationS());
        putIfNotNull(map, "totalDistanceText", card.getTotalDistanceText());
        putIfNotNull(map, "totalDurationText", card.getTotalDurationText());
        putIfNotNull(map, "dispatchStage", card.getDispatchStage());
        putIfNotNull(map, "dispatchStageLabel", card.getDispatchStageLabel());
        putIfNotNull(map, "plannedDepartureLabel", card.getPlannedDepartureLabel());
        putIfNotNull(map, "estimatedReturnLabel", card.getEstimatedReturnLabel());
        if (card.getCustomerCount() != null) {
            map.put("customerCount", card.getCustomerCount());
        }
        if (card.getCompletedStopCount() != null) {
            map.put("completedStopCount", card.getCompletedStopCount());
        }
        if (card.getPendingStopCount() != null) {
            map.put("pendingStopCount", card.getPendingStopCount());
        }
        putIfNotNull(map, "driverNameResolveStatus", card.getDriverNameResolveStatus());
        putIfNotNull(map, "driverNameMissingReason", card.getDriverNameMissingReason());
        if (card.getDriverCard() != null) {
            map.put("driverCard", RouteDispatchReadModelAssembler.toDispatchDriverCardMap(card.getDriverCard()));
        }
        List<Map<String, Object>> stopCards = toStopCardsMap(card.getStopCards());
        map.put("stopCards", stopCards);
        List<Map<String, Object>> timeline = card.getTimeline();
        map.put("timeline", timeline != null && !timeline.isEmpty()
                ? timeline
                : DisRouteSandboxTodayTimelineBuilder.buildFromStopCardMaps(stopCards, card, null));
        if (card.getPrimaryAction() != null) {
            map.put("primaryAction", card.getPrimaryAction());
        }
        if (card.getRouteEditAction() != null) {
            map.put("routeEditAction", card.getRouteEditAction());
        }
        return map;
    }

    /** 待分配客户卡：仅调度展示字段，不含 items / 根层业务 ID。 */
    private static Map<String, Object> toUnassignedCustomerCardMap(SandboxTodaySectionCardDto card) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("cardType", card.getCardType());
        putIfNotNull(map, "customerName", card.getCustomerName());
        putIfNotNull(map, "goodsSummary", card.getGoodsSummary());
        putIfNotNull(map, "driverLabel", card.getDriverLabel());
        putIfNotNull(map, "statusLabel", card.getStatusLabel());
        putIfNotNull(map, "badgeLabel", card.getBadgeLabel());
        putIfNotNull(map, "customerWindowLabel", card.getCustomerWindowLabel());
        putIfNotNull(map, "plannedArrivalLabel", card.getPlannedArrivalLabel());
        putIfNotNull(map, "plannedDepartureLabel", card.getPlannedDepartureLabel());
        putIfNotNull(map, "serviceDurationLabel", card.getServiceDurationLabel());
        putIfNotNull(map, "distanceText", card.getDistanceText());
        putIfNotNull(map, "durationText", card.getDurationText());
        putIfNotNull(map, "timeLabel", card.getTimeLabel());
        Map<String, Object> action = ManualDispatchPrimaryActionMaps.toPageActionMap(card.getPrimaryAction());
        if (action != null && !action.isEmpty()) {
            map.put("primaryAction", action);
        }
        return map;
    }

    private static Map<String, Object> toCustomerCardMap(SandboxTodaySectionCardDto card) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("cardType", card.getCardType());
        putIfNotNull(map, "cardKey", card.getCardKey());
        putIfNotNull(map, "customerName", card.getCustomerName());
        map.put("departmentId", card.getDepartmentId());
        putIfNotNull(map, "goodsSummary", card.getGoodsSummary());
        map.put("items", toCardItemsMap(card.getItems()));
        putIfNotNull(map, "plannedArrivalLabel", card.getPlannedArrivalLabel());
        putIfNotNull(map, "plannedDepartureLabel", card.getPlannedDepartureLabel());
        putIfNotNull(map, "customerWindowLabel", card.getCustomerWindowLabel());
        putIfNotNull(map, "serviceDurationLabel", card.getServiceDurationLabel());
        putIfNotNull(map, "driverLabel", card.getDriverLabel());
        map.put("suggestedDriverUserId", card.getSuggestedDriverUserId());
        putIfNotNull(map, "suggestedDriverName", card.getSuggestedDriverName());
        putIfNotNull(map, "distanceText", card.getDistanceText());
        putIfNotNull(map, "durationText", card.getDurationText());
        putIfNotNull(map, "timeLabel", card.getTimeLabel());
        putIfNotNull(map, "statusLabel", card.getStatusLabel());
        putIfNotNull(map, "badgeLabel", card.getBadgeLabel());
        if (card.getPrimaryAction() != null) {
            map.put("primaryAction", card.getPrimaryAction());
        }
        return map;
    }

    private static List<Map<String, Object>> toStopCardsMap(List<SandboxTodayStopCardDto> stopCards) {
        if (stopCards == null || stopCards.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (SandboxTodayStopCardDto stopCard : stopCards) {
            if (stopCard == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            putIfNotNull(map, "cardKey", stopCard.getCardKey());
            map.put("stopSeq", stopCard.getStopSeq());
            putIfNotNull(map, "customerName", stopCard.getCustomerName());
            map.put("departmentId", stopCard.getDepartmentId());
            putIfNotNull(map, "goodsSummary", stopCard.getGoodsSummary());
            map.put("items", toCardItemsMap(stopCard.getItems()));
            putIfNotNull(map, "plannedArrivalLabel", stopCard.getPlannedArrivalLabel());
            putIfNotNull(map, "plannedDepartureLabel", stopCard.getPlannedDepartureLabel());
            putIfNotNull(map, "customerWindowLabel", stopCard.getCustomerWindowLabel());
            putIfNotNull(map, "deliveryWindowLabel", stopCard.getDeliveryWindowLabel());
            putIfNotNull(map, "windowStatusLabel", stopCard.getWindowStatusLabel());
            putIfNotNull(map, "windowRequirementLabel", stopCard.getWindowRequirementLabel());
            putIfNotNull(map, "arrivalStatusLabel", stopCard.getArrivalStatusLabel());
            putIfNotNull(map, "arrivalStatusTone", stopCard.getArrivalStatusTone());
            putIfNotNull(map, "serviceDurationLabel", stopCard.getServiceDurationLabel());
            putIfNotNull(map, "distanceText", stopCard.getDistanceText());
            putIfNotNull(map, "durationText", stopCard.getDurationText());
            putIfNotNull(map, "timeLabel", stopCard.getTimeLabel());
            putIfNotNull(map, "statusLabel", stopCard.getStatusLabel());
            if (stopCard.getPrimaryAction() != null) {
                map.put("primaryAction", stopCard.getPrimaryAction());
            }
            list.add(map);
        }
        return list;
    }

    private static List<Map<String, Object>> toCardItemsMap(List<SandboxTodayCardItemDto> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (SandboxTodayCardItemDto item : items) {
            if (item == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("liveOrderId", item.getLiveOrderId());
            putIfNotNull(map, "goodsName", item.getGoodsName());
            putIfNotNull(map, "standard", item.getStandard());
            map.put("quantity", item.getQuantity());
            putIfNotNull(map, "status", item.getStatus());
            list.add(map);
        }
        return list;
    }

    private static List<Map<String, Object>> toAvailableDriversMap(List<SandboxTodayAvailableDriverDto> drivers) {
        if (drivers == null || drivers.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (SandboxTodayAvailableDriverDto driver : drivers) {
            if (driver == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("driverUserId", driver.getDriverUserId());
            putIfNotNull(map, "driverName", driver.getDriverName());
            putIfNonBlank(map, "driverAvatarUrl", driver.getDriverAvatarUrl());
            putIfNotNull(map, "statusLabel", driver.getStatusLabel());
            list.add(map);
        }
        return list;
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

    private static Map<String, Object> toMapOverviewMap(SandboxTodayMapOverviewDto overview) {
        if (overview == null) {
            return defaultMapOverviewContractMap();
        }
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("summary", toMapSummaryMap(overview.getSummary()));
        map.put("depot", toMapDepotMap(overview.getDepot()));
        map.put("markers", toMapMarkerList(overview.getMarkers()));
        map.put("polylines", toMapPolylineList(overview.getPolylines()));
        map.put("legend", toMapLegendList(overview.getLegend()));
        map.put("missingCoordinateStops", toMapMissingStopList(overview.getMissingCoordinateStops()));
        putIfNotNull(map, "centerLat", overview.getCenterLat());
        putIfNotNull(map, "centerLng", overview.getCenterLng());
        putIfNotNull(map, "suggestedScale", overview.getSuggestedScale());
        putIfNotNull(map, "subkey", overview.getSubkey());
        putIfNotNull(map, "layerStyle", overview.getLayerStyle());
        putIfNotNull(map, "emptyHint", overview.getEmptyHint());
        return ensureMapOverviewContract(map);
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

    private static Map<String, Object> toMapSummaryMap(SandboxTodayMapSummaryDto summary) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (summary == null) {
            return defaultSummaryMap();
        }
        putIfNotNull(map, "customerStopCount", summary.getCustomerStopCount());
        putIfNotNull(map, "routeCount", summary.getRouteCount());
        putIfNotNull(map, "unassignedCount", summary.getUnassignedCount());
        return map;
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

    private static Map<String, Object> toMapDepotMap(SandboxTodayMapDepotDto depot) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (depot == null) {
            return map;
        }
        putIfNotNull(map, "markerType", depot.getMarkerType());
        putIfNotNull(map, "name", depot.getName());
        putIfNotNull(map, "lat", depot.getLat());
        putIfNotNull(map, "lng", depot.getLng());
        putIfNotNull(map, "colorKey", depot.getColorKey());
        putIfNotNull(map, "color", depot.getColor());
        return map;
    }

    private static List<Map<String, Object>> toMapMarkerList(List<SandboxTodayMapMarkerDto> markers) {
        if (markers == null || markers.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (SandboxTodayMapMarkerDto marker : markers) {
            if (marker == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            putIfNotNull(map, "markerKey", marker.getMarkerKey());
            putIfNotNull(map, "markerType", marker.getMarkerType());
            putIfNotNull(map, "lat", marker.getLat());
            putIfNotNull(map, "lng", marker.getLng());
            putIfNotNull(map, "customerName", marker.getCustomerName());
            putIfNotNull(map, "stopSeq", marker.getStopSeq());
            putIfNotNull(map, "driverName", marker.getDriverName());
            putIfNotNull(map, "driverUserId", marker.getDriverUserId());
            putIfNotNull(map, "departmentId", marker.getDepartmentId());
            putIfNotNull(map, "depFatherId", marker.getDepFatherId());
            putIfNotNull(map, "colorKey", marker.getColorKey());
            putIfNotNull(map, "color", marker.getColor());
            putIfNotNull(map, "assignmentLabel", marker.getAssignmentLabel());
            putIfNotNull(map, "displayTitle", marker.getDisplayTitle());
            putIfNotNull(map, "displaySubtitle", marker.getDisplaySubtitle());
            putIfNotNull(map, "arrivalLabel", marker.getArrivalLabel());
            putIfNotNull(map, "badgeText", marker.getBadgeText());
            list.add(map);
        }
        return list;
    }

    private static List<Map<String, Object>> toMapPolylineList(List<SandboxTodayMapPolylineDto> polylines) {
        if (polylines == null || polylines.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (SandboxTodayMapPolylineDto polyline : polylines) {
            if (polyline == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            putIfNotNull(map, "routeKey", polyline.getRouteKey());
            putIfNotNull(map, "driverUserId", polyline.getDriverUserId());
            putIfNotNull(map, "driverName", polyline.getDriverName());
            putIfNotNull(map, "colorKey", polyline.getColorKey());
            putIfNotNull(map, "color", polyline.getColor());
            putIfNotNull(map, "lineStyle", polyline.getLineStyle());
            putIfNotNull(map, "kind", polyline.getKind());
            putIfNotNull(map, "lineType", polyline.getLineType());
            map.put("points", toMapPointList(polyline.getPoints()));
            list.add(map);
        }
        return list;
    }

    private static List<Map<String, Object>> toMapPointList(List<SandboxTodayMapPointDto> points) {
        if (points == null || points.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (SandboxTodayMapPointDto point : points) {
            if (point == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            putIfNotNull(map, "lat", point.getLat());
            putIfNotNull(map, "lng", point.getLng());
            list.add(map);
        }
        return list;
    }

    private static List<Map<String, Object>> toMapLegendList(List<SandboxTodayMapLegendItemDto> legend) {
        if (legend == null || legend.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (SandboxTodayMapLegendItemDto item : legend) {
            if (item == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            putIfNotNull(map, "kind", item.getKind());
            putIfNotNull(map, "colorKey", item.getColorKey());
            putIfNotNull(map, "color", item.getColor());
            putIfNotNull(map, "label", item.getLabel());
            putIfNotNull(map, "driverUserId", item.getDriverUserId());
            putIfNotNull(map, "lineStyle", item.getLineStyle());
            list.add(map);
        }
        return list;
    }

    private static List<Map<String, Object>> toMapMissingStopList(List<SandboxTodayMapMissingStopDto> missing) {
        if (missing == null || missing.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (SandboxTodayMapMissingStopDto item : missing) {
            if (item == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            putIfNotNull(map, "customerName", item.getCustomerName());
            putIfNotNull(map, "departmentId", item.getDepartmentId());
            putIfNotNull(map, "depFatherId", item.getDepFatherId());
            putIfNotNull(map, "driverName", item.getDriverName());
            putIfNotNull(map, "driverUserId", item.getDriverUserId());
            putIfNotNull(map, "assignmentLabel", item.getAssignmentLabel());
            putIfNotNull(map, "reason", item.getReason());
            list.add(map);
        }
        return list;
    }
}
