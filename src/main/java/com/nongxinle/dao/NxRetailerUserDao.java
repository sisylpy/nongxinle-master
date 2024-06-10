package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-22 15:35
 */

import com.nongxinle.entity.NxRestrauntUserEntity;
import com.nongxinle.entity.NxRetailerUserEntity;

import java.util.List;


public interface NxRetailerUserDao extends BaseDao<NxRetailerUserEntity> {

    NxRetailerUserEntity queryRetailerUserByOpenId(String openid);

    List<NxRetailerUserEntity> queryRetUsersById(Integer retId);
}
