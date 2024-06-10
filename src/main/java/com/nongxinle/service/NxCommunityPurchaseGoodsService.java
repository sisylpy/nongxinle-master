package com.nongxinle.service;

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

public interface NxCommunityPurchaseGoodsService {
	
	NxCommunityPurchaseGoodsEntity queryObject(Integer nxCommunityPurchaseGoodsId);
	
	List<NxCommunityPurchaseGoodsEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityPurchaseGoodsEntity nxCommunityPurchaseGoods);
	
	void update(NxCommunityPurchaseGoodsEntity nxCommunityPurchaseGoods);
	
	void delete(Integer nxCommunityPurchaseGoodsId);
	
	void deleteBatch(Integer[] nxCommunityPurchaseGoodsIds);

    List<NxCommunityPurchaseGoodsEntity> queryPurchaseForComGoods(Map<String, Object> map2);

    List<NxCommunityFatherGoodsEntity> queryResOrdersByComPurchaseGoods(Map<String, Object> map2);

	List<NxCommunityFatherGoodsEntity> queryComPurchaseGoods(Map<String, Object> map);

    List<NxCommunityPurchaseGoodsEntity> queryPurchaseGoodsByBathcId(Integer batchId);
}
