package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-24 11:45
 */

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerPurchaseGoodsEntity;
import com.nongxinle.entity.NxDistributerEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerPurchaseGoodsDao extends BaseDao<GbDistributerPurchaseGoodsEntity> {

    List<GbDistributerFatherGoodsEntity> queryDisPurchaseGoods(Map<String, Object> map);

//    /////////////////

//    List<GbDistributerPurchaseGoodsEntity> queryPurchaseGoodsByUUID(String uuid);

    List<GbDistributerPurchaseGoodsEntity> queryPurchaseGoodsByGoodsId(Map<String, Object> map);

    List<GbDistributerPurchaseGoodsEntity> queryPurUserPurchaseGoods(Integer purUserId);

    List<GbDistributerPurchaseGoodsEntity> queryPurchaseGoodsByBatchId(Integer purchaseBatchId);

    List<GbDistributerPurchaseGoodsEntity> queryForDisGoods(Map<String, Object> map2);

    List<GbDistributerPurchaseGoodsEntity> queryPurchaseGoodsByParams(Map<String, Object> map2);

    int queryPurchaseGoodsTotal(Map<String, Object> map2);

    List<GbDistributerPurchaseGoodsEntity> queryPurchaseGoodsWithDetailByParams(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryStockPurchaseGoods(Map<String, Object> map4);

    Integer queryGbPurchaseGoodsCount(Map<String, Object> map);

    Double queryPurchaseGoodsSubTotal(Map<String, Object> map);

    int queryPurchaseGoodsAmount(Map<String, Object> map);

    Double queryPurchaseInventoryGoodsSubTotal(Map<String, Object> map);

    List<GbDistributerPurchaseGoodsEntity> queryPurchaseInventoryGoodsList(Map<String, Object> map);

    Double queryPurchaseGoodsWeightTotal(Map<String, Object> map1);

    String queryPurchaseGoodsPrice(Map<String, Object> map1);

    List<GbDistributerFatherGoodsEntity> queryDisPurchaseGoodsForNxDis(Map<String, Object> map4);

    String queryPurGoodsMaxPrice(Map<String, Object> map);

    String queryPurGoodsMinPrice(Map<String, Object> map);

    GbDistributerPurchaseGoodsEntity queryPurGoodsWithOrders(Integer id);
}
