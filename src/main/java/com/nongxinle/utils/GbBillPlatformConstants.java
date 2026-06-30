package com.nongxinle.utils;

/**
 * 平台现金 GB Bill 常量。
 * <p>补款（{@link #PAY_STATUS_AWAIT_SUPPLEMENT} / {@link #PAY_PHASE_SUPPLEMENT}）指同一张 bill 内
 * checkout 时已存在行的称重差额；不是向旧 bill 追加新商品。</p>
 */
public final class GbBillPlatformConstants {

    public static final String BILL_SOURCE_LEGACY = "LEGACY";
    public static final String BILL_SOURCE_PLATFORM_CASH = "PLATFORM_CASH";

    public static final String PAY_STATUS_NONE = "NONE";
    public static final String PAY_STATUS_AWAIT_FIRST_PAY = "AWAIT_FIRST_PAY";
    public static final String PAY_STATUS_PARTIAL_PAID = "PARTIAL_PAID";
    public static final String PAY_STATUS_AWAIT_SUPPLEMENT = "AWAIT_SUPPLEMENT";
    public static final String PAY_STATUS_PAID = "PAID";
    public static final String PAY_STATUS_CANCELLED = "CANCELLED";

    public static final String PRICE_CONFIRM_PENDING = "PENDING";
    public static final String PRICE_CONFIRM_CONFIRMED = "CONFIRMED";
    /** bill 内部分行已确认、部分待确认 */
    public static final String PRICE_CONFIRM_PARTIAL_PENDING = "PARTIAL_PENDING";

    public static final String PAY_PHASE_FIRST = "FIRST";
    public static final String PAY_PHASE_SUPPLEMENT = "SUPPLEMENT";

    public static final String PAYMENT_STATUS_PENDING = "PENDING";
    public static final String PAYMENT_STATUS_SUCCESS = "SUCCESS";
    public static final String PAYMENT_STATUS_FAILED = "FAILED";
    /** 用户主动取消（cancelPayment） */
    public static final String PAYMENT_STATUS_CANCELLED = "CANCELLED";
    /** PENDING 超时自动关闭 */
    public static final String PAYMENT_STATUS_EXPIRED = "EXPIRED";
    /** 兼容旧称；新代码优先用 CANCELLED / EXPIRED */
    public static final String PAYMENT_STATUS_CLOSED = "CLOSED";

    private GbBillPlatformConstants() {
    }

    public static boolean isCheckoutPaymentUnlockStatus(String status) {
        return PAYMENT_STATUS_FAILED.equals(status)
                || PAYMENT_STATUS_CANCELLED.equals(status)
                || PAYMENT_STATUS_EXPIRED.equals(status)
                || PAYMENT_STATUS_CLOSED.equals(status);
    }

    public static boolean blocksNewOrder(String payStatus) {
        return PAY_STATUS_AWAIT_FIRST_PAY.equals(payStatus)
                || PAY_STATUS_AWAIT_SUPPLEMENT.equals(payStatus);
    }
}
