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
import java.util.TreeSet;


public interface GbDepartmentDisGoodsDao extends BaseDao<GbDepartmentDisGoodsEntity> {

    List<GbDepartmentDisGoodsEntity> queryGbDepDisGoodsByParams(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> depQueryDepGoodsWithOrderGb(Map<String, Object> map);

    List<GbDepartmentDisGoodsEntity> depGetDepsGoodsGb(Map<String, Object> map);

    int queryGbDisGoodsTotal(Map<String, Object> map3);

    TreeSet<GbDepartmentDisGoodsEntity> queryDepDisGoodsQuickSearchStrGb(Map<String, Object> map1);

    List<GbDistributerFatherGoodsEntity> disGetDepDisGoodsCataGb(Map<String, Object> map);


    GbDepartmentDisGoodsEntity queryDepGoodsItemByParams(Map<String, Object> map1);

    List<GbDepartmentDisGoodsEntity> depQueryDepGoodsWithOrderDepGoods(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryDepTypeFatherGoods(Map<String, Object> mapD);

    List<GbDistributerFatherGoodsEntity> selfMendiainGetDepDisGoodsCata(Map<String, Object> mapD);

    List<GbDistributerFatherGoodsEntity> selfMendiainGetDepDisGoodsCataWithGoods(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryDepFatherGoodsByParams(Map<String, Object> mapG);

    List<GbDistributerFatherGoodsEntity> depQueryDepGoodsWithOrderGbNew(Map<String, Object> map);

    List<GbDepartmentDisGoodsEntity> queryDepDisGoodsByParams(Map<String, Object> map);

    List<GbDepartmentDisGoodsEntity>  queryDepartmentGoods(Map<String, Object> map);

    List<GbDepartmentDisGoodsEntity> depQueryDepGoodsWithOrder(Map<String, Object> map);

    int queryDepGoodsCount(Map<String, Object> mapC);

    List<Integer> queryOnlyDepGoodsIds(Map<String, Object> map);

    List<GbDepartmentDisGoodsEntity> depQueryDepGoodsWithOrderForAi(Map<String, Object> map);

    GbDepartmentDisGoodsEntity queryDepartmentGoodsForAi(Map<String, Object> map);

    TreeSet<GbDistributerGoodsEntity> disQueryDisGoodsWithOrderForAiTree(Map<String, Object> map);

    List<Integer> queryOnlyDisGoodsIds(Map<String, Object> map);

    GbDepartmentDisGoodsEntity queryDepartmentGoodsOnly(Map<String, Object> map);


}
