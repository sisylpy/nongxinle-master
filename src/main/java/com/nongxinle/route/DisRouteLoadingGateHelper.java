package com.nongxinle.route;

import com.nongxinle.dto.route.RouteDispatchOperationDecision;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.List;

import static com.nongxinle.route.DisShipmentTaskStatus.ASSIGNED;
import static com.nongxinle.route.DisShipmentTaskStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.READY_TO_GO;

/**
 * Phase 3f：路线级进入装车 / 撤销回今日派单门禁。
 * <p>
 * 站点确认分派（ASSIGNED）≠ 进入装车流程；仅 {@code nx_ddr_loading_entered_at} 非空时路线出现在装车页。
 */
public final class DisRouteLoadingGateHelper {

    public static final String ENTER_LOADING_LABEL = "进入装车";
    public static final String RETURN_TO_DISPATCH_LABEL = "撤销装车 · 回到今日派单";
    public static final String RETURN_CONFIRM_TITLE = "撤销装车";
    public static final String RETURN_CONFIRM_MESSAGE =
            "撤销后，该司机路线会回到今日派单页继续处理。已确认的分派关系会保留，不会取消店铺分派。";

    private DisRouteLoadingGateHelper() {
    }

    public static boolean isRouteEnteredLoading(NxDisDriverRouteEntity route) {
        return route != null && route.getNxDdrLoadingEnteredAt() != null;
    }

    public static boolean isLoadingScopeStop(NxDisRouteStopEntity stop, NxDisDriverRouteEntity route) {
        if (stop == null || !hasConfirmedTaskStatus(stop)) {
            return false;
        }
        return isRouteEnteredLoading(route);
    }

    public static RouteDispatchOperationDecision evaluateEnterLoading(NxDisDriverRouteEntity route,
                                                                    boolean hasPendingDispatchStopsForDriver) {
        Integer driverRouteId = resolveDriverRouteId(route, route != null ? route.getStops() : null);
        if (driverRouteId == null) {
            return RouteDispatchOperationDecision.deny("未找到司机路线");
        }
        if (route != null && DisRouteDriverDepartPolicy.isRouteDeparted(route)) {
            return RouteDispatchOperationDecision.deny("司机已出发，不能进入装车");
        }
        if (route != null && isRouteEnteredLoading(route)) {
            return RouteDispatchOperationDecision.deny("该路线已在装车流程中");
        }
        if (hasPendingDispatchStopsForDriver) {
            return RouteDispatchOperationDecision.deny("请先确认全部站点分派");
        }
        List<NxDisRouteStopEntity> stops = route != null ? route.getStops() : null;
        if (stops == null || stops.isEmpty()) {
            return RouteDispatchOperationDecision.deny("当前路线没有已确认站点");
        }
        if (!hasConfirmedAssignedStop(stops)) {
            return RouteDispatchOperationDecision.deny("当前路线没有可进入装车的站点");
        }
        return RouteDispatchOperationDecision.allow();
    }

    /**
     * 今日派单页 ViewModel：已确认分区路线卡上的 GO_LOADING 动作。
     * 仅看本卡站点，不扫描整条 plan 上的其它待确认站。
     */
    public static RouteDispatchOperationDecision evaluateGoLoadingPrimaryAction(
            NxDisDriverRouteEntity route, List<NxDisRouteStopEntity> cardStops) {
        if (cardStops == null || cardStops.isEmpty()) {
            return RouteDispatchOperationDecision.deny("当前路线没有站点");
        }
        if (resolveDriverRouteId(route, cardStops) == null) {
            return RouteDispatchOperationDecision.deny("未找到司机路线");
        }
        if (route != null && DisRouteDriverDepartPolicy.isRouteDeparted(route)) {
            return RouteDispatchOperationDecision.deny("司机已出发，不能进入装车");
        }
        if (route != null && isRouteEnteredLoading(route)) {
            return RouteDispatchOperationDecision.deny("该路线已在装车流程中");
        }
        if (DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
            return RouteDispatchOperationDecision.deny("路线已在配送执行中");
        }
        for (NxDisRouteStopEntity stop : cardStops) {
            if (stop == null) {
                continue;
            }
            if (isPendingDispatchStopForGoLoading(stop)) {
                return RouteDispatchOperationDecision.deny("请先确认全部站点分派");
            }
            if (!isConfirmedAssignedStop(stop, route)) {
                return RouteDispatchOperationDecision.deny("当前路线没有可进入装车的站点");
            }
        }
        return RouteDispatchOperationDecision.allow();
    }

