package com.nongxinle.service;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-09 18:47
 */

import com.nongxinle.entity.NxDistributerGoodsShelfGoodsEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerGoodsShelfGoodsService {
	
	NxDistributerGoodsShelfGoodsEntity queryObject(Integer nxDistributerGoodsShelfGoodsId);
	
	List<NxDistributerGoodsShelfGoodsEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerGoodsShelfGoodsEntity nxDistributerGoodsShelfGoods);
	
	void update(NxDistributerGoodsShelfGoodsEntity nxDistributerGoodsShelfGoods);
	
	void delete(Integer nxDistributerGoodsShelfGoodsId);
	
	void deleteBatch(Integer[] nxDistributerGoodsShelfGoodsIds);

    List<NxDistributerGoodsShelfGoodsEntity> queryShelfForGoodsByParams(Map<String, Object> map);

    int queryShelfForGoodsCount(Map<String, Object> map);

    int queryShelfGoodsCount(Map<String, Object> map);

    List<NxDistributerGoodsShelfGoodsEntity> queryShelfForGoodsWithOrders(Map<String, Object> pageParams);

	NxDistributerGoodsShelfGoodsEntity queryShlefGoodsByGoodsId(Integer nxDpgDisGoodsId);

	NxDistributerGoodsShelfGoodsEntity queryShlefGoodsByGoodsIdAndShelfId(Integer disGoodsId, Integer shelfId);

	List<NxDistributerGoodsShelfGoodsEntity> queryShelfGoodsBasic(Integer shelfId);

	void updateShelfLayer(Integer id, Integer layer);

	/**
	 * 更新商品在所有货架的重复标记
	 * 如果该商品出现在2个或以上的不同货架，则标记为1（重复），否则标记为0（未重复）
	 * @param disGoodsId 配送商商品ID
	 */
	void updateDuplicateFlagForGoods(Integer disGoodsId);

	// 盘库相关方法
	List<NxDistributerGoodsShelfGoodsEntity> queryUnInventoriedShelfGoods(Map<String, Object> map);

	Integer queryUnInventoriedShelfGoodsCount(Map<String, Object> map);

}
