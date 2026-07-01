package com.nongxinle.dispatch.core.view;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 地图概览：三页统一 mapOverview 契约。 */
@Getter
@Setter
public class DispatchMapOverview {

    private String summaryText;
    private DispatchMapSummary summary;
    private DispatchMapDepot depot;
    private Double centerLat;
    private Double centerLng;
    private Integer suggestedScale;
    private Integer zoomLevel;
    private List<DispatchMapMarker> markers = new ArrayList<DispatchMapMarker>();
    private List<DispatchMapPolyline> polylines = new ArrayList<DispatchMapPolyline>();
    private List<DispatchMapLegendItem> legend = new ArrayList<DispatchMapLegendItem>();
    private List<DispatchMapMissingStop> missingCoordinateStops = new ArrayList<DispatchMapMissingStop>();
    private String emptyHint;

    @Getter
    @Setter
    public static class DispatchMapSummary {
        private Integer customerStopCount;
        private Integer routeCount;
        private Integer unassignedCount;
    }

    @Getter
    @Setter
    public static class DispatchMapDepot {
        private Double lat;
        private Double lng;
        private String name;
        private String address;
        private String colorKey;
        private String color;
    }

    @Getter
    @Setter
    public static class DispatchMapMarker {
        private String markerType;
        private String markerKey;
        private String title;
        private String subtitle;
        private Double lat;
        private Double lng;
        private String toneClass;
        private String colorKey;
        private String color;
        private Integer driverRouteId;
        private Integer driverUserId;
        private String driverName;
        private String sandboxStopKey;
        private String customerName;
        private String displayTitle;
        private String assignmentLabel;
        private String badgeText;
        private Integer stopSeq;
    }

    @Getter
    @Setter
    public static class DispatchMapPolyline {
        private String polylineKey;
        private String routeKey;
        private Integer driverRouteId;
        private Integer driverUserId;
        private String driverName;
        private String toneClass;
        private String colorKey;
        private String color;
        private String kind;
        private String lineStyle;
        private String lineType;
        private List<DispatchMapPoint> points = new ArrayList<DispatchMapPoint>();
    }

    @Getter
    @Setter
    public static class DispatchMapPoint {
        private Double lat;
        private Double lng;
    }

    @Getter
    @Setter
    public static class DispatchMapLegendItem {
        private String kind;
        private String label;
        private String toneClass;
        private String colorKey;
        private String color;
        private String lineStyle;
        private Integer driverUserId;
    }

    @Getter
    @Setter
    public static class DispatchMapMissingStop {
        private String customerName;
        private Integer addressId;
        private String sandboxStopKey;
        private boolean unassigned;
    }
}
