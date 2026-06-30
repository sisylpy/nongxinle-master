package com.nongxinle.community.dispatch.service;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.dao.*;
import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.nongxinle.community.dispatch.constants.CommunityDispatchConstants.DRIVER_ROLE_ID;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

@Service
public class CommunityDispatchComputeService {

    @Autowired
    private NxCommunityOrderDispatchDao nxCommunityOrderDispatchDao;
    @Autowired
    private NxCommunityDispatchPlanDao nxCommunityDispatchPlanDao;
    @Autowired
    private NxCommunityDispatchDriverRouteDao nxCommunityDispatchDriverRouteDao;
    @Autowired
    private NxCommunityDispatchStopDao nxCommunityDispatchStopDao;
    @Autowired
    private NxCommunityDao nxCommunityDao;
    @Autowired
    private NxCommunityUserDao nxCommunityUserDao;

    public CommunityDispatchSandboxResult computeSandbox(Integer communityId, String routeDate) {
        if (routeDate == null || routeDate.trim().isEmpty()) {
            routeDate = formatWhatDay(0);
        }
        CommunityDispatchSandboxResult result = new CommunityDispatchSandboxResult();
        result.setCommunityId(communityId);
        result.setRouteDate(routeDate);
        result.setPageMode(CommunityDispatchConstants.PAGE_MODE_SANDBOX);

        NxCommunityEntity community = nxCommunityDao.queryObject(communityId);
        if (community != null) {
            result.setDepotLat(community.getNxCommunityLat());
            result.setDepotLng(community.getNxCommunityLng());
        }

        Map<String, Object> eligibleMap = new HashMap<>();
        eligibleMap.put("communityId", communityId);
        eligibleMap.put("routeDate", routeDate);
        List<NxCommunityOrdersEntity> eligible = nxCommunityOrderDispatchDao.queryEligibleDeliveryOrders(eligibleMap);
        result.setEligibleOrders(eligible);

        Map<String, Object> driverMap = new HashMap<>();
        driverMap.put("commId", communityId);
        driverMap.put("roleId", DRIVER_ROLE_ID);
        result.setAvailableDrivers(nxCommunityUserDao.queryCommunityRoleUsers(driverMap));

        Map<String, Object> planMap = new HashMap<>();
        planMap.put("communityId", communityId);
        planMap.put("routeDate", routeDate);
        NxCommunityDispatchPlanEntity plan = nxCommunityDispatchPlanDao.queryByCommunityAndRouteDate(planMap);
        if (plan != null) {
            List<NxCommunityDispatchDriverRouteEntity> routes = nxCommunityDispatchDriverRouteDao.queryByPlanId(
                    plan.getNxCommunityDispatchPlanId());
            Map<String, Object> activeStopMap = new HashMap<>();
            activeStopMap.put("planId", plan.getNxCommunityDispatchPlanId());
            activeStopMap.put("cancelledStatus", CommunityDispatchConstants.STOP_STATUS_CANCELLED);
            List<NxCommunityDispatchStopEntity> activeStops =
                    nxCommunityDispatchStopDao.queryActiveByPlanId(activeStopMap);
            for (NxCommunityDispatchDriverRouteEntity route : routes) {
                List<NxCommunityDispatchStopEntity> routeStops = new ArrayList<>();
                for (NxCommunityDispatchStopEntity stop : activeStops) {
                    if (route.getNxCommunityDispatchDriverRouteId().equals(stop.getNxCdsDriverRouteId())) {
                        routeStops.add(stop);
                    }
                }
                route.setStops(routeStops);
            }
            result.setConfirmedRoutes(routes);
            result.setConfirmedStops(activeStops);
            removeAssignedOrders(eligible, activeStops);
        }

        buildSimulatedGroups(result, eligible);
        return result;
    }

    public CommunityDispatchSandboxResult computeLoading(Integer communityId, String routeDate) {
        CommunityDispatchSandboxResult base = loadPersistedRoutes(communityId, routeDate,
                CommunityDispatchConstants.PAGE_MODE_LOADING);
        base.getConfirmedRoutes().removeIf(route -> !isLoadingRoute(route));
        return base;
    }

    public CommunityDispatchSandboxResult computeDelivery(Integer communityId, String routeDate) {
        CommunityDispatchSandboxResult base = loadPersistedRoutes(communityId, routeDate,
                CommunityDispatchConstants.PAGE_MODE_DELIVERY);
        base.getConfirmedRoutes().removeIf(route -> !isDeliveryRoute(route));
        return base;
    }

    public CommunityDispatchSandboxResult computeDriverLoading(Integer communityId, String routeDate,
                                                             Integer driverUserId) {
        CommunityDispatchSandboxResult base = computeLoading(communityId, routeDate);
        filterRoutesByDriver(base, driverUserId);
        return base;
    }

    public CommunityDispatchSandboxResult computeDriverDelivery(Integer communityId, String routeDate,
                                                              Integer driverUserId) {
        CommunityDispatchSandboxResult base = computeDelivery(communityId, routeDate);
        filterRoutesByDriver(base, driverUserId);
        return base;
    }

