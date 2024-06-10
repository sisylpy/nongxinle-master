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
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartGoodsStockReduceTypeProduce;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartGoodsStockReduceTypeWaste;


@RestController
@RequestMapping("api/gbdepinventorygoodsweek")
public class GbDepInventoryGoodsWeekController {
    @Autowired
    private GbDepInventoryGoodsWeekService gbDepInventoryGoodsWeekService;
    @Autowired
    private GbDepInventoryWeekService gbDepInventoryWeekService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepartmentStockReduceService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;

//    @RequestMapping(value = "/saveDepChangeStockWeek", method = RequestMethod.POST)
//    @ResponseBody
//    public R saveDepChangeStockWeek (@RequestBody  GbDepartmentGoodsStockEntity stock) {
//        if (!stock.getGbDgsInventoryWeight().equals(stock.getGbDgsRestWeight())) {
//            System.out.println("saveDepInventoryStockWeeksaveDepInventoryStockWeek");
//
//            BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight());
//            BigDecimal inventoryWeight = new BigDecimal(stock.getGbDgsInventoryWeight());
//            BigDecimal useWeight = restWeight.subtract(inventoryWeight).setScale(1, BigDecimal.ROUND_HALF_DOWN); //剩余数量-最新库存数量（inventory）
//            BigDecimal price = new BigDecimal(stock.getGbDgsPrice());
//            BigDecimal useSubtotal = useWeight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
//
//            //1, reduce
//            GbDepartmentGoodsStockReduceEntity reduceEntity = new GbDepartmentGoodsStockReduceEntity();
//            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeProduce()); //
//            reduceEntity.setGbDgsrGbDistributerId(stock.getGbDgsGbDistributerId());
//            reduceEntity.setGbDgsrGbDepartmentId(stock.getGbDgsGbDepartmentId());
//            reduceEntity.setGbDgsrGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//            reduceEntity.setGbDgsrGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
//            reduceEntity.setGbDgsrGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
//            reduceEntity.setGbDgsrGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
//            reduceEntity.setGbDgsrGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
//            reduceEntity.setGbDgsrFullTime(formatFullTime());
//            reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReduceWeightUserId());
//            reduceEntity.setGbDgsrDate(formatWhatDay(0));
//            reduceEntity.setGbDgsrWeek(getWeekOfYear(0).toString());
//            reduceEntity.setGbDgsrMonth(formatWhatMonth(0));
//            reduceEntity.setGbDgsrCostWeight(useWeight.toString());
//            reduceEntity.setGbDgsrCostSubtotal(useSubtotal.toString());
//            gbDepartmentStockReduceService.save(reduceEntity);
//
//            // 更新depDisGoods
//            subscribeDepDisGoodsTotal(useWeight, useSubtotal, stock.getGbDgsGbDepDisGoodsId());
//
//
//            //2
//            Map<String, Object> map11 = new HashMap<>();
//            map11.put("stockId", stock.getGbDepartmentGoodsStockId());
//            map11.put("week", getWeekOfYear(0));
//            map11.put("depId", stock.getGbDgsGbDepartmentId());
//            List<GbDepInventoryGoodsWeekEntity> depInventoryGoodsWeekEntities = gbDepInventoryGoodsWeekService.queryWeekStockByParams(map11);
//            if (depInventoryGoodsWeekEntities.size() > 0) {
//                //在最后一条盘库记录记录添加损耗
//                int index = depInventoryGoodsWeekEntities.size() - 1;
//                GbDepInventoryGoodsWeekEntity goodsDailyEntity = depInventoryGoodsWeekEntities.get(index);
//                BigDecimal dailyWeight = new BigDecimal(goodsDailyEntity.getGbIgwWeight());
//                BigDecimal newWeight = dailyWeight.add(useWeight);
//                BigDecimal newSubtotal = newWeight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
//                goodsDailyEntity.setGbIgwWeight(newWeight.toString());
//                goodsDailyEntity.setGbIgwSubtotal(newSubtotal.toString());
//                gbDepInventoryGoodsWeekService.update(goodsDailyEntity);
//            } else {
//
//                GbDepInventoryGoodsWeekEntity goodsWeekEntity = new GbDepInventoryGoodsWeekEntity();
//                goodsWeekEntity.setGbIgwDisGoodsId(stock.getGbDgsGbDisGoodsId());
//                goodsWeekEntity.setGbIgwDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
//                goodsWeekEntity.setGbIgwDepartmentId(stock.getGbDgsGbDepartmentId());
//                goodsWeekEntity.setGbIgwDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//                goodsWeekEntity.setGbIgwSubtotal(useSubtotal.toString());
//                goodsWeekEntity.setGbIgwWeight(useWeight.toString());
//                goodsWeekEntity.setGbIgwWeek(getWeekOfYear(0).toString());
//                goodsWeekEntity.setGbIgwYear(formatWhatYear(0));
//                goodsWeekEntity.setGbIgwWasteWeight("0");
//                goodsWeekEntity.setGbIgwWasteSubtotal("0");
//                goodsWeekEntity.setGbIgwLossWeight("0");
//                goodsWeekEntity.setGbIgwLossSubtotal("0");
//                goodsWeekEntity.setGbIgwReturnWeight("0");
//                goodsWeekEntity.setGbIgwReturnSubtotal("0");
//                goodsWeekEntity.setGbIgwGbDepStockId(stock.getGbDepartmentGoodsStockId());
//                goodsWeekEntity.setGbIgwDistributerId(stock.getGbDgsGbDistributerId());
//                goodsWeekEntity.setGbIgwDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
//                goodsWeekEntity.setGbIgwFullTime(formatFullTime());
//                goodsWeekEntity.setGbIgwStatus(0);
//                gbDepInventoryGoodsWeekService.save(goodsWeekEntity);
//
//            }
//
//
//            //daily
//
//            Map<String, Object> map = new HashMap<>();
//            map.put("depId", stock.getGbDgsGbDepartmentId());
//            map.put("week", getWeekOfYear(0));
//
//            //修改日成本表数据
//            GbDepInventoryWeekEntity weekEntity = gbDepInventoryWeekService.queryInventoryWeek(map);
//            if (weekEntity != null) {
//                //增加成本值
//                BigDecimal bigDecimal = new BigDecimal(weekEntity.getGbDiwProduceTotal());
//                BigDecimal add = bigDecimal.add(useSubtotal);
//                weekEntity.setGbDiwProduceTotal(add.toString());
//                gbDepInventoryWeekService.update(weekEntity);
//            } else {
//                //添加新成本
//                GbDepInventoryWeekEntity weekEntity1 = new GbDepInventoryWeekEntity();
//                weekEntity1.setGbDiwProduceTotal(useSubtotal.toString());
//                weekEntity1.setGbDiwDepartmentId(stock.getGbDgsGbDepartmentId());
//                weekEntity1.setGbDiwDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//                weekEntity1.setGbDiwDistributerId(stock.getGbDgsGbDistributerId());
//                weekEntity1.setGbDiwWeek(getWeekOfYear(0).toString());
//                weekEntity1.setGbDiwYear(formatWhatYear(0));
//                weekEntity1.setGbDiwWasteTotal("0");
//                weekEntity1.setGbDiwLossTotal("0");
//                weekEntity1.setGbDiwReturnTotal("0");
//                weekEntity1.setGbDiwStatus(0);
//                gbDepInventoryWeekService.save(weekEntity1);
//            }
//
//            stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
//            stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//        }
//
//
//        gbDepartmentGoodsStockService.update(stock);
//        return R.ok();
//    }
//
//    @RequestMapping(value = "/saveDepInventoryStockWeek", method = RequestMethod.POST)
//    @ResponseBody
//    public R saveDepInventoryStockWeek (@RequestBody  GbDepartmentGoodsStockEntity stock) {
//        if (!stock.getGbDgsInventoryWeight().equals(stock.getGbDgsRestWeight())) {
//            System.out.println("saveDepInventoryStockWeeksaveDepInventoryStockWeek");
//
//            BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight());
//            BigDecimal inventoryWeight = new BigDecimal(stock.getGbDgsInventoryWeight());
//            BigDecimal useWeight = restWeight.subtract(inventoryWeight).setScale(2, BigDecimal.ROUND_HALF_DOWN); //剩余数量-最新库存数量（inventory）
//            BigDecimal price = new BigDecimal(stock.getGbDgsPrice());
//            BigDecimal useSubtotal = useWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);
//
//            //1, reduce
//            GbDepartmentGoodsStockReduceEntity reduceEntity = new GbDepartmentGoodsStockReduceEntity();
//            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeProduce()); //
//            reduceEntity.setGbDgsrGbDistributerId(stock.getGbDgsGbDistributerId());
//            reduceEntity.setGbDgsrGbDepartmentId(stock.getGbDgsGbDepartmentId());
//            reduceEntity.setGbDgsrGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//            reduceEntity.setGbDgsrGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
//            reduceEntity.setGbDgsrGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
//            reduceEntity.setGbDgsrGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
//            reduceEntity.setGbDgsrGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
//            reduceEntity.setGbDgsrFullTime(formatFullTime());
//            reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReduceWeightUserId());
//            reduceEntity.setGbDgsrDate(formatWhatDay(0));
//            reduceEntity.setGbDgsrWeek(getWeekOfYear(0).toString());
//            reduceEntity.setGbDgsrMonth(formatWhatMonth(0));
//            reduceEntity.setGbDgsrCostWeight(useWeight.toString());
//            reduceEntity.setGbDgsrCostSubtotal(useSubtotal.toString());
//            gbDepartmentStockReduceService.save(reduceEntity);
//
//            // 更新depDisGoods
//            subscribeDepDisGoodsTotal(useWeight, useSubtotal, stock.getGbDgsGbDepDisGoodsId());
//
//
//            //2
//            Map<String, Object> map11 = new HashMap<>();
//            map11.put("stockId", stock.getGbDepartmentGoodsStockId());
//            map11.put("week", getWeekOfYear(0));
//            map11.put("depId", stock.getGbDgsGbDepartmentId());
//            List<GbDepInventoryGoodsWeekEntity> depInventoryGoodsWeekEntities = gbDepInventoryGoodsWeekService.queryWeekStockByParams(map11);
//            if (depInventoryGoodsWeekEntities.size() > 0) {
//                //在最后一条盘库记录记录添加损耗
//                int index = depInventoryGoodsWeekEntities.size() - 1;
//                GbDepInventoryGoodsWeekEntity goodsDailyEntity = depInventoryGoodsWeekEntities.get(index);
//                BigDecimal dailyWeight = new BigDecimal(goodsDailyEntity.getGbIgwWeight());
//                BigDecimal newWeight = dailyWeight.add(useWeight);
//                BigDecimal newSubtotal = newWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);
//                goodsDailyEntity.setGbIgwWeight(newWeight.toString());
//                goodsDailyEntity.setGbIgwSubtotal(newSubtotal.toString());
//                gbDepInventoryGoodsWeekService.update(goodsDailyEntity);
//            } else {
//
//                GbDepInventoryGoodsWeekEntity goodsWeekEntity = new GbDepInventoryGoodsWeekEntity();
//                goodsWeekEntity.setGbIgwDisGoodsId(stock.getGbDgsGbDisGoodsId());
//                goodsWeekEntity.setGbIgwDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
//                goodsWeekEntity.setGbIgwDepartmentId(stock.getGbDgsGbDepartmentId());
//                goodsWeekEntity.setGbIgwDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//                goodsWeekEntity.setGbIgwSubtotal(useSubtotal.toString());
//                goodsWeekEntity.setGbIgwWeight(useWeight.toString());
//                goodsWeekEntity.setGbIgwWeek(getWeekOfYear(0).toString());
//                goodsWeekEntity.setGbIgwYear(formatWhatYear(0));
//                goodsWeekEntity.setGbIgwWasteWeight("0");
//                goodsWeekEntity.setGbIgwWasteSubtotal("0");
//                goodsWeekEntity.setGbIgwLossWeight("0");
//                goodsWeekEntity.setGbIgwLossSubtotal("0");
//                goodsWeekEntity.setGbIgwReturnWeight("0");
//                goodsWeekEntity.setGbIgwReturnSubtotal("0");
//                goodsWeekEntity.setGbIgwGbDepStockId(stock.getGbDepartmentGoodsStockId());
//                goodsWeekEntity.setGbIgwDistributerId(stock.getGbDgsGbDistributerId());
//                goodsWeekEntity.setGbIgwDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
//                goodsWeekEntity.setGbIgwFullTime(formatFullTime());
//                goodsWeekEntity.setGbIgwStatus(0);
//                gbDepInventoryGoodsWeekService.save(goodsWeekEntity);
//
//            }
//
//
//            //daily
//
//            Map<String, Object> map = new HashMap<>();
//            map.put("depId", stock.getGbDgsGbDepartmentId());
//            map.put("week", getWeekOfYear(0));
//
//            //修改日成本表数据
//            GbDepInventoryWeekEntity weekEntity = gbDepInventoryWeekService.queryInventoryWeek(map);
//            if (weekEntity != null) {
//                //增加成本值
//                BigDecimal bigDecimal = new BigDecimal(weekEntity.getGbDiwProduceTotal());
//                BigDecimal add = bigDecimal.add(useSubtotal);
//                weekEntity.setGbDiwProduceTotal(add.toString());
//                gbDepInventoryWeekService.update(weekEntity);
//            } else {
//                //添加新成本
//                GbDepInventoryWeekEntity weekEntity1 = new GbDepInventoryWeekEntity();
//                weekEntity1.setGbDiwProduceTotal(useSubtotal.toString());
//                weekEntity1.setGbDiwDepartmentId(stock.getGbDgsGbDepartmentId());
//                weekEntity1.setGbDiwDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//                weekEntity1.setGbDiwDistributerId(stock.getGbDgsGbDistributerId());
//                weekEntity1.setGbDiwWeek(getWeekOfYear(0).toString());
//                weekEntity1.setGbDiwYear(formatWhatYear(0));
//                weekEntity1.setGbDiwWasteTotal("0");
//                weekEntity1.setGbDiwLossTotal("0");
//                weekEntity1.setGbDiwReturnTotal("0");
//                weekEntity1.setGbDiwStatus(0);
//                gbDepInventoryWeekService.save(weekEntity1);
//            }
//
//            stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
//            stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
//
//        }
//
//        BigDecimal week = new BigDecimal(getWeekOfYear(0)).add(new BigDecimal("1"));
//        stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
//        stock.setGbDgsInventoryDate(formatWhatDay(0));
//        stock.setGbDgsInventoryMonth(formatWhatMonth(0));
//        stock.setGbDgsInventoryWeek(week.toString());
//        gbDepartmentGoodsStockService.update(stock);
//        return R.ok();
//    }


