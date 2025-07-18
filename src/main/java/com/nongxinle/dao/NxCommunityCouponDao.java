package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-15 08:33
 */

import com.nongxinle.entity.NxCommunityCouponEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityCouponDao extends BaseDao<NxCommunityCouponEntity> {

    List<NxCommunityCouponEntity> queryCouponListByParams(Map<String, Object> map);

    List<NxCommunityCouponEntity> queryCustomerShowCoupon(Map<String, Object> map);

    NxCommunityCouponEntity queryCouponDetail(Map<String, Object> map);
}
