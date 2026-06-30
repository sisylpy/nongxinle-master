package com.nongxinle.dao;

import com.nongxinle.entity.NxCustomerReferralReadStateEntity;

public interface NxCustomerReferralReadStateDao {

    NxCustomerReferralReadStateEntity queryByUserId(Integer userId);

    int save(NxCustomerReferralReadStateEntity entity);

    int update(NxCustomerReferralReadStateEntity entity);
}
