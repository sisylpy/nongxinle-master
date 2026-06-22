package com.nongxinle.service.impl;

import com.nongxinle.dao.*;
import com.nongxinle.dto.route.RouteDispatchOperationDecision;
import com.nongxinle.dto.route.SandboxStopReturnToSandboxRequest;
import com.nongxinle.entity.*;
import com.nongxinle.route.DisRouteDispatchBatch;
import com.nongxinle.route.DisRouteDispatchOperatorResolver;
import com.nongxinle.route.DisRouteReturnToSandboxPolicy;
import com.nongxinle.route.DisShipmentTaskStatus;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisShipmentTaskItemStatus.ACTIVE;
import static com.nongxinle.route.DisShipmentTaskItemStatus.CANCELLED;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

/**
 * Phase 3c：撤销派车确认关系，让店回到动态沙盘。不删订单、不撤销配送单。
 */
@Service("disRouteSandboxReturnService")
public class DisRouteSandboxReturnServiceImpl implements DisRouteSandboxReturnService {

    @Autowired
    private DisRouteSandboxTodayService disRouteSandboxTodayService;
    @Autowired
    private DisShipmentTaskService disShipmentTaskService;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private NxDisShipmentTaskItemDao nxDisShipmentTaskItemDao;
    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisRouteStopDao nxDisRouteStopDao;
    @Autowired
    private DisRoutePlanPresentationHelper disRoutePlanPresentationHelper;
    @Autowired
    private DisRouteScheduleService disRouteScheduleService;
    @Autowired
    private DisRouteFeasibilityService disRouteFeasibilityService;
    @Autowired
    private DisRouteDispatchOperatorResolver disRouteDispatchOperatorResolver;

    @Override
    @Transactional
    public Map<String, Object> returnToSandbox(Integer deliveryStopId,
                                               SandboxStopReturnToSandboxRequest request) throws Exception {
        normalizeReturnRequest(request);
        validateRequest(deliveryStopId, request);

        NxDisShipmentTaskEntity task = disShipmentTaskService.queryTaskDetail(deliveryStopId);
        if (task == null) {
            throw new IllegalArgumentException("deliveryStopId=" + deliveryStopId + " 不存在");
        }

        List<NxDisShipmentTaskEntity> siblings = task.getNxDstDriverRouteId() != null
                ? nxDisShipmentTaskDao.queryByDriverRouteId(task.getNxDstDriverRouteId())
                : null;
        NxDisDriverRouteEntity driverRoute = task.getNxDstDriverRouteId() != null
                ? nxDisDriverRouteDao.queryObject(task.getNxDstDriverRouteId()) : null;
        RouteDispatchOperationDecision decision =
                DisRouteReturnToSandboxPolicy.evaluate(task, siblings, driverRoute);
        if (!decision.isAllowed()) {
            throw new IllegalStateException(decision.getBlockedReason());
        }

        Integer planId = task.getNxDstPlanId();
        Integer driverRouteId = task.getNxDstDriverRouteId();
        Integer disId = request.getDisId() != null ? request.getDisId() : task.getNxDstDistributerId();
        String routeDate = resolveRouteDate(request, task);
        String batchCode = resolveBatchCode(request, planId);

        cancelDispatchExecution(task, request.getOperatorUserId(), request.getReason());
        nxDisRouteStopDao.deleteByShipmentTaskId(deliveryStopId);

        if (planId != null) {
            disRoutePlanPresentationHelper.refreshPlanPresentation(planId);
            disRouteScheduleService.computeSchedule(planId);
            disRouteFeasibilityService.assess(planId);
            disShipmentTaskService.reconcilePlanStatus(planId);
        }
        reconcileEmptyDriverRoute(driverRouteId);

        return disRouteSandboxTodayService.buildToday(disId, routeDate, batchCode, request.getOperatorUserId());
    }

    private void normalizeReturnRequest(SandboxStopReturnToSandboxRequest request) {
        if (request == null) {
            return;
        }
        request.setOperatorUserId(disRouteDispatchOperatorResolver.resolve(
                request.getDisId(), request.getOperatorUserId()));
    }

