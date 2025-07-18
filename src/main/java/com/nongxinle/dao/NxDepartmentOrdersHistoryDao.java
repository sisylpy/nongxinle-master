package com.nongxinle.dao;

/**
 *
 *
 * @author lpy
 * @date 05-08 19:09
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;


public interface NxDepartmentOrdersHistoryDao extends BaseDao<NxDepartmentOrdersHistoryEntity> {

    List<NxDepartmentOrdersHistoryEntity> queryDepHistoryOrdersByParams(Map<String, Object> map1);

    int queryOrderTimes(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDepTodayOrder(Map<String, Object> map);


    List<Integer> queryRecentOrders(NxDepartmentEntity departmentId);

    int queryDepOrderCount(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDisGoodsByParams(Map<String, Object> mapGD);

    List<Map<String,Object>> queryLogs(Map<String, Object> mapLog);

    List<Integer> selectFrequentGoods(Map<String, Object> map);

    List<NxDepartmentOrdersHistoryEntity> selectLastOrders(Map<String, Object> map);

    List<AvgDaily> selectAvgDaily(Map<String, Object> map);


//    NxDepartmentOrdersHistoryEntity selectLastOrder(Map<String, Object> lastParams);

}
