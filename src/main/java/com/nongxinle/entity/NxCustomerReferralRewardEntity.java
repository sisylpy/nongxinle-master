package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxCustomerReferralRewardEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCustomerReferralRewardId;
    private String beneficiaryType;
    private Integer beneficiaryUserId;
    private Integer triggerUserId;
    private Integer referralId;
    private Integer communityId;
    private Integer rewardLevel;
    private String rewardKind;
    private Integer ruleId;
    private Integer couponTemplateId;
    private String couponNameSnapshot;
    private String couponPriceSnapshot;
    private String couponOriginalPriceSnapshot;
    private String couponWordsSnapshot;
    private String couponStartDateSnapshot;
    private String couponStopDateSnapshot;
    private String couponValidityTypeSnapshot;
    private Integer couponValidityDaysSnapshot;
    private Integer pointsAmount;
    private Integer status;
    private Integer userCouponId;
    private Date claimedAt;
    private Date createdAt;

    /** 查询扩展 */
    private NxCustomerUserEntity triggerUser;
    private NxCommunityCouponEntity couponTemplate;
}
