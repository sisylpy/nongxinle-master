package com.nongxinle.route.proposal;

import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteLoadingGateHelper;
import com.nongxinle.route.DisRouteSandboxDisplayFormatHelper;
import com.nongxinle.route.DisRouteSandboxDispatchEligibilityHelper;
import com.nongxinle.route.DisRouteSandboxDriverDispatchStateHelper;
import com.nongxinle.route.DisRouteSandboxUnassignedStopHelper;
import com.nongxinle.route.dispatch.strategy.DispatchAssignmentPlan;
import com.nongxinle.route.dispatch.strategy.FallbackStopAssignment;
import com.nongxinle.route.dispatch.strategy.StopAssignment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.route.DisShipmentTaskStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.CLOSED;
import static com.nongxinle.route.DisShipmentTaskStatus.DELIVERED;
import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;

/**
 * 从 compute 产出的 ephemeral suggested/unassigned 构建 {@link SandboxProposalPlan}。
 * 不读取 mergedPlan / DB route / confirmed delivered stops。
 */
public final class SandboxProposalPlanBuilder {

    private SandboxProposalPlanBuilder() {
    }

    public static SandboxProposalPlan build(List<NxDisRouteStopEntity> suggestedStops,
                                            List<NxDisRouteStopEntity> unassignedStops,
                                            DispatchAssignmentPlan assignmentPlan) {
        Set<Integer> historyBoundDepIds = indexHistoryBoundDepIds(assignmentPlan);
        Set<Integer> fallbackDepIds = indexFallbackDepIds(assignmentPlan);

        Map<Integer, ProposalDriverRoute> routeByDriver = new LinkedHashMap<Integer, ProposalDriverRoute>();
        if (suggestedStops != null) {
            for (NxDisRouteStopEntity stop : suggestedStops) {
                if (!isEligibleProposalStop(stop)) {
                    continue;
                }
                Integer driverUserId = DisRouteSandboxDriverDispatchStateHelper.resolveStopDriverUserId(stop);
                if (driverUserId == null) {
                    continue;
                }
                ProposalDriverRoute route = routeByDriver.get(driverUserId);
                if (route == null) {
                    route = new ProposalDriverRoute();
                    route.setDriverUserId(driverUserId);
                    route.setDriverName(resolveDriverName(stop));
                    routeByDriver.put(driverUserId, route);
                }
                route.getStops().add(toProposalStop(stop, resolveProposalSource(
                        stop, historyBoundDepIds, fallbackDepIds, true)));
            }
        }

        List<ProposalStop> proposalUnassigned = new ArrayList<ProposalStop>();
        if (unassignedStops != null) {
            for (NxDisRouteStopEntity stop : unassignedStops) {
                if (!isEligibleProposalStop(stop)) {
                    continue;
                }
                proposalUnassigned.add(toProposalStop(stop, resolveProposalSource(
                        stop, historyBoundDepIds, fallbackDepIds, false)));
            }
        }

        SandboxProposalPlan plan = new SandboxProposalPlan();
        for (ProposalDriverRoute route : routeByDriver.values()) {
            enrichRouteTotals(route);
            if (route.getStops() != null && !route.getStops().isEmpty()) {
                plan.getProposalRoutes().add(route);
            }
        }
        plan.setUnassignedStops(proposalUnassigned);
        plan.setSummary(buildSummary(plan));
        return plan;
    }

