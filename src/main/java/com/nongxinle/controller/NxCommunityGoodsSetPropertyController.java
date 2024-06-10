package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 04-06 00:18
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxCommunityGoodsSetItemEntity;
import com.nongxinle.service.NxCommunityGoodsSetItemService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunityGoodsSetPropertyEntity;
import com.nongxinle.service.NxCommunityGoodsSetPropertyService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxcommunitygoodssetproperty")
public class NxCommunityGoodsSetPropertyController {
	@Autowired
	private NxCommunityGoodsSetPropertyService nxCgSetPropertyService;

	@Autowired
	private NxCommunityGoodsSetItemService nxCommunityGoodsSetItemService;


	@RequestMapping(value = "/delCgSetProperty/{id}")
	@ResponseBody
	public R delCgSetProperty (@PathVariable Integer id) {

		Map<String, Object> map = new HashMap<>();
		map.put("propertyId", id);
		List<NxCommunityGoodsSetItemEntity> setItemEntities =  nxCommunityGoodsSetItemService.queryCgGoodsSetListByParams(map);
		if(setItemEntities.size() > 0){
			for(NxCommunityGoodsSetItemEntity itemEntity: setItemEntities){
				nxCommunityGoodsSetItemService.delete(itemEntity.getNxCommunityGoodsSetItemId());
			}
		}
		nxCgSetPropertyService.delete(id);
		return R.ok();
	}


	@RequestMapping(value = "/saveCgSetProperty", method = RequestMethod.POST)
	@ResponseBody
	public R saveCgSetProperty (@RequestBody NxCommunityGoodsSetPropertyEntity property) {
	    nxCgSetPropertyService.save(property);
	    return R.ok();
	}
	@RequestMapping(value = "/updateCgSetProperty", method = RequestMethod.POST)
	@ResponseBody
	public R updateCgSetProperty (@RequestBody NxCommunityGoodsSetPropertyEntity property) {
		nxCgSetPropertyService.update(property);
		return R.ok();
	}

	
}
