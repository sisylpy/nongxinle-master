package com.nongxinle.service.impl;

import com.nongxinle.dto.route.*;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteDispatchBatch;
import com.nongxinle.route.DisRouteDispatchOperatorResolver;
import com.nongxinle.route.DisRouteAssignReason;
import com.nongxinle.route.DisRouteDispatchCardTemplateBuilder;
import com.nongxinle.route.DisRouteSandboxManualDispatchPanoramaHelper;
import com.nongxinle.route.DisRouteSandboxManualDispatchPanoramaHelper.DriverStopCounts;
import com.nongxinle.route.DisRouteSandboxManualDispatchPanoramaHelper.ManualDispatchPanoramaCapabilities;
import com.nongxinle.route.ManualDispatchConfirmMode;
import com.nongxinle.route.ManualDispatchDispatchStage;
import com.nongxinle.route.ManualDispatchPrimaryActionMaps;
import com.nongxinle.route.DisRouteSandboxManualDispatchEditPageBuilder;
import com.nongxinle.route.DisRouteSandboxManualDispatchSimulator;
import com.nongxinle.route.DisRouteSandboxStopKeyUtils;
import com.nongxinle.route.DisRouteSandboxUnassignedStopHelper;
import com.nongxinle.service.DisRouteDriverDispatchListService;
import com.nongxinle.service.DisRouteSandboxComputeService;
import com.nongxinle.service.DisRouteSandboxConfirmService;
import com.nongxinle.service.DisRouteSandboxManualDispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

import com.nongxinle.route.ManualDispatchPrepareResult;
import com.nongxinle.route.ManualDispatchSimulateCommand;

@Service("disRouteSandboxManualDispatchService")
public class DisRouteSandboxManualDispatchServiceImpl implements DisRouteSandboxManualDispatchService {

    @Autowired
    private DisRouteSandboxComputeService disRouteSandboxComputeService;
    @Autowired
    private DisRouteDriverDispatchListService disRouteDriverDispatchListService;
    @Autowired
    private DisRouteSandboxManualDispatchSimulator manualDispatchSimulator;
    @Autowired
    private DisRouteSandboxManualDispatchEditPageBuilder manualDispatchEditPageBuilder;
    @Autowired
    private DisRouteSandboxConfirmService disRouteSandboxConfirmService;
    @Autowired
    private DisRouteDispatchOperatorResolver disRouteDispatchOperatorResolver;

    @Override
    public SandboxManualDispatchDriverPanoramaResponse listDriverPanorama(
            SandboxManualDispatchBaseRequest request) throws Exception {
        normalizeBaseRequest(request);
        validateBaseRequest(request);
        SandboxComputeResult compute = computeContext(request);
        Integer departmentId = resolveDepartmentId(request);
        NxDisRouteStopEntity unassigned = requireUnassignedStop(compute, departmentId);
        DriverDispatchListResponse driverList = disRouteDriverDispatchListService.listDriversForBatch(
                request.getDisId(), resolveRouteDate(request.getRouteDate()), normalizeBatch(request.getBatchCode()));

        SandboxManualDispatchDriverPanoramaResponse response = new SandboxManualDispatchDriverPanoramaResponse();
        response.setDisId(request.getDisId());
        response.setRouteDate(resolveRouteDate(request.getRouteDate()));
        response.setBatchCode(normalizeBatch(request.getBatchCode()));
        response.setStoreCard(DisRouteDispatchCardTemplateBuilder.buildUnassignedStoreCard(
                unassigned, new Date(), resolveSandboxStopKey(request, departmentId)));

        Map<Integer, NxDisDriverRouteEntity> routeByDriver = indexAllPlanRoutes(compute.getMergedPlan());
        int simulatable = 0;
        int directConfirm = 0;
        int riskAck = 0;

        if (driverList.getDrivers() != null) {
            for (DriverDispatchCandidateDto driver : driverList.getDrivers()) {
                if (driver == null || !DisRouteSandboxManualDispatchPanoramaHelper.isOnDuty(driver.getDutyStatus())) {
                    continue;
                }
                NxDisDriverRouteEntity route = routeByDriver.get(driver.getDriverUserId());
                String dispatchStage = DisRouteSandboxManualDispatchPanoramaHelper.resolveDispatchStage(
                        route, compute, driver.getDriverUserId());
                ManualDispatchPanoramaCapabilities capabilities =
                        DisRouteSandboxManualDispatchPanoramaHelper.resolveCapabilities(dispatchStage);
                DriverStopCounts stopCounts = DisRouteSandboxManualDispatchPanoramaHelper.resolveStopCounts(
                        compute, route, driver.getDriverUserId());

                DispatchDriverCardDto dto = DisRouteDispatchCardTemplateBuilder.buildDriverCard(
                        driver,
                        route,
                        dispatchStage,
                        capabilities,
                        stopCounts,
                        DisRouteSandboxManualDispatchPanoramaHelper.buildOperationHint(
                                dispatchStage, stopCounts.getPendingStopCount()),
                        buildSimulateAction(request, departmentId, driver.getDriverUserId()));
                response.getDrivers().add(dto);

                simulatable++;
                if (ManualDispatchConfirmMode.DIRECT.equals(capabilities.getConfirmMode())) {
                    directConfirm++;
                } else if (ManualDispatchConfirmMode.RISK_ACK.equals(capabilities.getConfirmMode())) {
                    riskAck++;
                }
            }
        }
        sortPanoramaDrivers(response.getDrivers());

        SandboxManualDispatchPanoramaSummaryDto summary = new SandboxManualDispatchPanoramaSummaryDto();
        summary.setOnDutyDriverCount(response.getDrivers().size());
        summary.setSimulatableDriverCount(simulatable);
        summary.setDirectConfirmDriverCount(directConfirm);
        summary.setRiskAckDriverCount(riskAck);
        summary.setForbiddenDriverCount(0);
        summary.setSummaryLine(buildManualDispatchSummaryLine(response.getDrivers().size(), simulatable));
        response.setSummary(summary);
        return response;
    }

