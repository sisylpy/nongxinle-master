package com.nongxinle.dispatch.core.domain;

/** 司机路线生命周期状态。 */
public enum DispatchRouteStatus {
    DRAFT,
    LOADING,
    IN_DELIVERY,
    COMPLETED,
    IDLE,
    CANCELLED
}
