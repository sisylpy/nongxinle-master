package com.nongxinle.todaydispatch;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteDispatchLabels;
import com.nongxinle.route.DisRouteSandboxDisplayFormatHelper;
import com.nongxinle.route.DisRouteSandboxDriverTerminalStopPresentationHelper;
import com.nongxinle.route.DisRouteSandboxTodayTimelineBuilder;
import com.nongxinle.route.DisRouteTemporalHelper;
import com.nongxinle.route.DisShipmentTaskStatus;
import com.nongxinle.route.RouteCoordinateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 司机装车/配送页 — 从 TodayDispatchResult 直出扁平 pageViewModel。 */
@Component
public class DriverTerminalPageAssembler {

    private static final String NAV_PENDING_LABEL = "导航功能待接入";
    private static final String VIEW_ORDER_DETAIL_LABEL = "查看详细订单";
    private static final String CARD_DRIVER_ROUTE = "DRIVER_ROUTE";

    @Autowired
    private DispatchPageAssembler dispatchPageAssembler;

    public Map<String, Object> assembleLoading(TodayDispatchResult result,
                                               Integer driverUserId,
                                               String driverName) throws Exception {
        TodayDispatchResult scoped = scopeToDriver(result, driverUserId);
        Map<String, Object> pageViewModel = dispatchPageAssembler.assemble(scoped);
        return overlayDriverTerminal(pageViewModel, scoped, driverUserId, driverName, "LOADING", true);
    }

    public Map<String, Object> assembleDelivery(TodayDispatchResult result,
                                                Integer driverUserId,
                                                String driverName) throws Exception {
        TodayDispatchResult scoped = scopeToDriver(result, driverUserId);
        Map<String, Object> pageViewModel = dispatchPageAssembler.assemble(scoped);
        return overlayDriverTerminal(pageViewModel, scoped, driverUserId, driverName, "DELIVERY", false);
    }

    private static TodayDispatchResult scopeToDriver(TodayDispatchResult result, Integer driverUserId) {
        if (result == null || driverUserId == null) {
            return result;
        }
        TodayDispatchResult scoped = result;
        List<DriverRoutePlan> routes = new ArrayList<DriverRoutePlan>();
        if (result.getSuggestedRoutes() != null) {
            for (DriverRoutePlan route : result.getSuggestedRoutes()) {
                if (route != null && driverUserId.equals(route.getDriverUserId())) {
                    routes.add(route);
                }
            }
        }
        scoped.setSuggestedRoutes(routes);
        return scoped;
    }

    private Map<String, Object> overlayDriverTerminal(Map<String, Object> pageViewModel,
                                                     TodayDispatchResult result,
                                                     Integer driverUserId,
                                                     String driverName,
                                                     String phase,
                                                     boolean loading) {
        Map<String, Object> vm = pageViewModel != null
                ? new LinkedHashMap<String, Object>(pageViewModel)
                : new LinkedHashMap<String, Object>();

        DriverRoutePlan routePlan = firstRoute(result);
        List<Map<String, Object>> stopList = buildStopList(routePlan);

        vm.put("phase", loading ? "LOADING" : "DELIVERY");
        vm.put("driverUserId", driverUserId);
        vm.put("driverName", driverName);
        vm.put("stopList", stopList);
        vm.put("availableDrivers", Collections.emptyList());

        NxDisRoutePlanEntity plan = result != null && result.getCompute() != null
                ? result.getCompute().getMergedPlan() : null;
        vm.put("planId", plan != null ? plan.getNxDrpId() : null);
        vm.put("driverRouteId", resolveDriverRouteId(plan, driverUserId));

        Date serverNow = result != null && result.getServerNow() != null
                ? result.getServerNow() : new Date();
        NxDisDriverRouteEntity driverRoute = resolveDriverRouteEntity(plan, driverUserId);
        overlayDriverTerminalTimeline(vm, routePlan, driverRoute, serverNow, loading);
        if (loading) {
            overlayLoadingDepart(vm, routePlan, stopList);
            overlayLoadingPageHeader(vm, routePlan);
        } else {
            vm.put("navPendingLabel", NAV_PENDING_LABEL);
            overlayDeliveryPageHeader(vm, routePlan);
        }
        if (!vm.containsKey("mapOverview")) {
            vm.put("mapOverview", defaultMapOverview(result));
        }
        return vm;
    }

