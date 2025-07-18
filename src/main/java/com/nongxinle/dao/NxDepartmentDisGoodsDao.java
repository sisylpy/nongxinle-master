package com.nongxinle.dao;

/**
 *
 *
 * @author lpy
 * @date 07-30 23:58
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;


public interface NxDepartmentDisGoodsDao extends BaseDao<NxDepartmentDisGoodsEntity> {

    List<NxDepartmentEntity> queryDepartmentsByDisGoodsId(Integer disGoodsId);

    List<NxDistributerFatherGoodsEntity> depGetDepDisGoodsCata(Map<String, Object> map);

    List<NxDepartmentDisGoodsEntity> queryDepGoodsByFatherId(Map<String, Object> map);

    List<NxDepartmentDisGoodsEntity> queryDisDepGoods(Map<String, Object> map);

    int queryDisGoodsTotal(Map<String, Object> map3);

    TreeSet<NxDepartmentDisGoodsEntity> queryDepDisGoodsQuickSearchStr(Map<String, Object> map);

    List<NxDepartmentDisGoodsEntity> queryDepDisSearchPinyin(Map<String, Object> map);

    List<NxDepartmentDisGoodsEntity> queryDepDisGoodsByParams(Map<String, Object> map);


    List<NxDistributerFatherGoodsEntity> disGetDepGoodsCata(Integer depFatherId);

    List<NxDistributerFatherGoodsEntity> depQueryDepGoodsWithOrder(Map<String, Object> map);

    NxDepartmentEntity depFatherGetSubDepsGoods(Map<String, Object> map);

    List<NxDepartmentDisGoodsEntity> depGetDepsGoods(Map<String, Object> map);

    List<GbDepartmentEntity> queryGbDepartmentsByDisGoodsId(Integer disGoodsId);

    List<NxDistributerFatherGoodsEntity> queryDepDisGoodsWithOrders(Map<String, Object> mapDep);

    List<NxDepartmentDisGoodsEntity> queryDepartmentGoods(Map<String, Object> mapDep);

    List<NxDistributerFatherGoodsEntity> queryGbDisGbDepGoods(Map<String, Object> map);

    List<NxDepartmentDisGoodsEntity> queryDepDisGoodsOrders(Map<String, Object> map);

    int queryDepGoodsCount(Map<String, Object> map);


    List<NxDepartmentDisGoodsEntity> queryWenti();

    List<Integer> queryOnlyDepGoodsIds(Map<String, Object> map);

    List<NxDepartmentDisGoodsEntity> queryDepDisGoodsOrdersForAi(Map<String, Object> map);

    NxDepartmentDisGoodsEntity queryDepartmentGoodsOnly(Map<String, Object> map);
}
