package com.nongxinle.dao;

import com.nongxinle.entity.NxCustomerPromotionCodeAttemptEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerPromotionCodeAttemptDao {

    int save(NxCustomerPromotionCodeAttemptEntity entity);

    List<NxCustomerPromotionCodeAttemptEntity> queryList(Map<String, Object> map);

    int queryTotal(Map<String, Object> map);

    int deleteBySourceOwner(Map<String, Object> map);
}
