package com.nongxinle.dispatch.adapter.community;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.dispatch.core.view.DispatchTimelineItem;
import com.nongxinle.entity.NxCommunityDispatchDriverRouteEntity;
import com.nongxinle.entity.NxCommunityDispatchStopEntity;
import com.nongxinle.route.DisRouteTemporalHelper;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/** 社区司机配送页展示：对齐 Nx DisRouteSandboxDriverTerminalStopPresentationHelper。 */
public final class CommunityDispatchDriverTerminalPresentationHelper {

    private static final int DEFAULT_SERVICE_MINUTES = 3;

    private CommunityDispatchDriverTerminalPresentationHelper() {
    }

    public static int resolveCurrentDeliveryStopIndex(List<NxCommunityDispatchStopEntity> orderedStops) {
        if (orderedStops == null) {
            return -1;
        }
        for (int i = 0; i < orderedStops.size(); i++) {
            NxCommunityDispatchStopEntity stop = orderedStops.get(i);
            if (stop == null
                    || CommunityDispatchConstants.STOP_STATUS_CANCELLED.equals(stop.getNxCdsStopStatus())) {
                continue;
            }
            if (!CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(stop.getNxCdsStopStatus())) {
                return i;
            }
        }
        return -1;
    }

    public static NxCommunityDispatchStopEntity findFirstPendingStop(List<NxCommunityDispatchStopEntity> stops) {
        if (stops == null) {
            return null;
        }
        for (NxCommunityDispatchStopEntity stop : stops) {
            if (stop == null
                    || CommunityDispatchConstants.STOP_STATUS_CANCELLED.equals(stop.getNxCdsStopStatus())) {
                continue;
            }
            if (!CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(stop.getNxCdsStopStatus())) {
                return stop;
            }
        }
        return null;
    }

    public static void applyDeliveryStopPresentation(
            DispatchTimelineItem node,
            NxCommunityDispatchStopEntity stop,
            int stopNodeIndex,
            List<NxCommunityDispatchStopEntity> orderedStops,
            NxCommunityDispatchDriverRouteEntity route,
            int currentStopIndex,
            Date serverNow,
            String routeDate) {
        if (node == null || stop == null) {
            return;
        }
        if (CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(stop.getNxCdsStopStatus())) {
            applyDeliveredActualTimes(node, stop, stopNodeIndex, orderedStops, route, serverNow, routeDate);
            return;
        }
        node.setArrivalFieldLabel("预计到达");
        node.setDepartureFieldLabel("预计离开");
        if (currentStopIndex >= 0 && stopNodeIndex == currentStopIndex) {
            node.setStatusLabel("配送中");
            node.setCardToneClass("");
            node.setShowComplete(Boolean.TRUE);
            node.setShowNav(Boolean.TRUE);
        }
    }

    public static String formatExecutionTimeLabel(Date at, Date serverNow, String routeDate) {
        if (at == null) {
            return null;
        }
        String label = DisRouteTemporalHelper.formatRouteTimeLabel(at, serverNow, routeDate);
        if (label == null || label.trim().isEmpty() || "现在".equals(label.trim())) {
            return new SimpleDateFormat("HH:mm").format(at);
        }
        return label.trim();
    }

    public static List<NxCommunityDispatchStopEntity> sortStops(List<NxCommunityDispatchStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        List<NxCommunityDispatchStopEntity> sorted = new java.util.ArrayList<NxCommunityDispatchStopEntity>(stops);
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

    private static void applyDeliveredActualTimes(
            DispatchTimelineItem node,
            NxCommunityDispatchStopEntity stop,
            int stopNodeIndex,
            List<NxCommunityDispatchStopEntity> orderedStops,
            NxCommunityDispatchDriverRouteEntity route,
            Date serverNow,
            String routeDate) {
        node.setStopDone(Boolean.TRUE);
        node.setStatusLabel("已送达");
        node.setCardToneClass("timeline-stop-done");
        node.setShowComplete(Boolean.FALSE);
        node.setArrivalFieldLabel("出发时间");
        node.setDepartureFieldLabel("到达时间");
        node.setArrivalStatusLabel(null);
        node.setArrivalStatusTone(null);

        Date departTime = resolveDepartTimeForStop(stopNodeIndex, orderedStops, route);
        if (departTime != null) {
            node.setPlannedArrivalLabel(formatExecutionTimeLabel(departTime, serverNow, routeDate));
        }
        if (stop.getNxCdsDeliveredAt() != null) {
            node.setPlannedDepartureLabel(formatExecutionTimeLabel(
                    stop.getNxCdsDeliveredAt(), serverNow, routeDate));
        }
    }

    private static Date resolveDepartTimeForStop(
            int stopNodeIndex,
            List<NxCommunityDispatchStopEntity> orderedStops,
            NxCommunityDispatchDriverRouteEntity route) {
        if (stopNodeIndex > 0 && orderedStops != null && stopNodeIndex <= orderedStops.size()) {
            NxCommunityDispatchStopEntity previous = orderedStops.get(stopNodeIndex - 1);
            if (previous != null && previous.getNxCdsDeliveredAt() != null) {
                return new Date(previous.getNxCdsDeliveredAt().getTime()
                        + DEFAULT_SERVICE_MINUTES * 60L * 1000L);
            }
        }
        if (route != null && route.getNxCddrActualDepartAt() != null) {
            return route.getNxCddrActualDepartAt();
        }
        return null;
    }
}
