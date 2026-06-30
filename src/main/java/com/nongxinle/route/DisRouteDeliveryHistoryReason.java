package com.nongxinle.route;

/** 历史配送偏好解析原因码（P1 只读服务）。 */
public final class DisRouteDeliveryHistoryReason {

    public static final String HISTORY_DOMINANT_DRIVER = "HISTORY_DOMINANT_DRIVER";
    public static final String HISTORY_TIE_BROKEN_BY_RECENCY = "HISTORY_TIE_BROKEN_BY_RECENCY";
    public static final String INSUFFICIENT_HISTORY = "INSUFFICIENT_HISTORY";
    public static final String NO_HISTORY = "NO_HISTORY";
    public static final String PREFERRED_DRIVER_NOT_ELIGIBLE = "PREFERRED_DRIVER_NOT_ELIGIBLE";
    public static final String NO_ELIGIBLE_DRIVER = "NO_ELIGIBLE_DRIVER";
    public static final String MULTIPLE_EQUAL_CANDIDATES = "MULTIPLE_EQUAL_CANDIDATES";

    private DisRouteDeliveryHistoryReason() {
    }
}
