package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDepartmentDao;
import com.nongxinle.dao.NxDepartmentOrderHistoryDao;
import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDepartmentOrderHistoryEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;
import com.nongxinle.route.DisRouteEligibleOrderPolicy;
import com.nongxinle.route.DisShipmentTaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.route.DisShipmentTaskItemStatus.ACTIVE;

/**
 * GET 读模型完整性：只展示有有效 eligible 订单关联的站点。
 */
@Component
public class DisRouteDispatchReadIntegrityHelper {

    @Autowired
    private DisRouteShipmentTaskItemOrderResolver disRouteShipmentTaskItemOrderResolver;
    @Autowired
    private NxDepartmentDao nxDepartmentDao;
    @Autowired
    private NxDepartmentOrdersDao nxDepartmentOrdersDao;
    @Autowired
    private NxDepartmentOrderHistoryDao nxDepartmentOrderHistoryDao;

    /**
     * simulate 写路径：判断 task 是否仍有可展示的有效 item（与 GET 同口径）。
     */
    public boolean hasValidDisplayItems(NxDisShipmentTaskEntity task,
                                        Integer disId,
                                        String routeDate) {
        if (task == null) {
            return false;
        }
        if (isExecutionDisplayTask(task)) {
            return hasActiveLinkedItems(task);
        }
        List<NxDisShipmentTaskItemEntity> items = task.getItems();
        if (items == null || items.isEmpty()) {
            items = new ArrayList<NxDisShipmentTaskItemEntity>();
        }
        disRouteShipmentTaskItemOrderResolver.enrichItems(items);
        Map<Integer, NxDepartmentEntity> departmentById = loadDepartments(singleTaskList(task));
        String taskInvalidReason = validateTaskShell(task, disId, routeDate, departmentById);
        return !collectValidItems(task, disId, routeDate, taskInvalidReason).isEmpty();
    }

    /** 配送执行态 task：保留 item 快照，不因 live order eligible 口径变化而隐藏 */
    public boolean hasExecutionDisplayItems(NxDisShipmentTaskEntity task) {
        return task != null && isExecutionDisplayTask(task) && hasActiveLinkedItems(task);
    }

    private static boolean isExecutionDisplayTask(NxDisShipmentTaskEntity task) {
        if (task == null || task.getNxDstStatus() == null) {
            return false;
        }
        String status = task.getNxDstStatus();
        return DisShipmentTaskStatus.IN_DELIVERY.equals(status)
                || DisShipmentTaskStatus.DELIVERED.equals(status)
                || DisShipmentTaskStatus.EXCEPTION.equals(status);
    }

    private boolean hasActiveLinkedItems(NxDisShipmentTaskEntity task) {
        return !collectExecutionDisplayItems(task).isEmpty();
    }

    private List<NxDisShipmentTaskItemEntity> collectExecutionDisplayItems(NxDisShipmentTaskEntity task) {
        List<NxDisShipmentTaskItemEntity> executionItems = new ArrayList<NxDisShipmentTaskItemEntity>();
        if (task == null) {
            return executionItems;
        }
        List<NxDisShipmentTaskItemEntity> items = task.getItems();
        if (items == null || items.isEmpty()) {
            return executionItems;
        }
        disRouteShipmentTaskItemOrderResolver.enrichItems(items);
        for (NxDisShipmentTaskItemEntity item : items) {
            if (item == null || !isActiveItem(item)) {
                continue;
            }
            if (item.getNxDstiLiveOrderId() != null || item.getNxDstiHistoryOrderId() != null) {
                item.setItemValid(true);
                item.setItemInvalidReason(null);
                executionItems.add(item);
            }
        }
        return executionItems;
    }

    private List<NxDisShipmentTaskItemEntity> collectValidItems(NxDisShipmentTaskEntity task,
                                                                Integer disId,
                                                                String routeDate,
                                                                String taskInvalidReason) {
        List<NxDisShipmentTaskItemEntity> validItems = new ArrayList<NxDisShipmentTaskItemEntity>();
        if (taskInvalidReason != null) {
            markAllItemsInvalid(task.getItems(), taskInvalidReason);
            return validItems;
        }
        List<NxDisShipmentTaskItemEntity> items = task.getItems();
        if (items == null) {
            return validItems;
        }
        for (NxDisShipmentTaskItemEntity item : items) {
            if (item == null) {
                continue;
            }
            String reason = validateItem(item, task, disId, routeDate);
            if (reason == null) {
                item.setItemValid(true);
                item.setItemInvalidReason(null);
                validItems.add(item);
            } else {
                item.setItemValid(false);
                item.setItemInvalidReason(reason);
            }
        }
        return validItems;
    }

