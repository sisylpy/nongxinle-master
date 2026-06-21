package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DispatchWorkbenchMetricsDto {
    private Integer totalStopCount;
    private Integer driverCount;
    private Integer eligibleDriverCount;
    private Integer lateStopCount;
    private Integer maxLateMinutes;
    private Integer totalLateMinutes;
    private Integer totalWaitMinutes;
    private Integer lockedConflictCount;
}
