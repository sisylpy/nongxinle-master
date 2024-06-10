package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-04 11:07
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDistributerModuleEntity;
import com.nongxinle.service.GbDistributerModuleService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("gbdistributermodule")
public class GbDistributerModuleController {
	@Autowired
	private GbDistributerModuleService gbDistributerModuleService;




	
}
