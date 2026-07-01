package com.nongxinle.dispatch.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class DispatchRoute {

    private Integer driverRouteId;
    private Integer planId;
    private Integer driverUserId;
    private String driverName;
    private DispatchRouteStatus routeStatus;
    private int stopCount;
    private Date loadingEnteredAt;
    private Date actualDepartAt;
    private boolean showDepartAction;
    private List<DispatchStop> stops = new ArrayList<DispatchStop>();
    private boolean simulated;
}
