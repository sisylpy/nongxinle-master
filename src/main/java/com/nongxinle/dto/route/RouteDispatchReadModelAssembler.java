package com.nongxinle.dto.route;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;
import com.nongxinle.route.DisRouteBillPrintStatusHelper;
import com.nongxinle.route.DisRouteDispatchLabels;
import com.nongxinle.route.DisRoutePurchaseStatusLabels;
import com.nongxinle.route.DisRouteSandboxReadModelPartitionHelper;
import com.nongxinle.route.DisRouteSandboxStopSource;
import com.nongxinle.route.DisShipmentTaskStatus;
import com.nongxinle.route.DisRouteTemporalHelper;
import com.nongxinle.route.RouteDispatchDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 派单读模型 Map 组装：task 实例关联 + 司机终端装车站点卡片。 */
public final class RouteDispatchReadModelAssembler {

    private RouteDispatchReadModelAssembler() {
    }

    public static void linkSharedTaskInstances(NxDisRoutePlanEntity plan,
                                               List<NxDisShipmentTaskEntity> tasks) {
        if (plan == null || tasks == null || tasks.isEmpty()) {
            return;
        }
        Map<Integer, NxDisShipmentTaskEntity> taskById = new HashMap<Integer, NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task != null && task.getNxDstId() != null) {
                taskById.put(task.getNxDstId(), task);
            }
        }
        if (plan.getDriverRoutes() == null && plan.getExecutionDriverRoutes() == null
                && plan.getLoadingDriverRoutes() == null) {
            return;
        }
        linkRoutesToTasks(plan.getDriverRoutes(), taskById);
        linkRoutesToTasks(plan.getLoadingDriverRoutes(), taskById);
        linkRoutesToTasks(plan.getExecutionDriverRoutes(), taskById);
    }

    private static void linkRoutesToTasks(List<NxDisDriverRouteEntity> routes,
                                          Map<Integer, NxDisShipmentTaskEntity> taskById) {
        if (routes == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop.getShipmentTask() != null && stop.getShipmentTask().getNxDstId() != null) {
                    NxDisShipmentTaskEntity shared = taskById.get(stop.getShipmentTask().getNxDstId());
                    if (shared != null) {
                        stop.setShipmentTask(shared);
                    }
                    continue;
                }
                if (stop.getNxDrsShipmentTaskId() == null) {
                    continue;
                }
                NxDisShipmentTaskEntity shared = taskById.get(stop.getNxDrsShipmentTaskId());
                if (shared != null) {
                    stop.setShipmentTask(shared);
                }
            }
        }
    }

    public static Map<String, Object> toLoadingStopMap(NxDisRouteStopEntity stop) {
        return toLoadingStopMap(stop, null);
    }

    public static Map<String, Object> toLoadingStopMap(NxDisRouteStopEntity stop,
                                                         Map<Integer, Integer> purchaseStatusByOrderId) {
        Map<String, Object> map = toConfirmedCanonicalStopMap(stop);
        if (map == null) {
            return null;
        }
        NxDisShipmentTaskEntity task = stop != null ? stop.getShipmentTask() : null;
        List<Map<String, Object>> orders = groupItemsIntoOrders(task, purchaseStatusByOrderId);
        map.put("orders", orders);
        map.put("items", flattenOrderItems(orders));
        overlayLoadingOperationFields(stop, map);
        return map;
    }

    private static Map<String, Object> toConfirmedCanonicalStopMap(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        Integer deliveryStopId = task != null && task.getNxDstId() != null
                ? task.getNxDstId() : stop.getNxDrsShipmentTaskId();
        map.put("deliveryStopId", deliveryStopId);
        map.put("departmentId", stop.getNxDrsDepartmentId() != null
                ? stop.getNxDrsDepartmentId()
                : (task != null ? task.getNxDstDepFatherId() : null));
        map.put("depFatherId", map.get("departmentId"));
        String departmentName = resolveDepartmentName(stop, task);
        map.put("departmentName", departmentName);
        map.put("customerName", departmentName);
        if (task != null && task.getNxDstAddress() != null) {
            map.put("customerAddress", task.getNxDstAddress());
        }
        map.put("driverUserId", task != null ? task.getNxDstAssignedDriverUserId() : null);
        map.put("driverRouteId", task != null && task.getNxDstDriverRouteId() != null
                ? task.getNxDstDriverRouteId() : stop.getNxDrsDriverRouteId());
        map.put("stopSource", DisRouteSandboxStopSource.CONFIRMED);
        map.put("stopScope", stop.getStopScope() != null
                ? stop.getStopScope() : DisRouteSandboxReadModelPartitionHelper.STOP_SCOPE_SANDBOX);

        String status = task != null ? task.getNxDstStatus() : null;
        map.put("status", status);
        String statusLabel = resolveStopStatusLabel(status, task);
        map.put("statusLabel", statusLabel);
        map.put("loadingStatusLabel", resolveLoadingStatusLabel(status, statusLabel, stop));
        map.put("deliveryStatusLabel", resolveDeliveryStatusLabel(status, statusLabel, stop));

        map.put("canReturnToSandbox", stop.getCanReturnToSandbox());
        map.put("returnToSandboxActionLabel", stop.getReturnToSandboxActionLabel());
        map.put("returnToSandboxBlockedReason", stop.getReturnToSandboxBlockedReason());
        map.put("returnToSandboxWarning", stop.getReturnToSandboxWarning());
        map.put("returnToSandboxConfirmMessage", stop.getReturnToSandboxConfirmMessage());
        map.put("canMove", stop.getCanMove());
        map.put("moveBlockedReason", stop.getMoveBlockedReason());

        map.put("legDistanceM", firstNonNullLong(stop.getNxDrsLegDistanceM(),
                task != null ? task.getNxDstLegDistanceM() : null));
        map.put("legDurationS", firstNonNullLong(stop.getNxDrsLegDurationS(),
                task != null ? task.getNxDstLegDurationS() : null));
        map.put("distanceProvider", firstNonBlank(stop.getLegDistanceProvider(),
                task != null ? task.getLegDistanceProvider() : null));
        map.put("distanceType", firstNonBlank(stop.getLegDistanceType(),
                task != null ? task.getLegDistanceType() : null));

        if (task != null) {
            overlayBillPrintHints(task, map);
            List<Map<String, Object>> itemMaps = extractSandboxItemMaps(task);
            map.put("items", itemMaps);
            List<Integer> liveOrderIds = collectLiveOrderIdsFromItems(itemMaps);
            if (!liveOrderIds.isEmpty()) {
                map.put("liveOrderIds", liveOrderIds);
            }
        }

        map.put("stopSeq", stop.getNxDrsStopSeq());
        overlayStopScheduleCanonicalFields(stop, map);
        overlayEtaAliases(map);
        map.put("operationStatusLabel", stop.getOperationStatusLabel());
        overlayLoadingOperationFields(stop, map);
        return map;
    }

    private static void overlayBillPrintHints(NxDisShipmentTaskEntity task, Map<String, Object> map) {
        if (task == null || map == null) {
            return;
        }
        Map<String, Object> hints = DisRouteBillPrintStatusHelper.buildBillPrintHints(task);
        map.putAll(hints);
        if (task.getBillPrintStatus() != null) {
            map.put("billPrintStatus", task.getBillPrintStatus());
        }
        if (task.getUnprintedBillCount() != null) {
            map.put("unprintedBillCount", task.getUnprintedBillCount());
        }
        if (task.getBillPrintWarning() != null) {
            map.put("billPrintWarning", task.getBillPrintWarning());
        }
    }

    private static String resolveDepartmentName(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        String fromStop = firstNonBlank(stop != null ? stop.getLiveDepartmentName() : null,
                stop != null ? stop.getNxDrsDepartmentName() : null);
        if (fromStop != null) {
            return fromStop;
        }
        if (task != null) {
            return firstNonBlank(task.getLiveDepartmentName(), task.getNxDstDepName());
        }
        return null;
    }

    private static Long firstNonNullLong(Long first, Long second) {
        return first != null ? first : second;
    }

    private static void overlayStopScheduleCanonicalFields(NxDisRouteStopEntity stop, Map<String, Object> map) {
        if (stop == null || map == null) {
            return;
        }
        Date arrivalAt = resolveCanonicalPlannedArrivalAt(stop);
        Date departureAt = stop.getNxDrsPlannedDepartureAt();
        if (arrivalAt != null) {
            map.put("plannedArrivalAt", RouteDispatchDateFormat.format(arrivalAt));
        }
        if (departureAt != null) {
            map.put("plannedDepartureAt", RouteDispatchDateFormat.format(departureAt));
        }
        map.put("plannedArrivalLabel", stop.getPlannedArrivalLabel());
        map.put("plannedDepartureLabel", resolvePlannedDepartureLabel(stop, stop.getShipmentTask()));
        map.put("customerWindowLabel", stop.getCustomerWindowLabel());
        map.put("serviceMinutes", resolveServiceMinutes(stop));
        map.put("serviceMinutesSource", stop.getServiceMinutesSource());
        map.put("serviceDurationLabel", stop.getServiceDurationLabel());
    }

    private static Date resolveCanonicalPlannedArrivalAt(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getNxDrsPlannedServiceStartAt() != null) {
            return stop.getNxDrsPlannedServiceStartAt();
        }
        return stop.getNxDrsPlannedArrivalAt();
    }

    private static Integer resolveServiceMinutes(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        return stop.getNxDrsServiceMinutes();
    }

    private static String resolvePlannedDepartureLabel(NxDisRouteStopEntity stop,
                                                         NxDisShipmentTaskEntity task) {
        if (stop != null && stop.getPlannedDepartureLabel() != null
                && !stop.getPlannedDepartureLabel().trim().isEmpty()) {
            return stop.getPlannedDepartureLabel().trim();
        }
        Date departureAt = stop != null ? stop.getNxDrsPlannedDepartureAt() : null;
        if (departureAt == null && task != null) {
            departureAt = task.getNxDstPlannedDepartureAt();
        }
        if (departureAt == null) {
            return null;
        }
        return DisRouteTemporalHelper.formatDateTimeLabel(departureAt, new Date());
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }
        if (second != null && !second.trim().isEmpty()) {
            return second.trim();
        }
        return null;
    }

    private static String resolveStopStatusLabel(String status, NxDisShipmentTaskEntity task) {
        if (DisShipmentTaskStatus.DELIVERED.equals(status)) {
            return DisRouteDispatchLabels.label(DisShipmentTaskStatus.DELIVERED);
        }
        if (DisShipmentTaskStatus.EXCEPTION.equals(status)) {
            return DisRouteDispatchLabels.label(DisShipmentTaskStatus.EXCEPTION);
        }
        if (DisShipmentTaskStatus.IN_DELIVERY.equals(status)) {
            return DisRouteDispatchLabels.label(DisShipmentTaskStatus.IN_DELIVERY);
        }
        if (task != null && task.getNxDstStatusLabel() != null && !task.getNxDstStatusLabel().trim().isEmpty()) {
            return task.getNxDstStatusLabel().trim();
        }
        return DisRouteDispatchLabels.label(status);
    }

    private static String resolveLoadingStatusLabel(String status, String statusLabel, NxDisRouteStopEntity stop) {
        String scope = stop != null ? stop.getStopScope() : null;
        if (DisRouteSandboxReadModelPartitionHelper.STOP_SCOPE_EXECUTION.equals(scope)) {
            return null;
        }
        if (stop != null && stop.getOperationStatusLabel() != null
                && !stop.getOperationStatusLabel().trim().isEmpty()) {
            return stop.getOperationStatusLabel().trim();
        }
        if (DisShipmentTaskStatus.ASSIGNED.equals(status) || DisShipmentTaskStatus.READY_TO_GO.equals(status)) {
            return statusLabel;
        }
        return statusLabel;
    }

    private static String resolveDeliveryStatusLabel(String status, String statusLabel, NxDisRouteStopEntity stop) {
        String scope = stop != null ? stop.getStopScope() : null;
        if (DisRouteSandboxReadModelPartitionHelper.STOP_SCOPE_LOADING.equals(scope)) {
            return "待出发";
        }
        if (DisShipmentTaskStatus.DELIVERED.equals(status)) {
            return DisRouteDispatchLabels.label(DisShipmentTaskStatus.DELIVERED);
        }
        if (DisShipmentTaskStatus.EXCEPTION.equals(status)) {
            return DisRouteDispatchLabels.label(DisShipmentTaskStatus.EXCEPTION);
        }
        if (DisShipmentTaskStatus.IN_DELIVERY.equals(status)
                || DisRouteSandboxReadModelPartitionHelper.STOP_SCOPE_EXECUTION.equals(scope)) {
            return statusLabel != null ? statusLabel : DisRouteDispatchLabels.label(status);
        }
        return statusLabel;
    }

    private static void overlayLoadingOperationFields(NxDisRouteStopEntity stop, Map<String, Object> map) {
        if (stop == null || map == null) {
            return;
        }
        map.put("canConfirmLoad", stop.getCanConfirmLoad() != null ? stop.getCanConfirmLoad() : Boolean.FALSE);
        map.put("confirmLoadBlockedReason", stop.getConfirmLoadBlockedReason());
    }

    private static void overlayEtaAliases(Map<String, Object> map) {
        if (map == null) {
            return;
        }
        if (map.containsKey("plannedArrivalAt")) {
            map.put("etaArriveAt", map.get("plannedArrivalAt"));
        }
        if (map.containsKey("plannedDepartureAt")) {
            map.put("etaLeaveAt", map.get("plannedDepartureAt"));
        }
    }

    private static List<Map<String, Object>> groupItemsIntoOrders(NxDisShipmentTaskEntity task,
                                                                  Map<Integer, Integer> purchaseStatusByOrderId) {
        if (task == null || task.getItems() == null || task.getItems().isEmpty()) {
            return Collections.emptyList();
        }
        LinkedHashMap<Integer, Map<String, Object>> byOrderId = new LinkedHashMap<Integer, Map<String, Object>>();
        for (NxDisShipmentTaskItemEntity item : task.getItems()) {
            if (item == null) {
                continue;
            }
            Integer liveOrderId = item.getNxDstiLiveOrderId();
            Map<String, Object> orderMap = byOrderId.get(liveOrderId);
            if (orderMap == null) {
                orderMap = new LinkedHashMap<String, Object>();
                orderMap.put("liveOrderId", liveOrderId);
                orderMap.put("items", new ArrayList<Map<String, Object>>());
                if (liveOrderId != null && purchaseStatusByOrderId != null
                        && purchaseStatusByOrderId.containsKey(liveOrderId)) {
                    Integer purchaseStatus = purchaseStatusByOrderId.get(liveOrderId);
                    orderMap.put("purchaseStatus", purchaseStatus);
                    orderMap.put("purchaseStatusLabel", DisRoutePurchaseStatusLabels.label(purchaseStatus));
                }
                byOrderId.put(liveOrderId, orderMap);
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderMap.get("items");
            Map<String, Object> itemMap = new LinkedHashMap<String, Object>();
            itemMap.put("liveOrderId", liveOrderId);
            itemMap.put("goodsName", item.getNxDstiGoodsName());
            itemMap.put("standard", item.getNxDstiStandard());
            itemMap.put("quantity", item.getNxDstiQuantity());
            itemMap.put("status", item.getNxDstiItemStatus());
            items.add(itemMap);
        }
        return new ArrayList<Map<String, Object>>(byOrderId.values());
    }

    private static List<Map<String, Object>> extractSandboxItemMaps(NxDisShipmentTaskEntity task) {
        if (task == null || task.getItems() == null || task.getItems().isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (NxDisShipmentTaskItemEntity item : task.getItems()) {
            if (item == null) {
                continue;
            }
            Map<String, Object> itemMap = new LinkedHashMap<String, Object>();
            itemMap.put("liveOrderId", item.getNxDstiLiveOrderId());
            itemMap.put("goodsName", item.getNxDstiGoodsName());
            itemMap.put("standard", item.getNxDstiStandard());
            itemMap.put("quantity", item.getNxDstiQuantity());
            itemMap.put("status", item.getNxDstiItemStatus());
            result.add(itemMap);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> flattenOrderItems(List<Map<String, Object>> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> flat = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> order : orders) {
            if (order == null) {
                continue;
            }
            Object itemsObj = order.get("items");
            if (itemsObj instanceof List) {
                for (Object item : (List<?>) itemsObj) {
                    if (item instanceof Map) {
                        flat.add((Map<String, Object>) item);
                    }
                }
            }
        }
        return flat;
    }

    private static List<Integer> collectLiveOrderIdsFromItems(List<Map<String, Object>> itemMaps) {
        if (itemMaps == null || itemMaps.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> liveOrderIds = new ArrayList<Integer>();
        for (Map<String, Object> itemMap : itemMaps) {
            Object liveOrderId = itemMap.get("liveOrderId");
            if (liveOrderId instanceof Number) {
                liveOrderIds.add(((Number) liveOrderId).intValue());
            }
        }
        return liveOrderIds;
    }
}
