package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 03-26 18:31
 */

import com.nongxinle.entity.GbDistributerFoodEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerFoodDao extends BaseDao<GbDistributerFoodEntity> {

    List<GbDistributerFoodEntity> queryFoodByParams(Map<String, Object> map);

    List<GbDistributerFoodEntity> queryDisAllFood(Map<String, Object> map);
}
