package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 02-24 09:47
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxDistributerEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDistributerCommunityEntity;
import com.nongxinle.service.NxDistributerCommunityService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxdistributercommunity")
public class NxDistributerCommunityController {
	@Autowired
	private NxDistributerCommunityService dcService;

	@RequestMapping(value = "/comGetDistributer/{comId}")
	@ResponseBody
	public R comGetDistributer(@PathVariable Integer comId) {
	    List<NxDistributerEntity> distributerEntities =  dcService.comQueryDistributer(comId);
	    return R.ok().put("data", distributerEntities);
	}

	@RequestMapping(value = "/saveCommunityDistributer", method = RequestMethod.POST)
	@ResponseBody
	public R saveCommunityDistributer (@RequestBody NxDistributerCommunityEntity   entity) {
	    dcService.save(entity);
	    return R.ok();
	}



	
}
