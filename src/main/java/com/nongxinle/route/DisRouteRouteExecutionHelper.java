package com.nongxinle.route;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import static com.nongxinle.route.DisShipmentTaskStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.CLOSED;
import static com.nongxinle.route.DisShipmentTaskStatus.DELIVERED;
import static com.nongxinle.route.DisShipmentTaskStatus.EXCEPTION;
import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;

/**
 * Phase 3D：司机路线配送执行态 — 出发后退出沙盘，DB + stop/task 状态为读模型主权。
 * <p>
 * 沙盘优化/改派不得覆盖已出发路线；路线里程/排程展示 enrichment 见 {@link DisRouteExecutionRouteSnapshotHelper}。
 */
public final class DisRouteRouteExecutionHelper {

    private DisRouteRouteExecutionHelper() {
    }

    /** 已出发 / 配送中 / 已完成 — 不再参与沙盘分配与路线重算。 */
    public static boolean isExecutionRoute(NxDisDriverRouteEntity route) {
        if (route == null) {
            return false;
        }
        if (route.getNxDdrActualDepartAt() != null || route.getDepartedAt() != null) {
            return true;
        }
        if (hasExecutionTaskStop(route)) {
            return true;
        }
        String stored = normalizeRouteStatus(route.getNxDdrRouteStatus());
        if (DisRouteDriverRouteStatus.IN_DELIVERY.equals(stored)
                || DisRouteDriverRouteStatus.DELIVERED.equals(stored)
                || DisRouteDriverRouteStatus.COMPLETED.equals(stored)
                || DisRouteDriverRouteStatus.CLOSED.equals(stored)) {
            return true;
        }
        String derived = deriveRouteStatusFromStops(route);
        return DisRouteDriverRouteStatus.IN_DELIVERY.equals(derived)
                || DisRouteDriverRouteStatus.DELIVERED.equals(derived)
                || DisRouteDriverRouteStatus.COMPLETED.equals(derived);
    }

    /** 仍可参与沙盘规划（未出发）。 */
    public static boolean isSandboxPlanningRoute(NxDisDriverRouteEntity route) {
        return !isExecutionRoute(route);
    }

    /**
     * 路线读模型 status 主权链：
     * 1. DB nxDdrRouteStatus（IN_DELIVERY/DELIVERED/CLOSED）
     * 2. actualDepartAt / departedAt → IN_DELIVERY
     * 3. stop/task IN_DELIVERY → IN_DELIVERY
     * 4. stop/task 全 DELIVERED → DELIVERED
     * 5. 未出发且全 confirmed → READY_TO_DEPART
     * 6. 否则 LOADING / IDLE
     */
    public static String resolveRouteStatus(NxDisDriverRouteEntity route) {
        if (route == null) {
            return DisRouteDriverRouteStatus.IDLE;
        }
        String stored = normalizeRouteStatus(route.getNxDdrRouteStatus());
        if (DisRouteDriverRouteStatus.IN_DELIVERY.equals(stored)
                || DisRouteDriverRouteStatus.DELIVERED.equals(stored)
                || DisRouteDriverRouteStatus.COMPLETED.equals(stored)
                || DisRouteDriverRouteStatus.CLOSED.equals(stored)) {
            return stored;
        }
        if (route.getNxDdrActualDepartAt() != null || route.getDepartedAt() != null) {
            return DisRouteDriverRouteStatus.IN_DELIVERY;
        }
        String stopDerived = deriveRouteStatusFromStops(route);
        if (stopDerived != null) {
            return stopDerived;
        }

        int totalStopCount = countActiveStops(route);
        if (route.getNxDdrId() == null && totalStopCount <= 0) {
            return DisRouteDriverRouteStatus.IDLE;
        }
        if (route.getNxDdrId() != null && totalStopCount <= 0) {
            return DisRouteDriverRouteStatus.IDLE;
        }
        if (route.getNxDdrId() != null && totalStopCount > 0 && isAllConfirmedForDepart(route)) {
            return DisRouteDriverRouteStatus.READY_TO_DEPART;
        }
        return DisRouteDriverRouteStatus.LOADING;
    }

    public static boolean isRouteDeparted(NxDisDriverRouteEntity route) {
        return DisRouteDriverRouteStatus.isDeparted(resolveRouteStatus(route));
    }

    /** 路线级 label：COMPLETED/DELIVERED→已完成，IN_DELIVERY→配送中（与 stop 已送达区分） */
    public static String resolveRouteStatusLabel(String routeStatus) {
        if (routeStatus == null || routeStatus.trim().isEmpty()) {
            return null;
        }
        String normalized = routeStatus.trim().toUpperCase();
        if (DisRouteDriverRouteStatus.COMPLETED.equals(normalized)
                || DisRouteDriverRouteStatus.DELIVERED.equals(normalized)
                || DisRouteDriverRouteStatus.CLOSED.equals(normalized)) {
            return DisRouteDispatchLabels.label(DisRouteDriverRouteStatus.COMPLETED);
        }
        return DisRouteDispatchLabels.label(normalized);
    }

    public static void syncExecutionCanonicalFields(NxDisDriverRouteEntity route) {
        if (route == null) {
            return;
        }
        String status = resolveRouteStatus(route);
        route.setRouteStatus(status);
        route.setRouteStatusLabel(resolveRouteStatusLabel(status));
        if (route.getNxDdrRouteStatus() == null || route.getNxDdrRouteStatus().trim().isEmpty()) {
            route.setNxDdrRouteStatus(status);
        }
        if (route.getNxDdrActualDepartAt() != null) {
            route.setDepartedAt(route.getNxDdrActualDepartAt());
        }
        if (isExecutionRoute(route)) {
            route.setNxDdrDispatchEligible(0);
            route.setCanDepart(false);
            route.setDepartBlockedReason("该司机路线已在配送中或已完成");
        }
    }

