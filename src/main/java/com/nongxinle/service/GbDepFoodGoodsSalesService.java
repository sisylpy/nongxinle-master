package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 03-27 21:55
 */

import com.nongxinle.entity.GbDepFoodGoodsSalesEntity;
import com.nongxinle.entity.GbDepFoodSalesEntity;
import com.nongxinle.entity.GbDistributerPurchaseGoodsEntity;

import java.util.List;
import java.util.Map;

public interface GbDepFoodGoodsSalesService {
	
	GbDepFoodGoodsSalesEntity queryObject(Integer gbDepFoodGoodsSalesId);
	
	List<GbDepFoodGoodsSalesEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepFoodGoodsSalesEntity gbDepFoodGoodsSales);
	
	void update(GbDepFoodGoodsSalesEntity gbDepFoodGoodsSales);
	
	void delete(Integer gbDepFoodGoodsSalesId);
	
	void deleteBatch(Integer[] gbDepFoodGoodsSalesIds);

    List<GbDepFoodGoodsSalesEntity> queryDepFoodGoodsSalesByParams(Map<String, Object> mapFoodSales);

    List<GbDepFoodSalesEntity> queryDepFoodsWithGoods(Map<String, Object> map);

//    List<GbDistributerPurchaseGoodsEntity> queryPurGoodsForCost(Map<String, Object> map);
}
