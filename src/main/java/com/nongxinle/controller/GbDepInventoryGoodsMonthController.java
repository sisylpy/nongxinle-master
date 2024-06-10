package com.nongxinle.controller;

/**
 * @author lpy
 * @date 08-23 14:08
 */

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartGoodsStockReduceTypeLoss;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartGoodsStockReduceTypeProduce;


@RestController
@RequestMapping("api/gbdepinventorygoodsmonth")
public class GbDepInventoryGoodsMonthController {
    @Autowired
    private GbDepInventoryGoodsMonthService gbDepInventoryGoodsMonthService;
    @Autowired
    private GbDepInventoryMonthService gbDepInventoryMonthService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepartmentStockReduceService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;


//	@RequestMapping(value = "/saveDepChangeStockMonth", method = RequestMethod.POST)
//	@ResponseBody
//	public R saveDepChangeStockMonth (@RequestBody GbDepartmentGoodsStockEntity  stock) {
//		//如果库存数量不等于剩余数量，也就是----使用数量不等于0
//		if(!stock.getGbDgsMyProduceWeight().equals(stock.getGbDgsRestWeight()) ){
//			BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight());
//			BigDecimal produceWeight = new BigDecimal(stock.getGbDgsMyProduceWeight());
//			// useWeight：门店使用数量指制作成本；
//			// useWeight：库房使用数量指应该库存与实际库存的差异数量
//			BigDecimal produceWeighData = restWeight.subtract(produceWeight);//剩余数量-最新制作数量
//			BigDecimal price = new BigDecimal(stock.getGbDgsPrice());
//			BigDecimal produceSubtotal = produceWeighData.multiply(price).setScale(1,BigDecimal.ROUND_HALF_UP);
//
//			//1, reduce
//			GbDepartmentGoodsStockReduceEntity  reduceEntity = new GbDepartmentGoodsStockReduceEntity();
//			reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeProduce()); //
//			reduceEntity.setGbDgsrGbDistributerId(stock.getGbDgsGbDistributerId());
//			reduceEntity.setGbDgsrGbDepartmentId(stock.getGbDgsGbDepartmentId());
//			reduceEntity.setGbDgsrGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//			reduceEntity.setGbDgsrGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
//			reduceEntity.setGbDgsrGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
//			reduceEntity.setGbDgsrGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
//			reduceEntity.setGbDgsrGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
//			reduceEntity.setGbDgsrFullTime(formatFullTime());
//			reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReduceWeightUserId());
//			reduceEntity.setGbDgsrDate(formatWhatDay(0));
//			reduceEntity.setGbDgsrWeek(getWeekOfYear(0).toString());
//			reduceEntity.setGbDgsrMonth(formatWhatMonth(0));
////			reduceEntity.setGbDgsrCostWeight(useWeight.toString());
////			reduceEntity.setGbDgsrCostSubtotal(useSubtotal.toString());
//			reduceEntity.setGbDgsrProduceWeight(produceWeight.toString());
//			reduceEntity.setGbDgsrProduceSubtotal(produceSubtotal.toString());
//
//			gbDepartmentStockReduceService.save(reduceEntity);
//
//
//			// 更新depDisGoods
//			subscribeDepDisGoodsTotal(produceWeight, produceSubtotal,stock.getGbDgsGbDepDisGoodsId());
//
//
//			//2， 更新inventoryGoodsMonth表
//			Map<String, Object> map11 = new HashMap<>();
//			map11.put("stockId", stock.getGbDepartmentGoodsStockId());
//			map11.put("month", formatWhatMonth(0));
//			map11.put("depId", stock.getGbDgsGbDepartmentId());
//			List<GbDepInventoryGoodsMonthEntity> depInventoryGoodsMonthEntities = gbDepInventoryGoodsMonthService.queryMonthStockByParams(map11);
//			//如果有批次数据
//			if(depInventoryGoodsMonthEntities.size() > 0){
//				//在最后一条盘库记录添加损耗
//				int index = depInventoryGoodsMonthEntities.size() - 1;
//				GbDepInventoryGoodsMonthEntity goodsMonthEntity = depInventoryGoodsMonthEntities.get(index);
//				BigDecimal dailyWeight = new BigDecimal(goodsMonthEntity.getGbIgmWeight());
//				BigDecimal newWeight = dailyWeight.add(produceWeight);
//				BigDecimal newSubtotal = newWeight.multiply(price).setScale(1,BigDecimal.ROUND_HALF_UP);
//				goodsMonthEntity.setGbIgmProduceWeight(newWeight.toString());
//				goodsMonthEntity.setGbIgmProduceSubtotal(newSubtotal.toString());
//				gbDepInventoryGoodsMonthService.update(goodsMonthEntity);
//
//			}else{
//				//添加新盘库商品记录
//				GbDepInventoryGoodsMonthEntity goodsMonthEntity = new GbDepInventoryGoodsMonthEntity();
//				goodsMonthEntity.setGbIgmDisGoodsId(stock.getGbDgsGbDisGoodsId());
//				goodsMonthEntity.setGbIgmDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
//				goodsMonthEntity.setGbIgmDepartmentId(stock.getGbDgsGbDepartmentId());
//				goodsMonthEntity.setGbIgmDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//				goodsMonthEntity.setGbIgmProduceSubtotal(.toString());
//				goodsMonthEntity.setGbIgmProduceWeight(useWeight.toString());
//				goodsMonthEntity.setGbIgmMonth(formatWhatMonth(0));
//				goodsMonthEntity.setGbIgmYear(formatWhatYear(0));
//				goodsMonthEntity.setGbIgmWasteWeight("0");
//				goodsMonthEntity.setGbIgmWasteSubtotal("0");
//				goodsMonthEntity.setGbIgmLossSubtotal("0");
//				goodsMonthEntity.setGbIgmLossWeight("0");
//				goodsMonthEntity.setGbIgmReturnWeight("0");
//				goodsMonthEntity.setGbIgmReturnSubtotal("0");
//				goodsMonthEntity.setGbIgmGbDepStockId(stock.getGbDepartmentGoodsStockId());
//				goodsMonthEntity.setGbIgmDistributerId(stock.getGbDgsGbDistributerId());
//				goodsMonthEntity.setGbIgmDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
//				goodsMonthEntity.setGbIgmFullTime(formatFullTime());
//				goodsMonthEntity.setGbIgmStatus(0);
//				gbDepInventoryGoodsMonthService.save(goodsMonthEntity);
//			}
//
//			//3，更新 monthInventory表
//			Map<String, Object> map = new HashMap<>();
//			map.put("depId", stock.getGbDgsGbDepartmentId());
//			map.put("month", formatWhatMonth(0));
//			//修改日成本表数据
//			GbDepInventoryMonthEntity monthEntity = gbDepInventoryMonthService.queryInventoryMonth(map);
//			if (monthEntity != null) {
//				//增加成本值
//				BigDecimal bigDecimal = new BigDecimal(monthEntity.getGbImProduceTotal());
//				BigDecimal add = bigDecimal.add(useSubtotal);
//				monthEntity.setGbImSubtotal(add.toString());
//				gbDepInventoryMonthService.update(monthEntity);
//			} else {
//				//添加新成本
//				GbDepInventoryMonthEntity monthEntity1 = new GbDepInventoryMonthEntity();
//				monthEntity1.setGbImSubtotal(useSubtotal.toString());
//				monthEntity1.setGbImDepartmentId(stock.getGbDgsGbDepartmentId());
//				monthEntity1.setGbImDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//				monthEntity1.setGbImDistributerId(stock.getGbDgsGbDistributerId());
//				monthEntity1.setGbImWasteTotal("0");
//				monthEntity1.setGbImLossTotal("0");
//				monthEntity1.setGbImReturnTotal("0");
//				monthEntity1.setGbImMonth(formatWhatMonth(0));
//				monthEntity1.setGbImYear(formatWhatYear(0));
//				monthEntity1.setGbImStatus(0);
//				gbDepInventoryMonthService.save(monthEntity1);
//			}
//
//			// 修改剩余数量和成本
//			stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
//			stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
//		}
//
//		//3， 更新goodsStock表
//		gbDepartmentGoodsStockService.update(stock);
//		return R.ok();
//	}

//	@RequestMapping(value = "/saveDepInventoryStockMonth", method = RequestMethod.POST)
//	@ResponseBody
//	public R saveDepInventoryStockMonth (@RequestBody GbDepartmentGoodsStockEntity  stock) {
//		//如果库存数量不等于剩余数量，也就是----使用数量不等于0
//		if(!stock.getGbDgsInventoryWeight().equals(stock.getGbDgsRestWeight()) ){
//			BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight());
//			BigDecimal inventoryWeight = new BigDecimal(stock.getGbDgsInventoryWeight());
//			// useWeight：门店使用数量指制作成本；
//			// useWeight：库房使用数量指应该库存与实际库存的差异数量
//			BigDecimal useWeight = restWeight.subtract(inventoryWeight);//剩余数量-最新库存数量（inventory）
//			BigDecimal price = new BigDecimal(stock.getGbDgsPrice());
//			BigDecimal useSubtotal = useWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP);
//
//			//1, reduce
//			GbDepartmentGoodsStockReduceEntity  reduceEntity = new GbDepartmentGoodsStockReduceEntity();
//			reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeProduce()); //
//			reduceEntity.setGbDgsrGbDistributerId(stock.getGbDgsGbDistributerId());
//			reduceEntity.setGbDgsrGbDepartmentId(stock.getGbDgsGbDepartmentId());
//			reduceEntity.setGbDgsrGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//			reduceEntity.setGbDgsrGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
//			reduceEntity.setGbDgsrGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
//			reduceEntity.setGbDgsrGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
//			reduceEntity.setGbDgsrGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
//			reduceEntity.setGbDgsrFullTime(formatFullTime());
//			reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReduceWeightUserId());
//			reduceEntity.setGbDgsrDate(formatWhatDay(0));
//			reduceEntity.setGbDgsrWeek(getWeekOfYear(0).toString());
//			reduceEntity.setGbDgsrMonth(formatWhatMonth(0));
//			reduceEntity.setGbDgsrCostWeight(useWeight.toString());
//			reduceEntity.setGbDgsrCostSubtotal(useSubtotal.toString());
//			gbDepartmentStockReduceService.save(reduceEntity);
//
//
//			// 更新depDisGoods
//			subscribeDepDisGoodsTotal(useWeight, useSubtotal,stock.getGbDgsGbDepDisGoodsId());
//
//
//			//2， 更新inventoryGoodsMonth表
//			Map<String, Object> map11 = new HashMap<>();
//			map11.put("stockId", stock.getGbDepartmentGoodsStockId());
//			map11.put("month", formatWhatMonth(0));
//			map11.put("depId", stock.getGbDgsGbDepartmentId());
//			List<GbDepInventoryGoodsMonthEntity> depInventoryGoodsMonthEntities = gbDepInventoryGoodsMonthService.queryMonthStockByParams(map11);
//			//如果有批次数据
//			if(depInventoryGoodsMonthEntities.size() > 0){
//				//在最后一条盘库记录添加损耗
//				int index = depInventoryGoodsMonthEntities.size() - 1;
//				GbDepInventoryGoodsMonthEntity goodsMonthEntity = depInventoryGoodsMonthEntities.get(index);
//				BigDecimal dailyWeight = new BigDecimal(goodsMonthEntity.getGbIgmWeight());
//				BigDecimal newWeight = dailyWeight.add(useWeight);
//				BigDecimal newSubtotal = newWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP);
//				goodsMonthEntity.setGbIgmProduceWeight(newWeight.toString());
//				goodsMonthEntity.setGbIgmProduceSubtotal(newSubtotal.toString());
//				gbDepInventoryGoodsMonthService.update(goodsMonthEntity);
//
//			}else{
//				//添加新盘库商品记录
//				GbDepInventoryGoodsMonthEntity goodsMonthEntity = new GbDepInventoryGoodsMonthEntity();
//				goodsMonthEntity.setGbIgmDisGoodsId(stock.getGbDgsGbDisGoodsId());
//				goodsMonthEntity.setGbIgmDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
//				goodsMonthEntity.setGbIgmDepartmentId(stock.getGbDgsGbDepartmentId());
//				goodsMonthEntity.setGbIgmDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//				goodsMonthEntity.setGbIgmProduceSubtotal(useSubtotal.toString());
//				goodsMonthEntity.setGbIgmProduceWeight(useWeight.toString());
//				goodsMonthEntity.setGbIgmMonth(formatWhatMonth(0));
//				goodsMonthEntity.setGbIgmYear(formatWhatYear(0));
//				goodsMonthEntity.setGbIgmWasteWeight("0");
//				goodsMonthEntity.setGbIgmWasteSubtotal("0");
//				goodsMonthEntity.setGbIgmLossSubtotal("0");
//				goodsMonthEntity.setGbIgmLossWeight("0");
//				goodsMonthEntity.setGbIgmReturnWeight("0");
//				goodsMonthEntity.setGbIgmReturnSubtotal("0");
//				goodsMonthEntity.setGbIgmGbDepStockId(stock.getGbDepartmentGoodsStockId());
//				goodsMonthEntity.setGbIgmDistributerId(stock.getGbDgsGbDistributerId());
//				goodsMonthEntity.setGbIgmDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
//				goodsMonthEntity.setGbIgmFullTime(formatFullTime());
//				goodsMonthEntity.setGbIgmStatus(0);
//				gbDepInventoryGoodsMonthService.save(goodsMonthEntity);
//			}
//
//			//3，更新 monthInventory表
//			Map<String, Object> map = new HashMap<>();
//			map.put("depId", stock.getGbDgsGbDepartmentId());
//			map.put("month", formatWhatMonth(0));
//			//修改日成本表数据
//			GbDepInventoryMonthEntity monthEntity = gbDepInventoryMonthService.queryInventoryMonth(map);
//			if (monthEntity != null) {
//				//增加成本值
//				BigDecimal bigDecimal = new BigDecimal(monthEntity.getGbImProduceTotal());
//				BigDecimal add = bigDecimal.add(useSubtotal);
//				monthEntity.setGbImSubtotal(add.toString());
//				gbDepInventoryMonthService.update(monthEntity);
//			} else {
//				//添加新成本
//				GbDepInventoryMonthEntity monthEntity1 = new GbDepInventoryMonthEntity();
//				monthEntity1.setGbImSubtotal(useSubtotal.toString());
//				monthEntity1.setGbImDepartmentId(stock.getGbDgsGbDepartmentId());
//				monthEntity1.setGbImDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//				monthEntity1.setGbImDistributerId(stock.getGbDgsGbDistributerId());
//				monthEntity1.setGbImWasteTotal("0");
//				monthEntity1.setGbImLossTotal("0");
//				monthEntity1.setGbImReturnTotal("0");
//				monthEntity1.setGbImMonth(formatWhatMonth(0));
//				monthEntity1.setGbImYear(formatWhatYear(0));
//				monthEntity1.setGbImStatus(0);
//				gbDepInventoryMonthService.save(monthEntity1);
//			}
//
//			// 修改剩余数量和成本
//			stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
//			stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
//		}
//
//		//3， 更新goodsStock表
//		stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
//		stock.setGbDgsInventoryDate(formatWhatDay(0));
//		stock.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
//		stock.setGbDgsInventoryMonth(getNextMonth()); //下一个月的时间
//		gbDepartmentGoodsStockService.update(stock);
//		return R.ok();
//	}


