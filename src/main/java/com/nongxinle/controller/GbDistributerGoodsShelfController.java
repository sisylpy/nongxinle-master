package com.nongxinle.controller;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 08-19 09:57
 */

import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;


@RestController
@RequestMapping("api/gbdistributergoodsshelf")
public class GbDistributerGoodsShelfController {
	@Autowired
	private GbDistributerGoodsShelfService gbDisGoodsShelfService;
	@Autowired
	private GbDistributerGoodsShelfGoodsService gbDisGoodsShelfGoodsService;
	@Autowired
	private GbDepartmentOrdersService gbDepartmentOrdersService;
	@Autowired
	private GbDistributerPurchaseGoodsService gbDpgService;
	@Autowired
	private GbDistributerGoodsService gbDistributerGoodsService;


	@RequestMapping(value = "/updateShelf", method = RequestMethod.POST)
	@ResponseBody
	public R updateShelf (@RequestBody List<GbDistributerGoodsShelfEntity> shelfList) {

		for (int i = 0; i < shelfList.size(); i++){
			GbDistributerGoodsShelfEntity shelfEntity = shelfList.get(i);
			shelfEntity.setGbDistributerGoodsShelfSort(i + 1);
			gbDisGoodsShelfService.update(shelfEntity);
		}
	    return R.ok();
	}

	@RequestMapping(value = "/stockGetShelfList/{depId}")
	@ResponseBody
	public R stockGetShelfList(@PathVariable Integer depId) {

		Map<String, Object> map = new HashMap<>();
		map.put("depId", depId);
		List<GbDistributerGoodsShelfEntity> shelfEntities =  gbDisGoodsShelfService.queryShelfList(map);
	    return R.ok().put("data", shelfEntities);
	}


	@RequestMapping(value = "/depGetOutStockOrders")
	@ResponseBody
	public R depGetOutStockOrders(Integer depId, Integer toDepId) {
		Map<String, Object> map = new HashMap<>();
		map.put("depFatherId", depId);
		map.put("toDepId", toDepId);
		map.put("status", 3);
		map.put("dayuStatus", -1);
		map.put("isSelf", 0);
		System.out.println("buyaodieieieie" + map);
		List<GbDistributerGoodsShelfEntity> shelfEntities = gbDisGoodsShelfService.queryShelfGoodsWithPurOrder(map);

		List<GbDistributerGoodsShelfGoodsEntity> shelfGoodsEntities = new ArrayList<>();
		List<GbDistributerGoodsShelfEntity> shelfEntityList = new ArrayList<>();
		GbDistributerGoodsShelfEntity shelfGoodsEntity = new GbDistributerGoodsShelfEntity();
		for (GbDistributerGoodsShelfEntity shelf : shelfEntities) {
			Integer gbDistributerGoodsShelfId = shelf.getGbDistributerGoodsShelfId();
			if(gbDistributerGoodsShelfId == null){
				shelfGoodsEntities.add(shelf.getGbDisGoodsShelfGoodsEntities().get(0));
			}else{
				shelfEntityList.add(shelf);
			}
		}
		if(shelfGoodsEntities.size() > 0){
			shelfGoodsEntity.setGbDisGoodsShelfGoodsEntities(shelfGoodsEntities);
			shelfEntityList.add(shelfGoodsEntity);
		}

		return R.ok().put("data", shelfEntityList);
	}



