package com.nongxinle.dispatch.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 持久化或模拟站点（领域层，非 pageView）。 */
@Getter
@Setter
public class DispatchStop {

    private Integer stopId;
    private Integer driverRouteId;
    private Integer driverUserId;
    private String sandboxStopKey;
    private Integer addressId;
    private Integer routeSeq;
    private DispatchStopStatus stopStatus;
    private String customerName;
    private String customerPhone;
    private String addressText;
    private Double lat;
    private Double lng;
    private Integer orderCount;
    private List<Integer> orderIds = new ArrayList<Integer>();
    private List<String> goodsSummaries = new ArrayList<String>();
    private boolean simulated;
}
