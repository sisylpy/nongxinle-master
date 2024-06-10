package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 02-18 14:22
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDepInventoryGoodsWeekTotalEntity;
import com.nongxinle.service.GbDepInventoryGoodsWeekTotalService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/gbdepinventorygoodsweektotal")
public class GbDepInventoryGoodsWeekTotalController {
	@Autowired
	private GbDepInventoryGoodsWeekTotalService gbDepInventoryGoodsWeekTotalService;
	
	@RequestMapping("/gbdepinventorygoodsweektotal.html")
	public String list(){
		return "gbdepinventorygoodsweektotal/gbdepinventorygoodsweektotal.html";
	}
	
	@RequestMapping("/gbdepinventorygoodsweektotal_add.html")
	public String add(){
		return "gbdepinventorygoodsweektotal/gbdepinventorygoodsweektotal_add.html";
	}
	
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("gbdepinventorygoodsweektotal:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<GbDepInventoryGoodsWeekTotalEntity> gbDepInventoryGoodsWeekTotalList = gbDepInventoryGoodsWeekTotalService.queryList(map);
		int total = gbDepInventoryGoodsWeekTotalService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(gbDepInventoryGoodsWeekTotalList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{gbInventoryGoodsWeekTotalId}")
	@RequiresPermissions("gbdepinventorygoodsweektotal:info")
	public R info(@PathVariable("gbInventoryGoodsWeekTotalId") Integer gbInventoryGoodsWeekTotalId){
		GbDepInventoryGoodsWeekTotalEntity gbDepInventoryGoodsWeekTotal = gbDepInventoryGoodsWeekTotalService.queryObject(gbInventoryGoodsWeekTotalId);
		
		return R.ok().put("gbDepInventoryGoodsWeekTotal", gbDepInventoryGoodsWeekTotal);
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("gbdepinventorygoodsweektotal:save")
	public R save(@RequestBody GbDepInventoryGoodsWeekTotalEntity gbDepInventoryGoodsWeekTotal){
		gbDepInventoryGoodsWeekTotalService.save(gbDepInventoryGoodsWeekTotal);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("gbdepinventorygoodsweektotal:update")
	public R update(@RequestBody GbDepInventoryGoodsWeekTotalEntity gbDepInventoryGoodsWeekTotal){
		gbDepInventoryGoodsWeekTotalService.update(gbDepInventoryGoodsWeekTotal);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("gbdepinventorygoodsweektotal:delete")
	public R delete(@RequestBody Integer[] gbInventoryGoodsWeekTotalIds){
		gbDepInventoryGoodsWeekTotalService.deleteBatch(gbInventoryGoodsWeekTotalIds);
		
		return R.ok();
	}
	
}
