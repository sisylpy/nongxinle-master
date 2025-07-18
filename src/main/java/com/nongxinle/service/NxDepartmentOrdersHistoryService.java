package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-08 19:09
 */

import com.nongxinle.entity.*;
import com.nongxinle.utils.PageUtils;

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

    List<NxDistributerGoodsEntity> queryDepTodayOrder(Map<String, Object> map);

	List<NxDistributerGoodsEntity> queryDisGoodsByParams(Map<String, Object> mapGD);

	List<Map<String,Object>> queryLogs(Map<String, Object> mapLog);

}
