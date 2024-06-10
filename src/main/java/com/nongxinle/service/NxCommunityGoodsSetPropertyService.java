package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 04-06 00:18
 */

import com.nongxinle.entity.NxCommunityGoodsSetPropertyEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityGoodsSetPropertyService {
	
	NxCommunityGoodsSetPropertyEntity queryObject(Integer nxCommunityGoodsSetPropertyId);
	
	List<NxCommunityGoodsSetPropertyEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityGoodsSetPropertyEntity nxCommunityGoodsSetProperty);
	
	void update(NxCommunityGoodsSetPropertyEntity nxCommunityGoodsSetProperty);
	
	void delete(Integer nxCommunityGoodsSetPropertyId);
	
	void deleteBatch(Integer[] nxCommunityGoodsSetPropertyIds);

    List<NxCommunityGoodsSetPropertyEntity> queryCgGoodsPropertyListByParams(Map<String, Object> mapP);
}
