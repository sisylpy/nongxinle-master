package com.nongxinle.dispatch.adapter.community;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.dispatch.core.view.DispatchRouteCard;
import com.nongxinle.entity.NxCommunityDispatchDriverRouteEntity;
import com.nongxinle.entity.NxCommunityDispatchStopEntity;
import com.nongxinle.route.DisRouteSandboxDisplayFormatHelper;
import com.nongxinle.route.DisRouteTemporalHelper;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/** M1 路线指标：直线距离 + 固定速度时长估算（不接腾讯路线）。 */
public final class CommunityDispatchRouteMetricsHelper {

    /** 约 43 km/h，与 Nx return leg 降级一致：distanceM / 12。 */
    private static final long METERS_PER_SECOND = 12L;

    private CommunityDispatchRouteMetricsHelper() {
    }

    public static void applyRouteMetrics(
            DispatchRouteCard card,
            CommunityDispatchSandboxResult result,
            NxCommunityDispatchDriverRouteEntity route) {
        RouteMetrics metrics = estimate(result, route, card.getStopCount(), null);
        applyMetricsToCard(card, metrics);
    }

    /** 配送中：以实际发车时间为锚点，展示真实出发/下一站点到达估算。 */
    public static void applyDeliveryExecutionMetrics(
            DispatchRouteCard card,
            CommunityDispatchSandboxResult result,
            NxCommunityDispatchDriverRouteEntity route) {
        Date anchor = route != null ? route.getNxCddrActualDepartAt() : null;
        RouteMetrics metrics = estimate(result, route, card.getStopCount(), anchor);
        applyMetricsToCard(card, metrics);

        Date serverNow = new Date();
        String routeDate = result.getRouteDate();
        if (anchor != null) {
            String departLabel = CommunityDispatchDriverTerminalPresentationHelper.formatExecutionTimeLabel(
                    anchor, serverNow, routeDate);
            card.setPlannedDepartLabel(departLabel);
            card.setActualDepartAtLabel(departLabel);
            card.setActualDepartStatusLabel("已出发");
            card.setActualDepartStatusTone("ok");
        }

        List<NxCommunityDispatchStopEntity> sortedStops =
                CommunityDispatchDriverTerminalPresentationHelper.sortStops(route.getStops());
        NxCommunityDispatchStopEntity nextPending =
                CommunityDispatchDriverTerminalPresentationHelper.findFirstPendingStop(sortedStops);
        if (nextPending != null) {
            Date nextArrivalAt = resolvePendingStopArrivalAt(
                    nextPending, sortedStops, result, route, anchor != null ? anchor : serverNow);
            if (nextPending.getNxCdsDeliveredAt() != null) {
                nextArrivalAt = nextPending.getNxCdsDeliveredAt();
            }
            if (nextArrivalAt != null) {
                String nextLabel = CommunityDispatchDriverTerminalPresentationHelper.formatExecutionTimeLabel(
                        nextArrivalAt, serverNow, routeDate);
                card.setFirstStopPlannedArrivalTimeLabel(nextLabel);
                if (CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(nextPending.getNxCdsStopStatus())) {
                    card.setFirstStopArrivalStatusLabel("已送达");
                } else {
                    card.setFirstStopArrivalStatusLabel("准时");
                }
                card.setFirstStopArrivalStatusTone("ok");
            }
        }
    }

