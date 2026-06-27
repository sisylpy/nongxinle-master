package com.nongxinle.route;

import com.nongxinle.dto.route.DispatchDriverCardDto;
import com.nongxinle.dto.route.DispatchStoreCardDto;
import com.nongxinle.dto.route.DriverDispatchCandidateDto;
import com.nongxinle.dto.route.SandboxManualDispatchManualTimeConstraintDto;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteSandboxManualDispatchPanoramaHelper.DriverStopCounts;
import com.nongxinle.route.DisRouteSandboxManualDispatchPanoramaHelper.ManualDispatchPanoramaCapabilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 派单系统统一分店卡 / 司机卡模板组装。
 * 各页面通过此 Builder 保证字段口径一致。
 */
public final class DisRouteDispatchCardTemplateBuilder {

    private static final String DISPATCH_STATUS_UNASSIGNED = "未分配";

    private DisRouteDispatchCardTemplateBuilder() {
    }

    public static DispatchStoreCardDto buildStoreCardFromStop(NxDisRouteStopEntity stop,
                                                              Date serverNow,
                                                              String sandboxStopKey,
                                                              String dispatchStatusLabel,
                                                              SandboxManualDispatchManualTimeConstraintDto manualTimeConstraint) {
        DispatchStoreCardDto card = new DispatchStoreCardDto();
        if (stop == null) {
            card.setManualTimeConstraint(emptyManualTimeConstraint());
            return card;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        Integer departmentId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
        card.setDepartmentId(departmentId);
        card.setSandboxStopKey(sandboxStopKey != null ? sandboxStopKey
                : (departmentId != null ? DisRouteSandboxStopKeyUtils.build(departmentId) : stop.getSandboxStopKey()));
        card.setCustomerName(resolveCustomerName(stop));
        card.setGoodsSummary(DisRouteSandboxDisplayFormatHelper.buildGoodsSummary(task));
        card.setDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(
                DisRouteSandboxDisplayFormatHelper.resolveLegDistanceM(stop)));
        card.setDurationText(DisRouteSandboxDisplayFormatHelper.formatDurationText(
                DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop)));
        card.setPlannedArrivalLabel(
                DisRouteSandboxTodayStopScheduleHelper.resolvePlannedArrivalLabel(stop, serverNow));
        card.setPlannedDepartureLabel(
                DisRouteSandboxTodayStopScheduleHelper.resolvePlannedDepartureLabel(stop, serverNow));
        card.setCustomerWindowLabel(
                DisRouteSandboxTodayStopScheduleHelper.resolveCustomerWindowLabel(stop, serverNow));
        card.setServiceDurationLabel(
                DisRouteSandboxTodayStopScheduleHelper.resolveServiceDurationLabel(stop));
        card.setDispatchStatusLabel(dispatchStatusLabel != null ? dispatchStatusLabel : resolveStopDispatchStatusLabel(stop, task));
        card.setManualTimeConstraint(manualTimeConstraint != null
                ? DisRouteManualTimeConstraintHelper.enrich(manualTimeConstraint)
                : DisRouteManualTimeConstraintHelper.empty());
        return card;
    }

    public static DispatchStoreCardDto buildUnassignedStoreCard(NxDisRouteStopEntity stop,
                                                              Date serverNow,
                                                              String sandboxStopKey) {
        return buildStoreCardFromStop(stop, serverNow, sandboxStopKey, DISPATCH_STATUS_UNASSIGNED, null);
    }

    public static DispatchDriverCardDto buildDriverCard(DriverDispatchCandidateDto driverMeta,
                                                        NxDisDriverRouteEntity route,
                                                        String dispatchStage,
                                                        ManualDispatchPanoramaCapabilities capabilities,
                                                        DriverStopCounts stopCounts,
                                                        String operationHint,
                                                        Map<String, Object> primaryAction) {
        DispatchDriverCardDto card = new DispatchDriverCardDto();
        if (driverMeta != null) {
            card.setDriverUserId(driverMeta.getDriverUserId());
            card.setDriverName(driverMeta.getDriverName());
            card.setDriverAvatarUrl(driverMeta.getDriverAvatarUrl());
            card.setDutyStatus(driverMeta.getDutyStatus());
            card.setDutyStatusLabel(DisRouteDispatchLabels.label(driverMeta.getDutyStatus()));
        }
        if (card.getDutyStatus() == null) {
            card.setDutyStatus(DisDriverDutyStatus.ON_DUTY);
            card.setDutyStatusLabel(DisRouteDispatchLabels.label(DisDriverDutyStatus.ON_DUTY));
        }
        card.setDispatchStage(dispatchStage);
        card.setDispatchStageLabel(ManualDispatchDispatchStage.label(dispatchStage));

        if (route != null) {
            card.setPlannedDepartAt(formatDateTime(route.getNxDdrPlannedDepartAt()));
            card.setPlannedDepartLabel(firstNonBlank(
                    route.getPlannedDepartLabel(),
                    formatDateTimeLabel(route.getNxDdrPlannedDepartAt())));
            Date returnAt = route.getPlannedReturnAt() != null
                    ? route.getPlannedReturnAt() : route.getNxDdrPlannedFinishAt();
            card.setPlannedReturnAt(formatDateTime(returnAt));
            card.setPlannedReturnLabel(firstNonBlank(
                    route.getPlannedReturnLabel(),
                    route.getPlannedFinishLabel(),
                    formatDateTimeLabel(returnAt)));
        }

        RouteTotals totals = resolveRouteTotals(route);
        card.setTotalDistanceM(totals.distanceM);
        card.setTotalDurationS(totals.durationS);
        card.setTotalDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(totals.distanceM));
        card.setTotalDurationText(DisRouteSandboxDisplayFormatHelper.formatDurationText(totals.durationS));

        if (stopCounts != null) {
            card.setCurrentStopCount(stopCounts.getCurrentStopCount());
            card.setCompletedStopCount(stopCounts.getCompletedStopCount());
            card.setPendingStopCount(stopCounts.getPendingStopCount());
        }
        card.setRouteSummary(DisRouteSandboxManualDispatchPanoramaHelper.buildRouteSummary(
                route, stopCounts, dispatchStage));

        if (capabilities != null) {
            applyManualDispatchPresentation(card, dispatchStage, stopCounts);
        }

        if (capabilities != null) {
            card.setCanSimulate(capabilities.isCanSimulate());
            card.setCanConfirm(capabilities.isCanConfirm());
            card.setConfirmMode(capabilities.getConfirmMode());
            card.setConfirmModeLabel(ManualDispatchConfirmMode.label(capabilities.getConfirmMode()));
            card.setBlockedReason(capabilities.getBlockedReason());
            card.setRiskHints(new java.util.ArrayList<String>(capabilities.getRiskHints()));
        }
        card.setOperationHint(operationHint);
        card.setPrimaryAction(primaryAction);
        applyPresentationAliases(card);
        return card;
    }

    public static DispatchDriverCardDto buildDriverDutyCard(DriverDispatchCandidateDto driverMeta,
                                                            NxDisDriverRouteEntity route,
                                                            String dispatchStage,
                                                            DriverStopCounts stopCounts,
                                                            boolean canToggleDuty,
                                                            String toggleDisabledReason,
                                                            Map<String, Object> toggleAction,
                                                            String operationHint) {
        List<String> riskHints = new ArrayList<String>(
                DisRouteDriverDutyToggleHelper.buildDutyRiskHints(dispatchStage));
        DispatchDriverCardDto card = buildDriverCard(
                driverMeta, route, dispatchStage, null, stopCounts, operationHint, toggleAction);
        card.setDispatchStageLabel(ManualDispatchDispatchStage.todayDutyCardStageLabel(dispatchStage));
        card.setCanToggleDuty(canToggleDuty);
        card.setToggleDisabledReason(toggleDisabledReason);
        card.setRiskHints(riskHints);
        applyPresentationAliases(card);
        return card;
    }

    /** 配送任务页 executionDriverRoutes：统一司机卡。 */
    public static DispatchDriverCardDto buildDeliveryExecutionDriverCard(
            NxDisDriverRouteEntity route,
            DriverDispatchCandidateDto driverMeta) {
        String dispatchStage = resolveExecutionDispatchStage(route);
        DriverStopCounts stopCounts = resolveExecutionStopCounts(route);
        String operationHint = buildDeliveryExecutionOperationHint(dispatchStage, stopCounts);
        if (driverMeta != null && driverMeta.getDriverName() != null
                && DisRouteDispatchDriverNameHelper.isPlaceholderDriverName(
                        driverMeta.getDriverName(), driverMeta.getDriverUserId())) {
            driverMeta.setDriverName(DisRouteDispatchDriverNameHelper.resolveRouteDriverName(
                    driverMeta.getDriverName(), driverMeta.getDriverUserId(), driverMeta));
        }
        DispatchDriverCardDto card = buildDriverCard(
                driverMeta, route, dispatchStage, null, stopCounts, operationHint, null);
        if (route != null) {
            if (card.getDriverUserId() == null) {
                card.setDriverUserId(route.getNxDdrDriverUserId());
            }
            card.setDriverName(DisRouteDispatchDriverNameHelper.resolveRouteDriverName(route, driverMeta));
        }
        card.setRiskHints(new ArrayList<String>());
        applyPresentationAliases(card);
        return card;
    }

    private static String resolveExecutionDispatchStage(NxDisDriverRouteEntity route) {
        if (route == null) {
            return ManualDispatchDispatchStage.EXECUTION;
        }
        String status = DisRouteRouteExecutionHelper.resolveRouteStatus(route);
        if (status != null) {
            String normalized = status.trim().toUpperCase();
            if (DisRouteDriverRouteStatus.COMPLETED.equals(normalized)
                    || DisRouteDriverRouteStatus.DELIVERED.equals(normalized)
                    || DisRouteDriverRouteStatus.CLOSED.equals(normalized)) {
                return ManualDispatchDispatchStage.COMPLETED;
            }
        }
        return ManualDispatchDispatchStage.EXECUTION;
    }

    private static DriverStopCounts resolveExecutionStopCounts(NxDisDriverRouteEntity route) {
        int total = 0;
        int completed = 0;
        if (route != null && route.getStops() != null) {
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop == null) {
                    continue;
                }
                total++;
                if (isDeliveredExecutionStop(stop)) {
                    completed++;
                }
            }
        } else if (route != null && route.getTotalStopCount() != null && route.getTotalStopCount() > 0) {
            total = route.getTotalStopCount();
            if (route.getConfirmedStopCount() != null) {
                completed = Math.max(0, total - route.getConfirmedStopCount());
            }
        }
        int pending = Math.max(0, total - completed);
        return new DriverStopCounts(total, completed, pending);
    }

    private static boolean isDeliveredExecutionStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task == null || task.getNxDstStatus() == null) {
            return false;
        }
        String status = task.getNxDstStatus().trim().toUpperCase();
        return DisShipmentTaskStatus.DELIVERED.equals(status)
                || DisShipmentTaskStatus.CLOSED.equals(status);
    }

    private static String buildDeliveryExecutionOperationHint(String dispatchStage,
                                                              DriverStopCounts stopCounts) {
        if (ManualDispatchDispatchStage.COMPLETED.equals(dispatchStage)) {
            return "今日路线已完成";
        }
        if (stopCounts != null && stopCounts.getPendingStopCount() > 0) {
            return "配送中，待送 " + stopCounts.getPendingStopCount() + " 站";
        }
        return "配送中";
    }

    private static void applyPresentationAliases(DispatchDriverCardDto card) {
        if (card == null) {
            return;
        }
        card.setPlannedDepartureAt(card.getPlannedDepartAt());
        card.setPlannedDepartureLabel(card.getPlannedDepartLabel());
        card.setEstimatedReturnAt(card.getPlannedReturnAt());
        card.setEstimatedReturnLabel(card.getPlannedReturnLabel());
        card.setCustomerCount(card.getCurrentStopCount());
    }

    private static void applyManualDispatchPresentation(DispatchDriverCardDto card,
                                                          String dispatchStage,
                                                          DriverStopCounts stopCounts) {
        if (card == null) {
            return;
        }
        NxDisDriverRouteEntity routeProxy = new NxDisDriverRouteEntity();
        routeProxy.setNxDdrTotalDistanceM(card.getTotalDistanceM());
        routeProxy.setNxDdrTotalDurationS(card.getTotalDurationS());
        routeProxy.setPlannedDepartLabel(card.getPlannedDepartLabel());
        routeProxy.setPlannedReturnLabel(card.getPlannedReturnLabel());
        routeProxy.setPlannedReturnAt(parseDateTime(card.getPlannedReturnAt()));
        routeProxy.setNxDdrPlannedDepartAt(parseDateTime(card.getPlannedDepartAt()));
        routeProxy.setNxDdrPlannedFinishAt(parseDateTime(card.getPlannedReturnAt()));

        card.setHeadline(DisRouteSandboxManualDispatchPanoramaHelper.buildHeadline(routeProxy, dispatchStage));
        card.setMetricsLine(DisRouteSandboxManualDispatchPanoramaHelper.buildMetricsLine(routeProxy));
        card.setCurrentTaskLine(DisRouteSandboxManualDispatchPanoramaHelper.buildCurrentTaskLine(
                stopCounts, dispatchStage));
        card.setDutyBadgeTone("ok");
        card.setStageBadgeTone(DisRouteSandboxManualDispatchPanoramaHelper.resolveStageBadgeTone(dispatchStage));
        card.setHintTone(DisRouteSandboxManualDispatchPanoramaHelper.resolveHintTone(
                dispatchStage, card.getRiskHints()));
        if (card.getBlockedReason() != null && !card.getBlockedReason().trim().isEmpty()) {
            card.setDutyBadgeTone("muted");
            card.setStageBadgeTone("muted");
        }
    }

    private static java.util.Date parseDateTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return com.nongxinle.route.RouteDispatchDateFormat.parse(text.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    public static SandboxManualDispatchManualTimeConstraintDto emptyManualTimeConstraint() {
        return DisRouteManualTimeConstraintHelper.empty();
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

    private static String resolveStopDispatchStatusLabel(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        if (task != null && task.getNxDstStatus() != null) {
            String label = DisRouteDispatchLabels.label(task.getNxDstStatus());
            if (label != null) {
                return label;
            }
        }
        return DISPATCH_STATUS_UNASSIGNED;
    }

    private static RouteTotals resolveRouteTotals(NxDisDriverRouteEntity route) {
        Long distanceM = route != null ? route.getNxDdrTotalDistanceM() : null;
        Long durationS = route != null ? route.getNxDdrTotalDurationS() : null;
        if (route == null) {
            return new RouteTotals(distanceM, durationS);
        }
        if (distanceM != null && durationS != null) {
            return new RouteTotals(distanceM, durationS);
        }
        long legDistanceM = 0L;
        long legDurationS = 0L;
        boolean hasLeg = false;
        List<NxDisRouteStopEntity> stops = route.getStops();
        if (stops != null) {
            for (NxDisRouteStopEntity stop : stops) {
                if (stop == null) {
                    continue;
                }
                Long legDist = DisRouteSandboxDisplayFormatHelper.resolveLegDistanceM(stop);
                Long legDur = DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop);
                if (legDist != null && legDist > 0L) {
                    legDistanceM += legDist;
                    hasLeg = true;
                }
                if (legDur != null && legDur > 0L) {
                    legDurationS += legDur;
                    hasLeg = true;
                }
            }
        }
        if (route.getReturnLegDistanceM() != null && route.getReturnLegDistanceM() > 0L) {
            legDistanceM += route.getReturnLegDistanceM();
            hasLeg = true;
        }
        if (route.getReturnLegDurationS() != null && route.getReturnLegDurationS() > 0L) {
            legDurationS += route.getReturnLegDurationS();
            hasLeg = true;
        }
        if (hasLeg) {
            if (distanceM == null) {
                distanceM = legDistanceM;
            }
            if (durationS == null) {
                durationS = legDurationS;
            }
        }
        return new RouteTotals(distanceM, durationS);
    }

    private static String formatDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return RouteDispatchDateFormat.format(date);
    }

    private static String formatDateTimeLabel(Date date) {
        if (date == null) {
            return null;
        }
        return DisRouteTemporalHelper.formatDateTimeLabel(date, new Date());
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static final class RouteTotals {
        private final Long distanceM;
        private final Long durationS;

        private RouteTotals(Long distanceM, Long durationS) {
            this.distanceM = distanceM;
            this.durationS = durationS;
        }
    }
}
