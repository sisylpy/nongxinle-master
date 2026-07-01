package com.nongxinle.dispatch.adapter.community;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.dao.NxCommunityDao;
import com.nongxinle.dispatch.core.domain.DispatchPageMode;
import com.nongxinle.dispatch.core.view.DispatchTimelineItem;
import com.nongxinle.entity.*;
import com.nongxinle.route.DisRouteSandboxDisplayFormatHelper;
import com.nongxinle.route.DisRouteSandboxTodayTimelineBuilder;
import com.nongxinle.route.DisRouteTemporalHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** DRIVER_ROUTE timeline：start → leg → stop → … → return leg → end。 */
@Component
public class CommunityDispatchRouteTimelineBuilder {

    private static final long METERS_PER_SECOND = 12L;
    private static final int DEFAULT_SERVICE_MINUTES = 3;

    @Autowired
    private NxCommunityDao nxCommunityDao;

    public List<DispatchTimelineItem> build(
            CommunityDispatchSandboxResult result,
            NxCommunityDispatchDriverRouteEntity route,
            DispatchPageMode pageMode,
            CommunityDispatchPageViewAdapter.AdapterOptions opts) {
        List<DispatchTimelineItem> timeline = new ArrayList<DispatchTimelineItem>();
        Double depotLat = parseCoordinate(result.getDepotLat());
        Double depotLng = parseCoordinate(result.getDepotLng());
        String depotName = resolveDepotName(result.getCommunityId());
        List<NxCommunityDispatchStopEntity> stops = sortStops(route.getStops());

        Date serverNow = new Date();
        String routeDate = result.getRouteDate();
        Date anchor = resolveTimelineAnchor(route, pageMode, opts, serverNow);
        String departLabel = route.getNxCddrActualDepartAt() != null
                && pageMode == DispatchPageMode.DELIVERY && opts.isDriverDeliveryMode()
                ? CommunityDispatchDriverTerminalPresentationHelper.formatExecutionTimeLabel(
                        route.getNxCddrActualDepartAt(), serverNow, routeDate)
                : "现在";

        timeline.add(buildStartNode(depotName, departLabel));
        if (stops.isEmpty()) {
            timeline.add(buildEndNode(depotName, routeDate, 0L, anchor));
            enrichTimelinePresentation(timeline);
            return timeline;
        }

        double prevLat = depotLat != null ? depotLat : 0D;
        double prevLng = depotLng != null ? depotLng : 0D;
        boolean hasPrevCoord = depotLat != null && depotLng != null;
        int displaySeq = 1;
        long totalDurationS = 0L;
        long cursorMs = anchor.getTime();

        for (NxCommunityDispatchStopEntity stop : stops) {
            Double lat = parseCoordinate(stop.getNxCdsLat());
            Double lng = parseCoordinate(stop.getNxCdsLng());
            if (hasPrevCoord && lat != null && lng != null) {
                LegMetrics leg = legMetrics(prevLat, prevLng, lat, lng);
                totalDurationS += leg.durationS;
                timeline.add(buildLegNode(prevLat, prevLng, lat, lng, null));
                cursorMs += leg.durationS * 1000L;
                prevLat = lat;
                prevLng = lng;
            } else if (lat != null && lng != null) {
                prevLat = lat;
                prevLng = lng;
                hasPrevCoord = true;
            }
            timeline.add(buildStopNode(stop, displaySeq++, pageMode, result, route, opts, cursorMs, serverNow, routeDate));
            cursorMs += DEFAULT_SERVICE_MINUTES * 60L * 1000L;
        }

        if (pageMode == DispatchPageMode.DELIVERY && opts.isDriverDeliveryMode()) {
            applyDeliveryTimelinePresentation(timeline, stops, route, serverNow, routeDate);
        }

        if (hasPrevCoord && depotLat != null && depotLng != null) {
            LegMetrics returnLeg = legMetrics(prevLat, prevLng, depotLat, depotLng);
            totalDurationS += returnLeg.durationS;
            timeline.add(buildLegNode(prevLat, prevLng, depotLat, depotLng, "RETURN"));
        }
        timeline.add(buildEndNode(depotName, routeDate, totalDurationS, anchor));
        enrichTimelinePresentation(timeline);
        return timeline;
    }

