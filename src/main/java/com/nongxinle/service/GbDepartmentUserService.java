package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-16 11:26
 */

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbDepartmentUserEntity;

import java.util.List;
import java.util.Map;

public interface GbDepartmentUserService {
	
	GbDepartmentUserEntity queryObject(Integer nxDepartmentUserId);
	
//	List<GbDepartmentUserEntity> queryList(Map<String, Object> map);
	
//	int queryTotal(Map<String, Object> map);
	
	void save(GbDepartmentUserEntity nxDepartmentUser);
	
	void update(GbDepartmentUserEntity nxDepartmentUser);
	
	void delete(Integer nxDepartmentUserId);
	
//	void deleteBatch(Integer[] nxDepartmentUserIds);

    List<GbDepartmentUserEntity> queryAllUsersByDepId(Integer depId);

    GbDepartmentUserEntity queryDepUserByOpenId(String openId);

    List<GbDepartmentUserEntity> queryGroupAdminUserAmount(Integer nxDuDepartmentId);

    List<GbDepartmentEntity> queryMultiDepartmentByWxOpenIdGb(String openId);

    List<GbDepartmentUserEntity> queryDepUsersByDepId(Integer depId);


    GbDepartmentUserEntity queryGroupDepartmentUserByParams(Map<String, Object> map);

    List<GbDepartmentUserEntity> queryDepUsersByDepIdAndAdmin(Map<String, Object> map);

    GbDepartmentUserEntity queryDepUserInfoGb(Integer userId);

}
