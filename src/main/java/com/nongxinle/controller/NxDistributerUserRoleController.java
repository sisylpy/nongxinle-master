package com.nongxinle.controller;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 06-20 10:55
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDistributerUserRoleEntity;
import com.nongxinle.service.NxDistributerUserRoleService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("nxdistributeruserrole")
public class NxDistributerUserRoleController {
	@Autowired
	private NxDistributerUserRoleService nxDistributerUserRoleService;
	

	
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxdistributeruserrole:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<NxDistributerUserRoleEntity> nxDistributerUserRoleList = nxDistributerUserRoleService.queryList(map);
		int total = nxDistributerUserRoleService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(nxDistributerUserRoleList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{nxDistributerUserRoleId}")
	@RequiresPermissions("nxdistributeruserrole:info")
	public R info(@PathVariable("nxDistributerUserRoleId") Integer nxDistributerUserRoleId){
		NxDistributerUserRoleEntity nxDistributerUserRole = nxDistributerUserRoleService.queryObject(nxDistributerUserRoleId);
		
		return R.ok().put("nxDistributerUserRole", nxDistributerUserRole);
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("nxdistributeruserrole:save")
	public R save(@RequestBody NxDistributerUserRoleEntity nxDistributerUserRole){
		nxDistributerUserRoleService.save(nxDistributerUserRole);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxdistributeruserrole:update")
	public R update(@RequestBody NxDistributerUserRoleEntity nxDistributerUserRole){
		nxDistributerUserRoleService.update(nxDistributerUserRole);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxdistributeruserrole:delete")
	public R delete(@RequestBody Integer[] nxDistributerUserRoleIds){
		nxDistributerUserRoleService.deleteBatch(nxDistributerUserRoleIds);
		
		return R.ok();
	}
	
}
