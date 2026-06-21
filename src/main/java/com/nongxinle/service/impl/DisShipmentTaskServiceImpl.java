package com.nongxinle.service.impl;

import com.nongxinle.dao.*;
import com.nongxinle.dto.route.AssignTaskRequest;
import com.nongxinle.dto.route.BillPrintedOrderRef;
import com.nongxinle.dto.route.MoveTaskRequest;
import com.nongxinle.dto.route.UnlockTaskRequest;
import com.nongxinle.entity.*;
import com.nongxinle.route.DisRoutePlanStatus;
import com.nongxinle.route.DisShipmentTaskOpenKeyUtils;
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
    private NxDisRouteStopDao nxDisRouteStopDao;
    @Autowired
    private DisRoutePlanPresentationHelper disRoutePlanPresentationHelper;
    @Autowired
    private DisRouteDispatchOperationPolicy disRouteDispatchOperationPolicy;

    @Override
    public NxDisShipmentTaskEntity queryTaskDetail(Integer taskId) {
        NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(taskId);
        if (task == null) {
            return null;
        }
        task.setItems(nxDisShipmentTaskItemDao.queryByTaskId(taskId));
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

        syncStopToDriverRoute(task, request.getAssignedDriverUserId(), request.getManualStopSeq());

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

        syncStopToDriverRoute(task, request.getAssignedDriverUserId(), request.getManualStopSeq());

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
            promoteTaskToReadyToGoIfEligible(taskId);
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
            revertTaskFromReadyToGoIfNeeded(taskId);
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

        int readyCount = 0;
        int assignedOrReadyCount = 0;
        for (NxDisShipmentTaskEntity task : activeTasks) {
            String status = task.getNxDstStatus();
            if (DisShipmentTaskStatus.READY_TO_GO.equals(status)) {
                readyCount++;
                assignedOrReadyCount++;
            } else if (DisShipmentTaskStatus.ASSIGNED.equals(status)) {
                assignedOrReadyCount++;
            }
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
        NxDisShipmentTaskEntity task = queryTaskDetail(taskId);
        if (task == null || !allActiveItemsHaveBill(task)) {
            return;
        }
        try {
            disRouteDispatchOperationPolicy.requireBillReadyPromotion(task);
        } catch (IllegalStateException ex) {
            return;
        }
        if (!isTaskReadyForDeparturePromotion(task)) {
            return;
        }
        if (DisShipmentTaskStatus.READY_TO_GO.equals(task.getNxDstStatus())) {
            if (task.getNxDstOpenKey() != null) {
                nxDisShipmentTaskDao.clearOpenKey(taskId);
            }
            return;
        }

        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(taskId);
        update.setNxDstStatus(DisShipmentTaskStatus.READY_TO_GO);
        update.setClearOpenKey(true);
        nxDisShipmentTaskDao.update(update);
    }

    /** bill 齐 + 已人工 assign（assignedDriverUserId 非空，status 至少 ASSIGNED）才可 READY_TO_GO。 */
    private boolean isTaskReadyForDeparturePromotion(NxDisShipmentTaskEntity task) {
        if (task.getNxDstAssignedDriverUserId() == null) {
            return false;
        }
        String status = task.getNxDstStatus();
        if (DisShipmentTaskStatus.CANCELLED.equals(status)
                || DisShipmentTaskStatus.CLOSED.equals(status)
                || DisShipmentTaskStatus.IN_DELIVERY.equals(status)
                || DisShipmentTaskStatus.DELIVERED.equals(status)) {
            return false;
        }
        return DisShipmentTaskStatus.ASSIGNED.equals(status)
                || DisShipmentTaskStatus.READY_TO_GO.equals(status);
    }

    private void revertTaskFromReadyToGoIfNeeded(Integer taskId) {
        NxDisShipmentTaskEntity task = queryTaskDetail(taskId);
        if (task == null || !DisShipmentTaskStatus.READY_TO_GO.equals(task.getNxDstStatus())) {
            return;
        }
        if (allActiveItemsHaveBill(task)) {
            return;
        }

        String openKey = DisShipmentTaskOpenKeyUtils.buildOpenKey(
                task.getNxDstDistributerId(), task.getNxDstRouteDate(), task.getNxDstDepFatherId());
        NxDisShipmentTaskEntity conflict = nxDisShipmentTaskDao.queryByOpenKey(openKey);
        if (conflict != null && !conflict.getNxDstId().equals(taskId)) {
            throw new IllegalStateException("无法恢复 task " + taskId + " 的 open_key=" + openKey
                    + "，已被 task " + conflict.getNxDstId() + " 占用");
        }

        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(taskId);
        update.setNxDstStatus(DisShipmentTaskStatus.ASSIGNED);
        update.setNxDstOpenKey(openKey);
        nxDisShipmentTaskDao.update(update);
    }

    private boolean allActiveItemsHaveBill(NxDisShipmentTaskEntity task) {
        List<NxDisShipmentTaskItemEntity> items = task.getItems();
        if (items == null || items.isEmpty()) {
            return false;
        }
        boolean hasActive = false;
        for (NxDisShipmentTaskItemEntity item : items) {
            if (!ACTIVE.equals(item.getNxDstiItemStatus())) {
                continue;
            }
            hasActive = true;
            if (item.getNxDstiBillId() == null) {
                return false;
            }
        }
        return hasActive;
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

    /**
     * 若已存在 route_stop，将其挂到目标 driver_route；否则跳过（由 simulate 创建 stop，known gap）。
     */
    private void syncStopToDriverRoute(NxDisShipmentTaskEntity task,
                                       Integer driverUserId,
                                       Integer manualStopSeq) {
        if (task.getNxDstPlanId() == null || driverUserId == null) {
            return;
        }
        NxDisRouteStopEntity stop = nxDisRouteStopDao.queryByShipmentTaskId(task.getNxDstId());
        if (stop == null) {
            return;
        }
        NxDisDriverRouteEntity driverRoute = nxDisDriverRouteDao.queryByPlanAndDriver(
                task.getNxDstPlanId(), driverUserId);
        if (driverRoute == null) {
            return;
        }
        NxDisRouteStopEntity stopUpdate = new NxDisRouteStopEntity();
        stopUpdate.setNxDrsId(stop.getNxDrsId());
        stopUpdate.setNxDrsDriverRouteId(driverRoute.getNxDdrId());
        if (manualStopSeq != null) {
            stopUpdate.setNxDrsStopSeq(manualStopSeq);
        }
        nxDisRouteStopDao.update(stopUpdate);
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
        task.setItems(nxDisShipmentTaskItemDao.queryByTaskId(taskId));
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
