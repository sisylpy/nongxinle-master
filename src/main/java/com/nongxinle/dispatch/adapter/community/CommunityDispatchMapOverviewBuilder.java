package com.nongxinle.dispatch.adapter.community;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.dao.NxCommunityDao;
import com.nongxinle.dao.NxCustomerUserAddressDao;
import com.nongxinle.dispatch.core.domain.DispatchPageMode;
import com.nongxinle.dispatch.core.view.DispatchMapOverview;
import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * nxCommunity mapOverview 基础数据：marker / polyline / legend / summary（直线，不接腾讯路线）。
 */
@Component
public class CommunityDispatchMapOverviewBuilder {

    private static final String[] DRIVER_COLORS = {
            "#2563EB", "#DC2626", "#059669", "#D97706", "#7C3AED", "#0891B2"
    };
    private static final String[] DRIVER_COLOR_KEYS = {
            "blue", "red", "green", "orange", "purple", "cyan"
    };
    private static final double MIN_VIEWPORT_SPAN_DEG = 0.012;

    @Autowired
    private NxCustomerUserAddressDao nxCustomerUserAddressDao;
    @Autowired
    private NxCommunityDao nxCommunityDao;

    public DispatchMapOverview build(
            CommunityDispatchSandboxResult result,
            DispatchPageMode pageMode,
            Map<Integer, String> driverNames) {
        if (result == null) {
            DispatchMapOverview overview = new DispatchMapOverview();
            overview.setEmptyHint("暂无地图数据");
            return overview;
        }
        List<NxCommunityDispatchDriverRouteEntity> displayRoutes = resolveDisplayRoutes(result, pageMode);
        return buildInternal(result, displayRoutes, null, pageMode, driverNames);
    }

    /**
     * 路线编辑页：用编辑后的司机路线覆盖沙盘分配，并从其它司机路线中移除已挪入的站点；
     * 可添加门店以未分配 marker 展示（对齐 NX buildRouteEditMapOverview）。
     */
    public DispatchMapOverview buildRouteEdit(
            CommunityDispatchSandboxResult result,
            NxCommunityDispatchDriverRouteEntity editedRoute,
            List<NxCommunityDispatchStopEntity> addableStops,
            Map<Integer, String> driverNames) {
        if (result == null) {
            DispatchMapOverview overview = new DispatchMapOverview();
            overview.setEmptyHint("暂无地图数据");
            return overview;
        }
        List<NxCommunityDispatchDriverRouteEntity> displayRoutes = mergeRoutesForRouteEdit(
                result.resolveSandboxRoutes(), editedRoute);
        return buildInternal(result, displayRoutes, addableStops,
                DispatchPageMode.DISPATCH_SANDBOX, driverNames);
    }

    private DispatchMapOverview buildInternal(
            CommunityDispatchSandboxResult result,
            List<NxCommunityDispatchDriverRouteEntity> displayRoutes,
            List<NxCommunityDispatchStopEntity> addableStops,
            DispatchPageMode pageMode,
            Map<Integer, String> driverNames) {
        DispatchMapOverview overview = new DispatchMapOverview();
        if (displayRoutes == null) {
            displayRoutes = Collections.emptyList();
        }
        int unassignedCount = addableStops != null ? addableStops.size()
                : (result.getUnassignedStopGroups() != null
                ? result.getUnassignedStopGroups().size() : 0);
        int routeStopCount = 0;
        int routeCount = 0;
        for (NxCommunityDispatchDriverRouteEntity route : displayRoutes) {
            if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
                continue;
            }
            routeCount++;
            routeStopCount += route.getStops().size();
        }

        DispatchMapOverview.DispatchMapSummary summary = new DispatchMapOverview.DispatchMapSummary();
        summary.setCustomerStopCount(routeStopCount + unassignedCount);
        summary.setRouteCount(routeCount);
        summary.setUnassignedCount(unassignedCount);
        overview.setSummary(summary);
        overview.setSummaryText(buildSummaryText(summary));

