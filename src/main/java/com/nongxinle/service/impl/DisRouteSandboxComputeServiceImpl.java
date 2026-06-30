package com.nongxinle.service.impl;

import com.nongxinle.dao.*;
import com.nongxinle.dto.route.*;
import com.nongxinle.dto.route.InvalidDispatchStopDto;
import com.nongxinle.dto.route.SandboxComputeRequest;
import com.nongxinle.dto.route.SandboxComputeResult;
import com.nongxinle.entity.*;
import com.nongxinle.route.*;
import com.nongxinle.route.cost.ResilientTencentRouteCostProvider;
import com.nongxinle.route.cost.TencentMatrixRouteCostProvider;
import com.nongxinle.route.dispatch.strategy.DispatchAssignmentPlan;
import com.nongxinle.route.dispatch.strategy.DispatchPlanningPhase;
import com.nongxinle.route.dispatch.strategy.DispatchStrategyContext;
import com.nongxinle.route.dispatch.strategy.DispatchStrategyMode;
import com.nongxinle.route.dispatch.strategy.DispatchStrategyOrchestrator;
import com.nongxinle.route.dispatch.strategy.DispatchStrategyOutcome;
import com.nongxinle.route.dispatch.strategy.DispatchStrategyOptimizerEligibility;
import com.nongxinle.route.dispatch.strategy.DriverRoutePlan;
import com.nongxinle.route.dispatch.strategy.OwnerFixedRouteTimeWindowRouteSequencer;
import com.nongxinle.route.dispatch.strategy.StopAssignment;
import com.nongxinle.route.model.*;
import com.nongxinle.route.proposal.SandboxProposalPlan;
import com.nongxinle.route.proposal.SandboxProposalPlanBuilder;
import com.nongxinle.service.DisDriverDutyService;
import com.nongxinle.service.DisRouteSandboxComputeService;
import com.nongxinle.service.NxDistributerService;
import com.nongxinle.service.SysCityMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.nongxinle.route.DisRoutePlanStatus.ASSIGNED;
import static com.nongxinle.route.DisRoutePlanStatus.READY;
import static com.nongxinle.route.DisRoutePlanStatus.SIMULATED;
import static com.nongxinle.route.DisShipmentTaskItemStatus.ACTIVE;
import static com.nongxinle.route.RouteCoordinateUtils.isValidCoordinate;
import static com.nongxinle.route.RouteCoordinateUtils.toPoint;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

/**
 * Phase 3a：动态沙盘内存计算。未确认客户不落库；已确认客户从 DB 加载。
 */
@Service("disRouteSandboxComputeService")
public class DisRouteSandboxComputeServiceImpl implements DisRouteSandboxComputeService {

    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisRouteStopDao nxDisRouteStopDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private NxDisShipmentTaskItemDao nxDisShipmentTaskItemDao;
    @Autowired
    private NxDistributerUserDao nxDistributerUserDao;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private SysCityMarketService sysCityMarketService;
    @Autowired
    private RouteEngineRegistry routeEngineRegistry;
    @Autowired
    private TencentMatrixRouteCostProvider tencentMatrixRouteCostProvider;
    @Autowired
    private DisDriverDutyService disDriverDutyService;
    @Autowired
    private DisRouteDispatchSnapshotHelper disRouteDispatchSnapshotHelper;
    @Autowired
    private DisRouteDispatchReadIntegrityHelper disRouteDispatchReadIntegrityHelper;
    @Autowired
    private DisRouteShipmentTaskItemOrderResolver disRouteShipmentTaskItemOrderResolver;
    @Autowired
    private DisRouteSandboxLegMetricsHelper disRouteSandboxLegMetricsHelper;
    @Autowired
    private DisRouteSandboxComputeMergedPlanBuilder mergedPlanBuilder;
    @Autowired
    private NxDisDriverDutyDao nxDisDriverDutyDao;
    @Autowired
    private NxDepartmentDao nxDepartmentDao;
    @Autowired
    private com.nongxinle.service.DisRouteDispatchService disRouteDispatchService;
    @Autowired
    private com.nongxinle.service.DisRouteDeliveryHistoryPreferenceService disRouteDeliveryHistoryPreferenceService;
    @Autowired
    private NxDisSandboxDayTimeWindowDao nxDisSandboxDayTimeWindowDao;
    @Autowired
    private DispatchStrategyOrchestrator dispatchStrategyOrchestrator;

