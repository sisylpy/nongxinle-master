package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 2020-03-04 19:11:55
 */

import com.nongxinle.entity.NxCustomerUserEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerUserService {
	
	NxCustomerUserEntity queryObject(Integer custUserId);
	
	List<NxCustomerUserEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCustomerUserEntity nxCustomerUser);
	
	void update(NxCustomerUserEntity nxCustomerUser);
	
	void delete(Integer custUserId);
	
	void deleteBatch(Integer[] custUserIds);

	NxCustomerUserEntity queryUserByOpenId(String openid);

	Map<String, Object> queryCustomerUserInfo(Integer gbDepartmentUserId);

    List<NxCustomerUserEntity> queryCustomerByParams(Map<String, Object> map);

    Integer queryCustomerUserCount(Map<String, Object> map);

    Integer queryCommerceCustomerUserCount(Map<String, Object> map);

	NxCustomerUserEntity queryUserWithAddress(Integer orderUserId);

    NxCustomerUserEntity queryUserByOpenIdAndCommerceId(Map<String, Object> mapU);
}
