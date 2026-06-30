package com.nongxinle.community.dispatch.service;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.community.dispatch.dto.CommunitySandboxStopConfirmRequest;
import com.nongxinle.community.dispatch.dto.CommunitySandboxStopReturnRequest;
import com.nongxinle.dao.*;
import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.nongxinle.utils.DateUtils.formatWhatDay;

@Service
public class CommunityDispatchConfirmService {

    @Autowired
    private CommunityDispatchComputeService communityDispatchComputeService;
    @Autowired
    private CommunityDispatchPageAssembler communityDispatchPageAssembler;
    @Autowired
    private NxCommunityOrderDispatchDao nxCommunityOrderDispatchDao;
    @Autowired
    private NxCommunityDispatchPlanDao nxCommunityDispatchPlanDao;
    @Autowired
    private NxCommunityDispatchDriverRouteDao nxCommunityDispatchDriverRouteDao;
    @Autowired
    private NxCommunityDispatchStopDao nxCommunityDispatchStopDao;
    @Autowired
    private NxCommunityDispatchStopItemDao nxCommunityDispatchStopItemDao;
    @Autowired
    private NxCommunityOrdersDao nxCommunityOrdersDao;
    @Autowired
    private NxCustomerUserAddressDao nxCustomerUserAddressDao;
    @Autowired
    private NxCommunityDao nxCommunityDao;
    @Autowired
    private NxCommunityUserDao nxCommunityUserDao;

    @Transactional
    public Map<String, Object> confirmStop(CommunitySandboxStopConfirmRequest request) {
        validateConfirmRequest(request);
        String routeDate = normalizeRouteDate(request.getRouteDate());
        Integer addressId = resolveAddressId(request);

        Map<String, Object> eligibleMap = new HashMap<>();
        eligibleMap.put("communityId", request.getCommunityId());
        eligibleMap.put("routeDate", routeDate);
        List<NxCommunityOrdersEntity> eligible = nxCommunityOrderDispatchDao.queryEligibleDeliveryOrders(eligibleMap);
        List<NxCommunityOrdersEntity> selectedOrders = filterSelectedOrders(eligible, addressId, request.getOrderIds());
        if (selectedOrders.isEmpty()) {
            throw new IllegalArgumentException("没有可确认的待派单订单");
        }

        NxCommunityUserEntity driver = nxCommunityUserDao.queryObject(request.getDriverUserId());
        if (driver == null || !Integer.valueOf(CommunityDispatchConstants.DRIVER_ROLE_ID).equals(driver.getNxCouRoleId())) {
            throw new IllegalArgumentException("司机无效");
        }

        for (NxCommunityOrdersEntity order : selectedOrders) {
            assertOrderCanConfirm(order.getNxCommunityOrdersId());
        }

        NxCommunityDispatchPlanEntity plan = resolveOrCreatePlan(request.getCommunityId(), routeDate);
        NxCommunityDispatchDriverRouteEntity route = resolveOrCreateDriverRoute(plan, request.getDriverUserId());
        enterLoadingGate(route);

        NxCustomerUserAddressEntity address = nxCustomerUserAddressDao.queryObject(addressId);
        NxCommunityOrdersEntity firstOrder = selectedOrders.get(0);

        int nextSeq = nextActiveRouteSeq(route.getNxCommunityDispatchDriverRouteId());
        Date now = new Date();
        NxCommunityDispatchStopEntity stop = new NxCommunityDispatchStopEntity();
        stop.setNxCdsPlanId(plan.getNxCommunityDispatchPlanId());
        stop.setNxCdsDriverRouteId(route.getNxCommunityDispatchDriverRouteId());
        stop.setNxCdsCommunityId(request.getCommunityId());
        stop.setNxCdsAddressId(addressId);
        stop.setNxCdsCustomerName(address != null ? address.getNxCuaUserName() : buildCustomerLabel(firstOrder));
        stop.setNxCdsCustomerPhone(address != null ? address.getNxCuaUserPhone() : null);
        stop.setNxCdsLat(address != null ? address.getNxCuaLat() : null);
        stop.setNxCdsLng(address != null ? address.getNxCuaLng() : null);
        stop.setNxCdsAddressText(buildAddressText(address));
        stop.setNxCdsStopStatus(CommunityDispatchConstants.STOP_STATUS_LOADING);
        stop.setNxCdsRouteSeq(nextSeq);
        stop.setNxCdsServiceDate(firstOrder.getNxCoServiceDate());
        stop.setNxCdsServiceTime(firstOrder.getNxCoServiceTime());
        stop.setNxCdsAssignedDriverUserId(request.getDriverUserId());
        stop.setNxCdsOrderCount(selectedOrders.size());
        stop.setNxCdsConfirmedAt(now);
        nxCommunityDispatchStopDao.save(stop);

        for (NxCommunityOrdersEntity order : selectedOrders) {
            NxCommunityDispatchStopItemEntity item = new NxCommunityDispatchStopItemEntity();
            item.setNxCdsiStopId(stop.getNxCommunityDispatchStopId());
            item.setNxCdsiCommunityOrderId(order.getNxCommunityOrdersId());
            item.setNxCdsiGoodsSummary("订单#" + order.getNxCommunityOrdersId());
            item.setNxCdsiOrderTotal(order.getNxCoTotal());
            nxCommunityDispatchStopItemDao.save(item);

            bindOrderDispatch(order, request.getCommunityId(), stop.getNxCommunityDispatchStopId(),
                    request.getDriverUserId(), routeDate);
        }

        route.setNxCddrStopCount(nxCommunityDispatchStopDao.countActiveByDriverRouteId(
                route.getNxCommunityDispatchDriverRouteId()));
        nxCommunityDispatchDriverRouteDao.update(route);

        return rebuildConfirmResponse(request.getCommunityId(), routeDate);
    }

