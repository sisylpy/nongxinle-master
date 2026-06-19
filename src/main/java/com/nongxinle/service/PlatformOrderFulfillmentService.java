package com.nongxinle.service;

import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxPlatformOrderAssignEntity;
import com.nongxinle.entity.NxPlatformOrderFulfillmentEntity;
import com.nongxinle.service.platform.PlatformDisGoodsCostResolver;

public interface PlatformOrderFulfillmentService {

    NxPlatformOrderFulfillmentEntity queryByOrderId(Integer orderId);

    /**
     * assign 成功后幂等创建 fulfillment=ASSIGNED；已存在则直接返回。
     */
    NxPlatformOrderFulfillmentEntity ensureAssignedFulfillment(
            NxPlatformOrderAssignEntity poa,
            NxDepartmentOrdersEntity order,
            PlatformDisGoodsCostResolver.CostResolveResult costResult,
            Integer operatorId);

    /**
     * 出库完成前：平台 ASSIGNED 单尝试补有效 buying 成本；无有效成本不抛异常。
     */
    void tryResolveOutboundCost(NxDepartmentOrdersEntity order);

    /**
     * 有效成本价时写 nxDoCostSubtotal；否则跳过。
     */
    void applyOutboundCostSubtotalIfValid(NxDepartmentOrdersEntity order);

    /**
     * 现有出库完成后：PLATFORM+ASSIGNED 平台单同步 fulfillment → READY_FOR_PICKUP（幂等）。
     */
    void syncReadyForPickupAfterOutboundFinish(NxDepartmentOrdersEntity order, Integer operatorId);
}
