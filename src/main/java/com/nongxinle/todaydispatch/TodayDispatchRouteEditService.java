package com.nongxinle.todaydispatch;

import com.nongxinle.dto.route.DisRouteCustomerDriverConstraintDto;
import com.nongxinle.dto.route.SandboxComputeResult;
import com.nongxinle.dto.route.SandboxDriverRouteEditConfirmRequest;
import com.nongxinle.dto.route.SandboxDriverRouteEditPageRequest;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.route.DriverRouteEditPrimaryActionMaps;
import com.nongxinle.service.DisRouteCustomerDriverConstraintService;
import com.nongxinle.service.DisRouteCustomerDriverConstraintService.ConstraintCheckResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.utils.DateUtils.formatWhatDay;

/** todaydispatch 轻量路线编辑：page / preview / confirm（不依赖旧 route-edit 链）。 */
@Service
public class TodayDispatchRouteEditService {

    private static final String SOURCE_LOADING = "LOADING";

    @Autowired
    private TodayDispatchComputeService todayDispatchComputeService;

    @Autowired
    private DispatchPageAssembler dispatchPageAssembler;

    @Autowired
    private TodayDispatchRouteConfirmService todayDispatchRouteConfirmService;

    @Autowired
    private DisRouteCustomerDriverConstraintService constraintService;

    public Map<String, Object> buildPage(SandboxDriverRouteEditPageRequest request) throws Exception {
        validateRequest(request);
        TodayDispatchResult dispatchResult = loadDispatchResult(request);
        List<String> effectiveStopKeys = resolveEffectiveStopKeys(request, dispatchResult);
        DriverRoutePlan route = todayDispatchComputeService.buildEditedDriverRoute(
                dispatchResult, request.getDriverUserId(), effectiveStopKeys);
        List<String> stopKeys = mergeStopKeysPreservingOrder(effectiveStopKeys, extractStopKeysFromRoute(route));
        return wrapResponse(dispatchResult, route, request, stopKeys);
    }

    public Map<String, Object> preview(SandboxDriverRouteEditPageRequest request) throws Exception {
        validateRequest(request);
        TodayDispatchResult dispatchResult = loadDispatchResult(request);
        List<String> effectiveStopKeys = resolveEffectiveStopKeys(request, dispatchResult);
        if (effectiveStopKeys.isEmpty()) {
            throw new IllegalArgumentException("stopKeys 不能为空");
        }
        DriverRoutePlan route = todayDispatchComputeService.buildEditedDriverRoute(
                dispatchResult, request.getDriverUserId(), effectiveStopKeys);
        return wrapResponse(dispatchResult, route, request, effectiveStopKeys);
    }

    public Map<String, Object> confirm(SandboxDriverRouteEditConfirmRequest request) throws Exception {
        if (request == null || request.getDisId() == null || request.getDriverUserId() == null) {
            throw new IllegalArgumentException("disId / driverUserId 不能为空");
        }
        return todayDispatchRouteConfirmService.confirmDriverRouteEdit(request);
    }

    private Map<String, Object> wrapResponse(TodayDispatchResult dispatchResult,
                                             DriverRoutePlan route,
                                             SandboxDriverRouteEditPageRequest request,
                                             List<String> stopKeys) {
        boolean manualMode = Boolean.TRUE.equals(request.getManualDispatch());
        List<CustomerStopPlan> addableStops = manualMode
                ? Collections.<CustomerStopPlan>emptyList()
                : listAddableStops(dispatchResult, route);
        Map<Integer, DisRouteCustomerDriverConstraintDto> constraints =
                resolveConstraints(dispatchResult, request.getDriverUserId(), route, addableStops);
        boolean blocking = isBlocking(route, request.getDriverUserId(), constraints);
        Map<String, Object> pageViewModel = assemblePageViewModel(
                dispatchResult, route, addableStops, constraints, blocking, request, stopKeys);
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("pageViewModel", pageViewModel);
        return data;
    }

