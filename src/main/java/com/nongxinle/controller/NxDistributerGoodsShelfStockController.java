package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 10-14 12:03
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDistributerGoodsShelfStockEntity;
import com.nongxinle.service.NxDistributerGoodsShelfStockService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("nxdistributergoodsshelfstock")
public class NxDistributerGoodsShelfStockController {
	@Autowired
	private NxDistributerGoodsShelfStockService nxDistributerGoodsShelfStockService;
	
	@RequestMapping("/nxdistributergoodsshelfstock.html")
	public String list(){
		return "nxdistributergoodsshelfstock/nxdistributergoodsshelfstock.html";
	}
	
	@RequestMapping("/nxdistributergoodsshelfstock_add.html")
	public String add(){
		return "nxdistributergoodsshelfstock/nxdistributergoodsshelfstock_add.html";
	}
	
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxdistributergoodsshelfstock:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<NxDistributerGoodsShelfStockEntity> nxDistributerGoodsShelfStockList = nxDistributerGoodsShelfStockService.queryList(map);
		int total = nxDistributerGoodsShelfStockService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(nxDistributerGoodsShelfStockList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{nxDistributerGoodsShelfStockId}")
	@RequiresPermissions("nxdistributergoodsshelfstock:info")
	public R info(@PathVariable("nxDistributerGoodsShelfStockId") Integer nxDistributerGoodsShelfStockId){
		NxDistributerGoodsShelfStockEntity nxDistributerGoodsShelfStock = nxDistributerGoodsShelfStockService.queryObject(nxDistributerGoodsShelfStockId);
		
		return R.ok().put("nxDistributerGoodsShelfStock", nxDistributerGoodsShelfStock);
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("nxdistributergoodsshelfstock:save")
	public R save(@RequestBody NxDistributerGoodsShelfStockEntity nxDistributerGoodsShelfStock){
		nxDistributerGoodsShelfStockService.save(nxDistributerGoodsShelfStock);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxdistributergoodsshelfstock:update")
	public R update(@RequestBody NxDistributerGoodsShelfStockEntity nxDistributerGoodsShelfStock){
		nxDistributerGoodsShelfStockService.update(nxDistributerGoodsShelfStock);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxdistributergoodsshelfstock:delete")
	public R delete(@RequestBody Integer[] nxDistributerGoodsShelfStockIds){
		nxDistributerGoodsShelfStockService.deleteBatch(nxDistributerGoodsShelfStockIds);
		
		return R.ok();
	}
	
}
