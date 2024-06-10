package com.nongxinle.service;

/**
 *
 *
 * @author lpy
 * @date 06-16 11:26
 */

import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDepartmentUserEntity;

import java.util.List;
import java.util.Map;

public interface NxDepartmentUserService {
	
	NxDepartmentUserEntity queryObject(Integer nxDepartmentUserId);
	
//	List<NxDepartmentUserEntity> queryList(Map<String, Object> map);
	
//	int queryTotal(Map<String, Object> map);
	
	void save(NxDepartmentUserEntity nxDepartmentUser);
	
	void update(NxDepartmentUserEntity nxDepartmentUser);
	
	void delete(Integer nxDepartmentUserId);
	
//	void deleteBatch(Integer[] nxDepartmentUserIds);

    List<NxDepartmentUserEntity> queryAllUsersByDepId(Integer depId);

    NxDepartmentUserEntity queryDepUserByOpenId(String openId);

    List<NxDepartmentUserEntity> queryGroupAdminUserAmount(Integer nxDuDepartmentId);

    List<NxDepartmentEntity> queryMultiDepartmentByWxOpenId(String openId);

    List<NxDepartmentUserEntity> queryDepUsersByDepId(Integer depId);

    List<NxDepartmentUserEntity> queryAllDepUsers();

    List<NxDepartmentUserEntity> queryAllUsersByDepFatherId(Integer depId);
}
