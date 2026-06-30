package com.nongxinle.entity;

/**
 * 社区优惠券模板（规则券）
 */
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class NxCommunityCouponEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCommunityCouponId;
    private String nxCommunityCouponName;

    /** CASH | FULL_REDUCTION */
    private String couponType;
    private BigDecimal discountAmount;
    private BigDecimal thresholdAmount;
    /** ALL | CATEGORY | GOODS */
    private String scopeType;
    /** JSON 数组：分类或商品 ID */
    private String scopeRefIds;
    /** ALL | POS | MINIAPP */
    private String useChannel;

    private String nxCpStopTime;
    private String nxCpStartTime;
    private String nxCpWords;
    private String nxCpStartDate;
    private String nxCpStopDate;
    private String nxCpFilePath;

    private LocalDateTime nxCpStartTimeZone;
    private LocalDateTime nxCpStopTimeZone;

    private Integer nxCpCommunityId;
    private Integer nxCpDownCount;
    private Integer nxCpUseCount;

    /** marketing / referral_register / new_user_gift / compensation */
    private String nxCpBizPurpose;
    /** public_active / reward_only / auto_grant */
    private String nxCpClaimStrategy;
    /** FIXED_DATE / DAYS_AFTER_CLAIM */
    private String nxCpValidityType;
    private Integer nxCpValidityDays;
    private Integer nxCpStatus;
}
