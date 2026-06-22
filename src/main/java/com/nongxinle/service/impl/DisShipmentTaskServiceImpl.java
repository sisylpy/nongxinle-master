package com.nongxinle.service.impl;

import com.nongxinle.dao.*;
import com.nongxinle.dto.route.AssignTaskRequest;
import com.nongxinle.dto.route.BillPrintedOrderRef;
import com.nongxinle.dto.route.MoveTaskRequest;
import com.nongxinle.dto.route.UnlockTaskRequest;
import com.nongxinle.entity.*;
import com.nongxinle.route.DisRoutePlanStatus;
import com.nongxinle.route.DisShipmentTaskStatus;
import com.nongxinle.service.DisRouteDispatchOperationPolicy;
import com.nongxinle.service.DisShipmentTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.nongxinle.route.DisRoutePlanStatus.READY;
import static com.nongxinle.route.DisShipmentTaskItemStatus.ACTIVE;

@Service("disShipmentTaskService")
public class DisShipmentTaskServiceImpl implements DisShipmentTaskService {

    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private NxDisShipmentTaskItemDao nxDisShipmentTaskItemDao;
    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private DisRoutePlanPresentationHelper disRoutePlanPresentationHelper;
    @Autowired
    private DisRouteDispatchOperationPolicy disRouteDispatchOperationPolicy;
    @Autowired
    private DisRouteShipmentTaskItemOrderResolver disRouteShipmentTaskItemOrderResolver;

    @Override
    public NxDisShipmentTaskEntity queryTaskDetail(Integer taskId) {
        NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(taskId);
        if (task == null) {
            return null;
        }
        List<NxDisShipmentTaskItemEntity> items = nxDisShipmentTaskItemDao.queryByTaskId(taskId);
        disRouteShipmentTaskItemOrderResolver.enrichItems(items);
        task.setItems(items);
        return task;
    }

    @Override
    public List<NxDisShipmentTaskEntity> queryTasksByPlanId(Integer planId) {
        List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByPlanId(planId);
        if (tasks == null || tasks.isEmpty()) {
            return tasks;
        }
        attachItems(tasks);
        return tasks;
    }

    @Override
    @Transactional
    public NxDisShipmentTaskEntity assignTask(AssignTaskRequest request) {
        validateAssignRequest(request);
        NxDisShipmentTaskEntity task = requireTaskWithItems(request.getTaskId());
        disRouteDispatchOperationPolicy.requireAssign(task, request.getAssignedDriverUserId());

        Date now = new Date();
        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(task.getNxDstId());
        update.setNxDstStatus(DisShipmentTaskStatus.ASSIGNED);
        update.setNxDstAssignedDriverUserId(request.getAssignedDriverUserId());
        update.setNxDstManualLocked(1);
        update.setNxDstAssignConfirmedAt(now);
        update.setNxDstOperatorUserId(request.getOperatorUserId());
        update.setNxDstAssignReason(request.getAssignReason());
        if (request.getManualStopSeq() != null) {
            update.setNxDstManualStopSeq(request.getManualStopSeq());
        }
        nxDisShipmentTaskDao.update(update);

        syncTaskToDriverRoute(task, request.getAssignedDriverUserId(), request.getManualStopSeq());

        if (task.getNxDstPlanId() != null) {
            disRoutePlanPresentationHelper.refreshPlanPresentation(task.getNxDstPlanId());
            reconcilePlanStatus(task.getNxDstPlanId());
        }
        return queryTaskDetail(task.getNxDstId());
    }

    @Override
    @Transactional
    public NxDisShipmentTaskEntity moveTask(MoveTaskRequest request) {
        validateMoveRequest(request);
        NxDisShipmentTaskEntity task = requireTaskWithItems(request.getTaskId());
        disRouteDispatchOperationPolicy.requireMove(task, request.getAssignedDriverUserId());

        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(task.getNxDstId());
        update.setNxDstAssignedDriverUserId(request.getAssignedDriverUserId());
        update.setNxDstManualLocked(1);
        update.setNxDstOperatorUserId(request.getOperatorUserId());
        update.setNxDstAdjustReason(request.getAdjustReason());
        if (request.getManualStopSeq() != null) {
            update.setNxDstManualStopSeq(request.getManualStopSeq());
        }
        if (!DisShipmentTaskStatus.ASSIGNED.equals(task.getNxDstStatus())
                && !DisShipmentTaskStatus.READY_TO_GO.equals(task.getNxDstStatus())) {
            update.setNxDstStatus(DisShipmentTaskStatus.ASSIGNED);
        }
        nxDisShipmentTaskDao.update(update);

        syncTaskToDriverRoute(task, request.getAssignedDriverUserId(), request.getManualStopSeq());

        if (task.getNxDstPlanId() != null) {
            disRoutePlanPresentationHelper.refreshPlanPresentation(task.getNxDstPlanId());
            reconcilePlanStatus(task.getNxDstPlanId());
        }
        return queryTaskDetail(task.getNxDstId());
    }

