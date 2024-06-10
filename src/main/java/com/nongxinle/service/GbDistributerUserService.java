package com.nongxinle.service;

/**
 *
 *
 * @author lpy
 * @date 06-27 09:44
 */

import com.nongxinle.entity.GbDistributerUserEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerUserService {
	
	GbDistributerUserEntity queryObject(Integer gbDistributerUserId);
	
	List<GbDistributerUserEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	Integer  save(GbDistributerUserEntity gbDistributerUser);
	
	void update(GbDistributerUserEntity gbDistributerUser);
	
	void delete(Integer gbDistributerUserId);
	
	void deleteBatch(Integer[] gbDistributerUserIds);

    GbDistributerUserEntity queryDisUserByOpenIdGb(String openid);

    Map<String, Object> queryDisAndUserInfo(Integer resUserId);

    GbDistributerUserEntity queryUserInfo(Integer userId);

	List<GbDistributerUserEntity> queryUserByOpenId(String openid);

	List<GbDistributerUserEntity> getAllUserByDisId(Integer disId);

    List<GbDistributerUserEntity> queryGbUser(Integer disId);

    List<GbDistributerUserEntity> queryGbDisUserByParams(Map<String, Object> map);

	List<GbDistributerUserEntity> queryAllGbUsers();



}
