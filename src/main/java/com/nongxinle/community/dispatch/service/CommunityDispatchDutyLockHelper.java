package com.nongxinle.community.dispatch.service;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.dao.NxCommunityDispatchDriverRouteDao;
import com.nongxinle.dao.NxCommunityDispatchPlanDao;
import com.nongxinle.entity.NxCommunityDispatchDriverRouteEntity;
import com.nongxinle.entity.NxCommunityDispatchPlanEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 社区司机下岗校验：装车/配送中不可关闭可派。 */
@Component
public class CommunityDispatchDutyLockHelper {

    @Autowired
    private NxCommunityDispatchPlanDao nxCommunityDispatchPlanDao;
    @Autowired
    private NxCommunityDispatchDriverRouteDao nxCommunityDispatchDriverRouteDao;

    public String resolveDutyLockReason(Integer communityId, String routeDate, Integer driverUserId) {
        if (driverUserId == null) {
            return "未找到司机";
        }
        NxCommunityDispatchDriverRouteEntity route = findActiveRoute(communityId, routeDate, driverUserId);
        if (route == null) {
            return null;
        }
        if (route.getNxCddrActualDepartAt() != null
                || CommunityDispatchConstants.ROUTE_STATUS_IN_DELIVERY.equals(route.getNxCddrRouteStatus())) {
            return "司机配送中，不能关闭可派";
        }
        if (route.getNxCddrLoadingEnteredAt() != null
                || CommunityDispatchConstants.ROUTE_STATUS_LOADING.equals(route.getNxCddrRouteStatus())) {
            return "司机已进入装车，不能关闭可派";
        }
        if (route.getNxCddrStopCount() != null && route.getNxCddrStopCount() > 0) {
            return "司机有进行中的路线任务，不能关闭可派";
        }
        return null;
    }

    private NxCommunityDispatchDriverRouteEntity findActiveRoute(
            Integer communityId, String routeDate, Integer driverUserId) {
        Map<String, Object> planMap = new HashMap<String, Object>();
        planMap.put("communityId", communityId);
        planMap.put("routeDate", routeDate);
        NxCommunityDispatchPlanEntity plan = nxCommunityDispatchPlanDao.queryByCommunityAndRouteDate(planMap);
        if (plan == null) {
            return null;
        }
        Map<String, Object> routeMap = new HashMap<String, Object>();
        routeMap.put("planId", plan.getNxCommunityDispatchPlanId());
        routeMap.put("driverUserId", driverUserId);
        NxCommunityDispatchDriverRouteEntity route =
                nxCommunityDispatchDriverRouteDao.queryByPlanAndDriver(routeMap);
        if (route == null) {
            return null;
        }
        String status = route.getNxCddrRouteStatus();
        if (CommunityDispatchConstants.ROUTE_STATUS_COMPLETED.equals(status)
                || CommunityDispatchConstants.ROUTE_STATUS_IDLE.equals(status)) {
            return null;
        }
        return route;
    }
}
