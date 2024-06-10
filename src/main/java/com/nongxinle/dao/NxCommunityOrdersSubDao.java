package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 2020-03-22 18:07:28
 */

import com.nongxinle.entity.NxCommunityOrdersEntity;
import com.nongxinle.entity.NxCommunityOrdersSubEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityOrdersSubDao extends BaseDao<NxCommunityOrdersSubEntity> {

    List<NxCommunityOrdersSubEntity> queryPurchaseGoods(Map<String, Object> map);

    List<NxCommunityOrdersSubEntity> querySubsByGoodsId(Map<String, Object> subMap);

    List<NxCommunityOrdersSubEntity> querySubOrdersByDisIdandStatus(Map<String, Object> map2);

    List<NxCommunityOrdersSubEntity> querySubOrdersByCustomerUserId(Map<String, Object> map);

    List<NxCommunityOrdersSubEntity>  queryListByOrderId(Map<String, Object> map);

    List<NxCommunityOrdersEntity> queryOutGoodsByType(Map<String, Object> map);

    List<NxCommunityOrdersSubEntity> querySubOrdersByParams(Map<String, Object> map);
}
