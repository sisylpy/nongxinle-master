package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 07-24 13:00
 */

import java.util.*;

import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxGoodsEntity;
import com.nongxinle.entity.NxLettersEntity;
import com.nongxinle.service.NxDepartmentDisGoodsService;
import com.nongxinle.service.NxDepartmentOrdersService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDepartmentIndependentGoodsEntity;
import com.nongxinle.service.NxDepartmentIndependentGoodsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.formatWhatDate;
import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.PinYin4jUtils.getHeadStringByString;
import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@RestController
@RequestMapping("api/nxdepartmentindependentgoods")
public class NxDepartmentIndependentGoodsController {
	@Autowired
	private NxDepartmentIndependentGoodsService nxDepIndepenGoodsService;

	@Autowired
	private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;

	@Autowired
	private NxDepartmentOrdersService nxDepartmentOrdersService;


	/**
	 * PURCHASE
	 * 修改自定义商品
	 * @param nxDepartmentIndependentGoods 自定义商品
	 * @return ok
	 */
	@ResponseBody
	@RequestMapping("/update")
	public R update(@RequestBody NxDepartmentIndependentGoodsEntity nxDepartmentIndependentGoods){
		nxDepIndepenGoodsService.update(nxDepartmentIndependentGoods);
		return R.ok();
	}

	/**
	 * PURCHASE
	 * 新添加自定义商品
	 * @param nxDepIndependentGoods 自定义商品
	 * @return ok
	 */
	@ResponseBody
	@RequestMapping("/save")
	public R save(@RequestBody NxDepartmentIndependentGoodsEntity nxDepIndependentGoods){

		String goodsName = nxDepIndependentGoods.getNxDigGoodsName();
		String pinyin = hanziToPinyin(goodsName);
		String headPinyin = getHeadStringByString(goodsName, false, null);
		nxDepIndependentGoods.setNxDigGoodsPinyin(pinyin);
		nxDepIndependentGoods.setNxDigGoodsPy(headPinyin);
		nxDepIndepenGoodsService.save(nxDepIndependentGoods);
		return R.ok();
	}

	/**
	 * PURCHASE
	 * 删除自定义商品
	 * @param id 自定义商品id
	 * @return ok
	 */
	@ResponseBody
	@RequestMapping("/delete/{id}")
	public R delete(@PathVariable  Integer id){
		Map<String, Object> map = new HashMap<>();
		map.put("indGoodsId", id);
		map.put("applyDate", formatWhatDay(-30));
		System.out.println(formatWhatDay(-30) + "kkkkk30303030");
		List<NxDepartmentOrdersEntity>  ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
		if(ordersEntities.size() > 0){
			return R.error(-1,"过一段时间才可删除，近期有订单。");
		}else {
			nxDepIndepenGoodsService.delete(id);
			return R.ok();
		}

	}


	/**
	 * ORDER
	 * 获取自定义商品
	 * @param depId 群id
	 * @return 自定义商品id
	 */
	@ResponseBody
	@RequestMapping(value = "/list/{depId}")
	public R list(@PathVariable Integer depId){
		Map<String, Object> map = new HashMap<>();

		map.put("depId",depId);

		//查询列表数据
		List<NxDepartmentIndependentGoodsEntity> nxDepIndGoodsList = nxDepIndepenGoodsService.queryList(map);
		List<Map<String, Object>> mapList = letterGoods(nxDepIndGoodsList);


		return R.ok().put("data", mapList);
	}



//////////////


//	@RequestMapping(value = "/searchDepDisGoodsAndIndependentGoods", method = RequestMethod.POST)
//	@ResponseBody
//	public R searchDepDisGoodsAndIndependentGoods (Integer depId, String searchStr) {
//		System.out.println(searchStr);
//		//1 DepDisGoods-search
//		Map<String, Object> map = new HashMap<>();
//		map.put("depId", depId);
//		map.put("searchStr", searchStr);
//		List<NxGoodsEntity> depDisGoods = nxDepartmentDisGoodsService.queryDepDisSearchStr(map);
//		System.out.println("depDisGoodsssss====" + depDisGoods);
//
//		//2, depIndependent -search
//		List<NxDepartmentIndependentGoodsEntity> independentGoods = nxDepIndepenGoodsService.querySearchStr(map);
//
//		List<Map<String, Object>> mapList = letterGoods(independentGoods);
//		Map<String, Object>  result = new HashMap<>();
//		result.put("depGoodsArr", depDisGoods);
//		result.put("independentArr", mapList);
//	    return R.ok().put("data", result);
//	}




	private List<Map<String, Object>> letterGoods (List<NxDepartmentIndependentGoodsEntity> nxDepIndGoodsList){
		Set<NxLettersEntity> lettersEntities = new TreeSet<>();
		for (NxDepartmentIndependentGoodsEntity goods:nxDepIndGoodsList) {
			String nxDigGoodsPy = goods.getNxDigGoodsPy();
			String substring = nxDigGoodsPy.substring(0, 1);
			NxLettersEntity lettersEntity = new NxLettersEntity();
			lettersEntity.setLetter(substring);
			lettersEntities.add(lettersEntity);
		}
		System.out.println(lettersEntities + "leelelelle");
		List<Map<String, Object>> result = new ArrayList<>();

		for (NxLettersEntity letters : lettersEntities) {
			String letter = letters.getLetter().toUpperCase();
			Map<String, Object> map1 = new HashMap<>();
			map1.put("letter", letter);
			List<NxDepartmentIndependentGoodsEntity> depIndpenList = new ArrayList<>();
			for (NxDepartmentIndependentGoodsEntity depIndGoods : nxDepIndGoodsList) {

				String nxDigGoodsPy = depIndGoods.getNxDigGoodsPy().substring(0,1).toUpperCase();
				if(nxDigGoodsPy.equals(letter)){
					depIndpenList.add(depIndGoods);

				}
			}
			map1.put("list", depIndpenList);
			result.add(map1);

		}
		return  result;
	}



	
}
