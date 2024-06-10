package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-21 12:08
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;


public interface NxDistributerWeightDao extends BaseDao<NxDistributerWeightEntity> {

    List<NxDistributerWeightEntity> queryWeightListByParams(Map<String, Object> map);

    NxDistributerWeightEntity queryWeightOrdersById(Integer id);

    int queryWeightCountByParams(Map<String, Object> map);

    List<NxDistributerPurchaseGoodsEntity> queryWeightGoodsById(Integer id);

    List<NxDistributerFatherGoodsEntity> queryFatherGoodsStockOrder(Map<String, Object> map);

    List<NxDepartmentEntity> queryWeightDepOrders(Integer id);
}
