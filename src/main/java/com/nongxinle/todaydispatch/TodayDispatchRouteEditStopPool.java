package com.nongxinle.todaydispatch;

import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dto.route.DisRouteOrderSnapshotDto;
import com.nongxinle.dto.route.SandboxComputeResult;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteOrderArriveDateHelper;
import com.nongxinle.route.DisRouteSandboxDriverDispatchStateHelper;
import com.nongxinle.route.DisRouteSandboxStopKeyUtils;
import com.nongxinle.route.DisRouteSandboxStopSource;
import com.nongxinle.route.DisRouteSandboxUnassignedStopHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisShipmentTaskStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.CLOSED;
import static com.nongxinle.route.DisShipmentTaskStatus.DELIVERED;

/** 路线编辑可编辑客户池（覆盖当前路线、未分配、建议分派及其他司机站点，并支持 eligible 订单兜底）。 */
@Component
public class TodayDispatchRouteEditStopPool {

    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;

    public Map<String, CustomerStopPlan> buildPool(TodayDispatchResult context, Integer driverUserId) {
        Map<String, CustomerStopPlan> pool = new LinkedHashMap<String, CustomerStopPlan>();
        if (context == null) {
            return pool;
        }
        appendPlans(pool, collectAllSandboxCustomerPlans(context));
        SandboxComputeResult compute = context.getCompute();
        if (compute != null) {
            appendEntityStops(pool, context,
                    DisRouteSandboxUnassignedStopHelper.consolidateByDepartment(compute.getUnassignedStops()));
            appendEntityStops(pool, context, compute.getSandboxSuggestedStops());
            appendEntityStops(pool, context, compute.getConfirmedStops());
            appendEntityStops(pool, context, collectDriverBaselineStops(context, driverUserId));
            appendEntityStopsForDriver(pool, context, compute.getLoadingStops(), driverUserId);
            appendEntityStopsForDriver(pool, context, compute.getExecutionStops(), driverUserId);
        }
        return pool;
    }

    /**
     * 司机当前在途/待送基线站点：配送中读 execution 分区 + execution 路线，否则读装车/已确认/建议。
     */
    public List<NxDisRouteStopEntity> collectDriverBaselineStops(TodayDispatchResult context,
                                                                 Integer driverUserId) {
        List<NxDisRouteStopEntity> stops = new ArrayList<NxDisRouteStopEntity>();
        if (context == null || driverUserId == null) {
            return stops;
        }
        SandboxComputeResult compute = context.getCompute();
        if (compute == null) {
            return stops;
        }
        NxDisRoutePlanEntity plan = compute.getMergedPlan();
        boolean inExecution = hasDriverStops(compute.getExecutionStops(), driverUserId)
                || findDriverRoute(plan != null ? plan.getExecutionDriverRoutes() : null, driverUserId) != null;
        if (inExecution) {
            appendRawDriverStops(stops, compute.getExecutionStops(), driverUserId);
            appendActiveStopsFromRouteList(stops, plan != null ? plan.getExecutionDriverRoutes() : null, driverUserId);
        } else {
            appendRawDriverStops(stops, compute.getLoadingStops(), driverUserId);
            appendRawDriverStops(stops, compute.getConfirmedStops(), driverUserId);
            appendRawDriverStops(stops, compute.getSandboxSuggestedStops(), driverUserId);
            appendActiveStopsFromRouteList(stops, plan != null ? plan.getLoadingDriverRoutes() : null, driverUserId);
            appendActiveStopsFromRouteList(stops, plan != null ? plan.getDriverRoutes() : null, driverUserId);
        }
        return dedupeBaselineStopsByDepartment(stops);
    }

    public CustomerStopPlan resolveStop(TodayDispatchResult context,
                                        Integer driverUserId,
                                        String stopKey) {
        if (context == null || stopKey == null || stopKey.trim().isEmpty()) {
            return null;
        }
        CustomerStopPlan stop = buildPool(context, driverUserId).get(stopKey.trim());
        if (stop != null) {
            return stop;
        }
        Integer depId = DisRouteSandboxStopKeyUtils.parseDepFatherId(stopKey);
        if (depId == null) {
            return null;
        }
        stop = findInComputeByDepId(context, driverUserId, depId);
        if (stop != null) {
            return stop;
        }
        return buildFromEligibleOrders(context, depId);
    }

