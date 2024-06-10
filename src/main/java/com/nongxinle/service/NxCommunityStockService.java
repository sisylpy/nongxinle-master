package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-08 09:22
 */

import com.nongxinle.entity.NxCommunityStockEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityStockService {
	
	NxCommunityStockEntity queryObject(Integer nxCommunityStockId);
	
	List<NxCommunityStockEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityStockEntity nxCommunityStock);
	
	void update(NxCommunityStockEntity nxCommunityStock);
	
	void delete(Integer nxCommunityStockId);
	
	void deleteBatch(Integer[] nxCommunityStockIds);
}
