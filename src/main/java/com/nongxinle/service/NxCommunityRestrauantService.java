package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 11-30 15:31
 */

import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.entity.NxCommunityRestrauantEntity;
import com.nongxinle.entity.NxRestrauntEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityRestrauantService {
	
	NxCommunityRestrauantEntity queryObject(Integer nxCommunityRestaruantId);
	
	List<NxCommunityRestrauantEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityRestrauantEntity nxCommunityRestrauant);
	
	void update(NxCommunityRestrauantEntity nxCommunityRestrauant);
	
	void delete(Integer nxCommunityRestaruantId);
	
	void deleteBatch(Integer[] nxCommunityRestaruantIds);

    List<NxRestrauntEntity> queryRestrauntsByComId(Integer comId);

    NxCommunityEntity queryCommunityByResId(Integer resId);

    List<NxRestrauntEntity> queryRestrauntsByParams(Map<String, Object> map);
}
