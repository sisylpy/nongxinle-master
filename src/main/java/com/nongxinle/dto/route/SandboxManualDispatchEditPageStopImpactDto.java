package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 插入模拟后，相对原路线的到达变化（仅模拟路线店卡使用）。 */
@Setter
@Getter
@ToString
public class SandboxManualDispatchEditPageStopImpactDto {
    private String plannedArrivalLabelBefore;
    private String plannedArrivalLabelAfter;
    private Integer arrivalDeltaMinutes;
    private String timeWindowImpactLabel;
}
