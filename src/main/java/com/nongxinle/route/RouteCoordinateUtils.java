package com.nongxinle.route;

import com.nongxinle.route.model.GeoPoint;

import java.util.List;

public final class RouteCoordinateUtils {

    private RouteCoordinateUtils() {
    }

    public static boolean isValidCoordinate(String lat, String lng) {
        if (lat == null || lng == null || lat.trim().isEmpty() || lng.trim().isEmpty()) {
            return false;
        }
        try {
            double latValue = Double.parseDouble(lat.trim());
            double lngValue = Double.parseDouble(lng.trim());
            return latValue >= 18.0 && latValue <= 54.0 && lngValue >= 73.0 && lngValue <= 135.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static GeoPoint toPoint(String lat, String lng) {
        GeoPoint point = new GeoPoint();
        point.setLat(lat);
        point.setLng(lng);
        return point;
    }

    /** 无市场坐标时，用站点坐标估算出发点（沙盘内存计算用）。 */
    public static GeoPoint deriveCentroidDepot(List<GeoPoint> points) {
        if (points == null || points.isEmpty()) {
            return null;
        }
        double sumLat = 0;
        double sumLng = 0;
        int count = 0;
        for (GeoPoint point : points) {
            if (point == null || !isValidCoordinate(point.getLat(), point.getLng())) {
                continue;
            }
            sumLat += Double.parseDouble(point.getLat().trim());
            sumLng += Double.parseDouble(point.getLng().trim());
            count++;
        }
        if (count == 0) {
            return null;
        }
        GeoPoint centroid = new GeoPoint();
        centroid.setLat(String.valueOf(sumLat / count));
        centroid.setLng(String.valueOf(sumLng / count));
        return centroid;
    }
}
