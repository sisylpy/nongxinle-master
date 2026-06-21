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
    private List<OptimizedStopResult> stops = new ArrayList<>();
}
