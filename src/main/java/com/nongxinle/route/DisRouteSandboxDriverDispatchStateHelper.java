package com.nongxinle.route;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.ArrayList;
import java.util.List;

import static com.nongxinle.route.DisRouteSandboxDriverDispatchPhase.ACTIVE_EXECUTION;
import static com.nongxinle.route.DisRouteSandboxDriverDispatchPhase.IDLE;
import static com.nongxinle.route.DisRouteSandboxDriverDispatchPhase.LOADING;
import static com.nongxinle.route.DisRouteSandboxDriverDispatchPhase.REDISPATCH_PRE_DEPART;
import static com.nongxinle.route.DisShipmentTaskStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.CLOSED;
import static com.nongxinle.route.DisShipmentTaskStatus.DELIVERED;

/**
 * 今日派单沙盘：司机/route 可派 / 在途 / 空闲 唯一主判断。
 * <p>
 * 当前运行时只暴露 OFF_DUTY / IDLE / LOADING / IN_DELIVERY（route phase 为 LOADING / ACTIVE_EXECUTION）。
 * DELIVERED / COMPLETED 仅作 DB 历史，不作为司机当前 phase。
 * <p>
 * 口径文档：{@code docs/route-dispatch/Route-Dispatch-Sandbox-Driver-State.md}
 */
public final class DisRouteSandboxDriverDispatchStateHelper {

    private DisRouteSandboxDriverDispatchStateHelper() {
    }

    /**
     * 解析 route 在沙盘新单语境下的阶段（task 主权优先于 route 头 IN_DELIVERY/DELIVERED）。
     */
    public static String resolveRoutePhase(NxDisDriverRouteEntity route) {
        return resolveRoutePhase(route, null, null);
    }

    public static String resolveRoutePhase(NxDisDriverRouteEntity route,
                                           NxDisDriverRouteDao routeDao,
                                           NxDisShipmentTaskDao taskDao) {
        if (route == null) {
            return IDLE;
        }
        hydrateRouteExecutionContext(route, routeDao, taskDao);

        if (areAllActiveRouteTasksTerminal(route, taskDao)) {
            return IDLE;
        }
        if (DisRouteRouteExecutionHelper.hasPendingExecutionStops(route)) {
            return ACTIVE_EXECUTION;
        }
        if (isPersistedLoadingRoute(route) || DisRouteLoadingGateHelper.isRouteEnteredLoading(route)) {
            return LOADING;
        }
        if (DisRouteRouteExecutionHelper.isRedispatchPreDepartRoute(route)) {
            return REDISPATCH_PRE_DEPART;
        }
        if (DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
            return ACTIVE_EXECUTION;
        }
        return IDLE;
    }

    private static boolean isPersistedLoadingRoute(NxDisDriverRouteEntity route) {
        if (route == null) {
            return false;
        }
        return LOADING.equalsIgnoreCase(normalize(route.getNxDdrRouteStatus()))
                || LOADING.equalsIgnoreCase(normalize(route.getRouteStatus()));
    }

    private static String normalize(String value) {
        return value != null ? value.trim() : "";
    }

    /** compute：是否阻挡进入 dispatchEligibleDrivers / 历史 preferred eligible。 */
    public static boolean blocksSandboxComputeDispatch(NxDisDriverRouteEntity route,
                                                       NxDisDriverRouteDao routeDao,
                                                       NxDisShipmentTaskDao taskDao) {
        return DisRouteSandboxDriverDispatchPhase.blocksSandboxComputeDispatch(
                resolveRoutePhase(route, routeDao, taskDao));
    }

    /**
     * legacy merge 局部判断：route 下 active task 是否均已 terminal（非司机当前 phase）。
     */
    public static boolean hasOnlyTerminalActiveTasks(NxDisDriverRouteEntity route,
                                                     NxDisDriverRouteDao routeDao,
                                                     NxDisShipmentTaskDao taskDao) {
        return areAllActiveRouteTasksTerminal(route, taskDao);
    }

