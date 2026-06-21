package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
public class PlatformCouponUsageLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long pculId;
    private Integer storeCouponId;
    private Integer templateId;
    private Integer marketId;
    private Integer storeGbDepartmentId;
    private String verifyType;
    private String checkoutToken;
    private Integer paymentId;
    private Integer billId;
    private BigDecimal discountAmount;
    private String beforeStatus;
    private String afterStatus;
    private String operatorType;
    private Integer operatorId;
    private Date createdAt;
}
