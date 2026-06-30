package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisRouteStopDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.route.*;
import com.nongxinle.route.model.GeoPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.nongxinle.route.DisRoutePlanStatus.ASSIGNED;
import static com.nongxinle.route.DisRoutePlanStatus.SIMULATED;

/** compute 内存 mergedPlan 构建与 execution 路线快照 hydration。 */
@Component
public class DisRouteSandboxComputeMergedPlanBuilder {

    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisRouteStopDao nxDisRouteStopDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private DisRouteSandboxSchedulePreviewHelper disRouteSandboxSchedulePreviewHelper;

    public NxDisRoutePlanEntity buildMergedPlan(NxDisRoutePlanEntity planContext,
                                                Integer disId,
                                                String routeDate,
                                                String batchCode,
                                                GeoPoint depot,
                                                List<NxDistributerUserEntity> onDutyDrivers,
                                                List<NxDisRouteStopEntity> confirmedStops,
                                                List<NxDisRouteStopEntity> suggestedStops,
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
        if (onDutyDrivers != null) {
            for (NxDistributerUserEntity driver : onDutyDrivers) {
                NxDisDriverRouteEntity route = new NxDisDriverRouteEntity();
                route.setNxDdrDriverUserId(driver.getNxDistributerUserId());
                route.setNxDdrRouteSeq(routeSeq++);
                route.setDriverName(driver.getNxDiuWxNickName());
                route.setStops(new ArrayList<NxDisRouteStopEntity>());
                route.setNxDdrStopCount(0);
                routeByDriver.put(driver.getNxDistributerUserId(), route);
            }
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

    public void applySchedulePreviewToMergedPlan(NxDisRoutePlanEntity plan, String routeDate) {
        if (plan == null || plan.getDriverRoutes() == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            String scheduleRouteDate = resolveScheduleRouteDate(route, routeDate);
            disRouteSandboxSchedulePreviewHelper.applySchedulePreview(plan, route, scheduleRouteDate);
        }
        DisRouteSandboxPlanTimelineHelper.applyAggregatedTimeline(plan);
    }

    public void applySchedulePreviewToUnassignedStops(List<NxDisRouteStopEntity> unassignedStops,
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

    public void hydrateExecutionRouteSnapshots(NxDisRoutePlanEntity plan) {
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

    public void reconcileExecutionRoutesAfterSnapshot(NxDisRoutePlanEntity plan) {
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
                if (hasSandboxSuggestedStops(existing)
                        && DisRouteSandboxDriverDispatchStateHelper.hasOnlyTerminalActiveTasks(
                        dbRoute, nxDisDriverRouteDao, nxDisShipmentTaskDao)) {
                    if (existing.getDriverName() == null || existing.getDriverName().trim().isEmpty()) {
                        existing.setDriverName(dbRoute.getDriverName());
                    }
                    continue;
                }
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

    private static boolean hasSandboxSuggestedStops(NxDisDriverRouteEntity route) {
        if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
            return false;
        }
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop != null && DisRouteSandboxDispatchEligibilityHelper.isSandboxEphemeralStop(stop)) {
                return true;
            }
        }
        return false;
    }

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
        if (stops == null) {
            return;
        }
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
            boolean ephemeral = DisRouteSandboxDispatchEligibilityHelper.isSandboxEphemeralStop(stop);
            if (ephemeral && !DisRouteSandboxDispatchEligibilityHelper.isDriverEligibleForSandboxDispatch(
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
            if (ephemeral && route != null
                    && !DisRouteSandboxDispatchEligibilityHelper.acceptsSandboxEphemeralStops(
                            route, nxDisDriverRouteDao, nxDisShipmentTaskDao)) {
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
}
