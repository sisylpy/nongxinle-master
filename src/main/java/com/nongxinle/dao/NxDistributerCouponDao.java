package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 03-28 18:43
 */

import com.nongxinle.entity.NxDistributerCouponEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerCouponDao extends BaseDao<NxDistributerCouponEntity> {

    List<NxDistributerCouponEntity> queryLoadDownListByParams(Map<String, Object> map);
}
