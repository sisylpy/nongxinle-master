package com.nongxinle.route;

import com.nongxinle.dto.route.SandboxManualDispatchCustomerContextDto;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageActionsDto;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageCustomerTimeWindowDto;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageDriverDto;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageInsertPositionDto;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageRouteDto;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageSimulationSummaryDto;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageStopCardDto;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageStopImpactDto;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageSystemEtaDto;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageViewModel;
import com.nongxinle.dto.route.SandboxManualDispatchIncomingStopPreviewDto;
import com.nongxinle.dto.route.SandboxManualDispatchInsertOptionDto;
import com.nongxinle.dto.route.SandboxManualDispatchManualTimeConstraintDto;
import com.nongxinle.dto.route.SandboxManualDispatchRequiredArrivalDto;
import com.nongxinle.dto.route.SandboxManualDispatchRouteImpactDto;
import com.nongxinle.dto.route.SandboxManualDispatchRouteSnapshotDto;
import com.nongxinle.dto.route.SandboxManualDispatchSimulateResponse;
import com.nongxinle.dto.route.SandboxManualDispatchStopImpactDto;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteSandboxManualDispatchPanoramaHelper.ManualDispatchPanoramaCapabilities;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 2B：人工路线编辑页 ViewModel 组装（基于 simulate 结果，不写 DB，不重复距离矩阵）。
 */
@Component
public class DisRouteSandboxManualDispatchEditPageBuilder {

    private static final String EDIT_PAGE_PATH =
            "/api/nxdisroutedispatch/sandbox/manual-dispatch/edit-page";
    private static final String SIMULATE_PATH =
            "/api/nxdisroutedispatch/sandbox/manual-dispatch/simulate";
    private static final String CONFIRM_PATH =
            "/api/nxdisroutedispatch/sandbox/manual-dispatch/confirm";

    public SandboxManualDispatchEditPageViewModel build(ManualDispatchSimulateCommand command,
                                                        SandboxManualDispatchSimulateResponse simulate,
                                                        ManualDispatchPanoramaCapabilities capabilities,
                                                        String operationHint) {
        SandboxManualDispatchEditPageViewModel vm = new SandboxManualDispatchEditPageViewModel();
        vm.setDisId(command.getDisId());
        vm.setRouteDate(command.getRouteDate());
        vm.setBatchCode(command.getBatchCode());
        vm.setSimulationId(simulate.getSimulationId());
        vm.setSimulationMode(simulate.getSimulationMode());
        vm.setManualConstraints(simulate.getManualConstraints());
        vm.setCustomer(simulate.getCustomer());
        vm.setIncomingManualTimeConstraint(buildIncomingManualConstraint(command));
        vm.setDriver(buildDriver(command, capabilities, operationHint));
        vm.setRecommendedPositionKey(simulate.getRecommendedOptionKey());
        vm.setSelectedManualStopSeq(command.getManualStopSeq());
        vm.setRequiredArrival(simulate.getRequiredArrival());
        vm.setConfirmMode(simulate.getConfirmMode());
        vm.setRiskHints(simulate.getRiskHints() != null
                ? new ArrayList<String>(simulate.getRiskHints()) : new ArrayList<String>());

        List<NxDisRouteStopEntity> baselineStops = command.getBaselineStops() != null
                ? command.getBaselineStops() : Collections.<NxDisRouteStopEntity>emptyList();
        Date serverNow = new Date();
        Map<Integer, NxDisRouteStopEntity> baselineByDep = indexByDepartment(baselineStops);

        vm.setBaselineRoute(buildBaselineRoute(simulate.getBaseline(), baselineStops, serverNow, command));
        vm.setInsertPositions(buildInsertPositions(
                simulate, baselineStops, baselineByDep, command, serverNow, vm.getBaselineRoute()));
        if (command.getManualStopSeq() != null) {
            vm.setSimulationSummary(buildSimulationSummary(
                    command.getManualStopSeq(), vm.getInsertPositions(), simulate, baselineStops));
        }
        vm.setActions(buildActions());
        applyRouteTimelineAndHints(vm, command, baselineStops, simulate.getCustomer());
        return vm;
    }

