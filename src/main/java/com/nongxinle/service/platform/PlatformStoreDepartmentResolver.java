package com.nongxinle.service.platform;

import com.nongxinle.entity.GbDepartmentEntity;

/**
 * 将任意 GB 部门解析为门店级持券主体。
 */
public interface PlatformStoreDepartmentResolver {

    /**
     * 解析门店部门；子部门向上查找所属门店。
     */
    GbDepartmentEntity resolveStoreDepartment(Integer gbDepartmentId);

    /**
     * 解析门店并校验其归属于指定市场（nx_market_department ACTIVE）。
     */
    GbDepartmentEntity resolveStoreDepartmentForMarket(Integer marketId, Integer gbDepartmentId);
}
