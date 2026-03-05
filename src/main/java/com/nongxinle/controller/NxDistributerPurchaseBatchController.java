package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-25 22:52
 */

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import javax.swing.plaf.basic.BasicIconFactory;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisPurchaseBatchDisUserFinish;


@RestController
@RequestMapping("api/nxdistributerpurchasebatch")
public class NxDistributerPurchaseBatchController {
	@Autowired
	private NxDistributerPurchaseBatchService nxDPBService;
	@Autowired
	private NxDistributerPurchaseGoodsService dpgService;
	@Autowired
	private NxDepartmentOrdersService nxDepartmentOrdersService;
	@Autowired
	private NxBuyUserService nxBuyUserService;
	@Autowired
	private NxDistributerGoodsService dgService;
	@Autowired
	private NxJrdhSupplierService nxJrdhSupplierService;
	@Autowired
	private NxDistributerService nxDistributerService;
	@Autowired
	private GbDepartmentOrdersService gbDepartmentOrdersService;
	@Autowired
	private NxDistributerGoodsShelfStockService shelfStockService;
	@Autowired
	private NxDistributerUserService nxDistributerUserService;
	@Autowired
	private NxDistributerPurchaseBatchService nxDistributerPurchaseBatchService;


	@RequestMapping(value = "/sellerReceiveReturnBillNx")
	@ResponseBody
	public R sellerReceiveReturnBillNx(@RequestBody NxDistributerPurchaseBatchEntity batchEntity) {

		BigDecimal tuihuo = new BigDecimal(0);
		List<NxDistributerPurchaseGoodsEntity> nxDPBEntities = batchEntity.getNxDPGEntities();
		for (NxDistributerPurchaseGoodsEntity purGoods : nxDPBEntities) {
			Integer nxDistributerPurchaseGoodsId = purGoods.getNxDistributerPurchaseGoodsId();
			NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = dpgService.queryObject(nxDistributerPurchaseGoodsId);
			String gbDpgBuySubtotal = purchaseGoodsEntity.getNxDpgBuySubtotal();
			tuihuo = tuihuo.add(new BigDecimal(gbDpgBuySubtotal));
			purchaseGoodsEntity.setNxDpgStatus(3);
			dpgService.update(purchaseGoodsEntity);

			Map<String, Object> map = new HashMap<>();
			map.put("purGoodsId", purGoods.getNxDistributerPurchaseGoodsId());
			List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
			if (nxDepartmentOrdersEntities.size() > 0) {
				for (NxDepartmentOrdersEntity ordersEntity : nxDepartmentOrdersEntities) {
					ordersEntity.setNxDoStatus(4);
					ordersEntity.setNxDoPurchaseStatus(6);
					nxDepartmentOrdersService.update(ordersEntity);

				}
			}
		}

		batchEntity.setNxDpbStatus(3);
		batchEntity.setNxDpbTime(formatFullTime());
		nxDPBService.update(batchEntity);
		return R.ok();
	}

//	@RequestMapping(value = "/purDelLastBatch/{batchId}")
//	@ResponseBody
//	public R purDelLastBatch(@PathVariable Integer batchId) {
//
//		NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryBatchWithOrders(batchId);
//		for (NxDistributerPurchaseGoodsEntity purGoods : nxDistributerPurchaseBatchEntity.getNxDPGEntities()) {
//
//			Integer nxDistributerPurchaseGoodsId = purGoods.getNxDistributerPurchaseGoodsId();
//			NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = dpgService.queryObject(nxDistributerPurchaseGoodsId);
//
//			purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishPay());
//			purchaseGoodsEntity.setNxDpgBuyPrice("0.0");
//			purchaseGoodsEntity.setNxDpgBuyQuantity("0.0");
//			purchaseGoodsEntity.setNxDpgBuySubtotal("0.0");
//			purchaseGoodsEntity.setNxDpgPurchaseType(-1);
//
//			dpgService.update(purchaseGoodsEntity);
//
//		}
//
//			nxDPBService.delete(batchId);
//		return R.ok();
//	}



	@RequestMapping(value = "/disUserPurchaserPurBatch/{userId}")
	@ResponseBody
	public R disUserPurchaserPurBatch(@PathVariable Integer userId) {
		Map<String, Object> map = new HashMap<>();
		map.put("purUserId", userId);
		map.put("dayuStatus", 1);
		map.put("status", 5);

		List<NxDistributerPurchaseBatchEntity> purchaseBatch = nxDPBService.queryDisPurchaseBatch(map);

		return R.ok().put("data", purchaseBatch);
	}


	@RequestMapping(value = "/buyerGetFinishBatch/{buyerId}")
	@ResponseBody
	public R buyerGetFinishBatch(@PathVariable Integer buyerId) {
		//today
		Map<String, Object> mapZero = new HashMap<>();
		mapZero.put("buyerId", buyerId);
		mapZero.put("status", 4);
		mapZero.put("dayuStatus", 1);
		mapZero.put("date", formatWhatDay(0));
	    List<NxDistributerPurchaseBatchEntity> purchaseBatchDayWorkZero =  nxBuyUserService.queryBuyerPurchaseBatchDayWork(mapZero);
		Map<String, Object> mapZeroData = new HashMap<>();
		mapZeroData.put("arr", purchaseBatchDayWorkZero );
		mapZeroData.put("day", formatWhatDay(0));

		//one
		Map<String, Object> mapOne = new HashMap<>();
		mapOne.put("buyerId", buyerId);
		mapOne.put("status", 4);
		mapOne.put("dayuStatus", 1);
		mapOne.put("date", formatWhatDay(-1));
		List<NxDistributerPurchaseBatchEntity> purchaseBatchDayWorkOne =  nxBuyUserService.queryBuyerPurchaseBatchDayWork(mapOne);
		Map<String, Object> mapOneData = new HashMap<>();
		mapOneData.put("arr", purchaseBatchDayWorkOne );
		mapOneData.put("day", formatWhatDay(-1) );

		//two
		Map<String, Object> mapTwo = new HashMap<>();
		mapTwo.put("buyerId", buyerId);
		mapTwo.put("status", 4);
		mapTwo.put("dayuStatus", 1);
		mapTwo.put("date", formatWhatDay(-2));
		List<NxDistributerPurchaseBatchEntity> purchaseBatchDayWorkTwo =  nxBuyUserService.queryBuyerPurchaseBatchDayWork(mapTwo);
		Map<String, Object> mapTwoData = new HashMap<>();
		mapTwoData.put("arr", purchaseBatchDayWorkTwo );
		mapTwoData.put("day", formatWhatDay(-2) );

		//three
		Map<String, Object> mapThree = new HashMap<>();
		mapThree.put("buyerId", buyerId);
		mapThree.put("status", 4);
		mapThree.put("dayuStatus", 1);
		mapThree.put("date", formatWhatDay(-3));
		List<NxDistributerPurchaseBatchEntity> purchaseBatchDayWorkThree =  nxBuyUserService.queryBuyerPurchaseBatchDayWork(mapThree);
		Map<String, Object> mapThreeData = new HashMap<>();
		mapThreeData.put("arr", purchaseBatchDayWorkThree );
		mapThreeData.put("day", formatWhatDay(-3) );

		//four
		Map<String, Object> mapFour = new HashMap<>();
		mapFour.put("buyerId", buyerId);
		mapFour.put("status", 4);
		mapFour.put("dayuStatus", 1);
		mapFour.put("date", formatWhatDay(-4));
		List<NxDistributerPurchaseBatchEntity> purchaseBatchDayWorkFour =  nxBuyUserService.queryBuyerPurchaseBatchDayWork(mapFour);
		Map<String, Object> mapFourData = new HashMap<>();
		mapFourData.put("arr", purchaseBatchDayWorkFour );
		mapFourData.put("day", formatWhatDay(-4) );

		//five
		Map<String, Object> mapFive = new HashMap<>();
		mapFive.put("buyerId", buyerId);
		mapFive.put("status", 4);
		mapFive.put("dayuStatus", 1);
		mapFive.put("date", formatWhatDay(-5));
		List<NxDistributerPurchaseBatchEntity> purchaseBatchDayWorkFive =  nxBuyUserService.queryBuyerPurchaseBatchDayWork(mapFive);
		Map<String, Object> mapFiveData = new HashMap<>();
		mapFiveData.put("arr", purchaseBatchDayWorkFive );
		mapFiveData.put("day", formatWhatDay(-5) );


		//six
		Map<String, Object> mapSix = new HashMap<>();
		mapSix.put("buyerId", buyerId);
		mapSix.put("status", 4);
		mapSix.put("dayuStatus", 1);
		mapSix.put("date", formatWhatDay(-6));
		List<NxDistributerPurchaseBatchEntity> purchaseBatchDayWorkSix =  nxBuyUserService.queryBuyerPurchaseBatchDayWork(mapSix);
		Map<String, Object> mapSixData = new HashMap<>();
		mapSixData.put("arr", purchaseBatchDayWorkSix );
		mapSixData.put("day", formatWhatDay(-6) );

		List<Map<String, Object>> result = new ArrayList<>();
		result.add(mapZeroData);
		result.add(mapOneData);
		result.add(mapThreeData);
		result.add(mapFourData);
		result.add(mapFiveData);
		result.add(mapSixData);

		return R.ok().put("data", result);
	}
	@RequestMapping(value = "/purUserGetPurchasingBatch",method = RequestMethod.POST)
	@ResponseBody
	public R purUserGetPurchasingBatch(Integer disId, Integer status) {

		Map<String, Object> map2 = new HashMap<>();
		map2.put("disId", disId);
		map2.put("status", status);
		List<NxDistributerPurchaseBatchEntity> batchEntities = nxDPBService.queryDisPurchaseBatch(map2);
		return R.ok().put("data", batchEntities);


	}



	@RequestMapping(value = "/disGetPurchasingBatch",method = RequestMethod.POST)
	@ResponseBody
	public R disGetPurchasingBatch(Integer disId, Integer type) {


		Map<String, Object> map2 = new HashMap<>();
		map2.put("disId", disId);
		map2.put("status", 2);
//		map2.put("purchaseType", type);
		System.out.println("map222" + map2);
		List<NxDistributerPurchaseBatchEntity> batchEntities = nxDPBService.queryDisPurchaseBatchSimple(map2);

		Map<String, Object> mapR = new HashMap<>();
		mapR.put("arr", batchEntities);
		Map<String, Object> map111 = new HashMap<>();
		map111.put("disId", disId);
		map111.put("purStatus", 4);
		map111.put("dayuOrderStatus", -2);
		// 出库
		map111.put("goodsType", -1);
		int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

		map111.put("goodsType", 1);
//
		int puringCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

		map111.put("batchId", 0);
		int unPurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
		map111.put("batchId", 1);
		int havePurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
		//协作订单数量
		Map<String, Object> mapcoll = new HashMap<>();
		mapcoll.put("disId", disId);
		mapcoll.put("purStatus", 4);
		mapcoll.put("status", 3);
		mapcoll.put("hasCollOrder", 1);

		Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(mapcoll);


		mapR.put("collCount", integer);
		mapR.put("unPurCount", unPurCount);
		mapR.put("havePurCount", havePurCount);
		mapR.put("stockCount", stockCount);
		mapR.put("puringCount", puringCount);
		return R.ok().put("data", mapR);
	}

