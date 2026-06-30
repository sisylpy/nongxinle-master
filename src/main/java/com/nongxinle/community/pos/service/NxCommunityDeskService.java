package com.nongxinle.community.pos.service;

/**
 * 
 *
 * @author lpy
 * @date 04-07 09:33
 */

import com.nongxinle.entity.NxCommunityDeskEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityDeskService {
	
	NxCommunityDeskEntity queryObject(Integer nxCommunityDeskId);
	
	List<NxCommunityDeskEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityDeskEntity nxCommunityDesk);
	
	void update(NxCommunityDeskEntity nxCommunityDesk);
	
	void delete(Integer nxCommunityDeskId);
	
	void deleteBatch(Integer[] nxCommunityDeskIds);

	List<NxCommunityDeskEntity> queryComDeskByParams(Map<String, Object> map);

    NxCommunityDeskEntity queryDeskWithOrders(Map<String, Object> map);

    NxCommunityDeskEntity queryDeskByCurrentOrderId(Integer currentOrderId);

    void bindCurrentOrder(Integer deskId, Integer orderId);

    void releaseCurrentOrder(Integer deskId);
}
