package com.nongxinle.dao;

/**
 *
 *
 * @author lpy
 * @date 09-06 15:27
 */

import com.nongxinle.entity.GbDepInventoryWeekEntity;

import java.util.List;
import java.util.Map;


public interface GbDepInventoryWeekDao extends BaseDao<GbDepInventoryWeekEntity> {

    GbDepInventoryWeekEntity queryInventoryWeek(Map<String, Object> map1);

    List<GbDepInventoryWeekEntity> queryDepWeekList(Map<String, Object> map);

    Double queryInventoryWeekTotal(Map<String, Object> map);

    Double queryInventoryWeekWasteTotal(Map<String, Object> map);

    Double queryInventoryWeekLossTotal(Map<String, Object> map);

    List<GbDepInventoryWeekEntity> queryInventoryWeekListByParams(Map<String, Object> map4);

    Double queryInventoryWeekReturnTotal(Map<String, Object> map);
}
