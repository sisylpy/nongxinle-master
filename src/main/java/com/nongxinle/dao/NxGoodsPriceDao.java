package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 03-09 08:28
 */

import com.nongxinle.entity.NxGoodsPriceEntity;

import java.util.Map;


public interface NxGoodsPriceDao extends BaseDao<NxGoodsPriceEntity> {

    NxGoodsPriceEntity queryPriceGoodsByParams(Map<String, Object> map);

    double queryLowestPriceByParams(Map<String, Object> mapL);

    double queryHighestPriceByParams(Map<String, Object> mapL);

    int queryPriceGoodsCount(Map<String, Object> mapL);
}
