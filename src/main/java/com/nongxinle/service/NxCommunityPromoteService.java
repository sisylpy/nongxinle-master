package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-25 08:54
 */

import com.nongxinle.entity.NxCommunityPromoteEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityPromoteService {
	
	NxCommunityPromoteEntity queryObject(Integer nxPromoteId);
	
	List<NxCommunityPromoteEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityPromoteEntity nxPromote);
	
	void update(NxCommunityPromoteEntity nxPromote);
	
	void delete(Integer nxPromoteId);
	
	void deleteBatch(Integer[] nxPromoteIds);

    List<NxCommunityPromoteEntity> getListByCommunityId(Integer communityId);

    List<NxCommunityPromoteEntity> queryPromoteByFatherId(Integer nxCommunityFatherGoodsId);
}
