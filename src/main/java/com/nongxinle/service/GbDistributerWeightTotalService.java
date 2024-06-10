package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 11-29 07:10
 */

import com.nongxinle.entity.GbDistributerWeightTotalEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerWeightTotalService {
	
	GbDistributerWeightTotalEntity queryObject(Integer gbDistributerWeightTotalId);
	
	List<GbDistributerWeightTotalEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerWeightTotalEntity gbDistributerWeightTotal);
	
	void update(GbDistributerWeightTotalEntity gbDistributerWeightTotal);
	
	void delete(Integer gbDistributerWeightTotalId);
	
	void deleteBatch(Integer[] gbDistributerWeightTotalIds);

    List<GbDistributerWeightTotalEntity> queryDepWeightListByParams(Map<String, Object> map);

	int queryDepWeightCountByParams(Map<String, Object> map3);
}