    private Map<String, Object> assemblePageViewModel(TodayDispatchResult dispatchResult,
                                                      DriverRoutePlan route,
                                                      List<CustomerStopPlan> addableStops,
                                                      Map<Integer, DisRouteCustomerDriverConstraintDto> constraints,
                                                      boolean blocking,
                                                      SandboxDriverRouteEditPageRequest request,
                                                      List<String> stopKeys) {
        Map<String, Object> vm = new LinkedHashMap<String, Object>();
        boolean loadingPage = SOURCE_LOADING.equalsIgnoreCase(normalizeSourcePage(request.getSourcePage()));
        boolean manualMode = Boolean.TRUE.equals(request.getManualDispatch());

        todayDispatchComputeService.enrichSingleDriverRoute(route, dispatchResult);
        vm.put("driver", dispatchPageAssembler.buildRouteEditDriverSummary(route));
        List<Map<String, Object>> routeStops = buildRouteStopDtos(route, loadingPage, stopKeys);
        vm.put("routeStops", routeStops);
        vm.put("stopKeys", stopKeys != null ? new ArrayList<String>(stopKeys) : Collections.<String>emptyList());

        List<Map<String, Object>> availableCustomers = buildAvailableCustomerDtos(
                addableStops, request.getDriverUserId(), constraints);
        vm.put("availableCustomers", availableCustomers);
        vm.put("addableStops", availableCustomers);

        vm.put("mapOverview", dispatchPageAssembler.buildRouteEditMapOverview(
                dispatchResult, route, addableStops));
        vm.put("timeline", dispatchPageAssembler.buildRouteEditTimeline(route, dispatchResult));

        vm.put("actions", buildActions(route, blocking, loadingPage));
        vm.put("pageTitle", manualMode ? "调整送货顺序" : "编辑司机路线");
        vm.put("manualDispatchMode", manualMode);
        vm.put("removeStopMode", loadingPage ? "REMOTE" : "LOCAL");
        putIfNotNull(vm, "routeDate", dispatchResult.getRouteDate());
        putIfNotNull(vm, "batchCode", dispatchResult.getBatchCode());
        return vm;
    }

    private static List<Map<String, Object>> buildRouteStopDtos(DriverRoutePlan route,
                                                                boolean loadingPage,
                                                                List<String> stopKeys) {
        Map<String, CustomerStopPlan> planByKey = new LinkedHashMap<String, CustomerStopPlan>();
        if (route != null && route.getStops() != null) {
            for (CustomerStopPlan stop : route.getStops()) {
                if (stop == null) {
                    continue;
                }
                String key = TodayDispatchComputeService.resolvePlanStopKey(stop);
                if (key != null) {
                    planByKey.put(key, stop);
                }
            }
        }
        List<Map<String, Object>> dtos = new ArrayList<Map<String, Object>>();
        if (stopKeys != null && !stopKeys.isEmpty()) {
            for (String stopKey : stopKeys) {
                CustomerStopPlan stop = planByKey.get(stopKey);
                if (stop != null) {
                    dtos.add(toRouteStopDto(stop, loadingPage));
                }
            }
            return dtos;
        }
        for (CustomerStopPlan stop : planByKey.values()) {
            dtos.add(toRouteStopDto(stop, loadingPage));
        }
        return dtos;
    }

