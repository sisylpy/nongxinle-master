package com.nongxinle.dao;

import com.nongxinle.entity.NxSupplierSwitchLogEntity;

public interface NxSupplierSwitchLogDao {

    NxSupplierSwitchLogEntity queryObject(Integer id);

    void save(NxSupplierSwitchLogEntity entity);
}
