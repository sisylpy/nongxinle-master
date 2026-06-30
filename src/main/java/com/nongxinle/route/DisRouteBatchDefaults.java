package com.nongxinle.route;

import com.nongxinle.dto.route.DisRouteBatchContext;
import com.nongxinle.entity.NxDisRoutePlanEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Phase 2b-1：配送批次默认时间（系统级，后续可迁配送商配置）。
 */
public final class DisRouteBatchDefaults {

    private DisRouteBatchDefaults() {
    }

    public static DisRouteBatchContext fromPlan(NxDisRoutePlanEntity plan) {
        DisRouteBatchContext context = new DisRouteBatchContext();
        if (plan == null) {
            return context;
        }
        String batchCode = normalizeBatchCode(plan.getNxDrpDispatchBatch());
        context.setBatchCode(batchCode);
        if (needsBatchPersist(plan)) {
            DisRouteBatchContext defaults = resolveForRouteDate(resolveRouteDateFromPlan(plan), batchCode);
            context.setBatchStartAt(defaults.getBatchStartAt());
            context.setDefaultDepartAt(defaults.getDefaultDepartAt());
            context.setBatchEndAt(defaults.getBatchEndAt());
            return context;
        }
        context.setBatchStartAt(plan.getNxDrpBatchStartAt());
        context.setDefaultDepartAt(plan.getNxDrpDefaultDepartAt());
        context.setBatchEndAt(plan.getNxDrpBatchEndAt());
        return context;
    }

    /** 旧 plan 缺 batch 快照时返回 true，需写库回填。 */
    public static boolean needsBatchPersist(NxDisRoutePlanEntity plan) {
        if (plan == null) {
            return false;
        }
        if (isBlank(plan.getNxDrpDispatchBatch())) {
            return true;
        }
        return plan.getNxDrpBatchStartAt() == null
                || plan.getNxDrpDefaultDepartAt() == null
                || plan.getNxDrpBatchEndAt() == null;
    }

    /** 按 routeDate + batchCode 生成系统默认批次时间（不含 ADHOC 自定义）。 */
    public static DisRouteBatchContext resolveForRouteDate(String routeDate, String batchCode) {
        String normalizedBatch = normalizeBatchCode(batchCode);
        if (DisRouteDispatchBatch.ADHOC.equals(normalizedBatch)) {
            throw new IllegalArgumentException("ADHOC 批次缺少 batchStartAt/defaultDepartAt/batchEndAt，无法自动回填");
        }
        DisRouteBatchContext context = new DisRouteBatchContext();
        context.setBatchCode(normalizedBatch);
        Date dayStart = parseRouteDateStart(routeDate);
        if (DisRouteDispatchBatch.AFTERNOON.equals(normalizedBatch)) {
            context.setBatchStartAt(combine(dayStart, 13, 0));
            context.setDefaultDepartAt(combine(dayStart, 13, 30));
            context.setBatchEndAt(combine(dayStart, 18, 0));
        } else {
            context.setBatchStartAt(combine(dayStart, 5, 30));
            context.setDefaultDepartAt(combine(dayStart, 6, 0));
            context.setBatchEndAt(combine(dayStart, 12, 0));
        }
        return context;
    }

    public static String resolveRouteDateFromPlan(NxDisRoutePlanEntity plan) {
        if (plan.getNxDrpRouteDate() != null && !plan.getNxDrpRouteDate().trim().isEmpty()) {
            return plan.getNxDrpRouteDate().trim();
        }
        if (plan.getNxDrpPlanDate() != null && !plan.getNxDrpPlanDate().trim().isEmpty()) {
            return plan.getNxDrpPlanDate().trim();
        }
        throw new IllegalArgumentException("路线计划缺少 routeDate");
    }

    public static Date resolveDefaultDepartAt(NxDisRoutePlanEntity plan) {
        if (plan != null && plan.getNxDrpDefaultDepartAt() != null) {
            return plan.getNxDrpDefaultDepartAt();
        }
        if (plan != null && needsBatchPersist(plan)) {
            return resolveForRouteDate(resolveRouteDateFromPlan(plan),
                    normalizeBatchCode(plan.getNxDrpDispatchBatch())).getDefaultDepartAt();
        }
        String routeDate = plan != null ? plan.getNxDrpRouteDate() : null;
        if (routeDate == null && plan != null) {
            routeDate = plan.getNxDrpPlanDate();
        }
        if (routeDate == null || routeDate.trim().isEmpty()) {
            return null;
        }
        return combine(parseRouteDateStart(routeDate.trim()), 6, 0);
    }

    private static String normalizeBatchCode(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return DisRouteDispatchBatch.MORNING;
        }
        return raw.trim().toUpperCase();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static Date parseRouteDateStart(String routeDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setLenient(false);
            return format.parse(routeDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("路线日格式无效: " + routeDate);
        }
    }

    private static Date combine(Date dayStart, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dayStart);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
