package com.nongxinle.service.platform;

import com.nongxinle.entity.NxDepartmentOrdersEntity;

/**
 * 平台配送商改 NX 订单时，同步关联的 GB 饭店订单（与 updateOrderWeightGb / cancelOutOrder 一致）。
 */
public interface PlatformDistributerGbOrderSyncService {

    void syncAfterWeightSave(NxDepartmentOrdersEntity nxOrder);

    void syncAfterOutboundFinish(NxDepartmentOrdersEntity nxOrder);

    void syncAfterPriceUpdate(NxDepartmentOrdersEntity nxOrder);

    void syncAfterCancelOutbound(NxDepartmentOrdersEntity nxOrder);
}
