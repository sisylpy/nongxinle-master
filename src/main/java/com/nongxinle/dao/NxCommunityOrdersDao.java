package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 2020-03-22 18:07:28
 */

import com.nongxinle.entity.NxCommunityOrdersEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityOrdersDao extends BaseDao<NxCommunityOrdersEntity> {


    List<NxCommunityOrdersEntity> queryOrdersToWeigh(Map<String, Object> map);

    List<NxCommunityOrdersEntity> queryOrdersDetail(Map<String, Object> map);

    List<NxCommunityOrdersEntity> queryOrdersPaymentInformation(Map<String, Object> map);

    List<NxCommunityOrdersEntity> queryCustomerOrder(Map<String, Object> map);

    List<NxCommunityOrdersEntity> queryDeliveryOrders(Map<String, Object> map);

    NxCommunityOrdersEntity queryOrderByTradeNo(String ordersSn);

    List<NxCommunityOrdersEntity> queryOrderWithUserInfo(Map<String, Object> mapU);

    NxCommunityOrdersEntity queryPindanDetail(Map<String, Object> map);

}
