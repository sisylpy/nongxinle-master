package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 12-05 18:56
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunityAliasEntity;
import com.nongxinle.service.NxCommunityAliasService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxcommunityalias")
public class NxCommunityAliasController {
	@Autowired
	private NxCommunityAliasService nxCommunityAliasService;


	@RequestMapping(value = "/comDeleteAlias/{id}")
	@ResponseBody
	public R comDeleteAlias(@PathVariable Integer id) {
	    nxCommunityAliasService.delete(id);
	    return R.ok();
	}
	@RequestMapping(value = "/comUpdateAlias", method = RequestMethod.POST)
	@ResponseBody
	public R comUpdateAlias (@RequestBody NxCommunityAliasEntity aliasEntity) {
	    nxCommunityAliasService.update(aliasEntity);
	    return R.ok();
	}


	@RequestMapping(value = "/comSaveAlias", method = RequestMethod.POST)
	@ResponseBody
	public R comSaveAlias (@RequestBody NxCommunityAliasEntity aliasEntity ) {
	    nxCommunityAliasService.save(aliasEntity);
	    return R.ok().put("data", aliasEntity);
	}

	
}
