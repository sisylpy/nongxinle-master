package com.nongxinle.route;

import com.nongxinle.dto.route.*;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.route.RouteCoordinateUtils.isValidCoordinate;

/**
 * 今日沙盘 pageViewModel.mapOverview：市场点、客户 marker、司机 polyline（初始直线，由 enricher 升级道路）、图例。
 */
public final class DisRouteSandboxTodayMapOverviewBuilder {

    private static final String MARKER_DEPOT = "DEPOT";
    private static final String MARKER_CUSTOMER = "CUSTOMER";
    private static final String MARKER_UNASSIGNED = "UNASSIGNED";
    private static final String LEGEND_DEPOT = "DEPOT";
    private static final String LEGEND_DRIVER = "DRIVER";
    private static final String LEGEND_UNASSIGNED = "UNASSIGNED";
    private static final String COLOR_KEY_UNASSIGNED = "UNASSIGNED";
    private static final String COLOR_UNASSIGNED = "#9CA3AF";
    private static final String LINE_STYLE_SOLID = "SOLID";
    private static final String LINE_STYLE_DASHED = "DASHED";
    private static final String POLYLINE_KIND_DRIVER = "DRIVER_ROUTE";
    private static final String POLYLINE_KIND_UNASSIGNED = "UNASSIGNED";
    private static final String[] DRIVER_COLORS = {
            "#34B768", "#3B82F6", "#F59E0B", "#8B5CF6", "#EF4444", "#0EA5E9", "#22C55E", "#F97316"
    };

    private DisRouteSandboxTodayMapOverviewBuilder() {
    }

    public static SandboxTodayMapOverviewDto emptyOverview() {
        SandboxTodayMapOverviewDto overview = new SandboxTodayMapOverviewDto();
        overview.setEmptyHint("暂无地图数据");
        overview.setLegend(defaultLegendWithoutDepot());
        return overview;
    }

    public static SandboxTodayMapOverviewDto build(SandboxTodayPageBuildContext ctx) {
        SandboxTodayMapOverviewDto overview = new SandboxTodayMapOverviewDto();
        if (ctx == null) {
            overview.setEmptyHint("暂无地图数据");
            return overview;
        }

        NxDisRoutePlanEntity plan = ctx.getMergedPlan();
        SandboxTodayMapDepotDto depot = buildDepot(plan, ctx.getDepotName());
        overview.setDepot(depot);

        Map<Integer, DriverColor> driverColors = new LinkedHashMap<Integer, DriverColor>();
        List<SandboxTodayMapMarkerDto> markers = new ArrayList<SandboxTodayMapMarkerDto>();
        List<SandboxTodayMapPolylineDto> polylines = new ArrayList<SandboxTodayMapPolylineDto>();
        List<SandboxTodayMapMissingStopDto> missing = new ArrayList<SandboxTodayMapMissingStopDto>();
        Set<String> markerKeys = new HashSet<String>();
        Date serverNow = ctx.getServerNow();
        int unassignedMarkerCount = 0;

        appendVisibleRouteSnapshotsToMap(ctx, depot, driverColors, markers, polylines, missing, markerKeys);

        List<NxDisRouteStopEntity> unassigned = ctx.getUnassignedStops();
        if (unassigned != null && !unassigned.isEmpty()) {
            List<NxDisRouteStopEntity> consolidated =
                    DisRouteSandboxUnassignedStopHelper.consolidateByDepartment(unassigned);
            DriverColor unassignedColor = unassignedColor();
            for (NxDisRouteStopEntity stop : consolidated) {
                if (stop == null) {
                    continue;
                }
                Integer depId = resolveDepFatherId(stop);
                String markerKey = "U:DEP:" + (depId != null ? depId : stop.hashCode());
                if (!markerKeys.add(markerKey)) {
                    continue;
                }
                if (!hasValidStopCoordinate(stop)) {
                    missing.add(buildMissingStop(stop, null, null, depId, true));
                    continue;
                }
                SandboxTodayMapMarkerDto marker = buildCustomerMarker(
                        stop, null, null, depId, unassignedColor, MARKER_UNASSIGNED, markerKey, serverNow);
                marker.setAssignmentLabel("未分配");
                marker.setBadgeText("?");
                if (marker.getCustomerName() == null || marker.getCustomerName().trim().isEmpty()) {
                    marker.setCustomerName("未分配客户");
                }
                marker.setDisplayTitle(marker.getCustomerName());
                marker.setDisplaySubtitle("未分配客户");
                marker.setArrivalLabel(null);
                markers.add(marker);
                unassignedMarkerCount++;

                if (depot != null && depot.getLat() != null && depot.getLng() != null) {
                    List<SandboxTodayMapPointDto> unassignedLine = new ArrayList<SandboxTodayMapPointDto>();
                    appendDepotPoint(unassignedLine, depot);
                    unassignedLine.add(point(stop));
                    polylines.add(buildUnassignedPolyline(depId, unassignedColor, unassignedLine));
                }
            }
        }

        overview.setMarkers(markers);
        overview.setPolylines(polylines);
        overview.setMissingCoordinateStops(missing);
        overview.setSummary(buildSummary(markers, polylines, unassignedMarkerCount));
        overview.setLegend(buildLegend(depot, driverColors, unassignedMarkerCount > 0));
        applyMapStyle(overview);

        applyViewport(overview, depot, markers, polylines);
        if (markers.isEmpty() && (depot == null || depot.getLat() == null)) {
            overview.setEmptyHint(missing.isEmpty() ? "暂无待配送客户坐标" : "部分客户缺少坐标，请完善地址");
        }
        return overview;
    }

