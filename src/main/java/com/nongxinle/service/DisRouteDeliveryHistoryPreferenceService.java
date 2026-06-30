package com.nongxinle.service;

import com.nongxinle.dto.route.DeliveryHistoryPreferenceBatchResult;
import com.nongxinle.dto.route.DeliveryHistoryPreferenceResolveRequest;

/**
 * 历史配送偏好只读服务（P1）。
 * 不写 DB；不改变 optimizer 结果。
 */
public interface DisRouteDeliveryHistoryPreferenceService {

    DeliveryHistoryPreferenceBatchResult resolve(DeliveryHistoryPreferenceResolveRequest request);
}