    private static void overlayLoadingPageHeader(Map<String, Object> vm, DriverRoutePlan routePlan) {
        Map<String, Object> header = castMap(vm.get("pageHeader"));
        if (header == null) {
            return;
        }
        int stopCount = routePlan != null && routePlan.getStops() != null ? routePlan.getStops().size() : 0;
        if (stopCount > 0) {
            header.put("operationHint", "点击门店下方按钮查看订单详情");
        } else {
            header.put("operationHint", "请等待老板分派客户");
        }
    }

    private static void overlayDeliveryPageHeader(Map<String, Object> vm, DriverRoutePlan routePlan) {
        Map<String, Object> header = castMap(vm.get("pageHeader"));
        if (header == null) {
            return;
        }
        int stopCount = routePlan != null && routePlan.getStops() != null ? routePlan.getStops().size() : 0;
        if (stopCount > 0) {
            header.put("operationHint", "按顺序配送，在门店卡片下方点击「送货完成」");
        } else {
            header.put("operationHint", "请等待老板分派客户，或确认已从装车页出发");
        }
    }

    private static void overlayDriverTerminalTimeline(Map<String, Object> vm,
                                                      DriverRoutePlan routePlan,
                                                      NxDisDriverRouteEntity driverRoute,
                                                      Date serverNow,
                                                      boolean loadingPhase) {
        List<Map<String, Object>> sections = castList(vm.get("sections"));
        if (sections == null || routePlan == null) {
            return;
        }
        List<NxDisRouteStopEntity> orderedStops = collectOrderedStops(routePlan);
        int currentStopIndex = loadingPhase ? -1
                : DisRouteSandboxDriverTerminalStopPresentationHelper
                        .resolveCurrentDeliveryStopIndex(orderedStops, driverRoute);
        for (Map<String, Object> section : sections) {
            List<Map<String, Object>> cards = castList(section != null ? section.get("cards") : null);
            if (cards == null) {
                continue;
            }
            for (Map<String, Object> card : cards) {
                if (card == null || !CARD_DRIVER_ROUTE.equals(String.valueOf(card.get("cardType")))) {
                    continue;
                }
                card.remove("primaryAction");
                List<Map<String, Object>> timeline = castList(card.get("timeline"));
                if (timeline == null) {
                    continue;
                }
                overlayDriverTerminalTimelineStops(
                        timeline, orderedStops, driverRoute, currentStopIndex, serverNow, loadingPhase);
            }
        }
    }

    private static void overlayDriverTerminalTimelineStops(List<Map<String, Object>> timeline,
                                                           List<NxDisRouteStopEntity> orderedStops,
                                                           NxDisDriverRouteEntity driverRoute,
                                                           int currentStopIndex,
                                                           Date serverNow,
                                                           boolean loadingPhase) {
        int stopNodeIndex = 0;
        for (Map<String, Object> node : timeline) {
            if (node == null || !"stop".equals(node.get("type"))) {
                continue;
            }
            node.remove("goodsSummary");
            node.remove("primaryAction");
            NxDisRouteStopEntity stop = stopNodeIndex < orderedStops.size()
                    ? orderedStops.get(stopNodeIndex) : null;
            NxDisShipmentTaskEntity task = stop != null ? stop.getShipmentTask() : null;
            Integer taskId = task != null ? task.getNxDstId() : null;
            Integer deliveryStopId = resolveDeliveryStopId(stop, task);
            if (taskId != null) {
                node.put("taskId", taskId);
            }
            if (deliveryStopId != null) {
                node.put("deliveryStopId", deliveryStopId);
            }
            node.put("seq", stopNodeIndex + 1);
            if (loadingPhase) {
                Map<String, Object> viewOrder = new LinkedHashMap<String, Object>();
                viewOrder.put("label", VIEW_ORDER_DETAIL_LABEL);
                viewOrder.put("enabled", deliveryStopId != null || taskId != null);
                node.put("viewOrderDetail", viewOrder);
            }
            overlayLegMetricsFromStop(node, stop);
            stopNodeIndex++;
        }
        DisRouteSandboxTodayTimelineBuilder.enrichStopLegPresentation(timeline);
        stopNodeIndex = 0;
        for (Map<String, Object> node : timeline) {
            if (node == null || !"stop".equals(node.get("type"))) {
                continue;
            }
            NxDisRouteStopEntity stop = stopNodeIndex < orderedStops.size()
                    ? orderedStops.get(stopNodeIndex) : null;
            NxDisShipmentTaskEntity task = stop != null ? stop.getShipmentTask() : null;
            DisRouteSandboxDriverTerminalStopPresentationHelper.applyDriverTerminalUnifiedStopCard(
                    node, stop, task, loadingPhase, stopNodeIndex, currentStopIndex, driverRoute, orderedStops, serverNow);
            if (!loadingPhase) {
                DisRouteSandboxDriverTerminalStopPresentationHelper.applyDriverTerminalDeliveryActions(
                        node, stopNodeIndex, currentStopIndex);
                if (Boolean.TRUE.equals(node.get("showNav"))) {
                    overlayNavTarget(node, stop);
                }
            }
            stopNodeIndex++;
        }
    }

