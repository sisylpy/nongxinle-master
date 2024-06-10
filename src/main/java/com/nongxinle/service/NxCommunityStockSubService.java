package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-08 09:22
 */

import com.nongxinle.entity.NxCommunityStockSubEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityStockSubService {
	
	NxCommunityStockSubEntity queryObject(Integer nxCommunitySubStockId);
	
	List<NxCommunityStockSubEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityStockSubEntity nxCommunityStockSub);
	
	void update(NxCommunityStockSubEntity nxCommunityStockSub);
	
	void delete(Integer nxCommunitySubStockId);
	
	void deleteBatch(Integer[] nxCommunitySubStockIds);
}
