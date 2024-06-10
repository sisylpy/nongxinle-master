package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 04-25 10:39
 */

import com.nongxinle.entity.NxCommunitySplicingOrdersEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunitySplicingOrdersService {
	
	NxCommunitySplicingOrdersEntity queryObject(Integer nxCommunitySplicingOrdersId);
	
	List<NxCommunitySplicingOrdersEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunitySplicingOrdersEntity nxCommunitySplicingOrders);
	
	void update(NxCommunitySplicingOrdersEntity nxCommunitySplicingOrders);
	
	void delete(Integer nxCommunitySplicingOrdersId);
	
	void deleteBatch(Integer[] nxCommunitySplicingOrdersIds);

    NxCommunitySplicingOrdersEntity queryNewPindan(Map<String, Object> map);

	List<NxCommunitySplicingOrdersEntity> querySplicingListByParams(Map<String, Object> map);

}
