package com.nongxinle.community.dispatch.service;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.entity.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CommunityDispatchPageAssembler {

    public Map<String, Object> assemble(CommunityDispatchSandboxResult result) {
        Map<String, Object> vm = new LinkedHashMap<>();
        vm.put("pageMode", result.getPageMode());
        vm.put("communityId", result.getCommunityId());
        vm.put("routeDate", result.getRouteDate());
        vm.put("statusLabel", buildStatusLabel(result));
        vm.put("depot", buildDepot(result));
        vm.put("availableDrivers", buildDrivers(result.getAvailableDrivers()));
        vm.put("eligibleOrderCount", result.getEligibleOrders() != null ? result.getEligibleOrders().size() : 0);
        vm.put("unassignedStops", buildSimulatedStops(result));
        vm.put("driverRoutes", buildConfirmedRoutes(result));
        vm.put("confirmedStopCount", result.getConfirmedStops() != null ? result.getConfirmedStops().size() : 0);
        return vm;
    }

    private String buildStatusLabel(CommunityDispatchSandboxResult result) {
        if (CommunityDispatchConstants.PAGE_MODE_SANDBOX.equals(result.getPageMode())) {
            int sim = result.getSimulatedStopGroups() != null ? result.getSimulatedStopGroups().size() : 0;
            int confirmed = result.getConfirmedStops() != null ? result.getConfirmedStops().size() : 0;
            return "待派单 " + sim + " 站 · 已确认 " + confirmed + " 站";
        }
        if (CommunityDispatchConstants.PAGE_MODE_LOADING.equals(result.getPageMode())) {
            return "装车中";
        }
        return "配送中";
    }

    private Map<String, Object> buildDepot(CommunityDispatchSandboxResult result) {
        Map<String, Object> depot = new LinkedHashMap<>();
        depot.put("lat", result.getDepotLat());
        depot.put("lng", result.getDepotLng());
        return depot;
    }

    private List<Map<String, Object>> buildDrivers(List<NxCommunityUserEntity> drivers) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (drivers == null) {
            return list;
        }
        for (NxCommunityUserEntity driver : drivers) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("driverUserId", driver.getNxCommunityUserId());
            map.put("driverName", driver.getNxCouWxNickName());
            map.put("driverPhone", driver.getNxCouWxPhone());
            list.add(map);
        }
        return list;
    }

    private List<Map<String, Object>> buildSimulatedStops(CommunityDispatchSandboxResult result) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (result.getSimulatedStopGroups() == null) {
            return list;
        }
        for (Map.Entry<Integer, List<NxCommunityOrdersEntity>> entry : result.getSimulatedStopGroups().entrySet()) {
            Integer addressId = entry.getKey();
            List<NxCommunityOrdersEntity> orders = entry.getValue();
            if (orders == null || orders.isEmpty()) {
                continue;
            }
            Map<String, Object> stop = new LinkedHashMap<>();
            stop.put("simulated", true);
            stop.put("sandboxStopKey", CommunityDispatchConstants.sandboxKeyForAddress(addressId));
            stop.put("addressId", addressId);
            stop.put("suggestedDriverUserId", result.getSimulatedDriverByAddress().get(addressId));
            stop.put("orderCount", orders.size());
            stop.put("orderIds", collectOrderIds(orders));
            stop.put("customerLabel", buildCustomerLabel(orders.get(0)));
            stop.put("serviceTime", orders.get(0).getNxCoServiceTime());
            stop.put("primaryAction", buildPrimaryAction("确认分配", true));
            list.add(stop);
        }
        return list;
    }

    private List<Map<String, Object>> buildConfirmedRoutes(CommunityDispatchSandboxResult result) {
        List<Map<String, Object>> routes = new ArrayList<>();
        if (result.getConfirmedRoutes() == null) {
            return routes;
        }
        for (NxCommunityDispatchDriverRouteEntity route : result.getConfirmedRoutes()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("simulated", false);
            map.put("driverRouteId", route.getNxCommunityDispatchDriverRouteId());
            map.put("driverUserId", route.getNxCddrDriverUserId());
            map.put("routeStatus", route.getNxCddrRouteStatus());
            map.put("routeStatusLabel", routeStatusLabel(route.getNxCddrRouteStatus()));
            List<Map<String, Object>> persistedStops = buildPersistedStops(route.getStops());
            map.put("stopCount", persistedStops.size());
            map.put("loadingEnteredAt", route.getNxCddrLoadingEnteredAt());
            map.put("actualDepartAt", route.getNxCddrActualDepartAt());
            map.put("stops", persistedStops);
            if (CommunityDispatchConstants.PAGE_MODE_LOADING.equals(result.getPageMode())) {
                map.put("showDepartAction", route.getNxCddrActualDepartAt() == null);
            }
            routes.add(map);
        }
        return routes;
    }

    private List<Map<String, Object>> buildPersistedStops(List<NxCommunityDispatchStopEntity> stops) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (stops == null) {
            return list;
        }
        int displaySeq = 1;
        for (NxCommunityDispatchStopEntity stop : stops) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("simulated", false);
            map.put("stopId", stop.getNxCommunityDispatchStopId());
            map.put("sandboxStopKey", CommunityDispatchConstants.sandboxKeyForAddress(stop.getNxCdsAddressId()));
            map.put("addressId", stop.getNxCdsAddressId());
            map.put("stopStatus", stop.getNxCdsStopStatus());
            map.put("stopStatusLabel", stopStatusLabel(stop.getNxCdsStopStatus()));
            map.put("routeSeq", displaySeq++);
            map.put("customerName", stop.getNxCdsCustomerName());
            map.put("customerPhone", stop.getNxCdsCustomerPhone());
            map.put("addressText", stop.getNxCdsAddressText());
            map.put("lat", stop.getNxCdsLat());
            map.put("lng", stop.getNxCdsLng());
            map.put("orderCount", stop.getNxCdsOrderCount());
            map.put("items", buildStopItems(stop.getItems()));
            map.put("primaryAction", buildStopPrimaryAction(stop));
            list.add(map);
        }
        return list;
    }

    private List<Map<String, Object>> buildStopItems(List<NxCommunityDispatchStopItemEntity> items) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (items == null) {
            return list;
        }
        for (NxCommunityDispatchStopItemEntity item : items) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("orderId", item.getNxCdsiCommunityOrderId());
            map.put("goodsSummary", item.getNxCdsiGoodsSummary());
            map.put("orderTotal", item.getNxCdsiOrderTotal());
            list.add(map);
        }
        return list;
    }

    private Map<String, Object> buildStopPrimaryAction(NxCommunityDispatchStopEntity stop) {
        if (CommunityDispatchConstants.STOP_STATUS_IN_DELIVERY.equals(stop.getNxCdsStopStatus())) {
            return buildPrimaryAction("确认送达", true);
        }
        if (CommunityDispatchConstants.STOP_STATUS_LOADING.equals(stop.getNxCdsStopStatus())
                || CommunityDispatchConstants.STOP_STATUS_ASSIGNED.equals(stop.getNxCdsStopStatus())) {
            return buildPrimaryAction("查看订单", true);
        }
        if (CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(stop.getNxCdsStopStatus())) {
            return buildPrimaryAction("已送达", false);
        }
        return buildPrimaryAction("查看", true);
    }

    private Map<String, Object> buildPrimaryAction(String label, boolean enabled) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("label", label);
        action.put("enabled", enabled);
        return action;
    }

    private List<Integer> collectOrderIds(List<NxCommunityOrdersEntity> orders) {
        List<Integer> ids = new ArrayList<>();
        for (NxCommunityOrdersEntity order : orders) {
            ids.add(order.getNxCommunityOrdersId());
        }
        return ids;
    }

    private String buildCustomerLabel(NxCommunityOrdersEntity order) {
        return "订单#" + order.getNxCommunityOrdersId();
    }

    private String routeStatusLabel(String status) {
        if (CommunityDispatchConstants.ROUTE_STATUS_LOADING.equals(status)) {
            return "装车中";
        }
        if (CommunityDispatchConstants.ROUTE_STATUS_IN_DELIVERY.equals(status)) {
            return "配送中";
        }
        if (CommunityDispatchConstants.ROUTE_STATUS_COMPLETED.equals(status)) {
            return "已完成";
        }
        return status != null ? status : "";
    }

    private String stopStatusLabel(String status) {
        if (CommunityDispatchConstants.STOP_STATUS_ASSIGNED.equals(status)) {
            return "已分配";
        }
        if (CommunityDispatchConstants.STOP_STATUS_LOADING.equals(status)) {
            return "装车中";
        }
        if (CommunityDispatchConstants.STOP_STATUS_IN_DELIVERY.equals(status)) {
            return "配送中";
        }
        if (CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(status)) {
            return "已送达";
        }
        return status != null ? status : "";
    }
}
