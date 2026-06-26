package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dto.route.*;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.*;
import com.nongxinle.route.DisRouteSandboxDriverRouteEditPreviewHelper.DriverRoutePreviewResult;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

import static com.nongxinle.route.DisRoutePlanStatus.ASSIGNED;
import static com.nongxinle.route.DisRoutePlanStatus.READY;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

@Service("disRouteSandboxDriverRouteEditService")
public class DisRouteSandboxDriverRouteEditServiceImpl implements DisRouteSandboxDriverRouteEditService {

    @Autowired
    private DisRouteSandboxComputeService disRouteSandboxComputeService;
    @Autowired
    private DisRouteDriverDispatchListService disRouteDriverDispatchListService;
    @Autowired
    private DisRouteSandboxDriverRouteEditPreviewHelper previewHelper;
    @Autowired
    private DisRouteCustomerDriverConstraintService constraintService;
    @Autowired
    private DisRouteSandboxConfirmService disRouteSandboxConfirmService;
    @Autowired
    private DisRouteSandboxReturnService disRouteSandboxReturnService;
    @Autowired
    private DisRouteDispatchOperatorResolver disRouteDispatchOperatorResolver;
    @Autowired
    private DisShipmentTaskService disShipmentTaskService;
    @Autowired
    private DisRouteSandboxDriverRouteEditMapOverviewBuilder driverRouteEditMapOverviewBuilder;
    @Autowired
    private DisRouteSandboxDriverRouteEditStopPoolHelper stopPoolHelper;
    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private DisRouteSandboxRouteLoadingGateService disRouteSandboxRouteLoadingGateService;

    @Override
    public SandboxDriverRouteEditPageResponse buildEditPage(SandboxDriverRouteEditBaseRequest request)
            throws Exception {
        DriverRouteEditBuildContext ctx = prepareContext(request, null);
        List<String> stopKeys = resolveInitialStopKeys(ctx, request);
        applyPreview(ctx, stopKeys);
        return wrapResponse(ctx);
    }

    @Override
    public SandboxDriverRouteEditPageResponse preview(SandboxDriverRouteEditPreviewRequest request)
            throws Exception {
        DriverRouteEditBuildContext ctx = prepareContext(request, request.getStopKeys());
        applyPreview(ctx, normalizeStopKeys(request.getStopKeys()));
        return wrapResponse(ctx);
    }

    @Override
    @Transactional
    public Map<String, Object> confirm(SandboxDriverRouteEditConfirmRequest request) throws Exception {
        DriverRouteEditBuildContext ctx = prepareContext(request, request.getStopKeys());
        List<String> stopKeys = normalizeStopKeys(request.getStopKeys());
        validateConfirmConstraints(ctx, stopKeys);
        persistRouteEdit(ctx, stopKeys, request.getConfirmReason());
        Integer planId = resolvePersistedPlanId(ctx);
        if (planId != null) {
            disRouteSandboxConfirmService.refreshPlanAfterConfirm(planId);
        }
        Map<String, Object> response = buildConfirmSuccessResponse(ctx, stopKeys);
        scheduleAutoEnterLoadingAfterCommit(ctx, planId, response);
        return response;
    }

