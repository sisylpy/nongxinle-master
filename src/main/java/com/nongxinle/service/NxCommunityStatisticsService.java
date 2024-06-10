package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 01-14 21:23
 */

import com.nongxinle.entity.NxCommunityStatisticsEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityStatisticsService {
	
	NxCommunityStatisticsEntity queryObject(Integer nxCommunityStatisticsId);
	
	List<NxCommunityStatisticsEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityStatisticsEntity nxCommunityStatistics);
	
	void update(NxCommunityStatisticsEntity nxCommunityStatistics);
	
	void delete(Integer nxCommunityStatisticsId);
	
	void deleteBatch(Integer[] nxCommunityStatisticsIds);

	List<NxCommunityStatisticsEntity>  queryStatisticsGoods(Map<String, Object> map1);

    List<NxCommunityStatisticsEntity> querySt(Map<String, Object> map1);

    float queryStWeekProfitSum(Map<String, Object> map);
}
