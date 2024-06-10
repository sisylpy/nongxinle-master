package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-28 13:40
 */

import com.nongxinle.entity.GbDistributerSupplierPaymentEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerSupplierPaymentService {
	
	GbDistributerSupplierPaymentEntity queryObject(Integer gbDistributerSupplierPaymentId);
	
	List<GbDistributerSupplierPaymentEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerSupplierPaymentEntity gbDistributerSupplierPayment);
	
	void update(GbDistributerSupplierPaymentEntity gbDistributerSupplierPayment);
	
	void delete(Integer gbDistributerSupplierPaymentId);
	
	void deleteBatch(Integer[] gbDistributerSupplierPaymentIds);

    List<GbDistributerSupplierPaymentEntity> queryPaymentListByParams(Map<String, Object> map);

    GbDistributerSupplierPaymentEntity queryPaymentByWxTradeNo(String ordersSn);
}
