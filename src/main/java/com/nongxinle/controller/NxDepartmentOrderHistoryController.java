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





}
