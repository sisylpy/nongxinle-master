package com.nongxinle.dispatch.core.domain;

/** 站点生命周期状态。 */
public enum DispatchStopStatus {
    UNASSIGNED,
    ASSIGNED,
    LOADING,
    IN_DELIVERY,
    DELIVERED,
    EXCEPTION,
    CANCELLED
}
