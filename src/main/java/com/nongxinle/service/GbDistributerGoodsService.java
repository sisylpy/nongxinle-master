package com.nongxinle.service;

/**
 *
 *
 * @author lpy
 * @date 06-18 21:32
 */

import com.nongxinle.entity.GbDistributerEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;
import com.nongxinle.entity.NxDistributerEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerGoodsService {
	
	GbDistributerGoodsEntity queryObject(Integer gbDistributerGoodsId);
	
	List<GbDistributerGoodsEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerGoodsEntity gbDistributerGoods);
	
	void update(GbDistributerGoodsEntity gbDistributerGoods);
	
	int delete(Integer gbDistributerGoodsId);
	
	void deleteBatch(Integer[] gbDistributerGoodsIds);

    List<GbDistributerGoodsEntity> queryGoodsByParamsGb(Map<String, Object> map);

	int queryGbGoodsTotal(Map<String, Object> map3);

	List<GbDistributerGoodsEntity> queryDisGoodsByParams(Map<String, Object> map);

	List<GbDistributerGoodsEntity> queryAddDistributerNxGoods(Map<String, Object> map);

    GbDistributerGoodsEntity queryGbDisGoodsDetail(Integer disGoodsId);

    List<GbDistributerGoodsEntity> queryDgSubNameByFatherIdGb(Integer gbDistributerFatherGoodsId);


	List<GbDistributerGoodsEntity> queryGbDisGoodsQuickSearchStr(Map<String, Object> map);

    int queryGbStockGoodsTotal(Map<String, Object> map3);

    List<GbDistributerFatherGoodsEntity> queryDisFatherGoodsByParams(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryDisShelfGoodsWithParams(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryGbDisUnShlefGoodsQuickSearchStr(Map<String, Object> map);

    List<GbDistributerGoodsEntity> querySubNameByFatherId(Integer gbDistributerFatherGoodsId);

    int queryDisGoodsCount(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryPurchaserDisGoodsByParams(Map<String, Object> map);

    GbDistributerGoodsEntity queryDisGoodsWithDepDisGoods(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryUpdateGoodsByParams(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryDisGoodsWithShelfGoods(Integer depId);

    List<GbDistributerGoodsEntity> querydisGoodsByNxGoodsId(Integer nxGoodsId);

    List<GbDistributerEntity> queryGbDisByNxGoodsId(Integer nxGoodsId);

    GbDistributerGoodsEntity queryLinshiGoods(Integer lsGoodsId);

    List<GbDistributerGoodsEntity> queryDisGoodsQuickSearchStrWithDepOrdersGb(Map<String, Object> map);
}
