package com.nongxinle.dto.route;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class DriverRouteTasksResponse {
    private String routeDate;
    private Integer planId;
    private String planStatus;

    /** 当前司机在 plan 下的路线（stops → shipmentTask → items） */
    @Getter(AccessLevel.NONE)
    @JSONField(name = "driverRoute")
    private com.nongxinle.entity.NxDisDriverRouteEntity driverRoute;

    private List<com.nongxinle.entity.NxDisShipmentTaskEntity> tasks;

    @JSONField(name = "driverRoute")
    public com.nongxinle.entity.NxDisDriverRouteEntity getDriverRoute() {
        return driverRoute;
    }

    @JSONField(name = "driverRoute")
    public void setDriverRoute(com.nongxinle.entity.NxDisDriverRouteEntity driverRoute) {
        this.driverRoute = driverRoute;
    }
}
