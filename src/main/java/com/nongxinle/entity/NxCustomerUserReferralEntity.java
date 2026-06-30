package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxCustomerUserReferralEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCustomerUserReferralId;
    private Integer inviteeUserId;
    private Integer promotionCodeId;
    private String promotionCodeSnapshot;
    private String sourceOwnerType;
    private Integer sourceOwnerId;
    private Integer promoterId;
    private Integer commerceId;
    private Integer communityId;
    private String shareEntry;
    private String qualificationStatus;
    private String invalidReason;
    private Integer rewardQualified;
    private Integer rewardRuleId;
    private Integer campaignId;
    private Date bindTime;
    private Date createdAt;

    private NxCustomerUserEntity inviteeUser;
    private Integer subReferralCount;
    private NxCustomerUserEntity viaDirectFriend;
}
