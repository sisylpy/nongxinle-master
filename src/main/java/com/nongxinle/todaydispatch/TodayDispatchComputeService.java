package com.nongxinle.todaydispatch;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dao.NxDisShipmentTaskItemDao;
import com.nongxinle.dto.route.SandboxComputeRequest;
import com.nongxinle.dto.route.SandboxComputeResult;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;
import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.route.DisRouteDispatchBatch;
import com.nongxinle.route.DisRouteSandboxDriverDispatchStateHelper;
import com.nongxinle.route.DisRouteSandboxDisplayFormatHelper;
import com.nongxinle.route.DisRouteSandboxDispatchEligibilityHelper;
import com.nongxinle.route.DisRouteSandboxStopKeyUtils;
import com.nongxinle.route.DisRouteSandboxStopTimeWindowResolver;
import com.nongxinle.route.DisRouteSandboxTodayStopScheduleHelper;
import com.nongxinle.route.DisRouteSandboxTodayTimelineBuilder;
import com.nongxinle.route.DisRouteSandboxUnassignedStopHelper;
import com.nongxinle.route.DisRouteTemporalHelper;
import com.nongxinle.route.DriverRouteEditPrimaryActionMaps;
import com.nongxinle.route.ManualDispatchPrimaryActionMaps;
import com.nongxinle.route.SandboxComputeRequestFactory;
import com.nongxinle.route.SandboxTodayPrimaryActionMaps;
import com.nongxinle.route.dispatch.strategy.DispatchAssignmentPlan;
import com.nongxinle.route.dispatch.strategy.DispatchStrategyContext;
import com.nongxinle.route.dispatch.strategy.DispatchStrategyMode;
import com.nongxinle.route.dispatch.strategy.OwnerFixedRouteTimeWindowRouteSequencer;
import com.nongxinle.route.model.GeoPoint;
import com.nongxinle.route.proposal.ProposalDriverRoute;
import com.nongxinle.route.proposal.SandboxProposalPlan;
import com.nongxinle.route.proposal.SandboxProposalPlanBuilder;
import com.nongxinle.service.DisRouteSandboxComputeService;
import com.nongxinle.service.NxDistributerService;
import com.nongxinle.service.impl.DisRoutePlanPresentationHelper;
import com.nongxinle.service.impl.DisRouteShipmentTaskItemOrderResolver;
import com.nongxinle.service.impl.DisRouteSandboxLegMetricsHelper;
import com.nongxinle.service.impl.DisRouteSandboxSchedulePreviewHelper;
import com.nongxinle.route.DisRouteLoadingGateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.nongxinle.route.DisShipmentTaskStatus.ASSIGNED;
import static com.nongxinle.route.DisShipmentTaskStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.DELIVERED;
import static com.nongxinle.route.DisShipmentTaskStatus.EXCEPTION;
import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;
import static com.nongxinle.route.DisShipmentTaskStatus.READY_TO_GO;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 加载 + 黑盒 compute + finalize + 映射 / enrich DriverRoutePlan。 */
@Service
public class TodayDispatchComputeService {

    @Autowired
    private DisRouteSandboxComputeService disRouteSandboxComputeService;

    @Autowired
    private DriverDispatchStateService driverDispatchStateService;

    @Autowired
    private DisRouteSandboxLegMetricsHelper disRouteSandboxLegMetricsHelper;

    @Autowired
    private DisRouteSandboxSchedulePreviewHelper disRouteSandboxSchedulePreviewHelper;

    @Autowired
    private NxDistributerService nxDistributerService;

    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;

    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;

    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;

    @Autowired
    private NxDisShipmentTaskItemDao nxDisShipmentTaskItemDao;

    @Autowired
    private DisRouteShipmentTaskItemOrderResolver disRouteShipmentTaskItemOrderResolver;

    @Autowired
    private TodayDispatchRouteEditStopPool todayDispatchRouteEditStopPool;

    @Autowired
    private TodayDispatchStopTimeWindowHydrator todayDispatchStopTimeWindowHydrator;

    public TodayDispatchResult compute(Integer disId,
                                       String routeDate,
                                       String batchCode,
                                       Integer operatorUserId) throws Exception {
        String normalizedBatch = normalizeBatch(batchCode);
        SandboxComputeRequest request = SandboxComputeRequestFactory.dispatchTodayPage(
                disId, routeDate, normalizedBatch);

        SandboxComputeResult compute = disRouteSandboxComputeService.compute(request);

        TodayDispatchResult result = initResult(disId, routeDate, batchCode, operatorUserId, compute);
        result.setPageMode(TodayDispatchResult.PAGE_MODE_DISPATCH);
        applyDepot(result, compute, disId);
        applyMapSettings(result);

        Set<Integer> dispatchBlocked = driverDispatchStateService.resolveDispatchBlockedDriverIds(result);
        buildRoutesFromProposalPlan(result, compute, dispatchBlocked);
        result.setAvailableDrivers(driverDispatchStateService.buildAvailableDrivers(result, dispatchBlocked));
        enrichDriverRoutes(result);
        enrichUnassignedStops(result);
        return result;
    }

    /** 装车中：只读已确认 DB 路线（persistedRoutesOnly），复用分派中展示 enrich。 */
    public TodayDispatchResult computeLoading(Integer disId,
                                              String routeDate,
                                              String batchCode,
                                              Integer operatorUserId) throws Exception {
        String normalizedBatch = normalizeBatch(batchCode);
        SandboxComputeRequest request = SandboxComputeRequestFactory.persistedTodayPage(
                disId, routeDate, normalizedBatch);

        SandboxComputeResult compute = disRouteSandboxComputeService.compute(request);

        TodayDispatchResult result = initResult(disId, routeDate, batchCode, operatorUserId, compute);
        result.setPageMode(TodayDispatchResult.PAGE_MODE_LOADING);
        applyDepot(result, compute, disId);
        applyMapSettings(result);

        buildRoutesFromLoadingPlan(result, compute);
        result.setAvailableDrivers(Collections.<Map<String, Object>>emptyList());
        enrichDriverRoutes(result);
        return result;
    }

    /** 配送中：已出发 execution 路线（persistedRoutesOnly），不跑完整 sandbox/today。 */
    public TodayDispatchResult computeDelivery(Integer disId,
                                               String routeDate,
                                               String batchCode,
                                               Integer operatorUserId) throws Exception {
        String normalizedBatch = normalizeBatch(batchCode);
        SandboxComputeRequest request = SandboxComputeRequestFactory.persistedTodayPage(
                disId, routeDate, normalizedBatch);

        SandboxComputeResult compute = disRouteSandboxComputeService.compute(request);

        TodayDispatchResult result = initResult(disId, routeDate, batchCode, operatorUserId, compute);
        result.setPageMode(TodayDispatchResult.PAGE_MODE_DELIVERY);
        applyDepot(result, compute, disId);
        applyMapSettings(result);

        buildRoutesFromDeliveryPlan(result, compute);
        result.setAvailableDrivers(Collections.<Map<String, Object>>emptyList());
        enrichDriverRoutes(result);
        return result;
    }

    private void buildRoutesFromDeliveryPlan(TodayDispatchResult result, SandboxComputeResult compute) {
        TodayDispatchPlanContextHelper.PlanContext planContext = TodayDispatchPlanContextHelper.prepare(compute);
        NxDisRoutePlanEntity mergedPlan = planContext.mergedPlan;
        List<DriverRoutePlan> routes = new ArrayList<DriverRoutePlan>();

        if (mergedPlan != null && mergedPlan.getExecutionDriverRoutes() != null) {
            for (NxDisDriverRouteEntity executionRoute : mergedPlan.getExecutionDriverRoutes()) {
                if (executionRoute == null || executionRoute.getNxDdrDriverUserId() == null) {
                    continue;
                }
                List<NxDisRouteStopEntity> stopEntities = collectRouteStops(executionRoute);
                if (stopEntities.isEmpty()) {
                    continue;
                }
                routes.add(mapLoadingDriverRoute(executionRoute, stopEntities, result, compute));
            }
        }

        if (routes.isEmpty() && planContext.stopPartition != null
                && !planContext.stopPartition.executionStops.isEmpty()) {
            routes.addAll(buildRoutesFromStopsGrouped(
                    planContext.stopPartition.executionStops, mergedPlan, result, compute));
        }

        if (routes.isEmpty()) {
            routes.addAll(buildRoutesFromPersistedDb(result, compute, PersistedRouteMode.DELIVERY));
        }

        result.setSuggestedRoutes(routes);
        result.setUnassignedStops(Collections.<CustomerStopPlan>emptyList());
    }

    private List<DriverRoutePlan> buildRoutesFromPersistedDb(TodayDispatchResult result,
                                                             SandboxComputeResult compute,
                                                             PersistedRouteMode mode) {
        List<DriverRoutePlan> routes = new ArrayList<DriverRoutePlan>();
        Integer planId = resolvePlanId(result, compute);
        if (planId == null) {
            return routes;
        }
        List<NxDisDriverRouteEntity> dbRoutes = nxDisDriverRouteDao.queryByPlanId(planId);
        if (dbRoutes == null || dbRoutes.isEmpty()) {
            return routes;
        }
        for (NxDisDriverRouteEntity dbRoute : dbRoutes) {
            if (dbRoute == null || dbRoute.getNxDdrDriverUserId() == null
                    || !mode.acceptsRoute(dbRoute)) {
                continue;
            }
            List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByDriverRouteId(dbRoute.getNxDdrId());
            List<NxDisShipmentTaskEntity> activeTasks = filterPersistedTasks(tasks, mode, dbRoute);
            if (activeTasks.isEmpty()) {
                continue;
            }
            attachTaskItems(activeTasks);
            List<NxDisRouteStopEntity> stopEntities =
                    DisRoutePlanPresentationHelper.tasksToReadModelStops(activeTasks);
            for (NxDisRouteStopEntity stop : stopEntities) {
                if (stop == null) {
                    continue;
                }
                NxDisShipmentTaskEntity task = stop.getShipmentTask();
                if (task != null) {
                    stop.setShipmentTask(task);
                }
            }
            if (stopEntities.isEmpty()) {
                continue;
            }
            routes.add(mapLoadingDriverRoute(dbRoute, stopEntities, result, compute));
        }
        return routes;
    }

    private enum PersistedRouteMode {
        LOADING {
            @Override
            boolean acceptsRoute(NxDisDriverRouteEntity route) {
                return DisRouteLoadingGateHelper.isRouteEnteredLoading(route);
            }

            @Override
            boolean acceptsTask(NxDisShipmentTaskEntity task, NxDisDriverRouteEntity route) {
                if (task == null || task.getNxDstStatus() == null) {
                    return false;
                }
                String status = task.getNxDstStatus().trim().toUpperCase();
                if (ASSIGNED.equals(status) || READY_TO_GO.equals(status)) {
                    return true;
                }
                // 历史 IN_DELIVERY 误标在尚未出发的装车路线：仍应在装车页展示。
                if ((IN_DELIVERY.equals(status) || EXCEPTION.equals(status))
                        && route != null
                        && route.getNxDdrActualDepartAt() == null
                        && DisRouteLoadingGateHelper.isRouteEnteredLoading(route)) {
                    return true;
                }
                return false;
            }
        },
        DELIVERY {
            @Override
            boolean acceptsRoute(NxDisDriverRouteEntity route) {
                return route.getNxDdrActualDepartAt() != null;
            }

            @Override
            boolean acceptsTask(NxDisShipmentTaskEntity task, NxDisDriverRouteEntity route) {
                String status = task.getNxDstStatus();
                if (status == null) {
                    return false;
                }
                status = status.trim().toUpperCase();
                return IN_DELIVERY.equals(status) || DELIVERED.equals(status) || EXCEPTION.equals(status)
                        || ASSIGNED.equals(status) || READY_TO_GO.equals(status);
            }
        };

