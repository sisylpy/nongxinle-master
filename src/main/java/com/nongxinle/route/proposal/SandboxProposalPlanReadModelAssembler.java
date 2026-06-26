package com.nongxinle.route.proposal;

import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.route.dispatch.strategy.DispatchAssignmentPlan;
import com.nongxinle.route.dispatch.strategy.StopAssignment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** debug：{@code GET /sandbox/today} → proposalPlan。 */
public final class SandboxProposalPlanReadModelAssembler {

    private SandboxProposalPlanReadModelAssembler() {
    }

    public static Map<String, Object> toMap(SandboxProposalPlan plan) {
        return toMap(plan, null);
    }

    public static Map<String, Object> toMap(SandboxProposalPlan plan, DispatchAssignmentPlan assignmentPlan) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (plan == null) {
            map.put("proposalRoutes", new ArrayList<Object>());
            map.put("unassignedStops", new ArrayList<Object>());
            map.put("summary", new LinkedHashMap<String, Object>());
            return map;
        }
        Map<Integer, StopAssignment> planStopByDep = ProposalStopReadModelHelper.indexPlanStops(assignmentPlan);
        map.put("proposalRoutes", toRouteMaps(plan.getProposalRoutes(), planStopByDep));
        map.put("unassignedStops", toStopMaps(plan.getUnassignedStops(), planStopByDep));
        map.put("summary", toSummaryMap(plan.getSummary()));
        return map;
    }

    private static List<Map<String, Object>> toRouteMaps(List<ProposalDriverRoute> routes,
                                                         Map<Integer, StopAssignment> planStopByDep) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        if (routes == null) {
            return rows;
        }
        for (ProposalDriverRoute route : routes) {
            if (route == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("driverUserId", route.getDriverUserId());
            row.put("driverName", route.getDriverName());
            row.put("stopCount", route.getStops() != null ? route.getStops().size() : 0);
            row.put("depFatherIds", collectDepIds(route.getStops()));
            row.put("stops", toStopMaps(route.getStops(), planStopByDep));
            row.put("totalDistanceM", route.getTotalDistanceM());
            row.put("totalDurationS", route.getTotalDurationS());
            row.put("totalDistanceText", route.getTotalDistanceText());
            row.put("totalDurationText", route.getTotalDurationText());
            row.put("hasRouteId", Boolean.FALSE);
            row.put("hasRouteStatus", Boolean.FALSE);
            rows.add(row);
        }
        return rows;
    }

    private static List<Map<String, Object>> toStopMaps(List<ProposalStop> stops,
                                                        Map<Integer, StopAssignment> planStopByDep) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        if (stops == null) {
            return rows;
        }
        int finalSeq = 0;
        for (ProposalStop stop : stops) {
            if (stop == null) {
                continue;
            }
            finalSeq++;
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("finalSeq", finalSeq);
            row.put("depFatherId", stop.getDepFatherId());
            row.put("customerName", stop.getCustomerName());
            row.put("proposalSource", stop.getProposalSource() != null
                    ? stop.getProposalSource().name() : null);
            row.put("historyPreferenceNote", stop.getHistoryPreferenceNote());
            NxDisRouteStopEntity entity = stop.getStop();
            StopAssignment planStop = stop.getDepFatherId() != null && planStopByDep != null
                    ? planStopByDep.get(stop.getDepFatherId()) : null;
            row.put("earliestDeliveryTimeS",
                    ProposalStopReadModelHelper.resolveEarliestDeliveryTimeS(entity, planStop));
            row.put("latestDeliveryTimeS",
                    ProposalStopReadModelHelper.resolveLatestDeliveryTimeS(entity, planStop));
            row.put("timeWindowOverrideFlag",
                    ProposalStopReadModelHelper.resolveTimeWindowOverrideFlag(entity, planStop));
            rows.add(row);
        }
        return rows;
    }

    private static List<Integer> collectDepIds(List<ProposalStop> stops) {
        List<Integer> depIds = new ArrayList<Integer>();
        if (stops == null) {
            return depIds;
        }
        for (ProposalStop stop : stops) {
            if (stop != null && stop.getDepFatherId() != null) {
                depIds.add(stop.getDepFatherId());
            }
        }
        return depIds;
    }

    private static Map<String, Object> toSummaryMap(SandboxProposalPlanSummary summary) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (summary == null) {
            return map;
        }
        map.put("proposalRouteCount", summary.getProposalRouteCount());
        map.put("assignedStopCount", summary.getAssignedStopCount());
        map.put("unassignedStopCount", summary.getUnassignedStopCount());
        map.put("customerStopCount", summary.getCustomerStopCount());
        return map;
    }
}
