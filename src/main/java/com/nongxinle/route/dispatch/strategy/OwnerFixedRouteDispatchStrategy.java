package com.nongxinle.route.dispatch.strategy;

import com.nongxinle.route.RouteDispatchDateFormat;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDistributerUserEntity;

/**
 * OWNER 模式：历史司机预绑定 + fallback 走旧 optimizer。
 */
@Component
public class OwnerFixedRouteDispatchStrategy implements DispatchStrategy {

    @Override
    public DispatchStrategyMode strategyMode() {
        return DispatchStrategyMode.OWNER_FIXED_ROUTE;
    }

    @Override
    public DispatchStrategyOutcome plan(DispatchStrategyContext context) {
        DispatchAssignmentPlan plan = buildBasePlan(context);
        plan.setStrategyMode(DispatchStrategyMode.OWNER_FIXED_ROUTE);
        plan.setResolvedAt(RouteDispatchDateFormat.format(new Date()));

        List<NxDisShipmentTaskEntity> virtualTasks = context != null ? context.getVirtualTasks() : null;
        if (virtualTasks == null || virtualTasks.isEmpty()) {
            plan.setPlanningPhase(DispatchPlanningPhase.SKIPPED_NO_PENDING_STOPS);
            plan.getWarnings().add("no pending virtualTasks; legacy optimizer not invoked");
            return DispatchStrategyOutcome.skipped(plan);
        }

        List<NxDistributerUserEntity> eligibleDrivers = context.getDispatchEligibleDrivers();
        if (eligibleDrivers == null || eligibleDrivers.isEmpty()) {
            plan.setPlanningPhase(DispatchPlanningPhase.SKIPPED_NO_ELIGIBLE_DRIVERS);
            plan.getWarnings().add("no dispatch-eligible drivers; legacy optimizer not invoked");
            return DispatchStrategyOutcome.skipped(plan);
        }

        List<NxDisShipmentTaskEntity> optimizable = context.getOptimizableTasks();
        if (optimizable == null || optimizable.isEmpty()) {
            plan.setPlanningPhase(DispatchPlanningPhase.SKIPPED_NOT_OPTIMIZABLE);
            plan.getWarnings().add("pending stops exist but none are optimizable; legacy optimizer not invoked");
            return DispatchStrategyOutcome.skipped(plan);
        }

        Set<Integer> eligibleDriverIds = OwnerFixedRouteHistoryDriverBinder.eligibleDriverIds(eligibleDrivers);
        Map<Integer, String> driverNameById = OwnerFixedRouteHistoryDriverBinder.buildDriverNameIndex(eligibleDrivers);
        OwnerFixedRouteHistoryDriverBinder.buildPlan(plan, context, optimizable, eligibleDriverIds, driverNameById);
        OwnerFixedRouteTimeWindowRouteSequencer.sequencePlan(plan, context);

        if (OwnerFixedRouteHistoryDriverBinder.shouldDelegateLegacyOptimizer(plan)) {
            return DispatchStrategyOutcome.delegateLegacyOptimizer(plan);
        }
        return DispatchStrategyOutcome.fromPlan(plan);
    }

    private static DispatchAssignmentPlan buildBasePlan(DispatchStrategyContext context) {
        DispatchAssignmentPlan plan = new DispatchAssignmentPlan();
        if (context != null) {
            plan.setDisId(context.getDisId());
            plan.setRouteDate(context.getRouteDate());
            plan.setBatchCode(context.getBatchCode());
        }
        return plan;
    }
}
