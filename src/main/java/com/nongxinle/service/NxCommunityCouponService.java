package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-15 08:33
 */

import com.nongxinle.entity.NxCommunityAdsenseEntity;
import com.nongxinle.entity.NxCommunityCouponEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityCouponService {

    void save(NxCommunityCouponEntity nxCommunityCouponEntity);

    List<NxCommunityCouponEntity> queryCouponListByParams(Map<String, Object> map);

    NxCommunityCouponEntity queryObject(Integer id);

    void update(NxCommunityCouponEntity communityCouponEntity);

    void delte(Integer id);

    List<NxCommunityCouponEntity> queryCustomerShowCoupon(Map<String, Object> map);

    NxCommunityCouponEntity queryCouponDetail(Map<String, Object> map);
}
