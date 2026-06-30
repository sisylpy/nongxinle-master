package com.nongxinle.community.promotion.validator;

import com.nongxinle.dto.PromotionOwnerValidateResult;
import com.nongxinle.dto.PromotionOwnerValidationContext;

public interface PromotionOwnerValidator {

    String ownerType();

    PromotionOwnerValidateResult validate(PromotionOwnerValidationContext context);
}
