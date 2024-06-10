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
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import javax.swing.plaf.basic.BasicIconFactory;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbOrderStatusProcurement;
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
	private NxRestrauntOrdersService nxRestrauntOrdersService;
	@Autowired
	private NxJrdhSupplierService nxJrdhSupplierService;
	@Autowired
	private NxDistributerService nxDistributerService;
	@Autowired
	private GbDepartmentOrdersService gbDepartmentOrdersService;


	@RequestMapping(value = "/purDelLastBatch/{batchId}")
	@ResponseBody
	public R purDelLastBatch(@PathVariable Integer batchId) {

		NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryBatchWithOrders(batchId);
		for (NxDistributerPurchaseGoodsEntity purGoods : nxDistributerPurchaseBatchEntity.getNxDPGEntities()) {
			purGoods.setNxDpgStatus(getNxDisPurchaseGoodsFinishPay());
			purGoods.setNxDpgBuyPrice("0.0");
			purGoods.setNxDpgBuyQuantity("0.0");
			purGoods.setNxDpgBuySubtotal("0.0");
			purGoods.setNxDpgPurchaseType(-1);

			dpgService.update(purGoods);

		}

			nxDPBService.delete(batchId);
		return R.ok();
	}



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
		mapZeroData.put("day", formatWhatDay(0) );


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


	@RequestMapping(value = "/disGetPurchasingBatch",method = RequestMethod.POST)
	@ResponseBody
	public R disGetPurchasingBatch(Integer disId, Integer type) {

		Map<String, Object> map2 = new HashMap<>();
		map2.put("disId", disId);
		map2.put("status", 3);
		map2.put("goodsType", type);
		map2.put("equalBuyStatus", 2);
//		map2.put("buyStatus", 4);
		System.out.println("mapapp2o2222" + map2);
		List<NxDistributerPurchaseBatchEntity> batchEntities =  nxDepartmentOrdersService.queryDisPurchaseBatch(map2);

		Map<String, Object> mapB = new HashMap<>();
		mapB.put("disId",disId);
		mapB.put("equalStatus",-1);
		mapB.put("goodsType",type);
		int unReadBatchCount = nxDPBService.queryDisPurchaseBatchCount(mapB);
		mapB.put("equalStatus",0);
		int haveReadBatchCount = nxDPBService.queryDisPurchaseBatchCount(mapB);

		Map<String, Object> map1 = new HashMap<>();
		map1.put("disId", disId);
		map1.put("status", 3);
		map1.put("buyStatus", 3);
		map1.put("purType", -1);
		//新订单
		int newCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

		// 出库
		map1.put("buyStatus", 5);
		map1.put("purType", 0);
		int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);


		// 订货
		map1.put("purType", 1);
		map1.put("inputType", 1);
		System.out.println("wxxxxxxx" + map1);
		int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);
		map1.put("inputType", 2);
		System.out.println("wxxxxxxx" + map1);
		int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);
		// 打印
		map1.put("inputType", 0);
		int printCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

		//ok
		Map<String, Object> mapOk = new HashMap<>();
		mapOk.put("disId", disId);
		mapOk.put("status", 3);
		mapOk.put("purType", 0);
		mapOk.put("weight", 1);
		//出库完成
		int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

		//订货完成
		mapOk.put("purType", 1);
		mapOk.put("inputType", 1);
		mapOk.put("batchId", 1);
		mapOk.put("weight", 1);
		int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
		mapOk.put("inputType", 2);
		int wxCountAutoOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
		//打印完成
		mapOk.put("batchId", -1);
		mapOk.put("inputType", 0);
		mapOk.put("weightStatusEqual", 1);
		int printCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);


