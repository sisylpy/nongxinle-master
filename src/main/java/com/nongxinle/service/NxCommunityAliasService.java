package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 12-05 18:56
 */

import com.nongxinle.entity.NxCommunityAliasEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityAliasService {
	
	NxCommunityAliasEntity queryObject(Integer nxCommunityAliasId);
	
	List<NxCommunityAliasEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityAliasEntity nxCommunityAlias);
	
	void update(NxCommunityAliasEntity nxCommunityAlias);
	
	void delete(Integer nxCommunityAliasId);
	
	void deleteBatch(Integer[] nxCommunityAliasIds);

	List<NxCommunityAliasEntity> queryComAliasByComGoodsId(Integer comGoodsId);
}
