package com.nongxinle.route;

/** Phase 3a：沙盘排程模式 */
public final class DisRouteSandboxScheduleMode {

    /** 正式预排（按客户送达时间窗 / 批次） */
    public static final String SCHEDULED_BATCH = "SCHEDULED_BATCH";
    /** 即时补单（从 serverNow 起算最快送达） */
    public static final String ADHOC_NOW = "ADHOC_NOW";

    private DisRouteSandboxScheduleMode() {
    }
}
