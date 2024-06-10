package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 03-28 17:06
 */

import com.nongxinle.entity.NxResComGoodsWeekEntity;

import java.util.List;
import java.util.Map;

public interface NxResComGoodsWeekService {
	
	NxResComGoodsWeekEntity queryObject(Integer nxResComGoodsWeekId);
	
	List<NxResComGoodsWeekEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxResComGoodsWeekEntity nxResComGoodsWeek);
	
	void update(NxResComGoodsWeekEntity nxResComGoodsWeek);
	
	void delete(Integer nxResComGoodsWeekId);
	
	void deleteBatch(Integer[] nxResComGoodsWeekIds);

    NxResComGoodsWeekEntity queryWeekGoodsByParams(Map<String, Object> map2);
}
