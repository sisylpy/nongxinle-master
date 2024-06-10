package com.nongxinle.dao;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-22 15:25
 */

import com.nongxinle.entity.NxRetailerGoodsShelfGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxRetailerGoodsShelfGoodsDao extends BaseDao<NxRetailerGoodsShelfGoodsEntity> {

    List<NxRetailerGoodsShelfGoodsEntity> queryRetShelfGoodsByParams(Map<String, Object> map);

    List<NxRetailerGoodsShelfGoodsEntity> queryRetShelfGoodsWithPurchaseByParams(Map<String, Object> map);
}
