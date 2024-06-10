package com.nongxinle.service;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-22 15:25
 */

import com.nongxinle.entity.NxRetailerGoodsShelfGoodsEntity;

import java.util.List;
import java.util.Map;

public interface NxRetailerGoodsShelfGoodsService {
	
	NxRetailerGoodsShelfGoodsEntity queryObject(Integer nxRetailerGoodsShelfGoodsId);
	
	List<NxRetailerGoodsShelfGoodsEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxRetailerGoodsShelfGoodsEntity nxRetailerGoodsShelfGoods);
	
	void update(NxRetailerGoodsShelfGoodsEntity nxRetailerGoodsShelfGoods);
	
	void delete(Integer nxRetailerGoodsShelfGoodsId);
	
	void deleteBatch(Integer[] nxRetailerGoodsShelfGoodsIds);

    List<NxRetailerGoodsShelfGoodsEntity> queryRetShelfGoodsByParams(Map<String, Object> map);

    List<NxRetailerGoodsShelfGoodsEntity> queryRetShelfGoodsWithPurchaseByParams(Map<String, Object> map);
}
