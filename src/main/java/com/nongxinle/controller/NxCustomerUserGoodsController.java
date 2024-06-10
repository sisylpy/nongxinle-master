package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCustomerUserGoodsEntity;
import com.nongxinle.service.NxCustomerUserGoodsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxcustomerusergoods")
public class NxCustomerUserGoodsController {

	@Autowired
	private NxCustomerUserGoodsService nxCustomerUserGoodsService;



	 @RequestMapping(value = "/saveMyLove", method = RequestMethod.POST)
	  @ResponseBody
	  public R saveMyLove (@RequestBody NxCustomerUserGoodsEntity customerGoods) {
//		 Integer nxCugUserId = customerGoods.getNxCugUserId();
//		 NxCustomerUserGoodsEntity entity =  nxCustomerUserGoodsService.queryLove(nxCugUserId);
//		 if(entity != null){
//			 nxCustomerUserGoodsService.
//			 return R.ok();
//		 }else {
//			 nxCustomerUserGoodsService.
//			 return R.ok();
//		 }


		 return R.ok();

	  }


	   @RequestMapping(value = "/addUserGoods", method = RequestMethod.POST)
	    @ResponseBody
	    public R addUserGoods (@RequestBody NxCustomerUserGoodsEntity userGoodsEntity) {
		   Integer nxCugGoodsId = userGoodsEntity.getNxCugGoodsId();

		   NxCustomerUserGoodsEntity userGoodsEntity1 = nxCustomerUserGoodsService.queryObject(nxCugGoodsId);
		   userGoodsEntity1.setNxCugJoinMyTemplate(1);
		   userGoodsEntity1.setNxCugOrderQuantity(userGoodsEntity.getNxCugOrderQuantity());
		   userGoodsEntity1.setNxCugOrderStandard(userGoodsEntity.getNxCugOrderStandard());
		   nxCustomerUserGoodsService.update(userGoodsEntity1);

		   return R.ok();
	    }

	     @RequestMapping(value = "/getCustomerUserGoods", method = RequestMethod.POST)
	      @ResponseBody
	      public R getCustomerUserGoods (@RequestBody NxCustomerUserGoodsEntity userGoodsEntity) {

			 Map<String, Object> map = new HashMap<>();
			 map.put("nxCugUserId", userGoodsEntity.getNxCugUserId());
			 map.put("nxCugJoinMyTemplate", userGoodsEntity.getNxCugJoinMyTemplate());
			 List<NxCustomerUserGoodsEntity> goodsEntities = nxCustomerUserGoodsService.queryUserGoods(map);

			 return R.ok().put("data", goodsEntities);
	      }

//
//	@RequestMapping(value = "/customerUserQueryToJoin/{customerUserId}")
//	@ResponseBody
//	public R customerUserQueryToJoin(@PathVariable Integer customerUserId) {
//
//
//	    return R.ok().put("data", goodsEntities);
//	}


	




	
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxcustomerusergoods:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<NxCustomerUserGoodsEntity> nxCustomerUserGoodsList = nxCustomerUserGoodsService.queryList(map);
		int total = nxCustomerUserGoodsService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(nxCustomerUserGoodsList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{custUGoodsId}")
	@RequiresPermissions("nxcustomerusergoods:info")
	public R info(@PathVariable("custUGoodsId") Integer custUGoodsId){
		NxCustomerUserGoodsEntity nxCustomerUserGoods = nxCustomerUserGoodsService.queryObject(custUGoodsId);
		
		return R.ok().put("nxCustomerUserGoods", nxCustomerUserGoods);
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("nxcustomerusergoods:save")
	public R save(@RequestBody NxCustomerUserGoodsEntity nxCustomerUserGoods){
		nxCustomerUserGoodsService.save(nxCustomerUserGoods);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxcustomerusergoods:update")
	public R update(@RequestBody NxCustomerUserGoodsEntity nxCustomerUserGoods){
		nxCustomerUserGoodsService.update(nxCustomerUserGoods);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxcustomerusergoods:delete")
	public R delete(@RequestBody Integer[] custUGoodsIds){
		nxCustomerUserGoodsService.deleteBatch(custUGoodsIds);
		
		return R.ok();
	}
	
}
