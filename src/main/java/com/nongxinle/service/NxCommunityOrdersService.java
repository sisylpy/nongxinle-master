package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 2020-03-22 18:07:28
 */

import com.nongxinle.entity.NxCommunityOrdersEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityOrdersService {
	
	NxCommunityOrdersEntity queryObject(Integer nxOrdersId);
	
	List<NxCommunityOrdersEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityOrdersEntity nxOrders);
	
	void update(NxCommunityOrdersEntity nxOrders);
	
	void delete(Integer nxOrdersId);
	
	void deleteBatch(Integer[] nxOrdersIds);


	List<NxCommunityOrdersEntity> queryOrdersToWeigh(Map<String, Object> map);

	void updateSub(NxCommunityOrdersEntity arr);

	List<NxCommunityOrdersEntity> queryOrdersDetail(Map<String, Object> map);

	List<NxCommunityOrdersEntity> queryOrdersPaymentInformation(Map<String, Object> map);

	Integer  updatePaymentStatus(Map<String, Object> map);

	List<NxCommunityOrdersEntity> queryCustomerOrder(Map<String, Object> map);

	List<NxCommunityOrdersEntity> queryDeliveryOrder(Map<String, Object> map);


	List<NxCommunityOrdersEntity> queryOrderWithUserInfo(Map<String, Object> mapU);

	NxCommunityOrdersEntity queryOrderByTradeNo(String ordersSn);

	NxCommunityOrdersEntity queryPindanDetail(Map<String, Object> map);

	void justSave(NxCommunityOrdersEntity ordersEntity);
}