    /** 沙盘内全部待派客户：各司机建议路线 + 未分配，按 dep 去重。 */
    public static List<CustomerStopPlan> collectAllSandboxCustomerPlans(TodayDispatchResult context) {
        Map<Integer, CustomerStopPlan> byDep = new LinkedHashMap<Integer, CustomerStopPlan>();
        if (context == null) {
            return new ArrayList<CustomerStopPlan>();
        }
        if (context.getSuggestedRoutes() != null) {
            for (DriverRoutePlan route : context.getSuggestedRoutes()) {
                if (route == null || route.getStops() == null) {
                    continue;
                }
                for (CustomerStopPlan stop : route.getStops()) {
                    putByDep(byDep, stop);
                }
            }
        }
        if (context.getUnassignedStops() != null) {
            for (CustomerStopPlan stop : context.getUnassignedStops()) {
                putByDep(byDep, stop);
            }
        }
        SandboxComputeResult compute = context.getCompute();
        if (compute != null && compute.getSandboxSuggestedStops() != null) {
            for (NxDisRouteStopEntity stop : compute.getSandboxSuggestedStops()) {
                if (stop == null) {
                    continue;
                }
                Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
                if (depId != null && !byDep.containsKey(depId)) {
                    byDep.put(depId, TodayDispatchComputeService.buildStopPlanForPool(stop, context));
                }
            }
        }
        if (compute != null && compute.getUnassignedStops() != null) {
            List<NxDisRouteStopEntity> consolidated =
                    DisRouteSandboxUnassignedStopHelper.consolidateByDepartment(compute.getUnassignedStops());
            for (NxDisRouteStopEntity stop : consolidated) {
                if (stop == null) {
                    continue;
                }
                Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
                if (depId != null && !byDep.containsKey(depId)) {
                    byDep.put(depId, TodayDispatchComputeService.buildStopPlanForPool(stop, context));
                }
            }
        }
        return new ArrayList<CustomerStopPlan>(byDep.values());
    }

    private CustomerStopPlan findInComputeByDepId(TodayDispatchResult context,
                                                  Integer driverUserId,
                                                  Integer depId) {
        for (CustomerStopPlan stop : collectAllSandboxCustomerPlans(context)) {
            if (stop != null && depId.equals(stop.getDepFatherId())) {
                return stop;
            }
        }
        SandboxComputeResult compute = context.getCompute();
        if (compute == null) {
            return null;
        }
        NxDisRouteStopEntity entity = findEntityByDep(
                DisRouteSandboxUnassignedStopHelper.consolidateByDepartment(compute.getUnassignedStops()), depId);
        if (entity == null) {
            entity = findEntityByDep(compute.getSandboxSuggestedStops(), depId);
        }
        if (entity == null) {
            entity = findEntityByDep(collectDriverBaselineStops(context, driverUserId), depId);
        }
        if (entity == null) {
            return null;
        }
        return TodayDispatchComputeService.buildStopPlanForPool(entity, context);
    }

    private CustomerStopPlan buildFromEligibleOrders(TodayDispatchResult context, Integer depId) {
        if (context == null || context.getDisId() == null || depId == null) {
            return null;
        }
        List<DisRouteOrderSnapshotDto> depOrders = loadEligibleDepOrders(
                context.getDisId(), depId, context.getRouteDate());
        if (depOrders.isEmpty()) {
            return null;
        }
        DisRouteOrderSnapshotDto first = depOrders.get(0);
        NxDisShipmentTaskEntity task = new NxDisShipmentTaskEntity();
        task.setNxDstDistributerId(context.getDisId());
        task.setNxDstRouteDate(context.getRouteDate());
        task.setNxDstDepFatherId(depId);
        task.setNxDstDepName(first.getDepartmentName());
        task.setNxDstLat(first.getLat());
        task.setNxDstLng(first.getLng());
        task.setNxDstAddress(first.getAddress());
        String sandboxStopKey = DisRouteSandboxStopKeyUtils.build(depId);
        task.setSandboxStopKey(sandboxStopKey);
        task.setStopSource(DisRouteSandboxStopSource.UNASSIGNED);
        task.setConfirmViaSandbox(true);

        NxDisRouteStopEntity stop = new NxDisRouteStopEntity();
        stop.setNxDrsDepartmentId(depId);
        stop.setNxDrsDepartmentName(first.getDepartmentName());
        stop.setNxDrsLat(first.getLat());
        stop.setNxDrsLng(first.getLng());
        stop.setNxDrsAddress(first.getAddress());
        stop.setSandboxStopKey(sandboxStopKey);
        stop.setShipmentTask(task);
        stop.setStopSource(DisRouteSandboxStopSource.UNASSIGNED);
        stop.setConfirmViaSandbox(true);
        return TodayDispatchComputeService.buildStopPlanForPool(stop, context);
    }

    private List<DisRouteOrderSnapshotDto> loadEligibleDepOrders(Integer disId,
                                                                 Integer depFatherId,
                                                                 String routeDate) {
        List<DisRouteOrderSnapshotDto> depOrders = filterDepOrders(
                queryEligibleSnapshots(disId, routeDate), depFatherId);
        if (depOrders.isEmpty() && routeDate != null && !routeDate.trim().isEmpty()) {
            depOrders = filterDepOrders(queryEligibleSnapshots(disId, null), depFatherId);
        }
        return depOrders;
    }

    private List<DisRouteOrderSnapshotDto> filterDepOrders(List<DisRouteOrderSnapshotDto> all,
                                                           Integer depFatherId) {
        List<DisRouteOrderSnapshotDto> depOrders = new ArrayList<DisRouteOrderSnapshotDto>();
        if (all == null || depFatherId == null) {
            return depOrders;
        }
        for (DisRouteOrderSnapshotDto row : all) {
            if (depFatherId.equals(row.getDepartmentId())) {
                depOrders.add(row);
            }
        }
        return depOrders;
    }

