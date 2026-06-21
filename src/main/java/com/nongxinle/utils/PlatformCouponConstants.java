package com.nongxinle.utils;

/**
 * 京采平台优惠券常量（独立于 Community 券表）。
 */
public final class PlatformCouponConstants {

    private PlatformCouponConstants() {
    }

    public static final String TYPE_CASH = CouponRuleConstants.TYPE_CASH;
    public static final String TYPE_FULL_REDUCTION = CouponRuleConstants.TYPE_FULL_REDUCTION;

    public static final String SCOPE_ALL = CouponRuleConstants.SCOPE_ALL;
    public static final String SCOPE_CATEGORY = CouponRuleConstants.SCOPE_CATEGORY;
    public static final String SCOPE_GOODS = CouponRuleConstants.SCOPE_GOODS;

    public static final String CHANNEL_ALL = CouponRuleConstants.CHANNEL_ALL;
    public static final String CHANNEL_PLATFORM_MINIAPP = "PLATFORM_MINIAPP";
    public static final String CHANNEL_POS = CouponRuleConstants.CHANNEL_POS;

    public static final String VALIDITY_FIXED_DATE = "FIXED_DATE";
    public static final String VALIDITY_DAYS_AFTER_CLAIM = "DAYS_AFTER_CLAIM";

    public static final String TEMPLATE_STATUS_ACTIVE = "ACTIVE";
    public static final String TEMPLATE_STATUS_DISABLED = "DISABLED";

    public static final String STORE_STATUS_AVAILABLE = "AVAILABLE";
    public static final String STORE_STATUS_LOCKED = "LOCKED";
    public static final String STORE_STATUS_USED = "USED";
    public static final String STORE_STATUS_EXPIRED = "EXPIRED";
    public static final String STORE_STATUS_VOID = "VOID";

    public static final String SOURCE_MANUAL = "manual";
    public static final String SOURCE_ACTIVE_CLAIM = "active_claim";
    public static final String SOURCE_REFERRAL_REWARD = "referral_reward";
    public static final String SOURCE_PROMOTION_REWARD = "promotion_reward";

    public static final String BIZ_MARKETING = "marketing";
    public static final String BIZ_MANUAL = "manual";

    public static final String CLAIM_PUBLIC_ACTIVE = "public_active";

    public static final String VERIFY_ISSUE = "ISSUE";
    public static final String VERIFY_VOID = "VOID";

    public static final String OPERATOR_MARKET_USER = "MARKET_USER";
    public static final String OPERATOR_STORE_USER = "STORE_USER";
    public static final String OPERATOR_SYSTEM = "SYSTEM";
}