    @Transactional
    public Map<String, Object> returnStopToSandbox(Integer stopId, CommunitySandboxStopReturnRequest request) {
        NxCommunityDispatchStopEntity stop = nxCommunityDispatchStopDao.queryObject(stopId);
        if (stop == null) {
            throw new IllegalArgumentException("站点不存在");
        }
        if (CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(stop.getNxCdsStopStatus())) {
            throw new IllegalArgumentException("已送达站点不能退回沙箱");
        }
        stop.setNxCdsStopStatus(CommunityDispatchConstants.STOP_STATUS_CANCELLED);
        nxCommunityDispatchStopDao.update(stop);

        nxCommunityOrderDispatchDao.resetToUnassignedByStopId(stopId);

        nxCommunityDispatchStopItemDao.deleteByStopId(stopId);

        if (stop.getNxCdsDriverRouteId() != null) {
            NxCommunityDispatchDriverRouteEntity route = nxCommunityDispatchDriverRouteDao.queryObject(stop.getNxCdsDriverRouteId());
            if (route != null) {
                int activeCount = nxCommunityDispatchStopDao.countActiveByDriverRouteId(route.getNxCommunityDispatchDriverRouteId());
                route.setNxCddrStopCount(activeCount);
                if (activeCount == 0) {
                    route.setNxCddrRouteStatus(CommunityDispatchConstants.ROUTE_STATUS_IDLE);
                    route.setNxCddrLoadingEnteredAt(null);
                }
                nxCommunityDispatchDriverRouteDao.update(route);
            }
        }

        String routeDate = request != null && request.getRouteDate() != null
                ? request.getRouteDate() : formatWhatDay(0);
        Integer communityId = request != null && request.getCommunityId() != null
                ? request.getCommunityId() : stop.getNxCdsCommunityId();
        return rebuildReturnToSandboxResponse(communityId, routeDate);
    }

