package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisRouteStopDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dto.route.DriverRouteDepartRequest;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteDriverRouteStatus;
import com.nongxinle.route.DisRouteDispatchBatch;
import com.nongxinle.route.DisShipmentTaskStatus;
import com.nongxinle.todaydispatch.TodayDispatchDriverTerminalService;
import com.nongxinle.service.DisRouteSandboxDriverDepartNowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisRoutePlanStatus.ASSIGNED;
import static com.nongxinle.route.DisRoutePlanStatus.READY;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

@Service("disRouteSandboxDriverDepartNowService")
public class DisRouteSandboxDriverDepartNowServiceImpl implements DisRouteSandboxDriverDepartNowService {

    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisRouteStopDao nxDisRouteStopDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private TodayDispatchDriverTerminalService todayDispatchDriverTerminalService;

    @Override
    @Transactional
    public Map<String, Object> departNow(DriverRouteDepartRequest request) throws Exception {
        if (request == null || request.getDriverUserId() == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }

        Integer planId = resolvePlanId(request);
        Integer driverRouteId = resolveDriverRouteId(request, planId);
        Date departAt = new Date();

        NxDisDriverRouteEntity routeUpdate = new NxDisDriverRouteEntity();
        routeUpdate.setNxDdrId(driverRouteId);
        routeUpdate.setNxDdrActualDepartAt(departAt);
        routeUpdate.setNxDdrRouteStatus(DisRouteDriverRouteStatus.IN_DELIVERY);
        nxDisDriverRouteDao.updateExecution(routeUpdate);

        NxDisRouteStopEntity firstStop = findFirstStop(driverRouteId);
        if (firstStop != null && firstStop.getNxDrsId() != null) {
            NxDisRouteStopEntity stopUpdate = new NxDisRouteStopEntity();
            stopUpdate.setNxDrsId(firstStop.getNxDrsId());
            stopUpdate.setNxDrsPlannedDepartureAt(departAt);
            nxDisRouteStopDao.update(stopUpdate);

            Integer firstTaskId = firstStop.getNxDrsShipmentTaskId();
            if (firstTaskId != null) {
                NxDisShipmentTaskEntity firstTaskUpdate = new NxDisShipmentTaskEntity();
                firstTaskUpdate.setNxDstId(firstTaskId);
                firstTaskUpdate.setNxDstPlannedDepartureAt(departAt);
                nxDisShipmentTaskDao.update(firstTaskUpdate);
            }
        }

        List<NxDisShipmentTaskEntity> routeTasks = loadTasksForDepart(request, planId, driverRouteId);
        if (routeTasks != null) {
            for (NxDisShipmentTaskEntity task : routeTasks) {
                if (task == null || task.getNxDstId() == null) {
                    continue;
                }
                if (DisShipmentTaskStatus.CANCELLED.equals(task.getNxDstStatus())) {
                    continue;
                }
                NxDisShipmentTaskEntity taskUpdate = new NxDisShipmentTaskEntity();
                taskUpdate.setNxDstId(task.getNxDstId());
                taskUpdate.setNxDstStatus(DisShipmentTaskStatus.IN_DELIVERY);
                if (needsClearStaleDelivery(task, departAt)) {
                    taskUpdate.setClearDeliveryCompletion(true);
                }
                nxDisShipmentTaskDao.update(taskUpdate);
            }
        }

        String routeDate = resolveRouteDate(request);
        String batchCode = resolveBatchCode(request);
        Integer disId = request.getDisId();
        if (disId == null && routeTasks != null && !routeTasks.isEmpty() && routeTasks.get(0) != null) {
            disId = routeTasks.get(0).getNxDstDistributerId();
        }

        return todayDispatchDriverTerminalService.buildDriverDeliveryToday(
                disId, routeDate, batchCode, request.getDriverUserId());
    }

