package com.nongxinle.route;

import com.nongxinle.dto.route.SandboxManualDispatchConstraintsDto;
import com.nongxinle.dto.route.SandboxManualDispatchCustomerContextDto;
import com.nongxinle.dto.route.SandboxManualDispatchDriverSummaryDto;
import com.nongxinle.dto.route.SandboxManualDispatchIncomingStopPreviewDto;
import com.nongxinle.dto.route.SandboxManualDispatchInsertOptionDto;
import com.nongxinle.dto.route.SandboxManualDispatchRequiredArrivalDto;
import com.nongxinle.dto.route.SandboxManualDispatchRouteImpactDto;
import com.nongxinle.dto.route.SandboxManualDispatchRouteSnapshotDto;
import com.nongxinle.dto.route.SandboxManualDispatchSimulateResponse;
import com.nongxinle.dto.route.SandboxManualDispatchStopImpactDto;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.model.GeoPoint;
import com.nongxinle.service.impl.DisRouteSandboxLegMetricsHelper;
import com.nongxinle.service.impl.DisRouteSandboxSchedulePreviewHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.nongxinle.route.RouteCoordinateUtils.isValidCoordinate;
import static com.nongxinle.route.RouteCoordinateUtils.toPoint;

/**
 * Phase 2B-2：人工调度插单模拟（纯内存，不写 DB）。
 */
@Component
public class DisRouteSandboxManualDispatchSimulator {

    public static final String INSERT_FIRST = "FIRST";
    public static final String INSERT_SECOND = "SECOND";
    public static final String INSERT_LAST = "LAST";
    public static final String INSERT_RECOMMENDED = "RECOMMENDED";
    public static final String INSERT_LOCKED = "LOCKED";
    public static final String INSERT_POSITION_PREFIX = "POSITION_";

    private static final int MAX_FULL_OPTIONS = 6;

    @Autowired
    private DisRouteSandboxLegMetricsHelper disRouteSandboxLegMetricsHelper;
    @Autowired
    private DisRouteSandboxSchedulePreviewHelper disRouteSandboxSchedulePreviewHelper;

    public SandboxManualDispatchSimulateResponse simulate(ManualDispatchSimulateCommand command) throws IOException {
        SandboxManualDispatchSimulateResponse response = new SandboxManualDispatchSimulateResponse();
        response.setSimulationId(buildSimulationId());
        response.setManualConstraints(buildConstraints(command));

        NxDisRouteStopEntity incomingStop = normalizeIncomingStop(command.getIncomingStop(), command.getDepartmentId());
        response.setCustomer(buildCustomer(command, incomingStop));
        response.setDriver(buildDriverSummary(command));

        List<NxDisRouteStopEntity> base = command.getBaselineStops() != null
                ? command.getBaselineStops() : Collections.<NxDisRouteStopEntity>emptyList();
        RoutePreviewSnapshot baseline = previewRoute(cloneStops(base), command.getPlan(), command.getRouteDate());
        overlayBaselineTotalsFromPlan(baseline, command.getPlan(), command.getDriverUserId());
        response.setBaseline(toRouteSnapshotDto(baseline, base.size()));

        boolean seqLocked = command.getManualStopSeq() != null;
        response.setSimulationMode(seqLocked
                ? ManualDispatchSimulationMode.SINGLE_LOCKED_SEQ
                : ManualDispatchSimulationMode.MULTI_INSERT_OPTIONS);

        int slotCount = base.size() + 1;
        List<Integer> insertPositions = resolveInsertPositions(seqLocked, command.getManualStopSeq(), slotCount);
        Date requiredDeadline = parseRequiredDeadline(command.getRequiredLatestArrivalAt());

        List<SandboxManualDispatchInsertOptionDto> options = new ArrayList<SandboxManualDispatchInsertOptionDto>();
        SandboxManualDispatchInsertOptionDto best = null;
        Date serverNow = new Date();

        for (int insertAt : insertPositions) {
            List<NxDisRouteStopEntity> trialStops = cloneStops(base);
            trialStops.add(insertAt, cloneStop(incomingStop));
            resequence(trialStops);
            RoutePreviewSnapshot trial = previewRoute(trialStops, command.getPlan(), command.getRouteDate());
            SandboxManualDispatchInsertOptionDto option = buildInsertOption(
                    insertAt, slotCount, seqLocked, baseline, trial, incomingStop,
                    requiredDeadline, serverNow);
            options.add(option);
            if (best == null || compareTrial(best, option) > 0) {
                best = option;
            }
        }

        if (!seqLocked && best != null) {
            markRecommended(options, best.getInsertStopSeq());
        }
        applyDispatchStageRisks(options, command.getDispatchStage());
        response.setInsertOptions(options);
        SandboxManualDispatchInsertOptionDto primary = seqLocked
                ? (options.isEmpty() ? null : options.get(0))
                : findRecommended(options, best);
        if (primary != null) {
            response.setRecommendedOptionKey(primary.getInsertPositionKey());
            response.setConfirmMode(primary.getConfirmMode());
            response.setRiskHints(new ArrayList<String>(primary.getRiskHints()));
            if (primary.getRequiredArrival() != null) {
                response.setRequiredArrival(primary.getRequiredArrival());
            }
        } else {
            response.setConfirmMode(ManualDispatchConfirmMode.FORBIDDEN);
        }
        return response;
    }

