package com.nongxinle.route.proposal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** debug：{@code GET /sandbox/today} → proposalPlan。 */
public final class SandboxProposalPlanReadModelAssembler {

    private SandboxProposalPlanReadModelAssembler() {
    }

    public static Map<String, Object> toMap(SandboxProposalPlan plan) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (plan == null) {
            map.put("proposalRoutes", new ArrayList<Object>());
            map.put("unassignedStops", new ArrayList<Object>());
            map.put("summary", new LinkedHashMap<String, Object>());
            return map;
        }
        map.put("proposalRoutes", toRouteMaps(plan.getProposalRoutes()));
        map.put("unassignedStops", toStopMaps(plan.getUnassignedStops()));
        map.put("summary", toSummaryMap(plan.getSummary()));
        return map;
    }

    private static List<Map<String, Object>> toRouteMaps(List<ProposalDriverRoute> routes) {
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
            row.put("stops", toStopMaps(route.getStops()));
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

    private static List<Map<String, Object>> toStopMaps(List<ProposalStop> stops) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        if (stops == null) {
            return rows;
        }
        for (ProposalStop stop : stops) {
            if (stop == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("depFatherId", stop.getDepFatherId());
            row.put("customerName", stop.getCustomerName());
            row.put("proposalSource", stop.getProposalSource() != null
                    ? stop.getProposalSource().name() : null);
            row.put("historyPreferenceNote", stop.getHistoryPreferenceNote());
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
