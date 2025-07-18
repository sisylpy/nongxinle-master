package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-24 01:00
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxCustomerUserCouponEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.service.NxCustomerUserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCustomerUserCardEntity;
import com.nongxinle.service.NxCustomerUserCardService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@RestController
@RequestMapping("api/nxcustomerusercard")
public class NxCustomerUserCardController {
	@Autowired
	private NxCustomerUserCardService nxCustomerUserCardService;
	@Autowired
	private NxCustomerUserService nxCustomerUserService;

	@RequestMapping(value = "/userGetCards/{id}")
	@ResponseBody
	public R userGetCards (@PathVariable Integer id) {
		Map<String, Object> map = new HashMap<>();
		map.put("userId", id);
		map.put("status", 1);
		map.put("stopTime", formatWhatDay(0));
		System.out.println("getupodddi");
		List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(map);
		NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(id);
		Map<String, Object> mapR = new HashMap<>();
		mapR.put("arr", cardEntities);
		mapR.put("userInfo", userEntity);
		return R.ok().put("data", mapR);
	}
	

	
}
