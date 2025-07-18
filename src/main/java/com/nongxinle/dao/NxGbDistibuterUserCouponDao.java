package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 03-28 18:56
 */

import com.nongxinle.entity.NxGbDistibuterUserCouponEntity;

import java.util.List;
import java.util.Map;


public interface NxGbDistibuterUserCouponDao extends BaseDao<NxGbDistibuterUserCouponEntity> {

    List<NxGbDistibuterUserCouponEntity> queryGbCouponListByParams(Map<String, Object> mapG);
}
