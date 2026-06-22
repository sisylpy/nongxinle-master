package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DeliveryStopExceptionRequest {
    private Integer operatorUserId;
    private String exceptionType;
    private String remark;
}
