package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 人工调度（单站选司机）基础请求。 */
@Getter
@Setter
public class SandboxManualDispatchBaseRequest {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private Integer operatorUserId;
    private Integer departmentId;
    private Integer depFatherId;
    private String sandboxStopKey;
    private List<Integer> liveOrderIds = new ArrayList<Integer>();
}
