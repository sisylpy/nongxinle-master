package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
public class PlatformStoreCouponEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer pscId;
    private Integer templateId;
    private Integer marketId;
    private Integer storeGbDepartmentId;
    private String status;
    private String sourceType;
    private String sourceBizId;
    private String startDate;
    private String stopDate;
    private String lockedCheckoutToken;
    private Integer lockedPaymentId;
    private Integer issuedByMarketUserId;
    private Integer claimedByUserId;
    private Integer usedByUserId;
    private Integer usedBillId;
    private Date usedAt;
    private Date createdAt;
    private Date updatedAt;
}
