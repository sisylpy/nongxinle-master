package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 07-30 18:51
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxAliasEntity;
import com.nongxinle.service.NxAliasService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxalias")
public class NxAliasController {
	@Autowired
	private NxAliasService nxAliasService;

	@RequestMapping(value = "/saveAlias", method = RequestMethod.POST)
	@ResponseBody
	public R saveAlias (@RequestBody NxAliasEntity alias) {
	    nxAliasService.save(alias);
	    return R.ok().put("data",alias);
	}

	@RequestMapping(value = "/updateAlias", method = RequestMethod.POST)
	@ResponseBody
	public R updateAlias (@RequestBody NxAliasEntity alias) {
	    nxAliasService.update(alias);
	    return R.ok();
	}

	@RequestMapping(value = "/deleteAlias/{id}")
	@ResponseBody
	public R deleteAlias(@PathVariable Integer id) {
	    nxAliasService.delete(id);
	    return R.ok();
	}

	
}
