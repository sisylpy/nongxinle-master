package com.nongxinle.community.dispatch.service;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.community.dispatch.dto.CommunityDeliveryCompleteRequest;
import com.nongxinle.community.dispatch.dto.CommunityDriverDepartRequest;
import com.nongxinle.dao.*;
import com.nongxinle.entity.*;
import com.nongxinle.utils.NxCommunityTypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.nongxinle.utils.DateUtils.formatWhatDay;

@Service
public class CommunityDispatchExecutionService {

    @Autowired
    private CommunityDispatchComputeService communityDispatchComputeService;
    @Autowired
    private CommunityDispatchPageAssembler communityDispatchPageAssembler;
    @Autowired
    private NxCommunityDispatchPlanDao nxCommunityDispatchPlanDao;
    @Autowired
    private NxCommunityDispatchDriverRouteDao nxCommunityDispatchDriverRouteDao;
    @Autowired
    private NxCommunityDispatchStopDao nxCommunityDispatchStopDao;
    @Autowired
    private NxCommunityDispatchStopItemDao nxCommunityDispatchStopItemDao;
    @Autowired
    private NxCommunityOrderDispatchDao nxCommunityOrderDispatchDao;
    @Autowired
    private NxCommunityOrdersDao nxCommunityOrdersDao;

