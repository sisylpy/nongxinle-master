package com.nongxinle.controller;

/**
 * @author lpy
 * @date 08-19 19:02
 */

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.PageUtilsMap;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.CommonUtils.generateBillTradeNo;
import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;


@RestController
@RequestMapping("api/gbdepartmentgoodsstock")
public class GbDepartmentGoodsStockController {
    @Autowired
    private GbDepartmentGoodsStockService gbDepGoodsStockService;
    @Autowired
    private GbDepartmentService gbDepartmentService;

    @Autowired
    private GbDepInventoryGoodsWeekService gbDepInventoryGoodsWeekService;
    @Autowired
    private GbDepInventoryGoodsMonthService gbDepInventoryGoodsMonthService;
    @Autowired
    private GbDepInventoryDailyService gbDepInventoryDailyService;
    @Autowired
    private GbDepInventoryGoodsDailyService gbDepInventoryGoodsDailyService;
    @Autowired
    private GbDepInventoryGoodsDailyTotalService gbDepInventoryGoodsDailyTotalService;
    @Autowired
    private GbDepInventoryWeekService gbDepInventoryWeekService;
    @Autowired
    private GbDepInventoryMonthService gbDepInventoryMonthService;
    @Autowired
    private GbDepartmentGoodsStockRecordService gbDepGoodsStockRecordService;
    @Autowired
    private GbDepartmentOrdersService gbDepOrdersService;
    @Autowired
    private GbDistributerGoodsService disGoodsService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepartmentStockReduceService;
    @Autowired
    private GbDistributerGoodsPriceService goodsPriceService;
    @Autowired
    private GbDepartmentGoodsDailyService gbDepGoodsDailyService;
    @Autowired
    private GbDistributerWeightGoodsService gbDisWeightGoodsService;
    @Autowired
    private GbDepartmentGoodsStockReduceAttachmentService gbDepGoodsStockReduceAttachmentService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;


    @RequestMapping(value = "/getDepGoodsBusinessItem", method = RequestMethod.POST)
    @ResponseBody
    public R getDepGoodsBusinessItem(Integer searchDepId, String date, Integer disGoodsId, String type) {

        System.out.println("typee" + type);
        Map<String, Object> map = new HashMap<>();
//        map.put("reduceGoodsId", disGoodsId);
//        map.put("reduceDate", date);
//        map.put("reduceDepId", searchDepId);
//        if(type.equals("sales")){
//            map.put("reduceType", 1);
//        }
//        if(type.equals("loss")){
//            map.put("reduceType", 3);
//        }if(type.equals("waste")){
//            map.put("reduceType", 2);
//        }
        map.put("depFatherId", searchDepId);
        map.put("date", date);
        map.put("disGoodsId", disGoodsId);
        map.put("equalType", type);

        System.out.println("mapappappa" + map);
//        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockWithReduceList(map);
        List<GbDepartmentGoodsDailyEntity> departmentGoodsDailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListWithReduceByParams(map);

        GbDistributerGoodsEntity gbDistributerGoodsEntity = disGoodsService.queryObject(disGoodsId);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", departmentGoodsDailyEntities);
        mapR.put("goods", gbDistributerGoodsEntity);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/getDisGoodsStock/{disGoodsId}")
    @ResponseBody
    public R getDisGoodsStock(@PathVariable Integer disGoodsId) {

        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disGoodsId", disGoodsId);
        TreeSet<GbDepartmentEntity> departmentEntities = gbDepGoodsStockService.queryDepGoodsTreeDepartments(mapDep);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                double depDoutbleRest = 0;
                double depDoutbleRestV = 0;
                mapDep.put("depFatherId", department.getGbDepartmentId());
                Integer integer = gbDepGoodsStockService.queryGoodsStockCount(mapDep);
                if (integer > 0) {
                    depDoutbleRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDep);
                    depDoutbleRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(mapDep);
                    department.setDepRestGoodsTotalString(new BigDecimal(depDoutbleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    department.setDepRestGoodsWeightTotalString(new BigDecimal(depDoutbleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
                }
            }
        }
        return R.ok().put("data", departmentEntities);
    }

    @RequestMapping(value = "/getDistributerGoodsPriceMangement", method = RequestMethod.POST)
    @ResponseBody
    public R getDistributerGoodsPriceMangement(String startDate, String stopDate, Integer disId, Integer depId, Integer nxDisId) {

        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disId", disId);
        mapG.put("price", 1);
        List<GbDistributerGoodsEntity> distributerGoodsEntities = disGoodsService.queryGoodsByParamsGb(mapG);

        TreeSet<GbDistributerGoodsEntity> treeSetH = new TreeSet<>();
        TreeSet<GbDistributerGoodsEntity> treeSetL = new TreeSet<>();
        double hightotal = 0;
        double lowtotal = 0;

        if (distributerGoodsEntities.size() > 0) {

            for (int i = 0; i < distributerGoodsEntities.size(); i++) {

                Map<String, Object> map = new HashMap<>();
                map.put("dayuStatus", -1);
                map.put("startDate", startDate);
                map.put("stopDate", stopDate);
                map.put("disGoodsId", distributerGoodsEntities.get(i).getGbDistributerGoodsId());
                map.put("purWhat", 1);
                map.put("disId", disId);
                if (depId != -1) {
                    map.put("depId", depId);
                }
                if (nxDisId != -1) {
                    map.put("nxDisId", nxDisId);
                }
                Integer integer = goodsPriceService.queryDisPriceGoodsCount(map);
                if (integer > 0) {
                    Double priceTotal = goodsPriceService.queryPriceWhatTotal(map);
                    Double priceScale = goodsPriceService.queryPriceWhatScale(map);
                    Double weightTotal = goodsPriceService.queryPricePurWeight(map);
                    Double subtotal = goodsPriceService.queryPurTotal(map);
                    BigDecimal perPrice = new BigDecimal(subtotal).divide(new BigDecimal(weightTotal), 1, BigDecimal.ROUND_HALF_UP);

                    BigDecimal divide = new BigDecimal(priceScale).divide(new BigDecimal(integer), 4, BigDecimal.ROUND_HALF_UP);
                    BigDecimal decimal = divide.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                    GbDistributerGoodsEntity highGoods = new GbDistributerGoodsEntity();
                    highGoods.setGbDistributerGoodsId(distributerGoodsEntities.get(i).getGbDistributerGoodsId() + 1);
                    highGoods.setGbDgGoodsName(distributerGoodsEntities.get(i).getGbDgGoodsName());
                    highGoods.setGbDgGoodsStandardname(distributerGoodsEntities.get(i).getGbDgGoodsStandardname());
                    highGoods.setGoodsAveragePrice(perPrice.doubleValue());
                    highGoods.setGoodsAveragePriceString(perPrice.toString());
                    highGoods.setGoodsAveragePricePercent(decimal.doubleValue());
                    highGoods.setGoodsAveragePricePercentString(decimal.toString() + "%");
                    highGoods.setGoodsPriceTotal(priceTotal);
                    highGoods.setGoodsPriceTotalString(new BigDecimal(priceTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    hightotal = hightotal + priceTotal;

                    treeSetH.add(highGoods);

                }
            }

            for (int i = 0; i < distributerGoodsEntities.size(); i++) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("dayuStatus", -1);
                map1.put("startDate", startDate);
                map1.put("stopDate", stopDate);
                map1.put("disGoodsId", distributerGoodsEntities.get(i).getGbDistributerGoodsId());
                map1.put("purWhat", -1);
                map1.put("disId", disId);
                if (depId != -1) {
                    map1.put("depId", depId);
                }
                if (nxDisId != -1) {
                    map1.put("nxDisId", nxDisId);
                }
                Integer integer1 = goodsPriceService.queryDisPriceGoodsCount(map1);
                if (integer1 > 0) {
                    Double priceTotal = goodsPriceService.queryPriceWhatTotal(map1);
                    Double priceScale = goodsPriceService.queryPriceWhatScale(map1);
                    Double weightTotal = goodsPriceService.queryPricePurWeight(map1);
                    Double subtotal = goodsPriceService.queryPurTotal(map1);
                    BigDecimal perPrice = new BigDecimal(subtotal).divide(new BigDecimal(weightTotal), 1, BigDecimal.ROUND_HALF_UP);

                    BigDecimal divide = new BigDecimal(priceScale).divide(new BigDecimal(integer1), 4, BigDecimal.ROUND_HALF_UP);
                    BigDecimal decimal = divide.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                    distributerGoodsEntities.get(i).setGoodsAveragePrice(perPrice.doubleValue());
                    distributerGoodsEntities.get(i).setGoodsAveragePriceString(perPrice.toString());
                    distributerGoodsEntities.get(i).setGoodsAveragePricePercent(decimal.doubleValue());
                    distributerGoodsEntities.get(i).setGoodsAveragePricePercentString(decimal.toString() + "%");
                    distributerGoodsEntities.get(i).setGoodsPriceTotal(priceTotal * -1);
                    distributerGoodsEntities.get(i).setGoodsPriceTotalString("-" + new BigDecimal(priceTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    lowtotal = lowtotal + priceTotal;
                    treeSetL.add(distributerGoodsEntities.get(i));
                }
            }
        }

        if (distributerGoodsEntities.size() > 0) {

            List<GbDistributerGoodsEntity> list = new ArrayList<>();
            list.addAll(abcGoodsPriceTotal(treeSetL));
            list.addAll(abcGoodsPriceTotalHigh(treeSetH));
            Map<String, Object> map = new HashMap<>();
            map.put("arr", list);
            map.put("highTotal", new BigDecimal(hightotal).setScale(1, BigDecimal.ROUND_HALF_UP));
            map.put("lowerTotal", new BigDecimal(lowtotal).setScale(1, BigDecimal.ROUND_HALF_UP));

            return R.ok().put("data", map);
        } else {
            return R.error(-1, "没有数据");
        }
    }


//    @RequestMapping(value = "/getToDepartmentGoodsBusinessType", method = RequestMethod.POST)
//    @ResponseBody
//    public R getToDepartmentGoodsBusinessType(Integer disGoodsId, Integer depId, Integer nxDisId,
//                                              String startDate, String stopDate, Integer disId) {
//
//        Map<String, Object> mapResult = new HashMap<>();
//        Map<String, Object> map = new HashMap<>();
//        if (nxDisId != -1) {
//            map.put("nxDisId", nxDisId);
//        }
//        if (depId != -1) {
//            map.put("purDepId", depId);
//        }
//
//        map.put("startDate", startDate);
//        map.put("stopDate", stopDate);
//        map.put("disGoodsId", disGoodsId);
//        double doutbleCost = 0;
//        double doutbleCostV = 0;
//        double v = 0;
//        double doutbleRest = 0;
//        double doutbleRestV = 0;
//        String maxPrice = "0";
//        String minPrice = "0";
//        String perPrice = "0";
//        int purCount = 0;
//
//        purCount = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
//        if (purCount > 0) {
//            System.out.println("caigoushushul" + map);
//            doutbleCostV = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);
//            doutbleCost = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(map);
//            v = doutbleCostV / doutbleCost;
//            perPrice = new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//            maxPrice = gbDepGoodsStockService.queryDepStockMaxDgsPrice(map);
//            minPrice = gbDepGoodsStockService.queryDepStockMinDgsPrice(map);
//        }
//
//        Map<String, Object> mapStock = new HashMap<>();
//        mapStock.put("disGoodsId", disGoodsId);
//        doutbleRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapStock);
//        doutbleRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(mapStock);
//
//        Map<String, Object> mapDep = new HashMap<>();
//        mapDep.put("disId", disId);
//        mapDep.put("type", getGbDepartmentTypeMendian());
//        System.out.println("a=memndnddpep" + mapDep);
////        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(mapDep);
//        List<GbDepartmentEntity> departmentEntities = gbDepGoodsStockService.queryDepGoodsTreeDepartments(mapStock);
//        if (departmentEntities.size() > 0) {
//            for (GbDepartmentEntity department : departmentEntities) {
//                double depDoutbleRest = 0;
//                double depDoutbleRestV = 0;
//                double depDoutbleCostV = 0;
//                double depDoutbleCost = 0;
//                double depDoutbleLoss = 0;
//                double depDoutbleLossV = 0;
//                double depDoutbleWaste = 0;
//                double depDoutbleWasteV = 0;
//
//                double depDoutbleProduce = 0;
//                double depDoutbleProduceV = 0;
//                Map<String, Object> mapDepStock = new HashMap<>();
//
//                mapDepStock.put("disGoodsId", disGoodsId);
////                mapDepStock.put("dayuStatus", -1);
//                mapDepStock.put("depFatherId", department.getGbDepartmentId());
//                Integer integer = gbDepGoodsStockService.queryGoodsStockCount(mapDepStock);
//                if (integer > 0) {
//                    depDoutbleRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDepStock);
//                    depDoutbleRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(mapDepStock);
//                }
//                department.setDepRestGoodsTotalString(new BigDecimal(depDoutbleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                department.setDepRestGoodsWeightTotalString(new BigDecimal(depDoutbleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                mapDepStock.put("startDate", startDate);
//                mapDepStock.put("stopDate", stopDate);
//                Integer integer2 = gbDepGoodsStockService.queryGoodsStockCount(mapDepStock);
//                if (integer2 > 0) {
//                    depDoutbleCostV = gbDepGoodsStockService.queryDepGoodsSubtotal(mapDepStock);
//                    depDoutbleCost = gbDepGoodsStockService.queryDepStockWeightTotal(mapDepStock);
//
//                    depDoutbleProduce = gbDepGoodsStockService.queryDepStockProduceWeightTotal(mapDepStock);
//                    depDoutbleProduceV = gbDepGoodsStockService.queryDepStockProduceSubtotal(mapDepStock);
//                    depDoutbleLoss = gbDepGoodsStockService.queryDepStockLossWeightTotal(mapDepStock);
//                    depDoutbleLossV = gbDepGoodsStockService.queryDepStockLossSubtotal(mapDepStock);
//                    depDoutbleWaste = gbDepGoodsStockService.queryDepStockWasteWeightTotal(mapDepStock);
//                    depDoutbleWasteV = gbDepGoodsStockService.queryDepStockWasteSubtotal(mapDepStock);
//                }
//
//                department.setDepStockSubtotalString(new BigDecimal(depDoutbleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                department.setDepStockWeightTotalString(new BigDecimal(depDoutbleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                department.setDepProduceGoodsTotalString(new BigDecimal(depDoutbleProduceV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                department.setDepProduceGoodsWeightTotalString(new BigDecimal(depDoutbleProduce).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                department.setDepLossGoodsTotalString(new BigDecimal(depDoutbleLossV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                department.setDepLossGoodsWeightTotalString(new BigDecimal(depDoutbleLoss).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                department.setDepWasteGoodsTotalString(new BigDecimal(depDoutbleWasteV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                department.setDepWasteGoodsWeightTotalString(new BigDecimal(depDoutbleWaste).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//            }
//        }
//        //分店总成本
//        mapResult.put("arr", departmentEntities);
//        mapResult.put("totalCost", new BigDecimal(doutbleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//        mapResult.put("totalCostSubtotal", new BigDecimal(doutbleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//        mapResult.put("totalRest", new BigDecimal(doutbleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//        mapResult.put("totalRestSubtotal", new BigDecimal(doutbleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//        mapResult.put("maxPrice", maxPrice);
//        mapResult.put("minPrice", minPrice);
//        mapResult.put("perPrice", perPrice);
//        mapResult.put("purCount", purCount);
//        mapResult.put("code", 0);
//
//        return R.ok().put("data", mapResult);
//
//    }


    @RequestMapping(value = "/getDisGoodsBusinessType", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsBusinessType(Integer disGoodsId, String startDate, String stopDate, Integer disId, Integer searchDepId) {

        List<GbDepartmentEntity> departmentEntities = new ArrayList<>();

        if (searchDepId == -1) {
            Map<String, Object> mapDep = new HashMap<>();
            mapDep.put("disId", disId);
            mapDep.put("type", getGbDepartmentTypeMendian());
            departmentEntities = gbDepartmentService.queryDepByDepType(mapDep);
        } else {
            departmentEntities.add(gbDepartmentService.queryObject(searchDepId));
        }


        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                double depDoutbleCostV = 0;
                double depDoutbleCost = 0;
                double depDoutbleLoss = 0;
                double depDoutbleLossV = 0;
                double depDoutbleWaste = 0;
                double depDoutbleWasteV = 0;
                double depDoutbleProduce = 0;
                double depDoutbleProduceV = 0;
                double depDoutbleReturnV = 0;
                double depDoutbleReturn = 0;

                Map<String, Object> mapDepStock = new HashMap<>();
                mapDepStock.put("disGoodsId", disGoodsId);
                mapDepStock.put("depFatherId", department.getGbDepartmentId());
                mapDepStock.put("startDate", startDate);
                mapDepStock.put("stopDate", stopDate);
                System.out.println("mapDepStockmapDepStock" + mapDepStock);
                Integer integer2 = gbDepGoodsStockService.queryGoodsStockCount(mapDepStock);
                if (integer2 > 0) {
                    depDoutbleCostV = gbDepGoodsStockService.queryDepGoodsSubtotal(mapDepStock);
                    depDoutbleCost = gbDepGoodsStockService.queryDepStockWeightTotal(mapDepStock);

                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                    Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                    if (integerProduce > 0) {
                        depDoutbleProduceV = gbDepartmentStockReduceService.queryReduceProduceTotal(mapDepStock);
                        depDoutbleProduce = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(mapDepStock);
                    } else {
                        depDoutbleProduceV = 0;
                        depDoutbleProduce = 0;
                    }
                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                    Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                    if (integerLoss > 0) {
                        depDoutbleLossV = gbDepartmentStockReduceService.queryReduceLossTotal(mapDepStock);
                        depDoutbleLoss = gbDepartmentStockReduceService.queryReduceLossWeightTotal(mapDepStock);
                    } else {
                        depDoutbleLossV = 0;
                        depDoutbleLoss = 0;
                    }
                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                    Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                    if (integerWaste > 0) {
                        depDoutbleWasteV = gbDepartmentStockReduceService.queryReduceWasteTotal(mapDepStock);
                        depDoutbleWaste = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(mapDepStock);
                    } else {
                        depDoutbleWasteV = 0;
                        depDoutbleWaste = 0;
                    }
                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
                    Integer integerReturn = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                    if (integerReturn > 0) {
                        depDoutbleReturnV = gbDepartmentStockReduceService.queryReduceReturnTotal(mapDepStock);
                        depDoutbleReturn = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(mapDepStock);
                    } else {
                        depDoutbleReturnV = 0;
                        depDoutbleReturn = 0;
                    }

                }

                department.setDepStockSubtotalString(new BigDecimal(depDoutbleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                department.setDepStockWeightTotalString(new BigDecimal(depDoutbleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                department.setDepProduceGoodsTotalString(new BigDecimal(depDoutbleProduceV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                department.setDepProduceGoodsWeightTotalString(new BigDecimal(depDoutbleProduce).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                department.setDepLossGoodsTotalString(new BigDecimal(depDoutbleLossV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                department.setDepLossGoodsWeightTotalString(new BigDecimal(depDoutbleLoss).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                department.setDepWasteGoodsTotalString(new BigDecimal(depDoutbleWasteV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                department.setDepWasteGoodsWeightTotalString(new BigDecimal(depDoutbleWaste).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                department.setDepReturnGoodsWeightTotalString(new BigDecimal(depDoutbleReturn).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                department.setDepReturnGoodsTotalString(new BigDecimal(depDoutbleReturnV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            }
        }

        return R.ok().put("data", departmentEntities);

    }


    @RequestMapping(value = "/getDepGoodsClearEveryDay", method = RequestMethod.POST)
    @ResponseBody
    public R getDepGoodsFinishEveryDay(String startDate, String stopDate, Integer depGoodsId) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> lastList = new ArrayList<>();
        List<String> todayList = new ArrayList<>();
        List<String> produceList = new ArrayList<>();
        List<String> lossList = new ArrayList<>();
        List<String> wasteList = new ArrayList<>();
        List<String> freshList = new ArrayList<>();
        List<String> clearList = new ArrayList<>();
        List<String> hourList = new ArrayList<>();
        List<String> minuteList = new ArrayList<>();
        List<String> restList = new ArrayList<>();

        if (howManyDaysInPeriod > 0) {
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                dateList.add(whichDay);

                // disGoods
                Map<String, Object> mapDisGoods = new HashMap<>();
                mapDisGoods.put("date", whichDay);
                mapDisGoods.put("depGoodsId", depGoodsId);
                Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
                String lastWeight = "0";
                String todayWeight = "0";
                String produceWeight = "0";
                String lossWeight = "0";
                String wasteWeight = "0";
                String freshRate = "0";
                String clear = "-";
                String hour = "0";
                String minute = "0";
                String restWeight = "0";
                if (count > 0) {
                    GbDepartmentGoodsDailyEntity dailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapDisGoods);
                    lastWeight = dailyEntity.getGbDgdLastWeight();
                    todayWeight = dailyEntity.getGbDgdWeight();
                    produceWeight = dailyEntity.getGbDgdProduceWeight();
                    lossWeight = dailyEntity.getGbDgdLossWeight();
                    wasteWeight = dailyEntity.getGbDgdWasteWeight();
                    restWeight = dailyEntity.getGbDgdRestWeight();
                    freshRate = dailyEntity.getGbDgdFreshRate();
                    hour = dailyEntity.getGbDgdSellClearHour();
                    minute = dailyEntity.getGbDgdSellClearMinute();
                    if (!minute.equals("0") && new BigDecimal(minute).compareTo(new BigDecimal(10)) == -1) {
                        minute = "0" + minute;
                    }

                    clear = hour + ":" + minute;
                }
                lastList.add(lastWeight);
                todayList.add(todayWeight);
                produceList.add(produceWeight);
                lossList.add(lossWeight);
                wasteList.add(wasteWeight);
                freshList.add(freshRate);
                clearList.add(clear);
                hourList.add(hour);
                minuteList.add(minute);
                restList.add(restWeight);
            }

            Map<String, Object> mapResult = new HashMap<>();
            mapResult.put("date", dateList);
            mapResult.put("last", lastList);
            mapResult.put("today", todayList);
            mapResult.put("produce", produceList);
            mapResult.put("waste", wasteList);
            mapResult.put("loss", lossList);
            mapResult.put("fresh", freshList);
            mapResult.put("clear", clearList);
            mapResult.put("hour", hourList);
            mapResult.put("minute", minuteList);
            mapResult.put("rest", restList);

            return R.ok().put("data", mapResult);
        } else {
            // dateList
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            // disGoods
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("depGoodsId", depGoodsId);
            System.out.println("mapdididiee" + mapDisGoods);
            Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
            String lastWeight = "0";
            String todayWeight = "0";
            String produceWeight = "0";
            String lossWeight = "0";
            String wasteWeight = "0";
            String freshRate = "0";
            String clear = "-";
            String hour = "0";
            String minute = "0";
            String restWeight = "0";
            if (count > 0) {
                GbDepartmentGoodsDailyEntity dailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapDisGoods);
                lastWeight = dailyEntity.getGbDgdLastWeight();
                todayWeight = dailyEntity.getGbDgdWeight();
                produceWeight = dailyEntity.getGbDgdProduceWeight();
                lossWeight = dailyEntity.getGbDgdLossWeight();
                wasteWeight = dailyEntity.getGbDgdWasteWeight();
                restWeight = dailyEntity.getGbDgdRestWeight();
                freshRate = dailyEntity.getGbDgdFreshRate();
                hour = dailyEntity.getGbDgdSellClearHour();
                minute = dailyEntity.getGbDgdSellClearMinute();
                if (!minute.equals("0") && new BigDecimal(minute).compareTo(new BigDecimal(10)) == -1) {
                    minute = "0" + minute;
                }

                clear = hour + ":" + minute;
            }
            lastList.add(lastWeight);
            todayList.add(todayWeight);
            produceList.add(produceWeight);
            lossList.add(lossWeight);
            wasteList.add(wasteWeight);
            freshList.add(freshRate);
            clearList.add(clear);
            hourList.add(hour);
            minuteList.add(minute);
            restList.add(restWeight);

            Map<String, Object> mapResult = new HashMap<>();
            mapResult.put("date", dateList);
            mapResult.put("last", lastList);
            mapResult.put("today", todayList);
            mapResult.put("produce", produceList);
            mapResult.put("waste", wasteList);
            mapResult.put("loss", lossList);
            mapResult.put("fresh", freshList);
            mapResult.put("clear", clearList);
            mapResult.put("hour", hourList);
            mapResult.put("minute", minuteList);
            mapResult.put("rest", restList);

            return R.ok().put("data", mapResult);
        }

    }


    @RequestMapping(value = "/getDepGoodsFreshEveryDay", method = RequestMethod.POST)
    @ResponseBody
    public R getDepGoodsFreshEveryDay(String startDate, String stopDate, Integer depGoodsId) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<List<Map<String, Object>>> list = new ArrayList<>();
        List<String> reteList = new ArrayList<>();


        for (int i = 0; i < howManyDaysInPeriod + 1; i++) {

            // dateList
            String whichDay = "";
            if (i == 0) {
                whichDay = startDate;
            } else {
                whichDay = afterWhatDay(startDate, i);
            }
            dateList.add(whichDay);


            // disGoods
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("date", whichDay);
            mapDisGoods.put("depGoodsId", depGoodsId);
            System.out.println("mapooodomapDisGoods" + mapDisGoods);
            Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
            String gbDgdNewWeight = "0";
            String gbDgdLastWeight = "0";
            double freshRate = 0.0;
            if (count > 0) {
                GbDepartmentGoodsDailyEntity dailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapDisGoods);
                BigDecimal gbDgdProduceWeightB = new BigDecimal(dailyEntity.getGbDgdProduceWeight());
                BigDecimal gbDgdLastWeightB = new BigDecimal(dailyEntity.getGbDgdLastProduceWeight());
                BigDecimal todayWeight = gbDgdProduceWeightB.subtract(gbDgdLastWeightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                if (todayWeight.compareTo(BigDecimal.ZERO) == 1) {
                    gbDgdNewWeight = todayWeight.toString();
                }
                gbDgdLastWeight = gbDgdLastWeightB.toString();
                freshRate = gbDepGoodsDailyService.queryDepGoodsFreshRate(mapDisGoods);

            }
            reteList.add(new BigDecimal(freshRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

            List<Map<String, Object>> aMap = new ArrayList<>();

            Map<String, Object> mapDailyone = new HashMap<>();
            mapDailyone.put("value", gbDgdNewWeight);
            mapDailyone.put("name", "当日销售");
            Map<String, Object> mapDailyTwo = new HashMap<>();
            mapDailyTwo.put("value", gbDgdLastWeight);
            mapDailyTwo.put("name", "昨日销售");
            aMap.add(mapDailyone);
            aMap.add(mapDailyTwo);
            list.add(aMap);
        }


        Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("date", dateList);
        mapResult.put("arr", list);
        mapResult.put("rate", reteList);

        return R.ok().put("data", mapResult);
    }


    /**
     * 门店和管理端每天一个丙型图显示每日出入库数据
     */
    @RequestMapping(value = "/getDepGoodsEveryData/{depGoodsId}")
    @ResponseBody
    public R getDepGoodsEveryData(@PathVariable Integer depGoodsId) {

        GbDepartmentDisGoodsEntity depGoodsEntity = gbDepartmentDisGoodsService.queryObject(depGoodsId);
        Integer gbDdgDisGoodsId = depGoodsEntity.getGbDdgDisGoodsId();


        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depGoodsEntity.getGbDdgDepartmentId());
        Integer gbDepartmentType = departmentEntity.getGbDepartmentType();

        String settleDate = departmentEntity.getGbDepartmentSettleDate();
        String settleYear = departmentEntity.getGbDepartmentSettleYear();
        String settleMonth = departmentEntity.getGbDepartmentSettleMonth();
        String settleFirstDay = settleYear + "-" + settleMonth + "-" + "01";

        Integer dayTotal = getHowManyDaysInPeriod(formatWhatDay(0), settleFirstDay);
        //
        List<Map<String, Object>> list = new ArrayList<>();
        List<List<String>> a = new ArrayList<>();
        List<String> rili = new ArrayList<>();

        System.out.println(dayTotal + "dayTotaldayTotal");

        for (int i = dayTotal; i > -1; i--) {
            Integer twoDaysEarly = getTwoDaysEarly(settleDate, formatWhatDay(-i));
            Map<String, Object> everyDayMap = new HashMap<>();
            everyDayMap.put("date", formatWhatDay(-i));
            Map<String, Object> dayMap = new HashMap<>();
            if (twoDaysEarly > -1) {
                //日入库查询
                Map<String, Object> map = new HashMap<>();
                map.put("depGoodsId", depGoodsId);
                map.put("date", formatWhatDay(-i));
                Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);

                Double stockDouble = 0.0;
                List<GbDistributerGoodsEntity> stockGoodsEntities = new ArrayList<>();
                if (integer > 0) {
                    stockDouble = gbDepGoodsStockService.queryDepStockWeightTotal(map);
                    stockGoodsEntities = gbDepGoodsStockService.queryDisGoodsStockDetailByParams(map);
                }
                Map<String, Object> mapStock = new HashMap<>();
                mapStock.put("value", String.format("%.2f", stockDouble));
                mapStock.put("arr", stockGoodsEntities);
                dayMap.put("stock", mapStock);

                Double outStock = 0.0;
                if (gbDepartmentType == getGbDepartmentTypeKufang()) {
                    // 日出库存查询
                    Map<String, Object> map4 = new HashMap<>();
                    map4.put("depId", departmentEntity.getGbDepartmentId());
                    map4.put("date", formatWhatDay(-i));
                    map4.put("disGoodsId", gbDdgDisGoodsId);
                    System.out.println("4444444444444" + map4);
                    Integer integer4 = gbDepGoodsStockService.queryGoodsStockCount(map4);
                    List<GbDistributerGoodsEntity> stockGoodsEntities4 = new ArrayList<>();
                    if (integer4 > 0) {
                        outStock = gbDepGoodsStockService.queryDepStockWeightTotal(map4);
                        stockGoodsEntities4 = gbDepGoodsStockService.queryDisGoodsStockDetailByParams(map4);
                        System.out.println("outsotkckckck" + outStock);
                    }
                    Map<String, Object> mapStock4 = new HashMap<>();
                    mapStock4.put("value", String.format("%.2f", outStock));
                    mapStock4.put("arr", stockGoodsEntities4);
                    dayMap.put("outStock", mapStock4);
                }


                //日cost查询
                Double costDoutble = 0.0;
                List<GbDistributerGoodsEntity> costGoodsEntities = new ArrayList<>();

                Map<String, Object> map123 = new HashMap<>();
                map123.put("depGoodsId", depGoodsId);
                map123.put("date", formatWhatDay(-i));
                Integer count123 = gbDepartmentStockReduceService.queryReduceTypeCount(map123);
                if (count123 > 0) {
                    costDoutble = gbDepartmentStockReduceService.queryReduceCostWeightTotal(map123);
                    costGoodsEntities = gbDepartmentStockReduceService.queryReduceGoodsTypeByParams(map123);
                }
                Map<String, Object> mapCost = new HashMap<>();
                mapCost.put("value", String.format("%.2f", costDoutble));
                mapCost.put("arr", costGoodsEntities);
                dayMap.put("cost", mapCost);

                //日waste查询
                Double wasteDoutble = 0.0;
                List<GbDistributerGoodsEntity> wasteGoodsEntities = new ArrayList<>();

                Map<String, Object> map122 = new HashMap<>();
                map122.put("depGoodsId", depGoodsId);
                map122.put("date", formatWhatDay(-i));
                Integer count12 = gbDepartmentStockReduceService.queryReduceTypeCount(map122);
                if (count12 > 0) {
                    wasteDoutble = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(map122);
                    wasteGoodsEntities = gbDepartmentStockReduceService.queryReduceGoodsTypeByParams(map122);
                }
                Map<String, Object> mapWaste = new HashMap<>();
                mapWaste.put("value", String.format("%.2f", wasteDoutble));
                mapWaste.put("arr", wasteGoodsEntities);
                dayMap.put("waste", mapWaste);

                //日loss查询
                Double lossDoutble = 0.0;
                List<GbDistributerGoodsEntity> lossGoodsEntities = new ArrayList<>();

                Map<String, Object> map121 = new HashMap<>();
                map121.put("depGoodsId", depGoodsId);
                map121.put("date", formatWhatDay(-i));
                map121.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                Integer count1 = gbDepartmentStockReduceService.queryReduceTypeCount(map121);
                if (count1 > 0) {
                    lossDoutble = gbDepartmentStockReduceService.queryReduceLossWeightTotal(map121);
                    lossGoodsEntities = gbDepartmentStockReduceService.queryReduceGoodsTypeByParams(map121);
                }
                Map<String, Object> mapLoss = new HashMap<>();
                mapLoss.put("value", String.format("%.2f", lossDoutble));
                mapLoss.put("arr", lossGoodsEntities);
                dayMap.put("loss", mapLoss);
                everyDayMap.put("dateArr", dayMap);

                List<String> bbb = new ArrayList<>();
                bbb.add(formatWhatDay(-i));
                bbb.add(String.format("%.2f", stockDouble));
                if (gbDepartmentType.equals(getGbDepartmentTypeKufang())) {
                    bbb.add(String.format("%.2f", outStock));
                }
                bbb.add(String.format("%.2f", costDoutble));
                bbb.add(String.format("%.2f", wasteDoutble));
                bbb.add(String.format("%.2f", lossDoutble));
                a.add(bbb);

                list.add(everyDayMap);
            } else {
                everyDayMap.put("dateArr", -1);
                list.add(everyDayMap);
                List<String> bbb = new ArrayList<>();
                bbb.add(formatWhatDay(-i));
                a.add(bbb);
            }
            rili.add(formatWhatDay(-i));
        }


        Map<String, Object> map = new HashMap<>();
        map.put("rili2", a);
        map.put("rili", rili);
        map.put("shuju", list);
        map.put("settleDay", departmentEntity.getGbDepartmentSettleFullTime());
        map.put("settleDayTime", changeStringToTime(settleDate));
        map.put("startDay", settleFirstDay);
        map.put("dayNumber", dayTotal);
        map.put("stopDay", formatWhatDay(0));

        return R.ok().put("data", map);
    }


    @RequestMapping(value = "/getDepOutStockGoodsDetail", method = RequestMethod.POST)
    @ResponseBody
    public R getDepOutStockGoodsDetail(Integer toDepId, Integer disGoodsId, Integer isSelf, String startDate, String stopDate) {
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        if (howManyDaysInPeriod > 0) {
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                Map<String, Object> mapResult = new HashMap<>();
                mapResult.put("date", whichDay);
                mapResult.put("week", changeDateToWeek(whichDay));
                double total = 0;
                Map<String, Object> mapDisGoods = new HashMap<>();
                mapDisGoods.put("disGoodsId", disGoodsId);
                mapDisGoods.put("date", whichDay);
                mapDisGoods.put("fromDepId", toDepId);
                mapDisGoods.put("dayuStatus", -1);
//                mapDisGoods.put("isSelf", isSelf);
                mapDisGoods.put("depFatherIdNotEqual", toDepId);
                List<GbDepartmentGoodsStockEntity> stockEntities = new ArrayList<>();
                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);

                if (count > 0) {
                    stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(mapDisGoods);
                    total = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
                }

                mapResult.put("arr", stockEntities);
                mapResult.put("total", total);
                if (stockEntities.size() > 0) {
                    list.add(mapResult);
                }

            }
        } else {
            Map<String, Object> mapResult = new HashMap<>();
            mapResult.put("date", startDate);
            mapResult.put("week", changeDateToWeek(startDate));
            double total = 0;
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("disGoodsId", disGoodsId);
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("fromDepId", toDepId);
            mapDisGoods.put("dayuStatus", -1);
//            mapDisGoods.put("isSelf", isSelf);
            mapDisGoods.put("depFatherIdNotEqual", toDepId);
            List<GbDepartmentGoodsStockEntity> stockEntities = new ArrayList<>();
            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);

            if (count > 0) {
                stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(mapDisGoods);
                total = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
            }

            mapResult.put("arr", stockEntities);
            mapResult.put("total", total);
            if (stockEntities.size() > 0) {
                list.add(mapResult);
            }
        }


        return R.ok().put("data", list);
    }

    @RequestMapping(value = "/getDepOutStockGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getDepOutStockGoods(Integer toDepId, Integer disGoodsFatherId, String startDate, String stopDate,
                                 Integer mendianDepId) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> list = new ArrayList<>();
        Map<String, Object> mapTotal = new HashMap<>();

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
                mapDisGoods.put("disGoodsFatherId", disGoodsFatherId);
                mapDisGoods.put("date", whichDay);
                mapDisGoods.put("fromDepId", toDepId);
                mapDisGoods.put("dayuStatus", -1);
                mapDisGoods.put("depFatherIdNotEqual", toDepId);
                if (mendianDepId != -1) {
                    mapDisGoods.put("depId", mendianDepId);
                }
                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
                if (count > 0) {
                    Double aDouble = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
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
            mapDisGoods.put("disGoodsFatherId", disGoodsFatherId);
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("fromDepId", toDepId);
            mapDisGoods.put("dayuStatus", -1);
            if (mendianDepId != -1) {
                mapDisGoods.put("depId", mendianDepId);
            }
            mapDisGoods.put("depFatherIdNotEqual", toDepId);
            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
            if (count > 0) {
                Double aDouble = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
                list.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            } else {
                list.add("0");
            }
        }

        Map<String, Object> map0 = new HashMap<>();
        map0.put("disGoodsFatherId", disGoodsFatherId);
        map0.put("dayuStatus", -1);
        map0.put("fromDepId", toDepId);
        map0.put("depFatherIdNotEqual", toDepId);
        if (howManyDaysInPeriod > 0) {
            map0.put("startDate", startDate);
            map0.put("stopDate", stopDate);
        } else {
            map0.put("date", startDate);
        }
        if (mendianDepId != -1) {
            map0.put("depId", mendianDepId);
        }
        //1， stock 剩余金额总额
        Integer integer1 = gbDepGoodsStockService.queryGoodsStockCount(map0);
        if (integer1 > 0) {
            Double aDouble = gbDepGoodsStockService.queryDepStockWeightTotal(map0);
            System.out.println("dafafaaaaaaaaaaaaaaa" + map0);
            List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsStockService.queryDisGoodsWithFromDepGoods(map0);
//            List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map0);
            if (distributerGoodsEntities.size() > 0) {
                for (GbDistributerGoodsEntity goodsEntity : distributerGoodsEntities) {
                    map0.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                    Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
                    double stockTotal = 0;
                    if (integer > 0) {
                        stockTotal = gbDepGoodsStockService.queryDepStockWeightTotal(map0);
                    }
                    goodsEntity.setOutStockTotal(stockTotal);
                    goodsEntity.setOutStockTotalString(new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }

            mapTotal.put("stockTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapTotal.put("arr", distributerGoodsEntities);
            mapTotal.put("date", dateList);
            mapTotal.put("list", list);
        } else {
            return R.error(-1, "没有数据");
        }

        return R.ok().put("data", mapTotal);
    }


    @RequestMapping(value = "/getDepOutStockFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getDepOutStockFatherGoods(Integer depId, Integer mendianDepId, String startDate, String stopDate) {

        System.out.println("start" + startDate);
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> list = new ArrayList<>();
        if (howManyDaysInPeriod != 1) {
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
                mapDisGoods.put("fromDepId", depId);
                mapDisGoods.put("dayuStatus", -1);
                mapDisGoods.put("depFatherIdNotEqual", depId);
                if (mendianDepId != -1) {
                    mapDisGoods.put("depId", mendianDepId);
                }
                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
                if (count > 0) {
                    Double aDouble = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
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
            mapDisGoods.put("fromDepId", depId);
            mapDisGoods.put("dayuStatus", -1);
            if (mendianDepId != -1) {
                mapDisGoods.put("depId", mendianDepId);
            }
            mapDisGoods.put("depFatherIdNotEqual", depId);
            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
            if (count > 0) {
                Double aDouble = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
                list.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            } else {
                list.add("0");
            }
        }


        Map<String, Object> mapTotal = new HashMap<>();
        Map<String, Object> map0 = new HashMap<>();
        map0.put("fromDepId", depId);
        map0.put("dayuStatus", -1);
        if (mendianDepId != -1) {
            map0.put("depId", mendianDepId);
        }
        map0.put("depFatherIdNotEqual", depId);
        if (howManyDaysInPeriod > 0) {
            map0.put("startDate", startDate);
            map0.put("stopDate", stopDate);
        } else {
            map0.put("date", startDate);
        }
        //1， stock 剩余金额总额
        System.out.println("dafafa" + map0);
        Double aDouble = 0.0;
        List<GbDistributerFatherGoodsEntity> stockFatherGoodsTreeSet = new ArrayList<>();
        Integer integer1 = gbDepGoodsStockService.queryGoodsStockCount(map0);
        if (integer1 > 0) {
            aDouble = gbDepGoodsStockService.queryDepStockWeightTotal(map0);
            stockFatherGoodsTreeSet = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map0);
            if (stockFatherGoodsTreeSet.size() > 0) {
                for (GbDistributerFatherGoodsEntity greatGrandFather : stockFatherGoodsTreeSet) {
                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                        BigDecimal grandDouble = new BigDecimal(0);
                        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                        for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                            map0.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                            Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
                            double stockTotal = 0;
                            if (integer > 0) {
                                stockTotal = gbDepGoodsStockService.queryDepStockWeightTotal(map0);
                            }
                            father.setFatherOutStockTotal(stockTotal);
                            father.setFatherOutStockTotalString(new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            grandDouble = grandDouble.add(new BigDecimal(stockTotal));
                        }

//                        grandFather.setFatherGoodsEntities(fatherGoodsEntities);
                        grandFather.setFatherOutStockTotal(grandDouble.doubleValue());
                        grandFather.setFatherOutStockTotalString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                }
            }
        }
        mapTotal.put("stockTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapTotal.put("arr", stockFatherGoodsTreeSet);
        mapTotal.put("date", dateList);
        mapTotal.put("list", list);
        return R.ok().put("data", mapTotal);
    }


    @RequestMapping(value = "/getDepOutStockFatherGoodsSubtotal", method = RequestMethod.POST)
    @ResponseBody
    public R getDepOutStockFatherGoodsSubtotal(Integer depId, Integer mendianDepId, String startDate, String stopDate) {

        System.out.println("start" + startDate);
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> list = new ArrayList<>();
        if (howManyDaysInPeriod != 1) {
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
                mapDisGoods.put("fromDepId", depId);
                mapDisGoods.put("dayuStatus", -1);
                mapDisGoods.put("depFatherIdNotEqual", depId);
                if (mendianDepId != -1) {
                    mapDisGoods.put("depId", mendianDepId);
                }
                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
                if (count > 0) {
                    Double aDouble = gbDepGoodsStockService.queryDepStockSubtotal(mapDisGoods);
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
            mapDisGoods.put("fromDepId", depId);
            mapDisGoods.put("dayuStatus", -1);
            if (mendianDepId != -1) {
                mapDisGoods.put("depId", mendianDepId);
            }
            mapDisGoods.put("depFatherIdNotEqual", depId);
            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
            if (count > 0) {
                Double aDouble = gbDepGoodsStockService.queryDepStockSubtotal(mapDisGoods);
                list.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            } else {
                list.add("0");
            }
        }


        Map<String, Object> mapTotal = new HashMap<>();
        Map<String, Object> map0 = new HashMap<>();
        map0.put("fromDepId", depId);
        map0.put("dayuStatus", -1);
        if (mendianDepId != -1) {
            map0.put("depId", mendianDepId);
        }
        map0.put("depFatherIdNotEqual", depId);
        if (howManyDaysInPeriod > 0) {
            map0.put("startDate", startDate);
            map0.put("stopDate", stopDate);
        } else {
            map0.put("date", startDate);
        }
        //1， stock 剩余金额总额
        System.out.println("dafafa" + map0);
        Double aDouble = 0.0;
//        TreeSet<GbDistributerFatherGoodsEntity> stockFatherGoodsTreeSet = new TreeSet<>();

        List<GbDistributerFatherGoodsEntity> stockFatherGoodsTreeSet = new ArrayList<>();
        Integer integer1 = gbDepGoodsStockService.queryGoodsStockCount(map0);
        if (integer1 > 0) {
            aDouble = gbDepGoodsStockService.queryDepStockSubtotal(map0);
            stockFatherGoodsTreeSet = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map0);
            if (stockFatherGoodsTreeSet.size() > 0) {
                for (GbDistributerFatherGoodsEntity greatGrandFather : stockFatherGoodsTreeSet) {
                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                        BigDecimal grandDouble = new BigDecimal(0);
                        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                        for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                            map0.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                            Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
                            double stockTotal = 0;
                            if (integer > 0) {
                                stockTotal = gbDepGoodsStockService.queryDepStockSubtotal(map0);
                            }
                            father.setFatherOutStockTotal(stockTotal);
                            father.setFatherOutStockTotalString(new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            grandDouble = grandDouble.add(new BigDecimal(stockTotal));
                        }

//                        grandFather.setFatherGoodsEntities(fatherGoodsEntities);
                        grandFather.setFatherOutStockTotal(grandDouble.doubleValue());
                        grandFather.setFatherOutStockTotalString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                }
            }
        }
        mapTotal.put("stockTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapTotal.put("arr", stockFatherGoodsTreeSet);
        mapTotal.put("date", dateList);
        mapTotal.put("list", list);
        return R.ok().put("data", mapTotal);
    }


    @RequestMapping(value = "/getDepStockOutSubtotal", method = RequestMethod.POST)
    @ResponseBody
    public R getDepStockOutSubtotal(Integer depId, Integer disGoodsFatherId, String startDate, String stopDate,
                                    Integer mendianDepId) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> list = new ArrayList<>();
        Map<String, Object> mapTotal = new HashMap<>();

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
                mapDisGoods.put("disGoodsFatherId", disGoodsFatherId);
                mapDisGoods.put("date", whichDay);
                mapDisGoods.put("fromDepId", depId);
                mapDisGoods.put("dayuStatus", -1);

                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
                if (count > 0) {
                    Double aDouble = gbDepGoodsStockService.queryDepStockSubtotal(mapDisGoods);
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
            mapDisGoods.put("disGoodsFatherId", disGoodsFatherId);
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("fromDepId", depId);
            mapDisGoods.put("dayuStatus", -1);

            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
            if (count > 0) {
                Double aDouble = gbDepGoodsStockService.queryDepStockSubtotal(mapDisGoods);
                list.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            } else {
                list.add("0");
            }
        }

        Map<String, Object> map0 = new HashMap<>();
        map0.put("disGoodsFatherId", disGoodsFatherId);
        map0.put("dayuStatus", -1);
        map0.put("fromDepId", depId);
        if (howManyDaysInPeriod > 0) {
            map0.put("startDate", startDate);
            map0.put("stopDate", stopDate);
        } else {
            map0.put("date", startDate);
        }

        //1， stock 剩余金额总额
        Integer integer1 = gbDepGoodsStockService.queryGoodsStockCount(map0);
        if (integer1 > 0) {
            Double aDouble = gbDepGoodsStockService.queryDepStockSubtotal(map0);
            System.out.println("dafafaaaaaaaaaaaaaaa" + map0);
            List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsStockService.queryDisGoodsWithFromDepGoods(map0);
            if (distributerGoodsEntities.size() > 0) {
                for (GbDistributerGoodsEntity goodsEntity : distributerGoodsEntities) {
                    map0.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                    Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
                    double stockTotal = 0;
                    if (integer > 0) {
                        stockTotal = gbDepGoodsStockService.queryDepStockSubtotal(map0);
                    }
                    goodsEntity.setOutStockTotal(stockTotal);
                    goodsEntity.setOutStockTotalString(new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }

            mapTotal.put("stockTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapTotal.put("arr", distributerGoodsEntities);
            mapTotal.put("date", dateList);
            mapTotal.put("list", list);
        } else {
            return R.error(-1, "没有数据");
        }

        return R.ok().put("data", mapTotal);
    }


    @RequestMapping(value = "/getDepStockInSubtotal", method = RequestMethod.POST)
    @ResponseBody
    public R getDepStockInSubtotal(Integer depId, Integer disGoodsFatherId, String startDate, String stopDate,
                                   Integer mendianDepId) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> list = new ArrayList<>();
        Map<String, Object> mapTotal = new HashMap<>();

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
                mapDisGoods.put("disGoodsFatherId", disGoodsFatherId);
                mapDisGoods.put("date", whichDay);
                mapDisGoods.put("depFatherId", depId);
                mapDisGoods.put("dayuStatus", -1);
                mapDisGoods.put("stockId", -1);
                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
                if (count > 0) {
                    Double aDouble = gbDepGoodsStockService.queryDepStockSubtotal(mapDisGoods);
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
            mapDisGoods.put("disGoodsFatherId", disGoodsFatherId);
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("depId", depId);
            mapDisGoods.put("dayuStatus", -1);
            mapDisGoods.put("stockId", -1);

            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
            if (count > 0) {
                Double aDouble = gbDepGoodsStockService.queryDepStockSubtotal(mapDisGoods);
                list.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            } else {
                list.add("0");
            }
        }

        Map<String, Object> map0 = new HashMap<>();
        map0.put("disGoodsFatherId", disGoodsFatherId);
        map0.put("dayuStatus", -1);
        map0.put("depFatherId", depId);
        map0.put("stockId", -1);
        if (howManyDaysInPeriod > 0) {
            map0.put("startDate", startDate);
            map0.put("stopDate", stopDate);
        } else {
            map0.put("date", startDate);
        }

        //1， stock 剩余金额总额
        Integer integer1 = gbDepGoodsStockService.queryGoodsStockCount(map0);
        if (integer1 > 0) {
            Double aDouble = gbDepGoodsStockService.queryDepStockSubtotal(map0);
            System.out.println("dafafaaaaaaaaaaaaaaa" + map0);
            List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsStockService.queryDisGoodsWithFromDepGoods(map0);
            if (distributerGoodsEntities.size() > 0) {
                for (GbDistributerGoodsEntity goodsEntity : distributerGoodsEntities) {
                    map0.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                    Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
                    double stockTotal = 0;
                    if (integer > 0) {
                        stockTotal = gbDepGoodsStockService.queryDepStockSubtotal(map0);
                    }
                    goodsEntity.setOutStockTotal(stockTotal);
                    goodsEntity.setOutStockTotalString(new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }

            mapTotal.put("stockTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapTotal.put("arr", distributerGoodsEntities);
            mapTotal.put("date", dateList);
            mapTotal.put("list", list);
        } else {
            return R.error(-1, "没有数据");
        }

        return R.ok().put("data", mapTotal);
    }


    @RequestMapping(value = "/getDepStockFatherGoodsSubtotal", method = RequestMethod.POST)
    @ResponseBody
    public R getDepStockFatherGoodsSubtotal(Integer depId, Integer mendianDepId, String startDate, String stopDate) {

        System.out.println("start" + startDate);
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> list = new ArrayList<>();
        if (howManyDaysInPeriod != 1) {
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
                mapDisGoods.put("depFatherId", depId);
                mapDisGoods.put("dayuStatus", -1);
                mapDisGoods.put("stockId", -1);
                System.out.println("stockkdiididdiiddimap" + mapDisGoods);
                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
                if (count > 0) {
                    Double aDouble = gbDepGoodsStockService.queryDepStockSubtotal(mapDisGoods);
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
            mapDisGoods.put("depFatherId", depId);
            mapDisGoods.put("dayuStatus", -1);
            mapDisGoods.put("stockId", -1);
            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
            if (count > 0) {
                Double aDouble = gbDepGoodsStockService.queryDepStockSubtotal(mapDisGoods);
                list.add(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            } else {
                list.add("0");
            }
        }


        Map<String, Object> mapTotal = new HashMap<>();
        Map<String, Object> map0 = new HashMap<>();
        map0.put("depFatherId", depId);
        map0.put("dayuStatus", -1);
        map0.put("stockId", -1);
        if (howManyDaysInPeriod > 0) {
            map0.put("startDate", startDate);
            map0.put("stopDate", stopDate);
        } else {
            map0.put("date", startDate);
        }
        //1， stock 剩余金额总额
        System.out.println("dafafa" + map0);
        Double aDouble = 0.0;
        List<GbDistributerFatherGoodsEntity> stockFatherGoodsTreeSet = new ArrayList<>();
        Integer integer1 = gbDepGoodsStockService.queryGoodsStockCount(map0);

        if (integer1 > 0) {
            aDouble = gbDepGoodsStockService.queryDepStockSubtotal(map0);
            stockFatherGoodsTreeSet = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map0);
            if (stockFatherGoodsTreeSet.size() > 0) {
                for (GbDistributerFatherGoodsEntity greatGrandFather : stockFatherGoodsTreeSet) {
                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                        BigDecimal grandDouble = new BigDecimal(0);
                        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                        for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                            map0.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                            Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
                            double stockTotal = 0;
                            if (integer > 0) {
                                stockTotal = gbDepGoodsStockService.queryDepStockSubtotal(map0);
                            }
                            father.setFatherStockSubtotal(stockTotal);
                            father.setFatherStockSubtotalString(new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            grandDouble = grandDouble.add(new BigDecimal(stockTotal));
                        }

//                        grandFather.setFatherGoodsEntities(fatherGoodsEntities);
                        grandFather.setFatherStockSubtotal(grandDouble.doubleValue());
                        grandFather.setFatherStockSubtotalString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                }
            }
        }

        System.out.println("map1111111" + map0);

        Integer count122 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map0);

        Double wasteDouble = 0.0;
        Double lossDouble = 0.0;
        if (count122 > 0) {
            lossDouble = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map0);
            wasteDouble = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map0);

        }


        Double saleDouble = 0.0;

        Map<String, Object> map00 = new HashMap<>();
        map00.put("fromDepId", depId);
        map00.put("dayuStatus", -1);
        if (howManyDaysInPeriod > 0) {
            map00.put("startDate", startDate);
            map00.put("stopDate", stopDate);
        } else {
            map00.put("date", startDate);
        }
        Integer integerS = gbDepGoodsStockService.queryGoodsStockCount(map00);
        if (integerS > 0) {
            saleDouble = gbDepGoodsStockService.queryDepStockSubtotal(map00);
        }
        mapTotal.put("stockTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapTotal.put("arr", stockFatherGoodsTreeSet);
        mapTotal.put("date", dateList);
        mapTotal.put("list", list);
        mapTotal.put("outTotal", saleDouble);
        mapTotal.put("lossTotal", lossDouble);
        mapTotal.put("wasteTotal", wasteDouble);


        return R.ok().put("data", mapTotal);
    }


    @RequestMapping(value = "/getDepStockFatherGoods/{depId}")
    @ResponseBody
    public R getDepStockFatherGoods(@PathVariable Integer depId) {
        Map<String, Object> mapTotal = new HashMap<>();
//        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoods = new TreeSet<>();
        List<GbDistributerFatherGoodsEntity> greatGrandGoods = new ArrayList<>();
        Map<String, Object> map0 = new HashMap<>();
        map0.put("depId", depId);
        map0.put("dayuStatus", -1);
        //1， stock 剩余金额总额
        System.out.println("dafafa" + map0);
        double aDouble = 0.0;
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
        if (integer > 0) {
            aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map0);
            greatGrandGoods = getStockGoodsFatherRestSubTotal(map0, aDouble);
        }
        mapTotal.put("stockTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapTotal.put("arr", greatGrandGoods);
        return R.ok().put("data", mapTotal);
    }


    /**
     * @param disId    用户id
     * @param showType
     * @return Map
     */
    @RequestMapping(value = "/getMendianStockTypePeriod", method = RequestMethod.POST)
    @ResponseBody
    public R getMendianStockTypePeriod(Integer disId, Integer showType, Integer searchDepId) {
        Map<String, Object> mapResult = new HashMap<>();

        //按商品显示
        List<GbDistributerFatherGoodsEntity> greatGrandGoods = new ArrayList<>();
        Map<String, Object> map0 = new HashMap<>();
        map0.put("disId", disId);
        map0.put("dayuStatus", -1);
//        if (searchDepId != -1) {
//            map0.put("depId", searchDepId);
//        }
        map0.put("restWeight", 0);
        map0.put("depType", getGbDepartmentTypeMendian());

        //1， stock 剩余金额总额
        System.out.println("mapapappa" + map0);
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
        if (integer > 0) {
            Map<String, Object> mapTotal = new HashMap<>();
            Double aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map0);
            greatGrandGoods = getStockGoodsFatherRestSubTotal(map0, aDouble);
            mapTotal.put("arr", greatGrandGoods);
//            if (showType == 2) {
//                departmentEntities = getStockGoodsFatherRestSubTotalByDep(map0);
//                mapTotal.put("arr", departmentEntities);
//            }
            mapTotal.put("restTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("total", mapTotal);
            mapResult.put("exceed", queryExceedData(showType, disId, searchDepId));
            mapResult.put("three", queryThreePeriodData(showType, disId, searchDepId));
            mapResult.put("two", queryTwoPeriodData(showType, disId, searchDepId));
            mapResult.put("one", queryOnePeriodData(showType, disId, searchDepId));
            mapResult.put("in", queryInPeriodData(showType, disId, searchDepId));

        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("restTotal", 0);
            mapResult.put("total", map);
        }
        return R.ok().put("data", mapResult);
    }

    /**
     * @param disId 用户id
     * @param
     * @return Map
     */
    @RequestMapping(value = "/getMendianStockTypePeriodWithDepIds", method = RequestMethod.POST)
    @ResponseBody
    public R getMendianStockTypePeriodWithDepIds(Integer disId, Integer whichDay, String searchDepIds) {
        Map<String, Object> mapResult = new HashMap<>();


        //按商品显示
        List<GbDistributerFatherGoodsEntity> greatGrandGoods = new ArrayList<>();
        List<GbDepartmentEntity> departmentEntities = new ArrayList<>();
        Map<String, Object> map0 = new HashMap<>();
        map0.put("disId", disId);
        map0.put("dayuStatus", -1);
        List<String> idsGb = new ArrayList<>();
        if(!searchDepIds.equals("-1")){
            String[] arrGb = searchDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map0.put("depFatherIds", idsGb);
                }
            }
        }

        if (whichDay != 99) {
            if (whichDay == -4) {
                map0.put("stopDate", formatWhatDay(-4));
            } else {
                map0.put("date", formatWhatDay(whichDay));
            }
        }
        //1， stock 剩余金额总额
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
        if (integer > 0) {
            Map<String, Object> mapTotal = new HashMap<>();
            Double aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map0);

            System.out.println("mapapappaWithDeps00000" + whichDay+"??????????"+map0);
            Integer integerWhich = gbDepGoodsStockService.queryGoodsStockCount(map0);
            Double whichDouble = 0.0;
            if(integerWhich > 0){
                whichDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map0);
                departmentEntities = getStockGoodsFatherRestSubTotalByDepWithDepIds(idsGb,map0, whichDouble);
                Map<String, Object> mapAll = new HashMap<>();
                mapAll.put("disId", disId);
                mapAll.put("dayuStatus", -1);
//                mapAll.put("restWeight", 0);
                mapAll.put("depFatherIds", idsGb);
                if (whichDay != 99) {
                    if (whichDay == -4) {
                        mapAll.put("stopDate", formatWhatDay(-4));
                    } else {
                        mapAll.put("date", formatWhatDay(whichDay));
                    }
                }
                greatGrandGoods = getStockGoodsFatherRestSubTotal(mapAll, whichDouble);
            }

            mapTotal.put("arr", greatGrandGoods);
            mapTotal.put("restTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapTotal.put("whichTotal", new BigDecimal(whichDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("total", mapTotal);
            mapResult.put("depArr", departmentEntities);

            Map<String, Object> mapRen4 = new HashMap<>();
            mapRen4.put("disId", disId);
            mapRen4.put("depFatherIds", idsGb);
            mapRen4.put("dayuStatus", -1);
            mapRen4.put("restWeight", 0);

            //exceedThreeTotal
            mapRen4.put("stopDate", formatWhatDay(-4));
            Double exceedThreeTotal = 0.0;
            Integer integer33 = gbDepGoodsStockService.queryGoodsStockCount(mapRen4);
            if (integer33 > 0) {
                exceedThreeTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(mapRen4);
            }
            mapResult.put("exceedThreeTotal", new BigDecimal(exceedThreeTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            Map<String, Object> mapRen5 = new HashMap<>();
            mapRen5.put("disId", disId);
            mapRen5.put("depFatherIds", idsGb);
            mapRen5.put("dayuStatus", -1);
            mapRen5.put("restWeight", 0);

            //exceedThreeTotal
            mapRen5.put("date", formatWhatDay(-3));
            Double threeTotal = 0.0;
            Integer integer3 = gbDepGoodsStockService.queryGoodsStockCount(mapRen5);
            if (integer3 > 0) {
                threeTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(mapRen5);
            }
            mapResult.put("threeTotal", new BigDecimal(threeTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            System.out.println("maprenenenennenene" + mapRen4);
            //twoTotal
            mapRen5.put("date", formatWhatDay(-2));
            Double twoTotal = 0.0;
            Integer integer2 = gbDepGoodsStockService.queryGoodsStockCount(mapRen5);
            if (integer2 > 0) {
                twoTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(mapRen5);
            }
            mapResult.put("twoTotal", new BigDecimal(twoTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            //oneTotal
            mapRen5.put("date", formatWhatDay(-1));
            Double oneTotal = 0.0;
            Integer integer1 = gbDepGoodsStockService.queryGoodsStockCount(mapRen5);
            if (integer1 > 0) {
                oneTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(mapRen5);
            }
            mapResult.put("oneTotal", new BigDecimal(oneTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            //inTotal
            mapRen5.put("date", formatWhatDay(0));
            Double zeroTotal = 0.0;
            Integer integerzero = gbDepGoodsStockService.queryGoodsStockCount(mapRen5);
            if (integerzero > 0) {
                zeroTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(mapRen5);
            }
            mapResult.put("zeroTotal", new BigDecimal(zeroTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("restTotal", 0);
            mapResult.put("total", map);
        }
        return R.ok().put("data", mapResult);
    }

    private Map<String, Object> queryInPeriodData(Integer showType, Integer disId, Integer searchDepId) {
        Map<String, Object> mapResult = new HashMap<>();
        String zeroDateString = "";
        Map<String, Object> mapRenZero = new HashMap<>();
        mapRenZero.put("disId", disId);
        mapRenZero.put("dayuStatus", -1);
        mapRenZero.put("date", formatWhatDay(0));
        mapRenZero.put("depType", getGbDepartmentTypeMendian());
        zeroDateString = formatWhatDayString(0);
        if (searchDepId != -1) {
            mapRenZero.put("depId", searchDepId);
        }
        mapRenZero.put("restWeight", 0);

        Double zeroTotal = 0.0;
        Integer integerzero = gbDepGoodsStockService.queryGoodsStockCount(mapRenZero);
        if (integerzero > 0) {
            zeroTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(mapRenZero);
            mapResult.put("zeroTotal", new BigDecimal(zeroTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//            if (showType == 1) {
                List<GbDistributerFatherGoodsEntity> recentlyStockZero = getStockGoodsFatherRestSubTotal(mapRenZero, zeroTotal);
                mapResult.put("zeroArr", recentlyStockZero);
//            }
//            if (showType == 2) {
//                List<GbDepartmentEntity> recentlyStockZero = getStockGoodsFatherRestSubTotalByDep(mapRenZero, zeroTotal);
//                mapResult.put("zeroArr", recentlyStockZero);
//            }
            mapResult.put("zeroDateString", zeroDateString);
        } else {
            mapResult.put("zeroTotal", "0.0");
            mapResult.put("zeroArr", new ArrayList<>());
        }
        return mapResult;
    }


    private Map<String, Object> queryOnePeriodData(Integer showType, Integer disId, Integer searchDepId) {
        Map<String, Object> mapResult = new HashMap<>();
        String oneDateString = "";

        Map<String, Object> mapRen1 = new HashMap<>();
        mapRen1.put("disId", disId);
        mapRen1.put("dayuStatus", -1);
        mapRen1.put("depType", getGbDepartmentTypeMendian());
        mapRen1.put("date", formatWhatDay(-1));
        if (searchDepId != -1) {
            mapRen1.put("depId", searchDepId);
        }
        mapRen1.put("restWeight", 0);

        Double oneTotal = 0.0;
        Integer integer1 = gbDepGoodsStockService.queryGoodsStockCount(mapRen1);
        if (integer1 > 0) {
            oneTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(mapRen1);
            mapResult.put("oneTotal", new BigDecimal(oneTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//            if (showType == 1) {
                List<GbDistributerFatherGoodsEntity> recentlyStockOne = getStockGoodsFatherRestSubTotal(mapRen1, oneTotal);
                mapResult.put("oneArr", recentlyStockOne);
//            }
//            if (showType == 2) {
//                List<GbDepartmentEntity> recentlyStockZero = getStockGoodsFatherRestSubTotalByDep(mapRen1, oneTotal);
//                mapResult.put("oneArr", recentlyStockZero);
//            }

            mapResult.put("oneDateString", oneDateString);
        } else {
            mapResult.put("oneTotal", "0.0");
            mapResult.put("oneArr", new ArrayList<>());
        }

        return mapResult;

    }


    private Map<String, Object> queryTwoPeriodData(Integer showType, Integer disId, Integer searchDepId) {
        Map<String, Object> mapResult = new HashMap<>();
        String twoDateString = "";
        Map<String, Object> mapRen2 = new HashMap<>();
        mapRen2.put("disId", disId);
        mapRen2.put("dayuStatus", -1);
        mapRen2.put("date", formatWhatDay(-2));
        if (searchDepId != -1) {
            mapRen2.put("depId", searchDepId);
        }
        mapRen2.put("restWeight", 0);

        Double twoTotal = 0.0;
        Integer integer2 = gbDepGoodsStockService.queryGoodsStockCount(mapRen2);
        if (integer2 > 0) {
            twoTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(mapRen2);
            mapResult.put("twoTotal", new BigDecimal(twoTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//            if (showType == 1) {
                List<GbDistributerFatherGoodsEntity> recentlyStockTwo = getStockGoodsFatherRestSubTotal(mapRen2, twoTotal);
                mapResult.put("twoArr", recentlyStockTwo);
//            }
//            if (showType == 2) {
//                List<GbDepartmentEntity> recentlyStockZero = getStockGoodsFatherRestSubTotalByDep(mapRen2, twoTotal);
//                mapResult.put("twoArr", recentlyStockZero);
//            }
            mapResult.put("twoDateString", twoDateString);
        } else {
            mapResult.put("twoTotal", "0.0");
            mapResult.put("twoArr", new ArrayList<>());
        }


        return mapResult;

    }


    private Map<String, Object> queryThreePeriodData(Integer showType, Integer disId, Integer searchDepId) {
        Map<String, Object> mapResult = new HashMap<>();
        String threeDateString = "";

        //2,大于=2小于=3
        Map<String, Object> mapRen3 = new HashMap<>();
        mapRen3.put("disId", disId);
        mapRen3.put("dayuStatus", -1);
        if (searchDepId != -1) {
            mapRen3.put("depId", searchDepId);
        }
        mapRen3.put("restWeight", 0);
        mapRen3.put("depType", getGbDepartmentTypeMendian());
        mapRen3.put("date", formatWhatDay(-3));
        threeDateString = formatWhatDayString(-3);
        Double threeTotal = 0.0;
        Integer integer3 = gbDepGoodsStockService.queryGoodsStockCount(mapRen3);
        if (integer3 > 0) {
            threeTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(mapRen3);
            mapResult.put("threeTotal", new BigDecimal(threeTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

//            if (showType == 1) {

                List<GbDistributerFatherGoodsEntity> recentlyStockThree = getStockGoodsFatherRestSubTotal(mapRen3, threeTotal);
                mapResult.put("threeArr", recentlyStockThree);
//            }
//            if (showType == 2) {
//                List<GbDepartmentEntity> recentlyStockZero = getStockGoodsFatherRestSubTotalByDep(mapRen3, threeTotal);
//                mapResult.put("threeArr", recentlyStockZero);
//            }


            mapResult.put("threeDateString", threeDateString);
        } else {
            mapResult.put("threeTotal", "0.0");
            mapResult.put("threeArr", new ArrayList<>());
        }

        return mapResult;

    }

    private Map<String, Object> queryExceedData(Integer showType, Integer disId, Integer searchDepId) {
        Map<String, Object> mapResult = new HashMap<>();
        String exceedDateString = "";
        //1,超过3的入库数据
        Map<String, Object> mapRen4 = new HashMap<>();
        mapRen4.put("disId", disId);
        if (searchDepId != -1) {
            mapRen4.put("depId", searchDepId);
        }
        mapRen4.put("dayuStatus", -1);
        mapRen4.put("stopDate", formatWhatDay(-4));
        mapRen4.put("restWeight", 0);
        mapRen4.put("depType", getGbDepartmentTypeMendian());
        exceedDateString = formatWhatDayString(-4) + "以前";
        Double exceedThreeTotal = 0.0;
        Integer integer33 = gbDepGoodsStockService.queryGoodsStockCount(mapRen4);
        if (integer33 > 0) {
            exceedThreeTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(mapRen4);
            mapResult.put("exceedThreeTotal", new BigDecimal(exceedThreeTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

//            if (showType == 1) {
                List<GbDistributerFatherGoodsEntity> recentlyStockDayuThree = getStockGoodsFatherRestSubTotal(mapRen4, exceedThreeTotal);
                mapResult.put("exceedThreeArr", recentlyStockDayuThree);

//            }
//            if (showType == 2) {
//                List<GbDepartmentEntity> recentlyStockZero = getStockGoodsFatherRestSubTotalByDep(mapRen4, exceedThreeTotal);
//                mapResult.put("exceedThreeArr", recentlyStockZero);
//            }


            mapResult.put("exceedDateString", exceedDateString);
        } else {
            mapResult.put("exceedThreeTotal", "0.0");
            mapResult.put("exceedThreeArr", new ArrayList<>());
        }

        return mapResult;

    }


    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherGoodsStockEvery(TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {
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


    private TreeSet<GbDistributerGoodsEntity> abcGoodsPrice(TreeSet<GbDistributerGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;

                if (o2.getGoodsAveragePrice() - o1.getGoodsAveragePrice() < 0) {
                    result = -1;
                } else if (o2.getGoodsAveragePrice() - o1.getGoodsAveragePrice() > 0) {
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


    private TreeSet<GbDistributerGoodsEntity> abcGoodsPriceTotal(TreeSet<GbDistributerGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;

                if (o2.getGoodsPriceTotal() - o1.getGoodsPriceTotal() < 0) {
                    result = 1;
                } else if (o2.getGoodsPriceTotal() - o1.getGoodsPriceTotal() > 0) {
                    result = -1;
                } else {
                    result = -1;
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }

    private TreeSet<GbDistributerGoodsEntity> abcGoodsPriceTotalHigh(TreeSet<GbDistributerGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;

                if (o2.getGoodsPriceTotal() - o1.getGoodsPriceTotal() < 0) {
                    result = 1;
                } else if (o2.getGoodsPriceTotal() - o1.getGoodsPriceTotal() > 0) {
                    result = -1;
                } else {
                    result = -1;
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }

    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherGoodsStockSubtotal(TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;

                if (o2.getFatherStockSubtotal() - o1.getFatherStockSubtotal() < 0) {
                    result = -1;
                } else if (o2.getFatherStockSubtotal() - o1.getFatherStockSubtotal() > 0) {
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

    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherGoodsOutWeight(TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;

                if (o2.getFatherOutStockTotal() - o1.getFatherOutStockTotal() < 0) {
                    result = -1;
                } else if (o2.getFatherOutStockTotal() - o1.getFatherOutStockTotal() > 0) {
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

    @RequestMapping(value = "/getTypeRate", method = RequestMethod.POST)
    @ResponseBody
    public R getTypeRate(String startDate, String stopDate, String type, Integer disId) {
        Map<String, Object> fatherGoodsEntities = new HashMap<>();
//        if (type.equals("cost")) {
//            fatherGoodsEntities = getCostType(startDate, stopDate, disId);
//        }
        if (type.equals("sales")) {
            fatherGoodsEntities = getSalesType(startDate, stopDate, disId);
        }
        if (type.equals("loss")) {
            fatherGoodsEntities = getLossType(startDate, stopDate, disId);
        }
        if (type.equals("waste")) {
            fatherGoodsEntities = getWasteType(startDate, stopDate, disId);
        }
        if (fatherGoodsEntities.get("code").equals("0")) {
            return R.ok().put("data", fatherGoodsEntities);

        } else {
            return R.error(-1, "没有数据");
        }

    }


    @RequestMapping(value = "/getTypeRateGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getTypeRateGoods(String startDate, String stopDate, String type, Integer goodsFatherId, Integer disId, Integer depId) {
        Map<String, Object> map = new HashMap<>();
        if (type.equals("sales")) {
            map = getSalesTypeGoods(startDate, stopDate, goodsFatherId, disId, depId);
        }
        if (type.equals("loss")) {
            map = getLossTypeGoods(startDate, stopDate, goodsFatherId, disId, depId);
        }
        if (type.equals("waste")) {
            map = getWasteTypeGoods(startDate, stopDate, goodsFatherId, disId, depId);
        }

        if (type.equals("freshRate")) {
            map = getFreshRateGoods(startDate, stopDate, goodsFatherId, depId);
        }
        if (type.equals("finishRate")) {
            map = getFinishRateGoods(startDate, stopDate, goodsFatherId, depId);
        }

        if (map.get("code").equals("0")) {
            return R.ok().put("data", map);
        } else {
            return R.error(-1, "没有数据");
        }
    }


    @RequestMapping(value = "/getTypeRateGoodsDep", method = RequestMethod.POST)
    @ResponseBody
    public R getTypeRateGoodsDep(String startDate, String stopDate, String type, Integer disGoodsId,
                                 Integer disId, Integer depId) {
        Map<String, Object> map = new HashMap<>();
        if (type.equals("sales")) {
            map = getSalesTypeGoodsDep(startDate, stopDate, disGoodsId, disId, depId);
        }
        if (type.equals("loss")) {
            map = getLossTypeGoodsDep(startDate, stopDate, disGoodsId, disId, depId);
        }
        if (type.equals("waste")) {
            map = getWasteTypeGoodsDep(startDate, stopDate, disGoodsId, disId, depId);
        }
        if (type.equals("freshRate")) {
            map = getFreshRateGoodsDep(startDate, stopDate, disGoodsId, disId, depId);
        }
        if (type.equals("finishRate")) {
            map = getFinishRateGoodsDep(startDate, stopDate, disGoodsId, disId, depId);
        }

        System.out.println("mapcodeeelele" + map.get("code"));
        if (map.get("code").equals("0")) {
            return R.ok().put("data", map);
        } else {
            return R.error(-1, "没有数据");
        }

    }


    private Map<String, Object> getFinishRateGoods(String startDate, String stopDate, Integer goodsFatherId, Integer depId) {
        Map<String, Object> resultMap = new HashMap<>();
        //echarts
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> costList = new ArrayList<>();

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
                mapDisGoods.put("disGoodsFatherId", goodsFatherId);
                mapDisGoods.put("clear", -1);
                if (depId != -1) {
                    mapDisGoods.put("depId", depId);
                }
                Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
                if (count > 0) {
                    int hourTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(mapDisGoods);
                    int minuteTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(mapDisGoods);
                    int hourMinute = hourTotal * 60;
                    int totalMinute = (hourMinute + minuteTotal) / count;
                    costList.add(new BigDecimal(totalMinute).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    costList.add("0");
                }
            }
        } else {
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            // disGoods
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("disGoodsFatherId", goodsFatherId);
            mapDisGoods.put("clear", -1);
            if (depId != -1) {
                mapDisGoods.put("depId", depId);
            }
            Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
            if (count > 0) {
                int hourTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(mapDisGoods);
                int minuteTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(mapDisGoods);
                int hourMinute = hourTotal * 60;
                int totalMinute = (hourMinute + minuteTotal) / count;
                costList.add(new BigDecimal(totalMinute).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            } else {
                costList.add("0");
            }
        }


        // arr
        TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = new TreeSet<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsFatherId", goodsFatherId);
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }
        map.put("clear", -1);
        if (depId != -1) {
            map.put("depId", depId);
        }
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        List<GbDistributerGoodsEntity> resultList = new ArrayList<>();
        if (integer > 0) {
            distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);
            for (GbDistributerGoodsEntity distributerGoodsEntity : distributerGoodsEntities) {
                Map<String, Object> mapDis = new HashMap<>();
                mapDis.put("startDate", startDate);
                mapDis.put("stopDate", stopDate);
                mapDis.put("disGoodsId", distributerGoodsEntity.getGbDistributerGoodsId());
                mapDis.put("clear", -1);
                if (depId != -1) {
                    mapDis.put("depId", depId);
                }
                Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDis);
                int hour = 0;
                int min = 0;
                String minString = "0";
                if (integer1 > 0) {
                    int clearHour = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(mapDis);
                    int clearMinute = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(mapDis);
                    int hourM = clearHour * 60;
                    int totalMinute = (hourM + clearMinute) / integer1;
                    hour = totalMinute / 60;
                    min = totalMinute % 60;

                    if (min < 10) {
                        minString = "0" + min;
                    } else {
                        minString = Integer.toString(min);
                    }
                    String time = hour + ":" + minString;

                    System.out.println("vvvvvvvFresh" + time);
                    distributerGoodsEntity.setGoodsClearTimeString(time);
                    resultList.add(distributerGoodsEntity);
                }
            }
        } else {
            return R.error(-1, "aa");
        }

        resultMap.put("date", dateList);
        resultMap.put("list", costList);
        resultMap.put("arr", resultList);
        resultMap.put("code", "0");
        return resultMap;
    }

    private Map<String, Object> getFreshRateGoods(String startDate, String stopDate, Integer goodsFatherId, Integer depId) {
        Map<String, Object> resultMap = new HashMap<>();

        //echarts
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> costList = new ArrayList<>();

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
                mapDisGoods.put("disGoodsFatherId", goodsFatherId);
                mapDisGoods.put("produce", 0);
                mapDisGoods.put("controlFresh", 1);
                if (depId != -1) {
                    mapDisGoods.put("depId", depId);
                }
                Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
                double Tv = 0;

                if (count > 0) {
                    double v = gbDepGoodsDailyService.queryDepGoodsFreshRate(mapDisGoods);
                    Tv = v / count;
                    costList.add(new BigDecimal(Tv).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    costList.add("0");
                }
            }
        } else {
            // dateList
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            // disGoods
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("disGoodsFatherId", goodsFatherId);
            mapDisGoods.put("produce", 0);
            mapDisGoods.put("controlFresh", 1);
            if (depId != -1) {
                mapDisGoods.put("depId", depId);
            }
            System.out.println("coutnmappdpdid" + mapDisGoods);
            Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
            double Tv = 0;

            if (count > 0) {
                double v = gbDepGoodsDailyService.queryDepGoodsFreshRate(mapDisGoods);
                Tv = v / count;
                costList.add(new BigDecimal(Tv).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            } else {
                costList.add("0");
            }
        }

        // arr
        TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = new TreeSet<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsFatherId", goodsFatherId);
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }
        map.put("produce", 0);
        map.put("controlFresh", 1);
        if (depId != -1) {
            map.put("depId", depId);
        }
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        if (integer > 0) {
            distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);
            for (GbDistributerGoodsEntity distributerGoodsEntity : distributerGoodsEntities) {
                Map<String, Object> mapDis = new HashMap<>();
                if (howManyDaysInPeriod > 0) {
                    mapDis.put("startDate", startDate);
                    mapDis.put("stopDate", stopDate);
                } else {
                    mapDis.put("date", startDate);
                }

                mapDis.put("disGoodsId", distributerGoodsEntity.getGbDistributerGoodsId());
                mapDis.put("produce", 0);
                mapDis.put("controlFresh", 1);
                if (depId != -1) {
                    mapDis.put("depId", depId);
                }
                Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDis);
                double Tv = 0;
                if (integer1 > 0) {
                    double v = gbDepGoodsDailyService.queryDepGoodsFreshRate(mapDis);
                    Tv = v / integer1;
                }
                distributerGoodsEntity.setGoodsFreshRate(Tv);
                distributerGoodsEntity.setGoodsFreshRateString(new BigDecimal(Tv).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        } else {
            return R.error(-1, "dd");
        }

        resultMap.put("date", dateList);
        resultMap.put("list", costList);
        resultMap.put("arr", distributerGoodsEntities);
        resultMap.put("code", "0");
        return resultMap;
    }



    private List<GbDepartmentEntity> getStockGoodsFatherRestSubTotalByDepWithDepIds(List<String> idsGb,Map<String, Object> map0, Double aDouble) {

        List<GbDepartmentEntity> departmentEntitiesAll = new ArrayList<>();
        for(String id: idsGb){
            GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(id));
            map0.put("depFatherIds", null);
            map0.put("disGoodsFatherId", null);
            map0.put("depFatherId", departmentEntity.getGbDepartmentId());
            System.out.println("depanannnanan" + departmentEntity.getGbDepartmentName());
            System.out.println("depanannnanan" + map0);
            Integer integerStock = gbDepGoodsStockService.queryGoodsStockCount(map0);
            if (integerStock > 0) {
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepGoodsStockService.queryDepStockDisFatherGoodsFather(map0);
                System.out.println("allllGreatGransizeeeee" + fatherGoodsEntities.size());
                departmentEntity.setFatherGoodsEntities(fatherGoodsEntities);
                departmentEntitiesAll.add(getStockFatherGoodsRestSubtotalByDep(departmentEntity, map0, aDouble));

            } else {
                departmentEntity.setDepStockSubtotalString("0");
                departmentEntitiesAll.add(departmentEntity);
            }
        }
        System.out.println("foirriiiriirnenennenenenenemme" + departmentEntitiesAll.get(0).getGbDepartmentName());
        return departmentEntitiesAll;
    }


//    private List<GbDepartmentEntity> getStockGoodsFatherRestSubTotalByDep(Map<String, Object> map0, Double aDouble) {
//        System.out.println("whaiiiaiamap0map0map0map0deppedepeppe" + map0);
//        map0.put("restWeight", 0);
//        List<GbDepartmentEntity> departmentEntitiesS = getStockFatherGoodsTreeSetByDep(map0);
//        List<GbDepartmentEntity> departmentEntities = getStockFatherGoodsRestSubtotalByDep(departmentEntity, map0, aDouble);
//        return departmentEntities;
//    }

    private List<GbDistributerFatherGoodsEntity> getStockGoodsFatherRestSubTotal(Map<String, Object> map0, Double total) {
        System.out.println("whaiiiaiamap0map0map0map0Alllllllllll" + map0);
        map0.put("restWeight", 0);
        List<GbDistributerFatherGoodsEntity> stockAndRecordFatherGoodsTreeSet = getStockFatherGoodsTreeSet(map0);
        List<GbDistributerFatherGoodsEntity> stockFatherGoodsRestSubtotal = getStockFatherGoodsRestSubtotal(stockAndRecordFatherGoodsTreeSet, map0, total);
        return stockFatherGoodsRestSubtotal;
    }


    private List<GbDistributerFatherGoodsEntity> getStockFatherGoodsTreeSet(Map<String, Object> map0) {
        System.out.println("getStockFatherGoodsTreeSetAlllllll" + map0);
//        map0.put("restWeight", 0);
        Integer integerStock = gbDepGoodsStockService.queryGoodsStockCount(map0);
        if (integerStock > 0) {
            List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepGoodsStockService.queryDepStockDisFatherGoodsFather(map0);
            System.out.println("allllGreatGransizeeeee" + fatherGoodsEntities.size());
            return fatherGoodsEntities;
        } else {
            return new ArrayList<>();
        }

    }


    private List<GbDepartmentEntity> getStockFatherGoodsTreeSetByDep(Map<String, Object> map0) {
        List<GbDepartmentEntity> stockGoodsEntities = new ArrayList<>();
        System.out.println("getStockFatherGoodsTreeSet===ByDep" + map0);
//        map0.put("restWeight", 0);
        Integer integerStock = gbDepGoodsStockService.queryGoodsStockCount(map0);
        if (integerStock > 0) {
            List<GbDepartmentEntity> departmentEntities = gbDepGoodsStockService.queryStockDepWithFatherGoods(map0);
            stockGoodsEntities.addAll(departmentEntities);
        }
        return stockGoodsEntities;
    }


    private GbDepartmentEntity getStockFatherGoodsRestSubtotalByDep(GbDepartmentEntity departmentEntity, Map<String, Object> map0, Double total) {
            BigDecimal depTotal = new BigDecimal(0);
            List<GbDistributerFatherGoodsEntity> greatGrandGoods = departmentEntity.getFatherGoodsEntities();
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoods) {
                System.out.println("fathernanmamemememmeme" + greatGrandFather.getGbDfgFatherGoodsName());
                BigDecimal greatGrandTotal = new BigDecimal(0);
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    System.out.println("fathernanmamemememmeme" + grandFather.getGbDfgFatherGoodsName());

                    BigDecimal grandDouble = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        System.out.println("fathernanmamemememmeme" + father.getGbDfgFatherGoodsName());

                        double add = 0.0;
                        map0.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        map0.put("depFatherId", departmentEntity.getGbDepartmentId());
//                        map0.put("restWeight", 0);
                        map0.put("depFatherIds", null);
                        System.out.println("map0000GGGGGGGG" + map0);
                        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
                        if (integer > 0) {
                            Double stockTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(map0);
                            add = add + stockTotal;
                        }
                        father.setFatherStockTotal(add);
                        father.setFatherStockTotalString(new BigDecimal(add).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        grandDouble = grandDouble.add(new BigDecimal(add));
                        greatGrandTotal = greatGrandTotal.add(new BigDecimal(add));
                        depTotal = depTotal.add(new BigDecimal(add));

                    }
                    grandFather.setFatherStockTotal(grandDouble.doubleValue());
                    grandFather.setFatherStockTotalString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }

                BigDecimal divide = greatGrandTotal.divide(new BigDecimal(total), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                greatGrandFather.setFatherStockTotalPercent(divide.toString());
                greatGrandFather.setFatherStockTotal(greatGrandTotal.doubleValue());
                greatGrandFather.setFatherStockTotalString(greatGrandTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                System.out.println("aabdbbdbdbdbdbbdbdbd" + greatGrandTotal);
                System.out.println("aabdbbdbdbdbdbbdbdbd" + depTotal);



            }
        System.out.println("depname" + departmentEntity.getGbDepartmentName() + "==" + depTotal);
            departmentEntity.setDepStockSubtotal(depTotal.doubleValue());
            departmentEntity.setDepStockSubtotalString(depTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

        return departmentEntity;
    }


    private List<GbDistributerFatherGoodsEntity> getStockFatherGoodsRestSubtotal
            (List<GbDistributerFatherGoodsEntity> treeSet, Map<String, Object> map0, Double total) {
//        List<GbDistributerFatherGoodsEntity> grandTree = new ArrayList<>();

        for (GbDistributerFatherGoodsEntity greatGrandFather : treeSet) {
            System.out.println("zheliebainalalelele" + greatGrandFather.getGbDfgFatherGoodsName());
            BigDecimal greatGrandTotal = new BigDecimal(0);
            List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
            for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                BigDecimal grandDouble = new BigDecimal(0);
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                    map0.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                    double add = 0.0;
                    Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
                    if (integer > 0) {
                        Double stockTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(map0);
                        add = add + stockTotal;
                    }
                    father.setFatherStockTotal(add);
                    father.setFatherStockTotalString(new BigDecimal(add).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    grandDouble = grandDouble.add(new BigDecimal(add));
                    greatGrandTotal = greatGrandTotal.add(new BigDecimal(add));
                }

//                grandFather.setFatherGoodsEntities(abcFatherGoodsStockEvery(fatherGoodsEntities));
                BigDecimal divide = grandDouble.divide(new BigDecimal(total), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                grandFather.setFatherStockTotalPercent(divide.toString());
                grandFather.setFatherGoodsEntities(fatherGoodsEntities);
                grandFather.setFatherStockTotal(grandDouble.doubleValue());
                grandFather.setFatherStockTotalString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
//            greatGrandFather.setFatherGoodsEntities(grandTree);
            BigDecimal divide = greatGrandTotal.divide(new BigDecimal(total), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            greatGrandFather.setFatherStockTotalPercent(divide.toString());
            greatGrandFather.setFatherStockTotal(greatGrandTotal.doubleValue());
            greatGrandFather.setFatherStockTotalString(greatGrandTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

        }
//        grandTree = abcFatherGoodsStockEvery(grandTree);
        System.out.println("newwnwnnwnwnwnwn" + treeSet.size());
        return treeSet;
    }


    private Map<String, Object> getWasteTypeGoods(String startDate, String stopDate, Integer goodsFatherId,
                                                  Integer disId, Integer depId) {

        //echarts
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(mapDep);
        List<String> ids = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntities) {
                ids.add(dep.getGbDepartmentId().toString());
            }
        } else {
            return R.error(-1, "mei");
        }


        List<String> dateList = new ArrayList<>();
        List<String> costList = new ArrayList<>();
        Double costRateTotal = 0.0;

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
                mapDisGoods.put("disGoodsFatherId", goodsFatherId);
                if (depId != -1) {
                    mapDisGoods.put("depId", depId);
                } else {
                    mapDisGoods.put("depFatherIds", ids);
                }

                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
                if (count > 0) {
                    Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDisGoods);
                    costList.add(new BigDecimal(businessWeightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    costList.add("0");
                }
            }
        } else {
            // dateList
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            // disGoods
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("disGoodsFatherId", goodsFatherId);
            if (depId != -1) {
                mapDisGoods.put("depId", depId);
            } else {
                mapDisGoods.put("depFatherIds", ids);
            }

            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
            if (count > 0) {
                Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDisGoods);
                costList.add(new BigDecimal(businessWeightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            } else {
                costList.add("0");
            }
        }


        // arr
        List<GbDistributerGoodsEntity> distributerGoodsEntities = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsFatherId", goodsFatherId);
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }
        if (depId != -1) {
            map.put("depId", depId);
        } else {
            map.put("depFatherIds", ids);
        }
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer > 0) {
            distributerGoodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
            for (GbDistributerGoodsEntity distributerGoodsEntity : distributerGoodsEntities) {
                Map<String, Object> mapDis = new HashMap<>();
                if (howManyDaysInPeriod > 0) {
                    mapDis.put("startDate", startDate);
                    mapDis.put("stopDate", stopDate);
                } else {
                    mapDis.put("date", startDate);
                }
                mapDis.put("disGoodsId", distributerGoodsEntity.getGbDistributerGoodsId());
                if (depId != -1) {
                    mapDis.put("depId", depId);
                } else {
                    mapDis.put("depFatherIds", ids);
                }
                Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDis);
                Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(mapDis);
                Double restWeightTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDis);
                double v = 0;
                if (businessWeightTotal > 0) {
                    v = (businessWeightTotal / (weightTotal - restWeightTotal)) * 100;
                }
                distributerGoodsEntity.setGoodsWasteWeightTotalString(new BigDecimal(businessWeightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                distributerGoodsEntity.setGoodsWasteRate(v);
                distributerGoodsEntity.setGoodsWasteRateString(new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        } else {
            return R.error(-1, "e");
        }

        resultMap.put("date", dateList);
        resultMap.put("list", costList);
        resultMap.put("arr", distributerGoodsEntities);
        resultMap.put("code", "0");
        return resultMap;
    }


    private Map<String, Object> getLossTypeGoods(String startDate, String stopDate, Integer goodsFatherId,
                                                 Integer disId, Integer depId) {
        //echarts
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> costList = new ArrayList<>();

        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(mapDep);
        List<String> ids = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntities) {
                ids.add(dep.getGbDepartmentId().toString());
            }
        } else {
            return R.error(-1, "meiyou");
        }

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
                mapDisGoods.put("disGoodsFatherId", goodsFatherId);
                mapDisGoods.put("depFatherIds", ids);
                if (depId != -1) {
                    mapDisGoods.put("depId", depId);
                } else {
                    mapDisGoods.put("depFatherIds", ids);
                }

                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
                if (count > 0) {
                    Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDisGoods);
                    costList.add(new BigDecimal(businessWeightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    costList.add("0");
                }
            }
        } else {
            // dateList
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            // disGoods
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("disGoodsFatherId", goodsFatherId);
            if (depId != -1) {
                mapDisGoods.put("depId", depId);
            } else {
                mapDisGoods.put("depFatherIds", ids);
            }
            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
            if (count > 0) {
                Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDisGoods);
                costList.add(new BigDecimal(businessWeightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            } else {
                costList.add("0");
            }
        }


        // arr
        List<GbDistributerGoodsEntity> distributerGoodsEntities = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsFatherId", goodsFatherId);
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }
        if (depId != -1) {
            map.put("depId", depId);
        } else {
            map.put("depFatherIds", ids);
        }

        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer > 0) {
            distributerGoodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
            for (GbDistributerGoodsEntity distributerGoodsEntity : distributerGoodsEntities) {
                Map<String, Object> mapDis = new HashMap<>();
                if (howManyDaysInPeriod > 0) {
                    mapDis.put("startDate", startDate);
                    mapDis.put("stopDate", stopDate);
                } else {
                    mapDis.put("date", startDate);
                }
                mapDis.put("disGoodsId", distributerGoodsEntity.getGbDistributerGoodsId());
                if (depId != -1) {
                    mapDis.put("depId", depId);
                } else {
                    mapDis.put("depFatherIds", ids);
                }

                Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDis);
                Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(mapDis);
                Double restWeightTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDis);
                double v = 0;
                if (businessWeightTotal > 0) {
                    v = (businessWeightTotal / (weightTotal - restWeightTotal)) * 100;
                }

                distributerGoodsEntity.setGoodsLossWeightTotalString(new BigDecimal(businessWeightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                distributerGoodsEntity.setGoodsLossRate(v);
                distributerGoodsEntity.setGoodsLossRateString(new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        } else {
            return R.error(-1, "me");
        }

        resultMap.put("date", dateList);
        resultMap.put("list", costList);
        resultMap.put("arr", distributerGoodsEntities);
        resultMap.put("code", "0");

        return resultMap;
    }


    private Map<String, Object> getSalesTypeGoods(String startDate, String stopDate, Integer goodsFatherId,
                                                  Integer disId, Integer depId) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> costList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(mapDep);
        List<String> ids = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntities) {
                ids.add(dep.getGbDepartmentId().toString());
            }
        } else {
            return R.error(-1, "meiyou");
        }
        //echarts


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
                mapDisGoods.put("disGoodsFatherId", goodsFatherId);
                if (depId != -1) {
                    mapDisGoods.put("depFatherIds", ids);
                } else {
                    mapDisGoods.put("depId", depId);
                }
                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
                if (count > 0) {
                    Double produceWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDisGoods);
                    Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
                    Double restTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDisGoods);
                    double v = 0;
                    if (weightTotal.compareTo(restTotal) != 0) {
                        v = (produceWeightTotal / (weightTotal - restTotal)) * 100;
                    }
                    costList.add(new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    costList.add("0");
                }
            }

        } else {
            // dateList
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            // disGoods
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("disGoodsFatherId", goodsFatherId);
            if (depId != -1) {
                mapDisGoods.put("depFatherIds", ids);
            } else {
                mapDisGoods.put("depId", depId);
            }
            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
            if (count > 0) {
                Double produceWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDisGoods);
                Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
                Double restTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDisGoods);
                double v = 0;
                if (weightTotal.compareTo(restTotal) != 0) {
                    v = (produceWeightTotal / (weightTotal - restTotal)) * 100;
                }
                costList.add(new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            } else {
                costList.add("0");
            }
        }


        // arr
        List<GbDistributerGoodsEntity> distributerGoodsEntities = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsFatherId", goodsFatherId);
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }
        if (depId != -1) {
            map.put("depFatherIds", ids);
        } else {
            map.put("depId", depId);
        }

        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer > 0) {
            distributerGoodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
            for (GbDistributerGoodsEntity distributerGoodsEntity : distributerGoodsEntities) {
                Map<String, Object> mapDis = new HashMap<>();
                if (howManyDaysInPeriod > 0) {
                    mapDis.put("startDate", startDate);
                    mapDis.put("stopDate", stopDate);
                } else {
                    mapDis.put("date", startDate);
                }
                mapDis.put("disGoodsId", distributerGoodsEntity.getGbDistributerGoodsId());
                if (depId != -1) {
                    mapDis.put("depFatherIds", ids);
                } else {
                    mapDis.put("depId", depId);
                }
                Double produceWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDis);
                Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(mapDis);
                Double restWeightTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDis);
                double v = 0;
                if (weightTotal.compareTo(restWeightTotal) != 0) {
                    v = (produceWeightTotal / (weightTotal - restWeightTotal)) * 100;
                }
                System.out.println("vvvvvvv" + v);
                distributerGoodsEntity.setGoodsProduceWeightTotalString(new BigDecimal(produceWeightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                distributerGoodsEntity.setGoodsSalesRate(v);
                distributerGoodsEntity.setGoodsSalesRateString(new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        } else {
            return R.error(-1, "mei");
        }

        resultMap.put("date", dateList);
        resultMap.put("list", costList);
        resultMap.put("arr", distributerGoodsEntities);
        resultMap.put("code", "0");
        return resultMap;
    }


    private Map<String, Object> getFinishRateGoodsDep1(String startDate, String stopDate, Integer disGoodsId,
                                                       Integer disId, Integer depId) {
        Map<String, Object> resultMap = new HashMap<>();

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
                        map.put("equalRestWeight", 0);
                        map.put("produce", 0);
                        if (disGoodsId != -1) {
                            map.put("disGoodsId", disGoodsId);
                        }

                        String substring = whichDay.substring(8, 10);
                        dateList.add(substring);


                        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                        if (integer > 0) {
                            int hourTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(map);
                            int minuteTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(map);
                            int hourMinute = hourTotal * 60;
                            int totalMinute = (hourMinute + minuteTotal) / integer;
                            perDepList.add(new BigDecimal(totalMinute).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            Map<String, Object> mapDe = new HashMap<>();
                            mapDe.put("date", whichDay);
                            mapDe.put("value", new BigDecimal(totalMinute).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            detailList.add(mapDe);

                        } else {
                            perDepList.add("0");
                            Map<String, Object> mapDe = new HashMap<>();
                            mapDe.put("date", whichDay);
                            mapDe.put("value", "0");
                            detailList.add(mapDe);

                        }
                    }
//


                }
                depNameArr.add(perDepList);
                resultMap.put("date", dateList);
                mapDep.put("list", detailList);
                list.add(mapDep);

            }

            resultMap.put("depArr", depNameArr);
            resultMap.put("arr", list);

        }


        return resultMap;
    }


    private Map<String, Object> getFinishRateGoodsDep(String startDate, String stopDate, Integer disGoodsId,
                                                      Integer disId, Integer depId) {

        Map<String, Object> resultMap = new HashMap<>();
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
            return R.error(-1, "m");
        }

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }


        List<String> dateList = new ArrayList<>();
        List<String> costList = new ArrayList<>();
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
                mapDisGoods.put("equalRestWeight", 0);
                mapDisGoods.put("produce", 0);
                if (depId != -1) {
                    mapDisGoods.put("depId", depId);
                } else {
                    mapDisGoods.put("depFatherIds", ids);
                }

                Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
                if (count > 0) {
                    int hourTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(mapDisGoods);
                    int minuteTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(mapDisGoods);
                    int hourMinute = hourTotal * 60;
                    int totalMinute = (hourMinute + minuteTotal) / count;
                    costList.add(new BigDecimal(totalMinute).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    costList.add("0");
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
            mapDisGoods.put("equalRestWeight", 0);
            mapDisGoods.put("produce", 0);
            if (depId != -1) {
                mapDisGoods.put("depId", depId);
            } else {
                mapDisGoods.put("depFatherIds", ids);
            }
            Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
            if (count > 0) {
                int hourTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(mapDisGoods);
                int minuteTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(mapDisGoods);
                int hourMinute = hourTotal * 60;
                int totalMinute = (hourMinute + minuteTotal) / count;
                costList.add(new BigDecimal(totalMinute).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            } else {
                costList.add("0");
            }
        }


        // arr
        TreeSet<GbDepartmentEntity> departmentEntities = new TreeSet<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", disGoodsId);
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }
        map.put("equalRestWeight", 0);
        map.put("produce", 0);
        if (depId != -1) {
            map.put("depId", depId);
        } else {
            map.put("depFatherIds", ids);
        }
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        if (integer > 0) {
            departmentEntities = gbDepGoodsDailyService.queryWhichDepsHasProduceDepGoodsDaily(map);
            for (GbDepartmentEntity departmentEntity : departmentEntities) {
                Map<String, Object> mapEvery = new HashMap<>();
                mapEvery.put("disGoodsId", disGoodsId);
                if (howManyDaysInPeriod > 0) {
                    mapEvery.put("startDate", startDate);
                    mapEvery.put("stopDate", stopDate);
                } else {
                    mapEvery.put("date", startDate);
                }
                mapEvery.put("equalRestWeight", 0);
                mapEvery.put("produce", 0);
                mapEvery.put("depId", departmentEntity.getGbDepartmentId());
                System.out.println("kkkk" + mapEvery);
                int hourTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(mapEvery);
                int minuteTotal = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(mapEvery);
                int count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapEvery);

                int hourT = hourTotal * 60;
                int totalMinute = (hourT + minuteTotal) / count;

                int hour = totalMinute / 60;
                int minT = totalMinute % 60;
                String minStringT = "";
                if (minT < 10) {
                    minStringT = "0" + minT;
                } else {
                    minStringT = Integer.toString(minT);
                }
                String time = hour + ":" + minStringT;
                System.out.println("deptititit" + time);
                departmentEntity.setDepClearTimeString(time);
            }
        } else {
            return R.error(-1, "e");
        }

        resultMap.put("date", dateList);
        resultMap.put("list", costList);
        resultMap.put("arr", departmentEntities);
        resultMap.put("disGoods", disGoodsService.queryObject(disGoodsId));
        resultMap.put("code", "0");
        return resultMap;
    }


    private Map<String, Object> getFreshRateGoodsDep(String startDate, String stopDate, Integer disGoodsId,
                                                     Integer disId, Integer depId) {
        Map<String, Object> resultMap = new HashMap<>();
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
            return R.error(-1, "没有数据");
        }
        //echarts
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> costList = new ArrayList<>();
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
                mapDisGoods.put("produce", 0);
                if (depId != -1) {
                    mapDisGoods.put("depId", depId);
                } else {
                    mapDisGoods.put("depFatherIds", ids);
                }
                mapDisGoods.put("controlFresh", 1);
                Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
                double Tv = 0;

                if (count > 0) {

                    double v = gbDepGoodsDailyService.queryDepGoodsFreshRate(mapDisGoods);
                    Tv = v / count;
                    costList.add(new BigDecimal(Tv).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    costList.add("0");
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
            mapDisGoods.put("produce", 0);
            if (depId != -1) {
                mapDisGoods.put("depId", depId);
            } else {
                mapDisGoods.put("depFatherIds", ids);
            }
            mapDisGoods.put("controlFresh", 1);
            Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDisGoods);
            double Tv = 0;

            if (count > 0) {

                double v = gbDepGoodsDailyService.queryDepGoodsFreshRate(mapDisGoods);
                Tv = v / count;
                costList.add(new BigDecimal(Tv).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            } else {
                costList.add("0");
            }
        }


        // arr
        TreeSet<GbDepartmentEntity> departmentEntities = new TreeSet<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", disGoodsId);
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }
        map.put("produce", 0);
        if (depId != -1) {
            map.put("depId", depId);
        } else {
            map.put("depFatherIds", ids);
        }
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        if (integer > 0) {
            departmentEntities = gbDepGoodsDailyService.queryWhichDepsHasProduceDepGoodsDaily(map);
            for (GbDepartmentEntity departmentEntity : departmentEntities) {
                map.put("depId", departmentEntity.getGbDepartmentId());
                map.put("controlFresh", 1);
                Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                double Tv = 0;
                if (integer1 > 0) {
                    double v = gbDepGoodsDailyService.queryDepGoodsFreshRate(map);
                    Tv = v / integer1;

                }
                System.out.println("vvvvvvvFresh" + Tv);
                departmentEntity.setDepFreshRate(Tv);
                departmentEntity.setDepFreshRateString(new BigDecimal(Tv).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        } else {
            return R.error(-1, "没有数据");
        }

        resultMap.put("date", dateList);
        resultMap.put("list", costList);
        resultMap.put("arr", departmentEntities);
        resultMap.put("disGoods", disGoodsService.queryObject(disGoodsId));
        resultMap.put("code", "0");

        return resultMap;
    }


    private Map<String, Object> getWasteTypeGoodsDep(String startDate, String stopDate, Integer disGoodsId,
                                                     Integer disId, Integer depId) {
        Map<String, Object> resultMap = new HashMap<>();
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
            return R.error(-1, "m");
        }
        //echarts
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> costList = new ArrayList<>();
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
                if (depId != -1) {
                    mapDisGoods.put("depId", depId);
                } else {
                    mapDisGoods.put("depFatherIds", ids);
                }
                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
                if (count > 0) {
                    Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDisGoods);
                    Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
                    Double restTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDisGoods);
                    double v = 0;
                    if (weightTotal.compareTo(restTotal) != 0) {
                        v = (businessWeightTotal / (weightTotal - restTotal)) * 100;
                    }

                    costList.add(new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    costList.add("0");
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
            if (depId != -1) {
                mapDisGoods.put("depId", depId);
            } else {
                mapDisGoods.put("depFatherIds", ids);
            }
            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
            if (count > 0) {
                Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDisGoods);
                Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
                Double restTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDisGoods);
                double v = 0;
                if (weightTotal.compareTo(restTotal) != 0) {
                    v = (businessWeightTotal / (weightTotal - restTotal)) * 100;
                }

                costList.add(new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            } else {
                costList.add("0");
            }
        }


        // arr
        List<GbDepartmentEntity> departmentEntities = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", disGoodsId);
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }
        if (depId != -1) {
            map.put("depId", depId);
        } else {
            map.put("depFatherIds", ids);
        }
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer > 0) {
            departmentEntities = gbDepGoodsStockService.queryWhichDepHasStock(map);
            for (GbDepartmentEntity departmentEntity : departmentEntities) {
                map.put("depId", departmentEntity.getGbDepartmentId());
                Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
                Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(map);
                Double restWeightTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(map);
                double v = 0;
                if (weightTotal - restWeightTotal > 0) {
                    v = (businessWeightTotal / (weightTotal - restWeightTotal)) * 100;
                }
                System.out.println("vvvvvvv" + v);
                departmentEntity.setDepWasteRate(v);
                departmentEntity.setDepWasteRateString(new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        } else {
            return R.error(-1, "m");
        }

        resultMap.put("date", dateList);
        resultMap.put("list", costList);
        resultMap.put("arr", departmentEntities);
        resultMap.put("disGoods", disGoodsService.queryObject(disGoodsId));
        resultMap.put("code", "0");

        return resultMap;
    }


    private Map<String, Object> getLossTypeGoodsDep(String startDate, String stopDate, Integer disGoodsId,
                                                    Integer disId, Integer depId) {
        Map<String, Object> resultMap = new HashMap<>();
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
            return R.error(-1, "e");
        }
        //echarts
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> costList = new ArrayList<>();
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
                if (depId != -1) {
                    mapDisGoods.put("depId", depId);
                } else {
                    mapDisGoods.put("depFatherIds", ids);
                }
                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
                if (count > 0) {
                    Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDisGoods);
                    Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
                    Double restTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDisGoods);
                    double v = 0;
                    if (weightTotal.compareTo(restTotal) != 0) {
                        v = (businessWeightTotal / (weightTotal - restTotal)) * 100;
                    }

                    costList.add(new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    costList.add("0");
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
            if (depId != -1) {
                mapDisGoods.put("depId", depId);
            } else {
                mapDisGoods.put("depFatherIds", ids);
            }
            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
            if (count > 0) {
                Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDisGoods);
                Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
                Double restTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDisGoods);
                double v = 0;
                if (weightTotal.compareTo(restTotal) != 0) {
                    v = (businessWeightTotal / (weightTotal - restTotal)) * 100;
                }

                costList.add(new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            } else {
                costList.add("0");
            }
        }


        // arr
        List<GbDepartmentEntity> departmentEntities = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", disGoodsId);
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }
        if (depId != -1) {
            map.put("depId", depId);
        } else {
            map.put("depFatherIds", ids);
        }
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer > 0) {
            departmentEntities = gbDepGoodsStockService.queryWhichDepHasStock(map);
            for (GbDepartmentEntity departmentEntity : departmentEntities) {
                map.put("depId", departmentEntity.getGbDepartmentId());
                Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
                Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(map);
                Double restWeightTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(map);
                double v = 0;
                if (weightTotal - restWeightTotal > 0) {
                    v = (businessWeightTotal / (weightTotal - restWeightTotal)) * 100;
                }
                System.out.println("vvvvvvv" + v);
                departmentEntity.setDepLossRate(v);
                departmentEntity.setDepLossRateString(new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        } else {
            return R.error(-1, "m");
        }

        resultMap.put("date", dateList);
        resultMap.put("list", costList);
        resultMap.put("arr", departmentEntities);
        resultMap.put("disGoods", disGoodsService.queryObject(disGoodsId));
        resultMap.put("code", "0");

        return resultMap;
    }

    private Map<String, Object> getSalesTypeGoodsDep(String startDate, String stopDate, Integer disGoodsId,
                                                     Integer disId, Integer depId) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(mapDep);
        List<String> ids = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntities) {
                ids.add(dep.getGbDepartmentId().toString());
            }
        } else {
            return R.error(-1, "m");
        }
        //echarts
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<String> dateList = new ArrayList<>();
        List<String> costList = new ArrayList<>();
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
                if (depId != -1) {
                    mapDisGoods.put("depId", depId);
                } else {
                    mapDisGoods.put("depFatherIds", ids);
                }
                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
                if (count > 0) {
                    Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDisGoods);
                    Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
                    Double restTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDisGoods);
                    double v = 0;
                    if (weightTotal.compareTo(restTotal) != 0) {
                        v = (businessWeightTotal / (weightTotal - restTotal)) * 100;
                    }

                    costList.add(new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    costList.add("0");
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
            if (depId != -1) {
                mapDisGoods.put("depId", depId);
            } else {
                mapDisGoods.put("depFatherIds", ids);
            }
            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
            if (count > 0) {
                Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDisGoods);
                Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
                Double restTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDisGoods);
                double v = 0;
                if (weightTotal.compareTo(restTotal) != 0) {
                    v = (businessWeightTotal / (weightTotal - restTotal)) * 100;
                }

                costList.add(new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            } else {
                costList.add("0");
            }
        }


        // arr
        List<GbDepartmentEntity> departmentEntitiesS = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", disGoodsId);
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }
        if (depId != -1) {
            map.put("depId", depId);
        } else {
            map.put("depFatherIds", ids);
        }
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer > 0) {
            departmentEntitiesS = gbDepGoodsStockService.queryWhichDepHasStock(map);
            for (GbDepartmentEntity departmentEntity : departmentEntitiesS) {
                map.put("depId", departmentEntity.getGbDepartmentId());
                Double businessWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
                Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(map);
                Double restWeightTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(map);
                double v = 0;
                if (weightTotal - restWeightTotal > 0) {
                    v = (businessWeightTotal / (weightTotal - restWeightTotal)) * 100;
                }
                System.out.println("vvvvvvv" + v);
                departmentEntity.setDepSalesRate(v);
                departmentEntity.setDepSalesRateString(new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        } else {
            return R.error(-1, "e");
        }

        resultMap.put("date", dateList);
        resultMap.put("list", costList);
        resultMap.put("arr", departmentEntitiesS);
        resultMap.put("disGoods", disGoodsService.queryObject(disGoodsId));
        resultMap.put("code", "0");
        return resultMap;
    }


    private Map<String, Object> getCostTypeGoodsDep(String startDate, String stopDate, Integer disGoodsId) {
        Map<String, Object> resultMap = new HashMap<>();

        //echarts
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        List<String> dateList = new ArrayList<>();
        List<String> costList = new ArrayList<>();
        Double costRateTotal = 0.0;

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
                    Double sellingPriceTotal = gbDepGoodsStockService.queryStockSellingPriceTotal(mapDisGoods);
                    Double priceTotal = gbDepGoodsStockService.queryStockPriceTotal(mapDisGoods);
                    double v = (priceTotal / sellingPriceTotal) * 100;
                    costList.add(new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    costList.add("0");
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
                Double sellingPriceTotal = gbDepGoodsStockService.queryStockSellingPriceTotal(mapDisGoods);
                Double priceTotal = gbDepGoodsStockService.queryStockPriceTotal(mapDisGoods);
                double v = (priceTotal / sellingPriceTotal) * 100;
                costList.add(new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            } else {
                costList.add("0");
            }
        }


        // arr
        List<GbDepartmentEntity> departmentEntities = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", disGoodsId);
        if (howManyDaysInPeriod > 0) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        } else {
            map.put("date", startDate);
        }
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer > 0) {
            departmentEntities = gbDepGoodsStockService.queryWhichDepHasStock(map);
            for (GbDepartmentEntity departmentEntity : departmentEntities) {
                map.put("depId", departmentEntity.getGbDepartmentId());
                Double sellingPriceTotal = gbDepGoodsStockService.queryStockSellingPriceTotal(map);
                Double priceTotal = gbDepGoodsStockService.queryStockPriceTotal(map);
                double v = (priceTotal / sellingPriceTotal) * 100;
                System.out.println("vvvvvvv" + v);
                departmentEntity.setDepCostRate(v);
                departmentEntity.setDepCostRateString(new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        }

        resultMap.put("date", dateList);
        resultMap.put("list", costList);
        resultMap.put("arr", departmentEntities);
        resultMap.put("disGoods", disGoodsService.queryObject(disGoodsId));

        return resultMap;
    }


    private List<GbDistributerFatherGoodsEntity> getCostType(String startDate, String stopDate, Integer disId) {
//        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsEntities = new TreeSet<>();
        List<GbDistributerFatherGoodsEntity> greatGrandGoodsEntities = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer > 0) {
            greatGrandGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

            System.out.println("siisisisisi" + greatGrandGoodsEntities.size());
            for (GbDistributerFatherGoodsEntity greatGrand : greatGrandGoodsEntities) {
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrand.getFatherGoodsEntities();

                Double greatGrandCostRate = 0.0;
                int greatGrandCount = 0;
                for (GbDistributerFatherGoodsEntity grand : grandGoodsEntities) {
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();

                    Double grandCostRate = 0.0;
                    int grandCount = 0;
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map.put("disGoodsFatherId", gbDistributerFatherGoodsId);
                        Double costRateTotal = gbDepGoodsStockService.queryStockCostRateTotal(map);
                        int count = gbDepGoodsStockService.queryGoodsStockCount(map);

                        double v = (costRateTotal / count) * 100;
                        System.out.println("vvvvvvvCost" + v);
                        father.setFatherCostRate(v);
                        father.setFatherCostRateString(new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        grandCount = grandCount + count;
                        grandCostRate = grandCostRate + costRateTotal;

                    }
                    double vg = (grandCostRate / grandCount) * 100;
                    System.out.println("vvvvvvvgggggggggggCost" + vg);

                    grand.setFatherCostRate(vg);
                    grand.setFatherCostRateString(new BigDecimal(vg).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                    greatGrandCostRate = greatGrandCostRate + grandCostRate;
                    greatGrandCount = greatGrandCount + grandCount;
                }
                double vgg = (greatGrandCostRate / greatGrandCount) * 100;
                greatGrand.setFatherCostRate(vgg);
                greatGrand.setFatherCostRateString(new BigDecimal(vgg).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

            }

        }


        return greatGrandGoodsEntities;
    }

    private Map<String, Object> getSalesType(String startDate, String stopDate, Integer disId) {


//        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsEntities = new TreeSet<>();
        List<GbDistributerFatherGoodsEntity> greatGrandGoodsEntities = new ArrayList<>();
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


        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("depFatherIds", ids);
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer == 0) {
            return R.error(-1, "没有数据");
        }

        //salesRate
//        Double produceWeightTotalT = gbDepGoodsStockService.queryDepStockProduceWeightTotal(map);
        Double produceWeightTotalT = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
        Double weightTotalT = gbDepGoodsStockService.queryDepStockWeightTotal(map);
        Double restWeightTotalT = gbDepGoodsStockService.queryDepStockRestWeightTotal(map);
        Double lossWeightTotalT = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
        Double wasteWeightTotalT = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);

        double vTS = 0;
        if (weightTotalT.compareTo(restWeightTotalT) != 0) {
            vTS = (produceWeightTotalT / (weightTotalT - restWeightTotalT)) * 100;
        }
        double vTL = 0;
        if (lossWeightTotalT > 0) {
            vTL = (lossWeightTotalT / (weightTotalT - restWeightTotalT)) * 100;
        }
        double vTW = 0;
        if (wasteWeightTotalT > 0) {
            vTW = (wasteWeightTotalT / (weightTotalT - restWeightTotalT)) * 100;
        }

        if (integer > 0) {
            greatGrandGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);


            for (GbDistributerFatherGoodsEntity greatGrand : greatGrandGoodsEntities) {
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrand.getFatherGoodsEntities();
                double greatGrandProduceWeight = 0.0;
                Double greatGrandWeight = 0.0;
                Double greatGrandRestWeight = 0.0;
                for (GbDistributerFatherGoodsEntity grand : grandGoodsEntities) {
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    double grandProduceWeight = 0.0;
                    Double grandWeight = 0.0;
                    Double grandRestWeight = 0.0;
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map.put("disGoodsFatherId", gbDistributerFatherGoodsId);
                        Double produceWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
                        Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(map);
                        Double restWeightTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(map);
                        double v = 0;
                        if (weightTotal.compareTo(restWeightTotal) != 0 && produceWeightTotal > 0) {
                            v = (produceWeightTotal / (weightTotal - restWeightTotal)) * 100;
                        }
                        father.setFatherProduceWeightString(new BigDecimal(produceWeightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        father.setFatherSalesRate(v);
                        father.setFatherSalesRateString(new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                        grandProduceWeight = grandProduceWeight + produceWeightTotal;
                        grandWeight = grandWeight + weightTotal;
                        grandRestWeight = grandRestWeight + restWeightTotal;
                    }
                    double vg = 0;
                    if (grandWeight.compareTo(grandRestWeight) != 0 && grandProduceWeight > 0) {
                        vg = (grandProduceWeight / (grandWeight - grandRestWeight)) * 100;
                    }

                    grand.setFatherProduceWeightString(new BigDecimal(grandProduceWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    grand.setFatherSalesRate(vg);
                    grand.setFatherSalesRateString(new BigDecimal(vg).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                    greatGrandProduceWeight = greatGrandProduceWeight + grandProduceWeight;
                    greatGrandWeight = greatGrandWeight + grandWeight;
                    greatGrandRestWeight = greatGrandRestWeight + grandRestWeight;
                }

                double vgg = 0;
                if (greatGrandWeight.compareTo(greatGrandRestWeight) != 0) {
                    vgg = (greatGrandProduceWeight / (greatGrandWeight - greatGrandRestWeight)) * 100;
                }
                greatGrand.setFatherProduceWeightString(new BigDecimal(greatGrandProduceWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                greatGrand.setFatherSalesRate(vgg);
                greatGrand.setFatherSalesRateString(new BigDecimal(vgg).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        }


        Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("arr", greatGrandGoodsEntities);
        mapResult.put("salesRate", new BigDecimal(vTS).setScale(2, BigDecimal.ROUND_HALF_UP));
        mapResult.put("lossRate", new BigDecimal(vTL).setScale(2, BigDecimal.ROUND_HALF_UP));
        mapResult.put("wasteRate", new BigDecimal(vTW).setScale(2, BigDecimal.ROUND_HALF_UP));
        mapResult.put("code", "0");

        return mapResult;
    }


    private Map<String, Object> getLossType(String startDate, String stopDate, Integer disId) {
//        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsEntities = new TreeSet<>();
        List<GbDistributerFatherGoodsEntity> greatGrandGoodsEntities = new ArrayList<>();

        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(mapDep);
        List<String> ids = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntities) {
                ids.add(dep.getGbDepartmentId().toString());
            }
        } else {
            return R.error(-1, "meiyou");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("depFatherIds", ids);
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer == 0) {
            return R.error(-1, "meiyou");
        }

        //salesRate
//        Double produceWeightTotalT = gbDepGoodsStockService.queryDepStockProduceWeightTotal(map);
        Double produceWeightTotalT = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
        Double weightTotalT = gbDepGoodsStockService.queryDepStockWeightTotal(map);
        Double restWeightTotalT = gbDepGoodsStockService.queryDepStockRestWeightTotal(map);
        Double lossWeightTotalT = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
        Double wasteWeightTotalT = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);

        double vTS = 0;
        if (weightTotalT.compareTo(restWeightTotalT) != 0) {
            vTS = (produceWeightTotalT / (weightTotalT - restWeightTotalT)) * 100;
        }
        double vTL = 0;
        if (lossWeightTotalT > 0) {
            vTL = (lossWeightTotalT / (weightTotalT - restWeightTotalT)) * 100;
        }
        double vTW = 0;
        if (wasteWeightTotalT > 0) {
            vTW = (wasteWeightTotalT / (weightTotalT - restWeightTotalT)) * 100;
        }


        if (integer > 0) {
            greatGrandGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

            for (GbDistributerFatherGoodsEntity greatGrand : greatGrandGoodsEntities) {
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrand.getFatherGoodsEntities();
                double greatGrandLossWeight = 0.0;
                double greatGrandWeight = 0.0;
                double greatGrandRestWeight = 0.0;


                for (GbDistributerFatherGoodsEntity grand : grandGoodsEntities) {
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    double grandLossWeight = 0.0;
                    double grandWeight = 0.0;
                    double grandRestWeight = 0.0;

                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map.put("disGoodsFatherId", gbDistributerFatherGoodsId);
                        Double lossWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
                        Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(map);
                        Double restWeightTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(map);
                        double v = 0;
                        if (weightTotal.compareTo(restWeightTotal) != 0 && lossWeightTotal > 0) {
                            v = (lossWeightTotal / (weightTotal - restWeightTotal)) * 100;
                        }

                        father.setFatherLossWeightString(new BigDecimal(lossWeightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        father.setFatherLossRate(v);
                        father.setFatherLossRateString(new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                        grandLossWeight = grandLossWeight + lossWeightTotal;
                        grandWeight = grandWeight + weightTotal;
                        grandRestWeight = grandRestWeight + restWeightTotal;
                    }
                    double vg = 0;
                    if (grandWeight != grandRestWeight && grandLossWeight > 0) {
                        vg = (grandLossWeight / (grandWeight - grandRestWeight)) * 100;
                    }
                    System.out.println("lossssRate" + vg);
                    grand.setFatherLossWeightString(new BigDecimal(grandLossWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    grand.setFatherLossRate(vg);
                    grand.setFatherLossRateString(new BigDecimal(vg).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                    greatGrandLossWeight = greatGrandLossWeight + grandLossWeight;
                    greatGrandWeight = greatGrandWeight + grandWeight;
                    greatGrandRestWeight = greatGrandRestWeight + grandRestWeight;
                }
                double vgg = 0;
                if (greatGrandWeight != greatGrandRestWeight && greatGrandLossWeight > 0) {
                    vgg = (greatGrandLossWeight / (greatGrandWeight - greatGrandRestWeight)) * 100;
                }
                greatGrand.setFatherLossWeightString(new BigDecimal(greatGrandLossWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                greatGrand.setFatherLossRate(vgg);
                greatGrand.setFatherLossRateString(new BigDecimal(vgg).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

            }

        }

        Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("arr", greatGrandGoodsEntities);
        mapResult.put("salesRate", new BigDecimal(vTS).setScale(2, BigDecimal.ROUND_HALF_UP));
        mapResult.put("lossRate", new BigDecimal(vTL).setScale(2, BigDecimal.ROUND_HALF_UP));
        mapResult.put("wasteRate", new BigDecimal(vTW).setScale(2, BigDecimal.ROUND_HALF_UP));
        mapResult.put("code", "0");
        return mapResult;
    }

    private Map<String, Object> getWasteType(String startDate, String stopDate, Integer disId) {
//        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsEntities = new TreeSet<>();
        List<GbDistributerFatherGoodsEntity> greatGrandGoodsEntities = new ArrayList<>();
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(mapDep);
        List<String> ids = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntities) {
                ids.add(dep.getGbDepartmentId().toString());
            }
        } else {
            return R.error(-1, "meiyou");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("depFatherIds", ids);
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer == 0) {
            return R.error(-1, "meiyou");
        }
        //salesRate
        Double produceWeightTotalT = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(map);
        Double weightTotalT = gbDepGoodsStockService.queryDepStockWeightTotal(map);
        Double restWeightTotalT = gbDepGoodsStockService.queryDepStockRestWeightTotal(map);
        Double lossWeightTotalT = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(map);
        Double wasteWeightTotalT = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);

        double vTS = 0;
        if (weightTotalT.compareTo(restWeightTotalT) != 0) {
            vTS = (produceWeightTotalT / (weightTotalT - restWeightTotalT)) * 100;
        }
        double vTL = 0;
        if (lossWeightTotalT > 0) {
            vTL = (lossWeightTotalT / (weightTotalT - restWeightTotalT)) * 100;
        }
        double vTW = 0;
        if (wasteWeightTotalT > 0) {
            vTW = (wasteWeightTotalT / (weightTotalT - restWeightTotalT)) * 100;
        }
        if (integer > 0) {
            greatGrandGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);
            for (GbDistributerFatherGoodsEntity greatGrand : greatGrandGoodsEntities) {
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrand.getFatherGoodsEntities();
                double greatGrandWasteWeight = 0.0;
                double greatGrandWeight = 0.0;
                double greatGrandRestWeight = 0.0;

                for (GbDistributerFatherGoodsEntity grand : grandGoodsEntities) {
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();

                    double grandWasteWeight = 0.0;
                    double grandWeight = 0.0;
                    double grandRestWeight = 0.0;
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map.put("disGoodsFatherId", gbDistributerFatherGoodsId);
                        Double wasteWeightTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(map);
                        Double weightTotal = gbDepGoodsStockService.queryDepStockWeightTotal(map);
                        Double restWeightTotal = gbDepGoodsStockService.queryDepStockRestWeightTotal(map);

                        double v = 0;
                        if (wasteWeightTotal > 0) {
                            v = (wasteWeightTotal / (weightTotal - restWeightTotal)) * 100;
                        }
                        System.out.println("vvvvvvv" + v);
                        father.setFatherWasteWeightString(new BigDecimal(wasteWeightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        father.setFatherWasteRate(v);
                        father.setFatherWasteRateString(new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                        grandWasteWeight = grandWasteWeight + wasteWeightTotal;
                        grandWeight = grandWeight + weightTotal;
                        grandRestWeight = grandRestWeight + restWeightTotal;

                    }
                    double vg = 0;
                    if (grandWasteWeight > 0) {
                        vg = (grandWasteWeight / (grandWeight - grandRestWeight)) * 100;
                    }
                    System.out.println("vvvvvvvggggggggggg" + vg);
                    grand.setFatherWasteWeightString(new BigDecimal(grandWasteWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    grand.setFatherWasteRate(vg);
                    grand.setFatherWasteRateString(new BigDecimal(vg).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                    greatGrandWasteWeight = greatGrandWasteWeight + grandWasteWeight;
                    greatGrandWeight = greatGrandWeight + grandWeight;
                    greatGrandRestWeight = greatGrandRestWeight + grandRestWeight;

                }
                double vgg = 0;
                if (greatGrandWasteWeight > 0) {
                    vgg = (greatGrandWasteWeight / (greatGrandWeight - greatGrandRestWeight)) * 100;
                }
                greatGrand.setFatherWasteWeightString(new BigDecimal(greatGrandWasteWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                greatGrand.setFatherWasteRate(vgg);
                greatGrand.setFatherWasteRateString(new BigDecimal(vgg).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

            }

        }

        Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("arr", greatGrandGoodsEntities);
        mapResult.put("salesRate", new BigDecimal(vTS).setScale(2, BigDecimal.ROUND_HALF_UP));
        mapResult.put("lossRate", new BigDecimal(vTL).setScale(2, BigDecimal.ROUND_HALF_UP));
        mapResult.put("wasteRate", new BigDecimal(vTW).setScale(2, BigDecimal.ROUND_HALF_UP));
        mapResult.put("code", "0");

        return mapResult;
    }


    @RequestMapping(value = "/departmentGetInventoryGoodsStock", method = RequestMethod.POST)
    @ResponseBody
    public R departmentGetInventoryGoodsStock(Integer depId, String inventoryType) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("dayuStatus", -1);
        map.put("restWeight", 0);
        if (inventoryType.equals("daily")) {
            map.put("inventoryDate", formatWhatDay(0));
//            map.put("inventoryType", getDISGoodsInventroyDaily());
        }
        if (inventoryType.equals("week")) {
            map.put("week", getWeekOfYear(0));
//            map.put("inventoryType", getDISGoodsInventroyWeek());
        }
        if (inventoryType.equals("month")) {
            map.put("equalMonth", formatWhatMonth(0));
//            map.put("inventoryType", getDISGoodsInventroyMonth());
        }

        System.out.println("dkfkafsakf" + map);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepGoodsStockService.queryDepGoodsStockByParams(map);
        return R.ok().put("data", fatherGoodsEntities);
    }

    @RequestMapping(value = "/departmentGetHaveInventoryGoodsStock", method = RequestMethod.POST)
    @ResponseBody
    public R departmentGetHaveInventoryGoodsStock(Integer depId, String inventoryType) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("dayuStatus", -1);
        map.put("restWeight", 0);
        if (inventoryType.equals("daily")) {
            map.put("inventoryDateEqual", formatWhatDay(1));
        }
        if (inventoryType.equals("week")) {
            map.put("equalWeek", getWeekOfYear(0) + 1);
        }
        if (inventoryType.equals("month")) {
            map.put("equalMonth", getNextMonth());
        }

        System.out.println("dakfaslf;sa" + map);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepGoodsStockService.queryDepGoodsStockByParams(map);
        return R.ok().put("data", fatherGoodsEntities);
    }

    @RequestMapping(value = "/getGoodsStockList", method = RequestMethod.POST)
    @ResponseBody
    public R getGoodsStockList(Integer disGoodsId, String dateDuring,
                               Integer inventoryType, Integer depType) {

        String dateString = "";
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", disGoodsId);
        map.put("depType", depType);
        map.put("restWeight", 0);

        if (dateDuring.equals("all")) {
            Integer integerBefore = gbDepGoodsStockService.queryGoodsStockCount(map);
            if (integerBefore > 0) {
                List<GbDepartmentGoodsStockEntity> stockEntityStop = gbDepGoodsStockService.queryGoodsStockByParams(map);
                result.put("in", stockEntityStop);
            }
            dateString = "全部";
        }

        //inPeriod
        if (dateDuring.equals("in")) {
            if (inventoryType.equals(getDISGoodsInventroyDaily())) {
                //in zhiqian
                map.put("xiaoyuStopDate", formatWhatDay(0));
                Integer integerBefore = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerBefore > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityStop = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("before", stockEntityStop);
                }

                // in
                map.put("xiaoyuStopDate", null);
                map.put("date", formatWhatDay(0));
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                dateString = formatWhatDayString(0);
            } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {

                //in zhiqian
                map.put("stopDate", formatWhatDay(-7));
                System.out.println("befororoorroro" + map);
                Integer integerBefore = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerBefore > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityStop = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("before", stockEntityStop);
                }

                // in
                map.put("startDate", formatWhatDay(-7));
                map.put("notDayuStopDate", formatWhatDay(0));
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                // after
                map.put("stopDate", formatWhatDay(-7));
                map.put("startDate", null);
                System.out.println("zhihfoufhdodhhhouououo" + map);
                Integer integerAfter = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerAfter > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListAfter = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("after", stockEntityListAfter);
                }
                dateString = formatWhatDayString(-7) + "-" + formatWhatDayString(0);
            } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {

                //in zhiqian
                map.put("stopDate", formatWhatDay(-30));
                System.out.println("befororoorroro" + map);
                Integer integerBefore = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerBefore > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityStop = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("before", stockEntityStop);
                }

                // in
                map.put("stopDate", null);
                map.put("startDate", formatWhatDay(-30));
                map.put("notDayuStopDate", formatWhatDay(0));
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                // after
                map.put("stopDate", formatWhatDay(-30));
                map.put("startDate", null);
                System.out.println("zhihfoufhdodhhhouououo" + map);
                Integer integerAfter = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerAfter > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListAfter = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("after", stockEntityListAfter);
                }
                dateString = formatWhatDayString(-30) + "-" + formatWhatDayString(0);
            }
        }
        //one Period
        if (dateDuring.equals("one")) {

            if (inventoryType.equals(getDISGoodsInventroyDaily())) {
                //in zhiqian
                map.put("xiaoyuStopDate", formatWhatDay(-1));
                Integer integerBefore = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerBefore > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityStop = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("before", stockEntityStop);
                }

                // in
                map.put("startDate", formatWhatDay(-1));
                map.put("xiaoyuStopDate", formatWhatDay(0));
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                // after
                map.put("startDate", formatWhatDay(0));
                map.put("xiaoyuStopDate", null);
                System.out.println("zhihfoufhdodhhhouououooneonen" + map);
                Integer integerAfter = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerAfter > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListAfter = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("after", stockEntityListAfter);
                }
                dateString = formatWhatDayString(-1);

            } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {

                //in zhiqian
                map.put("stopDate", formatWhatDay(-14));
                System.out.println("befororoorroroweek" + map);
                Integer integerBefore = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerBefore > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityStop = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("before", stockEntityStop);
                }

                // in
                map.put("startDate", formatWhatDay(-7));
                map.put("stopDate", formatWhatDay(-14));
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                // after
                map.put("stopDate", formatWhatDay(-14));
                map.put("startDate", null);
                System.out.println("zhihfoufhdodhhhouououo" + map);
                Integer integerAfter = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerAfter > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListAfter = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("after", stockEntityListAfter);
                }
                dateString = formatWhatDayString(-7) + "-" + formatWhatDayString(-14);

            } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {

                //in zhiqian
                map.put("stopDate", formatWhatDay(-60));
                System.out.println("befororoorroro" + map);
                Integer integerBefore = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerBefore > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityStop = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("before", stockEntityStop);
                }

                // in
                map.put("startDate", formatWhatDay(-60));
                map.put("stopDate", formatWhatDay(-30));
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                // after
                map.put("stopDate", formatWhatDay(-60));
                map.put("startDate", null);
                System.out.println("zhihfoufhdodhhhouououo" + map);
                Integer integerAfter = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerAfter > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListAfter = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("after", stockEntityListAfter);
                }
                dateString = formatWhatDayString(-60) + "-" + formatWhatDayString(-30);
            }

        }
        //two Period
        if (dateDuring.equals("two")) {
            if (inventoryType.equals(getDISGoodsInventroyDaily())) {

                //in zhiqian
                map.put("xiaoyuStopDate", formatWhatDay(-2));
                System.out.println("befororoorroroafafafa" + map);
                Integer integerBefore = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerBefore > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityStop = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("before", stockEntityStop);
                }

                // in
                map.put("startDate", formatWhatDay(-2));
                map.put("xiaoyuStopDate", formatWhatDay(-1));
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                // after
                map.put("startDate", formatWhatDay(-1));
                map.put("xiaoyuStopDate", null);
                System.out.println("zhihfoufhdodhhhouououoaaaa" + map);
                Integer integerAfter = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerAfter > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListAfter = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("after", stockEntityListAfter);
                }

                dateString = formatWhatDayString(-2);

            } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {

                //in zhiqian
                map.put("stopDate", formatWhatDay(-21));
                Integer integerBefore = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerBefore > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityStop = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("before", stockEntityStop);
                }

                // in
                map.put("startDate", formatWhatDay(-21));
                map.put("notDayuStopDate", formatWhatDay(-14));
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                // after
                map.put("notDayuStopDate", formatWhatDay(-21));
                map.put("startDate", null);
                System.out.println("zhihfoufhdodhhhouououo" + map);
                Integer integerAfter = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerAfter > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListAfter = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("after", stockEntityListAfter);
                }

                dateString = formatWhatDayString(-21) + "-" + formatWhatDayString(-14);


            } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {

                //in zhiqian
                map.put("stopDate", formatWhatDay(-90));
                System.out.println("befororoorroro" + map);
                Integer integerBefore = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerBefore > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityStop = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("before", stockEntityStop);
                }

                // in
                map.put("startDate", formatWhatDay(-90));
                map.put("notDayuStopDate", formatWhatDay(-60));
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                // after
                map.put("notDayuStopDate", formatWhatDay(-90));
                map.put("startDate", null);
                System.out.println("zhihfoufhdodhhhouououo" + map);
                Integer integerAfter = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerAfter > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListAfter = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("after", stockEntityListAfter);
                }
                dateString = formatWhatDayString(-90) + "-" + formatWhatDayString(-60);

            }
        }

        //three Period
        if (dateDuring.equals("three")) {
            if (inventoryType.equals(getDISGoodsInventroyDaily())) {

                //in zhiqian
                map.put("xiaoyuStopDate", formatWhatDay(-3));
                System.out.println("befororoorroro" + map);
                Integer integerBefore = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerBefore > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityStop = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("before", stockEntityStop);
                }

                // in
                map.put("startDate", formatWhatDay(-3));
                map.put("xiaoyuStopDate", formatWhatDay(-2));
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                // after
                map.put("startDate", formatWhatDay(-2));
                map.put("xiaoyuStopDate", null);
                System.out.println("zhihfoufhdodhhhouououo" + map);
                Integer integerAfter = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerAfter > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListAfter = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("after", stockEntityListAfter);
                }
                dateString = formatWhatDayString(-3);

            } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {


                //in zhiqian
                map.put("stopDate", formatWhatDay(-28));
                System.out.println("befororoorroro" + map);
                Integer integerBefore = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerBefore > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityStop = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("before", stockEntityStop);
                }

                // in
                map.put("startDate", formatWhatDay(-28));
                map.put("notDayuStopDate", formatWhatDay(-21));
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                // after
                map.put("notDayuStopDate", formatWhatDay(-28));
                map.put("startDate", null);
                System.out.println("zhihfoufhdodhhhouououo" + map);
                Integer integerAfter = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerAfter > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListAfter = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("after", stockEntityListAfter);
                }
            } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {

                //in zhiqian
                map.put("stopDate", formatWhatDay(-120));
                System.out.println("befororoorroro" + map);
                Integer integerBefore = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerBefore > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityStop = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("before", stockEntityStop);
                }

                // in
                map.put("startDate", formatWhatDay(-120));
                map.put("notDayuStopDate", formatWhatDay(-90));
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                // after
                map.put("notDayuStopDate", formatWhatDay(-120));
                map.put("startDate", null);
                System.out.println("zhihfoufhdodhhhouououo" + map);
                Integer integerAfter = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerAfter > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListAfter = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("after", stockEntityListAfter);
                }
            }
        }

//exceed Period
        if (dateDuring.equals("exceed")) {
            if (inventoryType.equals(getDISGoodsInventroyDaily())) {
                // in
                map.put("stopDate", formatWhatDay(-3));
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                // after
                map.put("startDate", formatWhatDay(-3));
                map.put("stopDate", null);
                Integer integerAfter = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerAfter > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListAfter = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("after", stockEntityListAfter);
                }
            } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {
                // in
                map.put("startDate", formatWhatDay(-28));
                map.put("stopDate", null);
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                // after
                map.put("startDate", formatWhatDay(-28));
                map.put("stopDate", null);
                System.out.println("zhihfoufhdodhhhouououo" + map);
                Integer integerAfter = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerAfter > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListAfter = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("after", stockEntityListAfter);
                }

            } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {
                // in
                map.put("startDate", formatWhatDay(-120));
                map.put("stopDate", null);
                Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerIn > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("in", stockEntityListIn);
                }

                // after
                map.put("startDate", formatWhatDay(-120));
                map.put("stopDate", null);
                System.out.println("zhihfoufhdodhhhouououo" + map);
                Integer integerAfter = gbDepGoodsStockService.queryGoodsStockCount(map);
                if (integerAfter > 0) {
                    List<GbDepartmentGoodsStockEntity> stockEntityListAfter = gbDepGoodsStockService.queryGoodsStockByParams(map);
                    result.put("after", stockEntityListAfter);
                }


            }
        }


        Map<String, Object> mapr = new HashMap<>();
        mapr.put("arr", result);
        mapr.put("dateString", dateString);

        return R.ok().put("data", mapr);
    }

    @RequestMapping(value = "/depGetGoodsStockListAll", method = RequestMethod.POST)
    @ResponseBody
    public R depGetGoodsStockListAll(Integer disGoodsId, Integer depId) {
        Map<String, Object> map = new HashMap<>();

        System.out.println("depidid" + depId);
        map.put("in", depGetDepStockDayStock(disGoodsId, depId, 0));
        map.put("one", depGetDepStockDayStock(disGoodsId, depId, -1));
        map.put("two", depGetDepStockDayStock(disGoodsId, depId, -2));
        map.put("three", depGetDepStockDayStock(disGoodsId, depId, -3));
        map.put("exceedThree", depGetDepStockDayStock(disGoodsId, depId, -4));


        return R.ok().put("data", map);
    }

    private Map<String, Object> depGetDepStockDayStock(Integer disGoodsId, Integer depId, Integer which) {
        List<GbDepartmentGoodsStockEntity> stockEntityListIn = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        double total = 0.0;
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", disGoodsId);
        map.put("depFatherId", depId);
        map.put("restWeight", 0);
        System.out.println("deppeostotototo" + map);
        if (which == -4) {
            map.put("stopDate", formatWhatDay(which));

        } else {
            map.put("date", formatWhatDay(which));

        }
        System.out.println("everyrdyydydy" + map);
        Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integerIn > 0) {
            stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
            total = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
            result.put("arr", stockEntityListIn);
        } else {
            result.put("arr", stockEntityListIn);

        }
        result.put("dateString", formatWhatDayString(0));
        result.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        return result;
    }


    @RequestMapping(value = "/getGoodsStockListAll", method = RequestMethod.POST)
    @ResponseBody
    public R getGoodsStockListAll(Integer disGoodsId, Integer searchDepId) {
        Map<String, Object> map = new HashMap<>();

        map.put("in", getDepStockDayStock(disGoodsId, searchDepId, 0));
        map.put("one", getDepStockDayStock(disGoodsId,searchDepId, -1));
        map.put("two", getDepStockDayStock(disGoodsId, searchDepId, -2));
        map.put("three", getDepStockDayStock(disGoodsId,  searchDepId, -3));
        map.put("exceedThree", getDepStockDayStock(disGoodsId,  searchDepId, -4));


        return R.ok().put("data", map);
    }


    private Map<String, Object> getDepStockDayStock(Integer disGoodsId, Integer searchDepId, Integer which) {
        List<GbDepartmentGoodsStockEntity> stockEntityListIn = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        double total = 0.0;
        double totalWeight = 0.0;
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", disGoodsId);
        if (searchDepId != -1) {
            map.put("depId", searchDepId);
        }
//        else{
//            map.put("depType", depType);
//        }
        map.put("restWeight", 0);
        if (which == -4) {
            map.put("stopDate", formatWhatDay(which));

        } else {
            map.put("date", formatWhatDay(which));

        }
        System.out.println("mapddeieie" + map);
        Integer integerIn = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integerIn > 0) {
            System.out.println("depididid" + map);
            stockEntityListIn = gbDepGoodsStockService.queryGoodsStockByParams(map);
            System.out.println("liinn");

            total = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
            totalWeight = gbDepGoodsStockService.queryDepGoodsRestWeightTotal(map);
            result.put("arr", stockEntityListIn);
        } else {
            result.put("arr", stockEntityListIn);

        }
        result.put("dateString", formatWhatDayString(which));
        result.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        result.put("totalWeight", new BigDecimal(totalWeight).setScale(1, BigDecimal.ROUND_HALF_UP));
        return result;
    }

    @RequestMapping(value = "/getEveryGoodsStockMangement", method = RequestMethod.POST)
    @ResponseBody
    public R getEveryGoodsStockMangement(Integer goodsFatherId, String dateDuring,
                                         Integer inventoryType) {
        TreeSet<GbDistributerGoodsEntity> treeGoods = new TreeSet<>();

        String dateString = "全部";
        String startDate = "";
        String stopDate = "";


        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsFatherId", goodsFatherId);
        map.put("inventoryType", inventoryType);
        map.put("depType", getGbDepartmentTypeMendian());
        System.out.println("mappapdpdpdp" + map);

        //inPeriod
        if (dateDuring.equals("in")) {
            if (inventoryType.equals(getDISGoodsInventroyDaily())) {
//                map.put("startDate", formatWhatDay(0));
                map.put("date", formatWhatDay(0));

                dateString = formatWhatDayString(0);
                startDate = formatWhatDay(0);
            } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {
                map.put("startDate", formatWhatDay(-7));
                map.put("notDayuStopDate", formatWhatDay(0));
                dateString = formatWhatDayString(-7) + "-" + formatWhatDayString(0);
                startDate = formatWhatDay(-7);
                stopDate = formatWhatDay(0);

            } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {
                map.put("startDate", formatWhatDay(-30));
                map.put("notDayuStopDate", formatWhatDay(0));
                dateString = formatWhatDayString(-30) + "-" + formatWhatDayString(0);
                startDate = formatWhatDay(-30);
                stopDate = formatWhatDay(0);

            }
        }
        //one Period
        if (dateDuring.equals("one")) {
            if (inventoryType.equals(getDISGoodsInventroyDaily())) {
//                map.put("startDate", formatWhatDay(-1));
//                map.put("stopDate", formatWhatDay(0));
                map.put("date", formatWhatDay(-1));

                dateString = formatWhatDayString(-1);
                startDate = formatWhatDay(-1);
                stopDate = formatWhatDay(0);
            } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {
                map.put("startDate", formatWhatDay(-14));
                map.put("stopDate", formatWhatDay(-7));
                dateString = formatWhatDayString(-14) + "-" + formatWhatDayString(-7);
                startDate = formatWhatDay(-14);
                stopDate = formatWhatDay(-7);
            } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {
                map.put("startDate", formatWhatDay(-60));
                map.put("stopDate", formatWhatDay(-30));
                dateString = formatWhatDayString(-60) + "-" + formatWhatDayString(-30);
                startDate = formatWhatDay(-60);
                stopDate = formatWhatDay(-30);
            }


        }
        //two Period
        if (dateDuring.equals("two")) {
            if (inventoryType.equals(getDISGoodsInventroyDaily())) {
                map.put("date", formatWhatDay(-2));
                dateString = formatWhatDayString(-2);
//                startDate = formatWhatDay(-2);
//                stopDate = formatWhatDay(-1);
            } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {
                map.put("startDate", formatWhatDay(-21));
                map.put("stopDate", formatWhatDay(-14));
                dateString = formatWhatDayString(-21) + "-" + formatWhatDayString(-14);
                startDate = formatWhatDay(-14);
                stopDate = formatWhatDay(-7);
            } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {
                map.put("startDate", formatWhatDay(-90));
                map.put("stopDate", formatWhatDay(-60));
                dateString = formatWhatDayString(-90) + "-" + formatWhatDayString(-60);
                startDate = formatWhatDay(-90);
                stopDate = formatWhatDay(-60);
            }
        }

        //three Period
        if (dateDuring.equals("three")) {
            if (inventoryType.equals(getDISGoodsInventroyDaily())) {

                map.put("date", formatWhatDay(-3));

                dateString = formatWhatDayString(-3);
                startDate = formatWhatDay(-3);
                stopDate = formatWhatDay(-2);
            } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {
                map.put("startDate", formatWhatDay(-28));
                map.put("stopDate", formatWhatDay(-21));
                dateString = formatWhatDayString(-28) + "-" + formatWhatDayString(-21);
                startDate = formatWhatDay(-28);
                stopDate = formatWhatDay(-21);
            } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {
                map.put("startDate", formatWhatDay(-120));
                map.put("stopDate", formatWhatDay(-90));
                dateString = formatWhatDayString(-120) + "-" + formatWhatDayString(-90);
                startDate = formatWhatDay(-120);
                stopDate = formatWhatDay(-90);
            }
        }

//exceed Period
        if (dateDuring.equals("exceed")) {
            if (inventoryType.equals(getDISGoodsInventroyDaily())) {
                map.put("stopDate", formatWhatDay(-3));
                dateString = formatWhatDayString(-3) + "以前";
                startDate = "-1";
                stopDate = formatWhatDay(-3);
            } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {
                map.put("stopDate", formatWhatDay(-28));
                dateString = formatWhatDayString(-28) + "以前";
                startDate = "-1";
                stopDate = formatWhatDay(-28);
            } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {
                map.put("stopDate", formatWhatDay(-120));
                dateString = formatWhatDayString(-120) + "以前";
                startDate = "-1";
                stopDate = formatWhatDay(-120);
            }
        }

        System.out.println("mapppp" + map);
        Integer integer11 = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer11 > 0) {
            List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
            treeGoods.addAll(goodsEntities);
        }

        for (GbDistributerGoodsEntity goodsEntity : treeGoods) {
            //1 求总wasteTotal
            Map<String, Object> map1 = new HashMap<>();
            map1.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
            map1.put("depType", getGbDepartmentTypeMendian());
            if (dateDuring.equals("in")) {
                if (inventoryType.equals(getDISGoodsInventroyDaily())) {
                    map1.put("startDate", formatWhatDay(0));
                } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {
                    map1.put("startDate", formatWhatDay(-7));
                    map1.put("stopDate", formatWhatDay(0));
                } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {
                    map1.put("startDate", formatWhatDay(-30));
                    map1.put("stopDate", formatWhatDay(0));
                }
            }
            //one Period
            if (dateDuring.equals("one")) {
                if (inventoryType.equals(getDISGoodsInventroyDaily())) {
//                    map1.put("startDate", formatWhatDay(-1));
//                    map1.put("stopDate", formatWhatDay(0));
                    map1.put("date", formatWhatDay(-1));

                } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {
                    map1.put("startDate", formatWhatDay(-14));
                    map1.put("stopDate", formatWhatDay(-7));
                } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {
                    map1.put("startDate", formatWhatDay(-60));
                    map1.put("stopDate", formatWhatDay(-30));
                }
            }
            //two Period
            if (dateDuring.equals("two")) {
                if (inventoryType.equals(getDISGoodsInventroyDaily())) {
//                    map1.put("startDate", formatWhatDay(-2));
//                    map1.put("stopDate", formatWhatDay(-1));
                    map1.put("date", formatWhatDay(-2));

                } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {
                    map1.put("startDate", formatWhatDay(-21));
                    map1.put("stopDate", formatWhatDay(-14));
                } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {
                    map1.put("startDate", formatWhatDay(-90));
                    map1.put("stopDate", formatWhatDay(-60));
                }
            }
            //three Period
            if (dateDuring.equals("three")) {
                if (inventoryType.equals(getDISGoodsInventroyDaily())) {
//                    map1.put("startDate", formatWhatDay(-3));
//                    map1.put("stopDate", formatWhatDay(-2));
                    map1.put("date", formatWhatDay(-3));

                } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {
                    map1.put("startDate", formatWhatDay(-28));
                    map1.put("stopDate", formatWhatDay(-21));
                } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {
                    map1.put("startDate", formatWhatDay(-120));
                    map1.put("stopDate", formatWhatDay(-90));
                }
            }
            //three Period
            if (dateDuring.equals("exceed")) {
                if (inventoryType.equals(getDISGoodsInventroyDaily())) {
                    map1.put("stopDate", formatWhatDay(-3));
                } else if (inventoryType.equals(getDISGoodsInventroyWeek())) {
                    map1.put("stopDate", formatWhatDay(-28));
                } else if (inventoryType.equals(getDISGoodsInventroyMonth())) {
                    map1.put("stopDate", formatWhatDay(-120));
                }
            }


            Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map1);
            Double aDouble = 0.0;
            Double aDoubleWeight = 0.0;
            if (integer > 0) {
                aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map1);
                aDoubleWeight = gbDepGoodsStockService.queryDepGoodsRestWeightTotal(map1);
            }

            goodsEntity.setGoodsStockTotal(aDouble);
            goodsEntity.setGoodsStockTotalString(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            goodsEntity.setGoodsWeightTotal(aDoubleWeight);
            goodsEntity.setGoodsWeightTotalString(new BigDecimal(aDoubleWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        }
        TreeSet<GbDistributerGoodsEntity> abc = abcStockEvery(treeGoods);


        Map<String, Object> mapr = new HashMap<>();
        mapr.put("arr", abc);
//        mapr.put("dateString", dateString);
//        mapr.put("startDate", startDate);
        mapr.put("stopDate", stopDate);

        return R.ok().put("data", mapr);
    }

    @RequestMapping(value = "/getEveryGoodsStock", method = RequestMethod.POST)
    @ResponseBody
    public R getEveryGoodsStock(Integer goodsFatherId, Integer which, Integer disId, String searchDepIds) {
        Map<String, Object> map = new HashMap<>();

        if (which == 99) {
            map = getGoodsStockPeriodAll(goodsFatherId, disId, searchDepIds);
        } else {
            map = getGoodsStockPeriod(goodsFatherId, which, disId, searchDepIds);
        }
        return R.ok().put("data", map);
    }

    private Map<String, Object> getGoodsStockPeriodAll(Integer goodsFatherId, Integer disId,
                                                       String searchDepIds) {

//        Map<String, Object> mapDep = new HashMap<>();
//        mapDep.put("disId", disId);

        //in
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsFatherId", goodsFatherId);
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            List<String> idsGb = new ArrayList<>();
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("depFatherIds", idsGb);
                }
            }
        }
        map.put("restWeight", 0);
        map.put("dayuStatus", -1);
        System.out.println("wwwwwmammama" + map);
        Integer integer11 = gbDepGoodsStockService.queryGoodsStockCount(map);
        Double aDouble1 = 0.0;
        TreeSet<GbDistributerGoodsEntity> ccc = new TreeSet<>();
        if (integer11 > 0) {
            aDouble1 = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
            List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
            for (GbDistributerGoodsEntity goodsEntity : goodsEntities) {
                //1 求总wasteTotal
                Map<String, Object> map1 = new HashMap<>();
                map1.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                map1.put("restWeight", 0);
                if (!searchDepIds.equals("-1")) {
                    String[] arrGb = searchDepIds.split(",");
                    List<String> idsGb = new ArrayList<>();
                    for (String idGb : arrGb) {
                        idsGb.add(idGb);
                        if (idsGb.size() > 0) {
                            map1.put("depFatherIds", idsGb);
                        }
                    }
                }
                map1.put("dayuStatus", -1);

                Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map1);
                Double aDouble = 0.0;
                Double aDoubleWeight = 0.0;
                if (integer > 0) {
                    aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map1);
                    aDoubleWeight = gbDepGoodsStockService.queryDepGoodsRestWeightTotal(map1);
                }
                goodsEntity.setGoodsStockTotal(aDouble);
                goodsEntity.setGoodsStockTotalString(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsEntity.setGoodsWeightTotal(aDoubleWeight);
                goodsEntity.setGoodsWeightTotalString(new BigDecimal(aDoubleWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
            ccc.addAll(goodsEntities);

        }
        Map<String, Object> mapIn = new HashMap<>();
        mapIn.put("dateString", "全部");
        mapIn.put("arr", abcStockEvery(ccc));
        mapIn.put("total", new BigDecimal(aDouble1).setScale(1, BigDecimal.ROUND_HALF_UP));
        return mapIn;
    }

    private Map<String, Object> getGoodsStockPeriod(Integer goodsFatherId, Integer which, Integer disId,
                                                    String searchDepIds) {
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);

        //in
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsFatherId", goodsFatherId);
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            List<String> idsGb = new ArrayList<>();
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("depFatherIds", idsGb);
                }
            }
        }
        if (which == -4) {
            map.put("stopDate", formatWhatDay(which));
        } else {
            map.put("date", formatWhatDay(which));
        }
        map.put("dayuStatus", -1);
        map.put("restWeight", 0);
        System.out.println("whatisiisiis" + map);
        Integer integer11 = gbDepGoodsStockService.queryGoodsStockCount(map);
        Double aDouble1 = 0.0;
        TreeSet<GbDistributerGoodsEntity> ccc = new TreeSet<>();
        if (integer11 > 0) {
            aDouble1 = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
            List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
            for (GbDistributerGoodsEntity goodsEntity : goodsEntities) {
                //1 求总wasteTotal
                Map<String, Object> map1 = new HashMap<>();
                map1.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                if (!searchDepIds.equals("-1")) {
                    String[] arrGb = searchDepIds.split(",");
                    List<String> idsGb = new ArrayList<>();
                    for (String idGb : arrGb) {
                        idsGb.add(idGb);
                        if (idsGb.size() > 0) {
                            map1.put("depFatherIds", idsGb);
                        }
                    }
                }
                if (which == -4) {
                    map1.put("stopDate", formatWhatDay(which));
                } else {
                    map1.put("date", formatWhatDay(which));
                }


                map1.put("dayuStatus", -1);
                System.out.println("kkkkkkkkkk" + map1);
                Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map1);
                Double aDouble = 0.0;
                Double aDoubleWeight = 0.0;
                if (integer > 0) {
                    aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map1);
                    System.out.println("dayustatuuuss" + map1);
                    aDoubleWeight = gbDepGoodsStockService.queryDepGoodsRestWeightTotal(map1);
                }
                goodsEntity.setGoodsStockTotal(aDouble);
                goodsEntity.setGoodsStockTotalString(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsEntity.setGoodsWeightTotal(aDoubleWeight);
                goodsEntity.setGoodsWeightTotalString(new BigDecimal(aDoubleWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
            ccc.addAll(goodsEntities);
//            ;
        }
        Map<String, Object> mapIn = new HashMap<>();
        if (which == 0) {
            mapIn.put("dateString", "今天");
        }
        if (which == -1) {
            mapIn.put("dateString", "1天");
        }
        if (which == -2) {
            mapIn.put("dateString", "2天");
        }
        if (which == -3) {
            mapIn.put("dateString", "3天");
        }
        if (which == -4) {
            mapIn.put("dateString", "3天以上");
        }
        mapIn.put("arr", abcStockEvery(ccc));
        mapIn.put("total", new BigDecimal(aDouble1).setScale(1, BigDecimal.ROUND_HALF_UP));
        return mapIn;
    }


    private TreeSet<GbDepartmentEntity> abcStockDepartment(TreeSet<GbDepartmentEntity> goodsEntities) {

        TreeSet<GbDepartmentEntity> ts = new TreeSet<>(new Comparator<GbDepartmentEntity>() {
            @Override
            public int compare(GbDepartmentEntity o1, GbDepartmentEntity o2) {
                int result;
                if (o2.getDepStockSubtotal() - o1.getDepStockSubtotal() < 0) {
                    result = -1;
                } else if (o2.getDepStockSubtotal() - o1.getDepStockSubtotal() > 0) {
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


    private TreeSet<GbDistributerGoodsEntity> abcStockEvery(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                System.out.println("abckkcstockk" + o2.getGoodsStockTotal());
                System.out.println("abckkcstockk111" + o1.getGoodsStockTotal());
                int result;
                if (o2.getGoodsStockTotal() - o1.getGoodsStockTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsStockTotal() - o1.getGoodsStockTotal() > 0) {
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


    @RequestMapping(value = "/getMendianStockStatics", method = RequestMethod.POST)
    @ResponseBody
    public R getMendianStockStatics(Integer disId, String startDate, String stopDate) {

        List<Map<String, Object>> depList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("type", 3);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
        for (GbDepartmentEntity dep : departmentEntities) {
            Map<String, Object> mapDep = new HashMap<>();
            mapDep.put("dep", dep);

            Map<String, Object> map0 = new HashMap<>();
            map0.put("depFatherId", dep.getGbDepartmentId());
            map0.put("startDate", startDate);
            map0.put("stopDate", stopDate);
            Integer aDoubleTotalCount = gbDepGoodsStockService.queryGoodsStockCount(map0);
            if (aDoubleTotalCount > 0) {
                Double aDoubleCost = gbDepGoodsStockService.queryDepGoodsRestTotal(map0);
                mapDep.put("total", aDoubleCost.toString());
            } else {
                mapDep.put("total", "0");
            }

            Map<String, Object> map1 = new HashMap<>();
            map1.put("depFatherId", dep.getGbDepartmentId());
            map1.put("startDate", startDate);
            map1.put("stopDate", stopDate);
            map1.put("inventoryType", getDISGoodsInventroyDaily());
            Integer aDoubleCostCount = gbDepGoodsStockService.queryGoodsStockCount(map1);
            if (aDoubleCostCount > 0) {
                Double aDoubleCost = gbDepGoodsStockService.queryDepGoodsRestTotal(map1);
                mapDep.put("daily", aDoubleCost.toString());
            } else {
                mapDep.put("daily", "0");
            }

            Map<String, Object> map2 = new HashMap<>();
            map2.put("depFatherId", dep.getGbDepartmentId());
            map2.put("startDate", startDate);
            map2.put("stopDate", stopDate);
            map2.put("inventoryType", getDISGoodsInventroyWeek());
            Integer aDoubleWeekCount = gbDepGoodsStockService.queryGoodsStockCount(map2);
            if (aDoubleWeekCount > 0) {
                Double aDoubleCost = gbDepGoodsStockService.queryDepGoodsRestTotal(map2);
                mapDep.put("week", aDoubleCost.toString());
            } else {
                mapDep.put("week", "0");
            }

            Map<String, Object> map3 = new HashMap<>();
            map3.put("depFatherId", dep.getGbDepartmentId());
            map3.put("startDate", startDate);
            map3.put("stopDate", stopDate);
            map3.put("inventoryType", getDISGoodsInventroyMonth());
            Integer aDoubleMonthCount = gbDepGoodsStockService.queryGoodsStockCount(map3);
            if (aDoubleMonthCount > 0) {
                Double aDoubleCost = gbDepGoodsStockService.queryDepGoodsRestTotal(map3);
                mapDep.put("month", aDoubleCost.toString());
            } else {
                mapDep.put("month", "0");
            }
            depList.add(mapDep);
        }
        return R.ok().put("data", depList);
    }


    @RequestMapping(value = "/getStockDepGoodsBusiness", method = RequestMethod.POST)
    @ResponseBody
    public R getStockDepGoodsBusiness(Integer depGoodsId, String startDate, String stopDate, Integer type) {

        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depGoodsId);
        Integer gbDdgDisGoodsId = departmentDisGoodsEntity.getGbDdgDisGoodsId();
        Integer gbDdgDepartmentId = departmentDisGoodsEntity.getGbDdgDepartmentId();

        List<GbDepartmentGoodsStockReduceEntity> reduceEntities = new ArrayList<>();
        Map<String, Object> mapDisGoods = new HashMap<>();
        mapDisGoods.put("depGoodsId", depGoodsId);
        mapDisGoods.put("startDate", startDate);
        mapDisGoods.put("stopDate", stopDate);
        mapDisGoods.put("equalType", type);
        System.out.println("tyepepe" + mapDisGoods);
        Integer count = gbDepartmentStockReduceService.queryReduceTypeCount(mapDisGoods);
        if (count > 0) {
            reduceEntities = gbDepartmentStockReduceService.queryStockReduceListByParams(mapDisGoods);
            for (GbDepartmentGoodsStockReduceEntity reduce : reduceEntities) {
                GbDepartmentGoodsStockEntity gbDepGoodsStockEntity = reduce.getGbDepartmentGoodsStockEntity();
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsId", gbDdgDisGoodsId);
                map.put("fromDepId", gbDdgDepartmentId);
                map.put("fromStockId", gbDepGoodsStockEntity.getGbDepartmentGoodsStockId());
                Double aDouble = gbDepGoodsStockService.queryDepStockWeightTotal(map);
                gbDepGoodsStockEntity.setOutWeightTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
        }
        return R.ok().put("data", reduceEntities);
    }


    @RequestMapping(value = "/getDepGoodsBusiness", method = RequestMethod.POST)
    @ResponseBody
    public R getDepGoodsBusiness(Integer depGoodsId, String startDate, String stopDate, Integer type) {

        Map<String, Object> mapDisGoods = new HashMap<>();
        mapDisGoods.put("depGoodsId", depGoodsId);
        mapDisGoods.put("startDate", startDate);
        mapDisGoods.put("stopDate", stopDate);
        mapDisGoods.put("dayuStatus", -1);
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockWithReduceList(mapDisGoods);

        return R.ok().put("data", stockEntities);
    }

    /**
     * 获取自采商品每日业务数据
     *
     * @param disGoodsId
     * @param depFatherId
     * @return
     */
    @RequestMapping(value = "/getMendianGoodsBusiness", method = RequestMethod.POST)
    @ResponseBody
    public R getMendianGoodsBusiness(Integer disGoodsId, Integer depFatherId) {

        //1，查询该商品的全部库存批次
        Map<String, Object> map = new HashMap<>();
//        map.put("settleId", -1);
        map.put("disGoodsId", disGoodsId);
        map.put("depFatherId", depFatherId);
        map.put("dayuStatus", -1);
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);


        //2，查询该商品的业务数据
        Map<String, Object> map1 = new HashMap<>();
//        map1.put("settleId", -1);
        map1.put("businessDisGoodsId", disGoodsId);
        map1.put("depFatherId", depFatherId);
        List<GbDepartmentGoodsStockReduceEntity> reduceEntities = gbDepartmentStockReduceService.queryStockReduceListByParams(map1);
        List<GbDepartmentGoodsStockBusinessEntity> resultData = new ArrayList<>();

        if (stockEntities.size() > 0) {
            for (int i = 0; i < stockEntities.size(); i++) {
                //把库存批次改成业务条目
                GbDepartmentGoodsStockBusinessEntity businessEntity = new GbDepartmentGoodsStockBusinessEntity();
                businessEntity.setGbBusinessFullTime(stockEntities.get(i).getGbDgsFullTime());
                businessEntity.setGbBusinessWeight(stockEntities.get(i).getGbDgsWeight());
                businessEntity.setGbBusinessType("stock");
                String subName = stockEntities.get(i).getGbDepartmentOrdersEntity().getOrderDepartment().getGbDepartmentName();
                businessEntity.setGbApplyDepartment(subName);
//                if (i == 0) {
//                    businessEntity.setGbBusinessResultWeight(new BigDecimal(stockEntities.get(i).getGbDgsWeight()));
//                }
                resultData.add(businessEntity);
            }
        }

        if (reduceEntities.size() > 0) {
            for (GbDepartmentGoodsStockReduceEntity reduceEntity : reduceEntities) {
                //把业务数据改为业务条目
                GbDepartmentGoodsStockBusinessEntity businessEntity = new GbDepartmentGoodsStockBusinessEntity();
                businessEntity.setGbBusinessFullTime(reduceEntity.getGbDgsrFullTime());
                businessEntity.setGbApplyDepartment(reduceEntity.getGbDepartmentEntity().getGbDepartmentName());
                if (reduceEntity.getGbDgsrType().equals(getGbDepartGoodsStockReduceTypeProduce())) {
                    businessEntity.setGbBusinessWeight(reduceEntity.getGbDgsrProduceWeight());
                    businessEntity.setGbBusinessType("produce");
                }
                if (reduceEntity.getGbDgsrType().equals(getGbDepartGoodsStockReduceTypeWaste())) {
                    businessEntity.setGbBusinessWeight(reduceEntity.getGbDgsrWasteWeight());
                    businessEntity.setGbBusinessType("waste");
                }
                if (reduceEntity.getGbDgsrType().equals(getGbDepartGoodsStockReduceTypeLoss())) {
                    businessEntity.setGbBusinessWeight(reduceEntity.getGbDgsrLossWeight());
                    businessEntity.setGbBusinessType("loss");
                }
                if (reduceEntity.getGbDgsrType().equals(getGbDepartGoodsStockReduceTypeReturn())) {
                    businessEntity.setGbBusinessWeight(reduceEntity.getGbDgsrReturnWeight());
                    businessEntity.setGbBusinessType("return");
                }

                resultData.add(businessEntity);
            }

        }

        //3，按照业务时间排序
        resultData = resultData.stream().sorted(Comparator.comparing(GbDepartmentGoodsStockBusinessEntity::getGbBusinessFullTime).thenComparing(GbDepartmentGoodsStockBusinessEntity::getGbBusinessFullTime)).collect(Collectors.toList());

        //4，计算每一个业务的剩余数量
//        BigDecimal gbBusinessWeight = new BigDecimal(0);
//            for(int i = 0;i < resultData.size() - 1; i++){
//                BigDecimal stockWeight = resultData.get(i).getGbBusinessResultWeight() ;
//                String gbBusinessType = resultData.get(i + 1).getGbBusinessType();
//            if (gbBusinessType.equals("stock")) {
//                gbBusinessWeight =  stockWeight.add(new BigDecimal(resultData.get(i + 1).getGbBusinessWeight())).setScale(1, BigDecimal.ROUND_HALF_UP) ;
//            }else{
//
//                gbBusinessWeight =  stockWeight.subtract(new BigDecimal(resultData.get(i + 1).getGbBusinessWeight())).setScale(1, BigDecimal.ROUND_HALF_UP) ;
//            }
//                System.out.println("gdkfga" + gbBusinessWeight);
//                resultData.get(i + 1).setGbBusinessResultWeight(gbBusinessWeight);
//        }

        return R.ok().put("data", resultData);

    }


    @RequestMapping(value = "/getPurDepartmentGoodsBusinessType", method = RequestMethod.POST)
    @ResponseBody
    public R getPurDepartmentGoodsBusinessType(Integer disGoodsId, Integer depId, String type,
                                               String startDate, String stopDate, Integer nxDisId) {

        if (type.equals("stock")) {

            double doutbleCost = 0;
            double doutbleCostV = 0;
            double v = 0;
            double doutbleRest = 0;
            double doutbleRestV = 0;
            String maxPrice = "0";
            String minPrice = "0";
            String perPrice = "0";
            int purCount = 0;
            List<GbDistributerPurchaseGoodsEntity> goodsEntities = new ArrayList<>();


            Map<String, Object> map = new HashMap<>();
            if (depId != -1) {
                map.put("purDepId", depId);
            }
            if (nxDisId != -1) {
                map.put("nxDisId", nxDisId);
            }

            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
            map.put("disGoodsId", disGoodsId);


            purCount = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
            if (purCount > 0) {
                System.out.println("caigoushushul" + map);
                doutbleCostV = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);
                doutbleCost = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(map);
                v = doutbleCostV / doutbleCost;
                perPrice = new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                maxPrice = gbDepGoodsStockService.queryDepStockMaxDgsPrice(map);
                minPrice = gbDepGoodsStockService.queryDepStockMinDgsPrice(map);
                goodsEntities = gbDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
            }

            Map<String, Object> mapStock = new HashMap<>();
            mapStock.put("disGoodsId", disGoodsId);
            System.out.println("stockkmapapa" + mapStock);
            doutbleRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapStock);
            doutbleRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(mapStock);

            Map<String, Object> mapResult = new HashMap<>();

            mapResult.put("arr", goodsEntities);

            mapResult.put("totalCost", new BigDecimal(doutbleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalCostSubtotal", new BigDecimal(doutbleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalRest", new BigDecimal(doutbleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalRestSubtotal", new BigDecimal(doutbleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("maxPrice", maxPrice);
            mapResult.put("minPrice", minPrice);
            mapResult.put("perPrice", perPrice);
            mapResult.put("purCount", purCount);


            return R.ok().put("data", mapResult);
        }
        if (type.equals("out")) {
            Integer howManyDaysInPeriod = 0;
            if (!startDate.equals(stopDate)) {
                howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
            }

            List<Map<String, Object>> list = new ArrayList<>();
            if (howManyDaysInPeriod > 0) {
                for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                    // dateList
                    String whichDay = "";
                    if (i == 0) {
                        whichDay = startDate;
                    } else {
                        whichDay = afterWhatDay(startDate, i);
                    }
                    Map<String, Object> mapResult = new HashMap<>();
                    mapResult.put("date", whichDay);
                    mapResult.put("week", changeDateToWeek(whichDay));
                    double total = 0;
                    Map<String, Object> mapDisGoods = new HashMap<>();
                    mapDisGoods.put("disGoodsId", disGoodsId);
                    mapDisGoods.put("date", whichDay);
                    if (depId != -1) {
                        mapDisGoods.put("fromDepId", depId);
                    }
                    if (nxDisId != -1) {
                        mapDisGoods.put("nxDisId", nxDisId);
                    }
                    mapDisGoods.put("dayuStatus", -1);
                    mapDisGoods.put("depFatherIdNotEqual", depId);
                    List<GbDepartmentGoodsStockEntity> stockEntities = new ArrayList<>();
                    Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);

                    if (count > 0) {
                        stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(mapDisGoods);
                        total = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
                    }

                    mapResult.put("arr", stockEntities);
                    mapResult.put("total", total);
                    if (stockEntities.size() > 0) {
                        list.add(mapResult);
                    }

                }
            } else {
                Map<String, Object> mapResult = new HashMap<>();
                mapResult.put("date", startDate);
                mapResult.put("week", changeDateToWeek(startDate));
                double total = 0;
                Map<String, Object> mapDisGoods = new HashMap<>();
                mapDisGoods.put("disGoodsId", disGoodsId);
                mapDisGoods.put("date", startDate);
                mapDisGoods.put("fromDepId", depId);
                mapDisGoods.put("dayuStatus", -1);
                mapDisGoods.put("depFatherIdNotEqual", depId);
                List<GbDepartmentGoodsStockEntity> stockEntities = new ArrayList<>();
                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);

                if (count > 0) {
                    stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(mapDisGoods);
                    total = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
                }

                mapResult.put("arr", stockEntities);
                mapResult.put("total", total);
                if (stockEntities.size() > 0) {
                    list.add(mapResult);
                }
            }
            return R.ok().put("data", list);

        }
        return R.ok();

    }

    @RequestMapping(value = "/getDisGoodsOutEveryDay", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsOutEveryDay(Integer disGoodsId, String startDate, String stopDate, Integer depId, Integer searchDepId) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        if (howManyDaysInPeriod > 0) {
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                Map<String, Object> mapResult = new HashMap<>();
                mapResult.put("date", whichDay);
                mapResult.put("week", changeDateToWeek(whichDay));
                double total = 0;
                Map<String, Object> mapDisGoods = new HashMap<>();
                mapDisGoods.put("disGoodsId", disGoodsId);
                mapDisGoods.put("date", whichDay);
                mapDisGoods.put("depType", getGbDepartmentTypeMendian());
                mapDisGoods.put("dayuStatus", -1);
                if (searchDepId != -1) {
                    mapDisGoods.put("depId", searchDepId);
                }
                if (depId != -1) {
                    mapDisGoods.put("fromDepId", depId);
                }
                System.out.println("deptyytt" + mapDisGoods);
                List<GbDepartmentGoodsStockEntity> stockEntities = new ArrayList<>();
                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);

                if (count > 0) {
                    stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(mapDisGoods);
                    total = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
                }

                mapResult.put("arr", stockEntities);
                mapResult.put("total", total);
                if (stockEntities.size() > 0) {
                    list.add(mapResult);
                }

            }
        } else {
            Map<String, Object> mapResult = new HashMap<>();
            mapResult.put("date", startDate);
            mapResult.put("week", changeDateToWeek(startDate));
            double total = 0;
            Map<String, Object> mapDisGoods = new HashMap<>();
            mapDisGoods.put("disGoodsId", disGoodsId);
            mapDisGoods.put("date", startDate);
            mapDisGoods.put("depType", getGbDepartmentTypeMendian());
            mapDisGoods.put("dayuStatus", -1);
            if (searchDepId != -1) {
                mapDisGoods.put("depId", searchDepId);
            }
            if (depId != -1) {
                mapDisGoods.put("fromDepId", depId);
            }
            List<GbDepartmentGoodsStockEntity> stockEntities = new ArrayList<>();
            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);

            if (count > 0) {
                stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(mapDisGoods);
                total = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
            }

            mapResult.put("arr", stockEntities);
            mapResult.put("total", total);
            if (stockEntities.size() > 0) {
                list.add(mapResult);
            }
        }
        return R.ok().put("data", list);

    }


//
//    @RequestMapping(value = "/getStockGoodsPurList", method = RequestMethod.POST)
//    @ResponseBody
//    public R getStockGoodsPurList(Integer disGoodsId, Integer depId,
//                                                 String startDate, String stopDate) {
//        Map<String, Object> mapGoods = new HashMap<>();
//        mapGoods.put("disGoodsId", disGoodsId);
//        mapGoods.put("depFatherId", depId);
//        mapGoods.put("startDate", startDate);
//        mapGoods.put("stopDate", stopDate);
//        List<GbDepartmentGoodsStockEntity> departmentGoodsStockEntities = gbDepGoodsStockService.queryGoodsStockByParams(mapGoods);
//
//        double doutbleCost = 0;
//        double doutbleCostV = 0;
//        double v = 0;
//        double doutbleRest = 0;
//        double doutbleRestV = 0;
//        String maxPrice = "0";
//        String minPrice = "0";
//        String perPrice = "0";
//        int purCount = 0;
//        Map<String, Object> map = new HashMap<>();
//
//        if (depId != -1) {
//            map.put("purDepId", depId);
//        }
//
//        map.put("startDate", startDate);
//        map.put("stopDate", stopDate);
//        map.put("disGoodsId", disGoodsId);
//
//        purCount = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
//        if (purCount > 0) {
//            System.out.println("caigoushushul" + map);
//            doutbleCostV = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);
//            doutbleCost = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(map);
//            v = doutbleCostV / doutbleCost;
//            perPrice = new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//            maxPrice = gbDepGoodsStockService.queryDepStockMaxDgsPrice(map);
//            minPrice = gbDepGoodsStockService.queryDepStockMinDgsPrice(map);
//        }
//
//        Map<String, Object> mapStock = new HashMap<>();
//        mapStock.put("disGoodsId", disGoodsId);
//        System.out.println("stockkmapapa" + mapStock);
//        doutbleRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapStock);
//        doutbleRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(mapStock);
//        Map<String, Object> mapResult = new HashMap<>();
//
//        mapResult.put("arr", departmentGoodsStockEntities);
//
//
//        mapResult.put("totalCost", new BigDecimal(doutbleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//        mapResult.put("totalCostSubtotal", new BigDecimal(doutbleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//        mapResult.put("totalRest", new BigDecimal(doutbleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//        mapResult.put("totalRestSubtotal", new BigDecimal(doutbleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//        mapResult.put("maxPrice", maxPrice);
//        mapResult.put("minPrice", minPrice);
//        mapResult.put("perPrice", perPrice);
//        mapResult.put("purCount", purCount);
//
//
//        return R.ok().put("data", mapResult);
//
//
//    }
//
//
//    @RequestMapping(value = "/getStockDepartmentGoodsOutEveryDay", method = RequestMethod.POST)
//    @ResponseBody
//    public R getStockDepartmentGoodsOutEveryDay(Integer disGoodsId, Integer depId,
//                                                 String startDate, String stopDate) {
//        Integer howManyDaysInPeriod = 0;
//        if (!startDate.equals(stopDate)) {
//            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
//        }
//
//        List<Map<String, Object>> list = new ArrayList<>();
//        if (howManyDaysInPeriod > 0) {
//            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
//                // dateList
//                String whichDay = "";
//                if (i == 0) {
//                    whichDay = startDate;
//                } else {
//                    whichDay = afterWhatDay(startDate, i);
//                }
//                Map<String, Object> mapResult = new HashMap<>();
//                mapResult.put("date", whichDay);
//                mapResult.put("week", changeDateToWeek(whichDay));
//                double total = 0;
//                Map<String, Object> mapDisGoods = new HashMap<>();
//                mapDisGoods.put("disGoodsId", disGoodsId);
//                mapDisGoods.put("date", whichDay);
//                mapDisGoods.put("fromDepId", depId);
//                mapDisGoods.put("dayuStatus", -1);
//                mapDisGoods.put("depFatherIdNotEqual", depId);
//                List<GbDepartmentGoodsStockEntity> stockEntities = new ArrayList<>();
//                Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
//
//                if (count > 0) {
//                    stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(mapDisGoods);
//                    total = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
//                }
//
//                mapResult.put("arr", stockEntities);
//                mapResult.put("total", total);
//                if (stockEntities.size() > 0) {
//                    list.add(mapResult);
//                }
//
//            }
//        } else {
//            Map<String, Object> mapResult = new HashMap<>();
//            mapResult.put("date", startDate);
//            mapResult.put("week", changeDateToWeek(startDate));
//            double total = 0;
//            Map<String, Object> mapDisGoods = new HashMap<>();
//            mapDisGoods.put("disGoodsId", disGoodsId);
//            mapDisGoods.put("date", startDate);
//            mapDisGoods.put("fromDepId", depId);
//            mapDisGoods.put("dayuStatus", -1);
//            mapDisGoods.put("depFatherIdNotEqual", depId);
//            List<GbDepartmentGoodsStockEntity> stockEntities = new ArrayList<>();
//            Integer count = gbDepGoodsStockService.queryGoodsStockCount(mapDisGoods);
//
//            if (count > 0) {
//                stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(mapDisGoods);
//                total = gbDepGoodsStockService.queryDepStockWeightTotal(mapDisGoods);
//            }
//
//            mapResult.put("arr", stockEntities);
//            mapResult.put("total", total);
//            if (stockEntities.size() > 0) {
//                list.add(mapResult);
//            }
//        }
//        return R.ok().put("data", list);
//
//
//    }

    /**
     * 管理端，门店端，库房获取商品每日业务数据
     *
     * @param disGoodsId
     * @param fromDepId
     * @return
     */
//    @RequestMapping(value = "/getStockDepartmentGoodsBusiness", method = RequestMethod.POST)
//    @ResponseBody
//    public R getStockDepartmentGoodsBusiness(Integer disGoodsId, Integer fromDepId, String type,
//                                             String startDate, String stopDate) {
//
//        Boolean ifShowScale = false;
//        BigDecimal countScale = new BigDecimal(0);
//        Map<String, Object> mapGoods = new HashMap<>();
//        mapGoods.put("depId", fromDepId);
//        mapGoods.put("disGoodsId", disGoodsId);
//        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(mapGoods);
//        System.out.println("ziisiss" + departmentDisGoodsEntities.size());
//        if (departmentDisGoodsEntities.size() > 0) {
//            BigDecimal showScale = new BigDecimal(departmentDisGoodsEntities.get(0).getGbDdgShowStandardScale());
//            if (showScale.compareTo(BigDecimal.ZERO) != 0) {
//                System.out.println("budenennennenenen");
//                ifShowScale = true;
//                countScale = showScale;
//            }
//        }
//
//        System.out.println("scallelelleel" + ifShowScale);
//
//        List<GbDepartmentGoodsStockBusinessEntity> resultData = new ArrayList<>();
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("settleId", -1);
//        map.put("disGoodsId", disGoodsId);
//        map.put("stockDepId", fromDepId);
//        map.put("depId", fromDepId);
//
//        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);
//        if (stockEntities.size() > 0) {
//            for (int i = 0; i < stockEntities.size(); i++) {
//                //把库存批次改成业务条目
//                GbDepartmentGoodsStockBusinessEntity businessEntity = new GbDepartmentGoodsStockBusinessEntity();
//                businessEntity.setGbBusinessFullTime(stockEntities.get(i).getGbDgsFullTime());
//                businessEntity.setGbBusinessType("stock");
//                BigDecimal weightData = new BigDecimal(stockEntities.get(i).getGbDgsWeight());
//                System.out.println("abdd" + weightData);
//                if (ifShowScale) {
//                    weightData = weightData.divide(countScale, 2, BigDecimal.ROUND_HALF_UP);
//                }
//                businessEntity.setGbBusinessWeight(weightData.toString());
//                businessEntity.setGbBusinessResultWeight(weightData.setScale(2, BigDecimal.ROUND_HALF_UP));
//                resultData.add(businessEntity);
//            }
//        }
//
//
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("settleId", -1);
//        map1.put("disGoodsId", disGoodsId);
//        map1.put("fromDepId", fromDepId);
//        map1.put("startDate", startDate);
//        map1.put("stopDate", stopDate);
//        List<GbDepartmentGoodsStockEntity> stockEntitiesout = gbDepGoodsStockService.queryGoodsStockByParams(map1);
//
//        BigDecimal total = new BigDecimal(0);
//        if (stockEntitiesout.size() > 0) {
//            for (int i = 0; i < stockEntitiesout.size(); i++) {
//                //把库存批次改成业务条目
//                GbDepartmentGoodsStockBusinessEntity businessEntity = new GbDepartmentGoodsStockBusinessEntity();
//                if (stockEntitiesout.get(i).getGbDgsGbFromDepartmentId() != -1) {
//                    businessEntity.setGbBusinessFullTime(stockEntitiesout.get(i).getGbDgsOutFullTime());
//                } else {
//                    businessEntity.setGbBusinessFullTime(stockEntitiesout.get(i).getGbDgsFullTime());
//                }
//                BigDecimal decimal = new BigDecimal(stockEntitiesout.get(i).getGbDgsWeight());
//                if (ifShowScale) {
//                    decimal = decimal.divide(countScale, 2, BigDecimal.ROUND_HALF_UP);
//                }
//
//                businessEntity.setGbBusinessWeight(decimal.toString());
//                businessEntity.setGbBusinessType("out");
//                String subName = stockEntitiesout.get(i).getGbDepartmentOrdersEntity().getOrderDepartment().getGbDepartmentName();
//                businessEntity.setGbApplyDepartment(subName);
//                resultData.add(businessEntity);
//            }
//        }
//
//        Map<String, Object> map11 = new HashMap<>();
//        map11.put("disGoodsId", disGoodsId);
//        map11.put("fromDepId", fromDepId);
//        map11.put("startDate", startDate);
//        map11.put("stopDate", stopDate);
//        List<GbDepartmentGoodsStockRecordEntity> stockEntitiesout1 = gbDepGoodsStockRecordService.queryGoodsStockListByParams(map11);
//        BigDecimal total1 = new BigDecimal(0);
//        if (stockEntitiesout1.size() > 0) {
//            for (int i = 0; i < stockEntitiesout1.size(); i++) {
//                //把库存批次改成业务条目
//                GbDepartmentGoodsStockBusinessEntity businessEntity = new GbDepartmentGoodsStockBusinessEntity();
//                businessEntity.setGbBusinessFullTime(stockEntitiesout1.get(i).getGbDgscFullTime());
//
//                BigDecimal weightData = new BigDecimal(stockEntitiesout1.get(i).getGbDgscWeight());
//                if (ifShowScale) {
//                    weightData = weightData.divide(countScale, 2, BigDecimal.ROUND_HALF_UP);
//                }
//
//                businessEntity.setGbBusinessWeight(weightData.toString());
//                businessEntity.setGbBusinessType("out");
//                String subName = stockEntitiesout1.get(i).getGbDepartmentOrdersEntity().getOrderDepartment().getGbDepartmentName();
//                businessEntity.setGbApplyDepartment(subName);
//                resultData.add(businessEntity);
//            }
//        }
//
//
//        List<GbDepartmentGoodsStockReduceEntity> reduceEntities = gbDepartmentStockReduceService.queryStockReduceListByParams(map);
//        if (reduceEntities.size() > 0) {
//            for (GbDepartmentGoodsStockReduceEntity reduceEntity : reduceEntities) {
//
//                GbDepartmentGoodsStockBusinessEntity businessEntity = new GbDepartmentGoodsStockBusinessEntity();
//                businessEntity.setGbBusinessFullTime(reduceEntity.getGbDgsrFullTime());
//                BigDecimal businessWeight = new BigDecimal(0);
//                if (reduceEntity.getGbDgsrType().equals(getGbDepartGoodsStockReduceTypeProduce())) {
//                    businessWeight = new BigDecimal(reduceEntity.getGbDgsrProduceWeight());
//                    if (ifShowScale) {
//                        businessWeight = businessWeight.divide(countScale, 2, BigDecimal.ROUND_HALF_UP);
//                    }
//                    businessEntity.setGbBusinessType("cost");
//                }
//                if (reduceEntity.getGbDgsrType().equals(getGbDepartGoodsStockReduceTypeWaste())) {
//                    businessWeight = new BigDecimal(reduceEntity.getGbDgsrWasteWeight());
//                    if (ifShowScale) {
//                        businessWeight = businessWeight.divide(countScale, 2, BigDecimal.ROUND_HALF_UP);
//                    }
//                    businessEntity.setGbBusinessType("waste");
//                }
//                if (reduceEntity.getGbDgsrType().equals(getGbDepartGoodsStockReduceTypeLoss())) {
//                    businessWeight = new BigDecimal(reduceEntity.getGbDgsrLossWeight());
//                    if (ifShowScale) {
//                        businessWeight = businessWeight.divide(countScale, 2, BigDecimal.ROUND_HALF_UP);
//                    }
//                    businessEntity.setGbBusinessType("loss");
//                }
//                if (reduceEntity.getGbDgsrType().equals(getGbDepartGoodsStockReduceTypeReturn())) {
//                    businessWeight = new BigDecimal(reduceEntity.getGbDgsrReturnWeight());
//                    if (ifShowScale) {
//                        businessWeight = businessWeight.divide(countScale, 2, BigDecimal.ROUND_HALF_UP);
//                    }
//                    businessEntity.setGbBusinessType("return");
//                }
//                businessEntity.setGbBusinessWeight(businessWeight.toString());
//
//                resultData.add(businessEntity);
//            }
//
//        }
//
//        //按照时间排序后的新数组
//        resultData = resultData.stream().sorted(Comparator.comparing(GbDepartmentGoodsStockBusinessEntity::getGbBusinessFullTime).thenComparing(GbDepartmentGoodsStockBusinessEntity::getGbBusinessFullTime)).collect(Collectors.toList());
////
//        //4，计算每一个业务的剩余数量
//        BigDecimal gbBusinessWeight = new BigDecimal(0);
//        if (resultData.size() > 1) {
//            BigDecimal stockWeight = resultData.get(0).getGbBusinessResultWeight();
//
//            System.out.println("bususu" + resultData.get(0).getGbBusinessWeight());
//            for (int i = 1; i < resultData.size(); i++) {
//                String gbBusinessType = resultData.get(i).getGbBusinessType();
//                if (gbBusinessType.equals("stock")) {
//                    System.out.println("siziiiizizi" + resultData.get(i).getGbBusinessWeight());
//                    gbBusinessWeight = stockWeight.add(new BigDecimal(resultData.get(i).getGbBusinessWeight())).setScale(2, BigDecimal.ROUND_HALF_UP);
//                } else {
//                    gbBusinessWeight = stockWeight.subtract(new BigDecimal(resultData.get(i).getGbBusinessWeight())).setScale(2, BigDecimal.ROUND_HALF_UP);
//                }
//                resultData.get(i).setGbBusinessResultWeight(gbBusinessWeight);
//            }
//        }
//
//        return R.ok().put("data", resultData);
//    }


    /**
     * 管理端 获取disGoods采购数据图表
     *
     * @param disGoodsId
     * @param startDate
     * @param stopDate
     * @param ids
     * @return
     */
    @RequestMapping(value = "/getDisGoodsReceiveDayData", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsReceiveDayData(Integer disGoodsId, String startDate, String stopDate, String ids) {
        String[] arr = ids.split(",");
        TreeSet<String> list = new TreeSet<>();
        List<GbDepartmentEntity> departmentEntities = new ArrayList<>();
        BigDecimal decimal = new BigDecimal(0);
        try {
            for (String fatherID : arr) {
                GbDepartmentEntity fatherDep = gbDepartmentService.queryObject(Integer.valueOf(fatherID));
                Integer howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
                List<String> aaa = new ArrayList<>();
                for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat format11 = new SimpleDateFormat("MM-dd");
                    Date date = null;
                    date = format.parse(startDate);
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(date);
                    calendar.add(calendar.DATE, i);
                    date = calendar.getTime();
                    String format1 = format.format(date);
                    String format2 = format11.format(date);
                    list.add(format2);
                    Integer gbDepartmentId = fatherDep.getGbDepartmentId();
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("depFatherId", gbDepartmentId);
                    map1.put("date", format1);
                    map1.put("disGoodsId", disGoodsId);
                    Double aDouble = gbDepGoodsStockService.queryDepStockWeightTotal(map1);
                    if (aDouble == null) {
                        aDouble = 0.0;
                    }
                    aaa.add(aDouble.toString());
                    decimal = decimal.add(new BigDecimal(aDouble));
                }
                fatherDep.setDayData(aaa);
                departmentEntities.add(fatherDep);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Map<String, Object> map3 = new HashMap<>();
        map3.put("list", list);
        map3.put("arr", departmentEntities);
        map3.put("total", decimal);
        return R.ok().put("data", map3);
    }


    /**
     * 管理端-查询所有选中部门的库存图表
     * 管理端- businessPage swiper-2 接口
     *
     * @param startDate
     * @param stopDate
     * @param
     * @return
     */
//    @RequestMapping(value = "/getDistributerGoodsStockData", method = RequestMethod.POST)
//    @ResponseBody
//    public R getDistributerGoodsStockData(String startDate, String stopDate,
//                                          String ids) {
//        System.out.println("startDate" + startDate);
//        System.out.println("kfIds" + ids);
//        String[] arr = ids.split(",");
//        List<GbDepartmentEntity> dataDeps = new ArrayList<>();
//        for (String id : arr) {
//            GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(id));
//
//            Map<String, Object> resultDep = new HashMap<>();
//
//            resultDep.put("typeThreeData", getTotalTypeMonth(id, startDate, stopDate));
//            departmentEntity.setTotalMap(resultDep);
//
//            dataDeps.add(departmentEntity);
//        }
//
//        return R.ok().put("data", dataDeps);
//    }
    private Map<String, Object> getTotalTypeMonth(String id, String startDate, String stopDate) {
        Map<String, Object> map3 = new HashMap<>();
        //inventoryDate
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", id);
//        map.put("startDate", startDate);
//        map.put("stopDate", stopDate);
//        map.put("notDayuStopDate", stopDate);
//        map.put("dayuFreshTime", formatWhatYearDayTime(0));
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer > 0) {
            Double aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
            BigDecimal result = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
            map3.put("total", result);
        } else {
            map3.put("total", "0");
        }
        Map<String, Object> map2 = new HashMap<>();
        map2.put("depFatherId", id);
//        map2.put("startDate", startDate);
//        map2.put("notDayuStopDate", stopDate);
        map2.put("freshTime", formatWhatYearDayTime(0));
        Integer integer2 = gbDepGoodsStockService.queryGoodsStockCount(map2);
        if (integer2 > 0) {
            Double aDoublex = gbDepGoodsStockService.queryDepGoodsRestTotal(map2);
            BigDecimal resultx = new BigDecimal(aDoublex).setScale(2, BigDecimal.ROUND_HALF_UP);
            map3.put("totalx", resultx);
        } else {
            map3.put("totalx", "0");
        }
        return map3;

    }

    /**
     * 库房出库
     *
     * @param orderId 申请出库订单id
     * @return
     */
    @RequestMapping(value = "/getOutingStockOrder", method = RequestMethod.POST)
    @ResponseBody
    public R getOutingStockOrder(Integer orderId, Integer depId) {

        //1，查询订单出库库存
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", orderId);
        List<GbDepartmentGoodsStockEntity> gbDepGoodsStockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);
        System.out.println("ennenssiz" + gbDepGoodsStockEntities.size());

        //2，查询现有库存
        GbDepartmentOrdersEntity ordersEntity = gbDepOrdersService.queryObject(orderId);
        Integer toDepId = ordersEntity.getGbDoToDepartmentId();
        Integer goodsId = ordersEntity.getGbDoDisGoodsId();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disGoodsId", goodsId);
        map1.put("depFatherId", toDepId);
        map1.put("restWeight", 0);
        List<GbDepartmentGoodsStockEntity> gbDepGoodsStockEntities1 = gbDepGoodsStockService.queryGoodsStockByParams(map1);
        TreeSet<GbDepartmentGoodsStockEntity> treeSet = new TreeSet<>();
        // 3，最后库存结果
        for (GbDepartmentGoodsStockEntity outStock : gbDepGoodsStockEntities) {
            if (gbDepGoodsStockEntities1.size() > 0) {
                for (GbDepartmentGoodsStockEntity nowStock : gbDepGoodsStockEntities1) {
                    if (outStock.getGbDgsGbGoodsStockId().equals(nowStock.getGbDepartmentGoodsStockId())) {
                        nowStock.setGbDgsInventoryWeight(outStock.getGbDgsWeight());
                        nowStock.setIsSelected(true);
                        nowStock.setToDepStockEntity(outStock);
                    } else {
                        Integer gbDgsGbGoodsStockId = outStock.getGbDgsGbGoodsStockId();
                        GbDepartmentGoodsStockEntity stockEntity = gbDepGoodsStockService.queryObject(gbDgsGbGoodsStockId);
                        stockEntity.setGbDgsInventoryWeight(outStock.getGbDgsWeight());
                        stockEntity.setIsSelected(true);
                        stockEntity.setToDepStockEntity(outStock);
                        treeSet.add(stockEntity);
                    }
                    treeSet.add(nowStock);
                }
            } else {
                Integer gbDepartmentGoodsStockId = outStock.getGbDgsGbGoodsStockId();
                GbDepartmentGoodsStockEntity stockEntity = gbDepGoodsStockService.queryObject(gbDepartmentGoodsStockId);
                stockEntity.setGbDgsInventoryWeight(outStock.getGbDgsWeight());
                stockEntity.setIsSelected(true);
                stockEntity.setToDepStockEntity(outStock);
                treeSet.add(stockEntity);
            }
        }


        Map<String, Object> map11 = new HashMap<>();
        map11.put("depFatherId", depId);
        map11.put("disGoodsId", goodsId);
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryDepGoodsItemByParams(map11);
        Map<String, Object> map21 = new HashMap<>();
        map21.put("arr", treeSet);
        map21.put("depGoods", departmentDisGoodsEntity);
        return R.ok().put("data", map21);
    }

    /**
     * 订货端-获取部门库存商品列表
     * stockGoodsList 接口
     *
     * @param depId
     * @param fatherId
     * @param limit
     * @param page
     * @return
     */
    @RequestMapping(value = "/getSubDepartmentStockGoodsList", method = RequestMethod.POST)
    @ResponseBody
    public R getSubDepartmentStockGoodsList(Integer depId, Integer fatherId,
                                            Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("depId", depId);
        map.put("disGoodsFatherId", fatherId);
        map.put("restWeight", 0);
        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(map);

        Map<String, Object> mapMonth = new HashMap<>();
        mapMonth.put("depId", depId);
        mapMonth.put("disGoodsFatherId", fatherId);
        List<GbDepartmentDisGoodsEntity> disGoodsEntitiesMonth = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(mapMonth);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("countMonth", disGoodsEntitiesMonth.size());
        map1.put("arr", departmentDisGoodsEntities);
        int total = 0;
        total = disGoodsEntitiesMonth.size();


        PageUtilsMap pageUtil = new PageUtilsMap(map1, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    /**
     * 门店端-获取部门库存商品列表
     * stockGoodsList 接口
     *
     * @param depId
     * @param fatherId
     * @param inventoryType
     * @param limit
     * @param page
     * @return
     */
    @RequestMapping(value = "/getDepartmentStockGoodsList", method = RequestMethod.POST)
    @ResponseBody
    public R getDepartmentStockGoodsList(Integer depId, Integer fatherId, String inventoryType,
                                         Integer limit, Integer page, Integer isSelf) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("depFatherId", depId);
        map.put("disGoodsFatherId", fatherId);
        map.put("isSelf", isSelf);
        if (inventoryType.equals("month")) {
            map.put("inventoryType", getDISGoodsInventroyMonth());
        }
        if (inventoryType.equals("week")) {
            map.put("inventoryType", getDISGoodsInventroyWeek());
        }
        if (inventoryType.equals("daily")) {
            map.put("inventoryType", getDISGoodsInventroyDaily());

        }
        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(map);


        int total = 0;

        Map<String, Object> map1 = new HashMap<>();
        if (!inventoryType.equals("-1")) {
            Map<String, Object> mapMonth = new HashMap<>();
            mapMonth.put("depFatherId", depId);
            mapMonth.put("disGoodsFatherId", fatherId);
            mapMonth.put("inventoryType", getDISGoodsInventroyMonth());
            List<GbDepartmentDisGoodsEntity> disGoodsEntitiesMonth = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(mapMonth);


            Map<String, Object> mapWeek = new HashMap<>();
            mapWeek.put("depFatherId", depId);
            mapWeek.put("disGoodsFatherId", fatherId);
            mapWeek.put("inventoryType", getDISGoodsInventroyWeek());
            List<GbDepartmentDisGoodsEntity> disGoodsEntitiesWeek = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(mapWeek);


            Map<String, Object> mapDaily = new HashMap<>();
            mapDaily.put("depFatherId", depId);
            mapDaily.put("disGoodsFatherId", fatherId);
            mapDaily.put("inventoryType", getDISGoodsInventroyDaily());
            List<GbDepartmentDisGoodsEntity> disGoodsEntitiesDaily = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(mapDaily);


            map1.put("countMonth", disGoodsEntitiesMonth.size());
            map1.put("countWeek", disGoodsEntitiesWeek.size());
            map1.put("countDaily", disGoodsEntitiesDaily.size());
            if (inventoryType.equals("month")) {
                total = disGoodsEntitiesMonth.size();
            }
            if (inventoryType.equals("week")) {
                total = disGoodsEntitiesWeek.size();
            }
            if (inventoryType.equals("daily")) {
                total = disGoodsEntitiesDaily.size();
            }
        } else {
            Map<String, Object> mapDaily = new HashMap<>();
            mapDaily.put("depFatherId", depId);
            mapDaily.put("disGoodsFatherId", fatherId);
            List<GbDepartmentDisGoodsEntity> sizidi = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(mapDaily);
            total = sizidi.size();
        }

        map1.put("arr", departmentDisGoodsEntities);


        PageUtilsMap pageUtil = new PageUtilsMap(map1, total, limit, page);
        return R.ok().put("page", pageUtil);
    }

    /**
     * 管理端，门店端-获取部门库存商品父级列表
     *
     * @param depId
     * @return
     */
    @RequestMapping(value = "/getSubDepartmentStockGoodsFather/{depId}")
    @ResponseBody
    public R getSubDepartmentStockGoodsFather(@PathVariable String depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("dayuStatus", -1);
        List<GbDistributerFatherGoodsEntity> greatGrandGoods = gbDepGoodsStockService.queryDepStockDisFatherGoodsFather(map);
        for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoods) {
            BigDecimal greatGrandTotal = new BigDecimal(0);
            List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
            for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                BigDecimal grandDouble = new BigDecimal(0);
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                    Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                    map.put("disGoodsFatherId", gbDistributerFatherGoodsId);
                    System.out.println("susbbssbt" + map);
                    Double fatherDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
                    father.setFatherProduceTotal(fatherDouble);
                    father.setFatherProduceTotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(map);
                    father.setSubGoodsCount(Integer.toString(departmentDisGoodsEntities.size()));
                    grandDouble = grandDouble.add(new BigDecimal(fatherDouble));
                    greatGrandTotal = greatGrandTotal.add(new BigDecimal(fatherDouble));
                }
                grandFather.setFatherProduceTotal(grandDouble.doubleValue());
                grandFather.setFatherProduceTotalString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
            greatGrandFather.setFatherProduceTotal(greatGrandTotal.doubleValue());
            greatGrandFather.setFatherProduceTotalString(greatGrandTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        }

        return R.ok().put("data", greatGrandGoods);
    }


    /**
     * 库房获取每天入库数据
     *
     * @param depId
     * @param date
     * @return
     */
    @RequestMapping(value = "/getStockInStockEveryDay", method = RequestMethod.POST)
    @ResponseBody
    public R getStockInStockEveryDay(Integer depId, String date) {
        Map<String, Object> map = new HashMap<>();
        map.put("stockDepId", depId);
        map.put("date", date);
        List<GbDistributerGoodsShelfEntity> shelfGoodsEntities = gbDepGoodsStockService.queryEveryDayOutStockShelfGoods(map);

        return R.ok().put("data", shelfGoodsEntities);
    }

    /**
     * 库房获取每天出库数据
     *
     * @param depId
     * @param date
     * @return
     */
    @RequestMapping(value = "/getStockOutStockEveryDay", method = RequestMethod.POST)
    @ResponseBody
    public R getStockOutStockEveryDay(Integer depId, String date) {

        Map<String, Object> map = new HashMap<>();
        map.put("fromDepId", depId);
        map.put("equalOutDate", date);
//        map.put("dayuStatus", -1);
        System.out.println(map);
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);
        System.out.println(stockEntities.size() + "stocksiisziizizizizi");
        if (stockEntities.size() > 0) {

            GbDepartmentGoodsStockEntity minStock = gbDepGoodsStockService.queryMinFullTimeForDayStock(map);
            String strMin = minStock.getGbDgsOutHour().toString();
            GbDepartmentGoodsStockEntity maxStock = gbDepGoodsStockService.queryMaxFullTimeForDayStock(map);
            String strMax = maxStock.getGbDgsOutHour().toString();
            List<GbDistributerGoodsShelfEntity> shelfGoodsEntities = gbDepGoodsStockService.queryEveryDayOutStockShelfGoods(map);
            System.out.println(shelfGoodsEntities.size() + "zenmemelelel");
            Map<String, Object> map3 = new HashMap<>();
            map3.put("minTime", strMin);
            map3.put("maxTime", strMax);
            map3.put("arr", shelfGoodsEntities);

            return R.ok().put("data", map3);
        } else {

            return R.error(-1, "没有数据");
        }

    }


    /**
     * 初始化部门库存数据
     *
     * @param file
     * @param depId
     * @param disId
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateInitStockData", produces = "text/html;charset=UTF-8")
    public R updateInitStockData(@RequestParam("file") MultipartFile file,
                                 @RequestParam("depId") Integer depId,
                                 @RequestParam("disId") Integer disId,
                                 HttpSession session) {

        System.out.println(file.getName());
        HSSFWorkbook wb = null;
        try {
            wb = new HSSFWorkbook(file.getInputStream());
            HSSFSheet sheet = null;
            for (int j = 0; j < wb.getNumberOfSheets(); j++) {
                System.out.println(j);

                sheet = wb.getSheetAt(j);
                int lastRowNum = sheet.getLastRowNum();

                Row goodsRow = null;

                for (int i = 1; i <= lastRowNum; i++) {


                    goodsRow = sheet.getRow(i);
                    String depGoodsId = (String) getCellValue(goodsRow.getCell(1));
                    String goodsId = (String) getCellValue(goodsRow.getCell(2));

                    String goodsFatherId = (String) getCellValue(goodsRow.getCell(3));
                    String quantity = (String) getCellValue(goodsRow.getCell(5));
                    String price = (String) getCellValue(goodsRow.getCell(6));
                    System.out.println("iiiiii===" + goodsId + "fatherid==" + goodsFatherId + "quantity" + quantity
                            + "price==" + price);
                    BigDecimal subtotal = new BigDecimal(quantity).multiply(new BigDecimal(price)).setScale(2, BigDecimal.ROUND_HALF_UP);

                    Integer orderId = saveOrder(Integer.valueOf(goodsId), Integer.valueOf(goodsFatherId), depId, disId, quantity, price, Integer.valueOf(depGoodsId));

                    saveStock(price, quantity, subtotal.toString(), depId, Integer.valueOf(goodsId), Integer.valueOf(goodsFatherId), Integer.valueOf(depGoodsId), disId, orderId);

                    addDepDisGoods(orderId, Integer.valueOf(depGoodsId));

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.ok();
    }

    private Integer saveOrder(Integer goodsId, Integer goodsFatherId, Integer depId,
                              Integer disId, String quantity, String price, Integer depGoodsId) {
        GbDistributerGoodsEntity goodsEntity = disGoodsService.queryObject(goodsId);

        GbDepartmentOrdersEntity order = new GbDepartmentOrdersEntity();
        order.setGbDoOrderUserId(-1);
        order.setGbDoDepartmentId(depId);
        order.setGbDoDepartmentFatherId(depId);
        order.setGbDoDistributerId(disId);
        order.setGbDoOrderType(goodsEntity.getGbDgGoodsType());
        order.setGbDoGoodsType(goodsEntity.getGbDgGoodsType());
        order.setGbDoToDepartmentId(goodsEntity.getGbDgGbDepartmentId());
        order.setGbDoQuantity(quantity);
        order.setGbDoWeight(quantity);
        order.setGbDoDisGoodsId(goodsId);
        order.setGbDoDisGoodsFatherId(goodsFatherId);
        order.setGbDoStandard(goodsEntity.getGbDgGoodsStandardname());
        order.setGbDoPrice(price);
        BigDecimal bigDecimal = new BigDecimal(quantity.toString())
                .multiply(new BigDecimal(price.toString()))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        order.setGbDoSubtotal(bigDecimal.toString());
        order.setGbDoApplyDate(formatWhatDay(0));
        order.setGbDoArriveDate(formatWhatDay(0));
        order.setGbDoArriveOnlyDate(formatWhatDate(0));
        order.setGbDoApplyFullTime(formatFullTime());
        order.setGbDoArriveWhatDay(getWeek(0));
        order.setGbDoApplyOnlyTime(formatWhatTime(0));
        order.setGbDoArriveWeeksYear(getWeekOfYear(0));
        order.setGbDoPurchaseGoodsId(-1);
        order.setGbDoBuyStatus(-1);
        order.setGbDoDepDisGoodsId(depGoodsId);
        gbDepOrdersService.save(order);

        order.setGbDoStatus(4);
        gbDepOrdersService.update(order);
        return order.getGbDepartmentOrdersId();

    }


    private void addDepDisGoods(Integer orderId, Integer depDisGoodsId) {

        GbDepartmentOrdersEntity ordersEntity = gbDepOrdersService.queryObject(orderId);

        BigDecimal orderSubtotal = new BigDecimal(ordersEntity.getGbDoSubtotal());
        BigDecimal orderWeight = new BigDecimal(ordersEntity.getGbDoWeight());

        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);

        //updateOrder
        depDisGoodsEntity.setGbDdgOrderDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgOrderPrice(ordersEntity.getGbDoPrice());
        depDisGoodsEntity.setGbDdgOrderQuantity(ordersEntity.getGbDoQuantity());
        depDisGoodsEntity.setGbDdgOrderRemark(ordersEntity.getGbDoRemark());
        depDisGoodsEntity.setGbDdgOrderStandard(ordersEntity.getGbDoStandard());
        depDisGoodsEntity.setGbDdgOrderWeight(ordersEntity.getGbDoWeight());


        if (new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale()).compareTo(new BigDecimal(0)) == 1) {
            BigDecimal showScale = new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale());
            BigDecimal standardWeight = orderWeight.divide(showScale, 1, BigDecimal.ROUND_HALF_UP);
            depDisGoodsEntity.setGbDdgShowStandardWeight(standardWeight.toString());
        }

        depDisGoodsEntity.setGbDdgStockTotalSubtotal(orderSubtotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgStockTotalWeight(orderWeight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgInventoryDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgInventoryFullTime(formatWhatFullTime(0));
        gbDepartmentDisGoodsService.update(depDisGoodsEntity);

    }


    @RequestMapping(value = "/saveStockItem", method = RequestMethod.POST)
    @ResponseBody
    public R saveStockItem(@RequestBody GbDepartmentGoodsStockEntity stockEntity) {
        Integer gbDgsGbDisGoodsId = stockEntity.getGbDgsGbDisGoodsId();
        GbDistributerGoodsEntity distributerGoodsEntity = disGoodsService.queryObject(gbDgsGbDisGoodsId);


        //是个新采购商品
        GbDistributerPurchaseGoodsEntity disPurGoodsEntity = new GbDistributerPurchaseGoodsEntity();
        disPurGoodsEntity.setGbDpgDisGoodsFatherId(stockEntity.getGbDgsGbDisGoodsFatherId());
        disPurGoodsEntity.setGbDpgDisGoodsId(stockEntity.getGbDgsGbDisGoodsId());
        disPurGoodsEntity.setGbDpgDistributerId(stockEntity.getGbDgsGbDistributerId());
        disPurGoodsEntity.setGbDpgApplyDate(formatWhatDay(0));
        disPurGoodsEntity.setGbDpgStatus(3);
        disPurGoodsEntity.setGbDpgOrdersAmount(1);
        disPurGoodsEntity.setGbDpgTime(formatWhatTime(0));
        disPurGoodsEntity.setGbDpgPurchaseDate(formatWhatDay(0));
        disPurGoodsEntity.setGbDpgPurchaseMonth(formatWhatMonth(0));
        disPurGoodsEntity.setGbDpgPurchaseYear(formatWhatYear(0));
        disPurGoodsEntity.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
        disPurGoodsEntity.setGbDpgBatchId(-1);
        disPurGoodsEntity.setGbDpgPurUserId(stockEntity.getGbDgsReceiveUserId());
        disPurGoodsEntity.setGbDpgOrdersFinishAmount(1);
        disPurGoodsEntity.setGbDpgStandard(distributerGoodsEntity.getGbDgGoodsStandardname());
        disPurGoodsEntity.setGbDpgPurchaseWeek(getWeek(0));
        disPurGoodsEntity.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
        disPurGoodsEntity.setGbDpgBuyPrice(stockEntity.getGbDgsPrice());
        disPurGoodsEntity.setGbDpgBuyQuantity(stockEntity.getGbDgsWeight());
        disPurGoodsEntity.setGbDpgQuantity(stockEntity.getGbDgsWeight());
        disPurGoodsEntity.setGbDpgBuySubtotal(stockEntity.getGbDgsSubtotal());
        disPurGoodsEntity.setGbDpgPurchaseDepartmentId(stockEntity.getGbDgsGbDepartmentFatherId());
        disPurGoodsEntity.setGbDpgPurchaseNxDistributerId(-1);
        disPurGoodsEntity.setGbDpgPurchaseType(0);
        disPurGoodsEntity.setGbDpgPayType(0);


//        判断是否价格异常商品
        if (distributerGoodsEntity.getGbDgControlPrice() == 1) {
            disPurGoodsEntity = checkPurGoodsPrice(disPurGoodsEntity);
            stockEntity.setGbDgsGbPriceSubtotal(stockEntity.getGbDgsSubtotal());
            stockEntity.setGbDgsGbPriceGoodsId(disPurGoodsEntity.getGbDpgDisGoodsPriceId());
            stockEntity.setGbDgsGbPriceSubtotalScale(disPurGoodsEntity.getGbDpgBuyScale());
        }
        gbDistributerPurchaseGoodsService.save(disPurGoodsEntity);
        stockEntity.setGbDgsGbPurGoodsId(disPurGoodsEntity.getGbDistributerPurchaseGoodsId());

        GbDepartmentOrdersEntity order = new GbDepartmentOrdersEntity();
        order.setGbDoDepartmentId(stockEntity.getGbDgsGbDepartmentId());
        order.setGbDoDepartmentFatherId(stockEntity.getGbDgsGbDepartmentFatherId());
        order.setGbDoDistributerId(stockEntity.getGbDgsGbDistributerId());
        order.setGbDoOrderType(1);
        order.setGbDoToDepartmentId(distributerGoodsEntity.getGbDgGbDepartmentId());
        order.setGbDoQuantity(stockEntity.getGbDgsMyReturnWeight());
        order.setGbDoWeight(stockEntity.getGbDgsMyReturnWeight());
        order.setGbDoDisGoodsId(distributerGoodsEntity.getGbDistributerGoodsId());
        order.setGbDoDisGoodsFatherId(distributerGoodsEntity.getGbDgDfgGoodsFatherId());
        order.setGbDoStandard(distributerGoodsEntity.getGbDgGoodsStandardname());
        order.setGbDoPrice(stockEntity.getGbDgsPrice());
        BigDecimal bigDecimal = new BigDecimal(stockEntity.getGbDgsMyReturnWeight())
                .multiply(new BigDecimal(stockEntity.getGbDgsPrice()))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        order.setGbDoSubtotal(bigDecimal.toString());
        order.setGbDoSellingPrice(stockEntity.getGbDgsSellingPrice());
        order.setGbDoSellingSubtotal("0");
        order.setGbDoApplyDate(formatWhatDay(0));
        order.setGbDoArriveDate(formatWhatDay(0));
        order.setGbDoArriveOnlyDate(formatWhatDate(0));
        order.setGbDoApplyFullTime(formatFullTime());
        order.setGbDoArriveWhatDay(getWeek(0));
        order.setGbDoApplyOnlyTime(formatWhatTime(0));
        order.setGbDoArriveWeeksYear(getWeekOfYear(0));
        order.setGbDoDepDisGoodsId(stockEntity.getGbDgsGbDepDisGoodsId());
        order.setGbDoGoodsType(distributerGoodsEntity.getGbDgGoodsType());
        order.setGbDoNxDistributerId(distributerGoodsEntity.getGbDgNxDistributerId());
        order.setGbDoNxDistributerGoodsId(distributerGoodsEntity.getGbDgNxDistributerGoodsId());
        order.setGbDoStatus(getGbOrderStatusReceived());
        order.setGbDoBuyStatus(getGbOrderBuyStatusHasFinishPurGoods());
        gbDepOrdersService.justSave(order);
        stockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());


        stockEntity.setGbDgsRestWeight(stockEntity.getGbDgsWeight());
        stockEntity.setGbDgsRestSubtotal(stockEntity.getGbDgsSubtotal());
        stockEntity.setGbDgsLossWeight("0");
        stockEntity.setGbDgsLossSubtotal("0");
        stockEntity.setGbDgsReturnWeight("0");
        stockEntity.setGbDgsReturnSubtotal("0");
        stockEntity.setGbDgsWasteWeight("0");
        stockEntity.setGbDgsWasteSubtotal("0");
        stockEntity.setGbDgsProfitSubtotal("0");
        stockEntity.setGbDgsProfitWeight("0");
        stockEntity.setGbDgsProduceWeight("0");
        stockEntity.setGbDgsProduceSubtotal("0");
        stockEntity.setGbDgsSellingSubtotal("0");
        stockEntity.setGbDgsProduceSellingSubtotal("0");
        stockEntity.setGbDgsAfterProfitSubtotal("0");
        stockEntity.setGbDgsDate(formatWhatDay(0));
        stockEntity.setGbDgsTimeStamp(getTimeStamp());
        stockEntity.setGbDgsWeek(getWeek(0));
        stockEntity.setGbDgsMonth(formatWhatMonth(0));
        stockEntity.setGbDgsYear(formatWhatYear(0));

        stockEntity.setGbDgsGbGoodsStockId(-1);
        stockEntity.setGbDgsSellingPrice("-1");
        stockEntity.setGbDgsInventoryMonth(formatWhatMonth(0));
        stockEntity.setGbDgsInventoryYear(formatWhatYear(0));
        stockEntity.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
        stockEntity.setGbDgsInventoryDate(formatWhatDay(0));
        stockEntity.setGbDgsFullTime(formatFullTime());
        stockEntity.setGbDgsStatus(0);

        //判断是否有保鲜时间参数
        GbDistributerGoodsEntity gbDisGoodsEntity = disGoodsService.queryObject(Integer.valueOf(stockEntity.getGbDgsGbDisGoodsId()));
        if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
            int warnHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWarnHour());
            int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
            stockEntity.setGbDgsWarnFullTime(formatWhatFullTime(warnHour));
            stockEntity.setGbDgsWasteFullTime(formatWhatFullTime(wasteHour));
        }

        gbDepGoodsStockService.save(stockEntity);

        addDepDisGoodsData(stockEntity, stockEntity.getGbDgsGbDepDisGoodsId());

        updateDepGoodsDailyBusiness(stockEntity);
        return R.ok();
    }

    private void updateDepGoodsDailyBusiness(GbDepartmentGoodsStockEntity stock) {

        Map<String, Object> map = new HashMap<>();
        map.put("depGoodsId", stock.getGbDgsGbDepDisGoodsId());
        map.put("date", formatWhatDay(0));
        GbDepartmentGoodsDailyEntity depGoodsDailyItem = gbDepGoodsDailyService.queryDepGoodsDailyItem(map);
        if (depGoodsDailyItem != null) {
            BigDecimal weight = new BigDecimal(depGoodsDailyItem.getGbDgdWeight());
            BigDecimal total = new BigDecimal(depGoodsDailyItem.getGbDgdSubtotal());
            BigDecimal restWeight = new BigDecimal(depGoodsDailyItem.getGbDgdRestWeight());
            BigDecimal totalWeight = new BigDecimal(stock.getGbDgsWeight()).add(weight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalSubtotal = new BigDecimal(stock.getGbDgsSubtotal()).add(total).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalRestWeight = new BigDecimal(stock.getGbDgsWeight()).add(restWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            depGoodsDailyItem.setGbDgdWeight(totalWeight.toString());
            depGoodsDailyItem.setGbDgdSubtotal(totalSubtotal.toString());
            depGoodsDailyItem.setGbDgdRestWeight(totalRestWeight.toString());
            depGoodsDailyItem.setGbDgdSellClearHour("-1");
            depGoodsDailyItem.setGbDgdSellClearMinute("-1");
            gbDepGoodsDailyService.update(depGoodsDailyItem);

        } else {
            GbDepartmentGoodsDailyEntity dailyEntity = new GbDepartmentGoodsDailyEntity();
            dailyEntity.setGbDgdGbDistributerId(stock.getGbDgsGbDistributerId());
            dailyEntity.setGbDgdGbDepartmentId(stock.getGbDgsGbDepartmentId());
            dailyEntity.setGbDgdGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            dailyEntity.setGbDgdGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
            dailyEntity.setGbDgdGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
            dailyEntity.setGbDgdGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
            dailyEntity.setGbDgdDate(formatWhatDay(0));
            dailyEntity.setGbDgdWeek(getWeekOfYear(0).toString());
            dailyEntity.setGbDgdMonth(formatWhatMonth(0));
            dailyEntity.setGbDgdYear(formatWhatYear(0));
            dailyEntity.setGbDgdDay(getWeek(0));
            dailyEntity.setGbDgdWeight(stock.getGbDgsWeight());
            dailyEntity.setGbDgdRestWeight(stock.getGbDgsWeight());
            dailyEntity.setGbDgdSubtotal(stock.getGbDgsSubtotal());
            dailyEntity.setGbDgdProduceWeight("0");
            dailyEntity.setGbDgdProduceSubtotal("0");
            dailyEntity.setGbDgdLossWeight("0");
            dailyEntity.setGbDgdLossSubtotal("0");
            dailyEntity.setGbDgdReturnWeight("0");
            dailyEntity.setGbDgdReturnSubtotal("0");
            dailyEntity.setGbDgdWasteWeight("0");
            dailyEntity.setGbDgdWasteSubtotal("0");
            dailyEntity.setGbDgdSalesSubtotal("0");
            dailyEntity.setGbDgdProfitSubtotal("0");
            dailyEntity.setGbDgdAfterProfitSubtotal("0");
            dailyEntity.setGbDgdSellClearHour("-1");
            dailyEntity.setGbDgdSellClearMinute("-1");
            dailyEntity.setGbDgdLastWeight("0");
            dailyEntity.setGbDgdLastProduceWeight("0");
            Integer gbDgdGbDisGoodsId = dailyEntity.getGbDgdGbDisGoodsId();
            GbDistributerGoodsEntity distributerGoodsEntity = disGoodsService.queryObject(gbDgdGbDisGoodsId);
            if (distributerGoodsEntity.getGbDgControlFresh() == 1) {
                dailyEntity.setGbDgdFreshRate("100");
            } else {
                dailyEntity.setGbDgdFreshRate("0");
            }
            dailyEntity.setGbDgdFullTime(formatFullTime());
            gbDepGoodsDailyService.save(dailyEntity);
        }
    }


    private GbDistributerPurchaseGoodsEntity checkPurGoodsPrice(GbDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        System.out.println("checkkckGoododopriiddcheckPurGoodsPrice" + purchaseGoodsEntity.getGbDpgDisGoodsId());
        Integer gbDpgDisGoodsId = purchaseGoodsEntity.getGbDpgDisGoodsId();
        BigDecimal buyPrice = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
        Integer gbDpgDisGoodsPriceId = purchaseGoodsEntity.getGbDpgDisGoodsPriceId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = disGoodsService.queryObject(gbDpgDisGoodsId);

        BigDecimal weight = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
        BigDecimal goodsHighest = new BigDecimal(gbDistributerGoodsEntity.getGbDgGoodsHighestPrice());
        BigDecimal goodsLowest = new BigDecimal(gbDistributerGoodsEntity.getGbDgGoodsLowestPrice());
        String priceTotal = buyPrice.multiply(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString();

        if (buyPrice.compareTo(goodsHighest) == 1 && purchaseGoodsEntity.getGbDpgBuyQuantity() != null) { //高于最高价

            BigDecimal higherWhatPrice = buyPrice.subtract(goodsHighest);

            BigDecimal highertotal = higherWhatPrice.multiply(weight).setScale(2, BigDecimal.ROUND_HALF_UP); //高出的单价部分 * 重量
            BigDecimal multiply = higherWhatPrice.divide(goodsHighest, 4, BigDecimal.ROUND_HALF_DOWN); // 高出的单价部门 除以 正常价格
            BigDecimal highestTotal = goodsHighest.multiply(weight).setScale(1, BigDecimal.ROUND_HALF_UP);

            GbDistributerGoodsPriceEntity goodsPriceEntity = new GbDistributerGoodsPriceEntity();
            goodsPriceEntity.setGbDgpGoodsPrice(gbDistributerGoodsEntity.getGbDgGoodsPrice());
            goodsPriceEntity.setGbDgpGoodsLowestPrice(gbDistributerGoodsEntity.getGbDgGoodsLowestPrice());
            goodsPriceEntity.setGbDgpGoodsHighestPrice(gbDistributerGoodsEntity.getGbDgGoodsHighestPrice());
            goodsPriceEntity.setGbDgpDistributerGoodsId(gbDistributerGoodsEntity.getGbDistributerGoodsId());
            goodsPriceEntity.setGbDgpDistributerId(gbDistributerGoodsEntity.getGbDgDistributerId());
            goodsPriceEntity.setGbDgpDfgGoodsFatherId(gbDistributerGoodsEntity.getGbDgDfgGoodsFatherId());
            goodsPriceEntity.setGbDgpPurDate(formatWhatDay(0));
            goodsPriceEntity.setGbDgpPurGoodsId(purchaseGoodsEntity.getGbDistributerPurchaseGoodsId());
            goodsPriceEntity.setGbDgpPurUserId(purchaseGoodsEntity.getGbDpgPurUserId());
            goodsPriceEntity.setGbDgpPurWhat(1);
            goodsPriceEntity.setGbDgpPurPrice(purchaseGoodsEntity.getGbDpgBuyPrice());
            goodsPriceEntity.setGbDgpPurWeight(purchaseGoodsEntity.getGbDpgBuyQuantity());
            goodsPriceEntity.setGbDgpPurScale(multiply.toString());
            goodsPriceEntity.setGbDgpPurWhatTotal(highertotal.toString());
            goodsPriceEntity.setGbDgpGoodsHighestTotal(highestTotal.toString());
            goodsPriceEntity.setGbDgpGoodsLowestTotal("0");
            goodsPriceEntity.setGbDgpPurTotal(priceTotal);
            goodsPriceEntity.setGbDgpPurWeight(purchaseGoodsEntity.getGbDpgBuyQuantity());
            goodsPriceEntity.setGbDgpPurDepartmentId(purchaseGoodsEntity.getGbDpgPurchaseDepartmentId());
            goodsPriceEntity.setGbDgpStatus(0);
            goodsPriceEntity.setGbDgpWeek(getWeekOfYear(0).toString());
            goodsPriceEntity.setGbDgpMonth(formatWhatMonth(0));
            goodsPriceEntity.setGbDgpYear(formatWhatYear(0));
            goodsPriceEntity.setGbDgpPurNxDistributerId(gbDistributerGoodsEntity.getGbDgNxDistributerId());
            System.out.println("fdhfehfrekfe" + gbDistributerGoodsEntity.getGbDgNxDistributerId());
            goodsPriceService.save(goodsPriceEntity);

        }

        if (buyPrice.compareTo(goodsLowest) == -1 && purchaseGoodsEntity.getGbDpgBuyQuantity() != null) { //低于最低价
            BigDecimal lowerWhatPrice = goodsLowest.subtract(buyPrice);
            BigDecimal lowerTotal = lowerWhatPrice.multiply(weight).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal multiply = lowerWhatPrice.divide(goodsLowest, 4, BigDecimal.ROUND_HALF_DOWN);
            BigDecimal lowestTotal = goodsLowest.multiply(weight).setScale(1, BigDecimal.ROUND_HALF_UP);
            GbDistributerGoodsPriceEntity goodsPriceEntity = new GbDistributerGoodsPriceEntity();
            goodsPriceEntity.setGbDgpGoodsPrice(gbDistributerGoodsEntity.getGbDgGoodsPrice());
            goodsPriceEntity.setGbDgpGoodsLowestPrice(gbDistributerGoodsEntity.getGbDgGoodsLowestPrice());
            goodsPriceEntity.setGbDgpGoodsHighestPrice(gbDistributerGoodsEntity.getGbDgGoodsHighestPrice());
            goodsPriceEntity.setGbDgpDistributerGoodsId(gbDistributerGoodsEntity.getGbDistributerGoodsId());
            goodsPriceEntity.setGbDgpDistributerId(gbDistributerGoodsEntity.getGbDgDistributerId());
            goodsPriceEntity.setGbDgpDfgGoodsFatherId(gbDistributerGoodsEntity.getGbDgDfgGoodsFatherId());
            goodsPriceEntity.setGbDgpPurDate(formatWhatDay(0));
            goodsPriceEntity.setGbDgpPurGoodsId(purchaseGoodsEntity.getGbDistributerPurchaseGoodsId());
            goodsPriceEntity.setGbDgpPurUserId(purchaseGoodsEntity.getGbDpgPurUserId());
            goodsPriceEntity.setGbDgpPurWhat(-1);
            goodsPriceEntity.setGbDgpPurPrice(purchaseGoodsEntity.getGbDpgBuyPrice());
            goodsPriceEntity.setGbDgpPurScale(multiply.toString());
            goodsPriceEntity.setGbDgpPurWhatTotal(lowerTotal.toString());
            goodsPriceEntity.setGbDgpGoodsLowestTotal(lowestTotal.toString());
            goodsPriceEntity.setGbDgpPurTotal(priceTotal);
            goodsPriceEntity.setGbDgpPurWeight(purchaseGoodsEntity.getGbDpgBuyQuantity());
            goodsPriceEntity.setGbDgpGoodsHighestTotal("0");
            goodsPriceEntity.setGbDgpPurDepartmentId(purchaseGoodsEntity.getGbDpgPurchaseDepartmentId());
            goodsPriceEntity.setGbDgpStatus(0);
            goodsPriceEntity.setGbDgpWeek(getWeekOfYear(0).toString());
            goodsPriceEntity.setGbDgpMonth(formatWhatMonth(0));
            goodsPriceEntity.setGbDgpYear(formatWhatYear(0));
            goodsPriceEntity.setGbDgpPurNxDistributerId(gbDistributerGoodsEntity.getGbDgNxDistributerId());
            goodsPriceService.save(goodsPriceEntity);
            purchaseGoodsEntity.setGbDpgDisGoodsPriceId(goodsPriceEntity.getGbDistributerGoodsPriceId());

        }


        return purchaseGoodsEntity;

    }

    private void addDepDisGoodsData(GbDepartmentGoodsStockEntity stockEntity, Integer depDisGoodsId) {
        BigDecimal stockWeight = new BigDecimal(stockEntity.getGbDgsWeight());
        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        BigDecimal gbDdgStockTotalWeight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).add(stockWeight);
        BigDecimal depSubTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).add(new BigDecimal(stockEntity.getGbDgsSubtotal()));
        if (new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale()).compareTo(BigDecimal.ZERO) == 1) {
            BigDecimal showScale = new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale());
            BigDecimal divide = gbDdgStockTotalWeight.divide(showScale, 2, BigDecimal.ROUND_HALF_UP);
            depDisGoodsEntity.setGbDdgShowStandardWeight(divide.toString());
        }
        depDisGoodsEntity.setGbDdgStockTotalWeight(gbDdgStockTotalWeight.toString());
        depDisGoodsEntity.setGbDdgStockTotalSubtotal(depSubTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgInventoryDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgInventoryFullTime(formatWhatFullTime(0));
        gbDepartmentDisGoodsService.update(depDisGoodsEntity);

    }

    private void saveStock(String price, String weight, String subtotal, Integer depId,
                           Integer disGoodsId, Integer disGoodsFatherId, Integer depGoodsId,
                           Integer disId, Integer orderId) {

        GbDepartmentGoodsStockEntity stockEntity = new GbDepartmentGoodsStockEntity();
        stockEntity.setGbDgsWeight(weight);
        stockEntity.setGbDgsPrice(price);
        stockEntity.setGbDgsSubtotal(subtotal);
        stockEntity.setGbDgsRestWeight(weight);
        stockEntity.setGbDgsRestSubtotal(subtotal);
        stockEntity.setGbDgsLossWeight("0");
        stockEntity.setGbDgsLossSubtotal("0");
        stockEntity.setGbDgsReturnWeight("0");
        stockEntity.setGbDgsReturnSubtotal("0");
        stockEntity.setGbDgsWasteWeight("0");
        stockEntity.setGbDgsWasteSubtotal("0");
        stockEntity.setGbDgsDate(formatWhatDay(0));
        stockEntity.setGbDgsTimeStamp(getTimeStamp());
        stockEntity.setGbDgsWeek(getWeek(0));
        stockEntity.setGbDgsMonth(formatWhatMonth(0));
        stockEntity.setGbDgsYear(formatWhatYear(0));
        stockEntity.setGbDgsGbDepartmentId(depId);
        stockEntity.setGbDgsGbDepartmentFatherId(depId);
        stockEntity.setGbDgsGbDisGoodsFatherId(disGoodsFatherId);
        stockEntity.setGbDgsGbDisGoodsId(disGoodsId);
        stockEntity.setGbDgsGbDistributerId(disId);
        stockEntity.setGbDgsGbDepDisGoodsId(depGoodsId);
        stockEntity.setGbDgsGbDepartmentOrderId(orderId);
        stockEntity.setGbDgsGbPurGoodsId(-1);
        stockEntity.setGbDgsGbGoodsStockId(-1);
        stockEntity.setGbDgsGbFromDepartmentId(-1);
        stockEntity.setGbDgsInventoryMonth(formatWhatMonth(0));
        stockEntity.setGbDgsInventoryYear(formatWhatYear(0));
        stockEntity.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
        stockEntity.setGbDgsInventoryDate(formatWhatDay(0));
        stockEntity.setGbDgsFullTime(formatFullTime());
        stockEntity.setGbDgsStatus(0);

        //判断是否有保鲜时间参数
        GbDistributerGoodsEntity gbDisGoodsEntity = disGoodsService.queryObject(disGoodsId);
        if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
            int warnHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWarnHour());
            int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
            stockEntity.setGbDgsWarnFullTime(formatWhatFullTime(warnHour));
            stockEntity.setGbDgsWasteFullTime(formatWhatFullTime(wasteHour));
        }

//        //判断是否价格异常商品
//        if (stockEntity.getGbDgsGbPurGoodsId() != null) {
//            GbDistributerGoodsPriceEntity goodsPriceEntity = goodsPriceService.queryObject(stockEntity.getGbDgsGbPurGoodsId());
//            String gbDgpGoodsPrice = goodsPriceEntity.getGbDgpPurPrice();
//            String gbDgsWeight = stockEntity.getGbDgsWeight();
//            BigDecimal multiply = new BigDecimal(gbDgpGoodsPrice).multiply(new BigDecimal(gbDgsWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
//            String priceSubtotal = multiply.toString();
//            if (goodsPriceEntity.getGbDgpPurWhat() == -1) {
//                priceSubtotal = "-" + priceSubtotal;
//            }
//            stockEntity.setGbDgsGbPriceSubtotal(priceSubtotal);
//            stockEntity.setGbDgsGbPriceGoodsId(goodsPriceEntity.getGbDistributerGoodsPriceId());
//            stockEntity.setGbDgsGbPriceSubtotalScale(goodsPriceEntity.getGbDgpPurScale());
//        }
        gbDepGoodsStockService.save(stockEntity);
    }


    private Object getCellValue(Cell cell) {
        System.out.println(cell.getCellType() + "typepepep???????");
        String value = "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getRichStringCellValue().getString();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
//                    double numericCellValue = cell.getNumericCellValue();
//                    String s = String.valueOf(numericCellValue);
//                    int i1 = Integer.parseInt(s.replace(".0", ""));
//                    return i1;
                    DecimalFormat df = new DecimalFormat("#.#");
                    value = df.format(cell.getNumericCellValue());
                    return value;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
        }

        return cell;

    }


    /**
     * 订货端修改制作成本
     * depGoodsList 接口
     *
     * @param stock 批次
     * @return ok
     */
    @RequestMapping(value = "/saveDepProduceGoodsStock", method = RequestMethod.POST)
    @ResponseBody
    public R saveDepProduceGoodsStock(@RequestBody GbDepartmentGoodsStockEntity stock) {
        changeDepartmentStock(stock, "produce");
        return R.ok();
    }

    @RequestMapping(value = "/saveDepLossGoodsStock", method = RequestMethod.POST)
    @ResponseBody
    public R saveDepLossGoodsStock(@RequestBody GbDepartmentGoodsStockEntity stock) {
        GbDepartmentGoodsStockReduceEntity reduceEntity = changeDepartmentStock(stock, "loss");
        System.out.println("rednenenennen" + reduceEntity);
        return R.ok().put("data", reduceEntity);
    }


    @RequestMapping(value = "/saveDepWasteGoodsStock", method = RequestMethod.POST)
    @ResponseBody
    public R saveDepWasteGoodsStock(@RequestBody GbDepartmentGoodsStockEntity stock) {
//        transferDepartmentStock(stock, "waste");
        changeDepartmentStock(stock, "waste");

        return R.ok();
    }


//    private void transferDepartmentStock(GbDepartmentGoodsStockEntity stock, String what) {
//        BigDecimal myChangeWeight = new BigDecimal("0");
//        BigDecimal myChangeSubtotal = new BigDecimal(0);
//        BigDecimal profitSubtotal = new BigDecimal(0);
//        BigDecimal salesSubtotal = new BigDecimal(0);
//
////        BigDecimal myChangeProduceProfitSubtotal = new BigDecimal(0);
////        BigDecimal costPrice = new BigDecimal(stock.getGbDgsPrice()); //单价
////        BigDecimal gbDgsBetweenPrice = new BigDecimal(stock.getGbDgsBetweenPrice());
////        BigDecimal gbDgsSellingPrice = new BigDecimal(stock.getGbDgsSellingPrice());
////        BigDecimal gbDgsProfitWeight = new BigDecimal(stock.getGbDgsProfitWeight());
////        BigDecimal gbDgsProfitSubtotal = new BigDecimal(stock.getGbDgsProfitSubtotal());
//
//
//        GbDepartmentGoodsStockRecordEntity transRecordEntity = new GbDepartmentGoodsStockRecordEntity();
//        Integer reduceWeightUserId = stock.getGbDgsReduceWeightUserId();
//
//        Integer gbDgGoodsInventoryType = stock.getGbDistributerGoodsEntity().getGbDgGoodsInventoryType();
//
//        //利润单价
//        BigDecimal costPrice = new BigDecimal(stock.getGbDgsPrice()); //成本单价
//        BigDecimal gbSalesProfitSubtotal = new BigDecimal(stock.getGbDgsAfterProfitSubtotal()); //总的销售利润
//
//        // 1.1 如果是损耗接口
//        if (what.equals("loss")) {
//
//            //添加 reduce数据
//            myChangeWeight = new BigDecimal(stock.getGbDgsMyLossWeight()).setScale(1, BigDecimal.ROUND_HALF_UP); // 最新提交待损耗数量
//            myChangeSubtotal = myChangeWeight.multiply(costPrice).setScale(2,BigDecimal.ROUND_HALF_UP);
////            String myChangeSubtotalString = String.format("%.1f", myChangeSubtotal); //总损耗成本
//            BigDecimal newProfitWeight = gbDgsProfitWeight.subtract(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
//
//
//            transRecordEntity = transSaveDepStockRecordEntity(stock, "loss", myChangeWeight);
//
//            transSaveDepGoodsStockReduceEntity(transRecordEntity, "loss", myChangeWeight.toString(),
//                    myChangeSubtotalString, reduceWeightUserId, gbDgGoodsInventoryType, profitSubtotal, salesSubtotal);
//            subscribeDepDisGoodsTotal(myChangeWeight, new BigDecimal(myChangeSubtotal), stock.getGbDgsGbDepDisGoodsId());
//            updateDepGoodsDailyEntity(stock, "loss", myChangeWeight,myChangeSubtotal , salesSubtotal,myChangeProduceProfitSubtotal);
//
//        }
//        // 1.2 如果是制作接口，加上利润功能
//        if (what.equals("produce")) {
//            //添加 reduce数据
//            myChangeWeight = new BigDecimal(stock.getGbDgsMyProduceWeight()).setScale(1, BigDecimal.ROUND_HALF_UP); // 最新提交待损耗数量
//            myChangeSubtotal = myChangeWeight.multiply(costPrice).doubleValue();
//            String myChangeSubtotalString = String.format("%.1f", myChangeWeight.multiply(costPrice).doubleValue()); //总损耗成本
//
//            BigDecimal newProfitSubtotal = gbDgsBetweenPrice.multiply(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
//            profitSubtotal = gbDgsProfitSubtotal.add(newProfitSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
//            salesSubtotal = gbDgsSellingPrice.multiply(myChangeWeight).setScale(1,BigDecimal.ROUND_HALF_UP);
//
//            transRecordEntity = transSaveDepStockRecordEntity(stock, "produce", myChangeWeight);
//            transSaveDepGoodsStockReduceEntity(transRecordEntity, "produce", myChangeWeight.toString(), myChangeSubtotalString, reduceWeightUserId, gbDgGoodsInventoryType, newProfitSubtotal, salesSubtotal);
//            subscribeDepDisGoodsTotal(myChangeWeight, new BigDecimal(myChangeSubtotal), stock.getGbDgsGbDepDisGoodsId());
//            updateDepGoodsDailyEntity(stock, "produce", myChangeWeight.toString(), myChangeSubtotalString, newProfitSubtotal, salesSubtotal,myChangeProduceProfitSubtotal);
//
//        }
//        // 1.3 如果是废弃接口
//        if (what.equals("waste")) {
//            //添加 reduce数据
//            myChangeWeight = new BigDecimal(stock.getGbDgsDoWasteWeight()).setScale(1, BigDecimal.ROUND_HALF_UP); // 最新提交待损耗数量
//            myChangeSubtotal = myChangeWeight.multiply(costPrice).doubleValue();
//            String myChangeSubtotalString = String.format("%.1f", myChangeWeight.multiply(costPrice).doubleValue()); //总损耗成本
//
//            BigDecimal newProfitWeight = gbDgsProfitWeight.subtract(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
//            //成本subtotal
//            BigDecimal newProfitSubtotal = costPrice.multiply(newProfitWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
//            BigDecimal totalProfitSubtotal = gbDgsProfitSubtotal.subtract(newProfitSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
//
//            transRecordEntity = transSaveDepStockRecordEntity(stock, "waste", myChangeWeight);
//
//            transSaveDepGoodsStockReduceEntity(transRecordEntity, "waste", myChangeWeight.toString(), myChangeSubtotalString, reduceWeightUserId, gbDgGoodsInventoryType, profitSubtotal, salesSubtotal);
//
//            subscribeDepDisGoodsTotal(myChangeWeight, new BigDecimal(myChangeSubtotal), stock.getGbDgsGbDepDisGoodsId());
//            updateDepGoodsDailyEntity(stock, "waste", myChangeWeight.toString(), myChangeSubtotalString, newProfitSubtotal, salesSubtotal,myChangeProduceProfitSubtotal);
//
//        }
//
//
//        //月盘库
//        if (gbDgGoodsInventoryType.equals(getDISGoodsInventroyMonth())) {
//            transMonth(stock, what, transRecordEntity.getGbDepartmentGoodsStockRecordId());
//        }
//
//        //周盘库
//        if (gbDgGoodsInventoryType.equals(getDISGoodsInventroyWeek())) {
//            transWeek(stock, what, transRecordEntity.getGbDepartmentGoodsStockRecordId());
//        }
//
//        //日盘库
//        if (gbDgGoodsInventoryType.equals(getDISGoodsInventroyDaily())) {
//            transDaily(stock, what, transRecordEntity.getGbDepartmentGoodsStockRecordId());
//        }
//
//    }

    private GbDepartmentGoodsStockRecordEntity transSaveDepStockRecordEntity(GbDepartmentGoodsStockEntity stock, String what, BigDecimal myChangeWeight) {

        double allChangeSubtotal = 0.0;
        BigDecimal price = new BigDecimal(stock.getGbDgsPrice()); //单价

        GbDepartmentGoodsStockRecordEntity recordEntity = new GbDepartmentGoodsStockRecordEntity();

        if (what.equals("loss")) {
            BigDecimal lossWeight = new BigDecimal(stock.getGbDgsLossWeight()); // 已损耗数量
            BigDecimal allLossWeight = lossWeight.add(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
            allChangeSubtotal = allLossWeight.multiply(price).doubleValue(); //总损耗成本
            recordEntity.setGbDgscLossWeight(allLossWeight.toString());
            recordEntity.setGbDgscLossSubtotal(String.format("%.1f", allChangeSubtotal));
            recordEntity.setGbDgscProduceWeight(stock.getGbDgsProduceWeight());
            recordEntity.setGbDgscProduceSubtotal(stock.getGbDgsProduceSubtotal());
            recordEntity.setGbDgscReturnWeight(stock.getGbDgsReturnWeight());
            recordEntity.setGbDgscReturnSubtotal(stock.getGbDgsReturnSubtotal());
            recordEntity.setGbDgscDoWasteWeight("0.0");
            recordEntity.setGbDgscDoWasteSubtotal("0.0");
            recordEntity.setGbDgscDoWasteUserId(stock.getGbDgsReduceWeightUserId());
        } else if (what.equals("produce")) {
            BigDecimal produceWeight = new BigDecimal(stock.getGbDgsProduceWeight()); // 已制作数量
            BigDecimal allProduceWeight = produceWeight.add(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP); //总制作数量
            allChangeSubtotal = allProduceWeight.multiply(price).doubleValue(); //总损耗成本
            BigDecimal profitSubtotal = allProduceWeight.multiply(new BigDecimal(stock.getGbDgsBetweenPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
            recordEntity.setGbDgscProduceWeight(allProduceWeight.toString());
            recordEntity.setGbDgscProduceSubtotal(String.format("%.1f", allChangeSubtotal));
            recordEntity.setGbDgscProfitWeight(allProduceWeight.toString());
            recordEntity.setGbDgscProfitSubtotal(profitSubtotal.toString());
            recordEntity.setGbDgscLossWeight(stock.getGbDgsLossWeight());
            recordEntity.setGbDgscLossSubtotal(stock.getGbDgsLossSubtotal());
            recordEntity.setGbDgscReturnWeight(stock.getGbDgsReturnWeight());
            recordEntity.setGbDgscReturnSubtotal(stock.getGbDgsReturnSubtotal());
            recordEntity.setGbDgscDoWasteWeight("0.0");
            recordEntity.setGbDgscDoWasteSubtotal("0.0");

        } else if (what.equals("return")) {
            //update all
            BigDecimal returnWeight = new BigDecimal(stock.getGbDgsReturnWeight()); // 已制作数量
            BigDecimal allReturnWeight = returnWeight.add(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP); //总制作数量
            allChangeSubtotal = allReturnWeight.multiply(price).doubleValue(); //总损耗成本
            recordEntity.setGbDgscReturnWeight(allReturnWeight.toString());
            recordEntity.setGbDgscReturnSubtotal(String.format("%.1f", allChangeSubtotal));
            recordEntity.setGbDgscProduceWeight(stock.getGbDgsProduceWeight());
            recordEntity.setGbDgscProduceSubtotal(stock.getGbDgsProduceSubtotal());
            recordEntity.setGbDgscLossWeight(stock.getGbDgsLossWeight());
            recordEntity.setGbDgscLossSubtotal(stock.getGbDgsLossSubtotal());
            recordEntity.setGbDgscDoWasteWeight("0.0");
            recordEntity.setGbDgscDoWasteSubtotal("0.0");

        } else if (what.equals("waste")) {
            //update all
            allChangeSubtotal = myChangeWeight.multiply(price).doubleValue(); //总损耗成本
            recordEntity.setGbDgscDoWasteWeight(myChangeWeight.toString());
            recordEntity.setGbDgscDoWasteSubtotal(String.format("%.1f", allChangeSubtotal));
            recordEntity.setGbDgscDoWasteUserId(stock.getGbDgsReduceWeightUserId());
            recordEntity.setGbDgscDoWasteFullTime(formatFullTime());
            recordEntity.setGbDgscProduceWeight(stock.getGbDgsProduceWeight());
            recordEntity.setGbDgscProduceSubtotal(stock.getGbDgsProduceSubtotal());
            recordEntity.setGbDgscLossWeight(stock.getGbDgsLossWeight());
            recordEntity.setGbDgscLossSubtotal(stock.getGbDgsLossSubtotal());
            recordEntity.setGbDgscReturnWeight(stock.getGbDgsReturnWeight());
            recordEntity.setGbDgscReturnSubtotal(stock.getGbDgsReturnSubtotal());

        }

        recordEntity.setGbDgscSellingPrice(stock.getGbDgsSellingPrice());
        recordEntity.setGbDgscSellingSubtotal(stock.getGbDgsSellingSubtotal());
        recordEntity.setGbDgscProfitPrice(stock.getGbDgsBetweenPrice());


        recordEntity.setGbDgscDate(stock.getGbDgsDate());
        recordEntity.setGbDgscFullTime(stock.getGbDgsFullTime());
        recordEntity.setGbDgscGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
        recordEntity.setGbDgscGbDistributerId(stock.getGbDgsGbDistributerId());
        recordEntity.setGbDgscGbDepartmentId(stock.getGbDgsGbDepartmentId());
        recordEntity.setGbDgscGbDepartmentOrderId(stock.getGbDgsGbDepartmentOrderId());
        recordEntity.setGbDgscGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
        recordEntity.setGbDgscGbPurGoodsId(stock.getGbDgsGbPurGoodsId());
        recordEntity.setGbDgscWarnFullTime(stock.getGbDgsWarnFullTime());
        recordEntity.setGbDgscWasteFullTime(stock.getGbDgsWasteFullTime());
        recordEntity.setGbDgscGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
        recordEntity.setGbDgscGbFromDepartmentId(stock.getGbDgsGbFromDepartmentId());
        recordEntity.setGbDgscGbPurGoodsId(stock.getGbDgsGbPurGoodsId());
        recordEntity.setGbDgscGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
        recordEntity.setGbDgscWeek(stock.getGbDgsWeek());
        recordEntity.setGbDgscMonth(stock.getGbDgsMonth());
        recordEntity.setGbDgscYear(stock.getGbDgsYear());
        recordEntity.setGbDgscTimeStamp(stock.getGbDgsTimeStamp());
        recordEntity.setGbDgscWeight(stock.getGbDgsWeight());
        recordEntity.setGbDgscSubtotal(stock.getGbDgsSubtotal());
        recordEntity.setGbDgscGbDepartmentOrderId(stock.getGbDgsGbDepartmentOrderId());
        recordEntity.setGbDgscPrice(stock.getGbDgsPrice());
        recordEntity.setGbDgscRestWeight("0.0");
        recordEntity.setGbDgscRestSubtotal("0.0");
        recordEntity.setGbDgscReceiveUserId(stock.getGbDgsReceiveUserId());
        recordEntity.setGbDgscStatus(0);
        recordEntity.setGbDgscInventoryDate(formatWhatDay(0));
        recordEntity.setGbDgscInventoryWeek(getWeekOfYear(0).toString());
        recordEntity.setGbDgscInventoryMonth(formatWhatMonth(0));
        recordEntity.setGbDgscGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
        //
        Integer howManyDaysInPeriod = getHowManyDaysInPeriod(formatWhatDay(0), stock.getGbDgsDate());
        recordEntity.setGbDgscInventoryMany(howManyDaysInPeriod.toString());
        Integer gbDgsGbDisGoodsId = stock.getGbDgsGbDisGoodsId();
        Integer gbDgGoodsInventoryType = disGoodsService.queryObject(gbDgsGbDisGoodsId).getGbDgGoodsInventoryType();
        recordEntity.setGbDgscDgInventoryType(gbDgGoodsInventoryType);
        gbDepGoodsStockRecordService.save(recordEntity);

        return recordEntity;

    }

    private void transSaveDepGoodsStockReduceEntity(GbDepartmentGoodsStockRecordEntity recordStock, String what, String myChangeWeight,
                                                    String myChangeSubtotal, Integer reduceWeightUserId, Integer inventoryType,
                                                    BigDecimal profitSubtotal, BigDecimal salesSubtotal) {

        GbDepartmentGoodsStockReduceEntity reduceEntity = new GbDepartmentGoodsStockReduceEntity();
        reduceEntity.setGbDgsrGbGoodsStockRecordId(recordStock.getGbDepartmentGoodsStockRecordId());
        reduceEntity.setGbDgsrGbDistributerId(recordStock.getGbDgscGbDistributerId());
        reduceEntity.setGbDgsrGbDepartmentId(recordStock.getGbDgscGbDepartmentId());
        reduceEntity.setGbDgsrGbDepartmentFatherId(recordStock.getGbDgscGbDepartmentFatherId());
        reduceEntity.setGbDgsrGbDisGoodsId(recordStock.getGbDgscGbDisGoodsId());
        reduceEntity.setGbDgsrGbGoodsInventoryType(inventoryType);
        reduceEntity.setGbDgsrGbDisGoodsFatherId(recordStock.getGbDgscGbDisGoodsFatherId());
        reduceEntity.setGbDgsrGbDepDisGoodsId(recordStock.getGbDgscGbDepDisGoodsId());
        reduceEntity.setGbDgsrGbDepDisGoodsId(recordStock.getGbDgscGbDepDisGoodsId());
        reduceEntity.setGbDgsrFullTime(formatFullTime());
        reduceEntity.setGbDgsrDate(formatWhatDay(0));
        reduceEntity.setGbDgsrWeek(getWeekOfYear(0).toString());
        reduceEntity.setGbDgsrMonth(formatWhatMonth(0));
        reduceEntity.setGbDgsrGbGoodsStockId(-1);
        reduceEntity.setGbDgsrDoUserId(reduceWeightUserId);
        reduceEntity.setGbDgsrGbPurGoodsId(recordStock.getGbDgscGbPurGoodsId());
        if (what.equals("loss")) {
            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeLoss());
            reduceEntity.setGbDgsrStatus(0);
            reduceEntity.setGbDgsrLossWeight(myChangeWeight);
            reduceEntity.setGbDgsrLossSubtotal(myChangeSubtotal);
            reduceEntity.setGbDgsrCostWeight("0");
            reduceEntity.setGbDgsrCostSubtotal("0");
            reduceEntity.setGbDgsrProduceWeight("0");
            reduceEntity.setGbDgsrProduceSubtotal("0");
            reduceEntity.setGbDgsrReturnWeight("0");
            reduceEntity.setGbDgsrReturnSubtotal("0");
            reduceEntity.setGbDgsrWasteWeight("0");
            reduceEntity.setGbDgsrWasteSubtotal("0");
            reduceEntity.setGbDgsrProfitSubtotal("0");
            reduceEntity.setGbDgsrSalesSubtotal("0");
        } else if (what.equals("produce")) {
            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeProduce());
            reduceEntity.setGbDgsrStatus(0);
            reduceEntity.setGbDgsrProduceWeight(myChangeWeight);
            reduceEntity.setGbDgsrProduceSubtotal(myChangeSubtotal);
            reduceEntity.setGbDgsrCostWeight(myChangeWeight);
            reduceEntity.setGbDgsrCostSubtotal(myChangeSubtotal);
            reduceEntity.setGbDgsrLossWeight("0");
            reduceEntity.setGbDgsrLossSubtotal("0");
            reduceEntity.setGbDgsrReturnWeight("0");
            reduceEntity.setGbDgsrReturnSubtotal("0");
            reduceEntity.setGbDgsrWasteWeight("0");
            reduceEntity.setGbDgsrWasteSubtotal("0");
            reduceEntity.setGbDgsrProfitSubtotal(myChangeWeight);
            reduceEntity.setGbDgsrSalesSubtotal(myChangeSubtotal);
        } else if (what.equals("waste")) {
            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeWaste());
            reduceEntity.setGbDgsrStatus(0);
            reduceEntity.setGbDgsrWasteWeight(myChangeWeight);
            reduceEntity.setGbDgsrWasteSubtotal(myChangeSubtotal);
            reduceEntity.setGbDgsrCostWeight("0");
            reduceEntity.setGbDgsrCostSubtotal("0");
            reduceEntity.setGbDgsrLossWeight("0");
            reduceEntity.setGbDgsrLossSubtotal("0");
            reduceEntity.setGbDgsrReturnWeight("0");
            reduceEntity.setGbDgsrReturnSubtotal("0");
            reduceEntity.setGbDgsrProduceWeight("0");
            reduceEntity.setGbDgsrProduceSubtotal("0");
            reduceEntity.setGbDgsrProfitSubtotal("0");
            reduceEntity.setGbDgsrSalesSubtotal("0");

        } else if (what.equals("return")) {
            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeReturn());
            reduceEntity.setGbDgsrStatus(-1);
            reduceEntity.setGbDgsrReturnWeight(myChangeWeight);
            reduceEntity.setGbDgsrReturnSubtotal(myChangeSubtotal);
            reduceEntity.setGbDgsrCostWeight("0");
            reduceEntity.setGbDgsrCostSubtotal("0");
            reduceEntity.setGbDgsrLossWeight("0");
            reduceEntity.setGbDgsrLossSubtotal("0");
            reduceEntity.setGbDgsrProduceWeight("0");
            reduceEntity.setGbDgsrProduceSubtotal("0");
            reduceEntity.setGbDgsrWasteWeight("0");
            reduceEntity.setGbDgsrWasteSubtotal("0");
            reduceEntity.setGbDgsrProfitSubtotal("0");
            reduceEntity.setGbDgsrSalesSubtotal("0");
        }

        gbDepartmentStockReduceService.save(reduceEntity);

    }

    private void transDaily(GbDepartmentGoodsStockEntity stock, String what, Integer stockRecordId) {
        System.out.println("transDailytransDaily===stock==" + stock);
        System.out.println("transDailytransDaily===what==" + what);
        System.out.println("transDailytransDaily===stockRecordId==" + stockRecordId);
        Map<String, Object> map = new HashMap<>();
        map.put("stockId", stock.getGbDepartmentGoodsStockId());
        List<GbDepartmentGoodsStockReduceEntity> reduceEntities = gbDepartmentStockReduceService.queryStockReduceListByParams(map);
        for (GbDepartmentGoodsStockReduceEntity reduce : reduceEntities) {
            reduce.setGbDgsrGbGoodsStockRecordId(stockRecordId);
            reduce.setGbDgsrGbGoodsStockId(-1);
            gbDepartmentStockReduceService.update(reduce);
        }

        GbDepInventoryGoodsDailyEntity goodsDailyEntity = changeDaily(stock, what);
        System.out.println("goodsdilenne" + goodsDailyEntity);
        System.out.println("stockididididi==" + stockRecordId);
        goodsDailyEntity.setGbIgdGbDepStockRecordId(stockRecordId);
        System.out.println("zenmeelelel" + goodsDailyEntity);
        goodsDailyEntity.setGbIgdGbDepStockId(-1);
        gbDepInventoryGoodsDailyService.update(goodsDailyEntity);

        gbDepGoodsStockService.delete(stock.getGbDepartmentGoodsStockId());

    }


    private void transWeek(GbDepartmentGoodsStockEntity stock, String what, Integer stockRecordId) {

        Map<String, Object> map = new HashMap<>();
        map.put("stockId", stock.getGbDepartmentGoodsStockId());
        List<GbDepartmentGoodsStockReduceEntity> reduceEntities = gbDepartmentStockReduceService.queryStockReduceListByParams(map);
        for (GbDepartmentGoodsStockReduceEntity reduce : reduceEntities) {
            reduce.setGbDgsrGbGoodsStockRecordId(stockRecordId);
            reduce.setGbDgsrGbGoodsStockId(-1);
            gbDepartmentStockReduceService.update(reduce);
        }

        GbDepInventoryGoodsWeekEntity goodsWeeekEntity = changeWeek(stock, what);
        goodsWeeekEntity.setGbIgwGbDepStockRecordId(stockRecordId);
        goodsWeeekEntity.setGbIgwGbDepStockId(-1);
        gbDepInventoryGoodsWeekService.update(goodsWeeekEntity);

        gbDepGoodsStockService.delete(stock.getGbDepartmentGoodsStockId());

    }


    private GbDepartmentGoodsStockReduceEntity changeDepartmentStock(GbDepartmentGoodsStockEntity stock, String what) {
        GbDepartmentGoodsStockReduceEntity reduceEntity = new GbDepartmentGoodsStockReduceEntity();
        System.out.println("whastttt" + what);
        BigDecimal myChangeWeight = new BigDecimal("0");
        BigDecimal myChangeSubtotal = new BigDecimal(0);


        BigDecimal newAfterProfitSubtotal = new BigDecimal(0);
        BigDecimal salesSubtotal = new BigDecimal(0);
        BigDecimal profitSubtotal = new BigDecimal((0));

        Integer gbDgsGbDisGoodsId = stock.getGbDgsGbDisGoodsId();
        GbDistributerGoodsEntity distributerGoodsEntity = disGoodsService.queryObject(gbDgsGbDisGoodsId);
        Integer gbDgGoodsInventoryType = distributerGoodsEntity.getGbDgGoodsInventoryType();

        //利润单价
        BigDecimal costPrice = new BigDecimal(stock.getGbDgsPrice()); //成本单价

        //1，更新stock
        // 1.1 如果是损耗接口
        if (what.equals("loss")) {
            //转换数据
            myChangeWeight = new BigDecimal(stock.getGbDgsMyLossWeight()).setScale(1, BigDecimal.ROUND_HALF_UP); // 最新提交待损耗数量
            myChangeSubtotal = myChangeWeight.multiply(costPrice).setScale(2, BigDecimal.ROUND_HALF_UP); //总损耗成本

            //销售利润=利润-成本
            if (stock.getGbDgsSellingPrice() != null && !stock.getGbDgsSellingPrice().equals("-1")) {
                BigDecimal stockAfterProfitSubtotal = new BigDecimal(stock.getGbDgsAfterProfitSubtotal()); //总的销售利润
                newAfterProfitSubtotal = stockAfterProfitSubtotal.subtract(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                stock.setGbDgsAfterProfitSubtotal(newAfterProfitSubtotal.toString());
            }

            //update
            BigDecimal allWeight = new BigDecimal(stock.getGbDgsLossWeight()).add(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
            BigDecimal allSubtotal = new BigDecimal(stock.getGbDgsLossSubtotal()).add(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
            stock.setGbDgsLossWeight(allWeight.toString());
            stock.setGbDgsLossSubtotal(allSubtotal.toString());

            reduceEntity = addDepGoodsStockReduceEntity(stock, "loss", gbDgGoodsInventoryType, myChangeWeight, myChangeSubtotal);
            subscribeDepDisGoodsTotal(myChangeWeight, myChangeSubtotal, stock.getGbDgsGbDepDisGoodsId());
            updateDepGoodsDailyEntity(stock, "loss", myChangeWeight, myChangeSubtotal);

        }

        // 1.2 如果是制作接口
        if (what.equals("produce")) {
            //转换数据
            myChangeWeight = new BigDecimal(stock.getGbDgsMyProduceWeight()).setScale(1, BigDecimal.ROUND_HALF_UP); // 最新提交待损耗数量
            myChangeSubtotal = myChangeWeight.multiply(costPrice).setScale(2, BigDecimal.ROUND_HALF_UP); //总制作成本

            //update
            BigDecimal allWeight = new BigDecimal(stock.getGbDgsProduceWeight()).add(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
            BigDecimal allSubtotal = new BigDecimal(stock.getGbDgsProduceSubtotal()).add(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
            stock.setGbDgsProduceWeight(allWeight.toString());
            stock.setGbDgsProduceSubtotal(allSubtotal.toString());

            if (!stock.getGbDgsSellingPrice().equals("-1")) {
                //利润
                BigDecimal gbDgsBetweenPrice = new BigDecimal(stock.getGbDgsBetweenPrice()); //生产利润单价
                BigDecimal newProfitSubtotal = gbDgsBetweenPrice.multiply(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                profitSubtotal = new BigDecimal(stock.getGbDgsProfitSubtotal()).add(newProfitSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                stock.setGbDgsProfitSubtotal(profitSubtotal.toString()); //
                //销售利润=总利润+利润
                BigDecimal stockAfterProfitSubtotal = new BigDecimal(stock.getGbDgsAfterProfitSubtotal()); //总的销售利润
                newAfterProfitSubtotal = stockAfterProfitSubtotal.add(newProfitSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                stock.setGbDgsAfterProfitSubtotal(newAfterProfitSubtotal.toString());

                BigDecimal newSellingSubtotal = new BigDecimal(stock.getGbDgsSellingPrice()).multiply(myChangeWeight);
                salesSubtotal = newSellingSubtotal.add(new BigDecimal(stock.getGbDgsProduceSellingSubtotal()));
                stock.setGbDgsProduceSellingSubtotal(salesSubtotal.toString());
                // 产生利润的数量
                BigDecimal add = new BigDecimal(stock.getGbDgsProfitWeight()).add(myChangeWeight);
                stock.setGbDgsProfitWeight(add.toString());

            }


            reduceEntity = addDepGoodsStockReduceEntity(stock, "produce", gbDgGoodsInventoryType, myChangeWeight, myChangeSubtotal);
            subscribeDepDisGoodsTotal(myChangeWeight, myChangeSubtotal, stock.getGbDgsGbDepDisGoodsId());
            updateDepGoodsDailyEntity(stock, "produce", myChangeWeight, myChangeSubtotal);

        }
        // 1.3 如果是退货接口
        if (what.equals("return")) {
            //转换数据
            myChangeWeight = new BigDecimal(stock.getGbDgsMyReturnWeight()).setScale(1, BigDecimal.ROUND_HALF_UP); // 最新提交待损耗数量
            myChangeSubtotal = myChangeWeight.multiply(costPrice).setScale(2, BigDecimal.ROUND_HALF_UP); //总损耗成本

            if (!stock.getGbDgsSellingPrice().equals("-1")) {

                BigDecimal stockAfterProfitSubtotal = new BigDecimal(stock.getGbDgsAfterProfitSubtotal()); //总的销售利润
                //销售利润=利润-成本
                profitSubtotal = stockAfterProfitSubtotal.subtract(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                stock.setGbDgsAfterProfitSubtotal(profitSubtotal.toString());

            }


            //update
            BigDecimal allWeight = new BigDecimal(stock.getGbDgsReturnWeight()).add(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
            BigDecimal allSubtotal = new BigDecimal(stock.getGbDgsReturnSubtotal()).add(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
            stock.setGbDgsReturnWeight(allWeight.toString());
            stock.setGbDgsReturnSubtotal(allSubtotal.toString());

            reduceEntity = addDepGoodsStockReduceEntity(stock, what, gbDgGoodsInventoryType, myChangeWeight, myChangeSubtotal);
            subscribeDepDisGoodsTotal(myChangeWeight, myChangeSubtotal, stock.getGbDgsGbDepDisGoodsId());
            updateDepGoodsDailyEntity(stock, what, myChangeWeight, myChangeSubtotal);

        }
        BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight()); // 剩余数量
        BigDecimal newRestWeight = restWeight.subtract(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP); //最新剩余数量
        BigDecimal newRestSubtotal = newRestWeight.multiply(costPrice).setScale(1, BigDecimal.ROUND_HALF_UP); //最新剩余成本
        stock.setGbDgsRestWeight(newRestWeight.toString());
        stock.setGbDgsRestSubtotal(newRestSubtotal.toString());

        // 1.4 如果是废弃接口
        if (what.equals("waste")) {
            //转换数据
            BigDecimal wasteWeight = new BigDecimal(stock.getGbDgsMyWasteWeight()); // 最新提交待损耗数量
            BigDecimal wasteSubtotal = wasteWeight.multiply(costPrice).setScale(1, BigDecimal.ROUND_HALF_UP); //最新剩余成本

            BigDecimal produceWeight = new BigDecimal(stock.getGbDgsMyProduceWeight()).setScale(1, BigDecimal.ROUND_HALF_UP); // 最新提交待损耗数量
            BigDecimal produceSubtotal = produceWeight.multiply(costPrice).setScale(2, BigDecimal.ROUND_HALF_UP); //总制作成本
            BigDecimal allWeightProduce = new BigDecimal(stock.getGbDgsProduceWeight()).add(produceWeight).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
            BigDecimal allSubtotalProduce = new BigDecimal(stock.getGbDgsProduceSubtotal()).add(produceSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
            myChangeWeight = wasteWeight.add(produceWeight);
            myChangeSubtotal = wasteSubtotal.add(produceSubtotal);
            if (!stock.getGbDgsSellingPrice().equals("-1")) {

                //利润
                BigDecimal gbDgsBetweenPrice = new BigDecimal(stock.getGbDgsBetweenPrice()); //生产利润单价
                BigDecimal newProfitSubtotal = gbDgsBetweenPrice.multiply(produceWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                profitSubtotal = new BigDecimal(stock.getGbDgsProfitSubtotal()).add(newProfitSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                stock.setGbDgsProfitSubtotal(profitSubtotal.toString()); //
                //销售利润=总利润+利润
                BigDecimal stockAfterProfitSubtotal = new BigDecimal(stock.getGbDgsAfterProfitSubtotal()); //总的销售利润
                newAfterProfitSubtotal = stockAfterProfitSubtotal.add(newProfitSubtotal).subtract(wasteSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                stock.setGbDgsAfterProfitSubtotal(newAfterProfitSubtotal.toString());

                BigDecimal newSellingSubtotal = new BigDecimal(stock.getGbDgsSellingPrice()).multiply(produceWeight);
                salesSubtotal = newSellingSubtotal.add(new BigDecimal(stock.getGbDgsProduceSellingSubtotal()));
                stock.setGbDgsProduceSellingSubtotal(salesSubtotal.toString());
                // 产生利润的数量
                BigDecimal add = new BigDecimal(stock.getGbDgsProfitWeight()).add(produceWeight);
                stock.setGbDgsProfitWeight(add.toString());

            }

            //update
            stock.setGbDgsWasteWeight(wasteWeight.toString());
            stock.setGbDgsWasteSubtotal(wasteSubtotal.toString());
            stock.setGbDgsProduceWeight(allWeightProduce.toString());
            stock.setGbDgsProduceSubtotal(allSubtotalProduce.toString());
            stock.setGbDgsRestWeight("0");
            stock.setGbDgsRestSubtotal("0.0");

            subscribeDepDisGoodsTotal(myChangeWeight, myChangeSubtotal, stock.getGbDgsGbDepDisGoodsId());

            updateDepGoodsDailyEntity(stock, what, wasteWeight, wasteSubtotal);
            addDepGoodsStockReduceEntity(stock, what, gbDgGoodsInventoryType, wasteWeight, wasteSubtotal);

            if (produceWeight.compareTo(BigDecimal.ZERO) == 1) {
                addDepGoodsStockReduceEntity(stock, "produce", gbDgGoodsInventoryType, produceWeight, produceSubtotal);
                updateDepGoodsDailyEntity(stock, "produce", produceWeight, produceSubtotal);
            }

        }


        stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
        stock.setGbDgsInventoryDate(formatWhatDay(0));
        stock.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
        stock.setGbDgsInventoryMonth(formatWhatMonth(0));
        stock.setGbDgsInventoryYear(formatWhatYear(0));

        // 转换showStandardWeight
        if (stock.getGbDgsRestWeightShowStandard() != null) {
            if (new BigDecimal(stock.getGbDgsRestWeightShowStandard()).compareTo(new BigDecimal(0)) == 1) {
                Integer gbDgsGbDepDisGoodsId = stock.getGbDgsGbDepDisGoodsId();
                GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDgsGbDepDisGoodsId);
                BigDecimal decimal = new BigDecimal(departmentDisGoodsEntity.getGbDdgShowStandardScale());
                BigDecimal myChangeWeightScale = myChangeWeight.divide(decimal, 1, BigDecimal.ROUND_HALF_UP);
                BigDecimal decimal1 = new BigDecimal(stock.getGbDgsRestWeightShowStandard()).subtract(myChangeWeightScale).setScale(1, BigDecimal.ROUND_HALF_UP);
                stock.setGbDgsRestWeightShowStandard(decimal1.toString());
                stock.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
            }
        }

        gbDepGoodsStockService.update(stock);

        if (stock.getGbDgsWeightGoodsId() != null && !what.equals("produce")) { //更新出库制作商品业务数据
            updateWeightGoodsData(stock, what, myChangeWeight);
        }

        return reduceEntity;
    }

    private void addDepStockReduceAttachment(GbDepartmentGoodsStockEntity stockEntity, GbDepartmentGoodsStockReduceEntity reduceEntity) {
        System.out.println("attachhhhccment" + stockEntity.getReduceAttachmentEntity());
        Integer gbDepartmentGoodsStockReduceId = reduceEntity.getGbDepartmentGoodsStockReduceId();
        GbDepartmentGoodsStockReduceAttachmentEntity reduceAttachmentEntity = stockEntity.getReduceAttachmentEntity();
        reduceAttachmentEntity.setGbDgsraGbDgsrId(gbDepartmentGoodsStockReduceId);
        gbDepGoodsStockReduceAttachmentService.save(reduceAttachmentEntity);
    }

    private void updateWeightGoodsData(GbDepartmentGoodsStockEntity stock, String what, BigDecimal myChangeWeight) {
        System.out.println("updateWeightGoodsDataupdateWeightGoodsData");
        Integer gbDgsWeightGoodsId = stock.getGbDgsWeightGoodsId();
        GbDistributerWeightGoodsEntity weightGoodsEntity = gbDisWeightGoodsService.queryObject(gbDgsWeightGoodsId);
        if (what.equals("loss")) {
            BigDecimal add = myChangeWeight.add(new BigDecimal(weightGoodsEntity.getGbDwgLossWeight()));
            weightGoodsEntity.setGbDwgLossWeight(add.toString());
            System.out.println("abccc" + weightGoodsEntity);
        }
        if (what.equals("waste")) {
            BigDecimal add = myChangeWeight.add(new BigDecimal(weightGoodsEntity.getGbDwgWasteWeight()));
            weightGoodsEntity.setGbDwgWasteWeight(add.toString());
        }
        if (what.equals("return")) {
            BigDecimal add = myChangeWeight.add(new BigDecimal(weightGoodsEntity.getGbDwgReturnWeight()));
            weightGoodsEntity.setGbDwgReturnWeight(add.toString());
        }
        gbDisWeightGoodsService.update(weightGoodsEntity);
    }

    private void updateOutStockData(GbDepartmentGoodsStockEntity stock, String what, BigDecimal myChangeWeight,
                                    BigDecimal myChangeSubtotal, BigDecimal salesSubtotal, BigDecimal profitSubtotal, BigDecimal afterProfitSubtotal) {
        System.out.println("updateoutoutotutstoo");
        Integer gbDgsGbGoodsStockId = stock.getGbDgsGbGoodsStockId();
        GbDepartmentGoodsStockEntity outStockEntity = gbDepGoodsStockService.queryObject(gbDgsGbGoodsStockId);
        if (what.equals("loss")) {
            BigDecimal add = myChangeWeight.add(new BigDecimal(outStockEntity.getGbDgsLossWeight()));
            BigDecimal addSub = myChangeSubtotal.add(new BigDecimal(outStockEntity.getGbDgsLossSubtotal()));
            outStockEntity.setGbDgsLossWeight(add.toString());
            outStockEntity.setGbDgsLossSubtotal(addSub.toString());
        }
        if (what.equals("produce")) {
            BigDecimal add = myChangeWeight.add(new BigDecimal(outStockEntity.getGbDgsProduceWeight()));
            BigDecimal addSub = myChangeSubtotal.add(new BigDecimal(outStockEntity.getGbDgsProduceSubtotal()));
            outStockEntity.setGbDgsProduceWeight(add.toString());
            outStockEntity.setGbDgsProduceSubtotal(addSub.toString());

        }
        if (what.equals("waste")) {
            BigDecimal add = myChangeWeight.add(new BigDecimal(outStockEntity.getGbDgsWasteWeight()));
            BigDecimal addSub = myChangeSubtotal.add(new BigDecimal(outStockEntity.getGbDgsWasteSubtotal()));
            outStockEntity.setGbDgsWasteWeight(add.toString());
            outStockEntity.setGbDgsWasteSubtotal(addSub.toString());
        }
        if (what.equals("return")) {
            BigDecimal add = myChangeWeight.add(new BigDecimal(outStockEntity.getGbDgsReturnWeight()));
            BigDecimal addSub = myChangeSubtotal.add(new BigDecimal(outStockEntity.getGbDgsReturnSubtotal()));
            outStockEntity.setGbDgsReturnWeight(add.toString());
            outStockEntity.setGbDgsReturnSubtotal(addSub.toString());
        }

        Integer gbDgsGbDisGoodsId = outStockEntity.getGbDgsGbDisGoodsId();
        GbDistributerGoodsEntity distributerGoodsEntity = disGoodsService.queryObject(gbDgsGbDisGoodsId);
        String gbDgSellingPrice = distributerGoodsEntity.getGbDgSellingPrice();
        if (gbDgSellingPrice != null && new BigDecimal(gbDgSellingPrice).compareTo(new BigDecimal(0)) == 1) {
            BigDecimal addProfit = profitSubtotal.add(new BigDecimal(outStockEntity.getGbDgsProfitSubtotal()));
            BigDecimal addSales = salesSubtotal.add(new BigDecimal(outStockEntity.getGbDgsSellingSubtotal()));
            BigDecimal addAfter = salesSubtotal.add(new BigDecimal(outStockEntity.getGbDgsAfterProfitSubtotal()));
            outStockEntity.setGbDgsProfitSubtotal(addProfit.toString());
            outStockEntity.setGbDgsProduceSellingSubtotal(addSales.toString());
            outStockEntity.setGbDgsAfterProfitSubtotal(addAfter.toString());
            BigDecimal add1 = myChangeWeight.add(new BigDecimal(outStockEntity.getGbDgsProfitWeight()));
            outStockEntity.setGbDgsProfitWeight(add1.toString());
        }


        gbDepGoodsStockService.update(outStockEntity);

    }

    private void updateDepGoodsDailyEntity(GbDepartmentGoodsStockEntity stock, String what, BigDecimal myChangeWeight,
                                           BigDecimal myChangeSubtotal) {
        Map<String, Object> map = new HashMap<>();
        map.put("depGoodsId", stock.getGbDgsGbDepDisGoodsId());
        map.put("date", formatWhatDay(0));
        System.out.println("updateDepDaily" + what);
        GbDepartmentGoodsDailyEntity depGoodsDailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(map);
        if (depGoodsDailyEntity != null) {
            BigDecimal weight = new BigDecimal(0);
            BigDecimal subtotal = new BigDecimal(0);
            BigDecimal newSalesProfitSubtotal = new BigDecimal(0);
            if (what.equals("loss")) {
                weight = myChangeWeight.add(new BigDecimal(depGoodsDailyEntity.getGbDgdLossWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                subtotal = myChangeSubtotal.add(new BigDecimal(depGoodsDailyEntity.getGbDgdLossSubtotal())).setScale(1, BigDecimal.ROUND_HALF_UP);
                depGoodsDailyEntity.setGbDgdLossWeight(weight.toString());
                depGoodsDailyEntity.setGbDgdLossSubtotal(subtotal.toString());

                if (!stock.getGbDgsSellingPrice().equals("-1")) {
                    newSalesProfitSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdAfterProfitSubtotal()).subtract(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    depGoodsDailyEntity.setGbDgdAfterProfitSubtotal(newSalesProfitSubtotal.toString());
                }
            }
            if (what.equals("produce")) {
                weight = myChangeWeight.add(new BigDecimal(depGoodsDailyEntity.getGbDgdProduceWeight()));
                subtotal = myChangeSubtotal.add(new BigDecimal(depGoodsDailyEntity.getGbDgdProduceSubtotal()));
                depGoodsDailyEntity.setGbDgdProduceWeight(weight.toString());
                depGoodsDailyEntity.setGbDgdProduceSubtotal(subtotal.toString());

                if (!stock.getGbDgsSellingPrice().equals("-1")) {
                    BigDecimal profitSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdProfitSubtotal());
                    BigDecimal myChangeProfitSubtotal = new BigDecimal(stock.getGbDgsBetweenPrice()).multiply(myChangeWeight).setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal newProfitSubtotal = myChangeProfitSubtotal.add(profitSubtotal);
                    depGoodsDailyEntity.setGbDgdProfitSubtotal(newProfitSubtotal.toString());

                    newSalesProfitSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdAfterProfitSubtotal()).add(myChangeProfitSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    depGoodsDailyEntity.setGbDgdAfterProfitSubtotal(newSalesProfitSubtotal.toString());
                    BigDecimal sellingPrice = new BigDecimal(stock.getGbDgsSellingPrice());
                    BigDecimal newSalesSubtotal = sellingPrice.multiply(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal salesSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdSalesSubtotal()).add(newSalesSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    depGoodsDailyEntity.setGbDgdSalesSubtotal(salesSubtotal.toString());
                }

                if (!stock.getGbDgsDate().equals(formatWhatDay(0))) { // 不是今天的批次
                    BigDecimal decimal = new BigDecimal(depGoodsDailyEntity.getGbDgdLastProduceWeight());
                    BigDecimal add = myChangeWeight.add(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    depGoodsDailyEntity.setGbDgdLastProduceWeight(add.toString());
                }

                //freshRate
                BigDecimal gbDgdProduceWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdProduceWeight());
                BigDecimal gbDgdLastWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdLastWeight());
                BigDecimal gbDgdLastProduceWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdLastProduceWeight());
                if (gbDgdLastWeight.compareTo(BigDecimal.ZERO) == 1) {
                    if (gbDgdProduceWeight.compareTo(gbDgdLastProduceWeight) == 1) {
                        BigDecimal subtract = gbDgdProduceWeight.subtract(gbDgdLastProduceWeight);
                        BigDecimal decimal = subtract.divide(gbDgdProduceWeight, 4, BigDecimal.ROUND_HALF_UP);
                        BigDecimal decimal1 = decimal.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        depGoodsDailyEntity.setGbDgdFreshRate(decimal1.toString());
                    } else {
                        depGoodsDailyEntity.setGbDgdFreshRate("0.0");
                    }
                } else {
                    depGoodsDailyEntity.setGbDgdFreshRate("100.00");
                }
            }

            if (what.equals("waste")) {
                weight = myChangeWeight.add(new BigDecimal(depGoodsDailyEntity.getGbDgdWasteWeight()));
                subtotal = myChangeSubtotal.add(new BigDecimal(depGoodsDailyEntity.getGbDgdWasteSubtotal()));
                depGoodsDailyEntity.setGbDgdWasteWeight(weight.toString());
                depGoodsDailyEntity.setGbDgdWasteSubtotal(subtotal.toString());
                if (!stock.getGbDgsSellingPrice().equals("-1")) {
                    newSalesProfitSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdAfterProfitSubtotal()).subtract(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    depGoodsDailyEntity.setGbDgdAfterProfitSubtotal(newSalesProfitSubtotal.toString());
                }
            }
            if (what.equals("return")) {
                weight = myChangeWeight.add(new BigDecimal(depGoodsDailyEntity.getGbDgdReturnWeight()));
                subtotal = myChangeSubtotal.add(new BigDecimal(depGoodsDailyEntity.getGbDgdReturnSubtotal()));
                depGoodsDailyEntity.setGbDgdReturnWeight(weight.toString());
                depGoodsDailyEntity.setGbDgdReturnSubtotal(subtotal.toString());
                System.out.println("returnrnrnrnrn" + depGoodsDailyEntity.getGbDgdReturnWeight());
            }

            // update restWeight
            BigDecimal newRestWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdRestWeight()).subtract(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            depGoodsDailyEntity.setGbDgdRestWeight(newRestWeight.toString());
            depGoodsDailyEntity.setGbDgdFullTime(formatFullTime());
            if (newRestWeight.compareTo(BigDecimal.ZERO) == 0) {
                Calendar calendar = Calendar.getInstance();
                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);
                depGoodsDailyEntity.setGbDgdSellClearHour(Integer.toString(hours));
                depGoodsDailyEntity.setGbDgdSellClearMinute(Integer.toString(minutes));
            }

            // update LastWeight
//            Integer gbDgsGbDisGoodsId = stock.getGbDgsGbDisGoodsId();
//            GbDistributerGoodsEntity distributerGoodsEntity = disGoodsService.queryObject(gbDgsGbDisGoodsId);
//            Integer gbDgControlFresh = distributerGoodsEntity.getGbDgControlFresh();

            //todo
//            if (!what.equals("produce") && gbDgControlFresh == 1) {
//                BigDecimal lastWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdLastWeight());
//                if (lastWeight.compareTo(BigDecimal.ZERO) == 1) {
//                    String gbDgsDate = stock.getGbDgsDate();
//                    if (!gbDgsDate.equals(formatWhatDay(0))) { // 不是今天的批次
//                        depGoodsDailyEntity.setGbDgdFreshRate("0.0");
//                    }
//                }
//            }
            gbDepGoodsDailyService.update(depGoodsDailyEntity);
        }
    }

    private GbDepartmentGoodsStockReduceEntity addDepGoodsStockReduceEntity(GbDepartmentGoodsStockEntity stock, String what, Integer inventoryType, BigDecimal myChangeWeight,
                                                                            BigDecimal myChangeSubtotal) {

        GbDepartmentGoodsStockReduceEntity reduceEntity = new GbDepartmentGoodsStockReduceEntity();
        reduceEntity.setGbDgsrGbDistributerId(stock.getGbDgsGbDistributerId());
        reduceEntity.setGbDgsrGbDepartmentId(stock.getGbDgsGbDepartmentId());
        reduceEntity.setGbDgsrGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
        reduceEntity.setGbDgsrGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
        reduceEntity.setGbDgsrGbGoodsInventoryType(inventoryType);
        reduceEntity.setGbDgsrGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
        reduceEntity.setGbDgsrGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
        reduceEntity.setGbDgsrGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
        reduceEntity.setGbDgsrFullTime(formatFullTime());
        reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReduceWeightUserId());
        reduceEntity.setGbDgsrDate(formatWhatDay(0));
        reduceEntity.setGbDgsrStockNxDistribtuerId(stock.getGbDgsNxDistributerId());
        reduceEntity.setGbDgsrWeek(getWeekOfYear(0).toString());
        reduceEntity.setGbDgsrMonth(formatWhatMonth(0));
        reduceEntity.setGbDgsrGbPurGoodsId(stock.getGbDgsGbPurGoodsId());
        if (what.equals("loss")) {
            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeLoss());
            reduceEntity.setGbDgsrStatus(0);
            reduceEntity.setGbDgsrLossWeight(myChangeWeight.toString());
            reduceEntity.setGbDgsrLossSubtotal(myChangeSubtotal.toString());
            reduceEntity.setGbDgsrProduceWeight("0");
            reduceEntity.setGbDgsrProduceSubtotal("0");
            reduceEntity.setGbDgsrReturnWeight("0");
            reduceEntity.setGbDgsrReturnSubtotal("0");
            reduceEntity.setGbDgsrWasteWeight("0");
            reduceEntity.setGbDgsrWasteSubtotal("0");
            reduceEntity.setGbDgsrCostWeight(myChangeWeight.toString());
            reduceEntity.setGbDgsrCostSubtotal(myChangeSubtotal.toString());

        } else if (what.equals("produce")) {
            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeProduce());
            reduceEntity.setGbDgsrStatus(0);
            reduceEntity.setGbDgsrProduceWeight(myChangeWeight.toString());
            reduceEntity.setGbDgsrProduceSubtotal(myChangeSubtotal.toString());
            reduceEntity.setGbDgsrLossWeight("0");
            reduceEntity.setGbDgsrLossSubtotal("0");
            reduceEntity.setGbDgsrReturnWeight("0");
            reduceEntity.setGbDgsrReturnSubtotal("0");
            reduceEntity.setGbDgsrWasteWeight("0");
            reduceEntity.setGbDgsrWasteSubtotal("0");
            reduceEntity.setGbDgsrCostWeight(myChangeWeight.toString());
            reduceEntity.setGbDgsrCostSubtotal(myChangeSubtotal.toString());

        } else if (what.equals("return")) {
            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeReturn());
            reduceEntity.setGbDgsrStatus(-1);
            reduceEntity.setGbDgsrReturnWeight(myChangeWeight.toString());
            reduceEntity.setGbDgsrReturnSubtotal(myChangeSubtotal.toString());
            reduceEntity.setGbDgsrLossWeight("0");
            reduceEntity.setGbDgsrLossSubtotal("0");
            reduceEntity.setGbDgsrProduceWeight("0");
            reduceEntity.setGbDgsrProduceSubtotal("0");
            reduceEntity.setGbDgsrWasteWeight("0");
            reduceEntity.setGbDgsrWasteSubtotal("0");
            reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReturnUserId());
            reduceEntity.setGbDgsrCostWeight(myChangeWeight.toString());
            reduceEntity.setGbDgsrCostSubtotal(myChangeSubtotal.toString());
        } else if (what.equals("waste")) {
            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeWaste());
            reduceEntity.setGbDgsrStatus(0);
            reduceEntity.setGbDgsrWasteWeight(myChangeWeight.toString());
            reduceEntity.setGbDgsrWasteSubtotal(myChangeSubtotal.toString());
            reduceEntity.setGbDgsrLossWeight("0");
            reduceEntity.setGbDgsrLossSubtotal("0");
            reduceEntity.setGbDgsrProduceWeight("0");
            reduceEntity.setGbDgsrProduceSubtotal("0");
            reduceEntity.setGbDgsrSalesSubtotal("0");
            reduceEntity.setGbDgsrReturnWeight("0");
            reduceEntity.setGbDgsrReturnSubtotal("0");
            reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReturnUserId());
        }

        gbDepartmentStockReduceService.save(reduceEntity);
        return reduceEntity;

    }


    private GbDepartmentDisGoodsEntity subscribeDepDisGoodsTotal(BigDecimal weight, BigDecimal subtotal, Integer depDisGoodsId) {
        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        BigDecimal weightB = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).subtract(weight);
        BigDecimal subtotalB = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).subtract(subtotal);
        if (new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale()).compareTo(new BigDecimal(0)) == 1) {
            BigDecimal showScale = new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale());
            BigDecimal showWeight = weightB.divide(showScale, 1, BigDecimal.ROUND_HALF_UP);
            depDisGoodsEntity.setGbDdgShowStandardWeight(showWeight.toString());
        }
        depDisGoodsEntity.setGbDdgStockTotalSubtotal(subtotalB.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgStockTotalWeight(weightB.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        gbDepartmentDisGoodsService.update(depDisGoodsEntity);
        return depDisGoodsEntity;
    }


    private GbDepInventoryGoodsDailyEntity changeDaily(GbDepartmentGoodsStockEntity stock, String what) {
        System.out.println("stockckkkcc" + stock.getGbDepartmentGoodsStockId());
        GbDepInventoryGoodsDailyEntity returnGoodsDailyEntity;
        BigDecimal myChangeWeight = new BigDecimal("0");
        BigDecimal price = new BigDecimal(stock.getGbDgsPrice()); //单价

        if (what.equals("loss")) {
            myChangeWeight = new BigDecimal(stock.getGbDgsMyLossWeight()); // 最新提交待损耗数量
        }
        if (what.equals("produce")) {
            myChangeWeight = new BigDecimal(stock.getGbDgsMyProduceWeight()); // 最新提交zhizuo数量
        }
//        if (what.equals("waste")) {
//            myChangeWeight = new BigDecimal(stock.getGbDgsDoWasteWeight()); // 最新提交待损耗数量
//        }
        if (what.equals("return")) {
            myChangeWeight = new BigDecimal(stock.getGbDgsMyReturnWeight()); // 最新提交待损耗数量
        }
        BigDecimal myChangeSubtotal = myChangeWeight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP); //最新提交损耗成本
        BigDecimal profitPriceD = new BigDecimal(stock.getGbDgsBetweenPrice());
        BigDecimal myChangeProfitSubtotal = myChangeWeight.multiply(profitPriceD).setScale(1, BigDecimal.ROUND_HALF_UP);


        //2，更新-inventoryGoodsDaily
        Map<String, Object> map = new HashMap<>();
        map.put("stockId", stock.getGbDepartmentGoodsStockId());
        map.put("date", formatWhatDay(0));
        map.put("depId", stock.getGbDgsGbDepartmentId());
        GbDepInventoryGoodsDailyEntity resultGoodsDailyEntity = gbDepInventoryGoodsDailyService.queryDailyStockItemByParams(map);
        if (resultGoodsDailyEntity != null) {
            returnGoodsDailyEntity = resultGoodsDailyEntity;
            double allWeight = 0.0;
            double allProfit = 0.0;
            if (what.equals("loss")) {
                myChangeWeight.add(new BigDecimal(resultGoodsDailyEntity.getGbIgdLossWeight())).doubleValue();
                resultGoodsDailyEntity.setGbIgdLossWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                resultGoodsDailyEntity.setGbIgdLossSubtotal(multiply.toString());

            }
            if (what.equals("produce")) {

                allWeight = myChangeWeight.add(new BigDecimal(resultGoodsDailyEntity.getGbIgdProduceWeight())).doubleValue();
                resultGoodsDailyEntity.setGbIgdProduceWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                resultGoodsDailyEntity.setGbIgdProduceSubtotal(multiply.toString());
                allProfit = myChangeProfitSubtotal.add(new BigDecimal(resultGoodsDailyEntity.getGbIgdProfitSubtotal())).doubleValue();
                resultGoodsDailyEntity.setGbIgdProfitSubtotal(String.format("%.1f", allProfit));
            }
            if (what.equals("waste")) {
                allWeight = myChangeWeight.add(new BigDecimal(resultGoodsDailyEntity.getGbIgdWasteWeight())).doubleValue();
                resultGoodsDailyEntity.setGbIgdWasteWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                resultGoodsDailyEntity.setGbIgdWasteSubtotal(multiply.toString());
            }
            if (what.equals("return")) {
                allWeight = myChangeWeight.add(new BigDecimal(resultGoodsDailyEntity.getGbIgdReturnWeight())).doubleValue();
                resultGoodsDailyEntity.setGbIgdReturnWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                resultGoodsDailyEntity.setGbIgdReturnSubtotal(multiply.toString());
            }
            gbDepInventoryGoodsDailyService.update(resultGoodsDailyEntity);
        } else {
            GbDepInventoryGoodsDailyEntity resultGoodsDailyNew = new GbDepInventoryGoodsDailyEntity();
            resultGoodsDailyNew.setGbIgdDepartmentId(stock.getGbDgsGbDepartmentId());
            resultGoodsDailyNew.setGbIgdDate(formatWhatDay(0));
            resultGoodsDailyNew.setGbIgdDisGoodsId(stock.getGbDgsGbDisGoodsId());
            resultGoodsDailyNew.setGbIgdDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
            resultGoodsDailyNew.setGbIgdDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            resultGoodsDailyNew.setGbIgdGbDepStockId(stock.getGbDepartmentGoodsStockId());
            resultGoodsDailyNew.setGbIgdDistributerId(stock.getGbDgsGbDistributerId());
            resultGoodsDailyNew.setGbIgdDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
            resultGoodsDailyNew.setGbIgdFullTime(formatFullTime());
            resultGoodsDailyNew.setGbIgdWasteWeight("0");
            resultGoodsDailyNew.setGbIgdWasteSubtotal("0");
            if (what.equals("loss")) {
                resultGoodsDailyNew.setGbIgdLossWeight(myChangeWeight.toString());
                resultGoodsDailyNew.setGbIgdLossSubtotal(myChangeSubtotal.toString());
                resultGoodsDailyNew.setGbIgdReturnWeight("0");
                resultGoodsDailyNew.setGbIgdReturnSubtotal("0");
                resultGoodsDailyNew.setGbIgdProduceWeight("0");
                resultGoodsDailyNew.setGbIgdProduceSubtotal("0");
                resultGoodsDailyNew.setGbIgdProfitSubtotal("0");
                resultGoodsDailyNew.setGbIgdWasteWeight("0");
                resultGoodsDailyNew.setGbIgdWasteSubtotal("0");

            }
            if (what.equals("waste")) {
                resultGoodsDailyNew.setGbIgdWasteWeight(myChangeWeight.toString());
                resultGoodsDailyNew.setGbIgdWasteSubtotal(myChangeWeight.toString());
                resultGoodsDailyNew.setGbIgdLossWeight("0");
                resultGoodsDailyNew.setGbIgdLossSubtotal("0");
                resultGoodsDailyNew.setGbIgdReturnWeight("0");
                resultGoodsDailyNew.setGbIgdReturnSubtotal("0");
                resultGoodsDailyNew.setGbIgdProduceWeight("0");
                resultGoodsDailyNew.setGbIgdProduceSubtotal("0");
                resultGoodsDailyNew.setGbIgdProfitSubtotal("0");
            }
            if (what.equals("produce")) {
                resultGoodsDailyNew.setGbIgdProduceWeight(myChangeWeight.toString());
                resultGoodsDailyNew.setGbIgdProduceSubtotal(myChangeSubtotal.toString());
                resultGoodsDailyNew.setGbIgdProfitSubtotal(myChangeProfitSubtotal.toString());
                resultGoodsDailyNew.setGbIgdReturnWeight("0");
                resultGoodsDailyNew.setGbIgdReturnSubtotal("0");
                resultGoodsDailyNew.setGbIgdLossWeight("0");
                resultGoodsDailyNew.setGbIgdLossSubtotal("0");
                resultGoodsDailyNew.setGbIgdWasteWeight("0");
                resultGoodsDailyNew.setGbIgdWasteSubtotal("0");
            }
            if (what.equals("return")) {
                resultGoodsDailyNew.setGbIgdReturnWeight(myChangeWeight.toString());
                resultGoodsDailyNew.setGbIgdReturnSubtotal(myChangeSubtotal.toString());
                resultGoodsDailyNew.setGbIgdProduceWeight("0");
                resultGoodsDailyNew.setGbIgdProduceSubtotal("0");
                resultGoodsDailyNew.setGbIgdProfitSubtotal("0");
                resultGoodsDailyNew.setGbIgdLossWeight("0");
                resultGoodsDailyNew.setGbIgdLossSubtotal("0");
                resultGoodsDailyNew.setGbIgdWasteWeight("0");
                resultGoodsDailyNew.setGbIgdWasteSubtotal("0");
            }
            resultGoodsDailyNew.setGbIgdStatus(0);
            gbDepInventoryGoodsDailyService.save(resultGoodsDailyNew);
            returnGoodsDailyEntity = resultGoodsDailyNew;
        }

        //2.5 新加 inventoryGoodsDailyTotal
        Map<String, Object> mapTotal = new HashMap<>();
        mapTotal.put("date", formatWhatDay(0));
        mapTotal.put("depId", stock.getGbDgsGbDepartmentId());
        mapTotal.put("depGoodsId", stock.getGbDgsGbDepDisGoodsId());
        GbDepInventoryGoodsDailyTotalEntity totalEntity = gbDepInventoryGoodsDailyTotalService.queryDailyTotalItemByParams(mapTotal);
        if (totalEntity != null) {
            System.out.println("hasOldResultGoodsDailyenityt!!!!");
            double allWeight = 0.0;
            double allProfit = 0.0;

            if (what.equals("loss")) {
                allWeight = myChangeWeight.add(new BigDecimal(totalEntity.getGbIgdtLossWeight())).doubleValue();
                totalEntity.setGbIgdtLossWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                totalEntity.setGbIgdtLossSubtotal(multiply.toString());
            }
            if (what.equals("produce")) {
                allWeight = myChangeWeight.add(new BigDecimal(totalEntity.getGbIgdtProduceWeight())).doubleValue();
                totalEntity.setGbIgdtProduceWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                totalEntity.setGbIgdtProduceSubtotal(multiply.toString());

                allProfit = myChangeProfitSubtotal.add(new BigDecimal(totalEntity.getGbIgdtProfitSubtotal())).doubleValue();
                totalEntity.setGbIgdtProfitSubtotal(String.format("%.1f", allProfit));
                System.out.println("channanaprorororo" + allProfit);

            }
            if (what.equals("waste")) {
                allWeight = myChangeWeight.add(new BigDecimal(totalEntity.getGbIgdtWasteWeight())).doubleValue();
                totalEntity.setGbIgdtWasteWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                totalEntity.setGbIgdtWasteSubtotal(multiply.toString());
            }
            if (what.equals("return")) {
                allWeight = myChangeWeight.add(new BigDecimal(totalEntity.getGbIgdtReturnWeight())).doubleValue();
                totalEntity.setGbIgdtReturnWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                totalEntity.setGbIgdtReturnSubtotal(multiply.toString());
            }
            gbDepInventoryGoodsDailyTotalService.update(totalEntity);
        } else {
            GbDepInventoryGoodsDailyTotalEntity resultGoodsDailyTotalNew = new GbDepInventoryGoodsDailyTotalEntity();
            resultGoodsDailyTotalNew.setGbIgdtDepartmentId(stock.getGbDgsGbDepartmentId());
            resultGoodsDailyTotalNew.setGbIgdtDate(formatWhatDay(0));
            resultGoodsDailyTotalNew.setGbIgdtDisGoodsId(stock.getGbDgsGbDisGoodsId());
            resultGoodsDailyTotalNew.setGbIgdtDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
            resultGoodsDailyTotalNew.setGbIgdtDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            resultGoodsDailyTotalNew.setGbIgdtDistributerId(stock.getGbDgsGbDistributerId());
            resultGoodsDailyTotalNew.setGbIgdtDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
            resultGoodsDailyTotalNew.setGbIgdtFullTime(formatFullTime());
            if (what.equals("loss")) {
                resultGoodsDailyTotalNew.setGbIgdtLossWeight(myChangeWeight.toString());
                resultGoodsDailyTotalNew.setGbIgdtLossSubtotal(myChangeSubtotal.toString());
                resultGoodsDailyTotalNew.setGbIgdtReturnWeight("0");
                resultGoodsDailyTotalNew.setGbIgdtReturnSubtotal("0");
                resultGoodsDailyTotalNew.setGbIgdtProduceWeight("0");
                resultGoodsDailyTotalNew.setGbIgdtProduceSubtotal("0");
                resultGoodsDailyTotalNew.setGbIgdtProfitSubtotal("0");
                resultGoodsDailyTotalNew.setGbIgdtWasteWeight("0");
                resultGoodsDailyTotalNew.setGbIgdtWasteSubtotal("0");
            }
            if (what.equals("waste")) {
                resultGoodsDailyTotalNew.setGbIgdtWasteWeight(myChangeWeight.toString());
                resultGoodsDailyTotalNew.setGbIgdtWasteSubtotal(myChangeWeight.toString());
                resultGoodsDailyTotalNew.setGbIgdtLossWeight("0");
                resultGoodsDailyTotalNew.setGbIgdtLossSubtotal("0");
                resultGoodsDailyTotalNew.setGbIgdtReturnWeight("0");
                resultGoodsDailyTotalNew.setGbIgdtReturnSubtotal("0");
                resultGoodsDailyTotalNew.setGbIgdtProduceWeight("0");
                resultGoodsDailyTotalNew.setGbIgdtProfitSubtotal("0");
                resultGoodsDailyTotalNew.setGbIgdtProduceSubtotal("0");
            }
            if (what.equals("produce")) {
                resultGoodsDailyTotalNew.setGbIgdtProduceWeight(myChangeWeight.toString());
                resultGoodsDailyTotalNew.setGbIgdtProduceSubtotal(myChangeSubtotal.toString());
                resultGoodsDailyTotalNew.setGbIgdtProfitSubtotal(myChangeProfitSubtotal.toString());
                resultGoodsDailyTotalNew.setGbIgdtReturnWeight("0");
                resultGoodsDailyTotalNew.setGbIgdtReturnSubtotal("0");
                resultGoodsDailyTotalNew.setGbIgdtLossWeight("0");
                resultGoodsDailyTotalNew.setGbIgdtLossSubtotal("0");
                resultGoodsDailyTotalNew.setGbIgdtWasteWeight("0");
                resultGoodsDailyTotalNew.setGbIgdtWasteSubtotal("0");
                //12345
                System.out.println("kanaknprofiit" + myChangeProfitSubtotal);

            }
            if (what.equals("return")) {
                resultGoodsDailyTotalNew.setGbIgdtReturnWeight(myChangeWeight.toString());
                resultGoodsDailyTotalNew.setGbIgdtReturnSubtotal(myChangeSubtotal.toString());
                resultGoodsDailyTotalNew.setGbIgdtProduceWeight("0");
                resultGoodsDailyTotalNew.setGbIgdtProduceSubtotal("0");
                resultGoodsDailyTotalNew.setGbIgdtProfitSubtotal("0");
                resultGoodsDailyTotalNew.setGbIgdtLossWeight("0");
                resultGoodsDailyTotalNew.setGbIgdtLossSubtotal("0");
                resultGoodsDailyTotalNew.setGbIgdtWasteWeight("0");
                resultGoodsDailyTotalNew.setGbIgdtWasteSubtotal("0");
            }
            resultGoodsDailyTotalNew.setGbIgdtStatus(0);
            gbDepInventoryGoodsDailyTotalService.save(resultGoodsDailyTotalNew);
        }


        //3，更新Dep的inventoryDaily表
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depId", stock.getGbDgsGbDepartmentId());
        map1.put("date", formatWhatDay(0));

        GbDepInventoryDailyEntity dailyEntity = gbDepInventoryDailyService.queryInventoryDaily(map1);
        if (dailyEntity != null) {
            BigDecimal bigDecimal = new BigDecimal("0");
            if (what.equals("loss")) {
                bigDecimal = new BigDecimal(dailyEntity.getGbIdLossTotal());
                BigDecimal add = bigDecimal.add(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                dailyEntity.setGbIdLossTotal(add.toString());
            }
            if (what.equals("waste")) {
                bigDecimal = new BigDecimal(dailyEntity.getGbIdWasteTotal());
                BigDecimal add = bigDecimal.add(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                dailyEntity.setGbIdWasteTotal(add.toString());
            }
            if (what.equals("produce")) {
                bigDecimal = new BigDecimal(dailyEntity.getGbIdProduceTotal());
                BigDecimal add = bigDecimal.add(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                dailyEntity.setGbIdProduceTotal(add.toString());
            }
            if (what.equals("return")) {
                bigDecimal = new BigDecimal(dailyEntity.getGbIdReturnTotal());
                BigDecimal add = bigDecimal.add(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                dailyEntity.setGbIdReturnTotal(add.toString());
            }
            gbDepInventoryDailyService.update(dailyEntity);

        } else {
            GbDepInventoryDailyEntity dailyEntity1 = new GbDepInventoryDailyEntity();
            dailyEntity1.setGbIdDepartmentId(stock.getGbDgsGbDepartmentId());
            dailyEntity1.setGbIdDate(formatWhatDay(0));
            dailyEntity1.setGbIdDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            dailyEntity1.setGbIdDistributerId(stock.getGbDgsGbDistributerId());
            dailyEntity1.setGbIdWasteTotal("0");
            if (what.equals("waste")) {
                dailyEntity1.setGbIdWasteTotal(myChangeSubtotal.toString());
                dailyEntity1.setGbIdReturnTotal("0");
                dailyEntity1.setGbIdProduceTotal("0");
                dailyEntity1.setGbIdLossTotal("0");
            }
            if (what.equals("loss")) {
                dailyEntity1.setGbIdLossTotal(myChangeSubtotal.toString());
                dailyEntity1.setGbIdReturnTotal("0");
                dailyEntity1.setGbIdProduceTotal("0");
                dailyEntity1.setGbIdWasteTotal("0");
            }
            if (what.equals("produce")) {
                dailyEntity1.setGbIdProduceTotal(myChangeSubtotal.toString());
                dailyEntity1.setGbIdReturnTotal("0");
                dailyEntity1.setGbIdLossTotal("0");
                dailyEntity1.setGbIdWasteTotal("0");
            }
            if (what.equals("return")) {
                dailyEntity1.setGbIdLossTotal("0");
                dailyEntity1.setGbIdReturnTotal(myChangeSubtotal.toString());
                dailyEntity1.setGbIdProduceTotal("0");
                dailyEntity1.setGbIdWasteTotal("0");
            }
            dailyEntity1.setGbIdWeek(getWeekOfYear(0).toString());
            dailyEntity1.setGbIdYear(formatWhatYear(0));
            dailyEntity1.setGbIdStatus(0);
            gbDepInventoryDailyService.save(dailyEntity1);
        }
        return returnGoodsDailyEntity;

    }


    private GbDepInventoryGoodsWeekEntity changeWeek(GbDepartmentGoodsStockEntity stock, String what) {

        GbDepInventoryGoodsWeekEntity goodsWeekEntity = new GbDepInventoryGoodsWeekEntity();
        BigDecimal myChangeWeight = new BigDecimal("0");
        BigDecimal price = new BigDecimal(stock.getGbDgsPrice()); //单价
        if (what.equals("loss")) {
            myChangeWeight = new BigDecimal(stock.getGbDgsMyLossWeight()); // 最新提交待损耗数量
        }
        if (what.equals("produce")) {
            myChangeWeight = new BigDecimal(stock.getGbDgsMyProduceWeight()); // 最新提交待损耗数量
        }
//        if (what.equals("waste")) {
//            myChangeWeight = new BigDecimal(stock.getGbDgsDoWasteWeight()); // 最新提交待损耗数量
//        }
        if (what.equals("return")) {
            myChangeWeight = new BigDecimal(stock.getGbDgsMyReturnWeight()); // 最新提交待损耗数量
        }

        BigDecimal myChangeSubtotal = myChangeWeight.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP); //最新提交损耗成本

        //2，更新-inventoryGoodsWeek
        Map<String, Object> map = new HashMap<>();
        map.put("stockId", stock.getGbDepartmentGoodsStockId());
        map.put("week", getWeekOfYear(0));
        map.put("depId", stock.getGbDgsGbDepartmentId());
        List<GbDepInventoryGoodsWeekEntity> depInventoryGoodsWeekEntities = gbDepInventoryGoodsWeekService.queryWeekStockByParams(map);
        if (depInventoryGoodsWeekEntities.size() > 0) {
            //在最后一条盘库记录记录添加损耗
            int index = depInventoryGoodsWeekEntities.size() - 1;
            goodsWeekEntity = depInventoryGoodsWeekEntities.get(index);
            double allWeight = 0.0;
            if (what.equals("loss")) {
                allWeight = myChangeWeight.add(new BigDecimal(goodsWeekEntity.getGbIgwLossWeight())).doubleValue();
                goodsWeekEntity.setGbIgwLossWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);
                goodsWeekEntity.setGbIgwLossSubtotal(multiply.toString());
            }
            if (what.equals("produce")) {
                allWeight = myChangeWeight.add(new BigDecimal(goodsWeekEntity.getGbIgwProduceWeight())).doubleValue();
                goodsWeekEntity.setGbIgwProduceWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);
                goodsWeekEntity.setGbIgwProduceSubtotal(multiply.toString());
            }
            if (what.equals("waste")) {
                allWeight = myChangeWeight.add(new BigDecimal(goodsWeekEntity.getGbIgwWasteWeight())).doubleValue();
                goodsWeekEntity.setGbIgwWasteWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);
                goodsWeekEntity.setGbIgwWasteSubtotal(multiply.toString());
            }
            if (what.equals("return")) {
                allWeight = myChangeWeight.add(new BigDecimal(goodsWeekEntity.getGbIgwReturnWeight())).doubleValue();
                goodsWeekEntity.setGbIgwReturnWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);
                goodsWeekEntity.setGbIgwReturnSubtotal(multiply.toString());
            }
            gbDepInventoryGoodsWeekService.update(goodsWeekEntity);
        } else {
            //添加一条新盘库记录
            goodsWeekEntity.setGbIgwDepartmentId(stock.getGbDgsGbDepartmentId());
            goodsWeekEntity.setGbIgwDisGoodsId(stock.getGbDgsGbDisGoodsId());
            goodsWeekEntity.setGbIgwDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
            goodsWeekEntity.setGbIgwDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            goodsWeekEntity.setGbIgwWeek(getWeekOfYear(0).toString());
            goodsWeekEntity.setGbIgwYear(formatWhatYear(0));
            goodsWeekEntity.setGbIgwGbDepStockId(stock.getGbDepartmentGoodsStockId());
            goodsWeekEntity.setGbIgwDistributerId(stock.getGbDgsGbDistributerId());
            goodsWeekEntity.setGbIgwDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
            goodsWeekEntity.setGbIgwFullTime(formatFullTime());

            if (what.equals("loss")) {
                goodsWeekEntity.setGbIgwLossWeight(myChangeWeight.toString());
                goodsWeekEntity.setGbIgwLossSubtotal(myChangeSubtotal.toString());
                goodsWeekEntity.setGbIgwReturnWeight("0");
                goodsWeekEntity.setGbIgwReturnSubtotal("0");
                goodsWeekEntity.setGbIgwWasteWeight("0");
                goodsWeekEntity.setGbIgwWasteSubtotal("0");
                goodsWeekEntity.setGbIgwProduceWeight("0");
                goodsWeekEntity.setGbIgwProduceSubtotal("0");
            }
            if (what.equals("produce")) {
                goodsWeekEntity.setGbIgwProduceWeight(myChangeWeight.toString());
                goodsWeekEntity.setGbIgwProduceSubtotal(myChangeSubtotal.toString());
                goodsWeekEntity.setGbIgwReturnWeight("0");
                goodsWeekEntity.setGbIgwReturnSubtotal("0");
                goodsWeekEntity.setGbIgwWasteWeight("0");
                goodsWeekEntity.setGbIgwWasteSubtotal("0");
                goodsWeekEntity.setGbIgwLossWeight("0");
                goodsWeekEntity.setGbIgwLossSubtotal("0");
            }
            if (what.equals("loss")) {
                goodsWeekEntity.setGbIgwLossWeight(myChangeWeight.toString());
                goodsWeekEntity.setGbIgwLossSubtotal(myChangeSubtotal.toString());
                goodsWeekEntity.setGbIgwReturnWeight("0");
                goodsWeekEntity.setGbIgwReturnSubtotal("0");
                goodsWeekEntity.setGbIgwProduceWeight("0");
                goodsWeekEntity.setGbIgwProduceSubtotal("0");
                goodsWeekEntity.setGbIgwWasteWeight("0");
                goodsWeekEntity.setGbIgwWasteSubtotal("0");
            }
            if (what.equals("return")) {
                goodsWeekEntity.setGbIgwReturnWeight(myChangeWeight.toString());
                goodsWeekEntity.setGbIgwReturnSubtotal(myChangeSubtotal.toString());
                goodsWeekEntity.setGbIgwLossWeight("0");
                goodsWeekEntity.setGbIgwLossSubtotal("0");
                goodsWeekEntity.setGbIgwProduceWeight("0");
                goodsWeekEntity.setGbIgwProduceSubtotal("0");
                goodsWeekEntity.setGbIgwWasteWeight("0");
                goodsWeekEntity.setGbIgwWasteSubtotal("0");
            }

            goodsWeekEntity.setGbIgwStatus(0);
            gbDepInventoryGoodsWeekService.save(goodsWeekEntity);
        }


        //3，更新inventoryWeek表
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depId", stock.getGbDgsGbDepartmentId());
        map1.put("week", getWeekOfYear(0));
        GbDepInventoryWeekEntity weekEntity = gbDepInventoryWeekService.queryInventoryWeek(map1);
        if (weekEntity != null) {
            BigDecimal bigDecimal = new BigDecimal("0");
            if (what.equals("loss")) {
                bigDecimal = new BigDecimal(weekEntity.getGbDiwLossTotal());
                BigDecimal add = bigDecimal.add(myChangeSubtotal).setScale(2, BigDecimal.ROUND_HALF_UP);
                weekEntity.setGbDiwLossTotal(add.toString());
            }
            if (what.equals("produce")) {
                bigDecimal = new BigDecimal(weekEntity.getGbDiwProduceTotal());
                BigDecimal add = bigDecimal.add(myChangeSubtotal).setScale(2, BigDecimal.ROUND_HALF_UP);
                weekEntity.setGbDiwProduceTotal(add.toString());
            }
            if (what.equals("waste")) {
                bigDecimal = new BigDecimal(weekEntity.getGbDiwWasteTotal());
                BigDecimal add = bigDecimal.add(myChangeSubtotal).setScale(2, BigDecimal.ROUND_HALF_UP);
                weekEntity.setGbDiwWasteTotal(add.toString());
            }
            if (what.equals("return")) {
                bigDecimal = new BigDecimal(weekEntity.getGbDiwReturnTotal());
                BigDecimal add = bigDecimal.add(myChangeSubtotal).setScale(2, BigDecimal.ROUND_HALF_UP);
                weekEntity.setGbDiwReturnTotal(add.toString());
            }
            gbDepInventoryWeekService.update(weekEntity);

        } else {

            GbDepInventoryWeekEntity weekEntity1 = new GbDepInventoryWeekEntity();
            weekEntity1.setGbDiwDepartmentId(stock.getGbDgsGbDepartmentId());
            weekEntity1.setGbDiwDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            weekEntity1.setGbDiwDistributerId(stock.getGbDgsGbDistributerId());
            if (what.equals("loss")) {
                weekEntity1.setGbDiwLossTotal(myChangeSubtotal.toString());
                weekEntity1.setGbDiwReturnTotal("0");
                weekEntity1.setGbDiwProduceTotal("0");
                weekEntity1.setGbDiwWasteTotal("0");
            }
            if (what.equals("produce")) {
                weekEntity1.setGbDiwProduceTotal(myChangeSubtotal.toString());
                weekEntity1.setGbDiwReturnTotal("0");
                weekEntity1.setGbDiwWasteTotal("0");
                weekEntity1.setGbDiwLossTotal("0");
            }
            if (what.equals("waste")) {
                weekEntity1.setGbDiwWasteTotal(myChangeSubtotal.toString());
                weekEntity1.setGbDiwReturnTotal("0");
                weekEntity1.setGbDiwProduceTotal("0");
                weekEntity1.setGbDiwLossTotal("0");
            }
            if (what.equals("return")) {
                weekEntity1.setGbDiwReturnTotal(myChangeSubtotal.toString());
                weekEntity1.setGbDiwLossTotal("0");
                weekEntity1.setGbDiwProduceTotal("0");
                weekEntity1.setGbDiwWasteTotal("0");
            }
            weekEntity1.setGbDiwWeek(getWeekOfYear(0).toString());
            weekEntity1.setGbDiwYear(formatWhatYear(0));
            weekEntity1.setGbDiwStatus(0);
            gbDepInventoryWeekService.save(weekEntity1);
        }

        return goodsWeekEntity;
    }

    private GbDepInventoryGoodsMonthEntity changeMonth(GbDepartmentGoodsStockEntity stock, String what) {
        GbDepInventoryGoodsMonthEntity goodsMonthEntity = new GbDepInventoryGoodsMonthEntity();

        BigDecimal myChangeWeight = new BigDecimal("0");
        BigDecimal price = new BigDecimal(stock.getGbDgsPrice()); //单价
        if (what.equals("loss")) {
            myChangeWeight = new BigDecimal(stock.getGbDgsMyLossWeight()); // 最新提交待损耗数量
        }
        if (what.equals("produce")) {
            myChangeWeight = new BigDecimal(stock.getGbDgsMyProduceWeight()); // 最新提交待损耗数量
        }
//        if (what.equals("waste")) {
//            myChangeWeight = new BigDecimal(stock.getGbDgsDoWasteWeight()); // 最新提交待损耗数量
//        }
        if (what.equals("return")) {
            myChangeWeight = new BigDecimal(stock.getGbDgsMyReturnWeight()); // 最新提交待损耗数量
        }

        BigDecimal myChangeSubtotal = myChangeWeight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP); //最新提交损耗成本


        //2，更新-inventoryGoodsMonth
        Map<String, Object> map = new HashMap<>();
        map.put("stockId", stock.getGbDepartmentGoodsStockId());
        map.put("month", formatWhatMonth(0));
        map.put("depId", stock.getGbDgsGbDepartmentId());
        List<GbDepInventoryGoodsMonthEntity> depInventoryGoodsMonthEntities = gbDepInventoryGoodsMonthService.queryMonthStockByParams(map);
        if (depInventoryGoodsMonthEntities.size() > 0) {
            //在最后一条盘库记录记录添加损耗
            int index = depInventoryGoodsMonthEntities.size() - 1;
            goodsMonthEntity = depInventoryGoodsMonthEntities.get(index);
            double allWeight = 0.0;
            if (what.equals("loss")) {
                allWeight = myChangeWeight.add(new BigDecimal(goodsMonthEntity.getGbIgmLossWeight())).doubleValue();
                goodsMonthEntity.setGbIgmLossWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                goodsMonthEntity.setGbIgmLossSubtotal(multiply.toString());
            }
            if (what.equals("produce")) {
                allWeight = myChangeWeight.add(new BigDecimal(goodsMonthEntity.getGbIgmProduceWeight())).doubleValue();
                goodsMonthEntity.setGbIgmProduceWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                goodsMonthEntity.setGbIgmProduceSubtotal(multiply.toString());
            }
            if (what.equals("waste")) {
                allWeight = myChangeWeight.add(new BigDecimal(goodsMonthEntity.getGbIgmWasteWeight())).doubleValue();
                goodsMonthEntity.setGbIgmWasteWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                goodsMonthEntity.setGbIgmWasteSubtotal(multiply.toString());
            }
            if (what.equals("return")) {
                allWeight = myChangeWeight.add(new BigDecimal(goodsMonthEntity.getGbIgmReturnWeight())).doubleValue();
                goodsMonthEntity.setGbIgmReturnWeight(String.format("%.1f", allWeight));
                BigDecimal multiply = new BigDecimal(allWeight).multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                goodsMonthEntity.setGbIgmReturnSubtotal(multiply.toString());
            }

            gbDepInventoryGoodsMonthService.update(goodsMonthEntity);
        } else {
            //添加一条新盘库记录
            goodsMonthEntity.setGbIgmDepartmentId(stock.getGbDgsGbDepartmentId());
            goodsMonthEntity.setGbIgmDisGoodsId(stock.getGbDgsGbDisGoodsId());
            goodsMonthEntity.setGbIgmDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
            goodsMonthEntity.setGbIgmDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            goodsMonthEntity.setGbIgmMonth(formatWhatMonth(0));
            goodsMonthEntity.setGbIgmYear(formatWhatYear(0));
            goodsMonthEntity.setGbIgmGbDepStockId(stock.getGbDepartmentGoodsStockId());
            goodsMonthEntity.setGbIgmDistributerId(stock.getGbDgsGbDistributerId());
            goodsMonthEntity.setGbIgmDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
            goodsMonthEntity.setGbIgmFullTime(formatFullTime());

            if (what.equals("loss")) {
                goodsMonthEntity.setGbIgmLossWeight(myChangeWeight.toString());
                goodsMonthEntity.setGbIgmLossSubtotal(myChangeSubtotal.toString());
                goodsMonthEntity.setGbIgmReturnWeight("0");
                goodsMonthEntity.setGbIgmReturnSubtotal("0");
                goodsMonthEntity.setGbIgmWasteWeight("0");
                goodsMonthEntity.setGbIgmWasteSubtotal("0");
                goodsMonthEntity.setGbIgmProduceWeight("0");
                goodsMonthEntity.setGbIgmProduceSubtotal("0");
            }
            if (what.equals("produce")) {
                goodsMonthEntity.setGbIgmProduceWeight(myChangeWeight.toString());
                goodsMonthEntity.setGbIgmProduceSubtotal(myChangeSubtotal.toString());
                goodsMonthEntity.setGbIgmReturnWeight("0");
                goodsMonthEntity.setGbIgmReturnSubtotal("0");
                goodsMonthEntity.setGbIgmWasteWeight("0");
                goodsMonthEntity.setGbIgmWasteSubtotal("0");
                goodsMonthEntity.setGbIgmLossWeight("0");
                goodsMonthEntity.setGbIgmLossSubtotal("0");
            }
            if (what.equals("waste")) {
                goodsMonthEntity.setGbIgmWasteWeight(myChangeWeight.toString());
                goodsMonthEntity.setGbIgmWasteSubtotal(myChangeSubtotal.toString());
                goodsMonthEntity.setGbIgmReturnWeight("0");
                goodsMonthEntity.setGbIgmReturnSubtotal("0");
                goodsMonthEntity.setGbIgmLossWeight("0");
                goodsMonthEntity.setGbIgmLossSubtotal("0");
                goodsMonthEntity.setGbIgmProduceWeight("0");
                goodsMonthEntity.setGbIgmProduceSubtotal("0");
            }

            if (what.equals("return")) {
                goodsMonthEntity.setGbIgmReturnWeight(myChangeWeight.toString());
                goodsMonthEntity.setGbIgmReturnSubtotal(myChangeSubtotal.toString());
                goodsMonthEntity.setGbIgmLossWeight("0");
                goodsMonthEntity.setGbIgmLossSubtotal("0");
                goodsMonthEntity.setGbIgmWasteWeight("0");
                goodsMonthEntity.setGbIgmWasteSubtotal("0");
                goodsMonthEntity.setGbIgmProduceWeight("0");
                goodsMonthEntity.setGbIgmProduceSubtotal("0");
            }
            goodsMonthEntity.setGbIgmStatus(0);
            gbDepInventoryGoodsMonthService.save(goodsMonthEntity);
        }


        //3，更新inventoryMonth表
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depId", stock.getGbDgsGbDepartmentId());
        map1.put("month", formatWhatMonth(0));
        GbDepInventoryMonthEntity monthEntity = gbDepInventoryMonthService.queryInventoryMonth(map1);
        System.out.println(monthEntity);
        if (monthEntity != null) {
            double bigDecimal = 0.0;
            if (what.equals("loss")) {
                bigDecimal = new BigDecimal(monthEntity.getGbImLossTotal()).doubleValue();
                BigDecimal add = new BigDecimal(bigDecimal).add(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                monthEntity.setGbImLossTotal(add.toString());
            }
            if (what.equals("produce")) {
                bigDecimal = new BigDecimal(monthEntity.getGbImProduceTotal()).doubleValue();
                BigDecimal add = new BigDecimal(bigDecimal).add(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                monthEntity.setGbImProduceTotal(add.toString());
            }
            if (what.equals("waste")) {
                bigDecimal = new BigDecimal(monthEntity.getGbImWasteTotal()).doubleValue();
                BigDecimal add = new BigDecimal(bigDecimal).add(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                monthEntity.setGbImWasteTotal(add.toString());
            }
            if (what.equals("return")) {
                bigDecimal = new BigDecimal(monthEntity.getGbImReturnTotal()).doubleValue();
                BigDecimal add = new BigDecimal(bigDecimal).add(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                monthEntity.setGbImReturnTotal(add.toString());
            }

            gbDepInventoryMonthService.update(monthEntity);
        } else {
            GbDepInventoryMonthEntity monthEntity1 = new GbDepInventoryMonthEntity();
            monthEntity1.setGbImDepartmentId(stock.getGbDgsGbDepartmentId());
            monthEntity1.setGbImDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            monthEntity1.setGbImDistributerId(stock.getGbDgsGbDistributerId());
            if (what.equals("loss")) {
                monthEntity1.setGbImLossTotal(myChangeSubtotal.toString());
                monthEntity1.setGbImReturnTotal("0");
                monthEntity1.setGbImProduceTotal("0");
                monthEntity1.setGbImWasteTotal("0");
            }
            if (what.equals("produce")) {
                monthEntity1.setGbImProduceTotal(myChangeSubtotal.toString());
                monthEntity1.setGbImReturnTotal("0");
                monthEntity1.setGbImLossTotal("0");
                monthEntity1.setGbImWasteTotal("0");
            }
            if (what.equals("waste")) {
                monthEntity1.setGbImWasteTotal(myChangeSubtotal.toString());
                monthEntity1.setGbImReturnTotal("0");
                monthEntity1.setGbImProduceTotal("0");
                monthEntity1.setGbImLossTotal("0");
            }
            if (what.equals("return")) {
                monthEntity1.setGbImReturnTotal(myChangeSubtotal.toString());
                monthEntity1.setGbImLossTotal("0");
                monthEntity1.setGbImProduceTotal("0");
                monthEntity1.setGbImWasteTotal("0");
            }

            monthEntity1.setGbImMonth(formatWhatMonth(0));
            monthEntity1.setGbImYear(formatWhatYear(0));
            monthEntity1.setGbImStatus(0);
            gbDepInventoryMonthService.save(monthEntity1);
        }
        return goodsMonthEntity;
    }


    /**
     * todo
     *
     * @param orders
     * @return
     */

    @RequestMapping(value = "/finishReturnOrderToDep")
    @ResponseBody
    public R finishReturnOrderToDep(@RequestBody GbDepartmentOrdersEntity orders) {

        if (orders.getGbDoStatus().equals(getGbOrderStatusReceived())) {
            Integer gbDoDgsrReturnId = orders.getGbDoDgsrReturnId();
            GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepartmentStockReduceService.queryObject(gbDoDgsrReturnId);
            reduceEntity.setGbDgsrStatus(0);
            gbDepartmentStockReduceService.update(reduceEntity);

            orders.setGbDoArriveWeeksYear(getWeekOfYear(0));
            orders.setGbDoArriveWhatDay(getWeek(0));
            orders.setGbDoArriveOnlyDate(formatWhatDate(0));
            orders.setGbDoArriveDate(formatWhatDay(0));
            gbDepOrdersService.update(orders);

            Map<String, Object> map = new HashMap<>();
            map.put("depId", orders.getGbDoDepartmentId());
            map.put("equalStatus", -1);
            map.put("toDepId", orders.getGbDoToDepartmentId());
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepOrdersService.queryDisOrdersListByParams(map);
            //没有其它退货订单，生产bill
            if (ordersEntities.size() == 0) {
                GbDepartmentBillEntity billEntity = new GbDepartmentBillEntity();
                String areaCode = "1" + orders.getGbDoDistributerId();
                billEntity.setGbDbStatus(0);
                billEntity.setGbDbDisId(orders.getGbDoDistributerId());
                billEntity.setGbDbDepId(orders.getGbDoDepartmentId());
                billEntity.setGbDbDate(formatWhatDay(0));
                billEntity.setGbDbTime(formatWhatYearDayTime(0));
                billEntity.setGbDbMonth(formatWhatMonth(0));
                billEntity.setGbDbWeek(getWeek(0));
                billEntity.setGbDbTradeNo(generateBillTradeNo(areaCode));
                billEntity.setGbDbIssueDepId(orders.getGbDoToDepartmentId());
                billEntity.setGbDbIssueOrderType(getGbOrderTypeTuihuo());
                billEntity.setGbDbIssueUserId(orders.getGbDoReturnUserId());
                billEntity.setGbDbPrintTimes(0);
                gbDepartmentBillService.save(billEntity);
                //
                Integer gbDoDepartmentId = orders.getGbDoDepartmentId();
                Map<String, Object> map1 = new HashMap<>();
                map1.put("depId", gbDoDepartmentId);
                map1.put("equalStatus", getGbOrderStatusReceived());
                map1.put("equalBuyStatus", -1);
                map1.put("toDepId", orders.getGbDoToDepartmentId());
                map1.put("billId", -1);
                List<GbDepartmentOrdersEntity> ordersEntities1 = gbDepOrdersService.queryDisOrdersListByParams(map1);
                BigDecimal returnTotal = new BigDecimal(0);
                BigDecimal returnSellTotal = new BigDecimal(0);
                if (ordersEntities1.size() > 0) {
                    for (GbDepartmentOrdersEntity ordersEntity : ordersEntities1) {
                        returnTotal = returnTotal.add(new BigDecimal(ordersEntity.getGbDoSubtotal()));
                        returnSellTotal = returnSellTotal.add(new BigDecimal(ordersEntity.getGbDoSellingSubtotal()));
                        ordersEntity.setGbDoBillId(billEntity.getGbDepartmentBillId());
                        ordersEntity.setGbDoStatus(getGbOrderStatusReceived());
                        ordersEntity.setGbDoBuyStatus(3);
                        gbDepOrdersService.update(ordersEntity);

                        Integer purchaseGoodsId = orders.getGbDoPurchaseGoodsId();
                        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
                        purchaseGoodsEntity.setGbDpgStatus(getGbOrderStatusHasFinished());
                        gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                    }
                }
                billEntity.setGbDbTotal("-" + returnTotal.toString());
                billEntity.setGbDbSellingTotal("-" + returnSellTotal.toString());
                billEntity.setGbDbOrderAmount(ordersEntities1.size());
                gbDepartmentBillService.update(billEntity);
            }

        }


        return R.ok();
    }


    @RequestMapping(value = "/saveDepLossGoodsStockArr", method = RequestMethod.POST)
    @ResponseBody
    public R saveDepLossGoodsStockArr(@RequestBody List<GbDepartmentGoodsStockEntity> stockList) {
        for (GbDepartmentGoodsStockEntity stock : stockList) {
            if (new BigDecimal(stock.getGbDgsMyLossWeight()).compareTo(BigDecimal.ZERO) == 1) {
                System.out.println(" lossWeiigghht" + stock.getGbDgsMyLossWeight());
                changeDepartmentStock(stock, "loss");
            }
        }
        return R.ok();
    }


    /**
     * 退货接口
     * todo
     *
     * @param stockArr
     * @return
     */
    @RequestMapping(value = "/saveDepReturnGoodsStockArr", method = RequestMethod.POST)
    @ResponseBody
    public R saveDepReturnGoodsStockArr(@RequestBody List<GbDepartmentGoodsStockEntity> stockArr) {
        for (GbDepartmentGoodsStockEntity stock : stockArr) {
            if (new BigDecimal(stock.getGbDgsMyReturnWeight()).compareTo(BigDecimal.ZERO) == 1) {
                changeDepartmentStock(stock, "return");
//                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = saveRetrunOrder(stock);
//                saveReturnDepGoodsStock(stock, gbDepartmentOrdersEntity.getGbDepartmentOrdersId(), gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId());
            }
        }
        return R.ok();
    }

    @RequestMapping(value = "/saveDepReturnGoodsStock", method = RequestMethod.POST)
    @ResponseBody
    public R saveDepReturnGoodsStock(@RequestBody GbDepartmentGoodsStockEntity stock) {
        GbDepartmentGoodsStockReduceEntity reduceEntity = changeDepartmentStock(stock, "return");
        GbDepartmentOrdersEntity gbDepartmentOrdersEntity = saveRetrunOrder(stock, reduceEntity.getGbDepartmentGoodsStockReduceId());

        if (stock.getGbDgsGbGoodsStockId() != -1) {
            saveReturnDepGoodsStock(stock, gbDepartmentOrdersEntity.getGbDepartmentOrdersId(), gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId());
        }
        return R.ok();
    }

    @RequestMapping(value = "/saveStockDepReturnGoodsStock", method = RequestMethod.POST)
    @ResponseBody
    public R saveStockDepReturnGoodsStock(@RequestBody GbDepartmentGoodsStockEntity stock) {
        GbDepartmentGoodsStockReduceEntity reduceEntity = changeDepartmentStock(stock, "return");

        Integer gbDgsGbDepartmentId = stock.getGbDgsGbDepartmentId();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(gbDgsGbDepartmentId);
        Integer gbDepartmentType = departmentEntity.getGbDepartmentType();
        if (gbDepartmentType.equals(getGbDisGoodsTypeChuku())) {
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = saveStockDepRetrunOrder(stock, reduceEntity.getGbDepartmentGoodsStockReduceId());
        }

        return R.ok();
    }

    private void saveReturnDepGoodsStock(GbDepartmentGoodsStockEntity stock, Integer orderId, Integer purId) {
        BigDecimal bigDecimal = new BigDecimal(stock.getGbDgsPrice());
        BigDecimal bigDecimal1 = new BigDecimal(stock.getGbDgsMyReturnWeight());
        Integer gbDgsGbGoodsStockId = stock.getGbDgsGbGoodsStockId();
        GbDepartmentGoodsStockEntity fromStock = gbDepGoodsStockService.queryObject(gbDgsGbGoodsStockId);
        if (fromStock != null) {
            BigDecimal multiply = bigDecimal.multiply(bigDecimal1).setScale(2, BigDecimal.ROUND_HALF_UP);
            GbDepartmentGoodsStockEntity stockEntity = new GbDepartmentGoodsStockEntity();
            stockEntity.setGbDgsWeight(bigDecimal1.toString());
            stockEntity.setGbDgsPrice(stock.getGbDgsPrice());
            stockEntity.setGbDgsSubtotal(multiply.toString());
            stockEntity.setGbDgsSellingPrice(fromStock.getGbDgsSellingPrice());
            stockEntity.setGbDgsRestWeight("0");
            stockEntity.setGbDgsRestSubtotal("0");
            stockEntity.setGbDgsLossWeight("0");
            stockEntity.setGbDgsLossSubtotal("0");
            stockEntity.setGbDgsReturnWeight("0");
            stockEntity.setGbDgsReturnSubtotal("0");
            stockEntity.setGbDgsProduceWeight("0");
            stockEntity.setGbDgsProduceSubtotal("0");
            stockEntity.setGbDgsWasteWeight("0");
            stockEntity.setGbDgsWasteSubtotal("0");
            stockEntity.setGbDgsDate(formatWhatDay(0));
            stockEntity.setGbDgsTimeStamp(getTimeStamp());
            stockEntity.setGbDgsWeek(getWeek(0));
            stockEntity.setGbDgsMonth(formatWhatMonth(0));
            stockEntity.setGbDgsYear(formatWhatYear(0));
            stockEntity.setGbDgsGbDepartmentId(fromStock.getGbDgsGbDepartmentId());
            stockEntity.setGbDgsGbDepartmentFatherId(fromStock.getGbDgsGbDepartmentFatherId());
            stockEntity.setGbDgsGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
            stockEntity.setGbDgsGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
            stockEntity.setGbDgsGbDistributerId(stock.getGbDgsGbDistributerId());
            stockEntity.setGbDgsGbDepDisGoodsId(fromStock.getGbDgsGbDepDisGoodsId());
            stockEntity.setGbDgsGbDepartmentOrderId(orderId);
            stockEntity.setGbDgsGbPurGoodsId(purId);
            stockEntity.setGbDgsGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
            stockEntity.setGbDgsGbFromDepartmentId(stock.getGbDgsGbDepartmentId());
            stockEntity.setGbDgsInventoryMonth(formatWhatMonth(0));
            stockEntity.setGbDgsInventoryYear(formatWhatYear(0));
            stockEntity.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
            stockEntity.setGbDgsInventoryDate(formatWhatDay(0));
            stockEntity.setGbDgsFullTime(formatFullTime());
            System.out.println("wanrnrnrhsoururru" + fromStock.getGbDepartmentGoodsStockId());
            System.out.println("wanrnrnrhsoururru" + fromStock.getGbStockWasetHours());
            stockEntity.setGbDgsWarnFullTime(fromStock.getGbDgsWarnFullTime());
            stockEntity.setGbDgsWasteFullTime(fromStock.getGbDgsWasteFullTime());
            stockEntity.setGbDgsStatus(-1);
            gbDepGoodsStockService.save(stockEntity);
        }


    }

    private GbDepartmentOrdersEntity saveStockDepRetrunOrder(GbDepartmentGoodsStockEntity stock, Integer returnId) {

        System.out.println("saveStockDepRetrunOrder" + returnId);

        Integer gbDgsGbDisGoodsId = stock.getGbDgsGbDisGoodsId();
        GbDistributerGoodsEntity goodsEntity = disGoodsService.queryObject(gbDgsGbDisGoodsId);

        GbDepartmentOrdersEntity order = new GbDepartmentOrdersEntity();
        order.setGbDoOrderUserId(stock.getGbDgsReturnUserId());
        order.setGbDoDgsrReturnId(returnId);
        order.setGbDoDepartmentId(stock.getGbDgsGbDepartmentId());
        order.setGbDoDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
        order.setGbDoDistributerId(stock.getGbDgsGbDistributerId());
        order.setGbDoToDepartmentId(goodsEntity.getGbDgGbDepartmentId());
        order.setGbDoQuantity(stock.getGbDgsMyReturnWeight());
        order.setGbDoWeight(stock.getGbDgsMyReturnWeight());
        order.setGbDoDisGoodsId(goodsEntity.getGbDistributerGoodsId());
        order.setGbDoDisGoodsFatherId(goodsEntity.getGbDgDfgGoodsFatherId());
        order.setGbDoStandard(goodsEntity.getGbDgGoodsStandardname());
        order.setGbDoPrice(stock.getGbDgsPrice());
        BigDecimal bigDecimal = new BigDecimal(stock.getGbDgsMyReturnWeight())
                .multiply(new BigDecimal(stock.getGbDgsPrice()))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        order.setGbDoSubtotal(bigDecimal.toString());
        order.setGbDoSellingPrice(stock.getGbDgsSellingPrice());
        BigDecimal multiply = new BigDecimal(stock.getGbDgsSellingPrice()).multiply(new BigDecimal(stock.getGbDgsMyReturnWeight()));
        order.setGbDoSellingSubtotal(multiply.toString());
        order.setGbDoApplyDate(formatWhatDay(0));
        order.setGbDoArriveDate(formatWhatDay(0));
        order.setGbDoArriveOnlyDate(formatWhatDate(0));
        order.setGbDoApplyFullTime(formatFullTime());
        order.setGbDoArriveWhatDay(getWeek(0));
        order.setGbDoApplyOnlyTime(formatWhatTime(0));
        order.setGbDoArriveWeeksYear(getWeekOfYear(0));
        order.setGbDoDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
        order.setGbDoGoodsType(goodsEntity.getGbDgGoodsType());
        order.setGbDoNxDistributerId(goodsEntity.getGbDgNxDistributerId());
        order.setGbDoNxDistributerGoodsId(goodsEntity.getGbDgNxDistributerGoodsId());


        //是个新采购商品
        GbDistributerPurchaseGoodsEntity disGoods = new GbDistributerPurchaseGoodsEntity();
        disGoods.setGbDpgDisGoodsFatherId(goodsEntity.getGbDgDfgGoodsFatherId());
        disGoods.setGbDpgDisGoodsId(goodsEntity.getGbDistributerGoodsId());
        disGoods.setGbDpgDistributerId(goodsEntity.getGbDgDistributerId());
        disGoods.setGbDpgApplyDate(formatWhatDay(0));
        disGoods.setGbDpgStatus(-1);
        disGoods.setGbDpgOrdersAmount(1);
        disGoods.setGbDpgOrdersFinishAmount(0);
        disGoods.setGbDpgStandard(goodsEntity.getGbDgGoodsStandardname());
        disGoods.setGbDpgPurchaseWeek(getWeek(0));
        disGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
        disGoods.setGbDpgBuyPrice(order.getGbDoPrice());
        disGoods.setGbDpgBuyQuantity(order.getGbDoQuantity());
        disGoods.setGbDpgBuySubtotal("-" + order.getGbDoSubtotal());
        disGoods.setGbDpgPurchaseDepartmentId(order.getGbDoToDepartmentId());
        disGoods.setGbDpgPurchaseNxDistributerId(order.getGbDoNxDistributerId());
        disGoods.setGbDpgPurchaseType(getGbOrderTypeTuihuo());
        gbDistributerPurchaseGoodsService.save(disGoods);
        Integer gbDistributerPurchaseGoodsId = disGoods.getGbDistributerPurchaseGoodsId();
        order.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
        order.setGbDoStatus(-1);
        order.setGbDoBuyStatus(-1);
        order.setGbDoOrderType(getGbOrderTypeTuihuo());

        gbDepOrdersService.justSave(order);
        return order;
    }

    private GbDepartmentOrdersEntity saveRetrunOrder(GbDepartmentGoodsStockEntity stock, Integer returnId) {

        System.out.println("reididi" + returnId);

        Integer gbDgsGbDisGoodsId = stock.getGbDgsGbDisGoodsId();
        GbDistributerGoodsEntity goodsEntity = disGoodsService.queryObject(gbDgsGbDisGoodsId);

        GbDepartmentOrdersEntity order = new GbDepartmentOrdersEntity();
        order.setGbDoOrderUserId(stock.getGbDgsReturnUserId());
        order.setGbDoDgsrReturnId(returnId);
        order.setGbDoDepartmentId(stock.getGbDgsGbDepartmentId());
        order.setGbDoDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
        order.setGbDoDistributerId(stock.getGbDgsGbDistributerId());
        order.setGbDoToDepartmentId(goodsEntity.getGbDgGbDepartmentId());
        order.setGbDoQuantity(stock.getGbDgsMyReturnWeight());
        order.setGbDoWeight(stock.getGbDgsMyReturnWeight());
        order.setGbDoDisGoodsId(goodsEntity.getGbDistributerGoodsId());
        order.setGbDoDisGoodsFatherId(goodsEntity.getGbDgDfgGoodsFatherId());
        order.setGbDoStandard(goodsEntity.getGbDgGoodsStandardname());
        order.setGbDoPrice(stock.getGbDgsPrice());
        BigDecimal bigDecimal = new BigDecimal(stock.getGbDgsMyReturnWeight())
                .multiply(new BigDecimal(stock.getGbDgsPrice()))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        order.setGbDoSubtotal(bigDecimal.toString());
        order.setGbDoSellingPrice(stock.getGbDgsSellingPrice());
        BigDecimal multiply = new BigDecimal(stock.getGbDgsSellingPrice()).multiply(new BigDecimal(stock.getGbDgsMyReturnWeight()));
        order.setGbDoSellingSubtotal(multiply.toString());
        order.setGbDoApplyDate(formatWhatDay(0));
        order.setGbDoArriveDate(formatWhatDay(0));
        order.setGbDoArriveOnlyDate(formatWhatDate(0));
        order.setGbDoApplyFullTime(formatFullTime());
        order.setGbDoArriveWhatDay(getWeek(0));
        order.setGbDoApplyOnlyTime(formatWhatTime(0));
        order.setGbDoArriveWeeksYear(getWeekOfYear(0));
        order.setGbDoDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
        order.setGbDoGoodsType(goodsEntity.getGbDgGoodsType());

        order.setGbDoNxDistributerId(goodsEntity.getGbDgNxDistributerId());
        order.setGbDoNxDistributerGoodsId(goodsEntity.getGbDgNxDistributerGoodsId());

        if (goodsEntity.getGbDgGoodsType().equals(getGbDisGoodsTypeJicai()) || goodsEntity.getGbDgGoodsType().equals(getGbDisGoodsTypeZicai())
                || goodsEntity.getGbDgGoodsType().equals(getGbDisGoodsTypeAppSupplier())) {
            //是个新采购商品
            GbDistributerPurchaseGoodsEntity disGoods = new GbDistributerPurchaseGoodsEntity();
            disGoods.setGbDpgDisGoodsFatherId(goodsEntity.getGbDgDfgGoodsFatherId());
            disGoods.setGbDpgDisGoodsId(goodsEntity.getGbDistributerGoodsId());
            disGoods.setGbDpgDistributerId(goodsEntity.getGbDgDistributerId());
            disGoods.setGbDpgApplyDate(formatWhatDay(0));
            disGoods.setGbDpgStatus(-1);
            disGoods.setGbDpgOrdersAmount(1);
            disGoods.setGbDpgOrdersFinishAmount(0);
            disGoods.setGbDpgStandard(goodsEntity.getGbDgGoodsStandardname());
            disGoods.setGbDpgPurchaseWeek(getWeek(0));
            disGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            disGoods.setGbDpgBuyPrice(order.getGbDoPrice());
            disGoods.setGbDpgBuyQuantity(order.getGbDoQuantity());
            disGoods.setGbDpgBuySubtotal("-" + order.getGbDoSubtotal());
            disGoods.setGbDpgPurchaseDepartmentId(order.getGbDoToDepartmentId());
            disGoods.setGbDpgPurchaseNxDistributerId(order.getGbDoNxDistributerId());
            disGoods.setGbDpgPurchaseType(getGbOrderTypeTuihuo());
            gbDistributerPurchaseGoodsService.save(disGoods);
            Integer gbDistributerPurchaseGoodsId = disGoods.getGbDistributerPurchaseGoodsId();
            order.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
        }


        if (goodsEntity.getGbDgGoodsType().equals(getGbDisGoodsTypeAppSupplier())) {
            NxDepartmentOrdersEntity ordersEntity = new NxDepartmentOrdersEntity();
            ordersEntity.setNxDoDistributerId(goodsEntity.getGbDgNxDistributerId());
            ordersEntity.setNxDoDisGoodsId(goodsEntity.getGbDgNxDistributerGoodsId());
            ordersEntity.setNxDoQuantity(stock.getGbDgsMyReturnWeight());
            ordersEntity.setNxDoPrice(stock.getGbDgsPrice());
            ordersEntity.setNxDoSubtotal(bigDecimal.toString());
            ordersEntity.setNxDoReturnSubtotal(bigDecimal.toString());
            ordersEntity.setNxDoPurchaseStatus(-1);
            ordersEntity.setNxDoStatus(-1);
            ordersEntity.setNxDoWeight(stock.getGbDgsMyReturnWeight());
            ordersEntity.setNxDoStandard(goodsEntity.getGbDgGoodsStandardname());
            ordersEntity.setNxDoApplyDate(formatWhatDay(0));
            ordersEntity.setNxDoArriveOnlyDate(formatWhatDate(0));
            ordersEntity.setNxDoArriveWeeksYear(getWeekOfYear(0));
            ordersEntity.setNxDoApplyFullTime(formatFullTime());
            ordersEntity.setNxDoApplyOnlyTime(formatWhatTime(0));
            ordersEntity.setNxDoArriveDate(formatWhatDay(0));
            ordersEntity.setNxDoGbDistributerId(stock.getGbDgsGbDistributerId());
            ordersEntity.setNxDoGbDepartmentId(stock.getGbDgsGbDepartmentId());
            ordersEntity.setNxDoGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            ordersEntity.setNxDoDepartmentId(-1);
            ordersEntity.setNxDoDepartmentFatherId(-1);
            ordersEntity.setNxDoNxCommunityId(-1);
            ordersEntity.setNxDoNxCommRestrauntFatherId(-1);
            ordersEntity.setNxDoNxCommRestrauntId(-1);
            ordersEntity.setNxDoDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
            ordersEntity.setNxDoDepDisGoodsId(-1);
            ordersEntity.setNxDoArriveWhatDay(getWeek(0));

            nxDepartmentOrdersService.saveForGb(ordersEntity);

            Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
            order.setGbDoNxDepartmentOrderId(nxDepartmentOrdersId);
        }

        order.setGbDoStatus(-1);

        if (goodsEntity.getGbDgGoodsType().equals(getGbDisGoodsTypeChuku()) || goodsEntity.getGbDgGoodsType().equals(getGbDisGoodsTypeKitchen())) {
            order.setGbDoBuyStatus(3);
        } else {
            order.setGbDoBuyStatus(-1);
        }
        order.setGbDoOrderType(getGbOrderTypeTuihuo());
        gbDepOrdersService.justSave(order);

        Integer gbDoNxDepartmentOrderId = order.getGbDoNxDepartmentOrderId();
        if (gbDoNxDepartmentOrderId != null) {
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
            ordersEntity.setNxDoGbDepartmentOrderId(order.getGbDepartmentOrdersId());
            nxDepartmentOrdersService.update(ordersEntity);
        }


        return order;
    }

    /**
     * 修改选中部门查询废弃商品，查询全部部门
     *
     * @param startDate
     * @param stopDate
     * @param disId
     * @return
     */
//    @RequestMapping(value = "/getDistributerGoodsFreshMangement", method = RequestMethod.POST)
//    @ResponseBody
//    public R getDistributerGoodsFreshMangement(String startDate, String stopDate, Integer disId, String type) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("status", 2);
//        map.put("startDate", startDate);
//        map.put("stopDate", stopDate);
//        map.put("disId", disId);
//        if (type.equals("waste")) {
//            map.put("waste", 1);
//        }
//        if (type.equals("loss")) {
//            map.put("loss", 1);
//        }
//        System.out.println("kdafksaflsa" + map);
//        TreeSet<GbDistributerGoodsEntity> aaa = gbDepGoodsStockRecordService.queryGoodsStockRecordTreeByParams(map);
//
//        for (GbDistributerGoodsEntity goodsEntity : aaa) {
//            //1 求总wasteTotal
//            Map<String, Object> map1 = new HashMap<>();
//            map1.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//            map1.put("status", 2);
//            if (type.equals("waste")) {
//                map1.put("waste", 1);
//                Integer integer = gbDepGoodsStockRecordService.queryGoodsStockRecordCount(map1);
//                if (integer > 0) {
//                    Double aDouble = gbDepGoodsStockRecordService.queryGoodsStockRecordWasteSubtotal(map1);
//                    goodsEntity.setGoodsWasteTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
//                    goodsEntity.setGoodsWasteTotalString(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                }
//            }
//
//            if (type.equals("loss")) {
//                map1.put("loss", 1);
//                Integer integer = gbDepartmentStockReduceService.queryGoodsStockRecordCount(map1);
//                if (integer > 0) {
//                    Double aDouble = gbDepartmentStockReduceService.queryGoodsStockRecordLossSubtotal(map1);
//                    goodsEntity.setGoodsLossTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
//                    goodsEntity.setGoodsLossTotalString(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                }
//            }
//
//
//            //2, 求和
//            Map<String, Object> mapDis = new HashMap<>();
//            mapDis.put("disId", disId);
//            mapDis.put("type", getGbDepartmentTypeMendian());
//            List<GbDepartmentEntity> departmentEntities1 = gbDepartmentService.queryDepByDepType(mapDis);
//
//            for (GbDepartmentEntity departmentEntity : departmentEntities1) {
//                Map<String, Object> map3 = new HashMap<>();
//                map3.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//                map3.put("status", 2);
//                map3.put("depFatherId", departmentEntity.getGbDepartmentId());
//                if (type.equals("waste")) {
//                    map3.put("waste", 1);
//                }
//                if (type.equals("loss")) {
//                    map3.put("loss", 1);
//                }
//                System.out.println("map3" + map3);
//                List<GbDepartmentGoodsStockRecordEntity> stockrecordEntities = gbDepGoodsStockRecordService.queryGoodsStockListByParams(map3);
//                if (stockrecordEntities.size() > 0) {
//                    if (type.equals("waste")) {
//                        Double aDouble2 = gbDepGoodsStockRecordService.queryGoodsStockRecordWasteSubtotal(map3);
//                        departmentEntity.setDepWasteGoodsTotal(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
//                        departmentEntity.setDepWasteGoodsTotalString(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    }
//
//                    if (type.equals("loss")) {
//                        Double aDouble2 = gbDepGoodsStockRecordService.queryGoodsStockRecordLossSubtotal(map3);
//                        System.out.println("losresultltlltlaDouble2" + aDouble2);
//                        departmentEntity.setDepLossGoodsTotal(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
//                        departmentEntity.setDepLossGoodsTotalString(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    }
//
//                }
//            }
//            goodsEntity.setWasteDepartmentEntities(departmentEntities1);
//        }
//        if(type.equals("waste")){
//            TreeSet<GbDistributerGoodsEntity> abc = abcWaste(aaa);
//            System.out.println("abcccwwww" + abc.size());
//            return R.ok().put("data", abc);
//        } if(type.equals("loss")){
//            TreeSet<GbDistributerGoodsEntity> abc = abcLoss(aaa);
//            System.out.println("abclossss" + abc.size());
//
//            return R.ok().put("data", abc);
//        }
//        return R.error(-1, "获取失败");
//    }


    /**
     * 获取部门废弃商品
     * todo
     *
     * @param depId
     * @param depFatherId
     * @return
     */
    @RequestMapping(value = "/getDepartmentStockFresh")
    @ResponseBody
    public R getDepartmentStockFresh(Integer depId, Integer depFatherId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String s = formatWhatFullTime(0);
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("stockDepId", depId);
        map.put("restWeight", 0);
        map.put("controlFresh", 1);
        map.put("wasteWeight", 1);
        map.put("wasteTime", s);
        map.put("dayuStatus", -1);
        List<GbDepartmentGoodsStockEntity> wasteDepGoodsStockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("depFatherId", depFatherId);
        map1.put("stockDepId", depId);
        map1.put("restWeight", 0);
        map1.put("controlFresh", 1);
        map1.put("wasteWeight", 1);
        map1.put("warnTime", s);
        map1.put("dayuWasteTime", s);
        map1.put("dayuStatus", -1);
        List<GbDepartmentGoodsStockEntity> warnDepGoodsStockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map1);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("warnArr", warnDepGoodsStockEntities);
        map3.put("wasteArr", wasteDepGoodsStockEntities);


        return R.ok().put("data", map3);

    }


    /**
     * 获取门店和库房库存和成本统计表接口
     *
     * @param depFatherId 门店id
     * @return 表数据
     */
    @RequestMapping(value = "/getMendianCostAccount/{depFatherId}")
    @ResponseBody
    public R getMendianCostAccount(@PathVariable Integer depFatherId) {
        String formatDepAllSubtotal = "0.0";
        String formatDepAllRest = "0.0";
        Map<String, Object> map1 = new HashMap<>();

        //部门总库存
        Map<String, Object> map14 = new HashMap<>();
        map14.put("depFatherId", depFatherId);
        map14.put("status", 2);
        map14.put("dayuStatus", -1);
        Integer stockAmount = gbDepGoodsStockService.queryGoodsStockCount(map14);
        if (stockAmount > 0) {
            //1，库存金额
            Map<String, Object> map42 = new HashMap<>();
            map42.put("depFatherId", depFatherId);
            map42.put("status", 2);
            map42.put("dayuStatus", -1);
            Double allKucun = gbDepGoodsStockService.queryDepGoodsSubtotal(map42);
            formatDepAllSubtotal = String.format("%.2f", allKucun);

            //2，剩余金额
            Map<String, Object> map5 = new HashMap<>();
            map5.put("depFatherId", depFatherId);
            map5.put("status", 2);
            map5.put("dayuStatus", -1);
            Double allRest = gbDepGoodsStockService.queryDepGoodsRestTotal(map5);
            formatDepAllRest = String.format("%.2f", allRest);
        }


        //4 制作成本
        String formatAllProduct = String.format("%.2f", getAllProcuctCost(depFatherId));

        //5 loss成本
        String formatDepAllLoss = String.format("%.2f", getAllLossCost(depFatherId));

        //5 return成本
        String formatDepAllReturn = String.format("%.2f", getAllReturnCost(depFatherId));

        // 6，废弃成本
        String formatDepAllWaste = String.format("%.2f", getAllWasteCost(depFatherId));


        Map<String, Object> map0 = new HashMap<>();
        //门店功能-待部门总库存
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depFatherId);
        if (departmentEntity.getGbDepartmentType().equals(getGbDepartmentTypeMendian())) {
            String formatDepWaitAllSubtotal = "0.0"; //门店---库房出库未收货
            Map<String, Object> map144 = new HashMap<>();
            map144.put("depFatherId", depFatherId);
            map144.put("status", 0);
            Integer stockAmount4 = gbDepGoodsStockService.queryGoodsStockCount(map144);
            if (stockAmount4 > 0) {
                //3，待剩余金额
                Map<String, Object> map5 = new HashMap<>();
                map5.put("depFatherId", depFatherId);
                map5.put("status", 0);
                Double waitAllRest = gbDepGoodsStockService.queryDepGoodsSubtotal(map5);
                formatDepWaitAllSubtotal = String.format("%.2f", waitAllRest);
            }
            map0.put("waitSubtotal", formatDepWaitAllSubtotal);
        }

        if (departmentEntity.getGbDepartmentType().equals(getGbDepartmentTypeKufang())) {
            //new Stock out金额-
            Double outTotal = 0.0;
            String formatwaitOutSubtotal = "0.0";
            String formatOutSubtotal = "0.0";

            Map<String, Object> map145 = new HashMap<>();
            map145.put("fromDepId", depFatherId);
            map145.put("status", 1);
            map145.put("dayuStatus", -1);
            Integer stockAmount55 = gbDepGoodsStockService.queryGoodsStockCount(map145);
            if (stockAmount55 > 0) {
                //出库金额
                Map<String, Object> map42 = new HashMap<>();
                map42.put("fromDepId", depFatherId);
                map42.put("status", 1);
                map42.put("dayuStatus", -1);
                Double outNewTotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map42);
                outTotal = outTotal + outNewTotal;
            }

            //old Stock out金额 goods_record table status == 0
            Map<String, Object> map1451 = new HashMap<>();
            map1451.put("fromDepId", depFatherId);
            map1451.put("status", 1);
            Integer stockPriceAmount55 = gbDepGoodsStockRecordService.queryGoodsStockRecordCount(map1451);
            if (stockPriceAmount55 > 0) {
                //出库金额
                Map<String, Object> map42 = new HashMap<>();
                map42.put("fromDepId", depFatherId);
                map42.put("status", 1);
                Double outOldTotal = gbDepGoodsStockRecordService.queryGoodsStockRecordSubtotal(map42);
                outTotal = outOldTotal + outTotal;
            }

            formatOutSubtotal = String.format("%.2f", outTotal);

            //wait out
            Map<String, Object> map1446 = new HashMap<>();
            map1446.put("fromDepId", depFatherId);
            map1446.put("status", 0);
            Integer stockAmount16 = gbDepGoodsStockService.queryGoodsStockCount(map1446);
            if (stockAmount16 > 0) {
                //正在出库，门店未收货的金额
                Map<String, Object> map4 = new HashMap<>();
                map4.put("fromDepId", depFatherId);
                map4.put("status", 0);
                Double workingTotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map4);
                formatwaitOutSubtotal = String.format("%.2f", workingTotal);
            }
            map0.put("out", formatOutSubtotal);
            map0.put("waitOut", formatwaitOutSubtotal);
        }


        map0.put("subtotal", formatDepAllSubtotal);
        map0.put("rest", formatDepAllRest);
        map0.put("product", formatAllProduct);
        map0.put("loss", formatDepAllLoss);
        map0.put("waste", formatDepAllWaste);
        map0.put("returnData", formatDepAllReturn);


        //盘库商品分类库存
        Map<String, Object> stringObjectMap = costInventoryType(depFatherId, 3);
        Map<String, Object> stringObjectMap2 = costInventoryType(depFatherId, 2);
        Map<String, Object> stringObjectMap3 = costInventoryType(depFatherId, 1);

        map1.put("dep", map0);
        map1.put("ri", stringObjectMap);
        map1.put("zhou", stringObjectMap2);
        map1.put("yue", stringObjectMap3);

        return R.ok().put("data", map1);
    }

    private Double getAllProcuctCost(Integer depFatherId) {
        Double allProduct = 0.0;
        //日制作成本
        Map<String, Object> map24 = new HashMap<>();
        map24.put("depFatherId", depFatherId);
        map24.put("status", 1);
        map24.put("dayuTotal", 0);
        Integer inventoryDailyAmount = gbDepInventoryGoodsDailyService.queryDailyGoodsInventoryCount(map24);
        if (inventoryDailyAmount > 0) {
            //productTotal
            Map<String, Object> map6 = new HashMap<>();
            map6.put("depFatherId", depFatherId);
            map6.put("status", 1);
            allProduct = gbDepInventoryGoodsDailyService.queryDailyGoodsTotal(map6);
        }

        //周制作成本
        Map<String, Object> map25 = new HashMap<>();
        map25.put("depFatherId", depFatherId);
        map25.put("status", 1);
        map25.put("dayuTotal", 0);
        Integer inventoryWeekAmount = gbDepInventoryGoodsWeekService.queryWeekGoodsInventoryCount(map25);
        if (inventoryWeekAmount > 0) {
            //productTotal
            Map<String, Object> map6 = new HashMap<>();
            map6.put("depFatherId", depFatherId);
            map6.put("status", 1);
            Double aDouble2 = gbDepInventoryGoodsWeekService.queryWeekGoodsTotal(map6);
            allProduct = allProduct + aDouble2;
        }

        //月制作成本
        Map<String, Object> map26 = new HashMap<>();
        map26.put("depFatherId", depFatherId);
        map26.put("status", 1);
        map26.put("dayuTotal", 0);
        Integer inventoryMonthAmount = gbDepInventoryGoodsMonthService.queryMonthGoodsInventoryCount(map26);
        if (inventoryMonthAmount > 0) {
            //productTotal
            Map<String, Object> map6 = new HashMap<>();
            map6.put("depFatherId", depFatherId);
            map6.put("status", 1);
            Double aDouble1 = gbDepInventoryGoodsMonthService.queryMonthGoodsTotal(map6);
            allProduct = allProduct + aDouble1;


        }
        return allProduct;
    }

    private Double getAllReturnCost(Integer depFatherId) {

        Double allReturn = 0.0;
        //日损耗成本
        Map<String, Object> map241 = new HashMap<>();
        map241.put("depFatherId", depFatherId);
        map241.put("status", 1);
        map241.put("dayuReturn", 0);
        Integer inventoryDailyAmount1 = gbDepInventoryGoodsDailyService.queryDailyGoodsInventoryCount(map241);
        if (inventoryDailyAmount1 > 0) {
            //productTotal
            Map<String, Object> map6 = new HashMap<>();
            map6.put("depFatherId", depFatherId);
            map6.put("status", 1);
            allReturn = gbDepInventoryGoodsDailyService.queryDailyGoodsReturnTotal(map6);
        }

        //周制作成本
        Map<String, Object> map251 = new HashMap<>();
        map251.put("depFatherId", depFatherId);
        map251.put("status", 1);
        map251.put("dayuReturn", 0);
        Integer inventoryWeekAmount1 = gbDepInventoryGoodsWeekService.queryWeekGoodsInventoryCount(map251);
        if (inventoryWeekAmount1 > 0) {
            //productTotal
            Map<String, Object> map6 = new HashMap<>();
            map6.put("depFatherId", depFatherId);
            map6.put("status", 1);
            Double aDouble2 = gbDepInventoryGoodsWeekService.queryWeekGoodsReturnTotal(map6);
            allReturn = allReturn + aDouble2;
        }

        //月制作成本
        Map<String, Object> map261 = new HashMap<>();
        map261.put("depFatherId", depFatherId);
        map261.put("status", 1);
        map261.put("dayuReturn", 0);
        Integer inventoryMonthAmount1 = gbDepInventoryGoodsMonthService.queryMonthGoodsInventoryCount(map261);
        if (inventoryMonthAmount1 > 0) {
            //productTotal
            Map<String, Object> map6 = new HashMap<>();
            map6.put("depFatherId", depFatherId);
            map6.put("status", 1);
            Double aDouble1 = gbDepInventoryGoodsMonthService.queryMonthGoodsReturnTotal(map6);
            allReturn = allReturn + aDouble1;

        }
        return allReturn;
    }

    private Double getAllLossCost(Integer depFatherId) {

        Double allLoss = 0.0;
        //日损耗成本
        Map<String, Object> map241 = new HashMap<>();
        map241.put("depFatherId", depFatherId);
        map241.put("status", 1);
        map241.put("dayuLoss", 0);
        Integer inventoryDailyAmount1 = gbDepInventoryGoodsDailyService.queryDailyGoodsInventoryCount(map241);
        if (inventoryDailyAmount1 > 0) {
            //productTotal
            Map<String, Object> map6 = new HashMap<>();
            map6.put("depFatherId", depFatherId);
            map6.put("status", 1);
            allLoss = gbDepInventoryGoodsDailyService.queryDailyGoodsLossTotal(map6);
        }

        //周制作成本
        Map<String, Object> map251 = new HashMap<>();
        map251.put("depFatherId", depFatherId);
        map251.put("status", 1);
        map251.put("dayuLoss", 0);
        Integer inventoryWeekAmount1 = gbDepInventoryGoodsWeekService.queryWeekGoodsInventoryCount(map251);
        if (inventoryWeekAmount1 > 0) {
            //productTotal
            Map<String, Object> map6 = new HashMap<>();
            map6.put("depFatherId", depFatherId);
            map6.put("status", 1);
            Double aDouble2 = gbDepInventoryGoodsWeekService.queryWeekGoodsLossTotal(map6);
            allLoss = allLoss + aDouble2;
        }

        //月制作成本
        Map<String, Object> map261 = new HashMap<>();
        map261.put("depFatherId", depFatherId);
        map261.put("status", 1);
        map261.put("dayuLoss", 0);
        Integer inventoryMonthAmount1 = gbDepInventoryGoodsMonthService.queryMonthGoodsInventoryCount(map261);
        if (inventoryMonthAmount1 > 0) {
            //productTotal
            Map<String, Object> map6 = new HashMap<>();
            map6.put("depFatherId", depFatherId);
            map6.put("status", 1);
            Double aDouble1 = gbDepInventoryGoodsMonthService.queryMonthGoodsLossTotal(map6);
            allLoss = allLoss + aDouble1;

        }
        return allLoss;
    }

    private Double getAllWasteCost(Integer depFatherId) {
        Double allWaste = 0.0;
        //日损耗成本
        Map<String, Object> map242 = new HashMap<>();
        map242.put("depFatherId", depFatherId);
        map242.put("status", 1);
        map242.put("dayuWaste", 0);
        Integer inventoryDailyAmount2 = gbDepInventoryGoodsDailyService.queryDailyGoodsInventoryCount(map242);
        if (inventoryDailyAmount2 > 0) {
            //productTotal
            Map<String, Object> map6 = new HashMap<>();
            map6.put("depFatherId", depFatherId);
            map6.put("status", 1);
            allWaste = gbDepInventoryGoodsDailyService.queryDailyGoodsWasteTotal(map6);
        }

        //周制作成本
        Map<String, Object> map252 = new HashMap<>();
        map252.put("depFatherId", depFatherId);
        map252.put("status", 1);
        map252.put("dayuWaste", 0);
        Integer inventoryWeekAmount2 = gbDepInventoryGoodsWeekService.queryWeekGoodsInventoryCount(map252);
        if (inventoryWeekAmount2 > 0) {
            //productTotal
            Map<String, Object> map6 = new HashMap<>();
            map6.put("depFatherId", depFatherId);
            map6.put("status", 1);
            Double aDouble2 = gbDepInventoryGoodsWeekService.queryWeekGoodsWasteTotal(map6);
            allWaste = allWaste + aDouble2;
        }

        //月制作成本
        Map<String, Object> map262 = new HashMap<>();
        map262.put("depFatherId", depFatherId);
        map262.put("status", 1);
        map262.put("dayuWaste", 0);
        Integer inventoryMonthAmount2 = gbDepInventoryGoodsMonthService.queryMonthGoodsInventoryCount(map262);
        if (inventoryMonthAmount2 > 0) {
            //productTotal
            Map<String, Object> map6 = new HashMap<>();
            map6.put("depFatherId", depFatherId);
            map6.put("status", 1);
            Double aDouble1 = gbDepInventoryGoodsMonthService.queryMonthGoodsWasteTotal(map6);
            allWaste = allWaste + aDouble1;

        }
        return allWaste;
    }

    private Map<String, Object> costInventoryType(Integer depFatherId, Integer inventoryType) {
        Map<String, Object> map3 = new HashMap<>();

        String formatRestTotal = "0.0";
        String formatWaitSubtotal = "0.0";
        String formatSubtotal = "0.0";

        //已收货
        Map<String, Object> map14 = new HashMap<>();
        map14.put("depFatherId", depFatherId);
        map14.put("status", 2);
        map14.put("dayuStatus", -1);
        map14.put("inventoryType", inventoryType);
        Integer stockAmount = gbDepGoodsStockService.queryGoodsStockCount(map14);
        if (stockAmount > 0) {

            //subtotal金额
            Map<String, Object> map45 = new HashMap<>();
            map45.put("depFatherId", depFatherId);
            map45.put("status", 2);
            map45.put("dayuStatus", -1);
            map45.put("inventoryType", inventoryType);
            Double subtotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map45);
            formatSubtotal = String.format("%.2f", subtotal);

            //剩余金额
            Map<String, Object> map5 = new HashMap<>();
            map5.put("depFatherId", depFatherId);
            map5.put("status", 2);
            map5.put("dayuStatus", -1);
            map5.put("inventoryType", inventoryType);
            Double restTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(map5);
            ;
            formatRestTotal = String.format("%.2f", restTotal);
        }


        //4 制作成本
        String inventoryTypeTotal = getInventoryTypeTotal(depFatherId, inventoryType);

        // 5 loss 成本
        String inventoryTypeLossTotal = getInventoryTypeLossTotal(depFatherId, inventoryType);
        // 6 loss 成本
        String inventoryTypeReturnTotal = getInventoryTypeReturnTotal(depFatherId, inventoryType);

        //7 waste
        String inventoryTypeWasteTotal = getInventoryTypeWasteTotal(depFatherId, inventoryType);


        //门店功能-待部门总库存
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depFatherId);
        if (departmentEntity.getGbDepartmentType().equals(getGbDepartmentTypeMendian())) {
            //待收货
            Map<String, Object> map144 = new HashMap<>();
            map144.put("depFatherId", depFatherId);
            map144.put("status", 0);
            map144.put("inventoryType", inventoryType);
            Integer stockAmount4 = gbDepGoodsStockService.queryGoodsStockCount(map144);
            if (stockAmount4 > 0) {
                //kucun金额
                Map<String, Object> map5 = new HashMap<>();
                map5.put("depFatherId", depFatherId);
                map5.put("status", 0);
                map5.put("inventoryType", inventoryType);
                Double waitRestTotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map5);
                ;
                formatWaitSubtotal = String.format("%.2f", waitRestTotal);
            }
            map3.put("waitSubtotal", formatWaitSubtotal);
        }
        if (departmentEntity.getGbDepartmentType().equals(getGbDepartmentTypeKufang())) {
            //new Stock out金额-
            Double outTotal = 0.0;
            String formatOutSubtotal = "0.0";
            String formatwaitOutSubtotal = "0.0";


            Map<String, Object> map145 = new HashMap<>();
            map145.put("fromDepId", depFatherId);
            map145.put("status", 1);
            map145.put("dayuStatus", -1);
            map145.put("inventoryType", inventoryType);
            Integer stockAmount55 = gbDepGoodsStockService.queryGoodsStockCount(map145);
            if (stockAmount55 > 0) {
                //出库金额
                Map<String, Object> map42 = new HashMap<>();
                map42.put("fromDepId", depFatherId);
                map42.put("status", 1);
                map42.put("dayuStatus", -1);
                map42.put("inventoryType", inventoryType);
                Double outNewTotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map42);
                outTotal = outTotal + outNewTotal;
            }

            //old Stock out金额 goods_record table status == 0
            Map<String, Object> map1451 = new HashMap<>();
            map1451.put("fromDepId", depFatherId);
            map1451.put("status", 1);
            map1451.put("inventoryType", inventoryType);
            Integer stockPriceAmount55 = gbDepGoodsStockRecordService.queryGoodsStockRecordCount(map1451);
            if (stockPriceAmount55 > 0) {
                //出库金额
                Map<String, Object> map42 = new HashMap<>();
                map42.put("fromDepId", depFatherId);
                map42.put("status", 1);
                map42.put("inventoryType", inventoryType);
                Double outOldTotal = gbDepGoodsStockRecordService.queryGoodsStockRecordSubtotal(map42);
                outTotal = outOldTotal + outTotal;
            }

            formatOutSubtotal = String.format("%.2f", outTotal);

            //wait out
            Map<String, Object> map1446 = new HashMap<>();
            map1446.put("fromDepId", depFatherId);
            map1446.put("status", 0);
            map1446.put("inventoryType", inventoryType);
            Integer stockAmount16 = gbDepGoodsStockService.queryGoodsStockCount(map1446);
            if (stockAmount16 > 0) {
                //正在出库，门店未收货的金额
                Map<String, Object> map4 = new HashMap<>();
                map4.put("fromDepId", depFatherId);
                map4.put("status", 0);
                map4.put("inventoryType", inventoryType);
                Double workingTotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map4);
                formatwaitOutSubtotal = String.format("%.2f", workingTotal);
            }
            map3.put("out", formatOutSubtotal);
            map3.put("waitOut", formatwaitOutSubtotal);
        }


        map3.put("subtotal", formatSubtotal);
        map3.put("product", inventoryTypeTotal);
        map3.put("waste", inventoryTypeWasteTotal);
        map3.put("rest", formatRestTotal);
        map3.put("loss", inventoryTypeLossTotal);
        map3.put("returnData", inventoryTypeReturnTotal);
        return map3;

    }

    private String getInventoryTypeTotal(Integer depFatherId, Integer inventoryType) {
        String formatProductTotal = "0.0";
        if (inventoryType == 3) {
            Map<String, Object> map24 = new HashMap<>();
            map24.put("depFatherId", depFatherId);
            map24.put("status", 1);
            map24.put("dayuTotal", 0);
            map24.put("inventoryType", inventoryType);
            Integer inventoryAmount = gbDepInventoryGoodsDailyService.queryDailyGoodsInventoryCount(map24);
            if (inventoryAmount > 0) {
                //productTotal
                Map<String, Object> map6 = new HashMap<>();
                map6.put("depFatherId", depFatherId);
                map6.put("status", 1);
                map6.put("inventoryType", inventoryType);
                Double productTotal = gbDepInventoryGoodsDailyService.queryDailyGoodsTotal(map6);
                formatProductTotal = String.format("%.2f", productTotal);

            }
        }
        if (inventoryType == 2) {
            Map<String, Object> map24 = new HashMap<>();
            map24.put("depFatherId", depFatherId);
            map24.put("status", 1);
            map24.put("dayuTotal", 0);
            map24.put("inventoryType", inventoryType);
            Integer inventoryAmount = gbDepInventoryGoodsWeekService.queryWeekGoodsInventoryCount(map24);
            if (inventoryAmount > 0) {
                //productTotal
                Map<String, Object> map6 = new HashMap<>();
                map6.put("depFatherId", depFatherId);
                map6.put("status", 1);
                map6.put("inventoryType", inventoryType);
                Double productTotal = gbDepInventoryGoodsWeekService.queryWeekGoodsTotal(map6);
                formatProductTotal = String.format("%.2f", productTotal);
            }
        }
        if (inventoryType == 1) {
            Map<String, Object> map24 = new HashMap<>();
            map24.put("depFatherId", depFatherId);
            map24.put("status", 1);
            map24.put("dayuTotal", 0);
            map24.put("inventoryType", inventoryType);
            Integer inventoryAmount = gbDepInventoryGoodsMonthService.queryMonthGoodsInventoryCount(map24);
            if (inventoryAmount > 0) {
                //productTotal
                Map<String, Object> map6 = new HashMap<>();
                map6.put("depFatherId", depFatherId);
                map6.put("status", 1);
                map6.put("inventoryType", inventoryType);
                Double productTotal = gbDepInventoryGoodsMonthService.queryMonthGoodsTotal(map6);
                formatProductTotal = String.format("%.2f", productTotal);

            }
        }
        return formatProductTotal;
    }

    private String getInventoryTypeReturnTotal(Integer depFatherId, Integer inventoryType) {
        String formatReturnSubtotal = "0.0";
        if (inventoryType == 3) {
            Map<String, Object> map24 = new HashMap<>();
            map24.put("depFatherId", depFatherId);
            map24.put("status", 1);
            map24.put("dayuReturn", 0);
            map24.put("inventoryType", inventoryType);
            Integer inventoryAmount = gbDepInventoryGoodsDailyService.queryDailyGoodsInventoryCount(map24);
            if (inventoryAmount > 0) {
                //productTotal
                Map<String, Object> map6 = new HashMap<>();
                map6.put("depFatherId", depFatherId);
                map6.put("status", 1);
                map6.put("inventoryType", inventoryType);
                Double returnTotal = gbDepInventoryGoodsDailyService.queryDailyGoodsReturnTotal(map6);
                formatReturnSubtotal = String.format("%.2f", returnTotal);
            }
        }
        if (inventoryType == 2) {
            Map<String, Object> map24 = new HashMap<>();
            map24.put("depFatherId", depFatherId);
            map24.put("status", 1);
            map24.put("dayuReturn", 0);
            map24.put("inventoryType", inventoryType);
            Integer inventoryAmount = gbDepInventoryGoodsWeekService.queryWeekGoodsInventoryCount(map24);
            if (inventoryAmount > 0) {
                //productTotal
                Map<String, Object> map6 = new HashMap<>();
                map6.put("depFatherId", depFatherId);
                map6.put("status", 1);
                map6.put("inventoryType", inventoryType);
                Double returnTotal = gbDepInventoryGoodsWeekService.queryWeekGoodsReturnTotal(map6);
                formatReturnSubtotal = String.format("%.2f", returnTotal);
            }
        }
        if (inventoryType == 1) {
            Map<String, Object> map24 = new HashMap<>();
            map24.put("depFatherId", depFatherId);
            map24.put("status", 1);
            map24.put("dayuReturn", 0);
            map24.put("inventoryType", inventoryType);
            Integer inventoryAmount = gbDepInventoryGoodsMonthService.queryMonthGoodsInventoryCount(map24);
            if (inventoryAmount > 0) {
                //productTotal
                Map<String, Object> map6 = new HashMap<>();
                map6.put("depFatherId", depFatherId);
                map6.put("status", 1);
                map6.put("inventoryType", inventoryType);
                Double productTotal = gbDepInventoryGoodsMonthService.queryMonthGoodsReturnTotal(map6);
                formatReturnSubtotal = String.format("%.2f", productTotal);

            }
        }
        return formatReturnSubtotal;
    }

    private String getInventoryTypeLossTotal(Integer depFatherId, Integer inventoryType) {
        String formatLossSubtotal = "0.0";
        if (inventoryType == 3) {
            Map<String, Object> map24 = new HashMap<>();
            map24.put("depFatherId", depFatherId);
            map24.put("status", 1);
            map24.put("dayuLoss", 0);
            map24.put("inventoryType", inventoryType);
            Integer inventoryAmount = gbDepInventoryGoodsDailyService.queryDailyGoodsInventoryCount(map24);
            if (inventoryAmount > 0) {
                //productTotal
                Map<String, Object> map6 = new HashMap<>();
                map6.put("depFatherId", depFatherId);
                map6.put("status", 1);
                map6.put("inventoryType", inventoryType);
                Double lossTotal = gbDepInventoryGoodsDailyService.queryDailyGoodsLossTotal(map6);
                formatLossSubtotal = String.format("%.2f", lossTotal);

            }
        }
        if (inventoryType == 2) {
            Map<String, Object> map24 = new HashMap<>();
            map24.put("depFatherId", depFatherId);
            map24.put("status", 1);
            map24.put("dayuLoss", 0);
            map24.put("inventoryType", inventoryType);
            Integer inventoryAmount = gbDepInventoryGoodsWeekService.queryWeekGoodsInventoryCount(map24);
            if (inventoryAmount > 0) {
                //productTotal
                Map<String, Object> map6 = new HashMap<>();
                map6.put("depFatherId", depFatherId);
                map6.put("status", 1);
                map6.put("inventoryType", inventoryType);
                Double productTotal = gbDepInventoryGoodsWeekService.queryWeekGoodsLossTotal(map6);
                formatLossSubtotal = String.format("%.2f", productTotal);
            }
        }
        if (inventoryType == 1) {
            Map<String, Object> map24 = new HashMap<>();
            map24.put("depFatherId", depFatherId);
            map24.put("status", 1);
            map24.put("dayuLoss", 0);
            map24.put("inventoryType", inventoryType);
            Integer inventoryAmount = gbDepInventoryGoodsMonthService.queryMonthGoodsInventoryCount(map24);
            if (inventoryAmount > 0) {
                //productTotal
                Map<String, Object> map6 = new HashMap<>();
                map6.put("depFatherId", depFatherId);
                map6.put("status", 1);
                map6.put("inventoryType", inventoryType);
                Double productTotal = gbDepInventoryGoodsMonthService.queryMonthGoodsLossTotal(map6);
                formatLossSubtotal = String.format("%.2f", productTotal);

            }
        }
        return formatLossSubtotal;
    }

    private String getInventoryTypeWasteTotal(Integer depFatherId, Integer inventoryType) {
        String formatWasteTotal = "0.0";
        if (inventoryType == 3) {
            Map<String, Object> map24 = new HashMap<>();
            map24.put("depFatherId", depFatherId);
            map24.put("status", 1);
            map24.put("dayuWaste", 0);
            map24.put("inventoryType", inventoryType);
            Integer inventoryAmount = gbDepInventoryGoodsDailyService.queryDailyGoodsInventoryCount(map24);
            if (inventoryAmount > 0) {
                //productTotal
                Map<String, Object> map6 = new HashMap<>();
                map6.put("depFatherId", depFatherId);
                map6.put("status", 1);
                map6.put("inventoryType", inventoryType);
                Double lossTotal = gbDepInventoryGoodsDailyService.queryDailyGoodsWasteTotal(map6);
                formatWasteTotal = String.format("%.2f", lossTotal);
            }
        }
        if (inventoryType == 2) {
            Map<String, Object> map24 = new HashMap<>();
            map24.put("depFatherId", depFatherId);
            map24.put("status", 1);
            map24.put("dayuWaste", 0);
            map24.put("inventoryType", inventoryType);
            Integer inventoryAmount = gbDepInventoryGoodsWeekService.queryWeekGoodsInventoryCount(map24);
            if (inventoryAmount > 0) {
                //productTotal
                Map<String, Object> map6 = new HashMap<>();
                map6.put("depFatherId", depFatherId);
                map6.put("status", 1);
                map6.put("inventoryType", inventoryType);
                Double productTotal = gbDepInventoryGoodsWeekService.queryWeekGoodsWasteTotal(map6);
                formatWasteTotal = String.format("%.2f", productTotal);
            }
        }
        if (inventoryType == 1) {
            Map<String, Object> map24 = new HashMap<>();
            map24.put("depFatherId", depFatherId);
            map24.put("status", 1);
            map24.put("dayuWaste", 0);
            map24.put("inventoryType", inventoryType);
            Integer inventoryAmount = gbDepInventoryGoodsMonthService.queryMonthGoodsInventoryCount(map24);
            if (inventoryAmount > 0) {
                //productTotal
                Map<String, Object> map6 = new HashMap<>();
                map6.put("depFatherId", depFatherId);
                map6.put("status", 1);
                map6.put("inventoryType", inventoryType);
                Double productTotal = gbDepInventoryGoodsMonthService.queryMonthGoodsWasteTotal(map6);
                formatWasteTotal = String.format("%.2f", productTotal);

            }
        }
        return formatWasteTotal;
    }


    /**
     * vue后台接口
     *
     * @param depId
     * @return
     */
    @RequestMapping(value = "/disGetDepartmentInStockGoods/{depId}")
    @ResponseBody
    public R disGetDepartmentInStockGoods(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", depId);
        map.put("dayuStatus", -1);
        List<GbDistributerGoodsEntity> gbDepGoodsStockEntities = gbDepGoodsStockService.queryDisGoodsStockDetailByParams(map);
        return R.ok().put("data", gbDepGoodsStockEntities);
    }

    /**
     * vue后台接口
     *
     * @param depId
     * @return
     */
    @RequestMapping(value = "/disGetDepartmentOutStockGoods/{depId}")
    @ResponseBody
    public R disGetDepartmentOutStockGoods(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("fromDepId", depId);
        map.put("dayuStatus", -1);
        List<GbDistributerGoodsEntity> gbDepGoodsStockEntities = gbDepGoodsStockService.queryDisGoodsStockDetailByParams(map);
        return R.ok().put("data", gbDepGoodsStockEntities);
    }

    /**
     * vue后台接口
     *
     * @param depId
     * @return
     */
    @RequestMapping(value = "/disGetDepartmentStockGoods/{depId}")
    @ResponseBody
    public R disGetDepartmentStockGoods(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", depId);
        map.put("dayuStatus", -1);
        List<GbDistributerGoodsEntity> gbDepGoodsStockEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
        return R.ok().put("data", gbDepGoodsStockEntities);
    }


    /**
     * 库房 出库的时候
     *
     * @param goodsId 库存商品ID
     * @param toDepId 库房部门id
     * @return
     */
    @RequestMapping(value = "/getStockRoomGoodsStock")
    @ResponseBody
    public R getStockRoomGoodsStock(Integer goodsId, Integer toDepId, Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", goodsId);
        map.put("depFatherId", toDepId);
        map.put("restWeight", 0);
        List<GbDepartmentGoodsStockEntity> gbDepGoodsStockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depFatherId", depId);
        map1.put("disGoodsId", goodsId);
        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(map1);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("arr", gbDepGoodsStockEntities);
        map2.put("depGoods", departmentDisGoodsEntities.get(0));
        return R.ok().put("data", map2);
    }


}
