package com.nongxinle.service.impl;

import com.nongxinle.config.DisRouteDispatchSettings;
import com.nongxinle.dao.NxDisDriverDutyDao;
import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dao.NxDistributerUserDao;
import com.nongxinle.dto.route.RouteDispatchOperationDecision;
import com.nongxinle.dto.route.RouteFeasibilityResult;
import com.nongxinle.entity.*;
import com.nongxinle.route.*;
import com.nongxinle.service.DisRouteDispatchOperationPolicy;
import com.nongxinle.service.DisRouteFeasibilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;
import static com.nongxinle.route.DisShipmentTaskItemStatus.ACTIVE;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisUserDriver;

@Service
public class DisRouteDispatchOperationPolicyImpl implements DisRouteDispatchOperationPolicy {

    private static final Set<String> ASSIGN_REJECT = new HashSet<String>();
    private static final Set<String> MOVE_REJECT = new HashSet<String>();
    private static final Set<String> UNLOCK_REJECT = new HashSet<String>();
    private static final Set<String> LOAD_REJECT = new HashSet<String>();

    static {
        ASSIGN_REJECT.add(DisShipmentTaskStatus.READY_TO_GO);
        ASSIGN_REJECT.add(DisShipmentTaskStatus.IN_DELIVERY);
        ASSIGN_REJECT.add(DisShipmentTaskStatus.DELIVERED);
        ASSIGN_REJECT.add(DisShipmentTaskStatus.EXCEPTION);
        ASSIGN_REJECT.add(DisShipmentTaskStatus.CANCELLED);
        ASSIGN_REJECT.add(DisShipmentTaskStatus.CLOSED);

        MOVE_REJECT.add(DisShipmentTaskStatus.IN_DELIVERY);
        MOVE_REJECT.add(DisShipmentTaskStatus.DELIVERED);
        MOVE_REJECT.add(DisShipmentTaskStatus.EXCEPTION);
        MOVE_REJECT.add(DisShipmentTaskStatus.CANCELLED);
        MOVE_REJECT.add(DisShipmentTaskStatus.CLOSED);

        UNLOCK_REJECT.add(DisShipmentTaskStatus.IN_DELIVERY);
        UNLOCK_REJECT.add(DisShipmentTaskStatus.DELIVERED);
        UNLOCK_REJECT.add(DisShipmentTaskStatus.EXCEPTION);
        UNLOCK_REJECT.add(DisShipmentTaskStatus.CANCELLED);
        UNLOCK_REJECT.add(DisShipmentTaskStatus.CLOSED);
        UNLOCK_REJECT.add(DisShipmentTaskStatus.READY_TO_GO);

        LOAD_REJECT.add(DisShipmentTaskStatus.SIMULATED);
        LOAD_REJECT.add(DisShipmentTaskStatus.UNASSIGNED);
        LOAD_REJECT.add(DisShipmentTaskStatus.IN_DELIVERY);
        LOAD_REJECT.add(DisShipmentTaskStatus.DELIVERED);
        LOAD_REJECT.add(DisShipmentTaskStatus.EXCEPTION);
        LOAD_REJECT.add(DisShipmentTaskStatus.CANCELLED);
        LOAD_REJECT.add(DisShipmentTaskStatus.CLOSED);
    }

    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisDriverDutyDao nxDisDriverDutyDao;
    @Autowired
    private NxDistributerUserDao nxDistributerUserDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private DisRouteFeasibilityService disRouteFeasibilityService;
    @Autowired
    private DisRouteDispatchSnapshotHelper disRouteDispatchSnapshotHelper;
    @Autowired
    private DisRouteDispatchSettings disRouteDispatchSettings;

    @Override
    public RouteDispatchOperationDecision evaluateAssign(NxDisShipmentTaskEntity task,
                                                         Integer targetDriverUserId,
                                                         RouteFeasibilityResult feasibility) {
        RouteDispatchOperationDecision base = evaluateRepairMutationBase(task, targetDriverUserId);
        if (!base.isAllowed()) {
            return base;
        }
        String status = task.getNxDstStatus();
        if (ASSIGN_REJECT.contains(status)) {
            return RouteDispatchOperationDecision.deny("当前状态不允许分派司机：" + labelStatus(status));
        }
        if (DisShipmentTaskStatus.ASSIGNED.equals(status)) {
            return RouteDispatchOperationDecision.deny("当前站点已分派，请使用改派调整司机");
        }
        if (!DisShipmentTaskStatus.SIMULATED.equals(status)
                && !DisShipmentTaskStatus.UNASSIGNED.equals(status)) {
            return RouteDispatchOperationDecision.deny("当前状态不允许分派司机：" + labelStatus(status));
        }
        return RouteDispatchOperationDecision.allow();
    }

    @Override
    public RouteDispatchOperationDecision evaluateMove(NxDisShipmentTaskEntity task,
                                                       Integer targetDriverUserId,
                                                       RouteFeasibilityResult feasibility) {
        RouteDispatchOperationDecision base = evaluateRepairMutationBase(task, targetDriverUserId);
        if (!base.isAllowed()) {
            return base;
        }
        if (MOVE_REJECT.contains(task.getNxDstStatus())) {
            return RouteDispatchOperationDecision.deny("当前状态不允许改派司机：" + labelStatus(task.getNxDstStatus()));
        }
        NxDisDriverRouteEntity driverRoute = loadDriverRouteForTask(task);
        if (driverRoute != null && DisRouteDriverDepartPolicy.isRouteDeparted(driverRoute)) {
            return RouteDispatchOperationDecision.deny("司机已出发，不能改派");
        }
        return RouteDispatchOperationDecision.allow();
    }

