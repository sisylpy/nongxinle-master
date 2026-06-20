package com.nongxinle.service;

import com.nongxinle.dto.platform.GbPlatformOrderBridgeResult;
import com.nongxinle.entity.GbDepartmentOrdersEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;

/**
 * GB 饭店从批发商店铺下单成功后，追加平台 ASSIGNED + fulfillment（P1）。
 */
public interface GbPlatformOrderBridgeService {

    /**
     * 在 GB 单与 NX 单均已保存且双向关联完成后调用；幂等。
     */
    GbPlatformOrderBridgeResult onNxOrderCreatedFromGb(
            GbDepartmentOrdersEntity gbOrder,
            NxDepartmentOrdersEntity nxOrder);
}