        overview.setDepot(buildDepot(result));
        List<DispatchMapOverview.DispatchMapMarker> markers = new ArrayList<DispatchMapOverview.DispatchMapMarker>();
        List<DispatchMapOverview.DispatchMapMissingStop> missing = new ArrayList<DispatchMapOverview.DispatchMapMissingStop>();

        if (addableStops != null) {
            appendAddableStopMarkers(addableStops, markers, missing);
        } else {
            appendUnassignedMarkers(result, markers, missing);
        }
        appendRouteMarkers(displayRoutes, driverNames, markers, missing);

        overview.setMarkers(markers);
        overview.setMissingCoordinateStops(missing);
        overview.setPolylines(buildPolylines(displayRoutes, overview.getDepot()));
        overview.setLegend(buildLegend(displayRoutes, pageMode, driverNames, overview.getDepot(), unassignedCount));
        applyViewport(overview, markers);

        if (markers.isEmpty() && overview.getPolylines().isEmpty()
                && (overview.getDepot() == null || overview.getDepot().getLat() == null)) {
            overview.setEmptyHint("暂无地图数据");
        }
        return overview;
    }

    private List<NxCommunityDispatchDriverRouteEntity> mergeRoutesForRouteEdit(
            List<NxCommunityDispatchDriverRouteEntity> originalRoutes,
            NxCommunityDispatchDriverRouteEntity editedRoute) {
        if (editedRoute == null) {
            return originalRoutes != null
                    ? originalRoutes : Collections.<NxCommunityDispatchDriverRouteEntity>emptyList();
        }
        Set<Integer> editedAddressIds = new HashSet<Integer>();
        if (editedRoute.getStops() != null) {
            for (NxCommunityDispatchStopEntity stop : editedRoute.getStops()) {
                if (stop != null && stop.getNxCdsAddressId() != null) {
                    editedAddressIds.add(stop.getNxCdsAddressId());
                }
            }
        }
        Integer editedDriverId = editedRoute.getNxCddrDriverUserId();
        List<NxCommunityDispatchDriverRouteEntity> merged = new ArrayList<NxCommunityDispatchDriverRouteEntity>();
        boolean editedDriverFound = false;
        if (originalRoutes != null) {
            for (NxCommunityDispatchDriverRouteEntity route : originalRoutes) {
                if (route == null) {
                    continue;
                }
                if (editedDriverId != null && editedDriverId.equals(route.getNxCddrDriverUserId())) {
                    if (editedRoute.getStops() != null && !editedRoute.getStops().isEmpty()) {
                        merged.add(editedRoute);
                    }
                    editedDriverFound = true;
                    continue;
                }
                NxCommunityDispatchDriverRouteEntity trimmed = trimRouteStops(route, editedAddressIds);
                if (trimmed.getStops() != null && !trimmed.getStops().isEmpty()) {
                    merged.add(trimmed);
                }
            }
        }
        if (!editedDriverFound && editedRoute.getStops() != null && !editedRoute.getStops().isEmpty()) {
            merged.add(editedRoute);
        }
        return merged;
    }

    private NxCommunityDispatchDriverRouteEntity trimRouteStops(
            NxCommunityDispatchDriverRouteEntity route,
            Set<Integer> excludeAddressIds) {
        NxCommunityDispatchDriverRouteEntity copy = new NxCommunityDispatchDriverRouteEntity();
        copy.setNxCommunityDispatchDriverRouteId(route.getNxCommunityDispatchDriverRouteId());
        copy.setNxCddrDriverUserId(route.getNxCddrDriverUserId());
        copy.setNxCddrRouteStatus(route.getNxCddrRouteStatus());
        List<NxCommunityDispatchStopEntity> stops = new ArrayList<NxCommunityDispatchStopEntity>();
        if (route.getStops() != null) {
            for (NxCommunityDispatchStopEntity stop : route.getStops()) {
                if (stop == null || stop.getNxCdsAddressId() == null) {
                    continue;
                }
                if (!excludeAddressIds.contains(stop.getNxCdsAddressId())) {
                    stops.add(stop);
                }
            }
        }
        copy.setStops(stops);
        return copy;
    }

    private String buildSummaryText(DispatchMapOverview.DispatchMapSummary summary) {
        int stops = summary.getCustomerStopCount() != null ? summary.getCustomerStopCount() : 0;
        int routes = summary.getRouteCount() != null ? summary.getRouteCount() : 0;
        int unassigned = summary.getUnassignedCount() != null ? summary.getUnassignedCount() : 0;
        StringBuilder text = new StringBuilder();
        text.append(stops).append(" 站 · ").append(routes).append(" 路线");
        if (unassigned > 0) {
            text.append(" · ").append(unassigned).append(" 未分配");
        }
        return text.toString();
    }

    private DispatchMapOverview.DispatchMapDepot buildDepot(CommunityDispatchSandboxResult result) {
        Double lat = parseCoordinate(result.getDepotLat());
        Double lng = parseCoordinate(result.getDepotLng());
        if (lat == null || lng == null) {
            return null;
        }
        DispatchMapOverview.DispatchMapDepot depot = new DispatchMapOverview.DispatchMapDepot();
        depot.setLat(lat);
        depot.setLng(lng);
        depot.setName(resolveDepotName(result.getCommunityId()));
        depot.setColorKey("depot");
        depot.setColor("#111827");
        return depot;
    }

    private String resolveDepotName(Integer communityId) {
        if (communityId == null) {
            return "门店";
        }
        NxCommunityEntity community = nxCommunityDao.queryObject(communityId);
        if (community == null || community.getNxCommunityName() == null
                || community.getNxCommunityName().trim().isEmpty()) {
            return "门店";
        }
        return community.getNxCommunityName();
    }

    private List<NxCommunityDispatchDriverRouteEntity> resolveDisplayRoutes(
            CommunityDispatchSandboxResult result, DispatchPageMode pageMode) {
        if (pageMode == DispatchPageMode.DISPATCH_SANDBOX) {
            return result.resolveSandboxRoutes();
        }
        return result.getConfirmedRoutes() != null
                ? result.getConfirmedRoutes() : Collections.<NxCommunityDispatchDriverRouteEntity>emptyList();
    }

    private void appendAddableStopMarkers(
            List<NxCommunityDispatchStopEntity> addableStops,
            List<DispatchMapOverview.DispatchMapMarker> markers,
            List<DispatchMapOverview.DispatchMapMissingStop> missing) {
        if (addableStops == null) {
            return;
        }
        for (NxCommunityDispatchStopEntity stop : addableStops) {
            if (stop == null || stop.getNxCdsAddressId() == null) {
                continue;
            }
            Double lat = parseCoordinate(stop.getNxCdsLat());
            Double lng = parseCoordinate(stop.getNxCdsLng());
            String customerName = stop.getNxCdsCustomerName() != null
                    ? stop.getNxCdsCustomerName()
                    : (stop.getNxCdsAddressText() != null ? stop.getNxCdsAddressText() : "站点");
            String sandboxStopKey = CommunityDispatchConstants.sandboxKeyForAddress(stop.getNxCdsAddressId());
            if (lat == null || lng == null) {
                missing.add(buildMissingStop(customerName, stop.getNxCdsAddressId(), sandboxStopKey, true));
                continue;
            }
            DispatchMapOverview.DispatchMapMarker marker = new DispatchMapOverview.DispatchMapMarker();
            marker.setMarkerType("UNASSIGNED");
            marker.setMarkerKey("unassigned_" + sandboxStopKey);
            marker.setLat(lat);
            marker.setLng(lng);
            marker.setCustomerName(customerName);
            marker.setDisplayTitle(customerName);
            marker.setTitle(customerName);
            marker.setAssignmentLabel("未分配");
            marker.setBadgeText("?");
            marker.setColorKey("unassigned");
            marker.setColor("#9CA3AF");
            marker.setSandboxStopKey(sandboxStopKey);
            markers.add(marker);
        }
    }

    private void appendUnassignedMarkers(
            CommunityDispatchSandboxResult result,
            List<DispatchMapOverview.DispatchMapMarker> markers,
            List<DispatchMapOverview.DispatchMapMissingStop> missing) {
        if (result.getUnassignedStopGroups() == null) {
            return;
        }
        for (Map.Entry<Integer, List<NxCommunityOrdersEntity>> entry : result.getUnassignedStopGroups().entrySet()) {
            Integer addressId = entry.getKey();
            List<NxCommunityOrdersEntity> orders = entry.getValue();
            if (orders == null || orders.isEmpty()) {
                continue;
            }
            NxCustomerUserAddressEntity address = nxCustomerUserAddressDao.queryObject(addressId);
            Double lat = address != null ? parseCoordinate(address.getNxCuaLat()) : null;
            Double lng = address != null ? parseCoordinate(address.getNxCuaLng()) : null;
            String customerName = CommunityDispatchStopPresentationHelper.resolveCustomerName(
                    address, orders.get(0));
            if (lat == null || lng == null) {
                missing.add(buildMissingStop(customerName, addressId,
                        CommunityDispatchConstants.sandboxKeyForAddress(addressId), true));
                continue;
            }
            DispatchMapOverview.DispatchMapMarker marker = new DispatchMapOverview.DispatchMapMarker();
            marker.setMarkerType("UNASSIGNED");
            marker.setMarkerKey("unassigned_" + CommunityDispatchConstants.sandboxKeyForAddress(addressId));
            marker.setLat(lat);
            marker.setLng(lng);
            marker.setCustomerName(customerName);
            marker.setDisplayTitle(customerName);
            marker.setTitle(customerName);
            marker.setAssignmentLabel("未分配");
            marker.setBadgeText("?");
            marker.setColorKey("unassigned");
            marker.setColor("#9CA3AF");
            marker.setSandboxStopKey(CommunityDispatchConstants.sandboxKeyForAddress(addressId));
            markers.add(marker);
        }
    }

    private void appendRouteMarkers(
            List<NxCommunityDispatchDriverRouteEntity> routes,
            Map<Integer, String> driverNames,
            List<DispatchMapOverview.DispatchMapMarker> markers,
            List<DispatchMapOverview.DispatchMapMissingStop> missing) {
        if (routes == null) {
            return;
        }
        int colorIdx = 0;
        for (NxCommunityDispatchDriverRouteEntity route : routes) {
            String color = DRIVER_COLORS[colorIdx % DRIVER_COLORS.length];
            String colorKey = DRIVER_COLOR_KEYS[colorIdx % DRIVER_COLOR_KEYS.length];
            String driverName = driverNames.get(route.getNxCddrDriverUserId());
            List<NxCommunityDispatchStopEntity> stops = sortStops(route.getStops());
            int seq = 0;
            for (NxCommunityDispatchStopEntity stop : stops) {
                Double lat = parseCoordinate(stop.getNxCdsLat());
                Double lng = parseCoordinate(stop.getNxCdsLng());
                String customerName = stop.getNxCdsCustomerName() != null
                        ? stop.getNxCdsCustomerName()
                        : (stop.getNxCdsAddressText() != null ? stop.getNxCdsAddressText() : "站点");
                if (lat == null || lng == null) {
                    missing.add(buildMissingStop(customerName, stop.getNxCdsAddressId(),
                            CommunityDispatchConstants.sandboxKeyForAddress(stop.getNxCdsAddressId()), false));
                    continue;
                }
                seq++;
                DispatchMapOverview.DispatchMapMarker marker = new DispatchMapOverview.DispatchMapMarker();
                marker.setMarkerType("CUSTOMER");
                String markerKey = stop.getNxCommunityDispatchStopId() != null
                        ? ("stop_" + stop.getNxCommunityDispatchStopId())
                        : ("sandbox_" + CommunityDispatchConstants.sandboxKeyForAddress(stop.getNxCdsAddressId()));
                marker.setMarkerKey(markerKey);
                marker.setLat(lat);
                marker.setLng(lng);
                marker.setCustomerName(customerName);
                marker.setDisplayTitle(customerName);
                marker.setTitle(customerName);
                marker.setDriverUserId(route.getNxCddrDriverUserId());
                marker.setDriverName(driverName);
                marker.setDriverRouteId(route.getNxCommunityDispatchDriverRouteId());
                marker.setStopSeq(seq);
                marker.setBadgeText(String.valueOf(seq));
                marker.setAssignmentLabel(driverName);
                marker.setColorKey(colorKey);
                marker.setColor(color);
                markers.add(marker);
            }
            colorIdx++;
        }
    }

    private List<DispatchMapOverview.DispatchMapPolyline> buildPolylines(
            List<NxCommunityDispatchDriverRouteEntity> routes,
            DispatchMapOverview.DispatchMapDepot depot) {
        List<DispatchMapOverview.DispatchMapPolyline> polylines = new ArrayList<DispatchMapOverview.DispatchMapPolyline>();
        if (routes == null || depot == null
                || depot.getLat() == null || depot.getLng() == null) {
            return polylines;
        }
        int colorIdx = 0;
        for (NxCommunityDispatchDriverRouteEntity route : routes) {
            List<NxCommunityDispatchStopEntity> stops = sortStops(route.getStops());
            if (stops == null || stops.isEmpty()) {
                continue;
            }
            List<DispatchMapOverview.DispatchMapPoint> points = new ArrayList<DispatchMapOverview.DispatchMapPoint>();
            points.add(point(depot.getLat(), depot.getLng()));
            for (NxCommunityDispatchStopEntity stop : stops) {
                Double lat = parseCoordinate(stop.getNxCdsLat());
                Double lng = parseCoordinate(stop.getNxCdsLng());
                if (lat != null && lng != null) {
                    points.add(point(lat, lng));
                }
            }
            points.add(point(depot.getLat(), depot.getLng()));
            if (points.size() < 3) {
                colorIdx++;
                continue;
            }
            DispatchMapOverview.DispatchMapPolyline polyline = new DispatchMapOverview.DispatchMapPolyline();
            polyline.setPolylineKey(route.getNxCommunityDispatchDriverRouteId() != null
                    ? ("route_" + route.getNxCommunityDispatchDriverRouteId())
                    : ("route_driver_" + route.getNxCddrDriverUserId()));
            polyline.setRouteKey("driver-" + route.getNxCddrDriverUserId());
            polyline.setDriverRouteId(route.getNxCommunityDispatchDriverRouteId());
            polyline.setDriverUserId(route.getNxCddrDriverUserId());
            polyline.setColor(DRIVER_COLORS[colorIdx % DRIVER_COLORS.length]);
            polyline.setColorKey(DRIVER_COLOR_KEYS[colorIdx % DRIVER_COLOR_KEYS.length]);
            polyline.setKind("DRIVER");
            polyline.setLineStyle("solid");
            polyline.setLineType("STRAIGHT");
            polyline.setPoints(points);
            polylines.add(polyline);
            colorIdx++;
        }
        return polylines;
    }

    private List<DispatchMapOverview.DispatchMapLegendItem> buildLegend(
            List<NxCommunityDispatchDriverRouteEntity> routes,
            DispatchPageMode pageMode,
            Map<Integer, String> driverNames,
            DispatchMapOverview.DispatchMapDepot depot,
            int unassignedCount) {
        List<DispatchMapOverview.DispatchMapLegendItem> legend = new ArrayList<DispatchMapOverview.DispatchMapLegendItem>();
        if (depot != null && depot.getLat() != null && depot.getLng() != null) {
            DispatchMapOverview.DispatchMapLegendItem depotItem = new DispatchMapOverview.DispatchMapLegendItem();
            depotItem.setKind("DEPOT");
            depotItem.setColorKey("depot");
            depotItem.setColor("#111827");
            depotItem.setLabel(depot.getName() != null ? depot.getName() : "门店");
            depotItem.setLineStyle("solid");
            legend.add(depotItem);
        }
        if (routes != null) {
            int colorIdx = 0;
            for (NxCommunityDispatchDriverRouteEntity route : routes) {
                if (route.getNxCddrDriverUserId() == null) {
                    continue;
                }
                DispatchMapOverview.DispatchMapLegendItem item = new DispatchMapOverview.DispatchMapLegendItem();
                item.setKind("DRIVER");
                item.setColorKey(DRIVER_COLOR_KEYS[colorIdx % DRIVER_COLOR_KEYS.length]);
                item.setColor(DRIVER_COLORS[colorIdx % DRIVER_COLORS.length]);
                item.setLabel(driverNames.get(route.getNxCddrDriverUserId()));
                item.setLineStyle("solid");
                item.setDriverUserId(route.getNxCddrDriverUserId());
                legend.add(item);
                colorIdx++;
            }
        }
        if (pageMode == DispatchPageMode.DISPATCH_SANDBOX && unassignedCount > 0) {
            DispatchMapOverview.DispatchMapLegendItem unassigned = new DispatchMapOverview.DispatchMapLegendItem();
            unassigned.setKind("UNASSIGNED");
            unassigned.setColorKey("unassigned");
            unassigned.setColor("#9CA3AF");
            unassigned.setLabel("未分配");
            unassigned.setLineStyle("solid");
            legend.add(unassigned);
        }
        return legend;
    }

    private void applyViewport(DispatchMapOverview overview, List<DispatchMapOverview.DispatchMapMarker> markers) {
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        int count = 0;
        if (markers != null) {
            for (DispatchMapOverview.DispatchMapMarker marker : markers) {
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
        if (count <= 0 && overview.getDepot() != null
                && overview.getDepot().getLat() != null && overview.getDepot().getLng() != null) {
            minLat = maxLat = overview.getDepot().getLat();
            minLng = maxLng = overview.getDepot().getLng();
            count = 1;
        }
        if (count <= 0) {
            return;
        }
        double latSpan = Math.max(maxLat - minLat, MIN_VIEWPORT_SPAN_DEG);
        double lngSpan = Math.max(maxLng - minLng, MIN_VIEWPORT_SPAN_DEG);
        overview.setCenterLat((minLat + maxLat) / 2.0);
        overview.setCenterLng((minLng + maxLng) / 2.0);
        overview.setSuggestedScale(suggestScale(latSpan * 1.56, lngSpan * 1.56));
        overview.setZoomLevel(overview.getSuggestedScale());
    }

    private int suggestScale(double latSpan, double lngSpan) {
        double span = Math.max(latSpan, lngSpan);
        if (span <= 0.015) {
            return 15;
        }
        if (span <= 0.03) {
            return 14;
        }
        if (span <= 0.06) {
            return 13;
        }
        if (span <= 0.12) {
            return 12;
        }
        return 11;
    }

    private List<NxCommunityDispatchStopEntity> sortStops(List<NxCommunityDispatchStopEntity> stops) {
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

    private DispatchMapOverview.DispatchMapMissingStop buildMissingStop(
            String customerName, Integer addressId, String sandboxStopKey, boolean unassigned) {
        DispatchMapOverview.DispatchMapMissingStop missing = new DispatchMapOverview.DispatchMapMissingStop();
        missing.setCustomerName(customerName);
        missing.setAddressId(addressId);
        missing.setSandboxStopKey(sandboxStopKey);
        missing.setUnassigned(unassigned);
        return missing;
    }

    private DispatchMapOverview.DispatchMapPoint point(Double lat, Double lng) {
        DispatchMapOverview.DispatchMapPoint point = new DispatchMapOverview.DispatchMapPoint();
        point.setLat(lat);
        point.setLng(lng);
        return point;
    }

    private String buildCustomerLabel(NxCommunityOrdersEntity order) {
        return "订单#" + order.getNxCommunityOrdersId();
    }

    private Double parseCoordinate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
