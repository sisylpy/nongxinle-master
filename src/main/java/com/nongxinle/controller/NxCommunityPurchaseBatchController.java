package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 01-17 07:54
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.NxCommunityGoodsService;
import com.nongxinle.service.NxCommunityPurchaseGoodsService;
import com.nongxinle.service.NxRestrauntOrdersService;
import com.nongxinle.utils.R;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxCommunityPurchaseBatchService;

import static com.nongxinle.utils.DateUtils.*;


@RestController
@RequestMapping("api/nxcommunitypurchasebatch")
public class NxCommunityPurchaseBatchController {
	@Autowired
	private NxCommunityPurchaseBatchService cpbService;
	@Autowired
	private NxCommunityPurchaseGoodsService cpgService;
	@Autowired
	private NxRestrauntOrdersService nxRestrauntOrdersService;
	@Autowired
	private NxCommunityGoodsService cgService;

	@RequestMapping(value = "/commGetCommSupplierBills/{supplierId}")
	@ResponseBody
	public R commGetCommSupplierBills(@PathVariable Integer supplierId) {

		BigDecimal listTotal = new BigDecimal("0.0");
		double unSettleSubtotal = 0.0;

		//第一个月账单
		Map<String, Object> map = new HashMap<>();
		map.put("supplierId", supplierId);
		map.put("month", formatWhatMonth(0));
		map.put("dayuStatus", 1);
		String totalDec1 = "0";
		List<NxCommunityPurchaseBatchEntity> purchaseBatch = cpbService.queryComPurchaseBatchByParams(map);
		BigDecimal bigDecimal = new BigDecimal(purchaseBatch.size());
		listTotal = listTotal.add(bigDecimal); //账单数量

		Map<String, Object> map41 = new HashMap<>();
		map41.put("supplierId", supplierId);
		map41.put("month", formatWhatMonth(0));
		map41.put("equalStatus", 3);
		Integer integer = cpbService.queryCommPurchaseBatchCount(map41);
		if (integer > 0) {
			Double total1 = cpbService.queryCommSupplierUnSettleSubtotal(map41);
			unSettleSubtotal = unSettleSubtotal + total1; //未结账款总额
			totalDec1 = String.format("%.2f", total1);
		}
		Map<String, Object> map1 = new HashMap<>();
		map1.put("month", formatWhatMonth(0));
		map1.put("arr", purchaseBatch);
		map1.put("total", totalDec1);


		//第二个月账单
		Map<String, Object> map2 = new HashMap<>();
		map2.put("supplierId", supplierId);
		map2.put("month", getLastMonth());
		map2.put("dayuStatus", 1);
		List<NxCommunityPurchaseBatchEntity> purchaseBatch2 = cpbService.queryComPurchaseBatchByParams(map2);
		BigDecimal bigDecimal2 = new BigDecimal(purchaseBatch2.size());
		listTotal = listTotal.add(bigDecimal2); //账单数量

		String totalDec2 = "0";
		Map<String, Object> map42 = new HashMap<>();
		map42.put("supplierId", supplierId);
		map42.put("month", getLastMonth());
		map42.put("equalStatus", 3);
		Integer integer1 = cpbService.queryCommPurchaseBatchCount(map42);
		if (integer1 > 0) {
			Double total2 = cpbService.queryCommSupplierUnSettleSubtotal(map42);
			unSettleSubtotal = unSettleSubtotal + total2; //未结账款总额
			totalDec2 = String.format("%.2f", total2);
		}

		Map<String, Object> map3 = new HashMap<>();
		map3.put("month", getLastMonth());
		map3.put("arr", purchaseBatch2);
		map3.put("total", totalDec2);

		//第三个月账单
		Map<String, Object> map4 = new HashMap<>();
		map4.put("supplierId", supplierId);
		map4.put("month", getLastTwoMonth());
		map4.put("dayuStatus", 1);
		List<NxCommunityPurchaseBatchEntity> purchaseBatch3 = cpbService.queryComPurchaseBatchByParams(map4);
		BigDecimal bigDecimal3 = new BigDecimal(purchaseBatch3.size());
		listTotal = listTotal.add(bigDecimal3);

		String totalDec3 = "0";
		Map<String, Object> map43 = new HashMap<>();
		map43.put("supplierId", supplierId);
		map43.put("month", getLastTwoMonth());
		map43.put("equalStatus", 3);
		Integer integer2 = cpbService.queryCommPurchaseBatchCount(map43);

		if (integer2 > 0) {
			Double total3 = cpbService.queryCommSupplierUnSettleSubtotal(map43);
			unSettleSubtotal = unSettleSubtotal + total3; //未结账款总额
			totalDec3 = String.format("%.2f", total3);
		}

		Map<String, Object> map5 = new HashMap<>();
		map5.put("month", getLastTwoMonth());
		map5.put("arr", purchaseBatch3);
		map5.put("total", totalDec3);

		Map<String, Object> map111 = new HashMap<>();
		map111.put("unSettleSubtotal", unSettleSubtotal);
		map111.put("listTotal", listTotal);

		List<Map<String, Object>> result = new ArrayList<>();
		result.add(map1);
		result.add(map3);
		result.add(map5);
		result.add(map111);
		return R.ok().put("data", result);


	}

//	@RequestMapping(value = "/savePurchaseBatchDetailAll", method = RequestMethod.POST)
//	@ResponseBody
//	public R savePurchaseBatchDetailAll (@RequestBody  NxCommunityPurchaseBatchEntity batch ) {
//		//update batch
//		cpbService.update(batch);
//
//		//update purGoods
//		NxCommunityPurchaseGoodsEntity nxCommunityPurchaseGoodsEntity = batch.getNxCommunityPurchaseGoodsEntity();
//		cpgService.update(nxCommunityPurchaseGoodsEntity);
//		List<NxRestrauntOrdersEntity> nxRestrauntOrdersEntities = nxCommunityPurchaseGoodsEntity.getNxCommunityGoodsEntity().getNxRestrauntOrdersEntities();
//		for (NxRestrauntOrdersEntity orders : nxRestrauntOrdersEntities) {
//			nxRestrauntOrdersService.update(orders);
//		}
//		return R.ok();
//	}

//	@RequestMapping(value = "/savePurchaseBatchDetailOne", method = RequestMethod.POST)
//	@ResponseBody
//	public R savePurchaseBatchDetailOne (@RequestBody NxCommunityPurchaseBatchEntity batch) {
//		//update batch
//		cpbService.update(batch);
//
//		//update purGoods
//		NxCommunityPurchaseGoodsEntity nxCommunityPurchaseGoodsEntity = batch.getNxCommunityPurchaseGoodsEntity();
//		cpgService.update(nxCommunityPurchaseGoodsEntity);
//
//		//update order
//		NxRestrauntOrdersEntity nxRestrauntOrdersEntity = nxCommunityPurchaseGoodsEntity.getNxCommunityGoodsEntity().getNxRestrauntOrdersEntity();
//		nxRestrauntOrdersService.update(nxRestrauntOrdersEntity);
//
//		return R.ok();
//	}

