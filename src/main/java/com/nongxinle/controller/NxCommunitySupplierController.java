package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 10-15 18:45
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.GbDistributerSupplierEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunitySupplierEntity;
import com.nongxinle.service.NxCommunitySupplierService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxcommunitysupplier")
public class NxCommunitySupplierController {
	@Autowired
	private NxCommunitySupplierService nxCommunitySupplierService;



	@RequestMapping(value = "/saveNxCommunitySupplier", method = RequestMethod.POST)
	@ResponseBody
	public R saveNxCommunitySupplier(@RequestBody NxCommunitySupplierEntity suppler) {
		nxCommunitySupplierService.save(suppler);
		return R.ok();
	}



	@RequestMapping(value = "/communityGetSupplier/{comId}")
	@ResponseBody
	public R communityGetSupplier(@PathVariable Integer comId) {
		System.out.println("dfadfasdf");
		Map<String, Object> map = new HashMap<>();
		map.put("comId", comId);
		List<NxCommunitySupplierEntity> supplierEntities = nxCommunitySupplierService.queryCommunitySupplierByParams(map);
		return R.ok().put("data", supplierEntities);
	}

	
}