    @RequestMapping(value = "/mendianGetMonthInventoryDetail", method = RequestMethod.POST)
    @ResponseBody
    public R mendianGetMonthInventoryDetail(Integer depFatherId, String month) {
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("month", month);
        map.put("year", formatWhatYear(0));
        List<GbDepInventoryGoodsMonthEntity> depInventoryGoodsDailyEntities = gbDepInventoryGoodsMonthService.queryMonthStockByParams(map);

        return R.ok().put("data", depInventoryGoodsDailyEntities);
    }


    @RequestMapping(value = "/updateStockDepInventoryGoodsMonth", method = RequestMethod.POST)
    @ResponseBody
    public R updateStockDepInventoryGoodsMonth(@RequestBody List<GbDepartmentGoodsStockEntity> stockList) {


        for (GbDepartmentGoodsStockEntity stock : stockList) {

            //如果库存数量不等于剩余数量，也就是----使用数量不等于0
            if(!stock.getGbDgsInventoryWeight().equals(stock.getGbDgsRestWeight()) ){
                BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight());
                BigDecimal inventoryWeight = new BigDecimal(stock.getGbDgsInventoryWeight());
                // useWeight：门店使用数量指制作成本；
                // useWeight：库房使用数量指应该库存与实际库存的差异数量
                BigDecimal useWeight = restWeight.subtract(inventoryWeight);//剩余数量-最新库存数量（inventory）
                BigDecimal price = new BigDecimal(stock.getGbDgsPrice());
                BigDecimal useSubtotal = useWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP);

                // 2, 更新depDisGoods
                subscribeDepDisGoodsTotal(useWeight, useSubtotal, stock.getGbDgsGbDepDisGoodsId());


                // 修改剩余数量和成本
                stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
                stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
            }

            stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
            stock.setGbDgsInventoryDate(formatWhatDay(0));
            stock.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
            gbDepartmentGoodsStockService.update(stock);
        }
        return R.ok();
    }
    /**
     * 月盘库
     * @param stockList 商品库存全部批次
     * 前台提交 GbDgsInventoryWeight： 最新库存数量
     * @return 返回 0
     */
	@RequestMapping(value = "/updateDepInventoryGoodsMonth", method = RequestMethod.POST)
	@ResponseBody
	public R updateDepInventoryGoodsMonth(@RequestBody List<GbDepartmentGoodsStockEntity> stockList) {


		for (GbDepartmentGoodsStockEntity stock : stockList) {

			//如果库存数量不等于剩余数量，也就是----使用数量不等于0
			if(!stock.getGbDgsInventoryWeight().equals(stock.getGbDgsRestWeight()) ){
				BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight());
				BigDecimal inventoryWeight = new BigDecimal(stock.getGbDgsInventoryWeight());
				// useWeight：门店使用数量指制作成本；
				// useWeight：库房使用数量指应该库存与实际库存的差异数量
				BigDecimal useWeight = restWeight.subtract(inventoryWeight);//剩余数量-最新库存数量（inventory）
				BigDecimal price = new BigDecimal(stock.getGbDgsPrice());
				BigDecimal useSubtotal = useWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP);


				//1， 更新inventoryGoodsMonth表
				Map<String, Object> map11 = new HashMap<>();
				map11.put("stockId", stock.getGbDepartmentGoodsStockId());
				map11.put("month", formatWhatMonth(0));
				map11.put("depId", stock.getGbDgsGbDepartmentId());
				List<GbDepInventoryGoodsMonthEntity> depInventoryGoodsMonthEntities = gbDepInventoryGoodsMonthService.queryMonthStockByParams(map11);
				//如果有批次数据
				if(depInventoryGoodsMonthEntities.size() > 0){
					//在最后一条盘库记录记录添加
					int index = depInventoryGoodsMonthEntities.size() - 1;
					GbDepInventoryGoodsMonthEntity goodsMonthEntity = depInventoryGoodsMonthEntities.get(index);
					BigDecimal dailyWeight = new BigDecimal(goodsMonthEntity.getGbIgmWeight());
					BigDecimal newWeight = dailyWeight.add(useWeight);
					BigDecimal newSubtotal = newWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP);
					goodsMonthEntity.setGbIgmProduceWeight(newWeight.toString());
					goodsMonthEntity.setGbIgmProduceSubtotal(newSubtotal.toString());
					gbDepInventoryGoodsMonthService.update(goodsMonthEntity);
				}

				// 2, 更新depDisGoods
				subscribeDepDisGoodsTotal(useWeight, useSubtotal, stock.getGbDgsGbDepDisGoodsId());

				// 3,add  reduce
				GbDepartmentGoodsStockReduceEntity reduceEntity = new GbDepartmentGoodsStockReduceEntity();
				BigDecimal newCostSubtotal = useWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);
				reduceEntity.setGbDgsrCostWeight(useWeight.toString());
				reduceEntity.setGbDgsrCostSubtotal(newCostSubtotal.toString());
				reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeProduce()); //
				reduceEntity.setGbDgsrGbDistributerId(stock.getGbDgsGbDistributerId());
				reduceEntity.setGbDgsrGbDepartmentId(stock.getGbDgsGbDepartmentId());
				reduceEntity.setGbDgsrGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
				reduceEntity.setGbDgsrGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
				reduceEntity.setGbDgsrGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
				reduceEntity.setGbDgsrGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
				reduceEntity.setGbDgsrGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
				reduceEntity.setGbDgsrFullTime(formatFullTime());
				reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReduceWeightUserId());
				reduceEntity.setGbDgsrDate(formatWhatDay(0));
                reduceEntity.setGbDgsrStockNxDistribtuerId(stock.getGbDgsNxDistributerId());
                reduceEntity.setGbDgsrWeek(getWeekOfYear(0).toString());
				reduceEntity.setGbDgsrMonth(formatWhatMonth(0));

				gbDepartmentStockReduceService.save(reduceEntity);

				//4，更新 monthInventory表
				Map<String, Object> map = new HashMap<>();
				map.put("depId", stock.getGbDgsGbDepartmentId());
				map.put("month", formatWhatMonth(0));
				//修改日成本表数据
				GbDepInventoryMonthEntity monthEntity = gbDepInventoryMonthService.queryInventoryMonth(map);
				if (monthEntity != null) {
					//增加成本值
					BigDecimal bigDecimal = new BigDecimal(monthEntity.getGbImProduceTotal());
					BigDecimal add = bigDecimal.add(useSubtotal);
					monthEntity.setGbImSubtotal(add.toString());
					gbDepInventoryMonthService.update(monthEntity);
				}

				// 修改剩余数量和成本
				stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
				stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
			}

			stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
			stock.setGbDgsInventoryDate(formatWhatDay(0));
			stock.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
			gbDepartmentGoodsStockService.update(stock);
		}
		return R.ok();
	}

    @RequestMapping(value = "/saveStockDepInventoryGoodsMonth", method = RequestMethod.POST)
    @ResponseBody
    public R saveStockDepInventoryGoodsMonth(@RequestBody List<GbDepartmentGoodsStockEntity> stockList) {
        System.out.println("yuepankanankaka" + stockList.size());
        for (GbDepartmentGoodsStockEntity stock : stockList) {

            BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight());
            BigDecimal inventoryWeight = new BigDecimal(stock.getGbDgsInventoryWeight());
            // useWeight：门店使用数量指制作成本；
            // useWeight：库房使用数量指应该库存与实际库存的差异数量
            BigDecimal useWeight = restWeight.subtract(inventoryWeight);//剩余数量-最新库存数量（inventory）
            BigDecimal price = new BigDecimal(stock.getGbDgsPrice());
            BigDecimal useSubtotal = useWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);

            //1, reduce
            GbDepartmentGoodsStockReduceEntity reduceEntity = new GbDepartmentGoodsStockReduceEntity();
            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeLoss()); //
            reduceEntity.setGbDgsrStatus(0);
            reduceEntity.setGbDgsrGbDistributerId(stock.getGbDgsGbDistributerId());
            reduceEntity.setGbDgsrGbDepartmentId(stock.getGbDgsGbDepartmentId());
            reduceEntity.setGbDgsrGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            reduceEntity.setGbDgsrGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
            reduceEntity.setGbDgsrGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
            reduceEntity.setGbDgsrGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
            reduceEntity.setGbDgsrGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
            reduceEntity.setGbDgsrFullTime(formatFullTime());
            reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReduceWeightUserId());
            reduceEntity.setGbDgsrDate(formatWhatDay(0));
            reduceEntity.setGbDgsrStockNxDistribtuerId(stock.getGbDgsNxDistributerId());
            reduceEntity.setGbDgsrWeek(getWeekOfYear(0).toString());
            reduceEntity.setGbDgsrMonth(formatWhatMonth(0));
            reduceEntity.setGbDgsrCostWeight(useWeight.toString());
            reduceEntity.setGbDgsrCostSubtotal(useSubtotal.toString());
            reduceEntity.setGbDgsrLossWeight(useWeight.toString());
            reduceEntity.setGbDgsrLossSubtotal(useSubtotal.toString());
            reduceEntity.setGbDgsrGbPurGoodsId(stock.getGbDgsGbPurGoodsId());
            reduceEntity.setGbDgsrReturnWeight("0");
            reduceEntity.setGbDgsrReturnSubtotal("0");
            reduceEntity.setGbDgsrWasteWeight("0");
            reduceEntity.setGbDgsrWasteSubtotal("0");
            reduceEntity.setGbDgsrProduceWeight("0");
            reduceEntity.setGbDgsrProduceSubtotal("0");
            reduceEntity.setGbDgsrGbGoodsInventoryType(stock.getGbDistributerGoodsEntity().getGbDgGoodsInventoryType());
            gbDepartmentStockReduceService.save(reduceEntity);

            // 更新depDisGoods
            subscribeDepDisGoodsTotal(useWeight, useSubtotal, stock.getGbDgsGbDepDisGoodsId());


            // 修改剩余数量和成本
            stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
            stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

            String month = "";
            String s = formatWhatMonth(0);
            if (s.equals("12")) {
                month = "01";
            } else {
                BigDecimal monthth = new BigDecimal(s).add(new BigDecimal(1));
                if(monthth.compareTo(new BigDecimal(10)) == -1){
                    month = "0" + monthth.toString();
                }else{
                    month = monthth.toString();
                }
            }
            //3， 更新goodsStock表
            stock.setGbDgsInventoryMonth(month);
            stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
            stock.setGbDgsInventoryDate(formatWhatDay(0));
            stock.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
            gbDepartmentGoodsStockService.update(stock);
        }
        return R.ok();
    }

    /**
     * 月盘库
     * @param
     *  ： 最新库存数量
     * @return 返回 0
     */
    @RequestMapping(value = "/saveDepInventoryGoodsMonth", method = RequestMethod.POST)
    @ResponseBody
    public R saveDepInventoryGoodsMonth(@RequestBody List<GbDepartmentGoodsStockEntity> stockList) {
        System.out.println("yuepankanankaka" + stockList.size());
        for (GbDepartmentGoodsStockEntity stock : stockList) {

            //如果库存数量不等于剩余数量，也就是----使用数量不等于0
            if (!stock.getGbDgsInventoryWeight().equals(stock.getGbDgsRestWeight())) {
                BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight());
                BigDecimal inventoryWeight = new BigDecimal(stock.getGbDgsInventoryWeight());
                // useWeight：门店使用数量指制作成本；
                // useWeight：库房使用数量指应该库存与实际库存的差异数量
                BigDecimal useWeight = restWeight.subtract(inventoryWeight);//剩余数量-最新库存数量（inventory）
                BigDecimal price = new BigDecimal(stock.getGbDgsPrice());
                BigDecimal useSubtotal = useWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);

                //1, reduce
                GbDepartmentGoodsStockReduceEntity reduceEntity = new GbDepartmentGoodsStockReduceEntity();
                reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeProduce()); //
                reduceEntity.setGbDgsrStatus(0);
                reduceEntity.setGbDgsrGbDistributerId(stock.getGbDgsGbDistributerId());
                reduceEntity.setGbDgsrGbDepartmentId(stock.getGbDgsGbDepartmentId());
                reduceEntity.setGbDgsrGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
                reduceEntity.setGbDgsrGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
                reduceEntity.setGbDgsrGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
                reduceEntity.setGbDgsrGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
                reduceEntity.setGbDgsrGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
                reduceEntity.setGbDgsrFullTime(formatFullTime());
                reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReduceWeightUserId());
                reduceEntity.setGbDgsrDate(formatWhatDay(0));
                reduceEntity.setGbDgsrStockNxDistribtuerId(stock.getGbDgsNxDistributerId());
                reduceEntity.setGbDgsrWeek(getWeekOfYear(0).toString());
                reduceEntity.setGbDgsrMonth(formatWhatMonth(0));
                reduceEntity.setGbDgsrCostWeight(useWeight.toString());
                reduceEntity.setGbDgsrCostSubtotal(useSubtotal.toString());
                reduceEntity.setGbDgsrProduceWeight(useWeight.toString());
                reduceEntity.setGbDgsrProduceSubtotal(useSubtotal.toString());
                reduceEntity.setGbDgsrGbPurGoodsId(stock.getGbDgsGbPurGoodsId());
                reduceEntity.setGbDgsrReturnWeight("0");
                reduceEntity.setGbDgsrReturnSubtotal("0");
                reduceEntity.setGbDgsrWasteWeight("0");
                reduceEntity.setGbDgsrWasteSubtotal("0");
                reduceEntity.setGbDgsrLossWeight("0");
                reduceEntity.setGbDgsrLossSubtotal("0");
                reduceEntity.setGbDgsrGbGoodsInventoryType(stock.getGbDistributerGoodsEntity().getGbDgGoodsInventoryType());
                gbDepartmentStockReduceService.save(reduceEntity);

                // 更新depDisGoods
                subscribeDepDisGoodsTotal(useWeight, useSubtotal, stock.getGbDgsGbDepDisGoodsId());

                //2， 更新inventoryGoodsMonth表
                Map<String, Object> map11 = new HashMap<>();
                map11.put("stockId", stock.getGbDepartmentGoodsStockId());
                map11.put("month", formatWhatMonth(0));
                map11.put("depId", stock.getGbDgsGbDepartmentId());
                List<GbDepInventoryGoodsMonthEntity> depInventoryGoodsMonthEntities = gbDepInventoryGoodsMonthService.queryMonthStockByParams(map11);
                //如果有批次数据
                if (depInventoryGoodsMonthEntities.size() > 0) {
                    //在最后一条盘库记录添加损耗
                    int index = depInventoryGoodsMonthEntities.size() - 1;
                    GbDepInventoryGoodsMonthEntity goodsMonthEntity = depInventoryGoodsMonthEntities.get(index);
                    BigDecimal dailyWeight = new BigDecimal(goodsMonthEntity.getGbIgmProduceWeight());
                    BigDecimal newWeight = dailyWeight.add(useWeight);
                    BigDecimal newSubtotal = newWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);
                    goodsMonthEntity.setGbIgmProduceWeight(newWeight.toString());
                    goodsMonthEntity.setGbIgmProduceSubtotal(newSubtotal.toString());
                    gbDepInventoryGoodsMonthService.update(goodsMonthEntity);

                } else {
                    //添加新盘库商品记录
                    GbDepInventoryGoodsMonthEntity goodsMonthEntity = new GbDepInventoryGoodsMonthEntity();
                    goodsMonthEntity.setGbIgmDisGoodsId(stock.getGbDgsGbDisGoodsId());
                    goodsMonthEntity.setGbIgmDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
                    goodsMonthEntity.setGbIgmDepartmentId(stock.getGbDgsGbDepartmentId());
                    goodsMonthEntity.setGbIgmDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
                    goodsMonthEntity.setGbIgmProduceSubtotal(useSubtotal.toString());
                    goodsMonthEntity.setGbIgmProduceWeight(useWeight.toString());
                    goodsMonthEntity.setGbIgmMonth(formatWhatMonth(0));
                    goodsMonthEntity.setGbIgmYear(formatWhatYear(0));
                    goodsMonthEntity.setGbIgmWasteWeight("0");
                    goodsMonthEntity.setGbIgmWasteSubtotal("0");
                    goodsMonthEntity.setGbIgmLossSubtotal("0");
                    goodsMonthEntity.setGbIgmLossWeight("0");
                    goodsMonthEntity.setGbIgmReturnWeight("0");
                    goodsMonthEntity.setGbIgmReturnSubtotal("0");
                    goodsMonthEntity.setGbIgmGbDepStockId(stock.getGbDepartmentGoodsStockId());
                    goodsMonthEntity.setGbIgmDistributerId(stock.getGbDgsGbDistributerId());
                    goodsMonthEntity.setGbIgmDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
                    goodsMonthEntity.setGbIgmFullTime(formatFullTime());
                    goodsMonthEntity.setGbIgmStatus(0);
                    gbDepInventoryGoodsMonthService.save(goodsMonthEntity);
                }

                //3，更新 monthInventory表
                Map<String, Object> map = new HashMap<>();
                map.put("depId", stock.getGbDgsGbDepartmentId());
                map.put("month", formatWhatMonth(0));
                //修改日成本表数据
                GbDepInventoryMonthEntity monthEntity = gbDepInventoryMonthService.queryInventoryMonth(map);
                if (monthEntity != null) {
                    //增加成本值
                    BigDecimal bigDecimal = new BigDecimal(monthEntity.getGbImProduceTotal());
                    BigDecimal add = bigDecimal.add(useSubtotal);
                    monthEntity.setGbImSubtotal(add.toString());
                    gbDepInventoryMonthService.update(monthEntity);
                } else {
                    //添加新成本
                    GbDepInventoryMonthEntity monthEntity1 = new GbDepInventoryMonthEntity();
                    monthEntity1.setGbImSubtotal(useSubtotal.toString());
                    monthEntity1.setGbImDepartmentId(stock.getGbDgsGbDepartmentId());
                    monthEntity1.setGbImDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
                    monthEntity1.setGbImDistributerId(stock.getGbDgsGbDistributerId());
                    monthEntity1.setGbImWasteTotal("0");
                    monthEntity1.setGbImLossTotal("0");
                    monthEntity1.setGbImReturnTotal("0");
                    monthEntity1.setGbImMonth(formatWhatMonth(0));
                    monthEntity1.setGbImYear(formatWhatYear(0));
                    monthEntity1.setGbImStatus(0);
                    monthEntity1.setGbImProduceTotal(useWeight.toString());
                    gbDepInventoryMonthService.save(monthEntity1);
                }

                // 修改剩余数量和成本
                stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
                stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }

            String month = "";
            String s = formatWhatMonth(0);
            if (s.equals("12")) {
                month = "01";
            } else {
                month = new BigDecimal(s).add(new BigDecimal(1)).toString();
            }
            //3， 更新goodsStock表
            stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
            stock.setGbDgsInventoryDate(formatWhatDay(0));
            stock.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
            stock.setGbDgsInventoryMonth(month);
            gbDepartmentGoodsStockService.update(stock);
        }
        return R.ok();
    }


    private void subscribeDepDisGoodsTotal(BigDecimal weight, BigDecimal subtotal, Integer depDisGoodsId) {

        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        BigDecimal weightB = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).subtract(weight);
        BigDecimal subtotalB = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).subtract(subtotal);
        depDisGoodsEntity.setGbDdgStockTotalSubtotal(subtotalB.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgStockTotalWeight(weightB.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgInventoryDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgInventoryFullTime(formatWhatFullTime(0));
        gbDepartmentDisGoodsService.update(depDisGoodsEntity);
    }

    private void addDepDisGoodsTotal(BigDecimal weight, BigDecimal subtotal, Integer depDisGoodsId) {

        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        BigDecimal weightB = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).add(weight);
        BigDecimal subtotalB = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).add(subtotal);
        depDisGoodsEntity.setGbDdgStockTotalSubtotal(subtotalB.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgStockTotalWeight(weightB.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgInventoryDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgInventoryFullTime(formatWhatFullTime(0));
        gbDepartmentDisGoodsService.update(depDisGoodsEntity);
    }

}