    private CommunityDispatchSandboxResult loadPersistedRoutes(Integer communityId, String routeDate, String pageMode) {
        if (routeDate == null || routeDate.trim().isEmpty()) {
            routeDate = formatWhatDay(0);
        }
        CommunityDispatchSandboxResult result = new CommunityDispatchSandboxResult();
        result.setCommunityId(communityId);
        result.setRouteDate(routeDate);
        result.setPageMode(pageMode);
        NxCommunityEntity community = nxCommunityDao.queryObject(communityId);
        if (community != null) {
            result.setDepotLat(community.getNxCommunityLat());
            result.setDepotLng(community.getNxCommunityLng());
        }
        Map<String, Object> planMap = new HashMap<>();
        planMap.put("communityId", communityId);
        planMap.put("routeDate", routeDate);
        NxCommunityDispatchPlanEntity plan = nxCommunityDispatchPlanDao.queryByCommunityAndRouteDate(planMap);
        if (plan == null) {
            return result;
        }
        List<NxCommunityDispatchDriverRouteEntity> routes = nxCommunityDispatchDriverRouteDao.queryByPlanId(
                plan.getNxCommunityDispatchPlanId());
        Map<String, Object> activeStopMap = new HashMap<>();
        activeStopMap.put("planId", plan.getNxCommunityDispatchPlanId());
        activeStopMap.put("cancelledStatus", CommunityDispatchConstants.STOP_STATUS_CANCELLED);
        List<NxCommunityDispatchStopEntity> activeStops = nxCommunityDispatchStopDao.queryActiveByPlanId(activeStopMap);
        for (NxCommunityDispatchDriverRouteEntity route : routes) {
            List<NxCommunityDispatchStopEntity> routeStops = new ArrayList<>();
            for (NxCommunityDispatchStopEntity stop : activeStops) {
                if (route.getNxCommunityDispatchDriverRouteId().equals(stop.getNxCdsDriverRouteId())) {
                    routeStops.add(stop);
                }
            }
            route.setStops(routeStops);
        }
        result.setConfirmedRoutes(routes);
        result.setConfirmedStops(activeStops);
        return result;
    }

    private void removeAssignedOrders(List<NxCommunityOrdersEntity> eligible,
                                      List<NxCommunityDispatchStopEntity> activeStops) {
        Set<Integer> assignedOrderIds = new HashSet<>();
        for (NxCommunityDispatchStopEntity stop : activeStops) {
            if (stop.getItems() == null) {
                continue;
            }
            for (NxCommunityDispatchStopItemEntity item : stop.getItems()) {
                assignedOrderIds.add(item.getNxCdsiCommunityOrderId());
            }
        }
        eligible.removeIf(order -> assignedOrderIds.contains(order.getNxCommunityOrdersId()));
    }

    private void buildSimulatedGroups(CommunityDispatchSandboxResult result,
                                      List<NxCommunityOrdersEntity> eligible) {
        Map<Integer, List<NxCommunityOrdersEntity>> groups = new LinkedHashMap<>();
        for (NxCommunityOrdersEntity order : eligible) {
            Integer addressId = order.getNxCoDeliveryAddressId();
            if (addressId == null) {
                continue;
            }
            if (!groups.containsKey(addressId)) {
                groups.put(addressId, new ArrayList<>());
            }
            groups.get(addressId).add(order);
        }
        result.setSimulatedStopGroups(groups);

        List<NxCommunityUserEntity> drivers = result.getAvailableDrivers();
        if (drivers == null || drivers.isEmpty()) {
            return;
        }
        int idx = 0;
        for (Integer addressId : groups.keySet()) {
            NxCommunityUserEntity driver = drivers.get(idx % drivers.size());
            result.getSimulatedDriverByAddress().put(addressId, driver.getNxCommunityUserId());
            idx++;
        }
    }

    private boolean isLoadingRoute(NxCommunityDispatchDriverRouteEntity route) {
        if (CommunityDispatchConstants.ROUTE_STATUS_LOADING.equals(route.getNxCddrRouteStatus())) {
            return true;
        }
        return route.getNxCddrLoadingEnteredAt() != null
                && route.getNxCddrActualDepartAt() == null
                && !CommunityDispatchConstants.ROUTE_STATUS_IN_DELIVERY.equals(route.getNxCddrRouteStatus());
    }

    private boolean isDeliveryRoute(NxCommunityDispatchDriverRouteEntity route) {
        return CommunityDispatchConstants.ROUTE_STATUS_IN_DELIVERY.equals(route.getNxCddrRouteStatus())
                || route.getNxCddrActualDepartAt() != null;
    }

    private void filterRoutesByDriver(CommunityDispatchSandboxResult result, Integer driverUserId) {
        if (driverUserId == null) {
            return;
        }
        List<NxCommunityDispatchDriverRouteEntity> filtered = new ArrayList<>();
        for (NxCommunityDispatchDriverRouteEntity route : result.getConfirmedRoutes()) {
            if (driverUserId.equals(route.getNxCddrDriverUserId())) {
                filtered.add(route);
            }
        }
        result.setConfirmedRoutes(filtered);
        List<NxCommunityDispatchStopEntity> stopFiltered = new ArrayList<>();
        for (NxCommunityDispatchStopEntity stop : result.getConfirmedStops()) {
            if (driverUserId.equals(stop.getNxCdsAssignedDriverUserId())) {
                stopFiltered.add(stop);
            }
        }
        result.setConfirmedStops(stopFiltered);
    }
}