	@RequestMapping(value = "/getPurchaseBatchDetail/{batchId}")
	@ResponseBody
	public R getPurchaseBatchDetail(@PathVariable Integer batchId) {
		Map<String, Object> map = new HashMap<>();
		map.put("batchId", batchId);
		NxCommunityPurchaseBatchEntity batchEntity = cpbService.queryBatchDetail(map);
		if(batchEntity != null){
			return R.ok().put("data", batchEntity);
		}else{
			return R.error(-1,"没有订货");
		}
	}


	@RequestMapping(value = "/finishPurchaseBatch")
	@ResponseBody
	public R finishPurchaseBatch(@RequestBody NxCommunityPurchaseBatchEntity batchEntity) {

		List<NxCommunityPurchaseGoodsEntity> purchaseGoodsEntities = batchEntity.getNxCommunityPurchaseGoodsEntities();
		for (NxCommunityPurchaseGoodsEntity purGoods : purchaseGoodsEntities) {
			List<NxRestrauntOrdersEntity> ordersEntities = purGoods.getNxCommunityGoodsEntity().getNxRestrauntOrdersEntities();

			for (NxRestrauntOrdersEntity orders : ordersEntities) {
				orders.setNxRoBuyStatus(2);
				nxRestrauntOrdersService.update(orders);
			}
			purGoods.setNxCpgStatus(2);
			cpgService.update(purGoods);
		}
		batchEntity.setNxCpbStatus(2);
		cpbService.update(batchEntity);

		return R.ok();
	}


	@RequestMapping(value = "/fillCostPurchaseBatch")
	@ResponseBody
	public R fillCostPurchaseBatch(@RequestBody NxCommunityPurchaseBatchEntity batchEntity) {

		List<NxCommunityPurchaseGoodsEntity> purchaseGoodsEntities = batchEntity.getNxCommunityPurchaseGoodsEntities();
		for (NxCommunityPurchaseGoodsEntity purGoods : purchaseGoodsEntities) {
			List<NxRestrauntOrdersEntity> ordersEntities = purGoods.getNxCommunityGoodsEntity().getNxRestrauntOrdersEntities();

			for (NxRestrauntOrdersEntity orders : ordersEntities) {
				orders.setNxRoBuyStatus(1);
				nxRestrauntOrdersService.update(orders);

				//todo 转发开发的时候要考虑订单的情况
//				String nxRoSubtotal = orders.getNxRoCostSubtotal();
//				if ( nxRoSubtotal != null && !nxRoSubtotal.equals("-1")){
//					System.out.println("jinfbuflalalla");
//					orders.setNxRoBuyStatus(1);
//					nxRestrauntOrdersService.update(orders);
//				}else{
//					System.out.println("nullll-------jinfbuflalalla");
//					orders.setNxRoBuyStatus(null);
//					nxRestrauntOrdersService.update(orders);
//				}

			}
			purGoods.setNxCpgStatus(1);
			cpgService.update(purGoods);
		}
		batchEntity.setNxCpbStatus(1);
		cpbService.update(batchEntity);

		return R.ok();
	}


//	@RequestMapping(value = "/disGetComPurchaseBatch/{disId}")
//	@ResponseBody
//	public R disGetComPurchaseBatch(@PathVariable Integer disId) {
//		Map<String, Object> map = new HashMap<>();
//		map.put("disId", disId);
//		map.put("status", 0);
//		List<NxCommunityPurchaseBatchEntity> batchEntities = cpbService.queryComPurchaseBatchByParams(map);
//		return R.ok().put("data", batchEntities);
//	}



	@RequestMapping(value = "/comGetBuyingGoods", method = RequestMethod.POST)
	@ResponseBody
	public R comGetBuyingGoods(Integer comId, Integer type) {

		Map<String, Object> map = new HashMap<>();
		map.put("comId", comId);
		map.put("type", type);
		map.put("status", 2);
		List<NxCommunityPurchaseBatchEntity> batchEntities = cpbService.queryComPurchaseBatchByParams(map);
		return R.ok().put("data", batchEntities);
	}



	
}
