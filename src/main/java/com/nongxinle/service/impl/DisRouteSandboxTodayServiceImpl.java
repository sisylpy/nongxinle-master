package com.nongxinle.service.impl;

import com.nongxinle.config.DisRouteDispatchSettings;
import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dto.route.*;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;
import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.route.DisRouteBatchEligibility;
import com.nongxinle.route.DisRouteDispatchDisIdGuard;
import com.nongxinle.route.DisRouteDeliveryHistoryPreferenceReadModelAssembler;
import com.nongxinle.route.VisibleDriverRouteSnapshot;
import com.nongxinle.route.VisibleDriverRouteSnapshotBuilder;
import com.nongxinle.route.SandboxTodayPipelineTrace;
import com.nongxinle.route.proposal.SandboxProposalPlan;
import com.nongxinle.route.proposal.SandboxProposalPlanBuilder;
import com.nongxinle.route.proposal.SandboxProposalPlanReadModelAssembler;
import com.nongxinle.route.dispatch.strategy.DispatchAssignmentPlan;
import com.nongxinle.route.dispatch.strategy.DispatchAssignmentPlanReadModelAssembler;
import com.nongxinle.route.dispatch.strategy.DispatchFinalRouteDebugAssembler;
import com.nongxinle.route.dispatch.strategy.DispatchPlanningPhase;
import com.nongxinle.route.dispatch.strategy.DispatchStrategyMode;
import com.nongxinle.route.model.GeoPoint;
import com.nongxinle.route.DisRouteSandboxDriverDispatchPhase;
import com.nongxinle.route.DisRouteSandboxDriverDispatchStateHelper;
import com.nongxinle.route.DisRouteDaySegmentHelper;
import com.nongxinle.route.DisRouteDispatchLabels;
import com.nongxinle.route.DisRouteDriverRouteStatus;
import com.nongxinle.route.DisRouteExecutionRouteSnapshotHelper;
import com.nongxinle.route.DisRouteRouteExecutionHelper;
import com.nongxinle.route.DisRouteDispatchOperatorResolver;
import com.nongxinle.route.DisRouteSandboxUnassignedStopHelper;
import com.nongxinle.route.DisRouteSandboxTodayMapRoadPolylineEnricher;
import com.nongxinle.route.DisRouteSandboxTodayPageViewModelBuilder;
import com.nongxinle.route.DisRouteSandboxTodayPlanStopSyncHelper;
import com.nongxinle.route.DisRouteSandboxTodayTimelineBuilder;
import com.nongxinle.route.DisRouteSandboxTodayViewModelMaps;
import com.nongxinle.route.SandboxTodayPageBuildContext;
import com.nongxinle.route.DisRouteSandboxReadModelPartitionHelper;
import com.nongxinle.route.DisRouteSandboxScheduleLabelHelper;
import com.nongxinle.route.DisRouteSandboxScheduleMode;
import com.nongxinle.route.DisRouteStopTimeWindowStatus;
import com.nongxinle.route.DisRouteTemporalHelper;
import com.nongxinle.route.RouteDispatchDateFormat;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.nongxinle.route.RouteCoordinateUtils.isValidCoordinate;
import static com.nongxinle.route.RouteCoordinateUtils.toPoint;

@Service("disRouteSandboxTodayService")
public class DisRouteSandboxTodayServiceImpl implements DisRouteSandboxTodayService {

    @Autowired
    private DisRouteSandboxComputeService disRouteSandboxComputeService;
    @Autowired
    private DisRouteDispatchOperationPolicy disRouteDispatchOperationPolicy;
    @Autowired
    private DisRouteDispatchWorkbenchService disRouteDispatchWorkbenchService;
    @Autowired
    private DisRouteDriverDispatchListService disRouteDriverDispatchListService;
    @Autowired
    private DisRouteDispatchSettings disRouteDispatchSettings;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private DisRouteDispatchDisIdGuard disRouteDispatchDisIdGuard;
    @Autowired
    private DisRouteDispatchOperatorResolver disRouteDispatchOperatorResolver;
    @Autowired
    private DisRouteSandboxTodayMapRoadPolylineEnricher mapRoadPolylineEnricher;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private DisRouteSandboxLegMetricsHelper disRouteSandboxLegMetricsHelper;
    @Autowired
    private NxDistributerService nxDistributerService;

    @Override
    public Map<String, Object> buildToday(Integer disId, String routeDate, String batchCode) throws Exception {
        return buildToday(disId, routeDate, batchCode, null);
    }

    @Override
    public Map<String, Object> buildToday(Integer disId, String routeDate, String batchCode,
                                          Integer operatorUserId) throws Exception {
        return buildToday(disId, routeDate, batchCode, operatorUserId, true);
    }

