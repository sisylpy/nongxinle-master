package com.nongxinle.dto.coupon;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartPricingRequest {

    private Integer communityId;
    private Integer userId;
    private List<CartLineInput> items;

    @Getter
    @Setter
    public static class CartLineInput {
        private Integer goodsId;
        private Integer quantity;
    }
}
