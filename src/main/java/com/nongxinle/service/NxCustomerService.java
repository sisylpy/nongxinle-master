package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import com.nongxinle.entity.NxCustomerEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerService {
	
	NxCustomerEntity queryObject(Integer customerId);
	
	List<NxCustomerEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCustomerEntity nxCustomer);
	
	void update(NxCustomerEntity nxCustomer);
	
	void delete(Integer customerId);
	
	void deleteBatch(Integer[] customerIds);

    List<NxCustomerEntity> queryCommunityCustomers(Map<String, Object> map);

	int queryCustomerOfCommunityTotal(Map<String, Object> map);
}
