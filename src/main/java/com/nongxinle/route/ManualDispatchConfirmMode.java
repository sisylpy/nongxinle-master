package com.nongxinle.route;

/** Phase 2B：人工调度确认模式（前端只渲染，不自行推断）。 */
public final class ManualDispatchConfirmMode {

    public static final String DIRECT = "DIRECT";
    public static final String RISK_ACK = "RISK_ACK";
    public static final String FORBIDDEN = "FORBIDDEN";

    private ManualDispatchConfirmMode() {
    }

    public static String label(String confirmMode) {
        if (confirmMode == null || confirmMode.trim().isEmpty()) {
            return "";
        }
        switch (confirmMode.trim().toUpperCase()) {
            case DIRECT:
                return "可直接确认";
            case RISK_ACK:
                return "需风险提示";
            case FORBIDDEN:
                return "不可操作";
            default:
                return confirmMode.trim();
        }
    }
}