//        //////************************************
		// map4 未发送或未打印
		Map<String, Object> map4 = new HashMap<>();

		map4.put("disId", disId);
		map4.put("status", 1);
		map4.put("weightId", -1);
		map4.put("batchId", -1);
		map4.put("equalInputType", 1);
		map1.put("purType", 1);
		map1.put("inputType", 1);
		map1.put("equalStatus", 0);
		Integer unDoCount = nxDepartmentOrdersService.queryDepOrdersAcount(map1);

		// map4 订货已发送
		map4.put("status", 2); //NX_DIS_PURCHASE_GOODS_IS_PURCHASE == 2 huifu
		map4.put("orderStatus", 3);
		map4.put("batchId", 1);
		Integer wxIsBatchCountUnReply = dpgService.queryPurOrderCount(map4);
		map4.put("status", 4);
		map4.put("dayuStatus", 1);
		Integer wxIsBatchCountHaveReply = dpgService.queryPurOrderCount(map4);


		//  map4 已打印
		map4.put("batchId", -1);
		map4.put("weightId", 1);
		map4.put("weightStatusEqual", 0);
		map4.put("orderStatus", 3);
		map4.put("status", 4);
		Integer isPrintCount = dpgService.queryPurOrderCount(map4);
		System.out.println("isprint444444" + map4);
		map4.put("weightStatusEqual", 1);
		Integer isPrintHaveWeightCount = dpgService.queryPurOrderCount(map4);


		Map<String, Object> map111 = new HashMap<>();
		map111.put("arr", batchEntities);
		map111.put("newCount", newCount);
		map111.put("stockCount", stockCount);
		map111.put("stockCountOk", stockCountOK);
		map111.put("wxCount", wxCount);
		map111.put("wxCountAuto", wxCountAuto);
		map111.put("printCount", printCount);
		map111.put("wxCountOk", wxCountOk);
		map111.put("wxCountAutoOk", wxCountAutoOk);
		map111.put("printCountOk", printCountOk);

		map111.put("unDoCount", unDoCount);
		map111.put("isBatchCountUnRepaly", wxIsBatchCountUnReply);
		map111.put("isBatchCountHaveRepaly", wxIsBatchCountHaveReply);
		map111.put("isPrintCount", isPrintCount);
		map111.put("havePrintCount", isPrintHaveWeightCount);
		map111.put("unReadBatchCount", unReadBatchCount);
		map111.put("haveReadBatchCount", haveReadBatchCount);

		Map<String, Object> map222 = new HashMap<>();
		map222.put("disId", disId);
		map222.put("stopDate", formatWhatDay(-1));
		map222.put("equalStatus", -1);
		int count = nxDPBService.queryDisPurchaseBatchCount(map222);
		map111.put("unFinishBatchUnRead", count);
		map222.put("equalStatus", 0);
		int count1 = nxDPBService.queryDisPurchaseBatchCount(map222);
		map111.put("unFinishBatchHaveRead", count1);
		map222.put("equalStatus", 1);
		int count2 = nxDPBService.queryDisPurchaseBatchCount(map222);
		map111.put("unFinishBatchRepalyRead", count2);

		return R.ok().put("data", map111);
	}

	@RequestMapping(value = "/disGetPurchasingBatchReply", method = RequestMethod.POST)
	@ResponseBody
	public R disGetPurchasingBatchReply(Integer disId, Integer type) {

//		Map<String, Object> map113 = new HashMap<>();
//		map113.put("disId", disId);
//		map113.put("equalStatus", 1);
//		map113.put("equalOrderStatus", 3);
//		map113.put("purchaseType", type);
//		System.out.println("eieieiei" + map113);
//		List<NxDistributerPurchaseBatchEntity> batchEntities1 = nxDPBService.queryDisPurchaseBatch(map113);


		Map<String, Object> map2 = new HashMap<>();
		map2.put("disId", disId);
		map2.put("status", 3);
		map2.put("dayuStatus", 0);
		map2.put("orderStatus", 3);
		map2.put("purchaseType", type);
		List<NxDistributerPurchaseBatchEntity> batchEntities = nxDPBService.queryDisPurchaseBatch(map2);

		Map<String, Object> mapB = new HashMap<>();
		mapB.put("disId",disId);
		mapB.put("equalStatus",-1);
		int unReadBatchCount = nxDPBService.queryDisPurchaseBatchCount(mapB);
		mapB.put("equalStatus",0);
		int haveReadBatchCount = nxDPBService.queryDisPurchaseBatchCount(mapB);
		mapB.put("equalStatus",1);
		int replyReadBatchCount = nxDPBService.queryDisPurchaseBatchCount(mapB);

		Map<String, Object> map1 = new HashMap<>();
		map1.put("disId", disId);
		map1.put("status", 3);
		map1.put("buyStatus", 3);
		map1.put("purType", -1);
		//新订单
		int newCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

		// 出库
		map1.put("buyStatus", 5);
		map1.put("purType", 0);
		int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);


		// 订货
		map1.put("purType", 1);
		map1.put("inputType", 1);
		System.out.println("wxxxxxxx" + map1);
		int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);
		map1.put("inputType", 2);
		System.out.println("wxxxxxxx" + map1);
		int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);
		// 打印
		map1.put("inputType", 0);
		int printCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

		//ok
		Map<String, Object> mapOk = new HashMap<>();
		mapOk.put("disId", disId);
		mapOk.put("status", 3);
		mapOk.put("purType", 0);
		mapOk.put("weight", 1);
		//出库完成
		int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

		//订货完成
		mapOk.put("purType", 1);
		mapOk.put("inputType", 1);
		mapOk.put("batchId", 1);
		mapOk.put("weight", 1);
		int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
		mapOk.put("inputType", 2);
		int wxCountAutoOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
		//打印完成
		mapOk.put("batchId", -1);
		mapOk.put("inputType", 0);
		mapOk.put("weightStatusEqual", 1);
		int printCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);