    private static void applyMapStyle(SandboxTodayMapOverviewDto overview) {
        if (overview == null) {
            return;
        }
        String subkey = firstNonBlank(
                System.getProperty("nxl.routeDispatch.map.subkey"),
                System.getenv("NXL_ROUTE_DISPATCH_MAP_SUBKEY"));
        if (subkey != null) {
            overview.setSubkey(subkey);
        }
        Integer layerStyle = parsePositiveInt(firstNonBlank(
                System.getProperty("nxl.routeDispatch.map.layerStyle"),
                System.getenv("NXL_ROUTE_DISPATCH_MAP_LAYER_STYLE")));
        if (layerStyle != null) {
            overview.setLayerStyle(layerStyle);
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a.trim();
        }
        if (b != null && !b.trim().isEmpty()) {
            return b.trim();
        }
        return null;
    }

    private static Integer parsePositiveInt(String value) {
        if (value == null) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static List<SandboxTodayMapLegendItemDto> defaultLegendWithoutDepot() {
        List<SandboxTodayMapLegendItemDto> legend = new ArrayList<SandboxTodayMapLegendItemDto>();
        SandboxTodayMapLegendItemDto unassigned = new SandboxTodayMapLegendItemDto();
        unassigned.setKind(LEGEND_UNASSIGNED);
        unassigned.setColorKey(COLOR_KEY_UNASSIGNED);
        unassigned.setColor(COLOR_UNASSIGNED);
        unassigned.setLabel("未分配");
        unassigned.setLineStyle(LINE_STYLE_DASHED);
        legend.add(unassigned);
        return legend;
    }

    /** PR-2c：直接遍历 VisibleDriverRouteSnapshot，不再反查 plan / suggestedStops。 */
    private static void appendVisibleRouteSnapshotsToMap(
            SandboxTodayPageBuildContext ctx,
            SandboxTodayMapDepotDto depot,
            Map<Integer, DriverColor> driverColors,
            List<SandboxTodayMapMarkerDto> markers,
            List<SandboxTodayMapPolylineDto> polylines,
            List<SandboxTodayMapMissingStopDto> missing,
            Set<String> markerKeys) {
        if (ctx == null || ctx.getVisibleDriverRoutes() == null) {
            return;
        }
        for (VisibleDriverRouteSnapshot routeSnapshot : ctx.getVisibleDriverRoutes()) {
            if (routeSnapshot == null || routeSnapshot.getStops() == null || routeSnapshot.getStops().isEmpty()) {
                continue;
            }
            String sectionKey = routeSnapshot.getSectionKey();
            if (!VisibleDriverRouteSnapshotBuilder.SECTION_SUGGESTED.equals(sectionKey)
                    && !VisibleDriverRouteSnapshotBuilder.SECTION_CONFIRMED.equals(sectionKey)) {
                continue;
            }
            appendSnapshotRouteToMap(routeSnapshot, ctx, depot, driverColors, markers, polylines, missing, markerKeys);
        }
    }

    private static void appendSnapshotRouteToMap(VisibleDriverRouteSnapshot routeSnapshot,
                                                 SandboxTodayPageBuildContext ctx,
                                                 SandboxTodayMapDepotDto depot,
                                                 Map<Integer, DriverColor> driverColors,
                                                 List<SandboxTodayMapMarkerDto> markers,
                                                 List<SandboxTodayMapPolylineDto> polylines,
                                                 List<SandboxTodayMapMissingStopDto> missing,
                                                 Set<String> markerKeys) {
        Integer driverUserId = routeSnapshot.getDriverUserId();
        if (driverUserId == null) {
            return;
        }
        NxDisDriverRouteEntity route = routeSnapshot.getSourceRoute();
        String driverName = routeSnapshot.getDriverName();
        DriverColor color = colorForDriver(driverColors, driverUserId, driverName);
        List<SandboxTodayMapPointDto> linePoints = new ArrayList<SandboxTodayMapPointDto>();
        appendDepotPoint(linePoints, depot);

        for (VisibleDriverRouteStopSnapshot stopSnap : routeSnapshot.getStops()) {
            if (stopSnap == null) {
                continue;
            }
            Integer depId = stopSnap.getDepFatherId() != null ? stopSnap.getDepFatherId() : stopSnap.getDepartmentId();
            String markerKey = "D" + driverUserId + ":DEP:" + (depId != null ? depId : stopSnap.hashCode());
            if (!markerKeys.add(markerKey)) {
                continue;
            }
            NxDisRouteStopEntity stop = stopSnap.getSourceStop();
            if (stop == null || !hasValidSnapshotCoordinate(stopSnap)) {
                missing.add(buildMissingStopFromSnapshot(stopSnap, driverName, driverUserId, depId));
                continue;
            }
            SandboxTodayMapMarkerDto marker = buildCustomerMarkerFromSnapshot(
                    stopSnap, driverUserId, driverName, depId, color, MARKER_CUSTOMER, markerKey);
            markers.add(marker);
            linePoints.add(pointFromSnapshot(stopSnap));
        }

        if (linePoints.size() >= 2) {
            NxDisDriverRouteEntity polylineRoute = route != null ? route : new NxDisDriverRouteEntity();
            if (polylineRoute.getNxDdrDriverUserId() == null) {
                polylineRoute.setNxDdrDriverUserId(driverUserId);
            }
            polylines.add(buildPolyline(polylineRoute, driverUserId, driverName, color, linePoints,
                    POLYLINE_KIND_DRIVER, LINE_STYLE_SOLID));
        }
    }

    private static boolean hasValidSnapshotCoordinate(VisibleDriverRouteStopSnapshot stopSnap) {
        return stopSnap != null && isValidCoordinate(stopSnap.getLat(), stopSnap.getLng());
    }

    private static SandboxTodayMapPointDto pointFromSnapshot(VisibleDriverRouteStopSnapshot stopSnap) {
        SandboxTodayMapPointDto point = new SandboxTodayMapPointDto();
        point.setLat(parseDouble(stopSnap.getLat()));
        point.setLng(parseDouble(stopSnap.getLng()));
        return point;
    }

    private static SandboxTodayMapMarkerDto buildCustomerMarkerFromSnapshot(
            VisibleDriverRouteStopSnapshot stopSnap,
            Integer driverUserId,
            String driverName,
            Integer depId,
            DriverColor color,
            String markerType,
            String markerKey) {
        SandboxTodayMapMarkerDto marker = new SandboxTodayMapMarkerDto();
        marker.setMarkerKey(markerKey);
        marker.setMarkerType(markerType);
        marker.setLat(parseDouble(stopSnap.getLat()));
        marker.setLng(parseDouble(stopSnap.getLng()));
        marker.setCustomerName(stopSnap.getCustomerName());
        marker.setStopSeq(stopSnap.getVisibleSeq());
        marker.setDriverName(driverName);
        marker.setDriverUserId(driverUserId);
        marker.setDepartmentId(depId);
        marker.setDepFatherId(depId);
        marker.setColorKey(color.colorKey);
        marker.setColor(color.color);
        marker.setAssignmentLabel(buildAssignmentLabel(stopSnap.getVisibleSeq(), driverName, markerType));
        marker.setDisplayTitle(compactMapCustomerName(stopSnap.getCustomerName()));
        if (MARKER_CUSTOMER.equals(markerType)) {
            marker.setBadgeText(String.valueOf(stopSnap.getVisibleSeq()));
            marker.setArrivalLabel(stopSnap.getArrivalLabel());
            marker.setDisplaySubtitle(stopSnap.getArrivalLabel());
        }
        return marker;
    }

    private static SandboxTodayMapMissingStopDto buildMissingStopFromSnapshot(
            VisibleDriverRouteStopSnapshot stopSnap,
            String driverName,
            Integer driverUserId,
            Integer depId) {
        SandboxTodayMapMissingStopDto missing = new SandboxTodayMapMissingStopDto();
        missing.setCustomerName(stopSnap.getCustomerName());
        missing.setDepartmentId(depId);
        missing.setDepFatherId(depId);
        missing.setDriverName(driverName);
        missing.setDriverUserId(driverUserId);
        missing.setAssignmentLabel(buildAssignmentLabel(stopSnap.getVisibleSeq(), driverName, MARKER_CUSTOMER));
        return missing;
    }

    private static SandboxTodayMapDepotDto buildDepot(NxDisRoutePlanEntity plan, String depotName) {
        if (plan == null) {
            return null;
        }
        String lat = plan.getNxDrpDepotLat();
        String lng = plan.getNxDrpDepotLng();
        if (!isValidCoordinate(lat, lng)) {
            return null;
        }
        SandboxTodayMapDepotDto depot = new SandboxTodayMapDepotDto();
        depot.setMarkerType(MARKER_DEPOT);
        depot.setName(depotName != null && !depotName.trim().isEmpty()
                ? depotName.trim() : DisRouteSandboxTodayTimelineBuilder.DEPOT_NAME);
        depot.setLat(parseDouble(lat));
        depot.setLng(parseDouble(lng));
        depot.setColorKey("DEPOT");
        depot.setColor("#333333");
        return depot;
    }

    private static SandboxTodayMapSummaryDto buildSummary(List<SandboxTodayMapMarkerDto> markers,
                                                          List<SandboxTodayMapPolylineDto> polylines,
                                                          int unassignedCount) {
        SandboxTodayMapSummaryDto summary = new SandboxTodayMapSummaryDto();
        summary.setCustomerStopCount(markers != null ? markers.size() : 0);
        int routeCount = 0;
        if (polylines != null) {
            for (SandboxTodayMapPolylineDto polyline : polylines) {
                if (polyline != null && POLYLINE_KIND_DRIVER.equals(polyline.getKind())) {
                    routeCount++;
                }
            }
        }
        summary.setRouteCount(routeCount);
        summary.setUnassignedCount(unassignedCount);
        return summary;
    }

    private static SandboxTodayMapMarkerDto buildCustomerMarker(NxDisRouteStopEntity stop,
                                                                Integer driverUserId,
                                                                String driverName,
                                                                Integer depId,
                                                                DriverColor color,
                                                                String markerType,
                                                                String markerKey,
                                                                Date serverNow) {
        SandboxTodayMapMarkerDto marker = new SandboxTodayMapMarkerDto();
        marker.setMarkerKey(markerKey);
        marker.setMarkerType(markerType);
        marker.setLat(parseDouble(stop.getNxDrsLat()));
        marker.setLng(parseDouble(stop.getNxDrsLng()));
        marker.setCustomerName(resolveCustomerName(stop));
        marker.setDepartmentId(depId);
        marker.setDepFatherId(depId);
        marker.setColorKey(color.colorKey);
        marker.setColor(color.color);
        marker.setAssignmentLabel("未分配");
        marker.setDisplayTitle(compactMapCustomerName(marker.getCustomerName()));
        marker.setBadgeText("?");
        marker.setDisplaySubtitle("未分配客户");
        return marker;
    }

    private static SandboxTodayMapMissingStopDto buildMissingStop(NxDisRouteStopEntity stop,
                                                                  String driverName,
                                                                  Integer driverUserId,
                                                                  Integer depId,
                                                                  boolean unassigned) {
        SandboxTodayMapMissingStopDto missing = new SandboxTodayMapMissingStopDto();
        missing.setCustomerName(resolveCustomerName(stop));
        missing.setDepartmentId(depId);
        missing.setDepFatherId(depId);
        missing.setDriverName(driverName);
        missing.setDriverUserId(driverUserId);
        missing.setAssignmentLabel(unassigned ? "未分配" : buildAssignmentLabel(
                stop.getNxDrsStopSeq(), driverName, MARKER_CUSTOMER));
        return missing;
    }

    private static SandboxTodayMapPolylineDto buildPolyline(NxDisDriverRouteEntity route,
                                                            Integer driverUserId,
                                                            String driverName,
                                                            DriverColor color,
                                                            List<SandboxTodayMapPointDto> points,
                                                            String kind,
                                                            String lineStyle) {
        SandboxTodayMapPolylineDto polyline = new SandboxTodayMapPolylineDto();
        polyline.setRouteKey(route.getNxDdrId() != null
                ? "route-" + route.getNxDdrId()
                : "driver-" + driverUserId);
        polyline.setDriverUserId(driverUserId);
        polyline.setDriverName(driverName);
        polyline.setColorKey(color.colorKey);
        polyline.setColor(color.color);
        polyline.setKind(kind);
        polyline.setLineStyle(lineStyle);
        if (POLYLINE_KIND_DRIVER.equals(kind)) {
            polyline.setLineType(SandboxTodayMapPolylineLineTypes.STRAIGHT);
        }
        polyline.setPoints(points);
        return polyline;
    }

    private static SandboxTodayMapPolylineDto buildUnassignedPolyline(Integer depId,
                                                                      DriverColor color,
                                                                      List<SandboxTodayMapPointDto> points) {
        SandboxTodayMapPolylineDto polyline = new SandboxTodayMapPolylineDto();
        polyline.setRouteKey("unassigned-dep:" + (depId != null ? depId : "unknown"));
        polyline.setColorKey(color.colorKey);
        polyline.setColor(color.color);
        polyline.setKind(POLYLINE_KIND_UNASSIGNED);
        polyline.setLineStyle(LINE_STYLE_DASHED);
        polyline.setPoints(points);
        return polyline;
    }

    private static List<SandboxTodayMapLegendItemDto> buildLegend(SandboxTodayMapDepotDto depot,
                                                                 Map<Integer, DriverColor> driverColors,
                                                                 boolean includeUnassigned) {
        List<SandboxTodayMapLegendItemDto> legend = new ArrayList<SandboxTodayMapLegendItemDto>();
        if (depot != null && depot.getLat() != null) {
            SandboxTodayMapLegendItemDto item = new SandboxTodayMapLegendItemDto();
            item.setKind(LEGEND_DEPOT);
            item.setColorKey(depot.getColorKey());
            item.setColor(depot.getColor());
            item.setLabel(depot.getName() != null ? depot.getName() : "市场");
            item.setLineStyle(LINE_STYLE_SOLID);
            legend.add(item);
        }
        for (Map.Entry<Integer, DriverColor> entry : driverColors.entrySet()) {
            DriverColor color = entry.getValue();
            SandboxTodayMapLegendItemDto item = new SandboxTodayMapLegendItemDto();
            item.setKind(LEGEND_DRIVER);
            item.setDriverUserId(entry.getKey());
            item.setColorKey(color.colorKey);
            item.setColor(color.color);
            item.setLabel(color.driverName != null ? color.driverName : ("司机" + entry.getKey()));
            item.setLineStyle(LINE_STYLE_SOLID);
            legend.add(item);
        }
        if (includeUnassigned) {
            SandboxTodayMapLegendItemDto unassigned = new SandboxTodayMapLegendItemDto();
            unassigned.setKind(LEGEND_UNASSIGNED);
            unassigned.setColorKey(COLOR_KEY_UNASSIGNED);
            unassigned.setColor(COLOR_UNASSIGNED);
            unassigned.setLabel("未分配");
            unassigned.setLineStyle(LINE_STYLE_DASHED);
            legend.add(unassigned);
        }
        return legend;
    }

    /** 道路 polyline 替换直线后重新计算视野。 */
    public static void refreshViewport(SandboxTodayMapOverviewDto overview) {
        if (overview == null) {
            return;
        }
        applyViewport(overview, overview.getDepot(), overview.getMarkers(), overview.getPolylines());
    }

    /** 视野留白：按客户点聚合适配，市场不参与 bounds 计算。 */
    private static final double VIEWPORT_PADDING_RATIO = 0.28;
    private static final double MIN_VIEWPORT_SPAN_DEG = 0.012;

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

    private static void appendDepotPoint(List<SandboxTodayMapPointDto> points, SandboxTodayMapDepotDto depot) {
        if (depot == null || depot.getLat() == null || depot.getLng() == null) {
            return;
        }
        SandboxTodayMapPointDto point = new SandboxTodayMapPointDto();
        point.setLat(depot.getLat());
        point.setLng(depot.getLng());
        points.add(point);
    }

    private static SandboxTodayMapPointDto point(NxDisRouteStopEntity stop) {
        SandboxTodayMapPointDto point = new SandboxTodayMapPointDto();
        point.setLat(parseDouble(stop.getNxDrsLat()));
        point.setLng(parseDouble(stop.getNxDrsLng()));
        return point;
    }

    private static boolean hasValidStopCoordinate(NxDisRouteStopEntity stop) {
        return stop != null && isValidCoordinate(stop.getNxDrsLat(), stop.getNxDrsLng());
    }

    private static Integer resolveDepFatherId(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getNxDrsDepartmentId() != null) {
            return stop.getNxDrsDepartmentId();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && task.getNxDstDepFatherId() != null) {
            return task.getNxDstDepFatherId();
        }
        return null;
    }

