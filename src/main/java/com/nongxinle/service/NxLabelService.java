package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 04-27 12:56
 */

import com.nongxinle.entity.NxLabelEntity;

import java.util.List;
import java.util.Map;

public interface NxLabelService {
	
	NxLabelEntity queryObject(Integer nxLabelId);
	
	List<NxLabelEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxLabelEntity nxLabel);
	
	void update(NxLabelEntity nxLabel);
	
	void delete(Integer nxLabelId);
	
	void deleteBatch(Integer[] nxLabelIds);
}
