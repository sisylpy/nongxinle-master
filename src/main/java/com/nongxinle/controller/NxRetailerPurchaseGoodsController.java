package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-22 20:41
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.NxRetailerPurchaseBatchService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxRetailerPurchaseGoodsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatTime;


@RestController
@RequestMapping("api/nxretailerpurchasegoods")
public class NxRetailerPurchaseGoodsController {
	@Autowired
	private NxRetailerPurchaseGoodsService nxRetPurchaseGoodsService;
	@Autowired
	private NxRetailerPurchaseBatchService nxRetPurchaseBatchService;



	@RequestMapping(value = "/savePurGoodsWeight", method = RequestMethod.POST)
	@ResponseBody
	public R savePurGoodsWeight(@RequestBody NxRetailerPurchaseGoodsEntity purchaseGoodsEntity) {
//		Integer nxDpgInputType = purchaseGoodsEntity.getNxDpgInputType();
//		if(nxDpgInputType == 0){
//			NxDepartmentOrdersEntity nxDepartmentOrdersEntity = purchaseGoodsEntity.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntity();
//			nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);
//		}

		nxRetPurchaseGoodsService.update(purchaseGoodsEntity);
		return R.ok().put("data", purchaseGoodsEntity);
	}


	@RequestMapping(value = "/finishRetPurGoods", method = RequestMethod.POST)
	@ResponseBody
	public R finishRetPurGoods (@RequestBody List<NxRetailerPurchaseGoodsEntity> purGoods) {
		for (NxRetailerPurchaseGoodsEntity goods : purGoods) {
			goods.setNxRpgStatus(3);
			goods.setNxRpgPurchaseDate(formatWhatDate(0));
			goods.setNxRpgTime(formatWhatDayTime(0));
			goods.setNxRpgBatchId(-1);
			nxRetPurchaseGoodsService.update(goods);
		}
	    return R.ok();
	}


	@RequestMapping(value = "/getRetFinishPurGoods/{retId}")
	@ResponseBody
	public R getRetFinishPurGoods(@PathVariable Integer retId) {
	    Map<String, Object> map = new HashMap<>();

		Map<String, Object> stringObjectMap = queryDayFinish(retId, 0);
		Map<String, Object> stringObjectMap1 = queryDayFinish(retId, -1);
		Map<String, Object> stringObjectMap2 = queryDayFinish(retId, -2);
		List<Map<String, Object>> result = new ArrayList<>();
		result.add(stringObjectMap);
		result.add(stringObjectMap1);
		result.add(stringObjectMap2);
		return R.ok().put("data", result);
	}
	private Map<String,Object>  queryDayFinish(Integer retId, Integer which){
		//完成
		Map<String, Object> map3 = new HashMap<>();
		map3.put("retId", retId);
//		map3.put("equalStatus", 3);
		map3.put("finishDate", formatWhatDate(which));
		List<NxRetailerPurchaseGoodsEntity> purchaseGoods = nxRetPurchaseGoodsService.queryRetShelfPurGoodsByParame(map3);
        Map<String, Object> map = new HashMap<>();
        map.put("date", formatWhatDay(which));
        map.put("arr", purchaseGoods);
		return map;

	}

	@RequestMapping(value = "/getRetPurchaseGoods/{retId}")
	@ResponseBody
	public R getRetPurchaseGoods(@PathVariable Integer retId) {

//		Map<String, Object> map6 = new HashMap<>();
//		map6.put("retId", retId);
//		map6.put("equalStatus", 0);
//		List<NxRetailerPurchaseBatchEntity> purchaseBatchEntities1 = nxRetPurchaseBatchService.queryRetPurBatchByParams(map6);
//		if(purchaseBatchEntities1.size() > 0){
//			for (NxRetailerPurchaseBatchEntity batch : purchaseBatchEntities1) {
//				Integer nxRetailerPurchaseBatchId = batch.getNxRetailerPurchaseBatchId();
//				Map<String, Object> map7 = new HashMap<>();
//				map7.put("batchId", nxRetailerPurchaseBatchId);
//				List<NxRetailerPurchaseGoodsEntity> purchaseGoodsEntities = nxRetPurchaseGoodsService.queryRetShelfPurGoodsByParame(map7);
//
////				for (NxRetailerPurchaseGoodsEntity purGoods : purchaseGoodsEntities) {
////					purGoods.setNxRpgBatchId(null);
////					purGoods.setNxRpgStatus(0);
////					nxRetPurchaseGoodsService.update(purGoods);
////				}
////				nxRetPurchaseBatchService.delete(nxRetailerPurchaseBatchId);
//			}
//		}

		Map<String, Object> map = new HashMap<>();
		map.put("retId", retId);
		map.put("status", 1);
		List<NxRetailerPurchaseGoodsEntity> purchaseGoodsEntities = nxRetPurchaseGoodsService.queryRetShelfPurGoodsByParame(map);

		//
		Map<String, Object> map2 = new HashMap<>();
		map2.put("retId", retId);
		map2.put("status", 5 );
		List<NxRetailerPurchaseBatchEntity> purchaseBatchEntities = nxRetPurchaseBatchService.queryRetPurBatchByParams(map2);

		//完成
		Map<String, Object> map3 = new HashMap<>();
		map3.put("retId", retId);
		map3.put("equalStatus", 3);
		map3.put("purchaseDate", formatWhatDate(0));
		List<NxRetailerPurchaseGoodsEntity> purchaseGoodsEntities1 = nxRetPurchaseGoodsService.queryRetShelfPurGoodsByParame(map3);


		Map<String, Object> map1 = new HashMap<>();
		map1.put("purArr",purchaseGoodsEntities );
		map1.put("batchSize", purchaseBatchEntities.size() );
		map1.put("finishSize", purchaseGoodsEntities1.size());
	    return R.ok().put("data", map1);
	}




	@RequestMapping(value = "/upateRetPlanPurchase", method = RequestMethod.POST)
	@ResponseBody
	public R upateRetPlanPurchase (@RequestBody NxRetailerPurchaseGoodsEntity purGoods) {
		nxRetPurchaseGoodsService.update(purGoods);
	    return R.ok();
	}

	@RequestMapping(value = "/deletePurGoods/{purGoodsId}")
	@ResponseBody
	public R deletePurGoods(@PathVariable Integer purGoodsId) {

	    nxRetPurchaseGoodsService.delete(purGoodsId);
	    return R.ok();
	}

	@RequestMapping(value = "/saveRetPlanPurchase", method = RequestMethod.POST)
	@ResponseBody
	public R saveRetPlanPurchase (@RequestBody NxRetailerPurchaseGoodsEntity purGoods ) {
		purGoods.setNxRpgStatus(0);
		purGoods.setNxRpgInputType(1);
		purGoods.setNxRpgApplyDate(formatWhatDate(0));
		nxRetPurchaseGoodsService.save(purGoods);

	    return R.ok();
	}

	@RequestMapping(value = "/savePurchaseList", method = RequestMethod.POST)
	@ResponseBody
	public R savePurchaseList (@RequestBody List<NxRetailerPurchaseGoodsEntity> purGoods) {
		for (NxRetailerPurchaseGoodsEntity goods : purGoods) {
			goods.setNxRpgStatus(0);

			goods.setNxRpgApplyDate(formatWhatDate(0));

			nxRetPurchaseGoodsService.save(goods);
		}
	    return R.ok();
	}
	

	
}
