package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-08 19:09
 */

import com.nongxinle.entity.NxDepartmentOrdersHistoryEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;

import java.util.List;
import java.util.Map;

public interface NxDepartmentOrdersHistoryService {
	
	NxDepartmentOrdersHistoryEntity queryObject(Integer nxDepartmentOrdersHistoryId);
	
	List<NxDepartmentOrdersHistoryEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDepartmentOrdersHistoryEntity nxDepartmentOrdersHistory);
	
	void update(NxDepartmentOrdersHistoryEntity nxDepartmentOrdersHistory);
	
	void delete(Integer nxDepartmentOrdersHistoryId);
	
	void deleteBatch(Integer[] nxDepartmentOrdersHistoryIds);

    List<NxDepartmentOrdersHistoryEntity> queryDepHistoryOrdersByParams(Map<String, Object> map1);

    int queryOrderTimes(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDepTodayOrder(Map<String, Object> map);
}
