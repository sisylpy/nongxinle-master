package com.nongxinle.controller;

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
import com.nongxinle.entity.NxECommerceCommunityEntity;
import com.nongxinle.service.NxCommunityService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxECommerceEntity;
import com.nongxinle.service.NxECommerceService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxecommerce")
public class NxECommerceController {
	@Autowired
	private NxECommerceService nxECommerceService;
	@Autowired
	private NxCommunityService nxCommunityService;


	@RequestMapping(value = "/getGbCommunity/{id}")
	@ResponseBody
	public R getGbCommunity(@PathVariable Integer id) {

		NxECommerceEntity entity  = nxCommunityService.queryCommunityByECommerceId(id);

		return R.ok().put("data", entity);
	}


	
	@RequestMapping(value = "/gbGetECommerce/{id}")
	@ResponseBody
	public R gbGetECommerce(@PathVariable Integer id) {
		NxECommerceEntity commerceEntity = nxECommerceService.queryGbByGbId(id);
		return R.ok().put("data", commerceEntity);
	}

	@RequestMapping(value = "/registerGbCommerce", method = RequestMethod.POST)
	@ResponseBody
	public R registerGbCommerce (@RequestBody NxECommerceEntity entity) {
	    nxECommerceService.save(entity);
	    return R.ok();
	}
	




	
}
