package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 03-28 17:06
 */

import com.nongxinle.entity.NxResComGoodsWeekEntity;

import java.util.Map;


public interface NxResComGoodsWeekDao extends BaseDao<NxResComGoodsWeekEntity> {

    NxResComGoodsWeekEntity queryWeekGoodsByParams(Map<String, Object> map2);
}
