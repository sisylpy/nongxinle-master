package com.nongxinle.dto.pos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosSettleZeroRequest {
    private Integer orderId;
    private Integer operatorId;
}
