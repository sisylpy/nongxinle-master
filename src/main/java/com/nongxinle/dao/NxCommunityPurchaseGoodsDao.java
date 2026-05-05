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

	NxCommunityPurchaseGoodsEntity queryPurchaseGoodsByStatus(Map<String, Object> mapPur);

	/**
	 * 查询社区父商品分类（简化版，含订单数量统计）
	 */
	List<NxCommunityFatherGoodsEntity> queryCommFatherGoodsSimple(Map<String, Object> map);

	/**
	 * 查询社区采购商品总数（用于分页）
	 */
	int queryCommPurchaseGoodsCount(Map<String, Object> map);

	/**
	 * 查询社区采购商品列表（简化版）
	 */
	List<NxCommunityPurchaseGoodsEntity> queryCommPurchaseGoodsSimple(Map<String, Object> map);
}
