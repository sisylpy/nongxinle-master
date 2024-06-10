package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 09-16 09:12
 */

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbDistributerSupplierEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerSupplierService {
	
	GbDistributerSupplierEntity queryObject(Integer gbDistributerSupplierId);
	
	List<GbDistributerSupplierEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerSupplierEntity gbDistributerSupplier);
	
	void update(GbDistributerSupplierEntity gbDistributerSupplier);
	
	void delete(Integer gbDistributerSupplierId);
	
	void deleteBatch(Integer[] gbDistributerSupplierIds);

    List<GbDistributerSupplierEntity> querySupplierByParams(Map<String, Object> map);

    List<GbDistributerSupplierEntity> queryDepartmentSupplier(Map<String, Object> map);

    List<GbDepartmentEntity> querySupplierDepartmentGroup(Map<String, Object> map);

    List<GbDistributerSupplierEntity> queryDepartmentAppointSupplier(Map<String, Object> map);

	GbDistributerSupplierEntity queryAppointSupplierBySupplierId(Map<String, Object> map);
}
