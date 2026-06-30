package com.nongxinle.route;

import com.nongxinle.dto.route.SandboxTodayMapDepotDto;
import com.nongxinle.dto.route.SandboxTodayMapMarkerDto;
import com.nongxinle.dto.route.SandboxTodayMapOverviewDto;
import com.nongxinle.dto.route.SandboxTodayMapPolylineDto;

import java.util.List;

/** 地图视野计算（道路 polyline 替换直线后重新 fit bounds）。 */
public final class DisRouteSandboxMapViewportHelper {

    private static final double VIEWPORT_PADDING_RATIO = 0.28;
    private static final double MIN_VIEWPORT_SPAN_DEG = 0.012;

    private DisRouteSandboxMapViewportHelper() {
    }

    public static void refreshViewport(SandboxTodayMapOverviewDto overview) {
        if (overview == null) {
            return;
        }
        applyViewport(overview, overview.getDepot(), overview.getMarkers(), overview.getPolylines());
    }

    private static void applyViewport(SandboxTodayMapOverviewDto overview,
                                      SandboxTodayMapDepotDto depot,
                                      List<SandboxTodayMapMarkerDto> markers,
                                      List<SandboxTodayMapPolylineDto> polylines) {
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        int count = 0;
        if (markers != null) {
            for (SandboxTodayMapMarkerDto marker : markers) {
                if (marker == null || marker.getLat() == null || marker.getLng() == null) {
                    continue;
                }
                minLat = Math.min(minLat, marker.getLat());
                maxLat = Math.max(maxLat, marker.getLat());
                minLng = Math.min(minLng, marker.getLng());
                maxLng = Math.max(maxLng, marker.getLng());
                count++;
            }
        }
        if (count <= 0 && depot != null && depot.getLat() != null && depot.getLng() != null) {
            minLat = maxLat = depot.getLat();
            minLng = maxLng = depot.getLng();
            count = 1;
        }
        if (count <= 0) {
            return;
        }
        double latSpan = Math.max(maxLat - minLat, MIN_VIEWPORT_SPAN_DEG);
        double lngSpan = Math.max(maxLng - minLng, MIN_VIEWPORT_SPAN_DEG);
        double paddedLatSpan = latSpan * (1.0 + 2.0 * VIEWPORT_PADDING_RATIO);
        double paddedLngSpan = lngSpan * (1.0 + 2.0 * VIEWPORT_PADDING_RATIO);
        overview.setCenterLat((minLat + maxLat) / 2.0);
        overview.setCenterLng((minLng + maxLng) / 2.0);
        int scale = suggestScale(paddedLatSpan, paddedLngSpan);
        overview.setSuggestedScale(Math.min(15, Math.max(11, scale - 1)));
    }

    private static int suggestScale(double latSpan, double lngSpan) {
        double span = Math.max(latSpan, lngSpan);
        if (span <= 0.004) {
            return 15;
        }
        if (span <= 0.018) {
            return 14;
        }
        if (span <= 0.045) {
            return 13;
        }
        if (span <= 0.10) {
            return 12;
        }
        if (span <= 0.22) {
            return 11;
        }
        if (span <= 0.55) {
            return 10;
        }
        return 9;
    }
}
