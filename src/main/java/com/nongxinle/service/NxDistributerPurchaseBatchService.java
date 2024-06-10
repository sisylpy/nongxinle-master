package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-25 22:52
 */

import com.nongxinle.entity.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface NxDistributerPurchaseBatchService {
	
	NxDistributerPurchaseBatchEntity queryObject(Integer nxDistributerPurchaseBatchId);
	
	List<NxDistributerPurchaseBatchEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatch);
	
	void update(NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatch);
	
	void delete(Integer nxDpbUuid);
	
	void deleteBatch(String[] nxDpbUuids);

	List<NxDistributerPurchaseBatchEntity> queryDisPurchaseBatch(Map<String, Object> map);

    NxDistributerPurchaseBatchEntity queryBatchWithOrders(Integer batchId);


	int queryDisPurchaseBatchCount(Map<String, Object> map);

	Double queryDisPurchaseBatchTotal(Map<String, Object> map);

    Double queryPurchaseGoodsSubTotal(Map<String, Object> map);

	Double queryPurchaserCashTotal(Map<String, Object> map1);

	List<NxDistributerEntity> queryNxDistributerBySellerId(String sellId);

    NxDistributerPurchaseBatchEntity queryBatchItemByParams(Map<String, Object> mapB);
}
