package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-08 19:09
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDepartmentOrdersHistoryEntity;
import com.nongxinle.service.NxDepartmentOrdersHistoryService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxdepartmentordershistory")
public class NxDepartmentOrdersHistoryController {
	@Autowired
	private NxDepartmentOrdersHistoryService nxDepartmentOrdersHistoryService;

	

	@RequestMapping(value = "/delHisotory/{id}")
	@ResponseBody
	public R delHisotory(@PathVariable Integer id) {
		nxDepartmentOrdersHistoryService.delete(id);
	    
	    return R.ok();
	}

	
}
