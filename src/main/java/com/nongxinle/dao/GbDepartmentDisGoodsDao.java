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

    List<GbDistributerFatherGoodsEntity> disGetDepDisGoodsCataGb(Integer depFatherId);


    GbDepartmentDisGoodsEntity queryDepGoodsItemByParams(Map<String, Object> map1);

    List<GbDepartmentDisGoodsEntity> depQueryDepGoodsWithOrderDepGoods(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryDepTypeFatherGoods(Map<String, Object> mapD);
}
