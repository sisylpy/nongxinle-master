package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-14 12:03
 */

import com.nongxinle.entity.NxDistributerGoodsShelfStockReduceEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerGoodsShelfStockReduceDao extends BaseDao<NxDistributerGoodsShelfStockReduceEntity> {

    List<NxDistributerGoodsShelfStockReduceEntity> queryReduceListByParams(Map<String, Object> map);
}
