package com.nongxinle.controller;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-09 18:47
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.NxDistributerGoodsShelfGoodsService;
import com.nongxinle.service.NxDistributerService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxDistributerGoodsShelfService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxdistributergoodsshelf")
public class NxDistributerGoodsShelfController {
	@Autowired
	private NxDistributerGoodsShelfService nxDisGoodsShelfService;
	@Autowired
	private NxDistributerGoodsShelfGoodsService nxDisGoodsShelfGoodsService;
	@Autowired
	private NxDistributerService nxDistributerService;




	@RequestMapping(value = "/disGetToPlanPurchaseShelfGoods", method = RequestMethod.POST)
	@ResponseBody
	public R disGetToPlanPurchaseShelfGoods(Integer disId, Integer shelfId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("purStatus", 2);
		map.put("shelfId", shelfId);
		List<NxDistributerFatherGoodsEntity> shelfGoodsEntities = nxDisGoodsShelfService.disGetUnPlanShelfPurchaseApplys(map);

		return R.ok().put("data", shelfGoodsEntities);
	}

	@RequestMapping(value = "/updateShelfName", method = RequestMethod.POST)
	@ResponseBody
	public R updateShelfName (Integer shelfId, String shelfName) {

		NxDistributerGoodsShelfEntity shelfEntity = nxDisGoodsShelfService.queryObject(shelfId);
		shelfEntity.setNxDistributerGoodsShelfName(shelfName);
		nxDisGoodsShelfService.update(shelfEntity);
		return R.ok().put("data", shelfEntity);
	}

	@RequestMapping(value = "/updateShelfSort", method = RequestMethod.POST)
	@ResponseBody
	public R updateShelfSort (@RequestBody List<NxDistributerGoodsShelfEntity> shelfList) {

		for (int i = 0; i < shelfList.size(); i++){
			NxDistributerGoodsShelfEntity shelfEntity = shelfList.get(i);
			shelfEntity.setNxDistributerGoodsShelfSort(i + 1);
			nxDisGoodsShelfService.update(shelfEntity);
		}
		return R.ok();
	}

	@RequestMapping(value = "/getShelfGoods/{shelfId}")
	@ResponseBody
	public R getShelfGoods(@PathVariable Integer shelfId) {
		Map<String, Object> map = new HashMap<>();
		map.put("shelfId", shelfId);
//		NxDistributerGoodsShelfEntity shelfEntity =  nxDisGoodsShelfService.queryShelfGoodsByParams(map);
		List<NxDistributerGoodsShelfGoodsEntity> nxDistributerGoodsShelfGoodsEntities = nxDisGoodsShelfGoodsService.queryShelfForGoodsByParams(map);
//		List<NxDistributerGoodsShelfGoodsEntity> nxDistributerGoodsShelfGoodsEntities = nxDisGoodsShelfGoodsService.queryShelfForGoodsWithOrders(map);
		return R.ok().put("data",nxDistributerGoodsShelfGoodsEntities);
	}

	@RequestMapping(value = "/disGetShelfs/{disId}")
	@ResponseBody
	public R disGetShelfs(@PathVariable Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		List<NxDistributerGoodsShelfEntity> shelfEntities = nxDisGoodsShelfService.queryShelfByParams(map);
	    return R.ok().put("data", shelfEntities);
	}

//
//	@RequestMapping(value = "/disGetShelfsWithDetail/{disId}")
//	@ResponseBody
//	public R disGetShelfsWithDetail(@PathVariable Integer disId) {
//		Map<String, Object> map = new HashMap<>();
//		map.put("disId", disId);
//		List<NxDistributerGoodsShelfEntity> shelfEntities = nxDisGoodsShelfService.queryShelfWithDetailByParams(map);
//		return R.ok().put("data", shelfEntities);
//	}

	@RequestMapping(value = "/updateShelf", method = RequestMethod.POST)
	@ResponseBody
	public R updateShelf (@RequestBody NxDistributerGoodsShelfEntity shelfEntity ) {
//		for (int i = 0; i < shelfList.size(); i++){
//			NxDistributerGoodsShelfEntity shelfEntity = shelfList.get(i);
//			shelfEntity.setNxDistributerGoodsShelfSort(i + 1);
//			nxDisGoodsShelfService.update(shelfEntity);
//		}
		nxDisGoodsShelfService.update(shelfEntity);



		return R.ok();
	}

	@RequestMapping(value = "/saveNewShelf", method = RequestMethod.POST)
	@ResponseBody
	public R saveNewShelf (@RequestBody NxDistributerGoodsShelfEntity shelf) {
	    nxDisGoodsShelfService.save(shelf);
		Integer nxDistributerGoodsShelfDisId = shelf.getNxDistributerGoodsShelfDisId();
		NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDistributerGoodsShelfDisId);
		nxDistributerEntity.setNxDistributerShelfQuantity(nxDistributerEntity.getNxDistributerShelfQuantity() + 1);
		nxDistributerService.update(nxDistributerEntity);
		return R.ok().put("data", shelf);
	}

	@RequestMapping(value = "/deleteShelf/{shelfId}")
	@ResponseBody
	public R deleteShelf(@PathVariable Integer shelfId) {
		Map<String, Object> map = new HashMap<>();
		map.put("shelfId", shelfId);

		List<NxDistributerGoodsShelfGoodsEntity> shelfGoodsEntities =  nxDisGoodsShelfGoodsService.queryShelfForGoodsByParams(map);
		if(shelfGoodsEntities.size() > 0){
			return R.error(-1,"先删除商品");
		}else{
			NxDistributerGoodsShelfEntity shelfEntity = nxDisGoodsShelfService.queryObject(shelfId);
			Integer nxDistributerGoodsShelfDisId = shelfEntity.getNxDistributerGoodsShelfDisId();
			NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDistributerGoodsShelfDisId);
			nxDistributerEntity.setNxDistributerShelfQuantity(nxDistributerEntity.getNxDistributerShelfQuantity() - 1);
			nxDistributerService.update(nxDistributerEntity);

			nxDisGoodsShelfService.delete(shelfId);

			return R.ok();
		}

	}




	
}
