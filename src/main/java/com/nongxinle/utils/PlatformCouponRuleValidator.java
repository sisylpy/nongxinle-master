package com.nongxinle.utils;

import com.nongxinle.entity.PlatformCouponTemplateEntity;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

/**
 * 京采平台券模板规则校验（不依赖 Community 券表）。
 */
public final class PlatformCouponRuleValidator {

    private PlatformCouponRuleValidator() {
    }

    public static void normalizeDefaults(PlatformCouponTemplateEntity template) {
        if (template == null) {
            throw new IllegalArgumentException("券模板不能为空");
        }
        if (StringUtils.isBlank(template.getCouponType())) {
            template.setCouponType(PlatformCouponConstants.TYPE_CASH);
        }
        if (StringUtils.isBlank(template.getScopeType())) {
            template.setScopeType(PlatformCouponConstants.SCOPE_ALL);
        }
        if (StringUtils.isBlank(template.getUseChannel())) {
            template.setUseChannel(PlatformCouponConstants.CHANNEL_ALL);
        }
        if (StringUtils.isBlank(template.getBizPurpose())) {
            template.setBizPurpose(PlatformCouponConstants.BIZ_MARKETING);
        }
        if (StringUtils.isBlank(template.getClaimStrategy())) {
            template.setClaimStrategy(PlatformCouponConstants.CLAIM_PUBLIC_ACTIVE);
        }
        if (PlatformCouponConstants.TYPE_CASH.equals(template.getCouponType())) {
            template.setThresholdAmount(BigDecimal.ZERO);
        }
        if (PlatformCouponConstants.SCOPE_ALL.equals(template.getScopeType())) {
            template.setScopeRefIds(null);
        }
    }

    public static void validateTemplate(PlatformCouponTemplateEntity template) {
        if (template == null) {
            throw new IllegalArgumentException("券模板不能为空");
        }
        if (StringUtils.isBlank(template.getTemplateName())) {
            throw new IllegalArgumentException("templateName 不能为空");
        }
        String couponType = template.getCouponType();
        if (!PlatformCouponConstants.TYPE_CASH.equals(couponType)
                && !PlatformCouponConstants.TYPE_FULL_REDUCTION.equals(couponType)) {
            throw new IllegalArgumentException("couponType 必须为 CASH 或 FULL_REDUCTION");
        }
        BigDecimal discount = template.getDiscountAmount();
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("discountAmount 必须大于 0");
        }
        BigDecimal threshold = template.getThresholdAmount() == null
                ? BigDecimal.ZERO : template.getThresholdAmount();
        if (PlatformCouponConstants.TYPE_CASH.equals(couponType)) {
            if (threshold.compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException("代金券门槛必须为 0");
            }
        } else {
            if (threshold.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("满减券门槛必须大于 0");
            }
            if (discount.compareTo(threshold) > 0) {
                throw new IllegalArgumentException("减免金额不能大于满减门槛");
            }
        }
        validateScope(template.getScopeType(), template.getScopeRefIds());
        validateChannel(template.getUseChannel());
        validateValidity(template.getValidityType(), template.getValidityDays(),
                template.getStartDate(), template.getStopDate());
    }

    private static void validateScope(String scopeType, String scopeRefIds) {
        if (!PlatformCouponConstants.SCOPE_ALL.equals(scopeType)
                && !PlatformCouponConstants.SCOPE_CATEGORY.equals(scopeType)
                && !PlatformCouponConstants.SCOPE_GOODS.equals(scopeType)) {
            throw new IllegalArgumentException("scopeType 无效");
        }
        if (!PlatformCouponConstants.SCOPE_ALL.equals(scopeType)) {
            if (StringUtils.isBlank(scopeRefIds)) {
                throw new IllegalArgumentException("请选择适用范围");
            }
            try {
                JSONArray arr = JSONArray.fromObject(scopeRefIds);
                if (arr == null || arr.isEmpty()) {
                    throw new IllegalArgumentException("scopeRefIds 不能为空");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("scopeRefIds 必须为 JSON 数组");
            }
        }
    }

    private static void validateChannel(String channel) {
        if (!PlatformCouponConstants.CHANNEL_ALL.equals(channel)
                && !PlatformCouponConstants.CHANNEL_PLATFORM_MINIAPP.equals(channel)
                && !PlatformCouponConstants.CHANNEL_POS.equals(channel)) {
            throw new IllegalArgumentException("useChannel 无效");
        }
    }

    private static void validateValidity(String validityType, Integer validityDays,
                                         String startDate, String stopDate) {
        if (StringUtils.isBlank(validityType)) {
            throw new IllegalArgumentException("validityType 不能为空");
        }
        if (PlatformCouponConstants.VALIDITY_FIXED_DATE.equals(validityType)) {
            if (StringUtils.isBlank(startDate) || StringUtils.isBlank(stopDate)) {
                throw new IllegalArgumentException("固定有效期需填写 startDate 与 stopDate");
            }
        } else if (PlatformCouponConstants.VALIDITY_DAYS_AFTER_CLAIM.equals(validityType)) {
            if (validityDays == null || validityDays < 1) {
                throw new IllegalArgumentException("领取后有效天数必须大于 0");
            }
        } else {
            throw new IllegalArgumentException("validityType 无效");
        }
    }
}
