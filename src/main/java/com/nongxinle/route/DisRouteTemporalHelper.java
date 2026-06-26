package com.nongxinle.route;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Phase 2b-6：派车读模型时间主权（服务器时间为准，前端不猜）。
 */
public final class DisRouteTemporalHelper {

    private DisRouteTemporalHelper() {
    }

    public static String formatTodayYyyyMmDd(Date serverNow) {
        return new SimpleDateFormat("yyyy-MM-dd").format(serverNow);
    }

    public static String formatRouteDateLabel(String routeDate, Date serverNow) {
        if (routeDate == null || routeDate.trim().isEmpty()) {
            return null;
        }
        String today = formatTodayYyyyMmDd(serverNow);
        if (routeDate.equals(today)) {
            return "今天";
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(serverNow);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrow = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        if (routeDate.equals(tomorrow)) {
            return "明天";
        }
        try {
            Date date = DisRouteOrderArriveDateHelper.parseRouteDateStart(routeDate);
            return new SimpleDateFormat("M月d日").format(date);
        } catch (IllegalArgumentException ex) {
            return routeDate;
        }
    }

    public static String formatDateTimeLabel(Date dateTime, Date serverNow) {
        if (dateTime == null) {
            return null;
        }
        String datePart = new SimpleDateFormat("yyyy-MM-dd").format(dateTime);
        String dayLabel = formatRouteDateLabel(datePart, serverNow);
        String time = new SimpleDateFormat("HH:mm").format(dateTime);
        if (dayLabel == null) {
            return RouteDispatchDateFormat.format(dateTime);
        }
        return dayLabel + " " + time;
    }

    /**
     * 派车页时间 label：routeDate 当天仅 HH:mm；当前时刻「现在」；确跨日「次日 HH:mm」。
     * 禁止在同 routeDate 凌晨把 05:54 显示成「明天 04:31」。
     */
    public static String formatRouteTimeLabel(Date at, Date serverNow, String routeDate) {
        if (at == null) {
            return null;
        }
        if (isSameInstant(at, serverNow)) {
            return "现在";
        }
        String time = new SimpleDateFormat("HH:mm").format(at);
        String atDate = new SimpleDateFormat("yyyy-MM-dd").format(at);
        if (routeDate != null && !routeDate.trim().isEmpty()) {
            if (routeDate.trim().equals(atDate)) {
                return time;
            }
            if (isNextCalendarDay(routeDate.trim(), atDate)) {
                return "次日 " + time;
            }
        }
        if (serverNow != null) {
            String today = formatTodayYyyyMmDd(serverNow);
            if (today.equals(atDate)) {
                return time;
            }
            Calendar next = Calendar.getInstance();
            next.setTime(serverNow);
            next.add(Calendar.DAY_OF_YEAR, 1);
            String tomorrow = new SimpleDateFormat("yyyy-MM-dd").format(next.getTime());
            if (tomorrow.equals(atDate)) {
                return "次日 " + time;
            }
        }
        String dayLabel = formatRouteDateLabel(atDate, serverNow);
        return dayLabel != null ? dayLabel + " " + time : time;
    }

    private static boolean isSameInstant(Date a, Date b) {
        if (a == null || b == null) {
            return false;
        }
        return Math.abs(a.getTime() - b.getTime()) <= 60_000L;
    }

    private static boolean isNextCalendarDay(String baseDate, String targetDate) {
        try {
            Date dayStart = DisRouteOrderArriveDateHelper.parseRouteDateStart(baseDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(dayStart);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            String nextDay = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
            return nextDay.equals(targetDate);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static String resolvePlanTemporalStatus(String routeDate,
                                                   Date batchStartAt,
                                                   Date batchEndAt,
                                                   Date serverNow) {
        if (routeDate == null || serverNow == null) {
            return DisRoutePlanTemporalStatus.UPCOMING;
        }
        String today = formatTodayYyyyMmDd(serverNow);
        int cmp = routeDate.compareTo(today);
        if (cmp < 0) {
            return DisRoutePlanTemporalStatus.EXPIRED;
        }
        if (cmp > 0) {
            return DisRoutePlanTemporalStatus.UPCOMING;
        }
        if (batchEndAt != null && serverNow.after(batchEndAt)) {
            return DisRoutePlanTemporalStatus.EXPIRED;
        }
        if (batchStartAt != null && serverNow.before(batchStartAt)) {
            return DisRoutePlanTemporalStatus.UPCOMING;
        }
        return DisRoutePlanTemporalStatus.ACTIVE;
    }

    public static String resolveStopTemporalStatus(Date plannedArrivalAt, Date serverNow) {
        if (plannedArrivalAt == null || serverNow == null) {
            return DisRouteStopTemporalStatus.UPCOMING;
        }
        if (plannedArrivalAt.before(serverNow)) {
            return DisRouteStopTemporalStatus.PAST_DUE;
        }
        return DisRouteStopTemporalStatus.UPCOMING;
    }

    public static void enrichPlanTemporalFields(NxDisRoutePlanEntity plan, Date serverNow) {
        if (plan == null) {
            return;
        }
        String routeDate = plan.getNxDrpRouteDate() != null ? plan.getNxDrpRouteDate() : plan.getNxDrpPlanDate();
        plan.setRouteDateLabel(formatRouteDateLabel(routeDate, serverNow));
        String temporalStatus = resolvePlanTemporalStatus(
                routeDate, plan.getNxDrpBatchStartAt(), plan.getNxDrpBatchEndAt(), serverNow);
        plan.setPlanTemporalStatus(temporalStatus);
        plan.setPlanTemporalStatusLabel(DisRouteDispatchLabels.label(temporalStatus));
        if (plan.getNxDrpDispatchBatchLabel() == null) {
            plan.setNxDrpDispatchBatchLabel(DisRouteDispatchLabels.label(plan.getNxDrpDispatchBatch()));
        }
    }

    public static void enrichStopTemporalFields(NxDisRouteStopEntity stop, Date serverNow) {
        if (stop == null) {
            return;
        }
        stop.setPlannedArrivalLabel(formatDateTimeLabel(stop.getNxDrsPlannedArrivalAt(), serverNow));
        String status = resolveStopTemporalStatus(stop.getNxDrsPlannedArrivalAt(), serverNow);
        stop.setStopTemporalStatus(status);
        stop.setStopTemporalStatusLabel(DisRouteDispatchLabels.label(status));
    }

    public static void enrichDriverRouteOperationFields(NxDisDriverRouteEntity route) {
        enrichDriverRouteOperationFields(route, null);
    }

    public static void enrichDriverRouteOperationFields(NxDisDriverRouteEntity route,
                                                        NxDisRoutePlanEntity plan) {
        if (route == null) {
            return;
        }
        int stopCount = route.getNxDdrStopCount() != null ? route.getNxDdrStopCount() : 0;
        int suggested = 0;
        int assigned = 0;
        int locked = 0;
        if (route.getStops() != null) {
            for (NxDisRouteStopEntity stop : route.getStops()) {
                NxDisShipmentTaskEntity task = stop.getShipmentTask();
                if (task == null) {
                    continue;
                }
                if (task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1) {
                    locked++;
                }
                String taskStatus = task.getNxDstStatus();
                if (DisShipmentTaskStatus.ASSIGNED.equals(taskStatus)
                        || DisShipmentTaskStatus.READY_TO_GO.equals(taskStatus)) {
                    assigned++;
                } else if (DisShipmentTaskStatus.SIMULATED.equals(taskStatus)) {
                    suggested++;
                }
            }
        }
        route.setSuggestedStopCount(suggested);
        route.setAssignedStopCount(assigned);
        route.setLockedStopCount(locked);
        route.setRouteOperationStatusLabel(buildRouteOperationStatusLabel(route, stopCount, plan));
    }

    private static String buildRouteOperationStatusLabel(NxDisDriverRouteEntity route,
                                                         int stopCount,
                                                         NxDisRoutePlanEntity plan) {
        int effectiveStopCount = stopCount;
        if (effectiveStopCount <= 0 && route != null) {
            effectiveStopCount = DisRouteExecutionRouteSnapshotHelper.resolveEffectiveStopCount(route);
        }
        if (route != null && DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
            String routeStatus = DisRouteRouteExecutionHelper.resolveRouteStatus(route);
            if (DisRouteDriverRouteStatus.IN_DELIVERY.equals(routeStatus)) {
                return effectiveStopCount > 0
                        ? "配送中 · " + effectiveStopCount + " 个站点"
                        : "配送中";
            }
            if (DisRouteDriverRouteStatus.DELIVERED.equals(routeStatus)) {
                return effectiveStopCount > 0
                        ? "已完成 · " + effectiveStopCount + " 个站点"
                        : "已完成配送";
            }
        }
        if (effectiveStopCount <= 0) {
            return "等待沙盘分配";
        }
        boolean ineligible = route.getNxDdrDispatchEligible() != null && route.getNxDdrDispatchEligible() == 0;
        if (ineligible && route != null && DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
            return effectiveStopCount > 0
                    ? "配送中 · " + effectiveStopCount + " 个站点"
                    : "配送中";
        }
        if (ineligible) {
            return "已有站点待处理 · 当前不可执行";
        }
        if (plan != null && DisRoutePlanTemporalStatus.EXPIRED.equals(plan.getPlanTemporalStatus())) {
            return "已有 " + effectiveStopCount + " 个站点方案 · 当前计划已过期";
        }
        return "已有 " + effectiveStopCount + " 个站点路线建议";
    }
}
