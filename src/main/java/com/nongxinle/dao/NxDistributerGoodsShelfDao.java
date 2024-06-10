package com.nongxinle.dao;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-09 18:47
 */

import com.nongxinle.entity.NxDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerGoodsShelfDao extends BaseDao<NxDistributerGoodsShelfEntity> {

    List<NxDistributerGoodsShelfEntity> queryShelfByParams(Map<String, Object> map);

    NxDistributerGoodsShelfEntity queryShelfGoodsByParams(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryDisPlanShelfPurchaseApplys(Map<String, Object> map);

    List<NxDistributerGoodsShelfEntity> queryShelfWithDetailByParams(Map<String, Object> map);
}
