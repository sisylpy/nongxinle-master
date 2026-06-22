package com.nongxinle.service.impl;

import com.nongxinle.config.DisRouteDispatchSettings;
import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dao.NxDisShipmentTaskItemDao;
import com.nongxinle.dto.route.ConfirmLoadingRequest;
import com.nongxinle.dto.route.RouteDispatchOperationDecision;
import com.nongxinle.dto.route.RouteFeasibilityResult;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;
import com.nongxinle.route.DisRouteDispatchBatch;
import com.nongxinle.route.DisShipmentTaskStatus;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDepOrderBuyStatusFinishLoad;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDepOrderBuyStatusFinishOut;

@Service("disRouteSandboxConfirmLoadingService")
public class DisRouteSandboxConfirmLoadingServiceImpl implements DisRouteSandboxConfirmLoadingService {

    @Autowired
    private DisRouteSandboxTodayService disRouteSandboxTodayService;
    @Autowired
    private DisRouteDispatchOperationPolicy disRouteDispatchOperationPolicy;
    @Autowired
    private DisRouteFeasibilityService disRouteFeasibilityService;
    @Autowired
    private DisShipmentTaskService disShipmentTaskService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private NxDisShipmentTaskItemDao nxDisShipmentTaskItemDao;

    @Override
    @Transactional
    public java.util.Map<String, Object> confirmTaskLoading(Integer taskId, ConfirmLoadingRequest request) throws Exception {
        validateRequest(request);
        NxDisShipmentTaskEntity task = loadTaskWithItems(taskId);
        if (task == null) {
            throw new IllegalArgumentException("配送任务不存在");
        }
        if (request.getDisId() != null && !request.getDisId().equals(task.getNxDstDistributerId())) {
            throw new IllegalArgumentException("disId 与任务不匹配");
        }

        RouteFeasibilityResult feasibility = loadFeasibility(task);
        RouteDispatchOperationDecision decision = disRouteDispatchOperationPolicy.evaluateConfirmLoad(
                task, task.getNxDstAssignedDriverUserId(), feasibility);
        if (!decision.isAllowed()) {
            throw new IllegalStateException(decision.getBlockedReason());
        }

        promoteTaskToReadyToGo(task, request.getOperatorUserId());
        if (shouldUpdatePurchaseStatus(request)) {
            updatePurchaseStatusForTaskItems(task);
        }
        if (task.getNxDstPlanId() != null) {
            disShipmentTaskService.reconcilePlanStatus(task.getNxDstPlanId());
        }

        return disRouteSandboxTodayService.buildLoadingToday(
                task.getNxDstDistributerId(),
                resolveRouteDate(request, task),
                resolveBatchCode(request, task));
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> confirmRouteLoadingAll(Integer driverRouteId, ConfirmLoadingRequest request) throws Exception {
        validateRequest(request);
        NxDisDriverRouteEntity route = nxDisDriverRouteDao.queryObject(driverRouteId);
        if (route == null) {
            throw new IllegalArgumentException("driverRouteId=" + driverRouteId + " 不存在");
        }
        NxDisRoutePlanEntity plan = route.getNxDdrPlanId() != null
                ? nxDisRoutePlanDao.queryObject(route.getNxDdrPlanId()) : null;
        if (plan == null) {
            throw new IllegalStateException("司机路线未关联有效计划");
        }
        if (request.getDisId() != null && !request.getDisId().equals(plan.getNxDrpDistributerId())) {
            throw new IllegalArgumentException("disId 与路线计划不匹配");
        }

        List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByDriverRouteId(driverRouteId);
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalStateException("该司机路线没有可装车的配送任务");
        }

        RouteFeasibilityResult feasibility = disRouteFeasibilityService.assess(plan.getNxDrpId());
        int confirmedCount = 0;
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task == null || task.getNxDstId() == null) {
                continue;
            }
            if (DisShipmentTaskStatus.CANCELLED.equals(task.getNxDstStatus())) {
                continue;
            }
            task.setItems(nxDisShipmentTaskItemDao.queryByTaskId(task.getNxDstId()));
            RouteDispatchOperationDecision decision = disRouteDispatchOperationPolicy.evaluateConfirmLoad(
                    task, task.getNxDstAssignedDriverUserId(), feasibility);
            if (!decision.isAllowed()) {
                if (DisShipmentTaskStatus.READY_TO_GO.equals(task.getNxDstStatus())) {
                    continue;
                }
                throw new IllegalStateException(decision.getBlockedReason());
            }
            promoteTaskToReadyToGo(task, request.getOperatorUserId());
            if (shouldUpdatePurchaseStatus(request)) {
                updatePurchaseStatusForTaskItems(task);
            }
            confirmedCount++;
        }
        if (confirmedCount <= 0) {
            throw new IllegalStateException("该司机路线没有待装车确认的任务");
        }
        disShipmentTaskService.reconcilePlanStatus(plan.getNxDrpId());

        return disRouteSandboxTodayService.buildLoadingToday(
                plan.getNxDrpDistributerId(),
                resolveRouteDate(request, plan),
                resolveBatchCode(request, plan));
    }

    private void validateRequest(ConfirmLoadingRequest request) {
        if (request == null || request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
    }

    private NxDisShipmentTaskEntity loadTaskWithItems(Integer taskId) {
        NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(taskId);
        if (task != null && task.getNxDstId() != null) {
            task.setItems(nxDisShipmentTaskItemDao.queryByTaskId(task.getNxDstId()));
        }
        return task;
    }

    private RouteFeasibilityResult loadFeasibility(NxDisShipmentTaskEntity task) {
        if (task.getNxDstPlanId() == null) {
            return new RouteFeasibilityResult();
        }
        return disRouteFeasibilityService.assess(task.getNxDstPlanId());
    }

    private void promoteTaskToReadyToGo(NxDisShipmentTaskEntity task, Integer operatorUserId) {
        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(task.getNxDstId());
        update.setNxDstStatus(DisShipmentTaskStatus.READY_TO_GO);
        update.setNxDstOperatorUserId(operatorUserId);
        update.setNxDstAdjustReason("装车确认");
        nxDisShipmentTaskDao.update(update);
        task.setNxDstStatus(DisShipmentTaskStatus.READY_TO_GO);
    }

    private boolean shouldUpdatePurchaseStatus(ConfirmLoadingRequest request) {
        return request.getUpdatePurchaseStatus() == null || Boolean.TRUE.equals(request.getUpdatePurchaseStatus());
    }

    private void updatePurchaseStatusForTaskItems(NxDisShipmentTaskEntity task) {
        if (task == null || task.getItems() == null) {
            return;
        }
        List<Integer> updated = new ArrayList<Integer>();
        for (NxDisShipmentTaskItemEntity item : task.getItems()) {
            if (item == null || item.getNxDstiLiveOrderId() == null) {
                continue;
            }
            Integer orderId = item.getNxDstiLiveOrderId();
            if (updated.contains(orderId)) {
                continue;
            }
            NxDepartmentOrdersEntity order = nxDepartmentOrdersService.queryObject(orderId);
            if (order == null) {
                continue;
            }
            Integer purchaseStatus = order.getNxDoPurchaseStatus();
            if (purchaseStatus != null && purchaseStatus.equals(getNxDepOrderBuyStatusFinishOut())) {
                order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishLoad());
                nxDepartmentOrdersService.update(order);
            }
            updated.add(orderId);
        }
    }

    private String resolveRouteDate(ConfirmLoadingRequest request, NxDisShipmentTaskEntity task) {
        if (request.getRouteDate() != null && !request.getRouteDate().trim().isEmpty()) {
            return request.getRouteDate().trim();
        }
        if (task.getNxDstRouteDate() != null && !task.getNxDstRouteDate().trim().isEmpty()) {
            return task.getNxDstRouteDate().trim();
        }
        return formatWhatDay(0);
    }

    private String resolveBatchCode(ConfirmLoadingRequest request, NxDisShipmentTaskEntity task) {
        if (request.getBatchCode() != null && !request.getBatchCode().trim().isEmpty()) {
            return request.getBatchCode().trim().toUpperCase();
        }
        return DisRouteDispatchBatch.MORNING;
    }

    private String resolveRouteDate(ConfirmLoadingRequest request, NxDisRoutePlanEntity plan) {
        if (request.getRouteDate() != null && !request.getRouteDate().trim().isEmpty()) {
            return request.getRouteDate().trim();
        }
        if (plan.getNxDrpRouteDate() != null && !plan.getNxDrpRouteDate().trim().isEmpty()) {
            return plan.getNxDrpRouteDate().trim();
        }
        return formatWhatDay(0);
    }

    private String resolveBatchCode(ConfirmLoadingRequest request, NxDisRoutePlanEntity plan) {
        if (request.getBatchCode() != null && !request.getBatchCode().trim().isEmpty()) {
            return request.getBatchCode().trim().toUpperCase();
        }
        if (plan.getNxDrpDispatchBatch() != null && !plan.getNxDrpDispatchBatch().trim().isEmpty()) {
            return plan.getNxDrpDispatchBatch().trim().toUpperCase();
        }
        return DisRouteDispatchBatch.MORNING;
    }
}