    private static String resolveCustomerName(NxDisRouteStopEntity stop) {
        if (stop.getNxDrsDepartmentName() != null && !stop.getNxDrsDepartmentName().trim().isEmpty()) {
            return stop.getNxDrsDepartmentName().trim();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && task.getNxDstDepName() != null && !task.getNxDstDepName().trim().isEmpty()) {
            return task.getNxDstDepName().trim();
        }
        Integer depId = resolveDepFatherId(stop);
        return depId != null ? ("客户" + depId) : "客户";
    }

    private static String compactMapCustomerName(String name) {
        if (name == null) {
            return null;
        }
        String text = name.trim();
        if (text.length() <= 7) {
            return text;
        }
        return text.substring(0, 7);
    }

    private static String buildAssignmentLabel(Integer stopSeq, String driverName, String markerType) {
        if (MARKER_UNASSIGNED.equals(markerType)) {
            return "未分配";
        }
        StringBuilder sb = new StringBuilder();
        if (stopSeq != null && stopSeq > 0) {
            sb.append("第").append(stopSeq).append("站");
        }
        if (driverName != null && !driverName.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append(driverName.trim());
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private static DriverColor colorForDriver(Map<Integer, DriverColor> index,
                                              Integer driverUserId,
                                              String driverName) {
        DriverColor existing = index.get(driverUserId);
        if (existing != null) {
            if (driverName != null && !driverName.trim().isEmpty()) {
                existing.driverName = driverName.trim();
            }
            return existing;
        }
        int paletteIndex = index.size() % DRIVER_COLORS.length;
        DriverColor color = new DriverColor();
        color.colorKey = "DRIVER_" + driverUserId;
        color.color = DRIVER_COLORS[paletteIndex];
        color.driverName = driverName != null ? driverName.trim() : null;
        index.put(driverUserId, color);
        return color;
    }

    private static DriverColor unassignedColor() {
        DriverColor color = new DriverColor();
        color.colorKey = COLOR_KEY_UNASSIGNED;
        color.color = COLOR_UNASSIGNED;
        return color;
    }

    private static Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static final class DriverColor {
        private String colorKey;
        private String color;
        private String driverName;
    }
}
