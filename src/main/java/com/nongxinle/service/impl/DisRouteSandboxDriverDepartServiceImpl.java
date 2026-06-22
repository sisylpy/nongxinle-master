package com.nongxinle.service.impl;

import com.nongxinle.config.DisRouteDispatchSettings;
import com.nongxinle.dao.*;
import com.nongxinle.dto.route.DriverRouteDepartRequest;
import com.nongxinle.dto.route.RouteDispatchOperationDecision;
import com.nongxinle.dto.route.RouteFeasibilityResult;
import com.nongxinle.entity.*;
import com.nongxinle.route.*;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.*;

import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;
import static com.nongxinle.route.DisRouteDispatchBatch.MORNING;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

/**
 * Phase 3D：老板确认司机整车出发（route 级，非门店级）。
 */
@Service("disRouteSandboxDriverDepartService")
public class DisRouteSandboxDriverDepartServiceImpl implements DisRouteSandboxDriverDepartService {

    @Autowired
    private DisRouteSandboxTodayService disRouteSandboxTodayService;
    @Autowired
    private DisRouteSandboxComputeService disRouteSandboxComputeService;
    @Autowired
    private DisRouteFeasibilityService disRouteFeasibilityService;
    @Autowired
    private DisShipmentTaskService disShipmentTaskService;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private NxDisShipmentTaskItemDao nxDisShipmentTaskItemDao;
    @Autowired
    private NxDisDriverDutyDao nxDisDriverDutyDao;
    @Autowired
    private DisRouteDispatchSettings disRouteDispatchSettings;

    @Override
    @Transactional
    public Map<String, Object> depart(Integer driverRouteId, DriverRouteDepartRequest request) throws Exception {
        validateRequest(driverRouteId, request);

        NxDisDriverRouteEntity route = nxDisDriverRouteDao.queryObject(driverRouteId);
        if (route == null) {
            throw new IllegalArgumentException("driverRouteId=" + driverRouteId + " 不存在");
        }

        NxDisRoutePlanEntity plan = route.getNxDdrPlanId() != null
                ? nxDisRoutePlanDao.queryObject(route.getNxDdrPlanId()) : null;
        if (plan == null) {
            throw new IllegalStateException("司机路线未关联有效计划");
        }
        if (request.getDisId() != null && !request.getDisId().equals(plan.getNxDrpDistributerId())) {
            throw new IllegalArgumentException("disId 与路线计划不匹配");
        }

        String routeDate = resolveRouteDate(request, plan);
        String batchCode = normalizeBatch(request, plan);
        Integer disId = plan.getNxDrpDistributerId();
        Integer driverUserId = request.getDriverUserId() != null
                ? request.getDriverUserId() : route.getNxDdrDriverUserId();
        if (driverUserId != null && route.getNxDdrDriverUserId() != null
                && !driverUserId.equals(route.getNxDdrDriverUserId())) {
            throw new IllegalArgumentException("driverUserId 与路线不匹配");
        }

        com.nongxinle.dto.route.SandboxComputeResult compute =
                attachRouteStopsFromSandbox(route, disId, routeDate, batchCode);
        freezeRouteSnapshotOnDepart(route, compute != null ? compute.getMergedPlan() : null);

        boolean driverOnDuty = isDriverOnDuty(disId, route.getNxDdrDriverUserId(), routeDate);
        List<NxDisShipmentTaskEntity> routeTasks = resolveRouteTasksForDepart(route, driverRouteId);
        RouteFeasibilityResult feasibility = disRouteFeasibilityService.assess(plan.getNxDrpId());

        RouteDispatchOperationDecision decision = DisRouteDriverDepartPolicy.evaluateDepart(
                route, plan, routeTasks, driverOnDuty, feasibility,
                disRouteDispatchSettings.isRequireLoadBeforeDepart(disId));
        if (!decision.isAllowed()) {
            throw new IllegalStateException(decision.getBlockedReason());
        }

        Date departAt = parseDepartAt(request.getDepartAt());
        markRouteDeparted(driverRouteId, request.getOperatorUserId(), request.getRemark(), departAt);
        markTasksInDelivery(routeTasks, request.getOperatorUserId());
        disShipmentTaskService.reconcilePlanStatus(plan.getNxDrpId());

        return disRouteSandboxTodayService.buildToday(disId, routeDate, batchCode);
    }

