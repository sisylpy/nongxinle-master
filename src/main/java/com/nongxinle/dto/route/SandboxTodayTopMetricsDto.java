package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SandboxTodayTopMetricsDto {
    private Integer driverCount;
    private Integer availableDriverCount;
    private Integer customerStopCount;
    private Long totalDistanceM;
    private Long totalDurationS;
    private String totalDistanceText;
    private String totalDurationText;
}
