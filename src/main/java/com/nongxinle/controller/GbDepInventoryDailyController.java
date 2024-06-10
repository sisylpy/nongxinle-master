package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 09-06 15:27
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.GbDepInventoryMonthEntity;
import com.nongxinle.entity.GbDepInventoryWeekEntity;
import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDepInventoryDailyEntity;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeKufang;


@RestController
@RequestMapping("api/gbdepinventorydaily")
public class GbDepInventoryDailyController {
	@Autowired
	private GbDepInventoryDailyService gbDepInventoryDailyService;
	@Autowired
	private GbDepInventoryWeekService gbDepInventoryWeekService;
	@Autowired
	private GbDepInventoryMonthService gbDepInventoryMonthService;
	@Autowired
	private GbDepartmentGoodsStockService gbDepGoodsStockService;
	@Autowired
	private GbDepartmentService gbDepartmentService;

	@Autowired
	private GbDepartmentGoodsStockRecordService gbDepGoodsStockRecordService;



	@RequestMapping(value = "/mendianGetDailyInventory/{depFatherId}")
	@ResponseBody
	public R mendianGetDailyInventory (@PathVariable Integer depFatherId  ) {

		Map<String, Object> map8 = new HashMap<>();

		double subtotal = 0.0;
		double wasteTotal = 0.0;
		double lossTotal = 0.0;
		double returnTotal = 0.0;

		//日盘库详细
		Map<String, Object> map = new HashMap<>();
		map.put("depFatherId", depFatherId);
		map.put("equalStatus", 0);
	   List<GbDepInventoryDailyEntity> inventoryDailyEntities =  gbDepInventoryDailyService.queryDepDailyList(map);
	   if(inventoryDailyEntities.size() > 0){
	    Double riTotal =	gbDepInventoryDailyService.queryInventoryDailyTotal(map);
	    Double riWasteTotal =	gbDepInventoryDailyService.queryInventoryDailyWasteTotal(map);
	    Double riLossTotal =	gbDepInventoryDailyService.queryInventoryDailyLossTotal(map);
	    Double riReturnTotal =	gbDepInventoryDailyService.queryInventoryDailyReturnTotal(map);
		   subtotal = subtotal + riTotal;
		   wasteTotal = wasteTotal + riWasteTotal;
		   lossTotal = lossTotal + riLossTotal;
		   returnTotal = returnTotal + riReturnTotal;
	   }

	   //周盘库详细
		Map<String, Object> map1 = new HashMap<>();
		map1.put("depFatherId", depFatherId);
		map1.put("equalStatus", 0);
		List<GbDepInventoryWeekEntity> inventoryDailyEntities1 =  gbDepInventoryWeekService.queryDepWeekList(map1);
		if(inventoryDailyEntities1.size() > 0){
			Double zhouTotal =	gbDepInventoryWeekService.queryInventoryWeekTotal(map);
			Double zhouWasteTotal =	gbDepInventoryWeekService.queryInventoryWeekWasteTotal(map);
			Double zhouLossTotal =	gbDepInventoryWeekService.queryInventoryWeekLossTotal(map);
			Double zhouRetrunTotal =	gbDepInventoryWeekService.queryInventoryWeekReturnTotal(map);
			subtotal = subtotal + zhouTotal;
			wasteTotal = wasteTotal + zhouWasteTotal;
			lossTotal = lossTotal + zhouLossTotal;
			returnTotal = returnTotal + zhouRetrunTotal;
		}

		//月盘库详细
		Map<String, Object> map2 = new HashMap<>();
		map2.put("depFatherId", depFatherId);
		map2.put("equalStatus", 0);
		List<GbDepInventoryMonthEntity> inventoryDailyEntities2 =  gbDepInventoryMonthService.queryDepMonthList(map2);
		System.out.println(inventoryDailyEntities2);

		if(inventoryDailyEntities2.size() > 0){
			Double yueTotal =	gbDepInventoryMonthService.queryInventoryMonthTotal(map);
			Double yueWasteTotal =	gbDepInventoryMonthService.queryInventoryMonthWasteTotal(map);
			Double yueLossTotal =	gbDepInventoryMonthService.queryInventoryMonthLossTotal(map);
			Double yueReturnTotal =	gbDepInventoryMonthService.queryInventoryMonthReturnTotal(map);
			System.out.println(yueTotal + "yueTotalyueTotal");
			subtotal = subtotal + yueTotal;
			wasteTotal = wasteTotal + yueWasteTotal;
			lossTotal = lossTotal + yueLossTotal;
			returnTotal = returnTotal + yueReturnTotal;
		}

		String formatTotal = String.format("%.2f", subtotal);
		String formatLossTotal = String.format("%.2f", wasteTotal);
		String formatWasteTotal = String.format("%.2f", lossTotal);
		String formatReturnTotal = String.format("%.2f", returnTotal);

		//kucun
		String stockTotal = "0";
		Map<String, Object> map111 = new HashMap<>();
		map111.put("depFatherId",depFatherId );
		map111.put("equalStatus", 0);
		Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map111);
		if(integer > 0){
			Map<String, Object> map11 = new HashMap<>();
			map11.put("depFatherId",depFatherId );
			map11.put("equalStatus", 0);
			Double aDouble = gbDepGoodsStockService.queryDepGoodsSubtotal(map11);
			stockTotal = String.format("%.2f", aDouble);
		}


