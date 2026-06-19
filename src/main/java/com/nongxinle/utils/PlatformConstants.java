package com.nongxinle.utils;

/**
 * 批发市场平台化常量（与配送商协作链、社区 POS 隔离）
 */
public final class PlatformConstants {

    private PlatformConstants() {
    }

    public static final String ASSIGN_STATUS_PENDING = "PENDING";
    public static final String ASSIGN_STATUS_ASSIGNED = "ASSIGNED";

    public static final String ASSIGN_MODE_PLATFORM = "PLATFORM";
    public static final String ASSIGN_MODE_LEGACY_DIS = "LEGACY_DIS";

    public static final String MARKET_DEP_STATUS_ACTIVE = "ACTIVE";

    public static final String SWITCH_SCOPE_ORDER_ONLY = "ORDER_ONLY";
    public static final String SWITCH_SCOPE_ORDER_AND_DEFAULT = "ORDER_AND_DEFAULT";
    public static final String SWITCH_SCOPE_ORDER_AND_SNAPSHOT = "ORDER_AND_SNAPSHOT";
}
