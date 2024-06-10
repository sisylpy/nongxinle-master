package com.nongxinle.dao;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-09 18:47
 */

import com.nongxinle.entity.NxDistributerGoodsShelfGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerGoodsShelfGoodsDao extends BaseDao<NxDistributerGoodsShelfGoodsEntity> {

    List<NxDistributerGoodsShelfGoodsEntity> queryShelfGoodsByParams(Map<String, Object> map);
}