    private Integer resolvePlanId(DriverRouteDepartRequest request) {
        if (request.getPlanId() != null) {
            return request.getPlanId();
        }
        if (request.getDisId() == null) {
            return null;
        }
        String routeDate = resolveRouteDate(request);
        String batchCode = resolveBatchCode(request);
        for (String status : new String[]{ASSIGNED, READY}) {
            NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                    request.getDisId(), routeDate, batchCode, status);
            if (plan != null && plan.getNxDrpId() != null) {
                return plan.getNxDrpId();
            }
        }
        return null;
    }

    private Integer resolveDriverRouteId(DriverRouteDepartRequest request, Integer planId) {
        if (request.getDriverRouteId() != null) {
            return request.getDriverRouteId();
        }
        if (planId != null && request.getDriverUserId() != null) {
            NxDisDriverRouteEntity route = nxDisDriverRouteDao.queryByPlanAndDriver(
                    planId, request.getDriverUserId());
            if (route != null && route.getNxDdrId() != null) {
                return route.getNxDdrId();
            }
            route = createDriverRoute(planId, request.getDriverUserId());
            if (route != null && route.getNxDdrId() != null) {
                return route.getNxDdrId();
            }
        }
        throw new IllegalStateException("未找到司机路线");
    }

    private NxDisDriverRouteEntity createDriverRoute(Integer planId, Integer driverUserId) {
        NxDisDriverRouteEntity route = new NxDisDriverRouteEntity();
        route.setNxDdrPlanId(planId);
        route.setNxDdrDriverUserId(driverUserId);
        route.setNxDdrRouteSeq(1);
        route.setNxDdrTotalDistanceM(0L);
        route.setNxDdrTotalDurationS(0L);
        route.setNxDdrStopCount(0);
        route.setNxDdrRouteStatus(DisRouteDriverRouteStatus.DISPATCH_CONFIRMED);
        nxDisDriverRouteDao.save(route);
        return route;
    }

    private List<NxDisShipmentTaskEntity> loadTasksForDepart(DriverRouteDepartRequest request,
                                                             Integer planId,
                                                             Integer driverRouteId) {
        List<NxDisShipmentTaskEntity> routeTasks = nxDisShipmentTaskDao.queryByDriverRouteId(driverRouteId);
        if (routeTasks != null && !routeTasks.isEmpty()) {
            return routeTasks;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("disId", request.getDisId());
        params.put("routeDate", resolveRouteDate(request));
        params.put("assignedDriverUserId", request.getDriverUserId());
        if (planId != null) {
            params.put("planId", planId);
        }
        List<NxDisShipmentTaskEntity> assignedTasks = nxDisShipmentTaskDao.queryList(params);
        if (assignedTasks == null || assignedTasks.isEmpty()) {
            return new ArrayList<NxDisShipmentTaskEntity>();
        }
        List<NxDisShipmentTaskEntity> activeTasks = new ArrayList<NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : assignedTasks) {
            if (task == null || task.getNxDstId() == null) {
                continue;
            }
            if (DisShipmentTaskStatus.CANCELLED.equals(task.getNxDstStatus())) {
                continue;
            }
            if (task.getNxDstDriverRouteId() == null) {
                NxDisShipmentTaskEntity link = new NxDisShipmentTaskEntity();
                link.setNxDstId(task.getNxDstId());
                link.setNxDstDriverRouteId(driverRouteId);
                nxDisShipmentTaskDao.update(link);
                task.setNxDstDriverRouteId(driverRouteId);
            }
            activeTasks.add(task);
        }
        return activeTasks;
    }

    private NxDisRouteStopEntity findFirstStop(Integer driverRouteId) {
        List<NxDisRouteStopEntity> stops = nxDisRouteStopDao.queryByDriverRouteId(driverRouteId);
        if (stops == null || stops.isEmpty()) {
            return null;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop != null && stop.getNxDrsShipmentTaskId() != null) {
                return stop;
            }
        }
        return stops.get(0);
    }

    private String resolveRouteDate(DriverRouteDepartRequest request) {
        if (request.getRouteDate() != null && !request.getRouteDate().trim().isEmpty()) {
            return request.getRouteDate().trim();
        }
        return formatWhatDay(0);
    }

    private String resolveBatchCode(DriverRouteDepartRequest request) {
        if (request.getBatchCode() != null && !request.getBatchCode().trim().isEmpty()) {
            return request.getBatchCode().trim().toUpperCase();
        }
        return DisRouteDispatchBatch.MORNING;
    }

    /** 上一轮测试/误点的 DELIVERED 在重新出发时应清掉并进入 IN_DELIVERY。 */
    private static boolean needsClearStaleDelivery(NxDisShipmentTaskEntity task, Date departAt) {
        if (task == null || task.getNxDstStatus() == null) {
            return false;
        }
        String status = task.getNxDstStatus().trim().toUpperCase();
        if (DisShipmentTaskStatus.DELIVERED.equals(status) || DisShipmentTaskStatus.CLOSED.equals(status)) {
            return true;
        }
        if (task.getNxDstDeliveredAt() != null && departAt != null
                && task.getNxDstDeliveredAt().before(departAt)) {
            return true;
        }
        return false;
    }
}
