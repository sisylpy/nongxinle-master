package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-15 08:30
 */

import com.nongxinle.entity.NxCommunityCouponEntity;
import com.nongxinle.entity.NxCustomerUserCouponEntity;

import java.util.List;
import java.util.Map;



public interface NxCustomerUserCouponDao extends BaseDao<NxCustomerUserCouponEntity> {


    List<NxCustomerUserCouponEntity> queryUserCouponListByParams(Map<String, Object> map);

    NxCustomerUserCouponEntity queryUserCouponDetail(Integer id);

    int queryUserCouponCount(Map<String, Object> mapUC);
}
