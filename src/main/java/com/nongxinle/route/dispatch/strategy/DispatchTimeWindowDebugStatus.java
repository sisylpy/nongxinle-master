package com.nongxinle.route.dispatch.strategy;

/** PR-2c：策略层 / debug 时间窗状态（与 page 层 DisRouteStopTimeWindowStatus 解耦）。 */
public enum DispatchTimeWindowDebugStatus {
    ON_TIME,
    LATE,
    WINDOW_MISSED,
    NO_WINDOW,
    /** 连续 ETA 早于窗口开始：仅提示，不推迟到达时间（第二站及以后）。 */
    EARLY_ARRIVAL
}
