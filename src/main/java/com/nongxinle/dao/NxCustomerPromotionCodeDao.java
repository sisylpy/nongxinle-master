package com.nongxinle.dao;

import com.nongxinle.entity.NxCustomerPromotionCodeEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerPromotionCodeDao {

    NxCustomerPromotionCodeEntity queryObject(Integer id);

    NxCustomerPromotionCodeEntity queryByCode(String promotionCode);

    NxCustomerPromotionCodeEntity queryActiveByOwner(Map<String, Object> map);

    NxCustomerPromotionCodeEntity queryLatestByOwner(Map<String, Object> map);

    List<NxCustomerPromotionCodeEntity> queryList(Map<String, Object> map);

    int save(NxCustomerPromotionCodeEntity entity);

    int update(NxCustomerPromotionCodeEntity entity);

    int incrementUseCount(Integer id);

    int incrementValidRegisterCount(Integer id);

    int incrementInvalidRegisterCount(Integer id);

    int disableAllActiveByOwner(Map<String, Object> map);

    int countActiveByOwner(Map<String, Object> map);

    int deleteByOwner(Map<String, Object> map);
}
