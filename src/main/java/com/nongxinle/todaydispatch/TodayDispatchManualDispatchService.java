package com.nongxinle.todaydispatch;

import com.nongxinle.dto.route.DispatchDriverCardDto;
import com.nongxinle.dto.route.DriverDispatchCandidateDto;
import com.nongxinle.dto.route.DriverDispatchListResponse;
import com.nongxinle.dto.route.SandboxManualDispatchBaseRequest;
import com.nongxinle.dto.route.SandboxManualDispatchDriverPanoramaResponse;
import com.nongxinle.dto.route.SandboxManualDispatchPanoramaSummaryDto;
import com.nongxinle.route.DisRouteDispatchBatch;
import com.nongxinle.route.DisRouteSandboxDriverDispatchPhase;
import com.nongxinle.route.DriverRouteEditPrimaryActionMaps;
import com.nongxinle.route.ManualDispatchPrimaryActionMaps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

/**
 * 单站人工调度（Phase 2B 精简版）：上岗司机全景 + 跳转司机路线编辑确认。
 * 不复用旧 DisRouteSandboxManualDispatch* 链。
 */
@Service
public class TodayDispatchManualDispatchService {

    @Autowired
    private TodayDispatchComputeService todayDispatchComputeService;
    @Autowired
    private TodayDispatchDriverListService todayDispatchDriverListService;
    @Autowired
    private DriverDispatchStateService driverDispatchStateService;

    public SandboxManualDispatchDriverPanoramaResponse listDriverPanorama(
            SandboxManualDispatchBaseRequest request) throws Exception {
        normalizeRequest(request);
        validateRequest(request);

        TodayDispatchResult dispatchResult = todayDispatchComputeService.compute(
                request.getDisId(),
                resolveRouteDate(request.getRouteDate()),
                normalizeBatch(request.getBatchCode()),
                request.getOperatorUserId());

        CustomerStopPlan targetStop = requireUnassignedStop(dispatchResult, resolveDepartmentId(request));
        DriverDispatchListResponse driverList = todayDispatchDriverListService.listDriversForBatch(
                request.getDisId(),
                dispatchResult.getRouteDate(),
                dispatchResult.getBatchCode(),
                false);

        SandboxManualDispatchDriverPanoramaResponse response = new SandboxManualDispatchDriverPanoramaResponse();
        response.setDisId(request.getDisId());
        response.setRouteDate(dispatchResult.getRouteDate());
        response.setBatchCode(dispatchResult.getBatchCode());
        response.setStoreCard(DispatchStopCardTemplate.toStoreCard(targetStop));

        int simulatable = 0;
        int directConfirm = 0;
        int riskAck = 0;
        if (driverList.getDrivers() != null) {
            for (DriverDispatchCandidateDto candidate : driverList.getDrivers()) {
                if (candidate == null || !ON_DUTY.equals(candidate.getDutyStatus())) {
                    continue;
                }
                DispatchDriverCardDto card = buildDriverCard(
                        dispatchResult, request, targetStop, candidate);
                response.getDrivers().add(card);
                simulatable++;
                if ("RISK_ACK".equals(card.getConfirmMode())) {
                    riskAck++;
                } else {
                    directConfirm++;
                }
            }
        }
        sortDrivers(response.getDrivers());

        SandboxManualDispatchPanoramaSummaryDto summary = new SandboxManualDispatchPanoramaSummaryDto();
        summary.setOnDutyDriverCount(response.getDrivers().size());
        summary.setSimulatableDriverCount(simulatable);
        summary.setDirectConfirmDriverCount(directConfirm);
        summary.setRiskAckDriverCount(riskAck);
        summary.setForbiddenDriverCount(0);
        summary.setSummaryLine(buildSummaryLine(response.getDrivers().size(), simulatable));
        response.setSummary(summary);
        return response;
    }

