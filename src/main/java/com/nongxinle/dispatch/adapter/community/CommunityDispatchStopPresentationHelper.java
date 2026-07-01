package com.nongxinle.dispatch.adapter.community;

import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.dispatch.core.view.DispatchStopCard;
import com.nongxinle.entity.NxCommunityDispatchStopEntity;
import com.nongxinle.entity.NxCommunityOrdersEntity;
import com.nongxinle.entity.NxCustomerUserAddressEntity;
import com.nongxinle.route.DisRouteSandboxDisplayFormatHelper;
import com.nongxinle.route.DisRouteSandboxTodayTimelineBuilder;
import com.nongxinle.route.DisRouteTemporalHelper;

import java.util.Date;
import java.util.List;

/** 社区派单站点卡展示：对齐 Nx DispatchStopCardTemplate 字段契约。 */
public final class CommunityDispatchStopPresentationHelper {

    private static final long METERS_PER_SECOND = 12L;
    private static final int DEFAULT_SERVICE_MINUTES = 3;

    private CommunityDispatchStopPresentationHelper() {
    }

    public static String resolveCustomerName(NxCustomerUserAddressEntity address,
                                             NxCommunityOrdersEntity firstOrder) {
        if (address != null && address.getNxCuaUserName() != null
                && !address.getNxCuaUserName().trim().isEmpty()) {
            return address.getNxCuaUserName().trim();
        }
        if (address != null && address.getNxCuaAddressBuildingName() != null
                && !address.getNxCuaAddressBuildingName().trim().isEmpty()) {
            return address.getNxCuaAddressBuildingName().trim();
        }
        if (firstOrder != null && firstOrder.getNxCommunityOrdersId() != null) {
            return "订单#" + firstOrder.getNxCommunityOrdersId();
        }
        return "客户";
    }

    public static String buildGoodsSummaryFromOrders(List<NxCommunityOrdersEntity> orders) {
        if (orders == null || orders.isEmpty()) {
            return "";
        }
        if (orders.size() == 1) {
            return "订单#" + orders.get(0).getNxCommunityOrdersId();
        }
        Integer minId = null;
        Integer maxId = null;
        for (NxCommunityOrdersEntity order : orders) {
            if (order == null || order.getNxCommunityOrdersId() == null) {
                continue;
            }
            Integer id = order.getNxCommunityOrdersId();
            if (minId == null || id < minId) {
                minId = id;
            }
            if (maxId == null || id > maxId) {
                maxId = id;
            }
        }
        if (minId == null) {
            return orders.size() + " 单";
        }
        if (minId.equals(maxId)) {
            return orders.size() + " 单 · 订单#" + minId;
        }
        return orders.size() + " 单 · 订单#" + minId + "~#" + maxId;
    }

    public static void applyUnassignedStopPresentation(
            DispatchStopCard card,
            NxCustomerUserAddressEntity address,
            List<NxCommunityOrdersEntity> orders,
            CommunityDispatchSandboxResult result) {
        if (card == null || orders == null || orders.isEmpty()) {
            return;
        }
        NxCommunityOrdersEntity firstOrder = orders.get(0);
        card.setCustomerName(resolveCustomerName(address, firstOrder));
        card.setGoodsSummary(buildGoodsSummaryFromOrders(orders));
        if (address != null) {
            card.setAddressText(buildAddressText(address));
            card.setLat(parseCoordinate(address.getNxCuaLat()));
            card.setLng(parseCoordinate(address.getNxCuaLng()));
            card.setCustomerPhone(address.getNxCuaUserPhone());
        }

        Double depotLat = parseCoordinate(result != null ? result.getDepotLat() : null);
        Double depotLng = parseCoordinate(result != null ? result.getDepotLng() : null);
        Double stopLat = card.getLat();
        Double stopLng = card.getLng();
        if (depotLat != null && depotLng != null && stopLat != null && stopLng != null) {
            long legDistanceM = straightLineMeters(depotLat, depotLng, stopLat, stopLng);
            long legDurationS = Math.max(1L, legDistanceM / METERS_PER_SECOND);
            String distanceText = DisRouteSandboxDisplayFormatHelper.formatDistanceText(legDistanceM);
            String durationText = DisRouteSandboxDisplayFormatHelper.formatDurationText(legDurationS);
            String legText = DisRouteSandboxTodayTimelineBuilder.joinLegText(distanceText, durationText);
            card.setLegText(legText);
            card.setDistanceText(distanceText);
            card.setDurationText(durationText);

            Date now = new Date();
            String routeDate = result != null ? result.getRouteDate() : null;
            Date arrivalAt = new Date(now.getTime() + legDurationS * 1000L);
            Date departureAt = new Date(arrivalAt.getTime() + DEFAULT_SERVICE_MINUTES * 60L * 1000L);
            card.setPlannedArrivalLabel(DisRouteTemporalHelper.formatRouteTimeLabel(arrivalAt, now, routeDate));
            card.setPlannedDepartureLabel(DisRouteTemporalHelper.formatRouteTimeLabel(departureAt, now, routeDate));
            card.setServiceDurationLabel(DEFAULT_SERVICE_MINUTES + "分钟");
            applyCustomerServiceWindow(card, orders, result != null ? result.getRouteDate() : null, arrivalAt);
        }
    }

