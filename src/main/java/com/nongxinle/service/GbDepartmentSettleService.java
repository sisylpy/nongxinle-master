package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-22 16:10
 */

import com.nongxinle.entity.GbDepartmentSettleEntity;

import java.util.List;
import java.util.Map;

public interface GbDepartmentSettleService {
	
	GbDepartmentSettleEntity queryObject(Integer gbDepartmentSettleId);
	
	List<GbDepartmentSettleEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepartmentSettleEntity gbDepartmentSettle);
	
	void update(GbDepartmentSettleEntity gbDepartmentSettle);
	
	void delete(Integer gbDepartmentSettleId);
	
	void deleteBatch(Integer[] gbDepartmentSettleIds);

    List<GbDepartmentSettleEntity> queryDepartmentSettlesByParams(Map<String, Object> map);

	GbDepartmentSettleEntity queryTotalBySettleId(String settleId);

}
