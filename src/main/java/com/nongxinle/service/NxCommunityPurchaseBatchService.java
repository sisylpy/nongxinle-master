package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 01-17 07:54
 */

import com.nongxinle.entity.NxCommunityPurchaseBatchEntity;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface NxCommunityPurchaseBatchService {
	
	NxCommunityPurchaseBatchEntity queryObject(Integer nxCommunityPurchaseBatchId);
	
	List<NxCommunityPurchaseBatchEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityPurchaseBatchEntity nxCommunityPurchaseBatch);
	
	void update(NxCommunityPurchaseBatchEntity nxCommunityPurchaseBatch);
	
	void delete(Integer nxCommunityPurchaseBatchId);
	
	void deleteBatch(Integer[] nxCommunityPurchaseBatchIds);

    List<NxCommunityPurchaseBatchEntity> queryComPurchaseBatchByParams(Map<String, Object> map2);

    NxCommunityPurchaseBatchEntity queryBatchDetail(Map<String, Object> map);

    Integer queryCommPurchaseBatchCount(Map<String, Object> map41);

	Double queryCommSupplierUnSettleSubtotal(Map<String, Object> map41);
}
