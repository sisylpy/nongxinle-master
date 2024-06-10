package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-14 20:17
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDistributerRouteEntity;
import com.nongxinle.service.NxDistributerRouteService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("nxdistributerroute")
public class NxDistributerRouteController {
	@Autowired
	private NxDistributerRouteService nxDistributerRouteService;

	

	
}
