package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxCustomerPromotionCampaignEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCustomerPromotionCampaignId;
    private Integer communityId;
    private Integer commerceId;
    private String campaignCode;
    private String campaignScene;
    private String campaignName;
    /** ALL / CUSTOMER_USER / COMMUNITY_USER / EXTERNAL_PROMOTER */
    private String ownerScope;
    /** ACTIVE / SUSPENDED / TERMINATED */
    private String campaignStatus;
    private Date effectiveStartAt;
    private Date effectiveEndAt;
    private Date terminatedAt;
    private String statusReason;
    private Integer campaignVersion;
    private Date createdAt;
    private Date updatedAt;

    private Integer qualifiedReferralCount;
}
