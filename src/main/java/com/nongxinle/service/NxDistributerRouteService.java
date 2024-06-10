package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-14 20:17
 */

import com.nongxinle.entity.NxDistributerRouteEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerRouteService {
	
	NxDistributerRouteEntity queryObject(Integer nxDistributerRouteId);
	
	List<NxDistributerRouteEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerRouteEntity nxDistributerRoute);
	
	void update(NxDistributerRouteEntity nxDistributerRoute);
	
	void delete(Integer nxDistributerRouteId);
	
	void deleteBatch(Integer[] nxDistributerRouteIds);
}
