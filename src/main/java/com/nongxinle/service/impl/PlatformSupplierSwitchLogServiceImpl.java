package com.nongxinle.service.impl;

import com.nongxinle.dao.NxSupplierSwitchLogDao;
import com.nongxinle.entity.NxSupplierSwitchLogEntity;
import com.nongxinle.service.PlatformSupplierSwitchLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("platformSupplierSwitchLogService")
public class PlatformSupplierSwitchLogServiceImpl implements PlatformSupplierSwitchLogService {

    @Autowired
    private NxSupplierSwitchLogDao nxSupplierSwitchLogDao;

    @Override
    public NxSupplierSwitchLogEntity queryObject(Integer id) {
        return nxSupplierSwitchLogDao.queryObject(id);
    }
}
