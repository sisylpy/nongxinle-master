package com.nongxinle.service;

import com.nongxinle.entity.NxMarketDepartmentEntity;

public interface PlatformMarketDepartmentService {

    NxMarketDepartmentEntity queryActive(Integer marketId, Integer departmentId);

    /**
     * 京采 GB 饭店：若尚未绑定市场则自动写入 nx_market_department（ACTIVE）。
     */
    NxMarketDepartmentEntity ensureActiveForGbCustomer(Integer marketId, Integer gbDepartmentId);
}
