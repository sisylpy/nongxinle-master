package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 11-30 10:09
 */

import com.nongxinle.entity.GbDepartmentDisGoodsEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsShelfEntity;
import com.nongxinle.entity.GbDistributerWeightGoodsEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerWeightGoodsService {
	
	GbDistributerWeightGoodsEntity queryObject(Integer gbDistributerWeightGoodsId);
	
	List<GbDistributerWeightGoodsEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerWeightGoodsEntity gbDistributerWeightGoods);
	
	void update(GbDistributerWeightGoodsEntity gbDistributerWeightGoods);
	
	void delete(Integer gbDistributerWeightGoodsId);
	
	void deleteBatch(Integer[] gbDistributerWeightGoodsIds);

    List<GbDistributerWeightGoodsEntity> queryWeightGoodsByParams(Map<String, Object> map);

    List<GbDistributerGoodsShelfEntity> queryShelfGoodsToWeightByParams(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryFatherGoodsToWeightByParams(Map<String, Object> map);

    List<GbDistributerWeightGoodsEntity> queryWeightGoodsWithOrderByParams(Map<String, Object> map);

    int queryWeightGoodsAccount(Map<String, Object> map33);
}