    private static void applyDispatchStageRisks(List<SandboxManualDispatchInsertOptionDto> options,
                                                String dispatchStage) {
        if (options == null || options.isEmpty()) {
            return;
        }
        DisRouteSandboxManualDispatchPanoramaHelper.ManualDispatchPanoramaCapabilities capabilities =
                DisRouteSandboxManualDispatchPanoramaHelper.resolveCapabilities(dispatchStage);
        List<String> stageHints = capabilities.getRiskHints();
        if (stageHints == null || stageHints.isEmpty()) {
            return;
        }
        for (SandboxManualDispatchInsertOptionDto option : options) {
            if (option == null) {
                continue;
            }
            List<String> merged = new ArrayList<String>(stageHints);
            if (option.getRiskHints() != null && !option.getRiskHints().isEmpty()) {
                for (String hint : option.getRiskHints()) {
                    if (hint != null && !hint.trim().isEmpty() && !merged.contains(hint)) {
                        merged.add(hint);
                    }
                }
            }
            option.setRiskHints(merged);
            if (ManualDispatchConfirmMode.RISK_ACK.equals(capabilities.getConfirmMode())) {
                option.setConfirmMode(ManualDispatchConfirmMode.RISK_ACK);
            }
        }
    }

    private static NxDisRouteStopEntity normalizeIncomingStop(NxDisRouteStopEntity incomingStop, Integer departmentId) {
        if (incomingStop == null) {
            return null;
        }
        Integer resolvedDepId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(incomingStop);
        if (resolvedDepId == null) {
            resolvedDepId = departmentId;
        }
        if (resolvedDepId != null && incomingStop.getNxDrsDepartmentId() == null) {
            incomingStop.setNxDrsDepartmentId(resolvedDepId);
        }
        if ((incomingStop.getNxDrsDepartmentName() == null || incomingStop.getNxDrsDepartmentName().trim().isEmpty())
                && incomingStop.getShipmentTask() != null
                && incomingStop.getShipmentTask().getNxDstDepName() != null) {
            incomingStop.setNxDrsDepartmentName(incomingStop.getShipmentTask().getNxDstDepName().trim());
        }
        return incomingStop;
    }

    private static SandboxManualDispatchConstraintsDto buildConstraints(ManualDispatchSimulateCommand command) {
        SandboxManualDispatchConstraintsDto constraints = new SandboxManualDispatchConstraintsDto();
        constraints.setManualDriverLocked(command.getDriverUserId() != null);
        constraints.setManualSeqLocked(command.getManualStopSeq() != null);
        constraints.setRequiredArrivalLocked(hasText(command.getRequiredLatestArrivalAt()));
        constraints.setManualStopSeq(command.getManualStopSeq());
        constraints.setRequiredLatestArrivalAt(command.getRequiredLatestArrivalAt());
        return constraints;
    }

    private static SandboxManualDispatchCustomerContextDto buildCustomer(
            ManualDispatchSimulateCommand command, NxDisRouteStopEntity incomingStop) {
        SandboxManualDispatchCustomerContextDto customer = new SandboxManualDispatchCustomerContextDto();
        Integer depId = command.getDepartmentId();
        if (depId == null && incomingStop != null) {
            depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(incomingStop);
        }
        customer.setDepartmentId(depId);
        customer.setSandboxStopKey(command.getSandboxStopKey());
        if (customer.getSandboxStopKey() == null && depId != null) {
            customer.setSandboxStopKey(DisRouteSandboxStopKeyUtils.build(depId));
        }
        customer.setCustomerName(resolveCustomerName(incomingStop));
        NxDisShipmentTaskEntity task = incomingStop != null ? incomingStop.getShipmentTask() : null;
        customer.setGoodsSummary(DisRouteSandboxDisplayFormatHelper.buildGoodsSummary(task));
        return customer;
    }