    private static void applyMetricsToCard(DispatchRouteCard card, RouteMetrics metrics) {
        card.setRouteSummary(metrics.getRouteSummary());
        card.setRouteStatsLine(metrics.getRouteStatsLine());
        card.setPlannedDepartLabel(metrics.getPlannedDepartLabel());
        card.setPlannedReturnLabel(metrics.getPlannedReturnLabel());
        card.setTotalDistanceText(metrics.getTotalDistanceText());
        card.setTotalDurationText(metrics.getTotalDurationText());
        card.setTotalRoundTripDistanceText(metrics.getTotalDistanceText());
        card.setTotalRoundTripDurationText(metrics.getTotalDurationText());
        card.setFirstStopPlannedArrivalTimeLabel(metrics.getFirstStopPlannedArrivalTimeLabel());
        card.setFirstStopArrivalStatusLabel(metrics.getFirstStopArrivalStatusLabel());
        card.setFirstStopArrivalStatusTone(metrics.getFirstStopArrivalStatusTone());
        card.setRouteHeadlineLine(metrics.getRouteHeadlineLine());
        card.setRouteRoundTripSummaryLine(metrics.getRouteRoundTripSummaryLine());
    }

    private static RouteMetrics estimate(
            CommunityDispatchSandboxResult result,
            NxCommunityDispatchDriverRouteEntity route,
            int stopCount,
            Date anchorDepartAt) {
        RouteMetrics metrics = new RouteMetrics();
        Double depotLat = parseCoordinate(result.getDepotLat());
        Double depotLng = parseCoordinate(result.getDepotLng());
        List<NxCommunityDispatchStopEntity> stops = sortStops(route.getStops());

        long totalDistanceM = 0L;
        long totalDurationS = 0L;
        long firstLegDurationS = 0L;
        if (depotLat != null && depotLng != null && !stops.isEmpty()) {
            double prevLat = depotLat;
            double prevLng = depotLng;
            boolean firstLeg = true;
            for (NxCommunityDispatchStopEntity stop : stops) {
                Double lat = parseCoordinate(stop.getNxCdsLat());
                Double lng = parseCoordinate(stop.getNxCdsLng());
                if (lat == null || lng == null) {
                    continue;
                }
                long legDistanceM = straightLineMeters(prevLat, prevLng, lat, lng);
                long legDurationS = Math.max(1L, legDistanceM / METERS_PER_SECOND);
                totalDistanceM += legDistanceM;
                totalDurationS += legDurationS;
                if (firstLeg) {
                    firstLegDurationS = legDurationS;
                    firstLeg = false;
                }
                prevLat = lat;
                prevLng = lng;
            }
            if (totalDistanceM > 0L) {
                long returnDistanceM = straightLineMeters(prevLat, prevLng, depotLat, depotLng);
                long returnDurationS = Math.max(1L, returnDistanceM / METERS_PER_SECOND);
                totalDistanceM += returnDistanceM;
                totalDurationS += returnDurationS;
            }
        }

        String totalDistanceText = DisRouteSandboxDisplayFormatHelper.formatDistanceText(
                totalDistanceM > 0L ? totalDistanceM : null);
        String totalDurationText = formatCompactDuration(totalDurationS > 0L ? totalDurationS : null);

        Date now = new Date();
        String routeDate = result.getRouteDate();
        Date cursor = anchorDepartAt != null ? anchorDepartAt : now;
        String plannedDepartLabel = anchorDepartAt != null
                ? CommunityDispatchDriverTerminalPresentationHelper.formatExecutionTimeLabel(
                        anchorDepartAt, now, routeDate)
                : "现在";
        Date plannedReturnAt = totalDurationS > 0L
                ? new Date(cursor.getTime() + totalDurationS * 1000L) : cursor;
        String plannedReturnLabel = DisRouteTemporalHelper.formatRouteTimeLabel(
                plannedReturnAt, now, routeDate);

        String firstStopArrivalTimeLabel = null;
        String firstStopArrivalLabel = null;
        NxCommunityDispatchStopEntity firstPending =
                CommunityDispatchDriverTerminalPresentationHelper.findFirstPendingStop(stops);
        if (firstPending != null) {
            Date firstArrivalAt = resolvePendingStopArrivalAt(
                    firstPending, stops, result, route, cursor);
            if (firstPending.getNxCdsDeliveredAt() != null) {
                firstArrivalAt = firstPending.getNxCdsDeliveredAt();
            }
            if (firstArrivalAt != null) {
                String arrivalTime = CommunityDispatchDriverTerminalPresentationHelper.formatExecutionTimeLabel(
                        firstArrivalAt, now, routeDate);
                firstStopArrivalTimeLabel = arrivalTime;
                firstStopArrivalLabel = buildFirstStopPlannedArrivalLabel(arrivalTime);
            }
        } else if (firstLegDurationS > 0L) {
            Date firstArrivalAt = new Date(cursor.getTime() + firstLegDurationS * 1000L);
            String arrivalTime = DisRouteTemporalHelper.formatRouteTimeLabel(firstArrivalAt, now, routeDate);
            firstStopArrivalTimeLabel = arrivalTime;
            firstStopArrivalLabel = buildFirstStopPlannedArrivalLabel(arrivalTime);
        }

        metrics.setPlannedDepartLabel(plannedDepartLabel);
        metrics.setPlannedReturnLabel(plannedReturnLabel);
        metrics.setTotalDistanceText(totalDistanceText);
        metrics.setTotalDurationText(totalDurationText);
        metrics.setFirstStopPlannedArrivalTimeLabel(firstStopArrivalTimeLabel);
        metrics.setFirstStopArrivalStatusLabel(firstStopArrivalTimeLabel != null ? "准时" : null);
        metrics.setFirstStopArrivalStatusTone(firstStopArrivalTimeLabel != null ? "ok" : null);
        metrics.setRouteStatsLine(buildRouteStatsLine(
                stopCount, totalDistanceText, plannedDepartLabel, plannedReturnLabel, totalDurationText));
        metrics.setRouteSummary(metrics.getRouteStatsLine());
        metrics.setRouteRoundTripSummaryLine(buildRouteRoundTripSummaryLine(totalDistanceText, totalDurationText));
        metrics.setRouteHeadlineLine(buildRouteHeadlineLine(firstStopArrivalLabel, plannedReturnLabel));
        return metrics;
    }

