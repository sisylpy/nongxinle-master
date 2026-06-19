package com.nongxinle.dao;

import com.nongxinle.dto.platform.PlatformSupplierRow;
import com.nongxinle.entity.NxDepartmentNxGoodsDefaultEntity;

import java.util.List;
import java.util.Map;

public interface NxDepartmentNxGoodsDefaultDao {

    NxDepartmentNxGoodsDefaultEntity queryObject(Integer id);

    NxDepartmentNxGoodsDefaultEntity queryActiveByMarketDepGoods(Map<String, Object> params);

    void save(NxDepartmentNxGoodsDefaultEntity entity);

    void update(NxDepartmentNxGoodsDefaultEntity entity);
}
