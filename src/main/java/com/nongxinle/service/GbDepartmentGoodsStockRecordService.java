package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-22 17:52
 */

import com.nongxinle.entity.GbDepartmentGoodsStockEntity;
import com.nongxinle.entity.GbDepartmentGoodsStockRecordEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface GbDepartmentGoodsStockRecordService {
	
	GbDepartmentGoodsStockRecordEntity queryObject(Integer gbDepartmentGoodsStockRecordId);
	
	List<GbDepartmentGoodsStockRecordEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepartmentGoodsStockRecordEntity gbDepartmentGoodsStockRecord);
	
	void update(GbDepartmentGoodsStockRecordEntity gbDepartmentGoodsStockRecord);
	
	void delete(Integer gbDepartmentGoodsStockRecordId);
	
	void deleteBatch(Integer[] gbDepartmentGoodsStockRecordIds);

    Integer queryGoodsStockRecordCount(Map<String, Object> map1451);

	Double queryGoodsStockRecordSubtotal(Map<String, Object> map42);

    List<GbDepartmentGoodsStockRecordEntity> queryGoodsStockListByParams(Map<String, Object> map5);


    Double queryManyTotal(Map<String, Object> map0);

	List<GbDistributerFatherGoodsEntity> queryDepStockRecordDisFatherGoodsFather(Map<String, Object> map0);

    List<GbDistributerGoodsEntity> queryDisGoodsByParams(Map<String, Object> map);

    Double queryGoodsStockRecordWeightTotal(Map<String, Object> map1);

    List<GbDepartmentGoodsStockRecordEntity> queryDepGoodsStockRecordDetailByParams(Map<String, Object> map);
}
