package com.nongxinle.route;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** 司机路线人工编辑 primaryAction / routeEditAction 契约。 */
public final class DriverRouteEditPrimaryActionMaps {

    public static final String ACTION_TYPE_EDIT_DRIVER_ROUTE = "EDIT_DRIVER_ROUTE";
    public static final String EDIT_PAGE_PATH =
            "/api/nxdisroutedispatch/sandbox/driver-route-edit/page";
    public static final String PREVIEW_PATH =
            "/api/nxdisroutedispatch/sandbox/driver-route-edit/preview";
    public static final String CONFIRM_PATH =
            "/api/nxdisroutedispatch/sandbox/driver-route-edit/confirm";

    private DriverRouteEditPrimaryActionMaps() {
    }

    public static Map<String, Object> enabledEditRoute(String label, Map<String, Object> payload) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_EDIT_DRIVER_ROUTE);
        action.put("label", label != null ? label : "编辑路线");
        action.put("enabled", Boolean.TRUE);
        action.put("disabledReason", "");
        action.put("payload", payload != null ? payload : buildEmptyPayload());
        return action;
    }

    public static Map<String, Object> disabledEditRoute(String label, String reason) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_EDIT_DRIVER_ROUTE);
        action.put("label", label != null ? label : "编辑路线");
        action.put("enabled", Boolean.FALSE);
        action.put("disabledReason", reason != null ? reason : "当前不可编辑");
        action.put("payload", buildEmptyPayload());
        return action;
    }

    public static Map<String, Object> buildPayload(Integer disId,
                                                   String routeDate,
                                                   String batchCode,
                                                   Integer operatorUserId,
                                                   Integer driverUserId) {
        return buildPayload(disId, routeDate, batchCode, operatorUserId, driverUserId, null);
    }

    public static Map<String, Object> buildPayload(Integer disId,
                                                   String routeDate,
                                                   String batchCode,
                                                   Integer operatorUserId,
                                                   Integer driverUserId,
                                                   String sourcePage) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("disId", disId);
        payload.put("routeDate", routeDate);
        payload.put("batchCode", batchCode);
        payload.put("operatorUserId", operatorUserId);
        payload.put("driverUserId", driverUserId);
        payload.put("editPagePath", EDIT_PAGE_PATH);
        payload.put("previewPath", PREVIEW_PATH);
        payload.put("confirmPath", CONFIRM_PATH);
        if (sourcePage != null && !sourcePage.trim().isEmpty()) {
            payload.put("sourcePage", sourcePage.trim());
        }
        return payload;
    }

    private static Map<String, Object> buildEmptyPayload() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("disId", null);
        payload.put("routeDate", null);
        payload.put("batchCode", null);
        payload.put("operatorUserId", null);
        payload.put("driverUserId", null);
        payload.put("editPagePath", EDIT_PAGE_PATH);
        return payload;
    }
}
