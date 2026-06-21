package com.nongxinle.dto.platform.admin.coupon;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PlatformStoreCouponIssueRequest {

    private Integer templateId;
    private Integer gbDepartmentId;
    private Integer storeGbDepartmentId;
    private Integer count;
}