    @Transactional
    public Map<String, Object> departNow(Integer driverUserId, CommunityDriverDepartRequest request) {
        if (driverUserId == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }
        if (request != null && request.getDriverUserId() != null
                && !request.getDriverUserId().equals(driverUserId)) {
            throw new IllegalArgumentException("只能操作自己的路线");
        }
        String routeDate = normalizeRouteDate(request != null ? request.getRouteDate() : null);
        Integer communityId = request != null ? request.getCommunityId() : null;
        NxCommunityDispatchDriverRouteEntity route = resolveDriverRoute(communityId, routeDate, driverUserId);

        if (!driverUserId.equals(route.getNxCddrDriverUserId())) {
            throw new IllegalArgumentException("路线不属于当前司机");
        }
        if (!CommunityDispatchConstants.ROUTE_STATUS_LOADING.equals(route.getNxCddrRouteStatus())) {
            throw new IllegalArgumentException("当前路线不在装车状态，无法发车");
        }

        Date now = new Date();
        route.setNxCddrRouteStatus(CommunityDispatchConstants.ROUTE_STATUS_IN_DELIVERY);
        route.setNxCddrActualDepartAt(now);
        nxCommunityDispatchDriverRouteDao.update(route);

        List<NxCommunityDispatchStopEntity> stops = nxCommunityDispatchStopDao.queryByDriverRouteId(
                route.getNxCommunityDispatchDriverRouteId());
        for (NxCommunityDispatchStopEntity stop : stops) {
            if (CommunityDispatchConstants.STOP_STATUS_CANCELLED.equals(stop.getNxCdsStopStatus())
                    || CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(stop.getNxCdsStopStatus())) {
                continue;
            }
            if (!CommunityDispatchConstants.STOP_STATUS_LOADING.equals(stop.getNxCdsStopStatus())) {
                continue;
            }
            if (!driverUserId.equals(stop.getNxCdsAssignedDriverUserId())) {
                continue;
            }
            stop.setNxCdsStopStatus(CommunityDispatchConstants.STOP_STATUS_IN_DELIVERY);
            nxCommunityDispatchStopDao.update(stop);
            Map<String, Object> dispatchMap = new HashMap<>();
            dispatchMap.put("stopId", stop.getNxCommunityDispatchStopId());
            dispatchMap.put("dispatchStatus", CommunityDispatchConstants.DISPATCH_STATUS_IN_DELIVERY);
            nxCommunityOrderDispatchDao.updateStatusByStopId(dispatchMap);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pageViewModel", communityDispatchPageAssembler.assemble(
                communityDispatchComputeService.computeDriverDelivery(
                        route.getNxCddrCommunityId(), routeDate, driverUserId)));
        return data;
    }

    @Transactional
    public Map<String, Object> completeStopNow(Integer stopId, CommunityDeliveryCompleteRequest request) {
        NxCommunityDispatchStopEntity stop = nxCommunityDispatchStopDao.queryObject(stopId);
        if (stop == null) {
            throw new IllegalArgumentException("站点不存在");
        }

        Integer driverUserId = request != null && request.getDriverUserId() != null
                ? request.getDriverUserId() : stop.getNxCdsAssignedDriverUserId();
        if (driverUserId == null) {
            throw new IllegalArgumentException("缺少 driverUserId");
        }
        if (!driverUserId.equals(stop.getNxCdsAssignedDriverUserId())) {
            throw new IllegalArgumentException("站点不属于当前司机");
        }
        if (request != null && request.getCommunityId() != null
                && !request.getCommunityId().equals(stop.getNxCdsCommunityId())) {
            throw new IllegalArgumentException("站点不属于当前门店");
        }
        if (!CommunityDispatchConstants.STOP_STATUS_IN_DELIVERY.equals(stop.getNxCdsStopStatus())) {
            throw new IllegalArgumentException("站点不在配送中，无法送达");
        }

        NxCommunityDispatchDriverRouteEntity route = null;
        if (stop.getNxCdsDriverRouteId() != null) {
            route = nxCommunityDispatchDriverRouteDao.queryObject(stop.getNxCdsDriverRouteId());
        }
        if (route == null) {
            throw new IllegalArgumentException("站点所属路线不存在");
        }
        if (!driverUserId.equals(route.getNxCddrDriverUserId())) {
            throw new IllegalArgumentException("路线不属于当前司机");
        }
        if (!CommunityDispatchConstants.ROUTE_STATUS_IN_DELIVERY.equals(route.getNxCddrRouteStatus())) {
            throw new IllegalArgumentException("路线不在配送中，无法送达");
        }

        List<NxCommunityDispatchStopItemEntity> items = nxCommunityDispatchStopItemDao.queryByStopId(stopId);
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("站点没有绑定订单");
        }

        List<NxCommunityOrdersEntity> ordersToComplete = new ArrayList<>();
        List<String> blockedReasons = new ArrayList<>();
        for (NxCommunityDispatchStopItemEntity item : items) {
            Integer orderId = item.getNxCdsiCommunityOrderId();
            NxCommunityOrdersEntity order = nxCommunityOrdersDao.queryObject(orderId);
            NxCommunityOrderDispatchEntity dispatch = nxCommunityOrderDispatchDao.queryByOrderId(orderId);
            String blocked = validateOrderForComplete(order, dispatch, stop);
            if (blocked != null) {
                blockedReasons.add(blocked);
            } else {
                ordersToComplete.add(order);
            }
        }
        if (!blockedReasons.isEmpty()) {
            throw new IllegalArgumentException("送达校验失败: " + String.join("; ", blockedReasons));
        }

        Date now = new Date();
        stop.setNxCdsStopStatus(CommunityDispatchConstants.STOP_STATUS_DELIVERED);
        stop.setNxCdsDeliveredAt(now);
        nxCommunityDispatchStopDao.update(stop);

        Map<String, Object> dispatchMap = new HashMap<>();
        dispatchMap.put("stopId", stopId);
        dispatchMap.put("dispatchStatus", CommunityDispatchConstants.DISPATCH_STATUS_DELIVERED);
        nxCommunityOrderDispatchDao.updateStatusByStopId(dispatchMap);

        for (NxCommunityOrdersEntity order : ordersToComplete) {
            order.setNxCoStatus(NxCommunityTypeUtils.NX_COMMUNITY_ORDER_STATUS_ORDER_FINISH);
            nxCommunityOrdersDao.update(order);
        }

        int pending = nxCommunityDispatchStopDao.countActiveByDriverRouteId(route.getNxCommunityDispatchDriverRouteId());
        if (pending == 0) {
            route.setNxCddrRouteStatus(CommunityDispatchConstants.ROUTE_STATUS_COMPLETED);
            nxCommunityDispatchDriverRouteDao.update(route);
        }

        String routeDate = request != null && request.getRouteDate() != null
                ? request.getRouteDate() : formatWhatDay(0);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pageViewModel", communityDispatchPageAssembler.assemble(
                communityDispatchComputeService.computeDriverDelivery(
                        stop.getNxCdsCommunityId(), routeDate, driverUserId)));
        return data;
    }

    private String validateOrderForComplete(NxCommunityOrdersEntity order,
                                            NxCommunityOrderDispatchEntity dispatch,
                                            NxCommunityDispatchStopEntity stop) {
        Integer orderId = order != null ? order.getNxCommunityOrdersId() : null;
        if (order == null) {
            return "订单#" + (dispatch != null ? dispatch.getNxCodCommunityOrderId() : "?") + "不存在";
        }
        if (!stop.getNxCdsCommunityId().equals(order.getNxCoCommunityId())) {
            return "订单#" + orderId + "不属于当前门店";
        }
        if (!Integer.valueOf(CommunityDispatchConstants.ORDER_SERVICE_TYPE_DELIVERY).equals(order.getNxCoServiceType())) {
            return "订单#" + orderId + "不是外卖配送单";
        }
        if (NxCommunityTypeUtils.NX_COMMUNITY_ORDER_STATUS_ORDER_CANCEL.equals(order.getNxCoStatus())) {
            return "订单#" + orderId + "已取消";
        }
        if (NxCommunityTypeUtils.NX_COMMUNITY_ORDER_STATUS_ORDER_FINISH.equals(order.getNxCoStatus())) {
            return "订单#" + orderId + "已完成";
        }
        if (dispatch == null) {
            return "订单#" + orderId + "缺少派单扩展记录";
        }
        if (!stop.getNxCommunityDispatchStopId().equals(dispatch.getNxCodDispatchStopId())) {
            return "订单#" + orderId + "不属于当前站点";
        }
        if (!CommunityDispatchConstants.DISPATCH_STATUS_IN_DELIVERY.equals(dispatch.getNxCodDispatchStatus())) {
            return "订单#" + orderId + "派单状态不是 IN_DELIVERY";
        }
        return null;
    }

    private NxCommunityDispatchDriverRouteEntity resolveDriverRoute(Integer communityId, String routeDate,
                                                                    Integer driverUserId) {
        Map<String, Object> planMap = new HashMap<>();
        planMap.put("communityId", communityId);
        planMap.put("routeDate", routeDate);
        NxCommunityDispatchPlanEntity plan = nxCommunityDispatchPlanDao.queryByCommunityAndRouteDate(planMap);
        if (plan == null) {
            throw new IllegalArgumentException("当日派单计划不存在");
        }
        Map<String, Object> routeMap = new HashMap<>();
        routeMap.put("planId", plan.getNxCommunityDispatchPlanId());
        routeMap.put("driverUserId", driverUserId);
        NxCommunityDispatchDriverRouteEntity route = nxCommunityDispatchDriverRouteDao.queryByPlanAndDriver(routeMap);
        if (route == null) {
            throw new IllegalArgumentException("司机路线不存在");
        }
        return route;
    }

    private String normalizeRouteDate(String routeDate) {
        if (routeDate == null || routeDate.trim().isEmpty()) {
            return formatWhatDay(0);
        }
        return routeDate;
    }
}