    private static Date resolvePendingStopArrivalAt(
            NxCommunityDispatchStopEntity target,
            List<NxCommunityDispatchStopEntity> stops,
            CommunityDispatchSandboxResult result,
            NxCommunityDispatchDriverRouteEntity route,
            Date cursor) {
        if (target == null || cursor == null || stops == null) {
            return null;
        }
        if (CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(target.getNxCdsStopStatus())
                && target.getNxCdsDeliveredAt() != null) {
            return target.getNxCdsDeliveredAt();
        }
        Double depotLat = parseCoordinate(result.getDepotLat());
        Double depotLng = parseCoordinate(result.getDepotLng());
        double prevLat = depotLat != null ? depotLat : 0D;
        double prevLng = depotLng != null ? depotLng : 0D;
        boolean hasPrevCoord = depotLat != null && depotLng != null;
        long cursorMs = cursor.getTime();

        for (NxCommunityDispatchStopEntity stop : stops) {
            if (stop == null
                    || CommunityDispatchConstants.STOP_STATUS_CANCELLED.equals(stop.getNxCdsStopStatus())) {
                continue;
            }
            if (hasPrevCoord) {
                Double lat = parseCoordinate(stop.getNxCdsLat());
                Double lng = parseCoordinate(stop.getNxCdsLng());
                if (lat != null && lng != null) {
                    long legDurationS = Math.max(1L, straightLineMeters(prevLat, prevLng, lat, lng) / METERS_PER_SECOND);
                    if (isSameStop(stop, target)) {
                        return new Date(cursorMs + legDurationS * 1000L);
                    }
                    cursorMs += legDurationS * 1000L;
                    prevLat = lat;
                    prevLng = lng;
                }
            }
            if (CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(stop.getNxCdsStopStatus())
                    && stop.getNxCdsDeliveredAt() != null) {
                cursorMs = stop.getNxCdsDeliveredAt().getTime() + DEFAULT_SERVICE_MINUTES * 60L * 1000L;
            } else {
                cursorMs += DEFAULT_SERVICE_MINUTES * 60L * 1000L;
            }
        }
        return null;
    }

