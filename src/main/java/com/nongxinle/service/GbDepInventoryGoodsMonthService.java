package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 08-23 14:08
 */

import com.nongxinle.entity.GbDepInventoryGoodsMonthEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface GbDepInventoryGoodsMonthService {
	
	GbDepInventoryGoodsMonthEntity queryObject(Integer gbInventoryGoodsMonthId);
	
	List<GbDepInventoryGoodsMonthEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepInventoryGoodsMonthEntity gbDepInventoryGoodsMonth);
	
	void update(GbDepInventoryGoodsMonthEntity gbDepInventoryGoodsMonth);
	
	void delete(Integer gbInventoryGoodsMonthId);
	
	void deleteBatch(Integer[] gbInventoryGoodsMonthIds);

    List<GbDepInventoryGoodsMonthEntity> queryMonthStockByParams(Map<String, Object> map);

    Integer queryMonthGoodsInventoryCount(Map<String, Object> map24);

	Double queryMonthGoodsTotal(Map<String, Object> map6);

    List<GbDepInventoryGoodsMonthEntity> queryMonthStockListByParams(Map<String, Object> map2);

    Double queryMonthGoodsLossTotal(Map<String, Object> map6);

    Double queryMonthGoodsWasteTotal(Map<String, Object> map6);

    Double queryMonthGoodsReturnTotal(Map<String, Object> map6);

    GbDepInventoryGoodsMonthEntity queryDepMonthStockByParams(Map<String, Object> map1);

    TreeSet<GbDistributerFatherGoodsEntity> queryTreeMonthGoodsList(Map<String, Object> map2);

    Double queryMonthGoodsProduceTotal(Map<String, Object> map2);
}
