package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SandboxDriverRouteEditBaseRequest {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private Integer driverUserId;
    private Integer operatorUserId;

    /** 人工调度：待分派客户部门 id，有值时进入 manualDispatch 模式。 */
    private Integer departmentId;
    private Integer depFatherId;
    private String sandboxStopKey;
    private List<Integer> liveOrderIds = new ArrayList<Integer>();
    private Boolean manualDispatch;
}
