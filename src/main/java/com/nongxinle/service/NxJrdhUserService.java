package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-12 11:38
 */

import com.nongxinle.entity.NxBuyUserEntity;
import com.nongxinle.entity.NxJrdhUserEntity;

import java.util.List;
import java.util.Map;

public interface NxJrdhUserService {
	
	NxJrdhUserEntity queryObject(Integer nxJrdhUserId);
	
	List<NxJrdhUserEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxJrdhUserEntity nxJrdhUser);
	
	void update(NxJrdhUserEntity nxJrdhUser);
	
	void delete(Integer nxJrdhUserId);
	
	void deleteBatch(Integer[] nxJrdhUserIds);

	NxJrdhUserEntity queryWhichUserByOpenId(String openid);

    List<NxJrdhUserEntity> queryJrdhUserByParams(Map<String, Object> map);

    NxJrdhUserEntity queryJrdhUserById(Map<String, Object> map);

    NxJrdhUserEntity queryDisUserByOpenId(String openId);

	NxJrdhUserEntity querySelluserByOpenId(String openId);

    NxJrdhUserEntity queryJrdhUserByAdmin(Map<String, Object> map);
}
