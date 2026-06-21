package com.nongxinle.route;

/** Phase 2b-1：司机批次可派性判定常量 */
public final class DisRouteBatchEligibility {
    /** checkInAt 允许晚于 defaultDepartAt 的宽限分钟（后续可迁配送商配置） */
    public static final int CHECKIN_GRACE_MINUTES = 30;

    public static final String INELIGIBLE_CHECKIN_TOO_LATE = "DRIVER_CHECKIN_TOO_LATE";
    public static final String INELIGIBLE_OFF_DUTY = "OFF_DUTY";
    public static final String NOT_DRIVER_ROLE = "NOT_DRIVER_ROLE";
    public static final String NOT_BELONG_TO_DIS = "NOT_BELONG_TO_DIS";

    private DisRouteBatchEligibility() {
    }
}
