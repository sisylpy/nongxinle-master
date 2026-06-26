package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SandboxDriverRouteEditStopDto {
    private String stopKey;
    private Integer departmentId;
    private String customerName;
    private String goodsSummary;
    private String plannedArrivalLabel;
    private String plannedDepartureLabel;
    private String windowLabel;
    private Boolean locked;
    private String lockReason;
}
