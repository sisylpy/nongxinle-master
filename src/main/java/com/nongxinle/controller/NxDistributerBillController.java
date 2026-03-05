package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 02-22 22:34
 */

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatYear;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisPayListWeb;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisPurchaseGoodsFinishPay;


@RestController
@RequestMapping("api/nxdistributerbill")
public class NxDistributerBillController {
	@Autowired
	private NxDistributerBillService nxDistributerBillService;
	@Autowired
	private NxDepartmentOrdersService nxDepartmentOrdersService;
	@Autowired
	private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
	@Autowired
	private NxDistributerGoodsService dgService;
	@Autowired
	private NxDistributerFatherGoodsService distributerFatherGoodsService;


	@RequestMapping(value = "/changeDisBillStatus", method = RequestMethod.POST)
	@ResponseBody
	public R changeDisBillStatus (Integer billId, Integer status) {

		NxDistributerBillEntity nxDistributerBillEntity = nxDistributerBillService.queryObject(billId);
		nxDistributerBillEntity.setNxDbdStatus(status);
		nxDistributerBillService.update(nxDistributerBillEntity);
		return R.ok();
	}

	@RequestMapping(value = "/disGetConfirmBills/{disId}")
	@ResponseBody
	public R disGetConfirmBills(@PathVariable Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("orderDisId", disId);
		map.put("equalStatus", 0);
		List<NxDistributerBillEntity> billEntities  = nxDistributerBillService.queryDisBillsDetail(map);
		map.put("orderDisId", null);
		map.put("offerDisId", disId);
		map.put("equalStatus", 2);
		System.out.println("mappapap" + map);
		List<NxDistributerBillEntity> billEntitiesPayed  = nxDistributerBillService.queryDisBillsDetail(map);

		billEntities.addAll(billEntitiesPayed);

		return R.ok().put("data", billEntities);
	}

	@RequestMapping(value = "/disGetNxDistributerBillsWithStatus", method = RequestMethod.POST)
	@ResponseBody
	public R disGetNxDistributerBillsWithStatus(Integer offerDisId, Integer orderDisId, String type, String startDate, String stopDate) {

		Map<String, Object> map = new HashMap<>();
		map.put("offerDisId", offerDisId);
		map.put("orderDisId", orderDisId);
		map.put("type", type);
		map.put("startDate", startDate);
		map.put("stopDate", stopDate);
		System.out.println("tyype111" + map);
		List<NxDistributerBillEntity> billEntities = nxDistributerBillService.queryDisBillsDetail(map);

		map.put("offerDisId", orderDisId);
		map.put("orderDisId", offerDisId);
		System.out.println("tyype222" + map);
		Integer integer = nxDistributerBillService.queryDisBillsDetailCount(map);
		return R.ok().put("data", billEntities)
				.put("collCount", integer);
	}


	@Transactional
	@RequestMapping(value = "/saveCollationoBill", method = RequestMethod.POST)
	@ResponseBody
	public R saveCollationoBill(@RequestBody NxDistributerBillEntity nxDistributerBillEntity) {
		System.out.println("savephoneacoucnccucnncn");
		nxDistributerBillEntity.setNxDbdStatus(0);
		nxDistributerBillEntity.setNxDbdDate(formatWhatDay(0));
		nxDistributerBillEntity.setNxDbdTime(formatWhatYearDayTime(0));
		nxDistributerBillEntity.setNxDbdMonth(formatWhatMonth(0));
		nxDistributerBillEntity.setNxDbdWeek(getWeekOfYear(0).toString());
		nxDistributerBillEntity.setNxDbdDay(getWeek(0));
		nxDistributerBillEntity.setNxDbdYear(formatWhatYear(0));
		nxDistributerBillService.save(nxDistributerBillEntity);

		updateNxDistributerBillData(nxDistributerBillEntity);

		return R.ok();
	}



	private void updateNxDistributerBillData(NxDistributerBillEntity nxDistributerBillEntity) {

		System.out.println("savesonnsuusbsilllnwnewenewwnwnennwnwn" + nxDistributerBillEntity);
		BigDecimal billTotal = new BigDecimal(0);
		BigDecimal billProfit = new BigDecimal(0);

		Map<String, Object> map = new HashMap<>();
		map.put("disId",nxDistributerBillEntity.getNxDbdOfferNxDisId());
		map.put("equalStatus", 2);
		map.put("equalPurStatus", 4);
		map.put("collNxDisId", nxDistributerBillEntity.getNxDbdOrderDisId());


		List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
		for (NxDepartmentOrdersEntity orders : ordersEntities) {
			//0 subtotal
			billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));