    @Override
    @Transactional
    public Map<String, Object> departByDriverUserId(DriverRouteDepartRequest request) throws Exception {
        if (request == null || request.getDriverUserId() == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }
        if (request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
        Integer driverRouteId = resolveDriverRouteId(request);
        depart(driverRouteId, request);
        String routeDate = request.getRouteDate() != null && !request.getRouteDate().trim().isEmpty()
                ? request.getRouteDate().trim() : formatWhatDay(0);
        String batchCode = request.getBatchCode() != null && !request.getBatchCode().trim().isEmpty()
                ? request.getBatchCode().trim().toUpperCase() : MORNING;
        return disRouteSandboxTodayService.buildLoadingToday(request.getDisId(), routeDate, batchCode);
    }

    private Integer resolveDriverRouteId(DriverRouteDepartRequest request) throws Exception {
        if (request.getPlanId() != null) {
            NxDisDriverRouteEntity route = nxDisDriverRouteDao.queryByPlanAndDriver(
                    request.getPlanId(), request.getDriverUserId());
            if (route != null && route.getNxDdrId() != null) {
                return route.getNxDdrId();
            }
        }
        String routeDate = request.getRouteDate() != null && !request.getRouteDate().trim().isEmpty()
                ? request.getRouteDate().trim() : formatWhatDay(0);
        String batchCode = request.getBatchCode() != null && !request.getBatchCode().trim().isEmpty()
                ? request.getBatchCode().trim().toUpperCase() : MORNING;
        Map<String, Object> loadingToday = disRouteSandboxTodayService.buildLoadingToday(
                request.getDisId(), routeDate, batchCode);
        Object routesObj = loadingToday.get("loadingDriverRoutes");
        if (routesObj instanceof List) {
            List<?> routes = (List<?>) routesObj;
            for (Object item : routes) {
                if (!(item instanceof Map)) {
                    continue;
                }
                Map<?, ?> routeMap = (Map<?, ?>) item;
                Object driverUserId = routeMap.get("driverUserId");
                Object routeId = routeMap.get("driverRouteId") != null
                        ? routeMap.get("driverRouteId")
                        : (routeMap.get("nxDdrId") != null ? routeMap.get("nxDdrId") : null);
                if (driverUserId != null && request.getDriverUserId().equals(Integer.valueOf(String.valueOf(driverUserId)))
                        && routeId != null) {
                    return Integer.valueOf(String.valueOf(routeId));
                }
            }
        }
        throw new IllegalStateException("未找到司机 " + request.getDriverUserId() + " 的装车路线");
    }

    private void validateRequest(Integer driverRouteId, DriverRouteDepartRequest request) {
        if (driverRouteId == null) {
            throw new IllegalArgumentException("driverRouteId 不能为空");
        }
        if (request == null || request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
    }

    private com.nongxinle.dto.route.SandboxComputeResult attachRouteStopsFromSandbox(NxDisDriverRouteEntity route,
                                             Integer disId,
                                             String routeDate,
                                             String batchCode) throws Exception {
        com.nongxinle.dto.route.SandboxComputeRequest computeRequest =
                new com.nongxinle.dto.route.SandboxComputeRequest();
        computeRequest.setDisId(disId);
        computeRequest.setRouteDate(routeDate);
        computeRequest.setBatchCode(batchCode);
        com.nongxinle.dto.route.SandboxComputeResult compute =
                disRouteSandboxComputeService.compute(computeRequest);
        NxDisRoutePlanEntity mergedPlan = compute.getMergedPlan();
        if (mergedPlan == null) {
            route.setStops(Collections.<NxDisRouteStopEntity>emptyList());
            return compute;
        }
        NxDisDriverRouteEntity matched = findMergedRoute(mergedPlan, route.getNxDdrId());
        if (matched != null) {
            route.setStops(matched.getStops());
        } else {
            route.setStops(Collections.<NxDisRouteStopEntity>emptyList());
        }
        return compute;
    }

    private NxDisDriverRouteEntity findMergedRoute(NxDisRoutePlanEntity mergedPlan, Integer driverRouteId) {
        if (mergedPlan == null || driverRouteId == null) {
            return null;
        }
        NxDisDriverRouteEntity found = findRouteInList(mergedPlan.getDriverRoutes(), driverRouteId);
        if (found != null) {
            return found;
        }
        found = findRouteInList(mergedPlan.getLoadingDriverRoutes(), driverRouteId);
        if (found != null) {
            return found;
        }
        return findRouteInList(mergedPlan.getExecutionDriverRoutes(), driverRouteId);
    }

    private NxDisDriverRouteEntity findRouteInList(List<NxDisDriverRouteEntity> routes, Integer driverRouteId) {
        if (routes == null) {
            return null;
        }
        for (NxDisDriverRouteEntity mergedRoute : routes) {
            if (mergedRoute != null && driverRouteId.equals(mergedRoute.getNxDdrId())) {
                return mergedRoute;
            }
        }
        return null;
    }

    /** 出发时冻结路线快照到 DB，后续 GET 优先读持久化里程/排程。 */
    private void freezeRouteSnapshotOnDepart(NxDisDriverRouteEntity route,
                                           NxDisRoutePlanEntity mergedPlan) {
        if (route == null || route.getNxDdrId() == null || mergedPlan == null) {
            return;
        }
        NxDisDriverRouteEntity mergedRoute = findMergedRoute(mergedPlan, route.getNxDdrId());
        if (mergedRoute == null) {
            return;
        }
        NxDisDriverRouteEntity totalsUpdate =
                DisRouteExecutionRouteSnapshotHelper.buildRouteSnapshotUpdate(mergedRoute);
        if (totalsUpdate != null) {
            nxDisDriverRouteDao.update(totalsUpdate);
        }
        NxDisDriverRouteEntity scheduleUpdate =
                DisRouteExecutionRouteSnapshotHelper.buildRouteScheduleSnapshotUpdate(mergedRoute);
        if (scheduleUpdate != null) {
            nxDisDriverRouteDao.updateSchedule(scheduleUpdate);
        }
        if (mergedRoute.getStops() == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : mergedRoute.getStops()) {
            NxDisShipmentTaskEntity taskUpdate =
                    DisRouteExecutionRouteSnapshotHelper.buildTaskLegSnapshotUpdate(stop);
            if (taskUpdate != null) {
                nxDisShipmentTaskDao.update(taskUpdate);
            }
        }
    }

    private List<NxDisShipmentTaskEntity> resolveRouteTasksForDepart(NxDisDriverRouteEntity route,
                                                                     Integer driverRouteId) {
        List<NxDisShipmentTaskEntity> tasks = loadRouteTasksWithItems(driverRouteId);
        if (tasks != null && !tasks.isEmpty()) {
            return tasks;
        }
        List<NxDisShipmentTaskEntity> fromStops = new ArrayList<NxDisShipmentTaskEntity>();
        if (route != null && route.getStops() != null) {
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop == null || stop.getShipmentTask() == null || stop.getShipmentTask().getNxDstId() == null) {
                    continue;
                }
                NxDisShipmentTaskEntity task = stop.getShipmentTask();
                if (task.getItems() == null || task.getItems().isEmpty()) {
                    task.setItems(nxDisShipmentTaskItemDao.queryByTaskId(task.getNxDstId()));
                }
                fromStops.add(task);
            }
        }
        return fromStops;
    }

