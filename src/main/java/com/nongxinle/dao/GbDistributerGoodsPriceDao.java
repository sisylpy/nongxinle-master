package com.nongxinle.dao;

/**
 *
 *
 * @author lpy
 * @date 10-03 09:29
 */

import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsPriceEntity;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;


public interface GbDistributerGoodsPriceDao extends BaseDao<GbDistributerGoodsPriceEntity> {

    List<GbDistributerGoodsPriceEntity> queryPriceGoodsListByParams(Map<String, Object> map);

    Double queryPriceWhatTotal(Map<String, Object> map1);

    int queryPriceWhatAmount(Map<String, Object> map1);

    TreeSet<GbDistributerFatherGoodsEntity> queryTreePriceGoodsList(Map<String, Object> map0);

    Double queryPurTotal(Map<String, Object> map0);

    Double queryPriceHighestTotal(Map<String, Object> map0);

    Double queryPriceLowestTotal(Map<String, Object> map0);

    Integer queryDisPriceGoodsCount(Map<String, Object> map0);

    Double queryPriceWhatScale(Map<String, Object> map);

    List<GbDistributerGoodsEntity> queryTreeSetDisGoods(Map<String, Object> map);

    Double queryPricePurWeight(Map<String, Object> map1);

    GbDistributerGoodsPriceEntity queryItemByPurGoodsId(Integer purchaseGoodsId);
}
