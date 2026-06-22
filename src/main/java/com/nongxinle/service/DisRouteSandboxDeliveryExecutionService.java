package com.nongxinle.service;

import com.nongxinle.dto.route.DeliveryStopCompleteRequest;
import com.nongxinle.dto.route.DeliveryStopExceptionRequest;

import java.util.Map;

/** Phase 3E：司机端单店配送执行（送达 / 异常） */
public interface DisRouteSandboxDeliveryExecutionService {

    Map<String, Object> completeStop(Integer deliveryStopId, DeliveryStopCompleteRequest request) throws Exception;

    Map<String, Object> markException(Integer deliveryStopId, DeliveryStopExceptionRequest request) throws Exception;
}
