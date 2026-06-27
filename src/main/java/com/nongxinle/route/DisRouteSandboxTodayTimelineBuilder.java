package com.nongxinle.route;

import com.nongxinle.dto.route.SandboxTodaySectionCardDto;
import com.nongxinle.dto.route.SandboxTodayStopCardDto;
import com.nongxinle.entity.NxDisDriverRouteEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 今日派车 DRIVER_ROUTE timeline[] 组装。 */
public final class DisRouteSandboxTodayTimelineBuilder {

    public static final String DEPOT_NAME = "市场";
    public static final String RETURN_NAME = "返回市场";

    private DisRouteSandboxTodayTimelineBuilder() {
    }

    public static List<Map<String, Object>> buildFromStopCardMaps(List<Map<String, Object>> stopCards,
                                                                  SandboxTodaySectionCardDto routeCard,
                                                                  String depotName) {
        List<Map<String, Object>> timeline = new ArrayList<Map<String, Object>>();
        String depot = depotName != null && !depotName.trim().isEmpty() ? depotName.trim() : DEPOT_NAME;

        Map<String, Object> start = new LinkedHashMap<String, Object>();
        start.put("type", "start");
        start.put("marker", "起");
        start.put("name", depot);
        start.put("timeRight", routeCard != null && routeCard.getScheduleHeadline() != null
                && routeCard.getScheduleHeadline().contains("现在可送")
                ? "现在可送" : "现在可送");
        timeline.add(start);

        int seq = 0;
        if (stopCards != null) {
            for (Map<String, Object> stopCard : stopCards) {
                if (stopCard == null) {
                    continue;
                }
                Object distanceText = stopCard.get("distanceText");
                Object durationText = stopCard.get("durationText");
                if (distanceText != null || durationText != null) {
                    Map<String, Object> leg = new LinkedHashMap<String, Object>();
                    leg.put("type", "leg");
                    leg.put("legText", joinLegText(
                            distanceText != null ? String.valueOf(distanceText) : null,
                            durationText != null ? String.valueOf(durationText) : null));
                    timeline.add(leg);
                }
                seq++;
                Map<String, Object> stopNode = new LinkedHashMap<String, Object>();
                stopNode.put("type", "stop");
                Object stopSeq = stopCard.get("stopSeq");
                stopNode.put("seq", stopSeq != null ? stopSeq : seq);
                putIfNotNull(stopNode, "cardKey", stopCard.get("cardKey"));
                stopNode.put("name", stopCard.get("customerName"));
                stopNode.put("customerName", stopCard.get("customerName"));
                stopNode.put("arrivalLabel", stopCard.get("plannedArrivalLabel"));
                stopNode.put("departureLabel", stopCard.get("plannedDepartureLabel"));
                stopNode.put("windowLabel", stopCard.get("customerWindowLabel"));
                putIfNotNull(stopNode, "serviceDurationLabel", stopCard.get("serviceDurationLabel"));
                putIfNotNull(stopNode, "timeLabel", stopCard.get("timeLabel"));
                stopNode.put("goodsSummary", stopCard.get("goodsSummary"));
                stopNode.put("statusLabel", stopCard.get("statusLabel"));
                if (stopCard.get("primaryAction") != null) {
                    stopNode.put("primaryAction", stopCard.get("primaryAction"));
                }
                timeline.add(stopNode);
            }
        }

        Map<String, Object> end = new LinkedHashMap<String, Object>();
        end.put("type", "end");
        end.put("marker", "终");
        end.put("name", RETURN_NAME);
        end.put("timeRight", routeCard != null && routeCard.getTotalDurationText() != null
                ? "约 " + routeCard.getTotalDurationText() + " 后 返回" : "预计返回");
        timeline.add(end);
        enrichStopLegPresentation(timeline);
        return timeline;
    }

    /** PR-2c：从 VisibleDriverRouteSnapshot 组装 timeline（与 stopCards 同源）。 */
    public static List<Map<String, Object>> buildFromVisibleStopSnapshots(
            VisibleDriverRouteSnapshot routeSnapshot,
            List<SandboxTodayStopCardDto> stopCards,
            String depotName) {
        NxDisDriverRouteEntity route = routeSnapshot != null ? routeSnapshot.getSourceRoute() : null;
        return build(route, stopCards, depotName);
    }

