package com.nongxinle.route;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.List;

import static com.nongxinle.route.DisRoutePlanStatus.ASSIGNED;
import static com.nongxinle.route.DisRoutePlanStatus.CANCELLED;
import static com.nongxinle.route.DisRoutePlanStatus.READY;
import static com.nongxinle.route.DisRoutePlanStatus.SIMULATED;
import static com.nongxinle.route.DisShipmentTaskStatus.DELIVERED;
import static com.nongxinle.route.DisShipmentTaskStatus.EXCEPTION;
import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;

/**
 * Phase 3D：出发后 plan 不得退回 SIMULATED/null — 从 execution route / task 找回 DB plan 头。
 */
public final class DisRouteExecutionPlanHelper {

    private DisRouteExecutionPlanHelper() {
    }

    public static boolean isPersistedPlan(NxDisRoutePlanEntity plan) {
        return plan != null && plan.getNxDrpId() != null;
    }

    public static boolean isEmptySimulatedPlan(NxDisRoutePlanEntity plan) {
        return plan != null && plan.getNxDrpId() == null && SIMULATED.equals(plan.getNxDrpStatus());
    }

    /** 将 execution route 关联的 DB plan 头合并进 sandbox plan（避免 nxDrpId=null）。 */
    public static void attachPlanHeaderFromRoutes(NxDisRoutePlanEntity plan,
                                                  List<NxDisDriverRouteEntity> routes) {
        if (plan == null || isPersistedPlan(plan) || routes == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null || !DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
                continue;
            }
            Integer planId = route.getNxDdrPlanId();
            if (planId != null) {
                copyPlanIdentity(plan, planId);
                return;
            }
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route != null && route.getNxDdrPlanId() != null) {
                copyPlanIdentity(plan, route.getNxDdrPlanId());
                return;
            }
        }
    }

    public static void copyPlanFields(NxDisRoutePlanEntity target, NxDisRoutePlanEntity source) {
        if (target == null || source == null || source.getNxDrpId() == null) {
            return;
        }
        target.setNxDrpId(source.getNxDrpId());
        if (source.getNxDrpStatus() != null) {
            target.setNxDrpStatus(source.getNxDrpStatus());
        }
        if (source.getNxDrpRouteDate() != null) {
            target.setNxDrpRouteDate(source.getNxDrpRouteDate());
        }
        if (source.getNxDrpPlanDate() != null) {
            target.setNxDrpPlanDate(source.getNxDrpPlanDate());
        }
        if (source.getNxDrpDispatchBatch() != null) {
            target.setNxDrpDispatchBatch(source.getNxDrpDispatchBatch());
        }
        if (source.getNxDrpCostProviderType() != null) {
            target.setNxDrpCostProviderType(source.getNxDrpCostProviderType());
        }
        if (source.getNxDrpDepotLat() != null) {
            target.setNxDrpDepotLat(source.getNxDrpDepotLat());
        }
        if (source.getNxDrpDepotLng() != null) {
            target.setNxDrpDepotLng(source.getNxDrpDepotLng());
        }
    }

    public static boolean hasExecutionDriverRoute(List<NxDisDriverRouteEntity> routes) {
        if (routes == null) {
            return false;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null) {
                continue;
            }
            if (DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasConfirmedOrExecutionTasks(List<NxDisShipmentTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return false;
        }
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task == null || DisShipmentTaskStatus.CANCELLED.equals(task.getNxDstStatus())) {
                continue;
            }
            if (task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1) {
                return true;
            }
            if (IN_DELIVERY.equals(task.getNxDstStatus()) || DELIVERED.equals(task.getNxDstStatus())
                    || DisShipmentTaskStatus.EXCEPTION.equals(task.getNxDstStatus())) {
                return true;
            }
        }
        return false;
    }

    public static String[] activePlanStatuses() {
        return new String[]{ASSIGNED, READY};
    }

    private static void copyPlanIdentity(NxDisRoutePlanEntity plan, Integer planId) {
        plan.setNxDrpId(planId);
        if (plan.getNxDrpStatus() == null || SIMULATED.equals(plan.getNxDrpStatus())) {
            plan.setNxDrpStatus(ASSIGNED);
        }
    }

    public static boolean isCancelledPlan(NxDisRoutePlanEntity plan) {
        return plan != null && CANCELLED.equals(plan.getNxDrpStatus());
    }
}