    @Override
    public RouteDispatchOperationDecision evaluateUnlock(NxDisShipmentTaskEntity task) {
        if (task == null) {
            return RouteDispatchOperationDecision.deny("配送任务不存在");
        }
        if (UNLOCK_REJECT.contains(task.getNxDstStatus())) {
            return RouteDispatchOperationDecision.deny("当前状态不允许解锁：" + labelStatus(task.getNxDstStatus()));
        }
        if (!DisShipmentTaskStatus.ASSIGNED.equals(task.getNxDstStatus())) {
            return RouteDispatchOperationDecision.deny("仅已分派且未可出发的任务可解锁");
        }
        if (task.getNxDstManualLocked() == null || task.getNxDstManualLocked() != 1) {
            return RouteDispatchOperationDecision.deny("当前任务未锁定，无需解锁");
        }
        return RouteDispatchOperationDecision.allow();
    }

    @Override
    public RouteDispatchOperationDecision evaluateConfirmLoad(NxDisShipmentTaskEntity task,
                                                              Integer targetDriverUserId,
                                                              RouteFeasibilityResult feasibility) {
        if (task == null) {
            return RouteDispatchOperationDecision.deny("配送任务不存在");
        }
        String status = task.getNxDstStatus();
        if (LOAD_REJECT.contains(status)) {
            String blockedReason = resolveConfirmLoadBlockedReason(status);
            if (blockedReason != null) {
                return RouteDispatchOperationDecision.deny(blockedReason);
            }
            if (DisShipmentTaskStatus.SIMULATED.equals(status)
                    || DisShipmentTaskStatus.UNASSIGNED.equals(status)) {
                return RouteDispatchOperationDecision.deny("请先确认分派后再装车");
            }
            return RouteDispatchOperationDecision.deny("当前状态不允许装车：" + labelStatus(status));
        }
        if (DisShipmentTaskStatus.READY_TO_GO.equals(status)) {
            return RouteDispatchOperationDecision.deny("已装车");
        }
        if (!DisShipmentTaskStatus.ASSIGNED.equals(status)) {
            return RouteDispatchOperationDecision.deny("当前状态不允许装车：" + labelStatus(status));
        }
        if (task.getNxDstAssignedDriverUserId() == null) {
            return RouteDispatchOperationDecision.deny("请先确认分派后再装车");
        }
        RouteDispatchOperationDecision execution = evaluateExecutionMutationBase(
                task, task.getNxDstAssignedDriverUserId(), feasibility);
        if (!execution.isAllowed()) {
            return execution;
        }
        RouteDispatchOperationDecision decision = RouteDispatchOperationDecision.allow();
        decision.setOperationHint("确认装车");
        return decision;
    }

    @Override
    public RouteDispatchOperationDecision evaluateConfirmCustomer(NxDisShipmentTaskEntity task,
                                                                  Integer suggestedDriverUserId,
                                                                  String sandboxRouteDate) {
        if (task == null) {
            return RouteDispatchOperationDecision.deny("配送任务不存在");
        }
        if (!hasActiveItems(task)) {
            return RouteDispatchOperationDecision.deny("当前站点没有有效订单行");
        }
        if (suggestedDriverUserId == null) {
            return RouteDispatchOperationDecision.deny("请选择可派司机或当前无可派司机");
        }
        NxDistributerUserEntity driver = nxDistributerUserDao.queryObject(suggestedDriverUserId);
        if (driver == null) {
            return RouteDispatchOperationDecision.deny("请选择可派司机或当前无可派司机");
        }
        if (!task.getNxDstDistributerId().equals(driver.getNxDiuDistributerId())) {
            return RouteDispatchOperationDecision.deny("请选择可派司机或当前无可派司机");
        }
        if (!getNxDisUserDriver().equals(driver.getNxDiuAdmin())) {
            return RouteDispatchOperationDecision.deny("请选择可派司机或当前无可派司机");
        }
        String dutyDate = sandboxRouteDate != null && !sandboxRouteDate.trim().isEmpty()
                ? sandboxRouteDate.trim() : task.getNxDstRouteDate();
        NxDisDriverDutyEntity duty = nxDisDriverDutyDao.queryByDisDriverDate(
                task.getNxDstDistributerId(), suggestedDriverUserId, dutyDate);
        if (duty == null || !ON_DUTY.equals(duty.getNxDddDutyStatus())) {
            return RouteDispatchOperationDecision.deny("请选择可派司机或当前无可派司机");
        }
        RouteDispatchOperationDecision decision = RouteDispatchOperationDecision.allow();
        decision.setOperationHint("确认该店出货完成");
        return decision;
    }

    @Override
    public RouteDispatchOperationDecision evaluateBillReadyPromotion(NxDisShipmentTaskEntity task,
                                                                     RouteFeasibilityResult feasibility) {
        if (task == null) {
            return RouteDispatchOperationDecision.deny("配送任务不存在");
        }
        if (!DisShipmentTaskStatus.ASSIGNED.equals(task.getNxDstStatus())
                && !DisShipmentTaskStatus.READY_TO_GO.equals(task.getNxDstStatus())) {
            return RouteDispatchOperationDecision.deny("仅已分派任务可在单据齐全后标记可出发");
        }
        if (task.getNxDstAssignedDriverUserId() == null) {
            return RouteDispatchOperationDecision.deny("请先确认分派后再标记可出发");
        }
        if (!hasActiveItems(task)) {
            return RouteDispatchOperationDecision.deny("当前站点没有有效订单行，不能标记可出发");
        }
        RouteDispatchOperationDecision driverDecision = evaluateDriverForTask(
                task, task.getNxDstAssignedDriverUserId(), DriverCheckContext.EXECUTION, true);
        if (!driverDecision.isAllowed()) {
            return driverDecision;
        }
        RouteDispatchOperationDecision planDecision = evaluatePlanExecutionBlocking(feasibility);
        if (!planDecision.isAllowed()) {
            return planDecision;
        }
        return RouteDispatchOperationDecision.allow();
    }

