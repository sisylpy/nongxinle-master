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

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxLabelEntity;
import com.nongxinle.service.NxLabelService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxlabel")
public class NxLabelController {
	@Autowired
	private NxLabelService nxLabelService;
	
	@RequestMapping("/nxlabel.html")
	public String list(){
		return "nxlabel/nxlabel.html";
	}
	
	@RequestMapping("/nxlabel_add.html")
	public String add(){
		return "nxlabel/nxlabel_add.html";
	}
	
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxlabel:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<NxLabelEntity> nxLabelList = nxLabelService.queryList(map);
		int total = nxLabelService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(nxLabelList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{nxLabelId}")
	@RequiresPermissions("nxlabel:info")
	public R info(@PathVariable("nxLabelId") Integer nxLabelId){
		NxLabelEntity nxLabel = nxLabelService.queryObject(nxLabelId);
		
		return R.ok().put("nxLabel", nxLabel);
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("nxlabel:save")
	public R save(@RequestBody NxLabelEntity nxLabel){
		nxLabelService.save(nxLabel);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxlabel:update")
	public R update(@RequestBody NxLabelEntity nxLabel){
		nxLabelService.update(nxLabel);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxlabel:delete")
	public R delete(@RequestBody Integer[] nxLabelIds){
		nxLabelService.deleteBatch(nxLabelIds);
		
		return R.ok();
	}
	
}
