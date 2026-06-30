package com.nongxinle.route;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 未分配客户 · 人工调度入口动作。 */
public final class ManualDispatchPrimaryActionMaps {

    public static final String ACTION_TYPE_START_MANUAL_DISPATCH = "START_MANUAL_DISPATCH";

    public static final String DRIVER_PANORAMA_PATH =
            "/api/nxdisroutedispatch/sandbox/manual-dispatch/driver-panorama";
    public static final String SIMULATE_PATH =
            "/api/nxdisroutedispatch/sandbox/manual-dispatch/simulate";
    public static final String CONFIRM_PATH =
            "/api/nxdisroutedispatch/sandbox/manual-dispatch/confirm";

    private static final Set<String> REQUIRED_PAYLOAD_KEYS = new HashSet<String>(Arrays.asList(
            "disId", "routeDate", "batchCode", "operatorUserId",
            "departmentId", "depFatherId", "sandboxStopKey", "liveOrderIds",
            "driverPanoramaPath", "simulatePath", "confirmPath"));

    private ManualDispatchPrimaryActionMaps() {
    }

    public static Map<String, Object> enabledStartManualDispatch(String label, Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("START_MANUAL_DISPATCH payload 不能为空");
        }
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_START_MANUAL_DISPATCH);
        action.put("label", label != null ? label : "人工调度");
        action.put("enabled", Boolean.TRUE);
        action.put("disabledReason", "");
        action.put("toneClass", "stop-state-action");
        action.put("payload", compactPayload(payload));
        return action;
    }

    public static Map<String, Object> disabledStartManualDispatch(String reason) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_START_MANUAL_DISPATCH);
        action.put("label", "人工调度");
        action.put("enabled", Boolean.FALSE);
        action.put("disabledReason", reason != null ? reason : "暂不可人工调度");
        action.put("toneClass", "stop-state-action");
        return action;
    }

    public static Map<String, Object> buildPayload(Integer disId,
                                                   String routeDate,
                                                   String batchCode,
                                                   Integer operatorUserId,
                                                   Integer departmentId,
                                                   String sandboxStopKey,
                                                   List<Integer> liveOrderIds) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("disId", disId);
        payload.put("routeDate", routeDate);
        payload.put("batchCode", batchCode);
        payload.put("operatorUserId", operatorUserId);
        payload.put("departmentId", departmentId);
        payload.put("depFatherId", departmentId);
        payload.put("sandboxStopKey", sandboxStopKey);
        payload.put("liveOrderIds", liveOrderIds != null ? liveOrderIds : Collections.<Integer>emptyList());
        payload.put("driverPanoramaPath", DRIVER_PANORAMA_PATH);
        payload.put("simulatePath", SIMULATE_PATH);
        payload.put("confirmPath", CONFIRM_PATH);
        return compactPayload(payload);
    }

    private static Map<String, Object> compactPayload(Map<String, Object> payload) {
        Map<String, Object> compact = new LinkedHashMap<String, Object>();
        if (payload == null) {
            return compact;
        }
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (REQUIRED_PAYLOAD_KEYS.contains(key)) {
                if ("liveOrderIds".equals(key)) {
                    compact.put(key, value instanceof Collection ? value : Collections.emptyList());
                } else if (value != null) {
                    compact.put(key, value);
                }
            } else if (value != null && !(value instanceof Collection && ((Collection<?>) value).isEmpty())) {
                compact.put(key, value);
            }
        }
        return compact;
    }
}
