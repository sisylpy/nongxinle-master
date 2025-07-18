package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 03-28 18:56
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxGbDistibuterUserCouponEntity;
import com.nongxinle.service.NxGbDistibuterUserCouponService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxgbdistibuterusercoupon")
public class NxGbDistibuterUserCouponController {
	@Autowired
	private NxGbDistibuterUserCouponService nxGbDistibuterUserCouponService;



	@RequestMapping(value = "/scanGetCoupon/XqLi4ZvFRv.txt")
	@ResponseBody
	public String scanGetCoupon( ) {
		return "9976e22e84530fdfba7dfe6907737027";
	}


	@RequestMapping(value = "/disGetCouponList", method = RequestMethod.POST)
	@ResponseBody
	public R disGetCouponList (Integer disId){

		Map<String, Object> map = new HashMap<>();
		map.put("gbDisId", disId);
		map.put("equalStatus", 0);
		List<NxGbDistibuterUserCouponEntity> userCouponEntities = nxGbDistibuterUserCouponService.queryGbCouponListByParams(map);

		return R.ok().put("data", userCouponEntities);
	}



	@RequestMapping(value = "/XqLi4ZvFRv.txt")
	@ResponseBody
	public String nxDisInvite( ) {
		return "9976e22e84530fdfba7dfe6907737027";
	}




}
