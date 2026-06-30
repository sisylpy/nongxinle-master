package com.nongxinle.service;

import com.nongxinle.dto.route.DriverDeliveryNowRequest;

import java.util.Map;

/** 司机端：送货完成 / 返回沙盘（简化版，无额外业务校验） */
public interface DisRouteSandboxDriverDeliveryNowService {

    Map<String, Object> completeNow(Integer deliveryStopId, DriverDeliveryNowRequest request) throws Exception;

    Map<String, Object> returnToSandboxNow(Integer deliveryStopId, DriverDeliveryNowRequest request) throws Exception;
}
