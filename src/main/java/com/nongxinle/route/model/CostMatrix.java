package com.nongxinle.route.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class CostMatrix {
    private GeoPoint depot;
    private List<RouteStopInput> stops = new ArrayList<>();
    /** distanceM[i][j] */
    private long[][] distanceM;
    /** durationS[i][j] */
    private long[][] durationS;
    /** distanceType[i][j]：ROUTE_DISTANCE | ESTIMATED_STRAIGHT_DISTANCE */
    private String[][] distanceType;
    /** 矩阵主提供方：TENCENT_MATRIX | HAVERSINE */
    private String distanceProvider;

    public int size() {
        return stops.size() + 1;
    }

    public long distance(int fromIdx, int toIdx) {
        return distanceM[fromIdx][toIdx];
    }

    public long duration(int fromIdx, int toIdx) {
        return durationS[fromIdx][toIdx];
    }

    public String distanceType(int fromIdx, int toIdx) {
        if (distanceType == null || fromIdx < 0 || toIdx < 0
                || fromIdx >= distanceType.length || toIdx >= distanceType[fromIdx].length) {
            return null;
        }
        return distanceType[fromIdx][toIdx];
    }
}
