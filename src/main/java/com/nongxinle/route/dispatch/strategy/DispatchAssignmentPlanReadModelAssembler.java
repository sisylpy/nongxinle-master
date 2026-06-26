package com.nongxinle.route.dispatch.strategy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** debug 序列化：{@code GET /sandbox/today} → dispatchAssignmentPlan。 */
public final class DispatchAssignmentPlanReadModelAssembler {

    private DispatchAssignmentPlanReadModelAssembler() {
    }

    public static Map<String, Object> toMap(DispatchAssignmentPlan plan) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (plan == null) {
            return map;
        }
        map.put("disId", plan.getDisId());
        map.put("routeDate", plan.getRouteDate());
        map.put("batchCode", plan.getBatchCode());
        if (plan.getStrategyMode() != null) {
            map.put("strategyMode", plan.getStrategyMode().name());
        }
        if (plan.getPlanningPhase() != null) {
            map.put("planningPhase", plan.getPlanningPhase().name());
        }
        map.put("resolvedAt", plan.getResolvedAt());
        map.put("warnings", plan.getWarnings() != null
                ? new ArrayList<String>(plan.getWarnings()) : new ArrayList<String>());
        map.put("historyBoundStopCount", plan.getHistoryBoundStopCount());
        map.put("fallbackStopCount", plan.getFallbackStopCount());
        map.put("frozenStopCount", plan.getFrozenStopCount());
        map.put("driverRoutes", toDriverRoutes(plan.getDriverRoutes()));
        map.put("fallbackStops", toFallbackStops(plan.getFallbackStops()));
        map.put("frozenStops", toFrozenStops(plan.getFrozenStops()));
        map.put("finalDriverRoutes", toFinalDriverRoutes(plan.getFinalDriverRoutes()));
        return map;
    }

    private static List<Map<String, Object>> toFinalDriverRoutes(List<FinalSequencedDriverRouteDebug> routes) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (routes == null) {
            return list;
        }
        for (FinalSequencedDriverRouteDebug route : routes) {
            if (route == null) {
                continue;
            }
            Map<String, Object> routeMap = new LinkedHashMap<String, Object>();
            routeMap.put("driverUserId", route.getDriverUserId());
            routeMap.put("driverName", route.getDriverName());
            routeMap.put("suggestedDepartTimeS", route.getSuggestedDepartTimeS());
            routeMap.put("plannedDepartTimeS", route.getPlannedDepartTimeS());
            routeMap.put("suggestedDepartReason", route.getSuggestedDepartReason());
            routeMap.put("suggestedDepartReasonLabel", route.getSuggestedDepartReasonLabel());
            routeMap.put("stops", toFinalStops(route.getStops()));
            list.add(routeMap);
        }
        return list;
    }

    private static List<Map<String, Object>> toFinalStops(List<FinalSequencedStopDebug> stops) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (stops == null) {
            return list;
        }
        for (FinalSequencedStopDebug stop : stops) {
            if (stop == null) {
                continue;
            }
            Map<String, Object> stopMap = new LinkedHashMap<String, Object>();
            stopMap.put("seq", stop.getSeq());
            stopMap.put("depFatherId", stop.getDepFatherId());
            stopMap.put("customerName", stop.getCustomerName());
            stopMap.put("earliestDeliveryTimeS", stop.getEarliestDeliveryTimeS());
            stopMap.put("latestDeliveryTimeS", stop.getLatestDeliveryTimeS());
            stopMap.put("projectedArrivalTimeS", stop.getProjectedArrivalTimeS());
            stopMap.put("timeWindowStatus", stop.getTimeWindowStatus());
            stopMap.put("lateMinutes", stop.getLateMinutes());
            stopMap.put("waitMinutes", stop.getWaitMinutes());
            stopMap.put("warningLabel", stop.getWarningLabel());
            stopMap.put("windowSource", stop.getWindowSource());
            if (stop.getSequenceBucket() != null) {
                stopMap.put("sequenceBucket", stop.getSequenceBucket().name());
            }
            stopMap.put("sequenceReason", stop.getSequenceReason());
            stopMap.put("sequenceSortKey", stop.getSequenceSortKey());
            list.add(stopMap);
        }
        return list;
    }

    private static List<Map<String, Object>> toFrozenStops(List<FrozenStopAssignment> frozenStops) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (frozenStops == null) {
            return list;
        }
        for (FrozenStopAssignment frozen : frozenStops) {
            if (frozen == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("depFatherId", frozen.getDepFatherId());
            map.put("sandboxStopKey", frozen.getSandboxStopKey());
            if (frozen.getPlanningReason() != null) {
                map.put("planningReason", frozen.getPlanningReason().name());
            }
            map.put("frozenReason", frozen.getFrozenReason());
            list.add(map);
        }
        return list;
    }

    private static List<Map<String, Object>> toFallbackStops(List<FallbackStopAssignment> fallbackStops) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (fallbackStops == null) {
            return list;
        }
        for (FallbackStopAssignment fallback : fallbackStops) {
            if (fallback == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("depFatherId", fallback.getDepFatherId());
            map.put("sandboxStopKey", fallback.getSandboxStopKey());
            if (fallback.getPlanningReason() != null) {
                map.put("planningReason", fallback.getPlanningReason().name());
            }
            map.put("historyReason", fallback.getHistoryReason());
            list.add(map);
        }
        return list;
    }

    private static List<Map<String, Object>> toDriverRoutes(List<DriverRoutePlan> routes) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (routes == null) {
            return list;
        }
        for (DriverRoutePlan route : routes) {
            if (route == null) {
                continue;
            }
            Map<String, Object> routeMap = new LinkedHashMap<String, Object>();
            routeMap.put("driverUserId", route.getDriverUserId());
            routeMap.put("driverName", route.getDriverName());
            routeMap.put("routeScheduleMode", route.getRouteScheduleMode());
            routeMap.put("suggestedDepartTimeS", route.getSuggestedDepartTimeS());
            routeMap.put("plannedDepartTimeS", route.getPlannedDepartTimeS());
            routeMap.put("suggestedDepartReason", route.getSuggestedDepartReason());
            routeMap.put("suggestedDepartReasonLabel", route.getSuggestedDepartReasonLabel());
            routeMap.put("stops", toStops(route.getStops()));
            list.add(routeMap);
        }
        return list;
    }

    private static List<Map<String, Object>> toStops(List<StopAssignment> stops) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (stops == null) {
            return list;
        }
        for (StopAssignment stop : stops) {
            if (stop == null) {
                continue;
            }
            Map<String, Object> stopMap = new LinkedHashMap<String, Object>();
            stopMap.put("depFatherId", stop.getDepFatherId());
            stopMap.put("sandboxStopKey", stop.getSandboxStopKey());
            stopMap.put("assignedDriverUserId", stop.getAssignedDriverUserId());
            stopMap.put("stopSeq", stop.getStopSeq());
            if (stop.getStopClass() != null) {
                stopMap.put("stopClass", stop.getStopClass().name());
            }
            if (stop.getFeasibility() != null) {
                stopMap.put("feasibility", stop.getFeasibility().name());
            }
            stopMap.put("earliestDeliveryTimeS", stop.getEarliestDeliveryTimeS());
            stopMap.put("latestDeliveryTimeS", stop.getLatestDeliveryTimeS());
            stopMap.put("serviceMinutes", stop.getServiceMinutes());
            stopMap.put("timeWindowOverrideFlag", stop.getTimeWindowOverrideFlag());
            stopMap.put("preferredDriverUserId", stop.getPreferredDriverUserId());
            stopMap.put("historyAvgStopSeq", stop.getHistoryAvgStopSeq());
            if (stop.getPlanningReason() != null) {
                stopMap.put("planningReason", stop.getPlanningReason().name());
            }
            stopMap.put("projectedArrivalTimeS", stop.getProjectedArrivalTimeS());
            stopMap.put("timeWindowStatus", stop.getTimeWindowStatus());
            stopMap.put("lateMinutes", stop.getLateMinutes());
            stopMap.put("waitMinutes", stop.getWaitMinutes());
            stopMap.put("warningLabel", stop.getWarningLabel());
            list.add(stopMap);
        }
        return list;
    }
}
