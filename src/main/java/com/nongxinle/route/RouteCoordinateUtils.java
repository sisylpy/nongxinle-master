package com.nongxinle.route;

import com.nongxinle.route.model.GeoPoint;

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
}
