package com.nongxinle.route;

import com.nongxinle.dto.route.SandboxComputeResult;
import com.nongxinle.dto.route.RouteDispatchReadModelAssembler;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.List;

/** 人工调度 / 装车中路线编辑：对齐 Today 的 confirmed stop 分区。 */
public final class DisRouteSandboxManualDispatchComputeEnricher {

    private DisRouteSandboxManualDispatchComputeEnricher() {
    }

    public static void enrich(SandboxComputeResult compute) {
        if (compute == null || compute.getMergedPlan() == null) {
            return;
        }
        NxDisRoutePlanEntity mergedPlan = compute.getMergedPlan();
        List<NxDisShipmentTaskEntity> tasks = compute.getAllDisplayTasks();
        if (tasks != null && !tasks.isEmpty()) {
            RouteDispatchReadModelAssembler.linkSharedTaskInstances(mergedPlan, tasks);
            DisRouteSandboxReadModelPartitionHelper.linkConfirmedStopsToTasks(
                    compute.getConfirmedStops(), tasks);
        }
        DisRouteSandboxReadModelPartitionHelper.StopPartition stopPartition =
                DisRouteSandboxReadModelPartitionHelper.partitionConfirmedStops(
                        compute.getConfirmedStops(),
                        DisRouteSandboxReadModelPartitionHelper.buildRouteIndex(mergedPlan));
        DisRouteSandboxReadModelPartitionHelper.partitionPlanRoutes(mergedPlan);
        compute.setConfirmedStops(stopPartition.sandboxStops);
        compute.setLoadingStops(stopPartition.loadingStops);
        compute.setExecutionStops(stopPartition.executionStops);
    }
}
