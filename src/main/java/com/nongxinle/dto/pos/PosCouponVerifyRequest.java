package com.nongxinle.dto.pos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosCouponVerifyRequest {
    private Integer userCouponId;
    private Integer communityId;
    private Integer operatorId;
    private Integer deskId;
    private String remark;
}