    @RequestMapping(value = "/mendianGetWeekInventoryDetail", method = RequestMethod.POST)
    @ResponseBody
    public R mendianGetWeekInventoryDetail(Integer depFatherId, String week) {
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("week", week);
        map.put("year", formatWhatYear(0));
        List<GbDepInventoryGoodsWeekEntity> depInventoryGoodsDailyEntities = gbDepInventoryGoodsWeekService.queryWeekStockByParams(map);

        return R.ok().put("data", depInventoryGoodsDailyEntities);
    }


//    @RequestMapping(value = "/updateDepInventoryGoodsWeek", method = RequestMethod.POST)
//    @ResponseBody
//    public R updateDepInventoryGoodsWeek(@RequestBody List<GbDepartmentGoodsStockEntity> stockList) {
//        for (GbDepartmentGoodsStockEntity stock : stockList) {
//            if (!stock.getGbDgsInventoryWeight().equals(stock.getGbDgsRestWeight())) {
//
//                BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight());
//                BigDecimal inventoryWeight = new BigDecimal(stock.getGbDgsInventoryWeight());
//                BigDecimal useWeight = restWeight.subtract(inventoryWeight).setScale(2, BigDecimal.ROUND_HALF_DOWN); //剩余数量-最新库存数量（inventory）
//                BigDecimal price = new BigDecimal(stock.getGbDgsPrice());
//                BigDecimal useSubtotal = useWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);
//                Map<String, Object> map11 = new HashMap<>();
//                map11.put("stockId", stock.getGbDepartmentGoodsStockId());
//                map11.put("week", getWeekOfYear(0));
//                map11.put("depId", stock.getGbDgsGbDepartmentId());
//                List<GbDepInventoryGoodsWeekEntity> depInventoryGoodsWeekEntities = gbDepInventoryGoodsWeekService.queryWeekStockByParams(map11);
//                if (depInventoryGoodsWeekEntities.size() > 0) {
//                    //在最后一条盘库记录记录添加损耗
//                    int index = depInventoryGoodsWeekEntities.size() - 1;
//                    GbDepInventoryGoodsWeekEntity goodsDailyEntity = depInventoryGoodsWeekEntities.get(index);
//                    BigDecimal dailyWeight = new BigDecimal(goodsDailyEntity.getGbIgwWeight());
//                    BigDecimal newWeight = dailyWeight.add(useWeight);
//                    BigDecimal newSubtotal = newWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    goodsDailyEntity.setGbIgwWeight(newWeight.toString());
//                    goodsDailyEntity.setGbIgwSubtotal(newSubtotal.toString());
//                    gbDepInventoryGoodsWeekService.update(goodsDailyEntity);
//                }
//
//                // 2, 更新depDisGoods
//                GbDepartmentGoodsStockReduceEntity reduceEntity = new GbDepartmentGoodsStockReduceEntity();
//
//                //3,add reduce
//                BigDecimal newCostSubtotal = useWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);
//                reduceEntity.setGbDgsrCostWeight(useWeight.toString());
//                reduceEntity.setGbDgsrCostSubtotal(newCostSubtotal.toString());
//                reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeProduce()); //
//                reduceEntity.setGbDgsrGbDistributerId(stock.getGbDgsGbDistributerId());
//                reduceEntity.setGbDgsrGbDepartmentId(stock.getGbDgsGbDepartmentId());
//                reduceEntity.setGbDgsrGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//                reduceEntity.setGbDgsrGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
//                reduceEntity.setGbDgsrGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
//                reduceEntity.setGbDgsrGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
//                reduceEntity.setGbDgsrGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
//                reduceEntity.setGbDgsrFullTime(formatFullTime());
//                reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReduceWeightUserId());
//                reduceEntity.setGbDgsrDate(formatWhatDay(0));
//                reduceEntity.setGbDgsrWeek(getWeekOfYear(0).toString());
//                reduceEntity.setGbDgsrMonth(formatWhatMonth(0));
//                gbDepartmentStockReduceService.save(reduceEntity);
//
//
//// 更新depDisGoods
//                subscribeDepDisGoodsTotal(useWeight, useSubtotal, stock.getGbDgsGbDepDisGoodsId());
//
//                //daily
//                Map<String, Object> map = new HashMap<>();
//                map.put("depId", stock.getGbDgsGbDepartmentId());
//                map.put("week", getWeekOfYear(0));
//
//                //修改日成本表数据
//                GbDepInventoryWeekEntity weekEntity = gbDepInventoryWeekService.queryInventoryWeek(map);
//                if (weekEntity != null) {
//                    //增加成本值
//                    BigDecimal bigDecimal = new BigDecimal(weekEntity.getGbDiwProduceTotal());
//                    BigDecimal add = bigDecimal.add(useSubtotal);
//                    weekEntity.setGbDiwProduceTotal(add.toString());
//
//                    gbDepInventoryWeekService.update(weekEntity);
//                }
//                stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
//                stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
//            }
//
//            stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
//            stock.setGbDgsInventoryDate(formatWhatDay(0));
//            stock.setGbDgsInventoryMonth(formatWhatMonth(0));
//            gbDepartmentGoodsStockService.update(stock);
//        }
//        return R.ok();
//    }
//
//
//    @RequestMapping(value = "/saveDepInventoryGoodsWeek", method = RequestMethod.POST)
//    @ResponseBody
//    public R saveDepInventoryGoodsWeek(@RequestBody List<GbDepartmentGoodsStockEntity> stockList) {
//        for (GbDepartmentGoodsStockEntity stock : stockList) {
//
//            if(!stock.getGbDgsInventoryWeight().equals(stock.getGbDgsRestWeight())){
//
//                BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight());
//                BigDecimal inventoryWeight = new BigDecimal(stock.getGbDgsInventoryWeight());
//                BigDecimal useWeight = restWeight.subtract(inventoryWeight);
//                BigDecimal price = new BigDecimal(stock.getGbDgsPrice());
//                BigDecimal useSubtotal = useWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP);
//
//                //1, reduce
//                GbDepartmentGoodsStockReduceEntity  reduceEntity = new GbDepartmentGoodsStockReduceEntity();
//                reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeProduce()); //
//                reduceEntity.setGbDgsrGbDistributerId(stock.getGbDgsGbDistributerId());
//                reduceEntity.setGbDgsrGbDepartmentId(stock.getGbDgsGbDepartmentId());
//                reduceEntity.setGbDgsrGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//                reduceEntity.setGbDgsrGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
//                reduceEntity.setGbDgsrGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
//                reduceEntity.setGbDgsrGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
//                reduceEntity.setGbDgsrGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
//                reduceEntity.setGbDgsrFullTime(formatFullTime());
//                reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReduceWeightUserId());
//                reduceEntity.setGbDgsrDate(formatWhatDay(0));
//                reduceEntity.setGbDgsrWeek(getWeekOfYear(0).toString());
//                reduceEntity.setGbDgsrMonth(formatWhatMonth(0));
//                reduceEntity.setGbDgsrCostWeight(useWeight.toString());
//                reduceEntity.setGbDgsrCostSubtotal(useSubtotal.toString());
//                gbDepartmentStockReduceService.save(reduceEntity);
//
//                // 更新depDisGoods
//                subscribeDepDisGoodsTotal(useWeight, useSubtotal,stock.getGbDgsGbDepDisGoodsId());
//
//                //2
//                Map<String, Object> map11 = new HashMap<>();
//                map11.put("stockId", stock.getGbDepartmentGoodsStockId());
//                map11.put("date", formatWhatDay(0));
//                map11.put("depId", stock.getGbDgsGbDepartmentId());
//                List<GbDepInventoryGoodsWeekEntity> depInventoryGoodsWeekEntities = gbDepInventoryGoodsWeekService.queryWeekStockByParams(map11);
//                if(depInventoryGoodsWeekEntities.size() > 0){
//                    //在最后一条盘库记录记录添加损耗
//                    int index = depInventoryGoodsWeekEntities.size() - 1;
//                    GbDepInventoryGoodsWeekEntity goodsWeekEntity = depInventoryGoodsWeekEntities.get(index);
//
//                    BigDecimal weekWeight = new BigDecimal(goodsWeekEntity.getGbIgwWeight());
//                    BigDecimal newWeight = weekWeight.add(useWeight);
//                    BigDecimal newSubtotal = newWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP);
//                    goodsWeekEntity.setGbIgwWeight(newWeight.toString());
//                    goodsWeekEntity.setGbIgwSubtotal(newSubtotal.toString());
//                    gbDepInventoryGoodsWeekService.update(goodsWeekEntity);
//                }else{
//                    GbDepInventoryGoodsWeekEntity goodsWeekEntity = new GbDepInventoryGoodsWeekEntity();
//                    goodsWeekEntity.setGbIgwDisGoodsId(stock.getGbDgsGbDisGoodsId());
//                    goodsWeekEntity.setGbIgwDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
//                    goodsWeekEntity.setGbIgwDepartmentId(stock.getGbDgsGbDepartmentId());
//                    goodsWeekEntity.setGbIgwDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//
//                    goodsWeekEntity.setGbIgwSubtotal(useSubtotal.toString());
//                    goodsWeekEntity.setGbIgwWeight(useWeight.toString());
//                    goodsWeekEntity.setGbIgwWeek(getWeek(0));
//                    goodsWeekEntity.setGbIgwGbDepStockId(stock.getGbDepartmentGoodsStockId());
//                    goodsWeekEntity.setGbIgwDistributerId(stock.getGbDgsGbDistributerId());
//                    goodsWeekEntity.setGbIgwDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
//                    goodsWeekEntity.setGbIgwFullTime(formatFullTime());
//                    goodsWeekEntity.setGbIgwWasteWeight("0");
//                    goodsWeekEntity.setGbIgwWasteSubtotal("0");
//                    goodsWeekEntity.setGbIgwLossWeight("0");
//                    goodsWeekEntity.setGbIgwLossSubtotal("0");
//                    goodsWeekEntity.setGbIgwStatus(0);
//                    gbDepInventoryGoodsWeekService.save(goodsWeekEntity);
//                }
//
//                //daily
//
//                Map<String, Object> map = new HashMap<>();
//                map.put("depId", stock.getGbDgsGbDepartmentId());
//                map.put("date", formatWhatDay(0));
//                //修改日成本表数据
//                GbDepInventoryWeekEntity weekEntity = gbDepInventoryWeekService.queryInventoryWeek(map);
//                if (weekEntity != null) {
//                    //增加成本值
//                    BigDecimal bigDecimal = new BigDecimal(weekEntity.getGbDiwProduceTotal());
//                    BigDecimal add = bigDecimal.add(useSubtotal);
//                    weekEntity.setGbDiwProduceTotal(add.toString());
//                    gbDepInventoryWeekService.update(weekEntity);
//                } else {
//                    //添加新成本
//                    GbDepInventoryWeekEntity weekEntity1 = new GbDepInventoryWeekEntity();
//                    weekEntity1.setGbDiwWeek(getWeek(0));
//                    weekEntity1.setGbDiwProduceTotal(useSubtotal.toString());
//                    weekEntity1.setGbDiwDepartmentId(stock.getGbDgsGbDepartmentId());
//                    weekEntity1.setGbDiwDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//                    weekEntity1.setGbDiwDistributerId(stock.getGbDgsGbDistributerId());
//                    weekEntity1.setGbDiwWeek(getWeek(0));
//                    weekEntity1.setGbDiwYear(formatWhatYear(0));
//                    weekEntity1.setGbDiwWasteTotal("0");
//                    weekEntity1.setGbDiwLossTotal("0");
//                    weekEntity1.setGbDiwReturnTotal("0");
//                    weekEntity1.setGbDiwStatus(0);
//                    gbDepInventoryWeekService.save(weekEntity1);
//                }
//
//                stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
//                stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
//            }
//
//            stock.setGbDgsInventoryDate(formatWhatDay(0));
//            stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
//            stock.setGbDgsInventoryWeek(getWeekOfYear(1).toString());
//            stock.setGbDgsInventoryMonth(formatWhatMonth(0));
//            gbDepartmentGoodsStockService.update(stock);
//        }
//        return R.ok();
//    }


    private void subscribeDepDisGoodsTotal(BigDecimal weight, BigDecimal subtotal, Integer depDisGoodsId) {

        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        BigDecimal weightB = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).subtract(weight);
        BigDecimal subtotalB = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).subtract(subtotal);
        depDisGoodsEntity.setGbDdgStockTotalSubtotal(subtotalB.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgStockTotalWeight(weightB.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        System.out.println("reeeeee" + depDisGoodsEntity.getGbDdgStockTotalWeight() + "rsusus" + depDisGoodsEntity.getGbDdgStockTotalSubtotal());
        depDisGoodsEntity.setGbDdgInventoryDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgInventoryFullTime(formatWhatFullTime(0));
        gbDepartmentDisGoodsService.update(depDisGoodsEntity);
    }

}
