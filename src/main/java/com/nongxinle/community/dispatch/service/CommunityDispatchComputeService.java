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
    @Autowired
    private NxCustomerUserAddressDao nxCustomerUserAddressDao;
    @Autowired
    private CommunityDriverDutyService communityDriverDutyService;

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

        result.setAvailableDrivers(communityDriverDutyService.listOnDutyDriverUsers(communityId, routeDate));

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

        reconcileFullyDeliveredRoutes(result);
        buildSimulatedGroups(result, eligible);
        CommunityDispatchSandboxRouteBuilder.buildSuggestedRoutes(result, nxCustomerUserAddressDao);
        enrichRouteDriverUsers(result);
        return result;
    }

    public CommunityDispatchSandboxResult computeLoading(Integer communityId, String routeDate) {
        CommunityDispatchSandboxResult base = loadPersistedRoutes(communityId, routeDate,
                CommunityDispatchConstants.PAGE_MODE_LOADING);
        reconcileEmptyLoadingRoutes(base);
        base.getConfirmedRoutes().removeIf(route -> !isLoadingRoute(route) || !hasActiveCustomerStops(route));
        return base;
    }

    public CommunityDispatchSandboxResult computeDelivery(Integer communityId, String routeDate) {
        CommunityDispatchSandboxResult base = loadPersistedRoutes(communityId, routeDate,
                CommunityDispatchConstants.PAGE_MODE_DELIVERY);
        base.getConfirmedRoutes().removeIf(route -> !isDeliveryRoute(route));
        return base;
    }

    /** 本趟全部送达后释放司机路线，便于回到分派沙盘接单。 */
    public void releaseRouteToIdleIfFullyDelivered(Integer driverRouteId) {
        if (driverRouteId == null) {
            return;
        }
        if (nxCommunityDispatchStopDao.countActiveByDriverRouteId(driverRouteId) > 0) {
            return;
        }
        releaseRouteToIdle(driverRouteId);
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
        result.setAvailableDrivers(communityDriverDutyService.listOnDutyDriverUsers(communityId, routeDate));
        reconcileFullyDeliveredRoutes(result);
        enrichRouteDriverUsers(result);
        return result;
    }

    private void enrichRouteDriverUsers(CommunityDispatchSandboxResult result) {
        List<NxCommunityUserEntity> drivers = result.getAvailableDrivers();
        if (drivers == null) {
            drivers = new ArrayList<>();
            result.setAvailableDrivers(drivers);
        }
        Set<Integer> knownIds = new HashSet<>();
        for (NxCommunityUserEntity driver : drivers) {
            if (driver != null && driver.getNxCommunityUserId() != null) {
                knownIds.add(driver.getNxCommunityUserId());
            }
        }
        if (result.getConfirmedRoutes() == null) {
            return;
        }
        for (NxCommunityDispatchDriverRouteEntity route : result.getConfirmedRoutes()) {
            if (route == null || route.getNxCddrDriverUserId() == null) {
                continue;
            }
            if (knownIds.contains(route.getNxCddrDriverUserId())) {
                continue;
            }
            NxCommunityUserEntity user = nxCommunityUserDao.queryObject(route.getNxCddrDriverUserId());
            if (user != null) {
                drivers.add(user);
                knownIds.add(user.getNxCommunityUserId());
            }
        }
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
        if (route == null) {
            return false;
        }
        if (CommunityDispatchConstants.ROUTE_STATUS_LOADING.equals(route.getNxCddrRouteStatus())) {
            return hasActiveCustomerStops(route);
        }
        return route.getNxCddrLoadingEnteredAt() != null
                && route.getNxCddrActualDepartAt() == null
                && !CommunityDispatchConstants.ROUTE_STATUS_IN_DELIVERY.equals(route.getNxCddrRouteStatus())
                && hasActiveCustomerStops(route);
    }

    private boolean hasActiveCustomerStops(NxCommunityDispatchDriverRouteEntity route) {
        return route != null && route.getStops() != null && !route.getStops().isEmpty();
    }

    /** 修复历史脏数据：无站点但仍占装车门禁的路线，退回可分配状态。 */
    private void reconcileEmptyLoadingRoutes(CommunityDispatchSandboxResult result) {
        if (result.getConfirmedRoutes() == null) {
            return;
        }
        for (NxCommunityDispatchDriverRouteEntity route : result.getConfirmedRoutes()) {
            if (route == null || route.getNxCommunityDispatchDriverRouteId() == null) {
                continue;
            }
            if (hasActiveCustomerStops(route)) {
                continue;
            }
            if (!CommunityDispatchConstants.ROUTE_STATUS_LOADING.equals(route.getNxCddrRouteStatus())
                    && route.getNxCddrLoadingEnteredAt() == null) {
                continue;
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("routeId", route.getNxCommunityDispatchDriverRouteId());
            map.put("routeStatus", CommunityDispatchConstants.ROUTE_STATUS_IDLE);
            map.put("stopCount", 0);
            nxCommunityDispatchDriverRouteDao.clearLoadingGate(map);
            route.setNxCddrRouteStatus(CommunityDispatchConstants.ROUTE_STATUS_IDLE);
            route.setNxCddrLoadingEnteredAt(null);
            route.setNxCddrStopCount(0);
            route.setStops(new ArrayList<NxCommunityDispatchStopEntity>());
        }
    }

    private boolean isDeliveryRoute(NxCommunityDispatchDriverRouteEntity route) {
        if (route == null) {
            return false;
        }
        if (!CommunityDispatchConstants.ROUTE_STATUS_IN_DELIVERY.equals(route.getNxCddrRouteStatus())) {
            return false;
        }
        return hasUndeliveredStops(route);
    }

    /** 修复历史脏数据：已全部送达但仍占配送中的路线，释放回 IDLE。 */
    private void reconcileFullyDeliveredRoutes(CommunityDispatchSandboxResult result) {
        if (result.getConfirmedRoutes() == null) {
            return;
        }
        for (NxCommunityDispatchDriverRouteEntity route : result.getConfirmedRoutes()) {
            if (route == null || route.getNxCommunityDispatchDriverRouteId() == null) {
                continue;
            }
            if (hasUndeliveredStops(route)) {
                continue;
            }
            String status = route.getNxCddrRouteStatus();
            if (!CommunityDispatchConstants.ROUTE_STATUS_IN_DELIVERY.equals(status)
                    && !CommunityDispatchConstants.ROUTE_STATUS_COMPLETED.equals(status)
                    && route.getNxCddrActualDepartAt() == null) {
                continue;
            }
            releaseRouteToIdle(route.getNxCommunityDispatchDriverRouteId());
            route.setNxCddrRouteStatus(CommunityDispatchConstants.ROUTE_STATUS_IDLE);
            route.setNxCddrLoadingEnteredAt(null);
            route.setNxCddrActualDepartAt(null);
            route.setNxCddrStopCount(0);
        }
    }

    private boolean hasUndeliveredStops(NxCommunityDispatchDriverRouteEntity route) {
        if (route.getStops() == null || route.getStops().isEmpty()) {
            return false;
        }
        for (NxCommunityDispatchStopEntity stop : route.getStops()) {
            if (stop == null
                    || CommunityDispatchConstants.STOP_STATUS_CANCELLED.equals(stop.getNxCdsStopStatus())) {
                continue;
            }
            if (!CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(stop.getNxCdsStopStatus())) {
                return true;
            }
        }
        return false;
    }

    private void releaseRouteToIdle(Integer driverRouteId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("routeId", driverRouteId);
        map.put("routeStatus", CommunityDispatchConstants.ROUTE_STATUS_IDLE);
        map.put("stopCount", 0);
        nxCommunityDispatchDriverRouteDao.clearLoadingGate(map);
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