    private void scheduleAutoEnterLoadingAfterCommit(DriverRouteEditBuildContext ctx,
                                                     Integer planId,
                                                     Map<String, Object> response) {
        if (response == null) {
            return;
        }
        response.put("enteredLoading", Boolean.FALSE);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            tryAutoEnterLoading(ctx, planId, response);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                tryAutoEnterLoading(ctx, planId, response);
            }
        });
    }

    private void validateConfirmConstraints(DriverRouteEditBuildContext ctx, List<String> stopKeys) {
        validateConfirmStops(ctx, stopKeys);
        List<NxDisRouteStopEntity> trialStops = resolveStopsByKeys(ctx, stopKeys);
        for (NxDisRouteStopEntity stop : trialStops) {
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            ctx.evaluateConstraintWarnings(depId);
        }
        if (ctx.hasBlockingWarnings()) {
            throw new IllegalStateException("存在阻断性风险，无法确认：" + String.join("；", ctx.getWarnings()));
        }
    }

    private Integer resolvePersistedPlanId(DriverRouteEditBuildContext ctx) {
        if (ctx == null) {
            return null;
        }
        if (ctx.getPlan() != null && ctx.getPlan().getNxDrpId() != null) {
            return ctx.getPlan().getNxDrpId();
        }
        NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                ctx.getDisId(), ctx.getRouteDate(), ctx.getBatchCode(), ASSIGNED);
        if (plan == null) {
            plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                    ctx.getDisId(), ctx.getRouteDate(), ctx.getBatchCode(), READY);
        }
        return plan != null ? plan.getNxDrpId() : null;
    }

    private void tryAutoEnterLoading(DriverRouteEditBuildContext ctx,
                                     Integer planId,
                                     Map<String, Object> response) {
        if (response == null) {
            return;
        }
        response.put("enteredLoading", Boolean.FALSE);
        if (ctx == null || planId == null || ctx.getDriverUserId() == null) {
            return;
        }
        NxDisDriverRouteEntity driverRoute = nxDisDriverRouteDao.queryByPlanAndDriver(
                planId, ctx.getDriverUserId());
        if (driverRoute == null || driverRoute.getNxDdrId() == null) {
            response.put("enterLoadingBlockedReason", "未找到司机路线，请从今日派车手动进入装车");
            return;
        }
        try {
            DriverRouteLoadingGateRequest loadingRequest = new DriverRouteLoadingGateRequest();
            loadingRequest.setDisId(ctx.getDisId());
            loadingRequest.setRouteDate(ctx.getRouteDate());
            loadingRequest.setBatchCode(ctx.getBatchCode());
            loadingRequest.setOperatorUserId(ctx.getOperatorUserId());
            loadingRequest.setDriverUserId(ctx.getDriverUserId());
            disRouteSandboxRouteLoadingGateService.enterLoading(
                    driverRoute.getNxDdrId(), loadingRequest);
            response.put("enteredLoading", Boolean.TRUE);
            response.put("driverRouteId", driverRoute.getNxDdrId());
            response.put("nextPage", "LOADING");
        } catch (Exception ex) {
            response.put("enterLoadingBlockedReason", ex.getMessage() != null
                    ? ex.getMessage() : "自动进入装车失败，请从今日派车手动进入装车");
        }
    }

    private static Map<String, Object> buildConfirmSuccessResponse(DriverRouteEditBuildContext ctx,
                                                                     List<String> stopKeys) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("success", true);
        data.put("driverUserId", ctx.getDriverUserId());
        data.put("stopCount", stopKeys != null ? stopKeys.size() : 0);
        data.put("routeDate", ctx.getRouteDate());
        data.put("batchCode", ctx.getBatchCode());
        return data;
    }

    private DriverRouteEditBuildContext prepareContext(SandboxDriverRouteEditBaseRequest request,
                                                       List<String> requestedStopKeys) throws Exception {
        normalizeBaseRequest(request);
        validateBaseRequest(request);

        SandboxComputeResult compute = computeContext(request);
        DriverDispatchListResponse driverList = disRouteDriverDispatchListService.listDriversForBatch(
                request.getDisId(), resolveRouteDate(request.getRouteDate()), normalizeBatch(request.getBatchCode()));
        DriverDispatchCandidateDto driverMeta = findDriver(driverList, request.getDriverUserId());
        if (driverMeta == null || !DisRouteSandboxManualDispatchPanoramaHelper.isOnDuty(driverMeta.getDutyStatus())) {
            throw new IllegalStateException("司机未上岗或不存在，无法编辑路线");
        }
        boolean manualDispatch = isManualDispatchRequest(request);
        if (!manualDispatch && !Boolean.TRUE.equals(driverMeta.getBatchEligible())) {
            throw new IllegalStateException("司机当前不可派，无法编辑路线");
        }

        NxDisDriverRouteEntity route = indexAllPlanRoutes(compute.getMergedPlan()).get(request.getDriverUserId());
        String dispatchStage = DisRouteSandboxManualDispatchPanoramaHelper.resolveDispatchStage(
                route, compute, request.getDriverUserId());
        if (!manualDispatch
                && ManualDispatchDispatchStage.EXECUTION.equals(dispatchStage)) {
            throw new IllegalStateException("路线已在配送中或已完成，无法编辑");
        }
        if (!manualDispatch && (ManualDispatchDispatchStage.LOADING.equals(dispatchStage)
                || ManualDispatchDispatchStage.EXECUTION.equals(dispatchStage))) {
            throw new IllegalStateException("路线已进入装车/配送，无法编辑");
        }

        List<NxDisRouteStopEntity> baselineStops = DisRouteSandboxManualDispatchPanoramaHelper
                .collectDriverBaselineStops(compute, request.getDriverUserId(), dispatchStage);
        List<NxDisRouteStopEntity> availableStops = manualDispatch
                ? Collections.<NxDisRouteStopEntity>emptyList()
                : collectAvailableCustomers(compute, baselineStops);

        DriverRouteEditBuildContext ctx = new DriverRouteEditBuildContext();
        ctx.setManualDispatchMode(manualDispatch);
        if (manualDispatch) {
            ctx.setManualDispatchIncomingDepId(resolveManualDispatchDepId(request));
        }
        ctx.setDisId(request.getDisId());
        ctx.setRouteDate(resolveRouteDate(request.getRouteDate()));
        ctx.setBatchCode(normalizeBatch(request.getBatchCode()));
        ctx.setDriverUserId(request.getDriverUserId());
        ctx.setDriverName(driverMeta.getDriverName());
        ctx.setOperatorUserId(request.getOperatorUserId());
        ctx.setDispatchStage(dispatchStage);
        ctx.setPlan(compute.getMergedPlan());
        ctx.setRoute(route);
        ctx.setBaselineStops(baselineStops);
        ctx.setInitialBaselineStops(DisRouteSandboxDriverRouteEditPreviewHelper.cloneStops(baselineStops));
        ctx.setAvailableStops(availableStops);
        ctx.setCompute(compute);
        ctx.setConstraintService(constraintService);
        ctx.setConstraints(resolveConstraints(ctx, availableStops, baselineStops, driverList));
        ctx.setConfirmedDriverByDep(indexConfirmedDrivers(compute));
        if (requestedStopKeys != null && !requestedStopKeys.isEmpty()) {
            validateStopKeysBelongToPool(ctx, normalizeStopKeys(requestedStopKeys));
        }
        return ctx;
    }

    private void applyPreview(DriverRouteEditBuildContext ctx, List<String> stopKeys) throws Exception {
        List<NxDisRouteStopEntity> trialStops = resolveStopsByKeys(ctx, stopKeys);
        for (NxDisRouteStopEntity stop : trialStops) {
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            ctx.evaluateConstraintWarnings(depId);
        }
        DriverRoutePreviewResult preview = previewHelper.previewRoute(
                trialStops, ctx.getPlan(), ctx.getRouteDate());
        ctx.setPreview(preview);
        ctx.setBaselineStops(trialStops);
        evaluateWindowWarnings(ctx, preview);
    }

    private void evaluateWindowWarnings(DriverRouteEditBuildContext ctx, DriverRoutePreviewResult preview) {
        if (preview == null || preview.stopPreviews == null) {
            return;
        }
        int lateCount = 0;
        for (DisRouteSandboxDriverRouteEditPreviewHelper.StopPreview stopPreview : preview.stopPreviews) {
            if (stopPreview != null && stopPreview.lateMinutes > 0) {
                lateCount++;
            }
        }
        if (lateCount > 0) {
            ctx.addWarning("有 " + lateCount + " 个客户可能超出送达时间窗");
        }
    }

    private void validateConfirmStops(DriverRouteEditBuildContext ctx, List<String> stopKeys) {
        if (stopKeys == null || stopKeys.isEmpty()) {
            throw new IllegalArgumentException("stopKeys 不能为空");
        }
        validateStopKeysBelongToPool(ctx, stopKeys);
        for (String stopKey : stopKeys) {
            Integer depId = DisRouteSandboxStopKeyUtils.parseDepFatherId(stopKey);
            if (depId == null) {
                throw new IllegalArgumentException("无效 stopKey: " + stopKey);
            }
            Integer assignedDriver = resolveConfirmedDriverForDep(ctx, depId);
            if (assignedDriver != null && !assignedDriver.equals(ctx.getDriverUserId())) {
                throw new IllegalStateException("客户 dep:" + depId + " 已被其他司机确认");
            }
        }
    }

    private void persistRouteEdit(DriverRouteEditBuildContext ctx,
                                  List<String> stopKeys,
                                  String confirmReason) throws Exception {
        Set<Integer> targetDepIds = new LinkedHashSet<Integer>();
        for (String stopKey : stopKeys) {
            Integer depId = DisRouteSandboxStopKeyUtils.parseDepFatherId(stopKey);
            if (depId != null) {
                targetDepIds.add(depId);
            }
        }
        Set<Integer> originalDepIds = ctx.initialDepartmentIds();
        for (Integer depId : new HashSet<Integer>(originalDepIds)) {
            if (!targetDepIds.contains(depId)) {
                removeStopFromRoute(ctx, depId);
            }
        }
        int seq = 1;
        for (String stopKey : stopKeys) {
            Integer depId = DisRouteSandboxStopKeyUtils.parseDepFatherId(stopKey);
            if (depId == null) {
                continue;
            }
            upsertStopOnRoute(ctx, depId, seq++, confirmReason);
        }
    }

    private void removeStopFromRoute(DriverRouteEditBuildContext ctx, Integer depId) throws Exception {
        NxDisRouteStopEntity existing = findStopByDep(ctx.getInitialBaselineStops(), depId);
        if (existing == null || existing.getShipmentTask() == null) {
            return;
        }
        Integer taskId = existing.getShipmentTask().getNxDstId();
        if (taskId == null) {
            return;
        }
        SandboxStopReturnToSandboxRequest returnRequest = new SandboxStopReturnToSandboxRequest();
        returnRequest.setDisId(ctx.getDisId());
        returnRequest.setRouteDate(ctx.getRouteDate());
        returnRequest.setBatchCode(ctx.getBatchCode());
        returnRequest.setOperatorUserId(ctx.getOperatorUserId());
        returnRequest.setReason("司机路线人工编辑移除");
        returnRequest.setSuppressTodayResponse(true);
        returnRequest.setDeferPlanRefresh(true);
        disRouteSandboxReturnService.returnToSandbox(taskId, returnRequest);
    }

    private void upsertStopOnRoute(DriverRouteEditBuildContext ctx,
                                   Integer depId,
                                   int manualStopSeq,
                                   String confirmReason) throws Exception {
        NxDisRouteStopEntity existing = findStopByDep(ctx.getInitialBaselineStops(), depId);
        if (existing != null && existing.getShipmentTask() != null
                && existing.getShipmentTask().getNxDstId() != null
                && ctx.getDriverUserId().equals(existing.getShipmentTask().getNxDstAssignedDriverUserId())) {
            MoveTaskRequest moveRequest = new MoveTaskRequest();
            moveRequest.setTaskId(existing.getShipmentTask().getNxDstId());
            moveRequest.setAssignedDriverUserId(ctx.getDriverUserId());
            moveRequest.setManualStopSeq(manualStopSeq);
            moveRequest.setOperatorUserId(ctx.getOperatorUserId());
            moveRequest.setAdjustReason(resolveAssignReason(ctx, confirmReason));
            moveRequest.setDeferPlanRefresh(true);
            disShipmentTaskService.moveTask(moveRequest);
            return;
        }
        SandboxStopConfirmRequest confirmRequest = new SandboxStopConfirmRequest();
        confirmRequest.setDisId(ctx.getDisId());
        confirmRequest.setRouteDate(ctx.getRouteDate());
        confirmRequest.setBatchCode(ctx.getBatchCode());
        confirmRequest.setOperatorUserId(ctx.getOperatorUserId());
        confirmRequest.setDriverUserId(ctx.getDriverUserId());
        confirmRequest.setDepFatherId(depId);
        confirmRequest.setSandboxStopKey(DisRouteSandboxStopKeyUtils.build(depId));
        confirmRequest.setManualStopSeq(manualStopSeq);
        confirmRequest.setAssignReason(resolveAssignReason(ctx, confirmReason));
        confirmRequest.setSuppressTodayResponse(true);
        confirmRequest.setDeferPlanRefresh(true);
        NxDisRouteStopEntity pooledStop = stopPoolHelper.resolveStop(ctx, DisRouteSandboxStopKeyUtils.build(depId));
        if (pooledStop != null && pooledStop.getShipmentTask() != null) {
            List<Integer> liveOrderIds = DisRouteSandboxTodayDriverRouteCardBuilder.collectLiveOrderIds(
                    pooledStop.getShipmentTask());
            if (liveOrderIds != null && !liveOrderIds.isEmpty()) {
                confirmRequest.setLiveOrderIds(liveOrderIds);
            }
        }
        disRouteSandboxConfirmService.confirmStop(confirmRequest);
    }

    private static String resolveAssignReason(DriverRouteEditBuildContext ctx, String confirmReason) {
        if (confirmReason != null && !confirmReason.trim().isEmpty()) {
            return confirmReason.trim();
        }
        if (ctx != null && ctx.isManualDispatchMode()) {
            return DisRouteAssignReason.MANUAL_DISPATCH;
        }
        return DisRouteAssignReason.DRIVER_ROUTE_EDIT;
    }

    private static boolean isManualDispatchRequest(SandboxDriverRouteEditBaseRequest request) {
        if (request == null) {
            return false;
        }
        if (Boolean.TRUE.equals(request.getManualDispatch())) {
            return true;
        }
        return resolveManualDispatchDepId(request) != null;
    }

    private static Integer resolveManualDispatchDepId(SandboxDriverRouteEditBaseRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getDepartmentId() != null) {
            return request.getDepartmentId();
        }
        if (request.getDepFatherId() != null) {
            return request.getDepFatherId();
        }
        return DisRouteSandboxStopKeyUtils.parseDepFatherId(request.getSandboxStopKey());
    }

    private List<String> resolveInitialStopKeys(DriverRouteEditBuildContext ctx,
                                                SandboxDriverRouteEditBaseRequest request) {
        List<String> keys = buildStopKeysFromBaseline(ctx.getInitialBaselineStops());
        if (!isManualDispatchRequest(request)) {
            return keys;
        }
        Integer depId = resolveManualDispatchDepId(request);
        String incomingKey = depId != null ? DisRouteSandboxStopKeyUtils.build(depId) : null;
        if (incomingKey != null && !keys.contains(incomingKey)) {
            keys.add(incomingKey);
        }
        return keys;
    }

    private List<NxDisRouteStopEntity> resolveStopsByKeys(DriverRouteEditBuildContext ctx,
                                                          List<String> stopKeys) {
        List<NxDisRouteStopEntity> resolved = new ArrayList<NxDisRouteStopEntity>();
        for (String stopKey : stopKeys) {
            NxDisRouteStopEntity stop = stopPoolHelper.resolveStop(ctx, stopKey);
            if (stop == null) {
                throw new IllegalArgumentException("stopKey 不在可编辑客户池: " + stopKey);
            }
            resolved.add(DisRouteSandboxDriverRouteEditPreviewHelper.cloneStop(stop));
        }
        return resolved;
    }

    private void validateStopKeysBelongToPool(DriverRouteEditBuildContext ctx, List<String> stopKeys) {
        for (String stopKey : stopKeys) {
            if (stopPoolHelper.resolveStop(ctx, stopKey) == null) {
                throw new IllegalArgumentException("stopKey 不在可编辑客户池: " + stopKey);
            }
        }
    }

    private Integer resolveConfirmedDriverForDep(DriverRouteEditBuildContext ctx, Integer depId) {
        if (ctx.getConfirmedDriverByDep() != null) {
            Integer driverId = ctx.getConfirmedDriverByDep().get(depId);
            if (driverId != null) {
                return driverId;
            }
        }
        NxDisRouteStopEntity stop = findStopByDep(ctx.getInitialBaselineStops(), depId);
        if (stop == null) {
            stop = findStopByDep(ctx.getAvailableStops(), depId);
        }
        if (stop == null || stop.getShipmentTask() == null) {
            return null;
        }
        return stop.getShipmentTask().getNxDstAssignedDriverUserId();
    }

    private static NxDisRouteStopEntity findStopByDep(List<NxDisRouteStopEntity> stops, Integer depId) {
        if (stops == null || depId == null) {
            return null;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (depId.equals(DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop))) {
                return stop;
            }
        }
        return null;
    }

    private List<NxDisRouteStopEntity> collectAvailableCustomers(SandboxComputeResult compute,
                                                                 List<NxDisRouteStopEntity> baselineStops) {
        Set<Integer> onRoute = new HashSet<Integer>();
        if (baselineStops != null) {
            for (NxDisRouteStopEntity stop : baselineStops) {
                Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
                if (depId != null) {
                    onRoute.add(depId);
                }
            }
        }
        List<NxDisRouteStopEntity> available = new ArrayList<NxDisRouteStopEntity>();
        if (compute.getUnassignedStops() != null) {
            List<NxDisRouteStopEntity> consolidated =
                    DisRouteSandboxUnassignedStopHelper.consolidateByDepartment(compute.getUnassignedStops());
            for (NxDisRouteStopEntity stop : consolidated) {
                Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
                if (depId != null && !onRoute.contains(depId)) {
                    available.add(stop);
                }
            }
        }
        return available;
    }

    private Map<Integer, DisRouteCustomerDriverConstraintDto> resolveConstraints(
            DriverRouteEditBuildContext ctx,
            List<NxDisRouteStopEntity> availableStops,
            List<NxDisRouteStopEntity> baselineStops,
            DriverDispatchListResponse driverList) {
        Set<Integer> depIds = new LinkedHashSet<Integer>();
        collectDepIds(depIds, availableStops);
        collectDepIds(depIds, baselineStops);
        List<Integer> eligibleDrivers = new ArrayList<Integer>();
        if (driverList != null && driverList.getDrivers() != null) {
            for (DriverDispatchCandidateDto driver : driverList.getDrivers()) {
                if (driver != null && driver.getDriverUserId() != null) {
                    eligibleDrivers.add(driver.getDriverUserId());
                }
            }
        }
        return constraintService.resolveConstraints(ctx.getDisId(), new ArrayList<Integer>(depIds), eligibleDrivers);
    }

    private static void collectDepIds(Set<Integer> depIds, List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            if (depId != null) {
                depIds.add(depId);
            }
        }
    }

    private static List<String> buildStopKeysFromBaseline(List<NxDisRouteStopEntity> baselineStops) {
        List<String> keys = new ArrayList<String>();
        if (baselineStops == null) {
            return keys;
        }
        for (NxDisRouteStopEntity stop : baselineStops) {
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            String key = DisRouteSandboxStopKeyUtils.build(depId);
            if (key != null) {
                keys.add(key);
            }
        }
        return keys;
    }

    private static List<String> normalizeStopKeys(List<String> stopKeys) {
        if (stopKeys == null) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<String>();
        Set<String> seen = new HashSet<String>();
        for (String key : stopKeys) {
            if (key == null || key.trim().isEmpty()) {
                continue;
            }
            String trimmed = key.trim();
            if (seen.add(trimmed)) {
                normalized.add(trimmed);
            }
        }
        return normalized;
    }

    private SandboxDriverRouteEditPageResponse wrapResponse(DriverRouteEditBuildContext ctx) {
        SandboxDriverRouteEditPageResponse response = new SandboxDriverRouteEditPageResponse();
        SandboxDriverRouteEditPageViewModel pageViewModel = DisRouteSandboxDriverRouteEditPageBuilder.build(ctx);
        if (ctx.isManualDispatchMode()) {
            pageViewModel.setPageTitle("调整送货顺序");
            pageViewModel.setManualDispatchMode(Boolean.TRUE);
            pageViewModel.setAvailableCustomers(new ArrayList<SandboxDriverRouteEditAvailableCustomerDto>());
        }
        pageViewModel.setMapOverview(driverRouteEditMapOverviewBuilder.build(ctx));
        response.setPageViewModel(pageViewModel);
        return response;
    }

    private SandboxComputeResult computeContext(SandboxDriverRouteEditBaseRequest request) throws Exception {
        SandboxComputeRequest computeRequest = new SandboxComputeRequest();
        computeRequest.setDisId(request.getDisId());
        computeRequest.setRouteDate(resolveRouteDate(request.getRouteDate()));
        computeRequest.setBatchCode(normalizeBatch(request.getBatchCode()));
        SandboxComputeResult compute = disRouteSandboxComputeService.compute(computeRequest);
        if (isManualDispatchRequest(request)) {
            DisRouteSandboxManualDispatchComputeEnricher.enrich(compute);
        }
        return compute;
    }

    private void normalizeBaseRequest(SandboxDriverRouteEditBaseRequest request) {
        if (request == null) {
            return;
        }
        request.setOperatorUserId(disRouteDispatchOperatorResolver.resolve(
                request.getDisId(), request.getOperatorUserId()));
        if (request.getBatchCode() != null) {
            request.setBatchCode(request.getBatchCode().trim().toUpperCase());
        }
        if (request.getDepartmentId() == null && request.getDepFatherId() != null) {
            request.setDepartmentId(request.getDepFatherId());
        }
    }

    private void validateBaseRequest(SandboxDriverRouteEditBaseRequest request) {
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        if (request.getDriverUserId() == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }
        if (request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
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

    private Map<Integer, Integer> indexConfirmedDrivers(SandboxComputeResult compute) {
        Map<Integer, Integer> index = new LinkedHashMap<Integer, Integer>();
        appendConfirmedDrivers(index, compute != null ? compute.getConfirmedStops() : null);
        return index;
    }

    private void appendConfirmedDrivers(Map<Integer, Integer> index, List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            Integer driverId = resolveStopAssignedDriver(stop);
            if (depId != null && driverId != null) {
                index.put(depId, driverId);
            }
        }
    }

    private static Integer resolveStopAssignedDriver(NxDisRouteStopEntity stop) {
        if (stop.getShipmentTask() != null && stop.getShipmentTask().getNxDstAssignedDriverUserId() != null) {
            return stop.getShipmentTask().getNxDstAssignedDriverUserId();
        }
        return stop.getSuggestedDriverUserId();
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
        if (routes == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route != null && route.getNxDdrDriverUserId() != null) {
                index.put(route.getNxDdrDriverUserId(), route);
            }
        }
    }
}
