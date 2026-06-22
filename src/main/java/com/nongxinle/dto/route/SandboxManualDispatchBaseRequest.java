package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class SandboxManualDispatchBaseRequest {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private Integer operatorUserId;
    private Integer departmentId;
    private Integer depFatherId;
    private String sandboxStopKey;
    private List<Integer> liveOrderIds;
}