    @Override
    @Transactional
    public NxDisShipmentTaskEntity unlockTask(UnlockTaskRequest request) {
        if (request == null || request.getTaskId() == null) {
            throw new IllegalArgumentException("taskId 不能为空");
        }
        if (request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }

        NxDisShipmentTaskEntity task = requireTaskWithItems(request.getTaskId());
        disRouteDispatchOperationPolicy.requireUnlock(task);

        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(task.getNxDstId());
        update.setNxDstManualLocked(0);
        update.setNxDstOperatorUserId(request.getOperatorUserId());
        if (request.getAdjustReason() != null) {
            update.setNxDstAdjustReason(request.getAdjustReason());
        }
        nxDisShipmentTaskDao.update(update);

        if (task.getNxDstPlanId() != null) {
            disRoutePlanPresentationHelper.refreshPlanPresentation(task.getNxDstPlanId());
            reconcilePlanStatus(task.getNxDstPlanId());
        }
        return queryTaskDetail(task.getNxDstId());
    }

    @Override
    @Transactional
    public void onBillPrinted(Integer billId, List<BillPrintedOrderRef> refs) {
        if (billId == null) {
            throw new IllegalArgumentException("billId 不能为空");
        }
        if (refs == null || refs.isEmpty()) {
            return;
        }

        Set<Integer> affectedTaskIds = new LinkedHashSet<Integer>();
        for (BillPrintedOrderRef ref : refs) {
            if (ref == null || ref.getLiveOrderId() == null) {
                continue;
            }
            NxDisShipmentTaskItemEntity item = nxDisShipmentTaskItemDao.queryByLiveOrderId(ref.getLiveOrderId());
            if (item == null) {
                continue;
            }
            if (item.getNxDstiBillId() != null && !billId.equals(item.getNxDstiBillId())) {
                throw new IllegalStateException("liveOrderId=" + ref.getLiveOrderId()
                        + " 已绑定其他 billId=" + item.getNxDstiBillId());
            }

            NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(item.getNxDstiTaskId());
            if (task != null) {
                rejectBillMutationOnTerminalTask(task);
            }

            NxDisShipmentTaskItemEntity itemUpdate = new NxDisShipmentTaskItemEntity();
            itemUpdate.setNxDstiId(item.getNxDstiId());
            itemUpdate.setNxDstiBillId(billId);
            if (ref.getHistoryOrderId() != null) {
                itemUpdate.setNxDstiHistoryOrderId(ref.getHistoryOrderId());
            }
            nxDisShipmentTaskItemDao.update(itemUpdate);
            affectedTaskIds.add(item.getNxDstiTaskId());
        }

        Set<Integer> affectedPlanIds = new LinkedHashSet<Integer>();
        for (Integer taskId : affectedTaskIds) {
            NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(taskId);
            if (task != null && task.getNxDstPlanId() != null) {
                affectedPlanIds.add(task.getNxDstPlanId());
            }
        }
        for (Integer planId : affectedPlanIds) {
            reconcilePlanStatus(planId);
        }
    }

