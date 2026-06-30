package com.nongxinle.route;

import com.nongxinle.route.model.CostMatrix;
import com.nongxinle.route.model.GeoPoint;
import com.nongxinle.route.model.RouteStopInput;

import java.io.IOException;
import java.util.List;

public interface RouteCostProvider {

    RouteCostProviderType providerType();

    CostMatrix buildMatrix(GeoPoint depot, List<RouteStopInput> stops) throws IOException;
}
