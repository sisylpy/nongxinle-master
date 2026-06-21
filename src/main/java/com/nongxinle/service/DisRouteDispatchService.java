package com.nongxinle.service;

import com.nongxinle.dto.route.DisRouteDispatchResult;
import com.nongxinle.dto.route.DisRoutePreviewRequest;
import com.nongxinle.dto.route.DisRouteReoptimizeRequest;
import com.nongxinle.dto.route.DriverRouteTasksResponse;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDistributerUserEntity;

import java.util.List;

public interface DisRouteDispatchService {

    /** 沙盘自动分派（新主链） */
    DisRouteDispatchResult simulate(DisRoutePreviewRequest request) throws Exception;

    /** 尊重 manualLocked 的重算（后续实现） */
    NxDisRoutePlanEntity reoptimize(DisRouteReoptimizeRequest request) throws Exception;

    NxDisRoutePlanEntity getPlan(Integer planId);

    NxDisRoutePlanEntity getPlanByRouteDate(Integer disId, String routeDate, String status, String batchCode);

    NxDisRoutePlanEntity getTodayPlan(Integer disId, String status, String batchCode);

    /** 司机装车：task.status=ASSIGNED */
    DriverRouteTasksResponse getDriverLoadingToday(Integer driverUserId);

    /** 司机配送：task.status=READY_TO_GO 或 plan.status=READY */
    DriverRouteTasksResponse getDriverDeliveryToday(Integer driverUserId);

    List<NxDistributerUserEntity> listDrivers(Integer disId);
}
