package com.nongxinle.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MiniappApplyCouponRequest {
    private Integer orderId;
    private Integer userCouponId;
    private Integer customerUserId;
    private Integer communityId;
}
