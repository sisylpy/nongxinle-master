package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import com.nongxinle.entity.NxCustomerEntity;

import java.util.List;
import java.util.Map;


public interface NxCustomerDao extends BaseDao<NxCustomerEntity> {

    List<NxCustomerEntity> queryCommunityCustomers(Map<String, Object> map);

    int queryCustomerOfCommunityTotal(Map<String, Object> map);
}
