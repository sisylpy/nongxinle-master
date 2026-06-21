package com.nongxinle.route.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class RouteOptimizeResult {
    private List<OptimizedDriverRouteResult> driverRoutes = new ArrayList<>();
    private long totalDistanceM;
    private long totalDurationS;
}
