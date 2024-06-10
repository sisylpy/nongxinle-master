package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 12-02 20:50
 */

import com.nongxinle.entity.NxCommunityFatherGoodsEntity;
import com.nongxinle.entity.NxCommunityPurchaseGoodsEntity;
import com.nongxinle.entity.NxDistributerPurchaseGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityPurchaseGoodsDao extends BaseDao<NxCommunityPurchaseGoodsEntity> {

    List<NxCommunityPurchaseGoodsEntity> queryPurchaseForComGoods(Map<String, Object> map2);

    List<NxCommunityFatherGoodsEntity> queryResOrdersByComPurchaseGoods(Map<String, Object> map2);

    List<NxCommunityFatherGoodsEntity> queryComPurchaseGoods(Map<String, Object> map);

    List<NxCommunityPurchaseGoodsEntity> queryPurchaseGoodsByBathcId(Integer batchId);
}
