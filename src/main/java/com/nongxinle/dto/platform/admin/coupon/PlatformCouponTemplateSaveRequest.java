package com.nongxinle.dto.platform.admin.coupon;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class PlatformCouponTemplateSaveRequest {

    private String templateName;
    private String couponType;
    private BigDecimal discountAmount;
    private BigDecimal thresholdAmount;
    private String scopeType;
    private String scopeRefIds;
    private String useChannel;
    private String bizPurpose;
    private String claimStrategy;
    private String validityType;
    private Integer validityDays;
    private String startDate;
    private String stopDate;
}
