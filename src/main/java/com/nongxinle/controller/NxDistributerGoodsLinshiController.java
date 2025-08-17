package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 08-14 22:10
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nongxinle.entity.NxDistributerGoodsLinshiEntity;
import com.nongxinle.service.NxDistributerGoodsLinshiService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("nxdistributergoodslinshi")
public class NxDistributerGoodsLinshiController {
	@Autowired
	private NxDistributerGoodsLinshiService nxDistributerGoodsLinshiService;
	
	@RequestMapping("/nxdistributergoodslinshi.html")
	public String list(){
		return "nxdistributergoodslinshi/nxdistributergoodslinshi.html";
	}
	
	@RequestMapping("/nxdistributergoodslinshi_add.html")
	public String add(){
		return "nxdistributergoodslinshi/nxdistributergoodslinshi_add.html";
	}
	
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxdistributergoodslinshi:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<NxDistributerGoodsLinshiEntity> nxDistributerGoodsLinshiList = nxDistributerGoodsLinshiService.queryList(map);
		int total = nxDistributerGoodsLinshiService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(nxDistributerGoodsLinshiList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{nxDistributerGoodsLsId}")
	@RequiresPermissions("nxdistributergoodslinshi:info")
	public R info(@PathVariable("nxDistributerGoodsLsId") Integer nxDistributerGoodsLsId){
		NxDistributerGoodsLinshiEntity nxDistributerGoodsLinshi = nxDistributerGoodsLinshiService.queryObject(nxDistributerGoodsLsId);
		
		return R.ok().put("nxDistributerGoodsLinshi", nxDistributerGoodsLinshi);
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("nxdistributergoodslinshi:save")
	public R save(@RequestBody NxDistributerGoodsLinshiEntity nxDistributerGoodsLinshi){
		nxDistributerGoodsLinshiService.save(nxDistributerGoodsLinshi);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxdistributergoodslinshi:update")
	public R update(@RequestBody NxDistributerGoodsLinshiEntity nxDistributerGoodsLinshi){
		nxDistributerGoodsLinshiService.update(nxDistributerGoodsLinshi);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxdistributergoodslinshi:delete")
	public R delete(@RequestBody Integer[] nxDistributerGoodsLsIds){
		nxDistributerGoodsLinshiService.deleteBatch(nxDistributerGoodsLsIds);
		
		return R.ok();
	}
	
}
