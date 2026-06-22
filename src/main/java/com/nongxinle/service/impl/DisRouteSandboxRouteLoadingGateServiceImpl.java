package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisRouteStopDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dto.route.DriverRouteLoadingGateRequest;
import com.nongxinle.dto.route.RouteDispatchOperationDecision;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteDispatchBatch;
import com.nongxinle.route.DisRouteDispatchOperatorResolver;
import com.nongxinle.route.DisRouteDriverRouteStatus;
import com.nongxinle.route.DisRouteLoadingGateHelper;
import com.nongxinle.route.DisShipmentTaskStatus;
import com.nongxinle.service.DisDriverDutyService;
import com.nongxinle.service.DisRouteFeasibilityService;
import com.nongxinle.service.DisRouteSandboxRouteLoadingGateService;
import com.nongxinle.service.DisRouteSandboxTodayService;
import com.nongxinle.service.DisRouteScheduleService;
import com.nongxinle.service.DisShipmentTaskService;
import com.nongxinle.service.impl.DisRoutePlanPresentationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.formatWhatDay;

@Service("disRouteSandboxRouteLoadingGateService")
public class DisRouteSandboxRouteLoadingGateServiceImpl implements DisRouteSandboxRouteLoadingGateService {

    @Autowired
    private DisRouteSandboxTodayService disRouteSandboxTodayService;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisRouteStopDao nxDisRouteStopDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private DisRoutePlanPresentationHelper disRoutePlanPresentationHelper;
    @Autowired
    private DisRouteScheduleService disRouteScheduleService;
    @Autowired
    private DisRouteFeasibilityService disRouteFeasibilityService;
    @Autowired
    private DisShipmentTaskService disShipmentTaskService;
    @Autowired
    private DisRouteDispatchOperatorResolver disRouteDispatchOperatorResolver;
    @Autowired
    private DisDriverDutyService disDriverDutyService;

