package com.nongxinle.dao;

import com.nongxinle.entity.NxMarketDepartmentEntity;

public interface NxMarketDepartmentDao {

    NxMarketDepartmentEntity queryObject(Integer id);

    NxMarketDepartmentEntity queryActiveByMarketAndDepartment(Integer marketId, Integer departmentId);

    void save(NxMarketDepartmentEntity entity);
}
