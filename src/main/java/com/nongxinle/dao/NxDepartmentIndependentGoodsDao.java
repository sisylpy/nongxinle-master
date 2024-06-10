package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 07-24 13:00
 */

import com.nongxinle.entity.NxDepartmentIndependentGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxDepartmentIndependentGoodsDao extends BaseDao<NxDepartmentIndependentGoodsEntity> {

    List<NxDepartmentIndependentGoodsEntity> querySearchStr(Map<String, Object> map);
}
