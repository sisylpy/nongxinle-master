package com.nongxinle.route;

public final class DisRouteStopTimeWindowStatus {
    public static final String OK = "OK";
    public static final String EARLY_WAIT = "EARLY_WAIT";
    /** 连续 ETA 早于窗口：仅状态提示，不插入等待时间。 */
    public static final String EARLY_ARRIVAL = "EARLY_ARRIVAL";
    public static final String LATE = "LATE";
    public static final String NO_WINDOW = "NO_WINDOW";
    /** Phase 3a：补单模式已超过客户常规窗口（非正式批次迟到） */
    public static final String SUPPLEMENT_AFTER_WINDOW = "SUPPLEMENT_AFTER_WINDOW";

    private DisRouteStopTimeWindowStatus() {
    }
}
