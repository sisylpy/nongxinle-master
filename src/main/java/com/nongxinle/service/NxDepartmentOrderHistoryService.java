package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 04-19 23:55
 */

import com.nongxinle.entity.DailyUsage;
import com.nongxinle.entity.NxDepartmentOrderHistoryEntity;
import com.nongxinle.entity.NxDistributerFatherGoodsEntity;

import java.util.List;
import java.util.Map;

public interface NxDepartmentOrderHistoryService {
	
	NxDepartmentOrderHistoryEntity queryObject(Integer nxDepartmentOrdersId);
	
	List<NxDepartmentOrderHistoryEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDepartmentOrderHistoryEntity nxDepartmentOrderHistory);
	
	void update(NxDepartmentOrderHistoryEntity nxDepartmentOrderHistory);
	
	void delete(Integer nxDepartmentOrdersId);
	
	void deleteBatch(Integer[] nxDepartmentOrdersIds);

    List<NxDepartmentOrderHistoryEntity> queryDisHistoryOrdersByParams(Map<String, Object> map);

	void moveOrderFromHistory(NxDepartmentOrderHistoryEntity orders);

    List<NxDistributerFatherGoodsEntity> queryGrandGoodsOrder(Map<String, Object> map);

	Double queryDepOrdersSubtotal(Map<String, Object> map);

	Integer queryDepOrdersAcount(Map<String, Object> map);

    double queryDisGoodsOrderWeightTotal(Map<String, Object> mapFather);

    List<Integer> queryGoodsIds(Map<String, Object> mapToday);

    NxDepartmentOrderHistoryEntity selectLastOrder(Map<String, Object> lastParams);

	List<DailyUsage> selectDailyUsage(Map<String, Object> usageParams);

	List<Integer> selectFrequentGoods(Map<String, Object> freqParams);

    List<NxDepartmentOrderHistoryEntity> queryDisHistoryOrdersByParamsForAi(Map<String, Object> orderHistoryParams);

    List<Integer> selectFrequentGoodsWithPage(Map<String, Object> freqParams);

	int selectFrequentGoodsCount(Map<String, Object> freqParams);

    List<Map<String, Object>> queryDepGoodsHistoryPrice(Map<String, Object> map);
}
