package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 09-06 15:27
 */

import com.nongxinle.entity.GbDepInventoryWeekEntity;

import java.util.List;
import java.util.Map;

public interface GbDepInventoryWeekService {
	
	GbDepInventoryWeekEntity queryObject(Integer gbInventoryWeekId);
	
	List<GbDepInventoryWeekEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepInventoryWeekEntity gbDepInventoryWeek);
	
	void update(GbDepInventoryWeekEntity gbDepInventoryWeek);
	
	void delete(Integer gbInventoryWeekId);
	
	void deleteBatch(Integer[] gbInventoryWeekIds);

	GbDepInventoryWeekEntity queryInventoryWeek(Map<String, Object> map1);

    List<GbDepInventoryWeekEntity> queryDepWeekList(Map<String, Object> map);

	Double queryInventoryWeekTotal(Map<String, Object> map);

    Double queryInventoryWeekWasteTotal(Map<String, Object> map);

	Double queryInventoryWeekLossTotal(Map<String, Object> map);

    List<GbDepInventoryWeekEntity> queryInventoryWeekListByParams(Map<String, Object> map4);

    Double queryInventoryWeekReturnTotal(Map<String, Object> map);
}
