package com.nongxinle.service;

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

public interface NxDistributerPurchaseGoodsService {


	List<NxDistributerFatherGoodsEntity> queryDisPurchaseGoods(Map<String, Object> map);


//	//////////////////

	List<NxDistributerPurchaseGoodsEntity> purUserGetPurchaseGoods(Integer purUserId);


	NxDistributerPurchaseGoodsEntity queryObject(Integer nxDistributerPurchaseGoods);
	
	List<NxDistributerPurchaseGoodsEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerPurchaseGoodsEntity nxDistributerPurchaseGoods);
	
	void update(NxDistributerPurchaseGoodsEntity nxDistributerPurchaseGoods);
	
	void delete(Integer nxDistributerPurchaseGoods);
	
	void deleteBatch(Integer[] nxDistributerPurchaseGoodss);


//	List<NxDistributerPurchaseGoodsEntity> queryPurchaseGoodsByUUID(String uuid);

	List<NxDistributerPurchaseGoodsEntity> queryPurchaseGoodsByGoodsId(Map<String, Object> map);


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
