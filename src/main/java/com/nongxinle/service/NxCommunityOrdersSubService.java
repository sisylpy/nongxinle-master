package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 2020-03-22 18:07:28
 */

import com.nongxinle.entity.NxCommunityOrdersEntity;
import com.nongxinle.entity.NxCommunityOrdersSubEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityOrdersSubService {
	
	NxCommunityOrdersSubEntity queryObject(Integer nxOrdersSubId);
	
	List<NxCommunityOrdersSubEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityOrdersSubEntity nxOrdersSub);
	
	void update(NxCommunityOrdersSubEntity nxOrdersSub);
	
	void delete(Integer nxOrdersSubId);
	
	void deleteBatch(Integer[] nxOrdersSubIds);

	List<NxCommunityOrdersSubEntity> querySubOrdersByCustomerUserId(Map<String, Object> map);

    List<NxCommunityOrdersEntity> queryOutGoodsByType(Map<String, Object> map);

    List<NxCommunityOrdersSubEntity> querySubOrdersByParams(Map<String, Object> map);

}
