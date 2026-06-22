package com.nongxinle.route;

/**
 * Phase 3a：沙盘建议站点唯一键（未落库 task 时前端确认入口使用）。
 */
public final class DisRouteSandboxStopKeyUtils {

    public static final String PREFIX = "dep:";

    private DisRouteSandboxStopKeyUtils() {
    }

    public static String build(Integer depFatherId) {
        if (depFatherId == null) {
            return null;
        }
        return PREFIX + depFatherId;
    }

    public static Integer parseDepFatherId(String sandboxStopKey) {
        if (sandboxStopKey == null || !sandboxStopKey.startsWith(PREFIX)) {
            return null;
        }
        try {
            return Integer.parseInt(sandboxStopKey.substring(PREFIX.length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