    /** compute / legacy merge：是否可在该 route 上追加 ephemeral suggested 站。 */
    public static boolean acceptsSandboxEphemeralStops(NxDisDriverRouteEntity route,
                                                       NxDisDriverRouteDao routeDao,
                                                       NxDisShipmentTaskDao taskDao) {
        if (route == null) {
            return true;
        }
        String phase = resolveRoutePhase(route, routeDao, taskDao);
        return !LOADING.equals(phase) && !ACTIVE_EXECUTION.equals(phase);
    }

    public static Integer resolveStopDriverUserId(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        Integer driverId = stop.getSuggestedDriverUserId();
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (driverId == null && task != null) {
            driverId = task.getNxDstAssignedDriverUserId() != null
                    ? task.getNxDstAssignedDriverUserId()
                    : (task.getNxDstSuggestedDriverUserId() != null
                    ? task.getNxDstSuggestedDriverUserId() : task.getSuggestedDriverUserId());
        }
        return driverId;
    }

    /** queryByPlanId 不含 stops；按 route task 补全后再判阶段。 */
    public static void hydrateRouteExecutionContext(NxDisDriverRouteEntity route,
                                                    NxDisDriverRouteDao routeDao,
                                                    NxDisShipmentTaskDao taskDao) {
        if (route == null || route.getNxDdrId() == null) {
            return;
        }
        if (routeDao != null) {
            NxDisDriverRouteEntity dbRoute = routeDao.queryObject(route.getNxDdrId());
            if (dbRoute != null) {
                DisRouteRouteExecutionHelper.mergeExecutionFieldsFromDb(route, dbRoute);
            }
        }
        if (taskDao == null || (route.getStops() != null && !route.getStops().isEmpty())) {
            return;
        }
        List<NxDisShipmentTaskEntity> tasks = taskDao.queryByDriverRouteId(route.getNxDdrId());
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        List<NxDisRouteStopEntity> stops = new ArrayList<NxDisRouteStopEntity>();
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task == null) {
                continue;
            }
            NxDisRouteStopEntity stop = new NxDisRouteStopEntity();
            stop.setShipmentTask(task);
            stop.setNxDrsDepartmentId(task.getNxDstDepFatherId());
            stops.add(stop);
        }
        route.setStops(stops);
    }

    /**
     * route 下 active task 是否全部已送达/关闭/取消（legacy 局部判断，非司机当前 phase）。
     */
    public static boolean areAllActiveRouteTasksTerminal(NxDisDriverRouteEntity route,
                                                         NxDisShipmentTaskDao taskDao) {
        if (route == null) {
            return false;
        }
        List<NxDisShipmentTaskEntity> tasks = collectRouteTasks(route, taskDao);
        if (tasks.isEmpty()) {
            return false;
        }
        boolean hasActive = false;
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task == null || isCancelledOrClosedTask(task)) {
                continue;
            }
            hasActive = true;
            if (!DELIVERED.equals(task.getNxDstStatus())) {
                return false;
            }
        }
        return hasActive;
    }

    private static List<NxDisShipmentTaskEntity> collectRouteTasks(NxDisDriverRouteEntity route,
                                                                   NxDisShipmentTaskDao taskDao) {
        List<NxDisShipmentTaskEntity> tasks = new ArrayList<NxDisShipmentTaskEntity>();
        if (route.getStops() != null) {
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop != null && stop.getShipmentTask() != null) {
                    tasks.add(stop.getShipmentTask());
                }
            }
        }
        if (tasks.isEmpty() && taskDao != null && route.getNxDdrId() != null) {
            List<NxDisShipmentTaskEntity> dbTasks = taskDao.queryByDriverRouteId(route.getNxDdrId());
            if (dbTasks != null) {
                tasks.addAll(dbTasks);
            }
        }
        return tasks;
    }

    private static boolean isCancelledOrClosedTask(NxDisShipmentTaskEntity task) {
        return CANCELLED.equals(task.getNxDstStatus()) || CLOSED.equals(task.getNxDstStatus());
    }
}
