package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 08-19 12:35
 */

import com.nongxinle.entity.SysCityEntity;

import java.util.List;
import java.util.Map;

public interface SysCityService {
	
	SysCityEntity queryObject(Integer sysCityId);
	
	List<SysCityEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(SysCityEntity sysCity);
	
	void update(SysCityEntity sysCity);
	
	void delete(Integer sysCityId);
	
	void deleteBatch(Integer[] sysCityIds);
}