    private DispatchDriverCardDto buildDriverCard(TodayDispatchResult dispatchResult,
                                                    SandboxManualDispatchBaseRequest request,
                                                    CustomerStopPlan targetStop,
                                                    DriverDispatchCandidateDto candidate) {
        Integer driverUserId = candidate.getDriverUserId();
        DriverDispatchStateService.StateQuery query = driverDispatchStateService.forCompute(
                dispatchResult.getDisId(),
                dispatchResult.getRouteDate(),
                dispatchResult.getBatchCode(),
                resolvePlanId(dispatchResult));
        DriverDispatchStateService.Eligibility eligibility =
                driverDispatchStateService.eligibilityFor(driverUserId, query);

        String phase = eligibility.getPhase();
        boolean riskAck = DisRouteSandboxDriverDispatchPhase.LOADING.equals(phase)
                || DisRouteSandboxDriverDispatchPhase.ACTIVE_EXECUTION.equals(phase);

        int pendingCount = countPendingStops(dispatchResult, driverUserId);
        int totalStops = pendingCount + 1;
        List<String> pendingStopNames = todayDispatchComputeService.collectDriverPendingStopNames(
                dispatchResult, driverUserId);

        DispatchDriverCardDto card = new DispatchDriverCardDto();
        card.setDriverUserId(driverUserId);
        card.setDriverName(candidate.getDriverName());
        card.setDriverAvatarUrl(candidate.getDriverAvatarUrl());
        card.setDutyStatus(ON_DUTY);
        card.setDutyStatusLabel("可派");
        card.setDispatchStage(mapDispatchStage(phase));
        card.setDispatchStageLabel(labelDispatchStage(phase));
        card.setDutyBadgeTone("ok");
        card.setStageBadgeTone(riskAck ? "warn" : "ok");
        card.setPendingStopCount(pendingCount);
        card.setCurrentStopCount(totalStops);
        card.setCustomerCount(totalStops);
        card.setCompletedStopCount(0);
        card.setRouteSummary(buildRouteSummary(totalStops, dispatchResult, driverUserId));
        card.setHeadline(buildHeadline(phase, pendingCount));
        card.setCurrentTaskLine(buildCurrentTaskLine(pendingCount, pendingStopNames));
        card.setCanSimulate(Boolean.TRUE);
        card.setCanConfirm(Boolean.TRUE);
        card.setConfirmMode(riskAck ? "RISK_ACK" : "DIRECT");
        card.setConfirmModeLabel(riskAck ? "需确认风险" : null);
        if (riskAck) {
            card.getRiskHints().add(buildRiskHint(phase));
        }
        card.setOperationHint(buildOperationHint(phase, pendingCount));
        card.setHintTone(riskAck ? "warn" : "ok");
        card.setPrimaryAction(buildSimulateAction(dispatchResult, request, targetStop, driverUserId, riskAck));
        return card;
    }

    private Map<String, Object> buildSimulateAction(TodayDispatchResult dispatchResult,
                                                    SandboxManualDispatchBaseRequest request,
                                                    CustomerStopPlan targetStop,
                                                    Integer driverUserId,
                                                    boolean riskAck) {
        List<String> stopKeys = new ArrayList<String>(todayDispatchComputeService.collectDriverStopKeys(
                dispatchResult, driverUserId));
        String incomingKey = targetStop.getSandboxStopKey();
        if (incomingKey != null) {
            stopKeys.remove(incomingKey);
            stopKeys.add(incomingKey);
        }

        Map<String, Object> payload = ManualDispatchPrimaryActionMaps.buildPayload(
                request.getDisId(),
                dispatchResult.getRouteDate(),
                dispatchResult.getBatchCode(),
                request.getOperatorUserId(),
                targetStop.getDepFatherId(),
                incomingKey,
                targetStop.getLiveOrderIds());
        payload.put("driverUserId", driverUserId);
        payload.put("manualDispatch", Boolean.TRUE);
        payload.put("stopKeys", stopKeys);
        payload.put("sourcePage", "DISPATCH_SANDBOX");
        payload.put("editPagePath", DriverRouteEditPrimaryActionMaps.EDIT_PAGE_PATH);
        payload.put("previewPath", DriverRouteEditPrimaryActionMaps.PREVIEW_PATH);
        payload.put("confirmPath", DriverRouteEditPrimaryActionMaps.CONFIRM_PATH);

        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", "SIMULATE_MANUAL_DISPATCH");
        action.put("label", "加入并调整路线");
        action.put("enabled", Boolean.TRUE);
        action.put("disabledReason", "");
        action.put("payload", payload);
        action.put("toneClass", riskAck ? "stop-state-warn" : "stop-state-action");
        return action;
    }