    @Override
    public void requireAssign(NxDisShipmentTaskEntity task, Integer targetDriverUserId) {
        evaluateAssign(task, targetDriverUserId, loadFeasibility(task)).requireAllowed();
    }

    @Override
    public void requireMove(NxDisShipmentTaskEntity task, Integer targetDriverUserId) {
        evaluateMove(task, targetDriverUserId, loadFeasibility(task)).requireAllowed();
    }

    @Override
    public void requireUnlock(NxDisShipmentTaskEntity task) {
        evaluateUnlock(task).requireAllowed();
    }

    @Override
    public void requireBillReadyPromotion(NxDisShipmentTaskEntity task) {
        evaluateBillReadyPromotion(task, loadFeasibility(task)).requireAllowed();
    }

    @Override
    public void enrichPlanReadModel(NxDisRoutePlanEntity plan, RouteFeasibilityResult feasibility) {
        if (plan == null) {
            return;
        }
        Date serverNow = new Date();
        DisRouteTemporalHelper.enrichPlanTemporalFields(plan, serverNow);

        plan.setNxDrpDispatchBatchLabel(DisRouteDispatchLabels.label(plan.getNxDrpDispatchBatch()));
        plan.setNxDrpFeasibilityStatusLabel(DisRouteDispatchLabels.label(
                feasibility != null ? feasibility.getFeasibilityStatus() : plan.getNxDrpFeasibilityStatus()));
        plan.setNxDrpScheduleStatusLabel(DisRouteDispatchLabels.label(plan.getNxDrpScheduleStatus()));

        if (plan.getDriverRoutes() != null) {
            for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
                DisRouteDriverDutyOverlayHelper.overlayOnRoute(route, plan, nxDisDriverDutyDao, serverNow);
            }
        }