    @Override
    @Transactional
    public void onBillReverted(Integer billId) {
        if (billId == null) {
            throw new IllegalArgumentException("billId 不能为空");
        }
        List<NxDisShipmentTaskItemEntity> items = nxDisShipmentTaskItemDao.queryByBillId(billId);
        if (items == null || items.isEmpty()) {
            return;
        }

        Set<Integer> affectedTaskIds = new LinkedHashSet<Integer>();
        for (NxDisShipmentTaskItemEntity item : items) {
            NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(item.getNxDstiTaskId());
            if (task != null) {
                rejectBillMutationOnTerminalTask(task);
            }

            NxDisShipmentTaskItemEntity itemUpdate = new NxDisShipmentTaskItemEntity();
            itemUpdate.setNxDstiId(item.getNxDstiId());
            itemUpdate.setClearBillId(true);
            itemUpdate.setClearHistoryOrderId(true);
            nxDisShipmentTaskItemDao.update(itemUpdate);
            affectedTaskIds.add(item.getNxDstiTaskId());
        }

        Set<Integer> affectedPlanIds = new LinkedHashSet<Integer>();
        for (Integer taskId : affectedTaskIds) {
            NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(taskId);
            if (task != null && task.getNxDstPlanId() != null) {
                affectedPlanIds.add(task.getNxDstPlanId());
            }
        }
        for (Integer planId : affectedPlanIds) {
            reconcilePlanStatus(planId);
        }
    }

    @Override
    @Transactional
    public void reconcilePlanStatus(Integer planId) {
        if (planId == null) {
            return;
        }
        NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryObject(planId);
        if (plan == null) {
            throw new IllegalArgumentException("路线计划不存在: " + planId);
        }
        if (DisRoutePlanStatus.CANCELLED.equals(plan.getNxDrpStatus())) {
            return;
        }

        List<NxDisShipmentTaskEntity> allTasks = nxDisShipmentTaskDao.queryByPlanId(planId);
        if (allTasks == null) {
            allTasks = Collections.emptyList();
        }
        attachItems(allTasks);

        List<NxDisShipmentTaskEntity> activeTasks = new ArrayList<NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : allTasks) {
            if (isActiveTask(task)) {
                activeTasks.add(task);
            }
        }

        String derivedStatus = derivePlanStatus(activeTasks);
        if (derivedStatus.equals(plan.getNxDrpStatus())) {
            return;
        }