    private static SandboxManualDispatchDriverSummaryDto buildDriverSummary(ManualDispatchSimulateCommand command) {
        SandboxManualDispatchDriverSummaryDto driver = new SandboxManualDispatchDriverSummaryDto();
        driver.setDriverUserId(command.getDriverUserId());
        driver.setDriverName(command.getDriverName());
        driver.setDispatchStage(command.getDispatchStage());
        driver.setDispatchStageLabel(ManualDispatchDispatchStage.label(command.getDispatchStage()));
        return driver;
    }

    private static SandboxManualDispatchRouteSnapshotDto toRouteSnapshotDto(RoutePreviewSnapshot snapshot,
                                                                            int stopCount) {
        SandboxManualDispatchRouteSnapshotDto dto = new SandboxManualDispatchRouteSnapshotDto();
        if (snapshot == null) {
            dto.setStopCount(stopCount);
            return dto;
        }
        dto.setStopCount(stopCount);
        dto.setTotalDistanceM(snapshot.totalDistanceM);
        dto.setTotalDurationS(snapshot.totalDurationS);
        dto.setReturnToDepotDurationS(snapshot.returnDurationS);
        dto.setRouteSummary(formatRouteSummary(snapshot));
        return dto;
    }

    private static List<Integer> resolveInsertPositions(boolean seqLocked,
                                                        Integer manualStopSeq,
                                                        int slotCount) {
        if (slotCount <= 0) {
            throw new IllegalArgumentException("当前路线无法插入客户");
        }
        if (seqLocked) {
            if (manualStopSeq == null || manualStopSeq < 1 || manualStopSeq > slotCount) {
                throw new IllegalArgumentException("manualStopSeq 必须在 1.." + slotCount + " 之间");
            }
            List<Integer> locked = new ArrayList<Integer>();
            locked.add(manualStopSeq - 1);
            return locked;
        }
        if (slotCount <= MAX_FULL_OPTIONS) {
            List<Integer> all = new ArrayList<Integer>();
            for (int i = 0; i < slotCount; i++) {
                all.add(i);
            }
            return all;
        }
        Set<Integer> positions = new LinkedHashSet<Integer>();
        positions.add(0);
        if (slotCount > 2) {
            positions.add(1);
        }
        positions.add(slotCount - 1);
        return new ArrayList<Integer>(positions);
    }

