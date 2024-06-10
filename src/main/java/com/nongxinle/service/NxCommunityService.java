package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 2020-03-04 17:57:31
 */

import com.nongxinle.entity.NxCommunityEntity;

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

    List<NxCommunityEntity> queryDistributerCommunityList(Integer disId);
}