    private static List<NxDisRouteStopEntity> collectOrderedStops(DriverRoutePlan routePlan) {
        List<NxDisRouteStopEntity> orderedStops = new ArrayList<NxDisRouteStopEntity>();
        if (routePlan == null || routePlan.getStops() == null) {
            return orderedStops;
        }
        for (CustomerStopPlan stopPlan : routePlan.getStops()) {
            if (stopPlan != null && stopPlan.getSourceStop() != null) {
                orderedStops.add(stopPlan.getSourceStop());
            }
        }
        return orderedStops;
    }

    private static void overlayLegMetricsFromStop(Map<String, Object> node, NxDisRouteStopEntity stop) {
        if (node == null || stop == null) {
            return;
        }
        Long legDist = DisRouteSandboxDisplayFormatHelper.resolveLegDistanceM(stop);
        Long legDur = DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop);
        String distanceText = DisRouteSandboxDisplayFormatHelper.formatDistanceText(legDist);
        String durationText = DisRouteSandboxDisplayFormatHelper.formatDurationText(legDur);
        if (distanceText != null) {
            node.put("distanceText", distanceText);
        }
        if (durationText != null) {
            node.put("durationText", durationText);
        }
        String legDistanceLabel = DisRouteSandboxTodayTimelineBuilder.joinLegText(distanceText, durationText);
        if (legDistanceLabel != null) {
            node.put("legDistanceLabel", legDistanceLabel);
        }
    }

    private static void overlayNavTarget(Map<String, Object> node, NxDisRouteStopEntity stop) {
        if (node == null || stop == null) {
            return;
        }
        Double lat = null;
        Double lng = null;
        if (RouteCoordinateUtils.isValidCoordinate(stop.getNxDrsLat(), stop.getNxDrsLng())) {
            lat = parseDouble(stop.getNxDrsLat());
            lng = parseDouble(stop.getNxDrsLng());
        } else {
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task != null && RouteCoordinateUtils.isValidCoordinate(task.getNxDstLat(), task.getNxDstLng())) {
                lat = parseDouble(task.getNxDstLat());
                lng = parseDouble(task.getNxDstLng());
            }
        }
        if (lat != null && lng != null) {
            node.put("navLat", lat);
            node.put("navLng", lng);
        }
        String address = stop.getNxDrsAddress();
        if (address == null || address.trim().isEmpty()) {
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task != null && task.getNxDstAddress() != null) {
                address = task.getNxDstAddress();
            }
        }
        if (address != null && !address.trim().isEmpty()) {
            node.put("navAddress", address.trim());
        }
    }

    private static Integer resolveDeliveryStopId(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        if (task != null && task.getNxDstId() != null) {
            return task.getNxDstId();
        }
        if (stop != null && stop.getNxDrsShipmentTaskId() != null) {
            return stop.getNxDrsShipmentTaskId();
        }
        return null;
    }

    private static Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static NxDisDriverRouteEntity resolveDriverRouteEntity(NxDisRoutePlanEntity plan, Integer driverUserId) {
        if (plan == null || driverUserId == null) {
            return null;
        }
        NxDisDriverRouteEntity route = findRoute(plan.getExecutionDriverRoutes(), driverUserId);
        if (route == null) {
            route = findRoute(plan.getLoadingDriverRoutes(), driverUserId);
        }
        if (route == null) {
            route = findRoute(plan.getDriverRoutes(), driverUserId);
        }
        return route;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castList(Object value) {
        if (value instanceof List) {
            return (List<Map<String, Object>>) value;
        }
        return null;
    }

    private static void overlayLoadingDepart(Map<String, Object> vm,
                                             DriverRoutePlan routePlan,
                                             List<Map<String, Object>> stopList) {
        boolean hasStops = stopList != null && !stopList.isEmpty();
        int readyCount = 0;
        int assignedCount = 0;
        if (stopList != null) {
            for (Map<String, Object> stop : stopList) {
                if (stop == null) {
                    continue;
                }
                if (DisShipmentTaskStatus.READY_TO_GO.equals(stop.get("status"))) {
                    readyCount++;
                }
                if (DisShipmentTaskStatus.ASSIGNED.equals(stop.get("status"))) {
                    assignedCount++;
                }
            }
        }
        boolean canDepart = hasStops && assignedCount == 0 && readyCount > 0;
        vm.put("showDepartAction", hasStops);
        vm.put("departActionEnabled", canDepart);
        vm.put("departActionLabel", "现在出发");
        if (!canDepart && hasStops) {
            vm.put("departBlockedReason", assignedCount > 0 ? "请先完成装车确认" : "暂不可出发");
        }
    }

    private static List<Map<String, Object>> buildStopList(DriverRoutePlan routePlan) {
        List<Map<String, Object>> stopList = new ArrayList<Map<String, Object>>();
        if (routePlan == null || routePlan.getStops() == null) {
            return stopList;
        }
        for (CustomerStopPlan stop : routePlan.getStops()) {
            if (stop == null) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("deliveryStopId", stop.getDeliveryStopId());
            map.put("taskId", stop.getDeliveryStopId());
            map.put("customerName", stop.getCustomerName());
            String taskStatus = resolveTaskStatus(stop);
            map.put("status", taskStatus);
            map.put("stopSeq", stop.getSequence());
            map.put("canConfirmLoad", DisShipmentTaskStatus.ASSIGNED.equals(taskStatus));
            stopList.add(map);
        }
        return stopList;
    }

    private static String resolveTaskStatus(CustomerStopPlan stop) {
        if (stop == null || stop.getSourceStop() == null || stop.getSourceStop().getShipmentTask() == null) {
            return null;
        }
        return stop.getSourceStop().getShipmentTask().getNxDstStatus();
    }

    private static DriverRoutePlan firstRoute(TodayDispatchResult result) {
        if (result == null || result.getSuggestedRoutes() == null || result.getSuggestedRoutes().isEmpty()) {
            return null;
        }
        return result.getSuggestedRoutes().get(0);
    }

    private static Integer resolveDriverRouteId(NxDisRoutePlanEntity plan, Integer driverUserId) {
        if (plan == null || driverUserId == null) {
            return null;
        }
        NxDisDriverRouteEntity route = findRoute(plan.getLoadingDriverRoutes(), driverUserId);
        if (route == null) {
            route = findRoute(plan.getExecutionDriverRoutes(), driverUserId);
        }
        if (route == null) {
            route = findRoute(plan.getDriverRoutes(), driverUserId);
        }
        return route != null ? route.getNxDdrId() : null;
    }

    private static NxDisDriverRouteEntity findRoute(List<NxDisDriverRouteEntity> routes, Integer driverUserId) {
        if (routes == null || driverUserId == null) {
            return null;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route != null && driverUserId.equals(route.getNxDdrDriverUserId())) {
                return route;
            }
        }
        return null;
    }

    private static Map<String, Object> defaultMapOverview(TodayDispatchResult result) {
        Map<String, Object> overview = new LinkedHashMap<String, Object>();
        if (result != null && result.getDepotLat() != null && result.getDepotLng() != null) {
            Map<String, Object> depot = new LinkedHashMap<String, Object>();
            depot.put("lat", result.getDepotLat());
            depot.put("lng", result.getDepotLng());
            depot.put("name", result.getDepotName());
            overview.put("depot", depot);
        }
        overview.put("markers", Collections.emptyList());
        overview.put("polylines", Collections.emptyList());
        return overview;
    }

    public static Map<String, Object> wrapResponse(Integer disId,
                                                   String routeDate,
                                                   String batchCode,
                                                   Integer driverUserId,
                                                   String driverName,
                                                   Map<String, Object> pageViewModel) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("routeDate", routeDate);
        data.put("routeDateLabel", DisRouteTemporalHelper.formatRouteDateLabel(routeDate, new Date()));
        data.put("dispatchBatch", batchCode);
        data.put("dispatchBatchLabel", DisRouteDispatchLabels.label(batchCode));
        data.put("driverUserId", driverUserId);
        data.put("driverName", driverName);
        data.put("disId", disId);
        data.put("pageViewModel", pageViewModel);
        return data;
    }
}