    private static Date resolveTimelineAnchor(
            NxCommunityDispatchDriverRouteEntity route,
            DispatchPageMode pageMode,
            CommunityDispatchPageViewAdapter.AdapterOptions opts,
            Date serverNow) {
        if (pageMode == DispatchPageMode.DELIVERY
                && opts.isDriverDeliveryMode()
                && route != null
                && route.getNxCddrActualDepartAt() != null) {
            return route.getNxCddrActualDepartAt();
        }
        return serverNow;
    }

    private void applyDeliveryTimelinePresentation(
            List<DispatchTimelineItem> timeline,
            List<NxCommunityDispatchStopEntity> stops,
            NxCommunityDispatchDriverRouteEntity route,
            Date serverNow,
            String routeDate) {
        List<NxCommunityDispatchStopEntity> orderedStops =
                CommunityDispatchDriverTerminalPresentationHelper.sortStops(stops);
        int currentStopIndex = CommunityDispatchDriverTerminalPresentationHelper
                .resolveCurrentDeliveryStopIndex(orderedStops);
        int stopNodeIndex = 0;
        for (DispatchTimelineItem node : timeline) {
            if (node == null || !"stop".equals(node.getType())) {
                continue;
            }
            NxCommunityDispatchStopEntity stop = stopNodeIndex < orderedStops.size()
                    ? orderedStops.get(stopNodeIndex) : null;
            CommunityDispatchDriverTerminalPresentationHelper.applyDeliveryStopPresentation(
                    node, stop, stopNodeIndex, orderedStops, route, currentStopIndex, serverNow, routeDate);
            stopNodeIndex++;
        }
    }

    /**
     * route-timeline 只渲染 stop/end；把前序 leg 文案挂到 stop/end 上。
     */
    private static void enrichTimelinePresentation(List<DispatchTimelineItem> timeline) {
        if (timeline == null || timeline.isEmpty()) {
            return;
        }
        String pendingLegText = null;
        String pendingLegDistance = null;
        for (DispatchTimelineItem item : timeline) {
            if (item == null || item.getType() == null) {
                continue;
            }
            if ("leg".equals(item.getType())) {
                pendingLegText = item.getLegText();
                pendingLegDistance = item.getLegDistanceLabel() != null
                        ? item.getLegDistanceLabel()
                        : item.getLegText();
                continue;
            }
            if ("stop".equals(item.getType())) {
                if (pendingLegText != null) {
                    item.setLegText(pendingLegText);
                    item.setLegDistanceLabel(pendingLegDistance);
                }
                pendingLegText = null;
                pendingLegDistance = null;
                continue;
            }
            if ("end".equals(item.getType()) && pendingLegText != null) {
                item.setLegText(pendingLegText);
                item.setLegDistanceLabel(pendingLegDistance);
            }
        }
    }

    private DispatchTimelineItem buildStartNode(String depotName, String departLabel) {
        DispatchTimelineItem node = new DispatchTimelineItem();
        node.setType("start");
        node.setName(depotName);
        node.setTitle(depotName);
        node.setTimeRight(formatDepartTimeRight(departLabel != null ? departLabel : "现在"));
        return node;
    }

    private DispatchTimelineItem buildLegNode(double fromLat, double fromLng, double toLat, double toLng,
                                                String legRole) {
        LegMetrics leg = legMetrics(fromLat, fromLng, toLat, toLng);
        DispatchTimelineItem node = new DispatchTimelineItem();
        node.setType("leg");
        node.setLegRole(legRole);
        node.setDistanceText(leg.distanceText);
        node.setDurationText(leg.durationText);
        node.setLegText(leg.legText);
        node.setLegDistanceLabel(leg.legText);
        return node;
    }

