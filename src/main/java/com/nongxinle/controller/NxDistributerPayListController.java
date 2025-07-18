package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 10-20 11:05
 */

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.entity.NxDistributerPayEntity;
import com.nongxinle.service.NxDepartmentService;
import com.nongxinle.service.NxDistributerService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDistributerPayListEntity;
import com.nongxinle.service.NxDistributerPayListService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatYear;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisPayListRecord;


@RestController
@RequestMapping("api/nxdistributerpaylist")
public class NxDistributerPayListController {
	@Autowired
	private NxDistributerPayListService nxDistributerPayListService;
	@Autowired
	private NxDistributerService nxDistributerService;
	@Autowired
	private NxDepartmentService nxDepartmentService;


	@RequestMapping(value = "/depGetTodayRecordSeconds/{depFatherId}")
	@ResponseBody
	public R depGetTodayRecordSeconds(@PathVariable Integer depFatherId) {
		NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryObject(depFatherId);
		BigDecimal recordMinutes = new BigDecimal(nxDepartmentEntity.getNxDepartmentRecordMinutes());

		Map<String, Object> map = new HashMap<>();
		map.put("depFatherId", depFatherId);
		map.put("date", formatWhatDay(0));
		map.put("type", getNxDisPayListRecord());
	   int count =	nxDistributerPayListService.queryDisPayListCount(map);
		System.out.println("tootttc" + count);
	   if(count > 0){
		   int total =	nxDistributerPayListService.queryDepRecordSecondsTotal(map);
		   recordMinutes = recordMinutes.subtract(new BigDecimal(total));
	   }


		return R.ok().put("data", recordMinutes);
	}

	@RequestMapping(value = "/disGetPayListDetail",method = RequestMethod.POST)
	@ResponseBody
	public R disGetPayListDetail(Integer disId,Integer limit, Integer page, Integer type) {

		System.out.println("ididid" + disId);
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		map.put("type", type);
		System.out.println("ididid" + map);
		List<NxDistributerPayListEntity> listEntities = nxDistributerPayListService.queryPayListListByParams(map);
		System.out.println("rlisisisis" + listEntities);
		Map<String, Object> mapCount = new HashMap<>();
		mapCount.put("disId", disId);
		mapCount.put("type", type);
		int total = nxDistributerPayListService.queryDisPayListCount(mapCount);
		PageUtils pageUtil = new PageUtils(listEntities, total, limit, page);
	    return R.ok().put("page", pageUtil);
	}


	
	@RequestMapping(value = "/disAaddRecord", method = RequestMethod.POST)
	@ResponseBody
	public R disAaddRecord (@RequestBody  NxDistributerPayListEntity payListEntity) {

		NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(payListEntity.getNxNdplNxDisId());

		payListEntity.setNxNdplPayTime(formatFullTime());
		payListEntity.setNxNdplPayDate(formatWhatDay(0));
		payListEntity.setNxNdplPayMonth(formatWhatMonth(0));
		payListEntity.setNxNdplPayYear(formatWhatYear(0));
		payListEntity.setNxNdplStatus(0);
		payListEntity.setNxNdplType(getNxDisPayListRecord());
		payListEntity.setNxNdplRestPoints(Integer.valueOf(nxDistributerEntity.getNxDistributerBuyQuantity()));
		payListEntity.setNxNdplNxDbId(-1);
		nxDistributerPayListService.save(payListEntity);


		BigDecimal decimal0 = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());
		BigDecimal decimal1 = new BigDecimal(payListEntity.getNxNdplPaySubtotal());
		BigDecimal restPoints = decimal0.subtract(decimal1).setScale(0,BigDecimal.ROUND_HALF_UP);
		nxDistributerEntity.setNxDistributerBuyQuantity(restPoints.toString());
		nxDistributerService.update(nxDistributerEntity);
	    return R.ok();
	}
	

	
}
