package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-22 15:35
 */

import com.nongxinle.entity.NxRestrauntUserEntity;
import com.nongxinle.entity.NxRetailerUserEntity;

import java.util.List;
import java.util.Map;

public interface NxRetailerUserService {
	
	NxRetailerUserEntity queryObject(Integer nxRetailerUserId);

	void  save (NxRetailerUserEntity retailerUserEntity);

    NxRetailerUserEntity queryRetailerUserByOpenId(String openid);

	Map<String, Object> queryRetailerAndUserInfo(Integer restailerUserId);

    List<NxRetailerUserEntity> queryRetUsersById(Integer retId);
}
