package com.nongxinle.dao;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 08-19 09:57
 */

import com.nongxinle.entity.GbDepartmentDisGoodsEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsShelfEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerGoodsShelfDao extends BaseDao<GbDistributerGoodsShelfEntity> {


    List<GbDistributerFatherGoodsEntity> queryShelfByParamsWithStock(Map<String, Object> map);

    GbDistributerGoodsShelfEntity queryShelfGoodsByParams(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> stockRoomGetShelfGoodsGb(Map<String, Object> map);

    List<GbDistributerGoodsEntity>  queryShelfInventoryGoodsByParams(Map<String, Object> map);

    List<GbDistributerGoodsShelfEntity> queryStockOrdersByParams(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryShelfInventoryDepGoodsByParams(Map<String, Object> map);

    List<GbDistributerGoodsShelfEntity> queryShelfList(Map<String, Object> map);

    List<GbDistributerGoodsShelfEntity> queryShelfGoodsWithPurOrder(Map<String, Object> map);

    List<GbDistributerGoodsShelfEntity> queryStockOrdersByParamsWithOrder(Map<String, Object> map);
}
