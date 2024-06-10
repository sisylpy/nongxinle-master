package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 04-14 12:45
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxOrderTemplateItemEntity;
import com.nongxinle.service.NxOrderTemplateItemService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxordertemplateitem")
public class NxOrderTemplateItemController {
	@Autowired
	private NxOrderTemplateItemService nxOrderTemplateItemService;
	
	 @RequestMapping(value = "/addUserGoodsItem", method = RequestMethod.POST)
	  @ResponseBody
	  public R addUserGoodsItem (@RequestBody NxOrderTemplateItemEntity itemEntity) {

	    
	    return R.ok();
	  }
	 


	@RequestMapping(value = "/getCustomerUserItems/{nxCustomerUserId}")
	@ResponseBody
	public R getCustomerUserItems(@PathVariable Integer nxCustomerUserId) {
		System.out.println(nxCustomerUserId);

	       List<NxOrderTemplateItemEntity> itemEntities = nxOrderTemplateItemService.queryCustomerUserItems(nxCustomerUserId);

	    return R.ok().put("data", itemEntities);
	}

	@RequestMapping(value = "/delateItem/{id}")
	@ResponseBody
	public R delateItem(@PathVariable Integer id) {
		nxOrderTemplateItemService.delete(id);


	    return R.ok();
	}


	 @RequestMapping(value = "/saveEditItems", method = RequestMethod.POST)
	  @ResponseBody
	  public R saveEditItems (@RequestBody List<NxOrderTemplateItemEntity> arr) {
		 System.out.println("haha");
		 System.out.println(arr);
		 for (NxOrderTemplateItemEntity item : arr) {
			 nxOrderTemplateItemService.update(item);
		 }
	    return R.ok();
	  }


	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxordertemplateitem:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<NxOrderTemplateItemEntity> nxOrderTemplateItemList = nxOrderTemplateItemService.queryList(map);
		int total = nxOrderTemplateItemService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(nxOrderTemplateItemList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{nxOtItemId}")
	@RequiresPermissions("nxordertemplateitem:info")
	public R info(@PathVariable("nxOtItemId") Integer nxOtItemId){
		NxOrderTemplateItemEntity nxOrderTemplateItem = nxOrderTemplateItemService.queryObject(nxOtItemId);
		
		return R.ok().put("nxOrderTemplateItem", nxOrderTemplateItem);
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
//	@RequiresPermissions("nxordertemplateitem:save")
	public R save(@RequestBody NxOrderTemplateItemEntity nxOrderTemplateItem){
		nxOrderTemplateItemService.save(nxOrderTemplateItem);
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxordertemplateitem:update")
	public R update(@RequestBody NxOrderTemplateItemEntity nxOrderTemplateItem){
		nxOrderTemplateItemService.update(nxOrderTemplateItem);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxordertemplateitem:delete")
	public R delete(@RequestBody Integer[] nxOtItemIds){
		nxOrderTemplateItemService.deleteBatch(nxOtItemIds);
		
		return R.ok();
	}
	
}