    private static String buildManualDispatchSummaryLine(int onDutyDriverCount, int simulatableDriverCount) {
        if (onDutyDriverCount <= 0) {
            return "暂无上岗司机，请先在「司机可派状态」开启可派";
        }
        if (simulatableDriverCount <= 0) {
            return "上岗司机 " + onDutyDriverCount + " 人，当前暂无可试算路线";
        }
        return "上岗司机 " + onDutyDriverCount + " 人，" + simulatableDriverCount + " 人可试算加入路线";
    }

    @Override
    public SandboxManualDispatchSimulateResponse simulate(SandboxManualDispatchSimulateRequest request)
            throws Exception {
        ManualDispatchPrepareResult prepared = prepareManualDispatch(
                request,
                request.getDriverUserId(),
                request.getManualStopSeq(),
                request.getRequiredLatestArrivalAt());
        return manualDispatchSimulator.simulate(prepared.getCommand());
    }

    @Override
    public SandboxManualDispatchEditPageResponse buildEditPage(SandboxManualDispatchEditPageRequest request)
            throws Exception {
        ManualDispatchPrepareResult prepared = prepareManualDispatch(
                request,
                request.getDriverUserId(),
                request.getManualStopSeq(),
                request.getRequiredLatestArrivalAt());
        SandboxManualDispatchSimulateResponse simulate =
                manualDispatchSimulator.simulate(prepared.getCommand());

        SandboxManualDispatchEditPageViewModel pageViewModel = manualDispatchEditPageBuilder.build(
                prepared.getCommand(), simulate, prepared.getCapabilities(), prepared.getOperationHint());
        pageViewModel.setOperatorUserId(request.getOperatorUserId());
        if (pageViewModel.getDriver() != null) {
            pageViewModel.getDriver().setRouteSummary(
                    DisRouteSandboxManualDispatchPanoramaHelper.buildRouteSummary(
                            prepared.getRoute(), prepared.getStopCounts(),
                            prepared.getCommand().getDispatchStage()));
        }

        SandboxManualDispatchEditPageResponse response = new SandboxManualDispatchEditPageResponse();
        response.setPageViewModel(pageViewModel);
        return response;
    }

