package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 01-08 12:27
 */

import com.nongxinle.entity.GbDistributerPayEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerPayService {
	
	GbDistributerPayEntity queryObject(Integer gbDistributerPayId);
	
	List<GbDistributerPayEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerPayEntity gbDistributerPay);
	
	void update(GbDistributerPayEntity gbDistributerPay);
	
	void delete(Integer gbDistributerPayId);
	
	void deleteBatch(Integer[] gbDistributerPayIds);

    List<GbDistributerPayEntity> queryDisPayListByParams(Map<String, Object> mapP);

	List<GbDistributerPayEntity> queryListByTradeNo(String ordersSn);

    GbDistributerPayEntity queryPayItemByPayId(Integer payId);
}
