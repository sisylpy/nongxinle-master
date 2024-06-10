package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 02-18 14:22
 */

import com.nongxinle.entity.GbDepInventoryGoodsMonthTotalEntity;

import java.util.List;
import java.util.Map;

public interface GbDepInventoryGoodsMonthTotalService {
	
	GbDepInventoryGoodsMonthTotalEntity queryObject(Integer gbInventoryGoodsMonthTotalId);
	
	List<GbDepInventoryGoodsMonthTotalEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepInventoryGoodsMonthTotalEntity gbDepInventoryGoodsMonthTotal);
	
	void update(GbDepInventoryGoodsMonthTotalEntity gbDepInventoryGoodsMonthTotal);
	
	void delete(Integer gbInventoryGoodsMonthTotalId);
	
	void deleteBatch(Integer[] gbInventoryGoodsMonthTotalIds);
}
