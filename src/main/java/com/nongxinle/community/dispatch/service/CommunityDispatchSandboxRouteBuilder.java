package com.nongxinle.community.dispatch.service;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.dao.NxCustomerUserAddressDao;
import com.nongxinle.dispatch.adapter.community.CommunityDispatchOrderServiceTimeHelper;
import com.nongxinle.entity.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 将 eligible 模拟站合并进司机路线卡（对齐 Nx SandboxProposalPlan → suggestedRoutes）。
 */
final class CommunityDispatchSandboxRouteBuilder {

    private CommunityDispatchSandboxRouteBuilder() {
    }

    static void buildSuggestedRoutes(CommunityDispatchSandboxResult result,
                                     NxCustomerUserAddressDao addressDao) {
        Map<Integer, NxCommunityDispatchDriverRouteEntity> routeByDriver =
                new LinkedHashMap<Integer, NxCommunityDispatchDriverRouteEntity>();
        if (result.getConfirmedRoutes() != null) {
            for (NxCommunityDispatchDriverRouteEntity confirmed : result.getConfirmedRoutes()) {
                if (confirmed == null || confirmed.getNxCddrDriverUserId() == null) {
                    continue;
                }
                if (!CommunityDispatchConstants.isSandboxScopeRoute(confirmed)) {
                    continue;
                }
                NxCommunityDispatchDriverRouteEntity sandboxRoute = cloneRouteWithSandboxStopsOnly(confirmed);
                if (sandboxRoute.getStops() == null || sandboxRoute.getStops().isEmpty()) {
                    continue;
                }
                routeByDriver.put(confirmed.getNxCddrDriverUserId(), sandboxRoute);
            }
        }

        Map<Integer, List<NxCommunityOrdersEntity>> unassigned = new LinkedHashMap<Integer, List<NxCommunityOrdersEntity>>();
        List<NxCommunityUserEntity> drivers = result.getAvailableDrivers();
        boolean hasDrivers = drivers != null && !drivers.isEmpty();

        if (result.getSimulatedStopGroups() != null) {
            for (Map.Entry<Integer, List<NxCommunityOrdersEntity>> entry
                    : result.getSimulatedStopGroups().entrySet()) {
                Integer addressId = entry.getKey();
                List<NxCommunityOrdersEntity> orders = entry.getValue();
                if (addressId == null || orders == null || orders.isEmpty()) {
                    continue;
                }
                Integer driverUserId = result.getSimulatedDriverByAddress().get(addressId);
                if (!hasDrivers || driverUserId == null) {
                    unassigned.put(addressId, orders);
                    continue;
                }
                NxCommunityDispatchDriverRouteEntity route = routeByDriver.get(driverUserId);
                if (route == null) {
                    route = createVirtualRoute(result, driverUserId);
                    routeByDriver.put(driverUserId, route);
                }
                int nextSeq = nextRouteSeq(route.getStops());
                route.getStops().add(buildSimulatedStop(result, addressId, orders, driverUserId, nextSeq, addressDao));
            }
        }

        List<NxCommunityDispatchDriverRouteEntity> suggested = new ArrayList<NxCommunityDispatchDriverRouteEntity>();
        for (NxCommunityDispatchDriverRouteEntity route : routeByDriver.values()) {
            sortStops(route.getStops());
            if (route.getStops() != null && !route.getStops().isEmpty()) {
                suggested.add(route);
            }
        }
        result.setSuggestedRoutes(suggested);
        result.setUnassignedStopGroups(unassigned);
    }

    private static NxCommunityDispatchDriverRouteEntity cloneRouteWithStops(
            NxCommunityDispatchDriverRouteEntity source) {
        NxCommunityDispatchDriverRouteEntity copy = new NxCommunityDispatchDriverRouteEntity();
        copy.setNxCommunityDispatchDriverRouteId(source.getNxCommunityDispatchDriverRouteId());
        copy.setNxCddrPlanId(source.getNxCddrPlanId());
        copy.setNxCddrCommunityId(source.getNxCddrCommunityId());
        copy.setNxCddrDriverUserId(source.getNxCddrDriverUserId());
        copy.setNxCddrRouteStatus(source.getNxCddrRouteStatus());
        copy.setNxCddrStopCount(source.getNxCddrStopCount());
        copy.setNxCddrLoadingEnteredAt(source.getNxCddrLoadingEnteredAt());
        copy.setNxCddrActualDepartAt(source.getNxCddrActualDepartAt());
        copy.setStops(source.getStops() != null
                ? new ArrayList<NxCommunityDispatchStopEntity>(source.getStops())
                : new ArrayList<NxCommunityDispatchStopEntity>());
        return copy;
    }

