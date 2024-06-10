package com.nongxinle.dao;

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


public interface NxJrdhUserDao extends BaseDao<NxJrdhUserEntity> {

    NxJrdhUserEntity queryWhichUserByOpenId(String openid);

    List<NxJrdhUserEntity> queryJrdhUserByParams(Map<String, Object> map);

    NxJrdhUserEntity queryJrdhUserById(Map<String, Object> map);

    NxJrdhUserEntity queryDisUserByOpenId(String openId);

    NxJrdhUserEntity querySelluserByOpenId(String openId);

    NxJrdhUserEntity queryJrdhUserByAdmin(Map<String, Object> map);

}
