package com.nongxinle.service.impl;

import com.nongxinle.dao.*;
import com.nongxinle.dto.route.*;
import com.nongxinle.entity.*;
import com.nongxinle.route.DisRouteDispatchBatch;
import com.nongxinle.route.DisRouteOrderArriveDateHelper;
import com.nongxinle.route.DisRouteDispatchOperatorResolver;
import com.nongxinle.route.DisRouteSandboxStopKeyUtils;
import com.nongxinle.route.DisShipmentTaskOpenKeyUtils;
import com.nongxinle.route.DisShipmentTaskStatus;
import com.nongxinle.route.RouteCostProviderType;
import com.nongxinle.route.RouteOptimizerType;
import com.nongxinle.route.model.GeoPoint;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.nongxinle.route.DisRoutePlanStatus.ASSIGNED;
import static com.nongxinle.route.DisRoutePlanStatus.READY;
import static com.nongxinle.route.DisShipmentTaskItemStatus.ACTIVE;
import static com.nongxinle.route.RouteCoordinateUtils.isValidCoordinate;
import static com.nongxinle.route.RouteCoordinateUtils.toPoint;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

/**
 * Phase 3a：确认沙盘客户装车/分派（无需预先存在 taskId）。
 */
@Service("disRouteSandboxConfirmService")
public class DisRouteSandboxConfirmServiceImpl implements DisRouteSandboxConfirmService {

    @Autowired
    private DisRouteSandboxTodayService disRouteSandboxTodayService;
    @Autowired
    private DisShipmentTaskService disShipmentTaskService;
    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private NxDisShipmentTaskItemDao nxDisShipmentTaskItemDao;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private DisRoutePlanPresentationHelper disRoutePlanPresentationHelper;
    @Autowired
    private DisRouteScheduleService disRouteScheduleService;
    @Autowired
    private DisRouteFeasibilityService disRouteFeasibilityService;
    @Autowired
    private DisRouteDispatchSnapshotHelper disRouteDispatchSnapshotHelper;
    @Autowired
    private DisDriverDutyService disDriverDutyService;
    @Autowired
    private DisRouteDispatchOperatorResolver disRouteDispatchOperatorResolver;

    @Override
    @Transactional
    public Map<String, Object> confirmStop(SandboxStopConfirmRequest request) throws Exception {
        normalizeConfirmRequest(request);
        validateConfirmRequest(request);

        if (request.getTaskId() != null) {
            AssignTaskRequest assignRequest = new AssignTaskRequest();
            assignRequest.setTaskId(request.getTaskId());
            assignRequest.setAssignedDriverUserId(request.getDriverUserId());
            assignRequest.setOperatorUserId(request.getOperatorUserId());
            assignRequest.setManualStopSeq(request.getManualStopSeq());
            assignRequest.setAssignReason(request.getAssignReason());
            disShipmentTaskService.assignTask(assignRequest);
            Map<String, Object> result = disRouteSandboxTodayService.buildToday(
                    request.getDisId(), resolveRouteDate(request.getRouteDate()), normalizeBatch(request.getBatchCode()),
                    request.getOperatorUserId());
            result.put("deliveryStopId", request.getTaskId());
            return result;
        }

        Integer depFatherId = resolveDepFatherId(request);
        String routeDate = resolveRouteDate(request.getRouteDate());
        String batchCode = normalizeBatch(request.getBatchCode());
        Integer disId = request.getDisId();

        List<DisRouteOrderSnapshotDto> depOrders = loadEligibleDepOrders(disId, depFatherId);
        validateLiveOrderIds(request.getLiveOrderIds(), depOrders);
        rejectBillConflict(depOrders);

        DisRouteOrderSnapshotDto first = depOrders.get(0);
        GeoPoint depot = resolveDepot(disId);
        NxDisRoutePlanEntity plan = resolveOrCreatePlan(
                disId, routeDate, batchCode, depot, request.getOperatorUserId());
        NxDisDriverRouteEntity driverRoute = ensureDriverRoute(plan.getNxDrpId(), request.getDriverUserId());

        NxDisShipmentTaskEntity task = upsertConfirmedTask(
                disId, routeDate, plan.getNxDrpId(), driverRoute.getNxDdrId(), depFatherId, first, depOrders, request);

        syncPlanDriverCount(plan.getNxDrpId());
        disRoutePlanPresentationHelper.refreshPlanPresentation(plan.getNxDrpId());
        disRouteScheduleService.computeSchedule(plan.getNxDrpId());
        disRouteFeasibilityService.assess(plan.getNxDrpId());
        disShipmentTaskService.reconcilePlanStatus(plan.getNxDrpId());

        Map<String, Object> result = disRouteSandboxTodayService.buildToday(
                disId, routeDate, batchCode, request.getOperatorUserId());
        result.put("deliveryStopId", task.getNxDstId());
        return result;
    }

    private void normalizeConfirmRequest(SandboxStopConfirmRequest request) {
        if (request == null) {
            return;
        }
        request.setOperatorUserId(disRouteDispatchOperatorResolver.resolve(
                request.getDisId(), request.getOperatorUserId()));
    }

