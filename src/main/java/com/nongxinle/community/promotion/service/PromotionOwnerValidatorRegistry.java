package com.nongxinle.community.promotion.service;

import com.nongxinle.dto.PromotionOwnerValidateResult;
import com.nongxinle.dto.PromotionOwnerValidationContext;
import com.nongxinle.community.promotion.validator.PromotionOwnerValidator;
import com.nongxinle.utils.CustomerReferralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PromotionOwnerValidatorRegistry {

    @Autowired
    private List<PromotionOwnerValidator> validators;

    private final Map<String, PromotionOwnerValidator> validatorMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (PromotionOwnerValidator validator : validators) {
            validatorMap.put(validator.ownerType(), validator);
        }
    }

    public PromotionOwnerValidateResult validate(String ownerType, PromotionOwnerValidationContext context) {
        PromotionOwnerValidator validator = validatorMap.get(ownerType);
        if (validator == null) {
            return PromotionOwnerValidateResult.fail(CustomerReferralConstants.INVALID_OWNER_DISABLED);
        }
        return validator.validate(context);
    }
}
