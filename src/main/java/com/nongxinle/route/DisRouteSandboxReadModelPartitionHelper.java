package com.nongxinle.route;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisRouteSandboxStopSource.CONFIRMED;
import static com.nongxinle.route.DisRouteSandboxStopSource.SANDBOX_SUGGESTED;
import static com.nongxinle.route.DisShipmentTaskStatus.ASSIGNED;
import static com.nongxinle.route.DisShipmentTaskStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.CLOSED;
import static com.nongxinle.route.DisShipmentTaskStatus.DELIVERED;
import static com.nongxinle.route.DisShipmentTaskStatus.EXCEPTION;
import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;
import static com.nongxinle.route.DisShipmentTaskStatus.READY_TO_GO;
import static com.nongxinle.route.DisShipmentTaskStatus.SIMULATED;
import static com.nongxinle.route.DisShipmentTaskStatus.UNASSIGNED;

/**
 * Phase 3D+：读模型三阶段分层 — 今日派车沙箱 / 司机装车 / 配送执行。
 */
public final class DisRouteSandboxReadModelPartitionHelper {

    public static final String ROUTE_SCOPE_SANDBOX = "SANDBOX";
    public static final String ROUTE_SCOPE_LOADING = "LOADING";
    public static final String ROUTE_SCOPE_EXECUTION = "EXECUTION";
    public static final String STOP_SCOPE_SANDBOX = "SANDBOX";
    public static final String STOP_SCOPE_LOADING = "LOADING";
    public static final String STOP_SCOPE_EXECUTION = "EXECUTION";

    private DisRouteSandboxReadModelPartitionHelper() {
    }

    /**
     * 将 plan.driverRoutes 拆为 sandbox / loading / execution 三路。
     * 同一 DB route 若同时含待确认与已确认站点，会拆成两条读模型路线。
     */
    public static void partitionPlanRoutes(NxDisRoutePlanEntity plan) {
        if (plan == null) {
            return;
        }
        if (plan.getDriverRoutes() == null) {
            plan.setDriverRoutes(Collections.<NxDisDriverRouteEntity>emptyList());
            plan.setLoadingDriverRoutes(Collections.<NxDisDriverRouteEntity>emptyList());
            plan.setExecutionDriverRoutes(Collections.<NxDisDriverRouteEntity>emptyList());
            return;
        }
        List<NxDisDriverRouteEntity> sandboxRoutes = new ArrayList<NxDisDriverRouteEntity>();
        List<NxDisDriverRouteEntity> loadingRoutes = new ArrayList<NxDisDriverRouteEntity>();
        List<NxDisDriverRouteEntity> executionRoutes = new ArrayList<NxDisDriverRouteEntity>();

        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            if (route == null) {
                continue;
            }
            ensureRouteMetricsCanonical(route);
            if (DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
                List<NxDisRouteStopEntity> peeled = peelSandboxEphemeralStops(route);
                boolean completedReassignable = route.getStops() != null && !route.getStops().isEmpty();
                if (completedReassignable) {
                    for (NxDisRouteStopEntity remainingStop : route.getStops()) {
                        NxDisShipmentTaskEntity task = remainingStop != null
                                ? remainingStop.getShipmentTask() : null;
                        String status = task != null ? task.getNxDstStatus() : null;
                        if (!DELIVERED.equals(status) && !CLOSED.equals(status) && !CANCELLED.equals(status)) {
                            completedReassignable = false;
                            break;
                        }
                    }
                }
                applyExecutionRouteReadOnlyOverlay(route);
                executionRoutes.add(route);
                if (completedReassignable && !peeled.isEmpty()) {
                    sandboxRoutes.add(buildFreshSandboxRoute(route, peeled));
                }
                continue;
            }
            boolean routeInLoadingPhase = DisRouteLoadingGateHelper.isRouteEnteredLoading(route);

            List<NxDisRouteStopEntity> sandboxStops = new ArrayList<NxDisRouteStopEntity>();
            List<NxDisRouteStopEntity> loadingStops = new ArrayList<NxDisRouteStopEntity>();
            List<NxDisRouteStopEntity> executionStopsOnRoute = new ArrayList<NxDisRouteStopEntity>();
            if (route.getStops() != null) {
                for (NxDisRouteStopEntity stop : route.getStops()) {
                    if (stop == null) {
                        continue;
                    }
                    if (isExecutionStop(stop)) {
                        applyExecutionStopReadOnlyOverlay(stop);
                        executionStopsOnRoute.add(stop);
                    } else if (isLoadingScopeStop(stop, route)) {
                        applyLoadingStopScope(stop);
                        loadingStops.add(stop);
                    } else {
                        if (routeInLoadingPhase && isEphemeralSandboxStop(stop)) {
                            continue;
                        }
                        if (stop.getStopSource() == null) {
                            stop.setStopSource(isEphemeralSandboxStop(stop)
                                    ? SANDBOX_SUGGESTED : UNASSIGNED);
                        }
                        stop.setStopScope(STOP_SCOPE_SANDBOX);
                        sandboxStops.add(stop);
                    }
                }
            }

            if (!executionStopsOnRoute.isEmpty()) {
                NxDisDriverRouteEntity executionRoute = copyRouteWithStops(route, executionStopsOnRoute);
                applyExecutionRouteReadOnlyOverlay(executionRoute);
                mergeExecutionRoute(executionRoutes, executionRoute);
            }
            if (!loadingStops.isEmpty()) {
                NxDisDriverRouteEntity loadingRoute = copyRouteWithStops(route, loadingStops);
                applyLoadingRouteScope(loadingRoute);
                loadingRoutes.add(loadingRoute);
            }
            if (!routeInLoadingPhase && !sandboxStops.isEmpty()) {
                NxDisDriverRouteEntity sandboxRoute = copyRouteWithStops(route, sandboxStops);
                sandboxRoute.setRouteScope(ROUTE_SCOPE_SANDBOX);
                sandboxRoute.setSandboxEligible(true);
                sandboxRoutes.add(sandboxRoute);
            }
        }