    private static boolean isSameStop(NxCommunityDispatchStopEntity a, NxCommunityDispatchStopEntity b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.getNxCommunityDispatchStopId() != null && b.getNxCommunityDispatchStopId() != null) {
            return a.getNxCommunityDispatchStopId().equals(b.getNxCommunityDispatchStopId());
        }
        return false;
    }

    private static final int DEFAULT_SERVICE_MINUTES = 3;

    private static String buildRouteStatsLine(
            int stopCount,
            String totalDistanceText,
            String plannedDepartLabel,
            String plannedReturnLabel,
            String totalDurationText) {
        StringBuilder sb = new StringBuilder();
        sb.append("共 ").append(stopCount).append(" 站");
        if (totalDistanceText != null && !totalDistanceText.isEmpty()) {
            sb.append(" · 往返里程 ").append(totalDistanceText);
        }
        if (plannedDepartLabel != null && plannedReturnLabel != null) {
            sb.append(" · ").append(plannedDepartLabel).append(" 出发，")
                    .append(plannedReturnLabel).append(" 返回");
        }
        if (totalDurationText != null && !totalDurationText.isEmpty()) {
            sb.append(" · 全程 ").append(totalDurationText);
        }
        return sb.toString();
    }

    private static String buildRouteRoundTripSummaryLine(String distanceText, String durationText) {
        StringBuilder sb = new StringBuilder();
        if (distanceText != null && !distanceText.isEmpty()) {
            sb.append("往返 ").append(distanceText);
        }
        if (durationText != null && !durationText.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" · 全程 ");
            } else {
                sb.append("全程 ");
            }
            sb.append(durationText);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private static String buildRouteHeadlineLine(String firstStopArrivalLabel, String plannedReturnLabel) {
        if (firstStopArrivalLabel == null && plannedReturnLabel == null) {
            return null;
        }
        if (firstStopArrivalLabel != null && plannedReturnLabel != null) {
            return firstStopArrivalLabel + " · " + plannedReturnLabel + " 回仓";
        }
        return firstStopArrivalLabel != null ? firstStopArrivalLabel : plannedReturnLabel + " 回仓";
    }

    private static String buildFirstStopPlannedArrivalLabel(String arrivalLabel) {
        if (arrivalLabel == null || arrivalLabel.trim().isEmpty()) {
            return null;
        }
        String trimmed = arrivalLabel.trim();
        if (trimmed.startsWith("约") || trimmed.endsWith("到")) {
            return "首站 " + trimmed;
        }
        return "首站预计 " + trimmed + " 到达";
    }

    private static String formatCompactDuration(Long durationS) {
        if (durationS == null || durationS <= 0L) {
            return null;
        }
        long minutes = Math.max(1L, Math.round(durationS / 60.0));
        if (minutes < 60L) {
            return minutes + "分钟";
        }
        long hours = minutes / 60L;
        long remain = minutes % 60L;
        if (remain <= 0L) {
            return hours + "小时";
        }
        return hours + "小时" + remain + "分";
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

    private static List<NxCommunityDispatchStopEntity> sortStops(List<NxCommunityDispatchStopEntity> stops) {
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

    @Getter
    @Setter
    private static class RouteMetrics {
        private String routeSummary;
        private String routeStatsLine;
        private String plannedDepartLabel;
        private String plannedReturnLabel;
        private String totalDistanceText;
        private String totalDurationText;
        private String routeHeadlineLine;
        private String routeRoundTripSummaryLine;
        private String firstStopPlannedArrivalTimeLabel;
        private String firstStopArrivalStatusLabel;
        private String firstStopArrivalStatusTone;
    }
}