	@RequestMapping(value = "/updatePasteBatch", method = RequestMethod.POST)
	@ResponseBody
	public R updatePasteBatch (String content, Integer batchId) {
		NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryObject(batchId);
		nxDistributerPurchaseBatchEntity.setNxDpbPasteContent(content);
		nxDPBService.update(nxDistributerPurchaseBatchEntity);

		return R.ok();
	}



	@RequestMapping(value = "/purchaseGetPasteBatch",method = RequestMethod.POST)
	@ResponseBody
	public R purchaseGetPasteBatch(Integer disId, Integer type) {


		Map<String, Object> map2 = new HashMap<>();
		map2.put("disId", disId);
		map2.put("status", 2);
//		map2.put("purchaseType", type);
		
		System.out.println("map222disGetPasteBatch" + map2);
		List<NxDistributerPurchaseBatchEntity> batchEntities = nxDistributerPurchaseBatchService.queryDisPurchaseBatch(map2);

		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("equalStatus", 0);
		map.put("purchaseType", 11);
		int count = dpgService.queryPurchaseGoodsCount(map);
		map.put("equalStatus", null);
		map.put("purchaseType", 12);
		map.put("dayuStatus",0);
		map.put("status",3);
		int countbuying = dpgService.queryPurchaseGoodsCount(map);
		Map<String, Object> mapR = new HashMap<>();
		mapR.put("arr", batchEntities);
		mapR.put("unBuyCount", count);
		mapR.put("buyingCount", countbuying);

		return R.ok().put("data", mapR);
	}



