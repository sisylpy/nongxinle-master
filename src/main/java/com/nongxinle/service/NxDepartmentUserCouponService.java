package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-26 16:29
 */

import com.nongxinle.entity.NxDepartmentUserCouponEntity;

import java.util.List;
import java.util.Map;

public interface NxDepartmentUserCouponService {
	
	NxDepartmentUserCouponEntity queryObject(Integer nxDepartmentUserCouponId);
	
	List<NxDepartmentUserCouponEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDepartmentUserCouponEntity nxDepartmentUserCoupon);
	
	void update(NxDepartmentUserCouponEntity nxDepartmentUserCoupon);
	
	void delete(Integer nxDepartmentUserCouponId);
	
	void deleteBatch(Integer[] nxDepartmentUserCouponIds);
}
