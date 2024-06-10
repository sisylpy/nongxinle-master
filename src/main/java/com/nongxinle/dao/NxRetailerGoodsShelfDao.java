package com.nongxinle.dao;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-22 15:25
 */

import com.nongxinle.entity.NxRetailerGoodsShelfEntity;

import java.util.List;
import java.util.Map;


public interface NxRetailerGoodsShelfDao extends BaseDao<NxRetailerGoodsShelfEntity> {

    List<NxRetailerGoodsShelfEntity> queryRetShelfByParams(Map<String, Object> map);

    List<NxRetailerGoodsShelfEntity> queryRetShelfWithPurGoodsByParams(Map<String, Object> map);
}
