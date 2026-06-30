package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 司机端配送简化操作（完成 / 返回沙盘） */
@Setter
@Getter
@ToString
public class DriverDeliveryNowRequest {
    private Integer operatorUserId;
    private Integer disId;
    private Integer driverUserId;
    private String routeDate;
    private String batchCode;
    private String reason;
}
