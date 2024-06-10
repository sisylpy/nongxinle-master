package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-22 20:41
 */

import com.nongxinle.entity.NxRetailerPurchaseGoodsEntity;

import java.util.List;
import java.util.Map;

public interface NxRetailerPurchaseGoodsService {
	
	NxRetailerPurchaseGoodsEntity queryObject(Integer nxRetailerPurchaseGoodsId);
	
	List<NxRetailerPurchaseGoodsEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxRetailerPurchaseGoodsEntity nxRetailerPurchaseGoods);
	
	void update(NxRetailerPurchaseGoodsEntity nxRetailerPurchaseGoods);
	
	void delete(Integer nxRetailerPurchaseGoodsId);
	
	void deleteBatch(Integer[] nxRetailerPurchaseGoodsIds);

    List<NxRetailerPurchaseGoodsEntity> queryRetShelfPurGoodsByParame(Map<String, Object> map);
}
