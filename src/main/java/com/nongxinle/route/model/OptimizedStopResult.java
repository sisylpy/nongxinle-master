package com.nongxinle.route.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class OptimizedStopResult {
    private Integer departmentId;
    private String departmentName;
    private GeoPoint location;
    private String address;
    private int orderCount;
    private int stopSeq;
    private long legDistanceM;
    private long legDurationS;
}
