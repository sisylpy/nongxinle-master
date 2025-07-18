package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-29 06:59
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxAiForecastLogEntity;
import com.nongxinle.service.NxAiForecastLogService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxaiforecastlog")
public class NxAiForecastLogController {
	@Autowired
	private NxAiForecastLogService nxAiForecastLogService;
	
	@RequestMapping("/nxaiforecastlog.html")
	public String list(){
		return "nxaiforecastlog/nxaiforecastlog.html";
	}

}
