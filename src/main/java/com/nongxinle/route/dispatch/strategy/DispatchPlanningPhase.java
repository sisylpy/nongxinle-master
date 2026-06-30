package com.nongxinle.route.dispatch.strategy;

/** 策略计划所处阶段。PR-2a 为 LEGACY_OPTIMIZER_DELEGATION 或 SKIPPED_*。 */
public enum DispatchPlanningPhase {
    /** 委托旧 BalancedInsertion2OptRouteOptimizer，Plan 仅作 debug 占位。 */
    LEGACY_OPTIMIZER_DELEGATION,
    /** 策略层已产出完整分派计划（PR-2b 起）。 */
    STRATEGY_PLANNED,
    /** 无待分派 virtualTasks，未调用 optimizer。 */
    SKIPPED_NO_PENDING_STOPS,
    /** 无 dispatch eligible 司机，未调用 optimizer。 */
    SKIPPED_NO_ELIGIBLE_DRIVERS,
    /** 有待分派任务但均不可优化（如坐标无效），未调用 optimizer。 */
    SKIPPED_NOT_OPTIMIZABLE,
    /** PR-2b：历史司机预绑定（可混合 fallback optimizer）。 */
    HISTORY_DRIVER_PREBIND
}
