package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-07 09:12
 */

import com.nongxinle.entity.NxDistributerAliasEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerAliasService {
	
	NxDistributerAliasEntity queryObject(Integer nxDistributerAliasId);
	
	List<NxDistributerAliasEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerAliasEntity nxDistributerAlias);
	
	void update(NxDistributerAliasEntity nxDistributerAlias);
	
	void delete(Integer nxDistributerAliasId);
	
	void deleteBatch(Integer[] nxDistributerAliasIds);
}
