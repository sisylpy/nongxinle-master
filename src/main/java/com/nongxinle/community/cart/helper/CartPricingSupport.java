package com.nongxinle.community.cart.helper;

import com.nongxinle.dto.coupon.CartPriceResult;
import com.nongxinle.dto.coupon.CouponEligibilityResult;
import com.nongxinle.utils.R;

import java.math.BigDecimal;
import java.util.Map;

public final class CartPricingSupport {

    private CartPricingSupport() {
    }

    public static void mergeIntoMap(Map<String, Object> target, CartPriceResult pricing) {
        if (target == null || pricing == null) {
            return;
        }
        target.putAll(pricing.toPricingFields());
    }

    public static R mergeIntoResponse(R response, CartPriceResult pricing) {
        if (response != null && pricing != null) {
            response.putAll(pricing.toPricingFields());
        }
        return response;
    }

    public static CartPriceResult emptyPricing() {
        CartPriceResult result = new CartPriceResult();
        result.setGoodsSubtotal(BigDecimal.ZERO);
        result.setDiscountSubtotal(BigDecimal.ZERO);
        result.setPayableTotal(BigDecimal.ZERO);
        return result;
    }

    public static CouponEligibilityResult findCoupon(CartPriceResult pricing, Integer userCouponId) {
        if (pricing == null || userCouponId == null) {
            return null;
        }
        if (pricing.getAvailableCoupons() != null) {
            for (CouponEligibilityResult item : pricing.getAvailableCoupons()) {
                if (userCouponId.equals(item.getUserCouponId())) {
                    return item;
                }
            }
        }
        if (pricing.getUnavailableCoupons() != null) {
            for (CouponEligibilityResult item : pricing.getUnavailableCoupons()) {
                if (userCouponId.equals(item.getUserCouponId())) {
                    return item;
                }
            }
        }
        return null;
    }

    public static void requireAvailableCoupon(CartPriceResult pricing, Integer userCouponId) {
        CouponEligibilityResult found = findCoupon(pricing, userCouponId);
        if (found == null) {
            throw new IllegalStateException("优惠券不可用");
        }
        if (!found.isAvailable()) {
            throw new IllegalStateException(found.getUnavailableReason() != null
                    ? found.getUnavailableReason() : "优惠券不可用");
        }
    }
}
