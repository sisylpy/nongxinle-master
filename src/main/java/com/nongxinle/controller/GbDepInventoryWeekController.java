package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 09-06 15:27
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.GbDepInventoryDailyEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDepInventoryWeekEntity;
import com.nongxinle.service.GbDepInventoryWeekService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/gbdepinventoryweek")
public class GbDepInventoryWeekController {
	@Autowired
	private GbDepInventoryWeekService gbDepInventoryWeekService;

	@RequestMapping(value = "/mendianGetWeekInventory/{depFatherId}")
	@ResponseBody
	public R mendianGetWeekInventory (@PathVariable Integer depFatherId  ) {
		Map<String, Object> map = new HashMap<>();
		map.put("depFatherId", depFatherId);
		map.put("status", 2);
		List<GbDepInventoryWeekEntity> inventoryDailyEntities =  gbDepInventoryWeekService.queryDepWeekList(map);
		return R.ok().put("data", inventoryDailyEntities);
	}
	

	
}
