package com.nongxinle.dao;

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


public interface GbDistributerSupplierDao extends BaseDao<GbDistributerSupplierEntity> {

    List<GbDistributerSupplierEntity> querySupplierByParams(Map<String, Object> map);

    List<GbDistributerSupplierEntity> queryDepartmentSupplier(Map<String, Object> map);

    List<GbDepartmentEntity> querySupplierDepartmentGroup(Map<String, Object> map);

    List<GbDistributerSupplierEntity> queryDepartmentAppointSupplier(Map<String, Object> map);

    GbDistributerSupplierEntity queryAppointSupplierBySupplierId(Map<String, Object> map);
}
