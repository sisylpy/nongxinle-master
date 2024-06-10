package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-09 21:11
 */

import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDistributerDepartmentEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerDepartmentDao extends BaseDao<NxDistributerDepartmentEntity> {

    List<NxDepartmentEntity> queryDisDepartmentsBySettleType(Map<String, Object> map);



}
