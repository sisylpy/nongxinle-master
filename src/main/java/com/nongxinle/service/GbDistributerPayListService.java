package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 02-12 21:10
 */

import com.nongxinle.entity.GbDistributerPayListEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerPayListService {
	
	GbDistributerPayListEntity queryObject(Integer gbDistributerPayListId);
	
	List<GbDistributerPayListEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerPayListEntity gbDistributerPayList);
	
	void update(GbDistributerPayListEntity gbDistributerPayList);
	
	void delete(Integer gbDistributerPayListId);
	
	void deleteBatch(Integer[] gbDistributerPayListIds);

    List<GbDistributerPayListEntity> queryPayListListByParams(Map<String, Object> map);

	int queryDisPayListCount(Map<String, Object> mapCount);

    int queryDisRecordSecondsTotal(Map<String, Object> map);
}
