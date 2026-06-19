package com.nongxinle.dao;

import com.nongxinle.entity.NxPlatformOrderFulfillmentEntity;

public interface NxPlatformOrderFulfillmentDao {

    NxPlatformOrderFulfillmentEntity queryObject(Integer id);

    NxPlatformOrderFulfillmentEntity queryByOrderId(Integer orderId);

    int countAll();

    void save(NxPlatformOrderFulfillmentEntity entity);

    int updateFulfillment(NxPlatformOrderFulfillmentEntity entity);
}