    private List<NxDisShipmentTaskEntity> loadRouteTasksWithItems(Integer driverRouteId) {
        List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByDriverRouteId(driverRouteId);
        if (tasks == null) {
            return Collections.emptyList();
        }
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task == null || task.getNxDstId() == null) {
                continue;
            }
            task.setItems(nxDisShipmentTaskItemDao.queryByTaskId(task.getNxDstId()));
        }
        return tasks;
    }

    private void markRouteDeparted(Integer driverRouteId,
                                   Integer operatorUserId,
                                   String remark,
                                   Date departAt) {
        NxDisDriverRouteEntity update = new NxDisDriverRouteEntity();
        update.setNxDdrId(driverRouteId);
        update.setNxDdrRouteStatus(DisRouteDriverRouteStatus.IN_DELIVERY);
        update.setNxDdrActualDepartAt(departAt);
        update.setNxDdrDepartOperatorUserId(operatorUserId);
        update.setNxDdrDepartRemark(remark);
        update.setNxDdrDispatchEligible(0);
        nxDisDriverRouteDao.updateExecution(update);
    }

    private void markTasksInDelivery(List<NxDisShipmentTaskEntity> routeTasks, Integer operatorUserId) {
        if (routeTasks == null) {
            return;
        }
        for (NxDisShipmentTaskEntity task : routeTasks) {
            if (task == null || task.getNxDstId() == null) {
                continue;
            }
            if (DisShipmentTaskStatus.CANCELLED.equals(task.getNxDstStatus())) {
                continue;
            }
            NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
            update.setNxDstId(task.getNxDstId());
            update.setNxDstStatus(DisShipmentTaskStatus.IN_DELIVERY);
            update.setNxDstOperatorUserId(operatorUserId);
            update.setNxDstAdjustReason("老板确认司机出发");
            nxDisShipmentTaskDao.update(update);
            task.setNxDstStatus(DisShipmentTaskStatus.IN_DELIVERY);
        }
    }

    private boolean isDriverOnDuty(Integer disId, Integer driverUserId, String routeDate) {
        if (disId == null || driverUserId == null) {
            return false;
        }
        NxDisDriverDutyEntity duty = nxDisDriverDutyDao.queryByDisDriverDate(disId, driverUserId, routeDate);
        return duty != null && ON_DUTY.equals(duty.getNxDddDutyStatus());
    }

    private String resolveRouteDate(DriverRouteDepartRequest request, NxDisRoutePlanEntity plan) {
        if (request.getRouteDate() != null && !request.getRouteDate().trim().isEmpty()) {
            return request.getRouteDate().trim();
        }
        if (plan.getNxDrpRouteDate() != null && !plan.getNxDrpRouteDate().trim().isEmpty()) {
            return plan.getNxDrpRouteDate().trim();
        }
        return formatWhatDay(0);
    }

    private String normalizeBatch(DriverRouteDepartRequest request, NxDisRoutePlanEntity plan) {
        if (request.getBatchCode() != null && !request.getBatchCode().trim().isEmpty()) {
            return request.getBatchCode().trim().toUpperCase();
        }
        if (plan.getNxDrpDispatchBatch() != null && !plan.getNxDrpDispatchBatch().trim().isEmpty()) {
            return plan.getNxDrpDispatchBatch().trim().toUpperCase();
        }
        return MORNING;
    }

    private Date parseDepartAt(String departAt) throws ParseException {
        if (departAt == null || departAt.trim().isEmpty()) {
            return new Date();
        }
        return RouteDispatchDateFormat.parse(departAt.trim());
    }
}
