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

import com.nongxinle.entity.NxCommunityEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxECommerceCommunityEntity;
import com.nongxinle.service.NxECommerceCommunityService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxecommercecommunity")
public class NxECommerceCommunityController {
	@Autowired
	private NxECommerceCommunityService nxECommerceCommunityService;


	@RequestMapping(value = "/getCommunityByCommerceId/{commerceId}")
	@ResponseBody
	public R getCommunityByDistributerId(@PathVariable Integer commerceId) {
		List<NxCommunityEntity> entities = nxECommerceCommunityService.queryCommunityByCommerceId(commerceId);
		return R.ok().put("data", entities);
	}
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxecommercecommunity:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<NxECommerceCommunityEntity> nxECommerceCommunityList = nxECommerceCommunityService.queryList(map);
		int total = nxECommerceCommunityService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(nxECommerceCommunityList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{nxEccId}")
	@RequiresPermissions("nxecommercecommunity:info")
	public R info(@PathVariable("nxEccId") Integer nxEccId){
		NxECommerceCommunityEntity nxECommerceCommunity = nxECommerceCommunityService.queryObject(nxEccId);
		
		return R.ok().put("nxECommerceCommunity", nxECommerceCommunity);
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("nxecommercecommunity:save")
	public R save(@RequestBody NxECommerceCommunityEntity nxECommerceCommunity){
		nxECommerceCommunityService.save(nxECommerceCommunity);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxecommercecommunity:update")
	public R update(@RequestBody NxECommerceCommunityEntity nxECommerceCommunity){
		nxECommerceCommunityService.update(nxECommerceCommunity);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxecommercecommunity:delete")
	public R delete(@RequestBody Integer[] nxEccIds){
		nxECommerceCommunityService.deleteBatch(nxEccIds);
		
		return R.ok();
	}
	
}
