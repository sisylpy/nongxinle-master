package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-24 11:45
 */

import com.nongxinle.entity.NxDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerPurchaseGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerPurchaseGoodsDao extends BaseDao<NxDistributerPurchaseGoodsEntity> {

    List<NxDistributerFatherGoodsEntity> queryDisPurchaseGoods(Map<String, Object> map);

//    /////////////////

//    List<NxDistributerPurchaseGoodsEntity> queryPurchaseGoodsByUUID(String uuid);

    List<NxDistributerPurchaseGoodsEntity> queryPurchaseGoodsByGoodsId(Map<String, Object> map);

    List<NxDistributerPurchaseGoodsEntity> queryPurUserPurchaseGoods(Integer purUserId);

    List<NxDistributerPurchaseGoodsEntity> queryPurchaseGoodsByBatchId(Integer purchaseBatchId);

    List<NxDistributerPurchaseGoodsEntity> queryForDisGoods(Map<String, Object> map2);

    List<NxDistributerPurchaseGoodsEntity> queryPurchaseGoodsByParams(Map<String, Object> map2);

    int queryPurchaseGoodsCount(Map<String, Object> map2);

    List<NxDistributerPurchaseGoodsEntity> queryPurchaseGoodsWithDetailByParams(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryDisAutoPurchaseGoods(Map<String, Object> map4);

    Double queryPurchaseGoodsSubTotal(Map<String, Object> map);

    Integer queryPurOrderCount(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryDisPurchaseGoodsGreat(Map<String, Object> map4);

    NxDistributerPurchaseGoodsEntity queryIfHavePurGoods(Map<String, Object> map);
}
