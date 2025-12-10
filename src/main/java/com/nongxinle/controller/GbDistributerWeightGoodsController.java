package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 11-30 10:09
 */

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbOrderStatusNew;


@RestController
@RequestMapping("api/gbdistributerweightgoods")
public class GbDistributerWeightGoodsController {
	@Autowired
	private GbDistributerWeightGoodsService gbDisWeightGoodsService;
	@Autowired
	private GbDepartmentOrdersService gbDepartmentOrdersService;
	@Autowired
	private GbDistributerWeightTotalService gbDisWeightTotalService;
	@Autowired
	private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;
	@Autowired
	private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;



	@RequestMapping(value = "/getDepGoodsWeightGoods", method = RequestMethod.POST)
	@ResponseBody
	public R getDepGoodsWeightGoods (Integer disGoodsId, Integer depId, String startDate, String stopDate) {
		Map<String, Object> map = new HashMap<>();
		map.put("disGoodsId", disGoodsId);
		map.put("depId", depId);
		map.put("startDate",startDate);
		map.put("stopDate",stopDate);

		List<GbDistributerWeightGoodsEntity> weightGoodsEntities = gbDisWeightGoodsService.queryWeightGoodsWithOrderByParams(map);

		return R.ok().put("data", weightGoodsEntities);
	}


	@RequestMapping(value = "/stockGetToWeightFatherGoods", method = RequestMethod.POST)
	@ResponseBody
	public R stockGetToWeightFatherGoods(Integer depId, Integer isSelf) {

		Map<String, Object> map = new HashMap<>();
		map.put("depId", depId);
		map.put("status",  0);
		map.put("isSelf",  1);
		List<GbDistributerFatherGoodsEntity> departmentDisGoodsEntities = gbDisWeightGoodsService.queryFatherGoodsToWeightByParams(map);
		Map<String, Object> map3 = new HashMap<>();
		map3.put("depId", depId);
		map3.put("date", formatWhatDay(0));
		map3.put("isSelf", isSelf);
		int count = gbDisWeightTotalService.queryDepWeightCountByParams(map3);
		BigDecimal trade = new BigDecimal(count).add(new BigDecimal(1));
		String name = "";
		if(isSelf == 0){
			name = "CKD";
		}
		if(isSelf == 1){
			name = "ZZD";
		}
		String s = formatWhatDayString(0) + name +"-" + trade;


		Map<String, Object> map1 = new HashMap<>();
		map1.put("arr", departmentDisGoodsEntities);
		map1.put("tradeNo", s);
		return R.ok().put("data", map1);
	}


	@RequestMapping(value = "/stockGetToPrepareShelfGoods/{depId}")
	@ResponseBody
	public R stockGetToPrepareShelfGoods(@PathVariable Integer depId) {
		Map<String, Object> map = new HashMap<>();
		map.put("depId", depId);
		map.put("status",  0);
		map.put("isSelf",  0);
		System.out.println("mapapapappaappapapa" + map);
		List<GbDistributerGoodsShelfEntity> shelfEntities = gbDisWeightGoodsService.queryShelfGoodsToWeightByParams(map);

		Map<String, Object> map3 = new HashMap<>();
		map3.put("depId", depId);
		map3.put("date", formatWhatDay(0));
		map3.put("isSelf", 0);
		int count = gbDisWeightTotalService.queryDepWeightCountByParams(map3);
		BigDecimal trade = new BigDecimal(count).add(new BigDecimal(1));
		String s = formatWhatDayString(0) + "CKD" +"-" + trade;
		Map<String, Object> map1 = new HashMap<>();
		map1.put("arr", shelfEntities);
		map1.put("tradeNo", s);
		return R.ok().put("data",map1 );
	}



	@RequestMapping(value = "/saveWeightGoodsWithOrders", method = RequestMethod.POST)
	@ResponseBody
	public R saveWeightGoodsWithOrders(@RequestBody GbDistributerWeightGoodsEntity weightGoodsEntity) {


		List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = weightGoodsEntity.getGbDepartmentOrdersEntities();
		weightGoodsEntity.setGbDwgStatus(getGbWeightGoodsStatusPrepare());
	    weightGoodsEntity.setGbDwgDate(formatWhatDay(0));
		weightGoodsEntity.setGbDwgLossWeight("0");
		weightGoodsEntity.setGbDwgWasteWeight("0");
		weightGoodsEntity.setGbDwgReturnWeight("0");
		gbDisWeightGoodsService.save(weightGoodsEntity);

		if (gbDepartmentOrdersEntities.size() > 0) {
			for (GbDepartmentOrdersEntity order : gbDepartmentOrdersEntities) {
				order.setGbDoBuyStatus(getGbOrderStatusProcurement());
				order.setGbDoWeightGoodsId(weightGoodsEntity.getGbDistributerWeightGoodsId());
				gbDepartmentOrdersService.update(order);
			}
		}
		return R.ok();
	}

	@RequestMapping(value = "/updateWeightGoods", method = RequestMethod.POST)
	@ResponseBody
	public R updateWeightGoods (@RequestBody GbDistributerWeightGoodsEntity weightGoodsEntity) {
		List<GbDepartmentOrdersEntity> appendOrdersEntities = weightGoodsEntity.getAppendOrdersEntities();
		if(appendOrdersEntities.size() > 0){
			for (GbDepartmentOrdersEntity order : appendOrdersEntities) {
				order.setGbDoBuyStatus(getGbOrderStatusProcurement());
				order.setGbDoWeightGoodsId(weightGoodsEntity.getGbDistributerWeightGoodsId());
				gbDepartmentOrdersService.update(order);
			}
		}
		gbDisWeightGoodsService.update(weightGoodsEntity);
		return R.ok();
	}

