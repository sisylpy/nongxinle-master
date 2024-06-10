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

import com.nongxinle.entity.GbDepInventoryWeekEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDepInventoryMonthEntity;
import com.nongxinle.service.GbDepInventoryMonthService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/gbdepinventorymonth")
public class GbDepInventoryMonthController {
	@Autowired
	private GbDepInventoryMonthService gbDepInventoryMonthService;


	@RequestMapping(value = "/mendianGetMonthInventory/{depFatherId}")
	@ResponseBody
	public R mendianGetMonthInventory (@PathVariable Integer depFatherId  ) {
		Map<String, Object> map = new HashMap<>();
		map.put("depFatherId", depFatherId);
		map.put("status", 2);
		List<GbDepInventoryMonthEntity> inventoryDailyEntities =  gbDepInventoryMonthService.queryDepMonthList(map);
		return R.ok().put("data", inventoryDailyEntities);
	}
	
}
