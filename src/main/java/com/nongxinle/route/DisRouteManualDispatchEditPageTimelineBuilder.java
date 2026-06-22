package com.nongxinle.route;

import com.nongxinle.dto.route.SandboxManualDispatchCustomerContextDto;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageInsertPositionDto;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageRouteDto;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageStopCardDto;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 人工路线编辑页 routeTimeline[] 组装（前端只渲染，不再从 stops 自行拼装）。 */
public final class DisRouteManualDispatchEditPageTimelineBuilder {

    private DisRouteManualDispatchEditPageTimelineBuilder() {
    }

    public static List<Map<String, Object>> buildBaselineWithInserts(
            SandboxManualDispatchEditPageRouteDto baselineRoute,
            List<SandboxManualDispatchEditPageInsertPositionDto> insertPositions,
            List<NxDisRouteStopEntity> baselineStops,
            NxDisDriverRouteEntity driverRoute) {
        List<Map<String, Object>> timeline = new ArrayList<Map<String, Object>>();
        timeline.add(buildStartNode(driverRoute));

        List<SandboxManualDispatchEditPageStopCardDto> stopCards = baselineRoute != null
                && baselineRoute.getStops() != null
                ? baselineRoute.getStops() : new ArrayList<SandboxManualDispatchEditPageStopCardDto>();
        int slotCount = stopCards.size() + 1;
        Map<Integer, SandboxManualDispatchEditPageInsertPositionDto> positionBySeq =
                indexInsertPositions(insertPositions);

        for (int i = 0; i < stopCards.size(); i++) {
            timeline.add(buildInsertNode(i + 1, positionBySeq.get(i + 1)));
            NxDisRouteStopEntity entity = baselineStops != null && i < baselineStops.size()
                    ? baselineStops.get(i) : null;
            appendLegFromEntity(timeline, entity);
            timeline.add(buildStopNode(stopCards.get(i), null));
        }
        timeline.add(buildInsertNode(slotCount, positionBySeq.get(slotCount)));
        appendReturnLeg(timeline, driverRoute, baselineRoute);
        timeline.add(buildEndNode(driverRoute, baselineRoute, null));
        return timeline;
    }

    public static List<Map<String, Object>> buildSimulated(
            SandboxManualDispatchEditPageRouteDto simulatedRoute,
            SandboxManualDispatchCustomerContextDto customer) {
        List<Map<String, Object>> timeline = new ArrayList<Map<String, Object>>();
        timeline.add(buildStartNode(null));

        List<SandboxManualDispatchEditPageStopCardDto> stopCards = simulatedRoute != null
                && simulatedRoute.getStops() != null
                ? simulatedRoute.getStops() : new ArrayList<SandboxManualDispatchEditPageStopCardDto>();
        for (SandboxManualDispatchEditPageStopCardDto stopCard : stopCards) {
            appendLegPlaceholder(timeline);
            String goodsSummary = Boolean.TRUE.equals(stopCard != null ? stopCard.getInsertedStop() : null)
                    && customer != null ? customer.getGoodsSummary() : null;
            timeline.add(buildStopNode(stopCard, goodsSummary));
        }

        appendReturnLegFromRoute(timeline, simulatedRoute);
        timeline.add(buildEndNode(null, simulatedRoute, simulatedRoute));
        return timeline;
    }

    private static Map<Integer, SandboxManualDispatchEditPageInsertPositionDto> indexInsertPositions(
            List<SandboxManualDispatchEditPageInsertPositionDto> insertPositions) {
        Map<Integer, SandboxManualDispatchEditPageInsertPositionDto> index =
                new HashMap<Integer, SandboxManualDispatchEditPageInsertPositionDto>();
        if (insertPositions == null) {
            return index;
        }
        for (SandboxManualDispatchEditPageInsertPositionDto position : insertPositions) {
            if (position != null && position.getManualStopSeq() != null) {
                index.put(position.getManualStopSeq(), position);
            }
        }
        return index;
    }

