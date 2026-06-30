package com.nongxinle.route.dispatch.strategy;

/** PR-2c debug：站序桶（与 {@link OwnerFixedRouteTimeWindowRouteSequencer} 一致）。 */
public enum DispatchSequenceBucket {
    L0_MANUAL_LOCKED,
    L1_TIMED_ON_TIME,
    L2_TIMED_LATE,
    L3_NO_WINDOW
}