    private Map<String, Object> buildToday(Integer disId, String routeDate, String batchCode,
                                           Integer operatorUserId,
                                           boolean includeFullDebugPayload) throws Exception {
        disRouteDispatchDisIdGuard.logMissingDistributerIfNeeded(disId, "sandbox/today");
        SandboxComputeRequest computeRequest = new SandboxComputeRequest();
        computeRequest.setDisId(disId);
        computeRequest.setRouteDate(routeDate);
        computeRequest.setBatchCode(batchCode);
        computeRequest.setFormalPageContractMode(!includeFullDebugPayload);
        SandboxTodayPipelineTrace pipelineTrace = null;
        if (includeFullDebugPayload) {
            pipelineTrace = SandboxTodayPipelineTrace.create(false);
            computeRequest.setEnablePipelineTrace(true);
            computeRequest.setSharedPipelineTrace(pipelineTrace);
        }
        SandboxComputeResult compute = disRouteSandboxComputeService.compute(computeRequest);
        if (pipelineTrace == null) {
            pipelineTrace = compute.getPipelineTrace();
        }

        Map<String, Object> data = new LinkedHashMap<String, Object>();
        Date serverNow = new Date();
        String effectiveRouteDate = compute.getRouteDate();
        String normalizedBatch = compute.getDispatchBatch() != null ? compute.getDispatchBatch() : "MORNING";

        data.put("routeDate", effectiveRouteDate);
        data.put("routeDateLabel", DisRouteTemporalHelper.formatRouteDateLabel(effectiveRouteDate, serverNow));
        data.put("dispatchBatch", normalizedBatch);
        data.put("dispatchBatchLabel", DisRouteDispatchLabels.label(normalizedBatch));
        data.put("serverNow", RouteDispatchDateFormat.format(serverNow));
        data.put("currentServerTime", RouteDispatchDateFormat.format(serverNow));
        data.put("sandboxVersion", compute.getSandboxVersion());
        data.put("orderVersion", compute.getOrderVersion());
        data.put("dutyVersion", compute.getDutyVersion());
        data.put("hasNewOrders", compute.isHasNewOrders());
        data.put("hasOrderChanges", compute.isHasOrderChanges());
        data.put("hasLockedStops", compute.isHasLockedStops());

        NxDisRoutePlanEntity mergedPlan = compute.getMergedPlan();
        List<NxDisShipmentTaskEntity> tasks = compute.getAllDisplayTasks();

        DriverDispatchListResponse drivers = disRouteDriverDispatchListService.listDriversForBatch(
                disId, effectiveRouteDate, normalizedBatch, includeFullDebugPayload);
        RouteFeasibilityResult feasibility = buildSandboxFeasibility(
                mergedPlan, compute, drivers, serverNow);
        appendMissingDisIdWarning(feasibility, disId);
        data.put("feasibilityStatus", feasibility.getFeasibilityStatus());
        data.put("warnings", feasibility.getWarnings());

        if (mergedPlan != null) {
            RouteDispatchReadModelAssembler.linkSharedTaskInstances(mergedPlan, tasks);
            DisRouteSandboxReadModelPartitionHelper.linkConfirmedStopsToTasks(
                    compute.getConfirmedStops(), tasks);
            disRouteDispatchOperationPolicy.enrichTasksReadModel(tasks, mergedPlan, feasibility);
            disRouteDispatchOperationPolicy.enrichPlanReadModel(mergedPlan, feasibility);

            DisRouteSandboxReadModelPartitionHelper.StopPartition stopPartition =
                    DisRouteSandboxReadModelPartitionHelper.partitionConfirmedStops(
                            compute.getConfirmedStops(),
                            DisRouteSandboxReadModelPartitionHelper.buildRouteIndex(mergedPlan));
            if (pipelineTrace != null) {
                pipelineTrace.recordConfirmedStopPartition(
                        stopPartition.sandboxStops, stopPartition.loadingStops, stopPartition.executionStops);
                pipelineTrace.recordPartitionBefore(mergedPlan.getDriverRoutes());
            }
            DisRouteSandboxReadModelPartitionHelper.partitionPlanRoutes(mergedPlan);
            if (pipelineTrace != null) {
                pipelineTrace.recordPartitionAfter(mergedPlan, nxDisDriverRouteDao, nxDisShipmentTaskDao);
            }
            reconcileSandboxPlanAfterPartition(mergedPlan, feasibility, stopPartition);
            disRouteDispatchOperationPolicy.enrichExecutionRoutesReadModel(mergedPlan, feasibility);
            disRouteDispatchOperationPolicy.enrichLoadingDriverRoutesReadModel(mergedPlan, feasibility);
            reconcileExecutionDeliveryReadModel(mergedPlan, stopPartition.executionStops);

            compute.setConfirmedStops(stopPartition.sandboxStops);
            compute.setLoadingStops(stopPartition.loadingStops);
            compute.setExecutionStops(stopPartition.executionStops);

            disRouteDispatchOperationPolicy.enrichEphemeralSandboxStops(
                    compute.getSandboxSuggestedStops(), mergedPlan, feasibility);
            disRouteDispatchOperationPolicy.enrichEphemeralSandboxStops(
                    compute.getUnassignedStops(), mergedPlan, feasibility);

            if ((compute.getConfirmedStops() == null || compute.getConfirmedStops().isEmpty())
                    && (compute.getLoadingStops() == null || compute.getLoadingStops().isEmpty())) {
                mergedPlan.setCanStartLoading(false);
                if (!DisRouteSandboxReadModelPartitionHelper.hasExecutionRoutes(mergedPlan)) {
                    mergedPlan.setLoadingBlockedReason("请先在站点卡片确认出货完成");
                }
            } else if (DisRouteSandboxReadModelPartitionHelper.hasLoadingRoutes(mergedPlan)) {
                mergedPlan.setCanStartLoading(true);
                mergedPlan.setLoadingBlockedReason(null);
            }
        }

        List<NxDisRouteStopEntity> sandboxConfirmedStops = compute.getConfirmedStops();
        List<NxDisRouteStopEntity> loadingStops = compute.getLoadingStops();
        List<NxDisRouteStopEntity> executionStops = compute.getExecutionStops();
        int sandboxCustomerStopCount = countSandboxCustomerStops(compute);
        int loadingCustomerStopCount = countLoadingCustomerStops(compute, loadingStops);
        int executionCustomerStopCount = executionStops != null ? executionStops.size() : 0;
        int sandboxConfirmedStopCount = sandboxConfirmedStops != null ? sandboxConfirmedStops.size() : 0;
        int loadingConfirmedStopCount = loadingStops != null ? loadingStops.size() : 0;
        boolean hasSandboxScheduledStops = sandboxCustomerStopCount > 0;
        boolean hasLoadingScheduledStops = loadingCustomerStopCount > 0;
        boolean executionOnly = mergedPlan != null
                && !hasSandboxScheduledStops
                && !hasLoadingScheduledStops
                && DisRouteSandboxReadModelPartitionHelper.hasExecutionRoutes(mergedPlan);
        boolean loadingOnly = mergedPlan != null
                && !hasSandboxScheduledStops
                && hasLoadingScheduledStops
                && !DisRouteSandboxReadModelPartitionHelper.hasExecutionRoutes(mergedPlan);

        data.put("sandboxCustomerStopCount", sandboxCustomerStopCount);
        data.put("loadingCustomerStopCount", loadingCustomerStopCount);
        data.put("executionCustomerStopCount", executionCustomerStopCount);
        data.put("hasSandboxScheduledStops", hasSandboxScheduledStops);
        data.put("hasLoadingScheduledStops", hasLoadingScheduledStops);
        data.put("hasLoadingRoutes", mergedPlan != null
                && DisRouteSandboxReadModelPartitionHelper.hasLoadingRoutes(mergedPlan));
        data.put("customerStopCount", sandboxCustomerStopCount);
        data.put("hasScheduledStops", hasSandboxScheduledStops);
        data.put("executionOnly", executionOnly);
        data.put("loadingOnly", loadingOnly);

        if (mergedPlan != null && !hasSandboxScheduledStops) {
            clearEmptySandboxTemporalFields(mergedPlan);
        }

        if (hasSandboxScheduledStops && mergedPlan != null && mergedPlan.getNxDrpBatchStartAt() != null) {
            data.put("batchStartAt", RouteDispatchDateFormat.format(mergedPlan.getNxDrpBatchStartAt()));
        }
        if (hasSandboxScheduledStops && mergedPlan != null && mergedPlan.getNxDrpBatchEndAt() != null) {
            data.put("batchEndAt", RouteDispatchDateFormat.format(mergedPlan.getNxDrpBatchEndAt()));
        }

        DispatchWorkbenchDto workbench = hasSandboxScheduledStops
                ? disRouteDispatchWorkbenchService.buildSandbox(
                mergedPlan, tasks, feasibility, effectiveRouteDate, normalizedBatch,
                sandboxCustomerStopCount, sandboxConfirmedStopCount, serverNow)
                : (executionOnly
                ? disRouteDispatchWorkbenchService.buildSandboxEmpty(effectiveRouteDate, serverNow)
                : disRouteDispatchWorkbenchService.buildSandboxEmpty(effectiveRouteDate, serverNow));
        if (executionOnly) {
            applyExecutionOnlyWorkbench(workbench, mergedPlan, effectiveRouteDate, serverNow);
        } else if (loadingOnly) {
            applyLoadingOnlySandboxWorkbench(workbench, mergedPlan, effectiveRouteDate, serverNow);
        }
        if (workbench != null && compute.getInvalidStops() != null && !compute.getInvalidStops().isEmpty()) {
            appendInvalidHint(workbench, compute.getInvalidStops().size());
        }
        applySandboxScheduleSummary(data, mergedPlan, serverNow, hasSandboxScheduledStops);
        applyDisplayShiftLabels(data, effectiveRouteDate, serverNow, mergedPlan, workbench, drivers);
        enrichSandboxDriverStopCounts(drivers, mergedPlan, sandboxConfirmedStops);
        if (pipelineTrace != null) {
            pipelineTrace.recordDriverCardsOverlay(drivers);
        }
        if (mergedPlan != null) {
            Map<String, Object> sandboxSummary = buildSandboxMetricsSummary(
                    compute, sandboxCustomerStopCount, drivers);
            data.put("summary", sandboxSummary);
            data.put("sandboxSummary", sandboxSummary);
        }

        SandboxTodayPageBuildContext pageCtx = createPageBuildContext(
                compute, mergedPlan, data, workbench, drivers,
                sandboxCustomerStopCount, sandboxConfirmedStopCount,
                loadingCustomerStopCount, loadingStops, executionStops,
                serverNow, disId, normalizedBatch, operatorUserId);
        pageCtx.setPipelineTrace(pipelineTrace);
        SandboxProposalPlan proposalPlan = compute.getProposalPlan();
        pageCtx.setProposalPlan(proposalPlan);
        if (mergedPlan != null) {
            DisRouteSandboxTodayPlanStopSyncHelper.syncSandboxScheduleLabelsFromPlan(
                    mergedPlan,
                    compute.getSandboxSuggestedStops(),
                    compute.getConfirmedStops(),
                    compute.getUnassignedStops());
        }
        if (pipelineTrace != null) {
            int proposalRouteCount = proposalPlan != null && proposalPlan.getProposalRoutes() != null
                    ? proposalPlan.getProposalRoutes().size() : 0;
            pipelineTrace.recordSnapshotInput(proposalRouteCount, "PROPOSAL_PLAN");
        }
        pageCtx.setVisibleDriverRoutes(buildVisibleDriverRouteSnapshots(
                pageCtx, proposalPlan, compute.getDispatchAssignmentPlan(), mergedPlan));
        if (pipelineTrace != null) {
            pipelineTrace.recordSnapshotOutput(pageCtx.getVisibleDriverRoutes());
        }
        SandboxTodayPageViewModel pageViewModel = buildPageViewModelFromContext(
                pageCtx, compute, mergedPlan, loadingStops, executionStops);
        if (pipelineTrace != null) {
            pipelineTrace.recordPageViewModel(
                    pageViewModel, pageCtx.getVisibleDriverRoutes(),
                    countProposalCustomerStops(proposalPlan), mergedPlan, proposalPlan);
        }
        Map<String, Object> pageViewModelMap = DisRouteSandboxTodayViewModelMaps.toMap(pageViewModel);
        data.put("pageViewModel", pageViewModelMap);

        if (includeFullDebugPayload) {
            attachDebugTraceToResponse(data, compute, pipelineTrace);
        }

        if (!includeFullDebugPayload) {
            return data;
        }

        data.put("visibleDriverRouteWarnings",
                VisibleDriverRouteSnapshotBuilder.collectScheduleWarnings(pageCtx.getVisibleDriverRoutes()));

        data.put("tasks", RouteDispatchReadModelAssembler.toPersistedTaskMaps(tasks));
        data.put("confirmedStops", toConfirmedStopMaps(sandboxConfirmedStops));
        data.put("loadingStops", toLoadingStopMaps(loadingStops, mergedPlan != null
                ? mergedPlan.getLoadingDriverRoutes() : null));
        data.put("executionStops", toExecutionStopMaps(executionStops, mergedPlan != null
                ? mergedPlan.getExecutionDriverRoutes() : null));
        data.put("sandboxSuggestedStops", toEphemeralStopMaps(compute.getSandboxSuggestedStops()));
        data.put("proposalPlan", SandboxProposalPlanReadModelAssembler.toMap(
                compute.getProposalPlan(), compute.getDispatchAssignmentPlan()));
        data.put("unassignedStops", toEphemeralStopMaps(compute.getUnassignedStops()));
        data.put("deliveryHistoryPreferences",
                DisRouteDeliveryHistoryPreferenceReadModelAssembler.toMap(compute.getDeliveryHistoryPreferences()));
        DispatchAssignmentPlan assignmentPlan = compute.getDispatchAssignmentPlan();
        if (assignmentPlan != null
                && assignmentPlan.getStrategyMode() == DispatchStrategyMode.OWNER_FIXED_ROUTE) {
            assignmentPlan.setFinalDriverRoutes(DispatchFinalRouteDebugAssembler.buildFromVisibleRouteSnapshots(
                    pageCtx.getVisibleDriverRoutes()));
        }
        Map<String, Object> assignmentPlanMap = DispatchAssignmentPlanReadModelAssembler.toMap(assignmentPlan);
        data.put("dispatchAssignmentPlan", assignmentPlanMap);
        data.put("hasDispatchAssignmentPlan", assignmentPlan != null);
        if (assignmentPlan != null && assignmentPlan.getStrategyMode() != null) {
            data.put("strategyMode", assignmentPlan.getStrategyMode().name());
        }
        if (assignmentPlan != null && assignmentPlan.getPlanningPhase() != null) {
            data.put("planningPhase", assignmentPlan.getPlanningPhase().name());
        }
        data.put("invalidStops", compute.getInvalidStops());
        data.put("invalidStopCount", compute.getInvalidStops() != null ? compute.getInvalidStops().size() : 0);
        data.put("effectiveOrders", compute.getEffectiveOrders());
        data.put("hasExecutionRoutes", mergedPlan != null && (
                DisRouteSandboxReadModelPartitionHelper.hasExecutionRoutes(mergedPlan)
                        || (executionStops != null && !executionStops.isEmpty())));
        data.put("drivers", drivers);
        data.put("plan", mergedPlan != null ? RouteDispatchReadModelAssembler.toPlanMap(mergedPlan) : null);
        data.put("dispatchWorkbench", workbench);
        data.put("actionPermissions", buildActionPermissions(compute, tasks, mergedPlan, loadingStops));

        if (mergedPlan != null && hasSandboxScheduledStops) {
            data.put("planTemporalStatus", mergedPlan.getPlanTemporalStatus());
            data.put("planTemporalStatusLabel", mergedPlan.getPlanTemporalStatusLabel());
        }
        if (mergedPlan != null) {
            data.put("loadingSummary", buildRouteMetricsSummary(mergedPlan.getLoadingDriverRoutes()));
            data.put("executionSummary", buildRouteMetricsSummary(mergedPlan.getExecutionDriverRoutes()));
            data.put("loadingDriverRoutes",
                    RouteDispatchReadModelAssembler.toLoadingDriverRouteMaps(
                            mergedPlan.getLoadingDriverRoutes(),
                            buildPurchaseStatusByOrderId(mergedPlan.getLoadingDriverRoutes())));
            data.put("executionDriverRoutes",
                    RouteDispatchReadModelAssembler.toDeliveryDriverRouteMaps(
                            mergedPlan.getExecutionDriverRoutes(), drivers));
        }

        data.put("loadingPageViewModel", DisRouteSandboxTodayViewModelMaps.toMap(
                buildLoadingPageViewModel(compute, mergedPlan, data, drivers, loadingStops, executionStops,
                        serverNow, disId, normalizedBatch, operatorUserId)));
        data.put("deliveryPageViewModel", DisRouteSandboxTodayViewModelMaps.toMap(
                buildDeliveryPageViewModel(compute, mergedPlan, data, drivers, executionStops,
                        serverNow, disId, normalizedBatch, operatorUserId)));
        data.put("pageHeader", pageViewModelMap.get("pageHeader"));
        data.put("topMetricsScope", pageViewModelMap.get("topMetricsScope"));
        data.put("topMetrics", pageViewModelMap.get("topMetrics"));
        data.put("sections", pageViewModelMap.get("sections"));
        data.put("availableDrivers", pageViewModelMap.get("availableDrivers"));
        attachDebugTraceToResponse(data, compute, pipelineTrace);
        stampSandboxTodayDebugContract(data);
        return data;
    }

