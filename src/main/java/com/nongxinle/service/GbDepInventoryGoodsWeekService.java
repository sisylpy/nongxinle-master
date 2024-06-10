package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 08-23 14:08
 */

import com.nongxinle.entity.GbDepInventoryGoodsWeekEntity;
import com.nongxinle.entity.GbDepInventoryWeekEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface GbDepInventoryGoodsWeekService {
	
	GbDepInventoryGoodsWeekEntity queryObject(Integer gbInventoryGoodsWeekId);
	
	List<GbDepInventoryGoodsWeekEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepInventoryGoodsWeekEntity gbDepInventoryGoodsWeek);
	
	void update(GbDepInventoryGoodsWeekEntity gbDepInventoryGoodsWeek);
	
	void delete(Integer gbInventoryGoodsWeekId);
	
	void deleteBatch(Integer[] gbInventoryGoodsWeekIds);

    List<GbDepInventoryGoodsWeekEntity> queryWeekStockByParams(Map<String, Object> map);

    Integer queryWeekGoodsInventoryCount(Map<String, Object> map24);

	Double queryWeekGoodsTotal(Map<String, Object> map6);

    List<GbDepInventoryGoodsWeekEntity> queryWeekStockListByParams(Map<String, Object> map1);

	Double queryWeekGoodsLossTotal(Map<String, Object> map6);


    Double queryWeekGoodsWasteTotal(Map<String, Object> map6);

	Double queryWeekGoodsReturnTotal(Map<String, Object> map6);

    TreeSet<GbDistributerFatherGoodsEntity> queryTreeWeekGoodsList(Map<String, Object> map0);

    Double queryWeekGoodsProduceTotal(Map<String, Object> map1);

	TreeSet<GbDepInventoryGoodsWeekEntity> queryTreeWeekDisGoodsList(Map<String, Object> map0);
}
