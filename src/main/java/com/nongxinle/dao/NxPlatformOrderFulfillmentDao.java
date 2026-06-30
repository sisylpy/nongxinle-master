package com.nongxinle.dao;

import com.nongxinle.entity.NxPlatformOrderFulfillmentEntity;
import org.apache.ibatis.annotations.Param;

public interface NxPlatformOrderFulfillmentDao {

    NxPlatformOrderFulfillmentEntity queryObject(Integer id);

    NxPlatformOrderFulfillmentEntity queryByOrderId(Integer orderId);

    int countAll();

    void save(NxPlatformOrderFulfillmentEntity entity);

    int updateFulfillment(NxPlatformOrderFulfillmentEntity entity);

    int revertReadyForPickupToAssigned(@Param("orderId") Integer orderId, @Param("operatorId") Integer operatorId);

    int deleteByOrderId(Integer orderId);
}
