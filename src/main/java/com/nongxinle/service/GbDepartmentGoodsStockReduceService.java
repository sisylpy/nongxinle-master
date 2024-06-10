package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 11-20 12:33
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface GbDepartmentGoodsStockReduceService {
	
	GbDepartmentGoodsStockReduceEntity queryObject(Integer gbDepartmentGoodsStockReduceId);
	
	List<GbDepartmentGoodsStockReduceEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepartmentGoodsStockReduceEntity gbDepartmentGoodsStockReduce);
	
	void update(GbDepartmentGoodsStockReduceEntity gbDepartmentGoodsStockReduce);
	
	void delete(Integer gbDepartmentGoodsStockReduceId);
	
	void deleteBatch(Integer[] gbDepartmentGoodsStockReduceIds);

    GbDepartmentGoodsStockReduceEntity queryReduceCostByParams(Map<String, Object> map111);

    Integer queryReduceTypeCount(Map<String, Object> map12);

	Double queryReduceProduceTotal(Map<String, Object> map12);

	Double queryReduceLossTotal(Map<String, Object> map121);

	Double queryReduceWasteTotal(Map<String, Object> map122);

    List<GbDistributerGoodsEntity> queryReduceGoodsTypeByParams(Map<String, Object> map121);


	Double queryReduceReturnTotal(Map<String, Object> map1213);

	List<GbDepartmentGoodsStockReduceEntity> queryStockReduceListByParams(Map<String, Object> mapRed);

	TreeSet<GbDistributerFatherGoodsEntity> queryReduceGoodsFatherTypeByParams(Map<String, Object> map1222);

    Double queryReduceWasteWeightTotal(Map<String, Object> map122);

	Double queryReduceLossWeightTotal(Map<String, Object> map121);

    Double queryReduceProduceWeightTotal(Map<String, Object> map123);

    List<GbDistributerFatherGoodsEntity> queryReduceTypeGoodsFather(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryReduceFatherGoods(Map<String, Object> map);

    TreeSet<GbDistributerGoodsEntity> queryGoodsStockRecordTreeByParams(Map<String, Object> map);

	GbDistributerGoodsEntity queryReduceGoodsTypeWorkByParams(Map<String, Object> map);

    Double queryReduceCostSubtotal(Map<String, Object> map1222);

	Double queryReduceCostWeightTotal(Map<String, Object> map1);


    TreeSet<GbDistributerFatherGoodsEntity> queryDepStockReduceTreeFatherGoodsByParams(Map<String, Object> map);

    double queryReduceReturnWeightTotal(Map<String, Object> mapDepStock);

	List<GbDepartmentEntity> queryReduceDepartment(Map<String, Object> disGoodsMap);

    List<GbDistributerPurchaseGoodsEntity> queryPurGoodsForCost(Map<String, Object> map);
}
