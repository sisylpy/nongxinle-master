package com.nongxinle.route;

import com.nongxinle.dto.route.*;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteSandboxDriverRouteEditPreviewHelper.StopPreview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.RouteCoordinateUtils.isValidCoordinate;

/**
 * 司机路线编辑页 mapOverview：复用 Today 视野/道路 polyline 能力，按当前 stopKeys 顺序展示单条路线。
 */
@Component
public class DisRouteSandboxDriverRouteEditMapOverviewBuilder {

    private static final String MARKER_DEPOT = "DEPOT";
    private static final String MARKER_CUSTOMER = "CUSTOMER";
    private static final String LINE_STYLE_SOLID = "SOLID";
    private static final String POLYLINE_KIND_DRIVER = "DRIVER_ROUTE";
    private static final String LEGEND_DRIVER = "DRIVER";
    private static final String LEGEND_DEPOT = "DEPOT";
    private static final String DRIVER_COLOR = "#34B768";

    @Autowired
    private DisRouteSandboxTodayMapRoadPolylineEnricher mapRoadPolylineEnricher;

    public SandboxTodayMapOverviewDto build(DriverRouteEditBuildContext ctx) {
        SandboxTodayMapOverviewDto overview = new SandboxTodayMapOverviewDto();
        if (ctx == null) {
            overview.setEmptyHint("暂无地图数据");
            return overview;
        }

        NxDisRoutePlanEntity plan = ctx.getPlan();
        String depotName = DisRouteSandboxTodayTimelineBuilder.DEPOT_NAME;
        SandboxTodayMapDepotDto depot = buildDepot(plan, depotName);
        overview.setDepot(depot);

        List<NxDisRouteStopEntity> routeStops = resolveOrderedStops(ctx);
        Map<Integer, StopPreview> previewByDep = indexStopPreviews(ctx);
        List<SandboxTodayMapMarkerDto> markers = new ArrayList<SandboxTodayMapMarkerDto>();
        List<SandboxTodayMapPointDto> linePoints = new ArrayList<SandboxTodayMapPointDto>();
        List<SandboxTodayMapMissingStopDto> missing = new ArrayList<SandboxTodayMapMissingStopDto>();

        appendDepotMarker(markers, depot, linePoints);

        int seq = 1;
        for (NxDisRouteStopEntity stop : routeStops) {
            if (stop == null) {
                continue;
            }
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            if (!hasValidStopCoordinate(stop)) {
                missing.add(buildMissingStop(stop, depId, ctx));
                continue;
            }
            StopPreview preview = depId != null ? previewByDep.get(depId) : null;
            markers.add(buildCustomerMarker(stop, depId, seq, preview, ctx));
            linePoints.add(toPoint(stop));
            seq++;
        }

        List<SandboxTodayMapPolylineDto> polylines = new ArrayList<SandboxTodayMapPolylineDto>();
        if (linePoints.size() >= 2) {
            polylines.add(buildDriverPolyline(ctx, linePoints));
        }

        overview.setMarkers(markers);
        overview.setPolylines(polylines);
        overview.setMissingCoordinateStops(missing);
        overview.setLegend(buildLegend(depot, ctx));
        applyMapStyle(overview);

        mapRoadPolylineEnricher.enrichDriverRoutes(overview);
        DisRouteSandboxTodayMapOverviewBuilder.refreshViewport(overview);

        if (!hasCustomerMarker(markers) && (depot == null || depot.getLat() == null)) {
            overview.setEmptyHint(missing.isEmpty() ? "暂无待配送客户坐标" : "部分客户缺少坐标，请完善地址");
        }
        return overview;
    }

    private static boolean hasCustomerMarker(List<SandboxTodayMapMarkerDto> markers) {
        if (markers == null) {
            return false;
        }
        for (SandboxTodayMapMarkerDto marker : markers) {
            if (marker != null && MARKER_CUSTOMER.equals(marker.getMarkerType())) {
                return true;
            }
        }
        return false;
    }

