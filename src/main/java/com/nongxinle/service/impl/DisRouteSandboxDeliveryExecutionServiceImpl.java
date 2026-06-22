package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dto.route.DeliveryStopCompleteRequest;
import com.nongxinle.dto.route.DeliveryStopExceptionRequest;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteDeliveryExceptionType;
import com.nongxinle.route.DisRouteDispatchLabels;
import com.nongxinle.route.DisRouteDriverRouteStatus;
import com.nongxinle.route.DisRouteRouteExecutionHelper;
import com.nongxinle.route.DisShipmentTaskStatus;
import com.nongxinle.route.RouteDispatchDateFormat;
import com.nongxinle.service.DisRouteSandboxDeliveryExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisShipmentTaskStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.CLOSED;

@Service("disRouteSandboxDeliveryExecutionService")
public class DisRouteSandboxDeliveryExecutionServiceImpl implements DisRouteSandboxDeliveryExecutionService {

    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;

    @Override
    @Transactional
    public Map<String, Object> completeStop(Integer deliveryStopId, DeliveryStopCompleteRequest request) throws Exception {
        validateOperator(request != null ? request.getOperatorUserId() : null);
        NxDisShipmentTaskEntity task = requireTask(deliveryStopId);

        if (DisShipmentTaskStatus.DELIVERED.equals(task.getNxDstStatus())) {
            return buildCompleteResponse(task, true, isDriverRouteCompleted(task.getNxDstDriverRouteId()));
        }
        if (DisShipmentTaskStatus.EXCEPTION.equals(task.getNxDstStatus())) {
            throw new IllegalStateException("配送异常状态不能标记已送达");
        }
        if (!DisShipmentTaskStatus.IN_DELIVERY.equals(task.getNxDstStatus())) {
            throw new IllegalStateException("当前状态不允许标记已送达：" + labelStatus(task.getNxDstStatus()));
        }

        Date completedAt = new Date();
        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(task.getNxDstId());
        update.setNxDstStatus(DisShipmentTaskStatus.DELIVERED);
        update.setNxDstDeliveredAt(completedAt);
        update.setNxDstDeliveryRemark(trimToNull(request.getRemark()));
        update.setNxDstDeliveryOperatorUserId(request.getOperatorUserId());
        update.setNxDstOperatorUserId(request.getOperatorUserId());
        nxDisShipmentTaskDao.update(update);

        task.setNxDstStatus(DisShipmentTaskStatus.DELIVERED);
        task.setNxDstDeliveredAt(completedAt);
        task.setNxDstDeliveryRemark(update.getNxDstDeliveryRemark());
        task.setNxDstDeliveryOperatorUserId(request.getOperatorUserId());

        boolean driverRouteCompleted = maybeCompleteDriverRoute(task.getNxDstDriverRouteId());
        return buildCompleteResponse(task, false, driverRouteCompleted);
    }

