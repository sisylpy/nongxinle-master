package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-21 21:51
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;


public interface GbDepartmentOrdersDao extends BaseDao<GbDepartmentOrdersEntity> {

    List<GbDepartmentOrdersEntity> queryDisOrdersByParams(Map<String, Object> map);

    List<GbDepartmentEntity> queryDistributerTodayDepartments(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity>  disGetUnPlanPurchaseApplys(Map<String, Object> map);

    List<GbDepartmentOrdersEntity> queryOrdersForDisGoods(Map<String, Object> map1);

    int queryTotalByParams(Map<String, Object> map);

    List<GbDepartmentOrdersEntity> disQueryDisOrdersByParams(Map<String, Object> map);

    GbDepartmentOrdersEntity queryGbOrderByNxOrderId(Integer nxDepartmentOrdersId);

    List<GbDepartmentOrdersEntity> queryMendianDisOrdersByParams(Map<String, Object> map3);

    List<GbDepartmentOrdersEntity> queryOrdersByBillId(Map<String, Object> map);

    List<GbDepartmentOrdersEntity> queryDisOrdersListByParams(Map<String, Object> map);

    List<GbDepartmentEntity> queryFatherDepartment(Map<String, Object> map14);

    List<NxDistributerEntity> queryGbDepNxDistributerOrder(Map<String, Object> map4);

    List<GbDistributerGoodsShelfEntity> queryWeightGoodsByParams(Map<String, Object> map);

    Integer queryGbDepartmentOrderAmount(Map<String, Object> map);

    Double queryGbOrdersSubtotal(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> disGetPrintedWeightApplys(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryFatherGoods(Map<String, Object> map1);

    List<GbDistributerGoodsEntity> disGetTodayGoodsOrder(Map<String, Object> map);

    Double queryGbOrdersSellingSubtotal(Map<String, Object> map2);

    List<GbDistributerFatherGoodsEntity> stockGetDepApply(Map<String, Object> map3);

    List<GbDistributerGoodsShelfEntity> disGetUnPlanPurchaseApplysStock(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> disGetPrintedPurGoodsApply(Map<String, Object> map);

    Integer queryOrdersDisGoodsAcount(Map<String, Object> map);

    List<GbDepartmentOrdersEntity> queryPeisongOrdersByParams(Map<String, Object> map);

    GbDepartmentOrdersEntity queryReturnOrderByReduceId(Integer gbDepartmentGoodsStockReduceId);

}
