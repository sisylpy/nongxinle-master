package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 沙箱站点当日送达时间窗 override（confirm 前无 taskId 时使用）。 */
@Setter
@Getter
@ToString
public class SandboxStopTimeWindowRequest {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private Integer departmentId;
    private Integer depFatherId;
    private String sandboxStopKey;
    /** 已 confirm 时可传，走 task 级 override */
    private Integer deliveryStopId;
    private Integer earliestDeliveryTimeS;
    private Integer latestDeliveryTimeS;
    private Integer serviceMinutes;
    private String reason;
    private Integer operatorUserId;
    /** 前端回刷页：LOADING 时返回装车 pageViewModel */
    private String responsePage;
}
