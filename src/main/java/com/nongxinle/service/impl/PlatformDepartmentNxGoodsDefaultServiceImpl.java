package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDepartmentNxGoodsDefaultDao;
import com.nongxinle.entity.NxDepartmentNxGoodsDefaultEntity;
import com.nongxinle.service.PlatformDepartmentNxGoodsDefaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("platformDepartmentNxGoodsDefaultService")
public class PlatformDepartmentNxGoodsDefaultServiceImpl implements PlatformDepartmentNxGoodsDefaultService {

    @Autowired
    private NxDepartmentNxGoodsDefaultDao nxDepartmentNxGoodsDefaultDao;

    @Override
    public NxDepartmentNxGoodsDefaultEntity queryObject(Integer id) {
        return nxDepartmentNxGoodsDefaultDao.queryObject(id);
    }
}
