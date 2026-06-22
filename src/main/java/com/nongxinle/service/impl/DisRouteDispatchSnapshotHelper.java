package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDepartmentDao;
import com.nongxinle.dao.NxDisShipmentTaskItemDao;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;
import com.nongxinle.route.DisRouteCustomerTier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.nongxinle.route.DisShipmentTaskItemStatus.ACTIVE;

/**
 * Phase 2b-5：从 department + task items（经 live/history 订单 enrich）生成派车汇总指标，写入 task / stop。
 * 商品明细主权不在 task_item 列；applyItemMetrics 统计前经 OrderResolver 从订单表解析数量。
 */
@Component
public class DisRouteDispatchSnapshotHelper {

    private static final int DEFAULT_SERVICE_MINUTES = 30;

    @Autowired
    private NxDepartmentDao nxDepartmentDao;
    @Autowired
    private NxDisShipmentTaskItemDao nxDisShipmentTaskItemDao;
    @Autowired
    private DisRouteShipmentTaskItemOrderResolver disRouteShipmentTaskItemOrderResolver;

    public void refreshTaskSnapshot(NxDisShipmentTaskEntity task, boolean preserveTimeWindowOverride) {
        if (task == null || task.getNxDstId() == null) {
            return;
        }
        NxDepartmentEntity department = loadDepartment(task.getNxDstDepFatherId());
        applyDepartmentDefaultsToTask(task, department, preserveTimeWindowOverride);
        applyItemMetricsToTask(task);
    }

    /** 仅从 task.items（或 DB）统计 orderCount / itemCount / totalQuantity，写入 task 内存字段。 */
    public void applyItemMetrics(NxDisShipmentTaskEntity task) {
        applyItemMetricsToTask(task);
    }

    public int countActiveItems(Integer taskId) {
        if (taskId == null) {
            return 0;
        }
        return countActiveItems(nxDisShipmentTaskItemDao.queryByTaskId(taskId));
    }

    public int countActiveItems(List<NxDisShipmentTaskItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (NxDisShipmentTaskItemEntity item : items) {
            if (isActiveItem(item)) {
                count++;
            }
        }
        return count;
    }

    /** override 路径：只同步送达窗口字段到 stop，不触碰 orderCount / itemCount 等。 */
    public NxDisRouteStopEntity buildStopTimeWindowUpdate(NxDisShipmentTaskEntity task, Integer stopId) {
        NxDisRouteStopEntity stopUpdate = new NxDisRouteStopEntity();
        stopUpdate.setNxDrsId(stopId);
        stopUpdate.setNxDrsEarliestDeliveryTimeS(task.getNxDstEarliestDeliveryTimeS());
        stopUpdate.setNxDrsLatestDeliveryTimeS(task.getNxDstLatestDeliveryTimeS());
        stopUpdate.setNxDrsServiceMinutes(task.getNxDstServiceMinutes());
        stopUpdate.setNxDrsTimeWindowOverrideFlag(task.getNxDstTimeWindowOverrideFlag());
        stopUpdate.setNxDrsTimeWindowAdjustReason(task.getNxDstTimeWindowAdjustReason());
        return stopUpdate;
    }

    public void copyTaskSnapshotToStop(NxDisShipmentTaskEntity task, NxDisRouteStopEntity stop) {
        if (task == null || stop == null) {
            return;
        }
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
    }

    public NxDisShipmentTaskEntity buildTaskUpdateFromSnapshot(NxDisShipmentTaskEntity task,
                                                                boolean preserveTimeWindowOverride) {
        refreshTaskSnapshot(task, preserveTimeWindowOverride);
        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(task.getNxDstId());
        update.setNxDstCustomerTier(task.getNxDstCustomerTier());
        update.setNxDstPriorityWeight(task.getNxDstPriorityWeight());
        update.setNxDstOrderCount(task.getNxDstOrderCount());
        update.setNxDstItemCount(task.getNxDstItemCount());
        update.setNxDstTotalQuantity(task.getNxDstTotalQuantity());
        if (!preserveTimeWindowOverride || task.getNxDstTimeWindowOverrideFlag() == null
                || task.getNxDstTimeWindowOverrideFlag() == 0) {
            update.setNxDstEarliestDeliveryTimeS(task.getNxDstEarliestDeliveryTimeS());
            update.setNxDstLatestDeliveryTimeS(task.getNxDstLatestDeliveryTimeS());
            update.setNxDstServiceMinutes(task.getNxDstServiceMinutes());
        }
        update.setNxDstPriorityLevel(resolveLegacyPriorityLevel(task));
        return update;
    }

