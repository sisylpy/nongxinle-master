package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 04-25 10:39
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxCommunityOrdersEntity;
import com.nongxinle.entity.NxCommunityOrdersSubEntity;
import com.nongxinle.service.NxCommunityOrdersSubService;
import org.apache.poi.util.Internal;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunitySplicingOrdersEntity;
import com.nongxinle.service.NxCommunitySplicingOrdersService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxcommunitysplicingorders")
public class NxCommunitySplicingOrdersController {
	@Autowired
	private NxCommunitySplicingOrdersService nxCommSplicingOrdersService;
	@Autowired
	private NxCommunityOrdersSubService nxCommunityOrdersSubService;


	@RequestMapping(value = "/outPindan/{id}")
	@ResponseBody
	public R outPindan(@PathVariable Integer id) {
		nxCommSplicingOrdersService.delete(id);
		Map<String, Object> map = new HashMap<>();
		map.put("orderId", id);
		map.put("xiaoyuStatus", 2);
		map.put("type", 40);
		List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
		if(nxCommunityOrdersSubEntities.size() > 0){
			for(NxCommunityOrdersSubEntity subEntity: nxCommunityOrdersSubEntities){
				nxCommunityOrdersSubService.delete(subEntity.getNxCommunityOrdersSubId());
			}
		}
		return R.ok();
	}





	@RequestMapping(value = "/saveOrderPindan", method = RequestMethod.POST)
	@ResponseBody
	public R saveOrderPindan (@RequestBody NxCommunitySplicingOrdersEntity splicingOrdersEntity) {
		splicingOrdersEntity.setNxCsoStatus(2);
		System.out.println("whwhhwwhwhwhwhhwh"+ splicingOrdersEntity);
		nxCommSplicingOrdersService.update(splicingOrdersEntity);

		List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = splicingOrdersEntity.getNxCommunityOrdersSubEntities();
		if(nxCommunityOrdersSubEntities.size() > 0){
			for(NxCommunityOrdersSubEntity subEntity: nxCommunityOrdersSubEntities){
				subEntity.setNxCosStatus(1);
				nxCommunityOrdersSubService.update(subEntity);
			}
		}

		return R.ok();
	}











}
