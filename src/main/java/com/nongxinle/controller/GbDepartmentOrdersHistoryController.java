package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-30 10:14
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDepartmentOrdersHistoryEntity;
import com.nongxinle.service.GbDepartmentOrdersHistoryService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("gbdepartmentordershistory")
public class GbDepartmentOrdersHistoryController {
	@Autowired
	private GbDepartmentOrdersHistoryService gbDepartmentOrdersHistoryService;

	
}
