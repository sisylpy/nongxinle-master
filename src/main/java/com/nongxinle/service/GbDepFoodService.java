package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 03-27 21:55
 */

import com.nongxinle.entity.GbDepFoodEntity;

import java.util.List;
import java.util.Map;

public interface GbDepFoodService {
	
	GbDepFoodEntity queryObject(Integer gbDepFoodId);
	
	List<GbDepFoodEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepFoodEntity gbDepFood);
	
	void update(GbDepFoodEntity gbDepFood);
	
	void delete(Integer gbDepFoodId);
	
	void deleteBatch(Integer[] gbDepFoodIds);

	List<GbDepFoodEntity> queryDepFoodByParams(Map<String, Object> map);

	List<GbDepFoodEntity> queryDepFoodByParamsWithoutFather(Map<String, Object> map);
}
