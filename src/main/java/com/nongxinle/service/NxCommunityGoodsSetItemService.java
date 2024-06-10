package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 04-06 00:18
 */

import com.nongxinle.entity.NxCommunityGoodsSetItemEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityGoodsSetItemService {
	
	NxCommunityGoodsSetItemEntity queryObject(Integer nxCommunityGoodsSetItemId);
	
	List<NxCommunityGoodsSetItemEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityGoodsSetItemEntity nxCommunityGoodsSetItem);
	
	void update(NxCommunityGoodsSetItemEntity nxCommunityGoodsSetItem);
	
	void delete(Integer nxCommunityGoodsSetItemId);
	
	void deleteBatch(Integer[] nxCommunityGoodsSetItemIds);

	List<NxCommunityGoodsSetItemEntity> queryCgGoodsSetListByParams(Map<String, Object> map);
}
