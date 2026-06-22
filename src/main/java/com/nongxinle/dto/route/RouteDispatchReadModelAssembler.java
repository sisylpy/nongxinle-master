package com.nongxinle.dto.route;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;
import com.nongxinle.route.DisRouteBillPrintStatusHelper;
import com.nongxinle.route.DisRouteDispatchCardTemplateBuilder;
import com.nongxinle.route.DisRouteDispatchDriverNameHelper;
import com.nongxinle.route.DisRouteDeliveryExceptionType;
import com.nongxinle.route.DisRouteDispatchLabels;
import com.nongxinle.route.DisRoutePurchaseStatusLabels;
import com.nongxinle.route.DisRouteSandboxReadModelPartitionHelper;
import com.nongxinle.route.DisRouteSandboxStopSource;
import com.nongxinle.route.DisShipmentTaskStatus;
import com.nongxinle.route.RouteDispatchDateFormat;
import com.nongxinle.route.DisRouteTemporalHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 2b-2：显式 Map 读模型，确保 Fastjson 输出 enrichment / label 字段。
 */
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

    public static Map<String, Object> toPlanMap(NxDisRoutePlanEntity plan) {
        if (plan == null) {
            return null;
        }
        Map<String, Object> map = entityToMap(plan);
        overlayPlanOperationFields(plan, map);
        if (plan.getDriverRoutes() != null) {
            List<Map<String, Object>> routeMaps = new ArrayList<Map<String, Object>>();
            for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
                routeMaps.add(toDriverRouteMap(route));
            }
            map.put("driverRoutes", routeMaps);
        } else {
            map.put("driverRoutes", Collections.emptyList());
        }
        if (plan.getLoadingDriverRoutes() != null) {
            List<Map<String, Object>> loadingMaps = new ArrayList<Map<String, Object>>();
            for (NxDisDriverRouteEntity route : plan.getLoadingDriverRoutes()) {
                loadingMaps.add(toDriverRouteMap(route));
            }
            map.put("loadingDriverRoutes", loadingMaps);
        } else {
            map.put("loadingDriverRoutes", Collections.emptyList());
        }
        if (plan.getExecutionDriverRoutes() != null) {
            List<Map<String, Object>> executionMaps = new ArrayList<Map<String, Object>>();
            for (NxDisDriverRouteEntity route : plan.getExecutionDriverRoutes()) {
                executionMaps.add(toDriverRouteMap(route));
            }
            map.put("executionDriverRoutes", executionMaps);
        } else {
            map.put("executionDriverRoutes", Collections.emptyList());
        }
        return map;
    }

    public static List<Map<String, Object>> toExecutionDriverRouteMaps(List<NxDisDriverRouteEntity> routes) {
        if (routes == null || routes.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (NxDisDriverRouteEntity route : routes) {
            result.add(toDriverRouteMap(route));
        }
        return result;
    }

    /** 装车页：路线含 orders[] 分组商品行。 */
    public static List<Map<String, Object>> toLoadingDriverRouteMaps(List<NxDisDriverRouteEntity> routes) {
        return toLoadingDriverRouteMaps(routes, null);
    }

    public static List<Map<String, Object>> toLoadingDriverRouteMaps(List<NxDisDriverRouteEntity> routes,
                                                                      Map<Integer, Integer> purchaseStatusByOrderId) {
        if (routes == null || routes.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (NxDisDriverRouteEntity route : routes) {
            result.add(toLoadingDriverRouteMap(route, purchaseStatusByOrderId));
        }
        return result;
    }

    /** 配送页：路线含 orders[] / 送达与异常字段。 */
    public static List<Map<String, Object>> toDeliveryDriverRouteMaps(List<NxDisDriverRouteEntity> routes) {
        return toDeliveryDriverRouteMaps(routes, null);
    }

    public static List<Map<String, Object>> toDeliveryDriverRouteMaps(List<NxDisDriverRouteEntity> routes,
                                                                      DriverDispatchListResponse drivers) {
        if (routes == null || routes.isEmpty()) {
            return Collections.emptyList();
        }
        DisRouteDispatchDriverNameHelper.enrichRoutesDriverNames(routes, drivers);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (NxDisDriverRouteEntity route : routes) {
            DriverDispatchCandidateDto driverMeta = DisRouteDispatchDriverNameHelper.findDriver(
                    drivers, route != null ? route.getNxDdrDriverUserId() : null);
            result.add(toDeliveryDriverRouteMap(route, driverMeta));
        }
        return result;
    }

    public static Map<String, Object> toDeliveryDriverRouteMap(NxDisDriverRouteEntity route) {
        return toDeliveryDriverRouteMap(route, null);
    }

    public static Map<String, Object> toDeliveryDriverRouteMap(NxDisDriverRouteEntity route,
                                                               DriverDispatchCandidateDto driverMeta) {
        if (route == null) {
            return null;
        }
        String driverName = DisRouteDispatchDriverNameHelper.resolveRouteDriverName(route, driverMeta);
        route.setDriverName(driverName);
        Map<String, Object> map = entityToMap(route);
        overlayDriverRouteOperationFields(route, map);
        map.put("driverUserId", route.getNxDdrDriverUserId());
        map.put("driverName", driverName);
        if (!DisRouteDispatchDriverNameHelper.isResolvedName(driverName)) {
            map.put("driverNameResolveStatus", "MISSING");
            map.put("driverNameMissingReason", DisRouteDispatchDriverNameHelper.MISSING_REASON);
        }
        DispatchDriverCardDto driverCard = DisRouteDispatchCardTemplateBuilder.buildDeliveryExecutionDriverCard(
                route, driverMeta);
        map.put("driverCard", toDispatchDriverCardMap(driverCard));
        List<Map<String, Object>> stopMaps = new ArrayList<Map<String, Object>>();
        if (route.getStops() != null) {
            for (NxDisRouteStopEntity stop : route.getStops()) {
                stopMaps.add(toDeliveryStopMap(stop, driverName));
            }
        }
        map.put("stops", stopMaps);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toDispatchDriverCardMap(DispatchDriverCardDto card) {
        if (card == null) {
            return Collections.emptyMap();
        }
        String json = JSON.toJSONString(card,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteDateUseDateFormat);
        return JSON.parseObject(json, LinkedHashMap.class);
    }

    public static List<Map<String, Object>> toDeliveryStopMaps(List<NxDisRouteStopEntity> stops, String driverName) {
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (NxDisRouteStopEntity stop : stops) {
            Map<String, Object> map = toDeliveryStopMap(stop, driverName);
            if (map != null) {
                result.add(map);
            }
        }
        return result;
    }

    public static Map<String, Object> toDeliveryStopMap(NxDisRouteStopEntity stop, String driverName) {
        Map<String, Object> map = toConfirmedCanonicalStopMap(stop);
        if (map == null) {
            return null;
        }
        NxDisShipmentTaskEntity task = stop != null ? stop.getShipmentTask() : null;
        if (driverName != null && !driverName.trim().isEmpty()) {
            map.put("driverName", driverName.trim());
        }
        overlayDeliveryExecutionFields(task, map);
        List<Map<String, Object>> orders = groupItemsIntoOrders(task, null);
        map.put("orders", orders);
        map.put("items", flattenOrderItems(orders));
        return map;
    }

    public static Map<String, Object> toLoadingDriverRouteMap(NxDisDriverRouteEntity route) {
        return toLoadingDriverRouteMap(route, null);
    }

    public static Map<String, Object> toLoadingDriverRouteMap(NxDisDriverRouteEntity route,
                                                                Map<Integer, Integer> purchaseStatusByOrderId) {
        if (route == null) {
            return null;
        }
        Map<String, Object> map = entityToMap(route);
        overlayDriverRouteOperationFields(route, map);
        List<Map<String, Object>> stopMaps = new ArrayList<Map<String, Object>>();
        if (route.getStops() != null) {
            for (NxDisRouteStopEntity stop : route.getStops()) {
                stopMaps.add(toLoadingStopMap(stop, purchaseStatusByOrderId));
            }
        }
        map.put("stops", stopMaps);
        return map;
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

    public static List<Map<String, Object>> toLoadingStopMaps(List<NxDisRouteStopEntity> stops) {
        return toLoadingStopMaps(stops, null);
    }

    public static List<Map<String, Object>> toLoadingStopMaps(List<NxDisRouteStopEntity> stops,
                                                             Map<Integer, Integer> purchaseStatusByOrderId) {
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (NxDisRouteStopEntity stop : stops) {
            Map<String, Object> map = toLoadingStopMap(stop, purchaseStatusByOrderId);
            if (map != null) {
                result.add(map);
            }
        }
        return result;
    }

    public static List<Map<String, Object>> toTaskMaps(List<NxDisShipmentTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (NxDisShipmentTaskEntity task : tasks) {
            result.add(toTaskMap(task));
        }
        return result;
    }

    public static Map<String, Object> toDriverRouteMap(NxDisDriverRouteEntity route) {
        if (route == null) {
            return null;
        }
        Map<String, Object> map = entityToMap(route);
        overlayDriverRouteOperationFields(route, map);
        List<Map<String, Object>> stopMaps = new ArrayList<Map<String, Object>>();
        if (route.getStops() != null) {
            for (NxDisRouteStopEntity stop : route.getStops()) {
                stopMaps.add(toStopMap(stop));
            }
        }
        map.put("stops", stopMaps);
        return map;
    }

    /**
     * driverRoutes.stops / confirmedStops 统一出口：只输出 canonical 字段，不暴露 nxDrs/nxDst 主合同。
     */
    public static Map<String, Object> toStopMap(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (isEphemeralSandboxStop(stop)) {
            return toSandboxSuggestedStopMap(stop);
        }
        if (isConfirmedExecutionStop(stop)) {
            return toConfirmedCanonicalStopMap(stop);
        }
        return toSandboxSuggestedStopMap(stop);
    }

    /** Phase 3a.1：已确认分店执行任务 — canonical 顶层字段，禁止前端从 shipmentTask 兜底。 */
    public static Map<String, Object> toConfirmedDeliveryStopMap(NxDisRouteStopEntity stop) {
        return toConfirmedCanonicalStopMap(stop);
    }

    public static Map<String, Object> toConfirmedDeliveryStopMapFromTask(NxDisShipmentTaskEntity task) {
        if (task == null || task.getNxDstId() == null) {
            return null;
        }
        NxDisRouteStopEntity shell = buildConfirmedStopShellFromTask(task);
        return toConfirmedCanonicalStopMap(shell);
    }

    private static NxDisRouteStopEntity buildConfirmedStopShellFromTask(NxDisShipmentTaskEntity task) {
        NxDisRouteStopEntity shell = new NxDisRouteStopEntity();
        shell.setShipmentTask(task);
        shell.setNxDrsShipmentTaskId(task.getNxDstId());
        shell.setNxDrsDepartmentId(task.getNxDstDepFatherId());
        shell.setNxDrsDepartmentName(task.getNxDstDepName());
        shell.setLiveDepartmentName(task.getLiveDepartmentName());
        shell.setNxDrsDriverRouteId(task.getNxDstDriverRouteId());
        shell.setStopSource(task.getStopSource() != null
                ? task.getStopSource() : DisRouteSandboxStopSource.CONFIRMED);
        shell.setSandboxStopKey(task.getSandboxStopKey());
        shell.setCanReturnToSandbox(task.getCanReturnToSandbox());
        shell.setReturnToSandboxActionLabel(task.getReturnToSandboxActionLabel());
        shell.setReturnToSandboxBlockedReason(task.getReturnToSandboxBlockedReason());
        shell.setReturnToSandboxWarning(task.getReturnToSandboxWarning());
        shell.setReturnToSandboxConfirmMessage(task.getReturnToSandboxConfirmMessage());
        shell.setCanMove(task.getCanMove());
        shell.setMoveBlockedReason(task.getMoveBlockedReason());
        shell.setNxDrsLegDistanceM(task.getNxDstLegDistanceM());
        shell.setNxDrsLegDurationS(task.getNxDstLegDurationS());
        shell.setLegDistanceProvider(task.getLegDistanceProvider());
        shell.setLegDistanceType(task.getLegDistanceType());
        shell.setNxDrsPlannedArrivalAt(task.getNxDstPlannedArrivalAt());
        shell.setNxDrsPlannedDepartureAt(task.getNxDstPlannedDepartureAt());
        shell.setNxDrsEarliestDeliveryTimeS(task.getNxDstEarliestDeliveryTimeS());
        shell.setNxDrsLatestDeliveryTimeS(task.getNxDstLatestDeliveryTimeS());
        shell.setNxDrsServiceMinutes(task.getNxDstServiceMinutes());
        return shell;
    }

    /** confirmed stop canonical 合同（driverRoutes.stops / confirmedStops 共用）。 */
    public static Map<String, Object> toConfirmedCanonicalStopMap(NxDisRouteStopEntity stop) {
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

    public static Map<String, Object> toEphemeralStopMap(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        return toSandboxSuggestedStopMap(stop);
    }

    /** Phase 3a.1b：沙盘建议站点对外瘦身结构（主流程不依赖 nxDrs/nxDst 实体字段）。 */
    public static Map<String, Object> toSandboxSuggestedStopMap(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("sandboxStopKey", stop.getSandboxStopKey());
        map.put("departmentId", stop.getNxDrsDepartmentId());
        map.put("departmentName", resolveDepartmentName(stop, task));
        map.put("stopSource", stop.getStopSource() != null
                ? stop.getStopSource() : DisRouteSandboxStopSource.SANDBOX_SUGGESTED);
        map.put("confirmViaSandbox", stop.getConfirmViaSandbox());
        map.put("suggestedDriverUserId", stop.getSuggestedDriverUserId());
        map.put("suggestedDriverName", stop.getSuggestedDriverName());
        map.put("scheduleMode", stop.getScheduleMode());
        map.put("fastestArrivalLabel", stop.getFastestArrivalLabel());
        overlayStopScheduleCanonicalFields(stop, map);
        map.put("timeBasisLabel", stop.getTimeBasisLabel());
        map.put("customerWindowLabel", stop.getCustomerWindowLabel());
        map.put("canConfirmCustomer", stop.getCanConfirmCustomer());
        map.put("confirmCustomerActionLabel", stop.getConfirmCustomerActionLabel());
        map.put("confirmCustomerBlockedReason", stop.getConfirmCustomerBlockedReason());
        List<Map<String, Object>> itemMaps = extractSandboxItemMaps(task);
        map.put("items", itemMaps);
        map.put("legDistanceM", stop.getNxDrsLegDistanceM());
        map.put("legDurationS", stop.getNxDrsLegDurationS());
        map.put("distanceProvider", stop.getLegDistanceProvider());
        map.put("distanceType", stop.getLegDistanceType());
        List<Integer> liveOrderIds = collectLiveOrderIdsFromItems(itemMaps);
        if (!liveOrderIds.isEmpty()) {
            map.put("liveOrderIds", liveOrderIds);
        }
        return map;
    }

    public static Map<String, Object> toTaskMap(NxDisShipmentTaskEntity task) {
        if (task == null) {
            return null;
        }
        Map<String, Object> map = entityToMap(task);
        overlayTaskOperationFields(task, map);
        if (isEphemeralSandboxTask(task)) {
            sanitizeEphemeralTaskMap(map);
        } else if (task.getNxDstId() != null) {
            overlayConfirmedTaskIds(task, map);
        }
        return map;
    }

    public static List<Map<String, Object>> toConfirmedDeliveryStopMaps(List<NxDisShipmentTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (NxDisShipmentTaskEntity task : tasks) {
            Map<String, Object> map = toConfirmedDeliveryStopMapFromTask(task);
            if (map != null) {
                result.add(map);
            }
        }
        return result;
    }

    public static List<Map<String, Object>> toPersistedTaskMaps(List<NxDisShipmentTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task == null || isEphemeralSandboxTask(task) || task.getNxDstId() == null) {
                continue;
            }
            result.add(toTaskMap(task));
        }
        return result;
    }

    private static void overlayPlanOperationFields(NxDisRoutePlanEntity plan, Map<String, Object> map) {
        map.put("canStartLoading", plan.getCanStartLoading());
        map.put("loadingBlockedReason", plan.getLoadingBlockedReason());
        map.put("operationHint", plan.getOperationHint());
        map.put("nxDrpDispatchBatchLabel", plan.getNxDrpDispatchBatchLabel());
        map.put("nxDrpFeasibilityStatusLabel", plan.getNxDrpFeasibilityStatusLabel());
        map.put("nxDrpScheduleStatusLabel", plan.getNxDrpScheduleStatusLabel());
        map.put("routeDateLabel", plan.getRouteDateLabel());
        map.put("planTemporalStatus", plan.getPlanTemporalStatus());
        map.put("planTemporalStatusLabel", plan.getPlanTemporalStatusLabel());
        map.put("dispatchBatchLabel", plan.getNxDrpDispatchBatchLabel());
    }

    private static void overlayDriverRouteOperationFields(NxDisDriverRouteEntity route, Map<String, Object> map) {
        DisRouteSandboxReadModelPartitionHelper.ensureRouteMetricsCanonical(route);
        map.put("canLoad", route.getCanLoad());
        map.put("loadBlockedReason", route.getLoadBlockedReason());
        map.put("canAssignMore", route.getCanAssignMore());
        map.put("assignBlockedReason", route.getAssignBlockedReason());
        map.put("nxDdrFeasibilityStatusLabel", route.getNxDdrFeasibilityStatusLabel());
        map.put("nxDdrIneligibleReasonLabel", route.getNxDdrIneligibleReasonLabel());
        map.put("routeOperationStatusLabel", route.getRouteOperationStatusLabel());
        map.put("assignedStopCount", route.getAssignedStopCount());
        map.put("suggestedStopCount", route.getSuggestedStopCount());
        map.put("lockedStopCount", route.getLockedStopCount());
        map.put("scheduleMode", route.getScheduleMode());
        map.put("scheduleModeLabel", route.getScheduleModeLabel());
        map.put("routeScheduleSummaryLabel", route.getRouteScheduleSummaryLabel());
        map.put("plannedDepartLabel", route.getPlannedDepartLabel());
        map.put("plannedFinishLabel", route.getPlannedFinishLabel());
        map.put("plannedReturnLabel", route.getPlannedReturnLabel());
        overlayRouteScheduleCanonicalFields(route, map);
        map.put("timeBasisLabel", route.getTimeBasisLabel());
        map.put("returnLegDistanceM", route.getReturnLegDistanceM());
        map.put("returnLegDurationS", route.getReturnLegDurationS());
        map.put("returnLegDistanceType", route.getReturnLegDistanceType());
        map.put("returnLegLabel", route.getReturnLegLabel());
        map.put("routeDistanceType", route.getRouteDistanceType());
        map.put("distanceProvider", route.getDistanceProvider());
        map.put("driverRouteId", route.getNxDdrId());
        map.put("driverUserId", route.getNxDdrDriverUserId());
        map.put("driverName", route.getDriverName());
        map.put("routeStatus", route.getRouteStatus());
        map.put("routeStatusLabel", route.getRouteStatusLabel());
        map.put("totalStopCount", route.getTotalStopCount());
        map.put("confirmedStopCount", route.getConfirmedStopCount());
        map.put("sandboxSuggestedStopCount", route.getSuggestedStopCount());
        map.put("totalDistanceM", route.getNxDdrTotalDistanceM() != null ? route.getNxDdrTotalDistanceM() : 0L);
        map.put("totalDurationS", route.getNxDdrTotalDurationS() != null ? route.getNxDdrTotalDurationS() : 0L);
        map.put("routeScope", route.getRouteScope() != null
                ? route.getRouteScope() : DisRouteSandboxReadModelPartitionHelper.ROUTE_SCOPE_SANDBOX);
        map.put("sandboxEligible", route.getSandboxEligible() != null ? route.getSandboxEligible() : Boolean.TRUE);
        map.put("canDepart", route.getCanDepart());
        map.put("departActionLabel", route.getDepartActionLabel());
        map.put("departBlockedReason", route.getDepartBlockedReason());
        map.put("departConfirmMessage", route.getDepartConfirmMessage());
        map.put("departWarning", route.getDepartWarning());
        map.put("unprintedBillCount", route.getUnprintedBillCount());
        String routeStatus = route.getRouteStatus() != null
                ? route.getRouteStatus()
                : (route.getNxDdrRouteStatus() != null ? route.getNxDdrRouteStatus().trim().toUpperCase() : null);
        if (routeStatus != null) {
            map.put("nxDdrRouteStatus", routeStatus);
        }
        if (route.getNxDdrActualDepartAt() != null) {
            String formatted = RouteDispatchDateFormat.format(route.getNxDdrActualDepartAt());
            map.put("nxDdrActualDepartAt", formatted);
            map.put("actualDepartAt", formatted);
            map.put("departedAt", formatted);
        } else if (route.getDepartedAt() != null) {
            String formatted = RouteDispatchDateFormat.format(route.getDepartedAt());
            map.put("actualDepartAt", formatted);
            map.put("departedAt", formatted);
        }
        if (route.getAllStopsDelivered() != null) {
            map.put("allStopsDelivered", route.getAllStopsDelivered());
        }
        if (route.getCanCompleteRoute() != null) {
            map.put("canCompleteRoute", route.getCanCompleteRoute());
        }
    }

    private static void overlayStopOperationFields(NxDisRouteStopEntity stop, Map<String, Object> map) {
        map.put("canAssign", stop.getCanAssign());
        map.put("assignBlockedReason", stop.getAssignBlockedReason());
        map.put("canConfirmLoad", stop.getCanConfirmLoad() != null ? stop.getCanConfirmLoad() : Boolean.FALSE);
        map.put("confirmLoadBlockedReason", stop.getConfirmLoadBlockedReason());
        map.put("canMove", stop.getCanMove());
        map.put("moveBlockedReason", stop.getMoveBlockedReason());
        map.put("canUnlock", stop.getCanUnlock());
        map.put("unlockBlockedReason", stop.getUnlockBlockedReason());
        map.put("operationStatusLabel", stop.getOperationStatusLabel());
        map.put("nxDrsTimeWindowStatusLabel", stop.getNxDrsTimeWindowStatusLabel());
        map.put("plannedArrivalLabel", stop.getPlannedArrivalLabel());
        map.put("stopTemporalStatus", stop.getStopTemporalStatus());
        map.put("stopTemporalStatusLabel", stop.getStopTemporalStatusLabel());
        map.put("sandboxStopKey", stop.getSandboxStopKey());
        map.put("stopSource", stop.getStopSource());
        map.put("confirmViaSandbox", stop.getConfirmViaSandbox());
        map.put("scheduleMode", stop.getScheduleMode());
        map.put("scheduleModeLabel", stop.getScheduleModeLabel());
        map.put("timeAnchorAt", stop.getTimeAnchorAt());
        map.put("timeAnchorLabel", stop.getTimeAnchorLabel());
        map.put("plannedDepartLabel", stop.getPlannedDepartLabel());
        map.put("fastestArrivalLabel", stop.getFastestArrivalLabel());
        map.put("customerWindowLabel", stop.getCustomerWindowLabel());
        map.put("isAfterCustomerWindow", stop.getIsAfterCustomerWindow());
        map.put("timeBasis", stop.getTimeBasis());
        map.put("timeBasisLabel", stop.getTimeBasisLabel());
        map.put("liveDepartmentName", stop.getLiveDepartmentName());
        map.put("canConfirmCustomer", stop.getCanConfirmCustomer());
        map.put("confirmCustomerActionLabel", stop.getConfirmCustomerActionLabel());
        map.put("confirmCustomerBlockedReason", stop.getConfirmCustomerBlockedReason());
        map.put("canReturnToSandbox", stop.getCanReturnToSandbox());
        map.put("returnToSandboxActionLabel", stop.getReturnToSandboxActionLabel());
        map.put("returnToSandboxBlockedReason", stop.getReturnToSandboxBlockedReason());
        map.put("returnToSandboxWarning", stop.getReturnToSandboxWarning());
        map.put("returnToSandboxConfirmMessage", stop.getReturnToSandboxConfirmMessage());
        map.put("suggestedDriverUserId", stop.getSuggestedDriverUserId());
        map.put("suggestedDriverName", stop.getSuggestedDriverName());
        map.put("legDistanceM", stop.getNxDrsLegDistanceM());
        map.put("legDurationS", stop.getNxDrsLegDurationS());
        map.put("distanceProvider", stop.getLegDistanceProvider());
        map.put("distanceType", stop.getLegDistanceType());
        overlayDispatchParamFields(stop, map);
    }

    private static void overlayTaskOperationFields(NxDisShipmentTaskEntity task, Map<String, Object> map) {
        map.put("canAssign", task.getCanAssign());
        map.put("assignBlockedReason", task.getAssignBlockedReason());
        map.put("canConfirmLoad", task.getCanConfirmLoad() != null ? task.getCanConfirmLoad() : Boolean.FALSE);
        map.put("confirmLoadBlockedReason", task.getConfirmLoadBlockedReason());
        map.put("canMove", task.getCanMove());
        map.put("moveBlockedReason", task.getMoveBlockedReason());
        map.put("canUnlock", task.getCanUnlock());
        map.put("unlockBlockedReason", task.getUnlockBlockedReason());
        map.put("operationStatusLabel", task.getOperationStatusLabel());
        map.put("nxDstStatusLabel", task.getNxDstStatusLabel());
        map.put("canConfirmCustomer", task.getCanConfirmCustomer());
        map.put("confirmCustomerActionLabel", task.getConfirmCustomerActionLabel());
        map.put("confirmCustomerBlockedReason", task.getConfirmCustomerBlockedReason());
        map.put("canReturnToSandbox", task.getCanReturnToSandbox());
        map.put("returnToSandboxActionLabel", task.getReturnToSandboxActionLabel());
        map.put("returnToSandboxBlockedReason", task.getReturnToSandboxBlockedReason());
        map.put("returnToSandboxWarning", task.getReturnToSandboxWarning());
        map.put("returnToSandboxConfirmMessage", task.getReturnToSandboxConfirmMessage());
        map.put("suggestedDriverUserId", task.getSuggestedDriverUserId());
        map.put("suggestedDriverName", task.getSuggestedDriverName());
        map.put("legDistanceM", task.getNxDstLegDistanceM());
        map.put("legDurationS", task.getNxDstLegDurationS());
        map.put("distanceProvider", task.getLegDistanceProvider());
        map.put("distanceType", task.getLegDistanceType());
        overlayDispatchParamFields(task, map);
    }

    private static void overlayDispatchParamFields(NxDisShipmentTaskEntity task, Map<String, Object> map) {
        map.put("customerTier", task.getNxDstCustomerTier());
        map.put("customerTierLabel", task.getCustomerTierLabel());
        map.put("priorityWeight", task.getNxDstPriorityWeight());
        map.put("orderCount", task.getNxDstOrderCount());
        map.put("itemCount", task.getNxDstItemCount());
        map.put("totalQuantity", task.getNxDstTotalQuantity());
        map.put("earliestDeliveryTimeS", task.getNxDstEarliestDeliveryTimeS());
        map.put("latestDeliveryTimeS", task.getNxDstLatestDeliveryTimeS());
        map.put("serviceMinutes", task.getNxDstServiceMinutes());
        map.put("timeWindowOverrideFlag", task.getNxDstTimeWindowOverrideFlag() != null
                && task.getNxDstTimeWindowOverrideFlag() == 1);
        map.put("timeWindowAdjustReason", task.getNxDstTimeWindowAdjustReason());
        map.put("priorityScorePreview", task.getPriorityScorePreview());
        map.put("priorityReason", task.getPriorityReason());
        map.put("sandboxStopKey", task.getSandboxStopKey());
        map.put("stopSource", task.getStopSource());
        map.put("confirmViaSandbox", task.getConfirmViaSandbox());
        map.put("liveDepartmentName", task.getLiveDepartmentName());
        overlayBillPrintHints(task, map);
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

    private static void overlayConfirmedTaskIds(NxDisShipmentTaskEntity task, Map<String, Object> map) {
        if (task == null || map == null || task.getNxDstId() == null) {
            return;
        }
        map.put("deliveryStopId", task.getNxDstId());
        if (task.getNxDstDriverRouteId() != null) {
            map.put("driverRouteId", task.getNxDstDriverRouteId());
        }
        if (task.getNxDstAssignedDriverUserId() != null) {
            map.put("driverUserId", task.getNxDstAssignedDriverUserId());
        }
        removeKeys(map, "taskId", "nxDstId", "nxDrsId", "routeStopId", "nxDrsShipmentTaskId");
    }

    private static void overlayDispatchParamFields(NxDisRouteStopEntity stop, Map<String, Object> map) {
        map.put("customerTier", stop.getNxDrsCustomerTier());
        map.put("customerTierLabel", stop.getCustomerTierLabel());
        map.put("priorityWeight", stop.getNxDrsPriorityWeight());
        map.put("orderCount", stop.getNxDrsOrderCount());
        map.put("itemCount", stop.getNxDrsItemCount());
        map.put("totalQuantity", stop.getNxDrsTotalQuantity());
        map.put("earliestDeliveryTimeS", stop.getNxDrsEarliestDeliveryTimeS());
        map.put("latestDeliveryTimeS", stop.getNxDrsLatestDeliveryTimeS());
        map.put("serviceMinutes", stop.getNxDrsServiceMinutes());
        map.put("timeWindowOverrideFlag", stop.getNxDrsTimeWindowOverrideFlag() != null
                && stop.getNxDrsTimeWindowOverrideFlag() == 1);
        map.put("timeWindowAdjustReason", stop.getNxDrsTimeWindowAdjustReason());
        map.put("priorityScorePreview", stop.getPriorityScorePreview());
        map.put("priorityReason", stop.getPriorityReason());
    }

    private static boolean isEphemeralSandboxStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        if (Boolean.TRUE.equals(stop.getConfirmViaSandbox())) {
            return true;
        }
        String source = stop.getStopSource();
        return DisRouteSandboxStopSource.SANDBOX_SUGGESTED.equals(source)
                || DisRouteSandboxStopSource.UNASSIGNED.equals(source);
    }

    private static boolean isEphemeralSandboxTask(NxDisShipmentTaskEntity task) {
        if (task == null) {
            return false;
        }
        if (task.getNxDstId() == null) {
            return true;
        }
        if (Boolean.TRUE.equals(task.getConfirmViaSandbox())) {
            return true;
        }
        String source = task.getStopSource();
        return DisRouteSandboxStopSource.SANDBOX_SUGGESTED.equals(source)
                || DisRouteSandboxStopSource.UNASSIGNED.equals(source);
    }

    private static boolean isConfirmedExecutionStop(NxDisRouteStopEntity stop) {
        if (stop == null || isEphemeralSandboxStop(stop)) {
            return false;
        }
        if (stop.getShipmentTask() != null && stop.getShipmentTask().getNxDstId() != null) {
            return true;
        }
        return DisRouteSandboxStopSource.CONFIRMED.equals(stop.getStopSource());
    }

    @SuppressWarnings("unchecked")
    private static void sanitizeConfirmedStopMap(Map<String, Object> map) {
        if (map == null) {
            return;
        }
        Object nestedTask = map.get("shipmentTask");
        if (nestedTask instanceof Map) {
            Map<String, Object> taskMap = (Map<String, Object>) nestedTask;
            Object dstId = taskMap.get("nxDstId");
            if (dstId instanceof Number) {
                map.put("deliveryStopId", ((Number) dstId).intValue());
            }
            Object driverRouteId = taskMap.get("nxDstDriverRouteId");
            if (driverRouteId instanceof Number) {
                map.put("driverRouteId", ((Number) driverRouteId).intValue());
            }
            Object driverUserId = taskMap.get("nxDstAssignedDriverUserId");
            if (driverUserId instanceof Number) {
                map.put("driverUserId", ((Number) driverUserId).intValue());
            }
            Object status = taskMap.get("nxDstStatus");
            if (status != null) {
                map.put("status", status);
            }
            for (String key : new String[]{"billPrintStatus", "unprintedBillCount", "billPrintWarning",
                    "activeItemCount", "printedItemCount",
                    "canReturnToSandbox", "returnToSandboxActionLabel", "returnToSandboxBlockedReason",
                    "returnToSandboxWarning", "returnToSandboxConfirmMessage"}) {
                if (taskMap.containsKey(key)) {
                    map.put(key, taskMap.get(key));
                }
            }
            sanitizeConfirmedTaskMap(taskMap);
        }
        removeKeys(map,
                "nxDrsId", "nxDrsShipmentTaskId", "nxDrsDriverRouteId",
                "taskId", "routeStopId", "nxDstId", "shipmentTaskId");
        overlayLiveOrderIds(map);
    }

    private static void sanitizeConfirmedTaskMap(Map<String, Object> map) {
        if (map == null) {
            return;
        }
        Object dstId = map.get("nxDstId");
        if (dstId instanceof Number) {
            map.put("deliveryStopId", ((Number) dstId).intValue());
        }
        removeKeys(map, "nxDstId", "taskId", "nxDstPlanId", "planId");
    }

    @SuppressWarnings("unchecked")
    private static void sanitizeEphemeralStopMap(Map<String, Object> map) {
        if (map == null) {
            return;
        }
        removeKeys(map,
                "nxDrsId", "nxDrsShipmentTaskId", "nxDrsDriverRouteId",
                "taskId", "routeStopId", "driverRouteId");
        Object nestedTask = map.get("shipmentTask");
        if (nestedTask instanceof Map) {
            sanitizeEphemeralTaskMap((Map<String, Object>) nestedTask);
        }
        overlayLiveOrderIds(map);
    }

    @SuppressWarnings("unchecked")
    private static void sanitizeEphemeralTaskMap(Map<String, Object> map) {
        if (map == null) {
            return;
        }
        removeKeys(map,
                "nxDstId", "nxDstPlanId", "taskId", "planId",
                "nxDstBillId", "billId", "historyOrderId");
        Object items = map.get("items");
        if (items instanceof List) {
            for (Object item : (List<?>) items) {
                if (item instanceof Map) {
                    removeKeys((Map<String, Object>) item,
                            "nxDstiId", "nxDstiTaskId", "nxDstiBillId", "nxDstiHistoryOrderId");
                }
            }
        }
        overlayLiveOrderIds(map);
    }

    @SuppressWarnings("unchecked")
    private static void overlayLiveOrderIds(Map<String, Object> map) {
        if (map == null || map.containsKey("liveOrderIds")) {
            return;
        }
        Object items = map.get("items");
        if (!(items instanceof List)) {
            Object task = map.get("shipmentTask");
            if (task instanceof Map) {
                items = ((Map<String, Object>) task).get("items");
            }
        }
        if (!(items instanceof List)) {
            return;
        }
        List<Integer> liveOrderIds = new ArrayList<Integer>();
        for (Object item : (List<?>) items) {
            if (!(item instanceof Map)) {
                continue;
            }
            Object liveOrderId = ((Map<String, Object>) item).get("nxDstiLiveOrderId");
            if (liveOrderId == null) {
                liveOrderId = ((Map<String, Object>) item).get("liveOrderId");
            }
            if (liveOrderId instanceof Number) {
                liveOrderIds.add(((Number) liveOrderId).intValue());
            }
        }
        if (!liveOrderIds.isEmpty()) {
            map.put("liveOrderIds", liveOrderIds);
        }
    }

    private static void removeKeys(Map<String, Object> map, String... keys) {
        if (map == null || keys == null) {
            return;
        }
        for (String key : keys) {
            map.remove(key);
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

    /** stop 顶层排程 canonical 字段（datetime + label + serviceMinutes）。 */
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

    /** driverRoute 顶层排程 canonical datetime 字段。 */
    private static void overlayRouteScheduleCanonicalFields(NxDisDriverRouteEntity route,
                                                            Map<String, Object> map) {
        if (route == null || map == null) {
            return;
        }
        if (route.getNxDdrPlannedDepartAt() != null) {
            map.put("plannedDepartAt", RouteDispatchDateFormat.format(route.getNxDdrPlannedDepartAt()));
        }
        Date plannedReturnAt = route.getPlannedReturnAt() != null
                ? route.getPlannedReturnAt() : route.getNxDdrPlannedFinishAt();
        if (plannedReturnAt != null) {
            map.put("plannedReturnAt", RouteDispatchDateFormat.format(plannedReturnAt));
            map.put("plannedFinishAt", RouteDispatchDateFormat.format(plannedReturnAt));
        }
        if (route.getLastStopArrivalAt() != null) {
            map.put("lastStopArrivalAt", RouteDispatchDateFormat.format(route.getLastStopArrivalAt()));
        }
        if (route.getLastStopDepartureAt() != null) {
            map.put("lastStopDepartureAt", RouteDispatchDateFormat.format(route.getLastStopDepartureAt()));
        }
    }

    /** 排程到达时刻：优先服务开始（含等待），与出发倒推公式一致。 */
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

    /** stop 顶层 plannedDepartureLabel；优先读 enrichment 字段，否则从 stop/task 排程时间格式化。 */
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

    private static void overlayDeliveryExecutionFields(NxDisShipmentTaskEntity task, Map<String, Object> map) {
        if (task == null || map == null) {
            return;
        }
        if (task.getNxDstDeliveredAt() != null) {
            String formatted = RouteDispatchDateFormat.format(task.getNxDstDeliveredAt());
            map.put("deliveredAt", formatted);
            map.put("completedAt", formatted);
        }
        if (task.getNxDstDeliveryRemark() != null) {
            map.put("remark", task.getNxDstDeliveryRemark());
        }
        if (task.getNxDstExceptionType() != null) {
            map.put("exceptionType", task.getNxDstExceptionType());
            map.put("exceptionTypeLabel", DisRouteDeliveryExceptionType.label(task.getNxDstExceptionType()));
        }
        if (task.getNxDstExceptionRemark() != null) {
            map.put("exceptionRemark", task.getNxDstExceptionRemark());
        }
        if (task.getNxDstExceptionAt() != null) {
            map.put("exceptionAt", RouteDispatchDateFormat.format(task.getNxDstExceptionAt()));
        }
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

    @SuppressWarnings("unchecked")
    private static Map<String, Object> entityToMap(Object entity) {
        String json = JSON.toJSONString(entity,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteDateUseDateFormat);
        Map<String, Object> map = JSON.parseObject(json, LinkedHashMap.class);
        normalizeDateFields(map);
        return map;
    }

    @SuppressWarnings("unchecked")
    private static void normalizeDateFields(Map<String, Object> map) {
        if (map == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Number) {
                String key = entry.getKey();
                if (key != null && (key.endsWith("At") || key.contains("Date") || key.contains("Time"))) {
                    long millis = ((Number) value).longValue();
                    if (millis > 1_000_000_000_000L) {
                        entry.setValue(RouteDispatchDateFormat.format(new java.util.Date(millis)));
                    }
                }
            } else if (value instanceof Map) {
                normalizeDateFields((Map<String, Object>) value);
            } else if (value instanceof List) {
                for (Object item : (List<?>) value) {
                    if (item instanceof Map) {
                        normalizeDateFields((Map<String, Object>) item);
                    }
                }
            }
        }
    }
}