    private void markAllItemsInvalid(List<NxDisShipmentTaskItemEntity> items, String reason) {
        if (items == null) {
            return;
        }
        for (NxDisShipmentTaskItemEntity item : items) {
            if (item != null) {
                item.setItemValid(false);
                item.setItemInvalidReason(reason);
            }
        }
    }

    private String validateTaskShell(NxDisShipmentTaskEntity task,
                                     Integer disId,
                                     String routeDate,
                                     Map<Integer, NxDepartmentEntity> departmentById) {
        if (disId != null && task.getNxDstDistributerId() != null && !disId.equals(task.getNxDstDistributerId())) {
            return "TASK_DIS_MISMATCH";
        }
        if (routeDate != null && task.getNxDstRouteDate() != null
                && !routeDate.equals(task.getNxDstRouteDate())) {
            return "TASK_ROUTE_DATE_MISMATCH";
        }
        Integer depFatherId = task.getNxDstDepFatherId();
        if (depFatherId == null) {
            return "TASK_DEP_MISSING";
        }
        NxDepartmentEntity department = departmentById.get(depFatherId);
        if (department == null) {
            return "DEPARTMENT_NOT_FOUND";
        }
        if (disId != null && department.getNxDepartmentDisId() != null
                && !disId.equals(department.getNxDepartmentDisId())) {
            return "DEPARTMENT_DIS_MISMATCH";
        }
        return null;
    }

    private String validateItem(NxDisShipmentTaskItemEntity item,
                                NxDisShipmentTaskEntity task,
                                Integer disId,
                                String routeDate) {
        if (!isActiveItem(item)) {
            return "ITEM_NOT_ACTIVE";
        }
        Integer liveOrderId = item.getNxDstiLiveOrderId();
        Integer historyOrderId = item.getNxDstiHistoryOrderId();
        if (liveOrderId == null && historyOrderId == null) {
            return "MISSING_ORDER_LINK";
        }

        Integer expectedDepFatherId = task.getNxDstDepFatherId();
        if (historyOrderId != null && item.getNxDstiBillId() != null) {
            NxDepartmentOrderHistoryEntity history = loadHistory(historyOrderId);
            return DisRouteEligibleOrderPolicy.validateHistoryOrder(
                    history, disId, routeDate, expectedDepFatherId);
        }
        if (historyOrderId != null && liveOrderId == null) {
            NxDepartmentOrderHistoryEntity history = loadHistory(historyOrderId);
            return DisRouteEligibleOrderPolicy.validateHistoryOrder(
                    history, disId, routeDate, expectedDepFatherId);
        }
        if (liveOrderId != null) {
            NxDepartmentOrdersEntity live = loadLive(liveOrderId);
            return DisRouteEligibleOrderPolicy.validateLiveOrder(
                    live, disId, routeDate, expectedDepFatherId);
        }
        return "ORDER_NOT_ELIGIBLE";
    }

    private NxDepartmentOrdersEntity loadLive(Integer orderId) {
        return nxDepartmentOrdersDao.queryObject(orderId);
    }

    private NxDepartmentOrderHistoryEntity loadHistory(Integer orderId) {
        return nxDepartmentOrderHistoryDao.queryObject(orderId);
    }

    private Map<Integer, NxDepartmentEntity> loadDepartments(List<NxDisShipmentTaskEntity> tasks) {
        Set<Integer> depIds = new HashSet<Integer>();
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task != null && task.getNxDstDepFatherId() != null) {
                depIds.add(task.getNxDstDepFatherId());
            }
        }
        Map<Integer, NxDepartmentEntity> map = new HashMap<Integer, NxDepartmentEntity>();
        for (Integer depId : depIds) {
            NxDepartmentEntity department = nxDepartmentDao.queryObject(depId);
            if (department != null) {
                map.put(depId, department);
            }
        }
        return map;
    }

    private static boolean isActiveItem(NxDisShipmentTaskItemEntity item) {
        if (item == null) {
            return false;
        }
        String status = item.getNxDstiItemStatus();
        if (status == null || status.trim().isEmpty()) {
            return true;
        }
        return ACTIVE.equals(status);
    }

    private static List<NxDisShipmentTaskEntity> singleTaskList(NxDisShipmentTaskEntity task) {
        List<NxDisShipmentTaskEntity> list = new ArrayList<NxDisShipmentTaskEntity>();
        list.add(task);
        return list;
    }
}
