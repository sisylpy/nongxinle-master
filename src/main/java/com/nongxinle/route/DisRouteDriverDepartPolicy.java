package com.nongxinle.route;

import com.nongxinle.dto.route.RouteDispatchOperationDecision;
import com.nongxinle.dto.route.RouteFeasibilityResult;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.List;

import static com.nongxinle.route.DisRouteBillPrintStatusHelper.summarize;
import static com.nongxinle.route.DisShipmentTaskStatus.ASSIGNED;
import static com.nongxinle.route.DisShipmentTaskStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.CLOSED;
import static com.nongxinle.route.DisShipmentTaskStatus.DELIVERED;
import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;
import static com.nongxinle.route.DisShipmentTaskStatus.READY_TO_GO;

/**
 * Phase 3D：司机路线确认出发 — route 级动作，按 route canonical stops 判断；配送单未打印仅 warning。
 */
public final class DisRouteDriverDepartPolicy {

    public static final String ACTION_LABEL = "确认司机出发";

    private DisRouteDriverDepartPolicy() {
    }

    public static RouteStopCounts countRouteStops(NxDisDriverRouteEntity route) {
        RouteStopCounts counts = new RouteStopCounts();
        if (route == null || route.getStops() == null) {
            return counts;
        }
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop == null || isCancelledStop(stop)) {
                continue;
            }
            counts.totalStopCount++;
            if (isConfirmedStop(stop)) {
                counts.confirmedStopCount++;
            } else {
                counts.unconfirmedStopCount++;
            }
        }
        return counts;
    }

    public static int countUnprintedBillsOnRoute(NxDisDriverRouteEntity route) {
        int total = 0;
        if (route == null || route.getStops() == null) {
            return total;
        }
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop == null || stop.getShipmentTask() == null) {
                continue;
            }
            total += summarize(stop.getShipmentTask()).unprintedCount;
        }
        return total;
    }

    /**
     * 按 route canonical 字段判断是否可出发。
     * requireLoadBeforeDepart=true 时要求 routeTasks 全部为 READY_TO_GO；
     * false 时 ASSIGNED 即可（出库确认后）。
     */
    public static RouteDispatchOperationDecision evaluateDepart(NxDisDriverRouteEntity route,
                                                                NxDisRoutePlanEntity plan,
                                                                List<NxDisShipmentTaskEntity> routeTasks,
                                                                boolean driverOnDuty,
                                                                RouteFeasibilityResult feasibility) {
        return evaluateDepart(route, plan, routeTasks, driverOnDuty, feasibility, false);
    }

    public static RouteDispatchOperationDecision evaluateDepart(NxDisDriverRouteEntity route,
                                                                NxDisRoutePlanEntity plan,
                                                                List<NxDisShipmentTaskEntity> routeTasks,
                                                                boolean driverOnDuty,
                                                                RouteFeasibilityResult feasibility,
                                                                boolean requireLoadBeforeDepart) {
        if (route == null || route.getNxDdrId() == null) {
            return RouteDispatchOperationDecision.deny("司机路线不存在");
        }
        String routeStatus = resolveRouteStatus(route);
        if (DisRouteDriverRouteStatus.IN_DELIVERY.equals(routeStatus)) {
            return RouteDispatchOperationDecision.deny("该司机路线已在配送中");
        }
        if (DisRouteDriverRouteStatus.DELIVERED.equals(routeStatus)) {
            return RouteDispatchOperationDecision.deny("该司机路线已完成配送");
        }
        if (DisRouteDriverRouteStatus.CLOSED.equals(routeStatus)) {
            return RouteDispatchOperationDecision.deny("该司机路线已关闭");
        }
        if (!driverOnDuty) {
            return RouteDispatchOperationDecision.deny("司机当前不可派，不能确认出发");
        }

        RouteStopCounts counts = countRouteStops(route);
        if (counts.totalStopCount <= 0) {
            return RouteDispatchOperationDecision.deny("该司机路线还没有可出发的门店");
        }
        if (counts.confirmedStopCount <= 0) {
            return RouteDispatchOperationDecision.deny("该司机路线还没有已确认出货的门店");
        }
        if (counts.unconfirmedStopCount > 0) {
            return RouteDispatchOperationDecision.deny("请先确认全部门店出货完成");
        }
        if (counts.confirmedStopCount != counts.totalStopCount) {
            return RouteDispatchOperationDecision.deny("请先确认全部门店出货完成");
        }

        String stopBlockReason = validateConfirmedStopsForDepart(route, requireLoadBeforeDepart);
        if (stopBlockReason != null) {
            return RouteDispatchOperationDecision.deny(stopBlockReason);
        }

        if (requireLoadBeforeDepart) {
            String loadBlockReason = validateAllTasksReadyToGo(routeTasks);
            if (loadBlockReason != null) {
                return RouteDispatchOperationDecision.deny(loadBlockReason);
            }
        }

        if (feasibility != null
                && DisRouteFeasibilityStatus.INFEASIBLE.equals(feasibility.getFeasibilityStatus())) {
            return RouteDispatchOperationDecision.deny("当前批次不可执行，请先处理异常后再出发");
        }

        RouteDispatchOperationDecision ok = RouteDispatchOperationDecision.allow();
        ok.setOperationHint(ACTION_LABEL);
        return ok;
    }

    public static String buildDepartWarning(NxDisDriverRouteEntity route) {
        int unprinted = countUnprintedBillsOnRoute(route);
        if (unprinted > 0) {
            return "还有 " + unprinted + " 个配送单未打印，可继续出发。";
        }
        return null;
    }

    public static String buildDepartConfirmMessage(NxDisDriverRouteEntity route) {
        String base = "出发后，该司机路线将进入配送中，门店不能再返回沙盘，也不能再改派。";
        String warning = buildDepartWarning(route);
        if (warning != null && !warning.isEmpty()) {
            return base + warning;
        }
        return base;
    }

    public static String resolveRouteStatus(NxDisDriverRouteEntity route) {
        return DisRouteRouteExecutionHelper.resolveRouteStatus(route);
    }

    public static boolean isRouteDeparted(NxDisDriverRouteEntity route) {
        return DisRouteRouteExecutionHelper.isRouteDeparted(route);
    }

    private static String validateConfirmedStopsForDepart(NxDisDriverRouteEntity route,
                                                          boolean requireLoadBeforeDepart) {
        if (route == null || route.getStops() == null) {
            return "该司机路线还没有可出发的门店";
        }
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop == null || isCancelledStop(stop)) {
                continue;
            }
            if (!isConfirmedStop(stop)) {
                return "请先确认全部门店出货完成";
            }
            if (!isConfirmedStopSource(stop)) {
                return "请先确认全部门店出货完成";
            }
            if (isStopInTerminalDeliveryState(stop)) {
                return "该司机路线已在配送中或已完成";
            }
            if (requireLoadBeforeDepart) {
                NxDisShipmentTaskEntity task = stop.getShipmentTask();
                if (task != null && !READY_TO_GO.equals(task.getNxDstStatus())) {
                    return "请先完成全部装车确认后再出发";
                }
            }
        }
        return null;
    }

    private static String validateAllTasksReadyToGo(List<NxDisShipmentTaskEntity> routeTasks) {
        if (routeTasks == null || routeTasks.isEmpty()) {
            return "请先完成全部装车确认后再出发";
        }
        for (NxDisShipmentTaskEntity task : routeTasks) {
            if (task == null || CANCELLED.equals(task.getNxDstStatus())) {
                continue;
            }
            if (!READY_TO_GO.equals(task.getNxDstStatus())) {
                return "请先完成全部装车确认后再出发";
            }
        }
        return null;
    }

    private static boolean isConfirmedStopSource(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        if (DisRouteSandboxStopSource.CONFIRMED.equals(stop.getStopSource())) {
            return true;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        return task != null && DisRouteSandboxStopSource.CONFIRMED.equals(task.getStopSource());
    }

    private static boolean isStopInTerminalDeliveryState(NxDisRouteStopEntity stop) {
        NxDisShipmentTaskEntity task = stop != null ? stop.getShipmentTask() : null;
        if (task == null) {
            return false;
        }
        String status = task.getNxDstStatus();
        return IN_DELIVERY.equals(status) || DELIVERED.equals(status) || CLOSED.equals(status);
    }

    private static boolean isConfirmedStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        if (DisRouteSandboxStopSource.CONFIRMED.equals(stop.getStopSource())) {
            return true;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        return task != null && task.getNxDstId() != null && isConfirmedExecutionTask(task);
    }

    private static boolean isCancelledStop(NxDisRouteStopEntity stop) {
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && CANCELLED.equals(task.getNxDstStatus())) {
            return true;
        }
        String stopStatus = stop.getNxDrsStopStatus();
        return stopStatus != null && CANCELLED.equalsIgnoreCase(stopStatus);
    }

    /** 老板确认出货完成后的 execution task：ASSIGNED 即可出发，不要求 READY_TO_GO。 */
    private static boolean isConfirmedExecutionTask(NxDisShipmentTaskEntity task) {
        if (task == null || task.getNxDstId() == null) {
            return false;
        }
        if (CANCELLED.equals(task.getNxDstStatus())) {
            return false;
        }
        if (task.getNxDstManualLocked() == null || task.getNxDstManualLocked() != 1) {
            return false;
        }
        String status = task.getNxDstStatus();
        return ASSIGNED.equals(status) || READY_TO_GO.equals(status)
                || IN_DELIVERY.equals(status) || DELIVERED.equals(status);
    }

    public static final class RouteStopCounts {
        public int totalStopCount;
        public int confirmedStopCount;
        public int unconfirmedStopCount;
    }
}
