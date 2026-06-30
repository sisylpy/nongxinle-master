package com.nongxinle.route;

/** Phase 2b-1：司机批次可派性判定常量 */
public final class DisRouteBatchEligibility {
    public static final String INELIGIBLE_CHECKIN_TOO_LATE = "DRIVER_CHECKIN_TOO_LATE";
    public static final String INELIGIBLE_OFF_DUTY = "OFF_DUTY";
    /** 路线已进入装车流程，不得参与沙盘新分派 */
    public static final String INELIGIBLE_LOADING = "LOADING";
    public static final String NOT_DRIVER_ROLE = "NOT_DRIVER_ROLE";
    public static final String NOT_BELONG_TO_DIS = "NOT_BELONG_TO_DIS";

    private DisRouteBatchEligibility() {
    }
}