    private DispatchTimelineItem buildEndNode(String depotName, String routeDate, long totalDurationS, Date anchor) {
        Date anchorAt = anchor != null ? anchor : new Date();
        Date returnAt = totalDurationS > 0L
                ? new Date(anchorAt.getTime() + totalDurationS * 1000L) : anchorAt;
        String returnLabel = DisRouteTemporalHelper.formatRouteTimeLabel(returnAt, anchorAt, routeDate);

        DispatchTimelineItem node = new DispatchTimelineItem();
        node.setType("end");
        node.setMarker("终");
        node.setName("回仓");
        node.setTitle("回仓");
        node.setTimeRight(returnLabel);
        return node;
    }

    private DispatchTimelineItem buildStopNode(
            NxCommunityDispatchStopEntity stop,
            int displaySeq,
            DispatchPageMode pageMode,
            CommunityDispatchSandboxResult result,
            NxCommunityDispatchDriverRouteEntity route,
            CommunityDispatchPageViewAdapter.AdapterOptions opts,
            long arrivalMs,
            Date now,
            String routeDate) {
        DispatchTimelineItem node = new DispatchTimelineItem();
        node.setType("stop");
        node.setRouteSeq(displaySeq);
        String customerName = stop.getNxCdsCustomerName() != null
                ? stop.getNxCdsCustomerName()
                : (stop.getNxCdsAddressText() != null ? stop.getNxCdsAddressText() : "站点#" + stop.getNxCdsAddressId());
        node.setName(customerName);
        node.setTitle(customerName);
        node.setSubtitle(buildGoodsSummary(stop.getItems()));
        node.setStatus(stop.getNxCdsStopStatus());
        node.setStatusLabel(stopStatusLabel(stop.getNxCdsStopStatus()));
        node.setStopDone(CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(stop.getNxCdsStopStatus()));
        node.setCardToneClass(node.getStopDone() != null && node.getStopDone()
                ? "timeline-stop-done"
                : (CommunityDispatchConstants.STOP_STATUS_IN_DELIVERY.equals(stop.getNxCdsStopStatus())
                ? "" : "timeline-stop-pending"));
        node.setStopId(stop.getNxCommunityDispatchStopId());
        node.setDeliveryStopId(stop.getNxCommunityDispatchStopId());
        node.setStopKey(CommunityDispatchRouteEditActionHelper.resolveStopKey(stop));
        node.setAddressId(stop.getNxCdsAddressId());
        node.setNavLat(parseCoordinate(stop.getNxCdsLat()));
        node.setNavLng(parseCoordinate(stop.getNxCdsLng()));
        node.setNavAddress(stop.getNxCdsAddressText());

        Date arrivalAt = new Date(arrivalMs);
        String plannedArrivalLabel = DisRouteTemporalHelper.formatRouteTimeLabel(arrivalAt, now, routeDate);
        node.setPlannedArrivalLabel(plannedArrivalLabel);
        Date departureAt = new Date(arrivalMs + DEFAULT_SERVICE_MINUTES * 60L * 1000L);
        node.setPlannedDepartureLabel(DisRouteTemporalHelper.formatRouteTimeLabel(departureAt, now, routeDate));
        node.setServiceDurationLabel(DEFAULT_SERVICE_MINUTES + "分钟");
        CommunityDispatchStopPresentationHelper.applyStopServiceWindow(
                node, stop, routeDate, arrivalAt);

        if (pageMode == DispatchPageMode.DISPATCH_SANDBOX
                && CommunityDispatchConstants.isSandboxSimulatedStop(stop)) {
            node.setCardToneClass("timeline-stop-pending");
        }
        if (pageMode == DispatchPageMode.DELIVERY && opts.isDriverDeliveryMode()) {
            if (CommunityDispatchConstants.STOP_STATUS_IN_DELIVERY.equals(stop.getNxCdsStopStatus())) {
                node.setShowComplete(true);
            }
            if (CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(stop.getNxCdsStopStatus())) {
                node.setStopDone(true);
                node.setStatusLabel("已送达");
            }
        }
        return node;
    }

