package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-08 19:09
 */

import com.nongxinle.entity.NxDepartmentOrdersHistoryEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxDepartmentOrdersHistoryDao extends BaseDao<NxDepartmentOrdersHistoryEntity> {

    List<NxDepartmentOrdersHistoryEntity> queryDepHistoryOrdersByParams(Map<String, Object> map1);

    int queryOrderTimes(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDepTodayOrder(Map<String, Object> map);
}
