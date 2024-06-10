package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 08-11 22:02
 */

import com.nongxinle.entity.NxDepartmentStandardEntity;

import java.util.List;
import java.util.Map;

public interface NxDepartmentStandardService {
	
	NxDepartmentStandardEntity queryObject(Integer nxDepartmentStandardId);
	
//	List<NxDepartmentStandardEntity> queryList(Map<String, Object> map);
	
//	int queryTotal(Map<String, Object> map);
	
	void save(NxDepartmentStandardEntity nxDepartmentStandard);
	
	void update(NxDepartmentStandardEntity nxDepartmentStandard);
	
	void delete(Integer nxDepartmentStandardId);
	
//	void deleteBatch(Integer[] nxDepartmentStandardIds);

    List<NxDepartmentStandardEntity> queryDepGoodsStandards(Integer depGoodsId);
}