    private ManualDispatchPrepareResult prepareManualDispatch(SandboxManualDispatchBaseRequest request,
                                                                Integer driverUserId,
                                                                Integer manualStopSeq,
                                                                String requiredLatestArrivalAt)
            throws Exception {
        normalizeBaseRequest(request);
        validateBaseRequest(request);
        if (driverUserId == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }
        SandboxComputeResult compute = computeContext(request);
        ManualDispatchSimulateCommand command = buildSimulateCommand(
                request, driverUserId, manualStopSeq, requiredLatestArrivalAt, compute);

        NxDisDriverRouteEntity route = indexAllPlanRoutes(compute.getMergedPlan()).get(driverUserId);
        DriverStopCounts stopCounts = DisRouteSandboxManualDispatchPanoramaHelper.resolveStopCounts(
                compute, route, driverUserId);

        ManualDispatchPrepareResult prepared = new ManualDispatchPrepareResult();
        prepared.setCommand(command);
        prepared.setCompute(compute);
        prepared.setRoute(route);
        prepared.setStopCounts(stopCounts);
        prepared.setCapabilities(DisRouteSandboxManualDispatchPanoramaHelper.resolveCapabilities(
                command.getDispatchStage()));
        prepared.setOperationHint(DisRouteSandboxManualDispatchPanoramaHelper.buildOperationHint(
                command.getDispatchStage(), stopCounts.getPendingStopCount()));
        return prepared;
    }

    private ManualDispatchSimulateCommand buildSimulateCommand(SandboxManualDispatchBaseRequest request,
                                                               Integer driverUserId,
                                                               Integer manualStopSeq,
                                                               String requiredLatestArrivalAt,
                                                               SandboxComputeResult cachedCompute)
            throws Exception {
        if (driverUserId == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }
        SandboxComputeResult compute = cachedCompute != null ? cachedCompute : computeContext(request);
        Integer departmentId = resolveDepartmentId(request);
        NxDisRouteStopEntity unassigned = requireUnassignedStop(compute, departmentId);
        DriverDispatchListResponse driverList = disRouteDriverDispatchListService.listDriversForBatch(
                request.getDisId(), resolveRouteDate(request.getRouteDate()), normalizeBatch(request.getBatchCode()));
        DriverDispatchCandidateDto driverMeta = findDriver(driverList, driverUserId);
        if (driverMeta == null || !DisRouteSandboxManualDispatchPanoramaHelper.isOnDuty(driverMeta.getDutyStatus())) {
            throw new IllegalStateException("司机未上岗或不存在");
        }
        NxDisDriverRouteEntity route = indexAllPlanRoutes(compute.getMergedPlan()).get(driverUserId);
        String dispatchStage = DisRouteSandboxManualDispatchPanoramaHelper.resolveDispatchStage(
                route, compute, driverUserId);
        List<NxDisRouteStopEntity> baselineStops = DisRouteSandboxManualDispatchPanoramaHelper
                .collectDriverBaselineStops(compute, driverUserId, dispatchStage);

        ManualDispatchSimulateCommand command = new ManualDispatchSimulateCommand();
        command.setDisId(request.getDisId());
        command.setRouteDate(resolveRouteDate(request.getRouteDate()));
        command.setBatchCode(normalizeBatch(request.getBatchCode()));
        command.setDepartmentId(departmentId);
        command.setSandboxStopKey(request.getSandboxStopKey() != null
                ? request.getSandboxStopKey()
                : DisRouteSandboxStopKeyUtils.build(departmentId));
        command.setDriverUserId(driverUserId);
        command.setDriverName(driverMeta.getDriverName());
        command.setDispatchStage(dispatchStage);
        command.setIncomingStop(unassigned);
        command.setBaselineStops(baselineStops);
        command.setPlan(compute.getMergedPlan());
        command.setManualStopSeq(manualStopSeq);
        command.setRequiredLatestArrivalAt(requiredLatestArrivalAt);
        return command;
    }

    @Override
    public Map<String, Object> confirmManualDispatch(SandboxStopConfirmRequest request) throws Exception {
        if (request == null) {
            throw new IllegalArgumentException("request 不能为空");
        }
        request.setOperatorUserId(disRouteDispatchOperatorResolver.resolve(
                request.getDisId(), request.getOperatorUserId()));
        if (request.getAssignReason() == null || request.getAssignReason().trim().isEmpty()) {
            request.setAssignReason(DisRouteAssignReason.MANUAL_DISPATCH);
        }
        return disRouteSandboxConfirmService.confirmStop(request);
    }

