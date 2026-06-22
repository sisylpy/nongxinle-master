package com.nongxinle.route;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisRouteWorkbenchActionCode.CONFIRM_CUSTOMER;
import static com.nongxinle.route.DisRouteWorkbenchActionCode.GO_LOADING;
import static com.nongxinle.route.DisRouteWorkbenchActionCode.RETURN_TO_DISPATCH;

/** 今日派车 primaryAction 契约 — 以 Map 输出，避免 JSON 序列化丢字段。 */
public final class SandboxTodayPrimaryActionMaps {

    public static final String ACTION_TYPE_CONFIRM_SANDBOX_STOP = "CONFIRM_SANDBOX_STOP";
    public static final String ACTION_TYPE_RETURN_TO_SANDBOX = "RETURN_TO_SANDBOX";
    public static final String ACTION_TYPE_GO_LOADING = GO_LOADING;
    public static final String ACTION_TYPE_RETURN_TO_DISPATCH = RETURN_TO_DISPATCH;

    private SandboxTodayPrimaryActionMaps() {
    }

    public static Map<String, Object> enabledConfirmSandboxStop(String label, Map<String, Object> payload) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_CONFIRM_SANDBOX_STOP);
        action.put("action", CONFIRM_CUSTOMER);
        action.put("label", label != null ? label : "确认分派给司机");
        action.put("enabled", Boolean.TRUE);
        action.put("disabledReason", "");
        action.put("payload", payload != null ? payload : buildEmptyPayload());
        return action;
    }

    public static Map<String, Object> disabledConfirmSandboxStop(String label, String reason) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_CONFIRM_SANDBOX_STOP);
        action.put("action", CONFIRM_CUSTOMER);
        action.put("label", label != null ? label : "确认分派给司机");
        action.put("enabled", Boolean.FALSE);
        action.put("disabledReason", reason != null ? reason : "暂不可确认");
        action.put("payload", buildEmptyPayload());
        return action;
    }

    public static Map<String, Object> enabledReturnToSandbox(String label, Map<String, Object> payload) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_RETURN_TO_SANDBOX);
        action.put("action", ACTION_TYPE_RETURN_TO_SANDBOX);
        action.put("label", label != null ? label : "取消确认");
        action.put("enabled", Boolean.TRUE);
        action.put("disabledReason", "");
        action.put("payload", payload != null ? payload : buildEmptyReturnPayload());
        return action;
    }

    public static Map<String, Object> disabledReturnToSandbox(String label, String reason) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_RETURN_TO_SANDBOX);
        action.put("action", ACTION_TYPE_RETURN_TO_SANDBOX);
        action.put("label", label != null ? label : "取消确认");
        action.put("enabled", Boolean.FALSE);
        action.put("disabledReason", reason != null ? reason : "暂不可取消");
        action.put("payload", buildEmptyReturnPayload());
        return action;
    }

    public static Map<String, Object> enabledGoLoading(String label, Map<String, Object> payload) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_GO_LOADING);
        action.put("action", GO_LOADING);
        action.put("label", label != null ? label : "进入装车");
        action.put("enabled", Boolean.TRUE);
        action.put("disabledReason", "");
        action.put("payload", payload != null ? payload : buildEmptyGoLoadingPayload());
        return action;
    }

    public static Map<String, Object> disabledGoLoading(String label, String reason) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_GO_LOADING);
        action.put("action", GO_LOADING);
        action.put("label", label != null ? label : "进入装车");
        action.put("enabled", Boolean.FALSE);
        action.put("disabledReason", reason != null ? reason : "暂不可进入装车");
        action.put("payload", buildEmptyGoLoadingPayload());
        return action;
    }

    public static Map<String, Object> enabledReturnToDispatch(String label, Map<String, Object> payload) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_RETURN_TO_DISPATCH);
        action.put("action", RETURN_TO_DISPATCH);
        action.put("label", label != null ? label : DisRouteLoadingGateHelper.RETURN_TO_DISPATCH_LABEL);
        action.put("enabled", Boolean.TRUE);
        action.put("disabledReason", "");
        action.put("payload", payload != null ? payload : buildEmptyReturnToDispatchPayload());
        return action;
    }

    public static Map<String, Object> disabledReturnToDispatch(String label, String reason) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_RETURN_TO_DISPATCH);
        action.put("action", RETURN_TO_DISPATCH);
        action.put("label", label != null ? label : DisRouteLoadingGateHelper.RETURN_TO_DISPATCH_LABEL);
        action.put("enabled", Boolean.FALSE);
        action.put("disabledReason", reason != null ? reason : "暂不可撤销");
        action.put("payload", buildEmptyReturnToDispatchPayload());
        return action;
    }

    public static final String ACTION_TYPE_STATUS_ONLY = "STATUS_ONLY";

    public static Map<String, Object> statusOnly(String label, String reason) {
        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", ACTION_TYPE_STATUS_ONLY);
        action.put("action", ACTION_TYPE_STATUS_ONLY);
        action.put("label", label != null ? label : "不可操作");
        action.put("enabled", Boolean.FALSE);
        action.put("disabledReason", reason != null ? reason : "");
        action.put("payload", Collections.<String, Object>emptyMap());
        return action;
    }

    public static Map<String, Object> disabledPlain(String label, String reason) {
        return statusOnly(label, reason);
    }

    public static Map<String, Object> buildConfirmPayload(Integer disId,
                                                            String routeDate,
                                                            String batchCode,
                                                            Integer operatorUserId,
                                                            String sandboxStopKey,
                                                            Integer departmentId,
                                                            List<Integer> liveOrderIds,
                                                            Integer driverUserId) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("disId", disId);
        payload.put("routeDate", routeDate);
        payload.put("batchCode", batchCode);
        payload.put("operatorUserId", operatorUserId);
        payload.put("sandboxStopKey", sandboxStopKey);
        payload.put("departmentId", departmentId);
        payload.put("depFatherId", departmentId);
        payload.put("liveOrderIds", liveOrderIds != null ? liveOrderIds : java.util.Collections.emptyList());
        payload.put("driverUserId", driverUserId);
        payload.put("suggestedDriverUserId", driverUserId);
        return payload;
    }

    public static Map<String, Object> buildReturnPayload(Integer disId,
                                                         String routeDate,
                                                         String batchCode,
                                                         Integer operatorUserId,
                                                         Integer deliveryStopId,
                                                         String confirmTitle,
                                                         String confirmMessage) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("disId", disId);
        payload.put("routeDate", routeDate);
        payload.put("batchCode", batchCode);
        payload.put("operatorUserId", operatorUserId);
        payload.put("deliveryStopId", deliveryStopId);
        payload.put("confirmTitle", confirmTitle != null ? confirmTitle : "取消确认");
        payload.put("confirmMessage", confirmMessage != null ? confirmMessage : "");
        return payload;
    }

    public static Map<String, Object> buildGoLoadingPayload(Integer disId,
                                                             String routeDate,
                                                             String batchCode,
                                                             Integer operatorUserId,
                                                             Integer driverUserId,
                                                             Integer driverRouteId,
                                                             Integer planId) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("disId", disId);
        payload.put("routeDate", routeDate);
        payload.put("batchCode", batchCode);
        payload.put("operatorUserId", operatorUserId);
        payload.put("driverUserId", driverUserId);
        payload.put("driverRouteId", driverRouteId);
        payload.put("planId", planId);
        payload.put("confirmTitle", "让司机去装车");
        payload.put("confirmMessage", "确认后该司机路线会进入装车页，司机可开始装货。");
        return payload;
    }

    public static Map<String, Object> buildReturnToDispatchPayload(Integer disId,
                                                                      String routeDate,
                                                                      String batchCode,
                                                                      Integer operatorUserId,
                                                                      Integer driverUserId,
                                                                      Integer driverRouteId,
                                                                      Integer planId) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("disId", disId);
        payload.put("routeDate", routeDate);
        payload.put("batchCode", batchCode);
        payload.put("operatorUserId", operatorUserId);
        payload.put("driverUserId", driverUserId);
        payload.put("driverRouteId", driverRouteId);
        payload.put("planId", planId);
        payload.put("confirmTitle", DisRouteLoadingGateHelper.RETURN_CONFIRM_TITLE);
        payload.put("confirmMessage", DisRouteLoadingGateHelper.RETURN_CONFIRM_MESSAGE);
        return payload;
    }

    private static Map<String, Object> buildEmptyReturnToDispatchPayload() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("disId", null);
        payload.put("routeDate", null);
        payload.put("batchCode", null);
        payload.put("operatorUserId", null);
        payload.put("driverUserId", null);
        payload.put("driverRouteId", null);
        payload.put("planId", null);
        payload.put("confirmTitle", DisRouteLoadingGateHelper.RETURN_CONFIRM_TITLE);
        payload.put("confirmMessage", DisRouteLoadingGateHelper.RETURN_CONFIRM_MESSAGE);
        return payload;
    }

    private static Map<String, Object> buildEmptyGoLoadingPayload() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("disId", null);
        payload.put("routeDate", null);
        payload.put("batchCode", null);
        payload.put("operatorUserId", null);
        payload.put("driverUserId", null);
        payload.put("driverRouteId", null);
        payload.put("planId", null);
        payload.put("confirmTitle", "让司机去装车");
        payload.put("confirmMessage", "确认后该司机路线会进入装车页，司机可开始装货。");
        return payload;
    }

    private static Map<String, Object> buildEmptyReturnPayload() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("disId", null);
        payload.put("routeDate", null);
        payload.put("batchCode", null);
        payload.put("operatorUserId", null);
        payload.put("deliveryStopId", null);
        payload.put("confirmTitle", null);
        payload.put("confirmMessage", null);
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
