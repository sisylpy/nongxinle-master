package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxCustomerReferralRewardRuleEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCustomerReferralRewardRuleId;
    private Integer communityId;
    private String ruleCode;
    private String triggerType;
    private String rewardTarget;
    private String beneficiaryType;
    private String rewardKind;
    private Integer couponTemplateId;
    private Integer pointsAmount;
    private Integer enabled;
    private Date effectiveStartAt;
    private Date effectiveEndAt;
    private Integer ruleStatus;
    private Integer priority;
    private Integer ruleVersion;
    private Date createdAt;
    private Date updatedAt;
}