			Map<String, Object> mapDG = new HashMap<>();
			mapDG.put("disId", nxDistributerBillEntity.getNxDbdOfferNxDisId());
			mapDG.put("disGoodsId", orders.getNxDoDisGoodsId());
			mapDG.put("depId", orders.getNxDoDepartmentId());
			System.out.println("dedigodo" + mapDG);
			NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(mapDG);
			NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(orders.getNxDoDisGoodsId());

			//1，配送商自己的客户
			if (nxDepartmentDisGoodsEntity != null) {
				orders.setNxDoDepDisGoodsId(nxDepartmentDisGoodsEntity.getNxDepartmentDisGoodsId());
				nxDepartmentDisGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
				nxDepartmentDisGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
				nxDepartmentDisGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
				nxDepartmentDisGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
				nxDepartmentDisGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
				nxDepartmentDisGoodsEntity.setNxDdgDepGoodsName(orders.getNxDoGoodsName());
				nxDepartmentDisGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
				if (orders.getNxDoGoodsName() != null) {
					nxDepartmentDisGoodsEntity.setNxDdgOrderGoodsName(orders.getNxDoGoodsName());
				}

				nxDepartmentDisGoodsEntity.setNxDdgOrderPriceLevel(orders.getNxDoCostPriceLevel());
				nxDepartmentDisGoodsService.update(nxDepartmentDisGoodsEntity);

			} else {
				System.out.println("new Depdiiddid");
				NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
				disGoodsEntity.setNxDdgDepGoodsName(orders.getNxDoGoodsName());
				disGoodsEntity.setNxDdgDisGoodsId(orders.getNxDoDisGoodsId());
				disGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
				disGoodsEntity.setNxDdgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());

				NxDistributerFatherGoodsEntity fatherGoodsEntity = distributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
				Integer greatFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
				disGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);

				disGoodsEntity.setNxDdgDepGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
				disGoodsEntity.setNxDdgDepGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
				disGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
				disGoodsEntity.setNxDdgDepartmentId(orders.getNxDoDepartmentId());
				disGoodsEntity.setNxDdgDepartmentFatherId(orders.getNxDoDepartmentFatherId());
				disGoodsEntity.setNxDdgNxDistributerId(nxDistributerBillEntity.getNxDbdOfferNxDisId());
				//orderData
				disGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
				disGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
				disGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
				disGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
				disGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
				disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
				disGoodsEntity.setNxDdgGbDistributerId(orders.getNxDoGbDistributerId());
				disGoodsEntity.setNxDdgGoodsNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());

				if (orders.getNxDoGoodsName() != null) {
					disGoodsEntity.setNxDdgOrderGoodsName(orders.getNxDoGoodsName());
				}
				disGoodsEntity.setNxDdgOrderPriceLevel(orders.getNxDoCostPriceLevel());
				nxDepartmentDisGoodsService.save(disGoodsEntity);
				orders.setNxDoDepDisGoodsId(disGoodsEntity.getNxDepartmentDisGoodsId());
			}

			orders.setNxDoStatus(3);
			orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
			orders.setNxDoBillId(nxDistributerBillEntity.getNxDistributerBillId());
			nxDepartmentOrdersService.update(orders);

			//更新协作订单
//			Integer nxDoNxRestrauntOrderId = orders.getNxDepartmentOrdersId();
//			NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryByRestrauntId(nxDoNxRestrauntOrderId);
//			ordersEntity.setNxDoPurchaseStatus(2);
//			nxDepartmentOrdersService.update(ordersEntity);

			//迁移
			nxDepartmentOrdersService.moveOrderToHistory(orders);  // ✅ 迁移

			//updata weight
			Integer doDisGoodsId = orders.getNxDoDisGoodsId();
			NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
			BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
			BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
			BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
			distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
			dgService.update(distributerGoodsEntity);
		}

		nxDistributerBillEntity.setNxDbdProfitTotal(billProfit.toString());
		BigDecimal decimal = billProfit.divide(billTotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
		nxDistributerBillEntity.setNxDbdProfitScale(decimal.toString());
		nxDistributerBillService.update(nxDistributerBillEntity);


	}




	
}
