package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-10 10:29
 */

import com.nongxinle.entity.GbDistributerSupplierUserEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerSupplierUserDao extends BaseDao<GbDistributerSupplierUserEntity> {

    GbDistributerSupplierUserEntity querySupplierUserByParams(Map<String, Object> map);

    GbDistributerSupplierUserEntity querySupplierUserByOpenId(String openId);

    List<GbDistributerSupplierUserEntity> querySupplierUsersBySupplierId(Integer returnSupplierId);

    GbDistributerSupplierUserEntity queryAppointUserBySupplierId(Integer supplierId);

}
