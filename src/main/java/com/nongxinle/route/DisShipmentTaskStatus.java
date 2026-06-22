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
    /** Phase 3E：配送异常（司机端记录，不回到沙盘/装车） */
    public static final String EXCEPTION = "EXCEPTION";

    private DisShipmentTaskStatus() {
    }
}