        abstract boolean acceptsRoute(NxDisDriverRouteEntity route);

        abstract boolean acceptsTask(NxDisShipmentTaskEntity task, NxDisDriverRouteEntity route);
    }

    private static List<NxDisShipmentTaskEntity> filterPersistedTasks(
            List<NxDisShipmentTaskEntity> tasks,
            PersistedRouteMode mode,
            NxDisDriverRouteEntity route) {
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }
        List<NxDisShipmentTaskEntity> active = new ArrayList<NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task == null || CANCELLED.equals(task.getNxDstStatus())) {
                continue;
            }
            if (mode.acceptsTask(task, route)) {
                active.add(task);
            }
        }
        return active;
    }

    private static TodayDispatchResult initResult(Integer disId,
                                                  String routeDate,
                                                  String batchCode,
                                                  Integer operatorUserId,
                                                  SandboxComputeResult compute) {
        TodayDispatchResult result = new TodayDispatchResult();
        result.setDisId(disId);
        result.setRouteDate(compute.getRouteDate() != null ? compute.getRouteDate() : routeDate);
        result.setBatchCode(compute.getDispatchBatch() != null ? compute.getDispatchBatch() : normalizeBatch(batchCode));
        result.setOperatorUserId(operatorUserId);
        result.setServerNow(new Date());
        result.setCompute(compute);
        return result;
    }

    private void buildRoutesFromLoadingPlan(TodayDispatchResult result, SandboxComputeResult compute) {
        List<DriverRoutePlan> routes = buildRoutesFromPersistedDb(result, compute, PersistedRouteMode.LOADING);
        if (!routes.isEmpty()) {
            result.setSuggestedRoutes(routes);
            result.setUnassignedStops(Collections.<CustomerStopPlan>emptyList());
            return;
        }

        TodayDispatchPlanContextHelper.PlanContext planContext = TodayDispatchPlanContextHelper.prepare(compute);
        NxDisRoutePlanEntity mergedPlan = planContext.mergedPlan;
        List<DriverRoutePlan> fallbackRoutes = new ArrayList<DriverRoutePlan>();

        if (mergedPlan != null && mergedPlan.getLoadingDriverRoutes() != null) {
            for (NxDisDriverRouteEntity loadingRoute : mergedPlan.getLoadingDriverRoutes()) {
                if (loadingRoute == null || loadingRoute.getNxDdrDriverUserId() == null) {
                    continue;
                }
                List<NxDisRouteStopEntity> stopEntities = collectRouteStops(loadingRoute);
                if (stopEntities.isEmpty()) {
                    continue;
                }
                fallbackRoutes.add(mapLoadingDriverRoute(loadingRoute, stopEntities, result, compute));
            }
        }

        if (fallbackRoutes.isEmpty() && planContext.stopPartition != null
                && !planContext.stopPartition.loadingStops.isEmpty()) {
            fallbackRoutes.addAll(buildRoutesFromStopsGrouped(
                    planContext.stopPartition.loadingStops, mergedPlan, result, compute));
        }

        if (fallbackRoutes.isEmpty()) {
            fallbackRoutes.addAll(buildRoutesFromPersistedDb(result, compute, PersistedRouteMode.LOADING));
        }

        result.setSuggestedRoutes(fallbackRoutes);
        result.setUnassignedStops(Collections.<CustomerStopPlan>emptyList());
    }

    private Integer resolvePlanId(TodayDispatchResult result, SandboxComputeResult compute) {
        if (compute != null && compute.getPlanContext() != null
                && compute.getPlanContext().getNxDrpId() != null) {
            return compute.getPlanContext().getNxDrpId();
        }
        if (compute != null && compute.getMergedPlan() != null
                && compute.getMergedPlan().getNxDrpId() != null) {
            return compute.getMergedPlan().getNxDrpId();
        }
        if (result == null || result.getDisId() == null) {
            return null;
        }
        String routeDate = result.getRouteDate();
        String batchCode = normalizeBatch(result.getBatchCode());
        NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                result.getDisId(), routeDate, batchCode,
                com.nongxinle.route.DisRoutePlanStatus.ASSIGNED);
        if (plan == null) {
            plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                    result.getDisId(), routeDate, batchCode,
                    com.nongxinle.route.DisRoutePlanStatus.READY);
        }
        return plan != null ? plan.getNxDrpId() : null;
    }

    private void attachTaskItems(List<NxDisShipmentTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        List<Integer> taskIds = new ArrayList<Integer>();
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task != null && task.getNxDstId() != null) {
                taskIds.add(task.getNxDstId());
            }
        }
        if (taskIds.isEmpty()) {
            return;
        }
        List<NxDisShipmentTaskItemEntity> allItems = nxDisShipmentTaskItemDao.queryByTaskIds(taskIds);
        disRouteShipmentTaskItemOrderResolver.enrichItems(allItems);
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task == null || task.getNxDstId() == null) {
                continue;
            }
            List<NxDisShipmentTaskItemEntity> items = new ArrayList<NxDisShipmentTaskItemEntity>();
            if (allItems != null) {
                for (NxDisShipmentTaskItemEntity item : allItems) {
                    if (item != null && task.getNxDstId().equals(item.getNxDstiTaskId())) {
                        items.add(item);
                    }
                }
            }
            task.setItems(items);
        }
    }

    private List<DriverRoutePlan> buildRoutesFromStopsGrouped(List<NxDisRouteStopEntity> stops,
                                                              NxDisRoutePlanEntity mergedPlan,
                                                              TodayDispatchResult result,
                                                              SandboxComputeResult compute) {
        List<DriverRoutePlan> routes = new ArrayList<DriverRoutePlan>();
        if (stops == null || stops.isEmpty()) {
            return routes;
        }
        Map<Integer, List<NxDisRouteStopEntity>> stopsByDriver = new LinkedHashMap<Integer, List<NxDisRouteStopEntity>>();
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Integer driverId = DisRouteSandboxDriverDispatchStateHelper.resolveStopDriverUserId(stop);
            if (driverId == null) {
                continue;
            }
            List<NxDisRouteStopEntity> bucket = stopsByDriver.get(driverId);
            if (bucket == null) {
                bucket = new ArrayList<NxDisRouteStopEntity>();
                stopsByDriver.put(driverId, bucket);
            }
            bucket.add(stop);
        }
        for (Map.Entry<Integer, List<NxDisRouteStopEntity>> entry : stopsByDriver.entrySet()) {
            Integer driverId = entry.getKey();
            List<NxDisRouteStopEntity> driverStops = entry.getValue();
            if (driverStops == null || driverStops.isEmpty()) {
                continue;
            }
            NxDisDriverRouteEntity planRoute = findPlanRouteForDriver(mergedPlan, driverId);
            if (planRoute == null) {
                planRoute = new NxDisDriverRouteEntity();
                planRoute.setNxDdrDriverUserId(driverId);
                planRoute.setStops(new ArrayList<NxDisRouteStopEntity>(driverStops));
            }
            routes.add(mapLoadingDriverRoute(planRoute, new ArrayList<NxDisRouteStopEntity>(driverStops),
                    result, compute));
        }
        return routes;
    }

    private static NxDisDriverRouteEntity findPlanRouteForDriver(NxDisRoutePlanEntity plan,
                                                                 Integer driverUserId) {
        if (plan == null || driverUserId == null) {
            return null;
        }
        NxDisDriverRouteEntity route = findRouteInList(plan.getLoadingDriverRoutes(), driverUserId);
        if (route != null) {
            return route;
        }
        route = findRouteInList(plan.getDriverRoutes(), driverUserId);
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

    private static List<NxDisRouteStopEntity> collectRouteStops(NxDisDriverRouteEntity route) {
        List<NxDisRouteStopEntity> stops = new ArrayList<NxDisRouteStopEntity>();
        if (route == null || route.getStops() == null) {
            return stops;
        }
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop != null) {
                stops.add(stop);
            }
        }
        return stops;
    }

    private DriverRoutePlan mapLoadingDriverRoute(NxDisDriverRouteEntity loadingRoute,
                                                  List<NxDisRouteStopEntity> stopEntities,
                                                  TodayDispatchResult result,
                                                  SandboxComputeResult compute) {
        DriverRoutePlan route = new DriverRoutePlan();
        route.setDriverUserId(loadingRoute.getNxDdrDriverUserId());
        route.setDriverName(resolveLoadingDriverName(loadingRoute, compute));
        if (loadingRoute.getReturnLegDistanceM() != null) {
            route.setReturnLegDistanceM(loadingRoute.getReturnLegDistanceM());
            route.setReturnLegDurationS(loadingRoute.getReturnLegDurationS());
        }
        if (loadingRoute.getNxDdrPlannedDepartAt() != null) {
            route.setPlannedDepartAt(loadingRoute.getNxDdrPlannedDepartAt());
        }

        List<NxDisRouteStopEntity> working = new ArrayList<NxDisRouteStopEntity>(stopEntities);
        boolean preserveManualOrder = shouldPreserveManualRouteOrder(working);
        if (!preserveManualOrder) {
            sortStopsBySeq(working);
        }
        if (preserveManualOrder) {
            applyRouteEditSchedulePipeline(working, route, result, compute);
        } else {
            applyVisibleSchedulePipeline(working, route, result, compute);
        }
        if (route.getPlannedDepartAt() == null && loadingRoute.getNxDdrPlannedDepartAt() != null) {
            route.setPlannedDepartAt(loadingRoute.getNxDdrPlannedDepartAt());
        }
        if (route.getReturnLegDistanceM() == null && loadingRoute.getReturnLegDistanceM() != null) {
            route.setReturnLegDistanceM(loadingRoute.getReturnLegDistanceM());
            route.setReturnLegDurationS(loadingRoute.getReturnLegDurationS());
        }

        List<CustomerStopPlan> stops = new ArrayList<CustomerStopPlan>();
        int seq = 1;
        for (NxDisRouteStopEntity stop : working) {
            stops.add(buildStopPlan(stop, seq++, result, true));
        }
        route.setStops(stops);
        return route;
    }

    private static String resolveLoadingDriverName(NxDisDriverRouteEntity loadingRoute,
                                                   SandboxComputeResult compute) {
        if (loadingRoute == null) {
            return null;
        }
        if (loadingRoute.getDriverName() != null && !loadingRoute.getDriverName().trim().isEmpty()) {
            return loadingRoute.getDriverName().trim();
        }
        NxDisRouteStopEntity firstStop = loadingRoute.getStops() != null && !loadingRoute.getStops().isEmpty()
                ? loadingRoute.getStops().get(0) : null;
        return resolveFallbackDriverName(loadingRoute.getNxDdrDriverUserId(), firstStop, compute);
    }

    /**
     * 与旧路径一致：以 compute 产出的 {@link SandboxProposalPlan} 为路线主权，
     * 不再二次 demote sandboxSuggestedStops（compute 已 sanitize + reassign）。
     */
    private void buildRoutesFromProposalPlan(TodayDispatchResult result,
                                             SandboxComputeResult compute,
                                             Set<Integer> dispatchBlocked) {
        List<DriverRoutePlan> routes = new ArrayList<DriverRoutePlan>();
        List<CustomerStopPlan> unassigned = new ArrayList<CustomerStopPlan>();
        Set<Integer> assignedDepIds = new HashSet<Integer>();

        SandboxProposalPlan proposalPlan = compute.getProposalPlan();
        if (proposalPlan != null && proposalPlan.getProposalRoutes() != null) {
            for (ProposalDriverRoute proposalRoute : proposalPlan.getProposalRoutes()) {
                if (proposalRoute == null || proposalRoute.getDriverUserId() == null) {
                    continue;
                }
                List<NxDisRouteStopEntity> stopEntities =
                        SandboxProposalPlanBuilder.toStopEntities(proposalRoute.getStops());
                if (stopEntities.isEmpty()) {
                    continue;
                }
                Integer driverUserId = proposalRoute.getDriverUserId();
                if (!DisRouteSandboxDispatchEligibilityHelper.isDriverEligibleForSandboxDispatch(
                        driverUserId, dispatchBlocked)) {
                    appendStopsAsUnassigned(unassigned, stopEntities, result, assignedDepIds);
                    continue;
                }
                routes.add(mapProposalDriverRoute(proposalRoute, stopEntities, result, compute, assignedDepIds));
            }
        }

        if (routes.isEmpty()) {
            routes.addAll(mapSuggestedRoutesFallback(compute, result, dispatchBlocked, assignedDepIds));
        }

        appendProposalUnassigned(unassigned, proposalPlan, compute, result, assignedDepIds);
        result.setSuggestedRoutes(routes);
        result.setUnassignedStops(unassigned);
    }

    private DriverRoutePlan mapProposalDriverRoute(ProposalDriverRoute proposalRoute,
                                                   List<NxDisRouteStopEntity> stopEntities,
                                                   TodayDispatchResult result,
                                                   SandboxComputeResult compute,
                                                   Set<Integer> assignedDepIds) {
        DriverRoutePlan route = new DriverRoutePlan();
        route.setDriverUserId(proposalRoute.getDriverUserId());
        route.setDriverName(proposalRoute.getDriverName());
        route.setScheduleHeadline(proposalRoute.getScheduleHeadline());

        List<NxDisRouteStopEntity> working = new ArrayList<NxDisRouteStopEntity>(stopEntities);
        applyVisibleSchedulePipeline(working, route, result, compute);

        List<CustomerStopPlan> stops = new ArrayList<CustomerStopPlan>();
        int seq = 1;
        for (NxDisRouteStopEntity stop : working) {
            if (stop == null) {
                continue;
            }
            CustomerStopPlan plan = buildStopPlan(stop, seq++, result, true);
            stops.add(plan);
            if (plan.getDepFatherId() != null) {
                assignedDepIds.add(plan.getDepFatherId());
            }
        }
        route.setStops(stops);
        return route;
    }

    /** todaydispatch 可见排程：重排 + 重算 ETA/窗口状态（OwnerFixedRouteTimeWindowRouteSequencer + leg metrics）。 */
    private void applyVisibleSchedulePipeline(List<NxDisRouteStopEntity> working,
                                              DriverRoutePlan route,
                                              TodayDispatchResult result,
                                              SandboxComputeResult compute) {
        if (working == null || working.isEmpty()) {
            return;
        }
        DispatchAssignmentPlan assignmentPlan = compute.getDispatchAssignmentPlan();
        DispatchStrategyContext context = buildSequencingContext(result);
        boolean ownerFixed = assignmentPlan != null
                && assignmentPlan.getStrategyMode() == DispatchStrategyMode.OWNER_FIXED_ROUTE;
        if (ownerFixed) {
            OwnerFixedRouteTimeWindowRouteSequencer.resequenceSuggestedStops(
                    working, context, assignmentPlan);
        } else {
            sortStopsBySeq(working);
        }
        OwnerFixedRouteTimeWindowRouteSequencer.invalidateLegMetrics(working);

        NxDisDriverRouteEntity pseudoRoute = new NxDisDriverRouteEntity();
        pseudoRoute.setNxDdrDriverUserId(route.getDriverUserId());

        GeoPoint depot = context.getDepot();
        if (depot != null && disRouteSandboxLegMetricsHelper != null) {
            NxDisDriverRouteEntity legRoute = new NxDisDriverRouteEntity();
            legRoute.setNxDdrDriverUserId(route.getDriverUserId());
            legRoute.setStops(working);
            try {
                disRouteSandboxLegMetricsHelper.applyToDriverRoute(depot, legRoute);
                copyReturnLeg(pseudoRoute, legRoute);
                route.setReturnLegDistanceM(pseudoRoute.getReturnLegDistanceM());
                route.setReturnLegDurationS(pseudoRoute.getReturnLegDurationS());
            } catch (IOException ignored) {
                // enrichRouteCard.applyReturnLeg 直线兜底
            }
        }

        if (assignmentPlan != null) {
            OwnerFixedRouteTimeWindowRouteSequencer.recalculateVisibleScheduleForDriverRoute(
                    pseudoRoute, working, assignmentPlan, context);
        } else if (disRouteSandboxSchedulePreviewHelper != null) {
            NxDisDriverRouteEntity scheduleRoute = new NxDisDriverRouteEntity();
            scheduleRoute.setNxDdrDriverUserId(route.getDriverUserId());
            scheduleRoute.setStops(working);
            scheduleRoute.setReturnLegDistanceM(route.getReturnLegDistanceM());
            scheduleRoute.setReturnLegDurationS(route.getReturnLegDurationS());
            disRouteSandboxSchedulePreviewHelper.applySchedulePreview(
                    null, scheduleRoute, result.getRouteDate());
            route.setReturnLegDistanceM(scheduleRoute.getReturnLegDistanceM());
            route.setReturnLegDurationS(scheduleRoute.getReturnLegDurationS());
            pseudoRoute.setNxDdrPlannedDepartAt(scheduleRoute.getNxDdrPlannedDepartAt());
        }
        route.setPlannedDepartAt(pseudoRoute.getNxDdrPlannedDepartAt());
    }

    /** 路线编辑：保持 stopKeys 顺序，仅重算 leg/ETA（不做策略重排）。 */
    private void applyRouteEditSchedulePipeline(List<NxDisRouteStopEntity> working,
                                                DriverRoutePlan route,
                                                TodayDispatchResult result,
                                                SandboxComputeResult compute) {
        if (working == null || working.isEmpty()) {
            return;
        }
        assignStopSeqInWorkingOrder(working);
        DispatchAssignmentPlan assignmentPlan = compute != null ? compute.getDispatchAssignmentPlan() : null;
        DispatchStrategyContext context = buildSequencingContext(result);
        OwnerFixedRouteTimeWindowRouteSequencer.invalidateLegMetrics(working);

        NxDisDriverRouteEntity pseudoRoute = new NxDisDriverRouteEntity();
        pseudoRoute.setNxDdrDriverUserId(route.getDriverUserId());

        GeoPoint depot = context.getDepot();
        if (depot != null && disRouteSandboxLegMetricsHelper != null) {
            NxDisDriverRouteEntity legRoute = new NxDisDriverRouteEntity();
            legRoute.setNxDdrDriverUserId(route.getDriverUserId());
            legRoute.setStops(working);
            try {
                disRouteSandboxLegMetricsHelper.applyToDriverRoute(depot, legRoute);
                copyReturnLeg(pseudoRoute, legRoute);
                route.setReturnLegDistanceM(pseudoRoute.getReturnLegDistanceM());
                route.setReturnLegDurationS(pseudoRoute.getReturnLegDurationS());
            } catch (IOException ignored) {
                // enrichRouteCard.applyReturnLeg 直线兜底
            }
        }

        if (assignmentPlan != null) {
            OwnerFixedRouteTimeWindowRouteSequencer.recalculateVisibleScheduleForDriverRoute(
                    pseudoRoute, working, assignmentPlan, context);
        } else if (disRouteSandboxSchedulePreviewHelper != null) {
            NxDisDriverRouteEntity scheduleRoute = new NxDisDriverRouteEntity();
            scheduleRoute.setNxDdrDriverUserId(route.getDriverUserId());
            scheduleRoute.setStops(working);
            scheduleRoute.setReturnLegDistanceM(route.getReturnLegDistanceM());
            scheduleRoute.setReturnLegDurationS(route.getReturnLegDurationS());
            disRouteSandboxSchedulePreviewHelper.applySchedulePreview(
                    null, scheduleRoute, result.getRouteDate());
            route.setReturnLegDistanceM(scheduleRoute.getReturnLegDistanceM());
            route.setReturnLegDurationS(scheduleRoute.getReturnLegDurationS());
            pseudoRoute.setNxDdrPlannedDepartAt(scheduleRoute.getNxDdrPlannedDepartAt());
        }
        route.setPlannedDepartAt(pseudoRoute.getNxDdrPlannedDepartAt());
    }

    private NxDisRouteStopEntity findBaselineEntityByStopKey(TodayDispatchResult context,
                                                             Integer driverUserId,
                                                             String stopKey) {
        if (todayDispatchRouteEditStopPool == null || context == null || stopKey == null) {
            return null;
        }
        Integer depId = DisRouteSandboxStopKeyUtils.parseDepFatherId(stopKey);
        if (depId == null) {
            return null;
        }
        for (NxDisRouteStopEntity stop : todayDispatchRouteEditStopPool.collectDriverBaselineStops(
                context, driverUserId)) {
            if (stop == null) {
                continue;
            }
            Integer stopDepId = stop.getNxDrsDepartmentId();
            if (stopDepId == null && stop.getShipmentTask() != null) {
                stopDepId = stop.getShipmentTask().getNxDstDepFatherId();
            }
            if (depId.equals(stopDepId)) {
                return cloneStopEntity(stop);
            }
        }
        return null;
    }

    private static void stampStopForRouteEdit(NxDisRouteStopEntity entity, Integer driverUserId) {
        if (entity == null || driverUserId == null) {
            return;
        }
        if (entity.getSuggestedDriverUserId() == null) {
            entity.setSuggestedDriverUserId(driverUserId);
        }
    }

    /** 按当前 working 列表顺序写 stopSeq / manualStopSeq，供试算与装车页保序。 */
    private static void assignStopSeqInWorkingOrder(List<NxDisRouteStopEntity> working) {
        if (working == null) {
            return;
        }
        int seq = 1;
        for (NxDisRouteStopEntity stop : working) {
            if (stop == null) {
                continue;
            }
            stop.setNxDrsStopSeq(seq);
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task != null) {
                task.setNxDstManualStopSeq(seq);
                task.setNxDstRouteSeq(seq);
                task.setNxDstManualLocked(1);
            }
            seq++;
        }
    }

    private static boolean shouldPreserveManualRouteOrder(List<NxDisRouteStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return false;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task != null && task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1) {
                return true;
            }
        }
        return false;
    }

    private static void copyReturnLeg(NxDisDriverRouteEntity target, NxDisDriverRouteEntity source) {
        if (target == null || source == null) {
            return;
        }
        target.setReturnLegDistanceM(source.getReturnLegDistanceM());
        target.setReturnLegDurationS(source.getReturnLegDurationS());
        target.setReturnLegDistanceType(source.getReturnLegDistanceType());
        target.setReturnLegLabel(source.getReturnLegLabel());
    }

    private static DispatchStrategyContext buildSequencingContext(TodayDispatchResult result) {
        GeoPoint depot = null;
        if (result.getDepotLat() != null && result.getDepotLng() != null) {
            depot = new GeoPoint();
            depot.setLat(String.valueOf(result.getDepotLat()));
            depot.setLng(String.valueOf(result.getDepotLng()));
        }
        return DispatchStrategyContext.builder()
                .disId(result.getDisId())
                .routeDate(result.getRouteDate())
                .batchCode(result.getBatchCode())
                .depot(depot)
                .serverNow(result.getServerNow() != null ? result.getServerNow() : new Date())
                .build();
    }

    private static void sortStopsBySeq(List<NxDisRouteStopEntity> stops) {
        if (stops == null || stops.size() <= 1) {
            return;
        }
        Collections.sort(stops, new Comparator<NxDisRouteStopEntity>() {
            @Override
            public int compare(NxDisRouteStopEntity a, NxDisRouteStopEntity b) {
                return Integer.compare(resolveEntityStopSeq(a), resolveEntityStopSeq(b));
            }
        });
    }

    private static int resolveEntityStopSeq(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return Integer.MAX_VALUE;
        }
        if (stop.getNxDrsStopSeq() != null && stop.getNxDrsStopSeq() > 0) {
            return stop.getNxDrsStopSeq();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1
                && task.getNxDstManualStopSeq() != null && task.getNxDstManualStopSeq() > 0) {
            return task.getNxDstManualStopSeq();
        }
        return Integer.MAX_VALUE;
    }

    private static void appendStopsAsUnassigned(List<CustomerStopPlan> unassigned,
                                                List<NxDisRouteStopEntity> stopEntities,
                                                TodayDispatchResult result,
                                                Set<Integer> assignedDepIds) {
        int seq = unassigned.size() + 1;
        for (NxDisRouteStopEntity stop : stopEntities) {
            if (stop == null) {
                continue;
            }
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            if (depId != null && assignedDepIds.contains(depId)) {
                continue;
            }
            CustomerStopPlan plan = buildStopPlanStatic(stop, seq++, result, false);
            unassigned.add(plan);
            if (depId != null) {
                assignedDepIds.add(depId);
            }
        }
    }

    private void appendProposalUnassigned(List<CustomerStopPlan> unassigned,
                                          SandboxProposalPlan proposalPlan,
                                          SandboxComputeResult compute,
                                          TodayDispatchResult result,
                                          Set<Integer> assignedDepIds) {
        List<NxDisRouteStopEntity> rawUnassigned;
        if (proposalPlan != null && proposalPlan.getUnassignedStops() != null) {
            rawUnassigned = SandboxProposalPlanBuilder.toStopEntities(proposalPlan.getUnassignedStops());
        } else if (compute.getUnassignedStops() != null) {
            rawUnassigned = compute.getUnassignedStops();
        } else {
            rawUnassigned = Collections.emptyList();
        }
        int seq = unassigned.size() + 1;
        for (NxDisRouteStopEntity stop : rawUnassigned) {
            if (stop == null) {
                continue;
            }
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            if (depId != null && assignedDepIds.contains(depId)) {
                continue;
            }
            unassigned.add(buildStopPlan(stop, seq++, result, false));
            if (depId != null) {
                assignedDepIds.add(depId);
            }
        }
    }

    private List<DriverRoutePlan> mapSuggestedRoutesFallback(SandboxComputeResult compute,
                                                             TodayDispatchResult result,
                                                             Set<Integer> dispatchBlocked,
                                                             Set<Integer> assignedDepIds) {
        List<DriverRoutePlan> routes = new ArrayList<DriverRoutePlan>();
        Map<Integer, List<NxDisRouteStopEntity>> stopsByDriver = new LinkedHashMap<Integer, List<NxDisRouteStopEntity>>();
        List<NxDisRouteStopEntity> suggested = compute.getSandboxSuggestedStops();
        if (suggested == null) {
            return routes;
        }
        for (NxDisRouteStopEntity stop : suggested) {
            if (stop == null) {
                continue;
            }
            Integer driverId = DisRouteSandboxDriverDispatchStateHelper.resolveStopDriverUserId(stop);
            if (driverId == null
                    || !DisRouteSandboxDispatchEligibilityHelper.isDriverEligibleForSandboxDispatch(
                    driverId, dispatchBlocked)) {
                continue;
            }
            List<NxDisRouteStopEntity> bucket = stopsByDriver.get(driverId);
            if (bucket == null) {
                bucket = new ArrayList<NxDisRouteStopEntity>();
                stopsByDriver.put(driverId, bucket);
            }
            bucket.add(stop);
        }
        for (Map.Entry<Integer, List<NxDisRouteStopEntity>> entry : stopsByDriver.entrySet()) {
            Integer driverId = entry.getKey();
            List<NxDisRouteStopEntity> working = new ArrayList<NxDisRouteStopEntity>(entry.getValue());
            DriverRoutePlan route = new DriverRoutePlan();
            route.setDriverUserId(driverId);
            route.setDriverName(resolveFallbackDriverName(driverId,
                    working.isEmpty() ? null : working.get(0), compute));
            applyVisibleSchedulePipeline(working, route, result, compute);
            List<CustomerStopPlan> stops = new ArrayList<CustomerStopPlan>();
            int seq = 1;
            for (NxDisRouteStopEntity stop : working) {
                CustomerStopPlan plan = buildStopPlan(stop, seq++, result, true);
                stops.add(plan);
                if (plan.getDepFatherId() != null) {
                    assignedDepIds.add(plan.getDepFatherId());
                }
            }
            route.setStops(stops);
            routes.add(route);
        }
        return routes;
    }

    private static String resolveFallbackDriverName(Integer driverId,
                                                    NxDisRouteStopEntity stop,
                                                    SandboxComputeResult compute) {
        if (stop != null && stop.getSuggestedDriverName() != null
                && !stop.getSuggestedDriverName().trim().isEmpty()) {
            return stop.getSuggestedDriverName().trim();
        }
        if (compute != null && compute.getOnDutyDrivers() != null) {
            for (com.nongxinle.entity.NxDistributerUserEntity driver : compute.getOnDutyDrivers()) {
                if (driver != null && driverId.equals(driver.getNxDistributerUserId())) {
                    if (driver.getNxDiuWxNickName() != null && !driver.getNxDiuWxNickName().trim().isEmpty()) {
                        return driver.getNxDiuWxNickName().trim();
                    }
                }
            }
        }
        return String.valueOf(driverId);
    }

    private static CustomerStopPlan buildStopPlanStatic(NxDisRouteStopEntity stop,
                                                      int sequence,
                                                      TodayDispatchResult result,
                                                      boolean suggested) {
        CustomerStopPlan plan = new CustomerStopPlan();
        plan.setSourceStop(stop);
        plan.setSequence(sequence);
        enrichStopPlan(plan, stop, result, suggested);
        return plan;
    }

    private void enrichUnassignedStops(TodayDispatchResult result) {
        if (result.getUnassignedStops() == null || result.getUnassignedStops().isEmpty()) {
            return;
        }
        List<NxDisRouteStopEntity> raw = new ArrayList<NxDisRouteStopEntity>();
        for (CustomerStopPlan plan : result.getUnassignedStops()) {
            if (plan != null && plan.getSourceStop() != null) {
                raw.add(plan.getSourceStop());
            }
        }
        List<NxDisRouteStopEntity> consolidated =
                DisRouteSandboxUnassignedStopHelper.consolidateByDepartment(raw);
        List<CustomerStopPlan> enriched = new ArrayList<CustomerStopPlan>();
        int seq = 1;
        for (NxDisRouteStopEntity stop : consolidated) {
            if (stop == null) {
                continue;
            }
            CustomerStopPlan plan = buildStopPlan(stop, seq++, result, false);
            plan.setUnassignedDriverHint(buildUnassignedDriverHint(result));
            plan.setPrimaryAction(buildUnassignedPrimaryAction(stop, plan, result));
            enriched.add(plan);
        }
        result.setUnassignedStops(enriched);
    }

    private void enrichDriverRoutes(TodayDispatchResult result) {
        if (result.getSuggestedRoutes() == null) {
            return;
        }
        for (DriverRoutePlan route : result.getSuggestedRoutes()) {
            if (route == null) {
                continue;
            }
            if (route.getStops() != null) {
                for (CustomerStopPlan stop : route.getStops()) {
                    if (stop == null) {
                        continue;
                    }
                    enrichStopPlan(stop, stop.getSourceStop(), result, true);
                    stop.setPrimaryAction(buildSuggestedStopPrimaryAction(stop, result));
                }
            }
            enrichRouteCard(route, result);
        }
    }

    private static void enrichRouteCard(DriverRoutePlan route, TodayDispatchResult result) {
        boolean loadingPage = result != null
                && TodayDispatchResult.PAGE_MODE_LOADING.equals(result.getPageMode());
        route.setBadgeLabel(loadingPage ? "装车中" : "建议路线");
        int stopCount = route.getStops() != null ? route.getStops().size() : 0;
        Date serverNow = result.getServerNow() != null ? result.getServerNow() : new Date();
        String routeDate = result.getRouteDate();

        long outboundDistanceM = 0L;
        long outboundDurationS = 0L;
        long serviceDurationS = 0L;
        if (route.getStops() != null) {
            for (CustomerStopPlan stop : route.getStops()) {
                if (stop == null) {
                    continue;
                }
                if (stop.getLegDistanceM() != null) {
                    outboundDistanceM += stop.getLegDistanceM();
                }
                if (stop.getLegDurationS() != null) {
                    outboundDurationS += stop.getLegDurationS();
                }
                if (stop.getServiceMinutes() != null) {
                    serviceDurationS += stop.getServiceMinutes() * 60L;
                }
            }
        }
        route.setOutboundDistanceM(outboundDistanceM);
        route.setOutboundDurationS(outboundDurationS);
        route.setOutboundDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(outboundDistanceM));

        applyReturnLeg(route, result);

        long returnDistanceM = route.getReturnLegDistanceM() != null ? route.getReturnLegDistanceM() : 0L;
        long returnDurationS = route.getReturnLegDurationS() != null ? route.getReturnLegDurationS() : 0L;
        long totalDistanceM = outboundDistanceM + returnDistanceM;
        long legDurationTotalS = outboundDurationS + serviceDurationS + returnDurationS;

        Date plannedReturnAt = resolvePlannedReturnAt(route);
        route.setPlannedReturnAt(plannedReturnAt);

        Date routeSuggestedDepartAt = resolveRouteSuggestedDepartAt(route, result, plannedReturnAt);
        route.setPlannedDepartAt(routeSuggestedDepartAt);

        String departTimeLabel = formatDepartTimeLabel(routeSuggestedDepartAt, serverNow, routeDate);
        String returnTimeLabel = DisRouteTemporalHelper.formatRouteTimeLabel(
                plannedReturnAt, serverNow, routeDate);

        route.setPlannedDepartLabel(departTimeLabel);
        route.setPlannedReturnLabel(returnTimeLabel);
        route.setRecommendedDepartLabel(buildRouteSuggestedDepartLabel(departTimeLabel));
        route.setRouteSuggestedDepartLabel(route.getRecommendedDepartLabel());

        Long durationFromTimes = calcRoundTripDurationS(routeSuggestedDepartAt, plannedReturnAt);
        long totalDurationS = durationFromTimes != null ? durationFromTimes : legDurationTotalS;

        route.setTotalDistanceM(totalDistanceM);
        route.setTotalDurationS(totalDurationS);
        route.setTotalDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(totalDistanceM));
        route.setTotalDurationText(formatCompactDuration(totalDurationS));
        route.setTotalRoundTripDistanceText(route.getTotalDistanceText());
        route.setTotalRoundTripDurationText(route.getTotalDurationText());

        route.setRouteStatsLine(buildRouteStatsLine(
                stopCount,
                route.getTotalRoundTripDistanceText(),
                route.getPlannedDepartLabel(),
                route.getPlannedReturnLabel(),
                route.getTotalRoundTripDurationText()));
        route.setRouteSummary(route.getRouteStatsLine());
        route.setRouteRoundTripSummaryLine(buildRouteRoundTripSummaryLine(
                route.getTotalRoundTripDistanceText(), route.getTotalRoundTripDurationText()));

        if (route.getStops() != null && !route.getStops().isEmpty()) {
            CustomerStopPlan first = route.getStops().get(0);
            route.setFirstStopWindowLabel(first.getCustomerWindowLabel());
            Date firstArrivalAt = resolveFirstStopPlannedArrivalAt(
                    first.getSourceStop(), routeSuggestedDepartAt);
            String firstArrivalLabel = DisRouteTemporalHelper.formatRouteTimeLabel(
                    firstArrivalAt, serverNow, routeDate);
            if (isBlank(firstArrivalLabel)) {
                firstArrivalLabel = first.getPlannedArrivalLabel();
            }
            route.setFirstStopPlannedArrivalTimeLabel(firstArrivalLabel);
            route.setFirstStopPlannedArrivalLabel(buildFirstStopPlannedArrivalLabel(firstArrivalLabel));
            route.setFirstStopArrivalStatusLabel(
                    resolveArrivalWindowStatusLabel(
                            first.getEarliestDeliveryTimeS(),
                            first.getLatestDeliveryTimeS(),
                            firstArrivalAt,
                            routeDate));
            route.setFirstStopArrivalStatusTone(
                    resolveArrivalWindowStatusTone(
                            first.getEarliestDeliveryTimeS(),
                            first.getLatestDeliveryTimeS(),
                            firstArrivalAt,
                            routeDate));
            route.setFirstStopWindowStartS(first.getEarliestDeliveryTimeS());
            route.setFirstStopWindowEndS(first.getLatestDeliveryTimeS());
            route.setRouteHeadlineLine(buildRouteHeadlineLine(
                    route.getFirstStopPlannedArrivalLabel(), route.getPlannedReturnLabel()));
        }

        route.setPrimaryAction(null);
        route.setRouteEditAction(DriverRouteEditPrimaryActionMaps.enabledEditRoute(
                "编辑路线",
                DriverRouteEditPrimaryActionMaps.buildPayload(
                        result.getDisId(),
                        result.getRouteDate(),
                        result.getBatchCode(),
                        result.getOperatorUserId(),
                        route.getDriverUserId(),
                        "DISPATCH_SANDBOX")));
    }

    private static CustomerStopPlan buildStopPlan(NxDisRouteStopEntity stop,
                                                  int sequence,
                                                  TodayDispatchResult result,
                                                  boolean suggested) {
        CustomerStopPlan plan = new CustomerStopPlan();
        plan.setSourceStop(stop);
        plan.setSequence(sequence);
        enrichStopPlan(plan, stop, result, suggested);
        return plan;
    }

    private static void enrichStopPlan(CustomerStopPlan plan,
                                       NxDisRouteStopEntity stop,
                                       TodayDispatchResult result,
                                       boolean suggested) {
        if (stop == null) {
            return;
        }
        Date serverNow = result.getServerNow() != null ? result.getServerNow() : new Date();
        String routeDate = result.getRouteDate();
        NxDisShipmentTaskEntity task = stop.getShipmentTask();

        Integer depId = stop.getNxDrsDepartmentId();
        if (depId == null && task != null) {
            depId = task.getNxDstDepFatherId();
        }
        plan.setDepFatherId(depId);
        plan.setCustomerId(depId);
        plan.setTaskId(task != null ? task.getNxDstId() : null);
        plan.setDeliveryStopId(resolveDeliveryStopId(stop, task));
        plan.setCustomerName(resolveCustomerName(stop, task));
        plan.setCustomerShortName(compactCustomerName(plan.getCustomerName()));
        plan.setCardKey(resolveCardKey(stop));
        plan.setSandboxStopKey(resolveSandboxStopKey(stop, depId));
        plan.setGoodsSummary(DisRouteSandboxDisplayFormatHelper.buildGoodsSummary(task));
        plan.setEarliestDeliveryTimeS(DisRouteSandboxStopTimeWindowResolver.readResolvedEarliest(stop));
        plan.setLatestDeliveryTimeS(DisRouteSandboxStopTimeWindowResolver.readResolvedLatest(stop));
        if (plan.getEarliestDeliveryTimeS() == null && task != null) {
            plan.setEarliestDeliveryTimeS(task.getNxDstEarliestDeliveryTimeS());
        }
        if (plan.getLatestDeliveryTimeS() == null && task != null) {
            plan.setLatestDeliveryTimeS(task.getNxDstLatestDeliveryTimeS());
        }
        plan.setServiceMinutes(stop.getNxDrsServiceMinutes());
        if (plan.getServiceMinutes() == null && task != null) {
            plan.setServiceMinutes(task.getNxDstServiceMinutes());
        }

        Date plannedArrivalAt = resolvePlannedArrivalAt(stop);
        Date plannedDepartureAt = resolvePlannedDepartureAt(stop);
        plan.setPlannedArrivalLabel(DisRouteTemporalHelper.formatRouteTimeLabel(
                plannedArrivalAt, serverNow, routeDate));
        plan.setPlannedDepartureLabel(DisRouteTemporalHelper.formatRouteTimeLabel(
                plannedDepartureAt, serverNow, routeDate));
        plan.setCustomerWindowLabel(DisRouteSandboxTodayStopScheduleHelper.resolveCustomerWindowLabel(
                stop, serverNow));
        plan.setWindowLabel(plan.getCustomerWindowLabel());
        DisRouteSandboxTodayStopScheduleHelper.WindowRequirementView windowRequirement =
                DisRouteSandboxTodayStopScheduleHelper.buildWindowRequirementView(stop);
        if (windowRequirement != null) {
            plan.setWindowRequirementLabel(windowRequirement.getLabel());
            plan.setWindowRequirementModified(windowRequirement.isModified());
        } else {
            plan.setWindowRequirementLabel(null);
            plan.setWindowRequirementModified(Boolean.FALSE);
        }
        plan.setServiceDurationLabel(formatCompactServiceDuration(
                DisRouteSandboxTodayStopScheduleHelper.resolveServiceDurationLabel(stop),
                plan.getServiceMinutes()));
        plan.setLegDistanceM(DisRouteSandboxDisplayFormatHelper.resolveLegDistanceM(stop));
        plan.setLegDurationS(DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop));
        plan.setDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(plan.getLegDistanceM()));
        plan.setDurationText(DisRouteSandboxDisplayFormatHelper.formatDurationText(plan.getLegDurationS()));
        plan.setTimeLabel(suggested ? null : buildStopTimeLabel(plan));
        applyArrivalStatusFromWindow(plan, plannedArrivalAt, routeDate);
        plan.setWindowStatusLabel(plan.getArrivalStatusLabel());
        plan.setStatusLabel(suggested ? plan.getArrivalStatusLabel() : "无司机");
        plan.setLat(parseCoordinate(firstNonBlank(
                stop.getNxDrsLat(), task != null ? task.getNxDstLat() : null)));
        plan.setLng(parseCoordinate(firstNonBlank(
                stop.getNxDrsLng(), task != null ? task.getNxDstLng() : null)));
        plan.setTimeWindowOverrideFlag(DisRouteSandboxStopTimeWindowResolver.isTodayOverride(stop));
        plan.setLiveOrderIds(collectLiveOrderIds(task));
    }

    private static Date resolvePlannedArrivalAt(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getNxDrsPlannedArrivalAt() != null) {
            return stop.getNxDrsPlannedArrivalAt();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        return task != null ? task.getNxDstPlannedArrivalAt() : null;
    }

    private static Date resolvePlannedDepartureAt(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getNxDrsPlannedDepartureAt() != null) {
            return stop.getNxDrsPlannedDepartureAt();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        return task != null ? task.getNxDstPlannedDepartureAt() : null;
    }

    private static Date resolveFirstStopPlannedArrivalAt(NxDisRouteStopEntity stop,
                                                         Date routeSuggestedDepartAt) {
        Date arrival = resolvePlannedArrivalAt(stop);
        if (arrival != null) {
            return arrival;
        }
        if (stop == null || routeSuggestedDepartAt == null) {
            return null;
        }
        Long legDurationS = DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop);
        if (legDurationS != null && legDurationS > 0L) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(routeSuggestedDepartAt);
            cal.add(Calendar.SECOND, legDurationS.intValue());
            return cal.getTime();
        }
        return null;
    }

    private static void applyArrivalStatusFromWindow(CustomerStopPlan plan,
                                                     Date arrivalAt,
                                                     String routeDate) {
        if (plan == null) {
            return;
        }
        String label = resolveArrivalWindowStatusLabel(
                plan.getEarliestDeliveryTimeS(),
                plan.getLatestDeliveryTimeS(),
                arrivalAt,
                routeDate);
        String tone = resolveArrivalWindowStatusTone(
                plan.getEarliestDeliveryTimeS(),
                plan.getLatestDeliveryTimeS(),
                arrivalAt,
                routeDate);
        plan.setArrivalStatusLabel(label);
        plan.setArrivalStatusTone(tone != null ? tone : "ok");
    }

    /** 与 DisRouteSandboxTodayDriverRoutePresentationHelper 窗口比较口径一致。 */
    private static String resolveArrivalWindowStatusLabel(Integer earliestDeliveryTimeS,
                                                          Integer latestDeliveryTimeS,
                                                          Date arrivalAt,
                                                          String routeDate) {
        if (arrivalAt == null || routeDate == null) {
            return null;
        }
        Date latest = latestDeliveryTimeS != null
                ? secondsOnRouteDate(routeDate, latestDeliveryTimeS) : null;
        if (latest != null && arrivalAt.after(latest)) {
            long lateSeconds = (arrivalAt.getTime() - latest.getTime()) / 1000L;
            int lateMinutes = minutesCeil(lateSeconds);
            return lateMinutes > 0 ? "迟到" + lateMinutes + "分钟" : "预计迟到";
        }
        Date earliest = earliestDeliveryTimeS != null
                ? secondsOnRouteDate(routeDate, earliestDeliveryTimeS) : null;
        if (earliest != null && arrivalAt.before(earliest)) {
            long waitSeconds = (earliest.getTime() - arrivalAt.getTime()) / 1000L;
            int waitMinutes = minutesCeil(waitSeconds);
            return waitMinutes > 0 ? "早到 " + waitMinutes + " 分钟" : "早于窗口";
        }
        if (earliest != null || latest != null) {
            return "准时";
        }
        return null;
    }

    private static String resolveArrivalWindowStatusTone(Integer earliestDeliveryTimeS,
                                                         Integer latestDeliveryTimeS,
                                                         Date arrivalAt,
                                                         String routeDate) {
        if (arrivalAt == null || routeDate == null) {
            return null;
        }
        Date latest = latestDeliveryTimeS != null
                ? secondsOnRouteDate(routeDate, latestDeliveryTimeS) : null;
        if (latest != null && arrivalAt.after(latest)) {
            return "warn";
        }
        Date earliest = earliestDeliveryTimeS != null
                ? secondsOnRouteDate(routeDate, earliestDeliveryTimeS) : null;
        if (earliest != null && arrivalAt.before(earliest)) {
            return "early";
        }
        if (earliest != null || latest != null) {
            return "ok";
        }
        return null;
    }

    private static Date secondsOnRouteDate(String routeDate, int seconds) {
        if (routeDate == null || routeDate.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date dayStart = sdf.parse(routeDate.trim());
            Calendar cal = Calendar.getInstance();
            cal.setTime(dayStart);
            cal.add(Calendar.SECOND, seconds);
            return cal.getTime();
        } catch (ParseException ex) {
            return null;
        }
    }

    private static int minutesCeil(long deltaSeconds) {
        if (deltaSeconds <= 0L) {
            return 0;
        }
        return (int) ((deltaSeconds + 59L) / 60L);
    }

    private static String formatCompactServiceDuration(String serviceDurationLabel, Integer serviceMinutes) {
        if (serviceMinutes != null && serviceMinutes > 0) {
            return serviceMinutes + " 分钟";
        }
        if (serviceDurationLabel == null || serviceDurationLabel.trim().isEmpty()) {
            return null;
        }
        String trimmed = serviceDurationLabel.trim();
        if (trimmed.startsWith("服务 ")) {
            trimmed = trimmed.substring("服务 ".length());
        }
        trimmed = trimmed.replace("分钟", " 分钟").replace("  分钟", " 分钟");
        return trimmed.trim();
    }

    private static String formatCompactDuration(Long durationS) {
        if (durationS == null || durationS <= 0L) {
            return null;
        }
        long minutes = Math.max(1L, Math.round(durationS / 60.0));
        if (minutes < 60L) {
            return minutes + "分钟";
        }
        long hours = minutes / 60L;
        long remain = minutes % 60L;
        if (remain <= 0L) {
            return hours + "小时";
        }
        return hours + "小时" + remain + "分";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void applyReturnLeg(DriverRoutePlan route, TodayDispatchResult result) {
        if (route.getReturnLegDistanceM() != null && route.getReturnLegDurationS() != null) {
            route.setReturnDistanceText(
                    DisRouteSandboxDisplayFormatHelper.formatDistanceText(route.getReturnLegDistanceM()));
            route.setReturnLegLabel(buildLegLabel(route.getReturnLegDistanceM(), route.getReturnLegDurationS()));
            return;
        }
        CustomerStopPlan lastStop = findLastStopWithCoordinates(route);
        if (lastStop == null || result.getDepotLat() == null || result.getDepotLng() == null) {
            return;
        }
        long returnDistanceM = Math.max(1L, straightLineMeters(
                lastStop.getLat(), lastStop.getLng(), result.getDepotLat(), result.getDepotLng()));
        long returnDurationS = Math.max(1L, returnDistanceM / 12L);
        route.setReturnLegDistanceM(returnDistanceM);
        route.setReturnLegDurationS(returnDurationS);
        route.setReturnDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(returnDistanceM));
        route.setReturnLegLabel(buildLegLabel(returnDistanceM, returnDurationS));
    }

    private static CustomerStopPlan findLastStopWithCoordinates(DriverRoutePlan route) {
        if (route.getStops() == null || route.getStops().isEmpty()) {
            return null;
        }
        for (int i = route.getStops().size() - 1; i >= 0; i--) {
            CustomerStopPlan stop = route.getStops().get(i);
            if (stop != null && stop.getLat() != null && stop.getLng() != null) {
                return stop;
            }
        }
        return null;
    }

    private static String buildLegLabel(Long distanceM, Long durationS) {
        return joinLegText(
                DisRouteSandboxDisplayFormatHelper.formatDistanceText(distanceM),
                DisRouteSandboxDisplayFormatHelper.formatDurationText(durationS));
    }

    private static String joinLegText(String distanceText, String durationText) {
        if (distanceText != null && durationText != null) {
            return distanceText + " · " + durationText;
        }
        if (distanceText != null) {
            return distanceText;
        }
        return durationText;
    }

    private static long straightLineMeters(double lat1, double lng1, double lat2, double lng2) {
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);
        double dLat = rLat2 - rLat1;
        double dLng = Math.toRadians(lng2 - lng1);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(rLat1) * Math.cos(rLat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return Math.round(2 * 6371000D * Math.asin(Math.min(1.0, Math.sqrt(h))));
    }

    private static Map<String, Object> buildSuggestedStopPrimaryAction(CustomerStopPlan stop,
                                                                     TodayDispatchResult result) {
        Integer depId = stop.getDepFatherId();
        if (depId == null) {
            return null;
        }
        return SandboxTodayPrimaryActionMaps.enabledEditTodayTimeWindow(
                SandboxTodayPrimaryActionMaps.buildEditTodayTimeWindowPayload(
                        result.getDisId(),
                        result.getRouteDate(),
                        result.getBatchCode(),
                        result.getOperatorUserId(),
                        depId,
                        stop.getSandboxStopKey(),
                        stop.getDeliveryStopId(),
                        stop.getEarliestDeliveryTimeS(),
                        stop.getLatestDeliveryTimeS(),
                        stop.getServiceMinutes(),
                        stop.getTimeWindowOverrideFlag(),
                        stop.getCustomerWindowLabel(),
                        stop.getCustomerName()));
    }

    private static Map<String, Object> buildUnassignedPrimaryAction(NxDisRouteStopEntity stop,
                                                                    CustomerStopPlan plan,
                                                                    TodayDispatchResult result) {
        if (!hasOnDutyDriver(result)) {
            return ManualDispatchPrimaryActionMaps.disabledStartManualDispatch("当前没有上岗司机");
        }
        Integer depId = plan != null ? plan.getDepFatherId() : null;
        if (depId == null && stop != null) {
            depId = stop.getNxDrsDepartmentId();
        }
        String sandboxStopKey = plan != null ? plan.getSandboxStopKey() : null;
        if (sandboxStopKey == null && depId != null) {
            sandboxStopKey = DisRouteSandboxStopKeyUtils.build(depId);
        }
        List<Integer> liveOrderIds = plan != null && plan.getLiveOrderIds() != null
                ? plan.getLiveOrderIds() : new ArrayList<Integer>();
        Map<String, Object> payload = ManualDispatchPrimaryActionMaps.buildPayload(
                result.getDisId(),
                result.getRouteDate(),
                result.getBatchCode(),
                result.getOperatorUserId(),
                depId,
                sandboxStopKey,
                liveOrderIds);
        return ManualDispatchPrimaryActionMaps.enabledStartManualDispatch("人工调度", payload);
    }

    private void applyDepot(TodayDispatchResult result, SandboxComputeResult compute, Integer disId) {
        NxDisRoutePlanEntity plan = compute.getMergedPlan();
        if (plan == null) {
            plan = compute.getPlanContext();
        }
        result.setDepotName(DisRouteSandboxTodayTimelineBuilder.DEPOT_NAME);
        if (plan != null) {
            if (plan.getNxDrpDepotLat() != null) {
                result.setDepotLat(parseCoordinate(plan.getNxDrpDepotLat()));
            }
            if (plan.getNxDrpDepotLng() != null) {
                result.setDepotLng(parseCoordinate(plan.getNxDrpDepotLng()));
            }
        }
        if (disId != null && nxDistributerService != null) {
            NxDistributerEntity dis = nxDistributerService.queryObject(disId);
            if (dis != null) {
                String name = firstNonBlank(
                        dis.getNxDistributerShowName(),
                        dis.getNxDistributerName(),
                        dis.getNxDistributerMarketName());
                if (name != null) {
                    result.setDepotName(name);
                }
                if (dis.getNxDistributerAddress() != null && !dis.getNxDistributerAddress().trim().isEmpty()) {
                    result.setDepotAddress(dis.getNxDistributerAddress().trim());
                }
            }
        }
        if (result.getDepotAddress() == null) {
            result.setDepotAddress(null);
        }
    }

    private static void applyMapSettings(TodayDispatchResult result) {
        String subkey = System.getProperty("nxl.routeDispatch.map.subkey");
        if (subkey == null || subkey.trim().isEmpty()) {
            subkey = System.getenv("NXL_ROUTE_DISPATCH_MAP_SUBKEY");
        }
        result.setMapSubkey(subkey);
        result.setMapLayerStyle("satellite");
    }

    private static boolean hasOnDutyDriver(TodayDispatchResult result) {
        SandboxComputeResult compute = result.getCompute();
        return compute != null && compute.getOnDutyDrivers() != null && !compute.getOnDutyDrivers().isEmpty();
    }

    private static String buildUnassignedDriverHint(TodayDispatchResult result) {
        if (result.getAvailableDrivers() == null || result.getAvailableDrivers().isEmpty()) {
            return "暂无空闲司机，请先安排上岗";
        }
        StringBuilder sb = new StringBuilder("可派：");
        int count = 0;
        for (Map<String, Object> driver : result.getAvailableDrivers()) {
            if (driver == null || driver.get("driverName") == null) {
                continue;
            }
            if (count > 0) {
                sb.append("、");
            }
            sb.append(String.valueOf(driver.get("driverName")));
            count++;
            if (count >= 3) {
                break;
            }
        }
        if (result.getAvailableDrivers().size() > 3) {
            sb.append(" 等");
        }
        return sb.toString();
    }

    private static String resolveCustomerName(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        if (stop != null && stop.getNxDrsDepartmentName() != null
                && !stop.getNxDrsDepartmentName().trim().isEmpty()) {
            return stop.getNxDrsDepartmentName().trim();
        }
        if (stop != null && stop.getLiveDepartmentName() != null
                && !stop.getLiveDepartmentName().trim().isEmpty()) {
            return stop.getLiveDepartmentName().trim();
        }
        if (task != null && task.getLiveDepartmentName() != null
                && !task.getLiveDepartmentName().trim().isEmpty()) {
            return task.getLiveDepartmentName().trim();
        }
        if (task != null && task.getNxDstDepName() != null && !task.getNxDstDepName().trim().isEmpty()) {
            return task.getNxDstDepName().trim();
        }
        return null;
    }

    private static String resolveCardKey(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getSandboxStopKey() != null && !stop.getSandboxStopKey().trim().isEmpty()) {
            return stop.getSandboxStopKey().trim();
        }
        if (stop.getNxDrsDepartmentId() != null) {
            return DisRouteSandboxStopKeyUtils.build(stop.getNxDrsDepartmentId());
        }
        return null;
    }

    private static String resolveSandboxStopKey(NxDisRouteStopEntity stop, Integer depId) {
        if (stop != null && stop.getSandboxStopKey() != null && !stop.getSandboxStopKey().trim().isEmpty()) {
            return stop.getSandboxStopKey().trim();
        }
        if (depId != null) {
            return DisRouteSandboxStopKeyUtils.build(depId);
        }
        return null;
    }

    private static Integer resolveDeliveryStopId(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        if (task != null && task.getNxDstId() != null) {
            return task.getNxDstId();
        }
        if (stop != null && stop.getNxDrsShipmentTaskId() != null) {
            return stop.getNxDrsShipmentTaskId();
        }
        return null;
    }

    private static List<Integer> collectLiveOrderIds(NxDisShipmentTaskEntity task) {
        if (task == null || task.getItems() == null || task.getItems().isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> liveOrderIds = new ArrayList<Integer>();
        for (NxDisShipmentTaskItemEntity item : task.getItems()) {
            if (item != null && item.getNxDstiLiveOrderId() != null) {
                liveOrderIds.add(item.getNxDstiLiveOrderId());
            }
        }
        return liveOrderIds;
    }

    private static String buildStopTimeLabel(CustomerStopPlan plan) {
        String legText = DisRouteSandboxTodayTimelineBuilder.joinLegText(
                plan.getDistanceText(), plan.getDurationText());
        if (legText != null && !legText.trim().isEmpty()) {
            return legText.trim();
        }
        if (plan.getPlannedArrivalLabel() != null && !plan.getPlannedArrivalLabel().trim().isEmpty()) {
            return plan.getPlannedArrivalLabel().trim();
        }
        return null;
    }

    private static String buildRouteStatsLine(int stopCount,
                                              String totalDistanceText,
                                              String plannedDepartLabel,
                                              String plannedReturnLabel,
                                              String totalDurationText) {
        StringBuilder sb = new StringBuilder();
        sb.append("共 ").append(stopCount).append(" 站");
        if (totalDistanceText != null && !totalDistanceText.isEmpty()) {
            sb.append(" · 往返里程 ").append(totalDistanceText);
        }
        if (plannedDepartLabel != null && plannedReturnLabel != null) {
            sb.append(" · ").append(plannedDepartLabel).append(" 出发，")
                    .append(plannedReturnLabel).append(" 返回");
        }
        if (totalDurationText != null && !totalDurationText.isEmpty()) {
            sb.append(" · 全程 ").append(totalDurationText);
        }
        return sb.toString();
    }

    private static String buildRouteRoundTripSummaryLine(String distanceText, String durationText) {
        StringBuilder sb = new StringBuilder();
        if (distanceText != null && !distanceText.isEmpty()) {
            sb.append("往返 ").append(distanceText);
        }
        if (durationText != null && !durationText.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" · 全程 ");
            } else {
                sb.append("全程 ");
            }
            sb.append(durationText);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private static String buildRouteSuggestedDepartLabel(String plannedDepartLabel) {
        if (plannedDepartLabel == null || plannedDepartLabel.trim().isEmpty()) {
            return null;
        }
        return "建议出发：" + plannedDepartLabel.trim();
    }

    private static String buildFirstStopPlannedArrivalLabel(String arrivalLabel) {
        if (arrivalLabel == null || arrivalLabel.trim().isEmpty()) {
            return null;
        }
        String trimmed = arrivalLabel.trim();
        if (trimmed.startsWith("约") || trimmed.endsWith("到")) {
            return "首站 " + trimmed;
        }
        return "首站预计 " + trimmed + " 到达";
    }

    private static Date resolvePlannedReturnAt(DriverRoutePlan route) {
        if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
            return null;
        }
        CustomerStopPlan last = route.getStops().get(route.getStops().size() - 1);
        if (last == null || last.getSourceStop() == null) {
            return null;
        }
        Date lastDeparture = last.getSourceStop().getNxDrsPlannedDepartureAt();
        if (lastDeparture == null) {
            return null;
        }
        long returnDurationS = route.getReturnLegDurationS() != null ? route.getReturnLegDurationS() : 0L;
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastDeparture);
        cal.add(Calendar.SECOND, (int) returnDurationS);
        return cal.getTime();
    }

    private static Date resolveRouteSuggestedDepartAt(DriverRoutePlan route,
                                                      TodayDispatchResult result,
                                                      Date plannedReturnAt) {
        if (route.getPlannedDepartAt() != null) {
            Date serverNow = result.getServerNow();
            if (serverNow != null && !route.getPlannedDepartAt().after(serverNow)) {
                return serverNow;
            }
            return route.getPlannedDepartAt();
        }
        return result.getServerNow();
    }

    private static String formatDepartTimeLabel(Date plannedDepartAt,
                                                Date serverNow,
                                                String routeDate) {
        if (plannedDepartAt == null) {
            return "现在";
        }
        if (serverNow != null && !plannedDepartAt.after(serverNow)) {
            return "现在";
        }
        String label = DisRouteTemporalHelper.formatRouteTimeLabel(plannedDepartAt, serverNow, routeDate);
        return label != null && !label.trim().isEmpty() ? label.trim() : "现在";
    }

    private static Long calcRoundTripDurationS(Date departAt, Date returnAt) {
        if (departAt == null || returnAt == null) {
            return null;
        }
        long diffMs = returnAt.getTime() - departAt.getTime();
        if (diffMs <= 0L) {
            return null;
        }
        return diffMs / 1000L;
    }

    private static String buildRouteHeadlineLine(String firstStopArrivalLabel, String plannedReturnLabel) {
        if (firstStopArrivalLabel == null && plannedReturnLabel == null) {
            return null;
        }
        if (firstStopArrivalLabel != null && plannedReturnLabel != null) {
            return firstStopArrivalLabel + " · " + plannedReturnLabel + " 回仓";
        }
        return firstStopArrivalLabel != null ? firstStopArrivalLabel : plannedReturnLabel + " 回仓";
    }

    private static String compactCustomerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String trimmed = name.trim();
        return trimmed.length() <= 6 ? trimmed : trimmed.substring(0, 6);
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

    private static String normalizeBatch(String batchCode) {
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return DisRouteDispatchBatch.MORNING;
        }
        return batchCode.trim().toUpperCase();
    }

    private static Double parseCoordinate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * 路线编辑：按 stopKeys 重排并重算 ETA/里程（复用分派中 schedule pipeline）。
     */
    public DriverRoutePlan buildEditedDriverRoute(TodayDispatchResult context,
                                                  Integer driverUserId,
                                                  List<String> stopKeys) throws Exception {
        if (context == null || driverUserId == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }
        List<String> effectiveKeys = stopKeys != null && !stopKeys.isEmpty()
                ? stopKeys
                : defaultStopKeysForDriver(context, driverUserId);
        Map<String, CustomerStopPlan> stopIndex = indexEditableStopPlans(context, driverUserId);
        List<NxDisRouteStopEntity> working = new ArrayList<NxDisRouteStopEntity>();
        for (String stopKey : effectiveKeys) {
            String normalizedKey = normalizeStopKey(stopKey);
            CustomerStopPlan plan = stopIndex.get(normalizedKey);
            if (plan == null && todayDispatchRouteEditStopPool != null) {
                plan = todayDispatchRouteEditStopPool.resolveStop(context, driverUserId, stopKey);
            }
            if (plan == null) {
                throw new IllegalArgumentException("stopKey 不在可编辑客户池: " + stopKey);
            }
            NxDisRouteStopEntity entity = resolveEditableStopEntity(plan);
            if (entity == null && todayDispatchRouteEditStopPool != null) {
                entity = findBaselineEntityByStopKey(context, driverUserId, normalizedKey);
            }
            if (entity == null) {
                throw new IllegalArgumentException("stopKey 不在可编辑客户池: " + stopKey);
            }
            stampStopForRouteEdit(entity, driverUserId);
            working.add(entity);
        }

        hydrateRouteEditWorkingStops(working, context);

        DriverRoutePlan route = new DriverRoutePlan();
        route.setDriverUserId(driverUserId);
        route.setDriverName(resolveDriverDisplayName(context, driverUserId));
        SandboxComputeResult compute = context.getCompute();
        applyRouteEditSchedulePipeline(working, route, context, compute);

        List<CustomerStopPlan> stops = new ArrayList<CustomerStopPlan>();
        int seq = 1;
        for (NxDisRouteStopEntity entity : working) {
            stops.add(buildStopPlan(entity, seq++, context, true));
        }
        route.setStops(stops);

        enrichSingleDriverRoute(route, context);
        return route;
    }

    /** 单条司机路线展示 enrich（路线编辑 / 人工调度预览共用）。 */
    public void enrichSingleDriverRoute(DriverRoutePlan route, TodayDispatchResult context) {
        if (route == null || context == null) {
            return;
        }
        if (route.getStops() != null) {
            for (CustomerStopPlan stop : route.getStops()) {
                if (stop == null) {
                    continue;
                }
                enrichStopPlan(stop, stop.getSourceStop(), context, true);
                stop.setPrimaryAction(buildSuggestedStopPrimaryAction(stop, context));
            }
        }
        enrichRouteCard(route, context);
    }

    private List<String> defaultStopKeysForDriver(TodayDispatchResult context, Integer driverUserId) {
        return collectDriverStopKeys(context, driverUserId);
    }

    /**
     * 司机当前可编辑站点键：沙盘建议 + 已确认 + 装车中 + 配送中 + 路线基线（去重保序）。
     * 人工调度 / 路线编辑共用，避免只带新店丢失老店铺。
     */
    List<String> collectDriverStopKeys(TodayDispatchResult context, Integer driverUserId) {
        LinkedHashSet<String> ordered = new LinkedHashSet<String>();
        if (context == null || driverUserId == null) {
            return new ArrayList<String>();
        }
        DriverRoutePlan suggestedRoute = findDriverRoute(context, driverUserId);
        if (suggestedRoute != null && suggestedRoute.getStops() != null) {
            for (CustomerStopPlan stop : suggestedRoute.getStops()) {
                String key = resolvePlanStopKey(stop);
                if (key != null) {
                    ordered.add(key);
                }
            }
        }
        SandboxComputeResult compute = context.getCompute();
        if (compute != null) {
            appendDriverStopKeysFromEntities(ordered, compute.getConfirmedStops(), driverUserId);
            appendDriverStopKeysFromEntities(ordered, compute.getLoadingStops(), driverUserId);
            appendDriverStopKeysFromEntities(ordered, compute.getExecutionStops(), driverUserId);
            appendDriverStopKeysFromEntities(ordered, compute.getSandboxSuggestedStops(), driverUserId);
        }
        appendDriverStopKeysFromBaseline(ordered, context, driverUserId);
        return new ArrayList<String>(ordered);
    }

    /** 司机当前在途/待送客户名（不含即将人工调度的新店）。 */
    List<String> collectDriverPendingStopNames(TodayDispatchResult context, Integer driverUserId) {
        List<String> names = new ArrayList<String>();
        if (context == null || driverUserId == null || todayDispatchRouteEditStopPool == null) {
            return names;
        }
        LinkedHashSet<String> deduped = new LinkedHashSet<String>();
        for (NxDisRouteStopEntity stop : todayDispatchRouteEditStopPool.collectDriverBaselineStops(
                context, driverUserId)) {
            if (stop == null) {
                continue;
            }
            String name = resolveCustomerName(stop, stop.getShipmentTask());
            if (name != null && !name.isEmpty()) {
                deduped.add(name);
            }
        }
        names.addAll(deduped);
        return names;
    }

    private void appendDriverStopKeysFromBaseline(LinkedHashSet<String> ordered,
                                                  TodayDispatchResult context,
                                                  Integer driverUserId) {
        if (ordered == null || context == null || driverUserId == null
                || todayDispatchRouteEditStopPool == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : todayDispatchRouteEditStopPool.collectDriverBaselineStops(
                context, driverUserId)) {
            String key = resolveEntityStopKey(stop);
            if (key != null) {
                ordered.add(key);
            }
        }
    }

    private static void appendDriverStopKeysFromEntities(LinkedHashSet<String> ordered,
                                                         List<NxDisRouteStopEntity> stops,
                                                         Integer driverUserId) {
        if (ordered == null || stops == null || driverUserId == null) {
            return;
        }
        List<NxDisRouteStopEntity> forDriver = new ArrayList<NxDisRouteStopEntity>();
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            if (driverUserId.equals(DisRouteSandboxDriverDispatchStateHelper.resolveStopDriverUserId(stop))) {
                forDriver.add(stop);
            }
        }
        Collections.sort(forDriver, new Comparator<NxDisRouteStopEntity>() {
            @Override
            public int compare(NxDisRouteStopEntity a, NxDisRouteStopEntity b) {
                return Integer.compare(resolveEntityStopSeq(a), resolveEntityStopSeq(b));
            }
        });
        for (NxDisRouteStopEntity stop : forDriver) {
            String key = resolveEntityStopKey(stop);
            if (key != null) {
                ordered.add(key);
            }
        }
    }

    static String resolveEntityStopKey(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        Integer depId = stop.getNxDrsDepartmentId();
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (depId == null && task != null) {
            depId = task.getNxDstDepFatherId();
        }
        return resolveSandboxStopKey(stop, depId);
    }

    private static DriverRoutePlan findDriverRoute(TodayDispatchResult context, Integer driverUserId) {
        if (context == null || context.getSuggestedRoutes() == null || driverUserId == null) {
            return null;
        }
        for (DriverRoutePlan route : context.getSuggestedRoutes()) {
            if (route != null && driverUserId.equals(route.getDriverUserId())) {
                return route;
            }
        }
        return null;
    }

    Map<String, CustomerStopPlan> indexEditableStopPlans(TodayDispatchResult context,
                                                                Integer driverUserId) {
        Map<String, CustomerStopPlan> index = new LinkedHashMap<String, CustomerStopPlan>();
        if (context == null) {
            return index;
        }
        for (CustomerStopPlan stop : TodayDispatchRouteEditStopPool.collectAllSandboxCustomerPlans(context)) {
            putStopIndex(index, stop);
        }
        SandboxComputeResult compute = context.getCompute();
        if (compute != null) {
            appendEntityStopIndex(index, context, compute.getSandboxSuggestedStops());
            appendEntityStopIndex(index, context,
                    DisRouteSandboxUnassignedStopHelper.consolidateByDepartment(compute.getUnassignedStops()));
            appendEntityStopIndex(index, context, compute.getConfirmedStops());
            appendEntityStopIndexFromBaseline(index, context, driverUserId);
            // 装车/配送中的 task 快照最后写入，避免被路线基线里的旧时间窗覆盖
            appendEntityStopIndexForDriver(index, context, compute.getLoadingStops(), driverUserId);
            appendEntityStopIndexForDriver(index, context, compute.getExecutionStops(), driverUserId);
        }
        return index;
    }

    private void hydrateRouteEditWorkingStops(List<NxDisRouteStopEntity> working,
                                              TodayDispatchResult context) {
        if (working == null || working.isEmpty() || context == null
                || todayDispatchStopTimeWindowHydrator == null) {
            return;
        }
        List<NxDisShipmentTaskEntity> planTasks = context.getCompute() != null
                ? context.getCompute().getAllDisplayTasks() : null;
        todayDispatchStopTimeWindowHydrator.hydratePlanRouteStops(
                context.getDisId(), context.getRouteDate(), working, planTasks);
    }

    private void appendEntityStopIndexFromBaseline(Map<String, CustomerStopPlan> index,
                                                   TodayDispatchResult context,
                                                   Integer driverUserId) {
        if (index == null || context == null || driverUserId == null
                || todayDispatchRouteEditStopPool == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : todayDispatchRouteEditStopPool.collectDriverBaselineStops(
                context, driverUserId)) {
            putStopIndex(index, buildStopPlanForPool(stop, context));
        }
    }

    static CustomerStopPlan buildStopPlanForPool(NxDisRouteStopEntity stop, TodayDispatchResult context) {
        return buildStopPlan(stop, 0, context, false);
    }

    private static void appendEntityStopIndex(Map<String, CustomerStopPlan> index,
                                              TodayDispatchResult context,
                                              List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            putStopIndex(index, buildStopPlanForPool(stop, context));
        }
    }

    private static void appendEntityStopIndexForDriver(Map<String, CustomerStopPlan> index,
                                                       TodayDispatchResult context,
                                                       List<NxDisRouteStopEntity> stops,
                                                       Integer driverUserId) {
        if (stops == null || driverUserId == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Integer stopDriverId = DisRouteSandboxDriverDispatchStateHelper.resolveStopDriverUserId(stop);
            if (!driverUserId.equals(stopDriverId)) {
                continue;
            }
            putStopIndex(index, buildStopPlanForPool(stop, context));
        }
    }

    private static void putStopIndex(Map<String, CustomerStopPlan> index, CustomerStopPlan stop) {
        if (stop == null) {
            return;
        }
        String key = resolvePlanStopKey(stop);
        if (key != null) {
            index.put(key, stop);
        }
    }

    static String resolvePlanStopKey(CustomerStopPlan stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getSandboxStopKey() != null && !stop.getSandboxStopKey().trim().isEmpty()) {
            return normalizeStopKey(stop.getSandboxStopKey());
        }
        if (stop.getCardKey() != null && !stop.getCardKey().trim().isEmpty()) {
            return normalizeStopKey(stop.getCardKey());
        }
        if (stop.getDepFatherId() != null) {
            return DisRouteSandboxStopKeyUtils.build(stop.getDepFatherId());
        }
        return null;
    }

    private static String normalizeStopKey(String stopKey) {
        if (stopKey == null) {
            return null;
        }
        String trimmed = stopKey.trim();
        if (trimmed.startsWith(DisRouteSandboxStopKeyUtils.PREFIX)) {
            return trimmed;
        }
        if (trimmed.startsWith("dep:")) {
            return trimmed;
        }
        try {
            return DisRouteSandboxStopKeyUtils.build(Integer.valueOf(trimmed));
        } catch (NumberFormatException ignored) {
            return trimmed;
        }
    }

    private static NxDisRouteStopEntity resolveEditableStopEntity(CustomerStopPlan plan) {
        if (plan == null) {
            return null;
        }
        if (plan.getSourceStop() != null) {
            return cloneStopEntity(plan.getSourceStop());
        }
        NxDisRouteStopEntity entity = new NxDisRouteStopEntity();
        entity.setNxDrsDepartmentId(plan.getDepFatherId());
        entity.setNxDrsDepartmentName(plan.getCustomerName());
        entity.setSandboxStopKey(plan.getSandboxStopKey());
        if (plan.getLat() != null) {
            entity.setNxDrsLat(String.valueOf(plan.getLat()));
        }
        if (plan.getLng() != null) {
            entity.setNxDrsLng(String.valueOf(plan.getLng()));
        }
        if (plan.getEarliestDeliveryTimeS() != null) {
            entity.setNxDrsEarliestDeliveryTimeS(plan.getEarliestDeliveryTimeS());
        }
        if (plan.getLatestDeliveryTimeS() != null) {
            entity.setNxDrsLatestDeliveryTimeS(plan.getLatestDeliveryTimeS());
        }
        if (plan.getServiceMinutes() != null) {
            entity.setNxDrsServiceMinutes(plan.getServiceMinutes());
        }
        return entity;
    }

    private static NxDisRouteStopEntity cloneStopEntity(NxDisRouteStopEntity source) {
        if (source == null) {
            return null;
        }
        NxDisRouteStopEntity copy = new NxDisRouteStopEntity();
        copy.setNxDrsId(source.getNxDrsId());
        copy.setNxDrsDepartmentId(source.getNxDrsDepartmentId());
        copy.setNxDrsDepartmentName(source.getNxDrsDepartmentName());
        copy.setNxDrsLat(source.getNxDrsLat());
        copy.setNxDrsLng(source.getNxDrsLng());
        copy.setNxDrsStopSeq(source.getNxDrsStopSeq());
        copy.setNxDrsPlannedArrivalAt(source.getNxDrsPlannedArrivalAt());
        copy.setNxDrsPlannedDepartureAt(source.getNxDrsPlannedDepartureAt());
        copy.setNxDrsEarliestDeliveryTimeS(source.getNxDrsEarliestDeliveryTimeS());
        copy.setNxDrsLatestDeliveryTimeS(source.getNxDrsLatestDeliveryTimeS());
        copy.setNxDrsServiceMinutes(source.getNxDrsServiceMinutes());
        copy.setNxDrsTimeWindowOverrideFlag(source.getNxDrsTimeWindowOverrideFlag());
        copy.setResolvedEarliestDeliveryTimeS(source.getResolvedEarliestDeliveryTimeS());
        copy.setResolvedLatestDeliveryTimeS(source.getResolvedLatestDeliveryTimeS());
        copy.setResolvedWindowSource(source.getResolvedWindowSource());
        copy.setNxDrsLegDistanceM(source.getNxDrsLegDistanceM());
        copy.setNxDrsLegDurationS(source.getNxDrsLegDurationS());
        copy.setSandboxStopKey(source.getSandboxStopKey());
        copy.setCustomerWindowLabel(source.getCustomerWindowLabel());
        copy.setShipmentTask(source.getShipmentTask());
        copy.setNxDrsShipmentTaskId(source.getNxDrsShipmentTaskId());
        return copy;
    }

    private static String resolveDriverDisplayName(TodayDispatchResult context, Integer driverUserId) {
        DriverRoutePlan route = findDriverRoute(context, driverUserId);
        if (route != null && route.getDriverName() != null && !route.getDriverName().trim().isEmpty()) {
            return route.getDriverName().trim();
        }
        if (context != null && context.getCompute() != null && context.getCompute().getOnDutyDrivers() != null) {
            for (com.nongxinle.entity.NxDistributerUserEntity driver : context.getCompute().getOnDutyDrivers()) {
                if (driver != null && driverUserId.equals(driver.getNxDistributerUserId())) {
                    if (driver.getNxDiuWxNickName() != null && !driver.getNxDiuWxNickName().trim().isEmpty()) {
                        return driver.getNxDiuWxNickName().trim();
                    }
                }
            }
        }
        return String.valueOf(driverUserId);
    }
}
