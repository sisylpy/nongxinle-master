package com.nongxinle.service;

/**
 *
 *
 * @author lpy
 * @date 04-16 17:06
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface GbDepartmentGoodsDailyService {
	
	GbDepartmentGoodsDailyEntity queryObject(Integer gbDepartmentGoodsDailyId);
	
	List<GbDepartmentGoodsDailyEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepartmentGoodsDailyEntity gbDepartmentGoodsDaily);
	
	void update(GbDepartmentGoodsDailyEntity gbDepartmentGoodsDaily);
	
	void delete(Integer gbDepartmentGoodsDailyId);
	
	void deleteBatch(Integer[] gbDepartmentGoodsDailyIds);

    GbDepartmentGoodsDailyEntity queryDepGoodsDailyItem(Map<String, Object> map);

    Integer queryDepGoodsDailyCount(Map<String, Object> map);

	List<GbDepartmentGoodsDailyEntity> queryDepGoodsDailyListByParams(Map<String, Object> map);

	Double queryDepGoodsDailyProfitSubtotal(Map<String, Object> map1);

	Double queryDepGoodsDailySalesSubtotal(Map<String, Object> map1222);

	Double queryDepGoodsDailySalesProfitSubtotal(Map<String, Object> map1222);

	List<GbDistributerFatherGoodsEntity> queryDepDailyGoodsFatherTypeByParams(Map<String, Object> map1222);

    TreeSet<GbDistributerGoodsEntity> queryDisGoodsTreesetByParams(Map<String, Object> map);

    Double queryDepGoodsDailyLossSubtotal(Map<String, Object> map0);

	Double queryDepGoodsDailyWasteSubtotal(Map<String, Object> map0);

	TreeSet<GbDepartmentEntity> queryWhichDepsHasProduceDepGoodsDaily(Map<String, Object> mapDep);

    Double queryDepGoodsDailyProduceWeight(Map<String, Object> map0);

	Double queryDepGoodsDailyLossWeight(Map<String, Object> map0);

	Double queryDepGoodsDailyWasteWeight(Map<String, Object> map0);

	TreeSet<GbDistributerFatherGoodsEntity> queryFreshFatherGoods(Map<String, Object> map);

	Double queryDepGoodsDailyRestWeight(Map<String, Object> map);

    TreeSet<GbDepartmentDisGoodsEntity> queryDepDisGoodsTreeByParams(Map<String, Object> map);

	Double queryDepGoodsDailyWeight(Map<String, Object> map);

    Double queryDepGoodsDailyLastWeight(Map<String, Object> map);

    TreeSet<GbDistributerFatherGoodsEntity> queryClearFatherGoods(Map<String, Object> map);

	int queryDepGoodsDailyClearHour(Map<String, Object> map);

	int queryDepGoodsDailyClearMinute(Map<String, Object> map);

    Double queryDepGoodsDailyProduceSubtotal(Map<String, Object> map11);

    double queryDepGoodsFreshRate(Map<String, Object> map);


    Double queryDepGoodsDailySubtotal(Map<String, Object> disGoodsMap);

    double queryDepGoodsDailyReturnSubtotal(Map<String, Object> disGoodsMap);

	double queryDepGoodsDailyHighestFreshRate(Map<String, Object> map);

	double queryDepGoodsDailyLowestFreshRate(Map<String, Object> map);

    Double queryDepGoodsDailyReturnWeight(Map<String, Object> disGoodsMap);

    TreeSet<GbDepartmentEntity> queryWhichDepsHasProduceDepGoodsDailyNew(Map<String, Object> map);

	List<GbDepartmentGoodsDailyEntity> queryDepGoodsDailyListWithGoodsByParams(Map<String, Object> mapSearch);

    List<GbDepartmentGoodsDailyEntity> queryDepGoodsDailyListWithReduceByParams(Map<String, Object> mapSearch);

}
