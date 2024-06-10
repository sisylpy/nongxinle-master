package com.nongxinle.dao;

/**
 *
 *
 * @author lpy
 * @date 08-23 14:08
 */

import com.nongxinle.entity.GbDepInventoryGoodsDailyEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;


public interface GbDepInventoryGoodsDailyDao extends BaseDao<GbDepInventoryGoodsDailyEntity> {

    List<GbDepInventoryGoodsDailyEntity> queryDailyStockByParams(Map<String, Object> map);

    Double queryDailyGoodsTotal(Map<String, Object> map6);

    Integer queryDailyGoodsInventoryCount(Map<String, Object> map24);

    List<GbDepInventoryGoodsDailyEntity> queryDailyStockListByParams(Map<String, Object> map);

    Double queryDailyGoodsLossTotal(Map<String, Object> map6);

    Double queryDailyGoodsWasteTotal(Map<String, Object> map6);

    Double queryDailyGoodsReturnTotal(Map<String, Object> map6);

    TreeSet<GbDistributerFatherGoodsEntity> queryTreeDailyGoodsList(Map<String, Object> map1);

    Double queryDailyGoodsProduceTotal(Map<String, Object> map0);

    TreeSet<GbDepInventoryGoodsDailyEntity> queryTreeDailyDisGoodsList(Map<String, Object> map0);

    GbDepInventoryGoodsDailyEntity queryDailyStockItemByParams(Map<String, Object> map);

    Double queryDailyGoodsProduceWeight(Map<String, Object> map0);

    Double queryDailyGoodsProfit(Map<String, Object> map0);
}