    /** 仅本轮 pending ephemeral；排除 delivered / in-delivery / loading-scope / confirmed DB stops。 */
    public static boolean isEligibleProposalStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        if (!DisRouteSandboxDispatchEligibilityHelper.isSandboxEphemeralStop(stop)) {
            return false;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null) {
            String status = task.getNxDstStatus();
            if (status != null) {
                String normalized = status.trim().toUpperCase();
                if (DELIVERED.equals(normalized) || IN_DELIVERY.equals(normalized)
                        || CLOSED.equals(normalized) || CANCELLED.equals(normalized)) {
                    return false;
                }
            }
            if (DisRouteLoadingGateHelper.isLoadingScopeStop(stop, null)) {
                return false;
            }
        }
        return true;
    }

    private static ProposalStopSource resolveProposalSource(NxDisRouteStopEntity stop,
                                                            Set<Integer> historyBoundDepIds,
                                                            Set<Integer> fallbackDepIds,
                                                            boolean assignedToDriver) {
        Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
        if (depId != null && historyBoundDepIds.contains(depId)) {
            return ProposalStopSource.HISTORY;
        }
        if (!assignedToDriver) {
            if (depId != null && fallbackDepIds.contains(depId)) {
                return ProposalStopSource.FALLBACK;
            }
            return ProposalStopSource.UNASSIGNED;
        }
        if (depId != null && fallbackDepIds.contains(depId)) {
            return ProposalStopSource.OPTIMIZER;
        }
        return ProposalStopSource.OPTIMIZER;
    }

    private static ProposalStop toProposalStop(NxDisRouteStopEntity stop, ProposalStopSource source) {
        ProposalStop proposalStop = new ProposalStop();
        proposalStop.setStop(stop);
        proposalStop.setDepFatherId(DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop));
        proposalStop.setCustomerName(resolveCustomerName(stop));
        proposalStop.setProposalSource(source);
        if (source == ProposalStopSource.HISTORY) {
            proposalStop.setHistoryPreferenceNote("history-bound preferred driver");
        }
        return proposalStop;
    }

    private static Set<Integer> indexHistoryBoundDepIds(DispatchAssignmentPlan assignmentPlan) {
        Set<Integer> depIds = new HashSet<Integer>();
        if (assignmentPlan == null || assignmentPlan.getDriverRoutes() == null) {
            return depIds;
        }
        for (com.nongxinle.route.dispatch.strategy.DriverRoutePlan route : assignmentPlan.getDriverRoutes()) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            for (StopAssignment assignment : route.getStops()) {
                if (assignment != null && assignment.getDepFatherId() != null) {
                    depIds.add(assignment.getDepFatherId());
                }
            }
        }
        return depIds;
    }

    private static Set<Integer> indexFallbackDepIds(DispatchAssignmentPlan assignmentPlan) {
        Set<Integer> depIds = new HashSet<Integer>();
        if (assignmentPlan == null || assignmentPlan.getFallbackStops() == null) {
            return depIds;
        }
        for (FallbackStopAssignment fallback : assignmentPlan.getFallbackStops()) {
            if (fallback != null && fallback.getDepFatherId() != null) {
                depIds.add(fallback.getDepFatherId());
            }
        }
        return depIds;
    }

    private static String resolveDriverName(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getSuggestedDriverName() != null && !stop.getSuggestedDriverName().trim().isEmpty()) {
            return stop.getSuggestedDriverName().trim();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && task.getSuggestedDriverName() != null
                && !task.getSuggestedDriverName().trim().isEmpty()) {
            return task.getSuggestedDriverName().trim();
        }
        return null;
    }

    private static String resolveCustomerName(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getNxDrsDepartmentName() != null && !stop.getNxDrsDepartmentName().trim().isEmpty()) {
            return stop.getNxDrsDepartmentName().trim();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && task.getNxDstDepName() != null) {
            return task.getNxDstDepName().trim();
        }
        return null;
    }

    private static void enrichRouteTotals(ProposalDriverRoute route) {
        if (route == null || route.getStops() == null) {
            return;
        }
        long distanceM = 0L;
        long durationS = 0L;
        for (ProposalStop proposalStop : route.getStops()) {
            NxDisRouteStopEntity stop = proposalStop != null ? proposalStop.getStop() : null;
            if (stop == null) {
                continue;
            }
            Long legDistanceM = DisRouteSandboxDisplayFormatHelper.resolveLegDistanceM(stop);
            Long legDurationS = DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop);
            if (legDistanceM != null) {
                distanceM += legDistanceM;
            }
            if (legDurationS != null) {
                durationS += legDurationS;
            }
        }
        route.setTotalDistanceM(distanceM);
        route.setTotalDurationS(durationS);
        route.setTotalDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(distanceM));
        route.setTotalDurationText(DisRouteSandboxDisplayFormatHelper.formatDurationText(durationS));
    }

    private static SandboxProposalPlanSummary buildSummary(SandboxProposalPlan plan) {
        SandboxProposalPlanSummary summary = new SandboxProposalPlanSummary();
        int assigned = 0;
        if (plan.getProposalRoutes() != null) {
            summary.setProposalRouteCount(plan.getProposalRoutes().size());
            for (ProposalDriverRoute route : plan.getProposalRoutes()) {
                if (route != null && route.getStops() != null) {
                    assigned += route.getStops().size();
                }
            }
        }
        int unassigned = plan.getUnassignedStops() != null ? plan.getUnassignedStops().size() : 0;
        summary.setAssignedStopCount(assigned);
        summary.setUnassignedStopCount(unassigned);
        summary.setCustomerStopCount(assigned + unassigned);
        return summary;
    }

    public static List<NxDisRouteStopEntity> toStopEntities(List<ProposalStop> proposalStops) {
        List<NxDisRouteStopEntity> stops = new ArrayList<NxDisRouteStopEntity>();
        if (proposalStops == null) {
            return stops;
        }
        for (ProposalStop proposalStop : proposalStops) {
            if (proposalStop != null && proposalStop.getStop() != null) {
                stops.add(proposalStop.getStop());
            }
        }
        return stops;
    }
}
