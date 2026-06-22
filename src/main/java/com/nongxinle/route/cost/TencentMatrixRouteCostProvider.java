package com.nongxinle.route.cost;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.route.DisRouteDistanceTypes;
import com.nongxinle.route.RouteCoordinateUtils;
import com.nongxinle.route.RouteCostProvider;
import com.nongxinle.route.RouteCostProviderType;
import com.nongxinle.route.model.CostMatrix;
import com.nongxinle.route.model.GeoPoint;
import com.nongxinle.route.model.RouteStopInput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class TencentMatrixRouteCostProvider implements RouteCostProvider {

    private static final int TENCENT_MATRIX_TO_MAX = 10;

    @Value("${tencent.map.key:C5HBZ-KEIW2-JXXUJ-COLGS-FQO47-WWFAK}")
    private String mapKey;

    @Override
    public RouteCostProviderType providerType() {
        return RouteCostProviderType.TENCENT_MATRIX;
    }

    @Override
    public CostMatrix buildMatrix(GeoPoint depot, List<RouteStopInput> stops) throws IOException {
        if (depot == null || !RouteCoordinateUtils.isValidCoordinate(depot.getLat(), depot.getLng())) {
            throw new IOException("出发点坐标无效，无法请求腾讯距离矩阵");
        }
        List<GeoPoint> allPoints = new ArrayList<>();
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
                    distanceM[i][j] = Long.MAX_VALUE / 4;
                    durationS[i][j] = Long.MAX_VALUE / 4;
                    distanceType[i][j] = DisRouteDistanceTypes.ROUTE_DISTANCE;
                }
            }
        }

        for (int from = 0; from < n; from++) {
            GeoPoint fromPoint = allPoints.get(from);
            List<GeoPoint> targets = new ArrayList<>();
            List<Integer> targetIndexes = new ArrayList<>();
            for (int to = 0; to < n; to++) {
                if (from == to) {
                    continue;
                }
                targets.add(allPoints.get(to));
                targetIndexes.add(to);
            }
            MatrixLeg[] legs;
            try {
                legs = queryMatrixLegs(fromPoint, targets);
            } catch (IOException ex) {
                legs = fallbackLegs(fromPoint, targets);
            }
            for (int k = 0; k < legs.length; k++) {
                int to = targetIndexes.get(k);
                distanceM[from][to] = legs[k].distanceM;
                durationS[from][to] = legs[k].durationS;
                distanceType[from][to] = legs[k].distanceType;
            }
            if (from > 0) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        CostMatrix matrix = new CostMatrix();
        matrix.setDepot(depot);
        matrix.setStops(stops);
        matrix.setDistanceM(distanceM);
        matrix.setDurationS(durationS);
        matrix.setDistanceType(distanceType);
        matrix.setDistanceProvider(DisRouteDistanceTypes.PROVIDER_TENCENT_MATRIX);
        return matrix;
    }

    private MatrixLeg[] fallbackLegs(GeoPoint from, List<GeoPoint> targets) {
        MatrixLeg[] legs = new MatrixLeg[targets.size()];
        for (int i = 0; i < targets.size(); i++) {
            MatrixLeg leg = new MatrixLeg();
            leg.distanceM = HaversineStraightRouteCostProvider.haversineMeters(from, targets.get(i));
            leg.durationS = Math.max(60L, Math.round(leg.distanceM / (30_000.0 / 3600.0)));
            leg.distanceType = DisRouteDistanceTypes.ESTIMATED_STRAIGHT_DISTANCE;
            legs[i] = leg;
        }
        return legs;
    }

    private MatrixLeg[] queryMatrixLegs(GeoPoint from, List<GeoPoint> targets) throws IOException {
        MatrixLeg[] legs = new MatrixLeg[targets.size()];
        String fromCoord = from.getLat() + "," + from.getLng();
        for (int start = 0; start < targets.size(); start += TENCENT_MATRIX_TO_MAX) {
            int end = Math.min(start + TENCENT_MATRIX_TO_MAX, targets.size());
            StringBuilder toBuilder = new StringBuilder();
            for (int i = start; i < end; i++) {
                GeoPoint p = targets.get(i);
                toBuilder.append(p.getLat()).append(",").append(p.getLng());
                if (i < end - 1) {
                    toBuilder.append(";");
                }
            }
            String urlString = "https://apis.map.qq.com/ws/distance/v1/matrix?mode=driving&from="
                    + fromCoord + "&to=" + toBuilder + "&key=" + mapKey;
            JSONObject jsonObject = requestTencentMapJson(urlString);
            if (jsonObject == null || jsonObject.getInteger("status") == null || jsonObject.getInteger("status") != 0) {
                String msg = jsonObject != null ? jsonObject.getString("message") : "响应为空";
                throw new IOException("腾讯距离矩阵接口失败: " + msg);
            }
            JSONObject resObj = jsonObject.getJSONObject("result");
            JSONArray rows = resObj.getJSONArray("rows");
            if (rows == null || rows.isEmpty()) {
                throw new IOException("腾讯距离矩阵接口 rows 为空");
            }
            JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");
            for (int j = 0; j < end - start; j++) {
                JSONObject element = elements.getJSONObject(j);
                MatrixLeg leg = new MatrixLeg();
                leg.distanceM = parseLongSafe(element.getString("distance"));
                leg.durationS = parseLongSafe(element.getString("duration"));
                leg.distanceType = DisRouteDistanceTypes.ROUTE_DISTANCE;
                legs[start + j] = leg;
            }
        }
        return legs;
    }

    private JSONObject requestTencentMapJson(String urlString) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line).append('\n');
            }
        }
        return JSONObject.parseObject(result.toString());
    }

    private long parseLongSafe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Long.MAX_VALUE / 4;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE / 4;
        }
    }

    private static class MatrixLeg {
        long distanceM;
        long durationS;
        String distanceType;
    }
}
