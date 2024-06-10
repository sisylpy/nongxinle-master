package com.nongxinle.controller;

/**
 * @author lpy
 * @date 08-23 14:08
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

import javax.swing.plaf.basic.BasicIconFactory;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.getWeekDate;
import static com.nongxinle.utils.GbTypeUtils.*;


@RestController
@RequestMapping("api/gbdepinventorygoodsdaily")
public class GbDepInventoryGoodsDailyController {
    @Autowired
    private GbDepInventoryGoodsDailyService gbDepInvGoodsDailyService;
    @Autowired
    private GbDepInventoryGoodsWeekService gbDepInvGoodsWeekService;
    @Autowired
    private GbDepInventoryGoodsMonthService gbDepInvGoodsMonthService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private GbDistributerFatherGoodsService gbDisFatherGoodsService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDepInventoryDailyService gbDepInventoryDailyService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepartmentStockReduceService;


    @RequestMapping(value = "/getMendianCostTypeStaticsByGoodsFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R getMendianCostTypeStaticsByGoodsFatherId(Integer disId, String startDate, String stopDate,
                                                      String startWeek, String stopWeek, String year,
                                                      String startMonth, String stopMonth, Integer type, String ids, Integer goodsFatherId) {

        Map<String, Object> map = new HashMap<>();
        map.put("fathersFatherId", goodsFatherId);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities1 = gbDisFatherGoodsService.queryDisFathersGoodsByParamsGb(map);
        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoods = new TreeSet<>();
        if (fatherGoodsEntities1.size() > 0) {

            for (GbDistributerFatherGoodsEntity fatherGoods : fatherGoodsEntities1) {
                Integer gbDistributerFatherGoodsId = fatherGoods.getGbDistributerFatherGoodsId();

                //daily
                if (type.equals(getDISGoodsInventroyDaily())) {
                    Map<String, Object> map0 = new HashMap<>();

                    map0.put("disId", disId);
                    map0.put("goodsFatherId", gbDistributerFatherGoodsId);
                    if (!startDate.equals("-1")) {
                        map0.put("startDate", startDate);
                    }
                    if (!stopDate.equals("-1")) {
                        map0.put("notDayuStopDate", stopDate);
                    }
                    BigDecimal decimal = new BigDecimal(1);
                    if (!startDate.equals("-1") && !stopDate.equals("-1")) {
                        Integer howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
                        decimal = new BigDecimal(Integer.parseInt(howManyDaysInPeriod.toString()));
                    }
                    Integer integer = gbDepInvGoodsDailyService.queryDailyGoodsInventoryCount(map0);
                    if (integer > 0) {
                        map0.put("goodsFatherId", fatherGoods.getGbDistributerFatherGoodsId());
                        Double produceTotal = gbDepInvGoodsDailyService.queryDailyGoodsProduceTotal(map0);
                        Double produceTotalWaste = gbDepInvGoodsDailyService.queryDailyGoodsWasteTotal(map0);
                        Double produceTotalLoss = gbDepInvGoodsDailyService.queryDailyGoodsLossTotal(map0);
                        BigDecimal addTotal = new BigDecimal(produceTotal).add(new BigDecimal(produceTotalWaste)).add(new BigDecimal(produceTotalLoss));
                        BigDecimal everyDayWeight = addTotal.divide(decimal, 2, BigDecimal.ROUND_HALF_UP);
                        fatherGoods.setEveryDayWeight(everyDayWeight.doubleValue());
                        fatherGoods.setEveryDayWeightString(everyDayWeight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        System.out.println("dfakfjasdsafdas;fd" + map0);
                        TreeSet<GbDepInventoryGoodsDailyEntity> distributerFatherGoodsEntities = gbDepInvGoodsDailyService.queryTreeDailyDisGoodsList(map0);

                        TreeSet<GbDistributerGoodsEntity> dailyGoodsTree = new TreeSet<>();
                        List<GbDistributerGoodsEntity> distributerGoodsEntities = new ArrayList<>();

                        for (GbDepInventoryGoodsDailyEntity dailyEntity : distributerFatherGoodsEntities) {
                            Integer gbIgdDisGoodsId = dailyEntity.getGbIgdDisGoodsId();
                            GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(gbIgdDisGoodsId);
                            dailyGoodsTree.add(goodsEntity);
                        }
                        for (GbDistributerGoodsEntity goodsEntity : dailyGoodsTree) {
                            map0.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                            Double produceTotalGoods = gbDepInvGoodsDailyService.queryDailyGoodsProduceTotal(map0);
                            Double produceTotalWasteGoods = gbDepInvGoodsDailyService.queryDailyGoodsWasteTotal(map0);
                            Double produceTotalLossGoods = gbDepInvGoodsDailyService.queryDailyGoodsLossTotal(map0);
                            BigDecimal addTotalGoods = new BigDecimal(produceTotalGoods).add(new BigDecimal(produceTotalWasteGoods)).add(new BigDecimal(produceTotalLossGoods));
                            BigDecimal everyDayWeightGoods = addTotalGoods.divide(decimal, 2, BigDecimal.ROUND_HALF_UP);
                            goodsEntity.setEveryDayWeight(everyDayWeightGoods.doubleValue());
                            goodsEntity.setEveryDayWeightString(everyDayWeightGoods.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            distributerGoodsEntities.add(goodsEntity);
                        }
                        fatherGoods.setGbDistributerGoodsEntities(distributerGoodsEntities);
                        greatGrandGoods.add(fatherGoods);
                    }
                }


            }
            return R.ok().put("data", greatGrandGoods);
        }
        return R.ok();

    }


    @RequestMapping(value = "/getMendianCostTypeStatics", method = RequestMethod.POST)
    @ResponseBody
    public R getMendianCostTypeStatics(Integer disId, String startDate, String stopDate,
                                       String startWeek, String stopWeek, String year,
                                       String startMonth, String stopMonth, Integer type, String ids) {
        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoods = new TreeSet<>();
        TreeSet<GbDistributerFatherGoodsEntity> resultFatherGoodsList = new TreeSet<>();

        Map<String, Object> map0 = new HashMap<>();
        if (!startDate.equals("-1")) {
            map0.put("startDate", startDate);
        }
        if (!stopDate.equals("-1")) {
            map0.put("stopDate", stopDate);
        }
        map0.put("disId", disId);
        BigDecimal decimal = new BigDecimal(1);
        if (!startDate.equals("-1") && !stopDate.equals("-1") && !startDate.equals(stopDate)) {
            Integer howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
            decimal = new BigDecimal(Integer.parseInt(howManyDaysInPeriod.toString()));
        } else {
            decimal = new BigDecimal(1);
        }

        greatGrandGoods = gbDepInvGoodsDailyService.queryTreeDailyGoodsList(map0);
        if (greatGrandGoods.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoods) {
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    BigDecimal grandDouble = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        map0.put("goodsFatherId", father.getGbDistributerFatherGoodsId());
//                            Double produceTotal = gbDepInvGoodsDailyService.queryDailyGoodsProduceTotal(map0);
                        Double produceTotal = gbDepInvGoodsDailyService.queryDailyGoodsProduceWeight(map0);
//                            Double produceTotalWaste = gbDepInvGoodsDailyService.queryDailyGoodsWasteTotal(map0);
//                            Double produceTotalLoss = gbDepInvGoodsDailyService.queryDailyGoodsLossTotal(map0);
//                            BigDecimal addTotal = new BigDecimal(produceTotal).add(new BigDecimal(produceTotalWaste)).add(new BigDecimal(produceTotalLoss));
//                            BigDecimal everyDayWeight = addTotal.divide(decimal, 1, BigDecimal.ROUND_HALF_UP);
//                            BigDecimal everyDayWeight = new BigDecimal(produceTotal).divide(decimal, 1, BigDecimal.ROUND_HALF_UP);
                        father.setFatherProduceWeight(produceTotal);
                        father.setFatherProduceWeightString(new BigDecimal(produceTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                            father.setEveryDayWeight(everyDayWeight.doubleValue());
//                            father.setEveryDayWeightString(everyDayWeight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        grandDouble = grandDouble.add(new BigDecimal(produceTotal));


                    }
//                    grandFather.setFatherGoodsEntities(abcFatherGoodsProduceWeight(fatherGoodsEntities));
                    grandFather.setFatherProduceWeight(grandDouble.doubleValue());
                    grandFather.setFatherProduceWeightString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    resultFatherGoodsList.add(grandFather);
                    resultFatherGoodsList = abcFatherGoodsProduceWeight(resultFatherGoodsList);

                }

            }
        }

//
//        if (type.equals("waste")) {
//            mapCost.put("arr", abcFatherWaste(resultGrandFatherList));
//        }
//        map123.put("cost", mapCost);

        return R.ok().put("data", resultFatherGoodsList);
    }


    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherGoodsEveryMonthWeight(TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;

                if (o2.getEveryMonthWeight() - o1.getEveryMonthWeight() < 0) {
                    result = -1;
                } else if (o2.getEveryMonthWeight() - o1.getEveryMonthWeight() > 0) {
                    result = 1;
                } else {
                    result = 1;
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }

    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherGoodsEveryWeekWeight(TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;

                if (o2.getEveryWeekWeight() - o1.getEveryWeekWeight() < 0) {
                    result = -1;
                } else if (o2.getEveryWeekWeight() - o1.getEveryWeekWeight() > 0) {
                    result = 1;
                } else {
                    result = 1;
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }


    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherGoodsProduceWeight(TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;
                if (o2.getFatherProduceWeight() - o1.getFatherProduceWeight() < 0) {
                    result = -1;
                } else if (o2.getFatherProduceWeight() - o1.getFatherProduceWeight() > 0) {
                    result = 1;
                } else {
                    result = 1;
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }
//    @RequestMapping(value = "/saveDepInventoryStockDaily", method = RequestMethod.POST)
//    @ResponseBody
//    public R saveDepInventoryStockDaily (@RequestBody  GbDepartmentGoodsStockEntity stock) {
//        System.out.println("dailydailyddldlldldlyDaily");
//
//        if (!stock.getGbDgsInventoryWeight().equals(stock.getGbDgsRestWeight())) {
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
//            List<GbDepInventoryGoodsDailyEntity> depInventoryGoodsDailyEntities = gbDepInventoryGoodsDailyService.queryDailyStockByParams(map11);
//            if (depInventoryGoodsDailyEntities.size() > 0) {
//                //在最后一条盘库记录记录添加损耗
//                int index = depInventoryGoodsDailyEntities.size() - 1;
//                GbDepInventoryGoodsDailyEntity goodsDailyEntity = depInventoryGoodsDailyEntities.get(index);
//                BigDecimal dailyWeight = new BigDecimal(goodsDailyEntity.getGbIgdWeight());
//                BigDecimal newWeight = dailyWeight.add(useWeight);
//                BigDecimal newSubtotal = newWeight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
//                goodsDailyEntity.setGbIgdWeight(newWeight.toString());
//                goodsDailyEntity.setGbIgdSubtotal(newSubtotal.toString());
//                gbDepInventoryGoodsDailyService.update(goodsDailyEntity);
//            } else {
//
//                GbDepInventoryGoodsDailyEntity goodsDailyEntity = new GbDepInventoryGoodsDailyEntity();
//                goodsDailyEntity.setGbIgdDisGoodsId(stock.getGbDgsGbDisGoodsId());
//                goodsDailyEntity.setGbIgdDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
//                goodsDailyEntity.setGbIgdDepartmentId(stock.getGbDgsGbDepartmentId());
//                goodsDailyEntity.setGbIgdDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//                goodsDailyEntity.setGbIgdSubtotal(useSubtotal.toString());
//                goodsDailyEntity.setGbIgdWeight(useWeight.toString());
////                goodsDailyEntity.setGbIgdWeek(getWeekOfYear(0).toString());
////                goodsDailyEntity.setGbIgdYear(formatWhatYear(0));
//                goodsDailyEntity.setGbIgdWasteWeight("0");
//                goodsDailyEntity.setGbIgdWasteSubtotal("0");
//                goodsDailyEntity.setGbIgdLossWeight("0");
//                goodsDailyEntity.setGbIgdLossSubtotal("0");
//                goodsDailyEntity.setGbIgdReturnWeight("0");
//                goodsDailyEntity.setGbIgdReturnSubtotal("0");
//                goodsDailyEntity.setGbIgdGbDepStockId(stock.getGbDepartmentGoodsStockId());
//                goodsDailyEntity.setGbIgdDistributerId(stock.getGbDgsGbDistributerId());
//                goodsDailyEntity.setGbIgdDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
//                goodsDailyEntity.setGbIgdFullTime(formatFullTime());
//                goodsDailyEntity.setGbIgdStatus(0);
//                gbDepInventoryGoodsDailyService.save(goodsDailyEntity);
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
//            GbDepInventoryDailyEntity dailyEntity = gbDepInventoryDailyService.queryInventoryDaily(map);
//            if (dailyEntity != null) {
//                //增加成本值
//                BigDecimal bigDecimal = new BigDecimal(dailyEntity.getGbIdProduceTotal());
//                BigDecimal add = bigDecimal.add(useSubtotal);
//                dailyEntity.setGbIdTotal(add.toString());
//                gbDepInventoryDailyService.update(dailyEntity);
//
//            } else {
//                //添加新成本
//                GbDepInventoryDailyEntity dailyEntity1 = new GbDepInventoryDailyEntity();
//                dailyEntity1.setGbIdTotal(useSubtotal.toString());
//                dailyEntity1.setGbIdDepartmentId(stock.getGbDgsGbDepartmentId());
//                dailyEntity1.setGbIdDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//                dailyEntity1.setGbIdDistributerId(stock.getGbDgsGbDistributerId());
//                dailyEntity1.setGbIdWeek(getWeekOfYear(0).toString());
//                dailyEntity1.setGbIdYear(formatWhatYear(0));
//                dailyEntity1.setGbIdWasteTotal("0");
//                dailyEntity1.setGbIdLossTotal("0");
//                dailyEntity1.setGbIdReturnTotal("0");
//                dailyEntity1.setGbIdStatus(0);
//                gbDepInventoryDailyService.save(dailyEntity1);
//            }
//
//            stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
//            stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//        }
//
//        BigDecimal week = new BigDecimal(getWeekOfYear(0)).add(new BigDecimal("1"));
//        stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
//        stock.setGbDgsInventoryDate(formatWhatDay(1));
//        stock.setGbDgsInventoryMonth(formatWhatMonth(0));
//        stock.setGbDgsInventoryWeek(week.toString());
//        gbDepartmentGoodsStockService.update(stock);
//        return R.ok();
//    }


//    @RequestMapping(value = "/saveDepInventoryStockDaily1", method = RequestMethod.POST)
//    @ResponseBody
//    public R saveDepInventoryStockDaily1 (@RequestBody  GbDepartmentGoodsStockEntity stock) {
//        if(!stock.getGbDgsInventoryWeight().equals(stock.getGbDgsRestWeight())){
//
//            BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight());
//            BigDecimal inventoryWeight = new BigDecimal(stock.getGbDgsInventoryWeight());
//            BigDecimal useWeight = restWeight.subtract(inventoryWeight).setScale(2,BigDecimal.ROUND_HALF_DOWN);
//            BigDecimal price = new BigDecimal(stock.getGbDgsPrice());
//            BigDecimal useSubtotal = useWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP);
//
//            //1, update dailyGoods
//            Map<String, Object> map11 = new HashMap<>();
//            map11.put("stockId", stock.getGbDepartmentGoodsStockId());
//            map11.put("date", formatWhatDay(0));
//            map11.put("depId", stock.getGbDgsGbDepartmentId());
//            List<GbDepInventoryGoodsDailyEntity> depInventoryGoodsDailyEntities = gbDepInventoryGoodsDailyService.queryDailyStockByParams(map11);
//            if(depInventoryGoodsDailyEntities.size() > 0){
//                //在最后一条盘库记录记录添加损耗
//                int index = depInventoryGoodsDailyEntities.size() - 1;
//                GbDepInventoryGoodsDailyEntity goodsDailyEntity = depInventoryGoodsDailyEntities.get(index);
//
//                BigDecimal dailyWeight = new BigDecimal(goodsDailyEntity.getGbIgdWeight());
//                BigDecimal newWeight = dailyWeight.add(useWeight);
//                BigDecimal newSubtotal = newWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP);
//                goodsDailyEntity.setGbIgdWeight(newWeight.toString());
//                goodsDailyEntity.setGbIgdSubtotal(newSubtotal.toString());
//                gbDepInventoryGoodsDailyService.update(goodsDailyEntity);
//            }
//
//            // 2, 更新depDisGoods
//            subscribeDepDisGoodsTotal(useWeight, useSubtotal, stock.getGbDgsGbDepDisGoodsId());
//
//            // 3,update reduce
//            GbDepartmentGoodsStockReduceEntity reduceEntity = new GbDepartmentGoodsStockReduceEntity();
//            BigDecimal newCostSubtotal = useWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);
//            reduceEntity.setGbDgsrCostWeight(useWeight.toString());
//            reduceEntity.setGbDgsrCostSubtotal(newCostSubtotal.toString());
//            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeCost()); //
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
//            gbDepartmentStockReduceService.save(reduceEntity);
//
//
//            //daily
//            Map<String, Object> map = new HashMap<>();
//            map.put("depId", stock.getGbDgsGbDepartmentId());
//            map.put("date", formatWhatDay(0));
//            //修改日成本表数据
//            GbDepInventoryDailyEntity dailyEntity = gbDepInventoryDailyService.queryInventoryDaily(map);
//            if (dailyEntity != null) {
//                //增加成本值
//                BigDecimal bigDecimal = new BigDecimal(dailyEntity.getGbIdProduceTotal());
//                BigDecimal add = bigDecimal.add(useSubtotal);
//                dailyEntity.setGbIdTotal(add.toString());
//                gbDepInventoryDailyService.update(dailyEntity);
//            }
//
//            stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
//            stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
//        }
//
//        stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
//        stock.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
//        stock.setGbDgsInventoryMonth(formatWhatMonth(0));
//        gbDepartmentGoodsStockService.update(stock);
//        return R.ok();
//    }
//
//
//    @RequestMapping(value = "/mendianGetDailyInventoryDetail", method = RequestMethod.POST)
//    @ResponseBody
//    public R mendianGetDailyInventoryDetail(Integer depFatherId, String date) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("depFatherId", depFatherId);
//        map.put("date", date);
//        map.put("year", formatWhatYear(0));
//        List<GbDepInventoryGoodsDailyEntity> depInventoryGoodsDailyEntities = gbDepInventoryGoodsDailyService.queryDailyStockByParams(map);
//
//        return R.ok().put("data", depInventoryGoodsDailyEntities);
//    }


//    @RequestMapping(value = "/getDepStockDaily/{depId}")
//    @ResponseBody
//    public R getDepStockDaily(@PathVariable Integer depId) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("depId", depId);
//        List<GbDepInventoryGoodsDailyEntity> dailyEntities = gbDepInventoryGoodsDailyService.queryDailyStockByParams(map);
//        return R.ok().put("data", dailyEntities);
//    }


    //    @RequestMapping(value = "/updateDepInventoryGoodsDaily", method = RequestMethod.POST)
//    @ResponseBody
//    public R updateDepInventoryGoodsDaily(@RequestBody List<GbDepartmentGoodsStockEntity> stockList) {
//        for (GbDepartmentGoodsStockEntity stock : stockList) {
//
//            if(!stock.getGbDgsInventoryWeight().equals(stock.getGbDgsRestWeight())){
//
//                BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight());
//                BigDecimal inventoryWeight = new BigDecimal(stock.getGbDgsInventoryWeight());
//                BigDecimal useWeight = restWeight.subtract(inventoryWeight).setScale(2,BigDecimal.ROUND_HALF_DOWN);
//                BigDecimal price = new BigDecimal(stock.getGbDgsPrice());
//                BigDecimal useSubtotal = useWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP);
//
//                //1, update dailyGoods
//                Map<String, Object> map11 = new HashMap<>();
//                map11.put("stockId", stock.getGbDepartmentGoodsStockId());
//                map11.put("date", formatWhatDay(0));
//                map11.put("depId", stock.getGbDgsGbDepartmentId());
//                List<GbDepInventoryGoodsDailyEntity> depInventoryGoodsDailyEntities = gbDepInventoryGoodsDailyService.queryDailyStockByParams(map11);
//                if(depInventoryGoodsDailyEntities.size() > 0){
//                    //在最后一条盘库记录记录添加损耗
//                    int index = depInventoryGoodsDailyEntities.size() - 1;
//                    GbDepInventoryGoodsDailyEntity goodsDailyEntity = depInventoryGoodsDailyEntities.get(index);
//
//                    BigDecimal dailyWeight = new BigDecimal(goodsDailyEntity.getGbIgdWeight());
//                    BigDecimal newWeight = dailyWeight.add(useWeight);
//                    BigDecimal newSubtotal = newWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP);
//                    goodsDailyEntity.setGbIgdWeight(newWeight.toString());
//                    goodsDailyEntity.setGbIgdSubtotal(newSubtotal.toString());
//                    gbDepInventoryGoodsDailyService.update(goodsDailyEntity);
//                }
//
//                // 2, 更新depDisGoods
//                subscribeDepDisGoodsTotal(useWeight, useSubtotal, stock.getGbDgsGbDepDisGoodsId());
//
//                // 3,update reduce
//                GbDepartmentGoodsStockReduceEntity reduceEntity = new GbDepartmentGoodsStockReduceEntity();
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
//                //daily
//                Map<String, Object> map = new HashMap<>();
//                map.put("depId", stock.getGbDgsGbDepartmentId());
//                map.put("date", formatWhatDay(0));
//                //修改日成本表数据
//                GbDepInventoryDailyEntity dailyEntity = gbDepInventoryDailyService.queryInventoryDaily(map);
//                if (dailyEntity != null) {
//                    //增加成本值
//                    BigDecimal bigDecimal = new BigDecimal(dailyEntity.getGbIdProduceTotal());
//                    BigDecimal add = bigDecimal.add(useSubtotal);
//                    dailyEntity.setGbIdTotal(add.toString());
//                    gbDepInventoryDailyService.update(dailyEntity);
//                }
//
//                stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
//                stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
//            }
//
//            stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
//            stock.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
//            stock.setGbDgsInventoryMonth(formatWhatMonth(0));
//            gbDepartmentGoodsStockService.update(stock);
//        }
//        return R.ok();
//    }
//
//
//
    @RequestMapping(value = "/saveStockDepInventoryGoodsDaily", method = RequestMethod.POST)
    @ResponseBody
    public R saveStockDepInventoryGoodsDaily(@RequestBody List<GbDepartmentGoodsStockEntity> stockList) {
        for (GbDepartmentGoodsStockEntity stock : stockList) {


            BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight());
            BigDecimal inventoryWeight = new BigDecimal(stock.getGbDgsInventoryWeight());
            BigDecimal useWeight = restWeight.subtract(inventoryWeight);
            BigDecimal price = new BigDecimal(stock.getGbDgsPrice());
            BigDecimal useSubtotal = useWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);
            System.out.println("invenweight" + inventoryWeight);

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
            gbDepartmentStockReduceService.save(reduceEntity);
            System.out.println("savelosss" + reduceEntity);

            // 更新depDisGoods
            subscribeDepDisGoodsTotal(useWeight, useSubtotal, stock.getGbDgsGbDepDisGoodsId());
            stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
            stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

            stock.setGbDgsInventoryDate(formatWhatDay(1));
            stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
            stock.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
            stock.setGbDgsInventoryMonth(formatWhatMonth(0));
            gbDepartmentGoodsStockService.update(stock);
        }
        return R.ok();
    }

    @RequestMapping(value = "/saveDepInventoryGoodsDaily", method = RequestMethod.POST)
    @ResponseBody
    public R saveDepInventoryGoodsDaily(@RequestBody List<GbDepartmentGoodsStockEntity> stockList) {
        System.out.println("saveDepInventoryGoodsDailysaveDepInventoryGoodsDaily");
        for (GbDepartmentGoodsStockEntity stock : stockList) {


            if (!stock.getGbDgsInventoryWeight().equals(stock.getGbDgsRestWeight())) {

                BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight());
                BigDecimal inventoryWeight = new BigDecimal(stock.getGbDgsInventoryWeight());
                BigDecimal useWeight = restWeight.subtract(inventoryWeight);
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
                gbDepartmentStockReduceService.save(reduceEntity);

                // 更新depDisGoods
                subscribeDepDisGoodsTotal(useWeight, useSubtotal, stock.getGbDgsGbDepDisGoodsId());

//                //2
//                Map<String, Object> map11 = new HashMap<>();
//                map11.put("stockId", stock.getGbDepartmentGoodsStockId());
//                map11.put("date", formatWhatDay(0));
//                map11.put("depId", stock.getGbDgsGbDepartmentId());
//                List<GbDepInventoryGoodsDailyEntity> depInventoryGoodsDailyEntities = gbDepInvGoodsDailyService.queryDailyStockByParams(map11);
//                if(depInventoryGoodsDailyEntities.size() > 0){
//                    //在最后一条盘库记录记录添加损耗
//                    int index = depInventoryGoodsDailyEntities.size() - 1;
//                    GbDepInventoryGoodsDailyEntity goodsDailyEntity = depInventoryGoodsDailyEntities.get(index);
//
//                    BigDecimal dailyWeight = new BigDecimal(goodsDailyEntity.getGbIgdWeight());
//                    BigDecimal newWeight = dailyWeight.add(useWeight);
//                    BigDecimal newSubtotal = newWeight.multiply(price).setScale(2,BigDecimal.ROUND_HALF_UP);
//                    goodsDailyEntity.setGbIgdWeight(newWeight.toString());
//                    goodsDailyEntity.setGbIgdSubtotal(newSubtotal.toString());
//                    gbDepInvGoodsDailyService.update(goodsDailyEntity);
//                }else{
//                    GbDepInventoryGoodsDailyEntity goodsDailyEntity = new GbDepInventoryGoodsDailyEntity();
//                    goodsDailyEntity.setGbIgdDisGoodsId(stock.getGbDgsGbDisGoodsId());
//                    goodsDailyEntity.setGbIgdDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
//                    goodsDailyEntity.setGbIgdDepartmentId(stock.getGbDgsGbDepartmentId());
//                    goodsDailyEntity.setGbIgdDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//
//                    goodsDailyEntity.setGbIgdSubtotal(useSubtotal.toString());
//                    goodsDailyEntity.setGbIgdWeight(useWeight.toString());
//                    goodsDailyEntity.setGbIgdDate(formatWhatDay(0));
//                    goodsDailyEntity.setGbIgdGbDepStockId(stock.getGbDepartmentGoodsStockId());
//                    goodsDailyEntity.setGbIgdDistributerId(stock.getGbDgsGbDistributerId());
//                    goodsDailyEntity.setGbIgdDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
//                    goodsDailyEntity.setGbIgdFullTime(formatFullTime());
//                    goodsDailyEntity.setGbIgdWasteWeight("0");
//                    goodsDailyEntity.setGbIgdWasteSubtotal("0");
//                    goodsDailyEntity.setGbIgdLossWeight("0");
//                    goodsDailyEntity.setGbIgdLossSubtotal("0");
//                    goodsDailyEntity.setGbIgdStatus(0);
//                    gbDepInvGoodsDailyService.save(goodsDailyEntity);
//                }

                //daily

//                Map<String, Object> map = new HashMap<>();
//                map.put("depId", stock.getGbDgsGbDepartmentId());
//                map.put("date", formatWhatDay(0));
//                //修改日成本表数据
//                GbDepInventoryDailyEntity dailyEntity = gbDepInventoryDailyService.queryInventoryDaily(map);
//                if (dailyEntity != null) {
//                    //增加成本值
//                    BigDecimal bigDecimal = new BigDecimal(dailyEntity.getGbIdProduceTotal());
//                    BigDecimal add = bigDecimal.add(useSubtotal);
//                    dailyEntity.setGbIdTotal(add.toString());
//                    gbDepInventoryDailyService.update(dailyEntity);
//                } else {
//                    //添加新成本
//                    GbDepInventoryDailyEntity dailyEntity1 = new GbDepInventoryDailyEntity();
//                    dailyEntity1.setGbIdDate(formatWhatDay(0));
//                    dailyEntity1.setGbIdTotal(useSubtotal.toString());
//                    dailyEntity1.setGbIdDepartmentId(stock.getGbDgsGbDepartmentId());
//                    dailyEntity1.setGbIdDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
//                    dailyEntity1.setGbIdDistributerId(stock.getGbDgsGbDistributerId());
//                    dailyEntity1.setGbIdWeek(getWeek(0));
//                    dailyEntity1.setGbIdYear(formatWhatYear(0));
//                    dailyEntity1.setGbIdWasteTotal("0");
//                    dailyEntity1.setGbIdLossTotal("0");
//                    dailyEntity1.setGbIdReturnTotal("0");
//                    dailyEntity1.setGbIdStatus(0);
//                    gbDepInventoryDailyService.save(dailyEntity1);
//                }

                stock.setGbDgsRestWeight(stock.getGbDgsInventoryWeight());
                stock.setGbDgsRestSubtotal(inventoryWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }

            stock.setGbDgsInventoryDate(formatWhatDay(1));
            stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
            stock.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
            stock.setGbDgsInventoryMonth(formatWhatMonth(0));
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


}
