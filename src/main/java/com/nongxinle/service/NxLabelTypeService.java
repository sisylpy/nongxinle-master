package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 04-27 12:56
 */

import com.nongxinle.entity.NxLabelEntity;
import com.nongxinle.entity.NxLabelTypeEntity;

import java.util.List;
import java.util.Map;

public interface NxLabelTypeService {
	
	NxLabelTypeEntity queryObject(Integer nxLabelTypeId);
	
	List<NxLabelTypeEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxLabelTypeEntity nxLabelType);
	
	void update(NxLabelTypeEntity nxLabelType);
	
	void delete(Integer nxLabelTypeId);
	
	void deleteBatch(Integer[] nxLabelTypeIds);

    List<NxLabelEntity> queryAllLabel();
}
