package com.nongxinle.community.customer.service;

/**
 * 
 *
 * @author lpy
 * @date 05-24 01:00
 */

import com.nongxinle.entity.NxCustomerUserCardEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerUserCardService {
	
	NxCustomerUserCardEntity queryObject(Integer nxCustomerUserCardId);
	
	List<NxCustomerUserCardEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCustomerUserCardEntity nxCustomerUserCard);
	
	void update(NxCustomerUserCardEntity nxCustomerUserCard);
	
	void delete(Integer nxCustomerUserCardId);
	
	void deleteBatch(Integer[] nxCustomerUserCardIds);

    List<NxCustomerUserCardEntity> queryUserCardByParams(Map<String, Object> map);

    NxCustomerUserCardEntity queryUserGoodsCard(Map<String, Object> map);

	int queryUserCardCount(Map<String, Object> mapCARD);
}