    @Override
    @Transactional
    public Map<String, Object> markException(Integer deliveryStopId, DeliveryStopExceptionRequest request) throws Exception {
        validateOperator(request != null ? request.getOperatorUserId() : null);
        NxDisShipmentTaskEntity task = requireTask(deliveryStopId);

        if (DisShipmentTaskStatus.EXCEPTION.equals(task.getNxDstStatus())) {
            return buildExceptionResponse(task, true);
        }
        if (DisShipmentTaskStatus.DELIVERED.equals(task.getNxDstStatus())) {
            throw new IllegalStateException("已送达客户不能标记配送异常");
        }
        if (!DisShipmentTaskStatus.IN_DELIVERY.equals(task.getNxDstStatus())) {
            throw new IllegalStateException("当前状态不允许标记配送异常：" + labelStatus(task.getNxDstStatus()));
        }

        String exceptionType = DisRouteDeliveryExceptionType.normalize(request.getExceptionType());
        Date exceptionAt = new Date();
        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(task.getNxDstId());
        update.setNxDstStatus(DisShipmentTaskStatus.EXCEPTION);
        update.setNxDstExceptionType(exceptionType);
        update.setNxDstExceptionRemark(trimToNull(request.getRemark()));
        update.setNxDstExceptionAt(exceptionAt);
        update.setNxDstOperatorUserId(request.getOperatorUserId());
        nxDisShipmentTaskDao.update(update);

        task.setNxDstStatus(DisShipmentTaskStatus.EXCEPTION);
        task.setNxDstExceptionType(exceptionType);
        task.setNxDstExceptionRemark(update.getNxDstExceptionRemark());
        task.setNxDstExceptionAt(exceptionAt);

        return buildExceptionResponse(task, false);
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

    private void validateOperator(Integer operatorUserId) {
        if (operatorUserId == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
    }

    private boolean maybeCompleteDriverRoute(Integer driverRouteId) {
        if (driverRouteId == null) {
            return false;
        }
        List<NxDisShipmentTaskEntity> routeTasks = nxDisShipmentTaskDao.queryByDriverRouteId(driverRouteId);
        if (routeTasks == null || routeTasks.isEmpty()) {
            return false;
        }
        boolean allDelivered = true;
        for (NxDisShipmentTaskEntity routeTask : routeTasks) {
            if (routeTask == null || isInactiveTask(routeTask)) {
                continue;
            }
            if (!DisShipmentTaskStatus.DELIVERED.equals(routeTask.getNxDstStatus())) {
                allDelivered = false;
                break;
            }
        }
        if (!allDelivered) {
            return false;
        }
        NxDisDriverRouteEntity routeUpdate = new NxDisDriverRouteEntity();
        routeUpdate.setNxDdrId(driverRouteId);
        routeUpdate.setNxDdrRouteStatus(DisRouteDriverRouteStatus.COMPLETED);
        routeUpdate.setNxDdrDispatchEligible(0);
        nxDisDriverRouteDao.updateExecution(routeUpdate);
        return true;
    }

    private boolean isDriverRouteCompleted(Integer driverRouteId) {
        if (driverRouteId == null) {
            return false;
        }
        NxDisDriverRouteEntity route = nxDisDriverRouteDao.queryObject(driverRouteId);
        if (route == null) {
            return false;
        }
        String status = DisRouteRouteExecutionHelper.resolveRouteStatus(route);
        return DisRouteDriverRouteStatus.COMPLETED.equals(status)
                || DisRouteDriverRouteStatus.DELIVERED.equals(status);
    }

    private static boolean isInactiveTask(NxDisShipmentTaskEntity task) {
        return CANCELLED.equals(task.getNxDstStatus()) || CLOSED.equals(task.getNxDstStatus());
    }

    private Map<String, Object> buildCompleteResponse(NxDisShipmentTaskEntity task,
                                                      boolean alreadyCompleted,
                                                      boolean driverRouteCompleted) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("deliveryStopId", task.getNxDstId());
        result.put("driverRouteId", task.getNxDstDriverRouteId());
        result.put("driverUserId", task.getNxDstAssignedDriverUserId());
        result.put("status", DisShipmentTaskStatus.DELIVERED);
        result.put("statusLabel", DisRouteDispatchLabels.label(DisShipmentTaskStatus.DELIVERED));
        result.put("deliveryStatusLabel", DisRouteDispatchLabels.label(DisShipmentTaskStatus.DELIVERED));
        if (task.getNxDstDeliveredAt() != null) {
            String formatted = RouteDispatchDateFormat.format(task.getNxDstDeliveredAt());
            result.put("deliveredAt", formatted);
            result.put("completedAt", formatted);
        }
        if (task.getNxDstDeliveryRemark() != null) {
            result.put("remark", task.getNxDstDeliveryRemark());
        }
        result.put("driverRouteCompleted", driverRouteCompleted);
        if (alreadyCompleted) {
            result.put("alreadyCompleted", true);
        }
        return result;
    }

    private Map<String, Object> buildExceptionResponse(NxDisShipmentTaskEntity task, boolean alreadyMarked) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("deliveryStopId", task.getNxDstId());
        result.put("driverRouteId", task.getNxDstDriverRouteId());
        result.put("driverUserId", task.getNxDstAssignedDriverUserId());
        result.put("status", DisShipmentTaskStatus.EXCEPTION);
        result.put("statusLabel", DisRouteDispatchLabels.label(DisShipmentTaskStatus.EXCEPTION));
        result.put("deliveryStatusLabel", DisRouteDispatchLabels.label(DisShipmentTaskStatus.EXCEPTION));
        result.put("exceptionType", task.getNxDstExceptionType());
        result.put("exceptionTypeLabel", DisRouteDeliveryExceptionType.label(task.getNxDstExceptionType()));
        if (task.getNxDstExceptionRemark() != null) {
            result.put("exceptionRemark", task.getNxDstExceptionRemark());
        }
        if (task.getNxDstExceptionAt() != null) {
            result.put("exceptionAt", RouteDispatchDateFormat.format(task.getNxDstExceptionAt()));
        }
        if (alreadyMarked) {
            result.put("alreadyMarked", true);
        }
        return result;
    }

    private static String labelStatus(String status) {
        String label = DisRouteDispatchLabels.label(status);
        return label != null ? label : status;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
