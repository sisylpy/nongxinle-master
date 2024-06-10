package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-30 10:14
 */

import com.nongxinle.entity.GbDepartmentOrdersHistoryEntity;
import com.nongxinle.entity.NxDepartmentOrdersHistoryEntity;

import java.util.List;
import java.util.Map;

public interface GbDepartmentOrdersHistoryService {
	
	GbDepartmentOrdersHistoryEntity queryObject(Integer gbDepartmentOrdersHistoryId);
	
	List<GbDepartmentOrdersHistoryEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepartmentOrdersHistoryEntity gbDepartmentOrdersHistory);
	
	void update(GbDepartmentOrdersHistoryEntity gbDepartmentOrdersHistory);
	
	void delete(Integer gbDepartmentOrdersHistoryId);
	
	void deleteBatch(Integer[] gbDepartmentOrdersHistoryIds);

    List<GbDepartmentOrdersHistoryEntity> queryGbDepHistoryOrdersByParams(Map<String, Object> map1);

    List<GbDepartmentOrdersHistoryEntity> queryDepHistoryOrdersByParamsGb(Map<String, Object> map1);
}
