package com.nongxinle.route;

import com.nongxinle.dao.NxDisDriverDutyDao;
import com.nongxinle.entity.NxDisDriverDutyEntity;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;

import java.util.Date;

import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;

/**
 * 读模型/assess 前清洗：以 duty 表为准，清除 DRIVER_TOO_LATE / DRIVER_CHECKIN_TOO_LATE 等旧语义。
 */
public final class DisRouteDriverDutyOverlayHelper {

    private DisRouteDriverDutyOverlayHelper() {
    }

    public static void overlayOnRoute(NxDisDriverRouteEntity route,
                                      NxDisRoutePlanEntity plan,
                                      NxDisDriverDutyDao dutyDao) {
        overlayOnRoute(route, plan, dutyDao, null);
    }

    public static void overlayOnRoute(NxDisDriverRouteEntity route,
                                      NxDisRoutePlanEntity plan,
                                      NxDisDriverDutyDao dutyDao,
                                      Date serverNow) {
        if (route == null) {
            return;
        }
        sanitizeLegacyFields(route);

        int stopCount = route.getNxDdrStopCount() != null ? route.getNxDdrStopCount() : 0;
        boolean onDuty = resolveOnDuty(route, plan, dutyDao);

        if (onDuty) {
            route.setNxDdrDispatchEligible(1);
            route.setNxDdrIneligibleReason(null);
            route.setNxDdrFeasibilityStatus(deriveScheduleFeasibility(route, stopCount));
        } else {
            route.setNxDdrDispatchEligible(0);
            route.setNxDdrIneligibleReason(DisRouteBatchEligibility.INELIGIBLE_OFF_DUTY);
            route.setNxDdrFeasibilityStatus(stopCount > 0
                    ? DisRouteFeasibilityStatus.INFEASIBLE : DisRouteFeasibilityStatus.IDLE);
        }

        route.setNxDdrFeasibilityStatusLabel(labelDriverRouteFeasibility(route, plan));
        route.setNxDdrIneligibleReasonLabel(labelDriverRouteIneligibleReason(route));
    }

    /** 清除 DB 中 assess 旧版本遗留的 check-in / driver-too-late 字段。 */
    public static void sanitizeLegacyFields(NxDisDriverRouteEntity route) {
        if (route == null) {
            return;
        }
        if (DisRouteBatchEligibility.INELIGIBLE_CHECKIN_TOO_LATE.equals(route.getNxDdrIneligibleReason())) {
            route.setNxDdrIneligibleReason(null);
        }
        if (DisRouteFeasibilityStatus.DRIVER_TOO_LATE.equals(route.getNxDdrFeasibilityStatus())) {
            route.setNxDdrFeasibilityStatus(null);
        }
    }

    public static String labelDriverRouteFeasibility(NxDisDriverRouteEntity route, NxDisRoutePlanEntity plan) {
        if (route == null) {
            return null;
        }
        if (DisRouteBatchEligibility.INELIGIBLE_OFF_DUTY.equals(route.getNxDdrIneligibleReason())
                || (route.getNxDdrDispatchEligible() != null && route.getNxDdrDispatchEligible() == 0
                && route.getNxDdrStopCount() != null && route.getNxDdrStopCount() > 0)) {
            return "当前不可执行";
        }
        if (plan != null && DisRoutePlanTemporalStatus.EXPIRED.equals(plan.getPlanTemporalStatus())
                && route.getNxDdrStopCount() != null && route.getNxDdrStopCount() > 0
                && ON_DUTY.equals(resolveDutyStatus(route, plan))) {
            return "当前计划已过期";
        }
        String status = route.getNxDdrFeasibilityStatus();
        if (DisRouteFeasibilityStatus.DRIVER_TOO_LATE.equals(status)) {
            return "当前不可执行";
        }
        return DisRouteDispatchLabels.label(status);
    }

    public static String labelDriverRouteIneligibleReason(NxDisDriverRouteEntity route) {
        if (route == null || route.getNxDdrIneligibleReason() == null) {
            return null;
        }
        if (DisRouteBatchEligibility.INELIGIBLE_CHECKIN_TOO_LATE.equals(route.getNxDdrIneligibleReason())) {
            return null;
        }
        return DisRouteDispatchLabels.label(route.getNxDdrIneligibleReason());
    }

    private static boolean resolveOnDuty(NxDisDriverRouteEntity route,
                                         NxDisRoutePlanEntity plan,
                                         NxDisDriverDutyDao dutyDao) {
        if (plan == null || dutyDao == null || route.getNxDdrDriverUserId() == null) {
            return route.getNxDdrDispatchEligible() != null && route.getNxDdrDispatchEligible() == 1;
        }
        NxDisDriverDutyEntity duty = dutyDao.queryByDisDriverDate(
                plan.getNxDrpDistributerId(), route.getNxDdrDriverUserId(), resolveRouteDate(plan));
        return duty != null && ON_DUTY.equals(duty.getNxDddDutyStatus());
    }

    private static String resolveDutyStatus(NxDisDriverRouteEntity route, NxDisRoutePlanEntity plan) {
        if (route.getNxDdrDispatchEligible() != null && route.getNxDdrDispatchEligible() == 1) {
            return ON_DUTY;
        }
        return DisDriverDutyStatus.OFF_DUTY;
    }

    private static String deriveScheduleFeasibility(NxDisDriverRouteEntity route, int stopCount) {
        if (stopCount == 0) {
            return DisRouteFeasibilityStatus.IDLE;
        }
        if (route.getNxDdrTotalLateMinutes() != null && route.getNxDdrTotalLateMinutes() > 0) {
            return DisRouteFeasibilityStatus.HAS_LATE;
        }
        if (route.getNxDdrTotalWaitMinutes() != null && route.getNxDdrTotalWaitMinutes() > 0) {
            return DisRouteFeasibilityStatus.HAS_WAIT;
        }
        return DisRouteFeasibilityStatus.FEASIBLE;
    }

    private static String resolveRouteDate(NxDisRoutePlanEntity plan) {
        if (plan.getNxDrpRouteDate() != null && !plan.getNxDrpRouteDate().trim().isEmpty()) {
            return plan.getNxDrpRouteDate().trim();
        }
        if (plan.getNxDrpPlanDate() != null && !plan.getNxDrpPlanDate().trim().isEmpty()) {
            return plan.getNxDrpPlanDate().trim();
        }
        throw new IllegalArgumentException("路线计划缺少 routeDate");
    }
}
