package com.nongxinle.dto.platform.admin.coupon;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PlatformStoreCouponListRequest {

    private Integer storeGbDepartmentId;
    private Integer gbDepartmentId;
    private String status;
    private Integer templateId;
    private Integer offset;
    private Integer limit;
}
