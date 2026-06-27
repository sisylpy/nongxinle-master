package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dto.route.DriverDispatchCandidateDto;
import com.nongxinle.dto.route.DriverDispatchListResponse;
import com.nongxinle.dto.route.RouteFeasibilityResult;
import com.nongxinle.dto.route.SandboxComputeRequest;
import com.nongxinle.dto.route.SandboxComputeResult;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;
import com.nongxinle.route.DisRouteDispatchDisIdGuard;
import com.nongxinle.route.DisRouteDispatchLabels;
import com.nongxinle.route.DisRouteDispatchOperatorResolver;
import com.nongxinle.route.DisRouteRouteExecutionHelper;
import com.nongxinle.route.DisRouteSandboxManualDispatchComputeEnricher;
import com.nongxinle.route.DisRouteSandboxDriverTerminalPageBuilder;
import com.nongxinle.route.DisRouteSandboxDriverTerminalStopCollector;
import com.nongxinle.route.DisRouteSandboxReadModelPartitionHelper;
import com.nongxinle.route.DisRouteSandboxTodayPlanStopSyncHelper;
import com.nongxinle.route.DisRouteTemporalHelper;
import com.nongxinle.route.DriverTerminalBuildContext;
import com.nongxinle.route.RouteDispatchDateFormat;
import com.nongxinle.dto.route.RouteDispatchReadModelAssembler;
import com.nongxinle.service.DisRouteDispatchOperationPolicy;
import com.nongxinle.service.DisRouteDriverDispatchListService;
import com.nongxinle.service.DisRouteFeasibilityService;
import com.nongxinle.service.DisRouteSandboxComputeService;
import com.nongxinle.service.DisRouteSandboxDriverTerminalService;
import com.nongxinle.service.NxDepartmentOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DriverTerminalBuildContext.PHASE_DELIVERY;
import static com.nongxinle.route.DriverTerminalBuildContext.PHASE_LOADING;

@Service("disRouteSandboxDriverTerminalService")
public class DisRouteSandboxDriverTerminalServiceImpl implements DisRouteSandboxDriverTerminalService {

    @Autowired
    private DisRouteSandboxComputeService disRouteSandboxComputeService;
    @Autowired
    private DisRouteDriverDispatchListService disRouteDriverDispatchListService;
    @Autowired
    private DisRouteDispatchOperationPolicy disRouteDispatchOperationPolicy;
    @Autowired
    private DisRouteFeasibilityService disRouteFeasibilityService;
    @Autowired
    private DisRouteDispatchDisIdGuard disRouteDispatchDisIdGuard;
    @Autowired
    private DisRouteDispatchOperatorResolver disRouteDispatchOperatorResolver;
    @Autowired
    private DisRouteSandboxDriverTerminalPageBuilder driverTerminalPageBuilder;
    @Autowired
    private DisRouteSandboxDriverTerminalStopCollector driverTerminalStopCollector;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;

    @Override
    public Map<String, Object> buildDriverLoadingToday(Integer disId,
                                                       String routeDate,
                                                       String batchCode,
                                                       Integer driverUserId) throws Exception {
        validateRequest(disId, driverUserId);
        DriverTerminalBuildContext ctx = prepareContext(disId, routeDate, batchCode, driverUserId, PHASE_LOADING);
        return wrapResponse(ctx);
    }

    @Override
    public Map<String, Object> buildDriverDeliveryToday(Integer disId,
                                                        String routeDate,
                                                        String batchCode,
                                                        Integer driverUserId) throws Exception {
        validateRequest(disId, driverUserId);
        DriverTerminalBuildContext ctx = prepareContext(disId, routeDate, batchCode, driverUserId, PHASE_DELIVERY);
        return wrapResponse(ctx);
    }

