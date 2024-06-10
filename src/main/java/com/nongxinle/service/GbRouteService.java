package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-18 21:32
 */

import com.nongxinle.entity.GbRouteEntity;

import java.util.List;
import java.util.Map;

public interface GbRouteService {
	
	GbRouteEntity queryObject(Integer gbRouteId);
	
	List<GbRouteEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbRouteEntity gbRoute);
	
	void update(GbRouteEntity gbRoute);
	
	void delete(Integer gbRouteId);
	
	void deleteBatch(Integer[] gbRouteIds);

    List<GbRouteEntity> getDisRoutesByDisId(Map<String, Object> map);
}
