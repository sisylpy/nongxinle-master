package com.nongxinle.community.cart.service;

import com.nongxinle.dto.coupon.CartPriceResult;
import com.nongxinle.dto.coupon.CartPricingRequest;
import com.nongxinle.dto.coupon.CartSnapshot;

public interface CartPricingService {

    CartPriceResult recalculate(CartPricingRequest request);

    CartPriceResult recalculateFromSnapshot(CartSnapshot snapshot, String channel);

    CartPriceResult recalculateMiniappCart(Integer communityId, Integer userId,
                                           Integer orderType, Integer serviceType, Integer spId);

    /** @deprecated 使用 {@link #recalculateMiniappCart} */
    CartPriceResult recalculateFromDraftSubOrders(Integer communityId, Integer customerUserId, String channel);

    /** @deprecated 使用 {@link #recalculateMiniappCart} */
    CartPriceResult recalculateFromDraftSubOrders(Integer communityId, Integer customerUserId,
                                                  Integer orderType, Integer serviceType, Integer spId,
                                                  String channel);
}
