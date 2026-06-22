package com.nongxinle.route;

import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 未分配客户站点：按客户 fatherId 聚合，合并多来源 task / 订单行。
 */
public final class DisRouteSandboxUnassignedStopHelper {

    private DisRouteSandboxUnassignedStopHelper() {
    }

    public static List<NxDisRouteStopEntity> consolidateByDepartment(List<NxDisRouteStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return new ArrayList<NxDisRouteStopEntity>();
        }
        Map<Integer, NxDisRouteStopEntity> byDep = new LinkedHashMap<Integer, NxDisRouteStopEntity>();
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Integer depId = resolveDepartmentId(stop);
            if (depId == null) {
                continue;
            }
            NxDisRouteStopEntity existing = byDep.get(depId);
            if (existing == null) {
                byDep.put(depId, stop);
                continue;
            }
            mergeStopInto(existing, stop);
        }
        return new ArrayList<NxDisRouteStopEntity>(byDep.values());
    }

    public static int countUniqueCustomerStops(List<NxDisRouteStopEntity>... lists) {
        Set<Integer> depIds = new LinkedHashSet<Integer>();
        if (lists == null) {
            return 0;
        }
        for (List<NxDisRouteStopEntity> list : lists) {
            if (list == null) {
                continue;
            }
            for (NxDisRouteStopEntity stop : list) {
                Integer depId = resolveDepartmentId(stop);
                if (depId != null) {
                    depIds.add(depId);
                }
            }
        }
        return depIds.size();
    }

    public static Integer resolveDepartmentId(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getNxDrsDepartmentId() != null) {
            return stop.getNxDrsDepartmentId();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && task.getNxDstDepFatherId() != null) {
            return task.getNxDstDepFatherId();
        }
        return DisRouteSandboxStopKeyUtils.parseDepFatherId(stop.getSandboxStopKey());
    }

    private static void mergeStopInto(NxDisRouteStopEntity target, NxDisRouteStopEntity source) {
        if (target == null || source == null) {
            return;
        }
        NxDisShipmentTaskEntity targetTask = ensureTask(target);
        NxDisShipmentTaskEntity sourceTask = source.getShipmentTask();
        if (sourceTask == null) {
            return;
        }
        if (preferSourceTask(targetTask, sourceTask)) {
            copyTaskFields(targetTask, sourceTask);
            copyStopFieldsFromTask(target, targetTask);
        }
        mergeTaskItems(targetTask, sourceTask);
        if (target.getSandboxStopKey() == null && source.getSandboxStopKey() != null) {
            target.setSandboxStopKey(source.getSandboxStopKey());
        }
        if ((target.getNxDrsDepartmentName() == null || target.getNxDrsDepartmentName().trim().isEmpty())
                && source.getNxDrsDepartmentName() != null) {
            target.setNxDrsDepartmentName(source.getNxDrsDepartmentName());
        }
        if (!hasLegMetrics(target) && hasLegMetrics(source)) {
            copyLegMetrics(source, target);
        }
    }

    private static NxDisShipmentTaskEntity ensureTask(NxDisRouteStopEntity stop) {
        if (stop.getShipmentTask() == null) {
            stop.setShipmentTask(new NxDisShipmentTaskEntity());
        }
        return stop.getShipmentTask();
    }

    private static boolean preferSourceTask(NxDisShipmentTaskEntity target, NxDisShipmentTaskEntity source) {
        if (target.getNxDstId() == null && source.getNxDstId() != null) {
            return true;
        }
        if (target.getNxDstId() != null && source.getNxDstId() == null) {
            return false;
        }
        int targetItems = target.getItems() != null ? target.getItems().size() : 0;
        int sourceItems = source.getItems() != null ? source.getItems().size() : 0;
        return sourceItems > targetItems;
    }

    private static void copyTaskFields(NxDisShipmentTaskEntity target, NxDisShipmentTaskEntity source) {
        target.setNxDstId(source.getNxDstId());
        target.setNxDstDistributerId(source.getNxDstDistributerId());
        target.setNxDstRouteDate(source.getNxDstRouteDate());
        target.setNxDstDepFatherId(source.getNxDstDepFatherId());
        target.setNxDstDepName(source.getNxDstDepName());
        target.setNxDstLat(source.getNxDstLat());
        target.setNxDstLng(source.getNxDstLng());
        target.setNxDstAddress(source.getNxDstAddress());
        target.setNxDstStatus(source.getNxDstStatus());
        target.setNxDstEarliestDeliveryTimeS(source.getNxDstEarliestDeliveryTimeS());
        target.setNxDstLatestDeliveryTimeS(source.getNxDstLatestDeliveryTimeS());
        target.setNxDstServiceMinutes(source.getNxDstServiceMinutes());
        target.setNxDstTimeWindowOverrideFlag(source.getNxDstTimeWindowOverrideFlag());
        target.setNxDstTimeWindowAdjustReason(source.getNxDstTimeWindowAdjustReason());
        target.setSandboxStopKey(source.getSandboxStopKey());
        target.setStopSource(DisRouteSandboxStopSource.UNASSIGNED);
        target.setConfirmViaSandbox(true);
    }

    private static void copyStopFieldsFromTask(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        stop.setNxDrsDepartmentId(task.getNxDstDepFatherId());
        stop.setNxDrsDepartmentName(task.getNxDstDepName());
        stop.setNxDrsLat(task.getNxDstLat());
        stop.setNxDrsLng(task.getNxDstLng());
        stop.setNxDrsAddress(task.getNxDstAddress());
        if (task.getNxDstId() != null) {
            stop.setNxDrsShipmentTaskId(task.getNxDstId());
        }
        stop.setStopSource(DisRouteSandboxStopSource.UNASSIGNED);
        stop.setConfirmViaSandbox(true);
    }

    private static void mergeTaskItems(NxDisShipmentTaskEntity target, NxDisShipmentTaskEntity source) {
        if (source.getItems() == null || source.getItems().isEmpty()) {
            return;
        }
        List<NxDisShipmentTaskItemEntity> merged = new ArrayList<NxDisShipmentTaskItemEntity>();
        Map<Integer, NxDisShipmentTaskItemEntity> byOrderId = new LinkedHashMap<Integer, NxDisShipmentTaskItemEntity>();
        if (target.getItems() != null) {
            for (NxDisShipmentTaskItemEntity item : target.getItems()) {
                if (item != null && item.getNxDstiLiveOrderId() != null) {
                    byOrderId.put(item.getNxDstiLiveOrderId(), item);
                }
            }
        }
        for (NxDisShipmentTaskItemEntity item : source.getItems()) {
            if (item == null || item.getNxDstiLiveOrderId() == null) {
                continue;
            }
            NxDisShipmentTaskItemEntity existing = byOrderId.get(item.getNxDstiLiveOrderId());
            if (existing == null) {
                byOrderId.put(item.getNxDstiLiveOrderId(), item);
            } else if (isRicherItem(item, existing)) {
                byOrderId.put(item.getNxDstiLiveOrderId(), item);
            }
        }
        merged.addAll(byOrderId.values());
        target.setItems(merged);
    }

    private static boolean isRicherItem(NxDisShipmentTaskItemEntity candidate, NxDisShipmentTaskItemEntity existing) {
        if (candidate.getNxDstiGoodsName() != null && existing.getNxDstiGoodsName() == null) {
            return true;
        }
        return candidate.getNxDstiQuantity() != null && existing.getNxDstiQuantity() == null;
    }

    private static boolean hasLegMetrics(NxDisRouteStopEntity stop) {
        return stop.getNxDrsLegDistanceM() != null && stop.getNxDrsLegDistanceM() > 0L;
    }

    private static void copyLegMetrics(NxDisRouteStopEntity from, NxDisRouteStopEntity to) {
        to.setNxDrsLegDistanceM(from.getNxDrsLegDistanceM());
        to.setNxDrsLegDurationS(from.getNxDrsLegDurationS());
        to.setLegDistanceProvider(from.getLegDistanceProvider());
        to.setLegDistanceType(from.getLegDistanceType());
        NxDisShipmentTaskEntity toTask = to.getShipmentTask();
        NxDisShipmentTaskEntity fromTask = from.getShipmentTask();
        if (toTask != null && fromTask != null) {
            toTask.setNxDstLegDistanceM(fromTask.getNxDstLegDistanceM());
            toTask.setNxDstLegDurationS(fromTask.getNxDstLegDurationS());
            toTask.setLegDistanceProvider(fromTask.getLegDistanceProvider());
            toTask.setLegDistanceType(fromTask.getLegDistanceType());
        }
    }
}
