package com.nongxinle.service.impl;

import com.nongxinle.dao.GbDepartmentDao;
import com.nongxinle.dao.NxMarketDepartmentDao;
import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.NxMarketDepartmentEntity;
import com.nongxinle.service.PlatformMarketDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("platformMarketDepartmentService")
public class PlatformMarketDepartmentServiceImpl implements PlatformMarketDepartmentService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String SOURCE_GB_AUTO = "GB_AUTO";

    @Autowired
    private NxMarketDepartmentDao nxMarketDepartmentDao;
    @Autowired
    private GbDepartmentDao gbDepartmentDao;

    @Override
    public NxMarketDepartmentEntity queryActive(Integer marketId, Integer departmentId) {
        if (marketId == null || departmentId == null) {
            return null;
        }
        return nxMarketDepartmentDao.queryActiveByMarketAndDepartment(marketId, departmentId);
    }

    @Override
    public NxMarketDepartmentEntity ensureActiveForGbCustomer(Integer marketId, Integer gbDepartmentId) {
        NxMarketDepartmentEntity active = queryActive(marketId, gbDepartmentId);
        if (active != null) {
            return active;
        }
        GbDepartmentEntity gbDepartment = gbDepartmentDao.queryObject(gbDepartmentId);
        if (gbDepartment == null) {
            throw new IllegalArgumentException("客户不存在: departmentId=" + gbDepartmentId);
        }
        NxMarketDepartmentEntity row = new NxMarketDepartmentEntity();
        row.setNxMdMarketId(marketId);
        row.setNxMdDepartmentId(gbDepartmentId);
        row.setNxMdStatus(STATUS_ACTIVE);
        row.setNxMdSource(SOURCE_GB_AUTO);
        nxMarketDepartmentDao.save(row);
        return row;
    }
}
