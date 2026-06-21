package com.nongxinle.utils;

public final class PlatformMarketUserConstants {

    private PlatformMarketUserConstants() {
    }

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DISABLED = "DISABLED";

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_OPERATOR = "OPERATOR";
    public static final String ROLE_FINANCE = "FINANCE";
    public static final String ROLE_CUSTOMER_SERVICE = "CUSTOMER_SERVICE";

    public static final String HEADER_MARKET_TOKEN = "X-Platform-Market-Token";

    /** 会话有效期（天） */
    public static final int SESSION_TTL_DAYS = 7;
}
