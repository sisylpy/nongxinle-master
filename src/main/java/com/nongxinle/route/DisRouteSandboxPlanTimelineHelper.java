package com.nongxinle.route;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;

import java.util.Date;
import java.util.List;

/**
 * Phase 3a：沙盘计划时间轴 — 由各路线的排程预览聚合，不用批次硬编码窗口。
 */
public final class DisRouteSandboxPlanTimelineHelper {

    private DisRouteSandboxPlanTimelineHelper() {
    }

    /**
     * 从已排程的 driverRoutes 聚合计划级时间窗口与晚点/等待汇总。
     * 有站点时：batchStartAt = 最早出发，batchEndAt = 最晚回来（末站离开时刻）；
     * 无站点时：清空计划时间与批次窗口，避免展示过期的默认早班时段。
     */
    public static void applyAggregatedTimeline(NxDisRoutePlanEntity plan) {
        if (plan == null) {
            return;
        }
        Date planStartAt = null;
        Date planEndAt = null;
        int planTotalWait = 0;
        int planTotalLate = 0;
        boolean planHasLate = false;
        boolean planAllNoWindow = false;
        boolean hasScheduledRoute = false;

        List<NxDisDriverRouteEntity> driverRoutes = plan.getDriverRoutes();
        if (driverRoutes != null && !driverRoutes.isEmpty()) {
            planAllNoWindow = true;
            for (NxDisDriverRouteEntity route : driverRoutes) {
                int stopCount = route.getNxDdrStopCount() != null ? route.getNxDdrStopCount() : 0;
                if (stopCount <= 0) {
                    continue;
                }
                hasScheduledRoute = true;
                if (route.getNxDdrPlannedDepartAt() != null) {
                    planStartAt = minDate(planStartAt, route.getNxDdrPlannedDepartAt());
                }
                if (route.getNxDdrPlannedFinishAt() != null) {
                    planEndAt = maxDate(planEndAt, route.getNxDdrPlannedFinishAt());
                }
                planTotalWait += route.getNxDdrTotalWaitMinutes() != null ? route.getNxDdrTotalWaitMinutes() : 0;
                planTotalLate += route.getNxDdrTotalLateMinutes() != null ? route.getNxDdrTotalLateMinutes() : 0;
                if (DisRouteScheduleStatus.HAS_LATE.equals(route.getNxDdrScheduleStatus())) {
                    planHasLate = true;
                }
                if (!DisRouteScheduleStatus.NO_WINDOW.equals(route.getNxDdrScheduleStatus())) {
                    planAllNoWindow = false;
                }
            }
        }

        if (!hasScheduledRoute) {
            plan.setNxDrpPlannedStartAt(null);
            plan.setNxDrpPlannedEndAt(null);
            plan.setNxDrpBatchStartAt(null);
            plan.setNxDrpBatchEndAt(null);
            plan.setNxDrpDefaultDepartAt(null);
            plan.setNxDrpTotalWaitMinutes(0);
            plan.setNxDrpTotalLateMinutes(0);
            plan.setNxDrpTotalDistanceM(0L);
            plan.setNxDrpTotalDurationS(0L);
            plan.setNxDrpScheduleStatus(DisRouteScheduleStatus.OK);
            return;
        }

        long planTotalDistanceM = 0L;
        long planTotalDurationS = 0L;
        for (NxDisDriverRouteEntity route : driverRoutes) {
            if (route.getNxDdrTotalDistanceM() != null) {
                planTotalDistanceM += route.getNxDdrTotalDistanceM();
            }
            if (route.getNxDdrTotalDurationS() != null) {
                planTotalDurationS += route.getNxDdrTotalDurationS();
            }
        }
        plan.setNxDrpTotalDistanceM(planTotalDistanceM);
        plan.setNxDrpTotalDurationS(planTotalDurationS);

        plan.setNxDrpPlannedStartAt(planStartAt);
        plan.setNxDrpPlannedEndAt(planEndAt);
        plan.setNxDrpBatchStartAt(planStartAt);
        plan.setNxDrpBatchEndAt(planEndAt);
        plan.setNxDrpDefaultDepartAt(planStartAt);
        plan.setNxDrpTotalWaitMinutes(planTotalWait);
        plan.setNxDrpTotalLateMinutes(planTotalLate);
        if (planHasLate) {
            plan.setNxDrpScheduleStatus(DisRouteScheduleStatus.HAS_LATE);
        } else if (planAllNoWindow) {
            plan.setNxDrpScheduleStatus(DisRouteScheduleStatus.NO_WINDOW);
        } else {
            plan.setNxDrpScheduleStatus(DisRouteScheduleStatus.OK);
        }
    }

    public static boolean hasScheduledStops(NxDisRoutePlanEntity plan) {
        if (plan == null || plan.getDriverRoutes() == null) {
            return false;
        }
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            int stopCount = route.getNxDdrStopCount() != null ? route.getNxDdrStopCount() : 0;
            if (stopCount > 0) {
                return true;
            }
        }
        return false;
    }

    private static Date minDate(Date current, Date candidate) {
        if (candidate == null) {
            return current;
        }
        if (current == null || candidate.before(current)) {
            return candidate;
        }
        return current;
    }

    private static Date maxDate(Date current, Date candidate) {
        if (candidate == null) {
            return current;
        }
        if (current == null || candidate.after(current)) {
            return candidate;
        }
        return current;
    }
}