    @Override
    @Transactional
    public Map<String, Object> enterLoading(Integer driverRouteId, DriverRouteLoadingGateRequest request)
            throws Exception {
        normalizeRequest(request);
        validateRequest(driverRouteId, request);

        NxDisDriverRouteEntity route = requireDriverRoute(driverRouteId);
        NxDisRoutePlanEntity plan = requirePlan(route.getNxDdrPlanId());
        validatePlanScope(plan, request);

        attachRouteStops(route);
        disDriverDutyService.requireDriverOnDuty(
                request.getDisId(),
                route.getNxDdrDriverUserId(),
                resolveRouteDate(request.getRouteDate(), plan),
                route.getDriverName());
        boolean pendingForDriver = hasPendingDispatchStopsForDriver(plan.getNxDrpId(), route.getNxDdrDriverUserId());
        RouteDispatchOperationDecision decision =
                DisRouteLoadingGateHelper.evaluateEnterLoading(route, pendingForDriver);
        if (!decision.isAllowed()) {
            throw new IllegalStateException(decision.getBlockedReason());
        }

        Date now = new Date();
        route.setNxDdrLoadingEnteredAt(now);
        route.setNxDdrLoadingEnteredOperatorUserId(request.getOperatorUserId());
        route.setNxDdrRouteStatus(DisRouteDriverRouteStatus.LOADING);
        nxDisDriverRouteDao.updateLoadingGate(route);

        refreshPlan(plan.getNxDrpId());

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("driverRouteId", driverRouteId);
        result.putAll(disRouteSandboxTodayService.buildLoadingSandboxToday(
                request.getDisId(),
                resolveRouteDate(request.getRouteDate(), plan),
                normalizeBatch(request.getBatchCode()),
                request.getOperatorUserId()));
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> returnToDispatch(Integer driverRouteId, DriverRouteLoadingGateRequest request)
            throws Exception {
        normalizeRequest(request);
        validateRequest(driverRouteId, request);

        NxDisDriverRouteEntity route = requireDriverRoute(driverRouteId);
        NxDisRoutePlanEntity plan = requirePlan(route.getNxDdrPlanId());
        validatePlanScope(plan, request);

        attachRouteStops(route);
        RouteDispatchOperationDecision decision = DisRouteLoadingGateHelper.evaluateReturnToDispatch(route);
        if (!decision.isAllowed()) {
            throw new IllegalStateException(decision.getBlockedReason());
        }

        route.setNxDdrLoadingEnteredAt(null);
        route.setNxDdrLoadingEnteredOperatorUserId(null);
        route.setNxDdrRouteStatus(DisRouteDriverRouteStatus.DISPATCH_CONFIRMED);
        nxDisDriverRouteDao.updateLoadingGate(route);

        refreshPlan(plan.getNxDrpId());

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("driverRouteId", driverRouteId);
        result.putAll(disRouteSandboxTodayService.buildDispatchSandboxToday(
                request.getDisId(),
                resolveRouteDate(request.getRouteDate(), plan),
                normalizeBatch(request.getBatchCode()),
                request.getOperatorUserId()));
        return result;
    }

    private void normalizeRequest(DriverRouteLoadingGateRequest request) {
        if (request == null) {
            return;
        }
        request.setOperatorUserId(disRouteDispatchOperatorResolver.resolve(
                request.getDisId(), request.getOperatorUserId()));
    }

    private void validateRequest(Integer driverRouteId, DriverRouteLoadingGateRequest request) {
        if (driverRouteId == null) {
            throw new IllegalArgumentException("driverRouteId 不能为空");
        }
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
    }

    private NxDisDriverRouteEntity requireDriverRoute(Integer driverRouteId) {
        NxDisDriverRouteEntity route = nxDisDriverRouteDao.queryObject(driverRouteId);
        if (route == null) {
            throw new IllegalArgumentException("driverRouteId=" + driverRouteId + " 不存在");
        }
        return route;
    }

    private NxDisRoutePlanEntity requirePlan(Integer planId) {
        NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryObject(planId);
        if (plan == null) {
            throw new IllegalArgumentException("planId=" + planId + " 不存在");
        }
        return plan;
    }

    private void validatePlanScope(NxDisRoutePlanEntity plan, DriverRouteLoadingGateRequest request) {
        if (plan.getNxDrpDistributerId() != null && !plan.getNxDrpDistributerId().equals(request.getDisId())) {
            throw new IllegalArgumentException("disId 与路线计划不匹配");
        }
        String routeDate = resolveRouteDate(request.getRouteDate(), plan);
        if (plan.getNxDrpRouteDate() != null && routeDate != null
                && !routeDate.equals(plan.getNxDrpRouteDate())) {
            throw new IllegalArgumentException("routeDate 与路线计划不匹配");
        }
        String batchCode = normalizeBatch(request.getBatchCode());
        if (plan.getNxDrpDispatchBatch() != null && batchCode != null
                && !batchCode.equalsIgnoreCase(plan.getNxDrpDispatchBatch())) {
            throw new IllegalArgumentException("batchCode 与路线计划不匹配");
        }
        if (request.getDriverUserId() != null && plan.getNxDrpId() != null) {
            NxDisDriverRouteEntity route = nxDisDriverRouteDao.queryByPlanAndDriver(
                    plan.getNxDrpId(), request.getDriverUserId());
            if (route == null) {
                throw new IllegalArgumentException("driverUserId 与路线计划不匹配");
            }
        }
    }

    private void attachRouteStops(NxDisDriverRouteEntity route) {
        List<NxDisRouteStopEntity> stops = nxDisRouteStopDao.queryByDriverRouteId(route.getNxDdrId());
        List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByDriverRouteId(route.getNxDdrId());
        Map<Integer, NxDisShipmentTaskEntity> taskById = new java.util.HashMap<Integer, NxDisShipmentTaskEntity>();
        if (tasks != null) {
            for (NxDisShipmentTaskEntity task : tasks) {
                if (task != null && task.getNxDstId() != null) {
                    taskById.put(task.getNxDstId(), task);
                }
            }
        }
        if (stops != null) {
            for (NxDisRouteStopEntity stop : stops) {
                if (stop == null) {
                    continue;
                }
                Integer taskId = stop.getNxDrsShipmentTaskId();
                if (taskId != null) {
                    stop.setShipmentTask(taskById.get(taskId));
                }
            }
        }
        route.setStops(stops);
    }

    private boolean hasPendingDispatchStopsForDriver(Integer planId, Integer driverUserId) {
        if (planId == null || driverUserId == null) {
            return false;
        }
        List<NxDisShipmentTaskEntity> planTasks = nxDisShipmentTaskDao.queryByPlanId(planId);
        if (planTasks == null) {
            return false;
        }
        for (NxDisShipmentTaskEntity task : planTasks) {
            if (task == null) {
                continue;
            }
            Integer targetDriver = task.getNxDstAssignedDriverUserId() != null
                    ? task.getNxDstAssignedDriverUserId()
                    : task.getNxDstSuggestedDriverUserId();
            if (!driverUserId.equals(targetDriver)) {
                continue;
            }
            String status = task.getNxDstStatus();
            if (DisShipmentTaskStatus.SIMULATED.equals(status)
                    || DisShipmentTaskStatus.UNASSIGNED.equals(status)) {
                return true;
            }
            if (Boolean.TRUE.equals(task.getConfirmViaSandbox())) {
                return true;
            }
        }
        return false;
    }

    private void refreshPlan(Integer planId) throws Exception {
        disRoutePlanPresentationHelper.refreshPlanPresentation(planId);
        disRouteScheduleService.computeSchedule(planId);
        disRouteFeasibilityService.assess(planId);
        disShipmentTaskService.reconcilePlanStatus(planId);
    }

    private static String resolveRouteDate(String requestRouteDate, NxDisRoutePlanEntity plan) {
        if (requestRouteDate != null && !requestRouteDate.trim().isEmpty()) {
            return requestRouteDate.trim();
        }
        if (plan != null && plan.getNxDrpRouteDate() != null && !plan.getNxDrpRouteDate().trim().isEmpty()) {
            return plan.getNxDrpRouteDate().trim();
        }
        return formatWhatDay(0);
    }

    private static String normalizeBatch(String batchCode) {
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return DisRouteDispatchBatch.MORNING;
        }
        return batchCode.trim().toUpperCase();
    }
}