    private static List<NxDisRouteStopEntity> resolveOrderedStops(DriverRouteEditBuildContext ctx) {
        if (ctx.getPreview() != null && ctx.getPreview().stops != null && !ctx.getPreview().stops.isEmpty()) {
            return ctx.getPreview().stops;
        }
        if (ctx.getBaselineStops() != null) {
            return ctx.getBaselineStops();
        }
        return new ArrayList<NxDisRouteStopEntity>();
    }

    private static Map<Integer, StopPreview> indexStopPreviews(DriverRouteEditBuildContext ctx) {
        Map<Integer, StopPreview> index = new HashMap<Integer, StopPreview>();
        if (ctx.getPreview() == null || ctx.getPreview().stopPreviews == null) {
            return index;
        }
        for (StopPreview preview : ctx.getPreview().stopPreviews) {
            if (preview != null && preview.departmentId != null) {
                index.put(preview.departmentId, preview);
            }
        }
        return index;
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
        depot.setName(depotName);
        depot.setLat(parseDouble(lat));
        depot.setLng(parseDouble(lng));
        depot.setColorKey("DEPOT");
        depot.setColor("#333333");
        return depot;
    }

    private static void appendDepotMarker(List<SandboxTodayMapMarkerDto> markers,
                                          SandboxTodayMapDepotDto depot,
                                          List<SandboxTodayMapPointDto> linePoints) {
        if (depot == null || depot.getLat() == null || depot.getLng() == null) {
            return;
        }
        SandboxTodayMapMarkerDto marker = new SandboxTodayMapMarkerDto();
        marker.setMarkerType(MARKER_DEPOT);
        marker.setLat(depot.getLat());
        marker.setLng(depot.getLng());
        marker.setDisplayTitle(depot.getName() != null ? depot.getName() : "市场");
        markers.add(marker);
        SandboxTodayMapPointDto point = new SandboxTodayMapPointDto();
        point.setLat(depot.getLat());
        point.setLng(depot.getLng());
        linePoints.add(point);
    }

    private static SandboxTodayMapMarkerDto buildCustomerMarker(NxDisRouteStopEntity stop,
                                                                Integer depId,
                                                                int seq,
                                                                StopPreview preview,
                                                                DriverRouteEditBuildContext ctx) {
        SandboxTodayMapMarkerDto marker = new SandboxTodayMapMarkerDto();
        marker.setMarkerKey("D" + ctx.getDriverUserId() + ":DEP:" + (depId != null ? depId : seq));
        marker.setMarkerType(MARKER_CUSTOMER);
        marker.setLat(parseDouble(stop.getNxDrsLat()));
        marker.setLng(parseDouble(stop.getNxDrsLng()));
        String customerName = resolveCustomerName(stop);
        marker.setCustomerName(customerName);
        marker.setStopSeq(seq);
        marker.setBadgeText(String.valueOf(seq));
        marker.setDriverUserId(ctx.getDriverUserId());
        marker.setDriverName(ctx.getDriverName());
        marker.setDepartmentId(depId);
        marker.setDepFatherId(depId);
        marker.setColorKey("DRIVER_" + ctx.getDriverUserId());
        marker.setColor(DRIVER_COLOR);
        marker.setDisplayTitle(compactMapCustomerName(customerName));
        String arrivalLabel = preview != null && preview.plannedArrivalLabel != null
                ? preview.plannedArrivalLabel
                : DisRouteSandboxTodayStopScheduleHelper.resolvePlannedArrivalLabel(stop, ctx.getServerNow());
        if (arrivalLabel != null && !arrivalLabel.trim().isEmpty()) {
            String subtitle = arrivalLabel.trim();
            if (!subtitle.contains("到达")) {
                subtitle = subtitle + " 到达";
            }
            marker.setArrivalLabel(subtitle);
            marker.setDisplaySubtitle(subtitle);
        }
        return marker;
    }

