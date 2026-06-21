package com.nongxinle.route;

public final class DisRoutePlanStatus {
    public static final String SIMULATED = "SIMULATED";
    public static final String ASSIGNED = "ASSIGNED";
    public static final String READY = "READY";
    public static final String CANCELLED = "CANCELLED";

    /** @deprecated Phase 1.5a 起请用 {@link #SIMULATED} */
    public static final String DRAFT = SIMULATED;
    /** @deprecated Phase 1.5a 起请用 {@link #ASSIGNED} */
    public static final String CONFIRMED = ASSIGNED;

    private DisRoutePlanStatus() {
    }
}
