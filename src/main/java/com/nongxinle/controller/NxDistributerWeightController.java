package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 10-21 12:08
 */

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDepOrderBuyStatusIsPurchase;


@RestController
@RequestMapping("api/nxdistributerweight")
public class NxDistributerWeightController {
	@Autowired
	private NxDistributerWeightService nxDistributerWeightService;
	@Autowired
	private NxDepartmentOrdersService nxDepartmentOrdersService;
	@Autowired
	private NxDistributerPurchaseGoodsService nxDisPurchaseGoodsService;
	@Autowired
	private NxRestrauntOrdersService nxRestrauntOrdersService;
	@Autowired
	private GbDepartmentOrdersService gbDepartmentOrdersService;
	@Autowired
	private NxDistributerGoodsService nxDistributerGoodsService;



	@RequestMapping(value = "/disSaveWeightStockGoods", method = RequestMethod.POST)
	@ResponseBody
	public R disSaveWeightStockGoods (@RequestBody NxDistributerWeightEntity weight) {

		weight.setNxDwStatus(0);
		weight.setNxDwItemFinishCount(0);
		weight.setNxDwDate(formatWhatDay(0));

		nxDistributerWeightService.save(weight);
		Integer nxDistributerWeightId = weight.getNxDistributerWeightId();

		int haveWeight = 0;
		String orderIds = weight.getNxDwOrderIds();
		String[] split = orderIds.split(",");
		for (String orderId : split) {
			NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(Integer.valueOf(orderId));
			ordersEntity.setNxDoWeightId(nxDistributerWeightId);
			if(ordersEntity.getNxDoPurchaseStatus() < getNxDepOrderBuyStatusIsPurchase()){
				ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusIsPurchase());
			}
			nxDepartmentOrdersService.update(ordersEntity);
			if(ordersEntity.getNxDoGbDepartmentOrderId() != null){
				//更新gbDepOrder
				Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
				GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(nxDepartmentOrdersId);
				if (gbDepartmentOrdersEntity != null) {
					if(gbDepartmentOrdersEntity.getGbDoBuyStatus() < 1){
						gbDepartmentOrdersEntity.setGbDoBuyStatus(1);
					}
					gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
				}
			}
		}

		nxDistributerWeightService.update(weight);
		return R.ok();
	}





	@RequestMapping(value = "/purchaserGetWeightingList", method = RequestMethod.POST)
	@ResponseBody
	public R purchaserGetWeightingList(Integer disId, Integer status, Integer type) {
		Map<String, Object> map11 = new HashMap<>();
		map11.put("disId", disId);
		map11.put("equalStatus", status);
		map11.put("orderStatus", 3);
		map11.put("type", type);
		if(status == 1){
			map11.put("startDate", formatWhatDay(-2));
			map11.put("stopDate", formatWhatDay(0));
		}
		System.out.println("puuruururuururuur" + map11);
		List<NxDistributerWeightEntity> distributerWeightEntities =  nxDistributerWeightService.queryWeightListByParams(map11);
		//

		Map<String, Object> map1 = new HashMap<>();
		map1.put("disId", disId);
		map1.put("status", 3);
		map1.put("purStatus", 3);
		map1.put("purType", -1);
		//新订单
		int newCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

		// 出库
		map1.put("purStatus", 5);
		map1.put("purType", 0);
		int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

		// 订货
		map1.put("purType", 1);
		map1.put("inputType", 1);
		System.out.println("wxxxxxxx" + map1);
		int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

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
		System.out.println("mapaosoisis" + mapOk);
		int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

		//打印完成
		mapOk.put("inputType", 0);
		mapOk.put("batchId", -1);
		mapOk.put("weightStatusEqual", 1);
		int printCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);


