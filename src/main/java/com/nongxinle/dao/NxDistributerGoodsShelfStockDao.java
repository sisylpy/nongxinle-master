package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-14 12:03
 */

import com.nongxinle.entity.NxDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfStockEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerGoodsShelfStockDao extends BaseDao<NxDistributerGoodsShelfStockEntity> {

    List<NxDistributerGoodsShelfStockEntity> queryShelfStockListByParams(Map<String, Object> map);
    
    Integer queryShelfStockCount(Map<String, Object> map);
    
    Double queryShelfStockRestTotal(Map<String, Object> map);

    Double queryShelfStockRestWeightTotal(Map<String, Object> map);

    // 库存商品查询新增方法
    Integer queryStockGoodsCount(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryGoodsStockList(Map<String, Object> map);

    Integer queryGoodsStockCount(Map<String, Object> map);

    List<NxDistributerGoodsShelfStockEntity> queryStockByDisGoodsIdAndDate(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryStockTreeFatherGoodsByParams(Map<String, Object> map);
    
    List<NxDistributerGoodsShelfStockEntity> queryStockListByDepFatherId(Map<String, Object> map);
    
    Integer queryStockCountByDepFatherId(Map<String, Object> map);
}