    @Override
    public SandboxComputeResult compute(SandboxComputeRequest request) throws Exception {
        validateRequest(request);
        Integer disId = request.getDisId();
        String routeDate = resolveRouteDate(request.getRouteDate());
        String batchCode = normalizeBatch(request.getBatchCode());

        SandboxComputeResult result = new SandboxComputeResult();
        result.setRouteDate(routeDate);
        result.setDispatchBatch(batchCode);
        boolean formalPage = request.isFormalPageContractMode();
        boolean persistedOnly = request.isPersistedRoutesOnlyMode();

        NxDisRoutePlanEntity planContext = loadPlanContext(disId, routeDate, batchCode);
        result.setPlanContext(planContext);

        List<NxDisShipmentTaskEntity> dbTasks = loadDbTasks(disId, routeDate);
        attachItems(dbTasks);

        Set<Integer> confirmedDepIds = new HashSet<Integer>();
        List<NxDisRouteStopEntity> confirmedStops = new ArrayList<NxDisRouteStopEntity>();
        List<InvalidDispatchStopDto> invalidStops = new ArrayList<InvalidDispatchStopDto>();

        partitionDbTasks(dbTasks, disId, routeDate, planContext, confirmedDepIds, confirmedStops, invalidStops);
        supplementExecutionConfirmedStops(disId, routeDate, planContext, confirmedDepIds, confirmedStops, invalidStops);

        List<Integer> excludeDepIds = confirmedDepIds.isEmpty()
                ? null : new ArrayList<Integer>(confirmedDepIds);
        List<DisRouteOrderSnapshotDto> pendingOrders = persistedOnly
                ? Collections.<DisRouteOrderSnapshotDto>emptyList()
                : queryEligibleOrders(disId, excludeDepIds);
        if (formalPage) {
            result.setEffectiveOrders(Collections.<DisRouteOrderSnapshotDto>emptyList());
        } else {
            result.setEffectiveOrders(pendingOrders);
        }

        GeoPoint depot = resolveDepot(request, disId, planContext, pendingOrders);
        if (planContext != null && depot != null) {
            planContext.setNxDrpDepotLat(depot.getLat());
            planContext.setNxDrpDepotLng(depot.getLng());
        }

        Map<Integer, List<DisRouteOrderSnapshotDto>> ordersByDep = groupOrdersByDepartment(pendingOrders);
        List<NxDistributerUserEntity> onDutyDrivers = listOnDutyDrivers(disId, routeDate);
        result.setOnDutyDrivers(onDutyDrivers);
        Set<Integer> sandboxIneligibleDrivers = DisRouteSandboxDispatchEligibilityHelper
                .resolveSandboxDispatchIneligibleDriverUserIds(
                        nxDisDriverRouteDao,
                        nxDisRoutePlanDao,
                        nxDisShipmentTaskDao,
                        disId,
                        routeDate,
                        planContext != null ? planContext.getNxDrpId() : null);
        Set<Integer> offDutyDriverIds = resolveOffDutyDriverUserIds(disId, routeDate, onDutyDrivers);
        List<NxDisRouteStopEntity> suggestedStops = new ArrayList<NxDisRouteStopEntity>();
        List<NxDisRouteStopEntity> unassignedStops = new ArrayList<NxDisRouteStopEntity>();
        sanitizeConfirmedForOffDutyDrivers(confirmedStops, confirmedDepIds, unassignedStops, offDutyDriverIds);
        Set<Integer> dispatchBlockedDriverIds = DisRouteSandboxDispatchEligibilityHelper.unionDriverIds(
                sandboxIneligibleDrivers, offDutyDriverIds);
        List<NxDistributerUserEntity> dispatchEligibleDrivers = filterDispatchEligibleDrivers(
                onDutyDrivers, dispatchBlockedDriverIds);

        List<NxDisShipmentTaskEntity> virtualTasks = buildVirtualTasks(
                disId, routeDate, ordersByDep, confirmedDepIds, dbTasks);

        Map<Integer, NxDisSandboxDayTimeWindowEntity> sandboxDayOverrideByDepId =
                loadSandboxDayTimeWindowOverrides(disId, routeDate);
        Map<Integer, NxDepartmentEntity> departmentByDepId =
                DisRouteSandboxStopTimeWindowResolutionSupport.loadDepartmentsByDepId(
                        nxDepartmentDao,
                        DisRouteSandboxStopTimeWindowResolutionSupport.collectDepIdsFromTasks(virtualTasks));

        if (!formalPage && !dispatchEligibleDrivers.isEmpty() && !virtualTasks.isEmpty()) {
            result.setDeliveryHistoryPreferences(resolveDeliveryHistoryPreferences(
                    disId, routeDate, virtualTasks, dispatchEligibleDrivers));
        }
        DeliveryHistoryPreferenceBatchResult historyPreferencesForStrategy = null;
        if (!persistedOnly && !virtualTasks.isEmpty() && !dispatchEligibleDrivers.isEmpty()) {
            historyPreferencesForStrategy = result.getDeliveryHistoryPreferences() != null
                    ? result.getDeliveryHistoryPreferences()
                    : resolveDeliveryHistoryPreferences(disId, routeDate, virtualTasks, dispatchEligibleDrivers);
        }

        RouteOptimizeResult optimizeResult = null;
        DispatchAssignmentPlan dispatchAssignmentPlan = null;

        if (!persistedOnly) {
            List<NxDisShipmentTaskEntity> optimizable = filterOptimizable(virtualTasks, confirmedDepIds);
            DispatchStrategyOutcome strategyOutcome = dispatchStrategyOrchestrator.plan(
                    DispatchStrategyContext.builder()
                            .disId(disId)
                            .routeDate(routeDate)
                            .batchCode(batchCode)
                            .depot(depot)
                            .virtualTasks(virtualTasks)
                            .optimizableTasks(optimizable)
                            .dispatchEligibleDrivers(dispatchEligibleDrivers)
                            .confirmedStops(confirmedStops)
                            .confirmedDepIds(confirmedDepIds)
                            .deliveryHistoryPreferences(historyPreferencesForStrategy)
                            .departmentByDepId(departmentByDepId)
                            .sandboxDayOverrideByDepId(sandboxDayOverrideByDepId)
                            .build());
            if (strategyOutcome != null && strategyOutcome.getPlan() != null) {
                dispatchAssignmentPlan = strategyOutcome.getPlan();
            }

            Set<Integer> historyBoundDepIds = new HashSet<Integer>();
            if (dispatchAssignmentPlan != null && !isSkippedStrategyPlanningPhase(dispatchAssignmentPlan)) {
                historyBoundDepIds = applyHistoryDriverBindingsFromPlan(
                        dispatchAssignmentPlan, optimizable, suggestedStops, dispatchEligibleDrivers);
                enrichPlanFrozenDebug(dispatchAssignmentPlan, confirmedDepIds, confirmedStops);
            }

            List<NxDisShipmentTaskEntity> fallbackOptimizable = filterPendingOptimizerCandidates(
                    optimizable, historyBoundDepIds, confirmedDepIds);

            if (strategyOutcome != null && strategyOutcome.isDelegateLegacyOptimizer()) {
                if (!fallbackOptimizable.isEmpty()) {
                    optimizeResult = runOptimization(depot, dispatchEligibleDrivers, fallbackOptimizable);
                    applyOptimizationInMemory(fallbackOptimizable, optimizeResult, suggestedStops, unassignedStops,
                            dispatchEligibleDrivers);
                }
            } else if (strategyOutcome == null) {
                throw new IllegalStateException("dispatch strategy outcome missing");
            } else if (isSkippedStrategyPlanningPhase(dispatchAssignmentPlan) && !virtualTasks.isEmpty()) {
                for (NxDisShipmentTaskEntity task : virtualTasks) {
                    unassignedStops.add(buildUnassignedStop(task));
                }
            }

        } else if (!virtualTasks.isEmpty()) {
            for (NxDisShipmentTaskEntity task : virtualTasks) {
                unassignedStops.add(buildUnassignedStop(task));
            }
        }
        result.setDispatchAssignmentPlan(dispatchAssignmentPlan);
        sanitizeSuggestedDriverAssignments(suggestedStops, unassignedStops, dispatchBlockedDriverIds);
        sanitizeSuggestedAgainstConfirmedDepartments(suggestedStops, confirmedDepIds);
        reassignOptimizedUnassignedToEligibleDrivers(
                suggestedStops, unassignedStops, dispatchEligibleDrivers, buildDriverNameIndex(onDutyDrivers));

        DispatchStrategyContext sequencingContext = null;
        if (!persistedOnly
                && dispatchAssignmentPlan != null
                && !isSkippedStrategyPlanningPhase(dispatchAssignmentPlan)
                && dispatchAssignmentPlan.getStrategyMode() == DispatchStrategyMode.OWNER_FIXED_ROUTE) {
            DisRouteSandboxStopTimeWindowResolutionSupport.ensureDepartmentsLoaded(
                    nxDepartmentDao,
                    DisRouteSandboxStopTimeWindowResolutionSupport.collectDepIdsFromStops(suggestedStops),
                    departmentByDepId);
            DisRouteSandboxStopTimeWindowResolutionSupport.applyToStops(
                    suggestedStops, departmentByDepId, sandboxDayOverrideByDepId);
            sequencingContext = DispatchStrategyContext.builder()
                    .disId(disId)
                    .routeDate(routeDate)
                    .batchCode(batchCode)
                    .depot(depot)
                    .serverNow(new Date())
                    .departmentByDepId(departmentByDepId)
                    .sandboxDayOverrideByDepId(sandboxDayOverrideByDepId)
                    .optimizableTasks(filterOptimizable(virtualTasks, confirmedDepIds))
                    .build();
            OwnerFixedRouteTimeWindowRouteSequencer.resequenceSuggestedStops(
                    suggestedStops, sequencingContext, dispatchAssignmentPlan);
            OwnerFixedRouteTimeWindowRouteSequencer.invalidateLegMetrics(suggestedStops);
        }

        markSandboxMetadata(suggestedStops, DisRouteSandboxStopSource.SANDBOX_SUGGESTED, true);
        markSandboxMetadata(unassignedStops, DisRouteSandboxStopSource.UNASSIGNED, true);
        markSandboxMetadata(confirmedStops, DisRouteSandboxStopSource.CONFIRMED, false);

        unassignedStops = DisRouteSandboxUnassignedStopHelper.consolidateByDepartment(unassignedStops);

        result.setConfirmedStops(confirmedStops);
        result.setSandboxSuggestedStops(suggestedStops);
        result.setUnassignedStops(unassignedStops);
        result.setInvalidStops(invalidStops);
        result.setHasLockedStops(!confirmedStops.isEmpty());

        SandboxProposalPlan proposalPlan = SandboxProposalPlanBuilder.build(
                suggestedStops, unassignedStops, dispatchAssignmentPlan, sequencingContext);
        result.setProposalPlan(proposalPlan);

        NxDisRoutePlanEntity mergedPlan = mergedPlanBuilder.buildMergedPlan(
                planContext, disId, routeDate, batchCode, depot, onDutyDrivers,
                confirmedStops, suggestedStops, dispatchBlockedDriverIds, offDutyDriverIds);
        mergedPlanBuilder.hydrateExecutionRouteSnapshots(mergedPlan);
        if (!persistedOnly) {
            disRouteSandboxLegMetricsHelper.applyToPlan(depot, mergedPlan);
            try {
                disRouteSandboxLegMetricsHelper.applyDepotLegToStops(depot, unassignedStops);
            } catch (IOException ex) {
                // 未分配 leg 失败时不阻断整页
            }
        }
        DisRouteSandboxStopTimeWindowResolutionSupport.ensureDepartmentsLoaded(
                nxDepartmentDao,
                DisRouteSandboxStopTimeWindowResolutionSupport.collectDepIdsFromStops(suggestedStops),
                departmentByDepId);
        DisRouteSandboxStopTimeWindowResolutionSupport.ensureDepartmentsLoaded(
                nxDepartmentDao,
                DisRouteSandboxStopTimeWindowResolutionSupport.collectDepIdsFromStops(confirmedStops),
                departmentByDepId);
        DisRouteSandboxStopTimeWindowResolutionSupport.applyToPlan(
                mergedPlan, departmentByDepId, sandboxDayOverrideByDepId);
        mergedPlanBuilder.applySchedulePreviewToMergedPlan(mergedPlan, routeDate);
        if (!persistedOnly) {
            DisRouteSandboxStopTimeWindowResolutionSupport.ensureDepartmentsLoaded(
                    nxDepartmentDao,
                    DisRouteSandboxStopTimeWindowResolutionSupport.collectDepIdsFromStops(unassignedStops),
                    departmentByDepId);
            DisRouteSandboxStopTimeWindowResolutionSupport.applyToStops(
                    unassignedStops, departmentByDepId, sandboxDayOverrideByDepId);
            mergedPlanBuilder.applySchedulePreviewToUnassignedStops(unassignedStops, routeDate);
        }
        mergedPlanBuilder.reconcileExecutionRoutesAfterSnapshot(mergedPlan);
        applyLiveDepartmentNames(mergedPlan, confirmedStops, suggestedStops, unassignedStops);
        result.setMergedPlan(mergedPlan);

        List<NxDisShipmentTaskEntity> displayTasks = collectDisplayTasks(mergedPlan);
        result.setAllDisplayTasks(displayTasks);

        DisRouteSandboxManualDispatchComputeEnricher.enrich(result);
        result.setHasNewOrders(pendingOrders != null && !pendingOrders.isEmpty());

        return result;
    }