//        //////************************************
		// map4 未发送或未打印
		Map<String, Object> map4 = new HashMap<>();

		map4.put("disId", disId);
		map4.put("status", 1);
		map4.put("weightId", -1);
		map4.put("batchId", -1);
		map4.put("equalInputType", 0);
		Integer unDoCount = nxDisPurchaseGoodsService.queryPurOrderCount(map4);

		// map4 订货已发送
		map4.put("status", 2); //NX_DIS_PURCHASE_GOODS_IS_PURCHASE == 2 huifu
		map4.put("orderStatus", 3);
		map4.put("batchId", 1);
		Integer wxIsBatchCountUnReply = nxDisPurchaseGoodsService.queryPurOrderCount(map4);
		map4.put("status", 4);
		map4.put("dayuStatus", 1);
		Integer wxIsBatchCountHaveReply = nxDisPurchaseGoodsService.queryPurOrderCount(map4);


		//  map4 已打印
		map4.put("batchId", -1);
		map4.put("weightId", 1);
		map4.put("weightStatusEqual", 0);
		map4.put("orderStatus", 3);
		map4.put("status", 4);
		Integer isPrintCount = nxDisPurchaseGoodsService.queryPurOrderCount(map4);
		map4.put("weightStatusEqual", 1);
		Integer isPrintHaveWeightCount = nxDisPurchaseGoodsService.queryPurOrderCount(map4);


		Map<String, Object> map111 = new HashMap<>();
		map111.put("arr", distributerWeightEntities);
		map111.put("newCount", newCount);
		map111.put("stockCount", stockCount);
		map111.put("stockCountOk", stockCountOK);
		map111.put("wxCount", wxCount);
		map111.put("printCount", printCount);
		map111.put("wxCountOk", wxCountOk);
		map111.put("printCountOk", printCountOk);

		map111.put("unDoCount", unDoCount);
		map111.put("isBatchCountUnRepaly", wxIsBatchCountUnReply);
		map111.put("isBatchCountHaveRepaly", wxIsBatchCountHaveReply);
		map111.put("isPrintCount", isPrintCount);
		map111.put("havePrintCount", isPrintHaveWeightCount);


		return R.ok().put("data", map111);
	}


	@RequestMapping(value = "/disUserGetWeightingList", method = RequestMethod.POST)
	@ResponseBody
	public R disUserGetWeightingList(Integer disId, Integer status, Integer type) {
		Map<String, Object> map1 = new HashMap<>();
		map1.put("disId", disId);
		map1.put("equalStatus", status);
//		map1.put("notType", 2);
		map1.put("type", type);
		if(status == 1){
			map1.put("startDate", formatWhatDay(-2));
			map1.put("stopDate", formatWhatDay(0));
		}
		System.out.println("tpypeooe" + map1);
		List<NxDistributerWeightEntity> distributerWeightEntities =  nxDistributerWeightService.queryWeightListByParams(map1);

		Map<String, Object> mapW = new HashMap<>();
		mapW.put("disId", disId);
		mapW.put("weightType", type); //出库单 == 3
		mapW.put("status", 2);
		mapW.put("hasWeight", -1);
		Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(mapW);

		mapW.put("hasWeight", 1);
		Integer count1 = nxDepartmentOrdersService.queryDepOrdersAcount(mapW);
		Map<String, Object> map = new HashMap<>();
		map.put("arr", distributerWeightEntities);
		map.put("unFinishCount", count);
		map.put("haveFinishCount", count1);
		return R.ok().put("data", map);
	}



	@RequestMapping(value = "/disDeleteStockWeight/{id}")
	@ResponseBody
	public R disDeleteStockWeight(@PathVariable Integer id) {
		Map<String, Object> map = new HashMap<>();
		map.put("weightId", id);
		List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
		if(ordersEntities.size() > 0){
			for (NxDepartmentOrdersEntity order : ordersEntities) {
				order.setNxDoWeightId(null);
				System.out.println("orsttuusust" + order.getNxDoPurchaseStatus());
				if(order.getNxDoPurchaseStatus() < 4){
					order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
				}
				nxDepartmentOrdersService.update(order);

				if(order.getNxDoGbDepartmentOrderId() != null){
					//更新gbDepOrder
					Integer nxDepartmentOrdersId = order.getNxDepartmentOrdersId();
					GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(nxDepartmentOrdersId);
					if (gbDepartmentOrdersEntity != null) {
						gbDepartmentOrdersEntity.setGbDoBuyStatus(0);
						gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
					}
				}
			}
		}
		nxDistributerWeightService.delete(id);

		return R.ok();
	}

	@RequestMapping(value = "/disDeletePurGoodsWeight/{id}")
	@ResponseBody
	public R disDeletePurGoodsWeight(@PathVariable Integer id) {

		Map<String, Object> mapW = new HashMap<>();
		mapW.put("weightId", id);
		List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(mapW);
		if(ordersEntities.size() > 0){
			for(NxDepartmentOrdersEntity ordersEntity: ordersEntities){
				Integer nxDoPurchaseStatus = ordersEntity.getNxDoPurchaseStatus();
				if(nxDoPurchaseStatus.equals(getNxDepOrderBuyStatusFinishOut())){
					return R.error(-1,"订单已出货，没必要删除");
				}
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put("weightId", id);
		List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(map);
        if(purchaseGoodsEntities.size() > 0){
			for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
				purchaseGoodsEntity.setNxDpgNxWeightId(null);
				purchaseGoodsEntity.setNxDpgStatus(getNxDepOrderBuyStatusUnPurchase());
				purchaseGoodsEntity.setNxDpgPurchaseDate(null);
				purchaseGoodsEntity.setNxDpgTime(null);
				purchaseGoodsEntity.setNxDpgPurUserId(null);

				nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
				Integer nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();
				Map<String, Object> mapP = new HashMap<>();
				mapP.put("purGoodsId", nxDistributerPurchaseGoodsId);
				List<NxDepartmentOrdersEntity> ordersEntitiesP = nxDepartmentOrdersService.queryDisOrdersByParams(mapP);
				for (NxDepartmentOrdersEntity orders : ordersEntitiesP) {
					orders.setNxDoStatus(getNxOrderStatusNew());
					orders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
					orders.setNxDoWeightId(null);
					nxDepartmentOrdersService.update(orders);
				}
			}
		}

		nxDistributerWeightService.delete(id);
	    return R.ok();
	}




	@RequestMapping(value = "/disGetDepWeightingList",  method = RequestMethod.POST)
	@ResponseBody
	public R disGetDepWeightingList(Integer depFatherId, Integer resFatherId, Integer gbDepFatherId,
								 Integer status) {
		Map<String, Object> map = new HashMap<>();
		map.put("depFatherId", depFatherId);
		map.put("resFatherId", resFatherId);
		map.put("gbDepFatherId", gbDepFatherId);
		map.put("equalStatus", status);
		if(status == 1){
			map.put("startDate", formatWhatDay(-2));
			map.put("stopDate", formatWhatDay(0));
		}
	    List<NxDistributerWeightEntity> distributerWeightEntities =  nxDistributerWeightService.queryWeightListByParams(map);


		Map<String, Object> mapS = new HashMap<>();
		mapS.put("purchaseType", 1);
		mapS.put("batchId", 0);
		mapS.put("weightId", 1);
		mapS.put("equalStatus", 0);
		mapS.put("equalPurStatus", 0); // dep是1，采购是2 出库是3
		Integer printOrderCount = nxDepartmentOrdersService.queryDepOrdersAcount(mapS);
		Map<String, Object> map111 = new HashMap<>();
		map111.put("arr", distributerWeightEntities);
		map111.put("stockOrderCount", printOrderCount);
	    return R.ok().put("data", map111);
	}

	@RequestMapping(value = "/disSaveWeightPurGoodsQuick", method = RequestMethod.POST)
	@ResponseBody
	public R disSaveWeightPurGoodsQuick (@RequestBody NxDistributerWeightEntity weight) {

		weight.setNxDwStatus(0);
		weight.setNxDwType(2);
		weight.setNxDwItemFinishCount(0);
		weight.setNxDwDate(formatWhatDay(0));
		nxDistributerWeightService.save(weight);
		Integer nxDistributerWeightId = weight.getNxDistributerWeightId();

		int haveWeight = 0;
		String purGoodsIds = weight.getNxDwPurGoodsIds();
		String[] split = purGoodsIds.split(",");
		for (String orderId : split) {
			NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDisPurchaseGoodsService.queryObject(Integer.valueOf(orderId));
			purchaseGoodsEntity.setNxDpgNxWeightId(nxDistributerWeightId);
			purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
			purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
			purchaseGoodsEntity.setNxDpgTime(formatWhatTime(0));
			purchaseGoodsEntity.setNxDpgPurUserId(weight.getNxDwUserId());
			nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
			Map<String, Object> mapP = new HashMap<>();
			mapP.put("purGoodsId", purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
			List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(mapP);
			if(ordersEntities.size() > 0){
				for(NxDepartmentOrdersEntity ordersEntity: ordersEntities){
					ordersEntity.setNxDoWeightId(nxDistributerWeightId);
					Integer nxDpgDisGoodsId = purchaseGoodsEntity.getNxDpgDisGoodsId();
					NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDpgDisGoodsId);
					String nxDgGoodsStandardname = nxDistributerGoodsEntity.getNxDgGoodsStandardname();
					if(ordersEntity.getNxDoStandard().equals(nxDgGoodsStandardname)){
						ordersEntity.setNxDoWeight(ordersEntity.getNxDoQuantity());
						//
						if(ordersEntity.getNxDoPrice() != null && new BigDecimal(ordersEntity.getNxDoPrice()).compareTo(BigDecimal.ZERO) == 1){
							BigDecimal decimal = new BigDecimal(ordersEntity.getNxDoQuantity()).multiply(new BigDecimal(ordersEntity.getNxDoPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
							ordersEntity.setNxDoSubtotal(decimal.toString());
						}
						BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
						BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoQuantity());
						BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
						ordersEntity.setNxDoCostSubtotal(decimal.toString());
						haveWeight = haveWeight + 1;
					}
					ordersEntity.setNxDoStatus(getNxOrderStatusProcurement());
					ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusIsPurchase());
					nxDepartmentOrdersService.update(ordersEntity);
				}
			}
		}
		weight.setNxDwItemFinishCount(haveWeight);
		if(haveWeight == weight.getNxDwItemCount()){
			weight.setNxDwStatus(1);
		}
		nxDistributerWeightService.update(weight);
		return R.ok();
	}



	@RequestMapping(value = "/disSaveWeightOrders", method = RequestMethod.POST)
	@ResponseBody
	public R disSaveWeightOrders (@RequestBody NxDistributerWeightEntity weight) {
		System.out.println("savaororoorro");

		weight.setNxDwStatus(0);
		weight.setNxDwType(1);
		weight.setNxDwItemFinishCount(0);
		weight.setNxDwDate(formatWhatDay(0));
		nxDistributerWeightService.save(weight);
		Integer nxDistributerWeightId = weight.getNxDistributerWeightId();

		String nxDwOrderIds = weight.getNxDwOrderIds();
		String[] split = nxDwOrderIds.split(",");
		for (String orderId : split) {
			NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(Integer.valueOf(orderId));
			ordersEntity.setNxDoWeightId(nxDistributerWeightId);
			NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(ordersEntity.getNxDoDisGoodsId());
			String nxDgGoodsStandardname = nxDistributerGoodsEntity.getNxDgGoodsStandardname();
			if(ordersEntity.getNxDoStandard().equals(nxDgGoodsStandardname)){
				ordersEntity.setNxDoWeight(ordersEntity.getNxDoQuantity());
				BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
				BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoQuantity());
				BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
				ordersEntity.setNxDoCostSubtotal(decimal.toString());
				System.out.println("dafkdajfkadsjfidiidddi===" + ordersEntity.getNxDepartmentOrdersId());
			}
			ordersEntity.setNxDoStatus(getNxOrderStatusProcurement());
			ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusIsPurchase());
			nxDepartmentOrdersService.update(ordersEntity);
		}

		return R.ok();
	}

	@RequestMapping(value = "/getWeightStockOrder/{id}")
	@ResponseBody
	public R getWeightStockOrder(@PathVariable Integer id) {
		Map<String, Object> map = new HashMap<>();
		map.put("weightId",id);
		System.out.println("fafawieeieieeie");
		List<NxDistributerFatherGoodsEntity> fatherGoodsEntities =  nxDistributerWeightService.queryFatherGoodsStockOrder(map);
		return R.ok().put("data", fatherGoodsEntities);
	}


	@RequestMapping(value = "/getWeightOrders/{id}")
	@ResponseBody
	public R getWeightOrders(@PathVariable Integer id) {
		NxDistributerWeightEntity weightEntity=  nxDistributerWeightService.queryWeightOrdersById(id);
	    return R.ok().put("data", weightEntity);
	}

	@RequestMapping(value = "/getWeightGoods/{id}")
	@ResponseBody
	public R getWeightGoods(@PathVariable Integer id) {
		List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities =  nxDistributerWeightService.queryWeightGoodsById(id);
		return R.ok().put("data", purchaseGoodsEntities);
	}


	@RequestMapping(value = "/getWeightDep/{id}")
	@ResponseBody
	public R getWeightDep(@PathVariable Integer id) {
		System.out.println("depeieieieiei" + id);
		List<NxDepartmentEntity> departmentEntities =  nxDistributerWeightService.queryWeightDepOrders(id);
		return R.ok().put("data", departmentEntities);
	}





	

	
}
