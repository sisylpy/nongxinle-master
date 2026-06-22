package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Phase 3f：路线级进入装车 / 撤销回今日派单 */
@Setter
@Getter
@ToString
public class DriverRouteLoadingGateRequest {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private Integer operatorUserId;
    private Integer driverUserId;
}