    private void validateRequest(SandboxComputeRequest request) {
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
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

    /** 沙盘：仅 disId + status&lt;3 等业务口径；不按送达日过滤。可排除已确认客户部门。 */
    private List<DisRouteOrderSnapshotDto> queryEligibleOrders(Integer disId, List<Integer> excludeDepartmentIds) {
        return nxDisRoutePlanDao.queryEligibleLiveOrderSnapshots(disId, null, null, excludeDepartmentIds);
    }

    private NxDisRoutePlanEntity loadPlanContext(Integer disId, String routeDate, String batchCode) {
        for (String status : new String[]{ASSIGNED, READY}) {
            NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                    disId, routeDate, batchCode, status);
            if (plan != null) {
                plan.setNxDrpDispatchBatch(batchCode);
                return plan;
            }
        }
        NxDisRoutePlanEntity executionPlan = findExecutionPlanContext(disId, routeDate, batchCode);
        if (executionPlan != null) {
            return executionPlan;
        }
        NxDisRoutePlanEntity empty = new NxDisRoutePlanEntity();
        empty.setNxDrpDistributerId(disId);
        empty.setNxDrpRouteDate(routeDate);
        empty.setNxDrpPlanDate(routeDate);
        empty.setNxDrpDispatchBatch(batchCode);
        empty.setNxDrpStatus(SIMULATED);
        return empty;
    }

