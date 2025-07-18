package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-20 11:05
 */

import com.nongxinle.entity.NxDistributerPayListEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerPayListService {
	
	NxDistributerPayListEntity queryObject(Integer nxDistributerPayListId);
	
	List<NxDistributerPayListEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerPayListEntity nxDistributerPayList);
	
	void update(NxDistributerPayListEntity nxDistributerPayList);
	
	void delete(Integer nxDistributerPayListId);
	
	void deleteBatch(Integer[] nxDistributerPayListIds);

    double queryDisPayListSubtotal(Map<String, Object> map);

	int queryDisPayListCount(Map<String, Object> map);

    List<NxDistributerPayListEntity> queryPayListListByParams(Map<String, Object> map);

    int queryDepRecordSecondsTotal(Map<String, Object> map);
}