    public NxDisRouteStopEntity buildStopUpdateFromTaskSnapshot(NxDisShipmentTaskEntity task) {
        NxDisRouteStopEntity stopUpdate = new NxDisRouteStopEntity();
        copyTaskSnapshotToStop(task, stopUpdate);
        return stopUpdate;
    }

    private void applyDepartmentDefaultsToTask(NxDisShipmentTaskEntity task,
                                               NxDepartmentEntity department,
                                               boolean preserveTimeWindowOverride) {
        String tier = department != null && department.getNxDepartmentDispatchCustomerTier() != null
                ? DisRouteCustomerTier.normalize(department.getNxDepartmentDispatchCustomerTier())
                : DisRouteCustomerTier.NORMAL;
        task.setNxDstCustomerTier(tier);
        task.setNxDstPriorityWeight(department != null && department.getNxDepartmentDispatchPriorityWeight() != null
                ? department.getNxDepartmentDispatchPriorityWeight() : 0);

        boolean hasOverride = task.getNxDstTimeWindowOverrideFlag() != null
                && task.getNxDstTimeWindowOverrideFlag() == 1;
        if (!preserveTimeWindowOverride || !hasOverride) {
            task.setNxDstEarliestDeliveryTimeS(department != null
                    ? department.getNxDepartmentEarliestDeliveryTime() : null);
            task.setNxDstLatestDeliveryTimeS(department != null
                    ? department.getNxDepartmentLatestDeliveryTime() : null);
            task.setNxDstServiceMinutes(resolveServiceMinutes(department));
        }
    }

    private void applyItemMetricsToTask(NxDisShipmentTaskEntity task) {
        List<NxDisShipmentTaskItemEntity> items = task.getItems();
        if (items == null) {
            items = nxDisShipmentTaskItemDao.queryByTaskId(task.getNxDstId());
            task.setItems(items);
        }
        disRouteShipmentTaskItemOrderResolver.enrichItems(items);
        int itemCount = 0;
        BigDecimal totalQty = BigDecimal.ZERO;
        if (items != null) {
            for (NxDisShipmentTaskItemEntity item : items) {
                if (!isActiveItem(item)) {
                    continue;
                }
                itemCount++;
                totalQty = totalQty.add(parseQuantity(item.getNxDstiQuantity()));
            }
        }
        task.setNxDstItemCount(itemCount);
        task.setNxDstOrderCount(itemCount);
        task.setNxDstTotalQuantity(formatQuantity(totalQty));
    }

    static boolean isActiveItem(NxDisShipmentTaskItemEntity item) {
        if (item == null) {
            return false;
        }
        String status = item.getNxDstiItemStatus();
        if (status == null || status.trim().isEmpty()) {
            return true;
        }
        return ACTIVE.equals(status);
    }

    private int resolveLegacyPriorityLevel(NxDisShipmentTaskEntity task) {
        int weight = task.getNxDstPriorityWeight() != null ? task.getNxDstPriorityWeight() : 0;
        int tierBonus = tierBonus(task.getNxDstCustomerTier());
        return tierBonus + weight;
    }

    private int tierBonus(String tier) {
        if (DisRouteCustomerTier.VIP.equals(tier)) {
            return 100;
        }
        if (DisRouteCustomerTier.NEW.equals(tier)) {
            return 30;
        }
        if (DisRouteCustomerTier.SMALL.equals(tier)) {
            return 20;
        }
        return 50;
    }

    private int resolveServiceMinutes(NxDepartmentEntity department) {
        if (department != null && department.getNxDepartmentUnloadDuration() != null
                && department.getNxDepartmentUnloadDuration() > 0) {
            return department.getNxDepartmentUnloadDuration();
        }
        return DEFAULT_SERVICE_MINUTES;
    }

    private NxDepartmentEntity loadDepartment(Integer departmentId) {
        if (departmentId == null) {
            return null;
        }
        return nxDepartmentDao.queryObject(departmentId);
    }

    private BigDecimal parseQuantity(String quantity) {
        if (quantity == null || quantity.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(quantity.trim());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private String formatQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return quantity.stripTrailingZeros().toPlainString();
    }
}
