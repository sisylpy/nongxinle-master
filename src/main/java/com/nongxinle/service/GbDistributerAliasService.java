package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-07 09:12
 */

import com.nongxinle.entity.GbDistributerAliasEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerAliasService {
	
	GbDistributerAliasEntity queryObject(Integer gbDistributerAliasId);
	
	List<GbDistributerAliasEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerAliasEntity gbDistributerAlias);
	
	void update(GbDistributerAliasEntity gbDistributerAlias);
	
	void delete(Integer gbDistributerAliasId);
	
	void deleteBatch(Integer[] gbDistributerAliasIds);
}
