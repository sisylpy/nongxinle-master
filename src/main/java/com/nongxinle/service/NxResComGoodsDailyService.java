package com.nongxinle.service;

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

public interface NxResComGoodsDailyService {
	
	NxResComGoodsDailyEntity queryObject(Integer nxResComGoodsDailyId);
	
	List<NxResComGoodsDailyEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxResComGoodsDailyEntity nxResComGoodsDaily);
	
	void update(NxResComGoodsDailyEntity nxResComGoodsDaily);
	
	void delete(Integer nxResComGoodsDailyId);
	
	void deleteBatch(Integer[] nxResComGoodsDailyIds);

    NxResComGoodsDailyEntity queryDailyGoodsByParams(Map<String, Object> map);

	List<NxCommunityGoodsEntity> queryResDailyStatistics(Map<String, Object> map);
}
