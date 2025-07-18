package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 03-28 18:43
 */

import com.nongxinle.entity.NxDistributerCouponEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerCouponService {
	
	NxDistributerCouponEntity queryObject(Integer nxDistributerCouponId);
	
	List<NxDistributerCouponEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerCouponEntity nxDistributerCoupon);
	
	void update(NxDistributerCouponEntity nxDistributerCoupon);
	
	void delete(Integer nxDistributerCouponId);
	
	void deleteBatch(Integer[] nxDistributerCouponIds);

    List<NxDistributerCouponEntity> queryLoadDownListByParams(Map<String, Object> map);
}
