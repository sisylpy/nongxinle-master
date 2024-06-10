package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-20 21:43
 */

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbRouteDepEntity;

import java.util.List;
import java.util.Map;

public interface GbRouteDepService {
	
	GbRouteDepEntity queryObject(Integer gbRouteDepId);
	
	List<GbRouteDepEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbRouteDepEntity gbRouteDep);
	
	void update(GbRouteDepEntity gbRouteDep);
	
	void delete(Integer gbRouteDepId);
	
	void deleteBatch(Integer[] gbRouteDepIds);

    List<GbDepartmentEntity> queryHaveLineDepsByDisId(Integer disId);
}
