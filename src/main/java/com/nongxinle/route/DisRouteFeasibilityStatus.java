package com.nongxinle.route;

public final class DisRouteFeasibilityStatus {
    public static final String FEASIBLE = "FEASIBLE";
    public static final String HAS_WAIT = "HAS_WAIT";
    public static final String HAS_LATE = "HAS_LATE";
    public static final String INFEASIBLE = "INFEASIBLE";
    public static final String DRIVER_TOO_LATE = "DRIVER_TOO_LATE";
    public static final String NO_AVAILABLE_DRIVER = "NO_AVAILABLE_DRIVER";
    /** 仅 driverRoute：空车路线 */
    public static final String IDLE = "IDLE";

    private DisRouteFeasibilityStatus() {
    }
}
