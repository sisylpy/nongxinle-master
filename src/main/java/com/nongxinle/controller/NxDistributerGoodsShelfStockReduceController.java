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

import com.nongxinle.entity.NxDistributerGoodsShelfStockReduceEntity;
import com.nongxinle.service.NxDistributerGoodsShelfStockReduceService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("nxdistributergoodsshelfstockreduce")
public class NxDistributerGoodsShelfStockReduceController {
	@Autowired
	private NxDistributerGoodsShelfStockReduceService nxDistributerGoodsShelfStockReduceService;
	
	@RequestMapping("/nxdistributergoodsshelfstockreduce.html")
	public String list(){
		return "nxdistributergoodsshelfstockreduce/nxdistributergoodsshelfstockreduce.html";
	}
	
	@RequestMapping("/nxdistributergoodsshelfstockreduce_add.html")
	public String add(){
		return "nxdistributergoodsshelfstockreduce/nxdistributergoodsshelfstockreduce_add.html";
	}
	
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxdistributergoodsshelfstockreduce:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<NxDistributerGoodsShelfStockReduceEntity> nxDistributerGoodsShelfStockReduceList = nxDistributerGoodsShelfStockReduceService.queryList(map);
		int total = nxDistributerGoodsShelfStockReduceService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(nxDistributerGoodsShelfStockReduceList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{nxDistributerGoodsShelfStockReduceId}")
	@RequiresPermissions("nxdistributergoodsshelfstockreduce:info")
	public R info(@PathVariable("nxDistributerGoodsShelfStockReduceId") Integer nxDistributerGoodsShelfStockReduceId){
		NxDistributerGoodsShelfStockReduceEntity nxDistributerGoodsShelfStockReduce = nxDistributerGoodsShelfStockReduceService.queryObject(nxDistributerGoodsShelfStockReduceId);
		
		return R.ok().put("nxDistributerGoodsShelfStockReduce", nxDistributerGoodsShelfStockReduce);
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("nxdistributergoodsshelfstockreduce:save")
	public R save(@RequestBody NxDistributerGoodsShelfStockReduceEntity nxDistributerGoodsShelfStockReduce){
		nxDistributerGoodsShelfStockReduceService.save(nxDistributerGoodsShelfStockReduce);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxdistributergoodsshelfstockreduce:update")
	public R update(@RequestBody NxDistributerGoodsShelfStockReduceEntity nxDistributerGoodsShelfStockReduce){
		nxDistributerGoodsShelfStockReduceService.update(nxDistributerGoodsShelfStockReduce);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxdistributergoodsshelfstockreduce:delete")
	public R delete(@RequestBody Integer[] nxDistributerGoodsShelfStockReduceIds){
		nxDistributerGoodsShelfStockReduceService.deleteBatch(nxDistributerGoodsShelfStockReduceIds);
		
		return R.ok();
	}
	
}
