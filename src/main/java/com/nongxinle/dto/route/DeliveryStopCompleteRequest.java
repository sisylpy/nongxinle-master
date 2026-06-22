package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DeliveryStopCompleteRequest {
    private Integer operatorUserId;
    private String remark;
}