    private static Map<String, Object> toRouteStopDto(CustomerStopPlan stop, boolean loadingPage) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        String stopKey = TodayDispatchComputeService.resolvePlanStopKey(stop);
        dto.put("stopKey", stopKey);
        dto.put("departmentId", stop.getDepFatherId());
        dto.put("customerName", stop.getCustomerName());
        putIfNotNull(dto, "goodsSummary", stop.getGoodsSummary());
        putIfNotNull(dto, "plannedArrivalLabel", stop.getPlannedArrivalLabel());
        putIfNotNull(dto, "plannedDepartureLabel", stop.getPlannedDepartureLabel());
        putIfNotNull(dto, "windowLabel", stop.getWindowLabel());
        dto.put("locked", Boolean.FALSE);
        dto.put("lockReason", "");
        if (loadingPage && stop.getDeliveryStopId() != null) {
            dto.put("deliveryStopId", stop.getDeliveryStopId());
            dto.put("removeConfirmTitle", "移除门店");
            String customerName = stop.getCustomerName() != null && !stop.getCustomerName().trim().isEmpty()
                    ? stop.getCustomerName().trim() : "该门店";
            dto.put("removeConfirmMessage", customerName + " 将从装车路线移除，是否确认？");
        }
        if ("warn".equals(stop.getArrivalStatusTone())) {
            putIfNotNull(dto, "constraintHint", stop.getArrivalStatusLabel());
        }
        return dto;
    }

    private List<Map<String, Object>> buildAvailableCustomerDtos(
            List<CustomerStopPlan> addableStops,
            Integer driverUserId,
            Map<Integer, DisRouteCustomerDriverConstraintDto> constraints) {
        List<Map<String, Object>> dtos = new ArrayList<Map<String, Object>>();
        if (addableStops == null) {
            return dtos;
        }
        for (CustomerStopPlan stop : addableStops) {
            if (stop == null) {
                continue;
            }
            Map<String, Object> dto = new LinkedHashMap<String, Object>();
            Integer depId = stop.getDepFatherId();
            dto.put("stopKey", TodayDispatchComputeService.resolvePlanStopKey(stop));
            dto.put("departmentId", depId);
            dto.put("customerName", stop.getCustomerName());
            putIfNotNull(dto, "goodsSummary", stop.getGoodsSummary());
            if (constraintService != null && constraints != null && depId != null) {
                DisRouteCustomerDriverConstraintDto constraint = constraints.get(depId);
                String label = constraintService.buildConstraintLabel(driverUserId, constraint);
                putIfNotNull(dto, "constraintLabel", label);
                putIfNotNull(dto, "constraintHint", label);
            }
            dtos.add(dto);
        }
        return dtos;
    }

    private boolean isBlocking(DriverRoutePlan route,
                               Integer driverUserId,
                               Map<Integer, DisRouteCustomerDriverConstraintDto> constraints) {
        if (route == null || route.getStops() == null || constraintService == null || constraints == null) {
            return false;
        }
        for (CustomerStopPlan stop : route.getStops()) {
            if (stop == null || stop.getDepFatherId() == null) {
                continue;
            }
            DisRouteCustomerDriverConstraintDto constraint = constraints.get(stop.getDepFatherId());
            ConstraintCheckResult result = constraintService.check(driverUserId, constraint);
            if (result.isBlocked()) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> buildActions(DriverRoutePlan route,
                                             boolean blocking,
                                             boolean loadingPage) {
        Map<String, Object> actions = new LinkedHashMap<String, Object>();
        actions.put("previewPath", DriverRouteEditPrimaryActionMaps.PREVIEW_PATH);
        actions.put("confirmPath", DriverRouteEditPrimaryActionMaps.CONFIRM_PATH);
        actions.put("editPagePath", DriverRouteEditPrimaryActionMaps.EDIT_PAGE_PATH);

        boolean hasBlocking = blocking;
        int stopCount = route != null && route.getStops() != null ? route.getStops().size() : 0;
        boolean confirmEnabled = !hasBlocking && stopCount > 0;
        actions.put("confirmEnabled", confirmEnabled);
        if (!confirmEnabled) {
            if (stopCount <= 0) {
                actions.put("confirmDisabledReason", loadingPage ? "当前路线暂无客户" : "请至少添加一个客户");
            } else if (hasBlocking) {
                actions.put("confirmDisabledReason", "存在不可确认的风险项");
            }
        }
        return actions;
    }

    private List<CustomerStopPlan> listAddableStops(TodayDispatchResult dispatchResult,
                                                    DriverRoutePlan currentRoute) {
        Set<Integer> onRoute = new HashSet<Integer>();
        if (currentRoute != null && currentRoute.getStops() != null) {
            for (CustomerStopPlan stop : currentRoute.getStops()) {
                if (stop != null && stop.getDepFatherId() != null) {
                    onRoute.add(stop.getDepFatherId());
                }
            }
        }
        List<CustomerStopPlan> addable = new ArrayList<CustomerStopPlan>();
        for (CustomerStopPlan stop : TodayDispatchRouteEditStopPool.collectAllSandboxCustomerPlans(dispatchResult)) {
            if (stop == null || stop.getDepFatherId() == null) {
                continue;
            }
            if (!onRoute.contains(stop.getDepFatherId())) {
                addable.add(stop);
            }
        }
        return addable;
    }

    private Map<Integer, DisRouteCustomerDriverConstraintDto> resolveConstraints(
            TodayDispatchResult dispatchResult,
            Integer driverUserId,
            DriverRoutePlan route,
            List<CustomerStopPlan> addableStops) {
        Set<Integer> depIds = new LinkedHashSet<Integer>();
        collectDepIds(depIds, route != null ? route.getStops() : null);
        collectDepIds(depIds, addableStops);
        List<Integer> eligibleDrivers = resolveEligibleDriverIds(dispatchResult);
        if (constraintService == null || dispatchResult == null || dispatchResult.getDisId() == null) {
            return Collections.emptyMap();
        }
        return constraintService.resolveConstraints(
                dispatchResult.getDisId(), new ArrayList<Integer>(depIds), eligibleDrivers);
    }

    private static void collectDepIds(Set<Integer> depIds, List<CustomerStopPlan> stops) {
        if (stops == null) {
            return;
        }
        for (CustomerStopPlan stop : stops) {
            if (stop != null && stop.getDepFatherId() != null) {
                depIds.add(stop.getDepFatherId());
            }
        }
    }

    private static List<Integer> resolveEligibleDriverIds(TodayDispatchResult dispatchResult) {
        List<Integer> driverIds = new ArrayList<Integer>();
        SandboxComputeResult compute = dispatchResult != null ? dispatchResult.getCompute() : null;
        if (compute != null && compute.getOnDutyDrivers() != null) {
            for (NxDistributerUserEntity driver : compute.getOnDutyDrivers()) {
                if (driver != null && driver.getNxDistributerUserId() != null) {
                    driverIds.add(driver.getNxDistributerUserId());
                }
            }
        }
        if (dispatchResult != null && dispatchResult.getAvailableDrivers() != null) {
            for (Map<String, Object> driver : dispatchResult.getAvailableDrivers()) {
                if (driver == null) {
                    continue;
                }
                Object id = driver.get("driverUserId");
                if (id instanceof Number) {
                    driverIds.add(((Number) id).intValue());
                }
            }
        }
        return driverIds;
    }

    private TodayDispatchResult loadDispatchResult(SandboxDriverRouteEditPageRequest request) throws Exception {
        String routeDate = resolveRouteDate(request.getRouteDate());
        String batchCode = normalizeBatch(request.getBatchCode());
        if (SOURCE_LOADING.equalsIgnoreCase(normalizeSourcePage(request.getSourcePage()))) {
            return todayDispatchComputeService.computeLoading(
                    request.getDisId(), routeDate, batchCode, request.getOperatorUserId());
        }
        return todayDispatchComputeService.compute(
                request.getDisId(), routeDate, batchCode, request.getOperatorUserId());
    }

    private static List<String> extractStopKeysFromRoute(DriverRoutePlan route) {
        List<String> keys = new ArrayList<String>();
        if (route == null || route.getStops() == null) {
            return keys;
        }
        for (CustomerStopPlan stop : route.getStops()) {
            String key = TodayDispatchComputeService.resolvePlanStopKey(stop);
            if (key != null) {
                keys.add(key);
            }
        }
        return keys;
    }

    private List<String> resolveEffectiveStopKeys(SandboxDriverRouteEditPageRequest request,
                                                  TodayDispatchResult dispatchResult) {
        List<String> fromRequest = normalizeStopKeyList(request != null ? request.getStopKeys() : null);
        if (!fromRequest.isEmpty()) {
            return fromRequest;
        }
        return todayDispatchComputeService.collectDriverStopKeys(
                dispatchResult, request.getDriverUserId());
    }

    private static List<String> normalizeStopKeyList(List<String> stopKeys) {
        List<String> normalized = new ArrayList<String>();
        if (stopKeys == null) {
            return normalized;
        }
        for (String key : stopKeys) {
            if (key != null && !key.trim().isEmpty()) {
                normalized.add(key.trim());
            }
        }
        return normalized;
    }

    private static List<String> mergeStopKeysPreservingOrder(List<String> preferred,
                                                             List<String> fromRoute) {
        LinkedHashSet<String> merged = new LinkedHashSet<String>();
        if (preferred != null) {
            for (String key : preferred) {
                if (key != null && !key.trim().isEmpty()) {
                    merged.add(key.trim());
                }
            }
        }
        if (fromRoute != null) {
            for (String key : fromRoute) {
                if (key != null && !key.trim().isEmpty()) {
                    merged.add(key.trim());
                }
            }
        }
        return new ArrayList<String>(merged);
    }

    private static void validateRequest(SandboxDriverRouteEditPageRequest request) {
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        if (request.getDriverUserId() == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }
    }

    private static String resolveRouteDate(String routeDate) {
        if (routeDate != null && !routeDate.trim().isEmpty()) {
            return routeDate.trim();
        }
        return formatWhatDay(0);
    }

    private static String normalizeBatch(String batchCode) {
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return com.nongxinle.route.DisRouteDispatchBatch.MORNING;
        }
        return batchCode.trim().toUpperCase();
    }

    private static String normalizeSourcePage(String sourcePage) {
        return sourcePage != null ? sourcePage.trim() : "";
    }

    private static void putIfNotNull(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }
}
