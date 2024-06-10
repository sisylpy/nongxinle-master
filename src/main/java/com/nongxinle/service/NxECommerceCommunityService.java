package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 11-28 21:17
 */

import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.entity.NxECommerceCommunityEntity;

import java.util.List;
import java.util.Map;

public interface NxECommerceCommunityService {
	
	NxECommerceCommunityEntity queryObject(Integer nxEccId);

	List<NxECommerceCommunityEntity> queryList(Map<String, Object> map);

	int queryTotal(Map<String, Object> map);

	void save(NxECommerceCommunityEntity nxECommerceCommunity);

	void update(NxECommerceCommunityEntity nxECommerceCommunity);

	void delete(Integer nxEccId);

	void deleteBatch(Integer[] nxEccIds);

//	/////
    List<NxCommunityEntity> queryCommunityByCommerceId(Integer commerceId);
}
