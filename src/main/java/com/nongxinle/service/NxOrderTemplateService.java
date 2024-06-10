package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 04-14 12:45
 */

import com.nongxinle.entity.NxOrderTemplateEntity;

import java.util.List;
import java.util.Map;

public interface NxOrderTemplateService {
	
	NxOrderTemplateEntity queryObject(Integer nxOrderTemplateId);
	
	List<NxOrderTemplateEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxOrderTemplateEntity nxOderTemplate);
	
	void update(NxOrderTemplateEntity nxOderTemplate);
	
	void delete(Integer nxOrderTemplateId);
	
	void deleteBatch(Integer[] nxOrderTemplateIds);

    NxOrderTemplateEntity queryTemplate(Map<String, Object> map);
}
