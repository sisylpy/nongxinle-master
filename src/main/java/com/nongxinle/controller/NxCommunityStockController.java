package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-08 09:22
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunityStockEntity;
import com.nongxinle.service.NxCommunityStockService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxcommunitystock")
public class NxCommunityStockController {
	@Autowired
	private NxCommunityStockService nxCommunityStockService;
	

	
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxcommunitystock:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<NxCommunityStockEntity> nxCommunityStockList = nxCommunityStockService.queryList(map);
		int total = nxCommunityStockService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(nxCommunityStockList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{nxCommunityStockId}")
//	@RequiresPermissions("nxcommunitystock:info")
	public R info(@PathVariable("nxCommunityStockId") Integer nxCommunityStockId){
		NxCommunityStockEntity nxCommunityStock = nxCommunityStockService.queryObject(nxCommunityStockId);
		
		return R.ok().put("nxCommunityStock", nxCommunityStock);
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
//	@RequiresPermissions("nxcommunitystock:save")
	public R save(@RequestBody NxCommunityStockEntity nxCommunityStock){
		System.out.println(nxCommunityStock);
		nxCommunityStockService.save(nxCommunityStock);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxcommunitystock:update")
	public R update(@RequestBody NxCommunityStockEntity nxCommunityStock){
		nxCommunityStockService.update(nxCommunityStock);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxcommunitystock:delete")
	public R delete(@RequestBody Integer[] nxCommunityStockIds){
		nxCommunityStockService.deleteBatch(nxCommunityStockIds);
		
		return R.ok();
	}
	
}
