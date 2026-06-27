package com.nongxinle.route;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 将 mergedPlan 路线上的排程 enrichment 同步到 compute 侧 stop 实例。 */
public final class DisRouteSandboxTodayPlanStopSyncHelper {

    private DisRouteSandboxTodayPlanStopSyncHelper() {
    }

    /** 正式 Today dispatch page：仅同步 sandbox 窗口/服务展示标签，不复制 seq / leg / planned Date。 */
    public static void syncSandboxScheduleLabelsFromPlan(NxDisRoutePlanEntity plan,
                                                         List<NxDisRouteStopEntity> suggestedStops,
                                                         List<NxDisRouteStopEntity> confirmedStops,
                                                         List<NxDisRouteStopEntity> unassignedStops) {
        if (plan == null) {
            return;
        }
        Map<String, NxDisRouteStopEntity> planStopsByKey = indexSandboxPlanStops(plan);
        if (planStopsByKey.isEmpty()) {
            return;
        }
        syncSandboxLabelStopList(planStopsByKey, suggestedStops);
        syncSandboxLabelStopList(planStopsByKey, confirmedStops);
        syncSandboxLabelStopList(planStopsByKey, unassignedStops);
    }

    /** 装车 / 配送 / 司机终端：从 plan 全量同步 seq / leg / ETA（含 loading / execution routes）。 */
    public static void syncFullScheduleFromPlan(NxDisRoutePlanEntity plan,
                                                List<NxDisRouteStopEntity> suggestedStops,
                                                List<NxDisRouteStopEntity> confirmedStops,
                                                List<NxDisRouteStopEntity> loadingStops,
                                                List<NxDisRouteStopEntity> executionStops,
                                                List<NxDisRouteStopEntity> unassignedStops) {
        if (plan == null) {
            return;
        }
        Map<String, NxDisRouteStopEntity> planStopsByKey = indexExecutionPlanStops(plan);
        if (planStopsByKey.isEmpty()) {
            return;
        }
        syncFullScheduleStopList(planStopsByKey, suggestedStops);
        syncFullScheduleStopList(planStopsByKey, confirmedStops);
        syncFullScheduleStopList(planStopsByKey, loadingStops);
        syncFullScheduleStopList(planStopsByKey, executionStops);
        syncFullScheduleStopList(planStopsByKey, unassignedStops);
    }

    private static Map<String, NxDisRouteStopEntity> indexSandboxPlanStops(NxDisRoutePlanEntity plan) {
        Map<String, NxDisRouteStopEntity> index = new HashMap<String, NxDisRouteStopEntity>();
        indexRoutes(plan.getDriverRoutes(), index);
        return index;
    }

    private static Map<String, NxDisRouteStopEntity> indexExecutionPlanStops(NxDisRoutePlanEntity plan) {
        Map<String, NxDisRouteStopEntity> index = new HashMap<String, NxDisRouteStopEntity>();
        indexRoutes(plan.getDriverRoutes(), index);
        indexRoutes(plan.getLoadingDriverRoutes(), index);
        indexRoutes(plan.getExecutionDriverRoutes(), index);
        return index;
    }

    private static void syncSandboxLabelStopList(Map<String, NxDisRouteStopEntity> planStopsByKey,
                                                 List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            NxDisRouteStopEntity planStop = planStopsByKey.get(resolveStopKey(stop));
            if (planStop != null) {
                DisRouteSandboxTodayStopScheduleHelper.copySandboxScheduleLabelFields(planStop, stop);
            }
        }
    }

    private static void syncFullScheduleStopList(Map<String, NxDisRouteStopEntity> planStopsByKey,
                                                 List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            NxDisRouteStopEntity planStop = planStopsByKey.get(resolveStopKey(stop));
            if (planStop != null) {
                DisRouteSandboxTodayStopScheduleHelper.copyFullScheduleFieldsForExecutionView(planStop, stop);
            }
        }
    }

    private static void indexRoutes(List<NxDisDriverRouteEntity> routes,
                                    Map<String, NxDisRouteStopEntity> index) {
        if (routes == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop == null) {
                    continue;
                }
                index.put(resolveStopKey(stop), stop);
            }
        }
    }

    private static String resolveStopKey(NxDisRouteStopEntity stop) {
        if (stop.getSandboxStopKey() != null && !stop.getSandboxStopKey().trim().isEmpty()) {
            return stop.getSandboxStopKey().trim();
        }
        if (stop.getNxDrsDepartmentId() != null) {
            return DisRouteSandboxStopKeyUtils.build(stop.getNxDrsDepartmentId());
        }
        if (stop.getNxDrsId() != null) {
            return "stop:" + stop.getNxDrsId();
        }
        return "stop";
    }
}