    private void applyRouteTimelineAndHints(SandboxManualDispatchEditPageViewModel vm,
                                            ManualDispatchSimulateCommand command,
                                            List<NxDisRouteStopEntity> baselineStops,
                                            SandboxManualDispatchCustomerContextDto customer) {
        Integer manualStopSeq = command.getManualStopSeq();
        boolean hasSelected = manualStopSeq != null;
        vm.setHasSelectedPosition(hasSelected);
        NxDisDriverRouteEntity driverRoute = findPlanDriverRoute(command.getPlan(), command.getDriverUserId());

        if (hasSelected) {
            SandboxManualDispatchEditPageInsertPositionDto selected =
                    findInsertPosition(vm.getInsertPositions(), manualStopSeq);
            vm.setSelectedPositionHint(buildSelectedPositionHint(selected));
            SandboxManualDispatchEditPageRouteDto simulated = selected != null
                    ? selected.getSimulatedRoute() : null;
            vm.setRouteTimeline(DisRouteManualDispatchEditPageTimelineBuilder.buildSimulated(
                    simulated, customer != null ? customer : vm.getCustomer()));
            vm.setBottomHint("已选插入位置，可重新模拟或调整约束");
        } else {
            vm.setSelectedPositionHint(null);
            vm.setRouteTimeline(DisRouteManualDispatchEditPageTimelineBuilder.buildBaselineWithInserts(
                    vm.getBaselineRoute(), vm.getInsertPositions(), baselineStops, driverRoute));
            vm.setBottomHint("请选择插入位置后查看模拟结果");
        }
        vm.setDrawerSubtitle(buildDrawerSubtitle(vm.getCustomer()));
    }

    private static String buildSelectedPositionHint(SandboxManualDispatchEditPageInsertPositionDto selected) {
        if (selected == null) {
            return null;
        }
        if (selected.getAnchorLabel() != null && !selected.getAnchorLabel().trim().isEmpty()) {
            return "已选：" + selected.getAnchorLabel().trim();
        }
        if (selected.getLabel() != null && !selected.getLabel().trim().isEmpty()) {
            return "已选：" + selected.getLabel().trim();
        }
        if (selected.getManualStopSeq() != null) {
            return "已选：第 " + selected.getManualStopSeq() + " 个插入位";
        }
        return null;
    }

    private static String buildDrawerSubtitle(SandboxManualDispatchCustomerContextDto customer) {
        if (customer == null || customer.getCustomerName() == null
                || customer.getCustomerName().trim().isEmpty()) {
            return "编辑送达时间约束";
        }
        return "为「" + customer.getCustomerName().trim() + "」编辑送达时间约束";
    }

    private static SandboxManualDispatchEditPageSimulationSummaryDto buildSimulationSummary(
            Integer manualStopSeq,
            List<SandboxManualDispatchEditPageInsertPositionDto> insertPositions,
            SandboxManualDispatchSimulateResponse simulate,
            List<NxDisRouteStopEntity> baselineStops) {
        SandboxManualDispatchEditPageInsertPositionDto selected =
                findInsertPosition(insertPositions, manualStopSeq);
        SandboxManualDispatchEditPageRouteDto simulatedRoute = selected != null
                ? selected.getSimulatedRoute() : null;

        SandboxManualDispatchEditPageSimulationSummaryDto summary =
                new SandboxManualDispatchEditPageSimulationSummaryDto();
        if (simulatedRoute != null) {
            String distanceText = DisRouteSandboxDisplayFormatHelper.formatDistanceText(
                    simulatedRoute.getTotalDistanceM());
            String durationText = DisRouteSandboxDisplayFormatHelper.formatDurationText(
                    simulatedRoute.getTotalDurationS());
            int stopCount = simulatedRoute.getStops() != null && !simulatedRoute.getStops().isEmpty()
                    ? simulatedRoute.getStops().size()
                    : (baselineStops != null ? baselineStops.size() + 1 : 0);
            summary.setRouteSummary(buildSimulatedRouteSummaryLine(
                    stopCount, simulatedRoute.getRouteSummary(), distanceText, durationText));
            summary.setTotalDistanceText(distanceText);
            summary.setTotalDurationText(durationText);
            summary.setDeltaLine(buildSimulationDeltaLine(
                    simulatedRoute.getTotalDistanceDeltaM(),
                    simulatedRoute.getTotalDurationDeltaS()));
        }
        summary.setRiskHints(collectSimulationRiskHints(selected, simulate, simulatedRoute));
        return summary;
    }

    private static SandboxManualDispatchEditPageInsertPositionDto findInsertPosition(
            List<SandboxManualDispatchEditPageInsertPositionDto> insertPositions,
            Integer manualStopSeq) {
        if (insertPositions == null || manualStopSeq == null) {
            return null;
        }
        for (SandboxManualDispatchEditPageInsertPositionDto position : insertPositions) {
            if (position != null && manualStopSeq.equals(position.getManualStopSeq())) {
                return position;
            }
        }
        return insertPositions.size() == 1 ? insertPositions.get(0) : null;
    }

