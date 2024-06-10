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

import com.nongxinle.entity.GbDistributerGoodsShelfEntity;
import com.nongxinle.entity.NxDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfGoodsEntity;
import com.nongxinle.service.NxDistributerGoodsShelfGoodsService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDistributerGoodsShelfEntity;
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




	@RequestMapping(value = "/disGetToPlanPurchaseShelfGoods", method = RequestMethod.POST)
	@ResponseBody
	public R disGetToPlanPurchaseShelfGoods(Integer disId, Integer shelfId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("purStatus", 1);
		map.put("shelfId", shelfId);
		List<NxDistributerFatherGoodsEntity> shelfGoodsEntities = nxDisGoodsShelfService.disGetUnPlanShelfPurchaseApplys(map);

		return R.ok().put("data", shelfGoodsEntities);
	}


	@RequestMapping(value = "/getShelfGoods/{shelfId}")
	@ResponseBody
	public R getShelfGoods(@PathVariable Integer shelfId) {
		Map<String, Object> map = new HashMap<>();
		map.put("shelfId", shelfId);
		NxDistributerGoodsShelfEntity shelfEntity =  nxDisGoodsShelfService.queryShelfGoodsByParams(map);
		return R.ok().put("data",shelfEntity);
	}

	@RequestMapping(value = "/disGetShelfs/{disId}")
	@ResponseBody
	public R disGetShelfs(@PathVariable Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		List<NxDistributerGoodsShelfEntity> shelfEntities = nxDisGoodsShelfService.queryShelfByParams(map);
	    return R.ok().put("data", shelfEntities);
	}

	@RequestMapping(value = "/disGetShelfsWithDetail/{disId}")
	@ResponseBody
	public R disGetShelfsWithDetail(@PathVariable Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		List<NxDistributerGoodsShelfEntity> shelfEntities = nxDisGoodsShelfService.queryShelfWithDetailByParams(map);
		return R.ok().put("data", shelfEntities);
	}

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
//		List<NxDistributerGoodsShelfGoodsEntity> nxDisGoodsShelfGoodsEntities = shelf.getNxDisGoodsShelfGoodsEntities();
//
//		for (NxDistributerGoodsShelfGoodsEntity shelfGoods : nxDisGoodsShelfGoodsEntities) {
//			shelfGoods.setNxDgsgShelfId(shelf.getNxDistributerGoodsShelfId());
//			nxDisGoodsShelfGoodsService.save(shelfGoods);
//		}
		return R.ok();
	}

	@RequestMapping(value = "/deleteShelf/{shelfId}")
	@ResponseBody
	public R deleteShelf(@PathVariable Integer shelfId) {
		Map<String, Object> map = new HashMap<>();
		map.put("shelfId", shelfId);
		List<NxDistributerGoodsShelfGoodsEntity> shelfGoodsEntities =  nxDisGoodsShelfGoodsService.queryShelfGoodsByParams(map);

		for (NxDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsEntities) {
			nxDisGoodsShelfGoodsService.delete(shelfGoods.getNxDistributerGoodsShelfGoodsId());
		}
		nxDisGoodsShelfService.delete(shelfId);
	    return R.ok();
	}




	
}
