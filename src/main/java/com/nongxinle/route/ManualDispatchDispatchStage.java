package com.nongxinle.route;

/** Phase 2B：人工调度司机全景 — 调度阶段。 */
public final class ManualDispatchDispatchStage {

    public static final String IDLE = "IDLE";
    public static final String SANDBOX = "SANDBOX";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String LOADING = "LOADING";
    public static final String EXECUTION = "EXECUTION";
    public static final String COMPLETED = "COMPLETED";

    private ManualDispatchDispatchStage() {
    }

    /**
     * 今日派车 duty card / listDrivers：ON_DUTY 且无 LOADING/EXECUTION 时统一为 IDLE。
     * 不再向客户端输出 COMPLETED / SANDBOX / CONFIRMED 作为当前运行时阶段。
     */
    public static String toTodayDutyCardStage(String stage) {
        if (stage == null || stage.trim().isEmpty()) {
            return IDLE;
        }
        String normalized = stage.trim().toUpperCase();
        if (LOADING.equals(normalized) || EXECUTION.equals(normalized)) {
            return normalized;
        }
        return IDLE;
    }

    public static String todayDutyCardStageLabel(String dutyCardStage) {
        if (LOADING.equals(dutyCardStage)) {
            return label(LOADING);
        }
        if (EXECUTION.equals(dutyCardStage)) {
            return label(EXECUTION);
        }
        return label(IDLE);
    }

    public static String label(String stage) {
        if (stage == null || stage.trim().isEmpty()) {
            return null;
        }
        switch (stage.trim().toUpperCase()) {
            case IDLE:
                return "空闲";
            case SANDBOX:
                return "沙盘中";
            case CONFIRMED:
                return "已确认待装车";
            case LOADING:
                return "装车中";
            case EXECUTION:
                return "配送中";
            case COMPLETED:
                return "已完成 / 可再派";
            default:
                return stage.trim();
        }
    }
}
