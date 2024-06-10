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

    List<GbDepartmentGoodsStockEntity> queryGoodsStockByParams(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryDisGoodsStockByParams(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryDisGoodsStockDetailByParams(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryDepGoodsStockByParams(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> getDepartmentDisGoodsStock(Map<String, Object> map);

    Integer queryGoodsStockCount(Map<String, Object> map14);

    Double queryDepGoodsRestTotal(Map<String, Object> map5);

    TreeSet<GbDepartmentEntity> queryDepGoodsTreeDepartments(Map<String, Object> map2);

    List<GbDepartmentGoodsStockEntity> queryStockListByParams(Integer depFatherId);

    Double queryDepGoodsSubtotal(Map<String, Object> map4);

    GbDepartmentGoodsStockEntity queryMinFullTimeForDayStock(Map<String, Object> map);

    List<GbDistributerGoodsShelfEntity> queryEveryDayOutStockShelfGoods(Map<String, Object> map1);

    GbDepartmentGoodsStockEntity queryMaxFullTimeForDayStock(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryDepStockDisFatherGoodsFather(Map<String, Object> map);

//    List<GbDepartmentGoodsStockEntity> queryDepGoodsWasteList(Map<String, Object> map3);

    List<GbDepartmentGoodsStockEntity> queryGoodsStockWithReduceList(Map<String, Object> map);

    Double queryDepGoodsRestWeightTotal(Map<String, Object> map1);

    long queryGoodsStockTimeStamp(Map<String, Object> map0);

    Double queryDepGoodsSellingSubtotal(Map<String, Object> map2);

    List<GbDepartmentGoodsStockEntity> queryGoodsStockReduceByParams(Map<String, Object> map);

//    TreeSet<GbDistributerFatherGoodsEntity> queryDepStockTreeFatherGoodsByParams(Map<String, Object> map);
    List<GbDistributerFatherGoodsEntity> queryDepStockTreeFatherGoodsByParams(Map<String, Object> map);

    Double queryStockSellingPriceTotal(Map<String, Object> map);

    Double queryStockPriceTotal(Map<String, Object> map);

//    Double queryDepStockProduceWeightTotal(Map<String, Object> map);

    Double queryDepStockWeightTotal(Map<String, Object> map);

    Double queryDepStockLossWeightTotal(Map<String, Object> map);

    Double queryDepStockWasteWeightTotal(Map<String, Object> map);

    Double queryDepStockRestWeightTotal(Map<String, Object> map);

    List<GbDepartmentEntity> queryWhichDepHasStock(Map<String, Object> map);

    Double queryStockCostRateTotal(Map<String, Object> map);

    Double queryGoodsPriceTotal(Map<String, Object> map);

    Double queryGoodsPriceScale(Map<String, Object> map);

//    Double queryDepStockProduceSubtotal(Map<String, Object> map);

//    Double queryDepStockWasteSubtotal(Map<String, Object> map);

//    Double queryDepStockLossSubtotal(Map<String, Object> map);

    Double queryDepStockRestSubtotal(Map<String, Object> map);

    String queryDepStockMaxDgsPrice(Map<String, Object> map);

    String queryDepStockMinDgsPrice(Map<String, Object> map);

    double queryDepStockProfitSubtotal(Map<String, Object> map);

    double queryDepStockAfterProfitSubtotal(Map<String, Object> map);

    double queryDepStockProduceSellingSubtotal(Map<String, Object> map);

    List<GbDepartmentGoodsStockEntity> queryDepStockListByParams(Map<String, Object> mapGoods);

    GbDepartmentGoodsStockEntity queryReturnStockItemByOrderId(Integer gbDepartmentOrdersId);

    double queryDepStockReturnSubtotal(Map<String, Object> map);

    Double queryDepStockReturnWeightTotal(Map<String, Object> disGoodsMap);

    List<GbDistributerGoodsEntity> queryDisGoodsWithFromDepGoods(Map<String, Object> map0);

    Double queryDepStockSubtotal(Map<String, Object> mapDisGoods);

    List<GbDepartmentEntity> queryStockDepWithFatherGoods(Map<String, Object> map0);
}
