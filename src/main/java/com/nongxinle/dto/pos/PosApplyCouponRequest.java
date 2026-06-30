package com.nongxinle.dto.pos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosApplyCouponRequest {
    private Integer orderId;
    private Integer userCouponId;
    private Integer operatorId;
}
