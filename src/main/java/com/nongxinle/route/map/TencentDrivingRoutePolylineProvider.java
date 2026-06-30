package com.nongxinle.route.map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.dto.route.SandboxTodayMapPointDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 腾讯驾车路线 polyline（仅 mapOverview 展示，不参与 optimizer / 距离矩阵）。
 */
@Component
public class TencentDrivingRoutePolylineProvider {

    private static final int CONNECT_TIMEOUT_MS = 5000;
    /** 仅 mapOverview 展示，失败会降级直线，不宜长时间阻塞页面。 */
    private static final int READ_TIMEOUT_MS = 10000;

    @Value("${tencent.map.key:C5HBZ-KEIW2-JXXUJ-COLGS-FQO47-WWFAK}")
    private String mapKey;

    /**
     * 请求 from→to 驾车路线点串；调用方可传入 request 级 cache 避免重复请求。
     */
    public List<SandboxTodayMapPointDto> fetchDrivingPolyline(double fromLat,
                                                              double fromLng,
                                                              double toLat,
                                                              double toLng,
                                                              Map<String, List<SandboxTodayMapPointDto>> legCache)
            throws IOException {
        String cacheKey = legCacheKey(fromLat, fromLng, toLat, toLng);
        if (legCache != null) {
            List<SandboxTodayMapPointDto> cached = legCache.get(cacheKey);
            if (cached != null) {
                return copyPoints(cached);
            }
        }

        String fromCoord = formatCoord(fromLat, fromLng);
        String toCoord = formatCoord(toLat, toLng);
        String urlString = "https://apis.map.qq.com/ws/direction/v1/driving/?from="
                + urlEncode(fromCoord) + "&to=" + urlEncode(toCoord) + "&key=" + urlEncode(mapKey);
        JSONObject jsonObject = requestTencentMapJson(urlString);
        if (jsonObject == null || jsonObject.getInteger("status") == null || jsonObject.getInteger("status") != 0) {
            String msg = jsonObject != null ? jsonObject.getString("message") : "响应为空";
            throw new IOException("腾讯驾车路线接口失败: " + msg);
        }
        JSONObject result = jsonObject.getJSONObject("result");
        if (result == null) {
            throw new IOException("腾讯驾车路线接口 result 为空");
        }
        JSONArray routes = result.getJSONArray("routes");
        if (routes == null || routes.isEmpty()) {
            throw new IOException("腾讯驾车路线接口 routes 为空");
        }
        JSONArray polyline = routes.getJSONObject(0).getJSONArray("polyline");
        List<SandboxTodayMapPointDto> points = decodeTencentPolyline(polyline);
        if (points.isEmpty()) {
            throw new IOException("腾讯驾车路线 polyline 解码为空");
        }
        if (legCache != null) {
            legCache.put(cacheKey, copyPoints(points));
        }
        return points;
    }

    static List<SandboxTodayMapPointDto> decodeTencentPolyline(JSONArray polyline) {
        List<SandboxTodayMapPointDto> points = new ArrayList<SandboxTodayMapPointDto>();
        if (polyline == null || polyline.size() < 2) {
            return points;
        }
        int size = polyline.size();
        double[] coors = new double[size];
        for (int i = 0; i < size; i++) {
            coors[i] = polyline.getDoubleValue(i);
        }
        for (int i = 2; i < size; i++) {
            coors[i] = coors[i - 2] + coors[i] / 1_000_000.0;
        }
        for (int i = 0; i + 1 < size; i += 2) {
            SandboxTodayMapPointDto point = new SandboxTodayMapPointDto();
            point.setLat(coors[i]);
            point.setLng(coors[i + 1]);
            points.add(point);
        }
        return points;
    }

    static String legCacheKey(double fromLat, double fromLng, double toLat, double toLng) {
        return String.format(Locale.US, "%.5f,%.5f|%.5f,%.5f", fromLat, fromLng, toLat, toLng);
    }

    private static String formatCoord(double lat, double lng) {
        return String.format(Locale.US, "%.6f,%.6f", lat, lng);
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception ex) {
            return value;
        }
    }

    private static List<SandboxTodayMapPointDto> copyPoints(List<SandboxTodayMapPointDto> source) {
        List<SandboxTodayMapPointDto> copy = new ArrayList<SandboxTodayMapPointDto>(source.size());
        for (SandboxTodayMapPointDto point : source) {
            if (point == null) {
                continue;
            }
            SandboxTodayMapPointDto cloned = new SandboxTodayMapPointDto();
            cloned.setLat(point.getLat());
            cloned.setLng(point.getLng());
            copy.add(cloned);
        }
        return copy;
    }

    private JSONObject requestTencentMapJson(String urlString) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line).append('\n');
            }
        }
        return JSONObject.parseObject(result.toString());
    }
}
