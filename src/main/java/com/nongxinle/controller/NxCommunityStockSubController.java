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

import com.nongxinle.entity.NxCommunityStockSubEntity;
import com.nongxinle.service.NxCommunityStockSubService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("nxcommunitystocksub")
public class NxCommunityStockSubController {
	@Autowired
	private NxCommunityStockSubService nxCommunityStockSubService;
	
	@RequestMapping("/nxcommunitystocksub.html")
	public String list(){
		return "nxcommunitystocksub/nxcommunitystocksub.html";
	}
	
	@RequestMapping("/nxcommunitystocksub_add.html")
	public String add(){
		return "nxcommunitystocksub/nxcommunitystocksub_add.html";
	}
	
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxcommunitystocksub:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<NxCommunityStockSubEntity> nxCommunityStockSubList = nxCommunityStockSubService.queryList(map);
		int total = nxCommunityStockSubService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(nxCommunityStockSubList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{nxCommunitySubStockId}")
	@RequiresPermissions("nxcommunitystocksub:info")
	public R info(@PathVariable("nxCommunitySubStockId") Integer nxCommunitySubStockId){
		NxCommunityStockSubEntity nxCommunityStockSub = nxCommunityStockSubService.queryObject(nxCommunitySubStockId);
		
		return R.ok().put("nxCommunityStockSub", nxCommunityStockSub);
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("nxcommunitystocksub:save")
	public R save(@RequestBody NxCommunityStockSubEntity nxCommunityStockSub){
		nxCommunityStockSubService.save(nxCommunityStockSub);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxcommunitystocksub:update")
	public R update(@RequestBody NxCommunityStockSubEntity nxCommunityStockSub){
		nxCommunityStockSubService.update(nxCommunityStockSub);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxcommunitystocksub:delete")
	public R delete(@RequestBody Integer[] nxCommunitySubStockIds){
		nxCommunityStockSubService.deleteBatch(nxCommunitySubStockIds);
		
		return R.ok();
	}
	
}
