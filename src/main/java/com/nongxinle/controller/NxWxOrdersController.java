package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-21 22:15
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxWxOrdersEntity;
import com.nongxinle.service.NxWxOrdersService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("nxwxorders")
public class NxWxOrdersController {
	@Autowired
	private NxWxOrdersService nxWxOrdersService;



	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxwxorders:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);

		//查询列表数据
		List<NxWxOrdersEntity> nxWxOrdersList = nxWxOrdersService.queryList(map);
		int total = nxWxOrdersService.queryTotal(map);

		PageUtils pageUtil = new PageUtils(nxWxOrdersList, total, limit, page);

		return R.ok().put("page", pageUtil);
	}


	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{nxWxOrdersId}")
	@RequiresPermissions("nxwxorders:info")
	public R info(@PathVariable("nxWxOrdersId") Integer nxWxOrdersId){
		NxWxOrdersEntity nxWxOrders = nxWxOrdersService.queryObject(nxWxOrdersId);

		return R.ok().put("nxWxOrders", nxWxOrders);
	}

	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("nxwxorders:save")
	public R save(@RequestBody NxWxOrdersEntity nxWxOrders){
		nxWxOrdersService.save(nxWxOrders);

		return R.ok();
	}

	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxwxorders:update")
	public R update(@RequestBody NxWxOrdersEntity nxWxOrders){
		nxWxOrdersService.update(nxWxOrders);

		return R.ok();
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxwxorders:delete")
	public R delete(@RequestBody Integer[] nxWxOrdersIds){
		nxWxOrdersService.deleteBatch(nxWxOrdersIds);

		return R.ok();
	}
	
}
