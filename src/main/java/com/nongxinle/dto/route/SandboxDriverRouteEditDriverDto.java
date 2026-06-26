package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SandboxDriverRouteEditDriverDto {
    private Integer driverUserId;
    private String driverName;
    private String routeSummary;
    private Integer customerStopCount;
    private String totalDistanceText;
    private String totalDurationText;
}
