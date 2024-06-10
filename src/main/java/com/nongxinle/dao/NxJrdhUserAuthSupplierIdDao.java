package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 09-11 14:46
 */

import com.nongxinle.entity.NxJrdhUserAuthSupplierIdEntity;

import java.util.Map;


public interface NxJrdhUserAuthSupplierIdDao extends BaseDao<NxJrdhUserAuthSupplierIdEntity> {

    NxJrdhUserAuthSupplierIdEntity queryAuthSupplierByIds(Map<String, Object> map);
}
