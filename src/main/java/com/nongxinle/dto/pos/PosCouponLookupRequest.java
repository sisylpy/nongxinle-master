package com.nongxinle.dto.pos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosCouponLookupRequest {
    private String code;
    private Integer communityId;
    private Integer orderId;
}