    private static String buildSimulatedRouteSummaryLine(int stopCount,
                                                         String impactRouteSummary,
                                                         String distanceText,
                                                         String durationText) {
        String metrics = null;
        if (impactRouteSummary != null && !impactRouteSummary.trim().isEmpty()) {
            metrics = impactRouteSummary.trim();
        } else {
            StringBuilder sb = new StringBuilder();
            if (distanceText != null && !distanceText.trim().isEmpty()) {
                sb.append(distanceText.trim());
            }
            if (durationText != null && !durationText.trim().isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" · ");
                }
                sb.append(durationText.trim());
            }
            metrics = sb.length() > 0 ? sb.toString() : null;
        }
        if (metrics == null) {
            return null;
        }
        if (stopCount > 0) {
            return "模拟后：共 " + stopCount + " 站 · " + metrics;
        }
        return "模拟后：" + metrics;
    }

    private static String buildSimulationDeltaLine(Long distanceDeltaM, Long durationDeltaS) {
        String distancePart = formatSignedDistanceDelta(distanceDeltaM);
        String durationPart = formatSignedDurationDelta(durationDeltaS);
        if (distancePart == null && durationPart == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("较原路线 ");
        if (distancePart != null) {
            sb.append(distancePart);
        }
        if (durationPart != null) {
            if (distancePart != null) {
                sb.append(" · ");
            }
            sb.append(durationPart);
        }
        return sb.toString();
    }

    private static String formatSignedDistanceDelta(Long deltaM) {
        if (deltaM == null || deltaM == 0L) {
            return null;
        }
        String text = DisRouteSandboxDisplayFormatHelper.formatDistanceText(Math.abs(deltaM));
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        return (deltaM > 0 ? "+" : "-") + text.trim();
    }

    private static String formatSignedDurationDelta(Long deltaS) {
        if (deltaS == null || deltaS == 0L) {
            return null;
        }
        String text = DisRouteSandboxDisplayFormatHelper.formatDurationText(Math.abs(deltaS));
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        return (deltaS > 0 ? "+" : "-") + text.trim();
    }

    private static List<String> collectSimulationRiskHints(
            SandboxManualDispatchEditPageInsertPositionDto selected,
            SandboxManualDispatchSimulateResponse simulate,
            SandboxManualDispatchEditPageRouteDto simulatedRoute) {
        List<String> hints = new ArrayList<String>();
        if (selected != null && selected.getRiskHints() != null) {
            for (String hint : selected.getRiskHints()) {
                appendUniqueHint(hints, hint);
            }
        }
        if (simulate != null && simulate.getRiskHints() != null) {
            for (String hint : simulate.getRiskHints()) {
                appendUniqueHint(hints, hint);
            }
        }
        if (simulatedRoute != null && simulatedRoute.getStops() != null) {
            for (SandboxManualDispatchEditPageStopCardDto stop : simulatedRoute.getStops()) {
                if (stop == null || stop.getImpact() == null) {
                    continue;
                }
                String windowImpact = stop.getImpact().getTimeWindowImpactLabel();
                if (windowImpact == null || windowImpact.trim().isEmpty()
                        || "符合时间窗".equals(windowImpact.trim())
                        || "未设置时间窗".equals(windowImpact.trim())) {
                    continue;
                }
                appendUniqueHint(hints, windowImpact.trim());
            }
        }
        return hints;
    }

    private static void appendUniqueHint(List<String> hints, String hint) {
        if (hint == null || hint.trim().isEmpty()) {
            return;
        }
        String trimmed = hint.trim();
        if (!hints.contains(trimmed)) {
            hints.add(trimmed);
        }
    }

    private static SandboxManualDispatchEditPageDriverDto buildDriver(
            ManualDispatchSimulateCommand command,
            ManualDispatchPanoramaCapabilities capabilities,
            String operationHint) {
        SandboxManualDispatchEditPageDriverDto driver = new SandboxManualDispatchEditPageDriverDto();
        driver.setDriverUserId(command.getDriverUserId());
        driver.setDriverName(command.getDriverName());
        driver.setDispatchStage(command.getDispatchStage());
        driver.setDispatchStageLabel(ManualDispatchDispatchStage.label(command.getDispatchStage()));
        driver.setDutyStatus(DisDriverDutyStatus.ON_DUTY);
        if (capabilities != null) {
            driver.setCanSimulate(capabilities.isCanSimulate());
            driver.setCanConfirm(capabilities.isCanConfirm());
            driver.setConfirmMode(capabilities.getConfirmMode());
            driver.setConfirmModeLabel(ManualDispatchConfirmMode.label(capabilities.getConfirmMode()));
            driver.setBlockedReason(capabilities.getBlockedReason());
            driver.setRiskHints(new ArrayList<String>(capabilities.getRiskHints()));
        }
        driver.setRouteSummary(null);
        driver.setOperationHint(operationHint);
        return driver;
    }

    private SandboxManualDispatchEditPageRouteDto buildBaselineRoute(
            SandboxManualDispatchRouteSnapshotDto snapshot,
            List<NxDisRouteStopEntity> baselineStops,
            Date serverNow,
            ManualDispatchSimulateCommand command) {
        SandboxManualDispatchEditPageRouteDto route = new SandboxManualDispatchEditPageRouteDto();
        NxDisDriverRouteEntity driverRoute = findPlanDriverRoute(command.getPlan(), command.getDriverUserId());

        Long distanceM = driverRoute != null ? driverRoute.getNxDdrTotalDistanceM() : null;
        Long durationS = driverRoute != null ? driverRoute.getNxDdrTotalDurationS() : null;
        if (distanceM == null && snapshot != null) {
            distanceM = snapshot.getTotalDistanceM();
        }
        if (durationS == null && snapshot != null) {
            durationS = snapshot.getTotalDurationS();
        }
        if (distanceM == null || durationS == null) {
            BaselineLegTotals summed = sumBaselineLegTotals(baselineStops, driverRoute);
            if (distanceM == null) {
                distanceM = summed.distanceM;
            }
            if (durationS == null) {
                durationS = summed.durationS;
            }
        }
        if (baselineStops.isEmpty() && distanceM == null && durationS == null) {
            distanceM = 0L;
            durationS = 0L;
        }
        route.setTotalDistanceM(distanceM);
        route.setTotalDurationS(durationS);
        route.setRouteSummary(buildRouteSummaryFromTotals(distanceM, durationS));

        int seq = 1;
        for (NxDisRouteStopEntity stop : baselineStops) {
            route.getStops().add(buildStopCardFromEntity(stop, seq++, serverNow));
        }
        return route;
    }

    private List<SandboxManualDispatchEditPageInsertPositionDto> buildInsertPositions(
            SandboxManualDispatchSimulateResponse simulate,
            List<NxDisRouteStopEntity> baselineStops,
            Map<Integer, NxDisRouteStopEntity> baselineByDep,
            ManualDispatchSimulateCommand command,
            Date serverNow,
            SandboxManualDispatchEditPageRouteDto baselineRoute) {
        List<SandboxManualDispatchEditPageInsertPositionDto> positions =
                new ArrayList<SandboxManualDispatchEditPageInsertPositionDto>();
        if (simulate.getInsertOptions() == null) {
            return positions;
        }
        int slotCount = baselineStops.size() + 1;
        boolean singleOption = simulate.getInsertOptions().size() == 1;
        for (SandboxManualDispatchInsertOptionDto option : simulate.getInsertOptions()) {
            if (option == null) {
                continue;
            }
            SandboxManualDispatchEditPageInsertPositionDto position =
                    new SandboxManualDispatchEditPageInsertPositionDto();
            int manualStopSeq = option.getInsertStopSeq() != null ? option.getInsertStopSeq() : 1;
            int insertAt = manualStopSeq - 1;
            position.setInsertPositionKey(option.getInsertPositionKey());
            position.setManualStopSeq(manualStopSeq);
            position.setLabel(resolveInsertLabel(manualStopSeq, insertAt, slotCount));
            position.setAnchorLabel(resolveAnchorLabel(insertAt, baselineStops, slotCount));
            position.setRecommended(option.getRecommended());
            position.setManualSeqLocked(option.getManualSeqLocked());
            boolean includeStopCards = singleOption
                    || Boolean.TRUE.equals(option.getRecommended())
                    || Boolean.TRUE.equals(option.getManualSeqLocked());
            position.setSimulatedRoute(buildSimulatedRoute(
                    option, baselineByDep, command, serverNow, includeStopCards, baselineRoute));
            position.setIncomingStop(option.getIncomingStop());
            position.setRequiredArrival(option.getRequiredArrival());
            position.setConfirmMode(option.getConfirmMode());
            position.setRiskHints(option.getRiskHints() != null
                    ? new ArrayList<String>(option.getRiskHints()) : new ArrayList<String>());
            positions.add(position);
        }
        return positions;
    }

    private SandboxManualDispatchEditPageRouteDto buildSimulatedRoute(
            SandboxManualDispatchInsertOptionDto option,
            Map<Integer, NxDisRouteStopEntity> baselineByDep,
            ManualDispatchSimulateCommand command,
            Date serverNow,
            boolean includeStopCards,
            SandboxManualDispatchEditPageRouteDto baselineRoute) {
        SandboxManualDispatchEditPageRouteDto route = new SandboxManualDispatchEditPageRouteDto();
        SandboxManualDispatchRouteImpactDto impact = option.getRouteImpact();
        if (impact != null) {
            route.setRouteSummary(impact.getRouteSummary());
            route.setTotalDistanceM(impact.getTotalDistanceM());
            route.setTotalDurationS(impact.getTotalDurationS());
            route.setReturnToDepotLabel(impact.getReturnToDepotLabel());
            route.setReturnToDepotDurationDeltaS(impact.getReturnToDepotDurationDeltaS());
        }
        applyRouteDeltas(route, baselineRoute);
        if (!includeStopCards || option.getStopImpacts() == null) {
            return route;
        }
        for (SandboxManualDispatchStopImpactDto stopImpact : option.getStopImpacts()) {
            if (stopImpact == null) {
                continue;
            }
            NxDisRouteStopEntity entity = baselineByDep.get(stopImpact.getDepartmentId());
            boolean inserted = Boolean.TRUE.equals(stopImpact.getInsertedStop());
            SandboxManualDispatchEditPageStopCardDto card;
            if (inserted) {
                card = buildInsertedStopCard(stopImpact, command, serverNow);
            } else {
                card = buildStopCardFromEntity(entity, stopImpact.getSeq(), serverNow);
                if (card.getCustomerName() == null) {
                    card.setCustomerName(stopImpact.getCustomerName());
                }
            }
            NxDisRouteStopEntity windowStop = inserted ? command.getIncomingStop() : entity;
            String windowLabel = card.getCustomerTimeWindow() != null
                    ? card.getCustomerTimeWindow().getWindowLabel() : null;
            card.setImpact(buildPageStopImpact(stopImpact, windowStop, windowLabel));
            card.setSystemEta(buildSystemEta(
                    stopImpact.getPlannedArrivalAtAfter(),
                    stopImpact.getPlannedArrivalLabelAfter()));
            route.getStops().add(card);
        }
        return route;
    }

    private SandboxManualDispatchEditPageStopCardDto buildStopCardFromEntity(
            NxDisRouteStopEntity stop,
            Integer seq,
            Date serverNow) {
        SandboxManualDispatchEditPageStopCardDto card = new SandboxManualDispatchEditPageStopCardDto();
        card.setSeq(seq);
        card.setInsertedStop(Boolean.FALSE);
        card.setManualTimeConstraint(emptyManualTimeConstraint());
        if (stop == null) {
            card.setSystemEta(emptySystemEta());
            applyManualConstraintSummary(card);
            return card;
        }
        Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
        card.setDepartmentId(depId);
        card.setSandboxStopKey(stop.getSandboxStopKey() != null
                ? stop.getSandboxStopKey()
                : (depId != null ? DisRouteSandboxStopKeyUtils.build(depId) : null));
        card.setCustomerName(resolveCustomerName(stop));
        card.setCustomerTimeWindow(buildCustomerTimeWindow(stop, serverNow));
        card.setSystemEta(buildSystemEtaFromEntity(stop, serverNow));
        applyManualConstraintSummary(card);
        return card;
    }

    private SandboxManualDispatchEditPageStopCardDto buildInsertedStopCard(
            SandboxManualDispatchStopImpactDto stopImpact,
            ManualDispatchSimulateCommand command,
            Date serverNow) {
        SandboxManualDispatchEditPageStopCardDto card = new SandboxManualDispatchEditPageStopCardDto();
        card.setSeq(stopImpact.getSeq());
        card.setInsertedStop(Boolean.TRUE);
        card.setDepartmentId(stopImpact.getDepartmentId());
        card.setCustomerName(stopImpact.getCustomerName());
        if (stopImpact.getDepartmentId() != null) {
            card.setSandboxStopKey(DisRouteSandboxStopKeyUtils.build(stopImpact.getDepartmentId()));
        }
        NxDisRouteStopEntity incoming = command.getIncomingStop();
        card.setCustomerTimeWindow(buildCustomerTimeWindow(incoming, serverNow));
        card.setManualTimeConstraint(buildIncomingManualConstraint(command));
        card.setSystemEta(buildSystemEta(
                stopImpact.getPlannedArrivalAtAfter(),
                stopImpact.getPlannedArrivalLabelAfter()));
        applyManualConstraintSummary(card);
        return card;
    }

    private static SandboxManualDispatchEditPageStopImpactDto buildPageStopImpact(
            SandboxManualDispatchStopImpactDto stopImpact,
            NxDisRouteStopEntity stopForWindow,
            String windowLabelFromCard) {
        SandboxManualDispatchEditPageStopImpactDto impact = new SandboxManualDispatchEditPageStopImpactDto();
        impact.setPlannedArrivalLabelBefore(stopImpact.getPlannedArrivalLabelBefore());
        impact.setPlannedArrivalLabelAfter(stopImpact.getPlannedArrivalLabelAfter());
        impact.setArrivalDeltaMinutes(stopImpact.getArrivalDeltaMinutes());
        impact.setTimeWindowImpactLabel(resolveEditPageTimeWindowImpactLabel(
                stopImpact.getTimeWindowImpactLabel(), stopForWindow, windowLabelFromCard));
        return impact;
    }

    private static SandboxManualDispatchEditPageSystemEtaDto buildSystemEta(
            String plannedArrivalAt,
            String plannedArrivalLabel) {
        SandboxManualDispatchEditPageSystemEtaDto eta = new SandboxManualDispatchEditPageSystemEtaDto();
        eta.setPlannedArrivalAt(plannedArrivalAt);
        eta.setPlannedArrivalLabel(plannedArrivalLabel);
        return eta;
    }

    private static SandboxManualDispatchEditPageSystemEtaDto buildSystemEtaFromEntity(
            NxDisRouteStopEntity stop,
            Date serverNow) {
        Date arrivalAt = resolvePlannedArrivalAt(stop);
        String arrivalAtText = arrivalAt != null ? RouteDispatchDateFormat.format(arrivalAt) : null;
        String label = arrivalAt != null
                ? DisRouteTemporalHelper.formatDateTimeLabel(arrivalAt, serverNow)
                : DisRouteSandboxTodayStopScheduleHelper.resolvePlannedArrivalLabel(stop, serverNow);
        return buildSystemEta(arrivalAtText, label);
    }

    private static SandboxManualDispatchEditPageSystemEtaDto emptySystemEta() {
        return new SandboxManualDispatchEditPageSystemEtaDto();
    }

    private static SandboxManualDispatchEditPageCustomerTimeWindowDto buildCustomerTimeWindow(
            NxDisRouteStopEntity stop,
            Date serverNow) {
        SandboxManualDispatchEditPageCustomerTimeWindowDto window =
                new SandboxManualDispatchEditPageCustomerTimeWindowDto();
        window.setWindowLabel(DisRouteSandboxTodayStopScheduleHelper.resolveCustomerWindowLabel(
                stop, serverNow));
        return window;
    }

    private static void applyRouteDeltas(SandboxManualDispatchEditPageRouteDto simulated,
                                         SandboxManualDispatchEditPageRouteDto baselineRoute) {
        if (simulated == null || baselineRoute == null) {
            return;
        }
        if (baselineRoute.getTotalDistanceM() == null || baselineRoute.getTotalDurationS() == null
                || simulated.getTotalDistanceM() == null || simulated.getTotalDurationS() == null) {
            simulated.setTotalDistanceDeltaM(null);
            simulated.setTotalDurationDeltaS(null);
            return;
        }
        simulated.setTotalDistanceDeltaM(
                simulated.getTotalDistanceM() - baselineRoute.getTotalDistanceM());
        simulated.setTotalDurationDeltaS(
                simulated.getTotalDurationS() - baselineRoute.getTotalDurationS());
    }

    private static String resolveEditPageTimeWindowImpactLabel(String rawLabel,
                                                               NxDisRouteStopEntity stop,
                                                               String windowLabelFromCard) {
        if (!hasCustomerTimeWindow(stop, windowLabelFromCard)) {
            return "未设置时间窗";
        }
        if (rawLabel != null && !rawLabel.trim().isEmpty()
                && !"未设置时间窗".equals(rawLabel.trim())
                && !"无时间窗约束".equals(rawLabel.trim())) {
            return rawLabel.trim();
        }
        return "符合时间窗";
    }

    private static boolean hasCustomerTimeWindow(NxDisRouteStopEntity stop, String windowLabelFromCard) {
        if (windowLabelFromCard != null && !windowLabelFromCard.trim().isEmpty()) {
            return true;
        }
        if (stop == null) {
            return false;
        }
        String windowLabel = DisRouteSandboxTodayStopScheduleHelper.resolveCustomerWindowLabel(
                stop, new Date());
        return windowLabel != null && !windowLabel.trim().isEmpty();
    }

    private static BaselineLegTotals sumBaselineLegTotals(List<NxDisRouteStopEntity> baselineStops,
                                                           NxDisDriverRouteEntity driverRoute) {
        BaselineLegTotals totals = new BaselineLegTotals();
        if (baselineStops == null || baselineStops.isEmpty()) {
            return totals;
        }
        boolean hasLeg = false;
        for (NxDisRouteStopEntity stop : baselineStops) {
            if (stop == null) {
                continue;
            }
            Long legDist = DisRouteSandboxDisplayFormatHelper.resolveLegDistanceM(stop);
            Long legDur = DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop);
            if (legDist != null && legDist > 0L) {
                totals.distanceM = (totals.distanceM != null ? totals.distanceM : 0L) + legDist;
                hasLeg = true;
            }
            if (legDur != null && legDur > 0L) {
                totals.durationS = (totals.durationS != null ? totals.durationS : 0L) + legDur;
                hasLeg = true;
            }
        }
        if (driverRoute != null) {
            if (driverRoute.getReturnLegDistanceM() != null && driverRoute.getReturnLegDistanceM() > 0L) {
                totals.distanceM = (totals.distanceM != null ? totals.distanceM : 0L)
                        + driverRoute.getReturnLegDistanceM();
                hasLeg = true;
            }
            if (driverRoute.getReturnLegDurationS() != null && driverRoute.getReturnLegDurationS() > 0L) {
                totals.durationS = (totals.durationS != null ? totals.durationS : 0L)
                        + driverRoute.getReturnLegDurationS();
                hasLeg = true;
            }
        }
        if (!hasLeg) {
            totals.distanceM = null;
            totals.durationS = null;
        }
        return totals;
    }

    private static String buildRouteSummaryFromTotals(Long distanceM, Long durationS) {
        if (distanceM == null && durationS == null) {
            return null;
        }
        String distance = DisRouteSandboxDisplayFormatHelper.formatDistanceText(distanceM);
        String duration = DisRouteSandboxDisplayFormatHelper.formatDurationText(durationS);
        if (distance != null && duration != null) {
            return distance + " · " + duration;
        }
        return distance != null ? distance : duration;
    }

    private static final class BaselineLegTotals {
        private Long distanceM;
        private Long durationS;
    }

    private static Date resolvePlannedArrivalAt(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getNxDrsPlannedServiceStartAt() != null) {
            return stop.getNxDrsPlannedServiceStartAt();
        }
        if (stop.getNxDrsPlannedArrivalAt() != null) {
            return stop.getNxDrsPlannedArrivalAt();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && task.getNxDstPlannedArrivalAt() != null) {
            return task.getNxDstPlannedArrivalAt();
        }
        return null;
    }

    private static String buildRouteSummary(NxDisDriverRouteEntity driverRoute) {
        if (driverRoute == null) {
            return null;
        }
        String distance = DisRouteSandboxDisplayFormatHelper.formatDistanceText(
                driverRoute.getNxDdrTotalDistanceM());
        String duration = DisRouteSandboxDisplayFormatHelper.formatDurationText(
                driverRoute.getNxDdrTotalDurationS());
        if (distance != null && duration != null) {
            return distance + " · " + duration;
        }
        return distance != null ? distance : duration;
    }

    private static NxDisDriverRouteEntity findPlanDriverRoute(NxDisRoutePlanEntity plan,
                                                              Integer driverUserId) {
        if (plan == null || driverUserId == null) {
            return null;
        }
        NxDisDriverRouteEntity route = findRouteInList(plan.getDriverRoutes(), driverUserId);
        if (route != null) {
            return route;
        }
        route = findRouteInList(plan.getLoadingDriverRoutes(), driverUserId);
        if (route != null) {
            return route;
        }
        return findRouteInList(plan.getExecutionDriverRoutes(), driverUserId);
    }

    private static NxDisDriverRouteEntity findRouteInList(List<NxDisDriverRouteEntity> routes,
                                                          Integer driverUserId) {
        if (routes == null || driverUserId == null) {
            return null;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route != null && driverUserId.equals(route.getNxDdrDriverUserId())) {
                return route;
            }
        }
        return null;
    }

    private static SandboxManualDispatchManualTimeConstraintDto emptyManualTimeConstraint() {
        SandboxManualDispatchManualTimeConstraintDto constraint = new SandboxManualDispatchManualTimeConstraintDto();
        constraint.setManualArrivalSpecified(Boolean.FALSE);
        constraint.setAllowLate(null);
        return constraint;
    }

    private static SandboxManualDispatchManualTimeConstraintDto buildIncomingManualConstraint(
            ManualDispatchSimulateCommand command) {
        SandboxManualDispatchManualTimeConstraintDto constraint = emptyManualTimeConstraint();
        if (command.getRequiredLatestArrivalAt() != null
                && !command.getRequiredLatestArrivalAt().trim().isEmpty()) {
            constraint.setManualArrivalSpecified(Boolean.TRUE);
            constraint.setRequiredLatestArrivalAt(command.getRequiredLatestArrivalAt().trim());
            try {
                Date deadline = RouteDispatchDateFormat.parse(command.getRequiredLatestArrivalAt().trim());
                constraint.setRequiredLatestArrivalLabel(
                        DisRouteTemporalHelper.formatDateTimeLabel(deadline, new Date()));
            } catch (Exception ignored) {
                constraint.setRequiredLatestArrivalLabel(command.getRequiredLatestArrivalAt().trim());
            }
        }
        return DisRouteManualTimeConstraintHelper.enrich(constraint);
    }

    private static void applyManualConstraintSummary(SandboxManualDispatchEditPageStopCardDto card) {
        if (card == null) {
            return;
        }
        SandboxManualDispatchManualTimeConstraintDto constraint = card.getManualTimeConstraint();
        if (constraint == null) {
            constraint = DisRouteManualTimeConstraintHelper.empty();
            card.setManualTimeConstraint(constraint);
        } else {
            DisRouteManualTimeConstraintHelper.enrich(constraint);
        }
        card.setManualConstraintSummary(constraint.getSummaryLabel());
    }

    private static SandboxManualDispatchEditPageActionsDto buildActions() {
        SandboxManualDispatchEditPageActionsDto actions = new SandboxManualDispatchEditPageActionsDto();
        actions.setEditPagePath(EDIT_PAGE_PATH);
        actions.setSimulatePath(SIMULATE_PATH);
        actions.setConfirmPath(CONFIRM_PATH);
        actions.setConfirmEnabled(Boolean.FALSE);
        return actions;
    }

    private static Map<Integer, NxDisRouteStopEntity> indexByDepartment(List<NxDisRouteStopEntity> stops) {
        Map<Integer, NxDisRouteStopEntity> index = new HashMap<Integer, NxDisRouteStopEntity>();
        if (stops == null) {
            return index;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            if (depId != null) {
                index.put(depId, stop);
            }
        }
        return index;
    }

    static String resolveInsertLabel(int manualStopSeq, int insertAt, int slotCount) {
        if (insertAt == 0) {
            return "插到第 1 个送";
        }
        if (insertAt == slotCount - 1) {
            return "插到最后送";
        }
        return "插到第 " + manualStopSeq + " 个送";
    }

    static String resolveAnchorLabel(int insertAt,
                                     List<NxDisRouteStopEntity> baselineStops,
                                     int slotCount) {
        if (insertAt <= 0 || insertAt >= slotCount - 1) {
            return null;
        }
        if (baselineStops == null || insertAt >= baselineStops.size()) {
            return null;
        }
        return "插到" + shopOrdinalLabel(insertAt + 1) + "前面";
    }

    private static String shopOrdinalLabel(int shopIndex) {
        String[] ordinals = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        if (shopIndex >= 1 && shopIndex <= 10) {
            return ordinals[shopIndex] + "号店";
        }
        return "第" + shopIndex + "个店";
    }

    private static String resolveCustomerName(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getNxDrsDepartmentName() != null && !stop.getNxDrsDepartmentName().trim().isEmpty()) {
            return stop.getNxDrsDepartmentName().trim();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && task.getNxDstDepName() != null && !task.getNxDstDepName().trim().isEmpty()) {
            return task.getNxDstDepName().trim();
        }
        Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
        return depId != null ? "客户 " + depId : null;
    }
}