	@RequestMapping(value = "/getWeightOutDepOrder",method = RequestMethod.POST)
	@ResponseBody
	public R getWeightOutDepOrder( Integer weightId,Integer toDepId ) {
		Map<String, Object> map = new HashMap<>();
		map.put("weightId", weightId);
		map.put("toDepId", toDepId);
		System.out.println("mapappa" + map);
		List<GbDistributerGoodsShelfEntity> shelfEntities = gbDisGoodsShelfService.queryShelfGoodsWithPurOrder(map);

		List<GbDistributerGoodsShelfGoodsEntity> shelfGoodsEntities = new ArrayList<>();
		List<GbDistributerGoodsShelfEntity> shelfEntityList = new ArrayList<>();
		GbDistributerGoodsShelfEntity shelfGoodsEntity = new GbDistributerGoodsShelfEntity();
		for (GbDistributerGoodsShelfEntity shelf : shelfEntities) {
			Integer gbDistributerGoodsShelfId = shelf.getGbDistributerGoodsShelfId();
			if(gbDistributerGoodsShelfId == null){
				shelfGoodsEntities.add(shelf.getGbDisGoodsShelfGoodsEntities().get(0));
			}else{
				shelfEntityList.add(shelf);
			}
		}
		if(shelfGoodsEntities.size() > 0){
			shelfGoodsEntity.setGbDisGoodsShelfGoodsEntities(shelfGoodsEntities);
			shelfEntityList.add(shelfGoodsEntity);
		}

		return R.ok().put("data", shelfEntityList);



	}




	@RequestMapping(value = "/getWeightDepDisGoods",method = RequestMethod.POST)
	@ResponseBody
	public R getWeightDepDisGoods( Integer weightId,Integer isSelf, Integer toDepId ) {
		Map<String, Object> map = new HashMap<>();
		map.put("weightId", weightId);
		if(isSelf == -1){
			List<GbDistributerPurchaseGoodsEntity> disPurGoodsEntities = gbDpgService.queryPurchaseGoodsWithDetailByParams(map);
			return R.ok().put("data", disPurGoodsEntities);
		}
		if(isSelf == 1){
			List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepartmentOrdersService.disGetPrintedWeightApplys(map);
             return R.ok().put("data", fatherGoodsEntities);
		}

        if(isSelf == 0){
			map.put("toDepId", toDepId);
			System.out.println("weicidididiid" + map);
			List<GbDistributerGoodsShelfEntity> shelfEntities = gbDisGoodsShelfService.queryShelfGoodsWithPurOrder(map);
			List<GbDistributerGoodsShelfGoodsEntity> shelfGoodsEntities = new ArrayList<>();
			List<GbDistributerGoodsShelfEntity> shelfEntityList = new ArrayList<>();
			GbDistributerGoodsShelfEntity shelfGoodsEntity = new GbDistributerGoodsShelfEntity();
			for (GbDistributerGoodsShelfEntity shelf : shelfEntities) {
				Integer gbDistributerGoodsShelfId = shelf.getGbDistributerGoodsShelfId();
				if(gbDistributerGoodsShelfId == null){
					shelfGoodsEntities.add(shelf.getGbDisGoodsShelfGoodsEntities().get(0));
				}else{
					shelfEntityList.add(shelf);
				}
			}
			if(shelfGoodsEntities.size() > 0){
				shelfGoodsEntity.setGbDisGoodsShelfGoodsEntities(shelfGoodsEntities);
				shelfEntityList.add(shelfGoodsEntity);
			}

			return R.ok().put("data", shelfEntityList);

		}

        return R.ok();

	}






	@RequestMapping(value = "/getShelfInventoryGoodsStock",method = RequestMethod.POST)
	@ResponseBody
	public R getShelfInventoryGoodsStock(Integer shelfId, Integer depId, String inventoryType) {
		Map<String, Object> map = new HashMap<>();
		map.put("shelfId", shelfId);
		map.put("depId", depId);
		map.put("status", -1);
		if(inventoryType.equals("daily")){
			map.put("inventoryDate", formatWhatDay(0));
			map.put("inventoryType", getDISGoodsInventroyDaily());
		}
		if(inventoryType.equals("week")){
			map.put("week", getWeekOfYear(0));
			map.put("inventoryType", getDISGoodsInventroyWeek());
		}
		if(inventoryType.equals("month")){
			map.put("month", formatWhatMonth(0));
			map.put("inventoryType", getDISGoodsInventroyMonth());
		}
		List<GbDistributerFatherGoodsEntity>  fatherGoodsEntities =  gbDisGoodsShelfService.queryShelfInventoryDepGoodsByParams(map);


		return R.ok().put("data",fatherGoodsEntities);
	}


