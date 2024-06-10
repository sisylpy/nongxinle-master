package com.nongxinle.service;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 08-19 09:57
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;

public interface GbDistributerGoodsShelfService {
	
	GbDistributerGoodsShelfEntity queryObject(Integer gbDistributerGoodsShelfId);
	
	List<GbDistributerGoodsShelfEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerGoodsShelfEntity gbDistributerGoodsShelf);
	
	void update(GbDistributerGoodsShelfEntity gbDistributerGoodsShelf);
	
	void delete(Integer gbDistributerGoodsShelfId);
	
	void deleteBatch(Integer[] gbDistributerGoodsShelfIds);

	List<GbDistributerFatherGoodsEntity> queryShelfByParamsWithStock(Map<String, Object> map);

	GbDistributerGoodsShelfEntity queryShelfGoodsByParams(Map<String, Object> map);

	List<GbDistributerFatherGoodsEntity> stockRoomGetShelfGoodsGb(Map<String, Object> map);

	List<GbDistributerGoodsEntity>  queryShelfInventoryGoodsByParams(Map<String, Object> map);

	List<GbDistributerGoodsShelfEntity> queryStockOrdersByParams(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryShelfInventoryDepGoodsByParams(Map<String, Object> map);

    List<GbDistributerGoodsShelfEntity> queryShelfList(Map<String, Object> map);

    List<GbDistributerGoodsShelfEntity> queryShelfGoodsWithPurOrder(Map<String, Object> map);

}
