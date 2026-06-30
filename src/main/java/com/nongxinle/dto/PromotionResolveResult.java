package com.nongxinle.dto;

import com.nongxinle.entity.NxCustomerPromotionCodeEntity;
import com.nongxinle.entity.NxCustomerPromotionCampaignEntity;
import com.nongxinle.entity.NxCustomerReferralRewardRuleEntity;
import com.nongxinle.utils.CustomerReferralConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromotionResolveResult {

    private boolean recordReferral;
    private boolean qualified;
    private boolean rewardQualified;
    private boolean rewardRuleConflict;
    private String rewardConflictReason;
    private String invalidReason;

    private NxCustomerPromotionCodeEntity promotionCode;
    private String ownerType;
    private Integer ownerId;
    private Integer externalPromoterId;
    private NxCustomerPromotionCampaignEntity matchedCampaign;
    private NxCustomerReferralRewardRuleEntity matchedRule;

    public static PromotionResolveResult noCode() {
        PromotionResolveResult r = new PromotionResolveResult();
        r.recordReferral = false;
        return r;
    }

    public static PromotionResolveResult attempt(String reason, NxCustomerPromotionCodeEntity code) {
        return invalid(reason, code);
    }

    public static PromotionResolveResult invalid(String reason, NxCustomerPromotionCodeEntity code) {
        PromotionResolveResult r = new PromotionResolveResult();
        r.recordReferral = true;
        r.qualified = false;
        r.rewardQualified = false;
        r.invalidReason = reason;
        r.promotionCode = code;
        fillOwnerSnapshot(r, code);
        return r;
    }

    private static void fillOwnerSnapshot(PromotionResolveResult r, NxCustomerPromotionCodeEntity code) {
        if (code == null) {
            return;
        }
        r.ownerType = code.getOwnerType();
        r.ownerId = code.getOwnerId();
        if (CustomerReferralConstants.OWNER_TYPE_EXTERNAL_PROMOTER.equals(code.getOwnerType())) {
            r.externalPromoterId = code.getOwnerId();
        }
    }

    public static PromotionResolveResult success(NxCustomerPromotionCodeEntity code, String ownerType, Integer ownerId,
                                                  Integer externalPromoterId, boolean rewardQualified,
                                                  NxCustomerPromotionCampaignEntity campaign,
                                                  NxCustomerReferralRewardRuleEntity rule) {
        PromotionResolveResult r = new PromotionResolveResult();
        r.recordReferral = true;
        r.qualified = true;
        r.rewardQualified = rewardQualified;
        r.promotionCode = code;
        r.ownerType = ownerType;
        r.ownerId = ownerId;
        r.externalPromoterId = externalPromoterId;
        r.matchedCampaign = campaign;
        r.matchedRule = rule;
        return r;
    }
}
