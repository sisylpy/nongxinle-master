package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-18 21:32
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;


public interface GbDistributerGoodsDao extends BaseDao<GbDistributerGoodsEntity> {

    List<GbDistributerGoodsEntity> queryGoodsByParamsGb(Map<String, Object> map);

    int queryGbGoodsTotal(Map<String, Object> map3);

    List<GbDistributerGoodsEntity> queryDisGoodsByParams(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryAddDistributerNxGoods(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryDisGoodsHasNxGoodsFather(Map<String, Object> map);

    GbDistributerGoodsEntity queryGbDisGoodsDetail(Integer disGoodsId);

    List<GbDistributerGoodsEntity> queryDgSubNameByFatherIdGb(Integer gbDistributerFatherGoodsId);

    List<GbDistributerGoodsEntity> depQueryDisGoodsWithOrdersByFatherIdGb(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryDisGoodsWithSupplierByParams(Map<String, Object> map);

    List<NxDistributerEntity> querySupplierOrderDisGoods(Map<String, Object> map4);

    List<GbDistributerGoodsEntity> queryDisGoodsByIds(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryGbDisGoodsQuickSearchStr(Map<String, Object> map);

    int queryGbStockGoodsTotal(Map<String, Object> map3);

    List<GbDistributerFatherGoodsEntity> queryDisFatherGoodsByParams(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryDisShelfGoodsWithParams(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryGbDisUnShlefGoodsQuickSearchStr(Map<String, Object> map);

    List<GbDistributerGoodsEntity> querySubNameByFatherId(Integer gbDistributerFatherGoodsId);

    List<GbDistributerGoodsEntity> queryDisGoodsQuickSearchStrWithDepOrdersGb(Map<String, Object> map);

    int queryDisGoodsCount(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryPurchaserDisGoodsByParams(Map<String, Object> map);

    GbDistributerGoodsEntity queryDisGoodsWithDepDisGoods(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryUpdateGoodsByParams(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryDisGoodsWithShelfGoods(Integer depId);

    List<GbDistributerGoodsEntity> querydisGoodsByNxGoodsId(Integer nxGoodsId);

    List<GbDistributerEntity> queryGbDisByNxGoodsId(Integer nxGoodsId);

    GbDistributerGoodsEntity queryGbGoodsByNxGoodsId(Integer lsGoodsId);

    List<GbDepartmentEntity> queryOutDepsByFatherId(Map<String, Object> map);

    List<GbDistributerGoodsEntity> querySupplierGoodsByGreatIdGb(Map<String, Object> map);

    int queryLinshiGoodsAcount(Integer gbDistributerId);

    List<GbDistributerGoodsEntity> queryDisGoodsByName(Map<String, Object> mapZero);

    List<GbDistributerGoodsEntity> queryDisGoodsByLikeName(Map<String, Object> mapOne);

    List<GbDistributerGoodsEntity> queryDisGoodsByNamePinyin(Map<String, Object> mapTwo);

    List<GbDistributerGoodsEntity> queryDisGoodsByAlias(Map<String, Object> mapA);

    List<Integer> queryOnlyDisGoodsIds(Map<String, Object> map);

    GbDistributerGoodsEntity queryDisGoodsDetail(Integer nxDdgDisGoodsId);
}
