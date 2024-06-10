package com.nongxinle.controller;

/**
 * @author lpy
 * @date 10-22 17:52
 */

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.sun.tools.doclint.Entity.divide;


@RestController
@RequestMapping("api/gbdepartmentgoodsstockrecord")
public class GbDepartmentGoodsStockRecordController {

    @Autowired
    private GbDepartmentGoodsStockService gbDepGoodsStockService;

    @Autowired
    private GbDepartmentGoodsStockRecordService gbDepGoodsStockRecordService;
    @Autowired
    private GbDepartmentService gbDepartmentService;


    @RequestMapping(value = "/getEveryGoodsStockMany", method = RequestMethod.POST)
    @ResponseBody
    public R getEveryGoodsStockMany(Integer goodsFatherId, String startDate, String stopDate, String ids, Integer disId) {
        TreeSet<GbDistributerGoodsEntity> disGoodsEntities = new TreeSet<>();
        Map<String, Object> map0 = new HashMap<>();
        if (!startDate.equals("-1")) {
            map0.put("startDate", startDate);
        }
        if (!stopDate.equals("-1")) {
            map0.put("notDayuStopDate", stopDate);
        }
        map0.put("disGoodsFatherId", goodsFatherId);

        // stock
        List<GbDistributerGoodsEntity> fatherGoodsEntities1 = gbDepGoodsStockService.queryDisGoodsStockByParams(map0);
        if (fatherGoodsEntities1.size() > 0) {
            disGoodsEntities.addAll(fatherGoodsEntities1);
        }

        if (!startDate.equals("-1") && !stopDate.equals("-1")) {
            System.out.println("getMendianCostTypeStatics" + map0);
            List<GbDistributerGoodsEntity> stockRecordGoodsEntitiesList = gbDepGoodsStockRecordService.queryDisGoodsByParams(map0);
            if (stockRecordGoodsEntitiesList.size() > 0) {
                disGoodsEntities.addAll(stockRecordGoodsEntitiesList);
            }
        }

        if (disGoodsEntities.size() > 0) {
            for (GbDistributerGoodsEntity goodsEntity : disGoodsEntities) {
                map0.put("goodsId", goodsEntity.getGbDistributerGoodsId());
                BigDecimal totalMany = new BigDecimal(0);
                Integer stockCount = gbDepGoodsStockService.queryGoodsStockCount(map0);
                if (stockCount > 0) {
                    long timeStampTotal = gbDepGoodsStockService.queryGoodsStockTimeStamp(map0);
                    long averageTimeStamp = timeStampTotal / stockCount;
                    long nowTimeStamp = System.currentTimeMillis() / 1000;
                    long diffTime = nowTimeStamp - averageTimeStamp;
                    long diffDay = diffTime / 1000 / 60 / 60 / 24;
                    totalMany = new BigDecimal(diffDay).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                if (!startDate.equals("-1") && !stopDate.equals("-1")) {
                    Integer stockRecordCount = gbDepGoodsStockRecordService.queryGoodsStockRecordCount(map0);
                    if (stockRecordCount > 0) {
                        Double aDoubleStockRecord = gbDepGoodsStockRecordService.queryManyTotal(map0);
                        double averageStockRecordMany = aDoubleStockRecord / stockRecordCount;
                        totalMany = totalMany.add(new BigDecimal(averageStockRecordMany));
                    }
                }
                goodsEntity.setGoodsStockManyString(totalMany.toString());
            }
        }
        
        return R.ok().put("data", disGoodsEntities);
    }

    @RequestMapping(value = "/getMendianStockMany", method = RequestMethod.POST)
    @ResponseBody
    public R getMendianStockMany(Integer disId, String startDate, String stopDate, String ids, Integer inventoryType) {

        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoods = new TreeSet<>();

        Map<String, Object> map0 = new HashMap<>();
        map0.put("disId", disId);
        map0.put("inventoryType", inventoryType);
        if (!startDate.equals("-1")) {
            map0.put("startDate", startDate);
        }
        if (!stopDate.equals("-1")) {
            map0.put("notDayuStopDate", stopDate);
        }

        // stock
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities1 = gbDepGoodsStockService.queryDepStockDisFatherGoodsFather(map0);
        if (fatherGoodsEntities1.size() > 0) {
            greatGrandGoods.addAll(fatherGoodsEntities1);
        }

        if (!startDate.equals("-1") && !stopDate.equals("-1")) {
            System.out.println("getMendianCostTypeStatics" + map0);
            List<GbDistributerFatherGoodsEntity> greatGrandGoodsList = gbDepGoodsStockRecordService.queryDepStockRecordDisFatherGoodsFather(map0);
            if (greatGrandGoodsList.size() > 0) {
                greatGrandGoods.addAll(greatGrandGoodsList);
            }
        }

        if (greatGrandGoods.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoods) {
                BigDecimal greatGrandMany = new BigDecimal(0);
                BigDecimal greatGrandInteger = new BigDecimal(0);
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    BigDecimal grandMany = new BigDecimal(0);
                    BigDecimal grandInteger = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        map0.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());

                        BigDecimal totalMany = new BigDecimal(0);
                        Integer stockCount = gbDepGoodsStockService.queryGoodsStockCount(map0);
                        if (stockCount > 0) {
                            long timeStampTotal = gbDepGoodsStockService.queryGoodsStockTimeStamp(map0);
                            long averageTimeStamp = timeStampTotal / stockCount;
                            System.out.println("averageTimeStamp" + averageTimeStamp);
                            long nowTimeStamp = System.currentTimeMillis() / 1000;
                            System.out.println("nowTimeStampnowTimeStamp" + nowTimeStamp);
                            long diffTime = nowTimeStamp - averageTimeStamp;
                            System.out.println("diffTimediffTime" + diffTime);

                            long diffDay = diffTime / 60 / 60 / 24;
                            System.out.println("diffdaya===" + diffDay);
                            totalMany = new BigDecimal(diffDay).setScale(2, BigDecimal.ROUND_HALF_UP);
                            grandInteger = grandInteger.add(new BigDecimal(stockCount));
                            grandMany = grandMany.add(totalMany);
                        }

                        if (!startDate.equals("-1") && !stopDate.equals("-1")) {
                            Integer stockRecordCount = gbDepGoodsStockRecordService.queryGoodsStockRecordCount(map0);
                            if (stockRecordCount > 0) {
                                Double aDoubleStockRecord = gbDepGoodsStockRecordService.queryManyTotal(map0);
                                double averageStockRecordMany = aDoubleStockRecord / stockRecordCount;
                                totalMany = totalMany.add(new BigDecimal(averageStockRecordMany));
                                grandInteger = grandInteger.add(new BigDecimal(stockRecordCount));
                                grandMany = grandMany.add(totalMany);
                            }
                        }
                        father.setFatherStockManyString(totalMany.toString());

                    }
                    System.out.println("idididididttgrandIntegert" + grandInteger);
                    if (grandInteger.compareTo(BigDecimal.ZERO) == 1) {
                        BigDecimal divide = grandMany.divide(grandInteger, 2).setScale(2, BigDecimal.ROUND_HALF_UP);
                        grandFather.setFatherStockManyString(divide.toString());
                    }
                }
//                if (greatGrandInteger.compareTo(BigDecimal.ZERO) == 1) {
//                    BigDecimal divide = greatGrandMany.divide(greatGrandInteger, 2).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    greatGrandFather.setFatherStockManyString(divide.toString());
//
//                }
            }

        }


        return R.ok().put("data", greatGrandGoods);
    }