    private static Map<String, Object> buildStartNode(NxDisDriverRouteEntity driverRoute) {
        Map<String, Object> start = new LinkedHashMap<String, Object>();
        start.put("type", "start");
        start.put("name", DisRouteSandboxTodayTimelineBuilder.DEPOT_NAME);
        start.put("timeRight", resolveStartTimeRight(driverRoute));
        return start;
    }

    private static Map<String, Object> buildEndNode(NxDisDriverRouteEntity driverRoute,
                                                    SandboxManualDispatchEditPageRouteDto route,
                                                    SandboxManualDispatchEditPageRouteDto simulatedRoute) {
        Map<String, Object> end = new LinkedHashMap<String, Object>();
        end.put("type", "end");
        end.put("name", DisRouteSandboxTodayTimelineBuilder.RETURN_NAME);
        end.put("timeRight", resolveEndTimeRight(driverRoute, route, simulatedRoute));
        return end;
    }

    private static Map<String, Object> buildInsertNode(
            int manualStopSeq,
            SandboxManualDispatchEditPageInsertPositionDto position) {
        Map<String, Object> insert = new LinkedHashMap<String, Object>();
        insert.put("type", "insert");
        insert.put("manualStopSeq", manualStopSeq);
        String label = position != null && position.getLabel() != null && !position.getLabel().trim().isEmpty()
                ? position.getLabel().trim()
                : "插到第 " + manualStopSeq + " 个送";
        insert.put("buttonLabel", label);
        return insert;
    }

    private static Map<String, Object> buildStopNode(SandboxManualDispatchEditPageStopCardDto card,
                                                     String goodsSummaryOverride) {
        Map<String, Object> stop = new LinkedHashMap<String, Object>();
        stop.put("type", "stop");
        if (card == null) {
            return stop;
        }
        stop.put("seq", card.getSeq());
        stop.put("customerName", card.getCustomerName());
        stop.put("insertedStop", Boolean.TRUE.equals(card.getInsertedStop()));
        if (goodsSummaryOverride != null && !goodsSummaryOverride.trim().isEmpty()) {
            stop.put("goodsSummary", goodsSummaryOverride.trim());
        }
        if (card.getCustomerTimeWindow() != null
                && card.getCustomerTimeWindow().getWindowLabel() != null
                && !card.getCustomerTimeWindow().getWindowLabel().trim().isEmpty()) {
            stop.put("windowLabel", card.getCustomerTimeWindow().getWindowLabel().trim());
        }
        if (card.getSystemEta() != null && card.getSystemEta().getPlannedArrivalLabel() != null
                && !card.getSystemEta().getPlannedArrivalLabel().trim().isEmpty()) {
            stop.put("plannedArrivalLabel", card.getSystemEta().getPlannedArrivalLabel().trim());
        } else if (card.getImpact() != null && card.getImpact().getPlannedArrivalLabelAfter() != null
                && !card.getImpact().getPlannedArrivalLabelAfter().trim().isEmpty()) {
            stop.put("plannedArrivalLabel", card.getImpact().getPlannedArrivalLabelAfter().trim());
        }
        if (card.getImpact() != null && card.getImpact().getTimeWindowImpactLabel() != null
                && !card.getImpact().getTimeWindowImpactLabel().trim().isEmpty()) {
            stop.put("timeWindowImpactLabel", card.getImpact().getTimeWindowImpactLabel().trim());
        }
        if (card.getManualConstraintSummary() != null && !card.getManualConstraintSummary().trim().isEmpty()) {
            stop.put("manualConstraintSummary", card.getManualConstraintSummary().trim());
        }
        if (Boolean.TRUE.equals(card.getInsertedStop()) && card.getSeq() != null) {
            stop.put("insertSeqLabel", "插入为第 " + card.getSeq() + " 站");
        }
        return stop;
    }

