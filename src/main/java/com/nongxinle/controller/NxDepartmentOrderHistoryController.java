package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 04-19 23:55
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxDistributerGoodsEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDepartmentOrderHistoryEntity;
import com.nongxinle.service.NxDepartmentOrderHistoryService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxdepartmentorderhistory")
public class NxDepartmentOrderHistoryController {
	@Autowired
	private NxDepartmentOrderHistoryService nxDepartmentOrderHistoryService;


	@RequestMapping(value = "/getNxDisBillDetail", method = RequestMethod.POST)
	@ResponseBody
	public R getNxDisBillDetail(Integer collNxDisId, Integer billId) {

		Map<String, Object> map = new HashMap<>();
		map.put("billId", billId);
		map.put("collNxDisId",collNxDisId );
		System.out.println("mappa" + map);

		List<NxDistributerGoodsEntity> goodsEntities = nxDepartmentOrderHistoryService.queryOfferOrdersGoods(map);

		return R.ok().put("data",goodsEntities);
	}

	@RequestMapping(value = "/disGetDepGoodsHistoryPrice", method = RequestMethod.POST)
	@ResponseBody
	public R disGetDepGoodsHistoryPrice (Integer depFatherId, Integer goodsId) {
		Map<String, Object> map = new HashMap<>();
		map.put("depFatherId", depFatherId);
		map.put("goodsId", goodsId);

		List<Map<String, Object>> list = nxDepartmentOrderHistoryService.queryDepGoodsHistoryPrice(map);

		System.out.println("lisis" + list);
		return R.ok().put("data", list);
	}

	@RequestMapping(value = "/disGetCollGoodsHistoryPrice", method = RequestMethod.POST)
	@ResponseBody
	public R disGetCollGoodsHistoryPrice (Integer collNxDisId, Integer goodsId, Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("collNxDisId", collNxDisId);
		map.put("goodsId", goodsId);
		map.put("nxDisId", disId);
		System.out.println("lisis" + map);

		List<Map<String, Object>> list = nxDepartmentOrderHistoryService.queryDepGoodsHistoryPrice(map);

		return R.ok().put("data", list);
	}




}