    private void attachDebugTraceToResponse(Map<String, Object> data,
                                            SandboxComputeResult compute,
                                            SandboxTodayPipelineTrace pipelineTrace) {
        SandboxTodayPipelineTrace effectiveTrace = resolvePipelineTrace(compute, pipelineTrace);
        Map<String, Object> traceMap;
        if (effectiveTrace != null) {
            traceMap = effectiveTrace.toResponseMap();
        } else if (compute != null && compute.getDebugTrace() != null && !compute.getDebugTrace().isEmpty()) {
            traceMap = new LinkedHashMap<String, Object>(compute.getDebugTrace());
        } else {
            traceMap = SandboxTodayPipelineTrace.diagnosticStub(
                    "pipelineTrace was null at response assembly; check enablePipelineTrace wiring");
        }
        if (compute != null) {
            compute.setDebugTrace(traceMap);
        }
        data.put("debugTrace", traceMap);
        data.put("hasDebugTrace", Boolean.TRUE);
        data.put("traceKeys", new ArrayList<String>(traceMap.keySet()));
    }

    private static SandboxTodayPipelineTrace resolvePipelineTrace(SandboxComputeResult compute,
                                                                  SandboxTodayPipelineTrace pipelineTrace) {
        if (pipelineTrace != null) {
            return pipelineTrace;
        }
        return compute != null ? compute.getPipelineTrace() : null;
    }

    /** GET /sandbox/today（debug-only）响应内标记，防与正式 /dispatch/sandbox/today 混淆。 */
    private static void stampSandboxTodayDebugContract(Map<String, Object> data) {
        if (data == null) {
            return;
        }
        data.put("_debugOnly", Boolean.TRUE);
        data.put("_formalEndpoint", "/api/nxdisroutedispatch/dispatch/sandbox/today");
        data.put("_formalContract", "pageViewModel only");
        if (!data.containsKey("hasDebugTrace")) {
            data.put("hasDebugTrace", Boolean.FALSE);
            data.put("traceKeys", Collections.emptyList());
        }
    }

    private SandboxTodayPageBuildContext createPageBuildContext(SandboxComputeResult compute,
                                                                NxDisRoutePlanEntity mergedPlan,
                                                                Map<String, Object> data,
                                                                DispatchWorkbenchDto workbench,
                                                                DriverDispatchListResponse drivers,
                                                                int sandboxCustomerStopCount,
                                                                int sandboxConfirmedStopCount,
                                                                int loadingCustomerStopCount,
                                                                List<NxDisRouteStopEntity> loadingStops,
                                                                List<NxDisRouteStopEntity> executionStops,
                                                                Date serverNow,
                                                                Integer disId,
                                                                String batchCode,
                                                                Integer operatorUserId) {
        SandboxTodayPageBuildContext ctx = new SandboxTodayPageBuildContext();
        ctx.setDisId(disId);
        ctx.setBatchCode(batchCode);
        ctx.setOperatorUserId(disRouteDispatchOperatorResolver.resolve(disId, operatorUserId));
        ctx.setRouteDate(compute.getRouteDate());
        ctx.setRouteDateLabel(data.get("routeDateLabel") != null
                ? String.valueOf(data.get("routeDateLabel")) : compute.getRouteDate());
        ctx.setDispatchBatchLabel(data.get("dispatchBatchLabel") != null
                ? String.valueOf(data.get("dispatchBatchLabel")) : null);
        ctx.setScheduleBannerLine(data.get("scheduleBannerLine") != null
                ? String.valueOf(data.get("scheduleBannerLine")) : null);
        ctx.setFeasibilityStatus(data.get("feasibilityStatus") != null
                ? String.valueOf(data.get("feasibilityStatus")) : null);
        ctx.setCustomerStopCount(sandboxCustomerStopCount);
        ctx.setTotalCustomerStopCount(sandboxCustomerStopCount + loadingCustomerStopCount);
        ctx.setConfirmedCustomerStopCount(sandboxConfirmedStopCount + loadingCustomerStopCount);
        ctx.setServerNow(serverNow);
        ctx.setMergedPlan(mergedPlan);
        enrichDepotPresentation(ctx, disId);
        Object summaryObj = data.get("sandboxSummary");
        if (summaryObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> summary = (Map<String, Object>) summaryObj;
            ctx.setSandboxSummary(summary);
        }
        ctx.setWorkbench(workbench);
        ctx.setDrivers(drivers);
        ctx.setSuggestedStops(compute.getSandboxSuggestedStops());
        ctx.setProposalPlan(compute.getProposalPlan());
        if (compute.getProposalPlan() != null) {
            ctx.setUnassignedStops(SandboxProposalPlanBuilder.toStopEntities(
                    compute.getProposalPlan().getUnassignedStops()));
        } else {
            ctx.setUnassignedStops(compute.getUnassignedStops());
        }
        ctx.setConfirmedStops(compute.getConfirmedStops());
        ctx.setLoadingStops(loadingStops);
        ctx.setExecutionStops(executionStops);
        ctx.setInvalidStops(compute.getInvalidStops());
        return ctx;
    }

    private SandboxTodayPageViewModel buildPageViewModelFromContext(SandboxTodayPageBuildContext ctx,
                                                                    SandboxComputeResult compute,
                                                                    NxDisRoutePlanEntity mergedPlan,
                                                                    List<NxDisRouteStopEntity> loadingStops,
                                                                    List<NxDisRouteStopEntity> executionStops) {
        SandboxTodayPageViewModel viewModel = DisRouteSandboxTodayPageViewModelBuilder.build(ctx);
        mapRoadPolylineEnricher.enrichDriverRoutes(viewModel.getMapOverview());
        return viewModel;
    }

    private List<VisibleDriverRouteSnapshot> buildVisibleDriverRouteSnapshots(
            SandboxTodayPageBuildContext ctx,
            SandboxProposalPlan proposalPlan,
            DispatchAssignmentPlan assignmentPlan,
            NxDisRoutePlanEntity mergedPlan) {
        GeoPoint depot = resolveDepotForVisibleRouteSchedule(mergedPlan);
        if (proposalPlan != null) {
            return VisibleDriverRouteSnapshotBuilder.buildFromProposalPlan(
                    ctx, proposalPlan, assignmentPlan, depot, disRouteSandboxLegMetricsHelper);
        }
        return Collections.emptyList();
    }

    private SandboxTodayPageViewModel buildLoadingPageViewModel(SandboxComputeResult compute,
                                                                NxDisRoutePlanEntity mergedPlan,
                                                                Map<String, Object> data,
                                                                DriverDispatchListResponse drivers,
                                                                List<NxDisRouteStopEntity> loadingStops,
                                                                List<NxDisRouteStopEntity> executionStops,
                                                                Date serverNow,
                                                                Integer disId,
                                                                String batchCode,
                                                                Integer operatorUserId) {
        SandboxTodayPageBuildContext ctx = new SandboxTodayPageBuildContext();
        ctx.setDisId(disId);
        ctx.setBatchCode(batchCode);
        ctx.setOperatorUserId(disRouteDispatchOperatorResolver.resolve(disId, operatorUserId));
        ctx.setRouteDate(compute.getRouteDate());
        ctx.setRouteDateLabel(data.get("routeDateLabel") != null
                ? String.valueOf(data.get("routeDateLabel")) : compute.getRouteDate());
        ctx.setDispatchBatchLabel(data.get("dispatchBatchLabel") != null
                ? String.valueOf(data.get("dispatchBatchLabel")) : null);
        ctx.setScheduleBannerLine(data.get("scheduleBannerLine") != null
                ? String.valueOf(data.get("scheduleBannerLine")) : null);
        ctx.setServerNow(serverNow);
        ctx.setMergedPlan(mergedPlan);
        enrichDepotPresentation(ctx, disId);
        ctx.setDrivers(drivers);
        ctx.setLoadingStops(loadingStops);
        ctx.setExecutionStops(executionStops);
        if (mergedPlan != null) {
            DisRouteSandboxTodayPlanStopSyncHelper.syncFullScheduleFromPlan(
                    mergedPlan,
                    compute.getSandboxSuggestedStops(),
                    compute.getConfirmedStops(),
                    loadingStops,
                    executionStops,
                    compute.getUnassignedStops());
        }
        return DisRouteSandboxTodayPageViewModelBuilder.buildLoadingPage(ctx);
    }

    private SandboxTodayPageViewModel buildDeliveryPageViewModel(SandboxComputeResult compute,
                                                                 NxDisRoutePlanEntity mergedPlan,
                                                                 Map<String, Object> data,
                                                                 DriverDispatchListResponse drivers,
                                                                 List<NxDisRouteStopEntity> executionStops,
                                                                 Date serverNow,
                                                                 Integer disId,
                                                                 String batchCode,
                                                                 Integer operatorUserId) {
        SandboxTodayPageBuildContext ctx = new SandboxTodayPageBuildContext();
        ctx.setDisId(disId);
        ctx.setBatchCode(batchCode);
        ctx.setOperatorUserId(disRouteDispatchOperatorResolver.resolve(disId, operatorUserId));
        ctx.setRouteDate(compute.getRouteDate());
        ctx.setRouteDateLabel(data.get("routeDateLabel") != null
                ? String.valueOf(data.get("routeDateLabel")) : compute.getRouteDate());
        ctx.setDispatchBatchLabel(data.get("dispatchBatchLabel") != null
                ? String.valueOf(data.get("dispatchBatchLabel")) : null);
        ctx.setScheduleBannerLine(data.get("scheduleBannerLine") != null
                ? String.valueOf(data.get("scheduleBannerLine")) : null);
        ctx.setServerNow(serverNow);
        ctx.setMergedPlan(mergedPlan);
        enrichDepotPresentation(ctx, disId);
        ctx.setDrivers(drivers);
        ctx.setExecutionStops(executionStops);
        if (mergedPlan != null) {
            DisRouteSandboxTodayPlanStopSyncHelper.syncFullScheduleFromPlan(
                    mergedPlan,
                    compute.getSandboxSuggestedStops(),
                    compute.getConfirmedStops(),
                    compute.getLoadingStops(),
                    executionStops,
                    compute.getUnassignedStops());
        }
        return DisRouteSandboxTodayPageViewModelBuilder.buildDeliveryPage(ctx);
    }

    @Override
    public Map<String, Object> buildLoadingToday(Integer disId, String routeDate, String batchCode) throws Exception {
        return buildLoadingToday(disId, routeDate, batchCode, null);
    }

