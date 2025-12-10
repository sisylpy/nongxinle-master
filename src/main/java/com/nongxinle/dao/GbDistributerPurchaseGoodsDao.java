package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-24 11:45
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;


public interface GbDistributerPurchaseGoodsDao extends BaseDao<GbDistributerPurchaseGoodsEntity> {

    int queryPurchaseGoodsTotal(Map<String, Object> map2);

    Integer queryGbPurchaseGoodsCount(Map<String, Object> map);

    Double queryPurchaseGoodsSubTotal(Map<String, Object> map);

    int queryPurchaseGoodsAmount(Map<String, Object> map);

    Double queryPurchaseInventoryGoodsSubTotal(Map<String, Object> map);

    Double queryPurchaseGoodsWeightTotal(Map<String, Object> map1);

    String queryPurchaseGoodsPrice(Map<String, Object> map1);

    String queryPurGoodsMaxPrice(Map<String, Object> map);

    String queryPurGoodsMinPrice(Map<String, Object> map);

    String queryPurchaseGoodsWeight(Map<String, Object> mapDay);

    int queryGbPurchaseOrderAmount(Map<String, Object> map1);

    int queryPurchaseGoodsOrderCount(Map<String, Object> map);

    Integer queryGbDisGoods(Map<String, Object> queryMap);


    List<GbDistributerFatherGoodsEntity> queryGbFatherDisPurchaseGoods(Map<String, Object> map4);

    List<GbDistributerPurchaseGoodsEntity> queryPurchaseGoodsWithDetailByParams(Map<String, Object> map);

    List<GbDistributerPurchaseGoodsEntity> querySimplePurGoods(Map<String, Object> map4);


    GbDistributerPurchaseGoodsEntity queryPurGoodsWithOrders(Integer id);

    List<GbDistributerGoodsEntity> queryDisTreeGoods(Map<String, Object> map);

    GbDistributerPurchaseGoodsEntity queryLastestItem(Map<String, Object> map);

    List<GbDistributerGoodsEntity> querySupplierGoods(Map<String, Object> queryMap);

    List<NxJrdhSupplierEntity> queryDisPurGoodsSupplierList(Map<String, Object> map);

    List<GbDistributerPurchaseGoodsEntity> queryOnlyPurGoods(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryGrandGoodsByDisGoods(Map<String, Object> map);

    List<GbDepartmentUserEntity> queryPurUserList(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryGbPurchaseGoodsTopTimes(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryGbPurchaseGoodsTopSubtotal(Map<String, Object> map);

    TreeSet<GbDistributerFatherGoodsEntity> queryPurchaseGreatGrand(Map<String, Object> map);

    double queryGbPurchaseSubtotalTopSubtotal(Map<String, Object> map);

    Integer queryGbDisGoodsTreeCount(Map<String, Object> queryMap);

    List<GbDistributerGoodsEntity> queryGbPurchaseGoodsTopPriceFluctuation(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryDisTreeGoodsWithPurList(Map<String, Object> queryMap);

    List<Map<String, Object>> debugQueryGoodsPriceData(Map<String, Object> map);

    int queryGbGoodsCount(Map<String, Object> map);

    GbDistributerPurchaseGoodsEntity queryPurchaseGoodsLastItem(Map<String, Object> mapG);

    List<GbDistributerGoodsEntity> queryReturnDisTreeGoodsWithPurList(Map<String, Object> map);
}