    /** 出发后 plan 可能被误标 SIMULATED，仍按 routeDate 找回含 execution route 的 DB plan。 */
    private NxDisRoutePlanEntity findExecutionPlanContext(Integer disId, String routeDate, String batchCode) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("disId", disId);
        params.put("routeDate", routeDate);
        List<NxDisRoutePlanEntity> plans = nxDisRoutePlanDao.queryList(params);
        if (plans == null || plans.isEmpty()) {
            return null;
        }
        NxDisRoutePlanEntity batchMatch = null;
        NxDisRoutePlanEntity anyActive = null;
        for (NxDisRoutePlanEntity plan : plans) {
            if (plan == null || DisRoutePlanStatus.CANCELLED.equals(plan.getNxDrpStatus())) {
                continue;
            }
            if (plan.getNxDrpId() == null) {
                continue;
            }
            List<NxDisDriverRouteEntity> routes = nxDisDriverRouteDao.queryByPlanId(plan.getNxDrpId());
            boolean hasExecutionRoute = DisRouteExecutionPlanHelper.hasExecutionDriverRoute(routes);
            if (!hasExecutionRoute && !hasConfirmedTasksOnPlan(plan.getNxDrpId())) {
                continue;
            }
            if (batchCode.equals(normalizeBatch(plan.getNxDrpDispatchBatch()))) {
                batchMatch = plan;
                break;
            }
            if (anyActive == null) {
                anyActive = plan;
            }
        }
        NxDisRoutePlanEntity chosen = batchMatch != null ? batchMatch : anyActive;
        if (chosen != null) {
            chosen.setNxDrpDispatchBatch(batchCode);
        }
        return chosen;
    }

    private boolean hasConfirmedTasksOnPlan(Integer planId) {
        if (planId == null) {
            return false;
        }
        List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByPlanId(planId);
        return DisRouteExecutionPlanHelper.hasConfirmedOrExecutionTasks(tasks);
    }

    private GeoPoint resolveDepot(SandboxComputeRequest request,
                                  Integer disId,
                                  NxDisRoutePlanEntity plan,
                                  List<DisRouteOrderSnapshotDto> orders) {
        if (isValidCoordinate(request.getDepotLat(), request.getDepotLng())) {
            return toPoint(request.getDepotLat(), request.getDepotLng());
        }
        if (plan != null && isValidCoordinate(plan.getNxDrpDepotLat(), plan.getNxDrpDepotLng())) {
            return toPoint(plan.getNxDrpDepotLat(), plan.getNxDrpDepotLng());
        }
        NxDistributerEntity dis = nxDistributerService.queryObject(disId);
        if (dis != null && isValidCoordinate(dis.getNxDistributerLan(), dis.getNxDistributerLun())) {
            return toPoint(dis.getNxDistributerLan(), dis.getNxDistributerLun());
        }
        if (dis != null && dis.getNxDistributerSysMarketId() != null) {
            com.nongxinle.entity.SysCityMarketEntity market =
                    sysCityMarketService.queryObject(dis.getNxDistributerSysMarketId());
            if (market != null && market.getSysCmCenterLatitude() != null
                    && market.getSysCmCenterLongitude() != null) {
                String lat = market.getSysCmCenterLatitude().toPlainString();
                String lng = market.getSysCmCenterLongitude().toPlainString();
                if (isValidCoordinate(lat, lng)) {
                    return toPoint(lat, lng);
                }
            }
        }
        return deriveDepotFromOrders(orders);
    }

    private GeoPoint deriveDepotFromOrders(List<DisRouteOrderSnapshotDto> orders) {
        if (orders == null || orders.isEmpty()) {
            return null;
        }
        List<GeoPoint> points = new ArrayList<GeoPoint>();
        for (DisRouteOrderSnapshotDto order : orders) {
            if (isValidCoordinate(order.getLat(), order.getLng())) {
                points.add(toPoint(order.getLat(), order.getLng()));
            }
        }
        return RouteCoordinateUtils.deriveCentroidDepot(points);
    }

    private GeoPoint resolveDepotForOptimization(GeoPoint depot, List<NxDisShipmentTaskEntity> optimizableTasks) {
        if (depot != null) {
            return depot;
        }
        if (optimizableTasks == null || optimizableTasks.isEmpty()) {
            return null;
        }
        List<GeoPoint> points = new ArrayList<GeoPoint>();
        for (NxDisShipmentTaskEntity task : optimizableTasks) {
            if (isValidCoordinate(task.getNxDstLat(), task.getNxDstLng())) {
                points.add(toPoint(task.getNxDstLat(), task.getNxDstLng()));
            }
        }
        return RouteCoordinateUtils.deriveCentroidDepot(points);
    }

    private List<NxDisShipmentTaskEntity> loadDbTasks(Integer disId, String routeDate) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("disId", disId);
        map.put("routeDate", routeDate);
        List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryList(map);
        return tasks != null ? tasks : new ArrayList<NxDisShipmentTaskEntity>();
    }

    private void attachItems(List<NxDisShipmentTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        List<Integer> taskIds = new ArrayList<Integer>();
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task.getNxDstId() != null) {
                taskIds.add(task.getNxDstId());
            }
        }
        if (taskIds.isEmpty()) {
            return;
        }
        List<NxDisShipmentTaskItemEntity> allItems = nxDisShipmentTaskItemDao.queryByTaskIds(taskIds);
        disRouteShipmentTaskItemOrderResolver.enrichItems(allItems);
        for (NxDisShipmentTaskEntity task : tasks) {
            List<NxDisShipmentTaskItemEntity> items = new ArrayList<NxDisShipmentTaskItemEntity>();
            for (NxDisShipmentTaskItemEntity item : allItems) {
                if (task.getNxDstId().equals(item.getNxDstiTaskId())) {
                    items.add(item);
                }
            }
            task.setItems(items);
        }
    }

    private void partitionDbTasks(List<NxDisShipmentTaskEntity> dbTasks,
                                  Integer disId,
                                  String routeDate,
                                  NxDisRoutePlanEntity planContext,
                                  Set<Integer> confirmedDepIds,
                                  List<NxDisRouteStopEntity> confirmedStops,
                                  List<InvalidDispatchStopDto> invalidStops) {
        for (NxDisShipmentTaskEntity task : dbTasks) {
            if (task == null || DisShipmentTaskStatus.CANCELLED.equals(task.getNxDstStatus())) {
                continue;
            }
            if (!matchesRouteDate(task, routeDate)) {
                continue;
            }
            boolean protectedTask = isTaskProtected(task);
            boolean hasValidItems = disRouteDispatchReadIntegrityHelper.hasExecutionDisplayItems(task)
                    || disRouteDispatchReadIntegrityHelper.hasValidDisplayItems(task, disId, null);

            if (protectedTask) {
                if (task.getNxDstDepFatherId() != null && isDispatchLockedDepForNewOrders(task)) {
                    confirmedDepIds.add(task.getNxDstDepFatherId());
                }
                if (hasValidItems) {
                    confirmedStops.add(buildConfirmedStopFromDb(task, planContext));
                } else {
                    confirmedStops.add(buildConfirmedStopFromDb(task, planContext));
                    invalidStops.add(buildInvalidStop(task, "CONFIRMED_NO_VALID_ORDERS",
                            "已确认客户无有效订单，需人工处理"));
                }
                continue;
            }

            if (DisShipmentTaskStatus.SIMULATED.equals(task.getNxDstStatus())
                    || DisShipmentTaskStatus.UNASSIGNED.equals(task.getNxDstStatus())) {
                invalidStops.add(buildInvalidStop(task, "STALE_SANDBOX_CACHE",
                        "旧沙盘缓存站点，请忽略；当前建议以 sandboxSuggestedStops 为准"));
            }
        }
    }

    private NxDisRouteStopEntity buildConfirmedStopFromDb(NxDisShipmentTaskEntity task,
                                                          NxDisRoutePlanEntity planContext) {
        NxDisRouteStopEntity legacyStop = nxDisRouteStopDao.queryByShipmentTaskId(task.getNxDstId());
        NxDisRouteStopEntity stop = DisRouteDeliveryStopAdapter.fromTask(task, legacyStop);
        DisRouteExecutionRouteSnapshotHelper.hydrateStopSnapshotFromPersistence(stop, task, legacyStop);
        disRouteDispatchSnapshotHelper.applyItemMetrics(task);
        stop.setShipmentTask(task);
        return stop;
    }

    private InvalidDispatchStopDto buildInvalidStop(NxDisShipmentTaskEntity task,
                                                    String reasonCode,
                                                    String message) {
        InvalidDispatchStopDto dto = new InvalidDispatchStopDto();
        dto.setTaskId(task.getNxDstId());
        dto.setDeliveryStopId(task.getNxDstId());
        dto.setDepFatherId(task.getNxDstDepFatherId());
        dto.setDepName(task.getNxDstDepName());
        dto.setInvalidReason(reasonCode + ": " + message);
        dto.setNeedsManualCleanup(isTaskProtected(task));
        return dto;
    }

    private static boolean matchesRouteDate(NxDisShipmentTaskEntity task, String routeDate) {
        if (routeDate == null || task == null) {
            return true;
        }
        String taskDate = task.getNxDstRouteDate();
        if (taskDate == null || taskDate.trim().isEmpty()) {
            return true;
        }
        return routeDate.equals(taskDate.trim());
    }

    /**
     * 配送执行态 task 可能因 live order eligible 变化从 partitionDbTasks 漏掉；
     * 按 routeDate / execution driverRoute 再补一轮 confirmedStops。
     */
    private void supplementExecutionConfirmedStops(Integer disId,
                                                   String routeDate,
                                                   NxDisRoutePlanEntity planContext,
                                                   Set<Integer> confirmedDepIds,
                                                   List<NxDisRouteStopEntity> confirmedStops,
                                                   List<InvalidDispatchStopDto> invalidStops) {
        Set<Integer> existingTaskIds = indexConfirmedTaskIds(confirmedStops);
        List<NxDisShipmentTaskEntity> supplemental = new ArrayList<NxDisShipmentTaskEntity>();

        for (String status : new String[]{
                DisShipmentTaskStatus.IN_DELIVERY,
                DisShipmentTaskStatus.DELIVERED,
                DisShipmentTaskStatus.EXCEPTION}) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("disId", disId);
            params.put("routeDate", routeDate);
            params.put("status", status);
            List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByDisRouteDateStatus(params);
            if (tasks != null) {
                supplemental.addAll(tasks);
            }
        }

        Map<String, Object> planParams = new HashMap<String, Object>();
        planParams.put("disId", disId);
        planParams.put("routeDate", routeDate);
        List<NxDisRoutePlanEntity> plans = nxDisRoutePlanDao.queryList(planParams);
        if (plans != null) {
            for (NxDisRoutePlanEntity plan : plans) {
                if (plan == null || plan.getNxDrpId() == null
                        || DisRoutePlanStatus.CANCELLED.equals(plan.getNxDrpStatus())) {
                    continue;
                }
                List<NxDisDriverRouteEntity> routes = nxDisDriverRouteDao.queryByPlanId(plan.getNxDrpId());
                if (routes == null) {
                    continue;
                }
                for (NxDisDriverRouteEntity route : routes) {
                    if (route == null || route.getNxDdrId() == null
                            || !DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
                        continue;
                    }
                    List<NxDisShipmentTaskEntity> routeTasks =
                            nxDisShipmentTaskDao.queryByDriverRouteId(route.getNxDdrId());
                    if (routeTasks != null) {
                        supplemental.addAll(routeTasks);
                    }
                }
            }
        }

        if (supplemental.isEmpty()) {
            return;
        }
        attachItems(supplemental);
        for (NxDisShipmentTaskEntity task : supplemental) {
            if (task == null || task.getNxDstId() == null || existingTaskIds.contains(task.getNxDstId())) {
                continue;
            }
            if (DisShipmentTaskStatus.CANCELLED.equals(task.getNxDstStatus())) {
                continue;
            }
            boolean hasValidItems = disRouteDispatchReadIntegrityHelper.hasExecutionDisplayItems(task)
                    || disRouteDispatchReadIntegrityHelper.hasValidDisplayItems(task, disId, routeDate);
            if (!hasValidItems) {
                invalidStops.add(buildInvalidStop(task, "CONFIRMED_NO_VALID_ORDERS",
                        "已确认客户无有效订单，需人工处理"));
                existingTaskIds.add(task.getNxDstId());
                continue;
            }
            if (task.getNxDstDepFatherId() != null && isDispatchLockedDepForNewOrders(task)) {
                confirmedDepIds.add(task.getNxDstDepFatherId());
            }
            confirmedStops.add(buildConfirmedStopFromDb(task, planContext));
            existingTaskIds.add(task.getNxDstId());
        }
    }

    private static Set<Integer> indexConfirmedTaskIds(List<NxDisRouteStopEntity> confirmedStops) {
        Set<Integer> ids = new HashSet<Integer>();
        if (confirmedStops == null) {
            return ids;
        }
        for (NxDisRouteStopEntity stop : confirmedStops) {
            if (stop == null) {
                continue;
            }
            if (stop.getNxDrsShipmentTaskId() != null) {
                ids.add(stop.getNxDrsShipmentTaskId());
            } else if (stop.getShipmentTask() != null && stop.getShipmentTask().getNxDstId() != null) {
                ids.add(stop.getShipmentTask().getNxDstId());
            }
        }
        return ids;
    }

    private List<NxDisShipmentTaskEntity> buildVirtualTasks(Integer disId,
                                                            String routeDate,
                                                            Map<Integer, List<DisRouteOrderSnapshotDto>> ordersByDep,
                                                            Set<Integer> confirmedDepIds,
                                                            List<NxDisShipmentTaskEntity> dbTasks) {
        Map<Integer, NxDisShipmentTaskEntity> overrideByDep = loadTimeWindowOverrides(dbTasks);
        Map<Integer, NxDisSandboxDayTimeWindowEntity> sandboxDayOverrides =
                loadSandboxDayTimeWindowOverrides(disId, routeDate);
        List<NxDisShipmentTaskEntity> virtualTasks = new ArrayList<NxDisShipmentTaskEntity>();
        for (Map.Entry<Integer, List<DisRouteOrderSnapshotDto>> entry : ordersByDep.entrySet()) {
            Integer depId = entry.getKey();
            if (confirmedDepIds.contains(depId)) {
                continue;
            }
            List<DisRouteOrderSnapshotDto> orders = entry.getValue();
            if (orders == null || orders.isEmpty()) {
                continue;
            }
            DisRouteOrderSnapshotDto first = orders.get(0);
            String orderRouteDate = DisRouteOrderArriveDateHelper.resolveOrderRouteDate(first);
            if (orderRouteDate == null) {
                orderRouteDate = routeDate;
            }
            NxDisShipmentTaskEntity task = new NxDisShipmentTaskEntity();
            task.setNxDstDistributerId(disId);
            task.setNxDstRouteDate(orderRouteDate);
            task.setNxDstDepFatherId(depId);
            task.setNxDstDepName(resolveLiveDepartmentName(depId, first.getDepartmentName()));
            task.setNxDstLat(first.getLat());
            task.setNxDstLng(first.getLng());
            task.setNxDstAddress(first.getAddress());
            task.setNxDstStatus(isValidCoordinate(first.getLat(), first.getLng())
                    ? DisShipmentTaskStatus.SIMULATED : DisShipmentTaskStatus.UNASSIGNED);
            task.setSandboxStopKey(DisRouteSandboxStopKeyUtils.build(depId));
            task.setStopSource(DisRouteSandboxStopSource.SANDBOX_SUGGESTED);
            task.setConfirmViaSandbox(true);
            task.setItems(buildVirtualItems(orders));
            disRouteDispatchSnapshotHelper.applyItemMetrics(task);

            applyTimeWindowOverrideToVirtualTask(task, overrideByDep.get(depId), sandboxDayOverrides.get(depId));
            disRouteDispatchSnapshotHelper.refreshTaskSnapshot(task, true);
            virtualTasks.add(task);
        }
        return virtualTasks;
    }

    private static void applyTimeWindowOverrideToVirtualTask(NxDisShipmentTaskEntity task,
                                                             NxDisShipmentTaskEntity taskOverride,
                                                             NxDisSandboxDayTimeWindowEntity dayOverride) {
        if (taskOverride != null) {
            task.setNxDstEarliestDeliveryTimeS(taskOverride.getNxDstEarliestDeliveryTimeS());
            task.setNxDstLatestDeliveryTimeS(taskOverride.getNxDstLatestDeliveryTimeS());
            task.setNxDstServiceMinutes(taskOverride.getNxDstServiceMinutes());
            task.setNxDstTimeWindowOverrideFlag(taskOverride.getNxDstTimeWindowOverrideFlag());
            task.setNxDstTimeWindowAdjustReason(taskOverride.getNxDstTimeWindowAdjustReason());
            return;
        }
        if (dayOverride == null) {
            return;
        }
        task.setNxDstEarliestDeliveryTimeS(dayOverride.getNxDsdtwEarliestDeliveryTimeS());
        task.setNxDstLatestDeliveryTimeS(dayOverride.getNxDsdtwLatestDeliveryTimeS());
        task.setNxDstServiceMinutes(dayOverride.getNxDsdtwServiceMinutes());
        task.setNxDstTimeWindowOverrideFlag(1);
        task.setNxDstTimeWindowAdjustReason(dayOverride.getNxDsdtwAdjustReason());
    }

    private Map<Integer, NxDisSandboxDayTimeWindowEntity> loadSandboxDayTimeWindowOverrides(Integer disId,
                                                                                            String routeDate) {
        Map<Integer, NxDisSandboxDayTimeWindowEntity> map = new HashMap<Integer, NxDisSandboxDayTimeWindowEntity>();
        if (disId == null || routeDate == null || routeDate.trim().isEmpty()) {
            return map;
        }
        List<NxDisSandboxDayTimeWindowEntity> rows = nxDisSandboxDayTimeWindowDao.queryByDisRouteDate(
                disId, routeDate.trim());
        if (rows == null) {
            return map;
        }
        for (NxDisSandboxDayTimeWindowEntity row : rows) {
            if (row != null && row.getNxDsdtwDepFatherId() != null) {
                map.put(row.getNxDsdtwDepFatherId(), row);
            }
        }
        return map;
    }

    private Map<Integer, NxDisShipmentTaskEntity> loadTimeWindowOverrides(List<NxDisShipmentTaskEntity> dbTasks) {
        Map<Integer, NxDisShipmentTaskEntity> map = new HashMap<Integer, NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : dbTasks) {
            if (task == null || !DisRouteSandboxStopTimeWindowResolver.isActiveOverrideSourceTask(task)) {
                continue;
            }
            if (task.getNxDstDepFatherId() != null) {
                map.put(task.getNxDstDepFatherId(), task);
            }
        }
        return map;
    }

    private List<NxDisShipmentTaskItemEntity> buildVirtualItems(List<DisRouteOrderSnapshotDto> orders) {
        List<NxDisShipmentTaskItemEntity> items = new ArrayList<NxDisShipmentTaskItemEntity>();
        for (DisRouteOrderSnapshotDto order : orders) {
            if (order.getOrderId() == null) {
                continue;
            }
            NxDisShipmentTaskItemEntity item = new NxDisShipmentTaskItemEntity();
            item.setNxDstiLiveOrderId(order.getOrderId());
            item.setNxDstiItemStatus(ACTIVE);
            item.setNxDstiGoodsName(order.getGoodsName());
            item.setNxDstiQuantity(order.getQuantity());
            item.setNxDstiStandard(order.getStandard());
            item.setNxDstiRemark(order.getRemark());
            item.setOrderResolved(true);
            items.add(item);
        }
        return items;
    }

    private List<NxDisShipmentTaskEntity> filterOptimizable(List<NxDisShipmentTaskEntity> virtualTasks,
                                                            Set<Integer> confirmedDepIds) {
        List<NxDisShipmentTaskEntity> list = new ArrayList<NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : virtualTasks) {
            if (DispatchStrategyOptimizerEligibility.isPendingOptimizerCandidate(task, confirmedDepIds)) {
                list.add(task);
            }
        }
        return list;
    }

    private RouteOptimizeResult runOptimization(GeoPoint depot,
                                                List<NxDistributerUserEntity> drivers,
                                                List<NxDisShipmentTaskEntity> optimizableTasks) throws Exception {
        depot = resolveDepotForOptimization(depot, optimizableTasks);
        if (depot == null) {
            return null;
        }
        List<RouteStopInput> stopInputs = new ArrayList<RouteStopInput>();
        for (NxDisShipmentTaskEntity task : optimizableTasks) {
            RouteStopInput input = new RouteStopInput();
            input.setStopKey(String.valueOf(task.getNxDstDepFatherId()));
            input.setDepartmentId(task.getNxDstDepFatherId());
            input.setDepartmentName(task.getNxDstDepName());
            input.setLocation(toPoint(task.getNxDstLat(), task.getNxDstLng()));
            input.setAddress(task.getNxDstAddress());
            input.setOrderCount(task.getItems() != null ? task.getItems().size() : 0);
            stopInputs.add(input);
        }
        RouteCostProvider costProvider = new ResilientTencentRouteCostProvider(tencentMatrixRouteCostProvider);
        CostMatrix matrix = costProvider.buildMatrix(depot, stopInputs);
        RouteOptimizeRequest optimizeRequest = new RouteOptimizeRequest();
        optimizeRequest.setDepot(depot);
        optimizeRequest.setStops(stopInputs);
        optimizeRequest.setCostMatrix(matrix);
        optimizeRequest.setOptimizerType(RouteOptimizerType.BALANCED_INSERTION_2OPT);
        optimizeRequest.setDrivers(toDriverInputs(drivers));
        return routeEngineRegistry.optimizer(RouteOptimizerType.BALANCED_INSERTION_2OPT).optimize(optimizeRequest);
    }

    private void applyOptimizationInMemory(List<NxDisShipmentTaskEntity> optimizableTasks,
                                             RouteOptimizeResult optimizeResult,
                                             List<NxDisRouteStopEntity> suggestedStops,
                                             List<NxDisRouteStopEntity> unassignedStops,
                                             List<NxDistributerUserEntity> dispatchEligibleDrivers) {
        Map<Integer, String> driverNameById = buildDriverNameIndex(dispatchEligibleDrivers);
        if (optimizeResult == null) {
            for (NxDisShipmentTaskEntity task : optimizableTasks) {
                unassignedStops.add(buildUnassignedStop(task));
            }
            return;
        }
        Map<Integer, NxDisShipmentTaskEntity> taskByDep = new HashMap<Integer, NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : optimizableTasks) {
            taskByDep.put(task.getNxDstDepFatherId(), task);
        }
        Set<Integer> assignedDeps = new HashSet<Integer>();
        for (OptimizedDriverRouteResult driverRoute : optimizeResult.getDriverRoutes()) {
            if (driverRoute.getStops() == null) {
                continue;
            }
            Integer driverUserId = driverRoute.getDriverUserId();
            String driverName = driverNameById.get(driverUserId);
            for (OptimizedStopResult stopResult : driverRoute.getStops()) {
                NxDisShipmentTaskEntity task = taskByDep.get(stopResult.getDepartmentId());
                if (task == null) {
                    continue;
                }
                task.setNxDstSuggestedDriverUserId(driverUserId);
                task.setSuggestedDriverUserId(driverUserId);
                task.setSuggestedDriverName(driverName);
                NxDisRouteStopEntity stop = buildStopShellFromTask(task, stopResult.getStopSeq());
                stop.setSuggestedDriverUserId(driverUserId);
                stop.setSuggestedDriverName(driverName);
                stop.setNxDrsLegDistanceM(stopResult.getLegDistanceM());
                stop.setNxDrsLegDurationS(stopResult.getLegDurationS());
                stop.setLegDistanceProvider(stopResult.getDistanceProvider());
                stop.setLegDistanceType(stopResult.getDistanceType());
                task.setNxDstLegDistanceM(stopResult.getLegDistanceM());
                task.setNxDstLegDurationS(stopResult.getLegDurationS());
                task.setLegDistanceProvider(stopResult.getDistanceProvider());
                task.setLegDistanceType(stopResult.getDistanceType());
                disRouteDispatchSnapshotHelper.copyTaskSnapshotToStop(task, stop);
                suggestedStops.add(stop);
                assignedDeps.add(task.getNxDstDepFatherId());
            }
        }
        for (NxDisShipmentTaskEntity task : optimizableTasks) {
            if (!assignedDeps.contains(task.getNxDstDepFatherId())) {
                unassignedStops.add(buildUnassignedStop(task));
            }
        }
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

    private Set<Integer> applyHistoryDriverBindingsFromPlan(DispatchAssignmentPlan plan,
                                                            List<NxDisShipmentTaskEntity> optimizableTasks,
                                                            List<NxDisRouteStopEntity> suggestedStops,
                                                            List<NxDistributerUserEntity> dispatchEligibleDrivers) {
        Set<Integer> boundDepIds = new HashSet<Integer>();
        if (plan == null || plan.getDriverRoutes() == null || optimizableTasks == null) {
            return boundDepIds;
        }
        Map<Integer, NxDisShipmentTaskEntity> taskByDep = new HashMap<Integer, NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : optimizableTasks) {
            if (task != null && task.getNxDstDepFatherId() != null) {
                taskByDep.put(task.getNxDstDepFatherId(), task);
            }
        }
        Map<Integer, String> driverNameById = buildDriverNameIndex(dispatchEligibleDrivers);
        for (DriverRoutePlan driverRoute : plan.getDriverRoutes()) {
            if (driverRoute == null || driverRoute.getStops() == null) {
                continue;
            }
            Integer driverUserId = driverRoute.getDriverUserId();
            String driverName = driverRoute.getDriverName();
            if (driverName == null && driverUserId != null) {
                driverName = driverNameById.get(driverUserId);
            }
            for (StopAssignment assignment : driverRoute.getStops()) {
                if (assignment == null || assignment.getDepFatherId() == null) {
                    continue;
                }
                NxDisShipmentTaskEntity task = taskByDep.get(assignment.getDepFatherId());
                if (task == null) {
                    continue;
                }
                task.setNxDstSuggestedDriverUserId(driverUserId);
                task.setSuggestedDriverUserId(driverUserId);
                task.setSuggestedDriverName(driverName);
                int stopSeq = assignment.getStopSeq() > 0 ? assignment.getStopSeq() : 1;
                NxDisRouteStopEntity stop = buildStopShellFromTask(task, stopSeq);
                stop.setSuggestedDriverUserId(driverUserId);
                stop.setSuggestedDriverName(driverName);
                disRouteDispatchSnapshotHelper.copyTaskSnapshotToStop(task, stop);
                suggestedStops.add(stop);
                boundDepIds.add(task.getNxDstDepFatherId());
            }
        }
        return boundDepIds;
    }

    private static List<NxDisShipmentTaskEntity> filterPendingOptimizerCandidates(
            List<NxDisShipmentTaskEntity> optimizableTasks,
            Set<Integer> historyBoundDepIds,
            Set<Integer> confirmedDepIds) {
        List<NxDisShipmentTaskEntity> filtered = new ArrayList<NxDisShipmentTaskEntity>();
        if (optimizableTasks == null || optimizableTasks.isEmpty()) {
            return filtered;
        }
        Set<Integer> excludedDepIds = new HashSet<Integer>();
        if (historyBoundDepIds != null) {
            excludedDepIds.addAll(historyBoundDepIds);
        }
        if (confirmedDepIds != null) {
            excludedDepIds.addAll(confirmedDepIds);
        }
        for (NxDisShipmentTaskEntity task : optimizableTasks) {
            if (task == null || task.getNxDstDepFatherId() == null) {
                continue;
            }
            if (excludedDepIds.contains(task.getNxDstDepFatherId())) {
                continue;
            }
            if (!DispatchStrategyOptimizerEligibility.isPendingOptimizerCandidate(task, confirmedDepIds)) {
                continue;
            }
            filtered.add(task);
        }
        return filtered;
    }

    private static void enrichPlanFrozenDebug(DispatchAssignmentPlan plan,
                                              Set<Integer> confirmedDepIds,
                                              List<NxDisRouteStopEntity> confirmedStops) {
        if (plan == null) {
            return;
        }
        Set<Integer> seen = new HashSet<Integer>();
        if (confirmedDepIds != null) {
            for (Integer depId : confirmedDepIds) {
                if (depId == null || !seen.add(depId)) {
                    continue;
                }
                DispatchStrategyOptimizerEligibility.addFrozenDebugEntry(
                        plan, depId, DisRouteSandboxStopKeyUtils.build(depId), "confirmedOrExecuting");
            }
        }
        if (confirmedStops != null) {
            for (NxDisRouteStopEntity stop : confirmedStops) {
                if (stop == null) {
                    continue;
                }
                Integer depId = stop.getNxDrsDepartmentId();
                if (depId == null && stop.getShipmentTask() != null) {
                    depId = stop.getShipmentTask().getNxDstDepFatherId();
                }
                if (depId == null || !seen.add(depId)) {
                    continue;
                }
                String reason = "confirmedStop";
                NxDisShipmentTaskEntity task = stop.getShipmentTask();
                if (task != null && task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1) {
                    reason = "manualLocked";
                }
                DispatchStrategyOptimizerEligibility.addFrozenDebugEntry(
                        plan, depId, stop.getSandboxStopKey(), reason);
            }
        }
        plan.setFrozenStopCount(plan.getFrozenStops() != null ? plan.getFrozenStops().size() : 0);
        if (plan.getFrozenStopCount() > 0) {
            plan.getWarnings().add("frozen stops excluded from legacy optimizer count="
                    + plan.getFrozenStopCount());
        }
    }

    private NxDisRouteStopEntity buildStopShellFromTask(NxDisShipmentTaskEntity task, int stopSeq) {
        NxDisRouteStopEntity stop = new NxDisRouteStopEntity();
        stop.setNxDrsStopSeq(stopSeq);
        stop.setNxDrsDepartmentId(task.getNxDstDepFatherId());
        stop.setNxDrsDepartmentName(task.getNxDstDepName());
        stop.setNxDrsLat(task.getNxDstLat());
        stop.setNxDrsLng(task.getNxDstLng());
        stop.setNxDrsAddress(task.getNxDstAddress());
        if (task.getNxDstId() != null) {
            stop.setNxDrsShipmentTaskId(task.getNxDstId());
        }
        stop.setShipmentTask(task);
        stop.setSandboxStopKey(task.getSandboxStopKey());
        stop.setStopSource(task.getStopSource());
        stop.setConfirmViaSandbox(task.getConfirmViaSandbox());
        return stop;
    }

    private NxDisRouteStopEntity buildUnassignedStop(NxDisShipmentTaskEntity task) {
        task.setStopSource(DisRouteSandboxStopSource.UNASSIGNED);
        NxDisRouteStopEntity stop = buildStopShellFromTask(task, 0);
        stop.setStopSource(DisRouteSandboxStopSource.UNASSIGNED);
        return stop;
    }

    private void markSandboxMetadata(List<NxDisRouteStopEntity> stops, String source, boolean confirmViaSandbox) {
        for (NxDisRouteStopEntity stop : stops) {
            stop.setStopSource(source);
            stop.setConfirmViaSandbox(confirmViaSandbox);
            if (stop.getSandboxStopKey() == null && stop.getNxDrsDepartmentId() != null) {
                stop.setSandboxStopKey(DisRouteSandboxStopKeyUtils.build(stop.getNxDrsDepartmentId()));
            }
            if (stop.getShipmentTask() != null) {
                stop.getShipmentTask().setStopSource(source);
                stop.getShipmentTask().setConfirmViaSandbox(confirmViaSandbox);
                if (stop.getShipmentTask().getSandboxStopKey() == null) {
                    stop.getShipmentTask().setSandboxStopKey(stop.getSandboxStopKey());
                }
            }
        }
    }


    private List<NxDisShipmentTaskEntity> collectDisplayTasks(NxDisRoutePlanEntity plan) {
        List<NxDisShipmentTaskEntity> tasks = new ArrayList<NxDisShipmentTaskEntity>();
        if (plan == null || plan.getDriverRoutes() == null) {
            return tasks;
        }
        Set<Integer> seen = new HashSet<Integer>();
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            if (route.getStops() == null) {
                continue;
            }
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop.getShipmentTask() == null) {
                    continue;
                }
                NxDisShipmentTaskEntity task = stop.getShipmentTask();
                Integer key = task.getNxDstId() != null ? task.getNxDstId() : task.getNxDstDepFatherId();
                if (seen.add(key)) {
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    private List<NxDistributerUserEntity> listOnDutyDrivers(Integer disId, String routeDate) {
        try {
            return disDriverDutyService.listOnDutyDriverUsers(disId, routeDate);
        } catch (IllegalArgumentException ex) {
            return new ArrayList<NxDistributerUserEntity>();
        }
    }

    private Map<Integer, List<DisRouteOrderSnapshotDto>> groupOrdersByDepartment(
            List<DisRouteOrderSnapshotDto> orderRows) {
        Map<Integer, List<DisRouteOrderSnapshotDto>> grouped = new LinkedHashMap<Integer, List<DisRouteOrderSnapshotDto>>();
        if (orderRows == null) {
            return grouped;
        }
        for (DisRouteOrderSnapshotDto row : orderRows) {
            if (!grouped.containsKey(row.getDepartmentId())) {
                grouped.put(row.getDepartmentId(), new ArrayList<DisRouteOrderSnapshotDto>());
            }
            grouped.get(row.getDepartmentId()).add(row);
        }
        return grouped;
    }

    private List<DriverInput> toDriverInputs(List<NxDistributerUserEntity> drivers) {
        List<DriverInput> inputs = new ArrayList<DriverInput>();
        for (NxDistributerUserEntity driver : drivers) {
            DriverInput input = new DriverInput();
            input.setDriverUserId(driver.getNxDistributerUserId());
            input.setDriverName(driver.getNxDiuWxNickName());
            inputs.add(input);
        }
        return inputs;
    }

    private DeliveryHistoryPreferenceBatchResult resolveDeliveryHistoryPreferences(
            Integer disId,
            String routeDate,
            List<NxDisShipmentTaskEntity> virtualTasks,
            List<NxDistributerUserEntity> dispatchEligibleDrivers) {
        List<Integer> pendingDepIds = extractDepFatherIds(virtualTasks);
        List<Integer> eligibleDriverIds = extractDriverUserIds(dispatchEligibleDrivers);
        return disRouteDeliveryHistoryPreferenceService.resolve(
                DeliveryHistoryPreferenceResolveRequest.of(
                        disId, pendingDepIds, eligibleDriverIds, routeDate));
    }

    private static List<Integer> extractDepFatherIds(List<NxDisShipmentTaskEntity> virtualTasks) {
        List<Integer> depIds = new ArrayList<Integer>();
        if (virtualTasks == null) {
            return depIds;
        }
        Set<Integer> seen = new LinkedHashSet<Integer>();
        for (NxDisShipmentTaskEntity task : virtualTasks) {
            if (task == null || task.getNxDstDepFatherId() == null) {
                continue;
            }
            if (seen.add(task.getNxDstDepFatherId())) {
                depIds.add(task.getNxDstDepFatherId());
            }
        }
        return depIds;
    }

    private static List<Integer> extractDriverUserIds(List<NxDistributerUserEntity> drivers) {
        List<Integer> ids = new ArrayList<Integer>();
        if (drivers == null) {
            return ids;
        }
        Set<Integer> seen = new LinkedHashSet<Integer>();
        for (NxDistributerUserEntity driver : drivers) {
            if (driver == null || driver.getNxDistributerUserId() == null) {
                continue;
            }
            if (seen.add(driver.getNxDistributerUserId())) {
                ids.add(driver.getNxDistributerUserId());
            }
        }
        return ids;
    }

    private boolean isTaskProtected(NxDisShipmentTaskEntity task) {
        if (task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1) {
            return true;
        }
        String status = task.getNxDstStatus();
        return DisShipmentTaskStatus.ASSIGNED.equals(status)
                || DisShipmentTaskStatus.READY_TO_GO.equals(status)
                || DisShipmentTaskStatus.IN_DELIVERY.equals(status)
                || DisShipmentTaskStatus.DELIVERED.equals(status)
                || DisShipmentTaskStatus.EXCEPTION.equals(status);
    }

    /**
     * 仅「在途派单」锁定客户部门：阻止同 dep 新 live 单进入 suggested。
     * 已 DELIVERED / EXCEPTION 的历史 task 仍展示在执行区，但不阻挡同店补单。
     */
    private static boolean isDispatchLockedDepForNewOrders(NxDisShipmentTaskEntity task) {
        if (task == null) {
            return false;
        }
        String status = task.getNxDstStatus();
        return DisShipmentTaskStatus.ASSIGNED.equals(status)
                || DisShipmentTaskStatus.READY_TO_GO.equals(status)
                || DisShipmentTaskStatus.IN_DELIVERY.equals(status);
    }

    /** Phase 3a：展示用客户名始终读 nx_department 最新值（仅内存读模型，不写库）。 */
    private void applyLiveDepartmentNames(NxDisRoutePlanEntity plan,
                                          List<NxDisRouteStopEntity> confirmedStops,
                                          List<NxDisRouteStopEntity> suggestedStops,
                                          List<NxDisRouteStopEntity> unassignedStops) {
        Map<Integer, String> liveNameByDep = new HashMap<Integer, String>();
        if (plan != null && plan.getDriverRoutes() != null) {
            for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
                if (route == null || route.getStops() == null) {
                    continue;
                }
                for (NxDisRouteStopEntity stop : route.getStops()) {
                    applyLiveDepartmentNameToStop(stop, liveNameByDep);
                }
            }
        }
        applyLiveDepartmentNamesToList(confirmedStops, liveNameByDep);
        applyLiveDepartmentNamesToList(suggestedStops, liveNameByDep);
        applyLiveDepartmentNamesToList(unassignedStops, liveNameByDep);
    }

    private void applyLiveDepartmentNamesToList(List<NxDisRouteStopEntity> stops,
                                              Map<Integer, String> liveNameByDep) {
        if (stops == null || stops.isEmpty()) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            applyLiveDepartmentNameToStop(stop, liveNameByDep);
        }
    }

    private void applyLiveDepartmentNameToStop(NxDisRouteStopEntity stop,
                                               Map<Integer, String> liveNameByDep) {
        if (stop == null) {
            return;
        }
        Integer depId = stop.getNxDrsDepartmentId();
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (depId == null && task != null) {
            depId = task.getNxDstDepFatherId();
        }
        if (depId == null) {
            return;
        }
        String liveName = resolveLiveDepartmentName(depId, liveNameByDep.get(depId));
        if (liveName == null || liveName.trim().isEmpty()) {
            return;
        }
        liveNameByDep.put(depId, liveName);
        stop.setLiveDepartmentName(liveName);
        stop.setNxDrsDepartmentName(liveName);
        if (task != null) {
            task.setLiveDepartmentName(liveName);
            task.setNxDstDepName(liveName);
        }
    }

    private String resolveLiveDepartmentName(Integer depId, String fallback) {
        if (depId == null) {
            return fallback;
        }
        NxDepartmentEntity department = nxDepartmentDao.queryObject(depId);
        if (department != null && department.getNxDepartmentName() != null
                && !department.getNxDepartmentName().trim().isEmpty()) {
            return department.getNxDepartmentName().trim();
        }
        return fallback;
    }

    private void sanitizeConfirmedForOffDutyDrivers(List<NxDisRouteStopEntity> confirmedStops,
                                                    Set<Integer> confirmedDepIds,
                                                    List<NxDisRouteStopEntity> unassignedStops,
                                                    Set<Integer> offDutyDriverIds) {
        if (confirmedStops == null || confirmedStops.isEmpty()
                || offDutyDriverIds == null || offDutyDriverIds.isEmpty()) {
            return;
        }
        List<NxDisRouteStopEntity> toMove = new ArrayList<NxDisRouteStopEntity>();
        for (NxDisRouteStopEntity stop : confirmedStops) {
            if (stop == null || stop.getShipmentTask() == null) {
                continue;
            }
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            Integer driverId = task.getNxDstAssignedDriverUserId();
            if (driverId == null || !offDutyDriverIds.contains(driverId)) {
                continue;
            }
            if (!DisRouteSandboxDispatchEligibilityHelper.shouldDemoteConfirmedStopForOffDutyDriver(
                    task, driverId, nxDisDriverRouteDao)) {
                continue;
            }
            toMove.add(stop);
        }
        if (toMove.isEmpty()) {
            return;
        }
        confirmedStops.removeAll(toMove);
        if (unassignedStops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : toMove) {
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task != null && task.getNxDstDepFatherId() != null && confirmedDepIds != null) {
                confirmedDepIds.remove(task.getNxDstDepFatherId());
            }
            if (task != null) {
                task.setSuggestedDriverUserId(null);
                task.setSuggestedDriverName(null);
            }
            stop.setSuggestedDriverUserId(null);
            stop.setSuggestedDriverName(null);
            stop.setStopSource(DisRouteSandboxStopSource.UNASSIGNED);
            unassignedStops.add(buildUnassignedStop(task));
        }
    }

    private Set<Integer> resolveOffDutyDriverUserIds(Integer disId,
                                                     String routeDate,
                                                     List<NxDistributerUserEntity> onDutyDrivers) {
        Set<Integer> allDriverIds = new LinkedHashSet<Integer>();
        try {
            List<NxDistributerUserEntity> allDrivers = disRouteDispatchService.listDrivers(disId);
            if (allDrivers != null) {
                for (NxDistributerUserEntity driver : allDrivers) {
                    if (driver != null && driver.getNxDistributerUserId() != null) {
                        allDriverIds.add(driver.getNxDistributerUserId());
                    }
                }
            }
        } catch (RuntimeException ex) {
            allDriverIds.clear();
        }
        if (allDriverIds.isEmpty() && onDutyDrivers != null) {
            for (NxDistributerUserEntity driver : onDutyDrivers) {
                if (driver != null && driver.getNxDistributerUserId() != null) {
                    allDriverIds.add(driver.getNxDistributerUserId());
                }
            }
        }
        return DisRouteSandboxDispatchEligibilityHelper.resolveOffDutyDriverUserIds(
                nxDisDriverDutyDao, disId, routeDate, allDriverIds);
    }

    private List<NxDistributerUserEntity> filterDispatchEligibleDrivers(
            List<NxDistributerUserEntity> onDutyDrivers,
            Set<Integer> sandboxIneligibleDrivers) {
        if (onDutyDrivers == null || onDutyDrivers.isEmpty()) {
            return new ArrayList<NxDistributerUserEntity>();
        }
        List<NxDistributerUserEntity> eligible = new ArrayList<NxDistributerUserEntity>();
        for (NxDistributerUserEntity driver : onDutyDrivers) {
            if (driver == null || driver.getNxDistributerUserId() == null) {
                continue;
            }
            if (DisRouteSandboxDispatchEligibilityHelper.isDriverEligibleForSandboxDispatch(
                    driver.getNxDistributerUserId(), sandboxIneligibleDrivers)) {
                eligible.add(driver);
            }
        }
        return eligible;
    }

    private void reassignOptimizedUnassignedToEligibleDrivers(
            List<NxDisRouteStopEntity> suggestedStops,
            List<NxDisRouteStopEntity> unassignedStops,
            List<NxDistributerUserEntity> dispatchEligibleDrivers,
            Map<Integer, String> driverNameById) {
        if (unassignedStops == null || unassignedStops.isEmpty()
                || dispatchEligibleDrivers == null || dispatchEligibleDrivers.isEmpty()) {
            return;
        }
        List<NxDisRouteStopEntity> toMove = new ArrayList<NxDisRouteStopEntity>();
        for (NxDisRouteStopEntity stop : unassignedStops) {
            if (stop == null || !hasOptimizedLegMetrics(stop)) {
                continue;
            }
            toMove.add(stop);
        }
        if (toMove.isEmpty()) {
            return;
        }
        unassignedStops.removeAll(toMove);
        int driverIdx = 0;
        for (NxDisRouteStopEntity stop : toMove) {
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task == null) {
                unassignedStops.add(stop);
                continue;
            }
            NxDistributerUserEntity driver = dispatchEligibleDrivers.get(
                    driverIdx % dispatchEligibleDrivers.size());
            driverIdx++;
            Integer driverUserId = driver.getNxDistributerUserId();
            String driverName = driverNameById.get(driverUserId);
            task.setNxDstSuggestedDriverUserId(driverUserId);
            task.setSuggestedDriverUserId(driverUserId);
            task.setSuggestedDriverName(driverName);
            stop.setSuggestedDriverUserId(driverUserId);
            stop.setSuggestedDriverName(driverName);
            stop.setStopSource(DisRouteSandboxStopSource.SANDBOX_SUGGESTED);
            task.setStopSource(DisRouteSandboxStopSource.SANDBOX_SUGGESTED);
            suggestedStops.add(stop);
        }
    }

    private static boolean hasOptimizedLegMetrics(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        if (stop.getNxDrsLegDistanceM() != null || stop.getNxDrsLegDurationS() != null) {
            return true;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        return task != null
                && (task.getNxDstLegDistanceM() != null || task.getNxDstLegDurationS() != null);
    }

    /** 已确认落库的客户不得再出现在系统建议列表（避免与已确认区重复）。 */
    private void sanitizeSuggestedAgainstConfirmedDepartments(List<NxDisRouteStopEntity> suggestedStops,
                                                              Set<Integer> confirmedDepIds) {
        if (suggestedStops == null || suggestedStops.isEmpty()
                || confirmedDepIds == null || confirmedDepIds.isEmpty()) {
            return;
        }
        java.util.Iterator<NxDisRouteStopEntity> iterator = suggestedStops.iterator();
        while (iterator.hasNext()) {
            NxDisRouteStopEntity stop = iterator.next();
            if (stop == null) {
                iterator.remove();
                continue;
            }
            Integer depId = stop.getNxDrsDepartmentId();
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (depId == null && task != null) {
                depId = task.getNxDstDepFatherId();
            }
            if (depId != null && confirmedDepIds.contains(depId)) {
                iterator.remove();
            }
        }
    }

    private void sanitizeSuggestedDriverAssignments(List<NxDisRouteStopEntity> suggestedStops,
                                                    List<NxDisRouteStopEntity> unassignedStops,
                                                    Set<Integer> sandboxIneligibleDrivers) {
        if (suggestedStops == null || suggestedStops.isEmpty()) {
            return;
        }
        List<NxDisRouteStopEntity> toMove = new ArrayList<NxDisRouteStopEntity>();
        for (NxDisRouteStopEntity stop : suggestedStops) {
            if (stop == null || stop.getShipmentTask() == null) {
                continue;
            }
            Integer driverId = stop.getSuggestedDriverUserId() != null
                    ? stop.getSuggestedDriverUserId()
                    : stop.getShipmentTask().getNxDstSuggestedDriverUserId();
            if (!DisRouteSandboxDispatchEligibilityHelper.isDriverEligibleForSandboxDispatch(
                    driverId, sandboxIneligibleDrivers)) {
                toMove.add(stop);
            }
        }
        if (toMove.isEmpty()) {
            return;
        }
        suggestedStops.removeAll(toMove);
        for (NxDisRouteStopEntity stop : toMove) {
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            task.setNxDstSuggestedDriverUserId(null);
            task.setSuggestedDriverUserId(null);
            task.setSuggestedDriverName(null);
            stop.setSuggestedDriverUserId(null);
            stop.setSuggestedDriverName(null);
            unassignedStops.add(buildUnassignedStop(task));
        }
    }

    private Map<Integer, String> buildDriverNameIndex(List<NxDistributerUserEntity> drivers) {
        Map<Integer, String> names = new HashMap<Integer, String>();
        if (drivers == null) {
            return names;
        }
        for (NxDistributerUserEntity driver : drivers) {
            if (driver == null || driver.getNxDistributerUserId() == null) {
                continue;
            }
            String name = driver.getNxDiuWxNickName();
            if (name != null && !name.trim().isEmpty()) {
                names.put(driver.getNxDistributerUserId(), name.trim());
            }
        }
        return names;
    }
}
