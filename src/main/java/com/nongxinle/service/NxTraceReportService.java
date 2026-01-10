package com.nongxinle.service;

/**
 * 溯源报告Service接口
 * 
 * @author lpy
 * @date 2025-01-XX
 */

import com.nongxinle.entity.NxTraceReportEntity;

import java.util.List;
import java.util.Map;

public interface NxTraceReportService {
	
	NxTraceReportEntity queryObject(Integer nxTraceReportId);
	
	List<NxTraceReportEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxTraceReportEntity nxTraceReport);
	
	void update(NxTraceReportEntity nxTraceReport);
	
	void delete(Integer nxTraceReportId);
	
	void deleteBatch(Integer[] nxTraceReportIds);

	/**
	 * 根据条件查询溯源报告列表
	 * @param map 查询条件
	 * @return 溯源报告列表
	 */
	List<NxTraceReportEntity> queryTraceReportList(Map<String, Object> map);
}

