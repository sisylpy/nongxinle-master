package com.nongxinle.route;

public final class DisShipmentTaskStatus {
    public static final String SIMULATED = "SIMULATED";
    public static final String ASSIGNED = "ASSIGNED";
    public static final String READY_TO_GO = "READY_TO_GO";
    public static final String UNASSIGNED = "UNASSIGNED";
    public static final String CANCELLED = "CANCELLED";
    public static final String CLOSED = "CLOSED";
    /** Phase 1 预留 */
    public static final String IN_DELIVERY = "IN_DELIVERY";
    /** Phase 1 预留 */
    public static final String DELIVERED = "DELIVERED";

    private DisShipmentTaskStatus() {
    }
}
