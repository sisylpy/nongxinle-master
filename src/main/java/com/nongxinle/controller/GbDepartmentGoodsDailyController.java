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
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepGoodsStockReduceService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;
    @Autowired
    private GbDepartmentUserService gbDepartmentUserService;


    @RequestMapping(value = "/getDepClearGoodsDetail", method = RequestMethod.POST)
    @ResponseBody
    public R getDepClearGoodsDetail(String searchDepIds, String startDate, String stopDate, Integer disId) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<Map<String, Object>> resultList = new ArrayList<>();

        if (howManyDaysInPeriod > 0) {
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                Map<String, Object> mapDate = new HashMap<>();
                mapDate.put("date", whichDay);

                Map<String, Object> mapSearch = new HashMap<>();
                mapSearch.put("date", whichDay);
                mapSearch.put("controlFresh", 1);
                mapSearch.put("equalType", 1);
                mapSearch.put("disId", disId);
                mapSearch.put("produce", 0);
                mapSearch.put("clear", -1);
                if (!searchDepIds.equals("-1")) {
                    List<String> idsGb = new ArrayList<>();
                    String[] arrGb = searchDepIds.split(",");
                    for (String idGb : arrGb) {
                        idsGb.add(idGb);
                        if (idsGb.size() > 0) {
                            mapSearch.put("depFatherIds", idsGb);
                        }
                    }
                } else {
                    mapSearch.put("depType", getGbDepartmentTypeMendian());
                }

                System.out.println("ffkdkafk;asfa" + mapSearch);

                TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsWithBusinessDep(mapSearch);
                if (distributerGoodsEntities.size() > 0) {
                    String time = "--:--";
                    Integer integerT = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapSearch);
                    if (integerT > 0) {
                        int clearHourT = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(mapSearch);
                        int clearMinuteT = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(mapSearch);
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
                        mapDate.put("time", time);
                        mapDate.put("arr", distributerGoodsEntities);
                        resultList.add(mapDate);
                    }
                }
            }
        } else {
            Map<String, Object> mapDate = new HashMap<>();
            mapDate.put("date", startDate);

            Map<String, Object> mapSearch = new HashMap<>();
            mapSearch.put("date", startDate);
            mapSearch.put("controlFresh", 1);
            mapSearch.put("equalType", 1);
            mapSearch.put("disId", disId);
            mapSearch.put("produce", 0);
            mapSearch.put("clear", -1);
            if (!searchDepIds.equals("-1")) {
                List<String> idsGb = new ArrayList<>();
                String[] arrGb = searchDepIds.split(",");
                for (String idGb : arrGb) {
                    idsGb.add(idGb);
                    if (idsGb.size() > 0) {
                        mapSearch.put("depFatherIds", idsGb);
                    }
                }
            } else {
                mapSearch.put("depType", getGbDepartmentTypeMendian());
            }

            TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsWithBusinessDep(mapSearch);
            if (distributerGoodsEntities.size() > 0) {
                mapDate.put("arr", distributerGoodsEntities);
                String time = "--:--";
                Integer integerT = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapSearch);
                if (integerT > 0) {
                    int clearHourT = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(mapSearch);
                    int clearMinuteT = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(mapSearch);
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
                    mapDate.put("time", time);
                    mapDate.put("arr", distributerGoodsEntities);
                    resultList.add(mapDate);
                }
            }
        }

        //
        Map<String, Object> mapSearchT = new HashMap<>();
        mapSearchT.put("startDate", startDate);
        mapSearchT.put("stopDate", stopDate);
        mapSearchT.put("controlFresh", 1);
        mapSearchT.put("equalType", 1);
        mapSearchT.put("disId", disId);
        mapSearchT.put("produce", 0);
        mapSearchT.put("clear", -1);
        if (!searchDepIds.equals("-1")) {
            List<String> idsGb = new ArrayList<>();
            String[] arrGb = searchDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    mapSearchT.put("depFatherIds", idsGb);
                }
            }
        } else {
            mapSearchT.put("depType", getGbDepartmentTypeMendian());
        }

        String totalValue = "";
        Integer integerT = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapSearchT);
        if (integerT > 0) {
            int clearHourT = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(mapSearchT);
            int clearMinuteT = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(mapSearchT);
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
            totalValue = hour + ":" + minStringT;

            Map<String, Object> mapResult = new HashMap<>();
            mapResult.put("arr", resultList);
            mapResult.put("totalData", totalValue);

            return R.ok().put("data", mapResult);

        } else {
            return R.error(-1, "没有数据");
        }

    }


    @RequestMapping(value = "/disGetPurGoodsCata", method = RequestMethod.POST)
    @ResponseBody
    public R disGetPurGoodsCata(Integer disId, String startDate, String stopDate,
                                Integer supplierId, Integer purUserId) {
        Map<String, Object> mapCost = new HashMap<>();
        //
        double aDoutble = 0.0;
        Double saleDouble = 0.0;
        Double wasteDouble = 0.0;
        Double lossDouble = 0.0;
        Double purSubtotal = 0.0;
        Double stockTotal = 0.0;


        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("dayuStatus", -1);
        mapDep.put("startDate", startDate);
        mapDep.put("stopDate", stopDate);

        System.out.println("mapdeeepee" + mapDep);
        Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDep);
        if (count > 0) {
            lossDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDep);
            wasteDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDep);
            saleDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDep);
            aDoutble = saleDouble + lossDouble + wasteDouble;

            System.out.println("newmaappapa" + mapDep);
            TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoods = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParamsTree(mapDep);
            for (GbDistributerFatherGoodsEntity great : greatGrandGoods) {
                double add = 0.0;
                mapDep.put("disGoodsGreatId", great.getGbDistributerFatherGoodsId());
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDep);
                if (integer > 0) {
                    Double produceTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDep);
                    Double wasteTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDep);
                    Double lossTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDep);
                    add = produceTotal + wasteTotal + lossTotal;

                }
