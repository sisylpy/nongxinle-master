package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 08-19 12:35
 */

import com.nongxinle.entity.SysCityMarketEntity;

import java.util.List;
import java.util.Map;

public interface SysCityMarketService {
	
	SysCityMarketEntity queryObject(Integer sysCityMarketId);
	
	List<SysCityMarketEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(SysCityMarketEntity sysCityMarket);
	
	void update(SysCityMarketEntity sysCityMarket);
	
	void delete(Integer sysCityMarketId);
	
	void deleteBatch(Integer[] sysCityMarketIds);
}
