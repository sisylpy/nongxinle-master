package com.nongxinle.route;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Phase 2b-2：调度域 code → 中文 label（保留原 code 字段不变）。 */
public final class DisRouteDispatchLabels {

    private static final Map<String, String> LABELS;

    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put(DisRouteDispatchBatch.MORNING, "早班");
        map.put(DisRouteDispatchBatch.AFTERNOON, "下午班");
        map.put(DisRouteDispatchBatch.ADHOC, "临时批次");
        map.put("MIDDAY", "中班");
        map.put("EVENING", "晚班");

        map.put(DisShipmentTaskStatus.SIMULATED, "系统建议");
        map.put(DisShipmentTaskStatus.ASSIGNED, "已分派");
        map.put(DisShipmentTaskStatus.READY_TO_GO, "可出发");
        map.put(DisShipmentTaskStatus.UNASSIGNED, "未分派");
        map.put(DisShipmentTaskStatus.CANCELLED, "已取消");
        map.put(DisShipmentTaskStatus.CLOSED, "已关闭");
        map.put(DisShipmentTaskStatus.IN_DELIVERY, "配送中");
        map.put(DisShipmentTaskStatus.DELIVERED, "已送达");
        map.put(DisShipmentTaskStatus.EXCEPTION, "配送异常");

        map.put(DisRouteFeasibilityStatus.FEASIBLE, "可执行");
        map.put(DisRouteFeasibilityStatus.HAS_WAIT, "有等待");
        map.put(DisRouteFeasibilityStatus.HAS_LATE, "有迟到");
        map.put(DisRouteFeasibilityStatus.INFEASIBLE, "当前不可执行");
        map.put(DisRouteFeasibilityStatus.DRIVER_TOO_LATE, "当前不可执行");
        map.put(DisRouteFeasibilityStatus.NO_AVAILABLE_DRIVER, "无可派司机");
        map.put(DisRouteFeasibilityStatus.IDLE, "空闲");

        map.put(DisRouteScheduleStatus.OK, "正常");
        map.put(DisRouteScheduleStatus.HAS_LATE, "有迟到");
        map.put(DisRouteScheduleStatus.NO_WINDOW, "未设置送达窗口");

        map.put(DisRouteStopTimeWindowStatus.OK, "正常");
        map.put(DisRouteStopTimeWindowStatus.EARLY_WAIT, "早到等待");
        map.put(DisRouteStopTimeWindowStatus.EARLY_ARRIVAL, "早于窗口");
        map.put(DisRouteStopTimeWindowStatus.LATE, "预计迟到");
        map.put(DisRouteStopTimeWindowStatus.SUPPLEMENT_AFTER_WINDOW, "已过常规窗口，按补单最快送");
        map.put(DisRouteStopTimeWindowStatus.NO_WINDOW, "未设置送达窗口");

        map.put(DisDriverDutyStatus.ON_DUTY, "可派");
        map.put(DisDriverDutyStatus.OFF_DUTY, "不可派");
        map.put(DisRouteBatchEligibility.INELIGIBLE_OFF_DUTY, "不可派");
        map.put(DisRouteBatchEligibility.INELIGIBLE_LOADING, "装车中");
        map.put(DisRouteBatchEligibility.NOT_DRIVER_ROLE, "非司机角色");
        map.put(DisRouteBatchEligibility.NOT_BELONG_TO_DIS, "不属于当前配送商");

        map.put(DisRouteCustomerTier.VIP, "VIP客户");
        map.put(DisRouteCustomerTier.NORMAL, "普通客户");
        map.put(DisRouteCustomerTier.SMALL, "小客户");
        map.put(DisRouteCustomerTier.NEW, "新客户");

        map.put(DisRoutePlanTemporalStatus.UPCOMING, "未开始");
        map.put(DisRoutePlanTemporalStatus.ACTIVE, "进行中");
        map.put(DisRoutePlanTemporalStatus.EXPIRED, "已过期");

        map.put(DisRouteDriverRouteStatus.IDLE, "空闲");
        map.put(DisRouteDriverRouteStatus.DISPATCH_CONFIRMED, "已确认待装车");
        map.put(DisRouteDriverRouteStatus.LOADING, "装车中");
        map.put(DisRouteDriverRouteStatus.READY_TO_DEPART, "待出发");
        map.put(DisRouteDriverRouteStatus.IN_DELIVERY, "配送中");
        map.put(DisRouteDriverRouteStatus.DELIVERED, "已完成");
        map.put(DisRouteDriverRouteStatus.COMPLETED, "已完成");
        map.put(DisRouteDriverRouteStatus.CLOSED, "已关闭");

        map.put(DisRouteStopTemporalStatus.UPCOMING, "未到达");
        map.put(DisRouteStopTemporalStatus.OK, "正常");
        map.put(DisRouteStopTemporalStatus.EXPIRED, "已过期");
        map.put(DisRouteStopTemporalStatus.PAST_DUE, "已过期");

        LABELS = Collections.unmodifiableMap(map);
    }

    private DisRouteDispatchLabels() {
    }

    public static String label(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        String key = code.trim().toUpperCase();
        if (LABELS.containsKey(key)) {
            return LABELS.get(key);
        }
        return LABELS.get(code.trim());
    }
}