//                BigDecimal divide = new BigDecimal(0);
//                if (!new BigDecimal(aDoutble).equals(BigDecimal.ZERO)) {
//                    divide = new BigDecimal(add).divide(new BigDecimal(aDoutble), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//                }

                Double subTotal = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(mapDep);
                mapDep.put("startDate", null);
                mapDep.put("stopDate", null);
                Double aDouble = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(mapDep);

                Map<String, Object> map = new HashMap<>();
                map.put("costTotal", String.format("%.1f", add));
                map.put("purTotal", String.format("%.1f", subTotal));
                map.put("stockTotal", String.format("%.1f", aDouble));
                great.setDailyData(map);

            }

            mapCost.put("arr", greatGrandGoods);
            mapCost.put("total", String.format("%.1f", aDoutble));
            mapCost.put("salesTotal", String.format("%.1f", saleDouble));
            mapCost.put("lossTotal", String.format("%.1f", lossDouble));
            mapCost.put("wasteTotal", String.format("%.1f", wasteDouble));

        }

        return R.ok().put("data", mapCost);
    }

    @RequestMapping(value = "/disGetDepGoodsDailyTotal", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDepGoodsDailyTotal(Integer disId, String startDate, String stopDate, String type,
                                      String fenxiType, String searchDepIds, Integer searchDepId) {

        Map<String, Object> depGoosDailyFenxi = new HashMap<>();
        if (fenxiType.equals("profitEcharts")) {
            depGoosDailyFenxi = getDepGoosDailyProfitFenxi(disId, startDate, stopDate, type, searchDepIds);
        }
        if (fenxiType.equals("weightEcharts")) {
            depGoosDailyFenxi = getDepGoosDailyWeightFenxi(disId, startDate, stopDate, type, searchDepIds);
        }

        if (fenxiType.equals("costEcharts")) {
            depGoosDailyFenxi = getDepGoosDailyCostFenxi(disId, startDate, stopDate, type, searchDepIds, searchDepId);
        }

        System.out.println("codidididiididiididdoddddcodcocococo====" + depGoosDailyFenxi.get("code"));
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


        System.out.println("fenxiixixix" + type);
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
                if (!searchDepIds.equals("-1")) {
                    String[] arrGb = searchDepIds.split(",");
                    List<String> idsGb = new ArrayList<>();
                    for (String idGb : arrGb) {
                        idsGb.add(idGb);
                        if (idsGb.size() > 0) {
                            mapDisGoods.put("depFatherIds", idsGb);
                        }
                    }
                } else {
                    mapDisGoods.put("depType", getGbDepartmentTypeMendian());

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
            } else {
                mapDisGoods.put("depType", getGbDepartmentTypeMendian());
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

                    System.out.println("dateee" + mapDisGoods + "adoublle" + aDouble);

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

                System.out.println("lisisiisisisiis" + list);
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
        } else {
            map1222.put("depType", getGbDepartmentTypeMendian());
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
            System.out.println("112133131313123" + map1222);
            greatGrandGoodsCost = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map1222);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsCost) {
                double greatGrandDouble = 0.0;
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
                    greatGrandFather.setFatherSellingSubtotal(greatGrandDouble);
                    greatGrandFather.setFatherSellingSubtotalString(new BigDecimal(greatGrandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("loss")) {
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
            return R.error(-1, "没有数据");
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

        mapCost.put("code", "0");
        return mapCost;
    }


    private Map<String, Object> getDepGoosDailyCostFenxi(Integer disId, String startDate, String stopDate, String type, String searchDepIds, Integer searchDepId) {

        Map<String, Object> mapCost = new HashMap<>();
        //
        double aDoutble = 0.0;
        Double saleDouble = 0.0;
        Double wasteDouble = 0.0;
        Double lossDouble = 0.0;
        Integer howManyDaysInPeriod = 0;
        Double divideData = 0.0;

        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        if (searchDepId != -1) {
            mapDep.put("depId", searchDepId);
        } else {
            if (!searchDepIds.equals("-1")) {
                String[] arrGb = searchDepIds.split(",");
                List<String> idsGb = new ArrayList<>();
                for (String idGb : arrGb) {
                    idsGb.add(idGb);
                    if (idsGb.size() > 0) {
                        mapDep.put("depFatherIds", idsGb);
                    }
                }
            } else {
                mapDep.put("depType", getGbDepartmentTypeMendian());
            }
        }

        mapDep.put("dayuStatus", -1);
        if (howManyDaysInPeriod > 0) {
            mapDep.put("startDate", startDate);
            mapDep.put("stopDate", stopDate);
        } else {
            mapDep.put("date", startDate);
        }


        System.out.println("mapdeeepee" + mapDep);
        Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDep);
        if (count > 0) {
            lossDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDep);
            wasteDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDep);
            saleDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDep);
            aDoutble = saleDouble + lossDouble + wasteDouble;
            divideData = aDoutble;

            if (type.equals("sales")) {
                mapDep.put("produce", 0);
                divideData = saleDouble;
            }
            if (type.equals("loss")) {
                mapDep.put("loss", 0);
                divideData = lossDouble;
            }
            if (type.equals("waste")) {
                mapDep.put("waste", 0);
                divideData = wasteDouble;
            }

            System.out.println("newmaappapa" + mapDep);
            TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoods = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParamsTree(mapDep);
            for (GbDistributerFatherGoodsEntity great : greatGrandGoods) {
                double add = 0.0;
                mapDep.put("disGoodsGreatId", great.getGbDistributerFatherGoodsId());
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDep);
                if (integer > 0) {
                    if (type.equals("total")) {
                        Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDep);
                        Double wasteTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDep);
                        Double lossTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDep);

                        add = stockTotal + wasteTotal + lossTotal;
                    }
                    if (type.equals("sales")) {
                        add = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDep);
                    }
                    if (type.equals("loss")) {
                        add = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDep);
                    }
                    if (type.equals("waste")) {
                        add = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDep);
                    }
                }
                BigDecimal divide = new BigDecimal(0);
                if (!new BigDecimal(aDoutble).equals(BigDecimal.ZERO)) {
                    divide = new BigDecimal(add).divide(new BigDecimal(divideData), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                great.setFatherStockTotalPercent(divide.toString());
                great.setFatherStockTotal(add);
                great.setFatherStockTotalString(new BigDecimal(add).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            }

            mapCost.put("arr", abcFatherStocktTotal(greatGrandGoods));
            mapCost.put("dayData", getEveryDayDailyData(disId, startDate, stopDate, -1, searchDepId, type));
            mapCost.put("total", String.format("%.1f", aDoutble));
            mapCost.put("salesTotal", String.format("%.1f", saleDouble));
            mapCost.put("lossTotal", String.format("%.1f", lossDouble));
            mapCost.put("wasteTotal", String.format("%.1f", wasteDouble));

        } else {
            mapCost.put("total", "0.0");
        }
        List<GbDepartmentEntity> deplist = new ArrayList<>();
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");

            for (String depId : arrGb) {
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(depId));
                mapDep.put("depFatherId", depId);
                mapDep.put("depFatherIds", null);
                mapDep.put("disGoodsFatherId", null);
                System.out.println("dadkafja;fjalfa;slf;alsf;as" + mapDep);
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDep);
                double aDoutbleDep = 0.0;
                if (integer > 0) {
                    double lossDoubleDep = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDep);
                    double wasteDoubleDep = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDep);
                    double saleDoubleDep = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDep);
                    if (type.equals("sales")) {
                        aDoutbleDep = saleDoubleDep;
                    }
                    if (type.equals("loss")) {
                        aDoutbleDep = lossDoubleDep;
                    }
                    if (type.equals("waste")) {
                        aDoutbleDep = wasteDoubleDep;
                    }
                    if (type.equals("total")) {
                        aDoutbleDep = saleDoubleDep + lossDoubleDep + wasteDoubleDep;
                    }
                }
                departmentEntity.setDepCostGoodsTotalString(new BigDecimal(aDoutbleDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                deplist.add(departmentEntity);
            }
        }
        mapCost.put("code", "0");
        mapCost.put("depArr", deplist);
        System.out.println("channgnmauuucococ1111" + mapCost);
        return mapCost;
    }


    private Map<String, Object> getEveryDayDailyData(Integer disId, String startDate, String stopDate,
                                                     Integer greatGrandId, Integer searchDepId, String type) {

        //
        double aDoutble = 0.0;
        List<Map<String, Object>> daysList = new ArrayList<>();
        Integer howManyDaysInPeriod = 0;

        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        System.out.println("aababbabbabbabbabab");

        Map<String, Object> mapCost = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<String> list = new ArrayList<>();

        if (howManyDaysInPeriod > 0) {

            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {

                Map<String, Object> mapDay = new HashMap<>();

                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);
                mapDay.put("date", whichDay);
                // disGoods
                Map<String, Object> mapDisGoods = new HashMap<>();
                mapDisGoods.put("date", whichDay);
                if (disId != -1) {
                    mapDisGoods.put("disId", disId);
                }
                if (searchDepId != -1) {
                    mapDisGoods.put("depId", searchDepId);
                }
                if (greatGrandId != -1) {
                    mapDisGoods.put("disGoodsGreatId", greatGrandId);
                }

                mapDisGoods.put("depType", getGbDepartmentTypeMendian());

                System.out.println("mapdididiidsisisidiidissssssss" + mapDisGoods);
                Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
                if (count > 0) {
                    if (type.equals("total")) {
                        Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                        Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                        Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                        aDoutble = aDoubleP + aDoubleL + aDoubleW;

                        list.add(new BigDecimal(aDoutble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        mapDay.put("value", new BigDecimal(aDoutble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        mapDay.put("produceValue", new BigDecimal(aDoubleP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        mapDay.put("lossValue", new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        mapDay.put("wasteValue", new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        daysList.add(mapDay);
                    }
                    if (type.equals("sales")) {
                        Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                        list.add(new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        mapDay.put("value", new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        daysList.add(mapDay);
                    }
                    if (type.equals("loss")) {
                        Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                        list.add(new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        mapDay.put("value", new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        daysList.add(mapDay);
                    }
                    if (type.equals("waste")) {
                        Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                        list.add(new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        mapDay.put("value", new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        daysList.add(mapDay);
                    }

                    System.out.println("lisiisisisisis" + list);

                } else {
                    list.add("0");
                    mapDay.put("value", "0");
                    daysList.add(mapDay);
                }

            }
        } else {
            // dateList
            String substring = startDate.substring(8, 10);
            dateList.add(substring);
            Map<String, Object> mapDay = new HashMap<>();
            mapDay.put("date", startDate);

            // disGoods
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("date", startDate);

            if (disId != -1) {
                mapDisGoods.put("disId", disId);
            }
            if (searchDepId != -1) {
                mapDisGoods.put("depId", searchDepId);
            }

            if (greatGrandId != -1) {
                mapDisGoods.put("disGoodsGreatId", greatGrandId);
            }
            mapDisGoods.put("depType", getGbDepartmentTypeMendian());

            System.out.println("mapdididiidsisisidiidi" + mapDisGoods);
            Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
            if (count > 0) {
                if (type.equals("total")) {
                    Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                    Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                    Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                    aDoutble = aDoubleP + aDoubleL + aDoubleW;

                    list.add(new BigDecimal(aDoutble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    mapDay.put("value", new BigDecimal(aDoutble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    daysList.add(mapDay);
                }
                if (type.equals("sales")) {
                    Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                    list.add(new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    mapDay.put("value", new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    daysList.add(mapDay);
                }
                if (type.equals("loss")) {
                    Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                    list.add(new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    mapDay.put("value", new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    daysList.add(mapDay);
                }
                if (type.equals("waste")) {
                    Double stockTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                    list.add(new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    mapDay.put("value", new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    daysList.add(mapDay);
                }
            } else {
                list.add("0");
                mapDay.put("value", "0");
                daysList.add(mapDay);
            }
        }


        mapCost.put("date", dateList);
        mapCost.put("value", list);
        mapCost.put("dayList", daysList);


        return mapCost;
    }

    private Map<String, Object> getEveryDayDailyDataType(Integer disId, String startDate, String stopDate,
                                                         Integer greatGrandId, String type) {

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
                if (disId != -1) {
                    mapDisGoods.put("disId", disId);
                }

                if (greatGrandId != -1) {
                    mapDisGoods.put("disGoodsGreatId", greatGrandId);
                }

                mapDisGoods.put("depType", getGbDepartmentTypeMendian());
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
                    if (type.equals("total")) {
                        Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                        Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                        Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                        aDoutble = aDoubleP + aDoubleL + aDoubleW;

                        list.add(new BigDecimal(aDoutble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }


                    if (type.equals("sales")) {
                        System.out.println("salessalessales" + mapDisGoods);
                        System.out.println("salessalessalessales" + list);
                        saleDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                        list.add(new BigDecimal(saleDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    }
                    if (type.equals("loss")) {

                        lossDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                        list.add(new BigDecimal(lossDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    }
                    if (type.equals("waste")) {
                        wasteDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                        list.add(new BigDecimal(wasteDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    }

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
            if (disId != -1) {
                mapDisGoods.put("disId", disId);
            }

            if (greatGrandId != -1) {
                mapDisGoods.put("disGoodsGreatId", greatGrandId);
            }
            mapDisGoods.put("depType", getGbDepartmentTypeMendian());

            System.out.println("mapdididiidsisisidiidi" + mapDisGoods);
            Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
            if (count > 0) {
                if (type.equals("total")) {
                    Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                    Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                    Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                    aDoutble = aDoubleP + aDoubleL + aDoubleW;

                    list.add(new BigDecimal(aDoutble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                if (type.equals("sales")) {
                    System.out.println("salessalessales" + mapDisGoods);
                    System.out.println("salessalessales" + list);
                    saleDouble = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDisGoods);
                    list.add(new BigDecimal(saleDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }
                if (type.equals("loss")) {

                    lossDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDisGoods);
                    list.add(new BigDecimal(lossDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }
                if (type.equals("waste")) {
                    wasteDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDisGoods);
                    list.add(new BigDecimal(wasteDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }


            } else {
                list.add("0");
            }
        }


        mapCost.put("date", dateList);
        mapCost.put("value", list);


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
            return R.error(-1, "没有数据");
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


    @RequestMapping(value = "/getGoodsEchartsByGoodsGreatId", method = RequestMethod.POST)
    @ResponseBody
    public R getGoodsEchartsByGoodsGreatId(String startDate, String stopDate, Integer disGoodsGreatId, Integer disId,
                                           String type, String echartsType, String searchDepIds, String searchDepId) {
        Map<String, Object> echartsWeight = new HashMap<>();
        if (echartsType.equals("weightEcharts")) {
            echartsWeight = getEchartsWeightGreat(disId, startDate, stopDate, disGoodsGreatId, type, searchDepIds, searchDepId);
        }
//        if (echartsType.equals("profitEcharts")) {
//            echartsWeight = getEchartsProfit(startDate, stopDate, disGoodsGrandId, type, searchDepIds);
//        }
        if (echartsType.equals("costEcharts")) {
//            if (disGoodsGreatId != -1) {
            echartsWeight = getEchartsCostGreat(disId, startDate, stopDate, disGoodsGreatId, type, searchDepIds, searchDepId);
//            } else {
//                echartsWeight = getEchartsCostFather(startDate, stopDate, type, searchDepIds, searchDepId);
//            }
        }

        if (echartsWeight.get("code").equals("0")) {
            return R.ok().put("data", echartsWeight);
        } else {
            return R.error(-1, "没有数据");
        }

    }


    @RequestMapping(value = "/getGbGoodsCostStatistics", method = RequestMethod.POST)
    @ResponseBody
    public R getGbGoodsCostStatistics(String startDate, String stopDate,
                                      Integer disId, Integer greatId, String searchDepId) {


        Map<String, Object> map0 = new HashMap<>();
        map0.put("disId", disId);
        map0.put("startDate", startDate);
        map0.put("stopDate", stopDate);
        if (greatId != -1) {
            map0.put("disGoodsGreatId", greatId);
        }
        if (!searchDepId.equals("-1")) {
            map0.put("depId", searchDepId);
        } else {
            map0.put("depType", getGbDepartmentTypeMendian());
        }

        System.out.println("granddnndnddnnddmdmdmd" + map0);
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
        if (integer1 == 0) {
            return R.error(-1, "没有数据");
        }

        Double aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map0);

        Double aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map0);
        Double aDouble1S = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map0);
        double aDouble1P = aDouble1S + aDouble1L + aDouble1W;

        map0.put("equalType", 1);
        System.out.println("coudndududud" + map0);
        Integer produceCount = gbDepGoodsStockReduceService.queryReduceGoodsTotalCount(map0);
        map0.put("equalType", 3);
        Integer lossCount = gbDepGoodsStockReduceService.queryReduceGoodsTotalCount(map0);
        map0.put("equalType", 2);
        Integer wasteCount = gbDepGoodsStockReduceService.queryReduceGoodsTotalCount(map0);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("allTotal", new BigDecimal(aDouble1P).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("salesTotal", new BigDecimal(aDouble1S).setScale(1, BigDecimal.ROUND_HALF_UP));

        mapR.put("lossTotal", new BigDecimal(aDouble1L).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("wasteTotal", new BigDecimal(aDouble1W).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("produceCount", produceCount);
        mapR.put("lossCount", lossCount);
        mapR.put("wasteCount", wasteCount);




        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/getGoodsCostBySearchDate", method = RequestMethod.POST)
    @ResponseBody
    public R getGoodsCostBySearchDate(String startDate, String stopDate, Integer disId,
                                      String type, String searchDepId, Integer page,
                                      Integer limit, Integer greatId) {

        Map<String, Object> map0 = new HashMap<>();
        map0.put("disId", disId);
        map0.put("offset", (page - 1) * limit);
        map0.put("limit", limit);
        map0.put("startDate", startDate);
        map0.put("stopDate", stopDate);
        if (greatId != -1) {
            map0.put("disGoodsGreatId", greatId);
        }

        String orderType = null;
        switch (type) {
            case "cost":
                orderType = "cost";
                break;
            case "sales":
                orderType = "produce";
                break; // 关键：sales -> produce
            case "loss":
                orderType = "loss";
                break;
            case "waste":
                orderType = "waste";
                break;
            default:
                orderType = null;      // 默认用商品排序
        }
        map0.put("orderType", orderType);

        if (type.equals("sales")) {
            map0.put("equalType", 1);
        } else if (type.equals("waste")) {
            map0.put("equalType", 2);
        } else if (type.equals("loss")) {
            map0.put("equalType", 3);
        }

        if (!searchDepId.equals("-1")) {
            map0.put("depId", searchDepId);
        } else {
            map0.put("depType", getGbDepartmentTypeMendian());
        }
        System.out.println("adrrrrr" + map0);

        Integer totalCount = gbDepGoodsStockReduceService.queryReduceGoodsTotalCount(map0);
        Integer totalPages = (int) Math.ceil((double) totalCount / limit);

        // 测试新接口
        List<GbDistributerGoodsEntity> goodsList = gbDepGoodsStockReduceService.queryGoodsStockRecordTreeWithDetailV2(map0);
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("totalPages", totalPages);
        result.put("currentPage", page);
        result.put("arr", goodsList);
        return R.ok().put("data", result);

    }


//    @RequestMapping(value = "/getGoodsCostByGreatId", method = RequestMethod.POST)
//    @ResponseBody
//    public R getGoodsCostByGreatId(String startDate, String stopDate, Integer greatId, Integer disId,
//                                      String type, String searchDepId, Integer page,
//                                      Integer limit) {
//
//        Map<String, Object> map0 = new HashMap<>();
//        map0.put("disId", disId);
//        map0.put("disGoodsGreatId", greatId);
//        map0.put("offset", (page - 1) * limit);
//        map0.put("limit", limit);
//
//        String orderType = null;
//        switch (type) {
//            case "cost":
//                orderType = "cost";
//                break;
//            case "sales":
//                orderType = "produce";
//                break; // 关键：sales -> produce
//            case "loss":
//                orderType = "loss";
//                break;
//            case "waste":
//                orderType = "waste";
//                break;
//            default:
//                orderType = null;      // 默认用商品排序
//        }
//        map0.put("orderType", orderType);
//
//        if (type.equals("sales")) {
//            map0.put("equalType", 1);
//        } else if (type.equals("waste")) {
//            map0.put("equalType", 2);
//        } else if (type.equals("loss")) {
//            map0.put("equalType", 3);
//        }
//
//        if (!searchDepId.equals("-1")) {
//            map0.put("depId", searchDepId);
//        } else {
//            map0.put("depType", getGbDepartmentTypeMendian());
//        }
//        Integer totalCount = gbDepGoodsStockReduceService.queryReduceGoodsTotalCount(map0);
//        Integer totalPages = (int) Math.ceil((double) totalCount / limit);
//
//        if (totalCount == 0) {
//            return R.error(-1, "没有数据");
//        }
//        System.out.println("grearmaappaa1111" + map0);
//
//        List<GbDistributerGoodsEntity> goodsList = gbDepGoodsStockReduceService.queryGoodsStockRecordListByParams(map0);
//        System.out.println("resultt=totalCount=" + totalCount + "goolistsize" + goodsList.size());
//
//        if (goodsList.size() > 0) {
//            for (GbDistributerGoodsEntity goodsEntity : goodsList) {
//
//                System.out.println("gooldnamme" + goodsEntity.getGoodsCostTotalString());
//                System.out.println("gooldnamme" + goodsEntity.getGbDgGoodsName());
//                //1 求总wasteTotal
//                Map<String, Object> map11 = new HashMap<>();
//                map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//                if (!searchDepId.equals("-1")) {
//                    map11.put("depId", searchDepId);
//                } else {
//                    map11.put("depType", getGbDepartmentTypeMendian());
//                }
//                Integer integerG = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
//                if (integerG > 0) {
////
//                    List<String> dateList = new ArrayList<>();
//                    List<String> produceDayValue = new ArrayList<>();
//                    List<String> wasteDayValue = new ArrayList<>();
//                    List<String> lossDayValue = new ArrayList<>();
//                    List<String> returnDayValue = new ArrayList<>();
//                    Integer howManyDaysInPeriod = 0;
//                    if (!startDate.equals(stopDate)) {
//                        howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
//                    }
//
//                    if (howManyDaysInPeriod > 0) {
//                        Map<String, Object> mapDay = new HashMap<>();
//                        // top
//                        for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
//                            // dateList
//                            String whichDay = "";
//                            if (i == 0) {
//                                whichDay = startDate;
//                            } else {
//                                whichDay = afterWhatDay(startDate, i);
//                            }
//                            //1.day
//                            String substring = whichDay.substring(8, 10);
//                            dateList.add(substring);
//                            //4,supplier
//                            mapDay.put("date", whichDay);
//                            mapDay.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//                            mapDay.put("buySubtotal", 0);
//                            processDailyBusinessData(mapDay, produceDayValue, lossDayValue, wasteDayValue, returnDayValue);
//
//                        }
//                    }
//
//                    Map<String, Object> mapEveryDay = new HashMap<>();
//                    mapEveryDay.put("produceValue", produceDayValue);
//                    mapEveryDay.put("lossValue", lossDayValue);
//                    mapEveryDay.put("wasteValue", wasteDayValue);
//                    mapEveryDay.put("returnValue", returnDayValue);
//                    mapEveryDay.put("dateList", dateList);
//
//
//                    double searchTotal = 0.0;
//                    double searchProduce = 0.0;
//                    double searchLoss = 0.0;
//                    double searchWaste = 0.0;
//                    double perTotal = 0.0;
//                    double perProduce = 0.0;
//                    double perLoss = 0.0;
//                    double perWaste = 0.0;
//                    Map<String, Object> mapRedu = new HashMap<>();
//                    mapRedu.put("disId", disId);
//                    if (!searchDepId.equals("-1")) {
//                        mapRedu.put("depId", searchDepId);
//                    } else {
//                        mapRedu.put("depType", getGbDepartmentTypeMendian());
//                    }
//                    mapRedu.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//                    Integer searchInteger = gbDepGoodsStockReduceService.queryReduceTypeCount(mapRedu);
//                    if(searchInteger > 0){
//                        searchProduce = gbDepGoodsStockReduceService.queryReduceProduceTotal(mapRedu);
//                        searchLoss = gbDepGoodsStockReduceService.queryReduceLossTotal(mapRedu);
//                        searchWaste = gbDepGoodsStockReduceService.queryReduceWasteTotal(mapRedu);
//                        searchTotal = searchProduce + searchLoss + searchWaste;
//                    }
//
//                    mapRedu.put("date", null);
//                    mapRedu.put("startDate", startDate);
//                    mapRedu.put("stopDate", stopDate);
//
//                    Integer perInteger = gbDepGoodsStockReduceService.queryReduceTypeCount(mapRedu);
//                    if(perInteger > 0){
//                        perProduce = gbDepGoodsStockReduceService.queryReduceProduceTotal(mapRedu);
//                        perLoss = gbDepGoodsStockReduceService.queryReduceLossTotal(mapRedu);
//                        perWaste = gbDepGoodsStockReduceService.queryReduceWasteTotal(mapRedu);
//                        perTotal = perProduce + perLoss + perWaste;
//                    }
//
//                    mapEveryDay.put("searchProduce", String.format("%.1f",searchProduce));
//                    mapEveryDay.put("searchLoss", String.format("%.1f",searchLoss));
//                    mapEveryDay.put("searchWaste", String.format("%.1f",searchWaste));
//                    mapEveryDay.put("searchTotal", String.format("%.1f",searchTotal));
//                    mapEveryDay.put("perProduce", String.format("%.1f",perProduce));
//                    mapEveryDay.put("perLoss", String.format("%.1f",perLoss));
//                    mapEveryDay.put("perWaste", String.format("%.1f",perWaste));
//                    mapEveryDay.put("perTotal", String.format("%.1f",perTotal));
//
//                    goodsEntity.setGoodsData(mapEveryDay);
//
//
//                } else {
//                    goodsEntity.setGoodsProfitTotalString("0");
//                    goodsEntity.setGoodsProfitTotal(0.0);
//                    goodsEntity.setGoodsCostTotal(BigDecimal.ZERO);
//                    goodsEntity.setGoodsProduceWeightTotalString("0");
//                    goodsEntity.setGoodsProduceTotalString("0");
//                    goodsEntity.setGoodsWasteWeightTotalString("0");
//                    goodsEntity.setGoodsWasteTotalString("0");
//                    goodsEntity.setGoodsLossWeightTotalString("0");
//                    goodsEntity.setGoodsLossTotalString("0");
//                    goodsEntity.setGoodsCostTotalString("0");
//                    goodsEntity.setGoodsCostRateString("0");
//                    goodsEntity.setGoodsCostWeightTotalString("0");
//                    goodsEntity.setGoodsPriceTotalString("0");
//                    goodsEntity.setGoodsPurTotalCount(0);
//                    goodsEntity.setGoodsPurTotalWeight("0");
//
//                }
//
//            }
//        }
//
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("totalCount", totalCount);
//        result.put("totalPages", totalPages);
//        result.put("currentPage", page);
//        result.put("arr", goodsList);
//        return R.ok().put("data", result);
//
//    }


    private Map<String, Object> getEchartsWeight(String startDate, String stopDate, Integer disGoodsFatherId,
                                                 String type, String searchDepIds, String searchDepId) {

        TreeSet<GbDistributerGoodsEntity> abcGbDistributerGoodsEntities = new TreeSet<>();
        Map<String, Object> map0 = new HashMap<>();
        map0.put("startDate", startDate);
        map0.put("stopDate", stopDate);
        map0.put("disGoodsFatherId", disGoodsFatherId);
        if (!searchDepId.equals("-1")) {
            map0.put("depId", searchDepId);
        } else {
            if (!searchDepIds.equals("-1")) {
                String[] arrGb = searchDepIds.split(",");
                List<String> idsGb = new ArrayList<>();
                for (String idGb : arrGb) {
                    idsGb.add(idGb);
                    if (idsGb.size() > 0) {
                        map0.put("depFatherIds", idsGb);
                    }
                }
            } else {
                map0.put("depType", getGbDepartmentTypeMendian());
            }
        }
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
        if (integer1 == 0) {
            return R.error(-1, "没有数据");
        }

        Double aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map0);
        Double aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map0);
        Double aDouble1S = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map0);
        double aDouble1P = aDouble1S + aDouble1L + aDouble1W;


        map0.put("disGoodsFatherId", disGoodsFatherId);
        TreeSet<GbDistributerGoodsEntity> aaa = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map0);
        if (aaa.size() > 0) {
            for (GbDistributerGoodsEntity goodsEntity : aaa) {
                //1 求总wasteTotal
                Map<String, Object> map11 = new HashMap<>();
                map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                map11.put("startDate", startDate);
                map11.put("stopDate", stopDate);
                if (!searchDepIds.equals("-1")) {
                    if (searchDepId.equals("-1")) {
                        String[] arrGb = searchDepIds.split(",");
                        List<String> idsGb = new ArrayList<>();
                        for (String idGb : arrGb) {
                            idsGb.add(idGb);
                            if (idsGb.size() > 0) {
                                map11.put("depFatherIds", idsGb);
                            }
                        }
                    } else {
                        map11.put("depId", searchDepId);
                    }
                } else {
                    map11.put("depType", getGbDepartmentTypeMendian());
                }
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
                if (integer > 0) {
                    Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map11);
                    Double produceWeight = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map11);
                    Double produceS = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                    Double lossWeight = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map11);
                    Double lossS = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                    Double wasteWeight = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map11);
                    Double wasteS = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                    double costWeightTotal = 0;
                    double costSubtotalTotal = 0;
                    costWeightTotal = produceWeight + lossWeight + wasteWeight;
                    costSubtotalTotal = produceS + lossS + wasteS;
                    goodsEntity.setGoodsCostTotal(new BigDecimal(costSubtotalTotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                    goodsEntity.setGoodsCostTotalString(new BigDecimal(costSubtotalTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsCostWeightTotal(costWeightTotal);
                    goodsEntity.setGoodsCostWeightTotalString(new BigDecimal(costWeightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsProduceWeightTotal(produceWeight);
                    goodsEntity.setGoodsProduceWeightTotalString(new BigDecimal(produceWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsProduceTotal(new BigDecimal(produceS).setScale(1, BigDecimal.ROUND_HALF_UP));
                    goodsEntity.setGoodsProduceTotalString(new BigDecimal(produceS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsWasteWeightTotal(wasteWeight);
                    goodsEntity.setGoodsWasteWeightTotalString(new BigDecimal(wasteWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsWasteTotal(new BigDecimal(produceS).setScale(1, BigDecimal.ROUND_HALF_UP));
                    goodsEntity.setGoodsWasteTotalString(new BigDecimal(wasteS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsLossWeightTotal(lossWeight);
                    goodsEntity.setGoodsLossWeightTotalString(new BigDecimal(lossWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsLossTotal(new BigDecimal(lossS).setScale(1, BigDecimal.ROUND_HALF_UP));
                    goodsEntity.setGoodsLossTotalString(new BigDecimal(lossS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                } else {
                    goodsEntity.setGoodsProfitTotalString("0");
                    goodsEntity.setGoodsProfitTotal(0.0);
                    goodsEntity.setGoodsCostTotal(BigDecimal.ZERO);
                    goodsEntity.setGoodsProduceWeightTotalString("0");
                    goodsEntity.setGoodsProduceTotalString("0");
                    goodsEntity.setGoodsWasteWeightTotalString("0");
                    goodsEntity.setGoodsWasteTotalString("0");
                    goodsEntity.setGoodsLossWeightTotalString("0");
                    goodsEntity.setGoodsLossTotalString("0");
                    goodsEntity.setGoodsCostTotalString("0");
                    goodsEntity.setGoodsCostRateString("0");
                    goodsEntity.setGoodsCostWeightTotalString("0");
                    goodsEntity.setGoodsCostWeightTotal(0.0);
                    goodsEntity.setGoodsPriceTotalString("0");
                    goodsEntity.setGoodsPurTotalCount(0);
                    goodsEntity.setGoodsPurTotalWeight("0");
                }
                if (type.equals("cost")) {
                    abcGbDistributerGoodsEntities = abcCostWeight(aaa);
                } else if (type.equals("sales")) {
                    abcGbDistributerGoodsEntities = abcSalesWeight(aaa);
                } else if (type.equals("waste")) {
                    abcGbDistributerGoodsEntities = abcWasteWeight(aaa);
                } else if (type.equals("loss")) {
                    abcGbDistributerGoodsEntities = abcLossWeight(aaa);
                }
            }
        }


        String[] arrGb = searchDepIds.split(",");
        List<GbDepartmentEntity> list = new ArrayList<>();
        if (!searchDepIds.equals("-1")) {
            for (String idGb : arrGb) {
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(idGb));
                map0.put("depId", departmentEntity.getGbDepartmentId());
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
                if (integer > 0) {
                    Double aDouble1LDep = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map0);
                    Double aDouble1WDep = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map0);
                    Double aDouble1SDep = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map0);
                    double aDouble1PDep = aDouble1WDep + aDouble1SDep + aDouble1LDep;
                    departmentEntity.setDepCostGoodsTotalString(new BigDecimal(aDouble1PDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    departmentEntity.setDepCostGoodsTotalString("0");
                }
                list.add(departmentEntity);
            }
        }


        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", abcGbDistributerGoodsEntities);
        mapR.put("depArr", list);
        mapR.put("oneTotal", new BigDecimal(aDouble1P).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("salesTotal", new BigDecimal(aDouble1S).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("lossTotal", new BigDecimal(aDouble1L).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("wasteTotal", new BigDecimal(aDouble1W).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("code", "0");
        return mapR;
    }


    private Map<String, Object> getEchartsWeightGreat(Integer disId, String startDate, String stopDate, Integer disGoodsGreatId,
                                                      String type, String searchDepIds, String searchDepId) {

        TreeSet<GbDistributerGoodsEntity> abcGbDistributerGoodsEntities = new TreeSet<>();
        Map<String, Object> map0 = new HashMap<>();
        map0.put("disId", disId);
        map0.put("startDate", startDate);
        map0.put("stopDate", stopDate);
        if (disGoodsGreatId != -1) {
            map0.put("disGoodsGreatId", disGoodsGreatId);
        }

        if (!searchDepId.equals("-1")) {
            map0.put("depId", searchDepId);
        } else {
            if (!searchDepIds.equals("-1")) {
                String[] arrGb = searchDepIds.split(",");
                List<String> idsGb = new ArrayList<>();
                for (String idGb : arrGb) {
                    idsGb.add(idGb);
                    if (idsGb.size() > 0) {
                        map0.put("depFatherIds", idsGb);
                    }
                }
            } else {
                map0.put("depType", getGbDepartmentTypeMendian());
            }
        }
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
        if (integer1 == 0) {
            return R.error(-1, "没有数据");
        }

        Double aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map0);
        Double aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map0);
        Double aDouble1S = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map0);
        double aDouble1P = aDouble1S + aDouble1L + aDouble1W;


        //todo sisy
        if (type.equals("sales")) {
            map0.put("produce", 0);
        }
        if (type.equals("produce")) {
            map0.put("produce", 0);
        }
        if (type.equals("loss")) {
            map0.put("loss", 0);
        }
        if (type.equals("waste")) {
            map0.put("waste", 0);
        }
        System.out.println("map0000000" + map0);
        TreeSet<GbDistributerGoodsEntity> aaa = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map0);
        if (aaa.size() > 0) {
            for (GbDistributerGoodsEntity goodsEntity : aaa) {
                //1 求总wasteTotal
                Map<String, Object> map11 = new HashMap<>();
                map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                map11.put("startDate", startDate);
                map11.put("stopDate", stopDate);
                if (!searchDepIds.equals("-1")) {
                    if (searchDepId.equals("-1")) {
                        String[] arrGb = searchDepIds.split(",");
                        List<String> idsGb = new ArrayList<>();
                        for (String idGb : arrGb) {
                            idsGb.add(idGb);
                            if (idsGb.size() > 0) {
                                map11.put("depFatherIds", idsGb);
                            }
                        }
                    } else {
                        map11.put("depId", searchDepId);
                    }
                } else {
                    map11.put("depType", getGbDepartmentTypeMendian());
                }
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
                if (integer > 0) {
                    Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map11);
                    Double produceWeight = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map11);
                    Double produceS = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map11);
                    Double lossWeight = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map11);
                    Double lossS = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                    Double wasteWeight = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map11);
                    Double wasteS = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map11);
                    double costWeightTotal = 0;
                    double costSubtotalTotal = 0;
                    costWeightTotal = produceWeight + lossWeight + wasteWeight;
                    costSubtotalTotal = produceS + lossS + wasteS;
                    goodsEntity.setGoodsCostTotal(new BigDecimal(costSubtotalTotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                    goodsEntity.setGoodsCostTotalString(new BigDecimal(costSubtotalTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsCostWeightTotal(costWeightTotal);
                    goodsEntity.setGoodsCostWeightTotalString(new BigDecimal(costWeightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsProduceWeightTotal(produceWeight);
                    goodsEntity.setGoodsProduceWeightTotalString(new BigDecimal(produceWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsProduceTotal(new BigDecimal(produceS).setScale(1, BigDecimal.ROUND_HALF_UP));
                    goodsEntity.setGoodsProduceTotalString(new BigDecimal(produceS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsWasteWeightTotal(wasteWeight);
                    goodsEntity.setGoodsWasteWeightTotalString(new BigDecimal(wasteWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsWasteTotal(new BigDecimal(wasteS).setScale(1, BigDecimal.ROUND_HALF_UP));
                    goodsEntity.setGoodsWasteTotalString(new BigDecimal(wasteS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsLossWeightTotal(lossWeight);
                    goodsEntity.setGoodsLossWeightTotalString(new BigDecimal(lossWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsLossTotal(new BigDecimal(lossS).setScale(1, BigDecimal.ROUND_HALF_UP));
                    goodsEntity.setGoodsLossTotalString(new BigDecimal(lossS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                } else {
                    goodsEntity.setGoodsProfitTotalString("0");
                    goodsEntity.setGoodsProfitTotal(0.0);
                    goodsEntity.setGoodsCostTotal(BigDecimal.ZERO);
                    goodsEntity.setGoodsProduceWeightTotalString("0");
                    goodsEntity.setGoodsProduceTotalString("0");
                    goodsEntity.setGoodsWasteWeightTotalString("0");
                    goodsEntity.setGoodsWasteTotalString("0");
                    goodsEntity.setGoodsLossWeightTotalString("0");
                    goodsEntity.setGoodsLossTotalString("0");
                    goodsEntity.setGoodsCostTotalString("0");
                    goodsEntity.setGoodsCostRateString("0");
                    goodsEntity.setGoodsCostWeightTotalString("0");
                    goodsEntity.setGoodsCostWeightTotal(0.0);
                    goodsEntity.setGoodsPriceTotalString("0");
                    goodsEntity.setGoodsPurTotalCount(0);
                    goodsEntity.setGoodsPurTotalWeight("0");
                }
                if (type.equals("cost")) {
                    abcGbDistributerGoodsEntities = abcCostWeight(aaa);
                } else if (type.equals("sales")) {
                    abcGbDistributerGoodsEntities = abcSalesWeight(aaa);
                } else if (type.equals("waste")) {
                    abcGbDistributerGoodsEntities = abcWasteWeight(aaa);
                } else if (type.equals("loss")) {
                    abcGbDistributerGoodsEntities = abcLossWeight(aaa);
                }
            }
        }


        String[] arrGb = searchDepIds.split(",");
        List<GbDepartmentEntity> list = new ArrayList<>();
        if (!searchDepIds.equals("-1")) {
            for (String idGb : arrGb) {
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(idGb));
                map0.put("depId", departmentEntity.getGbDepartmentId());
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
                if (integer > 0) {
                    Double aDouble1LDep = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map0);
                    Double aDouble1WDep = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map0);
                    Double aDouble1SDep = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map0);
                    double aDouble1PDep = aDouble1WDep + aDouble1SDep + aDouble1LDep;
                    departmentEntity.setDepCostGoodsTotalString(new BigDecimal(aDouble1PDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    departmentEntity.setDepCostGoodsTotalString("0");
                }
                list.add(departmentEntity);
            }
        }


        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", abcGbDistributerGoodsEntities);
        mapR.put("depArr", list);
        mapR.put("oneTotal", new BigDecimal(aDouble1P).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("salesTotal", new BigDecimal(aDouble1S).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("lossTotal", new BigDecimal(aDouble1L).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("wasteTotal", new BigDecimal(aDouble1W).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("code", "0");
        return mapR;
    }


    private Map<String, Object> getEchartsCostGreat(Integer disId, String startDate, String stopDate, Integer disGoodsGreatId,
                                                    String type, String searchDepIds, String searchDepId) {

        TreeSet<GbDistributerGoodsEntity> abcGbDistributerGoodsEntities = new TreeSet<>();

        Map<String, Object> map0 = new HashMap<>();
        map0.put("disId", disId);
        map0.put("startDate", startDate);
        map0.put("stopDate", stopDate);
        if (disGoodsGreatId != -1) {
            map0.put("disGoodsGreatId", disGoodsGreatId);
        }

        if (!searchDepId.equals("-1")) {
            map0.put("depId", searchDepId);
        } else {
            if (!searchDepIds.equals("-1")) {
                String[] arrGb = searchDepIds.split(",");
                List<String> idsGb = new ArrayList<>();
                for (String idGb : arrGb) {
                    idsGb.add(idGb);
                    if (idsGb.size() > 0) {
                        map0.put("depFatherIds", idsGb);
                    }
                }
            } else {
                map0.put("depType", getGbDepartmentTypeMendian());
            }
        }

        System.out.println("granddnndnddnnddmdmdmd" + map0);
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
        if (integer1 == 0) {
            return R.error(-1, "没有数据");
        }

        Double aDouble1L = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map0);
        Double aDouble1W = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map0);
        Double aDouble1S = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map0);
        double aDouble1P = aDouble1S + aDouble1L + aDouble1W;

        //todo sisy
        if (type.equals("sales")) {
            map0.put("produce", 0);
        }
        if (type.equals("produce")) {
            map0.put("produce", 0);
        }
        if (type.equals("loss")) {
            map0.put("loss", 0);
        }
        if (type.equals("waste")) {
            map0.put("waste", 0);
        }
        System.out.println("map0000000" + map0);

        TreeSet<GbDistributerGoodsEntity> aaa = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map0);
        if (aaa.size() > 0) {
            for (GbDistributerGoodsEntity goodsEntity : aaa) {
                //1 求总wasteTotal
                Map<String, Object> map11 = new HashMap<>();
                map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                map11.put("startDate", startDate);
                map11.put("stopDate", stopDate);
                if (!searchDepId.equals("-1")) {
                    map11.put("depId", searchDepId);
                } else {
                    if (!searchDepIds.equals("-1")) {
                        String[] arrGb = searchDepIds.split(",");
                        List<String> idsGb = new ArrayList<>();
                        for (String idGb : arrGb) {
                            idsGb.add(idGb);
                            if (idsGb.size() > 0) {
                                map11.put("depFatherIds", idsGb);
                            }
                        }
                    } else {
                        map11.put("depType", getGbDepartmentTypeMendian());
                    }
                }

                Integer integerG = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
                if (integerG > 0) {
                    Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map11);
                    Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map11);
                    System.out.println("kanaknakankaan" + lossSubtotal);
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
                    goodsEntity.setGoodsCostTotal(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                    goodsEntity.setGoodsProduceWeightTotalString(new BigDecimal(produceWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsProduceTotalString(new BigDecimal(produceSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsProduceTotal(new BigDecimal(produceSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                    goodsEntity.setGoodsWasteWeightTotalString(new BigDecimal(wasteWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsWasteTotalString(new BigDecimal(wasteSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsWasteTotal(new BigDecimal(wasteSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                    goodsEntity.setGoodsLossWeightTotalString(new BigDecimal(lossWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsLossTotalString(new BigDecimal(lossSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsLossTotal(new BigDecimal(lossSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                    goodsEntity.setGoodsCostTotalString(new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsCostRateString(new BigDecimal(profitRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsCostWeightTotalString(new BigDecimal(costWeightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    double v = 0;
                    if (costWeightTotal != 0) {
                        v = costTotal / costWeightTotal;
                    }
                    goodsEntity.setGoodsPriceTotalString(new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    map11.put("dayuStatus", 2);
                    Integer integer2 = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map11);
                    Integer howManyDaysInPeriod1 = getHowManyDaysInPeriod(stopDate, startDate);
                    double v1 = 0.0;
                    int wxCountAuto = 0;
                    if (integer2 > 0) {
                        double purTotal = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(map11);
                        v1 = purTotal / integer2;
                        wxCountAuto = howManyDaysInPeriod1 / integer2;
                    }
                    goodsEntity.setGoodsPurTotalCount(wxCountAuto);
                    goodsEntity.setGoodsPurTotalWeight(new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    goodsEntity.setGoodsProfitTotalString("0");
                    goodsEntity.setGoodsProfitTotal(0.0);
                    goodsEntity.setGoodsCostTotal(BigDecimal.ZERO);
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
                if (type.equals("total") || type.equals("cost")) {
                    abcGbDistributerGoodsEntities = abcCost(aaa);
                } else if (type.equals("sales")) {
                    abcGbDistributerGoodsEntities = abcSales(aaa);
                } else if (type.equals("loss")) {
                    abcGbDistributerGoodsEntities = abcLoss(aaa);
                } else if (type.equals("waste")) {
                    abcGbDistributerGoodsEntities = abcWaste(aaa);
                }
            }
        }
        List<GbDepartmentEntity> list = new ArrayList<>();
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            for (String idGb : arrGb) {
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(idGb));
                map0.put("depId", departmentEntity.getGbDepartmentId());
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
                if (integer > 0) {
                    Double aDouble1LDep = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map0);
                    Double aDouble1WDep = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map0);
                    Double aDouble1SDep = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map0);
                    double aDouble1PDep = aDouble1WDep + aDouble1SDep + aDouble1LDep;
                    departmentEntity.setDepCostGoodsTotalString(new BigDecimal(aDouble1PDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    departmentEntity.setDepCostGoodsTotalString("0");
                }
                list.add(departmentEntity);
            }
        }


        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", abcGbDistributerGoodsEntities);
        mapR.put("oneTotal", new BigDecimal(aDouble1P).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("salesTotal", new BigDecimal(aDouble1S).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("lossTotal", new BigDecimal(aDouble1L).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("wasteTotal", new BigDecimal(aDouble1W).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("depArr", list);
        Map<String, Object> result = getEveryDayDailyDataType(-1, startDate, stopDate, disGoodsGreatId, type);
        mapR.put("dayData", result);
        mapR.put("code", "0");
        return mapR;
    }


    private Map<String, Object> getEchartsCostSearchDate(Integer disId, String startDate, String stopDate, String searchDate,
                                                         String type, String searchDepId, Integer page, Integer limit) {

        Map<String, Object> map0 = new HashMap<>();
        map0.put("disId", disId);
        map0.put("date", searchDate);
        map0.put("offset", (page - 1) * limit);
        map0.put("limit", limit);

        String orderType = null;
        switch (type) {
            case "cost":
                orderType = "cost";
                break;
            case "sales":
                orderType = "produce";
                break; // 关键：sales -> produce
            case "loss":
                orderType = "loss";
                break;
            case "waste":
                orderType = "waste";
                break;
            default:
                orderType = null;      // 默认用商品排序
        }
        map0.put("orderType", orderType);

        if (type.equals("sales")) {
            map0.put("equalType", 1);
        } else if (type.equals("waste")) {
            map0.put("equalType", 2);
        } else if (type.equals("loss")) {
            map0.put("equalType", 3);
        }

        if (!searchDepId.equals("-1")) {
            map0.put("depId", searchDepId);
        } else {
            map0.put("depType", getGbDepartmentTypeMendian());
        }
        Integer totalCount = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);
        Integer totalPages = (int) Math.ceil((double) totalCount / limit);

        if (totalCount == 0) {
            return R.error(-1, "没有数据");
        }

        System.out.println("mapappanewnew" + map0);

//        TreeSet<GbDistributerGoodsEntity> goodsList = gbDepGoodsStockReduceService.queryGoodsStockRecordTreeByParams(map0);
        List<GbDistributerGoodsEntity> goodsList = gbDepGoodsStockReduceService.queryGoodsStockRecordListByParams(map0);
        System.out.println("resultt=totalCount=" + totalCount + "goolistsize" + goodsList.size());

        if (goodsList.size() > 0) {
            for (GbDistributerGoodsEntity goodsEntity : goodsList) {

                System.out.println("gooldnamme" + goodsEntity.getGoodsCostTotalString());
                System.out.println("gooldnamme" + goodsEntity.getGbDgGoodsName());
                //1 求总wasteTotal
                Map<String, Object> map11 = new HashMap<>();
                map11.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                map11.put("date", searchDate);
                if (!searchDepId.equals("-1")) {
                    map11.put("depId", searchDepId);
                } else {
                    map11.put("depType", getGbDepartmentTypeMendian());
                }

                Integer integerG = gbDepGoodsDailyService.queryDepGoodsDailyCount(map11);
                if (integerG > 0) {
//
                    List<String> dateList = new ArrayList<>();
                    List<String> produceDayValue = new ArrayList<>();
                    List<String> wasteDayValue = new ArrayList<>();
                    List<String> lossDayValue = new ArrayList<>();
                    List<String> returnDayValue = new ArrayList<>();
                    Integer howManyDaysInPeriod = 0;
                    if (!startDate.equals(stopDate)) {
                        howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
                    }

                    if (howManyDaysInPeriod > 0) {

                        System.out.println("epsososososo==0");
                        Map<String, Object> mapDay = new HashMap<>();

                        // top
                        for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                            // dateList
                            String whichDay = "";
                            if (i == 0) {
                                whichDay = startDate;
                            } else {
                                whichDay = afterWhatDay(startDate, i);
                            }
                            //1.day
                            String substring = whichDay.substring(8, 10);
                            dateList.add(substring);

                            //4,supplier
                            mapDay.put("date", whichDay);
                            mapDay.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                            mapDay.put("buySubtotal", 0);
                            processDailyBusinessData(mapDay, produceDayValue, lossDayValue, wasteDayValue, returnDayValue);

                        }
                    }

                    Map<String, Object> mapEveryDay = new HashMap<>();
                    mapEveryDay.put("produceValue", produceDayValue);
                    mapEveryDay.put("lossValue", lossDayValue);
                    mapEveryDay.put("wasteValue", wasteDayValue);
                    mapEveryDay.put("returnValue", returnDayValue);
                    mapEveryDay.put("dateList", dateList);

                    goodsEntity.setGoodsData(mapEveryDay);


                } else {
                    goodsEntity.setGoodsProfitTotalString("0");
                    goodsEntity.setGoodsProfitTotal(0.0);
                    goodsEntity.setGoodsCostTotal(BigDecimal.ZERO);
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

            }
        }


        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("totalPages", totalPages);
        result.put("currentPage", page);
        result.put("arr", goodsList);
        result.put("code", "0");
        return result;
    }

    private void processDailyBusinessData(Map<String, Object> mapDay, List<String> produceDayValue,
                                          List<String> lossDayValue, List<String> wasteDayValue, List<String> returnDayValue) {
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDay);
        System.out.println("onda0101001010 " + mapDay);
        if (integer1 > 0) {
            Double gbDgdProduceWeight = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDay);
            Double gbDgdLossWeight = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDay);
            Double gbDgdWasteWeight = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDay);
            Double gbDgdReturnWeight = gbDepGoodsDailyService.queryDepGoodsDailyReturnSubtotal(mapDay);

            produceDayValue.add(String.format("%.1f", gbDgdProduceWeight));
            lossDayValue.add(String.format("%.1f", gbDgdLossWeight));
            wasteDayValue.add(String.format("%.1f", gbDgdWasteWeight));
            returnDayValue.add(String.format("%.1f", gbDgdReturnWeight));
        } else {
            produceDayValue.add("0");
            lossDayValue.add("0");
            wasteDayValue.add("0");
            returnDayValue.add("0");
        }
    }


    private TreeSet<GbDistributerGoodsEntity> abcCost(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                // 使用 BigDecimal 的 compareTo
                int result = o2.getGoodsCostTotal().compareTo(o1.getGoodsCostTotal());
                if (result == 0) {
                    // 如果相等，为了避免TreeSet认为相等而丢掉元素，使用id或其他唯一键作为比较
                    return o2.getGbDistributerGoodsId().compareTo(o1.getGbDistributerGoodsId());
                }
                return result; // 大的在前 => 降序
            }
        });

        ts.addAll(goodsEntities);

        return ts;
    }


    private TreeSet<GbDistributerGoodsEntity> abcSales(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                // 使用 BigDecimal 的 compareTo
                int result = o2.getGoodsProduceTotal().compareTo(o1.getGoodsProduceTotal());
                if (result == 0) {
                    // 如果相等，为了避免TreeSet认为相等而丢掉元素，使用id或其他唯一键作为比较
                    return o2.getGbDistributerGoodsId().compareTo(o1.getGbDistributerGoodsId());
                }
                return result; // 大的在前 => 降序
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
                // 使用 BigDecimal 的 compareTo
                int result = o2.getGoodsLossTotal().compareTo(o1.getGoodsLossTotal());
                if (result == 0) {
                    // 如果相等，为了避免TreeSet认为相等而丢掉元素，使用id或其他唯一键作为比较
                    return o2.getGbDistributerGoodsId().compareTo(o1.getGbDistributerGoodsId());
                }
                return result; // 大的在前 => 降序
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
                // 使用 BigDecimal 的 compareTo
                int result = o2.getGoodsWasteTotal().compareTo(o1.getGoodsWasteTotal());
                if (result == 0) {
                    // 如果相等，为了避免TreeSet认为相等而丢掉元素，使用id或其他唯一键作为比较
                    return o2.getGbDistributerGoodsId().compareTo(o1.getGbDistributerGoodsId());
                }
                return result; // 大的在前 => 降序
            }
        });

        ts.addAll(goodsEntities);

        return ts;
    }


    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherStocktTotal
            (TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;
                if (o2.getFatherStockTotal() - o1.getFatherStockTotal() < 0) {
                    result = -1;
                } else if (o2.getFatherStockTotal() - o1.getFatherStockTotal() > 0) {
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


}
