package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-30 10:14
 */

import com.nongxinle.entity.GbDepartmentOrdersHistoryEntity;
import com.nongxinle.entity.NxDepartmentOrdersHistoryEntity;

import java.util.List;
import java.util.Map;


public interface GbDepartmentOrdersHistoryDao extends BaseDao<GbDepartmentOrdersHistoryEntity> {

    List<GbDepartmentOrdersHistoryEntity> queryGbDepHistoryOrdersByParams(Map<String, Object> map1);

    List<GbDepartmentOrdersHistoryEntity> queryDepHistoryOrdersByParamsGb(Map<String, Object> map1);
}