    private void validateRequest(Integer deliveryStopId, SandboxStopReturnToSandboxRequest request) {
        if (deliveryStopId == null) {
            throw new IllegalArgumentException("deliveryStopId 不能为空");
        }
        if (request == null || request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
    }

    private void cancelDispatchExecution(NxDisShipmentTaskEntity task,
                                         Integer operatorUserId,
                                         String reason) {
        List<NxDisShipmentTaskItemEntity> items = nxDisShipmentTaskItemDao.queryByTaskId(task.getNxDstId());
        if (items != null) {
            for (NxDisShipmentTaskItemEntity item : items) {
                if (item == null || !ACTIVE.equals(item.getNxDstiItemStatus())) {
                    continue;
                }
                NxDisShipmentTaskItemEntity itemUpdate = new NxDisShipmentTaskItemEntity();
                itemUpdate.setNxDstiId(item.getNxDstiId());
                itemUpdate.setNxDstiItemStatus(CANCELLED);
                nxDisShipmentTaskItemDao.update(itemUpdate);
            }
        }

        NxDisShipmentTaskEntity taskUpdate = new NxDisShipmentTaskEntity();
        taskUpdate.setNxDstId(task.getNxDstId());
        taskUpdate.setNxDstStatus(DisShipmentTaskStatus.CANCELLED);
        taskUpdate.setClearOpenKey(true);
        taskUpdate.setNxDstManualLocked(0);
        taskUpdate.setNxDstAssignedDriverUserId(null);
        taskUpdate.setNxDstDriverRouteId(null);
        taskUpdate.setNxDstRouteSeq(null);
        taskUpdate.setNxDstManualStopSeq(null);
        taskUpdate.setNxDstAssignConfirmedAt(null);
        taskUpdate.setNxDstOperatorUserId(operatorUserId);
        taskUpdate.setNxDstAdjustReason(reason);
        nxDisShipmentTaskDao.update(taskUpdate);
    }

    private void reconcileEmptyDriverRoute(Integer driverRouteId) {
        if (driverRouteId == null) {
            return;
        }
        List<NxDisShipmentTaskEntity> routeTasks = nxDisShipmentTaskDao.queryByDriverRouteId(driverRouteId);
        int activeCount = 0;
        if (routeTasks != null) {
            for (NxDisShipmentTaskEntity routeTask : routeTasks) {
                if (routeTask == null || DisShipmentTaskStatus.CANCELLED.equals(routeTask.getNxDstStatus())) {
                    continue;
                }
                activeCount++;
            }
        }
        if (activeCount > 0) {
            return;
        }
        NxDisDriverRouteEntity routeUpdate = new NxDisDriverRouteEntity();
        routeUpdate.setNxDdrId(driverRouteId);
        routeUpdate.setNxDdrStopCount(0);
        routeUpdate.setNxDdrTotalDistanceM(0L);
        routeUpdate.setNxDdrTotalDurationS(0L);
        nxDisDriverRouteDao.update(routeUpdate);
    }

    private String resolveRouteDate(SandboxStopReturnToSandboxRequest request, NxDisShipmentTaskEntity task) {
        if (request.getRouteDate() != null && !request.getRouteDate().trim().isEmpty()) {
            return request.getRouteDate().trim();
        }
        if (task.getNxDstRouteDate() != null && !task.getNxDstRouteDate().trim().isEmpty()) {
            return task.getNxDstRouteDate().trim();
        }
        return formatWhatDay(0);
    }

    private String resolveBatchCode(SandboxStopReturnToSandboxRequest request, Integer planId) {
        if (request.getBatchCode() != null && !request.getBatchCode().trim().isEmpty()) {
            return request.getBatchCode().trim().toUpperCase();
        }
        if (planId != null) {
            NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryObject(planId);
            if (plan != null && plan.getNxDrpDispatchBatch() != null) {
                return plan.getNxDrpDispatchBatch();
            }
        }
        return DisRouteDispatchBatch.MORNING;
    }
}
