package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-26 16:29
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxDistributerCouponEntity;
import com.nongxinle.entity.NxGbDistibuterUserCouponEntity;
import com.nongxinle.service.NxDistributerCouponService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDepartmentUserCouponEntity;
import com.nongxinle.service.NxDepartmentUserCouponService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController

@RequestMapping("nxdepartmentusercoupon")
public class NxDepartmentUserCouponController {
	@Autowired
	private NxDepartmentUserCouponService nxDepartmentUserCouponService;
	@Autowired
	private NxDistributerCouponService nxDistributerCouponService;
	


	@RequestMapping(value = "/disGetCoupons/{id}")
	@ResponseBody
	public R disGetCoupons(@PathVariable Integer id) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", id);
		map.put("equalStatus", 0);
		System.out.println("coupeoneoemap" + map);
		List<NxDistributerCouponEntity> couponEntities = nxDistributerCouponService.queryLoadDownListByParams(map);
	    return R.ok().put("data", couponEntities);
	}



	
}
