package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-15 18:45
 */

import com.nongxinle.entity.NxCommunityPurchaseBatchEntity;
import com.nongxinle.entity.NxCommunitySupplierPaymentEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunitySupplierPaymentService {
	
	NxCommunitySupplierPaymentEntity queryObject(Integer nxCommuntiySupplierPaymentId);
	
	List<NxCommunitySupplierPaymentEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunitySupplierPaymentEntity nxCommunitySupplierPayment);
	
	void update(NxCommunitySupplierPaymentEntity nxCommunitySupplierPayment);
	
	void delete(Integer nxCommuntiySupplierPaymentId);
	
	void deleteBatch(Integer[] nxCommuntiySupplierPaymentIds);

}
