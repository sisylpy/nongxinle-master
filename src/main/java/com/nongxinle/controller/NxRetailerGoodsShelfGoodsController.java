package com.nongxinle.controller;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-22 15:25
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxRetailerGoodsShelfEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxRetailerGoodsShelfGoodsEntity;
import com.nongxinle.service.NxRetailerGoodsShelfGoodsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxretailergoodsshelfgoods")
public class NxRetailerGoodsShelfGoodsController {
	@Autowired
	private NxRetailerGoodsShelfGoodsService nxRetGoodsShelfGoodsService;




	@RequestMapping(value = "/getRetShelfGoodsWithPurchase/{shelfId}")
	@ResponseBody
	public R getRetShelfGoodsWithPurchase(@PathVariable Integer shelfId) {
		Map<String, Object> map = new HashMap<>();
		map.put("shelfId", shelfId);
		map.put("status", 3);
		List<NxRetailerGoodsShelfGoodsEntity> entities = nxRetGoodsShelfGoodsService.queryRetShelfGoodsWithPurchaseByParams(map);
		return R.ok().put("data", entities);
	}


	@RequestMapping(value = "/updateRetShelfGoodsSort", method = RequestMethod.POST)
	@ResponseBody
	public R updateRetShelfGoodsSort (@RequestBody List<NxRetailerGoodsShelfGoodsEntity> goodsList) {
		for (NxRetailerGoodsShelfGoodsEntity goods : goodsList) {
			nxRetGoodsShelfGoodsService.update(goods);
		}
	    return R.ok();
	}


	@RequestMapping(value = "/deleteRetShelfGoods/{id}")
	@ResponseBody
	public R deleteRetShelfGoods(@PathVariable Integer id) {
	    nxRetGoodsShelfGoodsService.delete(id);
	    return R.ok();
	}


	@RequestMapping(value = "/getRetShelfGoods/{shelfId}")
	@ResponseBody
	public R getRetShelfGoods(@PathVariable Integer shelfId) {
		Map<String, Object> map = new HashMap<>();
		map.put("shelfId", shelfId);
	    List<NxRetailerGoodsShelfGoodsEntity> entities = nxRetGoodsShelfGoodsService.queryRetShelfGoodsByParams(map);
	    return R.ok().put("data", entities);
	}

	@RequestMapping(value = "/saveRetShelfGoods", method = RequestMethod.POST)
	@ResponseBody
	public R saveRetShelfGoods (@RequestBody List<NxRetailerGoodsShelfGoodsEntity> shelfGoodsEntities  ) {

		for (NxRetailerGoodsShelfGoodsEntity goods : shelfGoodsEntities) {
			nxRetGoodsShelfGoodsService.save(goods);
		}

		return R.ok();
	}

	
}
