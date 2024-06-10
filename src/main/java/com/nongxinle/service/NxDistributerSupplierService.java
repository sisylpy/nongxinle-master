package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-12 19:46
 */

import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.entity.NxDistributerSupplierEntity;
import com.nongxinle.entity.NxJrdhUserEntity;
import com.nongxinle.entity.NxSellUserEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerSupplierService {
	
	NxDistributerSupplierEntity queryObject(Integer nxDistributerSupplierId);
	
	List<NxDistributerSupplierEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerSupplierEntity nxDistributerSupplier);
	
	void update(NxDistributerSupplierEntity nxDistributerSupplier);
	
	void delete(Integer nxDistributerSupplierId);
	
	void deleteBatch(Integer[] nxDistributerSupplierIds);

    List<NxDistributerSupplierEntity> queryDisSupplierByParams(Map<String, Object> map);

    List<NxJrdhUserEntity> querySellerDistributerByParams(Map<String, Object> map);
}
