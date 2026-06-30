package com.nongxinle.todaydispatch;

import com.nongxinle.dto.route.RouteDispatchReadModelAssembler;
import com.nongxinle.dto.route.SandboxComputeResult;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteSandboxReadModelPartitionHelper;

import java.util.Collections;
import java.util.List;

/** compute 结果 → 装车/配送页共用的 plan 分区上下文。 */
final class TodayDispatchPlanContextHelper {

    private TodayDispatchPlanContextHelper() {
    }

    static PlanContext prepare(SandboxComputeResult compute) {
        PlanContext context = new PlanContext();
        context.mergedPlan = compute != null ? compute.getMergedPlan() : null;
        if (context.mergedPlan == null) {
            return context;
        }
        List<NxDisShipmentTaskEntity> tasks = compute.getAllDisplayTasks();
        if (tasks == null) {
            tasks = Collections.emptyList();
        }
        RouteDispatchReadModelAssembler.linkSharedTaskInstances(context.mergedPlan, tasks);
        DisRouteSandboxReadModelPartitionHelper.linkConfirmedStopsToTasks(
                compute.getConfirmedStops(), tasks);
        context.stopPartition = DisRouteSandboxReadModelPartitionHelper.partitionConfirmedStops(
                compute.getConfirmedStops(),
                DisRouteSandboxReadModelPartitionHelper.buildRouteIndex(context.mergedPlan));
        DisRouteSandboxReadModelPartitionHelper.partitionPlanRoutes(context.mergedPlan);
        return context;
    }

    static final class PlanContext {
        NxDisRoutePlanEntity mergedPlan;
        DisRouteSandboxReadModelPartitionHelper.StopPartition stopPartition;
    }
}
