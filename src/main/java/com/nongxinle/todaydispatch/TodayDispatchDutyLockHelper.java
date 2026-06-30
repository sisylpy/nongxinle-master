package com.nongxinle.todaydispatch;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dto.route.RouteDispatchOperationDecision;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.route.DisRouteDriverRouteStatus.COMPLETED;
import static com.nongxinle.route.DisRouteDriverRouteStatus.DELIVERED;
import static com.nongxinle.route.DisRouteDriverRouteStatus.IN_DELIVERY;
import static com.nongxinle.route.DisRouteDriverRouteStatus.READY_TO_DEPART;
import static com.nongxinle.route.DisRoutePlanStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.EXCEPTION;

/** 司机下岗校验 — todaydispatch 自有，不依赖 route 包 helper。 */
@Component
public class TodayDispatchDutyLockHelper {

    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;

    public RouteDispatchOperationDecision evaluateCheckOut(Integer disId,
                                                           String routeDate,
                                                           Integer driverUserId) {
        String lockReason = resolveDutyLockReason(disId, routeDate, driverUserId);
        if (lockReason != null) {
            RouteDispatchOperationDecision denied = RouteDispatchOperationDecision.deny(lockReason);
            denied.setOperationHint(lockReason);
            return denied;
        }
        return RouteDispatchOperationDecision.allow();
    }

    public String resolveDutyLockReason(Integer disId, String routeDate, Integer driverUserId) {
        if (driverUserId == null) {
            return "未找到司机";
        }
        if (hasActiveDeliveryTask(disId, routeDate, driverUserId)) {
            return "司机配送任务执行中，不能关闭可派";
        }
        NxDisDriverRouteEntity activeRoute = findActiveLockedRoute(disId, routeDate, driverUserId);
        if (activeRoute == null) {
            return null;
        }
        if (activeRoute.getNxDdrActualDepartAt() != null) {
            return "司机已出发，不能关闭可派";
        }
        if (activeRoute.getNxDdrLoadingEnteredAt() != null) {
            return "司机已进入装车，不能关闭可派";
        }
        String status = activeRoute.getNxDdrRouteStatus();
        if (status != null) {
            String normalized = status.trim().toUpperCase();
            if (IN_DELIVERY.equals(normalized) || DELIVERED.equals(normalized)
                    || COMPLETED.equals(normalized) || READY_TO_DEPART.equals(normalized)) {
                return "司机配送中，不能关闭可派";
            }
        }
        return "司机有进行中的路线任务，不能关闭可派";
    }

    private boolean hasActiveDeliveryTask(Integer disId, String routeDate, Integer driverUserId) {
        for (String status : new String[]{
                com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY, EXCEPTION}) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("disId", disId);
            params.put("routeDate", routeDate);
            params.put("status", status);
            List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByDisRouteDateStatus(params);
            if (tasks == null) {
                continue;
            }
            for (NxDisShipmentTaskEntity task : tasks) {
                if (task != null && driverUserId.equals(task.getNxDstAssignedDriverUserId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private NxDisDriverRouteEntity findActiveLockedRoute(Integer disId,
                                                         String routeDate,
                                                         Integer driverUserId) {
        Set<Integer> seenRouteIds = new HashSet<Integer>();
        if (disId == null || routeDate == null) {
            return null;
        }
        Map<String, Object> planParams = new HashMap<String, Object>();
        planParams.put("disId", disId);
        planParams.put("routeDate", routeDate);
        List<NxDisRoutePlanEntity> plans = nxDisRoutePlanDao.queryList(planParams);
        if (plans == null) {
            return null;
        }
        for (NxDisRoutePlanEntity plan : plans) {
            if (plan == null || plan.getNxDrpId() == null || CANCELLED.equals(plan.getNxDrpStatus())) {
                continue;
            }
            List<NxDisDriverRouteEntity> routes = nxDisDriverRouteDao.queryByPlanId(plan.getNxDrpId());
            NxDisDriverRouteEntity locked = findLockedRouteForDriver(routes, driverUserId, seenRouteIds);
            if (locked != null) {
                return locked;
            }
        }
        return null;
    }

    private NxDisDriverRouteEntity findLockedRouteForDriver(List<NxDisDriverRouteEntity> routes,
                                                            Integer driverUserId,
                                                            Set<Integer> seenRouteIds) {
        if (routes == null || driverUserId == null) {
            return null;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null || !driverUserId.equals(route.getNxDdrDriverUserId())
                    || route.getNxDdrId() == null || !seenRouteIds.add(route.getNxDdrId())) {
                continue;
            }
            NxDisDriverRouteEntity hydrated = nxDisDriverRouteDao.queryObject(route.getNxDdrId());
            if (hydrated == null) {
                hydrated = route;
            }
            if (isRouteLocked(hydrated)) {
                return hydrated;
            }
        }
        return null;
    }

    private static boolean isRouteLocked(NxDisDriverRouteEntity route) {
        if (route == null) {
            return false;
        }
        if (route.getNxDdrActualDepartAt() != null) {
            return true;
        }
        if (route.getNxDdrLoadingEnteredAt() != null) {
            return true;
        }
        String status = route.getNxDdrRouteStatus();
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        return IN_DELIVERY.equals(normalized) || DELIVERED.equals(normalized)
                || COMPLETED.equals(normalized) || READY_TO_DEPART.equals(normalized);
    }
}
