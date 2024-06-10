package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 04-30 06:45
 */

import com.nongxinle.entity.NxCommunityStandardEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityStandardService {
	
	NxCommunityStandardEntity queryObject(Integer nxCommunityStandardId);
	
	List<NxCommunityStandardEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityStandardEntity nxCommunityStandard);
	
	void update(NxCommunityStandardEntity nxCommunityStandard);
	
	void delete(Integer nxCommunityStandardId);
	
	void deleteBatch(Integer[] nxCommunityStandardIds);

    List<NxCommunityStandardEntity> queryComGoodsStandards(Integer comGoodsId);
}
