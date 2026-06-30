package com.nongxinle.community.promotion.validator;

import com.nongxinle.dao.NxCustomerPromoterDao;
import com.nongxinle.dto.PromotionOwnerValidateResult;
import com.nongxinle.dto.PromotionOwnerValidationContext;
import com.nongxinle.entity.NxCustomerPromoterEntity;
import com.nongxinle.entity.NxCustomerPromotionCodeEntity;
import com.nongxinle.community.promotion.service.PromotionScopeMatcher;
import com.nongxinle.utils.CustomerReferralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ExternalPromoterPromotionOwnerValidator implements PromotionOwnerValidator {

    @Autowired
    private NxCustomerPromoterDao nxCustomerPromoterDao;
    @Autowired
    private PromotionScopeMatcher promotionScopeMatcher;

    @Override
    public String ownerType() {
        return CustomerReferralConstants.OWNER_TYPE_EXTERNAL_PROMOTER;
    }

    @Override
    public PromotionOwnerValidateResult validate(PromotionOwnerValidationContext context) {
        NxCustomerPromotionCodeEntity code = context.getPromotionCode();
        Date now = context.getNow();
        Integer ownerId = code.getOwnerId();

        NxCustomerPromoterEntity promoter = nxCustomerPromoterDao.queryObject(ownerId);
        if (promoter == null) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_OWNER_DISABLED);
        }
        if (CustomerReferralConstants.PROMOTER_STATUS_TERMINATED.equals(promoter.getPromoterStatus())) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_PROMOTER_TERMINATED);
        }
        if (CustomerReferralConstants.PROMOTER_STATUS_SUSPENDED.equals(promoter.getPromoterStatus())) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_PROMOTER_SUSPENDED);
        }
        if (promoter.getCooperationStartAt() != null && now.before(promoter.getCooperationStartAt())) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_OWNER_DISABLED);
        }
        if (promoter.getCooperationEndAt() != null && now.after(promoter.getCooperationEndAt())) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_OWNER_DISABLED);
        }

        PromotionOwnerValidateResult scope = promotionScopeMatcher.matchCodeAndInvitee(code, context.getNewUser());
        if (!scope.isValid()) {
            return scope;
        }
        return PromotionOwnerValidateResult.okWithExternalPromoter(ownerId);
    }
}