        boolean anyCanLoad = false;
        boolean anyCanAssign = false;
        if (plan.getDriverRoutes() != null) {
            for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
                enrichDriverRoute(route, plan, feasibility);
                if (Boolean.TRUE.equals(route.getCanLoad())) {
                    anyCanLoad = true;
                }
                if (Boolean.TRUE.equals(route.getCanAssignMore())) {
                    anyCanAssign = true;
                }
                if (route.getStops() != null) {
                    for (NxDisRouteStopEntity stop : route.getStops()) {
                        if (Boolean.TRUE.equals(stop.getCanAssign())) {
                            anyCanAssign = true;
                        }
                    }
                }
            }
        }

        RouteDispatchOperationDecision planLoad = evaluatePlanLoading(plan, feasibility, anyCanLoad, anyCanAssign);
        plan.setCanStartLoading(planLoad.isAllowed());
        plan.setLoadingBlockedReason(planLoad.isAllowed() ? null : planLoad.getBlockedReason());
        plan.setOperationHint(planLoad.getOperationHint());

        if (plan.getDriverRoutes() != null) {
            for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
                DisRouteTemporalHelper.enrichDriverRouteOperationFields(route, plan);
                if (route.getStops() != null) {
                    for (NxDisRouteStopEntity stop : route.getStops()) {
                        DisRouteTemporalHelper.enrichStopTemporalFields(stop, serverNow);
                    }
                }
            }
        }
    }

    @Override
    public void enrichTasksReadModel(List<NxDisShipmentTaskEntity> tasks,
                                     NxDisRoutePlanEntity plan,
                                     RouteFeasibilityResult feasibility) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        Map<Integer, NxDisDriverRouteEntity> routeByTaskId = buildTaskRouteMap(plan);
        for (NxDisShipmentTaskEntity task : tasks) {
            NxDisDriverRouteEntity route = resolveRouteForTask(task, plan, routeByTaskId);
            enrichTask(task, route, plan, feasibility);
        }
    }

    @Override
    public void enrichEphemeralSandboxStops(List<NxDisRouteStopEntity> stops,
                                            NxDisRoutePlanEntity plan,
                                            RouteFeasibilityResult feasibility) {
        if (stops == null || stops.isEmpty()) {
            return;
        }
        Map<Integer, NxDisDriverRouteEntity> routeByDriver = new HashMap<Integer, NxDisDriverRouteEntity>();
        if (plan != null && plan.getDriverRoutes() != null) {
            for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
                if (route != null && route.getNxDdrDriverUserId() != null) {
                    routeByDriver.put(route.getNxDdrDriverUserId(), route);
                }
            }
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null || stop.getShipmentTask() == null) {
                continue;
            }
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (!isEphemeralSandboxTask(task)) {
                continue;
            }
            Integer suggestedDriver = task.getNxDstSuggestedDriverUserId() != null
                    ? task.getNxDstSuggestedDriverUserId() : task.getSuggestedDriverUserId();
            NxDisDriverRouteEntity route = suggestedDriver != null
                    ? routeByDriver.get(suggestedDriver) : null;
            enrichEphemeralSandboxTask(task, route, plan);
            mirrorStopFromTask(stop, task);
        }
    }

    @Override
    public void enrichExecutionRoutesReadModel(NxDisRoutePlanEntity plan,
                                               RouteFeasibilityResult feasibility) {
        if (plan == null || plan.getExecutionDriverRoutes() == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : plan.getExecutionDriverRoutes()) {
            enrichDriverRoute(route, plan, feasibility);
        }
    }

    private NxDisDriverRouteEntity resolveRouteForTask(NxDisShipmentTaskEntity task,
                                                       NxDisRoutePlanEntity plan,
                                                       Map<Integer, NxDisDriverRouteEntity> routeByTaskId) {
        if (task == null) {
            return null;
        }
        if (task.getNxDstDriverRouteId() != null && plan != null && plan.getDriverRoutes() != null) {
            for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
                if (task.getNxDstDriverRouteId().equals(route.getNxDdrId())) {
                    return route;
                }
            }
        }
        return routeByTaskId.get(task.getNxDstId());
    }

    private Map<Integer, NxDisDriverRouteEntity> buildTaskRouteMap(NxDisRoutePlanEntity plan) {
        Map<Integer, NxDisDriverRouteEntity> routeByTaskId = new HashMap<Integer, NxDisDriverRouteEntity>();
        if (plan == null || plan.getDriverRoutes() == null) {
            return routeByTaskId;
        }
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            if (route.getStops() == null) {
                continue;
            }
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop.getNxDrsShipmentTaskId() != null) {
                    routeByTaskId.put(stop.getNxDrsShipmentTaskId(), route);
                }
            }
        }
        return routeByTaskId;
    }

    private void enrichDriverRoute(NxDisDriverRouteEntity route,
                                   NxDisRoutePlanEntity plan,
                                   RouteFeasibilityResult feasibility) {
        route.setNxDdrFeasibilityStatusLabel(
                DisRouteDriverDutyOverlayHelper.labelDriverRouteFeasibility(route, plan));
        route.setNxDdrIneligibleReasonLabel(
                DisRouteDriverDutyOverlayHelper.labelDriverRouteIneligibleReason(route));

        RouteDispatchOperationDecision loadDecision = evaluateRouteLoad(route, plan, feasibility);
        route.setCanLoad(loadDecision.isAllowed());
        route.setLoadBlockedReason(loadDecision.isAllowed() ? null : loadDecision.getBlockedReason());

        RouteDispatchOperationDecision assignMore = evaluateRouteAssignMore(route, plan);
        route.setCanAssignMore(assignMore.isAllowed());
        route.setAssignBlockedReason(assignMore.isAllowed() ? null : assignMore.getBlockedReason());

        enrichDriverRouteDepartFields(route, plan, feasibility);

        if (route.getStops() == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : route.getStops()) {
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task == null && stop.getNxDrsShipmentTaskId() != null) {
                continue;
            }
            enrichTask(task, route, plan, feasibility);
            mirrorStopFromTask(stop, task);
        }
    }

    private void enrichTask(NxDisShipmentTaskEntity task,
                            NxDisDriverRouteEntity route,
                            NxDisRoutePlanEntity plan,
                            RouteFeasibilityResult feasibility) {
        if (task == null) {
            return;
        }
        if (isEphemeralSandboxTask(task)) {
            enrichEphemeralSandboxTask(task, route, plan);
            return;
        }
        task.setNxDstStatusLabel(DisRouteDispatchLabels.label(task.getNxDstStatus()));

        Integer targetDriver = task.getNxDstAssignedDriverUserId() != null
                ? task.getNxDstAssignedDriverUserId()
                : (route != null ? route.getNxDdrDriverUserId() : task.getNxDstSuggestedDriverUserId());

        RouteDispatchOperationDecision assign = evaluateAssign(task, targetDriver, feasibility);
        task.setCanAssign(assign.isAllowed());
        task.setAssignBlockedReason(assign.isAllowed() ? null : assign.getBlockedReason());

        RouteDispatchOperationDecision confirmLoad = evaluateConfirmLoad(task, targetDriver, feasibility);
        task.setCanConfirmLoad(Boolean.valueOf(confirmLoad.isAllowed()));
        task.setConfirmLoadBlockedReason(confirmLoad.isAllowed()
                ? null : confirmLoad.getBlockedReason());

        RouteDispatchOperationDecision move = evaluateMove(task, targetDriver, feasibility);
        task.setCanMove(move.isAllowed());
        task.setMoveBlockedReason(move.isAllowed() ? null : move.getBlockedReason());
        if (route != null && DisRouteDriverDepartPolicy.isRouteDeparted(route)) {
            task.setCanMove(false);
            task.setMoveBlockedReason("司机已出发，不能改派");
        }

        RouteDispatchOperationDecision unlock = evaluateUnlock(task);
        task.setCanUnlock(unlock.isAllowed());
        task.setUnlockBlockedReason(unlock.isAllowed() ? null : unlock.getBlockedReason());

        disRouteDispatchSnapshotHelper.applyItemMetrics(task);
        DisRoutePriorityPreviewHelper.enrichTask(task);

        DisRouteBillPrintStatusHelper.BillPrintSummary billSummary = DisRouteBillPrintStatusHelper.summarize(task);
        task.setBillPrintStatus(billSummary.status);
        task.setUnprintedBillCount(billSummary.unprintedCount);
        if (billSummary.unprintedCount > 0) {
            task.setBillPrintWarning("还有 " + billSummary.unprintedCount + " 个订单配送单未打印，可继续装车/出发");
        }

        enrichReturnToSandboxFields(task);

        task.setOperationStatusLabel(buildTaskOperationLabel(task, assign, confirmLoad));
    }

    private void enrichDriverRouteDepartFields(NxDisDriverRouteEntity route,
                                               NxDisRoutePlanEntity plan,
                                               RouteFeasibilityResult feasibility) {
        if (route == null) {
            return;
        }
        String routeStatus = DisRouteRouteExecutionHelper.resolveRouteStatus(route);
        route.setRouteStatus(routeStatus);
        route.setRouteStatusLabel(DisRouteRouteExecutionHelper.resolveRouteStatusLabel(routeStatus));
        if (route.getNxDdrRouteStatus() == null || route.getNxDdrRouteStatus().trim().isEmpty()) {
            route.setNxDdrRouteStatus(routeStatus);
        }

        DisRouteDriverDepartPolicy.RouteStopCounts counts = DisRouteDriverDepartPolicy.countRouteStops(route);
        route.setTotalStopCount(counts.totalStopCount);
        route.setConfirmedStopCount(counts.confirmedStopCount);
        route.setSuggestedStopCount(counts.unconfirmedStopCount);

        int unprinted = DisRouteDriverDepartPolicy.countUnprintedBillsOnRoute(route);
        route.setUnprintedBillCount(unprinted);
        route.setDepartWarning(DisRouteDriverDepartPolicy.buildDepartWarning(route));
        route.setDepartConfirmMessage(DisRouteDriverDepartPolicy.buildDepartConfirmMessage(route));
        route.setDepartActionLabel(DisRouteDriverDepartPolicy.ACTION_LABEL);
        route.setDepartedAt(route.getNxDdrActualDepartAt());

        boolean driverOnDuty = false;
        if (plan != null && route.getNxDdrDriverUserId() != null) {
            String routeDate = plan.getNxDrpRouteDate() != null ? plan.getNxDrpRouteDate() : plan.getNxDrpPlanDate();
            NxDisDriverDutyEntity duty = nxDisDriverDutyDao.queryByDisDriverDate(
                    plan.getNxDrpDistributerId(), route.getNxDdrDriverUserId(), routeDate);
            driverOnDuty = duty != null && ON_DUTY.equals(duty.getNxDddDutyStatus());
        }

        List<NxDisShipmentTaskEntity> routeTasks = null;
        if (route.getNxDdrId() != null) {
            routeTasks = nxDisShipmentTaskDao.queryByDriverRouteId(route.getNxDdrId());
        }
        boolean requireLoadBeforeDepart = plan != null
                && disRouteDispatchSettings.isRequireLoadBeforeDepart(plan.getNxDrpDistributerId());
        RouteDispatchOperationDecision departDecision = DisRouteDriverDepartPolicy.evaluateDepart(
                route, plan, routeTasks, driverOnDuty, feasibility, requireLoadBeforeDepart);
        route.setCanDepart(departDecision.isAllowed());
        if (departDecision.isAllowed()) {
            route.setDepartBlockedReason(null);
        } else if (DisRouteDriverRouteStatus.IN_DELIVERY.equals(routeStatus)
                || DisRouteDriverRouteStatus.DELIVERED.equals(routeStatus)
                || DisRouteDriverRouteStatus.CLOSED.equals(routeStatus)
                || DisRouteDriverDepartPolicy.isRouteDeparted(route)) {
            route.setDepartBlockedReason("该司机路线已在配送中或已完成");
            route.setCanDepart(false);
        } else if (counts.unconfirmedStopCount > 0) {
            route.setDepartBlockedReason("请先确认全部门店出货完成");
        } else {
            route.setDepartBlockedReason(departDecision.getBlockedReason());
        }
        if (route.getNxDdrActualDepartAt() != null) {
            route.setDepartedAt(route.getNxDdrActualDepartAt());
        }
        if (DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
            DisRouteRouteExecutionHelper.syncExecutionCanonicalFields(route);
        }
        boolean allDelivered = DisRouteRouteExecutionHelper.areAllStopsDelivered(route);
        route.setAllStopsDelivered(allDelivered);
        route.setCanCompleteRoute(allDelivered && !DisRouteRouteExecutionHelper.hasPendingExecutionStops(route));
    }

    private void enrichReturnToSandboxFields(NxDisShipmentTaskEntity task) {
        if (task == null || task.getNxDstId() == null || isEphemeralSandboxTask(task)) {
            return;
        }
        List<NxDisShipmentTaskEntity> siblings = task.getNxDstDriverRouteId() != null
                ? nxDisShipmentTaskDao.queryByDriverRouteId(task.getNxDstDriverRouteId())
                : null;
        NxDisDriverRouteEntity driverRoute = loadDriverRouteForTask(task);
        RouteDispatchOperationDecision returnDecision =
                DisRouteReturnToSandboxPolicy.evaluate(task, siblings, driverRoute);
        task.setCanReturnToSandbox(returnDecision.isAllowed());
        task.setReturnToSandboxActionLabel(DisRouteReturnToSandboxPolicy.ACTION_LABEL);
        task.setReturnToSandboxBlockedReason(returnDecision.isAllowed()
                ? null : returnDecision.getBlockedReason());
        task.setReturnToSandboxWarning(returnDecision.getOperationHint());
        task.setReturnToSandboxConfirmMessage(DisRouteReturnToSandboxPolicy.buildConfirmDialogMessage(task));
    }

    private void enrichEphemeralSandboxTask(NxDisShipmentTaskEntity task,
                                            NxDisDriverRouteEntity route,
                                            NxDisRoutePlanEntity plan) {
        task.setNxDstStatusLabel(DisRouteDispatchLabels.label(task.getNxDstStatus()));

        Integer suggestedDriver = task.getNxDstSuggestedDriverUserId() != null
                ? task.getNxDstSuggestedDriverUserId()
                : (route != null ? route.getNxDdrDriverUserId() : null);
        task.setSuggestedDriverUserId(suggestedDriver);
        if (suggestedDriver != null) {
            NxDistributerUserEntity driver = nxDistributerUserDao.queryObject(suggestedDriver);
            task.setSuggestedDriverName(resolveDriverDisplayName(driver));
        }

        String sandboxRouteDate = plan != null ? plan.getNxDrpRouteDate() : task.getNxDstRouteDate();
        RouteDispatchOperationDecision confirmCustomer = evaluateConfirmCustomer(
                task, suggestedDriver, sandboxRouteDate);
        task.setCanConfirmCustomer(confirmCustomer.isAllowed());
        task.setConfirmCustomerActionLabel("确认该店出货完成");
        task.setConfirmCustomerBlockedReason(confirmCustomer.isAllowed()
                ? null : confirmCustomer.getBlockedReason());

        task.setCanAssign(false);
        task.setAssignBlockedReason(null);
        task.setCanConfirmLoad(false);
        task.setConfirmLoadBlockedReason(null);
        task.setCanMove(false);
        task.setMoveBlockedReason(null);
        task.setCanUnlock(false);
        task.setUnlockBlockedReason(null);

        disRouteDispatchSnapshotHelper.applyItemMetrics(task);
        DisRoutePriorityPreviewHelper.enrichTask(task);

        DisRouteBillPrintStatusHelper.BillPrintSummary billSummary = DisRouteBillPrintStatusHelper.summarize(task);
        task.setBillPrintStatus(billSummary.status);
        task.setUnprintedBillCount(billSummary.unprintedCount);
        if (billSummary.unprintedCount > 0) {
            task.setBillPrintWarning("还有 " + billSummary.unprintedCount + " 个订单配送单未打印，可继续装车/出发");
        }

        task.setOperationStatusLabel(confirmCustomer.isAllowed()
                ? "可确认出货" : "暂不可确认");
    }

    private boolean isEphemeralSandboxTask(NxDisShipmentTaskEntity task) {
        if (task == null) {
            return false;
        }
        if (task.getNxDstId() == null) {
            return true;
        }
        if (Boolean.TRUE.equals(task.getConfirmViaSandbox())) {
            return true;
        }
        String source = task.getStopSource();
        return DisRouteSandboxStopSource.SANDBOX_SUGGESTED.equals(source)
                || DisRouteSandboxStopSource.UNASSIGNED.equals(source);
    }

    private String resolveDriverDisplayName(NxDistributerUserEntity driver) {
        if (driver == null) {
            return null;
        }
        if (driver.getNxDiuWxNickName() != null && !driver.getNxDiuWxNickName().trim().isEmpty()) {
            return driver.getNxDiuWxNickName().trim();
        }
        return "司机 " + driver.getNxDistributerUserId();
    }

    private void mirrorStopFromTask(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        if (stop == null || task == null) {
            return;
        }
        stop.setCanAssign(task.getCanAssign());
        stop.setAssignBlockedReason(task.getAssignBlockedReason());
        stop.setCanConfirmLoad(task.getCanConfirmLoad() != null ? task.getCanConfirmLoad() : Boolean.FALSE);
        stop.setConfirmLoadBlockedReason(task.getConfirmLoadBlockedReason());
        stop.setCanMove(task.getCanMove());
        stop.setMoveBlockedReason(task.getMoveBlockedReason());
        stop.setCanUnlock(task.getCanUnlock());
        stop.setUnlockBlockedReason(task.getUnlockBlockedReason());
        stop.setOperationStatusLabel(task.getOperationStatusLabel());
        stop.setCanConfirmCustomer(task.getCanConfirmCustomer());
        stop.setConfirmCustomerActionLabel(task.getConfirmCustomerActionLabel());
        stop.setConfirmCustomerBlockedReason(task.getConfirmCustomerBlockedReason());
        stop.setCanReturnToSandbox(task.getCanReturnToSandbox());
        stop.setReturnToSandboxActionLabel(task.getReturnToSandboxActionLabel());
        stop.setReturnToSandboxBlockedReason(task.getReturnToSandboxBlockedReason());
        stop.setReturnToSandboxWarning(task.getReturnToSandboxWarning());
        stop.setReturnToSandboxConfirmMessage(task.getReturnToSandboxConfirmMessage());
        stop.setSuggestedDriverUserId(task.getSuggestedDriverUserId() != null
                ? task.getSuggestedDriverUserId() : task.getNxDstSuggestedDriverUserId());
        stop.setSuggestedDriverName(task.getSuggestedDriverName());
        stop.setNxDrsTimeWindowStatusLabel(DisRouteDispatchLabels.label(stop.getNxDrsTimeWindowStatus()));
        mirrorDispatchSnapshotFields(stop, task);
    }

    /** 读模型：stop 与 task 使用同一套快照字段与 priority preview。 */
    private void mirrorDispatchSnapshotFields(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        stop.setNxDrsCustomerTier(task.getNxDstCustomerTier());
        stop.setNxDrsPriorityWeight(task.getNxDstPriorityWeight());
        stop.setNxDrsOrderCount(task.getNxDstOrderCount());
        stop.setNxDrsItemCount(task.getNxDstItemCount());
        stop.setNxDrsTotalQuantity(task.getNxDstTotalQuantity());
        stop.setNxDrsEarliestDeliveryTimeS(task.getNxDstEarliestDeliveryTimeS());
        stop.setNxDrsLatestDeliveryTimeS(task.getNxDstLatestDeliveryTimeS());
        if (task.getNxDstServiceMinutes() != null) {
            stop.setNxDrsServiceMinutes(task.getNxDstServiceMinutes());
        }
        stop.setNxDrsTimeWindowOverrideFlag(task.getNxDstTimeWindowOverrideFlag());
        stop.setNxDrsTimeWindowAdjustReason(task.getNxDstTimeWindowAdjustReason());
        stop.setCustomerTierLabel(task.getCustomerTierLabel());
        stop.setPriorityScorePreview(task.getPriorityScorePreview());
        stop.setPriorityReason(task.getPriorityReason());
    }

    private String buildTaskOperationLabel(NxDisShipmentTaskEntity task,
                                           RouteDispatchOperationDecision assign,
                                           RouteDispatchOperationDecision confirmLoad) {
        if (DisShipmentTaskStatus.READY_TO_GO.equals(task.getNxDstStatus())) {
            return "单据齐全，可出发";
        }
        if (DisShipmentTaskStatus.ASSIGNED.equals(task.getNxDstStatus())) {
            if (confirmLoad.isAllowed()) {
                return "已分派 · 可装车";
            }
            return "已分派";
        }
        if (DisShipmentTaskStatus.SIMULATED.equals(task.getNxDstStatus())) {
            return assign.isAllowed() ? "确认分派" : "暂不可分派";
        }
        return task.getNxDstStatusLabel();
    }

    /** 修复类动作（assign / move）：不因 plan INFEASIBLE 阻断 */
    private RouteDispatchOperationDecision evaluateRepairMutationBase(NxDisShipmentTaskEntity task,
                                                                      Integer targetDriverUserId) {
        if (task == null) {
            return RouteDispatchOperationDecision.deny("配送任务不存在");
        }
        if (!hasActiveItems(task)) {
            return RouteDispatchOperationDecision.deny("当前站点没有有效订单行，不能操作");
        }
        return evaluateDriverForTask(task, targetDriverUserId, DriverCheckContext.REPAIR, true);
    }

    /** 执行类动作（装车 / 出发）：plan INFEASIBLE 会阻断 */
    private RouteDispatchOperationDecision evaluateExecutionMutationBase(NxDisShipmentTaskEntity task,
                                                                         Integer targetDriverUserId,
                                                                         RouteFeasibilityResult feasibility) {
        if (task == null) {
            return RouteDispatchOperationDecision.deny("配送任务不存在");
        }
        if (!hasActiveItems(task)) {
            return RouteDispatchOperationDecision.deny("当前站点没有有效订单行，不能装车");
        }
        RouteDispatchOperationDecision planDecision = evaluatePlanExecutionBlocking(feasibility);
        if (!planDecision.isAllowed()) {
            return planDecision;
        }
        return evaluateDriverForTask(task, targetDriverUserId, DriverCheckContext.EXECUTION, true);
    }

    private enum DriverCheckContext {
        REPAIR, EXECUTION
    }

    private RouteDispatchOperationDecision evaluateDriverForTask(NxDisShipmentTaskEntity task,
                                                                 Integer targetDriverUserId,
                                                                 DriverCheckContext context,
                                                                 boolean checkLockedIneligible) {
        if (targetDriverUserId == null) {
            return RouteDispatchOperationDecision.deny("请先指定司机");
        }
        NxDistributerUserEntity driver = nxDistributerUserDao.queryObject(targetDriverUserId);
        if (driver == null) {
            return RouteDispatchOperationDecision.deny("司机不存在");
        }
        if (!task.getNxDstDistributerId().equals(driver.getNxDiuDistributerId())) {
            return RouteDispatchOperationDecision.deny("司机不属于当前配送商");
        }
        if (!getNxDisUserDriver().equals(driver.getNxDiuAdmin())) {
            return RouteDispatchOperationDecision.deny("目标用户不是司机角色");
        }

        NxDisRoutePlanEntity plan = task.getNxDstPlanId() != null
                ? nxDisRoutePlanDao.queryObject(task.getNxDstPlanId()) : null;
        String routeDate = task.getNxDstRouteDate();
        NxDisDriverDutyEntity duty = nxDisDriverDutyDao.queryByDisDriverDate(
                task.getNxDstDistributerId(), targetDriverUserId, routeDate);
        if (duty == null || !ON_DUTY.equals(duty.getNxDddDutyStatus())) {
            return RouteDispatchOperationDecision.deny("目标司机当前不可派，不能分派");
        }

        if (checkLockedIneligible && task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1
                && task.getNxDstAssignedDriverUserId() != null
                && task.getNxDstAssignedDriverUserId().equals(targetDriverUserId)) {
            NxDisDriverRouteEntity route = nxDisDriverRouteDao.queryByPlanAndDriver(
                    task.getNxDstPlanId(), targetDriverUserId);
            if (route != null && isRouteIneligible(route)) {
                return RouteDispatchOperationDecision.deny("当前站点已锁定在不可派司机上，请先解锁或改派");
            }
        }

        if (task.getNxDstPlanId() != null) {
            NxDisDriverRouteEntity targetRoute = nxDisDriverRouteDao.queryByPlanAndDriver(
                    task.getNxDstPlanId(), targetDriverUserId);
            if (targetRoute != null && isRouteIneligible(targetRoute)) {
                return denyDriverUnsuitableForBatch(context);
            }
        }
        return RouteDispatchOperationDecision.allow();
    }

    private RouteDispatchOperationDecision denyDriverUnsuitableForBatch(DriverCheckContext context) {
        if (context == DriverCheckContext.REPAIR) {
            return RouteDispatchOperationDecision.deny("目标司机当前不可派，不能分派");
        }
        return RouteDispatchOperationDecision.deny("目标司机当前不可派，不能装车");
    }

    private RouteDispatchOperationDecision evaluatePlanExecutionBlocking(RouteFeasibilityResult feasibility) {
        if (feasibility == null) {
            return RouteDispatchOperationDecision.allow();
        }
        if (DisRouteFeasibilityStatus.INFEASIBLE.equals(feasibility.getFeasibilityStatus())) {
            return RouteDispatchOperationDecision.deny("当前批次不可执行，请先处理异常后再装车或出发");
        }
        return RouteDispatchOperationDecision.allow();
    }

    private RouteDispatchOperationDecision evaluateRouteLoad(NxDisDriverRouteEntity route,
                                                             NxDisRoutePlanEntity plan,
                                                             RouteFeasibilityResult feasibility) {
        if (isRouteIdle(route)) {
            RouteDispatchOperationDecision decision = RouteDispatchOperationDecision.deny("当前司机路线暂无配送站");
            decision.setOperationHint("空闲路线");
            return decision;
        }
        if (isRouteIneligible(route)) {
            return RouteDispatchOperationDecision.deny("当前司机不可派，不能装车");
        }
        RouteDispatchOperationDecision planDecision = evaluatePlanExecutionBlocking(feasibility);
        if (!planDecision.isAllowed()) {
            return planDecision;
        }
        if (route.getStops() != null) {
            for (NxDisRouteStopEntity stop : route.getStops()) {
                NxDisShipmentTaskEntity task = stop.getShipmentTask();
                if (task == null) {
                    continue;
                }
                RouteDispatchOperationDecision load = evaluateConfirmLoad(
                        task, route.getNxDdrDriverUserId(), feasibility);
                if (load.isAllowed()) {
                    RouteDispatchOperationDecision ok = RouteDispatchOperationDecision.allow();
                    ok.setOperationHint("存在可装车站点");
                    return ok;
                }
            }
        }
        return RouteDispatchOperationDecision.deny("当前路线没有可装车的站点");
    }

    private RouteDispatchOperationDecision evaluateRouteAssignMore(NxDisDriverRouteEntity route,
                                                                   NxDisRoutePlanEntity plan) {
        int stopCount = route.getNxDdrStopCount() != null ? route.getNxDdrStopCount() : 0;
        if (isRouteIneligible(route)) {
            if (stopCount > 0) {
                return RouteDispatchOperationDecision.deny(
                        "当前司机不可派，已有 " + stopCount + " 个站点方案待处理，不可继续分派");
            }
            return RouteDispatchOperationDecision.deny("当前司机不可派，不能继续分派");
        }
        if (plan != null && DisRoutePlanTemporalStatus.EXPIRED.equals(plan.getPlanTemporalStatus())) {
            return RouteDispatchOperationDecision.deny("当前路线计划已过期，不可继续分派");
        }
        return RouteDispatchOperationDecision.allow();
    }

    private RouteDispatchOperationDecision evaluatePlanLoading(NxDisRoutePlanEntity plan,
                                                               RouteFeasibilityResult feasibility,
                                                               boolean anyCanLoad,
                                                               boolean anyCanAssign) {
        boolean planInfeasible = feasibility != null
                && DisRouteFeasibilityStatus.INFEASIBLE.equals(feasibility.getFeasibilityStatus());

        if (anyCanLoad && !planInfeasible) {
            RouteDispatchOperationDecision ok = RouteDispatchOperationDecision.allow();
            ok.setOperationHint("存在可装车站点");
            return ok;
        }

        if (planInfeasible) {
            RouteDispatchOperationDecision decision = RouteDispatchOperationDecision.deny(
                    "当前批次不可执行，请先处理异常后再装车或出发");
            if (anyCanAssign) {
                decision.setOperationHint("可先确认分派或改派修复方案");
            } else {
                decision.setOperationHint("请先处理批次异常");
            }
            return decision;
        }

        if (anyCanAssign) {
            RouteDispatchOperationDecision ok = RouteDispatchOperationDecision.allow();
            ok.setOperationHint("可继续分派司机");
            return ok;
        }

        RouteDispatchOperationDecision decision = RouteDispatchOperationDecision.deny("当前没有可装车的站点");
        decision.setOperationHint("暂无装车动作");
        return decision;
    }

    private RouteFeasibilityResult loadFeasibility(NxDisShipmentTaskEntity task) {
        if (task == null || task.getNxDstPlanId() == null) {
            return null;
        }
        return disRouteFeasibilityService.preview(task.getNxDstPlanId());
    }

    private boolean isRouteIneligible(NxDisDriverRouteEntity route) {
        if (route == null) {
            return false;
        }
        if (DisRouteDriverDepartPolicy.isRouteDeparted(route)) {
            return true;
        }
        if (route.getNxDdrDispatchEligible() != null && route.getNxDdrDispatchEligible() == 0) {
            return true;
        }
        String status = route.getNxDdrFeasibilityStatus();
        return DisRouteFeasibilityStatus.IDLE.equals(status);
    }

    private NxDisDriverRouteEntity loadDriverRouteForTask(NxDisShipmentTaskEntity task) {
        if (task == null || task.getNxDstDriverRouteId() == null) {
            return null;
        }
        return nxDisDriverRouteDao.queryObject(task.getNxDstDriverRouteId());
    }

    private boolean isRouteIdle(NxDisDriverRouteEntity route) {
        return route.getNxDdrStopCount() != null && route.getNxDdrStopCount() == 0;
    }

    private boolean hasActiveItems(NxDisShipmentTaskEntity task) {
        List<NxDisShipmentTaskItemEntity> items = task.getItems();
        if (items == null || items.isEmpty()) {
            return false;
        }
        for (NxDisShipmentTaskItemEntity item : items) {
            if (DisRouteDispatchSnapshotHelper.isActiveItem(item)) {
                return true;
            }
        }
        return false;
    }

    private static String resolveConfirmLoadBlockedReason(String status) {
        if (DisShipmentTaskStatus.READY_TO_GO.equals(status)) {
            return "已装车";
        }
        if (DisShipmentTaskStatus.IN_DELIVERY.equals(status)) {
            return "已出发";
        }
        if (DisShipmentTaskStatus.DELIVERED.equals(status)) {
            return "已送达";
        }
        if (DisShipmentTaskStatus.EXCEPTION.equals(status)) {
            return "配送异常";
        }
        return null;
    }

    private String labelStatus(String status) {
        String label = DisRouteDispatchLabels.label(status);
        return label != null ? label : status;
    }
}
