package com.nongxinle.service;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 06-20 10:55
 */

import com.nongxinle.entity.NxDistributerUserRoleEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerUserRoleService {
	
	NxDistributerUserRoleEntity queryObject(Integer nxDistributerUserRoleId);
	
	List<NxDistributerUserRoleEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerUserRoleEntity nxDistributerUserRole);
	
	void update(NxDistributerUserRoleEntity nxDistributerUserRole);
	
	void delete(Integer nxDistributerUserRoleId);
	
	void deleteBatch(Integer[] nxDistributerUserRoleIds);
}
