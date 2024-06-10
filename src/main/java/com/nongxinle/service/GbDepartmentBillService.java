package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 09-20 15:11
 */

import com.nongxinle.entity.GbDepartmentBillEntity;

import java.util.List;
import java.util.Map;

public interface GbDepartmentBillService {
	
	GbDepartmentBillEntity queryObject(Integer gbDepartmentBillId);
	
	List<GbDepartmentBillEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepartmentBillEntity gbDepartmentBill);
	
	void update(GbDepartmentBillEntity gbDepartmentBill);
	
	void delete(Integer gbDepartmentBillId);
	
	void deleteBatch(Integer[] gbDepartmentBillIds);

    List<GbDepartmentBillEntity> queryDepartmentBillList(Map<String, Object> map);

	GbDepartmentBillEntity queryGbDepartmentBillDetail(Integer billId);

	List<GbDepartmentBillEntity> queryBillsByParamsGb(Map<String, Object> map);

	int queryTotalByParamsGb(Map<String, Object> map1);

    Integer queryBillsCountByParamsGb(Map<String, Object> map4);

	Double queryGbDepBillsSubTotal(Map<String, Object> map);

    Double queryGbDepBillsSellingSubTotal(Map<String, Object> map12);

    int queryDepartmentBillCount(Map<String, Object> map3);

    List<GbDepartmentBillEntity> queryBillGoodsByParams(Map<String, Object> map);

    GbDepartmentBillEntity queryDepartBillByTradeNo(String ordersSn);

//    List<GbDepartmentBillEntity> queryDepartBillListByTradeNo(String ordersSn);

    GbDepartmentBillEntity queryDepartBillByTsxTradeNo(String nxDbTradeNo);
}
