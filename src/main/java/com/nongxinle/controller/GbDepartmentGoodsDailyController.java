package com.nongxinle.controller;

/**
 * @author lpy
 * @date 04-16 17:06
 */

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;


@RestController
@RequestMapping("api/gbdepartmentgoodsdaily")
public class GbDepartmentGoodsDailyController {

    @Autowired
    private GbDepartmentGoodsDailyService gbDepGoodsDailyService;
    @Autowired
    private GbDistributerGoodsService distributerGoodsService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepGoodsStockReduceService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;


    @RequestMapping(value = "/getFreshGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getFreshGoods(Integer disId, Integer searchDepId, String date, String showType) {
        System.out.println("shwotype" + showType);
        Map<String, Object> mapResult = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        if (showType.equals("day")) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", getGbDepartmentTypeMendian());
            map.put("disId", disId);
            List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
            for (GbDepartmentEntity departmentEntity : departmentEntities) {
                Map<String, Object> mapD = new HashMap<>();
                mapD.put("depId", departmentEntity.getGbDepartmentId());
                mapD.put("name", departmentEntity.getGbDepartmentName());

                Map<String, Object> mapSearch = new HashMap<>();
                mapSearch.put("date", date);
                mapSearch.put("depFatherId", departmentEntity.getGbDepartmentId());
                mapSearch.put("controlFresh", 1);
                List<GbDepartmentGoodsDailyEntity> departmentGoodsDailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListWithGoodsByParams(mapSearch);

                mapD.put("arr", departmentGoodsDailyEntities);
                list.add(mapD);

            }
        }
        if (showType.equals("dep")) {

            GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(searchDepId);
            Map<String, Object> mapD = new HashMap<>();
            mapD.put("depId", departmentEntity.getGbDepartmentId());
            mapD.put("name", departmentEntity.getGbDepartmentName());

            Map<String, Object> mapSearch = new HashMap<>();
            mapSearch.put("date", date);
            mapSearch.put("depFatherId", departmentEntity.getGbDepartmentId());
            mapSearch.put("controlFresh", 1);
            List<GbDepartmentGoodsDailyEntity> departmentGoodsDailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListWithGoodsByParams(mapSearch);

            mapD.put("arr", departmentGoodsDailyEntities);
            list.add(mapD);


        }

