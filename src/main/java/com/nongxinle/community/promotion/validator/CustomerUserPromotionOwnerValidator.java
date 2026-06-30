package com.nongxinle.community.promotion.validator;

import com.nongxinle.dto.PromotionOwnerValidateResult;
import com.nongxinle.dto.PromotionOwnerValidationContext;
import com.nongxinle.entity.NxCustomerPromotionCodeEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.community.customer.service.NxCustomerUserService;
import com.nongxinle.community.promotion.service.PromotionScopeMatcher;
import com.nongxinle.utils.CustomerReferralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerUserPromotionOwnerValidator implements PromotionOwnerValidator {

    @Autowired
    private NxCustomerUserService nxCustomerUserService;
    @Autowired
    private PromotionScopeMatcher promotionScopeMatcher;

    @Override
    public String ownerType() {
        return CustomerReferralConstants.OWNER_TYPE_CUSTOMER_USER;
    }

    @Override
    public PromotionOwnerValidateResult validate(PromotionOwnerValidationContext context) {
        NxCustomerPromotionCodeEntity code = context.getPromotionCode();
        NxCustomerUserEntity newUser = context.getNewUser();
        Integer ownerId = code.getOwnerId();

        NxCustomerUserEntity owner = nxCustomerUserService.queryObject(ownerId);
        if (owner == null) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_OWNER_DISABLED);
        }
        if (ownerId.equals(newUser.getNxCuUserId())) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_SELF_REFERRAL);
        }
        return promotionScopeMatcher.matchCodeAndInvitee(code, newUser);
    }
}
