package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SandboxDriverRouteEditConfirmRequest {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private Integer driverUserId;
    private Integer operatorUserId;
    private List<Integer> liveOrderIds = new ArrayList<Integer>();
    private List<String> stopKeys = new ArrayList<String>();
    private String confirmReason;
}
