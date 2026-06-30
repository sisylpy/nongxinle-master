package com.nongxinle.todaydispatch;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisRouteStopDao;
import com.nongxinle.dao.NxDisSandboxDayTimeWindowDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dao.NxDisShipmentTaskItemDao;
import com.nongxinle.dao.NxDistributerUserDao;
import com.nongxinle.dto.route.DisRouteOrderSnapshotDto;
import com.nongxinle.dto.route.RouteDispatchOperationDecision;
import com.nongxinle.dto.route.SandboxDriverRouteEditConfirmRequest;
import com.nongxinle.dto.route.SandboxStopConfirmRequest;
import com.nongxinle.dto.route.SandboxStopReturnToSandboxRequest;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisSandboxDayTimeWindowEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;
import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.route.DisRouteAssignReason;
import com.nongxinle.route.DisRouteDispatchBatch;
import com.nongxinle.route.DisRouteLoadingGateHelper;
import com.nongxinle.route.DisRouteRouteExecutionHelper;
import com.nongxinle.route.DisRouteSandboxStopKeyUtils;
import com.nongxinle.route.DisShipmentTaskOpenKeyUtils;
import com.nongxinle.route.DisShipmentTaskStatus;
import com.nongxinle.route.RouteCostProviderType;
import com.nongxinle.route.RouteOptimizerType;
import com.nongxinle.route.model.GeoPoint;
import com.nongxinle.service.NxDistributerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.route.DisRoutePlanStatus.ASSIGNED;
import static com.nongxinle.route.DisRoutePlanStatus.READY;
import static com.nongxinle.route.DisShipmentTaskItemStatus.ACTIVE;
import static com.nongxinle.route.DisShipmentTaskStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.CLOSED;
import static com.nongxinle.route.DisShipmentTaskStatus.DELIVERED;
import static com.nongxinle.route.DisShipmentTaskStatus.EXCEPTION;
import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;
import static com.nongxinle.route.DisShipmentTaskStatus.READY_TO_GO;
import static com.nongxinle.route.DisShipmentTaskStatus.SIMULATED;
import static com.nongxinle.route.DisShipmentTaskStatus.UNASSIGNED;
import static com.nongxinle.route.RouteCoordinateUtils.isValidCoordinate;
import static com.nongxinle.route.RouteCoordinateUtils.toPoint;
import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisUserAdmin;

/**
 * M1 分派确认写库（todaydispatch 自有逻辑，仅 DAO + 必要实体字段）。
 *
 * <p>写方法前必须对照 DAO XML 弄清 SQL 条件。本类查询约定：
 * <ul>
 *   <li>eligible 订单：与 compute 一致，{@code queryEligibleLiveOrderSnapshots(disId,null,null,null)}
 *       不按送达日过滤；一次 confirm 只查 1 次，按 dep 内存过滤</li>
 *   <li>已有 task：优先 taskId → liveOrderIds(item) → openKey → task.queryList(disId)，
 *       禁止为找 task 重复扫全表订单</li>
 *   <li>plan/路线：confirm 入口 resolveOrCreatePlan 一次；每司机 ensureDriverRoute 一次</li>
 * </ul>
 */
@Service
public class TodayDispatchRouteConfirmService {

    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;

    @Autowired
    private NxDisShipmentTaskItemDao nxDisShipmentTaskItemDao;

    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;

    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;

    @Autowired
    private NxDisSandboxDayTimeWindowDao nxDisSandboxDayTimeWindowDao;

    @Autowired
    private NxDisRouteStopDao nxDisRouteStopDao;

    @Autowired
    private NxDistributerService nxDistributerService;

    @Autowired
    private NxDistributerUserDao nxDistributerUserDao;

    @Autowired
    private TodayDispatchComputeService todayDispatchComputeService;

    @Autowired
    private DispatchPageAssembler dispatchPageAssembler;

    /**
     * 装车/分派中移除单店：取消 task + item，清 openKey，路线无站时撤销装车门禁。
     * 不调用 ReturnService / schedule / feasibility。
     */
    @Transactional
    public Map<String, Object> returnStopToSandbox(Integer deliveryStopId,
                                                   SandboxStopReturnToSandboxRequest request)
            throws Exception {
        normalizeReturnRequest(request);
        validateReturnRequest(deliveryStopId, request);

        NxDisShipmentTaskEntity task = loadTaskDetail(deliveryStopId);
        if (task == null) {
            throw new IllegalArgumentException("deliveryStopId=" + deliveryStopId + " 不存在");
        }

        RouteDispatchOperationDecision decision = evaluateReturnAllowed(task);
        if (!decision.isAllowed()) {
            throw new IllegalStateException(decision.getBlockedReason());
        }

        Integer planId = task.getNxDstPlanId();
        Integer driverRouteId = task.getNxDstDriverRouteId();
        Integer disId = request.getDisId() != null ? request.getDisId() : task.getNxDstDistributerId();
        String routeDate = resolveReturnRouteDate(request, task);
        String batchCode = resolveReturnBatchCode(request, planId);

        String reason = request.getReason() != null && !request.getReason().trim().isEmpty()
                ? request.getReason().trim() : "返回沙盘";
        cancelTaskFromRoute(task, request.getOperatorUserId(), reason);

        boolean exitedLoading = false;
        if (driverRouteId != null && countActiveTasksOnRoute(driverRouteId) == 0) {
            exitedLoading = clearLoadingGate(driverRouteId);
        }
        if (planId != null && !Boolean.TRUE.equals(request.getDeferPlanRefresh())) {
            syncPlanDriverCount(planId);
        }

        if (Boolean.TRUE.equals(request.getSuppressTodayResponse())) {
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("deliveryStopId", deliveryStopId);
            result.put("planId", planId);
            result.put("exitedLoading", Boolean.valueOf(exitedLoading));
            return result;
        }
        TodayDispatchResult pageResult = todayDispatchComputeService.compute(
                disId, routeDate, batchCode, request.getOperatorUserId());
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("pageViewModel", dispatchPageAssembler.assemble(pageResult));
        data.put("deliveryStopId", deliveryStopId);
        data.put("planId", planId);
        data.put("exitedLoading", Boolean.valueOf(exitedLoading));
        return data;
    }