    private String formatDepartTimeRight(String plannedDepartLabel) {
        if (plannedDepartLabel == null || plannedDepartLabel.trim().isEmpty()) {
            return "现在 出发";
        }
        String trimmed = plannedDepartLabel.trim();
        if (trimmed.endsWith("出发")) {
            return trimmed;
        }
        return trimmed + " 出发";
    }

    private LegMetrics legMetrics(double lat1, double lng1, double lat2, double lng2) {
        long distanceM = straightLineMeters(lat1, lng1, lat2, lng2);
        long durationS = Math.max(1L, distanceM / METERS_PER_SECOND);
        String distanceText = DisRouteSandboxDisplayFormatHelper.formatDistanceText(distanceM);
        String durationText = DisRouteSandboxDisplayFormatHelper.formatDurationText(durationS);
        String legText = DisRouteSandboxTodayTimelineBuilder.joinLegText(distanceText, durationText);
        LegMetrics metrics = new LegMetrics();
        metrics.distanceM = distanceM;
        metrics.durationS = durationS;
        metrics.distanceText = distanceText;
        metrics.durationText = durationText;
        metrics.legText = legText;
        return metrics;
    }

    private String buildGoodsSummary(List<NxCommunityDispatchStopItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        if (items.size() == 1) {
            NxCommunityDispatchStopItemEntity item = items.get(0);
            if (item.getNxCdsiGoodsSummary() != null && !item.getNxCdsiGoodsSummary().trim().isEmpty()) {
                return item.getNxCdsiGoodsSummary();
            }
            return "订单#" + item.getNxCdsiCommunityOrderId();
        }
        NxCommunityDispatchStopItemEntity first = items.get(0);
        String firstSummary = first.getNxCdsiGoodsSummary() != null && !first.getNxCdsiGoodsSummary().trim().isEmpty()
                ? first.getNxCdsiGoodsSummary()
                : ("订单#" + first.getNxCdsiCommunityOrderId());
        return items.size() + " 单 · " + firstSummary;
    }

    private String stopStatusLabel(String status) {
        if (CommunityDispatchConstants.STOP_STATUS_SANDBOX.equals(status)) {
            return null;
        }
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

    private String resolveDepotName(Integer communityId) {
        if (communityId == null) {
            return "门店";
        }
        NxCommunityEntity community = nxCommunityDao.queryObject(communityId);
        if (community == null || community.getNxCommunityName() == null
                || community.getNxCommunityName().trim().isEmpty()) {
            return "门店";
        }
        return community.getNxCommunityName();
    }

    private List<NxCommunityDispatchStopEntity> sortStops(List<NxCommunityDispatchStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        List<NxCommunityDispatchStopEntity> sorted = new ArrayList<NxCommunityDispatchStopEntity>(stops);
        Collections.sort(sorted, new Comparator<NxCommunityDispatchStopEntity>() {
            @Override
            public int compare(NxCommunityDispatchStopEntity a, NxCommunityDispatchStopEntity b) {
                int seqA = a.getNxCdsRouteSeq() != null ? a.getNxCdsRouteSeq() : Integer.MAX_VALUE;
                int seqB = b.getNxCdsRouteSeq() != null ? b.getNxCdsRouteSeq() : Integer.MAX_VALUE;
                return Integer.compare(seqA, seqB);
            }
        });
        return sorted;
    }

    private long straightLineMeters(double lat1, double lng1, double lat2, double lng2) {
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);
        double dLat = rLat2 - rLat1;
        double dLng = Math.toRadians(lng2 - lng1);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(rLat1) * Math.cos(rLat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return Math.max(1L, Math.round(2 * 6371000D * Math.asin(Math.min(1.0, Math.sqrt(h)))));
    }

    private Double parseCoordinate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static final class LegMetrics {
        long distanceM;
        long durationS;
        String distanceText;
        String durationText;
        String legText;
    }
}
