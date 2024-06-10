package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-09 21:11
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.GbDistributerEntity;
import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.service.NxDepartmentService;
import com.nongxinle.service.NxDistributerCommunityService;
import com.nongxinle.service.NxDistributerGbDistributerService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDistributerDepartmentEntity;
import com.nongxinle.service.NxDistributerDepartmentService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxdistributerdepartment")
public class NxDistributerDepartmentController {
	@Autowired
	private NxDistributerDepartmentService nxDistributerDepartmentService;



	@RequestMapping(value = "/disGetCustomerToReplaceOrder/{disId}")
	@ResponseBody
	public R disGetCustomerToReplaceOrder(@PathVariable Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("isGroup", 1);
		map.put("subAmount", 0);
		List<NxDepartmentEntity> entities =  nxDistributerDepartmentService.queryDisDepartmentsBySettleType(map);


		return R.ok().put("data", entities);
	}




}
