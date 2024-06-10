package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 09-06 15:27
 */

import com.nongxinle.entity.GbDepInventoryDailyEntity;

import java.util.List;
import java.util.Map;


public interface GbDepInventoryDailyDao extends BaseDao<GbDepInventoryDailyEntity> {

    GbDepInventoryDailyEntity queryInventoryDaily(Map<String, Object> map);

    List<GbDepInventoryDailyEntity> queryDepDailyList(Map<String, Object> map);

    Double queryInventoryDailyTotal(Map<String, Object> map);

    Double queryInventoryDailyWasteTotal(Map<String, Object> map);

    Double queryInventoryDailyLossTotal(Map<String, Object> map);

    List<GbDepInventoryDailyEntity> queryInventoryDailyListByParams(Map<String, Object> map3);

    Double queryInventoryDailyReturnTotal(Map<String, Object> map);
}
