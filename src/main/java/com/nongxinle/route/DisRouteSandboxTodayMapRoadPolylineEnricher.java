package com.nongxinle.route;

import com.nongxinle.dto.route.SandboxTodayMapOverviewDto;
import com.nongxinle.dto.route.SandboxTodayMapPointDto;
import com.nongxinle.dto.route.SandboxTodayMapPolylineDto;
import com.nongxinle.route.map.TencentDrivingRoutePolylineProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 将 mapOverview 司机路线 polyline 从直线升级为沿道路点串（失败时保留直线）。
 */
@Component
public class DisRouteSandboxTodayMapRoadPolylineEnricher {

    private static final Logger log = LoggerFactory.getLogger(DisRouteSandboxTodayMapRoadPolylineEnricher.class);
    private static final String POLYLINE_KIND_DRIVER = "DRIVER_ROUTE";
    private static final long LEG_REQUEST_INTERVAL_MS = 120L;

    @Autowired
    private TencentDrivingRoutePolylineProvider drivingRoutePolylineProvider;

    public void enrichDriverRoutes(SandboxTodayMapOverviewDto overview) {
        if (overview == null || overview.getPolylines() == null || overview.getPolylines().isEmpty()) {
            return;
        }
        Map<String, List<SandboxTodayMapPointDto>> legCache = new HashMap<String, List<SandboxTodayMapPointDto>>();
        boolean viewportDirty = false;
        for (SandboxTodayMapPolylineDto polyline : overview.getPolylines()) {
            if (polyline == null || !POLYLINE_KIND_DRIVER.equals(polyline.getKind())) {
                continue;
            }
            List<SandboxTodayMapPointDto> straightPoints = polyline.getPoints();
            if (straightPoints == null || straightPoints.size() < 2) {
                continue;
            }
            RoadPolylineBuildResult result = buildRoadPolyline(straightPoints, legCache);
            if (result.points != null && result.points.size() >= 2) {
                polyline.setPoints(result.points);
                polyline.setLineType(result.anyRoadLeg
                        ? SandboxTodayMapPolylineLineTypes.ROAD
                        : SandboxTodayMapPolylineLineTypes.FALLBACK);
                viewportDirty = true;
            } else {
                polyline.setLineType(SandboxTodayMapPolylineLineTypes.FALLBACK);
            }
        }
        if (viewportDirty) {
            DisRouteSandboxMapViewportHelper.refreshViewport(overview);
        }
    }

    private RoadPolylineBuildResult buildRoadPolyline(List<SandboxTodayMapPointDto> waypoints,
                                                      Map<String, List<SandboxTodayMapPointDto>> legCache) {
        List<SandboxTodayMapPointDto> merged = new ArrayList<SandboxTodayMapPointDto>();
        boolean anyRoadLeg = false;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            SandboxTodayMapPointDto from = waypoints.get(i);
            SandboxTodayMapPointDto to = waypoints.get(i + 1);
            if (!isValidPoint(from) || !isValidPoint(to)) {
                continue;
            }
            if (i > 0) {
                try {
                    Thread.sleep(LEG_REQUEST_INTERVAL_MS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            try {
                List<SandboxTodayMapPointDto> legPoints = drivingRoutePolylineProvider.fetchDrivingPolyline(
                        from.getLat(), from.getLng(), to.getLat(), to.getLng(), legCache);
                if (legPoints == null || legPoints.isEmpty()) {
                    throw new IllegalStateException("路段 polyline 为空");
                }
                appendLegPoints(merged, legPoints);
                anyRoadLeg = true;
            } catch (Exception ex) {
                log.debug("mapOverview 路段道路规划失败，该段降级直线: from=({},{}), to=({},{}), reason={}",
                        from.getLat(), from.getLng(), to.getLat(), to.getLng(), ex.getMessage());
                appendStraightLeg(merged, from, to);
            }
        }
        return new RoadPolylineBuildResult(merged, anyRoadLeg);
    }

    private static void appendLegPoints(List<SandboxTodayMapPointDto> merged,
                                        List<SandboxTodayMapPointDto> legPoints) {
        if (merged.isEmpty()) {
            merged.addAll(legPoints);
            return;
        }
        merged.addAll(legPoints.subList(1, legPoints.size()));
    }

    private static void appendStraightLeg(List<SandboxTodayMapPointDto> merged,
                                            SandboxTodayMapPointDto from,
                                            SandboxTodayMapPointDto to) {
        if (merged.isEmpty()) {
            merged.add(copyPoint(from));
        } else {
            SandboxTodayMapPointDto last = merged.get(merged.size() - 1);
            if (!sameCoordinate(last, from)) {
                merged.add(copyPoint(from));
            }
        }
        merged.add(copyPoint(to));
    }

    private static SandboxTodayMapPointDto copyPoint(SandboxTodayMapPointDto source) {
        SandboxTodayMapPointDto copy = new SandboxTodayMapPointDto();
        copy.setLat(source.getLat());
        copy.setLng(source.getLng());
        return copy;
    }

    private static boolean sameCoordinate(SandboxTodayMapPointDto a, SandboxTodayMapPointDto b) {
        if (a == null || b == null || a.getLat() == null || a.getLng() == null
                || b.getLat() == null || b.getLng() == null) {
            return false;
        }
        return Double.compare(a.getLat(), b.getLat()) == 0
                && Double.compare(a.getLng(), b.getLng()) == 0;
    }

    private static boolean isValidPoint(SandboxTodayMapPointDto point) {
        return point != null && point.getLat() != null && point.getLng() != null;
    }

    private static final class RoadPolylineBuildResult {
        private final List<SandboxTodayMapPointDto> points;
        private final boolean anyRoadLeg;

        private RoadPolylineBuildResult(List<SandboxTodayMapPointDto> points, boolean anyRoadLeg) {
            this.points = points;
            this.anyRoadLeg = anyRoadLeg;
        }
    }
}