    public static List<Map<String, Object>> build(NxDisDriverRouteEntity route,
                                                  List<SandboxTodayStopCardDto> stopCards,
                                                  String depotName) {
        List<Map<String, Object>> timeline = new ArrayList<Map<String, Object>>();
        String depot = depotName != null && !depotName.trim().isEmpty() ? depotName.trim() : DEPOT_NAME;

        Map<String, Object> start = new LinkedHashMap<String, Object>();
        start.put("type", "start");
        start.put("marker", "起");
        start.put("name", depot);
        start.put("timeRight", resolveStartTimeRight(route, stopCards));
        timeline.add(start);

        int seq = 0;
        for (SandboxTodayStopCardDto stopCard : stopCards) {
            if (stopCard == null) {
                continue;
            }
            if (stopCard.getDistanceText() != null || stopCard.getDurationText() != null) {
                Map<String, Object> leg = new LinkedHashMap<String, Object>();
                leg.put("type", "leg");
                leg.put("legText", joinLegText(stopCard.getDistanceText(), stopCard.getDurationText()));
                timeline.add(leg);
            }
            seq++;
            Map<String, Object> stopNode = new LinkedHashMap<String, Object>();
            stopNode.put("type", "stop");
            stopNode.put("seq", stopCard.getStopSeq() != null ? stopCard.getStopSeq() : seq);
            putIfNotNull(stopNode, "cardKey", stopCard.getCardKey());
            stopNode.put("name", stopCard.getCustomerName());
            stopNode.put("customerName", stopCard.getCustomerName());
            stopNode.put("arrivalLabel", stopCard.getPlannedArrivalLabel());
            stopNode.put("departureLabel", stopCard.getPlannedDepartureLabel());
            stopNode.put("windowLabel", stopCard.getCustomerWindowLabel());
            putIfNotNull(stopNode, "serviceDurationLabel", stopCard.getServiceDurationLabel());
            putIfNotNull(stopNode, "timeLabel", stopCard.getTimeLabel());
            stopNode.put("goodsSummary", stopCard.getGoodsSummary());
            stopNode.put("statusLabel", stopCard.getStatusLabel());
            putIfNotNull(stopNode, "customerTimeWindow", stopCard.getCustomerWindowLabel());
            putIfNotNull(stopNode, "systemEta", firstNonBlank(
                    stopCard.getPlannedArrivalLabel(), stopCard.getTimeLabel()));
            putIfNotNull(stopNode, "distanceText", stopCard.getDistanceText());
            putIfNotNull(stopNode, "durationText", stopCard.getDurationText());
            if (stopCard.getPrimaryAction() != null) {
                stopNode.put("primaryAction", stopCard.getPrimaryAction());
            }
            timeline.add(stopNode);
        }

        if (route != null && (route.getReturnLegDistanceM() != null || route.getReturnLegDurationS() != null)) {
            Map<String, Object> returnLeg = new LinkedHashMap<String, Object>();
            returnLeg.put("type", "leg");
            returnLeg.put("legRole", "RETURN");
            returnLeg.put("legText", joinLegText(
                    DisRouteSandboxDisplayFormatHelper.formatDistanceText(route.getReturnLegDistanceM()),
                    DisRouteSandboxDisplayFormatHelper.formatDurationText(route.getReturnLegDurationS())));
            timeline.add(returnLeg);
        } else if (route != null && route.getReturnLegLabel() != null && !route.getReturnLegLabel().trim().isEmpty()) {
            Map<String, Object> returnLeg = new LinkedHashMap<String, Object>();
            returnLeg.put("type", "leg");
            returnLeg.put("legRole", "RETURN");
            returnLeg.put("legText", route.getReturnLegLabel().trim());
            timeline.add(returnLeg);
        }

        Map<String, Object> end = new LinkedHashMap<String, Object>();
        end.put("type", "end");
        end.put("marker", "终");
        end.put("name", RETURN_NAME);
        end.put("timeRight", resolveEndTimeRight(route));
        timeline.add(end);
        enrichStopLegPresentation(timeline);
        return timeline;
    }