    private int countPendingStops(TodayDispatchResult dispatchResult, Integer driverUserId) {
        return todayDispatchComputeService.collectDriverStopKeys(dispatchResult, driverUserId).size();
    }

    private static String buildRouteSummary(int totalStops,
                                            TodayDispatchResult dispatchResult,
                                            Integer driverUserId) {
        DriverRoutePlan route = findDriverRoute(dispatchResult, driverUserId);
        if (route != null && route.getOutboundDistanceText() != null) {
            return totalStops + " 个客户 · " + route.getOutboundDistanceText();
        }
        return totalStops + " 个客户";
    }

    private static DriverRoutePlan findDriverRoute(TodayDispatchResult dispatchResult, Integer driverUserId) {
        if (dispatchResult == null || dispatchResult.getSuggestedRoutes() == null) {
            return null;
        }
        for (DriverRoutePlan route : dispatchResult.getSuggestedRoutes()) {
            if (route != null && driverUserId.equals(route.getDriverUserId())) {
                return route;
            }
        }
        return null;
    }

    private static String buildCurrentTaskLine(int pendingCount, List<String> pendingStopNames) {
        if (pendingCount <= 0) {
            return "暂无在途客户";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(pendingCount).append(" 个客户 · 待送 ");
        if (pendingStopNames != null && !pendingStopNames.isEmpty()) {
            sb.append(joinStopNames(pendingStopNames));
        } else {
            sb.append(pendingCount);
        }
        return sb.toString();
    }

    private static String joinStopNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                sb.append("、");
            }
            String name = names.get(i);
            if (name != null && !name.trim().isEmpty()) {
                sb.append(name.trim());
            }
        }
        return sb.toString();
    }

    private static String buildHeadline(String phase, int pendingCount) {
        if (DisRouteSandboxDriverDispatchPhase.LOADING.equals(phase)) {
            return "装车中";
        }
        if (DisRouteSandboxDriverDispatchPhase.ACTIVE_EXECUTION.equals(phase)) {
            return "配送中";
        }
        if (pendingCount <= 0) {
            return "空闲可派";
        }
        return "已有 " + pendingCount + " 站待送";
    }

    private static String buildOperationHint(String phase, int pendingCount) {
        if (DisRouteSandboxDriverDispatchPhase.LOADING.equals(phase)) {
            return "可插入装车路线，请关注装车顺序";
        }
        if (DisRouteSandboxDriverDispatchPhase.ACTIVE_EXECUTION.equals(phase)) {
            return "可插入配送路线，请关注后续客户影响";
        }
        if (pendingCount <= 0) {
            return "可直接加入该司机路线";
        }
        return "可插入已确认/建议路线";
    }

    private static String buildRiskHint(String phase) {
        if (DisRouteSandboxDriverDispatchPhase.LOADING.equals(phase)) {
            return "司机正在装车，追加可能影响装车顺序";
        }
        if (DisRouteSandboxDriverDispatchPhase.ACTIVE_EXECUTION.equals(phase)) {
            return "司机配送中，追加可能影响后续送达";
        }
        return "请确认后再派单";
    }

    private static String mapDispatchStage(String phase) {
        if (DisRouteSandboxDriverDispatchPhase.LOADING.equals(phase)) {
            return "LOADING";
        }
        if (DisRouteSandboxDriverDispatchPhase.ACTIVE_EXECUTION.equals(phase)) {
            return "EXECUTION";
        }
        if (DisRouteSandboxDriverDispatchPhase.REDISPATCH_PRE_DEPART.equals(phase)) {
            return "CONFIRMED";
        }
        return "SANDBOX";
    }

    private static String labelDispatchStage(String phase) {
        if (DisRouteSandboxDriverDispatchPhase.LOADING.equals(phase)) {
            return "装车中";
        }
        if (DisRouteSandboxDriverDispatchPhase.ACTIVE_EXECUTION.equals(phase)) {
            return "配送中";
        }
        if (DisRouteSandboxDriverDispatchPhase.REDISPATCH_PRE_DEPART.equals(phase)) {
            return "已确认待装车";
        }
        return "可派";
    }

    private static String buildSummaryLine(int onDutyCount, int simulatableCount) {
        if (onDutyCount <= 0) {
            return "暂无上岗司机，请先在「司机可派状态」开启可派";
        }
        return "上岗司机 " + onDutyCount + " 人，" + simulatableCount + " 人可试算加入路线";
    }

    private static void sortDrivers(List<DispatchDriverCardDto> drivers) {
        if (drivers == null) {
            return;
        }
        Collections.sort(drivers, new Comparator<DispatchDriverCardDto>() {
            @Override
            public int compare(DispatchDriverCardDto a, DispatchDriverCardDto b) {
                int pendingA = a != null && a.getPendingStopCount() != null ? a.getPendingStopCount() : 0;
                int pendingB = b != null && b.getPendingStopCount() != null ? b.getPendingStopCount() : 0;
                return Integer.compare(pendingA, pendingB);
            }
        });
    }

    private CustomerStopPlan requireUnassignedStop(TodayDispatchResult dispatchResult, Integer departmentId) {
        CustomerStopPlan stop = findUnassignedStop(dispatchResult, departmentId);
        if (stop == null) {
            throw new IllegalArgumentException("客户 depFatherId=" + departmentId + " 不在未分配列表中");
        }
        return stop;
    }

    private static CustomerStopPlan findUnassignedStop(TodayDispatchResult dispatchResult, Integer departmentId) {
        if (dispatchResult == null || dispatchResult.getUnassignedStops() == null || departmentId == null) {
            return null;
        }
        for (CustomerStopPlan stop : dispatchResult.getUnassignedStops()) {
            if (stop != null && departmentId.equals(stop.getDepFatherId())) {
                return stop;
            }
        }
        return null;
    }

    private static Integer resolvePlanId(TodayDispatchResult dispatchResult) {
        if (dispatchResult == null || dispatchResult.getCompute() == null
                || dispatchResult.getCompute().getMergedPlan() == null) {
            return null;
        }
        return dispatchResult.getCompute().getMergedPlan().getNxDrpId();
    }

    private static void normalizeRequest(SandboxManualDispatchBaseRequest request) {
        if (request == null) {
            return;
        }
        if (request.getDepFatherId() == null) {
            request.setDepFatherId(request.getDepartmentId());
        }
        if (request.getDepartmentId() == null) {
            request.setDepartmentId(request.getDepFatherId());
        }
        if (request.getBatchCode() != null) {
            request.setBatchCode(request.getBatchCode().trim().toUpperCase());
        }
    }

    private static void validateRequest(SandboxManualDispatchBaseRequest request) {
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        if (resolveDepartmentId(request) == null) {
            throw new IllegalArgumentException("departmentId / depFatherId 不能为空");
        }
    }

    private static Integer resolveDepartmentId(SandboxManualDispatchBaseRequest request) {
        if (request.getDepFatherId() != null) {
            return request.getDepFatherId();
        }
        return request.getDepartmentId();
    }

    private static String resolveRouteDate(String routeDate) {
        return routeDate != null && !routeDate.trim().isEmpty()
                ? routeDate.trim() : formatWhatDay(0);
    }

    private static String normalizeBatch(String batchCode) {
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return DisRouteDispatchBatch.MORNING;
        }
        return batchCode.trim().toUpperCase();
    }
}
