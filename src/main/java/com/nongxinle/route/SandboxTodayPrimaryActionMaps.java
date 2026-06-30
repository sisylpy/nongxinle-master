package com.nongxinle.route;

import java.util.LinkedHashMap;
import java.util.Map;

/** 今日派车 primaryAction 契约 — 以 Map 输出，避免 JSON 序列化丢字段。 */
public final class SandboxTodayPrimaryActionMaps {

    public static final String ACTION_TYPE_EDIT_TODAY_TIME_WINDOW = "EDIT_TODAY_TIME_WINDOW";

    private SandboxTodayPrimaryActionMaps() {
    }

    public static Map<String, Object> enabledEditTodayTimeWindow(Map<String, Object> payload) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_EDIT_TODAY_TIME_WINDOW);
        action.put("action", ACTION_TYPE_EDIT_TODAY_TIME_WINDOW);
        action.put("label", "编辑");
        action.put("enabled", Boolean.TRUE);
        action.put("disabledReason", "");
        action.put("toneClass", "stop-state-action");
        action.put("payload", payload != null ? payload : buildEmptyPayload());
        return action;
    }

    public static Map<String, Object> buildEditTodayTimeWindowPayload(Integer disId,
                                                                      String routeDate,
                                                                      String batchCode,
                                                                      Integer operatorUserId,
                                                                      Integer departmentId,
                                                                      String sandboxStopKey,
                                                                      Integer deliveryStopId,
                                                                      Integer earliestDeliveryTimeS,
                                                                      Integer latestDeliveryTimeS,
                                                                      Integer serviceMinutes,
                                                                      Boolean timeWindowOverrideFlag,
                                                                      String customerWindowLabel,
                                                                      String customerName) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("disId", disId);
        payload.put("routeDate", routeDate);
        payload.put("batchCode", batchCode);
        payload.put("operatorUserId", operatorUserId);
        payload.put("departmentId", departmentId);
        payload.put("depFatherId", departmentId);
        payload.put("sandboxStopKey", sandboxStopKey);
        payload.put("deliveryStopId", deliveryStopId);
        payload.put("earliestDeliveryTimeS", earliestDeliveryTimeS);
        payload.put("latestDeliveryTimeS", latestDeliveryTimeS);
        payload.put("serviceMinutes", serviceMinutes);
        payload.put("timeWindowOverrideFlag", Boolean.TRUE.equals(timeWindowOverrideFlag));
        payload.put("customerWindowLabel", customerWindowLabel);
        payload.put("customerName", customerName);
        return payload;
    }

    private static Map<String, Object> buildEmptyPayload() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("disId", null);
        payload.put("routeDate", null);
        payload.put("batchCode", null);
        payload.put("operatorUserId", null);
        payload.put("sandboxStopKey", null);
        payload.put("departmentId", null);
        payload.put("depFatherId", null);
        payload.put("liveOrderIds", java.util.Collections.emptyList());
        payload.put("driverUserId", null);
        payload.put("suggestedDriverUserId", null);
        return payload;
    }
}
