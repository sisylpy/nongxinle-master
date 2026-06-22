package com.nongxinle.route;

import com.nongxinle.entity.NxDepartmentOrderHistoryEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;

/**
 * 与 {@code NxDisRoutePlanDao.queryEligibleLiveOrderSnapshots} 一致的 eligible 口径。
 */
public final class DisRouteEligibleOrderPolicy {

    private static final int ELIGIBLE_STATUS_EXCLUSIVE_MAX = 3;

    private DisRouteEligibleOrderPolicy() {
    }

    public static boolean isLiveOrderEligible(NxDepartmentOrdersEntity order,
                                              Integer disId,
                                              String routeDate,
                                              Integer expectedDepFatherId) {
        return validateLiveOrder(order, disId, routeDate, expectedDepFatherId) == null;
    }

    public static String validateLiveOrder(NxDepartmentOrdersEntity order,
                                           Integer disId,
                                           String routeDate,
                                           Integer expectedDepFatherId) {
        if (order == null) {
            return "LIVE_ORDER_NOT_FOUND";
        }
        if (disId != null && order.getNxDoDistributerId() != null
                && !disId.equals(order.getNxDoDistributerId())) {
            return "ORDER_DIS_MISMATCH";
        }
        if (expectedDepFatherId != null && order.getNxDoDepartmentFatherId() != null
                && !expectedDepFatherId.equals(order.getNxDoDepartmentFatherId())) {
            return "ORDER_DEP_MISMATCH";
        }
        if (order.getNxDoStatus() != null && order.getNxDoStatus() >= ELIGIBLE_STATUS_EXCLUSIVE_MAX) {
            return "ORDER_STATUS_INELIGIBLE";
        }
        if (order.getNxDoGbDepartmentId() != null && order.getNxDoGbDepartmentId() != -1) {
            return "ORDER_GB_NOT_ELIGIBLE";
        }
        if (order.getNxDoNxCommRestrauntId() != null && order.getNxDoNxCommRestrauntId() != -1) {
            return "ORDER_COMM_NOT_ELIGIBLE";
        }
        if (order.getNxDoCollaborativeNxDisId() != null && order.getNxDoCollaborativeNxDisId() != -1) {
            return "ORDER_NOT_SELF";
        }
        if (!matchesRouteDate(routeDate, order.getNxDoArriveDate(), order.getNxDoArriveOnlyDate())) {
            return "ORDER_ROUTE_DATE_MISMATCH";
        }
        return null;
    }

    public static String validateHistoryOrder(NxDepartmentOrderHistoryEntity order,
                                              Integer disId,
                                              String routeDate,
                                              Integer expectedDepFatherId) {
        if (order == null) {
            return "HISTORY_ORDER_NOT_FOUND";
        }
        if (disId != null && order.getNxDoDistributerId() != null
                && !disId.equals(order.getNxDoDistributerId())) {
            return "ORDER_DIS_MISMATCH";
        }
        if (expectedDepFatherId != null && order.getNxDoDepartmentFatherId() != null
                && !expectedDepFatherId.equals(order.getNxDoDepartmentFatherId())) {
            return "ORDER_DEP_MISMATCH";
        }
        if (order.getNxDoGbDepartmentId() != null && order.getNxDoGbDepartmentId() != -1) {
            return "ORDER_GB_NOT_ELIGIBLE";
        }
        if (order.getNxDoNxCommRestrauntId() != null && order.getNxDoNxCommRestrauntId() != -1) {
            return "ORDER_COMM_NOT_ELIGIBLE";
        }
        if (order.getNxDoCollaborativeNxDisId() != null && order.getNxDoCollaborativeNxDisId() != -1) {
            return "ORDER_NOT_SELF";
        }
        if (!matchesRouteDate(routeDate, order.getNxDoArriveDate(), order.getNxDoArriveOnlyDate())) {
            return "ORDER_ROUTE_DATE_MISMATCH";
        }
        return null;
    }

    static boolean matchesRouteDate(String routeDate, String arriveDate, String arriveOnlyDate) {
        if (routeDate == null || routeDate.trim().isEmpty()) {
            return true;
        }
        if (arriveDate != null && routeDate.equals(arriveDate.trim())) {
            return true;
        }
        String routeDateOnly = DisRouteOrderArriveDateHelper.toRouteDateOnly(routeDate);
        if (routeDateOnly != null && arriveOnlyDate != null && routeDateOnly.equals(arriveOnlyDate.trim())) {
            return true;
        }
        return false;
    }
}
