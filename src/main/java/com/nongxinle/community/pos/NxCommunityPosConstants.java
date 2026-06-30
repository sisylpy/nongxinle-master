package com.nongxinle.community.pos;

/**
 * 社区 POS 常量（与小程序订单语义隔离）
 */
public final class NxCommunityPosConstants {

    private NxCommunityPosConstants() {
    }

    public static final String ORDER_CHANNEL_MINIAPP = "miniapp";
    public static final String ORDER_CHANNEL_POS = "pos";

    /** 堂食服务类型（区别于商品层 0自提/1外卖） */
    public static final int POS_SERVICE_TYPE = 2;
    public static final int POS_ORDER_USER_ID = 0;
    public static final int POS_ORDER_TYPE = 0;

    public static final int ORDER_STATUS_UNPAID = 0;
    public static final int ORDER_STATUS_PAID = 2;
    public static final int ORDER_STATUS_CANCELLED = 99;

    public static final int PAYMENT_STATUS_UNPAID = 0;
    public static final int PAYMENT_STATUS_PAID = 1;

    public static final int SUB_STATUS_UNPAID = 0;
    public static final int SUB_STATUS_PAID = 2;
    public static final int SUB_STATUS_DELETED = 99;

    public static final int COUPON_STATUS_AVAILABLE = 0;
    public static final int COUPON_STATUS_LOCKED = 1;
    public static final int COUPON_STATUS_VERIFIED = 3;

    public static final String PAY_CHANNEL_WECHAT = "WECHAT";
    public static final String PAY_CHANNEL_ALIPAY = "ALIPAY";

    public static final String PAY_FLOW_PENDING = "PENDING";
    public static final String PAY_FLOW_SUCCESS = "SUCCESS";
    public static final String PAY_FLOW_FAILED = "FAILED";
    public static final String PAY_FLOW_CLOSED = "CLOSED";

    public static final String VERIFY_STANDALONE = "standalone";
    public static final String VERIFY_ORDER_LOCK = "order_lock";
    public static final String VERIFY_ORDER_PAID = "order_paid_verify";
    public static final String VERIFY_UNLOCK = "unlock";

    public static final int DESK_STATUS_FREE = 0;
    public static final int DESK_STATUS_BUSY = 1;

    public static final int PAYMENT_EXPIRE_MINUTES = 15;

    /** application.properties 中 pos.wechat.notify-url 的默认值（与配置文件保持一致） */
    public static final String DEFAULT_POS_WECHAT_NOTIFY_URL =
            "https://grainservice.club:8443/nongxinle/api/nxcommunitypos/payment/notify/wechat";
}
