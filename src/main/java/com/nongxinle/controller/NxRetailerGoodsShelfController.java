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

import com.nongxinle.entity.*;
import com.nongxinle.service.NxRetailerGoodsShelfGoodsService;
import com.nongxinle.service.NxRetailerPurchaseGoodsService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxRetailerGoodsShelfService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxretailergoodsshelf")
public class NxRetailerGoodsShelfController {
	@Autowired
	private NxRetailerGoodsShelfService nxRetGoodsShelfService;
	@Autowired
	private NxRetailerGoodsShelfGoodsService nxRetGoodsShelfGoodsService;
	@Autowired
	private NxRetailerPurchaseGoodsService nxRetailerPurchaseGoodsService;

	@RequestMapping(value = "/deleteRetShelf/{shelfId}")
	@ResponseBody
	public R deleteRetShelf(@PathVariable Integer shelfId) {
		Map<String, Object> map = new HashMap<>();
		map.put("shelfId", shelfId);
		List<NxRetailerGoodsShelfGoodsEntity> shelfGoodsEntities =  nxRetGoodsShelfGoodsService.queryRetShelfGoodsWithPurchaseByParams(map);
		if(shelfGoodsEntities.size() > 0){
			for (NxRetailerGoodsShelfGoodsEntity shelfGoods : shelfGoodsEntities) {
				Integer nxRetailerGoodsShelfGoodsId = shelfGoods.getNxRetailerGoodsShelfGoodsId();
				Map<String, Object> map3 = new HashMap<>();
				map3.put("shelfGoodsId", nxRetailerGoodsShelfGoodsId);
				map3.put("status", 3);
				List<NxRetailerPurchaseGoodsEntity> purchaseGoodsEntities = nxRetailerPurchaseGoodsService.queryRetShelfPurGoodsByParame(map3);
				if(purchaseGoodsEntities.size() > 0){
					return R.error(-1, "有订单，等进货完成后再删除");
				}else{
					nxRetGoodsShelfGoodsService.delete(shelfGoods.getNxRetailerGoodsShelfGoodsId());
					nxRetGoodsShelfService.delete(shelfId);
					return R.ok();
				}
			}
		}else {
			nxRetGoodsShelfService.delete(shelfId);
			return R.ok();
		}
		return R.ok();
	}


	@RequestMapping(value = "/updateRetShelf", method = RequestMethod.POST)
	@ResponseBody
	public R updateRetShelf (@RequestBody NxRetailerGoodsShelfEntity shelf) {
	    nxRetGoodsShelfService.update(shelf);
	    return R.ok();
	}

	
	@RequestMapping(value = "/saveNewRetShelf", method = RequestMethod.POST)
	@ResponseBody
	public R saveNewRetShelf (@RequestBody NxRetailerGoodsShelfEntity shelf) {

	    nxRetGoodsShelfService.save(shelf);
		for (NxRetailerGoodsShelfGoodsEntity goods : shelf.getNxRetGoodsShelfGoodsEntityList()) {
			goods.setNxRgsgShelfId(shelf.getNxRetailerGoodsShelfId());
			nxRetGoodsShelfGoodsService.save(goods);
		}
		return R.ok();
	}



	@RequestMapping(value = "/retGetShelfs/{retId}")
	@ResponseBody
	public R retGetShelfs(@PathVariable Integer retId) {

		Map<String, Object> map = new HashMap<>();
		map.put("retId", retId);
		List<NxRetailerGoodsShelfEntity> shelfEntities = nxRetGoodsShelfService.queryRetShelfWithPurGoodsByParams(map);
//		for (NxRetailerGoodsShelfEntity shelf : shelfEntities) {
//			Integer nxRetailerGoodsShelfId = shelf.getNxRetailerGoodsShelfId();
//			Map<String, Object> map1 = new HashMap<>();
//			map1.put("shelfId", nxRetailerGoodsShelfId);
//			map1.put("status", 3);
//			List<NxRetailerPurchaseGoodsEntity> purchaseGoodsEntities = nxRetailerPurchaseGoodsService.queryRetShelfPurGoodsByParame(map1);
//
//			shelf.setShelfPurGoodsCount(purchaseGoodsEntities.size());
//		}
		return R.ok().put("data", shelfEntities);
	}

}
