package com.nongxinle.utils;

import com.nongxinle.entity.NxCommunityCouponEntity;
import net.sf.json.JSONArray;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.nongxinle.utils.CouponRuleConstants.*;

public final class CouponRuleValidator {

    private CouponRuleValidator() {
    }

    public static void normalizeDefaults(NxCommunityCouponEntity coupon) {
        if (coupon == null) {
            throw new IllegalArgumentException("优惠券不能为空");
        }
        if (coupon.getCouponType() == null || coupon.getCouponType().trim().isEmpty()) {
            coupon.setCouponType(TYPE_CASH);
        }
        if (coupon.getScopeType() == null || coupon.getScopeType().trim().isEmpty()) {
            coupon.setScopeType(SCOPE_ALL);
        }
        if (coupon.getUseChannel() == null || coupon.getUseChannel().trim().isEmpty()) {
            coupon.setUseChannel(CHANNEL_ALL);
        }
        if (TYPE_CASH.equals(coupon.getCouponType())) {
            coupon.setThresholdAmount(BigDecimal.ZERO);
        }
        if (SCOPE_ALL.equals(coupon.getScopeType())) {
            coupon.setScopeRefIds(null);
        }
    }

    public static void validateRuleCoupon(NxCommunityCouponEntity coupon) {
        if (coupon == null) {
            throw new IllegalArgumentException("优惠券不能为空");
        }
        if (coupon.getNxCommunityCouponName() == null || coupon.getNxCommunityCouponName().trim().isEmpty()) {
            throw new IllegalArgumentException("券名称不能为空");
        }
        if (coupon.getNxCpCommunityId() == null) {
            throw new IllegalArgumentException("communityId 不能为空");
        }
        String couponType = coupon.getCouponType();
        if (!TYPE_CASH.equals(couponType) && !TYPE_FULL_REDUCTION.equals(couponType)) {
            throw new IllegalArgumentException("couponType 必须为 CASH 或 FULL_REDUCTION");
        }
        BigDecimal discount = coupon.getDiscountAmount();
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("discountAmount 必须大于 0");
        }
        BigDecimal threshold = coupon.getThresholdAmount() == null ? BigDecimal.ZERO : coupon.getThresholdAmount();
        if (TYPE_CASH.equals(couponType)) {
            if (threshold.compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException("代金券门槛必须为 0");
            }
        } else {
            if (threshold.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("满减券门槛必须大于 0");
            }
            if (discount.compareTo(threshold) >= 0) {
                throw new IllegalArgumentException("减免金额必须小于满减门槛");
            }
        }
        String scopeType = coupon.getScopeType();
        if (!SCOPE_ALL.equals(scopeType) && !SCOPE_CATEGORY.equals(scopeType) && !SCOPE_GOODS.equals(scopeType)) {
            throw new IllegalArgumentException("scopeType 无效");
        }
        if (!SCOPE_ALL.equals(scopeType)) {
            if (coupon.getScopeRefIds() == null || coupon.getScopeRefIds().trim().isEmpty()) {
                throw new IllegalArgumentException("请选择适用范围");
            }
            try {
                JSONArray arr = JSONArray.fromObject(coupon.getScopeRefIds());
                if (arr == null || arr.isEmpty()) {
                    throw new IllegalArgumentException("scopeRefIds 不能为空");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("scopeRefIds 必须为 JSON 数组");
            }
        }
        String channel = coupon.getUseChannel();
        if (!CHANNEL_ALL.equals(channel) && !CHANNEL_POS.equals(channel) && !CHANNEL_MINIAPP.equals(channel)) {
            throw new IllegalArgumentException("useChannel 无效");
        }
        if (coupon.getNxCpValidityType() == null || coupon.getNxCpValidityType().trim().isEmpty()) {
            throw new IllegalArgumentException("有效期类型不能为空");
        }
        if ("FIXED_DATE".equals(coupon.getNxCpValidityType())) {
            requireDateTime(coupon.getNxCpStartDate(), "开始日期");
            requireDateTime(coupon.getNxCpStartTime(), "开始时间");
            requireDateTime(coupon.getNxCpStopDate(), "结束日期");
            requireDateTime(coupon.getNxCpStopTime(), "结束时间");
        } else if ("DAYS_AFTER_CLAIM".equals(coupon.getNxCpValidityType())) {
            if (coupon.getNxCpValidityDays() == null || coupon.getNxCpValidityDays() < 1) {
                throw new IllegalArgumentException("领取后有效天数必须大于 0");
            }
        } else {
            throw new IllegalArgumentException("nxCpValidityType 无效");
        }
    }

    public static void applyFixedDateTimeZones(NxCommunityCouponEntity coupon) {
        if (!"FIXED_DATE".equals(coupon.getNxCpValidityType())) {
            return;
        }
        String start = coupon.getNxCpStartDate() + "-" + coupon.getNxCpStartTime().replace(":", "-");
        String stop = coupon.getNxCpStopDate() + "-" + coupon.getNxCpStopTime().replace(":", "-");
        coupon.setNxCpStartTimeZone(parseDateTime(start));
        coupon.setNxCpStopTimeZone(parseDateTime(stop));
    }

    private static void requireDateTime(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + "不能为空");
        }
    }

    private static LocalDateTime parseDateTime(String value) {
        String[] parts = value.split("-");
        if (parts.length < 6) {
            throw new IllegalArgumentException("日期时间格式错误: " + value);
        }
        return LocalDateTime.of(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]),
                Integer.parseInt(parts[4]),
                Integer.parseInt(parts[5])
        );
    }
}
