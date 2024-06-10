package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 03-28 17:06
 */

import com.nongxinle.entity.NxResComGoodsMonthEntity;

import java.util.Map;


public interface NxResComGoodsMonthDao extends BaseDao<NxResComGoodsMonthEntity> {

    NxResComGoodsMonthEntity queryMonthGoodsByParams(Map<String, Object> map3);
}
