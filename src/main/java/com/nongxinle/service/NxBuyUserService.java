package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-29 10:22
 */

import com.nongxinle.entity.NxBuyUserEntity;
import com.nongxinle.entity.NxDistributerPurchaseBatchEntity;
import com.nongxinle.entity.NxRestrauntUserEntity;

import java.util.List;
import java.util.Map;

public interface NxBuyUserService {
	
	NxBuyUserEntity queryObject(Integer nxBuyUserId);
	
	List<NxBuyUserEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxBuyUserEntity nxBuyUser);
	
	void update(NxBuyUserEntity nxBuyUser);
	
	void delete(Integer nxBuyUserId);
	
	void deleteBatch(Integer[] nxBuyUserIds);


    List<NxDistributerPurchaseBatchEntity> queryBuyerPurchaseBatchDayWork(Map<String, Object> mapZero);

    List<NxBuyUserEntity> queryAllNxBuyerUsers();


}