		map8.put("ri", inventoryDailyEntities);
//		map8.put("week", inventoryDailyEntities1);
//		map8.put("month", inventoryDailyEntities2);getIssueDepartmentBill
		map8.put("stockTotal", stockTotal);
		map8.put("costTotal", formatTotal);
		map8.put("wasteTotal", formatLossTotal);
		map8.put("lossTotal", formatWasteTotal);
		map8.put("returnTotal", formatReturnTotal);

		//判断如果是库房，增加库房独有的统计内容：
		GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depFatherId);
		if(departmentEntity.getGbDepartmentType().equals(getGbDepartmentTypeKufang())){
			//chuku
			//new Stock out金额-
			Double outTotal = 0.0;
			String formatOutSubtotal = "0.0";
			String formatwaitOutSubtotal = "0.0";

			Map<String, Object> map145 = new HashMap<>();
			map145.put("fromDepId", depFatherId);
			map145.put("equalStatus",0);
			Integer stockAmount55 = gbDepGoodsStockService.queryGoodsStockCount(map145);
			if(stockAmount55 > 0){
				//出库金额
				Map<String, Object> map42 = new HashMap<>();
				map42.put("fromDepId",depFatherId);
				map42.put("equalStatus", 0);
				Double outNewTotal =  gbDepGoodsStockService.queryDepGoodsSubtotal(map42);
				outTotal = outTotal + outNewTotal;
			}

			//old Stock out金额 goods_record table status == 0
			Map<String, Object> map1451 = new HashMap<>();
			map1451.put("fromDepId", depFatherId);
			map1451.put("equalStatus",0);
			Integer stockPriceAmount55 = gbDepGoodsStockRecordService.queryGoodsStockRecordCount(map1451);
			if(stockPriceAmount55 > 0){
				//出库金额
				Map<String, Object> map42 = new HashMap<>();
				map42.put("fromDepId",depFatherId);
				map42.put("equalStatus", 0);
				Double outOldTotal =  gbDepGoodsStockRecordService.queryGoodsStockRecordSubtotal(map42);
				outTotal = outOldTotal + outTotal;
			}

			formatOutSubtotal = String.format("%.2f", outTotal);

			//wait out
			Map<String, Object> map1446 = new HashMap<>();
			map1446.put("fromDepId", depFatherId);
			map1446.put("status",0);
			Integer stockAmount16 = gbDepGoodsStockService.queryGoodsStockCount(map1446);
			if(stockAmount16 > 0){
				//正在出库，门店未收货的金额
				Map<String, Object> map4 = new HashMap<>();
				map4.put("fromDepId",depFatherId);
				map4.put("status", 0);
				Double workingTotal =  gbDepGoodsStockService.queryDepGoodsSubtotal(map4);
				formatwaitOutSubtotal = String.format("%.2f", workingTotal);
			}

			map8.put("out", formatOutSubtotal);
			map8.put("waitOut", formatwaitOutSubtotal);

		}

		return R.ok().put("data", map8);
	}






	

	
}
