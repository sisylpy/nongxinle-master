package com.nongxinle.service;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 08-19 09:57
 */

import com.nongxinle.entity.GbDistributerGoodsShelfGoodsEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerGoodsShelfGoodsService {
	
	GbDistributerGoodsShelfGoodsEntity queryObject(Integer gbDistributerGoodsShelfGoodsId);
	
	List<GbDistributerGoodsShelfGoodsEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerGoodsShelfGoodsEntity gbDistributerGoodsShelfGoods);
	
	void update(GbDistributerGoodsShelfGoodsEntity gbDistributerGoodsShelfGoods);
	
	void delete(Integer gbDistributerGoodsShelfGoodsId);
	
	void deleteBatch(Integer[] gbDistributerGoodsShelfGoodsIds);

    List<GbDistributerGoodsShelfGoodsEntity> queryShelfGoodsByParams(Map<String, Object> map);
}
