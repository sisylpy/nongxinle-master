package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisRouteStopDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dto.route.TaskTimeWindowRequest;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRoutePlanStatus;
import com.nongxinle.route.DisShipmentTaskStatus;
import com.nongxinle.service.DisRouteFeasibilityService;
import com.nongxinle.service.DisRouteScheduleService;
import com.nongxinle.service.DisRouteTaskTimeWindowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisRouteTaskTimeWindowServiceImpl implements DisRouteTaskTimeWindowService {

    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private NxDisRouteStopDao nxDisRouteStopDao;
    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private DisRouteScheduleService disRouteScheduleService;
    @Autowired
    private DisRouteFeasibilityService disRouteFeasibilityService;
    @Autowired
    private DisRouteDispatchSnapshotHelper disRouteDispatchSnapshotHelper;

    @Override
    @Transactional
    public NxDisShipmentTaskEntity updateTimeWindow(Integer taskId, TaskTimeWindowRequest request) {
        validateRequest(taskId, request);
        NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(taskId);
        if (task == null) {
            throw new IllegalArgumentException("配送任务不存在: " + taskId);
        }
        assertTaskMutable(task);
        assertPlanMutable(task.getNxDstPlanId());

        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(taskId);
        update.setNxDstEarliestDeliveryTimeS(request.getEarliestDeliveryTimeS());
        update.setNxDstLatestDeliveryTimeS(request.getLatestDeliveryTimeS());
        if (request.getServiceMinutes() != null) {
            update.setNxDstServiceMinutes(request.getServiceMinutes());
        }
        update.setNxDstTimeWindowOverrideFlag(1);
        update.setNxDstTimeWindowAdjustReason(request.getReason().trim());
        update.setNxDstOperatorUserId(request.getOperatorUserId());
        nxDisShipmentTaskDao.update(update);

        task = nxDisShipmentTaskDao.queryObject(taskId);
        syncStopTimeWindow(task);

        if (task.getNxDstPlanId() != null) {
            disRouteScheduleService.computeSchedule(task.getNxDstPlanId());
            disRouteFeasibilityService.assess(task.getNxDstPlanId());
        }
        return nxDisShipmentTaskDao.queryObject(taskId);
    }

    private void syncStopTimeWindow(NxDisShipmentTaskEntity task) {
        NxDisRouteStopEntity stop = nxDisRouteStopDao.queryByShipmentTaskId(task.getNxDstId());
        if (stop == null) {
            return;
        }
        NxDisRouteStopEntity stopUpdate = disRouteDispatchSnapshotHelper.buildStopTimeWindowUpdate(
                task, stop.getNxDrsId());
        nxDisRouteStopDao.updateDispatchSnapshot(stopUpdate);
    }

    private void validateRequest(Integer taskId, TaskTimeWindowRequest request) {
        if (taskId == null) {
            throw new IllegalArgumentException("taskId 不能为空");
        }
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (request.getLatestDeliveryTimeS() == null) {
            throw new IllegalArgumentException("latestDeliveryTimeS 不能为空");
        }
        if (request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("reason 不能为空");
        }
        if (request.getEarliestDeliveryTimeS() != null
                && request.getEarliestDeliveryTimeS() > request.getLatestDeliveryTimeS()) {
            throw new IllegalArgumentException("earliestDeliveryTimeS 不能晚于 latestDeliveryTimeS");
        }
    }

    private void assertTaskMutable(NxDisShipmentTaskEntity task) {
        String status = task.getNxDstStatus();
        if (DisShipmentTaskStatus.IN_DELIVERY.equals(status)
                || DisShipmentTaskStatus.DELIVERED.equals(status)
                || DisShipmentTaskStatus.CANCELLED.equals(status)
                || DisShipmentTaskStatus.CLOSED.equals(status)
                || DisShipmentTaskStatus.READY_TO_GO.equals(status)) {
            throw new IllegalStateException("当前任务状态不允许修改当日送达窗口：" + status);
        }
    }

    private void assertPlanMutable(Integer planId) {
        if (planId == null) {
            return;
        }
        NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryObject(planId);
        if (plan == null) {
            return;
        }
        if (DisRoutePlanStatus.CANCELLED.equals(plan.getNxDrpStatus())) {
            throw new IllegalStateException("当前路线计划已取消，不能修改送达窗口");
        }
    }
}
