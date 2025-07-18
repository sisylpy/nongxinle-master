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

import com.nongxinle.entity.SysCityMarketEntity;
import com.nongxinle.service.SysCityMarketService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/syscitymarket")
public class SysCityMarketController {
	@Autowired
	private SysCityMarketService sysCityMarketService;
	
	@RequestMapping(value = "/jjshGetMarket", method = RequestMethod.POST)
	@ResponseBody
	public R jjshGetMarket(Integer maId, Integer cityId) {

		Map<String, Object> map = new HashMap<>();
		if(maId != -1){
			map.put("maId", maId);
		}
		if(cityId != -1){
			map.put("cityId", cityId);
		}

		List<SysCityMarketEntity> marketEntities =  sysCityMarketService.queryMarketByParams(map);
	    return R.ok().put("data", marketEntities);
	}
	

	
}
