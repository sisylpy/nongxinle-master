package com.nongxinle.dto;

import com.nongxinle.entity.NxCustomerPromotionCodeEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class PromotionOwnerValidationContext {

    private NxCustomerPromotionCodeEntity promotionCode;
    private NxCustomerUserEntity newUser;
    private Date now;
}