    public static void applyStopServiceWindow(
            com.nongxinle.dispatch.core.view.DispatchTimelineItem node,
            NxCommunityDispatchStopEntity stop,
            String routeDate,
            Date plannedArrivalAt) {
        if (node == null || stop == null) {
            return;
        }
        CommunityDispatchOrderServiceTimeHelper.RequestedServiceTime requested =
                CommunityDispatchOrderServiceTimeHelper.resolveFromStopFields(
                        stop.getNxCdsServiceDate(), stop.getNxCdsServiceTime(), routeDate);
        applyServiceWindowFields(node, requested, plannedArrivalAt);
    }

    private static void applyCustomerServiceWindow(
            DispatchStopCard card,
            List<NxCommunityOrdersEntity> orders,
            String routeDate,
            Date plannedArrivalAt) {
        CommunityDispatchOrderServiceTimeHelper.RequestedServiceTime requested =
                CommunityDispatchOrderServiceTimeHelper.resolveStrictestFromOrders(orders, routeDate);
        applyServiceWindowFields(card, requested, plannedArrivalAt);
    }

    private static void applyServiceWindowFields(
            DispatchStopCard target,
            CommunityDispatchOrderServiceTimeHelper.RequestedServiceTime requested,
            Date plannedArrivalAt) {
        if (target == null) {
            return;
        }
        if (requested != null && requested.isPresent()) {
            target.setWindowRequirementLabel(
                    CommunityDispatchOrderServiceTimeHelper.formatWindowRequirementLabel(requested));
            target.setCustomerWindowLabel(
                    CommunityDispatchOrderServiceTimeHelper.formatCustomerWindowLabel(requested));
            target.setWindowRequirementModified(Boolean.FALSE);
        }
        if (plannedArrivalAt != null && requested != null && requested.isPresent()) {
            CommunityDispatchOrderServiceTimeHelper.ArrivalWindowStatus status =
                    CommunityDispatchOrderServiceTimeHelper.resolveArrivalWindowStatus(
                            plannedArrivalAt, requested);
            if (status.isPresent()) {
                target.setArrivalStatusLabel(status.getLabel());
                target.setArrivalStatusTone(status.getTone());
            }
        }
    }

    private static void applyServiceWindowFields(
            com.nongxinle.dispatch.core.view.DispatchTimelineItem target,
            CommunityDispatchOrderServiceTimeHelper.RequestedServiceTime requested,
            Date plannedArrivalAt) {
        if (target == null) {
            return;
        }
        if (requested != null && requested.isPresent()) {
            target.setWindowRequirementLabel(
                    CommunityDispatchOrderServiceTimeHelper.formatWindowRequirementLabel(requested));
            target.setCustomerWindowLabel(
                    CommunityDispatchOrderServiceTimeHelper.formatCustomerWindowLabel(requested));
            target.setWindowRequirementModified(Boolean.FALSE);
        }
        if (plannedArrivalAt != null && requested != null && requested.isPresent()) {
            CommunityDispatchOrderServiceTimeHelper.ArrivalWindowStatus status =
                    CommunityDispatchOrderServiceTimeHelper.resolveArrivalWindowStatus(
                            plannedArrivalAt, requested);
            if (status.isPresent()) {
                target.setArrivalStatusLabel(status.getLabel());
                target.setArrivalStatusTone(status.getTone());
            }
        }
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
        return sb.length() > 0 ? sb.toString() : null;
    }

    private static long straightLineMeters(double lat1, double lng1, double lat2, double lng2) {
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);
        double dLat = rLat2 - rLat1;
        double dLng = Math.toRadians(lng2 - lng1);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(rLat1) * Math.cos(rLat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return Math.max(1L, Math.round(2 * 6371000D * Math.asin(Math.min(1.0, Math.sqrt(h)))));
    }

    private static Double parseCoordinate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
