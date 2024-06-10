package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 02-23 21:44
 */

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbDepartmentGoodsStockReduceDailyEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;


public interface GbDepartmentGoodsStockReduceDailyDao extends BaseDao<GbDepartmentGoodsStockReduceDailyEntity> {

    List<GbDepartmentGoodsStockReduceDailyEntity> queryDepGoodsStockReduceDailyListByParams(Map<String, Object> map);

    int queryListTotal(Map<String, Object> map1);

    Double queryRestWeightTotal(Map<String, Object> map1);

    GbDepartmentGoodsStockReduceDailyEntity queryReduceDailyItem(Map<String, Object> map1);

    Integer queryReduceDailyCount(Map<String, Object> map1222);

    TreeSet<GbDistributerFatherGoodsEntity> queryReduceGoodsFatherTypeByParams(Map<String, Object> map1222);

    Double queryReduceProfitSubtotal(Map<String, Object> map1222);

    Double queryReduceSellingSubtotal(Map<String, Object> map1222);

    TreeSet<GbDistributerGoodsEntity> queryGoodsStockRecordTreeByParams(Map<String, Object> map);

    TreeSet<GbDepartmentEntity> queryWhichDepsHaveDailyTotal(Map<String, Object> mapDep);

    Double queryReduceFinishCount(Map<String, Object> map0);
}
