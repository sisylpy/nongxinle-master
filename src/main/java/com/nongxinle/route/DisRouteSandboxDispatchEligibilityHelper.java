package com.nongxinle.route;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.route.DisRoutePlanStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.EXCEPTION;
import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;
import static com.nongxinle.route.DisShipmentTaskStatus.SIMULATED;
import static com.nongxinle.route.DisShipmentTaskStatus.UNASSIGNED;

import com.nongxinle.dao.NxDisDriverDutyDao;
import com.nongxinle.entity.NxDisDriverDutyEntity;

import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;

/**
 * 今日派车沙箱：已出发/配送中/不可派司机不得参与新订单建议分派。
 */
public final class DisRouteSandboxDispatchEligibilityHelper {

    private DisRouteSandboxDispatchEligibilityHelper() {
    }

    public static Set<Integer> resolveIneligibleDriverUserIds(NxDisDriverRouteDao routeDao, Integer planId) {
        Set<Integer> ids = new HashSet<Integer>();
        if (routeDao == null || planId == null) {
            return ids;
        }
        List<NxDisDriverRouteEntity> routes = routeDao.queryByPlanId(planId);
        collectIneligibleDriverUserIds(routes, ids, routeDao);
        return ids;
    }

    /**
     * 沙盘派车：已出发/配送中司机不得参与新单建议。除主 plan 外，扫描当日全部 plan 与执行态 task，
     * 与司机列表 {@code batchEligible} 判定一致。
     */
    public static Set<Integer> resolveSandboxDispatchIneligibleDriverUserIds(
            NxDisDriverRouteDao routeDao,
            NxDisRoutePlanDao planDao,
            NxDisShipmentTaskDao taskDao,
            Integer disId,
            String routeDate,
            Integer primaryPlanId) {
        Set<Integer> ids = new HashSet<Integer>();
        if (routeDao == null || disId == null || routeDate == null) {
            return ids;
        }
        if (primaryPlanId != null) {
            ids.addAll(resolveIneligibleDriverUserIds(routeDao, primaryPlanId));
        }
        if (planDao != null) {
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
                    if (primaryPlanId != null && primaryPlanId.equals(plan.getNxDrpId())) {
                        continue;
                    }
                    List<NxDisDriverRouteEntity> routes = routeDao.queryByPlanId(plan.getNxDrpId());
                    collectIneligibleDriverUserIds(routes, ids, routeDao);
                }
            }
        }
        collectIneligibleDriversFromActiveExecutionTasks(taskDao, disId, routeDate, ids);
        return ids;
    }

    private static void collectIneligibleDriversFromActiveExecutionTasks(
            NxDisShipmentTaskDao taskDao,
            Integer disId,
            String routeDate,
            Set<Integer> target) {
        if (taskDao == null || target == null) {
            return;
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
                Integer driverId = task.getNxDstAssignedDriverUserId();
                if (driverId == null) {
                    driverId = task.getNxDstSuggestedDriverUserId();
                }
                if (driverId != null) {
                    target.add(driverId);
                }
            }
        }
    }

    public static void collectIneligibleDriverUserIds(List<NxDisDriverRouteEntity> routes,
                                                      Set<Integer> target,
                                                      NxDisDriverRouteDao routeDao) {
        if (routes == null || target == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null || route.getNxDdrDriverUserId() == null) {
                continue;
            }
            NxDisDriverRouteEntity hydrated = route;
            if (routeDao != null && route.getNxDdrId() != null) {
                NxDisDriverRouteEntity dbRoute = routeDao.queryObject(route.getNxDdrId());
                if (dbRoute != null) {
                    DisRouteRouteExecutionHelper.mergeExecutionFieldsFromDb(hydrated, dbRoute);
                }
            }
            if (DisRouteRouteExecutionHelper.isExecutionRoute(hydrated)) {
                target.add(route.getNxDdrDriverUserId());
            }
        }
    }

    public static boolean isSandboxEphemeralStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        if (DisRouteSandboxStopSource.SANDBOX_SUGGESTED.equals(stop.getStopSource())
                || DisRouteSandboxStopSource.UNASSIGNED.equals(stop.getStopSource())) {
            return true;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null) {
            String status = task.getNxDstStatus();
            return SIMULATED.equals(status) || UNASSIGNED.equals(status);
        }
        return stop.getNxDrsShipmentTaskId() == null;
    }

    public static boolean isDriverEligibleForSandboxDispatch(Integer driverUserId, Set<Integer> ineligibleDriverIds) {
        if (driverUserId == null) {
            return false;
        }
        return ineligibleDriverIds == null || !ineligibleDriverIds.contains(driverUserId);
    }

    /** 当日未上岗 / 已关闭可派的司机，不得参与今日派单沙盘（执行中任务除外）。 */
    public static Set<Integer> resolveOffDutyDriverUserIds(NxDisDriverDutyDao dutyDao,
                                                          Integer disId,
                                                          String routeDate,
                                                          Iterable<Integer> driverUserIds) {
        Set<Integer> offDuty = new HashSet<Integer>();
        if (dutyDao == null || disId == null || routeDate == null || driverUserIds == null) {
            return offDuty;
        }
        for (Integer driverUserId : driverUserIds) {
            if (driverUserId == null) {
                continue;
            }
            NxDisDriverDutyEntity duty = dutyDao.queryByDisDriverDate(disId, driverUserId, routeDate);
            if (duty == null || !ON_DUTY.equals(duty.getNxDddDutyStatus())) {
                offDuty.add(driverUserId);
            }
        }
        return offDuty;
    }

    public static Set<Integer> unionDriverIds(Set<Integer> first, Set<Integer> second) {
        Set<Integer> merged = new HashSet<Integer>();
        if (first != null) {
            merged.addAll(first);
        }
        if (second != null) {
            merged.addAll(second);
        }
        return merged;
    }

    /** 不可派司机的已确认站点（非装车/非执行）应回到未分配读模型。 */
    public static boolean shouldDemoteConfirmedStopForOffDutyDriver(NxDisShipmentTaskEntity task,
                                                                    Integer offDutyDriverId,
                                                                    NxDisDriverRouteDao routeDao) {
        if (task == null || offDutyDriverId == null
                || !offDutyDriverId.equals(task.getNxDstAssignedDriverUserId())) {
            return false;
        }
        return !isConfirmedAssignmentProtectedForOffDuty(task, routeDao);
    }

    public static boolean isConfirmedAssignmentProtectedForOffDuty(NxDisShipmentTaskEntity task,
                                                                     NxDisDriverRouteDao routeDao) {
        if (task == null) {
            return false;
        }
        String status = task.getNxDstStatus();
        if (IN_DELIVERY.equals(status) || EXCEPTION.equals(status)
                || DisShipmentTaskStatus.DELIVERED.equals(status)) {
            return true;
        }
        if (task.getNxDstDriverRouteId() == null || routeDao == null) {
            return false;
        }
        NxDisDriverRouteEntity route = routeDao.queryObject(task.getNxDstDriverRouteId());
        if (route == null) {
            return false;
        }
        return DisRouteLoadingGateHelper.isRouteEnteredLoading(route)
                || DisRouteRouteExecutionHelper.isExecutionRoute(route);
    }
}
