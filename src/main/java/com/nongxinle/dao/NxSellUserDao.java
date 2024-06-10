package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-29 10:21
 */

import com.nongxinle.entity.NxSellUserEntity;

import java.util.List;
import java.util.Map;


public interface NxSellUserDao extends BaseDao<NxSellUserEntity> {

    NxSellUserEntity querySellerUserByOpenId(String openId);

    List<NxSellUserEntity> querySupplierUserBySupplierId(Integer supplierId);

    NxSellUserEntity queryDisSellerUserByParmas(Map<String, Object> map);

    List<NxSellUserEntity> queryDisSellerUsersByParams(Integer disId);


    List<NxSellUserEntity> queryAllSellUsers();
}
