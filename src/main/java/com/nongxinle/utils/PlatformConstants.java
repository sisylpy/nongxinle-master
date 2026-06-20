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

    /** 饭店从批发商店铺主动选商下单（GB 链路） */
    public static final String ASSIGN_SOURCE_CUSTOMER_SELECTED_SUPPLIER = "CUSTOMER_SELECTED_SUPPLIER";
    /** 平台 Phase 2a 手动分配等 NX 侧来源 */
    public static final String ASSIGN_SOURCE_PLATFORM_MANUAL = "PLATFORM_MANUAL";

    public static final String SOURCE_TYPE_GB = "GB";
    public static final String SOURCE_TYPE_NX = "NX";

    public static final String MARKET_DEP_STATUS_ACTIVE = "ACTIVE";

    public static final String SWITCH_SCOPE_ORDER_ONLY = "ORDER_ONLY";
    public static final String SWITCH_SCOPE_ORDER_AND_DEFAULT = "ORDER_AND_DEFAULT";
    public static final String SWITCH_SCOPE_ORDER_AND_SNAPSHOT = "ORDER_AND_SNAPSHOT";

    public static final String FULFILLMENT_STATUS_ASSIGNED = "ASSIGNED";
    public static final String FULFILLMENT_STATUS_READY_FOR_PICKUP = "READY_FOR_PICKUP";
    public static final String FULFILLMENT_STATUS_PICKED_UP = "PICKED_UP";
    public static final String FULFILLMENT_STATUS_DELIVERING = "DELIVERING";
    public static final String FULFILLMENT_STATUS_DELIVERED = "DELIVERED";
    public static final String FULFILLMENT_STATUS_SUPPLIER_EXCEPTION = "SUPPLIER_EXCEPTION";
}
