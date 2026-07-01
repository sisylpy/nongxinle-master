package com.nongxinle.community.dispatch.service;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.dao.NxCustomerUserAddressDao;
import com.nongxinle.dispatch.adapter.community.CommunityDispatchOrderServiceTimeHelper;
import com.nongxinle.dispatch.adapter.community.CommunityDispatchRouteEditActionHelper;
import com.nongxinle.entity.NxCommunityDispatchDriverRouteEntity;
import com.nongxinle.entity.NxCommunityDispatchStopEntity;
import com.nongxinle.entity.NxCommunityDispatchStopItemEntity;
import com.nongxinle.entity.NxCommunityOrdersEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 路线编辑客户池：沙盘各司机建议站 + 未分配（对齐 NX TodayDispatchRouteEditStopPool）。 */
final class CommunityDispatchRouteEditStopPool {

    private CommunityDispatchRouteEditStopPool() {
    }

    static Map<String, NxCommunityDispatchStopEntity> indexByStopKey(
            CommunityDispatchSandboxResult result,
            NxCustomerUserAddressDao addressDao) {
        Map<String, NxCommunityDispatchStopEntity> map = new LinkedHashMap<String, NxCommunityDispatchStopEntity>();
        for (NxCommunityDispatchStopEntity stop : collectAllSandboxStops(result, addressDao)) {
            String stopKey = CommunityDispatchRouteEditActionHelper.resolveStopKey(stop);
            if (stopKey != null && !map.containsKey(stopKey)) {
                map.put(stopKey, stop);
            }
        }
        return map;
    }

    static List<NxCommunityDispatchStopEntity> listAddableStops(
            CommunityDispatchSandboxResult result,
            NxCommunityDispatchDriverRouteEntity currentRoute,
            NxCustomerUserAddressDao addressDao) {
        Set<Integer> onRouteAddressIds = collectOnRouteAddressIds(currentRoute);
        List<NxCommunityDispatchStopEntity> addable = new ArrayList<NxCommunityDispatchStopEntity>();
        LinkedHashSet<Integer> seenAddressIds = new LinkedHashSet<Integer>();
        for (NxCommunityDispatchStopEntity stop : collectAllSandboxStops(result, addressDao)) {
            if (stop == null || stop.getNxCdsAddressId() == null) {
                continue;
            }
            if (onRouteAddressIds.contains(stop.getNxCdsAddressId())) {
                continue;
            }
            if (stop.getNxCommunityDispatchStopId() != null) {
                continue;
            }
            if (!seenAddressIds.add(stop.getNxCdsAddressId())) {
                continue;
            }
            addable.add(stop);
        }
        return addable;
    }

    static NxCommunityDispatchDriverRouteEntity buildRouteFromStopKeys(
            CommunityDispatchSandboxResult result,
            NxCommunityDispatchDriverRouteEntity baseRoute,
            List<String> stopKeys,
            NxCustomerUserAddressDao addressDao) {
        Map<String, NxCommunityDispatchStopEntity> stopByKey = indexByStopKey(result, addressDao);
        List<NxCommunityDispatchStopEntity> reordered = new ArrayList<NxCommunityDispatchStopEntity>();
        int seq = 1;
        Integer driverUserId = baseRoute != null ? baseRoute.getNxCddrDriverUserId() : null;
        for (String stopKey : stopKeys) {
            NxCommunityDispatchStopEntity stop = stopByKey.get(stopKey);
            if (stop == null) {
                throw new IllegalArgumentException("无效 stopKey: " + stopKey);
            }
            NxCommunityDispatchStopEntity copy = cloneStopForEdit(stop);
            copy.setNxCdsRouteSeq(seq++);
            if (driverUserId != null) {
                copy.setNxCdsAssignedDriverUserId(driverUserId);
            }
            reordered.add(copy);
        }
        NxCommunityDispatchDriverRouteEntity route = new NxCommunityDispatchDriverRouteEntity();
        if (baseRoute != null) {
            route.setNxCommunityDispatchDriverRouteId(baseRoute.getNxCommunityDispatchDriverRouteId());
            route.setNxCddrPlanId(baseRoute.getNxCddrPlanId());
            route.setNxCddrCommunityId(baseRoute.getNxCddrCommunityId());
            route.setNxCddrDriverUserId(baseRoute.getNxCddrDriverUserId());
            route.setNxCddrRouteStatus(baseRoute.getNxCddrRouteStatus());
            route.setNxCddrLoadingEnteredAt(baseRoute.getNxCddrLoadingEnteredAt());
            route.setNxCddrActualDepartAt(baseRoute.getNxCddrActualDepartAt());
            route.setNxCddrStopCount(reordered.size());
        }
        route.setStops(reordered);
        return route;
    }

    private static List<NxCommunityDispatchStopEntity> collectAllSandboxStops(
            CommunityDispatchSandboxResult result,
            NxCustomerUserAddressDao addressDao) {
        Map<Integer, NxCommunityDispatchStopEntity> byAddress = new LinkedHashMap<Integer, NxCommunityDispatchStopEntity>();
        if (result == null) {
            return new ArrayList<NxCommunityDispatchStopEntity>();
        }
        List<NxCommunityDispatchDriverRouteEntity> routes = result.resolveSandboxRoutes();
        if (routes != null) {
            for (NxCommunityDispatchDriverRouteEntity route : routes) {
                if (route == null || route.getStops() == null) {
                    continue;
                }
                for (NxCommunityDispatchStopEntity stop : route.getStops()) {
                    putByAddress(byAddress, stop);
                }
            }
        }
        if (result.getUnassignedStopGroups() != null) {
            for (Map.Entry<Integer, List<NxCommunityOrdersEntity>> entry
                    : result.getUnassignedStopGroups().entrySet()) {
                Integer addressId = entry.getKey();
                if (addressId == null || byAddress.containsKey(addressId)) {
                    continue;
                }
                List<NxCommunityOrdersEntity> orders = entry.getValue();
                if (orders == null || orders.isEmpty()) {
                    continue;
                }
                byAddress.put(addressId, buildUnassignedStop(result, addressId, orders, addressDao));
            }
        }
        return new ArrayList<NxCommunityDispatchStopEntity>(byAddress.values());
    }

