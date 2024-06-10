package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-11 21:54
 */

import com.nongxinle.entity.NxJrdhSupplierEntity;

import java.util.List;
import java.util.Map;

public interface NxJrdhSupplierService {
	
	NxJrdhSupplierEntity queryObject(Integer nxJrdhSupplierId);
	
	List<NxJrdhSupplierEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxJrdhSupplierEntity nxJrdhSupplier);
	
	void update(NxJrdhSupplierEntity nxJrdhSupplier);
	
	void delete(Integer nxJrdhSupplierId);
	
	void deleteBatch(Integer[] nxJrdhSupplierIds);

    List<NxJrdhSupplierEntity> queryJrdhSupplerByParams(Map<String, Object> map);

    NxJrdhSupplierEntity querySellUserSupplier(Map<String, Object> mapS);

    List<NxJrdhSupplierEntity> queryJrdhSupplerWithDisByUserId(Map<String, Object> mapS);

    NxJrdhSupplierEntity querySupplierByUserId(Integer sellerId);
}
