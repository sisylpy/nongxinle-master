package com.nongxinle.route;

import com.nongxinle.dto.route.DriverDispatchCandidateDto;
import com.nongxinle.entity.NxDisDriverRouteEntity;

/** 人工调度：司机准入（前端不得自行判断）。 */
public final class DisRouteSandboxManualDispatchEligibilityHelper {

    private DisRouteSandboxManualDispatchEligibilityHelper() {
    }

    public static boolean canReceiveManualDispatch(DriverDispatchCandidateDto driver,
                                                   NxDisDriverRouteEntity route) {
        if (driver == null || driver.getDriverUserId() == null) {
            return false;
        }
        if (!Boolean.TRUE.equals(driver.getBatchEligible())) {
            return false;
        }
        if (route == null) {
            return true;
        }
        if (DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
            return false;
        }
        if (DisRouteDriverDepartPolicy.isRouteDeparted(route)) {
            return false;
        }
        if (DisRouteLoadingGateHelper.isRouteEnteredLoading(route)) {
            return false;
        }
        return true;
    }

    public static String resolveBlockedReason(DriverDispatchCandidateDto driver,
                                              NxDisDriverRouteEntity route) {
        if (driver == null) {
            return "未找到司机";
        }
        if (!Boolean.TRUE.equals(driver.getBatchEligible())) {
            if (driver.getIneligibleReasonLabel() != null && !driver.getIneligibleReasonLabel().trim().isEmpty()) {
                return driver.getIneligibleReasonLabel().trim();
            }
            return "司机当前不可派";
        }
        if (route != null && DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
            return "司机配送执行中，不能追加";
        }
        if (route != null && DisRouteDriverDepartPolicy.isRouteDeparted(route)) {
            return "司机已出发，不能追加";
        }
        if (route != null && DisRouteLoadingGateHelper.isRouteEnteredLoading(route)) {
            return "司机已进入装车，暂不支持追加";
        }
        return "暂不可选";
    }
}
