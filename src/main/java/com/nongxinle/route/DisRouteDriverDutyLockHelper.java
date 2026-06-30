package com.nongxinle.route;

import com.nongxinle.entity.NxDisDriverRouteEntity;

/**
 * 司机可派状态页：装车 / 出发 / 配送执行中不可关闭可派。
 */
public final class DisRouteDriverDutyLockHelper {

    public static final String ACTION_TYPE_TOGGLE_DUTY_OFF = "TOGGLE_DUTY_OFF";

    private DisRouteDriverDutyLockHelper() {
    }

    public static boolean isRouteLockedForDutyToggle(NxDisDriverRouteEntity route) {
        if (route == null) {
            return false;
        }
        if (DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
            return true;
        }
        if (route.getNxDdrActualDepartAt() != null) {
            return true;
        }
        if (DisRouteLoadingGateHelper.isRouteEnteredLoading(route)) {
            return true;
        }
        String status = route.getNxDdrRouteStatus();
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        return DisRouteDriverRouteStatus.IN_DELIVERY.equals(normalized)
                || DisRouteDriverRouteStatus.DELIVERED.equals(normalized)
                || DisRouteDriverRouteStatus.COMPLETED.equals(normalized)
                || DisRouteDriverRouteStatus.READY_TO_DEPART.equals(normalized);
    }
}
