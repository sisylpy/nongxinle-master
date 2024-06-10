package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-07 09:20
 */

import com.nongxinle.entity.NxRestrauntOrdersHistoryEntity;

import java.util.List;
import java.util.Map;

public interface NxRestrauntOrdersHistoryService {
	
	NxRestrauntOrdersHistoryEntity queryObject(Integer nxRestrauntOrdersHistoryId);
	
	List<NxRestrauntOrdersHistoryEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxRestrauntOrdersHistoryEntity nxRestrauntOrdersHistory);
	
	void update(NxRestrauntOrdersHistoryEntity nxRestrauntOrdersHistory);
	
	void delete(Integer nxRestrauntOrdersHistoryId);
	
	void deleteBatch(Integer[] nxRestrauntOrdersHistoryIds);

    List<NxRestrauntOrdersHistoryEntity> queryHistoryOrdersByParams(Map<String, Object> map);
}
