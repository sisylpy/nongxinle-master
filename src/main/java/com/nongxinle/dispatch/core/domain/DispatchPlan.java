package com.nongxinle.dispatch.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 某日某租户派单计划聚合根。 */
@Getter
@Setter
public class DispatchPlan {

    private Integer planId;
    private DispatchTenantRef tenant;
    private String routeDate;
    private String planStatus;
    private DispatchDepot depot;
    private List<DispatchRoute> routes = new ArrayList<DispatchRoute>();
    private List<DispatchStop> confirmedStops = new ArrayList<DispatchStop>();
}
