package com.nongxinle.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromotionOwnerValidateResult {

    private boolean valid;
    private String invalidReason;
    private Integer externalPromoterId;

    public static PromotionOwnerValidateResult ok() {
        PromotionOwnerValidateResult r = new PromotionOwnerValidateResult();
        r.valid = true;
        return r;
    }

    public static PromotionOwnerValidateResult okWithExternalPromoter(Integer promoterId) {
        PromotionOwnerValidateResult r = ok();
        r.externalPromoterId = promoterId;
        return r;
    }

    public static PromotionOwnerValidateResult fail(String reason) {
        PromotionOwnerValidateResult r = new PromotionOwnerValidateResult();
        r.valid = false;
        r.invalidReason = reason;
        return r;
    }
}
