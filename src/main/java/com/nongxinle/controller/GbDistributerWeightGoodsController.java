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
import com.nongxinle.service.GbDepartmentOrdersService;
import com.nongxinle.service.GbDistributerWeightTotalService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.GbDistributerWeightGoodsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;


@RestController
@RequestMapping("api/gbdistributerweightgoods")
public class GbDistributerWeightGoodsController {
	@Autowired
	private GbDistributerWeightGoodsService gbDisWeightGoodsService;
	@Autowired
	private GbDepartmentOrdersService gbDepartmentOrdersService;
	@Autowired
	private GbDistributerWeightTotalService gbDisWeightTotalService;



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
				order.setGbDoBuyStatus(getGbOrderStatusNew());
				order.setGbDoWeightGoodsId(null);
				gbDepartmentOrdersService.update(order);
			}
		}
		gbDisWeightGoodsService.delete(id);

		return R.ok();
	}





}
