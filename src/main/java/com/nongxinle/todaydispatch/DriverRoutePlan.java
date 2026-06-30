package com.nongxinle.todaydispatch;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** 分派中一条司机建议路线。 */
@Getter
@Setter
public class DriverRoutePlan {

    private Integer driverUserId;
    private String driverName;
    private List<CustomerStopPlan> stops = new ArrayList<CustomerStopPlan>();
    private String badgeLabel;
    private String scheduleHeadline;
    private String routeSummary;
    private String routeStatsLine;
    private String routeHeadlineLine;
    private String routeRoundTripSummaryLine;
    private String totalDistanceText;
    private String totalDurationText;
    private String totalRoundTripDistanceText;
    private String totalRoundTripDurationText;
    private String outboundDistanceText;
    private String returnDistanceText;
    private Long outboundDistanceM;
    private Long outboundDurationS;
    private Date plannedDepartAt;
    private Date plannedReturnAt;
    private Long totalDistanceM;
    private Long totalDurationS;
    private Long returnLegDistanceM;
    private Long returnLegDurationS;
    private String returnLegLabel;
    private String plannedDepartLabel;
    private String plannedReturnLabel;
    private String recommendedDepartLabel;
    private String routeSuggestedDepartLabel;
    private String firstStopWindowLabel;
    private String firstStopPlannedArrivalLabel;
    private String firstStopPlannedArrivalTimeLabel;
    private String firstStopArrivalStatusLabel;
    private String firstStopArrivalStatusTone;
    private Integer firstStopWindowStartS;
    private Integer firstStopWindowEndS;
    private Map<String, Object> primaryAction;
    private Map<String, Object> routeEditAction;
}