    @Override
    public Map<String, Object> buildLoadingToday(Integer disId, String routeDate, String batchCode,
                                                 Integer driverUserId) throws Exception {
        return buildLoadingToday(disId, routeDate, batchCode, driverUserId, null);
    }

    @Override
    public Map<String, Object> buildLoadingToday(Integer disId, String routeDate, String batchCode,
                                                 Integer driverUserId, Integer operatorUserId) throws Exception {
        return buildLoadingPageOnly(disId, routeDate, batchCode, operatorUserId);
    }

    @Override
    public Map<String, Object> buildLoadingSandboxToday(Integer disId, String routeDate, String batchCode,
                                                        Integer operatorUserId) throws Exception {
        return buildLoadingPageOnly(disId, routeDate, batchCode, operatorUserId);
    }

    /** 装车页专用：不跑完整 sandbox/today，避免优化与地图矩阵拖慢装车列表。 */
    private Map<String, Object> buildLoadingPageOnly(Integer disId,
                                                     String routeDate,
                                                     String batchCode,
                                                     Integer operatorUserId) throws Exception {
        disRouteDispatchDisIdGuard.logMissingDistributerIfNeeded(disId, "loading/today");

        SandboxComputeRequest computeRequest = new SandboxComputeRequest();
        computeRequest.setDisId(disId);
        computeRequest.setRouteDate(routeDate);
        computeRequest.setBatchCode(batchCode);
        computeRequest.setFormalPageContractMode(true);
        computeRequest.setPersistedRoutesOnlyMode(true);
        SandboxComputeResult compute = disRouteSandboxComputeService.compute(computeRequest);

        Map<String, Object> data = new LinkedHashMap<String, Object>();
        Date serverNow = new Date();
        String effectiveRouteDate = compute.getRouteDate();
        String normalizedBatch = compute.getDispatchBatch() != null ? compute.getDispatchBatch() : "MORNING";

        data.put("routeDate", effectiveRouteDate);
        data.put("routeDateLabel", DisRouteTemporalHelper.formatRouteDateLabel(effectiveRouteDate, serverNow));
        data.put("dispatchBatch", normalizedBatch);
        data.put("dispatchBatchLabel", DisRouteDispatchLabels.label(normalizedBatch));
        data.put("serverNow", RouteDispatchDateFormat.format(serverNow));

        NxDisRoutePlanEntity mergedPlan = compute.getMergedPlan();
        List<NxDisShipmentTaskEntity> tasks = compute.getAllDisplayTasks();
        DriverDispatchListResponse drivers = disRouteDriverDispatchListService.listDriversForBatch(
                disId, effectiveRouteDate, normalizedBatch, false);
        RouteFeasibilityResult feasibility = buildSandboxFeasibility(
                mergedPlan, compute, drivers, serverNow);
        appendMissingDisIdWarning(feasibility, disId);
        data.put("warnings", feasibility.getWarnings());

        List<NxDisRouteStopEntity> loadingStops = Collections.emptyList();
        List<NxDisRouteStopEntity> executionStops = Collections.emptyList();
        if (mergedPlan != null) {
            RouteDispatchReadModelAssembler.linkSharedTaskInstances(mergedPlan, tasks);
            DisRouteSandboxReadModelPartitionHelper.linkConfirmedStopsToTasks(
                    compute.getConfirmedStops(), tasks);
            disRouteDispatchOperationPolicy.enrichTasksReadModel(tasks, mergedPlan, feasibility);
            disRouteDispatchOperationPolicy.enrichPlanReadModel(mergedPlan, feasibility);

            DisRouteSandboxReadModelPartitionHelper.StopPartition stopPartition =
                    DisRouteSandboxReadModelPartitionHelper.partitionConfirmedStops(
                            compute.getConfirmedStops(),
                            DisRouteSandboxReadModelPartitionHelper.buildRouteIndex(mergedPlan));
            DisRouteSandboxReadModelPartitionHelper.partitionPlanRoutes(mergedPlan);
            reconcileSandboxPlanAfterPartition(mergedPlan, feasibility, stopPartition);
            disRouteDispatchOperationPolicy.enrichExecutionRoutesReadModel(mergedPlan, feasibility);
            disRouteDispatchOperationPolicy.enrichLoadingDriverRoutesReadModel(mergedPlan, feasibility);
            reconcileExecutionDeliveryReadModel(mergedPlan, stopPartition.executionStops);

            loadingStops = stopPartition.loadingStops;
            executionStops = stopPartition.executionStops;
        }

        int loadingCustomerStopCount = countLoadingCustomerStops(compute, loadingStops);
        applySandboxScheduleSummary(data, mergedPlan, serverNow, loadingCustomerStopCount > 0);
        applyDisplayShiftLabels(data, effectiveRouteDate, serverNow, mergedPlan, null, drivers);

        SandboxTodayPageViewModel loadingPageViewModel = buildLoadingPageViewModel(
                compute, mergedPlan, data, drivers, loadingStops, executionStops,
                serverNow, disId, normalizedBatch, operatorUserId);
        Map<String, Object> pageViewModelMap = DisRouteSandboxTodayViewModelMaps.toMap(loadingPageViewModel);
        data.put("pageViewModel", DisRouteSandboxTodayViewModelMaps.toLoadingPageMap(pageViewModelMap));
        return data;
    }

