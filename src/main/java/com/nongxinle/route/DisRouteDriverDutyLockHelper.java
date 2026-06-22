package com.nongxinle.route;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dto.route.RouteDispatchOperationDecision;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.route.DisRoutePlanStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.EXCEPTION;
import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;

/**
 * 司机可派状态页：装车 / 出发 / 配送执行中不可关闭可派。
 */
public final class DisRouteDriverDutyLockHelper {

    public static final String ACTION_TYPE_TOGGLE_DUTY_OFF = "TOGGLE_DUTY_OFF";

    private DisRouteDriverDutyLockHelper() {
    }

    public static RouteDispatchOperationDecision evaluateCheckOut(Integer disId,
                                                                  String routeDate,
                                                                  Integer driverUserId,
                                                                  NxDisDriverRouteDao routeDao,
                                                                  NxDisRoutePlanDao planDao,
                                                                  NxDisShipmentTaskDao taskDao) {
        if (driverUserId == null) {
            return RouteDispatchOperationDecision.deny("未找到司机");
        }
        String lockReason = resolveDutyLockReason(disId, routeDate, driverUserId, routeDao, planDao, taskDao);
        if (lockReason != null) {
            RouteDispatchOperationDecision denied = RouteDispatchOperationDecision.deny(lockReason);
            denied.setOperationHint(lockReason);
            return denied;
        }
        return RouteDispatchOperationDecision.allow();
    }

    public static String resolveDutyLockReason(Integer disId,
                                               String routeDate,
                                               Integer driverUserId,
                                               NxDisDriverRouteDao routeDao,
                                               NxDisRoutePlanDao planDao,
                                               NxDisShipmentTaskDao taskDao) {
        if (driverUserId == null) {
            return "未找到司机";
        }
        if (hasActiveExecutionTask(taskDao, disId, routeDate, driverUserId)) {
            return "司机配送任务执行中，不能关闭可派";
        }
        NxDisDriverRouteEntity activeRoute = findActiveLockedRoute(
                disId, routeDate, driverUserId, routeDao, planDao);
        if (activeRoute == null) {
            return null;
        }
        if (DisRouteRouteExecutionHelper.isExecutionRoute(activeRoute)
                || activeRoute.getNxDdrActualDepartAt() != null) {
            return "司机已出发，不能关闭可派";
        }
        if (DisRouteLoadingGateHelper.isRouteEnteredLoading(activeRoute)) {
            return "司机已进入装车，不能关闭可派";
        }
        String status = activeRoute.getNxDdrRouteStatus();
        if (status != null) {
            String normalized = status.trim().toUpperCase();
            if (DisRouteDriverRouteStatus.IN_DELIVERY.equals(normalized)
                    || DisRouteDriverRouteStatus.DELIVERED.equals(normalized)
                    || DisRouteDriverRouteStatus.COMPLETED.equals(normalized)) {
                return "司机配送中，不能关闭可派";
            }
            if (DisRouteDriverRouteStatus.READY_TO_DEPART.equals(normalized)) {
                return "司机已进入装车，不能关闭可派";
            }
        }
        return "司机有进行中的路线任务，不能关闭可派";
    }

    private static boolean hasActiveExecutionTask(NxDisShipmentTaskDao taskDao,
                                                  Integer disId,
                                                  String routeDate,
                                                  Integer driverUserId) {
        if (taskDao == null || disId == null || driverUserId == null) {
            return false;
        }
        for (String status : new String[]{IN_DELIVERY, EXCEPTION}) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("disId", disId);
            params.put("routeDate", routeDate);
            params.put("status", status);
            List<NxDisShipmentTaskEntity> tasks = taskDao.queryByDisRouteDateStatus(params);
            if (tasks == null) {
                continue;
            }
            for (NxDisShipmentTaskEntity task : tasks) {
                if (task == null) {
                    continue;
                }
                if (driverUserId.equals(task.getNxDstAssignedDriverUserId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static NxDisDriverRouteEntity findActiveLockedRoute(Integer disId,
                                                                String routeDate,
                                                                Integer driverUserId,
                                                                NxDisDriverRouteDao routeDao,
                                                                NxDisRoutePlanDao planDao) {
        if (routeDao == null || driverUserId == null) {
            return null;
        }
        Set<Integer> seenRouteIds = new HashSet<Integer>();
        if (planDao != null && disId != null && routeDate != null) {
            Map<String, Object> planParams = new HashMap<String, Object>();
            planParams.put("disId", disId);
            planParams.put("routeDate", routeDate);
            List<NxDisRoutePlanEntity> plans = planDao.queryList(planParams);
            if (plans != null) {
                for (NxDisRoutePlanEntity plan : plans) {
                    if (plan == null || plan.getNxDrpId() == null
                            || CANCELLED.equals(plan.getNxDrpStatus())) {
                        continue;
                    }
                    List<NxDisDriverRouteEntity> routes = routeDao.queryByPlanId(plan.getNxDrpId());
                    NxDisDriverRouteEntity locked = findLockedRouteForDriver(
                            routes, driverUserId, seenRouteIds, routeDao);
                    if (locked != null) {
                        return locked;
                    }
                }
            }
        }
        return null;
    }

    private static NxDisDriverRouteEntity findLockedRouteForDriver(List<NxDisDriverRouteEntity> routes,
                                                                   Integer driverUserId,
                                                                   Set<Integer> seenRouteIds,
                                                                   NxDisDriverRouteDao routeDao) {
        if (routes == null || driverUserId == null) {
            return null;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null || !driverUserId.equals(route.getNxDdrDriverUserId())
                    || route.getNxDdrId() == null || !seenRouteIds.add(route.getNxDdrId())) {
                continue;
            }
            NxDisDriverRouteEntity hydrated = routeDao.queryObject(route.getNxDdrId());
            if (hydrated == null) {
                hydrated = route;
            } else {
                DisRouteRouteExecutionHelper.mergeExecutionFieldsFromDb(hydrated, route);
            }
            if (isRouteLockedForDutyToggle(hydrated)) {
                return hydrated;
            }
        }
        return null;
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
