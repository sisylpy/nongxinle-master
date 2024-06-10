package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-25 15:21
 */

import com.nongxinle.entity.NxPrinterUserEntity;

import java.util.List;
import java.util.Map;

public interface NxPrinterUserService {
	
	NxPrinterUserEntity queryObject(Integer nxPrinterUserId);
	
	List<NxPrinterUserEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxPrinterUserEntity nxPrinterUser);
	
	void update(NxPrinterUserEntity nxPrinterUser);
	
	void delete(Integer nxPrinterUserId);
	
	void deleteBatch(Integer[] nxPrinterUserIds);

    NxPrinterUserEntity queryPrinterUserByOpenId(String openId);
}