    private Map<String, Object> extractLoadingPageContract(Map<String, Object> full) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        if (full == null) {
            return data;
        }
        Object pageViewModel = full.get("pageViewModel");
        if (pageViewModel instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> contracted = DisRouteSandboxTodayViewModelMaps.toLoadingPageMap(
                    (Map<String, Object>) pageViewModel);
            data.put("pageViewModel", contracted);
        }
        return data;
    }

    @Override
    public Map<String, Object> buildDeliveryToday(Integer disId, String routeDate, String batchCode) throws Exception {
        return buildDeliveryToday(disId, routeDate, batchCode, null);
    }

    @Override
    public Map<String, Object> buildDeliveryToday(Integer disId, String routeDate, String batchCode,
                                                  Integer driverUserId) throws Exception {
        disRouteDispatchDisIdGuard.logMissingDistributerIfNeeded(disId, "delivery/today");
        Map<String, Object> full = buildToday(disId, routeDate, batchCode);
        return extractDeliverySlice(full, driverUserId);
    }

    private void appendMissingDisIdWarning(RouteFeasibilityResult feasibility, Integer disId) {
        String hint = disRouteDispatchDisIdGuard.missingDistributerHint(disId);
        if (hint == null || feasibility == null) {
            return;
        }
        if (feasibility.getWarnings() == null) {
            feasibility.setWarnings(new ArrayList<RouteDispatchWarning>());
        }
        RouteDispatchWarning warning = new RouteDispatchWarning();
        warning.setCode("DIS_ID_NOT_FOUND");
        warning.setSeverity("WARN");
        warning.setMessage(hint);
        feasibility.getWarnings().add(warning);
    }

    @Override
    public Map<String, Object> buildDispatchSandboxToday(Integer disId, String routeDate, String batchCode) throws Exception {
        return buildDispatchSandboxToday(disId, routeDate, batchCode, null);
    }

    @Override
    public Map<String, Object> buildDispatchSandboxToday(Integer disId, String routeDate, String batchCode,
                                                         Integer operatorUserId) throws Exception {
        Map<String, Object> full = buildToday(disId, routeDate, batchCode, operatorUserId, false);
        return extractDispatchPageContract(full);
    }

    /** 正式今日派单页：仅返回收缩后的 pageViewModel。 */
    private Map<String, Object> extractDispatchPageContract(Map<String, Object> full) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        if (full == null) {
            return data;
        }
        Object pageViewModel = full.get("pageViewModel");
        if (pageViewModel instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> contracted = DisRouteSandboxTodayViewModelMaps.toDispatchPageMap(
                    (Map<String, Object>) pageViewModel);
            if (!contracted.containsKey("mapOverview")) {
                contracted.put("mapOverview", DisRouteSandboxTodayViewModelMaps.defaultDispatchMapOverview());
            }
            data.put("pageViewModel", contracted);
        }
        return data;
    }

    private Map<String, Object> extractLoadingSlice(Map<String, Object> full, Integer disId, Integer driverUserId,
                                                    Integer operatorUserId) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        if (full == null) {
            return data;
        }
        copyIfPresent(full, data, "routeDate", "routeDateLabel", "dispatchBatch", "dispatchBatchLabel",
                "serverNow", "warnings");
        Object loadingPageViewModel = full.get("loadingPageViewModel");
        if (!(loadingPageViewModel instanceof Map)) {
            loadingPageViewModel = full.get("pageViewModel");
        }
        if (loadingPageViewModel instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> contracted = DisRouteSandboxTodayViewModelMaps.toLoadingPageMap(
                    (Map<String, Object>) loadingPageViewModel);
            data.put("pageViewModel", contracted);
        }
        return data;
    }

    private Map<String, Object> extractDeliverySlice(Map<String, Object> full, Integer driverUserId) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        if (full == null) {
            return data;
        }
        copyIfPresent(full, data, "routeDate", "routeDateLabel", "dispatchBatch", "dispatchBatchLabel",
                "serverNow", "warnings");
        Object deliveryPageViewModel = full.get("deliveryPageViewModel");
        if (deliveryPageViewModel instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> contracted = DisRouteSandboxTodayViewModelMaps.toDeliveryPageMap(
                    (Map<String, Object>) deliveryPageViewModel);
            if (driverUserId != null) {
                contracted = DisRouteSandboxTodayViewModelMaps.filterDeliveryPageViewModelByDriver(
                        contracted, driverUserId);
            }
            data.put("pageViewModel", contracted);
        }
        return data;
    }

    private DriverDispatchListResponse sanitizeDriversForSandboxSlice(Object driversObj) {
        if (!(driversObj instanceof DriverDispatchListResponse)) {
            return null;
        }
        DriverDispatchListResponse src = (DriverDispatchListResponse) driversObj;
        DriverDispatchListResponse copy = new DriverDispatchListResponse();
        copy.setRouteDate(src.getRouteDate());
        copy.setDispatchBatch(src.getDispatchBatch());
        copy.setDispatchBatchLabel(src.getDispatchBatchLabel());
        copy.setSummary(src.getSummary());
        if (src.getDrivers() == null) {
            copy.setDrivers(Collections.<DriverDispatchCandidateDto>emptyList());
            return copy;
        }
        List<DriverDispatchCandidateDto> sanitized = new ArrayList<DriverDispatchCandidateDto>();
        for (DriverDispatchCandidateDto driver : src.getDrivers()) {
            if (driver == null) {
                continue;
            }
            sanitized.add(sanitizeDriverForSandboxSlice(driver));
        }
        copy.setDrivers(sanitized);
        return copy;
    }

    private DriverDispatchCandidateDto sanitizeDriverForSandboxSlice(DriverDispatchCandidateDto src) {
        DriverDispatchCandidateDto dto = new DriverDispatchCandidateDto();
        dto.setDriverUserId(src.getDriverUserId());
        dto.setDriverName(src.getDriverName());
        dto.setDriverPhone(src.getDriverPhone());
        dto.setDutyStatus(src.getDutyStatus());
        dto.setDutyStatusLabel(src.getDutyStatusLabel());
        dto.setCheckInAt(src.getCheckInAt());
        dto.setCheckOutAt(src.getCheckOutAt());
        dto.setDispatchBatch(src.getDispatchBatch());
        dto.setDispatchBatchLabel(src.getDispatchBatchLabel());
        dto.setBatchEligible(src.getBatchEligible());
        dto.setBatchEligibleLabel(src.getBatchEligibleLabel());
        dto.setIneligibleReason(src.getIneligibleReason());
        dto.setIneligibleReasonLabel(src.getIneligibleReasonLabel());
        dto.setCanToggleDutyOff(src.getCanToggleDutyOff());
        dto.setDutyOffLockReason(src.getDutyOffLockReason());
        dto.setToggleDutyOffAction(src.getToggleDutyOffAction());
        dto.setConfirmedStopCount(src.getConfirmedStopCount());
        dto.setSandboxSuggestedStopCount(src.getSandboxSuggestedStopCount());
        dto.setCurrentStopCount(src.getCurrentStopCount());
        dto.setCurrentLateMinutes(src.getCurrentLateMinutes());
        dto.setCurrentWaitMinutes(src.getCurrentWaitMinutes());
        dto.setCurrentFeasibilityStatus(src.getCurrentFeasibilityStatus());
        dto.setCurrentFeasibilityStatusLabel(src.getCurrentFeasibilityStatusLabel());
        if (DisRouteSandboxDriverDispatchStateHelper.shouldSanitizeAsActiveExecutionSlice(src)) {
            dto.setBatchEligible(false);
            dto.setBatchEligibleLabel("配送中");
            dto.setIneligibleReason("IN_DELIVERY");
            dto.setIneligibleReasonLabel("配送中");
            dto.setCurrentRouteId(null);
            dto.setOperationHint(null);
            dto.setConfirmedStopCount(null);
            dto.setCurrentStopCount(null);
            dto.setSandboxSuggestedStopCount(null);
            dto.setCurrentLateMinutes(null);
            dto.setCurrentWaitMinutes(null);
            dto.setCurrentFeasibilityStatus(null);
            dto.setCurrentFeasibilityStatusLabel(null);
        } else if (DisRouteSandboxDriverDispatchPhase.LOADING.equals(
                DisRouteSandboxDriverDispatchStateHelper.resolveDriverMetaPhase(src))) {
            dto.setBatchEligible(false);
            dto.setBatchEligibleLabel("装车中");
            dto.setIneligibleReason(DisRouteBatchEligibility.INELIGIBLE_LOADING);
            dto.setIneligibleReasonLabel("装车中");
            dto.setCurrentRouteId(null);
            dto.setOperationHint(null);
            dto.setConfirmedStopCount(null);
            dto.setCurrentStopCount(null);
            dto.setSandboxSuggestedStopCount(null);
            dto.setCurrentLateMinutes(null);
            dto.setCurrentWaitMinutes(null);
            dto.setCurrentFeasibilityStatus(null);
            dto.setCurrentFeasibilityStatusLabel(null);
        } else {
            dto.setCurrentRouteId(src.getCurrentRouteId());
            dto.setOperationHint(src.getOperationHint());
        }
        return dto;
    }

    private SandboxActionPermissionsDto sanitizeActionPermissionsForSandboxSlice(Object permissionsObj) {
        SandboxActionPermissionsDto sandboxOnly = new SandboxActionPermissionsDto();
        if (permissionsObj instanceof SandboxActionPermissionsDto) {
            SandboxActionPermissionsDto src = (SandboxActionPermissionsDto) permissionsObj;
            sandboxOnly.setCanConfirmCustomer(src.isCanConfirmCustomer());
        } else {
            sandboxOnly.setCanConfirmCustomer(true);
        }
        sandboxOnly.setCanPrintBill(false);
        sandboxOnly.setCanDepart(false);
        return sandboxOnly;
    }

    private void copyIfPresent(Map<String, Object> from, Map<String, Object> to, String... keys) {
        for (String key : keys) {
            if (from.containsKey(key)) {
                to.put(key, from.get(key));
            }
        }
    }

    private void applySandboxScheduleSummary(Map<String, Object> data,
                                             NxDisRoutePlanEntity mergedPlan,
                                             Date serverNow,
                                             boolean hasScheduledStops) {
        if (!hasScheduledStops || mergedPlan == null || mergedPlan.getDriverRoutes() == null) {
            return;
        }
        String planMode = DisRouteSandboxScheduleMode.SCHEDULED_BATCH;
        java.util.Date fastestArrival = null;
        boolean anyAfterWindow = false;
        for (com.nongxinle.entity.NxDisDriverRouteEntity route : mergedPlan.getDriverRoutes()) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop == null) {
                    continue;
                }
                if (DisRouteSandboxScheduleMode.ADHOC_NOW.equals(stop.getScheduleMode())) {
                    planMode = DisRouteSandboxScheduleMode.ADHOC_NOW;
                }
                if (Boolean.TRUE.equals(stop.getIsAfterCustomerWindow())) {
                    anyAfterWindow = true;
                }
                if (stop.getNxDrsPlannedArrivalAt() != null
                        && (fastestArrival == null || stop.getNxDrsPlannedArrivalAt().before(fastestArrival))) {
                    fastestArrival = stop.getNxDrsPlannedArrivalAt();
                }
            }
        }
        data.put("sandboxScheduleMode", planMode);
        data.put("sandboxScheduleModeLabel", DisRouteSandboxScheduleLabelHelper.modeLabel(planMode));
        if (DisRouteSandboxScheduleMode.ADHOC_NOW.equals(planMode)) {
            data.put("scheduleBannerLine", DisRouteSandboxScheduleLabelHelper.formatPlanScheduleBanner(
                    planMode, fastestArrival, serverNow));
            data.put("scheduleBasisLabel", "当前按最快送达计算");
            if (anyAfterWindow) {
                data.put("scheduleWindowHint", "已超常规送达窗口");
            }
            mergedPlan.setPlanTemporalStatus(com.nongxinle.route.DisRoutePlanTemporalStatus.ACTIVE);
            mergedPlan.setPlanTemporalStatusLabel(DisRouteDispatchLabels.label(
                    com.nongxinle.route.DisRoutePlanTemporalStatus.ACTIVE));
        }
    }

    /**
     * 今日路线日：按服务器当前时刻展示早/中/晚班文案，覆盖 DB dispatchBatch 的「早班」默认标签。
     * ADHOC_NOW 临时补单模式统一为「临时补单｜现在可送」，不展示早/晚班分段。
     */
    private void applyDisplayShiftLabels(Map<String, Object> data,
                                         String routeDate,
                                         Date serverNow,
                                         NxDisRoutePlanEntity mergedPlan,
                                         DispatchWorkbenchDto workbench,
                                         DriverDispatchListResponse drivers) {
        String planMode = data.get("sandboxScheduleMode") != null
                ? String.valueOf(data.get("sandboxScheduleMode")) : "";
        if (DisRouteSandboxScheduleMode.ADHOC_NOW.equals(planMode)) {
            String adhocLabel = DisRouteSandboxScheduleLabelHelper.modeLabel(planMode) + "｜现在可送";
            data.put("displayShiftCode", DisRouteSandboxScheduleMode.ADHOC_NOW);
            data.put("displayShiftLabel", adhocLabel);
            data.put("dispatchBatchLabel", adhocLabel);
            if (mergedPlan != null) {
                mergedPlan.setNxDrpDispatchBatchLabel(adhocLabel);
            }
            if (workbench != null) {
                workbench.setDispatchBatchLabel(adhocLabel);
            }
            if (drivers != null) {
                drivers.setDispatchBatchLabel(adhocLabel);
            }
            return;
        }
        if (!DisRouteDaySegmentHelper.isRouteDateToday(routeDate, serverNow)) {
            return;
        }
        String segmentCode = DisRouteDaySegmentHelper.resolveSegmentCode(serverNow);
        String segmentLabel = DisRouteDaySegmentHelper.resolveSegmentLabel(segmentCode);
        data.put("displayShiftCode", segmentCode);
        data.put("displayShiftLabel", segmentLabel);
        data.put("dispatchBatchLabel", segmentLabel);
        if (mergedPlan != null) {
            mergedPlan.setNxDrpDispatchBatchLabel(segmentLabel);
        }
        if (workbench != null) {
            workbench.setDispatchBatchLabel(segmentLabel);
        }
        if (drivers != null) {
            drivers.setDispatchBatchLabel(segmentLabel);
        }
    }

    private void enrichSandboxDriverStopCounts(DriverDispatchListResponse drivers,
                                               NxDisRoutePlanEntity mergedPlan,
                                               List<NxDisRouteStopEntity> confirmedStops) {
        if (drivers == null || drivers.getDrivers() == null) {
            return;
        }
        Map<Integer, Integer> suggestedByDriver = new HashMap<Integer, Integer>();
        if (mergedPlan != null && mergedPlan.getDriverRoutes() != null) {
            for (com.nongxinle.entity.NxDisDriverRouteEntity route : mergedPlan.getDriverRoutes()) {
                accumulateSuggestedByDriver(route, suggestedByDriver);
            }
        }
        Map<Integer, Integer> confirmedByDriver = new HashMap<Integer, Integer>();
        if (confirmedStops != null) {
            for (NxDisRouteStopEntity stop : confirmedStops) {
                if (stop == null) {
                    continue;
                }
                Integer driverId = stop.getSuggestedDriverUserId();
                if (driverId == null && stop.getShipmentTask() != null) {
                    driverId = stop.getShipmentTask().getNxDstAssignedDriverUserId();
                    if (driverId == null) {
                        driverId = stop.getShipmentTask().getNxDstSuggestedDriverUserId();
                    }
                }
                if (driverId == null) {
                    continue;
                }
                confirmedByDriver.put(driverId, confirmedByDriver.containsKey(driverId)
                        ? confirmedByDriver.get(driverId) + 1 : 1);
            }
        }
        for (DriverDispatchCandidateDto dto : drivers.getDrivers()) {
            if (dto == null || dto.getDriverUserId() == null) {
                continue;
            }
            int confirmed = confirmedByDriver.containsKey(dto.getDriverUserId())
                    ? confirmedByDriver.get(dto.getDriverUserId())
                    : (dto.getCurrentStopCount() != null ? dto.getCurrentStopCount() : 0);
            int suggested = suggestedByDriver.containsKey(dto.getDriverUserId())
                    ? suggestedByDriver.get(dto.getDriverUserId()) : 0;
            dto.setConfirmedStopCount(confirmed);
            dto.setSandboxSuggestedStopCount(suggested);
            dto.setCurrentStopCount(confirmed);
            overlayDepartFieldsFromPlan(dto, mergedPlan);
        }
    }

    private void overlayDepartFieldsFromPlan(DriverDispatchCandidateDto dto, NxDisRoutePlanEntity mergedPlan) {
        if (dto == null || mergedPlan == null) {
            return;
        }
        if (overlayDepartFieldsFromRouteList(dto, mergedPlan.getExecutionDriverRoutes())) {
            return;
        }
        overlayDepartFieldsFromRouteList(dto, mergedPlan.getDriverRoutes());
    }

    private boolean overlayDepartFieldsFromRouteList(DriverDispatchCandidateDto dto,
                                                     List<com.nongxinle.entity.NxDisDriverRouteEntity> routes) {
        if (routes == null) {
            return false;
        }
        for (com.nongxinle.entity.NxDisDriverRouteEntity route : routes) {
            if (route == null || route.getNxDdrDriverUserId() == null) {
                continue;
            }
            if (!route.getNxDdrDriverUserId().equals(dto.getDriverUserId())) {
                continue;
            }
            String routeStatus = DisRouteRouteExecutionHelper.resolveRouteStatus(route);
            dto.setTotalStopCount(route.getTotalStopCount());
            dto.setConfirmedStopCount(route.getConfirmedStopCount());
            dto.setCanDepart(route.getCanDepart());
            dto.setDepartActionLabel(route.getDepartActionLabel());
            dto.setDepartBlockedReason(route.getDepartBlockedReason());
            dto.setDepartConfirmMessage(route.getDepartConfirmMessage());
            dto.setDepartWarning(route.getDepartWarning());
            dto.setUnprintedBillCount(route.getUnprintedBillCount());
            DisRouteSandboxDriverDispatchStateHelper.applyDriverListOverlay(
                    dto, route, nxDisDriverRouteDao, nxDisShipmentTaskDao);
            if (dto.getRouteStatus() == null && routeStatus != null
                    && !DisRouteDriverRouteStatus.isTerminal(routeStatus.trim().toUpperCase())) {
                dto.setRouteStatus(routeStatus);
                dto.setRouteStatusLabel(DisRouteRouteExecutionHelper.resolveRouteStatusLabel(routeStatus));
            }
            if (route.getNxDdrActualDepartAt() != null) {
                String formatted = RouteDispatchDateFormat.format(route.getNxDdrActualDepartAt());
                dto.setActualDepartAt(formatted);
                dto.setDepartedAt(formatted);
            } else if (dto.getActualDepartAt() != null) {
                dto.setDepartedAt(dto.getActualDepartAt());
            }
            return true;
        }
        return false;
    }

    private static void accumulateSuggestedByDriver(com.nongxinle.entity.NxDisDriverRouteEntity route,
                                                    Map<Integer, Integer> suggestedByDriver) {
        if (route == null || route.getNxDdrDriverUserId() == null) {
            return;
        }
        int suggested = route.getSuggestedStopCount() != null
                ? route.getSuggestedStopCount()
                : (route.getStops() != null ? route.getStops().size() : 0);
        suggestedByDriver.put(route.getNxDdrDriverUserId(), suggested);
    }

    private void reconcileExecutionDeliveryReadModel(NxDisRoutePlanEntity plan,
                                                     List<NxDisRouteStopEntity> executionStops) {
        if (plan == null) {
            return;
        }
        if (plan.getExecutionDriverRoutes() == null) {
            plan.setExecutionDriverRoutes(new ArrayList<NxDisDriverRouteEntity>());
        }
        if (executionStops == null || executionStops.isEmpty()) {
            for (NxDisDriverRouteEntity route : plan.getExecutionDriverRoutes()) {
                if (route != null) {
                    DisRouteRouteExecutionHelper.syncExecutionCanonicalFields(route);
                }
            }
            return;
        }

        Map<Integer, NxDisDriverRouteEntity> routeById = new HashMap<Integer, NxDisDriverRouteEntity>();
        Map<Integer, NxDisDriverRouteEntity> routeByDriver = new HashMap<Integer, NxDisDriverRouteEntity>();
        indexExecutionRoutes(plan.getExecutionDriverRoutes(), routeById, routeByDriver);

        for (NxDisRouteStopEntity stop : executionStops) {
            if (stop == null) {
                continue;
            }
            DisRouteSandboxReadModelPartitionHelper.applyExecutionStopReadOnlyOverlay(stop);
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            Integer routeId = task != null && task.getNxDstDriverRouteId() != null
                    ? task.getNxDstDriverRouteId() : stop.getNxDrsDriverRouteId();
            Integer driverUserId = task != null && task.getNxDstAssignedDriverUserId() != null
                    ? task.getNxDstAssignedDriverUserId()
                    : (task != null ? task.getNxDstSuggestedDriverUserId() : null);

            NxDisDriverRouteEntity route = routeId != null ? routeById.get(routeId) : null;
            if (route == null && driverUserId != null) {
                route = routeByDriver.get(driverUserId);
            }
            if (route == null) {
                route = buildExecutionRouteShellFromStop(stop, task, routeId, driverUserId);
                plan.getExecutionDriverRoutes().add(route);
                if (route.getNxDdrId() != null) {
                    routeById.put(route.getNxDdrId(), route);
                }
                if (route.getNxDdrDriverUserId() != null) {
                    routeByDriver.put(route.getNxDdrDriverUserId(), route);
                }
            }
            mergeExecutionStopOntoRoute(route, stop);
        }

        for (NxDisDriverRouteEntity route : plan.getExecutionDriverRoutes()) {
            if (route == null) {
                continue;
            }
            DisRouteSandboxReadModelPartitionHelper.applyExecutionRouteReadOnlyOverlay(route);
            DisRouteRouteExecutionHelper.syncExecutionCanonicalFields(route);
        }
    }

    private static void indexExecutionRoutes(List<NxDisDriverRouteEntity> routes,
                                             Map<Integer, NxDisDriverRouteEntity> routeById,
                                             Map<Integer, NxDisDriverRouteEntity> routeByDriver) {
        if (routes == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null) {
                continue;
            }
            if (route.getNxDdrId() != null) {
                routeById.put(route.getNxDdrId(), route);
            }
            if (route.getNxDdrDriverUserId() != null) {
                routeByDriver.put(route.getNxDdrDriverUserId(), route);
            }
        }
    }

    private NxDisDriverRouteEntity buildExecutionRouteShellFromStop(NxDisRouteStopEntity stop,
                                                                    NxDisShipmentTaskEntity task,
                                                                    Integer routeId,
                                                                    Integer driverUserId) {
        NxDisDriverRouteEntity route = new NxDisDriverRouteEntity();
        route.setNxDdrId(routeId);
        route.setNxDdrDriverUserId(driverUserId);
        if (task != null) {
            route.setNxDdrPlanId(task.getNxDstPlanId());
        }
        route.setStops(new ArrayList<NxDisRouteStopEntity>());
        if (stop != null && stop.getNxDrsStopSeq() != null) {
            route.setNxDdrRouteSeq(stop.getNxDrsStopSeq());
        }
        return route;
    }

    private static void mergeExecutionStopOntoRoute(NxDisDriverRouteEntity route, NxDisRouteStopEntity stop) {
        if (route == null || stop == null) {
            return;
        }
        if (route.getStops() == null) {
            route.setStops(new ArrayList<NxDisRouteStopEntity>());
        }
        Integer taskId = stop.getNxDrsShipmentTaskId();
        if (taskId == null && stop.getShipmentTask() != null) {
            taskId = stop.getShipmentTask().getNxDstId();
        }
        for (NxDisRouteStopEntity existing : route.getStops()) {
            if (existing == null) {
                continue;
            }
            Integer existingTaskId = existing.getNxDrsShipmentTaskId();
            if (existingTaskId == null && existing.getShipmentTask() != null) {
                existingTaskId = existing.getShipmentTask().getNxDstId();
            }
            if (taskId != null && taskId.equals(existingTaskId)) {
                if (existing.getShipmentTask() == null && stop.getShipmentTask() != null) {
                    existing.setShipmentTask(stop.getShipmentTask());
                }
                return;
            }
        }
        route.getStops().add(stop);
        route.setNxDdrStopCount(route.getStops().size());
        route.setTotalStopCount(route.getStops().size());
        route.setConfirmedStopCount(route.getStops().size());
    }

    private void reconcileSandboxPlanAfterPartition(NxDisRoutePlanEntity plan,
                                                  RouteFeasibilityResult feasibility,
                                                  DisRouteSandboxReadModelPartitionHelper.StopPartition stopPartition) {
        if (plan == null) {
            return;
        }
        Date serverNow = new Date();
        if (plan.getExecutionDriverRoutes() != null) {
            for (com.nongxinle.entity.NxDisDriverRouteEntity route : plan.getExecutionDriverRoutes()) {
                DisRouteTemporalHelper.enrichDriverRouteOperationFields(route, plan);
                if (route.getStops() != null) {
                    for (NxDisRouteStopEntity stop : route.getStops()) {
                        DisRouteTemporalHelper.enrichStopTemporalFields(stop, serverNow);
                    }
                }
            }
        }
        if (plan.getLoadingDriverRoutes() != null) {
            for (com.nongxinle.entity.NxDisDriverRouteEntity route : plan.getLoadingDriverRoutes()) {
                DisRouteTemporalHelper.enrichDriverRouteOperationFields(route, plan);
                if (route.getStops() != null) {
                    for (NxDisRouteStopEntity stop : route.getStops()) {
                        DisRouteTemporalHelper.enrichStopTemporalFields(stop, serverNow);
                    }
                }
            }
        }
        boolean anyLoadingCanLoad = false;
        if (plan.getLoadingDriverRoutes() != null) {
            for (com.nongxinle.entity.NxDisDriverRouteEntity route : plan.getLoadingDriverRoutes()) {
                if (Boolean.TRUE.equals(route.getCanLoad()) || Boolean.TRUE.equals(route.getCanDepart())) {
                    anyLoadingCanLoad = true;
                    break;
                }
            }
        }
        if (DisRouteSandboxReadModelPartitionHelper.hasLoadingRoutes(plan)) {
            plan.setCanStartLoading(anyLoadingCanLoad);
            if (!anyLoadingCanLoad) {
                plan.setLoadingBlockedReason("装车未完成，暂不可出发");
            }
        } else if (DisRouteSandboxReadModelPartitionHelper.hasExecutionRoutes(plan)) {
            plan.setCanStartLoading(false);
            plan.setLoadingBlockedReason("司机已出发，请前往配送任务页查看");
            plan.setOperationHint("配送中路线请查看 executionDriverRoutes / executionStops");
        }
        if (stopPartition != null && stopPartition.sandboxStops.isEmpty()
                && stopPartition.loadingStops.isEmpty()
                && !stopPartition.executionStops.isEmpty()) {
            plan.setCanStartLoading(false);
            plan.setLoadingBlockedReason("司机已出发，沙箱不可再装车或改派");
            plan.setOperationHint("当前批次配送执行中");
        }
        recalculateSandboxPlanMetrics(plan);
    }

    /** 沙箱页 plan 级里程/时长只汇总 sandbox driverRoutes，不含 execution。 */
    private void recalculateSandboxPlanMetrics(NxDisRoutePlanEntity plan) {
        if (plan == null) {
            return;
        }
        long totalDistanceM = 0L;
        long totalDurationS = 0L;
        if (plan.getDriverRoutes() != null) {
            for (com.nongxinle.entity.NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
                if (route == null) {
                    continue;
                }
                DisRouteSandboxReadModelPartitionHelper.ensureRouteMetricsCanonical(route);
                if (route.getNxDdrTotalDistanceM() != null) {
                    totalDistanceM += route.getNxDdrTotalDistanceM();
                }
                if (route.getNxDdrTotalDurationS() != null) {
                    totalDurationS += route.getNxDdrTotalDurationS();
                }
            }
        }
        plan.setNxDrpTotalDistanceM(totalDistanceM);
        plan.setNxDrpTotalDurationS(totalDurationS);
    }

    private void applyExecutionOnlyWorkbench(DispatchWorkbenchDto workbench,
                                             NxDisRoutePlanEntity plan,
                                             String routeDate,
                                             Date serverNow) {
        if (workbench == null) {
            return;
        }
        String dateLabel = plan != null && plan.getRouteDateLabel() != null
                ? plan.getRouteDateLabel()
                : DisRouteTemporalHelper.formatRouteDateLabel(routeDate, serverNow);
        workbench.setTitle((dateLabel != null ? dateLabel : routeDate) + " · 配送沙箱");
        workbench.setSubtitle("暂无待分派客户");
        workbench.setOperationHint("司机已出发，沙箱不可再装车或改派；请在配送任务页查看执行中路线");
        workbench.setPrimaryReason("司机已出发，沙箱不可再装车或改派");
        workbench.setStatus(com.nongxinle.route.DisRouteWorkbenchStatus.EMPTY);
        workbench.setStatusLabel("暂无待分派客户");
        workbench.setSeverity("INFO");
        workbench.setNextActions(Collections.<DispatchWorkbenchActionDto>emptyList());
    }

    private void applyLoadingOnlySandboxWorkbench(DispatchWorkbenchDto workbench,
                                                  NxDisRoutePlanEntity plan,
                                                  String routeDate,
                                                  Date serverNow) {
        if (workbench == null) {
            return;
        }
        String dateLabel = plan != null && plan.getRouteDateLabel() != null
                ? plan.getRouteDateLabel()
                : DisRouteTemporalHelper.formatRouteDateLabel(routeDate, serverNow);
        workbench.setTitle((dateLabel != null ? dateLabel : routeDate) + " · 配送沙箱");
        workbench.setSubtitle("暂无待分派客户");
        workbench.setOperationHint("客户已确认分派，请前往司机装车页完成装车");
        workbench.setPrimaryReason("今日派车已完成，装车阶段请查看 loadingDriverRoutes");
        workbench.setStatus(com.nongxinle.route.DisRouteWorkbenchStatus.EMPTY);
        workbench.setStatusLabel("暂无待分派客户");
        workbench.setSeverity("INFO");
        workbench.setNextActions(Collections.<DispatchWorkbenchActionDto>emptyList());
    }

    private int countSandboxCustomerStops(SandboxComputeResult compute) {
        if (compute == null) {
            return 0;
        }
        SandboxProposalPlan proposalPlan = compute.getProposalPlan();
        if (proposalPlan != null && proposalPlan.getSummary() != null) {
            return proposalPlan.getSummary().getCustomerStopCount();
        }
        return DisRouteSandboxUnassignedStopHelper.countUniqueCustomerStops(
                compute.getSandboxSuggestedStops(),
                compute.getUnassignedStops(),
                Collections.<NxDisRouteStopEntity>emptyList());
    }

    private static int countProposalCustomerStops(SandboxProposalPlan proposalPlan) {
        if (proposalPlan == null || proposalPlan.getSummary() == null) {
            return 0;
        }
        return proposalPlan.getSummary().getCustomerStopCount();
    }

    private int countLoadingCustomerStops(SandboxComputeResult compute,
                                          List<NxDisRouteStopEntity> loadingStops) {
        if (loadingStops != null && !loadingStops.isEmpty()) {
            return loadingStops.size();
        }
        if (compute == null || compute.getMergedPlan() == null) {
            return 0;
        }
        int count = 0;
        List<com.nongxinle.entity.NxDisDriverRouteEntity> routes =
                compute.getMergedPlan().getLoadingDriverRoutes();
        if (routes == null) {
            return 0;
        }
        for (com.nongxinle.entity.NxDisDriverRouteEntity route : routes) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            count += route.getStops().size();
        }
        return count;
    }

    private SandboxActionPermissionsDto buildActionPermissions(SandboxComputeResult compute,
                                                             List<NxDisShipmentTaskEntity> tasks,
                                                             NxDisRoutePlanEntity plan,
                                                             List<NxDisRouteStopEntity> loadingStops) {
        SandboxActionPermissionsDto permissions = new SandboxActionPermissionsDto();
        permissions.setCanConfirmCustomer(!DisRouteSandboxReadModelPartitionHelper.hasExecutionRoutes(plan)
                || DisRouteSandboxReadModelPartitionHelper.hasSandboxOperableRoutes(plan));
        boolean hasAssigned = false;
        boolean allReady = true;
        if (tasks != null) {
            for (NxDisShipmentTaskEntity task : tasks) {
                if (com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY.equals(task.getNxDstStatus())
                        || com.nongxinle.route.DisShipmentTaskStatus.DELIVERED.equals(task.getNxDstStatus())) {
                    continue;
                }
                if (com.nongxinle.route.DisShipmentTaskStatus.ASSIGNED.equals(task.getNxDstStatus())) {
                    hasAssigned = true;
                    allReady = false;
                } else if (!com.nongxinle.route.DisShipmentTaskStatus.READY_TO_GO.equals(task.getNxDstStatus())) {
                    allReady = false;
                }
            }
        }
        permissions.setCanPrintBill(hasAssigned);
        boolean canDepart = loadingStops != null && !loadingStops.isEmpty();
        if (plan != null && plan.getLoadingDriverRoutes() != null) {
            for (com.nongxinle.entity.NxDisDriverRouteEntity route : plan.getLoadingDriverRoutes()) {
                if (Boolean.TRUE.equals(route.getCanDepart())) {
                    canDepart = true;
                    break;
                }
            }
        }
        permissions.setCanDepart(canDepart);
        return permissions;
    }

    private static Map<String, Object> buildRouteMetricsSummary(
            List<com.nongxinle.entity.NxDisDriverRouteEntity> routes) {
        Map<String, Object> summary = new LinkedHashMap<String, Object>();
        long totalDistanceM = 0L;
        long totalDurationS = 0L;
        int customerStopCount = 0;
        int driverCount = 0;
        if (routes != null) {
            for (com.nongxinle.entity.NxDisDriverRouteEntity route : routes) {
                if (route == null) {
                    continue;
                }
                DisRouteSandboxReadModelPartitionHelper.ensureRouteMetricsCanonical(route);
                int stopCount = DisRouteExecutionRouteSnapshotHelper.resolveEffectiveStopCount(route);
                if (stopCount <= 0) {
                    continue;
                }
                driverCount++;
                customerStopCount += stopCount;
                if (route.getNxDdrTotalDistanceM() != null) {
                    totalDistanceM += route.getNxDdrTotalDistanceM();
                }
                if (route.getNxDdrTotalDurationS() != null) {
                    totalDurationS += route.getNxDdrTotalDurationS();
                }
            }
        }
        summary.put("totalRouteDistanceM", totalDistanceM);
        summary.put("totalRouteDurationS", totalDurationS);
        summary.put("customerStopCount", customerStopCount);
        summary.put("driverCount", driverCount);
        return summary;
    }

    /** 沙盘顶部统计：按 ephemeral stop 的 leg 字段汇总，与 sandboxSuggestedStops 展示一致。 */
    private static Map<String, Object> buildSandboxMetricsSummary(
            SandboxComputeResult compute,
            int customerStopCount,
            DriverDispatchListResponse drivers) {
        Map<String, Object> summary = new LinkedHashMap<String, Object>();
        long[] legTotals = new long[2];
        Set<Integer> assignedDrivers = new HashSet<Integer>();

        accumulateSandboxStopLegMetrics(compute != null ? compute.getSandboxSuggestedStops() : null,
                legTotals, assignedDrivers);
        accumulateSandboxStopLegMetrics(compute != null ? compute.getConfirmedStops() : null,
                legTotals, assignedDrivers);
        accumulateSandboxStopLegMetrics(compute != null ? compute.getUnassignedStops() : null,
                legTotals, assignedDrivers);

        int eligibleDriverCount = countBatchEligibleDrivers(drivers);

        summary.put("customerStopCount", customerStopCount);
        summary.put("totalRouteDistanceM", legTotals[0]);
        summary.put("totalRouteDurationS", legTotals[1]);
        summary.put("driverCount", assignedDrivers.size());
        summary.put("eligibleDriverCount", eligibleDriverCount);
        summary.put("availableDriverCount", eligibleDriverCount);
        return summary;
    }

    private static void accumulateSandboxStopLegMetrics(List<NxDisRouteStopEntity> stops,
                                                          long[] legTotals,
                                                          Set<Integer> assignedDrivers) {
        if (stops == null || legTotals == null || legTotals.length < 2) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Long legDistanceM = stop.getNxDrsLegDistanceM();
            Long legDurationS = stop.getNxDrsLegDurationS();
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (legDistanceM == null && task != null) {
                legDistanceM = task.getNxDstLegDistanceM();
            }
            if (legDurationS == null && task != null) {
                legDurationS = task.getNxDstLegDurationS();
            }
            if (legDistanceM != null) {
                legTotals[0] += legDistanceM;
            }
            if (legDurationS != null) {
                legTotals[1] += legDurationS;
            }
            Integer driverId = stop.getSuggestedDriverUserId();
            if (driverId == null && task != null) {
                driverId = task.getNxDstSuggestedDriverUserId();
                if (driverId == null) {
                    driverId = task.getSuggestedDriverUserId();
                }
            }
            if (driverId != null && assignedDrivers != null) {
                assignedDrivers.add(driverId);
            }
        }
    }

    private static int countBatchEligibleDrivers(DriverDispatchListResponse drivers) {
        if (drivers == null || drivers.getDrivers() == null) {
            return 0;
        }
        int count = 0;
        for (DriverDispatchCandidateDto driver : drivers.getDrivers()) {
            if (driver != null && Boolean.TRUE.equals(driver.getBatchEligible())) {
                count++;
            }
        }
        return count;
    }

    private void clearEmptySandboxTemporalFields(NxDisRoutePlanEntity plan) {
        plan.setNxDrpBatchStartAt(null);
        plan.setNxDrpBatchEndAt(null);
        plan.setNxDrpDefaultDepartAt(null);
        plan.setNxDrpPlannedStartAt(null);
        plan.setNxDrpPlannedEndAt(null);
        plan.setNxDrpTotalWaitMinutes(0);
        plan.setNxDrpTotalLateMinutes(0);
        plan.setPlanTemporalStatus(null);
        plan.setPlanTemporalStatusLabel(null);
        String routeDate = plan.getNxDrpRouteDate() != null ? plan.getNxDrpRouteDate() : plan.getNxDrpPlanDate();
        plan.setRouteDateLabel(DisRouteTemporalHelper.formatRouteDateLabel(routeDate, new Date()));
    }

    private List<Map<String, Object>> toLoadingStopMaps(List<NxDisRouteStopEntity> stops,
                                                        List<com.nongxinle.entity.NxDisDriverRouteEntity> loadingRoutes) {
        return RouteDispatchReadModelAssembler.toLoadingStopMaps(
                stops, buildPurchaseStatusByOrderId(loadingRoutes));
    }

    private List<Map<String, Object>> toExecutionStopMaps(List<NxDisRouteStopEntity> stops,
                                                          List<com.nongxinle.entity.NxDisDriverRouteEntity> executionRoutes) {
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, String> driverNameByRouteId = buildDriverNameByRouteId(executionRoutes);
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        for (NxDisRouteStopEntity stop : stops) {
            Integer routeId = stop != null && stop.getShipmentTask() != null
                    ? stop.getShipmentTask().getNxDstDriverRouteId() : stop.getNxDrsDriverRouteId();
            String driverName = routeId != null ? driverNameByRouteId.get(routeId) : null;
            maps.add(RouteDispatchReadModelAssembler.toDeliveryStopMap(stop, driverName));
        }
        return maps;
    }

    private Map<Integer, String> buildDriverNameByRouteId(List<com.nongxinle.entity.NxDisDriverRouteEntity> routes) {
        Map<Integer, String> map = new HashMap<Integer, String>();
        if (routes == null) {
            return map;
        }
        for (com.nongxinle.entity.NxDisDriverRouteEntity route : routes) {
            if (route == null || route.getNxDdrId() == null) {
                continue;
            }
            map.put(route.getNxDdrId(), route.getDriverName());
        }
        return map;
    }

    private Map<Integer, Integer> buildPurchaseStatusByOrderId(List<com.nongxinle.entity.NxDisDriverRouteEntity> routes) {
        Map<Integer, Integer> purchaseStatusByOrderId = new HashMap<Integer, Integer>();
        if (routes == null) {
            return purchaseStatusByOrderId;
        }
        for (com.nongxinle.entity.NxDisDriverRouteEntity route : routes) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            for (NxDisRouteStopEntity stop : route.getStops()) {
                collectPurchaseStatusFromStop(stop, purchaseStatusByOrderId);
            }
        }
        return purchaseStatusByOrderId;
    }

    private void collectPurchaseStatusFromStop(NxDisRouteStopEntity stop,
                                               Map<Integer, Integer> purchaseStatusByOrderId) {
        if (stop == null || stop.getShipmentTask() == null || stop.getShipmentTask().getItems() == null) {
            return;
        }
        for (NxDisShipmentTaskItemEntity item : stop.getShipmentTask().getItems()) {
            if (item == null || item.getNxDstiLiveOrderId() == null) {
                continue;
            }
            Integer orderId = item.getNxDstiLiveOrderId();
            if (purchaseStatusByOrderId.containsKey(orderId)) {
                continue;
            }
            NxDepartmentOrdersEntity order = nxDepartmentOrdersService.queryObject(orderId);
            if (order != null && order.getNxDoPurchaseStatus() != null) {
                purchaseStatusByOrderId.put(orderId, order.getNxDoPurchaseStatus());
            }
        }
    }

    private List<Map<String, Object>> toConfirmedStopMaps(List<NxDisRouteStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        for (NxDisRouteStopEntity stop : stops) {
            maps.add(RouteDispatchReadModelAssembler.toConfirmedDeliveryStopMap(stop));
        }
        return maps;
    }

    private List<Map<String, Object>> toEphemeralStopMaps(List<NxDisRouteStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        for (NxDisRouteStopEntity stop : stops) {
            maps.add(RouteDispatchReadModelAssembler.toEphemeralStopMap(stop));
        }
        return maps;
    }

    private List<Map<String, Object>> toStopMaps(List<NxDisRouteStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        for (NxDisRouteStopEntity stop : stops) {
            maps.add(RouteDispatchReadModelAssembler.toStopMap(stop));
        }
        return maps;
    }

    private int countCustomerStops(SandboxComputeResult compute) {
        if (compute == null) {
            return 0;
        }
        int count = 0;
        if (compute.getConfirmedStops() != null) {
            count += compute.getConfirmedStops().size();
        }
        if (compute.getSandboxSuggestedStops() != null) {
            count += compute.getSandboxSuggestedStops().size();
        }
        if (compute.getUnassignedStops() != null) {
            count += compute.getUnassignedStops().size();
        }
        return count;
    }

    private RouteFeasibilityResult buildSandboxFeasibility(NxDisRoutePlanEntity plan,
                                                           SandboxComputeResult compute,
                                                           DriverDispatchListResponse driverList,
                                                           Date serverNow) {
        RouteFeasibilityResult result = new RouteFeasibilityResult();
        result.setFeasibilityStatus(com.nongxinle.route.DisRouteFeasibilityStatus.FEASIBLE);
        result.setWarnings(new ArrayList<RouteDispatchWarning>());

        int stopCount = countCustomerStops(compute);
        if (stopCount <= 0 || plan == null) {
            return result;
        }

        boolean windowActive = plan.getNxDrpPlannedEndAt() == null || !serverNow.after(plan.getNxDrpPlannedEndAt());
        if (!windowActive) {
            return result;
        }

        boolean hasLate = false;
        appendLateWarnings(result.getWarnings(), compute.getSandboxSuggestedStops());
        appendLateWarnings(result.getWarnings(), compute.getConfirmedStops());
        for (RouteDispatchWarning warning : result.getWarnings()) {
            if ("SANDBOX_LATE".equals(warning.getCode())) {
                hasLate = true;
                break;
            }
        }
        if (hasLate) {
            result.setFeasibilityStatus(com.nongxinle.route.DisRouteFeasibilityStatus.HAS_LATE);
        }

        int eligible = driverList != null && driverList.getSummary() != null
                && driverList.getSummary().getEligibleCount() != null
                ? driverList.getSummary().getEligibleCount() : 0;
        if (eligible == 0) {
            RouteDispatchWarning warning = new RouteDispatchWarning();
            warning.setCode(com.nongxinle.route.DisRouteWarningCode.NO_AVAILABLE_DRIVER);
            warning.setSeverity("ERROR");
            warning.setMessage("当前没有可派司机");
            result.getWarnings().add(warning);
            result.setFeasibilityStatus(com.nongxinle.route.DisRouteFeasibilityStatus.NO_AVAILABLE_DRIVER);
        }
        return result;
    }

    private void appendLateWarnings(List<RouteDispatchWarning> warnings,
                                    List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop.getNxDrsTimeWindowStatus() != null
                    && DisRouteStopTimeWindowStatus.LATE.equals(stop.getNxDrsTimeWindowStatus())
                    && !DisRouteSandboxScheduleMode.ADHOC_NOW.equals(stop.getScheduleMode())) {
                RouteDispatchWarning warning = new RouteDispatchWarning();
                warning.setCode("SANDBOX_LATE");
                warning.setSeverity("WARN");
                warning.setDepartmentName(stop.getNxDrsDepartmentName());
                warning.setLateMinutes(stop.getNxDrsLateMinutes());
                warning.setMessage(stop.getNxDrsDepartmentName() + " 预计迟到 "
                        + stop.getNxDrsLateMinutes() + " 分钟");
                warnings.add(warning);
            }
        }
    }

    private void appendInvalidHint(DispatchWorkbenchDto workbench, int invalidCount) {
        if (workbench == null || invalidCount <= 0) {
            return;
        }
        List<DispatchWorkbenchIssueDto> issues = workbench.getTopIssues() != null
                ? new ArrayList<DispatchWorkbenchIssueDto>(workbench.getTopIssues())
                : new ArrayList<DispatchWorkbenchIssueDto>();
        DispatchWorkbenchIssueDto issue = new DispatchWorkbenchIssueDto();
        issue.setType("STALE_SANDBOX_CACHE");
        issue.setSeverity("WARN");
        issue.setTitle("存在无效旧派车站点");
        issue.setDescription("有 " + invalidCount + " 个历史沙盘/无效站点，已从正常视图隐藏");
        issue.setSuggestion("当前动态沙盘已自动重算；已确认站点不受影响");
        issues.add(0, issue);
        if (issues.size() > 3) {
            issues = issues.subList(0, 3);
        }
        workbench.setTopIssues(issues);
    }

    private static GeoPoint resolveDepotForVisibleRouteSchedule(NxDisRoutePlanEntity plan) {
        if (plan == null) {
            return null;
        }
        if (isValidCoordinate(plan.getNxDrpDepotLat(), plan.getNxDrpDepotLng())) {
            return toPoint(plan.getNxDrpDepotLat(), plan.getNxDrpDepotLng());
        }
        return null;
    }

    private void enrichDepotPresentation(SandboxTodayPageBuildContext ctx, Integer disId) {
        ctx.setDepotName(DisRouteSandboxTodayTimelineBuilder.DEPOT_NAME);
        if (disId == null) {
            return;
        }
        NxDistributerEntity dis = nxDistributerService.queryObject(disId);
        if (dis == null) {
            return;
        }
        String name = firstNonBlank(
                dis.getNxDistributerShowName(),
                dis.getNxDistributerName(),
                dis.getNxDistributerMarketName());
        if (name != null) {
            ctx.setDepotName(name.trim());
        }
        if (dis.getNxDistributerAddress() != null && !dis.getNxDistributerAddress().trim().isEmpty()) {
            ctx.setDepotAddress(dis.getNxDistributerAddress().trim());
        }
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

    private static boolean isSkippedStrategyPlanningPhase(DispatchAssignmentPlan plan) {
        if (plan == null || plan.getPlanningPhase() == null) {
            return false;
        }
        DispatchPlanningPhase phase = plan.getPlanningPhase();
        return phase == DispatchPlanningPhase.SKIPPED_NO_PENDING_STOPS
                || phase == DispatchPlanningPhase.SKIPPED_NO_ELIGIBLE_DRIVERS
                || phase == DispatchPlanningPhase.SKIPPED_NOT_OPTIMIZABLE;
    }
}
