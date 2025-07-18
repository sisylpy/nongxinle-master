package com.nongxinle.dao;

/**
 *
 *
 * @author lpy
 * @date 05-24 01:00
 */

import com.nongxinle.entity.NxCustomerUserCardEntity;

import java.util.List;
import java.util.Map;


public interface NxCustomerUserCardDao extends BaseDao<NxCustomerUserCardEntity> {

    List<NxCustomerUserCardEntity> queryUserCardByParams(Map<String, Object> map);

    NxCustomerUserCardEntity queryUserGoodsCard(Map<String, Object> map);

    int queryUserCardCount(Map<String, Object> mapCARD);

}
