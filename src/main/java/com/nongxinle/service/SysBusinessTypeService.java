package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 08-19 12:35
 */

import com.nongxinle.entity.SysBusinessTypeEntity;

import java.util.List;
import java.util.Map;

public interface SysBusinessTypeService {
	
	SysBusinessTypeEntity queryObject(Integer sysBusinessTypeId);
	
	List<SysBusinessTypeEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(SysBusinessTypeEntity sysBusinessType);
	
	void update(SysBusinessTypeEntity sysBusinessType);
	
	void delete(Integer sysBusinessTypeId);
	
	void deleteBatch(Integer[] sysBusinessTypeIds);

    List<SysBusinessTypeEntity> queryTypeNxDistribterWithPeisong(Map<String, Object> map);
}
