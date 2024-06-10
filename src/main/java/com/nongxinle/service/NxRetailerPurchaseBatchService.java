package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-22 20:41
 */

import com.nongxinle.entity.NxRetailerPurchaseBatchEntity;

import java.util.List;
import java.util.Map;

public interface NxRetailerPurchaseBatchService {
	
	NxRetailerPurchaseBatchEntity queryObject(Integer nxRetailerPurchaseBatchId);
	
	List<NxRetailerPurchaseBatchEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxRetailerPurchaseBatchEntity nxRetailerPurchaseBatch);
	
	void update(NxRetailerPurchaseBatchEntity nxRetailerPurchaseBatch);
	
	void delete(Integer nxRetailerPurchaseBatchId);
	
	void deleteBatch(Integer[] nxRetailerPurchaseBatchIds);

    List<NxRetailerPurchaseBatchEntity> queryRetPurBatchByParams(Map<String, Object> map3);

	List<NxRetailerPurchaseBatchEntity> queryRetPurBatchSizeByParams(Map<String, Object> map3);

    NxRetailerPurchaseBatchEntity queryRetPurBatchDetail(Map<String, Object> map);
}
