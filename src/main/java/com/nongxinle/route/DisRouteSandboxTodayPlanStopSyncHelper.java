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

    public static void syncScheduleFromPlan(NxDisRoutePlanEntity plan,
                                            List<NxDisRouteStopEntity> suggestedStops,
                                            List<NxDisRouteStopEntity> confirmedStops,
                                            List<NxDisRouteStopEntity> loadingStops,
                                            List<NxDisRouteStopEntity> executionStops,
                                            List<NxDisRouteStopEntity> unassignedStops) {
        if (plan == null) {
            return;
        }
        Map<String, NxDisRouteStopEntity> planStopsByKey = indexPlanStops(plan);
        if (planStopsByKey.isEmpty()) {
            return;
        }
        syncStopList(planStopsByKey, suggestedStops);
        syncStopList(planStopsByKey, confirmedStops);
        syncStopList(planStopsByKey, loadingStops);
        syncStopList(planStopsByKey, executionStops);
        syncStopList(planStopsByKey, unassignedStops);
    }

    private static void syncStopList(Map<String, NxDisRouteStopEntity> planStopsByKey,
                                     List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            String key = resolveStopKey(stop);
            NxDisRouteStopEntity planStop = planStopsByKey.get(key);
            if (planStop != null) {
                DisRouteSandboxTodayStopScheduleHelper.copyScheduleFields(planStop, stop);
            }
        }
    }

    private static Map<String, NxDisRouteStopEntity> indexPlanStops(NxDisRoutePlanEntity plan) {
        Map<String, NxDisRouteStopEntity> index = new HashMap<String, NxDisRouteStopEntity>();
        indexRoutes(plan.getDriverRoutes(), index);
        indexRoutes(plan.getLoadingDriverRoutes(), index);
        indexRoutes(plan.getExecutionDriverRoutes(), index);
        return index;
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
