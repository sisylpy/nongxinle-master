package com.nongxinle.utils;

/**
 * 规则券类型、范围、渠道常量
 */
public final class CouponRuleConstants {

    private CouponRuleConstants() {
    }

    public static final String TYPE_CASH = "CASH";
    public static final String TYPE_FULL_REDUCTION = "FULL_REDUCTION";

    public static final String SCOPE_ALL = "ALL";
    public static final String SCOPE_CATEGORY = "CATEGORY";
    public static final String SCOPE_GOODS = "GOODS";

    public static final String CHANNEL_ALL = "ALL";
    public static final String CHANNEL_POS = "POS";
    public static final String CHANNEL_MINIAPP = "MINIAPP";

    public static final int TEMPLATE_STATUS_ACTIVE = 0;
}