    private static NxCommunityDispatchDriverRouteEntity cloneRouteWithSandboxStopsOnly(
            NxCommunityDispatchDriverRouteEntity source) {
        NxCommunityDispatchDriverRouteEntity copy = cloneRouteWithStops(source);
        List<NxCommunityDispatchStopEntity> sandboxStops = new ArrayList<NxCommunityDispatchStopEntity>();
        if (source.getStops() != null) {
            for (NxCommunityDispatchStopEntity stop : source.getStops()) {
                if (CommunityDispatchConstants.isSandboxScopeStop(stop)) {
                    sandboxStops.add(stop);
                }
            }
        }
        copy.setStops(sandboxStops);
        copy.setNxCddrStopCount(sandboxStops.size());
        return copy;
    }

    private static NxCommunityDispatchDriverRouteEntity createVirtualRoute(
            CommunityDispatchSandboxResult result,
            Integer driverUserId) {
        NxCommunityDispatchDriverRouteEntity route = new NxCommunityDispatchDriverRouteEntity();
        route.setNxCddrCommunityId(result.getCommunityId());
        route.setNxCddrDriverUserId(driverUserId);
        route.setNxCddrRouteStatus(CommunityDispatchConstants.ROUTE_STATUS_DRAFT);
        route.setStops(new ArrayList<NxCommunityDispatchStopEntity>());
        return route;
    }

    private static NxCommunityDispatchStopEntity buildSimulatedStop(
            CommunityDispatchSandboxResult result,
            Integer addressId,
            List<NxCommunityOrdersEntity> orders,
            Integer driverUserId,
            int routeSeq,
            NxCustomerUserAddressDao addressDao) {
        NxCustomerUserAddressEntity address = addressDao != null ? addressDao.queryObject(addressId) : null;
        NxCommunityOrdersEntity firstOrder = orders.get(0);

        NxCommunityDispatchStopEntity stop = new NxCommunityDispatchStopEntity();
        stop.setNxCdsCommunityId(result.getCommunityId());
        stop.setNxCdsAddressId(addressId);
        stop.setNxCdsAssignedDriverUserId(driverUserId);
        stop.setNxCdsStopStatus(CommunityDispatchConstants.STOP_STATUS_SANDBOX);
        stop.setNxCdsRouteSeq(routeSeq);
        stop.setNxCdsOrderCount(orders.size());
        stop.setNxCdsServiceDate(firstOrder.getNxCoServiceDate());
        stop.setNxCdsServiceTime(
                CommunityDispatchOrderServiceTimeHelper.resolveServiceTimeRaw(firstOrder));
        if (address != null) {
            stop.setNxCdsCustomerName(address.getNxCuaUserName());
            stop.setNxCdsCustomerPhone(address.getNxCuaUserPhone());
            stop.setNxCdsLat(address.getNxCuaLat());
            stop.setNxCdsLng(address.getNxCuaLng());
            stop.setNxCdsAddressText(buildAddressText(address));
        } else {
            stop.setNxCdsCustomerName(buildCustomerLabel(firstOrder));
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
        return stop;
    }

    private static int nextRouteSeq(List<NxCommunityDispatchStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return 1;
        }
        int max = 0;
        for (NxCommunityDispatchStopEntity stop : stops) {
            if (stop != null && stop.getNxCdsRouteSeq() != null) {
                max = Math.max(max, stop.getNxCdsRouteSeq());
            }
        }
        return max + 1;
    }

    private static void sortStops(List<NxCommunityDispatchStopEntity> stops) {
        if (stops == null || stops.size() <= 1) {
            return;
        }
        Collections.sort(stops, new Comparator<NxCommunityDispatchStopEntity>() {
            @Override
            public int compare(NxCommunityDispatchStopEntity a, NxCommunityDispatchStopEntity b) {
                int seqA = a != null && a.getNxCdsRouteSeq() != null ? a.getNxCdsRouteSeq() : Integer.MAX_VALUE;
                int seqB = b != null && b.getNxCdsRouteSeq() != null ? b.getNxCdsRouteSeq() : Integer.MAX_VALUE;
                return Integer.compare(seqA, seqB);
            }
        });
    }

    private static String buildAddressText(NxCustomerUserAddressEntity address) {
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

    private static String buildCustomerLabel(NxCommunityOrdersEntity order) {
        return "订单#" + order.getNxCommunityOrdersId();
    }
}