    private SandboxComputeResult computeContext(SandboxManualDispatchBaseRequest request) throws Exception {
        SandboxComputeRequest computeRequest = new SandboxComputeRequest();
        computeRequest.setDisId(request.getDisId());
        computeRequest.setRouteDate(resolveRouteDate(request.getRouteDate()));
        computeRequest.setBatchCode(normalizeBatch(request.getBatchCode()));
        return disRouteSandboxComputeService.compute(computeRequest);
    }

    private NxDisRouteStopEntity requireUnassignedStop(SandboxComputeResult compute, Integer departmentId) {
        NxDisRouteStopEntity stop = findUnassignedStop(compute, departmentId);
        if (stop == null) {
            throw new IllegalArgumentException("客户 depFatherId=" + departmentId + " 不在未分配列表中");
        }
        return stop;
    }

    private NxDisRouteStopEntity findUnassignedStop(SandboxComputeResult compute, Integer departmentId) {
        if (compute == null || compute.getUnassignedStops() == null || departmentId == null) {
            return null;
        }
        List<NxDisRouteStopEntity> consolidated =
                DisRouteSandboxUnassignedStopHelper.consolidateByDepartment(compute.getUnassignedStops());
        for (NxDisRouteStopEntity stop : consolidated) {
            if (departmentId.equals(DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop))) {
                return stop;
            }
        }
        return null;
    }

    private List<NxDisRouteStopEntity> collectDriverSandboxStops(SandboxComputeResult compute,
                                                                 Integer driverUserId) {
        List<NxDisRouteStopEntity> stops = new ArrayList<NxDisRouteStopEntity>();
        appendDriverStops(stops, compute != null ? compute.getConfirmedStops() : null, driverUserId);
        appendDriverStops(stops, compute != null ? compute.getSandboxSuggestedStops() : null, driverUserId);
        Collections.sort(stops, new Comparator<NxDisRouteStopEntity>() {
            @Override
            public int compare(NxDisRouteStopEntity a, NxDisRouteStopEntity b) {
                int seqA = a != null && a.getNxDrsStopSeq() != null ? a.getNxDrsStopSeq() : 0;
                int seqB = b != null && b.getNxDrsStopSeq() != null ? b.getNxDrsStopSeq() : 0;
                return Integer.compare(seqA, seqB);
            }
        });
        return stops;
    }

    private void appendDriverStops(List<NxDisRouteStopEntity> target,
                                   List<NxDisRouteStopEntity> source,
                                   Integer driverUserId) {
        if (target == null || source == null || driverUserId == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : source) {
            if (stop == null) {
                continue;
            }
            Integer stopDriverId = resolveStopDriverUserId(stop);
            if (driverUserId.equals(stopDriverId)) {
                target.add(stop);
            }
        }
    }

    private Integer resolveStopDriverUserId(NxDisRouteStopEntity stop) {
        if (stop.getSuggestedDriverUserId() != null) {
            return stop.getSuggestedDriverUserId();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task == null) {
            return null;
        }
        if (task.getNxDstAssignedDriverUserId() != null) {
            return task.getNxDstAssignedDriverUserId();
        }
        return task.getNxDstSuggestedDriverUserId();
    }

    private int countDriverSandboxStops(SandboxComputeResult compute, Integer driverUserId) {
        return collectDriverSandboxStops(compute, driverUserId).size();
    }

    private Map<Integer, NxDisDriverRouteEntity> indexAllPlanRoutes(NxDisRoutePlanEntity plan) {
        Map<Integer, NxDisDriverRouteEntity> index = new LinkedHashMap<Integer, NxDisDriverRouteEntity>();
        appendPlanRoutes(index, plan != null ? plan.getDriverRoutes() : null);
        appendPlanRoutes(index, plan != null ? plan.getLoadingDriverRoutes() : null);
        appendPlanRoutes(index, plan != null ? plan.getExecutionDriverRoutes() : null);
        return index;
    }

    private void appendPlanRoutes(Map<Integer, NxDisDriverRouteEntity> index,
                                  List<NxDisDriverRouteEntity> routes) {
        if (index == null || routes == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route != null && route.getNxDdrDriverUserId() != null) {
                index.put(route.getNxDdrDriverUserId(), route);
            }
        }
    }

    private String resolveSandboxStopKey(SandboxManualDispatchBaseRequest request, Integer departmentId) {
        if (request.getSandboxStopKey() != null) {
            return request.getSandboxStopKey();
        }
        if (departmentId != null) {
            return DisRouteSandboxStopKeyUtils.build(departmentId);
        }
        return null;
    }

    private void sortPanoramaDrivers(List<DispatchDriverCardDto> drivers) {
        if (drivers == null) {
            return;
        }
        Collections.sort(drivers, new Comparator<DispatchDriverCardDto>() {
            @Override
            public int compare(DispatchDriverCardDto a, DispatchDriverCardDto b) {
                int rankA = panoramaSortRank(a);
                int rankB = panoramaSortRank(b);
                if (rankA != rankB) {
                    return Integer.compare(rankA, rankB);
                }
                int pendingA = a.getPendingStopCount() != null ? a.getPendingStopCount() : 0;
                int pendingB = b.getPendingStopCount() != null ? b.getPendingStopCount() : 0;
                return Integer.compare(pendingA, pendingB);
            }
        });
    }

    private static int panoramaSortRank(DispatchDriverCardDto dto) {
        if (dto == null) {
            return 99;
        }
        if (Boolean.TRUE.equals(dto.getCanConfirm())) {
            return 0;
        }
        if (Boolean.TRUE.equals(dto.getCanSimulate())) {
            return 1;
        }
        return 2;
    }

    private Map<String, Object> buildSimulateAction(SandboxManualDispatchBaseRequest request,
                                                    Integer departmentId,
                                                    Integer driverUserId) {
        Map<String, Object> payload = ManualDispatchPrimaryActionMaps.buildPayload(
                request.getDisId(),
                resolveRouteDate(request.getRouteDate()),
                normalizeBatch(request.getBatchCode()),
                request.getOperatorUserId(),
                departmentId,
                request.getSandboxStopKey() != null
                        ? request.getSandboxStopKey()
                        : DisRouteSandboxStopKeyUtils.build(departmentId),
                request.getLiveOrderIds());
        payload.put("driverUserId", driverUserId);

        Map<String, Object> action = new LinkedHashMap<String, Object>();
        action.put("actionType", "SIMULATE_MANUAL_DISPATCH");
        action.put("label", "模拟加入该司机路线");
        action.put("enabled", Boolean.TRUE);
        action.put("payload", payload);
        return action;
    }

    private DriverDispatchCandidateDto findDriver(DriverDispatchListResponse driverList, Integer driverUserId) {
        if (driverList == null || driverList.getDrivers() == null || driverUserId == null) {
            return null;
        }
        for (DriverDispatchCandidateDto driver : driverList.getDrivers()) {
            if (driver != null && driverUserId.equals(driver.getDriverUserId())) {
                return driver;
            }
        }
        return null;
    }

    private void normalizeBaseRequest(SandboxManualDispatchBaseRequest request) {
        if (request == null) {
            return;
        }
        request.setOperatorUserId(disRouteDispatchOperatorResolver.resolve(
                request.getDisId(), request.getOperatorUserId()));
        if (request.getDepartmentId() == null && request.getDepFatherId() != null) {
            request.setDepartmentId(request.getDepFatherId());
        }
        if (request.getDepFatherId() == null && request.getDepartmentId() != null) {
            request.setDepFatherId(request.getDepartmentId());
        }
    }

    private void validateBaseRequest(SandboxManualDispatchBaseRequest request) {
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        if (resolveDepartmentId(request) == null) {
            throw new IllegalArgumentException("departmentId 或 sandboxStopKey 不能为空");
        }
    }

    private Integer resolveDepartmentId(SandboxManualDispatchBaseRequest request) {
        if (request.getDepartmentId() != null) {
            return request.getDepartmentId();
        }
        if (request.getDepFatherId() != null) {
            return request.getDepFatherId();
        }
        return DisRouteSandboxStopKeyUtils.parseDepFatherId(request.getSandboxStopKey());
    }

    private String resolveCustomerName(NxDisRouteStopEntity stop) {
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

    private String resolveRouteDate(String routeDate) {
        if (routeDate != null && !routeDate.trim().isEmpty()) {
            return routeDate.trim();
        }
        return formatWhatDay(0);
    }

    private String normalizeBatch(String batchCode) {
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return DisRouteDispatchBatch.MORNING;
        }
        return batchCode.trim().toUpperCase();
    }
}
