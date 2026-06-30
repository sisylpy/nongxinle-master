package com.nongxinle.utils;

import com.nongxinle.dto.coupon.CartPriceResult;
import com.nongxinle.dto.coupon.CouponEligibilityResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 购物车定价结果一致性校验；用于检测 snapshot 漂移或多入口计算分叉。
 */
public final class CouponConsistencyValidator {

    private static final String MISMATCH = "Coupon calculation mismatch (snapshot drift)";

    private CouponConsistencyValidator() {
    }

    public static void assertEqual(CartPriceResult a, CartPriceResult b) {
        if (a == null && b == null) {
            return;
        }
        if (a == null || b == null) {
            throw new IllegalStateException(MISMATCH);
        }
        assertAmountEqual("goodsSubtotal", a.getGoodsSubtotal(), b.getGoodsSubtotal());
        assertAmountEqual("discountSubtotal", a.getDiscountSubtotal(), b.getDiscountSubtotal());
        assertAmountEqual("payableTotal", a.getPayableTotal(), b.getPayableTotal());
        assertBestCouponEqual(a.getBestCoupon(), b.getBestCoupon());
    }

    private static void assertBestCouponEqual(CouponEligibilityResult left, CouponEligibilityResult right) {
        if (left == null && right == null) {
            return;
        }
        if (left == null || right == null) {
            throw new IllegalStateException(MISMATCH);
        }
        if (!safeEquals(left.getUserCouponId(), right.getUserCouponId())) {
            throw new IllegalStateException(MISMATCH);
        }
        assertAmountEqual("bestCoupon.discountAmount", left.getDiscountAmount(), right.getDiscountAmount());
    }

    private static void assertAmountEqual(String field, BigDecimal left, BigDecimal right) {
        if (!amountEqual(left, right)) {
            throw new IllegalStateException(MISMATCH + ": " + field);
        }
    }

    private static boolean amountEqual(BigDecimal left, BigDecimal right) {
        BigDecimal l = normalize(left);
        BigDecimal r = normalize(right);
        return l.compareTo(r) == 0;
    }

    private static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static boolean safeEquals(Integer a, Integer b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }
}
