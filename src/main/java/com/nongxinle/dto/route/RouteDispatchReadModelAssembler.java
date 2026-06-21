package com.nongxinle.dto.route;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.RouteDispatchDateFormat;

import java.util.ArrayList;
import java.util.Collections;
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
        if (plan.getDriverRoutes() == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            if (route.getStops() == null) {
                continue;
            }
            for (NxDisRouteStopEntity stop : route.getStops()) {
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
        }
        return map;
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
        if (route.getStops() != null) {
            List<Map<String, Object>> stopMaps = new ArrayList<Map<String, Object>>();
            for (NxDisRouteStopEntity stop : route.getStops()) {
                stopMaps.add(toStopMap(stop));
            }
            map.put("stops", stopMaps);
        }
        return map;
    }

    public static Map<String, Object> toStopMap(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        Map<String, Object> map = entityToMap(stop);
        overlayStopOperationFields(stop, map);
        if (stop.getShipmentTask() != null) {
            map.put("shipmentTask", toTaskMap(stop.getShipmentTask()));
        }
        return map;
    }

    public static Map<String, Object> toTaskMap(NxDisShipmentTaskEntity task) {
        if (task == null) {
            return null;
        }
        Map<String, Object> map = entityToMap(task);
        overlayTaskOperationFields(task, map);
        return map;
    }

    private static void overlayPlanOperationFields(NxDisRoutePlanEntity plan, Map<String, Object> map) {
        map.put("canStartLoading", plan.getCanStartLoading());
        map.put("loadingBlockedReason", plan.getLoadingBlockedReason());
        map.put("operationHint", plan.getOperationHint());
        map.put("nxDrpDispatchBatchLabel", plan.getNxDrpDispatchBatchLabel());
        map.put("nxDrpFeasibilityStatusLabel", plan.getNxDrpFeasibilityStatusLabel());
        map.put("nxDrpScheduleStatusLabel", plan.getNxDrpScheduleStatusLabel());
    }

    private static void overlayDriverRouteOperationFields(NxDisDriverRouteEntity route, Map<String, Object> map) {
        map.put("canLoad", route.getCanLoad());
        map.put("loadBlockedReason", route.getLoadBlockedReason());
        map.put("canAssignMore", route.getCanAssignMore());
        map.put("assignBlockedReason", route.getAssignBlockedReason());
        map.put("nxDdrFeasibilityStatusLabel", route.getNxDdrFeasibilityStatusLabel());
        map.put("nxDdrIneligibleReasonLabel", route.getNxDdrIneligibleReasonLabel());
    }

    private static void overlayStopOperationFields(NxDisRouteStopEntity stop, Map<String, Object> map) {
        map.put("canAssign", stop.getCanAssign());
        map.put("assignBlockedReason", stop.getAssignBlockedReason());
        map.put("canConfirmLoad", stop.getCanConfirmLoad());
        map.put("confirmLoadBlockedReason", stop.getConfirmLoadBlockedReason());
        map.put("canMove", stop.getCanMove());
        map.put("moveBlockedReason", stop.getMoveBlockedReason());
        map.put("canUnlock", stop.getCanUnlock());
        map.put("unlockBlockedReason", stop.getUnlockBlockedReason());
        map.put("operationStatusLabel", stop.getOperationStatusLabel());
        map.put("nxDrsTimeWindowStatusLabel", stop.getNxDrsTimeWindowStatusLabel());
        overlayDispatchParamFields(stop, map);
    }

    private static void overlayTaskOperationFields(NxDisShipmentTaskEntity task, Map<String, Object> map) {
        map.put("canAssign", task.getCanAssign());
        map.put("assignBlockedReason", task.getAssignBlockedReason());
        map.put("canConfirmLoad", task.getCanConfirmLoad());
        map.put("confirmLoadBlockedReason", task.getConfirmLoadBlockedReason());
        map.put("canMove", task.getCanMove());
        map.put("moveBlockedReason", task.getMoveBlockedReason());
        map.put("canUnlock", task.getCanUnlock());
        map.put("unlockBlockedReason", task.getUnlockBlockedReason());
        map.put("operationStatusLabel", task.getOperationStatusLabel());
        map.put("nxDstStatusLabel", task.getNxDstStatusLabel());
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
