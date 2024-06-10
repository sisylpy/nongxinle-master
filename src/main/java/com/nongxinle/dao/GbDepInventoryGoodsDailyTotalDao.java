package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 02-18 14:22
 */

import com.nongxinle.entity.GbDepInventoryGoodsDailyTotalEntity;
import com.nongxinle.entity.GbDepartmentEntity;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;


public interface GbDepInventoryGoodsDailyTotalDao extends BaseDao<GbDepInventoryGoodsDailyTotalEntity> {

    GbDepInventoryGoodsDailyTotalEntity queryDailyTotalItemByParams(Map<String, Object> map);

    Integer queryDailyTotalAccount(Map<String, Object> map);

    List<GbDepInventoryGoodsDailyTotalEntity> queryDailyTotalListByParams(Map<String, Object> map);

    Double queryDailyTotalProduceWeight(Map<String, Object> map);

    TreeSet<GbDepartmentEntity> queryWhichDepsProduceWeight(Map<String, Object> mapDep);

    Double queryDailyTotalProduceWeightTotal(Map<String, Object> mapdepTotal);

    Double queryDailyTotalProfitTotal(Map<String, Object> map0);

    Double queryDailyTotalLossTotal(Map<String, Object> map);

    Double queryDailyTotalWasteTotal(Map<String, Object> map);

    Double queryDailyTotalLossWeight(Map<String, Object> map);

    Double queryDailyTotalWasteWeight(Map<String, Object> map);

}