    public static void mergeExecutionFieldsFromDb(NxDisDriverRouteEntity target, NxDisDriverRouteEntity dbRoute) {
        if (target == null || dbRoute == null) {
            return;
        }
        if (dbRoute.getNxDdrRouteStatus() != null && !dbRoute.getNxDdrRouteStatus().trim().isEmpty()) {
            target.setNxDdrRouteStatus(dbRoute.getNxDdrRouteStatus());
        }
        if (dbRoute.getNxDdrActualDepartAt() != null) {
            target.setNxDdrActualDepartAt(dbRoute.getNxDdrActualDepartAt());
            target.setDepartedAt(dbRoute.getNxDdrActualDepartAt());
        }
        if (dbRoute.getNxDdrDepartOperatorUserId() != null) {
            target.setNxDdrDepartOperatorUserId(dbRoute.getNxDdrDepartOperatorUserId());
        }
        if (dbRoute.getNxDdrDepartRemark() != null) {
            target.setNxDdrDepartRemark(dbRoute.getNxDdrDepartRemark());
        }
        if (dbRoute.getNxDdrDispatchEligible() != null) {
            target.setNxDdrDispatchEligible(dbRoute.getNxDdrDispatchEligible());
        }
        if (dbRoute.getNxDdrPlanId() != null && target.getNxDdrPlanId() == null) {
            target.setNxDdrPlanId(dbRoute.getNxDdrPlanId());
        }
    }

    private static String deriveRouteStatusFromStops(NxDisDriverRouteEntity route) {
        if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
            return null;
        }
        boolean hasActiveStop = false;
        boolean anyInDelivery = false;
        boolean anyException = false;
        boolean allDelivered = true;
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop == null || isCancelledStop(stop)) {
                continue;
            }
            hasActiveStop = true;
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task == null) {
                allDelivered = false;
                continue;
            }
            String status = task.getNxDstStatus();
            if (IN_DELIVERY.equals(status)) {
                anyInDelivery = true;
                allDelivered = false;
            } else if (EXCEPTION.equals(status)) {
                anyException = true;
                allDelivered = false;
            } else if (!DELIVERED.equals(status) && !CLOSED.equals(status)) {
                allDelivered = false;
            }
        }
        if (!hasActiveStop) {
            return null;
        }
        if (allDelivered) {
            return DisRouteDriverRouteStatus.COMPLETED;
        }
        if (anyInDelivery || anyException) {
            return DisRouteDriverRouteStatus.IN_DELIVERY;
        }
        return null;
    }

    /** 读模型：路线下活跃 stop 是否全部已送达 */
    public static boolean areAllStopsDelivered(NxDisDriverRouteEntity route) {
        if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
            return false;
        }
        boolean hasActiveStop = false;
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop == null || isCancelledStop(stop)) {
                continue;
            }
            hasActiveStop = true;
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task == null || !DELIVERED.equals(task.getNxDstStatus())) {
                return false;
            }
        }
        return hasActiveStop;
    }

    /** 读模型：是否仍有配送中/异常 stop（路线不可自动完成） */
    public static boolean hasPendingExecutionStops(NxDisDriverRouteEntity route) {
        if (route == null || route.getStops() == null) {
            return false;
        }
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop == null || isCancelledStop(stop)) {
                continue;
            }
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task == null) {
                continue;
            }
            String status = task.getNxDstStatus();
            if (IN_DELIVERY.equals(status) || EXCEPTION.equals(status)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasExecutionTaskStop(NxDisDriverRouteEntity route) {
        if (route == null || route.getStops() == null) {
            return false;
        }
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop == null || isCancelledStop(stop)) {
                continue;
            }
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task == null) {
                continue;
            }
            String status = task.getNxDstStatus();
            if (IN_DELIVERY.equals(status) || DELIVERED.equals(status) || EXCEPTION.equals(status)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAllConfirmedForDepart(NxDisDriverRouteEntity route) {
        if (route == null || route.getStops() == null) {
            return false;
        }
        int total = 0;
        int confirmed = 0;
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop == null || isCancelledStop(stop)) {
                continue;
            }
            total++;
            if (DisRouteSandboxStopSource.CONFIRMED.equals(stop.getStopSource())) {
                confirmed++;
                continue;
            }
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task != null && task.getNxDstId() != null
                    && (DisRouteSandboxStopSource.CONFIRMED.equals(task.getStopSource())
                    || task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1)) {
                confirmed++;
            }
        }
        return total > 0 && confirmed == total;
    }

    private static int countActiveStops(NxDisDriverRouteEntity route) {
        if (route == null || route.getStops() == null) {
            return 0;
        }
        int count = 0;
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop != null && !isCancelledStop(stop)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isCancelledStop(NxDisRouteStopEntity stop) {
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && CANCELLED.equals(task.getNxDstStatus())) {
            return true;
        }
        String stopStatus = stop.getNxDrsStopStatus();
        return stopStatus != null && CANCELLED.equalsIgnoreCase(stopStatus);
    }

    private static String normalizeRouteStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        return status.trim().toUpperCase();
    }
}
