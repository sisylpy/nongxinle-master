package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-15 18:45
 */

import com.nongxinle.entity.NxCommunitySupplierEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunitySupplierService {
	
	NxCommunitySupplierEntity queryObject(Integer nxCommunitySupplierId);
	
	List<NxCommunitySupplierEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunitySupplierEntity nxCommunitySupplier);
	
	void update(NxCommunitySupplierEntity nxCommunitySupplier);
	
	void delete(Integer nxCommunitySupplierId);
	
	void deleteBatch(Integer[] nxCommunitySupplierIds);

    List<NxCommunitySupplierEntity> queryCommunitySupplierByParams(Map<String, Object> map);
}
