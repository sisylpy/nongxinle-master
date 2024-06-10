package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 08-23 14:08
 */

import com.nongxinle.entity.GbDepInventoryGoodsWeekEntity;
import com.nongxinle.entity.GbDepInventoryWeekEntity;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;


import com.nongxinle.entity.GbDepInventoryGoodsWeekEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;


public interface GbDepInventoryGoodsWeekDao extends BaseDao<GbDepInventoryGoodsWeekEntity> {

    List<GbDepInventoryGoodsWeekEntity> queryWeekStockByParams(Map<String, Object> map);

    Integer queryWeekGoodsInventoryCount(Map<String, Object> map24);

    Double queryWeekGoodsTotal(Map<String, Object> map6);

    List<GbDepInventoryGoodsWeekEntity> queryWeekStockListByParams(Map<String, Object> map1);

    Double queryWeekGoodsLossTotal(Map<String, Object> map6);

    Double queryWeekGoodsWasteTotal(Map<String, Object> map6);

    Double queryWeekGoodsReturnTotal(Map<String, Object> map6);

    TreeSet<GbDistributerFatherGoodsEntity> queryTreeWeekGoodsList(Map<String, Object> map0);

    Double queryWeekGoodsProduceTotal(Map<String, Object> map1);

    TreeSet<GbDepInventoryGoodsWeekEntity> queryTreeWeekDisGoodsList(Map<String, Object> map0);
}
