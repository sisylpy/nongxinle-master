package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 04-14 12:45
 */

import com.nongxinle.entity.NxOrderTemplateItemEntity;

import java.util.List;
import java.util.Map;

public interface NxOrderTemplateItemService {
	
	NxOrderTemplateItemEntity queryObject(Integer nxOtItemId);
	
	List<NxOrderTemplateItemEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxOrderTemplateItemEntity nxOrderTemplateItem);
	
	void update(NxOrderTemplateItemEntity nxOrderTemplateItem);
	
	void delete(Integer nxOtItemId);
	
	void deleteBatch(Integer[] nxOtItemIds);

    List<NxOrderTemplateItemEntity> queryUserItem(Map<String, Object> map1);

    List<NxOrderTemplateItemEntity> queryCustomerUserItems(Integer nxCustomerUserId);
}