    private void validateConfirmRequest(SandboxStopConfirmRequest request) {
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        if (request.getTaskId() == null) {
            if (resolveDepFatherId(request) == null) {
                throw new IllegalArgumentException("depFatherId 或 sandboxStopKey 不能为空");
            }
            if (request.getDriverUserId() == null) {
                throw new IllegalArgumentException("driverUserId 不能为空");
            }
        }
        if (request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
    }

    private Integer resolveDepFatherId(SandboxStopConfirmRequest request) {
        if (request.getDepFatherId() != null) {
            return request.getDepFatherId();
        }
        return DisRouteSandboxStopKeyUtils.parseDepFatherId(request.getSandboxStopKey());
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

    private List<DisRouteOrderSnapshotDto> loadEligibleDepOrders(Integer disId,
                                                                 Integer depFatherId) {
        List<DisRouteOrderSnapshotDto> all = nxDisRoutePlanDao.queryEligibleLiveOrderSnapshots(
                disId, null, null);
        List<DisRouteOrderSnapshotDto> depOrders = new ArrayList<DisRouteOrderSnapshotDto>();
        for (DisRouteOrderSnapshotDto row : all) {
            if (depFatherId.equals(row.getDepartmentId())) {
                depOrders.add(row);
            }
        }
        if (depOrders.isEmpty()) {
            throw new IllegalArgumentException("客户 depFatherId=" + depFatherId + " 当前没有 eligible live 订单");
        }
        return depOrders;
    }

    private void validateLiveOrderIds(List<Integer> liveOrderIds, List<DisRouteOrderSnapshotDto> depOrders) {
        if (liveOrderIds == null || liveOrderIds.isEmpty()) {
            return;
        }
        Set<Integer> eligible = new HashSet<Integer>();
        for (DisRouteOrderSnapshotDto row : depOrders) {
            eligible.add(row.getOrderId());
        }
        for (Integer orderId : liveOrderIds) {
            if (orderId != null && !eligible.contains(orderId)) {
                throw new IllegalArgumentException("liveOrderId=" + orderId + " 不在当前 eligible 订单中");
            }
        }
    }

    private void rejectBillConflict(List<DisRouteOrderSnapshotDto> depOrders) {
        for (DisRouteOrderSnapshotDto row : depOrders) {
            NxDisShipmentTaskItemEntity existing = nxDisShipmentTaskItemDao.queryByLiveOrderId(row.getOrderId());
            if (existing == null) {
                continue;
            }
            if (!ACTIVE.equals(existing.getNxDstiItemStatus())) {
                continue;
            }
            NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(existing.getNxDstiTaskId());
            if (task != null && isTaskProtected(task)) {
                throw new IllegalStateException("客户已有确认执行记录 deliveryStopId=" + task.getNxDstId());
            }
        }
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
            persistPlanBatchCode(plan.getNxDrpId(), batchCode);
            plan.setNxDrpDispatchBatch(batchCode);
        }
        return plan;
    }

    private void persistPlanBatchCode(Integer planId, String batchCode) {
        NxDisRoutePlanEntity batchUpdate = new NxDisRoutePlanEntity();
        batchUpdate.setNxDrpId(planId);
        batchUpdate.setNxDrpDispatchBatch(batchCode);
        nxDisRoutePlanDao.updateBatch(batchUpdate);
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
        if (route.getNxDdrLoadingEnteredAt() != null) {
            return;
        }
        if (route.getNxDdrActualDepartAt() != null) {
            return;
        }
        String status = route.getNxDdrRouteStatus();
        if (status == null
                || com.nongxinle.route.DisRouteDriverRouteStatus.LOADING.equals(status.trim().toUpperCase())) {
            route.setNxDdrRouteStatus(com.nongxinle.route.DisRouteDriverRouteStatus.DISPATCH_CONFIRMED);
            NxDisDriverRouteEntity patch = new NxDisDriverRouteEntity();
            patch.setNxDdrId(route.getNxDdrId());
            patch.setNxDdrRouteStatus(route.getNxDdrRouteStatus());
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

        applyConfirmFields(task, first, planId, driverRouteId, request);
        if (isNew) {
            nxDisShipmentTaskDao.save(task);
        }
        nxDisShipmentTaskDao.update(buildConfirmUpdate(task));
        task = nxDisShipmentTaskDao.queryObject(task.getNxDstId());

        for (DisRouteOrderSnapshotDto order : depOrders) {
            upsertTaskItem(task.getNxDstId(), order.getOrderId());
        }

        task = nxDisShipmentTaskDao.queryObject(task.getNxDstId());
        task.setItems(nxDisShipmentTaskItemDao.queryByTaskId(task.getNxDstId()));
        disRouteDispatchSnapshotHelper.refreshTaskSnapshot(task, true);
        NxDisShipmentTaskEntity snapUpdate = disRouteDispatchSnapshotHelper.buildTaskUpdateFromSnapshot(task, true);
        nxDisShipmentTaskDao.update(snapUpdate);
        return nxDisShipmentTaskDao.queryObject(task.getNxDstId());
    }

    private void applyConfirmFields(NxDisShipmentTaskEntity task,
                                    DisRouteOrderSnapshotDto first,
                                    Integer planId,
                                    Integer driverRouteId,
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
    }

    private NxDisShipmentTaskEntity buildConfirmUpdate(NxDisShipmentTaskEntity task) {
        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(task.getNxDstId());
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
                Integer seq = task.getNxDstRouteSeq() != null ? task.getNxDstRouteSeq() : task.getNxDstManualStopSeq();
                if (seq != null && seq > max) {
                    max = seq;
                }
            }
        }
        return max + 1;
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
}
