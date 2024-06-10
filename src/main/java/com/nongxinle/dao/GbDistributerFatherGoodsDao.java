package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-18 21:32
 */

import com.nongxinle.entity.GbDistributerFatherGoodsEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerFatherGoodsDao extends BaseDao<GbDistributerFatherGoodsEntity> {


    List<GbDistributerFatherGoodsEntity> queryDisAll(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> querySubFatherGoods(Integer goodsId);

    List<GbDistributerFatherGoodsEntity> queryHasDisFathersFather(Map<String, Object> map3);

    List<GbDistributerFatherGoodsEntity> queryDisGoodsCata(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryDisStockOrdersFatherGoods(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryDisFathersGoodsByParamsGb(Map<String, Object> mapGrand);

    List<GbDistributerFatherGoodsEntity> queryDisGoodsCataWithGoods(Map<String, Object> map);


    GbDistributerFatherGoodsEntity queryAppFatherGoods(Map<String, Object> map);

    List<GbDistributerFatherGoodsEntity> queryDisFathersGoodsByNxGoodsId(Integer nxGoodsId);
}
