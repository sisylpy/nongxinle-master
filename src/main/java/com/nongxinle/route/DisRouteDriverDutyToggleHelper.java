package com.nongxinle.route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 司机可派状态页：按派单阶段判定是否允许关闭可派。
 */
public final class DisRouteDriverDutyToggleHelper {

    public static final String TOGGLE_DISABLED_REASON = "司机当前有进行中的配送任务，不可关闭可派";

    private DisRouteDriverDutyToggleHelper() {
    }

    /** ON_DUTY 司机是否允许关闭可派（IDLE / COMPLETED 可关；CONFIRMED / LOADING / EXECUTION 不可关）。 */
    public static boolean canCloseDuty(String dispatchStage) {
        if (dispatchStage == null || dispatchStage.trim().isEmpty()) {
            return true;
        }
        switch (dispatchStage.trim().toUpperCase()) {
            case ManualDispatchDispatchStage.CONFIRMED:
            case ManualDispatchDispatchStage.LOADING:
            case ManualDispatchDispatchStage.EXECUTION:
                return false;
            default:
                return true;
        }
    }

    public static String resolveToggleDisabledReason(String dispatchStage) {
        if (canCloseDuty(dispatchStage)) {
            return null;
        }
        return TOGGLE_DISABLED_REASON;
    }

    public static List<String> buildDutyRiskHints(String dispatchStage) {
        if (dispatchStage == null) {
            return Collections.emptyList();
        }
        List<String> hints = new ArrayList<String>();
        switch (dispatchStage.trim().toUpperCase()) {
            case ManualDispatchDispatchStage.CONFIRMED:
                hints.add("司机路线已确认，关闭可派前请先处理已确认客户");
                break;
            case ManualDispatchDispatchStage.LOADING:
                hints.add("司机正在装车，不可关闭可派");
                break;
            case ManualDispatchDispatchStage.EXECUTION:
                hints.add("司机正在配送中，不可关闭可派");
                break;
            default:
                break;
        }
        return hints;
    }
}