        NxDisRoutePlanEntity update = new NxDisRoutePlanEntity();
        update.setNxDrpId(planId);
        update.setNxDrpStatus(derivedStatus);
        if (READY.equals(derivedStatus)) {
            update.setNxDrpReadyAt(new Date());
            if (plan.getNxDrpDispatchDate() == null) {
                update.setNxDrpDispatchDate(plan.getNxDrpRouteDate() != null
                        ? plan.getNxDrpRouteDate() : plan.getNxDrpPlanDate());
            }
        }
        nxDisRoutePlanDao.update(update);
    }

    private String derivePlanStatus(List<NxDisShipmentTaskEntity> activeTasks) {
        if (activeTasks.isEmpty()) {
            return DisRoutePlanStatus.SIMULATED;
        }

        int inDeliveryCount = 0;
        int readyCount = 0;
        int assignedOrReadyCount = 0;
        for (NxDisShipmentTaskEntity task : activeTasks) {
            String status = task.getNxDstStatus();
            if (DisShipmentTaskStatus.IN_DELIVERY.equals(status)
                    || DisShipmentTaskStatus.DELIVERED.equals(status)) {
                inDeliveryCount++;
            }
            if (DisShipmentTaskStatus.READY_TO_GO.equals(status)) {
                readyCount++;
                assignedOrReadyCount++;
            } else if (DisShipmentTaskStatus.ASSIGNED.equals(status)) {
                assignedOrReadyCount++;
            }
        }

        if (inDeliveryCount > 0) {
            return DisRoutePlanStatus.ASSIGNED;
        }
        if (readyCount == activeTasks.size()) {
            return DisRoutePlanStatus.READY;
        }
        if (assignedOrReadyCount > 0) {
            return DisRoutePlanStatus.ASSIGNED;
        }
        return DisRoutePlanStatus.SIMULATED;
    }

    private void promoteTaskToReadyToGoIfEligible(Integer taskId) {
        // Phase 3a.1：bill 打印不作 READY_TO_GO 自动晋升门槛；出发由老板/司机确认操作驱动。
    }

    private void revertTaskFromReadyToGoIfNeeded(Integer taskId) {
        // Phase 3a.1：bill 回退不自动降级 READY_TO_GO。
    }

    private void rejectBillMutationOnTerminalTask(NxDisShipmentTaskEntity task) {
        String status = task.getNxDstStatus();
        if (DisShipmentTaskStatus.CANCELLED.equals(status)
                || DisShipmentTaskStatus.CLOSED.equals(status)
                || DisShipmentTaskStatus.IN_DELIVERY.equals(status)
                || DisShipmentTaskStatus.DELIVERED.equals(status)) {
            throw new IllegalStateException("task " + task.getNxDstId() + " 状态 " + status + " 不允许 bill 回填/回退");
        }
    }

    private boolean isActiveTask(NxDisShipmentTaskEntity task) {
        if (DisShipmentTaskStatus.CANCELLED.equals(task.getNxDstStatus())) {
            return false;
        }
        List<NxDisShipmentTaskItemEntity> items = task.getItems();
        if (items == null || items.isEmpty()) {
            return true;
        }
        for (NxDisShipmentTaskItemEntity item : items) {
            if (ACTIVE.equals(item.getNxDstiItemStatus())) {
                return true;
            }
        }
        return false;
    }

    /** Phase 3a.1：task 挂 driver_route，不再写 route_stop。 */
    private void syncTaskToDriverRoute(NxDisShipmentTaskEntity task,
                                       Integer driverUserId,
                                       Integer manualStopSeq) {
        if (task.getNxDstPlanId() == null || driverUserId == null) {
            return;
        }
        NxDisDriverRouteEntity driverRoute = nxDisDriverRouteDao.queryByPlanAndDriver(
                task.getNxDstPlanId(), driverUserId);
        if (driverRoute == null) {
            return;
        }
        int routeSeq = manualStopSeq != null ? manualStopSeq : nextTaskRouteSeq(driverRoute.getNxDdrId());
        NxDisShipmentTaskEntity taskUpdate = new NxDisShipmentTaskEntity();
        taskUpdate.setNxDstId(task.getNxDstId());
        taskUpdate.setNxDstDriverRouteId(driverRoute.getNxDdrId());
        taskUpdate.setNxDstRouteSeq(routeSeq);
        if (manualStopSeq != null) {
            taskUpdate.setNxDstManualStopSeq(manualStopSeq);
        }
        nxDisShipmentTaskDao.update(taskUpdate);
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

    private NxDisShipmentTaskEntity requireTask(Integer taskId) {
        NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(taskId);
        if (task == null) {
            throw new IllegalArgumentException("配送任务不存在: " + taskId);
        }
        return task;
    }

    private NxDisShipmentTaskEntity requireTaskWithItems(Integer taskId) {
        NxDisShipmentTaskEntity task = requireTask(taskId);
        List<NxDisShipmentTaskItemEntity> items = nxDisShipmentTaskItemDao.queryByTaskId(taskId);
        disRouteShipmentTaskItemOrderResolver.enrichItems(items);
        task.setItems(items);
        return task;
    }

    private void validateAssignRequest(AssignTaskRequest request) {
        if (request == null || request.getTaskId() == null) {
            throw new IllegalArgumentException("taskId 不能为空");
        }
        if (request.getAssignedDriverUserId() == null) {
            throw new IllegalArgumentException("assignedDriverUserId 不能为空");
        }
        if (request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
    }

    private void validateMoveRequest(MoveTaskRequest request) {
        if (request == null || request.getTaskId() == null) {
            throw new IllegalArgumentException("taskId 不能为空");
        }
        if (request.getAssignedDriverUserId() == null) {
            throw new IllegalArgumentException("assignedDriverUserId 不能为空");
        }
        if (request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
        if (request.getAdjustReason() == null || request.getAdjustReason().trim().isEmpty()) {
            throw new IllegalArgumentException("adjustReason 不能为空");
        }
    }

    private void attachItems(List<NxDisShipmentTaskEntity> tasks) {
        List<Integer> taskIds = new ArrayList<Integer>();
        for (NxDisShipmentTaskEntity task : tasks) {
            taskIds.add(task.getNxDstId());
        }
        List<NxDisShipmentTaskItemEntity> allItems = nxDisShipmentTaskItemDao.queryByTaskIds(taskIds);
        disRouteShipmentTaskItemOrderResolver.enrichItems(allItems);
        for (NxDisShipmentTaskEntity task : tasks) {
            List<NxDisShipmentTaskItemEntity> items = new ArrayList<NxDisShipmentTaskItemEntity>();
            for (NxDisShipmentTaskItemEntity item : allItems) {
                if (task.getNxDstId().equals(item.getNxDstiTaskId())) {
                    items.add(item);
                }
            }
            task.setItems(items);
        }
    }
}