        return R.ok().put("data", list);
    }


    @RequestMapping(value = "/getDepClearGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getDepClearGoods(Integer searchDepId, String date) {
        Map<String, Object> mapResult = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(searchDepId);

        Map<String, Object> mapD = new HashMap<>();
        mapD.put("depId", departmentEntity.getGbDepartmentId());
        mapD.put("name", departmentEntity.getGbDepartmentName());

        Map<String, Object> mapSearch = new HashMap<>();
        mapSearch.put("date", date);
        mapSearch.put("depFatherId", departmentEntity.getGbDepartmentId());
        mapSearch.put("clear", -1);
        List<GbDepartmentGoodsDailyEntity> departmentGoodsDailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListWithGoodsByParams(mapSearch);

        mapD.put("arr", departmentGoodsDailyEntities);
        list.add(mapD);


        return R.ok().put("data", list);
    }


    @RequestMapping(value = "/getDepWasteGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getDepWasteGoods(Integer searchDepId, String startDate, String stopDate, Integer disGoodsId) {
        List<Map<String, Object>> list = new ArrayList<>();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(searchDepId);

        Map<String, Object> mapD = new HashMap<>();
        mapD.put("depId", departmentEntity.getGbDepartmentId());
        mapD.put("name", departmentEntity.getGbDepartmentName());

        Map<String, Object> mapSearch = new HashMap<>();
        mapSearch.put("startDate", startDate);
        mapSearch.put("stopDate", stopDate);
        mapSearch.put("disGoodsId", disGoodsId);
        mapSearch.put("depFatherId", departmentEntity.getGbDepartmentId());
        mapSearch.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        System.out.println("mapssserererere" + mapSearch);
        List<GbDepartmentGoodsDailyEntity> departmentGoodsDailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListWithReduceByParams(mapSearch);
        mapD.put("arr", departmentGoodsDailyEntities);
        list.add(mapD);

        return R.ok().put("data", list);
    }

    /**
     * 获取分析页面主数据
     *
     * @param disId     id
     * @param startDate 开始时间
     * @param stopDate  结束时间
     * @param type      "各种"
     * @param fenxiType "profit-利润， sales-数量"
     * @return 数据
     */
    @RequestMapping(value = "/disGetDepGoodsDailyTotal", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDepGoodsDailyTotal(Integer disId, String startDate, String stopDate, String type,
                                      String fenxiType, String searchDepIds) {

        Map<String, Object> depGoosDailyFenxi = new HashMap<>();
        if (fenxiType.equals("profitEcharts")) {
            depGoosDailyFenxi = getDepGoosDailyProfitFenxi(disId, startDate, stopDate, type, searchDepIds);
        }
        if (fenxiType.equals("weightEcharts")) {
            depGoosDailyFenxi = getDepGoosDailyWeightFenxi(disId, startDate, stopDate, type, searchDepIds);
        }

        if (fenxiType.equals("costEcharts")) {
            depGoosDailyFenxi = getDepGoosDailyCostFenxi(disId, startDate, stopDate, type, searchDepIds);
        }

        if (depGoosDailyFenxi.get("code").equals("0")) {
            return R.ok().put("data", depGoosDailyFenxi);

        } else {
            return R.error(-1, "没有数据");
        }

    }

    private Map<String, Object> getDepGoosDailyWeightFenxi(Integer disId, String startDate, String stopDate,
                                                           String type, String searchDepIds) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }


        List<String> dateList = new ArrayList<>();
        List<String> list = new ArrayList<>();

        double aDoutble = 0.0;
        double salesDouble = 0.0;
        double salesRate = 0.0;
        double wasteDouble = 0.0;
        double wasteRate = 0.0;
        double lossDouble = 0.0;
        double lossRate = 0.0;
        List<GbDistributerFatherGoodsEntity> greatGrandGoodsCost = new ArrayList<>();

        if (howManyDaysInPeriod > 0) {

            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                // disGoods
                Map<String, Object> mapDisGoods = new HashMap<>();
                mapDisGoods.put("date", whichDay);
                mapDisGoods.put("disId", disId);
//                if (searchDepId != -1) {
//                    mapDisGoods.put("depId", searchDepId);
//                }
                if (!searchDepIds.equals("-1")) {
                    String[] arrGb = searchDepIds.split(",");
                    List<String> idsGb = new ArrayList<>();
                    for (String idGb : arrGb) {
                        idsGb.add(idGb);
                        if (idsGb.size() > 0) {
                            mapDisGoods.put("depFatherIds", idsGb);
                        }
                    }
                }

                if (type.equals("sales")) {
                    mapDisGoods.put("produce", 0);
                }

                if (type.equals("loss")) {
                    mapDisGoods.put("loss", 0);
                }
                if (type.equals("waste")) {
                    mapDisGoods.put("waste", 0);
                }
                Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);

                if (count > 0) {
                    Double aDouble = 0.0;
                    if (type.equals("cost")) {
                        Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDisGoods);
                        Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDisGoods);
                        Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDisGoods);
                        aDouble = aDoubleP + aDoubleL + aDoubleW;

                    }
                    if (type.equals("sales")) {
                        aDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDisGoods);
                    }
                    if (type.equals("loss")) {
                        aDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDisGoods);
                    }
                    if (type.equals("waste")) {
                        aDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDisGoods);
                    }
                    list.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                } else {
                    list.add("0");
                }

            }
        } else {
            // dateList
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            // disGoods
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("disId", disId);

            if (!searchDepIds.equals("-1")) {
                String[] arrGb = searchDepIds.split(",");
                List<String> idsGb = new ArrayList<>();
                for (String idGb : arrGb) {
                    idsGb.add(idGb);
                    if (idsGb.size() > 0) {
                        mapDisGoods.put("depFatherIds", idsGb);
                    }
                }
            }
            if (type.equals("sales")) {
                mapDisGoods.put("produce", 0);
            }

            if (type.equals("loss")) {
                mapDisGoods.put("loss", 0);
            }
            if (type.equals("waste")) {
                mapDisGoods.put("waste", 0);
            }
            Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
            if (count > 0) {
                Double aDouble = 0.0;
                if (type.equals("cost")) {
                    Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDisGoods);
                    Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDisGoods);
                    Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDisGoods);
                    aDouble = aDoubleP + aDoubleL + aDoubleW;

                }
                if (type.equals("sales")) {
                    aDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDisGoods);
                }
                if (type.equals("loss")) {
                    aDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDisGoods);
                }
                if (type.equals("waste")) {
                    aDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDisGoods);
                }
                list.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            } else {
                list.add("0");
            }
        }


        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntitiesIds = gbDepartmentService.queryGroupDepsByDisId(mapDep);
        List<String> ids = new ArrayList<>();
        if (departmentEntitiesIds.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntitiesIds) {
                ids.add(dep.getGbDepartmentId().toString());
            }
        } else {
            return R.error(-1, "没有门店");
        }
        Map<String, Object> map1222 = new HashMap<>();
        map1222.put("disId", disId);
        if (howManyDaysInPeriod > 0) {
            map1222.put("startDate", startDate);
            map1222.put("stopDate", stopDate);
        } else {
            map1222.put("date", stopDate);
        }
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            List<String> idsGb = new ArrayList<>();
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map1222.put("depFatherIds", idsGb);
                }
            }
        }
        if (type.equals("sales")) {
            map1222.put("produce", 0);
        }
        if (type.equals("loss")) {
            map1222.put("loss", 0);
        }
        if (type.equals("waste")) {
            map1222.put("waste", 0);
        }
        Integer count122 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map1222);

        if (count122 > 0) {
            if (type.equals("cost")) {
                salesDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map1222);
                lossDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map1222);
                wasteDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map1222);
                aDoutble = salesDouble + lossDouble + wasteDouble;
                if (salesDouble > 0) {
                    salesRate = (salesDouble / aDoutble) * 100;
                }
                if (lossDouble > 0) {
                    lossRate = (lossDouble / aDoutble) * 100;
                }
                if (wasteDouble > 0) {
                    wasteRate = (wasteDouble / aDoutble) * 100;
                }

            }
            if (type.equals("sales")) {
                salesDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map1222);

            }
            if (type.equals("loss")) {
                lossDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map1222);
            }
            if (type.equals("waste")) {
                wasteDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map1222);
            }
            double greatGrandDouble = 0.0;
            System.out.println("112133131313123" + map1222);
            greatGrandGoodsCost = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map1222);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsCost) {
                List<GbDistributerFatherGoodsEntity> grandEntities = greatGrandFather.getFatherGoodsEntities();

                for (GbDistributerFatherGoodsEntity grandFather : grandEntities) {
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    double grandDouble = 0.0;

                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Double fatherDouble = 0.0;
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map1222.put("disGoodsFatherId", gbDistributerFatherGoodsId);
                        if (type.equals("cost")) {
                            Double fatherDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map1222);
                            Double fatherDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map1222);
                            Double fatherDoubleS = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map1222);
                            fatherDouble = fatherDoubleP + fatherDoubleL + fatherDoubleS;
                            System.out.println("fathrhrhthehfatherDouble" + fatherDouble);
                            father.setFatherCostWeight(fatherDouble);
                            father.setFatherCostWeightString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        }
                        if (type.equals("sales")) {
                            System.out.println("salllelelele" + map1222);
                            fatherDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map1222);
                            father.setFatherSellingSubtotal(fatherDouble);
                            father.setFatherSellingSubtotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        }
                        if (type.equals("loss")) {
                            fatherDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map1222);
                            father.setFatherLossTotal(fatherDouble);
                            father.setFatherLossTotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        }
                        if (type.equals("waste")) {
                            fatherDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map1222);
                            father.setFatherWasteTotal(fatherDouble);
                            father.setFatherWasteTotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        }
                        grandDouble = grandDouble + fatherDouble;
                    }

                    if (type.equals("cost")) {
                        System.out.println("cososssoosoosososogranddddgrandDouble" + grandDouble);
//                        grandFather.setFatherGoodsEntities(abcFatherCost(fatherGoodsEntities));
                        grandFather.setFatherCostWeight(grandDouble);
                        grandFather.setFatherCostWeightString(new BigDecimal(grandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("sales")) {
//                        grandFather.setFatherGoodsEntities(abcFatherSales(fatherGoodsEntities));
                        grandFather.setFatherSellingSubtotal(grandDouble);
                        grandFather.setFatherSellingSubtotalString(new BigDecimal(grandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("loss")) {
//                        grandFather.setFatherGoodsEntities(abcFatherLoss(fatherGoodsEntities));
                        grandFather.setFatherLossTotal(grandDouble);
                        grandFather.setFatherLossTotalString(new BigDecimal(grandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("waste")) {
//                        grandFather.setFatherGoodsEntities(abcFatherWaste(fatherGoodsEntities));
                        grandFather.setFatherWasteTotal(grandDouble);
                        grandFather.setFatherWasteTotalString(new BigDecimal(grandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    greatGrandDouble = greatGrandDouble + grandDouble;
                }


                if (type.equals("cost")) {
//                    greatGrandFather.setFatherGoodsEntities(abcFatherCost(grandEntities));
                    greatGrandFather.setFatherCostWeight(greatGrandDouble);
                    greatGrandFather.setFatherCostWeightString(new BigDecimal(greatGrandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("sales")) {
//                    greatGrandFather.setFatherGoodsEntities(abcFatherSales(grandEntities));
                    greatGrandFather.setFatherSellingSubtotal(greatGrandDouble);
                    greatGrandFather.setFatherSellingSubtotalString(new BigDecimal(greatGrandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("loss")) {
//                    greatGrandFather.setFatherGoodsEntities(abcFatherLoss(grandEntities));
                    greatGrandFather.setFatherLossTotal(greatGrandDouble);
                    greatGrandFather.setFatherLossTotalString(new BigDecimal(greatGrandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("waste")) {
//                    greatGrandFather.setFatherGoodsEntities(abcFatherWaste(grandEntities));
                    greatGrandFather.setFatherWasteTotal(greatGrandDouble);
                    greatGrandFather.setFatherWasteTotalString(new BigDecimal(greatGrandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }

        } else {
            return R.error(-1, "meiyoushuju");
        }


        Map<String, Object> mapCost = new HashMap<>();
        mapCost.put("date", dateList);
        mapCost.put("list", list);
        mapCost.put("total", String.format("%.1f", aDoutble));
        mapCost.put("salesTotal", String.format("%.1f", salesDouble));
        mapCost.put("salesRate", String.format("%.2f", salesRate));
        mapCost.put("lossRate", String.format("%.2f", lossRate));
        mapCost.put("wasteRate", String.format("%.2f", wasteRate));
        mapCost.put("lossTotal", String.format("%.1f", lossDouble));
        mapCost.put("wasteTotal", String.format("%.1f", wasteDouble));
        mapCost.put("arr", greatGrandGoodsCost);

//        if (type.equals("cost")) {
//            mapCost.put("arr", greatGrandGoodsCost);
//        }
//        if (type.equals("sales")) {
//            mapCost.put("arr", abcFatherSales(greatGrandGoodsCost));
//        }
//        if (type.equals("loss")) {
//            mapCost.put("arr", abcFatherLoss(greatGrandGoodsCost));
//        }
//        if (type.equals("waste")) {
//            mapCost.put("arr", abcFatherWaste(greatGrandGoodsCost));
//        }

        mapCost.put("code", "0");
        return mapCost;
    }

    private Map<String, Object> getDepGoosDailyCostFenxi(Integer disId, String startDate, String stopDate,
                                                         String type, String searchDepIds) {

        System.out.println("costutuutititit");
        Map<String, Object> mapCost = new HashMap<>();
        //
        double aDoutble = 0.0;
        Double saleDouble = 0.0;
        Double wasteDouble = 0.0;
        Double lossDouble = 0.0;
        Integer howManyDaysInPeriod = 0;

        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("depType", getGbDepartmentTypeMendian());

        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            List<String> idsGb = new ArrayList<>();
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    mapDep.put("depFatherIds", idsGb);
                }
            }
        }
        mapDep.put("dayuStatus", -1);
        if (howManyDaysInPeriod > 0) {
            mapDep.put("startDate", startDate);
            mapDep.put("stopDate", stopDate);
        } else {
            mapDep.put("date", startDate);
        }
        if (type.equals("sales")) {
            mapDep.put("produce", 0);
        }
        if (type.equals("loss")) {
            mapDep.put("loss", 0);
        }
        if (type.equals("waste")) {
            mapDep.put("waste", 0);
        }


        System.out.println("mapdeeepee" + mapDep);
        Integer count = gbDepartmentGoodsStockService.queryGoodsStockCount(mapDep);
        if (count > 0) {
            if (type.equals("total")) {
                lossDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDep);
                wasteDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDep);
                saleDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDep);
                aDoutble = saleDouble + lossDouble + wasteDouble;
            }
            if (type.equals("sales")) {
                saleDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDep);
            }
            if (type.equals("loss")) {
                lossDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDep);

            }
            if (type.equals("waste")) {
                wasteDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDep);
            }

            System.out.println("abcccccc" + mapDep);

            List<GbDistributerFatherGoodsEntity> greatGrandGoods = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(mapDep);
            for (GbDistributerFatherGoodsEntity great : greatGrandGoods) {
                System.out.println("granfa" + great.getGbDfgFatherGoodsName());
                double greatTotal = 0.0;
                List<GbDistributerFatherGoodsEntity> grandGoods = great.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grand : grandGoods) {
                    double grandTotal = 0.0;
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for(GbDistributerFatherGoodsEntity father : fatherGoodsEntities){
                        double add = 0.0;
                        mapDep.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDep);

                        if (integer > 0) {
                            if (type.equals("total")) {
                                Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDep);
                                Double wasteTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDep);
                                Double lossTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDep);

                                add = add + stockTotal + wasteTotal + lossTotal;
                                System.out.println("resusususss" + add);
                            }
                            if (type.equals("sales")) {
                                Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDep);
                                add = add + stockTotal;
                                System.out.println("resusususss" + add);
                            }
                            if (type.equals("loss")) {
                                Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDep);
                                add = add + stockTotal;
                            }
                            if (type.equals("waste")) {
                                Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDep);
                                add = add + stockTotal;
                            }

                        }
                        father.setFatherStockTotal(add);
                        father.setFatherStockTotalString(new BigDecimal(add).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        grandTotal = grandTotal + add;
                        greatTotal = greatTotal + add;
                    }
                    grand.setFatherStockTotal(grandTotal);
                    grand.setFatherStockTotalString(new BigDecimal(grandTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }

                great.setFatherStockTotal(greatTotal);
                great.setFatherStockTotalString(new BigDecimal(greatTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
                mapCost.put("arr", greatGrandGoods);
//                mapCost.put("list", greatGrandGoods);

        }

//            if (showType.equals("dep")) {
//                departmentEntityTreeSet = gbDepartmentGoodsStockService.queryDepGoodsTreeDepartments(mapDep);
//                if (departmentEntityTreeSet.size() > 0) {
//                    for (GbDepartmentEntity gbDepartmentEntity : departmentEntityTreeSet) {
//                        Map<String, Object> map1222 = new HashMap<>();
//                        if (howManyDaysInPeriod > 0) {
//                            map1222.put("startDate", startDate);
//                            map1222.put("stopDate", stopDate);
//                        } else {
//                            map1222.put("date", startDate);
//                        }
//                        map1222.put("depFatherId", gbDepartmentEntity.getGbDepartmentId());
//                        if (searchDepId != -1) {
//                            map1222.put("fromDepId", searchDepId);
//                        }
//                        if (type.equals("sales")) {
//                            map1222.put("produce", 0);
//                        }
//                        if (type.equals("loss")) {
//                            map1222.put("loss", 0);
//                        }
//                        if (type.equals("waste")) {
//                            map1222.put("waste", 0);
//                        }
//
////                        Integer count122 = gbDepartmentGoodsStockService.queryGoodsStockCount(map1222);
//                        Integer count122  =  gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDep);
//
//                        double depCostSubtotal = 0.0;
//                        if (count122 > 0) {
//                            if (type.equals("total")) {
//                                System.out.println("cosottttt" + map1222);
//                                double loss   =  gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDep);
//                                double waste   =  gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDep);
//                                double sale   =  gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDep);
//                                depCostSubtotal = sale + loss + waste;
//                            }
//
//                            if (type.equals("sales")) {
//                                depCostSubtotal   =  gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDep);
//                            }
//                            if (type.equals("loss")) {
//                                depCostSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map1222);
//
//                            }
//                            if (type.equals("waste")) {
//                                depCostSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map1222);
//                            }
//                        }
//                        gbDepartmentEntity.setDepCostGoodsTotal(depCostSubtotal);
//                        gbDepartmentEntity.setDepCostGoodsTotalString(new BigDecimal(depCostSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    }
//
//                }
//
//                List<GbDepartmentEntity> list = new ArrayList<>();
//                System.out.println("depwastssewwwww" + mapDep);
//                List<GbDepartmentEntity> departmentEntitiess = gbDepartmentGoodsStockService.queryStockDepWithFatherGoods(mapDep);
//                if (departmentEntitiess.size() > 0) {
//                    for (GbDepartmentEntity gbDepartmentEntity : departmentEntitiess) {
//                        Map<String, Object> map1222 = new HashMap<>();
//                        map1222.put("depFatherId", gbDepartmentEntity.getGbDepartmentId());
//                        if (howManyDaysInPeriod > 0) {
//                            map1222.put("startDate", startDate);
//                            map1222.put("stopDate", stopDate);
//                        } else {
//                            map1222.put("date", startDate);
//                        }
//                        map1222.put("depFatherId", gbDepartmentEntity.getGbDepartmentId());
//                        if (searchDepId != -1) {
//                            map1222.put("fromDepId", searchDepId);
//                        }
//                        if (type.equals("sales")) {
//                            map1222.put("produce", 0);
//                        }
//                        if (type.equals("loss")) {
//                            map1222.put("loss", 0);
//                        }
//                        if (type.equals("waste")) {
//                            map1222.put("waste", 0);
//                        }
//                        double depTotal = 0.0;
//
//                        List<GbDistributerFatherGoodsEntity> greatGrandGoods = gbDepartmentEntity.getFatherGoodsEntities();
//                        for (GbDistributerFatherGoodsEntity grandFather : greatGrandGoods) {
//                            System.out.println("granfa" + grandFather.getGbDfgFatherGoodsName());
//                            double grandTotal = 0.0;
//                            List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
//                            for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
//                                double add = 0.0;
//                                map1222.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
//                                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map1222);
//
//                                if (integer > 0) {
//                                    if (type.equals("total")) {
//                                        System.out.println("abccc" + map1222);
//                                        Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map1222);
//                                        Double wasteTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map1222);
//                                        Double lossTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map1222);
//
//                                        add = add + stockTotal + wasteTotal + lossTotal;
//                                        System.out.println("resusususss" + add);
//                                    }
//                                    if (type.equals("sales")) {
//                                        Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map1222);
//                                        add = add + stockTotal;
//                                        System.out.println("resusususss" + add);
//                                    }
//                                    if (type.equals("loss")) {
//                                        Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map1222);
//                                        add = add + stockTotal;
//                                    }
//                                    if (type.equals("waste")) {
//                                        Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map1222);
//                                        add = add + stockTotal;
//                                    }
//
//                                }
//                                father.setFatherStockTotal(add);
//                                father.setFatherStockTotalString(new BigDecimal(add).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                                grandTotal = grandTotal + add;
//                            }
//
//                            grandFather.setFatherStockTotal(grandTotal);
//                            grandFather.setFatherStockTotalString(new BigDecimal(grandTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                            depTotal = depTotal + grandTotal;
//                        }
//                        gbDepartmentEntity.setDepStockSubtotal(depTotal);
//                        gbDepartmentEntity.setDepStockSubtotalString(new BigDecimal(depTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        list.add(gbDepartmentEntity);
//                    }
//
//                }
//
//                mapCost.put("list", list);
//                mapCost.put("arr", abcDepCostSubtotal(departmentEntityTreeSet));
//            }
//            if (showType.equals("costType")) {
//                Map<String, Object> map = new HashMap<>();
//                Map<String, Object> map1222 = new HashMap<>();
//                if (howManyDaysInPeriod > 0) {
//                    map1222.put("startDate", startDate);
//                    map1222.put("stopDate", stopDate);
//                } else {
//                    map1222.put("date", startDate);
//                }
//                if (searchDepId != -1) {
//                    map1222.put("fromDepId", searchDepId);
//                }
//                map1222.put("depType", getGbDepartmentTypeMendian());
//
//                Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map1222);
//                Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map1222);
//                Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map1222);
//                double total = lossSubtotal + wasteDouble + produceSubtotal;
//                BigDecimal produceP = new BigDecimal(0);
//                BigDecimal produceL = new BigDecimal(0);
//                BigDecimal produceW = new BigDecimal(0);
//                if (total > 0) {
//                    produceP = new BigDecimal(produceSubtotal).divide(new BigDecimal(total), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    produceL = new BigDecimal(lossSubtotal).divide(new BigDecimal(total), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    produceW = new BigDecimal(wasteSubtotal).divide(new BigDecimal(total), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//                }
//                map.put("produce", new BigDecimal(produceSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                map.put("loss", new BigDecimal(lossSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                map.put("waste", new BigDecimal(wasteSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                map.put("producePercent", produceP);
//                map.put("lossPercent", produceL);
//                map.put("wastePercent", produceW);
//
//                map1222.put("depType", getGbDepartmentTypeMendian());
//                TreeSet<GbDepartmentEntity> departmentEntities = gbDepartmentGoodsStockService.queryDepGoodsTreeDepartments(map1222);
//                if (departmentEntities.size() > 0) {
//                    double depCost = 0.0;
//                    for (GbDepartmentEntity departmentEntity : departmentEntities) {
//                        Map<String, Object> mapDepType = new HashMap<>();
//                        if (howManyDaysInPeriod > 0) {
//                            mapDepType.put("startDate", startDate);
//                            mapDepType.put("stopDate", stopDate);
//                        } else {
//                            mapDepType.put("date", startDate);
//                        }
//                        if (searchDepId != -1) {
//                            mapDepType.put("fromDepId", searchDepId);
//                        }
//                        mapDepType.put("depFatherId", departmentEntity.getGbDepartmentId());
//                        Double produceSubtotal1 = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDepType);
//                        departmentEntity.setDepProduceGoodsTotal(produceSubtotal1);
//                        departmentEntity.setDepProduceGoodsTotalString(new BigDecimal(produceSubtotal1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                        Double lossSubtotal1 = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDepType);
//                        departmentEntity.setDepLossGoodsTotal(lossSubtotal1);
//                        departmentEntity.setDepLossGoodsTotalString(new BigDecimal(lossSubtotal1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        Double wasteSubtotal1 = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDepType);
//                        departmentEntity.setDepWasteGoodsTotal(wasteSubtotal1);
//                        departmentEntity.setDepWasteGoodsTotalString(new BigDecimal(wasteSubtotal1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        BigDecimal divideP = new BigDecimal(0);
//                        BigDecimal divideL = new BigDecimal(0);
//                        BigDecimal divideW = new BigDecimal(0);
//                        if (produceSubtotal > 0) {
//                            divideP = new BigDecimal(produceSubtotal1).divide(new BigDecimal(produceSubtotal), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//                        }
//                        if (lossSubtotal > 0) {
//                            divideL = new BigDecimal(lossSubtotal1).divide(new BigDecimal(lossSubtotal), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//                        }
//                        if (wasteSubtotal > 0) {
//                            divideW = new BigDecimal(wasteSubtotal1).divide(new BigDecimal(wasteSubtotal), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//                        }
//                        departmentEntity.setDepProduceGoodsPercent(divideP.toString());
//                        departmentEntity.setDepLossGoodsPercent(divideL.toString());
//                        departmentEntity.setDepWasteGoodsPercent(divideW.toString());
//
//                        depCost = produceSubtotal1 + lossSubtotal1 + wasteSubtotal1;
//                        BigDecimal dividePD = new BigDecimal(0);
//                        BigDecimal divideLD = new BigDecimal(0);
//                        BigDecimal divideWD = new BigDecimal(0);
//                        if (depCost > 0) {
//                            dividePD = new BigDecimal(produceSubtotal1).divide(new BigDecimal(depCost), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//                            divideLD = new BigDecimal(lossSubtotal1).divide(new BigDecimal(depCost), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//                            divideWD = new BigDecimal(wasteSubtotal1).divide(new BigDecimal(depCost), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//                        }
//                        departmentEntity.setDepProducePercent(dividePD.toString());
//                        departmentEntity.setDepLossPercent(divideLD.toString());
//                        departmentEntity.setDepWastePercent(divideWD.toString());
//                    }
//                }

//                map.put("deps", departmentEntities);
//                mapCost.put("typeMap", map);

//            }


//        }

        mapCost.put("total", String.format("%.1f", aDoutble));
        mapCost.put("salesTotal", String.format("%.1f", saleDouble));
        mapCost.put("lossTotal", String.format("%.1f", lossDouble));
        mapCost.put("wasteTotal", String.format("%.1f", wasteDouble));

//        if (type.equals("total")) {
//            mapCost.put("arr", abcDepCostSubtotal(departmentEntityTreeSet));
//        }
//        if (type.equals("sales")) {
//            mapCost.put("arr", abcFatherSales(departmentEntityTreeSet));
//        }
//        if (type.equals("loss")) {
//            mapCost.put("arr", abcFatherLoss(greatGrandGoodsCost));
//        }
//        if (type.equals("waste")) {
//            mapCost.put("arr", abcFatherWaste(greatGrandGoodsCost));
//        }

        mapCost.put("code", "0");

        return mapCost;
    }


    private Map<String, Object> getDepGoosDailyCostFenxi0(Integer disId, String startDate, String stopDate,
                                                          String type, Integer searchDepId) {

        System.out.println("costutuutititit");

        //
        double aDoutble = 0.0;
        Double saleDouble = 0.0;
        Double wasteDouble = 0.0;
        Double lossDouble = 0.0;
        Integer howManyDaysInPeriod = 0;

        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        Map<String, Object> mapCost = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<String> list = new ArrayList<>();

        if (howManyDaysInPeriod > 0) {
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                // disGoods
                Map<String, Object> mapDisGoods = new HashMap<>();
                mapDisGoods.put("date", whichDay);
                mapDisGoods.put("disId", disId);
                mapDisGoods.put("depType", getGbDepartmentTypeMendian());
                if (searchDepId != -1) {
                    mapDisGoods.put("depId", searchDepId);
                }
                if (type.equals("sales")) {
                    mapDisGoods.put("produce", 0);
                }
                if (type.equals("loss")) {
                    mapDisGoods.put("loss", 0);
                }
                if (type.equals("waste")) {
                    mapDisGoods.put("waste", 0);
                }
                System.out.println("mapdididiidsisisidiidi" + mapDisGoods);
                Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
                if (count > 0) {
                    Double aDouble = 0.0;
                    if (type.equals("total")) {
                        Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                        Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                        Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                        aDouble = aDoubleP + aDoubleL + aDoubleW;
                    }
                    if (type.equals("sales")) {
                        aDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                    }
                    if (type.equals("loss")) {
                        aDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                    }
                    if (type.equals("waste")) {
                        aDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                    }
                    list.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                } else {
                    list.add("0");
                }

            }
        } else {
            // dateList
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            // disGoods
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("disId", disId);
            mapDisGoods.put("depType", getGbDepartmentTypeMendian());
            if (searchDepId != -1) {
                mapDisGoods.put("depId", searchDepId);
            }
            if (type.equals("sales")) {
                mapDisGoods.put("produce", 0);
            }
            if (type.equals("loss")) {
                mapDisGoods.put("loss", 0);
            }
            if (type.equals("waste")) {
                mapDisGoods.put("waste", 0);
            }
            System.out.println("mapdididiidsisisidiidi" + mapDisGoods);
            Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
            if (count > 0) {
                Double aDouble = 0.0;
                if (type.equals("total")) {
                    Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                    Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                    Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                    aDouble = aDoubleP + aDoubleL + aDoubleW;
                }
                if (type.equals("sales")) {
                    aDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                }
                if (type.equals("loss")) {
                    aDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                }
                if (type.equals("waste")) {
                    aDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                }
                list.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            } else {
                list.add("0");
            }
        }


        List<GbDistributerFatherGoodsEntity> greatGrandGoodsCost = new ArrayList<>();
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntitiesIds = gbDepartmentService.queryGroupDepsByDisId(mapDep);
        List<String> ids = new ArrayList<>();
        if (departmentEntitiesIds.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntitiesIds) {
                ids.add(dep.getGbDepartmentId().toString());
            }
        } else {
            return R.error(-1, "没有门店");
        }


        Map<String, Object> map1222 = new HashMap<>();
        map1222.put("disId", disId);
        map1222.put("depType", getGbDepartmentTypeMendian());
        if (howManyDaysInPeriod > 0) {
            map1222.put("startDate", startDate);
            map1222.put("stopDate", stopDate);
        } else {
            map1222.put("date", startDate);
        }

        if (searchDepId != -1) {
            map1222.put("depId", searchDepId);
        } else {
            map1222.put("depFatherIds", ids);
        }
        if (type.equals("sales")) {
            map1222.put("produce", 0);
        }
        if (type.equals("loss")) {
            map1222.put("loss", 0);
        }
        if (type.equals("waste")) {
            map1222.put("waste", 0);
        }

        Integer count122 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map1222);
        if (count122 > 0) {
            if (type.equals("total")) {
                lossDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map1222);
                wasteDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map1222);
                saleDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map1222);
                aDoutble = saleDouble + lossDouble + wasteDouble;
            }
            if (type.equals("sales")) {
                saleDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map1222);

            }
            if (type.equals("loss")) {
                lossDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map1222);
            }
            if (type.equals("waste")) {
                wasteDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map1222);
            }

            double greatGrandDouble = 0.0;
            greatGrandGoodsCost = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map1222);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsCost) {
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    double grandDouble = 0.0;
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        double fatherDouble = 0.0;
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        Map<String, Object> map1223 = new HashMap<>();
                        map1223.put("disId", disId);
                        map1223.put("depType", getGbDepartmentTypeMendian());
                        if (howManyDaysInPeriod > 0) {
                            map1223.put("startDate", startDate);
                            map1223.put("stopDate", stopDate);
                        } else {
                            map1223.put("date", startDate);
                        }


                        if (searchDepId != -1) {
                            map1223.put("depId", searchDepId);
                        } else {
                            map1223.put("depFatherIds", ids);
                        }
                        if (type.equals("sales")) {
                            map1223.put("produce", 0);
                        }
                        if (type.equals("loss")) {
                            map1223.put("loss", 0);
                        }
                        if (type.equals("waste")) {
                            map1223.put("waste", 0);
                        }

                        map1223.put("disGoodsFatherId", gbDistributerFatherGoodsId);
                        if (type.equals("total")) {
                            Double fatherDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map1223);
                            Double fatherDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map1223);
                            Double fatherDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map1223);
                            fatherDouble = fatherDoubleP + fatherDoubleL + fatherDoubleW;
                            father.setFatherCostSubtotal(fatherDouble);
                            father.setFatherCostSubtotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        }
                        if (type.equals("sales")) {
                            fatherDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map1223);
                            father.setFatherSellingSubtotal(fatherDouble);
                            father.setFatherSellingSubtotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        }
                        if (type.equals("loss")) {
                            fatherDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map1223);
                            father.setFatherLossTotal(fatherDouble);
                            father.setFatherLossTotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        }
                        if (type.equals("waste")) {
                            fatherDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map1223);
                            father.setFatherWasteTotal(fatherDouble);
                            father.setFatherWasteTotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        }
                        grandDouble = grandDouble + fatherDouble;

                    }

                    if (type.equals("total")) {
//                        grandFather.setFatherGoodsEntities(abcFatherCostSubtotal(fatherGoodsEntities));
                        grandFather.setFatherCostSubtotal(grandDouble);
                        grandFather.setFatherCostSubtotalString(new BigDecimal(grandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("sales")) {
//                        grandFather.setFatherGoodsEntities(abcFatherSales(fatherGoodsEntities));
                        grandFather.setFatherSellingSubtotal(grandDouble);
                        grandFather.setFatherSellingSubtotalString(new BigDecimal(grandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("loss")) {
//                        grandFather.setFatherGoodsEntities(abcFatherLoss(fatherGoodsEntities));
                        grandFather.setFatherLossTotal(grandDouble);
                        grandFather.setFatherLossTotalString(new BigDecimal(grandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("waste")) {
//                        grandFather.setFatherGoodsEntities(abcFatherWaste(fatherGoodsEntities));
                        grandFather.setFatherWasteTotal(grandDouble);
                        grandFather.setFatherWasteTotalString(new BigDecimal(grandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    greatGrandDouble = greatGrandDouble + grandDouble;
                }

                if (type.equals("total")) {
//                    greatGrandFather.setFatherGoodsEntities(abcFatherCostSubtotal(grandGoodsEntities));
                    greatGrandFather.setFatherCostSubtotal(greatGrandDouble);
                    greatGrandFather.setFatherCostSubtotalString(new BigDecimal(greatGrandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("sales")) {
//                    greatGrandFather.setFatherGoodsEntities(abcFatherSales(grandGoodsEntities));
                    greatGrandFather.setFatherSellingSubtotal(greatGrandDouble);
                    greatGrandFather.setFatherSellingSubtotalString(new BigDecimal(greatGrandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("loss")) {
//                    greatGrandFather.setFatherGoodsEntities(abcFatherLoss(grandGoodsEntities));
                    greatGrandFather.setFatherLossTotal(greatGrandDouble);
                    greatGrandFather.setFatherLossTotalString(new BigDecimal(greatGrandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("waste")) {
//                    greatGrandFather.setFatherGoodsEntities(abcFatherWaste(grandGoodsEntities));
                    greatGrandFather.setFatherWasteTotal(greatGrandDouble);
                    greatGrandFather.setFatherWasteTotalString(new BigDecimal(greatGrandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }

        } else {
            return R.error(-1, "没有数据");
        }

        mapCost.put("date", dateList);
        mapCost.put("list", list);
        mapCost.put("total", String.format("%.1f", aDoutble));
        mapCost.put("salesTotal", String.format("%.1f", saleDouble));
        mapCost.put("lossTotal", String.format("%.1f", lossDouble));
        mapCost.put("wasteTotal", String.format("%.1f", wasteDouble));
        mapCost.put("arr", greatGrandGoodsCost);

//        if (type.equals("total")) {
//            mapCost.put("arr", abcFatherCostSubtotal(greatGrandGoodsCost));
//        }
//        if (type.equals("sales")) {
//            mapCost.put("arr", abcFatherSales(greatGrandGoodsCost));
//        }
//        if (type.equals("loss")) {
//            mapCost.put("arr", abcFatherLoss(greatGrandGoodsCost));
//        }
//        if (type.equals("waste")) {
//            mapCost.put("arr", abcFatherWaste(greatGrandGoodsCost));
//        }
        mapCost.put("code", "0");

        return mapCost;
    }

    private Map<String, Object> getDepGoosDailyProfitFenxi(Integer disId, String startDate, String stopDate,
                                                           String type, String searchDepIds) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> list = new ArrayList<>();

        if (howManyDaysInPeriod > 0) {
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                // disGoods
                Map<String, Object> mapDisGoods = new HashMap<>();
                mapDisGoods.put("date", whichDay);
                mapDisGoods.put("disId", disId);
                if (!searchDepIds.equals("-1")) {
                    String[] arrGb = searchDepIds.split(",");
                    List<String> idsGb = new ArrayList<>();
                    for (String idGb : arrGb) {
                        idsGb.add(idGb);
                        if (idsGb.size() > 0) {
                            mapDisGoods.put("depFatherIds", idsGb);
                        }
                    }
                }
                if (type.equals("sales")) {
                    mapDisGoods.put("produce", 0);
                }
                if (type.equals("loss")) {
                    mapDisGoods.put("loss", 0);
                }
                if (type.equals("waste")) {
                    mapDisGoods.put("waste", 0);
                }
                Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
                if (count > 0) {
                    Double aDouble = 0.0;
                    if (type.equals("profit")) {
                        aDouble = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(mapDisGoods);
                    }
                    if (type.equals("sales")) {
                        aDouble = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(mapDisGoods);
                    }
                    if (type.equals("loss")) {
                        aDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                    }
                    if (type.equals("waste")) {
                        aDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                    }
                    list.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                } else {
                    list.add("0");
                }

            }
        } else {
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            // disGoods
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("disId", disId);
//            if (searchDepId != -1) {
//                mapDisGoods.put("depId", searchDepId);
//            }
            if (!searchDepIds.equals("-1")) {
                String[] arrGb = searchDepIds.split(",");
                List<String> idsGb = new ArrayList<>();
                for (String idGb : arrGb) {
                    idsGb.add(idGb);
                    if (idsGb.size() > 0) {
                        mapDisGoods.put("depFatherIds", idsGb);
                    }
                }
            }
            if (type.equals("sales")) {
                mapDisGoods.put("produce", 0);
            }
            if (type.equals("loss")) {
                mapDisGoods.put("loss", 0);
            }
            if (type.equals("waste")) {
                mapDisGoods.put("waste", 0);
            }
            Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
            if (count > 0) {
                Double aDouble = 0.0;
                if (type.equals("profit")) {
                    aDouble = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(mapDisGoods);
                }
                if (type.equals("sales")) {
                    aDouble = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(mapDisGoods);
                }
                if (type.equals("loss")) {
                    aDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                }
                if (type.equals("waste")) {
                    aDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                }
                list.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            } else {
                list.add("0");
            }
        }


        Double aDoutble = 0.0;
        Double saleDouble = 0.0;
        Double wasteDouble = 0.0;
        Double lossDouble = 0.0;

        List<GbDistributerFatherGoodsEntity> greatGrandGoodsCost = new ArrayList<>();
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntitiesIds = gbDepartmentService.queryGroupDepsByDisId(mapDep);
        List<String> ids = new ArrayList<>();
        if (departmentEntitiesIds.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntitiesIds) {
                ids.add(dep.getGbDepartmentId().toString());
            }
        } else {
            return R.error(-1, "没有门店");
        }

        Map<String, Object> map1222 = new HashMap<>();
        map1222.put("disId", disId);
        if (howManyDaysInPeriod > 0) {
            map1222.put("startDate", startDate);
            map1222.put("stopDate", stopDate);
        } else {
            map1222.put("date", startDate);
        }

        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            List<String> idsGb = new ArrayList<>();
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map1222.put("depFatherIds", idsGb);
                }
            }
        }
        if (type.equals("sales")) {
            map1222.put("produce", 0);
        }
        if (type.equals("loss")) {
            map1222.put("loss", 0);
        }
        if (type.equals("waste")) {
            map1222.put("waste", 0);
        }
        Integer count122 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map1222);

        if (count122 > 0) {
            if (type.equals("profit")) {
                aDoutble = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map1222);
                saleDouble = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map1222);
                lossDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map1222);
                wasteDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map1222);
            }
            if (type.equals("sales")) {
                saleDouble = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map1222);

            }
            if (type.equals("loss")) {
                lossDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map1222);
            }
            if (type.equals("waste")) {
                wasteDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map1222);
            }


            double greatGrandDouble = 0.0;
            greatGrandGoodsCost = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map1222);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsCost) {
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    double grandDouble = 0.0;

                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        double fatherDouble = 0.0;
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map1222.put("disGoodsFatherId", gbDistributerFatherGoodsId);
                        if (type.equals("profit")) {
                            fatherDouble = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map1222);
                            father.setFatherProfitTotal(fatherDouble);
                            father.setFatherProfitTotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        }
                        if (type.equals("sales")) {
                            fatherDouble = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map1222);
                            father.setFatherSellingSubtotal(fatherDouble);
                            father.setFatherSellingSubtotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        }
                        if (type.equals("loss")) {
                            fatherDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map1222);
                            father.setFatherLossTotal(fatherDouble);
                            father.setFatherLossTotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        }
                        if (type.equals("waste")) {
                            fatherDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map1222);
                            father.setFatherWasteTotal(fatherDouble);
                            father.setFatherWasteTotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        }
                        grandDouble = grandDouble + fatherDouble;

                    }

                    if (type.equals("profit")) {
//                        grandFather.setFatherGoodsEntities(abcFatherProfit(fatherGoodsEntities));
                        grandFather.setFatherProfitTotal(grandDouble);
                        grandFather.setFatherProfitTotalString(new BigDecimal(grandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("sales")) {
//                        grandFather.setFatherGoodsEntities(abcFatherSales(fatherGoodsEntities));
                        grandFather.setFatherSellingSubtotal(grandDouble);
                        grandFather.setFatherSellingSubtotalString(new BigDecimal(grandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("loss")) {
//                        grandFather.setFatherGoodsEntities(abcFatherLoss(fatherGoodsEntities));
                        grandFather.setFatherLossTotal(grandDouble);
                        grandFather.setFatherLossTotalString(new BigDecimal(grandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("waste")) {
//                        grandFather.setFatherGoodsEntities(abcFatherWaste(fatherGoodsEntities));
                        grandFather.setFatherWasteTotal(grandDouble);
                        grandFather.setFatherWasteTotalString(new BigDecimal(grandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    greatGrandDouble = greatGrandDouble + grandDouble;

                }
                if (type.equals("profit")) {
//                    greatGrandFather.setFatherGoodsEntities(abcFatherProfit(grandGoodsEntities));
                    greatGrandFather.setFatherProfitTotal(greatGrandDouble);
                    greatGrandFather.setFatherProfitTotalString(new BigDecimal(greatGrandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("sales")) {
//                    greatGrandFather.setFatherGoodsEntities(abcFatherSales(grandGoodsEntities));
                    greatGrandFather.setFatherSellingSubtotal(greatGrandDouble);
                    greatGrandFather.setFatherSellingSubtotalString(new BigDecimal(greatGrandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("loss")) {
//                    greatGrandFather.setFatherGoodsEntities(abcFatherLoss(grandGoodsEntities));
                    greatGrandFather.setFatherLossTotal(greatGrandDouble);
                    greatGrandFather.setFatherLossTotalString(new BigDecimal(greatGrandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("waste")) {
//                    greatGrandFather.setFatherGoodsEntities(abcFatherWaste(grandGoodsEntities));
                    greatGrandFather.setFatherWasteTotal(greatGrandDouble);
                    greatGrandFather.setFatherWasteTotalString(new BigDecimal(greatGrandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }

        } else {
            return R.error(-1, "meiou");
        }

        Map<String, Object> mapCost = new HashMap<>();
        mapCost.put("date", dateList);
        mapCost.put("list", list);
        mapCost.put("total", String.format("%.1f", aDoutble));
        mapCost.put("salesTotal", String.format("%.1f", saleDouble));
        mapCost.put("lossTotal", String.format("%.1f", lossDouble));
        mapCost.put("wasteTotal", String.format("%.1f", wasteDouble));
        mapCost.put("arr", greatGrandGoodsCost);

//        if (type.equals("profit")) {
//            mapCost.put("arr", abcFatherProfit(greatGrandGoodsCost));
//        }
//        if (type.equals("sales")) {
//            mapCost.put("arr", abcFatherSales(greatGrandGoodsCost));
//        }
//        if (type.equals("loss")) {
//            mapCost.put("arr", abcFatherLoss(greatGrandGoodsCost));
//        }
//        if (type.equals("waste")) {
//            mapCost.put("arr", abcFatherWaste(greatGrandGoodsCost));
//        }
        mapCost.put("code", "0");
        return mapCost;
    }


    @RequestMapping(value = "/depGetDepGoodsDailyDetail", method = RequestMethod.POST)
    @ResponseBody
    public R depGetStockReduceDailyDetail(Integer depGoodsId, String startDate, String stopDate) {
        //cost查询
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("depGoodsId", depGoodsId);
        List<GbDepartmentGoodsDailyEntity> dailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListByParams(map);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("arr", dailyEntities);
        return R.ok().put("data", map1);
    }


    @RequestMapping(value = "/getDailyClearFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getDailyClearFatherGoods(Integer disId, String startDate, String stopDate, Integer depId) {
        Map<String, Object> mapResult = new HashMap<>();
        String time = "--:--";
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("controlFresh", 1);
        map.put("clear", -1);
        if (depId != -1) {
            map.put("depId", depId);
        }

        Map<String, Object> echartsWeight = getClearEveryDep(startDate, stopDate, disId);
        mapResult.put("arr", echartsWeight);


        Integer integerT = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        if (integerT > 0) {
            int clearHourT = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(map);
            int clearMinuteT = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(map);
            int hourT = clearHourT * 60;
            int totalMinute = (hourT + clearMinuteT) / integerT;
            int hour = totalMinute / 60;
            int minT = totalMinute % 60;

            String minStringT = "";
            if (minT < 10) {
                minStringT = "0" + minT;
            } else {
                minStringT = Integer.toString(minT);
            }
            time = hour + ":" + minStringT;
        }
        mapResult.put("time", time);

        //freshRate
        Map<String, Object> mapF = new HashMap<>();
        mapF.put("disId", disId);
        mapF.put("startDate", startDate);
        mapF.put("stopDate", stopDate);
        mapF.put("produce", 0);
        mapF.put("controlFresh", 1);
        if (depId != -1) {
            mapF.put("depId", depId);
        }
        System.out.println("freshRAtemapapappap" + mapF);
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapF);
        double Tv = 0;
        if (integer1 > 0) {
            double v = gbDepGoodsDailyService.queryDepGoodsFreshRate(mapF);
            Tv = v / integer1;
        }

        mapResult.put("freshRate", new BigDecimal(Tv).setScale(2, BigDecimal.ROUND_HALF_UP));


        Map<String, Object> mapG = new HashMap<>();
        mapG.put("status", 2);
        mapG.put("disId", disId);
        mapG.put("startDate", startDate);
        mapG.put("stopDate", stopDate);
        mapG.put("depType", getGbDepartmentTypeMendian());
        if (depId != -1) {
            mapG.put("depId", depId);
        }
        Integer integer2 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapG);
        BigDecimal decimal = new BigDecimal(0);
        if (integer2 > 0) {
//            Double  = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapG);
            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapG);
            decimal = new BigDecimal(wasteSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
//            if (produceSubtotal > 0) {
//                decimal = new BigDecimal(wasteSubtotal).divide(new BigDecimal(produceSubtotal), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//            }
        }
        mapResult.put("freshGoods", decimal);
        return R.ok().put("data", mapResult);

    }


    private Map<String, Object> getClearEveryDep(String startDate, String stopDate, Integer disId) {

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> mapDis = new HashMap<>();
        mapDis.put("startDate", startDate);
        mapDis.put("stopDate", stopDate);
        mapDis.put("disId", disId);
        mapDis.put("controlFresh", 1);
        mapDis.put("clear", -1);
        mapDis.put("depType", getGbDepartmentTypeMendian());

        List<List<String>> depNameArr = new ArrayList<>();
        List<Map<String, Object>> list = new ArrayList<>();

        System.out.println("mapdidididid" + mapDis);
        TreeSet<GbDepartmentEntity> departmentEntities = gbDepGoodsDailyService.queryWhichDepsHasProduceDepGoodsDailyNew(mapDis);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity departmentEntity : departmentEntities) {
                List<String> perDepList = new ArrayList<>();

                List<String> dateList = new ArrayList<>();
                perDepList.add(departmentEntity.getGbDepartmentName());
                dateList.add("ppp");
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("name", departmentEntity.getGbDepartmentName());
                mapDep.put("id", departmentEntity.getGbDepartmentId());
                mapDis.put("depFatherId", departmentEntity.getGbDepartmentId());

                String depTime = "0:00";
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDis);
                if (integer > 0) {
                    int hourTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(mapDis);
                    int minuteTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(mapDis);
                    int hourMinute = hourTotal * 60;
                    int totalMinute = (hourMinute + minuteTotal) / integer;
                    BigDecimal divide = new BigDecimal(totalMinute).divide(new BigDecimal(60), 0, BigDecimal.ROUND_DOWN);
                    String mintue = "";
                    BigDecimal remainder = new BigDecimal(totalMinute).remainder(new BigDecimal(60)).setScale(0, BigDecimal.ROUND_HALF_DOWN);
                    if (remainder.compareTo(new BigDecimal(10)) == -1) {
                        mintue = "0" + remainder.toString();
                    } else {
                        mintue = remainder.toString();
                    }
                    depTime = divide.toString() + ":" + mintue;
                }
                System.out.println("deptiitititit" + depTime);
                mapDep.put("time", depTime);


                Integer howManyDaysInPeriod = 0;
                if (!startDate.equals(stopDate)) {
                    howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
                }
                List<Map<String, Object>> detailList = new ArrayList<>();
                if (howManyDaysInPeriod > 0) {

                    for (int i = 0; i < howManyDaysInPeriod + 1; i++) {

                        // dateList
                        String whichDay = "";
                        if (i == 0) {
                            whichDay = startDate;
                        } else {
                            whichDay = afterWhatDay(startDate, i);
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put("date", whichDay);
                        map.put("depFatherId", departmentEntity.getGbDepartmentId());
                        map.put("controlFresh", 1);
                        map.put("clear", -1);

                        String substring = whichDay.substring(8, 10);
                        dateList.add(substring);
                        String dailyClear = "0";
                        Integer integerD = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                        if (integerD > 0) {
                            int hourTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(map);
                            int minuteTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(map);
                            int hourMinute = hourTotal * 60;
                            int totalMinute = (hourMinute + minuteTotal) / integerD;
                            dailyClear = new BigDecimal(totalMinute).setScale(1, BigDecimal.ROUND_HALF_UP).toString();

                        }
//
                        perDepList.add(dailyClear);
                        Map<String, Object> mapDe = new HashMap<>();
                        mapDe.put("date", whichDay);

                        BigDecimal divide = new BigDecimal(dailyClear).divide(new BigDecimal(60), 0, BigDecimal.ROUND_DOWN);
                        BigInteger integerPart = divide.toBigInteger();
                        String mintue = "";
                        BigDecimal remainder = new BigDecimal(dailyClear).remainder(new BigDecimal(60)).setScale(0, BigDecimal.ROUND_HALF_DOWN);
                        if (remainder.compareTo(new BigDecimal(10)) == -1) {
                            mintue = "0" + remainder.toString();
                        } else {
                            mintue = remainder.toString();
                        }
                        mapDe.put("value", divide + ":" + mintue);
                        detailList.add(mapDe);
                    }
                }
                depNameArr.add(perDepList);
                mapR.put("date", dateList);
                mapDep.put("list", detailList);


                list.add(mapDep);

            }

            mapR.put("depArr", depNameArr);
            mapR.put("arr", list);

        }


        return mapR;

    }


    @RequestMapping(value = "/getDailyClearFatherGoods1", method = RequestMethod.POST)
    @ResponseBody
    public R getDailyClearFatherGoods1(Integer disId, String startDate, String stopDate, Integer depId) {
        Map<String, Object> mapResult = new HashMap<>();
        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsEntities = new TreeSet<>();
        String time = "--:--";
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("controlFresh", 1);
        map.put("clear", -1);
        if (depId != -1) {
            map.put("depId", depId);
        }

        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        if (integer > 0) {
            int clearHourT = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(map);
            int clearMinuteT = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(map);
            int hourT = clearHourT * 60;
            int totalMinute = (hourT + clearMinuteT) / integer;

            int hour = totalMinute / 60;
            int minT = totalMinute % 60;

            String minStringT = "";
            if (minT < 10) {
                minStringT = "0" + minT;
            } else {
                minStringT = Integer.toString(minT);
            }
            time = hour + ":" + minStringT;

            greatGrandGoodsEntities = gbDepGoodsDailyService.queryClearFatherGoods(map);

            for (GbDistributerFatherGoodsEntity greatGrand : greatGrandGoodsEntities) {

                int greatGrandClearHour = 0;
                int greatGrandClearMinute = 0;
                int greatGrandCount = 0;
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrand.getFatherGoodsEntities();

                for (GbDistributerFatherGoodsEntity grand : grandGoodsEntities) {

                    int grandClearHour = 0;
                    int grandClearMinute = 0;
                    int grandCount = 0;
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map.put("disGoodsFatherId", gbDistributerFatherGoodsId);
                        System.out.println("mappclearrrr" + map);
                        int clearHour = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(map);
                        int clearMinute = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(map);
                        Integer integerCount = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                        grandCount = grandCount + integerCount;

                        int hourTF = clearHour * 60;
                        int totalMinuteF = (hourTF + clearMinute) / integerCount;
                        int hourF = totalMinuteF / 60;
                        int minTF = totalMinuteF % 60;

                        String minString = "";
                        if (minTF < 10) {
                            minString = "0" + minTF;
                        } else {
                            minString = Integer.toString(minTF);
                        }
                        father.setFatherClearTimeString(hourF + ":" + minString);
                        grandClearHour = grandClearHour + clearHour;
                        grandClearMinute = grandClearMinute + clearMinute;
                    }
                    int hourG = grandClearHour * 60;
                    int totalMinuteFG = (hourG + grandClearMinute) / grandCount;
                    int hourFG = totalMinuteFG / 60;
                    int minTFG = totalMinuteFG % 60;
                    String minString = "";
                    if (minTFG < 10) {
                        minString = "0" + minTFG;
                    } else {
                        minString = Integer.toString(minTFG);
                    }
                    grand.setFatherClearTimeString(hourFG + ":" + minString);
                    greatGrandClearHour = greatGrandClearHour + grandClearHour;
                    greatGrandClearMinute = greatGrandClearMinute + grandClearMinute;
                    greatGrandCount = greatGrandCount + grandCount;
                }
                int hourGG = greatGrandClearHour * 60;
                int totalMinuteFGG = (hourGG + greatGrandClearMinute) / greatGrandCount;
                int hourFGG = totalMinuteFGG / 60;
                int minTFGG = totalMinuteFGG % 60;
                String minString = "";
                if (minTFGG < 10) {
                    minString = "0" + minTFGG;
                } else {
                    minString = Integer.toString(minTFGG);
                }
                greatGrand.setFatherClearTimeString(hourFGG + ":" + minString);
            }
        }

        mapResult.put("arr", greatGrandGoodsEntities);


        mapResult.put("time", time);

        //freshRate
        Map<String, Object> mapF = new HashMap<>();
        mapF.put("disId", disId);
        mapF.put("startDate", startDate);
        mapF.put("stopDate", stopDate);
        mapF.put("produce", 0);
        mapF.put("controlFresh", 1);
        if (depId != -1) {
            mapF.put("depId", depId);
        }
        System.out.println("freshRAtemapapappap" + mapF);
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapF);
        double Tv = 0;
        if (integer1 > 0) {
            double v = gbDepGoodsDailyService.queryDepGoodsFreshRate(mapF);
            Tv = v / integer1;
        }

        mapResult.put("freshRate", new BigDecimal(Tv).setScale(2, BigDecimal.ROUND_HALF_UP));


        Map<String, Object> mapG = new HashMap<>();
        mapG.put("status", 2);
        mapG.put("disId", disId);
        mapG.put("startDate", startDate);
        mapG.put("stopDate", stopDate);
        mapG.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        if (depId != -1) {
            mapG.put("depId", depId);
        }
        Integer integer2 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapG);
        BigDecimal decimal = new BigDecimal(0);
        if (integer2 > 0) {
            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapG);
            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapG);
            if (produceSubtotal > 0) {
                decimal = new BigDecimal(wasteSubtotal).divide(new BigDecimal(produceSubtotal), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }
        mapResult.put("freshGoods", decimal);
        return R.ok().put("data", mapResult);

    }


    @RequestMapping(value = "/getDailyFreshFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getDailyFreshFatherGoods(Integer disId, String startDate, String stopDate, Integer depId,
                                      String showType, Integer disGoodsId) {

        Map<String, Object> mapResult = new HashMap<>();
        String time = "--:--";
        //time
        Map<String, Object> mapT = new HashMap<>();
        mapT.put("disId", disId);
        mapT.put("startDate", startDate);
        mapT.put("stopDate", stopDate);
        mapT.put("clear", -1);
        mapT.put("controlFresh", 1);
        if (depId != -1) {
            mapT.put("depId", depId);
        }
        Integer integerT = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapT);
        if (integerT > 0) {
            int clearHourT = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(mapT);
            int clearMinuteT = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(mapT);
            int hourT = clearHourT * 60;
            int totalMinute = (hourT + clearMinuteT) / integerT;
            int hour = totalMinute / 60;
            int minT = totalMinute % 60;

            String minStringT = "";
            if (minT < 10) {
                minStringT = "0" + minT;
            } else {
                minStringT = Integer.toString(minT);
            }
            time = hour + ":" + minStringT;
        }
        mapResult.put("time", time);


        //freshRate
        Map<String, Object> mapF = new HashMap<>();
        mapF.put("disId", disId);
        mapF.put("startDate", startDate);
        mapF.put("stopDate", stopDate);
        mapF.put("produce", 0);
        mapF.put("controlFresh", 1);
        if (depId != -1) {
            mapF.put("depId", depId);
        }
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapF);
        double Tv = 0;
        if (integer1 > 0) {
            double v = gbDepGoodsDailyService.queryDepGoodsFreshRate(mapF);
            Tv = v / integer1;
        }

        Map<String, Object> mapG = new HashMap<>();
        mapG.put("status", 2);
        mapG.put("disId", disId);
        mapG.put("startDate", startDate);
        mapG.put("stopDate", stopDate);
        if (depId != -1) {
            mapG.put("depId", depId);
        }
        Integer integer2 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapG);
        BigDecimal decimal = new BigDecimal(0);
        if (integer2 > 0) {
            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapG);
            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapG);
            if (produceSubtotal > 0) {
                decimal = new BigDecimal(wasteSubtotal).divide(new BigDecimal(produceSubtotal), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }

        mapResult.put("freshGoods", decimal);
        mapResult.put("freshRate", new BigDecimal(Tv).setScale(2, BigDecimal.ROUND_HALF_UP));

        Map<String, Object> echartsWeight = new HashMap<>();
        if (showType.equals("day")) {
            echartsWeight = getFreshEveryDay(startDate, stopDate, disId, depId, disGoodsId);
        }
        if (showType.equals("dep")) {
            echartsWeight = getFreshEveryDep(startDate, stopDate, disId, disGoodsId);
        }

        mapResult.put("arr", echartsWeight);

        return R.ok().put("data", mapResult);

    }


    private Map<String, Object> getFreshEveryDep(String startDate, String stopDate, Integer disId, Integer disGoodsId) {

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> mapDis = new HashMap<>();
        mapDis.put("startDate", startDate);
        mapDis.put("stopDate", stopDate);
        mapDis.put("disId", disId);
        mapDis.put("depType", getGbDepartmentTypeMendian());

        List<List<String>> depNameArr = new ArrayList<>();
        List<Map<String, Object>> list = new ArrayList<>();

        TreeSet<GbDepartmentEntity> departmentEntities = gbDepGoodsDailyService.queryWhichDepsHasProduceDepGoodsDailyNew(mapDis);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity departmentEntity : departmentEntities) {
                List<String> perDepList = new ArrayList<>();

                List<String> dateList = new ArrayList<>();
                perDepList.add(departmentEntity.getGbDepartmentName());
                dateList.add("ppp");
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("name", departmentEntity.getGbDepartmentName());
                mapDep.put("id", departmentEntity.getGbDepartmentId());

                Integer howManyDaysInPeriod = 0;
                if (!startDate.equals(stopDate)) {
                    howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
                }
                List<Map<String, Object>> detailList = new ArrayList<>();
                if (howManyDaysInPeriod > 0) {

                    for (int i = 0; i < howManyDaysInPeriod + 1; i++) {

                        // dateList
                        String whichDay = "";
                        if (i == 0) {
                            whichDay = startDate;
                        } else {
                            whichDay = afterWhatDay(startDate, i);
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put("date", whichDay);
                        map.put("depFatherId", departmentEntity.getGbDepartmentId());
                        map.put("controlFresh", 1);
                        if (disGoodsId != -1) {
                            map.put("disGoodsId", disGoodsId);
                        }

                        String substring = whichDay.substring(8, 10);
                        dateList.add(substring);

                        String dailyFresh = "0";
                        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                        if (integer > 0) {
                            System.out.println("dmapapappapa" + map);
                            double freshRate = gbDepGoodsDailyService.queryDepGoodsFreshRate(map);
                            dailyFresh = new BigDecimal(freshRate).divide(new BigDecimal(integer), 4, BigDecimal.ROUND_HALF_UP)
                                    .setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                            System.out.println("dailaffrieihshswh====" + dailyFresh);

                        }
//
                        perDepList.add(dailyFresh);
                        Map<String, Object> mapDe = new HashMap<>();
                        mapDe.put("date", whichDay);
                        mapDe.put("value", dailyFresh);
                        detailList.add(mapDe);
                    }
                }
                depNameArr.add(perDepList);
                mapR.put("date", dateList);
                mapDep.put("list", detailList);
                list.add(mapDep);

            }

            mapR.put("depArr", depNameArr);
            mapR.put("arr", list);

        }


        return mapR;

    }


    private Map<String, Object> getFreshEveryDay(String startDate, String stopDate, Integer disId,
                                                 Integer searchDepId, Integer disGoodsId) {

        System.out.println("getfrisheeieieidydyydydydyydydyydydydyy");
        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> itemList = new ArrayList<>();
        List<String> dateList = new ArrayList<>();
        List<String> totalList = new ArrayList<>();
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        if (howManyDaysInPeriod > 0) {

            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                Map<String, Object> map = new HashMap<>();
                map.put("date", whichDay);
                map.put("disId", disId);
                map.put("controlFresh", 1);

                if (disGoodsId != -1) {
                    map.put("disGoodsId", disGoodsId);
                }
                if (searchDepId != -1) {
                    map.put("depFatherId", searchDepId);
                } else {
                    map.put("depType", getGbDepartmentTypeMendian());
                }
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                String dailyFresh = "0";
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                if (integer > 0) {
                    double freshRate = gbDepGoodsDailyService.queryDepGoodsFreshRate(map);
                    System.out.println("frweshrAtte" + freshRate);

                    dailyFresh = new BigDecimal(freshRate).divide(new BigDecimal(integer), 4, BigDecimal.ROUND_HALF_UP)
                            .setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    System.out.println("frweshrAtte" + dailyFresh);

                }
                totalList.add(dailyFresh);
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("day", whichDay);
                mapItem.put("value", dailyFresh);
                itemList.add(mapItem);
                mapR.put("date", dateList);
                mapR.put("list", totalList);
                mapR.put("arr", itemList);

            }

        }
        return mapR;

    }

    @RequestMapping(value = "/getDailyFreshFatherGoods1", method = RequestMethod.POST)
    @ResponseBody
    public R getDailyFreshFatherGoods1(Integer disId, String startDate, String stopDate, Integer depId) {

        Map<String, Object> mapResult = new HashMap<>();
        String time = "--:--";
        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsEntities = new TreeSet<>();

        //time
        Map<String, Object> mapT = new HashMap<>();
        mapT.put("disId", disId);
        mapT.put("startDate", startDate);
        mapT.put("stopDate", stopDate);
        mapT.put("clear", -1);
        mapT.put("controlFresh", 1);
        if (depId != -1) {
            mapT.put("depId", depId);
        }
        Integer integerT = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapT);
        if (integerT > 0) {
            int clearHourT = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(mapT);
            int clearMinuteT = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(mapT);
            int hourT = clearHourT * 60;
            int totalMinute = (hourT + clearMinuteT) / integerT;

            int hour = totalMinute / 60;
            int minT = totalMinute % 60;

            String minStringT = "";
            if (minT < 10) {
                minStringT = "0" + minT;
            } else {
                minStringT = Integer.toString(minT);
            }
            time = hour + ":" + minStringT;
        }
        mapResult.put("time", time);


        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("produce", 0);
        map.put("controlFresh", 1);
        if (depId != -1) {
            map.put("depId", depId);
        }
        System.out.println("freshshshdhdappapap" + map);
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        if (integer > 0) {
            // arr
            greatGrandGoodsEntities = gbDepGoodsDailyService.queryFreshFatherGoods(map);
            for (GbDistributerFatherGoodsEntity greatGrand : greatGrandGoodsEntities) {
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrand.getFatherGoodsEntities();
                double greatGrandFreshTotal = 0;
                double vgg = 100.0;
                int integerGreatGrand = 0;
                for (GbDistributerFatherGoodsEntity grand : grandGoodsEntities) {
                    double vg = 100.0;
                    double grandFreshTotal = 0.0;
                    int integerGrand = 0;
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        double fatherFreshTotal = 0.0;
                        Integer integerFather = 1;
                        double v = 100.0;
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map.put("disGoodsFatherId", gbDistributerFatherGoodsId);
                        integerFather = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                        if (integerFather > 0) {
                            fatherFreshTotal = gbDepGoodsDailyService.queryDepGoodsFreshRate(map);
                        }
                        v = fatherFreshTotal / integerFather;

                        System.out.println("vvvvvFresh" + v + "totalla==== " + fatherFreshTotal);
                        father.setFatherFreshRate(v);
                        father.setFatherFreshRateString(new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                        grandFreshTotal = grandFreshTotal + fatherFreshTotal;
                        greatGrandFreshTotal = greatGrandFreshTotal + fatherFreshTotal;
                        integerGrand = integerGrand + integerFather;
                        integerGreatGrand = integerGreatGrand + integerFather;

                    }
                    vg = grandFreshTotal / integerGrand;
                    grand.setFatherFreshRate(vg);
                    grand.setFatherFreshRateString(new BigDecimal(vg).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                }
                vgg = greatGrandFreshTotal / integerGreatGrand;
                greatGrand.setFatherFreshRate(vgg);
                greatGrand.setFatherFreshRateString(new BigDecimal(vgg).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

            }

        }
        //freshRate
        Map<String, Object> mapF = new HashMap<>();
        mapF.put("disId", disId);
        mapF.put("startDate", startDate);
        mapF.put("stopDate", stopDate);
        mapF.put("produce", 0);
        mapF.put("controlFresh", 1);
        if (depId != -1) {
            mapF.put("depId", depId);
        }
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapF);
        double Tv = 0;
        if (integer1 > 0) {
            double v = gbDepGoodsDailyService.queryDepGoodsFreshRate(mapF);
            Tv = v / integer1;
        }

        Map<String, Object> mapG = new HashMap<>();
        mapG.put("status", 2);
        mapG.put("disId", disId);
        mapG.put("startDate", startDate);
        mapG.put("stopDate", stopDate);
        mapG.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        if (depId != -1) {
            mapG.put("depId", depId);
        }
        TreeSet<GbDistributerGoodsEntity> aaa = gbDepGoodsStockReduceService.queryGoodsStockRecordTreeByParams(mapG);

        mapResult.put("freshGoods", aaa.size());
        mapResult.put("arr", greatGrandGoodsEntities);
        mapResult.put("freshRate", new BigDecimal(Tv).setScale(2, BigDecimal.ROUND_HALF_UP));

        return R.ok().put("data", mapResult);

    }

    @RequestMapping(value = "/getEveryGoodsProfitFatherMangement", method = RequestMethod.POST)
    @ResponseBody
    public R getEveryGoodsProfitFatherMangement(String startDate, String stopDate, String disGoodsFatherId, Integer type, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("disGoodsFatherId", disGoodsFatherId);
        TreeSet<GbDistributerGoodsEntity> aaa = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);

        Integer howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        BigDecimal decimalDay = new BigDecimal(Integer.parseInt(howManyDaysInPeriod.toString()));


        int howManyDaysInPeriodWeek = getHowManyDaysInPeriod(stopDate, startDate) / 7;
        BigDecimal decimalWeek = new BigDecimal(Integer.toString(howManyDaysInPeriodWeek));

//
        int monthDiff = getMonthDiff(stopDate, startDate);
        BigDecimal decimalMonth = new BigDecimal(Integer.toString(monthDiff));

        for (GbDistributerGoodsEntity goodsEntity : aaa) {
            //1 求总wasteTotal
            Map<String, Object> map1 = new HashMap<>();
            map1.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
            map1.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
            map1.put("startDate", startDate);
            map1.put("stopDate", stopDate);
            System.out.println("map111111aaa" + map1);
            Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map1);
            if (integer > 0) {
                Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map1);
                Double aDoubleTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map1);
                Double aDoubleWeight = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map1);
                BigDecimal everyProfitTotal = new BigDecimal(0);
                BigDecimal everyProduceTotal = new BigDecimal(0);
                BigDecimal everyProduceWeight = new BigDecimal(0);
                if (type == 1) {
                    everyProfitTotal = new BigDecimal(aDouble).divide(decimalDay, 2, BigDecimal.ROUND_HALF_UP);
                    everyProduceTotal = new BigDecimal(aDoubleTotal).divide(decimalDay, 2, BigDecimal.ROUND_HALF_UP);
                    everyProduceWeight = new BigDecimal(aDoubleWeight).divide(decimalDay, 2, BigDecimal.ROUND_HALF_UP);
                }
                if (type == 2) {
                    everyProfitTotal = new BigDecimal(aDouble).divide(decimalWeek, 2, BigDecimal.ROUND_HALF_UP);
                    everyProduceTotal = new BigDecimal(aDoubleTotal).divide(decimalWeek, 2, BigDecimal.ROUND_HALF_UP);
                    everyProduceWeight = new BigDecimal(aDoubleWeight).divide(decimalWeek, 2, BigDecimal.ROUND_HALF_UP);
                }
                if (type == 3) {
                    everyProfitTotal = new BigDecimal(aDouble).divide(decimalMonth, 2, BigDecimal.ROUND_HALF_UP);
                    everyProduceTotal = new BigDecimal(aDoubleTotal).divide(decimalMonth, 2, BigDecimal.ROUND_HALF_UP);
                    everyProduceWeight = new BigDecimal(aDoubleWeight).divide(decimalMonth, 2, BigDecimal.ROUND_HALF_UP);
                }
                goodsEntity.setGoodsEveryProfitTotal(everyProfitTotal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                goodsEntity.setGoodsEveryProfitTotalString(everyProfitTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsEntity.setGoodsEveryProduceTotal(everyProduceTotal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                goodsEntity.setGoodsEveryProduceTotalString(everyProduceTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsEntity.setGoodsEveryProduceWeightTotal(everyProduceWeight.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                goodsEntity.setGoodsEveryProduceWeightTotalString(everyProduceWeight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }

            map1.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
            Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map1);
            if (integer1 > 0) {
                Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map1);
                Double aDoubleWeight = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map1);
                BigDecimal everyWasteTotal = new BigDecimal(0);
                BigDecimal everyWasteWeight = new BigDecimal(0);
                if (type == 1) {
                    everyWasteTotal = new BigDecimal(aDouble).divide(decimalDay, 2, BigDecimal.ROUND_HALF_UP);
                    everyWasteWeight = new BigDecimal(aDoubleWeight).divide(decimalDay, 2, BigDecimal.ROUND_HALF_UP);
                }
                if (type == 2) {
                    everyWasteTotal = new BigDecimal(aDouble).divide(decimalWeek, 2, BigDecimal.ROUND_HALF_UP);
                    everyWasteWeight = new BigDecimal(aDoubleWeight).divide(decimalWeek, 2, BigDecimal.ROUND_HALF_UP);
                }

                if (type == 3) {
                    everyWasteTotal = new BigDecimal(aDouble).divide(decimalMonth, 2, BigDecimal.ROUND_HALF_UP);
                    everyWasteWeight = new BigDecimal(aDoubleWeight).divide(decimalMonth, 2, BigDecimal.ROUND_HALF_UP);
                }

                goodsEntity.setGoodsEveryWasteTotal(everyWasteTotal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                goodsEntity.setGoodsEveryWasteTotalString(everyWasteTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsEntity.setGoodsEveryWasteWeightTotal(everyWasteWeight.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                goodsEntity.setGoodsEveryWasteWeightTotalString(everyWasteWeight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }

            map1.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
            Integer integer2 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map1);
            if (integer2 > 0) {
                Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map1);
                Double aDoubleWeight = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map1);
                BigDecimal everyLossTotal = new BigDecimal(0);
                BigDecimal everyLossWeight = new BigDecimal(0);

                if (type == 1) {
                    everyLossTotal = new BigDecimal(aDouble).divide(decimalDay, 2, BigDecimal.ROUND_HALF_UP);
                    everyLossWeight = new BigDecimal(aDoubleWeight).divide(decimalDay, 2, BigDecimal.ROUND_HALF_UP);
                }
                if (type == 2) {
                    everyLossTotal = new BigDecimal(aDouble).divide(decimalWeek, 2, BigDecimal.ROUND_HALF_UP);
                    everyLossWeight = new BigDecimal(aDoubleWeight).divide(decimalWeek, 2, BigDecimal.ROUND_HALF_UP);
                }
                if (type == 3) {
                    everyLossTotal = new BigDecimal(aDouble).divide(decimalMonth, 2, BigDecimal.ROUND_HALF_UP);
                    everyLossWeight = new BigDecimal(aDoubleWeight).divide(decimalMonth, 2, BigDecimal.ROUND_HALF_UP);
                }

                goodsEntity.setGoodsEveryLossTotal(everyLossTotal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                goodsEntity.setGoodsEveryLossTotalString(everyLossTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsEntity.setGoodsEveryLossWeightTotal(everyLossWeight.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                goodsEntity.setGoodsEveryLossWeightTotalString(everyLossWeight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
        }


        TreeSet<GbDistributerGoodsEntity> abc = abcProfitEvery(aaa);

        return R.ok().put("data", abc);
    }


    private TreeSet<GbDistributerGoodsEntity> abcProfitEvery(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;
                if (o2.getGoodsEveryProfitTotal() - o1.getGoodsEveryProfitTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsEveryProfitTotal() - o1.getGoodsEveryProfitTotal() > 0) {
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

    @RequestMapping(value = "/getMendianProfitTypeStatics", method = RequestMethod.POST)
    @ResponseBody
    public R getMendianProfitTypeStatics(Integer disId, String startDate, String stopDate, String ids) {
        List<GbDistributerFatherGoodsEntity> greatGrandGoods = new ArrayList<>();
        TreeSet<GbDistributerFatherGoodsEntity> resultFatherGoodsList = new TreeSet<>();

        Map<String, Object> map0 = new HashMap<>();
        if (!startDate.equals("-1")) {
            map0.put("startDate", startDate);
        }
        if (!stopDate.equals("-1")) {
            map0.put("stopDate", stopDate);
        }
        map0.put("disId", disId);
        System.out.println("getMendianCostTypeStatics" + map0);
        BigDecimal decimal = new BigDecimal(1);
        if (!startDate.equals("-1") && !stopDate.equals("-1")) {
            Integer howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
            decimal = new BigDecimal(Integer.parseInt(howManyDaysInPeriod.toString()));
        }

        greatGrandGoods = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map0);
        if (greatGrandGoods.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoods) {
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    BigDecimal grandDouble = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        map0.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        Double profitTotal = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map0);
                        BigDecimal everyDayProfit = new BigDecimal(profitTotal).divide(decimal, 1, BigDecimal.ROUND_HALF_UP);
                        father.setEveryDayProfit(everyDayProfit.doubleValue());
                        father.setEveryDayProfitString(everyDayProfit.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        grandDouble = grandDouble.add(everyDayProfit);
                    }
//                    grandFather.setFatherGoodsEntities(abcFatherGoodsEveryDayProfit(fatherGoodsEntities));
                    grandFather.setEveryDayProfit(grandDouble.doubleValue());
                    grandFather.setEveryDayProfitString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    resultFatherGoodsList.add(grandFather);
                    resultFatherGoodsList = abcFatherGoodsEveryDayProfit(resultFatherGoodsList);

                }

            }
        }


        return R.ok().put("data", resultFatherGoodsList);
    }


    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherGoodsEveryDayProfit(TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;
                if (o2.getEveryDayProfit() - o1.getEveryDayProfit() < 0) {
                    result = -1;
                } else if (o2.getEveryDayProfit() - o1.getEveryDayProfit() > 0) {
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


    @RequestMapping(value = "/getDepGoodsChartsProfit", method = RequestMethod.POST)
    @ResponseBody
    public R getDepGoodsChartsProfit(Integer depGoodsId, String startDate, String stopDate, Integer disGoodsId, String type) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }

        map.put("disGoodsId", disGoodsId);
        Double aDouble1P = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map);
        Double aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
        Double aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);

        map.put("depGoodsId", depGoodsId);
        Double depaDouble1P = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map);
        Double depaDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
        Double depaDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);


        Map<String, Object> map1 = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<String> produceList = new ArrayList<>();
        List<String> lossList = new ArrayList<>();
        List<String> wasteList = new ArrayList<>();
        List<String> depProduceList = new ArrayList<>();
        List<String> depLossList = new ArrayList<>();
        List<String> depWasteList = new ArrayList<>();

        if (howManyDaysInPeriod > 0) {
            // top
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                // disGoods
                Map<String, Object> mapDisGoods = new HashMap<>();
                mapDisGoods.put("date", whichDay);
                mapDisGoods.put("disGoodsId", disGoodsId);
                if (type.equals("sales")) {
                    mapDisGoods.put("produce", 0);
                }
                if (type.equals("loss")) {
                    mapDisGoods.put("loss", 0);
                }
                if (type.equals("waste")) {
                    mapDisGoods.put("waste", 0);
                }

                Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
                if (count > 0) {
                    Double aDouble = 0.0;
                    if (type.equals("sales")) {
                        aDouble = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(mapDisGoods);
                        produceList.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("loss")) {
                        aDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                        lossList.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("waste")) {
                        aDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                        wasteList.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                } else {
                    if (type.equals("sales")) {
                        produceList.add("0");
                    }
                    if (type.equals("loss")) {
                        lossList.add("0");
                    }
                    if (type.equals("waste")) {
                        wasteList.add("0");
                    }
                }

                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("date", whichDay);
                mapItem.put("depGoodsId", depGoodsId);
                if (type.equals("sales")) {
                    mapItem.put("produce", 0);
                }
                if (type.equals("loss")) {
                    mapItem.put("loss", 0);
                }
                if (type.equals("waste")) {
                    mapItem.put("waste", 0);
                }
                System.out.println("kanakankaaa" + mapItem);
                GbDepartmentGoodsDailyEntity dailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapItem);
                if (dailyEntity != null) {
                    if (type.equals("sales")) {
                        depProduceList.add(dailyEntity.getGbDgdProfitSubtotal());
                    }
                    if (type.equals("loss")) {
                        depLossList.add(dailyEntity.getGbDgdLossSubtotal());
                    }
                    if (type.equals("waste")) {
                        depWasteList.add(dailyEntity.getGbDgdWasteSubtotal());
                    }

                } else {
                    if (type.equals("sales")) {
                        depProduceList.add("0");
                    }
                    if (type.equals("loss")) {
                        depLossList.add("0");
                    }
                    if (type.equals("waste")) {
                        depWasteList.add("0");
                    }
                }


            }

        } else {
            // dateList
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            // disGoods
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("disGoodsId", disGoodsId);
            if (type.equals("sales")) {
                mapDisGoods.put("produce", 0);
            }
            if (type.equals("loss")) {
                mapDisGoods.put("loss", 0);
            }
            if (type.equals("waste")) {
                mapDisGoods.put("waste", 0);
            }

            Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
            if (count > 0) {
                Double aDouble = 0.0;
                if (type.equals("sales")) {
                    aDouble = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(mapDisGoods);
                    produceList.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("loss")) {
                    aDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                    lossList.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("waste")) {
                    aDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                    wasteList.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            } else {
                if (type.equals("sales")) {
                    produceList.add("0");
                }
                if (type.equals("loss")) {
                    lossList.add("0");
                }
                if (type.equals("waste")) {
                    wasteList.add("0");
                }
            }

            Map<String, Object> mapItem = new HashMap<>();
            mapItem.put("date", startDate);
            mapItem.put("depGoodsId", depGoodsId);
            if (type.equals("sales")) {
                mapItem.put("produce", 0);
            }
            if (type.equals("loss")) {
                mapItem.put("loss", 0);
            }
            if (type.equals("waste")) {
                mapItem.put("waste", 0);
            }
            System.out.println("kanakankaaa" + mapItem);
            GbDepartmentGoodsDailyEntity dailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapItem);
            if (dailyEntity != null) {
                if (type.equals("sales")) {
                    depProduceList.add(dailyEntity.getGbDgdProfitSubtotal());
                }
                if (type.equals("loss")) {
                    depLossList.add(dailyEntity.getGbDgdLossSubtotal());
                }
                if (type.equals("waste")) {
                    depWasteList.add(dailyEntity.getGbDgdWasteSubtotal());
                }

            } else {
                if (type.equals("sales")) {
                    depProduceList.add("0");
                }
                if (type.equals("loss")) {
                    depLossList.add("0");
                }
                if (type.equals("waste")) {
                    depWasteList.add("0");
                }
            }
        }


        map1.put("date", dateList);
        map1.put("sales", produceList);
        map1.put("loss", lossList);
        map1.put("waste", wasteList);
        map1.put("depProduce", depProduceList);
        map1.put("depLoss", depLossList);
        map1.put("depWaste", depWasteList);
        map1.put("produceTotal", aDouble1P);
        map1.put("lossTotal", aDouble1L);
        map1.put("wasteTotal", aDouble1W);
        map1.put("depProduceTotal", depaDouble1P);
        map1.put("depLossTotal", depaDouble1L);
        map1.put("depWasteTotal", depaDouble1W);

        if (aDouble1P != 0.0) {
            BigDecimal ps = new BigDecimal(depaDouble1P).divide(new BigDecimal(aDouble1P), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            map1.put("produceScale", ps);
        } else {
            map1.put("produceScale", 0);
        }
        if (aDouble1L != 0.0) {
            BigDecimal ps = new BigDecimal(depaDouble1L).divide(new BigDecimal(aDouble1L), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            map1.put("lossScale", ps);
        } else {
            map1.put("lossScale", 0);
        }
        if (aDouble1W != 0.0) {
            BigDecimal ps = new BigDecimal(depaDouble1W).divide(new BigDecimal(aDouble1W), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            map1.put("wasteScale", ps);
        } else {
            map1.put("wasteScale", 0);
        }

        GbDistributerGoodsEntity distributerGoodsEntity = distributerGoodsService.queryObject(disGoodsId);
        resultMap.put("top", map1);
        resultMap.put("disGoods", distributerGoodsEntity);


        return R.ok().put("data", resultMap);
    }


    @RequestMapping(value = "/getDepGoodsCharts", method = RequestMethod.POST)
    @ResponseBody
    public R getDepGoodsCharts(Integer depId, String startDate, String stopDate, Integer disGoodsId) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }

        map.put("disGoodsId", disGoodsId);
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        Double aDouble1S = 0.0;
        Double aDouble1SM = 0.0;
        Double aDouble1L = 0.0;
        Double aDouble1LM = 0.0;
        Double aDouble1W = 0.0;
        Double aDouble1WM = 0.0;
        double costDouble = 0.0;
        double costDoubleM = 0.0;
        String maxPrice = "";
        String minPrice = "";
        String perPrice = "";
        double weight = 0.0;
        double subtotal = 0.0;
        double restWeight = 0.0;
        double restSubtotal = 0.0;
        int stockCount = 0;

        if (integer > 0) {
            aDouble1S = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
            aDouble1SM = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
            aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
            aDouble1LM = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
            aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
            aDouble1WM = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);


            costDouble = aDouble1S + aDouble1L + aDouble1W;
            costDoubleM = aDouble1SM + aDouble1LM + aDouble1WM;
        } else {
            return R.error(-1, "没有数据");
        }

        if (depId != -1) {
            map.put("depId", depId);
        }


