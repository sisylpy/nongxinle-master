package com.nongxinle.service.impl;

import com.nongxinle.dao.NxMarketDepartmentDao;
import com.nongxinle.entity.NxMarketDepartmentEntity;
import com.nongxinle.service.PlatformMarketDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("platformMarketDepartmentService")
public class PlatformMarketDepartmentServiceImpl implements PlatformMarketDepartmentService {

    @Autowired
    private NxMarketDepartmentDao nxMarketDepartmentDao;

    @Override
    public NxMarketDepartmentEntity queryActive(Integer marketId, Integer departmentId) {
        if (marketId == null || departmentId == null) {
            return null;
        }
        return nxMarketDepartmentDao.queryActiveByMarketAndDepartment(marketId, departmentId);
    }
}
