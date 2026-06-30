package com.nongxinle.route.dispatch.strategy;

/** 站点在策略层的分类（PR-2b 起用于编排）。 */
public enum DispatchStopClass {
    FROZEN,
    MANUAL_LOCKED,
    TIMED_WITH_HISTORY,
    TIMED_NO_HISTORY,
    UNTIMED_WITH_HISTORY,
    DISTANCE_ONLY,
    INFEASIBLE_LATE
}
