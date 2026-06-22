package com.nongxinle.route;

import com.nongxinle.dto.route.SandboxManualDispatchManualTimeConstraintDto;

/** 人工时间约束读模型：统一 summaryLabel / manualConstraintSummary。 */
public final class DisRouteManualTimeConstraintHelper {

    private DisRouteManualTimeConstraintHelper() {
    }

    public static SandboxManualDispatchManualTimeConstraintDto enrich(SandboxManualDispatchManualTimeConstraintDto constraint) {
        if (constraint == null) {
            return empty();
        }
        constraint.setSummaryLabel(buildSummaryLabel(constraint));
        return constraint;
    }

    public static SandboxManualDispatchManualTimeConstraintDto empty() {
        SandboxManualDispatchManualTimeConstraintDto constraint = new SandboxManualDispatchManualTimeConstraintDto();
        constraint.setManualArrivalSpecified(Boolean.FALSE);
        constraint.setSummaryLabel("未设置人工约束");
        return constraint;
    }

    public static String buildSummaryLabel(SandboxManualDispatchManualTimeConstraintDto constraint) {
        if (constraint == null || !Boolean.TRUE.equals(constraint.getManualArrivalSpecified())) {
            return "未设置人工约束";
        }
        StringBuilder sb = new StringBuilder();
        if (constraint.getRequiredLatestArrivalLabel() != null
                && !constraint.getRequiredLatestArrivalLabel().trim().isEmpty()) {
            sb.append("必须 ").append(constraint.getRequiredLatestArrivalLabel().trim()).append(" 前到");
        } else if (constraint.getRequiredLatestArrivalAt() != null
                && !constraint.getRequiredLatestArrivalAt().trim().isEmpty()) {
            sb.append("必须 ").append(constraint.getRequiredLatestArrivalAt().trim()).append(" 前到");
        }
        if (constraint.getPreferredArrivalLabel() != null
                && !constraint.getPreferredArrivalLabel().trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append("最好 ").append(constraint.getPreferredArrivalLabel().trim()).append(" 到");
        }
        if (Boolean.TRUE.equals(constraint.getAllowLate())) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append("允许晚到");
        } else if (Boolean.FALSE.equals(constraint.getAllowLate())) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append("不允许晚到");
        }
        if (constraint.getRemarkReason() != null && !constraint.getRemarkReason().trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append(constraint.getRemarkReason().trim());
        }
        return sb.length() > 0 ? sb.toString() : "已设置人工约束";
    }
}
