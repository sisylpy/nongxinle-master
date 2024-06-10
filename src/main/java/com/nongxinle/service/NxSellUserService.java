package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-29 10:21
 */

import com.nongxinle.entity.NxSellUserEntity;

import java.util.List;
import java.util.Map;

public interface NxSellUserService {
	
	NxSellUserEntity queryObject(Integer nxSellUserId);
	
	List<NxSellUserEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxSellUserEntity nxSellUser);
	
	void update(NxSellUserEntity nxSellUser);
	
	void delete(Integer nxSellUserId);
	
	void deleteBatch(Integer[] nxSellUserIds);

    NxSellUserEntity querySellerUserByOpenId(String openId);

    List<NxSellUserEntity> querySupplierUserBySupplierId(Integer supplierId);

    NxSellUserEntity queryDisSellerUserByParmas(Map<String, Object> map);

	List<NxSellUserEntity> queryDisSellerUsersByParams(Integer disId);

    List<NxSellUserEntity> queryAllSellUsers();

}
