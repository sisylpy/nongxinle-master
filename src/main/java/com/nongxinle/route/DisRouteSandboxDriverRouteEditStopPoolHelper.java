package com.nongxinle.route;

import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dto.route.DisRouteOrderSnapshotDto;
import com.nongxinle.dto.route.SandboxComputeResult;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 司机路线编辑可编辑客户池：覆盖当前路线、未分配、计划路线站点，并支持 eligible 订单兜底。
 */
@Component
public class DisRouteSandboxDriverRouteEditStopPoolHelper {

    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;

    public Map<String, NxDisRouteStopEntity> buildPool(DriverRouteEditBuildContext ctx) {
        Map<String, NxDisRouteStopEntity> pool = new LinkedHashMap<String, NxDisRouteStopEntity>();
        if (ctx == null) {
            return pool;
        }
        appendPool(pool, ctx.getInitialBaselineStops());
        appendPool(pool, ctx.getBaselineStops());
        appendPool(pool, ctx.getAvailableStops());

        SandboxComputeResult compute = ctx.getCompute();
        if (compute != null) {
            appendPool(pool, DisRouteSandboxManualDispatchPanoramaHelper.collectDriverBaselineStops(
                    compute, ctx.getDriverUserId(), ctx.getDispatchStage()));
            appendPool(pool, DisRouteSandboxUnassignedStopHelper.consolidateByDepartment(
                    compute.getUnassignedStops()));
            appendPoolForDriver(pool, compute.getConfirmedStops(), ctx.getDriverUserId());
            appendPoolForDriver(pool, compute.getLoadingStops(), ctx.getDriverUserId());
            appendPoolForDriver(pool, compute.getSandboxSuggestedStops(), ctx.getDriverUserId());
        }
        NxDisDriverRouteEntity route = ctx.getRoute();
        if (route != null && route.getStops() != null) {
            appendPool(pool, route.getStops());
        }
        return pool;
    }

    public NxDisRouteStopEntity resolveStop(DriverRouteEditBuildContext ctx, String stopKey) {
        if (ctx == null || stopKey == null || stopKey.trim().isEmpty()) {
            return null;
        }
        NxDisRouteStopEntity stop = buildPool(ctx).get(stopKey.trim());
        if (stop != null) {
            return stop;
        }
        Integer depId = DisRouteSandboxStopKeyUtils.parseDepFatherId(stopKey);
        if (depId == null) {
            return null;
        }
        stop = findInComputeByDepId(ctx, depId);
        if (stop != null) {
            return stop;
        }
        return buildFromEligibleOrders(ctx, depId);
    }

    private NxDisRouteStopEntity findInComputeByDepId(DriverRouteEditBuildContext ctx, Integer depId) {
        SandboxComputeResult compute = ctx.getCompute();
        if (compute == null || depId == null) {
            return null;
        }
        Integer confirmedDriver = ctx.getConfirmedDriverByDep() != null
                ? ctx.getConfirmedDriverByDep().get(depId) : null;
        if (confirmedDriver != null && !confirmedDriver.equals(ctx.getDriverUserId())) {
            return null;
        }

        NxDisRouteStopEntity fromUnassigned = findStopByDep(
                DisRouteSandboxUnassignedStopHelper.consolidateByDepartment(compute.getUnassignedStops()), depId);
        if (fromUnassigned != null) {
            return fromUnassigned;
        }

        NxDisRouteStopEntity fromDriver = findStopByDep(
                DisRouteSandboxManualDispatchPanoramaHelper.collectDriverBaselineStops(
                        compute, ctx.getDriverUserId(), ctx.getDispatchStage()), depId);
        if (fromDriver != null) {
            return fromDriver;
        }

        NxDisRouteStopEntity suggestedOther = findSuggestedStopForOtherDriver(compute, depId, ctx.getDriverUserId());
        if (suggestedOther != null) {
            return null;
        }
        return null;
    }

    private static NxDisRouteStopEntity findSuggestedStopForOtherDriver(SandboxComputeResult compute,
                                                                        Integer depId,
                                                                        Integer driverUserId) {
        if (compute == null || depId == null || driverUserId == null) {
            return null;
        }
        NxDisRouteStopEntity hit = findAssignedStopForDep(compute.getSandboxSuggestedStops(), depId, driverUserId);
        if (hit != null) {
            return hit;
        }
        return findAssignedStopForDep(compute.getConfirmedStops(), depId, driverUserId);
    }

    private static NxDisRouteStopEntity findAssignedStopForDep(List<NxDisRouteStopEntity> stops,
                                                               Integer depId,
                                                               Integer currentDriverUserId) {
        if (stops == null || depId == null) {
            return null;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null || !depId.equals(DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop))) {
                continue;
            }
            Integer stopDriverId = resolveStopDriverUserId(stop);
            if (stopDriverId != null && !stopDriverId.equals(currentDriverUserId)) {
                return stop;
            }
        }
        return null;
    }

    private NxDisRouteStopEntity buildFromEligibleOrders(DriverRouteEditBuildContext ctx, Integer depId) {
        if (ctx == null || ctx.getDisId() == null || depId == null) {
            return null;
        }
        Integer confirmedDriver = ctx.getConfirmedDriverByDep() != null
                ? ctx.getConfirmedDriverByDep().get(depId) : null;
        if (confirmedDriver != null && !confirmedDriver.equals(ctx.getDriverUserId())) {
            return null;
        }
        List<DisRouteOrderSnapshotDto> depOrders = loadEligibleDepOrders(ctx.getDisId(), depId, ctx.getRouteDate());
        if (depOrders.isEmpty()) {
            return null;
        }
        DisRouteOrderSnapshotDto first = depOrders.get(0);
        NxDisShipmentTaskEntity task = new NxDisShipmentTaskEntity();
        task.setNxDstDistributerId(ctx.getDisId());
        task.setNxDstRouteDate(ctx.getRouteDate());
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
        return stop;
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

    private static void appendPool(Map<String, NxDisRouteStopEntity> pool, List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            String key = DisRouteSandboxStopKeyUtils.build(depId);
            if (key != null && !pool.containsKey(key)) {
                pool.put(key, stop);
            }
        }
    }

    private static void appendPoolForDriver(Map<String, NxDisRouteStopEntity> pool,
                                            List<NxDisRouteStopEntity> stops,
                                            Integer driverUserId) {
        if (stops == null || driverUserId == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            if (!driverUserId.equals(resolveStopDriverUserId(stop))) {
                continue;
            }
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            String key = DisRouteSandboxStopKeyUtils.build(depId);
            if (key != null && !pool.containsKey(key)) {
                pool.put(key, stop);
            }
        }
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

    private static Integer resolveStopDriverUserId(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
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
}
