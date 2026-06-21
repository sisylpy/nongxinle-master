package com.nongxinle.service.platform.impl;

import com.nongxinle.dao.GbDepartmentDao;
import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.NxMarketDepartmentEntity;
import com.nongxinle.service.PlatformMarketDepartmentService;
import com.nongxinle.service.platform.PlatformStoreDepartmentResolver;
import com.nongxinle.utils.GbTypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("platformStoreDepartmentResolver")
public class PlatformStoreDepartmentResolverImpl implements PlatformStoreDepartmentResolver {

    private static final int MAX_PARENT_DEPTH = 32;

    @Autowired
    private GbDepartmentDao gbDepartmentDao;
    @Autowired
    private PlatformMarketDepartmentService platformMarketDepartmentService;

    @Override
    public GbDepartmentEntity resolveStoreDepartment(Integer gbDepartmentId) {
        if (gbDepartmentId == null || gbDepartmentId <= 0) {
            throw new IllegalArgumentException("gbDepartmentId 无效");
        }
        GbDepartmentEntity current = gbDepartmentDao.queryObject(gbDepartmentId);
        if (current == null) {
            throw new IllegalArgumentException("部门不存在: gbDepartmentId=" + gbDepartmentId);
        }
        GbDepartmentEntity store = findStoreInChain(current);
        if (store == null) {
            throw new IllegalArgumentException("无法解析门店部门: gbDepartmentId=" + gbDepartmentId);
        }
        return store;
    }

    @Override
    public GbDepartmentEntity resolveStoreDepartmentForMarket(Integer marketId, Integer gbDepartmentId) {
        if (marketId == null || marketId <= 0) {
            throw new IllegalArgumentException("marketId 无效");
        }
        GbDepartmentEntity store = resolveStoreDepartment(gbDepartmentId);
        NxMarketDepartmentEntity link = platformMarketDepartmentService.queryActive(marketId, store.getGbDepartmentId());
        if (link == null) {
            throw new IllegalArgumentException("门店不属于当前市场: storeGbDepartmentId="
                    + store.getGbDepartmentId() + ", marketId=" + marketId);
        }
        return store;
    }

    private GbDepartmentEntity findStoreInChain(GbDepartmentEntity start) {
        GbDepartmentEntity current = start;
        int depth = 0;
        while (current != null && depth < MAX_PARENT_DEPTH) {
            if (isStoreDepartment(current)) {
                return current;
            }
            Integer fatherId = current.getGbDepartmentFatherId();
            if (fatherId == null || fatherId <= 0) {
                break;
            }
            current = gbDepartmentDao.queryObject(fatherId);
            depth++;
        }
        return null;
    }

    private boolean isStoreDepartment(GbDepartmentEntity department) {
        return department.getGbDepartmentIsGroupDep() != null
                && department.getGbDepartmentIsGroupDep() == 1
                && department.getGbDepartmentType() != null
                && department.getGbDepartmentType().equals(GbTypeUtils.GB_DEPARTMENT_TYPE_MENDIAN);
    }
}
