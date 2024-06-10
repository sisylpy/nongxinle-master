package com.nongxinle.service;

/**
 *
 *
 * @author lpy
 * @date 09-06 15:27
 */

import com.nongxinle.entity.GbDepInventoryMonthEntity;

import java.util.List;
import java.util.Map;

public interface GbDepInventoryMonthService {
	
	GbDepInventoryMonthEntity queryObject(Integer gbInventoryMonthId);
	
	List<GbDepInventoryMonthEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepInventoryMonthEntity gbDepInventoryMonth);
	
	void update(GbDepInventoryMonthEntity gbDepInventoryMonth);
	
	void delete(Integer gbInventoryMonthId);
	
	void deleteBatch(Integer[] gbInventoryMonthIds);

    GbDepInventoryMonthEntity queryInventoryMonth(Map<String, Object> map);

    List<GbDepInventoryMonthEntity> queryDepMonthList(Map<String, Object> map);

    Double queryInventoryMonthTotal(Map<String, Object> map);

	Double queryInventoryMonthWasteTotal(Map<String, Object> map);

	Double queryInventoryMonthLossTotal(Map<String, Object> map);

    List<GbDepInventoryMonthEntity> queryInventoryWeekListByParams(Map<String, Object> map5);

    Double queryInventoryMonthReturnTotal(Map<String, Object> map);
}
