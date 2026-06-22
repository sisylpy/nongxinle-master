package com.nongxinle.route.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class OptimizedDriverRouteResult {
    private Integer driverUserId;
    private String driverName;
    private int routeSeq;
    private long totalDistanceM;
    private long totalDurationS;
    private long returnLegDistanceM;
    private long returnLegDurationS;
    private String returnLegDistanceType;
    private String distanceProvider;
    /** 路线中若任一段为直线估算则为 ESTIMATED_STRAIGHT_DISTANCE，否则 ROUTE_DISTANCE */
    private String routeDistanceType;
    private List<OptimizedStopResult> stops = new ArrayList<>();
}