//        //////************************************
		// map4 未发送或未打印
		Map<String, Object> map4 = new HashMap<>();

		map4.put("disId", disId);
		map4.put("status", 1);
		map4.put("weightId", -1);
		map4.put("batchId", -1);
		map4.put("equalInputType", 1);
//		Integer unDoCount = dpgService.queryPurOrderCount(map4);
		map1.put("purType", 1);
		map1.put("inputType", 1);
		map1.put("equalStatus", 0);
		Integer unDoCount = nxDepartmentOrdersService.queryDepOrdersAcount(map1);

		// map4 订货已发送
		map4.put("status", 2); //NX_DIS_PURCHASE_GOODS_IS_PURCHASE == 2 huifu
		map4.put("orderStatus", 3);
		map4.put("batchId", 1);
		Integer wxIsBatchCountUnReply = dpgService.queryPurOrderCount(map4);
		map4.put("status", 4);
		map4.put("dayuStatus", 1);
		Integer wxIsBatchCountHaveReply = dpgService.queryPurOrderCount(map4);


		//  map4 已打印
		map4.put("batchId", -1);
		map4.put("weightId", 1);
		map4.put("weightStatusEqual", 0);
		map4.put("orderStatus", 3);
		map4.put("status", 4);
		Integer isPrintCount = dpgService.queryPurOrderCount(map4);
		System.out.println("isprint444444" + map4);
		map4.put("weightStatusEqual", 1);
		Integer isPrintHaveWeightCount = dpgService.queryPurOrderCount(map4);


		Map<String, Object> map111 = new HashMap<>();
		map111.put("arr", batchEntities);
		map111.put("newCount", newCount);
		map111.put("stockCount", stockCount);
		map111.put("stockCountOk", stockCountOK);
		map111.put("wxCount", wxCount);
		map111.put("wxCountAuto", wxCountAuto);
		map111.put("printCount", printCount);
		map111.put("wxCountOk", wxCountOk);
		map111.put("wxCountAutoOk", wxCountAutoOk);
		map111.put("printCountOk", printCountOk);

		map111.put("unDoCount", unDoCount);
		map111.put("isBatchCountUnRepaly", wxIsBatchCountUnReply);
		map111.put("isBatchCountHaveRepaly", wxIsBatchCountHaveReply);
		map111.put("isPrintCount", isPrintCount);
		map111.put("havePrintCount", isPrintHaveWeightCount);
		map111.put("unReadBatchCount", unReadBatchCount);
		map111.put("haveReadBatchCount", haveReadBatchCount);
		map111.put("replyReadBatchCount", replyReadBatchCount);

		Map<String, Object> map222 = new HashMap<>();
		map222.put("disId", disId);
		map222.put("stopDate", formatWhatDay(-1));
		map222.put("equalStatus", -1);
		int count = nxDPBService.queryDisPurchaseBatchCount(map222);
		map111.put("unFinishBatchUnRead", count);
		map222.put("equalStatus", 0);
		int count1 = nxDPBService.queryDisPurchaseBatchCount(map222);
		map111.put("unFinishBatchHaveRead", count1);
		map222.put("equalStatus", 1);
		int count2 = nxDPBService.queryDisPurchaseBatchCount(map222);
		map111.put("unFinishBatchRepalyRead", count2);

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


	@RequestMapping(value = "/sellerDistributerPurchaseBatchs", method = RequestMethod.POST)
	@ResponseBody
	public R sellerDistributerPurchaseBatchs (Integer disId, Integer sellUserId) {

		Double resultUnPay = 0.0;
		Double resultPay = 0.0;
		Double resultUnPay1 = 0.0;
		Double resultPay1 = 0.0;
		Double resultUnPay2 = 0.0;
		Double resultPay2 = 0.0;
		Double resultPayTotal = 0.0;
		//第一个月
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("sellerId", sellUserId);
		map.put("month",formatWhatMonth(0) );
		map.put("year",formatWhatYear(0) );
		List<NxDistributerPurchaseBatchEntity> batchEntities = nxDPBService.queryDisPurchaseBatch(map);

		map.put("dayuStatus", 1);
		map.put("status", getNxDisPurchaseBatchDisUserFinishPay());
		int i = nxDPBService.queryDisPurchaseBatchCount(map);
		if(i > 0){
			resultUnPay =  nxDPBService.queryDisPurchaseBatchTotal(map);
		}

		Map<String, Object> mapPay = new HashMap<>();
		mapPay.put("disId", disId);
		mapPay.put("sellerId", sellUserId);
		mapPay.put("month",formatWhatMonth(0) );
		mapPay.put("year",formatWhatYear(0) );
		mapPay.put("equalStatus", getNxDisPurchaseBatchDisUserFinishPay());
		int iUnpay = nxDPBService.queryDisPurchaseBatchCount(mapPay);
		if(iUnpay > 0){
			resultPay =  nxDPBService.queryDisPurchaseBatchTotal(mapPay);
		}

		//第二个月
		Map<String, Object> map1 = new HashMap<>();
		map1.put("disId", disId);
		map1.put("sellerId", sellUserId);
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
		mapPay1.put("sellerId", sellUserId);
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
		map2.put("sellerId", sellUserId);
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
		mapPay2.put("sellerId", sellUserId);
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
		map3.put("payTotal",resultPay);
		map3.put("unPayTotal",resultUnPay);
		Map<String, Object> map4 = new HashMap<>();
		map4.put("arr", batchEntities1);
		map4.put("month",getLastMonth());
		map4.put("payTotal", resultPay1);
		map4.put("unPayTotal", resultUnPay1);
		Map<String, Object> map5 = new HashMap<>();
		map5.put("arr", batchEntities2);
		map5.put("month",getLastTwoMonth());
		map5.put("payTotal", resultPay2);
		map5.put("unPayTotal", resultUnPay2);

		List<Map<String ,Object>> resultData = new ArrayList<>();
		resultData.add(map3);
		resultData.add(map4);
		resultData.add(map5);


		Map<String, Object> mapPayTotal = new HashMap<>();
		mapPayTotal.put("disId", disId);
		mapPayTotal.put("sellerId", sellUserId);
		mapPayTotal.put("dayuStatus", 1);
		mapPayTotal.put("status", getNxDisPurchaseBatchDisUserFinishPay());
		int iUnpayTotal = nxDPBService.queryDisPurchaseBatchCount(mapPayTotal);
		System.out.println("totootootcoundnd" + iUnpayTotal);
		if(iUnpayTotal > 0){
			resultPayTotal =  nxDPBService.queryDisPurchaseBatchTotal(mapPayTotal);
		}
		Map<String, Object> mapR = new HashMap<>();
		mapR.put("resultPayTotal", new BigDecimal(resultPayTotal).setScale(1,BigDecimal.ROUND_HALF_UP));
		mapR.put("arr", resultData);
		System.out.println("disidididid" + disId);
		System.out.println("----------------");
		mapR.put("disInfo", nxDistributerService.queryNxDisInfo(disId));

		return R.ok().put("data", mapR);

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
		map3.put("payTotal",resultPay);
		map3.put("unPayTotal",resultUnPay);
		Map<String, Object> map4 = new HashMap<>();
		map4.put("arr", batchEntities1);
		map4.put("month",getLastMonth());
		map4.put("payTotal", resultPay1);
		map4.put("unPayTotal", resultUnPay1);
		Map<String, Object> map5 = new HashMap<>();
		map5.put("arr", batchEntities2);
		map5.put("month",getLastTwoMonth());
		map5.put("payTotal", resultPay2);
		map5.put("unPayTotal", resultUnPay2);

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
				purGoods.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
				purGoods.setNxDpgPayType(batchEntity.getNxDpbPayType());
				purGoods.setNxDpgSellUserId(batchEntity.getNxDpbSellUserId());
				dpgService.update(purGoods);
				updateDisGoodsPriceThree(purGoods);
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
				dpgService.update(purGoods);
			}
		}

		NxDistributerPurchaseBatchEntity entity = nxDPBService.queryBatchWithOrders(batchId);
		return R.ok().put("data", entity);
	}

	/**
	 *
	 * @param batchEntity sellerFinishPurchaseGoodsBatchGb
	 * @return
	 */
	@RequestMapping(value = "/sellerReplayPurchaseBatch")
	@ResponseBody
	public R sellerReplayPurchaseBatch(@RequestBody NxDistributerPurchaseBatchEntity batchEntity) {
		List<NxDistributerPurchaseGoodsEntity> nxDPBEntities = batchEntity.getNxDPGEntities();
		for (NxDistributerPurchaseGoodsEntity purGoods : nxDPBEntities) {
			purGoods.setNxDpgSellUserId(batchEntity.getNxDpbSellUserId());
			purGoods.setNxDpgStatus(getNxDisPurchaseGoodsIsPurchase());
			dpgService.update(purGoods);
			Map<String, Object> map = new HashMap<>();
			map.put("purGoodsId", purGoods.getNxDistributerPurchaseGoodsId());
			List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
			if(ordersEntities.size() > 0){
				for(NxDepartmentOrdersEntity ordersEntity: ordersEntities){
					ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusIsPurchase());
					nxDepartmentOrdersService.update(ordersEntity);
				}
			}
		}

		batchEntity.setNxDpbStatus(getNxDisPurchaseBatchSellerReply());
		nxDPBService.update(batchEntity);
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

		if(supplierEntity != null){
			batch.setNxDpbSupplierId(supplierEntity.getNxJrdhSupplierId());
			batch.setNxDpbPayType(1);
		}
		batch.setNxDpbStatus(getNxDisPurchaseBatchHaveRead());
		nxDPBService.update(batch);

		Integer nxDistributerPurchaseBatchId = batch.getNxDistributerPurchaseBatchId();
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



	@RequestMapping(value = "/deleteDisPurBatchItem/{id}")
	@ResponseBody
	public R deleteDisPurBatchItem(@PathVariable Integer id) {
		NxDistributerPurchaseGoodsEntity purGoods = dpgService.queryObject(id);
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
			dpgService.update(purGoods);

			Integer nxDistributerPurchaseGoodsId = purGoods.getNxDistributerPurchaseGoodsId();
			Map<String, Object> map = new HashMap<>();
			map.put("purGoodsId", nxDistributerPurchaseGoodsId);
			List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
			for (NxDepartmentOrdersEntity orders : ordersEntities) {
				orders.setNxDoStatus(getNxOrderStatusNew());
				orders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
				orders.setNxDoWeight(null);
				orders.setNxDoSubtotal(null);
				orders.setNxDoCostSubtotal(null);
				orders.setNxDoPurchaseUserId(null);
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
			orders.setNxDoWeight(null);
			orders.setNxDoSubtotal(null);
			orders.setNxDoCostSubtotal(null);
			orders.setNxDoPurchaseUserId(null);
			orders.setNxDoPurchaseGoodsId(-1);
			orders.setNxDoGoodsType(-1);
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
			purGoods.setNxDpgBatchId(null);
			purGoods.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
			purGoods.setNxDpgBuyPrice("0.0");
			purGoods.setNxDpgBuyQuantity("0.0");
			purGoods.setNxDpgBuySubtotal("0.0");
			dpgService.update(purGoods);
			Integer nxDistributerPurchaseGoodsId = purGoods.getNxDistributerPurchaseGoodsId();
			Map<String, Object> map = new HashMap<>();
			map.put("purGoodsId", nxDistributerPurchaseGoodsId);
			List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
			for (NxDepartmentOrdersEntity orders : ordersEntities) {
				orders.setNxDoStatus(getNxOrderStatusNew());
				orders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
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
			disPurGoods.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
			disPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
			disPurGoods.setNxDpgPurchaseDate(formatWhatDay(0));
			disPurGoods.setNxDpgTime(formatWhatTime(0));
			disPurGoods.setNxDpgBuyUserId(batchEntity.getNxDpbBuyUserId());
			disPurGoods.setNxDpgPurUserId(batchEntity.getNxDpbPurUserId());
			dpgService.update(disPurGoods);

			Map<String, Object> map = new HashMap<>();
			map.put("purGoodsId", disPurGoods.getNxDistributerPurchaseGoodsId());
			List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
			if(ordersEntities.size() > 0){
				for(NxDepartmentOrdersEntity ordersEntity: ordersEntities){
					ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusIsPurchase());
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


	private void updateDisGoodsPriceThree(NxDistributerPurchaseGoodsEntity purgoods) {
		Integer nxDpgDisGoodsId = purgoods.getNxDpgDisGoodsId();
		NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(nxDpgDisGoodsId);
		String nxDpgBuyPrice = purgoods.getNxDpgBuyPrice();
		if(nxDistributerGoodsEntity.getNxDgBuyingPriceIsGrade() == 0){
			nxDistributerGoodsEntity.setNxDgBuyingPrice(nxDpgBuyPrice);
			nxDistributerGoodsEntity.setNxDgBuyingPriceUpdate(formatWhatDay(0));
			dgService.update(nxDistributerGoodsEntity);
		}
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
