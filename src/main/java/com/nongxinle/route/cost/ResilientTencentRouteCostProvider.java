package com.nongxinle.route.cost;

import com.nongxinle.route.DisRouteDistanceTypes;
import com.nongxinle.route.RouteCoordinateUtils;
import com.nongxinle.route.RouteCostProvider;
import com.nongxinle.route.RouteCostProviderType;
import com.nongxinle.route.model.CostMatrix;
import com.nongxinle.route.model.GeoPoint;
import com.nongxinle.route.model.RouteStopInput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 优先腾讯驾车矩阵；单段失败时该段 fallback 为直线估算，并标记 distanceType。
 */
public class ResilientTencentRouteCostProvider implements RouteCostProvider {

    private final RouteCostProvider primary;
    private final HaversineStraightRouteCostProvider fallback = new HaversineStraightRouteCostProvider();

    public ResilientTencentRouteCostProvider(RouteCostProvider primary) {
        this.primary = primary;
    }

    @Override
    public RouteCostProviderType providerType() {
        return primary != null ? primary.providerType() : RouteCostProviderType.TENCENT_MATRIX;
    }

    @Override
    public CostMatrix buildMatrix(GeoPoint depot, List<RouteStopInput> stops) throws IOException {
        if (depot == null || !RouteCoordinateUtils.isValidCoordinate(depot.getLat(), depot.getLng())) {
            return fallback.buildMatrix(depot, stops);
        }
        try {
            CostMatrix matrix = primary.buildMatrix(depot, stops);
            if (matrix.getDistanceProvider() == null) {
                matrix.setDistanceProvider(DisRouteDistanceTypes.PROVIDER_TENCENT_MATRIX);
            }
            ensureDistanceTypeMatrix(matrix, DisRouteDistanceTypes.ROUTE_DISTANCE);
            return matrix;
        } catch (IOException ex) {
            return fallback.buildMatrix(depot, stops);
        } catch (RuntimeException ex) {
            return fallback.buildMatrix(depot, stops);
        }
    }

    private static void ensureDistanceTypeMatrix(CostMatrix matrix, String defaultType) {
        if (matrix == null || matrix.getDistanceM() == null) {
            return;
        }
        int n = matrix.getDistanceM().length;
        if (matrix.getDistanceType() != null && matrix.getDistanceType().length == n) {
            return;
        }
        String[][] types = new String[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                types[i][j] = defaultType;
            }
        }
        matrix.setDistanceType(types);
    }
}