    private RoutePreviewSnapshot previewRoute(List<NxDisRouteStopEntity> stops,
                                              NxDisRoutePlanEntity plan,
                                              String routeDate) throws IOException {
        RoutePreviewSnapshot snapshot = new RoutePreviewSnapshot();
        if (stops == null || stops.isEmpty()) {
            return snapshot;
        }
        NxDisDriverRouteEntity route = new NxDisDriverRouteEntity();
        route.setStops(stops);
        GeoPoint depot = resolveDepot(plan);
        if (allStopsHaveLegMetrics(stops)) {
            applyRouteTotalsFromExistingLegs(route, snapshot);
            disRouteSandboxSchedulePreviewHelper.applySchedulePreview(plan, route, routeDate);
        } else {
            disRouteSandboxLegMetricsHelper.applyToDriverRoute(depot, route);
            disRouteSandboxSchedulePreviewHelper.applySchedulePreview(plan, route, routeDate);
        }
        snapshot.totalDistanceM = route.getNxDdrTotalDistanceM();
        snapshot.totalDurationS = route.getNxDdrTotalDurationS();
        snapshot.returnDurationS = route.getReturnLegDurationS() != null ? route.getReturnLegDurationS() : 0L;
        snapshot.stops = new ArrayList<StopScheduleSnapshot>();
        Date serverNow = new Date();
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            StopScheduleSnapshot item = new StopScheduleSnapshot();
            item.departmentId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            item.customerName = resolveCustomerName(stop);
            item.plannedArrivalAt = resolvePlannedArrivalAt(stop);
            item.plannedArrivalLabel = DisRouteSandboxTodayStopScheduleHelper.resolvePlannedArrivalLabel(
                    stop, serverNow);
            item.timeWindowLabel = DisRouteSandboxTodayStopScheduleHelper.resolveCustomerWindowLabel(
                    stop, serverNow);
            item.waitMinutes = stop.getNxDrsWaitMinutes() != null ? stop.getNxDrsWaitMinutes() : 0;
            item.timeWindowStatus = stop.getNxDrsTimeWindowStatus();
            item.lateMinutes = stop.getNxDrsLateMinutes() != null ? stop.getNxDrsLateMinutes() : 0;
            snapshot.stops.add(item);
        }
        return snapshot;
    }

    private SandboxManualDispatchInsertOptionDto buildInsertOption(int insertAt,
                                                                   int slotCount,
                                                                   boolean seqLocked,
                                                                   RoutePreviewSnapshot baseline,
                                                                   RoutePreviewSnapshot trial,
                                                                   NxDisRouteStopEntity incomingStop,
                                                                   Date requiredDeadline,
                                                                   Date serverNow) {
        SandboxManualDispatchInsertOptionDto option = new SandboxManualDispatchInsertOptionDto();
        int stopSeq = insertAt + 1;
        option.setInsertStopSeq(stopSeq);
        option.setManualSeqLocked(seqLocked);
        if (seqLocked) {
            option.setInsertPositionKey(INSERT_LOCKED);
            option.setLabel("第 " + stopSeq + " 个送（人工锁定）");
        } else {
            option.setInsertPositionKey(resolveInsertPositionKey(insertAt, slotCount));
            option.setLabel(resolveInsertLabel(insertAt, slotCount));
        }
        option.setRecommended(Boolean.FALSE);
        option.setRouteImpact(buildRouteImpact(baseline, trial));
        option.setStopImpacts(buildStopImpacts(baseline, trial, incomingStop));

        Integer incomingDepId = incomingStop != null
                ? DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(incomingStop) : null;
        StopScheduleSnapshot incomingSnapshot = resolveIncomingSnapshot(trial, insertAt, incomingDepId);
        option.setIncomingStop(buildIncomingStopPreview(
                stopSeq, incomingSnapshot, incomingDepId, incomingStop, serverNow));
        if (requiredDeadline != null) {
            option.setRequiredArrival(buildRequiredArrival(requiredDeadline, incomingSnapshot, serverNow));
        }
        applyConfirmModeAndRisks(option);
        return option;
    }

    private static StopScheduleSnapshot resolveIncomingSnapshot(RoutePreviewSnapshot trial,
                                                                 int insertAt,
                                                                 Integer incomingDepId) {
        if (trial != null && trial.stops != null && insertAt >= 0 && insertAt < trial.stops.size()) {
            return trial.stops.get(insertAt);
        }
        return findStop(trial, incomingDepId);
    }

    private static SandboxManualDispatchRouteImpactDto buildRouteImpact(RoutePreviewSnapshot baseline,
                                                                      RoutePreviewSnapshot trial) {
        SandboxManualDispatchRouteImpactDto impact = new SandboxManualDispatchRouteImpactDto();
        impact.setTotalDistanceM(trial.totalDistanceM);
        impact.setTotalDurationS(trial.totalDurationS);
        if (baseline.totalDistanceM == null || baseline.totalDurationS == null
                || trial.totalDistanceM == null || trial.totalDurationS == null) {
            impact.setTotalDistanceDeltaM(null);
            impact.setTotalDurationDeltaS(null);
        } else {
            impact.setTotalDistanceDeltaM(trial.totalDistanceM - baseline.totalDistanceM);
            impact.setTotalDurationDeltaS(trial.totalDurationS - baseline.totalDurationS);
        }
        impact.setReturnToDepotDurationS(trial.returnDurationS);
        impact.setReturnToDepotDurationDeltaS(trial.returnDurationS - baseline.returnDurationS);
        impact.setReturnToDepotLabel(DisRouteSandboxDisplayFormatHelper.formatDurationText(trial.returnDurationS));
        impact.setRouteSummary(formatRouteSummary(trial));
        return impact;
    }

    private static SandboxManualDispatchIncomingStopPreviewDto buildIncomingStopPreview(
            int stopSeq,
            StopScheduleSnapshot snapshot,
            Integer departmentId,
            NxDisRouteStopEntity incomingStop,
            Date serverNow) {
        SandboxManualDispatchIncomingStopPreviewDto preview = new SandboxManualDispatchIncomingStopPreviewDto();
        preview.setSeq(stopSeq);
        preview.setDepartmentId(departmentId);
        preview.setCustomerName(snapshot != null ? snapshot.customerName : resolveCustomerName(incomingStop));
        if (snapshot != null) {
            preview.setPlannedArrivalLabel(snapshot.plannedArrivalLabel);
            preview.setPlannedArrivalAt(formatDateTime(snapshot.plannedArrivalAt));
        }
        return preview;
    }

    private static SandboxManualDispatchRequiredArrivalDto buildRequiredArrival(Date deadline,
                                                                                StopScheduleSnapshot incoming,
                                                                                Date serverNow) {
        SandboxManualDispatchRequiredArrivalDto dto = new SandboxManualDispatchRequiredArrivalDto();
        dto.setDeadlineAt(formatDateTime(deadline));
        dto.setDeadlineLabel(DisRouteTemporalHelper.formatDateTimeLabel(deadline, serverNow));
        if (incoming != null) {
            dto.setPlannedArrivalAt(formatDateTime(incoming.plannedArrivalAt));
            dto.setPlannedArrivalLabel(incoming.plannedArrivalLabel);
        }
        if (incoming == null || incoming.plannedArrivalAt == null) {
            dto.setFeasible(Boolean.FALSE);
            return dto;
        }
        long diffMinutes = Math.round((deadline.getTime() - incoming.plannedArrivalAt.getTime()) / 60000.0);
        if (diffMinutes >= 0) {
            dto.setFeasible(Boolean.TRUE);
            dto.setSlackMinutes((int) diffMinutes);
        } else {
            dto.setFeasible(Boolean.FALSE);
            dto.setViolationMinutes((int) Math.abs(diffMinutes));
        }
        return dto;
    }

    private static void applyConfirmModeAndRisks(SandboxManualDispatchInsertOptionDto option) {
        List<String> hints = new ArrayList<String>();
        String confirmMode = ManualDispatchConfirmMode.DIRECT;

        if (option.getRequiredArrival() != null
                && Boolean.FALSE.equals(option.getRequiredArrival().getFeasible())) {
            confirmMode = ManualDispatchConfirmMode.RISK_ACK;
            Integer violation = option.getRequiredArrival().getViolationMinutes();
            if (violation != null && violation > 0) {
                hints.add("预计到达时间晚于必须送达时间 " + violation + " 分钟");
            } else if (option.getRequiredArrival().getPlannedArrivalAt() == null) {
                hints.add("无法估算预计到达时间，必须送达时间可行性未确认");
            } else {
                hints.add("预计到达时间晚于必须送达时间");
            }
            hints.add("确认后仍将保留您指定的顺序与必须送达要求");
        }

        int delayedStops = 0;
        int lateWindowStops = 0;
        if (option.getStopImpacts() != null) {
            for (SandboxManualDispatchStopImpactDto impact : option.getStopImpacts()) {
                if (impact == null || Boolean.TRUE.equals(impact.getInsertedStop())) {
                    continue;
                }
                Integer delta = impact.getArrivalDeltaMinutes();
                if (delta != null && delta > 0) {
                    delayedStops++;
                }
                String windowLabel = impact.getTimeWindowImpactLabel();
                if (windowLabel != null && windowLabel.contains("晚于时间窗")) {
                    lateWindowStops++;
                }
            }
        }
        if (delayedStops > 0) {
            confirmMode = ManualDispatchConfirmMode.RISK_ACK;
            hints.add("插入后将有 " + delayedStops + " 个客户预计到达时间延后");
        }
        if (lateWindowStops > 0) {
            confirmMode = ManualDispatchConfirmMode.RISK_ACK;
            hints.add("插入后将有 " + lateWindowStops + " 个客户可能超出时间窗");
        }

        option.setConfirmMode(confirmMode);
        option.setRiskHints(hints);
    }

    private List<SandboxManualDispatchStopImpactDto> buildStopImpacts(RoutePreviewSnapshot baseline,
                                                                    RoutePreviewSnapshot trial,
                                                                    NxDisRouteStopEntity incomingStop) {
        List<SandboxManualDispatchStopImpactDto> impacts = new ArrayList<SandboxManualDispatchStopImpactDto>();
        Integer incomingDepId = incomingStop != null
                ? DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(incomingStop) : null;
        int seq = 1;
        for (StopScheduleSnapshot after : trial.stops) {
            SandboxManualDispatchStopImpactDto impact = new SandboxManualDispatchStopImpactDto();
            impact.setSeq(seq++);
            impact.setDepartmentId(after.departmentId);
            impact.setCustomerName(after.customerName);
            impact.setPlannedArrivalLabelAfter(after.plannedArrivalLabel);
            impact.setPlannedArrivalAtAfter(formatDateTime(after.plannedArrivalAt));
            impact.setInsertedStop(incomingDepId != null && incomingDepId.equals(after.departmentId));
            StopScheduleSnapshot before = findStop(baseline, after.departmentId);
            if (before != null) {
                impact.setPlannedArrivalLabelBefore(before.plannedArrivalLabel);
                impact.setPlannedArrivalAtBefore(formatDateTime(before.plannedArrivalAt));
                impact.setArrivalDeltaMinutes(estimateDeltaMinutes(
                        before.plannedArrivalAt, after.plannedArrivalAt));
            }
            impact.setTimeWindowImpactLabel(resolveTimeWindowImpact(after));
            impacts.add(impact);
        }
        return impacts;
    }

    private static StopScheduleSnapshot findStop(RoutePreviewSnapshot snapshot, Integer departmentId) {
        if (snapshot == null || snapshot.stops == null || departmentId == null) {
            return null;
        }
        for (StopScheduleSnapshot stop : snapshot.stops) {
            if (departmentId.equals(stop.departmentId)) {
                return stop;
            }
        }
        return null;
    }

    private static String resolveTimeWindowImpact(StopScheduleSnapshot stop) {
        if (stop == null) {
            return null;
        }
        boolean hasWindow = stop.timeWindowLabel != null && !stop.timeWindowLabel.trim().isEmpty();
        if (!hasWindow) {
            return "未设置时间窗";
        }
        if (stop.lateMinutes > 0) {
            return "晚于时间窗 " + stop.lateMinutes + " 分钟";
        }
        if (stop.waitMinutes > 0) {
            return "早于时间窗 " + stop.waitMinutes + " 分钟";
        }
        if (DisRouteStopTimeWindowStatus.SUPPLEMENT_AFTER_WINDOW.equals(stop.timeWindowStatus)) {
            return "超出常规窗口";
        }
        return "符合时间窗";
    }

    private static void overlayBaselineTotalsFromPlan(RoutePreviewSnapshot snapshot,
                                                      NxDisRoutePlanEntity plan,
                                                      Integer driverUserId) {
        if (snapshot == null || plan == null || driverUserId == null) {
            return;
        }
        NxDisDriverRouteEntity driverRoute = findPlanDriverRoute(plan, driverUserId);
        if (driverRoute == null) {
            return;
        }
        if (driverRoute.getNxDdrTotalDistanceM() != null) {
            snapshot.totalDistanceM = driverRoute.getNxDdrTotalDistanceM();
        }
        if (driverRoute.getNxDdrTotalDurationS() != null) {
            snapshot.totalDurationS = driverRoute.getNxDdrTotalDurationS();
        }
        if (snapshot.totalDistanceM == null || snapshot.totalDurationS == null) {
            long legDistanceM = 0L;
            long legDurationS = 0L;
            boolean hasLeg = false;
            if (driverRoute.getStops() != null) {
                for (NxDisRouteStopEntity stop : driverRoute.getStops()) {
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
            if (driverRoute.getReturnLegDistanceM() != null && driverRoute.getReturnLegDistanceM() > 0L) {
                legDistanceM += driverRoute.getReturnLegDistanceM();
                hasLeg = true;
            }
            if (driverRoute.getReturnLegDurationS() != null && driverRoute.getReturnLegDurationS() > 0L) {
                legDurationS += driverRoute.getReturnLegDurationS();
                hasLeg = true;
            }
            if (hasLeg) {
                if (snapshot.totalDistanceM == null) {
                    snapshot.totalDistanceM = legDistanceM;
                }
                if (snapshot.totalDurationS == null) {
                    snapshot.totalDurationS = legDurationS;
                }
            }
        }
        if (driverRoute.getReturnLegDurationS() != null) {
            snapshot.returnDurationS = driverRoute.getReturnLegDurationS();
        }
    }

    private static NxDisDriverRouteEntity findPlanDriverRoute(NxDisRoutePlanEntity plan,
                                                            Integer driverUserId) {
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

    private static Integer estimateDeltaMinutes(Date beforeAt, Date afterAt) {
        if (beforeAt == null || afterAt == null) {
            return null;
        }
        long delta = Math.round((afterAt.getTime() - beforeAt.getTime()) / 60000.0);
        if (delta == 0) {
            return 0;
        }
        return (int) delta;
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

    private static Date parseRequiredDeadline(String text) {
        if (!hasText(text)) {
            return null;
        }
        try {
            return RouteDispatchDateFormat.parse(text.trim());
        } catch (ParseException ex) {
            throw new IllegalArgumentException("requiredLatestArrivalAt 格式无效，请使用 yyyy-MM-dd HH:mm:ss");
        }
    }

    private static String formatDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return RouteDispatchDateFormat.format(date);
    }

    private static String buildSimulationId() {
        return "md-sim-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private static SandboxManualDispatchInsertOptionDto findRecommended(
            List<SandboxManualDispatchInsertOptionDto> options,
            SandboxManualDispatchInsertOptionDto fallback) {
        if (options == null) {
            return fallback;
        }
        for (SandboxManualDispatchInsertOptionDto option : options) {
            if (Boolean.TRUE.equals(option.getRecommended())) {
                return option;
            }
        }
        return fallback;
    }

    private static int compareTrial(SandboxManualDispatchInsertOptionDto left,
                                    SandboxManualDispatchInsertOptionDto right) {
        long leftDur = left.getRouteImpact() != null && left.getRouteImpact().getTotalDurationS() != null
                ? left.getRouteImpact().getTotalDurationS() : Long.MAX_VALUE;
        long rightDur = right.getRouteImpact() != null && right.getRouteImpact().getTotalDurationS() != null
                ? right.getRouteImpact().getTotalDurationS() : Long.MAX_VALUE;
        if (leftDur != rightDur) {
            return Long.compare(leftDur, rightDur);
        }
        long leftDist = left.getRouteImpact() != null && left.getRouteImpact().getTotalDistanceDeltaM() != null
                ? left.getRouteImpact().getTotalDistanceDeltaM() : Long.MAX_VALUE;
        long rightDist = right.getRouteImpact() != null && right.getRouteImpact().getTotalDistanceDeltaM() != null
                ? right.getRouteImpact().getTotalDistanceDeltaM() : Long.MAX_VALUE;
        return Long.compare(leftDist, rightDist);
    }

    private static void markRecommended(List<SandboxManualDispatchInsertOptionDto> options, Integer stopSeq) {
        if (options == null || stopSeq == null) {
            return;
        }
        for (SandboxManualDispatchInsertOptionDto option : options) {
            boolean recommended = stopSeq.equals(option.getInsertStopSeq());
            option.setRecommended(recommended);
            if (recommended) {
                option.setInsertPositionKey(INSERT_RECOMMENDED);
                if (option.getLabel() != null && !option.getLabel().startsWith("系统推荐")) {
                    option.setLabel("系统推荐 · " + option.getLabel());
                }
            }
        }
    }

    private static String resolveInsertPositionKey(int insertAt, int slotCount) {
        if (insertAt == 0) {
            return INSERT_FIRST;
        }
        if (insertAt == slotCount - 1) {
            return INSERT_LAST;
        }
        if (insertAt == 1 && slotCount > 2) {
            return INSERT_SECOND;
        }
        return INSERT_POSITION_PREFIX + (insertAt + 1);
    }

    private static String resolveInsertLabel(int insertAt, int slotCount) {
        if (insertAt == 0) {
            return "第一个送";
        }
        if (insertAt == slotCount - 1) {
            return "最后送";
        }
        if (insertAt == 1) {
            return "第二个送";
        }
        return "第 " + (insertAt + 1) + " 个送";
    }

    private static String formatRouteSummary(RoutePreviewSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        String distance = DisRouteSandboxDisplayFormatHelper.formatDistanceText(snapshot.totalDistanceM);
        String duration = DisRouteSandboxDisplayFormatHelper.formatDurationText(snapshot.totalDurationS);
        if (distance != null && duration != null) {
            return distance + " · " + duration;
        }
        return distance != null ? distance : duration;
    }

    private static GeoPoint resolveDepot(NxDisRoutePlanEntity plan) {
        if (plan != null && isValidCoordinate(plan.getNxDrpDepotLat(), plan.getNxDrpDepotLng())) {
            return toPoint(plan.getNxDrpDepotLat(), plan.getNxDrpDepotLng());
        }
        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
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
        return stop.getNxDrsDepartmentId() != null ? "客户 " + stop.getNxDrsDepartmentId() : null;
    }

    private static List<NxDisRouteStopEntity> cloneStops(List<NxDisRouteStopEntity> stops) {
        List<NxDisRouteStopEntity> clones = new ArrayList<NxDisRouteStopEntity>();
        if (stops == null) {
            return clones;
        }
        for (NxDisRouteStopEntity stop : stops) {
            clones.add(cloneStop(stop));
        }
        return clones;
    }

    private static NxDisRouteStopEntity cloneStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return new NxDisRouteStopEntity();
        }
        NxDisRouteStopEntity copy = new NxDisRouteStopEntity();
        copy.setNxDrsStopSeq(stop.getNxDrsStopSeq());
        copy.setNxDrsDepartmentId(stop.getNxDrsDepartmentId());
        copy.setNxDrsDepartmentName(stop.getNxDrsDepartmentName());
        copy.setNxDrsLat(stop.getNxDrsLat());
        copy.setNxDrsLng(stop.getNxDrsLng());
        copy.setNxDrsAddress(stop.getNxDrsAddress());
        copy.setNxDrsEarliestDeliveryTimeS(stop.getNxDrsEarliestDeliveryTimeS());
        copy.setNxDrsLatestDeliveryTimeS(stop.getNxDrsLatestDeliveryTimeS());
        copy.setNxDrsServiceMinutes(stop.getNxDrsServiceMinutes());
        copy.setShipmentTask(stop.getShipmentTask());
        copy.setSandboxStopKey(stop.getSandboxStopKey());
        return copy;
    }

    private static void resequence(List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return;
        }
        int seq = 1;
        for (NxDisRouteStopEntity stop : stops) {
            if (stop != null) {
                stop.setNxDrsStopSeq(seq++);
            }
        }
    }

    private static boolean allStopsHaveLegMetrics(List<NxDisRouteStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return false;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null || !hasLegMetrics(stop)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasLegMetrics(NxDisRouteStopEntity stop) {
        if (stop.getNxDrsLegDistanceM() != null && stop.getNxDrsLegDistanceM() > 0L) {
            return true;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        return task != null && task.getNxDstLegDistanceM() != null && task.getNxDstLegDistanceM() > 0L;
    }

    private static void applyRouteTotalsFromExistingLegs(NxDisDriverRouteEntity route,
                                                         RoutePreviewSnapshot snapshot) {
        long totalDistanceM = 0L;
        long totalDurationS = 0L;
        boolean hasLeg = false;
        if (route.getStops() != null) {
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop == null) {
                    continue;
                }
                Long legDist = stop.getNxDrsLegDistanceM();
                Long legDur = stop.getNxDrsLegDurationS();
                if ((legDist == null || legDist <= 0L) && stop.getShipmentTask() != null) {
                    legDist = stop.getShipmentTask().getNxDstLegDistanceM();
                    legDur = stop.getShipmentTask().getNxDstLegDurationS();
                }
                if (legDist != null && legDist > 0L) {
                    totalDistanceM += legDist;
                    hasLeg = true;
                }
                if (legDur != null && legDur > 0L) {
                    totalDurationS += legDur;
                    hasLeg = true;
                }
            }
        }
        if (hasLeg) {
            route.setNxDdrTotalDistanceM(totalDistanceM);
            route.setNxDdrTotalDurationS(totalDurationS);
            snapshot.totalDistanceM = totalDistanceM;
            snapshot.totalDurationS = totalDurationS;
        }
        route.setReturnLegDistanceM(0L);
        route.setReturnLegDurationS(0L);
        snapshot.returnDurationS = 0L;
    }

    private static final class RoutePreviewSnapshot {
        private Long totalDistanceM;
        private Long totalDurationS;
        private long returnDurationS;
        private List<StopScheduleSnapshot> stops = new ArrayList<StopScheduleSnapshot>();
    }

    private static final class StopScheduleSnapshot {
        private Integer departmentId;
        private String customerName;
        private Date plannedArrivalAt;
        private String plannedArrivalLabel;
        private String timeWindowLabel;
        private String timeWindowStatus;
        private int waitMinutes;
        private int lateMinutes;
    }
}