	@RequestMapping(value = "/shelfGetHaveInventoryGoodsStock", method = RequestMethod.POST)
	@ResponseBody
	public R shelfGetHaveInventoryGoodsStock (Integer shelfId,  Integer depId, String  inventoryType ) {
		Map<String, Object> map = new HashMap<>();
		map.put("shelfId", shelfId);
		map.put("toDepId", depId);
		map.put("dayuRestWeight", 0);
		if(inventoryType.equals("daily")){
			map.put("stopDate", formatWhatDay(1));
			map.put("inventoryType", getDISGoodsInventroyDaily());
		}
		if(inventoryType.equals("week")){
			map.put("equalWeek", getWeekOfYear(0) + 1);
			map.put("inventoryType", getDISGoodsInventroyWeek());
		}
		if(inventoryType.equals("month")){
			String month = "";
			String s = formatWhatMonth(0);
			if(s.equals("12")){
				month = "01";
			}else{
				month =  new BigDecimal(s).add(new BigDecimal(1)).toString();
			}
			map.put("equalMonth", month);
			map.put("inventoryType", getDISGoodsInventroyMonth());
		}

		List<GbDistributerFatherGoodsEntity>  departmentDisGoodsEntities =  gbDisGoodsShelfService.queryShelfInventoryDepGoodsByParams(map);
		System.out.println("have" + departmentDisGoodsEntities.size());
		return R.ok().put("data",departmentDisGoodsEntities);
	}


	@RequestMapping(value = "/stockRoomGetShelfGoodsGb", method = RequestMethod.POST)
	@ResponseBody
	public R stockRoomGetShelfGoodsGb(Integer depId, Integer shelfId) {
		Map<String, Object> map = new HashMap<>();
		map.put("purDepId", depId);
		map.put("buyStatus", 1);
		map.put("shelfId", shelfId);
		List<GbDistributerFatherGoodsEntity> shelfGoodsEntities = gbDisGoodsShelfService.stockRoomGetShelfGoodsGb(map);

		return R.ok().put("data", shelfGoodsEntities);
	}


	@RequestMapping(value = "/getShelfGoodsGb/{shelfId}")
	@ResponseBody
	public R getShelfGoodsGb(@PathVariable Integer shelfId) {
		Map<String, Object> map = new HashMap<>();
		map.put("shelfId", shelfId);
		GbDistributerGoodsShelfEntity shelfEntity =  gbDisGoodsShelfService.queryShelfGoodsByParams(map);
		return R.ok().put("data",shelfEntity);
	}

	@RequestMapping(value = "/disGetShelfsGb/{disId}")
	@ResponseBody
	public R disGetShelfsGb(@PathVariable Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		List<GbDistributerFatherGoodsEntity> shelfEntities = gbDisGoodsShelfService.queryShelfByParamsWithStock(map);
		return R.ok().put("data", shelfEntities);
	}

