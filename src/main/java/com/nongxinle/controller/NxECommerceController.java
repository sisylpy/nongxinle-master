package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 11-28 21:17
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxECommerceEntity;
import com.nongxinle.service.NxECommerceService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("nxecommerce")
public class NxECommerceController {
	@Autowired
	private NxECommerceService nxECommerceService;
	

	
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxecommerce:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<NxECommerceEntity> nxECommerceList = nxECommerceService.queryList(map);
		int total = nxECommerceService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(nxECommerceList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{nxECommerceId}")
	@RequiresPermissions("nxecommerce:info")
	public R info(@PathVariable("nxECommerceId") Integer nxECommerceId){
		NxECommerceEntity nxECommerce = nxECommerceService.queryObject(nxECommerceId);
		
		return R.ok().put("nxECommerce", nxECommerce);
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("nxecommerce:save")
	public R save(@RequestBody NxECommerceEntity nxECommerce){
		nxECommerceService.save(nxECommerce);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxecommerce:update")
	public R update(@RequestBody NxECommerceEntity nxECommerce){
		nxECommerceService.update(nxECommerce);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxecommerce:delete")
	public R delete(@RequestBody Integer[] nxECommerceIds){
		nxECommerceService.deleteBatch(nxECommerceIds);
		
		return R.ok();
	}
	
}
