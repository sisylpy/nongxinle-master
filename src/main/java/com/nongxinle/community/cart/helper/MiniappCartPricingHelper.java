package com.nongxinle.community.cart.helper;

import com.nongxinle.dto.coupon.CartPriceResult;
import com.nongxinle.community.cart.service.CartPricingService;
import com.nongxinle.utils.CouponConsistencyValidator;
import com.nongxinle.utils.R;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 小程序购物车定价字段合并；计算统一走 CartPricingService.recalculateMiniappCart。
 */
public final class MiniappCartPricingHelper {

    private MiniappCartPricingHelper() {
    }

    public static CartPriceResult priceCart(CartPricingService cartPricingService,
                                            Integer communityId, Integer userId,
                                            Integer orderType, Integer serviceType, Integer spId) {
        return priceCartWithConsistencyCheck(cartPricingService,
                () -> cartPricingService.recalculateMiniappCart(
                        communityId, userId, orderType, serviceType, spId));
    }

    public static CartPriceResult priceCartWithConsistencyCheck(CartPricingService cartPricingService,
                                                                Supplier<CartPriceResult> pricingSupplier) {
        CartPriceResult first = pricingSupplier.get();
        CartPriceResult second = pricingSupplier.get();
        CouponConsistencyValidator.assertEqual(first, second);
        return first;
    }

    public static void mergeIntoMap(Map<String, Object> target, CartPricingService cartPricingService,
                                    Integer communityId, Integer userId,
                                    Integer orderType, Integer serviceType, Integer spId) {
        CartPricingSupport.mergeIntoMap(target,
                priceCart(cartPricingService, communityId, userId, orderType, serviceType, spId));
    }

    public static R mergeIntoResponse(R response, CartPricingService cartPricingService,
                                      Integer communityId, Integer userId,
                                      Integer orderType, Integer serviceType, Integer spId) {
        return CartPricingSupport.mergeIntoResponse(response,
                priceCart(cartPricingService, communityId, userId, orderType, serviceType, spId));
    }
}
