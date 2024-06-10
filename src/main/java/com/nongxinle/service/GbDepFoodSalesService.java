package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 03-27 21:55
 */

import com.nongxinle.entity.GbDepFoodSalesEntity;

import java.util.List;
import java.util.Map;

public interface GbDepFoodSalesService {
	
	GbDepFoodSalesEntity queryObject(Integer gbDepFoodSalesId);
	
	List<GbDepFoodSalesEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepFoodSalesEntity gbDepFoodSales);
	
	void update(GbDepFoodSalesEntity gbDepFoodSales);
	
	void delete(Integer gbDepFoodSalesId);
	
	void deleteBatch(Integer[] gbDepFoodSalesIds);

    List<GbDepFoodSalesEntity> queryDepFoodSalesByParams(Map<String, Object> mapFoodSales);

}