    public static Integer resolveDriverRouteId(NxDisDriverRouteEntity route,
                                               List<NxDisRouteStopEntity> stops) {
        if (route != null && route.getNxDdrId() != null) {
            return route.getNxDdrId();
        }
        if (stops == null) {
            return null;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task != null && task.getNxDstDriverRouteId() != null) {
                return task.getNxDstDriverRouteId();
            }
        }
        return null;
    }

    private static boolean isPendingDispatchStopForGoLoading(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        if (Boolean.TRUE.equals(stop.getCanConfirmCustomer())) {
            return true;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && Boolean.TRUE.equals(task.getCanConfirmCustomer())) {
            return true;
        }
        return false;
    }

    private static boolean isConfirmedAssignedStop(NxDisRouteStopEntity stop, NxDisDriverRouteEntity route) {
        return isConfirmedAssignedStopInternal(stop, route);
    }

    /** 分区 / ViewModel 共用：已确认分派、尚未进入装车。 */
    public static boolean isConfirmedAssignedStopForView(NxDisRouteStopEntity stop,
                                                         NxDisDriverRouteEntity route) {
        return isConfirmedAssignedStopInternal(stop, route);
    }

    private static boolean isConfirmedAssignedStopInternal(NxDisRouteStopEntity stop,
                                                           NxDisDriverRouteEntity route) {
        if (stop == null) {
            return false;
        }
        if (route != null && isRouteEnteredLoading(route)) {
            return false;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && CANCELLED.equals(task.getNxDstStatus())) {
            return false;
        }
        if (task != null && (ASSIGNED.equals(task.getNxDstStatus())
                || READY_TO_GO.equals(task.getNxDstStatus()))) {
            return task.getNxDstAssignedDriverUserId() != null;
        }
        return false;
    }

    public static RouteDispatchOperationDecision evaluateReturnToDispatch(NxDisDriverRouteEntity route) {
        if (route == null || route.getNxDdrId() == null) {
            return RouteDispatchOperationDecision.deny("未找到司机路线");
        }
        if (!isRouteEnteredLoading(route)) {
            return RouteDispatchOperationDecision.deny("该路线尚未进入装车流程");
        }
        if (DisRouteDriverDepartPolicy.isRouteDeparted(route)) {
            return RouteDispatchOperationDecision.deny("司机已出发，不能撤销回今日派单");
        }
        if (DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
            return RouteDispatchOperationDecision.deny("路线已在配送执行中，不能撤销回今日派单");
        }
        String status = route.getNxDdrRouteStatus();
        if (status != null) {
            String normalized = status.trim().toUpperCase();
            if (DisRouteDriverRouteStatus.IN_DELIVERY.equals(normalized)
                    || DisRouteDriverRouteStatus.DELIVERED.equals(normalized)
                    || DisRouteDriverRouteStatus.COMPLETED.equals(normalized)) {
                return RouteDispatchOperationDecision.deny("路线已在配送中或已完成，不能撤销回今日派单");
            }
        }
        return RouteDispatchOperationDecision.allow();
    }

    private static boolean hasConfirmedTaskStatus(NxDisRouteStopEntity stop) {
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && CANCELLED.equals(task.getNxDstStatus())) {
            return false;
        }
        if (task != null && (ASSIGNED.equals(task.getNxDstStatus())
                || READY_TO_GO.equals(task.getNxDstStatus()))) {
            return true;
        }
        return false;
    }

    private static boolean hasConfirmedAssignedStop(List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return false;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task != null && ASSIGNED.equals(task.getNxDstStatus())
                    && task.getNxDstAssignedDriverUserId() != null) {
                return true;
            }
        }
        return false;
    }
}