    private List<DisRouteOrderSnapshotDto> queryEligibleSnapshots(Integer disId, String routeDate) {
        String normalizedRouteDate = routeDate != null && !routeDate.trim().isEmpty()
                ? routeDate.trim() : null;
        String routeDateOnly = DisRouteOrderArriveDateHelper.toRouteDateOnly(normalizedRouteDate);
        return nxDisRoutePlanDao.queryEligibleLiveOrderSnapshots(
                disId, normalizedRouteDate, routeDateOnly, null);
    }

    private static void appendPlans(Map<String, CustomerStopPlan> pool, List<CustomerStopPlan> stops) {
        if (stops == null) {
            return;
        }
        for (CustomerStopPlan stop : stops) {
            putPlan(pool, stop);
        }
    }

    private static void appendEntityStops(Map<String, CustomerStopPlan> pool,
                                          TodayDispatchResult context,
                                          List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            putPlan(pool, TodayDispatchComputeService.buildStopPlanForPool(stop, context));
        }
    }

    private static void appendEntityStopsForDriver(Map<String, CustomerStopPlan> pool,
                                                   TodayDispatchResult context,
                                                   List<NxDisRouteStopEntity> stops,
                                                   Integer driverUserId) {
        if (stops == null || driverUserId == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null || !driverUserId.equals(
                    DisRouteSandboxDriverDispatchStateHelper.resolveStopDriverUserId(stop))) {
                continue;
            }
            putPlan(pool, TodayDispatchComputeService.buildStopPlanForPool(stop, context));
        }
    }

    private static void appendRawDriverStops(List<NxDisRouteStopEntity> target,
                                             List<NxDisRouteStopEntity> source,
                                             Integer driverUserId) {
        if (target == null || source == null || driverUserId == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : source) {
            if (stop == null) {
                continue;
            }
            if (!driverUserId.equals(DisRouteSandboxDriverDispatchStateHelper.resolveStopDriverUserId(stop))) {
                continue;
            }
            if (!isHistoricalCompletedStop(stop)) {
                target.add(stop);
            }
        }
    }

    private static void appendActiveStopsFromRouteList(List<NxDisRouteStopEntity> target,
                                                       List<NxDisDriverRouteEntity> routes,
                                                       Integer driverUserId) {
        NxDisDriverRouteEntity route = findDriverRoute(routes, driverUserId);
        if (route == null || route.getStops() == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop != null && !isHistoricalCompletedStop(stop)) {
                target.add(stop);
            }
        }
    }

    private static NxDisDriverRouteEntity findDriverRoute(List<NxDisDriverRouteEntity> routes,
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

    private static boolean hasDriverStops(List<NxDisRouteStopEntity> stops, Integer driverUserId) {
        if (stops == null || driverUserId == null) {
            return false;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop != null && driverUserId.equals(
                    DisRouteSandboxDriverDispatchStateHelper.resolveStopDriverUserId(stop))) {
                return true;
            }
        }
        return false;
    }

    private static List<NxDisRouteStopEntity> dedupeBaselineStopsByDepartment(List<NxDisRouteStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, NxDisRouteStopEntity> byDep = new LinkedHashMap<Integer, NxDisRouteStopEntity>();
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            if (depId != null) {
                byDep.put(depId, stop);
            }
        }
        List<NxDisRouteStopEntity> deduped = new ArrayList<NxDisRouteStopEntity>(byDep.values());
        Collections.sort(deduped, new Comparator<NxDisRouteStopEntity>() {
            @Override
            public int compare(NxDisRouteStopEntity a, NxDisRouteStopEntity b) {
                int seqA = a != null && a.getNxDrsStopSeq() != null ? a.getNxDrsStopSeq() : 0;
                int seqB = b != null && b.getNxDrsStopSeq() != null ? b.getNxDrsStopSeq() : 0;
                return Integer.compare(seqA, seqB);
            }
        });
        return deduped;
    }

    private static boolean isHistoricalCompletedStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return true;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task == null || task.getNxDstStatus() == null) {
            return false;
        }
        String status = task.getNxDstStatus().trim().toUpperCase();
        return DELIVERED.equals(status) || CLOSED.equals(status) || CANCELLED.equals(status);
    }

    private static void putPlan(Map<String, CustomerStopPlan> pool, CustomerStopPlan stop) {
        if (stop == null) {
            return;
        }
        String key = TodayDispatchComputeService.resolvePlanStopKey(stop);
        if (key != null && !pool.containsKey(key)) {
            pool.put(key, stop);
        }
    }

    private static void putByDep(Map<Integer, CustomerStopPlan> byDep, CustomerStopPlan stop) {
        if (stop == null || stop.getDepFatherId() == null) {
            return;
        }
        if (!byDep.containsKey(stop.getDepFatherId())) {
            byDep.put(stop.getDepFatherId(), stop);
        }
    }

    private static NxDisRouteStopEntity findEntityByDep(List<NxDisRouteStopEntity> stops, Integer depId) {
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

}
