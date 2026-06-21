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

    public int size() {
        return stops.size() + 1;
    }

    public long distance(int fromIdx, int toIdx) {
        return distanceM[fromIdx][toIdx];
    }

    public long duration(int fromIdx, int toIdx) {
        return durationS[fromIdx][toIdx];
    }
}
