package com.nongxinle.dao;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 08-19 09:57
 */

import com.nongxinle.entity.GbDistributerGoodsShelfGoodsEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerGoodsShelfGoodsDao extends BaseDao<GbDistributerGoodsShelfGoodsEntity> {

    List<GbDistributerGoodsShelfGoodsEntity> queryShelfGoodsByParams(Map<String, Object> map);
}
