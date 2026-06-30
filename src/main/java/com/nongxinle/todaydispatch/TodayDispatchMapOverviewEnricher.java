package com.nongxinle.todaydispatch;

import com.nongxinle.dto.route.SandboxTodayMapOverviewDto;
import com.nongxinle.dto.route.SandboxTodayMapPointDto;
import com.nongxinle.dto.route.SandboxTodayMapPolylineDto;
import com.nongxinle.route.DisRouteSandboxMapViewportHelper;
import com.nongxinle.route.DisRouteSandboxTodayMapRoadPolylineEnricher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 将 DispatchPageAssembler 的 mapOverview 升级为道路 polyline + 标准 viewport。 */
@Component
public class TodayDispatchMapOverviewEnricher {

    @Autowired
    private DisRouteSandboxTodayMapRoadPolylineEnricher mapRoadPolylineEnricher;

    public void enrich(Map<String, Object> overview) {
        if (overview == null || overview.isEmpty()) {
            return;
        }
        SandboxTodayMapOverviewDto dto = toDto(overview);
        if (dto.getPolylines() == null || dto.getPolylines().isEmpty()) {
            return;
        }
        mapRoadPolylineEnricher.enrichDriverRoutes(dto);
        DisRouteSandboxMapViewportHelper.refreshViewport(dto);
        applyDtoBack(overview, dto);
    }

    private static SandboxTodayMapOverviewDto toDto(Map<String, Object> overview) {
        SandboxTodayMapOverviewDto dto = new SandboxTodayMapOverviewDto();
        dto.setCenterLat(toDouble(overview.get("centerLat")));
        dto.setCenterLng(toDouble(overview.get("centerLng")));
        dto.setSuggestedScale(toInteger(overview.get("suggestedScale")));
        dto.setSubkey(toString(overview.get("subkey")));
        dto.setLayerStyle(toInteger(overview.get("layerStyle")));
        dto.setEmptyHint(toString(overview.get("emptyHint")));
        dto.setPolylines(toPolylineDtos(castList(overview.get("polylines"))));
        return dto;
    }

    private static void applyDtoBack(Map<String, Object> overview, SandboxTodayMapOverviewDto dto) {
        if (dto.getCenterLat() != null) {
            overview.put("centerLat", dto.getCenterLat());
        }
        if (dto.getCenterLng() != null) {
            overview.put("centerLng", dto.getCenterLng());
        }
        if (dto.getSuggestedScale() != null) {
            overview.put("suggestedScale", dto.getSuggestedScale());
        }
        overview.put("polylines", fromPolylineDtos(dto.getPolylines()));
    }

    private static List<SandboxTodayMapPolylineDto> toPolylineDtos(List<Map<String, Object>> polylines) {
        List<SandboxTodayMapPolylineDto> list = new ArrayList<SandboxTodayMapPolylineDto>();
        if (polylines == null) {
            return list;
        }
        for (Map<String, Object> line : polylines) {
            if (line == null) {
                continue;
            }
            SandboxTodayMapPolylineDto dto = new SandboxTodayMapPolylineDto();
            dto.setRouteKey(toString(line.get("routeKey")));
            dto.setDriverUserId(toInteger(line.get("driverUserId")));
            dto.setDriverName(toString(line.get("driverName")));
            dto.setColorKey(toString(line.get("colorKey")));
            dto.setColor(toString(line.get("color")));
            dto.setLineStyle(toString(line.get("lineStyle")));
            dto.setKind(toString(line.get("kind")));
            dto.setLineType(toString(line.get("lineType")));
            dto.setPoints(toPointDtos(castList(line.get("points"))));
            list.add(dto);
        }
        return list;
    }

    private static List<Map<String, Object>> fromPolylineDtos(List<SandboxTodayMapPolylineDto> polylines) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (polylines == null) {
            return list;
        }
        for (SandboxTodayMapPolylineDto line : polylines) {
            if (line == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            putIfNotNull(map, "routeKey", line.getRouteKey());
            putIfNotNull(map, "driverUserId", line.getDriverUserId());
            putIfNotNull(map, "driverName", line.getDriverName());
            putIfNotNull(map, "colorKey", line.getColorKey());
            putIfNotNull(map, "color", line.getColor());
            putIfNotNull(map, "lineStyle", line.getLineStyle());
            putIfNotNull(map, "kind", line.getKind());
            putIfNotNull(map, "lineType", line.getLineType());
            map.put("points", fromPointDtos(line.getPoints()));
            list.add(map);
        }
        return list;
    }

    private static List<SandboxTodayMapPointDto> toPointDtos(List<Map<String, Object>> points) {
        List<SandboxTodayMapPointDto> list = new ArrayList<SandboxTodayMapPointDto>();
        if (points == null) {
            return list;
        }
        for (Map<String, Object> point : points) {
            if (point == null) {
                continue;
            }
            Double lat = toDouble(point.get("lat"));
            Double lng = toDouble(point.get("lng"));
            if (lat == null || lng == null) {
                continue;
            }
            SandboxTodayMapPointDto dto = new SandboxTodayMapPointDto();
            dto.setLat(lat);
            dto.setLng(lng);
            list.add(dto);
        }
        return list;
    }

    private static List<Map<String, Object>> fromPointDtos(List<SandboxTodayMapPointDto> points) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (points == null) {
            return list;
        }
        for (SandboxTodayMapPointDto point : points) {
            if (point == null || point.getLat() == null || point.getLng() == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("lat", point.getLat());
            map.put("lng", point.getLng());
            list.add(map);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castList(Object value) {
        if (value instanceof List) {
            return (List<Map<String, Object>>) value;
        }
        return null;
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private static Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.valueOf(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String toString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }
}
