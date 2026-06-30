package com.nongxinle.dao;

import com.nongxinle.entity.NxCommunityOrderDispatchEntity;
import com.nongxinle.entity.NxCommunityOrdersEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityOrderDispatchDao extends BaseDao<NxCommunityOrderDispatchEntity> {

    NxCommunityOrderDispatchEntity queryByOrderId(Integer orderId);

    List<NxCommunityOrdersEntity> queryEligibleDeliveryOrders(Map<String, Object> map);

    int updateStatusByStopId(Map<String, Object> map);

    int resetToUnassignedByStopId(Integer stopId);
}
