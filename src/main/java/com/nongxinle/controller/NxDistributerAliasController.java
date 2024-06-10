package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 10-07 09:12
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.service.NxDistributerGoodsService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDistributerAliasEntity;
import com.nongxinle.service.NxDistributerAliasService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxdistributeralias")
public class NxDistributerAliasController {
	@Autowired
	private NxDistributerAliasService nxDistributerAliasService;
	@Autowired
	private NxDistributerGoodsService nxDistributerGoodsService;


	@RequestMapping(value = "/disDeleteAlias/{id}")
	@ResponseBody
	public R disDeleteAlias(@PathVariable Integer id) {
	    nxDistributerAliasService.delete(id);
	    return R.ok();
	}
	@RequestMapping(value = "/updateDisAlias", method = RequestMethod.POST)
	@ResponseBody
	public R updateDisAlias (@RequestBody NxDistributerAliasEntity alias) {
	    nxDistributerAliasService.update(alias);
	    return R.ok().put("data", alias);
	}
	
	@RequestMapping(value = "/saveDisAlias", method = RequestMethod.POST)
	@ResponseBody
	public R saveDisAlias (@RequestBody  NxDistributerAliasEntity distributerAliasEntity ) {
	    nxDistributerAliasService.save(distributerAliasEntity);

		NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(distributerAliasEntity.getNxDaDisGoodsId());
		return R.ok().put("data", distributerGoodsEntity);
	}

	
}
