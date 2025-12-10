package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 08-19 19:02
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;


public interface GbDepartmentGoodsStockDao extends BaseDao<GbDepartmentGoodsStockEntity> {

    List<GbDepartmentGoodsStockEntity> queryDepStockListByParams(Map<String, Object> mapGoods);

    List<GbDepartmentGoodsStockEntity> queryGoodsStockByParams(Map<String, Object> map);

    List<GbDepartmentGoodsStockEntity> queryGoodsStockByParamsWithDetail(Map<String, Object> map);

    List<GbDepartmentGoodsStockEntity> queryGoodsStockWithReduceList(Map<String, Object> map);


    List<GbDistributerGoodsEntity> queryDisGoodsWithFromDepGoods(Map<String, Object> map0);
    List<GbDistributerGoodsEntity> queryDisGoodsStockByParams(Map<String, Object> map);


    List<GbDistributerFatherGoodsEntity> queryDepStockTreeFatherGoodsByParams(Map<String, Object> map);


    TreeSet<GbDepartmentEntity> queryDepGoodsTreeDepartments(Map<String, Object> map2);

    List<GbDepartmentEntity> queryWhichDepHasStock(Map<String, Object> map);


    GbDepartmentGoodsStockEntity queryMinFullTimeForDayStock(Map<String, Object> map);
    GbDepartmentGoodsStockEntity queryMaxFullTimeForDayStock(Map<String, Object> map);

    List<GbDistributerGoodsShelfEntity> queryEveryDayOutStockShelfGoods(Map<String, Object> map1);

    GbDepartmentGoodsStockEntity queryReturnStockItemByOrderId(Integer gbDepartmentOrdersId);

    Integer queryGoodsStockCount(Map<String, Object> map14);

    Double queryDepGoodsRestTotal(Map<String, Object> map5);

    Double queryDepGoodsSubtotal(Map<String, Object> map4);

    Double queryDepGoodsRestWeightTotal(Map<String, Object> map1);

    long queryGoodsStockTimeStamp(Map<String, Object> map0);

    Double queryDepGoodsSellingSubtotal(Map<String, Object> map2);

    Double queryStockSellingPriceTotal(Map<String, Object> map);

    Double queryStockPriceTotal(Map<String, Object> map);

    Double queryDepStockWeightTotal(Map<String, Object> map);

    Double queryDepStockLossWeightTotal(Map<String, Object> map);

    Double queryDepStockWasteWeightTotal(Map<String, Object> map);

    Double queryDepStockRestWeightTotal(Map<String, Object> map);

    Double queryStockCostRateTotal(Map<String, Object> map);

    Double queryGoodsPriceTotal(Map<String, Object> map);

    Double queryGoodsPriceScale(Map<String, Object> map);

    Double queryDepStockRestSubtotal(Map<String, Object> map);

    String queryDepStockMaxDgsPrice(Map<String, Object> map);

    String queryDepStockMinDgsPrice(Map<String, Object> map);

    double queryDepStockProfitSubtotal(Map<String, Object> map);

    double queryDepStockAfterProfitSubtotal(Map<String, Object> map);

    double queryDepStockProduceSellingSubtotal(Map<String, Object> map);

    double queryDepStockReturnSubtotal(Map<String, Object> map);

    Double queryDepStockReturnWeightTotal(Map<String, Object> disGoodsMap);

    Double queryDepStockSubtotal(Map<String, Object> mapDisGoods);

    Double queryDepGoodsWasteTotal(Map<String, Object> map0);

    int queryGoodsStockStars(Map<String, Object> map1);

    Integer queryGoodsStarsTimes(Map<String, Object> map);

    Double queryDepStockProduceWeightTotal(Map<String, Object> mapS);

    Double queryDepGoodsWasteWeightTotal(Map<String, Object> mapS);

    double queryGoodsLatestWeight(Map<String, Object> map);

    double queryGoodsLatestWasteWeight(Map<String, Object> map);

    Integer queryDisStockGoodsCount(Map<String, Object> map);


}
