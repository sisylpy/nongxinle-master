package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 03-28 18:56
 */

import com.nongxinle.entity.NxGbDistibuterUserCouponEntity;

import java.util.List;
import java.util.Map;

public interface NxGbDistibuterUserCouponService {
	
	NxGbDistibuterUserCouponEntity queryObject(Integer nxGbDistributerUserCouponId);
	
	List<NxGbDistibuterUserCouponEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxGbDistibuterUserCouponEntity nxGbDistibuterUserCoupon);
	
	void update(NxGbDistibuterUserCouponEntity nxGbDistibuterUserCoupon);
	
	void delete(Integer nxGbDistributerUserCouponId);
	
	void deleteBatch(Integer[] nxGbDistributerUserCouponIds);

    List<NxGbDistibuterUserCouponEntity> queryGbCouponListByParams(Map<String, Object> mapG);
}
