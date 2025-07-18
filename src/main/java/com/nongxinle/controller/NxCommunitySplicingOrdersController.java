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
import com.nongxinle.entity.NxCustomerUserCardEntity;
import com.nongxinle.service.NxCommunityOrdersSubService;
import com.nongxinle.service.NxCustomerUserCardService;
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
	@Autowired
	private NxCustomerUserCardService nxCustomerUserCardService;


	@RequestMapping(value = "/outPindan/{id}")
	@ResponseBody
	public R outPindan(@PathVariable Integer id) {

		Map<String, Object> map = new HashMap<>();
		map.put("splicingOrderId", id);
		map.put("orderType", 1);
		List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
		if(nxCommunityOrdersSubEntities.size() > 0){
			for(NxCommunityOrdersSubEntity subEntity: nxCommunityOrdersSubEntities){
				nxCommunityOrdersSubService.delete(subEntity.getNxCommunityOrdersSubId());
			}
		}

		NxCommunitySplicingOrdersEntity splicingOrdersEntity = nxCommSplicingOrdersService.queryObject(id);
		Map<String, Object> mapC = new HashMap<>();
		mapC.put("status", -1);
		mapC.put("type", 1);
		mapC.put("userId",splicingOrdersEntity.getNxCsoUserId());
		List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
		if(cardEntities.size() > 0){
			for(NxCustomerUserCardEntity userCardEntity: cardEntities){
				nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
			}
		}

		nxCommSplicingOrdersService.delete(id);
		return R.ok();
	}

	//
	@RequestMapping(value = "/clearPindan/{id}")
	@ResponseBody
	public R clearPindan(@PathVariable Integer id) {

		NxCommunitySplicingOrdersEntity splicingOrdersEntity = nxCommSplicingOrdersService.queryObject(id);
		splicingOrdersEntity.setNxCsoStatus(0);
		nxCommSplicingOrdersService.update(splicingOrdersEntity);

		Map<String, Object> map = new HashMap<>();
		map.put("splicingOrderId", id);
		map.put("orderType", 1);
		List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
		if(nxCommunityOrdersSubEntities.size() > 0){
			for(NxCommunityOrdersSubEntity subEntity: nxCommunityOrdersSubEntities){
				nxCommunityOrdersSubService.delete(subEntity.getNxCommunityOrdersSubId());
			}
		}

		Map<String, Object> mapC = new HashMap<>();
		mapC.put("status", -1);
		mapC.put("type", 1);
		mapC.put("userId",splicingOrdersEntity.getNxCsoUserId());
		List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
		if(cardEntities.size() > 0){
			for(NxCustomerUserCardEntity userCardEntity: cardEntities){
				nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
			}
		}
		return R.ok();
	}




	@RequestMapping(value = "/editSplicingOrder/{id}")
	@ResponseBody
	public R editSplicingOrder(@PathVariable Integer id) {
		NxCommunitySplicingOrdersEntity splicingOrdersEntity = nxCommSplicingOrdersService.queryObject(id);
		splicingOrdersEntity.setNxCsoStatus(1);
		nxCommSplicingOrdersService.update(splicingOrdersEntity);
		return R.ok();
	}

	@RequestMapping(value = "/saveSplincingOrder", method = RequestMethod.POST)
	@ResponseBody
	public R saveSplincingOrder (@RequestBody NxCommunitySplicingOrdersEntity splicingOrdersEntity) {

		splicingOrdersEntity.setNxCsoStatus(2);
		nxCommSplicingOrdersService.update(splicingOrdersEntity);
		return R.ok();
	}











}
