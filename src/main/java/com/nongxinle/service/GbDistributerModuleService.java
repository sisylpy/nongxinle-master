package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-04 11:07
 */

import com.nongxinle.entity.GbDistributerModuleEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerModuleService {
	
	GbDistributerModuleEntity queryObject(Integer gbDistributerModuleId);
	
	List<GbDistributerModuleEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerModuleEntity gbDistributerModule);
	
	void update(GbDistributerModuleEntity gbDistributerModule);
	
	void delete(Integer gbDistributerModuleId);
	
	void deleteBatch(Integer[] gbDistributerModuleIds);
}
