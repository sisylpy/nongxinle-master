package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 04-01 10:51
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbDepartmentGoodsStockRecordEntity;
import com.nongxinle.entity.GbDepartmentGoodsStockReduceEntity;
import com.nongxinle.service.GbDepartmentGoodsStockRecordService;
import com.nongxinle.service.GbDepartmentGoodsStockReduceService;
import com.nongxinle.service.GbDepartmentService;
import org.apache.ibatis.annotations.Arg;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDepDisGoodsSettleEntity;
import com.nongxinle.service.GbDepDisGoodsSettleService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/gbdepdisgoodssettle")
public class GbDepDisGoodsSettleController {
	@Autowired
	private GbDepDisGoodsSettleService gbDepDisGoodsSettleService;
	@Autowired
	private GbDepartmentService gbDepartmentService;

	
	
	@RequestMapping(value = "/getDepDisGoodsSettleList",method = RequestMethod.POST) 
	@ResponseBody
	public R getDepDisGoodsSettleList(Integer depFatherId, String month) {


//		List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(depFatherId);
//		List<Map<String, Object>> result = new ArrayList<>();
//		for (GbDepartmentEntity dep : departmentEntities) {
//			Map<String, Object>  depMap = new HashMap<>();
//			Integer gbDepartmentId = dep.getGbDepartmentId();
//			Map<String, Object> map = new HashMap<>();
//			map.put("depId", gbDepartmentId);
//			map.put("month", month);
//			List<GbDepDisGoodsSettleEntity> depDisGoodsSettleEntities =	gbDepDisGoodsSettleService.queryDepDisGoodsSettleByParams(map);
//            depMap.put("dep", dep.getGbDepartmentName());
//            depMap.put("arr", depDisGoodsSettleEntities);
//			result.add(depMap);
//		}
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object>  depMap = new HashMap<>();
		Map<String, Object> map = new HashMap<>();
		map.put("depId", depFatherId);
		map.put("month", month);
		List<GbDepDisGoodsSettleEntity> depDisGoodsSettleEntities =	gbDepDisGoodsSettleService.queryDepDisGoodsSettleByParams(map);
		depMap.put("dep", depFatherId);
		depMap.put("arr", depDisGoodsSettleEntities);
		result.add(depMap);

		 return R.ok().put("data", result);
	}
	

	
}
