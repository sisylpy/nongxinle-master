package com.nongxinle.utils;

import com.nongxinle.entity.NxCommunityCouponEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.nongxinle.utils.CouponRuleConstants.*;

public final class CouponLabelUtils {

    private CouponLabelUtils() {
    }

    public static String couponTypeLabel(String couponType) {
        if (TYPE_FULL_REDUCTION.equals(couponType)) {
            return "满减券";
        }
        return "代金券";
    }

    public static String thresholdLabel(NxCommunityCouponEntity template) {
        if (template == null) {
            return "";
        }
        BigDecimal threshold = zeroIfNull(template.getThresholdAmount());
        BigDecimal discount = zeroIfNull(template.getDiscountAmount());
        if (TYPE_FULL_REDUCTION.equals(template.getCouponType())) {
            return "满" + scale(threshold) + "减" + scale(discount);
        }
        return "立减" + scale(discount);
    }

    public static String scopeLabel(String scopeType) {
        if (SCOPE_CATEGORY.equals(scopeType)) {
            return "指定分类";
        }
        if (SCOPE_GOODS.equals(scopeType)) {
            return "指定商品";
        }
        return "全店通用";
    }

    private static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
