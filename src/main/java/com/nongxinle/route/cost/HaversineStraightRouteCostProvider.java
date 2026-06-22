package com.nongxinle.route.cost;

import com.nongxinle.route.DisRouteDistanceTypes;
import com.nongxinle.route.RouteCoordinateUtils;
import com.nongxinle.route.RouteCostProvider;
import com.nongxinle.route.RouteCostProviderType;
import com.nongxinle.route.model.CostMatrix;
import com.nongxinle.route.model.GeoPoint;
import com.nongxinle.route.model.RouteStopInput;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 直线距离 fallback：Haversine 米 + 按 30km/h 估算时长。
 */
@Component
public class HaversineStraightRouteCostProvider implements RouteCostProvider {

    private static final double EARTH_RADIUS_M = 6371000.0;
    private static final double FALLBACK_SPEED_MPS = 30_000.0 / 3600.0;

    @Override
    public RouteCostProviderType providerType() {
        return RouteCostProviderType.HAVERSINE_STRAIGHT;
    }

    @Override
    public CostMatrix buildMatrix(GeoPoint depot, List<RouteStopInput> stops) {
        if (depot == null || !RouteCoordinateUtils.isValidCoordinate(depot.getLat(), depot.getLng())) {
            depot = deriveDepotFromStops(stops);
        }
        if (depot == null) {
            throw new IllegalArgumentException("出发点坐标无效，且无法从站点坐标估算");
        }
        List<GeoPoint> allPoints = new ArrayList<GeoPoint>();
        allPoints.add(depot);
        for (RouteStopInput stop : stops) {
            allPoints.add(stop.getLocation());
        }
        int n = allPoints.size();
        long[][] distanceM = new long[n][n];
        long[][] durationS = new long[n][n];
        String[][] distanceType = new String[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distanceM[i][j] = 0;
                    durationS[i][j] = 0;
                    distanceType[i][j] = DisRouteDistanceTypes.ROUTE_DISTANCE;
                } else {
                    long meters = haversineMeters(allPoints.get(i), allPoints.get(j));
                    distanceM[i][j] = meters;
                    durationS[i][j] = Math.max(60L, Math.round(meters / FALLBACK_SPEED_MPS));
                    distanceType[i][j] = DisRouteDistanceTypes.ESTIMATED_STRAIGHT_DISTANCE;
                }
            }
        }
        CostMatrix matrix = new CostMatrix();
        matrix.setDepot(depot);
        matrix.setStops(stops);
        matrix.setDistanceM(distanceM);
        matrix.setDurationS(durationS);
        matrix.setDistanceType(distanceType);
        matrix.setDistanceProvider(DisRouteDistanceTypes.PROVIDER_HAVERSINE);
        return matrix;
    }

    static long haversineMeters(GeoPoint a, GeoPoint b) {
        if (a == null || b == null || a.getLat() == null || a.getLng() == null
                || b.getLat() == null || b.getLng() == null) {
            return 0L;
        }
        double lat1 = Math.toRadians(parseCoord(a.getLat()));
        double lat2 = Math.toRadians(parseCoord(b.getLat()));
        double dLat = lat2 - lat1;
        double dLng = Math.toRadians(parseCoord(b.getLng()) - parseCoord(a.getLng()));
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return Math.round(2 * EARTH_RADIUS_M * Math.asin(Math.min(1.0, Math.sqrt(h))));
    }

    private static double parseCoord(String value) {
        return Double.parseDouble(value.trim());
    }

    private static GeoPoint deriveDepotFromStops(List<RouteStopInput> stops) {
        if (stops == null || stops.isEmpty()) {
            return null;
        }
        List<GeoPoint> points = new ArrayList<GeoPoint>();
        for (RouteStopInput stop : stops) {
            if (stop != null && stop.getLocation() != null) {
                points.add(stop.getLocation());
            }
        }
        return RouteCoordinateUtils.deriveCentroidDepot(points);
    }
}
