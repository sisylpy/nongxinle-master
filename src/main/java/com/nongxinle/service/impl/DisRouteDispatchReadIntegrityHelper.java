package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDepartmentDao;
import com.nongxinle.dao.NxDepartmentOrderHistoryDao;
import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.dto.route.DisRouteDispatchIntegrityResult;
import com.nongxinle.dto.route.InvalidDispatchStopDto;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDepartmentOrderHistoryEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;
import com.nongxinle.route.DisRouteEligibleOrderPolicy;
import com.nongxinle.route.DisRouteFeasibilityStatus;
import com.nongxinle.route.DisShipmentTaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.route.DisShipmentTaskItemStatus.ACTIVE;

/**
 * GET 读模型完整性：只展示有有效 eligible 订单关联的站点；无效残留进 invalidStops（不写库）。
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

    public DisRouteDispatchIntegrityResult apply(NxDisRoutePlanEntity plan,
                                                 List<NxDisShipmentTaskEntity> tasks,
                                                 String routeDate) {
        DisRouteDispatchIntegrityResult result = new DisRouteDispatchIntegrityResult();
        if (tasks == null || tasks.isEmpty()) {
            return result;
        }
        Integer disId = plan != null ? plan.getNxDrpDistributerId() : null;
        String effectiveRouteDate = resolveRouteDate(plan, routeDate);

        disRouteShipmentTaskItemOrderResolver.enrichTasks(tasks);
        Map<Integer, NxDepartmentEntity> departmentById = loadDepartments(tasks);
        Map<Integer, NxDisShipmentTaskEntity> taskById = indexTasks(tasks);

        List<NxDisShipmentTaskEntity> displayTasks = new ArrayList<NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task == null) {
                continue;
            }
            if (isExecutionDisplayTask(task)) {
                List<NxDisShipmentTaskItemEntity> executionItems = collectExecutionDisplayItems(task);
                task.setItems(executionItems);
                if (!executionItems.isEmpty()) {
                    task.setDispatchValid(true);
                    task.setDispatchInvalidReason(null);
                    task.setNeedsManualCleanup(false);
                    displayTasks.add(task);
                } else {
                    task.setDispatchValid(false);
                    task.setDispatchInvalidReason("NO_VALID_ORDER_ITEMS");
                    task.setNeedsManualCleanup(isManualCleanupRequired(task));
                    result.getExcludedTaskIds().add(task.getNxDstId());
                }
                continue;
            }
            String taskInvalidReason = validateTaskShell(task, disId, effectiveRouteDate, departmentById);
            List<NxDisShipmentTaskItemEntity> validItems = collectValidItems(
                    task, disId, effectiveRouteDate, taskInvalidReason);

            boolean hasValidItems = !validItems.isEmpty();
            task.setItems(validItems);
            if (hasValidItems) {
                task.setDispatchValid(true);
                task.setDispatchInvalidReason(null);
                task.setNeedsManualCleanup(false);
                displayTasks.add(task);
            } else {
                String reason = taskInvalidReason != null ? taskInvalidReason : "NO_VALID_ORDER_ITEMS";
                task.setDispatchValid(false);
                task.setDispatchInvalidReason(reason);
                task.setNeedsManualCleanup(isManualCleanupRequired(task));
                result.getExcludedTaskIds().add(task.getNxDstId());
            }
        }
        result.setDisplayTasks(displayTasks);

        if (plan != null && plan.getDriverRoutes() != null) {
            collectInvalidStops(plan, result, taskById);
            filterDriverRouteStops(plan, result.getExcludedTaskIds());
        }
        result.setInvalidStopCount(result.getInvalidStops().size());
        syncDriverRouteReadModelCounts(plan);
        return result;
    }

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

    private void syncDriverRouteReadModelCounts(NxDisRoutePlanEntity plan) {
        if (plan == null || plan.getDriverRoutes() == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            if (route == null) {
                continue;
            }
            int count = route.getStops() != null ? route.getStops().size() : 0;
            route.setNxDdrStopCount(count);
            if (count == 0) {
                route.setNxDdrTotalDistanceM(0L);
                route.setNxDdrTotalDurationS(0L);
                route.setNxDdrTotalLateMinutes(0);
                route.setNxDdrTotalWaitMinutes(0);
                route.setNxDdrTotalServiceMinutes(0);
                route.setNxDdrFeasibilityStatus(DisRouteFeasibilityStatus.IDLE);
            }
        }
    }

    private void collectInvalidStops(NxDisRoutePlanEntity plan,
                                     DisRouteDispatchIntegrityResult result,
                                     Map<Integer, NxDisShipmentTaskEntity> taskById) {
        Set<Integer> displayTaskIds = new HashSet<Integer>();
        for (NxDisShipmentTaskEntity task : result.getDisplayTasks()) {
            if (task.getNxDstId() != null) {
                displayTaskIds.add(task.getNxDstId());
            }
        }
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop == null) {
                    continue;
                }
                Integer taskId = stop.getNxDrsShipmentTaskId();
                if (taskId == null || displayTaskIds.contains(taskId)) {
                    continue;
                }
                InvalidDispatchStopDto invalid = new InvalidDispatchStopDto();
                invalid.setStopId(stop.getNxDrsId());
                invalid.setTaskId(taskId);
                invalid.setDepFatherId(stop.getNxDrsDepartmentId());
                invalid.setDepName(firstNonBlank(stop.getNxDrsDepartmentName(),
                        resolveTaskName(taskById, taskId)));
                NxDisShipmentTaskEntity excluded = taskById.get(taskId);
                if (excluded != null) {
                    invalid.setInvalidReason(excluded.getDispatchInvalidReason());
                    invalid.setNeedsManualCleanup(excluded.getNeedsManualCleanup());
                } else {
                    invalid.setInvalidReason("STALE_ROUTE_STOP");
                    invalid.setNeedsManualCleanup(false);
                }
                result.getInvalidStops().add(invalid);
                if (stop.getNxDrsId() != null) {
                    result.getExcludedStopIds().add(stop.getNxDrsId());
                }
            }
        }
    }

    private void filterDriverRouteStops(NxDisRoutePlanEntity plan, Set<Integer> excludedTaskIds) {
        if (excludedTaskIds == null || excludedTaskIds.isEmpty()) {
            return;
        }
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            Iterator<NxDisRouteStopEntity> iterator = route.getStops().iterator();
            while (iterator.hasNext()) {
                NxDisRouteStopEntity stop = iterator.next();
                if (stop != null && stop.getNxDrsShipmentTaskId() != null
                        && excludedTaskIds.contains(stop.getNxDrsShipmentTaskId())) {
                    stop.setShipmentTask(null);
                    iterator.remove();
                }
            }
        }
    }

    private static Map<Integer, NxDisShipmentTaskEntity> indexTasks(List<NxDisShipmentTaskEntity> tasks) {
        Map<Integer, NxDisShipmentTaskEntity> map = new HashMap<Integer, NxDisShipmentTaskEntity>();
        if (tasks == null) {
            return map;
        }
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task != null && task.getNxDstId() != null) {
                map.put(task.getNxDstId(), task);
            }
        }
        return map;
    }

    private static String resolveTaskName(Map<Integer, NxDisShipmentTaskEntity> taskById, Integer taskId) {
        NxDisShipmentTaskEntity task = taskById.get(taskId);
        return task != null ? task.getNxDstDepName() : null;
    }

    private String resolveRouteDate(NxDisRoutePlanEntity plan, String routeDate) {
        if (routeDate != null && !routeDate.trim().isEmpty()) {
            return routeDate.trim();
        }
        if (plan == null) {
            return null;
        }
        if (plan.getNxDrpRouteDate() != null) {
            return plan.getNxDrpRouteDate();
        }
        return plan.getNxDrpPlanDate();
    }

    private boolean isManualCleanupRequired(NxDisShipmentTaskEntity task) {
        if (task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1) {
            return true;
        }
        String status = task.getNxDstStatus();
        return DisShipmentTaskStatus.ASSIGNED.equals(status)
                || DisShipmentTaskStatus.READY_TO_GO.equals(status)
                || DisShipmentTaskStatus.IN_DELIVERY.equals(status)
                || DisShipmentTaskStatus.DELIVERED.equals(status);
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

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.trim().isEmpty()) {
            return primary;
        }
        return fallback;
    }
}