    private Map<String, Object> wrapResponse(DriverTerminalBuildContext ctx) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("routeDate", ctx.getRouteDate());
        data.put("routeDateLabel", ctx.getRouteDateLabel());
        data.put("dispatchBatch", ctx.getBatchCode());
        data.put("dispatchBatchLabel", ctx.getBatchCodeLabel());
        data.put("driverUserId", ctx.getDriverUserId());
        data.put("driverName", ctx.getDriverName());
        if (ctx.getServerNow() != null) {
            data.put("serverNow", RouteDispatchDateFormat.format(ctx.getServerNow()));
        }
        Map<Integer, Integer> purchaseStatusByOrderId = buildPurchaseStatusByOrderId(ctx.getStops());
        data.put("pageViewModel", driverTerminalPageBuilder.build(ctx, purchaseStatusByOrderId));
        return data;
    }

    private DriverTerminalBuildContext prepareContext(Integer disId,
                                                      String routeDate,
                                                      String batchCode,
                                                      Integer driverUserId,
                                                      String phase) throws Exception {
        disRouteDispatchDisIdGuard.logMissingDistributerIfNeeded(disId,
                PHASE_LOADING.equals(phase) ? "driver-terminal/loading/today" : "driver-terminal/delivery/today");

        SandboxComputeRequest computeRequest = new SandboxComputeRequest();
        computeRequest.setDisId(disId);
        computeRequest.setRouteDate(routeDate);
        computeRequest.setBatchCode(batchCode);
        computeRequest.setFormalPageContractMode(true);
        computeRequest.setPersistedRoutesOnlyMode(true);
        SandboxComputeResult compute = disRouteSandboxComputeService.compute(computeRequest);
        DisRouteSandboxManualDispatchComputeEnricher.enrich(compute);

        Date serverNow = new Date();
        String effectiveRouteDate = compute.getRouteDate();
        String normalizedBatch = compute.getDispatchBatch() != null ? compute.getDispatchBatch() : "MORNING";

        DriverDispatchListResponse drivers = disRouteDriverDispatchListService.listDriversForBatch(
                disId, effectiveRouteDate, normalizedBatch, false);
        DriverDispatchCandidateDto driverMeta = findDriver(drivers, driverUserId);
        if (driverMeta == null) {
            throw new IllegalArgumentException("司机不存在: " + driverUserId);
        }

        NxDisRoutePlanEntity mergedPlan = compute.getMergedPlan();
        List<NxDisShipmentTaskEntity> tasks = compute.getAllDisplayTasks();
        RouteFeasibilityResult feasibility = mergedPlan != null
                ? disRouteFeasibilityService.assess(mergedPlan.getNxDrpId())
                : new RouteFeasibilityResult();

        if (mergedPlan != null) {
            RouteDispatchReadModelAssembler.linkSharedTaskInstances(mergedPlan, tasks);
            DisRouteSandboxReadModelPartitionHelper.linkConfirmedStopsToTasks(
                    compute.getConfirmedStops(), tasks);
            DisRouteSandboxReadModelPartitionHelper.linkConfirmedStopsToTasks(
                    compute.getExecutionStops(), tasks);
            DisRouteSandboxReadModelPartitionHelper.linkConfirmedStopsToTasks(
                    compute.getLoadingStops(), tasks);
            disRouteDispatchOperationPolicy.enrichTasksReadModel(tasks, mergedPlan, feasibility);
            disRouteDispatchOperationPolicy.enrichPlanReadModel(mergedPlan, feasibility);
            disRouteDispatchOperationPolicy.enrichExecutionRoutesReadModel(mergedPlan, feasibility);
            disRouteDispatchOperationPolicy.enrichLoadingDriverRoutesReadModel(mergedPlan, feasibility);

            DisRouteSandboxTodayPlanStopSyncHelper.syncFullScheduleFromPlan(
                    mergedPlan,
                    compute.getSandboxSuggestedStops(),
                    compute.getConfirmedStops(),
                    compute.getLoadingStops(),
                    compute.getExecutionStops(),
                    compute.getUnassignedStops());
        }

        NxDisDriverRouteEntity route;
        List<NxDisRouteStopEntity> stops;
        if (PHASE_DELIVERY.equals(phase)) {
            NxDisDriverRouteEntity dbRoute = null;
            if (mergedPlan != null && mergedPlan.getNxDrpId() != null) {
                dbRoute = nxDisDriverRouteDao.queryByPlanAndDriver(
                        mergedPlan.getNxDrpId(), driverUserId);
            }
            route = resolveExecutionRoute(mergedPlan, driverUserId);
            if (route == null && dbRoute != null && DisRouteRouteExecutionHelper.isExecutionRoute(dbRoute)) {
                route = dbRoute;
            } else if (route != null && route.getNxDdrId() == null && dbRoute != null) {
                route.setNxDdrId(dbRoute.getNxDdrId());
            }
            stops = driverTerminalStopCollector.collectDeliveryStops(
                    compute, driverUserId, disId, effectiveRouteDate);
            if (stops.isEmpty() && route != null && route.getStops() != null) {
                stops = new ArrayList<NxDisRouteStopEntity>(route.getStops());
            }
            if (route != null && (route.getStops() == null || route.getStops().isEmpty()) && !stops.isEmpty()) {
                route.setStops(new ArrayList<NxDisRouteStopEntity>(stops));
                route.setNxDdrStopCount(stops.size());
            }
        } else {
            stops = driverTerminalStopCollector.collectLoadingStops(
                    compute, driverUserId, disId, effectiveRouteDate);
            RouteDispatchReadModelAssembler.linkSharedTaskInstances(mergedPlan, tasks);
            DisRouteSandboxReadModelPartitionHelper.linkConfirmedStopsToTasks(stops, tasks);
            NxDisDriverRouteEntity dbRoute = null;
            if (mergedPlan != null && mergedPlan.getNxDrpId() != null) {
                dbRoute = nxDisDriverRouteDao.queryByPlanAndDriver(
                        mergedPlan.getNxDrpId(), driverUserId);
            }
            route = resolveEnrichedLoadingRoute(mergedPlan, driverUserId, dbRoute);
            enrichLoadingRouteDepartReadModel(mergedPlan, feasibility, driverUserId, route, stops);
            route = resolveEnrichedLoadingRoute(mergedPlan, driverUserId, dbRoute);
        }
        sortStopsBySeq(stops);

        DriverTerminalBuildContext ctx = new DriverTerminalBuildContext();
        ctx.setDisId(disId);
        ctx.setRouteDate(effectiveRouteDate);
        ctx.setRouteDateLabel(DisRouteTemporalHelper.formatRouteDateLabel(effectiveRouteDate, serverNow));
        ctx.setBatchCode(normalizedBatch);
        ctx.setBatchCodeLabel(DisRouteDispatchLabels.label(normalizedBatch));
        ctx.setDriverUserId(driverUserId);
        ctx.setDriverName(driverMeta.getDriverName());
        ctx.setPhase(phase);
        ctx.setServerNow(serverNow);
        ctx.setPlan(mergedPlan);
        ctx.setRoute(route);
        ctx.setStops(stops);
        ctx.setCompute(compute);
        ctx.setFeasibility(feasibility);
        ctx.setDrivers(drivers);
        return ctx;
    }

    private void enrichLoadingRouteDepartReadModel(NxDisRoutePlanEntity plan,
                                                 RouteFeasibilityResult feasibility,
                                                 Integer driverUserId,
                                                 NxDisDriverRouteEntity route,
                                                 List<NxDisRouteStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return;
        }
        List<NxDisRouteStopEntity> routeStops = new ArrayList<NxDisRouteStopEntity>(stops);
        syncLoadingRouteStopsOnPlan(plan, driverUserId, routeStops);
        if (route != null && (route.getStops() == null || route.getStops().isEmpty())) {
            route.setStops(routeStops);
            route.setNxDdrStopCount(routeStops.size());
        }
        if (plan != null) {
            disRouteDispatchOperationPolicy.enrichLoadingDriverRoutesReadModel(plan, feasibility);
        }
        if (route != null && route.getCanDepart() == null) {
            disRouteDispatchOperationPolicy.enrichDriverRouteReadModel(route, plan, feasibility);
        }
    }

    private static void syncLoadingRouteStopsOnPlan(NxDisRoutePlanEntity plan,
                                                  Integer driverUserId,
                                                  List<NxDisRouteStopEntity> stops) {
        if (plan == null || driverUserId == null || stops == null || stops.isEmpty()) {
            return;
        }
        NxDisDriverRouteEntity loadingRoute = findInRoutes(plan.getLoadingDriverRoutes(), driverUserId);
        if (loadingRoute != null && (loadingRoute.getStops() == null || loadingRoute.getStops().isEmpty())) {
            loadingRoute.setStops(new ArrayList<NxDisRouteStopEntity>(stops));
            loadingRoute.setNxDdrStopCount(stops.size());
        }
    }

    /** 优先用 enrich 后的 plan 路线（含 canDepart），并保留 DB routeId。 */
    private static NxDisDriverRouteEntity resolveEnrichedLoadingRoute(NxDisRoutePlanEntity plan,
                                                                      Integer driverUserId,
                                                                      NxDisDriverRouteEntity dbRoute) {
        NxDisDriverRouteEntity route = findRouteByDriver(plan, driverUserId);
        if (route != null && DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
            route = null;
        }
        if (route != null) {
            if (route.getNxDdrId() == null && dbRoute != null) {
                route.setNxDdrId(dbRoute.getNxDdrId());
            }
            return route;
        }
        if (dbRoute != null && !DisRouteRouteExecutionHelper.isExecutionRoute(dbRoute)) {
            return dbRoute;
        }
        return null;
    }

    private static NxDisDriverRouteEntity resolveExecutionRoute(NxDisRoutePlanEntity plan, Integer driverUserId) {
        if (plan == null || driverUserId == null) {
            return null;
        }
        NxDisDriverRouteEntity route = findInRoutes(plan.getExecutionDriverRoutes(), driverUserId);
        if (route != null) {
            return route;
        }
        route = findRouteByDriver(plan, driverUserId);
        if (route != null && DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
            return route;
        }
        return null;
    }

    private static NxDisDriverRouteEntity findRouteByDriver(NxDisRoutePlanEntity plan, Integer driverUserId) {
        if (plan == null || driverUserId == null) {
            return null;
        }
        NxDisDriverRouteEntity route = findInRoutes(plan.getLoadingDriverRoutes(), driverUserId);
        if (route != null) {
            return route;
        }
        route = findInRoutes(plan.getExecutionDriverRoutes(), driverUserId);
        if (route != null) {
            return route;
        }
        return findInRoutes(plan.getDriverRoutes(), driverUserId);
    }

    private static NxDisDriverRouteEntity findInRoutes(List<NxDisDriverRouteEntity> routes, Integer driverUserId) {
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

    private static void sortStopsBySeq(List<NxDisRouteStopEntity> stops) {
        if (stops == null || stops.size() <= 1) {
            return;
        }
        Collections.sort(stops, new java.util.Comparator<NxDisRouteStopEntity>() {
            @Override
            public int compare(NxDisRouteStopEntity a, NxDisRouteStopEntity b) {
                int seqA = resolveStopSeq(a);
                int seqB = resolveStopSeq(b);
                if (seqA != seqB) {
                    return Integer.compare(seqA, seqB);
                }
                Integer idA = a != null && a.getShipmentTask() != null ? a.getShipmentTask().getNxDstId() : null;
                Integer idB = b != null && b.getShipmentTask() != null ? b.getShipmentTask().getNxDstId() : null;
                if (idA != null && idB != null) {
                    return Integer.compare(idA, idB);
                }
                return 0;
            }
        });
    }

    private static int resolveStopSeq(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return Integer.MAX_VALUE;
        }
        if (stop.getNxDrsStopSeq() != null) {
            return stop.getNxDrsStopSeq();
        }
        if (stop.getShipmentTask() != null) {
            if (stop.getShipmentTask().getNxDstManualStopSeq() != null) {
                return stop.getShipmentTask().getNxDstManualStopSeq();
            }
            if (stop.getShipmentTask().getNxDstRouteSeq() != null) {
                return stop.getShipmentTask().getNxDstRouteSeq();
            }
        }
        return Integer.MAX_VALUE;
    }

    private Map<Integer, Integer> buildPurchaseStatusByOrderId(List<NxDisRouteStopEntity> stops) {
        Map<Integer, Integer> purchaseStatusByOrderId = new HashMap<Integer, Integer>();
        if (stops == null) {
            return purchaseStatusByOrderId;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null || stop.getShipmentTask() == null || stop.getShipmentTask().getItems() == null) {
                continue;
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
        return purchaseStatusByOrderId;
    }

    private static DriverDispatchCandidateDto findDriver(DriverDispatchListResponse drivers, Integer driverUserId) {
        if (drivers == null || drivers.getDrivers() == null || driverUserId == null) {
            return null;
        }
        for (DriverDispatchCandidateDto driver : drivers.getDrivers()) {
            if (driver != null && driverUserId.equals(driver.getDriverUserId())) {
                return driver;
            }
        }
        return null;
    }

    private static void validateRequest(Integer disId, Integer driverUserId) {
        if (disId == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        if (driverUserId == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }
    }
}
