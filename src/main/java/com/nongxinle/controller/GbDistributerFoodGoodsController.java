package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 03-27 10:50
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.GbDistributerFoodEntity;
import com.nongxinle.service.GbDistributerFoodService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDistributerFoodGoodsEntity;
import com.nongxinle.service.GbDistributerFoodGoodsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/gbdistributerfoodgoods")
public class GbDistributerFoodGoodsController {
	@Autowired
	private GbDistributerFoodGoodsService gbDistributerFoodGoodsService;
	@Autowired
	private GbDistributerFoodService gbDistributerFoodService;

	@RequestMapping(value = "/updateGbFoodGoods", method = RequestMethod.POST)
	@ResponseBody
	public R updateGbFoodGoods (@RequestBody GbDistributerFoodGoodsEntity foodGoods) {
	    gbDistributerFoodGoodsService.update(foodGoods);
		Integer gbDfoodgFoodId = foodGoods.getGbDfoodgFoodId();

		GbDistributerFoodEntity foodEntity = gbDistributerFoodService.queryObject(gbDfoodgFoodId);
		return R.ok().put("data", foodEntity);
	}

	@RequestMapping(value = "/deleteGbFoodGoods/{id}")
	@ResponseBody
	public R deleteGbFoodGoods(@PathVariable Integer id) {
	    gbDistributerFoodGoodsService.delete(id);
	    return R.ok();
	}

	@RequestMapping(value = "/saveGbFoodGoods", method = RequestMethod.POST)
	@ResponseBody
	public R saveGbFoodGoods (@RequestBody List<GbDistributerFoodGoodsEntity> foodGoodsList  ) {
		for (GbDistributerFoodGoodsEntity foodGoods : foodGoodsList) {
			foodGoods.setGbDfoodgStatus(1);
			gbDistributerFoodGoodsService.save(foodGoods);
		}
	    return R.ok();
	}

	

}
