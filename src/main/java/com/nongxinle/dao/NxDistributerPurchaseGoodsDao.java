package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-24 11:45
 */

import com.nongxinle.entity.NxDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxDistributerPurchaseGoodsEntity;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.entity.NxJrdhUserEntity;
import com.nongxinle.entity.PurchaseGoodsSimpleDTO;

import java.util.List;
import java.util.Map;


public interface NxDistributerPurchaseGoodsDao extends BaseDao<NxDistributerPurchaseGoodsEntity> {

    List<NxDistributerFatherGoodsEntity> queryDisPurchaseGoods(Map<String, Object> map);

//    /////////////////

//    List<NxDistributerPurchaseGoodsEntity> queryPurchaseGoodsByUUID(String uuid);

    List<NxDistributerPurchaseGoodsEntity> queryPurchaseGoodsWithOrders(Map<String, Object> map);

    List<NxDistributerPurchaseGoodsEntity> queryPurUserPurchaseGoods(Integer purUserId);

    List<NxDistributerPurchaseGoodsEntity> queryPurchaseGoodsByBatchId(Integer purchaseBatchId);

//    List<NxDistributerPurchaseGoodsEntity> queryForDisGoods(Map<String, Object> map2);

    List<NxDistributerPurchaseGoodsEntity> queryPurchaseGoodsByParams(Map<String, Object> map2);

    int queryPurchaseGoodsCount(Map<String, Object> map2);

    List<NxDistributerPurchaseGoodsEntity> queryPurchaseGoodsWithDetailByParams(Map<String, Object> map);

//    List<NxDistributerFatherGoodsEntity> queryDisAutoPurchaseGoods(Map<String, Object> map4);

    Double queryPurchaseGoodsSubTotal(Map<String, Object> map);

    Integer queryPurOrderCount(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryDisPurchaseGoodsGreat(Map<String, Object> map4);

    NxDistributerPurchaseGoodsEntity queryIfHavePurGoods(Map<String, Object> map);

    // NX统计接口新增方法
    Integer queryDistinctGoodsCount(Map<String, Object> map);
    
    Integer queryGoodsListCount(Map<String, Object> map);
    
    List<NxDistributerGoodsEntity> queryGoodsListWithPurchase(Map<String, Object> map);
    
    List<NxDistributerPurchaseGoodsEntity> queryPurchaseGoodsByDisGoodsIdAndDate(Map<String, Object> map);

    List<NxDistributerUserEntity> queryPurUserList(Map<String, Object> map);

    List<NxJrdhUserEntity> querySupplierList(Map<String, Object> map);

    // 统计TOP方法
    List<NxDistributerGoodsEntity> queryNxPurchaseGoodsTopTimes(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryNxPurchaseGoodsTopSubtotal(Map<String, Object> map);

    Double queryNxPurchaseSubtotalTopSubtotal(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryNxPurchaseGoodsTopPriceFluctuation(Map<String, Object> map);

    /**
     * 查询采购商品列表（超简化版，减少数据传输量）
     */
    List<PurchaseGoodsSimpleDTO> queryPurchaseGoodsWithOrdersUltraSimple(Map<String, Object> map);
}
