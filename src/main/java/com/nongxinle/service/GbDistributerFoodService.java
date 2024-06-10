package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 03-26 18:31
 */

import com.nongxinle.entity.GbDistributerFoodEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerFoodService {
	
	GbDistributerFoodEntity queryObject(Integer gbDistributerFoodId);
	
	List<GbDistributerFoodEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerFoodEntity gbDistributerFood);
	
	void update(GbDistributerFoodEntity gbDistributerFood);
	
	void delete(Integer gbDistributerFoodId);
	
	void deleteBatch(Integer[] gbDistributerFoodIds);

    List<GbDistributerFoodEntity> queryFoodByParams(Map<String, Object> map);

    List<GbDistributerFoodEntity> queryDisAllFood(Map<String, Object> map);
}
