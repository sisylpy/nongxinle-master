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
import com.nongxinle.route.model.*;
import com.nongxinle.service.DisDriverDutyService;
import com.nongxinle.service.DisRouteSandboxComputeService;
import com.nongxinle.service.DisShipmentTaskService;
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
    private DisShipmentTaskService disShipmentTaskService;
    @Autowired
    private DisRouteDispatchSnapshotHelper disRouteDispatchSnapshotHelper;
    @Autowired
    private DisRouteDispatchReadIntegrityHelper disRouteDispatchReadIntegrityHelper;
    @Autowired
    private DisRouteShipmentTaskItemOrderResolver disRouteShipmentTaskItemOrderResolver;
    @Autowired
    private DisRouteSandboxSchedulePreviewHelper disRouteSandboxSchedulePreviewHelper;
    @Autowired
    private DisRouteSandboxLegMetricsHelper disRouteSandboxLegMetricsHelper;
    @Autowired
    private NxDisDriverDutyDao nxDisDriverDutyDao;
    @Autowired
    private NxDepartmentDao nxDepartmentDao;
    @Autowired
    private com.nongxinle.service.DisRouteDispatchService disRouteDispatchService;

    @Override
    public SandboxComputeResult compute(SandboxComputeRequest request) throws Exception {
        validateRequest(request);
        Integer disId = request.getDisId();
        String routeDate = resolveRouteDate(request.getRouteDate());
        String batchCode = normalizeBatch(request.getBatchCode());

        SandboxComputeResult result = new SandboxComputeResult();
        result.setRouteDate(routeDate);
        result.setDispatchBatch(batchCode);

        List<DisRouteOrderSnapshotDto> allOrders = queryEligibleOrders(disId);
        result.setEffectiveOrders(allOrders);

        NxDisRoutePlanEntity planContext = loadPlanContext(disId, routeDate, batchCode);
        result.setPlanContext(planContext);

        GeoPoint depot = resolveDepot(request, disId, planContext, allOrders);
        if (planContext != null && depot != null) {
            planContext.setNxDrpDepotLat(depot.getLat());
            planContext.setNxDrpDepotLng(depot.getLng());
        }

        List<NxDisShipmentTaskEntity> dbTasks = loadDbTasks(disId);
        attachItems(dbTasks);

        Set<Integer> confirmedDepIds = new HashSet<Integer>();
        List<NxDisRouteStopEntity> confirmedStops = new ArrayList<NxDisRouteStopEntity>();
        List<InvalidDispatchStopDto> invalidStops = new ArrayList<InvalidDispatchStopDto>();

        partitionDbTasks(dbTasks, disId, routeDate, planContext, confirmedDepIds, confirmedStops, invalidStops);
        supplementExecutionConfirmedStops(disId, routeDate, planContext, confirmedDepIds, confirmedStops, invalidStops);

        Map<Integer, List<DisRouteOrderSnapshotDto>> ordersByDep = groupOrdersByDepartment(allOrders);
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

        RouteOptimizeResult optimizeResult = null;

        if (!virtualTasks.isEmpty() && !dispatchEligibleDrivers.isEmpty()) {
            List<NxDisShipmentTaskEntity> optimizable = filterOptimizable(virtualTasks);
            if (!optimizable.isEmpty()) {
                optimizeResult = runOptimization(depot, dispatchEligibleDrivers, optimizable);
                applyOptimizationInMemory(optimizable, optimizeResult, suggestedStops, unassignedStops,
                        dispatchEligibleDrivers);
            } else {
                for (NxDisShipmentTaskEntity task : virtualTasks) {
                    unassignedStops.add(buildUnassignedStop(task));
                }
            }
        } else if (!virtualTasks.isEmpty()) {
            for (NxDisShipmentTaskEntity task : virtualTasks) {
                unassignedStops.add(buildUnassignedStop(task));
            }
        }
        sanitizeSuggestedDriverAssignments(suggestedStops, unassignedStops, dispatchBlockedDriverIds);
        reassignOptimizedUnassignedToEligibleDrivers(
                suggestedStops, unassignedStops, dispatchEligibleDrivers, buildDriverNameIndex(onDutyDrivers));

        markSandboxMetadata(suggestedStops, DisRouteSandboxStopSource.SANDBOX_SUGGESTED, true);
        markSandboxMetadata(unassignedStops, DisRouteSandboxStopSource.UNASSIGNED, true);
        markSandboxMetadata(confirmedStops, DisRouteSandboxStopSource.CONFIRMED, false);

        unassignedStops = DisRouteSandboxUnassignedStopHelper.consolidateByDepartment(unassignedStops);

        result.setConfirmedStops(confirmedStops);
        result.setSandboxSuggestedStops(suggestedStops);
        result.setUnassignedStops(unassignedStops);
        result.setInvalidStops(invalidStops);
        result.setHasLockedStops(!confirmedStops.isEmpty());

        NxDisRoutePlanEntity mergedPlan = buildMergedPlan(
                planContext, disId, routeDate, batchCode, depot, onDutyDrivers,
                confirmedStops, suggestedStops, unassignedStops, dispatchBlockedDriverIds, offDutyDriverIds);
        hydrateExecutionRouteSnapshots(mergedPlan);
        disRouteSandboxLegMetricsHelper.applyToPlan(depot, mergedPlan);
        try {
            disRouteSandboxLegMetricsHelper.applyDepotLegToStops(depot, unassignedStops);
        } catch (IOException ex) {
            // 未分配 leg 失败时不阻断整页
        }
        applySchedulePreviewToMergedPlan(mergedPlan, routeDate);
        applySchedulePreviewToUnassignedStops(unassignedStops, routeDate);
        reconcileExecutionRoutesAfterSnapshot(mergedPlan);
        applyLiveDepartmentNames(mergedPlan, confirmedStops, suggestedStops, unassignedStops);
        result.setMergedPlan(mergedPlan);

        List<NxDisShipmentTaskEntity> displayTasks = collectDisplayTasks(mergedPlan);
        result.setAllDisplayTasks(displayTasks);

        result.setOrderVersion(buildOrderVersion(allOrders));
        result.setDutyVersion(buildDutyVersion(disId, routeDate, onDutyDrivers));
        result.setSandboxVersion(buildSandboxVersion(result));
        result.setHasNewOrders(!allOrders.isEmpty() && confirmedDepIds.size() < ordersByDep.size());
        result.setHasOrderChanges(false);

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

    private List<DisRouteOrderSnapshotDto> queryEligibleOrders(Integer disId) {
        return nxDisRoutePlanDao.queryEligibleLiveOrderSnapshots(disId, null, null);
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

    private List<NxDisShipmentTaskEntity> loadDbTasks(Integer disId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("disId", disId);
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
                if (task.getNxDstDepFatherId() != null) {
                    confirmedDepIds.add(task.getNxDstDepFatherId());
                }
                if (hasValidItems) {
                    confirmedStops.add(buildConfirmedStopFromDb(task, planContext));
                } else {
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
            if (task.getNxDstDepFatherId() != null) {
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
            disRouteShipmentTaskItemOrderResolver.enrichItems(task.getItems());

            NxDisShipmentTaskEntity override = overrideByDep.get(depId);
            if (override != null) {
                task.setNxDstEarliestDeliveryTimeS(override.getNxDstEarliestDeliveryTimeS());
                task.setNxDstLatestDeliveryTimeS(override.getNxDstLatestDeliveryTimeS());
                task.setNxDstServiceMinutes(override.getNxDstServiceMinutes());
                task.setNxDstTimeWindowOverrideFlag(override.getNxDstTimeWindowOverrideFlag());
                task.setNxDstTimeWindowAdjustReason(override.getNxDstTimeWindowAdjustReason());
            }
            disRouteDispatchSnapshotHelper.refreshTaskSnapshot(task, true);
            virtualTasks.add(task);
        }
        return virtualTasks;
    }

    private Map<Integer, NxDisShipmentTaskEntity> loadTimeWindowOverrides(List<NxDisShipmentTaskEntity> dbTasks) {
        Map<Integer, NxDisShipmentTaskEntity> map = new HashMap<Integer, NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : dbTasks) {
            if (task == null || !isTaskProtected(task)) {
                continue;
            }
            if (task.getNxDstTimeWindowOverrideFlag() != null && task.getNxDstTimeWindowOverrideFlag() == 1) {
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
            items.add(item);
        }
        return items;
    }

    private List<NxDisShipmentTaskEntity> filterOptimizable(List<NxDisShipmentTaskEntity> virtualTasks) {
        List<NxDisShipmentTaskEntity> list = new ArrayList<NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : virtualTasks) {
            if (isValidCoordinate(task.getNxDstLat(), task.getNxDstLng())
                    && DisShipmentTaskStatus.SIMULATED.equals(task.getNxDstStatus())) {
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
                copyTaskSnapshotToStop(task, stop);
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

    private void copyTaskSnapshotToStop(NxDisShipmentTaskEntity task, NxDisRouteStopEntity stop) {
        disRouteDispatchSnapshotHelper.copyTaskSnapshotToStop(task, stop);
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

    private NxDisRoutePlanEntity buildMergedPlan(NxDisRoutePlanEntity planContext,
                                                 Integer disId,
                                                 String routeDate,
                                                 String batchCode,
                                                 GeoPoint depot,
                                                 List<NxDistributerUserEntity> onDutyDrivers,
                                                 List<NxDisRouteStopEntity> confirmedStops,
                                                 List<NxDisRouteStopEntity> suggestedStops,
                                                 List<NxDisRouteStopEntity> unassignedStops,
                                                 Set<Integer> sandboxIneligibleDrivers,
                                                 Set<Integer> offDutyDriverIds) {
        NxDisRoutePlanEntity plan = planContext != null ? planContext : new NxDisRoutePlanEntity();
        plan.setNxDrpDistributerId(disId);
        plan.setNxDrpRouteDate(routeDate);
        plan.setNxDrpDispatchBatch(batchCode);
        if (depot != null) {
            plan.setNxDrpDepotLat(depot.getLat());
            plan.setNxDrpDepotLng(depot.getLng());
        }

        Map<Integer, NxDisDriverRouteEntity> routeByDriver = new LinkedHashMap<Integer, NxDisDriverRouteEntity>();
        int routeSeq = 1;
        for (NxDistributerUserEntity driver : onDutyDrivers) {
            NxDisDriverRouteEntity route = new NxDisDriverRouteEntity();
            route.setNxDdrDriverUserId(driver.getNxDistributerUserId());
            route.setNxDdrRouteSeq(routeSeq++);
            route.setDriverName(driver.getNxDiuWxNickName());
            route.setStops(new ArrayList<NxDisRouteStopEntity>());
            route.setNxDdrStopCount(0);
            routeByDriver.put(driver.getNxDistributerUserId(), route);
        }

        appendStopsToDriverRoutes(routeByDriver, confirmedStops, sandboxIneligibleDrivers);
        appendStopsToDriverRoutes(routeByDriver, suggestedStops, sandboxIneligibleDrivers);
        resolveDriverRouteIdsFromStops(routeByDriver);
        attachExecutionPlanContext(plan, routeByDriver);
        mergeDbDriverRoutes(routeByDriver, plan.getNxDrpId(), offDutyDriverIds);
        hydrateDriverRouteExecutionFromDb(routeByDriver);
        reconcileExecutionRouteReadState(routeByDriver);

        for (NxDisDriverRouteEntity route : routeByDriver.values()) {
            resequenceStops(route.getStops());
        }

        stabilizePlanStatusForExecution(plan, routeByDriver);
        plan.setDriverRoutes(new ArrayList<NxDisDriverRouteEntity>(routeByDriver.values()));
        return plan;
    }

    /** 出发后 plan 头不得为 SIMULATED/null；从 execution route 找回 DB plan。 */
    private void attachExecutionPlanContext(NxDisRoutePlanEntity plan,
                                            Map<Integer, NxDisDriverRouteEntity> routeByDriver) {
        if (plan == null || routeByDriver == null || routeByDriver.isEmpty()) {
            return;
        }
        List<NxDisDriverRouteEntity> routes = new ArrayList<NxDisDriverRouteEntity>(routeByDriver.values());
        if (DisRouteExecutionPlanHelper.isPersistedPlan(plan)) {
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null || route.getNxDdrId() == null) {
                continue;
            }
            NxDisDriverRouteEntity dbRoute = nxDisDriverRouteDao.queryObject(route.getNxDdrId());
            if (dbRoute != null) {
                DisRouteRouteExecutionHelper.mergeExecutionFieldsFromDb(route, dbRoute);
            }
        }
        DisRouteExecutionPlanHelper.attachPlanHeaderFromRoutes(plan, routes);
        if (plan.getNxDrpId() != null) {
            loadAndMergePlanHeader(plan, plan.getNxDrpId(), routes);
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null || route.getNxDdrPlanId() == null) {
                continue;
            }
            NxDisRoutePlanEntity dbPlan = nxDisRoutePlanDao.queryObject(route.getNxDdrPlanId());
            if (dbPlan != null && !DisRouteExecutionPlanHelper.isCancelledPlan(dbPlan)) {
                DisRouteExecutionPlanHelper.copyPlanFields(plan, dbPlan);
                break;
            }
        }
        if (plan.getNxDrpId() != null) {
            loadAndMergePlanHeader(plan, plan.getNxDrpId(), routes);
        }
    }

    private void loadAndMergePlanHeader(NxDisRoutePlanEntity plan,
                                        Integer planId,
                                        List<NxDisDriverRouteEntity> routes) {
        NxDisRoutePlanEntity dbPlan = nxDisRoutePlanDao.queryObject(planId);
        if (dbPlan == null) {
            return;
        }
        DisRouteExecutionPlanHelper.copyPlanFields(plan, dbPlan);
        if (DisRouteExecutionPlanHelper.hasExecutionDriverRoute(routes)
                && (DisRouteExecutionPlanHelper.isEmptySimulatedPlan(plan)
                || SIMULATED.equals(plan.getNxDrpStatus()))) {
            plan.setNxDrpStatus(ASSIGNED);
        }
    }

    private void reconcileExecutionRouteReadState(Map<Integer, NxDisDriverRouteEntity> routeByDriver) {
        if (routeByDriver == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : routeByDriver.values()) {
            if (route == null) {
                continue;
            }
            DisRouteRouteExecutionHelper.syncExecutionCanonicalFields(route);
        }
    }

    private void stabilizePlanStatusForExecution(NxDisRoutePlanEntity plan,
                                                 Map<Integer, NxDisDriverRouteEntity> routeByDriver) {
        if (plan == null || routeByDriver == null) {
            return;
        }
        if (DisRouteExecutionPlanHelper.hasExecutionDriverRoute(
                new ArrayList<NxDisDriverRouteEntity>(routeByDriver.values()))) {
            if (plan.getNxDrpId() == null || DisRouteExecutionPlanHelper.isEmptySimulatedPlan(plan)
                    || SIMULATED.equals(plan.getNxDrpStatus())) {
                if (plan.getNxDrpId() != null) {
                    plan.setNxDrpStatus(ASSIGNED);
                }
            }
        }
    }

    private void applySchedulePreviewToMergedPlan(NxDisRoutePlanEntity plan, String routeDate) {
        if (plan == null || plan.getDriverRoutes() == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            String scheduleRouteDate = resolveScheduleRouteDate(route, routeDate);
            disRouteSandboxSchedulePreviewHelper.applySchedulePreview(plan, route, scheduleRouteDate);
        }
        DisRouteSandboxPlanTimelineHelper.applyAggregatedTimeline(plan);
    }

    /** 未分配客户：单站临时路线排程预览（市场→客户）。 */
    private void applySchedulePreviewToUnassignedStops(List<NxDisRouteStopEntity> unassignedStops,
                                                       String routeDate) {
        if (unassignedStops == null || unassignedStops.isEmpty()) {
            return;
        }
        for (NxDisRouteStopEntity stop : unassignedStops) {
            if (stop == null) {
                continue;
            }
            NxDisDriverRouteEntity pseudoRoute = new NxDisDriverRouteEntity();
            pseudoRoute.setStops(new ArrayList<NxDisRouteStopEntity>());
            pseudoRoute.getStops().add(stop);
            stop.setNxDrsStopSeq(1);
            disRouteSandboxSchedulePreviewHelper.applySchedulePreview(null, pseudoRoute, routeDate);
        }
    }

    /** 已出发路线：先从 DB / legacy stop 恢复快照，再决定是否坐标重算 leg。 */
    private void hydrateExecutionRouteSnapshots(NxDisRoutePlanEntity plan) {
        if (plan == null || plan.getDriverRoutes() == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            if (route == null || !DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
                continue;
            }
            if (route.getNxDdrId() != null) {
                NxDisDriverRouteEntity dbRoute = nxDisDriverRouteDao.queryObject(route.getNxDdrId());
                if (dbRoute != null) {
                    DisRouteExecutionRouteSnapshotHelper.mergeRouteSnapshotFromDb(route, dbRoute);
                }
            }
            if (route.getStops() == null) {
                continue;
            }
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop == null || stop.getShipmentTask() == null) {
                    continue;
                }
                NxDisRouteStopEntity legacyStop = nxDisRouteStopDao.queryByShipmentTaskId(
                        stop.getShipmentTask().getNxDstId());
                DisRouteExecutionRouteSnapshotHelper.hydrateStopSnapshotFromPersistence(
                        stop, stop.getShipmentTask(), legacyStop);
            }
        }
    }

    /** leg/排程 enrichment 后重新锁定 execution 态，不丢失里程/标签。 */
    private void reconcileExecutionRoutesAfterSnapshot(NxDisRoutePlanEntity plan) {
        if (plan == null || plan.getDriverRoutes() == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            if (route == null || !DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
                continue;
            }
            DisRouteRouteExecutionHelper.syncExecutionCanonicalFields(route);
            if (route.getNxDdrStopCount() == null && route.getStops() != null) {
                route.setNxDdrStopCount(route.getStops().size());
            }
        }
    }

    private String resolveScheduleRouteDate(NxDisDriverRouteEntity route, String fallbackRouteDate) {
        if (route != null && route.getStops() != null) {
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop != null && stop.getShipmentTask() != null
                        && stop.getShipmentTask().getNxDstRouteDate() != null
                        && !stop.getShipmentTask().getNxDstRouteDate().trim().isEmpty()) {
                    return stop.getShipmentTask().getNxDstRouteDate().trim();
                }
            }
        }
        return fallbackRouteDate;
    }

    private void mergeDbDriverRoutes(Map<Integer, NxDisDriverRouteEntity> routeByDriver,
                                     Integer planId,
                                     Set<Integer> offDutyDriverIds) {
        if (planId == null) {
            return;
        }
        List<NxDisDriverRouteEntity> dbRoutes = nxDisDriverRouteDao.queryByPlanId(planId);
        if (dbRoutes == null || dbRoutes.isEmpty()) {
            return;
        }
        for (NxDisDriverRouteEntity dbRoute : dbRoutes) {
            if (dbRoute == null || dbRoute.getNxDdrDriverUserId() == null) {
                continue;
            }
            if (isOffDutySandboxRouteOnly(dbRoute, offDutyDriverIds)) {
                continue;
            }
            NxDisDriverRouteEntity existing = routeByDriver.get(dbRoute.getNxDdrDriverUserId());
            if (existing != null) {
                existing.setNxDdrId(dbRoute.getNxDdrId());
                DisRouteExecutionRouteSnapshotHelper.mergeRouteSnapshotFromDb(existing, dbRoute);
                if (existing.getDriverName() == null || existing.getDriverName().trim().isEmpty()) {
                    existing.setDriverName(dbRoute.getDriverName());
                }
                if (existing.getNxDdrRouteSeq() == null && dbRoute.getNxDdrRouteSeq() != null) {
                    existing.setNxDdrRouteSeq(dbRoute.getNxDdrRouteSeq());
                }
            } else {
                if (dbRoute.getStops() == null) {
                    dbRoute.setStops(new ArrayList<NxDisRouteStopEntity>());
                }
                routeByDriver.put(dbRoute.getNxDdrDriverUserId(), dbRoute);
            }
        }
    }

    /** 不可派司机的沙盘路线（非装车/非执行）不参与今日派单读模型。 */
    private static boolean isOffDutySandboxRouteOnly(NxDisDriverRouteEntity dbRoute,
                                                     Set<Integer> offDutyDriverIds) {
        if (dbRoute == null || offDutyDriverIds == null || offDutyDriverIds.isEmpty()) {
            return false;
        }
        Integer driverId = dbRoute.getNxDdrDriverUserId();
        if (driverId == null || !offDutyDriverIds.contains(driverId)) {
            return false;
        }
        return !DisRouteDriverDutyLockHelper.isRouteLockedForDutyToggle(dbRoute)
                && !DisRouteRouteExecutionHelper.isExecutionRoute(dbRoute);
    }

    /** 按 driverRouteId 补全 execution 字段（plan 合并失败或 planId 为空时仍可读 actualDepartAt）。 */
    private void hydrateDriverRouteExecutionFromDb(Map<Integer, NxDisDriverRouteEntity> routeByDriver) {
        if (routeByDriver == null || routeByDriver.isEmpty()) {
            return;
        }
        for (NxDisDriverRouteEntity route : routeByDriver.values()) {
            if (route == null || route.getNxDdrId() == null) {
                continue;
            }
            NxDisDriverRouteEntity dbRoute = nxDisDriverRouteDao.queryObject(route.getNxDdrId());
            if (dbRoute == null) {
                continue;
            }
            DisRouteRouteExecutionHelper.mergeExecutionFieldsFromDb(route, dbRoute);
        }
    }

    private void resolveDriverRouteIdsFromStops(Map<Integer, NxDisDriverRouteEntity> routeByDriver) {
        for (NxDisDriverRouteEntity route : routeByDriver.values()) {
            if (route == null || route.getNxDdrId() != null) {
                continue;
            }
            if (route.getStops() == null || route.getStops().isEmpty()) {
                continue;
            }
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop == null) {
                    continue;
                }
                if (stop.getNxDrsDriverRouteId() != null) {
                    route.setNxDdrId(stop.getNxDrsDriverRouteId());
                    break;
                }
                NxDisShipmentTaskEntity task = stop.getShipmentTask();
                if (task != null && task.getNxDstDriverRouteId() != null) {
                    route.setNxDdrId(task.getNxDstDriverRouteId());
                    break;
                }
            }
        }
    }

    private void appendStopsToDriverRoutes(Map<Integer, NxDisDriverRouteEntity> routeByDriver,
                                           List<NxDisRouteStopEntity> stops,
                                           Set<Integer> sandboxIneligibleDrivers) {
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null || stop.getShipmentTask() == null) {
                continue;
            }
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            Integer driverId = task.getNxDstAssignedDriverUserId() != null
                    ? task.getNxDstAssignedDriverUserId()
                    : task.getNxDstSuggestedDriverUserId();
            if (driverId == null) {
                continue;
            }
            if (!DisRouteSandboxDispatchEligibilityHelper.isDriverEligibleForSandboxDispatch(
                    driverId, sandboxIneligibleDrivers)) {
                continue;
            }
            NxDisDriverRouteEntity route = routeByDriver.get(driverId);
            if (route == null) {
                route = new NxDisDriverRouteEntity();
                route.setNxDdrDriverUserId(driverId);
                route.setStops(new ArrayList<NxDisRouteStopEntity>());
                routeByDriver.put(driverId, route);
            }
            if (DisRouteRouteExecutionHelper.isExecutionRoute(route)
                    && DisRouteSandboxDispatchEligibilityHelper.isSandboxEphemeralStop(stop)) {
                continue;
            }
            route.getStops().add(stop);
        }
    }

    private void resequenceStops(List<NxDisRouteStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return;
        }
        Collections.sort(stops, new Comparator<NxDisRouteStopEntity>() {
            @Override
            public int compare(NxDisRouteStopEntity a, NxDisRouteStopEntity b) {
                boolean lockedA = a.getShipmentTask() != null && a.getShipmentTask().getNxDstManualLocked() != null
                        && a.getShipmentTask().getNxDstManualLocked() == 1;
                boolean lockedB = b.getShipmentTask() != null && b.getShipmentTask().getNxDstManualLocked() != null
                        && b.getShipmentTask().getNxDstManualLocked() == 1;
                if (lockedA != lockedB) {
                    return lockedA ? -1 : 1;
                }
                int seqA = a.getNxDrsStopSeq() != null ? a.getNxDrsStopSeq() : 999;
                int seqB = b.getNxDrsStopSeq() != null ? b.getNxDrsStopSeq() : 999;
                return Integer.compare(seqA, seqB);
            }
        });
        int seq = 1;
        for (NxDisRouteStopEntity stop : stops) {
            if (stop.getShipmentTask() != null && stop.getShipmentTask().getNxDstManualLocked() != null
                    && stop.getShipmentTask().getNxDstManualLocked() == 1
                    && stop.getShipmentTask().getNxDstManualStopSeq() != null) {
                stop.setNxDrsStopSeq(stop.getShipmentTask().getNxDstManualStopSeq());
            } else {
                stop.setNxDrsStopSeq(seq++);
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

    private String buildOrderVersion(List<DisRouteOrderSnapshotDto> orders) {
        StringBuilder sb = new StringBuilder();
        sb.append(orders != null ? orders.size() : 0);
        if (orders != null) {
            List<Integer> ids = new ArrayList<Integer>();
            for (DisRouteOrderSnapshotDto o : orders) {
                if (o.getOrderId() != null) {
                    ids.add(o.getOrderId());
                }
            }
            Collections.sort(ids);
            for (Integer id : ids) {
                sb.append(':').append(id);
            }
        }
        return Integer.toHexString(sb.toString().hashCode());
    }

    private String buildDutyVersion(Integer disId, String routeDate, List<NxDistributerUserEntity> drivers) {
        StringBuilder sb = new StringBuilder();
        sb.append(disId).append('@').append(routeDate).append('#');
        if (drivers != null) {
            List<Integer> ids = new ArrayList<Integer>();
            for (NxDistributerUserEntity d : drivers) {
                ids.add(d.getNxDistributerUserId());
            }
            Collections.sort(ids);
            for (Integer id : ids) {
                sb.append(id).append(',');
            }
        }
        return Integer.toHexString(sb.toString().hashCode());
    }

    private String buildSandboxVersion(SandboxComputeResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(result.getOrderVersion()).append('|').append(result.getDutyVersion());
        sb.append('|').append(result.getConfirmedStops().size());
        sb.append('|').append(result.getSandboxSuggestedStops().size());
        return Integer.toHexString(sb.toString().hashCode());
    }

    /**
     * Phase 3a：展示用客户名始终读 nx_department 最新值（仅内存读模型，不写库）。
     */
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
