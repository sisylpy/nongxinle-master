package com.nongxinle.community.ecommerce.controller;

/**
 * 
 *
 * @author lpy
 * @date 11-28 21:17
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.entity.NxECommerceEntity;
import com.nongxinle.community.ecommerce.service.NxECommerceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.community.ecommerce.service.NxECommerceCommunityService;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxecommercecommunity")
public class NxECommerceCommunityController {
	@Autowired
	private NxECommerceCommunityService nxECommerceCommunityService;
	@Autowired
	private NxECommerceService nxECommerceService;


	@RequestMapping(value = "/getCommunityByCommerceId/{commerceId}")
	@ResponseBody
	public R getCommunityByCommerceId(@PathVariable Integer commerceId) {
		List<NxCommunityEntity> entities = nxECommerceCommunityService.queryCommunityByCommerceId(commerceId);
		NxECommerceEntity nxECommerceEntity = nxECommerceService.queryObject(commerceId);
		Map<String, Object> map = new HashMap<>();
		map.put("arr", entities);
		map.put("commerce", nxECommerceEntity);
		System.out.println("aqrrr" + entities.get(0).getNxCommunityDeliveryStartTime());
		return R.ok().put("data", map);
	}

//	@RequestMapping(value = "/getCommunityByAreaId/{areaId}")
//	@ResponseBody
//	public R getCommunityByAreaId(@PathVariable Integer areaId) {
//		List<NxCommunityEntity> entities = nxECommerceCommunityService.queryCommunityByCommerceId(commerceId);
//		NxECommerceEntity nxECommerceEntity = nxECommerceService.queryObject(commerceId);
//		Map<String, Object> map = new HashMap<>();
//		map.put("arr", entities);
//		map.put("commerce", nxECommerceEntity);
//		return R.ok().put("data", map);
//	}



	
}
