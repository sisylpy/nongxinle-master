package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 01-14 21:23
 */

import com.nongxinle.entity.NxCommunityStatisticsEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityStatisticsDao extends BaseDao<NxCommunityStatisticsEntity> {

    List<NxCommunityStatisticsEntity> queryStatisticsGoods(Map<String, Object> map1);

    List<NxCommunityStatisticsEntity> querySt(Map<String, Object> map1);

    float queryStWeekProfitSum(Map<String, Object> map);
}
