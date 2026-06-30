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
public class PlatformCouponTemplateEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer pctId;
    private Integer marketId;
    private String templateName;
    private String couponType;
    private BigDecimal discountAmount;
    private BigDecimal thresholdAmount;
    private String scopeType;
    /** CATEGORY: nx_goods 分类 id JSON 数组；GOODS: nx_goods_id JSON 数组 */
    private String scopeRefIds;
    private String useChannel;
    private String bizPurpose;
    private String claimStrategy;
    private String validityType;
    private Integer validityDays;
    private String startDate;
    private String stopDate;
    private String status;
    private Integer issueCount;
    private Integer useCount;
    private Integer createdByMarketUserId;
    private Integer updatedByMarketUserId;
    private Date createdAt;
    private Date updatedAt;
}
