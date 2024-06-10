package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-18 21:32
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxDistributerStandardEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDistributerStandardEntity;
import com.nongxinle.service.GbDistributerStandardService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/gbdistributerstandard")
public class GbDistributerStandardController {
	@Autowired
	private GbDistributerStandardService gbDisStandardService;
	
	@RequestMapping(value = "/saveGbStandard", method = RequestMethod.POST)
	@ResponseBody
	public R saveGbStandard (@RequestParam("gbDsDisGoodsId") Integer gbDsDisGoodsId,
							 @RequestParam("gbDsStandardName" )String gbDsStandardName) {
		GbDistributerStandardEntity standard = new GbDistributerStandardEntity();
		standard.setGbDsDisGoodsId(gbDsDisGoodsId);
		standard.setGbDsStandardName(gbDsStandardName);
		gbDisStandardService.save(standard);
	    return R.ok();
	}

	@RequestMapping(value = "/gbDisDeleteStandard/{id}")
	@ResponseBody
	public R gbDisDeleteStandard(@PathVariable Integer id) {
		gbDisStandardService.delete(id);
		return R.ok();
	}

	/**
	 * 批发商修改订货单位
	 * @param standard 单位
	 * @return ok
	 */
	@RequestMapping(value = "/gbDisUpdateStandard", method = RequestMethod.POST)
	@ResponseBody
	public R gbDisUpdateStandard(@RequestBody GbDistributerStandardEntity standard) {
		System.out.println("update");
		gbDisStandardService.update(standard);
		return R.ok().put("data", standard);
	}

	/**
	 * 批发商添加订货规格
	 * @param standard 订货规格
	 * @return 规格
	 */
	@RequestMapping(value = "/gbDisSaveStandard", method = RequestMethod.POST)
	@ResponseBody
	public R gbDisSaveStandard(@RequestBody GbDistributerStandardEntity standard) {
		System.out.println("abc");
		gbDisStandardService.save(standard);
		return R.ok().put("data", standard);
	}



}
