package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 07-30 18:51
 */

import com.nongxinle.entity.NxAliasEntity;

import java.util.List;
import java.util.Map;

public interface NxAliasService {
	
	NxAliasEntity queryObject(Integer nxAliasId);
	
	List<NxAliasEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxAliasEntity nxAlias);
	
	void update(NxAliasEntity nxAlias);
	
	void delete(Integer nxAliasId);
	
	void deleteBatch(Integer[] nxAliasIds);
}
