package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-26 16:23
 */

import com.nongxinle.entity.NxCommunityAdsenseEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityAdsenseService {
	
	NxCommunityAdsenseEntity queryObject(Integer nxAdsenseId);
	
	List<NxCommunityAdsenseEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityAdsenseEntity nxAdsense);
	
	void update(NxCommunityAdsenseEntity nxAdsense);
	
	void delete(Integer nxAdsenseId);
	
	void deleteBatch(Integer[] nxAdsenseIds);

    List<NxCommunityAdsenseEntity> getListByCommunityId(Integer communityId);

	List<NxCommunityAdsenseEntity> queryAdsenseByNxCommunityId(Integer communityId);
}
