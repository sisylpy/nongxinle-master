package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 02-12 21:10
 */

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.GbDistributerEntity;
import com.nongxinle.entity.GbDistributerPayListEntity;
import com.nongxinle.service.GbDistributerService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDistributerPayListEntity;
import com.nongxinle.service.GbDistributerPayListService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatYear;


@RestController
@RequestMapping("api/gbdistributerpaylist")
public class GbDistributerPayListController {
	@Autowired
	private GbDistributerPayListService gbDistributerPayListService;
	@Autowired
	private GbDistributerService gbDistributerService;


	@RequestMapping(value = "/addPoints", method = RequestMethod.POST)
	@ResponseBody
	public R addPoints (Integer disId, String points) {
		GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(disId);
		BigDecimal decimal = new BigDecimal(gbDistributerEntity.getGbDistributerBuyQuantity());
		System.out.println("decimdall========" + decimal);
		BigDecimal decimal1 = new BigDecimal(points);
		System.out.println("nxdkkddkdk00" + gbDistributerEntity.getGbDistributerBuyQuantity());
		BigDecimal add = decimal.add(decimal1).setScale(0, BigDecimal.ROUND_HALF_UP);
		gbDistributerEntity.setGbDistributerBuyQuantity(add.toString());
		System.out.println("nxdkkddkdk11" + gbDistributerEntity.getGbDistributerBuyQuantity());
		gbDistributerService.update(gbDistributerEntity);
		return R.ok().put("data",gbDistributerEntity);
	}

	@RequestMapping(value = "/disGetPayListDetail",method = RequestMethod.POST)
	@ResponseBody
	public R disGetPayListDetail(Integer disId,Integer limit, Integer page, Integer type) {

		System.out.println("ididid" + disId);
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		System.out.println("ididid" + map);
		List<GbDistributerPayListEntity> listEntities = gbDistributerPayListService.queryPayListListByParams(map);
		System.out.println("rlisisisis" + listEntities);
		Map<String, Object> mapCount = new HashMap<>();
		mapCount.put("disId", disId);
		int total = gbDistributerPayListService.queryDisPayListCount(mapCount);
		PageUtils pageUtil = new PageUtils(listEntities, total, limit, page);
		return R.ok().put("page", pageUtil);
	}


	
}
