package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class SandboxManualDispatchStopImpactDto {
    /** 模拟路线中的送序（1-based）。 */
    private Integer seq;
    private Integer departmentId;
    private String customerName;
    private String plannedArrivalLabelBefore;
    private String plannedArrivalAtBefore;
    private String plannedArrivalLabelAfter;
    private String plannedArrivalAtAfter;
    private Integer arrivalDeltaMinutes;
    private String timeWindowImpactLabel;
    private Boolean insertedStop;
}
