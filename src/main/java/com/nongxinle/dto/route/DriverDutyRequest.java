package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DriverDutyRequest {
    private Integer disId;
    private Integer driverUserId;
    /** 派车日，不传默认今天 */
    private String dutyDate;
    private Integer operatorUserId;
}
