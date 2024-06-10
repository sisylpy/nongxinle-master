package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-10 10:29
 */

import com.nongxinle.entity.GbDistributerSupplierUserEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerSupplierUserService {
	
	GbDistributerSupplierUserEntity queryObject(Integer gbDistributerSupplierUserId);
	
	List<GbDistributerSupplierUserEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerSupplierUserEntity gbDistributerSupplierUser);
	
	void update(GbDistributerSupplierUserEntity gbDistributerSupplierUser);
	
	void delete(Integer gbDistributerSupplierUserId);
	
	void deleteBatch(Integer[] gbDistributerSupplierUserIds);

	GbDistributerSupplierUserEntity querySupplierUserByParams(Map<String, Object> map);

    GbDistributerSupplierUserEntity querySupplierUserByOpenId(String openId);

	List<GbDistributerSupplierUserEntity> querySupplierUsersBySupplierId(Integer returnSupplierId);

    GbDistributerSupplierUserEntity queryAppointUserBySupplierId(Integer supplierId);

}
