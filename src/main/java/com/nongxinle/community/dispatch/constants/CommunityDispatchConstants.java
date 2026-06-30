package com.nongxinle.community.dispatch.constants;

/**
 * nxCommunity 商城派单状态与角色常量（M1）。
 */
public final class CommunityDispatchConstants {

    private CommunityDispatchConstants() {
    }

    public static final int DRIVER_ROLE_ID = 5;

    public static final int ORDER_SERVICE_TYPE_DELIVERY = 1;

    public static final String PLAN_STATUS_ACTIVE = "ACTIVE";
    public static final String PLAN_STATUS_COMPLETED = "COMPLETED";
    public static final String PLAN_STATUS_CANCELLED = "CANCELLED";

    public static final String ROUTE_STATUS_DRAFT = "DRAFT";
    public static final String ROUTE_STATUS_LOADING = "LOADING";
    public static final String ROUTE_STATUS_IN_DELIVERY = "IN_DELIVERY";
    public static final String ROUTE_STATUS_COMPLETED = "COMPLETED";
    public static final String ROUTE_STATUS_IDLE = "IDLE";

    public static final String STOP_STATUS_ASSIGNED = "ASSIGNED";
    public static final String STOP_STATUS_LOADING = "LOADING";
    public static final String STOP_STATUS_IN_DELIVERY = "IN_DELIVERY";
    public static final String STOP_STATUS_DELIVERED = "DELIVERED";
    public static final String STOP_STATUS_EXCEPTION = "EXCEPTION";
    public static final String STOP_STATUS_CANCELLED = "CANCELLED";

    public static final String DISPATCH_STATUS_UNASSIGNED = "UNASSIGNED";
    public static final String DISPATCH_STATUS_ASSIGNED = "ASSIGNED";
    public static final String DISPATCH_STATUS_LOADING = "LOADING";
    public static final String DISPATCH_STATUS_IN_DELIVERY = "IN_DELIVERY";
    public static final String DISPATCH_STATUS_DELIVERED = "DELIVERED";
    public static final String DISPATCH_STATUS_EXCEPTION = "EXCEPTION";
    public static final String DISPATCH_STATUS_CANCELLED = "CANCELLED";

    /** 可再次 confirm 的 dispatch 状态（当前态表 upsert，不新增第二行）。 */
    public static boolean isRedispatchableDispatchStatus(String status) {
        return DISPATCH_STATUS_UNASSIGNED.equals(status)
                || DISPATCH_STATUS_CANCELLED.equals(status);
    }

    /** 占用订单、不可再次 confirm 的 dispatch 状态。 */
    public static boolean isActiveDispatchStatus(String status) {
        return DISPATCH_STATUS_ASSIGNED.equals(status)
                || DISPATCH_STATUS_LOADING.equals(status)
                || DISPATCH_STATUS_IN_DELIVERY.equals(status)
                || DISPATCH_STATUS_DELIVERED.equals(status)
                || DISPATCH_STATUS_EXCEPTION.equals(status);
    }

    public static final String DUTY_ON = "ON_DUTY";
    public static final String DUTY_OFF = "OFF_DUTY";

    public static final String PAGE_MODE_SANDBOX = "DISPATCH_SANDBOX";
    public static final String PAGE_MODE_LOADING = "LOADING";
    public static final String PAGE_MODE_DELIVERY = "DELIVERY";

    public static final String SANDBOX_KEY_PREFIX = "ADDR-";

    public static String sandboxKeyForAddress(Integer addressId) {
        return SANDBOX_KEY_PREFIX + addressId;
    }
}
