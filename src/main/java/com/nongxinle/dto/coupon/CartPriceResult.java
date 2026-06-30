package com.nongxinle.dto.coupon;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CartPriceResult {

    private BigDecimal goodsSubtotal;
    private BigDecimal discountSubtotal;
    private BigDecimal payableTotal;
    private CouponEligibilityResult bestCoupon;
    private List<CouponEligibilityResult> availableCoupons = new ArrayList<>();
    private List<CouponEligibilityResult> unavailableCoupons = new ArrayList<>();
    private List<CartLineSnapshot> cartLines = new ArrayList<>();

    public Map<String, Object> toCouponInfoMap() {
        Map<String, Object> info = new HashMap<>();
        info.put("bestCoupon", bestCoupon == null ? null : bestCoupon.toMap());
        List<Map<String, Object>> available = new ArrayList<>();
        for (CouponEligibilityResult r : availableCoupons) {
            available.add(r.toMap());
        }
        List<Map<String, Object>> unavailable = new ArrayList<>();
        for (CouponEligibilityResult r : unavailableCoupons) {
            unavailable.add(r.toMap());
        }
        info.put("availableCoupons", available);
        info.put("unavailableCoupons", unavailable);
        return info;
    }

    public Map<String, Object> toPricingFields() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("goodsSubtotal", scale(goodsSubtotal));
        fields.put("discountSubtotal", scale(discountSubtotal));
        fields.put("payableTotal", scale(payableTotal));
        fields.put("couponInfo", toCouponInfoMap());
        return fields;
    }

    private static String scale(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
