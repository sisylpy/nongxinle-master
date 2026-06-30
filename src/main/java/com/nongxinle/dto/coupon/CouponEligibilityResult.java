package com.nongxinle.dto.coupon;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CouponEligibilityResult {

    private Integer userCouponId;
    private String couponName;
    private String couponType;
    private String couponTypeLabel;
    private String thresholdLabel;
    private String scopeLabel;
    private boolean available;
    private String unavailableReason;
    private BigDecimal eligibleSubtotal;
    private BigDecimal thresholdAmount;
    private BigDecimal distanceToThreshold;
    private BigDecimal discountAmount;
    private String expireTime;
    private String validStart;
    private String validEnd;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userCouponId", userCouponId);
        map.put("couponName", couponName);
        map.put("couponType", couponType);
        map.put("couponTypeLabel", couponTypeLabel);
        map.put("thresholdLabel", thresholdLabel);
        map.put("scopeLabel", scopeLabel);
        map.put("available", available);
        map.put("unavailableReason", unavailableReason);
        map.put("eligibleSubtotal", scale(eligibleSubtotal));
        map.put("thresholdAmount", scale(thresholdAmount));
        map.put("distanceToThreshold", scale(distanceToThreshold));
        map.put("discountAmount", scale(discountAmount));
        map.put("expireTime", expireTime);
        map.put("validStart", validStart);
        map.put("validEnd", validEnd);
        return map;
    }

    private static String scale(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
