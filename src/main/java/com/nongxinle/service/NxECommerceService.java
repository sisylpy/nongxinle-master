package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 11-28 21:17
 */

import com.nongxinle.entity.NxECommerceEntity;

import java.util.List;
import java.util.Map;

public interface NxECommerceService {
	
	NxECommerceEntity queryObject(Integer nxECommerceId);
	
	List<NxECommerceEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxECommerceEntity nxECommerce);
	
	void update(NxECommerceEntity nxECommerce);
	
	void delete(Integer nxECommerceId);
	
	void deleteBatch(Integer[] nxECommerceIds);
}