//    private TreeSet<GbDistributerFatherGoodsEntity> getFatherGoodsTotal
//            (TreeSet<GbDistributerFatherGoodsEntity> treeSet, Map<String, Object> map0, Integer totalType) {
//
//        for (GbDistributerFatherGoodsEntity greatGrandFather : treeSet) {
//            BigDecimal greatGrandTotal = new BigDecimal(0);
//            BigDecimal greatGrandTotalMany = new BigDecimal(0);
//            BigDecimal greatGrandTotalCount = new BigDecimal(0);
//
//            TreeSet<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
//            for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
//                BigDecimal grandDouble = new BigDecimal(0);
//                BigDecimal grandDoubleMany = new BigDecimal(0);
//                BigDecimal grandDoubleCount = new BigDecimal(0);
//                TreeSet<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
//                for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
//                    map0.put("fatherGoodsId", father.getGbDistributerFatherGoodsId());
//                    double add = 0.0;
//                    if (totalType.equals(0)) {
//                        Integer integer1 = gbDepGoodsStockRecordService.queryGoodsStockRecordCount(map0);
//                        if (integer1 > 0) {
//                            Double stockRecordTotal = gbDepGoodsStockRecordService.queryGoodsStockRecordSubtotal(map0);
//                            add = add + stockRecordTotal;
//                        }
//                        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
//                        if (integer > 0) {
//                            Double stockTotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map0);
//                            add = add + stockTotal;
//                        }
//
//                        father.setFatherStockTotal(add);
//                        grandDouble = grandDouble.add(new BigDecimal(add));
//                        greatGrandTotal = greatGrandTotal.add(new BigDecimal(add));
//                    }
//                    if (totalType.equals(1)) {
//                        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
//                        if (integer > 0) {
//                            Double stockTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(map0);
//                            add = add + stockTotal;
//                        }
//                        father.setFatherStockTotal(add);
//                        grandDouble = grandDouble.add(new BigDecimal(add));
//                        greatGrandTotal = greatGrandTotal.add(new BigDecimal(add));
//                    }
//
//                    if (totalType.equals(2)) {
//                        //库存使用时间
//                        Integer integerRecord = gbDepGoodsStockRecordService.queryGoodsStockRecordCount((map0));
//                        Double aDoubleMany = gbDepGoodsStockRecordService.queryManyTotal(map0);
//                        double average = aDoubleMany / integerRecord;
//                        father.setAverageManyTotal(new BigDecimal(Double.toString(average)).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
//
//                        grandDoubleMany = grandDoubleMany.add(new BigDecimal(aDoubleMany));
//                        grandDoubleCount = grandDoubleCount.add(new BigDecimal(integerRecord));
//                        greatGrandTotalMany = greatGrandTotalMany.add(new BigDecimal(aDoubleMany));
//                        greatGrandTotalCount = greatGrandTotalCount.add(new BigDecimal(integerRecord));
//                    }
//                }
//                if (totalType.equals(2)) {
//                    BigDecimal divide = grandDoubleMany.divide(grandDoubleCount, 2, BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    grandFather.setAverageManyTotal(divide.toString());
//                } else {
//                    grandFather.setFatherStockTotal(grandDouble.doubleValue());
//                }
//            }
//
//            if (totalType.equals(2)) {
//                BigDecimal divide = greatGrandTotalMany.divide(greatGrandTotalCount, 2, BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP);
//                greatGrandFather.setAverageManyTotal(divide.toString());
//            } else {
//                greatGrandFather.setFatherStockTotal(greatGrandTotal.doubleValue());
//            }
//        }
//
//        return abcFatherGoodsStockEvery(treeSet);
//    }



    private TreeSet<GbDistributerFatherGoodsEntity> getRecordFatherGoodsTreeSet(Map<String, Object> map0) {
        TreeSet<GbDistributerFatherGoodsEntity> stockGoodsEntities = new TreeSet<>();
        Integer integerStock = gbDepGoodsStockRecordService.queryGoodsStockRecordCount(map0);
        if (integerStock > 0) {
            stockGoodsEntities.addAll(gbDepGoodsStockRecordService.queryDepStockRecordDisFatherGoodsFather(map0));
        }
        return stockGoodsEntities;
    }

    private TreeSet<GbDistributerFatherGoodsEntity> getStockAndRecordFatherGoodsTreeSet(Map<String, Object> map0) {
        TreeSet<GbDistributerFatherGoodsEntity> totalGreatGrandGoodsList = new TreeSet<>();
        TreeSet<GbDistributerFatherGoodsEntity> totalGrandGoodsList = new TreeSet<>();
        TreeSet<GbDistributerFatherGoodsEntity> totalFatherGoodsList = new TreeSet<>();

//        if (type.equals(getDISGoodsInventroyDaily())) {

        //stock--goods

        System.out.println("fdajdfafa" + map0);
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
        if (integer > 0) {
            //todo 查一下是否inventorytype 起作用
            List<GbDistributerFatherGoodsEntity> stockGoodsEntities = gbDepGoodsStockService.queryDepStockDisFatherGoodsFather(map0);
            //greatGrand
            totalGreatGrandGoodsList.addAll(stockGoodsEntities);
            for (GbDistributerFatherGoodsEntity greatGrandFather : stockGoodsEntities) {
                //grand
                totalGrandGoodsList.addAll(greatGrandFather.getFatherGoodsEntities());
                for (GbDistributerFatherGoodsEntity father : greatGrandFather.getFatherGoodsEntities()) {
                    totalFatherGoodsList.addAll(father.getFatherGoodsEntities());
                }
            }
        }


        //stockRecord--goods
        Integer integerStock = gbDepGoodsStockRecordService.queryGoodsStockRecordCount(map0);
        if (integerStock > 0) {
            List<GbDistributerFatherGoodsEntity> stockGoodsEntities = gbDepGoodsStockRecordService.queryDepStockRecordDisFatherGoodsFather(map0);
            //greatGrand
            totalGreatGrandGoodsList.addAll(stockGoodsEntities);
            for (GbDistributerFatherGoodsEntity greatGrandFather : stockGoodsEntities) {
                //grand
                totalGrandGoodsList.addAll(greatGrandFather.getFatherGoodsEntities());
                for (GbDistributerFatherGoodsEntity father : greatGrandFather.getFatherGoodsEntities()) {
                    totalFatherGoodsList.addAll(father.getFatherGoodsEntities());
                }
            }
        }


//        for (GbDistributerFatherGoodsEntity greatGrand : totalGreatGrandGoodsList) {
//            List<GbDistributerFatherGoodsEntity> grandFatherList = new ArrayList<>();
//
//            for (GbDistributerFatherGoodsEntity grand : totalGrandGoodsList) {
//                List<GbDistributerFatherGoodsEntity> fffaaa = new ArrayList<>();
//
//                if (greatGrand.getGbDistributerFatherGoodsId().equals(grand.getGbDfgFathersFatherId())) {
//                    grandFatherList.add(grand);
//                }
//                for (GbDistributerFatherGoodsEntity father : totalFatherGoodsList) {
//                    if (father.getGbDfgFathersFatherId().equals(grand.getGbDistributerFatherGoodsId())) {
//                        fffaaa.add(father);
//                    }
//                    grand.setFatherGoodsEntities(fffaaa);
//                }
//            }
//            greatGrand.setFatherGoodsEntities(grandFatherList);
//        }
//        }


        return totalGreatGrandGoodsList;
    }


