package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-22 17:52
 */

import com.nongxinle.entity.GbDepartmentGoodsStockEntity;
import com.nongxinle.entity.GbDepartmentGoodsStockRecordEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;


public interface GbDepartmentGoodsStockRecordDao extends BaseDao<GbDepartmentGoodsStockRecordEntity> {

    Integer queryGoodsStockRecordCount(Map<String, Object> map1451);

    Double queryGoodsStockRecordSubtotal(Map<String, Object> map42);

    List<GbDepartmentGoodsStockRecordEntity> queryGoodsStockListByParams(Map<String, Object> map5);


    Double queryGoodsStockRecordWasteSubtotal(Map<String, Object> map1);

    Double queryGoodsStockRecordLossSubtotal(Map<String, Object> map3);

    Double queryManyTotal(Map<String, Object> map0);

    List<GbDistributerFatherGoodsEntity> queryDepStockRecordDisFatherGoodsFather(Map<String, Object> map0);

    List<GbDistributerGoodsEntity> queryDisGoodsByParams(Map<String, Object> map);

    Double queryGoodsStockRecordWeightTotal(Map<String, Object> map1);

    List<GbDepartmentGoodsStockRecordEntity> queryDepGoodsStockRecordDetailByParams(Map<String, Object> map);
}
