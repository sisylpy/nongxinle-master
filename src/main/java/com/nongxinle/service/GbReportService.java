package com.nongxinle.service;

/**
 *
 *
 * @author lpy
 * @date 09-26 20:05
 */

import com.nongxinle.entity.GbReportEntity;

import java.util.List;
import java.util.Map;

public interface GbReportService {
	
	GbReportEntity queryObject(Integer gbReportId);
	
	List<GbReportEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbReportEntity gbReport);
	
	void update(GbReportEntity gbReport);
	
	void delete(Integer gbReportId);
	
	void deleteBatch(Integer[] gbReportIds);

    List<GbReportEntity> queryReportList(Map<String, Object> map);
}
