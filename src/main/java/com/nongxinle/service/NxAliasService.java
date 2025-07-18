package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 07-30 18:51
 */

import com.nongxinle.entity.NxAliasEntity;
import com.nongxinle.entity.NxGoodsEntity;

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

    List<NxAliasEntity> queryNxAliasList(Map<String, Object> map);

    List<NxGoodsEntity> queryNxGoodsByName(Map<String, Object> map);
}
