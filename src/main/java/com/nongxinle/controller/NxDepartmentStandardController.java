package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 08-11 22:02
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDepartmentStandardEntity;
import com.nongxinle.service.NxDepartmentStandardService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxdepartmentstandard")
public class NxDepartmentStandardController {
	@Autowired
	private NxDepartmentStandardService nxDepartmentStandardService;

	/**
	 * PURCHASE
	 * 保存群商品的订货规格
	 * @param nxDepartmentStandard 群商品订货规格
	 * @return ok
	 */
	@ResponseBody
	@RequestMapping("/save")
	public R save(@RequestBody NxDepartmentStandardEntity nxDepartmentStandard){
		nxDepartmentStandardService.save(nxDepartmentStandard);
		return R.ok().put("data", nxDepartmentStandard);
	}

	/**
	 * PURCHSE
	 * 修改群商品订货规格
	 * @param nxDepartmentStandard 群规格
	 * @return ok
	 */
	@ResponseBody
	@RequestMapping("/update")
	public R update(@RequestBody NxDepartmentStandardEntity nxDepartmentStandard){
		nxDepartmentStandardService.update(nxDepartmentStandard);
		return R.ok().put("data", nxDepartmentStandard);
	}


	/**
	 * ORDER
	 * 群订货单位
	 * @param depGoodsId 群商品id
	 * @return 订单单位
	 */
	@RequestMapping(value = "/getDepGoodsStandard/{depGoodsId}")
	@ResponseBody
	public R getDepGoodsStandards(@PathVariable Integer depGoodsId) {
	    List<NxDepartmentStandardEntity> standardEntities = nxDepartmentStandardService.queryDepGoodsStandards(depGoodsId);
	    return R.ok().put("data", standardEntities);
	}

	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete/{id}")
	public R delete(@PathVariable Integer id){
		nxDepartmentStandardService.delete(id);
		return R.ok();
	}
	
}
