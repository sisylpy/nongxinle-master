package com.nongxinle.community.customer.service;

/**
 * 
 *
 * @author lpy
 * @date 09-20 00:57
 */

import com.nongxinle.entity.NxCustomerUserAddressEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerUserAddressService {
	
	NxCustomerUserAddressEntity queryObject(Integer nxCustomerUserAddressId);
	
	List<NxCustomerUserAddressEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCustomerUserAddressEntity nxCustomerUserAddress);
	
	void update(NxCustomerUserAddressEntity nxCustomerUserAddress);
	
	void delete(Integer nxCustomerUserAddressId);
	
	void deleteBatch(Integer[] nxCustomerUserAddressIds);

    List<NxCustomerUserAddressEntity> queryAddressByUserId(Integer userId);

    NxCustomerUserAddressEntity queryMainAddressByUserId(Integer nxCuaCustomerUserId);
}
