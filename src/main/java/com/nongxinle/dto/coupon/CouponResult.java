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
public class CouponResult {

    private CouponEligibilityResult bestCoupon;
    private List<CouponEligibilityResult> availableCoupons = new ArrayList<>();
    private List<CouponEligibilityResult> unavailableCoupons = new ArrayList<>();
    private BigDecimal discountSubtotal = BigDecimal.ZERO;

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

    public static CouponResult empty() {
        return new CouponResult();
    }

    public static String scale(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
