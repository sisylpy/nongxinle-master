package com.nongxinle.dto.pos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosMemberCouponsRequest {
    private Integer communityId;
    private Integer customerUserId;
    private Integer orderId;
}
