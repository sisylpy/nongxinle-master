package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-18 21:32
 */

import com.nongxinle.entity.GbDistributerStandardEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerStandardService {
	
	GbDistributerStandardEntity queryObject(Integer gbDistributerStandardId);
	
	List<GbDistributerStandardEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerStandardEntity gbDistributerStandard);
	
	void update(GbDistributerStandardEntity gbDistributerStandard);
	
	void delete(Integer gbDistributerStandardId);
	
	void deleteBatch(Integer[] gbDistributerStandardIds);

    List<GbDistributerStandardEntity> queryDisStandardByDisGoodsIdGb(Integer disGoodsId);
}
