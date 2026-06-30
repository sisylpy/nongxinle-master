package com.nongxinle.route.dispatch.strategy;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DriverRoutePlan {
    private Integer driverUserId;
    private String driverName;
    private String routeScheduleMode;
    /** PR-2c：建议出发（routeDate 当天 0 点起秒数）。 */
    private Integer suggestedDepartTimeS;
    private Integer plannedDepartTimeS;
    private String suggestedDepartReason;
    private String suggestedDepartReasonLabel;
    private List<StopAssignment> stops = new ArrayList<StopAssignment>();
}