    /** 门店卡展示：到站距离 + 前往下一站 hint（与装车/配送页统一）。 */
    public static void enrichStopLegPresentation(List<Map<String, Object>> timeline) {
        if (timeline == null || timeline.isEmpty()) {
            return;
        }
        ensureLegNodes(timeline);
        List<Map<String, Object>> stopNodes = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> node : timeline) {
            if (node != null && "stop".equals(node.get("type"))) {
                stopNodes.add(node);
            }
        }
        for (int i = 0; i < stopNodes.size(); i++) {
            Map<String, Object> stopNode = stopNodes.get(i);
            String legDistanceLabel = joinLegText(
                    stopNode.get("distanceText") != null ? String.valueOf(stopNode.get("distanceText")) : null,
                    stopNode.get("durationText") != null ? String.valueOf(stopNode.get("durationText")) : null);
            putIfNotNull(stopNode, "legDistanceLabel", legDistanceLabel);
            if (i + 1 < stopNodes.size()) {
                Map<String, Object> nextStop = stopNodes.get(i + 1);
                String nextLegHint = joinLegText(
                        nextStop.get("distanceText") != null ? String.valueOf(nextStop.get("distanceText")) : null,
                        nextStop.get("durationText") != null ? String.valueOf(nextStop.get("durationText")) : null);
                putIfNotNull(stopNode, "nextLegHint", nextLegHint);
            }
        }
    }

    /** 若站点已有距离但 timeline 缺 leg 节点，补插 leg（司机终端 overlay 后使用）。 */
    public static void ensureLegNodes(List<Map<String, Object>> timeline) {
        if (timeline == null || timeline.isEmpty()) {
            return;
        }
        List<Map<String, Object>> rebuilt = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> node : timeline) {
            if (node == null) {
                continue;
            }
            if ("stop".equals(node.get("type"))) {
                boolean prevIsLeg = !rebuilt.isEmpty()
                        && "leg".equals(rebuilt.get(rebuilt.size() - 1).get("type"));
                String legText = joinLegText(
                        node.get("distanceText") != null ? String.valueOf(node.get("distanceText")) : null,
                        node.get("durationText") != null ? String.valueOf(node.get("durationText")) : null);
                if (!prevIsLeg && legText != null && !legText.trim().isEmpty()) {
                    Map<String, Object> leg = new LinkedHashMap<String, Object>();
                    leg.put("type", "leg");
                    leg.put("legText", legText);
                    rebuilt.add(leg);
                }
            }
            rebuilt.add(node);
        }
        timeline.clear();
        timeline.addAll(rebuilt);
    }

    private static String resolveStartTimeRight(NxDisDriverRouteEntity route,
                                                List<SandboxTodayStopCardDto> stopCards) {
        if (route != null) {
            if (route.getPlannedDepartLabel() != null && !route.getPlannedDepartLabel().trim().isEmpty()) {
                return route.getPlannedDepartLabel().trim();
            }
            if (route.getRouteScheduleSummaryLabel() != null
                    && route.getRouteScheduleSummaryLabel().contains("现在可送")) {
                return "现在可送";
            }
        }
        if (stopCards != null) {
            for (SandboxTodayStopCardDto stopCard : stopCards) {
                if (stopCard != null && stopCard.getPlannedArrivalLabel() != null) {
                    return "现在可送";
                }
            }
        }
        return "现在可送";
    }

    private static String resolveEndTimeRight(NxDisDriverRouteEntity route) {
        if (route == null) {
            return "预计返回";
        }
        if (route.getPlannedReturnLabel() != null && !route.getPlannedReturnLabel().trim().isEmpty()) {
            return route.getPlannedReturnLabel().trim();
        }
        if (route.getPlannedFinishLabel() != null && !route.getPlannedFinishLabel().trim().isEmpty()) {
            return route.getPlannedFinishLabel().trim();
        }
        return "预计返回";
    }

    public static String joinLegText(String distanceText, String durationText) {
        if (distanceText == null && durationText == null) {
            return null;
        }
        if (distanceText != null && durationText != null) {
            return distanceText + " · " + durationText;
        }
        return distanceText != null ? distanceText : durationText;
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
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
}
