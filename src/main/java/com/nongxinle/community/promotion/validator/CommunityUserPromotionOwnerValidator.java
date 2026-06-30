package com.nongxinle.community.promotion.validator;

import com.nongxinle.dao.NxCommunityUserPromotionEligibleDao;
import com.nongxinle.dto.PromotionOwnerValidateResult;
import com.nongxinle.dto.PromotionOwnerValidationContext;
import com.nongxinle.entity.NxCommunityUserEntity;
import com.nongxinle.entity.NxCommunityUserPromotionEligibleEntity;
import com.nongxinle.entity.NxCustomerPromotionCodeEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.community.customer.service.NxCommunityUserService;
import com.nongxinle.community.promotion.service.PromotionScopeMatcher;
import com.nongxinle.community.promotion.service.PromotionScopeService;
import com.nongxinle.utils.CustomerReferralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class CommunityUserPromotionOwnerValidator implements PromotionOwnerValidator {

    @Autowired
    private NxCommunityUserService nxCommunityUserService;
    @Autowired
    private NxCommunityUserPromotionEligibleDao nxCommunityUserPromotionEligibleDao;
    @Autowired
    private PromotionScopeService promotionScopeService;
    @Autowired
    private PromotionScopeMatcher promotionScopeMatcher;

    @Override
    public String ownerType() {
        return CustomerReferralConstants.OWNER_TYPE_COMMUNITY_USER;
    }

    @Override
    public PromotionOwnerValidateResult validate(PromotionOwnerValidationContext context) {
        NxCustomerPromotionCodeEntity code = context.getPromotionCode();
        NxCustomerUserEntity newUser = context.getNewUser();
        Date now = context.getNow();
        Integer ownerId = code.getOwnerId();

        NxCommunityUserEntity staff = nxCommunityUserService.queryObject(ownerId);
        if (staff == null) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_OWNER_DISABLED);
        }
        if (staff.getNxCouWorkingStatus() == null
                || staff.getNxCouWorkingStatus() != CustomerReferralConstants.COMMUNITY_USER_WORKING_ACTIVE) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_COMMUNITY_USER_INACTIVE);
        }
        if (staff.getNxCouCommunityId() != null && code.getCommunityId() != null
                && !staff.getNxCouCommunityId().equals(code.getCommunityId())) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_CROSS_COMMUNITY);
        }
        if (staff.getNxCouCommunityId() != null && newUser.getNxCuCommunityId() != null
                && !staff.getNxCouCommunityId().equals(newUser.getNxCuCommunityId())) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_CROSS_COMMUNITY);
        }

        String commerceError = promotionScopeService.validateCodeCommerceForCommunity(code, staff.getNxCouCommunityId());
        if (commerceError != null) {
            return PromotionOwnerValidateResult.fail(commerceError);
        }

        NxCommunityUserPromotionEligibleEntity eligible = nxCommunityUserPromotionEligibleDao.queryObject(ownerId);
        if (eligible == null || eligible.getEnabled() == null || eligible.getEnabled() != 1) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_COMMUNITY_USER_NOT_ELIGIBLE);
        }
        if (eligible.getValidStartAt() != null && now.before(eligible.getValidStartAt())) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_COMMUNITY_USER_NOT_ELIGIBLE);
        }
        if (eligible.getValidEndAt() != null && now.after(eligible.getValidEndAt())) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_COMMUNITY_USER_NOT_ELIGIBLE);
        }

        return promotionScopeMatcher.matchCodeAndInvitee(code, newUser);
    }
}
