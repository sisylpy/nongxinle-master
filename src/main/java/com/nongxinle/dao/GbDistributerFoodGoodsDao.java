package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 03-27 10:50
 */

import com.nongxinle.entity.GbDistributerFoodGoodsEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerFoodGoodsDao extends BaseDao<GbDistributerFoodGoodsEntity> {

    List<GbDistributerFoodGoodsEntity> queryFoodGoodsByParams(Map<String, Object> map);
}
