package com.nongxinle.community.promotion.service;

import com.nongxinle.dto.PromotionOwnerValidateResult;
import com.nongxinle.dto.PromotionOwnerValidationContext;
import com.nongxinle.entity.NxCustomerPromotionCodeEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.utils.CustomerReferralConstants;
import org.springframework.stereotype.Component;

@Component
public class PromotionScopeMatcher {

    public PromotionOwnerValidateResult matchCodeAndInvitee(NxCustomerPromotionCodeEntity code,
                                                            NxCustomerUserEntity newUser) {
        if (newUser.getNxCuCommerceId() != null && code.getCommerceId() != null
                && !newUser.getNxCuCommerceId().equals(code.getCommerceId())) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_CROSS_COMMERCE);
        }
        if (newUser.getNxCuCommunityId() != null && code.getCommunityId() != null
                && !newUser.getNxCuCommunityId().equals(code.getCommunityId())) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_CROSS_COMMUNITY);
        }
        return PromotionOwnerValidateResult.ok();
    }
}
