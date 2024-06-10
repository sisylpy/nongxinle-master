package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 08-23 14:08
 */

import com.nongxinle.entity.GbDepInventoryGoodsMonthEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;


public interface GbDepInventoryGoodsMonthDao extends BaseDao<GbDepInventoryGoodsMonthEntity> {

    List<GbDepInventoryGoodsMonthEntity> queryMonthStockByParams(Map<String, Object> map);

    Integer queryMonthGoodsInventoryCount(Map<String, Object> map24);

    Double queryMonthGoodsTotal(Map<String, Object> map6);

    List<GbDepInventoryGoodsMonthEntity> queryMonthStockListByParams(Map<String, Object> map2);

    Double queryMonthGoodsLossTotal(Map<String, Object> map6);

    Double queryMonthGoodsWasteTotal(Map<String, Object> map6);

    Double queryMonthGoodsReturnTotal(Map<String, Object> map6);

    GbDepInventoryGoodsMonthEntity queryDepMonthStockByParams(Map<String, Object> map1);

    TreeSet<GbDistributerFatherGoodsEntity> queryTreeMonthGoodsList(Map<String, Object> map2);

    Double queryMonthGoodsProduceTotal(Map<String, Object> map2);
}
