package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-29 06:59
 */

import com.nongxinle.entity.NxAiForecastLogEntity;

import java.util.List;
import java.util.Map;

public interface NxAiForecastLogService {
	
	NxAiForecastLogEntity queryObject(Integer nxAiForecastLogId);
	
	List<NxAiForecastLogEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxAiForecastLogEntity nxAiForecastLog);
	
	void update(NxAiForecastLogEntity nxAiForecastLog);
	
	void delete(Integer nxAiForecastLogId);
	
	void deleteBatch(Integer[] nxAiForecastLogIds);
}
