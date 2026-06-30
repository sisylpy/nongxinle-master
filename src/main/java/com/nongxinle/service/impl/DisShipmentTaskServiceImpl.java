package com.nongxinle.service.impl;

import com.nongxinle.dao.*;
import com.nongxinle.dto.route.BillPrintedOrderRef;
import com.nongxinle.entity.*;
import com.nongxinle.route.DisRoutePlanStatus;
import com.nongxinle.route.DisShipmentTaskStatus;
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
            // bill 与派单弱连接：送达/配送中后补打单仅回填 item.billId，不阻断打印保存。
            if (task != null && isBillPrintSkippedTask(task)) {
                continue;
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
                rejectBillRevertOnTerminalTask(task);
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

    /** bill 回退仍保护执行终态；打印回填见 {@link #isBillPrintSkippedTask}。 */
    private void rejectBillRevertOnTerminalTask(NxDisShipmentTaskEntity task) {
        String status = task.getNxDstStatus();
        if (DisShipmentTaskStatus.CANCELLED.equals(status)
                || DisShipmentTaskStatus.CLOSED.equals(status)
                || DisShipmentTaskStatus.IN_DELIVERY.equals(status)
                || DisShipmentTaskStatus.DELIVERED.equals(status)) {
            throw new IllegalStateException("task " + task.getNxDstId() + " 状态 " + status + " 不允许 bill 回填/回退");
        }
    }

    /** 仅作废 task 跳过打印 hook；DELIVERED / IN_DELIVERY 允许弱参考回填。 */
    private static boolean isBillPrintSkippedTask(NxDisShipmentTaskEntity task) {
        if (task == null) {
            return true;
        }
        String status = task.getNxDstStatus();
        return DisShipmentTaskStatus.CANCELLED.equals(status)
                || DisShipmentTaskStatus.CLOSED.equals(status);
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
