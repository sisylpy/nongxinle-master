package com.nongxinle.community.core.service;

/**
 * 
 *
 * @author lpy
 * @date 2020-03-04 17:57:31
 */

import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.entity.NxECommerceCommunityEntity;
import com.nongxinle.entity.NxECommerceEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityService {
	
	NxCommunityEntity queryObject(Integer nxCommunityId);
	
	List<NxCommunityEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityEntity nxCommunity);
	
	void update(NxCommunityEntity nxCommunity);
	
	void delete(Integer nxCommunityId);
	
	void deleteBatch(Integer[] nxCommunityIds);

	NxCommunityEntity saveWithEcommerce(NxCommunityEntity nxCommunity);

	NxECommerceEntity queryCommunityByECommerceId(Integer id);

    List<NxCommunityEntity> queryCommunityListByUserPoint(String nxCuaLat, String nxCuaLng, Integer commerceId);
}
