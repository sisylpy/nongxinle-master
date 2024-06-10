package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 04-27 12:56
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxLabelEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxLabelTypeEntity;
import com.nongxinle.service.NxLabelTypeService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxlabeltype")
public class NxLabelTypeController {
	@Autowired
	private NxLabelTypeService nxLabelTypeService;
	



	@RequestMapping(value = "/getLabel")
	@ResponseBody
	public R getLabel( ) {
		List<NxLabelEntity> nxLabelEntities =  nxLabelTypeService.queryAllLabel();

	    return R.ok().put("data",nxLabelEntities);
	}
	
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxlabeltype:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<NxLabelTypeEntity> nxLabelTypeList = nxLabelTypeService.queryList(map);
		int total = nxLabelTypeService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(nxLabelTypeList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{nxLabelTypeId}")
	@RequiresPermissions("nxlabeltype:info")
	public R info(@PathVariable("nxLabelTypeId") Integer nxLabelTypeId){
		NxLabelTypeEntity nxLabelType = nxLabelTypeService.queryObject(nxLabelTypeId);
		
		return R.ok().put("nxLabelType", nxLabelType);
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("nxlabeltype:save")
	public R save(@RequestBody NxLabelTypeEntity nxLabelType){
		nxLabelTypeService.save(nxLabelType);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxlabeltype:update")
	public R update(@RequestBody NxLabelTypeEntity nxLabelType){
		nxLabelTypeService.update(nxLabelType);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxlabeltype:delete")
	public R delete(@RequestBody Integer[] nxLabelTypeIds){
		nxLabelTypeService.deleteBatch(nxLabelTypeIds);
		
		return R.ok();
	}
	
}
