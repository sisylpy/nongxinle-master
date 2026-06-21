package com.nongxinle.route;

import com.nongxinle.route.model.CostMatrix;
import com.nongxinle.route.model.GeoPoint;
import com.nongxinle.route.model.RouteStopInput;

import java.io.IOException;
import java.util.List;

/**
 * 路网距离/时间提供者，可替换为腾讯/高德/OR-Tools 等实现。
 */
public interface RouteCostProvider {

    RouteCostProviderType providerType();

    CostMatrix buildMatrix(GeoPoint depot, List<RouteStopInput> stops) throws IOException;
}
