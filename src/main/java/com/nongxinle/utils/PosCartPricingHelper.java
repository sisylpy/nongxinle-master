package com.nongxinle.utils;

import com.nongxinle.dto.coupon.CartPriceResult;
import com.nongxinle.community.cart.service.CartPricingService;
import com.nongxinle.community.cart.helper.CartPricingSupport;
import com.nongxinle.community.cart.service.NxCommunityOrdersSubService;
import com.nongxinle.utils.CouponRuleConstants;

import java.util.Map;
import java.util.function.Supplier;

/**
 * POS 订单定价；计算统一走 CartPricingService.recalculateFromSnapshot + CouponEngine。
 */
public final class PosCartPricingHelper {

    private PosCartPricingHelper() {
    }

    public static CartPriceResult priceOrder(CartPricingService cartPricingService,
                                             NxCommunityOrdersSubService ordersSubService,
                                             Integer communityId, Integer customerUserId, Integer orderId) {
        return priceOrderWithConsistencyCheck(cartPricingService, ordersSubService,
                communityId, customerUserId, orderId);
    }

    public static CartPriceResult priceOrderWithConsistencyCheck(CartPricingService cartPricingService,
                                                                 NxCommunityOrdersSubService ordersSubService,
                                                                 Integer communityId, Integer customerUserId,
                                                                 Integer orderId) {
        if (communityId == null || orderId == null || customerUserId == null || customerUserId <= 0) {
            return CartPricingSupport.emptyPricing();
        }
        Supplier<CartPriceResult> supplier = () -> cartPricingService.recalculateFromSnapshot(
                ordersSubService.buildCartSnapshotFromOrder(communityId, customerUserId, orderId),
                CouponRuleConstants.CHANNEL_POS);
        CartPriceResult first = supplier.get();
        CartPriceResult second = supplier.get();
        CouponConsistencyValidator.assertEqual(first, second);
        return first;
    }

    public static void mergeIntoMap(Map<String, Object> target, CartPriceResult pricing) {
        if (target == null || pricing == null) {
            return;
        }
        target.putAll(pricing.toPricingFields());
    }
}