//        subtotal = gbDepartmentGoodsStockService.queryDepGoodsSubtotal(map);
//        weight = gbDepartmentGoodsStockService.queryDepStockWeightTotal(map);
//        double v = subtotal / weight;
//        perPrice = new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//
//        maxPrice = gbDepartmentGoodsStockService.queryDepStockMaxDgsPrice(map);
//        minPrice = gbDepartmentGoodsStockService.queryDepStockMinDgsPrice(map);
//
//        restWeight = gbDepartmentGoodsStockService.queryDepStockRestWeightTotal(map);
//        restSubtotal = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(map);
//        stockCount = gbDepartmentGoodsStockService.queryGoodsStockCount(map);


        Double depaDouble1P = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
        Double depaDouble1PM = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
        Double depaDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
        Double depaDouble1LM = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
        Double depaDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
        Double depaDouble1WM = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
        double depaDouble1Cost = depaDouble1P + depaDouble1L + depaDouble1W;
        double depaDouble1CostM = (depaDouble1PM + depaDouble1LM + depaDouble1WM);


        Map<String, Object> map1 = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<String> costList = new ArrayList<>();
        List<String> salesList = new ArrayList<>();
        List<String> lossList = new ArrayList<>();
        List<String> wasteList = new ArrayList<>();
        List<String> depCostList = new ArrayList<>();
        List<String> depSalesList = new ArrayList<>();
        List<String> depLossList = new ArrayList<>();
        List<String> depWasteList = new ArrayList<>();

        List<String> costListM = new ArrayList<>();
        List<String> salesListM = new ArrayList<>();
        List<String> lossListM = new ArrayList<>();
        List<String> wasteListM = new ArrayList<>();
        List<String> depCostListM = new ArrayList<>();
        List<String> depSalesListM = new ArrayList<>();
        List<String> depLossListM = new ArrayList<>();
        List<String> depWasteListM = new ArrayList<>();

        List<String> finishTimeList = new ArrayList<>();
        List<String> restWeightList = new ArrayList<>();

        if (howManyDaysInPeriod > 0) {
            // top
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                // disGoods
                Map<String, Object> mapDisGoods = new HashMap<>();
                mapDisGoods.put("date", whichDay);
                mapDisGoods.put("disGoodsId", disGoodsId);
                Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
                if (count > 0) {
                    Double aDoubleS = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDisGoods);
                    Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDisGoods);
                    Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDisGoods);
                    salesList.add(new BigDecimal(aDoubleS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    lossList.add(new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    wasteList.add(new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    double cost = aDoubleS + aDoubleL + aDoubleW;
                    costList.add(new BigDecimal(cost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    Double aDoubleSM = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                    Double aDoubleLM = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                    System.out.println("losssubtottoot" + aDoubleLM);
                    Double aDoubleWM = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                    salesListM.add(new BigDecimal(aDoubleSM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    lossListM.add(new BigDecimal(aDoubleLM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    wasteListM.add(new BigDecimal(aDoubleWM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    double costM = aDoubleSM + aDoubleLM + aDoubleWM;
                    costListM.add(new BigDecimal(costM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                } else {
                    salesList.add("0");
                    lossList.add("0");
                    wasteList.add("0");
                    costList.add("0");
                    salesListM.add("0");
                    lossListM.add("0");
                    wasteListM.add("0");
                    costListM.add("0");
                }

                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("date", whichDay);
                mapItem.put("depId", depId);
                mapItem.put("disGoodsId", disGoodsId);
                GbDepartmentGoodsDailyEntity dailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapItem);
                if (dailyEntity != null) {
                    String gbDgdProduceWeight = dailyEntity.getGbDgdProduceWeight();
                    String gbDgdLossWeight = dailyEntity.getGbDgdLossWeight();
                    String gbDgdWasteWeight = dailyEntity.getGbDgdWasteWeight();
                    depSalesList.add(gbDgdProduceWeight);
                    depLossList.add(gbDgdLossWeight);
                    depWasteList.add(gbDgdWasteWeight);
                    BigDecimal add = new BigDecimal(gbDgdProduceWeight).add(new BigDecimal(gbDgdLossWeight)).add(new BigDecimal(gbDgdWasteWeight));
                    depCostList.add(add.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    String gbDgdProduceWeightM = dailyEntity.getGbDgdProduceSubtotal();
                    String gbDgdLossWeightM = dailyEntity.getGbDgdLossSubtotal();
                    String gbDgdWasteWeightM = dailyEntity.getGbDgdWasteSubtotal();
                    depSalesListM.add(gbDgdProduceWeightM);
                    depLossListM.add(gbDgdLossWeightM);
                    depWasteListM.add(gbDgdWasteWeightM);
                    BigDecimal addM = new BigDecimal(gbDgdProduceWeightM).add(new BigDecimal(gbDgdLossWeightM)).add(new BigDecimal(gbDgdWasteWeightM));
                    depCostListM.add(addM.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                } else {
                    depSalesList.add("0");
                    depLossList.add("0");
                    depWasteList.add("0");
                    depCostList.add("0");
                    depSalesListM.add("0");
                    depLossListM.add("0");
                    depWasteListM.add("0");
                    depCostListM.add("0");
                }

                mapItem.put("equalRestWeight", 0);
                GbDepartmentGoodsDailyEntity dailyEntityFinish = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapItem);
                if (dailyEntityFinish != null) {
                    if (new BigDecimal(dailyEntityFinish.getGbDgdRestWeight()).compareTo(BigDecimal.ZERO) == 0) {
                        String hour = dailyEntityFinish.getGbDgdSellClearHour();
                        String min = dailyEntityFinish.getGbDgdSellClearMinute();
                        BigDecimal multiply = new BigDecimal(hour).multiply(new BigDecimal(60));
                        BigDecimal add = multiply.add(new BigDecimal(min));
                        finishTimeList.add(add.toString());
                    }
                } else {
                    finishTimeList.add("0");
                }

                mapItem.put("equalRestWeight", null);
                mapItem.put("restWeight", 0);
                GbDepartmentGoodsDailyEntity dailyEntityRest = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapItem);
                if (dailyEntityRest != null) {
                    if (new BigDecimal(dailyEntityRest.getGbDgdRestWeight()).compareTo(BigDecimal.ZERO) == 1) {
                        String gbDgsrdRestWeight = dailyEntityRest.getGbDgdRestWeight();
                        restWeightList.add(gbDgsrdRestWeight);
                    }
                } else {
                    restWeightList.add("0");
                }

            }
        } else {
            // dateList
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            // disGoods
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("disGoodsId", disGoodsId);
            Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
            if (count > 0) {
                Double aDoubleS = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDisGoods);
                Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDisGoods);
                Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDisGoods);
                salesList.add(new BigDecimal(aDoubleS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                lossList.add(new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                wasteList.add(new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                double cost = aDoubleS + aDoubleL + aDoubleW;
                costList.add(new BigDecimal(cost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                Double aDoubleSM = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                Double aDoubleLM = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                Double aDoubleWM = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                salesListM.add(new BigDecimal(aDoubleSM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                lossListM.add(new BigDecimal(aDoubleLM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                wasteListM.add(new BigDecimal(aDoubleWM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                double costM = aDoubleSM + aDoubleLM + aDoubleWM;
                costListM.add(new BigDecimal(costM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            } else {
                salesList.add("0");
                lossList.add("0");
                wasteList.add("0");
                costList.add("0");
                salesListM.add("0");
                lossListM.add("0");
                wasteListM.add("0");
                costListM.add("0");
            }

            Map<String, Object> mapItem = new HashMap<>();
            mapItem.put("date", startDate);
            mapItem.put("depId", depId);
            mapItem.put("disGoodsId", disGoodsId);
            GbDepartmentGoodsDailyEntity dailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapItem);
            if (dailyEntity != null) {
                String gbDgdProduceWeight = dailyEntity.getGbDgdProduceWeight();
                String gbDgdLossWeight = dailyEntity.getGbDgdLossWeight();
                String gbDgdWasteWeight = dailyEntity.getGbDgdWasteWeight();
                depSalesList.add(gbDgdProduceWeight);
                depLossList.add(gbDgdLossWeight);
                depWasteList.add(gbDgdWasteWeight);
                BigDecimal add = new BigDecimal(gbDgdProduceWeight).add(new BigDecimal(gbDgdLossWeight)).add(new BigDecimal(gbDgdWasteWeight));
                depCostList.add(add.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                String gbDgdProduceWeightM = dailyEntity.getGbDgdProduceSubtotal();
                String gbDgdLossWeightM = dailyEntity.getGbDgdLossSubtotal();
                String gbDgdWasteWeightM = dailyEntity.getGbDgdWasteSubtotal();
                depSalesListM.add(gbDgdProduceWeightM);
                depLossListM.add(gbDgdLossWeightM);
                depWasteListM.add(gbDgdWasteWeightM);
                BigDecimal addM = new BigDecimal(gbDgdProduceWeightM).add(new BigDecimal(gbDgdLossWeightM)).add(new BigDecimal(gbDgdWasteWeightM));
                depCostListM.add(addM.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            } else {
                depSalesList.add("0");
                depLossList.add("0");
                depWasteList.add("0");
                depCostList.add("0");
                depSalesListM.add("0");
                depLossListM.add("0");
                depWasteListM.add("0");
                depCostListM.add("0");
            }

            mapItem.put("equalRestWeight", 0);
            GbDepartmentGoodsDailyEntity dailyEntityFinish = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapItem);
            if (dailyEntityFinish != null) {
                if (new BigDecimal(dailyEntityFinish.getGbDgdRestWeight()).compareTo(BigDecimal.ZERO) == 0) {
                    String hour = dailyEntityFinish.getGbDgdSellClearHour();
                    String min = dailyEntityFinish.getGbDgdSellClearMinute();
                    BigDecimal multiply = new BigDecimal(hour).multiply(new BigDecimal(60));
                    BigDecimal add = multiply.add(new BigDecimal(min));
                    finishTimeList.add(add.toString());
                }
            } else {
                finishTimeList.add("0");
            }

            mapItem.put("equalRestWeight", null);
            mapItem.put("restWeight", 0);
            GbDepartmentGoodsDailyEntity dailyEntityRest = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapItem);
            if (dailyEntityRest != null) {
                if (new BigDecimal(dailyEntityRest.getGbDgdRestWeight()).compareTo(BigDecimal.ZERO) == 1) {
                    String gbDgsrdRestWeight = dailyEntityRest.getGbDgdRestWeight();
                    restWeightList.add(gbDgsrdRestWeight);
                }
            } else {
                restWeightList.add("0");
            }
        }


        map1.put("date", dateList);
        map1.put("cost", costList);
        map1.put("sales", salesList);
        map1.put("loss", lossList);
        map1.put("waste", wasteList);
        map1.put("depCost", depCostList);
        map1.put("depSales", depSalesList);
        map1.put("depLoss", depLossList);
        map1.put("depWaste", depWasteList);

        map1.put("costM", costListM);
        map1.put("salesM", salesListM);
        map1.put("lossM", lossListM);
        map1.put("wasteM", wasteListM);
        map1.put("depCostM", depCostListM);
        map1.put("depSalesM", depSalesListM);
        map1.put("depLossM", depLossListM);
        map1.put("depWasteM", depWasteListM);

        map1.put("finishTime", finishTimeList);
        map1.put("restWeight", restWeightList);

        map1.put("costTotal", new BigDecimal(costDouble).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("salesTotal", new BigDecimal(aDouble1S).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("lossTotal", new BigDecimal(aDouble1L).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("wasteTotal", new BigDecimal(aDouble1W).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("depCostTotal", new BigDecimal(depaDouble1Cost).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("depSalesTotal", new BigDecimal(depaDouble1P).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("depLossTotal", new BigDecimal(depaDouble1L).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("depWasteTotal", new BigDecimal(depaDouble1W).setScale(1, BigDecimal.ROUND_HALF_UP));

        map1.put("costTotalM", new BigDecimal(costDoubleM).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("salesTotalM", new BigDecimal(aDouble1SM).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("lossTotalM", new BigDecimal(aDouble1LM).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("wasteTotalM", new BigDecimal(aDouble1WM).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("depCostTotalM", new BigDecimal(depaDouble1CostM).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("depSalesTotalM",  new BigDecimal(depaDouble1PM).setScale(1, BigDecimal.ROUND_HALF_UP) );
        map1.put("depLossTotalM",   new BigDecimal(depaDouble1LM).setScale(1, BigDecimal.ROUND_HALF_UP) );
        map1.put("depWasteTotalM", new BigDecimal(depaDouble1WM).setScale(1, BigDecimal.ROUND_HALF_UP) );
//        map1.put("depPerPrice", perPrice);
//        map1.put("depMaxPrice", maxPrice);
//        map1.put("depMinPrice", minPrice);
        map1.put("depRestWeight", new BigDecimal(restWeight).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("depRestSubtotal", new BigDecimal(restSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("depSubtotal", new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP) );
        map1.put("depWeight", new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("depStockCount", new BigDecimal(stockCount).setScale(1, BigDecimal.ROUND_HALF_UP));

        if (costDouble != 0.0) {
            BigDecimal ps = new BigDecimal(depaDouble1Cost).divide(new BigDecimal(costDouble), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            map1.put("costScale", ps);
        } else {
            map1.put("costScale", 0);
        }
        if (aDouble1S != 0.0) {
            BigDecimal ps = new BigDecimal(depaDouble1P).divide(new BigDecimal(aDouble1S), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            map1.put("salesScale", ps);
        } else {
            map1.put("salesScale", 0);
        }
        if (aDouble1L != 0.0) {
            BigDecimal ps = new BigDecimal(depaDouble1L).divide(new BigDecimal(aDouble1L), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            map1.put("lossScale", ps);
        } else {
            map1.put("lossScale", 0);
        }
        if (aDouble1W != 0.0) {
            BigDecimal ps = new BigDecimal(depaDouble1W).divide(new BigDecimal(aDouble1W), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            map1.put("wasteScale", ps);
        } else {
            map1.put("wasteScale", 0);
        }

        GbDistributerGoodsEntity distributerGoodsEntity = distributerGoodsService.queryObject(disGoodsId);
        resultMap.put("top", map1);
        resultMap.put("disGoods", distributerGoodsEntity);
        return R.ok().put("data", resultMap);
    }


    @RequestMapping(value = "/getDisGoodsCharts", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsCharts(String startDate, String stopDate, Integer disGoodsId, Integer depGoodsId) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }

        map.put("disGoodsId", disGoodsId);
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        Double aDouble1S = 0.0;
        Double aDouble1SM = 0.0;
        Double aDouble1L = 0.0;
        Double aDouble1LM = 0.0;
        Double aDouble1W = 0.0;
        Double aDouble1WM = 0.0;
        double costDouble = 0.0;
        double costDoubleM = 0.0;
        String maxPrice = "";
        String minPrice = "";
        String perPrice = "";
        double weight = 0.0;
        double subtotal = 0.0;
        double restWeight = 0.0;
        double restSubtotal = 0.0;
        int stockCount = 0;

        if (integer > 0) {
            aDouble1S = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
            aDouble1SM = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
            aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
            aDouble1LM = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
            aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
            aDouble1WM = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);


            costDouble = aDouble1S + aDouble1L + aDouble1W;
            costDoubleM = aDouble1SM + aDouble1LM + aDouble1WM;
        } else {
            return R.error(-1, "没有数据");
        }

        map.put("depGoodsId", depGoodsId);


        subtotal = gbDepartmentGoodsStockService.queryDepGoodsSubtotal(map);
        weight = gbDepartmentGoodsStockService.queryDepStockWeightTotal(map);
        double v = subtotal / weight;
        perPrice = new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();

        maxPrice = gbDepartmentGoodsStockService.queryDepStockMaxDgsPrice(map);
        minPrice = gbDepartmentGoodsStockService.queryDepStockMinDgsPrice(map);

        restWeight = gbDepartmentGoodsStockService.queryDepStockRestWeightTotal(map);
        restSubtotal = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(map);
        stockCount = gbDepartmentGoodsStockService.queryGoodsStockCount(map);


        Double depaDouble1P = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
        Double depaDouble1PM = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
        Double depaDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
        Double depaDouble1LM = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
        Double depaDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
        Double depaDouble1WM = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
        double depaDouble1Cost = depaDouble1P + depaDouble1L + depaDouble1W;
        double depaDouble1CostM = depaDouble1PM + depaDouble1LM + depaDouble1WM;


        Map<String, Object> map1 = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<String> costList = new ArrayList<>();
        List<String> salesList = new ArrayList<>();
        List<String> lossList = new ArrayList<>();
        List<String> wasteList = new ArrayList<>();
        List<String> depCostList = new ArrayList<>();
        List<String> depSalesList = new ArrayList<>();
        List<String> depLossList = new ArrayList<>();
        List<String> depWasteList = new ArrayList<>();

        List<String> costListM = new ArrayList<>();
        List<String> salesListM = new ArrayList<>();
        List<String> lossListM = new ArrayList<>();
        List<String> wasteListM = new ArrayList<>();
        List<String> depCostListM = new ArrayList<>();
        List<String> depSalesListM = new ArrayList<>();
        List<String> depLossListM = new ArrayList<>();
        List<String> depWasteListM = new ArrayList<>();

        List<String> finishTimeList = new ArrayList<>();
        List<String> restWeightList = new ArrayList<>();

        if (howManyDaysInPeriod > 0) {
            // top
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                // disGoods
                Map<String, Object> mapDisGoods = new HashMap<>();
                mapDisGoods.put("date", whichDay);
                mapDisGoods.put("disGoodsId", disGoodsId);
                Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
                if (count > 0) {
                    Double aDoubleS = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDisGoods);
                    Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDisGoods);
                    Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDisGoods);
                    salesList.add(new BigDecimal(aDoubleS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    lossList.add(new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    wasteList.add(new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    double cost = aDoubleS + aDoubleL + aDoubleW;
                    costList.add(new BigDecimal(cost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    Double aDoubleSM = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                    Double aDoubleLM = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                    System.out.println("losssubtottoot" + aDoubleLM);
                    Double aDoubleWM = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                    salesListM.add(new BigDecimal(aDoubleSM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    lossListM.add(new BigDecimal(aDoubleLM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    wasteListM.add(new BigDecimal(aDoubleWM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    double costM = aDoubleSM + aDoubleLM + aDoubleWM;
                    costListM.add(new BigDecimal(costM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                } else {
                    salesList.add("0");
                    lossList.add("0");
                    wasteList.add("0");
                    costList.add("0");
                    salesListM.add("0");
                    lossListM.add("0");
                    wasteListM.add("0");
                    costListM.add("0");
                }

                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("date", whichDay);
                mapItem.put("depGoodsId", depGoodsId);
                GbDepartmentGoodsDailyEntity dailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapItem);
                if (dailyEntity != null) {
                    String gbDgdProduceWeight = dailyEntity.getGbDgdProduceWeight();
                    String gbDgdLossWeight = dailyEntity.getGbDgdLossWeight();
                    String gbDgdWasteWeight = dailyEntity.getGbDgdWasteWeight();
                    depSalesList.add(gbDgdProduceWeight);
                    depLossList.add(gbDgdLossWeight);
                    depWasteList.add(gbDgdWasteWeight);
                    BigDecimal add = new BigDecimal(gbDgdProduceWeight).add(new BigDecimal(gbDgdLossWeight)).add(new BigDecimal(gbDgdWasteWeight));
                    depCostList.add(add.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    String gbDgdProduceWeightM = dailyEntity.getGbDgdProduceSubtotal();
                    String gbDgdLossWeightM = dailyEntity.getGbDgdLossSubtotal();
                    String gbDgdWasteWeightM = dailyEntity.getGbDgdWasteSubtotal();
                    depSalesListM.add(gbDgdProduceWeightM);
                    depLossListM.add(gbDgdLossWeightM);
                    depWasteListM.add(gbDgdWasteWeightM);
                    BigDecimal addM = new BigDecimal(gbDgdProduceWeightM).add(new BigDecimal(gbDgdLossWeightM)).add(new BigDecimal(gbDgdWasteWeightM));
                    depCostListM.add(addM.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                } else {
                    depSalesList.add("0");
                    depLossList.add("0");
                    depWasteList.add("0");
                    depCostList.add("0");
                    depSalesListM.add("0");
                    depLossListM.add("0");
                    depWasteListM.add("0");
                    depCostListM.add("0");
                }

                mapItem.put("equalRestWeight", 0);
                GbDepartmentGoodsDailyEntity dailyEntityFinish = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapItem);
                if (dailyEntityFinish != null) {
                    if (new BigDecimal(dailyEntityFinish.getGbDgdRestWeight()).compareTo(BigDecimal.ZERO) == 0) {
                        String hour = dailyEntityFinish.getGbDgdSellClearHour();
                        String min = dailyEntityFinish.getGbDgdSellClearMinute();
                        BigDecimal multiply = new BigDecimal(hour).multiply(new BigDecimal(60));
                        BigDecimal add = multiply.add(new BigDecimal(min));
                        finishTimeList.add(add.toString());
                    }
                } else {
                    finishTimeList.add("0");
                }

                mapItem.put("equalRestWeight", null);
                mapItem.put("restWeight", 0);
                GbDepartmentGoodsDailyEntity dailyEntityRest = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapItem);
                if (dailyEntityRest != null) {
                    if (new BigDecimal(dailyEntityRest.getGbDgdRestWeight()).compareTo(BigDecimal.ZERO) == 1) {
                        String gbDgsrdRestWeight = dailyEntityRest.getGbDgdRestWeight();
                        restWeightList.add(gbDgsrdRestWeight);
                    }
                } else {
                    restWeightList.add("0");
                }

            }
        } else {
            // dateList
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            // disGoods
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("disGoodsId", disGoodsId);
            Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
            if (count > 0) {
                Double aDoubleS = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDisGoods);
                Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDisGoods);
                Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDisGoods);
                salesList.add(new BigDecimal(aDoubleS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                lossList.add(new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                wasteList.add(new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                double cost = aDoubleS + aDoubleL + aDoubleW;
                costList.add(new BigDecimal(cost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                Double aDoubleSM = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                Double aDoubleLM = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                Double aDoubleWM = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                salesListM.add(new BigDecimal(aDoubleSM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                lossListM.add(new BigDecimal(aDoubleLM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                wasteListM.add(new BigDecimal(aDoubleWM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                double costM = aDoubleSM + aDoubleLM + aDoubleWM;
                costListM.add(new BigDecimal(costM).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            } else {
                salesList.add("0");
                lossList.add("0");
                wasteList.add("0");
                costList.add("0");
                salesListM.add("0");
                lossListM.add("0");
                wasteListM.add("0");
                costListM.add("0");
            }

            Map<String, Object> mapItem = new HashMap<>();
            mapItem.put("date", startDate);
            mapItem.put("depGoodsId", depGoodsId);
            GbDepartmentGoodsDailyEntity dailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapItem);
            if (dailyEntity != null) {
                String gbDgdProduceWeight = dailyEntity.getGbDgdProduceWeight();
                String gbDgdLossWeight = dailyEntity.getGbDgdLossWeight();
                String gbDgdWasteWeight = dailyEntity.getGbDgdWasteWeight();
                depSalesList.add(gbDgdProduceWeight);
                depLossList.add(gbDgdLossWeight);
                depWasteList.add(gbDgdWasteWeight);
                BigDecimal add = new BigDecimal(gbDgdProduceWeight).add(new BigDecimal(gbDgdLossWeight)).add(new BigDecimal(gbDgdWasteWeight));
                depCostList.add(add.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                String gbDgdProduceWeightM = dailyEntity.getGbDgdProduceSubtotal();
                String gbDgdLossWeightM = dailyEntity.getGbDgdLossSubtotal();
                String gbDgdWasteWeightM = dailyEntity.getGbDgdWasteSubtotal();
                depSalesListM.add(gbDgdProduceWeightM);
                depLossListM.add(gbDgdLossWeightM);
                depWasteListM.add(gbDgdWasteWeightM);
                BigDecimal addM = new BigDecimal(gbDgdProduceWeightM).add(new BigDecimal(gbDgdLossWeightM)).add(new BigDecimal(gbDgdWasteWeightM));
                depCostListM.add(addM.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            } else {
                depSalesList.add("0");
                depLossList.add("0");
                depWasteList.add("0");
                depCostList.add("0");
                depSalesListM.add("0");
                depLossListM.add("0");
                depWasteListM.add("0");
                depCostListM.add("0");
            }

            mapItem.put("equalRestWeight", 0);
            GbDepartmentGoodsDailyEntity dailyEntityFinish = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapItem);
            if (dailyEntityFinish != null) {
                if (new BigDecimal(dailyEntityFinish.getGbDgdRestWeight()).compareTo(BigDecimal.ZERO) == 0) {
                    String hour = dailyEntityFinish.getGbDgdSellClearHour();
                    String min = dailyEntityFinish.getGbDgdSellClearMinute();
                    BigDecimal multiply = new BigDecimal(hour).multiply(new BigDecimal(60));
                    BigDecimal add = multiply.add(new BigDecimal(min));
                    finishTimeList.add(add.toString());
                }
            } else {
                finishTimeList.add("0");
            }

            mapItem.put("equalRestWeight", null);
            mapItem.put("restWeight", 0);
            GbDepartmentGoodsDailyEntity dailyEntityRest = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapItem);
            if (dailyEntityRest != null) {
                if (new BigDecimal(dailyEntityRest.getGbDgdRestWeight()).compareTo(BigDecimal.ZERO) == 1) {
                    String gbDgsrdRestWeight = dailyEntityRest.getGbDgdRestWeight();
                    restWeightList.add(gbDgsrdRestWeight);
                }
            } else {
                restWeightList.add("0");
            }
        }


        map1.put("date", dateList);
        map1.put("cost", costList);
        map1.put("sales", salesList);
        map1.put("loss", lossList);
        map1.put("waste", wasteList);
        map1.put("depCost", depCostList);
        map1.put("depSales", depSalesList);
        map1.put("depLoss", depLossList);
        map1.put("depWaste", depWasteList);

        map1.put("costM", costListM);
        map1.put("salesM", salesListM);
        map1.put("lossM", lossListM);
        map1.put("wasteM", wasteListM);
        map1.put("depCostM", depCostListM);
        map1.put("depSalesM", depSalesListM);
        map1.put("depLossM", depLossListM);
        map1.put("depWasteM", depWasteListM);

        map1.put("finishTime", finishTimeList);
        map1.put("restWeight", restWeightList);

        map1.put("costTotal", new BigDecimal(costDouble).setScale(1,BigDecimal.ROUND_HALF_UP) );
        map1.put("salesTotal", new BigDecimal(aDouble1S).setScale(1,BigDecimal.ROUND_HALF_UP));
        map1.put("lossTotal", new BigDecimal(aDouble1L).setScale(1,BigDecimal.ROUND_HALF_UP));
        map1.put("wasteTotal", new BigDecimal(aDouble1W).setScale(1,BigDecimal.ROUND_HALF_UP));
        map1.put("depCostTotal", new BigDecimal(depaDouble1Cost).setScale(1,BigDecimal.ROUND_HALF_UP));
        map1.put("depSalesTotal", new BigDecimal(depaDouble1P).setScale(1,BigDecimal.ROUND_HALF_UP));
        map1.put("depLossTotal", new BigDecimal(depaDouble1L).setScale(1,BigDecimal.ROUND_HALF_UP));
        map1.put("depWasteTotal", new BigDecimal(depaDouble1W).setScale(1,BigDecimal.ROUND_HALF_UP));

        map1.put("costTotalM", new BigDecimal(costDoubleM).setScale(1,BigDecimal.ROUND_HALF_UP));
        map1.put("salesTotalM", new BigDecimal(aDouble1SM).setScale(1,BigDecimal.ROUND_HALF_UP));
        map1.put("lossTotalM", new BigDecimal(aDouble1LM).setScale(1,BigDecimal.ROUND_HALF_UP));
        map1.put("wasteTotalM", new BigDecimal(aDouble1WM).setScale(1,BigDecimal.ROUND_HALF_UP));
        map1.put("depCostTotalM", depaDouble1CostM);
        map1.put("depSalesTotalM", depaDouble1PM);
        map1.put("depLossTotalM", depaDouble1LM);
        map1.put("depWasteTotalM", depaDouble1WM);
        map1.put("depPerPrice", perPrice);
        map1.put("depMaxPrice", maxPrice);
        map1.put("depMinPrice", minPrice);
        map1.put("depRestWeight", restWeight);
        map1.put("depRestSubtotal", restSubtotal);
        map1.put("depSubtotal", subtotal);
        map1.put("depWeight", weight);
        map1.put("depStockCount", stockCount);

        if (costDouble != 0.0) {
            BigDecimal ps = new BigDecimal(depaDouble1Cost).divide(new BigDecimal(costDouble), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            map1.put("costScale", ps);
        } else {
            map1.put("costScale", 0);
        }
        if (aDouble1S != 0.0) {
            BigDecimal ps = new BigDecimal(depaDouble1P).divide(new BigDecimal(aDouble1S), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            map1.put("salesScale", ps);
        } else {
            map1.put("salesScale", 0);
        }
        if (aDouble1L != 0.0) {
            BigDecimal ps = new BigDecimal(depaDouble1L).divide(new BigDecimal(aDouble1L), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            map1.put("lossScale", ps);
        } else {
            map1.put("lossScale", 0);
        }
        if (aDouble1W != 0.0) {
            BigDecimal ps = new BigDecimal(depaDouble1W).divide(new BigDecimal(aDouble1W), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            map1.put("wasteScale", ps);
        } else {
            map1.put("wasteScale", 0);
        }

        GbDistributerGoodsEntity distributerGoodsEntity = distributerGoodsService.queryObject(disGoodsId);
        resultMap.put("top", map1);
        resultMap.put("disGoods", distributerGoodsEntity);
        return R.ok().put("data", resultMap);
    }


    private Map<String, Object> getGoodsChartsProfit(String startDate, String stopDate, Integer disGoodsId,
                                                     String type, Integer searchDepId) {

        System.out.println("typepepe=" + type);
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }


        List<String> dateList = new ArrayList<>();
        List<String> list = new ArrayList<>();

        Map<String, Object> map0 = new HashMap<>();
        if (howManyDaysInPeriod > 0) {
            map0.put("startDate", startDate);
            map0.put("stopDate", stopDate);
        } else {
            map0.put("date", startDate);
        }
        map0.put("disGoodsId", disGoodsId);
        if (searchDepId != -1) {
            map0.put("depId", searchDepId);
        }
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
        System.out.println("chaonimaimamamamama" + integer1);
        if (integer1 == 0) {
            System.out.println("zzhiziziiaaaa");
            return R.error(-1, "没有数据");
        }

        Double weightTotalP = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map0);
        Double weightTotalS = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map0);
        Double weightTotalTL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map0);
        Double weightTotalTW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map0);


        if (howManyDaysInPeriod > 0) {
            // top
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                Map<String, Object> map = new HashMap<>();
                map.put("date", whichDay);
                map.put("disGoodsId", disGoodsId);
//                if (searchDepId != -1) {
//                    map.put("depId", searchDepId);
//                }
                if (type.equals("sales")) {
                    map.put("produce", 0);
                }
                if (type.equals("loss")) {
                    map.put("loss", 0);
                }
                if (type.equals("waste")) {
                    map.put("waste", 0);
                }
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);

                Double weightTotal = 0.0;
                if (integer > 0) {
                    if (type.equals("profit")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map);
                    }
                    if (type.equals("sales")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map);
                    }
                    if (type.equals("loss")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                    }
                    if (type.equals("waste")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                    }
                    list.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                } else {
                    list.add("0");
                }

            }
        } else {
            // dateList
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            Map<String, Object> map = new HashMap<>();
            map.put("date", startDate);
            map.put("disGoodsId", disGoodsId);
//            if (searchDepId != -1) {
//                map.put("depId", searchDepId);
//            }
            if (type.equals("sales")) {
                map.put("produce", 0);
            }
            if (type.equals("loss")) {
                map.put("loss", 0);
            }
            if (type.equals("waste")) {
                map.put("waste", 0);
            }
            Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);

            Double weightTotal = 0.0;
            if (integer > 0) {
                if (type.equals("profit")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map);
                }
                if (type.equals("sales")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map);
                }
                if (type.equals("loss")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                }
                if (type.equals("waste")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                }
                list.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            } else {
                list.add("0");
            }

        }


        //dep
        Map<String, Object> mapDep = new HashMap<>();
        if (howManyDaysInPeriod > 0) {
            mapDep.put("startDate", startDate);
            mapDep.put("stopDate", stopDate);
        } else {
            mapDep.put("date", startDate);
        }
        mapDep.put("disGoodsId", disGoodsId);
        if (searchDepId != -1) {
            mapDep.put("depId", searchDepId);
        }
        if (type.equals("sales")) {
            mapDep.put("produce", 0);
        }
        if (type.equals("loss")) {
            mapDep.put("loss", 0);
        }
        if (type.equals("waste")) {
            mapDep.put("waste", 0);
        }
        TreeSet<GbDepartmentEntity> departmentEntities = gbDepGoodsDailyService.queryWhichDepsHasProduceDepGoodsDaily(mapDep);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                Integer gbDepartmentId = department.getGbDepartmentId();
                Map<String, Object> mapdepTotal = new HashMap<>();
                mapdepTotal.put("depId", gbDepartmentId);
                mapdepTotal.put("disGoodsId", disGoodsId);
                if (howManyDaysInPeriod > 0) {
                    mapdepTotal.put("startDate", startDate);
                    mapdepTotal.put("stopDate", stopDate);
                } else {
                    mapdepTotal.put("date", startDate);
                }

                if (type.equals("profit")) {
                    Double toto = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(mapdepTotal);
                    department.setDepProfitGoodsTotal(toto);
                    department.setDepProfitGoodsTotalString(new BigDecimal(toto).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("sales")) {
                    Double toto = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(mapdepTotal);
                    department.setDepProduceGoodsTotal(toto);
                    department.setDepProduceGoodsTotalString(new BigDecimal(toto).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }

                if (type.equals("loss")) {
                    Double toto = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapdepTotal);
                    department.setDepLossGoodsTotal(toto);
                    department.setDepLossGoodsTotalString(new BigDecimal(toto).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("waste")) {
                    Double toto = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapdepTotal);
                    department.setDepWasteGoodsTotal(toto);
                    department.setDepWasteGoodsTotalString(new BigDecimal(toto).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }

        }


        GbDistributerGoodsEntity distributerGoodsEntity = distributerGoodsService.queryObject(disGoodsId);
        Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("date", dateList);
        mapResult.put("list", list);
        if (type.equals("profit")) {
            mapResult.put("dep", abcDepGoodsProfit(departmentEntities));
        }
        if (type.equals("sales")) {
            mapResult.put("dep", abcDepGoodsSales(departmentEntities));
        }
        if (type.equals("loss")) {
            mapResult.put("dep", abcDepGoodsLoss(departmentEntities));
        }
        if (type.equals("waste")) {
            mapResult.put("dep", abcDepGoodsWaste(departmentEntities));
        }
        mapResult.put("oneTotal", new BigDecimal(weightTotalP).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("salesTotal", new BigDecimal(weightTotalS).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("lossTotal", new BigDecimal(weightTotalTL).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("wasteTotal", new BigDecimal(weightTotalTW).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("disGoods", distributerGoodsEntity);
        mapResult.put("code", "0");

        return mapResult;
    }

    private Map<String, Object> getGoodsChartsCost(String startDate, String stopDate, Integer disGoodsId,
                                                   String type, Integer searchDepId) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        GbDistributerGoodsEntity distributerGoodsEntity = distributerGoodsService.queryObject(disGoodsId);
        Map<String, Object> mapResult = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<String> list = new ArrayList<>();
        List<Map<String, Object>> listItem = new ArrayList<>();

        Map<String, Object> map0 = new HashMap<>();
        map0.put("depType", getGbDepartmentTypeMendian());
        if (howManyDaysInPeriod > 0) {
            map0.put("startDate", startDate);
            map0.put("stopDate", stopDate);
        } else {
            map0.put("date", startDate);
        }
        map0.put("disGoodsId", disGoodsId);
        if (searchDepId != -1) {
            map0.put("depId", searchDepId);
        }
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
        if (integer1 == 0) {
            return R.error(-1, "没有数据");
        }

        System.out.println("appdpododod9999999" + map0);
        Double weightTotalTL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map0);
        System.out.println("---------------------------dafafjdas;lfkjas;lfjas;lkjf;saljkf;lasjf");
        Double weightTotalTW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map0);
        Double weightTotalS = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map0);

        double weightTotalP = weightTotalS + weightTotalTL + weightTotalTW;
        System.out.println("whhwhwhwhwhwhwhwhhwhhwhwh" + weightTotalP);

        if (howManyDaysInPeriod > 0) {
            // top
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);


                Map<String, Object> map = new HashMap<>();
                map.put("date", whichDay);
                map.put("disGoodsId", disGoodsId);
                map.put("depType", getGbDepartmentTypeMendian());
//                if (searchDepId != -1) {
//                    map.put("depId", searchDepId);
//                }
                if (type.equals("sales")) {
                    map.put("produce", 0);
                }
                if (type.equals("loss")) {
                    map.put("loss", 0);
                }
                if (type.equals("waste")) {
                    map.put("waste", 0);
                }

                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                Double weightTotal = 0.0;
                if (integer > 0) {
                    if (type.equals("total")) {
                        Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                        Double wastSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                        System.out.println("mohahhaehhehehhehehhe" + map);
                        Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                        weightTotal = produceSubtotal + lossSubtotal + wastSubtotal;
                    }
                    if (type.equals("sales")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                    }
                    if (type.equals("loss")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                    }
                    if (type.equals("waste")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                    }
                    list.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    Map<String, Object> mapItem = new HashMap<>();
                    mapItem.put("date",whichDay );
                    mapItem.put("value", new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString() );
                     listItem.add(mapItem);


                } else {
                    list.add("0");
                    Map<String, Object> mapItem = new HashMap<>();
                    mapItem.put("date",whichDay );
                    mapItem.put("value", "0.0");
                    listItem.add(mapItem);
                }

            }
        } else {
            // dateList
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            Map<String, Object> map = new HashMap<>();
            map.put("date", startDate);
            map.put("disGoodsId", disGoodsId);
            map.put("depType", getGbDepartmentTypeMendian());
//            if (searchDepId != -1) {
//                map.put("depId", searchDepId);
//            }
            if (type.equals("sales")) {
                map.put("produce", 0);
            }
            if (type.equals("loss")) {
                map.put("loss", 0);
            }
            if (type.equals("waste")) {
                map.put("waste", 0);
            }
            Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);

            Double weightTotal = 0.0;
            if (integer > 0) {
                if (type.equals("total")) {
                    Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                    Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                    Double wastSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                    weightTotal = produceSubtotal + lossSubtotal + wastSubtotal;

                }
                if (type.equals("sales")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                }
                if (type.equals("loss")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                }
                if (type.equals("waste")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                }
                list.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            } else {
                list.add("0");
            }
        }


        //dep
        Map<String, Object> mapDep = new HashMap<>();
        if (howManyDaysInPeriod > 0) {
            mapDep.put("startDate", startDate);
            mapDep.put("stopDate", stopDate);
        } else {
            mapDep.put("date", startDate);
        }
        mapDep.put("depType", getGbDepartmentTypeMendian());
        mapDep.put("disGoodsId", disGoodsId);
        if (searchDepId != -1) {
            mapDep.put("depId", searchDepId);
        }
        if (type.equals("sales")) {
            mapDep.put("produce", 0);
        }
        if (type.equals("loss")) {
            mapDep.put("loss", 0);
        }
        if (type.equals("waste")) {
            mapDep.put("waste", 0);
        }
        TreeSet<GbDepartmentEntity> departmentEntities = gbDepGoodsDailyService.queryWhichDepsHasProduceDepGoodsDaily(mapDep);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                Integer gbDepartmentId = department.getGbDepartmentId();
                Map<String, Object> mapdepTotal = new HashMap<>();
                mapdepTotal.put("depId", gbDepartmentId);
                if (howManyDaysInPeriod > 0) {
                    mapdepTotal.put("startDate", startDate);
                    mapdepTotal.put("stopDate", stopDate);
                } else {
                    mapdepTotal.put("date", startDate);
                }

                if (type.equals("total")) {
                    Double totoP = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapdepTotal);
                    Double totoL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapdepTotal);
                    Double totoW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapdepTotal);
                    double toto = totoP + totoL + totoW;
                    department.setDepCostGoodsTotal(toto);
                    department.setDepCostGoodsTotalString(new BigDecimal(toto).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("sales")) {
                    Double toto = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapdepTotal);
                    department.setDepProduceGoodsTotal(toto);
                    department.setDepProduceGoodsTotalString(new BigDecimal(toto).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }

                if (type.equals("loss")) {
                    Double toto = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapdepTotal);
                    department.setDepLossGoodsTotal(toto);
                    department.setDepLossGoodsTotalString(new BigDecimal(toto).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("waste")) {
                    Double toto = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapdepTotal);
                    department.setDepWasteGoodsTotal(toto);
                    department.setDepWasteGoodsTotalString(new BigDecimal(toto).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }
        }

        mapResult.put("list", list);
        if (type.equals("total")) {
            mapResult.put("dep", abcDepGoodsCost(departmentEntities));
            mapResult.put("itemList", listItem);
        }
        if (type.equals("sales")) {
            mapResult.put("dep", abcDepGoodsSales(departmentEntities));
            mapResult.put("itemList", listItem);
        }
        if (type.equals("loss")) {
            mapResult.put("dep", abcDepGoodsLoss(departmentEntities));
            mapResult.put("itemList", listItem);
        }
        if (type.equals("waste")) {
            mapResult.put("dep", abcDepGoodsWaste(departmentEntities));
            mapResult.put("itemList", listItem);
        }
        mapResult.put("oneTotal", new BigDecimal(weightTotalP).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("salesTotal", new BigDecimal(weightTotalS).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("lossTotal", new BigDecimal(weightTotalTL).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("wasteTotal", new BigDecimal(weightTotalTW).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("disGoods", distributerGoodsEntity);
        mapResult.put("date", dateList);
        mapResult.put("code", "0");

        return mapResult;
    }


    @RequestMapping(value = "/getGoodsCharts", method = RequestMethod.POST)
    @ResponseBody
    public R getGoodsCharts(String startDate, String stopDate, Integer disGoodsId, String type,
                            String fenxiType, Integer searchDepId) {
        Map<String, Object> goodsChartsWeight = new HashMap<>();

        System.out.println("fenxixixixiiixi" + fenxiType);
        if (fenxiType.equals("weightEcharts")) {
            goodsChartsWeight = getGoodsChartsWeight(startDate, stopDate, disGoodsId, type, searchDepId);
        }
        if (fenxiType.equals("profitEcharts")) {
            goodsChartsWeight = getGoodsChartsProfit(startDate, stopDate, disGoodsId, type, searchDepId);
        }
        if (fenxiType.equals("costEcharts")) {
            System.out.println("fennenenennenenennenenennenenennen");
            goodsChartsWeight = getGoodsChartsCost(startDate, stopDate, disGoodsId, type, searchDepId);
        }
        if (goodsChartsWeight.get("code").equals("0")) {
            return R.ok().put("data", goodsChartsWeight);
        } else {
            return R.error(-1, "没有数据");
        }
    }

    private Map<String, Object> getGoodsChartsWeight(String startDate, String stopDate, Integer disGoodsId,
                                                     String type, Integer searchDepId) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> list = new ArrayList<>();
        List<Map<String, Object>> listItem = new ArrayList<>();

        Map<String, Object> map0 = new HashMap<>();
        if (howManyDaysInPeriod > 0) {
            map0.put("startDate", startDate);
            map0.put("stopDate", stopDate);
        } else {
            map0.put("date", startDate);
        }

        map0.put("disGoodsId", disGoodsId);
        if (searchDepId != -1) {
            map0.put("depId", searchDepId);
        }
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
        if (integer1 == 0) {
            System.out.println("zzhiziziiaaaa");
            return R.error(-1, "没有数据");
        }

        Double weightTotalT = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map0);
        Double weightTotalTL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map0);
        Double weightTotalTW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map0);
        double weightTotalTC = weightTotalT + weightTotalTL + weightTotalTW;


        if (howManyDaysInPeriod > 0) {
            // top
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                Map<String, Object> map = new HashMap<>();
                map.put("date", whichDay);
                map.put("disGoodsId", disGoodsId);
//                if (searchDepId != -1) {
//                    map.put("depId", searchDepId);
//                }
                if (type.equals("sales")) {
                    map.put("produce", 0);
                }
                if (type.equals("loss")) {
                    map.put("loss", 0);
                }
                if (type.equals("waste")) {
                    map.put("waste", 0);
                }


                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                Double weightTotal = 0.0;
                if (integer > 0) {
                    if (type.equals("cost")) {
                        Double weightTotalP = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
                        Double weightTotalL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
                        Double weightTotalW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
                        weightTotal = weightTotalP + weightTotalL + weightTotalW;
                    }
                    if (type.equals("sales")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
                    }
                    if (type.equals("loss")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
                    }
                    if (type.equals("waste")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
                    }
                    list.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    Map<String, Object> mapItem = new HashMap<>();
                    mapItem.put("date",whichDay );
                    mapItem.put("value", new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString() );
                    listItem.add(mapItem);
                } else {
                    list.add("0");
                    Map<String, Object> mapItem = new HashMap<>();
                    mapItem.put("date",whichDay );
                    mapItem.put("value", "0.0");
                    listItem.add(mapItem);
                }
            }
        } else {
            // dateList
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            Map<String, Object> map = new HashMap<>();
            map.put("date", startDate);
            map.put("disGoodsId", disGoodsId);
//            if (searchDepId != -1) {
//                map.put("depId", searchDepId);
//            }
            if (type.equals("sales")) {
                map.put("produce", 0);
            }
            if (type.equals("loss")) {
                map.put("loss", 0);
            }
            if (type.equals("waste")) {
                map.put("waste", 0);
            }


            Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
            Double weightTotal = 0.0;
            if (integer > 0) {
                if (type.equals("cost")) {
                    Double weightTotalP = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
                    Double weightTotalL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
                    Double weightTotalW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
                    weightTotal = weightTotalP + weightTotalL + weightTotalW;
                }
                if (type.equals("sales")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
                }
                if (type.equals("loss")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
                }
                if (type.equals("waste")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
                }
                list.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            } else {
                list.add("0");
            }
        }


        //dep
        Map<String, Object> mapDep = new HashMap<>();
        if (howManyDaysInPeriod > 0) {
            mapDep.put("startDate", startDate);
            mapDep.put("stopDate", stopDate);
        } else {
            mapDep.put("date", startDate);
        }
        mapDep.put("disGoodsId", disGoodsId);
        if (searchDepId != -1) {
            mapDep.put("depId", searchDepId);
        }
        if (type.equals("sales")) {
            mapDep.put("produce", 0);
        }
        if (type.equals("loss")) {
            mapDep.put("loss", 0);
        }
        if (type.equals("waste")) {
            mapDep.put("waste", 0);
        }
        TreeSet<GbDepartmentEntity> departmentEntities = gbDepGoodsDailyService.queryWhichDepsHasProduceDepGoodsDaily(mapDep);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                Integer gbDepartmentId = department.getGbDepartmentId();
                Map<String, Object> mapdepTotal = new HashMap<>();
                mapdepTotal.put("depId", gbDepartmentId);
                mapdepTotal.put("disGoodsId", disGoodsId);
                if (howManyDaysInPeriod > 0) {
                    mapdepTotal.put("startDate", startDate);
                    mapdepTotal.put("stopDate", stopDate);
                } else {
                    mapdepTotal.put("date", startDate);
                }
                if (type.equals("cost")) {
                    Double totoP = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapdepTotal);
                    Double totoL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapdepTotal);
                    Double totoW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapdepTotal);
                    double toto = totoP + totoL + totoW;
                    department.setDepProfitGoodsTotal(toto);
                    department.setDepProfitGoodsTotalString(new BigDecimal(toto).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("sales")) {
                    Double toto = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapdepTotal);
                    department.setDepProduceGoodsWeightTotal(toto);
                    department.setDepProduceGoodsWeightTotalString(new BigDecimal(toto).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("loss")) {
                    Double toto = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapdepTotal);
                    department.setDepLossGoodsWeightTotal(toto);
                    department.setDepLossGoodsWeightTotalString(new BigDecimal(toto).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("waste")) {
                    Double toto = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapdepTotal);
                    department.setDepWasteGoodsWeightTotal(toto);
                    department.setDepWasteGoodsWeightTotalString(new BigDecimal(toto).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }
        }

        GbDistributerGoodsEntity distributerGoodsEntity = distributerGoodsService.queryObject(disGoodsId);
        Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("date", dateList);
        mapResult.put("list", list);

        if (type.equals("cost")) {
            mapResult.put("dep", abcDepGoodsProfit(departmentEntities));
            mapResult.put("itemList", listItem);

        }
        if (type.equals("sales")) {
            mapResult.put("dep", abcDepGoodsWeight(departmentEntities));
            mapResult.put("itemList", listItem);
        }
        if (type.equals("loss")) {
            mapResult.put("dep", abcDepGoodsLoss(departmentEntities));
            mapResult.put("itemList", listItem);

        }
        if (type.equals("waste")) {
            mapResult.put("dep", abcDepGoodsWaste(departmentEntities));
            mapResult.put("itemList", listItem);

        }
        mapResult.put("oneTotal", new BigDecimal(weightTotalTC).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("salesTotal", new BigDecimal(weightTotalT).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("lossTotal", new BigDecimal(weightTotalTL).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("wasteTotal", new BigDecimal(weightTotalTW).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("disGoods", distributerGoodsEntity);
        mapResult.put("code", "0");
        return mapResult;
    }


    private TreeSet<GbDepartmentEntity> abcDepGoodsWeight(TreeSet<GbDepartmentEntity> goodsEntities) {
        TreeSet<GbDepartmentEntity> ts = new TreeSet<>(new Comparator<GbDepartmentEntity>() {
            @Override
            public int compare(GbDepartmentEntity o1, GbDepartmentEntity o2) {
                int result;
                if (o2.getDepProduceGoodsWeightTotal() - o1.getDepProduceGoodsWeightTotal() < 0) {
                    result = -1;
                } else if (o2.getDepProduceGoodsWeightTotal() - o1.getDepProduceGoodsWeightTotal() > 0) {
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


    private TreeSet<GbDepartmentEntity> abcDepGoodsProfit(TreeSet<GbDepartmentEntity> goodsEntities) {
        TreeSet<GbDepartmentEntity> ts = new TreeSet<>(new Comparator<GbDepartmentEntity>() {
            @Override
            public int compare(GbDepartmentEntity o1, GbDepartmentEntity o2) {
                int result;
                if (o2.getDepProfitGoodsTotal() - o1.getDepProfitGoodsTotal() < 0) {
                    result = -1;
                } else if (o2.getDepProfitGoodsTotal() - o1.getDepProfitGoodsTotal() > 0) {
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


    private TreeSet<GbDepartmentEntity> abcDepGoodsCost(TreeSet<GbDepartmentEntity> goodsEntities) {
        TreeSet<GbDepartmentEntity> ts = new TreeSet<>(new Comparator<GbDepartmentEntity>() {
            @Override
            public int compare(GbDepartmentEntity o1, GbDepartmentEntity o2) {
                int result;
                if (o2.getDepCostGoodsTotal() - o1.getDepCostGoodsTotal() < 0) {
                    result = -1;
                } else if (o2.getDepCostGoodsTotal() - o1.getDepCostGoodsTotal() > 0) {
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

    private TreeSet<GbDepartmentEntity> abcDepGoodsSales(TreeSet<GbDepartmentEntity> goodsEntities) {
        TreeSet<GbDepartmentEntity> ts = new TreeSet<>(new Comparator<GbDepartmentEntity>() {
            @Override
            public int compare(GbDepartmentEntity o1, GbDepartmentEntity o2) {
                int result;
                if (o2.getDepProduceGoodsTotal() - o1.getDepProduceGoodsTotal() < 0) {
                    result = -1;
                } else if (o2.getDepProduceGoodsTotal() - o1.getDepProduceGoodsTotal() > 0) {
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


    private TreeSet<GbDepartmentEntity> abcDepGoodsWaste(TreeSet<GbDepartmentEntity> goodsEntities) {
        TreeSet<GbDepartmentEntity> ts = new TreeSet<>(new Comparator<GbDepartmentEntity>() {
            @Override
            public int compare(GbDepartmentEntity o1, GbDepartmentEntity o2) {
                int result;
                if (o2.getDepWasteGoodsTotal() - o1.getDepWasteGoodsTotal() < 0) {
                    result = -1;
                } else if (o2.getDepWasteGoodsTotal() - o1.getDepWasteGoodsTotal() > 0) {
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


    private TreeSet<GbDepartmentEntity> abcDepGoodsLoss(TreeSet<GbDepartmentEntity> goodsEntities) {
        TreeSet<GbDepartmentEntity> ts = new TreeSet<>(new Comparator<GbDepartmentEntity>() {
            @Override
            public int compare(GbDepartmentEntity o1, GbDepartmentEntity o2) {
                int result;
                if (o2.getDepLossGoodsTotal() - o1.getDepLossGoodsTotal() < 0) {
                    result = -1;
                } else if (o2.getDepLossGoodsTotal() - o1.getDepLossGoodsTotal() > 0) {
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

    @RequestMapping(value = "/getDepCostTypeGoodsDetail", method = RequestMethod.POST)
    @ResponseBody
    public R getDepCostTypeGoodsDetail(String startDate, String stopDate,
                                       String type, Integer disGoodsId, Integer searchDepId) {
        Map<String, Object> echartsWeight = getDepCostByType(startDate, stopDate, type, searchDepId, disGoodsId);

        return R.ok().put("data", echartsWeight);
    }

    @RequestMapping(value = "/getDepCostTypeDayCata", method = RequestMethod.POST)
    @ResponseBody
    public R getDepCostTypeDayCata(String date, String type, Integer searchDepId) {

        Map<String, Object> echartsWeight = getDepCostByTypeGoodsCata(date, type, searchDepId);

        return R.ok().put("data", echartsWeight);
    }

    @RequestMapping(value = "/getDepCostTypeDayDetail", method = RequestMethod.POST)
    @ResponseBody
    public R getDepCostTypeDayDetail(String startDate, String stopDate, String type, Integer searchDepId, Integer fatherGoodsId) {

        Map<String, Object> echartsWeight = getDepCostByTypeGoodsDetail(startDate, stopDate, type, searchDepId, fatherGoodsId);

        return R.ok().put("data", echartsWeight);
    }


    @RequestMapping(value = "/getDepCostType", method = RequestMethod.POST)
    @ResponseBody
    public R getDepCostType(String startDate, String stopDate,
                            String type, String costType, Integer searchDepId, Integer disGoodsId) {
        Map<String, Object> echartsWeight = new HashMap<>();
        if (costType.equals("day")) {
            echartsWeight = getDepCostByType(startDate, stopDate, type, searchDepId, disGoodsId);
        }
        if (costType.equals("goods")) {
            echartsWeight = getDepCostByTypeGoods(startDate, stopDate, type, searchDepId);
        }

        return R.ok().put("data", echartsWeight);
    }

    @RequestMapping(value = "/getGoodsEchartsByGoodsFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R getGoodsEchartsByGoodsFatherId(String startDate, String stopDate, Integer disGoodsFatherId,
                                            String type, String echartsType, String searchDepIds) {
        Map<String, Object> echartsWeight = new HashMap<>();
        if (echartsType.equals("weightEcharts")) {
            echartsWeight = getEchartsWeight(startDate, stopDate, disGoodsFatherId, type, searchDepIds);
        }
        if (echartsType.equals("profitEcharts")) {
            echartsWeight = getEchartsProfit(startDate, stopDate, disGoodsFatherId, type, searchDepIds);
        }
        if (echartsType.equals("costEcharts")) {
            if (disGoodsFatherId != -1) {
                echartsWeight = getEchartsCost(startDate, stopDate, disGoodsFatherId, type, searchDepIds);
            } else {
                echartsWeight = getEchartsCostFather(startDate, stopDate, type, searchDepIds);
            }
        }


        if (echartsWeight.get("code").equals("0")) {
            return R.ok().put("data", echartsWeight);
        } else {
            return R.error(-1, "没有数据");
        }

    }


    private Map<String, Object> getDepCostByTypeGoods(String startDate, String stopDate,
                                                      String type, Integer searchDepId) {

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("depFatherId", searchDepId);
        if (type.equals("sales")) {
            map.put("produce", 0);
        }
        if (type.equals("loss")) {
            map.put("loss", 0);
        }
        if (type.equals("waste")) {
            map.put("waste", 0);
        }
//        TreeSet<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);
        List<GbDistributerFatherGoodsEntity> greatGrandGoods = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map);
        if (greatGrandGoods.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrand : greatGrandGoods) {
                double produceSubtotalG = 0;

                List<GbDistributerFatherGoodsEntity> grandGoods = greatGrand.getFatherGoodsEntities();
                if (grandGoods.size() > 0) {
                    for (GbDistributerFatherGoodsEntity grand : grandGoods) {
                        double produceSubtotalF = 0;
                        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                        if (fatherGoodsEntities.size() > 0) {
                            for (GbDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
                                map.put("disGoodsFatherId", fatherGoodsEntity.getGbDistributerFatherGoodsId());
                                double produceSubtotal = 0;
                                System.out.println("mapdidgogogoaaabbbb" + map);
                                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                                if (integer > 0) {
                                    if (type.equals("sales")) {
                                        produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                                    }
                                    if (type.equals("loss")) {
                                        produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                                    }
                                    if (type.equals("waste")) {
                                        produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                                    }
                                }
                                produceSubtotalF = produceSubtotalF + produceSubtotal;
                                produceSubtotalG = produceSubtotalG + produceSubtotal;
                                fatherGoodsEntity.setFatherProduceTotalString(new BigDecimal(produceSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            }
                        }
                        grand.setFatherProduceTotalString(new BigDecimal(produceSubtotalF).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    greatGrand.setFatherProduceTotalString(new BigDecimal(produceSubtotalG).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }
        }
        //todo
        mapR.put("arr", greatGrandGoods);
//        if (gbDistributerGoodsEntities.size() > 0) {
//            for (GbDistributerGoodsEntity gbDistributerGoodsEntity : gbDistributerGoodsEntities) {
//                map.put("disGoodsId", gbDistributerGoodsEntity.getGbDistributerGoodsId());
//                double produceSubtotal = 0;
//                System.out.println("mapdidgogogoGGGGGG" + map);
//                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
//                if (integer > 0) {
//                    if (type.equals("sales")) {
//                        produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
//                    }
//                    if (type.equals("loss")) {
//                        produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
//                    }
//                    if (type.equals("waste")) {
//                        produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
//                    }
//                }
//                gbDistributerGoodsEntity.setGoodsProduceTotal(produceSubtotal);
//                gbDistributerGoodsEntity.setGoodsProduceTotalString(new BigDecimal(produceSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//            }
//        }

//        mapR.put("arr", abcSalesBig(gbDistributerGoodsEntities));
        return mapR;

    }


    private Map<String, Object> getDepCostByTypeGoodsDetail(String startDate, String stopDate, String type,
                                                            Integer searchDepId, Integer fatherGoodsId) {

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("disGoodsFatherId", fatherGoodsId);
        if (searchDepId != -1) {
            map.put("depFatherId", searchDepId);
        }

        if (type.equals("sales")) {
            map.put("produce", 0);
        }
        if (type.equals("loss")) {
            map.put("loss", 0);
        }
        if (type.equals("waste")) {
            map.put("waste", 0);
        }
        System.out.println("sotpdiidaidi" + map);

        TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);

        if (distributerGoodsEntities.size() > 0) {
            for (GbDistributerGoodsEntity distributerGoodsEntity : distributerGoodsEntities) {
                Map<String, Object> mapG = new HashMap<>();
                mapG.put("disGoodsId", distributerGoodsEntity.getGbDistributerGoodsId());
                mapG.put("startDate", startDate);
                mapG.put("stopDate", stopDate);
                if (type.equals("sales")) {
                    mapG.put("produce", 0);
                }
                if (type.equals("loss")) {
                    mapG.put("loss", 0);
                }
                if (type.equals("waste")) {
                    mapG.put("waste", 0);
                }

                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapG);
                Double produceWeight = 0.0;
                Double produceWeightSubtotal = 0.0;
                Double lossWeight = 0.0;
                Double lossWeightSubtotal = 0.0;
                Double wasteWeight = 0.0;
                Double wasteWeightSubtotal = 0.0;
                Double costTotalWeight = 0.0;
                Double costTotalWeightSubtotal = 0.0;
                if (integer > 0) {
                    produceWeight = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapG);
                    produceWeightSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapG);
                    lossWeight = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapG);
                    lossWeightSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapG);
                    wasteWeight = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapG);
                    wasteWeightSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapG);

                    costTotalWeight = produceWeight + lossWeight + wasteWeight;
                    costTotalWeightSubtotal = produceWeightSubtotal + lossWeightSubtotal + wasteWeightSubtotal;

                }
                distributerGoodsEntity.setGoodsCostWeightTotalString(new BigDecimal(costTotalWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                distributerGoodsEntity.setGoodsProduceWeightTotalString(new BigDecimal(produceWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                distributerGoodsEntity.setGoodsLossWeightTotalString(new BigDecimal(lossWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                distributerGoodsEntity.setGoodsWasteWeightTotalString(new BigDecimal(wasteWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                distributerGoodsEntity.setGoodsCostTotalString(new BigDecimal(costTotalWeightSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                distributerGoodsEntity.setGoodsProduceTotalString(new BigDecimal(produceWeightSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                distributerGoodsEntity.setGoodsLossTotalString(new BigDecimal(lossWeightSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                distributerGoodsEntity.setGoodsWasteTotalString(new BigDecimal(wasteWeightSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            }
        }
        mapR.put("arr", distributerGoodsEntities);
        return mapR;

    }

    private Map<String, Object> getDepCostByTypeGoodsCata(String startDate,
                                                          String type, Integer searchDepId) {

        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> itemList = new ArrayList<>();

        List<String> dateList = new ArrayList<>();
        List<String> totalList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("date", startDate);
        if (searchDepId != -1) {
            map.put("depFatherId", searchDepId);
        }

        if (type.equals("sales")) {
            map.put("produce", 0);
        }
        if (type.equals("loss")) {
            map.put("loss", 0);
        }
        if (type.equals("waste")) {
            map.put("waste", 0);
        }
        System.out.println("mapdidgogogoaaabbbb000000" + map);
        List<GbDistributerFatherGoodsEntity> greatGrandGoods = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map);

        if (greatGrandGoods.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrand : greatGrandGoods) {
                List<GbDistributerFatherGoodsEntity> grandGoods = greatGrand.getFatherGoodsEntities();
                if (grandGoods.size() > 0) {
                    for (GbDistributerFatherGoodsEntity grand : grandGoods) {
                        double produceSubtotalF = 0;
                        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                        if (fatherGoodsEntities.size() > 0) {
                            for (GbDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
                                map.put("disGoodsFatherId", fatherGoodsEntity.getGbDistributerFatherGoodsId());
                                double produceSubtotal = 0;
                                System.out.println("mapdidgogogoaaabbbb" + map);
                                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                                if (integer > 0) {
                                    if (type.equals("sales")) {
                                        produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                                    }
                                    if (type.equals("loss")) {
                                        produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                                    }
                                    if (type.equals("waste")) {
                                        produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                                    }
                                }
                                produceSubtotalF = produceSubtotalF + produceSubtotal;
                                fatherGoodsEntity.setFatherProduceTotalString(new BigDecimal(produceSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            }
                        }
                        grand.setFatherProduceTotalString(new BigDecimal(produceSubtotalF).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                }
            }
        }
        mapR.put("arr", greatGrandGoods);
        return mapR;

    }

    private Map<String, Object> getDepCostByType(String startDate, String stopDate,
                                                 String type, Integer searchDepId, Integer disGoodsId) {

        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> itemList = new ArrayList<>();

        List<String> dateList = new ArrayList<>();
        List<String> totalList = new ArrayList<>();
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        if (howManyDaysInPeriod > 0) {

            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                Map<String, Object> map = new HashMap<>();
                map.put("date", whichDay);
                if (disGoodsId != -1) {
                    map.put("disGoodsId", disGoodsId);
                }
                map.put("depFatherId", searchDepId);
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);
                if (type.equals("sales")) {
                    map.put("produce", 0);
                }
                if (type.equals("loss")) {
                    map.put("loss", 0);
                }
                if (type.equals("waste")) {
                    map.put("waste", 0);
                }
                double produceSubtotal = 0;
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                if (integer > 0) {
                    if (type.equals("sales")) {
                        System.out.println("abccccc" + map);
                        produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                    }
                    if (type.equals("loss")) {
                        produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                    }
                    if (type.equals("waste")) {
                        produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                    }
                }
                totalList.add(new BigDecimal(produceSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("day", whichDay);
                mapItem.put("value", new BigDecimal(produceSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                itemList.add(mapItem);
                mapR.put("date", dateList);
                mapR.put("list", totalList);
                mapR.put("arr", itemList);

            }

        }
        return mapR;

    }

    private Map<String, Object> getEchartsCost(String startDate, String stopDate, Integer disGoodsFatherId,
                                               String type, String searchDepIds) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        Map<String, Object> map1 = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<String> totalList = new ArrayList<>();
        List<String> salesList = new ArrayList<>();
        List<String> lossList = new ArrayList<>();
        List<String> wasteList = new ArrayList<>();
        TreeSet<GbDistributerGoodsEntity> abcGbDistributerGoodsEntities = new TreeSet<>();

        if (howManyDaysInPeriod > 0) {
            Map<String, Object> map0 = new HashMap<>();
            map0.put("startDate", startDate);
            map0.put("stopDate", stopDate);
            map0.put("disGoodsFatherId", disGoodsFatherId);
            map0.put("depType", getGbDepartmentTypeMendian());
//            if (searchDepId != -1) {
//                map0.put("depId", searchDepId);
//            }
            Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
            if (integer1 == 0) {
                return R.error(-1, "没有数据");
            }

            Double aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map0);
            Double aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map0);
            Double aDouble1S = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map0);
            double aDouble1P = aDouble1S + aDouble1L + aDouble1W;
            // top
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                Map<String, Object> map = new HashMap<>();
                map.put("date", whichDay);
                map.put("disGoodsFatherId", disGoodsFatherId);
                map.put("depType", getGbDepartmentTypeMendian());
//                if (searchDepId != -1) {
//                    map.put("depId", searchDepId);
//                }
                if (type.equals("sales")) {
                    map.put("produce", 0);
                }
                if (type.equals("loss")) {
                    map.put("loss", 0);
                }
                if (type.equals("waste")) {
                    map.put("waste", 0);
                }
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                if (integer > 0) {
                    Double weightTotal = 0.0;
                    if (type.equals("total")) {
                        Double weightTotalL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                        Double weightTotalW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                        Double weightTotalS = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                        weightTotal = weightTotalS + weightTotalL + weightTotalW;
                        totalList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("sales")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                        salesList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    }
                    if (type.equals("loss")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                        lossList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("waste")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                        wasteList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                } else {
                    if (type.equals("total")) {
                        totalList.add("0");
                    }
                    if (type.equals("sales")) {
                        salesList.add("0");
                    }
                    if (type.equals("loss")) {
                        lossList.add("0");
                    }
                    if (type.equals("waste")) {
                        wasteList.add("0");
                    }
                }
            }

            Map<String, Object> map = new HashMap<>();
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);

            map.put("disGoodsFatherId", disGoodsFatherId);
            map.put("depType", getGbDepartmentTypeMendian());
            if (type.equals("sales")) {
                map.put("produce", 0);
            }
            if (type.equals("loss")) {
                map.put("loss", 0);
            }
            if (type.equals("waste")) {
                map.put("waste", 0);
            }
//            if (searchDepId != -1) {
//                map.put("depId", searchDepId);
//            }
            TreeSet<GbDistributerGoodsEntity> aaa = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);
            if (aaa.size() > 0) {
                for (GbDistributerGoodsEntity goodsEntity : aaa) {
                    //1 求总wasteTotal
                    if (type.equals("total")) {
                        Map<String, Object> map11 = new HashMap<>();
                        map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                        map11.put("startDate", startDate);
                        map11.put("stopDate", stopDate);
                        map11.put("depType", getGbDepartmentTypeMendian());
//                        if (searchDepId != -1) {
//                            map11.put("depId", searchDepId);
//                        }
                        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
                        if (integer > 0) {
                            Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map11);
                            Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                            Double produceWeight = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map11);
                            Double lossWeight = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map11);
                            Double wasteWeight = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map11);
                            double profitRate = 0;
                            double costTotal = 0;
                            double costWeightTotal = 0;
                            costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                            costWeightTotal = produceWeight + lossWeight + wasteWeight;
                            if (produceSubtotal > 0) {
                                profitRate = aDouble / costTotal * 100;
                            }
                            String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            goodsEntity.setGoodsProfitTotalString(s);
                            goodsEntity.setGoodsProfitTotal(aDouble);
                            goodsEntity.setGoodsCostTotal(costTotal);
                            goodsEntity.setGoodsProduceWeightTotalString(new BigDecimal(produceWeight).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsProduceTotalString(new BigDecimal(produceSubtotal).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsWasteWeightTotalString(new BigDecimal(wasteWeight).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsWasteTotalString(new BigDecimal(wasteSubtotal).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsLossWeightTotalString(new BigDecimal(lossWeight).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsLossTotalString(new BigDecimal(lossSubtotal).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsCostRateString(new BigDecimal(profitRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsCostWeightTotalString(new BigDecimal(costWeightTotal).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
                            double v = 0;
                            if(costWeightTotal != 0){
                                v = costTotal / costWeightTotal;
                            }
                            goodsEntity.setGoodsPriceTotalString(new BigDecimal(v).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

                            Integer integer2 = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map11);
                            Integer howManyDaysInPeriod1 = getHowManyDaysInPeriod(stopDate, startDate);

                            double v1 = 0.0;
                            int wxCountAuto = 0;
                            if(integer2 > 0){
                                double   purTotal = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(map11);
                                 v1 = purTotal / integer2;
                                 wxCountAuto = howManyDaysInPeriod1 / integer2;
                            }
                            goodsEntity.setGoodsPurTotalCount(wxCountAuto);
                            goodsEntity.setGoodsPurTotalWeight(new BigDecimal(v1).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
                        } else {
                            goodsEntity.setGoodsProfitTotalString("0");
                            goodsEntity.setGoodsProfitTotal(0.0);
                            goodsEntity.setGoodsCostTotal(0.0);
                            goodsEntity.setGoodsProduceWeightTotalString("0");
                            goodsEntity.setGoodsProduceTotalString("0");
                            goodsEntity.setGoodsWasteWeightTotalString("0");
                            goodsEntity.setGoodsWasteTotalString("0");
                            goodsEntity.setGoodsLossWeightTotalString("0");
                            goodsEntity.setGoodsLossTotalString("0");
                            goodsEntity.setGoodsCostTotalString("0");
                            goodsEntity.setGoodsCostRateString("0");
                            goodsEntity.setGoodsCostWeightTotalString("0");
                            goodsEntity.setGoodsPriceTotalString("0");
                            goodsEntity.setGoodsPurTotalCount(0);
                            goodsEntity.setGoodsPurTotalWeight("0");

                        }
                        abcGbDistributerGoodsEntities = abcCost(aaa);
                    }
                    if (type.equals("sales")) {
                        Map<String, Object> map11 = new HashMap<>();
                        map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                        map11.put("startDate", startDate);
                        map11.put("stopDate", stopDate);
                        map11.put("produce", 0);
                        map11.put("depType", getGbDepartmentTypeMendian());
//                        if (searchDepId != -1) {
//                            map11.put("depId", searchDepId);
//                        }
                        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);

                        if (integer > 0) {
                            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                            double salesRate = 0;
                            double costTotal = 0;
                            if (produceSubtotal > 0) {
                                Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                                Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                                costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                                salesRate = produceSubtotal / costTotal * 100;
                            }

                            String s = new BigDecimal(produceSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            goodsEntity.setGoodsProduceTotalString(s);
                            goodsEntity.setGoodsProduceTotal(produceSubtotal);
                            goodsEntity.setGoodsCostTotal(costTotal);
                            goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsSalesRateString(new BigDecimal(salesRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                        } else {
                            goodsEntity.setGoodsProduceTotalString("0");
                            goodsEntity.setGoodsProduceTotal(0.0);
                            goodsEntity.setGoodsCostTotal(0.0);
                            goodsEntity.setGoodsCostTotalString("0");
                            goodsEntity.setGoodsSalesRateString("0.0");
                        }
                        abcGbDistributerGoodsEntities = abcSales(aaa);
                    }
                    if (type.equals("loss")) {
                        Map<String, Object> map11 = new HashMap<>();
                        map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                        map11.put("startDate", startDate);
                        map11.put("stopDate", stopDate);
//                        if (searchDepId != -1) {
//                            map11.put("depId", searchDepId);
//                        }
                        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);

                        if (integer > 0) {
                            Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                            Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                            double lossRate = 0;
                            double costTotal = 0;
                            if (produceSubtotal > 0) {
                                costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                                lossRate = aDouble / costTotal * 100;
                            }

                            String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            goodsEntity.setGoodsLossTotalString(s);
                            goodsEntity.setGoodsLossTotal(aDouble);
                            goodsEntity.setGoodsCostTotal(costTotal);
                            goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsLossRateString(new BigDecimal(lossRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        } else {
                            goodsEntity.setGoodsLossTotalString("0");
                            goodsEntity.setGoodsLossTotal(0.0);
                            goodsEntity.setGoodsCostTotal(0.0);
                            goodsEntity.setGoodsCostTotalString("0.0");
                            goodsEntity.setGoodsLossRateString("0.0");
                        }
                        abcGbDistributerGoodsEntities = abcLoss(aaa);
                    }
                    if (type.equals("waste")) {
                        Map<String, Object> map11 = new HashMap<>();
                        map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                        map11.put("startDate", startDate);
                        map11.put("stopDate", stopDate);
                        map11.put("depType", getGbDepartmentTypeMendian());
//                        if (searchDepId != -1) {
//                            map11.put("depId", searchDepId);
//                        }
                        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);

                        if (integer > 0) {
                            Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                            Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                            double wasteRate = 0;
                            double costTotal = 0;
                            if (produceSubtotal > 0) {
                                costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                                wasteRate = aDouble / costTotal * 100;

                            }
                            String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            goodsEntity.setGoodsWasteTotalString(s);
                            goodsEntity.setGoodsWasteTotal(aDouble);
                            goodsEntity.setGoodsCostTotal(costTotal);
                            goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsWasteRateString(new BigDecimal(wasteRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        } else {
                            goodsEntity.setGoodsWasteTotalString("0");
                            goodsEntity.setGoodsWasteTotal(0.0);
                            goodsEntity.setGoodsCostTotal(0.0);
                            goodsEntity.setGoodsCostTotalString("0.0");
                            goodsEntity.setGoodsWasteRateString("0.0");
                        }
                        abcGbDistributerGoodsEntities = abcWaste(aaa);
                    }
                }
            }
            map1.put("oneTotal", new BigDecimal(aDouble1P).setScale(1, BigDecimal.ROUND_HALF_UP));
            map1.put("salesTotal", new BigDecimal(aDouble1S).setScale(1, BigDecimal.ROUND_HALF_UP));
            map1.put("lossTotal", new BigDecimal(aDouble1L).setScale(1, BigDecimal.ROUND_HALF_UP));
            map1.put("wasteTotal", new BigDecimal(aDouble1W).setScale(1, BigDecimal.ROUND_HALF_UP));


        } else {
            //yitian

            Map<String, Object> map0 = new HashMap<>();
            map0.put("date", startDate);
            map0.put("disGoodsFatherId", disGoodsFatherId);
            map0.put("depType", getGbDepartmentTypeMendian());
//            if (searchDepId != -1) {
//                map0.put("depId", searchDepId);
//            }
            if (type.equals("sales")) {
                map0.put("produce", 0);
            }
            if (type.equals("loss")) {
                map0.put("loss", 0);
            }
            if (type.equals("waste")) {
                map0.put("waste", 0);
            }
            Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
            if (integer1 == 0) {
                return R.error(-1, "没有数据");
            }
            Double aDouble1S = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map0);
            Double aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map0);
            Double aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map0);
            double aDouble1P = aDouble1S + aDouble1L + aDouble1W;

            // top
            Map<String, Object> map = new HashMap<>();
            map.put("date", startDate);
            map.put("disGoodsFatherId", disGoodsFatherId);
            map.put("depType", getGbDepartmentTypeMendian());
//            if (searchDepId != -1) {
//                map.put("depId", searchDepId);
//            }
            if (type.equals("sales")) {
                map.put("produce", 0);
            }
            if (type.equals("loss")) {
                map.put("loss", 0);
            }
            if (type.equals("waste")) {
                map.put("waste", 0);
            }
            Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            if (integer > 0) {
                Double weightTotal = 0.0;
                if (type.equals("total")) {
                    Double weightTotalS = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                    Double weightTotalL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                    Double weightTotalW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                    weightTotal = weightTotalS + weightTotalL + weightTotalW;
                    totalList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("sales")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                    salesList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }
                if (type.equals("loss")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                    lossList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("waste")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                    wasteList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            } else {
                if (type.equals("total")) {
                    totalList.add("0");
                }
                if (type.equals("sales")) {
                    salesList.add("0");
                }
                if (type.equals("loss")) {
                    lossList.add("0");
                }
                if (type.equals("waste")) {
                    wasteList.add("0");
                }
            }
//            }

            Map<String, Object> map111 = new HashMap<>();
            map111.put("startDate", startDate);
            map111.put("stopDate", stopDate);
            map111.put("disGoodsFatherId", disGoodsFatherId);
            map111.put("depType", getGbDepartmentTypeMendian());
//            if (searchDepId != -1) {
//                map111.put("depId", searchDepId);
//            }
            if (type.equals("sales")) {
                map111.put("produce", 0);
            }
            if (type.equals("loss")) {
                map111.put("loss", 0);
            }
            if (type.equals("waste")) {
                map111.put("waste", 0);
            }
            TreeSet<GbDistributerGoodsEntity> aaa = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map111);
            if (aaa.size() > 0) {
                for (GbDistributerGoodsEntity goodsEntity : aaa) {
                    //1 求总wasteTotal
                    if (type.equals("total")) {
                        Map<String, Object> map11 = new HashMap<>();
                        map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                        map11.put("date", startDate);
                        map11.put("depType", getGbDepartmentTypeMendian());
                        Integer integerI = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
                        if (integerI > 0) {
                            Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map11);
                            Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                            double profitRate = 0;
                            double costTotal = 0;
                            if (produceSubtotal > 0) {
                                costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                                profitRate = aDouble / costTotal * 100;
                            }

                            String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            goodsEntity.setGoodsProfitTotalString(s);
                            goodsEntity.setGoodsProfitTotal(aDouble);
                            goodsEntity.setGoodsCostTotal(costTotal);
                            goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsCostRateString(new BigDecimal(profitRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        } else {
                            goodsEntity.setGoodsProfitTotalString("0");
                            goodsEntity.setGoodsProfitTotal(0.0);
                            goodsEntity.setGoodsCostTotal(0.0);
                            goodsEntity.setGoodsCostTotalString("0");
                            goodsEntity.setGoodsCostRateString("0.0");

                        }
                        abcGbDistributerGoodsEntities = abcProfit(aaa);
                    }
                    if (type.equals("sales")) {
                        Map<String, Object> map11 = new HashMap<>();
                        map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                        map11.put("date", startDate);
//                        if (searchDepId != -1) {
//                            map11.put("depId", searchDepId);
//                        }
                        map11.put("produce", 0);
                        map11.put("depType", getGbDepartmentTypeMendian());
                        Integer integerI = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);

                        if (integerI > 0) {
                            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                            double salesRate = 0;
                            double costTotal = 0;
                            if (produceSubtotal > 0) {
                                Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                                Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                                costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                                salesRate = produceSubtotal / costTotal * 100;
                            }

                            String s = new BigDecimal(produceSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            goodsEntity.setGoodsProduceTotalString(s);
                            goodsEntity.setGoodsProduceTotal(produceSubtotal);
                            goodsEntity.setGoodsCostTotal(costTotal);
                            goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsSalesRateString(new BigDecimal(salesRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                        } else {
                            goodsEntity.setGoodsProduceTotalString("0");
                            goodsEntity.setGoodsProduceTotal(0.0);
                            goodsEntity.setGoodsCostTotal(0.0);
                            goodsEntity.setGoodsCostTotalString("0");
                            goodsEntity.setGoodsSalesRateString("0.0");
                        }
                        abcGbDistributerGoodsEntities = abcSales(aaa);
                    }
                    if (type.equals("loss")) {
                        Map<String, Object> map11 = new HashMap<>();
                        map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                        map11.put("date", startDate);
                        map11.put("depType", getGbDepartmentTypeMendian());
//                        if (searchDepId != -1) {
//                            map11.put("depId", searchDepId);
//                        }
                        Integer integerI = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);

                        if (integerI > 0) {
                            Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                            Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                            double lossRate = 0;
                            double costTotal = 0;
                            if (produceSubtotal > 0) {
                                costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                                lossRate = aDouble / costTotal * 100;
                            }

                            String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            goodsEntity.setGoodsLossTotalString(s);
                            goodsEntity.setGoodsLossTotal(aDouble);
                            goodsEntity.setGoodsCostTotal(costTotal);
                            goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsLossRateString(new BigDecimal(lossRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        } else {
                            goodsEntity.setGoodsLossTotalString("0");
                            goodsEntity.setGoodsLossTotal(0.0);
                            goodsEntity.setGoodsCostTotal(0.0);
                            goodsEntity.setGoodsCostTotalString("0.0");
                            goodsEntity.setGoodsLossRateString("0.0");
                        }
                        abcGbDistributerGoodsEntities = abcLoss(aaa);
                    }
                    if (type.equals("waste")) {
                        Map<String, Object> map11 = new HashMap<>();
                        map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                        map11.put("date", startDate);
                        map11.put("depType", getGbDepartmentTypeMendian());
//                        if (searchDepId != -1) {
//                            map11.put("depId", searchDepId);
//                        }
                        Integer integerI = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);

                        if (integerI > 0) {
                            Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                            Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                            double wasteRate = 0;
                            double costTotal = 0;
                            if (produceSubtotal > 0) {
                                costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                                wasteRate = aDouble / costTotal * 100;

                            }
                            String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            goodsEntity.setGoodsWasteTotalString(s);
                            goodsEntity.setGoodsWasteTotal(aDouble);
                            goodsEntity.setGoodsCostTotal(costTotal);
                            goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsWasteRateString(new BigDecimal(wasteRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        } else {
                            goodsEntity.setGoodsWasteTotalString("0");
                            goodsEntity.setGoodsWasteTotal(0.0);
                            goodsEntity.setGoodsCostTotal(0.0);
                            goodsEntity.setGoodsCostTotalString("0.0");
                            goodsEntity.setGoodsWasteRateString("0.0");
                        }
                        abcGbDistributerGoodsEntities = abcWaste(aaa);
                    }
                }
            }
            map1.put("oneTotal", new BigDecimal(aDouble1P).setScale(1, BigDecimal.ROUND_HALF_UP));
            map1.put("salesTotal", new BigDecimal(aDouble1S).setScale(1, BigDecimal.ROUND_HALF_UP));
            map1.put("lossTotal", new BigDecimal(aDouble1L).setScale(1, BigDecimal.ROUND_HALF_UP));
            map1.put("wasteTotal", new BigDecimal(aDouble1W).setScale(1, BigDecimal.ROUND_HALF_UP));
        }

        map1.put("date", dateList);
        map1.put("one", totalList);
        map1.put("sales", salesList);
        map1.put("loss", lossList);
        map1.put("waste", wasteList);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", abcGbDistributerGoodsEntities);
        mapR.put("top", map1);
        mapR.put("code", "0");
        return mapR;
    }

    private Map<String, Object> getEchartsCostFather(String startDate, String stopDate,
                                                     String type, String searchDepId) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        Map<String, Object> map1 = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<String> totalList = new ArrayList<>();
        List<String> salesList = new ArrayList<>();
        List<String> lossList = new ArrayList<>();
        List<String> wasteList = new ArrayList<>();
        TreeSet<GbDistributerGoodsEntity> abcGbDistributerGoodsEntities = new TreeSet<>();

        if (howManyDaysInPeriod > 0) {
            Map<String, Object> map0 = new HashMap<>();
            map0.put("startDate", startDate);
            map0.put("stopDate", stopDate);
//            map0.put("disGoodsFatherId", disGoodsFatherId);
            map0.put("depType", getGbDepartmentTypeMendian());
//            if (searchDepId != -1) {
//                map0.put("depId", searchDepId);
//            }
            Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
            if (integer1 == 0) {
                return R.error(-1, "没有数据");
            }

            Double aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map0);
            Double aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map0);
            Double aDouble1S = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map0);
            double aDouble1P = aDouble1S + aDouble1L + aDouble1W;
            // top
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                Map<String, Object> map = new HashMap<>();
                map.put("date", whichDay);
//                map.put("disGoodsFatherId", disGoodsFatherId);
                map.put("depType", getGbDepartmentTypeMendian());
//                if (searchDepId != -1) {
//                    map.put("depId", searchDepId);
//                }
                if (type.equals("sales")) {
                    map.put("produce", 0);
                }
                if (type.equals("loss")) {
                    map.put("loss", 0);
                }
                if (type.equals("waste")) {
                    map.put("waste", 0);
                }
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                if (integer > 0) {
                    Double weightTotal = 0.0;
                    if (type.equals("total")) {
                        Double weightTotalL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                        Double weightTotalW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                        Double weightTotalS = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                        weightTotal = weightTotalS + weightTotalL + weightTotalW;
                        totalList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("sales")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                        salesList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    }
                    if (type.equals("loss")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                        lossList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("waste")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                        wasteList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                } else {
                    if (type.equals("total")) {
                        totalList.add("0");
                    }
                    if (type.equals("sales")) {
                        salesList.add("0");
                    }
                    if (type.equals("loss")) {
                        lossList.add("0");
                    }
                    if (type.equals("waste")) {
                        wasteList.add("0");
                    }
                }
            }

            Map<String, Object> map = new HashMap<>();
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);

//            map.put("disGoodsFatherId", disGoodsFatherId);
            map.put("depType", getGbDepartmentTypeMendian());
            if (type.equals("sales")) {
                map.put("produce", 0);
            }
            if (type.equals("loss")) {
                map.put("loss", 0);
            }
            if (type.equals("waste")) {
                map.put("waste", 0);
            }
//            if (searchDepId != -1) {
//                map.put("depId", searchDepId);
//            }
            List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map);
            if (fatherGoodsEntities.size() > 0) {

            }
            map1.put("oneTotal", new BigDecimal(aDouble1P).setScale(1, BigDecimal.ROUND_HALF_UP));
            map1.put("salesTotal", new BigDecimal(aDouble1S).setScale(1, BigDecimal.ROUND_HALF_UP));
            map1.put("lossTotal", new BigDecimal(aDouble1L).setScale(1, BigDecimal.ROUND_HALF_UP));
            map1.put("wasteTotal", new BigDecimal(aDouble1W).setScale(1, BigDecimal.ROUND_HALF_UP));


        } else {
            //yitian

            Map<String, Object> map0 = new HashMap<>();
            map0.put("date", startDate);
//            map0.put("disGoodsFatherId", disGoodsFatherId);
            map0.put("depType", getGbDepartmentTypeMendian());
//            if (searchDepId != -1) {
//                map0.put("depId", searchDepId);
//            }
            if (type.equals("sales")) {
                map0.put("produce", 0);
            }
            if (type.equals("loss")) {
                map0.put("loss", 0);
            }
            if (type.equals("waste")) {
                map0.put("waste", 0);
            }
            Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
            if (integer1 == 0) {
                return R.error(-1, "没有数据");
            }
            Double aDouble1S = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map0);
            Double aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map0);
            Double aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map0);
            double aDouble1P = aDouble1S + aDouble1L + aDouble1W;

            // top
            Map<String, Object> map = new HashMap<>();
            map.put("date", startDate);
//            map.put("disGoodsFatherId", disGoodsFatherId);
            map.put("depType", getGbDepartmentTypeMendian());
//            if (searchDepId != -1) {
//                map.put("depId", searchDepId);
//            }
            if (type.equals("sales")) {
                map.put("produce", 0);
            }
            if (type.equals("loss")) {
                map.put("loss", 0);
            }
            if (type.equals("waste")) {
                map.put("waste", 0);
            }
            Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            if (integer > 0) {
                Double weightTotal = 0.0;
                if (type.equals("total")) {
                    Double weightTotalS = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                    Double weightTotalL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                    Double weightTotalW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                    weightTotal = weightTotalS + weightTotalL + weightTotalW;
                    totalList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("sales")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                    salesList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }
                if (type.equals("loss")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                    lossList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("waste")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                    wasteList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            } else {
                if (type.equals("total")) {
                    totalList.add("0");
                }
                if (type.equals("sales")) {
                    salesList.add("0");
                }
                if (type.equals("loss")) {
                    lossList.add("0");
                }
                if (type.equals("waste")) {
                    wasteList.add("0");
                }
            }
//            }

            Map<String, Object> map111 = new HashMap<>();
            map111.put("startDate", startDate);
            map111.put("stopDate", stopDate);
//            map111.put("disGoodsFatherId", disGoodsFatherId);
            map111.put("depType", getGbDepartmentTypeMendian());
//            if (searchDepId != -1) {
//                map111.put("depId", searchDepId);
//            }
            if (type.equals("sales")) {
                map111.put("produce", 0);
            }
            if (type.equals("loss")) {
                map111.put("loss", 0);
            }
            if (type.equals("waste")) {
                map111.put("waste", 0);
            }
            TreeSet<GbDistributerGoodsEntity> aaa = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map111);
            if (aaa.size() > 0) {
                for (GbDistributerGoodsEntity goodsEntity : aaa) {
                    //1 求总wasteTotal
                    if (type.equals("total")) {
                        Map<String, Object> map11 = new HashMap<>();
                        map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                        map11.put("date", startDate);
                        map11.put("depType", getGbDepartmentTypeMendian());
                        Integer integerI = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
                        if (integerI > 0) {
                            Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map11);
                            Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                            double profitRate = 0;
                            double costTotal = 0;
                            if (produceSubtotal > 0) {
                                costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                                profitRate = aDouble / costTotal * 100;
                            }

                            String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            goodsEntity.setGoodsProfitTotalString(s);
                            goodsEntity.setGoodsProfitTotal(aDouble);
                            goodsEntity.setGoodsCostTotal(costTotal);
                            goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsCostRateString(new BigDecimal(profitRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        } else {
                            goodsEntity.setGoodsProfitTotalString("0");
                            goodsEntity.setGoodsProfitTotal(0.0);
                            goodsEntity.setGoodsCostTotal(0.0);
                            goodsEntity.setGoodsCostTotalString("0");
                            goodsEntity.setGoodsCostRateString("0.0");

                        }
                        System.out.println("abdbbdbdbdbdbbdbddbdb");
                        abcGbDistributerGoodsEntities = abcCost(aaa);
                    }
                    if (type.equals("sales")) {
                        Map<String, Object> map11 = new HashMap<>();
                        map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                        map11.put("date", startDate);
//                        if (searchDepId != -1) {
//                            map11.put("depId", searchDepId);
//                        }
                        map11.put("produce", 0);
                        map11.put("depType", getGbDepartmentTypeMendian());
                        Integer integerI = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);

                        if (integerI > 0) {
                            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                            double salesRate = 0;
                            double costTotal = 0;
                            if (produceSubtotal > 0) {
                                Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                                Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                                costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                                salesRate = produceSubtotal / costTotal * 100;
                            }

                            String s = new BigDecimal(produceSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            goodsEntity.setGoodsProduceTotalString(s);
                            goodsEntity.setGoodsProduceTotal(produceSubtotal);
                            goodsEntity.setGoodsCostTotal(costTotal);
                            goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsSalesRateString(new BigDecimal(salesRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                        } else {
                            goodsEntity.setGoodsProduceTotalString("0");
                            goodsEntity.setGoodsProduceTotal(0.0);
                            goodsEntity.setGoodsCostTotal(0.0);
                            goodsEntity.setGoodsCostTotalString("0");
                            goodsEntity.setGoodsSalesRateString("0.0");
                        }
                        abcGbDistributerGoodsEntities = abcSales(aaa);
                    }
                    if (type.equals("loss")) {
                        Map<String, Object> map11 = new HashMap<>();
                        map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                        map11.put("date", startDate);
                        map11.put("depType", getGbDepartmentTypeMendian());
//                        if (searchDepId != -1) {
//                            map11.put("depId", searchDepId);
//                        }
                        Integer integerI = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);

                        if (integerI > 0) {
                            Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                            Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                            double lossRate = 0;
                            double costTotal = 0;
                            if (produceSubtotal > 0) {
                                costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                                lossRate = aDouble / costTotal * 100;
                            }

                            String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            goodsEntity.setGoodsLossTotalString(s);
                            goodsEntity.setGoodsLossTotal(aDouble);
                            goodsEntity.setGoodsCostTotal(costTotal);
                            goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsLossRateString(new BigDecimal(lossRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        } else {
                            goodsEntity.setGoodsLossTotalString("0");
                            goodsEntity.setGoodsLossTotal(0.0);
                            goodsEntity.setGoodsCostTotal(0.0);
                            goodsEntity.setGoodsCostTotalString("0.0");
                            goodsEntity.setGoodsLossRateString("0.0");
                        }
                        abcGbDistributerGoodsEntities = abcLoss(aaa);
                    }
                    if (type.equals("waste")) {
                        Map<String, Object> map11 = new HashMap<>();
                        map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                        map11.put("date", startDate);
                        map11.put("depType", getGbDepartmentTypeMendian());
//                        if (searchDepId != -1) {
//                            map11.put("depId", searchDepId);
//                        }
                        Integer integerI = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);

                        if (integerI > 0) {
                            Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                            Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                            double wasteRate = 0;
                            double costTotal = 0;
                            if (produceSubtotal > 0) {
                                costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                                wasteRate = aDouble / costTotal * 100;

                            }
                            String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            goodsEntity.setGoodsWasteTotalString(s);
                            goodsEntity.setGoodsWasteTotal(aDouble);
                            goodsEntity.setGoodsCostTotal(costTotal);
                            goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsEntity.setGoodsWasteRateString(new BigDecimal(wasteRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        } else {
                            goodsEntity.setGoodsWasteTotalString("0");
                            goodsEntity.setGoodsWasteTotal(0.0);
                            goodsEntity.setGoodsCostTotal(0.0);
                            goodsEntity.setGoodsCostTotalString("0.0");
                            goodsEntity.setGoodsWasteRateString("0.0");
                        }
                        abcGbDistributerGoodsEntities = abcWaste(aaa);
                    }
                }
            }
            map1.put("oneTotal", new BigDecimal(aDouble1P).setScale(1, BigDecimal.ROUND_HALF_UP));
            map1.put("salesTotal", new BigDecimal(aDouble1S).setScale(1, BigDecimal.ROUND_HALF_UP));
            map1.put("lossTotal", new BigDecimal(aDouble1L).setScale(1, BigDecimal.ROUND_HALF_UP));
            map1.put("wasteTotal", new BigDecimal(aDouble1W).setScale(1, BigDecimal.ROUND_HALF_UP));
        }

        map1.put("date", dateList);
        map1.put("one", totalList);
        map1.put("sales", salesList);
        map1.put("loss", lossList);
        map1.put("waste", wasteList);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", abcGbDistributerGoodsEntities);
        mapR.put("top", map1);
        mapR.put("code", "0");
        return mapR;
    }

    private Map<String, Object> getEchartsProfit(String startDate, String stopDate, Integer disGoodsFatherId,
                                                 String type, String searchDepId) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        Map<String, Object> map1 = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<String> profitList = new ArrayList<>();
        List<String> salesList = new ArrayList<>();
        List<String> lossList = new ArrayList<>();
        List<String> wasteList = new ArrayList<>();

        Map<String, Object> map0 = new HashMap<>();
        if (howManyDaysInPeriod > 0) {
            map0.put("startDate", startDate);
            map0.put("stopDate", stopDate);
        } else {
            map0.put("date", startDate);
        }


        map0.put("disGoodsFatherId", disGoodsFatherId);
//        if (searchDepId != -1) {
//            map0.put("depId", searchDepId);
//        }
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
        if (integer1 == 0) {
            System.out.println("zzhiziziiaaaa");
            return R.error(-1, "没有数据");
        }
        Double aDouble1P = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map0);
        Double aDouble1S = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map0);
        Double aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map0);
        Double aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map0);


        if (howManyDaysInPeriod > 0) {
            // top
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                Map<String, Object> map = new HashMap<>();
                map.put("date", whichDay);
                map.put("disGoodsFatherId", disGoodsFatherId);
//                if (searchDepId != -1) {
//                    map.put("depId", searchDepId);
//                }
                if (type.equals("sales")) {
                    map.put("produce", 0);
                }
                if (type.equals("loss")) {
                    map.put("loss", 0);
                }
                if (type.equals("waste")) {
                    map.put("waste", 0);
                }
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                if (integer > 0) {
                    Double weightTotal = 0.0;
                    if (type.equals("profit")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map);
                        profitList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    }
                    if (type.equals("sales")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map);
                        salesList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    }
                    if (type.equals("loss")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                        lossList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("waste")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                        wasteList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                } else {
                    if (type.equals("profit")) {
                        profitList.add("0");
                    }
                    if (type.equals("sales")) {
                        salesList.add("0");
                    }
                    if (type.equals("loss")) {
                        lossList.add("0");
                    }
                    if (type.equals("waste")) {
                        wasteList.add("0");
                    }
                }
            }
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("date", startDate);
            map.put("disGoodsFatherId", disGoodsFatherId);
//            if (searchDepId != -1) {
//                map.put("depId", searchDepId);
//            }
            if (type.equals("sales")) {
                map.put("produce", 0);
            }
            if (type.equals("loss")) {
                map.put("loss", 0);
            }
            if (type.equals("waste")) {
                map.put("waste", 0);
            }
            Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            if (integer > 0) {
                Double weightTotal = 0.0;
                if (type.equals("profit")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map);
                    profitList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }
                if (type.equals("sales")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map);
                    salesList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }
                if (type.equals("loss")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                    lossList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("waste")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                    wasteList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            } else {
                if (type.equals("profit")) {
                    profitList.add("0");
                }
                if (type.equals("sales")) {
                    salesList.add("0");
                }
                if (type.equals("loss")) {
                    lossList.add("0");
                }
                if (type.equals("waste")) {
                    wasteList.add("0");
                }
            }

        }


        TreeSet<GbDistributerGoodsEntity> abcGbDistributerGoodsEntities = new TreeSet<>();
        Map<String, Object> map = new HashMap<>();
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }

        map.put("disGoodsFatherId", disGoodsFatherId);
        TreeSet<GbDistributerGoodsEntity> aaa = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);
        if (aaa.size() > 0) {
            for (GbDistributerGoodsEntity goodsEntity : aaa) {
                Map<String, Object> map11 = new HashMap<>();
                map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//                if (searchDepId != -1) {
//                    map11.put("depId", searchDepId);
//                }
                if (howManyDaysInPeriod > 0) {
                    map11.put("startDate", startDate);
                    map11.put("stopDate", stopDate);
                } else {
                    map11.put("date", startDate);
                }
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
                //1 求总wasteTotal
                if (type.equals("profit")) {

                    if (integer > 0) {
                        Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map11);
                        Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                        Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                        Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                        double profitRate = 0;
                        double costTotal = 0;
                        if (produceSubtotal > 0) {
                            costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                            profitRate = aDouble / costTotal * 100;
                        }

                        String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                        goodsEntity.setGoodsProfitTotalString(s);
                        goodsEntity.setGoodsProfitTotal(aDouble);
                        goodsEntity.setGoodsCostTotal(costTotal);
                        goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        goodsEntity.setGoodsCostRateString(new BigDecimal(profitRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    } else {
                        goodsEntity.setGoodsProfitTotalString("0");
                        goodsEntity.setGoodsProfitTotal(0.0);
                        goodsEntity.setGoodsCostTotal(0.0);
                        goodsEntity.setGoodsCostTotalString("0");
                        goodsEntity.setGoodsCostRateString("0.0");

                    }
                    abcGbDistributerGoodsEntities = abcProfit(aaa);
                }
                if (type.equals("sales")) {
                    if (integer > 0) {
                        Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map11);
                        Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                        double salesRate = 0;
                        double costTotal = 0;
                        if (produceSubtotal > 0) {
                            Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                            costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                            salesRate = aDouble / costTotal * 100;
                        }

                        String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                        goodsEntity.setGoodsProduceTotalString(s);
                        goodsEntity.setGoodsProduceTotal(aDouble);
                        goodsEntity.setGoodsCostTotal(costTotal);
                        goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        goodsEntity.setGoodsSalesRateString(new BigDecimal(salesRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                    } else {
                        goodsEntity.setGoodsProduceTotalString("0");
                        goodsEntity.setGoodsProduceTotal(0.0);
                        goodsEntity.setGoodsCostTotal(0.0);
                        goodsEntity.setGoodsCostTotalString("0");
                        goodsEntity.setGoodsSalesRateString("0.0");
                    }
                    abcGbDistributerGoodsEntities = abcSales(aaa);
                }
                if (type.equals("loss")) {
                    if (integer > 0) {
                        Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                        Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                        Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                        Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                        double lossRate = 0;
                        double costTotal = 0;
                        if (produceSubtotal > 0) {
                            costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                            lossRate = aDouble / costTotal * 100;
                        }

                        String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                        goodsEntity.setGoodsLossTotalString(s);
                        goodsEntity.setGoodsLossTotal(aDouble);
                        goodsEntity.setGoodsCostTotal(costTotal);
                        goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        goodsEntity.setGoodsLossRateString(new BigDecimal(lossRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    } else {
                        goodsEntity.setGoodsLossTotalString("0");
                        goodsEntity.setGoodsLossTotal(0.0);
                        goodsEntity.setGoodsCostTotal(0.0);
                        goodsEntity.setGoodsCostTotalString("0.0");
                        goodsEntity.setGoodsLossRateString("0.0");
                    }
                    abcGbDistributerGoodsEntities = abcLoss(aaa);
                }
                if (type.equals("waste")) {
                    if (integer > 0) {
                        Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                        Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                        Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                        Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                        double wasteRate = 0;
                        double costTotal = 0;
                        if (produceSubtotal > 0) {
                            costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
                            wasteRate = aDouble / costTotal * 100;
                        }
                        String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                        goodsEntity.setGoodsWasteTotalString(s);
                        goodsEntity.setGoodsWasteTotal(aDouble);
                        goodsEntity.setGoodsCostTotal(costTotal);
                        goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        goodsEntity.setGoodsWasteRateString(new BigDecimal(wasteRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    } else {
                        goodsEntity.setGoodsWasteTotalString("0");
                        goodsEntity.setGoodsWasteTotal(0.0);
                        goodsEntity.setGoodsCostTotal(0.0);
                        goodsEntity.setGoodsCostTotalString("0.0");
                        goodsEntity.setGoodsWasteRateString("0.0");
                    }
                    abcGbDistributerGoodsEntities = abcWaste(aaa);
                }
            }
        }


        map1.put("date", dateList);
        map1.put("one", profitList);
        map1.put("sales", salesList);
        map1.put("loss", lossList);
        map1.put("waste", wasteList);
        map1.put("oneTotal", new BigDecimal(aDouble1P).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("salesTotal", new BigDecimal(aDouble1S).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("lossTotal", new BigDecimal(aDouble1L).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("wasteTotal", new BigDecimal(aDouble1W).setScale(1, BigDecimal.ROUND_HALF_UP));
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", abcGbDistributerGoodsEntities);
        mapR.put("top", map1);
        mapR.put("code", "0");
        return mapR;
    }

    private Map<String, Object> getEchartsWeight(String startDate, String stopDate, Integer disGoodsFatherId,
                                                 String type, String searchDepId) {
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }


        Map<String, Object> map1 = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<String> costList = new ArrayList<>();
        List<String> salesList = new ArrayList<>();
        List<String> lossList = new ArrayList<>();
        List<String> wasteList = new ArrayList<>();


        Map<String, Object> map0 = new HashMap<>();
        if (howManyDaysInPeriod > 0) {
            map0.put("startDate", startDate);
            map0.put("stopDate", stopDate);
        } else {
            map0.put("date", startDate);
        }

        map0.put("disGoodsFatherId", disGoodsFatherId);
//        if (searchDepId != -1) {
//            map0.put("depId", searchDepId);
//        }
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
        System.out.println("chaonimaimamamamama" + integer1);
        if (integer1 == 0) {
            return R.error(-1, "没有数据");
        }

        Double aDouble1S = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map0);
        Double aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map0);
        Double aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map0);
        double aDouble1P = aDouble1S + aDouble1L + aDouble1W;


        if (howManyDaysInPeriod > 0) {
            // top
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                Map<String, Object> map = new HashMap<>();
                map.put("date", whichDay);
                map.put("disGoodsFatherId", disGoodsFatherId);
//                if (searchDepId != -1) {
//                    map.put("depId", searchDepId);
//                }


                if (type.equals("sales")) {
                    map.put("produce", 0);
                }
                if (type.equals("loss")) {
                    map.put("loss", 0);
                }
                if (type.equals("waste")) {
                    map.put("waste", 0);
                }
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                if (integer > 0) {
                    Double weightTotal = 0.0;

                    if (type.equals("cost")) {
                        Double weightTotalS = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
                        Double weightTotalL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
                        Double weightTotalW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
                        weightTotal = weightTotalS + weightTotalL + weightTotalW;
                        costList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    }

                    if (type.equals("sales")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
                        salesList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    }
                    if (type.equals("loss")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
                        lossList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("waste")) {
                        weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
                        wasteList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                } else {
                    if (type.equals("cost")) {
                        costList.add("0");
                    }

                    if (type.equals("sales")) {
                        salesList.add("0");
                    }
                    if (type.equals("loss")) {
                        lossList.add("0");
                    }
                    if (type.equals("waste")) {
                        wasteList.add("0");
                    }
                }
            }
        } else {

            Map<String, Object> map = new HashMap<>();
            map.put("date", startDate);
            map.put("disGoodsFatherId", disGoodsFatherId);
//            if (searchDepId != -1) {
//                map.put("depId", searchDepId);
//            }

            if (type.equals("sales")) {
                map.put("produce", 0);
            }
            if (type.equals("loss")) {
                map.put("loss", 0);
            }
            if (type.equals("waste")) {
                map.put("waste", 0);
            }
            Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            if (integer > 0) {
                Double weightTotal = 0.0;

                if (type.equals("cost")) {
                    Double weightTotalS = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
                    Double weightTotalL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
                    Double weightTotalW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
                    weightTotal = weightTotalS + weightTotalL + weightTotalW;
                    costList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }

                if (type.equals("sales")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
                    salesList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }
                if (type.equals("loss")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
                    lossList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("waste")) {
                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
                    wasteList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            } else {
                if (type.equals("cost")) {
                    costList.add("0");
                }

                if (type.equals("sales")) {
                    salesList.add("0");
                }
                if (type.equals("loss")) {
                    lossList.add("0");
                }
                if (type.equals("waste")) {
                    wasteList.add("0");
                }
            }

        }


        Map<String, Object> map = new HashMap<>();
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }

        map.put("disGoodsFatherId", disGoodsFatherId);
//        if (searchDepId != -1) {
//            map.put("depId", searchDepId);
//        }
//        if (searchDepId != -1) {
//            map.put("depId", searchDepId);
//        }
        if (type.equals("sales")) {
            map.put("produce", 0);
        }
        if (type.equals("loss")) {
            map.put("loss", 0);
        }
        if (type.equals("waste")) {
            map.put("waste", 0);
        }
        TreeSet<GbDistributerGoodsEntity> aaa = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);
        TreeSet<GbDistributerGoodsEntity> gbDistributerGoodsEntities = new TreeSet<>();
        for (GbDistributerGoodsEntity goodsEntity : aaa) {
            //1 求总wasteTotal

            Map<String, Object> map11 = new HashMap<>();
            map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//            if (searchDepId != -1) {
//                map11.put("depId", searchDepId);
//            }
            if (howManyDaysInPeriod > 0) {
                map11.put("startDate", startDate);
                map11.put("stopDate", stopDate);
            } else {
                map11.put("date", startDate);
            }
            Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);

            if (type.equals("cost")) {
                if (integer > 0) {
                    Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map11);
                    Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map11);
                    Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map11);
                    double aDouble = aDoubleP + aDoubleL + aDoubleW;
                    String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    goodsEntity.setGoodsCostWeightTotalString(s);
                    goodsEntity.setGoodsCostWeightTotal(aDouble);
                } else {
                    goodsEntity.setGoodsCostWeightTotalString("0");
                    goodsEntity.setGoodsCostWeightTotal(0.0);
                }
                gbDistributerGoodsEntities = abcCostWeight(aaa);
            }

            if (type.equals("sales")) {
                if (integer > 0) {
                    Double aDoubleS = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map11);
                    Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map11);
                    Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map11);
                    double costWeight = aDoubleS + aDoubleL + aDoubleW;
                    double salesRate = 0;
                    if (aDoubleS > 0) {
                        salesRate = (aDoubleS / costWeight) * 100;
                    }
                    String s = new BigDecimal(aDoubleS).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    goodsEntity.setGoodsProduceWeightTotalString(s);
                    goodsEntity.setGoodsProduceWeightTotal(aDoubleS);
                    goodsEntity.setGoodsCostWeightTotal(costWeight);
                    goodsEntity.setGoodsCostWeightTotalString(new BigDecimal(costWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsSalesRateString(new BigDecimal(salesRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsSalesRate(salesRate);

                } else {
                    goodsEntity.setGoodsProduceWeightTotalString("0");
                    goodsEntity.setGoodsProduceWeightTotal(0.0);
                    goodsEntity.setGoodsCostWeightTotal(0.0);
                    goodsEntity.setGoodsCostWeightTotalString("0");
                    goodsEntity.setGoodsSalesRateString("0");
                    goodsEntity.setGoodsSalesRate(0.0);
                }
                gbDistributerGoodsEntities = abcSalesWeight(aaa);
            }
            if (type.equals("loss")) {
                if (integer > 0) {
                    Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map11);
                    Double aDoubleS = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map11);
                    Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map11);
                    double costWeight = aDoubleS + aDoubleL + aDoubleW;
                    double lossRate = 0;
                    if (aDoubleS > 0) {
                        lossRate = (aDoubleL / costWeight) * 100;
                    }
                    String s = new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    goodsEntity.setGoodsLossWeightTotalString(s);
                    goodsEntity.setGoodsLossWeightTotal(aDoubleL);
                    goodsEntity.setGoodsCostWeightTotal(costWeight);
                    goodsEntity.setGoodsCostWeightTotalString(new BigDecimal(costWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsLossRateString(new BigDecimal(lossRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsLossRate(lossRate);

                } else {
                    goodsEntity.setGoodsLossWeightTotalString("0");
                    goodsEntity.setGoodsLossWeightTotal(0.0);
                    goodsEntity.setGoodsCostWeightTotal(0.0);
                    goodsEntity.setGoodsCostWeightTotalString("0");
                    goodsEntity.setGoodsLossRateString("0");
                    goodsEntity.setGoodsLossRate(0.0);

                }
                gbDistributerGoodsEntities = abcLossWeight(aaa);
            }

            if (type.equals("waste")) {
                if (integer > 0) {
                    Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map11);
                    Double aDoubleS = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map11);
                    Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map11);
                    double costWeight = aDoubleS + aDoubleL + aDoubleW;
                    double wasteRate = 0;
                    if (aDoubleW > 0) {
                        wasteRate = (aDoubleW / costWeight) * 100;
                    }
                    String s = new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    goodsEntity.setGoodsWasteWeightTotalString(s);
                    goodsEntity.setGoodsWasteWeightTotal(aDoubleW);
                    goodsEntity.setGoodsCostWeightTotal(costWeight);
                    goodsEntity.setGoodsCostWeightTotalString(new BigDecimal(costWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsWasteRateString(new BigDecimal(wasteRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsWasteRate(wasteRate);
                } else {
                    goodsEntity.setGoodsWasteWeightTotalString("0");
                    goodsEntity.setGoodsWasteWeightTotal(0.0);
                    goodsEntity.setGoodsCostWeightTotal(0.0);
                    goodsEntity.setGoodsCostWeightTotalString("0");
                    goodsEntity.setGoodsWasteRateString("0");
                    goodsEntity.setGoodsWasteRate(0.0);
                }
                gbDistributerGoodsEntities = abcWasteWeight(aaa);
            }
        }


        map1.put("date", dateList);
        map1.put("one", costList);
        map1.put("sales", salesList);
        map1.put("loss", lossList);
        map1.put("waste", wasteList);
        map1.put("oneTotal", new BigDecimal(aDouble1P).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("salesTotal", new BigDecimal(aDouble1S).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("lossTotal", new BigDecimal(aDouble1L).setScale(1, BigDecimal.ROUND_HALF_UP));
        map1.put("wasteTotal", new BigDecimal(aDouble1W).setScale(1, BigDecimal.ROUND_HALF_UP));
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", gbDistributerGoodsEntities);
        mapR.put("top", map1);
        mapR.put("code", "0");
        return mapR;
    }


    private TreeSet<GbDistributerGoodsEntity> abcProfit(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;

                if (o2.getGoodsProfitTotal() - o1.getGoodsProfitTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsProfitTotal() - o1.getGoodsProfitTotal() > 0) {
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


    private TreeSet<GbDistributerGoodsEntity> abcCost(TreeSet<GbDistributerGoodsEntity> goodsEntities) {
        System.out.println("costototosoososososoo");
        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;

                if (o2.getGoodsCostTotal() - o1.getGoodsCostTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsCostTotal() - o1.getGoodsCostTotal() > 0) {
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

    private TreeSet<GbDistributerGoodsEntity> abcSales(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;

                if (o2.getGoodsProduceTotal() - o1.getGoodsProduceTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsProduceTotal() - o1.getGoodsProduceTotal() > 0) {
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


    private TreeSet<GbDistributerGoodsEntity> abcSalesBig(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;

                if (o2.getGoodsProduceTotal() - o1.getGoodsProduceTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsProduceTotal() - o1.getGoodsProduceTotal() > 0) {
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

    private TreeSet<GbDistributerGoodsEntity> abcSalesWeight(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;

                if (o2.getGoodsProduceWeightTotal() - o1.getGoodsProduceWeightTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsProduceWeightTotal() - o1.getGoodsProduceWeightTotal() > 0) {
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

    private TreeSet<GbDistributerGoodsEntity> abcCostWeight(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;
                if (o2.getGoodsCostWeightTotal() - o1.getGoodsCostWeightTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsCostWeightTotal() - o1.getGoodsCostWeightTotal() > 0) {
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

    private TreeSet<GbDistributerGoodsEntity> abcLossWeight(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;
                if (o2.getGoodsLossWeightTotal() - o1.getGoodsLossWeightTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsLossWeightTotal() - o1.getGoodsLossWeightTotal() > 0) {
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

    private TreeSet<GbDistributerGoodsEntity> abcLoss(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;
                if (o2.getGoodsLossTotal() - o1.getGoodsLossTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsLossTotal() - o1.getGoodsLossTotal() > 0) {
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

    private TreeSet<GbDistributerGoodsEntity> abcWasteWeight(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;
                if (o2.getGoodsWasteWeightTotal() - o1.getGoodsWasteWeightTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsWasteWeightTotal() - o1.getGoodsWasteWeightTotal() > 0) {
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

    private TreeSet<GbDistributerGoodsEntity> abcWaste(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;
                if (o2.getGoodsWasteTotal() - o1.getGoodsWasteTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsWasteTotal() - o1.getGoodsWasteTotal() > 0) {
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


    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherProfit
            (TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;
                if (o2.getFatherProfitTotal() - o1.getFatherProfitTotal() < 0) {
                    result = -1;
                } else if (o2.getFatherProfitTotal() - o1.getFatherProfitTotal() > 0) {
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


    private TreeSet<GbDepartmentEntity> abcDepCostSubtotal(TreeSet<GbDepartmentEntity> departmentEntities) {

        TreeSet<GbDepartmentEntity> ts = new TreeSet<>(new Comparator<GbDepartmentEntity>() {
            @Override
            public int compare(GbDepartmentEntity o1, GbDepartmentEntity o2) {
                int result;
                if (o2.getDepCostGoodsTotal() - o1.getDepCostGoodsTotal() < 0) {
                    result = -1;
                } else if (o2.getDepCostGoodsTotal() - o1.getDepCostGoodsTotal() > 0) {
                    result = 1;
                } else {
                    result = 1;
                }

                return result;
            }
        });

        ts.addAll(departmentEntities);

        return ts;
    }

    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherCostSubtotal
            (TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;
                if (o2.getFatherCostSubtotal() - o1.getFatherCostSubtotal() < 0) {
                    result = -1;
                } else if (o2.getFatherCostSubtotal() - o1.getFatherCostSubtotal() > 0) {
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

    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherCost
            (TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;
                if (o2.getFatherCostWeight() - o1.getFatherCostWeight() < 0) {
                    result = -1;
                } else if (o2.getFatherCostWeight() - o1.getFatherCostWeight() > 0) {
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

    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherSales
            (TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;
                if (o2.getFatherSellingSubtotal() - o1.getFatherSellingSubtotal() < 0) {
                    result = -1;
                } else if (o2.getFatherSellingSubtotal() - o1.getFatherSellingSubtotal() > 0) {
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

    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherWaste
            (TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;
                if (o2.getFatherWasteTotal() - o1.getFatherWasteTotal() < 0) {
                    result = -1;
                } else if (o2.getFatherWasteTotal() - o1.getFatherWasteTotal() > 0) {
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

    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherLoss
            (TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;
                if (o2.getFatherLossTotal() - o1.getFatherLossTotal() < 0) {
                    result = -1;
                } else if (o2.getFatherLossTotal() - o1.getFatherLossTotal() > 0) {
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


//    @RequestMapping(value = "/disGetReduceGoodsWeight", method = RequestMethod.POST)
//    @ResponseBody
//    public R disGetReduceGoodsWeight(String startDate, String stopDate, String disGoodsFatherId, String type) {
//
//
//        Integer howManyDaysInPeriod = 1;
//        if (!startDate.equals(stopDate)) {
//            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
//        }
//        Map<String, Object> map1 = new HashMap<>();
//        List<String> dateList = new ArrayList<>();
//        List<String> costList = new ArrayList<>();
//        List<String> salesList = new ArrayList<>();
//        List<String> lossList = new ArrayList<>();
//        List<String> wasteList = new ArrayList<>();
//
//        Map<String, Object> map0 = new HashMap<>();
//        map0.put("startDate", startDate);
//        map0.put("stopDate", stopDate);
//        map0.put("disGoodsFatherId", disGoodsFatherId);
//        Double aDouble1S = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map0);
//        Double aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map0);
//        Double aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map0);
//        double aDouble1P = aDouble1S + aDouble1L + aDouble1W;
//
//
//        // top
//        for (int i = howManyDaysInPeriod + 1; i > 0; i--) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("date", formatWhatDay(-i + 1));
//            map.put("disGoodsFatherId", disGoodsFatherId);
//
//            if (type.equals("sales")) {
//                map.put("produce", 0);
//            }
//            if (type.equals("loss")) {
//                map.put("loss", 0);
//            }
//            if (type.equals("waste")) {
//                map.put("waste", 0);
//            }
//            Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
//            String s = formatWhatDay(-i + 1);
//            String substring = s.substring(8, 10);
//            dateList.add(substring);
//
//            if (integer > 0) {
//                Double weightTotal = 0.0;
//
//                if (type.equals("cost")) {
//                    Double weightTotalS = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
//                    Double weightTotalL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
//                    Double weightTotalW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
//                    weightTotal = weightTotalS + weightTotalL + weightTotalW;
//                    costList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                }
//
//                if (type.equals("sales")) {
//                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
//                    salesList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                }
//                if (type.equals("loss")) {
//                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
//                    lossList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                }
//                if (type.equals("waste")) {
//                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
//                    wasteList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                }
//            } else {
//                if (type.equals("cost")) {
//                    costList.add("0");
//                }
//
//                if (type.equals("sales")) {
//                    salesList.add("0");
//                }
//                if (type.equals("loss")) {
//                    lossList.add("0");
//                }
//                if (type.equals("waste")) {
//                    wasteList.add("0");
//                }
//            }
//        }
//
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("startDate", startDate);
//        map.put("stopDate", stopDate);
//        map.put("disGoodsFatherId", disGoodsFatherId);
//
//        if (type.equals("sales")) {
//            map.put("produce", 0);
//        }
//        if (type.equals("loss")) {
//            map.put("loss", 0);
//        }
//        if (type.equals("waste")) {
//            map.put("waste", 0);
//        }
//        TreeSet<GbDistributerGoodsEntity> aaa = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);
//        TreeSet<GbDistributerGoodsEntity> gbDistributerGoodsEntities = new TreeSet<>();
//        for (GbDistributerGoodsEntity goodsEntity : aaa) {
//            //1 求总wasteTotal
//
//            if (type.equals("cost")) {
//                Map<String, Object> map11 = new HashMap<>();
//                map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//                map11.put("startDate", startDate);
//                map11.put("stopDate", stopDate);
//                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
//
//                if (integer > 0) {
//                    Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map11);
//                    Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map11);
//                    Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map11);
//                    double aDouble = aDoubleP + aDoubleL + aDoubleW;
//                    String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//                    goodsEntity.setGoodsCostWeightTotalString(s);
//                    goodsEntity.setGoodsCostWeightTotal(aDouble);
//                } else {
//                    goodsEntity.setGoodsCostWeightTotalString("0");
//                    goodsEntity.setGoodsCostWeightTotal(0.0);
//                }
//                gbDistributerGoodsEntities = abcCostWeight(aaa);
//            }
//
//            if (type.equals("sales")) {
//                Map<String, Object> map11 = new HashMap<>();
//                map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//                map11.put("startDate", startDate);
//                map11.put("stopDate", stopDate);
//                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
//
//                if (integer > 0) {
//                    Double aDoubleS = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map11);
//                    Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map11);
//                    Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map11);
//                    double costWeight = aDoubleS + aDoubleL + aDoubleW;
//                    double salesRate = 0;
//                    if (aDoubleS > 0) {
//                        salesRate = (aDoubleS / costWeight) * 100;
//                    }
//                    String s = new BigDecimal(aDoubleS).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//                    goodsEntity.setGoodsProduceWeightTotalString(s);
//                    goodsEntity.setGoodsProduceWeightTotal(aDoubleS);
//                    goodsEntity.setGoodsCostWeightTotal(costWeight);
//                    goodsEntity.setGoodsCostWeightTotalString(new BigDecimal(costWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    goodsEntity.setGoodsSalesRateString(new BigDecimal(salesRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
//                    goodsEntity.setGoodsSalesRate(salesRate);
//
//                } else {
//                    goodsEntity.setGoodsProduceWeightTotalString("0");
//                    goodsEntity.setGoodsProduceWeightTotal(0.0);
//                    goodsEntity.setGoodsCostWeightTotal(0.0);
//                    goodsEntity.setGoodsCostWeightTotalString("0");
//                    goodsEntity.setGoodsSalesRateString("0");
//                    goodsEntity.setGoodsSalesRate(0.0);
//                }
//                gbDistributerGoodsEntities = abcSalesWeight(aaa);
//            }
//            if (type.equals("loss")) {
//                Map<String, Object> map11 = new HashMap<>();
//                map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//                map11.put("startDate", startDate);
//                map11.put("stopDate", stopDate);
//                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
//                if (integer > 0) {
//                    Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map11);
//                    Double aDoubleS = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map11);
//                    Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map11);
//                    double costWeight = aDoubleS + aDoubleL + aDoubleW;
//                    double lossRate = 0;
//                    if (aDoubleS > 0) {
//                        lossRate = (aDoubleL / costWeight) * 100;
//                    }
//                    String s = new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//                    goodsEntity.setGoodsLossWeightTotalString(s);
//                    goodsEntity.setGoodsLossWeightTotal(aDoubleL);
//                    goodsEntity.setGoodsCostWeightTotal(costWeight);
//                    goodsEntity.setGoodsCostWeightTotalString(new BigDecimal(costWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    goodsEntity.setGoodsLossRateString(new BigDecimal(lossRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
//                    goodsEntity.setGoodsLossRate(lossRate);
//
//                } else {
//                    goodsEntity.setGoodsLossWeightTotalString("0");
//                    goodsEntity.setGoodsLossWeightTotal(0.0);
//                    goodsEntity.setGoodsCostWeightTotal(0.0);
//                    goodsEntity.setGoodsCostWeightTotalString("0");
//                    goodsEntity.setGoodsLossRateString("0");
//                    goodsEntity.setGoodsLossRate(0.0);
//
//                }
//                gbDistributerGoodsEntities = abcLossWeight(aaa);
//            }
//            if (type.equals("waste")) {
//                Map<String, Object> map11 = new HashMap<>();
//                map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//                map11.put("startDate", startDate);
//                map11.put("stopDate", stopDate);
//                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
//                if (integer > 0) {
//                    Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map11);
//                    Double aDoubleS = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map11);
//                    Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map11);
//                    double costWeight = aDoubleS + aDoubleL + aDoubleW;
//                    double wasteRate = 0;
//                    if (aDoubleW > 0) {
//                        wasteRate = (aDoubleW / costWeight) * 100;
//                    }
//                    String s = new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//                    goodsEntity.setGoodsWasteWeightTotalString(s);
//                    goodsEntity.setGoodsWasteWeightTotal(aDoubleW);
//                    goodsEntity.setGoodsCostWeightTotal(costWeight);
//                    goodsEntity.setGoodsCostWeightTotalString(new BigDecimal(costWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    goodsEntity.setGoodsWasteRateString(new BigDecimal(wasteRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
//                    goodsEntity.setGoodsWasteRate(wasteRate);
//                } else {
//                    goodsEntity.setGoodsWasteWeightTotalString("0");
//                    goodsEntity.setGoodsWasteWeightTotal(0.0);
//                    goodsEntity.setGoodsCostWeightTotal(0.0);
//                    goodsEntity.setGoodsCostWeightTotalString("0");
//                    goodsEntity.setGoodsWasteRateString("0");
//                    goodsEntity.setGoodsWasteRate(0.0);
//                }
//                gbDistributerGoodsEntities = abcWasteWeight(aaa);
//            }
//
//
//        }
//
//
//        map1.put("date", dateList);
//        map1.put("cost", costList);
//        map1.put("sales", salesList);
//        map1.put("loss", lossList);
//        map1.put("waste", wasteList);
//        map1.put("costTotal", new BigDecimal(aDouble1P).setScale(1, BigDecimal.ROUND_HALF_UP));
//        map1.put("salesTotal", new BigDecimal(aDouble1S).setScale(1, BigDecimal.ROUND_HALF_UP));
//        map1.put("lossTotal", new BigDecimal(aDouble1L).setScale(1, BigDecimal.ROUND_HALF_UP));
//        map1.put("wasteTotal", new BigDecimal(aDouble1W).setScale(1, BigDecimal.ROUND_HALF_UP));
//        Map<String, Object> mapR = new HashMap<>();
//        mapR.put("arr", gbDistributerGoodsEntities);
//        mapR.put("top", map1);
//        return R.ok().put("data", mapR);
//
//
//    }
//
//
//    @RequestMapping(value = "/disGetReduceGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R disGetReduceGoods(String startDate, String stopDate, String disGoodsFatherId, String type, Integer disId) {
//
//
//        Integer howManyDaysInPeriod = 1;
//        if (!startDate.equals(stopDate)) {
//            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
//        }
//        Map<String, Object> map1 = new HashMap<>();
//        List<String> dateList = new ArrayList<>();
//        List<String> profitList = new ArrayList<>();
//        List<String> salesList = new ArrayList<>();
//        List<String> lossList = new ArrayList<>();
//        List<String> wasteList = new ArrayList<>();
//        List<Map<String, Object>> aaaf = new ArrayList<>();
//
//        Map<String, Object> map0 = new HashMap<>();
//        map0.put("startDate", startDate);
//        map0.put("stopDate", stopDate);
//        map0.put("disGoodsFatherId", disGoodsFatherId);
//        Double aDouble1P = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map0);
//        Double aDouble1S = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map0);
//        Double aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map0);
//        Double aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map0);
//
//        // top
//        for (int i = howManyDaysInPeriod + 1; i > 0; i--) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("date", formatWhatDay(-i + 1));
//            map.put("disGoodsFatherId", disGoodsFatherId);
//
//            if (type.equals("sales")) {
//                map.put("produce", 0);
//            }
//            if (type.equals("loss")) {
//                map.put("loss", 0);
//            }
//            if (type.equals("waste")) {
//                map.put("waste", 0);
//            }
//            Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
//            String s = formatWhatDay(-i + 1);
//            String substring = s.substring(8, 10);
//            dateList.add(substring);
//
//            if (integer > 0) {
//                Double weightTotal = 0.0;
//                if (type.equals("profit")) {
//                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map);
//                    profitList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                }
//                if (type.equals("sales")) {
//                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map);
//                    salesList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                }
//                if (type.equals("loss")) {
//                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
//                    lossList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                }
//                if (type.equals("waste")) {
//                    weightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
//                    wasteList.add(new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                }
//            } else {
//                if (type.equals("profit")) {
//                    profitList.add("0");
//                }
//                if (type.equals("sales")) {
//                    salesList.add("0");
//                }
//                if (type.equals("loss")) {
//                    lossList.add("0");
//                }
//                if (type.equals("waste")) {
//                    wasteList.add("0");
//                }
//            }
//        }
//
//
//        TreeSet<GbDistributerGoodsEntity> abcGbDistributerGoodsEntities = new TreeSet<>();
//        Map<String, Object> map = new HashMap<>();
//        map.put("startDate", startDate);
//        map.put("stopDate", stopDate);
//        map.put("disGoodsFatherId", disGoodsFatherId);
//        TreeSet<GbDistributerGoodsEntity> aaa = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);
//        if(aaa.size() > 0){
//            for (GbDistributerGoodsEntity goodsEntity : aaa) {
//                //1 求总wasteTotal
//                if (type.equals("profit")) {
//                    Map<String, Object> map11 = new HashMap<>();
//                    map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//                    map11.put("startDate", startDate);
//                    map11.put("stopDate", stopDate);
//                    Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
//                    if (integer > 0) {
//                        Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map11);
//                        Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
//                        Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
//                        Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
//                        double profitRate = 0;
//                        double costTotal = 0;
//                        if(produceSubtotal > 0){
//                            costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
//                            profitRate = aDouble / costTotal * 100;
//                        }
//
//                        String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//                        goodsEntity.setGoodsProfitTotalString(s);
//                        goodsEntity.setGoodsProfitTotal(aDouble);
//                        goodsEntity.setGoodsCostTotal(costTotal);
//                        goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        goodsEntity.setGoodsCostRateString(new BigDecimal(profitRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
//                    } else {
//                        goodsEntity.setGoodsProfitTotalString("0");
//                        goodsEntity.setGoodsProfitTotal(0.0);
//                        goodsEntity.setGoodsCostTotal(0.0);
//                        goodsEntity.setGoodsCostTotalString("0");
//                        goodsEntity.setGoodsCostRateString("0.0");
//
//                    }
//                    abcGbDistributerGoodsEntities = abcProfit(aaa);
//                }
//                if (type.equals("sales")) {
//                    Map<String, Object> map11 = new HashMap<>();
//                    map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//                    map11.put("startDate", startDate);
//                    map11.put("stopDate", stopDate);
//                    Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
//
//                    if (integer > 0) {
//                        Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map11);
//                        Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
//                        double salesRate = 0;
//                        double costTotal = 0;
//                        if(produceSubtotal > 0){
//                            Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
//                            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
//                            costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
//                            salesRate = aDouble / costTotal * 100;
//                        }
//
//                        String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//                        goodsEntity.setGoodsProduceTotalString(s);
//                        goodsEntity.setGoodsProduceTotal(aDouble);
//                        goodsEntity.setGoodsCostTotal(costTotal);
//                        goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        goodsEntity.setGoodsSalesRateString(new BigDecimal(salesRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
//
//                    } else {
//                        goodsEntity.setGoodsProduceTotalString("0");
//                        goodsEntity.setGoodsProduceTotal(0.0);
//                        goodsEntity.setGoodsCostTotal(0.0);
//                        goodsEntity.setGoodsCostTotalString("0");
//                        goodsEntity.setGoodsSalesRateString("0.0");
//                    }
//                    abcGbDistributerGoodsEntities = abcSales(aaa);
//                }
//                if (type.equals("loss")) {
//                    Map<String, Object> map11 = new HashMap<>();
//                    map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//                    map11.put("startDate", startDate);
//                    map11.put("stopDate", stopDate);
//                    Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
//
//                    if (integer > 0) {
//                        Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
//                        Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
//                        Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
//                        Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
//                        double lossRate = 0;
//                        double costTotal = 0;
//                        if(produceSubtotal > 0){
//                            costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
//                            lossRate = aDouble / costTotal * 100;
//                        }
//
//                        String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//                        goodsEntity.setGoodsLossTotalString(s);
//                        goodsEntity.setGoodsLossTotal(aDouble);
//                        goodsEntity.setGoodsCostTotal(costTotal);
//                        goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        goodsEntity.setGoodsLossRateString(new BigDecimal(lossRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
//                    } else {
//                        goodsEntity.setGoodsLossTotalString("0");
//                        goodsEntity.setGoodsLossTotal(0.0);
//                        goodsEntity.setGoodsCostTotal(0.0);
//                        goodsEntity.setGoodsCostTotalString("0.0");
//                        goodsEntity.setGoodsLossRateString("0.0");
//                    }
//                    abcGbDistributerGoodsEntities = abcLoss(aaa);
//                }
//                if (type.equals("waste")) {
//                    Map<String, Object> map11 = new HashMap<>();
//                    map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//                    map11.put("startDate", startDate);
//                    map11.put("stopDate", stopDate);
//                    Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
//
//                    if (integer > 0) {
//                        Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
//                        Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
//                        Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
//                        Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
//                        double wasteRate = 0;
//                        double costTotal = 0;
//                        if(produceSubtotal > 0){
//                            costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
//                            wasteRate = aDouble / costTotal * 100;
//                        }
//                        String s = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//                        goodsEntity.setGoodsWasteTotalString(s);
//                        goodsEntity.setGoodsWasteTotal(aDouble);
//                        goodsEntity.setGoodsCostTotal(costTotal);
//                        goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        goodsEntity.setGoodsWasteRateString(new BigDecimal(wasteRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
//                    } else {
//                        goodsEntity.setGoodsWasteTotalString("0");
//                        goodsEntity.setGoodsWasteTotal(0.0);
//                        goodsEntity.setGoodsCostTotal(0.0);
//                        goodsEntity.setGoodsCostTotalString("0.0");
//                        goodsEntity.setGoodsWasteRateString("0.0");
//                    }
//                    abcGbDistributerGoodsEntities = abcWaste(aaa);
//                }
//            }
//        }
//
//
//        map1.put("date", dateList);
//        map1.put("profit", profitList);
//        map1.put("sales", salesList);
//        map1.put("loss", lossList);
//        map1.put("waste", wasteList);
//        map1.put("profitTotal", new BigDecimal(aDouble1P).setScale(1, BigDecimal.ROUND_HALF_UP));
//        map1.put("salesTotal", new BigDecimal(aDouble1S).setScale(1, BigDecimal.ROUND_HALF_UP));
//        map1.put("lossTotal", new BigDecimal(aDouble1L).setScale(1, BigDecimal.ROUND_HALF_UP));
//        map1.put("wasteTotal", new BigDecimal(aDouble1W).setScale(1, BigDecimal.ROUND_HALF_UP));
//        Map<String, Object> mapR = new HashMap<>();
//        mapR.put("arr", abcGbDistributerGoodsEntities);
//        mapR.put("top", map1);
//        return R.ok().put("data", mapR);
//
//
//    }
}
