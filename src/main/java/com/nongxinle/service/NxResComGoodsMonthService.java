package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 03-28 17:06
 */

import com.nongxinle.entity.NxResComGoodsMonthEntity;

import java.util.List;
import java.util.Map;

public interface NxResComGoodsMonthService {
	
	NxResComGoodsMonthEntity queryObject(Integer nxResComGoodsMonthId);
	
	List<NxResComGoodsMonthEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxResComGoodsMonthEntity nxResComGoodsMonth);
	
	void update(NxResComGoodsMonthEntity nxResComGoodsMonth);
	
	void delete(Integer nxResComGoodsMonthId);
	
	void deleteBatch(Integer[] nxResComGoodsMonthIds);

    NxResComGoodsMonthEntity queryMonthGoodsByParams(Map<String, Object> map3);
}