	@RequestMapping(value = "/stockRoomGetShelfsGb/{depId}")
	@ResponseBody
	public R stockRoomGetShelfsGb(@PathVariable Integer depId) {
		Map<String, Object> map = new HashMap<>();
		map.put("depId", depId);
		System.out.println("depidididdidi" + map);
		List<GbDistributerFatherGoodsEntity> shelfEntities = gbDisGoodsShelfService.queryShelfByParamsWithStock(map);

		Map<String, Object> map22 = new HashMap<>();
		map22.put("toDepId", depId);
		map22.put("buyStatus", 1);
		map22.put("dayuStatus", -1);
		map22.put("status", 3);
		map22.put("isSelf", 0);
		map22.put("isNotSelf", 1);
		Integer amount2 = gbDepartmentOrdersService.queryTotalByParams(map22);

		map22.put("isSelf", 1);
		Integer amount3 = gbDepartmentOrdersService.queryTotalByParams(map22);
		Map<String, Object> map2 = new HashMap<>();
		map2.put("arr",shelfEntities );
		map2.put("isSelfAmount", amount3);
		map2.put("amount", amount2);


		System.out.println("shelflflgoood" +depId );
		List<GbDistributerGoodsEntity> goodsEntities =  gbDistributerGoodsService.queryDisGoodsWithShelfGoods(depId);
		int num = 0;
		List<GbDistributerGoodsEntity> add = new ArrayList<>();
		if(goodsEntities.size() > 0){
			for (GbDistributerGoodsEntity goods : goodsEntities) {
				if(goods.getGbDistributerGoodsShelfGoodsEntity() == null){
					num = num + 1;
					add.add(goods);
				}
			}
		}

		map2.put("shelfAdd", num);
		map2.put("shelfxxx", add);

		return R.ok().put("data", map2);
	}

	@RequestMapping(value = "/updateShelfGb", method = RequestMethod.POST)
	@ResponseBody
	public R updateShelfGb (Integer shelfId, String shelfName) {
		GbDistributerGoodsShelfEntity shelfEntity = gbDisGoodsShelfService.queryObject(shelfId);
		shelfEntity.setGbDistributerGoodsShelfName(shelfName);
		gbDisGoodsShelfService.update(shelfEntity);
		return R.ok();
	}

	@RequestMapping(value = "/saveNewShelfGb", method = RequestMethod.POST)
	@ResponseBody
	public R saveNewShelfGb (@RequestBody GbDistributerGoodsShelfEntity shelf) {
		gbDisGoodsShelfService.save(shelf);

		List<GbDistributerGoodsShelfGoodsEntity> nxDisGoodsShelfGoodsEntities = shelf.getGbDisGoodsShelfGoodsEntities();
		if(nxDisGoodsShelfGoodsEntities.size() > 0){
			for (GbDistributerGoodsShelfGoodsEntity shelfGoods : nxDisGoodsShelfGoodsEntities) {
				shelfGoods.setGbDgsgShelfId(shelf.getGbDistributerGoodsShelfId());
				gbDisGoodsShelfGoodsService.save(shelfGoods);
			}
		}
		return R.ok();
	}

	@RequestMapping(value = "/deleteShelfGb/{shelfId}")
	@ResponseBody
	public R deleteShelfGb(@PathVariable Integer shelfId) {

		//1，修改剩下货架排序
		GbDistributerGoodsShelfEntity delShelfEntity = gbDisGoodsShelfService.queryObject(shelfId);
		Map<String, Object> map1 = new HashMap<>();
		map1.put("depId", delShelfEntity.getGbDistributerGoodsShelfDepId());
		map1.put("dayuSort", delShelfEntity.getGbDistributerGoodsShelfSort());
		List<GbDistributerGoodsShelfEntity> shelfEntities = gbDisGoodsShelfService.queryShelfList(map1);
		for(int i = 0; i < shelfEntities.size(); i++){
			GbDistributerGoodsShelfEntity shelfEntity1 = shelfEntities.get(i);
			shelfEntity1.setGbDistributerGoodsShelfSort(delShelfEntity.getGbDistributerGoodsShelfSort() + i);
			gbDisGoodsShelfService.update(shelfEntity1);
		}

//		//2，删除货架商品
//		Map<String, Object> map = new HashMap<>();
//		map.put("shelfId", shelfId);
//		List<GbDistributerGoodsShelfGoodsEntity> shelfGoodsEntities =  gbDisGoodsShelfGoodsService.queryShelfGoodsByParams(map);
//		for (GbDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsEntities) {
//			gbDisGoodsShelfGoodsService.delete(shelfGoods.getGbDistributerGoodsShelfGoodsId());
//		}

		//3， 删除货架
		gbDisGoodsShelfService.delete(shelfId);
		return R.ok();
	}
	
}
