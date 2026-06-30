package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRouteStopDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dao.NxDisShipmentTaskItemDao;
import com.nongxinle.dto.route.DriverDeliveryNowRequest;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;
import com.nongxinle.route.DisRouteDispatchBatch;
import com.nongxinle.route.DisRouteDriverRouteStatus;
import com.nongxinle.route.DisShipmentTaskStatus;
import com.nongxinle.service.DisRouteSandboxStopTimeWindowService;
import com.nongxinle.todaydispatch.TodayDispatchDriverTerminalService;
import com.nongxinle.service.DisRouteSandboxDriverDeliveryNowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisShipmentTaskItemStatus.ACTIVE;
import static com.nongxinle.route.DisShipmentTaskItemStatus.CANCELLED;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

@Service("disRouteSandboxDriverDeliveryNowService")
public class DisRouteSandboxDriverDeliveryNowServiceImpl implements DisRouteSandboxDriverDeliveryNowService {

    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private NxDisShipmentTaskItemDao nxDisShipmentTaskItemDao;
    @Autowired
    private NxDisRouteStopDao nxDisRouteStopDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private TodayDispatchDriverTerminalService todayDispatchDriverTerminalService;
    @Autowired
    private DisRouteSandboxStopTimeWindowService disRouteSandboxStopTimeWindowService;

    @Override
    @Transactional
    public Map<String, Object> completeNow(Integer deliveryStopId, DriverDeliveryNowRequest request) throws Exception {
        NxDisShipmentTaskEntity task = requireTask(deliveryStopId);
        Date now = new Date();

        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(task.getNxDstId());
        update.setNxDstStatus(DisShipmentTaskStatus.DELIVERED);
        update.setNxDstDeliveredAt(now);
        nxDisShipmentTaskDao.update(update);

        invalidateTodayTimeWindowOverride(task);
        releaseRouteIfNoPendingStops(task.getNxDstDriverRouteId());
        return buildDeliveryResponse(task, request);
    }

    @Override
    @Transactional
    public Map<String, Object> returnToSandboxNow(Integer deliveryStopId, DriverDeliveryNowRequest request)
            throws Exception {
        NxDisShipmentTaskEntity task = requireTask(deliveryStopId);
        Integer driverRouteId = task.getNxDstDriverRouteId();
        String reason = request != null && request.getReason() != null && !request.getReason().trim().isEmpty()
                ? request.getReason().trim() : "司机返回沙盘";

        cancelTaskFromRoute(task, reason);
        invalidateTodayTimeWindowOverride(task);
        releaseRouteIfNoPendingStops(driverRouteId);
        return buildDeliveryResponse(task, request);
    }

    private void cancelTaskFromRoute(NxDisShipmentTaskEntity task, String reason) {
        if (task == null || task.getNxDstId() == null) {
            return;
        }
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
        taskUpdate.setNxDstAdjustReason(reason);
        nxDisShipmentTaskDao.update(taskUpdate);
        nxDisRouteStopDao.deleteByShipmentTaskId(task.getNxDstId());
    }

    private void invalidateTodayTimeWindowOverride(NxDisShipmentTaskEntity task) {
        if (task == null || disRouteSandboxStopTimeWindowService == null) {
            return;
        }
        disRouteSandboxStopTimeWindowService.clearTodayOverride(
                task.getNxDstDistributerId(),
                task.getNxDstRouteDate(),
                task.getNxDstDepFatherId());
    }

    /** 本趟无待送站点时释放司机路线，便于回沙盘继续分派。 */
    private void releaseRouteIfNoPendingStops(Integer driverRouteId) {
        if (driverRouteId == null) {
            return;
        }
        if (countPendingStopsOnRoute(driverRouteId) > 0) {
            return;
        }
        NxDisDriverRouteEntity routeUpdate = new NxDisDriverRouteEntity();
        routeUpdate.setNxDdrId(driverRouteId);
        routeUpdate.setNxDdrRouteStatus(DisRouteDriverRouteStatus.IDLE);
        routeUpdate.setNxDdrStopCount(0);
        routeUpdate.setNxDdrTotalDistanceM(0L);
        routeUpdate.setNxDdrTotalDurationS(0L);
        nxDisDriverRouteDao.update(routeUpdate);

        NxDisDriverRouteEntity gateUpdate = new NxDisDriverRouteEntity();
        gateUpdate.setNxDdrId(driverRouteId);
        gateUpdate.setNxDdrLoadingEnteredAt(null);
        gateUpdate.setNxDdrLoadingEnteredOperatorUserId(null);
        gateUpdate.setNxDdrRouteStatus(DisRouteDriverRouteStatus.IDLE);
        nxDisDriverRouteDao.updateLoadingGate(gateUpdate);
    }

    private int countPendingStopsOnRoute(Integer driverRouteId) {
        List<NxDisShipmentTaskEntity> routeTasks = nxDisShipmentTaskDao.queryByDriverRouteId(driverRouteId);
        if (routeTasks == null || routeTasks.isEmpty()) {
            return 0;
        }
        int pending = 0;
        for (NxDisShipmentTaskEntity routeTask : routeTasks) {
            if (routeTask == null || routeTask.getNxDstStatus() == null) {
                continue;
            }
            String status = routeTask.getNxDstStatus().trim().toUpperCase();
            if (DisShipmentTaskStatus.CANCELLED.equals(status) || DisShipmentTaskStatus.DELIVERED.equals(status)) {
                continue;
            }
            pending++;
        }
        return pending;
    }

    private Map<String, Object> buildDeliveryResponse(NxDisShipmentTaskEntity task,
                                                    DriverDeliveryNowRequest request) throws Exception {
        Integer driverUserId = request != null ? request.getDriverUserId() : null;
        if (driverUserId == null && task != null) {
            driverUserId = task.getNxDstAssignedDriverUserId();
        }
        Integer disId = request != null ? request.getDisId() : null;
        if (disId == null && task != null) {
            disId = task.getNxDstDistributerId();
        }
        String routeDate = resolveRouteDate(request, task);
        String batchCode = resolveBatchCode(request);
        if (driverUserId == null) {
            throw new IllegalStateException("未找到司机信息");
        }
        return todayDispatchDriverTerminalService.buildDriverDeliveryToday(
                disId, routeDate, batchCode, driverUserId);
    }

    private NxDisShipmentTaskEntity requireTask(Integer deliveryStopId) {
        if (deliveryStopId == null) {
            throw new IllegalArgumentException("deliveryStopId 不能为空");
        }
        NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(deliveryStopId);
        if (task == null) {
            throw new IllegalArgumentException("deliveryStopId=" + deliveryStopId + " 不存在");
        }
        return task;
    }

    private String resolveRouteDate(DriverDeliveryNowRequest request, NxDisShipmentTaskEntity task) {
        if (request != null && request.getRouteDate() != null && !request.getRouteDate().trim().isEmpty()) {
            return request.getRouteDate().trim();
        }
        if (task != null && task.getNxDstRouteDate() != null && !task.getNxDstRouteDate().trim().isEmpty()) {
            return task.getNxDstRouteDate().trim();
        }
        return formatWhatDay(0);
    }

    private String resolveBatchCode(DriverDeliveryNowRequest request) {
        if (request != null && request.getBatchCode() != null && !request.getBatchCode().trim().isEmpty()) {
            return request.getBatchCode().trim().toUpperCase();
        }
        return DisRouteDispatchBatch.MORNING;
    }
}
