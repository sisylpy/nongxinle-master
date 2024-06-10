package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 04-30 06:45
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunityStandardEntity;
import com.nongxinle.service.NxCommunityStandardService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxcommunitystandard")
public class NxCommunityStandardController {
	@Autowired
	private NxCommunityStandardService nxCommunityStandardService;

	@RequestMapping(value = "/comDeleteStandard/{id}")
	@ResponseBody
	public R comDeleteStandard(@PathVariable Integer id) {
	    nxCommunityStandardService.delete(id);
	    return R.ok();
	}


	@RequestMapping(value = "/comUpdateStandard", method = RequestMethod.POST)
	@ResponseBody
	public R comUpdateStandard (@RequestBody NxCommunityStandardEntity standardEntity) {
	    nxCommunityStandardService.update(standardEntity);
	    return R.ok();
	}


	@RequestMapping(value = "/comSaveStandard", method = RequestMethod.POST)
	@ResponseBody
	public R comSaveStandard (@RequestBody NxCommunityStandardEntity standard  ) {
		nxCommunityStandardService.save(standard);
	    return R.ok().put("data", standard);
	}
	



	
}
