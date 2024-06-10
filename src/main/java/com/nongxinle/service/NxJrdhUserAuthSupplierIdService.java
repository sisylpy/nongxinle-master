package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 09-11 14:46
 */

import com.nongxinle.entity.NxJrdhUserAuthSupplierIdEntity;

import java.util.List;
import java.util.Map;

public interface NxJrdhUserAuthSupplierIdService {
	
	NxJrdhUserAuthSupplierIdEntity queryObject(Integer nxJrdhUserAuthSupplierId);
	
	List<NxJrdhUserAuthSupplierIdEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxJrdhUserAuthSupplierIdEntity nxJrdhUserAuthSupplierId);
	
	void update(NxJrdhUserAuthSupplierIdEntity nxJrdhUserAuthSupplierId);
	
	void delete(Integer nxJrdhUserAuthSupplierId);
	
	void deleteBatch(Integer[] nxJrdhUserAuthSupplierIds);

    NxJrdhUserAuthSupplierIdEntity queryAuthSupplierByIds(Map<String, Object> map);
}
