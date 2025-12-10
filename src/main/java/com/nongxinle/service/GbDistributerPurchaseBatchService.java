package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-25 22:52
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;

public interface GbDistributerPurchaseBatchService {
	
	GbDistributerPurchaseBatchEntity queryObject(Integer gbDistributerPurchaseBatchId);
	
	List<GbDistributerPurchaseBatchEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatch);
	
	void update(GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatch);
	
	void delete(Integer nxDpbUuid);
	
	void deleteBatch(String[] nxDpbUuids);

	List<GbDistributerPurchaseBatchEntity> queryDisPurchaseBatch(Map<String, Object>  map);

    GbDistributerPurchaseBatchEntity queryBatchWithOrders(Integer batchId);

    List<GbDistributerPurchaseBatchEntity> queryDepartmentPurchaseBatch(Map<String, Object> map);

    Integer queryDisPurchaseBatchCount(Map<String, Object> map2);

    Double querySupplierUnSettleSubtotal(Map<String, Object> map4);

    List<GbDepartmentEntity> queryDistributerAccountingData(Map<String, Object> map2);

    Double queryPurchaserCashTotal(Map<String, Object> map1);

    int queryReturnList(Map<String, Object> map333);

    List<GbDistributerEntity> queryGbDistributerBySellerId(String sellId);

    GbDistributerPurchaseBatchEntity queryBatchItemByParams(Map<String, Object> mapB);

    List<NxJrdhSupplierEntity> querySupplierList(Map<String, Object> map);

    List<GbDistributerPurchaseBatchEntity> queryDisPurchaseBatchInfo(Map<String, Object> map);

    List<NxDistributerEntity> queryDisPurchaseBatchWithNx(Map<String, Object> map);

    List<NxJrdhSupplierEntity> querySupplierListWithDetail(Map<String, Object> mapS);
}