        plan.setDriverRoutes(sandboxRoutes);
        plan.setLoadingDriverRoutes(loadingRoutes);
        plan.setExecutionDriverRoutes(executionRoutes);
    }

    private static NxDisDriverRouteEntity buildFreshSandboxRoute(NxDisDriverRouteEntity source,
                                                                  List<NxDisRouteStopEntity> stops) {
        NxDisDriverRouteEntity sandboxRoute = new NxDisDriverRouteEntity();
        sandboxRoute.setNxDdrDriverUserId(source.getNxDdrDriverUserId());
        sandboxRoute.setNxDdrRouteSeq(source.getNxDdrRouteSeq());
        sandboxRoute.setDriverName(source.getDriverName());
        sandboxRoute.setDriverPhone(source.getDriverPhone());
        sandboxRoute.setStops(new ArrayList<NxDisRouteStopEntity>(stops));
        sandboxRoute.setNxDdrStopCount(stops.size());
        sandboxRoute.setTotalStopCount(stops.size());
        sandboxRoute.setSuggestedStopCount(stops.size());
        sandboxRoute.setConfirmedStopCount(0);
        sandboxRoute.setRouteScope(ROUTE_SCOPE_SANDBOX);
        sandboxRoute.setSandboxEligible(true);
        for (NxDisRouteStopEntity stop : stops) {
            if (stop != null) {
                stop.setStopScope(STOP_SCOPE_SANDBOX);
            }
        }
        return sandboxRoute;
    }

    private static void mergeExecutionRoute(List<NxDisDriverRouteEntity> executionRoutes,
                                            NxDisDriverRouteEntity incoming) {
        if (incoming == null) {
            return;
        }
        if (executionRoutes == null) {
            return;
        }
        for (NxDisDriverRouteEntity existing : executionRoutes) {
            if (existing == null || incoming.getNxDdrDriverUserId() == null) {
                continue;
            }
            boolean sameRoute = incoming.getNxDdrDriverUserId().equals(existing.getNxDdrDriverUserId())
                    && (incoming.getNxDdrId() == null || incoming.getNxDdrId().equals(existing.getNxDdrId()));
            if (!sameRoute) {
                continue;
            }
            if (existing.getStops() == null) {
                existing.setStops(new ArrayList<NxDisRouteStopEntity>());
            }
            java.util.Set<Integer> seenTaskIds = new java.util.HashSet<Integer>();
            for (NxDisRouteStopEntity stop : existing.getStops()) {
                if (stop != null && stop.getShipmentTask() != null && stop.getShipmentTask().getNxDstId() != null) {
                    seenTaskIds.add(stop.getShipmentTask().getNxDstId());
                }
            }
            if (incoming.getStops() != null) {
                for (NxDisRouteStopEntity stop : incoming.getStops()) {
                    if (stop == null || stop.getShipmentTask() == null
                            || stop.getShipmentTask().getNxDstId() == null) {
                        existing.getStops().add(stop);
                        continue;
                    }
                    if (seenTaskIds.add(stop.getShipmentTask().getNxDstId())) {
                        existing.getStops().add(stop);
                    }
                }
            }
            existing.setNxDdrStopCount(existing.getStops().size());
            existing.setTotalStopCount(existing.getStops().size());
            existing.setConfirmedStopCount(existing.getStops().size());
            ensureRouteMetricsCanonical(existing);
            DisRouteRouteExecutionHelper.syncExecutionCanonicalFields(existing);
            return;
        }
        executionRoutes.add(incoming);
    }

    public static StopPartition partitionConfirmedStops(List<NxDisRouteStopEntity> confirmedStops) {
        return partitionConfirmedStops(confirmedStops, null);
    }

    public static Map<Integer, NxDisDriverRouteEntity> buildRouteIndex(NxDisRoutePlanEntity plan) {
        Map<Integer, NxDisDriverRouteEntity> routeById = new HashMap<Integer, NxDisDriverRouteEntity>();
        if (plan == null || plan.getDriverRoutes() == null) {
            return routeById;
        }
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            if (route != null && route.getNxDdrId() != null) {
                routeById.put(route.getNxDdrId(), route);
            }
        }
        return routeById;
    }

    public static StopPartition partitionConfirmedStops(List<NxDisRouteStopEntity> confirmedStops,
                                                        Map<Integer, NxDisDriverRouteEntity> routeById) {
        StopPartition partition = new StopPartition();
        if (confirmedStops == null || confirmedStops.isEmpty()) {
            return partition;
        }
        for (NxDisRouteStopEntity stop : confirmedStops) {
            if (stop == null) {
                continue;
            }
            NxDisDriverRouteEntity route = resolveRouteForStop(stop, routeById);
            if (isExecutionStop(stop)) {
                applyExecutionStopReadOnlyOverlay(stop);
                partition.executionStops.add(stop);
            } else if (DisRouteLoadingGateHelper.isLoadingScopeStop(stop, route)) {
                applyLoadingStopScope(stop);
                partition.loadingStops.add(stop);
            } else {
                stop.setStopScope(STOP_SCOPE_SANDBOX);
                partition.sandboxStops.add(stop);
            }
        }
        return partition;
    }

    private static NxDisDriverRouteEntity resolveRouteForStop(NxDisRouteStopEntity stop,
                                                              Map<Integer, NxDisDriverRouteEntity> routeById) {
        if (stop == null || routeById == null || routeById.isEmpty()) {
            return null;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && task.getNxDstDriverRouteId() != null) {
            return routeById.get(task.getNxDstDriverRouteId());
        }
        return null;
    }

    public static void linkConfirmedStopsToTasks(List<NxDisRouteStopEntity> confirmedStops,
                                                 List<NxDisShipmentTaskEntity> tasks) {
        if (confirmedStops == null || confirmedStops.isEmpty() || tasks == null || tasks.isEmpty()) {
            return;
        }
        java.util.Map<Integer, NxDisShipmentTaskEntity> taskById =
                new java.util.HashMap<Integer, NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task != null && task.getNxDstId() != null) {
                taskById.put(task.getNxDstId(), task);
            }
        }
        for (NxDisRouteStopEntity stop : confirmedStops) {
            if (stop == null) {
                continue;
            }
            Integer taskId = stop.getNxDrsShipmentTaskId();
            if (taskId == null && stop.getShipmentTask() != null) {
                taskId = stop.getShipmentTask().getNxDstId();
            }
            if (taskId == null) {
                continue;
            }
            NxDisShipmentTaskEntity shared = taskById.get(taskId);
            if (shared != null) {
                stop.setShipmentTask(shared);
            }
        }
    }

    public static boolean hasSandboxOperableRoutes(NxDisRoutePlanEntity plan) {
        return plan != null && plan.getDriverRoutes() != null && !plan.getDriverRoutes().isEmpty();
    }

    public static boolean hasLoadingRoutes(NxDisRoutePlanEntity plan) {
        return plan != null && plan.getLoadingDriverRoutes() != null
                && !plan.getLoadingDriverRoutes().isEmpty();
    }

    public static boolean hasExecutionRoutes(NxDisRoutePlanEntity plan) {
        return plan != null && plan.getExecutionDriverRoutes() != null
                && !plan.getExecutionDriverRoutes().isEmpty();
    }

    public static void applyExecutionRouteReadOnlyOverlay(NxDisDriverRouteEntity route) {
        if (route == null) {
            return;
        }
        DisRouteRouteExecutionHelper.syncExecutionCanonicalFields(route);
        route.setRouteScope(ROUTE_SCOPE_EXECUTION);
        route.setSandboxEligible(false);
        route.setCanLoad(false);
        route.setLoadBlockedReason("司机已出发，路线为配送执行态");
        route.setCanAssignMore(false);
        route.setAssignBlockedReason("司机已出发，不能改派");
        route.setCanDepart(false);
        route.setDepartBlockedReason("该司机路线已在配送中或已完成");
        if (route.getStops() != null) {
            for (NxDisRouteStopEntity stop : route.getStops()) {
                applyExecutionStopReadOnlyOverlay(stop);
            }
        }
    }

    private static void applyLoadingRouteScope(NxDisDriverRouteEntity route) {
        if (route == null) {
            return;
        }
        route.setRouteScope(ROUTE_SCOPE_LOADING);
        route.setSandboxEligible(false);
        if (route.getStops() != null) {
            for (NxDisRouteStopEntity stop : route.getStops()) {
                applyLoadingStopScope(stop);
            }
        }
    }

    private static void applyLoadingStopScope(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return;
        }
        stop.setStopScope(STOP_SCOPE_LOADING);
        stop.setStopSource(CONFIRMED);
        stop.setCanConfirmCustomer(false);
        stop.setConfirmCustomerBlockedReason("请通过编辑路线确认分派");
    }

    public static void applyExecutionStopReadOnlyOverlay(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return;
        }
        stop.setStopScope(STOP_SCOPE_EXECUTION);
        stop.setCanAssign(false);
        stop.setAssignBlockedReason("司机已出发，不能改派");
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        stop.setCanConfirmLoad(false);
        stop.setConfirmLoadBlockedReason(resolveExecutionConfirmLoadBlockedReason(task));
        stop.setCanMove(false);
        stop.setMoveBlockedReason("司机已出发，不能改派");
        stop.setCanUnlock(false);
        stop.setUnlockBlockedReason("司机已出发，不能解锁");
        stop.setCanConfirmCustomer(false);
        stop.setConfirmCustomerBlockedReason("请通过编辑路线确认分派");
        if (task != null) {
            task.setCanAssign(false);
            task.setAssignBlockedReason("司机已出发，不能改派");
            task.setCanConfirmLoad(false);
            task.setConfirmLoadBlockedReason(resolveExecutionConfirmLoadBlockedReason(task));
            task.setCanMove(false);
            task.setMoveBlockedReason("司机已出发，不能改派");
            task.setCanUnlock(false);
            task.setUnlockBlockedReason("司机已出发，不能解锁");
            task.setCanConfirmCustomer(false);
            task.setConfirmCustomerBlockedReason("请通过编辑路线确认分派");
        }
    }

    /** strict contract：totalDistanceM / totalDurationS 必须存在；优先 DB/快照，否则从 leg 汇总。 */
    public static void ensureRouteMetricsCanonical(NxDisDriverRouteEntity route) {
        if (route == null) {
            return;
        }
        if (route.getNxDdrStopCount() == null || route.getNxDdrStopCount() <= 0) {
            int stopCount = DisRouteExecutionRouteSnapshotHelper.resolveEffectiveStopCount(route);
            if (stopCount > 0) {
                route.setNxDdrStopCount(stopCount);
            }
        }
        Long totalDistance = route.getNxDdrTotalDistanceM();
        Long totalDuration = route.getNxDdrTotalDurationS();
        if ((totalDistance == null || totalDistance <= 0) && route.getStops() != null) {
            long legDistance = 0L;
            long legDuration = 0L;
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop == null) {
                    continue;
                }
                if (stop.getNxDrsLegDistanceM() != null && stop.getNxDrsLegDistanceM() > 0) {
                    legDistance += stop.getNxDrsLegDistanceM();
                }
                if (stop.getNxDrsLegDurationS() != null && stop.getNxDrsLegDurationS() > 0) {
                    legDuration += stop.getNxDrsLegDurationS();
                }
            }
            if (route.getReturnLegDistanceM() != null && route.getReturnLegDistanceM() > 0) {
                legDistance += route.getReturnLegDistanceM();
            }
            if (route.getReturnLegDurationS() != null && route.getReturnLegDurationS() > 0) {
                legDuration += route.getReturnLegDurationS();
            }
            if (legDistance > 0) {
                totalDistance = legDistance;
            }
            if (legDuration > 0 && (totalDuration == null || totalDuration <= 0)) {
                totalDuration = legDuration;
            }
        }
        route.setNxDdrTotalDistanceM(totalDistance != null && totalDistance > 0 ? totalDistance : 0L);
        route.setNxDdrTotalDurationS(totalDuration != null && totalDuration > 0 ? totalDuration : 0L);
    }

    private static NxDisDriverRouteEntity copyRouteWithStops(NxDisDriverRouteEntity source,
                                                             List<NxDisRouteStopEntity> stops) {
        NxDisDriverRouteEntity copy = new NxDisDriverRouteEntity();
        copy.setNxDdrId(source.getNxDdrId());
        copy.setNxDdrPlanId(source.getNxDdrPlanId());
        copy.setNxDdrDriverUserId(source.getNxDdrDriverUserId());
        copy.setNxDdrRouteSeq(source.getNxDdrRouteSeq());
        copy.setNxDdrRouteStatus(source.getNxDdrRouteStatus());
        copy.setNxDdrActualDepartAt(source.getNxDdrActualDepartAt());
        copy.setNxDdrPlannedDepartAt(source.getNxDdrPlannedDepartAt());
        copy.setNxDdrLoadingEnteredAt(source.getNxDdrLoadingEnteredAt());
        copy.setNxDdrLoadingEnteredOperatorUserId(source.getNxDdrLoadingEnteredOperatorUserId());
        copy.setNxDdrPlannedFinishAt(source.getNxDdrPlannedFinishAt());
        copy.setNxDdrDispatchEligible(source.getNxDdrDispatchEligible());
        copy.setNxDdrIneligibleReason(source.getNxDdrIneligibleReason());
        copy.setNxDdrFeasibilityStatus(source.getNxDdrFeasibilityStatus());
        copy.setDriverName(source.getDriverName());
        copy.setDriverPhone(source.getDriverPhone());
        copy.setRouteDate(source.getRouteDate());
        copy.setDispatchDate(source.getDispatchDate());
        copy.setCanLoad(source.getCanLoad());
        copy.setLoadBlockedReason(source.getLoadBlockedReason());
        copy.setCanAssignMore(source.getCanAssignMore());
        copy.setAssignBlockedReason(source.getAssignBlockedReason());
        copy.setRouteOperationStatusLabel(source.getRouteOperationStatusLabel());
        copy.setScheduleMode(source.getScheduleMode());
        copy.setScheduleModeLabel(source.getScheduleModeLabel());
        copy.setRouteScheduleSummaryLabel(source.getRouteScheduleSummaryLabel());
        copy.setPlannedDepartLabel(source.getPlannedDepartLabel());
        copy.setPlannedFinishLabel(source.getPlannedFinishLabel());
        copy.setPlannedReturnAt(source.getPlannedReturnAt());
        copy.setPlannedReturnLabel(source.getPlannedReturnLabel());
        copy.setReturnLegDistanceM(source.getReturnLegDistanceM());
        copy.setReturnLegDurationS(source.getReturnLegDurationS());
        copy.setReturnLegLabel(source.getReturnLegLabel());
        copy.setRouteStatus(source.getRouteStatus());
        copy.setRouteStatusLabel(source.getRouteStatusLabel());
        copy.setCanDepart(source.getCanDepart());
        copy.setDepartActionLabel(source.getDepartActionLabel());
        copy.setDepartBlockedReason(source.getDepartBlockedReason());
        copy.setDepartConfirmMessage(source.getDepartConfirmMessage());
        copy.setDepartWarning(source.getDepartWarning());
        copy.setUnprintedBillCount(source.getUnprintedBillCount());
        copy.setStops(stops);
        copy.setNxDdrStopCount(stops != null ? stops.size() : 0);
        copy.setTotalStopCount(stops != null ? stops.size() : 0);
        copy.setConfirmedStopCount(stops != null ? stops.size() : 0);
        ensureRouteMetricsCanonical(copy);
        return copy;
    }

    private static List<NxDisRouteStopEntity> peelSandboxEphemeralStops(NxDisDriverRouteEntity route) {
        List<NxDisRouteStopEntity> peeled = new ArrayList<NxDisRouteStopEntity>();
        if (route == null || route.getStops() == null) {
            return peeled;
        }
        List<NxDisRouteStopEntity> remaining = new ArrayList<NxDisRouteStopEntity>();
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop != null && DisRouteSandboxDispatchEligibilityHelper.isSandboxEphemeralStop(stop)) {
                stop.setStopScope(STOP_SCOPE_SANDBOX);
                peeled.add(stop);
            } else if (stop != null) {
                remaining.add(stop);
            }
        }
        route.setStops(remaining);
        if (!remaining.isEmpty()) {
            route.setNxDdrStopCount(remaining.size());
            route.setTotalStopCount(remaining.size());
        } else {
            route.setNxDdrStopCount(0);
            route.setTotalStopCount(0);
            route.setConfirmedStopCount(0);
        }
        return peeled;
    }

    private static boolean isEphemeralSandboxStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null) {
            String status = task.getNxDstStatus();
            return SIMULATED.equals(status) || UNASSIGNED.equals(status);
        }
        return stop.getNxDrsShipmentTaskId() == null;
    }

    private static boolean isLoadingScopeStop(NxDisRouteStopEntity stop, NxDisDriverRouteEntity route) {
        return DisRouteLoadingGateHelper.isLoadingScopeStop(stop, route);
    }

    private static boolean isExecutionStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && CANCELLED.equals(task.getNxDstStatus())) {
            return false;
        }
        if (task != null && (IN_DELIVERY.equals(task.getNxDstStatus())
                || DELIVERED.equals(task.getNxDstStatus())
                || EXCEPTION.equals(task.getNxDstStatus()))) {
            return true;
        }
        return false;
    }

    private static String resolveExecutionConfirmLoadBlockedReason(NxDisShipmentTaskEntity task) {
        if (task == null || task.getNxDstStatus() == null) {
            return "司机已出发，不能装车";
        }
        if (DELIVERED.equals(task.getNxDstStatus())) {
            return "已送达";
        }
        if (EXCEPTION.equals(task.getNxDstStatus())) {
            return "配送异常";
        }
        if (IN_DELIVERY.equals(task.getNxDstStatus())) {
            return "已出发";
        }
        return "司机已出发，不能装车";
    }

    public static final class StopPartition {
        public final List<NxDisRouteStopEntity> sandboxStops = new ArrayList<NxDisRouteStopEntity>();
        public final List<NxDisRouteStopEntity> loadingStops = new ArrayList<NxDisRouteStopEntity>();
        public final List<NxDisRouteStopEntity> executionStops = new ArrayList<NxDisRouteStopEntity>();
    }
}
