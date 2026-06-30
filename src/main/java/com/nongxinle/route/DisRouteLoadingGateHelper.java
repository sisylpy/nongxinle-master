package com.nongxinle.route;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import static com.nongxinle.route.DisShipmentTaskStatus.ASSIGNED;
import static com.nongxinle.route.DisShipmentTaskStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.READY_TO_GO;

/**
 * Phase 3f：路线级装车分区门禁。
 * <p>
 * 站点确认分派（ASSIGNED）≠ 进入装车流程；仅 {@code nx_ddr_loading_entered_at} 非空时路线出现在装车页。
 */
public final class DisRouteLoadingGateHelper {

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
}
