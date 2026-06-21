package com.nongxinle.route.model;

import com.nongxinle.route.RouteOptimizerType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class RouteOptimizeRequest {
    private GeoPoint depot;
    private List<RouteStopInput> stops = new ArrayList<>();
    private List<DriverInput> drivers = new ArrayList<>();
    private CostMatrix costMatrix;
    private RouteOptimizerType optimizerType = RouteOptimizerType.BALANCED_INSERTION_2OPT;
}
