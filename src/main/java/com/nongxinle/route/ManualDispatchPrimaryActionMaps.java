package com.nongxinle.route;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 未分配客户：人工调度入口动作契约（Phase 2B）。 */
public final class ManualDispatchPrimaryActionMaps {

    public static final String ACTION_TYPE_START_MANUAL_DISPATCH = "START_MANUAL_DISPATCH";

    private static final String DRIVER_PANORAMA_PATH =
            "/api/nxdisroutedispatch/sandbox/manual-dispatch/driver-panorama";
    private static final String EDIT_PAGE_PATH =
            "/api/nxdisroutedispatch/sandbox/manual-dispatch/edit-page";
    private static final String SIMULATE_PATH =
            "/api/nxdisroutedispatch/sandbox/manual-dispatch/simulate";
    private static final String CONFIRM_PATH =
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
        action.put("action", ACTION_TYPE_START_MANUAL_DISPATCH);
        action.put("label", label != null ? label : "人工调度");
        action.put("enabled", Boolean.TRUE);
        action.put("disabledReason", "");
        action.put("payload", compactManualDispatchPayload(payload));
        return action;
    }

    public static Map<String, Object> disabledStartManualDispatch(String reason) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_START_MANUAL_DISPATCH);
        action.put("label", "人工调度");
        action.put("enabled", Boolean.FALSE);
        action.put("disabledReason", reason != null ? reason : "暂不可人工调度");
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
        return compactManualDispatchPayload(payload);
    }

    /** 正式 pageViewModel：enabled 才带 payload。 */
    public static Map<String, Object> toPageActionMap(Map<String, Object> action) {
        if (action == null || action.isEmpty()) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("actionType", action.get("actionType"));
        map.put("label", action.get("label"));
        Object enabled = action.get("enabled");
        map.put("enabled", enabled);
        if (Boolean.FALSE.equals(enabled)) {
            Object reason = action.get("disabledReason");
            if (reason != null && !String.valueOf(reason).trim().isEmpty()) {
                map.put("disabledReason", String.valueOf(reason).trim());
            }
            return map;
        }
        map.put("disabledReason", "");
        Object payload = action.get("payload");
        if (payload instanceof Map) {
            map.put("payload", compactManualDispatchPayload((Map<String, Object>) payload));
        }
        return map;
    }

    /** 保留合同必填字段；liveOrderIds 允许空数组；其余 null 字段剔除。 */
    private static Map<String, Object> compactManualDispatchPayload(Map<String, Object> payload) {
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
                continue;
            }
            if (value == null) {
                continue;
            }
            if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
                continue;
            }
            compact.put(key, value);
        }
        return compact;
    }
}
