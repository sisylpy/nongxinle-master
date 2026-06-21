package com.nongxinle.dto.platform.admin.coupon;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PlatformCouponTemplateListRequest {

    private String status;
    private String templateName;
    private Integer offset;
    private Integer limit;
}
