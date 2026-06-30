package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisSandboxDayTimeWindowDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dto.route.SandboxStopTimeWindowRequest;
import com.nongxinle.dto.route.TaskTimeWindowRequest;
import com.nongxinle.entity.NxDisSandboxDayTimeWindowEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteDispatchBatch;
import com.nongxinle.route.DisRouteDispatchOperatorResolver;
import com.nongxinle.route.DisRouteSandboxStopKeyUtils;
import com.nongxinle.route.DisShipmentTaskStatus;
import com.nongxinle.service.DisRouteSandboxStopTimeWindowService;
import com.nongxinle.service.DisRouteTaskTimeWindowService;
import com.nongxinle.todaydispatch.TodayDispatchFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.formatWhatDay;

@Service
public class DisRouteSandboxStopTimeWindowServiceImpl implements DisRouteSandboxStopTimeWindowService {

    @Autowired
    private NxDisSandboxDayTimeWindowDao nxDisSandboxDayTimeWindowDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private DisRouteTaskTimeWindowService disRouteTaskTimeWindowService;
    @Autowired
    private TodayDispatchFacade todayDispatchFacade;
    @Autowired
    private DisRouteDispatchOperatorResolver disRouteDispatchOperatorResolver;

    @Override
    @Transactional
    public Map<String, Object> updateStopTimeWindow(SandboxStopTimeWindowRequest request) throws Exception {
        validateRequest(request);
        Integer disId = request.getDisId();
        String routeDate = resolveRouteDate(request.getRouteDate());
        String batchCode = normalizeBatch(request.getBatchCode());
        Integer depFatherId = resolveDepFatherId(request);
        Integer operatorUserId = disRouteDispatchOperatorResolver.resolve(disId, request.getOperatorUserId());

        Integer deliveryStopId = request.getDeliveryStopId();
        if (deliveryStopId == null && depFatherId != null) {
            NxDisShipmentTaskEntity openTask = findMutableOpenTask(disId, routeDate, depFatherId);
            if (openTask != null && openTask.getNxDstId() != null) {
                deliveryStopId = openTask.getNxDstId();
            }
        }

        if (deliveryStopId != null) {
            TaskTimeWindowRequest taskRequest = new TaskTimeWindowRequest();
            taskRequest.setEarliestDeliveryTimeS(request.getEarliestDeliveryTimeS());
            taskRequest.setLatestDeliveryTimeS(request.getLatestDeliveryTimeS());
            taskRequest.setServiceMinutes(request.getServiceMinutes());
            taskRequest.setReason(normalizeReason(request.getReason()));
            taskRequest.setOperatorUserId(operatorUserId);
            disRouteTaskTimeWindowService.updateTimeWindow(deliveryStopId, taskRequest);
        }
        // 同一门店当日只保留一条调整：task 与 day override 表同步写入
        upsertSandboxDayOverride(disId, routeDate, depFatherId, request, operatorUserId);

        if ("LOADING".equalsIgnoreCase(request.getResponsePage())) {
            return todayDispatchFacade.buildLoadingPage(
                    disId, routeDate, batchCode, operatorUserId);
        }
        return todayDispatchFacade.buildDispatchPage(
                disId, routeDate, batchCode, operatorUserId);
    }

    @Override
    public void clearTodayOverride(Integer disId, String routeDate, Integer depFatherId) {
        if (disId == null || depFatherId == null) {
            return;
        }
        String effectiveDate = resolveRouteDate(routeDate);
        nxDisSandboxDayTimeWindowDao.deleteByDisRouteDep(disId, effectiveDate, depFatherId);
    }

    private void upsertSandboxDayOverride(Integer disId,
                                          String routeDate,
                                          Integer depFatherId,
                                          SandboxStopTimeWindowRequest request,
                                          Integer operatorUserId) {
        NxDisSandboxDayTimeWindowEntity entity = new NxDisSandboxDayTimeWindowEntity();
        entity.setNxDsdtwDistributerId(disId);
        entity.setNxDsdtwRouteDate(routeDate);
        entity.setNxDsdtwDepFatherId(depFatherId);
        entity.setNxDsdtwEarliestDeliveryTimeS(request.getEarliestDeliveryTimeS());
        entity.setNxDsdtwLatestDeliveryTimeS(request.getLatestDeliveryTimeS());
        entity.setNxDsdtwServiceMinutes(request.getServiceMinutes());
        entity.setNxDsdtwAdjustReason(normalizeReason(request.getReason()));
        entity.setNxDsdtwOperatorUserId(operatorUserId);
        entity.setNxDsdtwUpdatedAt(new Date());
        nxDisSandboxDayTimeWindowDao.upsert(entity);
    }

    private NxDisShipmentTaskEntity findMutableOpenTask(Integer disId, String routeDate, Integer depFatherId) {
        NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryByOpenKey(
                com.nongxinle.route.DisShipmentTaskOpenKeyUtils.buildOpenKey(disId, routeDate, depFatherId));
        if (task == null || task.getNxDstId() == null) {
            return null;
        }
        String status = task.getNxDstStatus();
        if (DisShipmentTaskStatus.IN_DELIVERY.equals(status)
                || DisShipmentTaskStatus.DELIVERED.equals(status)
                || DisShipmentTaskStatus.CANCELLED.equals(status)
                || DisShipmentTaskStatus.CLOSED.equals(status)
                || DisShipmentTaskStatus.READY_TO_GO.equals(status)) {
            return null;
        }
        return task;
    }

    private void validateRequest(SandboxStopTimeWindowRequest request) {
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        if (request.getLatestDeliveryTimeS() == null) {
            throw new IllegalArgumentException("latestDeliveryTimeS 不能为空");
        }
        if (request.getEarliestDeliveryTimeS() != null
                && request.getEarliestDeliveryTimeS() > request.getLatestDeliveryTimeS()) {
            throw new IllegalArgumentException("earliestDeliveryTimeS 不能晚于 latestDeliveryTimeS");
        }
        if (resolveDepFatherId(request) == null) {
            throw new IllegalArgumentException("departmentId / depFatherId 不能为空");
        }
    }

    private static Integer resolveDepFatherId(SandboxStopTimeWindowRequest request) {
        if (request.getDepFatherId() != null) {
            return request.getDepFatherId();
        }
        if (request.getDepartmentId() != null) {
            return request.getDepartmentId();
        }
        if (request.getSandboxStopKey() != null && !request.getSandboxStopKey().trim().isEmpty()) {
            return DisRouteSandboxStopKeyUtils.parseDepFatherId(request.getSandboxStopKey().trim());
        }
        return null;
    }

    private static String resolveRouteDate(String routeDate) {
        if (routeDate != null && !routeDate.trim().isEmpty()) {
            return routeDate.trim();
        }
        return formatWhatDay(0);
    }

    private static String normalizeReason(String reason) {
        if (reason == null) {
            return "";
        }
        return reason.trim();
    }

    private static String normalizeBatch(String batchCode) {
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return DisRouteDispatchBatch.MORNING;
        }
        return batchCode.trim().toUpperCase();
    }
}
