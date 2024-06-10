package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 03-28 17:06
 */

import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.entity.NxResComGoodsDailyEntity;
import com.nongxinle.entity.NxRestrauntComGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxResComGoodsDailyDao extends BaseDao<NxResComGoodsDailyEntity> {

    NxResComGoodsDailyEntity queryDailyGoodsByParams(Map<String, Object> map);

    List<NxCommunityGoodsEntity> queryResDailyStatistics(Map<String, Object> map);
}
