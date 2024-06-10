package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 03-27 21:55
 */

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;


@RestController
@RequestMapping("api/gbdepfood")
public class GbDepFoodController {
	@Autowired
	private GbDepFoodService gbDepFoodService;
	@Autowired
	private GbDepFoodSalesService gbDepFoodSalesService;
	@Autowired
	private GbDistributerFoodGoodsService gbDistributerFoodGoodsService;
	@Autowired
	private GbDepFoodGoodsSalesService gbDepFoodGoodsSalesService;
	@Autowired
	private GbDepDisGoodsSettleService gbDepDisGoodsSettleService;

	@RequestMapping(value = "/saveDepFoodSales", method = RequestMethod.POST)
	@ResponseBody
	public R saveDepFoodSales (@RequestBody  List<GbDepFoodEntity> depFoodEntities) {
		BigDecimal salesTotal = new BigDecimal(0);
		for (GbDepFoodEntity food : depFoodEntities) {
			GbDepFoodSalesEntity salesEntity = new GbDepFoodSalesEntity();
			salesEntity.setGbDfsAmount(food.getGbDfFoodSalesAmount());
			salesEntity.setGbDfsFoodId(food.getGbDfFoodId());
			salesEntity.setGbDfsDepFatherId(food.getGbDfDepFatherId());
			salesEntity.setGbDfsDepId(food.getGbDfDepId());
			salesEntity.setGbDfsFullDate(formatFullTime());
			salesEntity.setGbDfsMonth(formatWhatMonth(0));
			salesEntity.setGbDfsYear(formatWhatYear(0));
			salesEntity.setGbDfsSettleId(-1);
			BigDecimal gbDfFoodPriceDecimal = new BigDecimal(food.getGbDfFoodPrice());
			BigDecimal gbDfFoodSalesAmount = new BigDecimal(food.getGbDfFoodSalesAmount()) ;
			BigDecimal decimal = gbDfFoodPriceDecimal.multiply(gbDfFoodSalesAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
			salesTotal.add(decimal);
			salesEntity.setGbDfsSubtotal(decimal.toString());
			salesEntity.setGbDfsUserId(food.getGbDfFoodSalesUserId());
			gbDepFoodSalesService.save(salesEntity);

			Integer gbDepFoodId = food.getGbDfFoodId();

			Map<String, Object> map = new HashMap<>();
			map.put("foodId", gbDepFoodId);
			List<GbDistributerFoodGoodsEntity> entities = gbDistributerFoodGoodsService.queryFoodGoodsByParams(map);
			for (GbDistributerFoodGoodsEntity foodGoods : entities) {
				BigDecimal goodsAmountDecimal = new BigDecimal(foodGoods.getGbDfoodgGoodsAmount());
				BigDecimal salesDecimal = new BigDecimal(food.getGbDfFoodSalesAmount());
				BigDecimal useAmount = goodsAmountDecimal.multiply(salesDecimal).setScale(2,BigDecimal.ROUND_HALF_UP);
				GbDepFoodGoodsSalesEntity foodGoodsSalesEntity = new GbDepFoodGoodsSalesEntity();
				foodGoodsSalesEntity.setGbDfgsDepFatherId(food.getGbDfDepFatherId());
				foodGoodsSalesEntity.setGbDfgsDepId(food.getGbDfDepId());
				foodGoodsSalesEntity.setGbDfgsFullDate(formatFullTime());
				foodGoodsSalesEntity.setGbDfgsGoodsAmount(useAmount.toString());
				foodGoodsSalesEntity.setGbDfgsMonth(formatWhatMonth(0));
				foodGoodsSalesEntity.setGbDfgsSettleId(-1);
				foodGoodsSalesEntity.setGbDfgsFoodGoodsId(food.getGbDepFoodId());
				foodGoodsSalesEntity.setGbDfgsFoodSalesId(salesEntity.getGbDepFoodSalesId());
				foodGoodsSalesEntity.setGbDfgsDisGoodsId(foodGoods.getGbDfoodgDisGoodsId());
				gbDepFoodGoodsSalesService.save(foodGoodsSalesEntity);

				//
				//1.1.1
				Map<String, Object> mapGoodsSettle = new HashMap<>();
				mapGoodsSettle.put("depId", food.getGbDfDepId());
				mapGoodsSettle.put("month", formatWhatMonth(0));
				mapGoodsSettle.put("disGoodsId", foodGoods.getGbDfoodgDisGoodsId());
				BigDecimal useWeight = new BigDecimal(useAmount.toString());
				GbDepDisGoodsSettleEntity settleEntity = gbDepDisGoodsSettleService.queryDisGoodsSettleByParams(mapGoodsSettle);
				if(settleEntity != null){
					BigDecimal settleOldWeight = new BigDecimal(settleEntity.getGbDdgsSalesAmount());
					BigDecimal settleNewWeight = useWeight.add(settleOldWeight).setScale(2,BigDecimal.ROUND_HALF_UP);
					settleEntity.setGbDdgsSalesAmount(settleNewWeight.toString());
					gbDepDisGoodsSettleService.update(settleEntity);
				}else{
					GbDepDisGoodsSettleEntity newGoodsSettleEntity = new GbDepDisGoodsSettleEntity();
					newGoodsSettleEntity.setGbDdgsSalesAmount(useWeight.toString());
					newGoodsSettleEntity.setGbDdgsSettleId(-1);
					newGoodsSettleEntity.setGbDdgsDfgGoodsFatherId(foodGoods.getDistributerGoodsEntity().getGbDgDfgGoodsFatherId());
					newGoodsSettleEntity.setGbDdgsDisControlFresh(foodGoods.getDistributerGoodsEntity().getGbDgControlFresh());
					newGoodsSettleEntity.setGbDdgsDisControlPrice(foodGoods.getDistributerGoodsEntity().getGbDgDfgGoodsFatherId());
					newGoodsSettleEntity.setGbDdgsDisFreshWarnHour(foodGoods.getDistributerGoodsEntity().getGbDgFreshWarnHour());
					newGoodsSettleEntity.setGbDdgsDisFreshWasteHour(foodGoods.getDistributerGoodsEntity().getGbDgFreshWasteHour());
					newGoodsSettleEntity.setGbDdgsDisGoodsPrice(foodGoods.getDistributerGoodsEntity().getGbDgGoodsPrice());
					newGoodsSettleEntity.setGbDdgsDisGoodsLowestPrice(foodGoods.getDistributerGoodsEntity().getGbDgGoodsLowestPrice());
					newGoodsSettleEntity.setGbDdgsDisGoodsHighestPrice(foodGoods.getDistributerGoodsEntity().getGbDgGoodsHighestPrice());
					newGoodsSettleEntity.setGbDdgsDisGoodsId(foodGoods.getDistributerGoodsEntity().getGbDistributerGoodsId());
					newGoodsSettleEntity.setGbDdgsDisGoodsInventoryDepId(foodGoods.getDistributerGoodsEntity().getGbDgGbDepartmentId());
					newGoodsSettleEntity.setGbDdgsDisGoodsInventoryType(foodGoods.getDistributerGoodsEntity().getGbDgGoodsInventoryType());
					newGoodsSettleEntity.setGbDdgsDisGoodsName(foodGoods.getDistributerGoodsEntity().getGbDgGoodsName());
					newGoodsSettleEntity.setGbDdgsDisGoodsStandardname(foodGoods.getDistributerGoodsEntity().getGbDgGoodsStandardname());
					newGoodsSettleEntity.setGbDdgsDisGoodsStandardWeight(foodGoods.getDistributerGoodsEntity().getGbDgGoodsStandardWeight());
					newGoodsSettleEntity.setGbDdgsDisGoodsType(foodGoods.getDistributerGoodsEntity().getGbDgGoodsType());
					newGoodsSettleEntity.setGbDdgsSettleDepartmentFatherId(food.getGbDfDepFatherId());
					newGoodsSettleEntity.setGbDdgsSettleDepartmentId(food.getGbDfDepId());
					System.out.println("zenmhuishi==" + foodGoods.getGbDfoodgGbDistributerId());
					newGoodsSettleEntity.setGbDdgsDistributerId(foodGoods.getGbDfoodgGbDistributerId());
					newGoodsSettleEntity.setGbDdgsSettleMonth(formatWhatMonth(0));
					newGoodsSettleEntity.setGbDdgsSettleYear(formatWhatYear(0));
					newGoodsSettleEntity.setGbDdgsSettleAmount("0");
					newGoodsSettleEntity.setGbDdgsSettleSubtotal("0");
					newGoodsSettleEntity.setGbDdgsStatus(0);
					gbDepDisGoodsSettleService.save(newGoodsSettleEntity);

				}
			}
		}

		return R.ok();
	}

	@RequestMapping(value = "/depGetFoodToInputSales", method = RequestMethod.POST)
	@ResponseBody
	public R depGetFoodToInputSales (Integer depFatherId, Integer depId) {
		Map<String, Object> map = new HashMap<>();
		map.put("depFatherId", depFatherId);
		if(depId != -1){
			map.put("depId", depId);
		}
		List<GbDepFoodEntity> depFoodEntities =  gbDepFoodService.queryDepFoodByParamsWithoutFather(map);
		return R.ok().put("data", depFoodEntities);
	}

	@RequestMapping(value = "/depGetDepFood", method = RequestMethod.POST)
	@ResponseBody
	public R depGetDepFood (Integer depFatherId, Integer depId) {
		Map<String, Object> map = new HashMap<>();
		map.put("depFatherId", depFatherId);
		if(depId != -1){
			map.put("depId", depId);
		}
		System.out.println("ddd");
		List<GbDepFoodEntity> depFoodEntities =  gbDepFoodService.queryDepFoodByParams(map);
		return R.ok().put("data", depFoodEntities);
	}


	@RequestMapping(value = "/cancleDownloadFood", method = RequestMethod.POST)
	@ResponseBody
	public R cancleDownloadFood (@RequestBody GbDepFoodEntity  gbDepFood) {
		gbDepFoodService.delete(gbDepFood.getGbDepFoodId());
	    return R.ok();
	}

	@RequestMapping(value = "/downLoadDepFood", method = RequestMethod.POST)
	@ResponseBody
	public R downLoadDepFood (Integer depId, Integer depFatherId, Integer foodId, String price) {
		GbDepFoodEntity depFoodEntity = new GbDepFoodEntity();
		depFoodEntity.setGbDfFoodId(foodId);
		depFoodEntity.setGbDfDepFatherId(depFatherId);
		depFoodEntity.setGbDfDepId(depId);
		depFoodEntity.setGbDfFoodPrice(price);
		depFoodEntity.setGbDfStatus(1);
		gbDepFoodService.save(depFoodEntity);

	    return R.ok();
	}




	
}
