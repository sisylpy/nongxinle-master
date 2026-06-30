package com.nongxinle.dao;

import com.nongxinle.entity.NxCustomerPromoterEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerPromoterDao {

    NxCustomerPromoterEntity queryObject(Integer id);

    List<NxCustomerPromoterEntity> queryList(Map<String, Object> map);

    int save(NxCustomerPromoterEntity entity);

    int update(NxCustomerPromoterEntity entity);

    int countQualifiedReferrals(Integer promoterId);

    int delete(Integer promoterId);
}