    /** 沙盘/手动分派：确认单店装车（复用 upsertDepOnDriverRoute，返回分派中 pageViewModel）。 */
    @Transactional
    public Map<String, Object> confirmStop(SandboxStopConfirmRequest request) throws Exception {
        backfillDepFromTaskId(request);
        normalizeStopConfirmRequest(request);
        validateStopConfirmRequest(request);

        String routeDate = resolveRouteDate(request.getRouteDate());
        String batchCode = normalizeBatch(request.getBatchCode());
        Integer disId = request.getDisId();

        upsertDepOnDriverRoute(request);

        Integer depFatherId = resolveDepFatherId(request);
        NxDisShipmentTaskEntity task = resolveExistingTask(request, depFatherId);
        Integer planId = task != null ? task.getNxDstPlanId() : request.getPlanId();

        if (planId != null && !Boolean.TRUE.equals(request.getDeferPlanRefresh())) {
            refreshPlanAfterConfirm(planId);
        }

        if (Boolean.TRUE.equals(request.getSuppressTodayResponse())) {
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("deliveryStopId", task != null ? task.getNxDstId() : request.getTaskId());
            result.put("planId", planId);
            return result;
        }

        TodayDispatchResult pageResult = todayDispatchComputeService.compute(
                disId, routeDate, batchCode, request.getOperatorUserId());
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("pageViewModel", dispatchPageAssembler.assemble(pageResult));
        data.put("deliveryStopId", task != null ? task.getNxDstId() : request.getTaskId());
        return data;
    }

    private void backfillDepFromTaskId(SandboxStopConfirmRequest request) {
        if (request == null || request.getTaskId() == null || resolveDepFatherId(request) != null) {
            return;
        }
        NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(request.getTaskId());
        if (task != null && task.getNxDstDepFatherId() != null) {
            request.setDepFatherId(task.getNxDstDepFatherId());
        }
    }

    @Transactional
    public Map<String, Object> confirmDriverRouteEdit(SandboxDriverRouteEditConfirmRequest request)
            throws Exception {
        normalizeDriverRouteEditRequest(request);
        validateDriverRouteEditRequest(request);

        String routeDate = resolveRouteDate(request.getRouteDate());
        String batchCode = normalizeBatch(request.getBatchCode());
        List<String> stopKeys = normalizeStopKeys(request.getStopKeys());
        Set<Integer> targetDepIds = parseDepIds(stopKeys);

        GeoPoint depot = resolveDepot(request.getDisId());
        NxDisRoutePlanEntity plan = resolveOrCreatePlan(
                request.getDisId(), routeDate, batchCode, depot, request.getOperatorUserId());
        Integer planId = plan.getNxDrpId();
        List<DisRouteOrderSnapshotDto> eligibleSnapshots =
                queryAllEligibleSnapshots(request.getDisId());
        removeDepsNotOnRoute(request, planId, targetDepIds);

        int seq = 1;
        for (String stopKey : stopKeys) {
            Integer depId = DisRouteSandboxStopKeyUtils.parseDepFatherId(stopKey);
            if (depId == null) {
                continue;
            }
            SandboxStopConfirmRequest stopRequest = buildStopConfirmRequest(
                    request, routeDate, batchCode, depId, seq++, request.getConfirmReason());
            stopRequest.setPlanId(planId);
            stopRequest.setEligibleOrderSnapshotCache(eligibleSnapshots);
            upsertDepOnDriverRoute(stopRequest);
        }

        NxDisDriverRouteEntity driverRoute = nxDisDriverRouteDao.queryByPlanAndDriver(
                planId, request.getDriverUserId());
        if (driverRoute != null && driverRoute.getNxDdrId() != null) {
            reorderDriverRouteStops(driverRoute.getNxDdrId(), stopKeys);
        }

        refreshPlanAfterConfirm(planId);

        if (driverRoute == null) {
            driverRoute = nxDisDriverRouteDao.queryByPlanAndDriver(
                    planId, request.getDriverUserId());
        }
        boolean enteredLoading = tryAutoEnterLoadingAfterRouteEditConfirm(
                driverRoute, request.getOperatorUserId());

        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("success", true);
        response.put("driverUserId", request.getDriverUserId());
        response.put("stopCount", stopKeys.size());
        response.put("routeDate", routeDate);
        response.put("batchCode", batchCode);
        response.put("enteredLoading", Boolean.valueOf(enteredLoading));
        if (planId != null) {
            response.put("planId", planId);
        }
        if (driverRoute != null && driverRoute.getNxDdrId() != null) {
            response.put("driverRouteId", driverRoute.getNxDdrId());
        }
        if (enteredLoading) {
            response.put("nextPage", "LOADING");
        } else if (driverRoute != null && countActiveAssignedTasksOnRoute(driverRoute.getNxDdrId()) > 0) {
            response.put("enterLoadingBlockedReason", "自动进入装车失败，请刷新后重试");
        }
        return response;
    }

    /** 确认派单后自动进入装车（简化流程，替代已删除的 enter-loading 接口）。 */
    private boolean tryAutoEnterLoadingAfterRouteEditConfirm(NxDisDriverRouteEntity route,
                                                             Integer operatorUserId) {
        if (route == null || route.getNxDdrId() == null) {
            return false;
        }
        if (DisRouteLoadingGateHelper.isRouteEnteredLoading(route)) {
            return true;
        }
        if (com.nongxinle.route.DisRouteDriverDepartPolicy.isRouteDeparted(route)) {
            return false;
        }
        if (countActiveAssignedTasksOnRoute(route.getNxDdrId()) <= 0) {
            return false;
        }
        enterLoadingGate(route.getNxDdrId(), operatorUserId);
        return true;
    }

    private void enterLoadingGate(Integer driverRouteId, Integer operatorUserId) {
        NxDisDriverRouteEntity gateUpdate = new NxDisDriverRouteEntity();
        gateUpdate.setNxDdrId(driverRouteId);
        gateUpdate.setNxDdrLoadingEnteredAt(new Date());
        gateUpdate.setNxDdrLoadingEnteredOperatorUserId(operatorUserId);
        gateUpdate.setNxDdrRouteStatus(com.nongxinle.route.DisRouteDriverRouteStatus.LOADING);
        nxDisDriverRouteDao.updateLoadingGate(gateUpdate);
    }

