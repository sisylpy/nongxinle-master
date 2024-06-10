package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 02-18 14:22
 */

import com.nongxinle.entity.GbDepInventoryGoodsWeekTotalEntity;

import java.util.List;
import java.util.Map;

public interface GbDepInventoryGoodsWeekTotalService {
	
	GbDepInventoryGoodsWeekTotalEntity queryObject(Integer gbInventoryGoodsWeekTotalId);
	
	List<GbDepInventoryGoodsWeekTotalEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepInventoryGoodsWeekTotalEntity gbDepInventoryGoodsWeekTotal);
	
	void update(GbDepInventoryGoodsWeekTotalEntity gbDepInventoryGoodsWeekTotal);
	
	void delete(Integer gbInventoryGoodsWeekTotalId);
	
	void deleteBatch(Integer[] gbInventoryGoodsWeekTotalIds);
}