    private static void appendLegFromEntity(List<Map<String, Object>> timeline, NxDisRouteStopEntity stop) {
        if (stop == null) {
            return;
        }
        Long distanceM = DisRouteSandboxDisplayFormatHelper.resolveLegDistanceM(stop);
        Long durationS = DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop);
        appendLeg(timeline, distanceM, durationS);
    }

    private static void appendReturnLeg(List<Map<String, Object>> timeline,
                                        NxDisDriverRouteEntity driverRoute,
                                        SandboxManualDispatchEditPageRouteDto baselineRoute) {
        if (driverRoute != null) {
            appendLeg(timeline, driverRoute.getReturnLegDistanceM(), driverRoute.getReturnLegDurationS());
            return;
        }
        if (baselineRoute != null && baselineRoute.getReturnToDepotLabel() != null
                && !baselineRoute.getReturnToDepotLabel().trim().isEmpty()) {
            appendLegText(timeline, baselineRoute.getReturnToDepotLabel().trim());
        }
    }

    private static void appendReturnLegFromRoute(List<Map<String, Object>> timeline,
                                                 SandboxManualDispatchEditPageRouteDto simulatedRoute) {
        if (simulatedRoute == null) {
            return;
        }
        if (simulatedRoute.getReturnToDepotLabel() != null
                && !simulatedRoute.getReturnToDepotLabel().trim().isEmpty()) {
            appendLegText(timeline, simulatedRoute.getReturnToDepotLabel().trim());
            return;
        }
        appendLeg(timeline, null, null);
    }

    private static void appendLegPlaceholder(List<Map<String, Object>> timeline) {
        appendLeg(timeline, null, null);
    }

    private static void appendLeg(List<Map<String, Object>> timeline, Long distanceM, Long durationS) {
        String distanceText = DisRouteSandboxDisplayFormatHelper.formatDistanceText(distanceM);
        String durationText = DisRouteSandboxDisplayFormatHelper.formatDurationText(durationS);
        if (distanceText == null && durationText == null) {
            return;
        }
        appendLegText(timeline, joinLegText(distanceText, durationText));
    }

    private static void appendLegText(List<Map<String, Object>> timeline, String legText) {
        if (legText == null || legText.trim().isEmpty()) {
            return;
        }
        Map<String, Object> leg = new LinkedHashMap<String, Object>();
        leg.put("type", "leg");
        leg.put("legText", legText.trim());
        timeline.add(leg);
    }

    private static String resolveStartTimeRight(NxDisDriverRouteEntity driverRoute) {
        if (driverRoute != null && driverRoute.getPlannedDepartLabel() != null
                && !driverRoute.getPlannedDepartLabel().trim().isEmpty()) {
            return driverRoute.getPlannedDepartLabel().trim();
        }
        return "现在可送";
    }

    private static String resolveEndTimeRight(NxDisDriverRouteEntity driverRoute,
                                              SandboxManualDispatchEditPageRouteDto route,
                                              SandboxManualDispatchEditPageRouteDto simulatedRoute) {
        if (simulatedRoute != null && simulatedRoute.getReturnToDepotLabel() != null
                && !simulatedRoute.getReturnToDepotLabel().trim().isEmpty()) {
            return simulatedRoute.getReturnToDepotLabel().trim();
        }
        if (driverRoute != null && driverRoute.getPlannedReturnLabel() != null
                && !driverRoute.getPlannedReturnLabel().trim().isEmpty()) {
            return driverRoute.getPlannedReturnLabel().trim();
        }
        if (driverRoute != null && driverRoute.getPlannedFinishLabel() != null
                && !driverRoute.getPlannedFinishLabel().trim().isEmpty()) {
            return driverRoute.getPlannedFinishLabel().trim();
        }
        String durationText = DisRouteSandboxDisplayFormatHelper.formatDurationText(
                route != null ? route.getTotalDurationS() : null);
        if (durationText != null && !durationText.trim().isEmpty()) {
            return "约 " + durationText.trim() + " 后 返回";
        }
        return "预计返回";
    }

    private static String joinLegText(String distanceText, String durationText) {
        if (distanceText == null && durationText == null) {
            return null;
        }
        if (distanceText != null && durationText != null) {
            return distanceText + " · " + durationText;
        }
        return distanceText != null ? distanceText : durationText;
    }
}