    private static void putByAddress(Map<Integer, NxCommunityDispatchStopEntity> byAddress,
                                       NxCommunityDispatchStopEntity stop) {
        if (stop == null || stop.getNxCdsAddressId() == null) {
            return;
        }
        byAddress.putIfAbsent(stop.getNxCdsAddressId(), stop);
    }

    private static Set<Integer> collectOnRouteAddressIds(NxCommunityDispatchDriverRouteEntity currentRoute) {
        Set<Integer> onRoute = new LinkedHashSet<Integer>();
        if (currentRoute == null || currentRoute.getStops() == null) {
            return onRoute;
        }
        for (NxCommunityDispatchStopEntity stop : currentRoute.getStops()) {
            if (stop != null && stop.getNxCdsAddressId() != null) {
                onRoute.add(stop.getNxCdsAddressId());
            }
        }
        return onRoute;
    }

    private static NxCommunityDispatchStopEntity buildUnassignedStop(
            CommunityDispatchSandboxResult result,
            Integer addressId,
            List<NxCommunityOrdersEntity> orders,
            NxCustomerUserAddressDao addressDao) {
        NxCommunityOrdersEntity firstOrder = orders.get(0);
        NxCommunityDispatchStopEntity stop = new NxCommunityDispatchStopEntity();
        stop.setNxCdsCommunityId(result.getCommunityId());
        stop.setNxCdsAddressId(addressId);
        stop.setNxCdsStopStatus(CommunityDispatchConstants.STOP_STATUS_SANDBOX);
        stop.setNxCdsOrderCount(orders.size());
        stop.setNxCdsServiceDate(firstOrder.getNxCoServiceDate());
        stop.setNxCdsServiceTime(
                CommunityDispatchOrderServiceTimeHelper.resolveServiceTimeRaw(firstOrder));
        if (addressDao != null) {
            com.nongxinle.entity.NxCustomerUserAddressEntity address = addressDao.queryObject(addressId);
            if (address != null) {
                stop.setNxCdsCustomerName(address.getNxCuaUserName());
                stop.setNxCdsCustomerPhone(address.getNxCuaUserPhone());
                stop.setNxCdsLat(address.getNxCuaLat());
                stop.setNxCdsLng(address.getNxCuaLng());
                stop.setNxCdsAddressText(buildAddressText(address));
            }
        }
        if (stop.getNxCdsCustomerName() == null) {
            stop.setNxCdsCustomerName("订单#" + firstOrder.getNxCommunityOrdersId());
        }
        List<NxCommunityDispatchStopItemEntity> items = new ArrayList<NxCommunityDispatchStopItemEntity>();
        for (NxCommunityOrdersEntity order : orders) {
            NxCommunityDispatchStopItemEntity item = new NxCommunityDispatchStopItemEntity();
            item.setNxCdsiCommunityOrderId(order.getNxCommunityOrdersId());
            item.setNxCdsiGoodsSummary("订单#" + order.getNxCommunityOrdersId());
            item.setNxCdsiOrderTotal(order.getNxCoTotal());
            items.add(item);
        }
        stop.setItems(items);
        Integer suggestedDriver = result.getSimulatedDriverByAddress().get(addressId);
        stop.setNxCdsAssignedDriverUserId(suggestedDriver);
        return stop;
    }

    private static String buildAddressText(com.nongxinle.entity.NxCustomerUserAddressEntity address) {
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

    private static NxCommunityDispatchStopEntity cloneStopForEdit(NxCommunityDispatchStopEntity stop) {
        NxCommunityDispatchStopEntity copy = new NxCommunityDispatchStopEntity();
        copy.setNxCommunityDispatchStopId(stop.getNxCommunityDispatchStopId());
        copy.setNxCdsPlanId(stop.getNxCdsPlanId());
        copy.setNxCdsDriverRouteId(stop.getNxCdsDriverRouteId());
        copy.setNxCdsCommunityId(stop.getNxCdsCommunityId());
        copy.setNxCdsAddressId(stop.getNxCdsAddressId());
        copy.setNxCdsCustomerName(stop.getNxCdsCustomerName());
        copy.setNxCdsCustomerPhone(stop.getNxCdsCustomerPhone());
        copy.setNxCdsLat(stop.getNxCdsLat());
        copy.setNxCdsLng(stop.getNxCdsLng());
        copy.setNxCdsAddressText(stop.getNxCdsAddressText());
        copy.setNxCdsRouteSeq(stop.getNxCdsRouteSeq());
        copy.setNxCdsStopStatus(stop.getNxCdsStopStatus());
        copy.setNxCdsAssignedDriverUserId(stop.getNxCdsAssignedDriverUserId());
        copy.setNxCdsOrderCount(stop.getNxCdsOrderCount());
        copy.setItems(stop.getItems());
        return copy;
    }
}
