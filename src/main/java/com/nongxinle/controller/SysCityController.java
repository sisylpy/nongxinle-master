package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 08-19 12:35
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.SysCityEntity;
import com.nongxinle.service.SysCityService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/syscity")
public class SysCityController {
	@Autowired
	private SysCityService sysCityService;
	

	
}
