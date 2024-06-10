package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-27 09:44
 */

import com.nongxinle.entity.GbDistributerUserEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerUserDao extends BaseDao<GbDistributerUserEntity> {

    GbDistributerUserEntity queryDisUserByOpenIdGb(String openid);

    GbDistributerUserEntity queryUserInfo(Integer userId);

    List<GbDistributerUserEntity> queryUserByOpenId(String openid);

    List<GbDistributerUserEntity> getAllUserByDisId(Integer disId);

    List<GbDistributerUserEntity> queryGbDisUserByParams(Map<String, Object> map);

    List<GbDistributerUserEntity> queryAllGbUsers();

}