//		if (type.equals(getDISGoodsInventroyDaily())) {
//			Integer   integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
//			if (integer > 0) {
//				greatGrandGoods = gbDepGoodsStockService.queryDepStockDisFatherGoodsFather(map0);
//				for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoods) {
//					BigDecimal greatGrandTotal = new BigDecimal(0);
//					List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
//					for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
//						BigDecimal grandDouble = new BigDecimal(0);
//						List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
//						for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
//							Double produceTotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map0);
//							father.setFatherStockTotal(everyDayWeight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//							grandDouble = grandDouble.add(everyDayWeight);
//							greatGrandTotal = greatGrandTotal.add(everyDayWeight);
//						}
//						grandFather.setEveryDayWeight(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//					}
//					greatGrandFather.setEveryDayWeight(greatGrandTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//				}
//			}
//
//		}
//@RequestMapping(value = "/getMendianStockTypeStatics1", method = RequestMethod.POST)
//@ResponseBody
//public R getMendianStockTypeStatics1(Integer disId, String startDate, String stopDate, Integer type, String ids, Integer totalType) {
//    Map<String, Object> mapResult = new HashMap<>();
//
//    TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoods = new TreeSet<>();
//    Map<String, Object> map0 = new HashMap<>();
////        map0.put("startDate", startDate);
////        map0.put("notDayuStopDate", stopDate);
//    map0.put("disId", disId);
//    map0.put("inventoryType", type);
//    map0.put("dayuStatus", -1);
////		map0.put("mdIds", ids);
//
//    //1，stock, stockRecord 总入库金额总额
//    BigDecimal total = new BigDecimal(0);
//    Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
//    if (integer > 0) {
//        Double aDouble = gbDepGoodsStockService.queryDepGoodsSubtotal(map0);
//        total = total.add(new BigDecimal(aDouble));
//    }
//    Integer integerRecord = gbDepGoodsStockRecordService.queryGoodsStockRecordCount((map0));
//    if (integerRecord > 0) {
//        Double aDouble = gbDepGoodsStockRecordService.queryGoodsStockRecordSubtotal(map0);
//        total = total.add(new BigDecimal(aDouble));
//    }
//    if(total.compareTo(BigDecimal.ZERO) == 1){
//        mapResult.put("total", total);
//    }else {
//        mapResult.put("total", 0);
//    }
//
//
//    //2，stock 剩余金额总额
//    if (integer > 0) {
//        Double aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map0);
//        mapResult.put("restTotal", aDouble);
//    }else{
//        mapResult.put("restTotal", 0);
//    }
//
//    //3，stockRecord批次数量，many数量总数，批次数量/many总数=批次时间使用率
//    if (integerRecord > 0) {
//        Double aDoubleMany = gbDepGoodsStockRecordService.queryManyTotal(map0);
//        System.out.println("dkdkdkdkdkdmm" + aDoubleMany);
//        double average = aDoubleMany / integerRecord;
//        double averageWeek = aDoubleMany / integerRecord / 7;
//        double averageMonth = aDoubleMany / integerRecord / 30;
//        if(type.equals(1)){
//            mapResult.put("average", new BigDecimal(average).setScale(2, BigDecimal.ROUND_HALF_UP));
//        }else if(type.equals(2)){
//            mapResult.put("average", new BigDecimal(averageWeek).setScale(2, BigDecimal.ROUND_HALF_UP));
//        }else if(type.equals(3)){
//            mapResult.put("average", new BigDecimal(averageMonth).setScale(2, BigDecimal.ROUND_HALF_UP));
//        }
//
//    }else {
//        mapResult.put("average", 0);
//    }
//
//    //4,stock, stockRecord fathergoods 入库金额
//    if (totalType.equals(0)) {
//        greatGrandGoods = getStockAndRecordFatherGoodsTreeSet(map0);
//        getFatherGoodsTotal(greatGrandGoods, map0, totalType);
//
//    } else if (totalType.equals(1)) {
//        greatGrandGoods = getStockFatherGoodsTreeSet(map0);
//        getFatherGoodsTotal(greatGrandGoods, map0, totalType);
//    } else if (totalType.equals(2)) {
//        greatGrandGoods = getRecordFatherGoodsTreeSet(map0);
//        getFatherGoodsTotal(greatGrandGoods, map0, totalType);
//    }
//    mapResult.put("arr", greatGrandGoods);
//
//    //5, stock, stockRecord 最新入库fathergoods 入库金额
//    Map<String, Object> mapRen4 = new HashMap<>();
//    mapRen4.put("disId", disId);
//    mapRen4.put("inventoryType", type);
//    mapRen4.put("dayuStatus", -1);
//    if(type.equals(1)){
//        mapRen4.put("stopTime", formatWhatFullTime(-72));
//    } else if(type.equals(2)){
//        mapRen4.put("stopDate", formatWhatDay(-21));
//    }else if(type.equals(3)){
//        mapRen4.put("stopDate", formatWhatDay(-90));
//    }
//    TreeSet<GbDistributerFatherGoodsEntity> recentlyStockDayuThree = getStockGoodsFatherRestSubTotal(mapRen4);
//    mapResult.put("exceedThreeArr", recentlyStockDayuThree);
//
//    Map<String, Object> mapRen3 = new HashMap<>();
//    mapRen3.put("disId", disId);
//    mapRen3.put("inventoryType", type);
//    mapRen3.put("dayuStatus", -1);
//    if(type.equals(1)){
//        mapRen3.put("startTime", formatWhatFullTime(-72));
//        mapRen3.put("notDayuStopTime", formatWhatFullTime(-48));
//    } else if(type.equals(2)){
//        mapRen3.put("startDate", formatWhatDay(-21));
//        mapRen3.put("notDayuStopDate", formatWhatDay(-14));
//    }else if(type.equals(3)){
//        mapRen3.put("startDate", formatWhatDay(-90));
//        mapRen3.put("notDayuStopDate", formatWhatDay(-60));
//    }
//
//    System.out.println("maprenenennen33333333====" + mapRen3);
//    TreeSet<GbDistributerFatherGoodsEntity> recentlyStockThree = getStockGoodsFatherRestSubTotal(mapRen3);
//    System.out.println("aaaa" + recentlyStockThree.size());
//    mapResult.put("threeArr", recentlyStockThree);
//
//
//    Map<String, Object> mapRen2 = new HashMap<>();
//    mapRen2.put("disId", disId);
//    mapRen2.put("inventoryType", type);
//    mapRen2.put("dayuStatus", -1);
//
//    if(type.equals(1)){
//        mapRen2.put("startTime", formatWhatFullTime(-48));
//        mapRen2.put("notDayuStopTime", formatWhatFullTime(-24));
//    } else if(type.equals(2)){
//        mapRen2.put("startDate", formatWhatDay(-14));
//        mapRen2.put("notDayuStopDate", formatWhatDay(-7));
//    }else if(type.equals(3)){
//        mapRen2.put("startDate", formatWhatDay(-60));
//        mapRen2.put("notDayuStopDate", formatWhatDay(-30));
//    }
//    TreeSet<GbDistributerFatherGoodsEntity> recentlyStockTwo = getStockGoodsFatherRestSubTotal(mapRen2);
//    mapResult.put("twoArr", recentlyStockTwo);
//
//
//    Map<String, Object> mapRen1 = new HashMap<>();
//    mapRen1.put("disId", disId);
//    mapRen1.put("inventoryType", type);
//    mapRen1.put("dayuStatus", -1);
//    if(type.equals(1)){
//        mapRen1.put("startTime", formatWhatFullTime(-24));
//        mapRen1.put("notDayuStopTime", formatWhatFullTime(0));
//    } else if(type.equals(2)){
//        mapRen1.put("startDate", formatWhatDay(-7));
//        mapRen1.put("notDayuStopDate", formatWhatDay(0));
//    }else if(type.equals(3)){
//        mapRen1.put("startDate", formatWhatDay(-30));
//        mapRen1.put("notDayuStopDate", formatWhatDay(0));
//    }
//
//    System.out.println("ekkekekfkaejrlar;wlrwq" + mapRen1);
//    TreeSet<GbDistributerFatherGoodsEntity> recentlyStockOne = getStockGoodsFatherRestSubTotal(mapRen1);
//    mapResult.put("oneArr", recentlyStockOne);
//    return R.ok().put("data", mapResult);
//}


}
