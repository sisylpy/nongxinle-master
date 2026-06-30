package com.nongxinle.route;

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
}
