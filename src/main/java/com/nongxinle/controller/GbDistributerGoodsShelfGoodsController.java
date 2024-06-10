package com.nongxinle.controller;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 08-19 09:57
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.NxDistributerPurchaseGoodsService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDistributerGoodsShelfGoodsEntity;
import com.nongxinle.service.GbDistributerGoodsShelfGoodsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@RestController
@RequestMapping("api/gbdistributergoodsshelfgoods")
public class GbDistributerGoodsShelfGoodsController {
	@Autowired
	private GbDistributerGoodsShelfGoodsService gbDisGoodsShelfGoodsService;
	@Autowired
	private NxDistributerPurchaseGoodsService nxDisPurchaseGoodsService;




	@RequestMapping(value = "/deleteShelfGoodsGb/{id}")
	@ResponseBody
	public R deleteShelfGoodsGb(@PathVariable Integer id) {
		GbDistributerGoodsShelfGoodsEntity gbDisGoodsShelfGoods = gbDisGoodsShelfGoodsService.queryObject(id);
		Integer gbDgsgSort = gbDisGoodsShelfGoods.getGbDgsgSort();
		Integer gbDgsgShelfId = gbDisGoodsShelfGoods.getGbDgsgShelfId();
		Map<String, Object> map = new HashMap<>();
		map.put("dayuSort", gbDgsgSort - 1 );
		map.put("shelfId", gbDgsgShelfId);
		List<GbDistributerGoodsShelfGoodsEntity> gbDistGoodsShelfEntities = gbDisGoodsShelfGoodsService.queryShelfGoodsByParams(map);
		System.out.println(gbDistGoodsShelfEntities.size());
		for(int i = 0; i < gbDistGoodsShelfEntities.size(); i++){
			GbDistributerGoodsShelfGoodsEntity shelfGoodsEntity = gbDistGoodsShelfEntities.get(i);
			shelfGoodsEntity.setGbDgsgSort(shelfGoodsEntity.getGbDgsgSort()-1);
			gbDisGoodsShelfGoodsService.update(shelfGoodsEntity);
		}
	    gbDisGoodsShelfGoodsService.delete(id);
	    return R.ok();
	}


	@RequestMapping(value = "/addShelfGoodsGb", method = RequestMethod.POST)
	@ResponseBody
	public R addShelfGoodsGb (@RequestBody List<GbDistributerGoodsShelfGoodsEntity> shelfGoodsList  ) {

		GbDistributerGoodsShelfGoodsEntity gbDistributerGoodsShelfGoodsEntity = shelfGoodsList.get(0);
		Integer gbDgsgSort = gbDistributerGoodsShelfGoodsEntity.getGbDgsgSort();
		Integer gbDgsgShelfId = gbDistributerGoodsShelfGoodsEntity.getGbDgsgShelfId();

		Map<String, Object> map = new HashMap<>();
		map.put("dayuSort", gbDgsgSort - 1 );
		map.put("shelfId", gbDgsgShelfId);
		System.out.println("kkmap===="+ map);
		List<GbDistributerGoodsShelfGoodsEntity> gbDistGoodsShelfEntities = gbDisGoodsShelfGoodsService.queryShelfGoodsByParams(map);
		System.out.println(gbDistGoodsShelfEntities.size());
		for(int i = 0; i < gbDistGoodsShelfEntities.size(); i++){
			int size = shelfGoodsList.size();
			gbDistGoodsShelfEntities.get(i).setGbDgsgSort(size+ i + gbDgsgSort);
			gbDisGoodsShelfGoodsService.update(gbDistGoodsShelfEntities.get(i));
		}

		for (GbDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsList) {

			gbDisGoodsShelfGoodsService.save(shelfGoods);
		}
		return R.ok();
	}


	@RequestMapping(value = "/updateShelfGoodsSortGb", method = RequestMethod.POST)
	@ResponseBody
	public R updateShelfGoodsSortGb(@RequestBody List<GbDistributerGoodsShelfGoodsEntity> shelfGoodsList) {
		for (GbDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsList) {
			gbDisGoodsShelfGoodsService.update(shelfGoods);
		}
		return R.ok();
	}

//	@RequestMapping(value = "/deleteShelfGoods/{id}")
//	@ResponseBody
//	public R deleteShelfGoods(@PathVariable Integer id) {
//		GbDistributerGoodsShelfGoodsEntity gbDisGoodsShelfGoodsEntity = gbDisGoodsShelfGoodsService.queryObject(id);
//		Integer nxDgsgDisGoodsId = gbDisGoodsShelfGoodsEntity.getGbDgsgDisGoodsId();
//		Map<String, Object> map = new HashMap<>();
//		map.put("disGoodsId", nxDgsgDisGoodsId);
//		map.put("status", 4);
//		List<NxDistributerPurchaseGoodsEntity> disPurGoodsEntities = nxDisPurchaseGoodsService.queryForDisGoods(map);
//		if(disPurGoodsEntities.size() > 0){
//			return R.error(-1, "有订货");
//		}else{
//			gbDisGoodsShelfGoodsService.delete(id);
//			return R.ok();
//		}
//	}
	
}
