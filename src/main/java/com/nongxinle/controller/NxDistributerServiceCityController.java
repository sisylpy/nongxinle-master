package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 08-19 13:03
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDistributerServiceCityEntity;
import com.nongxinle.service.NxDistributerServiceCityService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("nxdistributerservicecity")
public class NxDistributerServiceCityController {
	@Autowired
	private NxDistributerServiceCityService nxDistributerServiceCityService;
	

	
}
