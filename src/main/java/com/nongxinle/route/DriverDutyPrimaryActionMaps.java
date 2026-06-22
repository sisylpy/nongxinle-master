package com.nongxinle.route;

import java.util.LinkedHashMap;
import java.util.Map;

/** 司机可派状态页：关闭可派 primaryAction 契约。 */
public final class DriverDutyPrimaryActionMaps {

    private DriverDutyPrimaryActionMaps() {
    }

    public static Map<String, Object> enabledToggleDutyOn(String label, Map<String, Object> payload) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", "TOGGLE_DUTY_ON");
        action.put("action", "TOGGLE_DUTY_ON");
        action.put("label", label != null ? label : "开启可派");
        action.put("enabled", Boolean.TRUE);
        action.put("disabledReason", "");
        action.put("payload", payload != null ? payload : buildEmptyToggleDutyOffPayload());
        return action;
    }

    public static Map<String, Object> enabledToggleDutyOff(String label, Map<String, Object> payload) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", DisRouteDriverDutyLockHelper.ACTION_TYPE_TOGGLE_DUTY_OFF);
        action.put("action", DisRouteDriverDutyLockHelper.ACTION_TYPE_TOGGLE_DUTY_OFF);
        action.put("label", label != null ? label : "关闭可派");
        action.put("enabled", Boolean.TRUE);
        action.put("disabledReason", "");
        action.put("payload", payload != null ? payload : buildEmptyToggleDutyOffPayload());
        return action;
    }

    public static Map<String, Object> disabledToggleDutyOff(String label, String reason) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", DisRouteDriverDutyLockHelper.ACTION_TYPE_TOGGLE_DUTY_OFF);
        action.put("action", DisRouteDriverDutyLockHelper.ACTION_TYPE_TOGGLE_DUTY_OFF);
        action.put("label", label != null ? label : "关闭可派");
        action.put("enabled", Boolean.FALSE);
        action.put("disabledReason", reason != null ? reason : "暂不可关闭可派");
        action.put("payload", buildEmptyToggleDutyOffPayload());
        return action;
    }

    public static Map<String, Object> buildToggleDutyOffPayload(Integer disId,
                                                                 String routeDate,
                                                                 Integer driverUserId,
                                                                 Integer operatorUserId) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("disId", disId);
        payload.put("routeDate", routeDate);
        payload.put("driverUserId", driverUserId);
        payload.put("operatorUserId", operatorUserId);
        return payload;
    }

    private static Map<String, Object> buildEmptyToggleDutyOffPayload() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("disId", null);
        payload.put("routeDate", null);
        payload.put("driverUserId", null);
        payload.put("operatorUserId", null);
        return payload;
    }
}
