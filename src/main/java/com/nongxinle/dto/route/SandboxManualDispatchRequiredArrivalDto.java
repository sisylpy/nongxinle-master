package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SandboxManualDispatchRequiredArrivalDto {
    private String deadlineAt;
    private String deadlineLabel;
    private String plannedArrivalAt;
    private String plannedArrivalLabel;
    private Boolean feasible;
    private Integer slackMinutes;
    private Integer violationMinutes;
}
