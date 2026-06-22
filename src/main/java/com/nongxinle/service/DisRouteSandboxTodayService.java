package com.nongxinle.service;

import java.util.Map;

public interface DisRouteSandboxTodayService {

    Map<String, Object> buildToday(Integer disId, String routeDate, String batchCode) throws Exception;

    Map<String, Object> buildToday(Integer disId, String routeDate, String batchCode,
                                   Integer operatorUserId) throws Exception;

    /** 司机装车页：只含 loadingDriverRoutes / loadingStops / loadingSummary */
    Map<String, Object> buildLoadingToday(Integer disId, String routeDate, String batchCode) throws Exception;

    Map<String, Object> buildLoadingToday(Integer disId, String routeDate, String batchCode,
                                          Integer driverUserId) throws Exception;

    Map<String, Object> buildLoadingToday(Integer disId, String routeDate, String batchCode,
                                          Integer driverUserId, Integer operatorUserId) throws Exception;

    /** 装车页正式接口：仅返回收缩后的 pageViewModel。 */
    Map<String, Object> buildLoadingSandboxToday(Integer disId, String routeDate, String batchCode,
                                                 Integer operatorUserId) throws Exception;

    /** 配送任务页：只含 executionDriverRoutes / executionStops / executionSummary */
    Map<String, Object> buildDeliveryToday(Integer disId, String routeDate, String batchCode) throws Exception;

    Map<String, Object> buildDeliveryToday(Integer disId, String routeDate, String batchCode,
                                           Integer driverUserId) throws Exception;

    /** 今日派车页：只含沙箱切片（不含 loading/execution 分区） */
    Map<String, Object> buildDispatchSandboxToday(Integer disId, String routeDate, String batchCode) throws Exception;

    Map<String, Object> buildDispatchSandboxToday(Integer disId, String routeDate, String batchCode,
                                                  Integer operatorUserId) throws Exception;
}