	@RequestMapping(value = "/disGetPasteBatch",method = RequestMethod.POST)
	@ResponseBody
	public R disGetPasteBatch(Integer disId, Integer type) {

		Map<String, Object> map2 = new HashMap<>();
		map2.put("disId", disId);
		map2.put("batchType", type);
		System.out.println("map222disGetPasteBatch" + map2);
		List<NxDistributerPurchaseBatchEntity> batchEntities = nxDepartmentOrdersService.queryDisPurchaseBatchDto(map2);

		Map<String, Object> map111 = new HashMap<>();
		map111.put("arr", batchEntities);

		Map<String, Object> map1 = new HashMap<>();
		map1.put("disId", disId);
		map1.put("status", 3);
		map1.put("purStatus", 4);
		// 未采购
		// 出库
		map1.put("goodsType", -1);
		int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);
		System.out.println("sttockckckkckc" + stockCount);
		map1.put("goodsType", 1);
		map1.put("batchId", 0);
		int unPurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);
		map1.put("batchId", 1);
		int puringCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);


		map1.put("batchType", 2);

		System.out.println("wokakkkskks" + map1);
		Integer printCount = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
		map1.put("batchType", 1);
		Integer pasteCount = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
		map1.put("batchType", 3);
		Integer wxCount = nxDepartmentOrdersService.queryDepOrdersAcount(map1);

		map111.put("unPurCount", unPurCount);
		map111.put("printCount", printCount);
		map111.put("pasteCount", pasteCount);
		map111.put("stockCount", stockCount);
		map111.put("puringCount", puringCount);
		map111.put("wxCount", wxCount);
		return R.ok().put("data", map111);
	}


	@RequestMapping(value = "/disGetPurchasingBatchReply", method = RequestMethod.POST)
	@ResponseBody
	public R disGetPurchasingBatchReply(Integer disId, Integer type) {

		Map<String, Object> map2 = new HashMap<>();
		map2.put("disId", disId);
		map2.put("equalStatus", 1);
		map2.put("purchaseType", type);
		System.out.println("whwhhwh" + map2);
		List<NxDistributerPurchaseBatchEntity> batchEntities = nxDPBService.queryDisPurchaseBatch(map2);

		Map<String, Object> map111 = new HashMap<>();
		map111.put("arr", batchEntities);
		Map<String, Object> map1 = new HashMap<>();
		map1.put("disId", disId);
		map1.put("status", 3);
		map1.put("purType", 1);
		map1.put("equalPurStatus", 1);
		// 未采购
		int unPurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

		Map<String, Object> map4 = new HashMap<>();
		map4.put("disId", disId);
		map4.put("equalInputType", 1);
		// map4 订货已发送
		map4.put("status", 3);
		map4.put("orderStatus", 3);
		map4.put("batchId", 1);
		map4.put("dayuPurStatus", 1);
		map4.put("purStatus", 3);
		Integer wxIsBatchCountUnReply = dpgService.queryPurOrderCount(map4);
		// map4 订货已回复
		map4.put("status", 4);
		map4.put("dayuStatus", 1);
		Integer wxIsBatchCountHaveReply = dpgService.queryPurOrderCount(map4);

		map111.put("unPurCount", unPurCount);
		map111.put("isBatchCountUnRepaly", wxIsBatchCountUnReply);
		map111.put("isBatchCountHaveRepaly", wxIsBatchCountHaveReply);

		return R.ok().put("data", map111);
	}



	@RequestMapping(value = "/finishPayPurchaseBatch", method = RequestMethod.POST)
	@ResponseBody
	public R finishPayPurchaseBatch (@RequestBody List<NxDistributerPurchaseBatchEntity> purList) {
	    for(NxDistributerPurchaseBatchEntity batchEntity: purList){

	    	Map<String, Object> map = new HashMap<>();
	    	map.put("batchId", batchEntity.getNxDistributerPurchaseBatchId());
			List<NxDistributerPurchaseGoodsEntity> distributerPurchaseGoodsEntities = dpgService.queryPurchaseGoodsByParams(map);
			for (NxDistributerPurchaseGoodsEntity purGoods : distributerPurchaseGoodsEntities) {
				purGoods.setNxDpgStatus(getNxDisPurchaseGoodsFinishPay());
				dpgService.update(purGoods);
			}
			batchEntity.setNxDpbPayFullTime(formatFullTime());
			batchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinishPay());
			nxDPBService.update(batchEntity);
		}
	    return R.ok();
	}




	@RequestMapping(value = "/disCheckUnPayBills", method = RequestMethod.POST)
	@ResponseBody
	public R disCheckUnPayBills (Integer disId, Integer supplierId) {

		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("supplierId", supplierId);
		map.put("status", getNxDisPurchaseBatchDisUserFinishPay());
		map.put("payType", 1);
		List<NxDistributerPurchaseBatchEntity> batchEntities = nxDPBService.queryDisPurchaseBatch(map);
		Map<String, Object> map1 = new HashMap<>();
		map1.put("disId", disId);
		map1.put("supplierId", supplierId);
		map1.put("payType", 1);
		map1.put("equalStatus", getNxDisPurchaseBatchDisUserFinishPay());
		int i = nxDPBService.queryDisPurchaseBatchCount(map1);
		Double decimal = 0.0;
		if(i > 0){
			decimal = nxDPBService.queryDisPurchaseBatchTotal(map1);
		}
		Map<String, Object> map2 = new HashMap<>();
		map2.put("arr", batchEntities);
		map2.put("total", new BigDecimal(decimal).setScale(1,BigDecimal.ROUND_HALF_UP).toString());

		return R.ok().put("data", map2);

	}


	@RequestMapping(value = "/disGetGbSupplierBillsWithStatus", method = RequestMethod.POST)
	@ResponseBody
	public R disGetGbSupplierBillsWithStatus(Integer supplierId, String status, Integer disId, String startDate, String stopDate) {

		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("supplierId", supplierId);
		map.put("notEqualPurchaseType", -1);
		System.out.println("whwwhjwwh" +  map);
		if (status.equals("all")) {
			map.put("dayuStatus", 0);
//			map.put("purchaseType", 12);
		} else if (status.equals("allUnPay")) {
			map.put("equalStatus", 2);
		} else if (status.equals("havePayed")) {
			map.put("equalStatus", 3);
		} else if (status.equals("unPayBills")) {
			map.put("purchaseType", null);
			map.put("equalStatus", 2);
		} else if (status.equals("unPayReturnBills")) {
			map.put("equalStatus", 2);
			map.put("notEqualPurchaseType", null);
			map.put("purchaseType", 9);
		}

		map.put("startDate", startDate);
		map.put("stopDate", stopDate);
		List<NxDistributerPurchaseBatchEntity> batchEntities = nxDistributerPurchaseBatchService.queryDisPurchaseBatch(map);

		return R.ok().put("data", batchEntities);
	}



	@RequestMapping(value = "/sellerDistributerPurchaseBatchs", method = RequestMethod.POST)
	@ResponseBody
	public R sellerDistributerPurchaseBatchs (Integer disId, Integer supplierId) {
		System.out.println("======== sellerDistributerPurchaseBatchs 开始 ========");
		System.out.println("参数 - disId: " + disId + ", supplierId: " + supplierId);
		
		try {

		//第一个月
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("supplierId", supplierId);
		map.put("month", formatWhatMonth(0));
		map.put("year", formatWhatYear(0));
		System.out.println("【第一个月】查询参数: " + map);
		List<NxDistributerPurchaseBatchEntity> batchEntities = nxDPBService.queryDisPurchaseBatch(map);
		System.out.println("【第一个月】批次数量: " + batchEntities.size());
		for (int i = 0; i < batchEntities.size(); i++) {
			NxDistributerPurchaseBatchEntity batch = batchEntities.get(i);
			System.out.println("批次[" + i + "] ID=" + batch.getNxDistributerPurchaseBatchId() + 
				", sellSubtotal=" + batch.getNxDpbSellSubtotal() + 
				", status=" + batch.getNxDpbStatus() + 
				", purchaseType=" + batch.getNxDpbPurchaseType());
		}

		map.put("equalStatus", 2);
		map.put("notEqualPurchaseType", 9);
		Double unPayOrderDouble = 0.0; // 未结账订单
		Double unPayReturn = 0.0; // 未记账退货
		Double havePayOrderDouble = 0.0; // 已结账订单
		Double havePayReturn = 0.0; // 已结账退货

		//未结账订单
		System.out.println("【第一个月-未结账订单】查询参数: " + map);
		Integer unPayCount = nxDPBService.queryDisPurchaseBatchCount(map);
		System.out.println("【第一个月-未结账订单】数量: " + unPayCount);
		if (unPayCount > 0) {
			unPayOrderDouble = nxDPBService.querySupplierUnSettleSubtotal(map);
			System.out.println("【第一个月-未结账订单】金额: " + unPayOrderDouble);
		}
		// //未结账退货
			map.put("notEqualPurchaseType", null);
		map.put("purchaseType", 9);
		System.out.println("【第一个月-未结账退货】查询参数: " + map);
		Integer unPayTuihuoCount = nxDPBService.queryDisPurchaseBatchCount(map);
		System.out.println("【第一个月-未结账退货】数量: " + unPayTuihuoCount);
		if (unPayTuihuoCount > 0) {
			unPayReturn = nxDPBService.querySupplierUnSettleSubtotal(map);
			System.out.println("【第一个月-未结账退货】金额: " + unPayReturn);
		}
		//已结账订单
		map.put("equalStatus", 3);
		map.put("notEqualPurchaseType", 9);
		map.put("purchaseType", null);
		System.out.println("【第一个月-已结账订单】查询参数: " + map);
		Integer havePayCount = nxDPBService.queryDisPurchaseBatchCount(map);
		System.out.println("【第一个月-已结账订单】数量: " + havePayCount);
		if (havePayCount > 0) {
			havePayOrderDouble = nxDPBService.querySupplierUnSettleSubtotal(map);
			System.out.println("【第一个月-已结账订单】金额: " + havePayOrderDouble);
		}

		// 已结账退货
		map.put("notEqualPurchaseType", null);
		map.put("purchaseType", 9);
		System.out.println("【第一个月-已结账退货】查询参数: " + map);
		Integer havePayTuihuoCount = nxDPBService.queryDisPurchaseBatchCount(map);
		System.out.println("【第一个月-已结账退货】数量: " + havePayTuihuoCount);
		if (havePayTuihuoCount > 0) {
			havePayReturn = nxDPBService.querySupplierUnSettleSubtotal(map);
			System.out.println("【第一个月-已结账退货】金额: " + havePayReturn);
		}


		//计算结果:
		//订单数量
		int billCount = unPayCount + havePayCount;
		//订单金额
		double billTotal = unPayOrderDouble + havePayOrderDouble;

		//已结订单
		int havePayCountTotal = havePayCount + havePayTuihuoCount;

		//已结金额
		double havePayTotl = havePayOrderDouble - havePayReturn;

		//实际未接金额
		double actPayTotal = unPayOrderDouble - unPayReturn;

		//实际订单数量
		int actPayCountTotal = unPayCount + unPayTuihuoCount;
		
		System.out.println("【第一个月-计算结果】订单数量: " + billCount + ", 订单金额: " + billTotal);
		System.out.println("【第一个月-计算结果】已结订单: " + havePayCountTotal + ", 已结金额: " + havePayTotl);
		System.out.println("【第一个月-计算结果】实际未结数量: " + actPayCountTotal + ", 实际未结金额: " + actPayTotal);

		Map<String, Object> mapDataOne = new HashMap<>();
		mapDataOne.put("billCount", billCount);
		mapDataOne.put("billTotal", String.format("%.1f", billTotal));

		mapDataOne.put("unPayCount", unPayCount);
		mapDataOne.put("unPayTotal", String.format("%.1f", unPayOrderDouble));

		mapDataOne.put("havePayCount", havePayCountTotal);
		mapDataOne.put("havePayTotal", String.format("%.1f", havePayTotl));

		mapDataOne.put("returnBillCount", unPayTuihuoCount);
		mapDataOne.put("returnPayTotal", String.format("%.1f", unPayReturn));

		mapDataOne.put("actBillCount", actPayCountTotal);
		mapDataOne.put("actPayTotal", String.format("%.1f", actPayTotal));


		//第二个月
		System.out.println("\n======== 第二个月查询开始 ========");
		Map<String, Object> map1 = new HashMap<>();
		map1.put("disId", disId);
		map1.put("supplierId", supplierId);
		map1.put("month", getLastMonth());
		map1.put("year", formatWhatYear(0));
		System.out.println("【第二个月】查询参数: " + map1);
		List<NxDistributerPurchaseBatchEntity> batchEntities1 = nxDPBService.queryDisPurchaseBatch(map1);
		System.out.println("【第二个月】批次数量: " + batchEntities1.size());

		map1.put("equalStatus", 2);
		map1.put("notEqualPurchaseType", 9);
		Double unPayOrderDoubleOne = 0.0; // 未结账订单
		Double unPayReturnOne = 0.0; // 未记账退货
		Double havePayOrderDoubleOne = 0.0; // 已结账订单
		Double havePayReturnOne = 0.0; // 已结账退货
		//未结账订单
		System.out.println("【第二个月-未结账订单】查询参数: " + map1);
		Integer unPayCountOne = nxDPBService.queryDisPurchaseBatchCount(map1);
		System.out.println("【第二个月-未结账订单】数量: " + unPayCountOne);
		if (unPayCountOne > 0) {
			unPayOrderDoubleOne = nxDPBService.querySupplierUnSettleSubtotal(map1);
			System.out.println("【第二个月-未结账订单】金额: " + unPayOrderDoubleOne);
		}
		// //未结账退货
		map1.put("notEqualPurchaseType", null);
		map1.put("purchaseType", 9);
		System.out.println("【第二个月-未结账退货】查询参数: " + map1);
		Integer unPayTuihuoCountOne = nxDPBService.queryDisPurchaseBatchCount(map1);
		System.out.println("【第二个月-未结账退货】数量: " + unPayTuihuoCountOne);
		if (unPayTuihuoCountOne > 0) {
			unPayReturnOne = nxDPBService.querySupplierUnSettleSubtotal(map1);
			System.out.println("【第二个月-未结账退货】金额: " + unPayReturnOne);
		}
		//已结账订单
		map1.put("equalStatus", 3);
		map1.put("notEqualPurchaseType", 9);
		map1.put("purchaseType", null);
		System.out.println("【第二个月-已结账订单】查询参数: " + map1);
		Integer havePayCountOne = nxDPBService.queryDisPurchaseBatchCount(map1);
		System.out.println("【第二个月-已结账订单】数量: " + havePayCountOne);
		if (havePayCountOne > 0) {
			havePayOrderDoubleOne = nxDPBService.querySupplierUnSettleSubtotal(map1);
			System.out.println("【第二个月-已结账订单】金额: " + havePayOrderDoubleOne);
		}

		// 已结账退货
		map1.put("notEqualPurchaseType", null);
		map1.put("purchaseType", 9);
		System.out.println("【第二个月-已结账退货】查询参数: " + map1);
		Integer havePayTuihuoCountOne = nxDPBService.queryDisPurchaseBatchCount(map1);
		System.out.println("【第二个月-已结账退货】数量: " + havePayTuihuoCountOne);
		if (havePayTuihuoCountOne > 0) {
			havePayReturnOne = nxDPBService.querySupplierUnSettleSubtotal(map1);
			System.out.println("【第二个月-已结账退货】金额: " + havePayReturnOne);
		}


		//计算结果:
		//订单数量
		int billCountOne = unPayCountOne + havePayCountOne;
		//订单金额
		double billTotalOne = unPayOrderDoubleOne + havePayOrderDoubleOne;


		//已结订单
		int havePayCountTotalOne = havePayCountOne + havePayTuihuoCountOne;

		//已结金额
		double havePayTotlOne = havePayOrderDoubleOne - havePayReturnOne;

		//实际未接金额
		double actPayTotalOne = unPayOrderDoubleOne - unPayReturnOne;
		//实际订单数量
		int actPayCountTotalOne = unPayCountOne + unPayTuihuoCountOne;
		
		System.out.println("【第二个月-计算结果】订单数量: " + billCountOne + ", 订单金额: " + billTotalOne);
		System.out.println("【第二个月-计算结果】已结订单: " + havePayCountTotalOne + ", 已结金额: " + havePayTotlOne);
		System.out.println("【第二个月-计算结果】实际未结数量: " + actPayCountTotalOne + ", 实际未结金额: " + actPayTotalOne);

		Map<String, Object> mapDataTwo = new HashMap<>();
		mapDataTwo.put("billCount", billCountOne);
		mapDataTwo.put("billTotal", String.format("%.1f", billTotalOne));

		mapDataTwo.put("unPayCount", unPayCountOne);
		mapDataTwo.put("unPayTotal", String.format("%.1f", unPayOrderDoubleOne));

		mapDataTwo.put("havePayCount", havePayCountTotalOne);
		mapDataTwo.put("havePayTotal", String.format("%.1f", havePayTotlOne));

		mapDataTwo.put("returnBillCount", unPayTuihuoCountOne);
		mapDataTwo.put("returnPayTotal", String.format("%.1f", unPayReturnOne));

		mapDataTwo.put("actBillCount", actPayCountTotalOne);
		mapDataTwo.put("actPayTotal", String.format("%.1f", actPayTotalOne));


		//第三个月
		System.out.println("\n======== 第三个月查询开始 ========");
		Map<String, Object> map2 = new HashMap<>();
		map2.put("disId", disId);
		map2.put("supplierId", supplierId);
		map2.put("month", getLastTwoMonth());
		map2.put("year", formatWhatYear(0));
		System.out.println("【第三个月】查询参数: " + map2);
		List<NxDistributerPurchaseBatchEntity> batchEntities2 = nxDPBService.queryDisPurchaseBatch(map2);
		System.out.println("【第三个月】批次数量: " + batchEntities2.size());

		map2.put("equalStatus", 2);
		map2.put("notEqualPurchaseType", 9);
		Double unPayOrderDoubleTwo = 0.0; // 未结账订单
		Double unPayReturnTwo = 0.0; // 未记账退货
		Double havePayOrderDoubleTwo = 0.0; // 已结账订单
		Double havePayReturnTwo = 0.0; // 已结账退货
		//未结账订单
		System.out.println("【第三个月-未结账订单】查询参数: " + map2);
		Integer unPayCountTwo = nxDPBService.queryDisPurchaseBatchCount(map2);
		System.out.println("【第三个月-未结账订单】数量: " + unPayCountTwo);
		if (unPayCountTwo > 0) {
			unPayOrderDoubleTwo = nxDPBService.querySupplierUnSettleSubtotal(map2);
			System.out.println("【第三个月-未结账订单】金额: " + unPayOrderDoubleTwo);
		}
		// //未结账退货
		map2.put("notEqualPurchaseType", null);
		map2.put("purchaseType", 9);
		System.out.println("【第三个月-未结账退货】查询参数: " + map2);
		Integer unPayTuihuoCountTwo = nxDPBService.queryDisPurchaseBatchCount(map2);
		System.out.println("【第三个月-未结账退货】数量: " + unPayTuihuoCountTwo);
		if (unPayTuihuoCountTwo > 0) {
			unPayReturnTwo = nxDPBService.querySupplierUnSettleSubtotal(map2);
			System.out.println("【第三个月-未结账退货】金额: " + unPayReturnTwo);
		}
		//已结账订单
		map2.put("equalStatus", 3);
		map2.put("notEqualPurchaseType", 9);
		map2.put("purchaseType", null);
		System.out.println("【第三个月-已结账订单】查询参数: " + map2);
		Integer havePayCountTwo = nxDPBService.queryDisPurchaseBatchCount(map2);
		System.out.println("【第三个月-已结账订单】数量: " + havePayCountTwo);
		if (havePayCountTwo > 0) {
			havePayOrderDoubleTwo = nxDPBService.querySupplierUnSettleSubtotal(map2);
			System.out.println("【第三个月-已结账订单】金额: " + havePayOrderDoubleTwo);
		}

		// 已结账退货
		map2.put("notEqualPurchaseType", null);
		map2.put("purchaseType", 9);
		System.out.println("【第三个月-已结账退货】查询参数: " + map2);
		Integer havePayTuihuoCountTwo = nxDPBService.queryDisPurchaseBatchCount(map2);
		System.out.println("【第三个月-已结账退货】数量: " + havePayTuihuoCountTwo);
		if (havePayTuihuoCountTwo > 0) {
			havePayReturnTwo = nxDPBService.querySupplierUnSettleSubtotal(map2);
			System.out.println("【第三个月-已结账退货】金额: " + havePayReturnTwo);
		}


		//计算结果:
		//订单数量
		int billCountTwo = unPayCountTwo + havePayCountTwo;
		//订单金额
		double billTotalTwo = unPayOrderDoubleTwo + havePayOrderDoubleTwo;

		//已结订单
		int havePayCountTotalTwo = havePayCountTwo + havePayTuihuoCountTwo;

		//已结金额
		double havePayTotlTwo = havePayOrderDoubleTwo - havePayReturnTwo;

		//实际未接金额
		double actPayTotalTwo = unPayOrderDoubleTwo - unPayReturnTwo;
		//实际订单数量
		int actPayCountTotalTwo = unPayCountTwo + unPayTuihuoCountTwo;
		
		System.out.println("【第三个月-计算结果】订单数量: " + billCountTwo + ", 订单金额: " + billTotalTwo);
		System.out.println("【第三个月-计算结果】已结订单: " + havePayCountTotalTwo + ", 已结金额: " + havePayTotlTwo);
		System.out.println("【第三个月-计算结果】实际未结数量: " + actPayCountTotalTwo + ", 实际未结金额: " + actPayTotalTwo);

		Map<String, Object> mapDataThree = new HashMap<>();
		mapDataThree.put("billCount", billCountTwo);
		mapDataThree.put("billTotal", String.format("%.1f", billTotalTwo));

		mapDataThree.put("unPayCount", unPayCountTwo);
		mapDataThree.put("unPayTotal", String.format("%.1f", unPayOrderDoubleTwo));

		mapDataThree.put("havePayCount", havePayCountTotalTwo);
		mapDataThree.put("havePayTotal", String.format("%.1f", havePayTotlTwo));

		mapDataThree.put("returnBillCount", unPayTuihuoCountTwo);
		mapDataThree.put("returnPayTotal", String.format("%.1f", unPayReturnTwo));

		mapDataThree.put("actPayTotal", String.format("%.1f", actPayTotalTwo));
		mapDataThree.put("actBillCount", actPayCountTotalTwo);


		Map<String, Object> map3 = new HashMap<>();
		map3.put("arr", batchEntities);
		map3.put("month", formatWhatMonth(0));
		map3.put("itemData", mapDataOne);
		Map<String, Object> map4 = new HashMap<>();
		map4.put("arr", batchEntities1);
		map4.put("itemData", mapDataTwo);
		map4.put("month", getLastMonth());
		Map<String, Object> map5 = new HashMap<>();
		map5.put("arr", batchEntities2);
		map5.put("itemData", mapDataThree);
		map5.put("month", getLastTwoMonth());

		List<Map<String, Object>> resultData = new ArrayList<>();
		resultData.add(map3);
		resultData.add(map4);
		resultData.add(map5);


		Map<String, Object> mapR = new HashMap<>();
		mapR.put("arr", resultData);
		
		System.out.println("\n======== 查询配送商和供应商信息 ========");
		NxDistributerEntity disInfo = nxDistributerService.queryObject(disId);
		System.out.println("配送商信息: " + (disInfo != null ? disInfo.getNxDistributerName() : "null"));
		mapR.put("disInfo", disInfo);

		NxJrdhSupplierEntity supplierEntity = nxJrdhSupplierService.queryObject(supplierId);
		System.out.println("供应商信息: " + (supplierEntity != null ? supplierEntity.getNxJrdhsSupplierName() : "null"));
		mapR.put("supplierInfo", supplierEntity);

		double unTotal =	unPayOrderDoubleOne + unPayOrderDoubleTwo +  unPayOrderDouble;
			mapR.put("unPayTotal", String.format("%.1f",unTotal));

			System.out.println("======== sellerDistributerPurchaseBatchs 完成 ========\n");
		return R.ok().put("data", mapR);

		} catch (Exception e) {
			System.err.println("❌ sellerDistributerPurchaseBatchs 发生异常:");
			System.err.println("异常类型: " + e.getClass().getName());
			System.err.println("异常消息: " + e.getMessage());
			e.printStackTrace();
			return R.error("查询供应商账单失败: " + e.getMessage());
		}

	}


	@RequestMapping(value = "/supplierGetPurchaseBatchs", method = RequestMethod.POST)
	@ResponseBody
	public R supplierGetPurchaseBatchs (Integer disId, Integer supplierId) {

		System.out.println("zahuishshiss" + supplierId);

		Double resultUnPay = 0.0;
		Double resultPay = 0.0;
		Double resultUnPay1 = 0.0;
		Double resultPay1 = 0.0;
		Double resultUnPay2 = 0.0;
		Double resultPay2 = 0.0;
		//第一个月
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("supplierId", supplierId);
		map.put("month",formatWhatMonth(0) );
		map.put("year",formatWhatYear(0) );
		List<NxDistributerPurchaseBatchEntity> batchEntities = nxDPBService.queryDisPurchaseBatch(map);


		map.put("dayuStatus", 1);
		map.put("status", getNxDisPurchaseBatchDisUserFinishPay());
		System.out.println("zahauishsi" + map);
		int i = nxDPBService.queryDisPurchaseBatchCount(map);
		if(i > 0){
			resultUnPay =  nxDPBService.queryDisPurchaseBatchTotal(map);
		}

		Map<String, Object> mapPay = new HashMap<>();
		mapPay.put("disId", disId);
		mapPay.put("supplierId", supplierId);
		mapPay.put("month",formatWhatMonth(0) );
		mapPay.put("year",formatWhatYear(0) );
		mapPay.put("equalStatus", getNxDisPurchaseBatchDisUserFinishPay());

		System.out.println("eelelelelelelellelelel");
		int iUnpay = nxDPBService.queryDisPurchaseBatchCount(mapPay);
		if(iUnpay > 0){
			resultPay =  nxDPBService.queryDisPurchaseBatchTotal(mapPay);
		}

		//第二个月
		Map<String, Object> map1 = new HashMap<>();
		map1.put("disId", disId);
		map1.put("supplierId", supplierId);
		map1.put("month",getLastMonth());
		map1.put("year",formatWhatYear(0) );

		List<NxDistributerPurchaseBatchEntity> batchEntities1 = nxDPBService.queryDisPurchaseBatch(map1);
		map1.put("dayuStatus", 1);
		map1.put("status", getNxDisPurchaseBatchDisUserFinishPay());
		int i1 = nxDPBService.queryDisPurchaseBatchCount(map1);
		if(i1 > 0){
			resultUnPay1 =  nxDPBService.queryDisPurchaseBatchTotal(map1);
		}

		Map<String, Object> mapPay1 = new HashMap<>();
		mapPay1.put("disId", disId);
		mapPay1.put("supplierId", supplierId);
		mapPay1.put("month",formatWhatMonth(0) );
		mapPay1.put("year",formatWhatYear(0) );
		mapPay1.put("equalStatus", getNxDisPurchaseBatchDisUserFinishPay());
		int iUnpay1 = nxDPBService.queryDisPurchaseBatchCount(mapPay1);
		if(iUnpay1 > 0){
			resultPay1 =  nxDPBService.queryDisPurchaseBatchTotal(mapPay1);
		}
		//第三个月
		Map<String, Object> map2 = new HashMap<>();
		map2.put("disId", disId);
		map2.put("supplierId", supplierId);
		map2.put("month",getLastTwoMonth());
		map2.put("year",formatWhatYear(0) );
		List<NxDistributerPurchaseBatchEntity> batchEntities2 = nxDPBService.queryDisPurchaseBatch(map2);
		map2.put("dayuStatus", 1);
		map2.put("status", getNxDisPurchaseBatchDisUserFinishPay());
		int i2 = nxDPBService.queryDisPurchaseBatchCount(map2);
		if(i2 > 0){
			resultUnPay2 =  nxDPBService.queryDisPurchaseBatchTotal(map2);
		}

		Map<String, Object> mapPay2 = new HashMap<>();
		mapPay2.put("disId", disId);
		mapPay2.put("supplierId", supplierId);
		mapPay2.put("month",formatWhatMonth(0) );
		mapPay2.put("year",formatWhatYear(0) );
		mapPay2.put("equalStatus", getNxDisPurchaseBatchDisUserFinishPay());
		int iUnpay2 = nxDPBService.queryDisPurchaseBatchCount(mapPay2);
		if(iUnpay2 > 0){
			resultPay2 =  nxDPBService.queryDisPurchaseBatchTotal(mapPay2);
		}

		Map<String, Object> map3 = new HashMap<>();
		map3.put("arr", batchEntities);
		map3.put("month",formatWhatMonth(0));
		map3.put("payTotal",String.format("%.1f", resultPay));
		map3.put("unPayTotal",String.format("%.1f", resultUnPay));
		Map<String, Object> map4 = new HashMap<>();
		map4.put("arr", batchEntities1);
		map4.put("month",getLastMonth());
		map4.put("payTotal", String.format("%.1f", resultPay1));
		map4.put("unPayTotal", String.format("%.1f", resultUnPay1));
		Map<String, Object> map5 = new HashMap<>();
		map5.put("arr", batchEntities2);
		map5.put("month",getLastTwoMonth());
		map5.put("payTotal", String.format("%.1f", resultPay2));
		map5.put("unPayTotal", String.format("%.1f", resultUnPay2));

		List<Map<String ,Object>> resultData = new ArrayList<>();
		resultData.add(map3);
		resultData.add(map4);
		resultData.add(map5);

		return R.ok().put("data", resultData);

	}


	@RequestMapping(value = "/disFinishPurchaseBatch")
	@ResponseBody
	public R disFinishPurchaseBatch(@RequestBody NxDistributerPurchaseBatchEntity batchEntity) {
		Integer nxDistributerPurchaseBatchId = batchEntity.getNxDistributerPurchaseBatchId();
		NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryObject(nxDistributerPurchaseBatchId);
		if(nxDistributerPurchaseBatchEntity.getNxDpbStatus().equals(getNxDisPurchaseBatchSellerReply())){
			List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = batchEntity.getNxDPGEntities();
			for (NxDistributerPurchaseGoodsEntity purGoods : purchaseGoodsEntities) {
				Integer nxDistributerPurchaseGoodsId = purGoods.getNxDistributerPurchaseGoodsId();
				NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = dpgService.queryObject(nxDistributerPurchaseGoodsId);

				purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
				purchaseGoodsEntity.setNxDpgPayType(batchEntity.getNxDpbPayType());
				purchaseGoodsEntity.setNxDpgSellUserId(batchEntity.getNxDpbSellUserId());
				dpgService.update(purchaseGoodsEntity);

				System.out.println("purtuos" + purchaseGoodsEntity.getNxDpgDisGoodsId());

				if(batchEntity.getNxDpbOrderIsNotice() != null &&  batchEntity.getNxDpbOrderIsNotice() == 1){
					Integer nxDpgDisGoodsId = purchaseGoodsEntity.getNxDpgDisGoodsId();

					NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(nxDpgDisGoodsId);
					distributerGoodsEntity.setNxDgSupplierId(nxDistributerPurchaseBatchEntity.getNxDpbSupplierId());
					System.out.println("purtuossetNxDgSupplierId" + nxDistributerPurchaseBatchEntity.getNxDpbSupplierId());

					distributerGoodsEntity.setNxDgPurchaseAuto(1);
					dgService.update(distributerGoodsEntity);
				}
				//
				updateDisGoodsPriceThree(purchaseGoodsEntity);
			}

			nxDistributerPurchaseBatchEntity.setNxDpbOrderIsNotice(batchEntity.getNxDpbOrderIsNotice());
			nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
			nxDPBService.update(nxDistributerPurchaseBatchEntity);
			return R.ok();
		}else{
			return R.error(-1,"请刷新数据");
		}

	}

	@RequestMapping(value = "/disFinishPurchaseBatchPush")
	@ResponseBody
	public R disFinishPurchaseBatchPush(@RequestBody NxDistributerPurchaseBatchEntity batchEntity) {
		Integer nxDistributerPurchaseBatchId = batchEntity.getNxDistributerPurchaseBatchId();
		NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryObject(nxDistributerPurchaseBatchId);
//		if(nxDistributerPurchaseBatchEntity.getNxDpbStatus().equals(getNxDisPurchaseBatchSellerReply())){
			List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = batchEntity.getNxDPGEntities();
			for (NxDistributerPurchaseGoodsEntity purGoods : purchaseGoodsEntities) {

				Integer nxDistributerPurchaseGoodsId = purGoods.getNxDistributerPurchaseGoodsId();
				NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = dpgService.queryObject(nxDistributerPurchaseGoodsId);


				purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
				purchaseGoodsEntity.setNxDpgPayType(batchEntity.getNxDpbPayType());
				purchaseGoodsEntity.setNxDpgSellUserId(batchEntity.getNxDpbSellUserId());
				dpgService.update(purchaseGoodsEntity);
				updateDisGoodsPriceThree(purchaseGoodsEntity);
			}

			batchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
			batchEntity.setNxDpbOrderIsNotice(0);
			nxDPBService.update(batchEntity);
			return R.ok();
//		}else{
//			return R.error(-1,"请刷新数据");
//		}

	}

	@RequestMapping(value = "/disFinishPurchaseBatchAdmin")
	@ResponseBody
	public R disFinishPurchaseBatchAdmin(@RequestBody NxDistributerPurchaseBatchEntity batchEntity) {
		Integer nxDistributerPurchaseBatchId = batchEntity.getNxDistributerPurchaseBatchId();
		NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryObject(nxDistributerPurchaseBatchId);
		if(nxDistributerPurchaseBatchEntity.getNxDpbStatus().equals(getNxDisPurchaseBatchSellerReply())){
			List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = batchEntity.getNxDPGEntities();
			for (NxDistributerPurchaseGoodsEntity purGoods : purchaseGoodsEntities) {
				Integer nxDistributerPurchaseGoodsId = purGoods.getNxDistributerPurchaseGoodsId();
				NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = dpgService.queryObject(nxDistributerPurchaseGoodsId);

				purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
				purchaseGoodsEntity.setNxDpgPayType(batchEntity.getNxDpbPayType());
				purchaseGoodsEntity.setNxDpgSellUserId(batchEntity.getNxDpbSellUserId());
				dpgService.update(purchaseGoodsEntity);
				updateDisGoodsPriceThree(purchaseGoodsEntity);

				NxDistributerGoodsShelfStockEntity stockEntity = new NxDistributerGoodsShelfStockEntity();
				stockEntity.setNxDgssReceiveUserId(batchEntity.getNxDpbPurUserId());
				stockEntity.setNxDgssNxPurGoodsId(purGoods.getNxDistributerPurchaseGoodsId());
				stockEntity.setNxDgssNxDistributerId(purGoods.getNxDpgDistributerId());
				stockEntity.setNxDgssNxDisGoodsId(purGoods.getNxDpgDisGoodsId());
				stockEntity.setNxDgssPrice(purGoods.getNxDpgBuyPrice());
				stockEntity.setNxDgssWeight(purGoods.getNxDpgBuyQuantity());
				stockEntity.setNxDgssSubtotal(purGoods.getNxDpgBuySubtotal());
				stockEntity.setNxDgssRestWeight(purGoods.getNxDpgBuyQuantity());
				stockEntity.setNxDgssRestSubtotal(purGoods.getNxDpgBuySubtotal());
				stockEntity.setNxDgssStatus(0);
				stockEntity.setNxDgssInventoryDate(formatWhatDay(0));
				shelfStockService.save(stockEntity);


			}

			batchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
			batchEntity.setNxDpbOrderIsNotice(0);
			nxDPBService.update(batchEntity);
			return R.ok();
		}else{
			return R.error(-1,"请刷新数据");
		}

	}

	@RequestMapping(value = "/purchaserEditBatch/{batchId}")
	@ResponseBody
	public R purchaserEditBatch(@PathVariable Integer batchId) {
		NxDistributerPurchaseBatchEntity nxDisPurBatchEntity = nxDPBService.queryObject(batchId);
		if (nxDisPurBatchEntity.getNxDpbStatus() == 2) {
			//purGoods
			List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = dpgService.queryPurchaseGoodsByBatchId(batchId);
			if(purchaseGoodsEntities.size() > 0){
				for (NxDistributerPurchaseGoodsEntity purGoods : purchaseGoodsEntities) {
					purGoods.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
					dpgService.update(purGoods);
				}
			}

			//purOrder
			nxDisPurBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchSellerReply());
			nxDPBService.update(nxDisPurBatchEntity);
		}
		return R.ok();
	}


	@RequestMapping(value = "/supplierEditBatch/{batchId}")
	@ResponseBody
	public R supplierEditBatch(@PathVariable Integer batchId) {
		NxDistributerPurchaseBatchEntity nxDisPurBatchEntity = nxDPBService.queryObject(batchId);
		if (nxDisPurBatchEntity.getNxDpbStatus() == 1) {
			nxDisPurBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchHaveRead());
			nxDPBService.update(nxDisPurBatchEntity);
		}
		List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = dpgService.queryPurchaseGoodsByBatchId(nxDisPurBatchEntity.getNxDistributerPurchaseBatchId());
		if(purchaseGoodsEntities.size() > 0){
			for (NxDistributerPurchaseGoodsEntity purGoods : purchaseGoodsEntities) {
				purGoods.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
				purGoods.setNxDpgBuySubtotal("0");
				purGoods.setNxDpgBuyPrice(null);
				dpgService.update(purGoods);
			}
		}

		NxDistributerPurchaseBatchEntity entity = nxDPBService.queryBatchWithOrders(batchId);
		return R.ok().put("data", entity);
	}

	/**
	 *
	 * @param batchEntity
	 * @return
	 */
	@RequestMapping(value = "/sellerReplayPurchaseBatch")
	@ResponseBody
	public R sellerReplayPurchaseBatch(@RequestBody NxDistributerPurchaseBatchEntity batchEntity) {
		List<NxDistributerPurchaseGoodsEntity> nxDPBEntities = batchEntity.getNxDPGEntities();
		for (NxDistributerPurchaseGoodsEntity purGoods : nxDPBEntities) {
			Integer nxDistributerPurchaseGoodsId = purGoods.getNxDistributerPurchaseGoodsId();
			NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = dpgService.queryObject(nxDistributerPurchaseGoodsId);

			purchaseGoodsEntity.setNxDpgSellUserId(batchEntity.getNxDpbSellUserId());
			purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsIsPurchase());
			purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
			purchaseGoodsEntity.setNxDpgBuySubtotal(purGoods.getNxDpgBuySubtotal());
			purchaseGoodsEntity.setNxDpgBuyPrice(purGoods.getNxDpgBuyPrice());
			purchaseGoodsEntity.setNxDpgBuyQuantity(purGoods.getNxDpgBuyQuantity());
			dpgService.update(purchaseGoodsEntity);


			List<NxDepartmentOrdersEntity> ordersEntities = purGoods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
			System.out.println("ordododoododdooddo" + ordersEntities.size());

			if(ordersEntities.size() > 0){
				for(NxDepartmentOrdersEntity ordersEntity: ordersEntities){

					Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
					NxDepartmentOrdersEntity oldOrderEntity = nxDepartmentOrdersService.queryObject(nxDepartmentOrdersId);
					oldOrderEntity.setNxDoQuantity(ordersEntity.getNxDoQuantity());
					oldOrderEntity.setNxDoCostPrice(ordersEntity.getNxDoCostPrice());
					oldOrderEntity.setNxDoCostSubtotal(ordersEntity.getNxDoCostSubtotal());
					oldOrderEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishPurchase());
					System.out.println("ordododoododdooddo" + oldOrderEntity.getNxDoCostSubtotal());
					nxDepartmentOrdersService.update(oldOrderEntity);
					if(ordersEntity.getNxDoGbDepartmentOrderId() != null){
						GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(ordersEntity.getNxDoGbDepartmentOrderId());
						gbDepartmentOrdersEntity.setGbDoBuyStatus(getNxDepOrderBuyStatusFinishPurchase());
						gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
					}
				}
			}
		}

		NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDistributerPurchaseBatchService.queryObject(batchEntity.getNxDistributerPurchaseBatchId());

		Map<String, Object> map = new HashMap<>();
		map.put("batchId", batchEntity.getNxDistributerPurchaseBatchId());
		Double subTotal = dpgService.queryPurchaseGoodsSubTotal(map);
		nxDistributerPurchaseBatchEntity.setNxDpbSellSubtotal(new BigDecimal(subTotal).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
		nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchSellerReply());
		nxDPBService.update(nxDistributerPurchaseBatchEntity);

		Map<String, TemplateData> mapNotice = new HashMap<>();
		mapNotice.put("character_string1", new TemplateData(batchEntity.getNxDistributerPurchaseBatchId().toString()));
		mapNotice.put("amount2", new TemplateData(batchEntity.getNxDpbSellSubtotal()));
		mapNotice.put("date3", new TemplateData( batchEntity.getNxDpbDate()));
		mapNotice.put("thing4", new TemplateData( "请收货"));
		mapNotice.put("phrase5", new TemplateData( "待确认"));
		System.out.println("nociiciiiicicautotootototoototoRRRRR" + mapNotice);
		Map<String, Object> mapU = new HashMap<>();
		mapU.put("disId", batchEntity.getNxDpbDistributerId());
		mapU.put("admin", 0);
		List<NxDistributerUserEntity> userEntities = nxDistributerUserService.queryRoleNxDisRoleUserList(mapU);
		if(userEntities.size() > 0){
			for(NxDistributerUserEntity userEntity: userEntities){
				System.out.println("diusern" + userEntity.getNxDiuWxNickName());
				WeNoticeService.jjSupplierBatchReceive(userEntity.getNxDiuWxOpenId(), "pages/prepare/index/index", mapNotice);
			}
		}

		return R.ok();
	}





	@RequestMapping(value = "/sellUserReadDisBatch", method = RequestMethod.POST)
	@ResponseBody
	public R sellUserReadDisBatch (@RequestBody NxDistributerPurchaseBatchEntity batch) {

		Integer nxDpbSellUserId = batch.getNxDpbSellUserId();
		Map<String, Object> map = new HashMap<>();
		map.put("nxDisId", batch.getNxDpbDistributerId());
		map.put("userId", nxDpbSellUserId);
		NxJrdhSupplierEntity supplierEntity = nxJrdhSupplierService.querySellUserSupplier(map);

		Integer nxDistributerPurchaseBatchId = batch.getNxDistributerPurchaseBatchId();

		NxDistributerPurchaseBatchEntity batchEntity = nxDistributerPurchaseBatchService.queryObject(nxDistributerPurchaseBatchId);
		if(supplierEntity != null){
			batchEntity.setNxDpbSupplierId(supplierEntity.getNxJrdhSupplierId());
			batchEntity.setNxDpbPayType(1);
		}

		batchEntity.setNxDpbStatus(getNxDisPurchaseBatchHaveRead());
		batchEntity.setNxDpbSellUserId(batch.getNxDpbSellUserId());
		batchEntity.setNxDpbBuyUserId(batch.getNxDpbBuyUserId());
		batchEntity.setNxDpbSellUserOpenId(batch.getNxDpbSellUserOpenId());


		nxDPBService.update(batchEntity);
		NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryBatchWithOrders(nxDistributerPurchaseBatchId);

return R.ok().put("data", nxDistributerPurchaseBatchEntity);
	}

	@RequestMapping(value = "/updateBatchBuyerId", method = RequestMethod.POST)
	@ResponseBody
	public R updateBatchBuyerId (Integer buyerId, Integer batchId, String openId) {
		NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryObject(batchId);
		nxDistributerPurchaseBatchEntity.setNxDpbBuyUserId(buyerId);
		nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchUnRead());
		nxDistributerPurchaseBatchEntity.setNxDpbBuyUserOpenId(openId);
		nxDPBService.update(nxDistributerPurchaseBatchEntity);
		return R.ok();
	}


	@RequestMapping(value = "/getDisPurchaseGoodsBatch/{batchId}")
	@ResponseBody
	public R getDisPurchaseGoodsBatch(@PathVariable Integer batchId) {

		System.out.println("baiici" + batchId);
		NxDistributerPurchaseBatchEntity entity = nxDPBService.queryBatchWithOrders(batchId);

		System.out.println(entity);
		if(entity != null){
			return R.ok().put("data", entity);
		}else{
			return R.error(-1, "没有订单");
		}
	}


	@RequestMapping(value = "/purchaseDeletePurBatchItem/{id}") 
	@ResponseBody
	public R purchaseDeletePurBatchItem(@PathVariable Integer id) {
		System.out.println("======== 删除采购批次商品开始 ========");
		System.out.println("采购商品ID: " + id);
		
		NxDistributerPurchaseGoodsEntity purGoods = dpgService.queryObject(id);
		System.out.println("采购商品状态: " + purGoods.getNxDpgStatus());
		System.out.println("批次ID: " + purGoods.getNxDpgBatchId());
		System.out.println("商品ID: " + purGoods.getNxDpgDisGoodsId());
		
		if(purGoods.getNxDpgStatus().equals(getNxDisPurchaseGoodsWithBatch())) {
			Integer nxDpgBatchId = purGoods.getNxDpgBatchId();
			System.out.println("--- 检查批次状态 ---");
			System.out.println("批次ID: " + nxDpgBatchId);
			
			Map<String, Object> map1 = new HashMap<>();
			map1.put("batchId", nxDpgBatchId);
			List<NxDistributerPurchaseGoodsEntity> goodsEntities = dpgService.queryPurchaseGoodsByParams(map1);
			System.out.println("批次中商品总数: " + goodsEntities.size());
			
			if (goodsEntities.size() == 1) {
				System.out.println("批次中只有1个商品，删除整个批次");
				nxDPBService.delete(nxDpgBatchId);
				System.out.println("批次已删除");
			}else{
				System.out.println("批次中有多个商品，检查是否需要更新批次状态");
				
				Map<String, Object> map = new HashMap<>();
				map.put("batchId", nxDpgBatchId);
				int count = dpgService.queryPurchaseGoodsCount(map);
				System.out.println("批次中采购商品总数: " + count);
				
				map.put("equalStatus", getNxDisPurchaseGoodsWithBatch());
				int countAll = dpgService.queryPurchaseGoodsCount(map);
				System.out.println("批次中状态=WithBatch的商品数: " + countAll);
				
				if(count - countAll == 1){
					System.out.println("所有商品都是WithBatch状态，更新批次为已完成");
					NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDistributerPurchaseBatchService.queryObject(nxDpgBatchId);
					System.out.println("批次当前状态: " + nxDistributerPurchaseBatchEntity.getNxDpbStatus());
					nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
					nxDistributerPurchaseBatchService.update(nxDistributerPurchaseBatchEntity);
					System.out.println("批次状态已更新为: " + getNxDisPurchaseBatchDisUserFinish());
				} else {
					System.out.println("批次中还有其他状态的商品，不更新批次状态");
				}
			}
			
			System.out.println("--- 重置采购商品信息 ---");
			purGoods.setNxDpgBuyPrice(null);
			purGoods.setNxDpgBuySubtotal(null);
			purGoods.setNxDpgBuyQuantity(null);
			purGoods.setNxDpgBatchId(null);
			purGoods.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
			purGoods.setNxDpgPurchaseDate(null);
			purGoods.setNxDpgTime(null);
			purGoods.setNxDpgBuyUserId(null);
			purGoods.setNxDpgPurUserId(null);
			purGoods.setNxDpgPurchaseType(getNxPurchaseGoodsTypeForOrder());
			dpgService.update(purGoods);
			System.out.println("采购商品已更新");
			
		} else {
			System.out.println("⚠️ 采购商品状态不是WithBatch，无法删除");
		}
		
		System.out.println("======== 删除采购批次商品完成 ========");
		return R.ok();
	}

	@RequestMapping(value = "/deleteDisPurBatchItem/{id}")
	@ResponseBody
	public R deleteDisPurBatchItem(@PathVariable Integer id) {
		NxDistributerPurchaseGoodsEntity purGoods = dpgService.queryObject(id);
		System.out.println("statssss" + purGoods.getNxDpgStatus());
		if(purGoods.getNxDpgStatus().equals(getNxDisPurchaseGoodsWithBatch())){
			Integer nxDpgBatchId = purGoods.getNxDpgBatchId();
			Map<String, Object> map1 = new HashMap<>();
			map1.put("batchId", nxDpgBatchId);
			List<NxDistributerPurchaseGoodsEntity> goodsEntities = dpgService.queryPurchaseGoodsByParams(map1);
			if(goodsEntities.size() == 1){
				nxDPBService.delete(nxDpgBatchId);
			}
			purGoods.setNxDpgBuyPrice(null);
			purGoods.setNxDpgBuySubtotal(null);
			purGoods.setNxDpgBuyQuantity(null);
			purGoods.setNxDpgBatchId(null);
			purGoods.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
			purGoods.setNxDpgPurchaseDate(null);
			purGoods.setNxDpgTime(null);
			purGoods.setNxDpgBuyUserId(null);
			purGoods.setNxDpgPurUserId(null);
			System.out.println("updatepurrr" + purGoods.getNxDpgBatchId());
			dpgService.update(purGoods);

			Integer nxDistributerPurchaseGoodsId = purGoods.getNxDistributerPurchaseGoodsId();
			Map<String, Object> map = new HashMap<>();
			map.put("purGoodsId", nxDistributerPurchaseGoodsId);
			System.out.println("orderdmappaappa" + map);
			List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
			for (NxDepartmentOrdersEntity orders : ordersEntities) {
				if(orders.getNxDoPurchaseStatus() < 4 && orders.getNxDoStatus() < 3){
					orders.setNxDoStatus(getNxOrderStatusNew());
					orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsUnBuy());
					System.out.println("oreener" + orders);
					nxDepartmentOrdersService.update(orders);

					if(orders.getNxDoGbDepartmentOrderId() != null){
						//更新gbDepOrder
						Integer nxDepartmentOrdersId = orders.getNxDepartmentOrdersId();
						GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(nxDepartmentOrdersId);
						if (gbDepartmentOrdersEntity != null) {
							gbDepartmentOrdersEntity.setGbDoBuyStatus(0);
							gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
						}
					}
				}



			}
			return R.ok();

		}else{
			return R.error(-1,"请刷新数据");
		}
	}



	@RequestMapping(value = "/deleteDisPurBatchItemAuto/{id}")
	@ResponseBody
	public R deleteDisPurBatchItemAuto(@PathVariable Integer id) {

		NxDistributerPurchaseGoodsEntity purGoods = dpgService.queryObject(id);
		Map<String, Object> map = new HashMap<>();
		map.put("purGoodsId", id);
		List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
		for (NxDepartmentOrdersEntity orders : ordersEntities) {
			orders.setNxDoStatus(getNxOrderStatusNew());
			orders.setNxDoPurchaseStatus(0);
//			orders.setNxDoWeight(null);
//			orders.setNxDoSubtotal(null);
//			orders.setNxDoCostSubtotal(null);
//			orders.setNxDoPurchaseUserId(null);
			orders.setNxDoPurchaseGoodsId(-1);
//			orders.setNxDoGoodsType(-1);
			nxDepartmentOrdersService.update(orders);

			if(orders.getNxDoGbDepartmentOrderId() != null){
				//更新gbDepOrder
				Integer nxDepartmentOrdersId = orders.getNxDepartmentOrdersId();
				GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(nxDepartmentOrdersId);
				if (gbDepartmentOrdersEntity != null) {
					gbDepartmentOrdersEntity.setGbDoBuyStatus(0);
					gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
				}
			}
		}


		if(purGoods.getNxDpgStatus().equals(getNxDisPurchaseGoodsWithBatch())){
			Integer nxDpgBatchId = purGoods.getNxDpgBatchId();
			Map<String, Object> map1 = new HashMap<>();
			map1.put("batchId", nxDpgBatchId);
			List<NxDistributerPurchaseGoodsEntity> goodsEntities = dpgService.queryPurchaseGoodsByParams(map1);
			if(goodsEntities.size() == 1){
				nxDPBService.delete(nxDpgBatchId);
			}
			dpgService.delete(id);
			return R.ok();

		}else{
			return R.error(-1,"请刷新数据");
		}
	}


	@RequestMapping(value = "/deleteDisBatch/{batchId}")
	@ResponseBody
	public R deleteDisBatch(@PathVariable Integer batchId) {

		NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryBatchWithOrders(batchId);
		for (NxDistributerPurchaseGoodsEntity purGoods : nxDistributerPurchaseBatchEntity.getNxDPGEntities()) {

			Integer nxDistributerPurchaseGoodsId = purGoods.getNxDistributerPurchaseGoodsId();
			NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = dpgService.queryObject(nxDistributerPurchaseGoodsId);

			purchaseGoodsEntity.setNxDpgBatchId(null);
			purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
			purchaseGoodsEntity.setNxDpgBuyPrice("0.0");
			purchaseGoodsEntity.setNxDpgBuyQuantity("0.0");
			purchaseGoodsEntity.setNxDpgBuySubtotal("0.0");
			dpgService.update(purchaseGoodsEntity);

			Map<String, Object> map = new HashMap<>();
			map.put("purGoodsId", nxDistributerPurchaseGoodsId);
			List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
			for (NxDepartmentOrdersEntity orders : ordersEntities) {
				orders.setNxDoStatus(getNxOrderStatusNew());
				orders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
				orders.setNxDoWeight(null);
				orders.setNxDoCostSubtotal(null);
				orders.setNxDoPurchaseUserId(null);
				nxDepartmentOrdersService.update(orders);
				System.out.println("oribtiididiid" + orders.getNxDoGbDepartmentOrderId());
				if(orders.getNxDoGbDepartmentOrderId() != null){
					//更新gbDepOrder
					Integer nxDepartmentOrdersId = orders.getNxDepartmentOrdersId();
					GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(nxDepartmentOrdersId);
					if (gbDepartmentOrdersEntity != null) {
						gbDepartmentOrdersEntity.setGbDoBuyStatus(0);
						gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
					}
				}
			}
		}
		nxDPBService.delete(batchId);
	    return R.ok();
	}

	/**
	 * 第一步 保存订货批次
	 * @param batchEntity 批次
	 * @return
	 */
	@RequestMapping(value = "/saveDisOfferPurGoodsBatch", method = RequestMethod.POST)
	@ResponseBody
	public R saveDisOfferPurGoodsBatch(@RequestBody NxDistributerPurchaseBatchEntity batchEntity) {

		batchEntity.setNxDpbDate(formatWhatDay(0));
		batchEntity.setNxDpbYear(formatWhatYear(0));
		batchEntity.setNxDpbMonth(formatWhatMonth(0));
		batchEntity.setNxDpbTime(formatWhatTime(0));

		LocalDateTime now = LocalDateTime.now();
		int hour = now.getHour();
		if( hour < 12){
			batchEntity.setNxDpbNeedDate(formatWhatDay(0));
		}else{
			batchEntity.setNxDpbNeedDate(formatWhatDay(1));
		}
		batchEntity.setNxDpbStatus(getNxDisPurchaseBatchUnSend());
		batchEntity.setNxDpbPruchaseWeek(getWeek(0));
		batchEntity.setNxDpbPayType(0);
		nxDPBService.save(batchEntity);

		for (NxDistributerPurchaseGoodsEntity disPurGoods : batchEntity.getNxDPGEntities()) {

			Integer nxDistributerPurchaseGoodsId = disPurGoods.getNxDistributerPurchaseGoodsId();
			NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = dpgService.queryObject(nxDistributerPurchaseGoodsId);

			purchaseGoodsEntity.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
			purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
			purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
			purchaseGoodsEntity.setNxDpgTime(formatWhatTime(0));
			purchaseGoodsEntity.setNxDpgBuyUserId(batchEntity.getNxDpbBuyUserId());
			purchaseGoodsEntity.setNxDpgPurUserId(batchEntity.getNxDpbPurUserId());
			purchaseGoodsEntity.setNxDpgBuyQuantity("0");
			purchaseGoodsEntity.setNxDpgBuyPrice("0");
			purchaseGoodsEntity.setNxDpgBuySubtotal("0");
			dpgService.update(purchaseGoodsEntity);

			Map<String, Object> map = new HashMap<>();
			map.put("purGoodsId", disPurGoods.getNxDistributerPurchaseGoodsId());
			List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
			if(ordersEntities.size() > 0){
				for(NxDepartmentOrdersEntity ordersEntity: ordersEntities){
					ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
					if(batchEntity.getNxDpbPurchaseType() == 3){
						ordersEntity.setNxDoCostPrice("");
						ordersEntity.setNxDoCostSubtotal("");
					}
					nxDepartmentOrdersService.update(ordersEntity);

					//save Offer Order
					NxDepartmentOrdersEntity offerOrder = new NxDepartmentOrdersEntity();
					offerOrder.setNxDoDistributerId(batchEntity.getNxDpbSupplierId());

//					nxDepartmentOrdersService.saveOrderWithGoods(offerOrder, )
					if(ordersEntity.getNxDoGbDepartmentOrderId() != null){
						//更新gbDepOrder
						Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
						GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(nxDepartmentOrdersId);
						if (gbDepartmentOrdersEntity != null) {
							gbDepartmentOrdersEntity.setGbDoBuyStatus(1);
							gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
						}
					}
				}
			}
		}
		return R.ok().put("data", batchEntity.getNxDistributerPurchaseBatchId());
	}


	/**
	 * 第一步 保存订货批次
	 * @param batchEntity 批次
	 * @return
	 */
	@RequestMapping(value = "/saveDisPurGoodsBatch", method = RequestMethod.POST)
	@ResponseBody
	public R saveDisPurGoodsBatch(@RequestBody NxDistributerPurchaseBatchEntity batchEntity) {

		batchEntity.setNxDpbDate(formatWhatDay(0));
		batchEntity.setNxDpbYear(formatWhatYear(0));
		batchEntity.setNxDpbMonth(formatWhatMonth(0));
		batchEntity.setNxDpbTime(formatWhatTime(0));

		LocalDateTime now = LocalDateTime.now();
		int hour = now.getHour();
		if( hour < 12){
			batchEntity.setNxDpbNeedDate(formatWhatDay(0));
		}else{
			batchEntity.setNxDpbNeedDate(formatWhatDay(1));
		}
		batchEntity.setNxDpbStatus(getNxDisPurchaseBatchUnSend());
		batchEntity.setNxDpbPruchaseWeek(getWeek(0));
		batchEntity.setNxDpbPayType(0);
		nxDPBService.save(batchEntity);

		for (NxDistributerPurchaseGoodsEntity disPurGoods : batchEntity.getNxDPGEntities()) {

			Integer nxDistributerPurchaseGoodsId = disPurGoods.getNxDistributerPurchaseGoodsId();
			NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = dpgService.queryObject(nxDistributerPurchaseGoodsId);

			purchaseGoodsEntity.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
			purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
			purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
			purchaseGoodsEntity.setNxDpgTime(formatWhatTime(0));
			purchaseGoodsEntity.setNxDpgBuyUserId(batchEntity.getNxDpbBuyUserId());
			purchaseGoodsEntity.setNxDpgPurUserId(batchEntity.getNxDpbPurUserId());
			purchaseGoodsEntity.setNxDpgBuyQuantity("0");
			purchaseGoodsEntity.setNxDpgBuyPrice("0");
			purchaseGoodsEntity.setNxDpgBuySubtotal("0");
			dpgService.update(purchaseGoodsEntity);

			Map<String, Object> map = new HashMap<>();
			map.put("purGoodsId", disPurGoods.getNxDistributerPurchaseGoodsId());
			List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
			if(ordersEntities.size() > 0){
				for(NxDepartmentOrdersEntity ordersEntity: ordersEntities){
					ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
					if(batchEntity.getNxDpbPurchaseType() == 3){
						ordersEntity.setNxDoCostPrice("");
						ordersEntity.setNxDoCostSubtotal("");
					}
					nxDepartmentOrdersService.update(ordersEntity);
					if(ordersEntity.getNxDoGbDepartmentOrderId() != null){
						//更新gbDepOrder
						Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
						GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(nxDepartmentOrdersId);
						if (gbDepartmentOrdersEntity != null) {
							gbDepartmentOrdersEntity.setGbDoBuyStatus(1);
							gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
						}
					}
				}
			}
		}
		return R.ok().put("data", batchEntity.getNxDistributerPurchaseBatchId());
	}


	@RequestMapping(value = "/purchaseSaveDisPurGoodsBatch", method = RequestMethod.POST)
	@ResponseBody
	public R purchaseSaveDisPurGoodsBatch(@RequestBody NxDistributerPurchaseBatchEntity batchEntity) {

		batchEntity.setNxDpbDate(formatWhatDay(0));
		batchEntity.setNxDpbYear(formatWhatYear(0));
		batchEntity.setNxDpbMonth(formatWhatMonth(0));
		batchEntity.setNxDpbTime(formatWhatTime(0));
		batchEntity.setNxDpbStatus(getNxDisPurchaseBatchUnSend());
		batchEntity.setNxDpbPruchaseWeek(getWeek(0));
		batchEntity.setNxDpbPayType(0);
		nxDPBService.save(batchEntity);
		for (NxDistributerPurchaseGoodsEntity disPurGoods : batchEntity.getNxDPGEntities()) {
			Integer nxDistributerPurchaseGoodsId = disPurGoods.getNxDistributerPurchaseGoodsId();
			NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = dpgService.queryObject(nxDistributerPurchaseGoodsId);
			purchaseGoodsEntity.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
			purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
			purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
			purchaseGoodsEntity.setNxDpgTime(formatWhatTime(0));
			purchaseGoodsEntity.setNxDpgBuyUserId(batchEntity.getNxDpbBuyUserId());
			purchaseGoodsEntity.setNxDpgPurUserId(batchEntity.getNxDpbPurUserId());
			dpgService.update(purchaseGoodsEntity);

		}
		return R.ok().put("data", batchEntity.getNxDistributerPurchaseBatchId());
	}

	/**
	 * 保存部门采购批次
	 * @param batchEntity 批次（包含部门父级ID）
	 * @return 批次ID
	 */
	@RequestMapping(value = "/saveDisPurGoodsBatchByDep", method = RequestMethod.POST)
	@ResponseBody
	public R saveDisPurGoodsBatchByDep(@RequestBody NxDistributerPurchaseBatchEntity batchEntity) {
		batchEntity.setNxDpbDate(formatWhatDay(0));
		batchEntity.setNxDpbYear(formatWhatYear(0));
		batchEntity.setNxDpbMonth(formatWhatMonth(0));
		batchEntity.setNxDpbTime(formatWhatTime(0));

		LocalDateTime now = LocalDateTime.now();
		int hour = now.getHour();
		if( hour < 12){
			batchEntity.setNxDpbNeedDate(formatWhatDay(0));
		}else{
			batchEntity.setNxDpbNeedDate(formatWhatDay(1));
		}
		batchEntity.setNxDpbStatus(getNxDisPurchaseBatchUnSend());
		batchEntity.setNxDpbPruchaseWeek(getWeek(0));
		batchEntity.setNxDpbPayType(0);
		// batchEntity.setNxDpbPurchaseType(4);
		nxDPBService.save(batchEntity);
		// 获取部门父级ID，用于过滤订单
		Integer depFatherId = batchEntity.getNxDpbNxDepartmentFatherId();

		for (NxDistributerPurchaseGoodsEntity disPurGoods1 : batchEntity.getNxDPGEntities()) {
			Integer nxDistributerPurchaseGoodsId = disPurGoods1.getNxDistributerPurchaseGoodsId();
			NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = dpgService.queryObject(nxDistributerPurchaseGoodsId);

			// 查询该采购商品关联的所有订单（不按部门过滤）
			Map<String, Object> allOrdersMap = new HashMap<>();
			allOrdersMap.put("purGoodsId", purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
			List<NxDepartmentOrdersEntity> allOrdersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(allOrdersMap);
			
			System.out.println("[saveDisPurGoodsBatchByDep] 采购商品ID: " + purchaseGoodsEntity.getNxDistributerPurchaseGoodsId()
				+ ", 当前部门父级ID: " + depFatherId
				+ ", 关联订单总数: " + (allOrdersEntities != null ? allOrdersEntities.size() : 0));
			
			// 检查是否有其他部门的订单
			boolean hasOtherDepOrders = false;
			if (depFatherId != null && allOrdersEntities.size() > 0) {
				for (NxDepartmentOrdersEntity order : allOrdersEntities) {
					Integer orderDepFatherId = order.getNxDoDepartmentFatherId();
					System.out.println("[saveDisPurGoodsBatchByDep] 订单ID: " + order.getNxDepartmentOrdersId() 
						+ ", 订单部门父级ID: " + orderDepFatherId 
						+ ", 是否匹配当前部门: " + depFatherId.equals(orderDepFatherId));
					if (!depFatherId.equals(orderDepFatherId)) {
						hasOtherDepOrders = true;
						System.out.println("[saveDisPurGoodsBatchByDep] 发现其他部门的订单！订单ID: " + order.getNxDepartmentOrdersId());
					}
				}
			}
			
			System.out.println("[saveDisPurGoodsBatchByDep] 是否有其他部门的订单: " + hasOtherDepOrders);
			
			// 如果有其他部门的订单，为当前部门创建新的采购商品
			if (hasOtherDepOrders) {
				// 创建新的采购商品（复制原采购商品的信息）
				NxDistributerPurchaseGoodsEntity newPurGoods = new NxDistributerPurchaseGoodsEntity();
				newPurGoods.setNxDpgDisGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
				newPurGoods.setNxDpgDisGoodsFatherId(purchaseGoodsEntity.getNxDpgDisGoodsFatherId());
				newPurGoods.setNxDpgDisGoodsGrandId(purchaseGoodsEntity.getNxDpgDisGoodsGrandId());
				newPurGoods.setNxDpgDistributerId(purchaseGoodsEntity.getNxDpgDistributerId());
				newPurGoods.setNxDpgStandard(purchaseGoodsEntity.getNxDpgStandard());
				newPurGoods.setNxDpgQuantity(purchaseGoodsEntity.getNxDpgQuantity());
				newPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
				newPurGoods.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
				newPurGoods.setNxDpgPurchaseDate(formatWhatDay(0));
				newPurGoods.setNxDpgTime(formatWhatTime(0));
				newPurGoods.setNxDpgBuyUserId(batchEntity.getNxDpbBuyUserId());
				newPurGoods.setNxDpgPurUserId(batchEntity.getNxDpbPurUserId());
				newPurGoods.setNxDpgPurchaseType(purchaseGoodsEntity.getNxDpgPurchaseType());
				newPurGoods.setNxDpgInputType(purchaseGoodsEntity.getNxDpgInputType());
				newPurGoods.setNxDpgApplyDate(purchaseGoodsEntity.getNxDpgApplyDate());
				newPurGoods.setNxDpgExpectPrice(purchaseGoodsEntity.getNxDpgExpectPrice());
				newPurGoods.setNxDpgBuyPrice(purchaseGoodsEntity.getNxDpgBuyPrice());
				newPurGoods.setNxDpgApplyShelfId(purchaseGoodsEntity.getNxDpgApplyShelfId());
				
				// 计算当前部门的订单数量
				int currentDepOrdersCount = 0;
				for (NxDepartmentOrdersEntity order : allOrdersEntities) {
					if (depFatherId != null && depFatherId.equals(order.getNxDoDepartmentFatherId())) {
						currentDepOrdersCount++;
					}
				}
				newPurGoods.setNxDpgOrdersAmount(currentDepOrdersCount);
				newPurGoods.setNxDpgFinishAmount(0);
				
				// 保存新的采购商品
				dpgService.save(newPurGoods);
				Integer newPurGoodsId = newPurGoods.getNxDistributerPurchaseGoodsId();
				
				// 更新原采购商品的订单数量（减去当前部门的订单数量）
				Integer originalOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
				if (originalOrdersAmount != null && originalOrdersAmount >= currentDepOrdersCount) {
					purchaseGoodsEntity.setNxDpgOrdersAmount(originalOrdersAmount - currentDepOrdersCount);
					dpgService.update(purchaseGoodsEntity);
					System.out.println("[saveDisPurGoodsBatchByDep] 更新原采购商品订单数量: 原数量=" + originalOrdersAmount 
						+ ", 当前部门订单数=" + currentDepOrdersCount 
						+ ", 更新后数量=" + (originalOrdersAmount - currentDepOrdersCount));
				} else {
					System.out.println("[saveDisPurGoodsBatchByDep] 警告：原采购商品订单数量异常，无法更新。原数量=" + originalOrdersAmount 
						+ ", 当前部门订单数=" + currentDepOrdersCount);
				}
				
				// 将当前部门的订单关联到新的采购商品
				Map<String, Object> currentDepOrdersMap = new HashMap<>();
				currentDepOrdersMap.put("purGoodsId", purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
				if (depFatherId != null) {
					currentDepOrdersMap.put("depFatherId", depFatherId);
				}
				List<NxDepartmentOrdersEntity> currentDepOrdersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(currentDepOrdersMap);
				if (currentDepOrdersEntities.size() > 0) {
					for (NxDepartmentOrdersEntity ordersEntity : currentDepOrdersEntities) {
						ordersEntity.setNxDoPurchaseGoodsId(newPurGoodsId);
						ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusIsPurchase());
						nxDepartmentOrdersService.update(ordersEntity);
						if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
							//更新gbDepOrder
							Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
							GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(nxDepartmentOrdersId);
							if (gbDepartmentOrdersEntity != null) {
								gbDepartmentOrdersEntity.setGbDoBuyStatus(1);
								gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
							}
						}
					}
				}
			} else {
				// 如果没有其他部门的订单，直接更新采购商品和订单
				purchaseGoodsEntity.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
				purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
				purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
				purchaseGoodsEntity.setNxDpgTime(formatWhatTime(0));
				purchaseGoodsEntity.setNxDpgBuyUserId(batchEntity.getNxDpbBuyUserId());
				purchaseGoodsEntity.setNxDpgPurUserId(batchEntity.getNxDpbPurUserId());
				dpgService.update(purchaseGoodsEntity);
				
				// 查询和更新属于当前部门的订单
				Map<String, Object> map = new HashMap<>();
				map.put("purGoodsId", purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
				if (depFatherId != null) {
					map.put("depFatherId", depFatherId);
				}
				List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
				if (ordersEntities.size() > 0) {
					for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
						ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusIsPurchase());
						nxDepartmentOrdersService.update(ordersEntity);
						if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
							//更新gbDepOrder
							Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
							GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(nxDepartmentOrdersId);
							if (gbDepartmentOrdersEntity != null) {
								gbDepartmentOrdersEntity.setGbDoBuyStatus(1);
								gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
							}
						}
					}
				}
			}
		}
		return R.ok().put("data", batchEntity.getNxDistributerPurchaseBatchId());
	}


	private void updateDisGoodsPriceThree(NxDistributerPurchaseGoodsEntity purgoods) {
		Integer nxDpgDisGoodsId = purgoods.getNxDpgDisGoodsId();
		NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(nxDpgDisGoodsId);
		String nxDpgBuyPrice = purgoods.getNxDpgBuyPrice();
//		if(nxDistributerGoodsEntity.getNxDgBuyingPriceIsGrade() == 0){
//			nxDistributerGoodsEntity.setNxDgBuyingPrice(nxDpgBuyPrice);
//			nxDistributerGoodsEntity.setNxDgBuyingPriceUpdate(formatWhatDay(0));
//			dgService.update(nxDistributerGoodsEntity);
//		}
		if(nxDistributerGoodsEntity.getNxDgBuyingPriceIsGrade() == 1){
			Integer level = purgoods.getNxDpgCostLevel();
			if(level == 1){
				nxDistributerGoodsEntity.setNxDgBuyingPriceOne(nxDpgBuyPrice);
				nxDistributerGoodsEntity.setNxDgBuyingPriceOneUpdate(formatWhatDayString(0));
				dgService.update(nxDistributerGoodsEntity);
			}
			if(level == 2){
				nxDistributerGoodsEntity.setNxDgBuyingPriceTwo(nxDpgBuyPrice);
				nxDistributerGoodsEntity.setNxDgBuyingPriceTwoUpdate(formatWhatDayString(0));
				dgService.update(nxDistributerGoodsEntity);
			}
			if(level == 3){
				nxDistributerGoodsEntity.setNxDgBuyingPriceThree(nxDpgBuyPrice);
				nxDistributerGoodsEntity.setNxDgBuyingPriceThreeUpdate(formatWhatDayString(0));
				dgService.update(nxDistributerGoodsEntity);
			}

		}



	}


	@RequestMapping(value = "/supplierGetUnWeightOutPurGoods/{batchId}")
	@ResponseBody
	public R supplierGetUnWeightOutPurGoods(@PathVariable Integer batchId) {
		Map<String, Object> map = new HashMap<>();
		map.put("batchId", batchId);
		map.put("status", getGbPurchaseGoodsStatusWeightFinished());
		System.out.println("unweieiieieiieieieaaaa222" + map);
		List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntityList = dpgService.queryPurchaseGoodsWithDetailByParams(map);
		return R.ok().put("data", purchaseGoodsEntityList);
	}



//	@RequestMapping(value = "/saveDisPurBatchBuyUserId", method = RequestMethod.POST)
//	@ResponseBody
//	public R saveDisPurBatchBuyUserId (Integer batchId, Integer buyUserId) {
//		NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryObject(batchId);
//		nxDistributerPurchaseBatchEntity.setNxDpbBuyUserId(buyUserId);
//		nxDPBService.update(nxDistributerPurchaseBatchEntity);
//		return R.ok();
//	}


//	@RequestMapping(value = "/getDisSellerBatches/{sellerId}")
//	@ResponseBody
//	public R getDisSellerBatches(@PathVariable Integer sellerId) {
//
//		Map<String, Object> map = new HashMap<>();
//		map.put("sellerId", sellerId);
//		List<NxDistributerPurchaseBatchEntity> batchEntities = nxDPBService.queryDisPurchaseBatch(map);
//		return R.ok().put("data", batchEntities);
//	}



}
