package com.nongxinle.service;

import com.nongxinle.entity.NxMarketDepartmentEntity;

public interface PlatformMarketDepartmentService {

    NxMarketDepartmentEntity queryActive(Integer marketId, Integer departmentId);
}