	@RequestMapping(value = "/delWeightGoods/{id}")
	@ResponseBody
	public R delWeightGoods(@PathVariable Integer id) {

		Map<String, Object> map = new HashMap<>();
		map.put("weightGoodsId", id);
		List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
		if(ordersEntities.size() > 0){
			for (GbDepartmentOrdersEntity order : ordersEntities) {
				if(order.getGbDoStatus() < 3){
					order.setGbDoBuyStatus(getGbOrderStatusNew());
					order.setGbDoStatus(getGbOrderStatusNew());
					order.setGbDoWeightGoodsId(null);
					order.setGbDoWeight("0");
					order.setGbDoSubtotal("0");
					gbDepartmentOrdersService.update(order);

					Map<String, Object> mapS = new HashMap<>();
					mapS.put("orderId", order.getGbDepartmentOrdersId());
					List<GbDepartmentGoodsStockEntity> departmentGoodsStockEntities = gbDepartmentGoodsStockService.queryDepStockListByParams(mapS);
					if(departmentGoodsStockEntities.size() > 0){
						for(GbDepartmentGoodsStockEntity stockEntity: departmentGoodsStockEntities){

							//修改出库部门数据
							Integer gbDgsGbGoodsStockId = stockEntity.getGbDgsGbGoodsStockId();
							GbDepartmentGoodsStockEntity outStockEntity = gbDepartmentGoodsStockService.queryObject(gbDgsGbGoodsStockId);
							BigDecimal outStockRestWeight = new BigDecimal(outStockEntity.getGbDgsRestWeight());
							BigDecimal outStockRestSubtotal = new BigDecimal(outStockEntity.getGbDgsRestSubtotal());
							BigDecimal newWeight = new BigDecimal(stockEntity.getGbDgsWeight());
							BigDecimal newSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());
							BigDecimal outTotalRestWeight = outStockRestWeight.add(newWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
							BigDecimal outTotalRestSubtotal = outStockRestSubtotal.add(newSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
							outStockEntity.setGbDgsRestWeight(outTotalRestWeight.toString());
							outStockEntity.setGbDgsRestSubtotal(outTotalRestSubtotal.toString());

							//
							if (outStockEntity.getGbDgsRestWeightShowStandard() != null && !outStockEntity.getGbDgsRestWeightShowStandard().trim().isEmpty()) {
								Integer gbDgsGbDepDisGoodsId = outStockEntity.getGbDgsGbDepDisGoodsId();
								GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDgsGbDepDisGoodsId);
								BigDecimal gbDdgShowStandardScale = new BigDecimal(departmentDisGoodsEntity.getGbDdgShowStandardScale());
								BigDecimal newShowStandardWeight = outTotalRestWeight.divide(gbDdgShowStandardScale, 1, BigDecimal.ROUND_HALF_UP);
								outStockEntity.setGbDgsRestWeightShowStandard(newShowStandardWeight.toString());
								outStockEntity.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
							}
							gbDepartmentGoodsStockService.update(outStockEntity);

							System.out.println("outstocenene" + outStockEntity.getGbDgsGbDepDisGoodsId());
							updateDepDisGoods(stockEntity, outStockEntity.getGbDgsGbDepDisGoodsId(), "add");


							//删除自己
							gbDepartmentGoodsStockService.delete(stockEntity.getGbDepartmentGoodsStockId());

							gbDepartmentGoodsStockService.delete(stockEntity.getGbDepartmentGoodsStockId());
						}
					}
				}else{
					order.setGbDoWeightGoodsId(null);
					gbDepartmentOrdersService.update(order);
				}
			}
		}
		gbDisWeightGoodsService.delete(id);

		return R.ok();
	}



	private void updateDepDisGoods(GbDepartmentGoodsStockEntity stockEntity, Integer depDisGoodsId, String what) {
		System.out.println("updateDepDisGoodsupdateDepDisGoods" + what);
		BigDecimal stockSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());
		BigDecimal stockWeight = new BigDecimal(stockEntity.getGbDgsWeight());
		System.out.println("sotoscksubd;dldl" + stockWeight);
		System.out.println("sotoscksubd;dldl" + stockSubtotal);
		BigDecimal subTotal = new BigDecimal(0);
		BigDecimal weight = new BigDecimal(0);
		GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
		System.out.println("depgoeoidididiid" + depDisGoodsEntity.getGbDepartmentDisGoodsId());
		if (what.equals("add")) {
			subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).add(stockSubtotal);
			weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).add(stockWeight);
			System.out.println("adddddd" + subTotal + "weight" + weight);
		}
		if (what.equals("subtract")) {
			subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).subtract(stockSubtotal);
			weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).subtract(stockWeight);

		}
		System.out.println("zahuishsihsis" + subTotal);
		if (new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale()).compareTo(new BigDecimal(0)) == 1) {
			BigDecimal showScale = new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale());
			BigDecimal standardWeight = weight.divide(showScale, 1, BigDecimal.ROUND_HALF_UP);
			depDisGoodsEntity.setGbDdgShowStandardWeight(standardWeight.toString());
		}
		System.out.println("suttootototo-------" + subTotal + "weithht=====" + weight);
		depDisGoodsEntity.setGbDdgStockTotalSubtotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
		depDisGoodsEntity.setGbDdgStockTotalWeight(weight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
		depDisGoodsEntity.setGbDdgInventoryDate(formatWhatDay(0));
		depDisGoodsEntity.setGbDdgInventoryFullTime(formatWhatFullTime(0));

		gbDepartmentDisGoodsService.update(depDisGoodsEntity);

	}






}