    private static SandboxTodayMapPolylineDto buildDriverPolyline(DriverRouteEditBuildContext ctx,
                                                                  List<SandboxTodayMapPointDto> points) {
        SandboxTodayMapPolylineDto polyline = new SandboxTodayMapPolylineDto();
        polyline.setRouteKey("driver-route-edit:" + ctx.getDriverUserId());
        polyline.setDriverUserId(ctx.getDriverUserId());
        polyline.setDriverName(ctx.getDriverName());
        polyline.setColorKey("DRIVER_" + ctx.getDriverUserId());
        polyline.setColor(DRIVER_COLOR);
        polyline.setKind(POLYLINE_KIND_DRIVER);
        polyline.setLineStyle(LINE_STYLE_SOLID);
        polyline.setLineType(SandboxTodayMapPolylineLineTypes.STRAIGHT);
        polyline.setPoints(new ArrayList<SandboxTodayMapPointDto>(points));
        return polyline;
    }

    private static List<SandboxTodayMapLegendItemDto> buildLegend(SandboxTodayMapDepotDto depot,
                                                                  DriverRouteEditBuildContext ctx) {
        List<SandboxTodayMapLegendItemDto> legend = new ArrayList<SandboxTodayMapLegendItemDto>();
        if (depot != null && depot.getLat() != null) {
            SandboxTodayMapLegendItemDto depotItem = new SandboxTodayMapLegendItemDto();
            depotItem.setKind(LEGEND_DEPOT);
            depotItem.setColorKey(depot.getColorKey());
            depotItem.setColor(depot.getColor());
            depotItem.setLabel(depot.getName() != null ? depot.getName() : "市场");
            depotItem.setLineStyle(LINE_STYLE_SOLID);
            legend.add(depotItem);
        }
        SandboxTodayMapLegendItemDto routeItem = new SandboxTodayMapLegendItemDto();
        routeItem.setKind(LEGEND_DRIVER);
        routeItem.setDriverUserId(ctx.getDriverUserId());
        routeItem.setColorKey("DRIVER_" + ctx.getDriverUserId());
        routeItem.setColor(DRIVER_COLOR);
        routeItem.setLabel("当前路线");
        routeItem.setLineStyle(LINE_STYLE_SOLID);
        legend.add(routeItem);
        return legend;
    }

    private static SandboxTodayMapMissingStopDto buildMissingStop(NxDisRouteStopEntity stop,
                                                                  Integer depId,
                                                                  DriverRouteEditBuildContext ctx) {
        SandboxTodayMapMissingStopDto missing = new SandboxTodayMapMissingStopDto();
        missing.setCustomerName(resolveCustomerName(stop));
        missing.setDepartmentId(depId);
        missing.setDepFatherId(depId);
        missing.setDriverName(ctx.getDriverName());
        missing.setDriverUserId(ctx.getDriverUserId());
        return missing;
    }

    private static SandboxTodayMapPointDto toPoint(NxDisRouteStopEntity stop) {
        SandboxTodayMapPointDto point = new SandboxTodayMapPointDto();
        point.setLat(parseDouble(stop.getNxDrsLat()));
        point.setLng(parseDouble(stop.getNxDrsLng()));
        return point;
    }

    private static boolean hasValidStopCoordinate(NxDisRouteStopEntity stop) {
        return stop != null && isValidCoordinate(stop.getNxDrsLat(), stop.getNxDrsLng());
    }

    private static String resolveCustomerName(NxDisRouteStopEntity stop) {
        if (stop.getNxDrsDepartmentName() != null && !stop.getNxDrsDepartmentName().trim().isEmpty()) {
            return stop.getNxDrsDepartmentName().trim();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && task.getNxDstDepName() != null && !task.getNxDstDepName().trim().isEmpty()) {
            return task.getNxDstDepName().trim();
        }
        Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
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
}