    private RouteDispatchOperationDecision evaluateReturnAllowed(NxDisShipmentTaskEntity task) {
        if (task == null || task.getNxDstId() == null) {
            return RouteDispatchOperationDecision.deny("未找到派车记录");
        }
        String status = task.getNxDstStatus();
        if (CANCELLED.equals(status)) {
            return RouteDispatchOperationDecision.deny("该店已返回沙盘或已取消");
        }
        if (DELIVERED.equals(status)) {
            return RouteDispatchOperationDecision.deny("该店已完成配送，不能取消分派");
        }
        return RouteDispatchOperationDecision.allow();
    }

    private int countActiveAssignedTasksOnRoute(Integer driverRouteId) {
        if (driverRouteId == null) {
            return 0;
        }
        List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByDriverRouteId(driverRouteId);
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task == null || CANCELLED.equals(task.getNxDstStatus())) {
                continue;
            }
            if (DisShipmentTaskStatus.ASSIGNED.equals(task.getNxDstStatus())
                    || READY_TO_GO.equals(task.getNxDstStatus())) {
                count++;
            }
        }
        return count;
    }

    public void upsertDepOnDriverRoute(SandboxStopConfirmRequest request) throws Exception {
        normalizeStopConfirmRequest(request);
        validateStopConfirmRequest(request);

        Integer depFatherId = resolveDepFatherId(request);
        NxDisShipmentTaskEntity existing = resolveExistingTask(request, depFatherId);
        if (existing != null && existing.getNxDstId() != null) {
            if (!CANCELLED.equals(existing.getNxDstStatus())) {
                if (isSameDriver(request, existing) || isReconfirmable(existing)) {
                    moveExistingTask(request, existing);
                    return;
                }
                throw new IllegalStateException(
                        "客户已在其他司机路线执行中，不能改派 deliveryStopId=" + existing.getNxDstId());
            }
        }
        createConfirmedTask(request, depFatherId);
    }

    private void createConfirmedTask(SandboxStopConfirmRequest request, Integer depFatherId)
            throws Exception {
        Integer disId = request.getDisId();
        String routeDate = resolveRouteDate(request.getRouteDate());
        String batchCode = normalizeBatch(request.getBatchCode());

        List<DisRouteOrderSnapshotDto> depOrders = loadEligibleDepOrders(
                disId, depFatherId, request.getEligibleOrderSnapshotCache());
        if (depOrders.isEmpty()) {
            throw new IllegalArgumentException(
                    "客户 depFatherId=" + depFatherId + " 当前没有 eligible live 订单");
        }

        DisRouteOrderSnapshotDto first = depOrders.get(0);
        Integer planId = request.getPlanId();
        NxDisDriverRouteEntity driverRoute;
        if (planId != null) {
            driverRoute = ensureDriverRoute(planId, request.getDriverUserId());
        } else {
            GeoPoint depot = resolveDepot(disId);
            NxDisRoutePlanEntity plan = resolveOrCreatePlan(
                    disId, routeDate, batchCode, depot, request.getOperatorUserId());
            planId = plan.getNxDrpId();
            driverRoute = ensureDriverRoute(planId, request.getDriverUserId());
        }

        upsertConfirmedTask(disId, routeDate, planId, driverRoute.getNxDdrId(),
                depFatherId, first, depOrders, request);
        syncPlanDriverCount(planId);
    }

    private void removeDepsNotOnRoute(SandboxDriverRouteEditConfirmRequest request,
                                      Integer planId,
                                      Set<Integer> targetDepIds) {
        if (planId == null || request.getDriverUserId() == null) {
            return;
        }
        NxDisDriverRouteEntity driverRoute = nxDisDriverRouteDao.queryByPlanAndDriver(
                planId, request.getDriverUserId());
        if (driverRoute == null || driverRoute.getNxDdrId() == null) {
            return;
        }
        List<NxDisShipmentTaskEntity> routeTasks =
                nxDisShipmentTaskDao.queryByDriverRouteId(driverRoute.getNxDdrId());
        if (routeTasks == null || routeTasks.isEmpty()) {
            return;
        }
        for (NxDisShipmentTaskEntity task : routeTasks) {
            if (task == null || task.getNxDstId() == null || CANCELLED.equals(task.getNxDstStatus())) {
                continue;
            }
            Integer depId = task.getNxDstDepFatherId();
            if (depId == null || targetDepIds.contains(depId)) {
                continue;
            }
            if (DELIVERED.equals(task.getNxDstStatus()) || CLOSED.equals(task.getNxDstStatus())) {
                continue;
            }
            cancelTaskFromRoute(task, request.getOperatorUserId(), "司机路线人工编辑移除");
        }
    }

    private void refreshPlanAfterConfirm(Integer planId) {
        syncPlanDriverCount(planId);
    }

    /** 确认编辑后按 stopKeys 批量写齐 routeSeq / manualStopSeq，避免老店 routeSeq 滞后。 */
    private void reorderDriverRouteStops(Integer driverRouteId, List<String> stopKeys) {
        if (driverRouteId == null || stopKeys == null || stopKeys.isEmpty()) {
            return;
        }
        List<NxDisShipmentTaskEntity> routeTasks =
                nxDisShipmentTaskDao.queryByDriverRouteId(driverRouteId);
        if (routeTasks == null || routeTasks.isEmpty()) {
            return;
        }
        Map<Integer, NxDisShipmentTaskEntity> taskByDepId = new HashMap<Integer, NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : routeTasks) {
            if (task == null || task.getNxDstDepFatherId() == null || CANCELLED.equals(task.getNxDstStatus())) {
                continue;
            }
            taskByDepId.put(task.getNxDstDepFatherId(), task);
        }
        int seq = 1;
        for (String stopKey : stopKeys) {
            Integer depId = DisRouteSandboxStopKeyUtils.parseDepFatherId(stopKey);
            if (depId == null) {
                continue;
            }
            NxDisShipmentTaskEntity task = taskByDepId.get(depId);
            if (task == null || task.getNxDstId() == null) {
                continue;
            }
            NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
            update.setNxDstId(task.getNxDstId());
            update.setNxDstManualStopSeq(seq);
            update.setNxDstRouteSeq(seq);
            update.setNxDstManualLocked(1);
            nxDisShipmentTaskDao.update(update);
            seq++;
        }
    }

    /** 已有 task：挂到当前 routeDate/batch 的 plan，不复用 task 上可能过期的 planId。 */
    private void moveExistingTask(SandboxStopConfirmRequest request,
                                  NxDisShipmentTaskEntity existing) throws Exception {
        String routeDate = resolveRouteDate(request.getRouteDate());
        String batchCode = normalizeBatch(request.getBatchCode());
        Integer planId = request.getPlanId();
        if (planId == null) {
            GeoPoint depot = resolveDepot(request.getDisId());
            NxDisRoutePlanEntity plan = resolveOrCreatePlan(
                    request.getDisId(), routeDate, batchCode, depot, request.getOperatorUserId());
            planId = plan.getNxDrpId();
        }
        NxDisDriverRouteEntity driverRoute = ensureDriverRoute(planId, request.getDriverUserId());

        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(existing.getNxDstId());
        update.setNxDstPlanId(planId);
        update.setNxDstRouteDate(routeDate);
        update.setNxDstAssignedDriverUserId(request.getDriverUserId());
        update.setNxDstSuggestedDriverUserId(request.getDriverUserId());
        update.setNxDstDriverRouteId(driverRoute.getNxDdrId());
        update.setNxDstManualLocked(1);
        update.setNxDstAssignConfirmedAt(new Date());
        update.setNxDstOperatorUserId(request.getOperatorUserId());
        update.setNxDstAdjustReason(request.getAssignReason());
        if (request.getManualStopSeq() != null) {
            update.setNxDstManualStopSeq(request.getManualStopSeq());
            update.setNxDstRouteSeq(request.getManualStopSeq());
        }
        String currentStatus = existing.getNxDstStatus();
        boolean redispatchTerminal = DELIVERED.equals(currentStatus) || CLOSED.equals(currentStatus);
        boolean staleExecutionOnLoadingRoute = IN_DELIVERY.equals(currentStatus)
                || EXCEPTION.equals(currentStatus);
        if (currentStatus == null
                || UNASSIGNED.equals(currentStatus)
                || SIMULATED.equals(currentStatus)
                || redispatchTerminal
                || staleExecutionOnLoadingRoute) {
            update.setNxDstStatus(DisShipmentTaskStatus.ASSIGNED);
            if (redispatchTerminal || staleExecutionOnLoadingRoute) {
                update.setClearDeliveryCompletion(true);
            }
        }
        nxDisShipmentTaskDao.update(update);

        Integer depFatherId = existing.getNxDstDepFatherId();
        if (depFatherId != null
                && (redispatchTerminal || staleExecutionOnLoadingRoute
                || (!DELIVERED.equals(currentStatus) && !CLOSED.equals(currentStatus)))) {
            List<DisRouteOrderSnapshotDto> depOrders = loadEligibleDepOrders(
                    request.getDisId(), depFatherId, request.getEligibleOrderSnapshotCache());
            for (DisRouteOrderSnapshotDto order : depOrders) {
                if (order != null && order.getOrderId() != null) {
                    upsertTaskItem(existing.getNxDstId(), order.getOrderId());
                }
            }
        }
    }

    /** 从司机路线移除：取消 task/item，不走 ReturnService（无 schedule/feasibility/装车副作用）。 */
    private void cancelTaskFromRoute(NxDisShipmentTaskEntity task,
                                   Integer operatorUserId,
                                   String reason) {
        if (task == null || task.getNxDstId() == null) {
            return;
        }
        Integer driverRouteId = task.getNxDstDriverRouteId();

        List<NxDisShipmentTaskItemEntity> items = nxDisShipmentTaskItemDao.queryByTaskId(task.getNxDstId());
        if (items != null) {
            for (NxDisShipmentTaskItemEntity item : items) {
                if (item == null || !ACTIVE.equals(item.getNxDstiItemStatus())) {
                    continue;
                }
                NxDisShipmentTaskItemEntity itemUpdate = new NxDisShipmentTaskItemEntity();
                itemUpdate.setNxDstiId(item.getNxDstiId());
                itemUpdate.setNxDstiItemStatus(CANCELLED);
                nxDisShipmentTaskItemDao.update(itemUpdate);
            }
        }

        NxDisShipmentTaskEntity taskUpdate = new NxDisShipmentTaskEntity();
        taskUpdate.setNxDstId(task.getNxDstId());
        taskUpdate.setNxDstStatus(CANCELLED);
        taskUpdate.setClearOpenKey(true);
        taskUpdate.setNxDstManualLocked(0);
        taskUpdate.setNxDstAssignedDriverUserId(null);
        taskUpdate.setNxDstDriverRouteId(null);
        taskUpdate.setNxDstRouteSeq(null);
        taskUpdate.setNxDstManualStopSeq(null);
        taskUpdate.setNxDstAssignConfirmedAt(null);
        taskUpdate.setNxDstOperatorUserId(operatorUserId);
        taskUpdate.setNxDstAdjustReason(reason);
        nxDisShipmentTaskDao.update(taskUpdate);
        nxDisRouteStopDao.deleteByShipmentTaskId(task.getNxDstId());

        reconcileDriverRouteAfterReturn(driverRouteId);
    }

    private void reconcileDriverRouteAfterReturn(Integer driverRouteId) {
        if (driverRouteId == null) {
            return;
        }
        if (DisRouteRouteExecutionHelper.tryMarkRouteCompleted(
                nxDisDriverRouteDao, nxDisShipmentTaskDao, driverRouteId)) {
            return;
        }
        if (countActiveTasksOnRoute(driverRouteId) > 0) {
            return;
        }
        NxDisDriverRouteEntity routeUpdate = new NxDisDriverRouteEntity();
        routeUpdate.setNxDdrId(driverRouteId);
        routeUpdate.setNxDdrStopCount(0);
        routeUpdate.setNxDdrTotalDistanceM(0L);
        routeUpdate.setNxDdrTotalDurationS(0L);
        nxDisDriverRouteDao.update(routeUpdate);
    }

    private boolean clearLoadingGate(Integer driverRouteId) {
        if (driverRouteId == null) {
            return false;
        }
        NxDisDriverRouteEntity gateUpdate = new NxDisDriverRouteEntity();
        gateUpdate.setNxDdrId(driverRouteId);
        gateUpdate.setNxDdrLoadingEnteredAt(null);
        gateUpdate.setNxDdrLoadingEnteredOperatorUserId(null);
        gateUpdate.setNxDdrRouteStatus(com.nongxinle.route.DisRouteDriverRouteStatus.DISPATCH_CONFIRMED);
        nxDisDriverRouteDao.updateLoadingGate(gateUpdate);
        return true;
    }

    private int countActiveTasksOnRoute(Integer driverRouteId) {
        if (driverRouteId == null) {
            return 0;
        }
        List<NxDisShipmentTaskEntity> routeTasks = nxDisShipmentTaskDao.queryByDriverRouteId(driverRouteId);
        if (routeTasks == null || routeTasks.isEmpty()) {
            return 0;
        }
        int activeCount = 0;
        for (NxDisShipmentTaskEntity routeTask : routeTasks) {
            if (routeTask == null || CANCELLED.equals(routeTask.getNxDstStatus())) {
                continue;
            }
            activeCount++;
        }
        return activeCount;
    }

    private NxDisShipmentTaskEntity loadTaskDetail(Integer taskId) {
        if (taskId == null) {
            return null;
        }
        NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(taskId);
        if (task == null) {
            return null;
        }
        List<NxDisShipmentTaskItemEntity> items = nxDisShipmentTaskItemDao.queryByTaskId(taskId);
        task.setItems(items != null ? items : new ArrayList<NxDisShipmentTaskItemEntity>());
        return task;
    }

    private void normalizeReturnRequest(SandboxStopReturnToSandboxRequest request) {
        if (request == null) {
            return;
        }
        request.setOperatorUserId(resolveOperatorUserId(request.getDisId(), request.getOperatorUserId()));
    }

    private static void validateReturnRequest(Integer deliveryStopId,
                                                SandboxStopReturnToSandboxRequest request) {
        if (deliveryStopId == null) {
            throw new IllegalArgumentException("deliveryStopId 不能为空");
        }
        if (request == null || request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
    }

    private static String resolveReturnRouteDate(SandboxStopReturnToSandboxRequest request,
                                                 NxDisShipmentTaskEntity task) {
        if (request.getRouteDate() != null && !request.getRouteDate().trim().isEmpty()) {
            return request.getRouteDate().trim();
        }
        if (task.getNxDstRouteDate() != null && !task.getNxDstRouteDate().trim().isEmpty()) {
            return task.getNxDstRouteDate().trim();
        }
        return formatWhatDay(0);
    }

    private String resolveReturnBatchCode(SandboxStopReturnToSandboxRequest request, Integer planId) {
        if (request.getBatchCode() != null && !request.getBatchCode().trim().isEmpty()) {
            return normalizeBatch(request.getBatchCode());
        }
        if (planId != null) {
            NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryObject(planId);
            if (plan != null && plan.getNxDrpDispatchBatch() != null) {
                return normalizeBatch(plan.getNxDrpDispatchBatch());
            }
        }
        return DisRouteDispatchBatch.MORNING;
    }

    private Integer resolveOperatorUserId(Integer disId, Integer explicitOperatorUserId) {
        if (explicitOperatorUserId != null) {
            return explicitOperatorUserId;
        }
        if (disId == null) {
            return null;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("disId", disId);
        params.put("admin", getNxDisUserAdmin());
        List<NxDistributerUserEntity> admins = nxDistributerUserDao.getAdminUserByParams(params);
        if (admins == null || admins.isEmpty()) {
            return null;
        }
        for (NxDistributerUserEntity admin : admins) {
            if (admin != null && admin.getNxDistributerUserId() != null) {
                return admin.getNxDistributerUserId();
            }
        }
        return null;
    }

    private static SandboxStopConfirmRequest buildStopConfirmRequest(
            SandboxDriverRouteEditConfirmRequest request,
            String routeDate,
            String batchCode,
            Integer depId,
            int manualStopSeq,
            String confirmReason) {
        SandboxStopConfirmRequest confirmRequest = new SandboxStopConfirmRequest();
        confirmRequest.setDisId(request.getDisId());
        confirmRequest.setRouteDate(routeDate);
        confirmRequest.setBatchCode(batchCode);
        confirmRequest.setOperatorUserId(request.getOperatorUserId());
        confirmRequest.setDriverUserId(request.getDriverUserId());
        confirmRequest.setDepFatherId(depId);
        confirmRequest.setSandboxStopKey(DisRouteSandboxStopKeyUtils.build(depId));
        confirmRequest.setManualStopSeq(manualStopSeq);
        confirmRequest.setAssignReason(resolveAssignReason(confirmReason));
        confirmRequest.setSuppressTodayResponse(true);
        confirmRequest.setDeferPlanRefresh(true);
        if (request.getLiveOrderIds() != null && !request.getLiveOrderIds().isEmpty()) {
            confirmRequest.setLiveOrderIds(request.getLiveOrderIds());
        }
        return confirmRequest;
    }

    private static String resolveAssignReason(String confirmReason) {
        if (confirmReason != null && !confirmReason.trim().isEmpty()) {
            return confirmReason.trim();
        }
        return DisRouteAssignReason.DRIVER_ROUTE_EDIT;
    }

    private void normalizeDriverRouteEditRequest(SandboxDriverRouteEditConfirmRequest request) {
        if (request == null) {
            return;
        }
        request.setOperatorUserId(resolveOperatorUserId(
                request.getDisId(), request.getOperatorUserId()));
        if (request.getBatchCode() != null) {
            request.setBatchCode(request.getBatchCode().trim().toUpperCase());
        }
    }

    private void normalizeStopConfirmRequest(SandboxStopConfirmRequest request) {
        if (request == null) {
            return;
        }
        request.setOperatorUserId(resolveOperatorUserId(
                request.getDisId(), request.getOperatorUserId()));
    }

    private static void validateDriverRouteEditRequest(SandboxDriverRouteEditConfirmRequest request) {
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        if (request.getDriverUserId() == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }
        if (request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
    }

    private static void validateStopConfirmRequest(SandboxStopConfirmRequest request) {
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        if (resolveDepFatherId(request) == null) {
            throw new IllegalArgumentException("depFatherId 或 sandboxStopKey 不能为空");
        }
        if (request.getDriverUserId() == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }
        if (request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
    }

    private static Integer resolveDepFatherId(SandboxStopConfirmRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getDepFatherId() != null) {
            return request.getDepFatherId();
        }
        return DisRouteSandboxStopKeyUtils.parseDepFatherId(request.getSandboxStopKey());
    }

    private static String resolveRouteDate(String routeDate) {
        if (routeDate != null && !routeDate.trim().isEmpty()) {
            return routeDate.trim();
        }
        return formatWhatDay(0);
    }

    private static String normalizeBatch(String batchCode) {
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return DisRouteDispatchBatch.MORNING;
        }
        return batchCode.trim().toUpperCase();
    }

    private static List<String> normalizeStopKeys(List<String> stopKeys) {
        if (stopKeys == null) {
            return new ArrayList<String>();
        }
        List<String> normalized = new ArrayList<String>();
        Set<String> seen = new HashSet<String>();
        for (String key : stopKeys) {
            if (key == null || key.trim().isEmpty()) {
                continue;
            }
            String trimmed = key.trim();
            if (seen.add(trimmed)) {
                normalized.add(trimmed);
            }
        }
        return normalized;
    }

    private static Set<Integer> parseDepIds(List<String> stopKeys) {
        Set<Integer> depIds = new LinkedHashSet<Integer>();
        for (String stopKey : stopKeys) {
            Integer depId = DisRouteSandboxStopKeyUtils.parseDepFatherId(stopKey);
            if (depId != null) {
                depIds.add(depId);
            }
        }
        return depIds;
    }

    private NxDisShipmentTaskEntity resolveExistingTask(SandboxStopConfirmRequest request,
                                                        Integer depFatherId) {
        Integer disId = request.getDisId();
        String routeDate = resolveRouteDate(request.getRouteDate());
        if (disId == null || depFatherId == null) {
            return null;
        }

        if (request.getTaskId() != null) {
            NxDisShipmentTaskEntity byId = nxDisShipmentTaskDao.queryObject(request.getTaskId());
            if (byId != null && !CANCELLED.equals(byId.getNxDstStatus())) {
                return byId;
            }
        }

        NxDisShipmentTaskEntity viaOrders = resolveViaLiveOrderIds(request.getLiveOrderIds());
        if (viaOrders != null) {
            return viaOrders;
        }

        String openKey = DisShipmentTaskOpenKeyUtils.buildOpenKey(disId, routeDate, depFatherId);
        NxDisShipmentTaskEntity byOpenKey = nxDisShipmentTaskDao.queryByOpenKey(openKey);
        if (byOpenKey != null && !CANCELLED.equals(byOpenKey.getNxDstStatus())) {
            return byOpenKey;
        }

        viaOrders = resolveViaCachedDepOrders(request.getEligibleOrderSnapshotCache(), depFatherId);
        if (viaOrders != null) {
            return viaOrders;
        }

        return resolveViaDepOnDis(disId, depFatherId);
    }

    private NxDisShipmentTaskEntity resolveViaLiveOrderIds(List<Integer> liveOrderIds) {
        if (liveOrderIds == null || liveOrderIds.isEmpty()) {
            return null;
        }
        for (Integer orderId : liveOrderIds) {
            if (orderId == null) {
                continue;
            }
            NxDisShipmentTaskItemEntity item = nxDisShipmentTaskItemDao.queryByLiveOrderId(orderId);
            if (item == null || !ACTIVE.equals(item.getNxDstiItemStatus())) {
                continue;
            }
            NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(item.getNxDstiTaskId());
            if (task != null && !CANCELLED.equals(task.getNxDstStatus())) {
                return task;
            }
        }
        return null;
    }

    private NxDisShipmentTaskEntity resolveViaCachedDepOrders(
            List<DisRouteOrderSnapshotDto> eligibleSnapshots, Integer depFatherId) {
        if (eligibleSnapshots == null || depFatherId == null) {
            return null;
        }
        List<Integer> orderIds = new ArrayList<Integer>();
        for (DisRouteOrderSnapshotDto row : filterDepOrders(eligibleSnapshots, depFatherId)) {
            if (row != null && row.getOrderId() != null) {
                orderIds.add(row.getOrderId());
            }
        }
        return resolveViaLiveOrderIds(orderIds);
    }

    /** 与分派中 compute 一致：不按送达日过滤；优先用 confirm 批次缓存。 */
    private List<DisRouteOrderSnapshotDto> loadEligibleDepOrders(Integer disId,
                                                                 Integer depFatherId,
                                                                 List<DisRouteOrderSnapshotDto> cache) {
        if (cache != null) {
            return filterDepOrders(cache, depFatherId);
        }
        return filterDepOrders(queryAllEligibleSnapshots(disId), depFatherId);
    }

    private List<DisRouteOrderSnapshotDto> queryAllEligibleSnapshots(Integer disId) {
        return nxDisRoutePlanDao.queryEligibleLiveOrderSnapshots(disId, null, null, null);
    }

    private static List<DisRouteOrderSnapshotDto> filterDepOrders(List<DisRouteOrderSnapshotDto> all,
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

    private NxDisShipmentTaskEntity resolveViaDepOnDis(Integer disId, Integer depFatherId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("disId", disId);
        return pickDepTask(nxDisShipmentTaskDao.queryList(params), depFatherId);
    }

    private static NxDisShipmentTaskEntity pickDepTask(List<NxDisShipmentTaskEntity> tasks,
                                                       Integer depFatherId) {
        if (tasks == null || depFatherId == null) {
            return null;
        }
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task == null || CANCELLED.equals(task.getNxDstStatus())) {
                continue;
            }
            if (depFatherId.equals(task.getNxDstDepFatherId())) {
                return task;
            }
        }
        return null;
    }

    private GeoPoint resolveDepot(Integer disId) {
        NxDistributerEntity dis = nxDistributerService.queryObject(disId);
        if (dis != null && isValidCoordinate(dis.getNxDistributerLan(), dis.getNxDistributerLun())) {
            return toPoint(dis.getNxDistributerLan(), dis.getNxDistributerLun());
        }
        throw new IllegalArgumentException("配送商出发点坐标无效，请配置 depot");
    }

    private NxDisRoutePlanEntity resolveOrCreatePlan(Integer disId,
                                                     String routeDate,
                                                     String batchCode,
                                                     GeoPoint depot,
                                                     Integer operatorUserId) {
        NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                disId, routeDate, batchCode, ASSIGNED);
        if (plan == null) {
            plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(disId, routeDate, batchCode, READY);
        }
        if (plan == null) {
            plan = new NxDisRoutePlanEntity();
            plan.setNxDrpDistributerId(disId);
            plan.setNxDrpPlanDate(routeDate);
            plan.setNxDrpRouteDate(routeDate);
            plan.setNxDrpDispatchBatch(batchCode);
            plan.setNxDrpStatus(ASSIGNED);
            plan.setNxDrpDepotLat(depot.getLat());
            plan.setNxDrpDepotLng(depot.getLng());
            plan.setNxDrpOptimizerType(RouteOptimizerType.BALANCED_INSERTION_2OPT.name());
            plan.setNxDrpCostProviderType(RouteCostProviderType.TENCENT_MATRIX.name());
            plan.setNxDrpDriverCount(1);
            plan.setNxDrpDispatchDate(routeDate);
            plan.setNxDrpCreatedBy(operatorUserId);
            plan.setNxDrpCreatedAt(new Date());
            plan.setNxDrpTotalDistanceM(0L);
            plan.setNxDrpTotalDurationS(0L);
            nxDisRoutePlanDao.save(plan);
            NxDisRoutePlanEntity batchUpdate = new NxDisRoutePlanEntity();
            batchUpdate.setNxDrpId(plan.getNxDrpId());
            batchUpdate.setNxDrpDispatchBatch(batchCode);
            nxDisRoutePlanDao.updateBatch(batchUpdate);
            plan.setNxDrpDispatchBatch(batchCode);
        }
        return plan;
    }

    private void syncPlanDriverCount(Integer planId) {
        List<NxDisDriverRouteEntity> routes = nxDisDriverRouteDao.queryByPlanId(planId);
        int count = routes != null ? routes.size() : 0;
        if (count <= 0) {
            return;
        }
        NxDisRoutePlanEntity update = new NxDisRoutePlanEntity();
        update.setNxDrpId(planId);
        update.setNxDrpDriverCount(count);
        nxDisRoutePlanDao.update(update);
    }

    private NxDisDriverRouteEntity ensureDriverRoute(Integer planId, Integer driverUserId) {
        NxDisDriverRouteEntity existing = nxDisDriverRouteDao.queryByPlanAndDriver(planId, driverUserId);
        if (existing != null) {
            normalizeDispatchConfirmedStatus(existing);
            return existing;
        }
        NxDisDriverRouteEntity route = new NxDisDriverRouteEntity();
        route.setNxDdrPlanId(planId);
        route.setNxDdrDriverUserId(driverUserId);
        route.setNxDdrRouteSeq(1);
        route.setNxDdrTotalDistanceM(0L);
        route.setNxDdrTotalDurationS(0L);
        route.setNxDdrStopCount(0);
        route.setNxDdrRouteStatus(com.nongxinle.route.DisRouteDriverRouteStatus.DISPATCH_CONFIRMED);
        nxDisDriverRouteDao.save(route);
        return route;
    }

    private void normalizeDispatchConfirmedStatus(NxDisDriverRouteEntity route) {
        if (route == null || route.getNxDdrId() == null) {
            return;
        }
        if (DisRouteRouteExecutionHelper.isCompletedTerminalRoute(route)) {
            nxDisDriverRouteDao.reopenForRedispatch(route.getNxDdrId(),
                    com.nongxinle.route.DisRouteDriverRouteStatus.DISPATCH_CONFIRMED);
            return;
        }
        if (route.getNxDdrLoadingEnteredAt() != null || route.getNxDdrActualDepartAt() != null) {
            return;
        }
        String status = route.getNxDdrRouteStatus();
        if (status == null
                || com.nongxinle.route.DisRouteDriverRouteStatus.LOADING.equals(status.trim().toUpperCase())) {
            NxDisDriverRouteEntity patch = new NxDisDriverRouteEntity();
            patch.setNxDdrId(route.getNxDdrId());
            patch.setNxDdrRouteStatus(com.nongxinle.route.DisRouteDriverRouteStatus.DISPATCH_CONFIRMED);
            nxDisDriverRouteDao.updateExecution(patch);
        }
    }

    private NxDisShipmentTaskEntity upsertConfirmedTask(Integer disId,
                                                        String routeDate,
                                                        Integer planId,
                                                        Integer driverRouteId,
                                                        Integer depFatherId,
                                                        DisRouteOrderSnapshotDto first,
                                                        List<DisRouteOrderSnapshotDto> depOrders,
                                                        SandboxStopConfirmRequest request) {
        String openKey = DisShipmentTaskOpenKeyUtils.buildOpenKey(disId, routeDate, depFatherId);
        NxDisShipmentTaskEntity existing = nxDisShipmentTaskDao.queryByOpenKey(openKey);
        boolean isNew = existing == null;
        NxDisShipmentTaskEntity task = isNew ? new NxDisShipmentTaskEntity() : existing;

        if (isNew) {
            task.setNxDstDistributerId(disId);
            task.setNxDstRouteDate(routeDate);
            task.setNxDstDepFatherId(depFatherId);
            task.setNxDstOpenKey(openKey);
            task.setNxDstPriorityLevel(0);
        }

        task.setNxDstRouteDate(routeDate);
        applyConfirmFields(task, first, planId, driverRouteId, depOrders, request);
        applySandboxDayTimeWindowIfPresent(disId, routeDate, depFatherId, task);
        if (isNew) {
            nxDisShipmentTaskDao.save(task);
        }
        nxDisShipmentTaskDao.update(buildConfirmUpdate(task));
        task = nxDisShipmentTaskDao.queryObject(task.getNxDstId());

        for (DisRouteOrderSnapshotDto order : depOrders) {
            upsertTaskItem(task.getNxDstId(), order.getOrderId());
        }
        return nxDisShipmentTaskDao.queryObject(task.getNxDstId());
    }

    private void applyConfirmFields(NxDisShipmentTaskEntity task,
                                    DisRouteOrderSnapshotDto first,
                                    Integer planId,
                                    Integer driverRouteId,
                                    List<DisRouteOrderSnapshotDto> depOrders,
                                    SandboxStopConfirmRequest request) {
        task.setNxDstDepName(first.getDepartmentName());
        task.setNxDstLat(first.getLat());
        task.setNxDstLng(first.getLng());
        task.setNxDstAddress(first.getAddress());
        task.setNxDstPlanId(planId);
        task.setNxDstDriverRouteId(driverRouteId);
        if (request.getManualStopSeq() != null) {
            task.setNxDstManualStopSeq(request.getManualStopSeq());
            task.setNxDstRouteSeq(request.getManualStopSeq());
        } else {
            task.setNxDstRouteSeq(nextTaskRouteSeq(driverRouteId));
        }
        task.setNxDstStatus(DisShipmentTaskStatus.ASSIGNED);
        task.setNxDstAssignedDriverUserId(request.getDriverUserId());
        task.setNxDstSuggestedDriverUserId(request.getDriverUserId());
        task.setNxDstManualLocked(1);
        task.setNxDstAssignConfirmedAt(new Date());
        task.setNxDstOperatorUserId(request.getOperatorUserId());
        task.setNxDstAssignReason(request.getAssignReason());
        task.setNxDstOrderCount(depOrders != null ? depOrders.size() : 0);
    }

    private static NxDisShipmentTaskEntity buildConfirmUpdate(NxDisShipmentTaskEntity task) {
        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(task.getNxDstId());
        update.setNxDstRouteDate(task.getNxDstRouteDate());
        update.setNxDstDepName(task.getNxDstDepName());
        update.setNxDstLat(task.getNxDstLat());
        update.setNxDstLng(task.getNxDstLng());
        update.setNxDstAddress(task.getNxDstAddress());
        update.setNxDstPlanId(task.getNxDstPlanId());
        update.setNxDstDriverRouteId(task.getNxDstDriverRouteId());
        update.setNxDstManualStopSeq(task.getNxDstManualStopSeq());
        update.setNxDstRouteSeq(task.getNxDstRouteSeq());
        update.setNxDstStatus(task.getNxDstStatus());
        update.setNxDstAssignedDriverUserId(task.getNxDstAssignedDriverUserId());
        update.setNxDstSuggestedDriverUserId(task.getNxDstSuggestedDriverUserId());
        update.setNxDstManualLocked(task.getNxDstManualLocked());
        update.setNxDstAssignConfirmedAt(task.getNxDstAssignConfirmedAt());
        update.setNxDstOperatorUserId(task.getNxDstOperatorUserId());
        update.setNxDstAssignReason(task.getNxDstAssignReason());
        update.setNxDstOrderCount(task.getNxDstOrderCount());
        return update;
    }

    private void upsertTaskItem(Integer taskId, Integer liveOrderId) {
        NxDisShipmentTaskItemEntity existing = nxDisShipmentTaskItemDao.queryByLiveOrderId(liveOrderId);
        if (existing != null) {
            NxDisShipmentTaskItemEntity update = new NxDisShipmentTaskItemEntity();
            update.setNxDstiId(existing.getNxDstiId());
            update.setNxDstiTaskId(taskId);
            update.setNxDstiItemStatus(ACTIVE);
            nxDisShipmentTaskItemDao.update(update);
        } else {
            NxDisShipmentTaskItemEntity item = new NxDisShipmentTaskItemEntity();
            item.setNxDstiTaskId(taskId);
            item.setNxDstiLiveOrderId(liveOrderId);
            item.setNxDstiItemStatus(ACTIVE);
            nxDisShipmentTaskItemDao.save(item);
        }
    }

    private int nextTaskRouteSeq(Integer driverRouteId) {
        List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByDriverRouteId(driverRouteId);
        int max = 0;
        if (tasks != null) {
            for (NxDisShipmentTaskEntity task : tasks) {
                if (!DisRouteRouteExecutionHelper.isRouteSeqActiveTask(task)) {
                    continue;
                }
                Integer seq = task.getNxDstRouteSeq() != null
                        ? task.getNxDstRouteSeq() : task.getNxDstManualStopSeq();
                if (seq != null && seq > max) {
                    max = seq;
                }
            }
        }
        return max + 1;
    }

    private void applySandboxDayTimeWindowIfPresent(Integer disId,
                                                    String routeDate,
                                                    Integer depFatherId,
                                                    NxDisShipmentTaskEntity task) {
        if (disId == null || routeDate == null || depFatherId == null || task == null) {
            return;
        }
        NxDisSandboxDayTimeWindowEntity dayOverride = nxDisSandboxDayTimeWindowDao.queryByDisRouteDep(
                disId, routeDate, depFatherId);
        if (dayOverride == null) {
            return;
        }
        task.setNxDstEarliestDeliveryTimeS(dayOverride.getNxDsdtwEarliestDeliveryTimeS());
        task.setNxDstLatestDeliveryTimeS(dayOverride.getNxDsdtwLatestDeliveryTimeS());
        task.setNxDstServiceMinutes(dayOverride.getNxDsdtwServiceMinutes());
        task.setNxDstTimeWindowOverrideFlag(1);
        task.setNxDstTimeWindowAdjustReason(dayOverride.getNxDsdtwAdjustReason());
    }

    private static boolean isSameDriver(SandboxStopConfirmRequest request,
                                        NxDisShipmentTaskEntity task) {
        if (request == null || task == null || request.getDriverUserId() == null) {
            return false;
        }
        return request.getDriverUserId().equals(task.getNxDstAssignedDriverUserId());
    }

    private static boolean isReconfirmable(NxDisShipmentTaskEntity task) {
        if (task == null) {
            return false;
        }
        String status = task.getNxDstStatus();
        return UNASSIGNED.equals(status)
                || SIMULATED.equals(status)
                || DisShipmentTaskStatus.ASSIGNED.equals(status)
                || READY_TO_GO.equals(status);
    }
}