    private Map<String, Object> rebuildConfirmResponse(Integer communityId, String routeDate) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pageViewModel", communityDispatchPageAssembler.assemble(
                communityDispatchComputeService.computeSandbox(communityId, routeDate)));
        data.put("enteredLoading", true);
        data.put("nextPage", CommunityDispatchConstants.PAGE_MODE_LOADING);
        return data;
    }

    private Map<String, Object> rebuildReturnToSandboxResponse(Integer communityId, String routeDate) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pageViewModel", communityDispatchPageAssembler.assemble(
                communityDispatchComputeService.computeSandbox(communityId, routeDate)));
        data.put("enteredLoading", false);
        data.put("nextPage", CommunityDispatchConstants.PAGE_MODE_SANDBOX);
        return data;
    }

    private Map<String, Object> rebuildSandboxResponse(Integer communityId, String routeDate) {
        return rebuildConfirmResponse(communityId, routeDate);
    }

    private NxCommunityDispatchPlanEntity resolveOrCreatePlan(Integer communityId, String routeDate) {
        Map<String, Object> planMap = new HashMap<>();
        planMap.put("communityId", communityId);
        planMap.put("routeDate", routeDate);
        NxCommunityDispatchPlanEntity plan = nxCommunityDispatchPlanDao.queryByCommunityAndRouteDate(planMap);
        if (plan != null) {
            return plan;
        }
        NxCommunityEntity community = nxCommunityDao.queryObject(communityId);
        plan = new NxCommunityDispatchPlanEntity();
        plan.setNxCdpCommunityId(communityId);
        plan.setNxCdpRouteDate(routeDate);
        plan.setNxCdpStatus(CommunityDispatchConstants.PLAN_STATUS_ACTIVE);
        if (community != null) {
            plan.setNxCdpDepotLat(community.getNxCommunityLat());
            plan.setNxCdpDepotLng(community.getNxCommunityLng());
        }
        nxCommunityDispatchPlanDao.save(plan);
        return plan;
    }

    private NxCommunityDispatchDriverRouteEntity resolveOrCreateDriverRoute(NxCommunityDispatchPlanEntity plan,
                                                                            Integer driverUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("planId", plan.getNxCommunityDispatchPlanId());
        map.put("driverUserId", driverUserId);
        NxCommunityDispatchDriverRouteEntity route = nxCommunityDispatchDriverRouteDao.queryByPlanAndDriver(map);
        if (route != null) {
            return route;
        }
        route = new NxCommunityDispatchDriverRouteEntity();
        route.setNxCddrPlanId(plan.getNxCommunityDispatchPlanId());
        route.setNxCddrCommunityId(plan.getNxCdpCommunityId());
        route.setNxCddrDriverUserId(driverUserId);
        route.setNxCddrRouteStatus(CommunityDispatchConstants.ROUTE_STATUS_DRAFT);
        route.setNxCddrStopCount(0);
        nxCommunityDispatchDriverRouteDao.save(route);
        return route;
    }

    private void enterLoadingGate(NxCommunityDispatchDriverRouteEntity route) {
        if (route.getNxCddrLoadingEnteredAt() == null) {
            route.setNxCddrLoadingEnteredAt(new Date());
        }
        route.setNxCddrRouteStatus(CommunityDispatchConstants.ROUTE_STATUS_LOADING);
        nxCommunityDispatchDriverRouteDao.update(route);
    }

    private int nextActiveRouteSeq(Integer driverRouteId) {
        return nxCommunityDispatchStopDao.countActiveByDriverRouteId(driverRouteId) + 1;
    }

    private List<NxCommunityOrdersEntity> filterSelectedOrders(List<NxCommunityOrdersEntity> eligible,
                                                             Integer addressId,
                                                             List<Integer> orderIds) {
        List<NxCommunityOrdersEntity> selected = new ArrayList<>();
        Set<Integer> wanted = orderIds != null ? new HashSet<>(orderIds) : null;
        for (NxCommunityOrdersEntity order : eligible) {
            if (!addressId.equals(order.getNxCoDeliveryAddressId())) {
                continue;
            }
            if (wanted != null && !wanted.contains(order.getNxCommunityOrdersId())) {
                continue;
            }
            selected.add(order);
        }
        return selected;
    }

    private Integer resolveAddressId(CommunitySandboxStopConfirmRequest request) {
        if (request.getAddressId() != null) {
            return request.getAddressId();
        }
        String key = request.getSandboxStopKey();
        if (key != null && key.startsWith(CommunityDispatchConstants.SANDBOX_KEY_PREFIX)) {
            return Integer.valueOf(key.substring(CommunityDispatchConstants.SANDBOX_KEY_PREFIX.length()));
        }
        throw new IllegalArgumentException("缺少 addressId 或 sandboxStopKey");
    }

    private void validateConfirmRequest(CommunitySandboxStopConfirmRequest request) {
        if (request.getCommunityId() == null) {
            throw new IllegalArgumentException("communityId 不能为空");
        }
        if (request.getDriverUserId() == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }
    }

    private String normalizeRouteDate(String routeDate) {
        if (routeDate == null || routeDate.trim().isEmpty()) {
            return formatWhatDay(0);
        }
        return routeDate;
    }

    private String buildAddressText(NxCustomerUserAddressEntity address) {
        if (address == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (address.getNxCuaAddressBuildingName() != null) {
            sb.append(address.getNxCuaAddressBuildingName());
        }
        if (address.getNxCuaAddressDetail() != null) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(address.getNxCuaAddressDetail());
        }
        return sb.toString();
    }

    private void assertOrderCanConfirm(Integer orderId) {
        NxCommunityOrderDispatchEntity existing = nxCommunityOrderDispatchDao.queryByOrderId(orderId);
        if (existing == null) {
            return;
        }
        if (CommunityDispatchConstants.isRedispatchableDispatchStatus(existing.getNxCodDispatchStatus())) {
            return;
        }
        if (CommunityDispatchConstants.isActiveDispatchStatus(existing.getNxCodDispatchStatus())) {
            throw new IllegalArgumentException("订单#" + orderId + "已在派单中，状态="
                    + existing.getNxCodDispatchStatus());
        }
        throw new IllegalArgumentException("订单#" + orderId + "派单状态异常："
                + existing.getNxCodDispatchStatus());
    }

    private void bindOrderDispatch(NxCommunityOrdersEntity order, Integer communityId, Integer stopId,
                                   Integer driverUserId, String routeDate) {
        NxCommunityOrderDispatchEntity existing = nxCommunityOrderDispatchDao.queryByOrderId(order.getNxCommunityOrdersId());
        if (existing == null) {
            NxCommunityOrderDispatchEntity dispatch = new NxCommunityOrderDispatchEntity();
            dispatch.setNxCodCommunityOrderId(order.getNxCommunityOrdersId());
            dispatch.setNxCodCommunityId(communityId);
            dispatch.setNxCodDispatchStatus(CommunityDispatchConstants.DISPATCH_STATUS_LOADING);
            dispatch.setNxCodDispatchStopId(stopId);
            dispatch.setNxCodAssignedDriverUserId(driverUserId);
            dispatch.setNxCodRouteDate(routeDate);
            nxCommunityOrderDispatchDao.save(dispatch);
            return;
        }
        if (!CommunityDispatchConstants.isRedispatchableDispatchStatus(existing.getNxCodDispatchStatus())) {
            throw new IllegalStateException("订单#" + order.getNxCommunityOrdersId() + "不可重复绑定派单");
        }
        existing.setNxCodCommunityId(communityId);
        existing.setNxCodDispatchStatus(CommunityDispatchConstants.DISPATCH_STATUS_LOADING);
        existing.setNxCodDispatchStopId(stopId);
        existing.setNxCodAssignedDriverUserId(driverUserId);
        existing.setNxCodRouteDate(routeDate);
        nxCommunityOrderDispatchDao.update(existing);
    }

    private String buildCustomerLabel(NxCommunityOrdersEntity order) {
        return "订单#" + order.getNxCommunityOrdersId();
    }
}
