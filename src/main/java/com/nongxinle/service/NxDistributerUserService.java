package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-20 10:21
 */

import com.nongxinle.entity.NxDepartmentUserEntity;
import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.entity.NxDistributerUserEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerUserService {

	List<NxDistributerUserEntity> getAllUserByDisId(Integer disId);

	NxDistributerUserEntity queryObject(Integer nxDistributerUserId);
	
	List<NxDistributerUserEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerUserEntity nxDistributerUser);
	
	void update(NxDistributerUserEntity nxDistributerUser);
	
	void delete(Integer nxDistributerUserId);
	
	void deleteBatch(Integer[] nxDistributerUserIds);

    List<NxDistributerUserEntity> queryUser(Integer disId);

	NxDistributerUserEntity queryUserInfo(Integer nxDistributerUserId);

    NxDistributerUserEntity queryUserByOpenId(String openid);

	Map<String, Object> queryNxDisAndUserInfo(Integer userId);

    List<NxDistributerUserEntity> getAllDisUsers();

    List<NxDistributerUserEntity> queryRoleNxDisRoleUserList(Map<String, Object> map);

    NxDistributerUserEntity queryDisUserByRoleAndOpen(Map<String, Object> map1);

	List<NxDistributerUserEntity> getAdminUserByParams(Map<String, Object> map);
}
