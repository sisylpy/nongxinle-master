package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 司机路线编辑 page / preview 请求（todaydispatch 轻量契约，兼容旧前台 payload）。 */
@Getter
@Setter
public class SandboxDriverRouteEditPageRequest {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private Integer driverUserId;
    private Integer operatorUserId;
    private List<String> stopKeys = new ArrayList<String>();
    /** DISPATCH_SANDBOX / LOADING */
    private String sourcePage;
    private Boolean manualDispatch;
    private Integer departmentId;
    private Integer depFatherId;
}
