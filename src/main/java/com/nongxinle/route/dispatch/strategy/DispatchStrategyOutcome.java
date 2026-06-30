package com.nongxinle.route.dispatch.strategy;

import lombok.Getter;

/**
 * 策略执行结果。PR-2a：{@link #delegateLegacyOptimizer} 为 true，compute 走旧 optimizer。
 */
@Getter
public class DispatchStrategyOutcome {

    private final DispatchStrategyMode strategyMode;
    private final DispatchAssignmentPlan plan;
    private final boolean delegateLegacyOptimizer;

    private DispatchStrategyOutcome(DispatchStrategyMode strategyMode,
                                    DispatchAssignmentPlan plan,
                                    boolean delegateLegacyOptimizer) {
        this.strategyMode = strategyMode;
        this.plan = plan;
        this.delegateLegacyOptimizer = delegateLegacyOptimizer;
    }

    public static DispatchStrategyOutcome delegateLegacyOptimizer(DispatchAssignmentPlan plan) {
        DispatchStrategyMode mode = plan != null && plan.getStrategyMode() != null
                ? plan.getStrategyMode() : DispatchStrategyMode.OWNER_FIXED_ROUTE;
        return new DispatchStrategyOutcome(mode, plan, true);
    }

    public static DispatchStrategyOutcome fromPlan(DispatchAssignmentPlan plan) {
        DispatchStrategyMode mode = plan != null && plan.getStrategyMode() != null
                ? plan.getStrategyMode() : DispatchStrategyMode.OWNER_FIXED_ROUTE;
        return new DispatchStrategyOutcome(mode, plan, false);
    }

    /** 策略已评估但未委托旧 optimizer（如无 eligible 司机）。 */
    public static DispatchStrategyOutcome skipped(DispatchAssignmentPlan plan) {
        DispatchStrategyMode mode = plan != null && plan.getStrategyMode() != null
                ? plan.getStrategyMode() : DispatchStrategyMode.OWNER_FIXED_ROUTE;
        return new DispatchStrategyOutcome(mode, plan, false);
    }
}
