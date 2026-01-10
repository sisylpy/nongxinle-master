package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-24 11:45
 */

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;


@RestController
@RequestMapping("api/gbdistributerpurchasegoods")
public class GbDistributerPurchaseGoodsController {
    @Autowired
    private GbDistributerPurchaseGoodsService gbDpgService;
    @Autowired
    private GbDistributerPurchaseBatchService gbDPBService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerFatherGoodsService gbDistributerFatherGoodsService;
    @Autowired
    private GbDistributerGoodsPriceService gbDistributerGoodsPriceService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepartmentStockReduceService;
    @Autowired
    private NxJrdhSupplierService nxJrdhSupplierService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private GbDepartmentGoodsDailyService gbDepGoodsDailyService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private NxDistributerPurchaseGoodsService nxDistributerPurchaseGoodsService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDPBService;
    @Autowired
    private NxGoodsPriceService nxGoodsPriceService;
    @Autowired
    private NxDistributerCouponService nxDistributerCouponService;
    @Autowired
    private NxGbDistibuterUserCouponService nxGbDistibuterUserCouponService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDistributerGbDistributerService nxDistributerGbDistributerService;
    @Autowired
    private GbDepartmentUserService gbDepartmentUserService;

    @Autowired
    private GbDistributerWeightTotalService gbDistributerWeightTotalService;
    @Autowired
    private GbDistributerGoodsShelfService gbDisGoodsShelfService;

    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    /**
     * PURCHASE，DISTRIBUTE
     * 获取群用户
     *
     * @param depId 群id
     * @return 用户列表
     */
    @RequestMapping(value = "/getDepUsersByFatherIdGb", method = RequestMethod.POST )
    @ResponseBody
    public R getDepUsersByFatherIdGb(String startDate, String stopDate, Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("purDepId", depId);
        System.out.println("mapapUUUUUUUU" + map);

//        List<GbDepartmentUserEntity> userEntities = gbDpgService.queryPurUserList(map);
        List<GbDepartmentUserEntity> userEntities = gbDepartmentUserService.queryAllUsersByDepId(depId);
        if(userEntities.size() > 0){
            for(GbDepartmentUserEntity userEntity : userEntities){

                Map<String, Object> mapB = new HashMap<>();
                mapB.put("purUserId",userEntity.getGbDepartmentUserId());
                mapB.put("dayuStatus", 2);
                mapB.put("startDate", startDate);
                mapB.put("stopDate", stopDate);
                mapB.put("typeNotEqual", 9);
                Double aDouble = 0.0;
                Double aDoubleReturn = 0.0;
                Integer integer = gbDpgService.queryGbPurchaseGoodsCount(mapB);
                if(integer > 0){
                    aDouble  = gbDpgService.queryPurchaseGoodsSubTotal(mapB);
                }
                mapB.put("typeNotEqual", null);
                mapB.put("purType", 9);
                System.out.println("reuree" + mapB);
                Integer integerReturn = gbDpgService.queryGbPurchaseGoodsCount(mapB);
                if(integerReturn > 0){
                    aDoubleReturn  = gbDpgService.queryPurchaseGoodsSubTotal(mapB);
                }

                Map<String, Object> mapData = new HashMap<>();
                mapData.put("billCount", integer);
                mapData.put("billTotal", String.format("%.1f",aDouble));

                mapData.put("returnBillCount", integerReturn);
                mapData.put("returnPayTotal", String.format("%.1f",aDoubleReturn));
                int i = integer + integerReturn;
                double v = aDouble - aDoubleReturn;
                mapData.put("actBillCount", i);
                mapData.put("actPayTotal", String.format("%.1f",v));
                userEntity.setItemData(mapData);
            }
        }

        return R.ok().put("data", userEntities);
    }



    @RequestMapping(value = "/purUserGetPurGoodsInfo", method = RequestMethod.POST)
    @ResponseBody
    public R purUserGetPurGoodsInfo (Integer purUserId, Integer disGoodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", purUserId);
        map.put("disGoodsId", disGoodsId);
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryPurchaseGoodsLastItem(map);
        if(purchaseGoodsEntity != null){
            return R.ok().put("data", purchaseGoodsEntity.getGbDpgBuyPrice());
        }else{
            return R.error(-1,"没有采购商品历史");
        }

    }

    @RequestMapping(value = "/deleteDisPurAndNxDataItem/{id}")
    @ResponseBody
    public R deleteDisPurAndNxDataItem(@PathVariable Integer id) {
        GbDistributerPurchaseGoodsEntity purGoods = gbDpgService.queryObject(id);

        Integer gbDpgBatchId = purGoods.getGbDpgBatchId();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("batchId", gbDpgBatchId);
        List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDpgService.queryOnlyPurGoods(map1);
        if (goodsEntities.size() == 1) {
            gbDPBService.delete(gbDpgBatchId);
        }
        purGoods.setGbDpgPurchaseType(2);
        purGoods.setGbDpgBatchId(null);
        purGoods.setGbDpgPurUserId(null);
        purGoods.setGbDpgStatus(0);
        purGoods.setGbDpgTime(null);
        purGoods.setGbDpgBuySubtotal("0");
        purGoods.setGbDpgBuyPrice(null);
        purGoods.setGbDpgBuyScalePrice(null);
        purGoods.setGbDpgBuyScaleQuantity(null);
        purGoods.setGbDpgPurchaseFullTime(null);
        purGoods.setGbDpgPurchaseMonth(null);
        purGoods.setGbDpgPurchaseYear(null);
        purGoods.setGbDpgPurchaseWeek(null);
        purGoods.setGbDpgPurchaseWeekYear(null);
        purGoods.setGbDpgPurchaseNxDistributerId(-1);
        System.out.println("purururruururur" + purGoods.getGbDistributerPurchaseGoodsId());
        gbDpgService.update(purGoods);


        Integer nxDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
        Map<String, Object> map = new HashMap<>();
        map.put("purGoodsId", nxDistributerPurchaseGoodsId);
        System.out.println("ororororooorororr" + map);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
        for (GbDepartmentOrdersEntity orders : ordersEntities) {

            if (orders.getGbDoNxDepartmentOrderId() != null && orders.getGbDoNxDepartmentOrderId() != -1) {
                System.out.println("nxnxnxnxxxoxoxxoxoxoxo" + orders.getGbDoNxDepartmentOrderId());
                Integer gbDoNxDepartmentOrderId = orders.getGbDoNxDepartmentOrderId();
                NxDepartmentOrdersEntity ordersEntity1 = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
                if (ordersEntity1 != null) {

                    Map<String, Object> mapGbDep = new HashMap<>();
                    mapGbDep.put("gbDepId", orders.getGbDoDepartmentId());
                    mapGbDep.put("disGoodsId", ordersEntity1.getNxDoDisGoodsId());
                    NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(mapGbDep);
                    if (departmentDisGoodsEntity != null) {
                        nxDepartmentDisGoodsService.delete(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                    }
                    System.out.println("nxnxnxnxxxoxoxxoxoxoxo" + ordersEntity1.getNxDepartmentOrdersId());
                    delNxPurGoods(ordersEntity1);
                    nxDepartmentOrdersService.delete(ordersEntity1.getNxDepartmentOrdersId());

                }
            }

            orders.setGbDoBuyStatus(getGbOrderBuyStatusNew());
            orders.setGbDoWeight("0.0");
            orders.setGbDoPrice("0.0");
            orders.setGbDoSubtotal("0.0");
            orders.setGbDoScalePrice("0.0");
            orders.setGbDoScaleWeight("0.0");
            orders.setGbDoStatus(getGbOrderStatusNew());
            orders.setGbDoPurchaseUserId(null);
            orders.setGbDoNxDistributerId(-1);
            orders.setGbDoNxDepartmentOrderId(-1);
            orders.setGbDoNxDistributerGoodsId(-1);
            orders.setGbDoGoodsType(2);
            orders.setGbDoOrderType(2);
            gbDepartmentOrdersService.update(orders);


        }
        return R.ok();

    }


    private void delNxPurGoods(NxDepartmentOrdersEntity ordersEntity) {
        if (ordersEntity.getNxDoPurchaseGoodsId() != null && ordersEntity.getNxDoPurchaseGoodsId() != -1) {
            NxDistributerPurchaseGoodsEntity nxDistributerPurchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(ordersEntity.getNxDoPurchaseGoodsId());
            if (nxDistributerPurchaseGoodsEntity != null) {
                Integer nxDpgOrdersAmount = nxDistributerPurchaseGoodsEntity.getNxDpgOrdersAmount();
                System.out.println("purgoodoamaomm" + nxDpgOrdersAmount);
                if (nxDpgOrdersAmount > 1) {
                    nxDistributerPurchaseGoodsEntity.setNxDpgOrdersAmount(nxDpgOrdersAmount - 1);
                    BigDecimal newWeight = new BigDecimal(nxDistributerPurchaseGoodsEntity.getNxDpgQuantity()).subtract(new BigDecimal(ordersEntity.getNxDoQuantity()));
                    nxDistributerPurchaseGoodsEntity.setNxDpgBuyQuantity(newWeight.toString());
                    nxDistributerPurchaseGoodsEntity.setNxDpgQuantity(newWeight.toString());
                    System.out.println("purgoodoamaomm111" + nxDistributerPurchaseGoodsEntity.getNxDpgBuyQuantity());

                    if (ordersEntity.getNxDoStandard().equals(nxDistributerPurchaseGoodsEntity.getNxDpgStandard())) {
                        System.out.println("purgoodoamaomm" + nxDpgOrdersAmount);

                        BigDecimal price = new BigDecimal(nxDistributerPurchaseGoodsEntity.getNxDpgBuyPrice());
                        BigDecimal decimal1 = newWeight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                        nxDistributerPurchaseGoodsEntity.setNxDpgBuySubtotal(decimal1.toString());
                    }
                    nxDistributerPurchaseGoodsService.update(nxDistributerPurchaseGoodsEntity);
                } else {
                    if (nxDistributerPurchaseGoodsEntity.getNxDpgBatchId() != null) {
                        Integer nxDpgBatchId = nxDistributerPurchaseGoodsEntity.getNxDpgBatchId();
                        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByBatchId(nxDpgBatchId);
                        if (purchaseGoodsEntities.size() == 1) {
                            nxDPBService.delete(nxDpgBatchId);
                        } else {
                            String nxDpgBuySubtotal = nxDistributerPurchaseGoodsEntity.getNxDpgBuySubtotal();
                            NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryObject(nxDpgBatchId);
                            if (nxDistributerPurchaseBatchEntity.getNxDpbSellSubtotal() != null && !nxDistributerPurchaseBatchEntity.getNxDpbSellSubtotal().trim().isEmpty()) {
                                String sellSubtotal = nxDistributerPurchaseBatchEntity.getNxDpbSellSubtotal();
                                if (sellSubtotal != null && nxDpgBuySubtotal != null) {
                                    BigDecimal decimal = new BigDecimal(sellSubtotal);
                                    BigDecimal decimal2 = new BigDecimal(nxDpgBuySubtotal);
                                    BigDecimal decimal1 = decimal.subtract(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                                    nxDistributerPurchaseBatchEntity.setNxDpbSellSubtotal(decimal1.toString());
                                    nxDPBService.update(nxDistributerPurchaseBatchEntity);
                                }
                            }
                        }
                    }

                    nxDistributerPurchaseGoodsService.delete(nxDistributerPurchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
                }
            }
        }
    }

//
//    @RequestMapping(value = "/disGetNxDistributerPurGoodsDate", method = RequestMethod.POST)
//    @ResponseBody
//    public R disGetNxDistributerPurGoodsDate(Integer disId, String startDate, String stopDate) {
//
//
//        Map<String, Object> map4 = new HashMap<>();
//        map4.put("disId", disId);
//        map4.put("dayuBuyStatus", 2);
//        map4.put("nxDis", 1);
//        map4.put("startDate", startDate);
//        map4.put("stopDate", stopDate);
//        System.out.println("map4444444=====" + map4);
//
//        List<NxDistributerEntity> distributerEntities = gbDepartmentOrdersService.queryGbDepNxDistributerOrder(map4);
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        if (distributerEntities.size() > 0) {
//            for (int i = 0; i < distributerEntities.size(); i++) {
//                NxDistributerEntity distributerEntity = distributerEntities.get(i);
//                Map<String, Object> mapItem = new HashMap<>();
//                mapItem.put("nxDis", distributerEntity);
//                Map<String, Object> mapB = new HashMap<>();
//
//                mapB.put("startDate", startDate);
//                mapB.put("stopDate", stopDate);
//                mapB.put("disId", distributerEntity.getNxDistributerId());
//                mapB.put("gbDisId", disId);
//                mapB.put("dayuStatus", -1);
//                System.out.println("wwhwhwhw" + mapB);
//                List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryGbDepBillsByParams(mapB);
//                mapItem.put("arr", billEntityList);
//
//                result.add(mapItem);
//            }
//        }
//        return R.ok().put("data", result);
//    }


    @RequestMapping(value = "/getNxGoodsFenxi", method = RequestMethod.POST)
    @ResponseBody
    public R getNxGoodsFenxi(Integer nxDisId, String startDate, String stopDate, Integer gbDisId, String type) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        NxDistributerEntity supplierEntity = nxDistributerService.queryObject(nxDisId);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("nxDisId", nxDisId);
        map1.put("disId", gbDisId);
        map1.put("dayuStatus", 1);
        map1.put("startDate", startDate);
        map1.put("stopDate", stopDate);
        map1.put("typeNotEqual", 9);
        System.out.println("mapapaudfadf" + map1);
        Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map1);

        TreeSet<GbDistributerGoodsEntity> goodsList = new TreeSet<>();
        if (integer > 0) {
//            List<GbDistributerFatherGoodsEntity> greatGrand = gbDpgService.queryGreatGrandGoodsByDisGoods(map1);
            List<GbDistributerFatherGoodsEntity> greatGrand = gbDpgService.queryGbFatherDisPurchaseGoods(map1);
            if (greatGrand.size() > 0) {
                for (GbDistributerFatherGoodsEntity greatGrandEntity : greatGrand) {
                    List<GbDistributerFatherGoodsEntity> grandEntityFatherGoodsEntities = greatGrandEntity.getFatherGoodsEntities();
                    if (grandEntityFatherGoodsEntities.size() > 0) {
                        for (GbDistributerFatherGoodsEntity grand : grandEntityFatherGoodsEntities) {
                            List<GbDistributerGoodsEntity> goodsEntities = grand.getGbDistributerGoodsEntities();
                            for (GbDistributerGoodsEntity goodsEntity : goodsEntities) {
                                System.out.println("gggg" + goodsEntity.getGbDgGoodsName());
                                Double doutbleCost = null;
                                Double doutbleCostV = null;
                                double v = 0;
                                String maxPrice = "0";
                                String minPrice = "0";
                                String perPrice = "0";
                                int purCount = 0;

                                Map<String, Object> map = new HashMap<>();
                                map.put("startDate", startDate);
                                map.put("stopDate", stopDate);
                                map.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                                map.put("dayuStatus", 2);
                                map.put("typeNotEqual", 9);
                                map.put("nxDisId", nxDisId);
                                map.put("disId", gbDisId);
                                System.out.println("mappsspspspspspspps" + map);
                                purCount = gbDpgService.queryGbPurchaseGoodsCount(map);
                                if (purCount > 0) {
                                    System.out.println("caigoushushul" + map);
                                    doutbleCostV = gbDpgService.queryPurchaseGoodsSubTotal(map);
                                    doutbleCost = gbDpgService.queryPurchaseGoodsWeightTotal(map);

                                    // 添加null检查和除零保护
                                    if (doutbleCostV != null && doutbleCost != null && doutbleCost != 0) {
                                        v = doutbleCostV / doutbleCost;
                                        perPrice = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                    } else {
                                        perPrice = "0.00";
                                    }

                                    maxPrice = gbDpgService.queryPurGoodsMaxPrice(map);
                                    minPrice = gbDpgService.queryPurGoodsMinPrice(map);

                                }

                                Map<String, Object> mapStars = new HashMap<>();
                                mapStars.put("startDate", startDate);
                                mapStars.put("stopDate", stopDate);
                                mapStars.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                                mapStars.put("starsLevel", 5);
                                mapStars.put("nxDisId", nxDisId);
                                System.out.println("mapStarsmapStarsmapStars" + mapStars);
                                Integer starsFive = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
                                mapStars.put("starsLevel", 4);
                                Integer starsFour = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
                                mapStars.put("starsLevel", 3);
                                Integer starsThree = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
                                mapStars.put("starsLevel", 2);
                                Integer starsTwo = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
                                mapStars.put("starsLevel", 1);
                                Integer starsOne = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
                                Map<String, Object> mapStar = new HashMap<>();
                                mapStar.put("starsFive", starsFive);
                                mapStar.put("starsFour", starsFour);
                                mapStar.put("starsThree", starsThree);
                                mapStar.put("starsTwo", starsTwo);
                                mapStar.put("starsOne", starsOne);


                                Map<String, Object> mapResult = new HashMap<>();
                                mapResult.put("totalCost", new BigDecimal(doutbleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                mapResult.put("totalCostSubtotal", new BigDecimal(doutbleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                mapResult.put("maxPrice", maxPrice);
                                mapResult.put("minPrice", minPrice);
                                mapResult.put("perPrice", perPrice);
                                mapResult.put("purCount", purCount);
                                mapResult.put("starsCount", mapStar);
                                goodsEntity.setGoodsData(mapResult);

                                map1.put("dayuStatus", null);
                                System.out.println("stoosososossososososos" + map1);

                                double goodsDoutbleCostV = 0;
                                double goodsDoutbleCost = 0;
                                double goodsDoutbleRestV = 0;
                                double goodsDoutbleRest = 0;
                                double goodsDoutbleLoss = 0;
                                double goodsDoutbleLossV = 0;
                                double goodsDoutbleWaste = 0;
                                double goodsDoutbleWasteV = 0;
                                double goodsDoutbleProduce = 0;
                                double goodsDoutbleProduceV = 0;
                                double goodsDoutbleReturnV = 0;
                                double goodsDoutbleReturn = 0;

                                Map<String, Object> mapDepStock = new HashMap<>();
                                mapDepStock.put("nxDisId", nxDisId);
                                mapDepStock.put("disId", gbDisId);
                                mapDepStock.put("startDate", startDate);
                                mapDepStock.put("stopDate", stopDate);
                                mapDepStock.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                                System.out.println("mapDepStockmapDepStock" + mapDepStock);
                                Integer integer2 = gbDepartmentGoodsStockService.queryGoodsStockCount(mapDepStock);
                                if (integer2 > 0) {
                                    goodsDoutbleCostV = gbDepartmentGoodsStockService.queryDepGoodsSubtotal(mapDepStock);
                                    goodsDoutbleCost = gbDepartmentGoodsStockService.queryDepStockWeightTotal(mapDepStock);

                                    goodsDoutbleRest = gbDepartmentGoodsStockService.queryDepGoodsRestWeightTotal(mapDepStock);
                                    goodsDoutbleRestV = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(mapDepStock);

                                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                                    System.out.println("pururururuururu" + mapDepStock);
                                    Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                                    if (integerProduce > 0) {
                                        goodsDoutbleProduceV = gbDepartmentStockReduceService.queryReduceProduceTotal(mapDepStock);
                                        goodsDoutbleProduce = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(mapDepStock);
                                    } else {
                                        goodsDoutbleProduceV = 0;
                                        goodsDoutbleProduce = 0;
                                    }
                                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                                    Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                                    if (integerLoss > 0) {
                                        goodsDoutbleLossV = gbDepartmentStockReduceService.queryReduceLossTotal(mapDepStock);
                                        goodsDoutbleLoss = gbDepartmentStockReduceService.queryReduceLossWeightTotal(mapDepStock);
                                    } else {
                                        goodsDoutbleLossV = 0;
                                        goodsDoutbleLoss = 0;
                                    }
                                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                                    Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                                    if (integerWaste > 0) {
                                        goodsDoutbleWasteV = gbDepartmentStockReduceService.queryReduceWasteTotal(mapDepStock);
                                        goodsDoutbleWaste = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(mapDepStock);
                                    } else {
                                        goodsDoutbleWasteV = 0;
                                        goodsDoutbleWaste = 0;
                                    }
                                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
                                    Integer integerReturn = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                                    if (integerReturn > 0) {
                                        goodsDoutbleReturnV = gbDepartmentStockReduceService.queryReduceReturnTotal(mapDepStock);
                                        goodsDoutbleReturn = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(mapDepStock);
                                    } else {
                                        goodsDoutbleReturnV = 0;
                                    }

                                    goodsEntity.setGoodsCostWeightTotal(goodsDoutbleCost);
                                    goodsEntity.setGoodsCostWeightTotalString(String.format("%.1f", goodsDoutbleCost));
                                    goodsEntity.setGoodsCostTotal(new BigDecimal(goodsDoutbleCostV).setScale(1, BigDecimal.ROUND_HALF_UP));
                                    goodsEntity.setGoodsCostTotalString(String.format("%.1f", goodsDoutbleCostV));

                                    goodsEntity.setGoodsStockTotal(goodsDoutbleRestV);
                                    goodsEntity.setGoodsStockTotalString(String.format("%.1f", goodsDoutbleRestV));
                                    goodsEntity.setGoodsStockWeightTotal(goodsDoutbleRest);
                                    goodsEntity.setGoodsStockWeightTotalString(String.format("%.1f", goodsDoutbleRest));

                                    goodsEntity.setGoodsProduceWeightTotal(goodsDoutbleProduce);
                                    goodsEntity.setGoodsProduceWeightTotalString(String.format("%.1f", goodsDoutbleProduce));
                                    goodsEntity.setGoodsProduceTotal(new BigDecimal(goodsDoutbleProduceV).setScale(1, BigDecimal.ROUND_HALF_UP));
                                    goodsEntity.setGoodsProduceTotalString(String.format("%.1f", goodsDoutbleProduceV));

                                    goodsEntity.setGoodsLossWeightTotal(goodsDoutbleLoss);
                                    goodsEntity.setGoodsLossWeightTotalString(String.format("%.1f", goodsDoutbleLoss));
                                    goodsEntity.setGoodsLossTotal(new BigDecimal(goodsDoutbleLossV).setScale(1, BigDecimal.ROUND_HALF_UP));
                                    goodsEntity.setGoodsLossTotalString(String.format("%.1f", goodsDoutbleLossV));

                                    goodsEntity.setGoodsWasteWeightTotal(goodsDoutbleWaste);
                                    goodsEntity.setGoodsWasteWeightTotalString(String.format("%.1f", goodsDoutbleWaste));
                                    goodsEntity.setGoodsWasteTotal(new BigDecimal(goodsDoutbleWasteV).setScale(1, BigDecimal.ROUND_HALF_UP));
                                    goodsEntity.setGoodsWasteTotalString(String.format("%.1f", goodsDoutbleWasteV));

                                    goodsEntity.setGoodsReturnWeightTotal(goodsDoutbleReturn);
                                    goodsEntity.setGoodsReturnWeightTotalString(String.format("%.1f", goodsDoutbleReturn));
                                    goodsEntity.setGoodsReturnTotal(goodsDoutbleReturnV);
                                    goodsEntity.setGoodsReturnTotalString(String.format("%.1f", goodsDoutbleReturnV));

                                    BigDecimal divide = new BigDecimal(integer2).divide(new BigDecimal(howManyDaysInPeriod), 1, BigDecimal.ROUND_HALF_UP);

                                    goodsEntity.setGoodsAverageOrderTimes(divide.toString());
                                    int total = gbDepartmentGoodsStockService.queryGoodsStockStars(mapDepStock);
                                    System.out.println("tattletale" + total);
                                    System.out.println("integer2integer2" + integer2);
                                    int i = total / integer2;
                                    int remainder = total % integer2;
                                    int gray = 0;
                                    if (remainder > 0) {
                                        remainder = 1;
                                    }
                                    gray = 5 - i - remainder;
                                    goodsEntity.setGoodsStarGreen(i);
                                    goodsEntity.setGoodsStarHalf(remainder);
                                    goodsEntity.setGoodsStarGray(gray);
                                    goodsEntity.setGoodsAverageStars(i);
                                    goodsEntity.setGoodsAverageStarsString(String.valueOf(i));
                                    goodsEntity.setGoodsPurTotalCount(integer2);
                                    goodsList.add(goodsEntity);
                                }
                            }
                        }
                    }

                }
            }

        }


        map1.put("dayuStatus", null);
        System.out.println("stoosososossososososos" + map1);
        Integer integer1 = gbDepartmentGoodsStockService.queryGoodsStockCount(map1);
        if (integer1 > 0) {
            double depDoutbleCostV = 0;
            double depDoutbleRestV = 0;
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
            mapDepStock.put("nxDisId", nxDisId);
            mapDepStock.put("disId", gbDisId);
            mapDepStock.put("startDate", startDate);
            mapDepStock.put("stopDate", stopDate);
            System.out.println("mapDepStockmapDepStock" + mapDepStock);
            Integer integer2 = gbDepartmentGoodsStockService.queryGoodsStockCount(mapDepStock);
            if (integer2 > 0) {
                depDoutbleCostV = gbDepartmentGoodsStockService.queryDepGoodsSubtotal(mapDepStock);
                depDoutbleCost = gbDepartmentGoodsStockService.queryDepStockWeightTotal(mapDepStock);
                depDoutbleRestV = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(mapDepStock);

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
                }

                supplierEntity.setPurGoodsTotalString(String.format("%.1f", depDoutbleCostV));
                supplierEntity.setStockGoodsTotalString(String.format("%.1f", depDoutbleRestV));
                supplierEntity.setProduceGoodsTotalString(String.format("%.1f", depDoutbleProduceV));
                supplierEntity.setWasteGoodsTotalString(String.format("%.1f", depDoutbleWasteV));
                supplierEntity.setLossGoodsTotalString(String.format("%.1f", depDoutbleLossV));
                supplierEntity.setReturnGoodsTotalString(String.format("%.1f", depDoutbleReturnV));
            }

            int total = gbDepartmentGoodsStockService.queryGoodsStockStars(map1);
            System.out.println("abccbcbcbcbcbcbcb" + total);
            System.out.println("abccbcbcbcbcbcbcb" + integer2);
            int i = total / integer2;
            int remainder = total % integer2;
            int singleDigitRemainder = remainder % 10;

// 如果 remainder 是负数，你需要将其转换为正数再取余
// 因为在大多数编程语言中，负数取余还是负数
            if (singleDigitRemainder < 0) {
                singleDigitRemainder += 10;
            }
            int gray = 5 - i;
            supplierEntity.setStarGreen(i);
            supplierEntity.setStarHalf(singleDigitRemainder);
            supplierEntity.setStarGray(gray);

        }


        Map<String, Object> map = new HashMap<>();
        if (type.equals("goods")) {
            map.put("arr", goodsList);
        } else if (type.equals("cost")) {
            map.put("arr", abcGoodsCost(goodsList));
        } else if (type.equals("costWeight")) {
            map.put("arr", abcGoodsCostWeight(goodsList));
        } else if (type.equals("produce")) {
            map.put("arr", abcGoodsProduce(goodsList));
        } else if (type.equals("losss")) {
            map.put("arr", abcGoodsLoss(goodsList));
        } else if (type.equals("waste")) {
            map.put("arr", abcGoodsWaste(goodsList));
        } else if (type.equals("return")) {
            map.put("arr", abcGoodsReturn(goodsList));
        } else if (type.equals("stars")) {
            map.put("arr", abcGoodsStars(goodsList));
        }
        map.put("item", supplierEntity);

        return R.ok().put("data", map);
    }


    private TreeSet<GbDistributerGoodsEntity> abcGoodsCost(TreeSet<GbDistributerGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                System.out.println("o1ccgetGoodsCostTotalgetGoodsCostTotal" + o1.getGoodsCostTotal());
                // 使用 BigDecimal 的 compareTo
                int result = o2.getGoodsCostTotal().compareTo(o1.getGoodsCostTotal());
                if (result == 0) {
                    // 如果相等，为了避免TreeSet认为相等而丢掉元素，使用id或其他唯一键作为比较
                    return o2.getGbDistributerGoodsId().compareTo(o1.getGbDistributerGoodsId());
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }

    private TreeSet<GbDistributerGoodsEntity> abcGoodsCostWeight(TreeSet<GbDistributerGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                System.out.println("getGoodsCostWeightTotalgetGoodsCostWeightTotal" + o1.getGoodsCostWeightTotal());
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

    private TreeSet<GbDistributerGoodsEntity> abcGoodsProduce(TreeSet<GbDistributerGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {

                int result = o2.getGoodsProduceTotal().compareTo(o1.getGoodsProduceTotal());
                if (result == 0) {
                    // 如果相等，为了避免TreeSet认为相等而丢掉元素，使用id或其他唯一键作为比较
                    return o2.getGbDistributerGoodsId().compareTo(o1.getGbDistributerGoodsId());
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }

    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherPurchaseTotal(TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                System.out.println("o1ccgetGoodsCostTotalgetGoodsCostTotal" + o1.getPurchaseSubTotalDouble());
                int result;
                if (o2.getPurchaseSubTotalDouble() - o1.getPurchaseSubTotalDouble() < 0) {
                    result = -1;
                } else if (o2.getPurchaseSubTotalDouble() - o1.getPurchaseSubTotalDouble() > 0) {
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

    private TreeSet<GbDistributerGoodsEntity> abcGoodsLoss(TreeSet<GbDistributerGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                System.out.println("o1ccgetGoodsCostTotalgetGoodsCostTotal" + o1.getGoodsCostTotal());
                int result = o2.getGoodsLossTotal().compareTo(o1.getGoodsLossTotal());
                if (result == 0) {
                    // 如果相等，为了避免TreeSet认为相等而丢掉元素，使用id或其他唯一键作为比较
                    return o2.getGbDistributerGoodsId().compareTo(o1.getGbDistributerGoodsId());
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }

    private TreeSet<GbDistributerGoodsEntity> abcGoodsReturn(TreeSet<GbDistributerGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                System.out.println("o1ccgetGoodsCostTotalgetGoodsCostTotal" + o1.getGoodsCostTotal());
                int result;
                if (o2.getGoodsReturnTotal() - o1.getGoodsReturnTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsReturnTotal() - o1.getGoodsReturnTotal() > 0) {
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

    private TreeSet<GbDistributerGoodsEntity> abcGoodsStars(TreeSet<GbDistributerGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                System.out.println("getGoodsAverageStarsgetGoodsAverageStars" + o1.getGoodsAverageStars());
                int result;
                if (o2.getGoodsAverageStars() - o1.getGoodsAverageStars() < 0) {
                    result = 1;
                } else if (o2.getGoodsAverageStars() - o1.getGoodsAverageStars() > 0) {
                    result = -1;
                } else {
                    result = 1;
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }


    private TreeSet<GbDistributerGoodsEntity> abcGoodsWaste(TreeSet<GbDistributerGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result = o2.getGoodsWasteTotal().compareTo(o1.getGoodsWasteTotal());
                if (result == 0) {
                    // 如果相等，为了避免TreeSet认为相等而丢掉元素，使用id或其他唯一键作为比较
                    return o2.getGbDistributerGoodsId().compareTo(o1.getGbDistributerGoodsId());
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }


    @RequestMapping(value = "/getJrdhGoodsFenxi", method = RequestMethod.POST)
    @ResponseBody
    public R getJrdhGoodsFenxi(Integer supplierId, String startDate, String stopDate, Integer gbDisId, String type) {

        System.out.println("disisiisidid==" + gbDisId);
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        System.out.println("homemmenna" + howManyDaysInPeriod);

        Map<String, Object> mapS = new HashMap<>();
        mapS.put("supplierId", supplierId);
        NxJrdhSupplierEntity supplierEntity = nxJrdhSupplierService.querySellUserSupplier(mapS);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", gbDisId);
        map1.put("supplierId", supplierId);
        map1.put("dayuStatus", 1);
        map1.put("startDate", startDate);
        map1.put("stopDate", stopDate);
        map1.put("typeNotEqual", 9);
        System.out.println("cddddddd" + map1);
        Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map1);
        TreeSet<GbDistributerGoodsEntity> goodsList = new TreeSet<>();
        if (integer > 0) {
            System.out.println("wsupsspspspsp" + map1);
//            List<GbDistributerFatherGoodsEntity> greatGrand = gbDpgService.queryGreatGrandGoodsByDisGoods(map1);
            List<GbDistributerFatherGoodsEntity> greatGrand = gbDpgService.queryGbFatherDisPurchaseGoods(map1);
            if (greatGrand.size() > 0) {
                for (GbDistributerFatherGoodsEntity greatGrandEntity : greatGrand) {
                    List<GbDistributerFatherGoodsEntity> grandEntityFatherGoodsEntities = greatGrandEntity.getFatherGoodsEntities();
                    if (grandEntityFatherGoodsEntities.size() > 0) {
                        for (GbDistributerFatherGoodsEntity grand : grandEntityFatherGoodsEntities) {
                            List<GbDistributerGoodsEntity> goodsEntities = grand.getGbDistributerGoodsEntities();
                            for (GbDistributerGoodsEntity goodsEntity : goodsEntities) {
                                System.out.println("gggg" + goodsEntity.getGbDgGoodsName());

                                Map<String, Object> mapDay = new HashMap<>();
                                List<String> dateList = new ArrayList<>();
                                List<Map<String, Object>> dateDataList = new ArrayList<>();

                                List<String> lowestPriceList = new ArrayList<>();
                                List<String> highestPriceList = new ArrayList<>();

                                Double doutbleCost = null;
                                Double doutbleCostV = null;
                                double v = 0;
                                String maxPrice = "0";
                                String minPrice = "0";
                                String perPrice = "0";
                                int purCount = 0;

                                Map<String, Object> map = new HashMap<>();
                                map.put("startDate", startDate);
                                map.put("stopDate", stopDate);
                                map.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                                map.put("dayuStatus", 2);
                                map.put("supplierId", supplierId);
                                map.put("typeNotEqual", 9);

                                purCount = gbDpgService.queryGbPurchaseGoodsCount(map);
                                if (purCount > 0) {
                                    doutbleCostV = gbDpgService.queryPurchaseGoodsSubTotal(map);
                                    doutbleCost = gbDpgService.queryPurchaseGoodsWeightTotal(map);
                                    System.out.println("AAAAA" + doutbleCostV + "bbb" + doutbleCost);

                                    // 添加null检查和除零保护
                                    if (doutbleCostV != null && doutbleCost != null && doutbleCost != 0) {
                                        v = doutbleCostV / doutbleCost;
                                        perPrice = new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                                    } else {
                                        perPrice = "0.0";
                                    }

                                    maxPrice = gbDpgService.queryPurGoodsMaxPrice(map);
                                    minPrice = gbDpgService.queryPurGoodsMinPrice(map);
                                }
//                                /////////////////
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
                                        //1.day
                                        String substring = whichDay.substring(8, 10);
                                        dateList.add(substring);
                                        //2,lowest
                                        if (goodsEntity.getGbDgNxGoodsId() != null) {
                                            Map<String, Object> mapL = new HashMap<>();
                                            mapL.put("nxGoodsId", goodsEntity.getGbDgNxGoodsId());
                                            mapL.put("date", whichDay);
                                            mapL.put("cityId", supplierEntity.getNxJrdhsSysCityId());
                                            mapL.put("marketId", 2);
                                            System.out.println("mappllllll" + mapL);
                                            int count = nxGoodsPriceService.queryPriceGoodsCount(mapL);
                                            if (count > 0) {
                                                double lowestPrice = nxGoodsPriceService.queryLowestPriceByParams(mapL);
                                                lowestPriceList.add(String.format("%.1f", lowestPrice));

                                                //3,highest
                                                double highestPrice = nxGoodsPriceService.queryHighestPriceByParams(mapL);
                                                highestPriceList.add(String.format("%.1f", highestPrice));
                                            } else {
                                                lowestPriceList.add("0");
                                                highestPriceList.add("0");
                                            }
                                        }

                                        //4,supplier
                                        mapDay.put("date", whichDay);
                                        mapDay.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                                        mapDay.put("supplierId", supplierId);
                                        mapDay.put("typeNotEqual", 9);
                                        System.out.println("wokkkkkk" + mapDay);
                                        int purCountDay = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
                                        if (purCountDay == 1) {
                                            String price = gbDpgService.queryPurchaseGoodsPrice(mapDay);
                                            String weight = gbDpgService.queryPurchaseGoodsWeight(mapDay);

                                            // 添加null检查，防止数据库字段值为null
                                            if (price == null || weight == null) {
                                                // 如果查询结果为空，按0处理
                                                Map<String, Object> mapDayValue = new HashMap<>();
                                                mapDayValue.put("dayPrice", "0.0");
                                                mapDayValue.put("dayWeight", "0.0");
                                                dateDataList.add(mapDayValue);
                                            } else {
                                                // 检查空字符串
                                                if (price.trim().isEmpty()) {
                                                    price = "0";
                                                }
                                                if (weight.trim().isEmpty()) {
                                                    weight = "0";
                                                }

                                                Map<String, Object> mapDayValue = new HashMap<>();
                                                mapDayValue.put("dayPrice", new BigDecimal(price).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                                mapDayValue.put("dayWeight", new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                                dateDataList.add(mapDayValue);
                                            }
                                        } else if (purCountDay > 1) {
                                            Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);
                                            Double weightTotal = gbDpgService.queryPurchaseGoodsWeightTotal(mapDay);

                                            // 添加null检查和除零保护
                                            if (subTotal == null || weightTotal == null || weightTotal == 0) {
                                                Map<String, Object> mapDayValue = new HashMap<>();
                                                mapDayValue.put("dayPrice", "0.0");
                                                mapDayValue.put("dayWeight", "0.0");
                                                dateDataList.add(mapDayValue);
                                            } else {
                                                double v1 = subTotal / weightTotal;
                                                double v2 = weightTotal / purCountDay;

                                                Map<String, Object> mapDayValue = new HashMap<>();
                                                mapDayValue.put("dayPrice", new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                                mapDayValue.put("dayWeight", new BigDecimal(v2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                                dateDataList.add(mapDayValue);
                                            }
                                        } else {
                                            Map<String, Object> mapDayValue = new HashMap<>();
                                            mapDayValue.put("dayPrice", 0);
                                            mapDayValue.put("dayWeight", 0);
                                            dateDataList.add(mapDayValue);
                                        }


                                    }
                                } else {
                                    String substring = startDate.substring(8, 10);
                                    dateList.add(substring);
                                    mapDay.put("date", startDate);
                                    int purCountDay = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
                                    if (purCountDay == 1) {
                                        String price = gbDpgService.queryPurchaseGoodsPrice(mapDay);
                                        String weight = gbDpgService.queryPurchaseGoodsWeight(mapDay);

                                        // 添加null检查，防止数据库字段值为null
                                        if (price == null || weight == null) {
                                            // 如果查询结果为空，按0处理
                                            Map<String, Object> mapDayValue = new HashMap<>();
                                            mapDayValue.put("dayPrice", "0.0");
                                            mapDayValue.put("dayWeight", "0.0");
                                            dateDataList.add(mapDayValue);
                                        } else {
                                            // 检查空字符串
                                            if (price.trim().isEmpty()) {
                                                price = "0";
                                            }
                                            if (weight.trim().isEmpty()) {
                                                weight = "0";
                                            }

                                            Map<String, Object> mapDayValue = new HashMap<>();
                                            mapDayValue.put("dayPrice", new BigDecimal(price).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                            mapDayValue.put("dayWeight", new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                            dateDataList.add(mapDayValue);
                                        }
                                    } else if (purCountDay > 1) {
                                        Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);
                                        Double weightTotal = gbDpgService.queryPurchaseGoodsWeightTotal(mapDay);

                                        // 添加null检查和除零保护
                                        if (subTotal == null || weightTotal == null || weightTotal == 0) {
                                            Map<String, Object> mapDayValue = new HashMap<>();
                                            mapDayValue.put("dayPrice", "0.0");
                                            mapDayValue.put("dayWeight", "0.0");
                                            dateDataList.add(mapDayValue);
                                        } else {
                                            double v1 = subTotal / weightTotal;
                                            double v2 = weightTotal / purCountDay;

                                            Map<String, Object> mapDayValue = new HashMap<>();
                                            mapDayValue.put("dayPrice", new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                            mapDayValue.put("dayWeight", new BigDecimal(v2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                            dateDataList.add(mapDayValue);
                                        }
                                    } else {
                                        Map<String, Object> mapDayValue = new HashMap<>();
                                        mapDayValue.put("dayPrice", 0);
                                        mapDayValue.put("dayWeight", 0);
                                        dateDataList.add(mapDayValue);
                                    }

                                }


                                Map<String, Object> mapEveryDay = new HashMap<>();
                                mapEveryDay.put("dayValue", dateDataList);
                                mapEveryDay.put("dateList", dateList);
                                mapEveryDay.put("lowestList", lowestPriceList);
                                mapEveryDay.put("highestList", highestPriceList);
                                goodsEntity.setPurEveryDay(mapEveryDay);

//                                ////////

                                Map<String, Object> mapStars = new HashMap<>();
                                mapStars.put("startDate", startDate);
                                mapStars.put("stopDate", stopDate);
                                mapStars.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                                mapStars.put("supplierId", supplierId);
                                mapStars.put("starsLevel", 5);
                                System.out.println("mapStarsmapStarsmapStars" + mapStars);
                                Integer starsFive = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
                                mapStars.put("starsLevel", 4);
                                Integer starsFour = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
                                mapStars.put("starsLevel", 3);
                                Integer starsThree = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
                                mapStars.put("starsLevel", 2);
                                Integer starsTwo = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
                                mapStars.put("starsLevel", 1);
                                Integer starsOne = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
                                Map<String, Object> mapStar = new HashMap<>();
                                mapStar.put("starsFive", starsFive);
                                mapStar.put("starsFour", starsFour);
                                mapStar.put("starsThree", starsThree);
                                mapStar.put("starsTwo", starsTwo);
                                mapStar.put("starsOne", starsOne);

                                Map<String, Object> mapResult = new HashMap<>();
                                // 添加null检查，防止数据库字段值为null
                                if (doutbleCost != null && doutbleCostV != null) {
                                    mapResult.put("totalCost", new BigDecimal(doutbleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                    mapResult.put("totalCostSubtotal", new BigDecimal(doutbleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                } else {
                                    mapResult.put("totalCost", "0.0");
                                    mapResult.put("totalCostSubtotal", "0.0");
                                }
                                mapResult.put("maxPrice", maxPrice);
                                mapResult.put("minPrice", minPrice);
                                mapResult.put("perPrice", perPrice);
                                mapResult.put("purCount", purCount);
                                mapResult.put("starsCount", mapStar);

                                goodsEntity.setGoodsData(mapResult);

                                map1.put("dayuStatus", null);
                                System.out.println("stoosososossososososos" + map1);

                                double goodsDoutbleCostV = 0;
                                double goodsDoutbleCost = 0;
                                double goodsDoutbleRestV = 0;
                                double goodsDoutbleRest = 0;
                                double goodsDoutbleLoss = 0;
                                double goodsDoutbleLossV = 0;
                                double goodsDoutbleWaste = 0;
                                double goodsDoutbleWasteV = 0;
                                double goodsDoutbleProduce = 0;
                                double goodsDoutbleProduceV = 0;
                                double goodsDoutbleReturnV = 0;
                                double goodsDoutbleReturn = 0;

                                Map<String, Object> mapDepStock = new HashMap<>();
                                mapDepStock.put("supplierId", supplierId);
                                mapDepStock.put("disId", gbDisId);
                                mapDepStock.put("startDate", startDate);
                                mapDepStock.put("stopDate", stopDate);
                                mapDepStock.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                                System.out.println("mapDepStockmapDepStock" + mapDepStock);
                                Integer integer2 = gbDepartmentGoodsStockService.queryGoodsStockCount(mapDepStock);
                                if (integer2 > 0) {
                                    goodsDoutbleCostV = gbDepartmentGoodsStockService.queryDepGoodsSubtotal(mapDepStock);
                                    goodsDoutbleCost = gbDepartmentGoodsStockService.queryDepStockWeightTotal(mapDepStock);

                                    goodsDoutbleRest = gbDepartmentGoodsStockService.queryDepGoodsRestWeightTotal(mapDepStock);
                                    goodsDoutbleRestV = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(mapDepStock);

                                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                                    System.out.println("pururururuururu" + mapDepStock);
                                    Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                                    if (integerProduce > 0) {
                                        goodsDoutbleProduceV = gbDepartmentStockReduceService.queryReduceProduceTotal(mapDepStock);
                                        goodsDoutbleProduce = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(mapDepStock);
                                    } else {
                                        goodsDoutbleProduceV = 0;
                                        goodsDoutbleProduce = 0;
                                    }
                                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                                    Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                                    if (integerLoss > 0) {
                                        goodsDoutbleLossV = gbDepartmentStockReduceService.queryReduceLossTotal(mapDepStock);
                                        goodsDoutbleLoss = gbDepartmentStockReduceService.queryReduceLossWeightTotal(mapDepStock);
                                    } else {
                                        goodsDoutbleLossV = 0;
                                        goodsDoutbleLoss = 0;
                                    }
                                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                                    Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                                    if (integerWaste > 0) {
                                        goodsDoutbleWasteV = gbDepartmentStockReduceService.queryReduceWasteTotal(mapDepStock);
                                        goodsDoutbleWaste = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(mapDepStock);
                                    } else {
                                        goodsDoutbleWasteV = 0;
                                        goodsDoutbleWaste = 0;
                                    }
                                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
                                    Integer integerReturn = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                                    if (integerReturn > 0) {
                                        goodsDoutbleReturnV = gbDepartmentStockReduceService.queryReduceReturnTotal(mapDepStock);
                                        goodsDoutbleReturn = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(mapDepStock);
                                    } else {
                                        goodsDoutbleReturnV = 0;
                                    }

                                    goodsEntity.setGoodsCostWeightTotalString(String.format("%.1f", goodsDoutbleCost));
                                    goodsEntity.setGoodsCostTotalString(String.format("%.1f", goodsDoutbleCostV));

                                    goodsEntity.setGoodsStockTotalString(String.format("%.1f", goodsDoutbleRestV));
                                    goodsEntity.setGoodsStockWeightTotalString(String.format("%.1f", goodsDoutbleRest));

                                    goodsEntity.setGoodsProduceWeightTotalString(String.format("%.1f", goodsDoutbleProduce));
                                    goodsEntity.setGoodsProduceTotalString(String.format("%.1f", goodsDoutbleProduceV));

                                    goodsEntity.setGoodsLossWeightTotalString(String.format("%.1f", goodsDoutbleLoss));
                                    goodsEntity.setGoodsLossTotalString(String.format("%.1f", goodsDoutbleLossV));

                                    goodsEntity.setGoodsWasteWeightTotalString(String.format("%.1f", goodsDoutbleWaste));
                                    goodsEntity.setGoodsWasteTotalString(String.format("%.1f", goodsDoutbleWasteV));

                                    goodsEntity.setGoodsReturnWeightTotalString(String.format("%.1f", goodsDoutbleReturn));
                                    goodsEntity.setGoodsReturnTotalString(String.format("%.1f", goodsDoutbleReturnV));

                                    BigDecimal divide = new BigDecimal(integer2).divide(new BigDecimal(howManyDaysInPeriod), 1, BigDecimal.ROUND_HALF_UP);

                                    goodsEntity.setGoodsAverageOrderTimes(divide.toString());
                                    int total = gbDepartmentGoodsStockService.queryGoodsStockStars(mapDepStock);
                                    System.out.println("totaltotaltotaltotal" + total);
                                    System.out.println("integer2integer2" + integer2);
                                    int i = total / integer2;
                                    int remainder = total % integer2;
                                    int gray = 0;
                                    if (remainder > 0) {
                                        remainder = 1;
                                    }
                                    gray = 5 - i - remainder;
                                    System.out.println("abccbcbcbcbcbcbcb" + i + "gray" + gray + "rem" + remainder);
                                    goodsEntity.setGoodsStarGreen(i);
                                    goodsEntity.setGoodsStarHalf(remainder);
                                    goodsEntity.setGoodsStarGray(gray);
                                    goodsEntity.setGoodsAverageStars(i);
                                    goodsEntity.setGoodsAverageStarsString(String.valueOf(i));
                                    goodsEntity.setGoodsPurTotalCount(integer2);

                                }
                                goodsList.add(goodsEntity);
                            }

                        }
                    }

                }
            }

        }

        map1.put("dayuStatus", null);
        System.out.println("stoosososossososososos" + map1);
        Integer integer1 = gbDepartmentGoodsStockService.queryGoodsStockCount(map1);
        if (integer1 > 0) {
            double depDoutbleCostV = 0;
            double depDoutbleRestV = 0;
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
            mapDepStock.put("supplierId", supplierId);
            mapDepStock.put("disId", gbDisId);
            mapDepStock.put("startDate", startDate);
            mapDepStock.put("stopDate", stopDate);
            System.out.println("mapDepStockmapDepStock" + mapDepStock);
            Integer integer2 = gbDepartmentGoodsStockService.queryGoodsStockCount(mapDepStock);
            if (integer2 > 0) {
                depDoutbleCostV = gbDepartmentGoodsStockService.queryDepGoodsSubtotal(mapDepStock);
                depDoutbleCost = gbDepartmentGoodsStockService.queryDepStockWeightTotal(mapDepStock);

                depDoutbleRestV = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(mapDepStock);

                mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                System.out.println("pururururuururu" + mapDepStock);
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
                }

                supplierEntity.setPurGoodsTotalString(String.format("%.1f", depDoutbleCostV));
                supplierEntity.setStockGoodsTotalString(String.format("%.1f", depDoutbleRestV));
                supplierEntity.setProduceGoodsTotalString(String.format("%.1f", depDoutbleProduceV));
                supplierEntity.setWasteGoodsTotalString(String.format("%.1f", depDoutbleWasteV));
                supplierEntity.setLossGoodsTotalString(String.format("%.1f", depDoutbleLossV));
                supplierEntity.setReturnGoodsTotalString(String.format("%.1f", depDoutbleReturnV));

            }

            int total = gbDepartmentGoodsStockService.queryGoodsStockStars(map1);
            System.out.println("abccbcbcbcbcbcbcb" + total);
            System.out.println("abccbcbcbcbcbcbcb" + integer2);
//            BigDecimal multiply = new BigDecimal(total).divide(new BigDecimal(integer2),1,BigDecimal.ROUND_HALF_UP);
            int i = total / integer2;
            int remainder = total % integer2;
            int gray = 5 - i;
            System.out.println("abccbcbcbcbcbcbcb" + i + "gray" + gray + "rem" + remainder);
            supplierEntity.setStarGreen(i);
            supplierEntity.setStarHalf(remainder);
            supplierEntity.setStarGray(gray);
            supplierEntity.setBillCount(goodsList.size());

        }

        //3

        Map<String, Object> map = new HashMap<>();
        if (type.equals("goods")) {
            map.put("arr", goodsList);
        } else if (type.equals("cost")) {
            map.put("arr", abcGoodsCost(goodsList));
        } else if (type.equals("costWeight")) {
            map.put("arr", abcGoodsCostWeight(goodsList));
        } else if (type.equals("produce")) {
            map.put("arr", abcGoodsProduce(goodsList));
        } else if (type.equals("losss")) {
            map.put("arr", abcGoodsLoss(goodsList));
        } else if (type.equals("waste")) {
            map.put("arr", abcGoodsWaste(goodsList));
        } else if (type.equals("return")) {
            map.put("arr", abcGoodsReturn(goodsList));
        } else if (type.equals("stars")) {
            map.put("arr", abcGoodsStars(goodsList));
        }
        map.put("item", supplierEntity);

        return R.ok().put("data", map);
    }


    /**
     * 获取供货商统计信息
     */
    @RequestMapping(value = "/getGbPurGoodsStatisticsForDis", method = RequestMethod.POST)
    @ResponseBody
    public R getGbPurGoodsStatisticsForDis(@RequestParam String supplierIds,
                                           @RequestParam String purUserIds,
                                           @RequestParam Integer disId,
                                           @RequestParam String startDate,
                                           @RequestParam String stopDate) {

        try {
            // 构建查询参数
            Map<String, Object> map = new HashMap<>();

            if (!purUserIds.equals("-1")) {
                String[] arrGb = purUserIds.split(",");
                List<String> idsGb = new ArrayList<>();
                for (String idGb : arrGb) {
                    idsGb.add(idGb);
                    if (idsGb.size() > 0) {
                        map.put("purUserIds", idsGb);
                    }
                }
            }
            if (!supplierIds.equals("-1")) {
                String[] arrGb = supplierIds.split(",");
                List<String> idsGb = new ArrayList<>();
                for (String idGb : arrGb) {
                    idsGb.add(idGb);
                    if (idsGb.size() > 0) {
                        map.put("supplierIds", idsGb);
                    }
                }
            }

            map.put("disId", disId);
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
            map.put("dayuStatus", 2);
            map.put("typeNotEqual", 9);  // 排除type=9的记录

            Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
            double purtotal = 0.0;
            if (integer > 0) {
                purtotal = gbDpgService.queryPurchaseGoodsSubTotal(map);
            }

            //采购员数据
            List<Map<String, Object>> purUserList = new ArrayList<>();
            System.out.println("puruuruuruuruuruuuruur" + map);
            map.put("disGoodsGrandId", null);
            List<GbDepartmentUserEntity> departmentUserEntities = gbDpgService.queryPurUserList(map);
            if (departmentUserEntities.size() > 0) {
                for (GbDepartmentUserEntity departmentUserEntity : departmentUserEntities) {
                    Map<String, Object> mapPurUser = new HashMap<>();
                    mapPurUser.put("purUserName", departmentUserEntity.getGbDuWxNickName());
                    map.put("purUserId", departmentUserEntity.getGbDepartmentUserId());
                    map.put("supplierId", null);
                    Double subTotal = 0.0;
                    Integer integerD = gbDpgService.queryGbPurchaseGoodsCount(map);
                    if (integerD > 0) {
                        subTotal = gbDpgService.queryPurchaseGoodsSubTotal(map);
                    }

                    mapPurUser.put("totalAmount", String.format("%.1f", subTotal));
                    purUserList.add(mapPurUser);
                }
            }


            //供货商数据
            List<Map<String, Object>> supplerList = new ArrayList<>();
            map.put("disGoodsGrandId", null);
            List<NxJrdhSupplierEntity> supplierEntities = gbDpgService.queryDisPurGoodsSupplierList(map);
            if (supplierEntities.size() > 0) {
                for (NxJrdhSupplierEntity supplierEntity : supplierEntities) {
                    Map<String, Object> mapPurUser = new HashMap<>();
                    mapPurUser.put("supplierName", supplierEntity.getNxJrdhsSupplierName());
                    map.put("supplierId", supplierEntity.getNxJrdhSupplierId());
                    map.put("purUserId", null);
                    Double subTotal = 0.0;

                    System.out.println("suppsmap" + map);
                    Integer integerS = gbDpgService.queryGbPurchaseGoodsCount(map);
                    if (integerS > 0) {
                        subTotal = gbDpgService.queryPurchaseGoodsSubTotal(map);
                    }
                    mapPurUser.put("totalAmount", String.format("%.1f", subTotal));
                    supplerList.add(mapPurUser);
                }
            }

            //按采购频率数据
            map.put("supplierId", null);
            System.out.println("tootititittiitqueryGbPurchaseGoodsTopTimes" + map);
            List<GbDistributerGoodsEntity> topGoods = new ArrayList<>();

            //按采购金额数据
            List<GbDistributerGoodsEntity> topGoodsWeight  = new ArrayList<>();
            double topSubtotal = 0.0;

            List<GbDistributerGoodsEntity> topGoodsPrice = new ArrayList<>();

            if(integer > 0){
                topGoods  = gbDpgService.queryGbPurchaseGoodsTopTimes(map);
                topGoodsWeight = gbDpgService.queryGbPurchaseGoodsTopSubtotal(map);
                topSubtotal = gbDpgService.queryGbPurchaseSubtotalTopSubtotal(map);
                //按价格浮动
                map.put("supplierId", null);
                System.out.println("tootititittiit" + map);
                topGoodsPrice = gbDpgService.queryGbPurchaseGoodsTopPriceFluctuation(map);
            }
            // 构建返回数据
            Map<String, Object> result = new HashMap<>();

            result.put("purTotal", String.format("%.1f", purtotal));
            result.put("purUserData", purUserList);
            result.put("supplierData", supplerList);
            result.put("topTimesGoods", topGoods);
            result.put("topSubtotalGoods", topGoodsWeight);
            result.put("topGoodsPrice", topGoodsPrice);
            BigDecimal bigDecimal = new BigDecimal(0);

            if(purtotal > 0){
                double v = topSubtotal / purtotal;
                bigDecimal = new BigDecimal(v).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            }

            result.put("topSubtotalGoodsPercent", bigDecimal);
            result.put("topSubtotalGoodsSubtotal", new BigDecimal(topSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP));
            return R.ok().put("data", result);

        } catch (Exception e) {
            e.printStackTrace();
            return R.error(-1, "没有数据");
        }
    }


    /**
     * 获取供货商统计信息
     */
    @RequestMapping(value = "/getGbPurGoodsStatistics", method = RequestMethod.POST)
    @ResponseBody
    public R getGbPurGoodsStatistics(@RequestParam Integer supplierId,
                                     @RequestParam Integer purUserId,
                                     @RequestParam Integer greatId,
                                     @RequestParam Integer disId,
                                     @RequestParam String startDate,
                                     @RequestParam String stopDate) {

        try {
            // 构建查询参数
            Map<String, Object> map = new HashMap<>();

            if (purUserId != -1) {
                map.put("purUserId", purUserId);
            } else if (supplierId != -1) {
                map.put("supplierId", supplierId);
            }

            map.put("disId", disId);
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
            map.put("dayuStatus", 2);
            map.put("typeNotEqual", 9);

            if (greatId != -1) {
                map.put("disGoodsGreatId", greatId);
            }
            // 获取采购统计
            System.out.println("哒哒哒哒哒" + map);
            Integer purCount = gbDpgService.queryGbPurchaseGoodsCount(map);

            if (purCount == 0) {
                return R.error(-1, "没有数据");

            }

            double totalCostV = gbDpgService.queryPurchaseGoodsSubTotal(map);

            Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);

            Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(map);
            double v = subTotal / totalCostV;
            BigDecimal bigDecimal = new BigDecimal(v).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

            List<GbDepartmentUserEntity> departmentUserEntities = gbDpgService.queryPurUserList(map);
            List<NxJrdhSupplierEntity> supplierEntities = gbDpgService.queryDisPurGoodsSupplierList(map);

            map.put("supplierBuy", -1);
            map.put("dayuStatus", 2);
            //自采数据
            Integer integerZicai = gbDpgService.queryGbPurchaseGoodsCount(map);
            BigDecimal batchBillTotalZicai = new BigDecimal(0);
            if (integerZicai > 0) {
                Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                batchBillTotalZicai = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
            }

            //订货数据
            map.put("supplierBuy", 1);
            map.put("dayuStatus", 2);
            map.put("batchDayuStatus", 2);
            Integer integerDh = gbDpgService.queryGbPurchaseGoodsCount(map);
            BigDecimal batchBillTotalDh = new BigDecimal(0);
            if (integerDh > 0) {
                Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                batchBillTotalDh = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
            }

            //kuun
            map.put("dayuStatus", null);
            Double aDouble = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(map);
            System.out.println("stockckkcmapGGGGGG" + map);
            map.put("restWeight", 0);
            Integer stockCount = gbDepartmentGoodsStockService.queryDisStockGoodsCount(map);


            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("dinghuo", batchBillTotalDh);
            result.put("dinghuoCount", integerDh);
            result.put("zicai", batchBillTotalZicai);
            result.put("zicaiCount", integerZicai);

            result.put("greatPercent", bigDecimal);
            result.put("greatTotal", String.format("%.1f", subTotal));
            result.put("stockTotal", String.format("%.1f", aDouble));
            result.put("totalString", String.format("%.1f", totalCostV));
            result.put("stockCount", stockCount);
            result.put("greatPurGoodsCount", integer);
            result.put("supplierArr", supplierEntities);
            result.put("depUserArr", departmentUserEntities);
            return R.ok().put("data", result);

        } catch (Exception e) {
            e.printStackTrace();
            return R.error("获取统计信息失败：" + e.getMessage());
        }
    }

    @RequestMapping(value = "/getGbPurGoodsStatisticsSeachDate", method = RequestMethod.POST)
    @ResponseBody
    public R getGbPurGoodsStatisticsSeachDate(@RequestParam Integer supplierId,
                                              @RequestParam Integer purUserId,
                                              @RequestParam Integer disId,
                                              @RequestParam String startDate,
                                              @RequestParam String stopDate,
                                              @RequestParam Integer greatId

    ) {

        try {
            // 构建查询参数
            Map<String, Object> map = new HashMap<>();

            if (purUserId != -1) {
                map.put("purUserId", purUserId);
            } else if (supplierId != -1) {
                map.put("supplierId", supplierId);
            }
            if (greatId != -1) {
                map.put("disGoodsGreatId", greatId);
            }
            map.put("disId", disId);
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
            map.put("dayuStatus", 2);
            map.put("typeNotEqual", 9);

            // 获取本期采购统计
            Integer purCount = gbDpgService.queryGbDisGoods(map);
            if (purCount == 0) {
                System.out.println("rriirr0000000");
                return R.error(-1, "没有数据");
            }

            Double allDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);

            List<GbDepartmentUserEntity> departmentUserEntities = gbDpgService.queryPurUserList(map);
            List<NxJrdhSupplierEntity> supplierEntities = gbDpgService.queryDisPurGoodsSupplierList(map);

            map.put("supplierBuy", -1);
            map.put("dayuStatus", 2);
            //自采数据
            Integer integerZicai = gbDpgService.queryGbPurchaseGoodsCount(map);
            BigDecimal batchBillTotalZicai = new BigDecimal(0);
            int zicaiGoodsCount = 0;
            if (integerZicai > 0) {
                Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                batchBillTotalZicai = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                System.out.println("ziciagouttt" + map);
                zicaiGoodsCount = gbDpgService.queryGbGoodsCount(map);
                System.out.println("自采商品数量查询结果: " + zicaiGoodsCount + " 个不同商品");
            }

            //订货数据
            map.put("supplierBuy", 1);
            map.put("dayuStatus", 2);
            map.put("batchDayuStatus", 2);
            Integer integerDh = gbDpgService.queryGbPurchaseGoodsCount(map);
            int dinghuoGoodsCount = 0;
            BigDecimal batchBillTotalDh = new BigDecimal(0);
            if (integerDh > 0) {
                Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                batchBillTotalDh = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                dinghuoGoodsCount = gbDpgService.queryGbGoodsCount(map);
            }


            //kuun
            map.put("dayuStatus", null);
            Double aDouble = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(map);
            map.put("restWeight", 0);
            Integer stockCount = gbDepartmentGoodsStockService.queryDisStockGoodsCount(map);
            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("allDouble", String.format("%.1f", allDouble));
            result.put("dinghuo", batchBillTotalDh);
            result.put("dinghuoCount", dinghuoGoodsCount);
            result.put("zicai", batchBillTotalZicai);
            result.put("zicaiCount", zicaiGoodsCount);
            result.put("stockTotal", String.format("%.1f", aDouble));
            result.put("stockCount", stockCount);
            result.put("supplierArr", supplierEntities);
            result.put("depUserArr", departmentUserEntities);
            return R.ok().put("data", result);

        } catch (Exception e) {
            e.printStackTrace();
            return R.error("获取统计信息失败：" + e.getMessage());
        }
    }


    /**
     * 分页获取商品列表（包含图表数据）
     */
    @RequestMapping(value = "/getGbPurGoodsList", method = RequestMethod.POST)
    @ResponseBody
    public R getGbPurGoodsList(@RequestParam Integer supplierId,
                               @RequestParam Integer purUserId,
                               @RequestParam Integer disId,
                               @RequestParam Integer greatId,
                               @RequestParam String startDate,
                               @RequestParam String stopDate,
                               @RequestParam(required = false) String type,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer limit) {

        try {
            // 构建查询参数
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("disId", disId);
            queryMap.put("startDate", startDate);
            queryMap.put("stopDate", stopDate);
            if (type.equals("money")) {
                queryMap.put("money", 1);
            }
            if (type.equals("times")) {
                queryMap.put("times", 1);
            }

            queryMap.put("typeNotEqual", 9);
            queryMap.put("dayuStatus", 2);
            queryMap.put("offset", (page - 1) * limit);
            queryMap.put("limit", limit);
            if (purUserId != -1) {
                queryMap.put("purUserId", purUserId);
            } else if (supplierId != -1) {
                queryMap.put("supplierId", supplierId);
            } else if (greatId != -1) {
                queryMap.put("disGoodsGreatId", greatId);
            }
            System.out.println("lisisisiisisis" + queryMap);
            // 获取商品总数
            Integer totalCount = gbDpgService.queryGbDisGoodsTreeCount(queryMap);
            Integer totalPages = (int) Math.ceil((double) totalCount / limit);

            // 获取商品列表
            System.out.println("查询商品map" + queryMap);
            List<GbDistributerGoodsEntity> goodsList = gbDpgService.queryDisTreeGoods(queryMap);
            System.out.println("resultt=totalCount=" + totalCount + "goolistsize" + goodsList.size());

            // 调试：专门检查黄瓜花商品的数据
            for (GbDistributerGoodsEntity goods : goodsList) {
                if ("黄瓜花".equals(goods.getGbDgGoodsName())) {
                    System.out.println("=== 黄瓜花商品调试信息 ===");
                    System.out.println("商品ID: " + goods.getGbDistributerGoodsId());
                    System.out.println("商品名称: " + goods.getGbDgGoodsName());
                    System.out.println("原始最低价: " + goods.getGbDgGoodsLowestPrice());
                    System.out.println("原始最高价: " + goods.getGbDgGoodsHighestPrice());
                    System.out.println("计算后最低价: " + goods.getGbDgGoodsLowestPrice());
                    System.out.println("计算后最高价: " + goods.getGbDgGoodsHighestPrice());
                    System.out.println("平均单价: " + goods.getGbDgGoodsAveragePrice());
                    System.out.println("销售总价: " + goods.getGbDgSellingPrice());
                    System.out.println("购买数量: " + goods.getGbDgSelfPrice());
                    System.out.println("购买次数: " + goods.getGoodsPurTotalCount());

                    // 查询黄瓜花商品的详细购买记录
                    Map<String, Object> debugQueryMap = new HashMap<>();
                    debugQueryMap.put("disGoodsId", goods.getGbDistributerGoodsId());
                    debugQueryMap.put("startDate", startDate);
                    debugQueryMap.put("stopDate", stopDate);

                    List<GbDistributerPurchaseGoodsEntity> purchaseRecords = gbDpgService.queryPurchaseGoodsWithDetailByParams(debugQueryMap);
                    System.out.println("黄瓜花商品购买记录数量: " + purchaseRecords.size());

                    for (GbDistributerPurchaseGoodsEntity record : purchaseRecords) {
                        System.out.println("购买记录ID: " + record.getGbDistributerPurchaseGoodsId());
                        System.out.println("购买价格: '" + record.getGbDpgBuyPrice() + "'");
                        System.out.println("购买小计: " + record.getGbDpgBuySubtotal());
                        System.out.println("购买数量: " + record.getGbDpgBuyQuantity());
                        System.out.println("状态: " + record.getGbDpgStatus());
                        System.out.println("完成日期: " + record.getGbDpgStockFinishDate());
                        System.out.println("---");
                    }

                    // 分析问题：原始最低价和最高价是空字符串，不是NULL
                    // COALESCE函数会返回第一个非NULL值，所以返回了空字符串而不是计算出的价格
                    System.out.println("问题分析：");
                    System.out.println("- 原始最低价和最高价是空字符串，不是NULL");
                    System.out.println("- COALESCE函数返回空字符串而不是计算出的MIN/MAX价格");
                    System.out.println("- 需要修改SQL查询逻辑");

                    // 测试：直接查询SQL结果
                    System.out.println("测试SQL查询结果：");
                    System.out.println("- 商品ID: " + goods.getGbDistributerGoodsId());
                    System.out.println("- 计算后最低价: '" + goods.getGbDgGoodsLowestPrice() + "'");
                    System.out.println("- 计算后最高价: '" + goods.getGbDgGoodsHighestPrice() + "'");

                    // 检查值是否为空
                    if (goods.getGbDgGoodsLowestPrice() == null || goods.getGbDgGoodsLowestPrice().isEmpty()) {
                        System.out.println("❌ 最低价仍然为空 - 需要重启应用加载新的SQL配置");
                    } else {
                        System.out.println("✅ 最低价: " + goods.getGbDgGoodsLowestPrice());
                    }

                    if (goods.getGbDgGoodsHighestPrice() == null || goods.getGbDgGoodsHighestPrice().isEmpty()) {
                        System.out.println("❌ 最高价仍然为空 - 需要重启应用加载新的SQL配置");
                    } else {
                        System.out.println("✅ 最高价: " + goods.getGbDgGoodsHighestPrice());
                    }

                    System.out.println("预期结果：最低价应该是6.0，最高价应该是8.0");
                    System.out.println("========================");
                }
            }

            // 为每个商品添加统计数据和图表数据
            for (GbDistributerGoodsEntity goods : goodsList) {

//                System.out.println("goodsnamememe" + goods.getGbDgGoodsName());
//                // 获取商品统计数据
//                Map<String, Object> goodsStatsMap = new HashMap<>();
//                goodsStatsMap.put("disGoodsId", goods.getGbDistributerGoodsId());
//                goodsStatsMap.put("startDate", startDate);
//                goodsStatsMap.put("stopDate", stopDate);
//                goodsStatsMap.put("dayuStatus", 2);
//                goodsStatsMap.put("buySubtotal", 0);
//                if (purUserId != -1) {
//                    goodsStatsMap.put("purUserId", purUserId);
//                } else if (supplierId != -1) {
//                    goodsStatsMap.put("supplierId", supplierId);
//                } else if (greatId != -1) {
//                    goodsStatsMap.put("disGoodsGreatId", greatId);
//                }

                // 获取图表数据
                Map<String, Object> chartData = getGoodsChartData(goods.getGbDistributerGoodsId(), startDate, stopDate, supplierId, purUserId, greatId);
                goods.setPurEveryDay(chartData);

                // 设置其他统计数据
//                setGoodsBusinessData(goods, goodsStatsMap);
            }

            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            result.put("goodsList", goodsList);

            return R.ok().put("data", result);

        } catch (Exception e) {
            e.printStackTrace();
            return R.error("获取商品列表失败：" + e.getMessage());
        }
    }


    @RequestMapping(value = "/getGbStockPurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getGbStockPurGoods(

            @RequestParam Integer disId,
            @RequestParam Integer greatId,
            @RequestParam String startDate,
            @RequestParam String stopDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {

        try {
            // 构建查询参数
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("disId", disId);
            queryMap.put("startDate", startDate);
            queryMap.put("stopDate", stopDate);
            queryMap.put("offset", (page - 1) * limit);
            queryMap.put("limit", limit);
            if (greatId != -1) {
                queryMap.put("disGoodsGreatId", greatId);
            }

            System.out.println("lisisisiisisisnnnn00000000sssssss" + queryMap);
            // 获取商品总数
            queryMap.put("restWeight", 0);
            Integer stockCount = gbDepartmentGoodsStockService.queryDisStockGoodsCount(queryMap);
            Double aDouble = gbDepartmentGoodsStockService.queryDepStockRestSubtotal(queryMap);

            System.out.println("totalCount: " + stockCount); // 新增日志
            Integer totalPages = (int) Math.ceil((double) stockCount / limit);

            // 获取商品列表
            System.out.println("查询商品map" + queryMap);
            System.out.println("开始查询库存商品列表..."); // 新增日志
            queryMap.put("orderByGoodsStockTotal", 1);
            List<GbDistributerGoodsEntity> goodsList = gbDepartmentGoodsStockService.queryDisGoodsStockByParams(queryMap);
            System.out.println("商品列表查询完成，数量: " + (goodsList != null ? goodsList.size() : "null")); // 新增日志


            // 构建返回数据s
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", stockCount);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            result.put("goodsList", goodsList);
            result.put("restSubtotal", String.format("%.1f", aDouble));
            System.out.println("reuslt" + result);

            return R.ok().put("data", result);

        } catch (Exception e) {
            System.out.println("查询商品列表异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("获取商品列表失败：" + e.getMessage());
        }
    }


    @RequestMapping(value = "/getGbPurGoodsListSearchDate", method = RequestMethod.POST)
    @ResponseBody
    public R getGbPurGoodsListSearchDate(

            @RequestParam Integer disId,
            @RequestParam Integer greatId,
            @RequestParam String startDate,
            @RequestParam String stopDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {

        try {
            // 构建查询参数
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("disId", disId);
            queryMap.put("startDate", startDate);
            queryMap.put("stopDate", stopDate);
            queryMap.put("typeNotEqual", 9);
            queryMap.put("dayuStatus", 2);
            queryMap.put("offset", (page - 1) * limit);
            queryMap.put("limit", limit);
            if (greatId != -1) {
                queryMap.put("disGoodsGreatId", greatId);
            }

            System.out.println("lisisisiisisisnnnn00000000aaa" + queryMap);
            // 获取商品总数
            Integer totalCount = gbDpgService.queryGbDisGoodsTreeCount(queryMap);
            System.out.println("totalCount: " + totalCount); // 新增日志
            Integer totalPages = (int) Math.ceil((double) totalCount / limit);

            // 获取商品列表
            queryMap.put("dateOrder", 1);
            System.out.println("查询商品map" + queryMap);
            System.out.println("开始查询商品列表..."); // 新增日志
            List<GbDistributerGoodsEntity> goodsList = gbDpgService.queryDisTreeGoodsWithPurList(queryMap);
            System.out.println("商品列表查询完成，数量: " + (goodsList != null ? goodsList.size() : "null")); // 新增日志


            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            result.put("goodsList", goodsList);
            System.out.println("reuslt" + result);

            return R.ok().put("data", result);

        } catch (Exception e) {
            System.out.println("查询商品列表异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("获取商品列表失败：" + e.getMessage());
        }
    }


    @RequestMapping(value = "/getGbPurGoodsDetailList", method = RequestMethod.POST)
    @ResponseBody
    public R getGbPurGoodsDetailList(

            @RequestParam Integer disGoodsId,
            @RequestParam String startDate,
            @RequestParam String stopDate) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        Map<String, Object> mapResult = new HashMap<>();

        try {
            // 构建查询参数
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("disGoodsId", disGoodsId);
            queryMap.put("startDate", startDate);
            queryMap.put("stopDate", stopDate);
            queryMap.put("dayuStatus", 2);
            queryMap.put("typeNotEqual", 9);


            // 获取商品列表
            System.out.println("查询商品map" + queryMap);
            System.out.println("开始查询商品列表..."); // 新增日志
            List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntityList = gbDpgService.queryPurchaseGoodsWithDetailByParams(queryMap);
//            System.out.println("商品列表查询完成，数量: " + (goodsList != null ? goodsList.size() : "null")); // 新增日志
            List<Map<String, Object>> purchaseDayValue = new ArrayList<>();

            if (howManyDaysInPeriod > 0) {
                // top
                for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                    Map<String, Object> mapEvery = new HashMap<>();

                    // dateList
                    String whichDay = "";
                    if (i == 0) {
                        whichDay = startDate;
                    } else {
                        whichDay = afterWhatDay(startDate, i);
                    }
                    Map<String, Object> mapDay = new HashMap<>();
                    mapDay.put("date", whichDay);
                    mapDay.put("disGoodsId", disGoodsId);
                    mapDay.put("typeNotEqual", 9);
                    Integer integer1 = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
                    mapEvery.put("date", whichDay);
                    if (integer1 > 0) {
                        Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);
                        mapEvery.put("purSubtotal", String.format("%.1f", subTotal));
                    } else {
                        mapEvery.put("purSubtotal", 0);
                    }
                    purchaseDayValue.add(mapEvery);
                }
            }

            mapResult.put("arr", purchaseGoodsEntityList);
            mapResult.put("itemList", purchaseDayValue);
            return R.ok().put("data", mapResult);

        } catch (Exception e) {
            System.out.println("查询商品列表异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("获取商品列表失败：" + e.getMessage());
        }
    }

    /**
     * 分页获取商品列表（包含图表数据）
     */
//    @RequestMapping(value = "/getGbPurGoodsListSearchDate", method = RequestMethod.POST)
//    @ResponseBody
//    public R getGbPurGoodsListSearchDate(
//
//            @RequestParam Integer disId,
//            @RequestParam String searchDate,
//            @RequestParam String startDate,
//            @RequestParam String stopDate,
//            @RequestParam(defaultValue = "1") Integer page,
//            @RequestParam(defaultValue = "10") Integer limit) {
//
//        try {
//            // 构建查询参数
//            Map<String, Object> queryMap = new HashMap<>();
//            queryMap.put("disId", disId);
//            queryMap.put("date", searchDate);
//            queryMap.put("buySubtotal", 0);
//            queryMap.put("dayuStatus", 2);
//
//
//            queryMap.put("offset", (page - 1) * limit);
//            queryMap.put("limit", limit);
//
//            System.out.println("lisisisiisisisaaaa" + queryMap);
//            // 获取商品总数
//            Integer totalCount = gbDpgService.queryGbDisGoodsTreeCount(queryMap);
//            Integer totalPages = (int) Math.ceil((double) totalCount / limit);
//
//            // 获取商品列表
//            System.out.println("查询商品map" + queryMap);
//            List<GbDistributerGoodsEntity> goodsList = gbDpgService.queryDisTreeGoods(queryMap);
//            System.out.println("resultt=totalCount=" + totalCount + "goolistsize" + goodsList.size());
////            // 为每个商品添加统计数据和图表数据
//            for (GbDistributerGoodsEntity goods : goodsList) {
//                System.out.println("goodsnamememe" + goods.getGbDgGoodsName());
//                // 获取商品统计数据
//                Map<String, Object> goodsStatsMap = new HashMap<>();
//                goodsStatsMap.put("disGoodsId", goods.getGbDistributerGoodsId());
//
//                goodsStatsMap.put("date", searchDate);
//                goodsStatsMap.put("dayuStatus", 2);
//                goodsStatsMap.put("buySubtotal", 0);
//
//                System.out.println("dattemapaapa" + goodsStatsMap);
//                List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntityList = gbDpgService.queryPurchaseGoodsWithDetailByParams(goodsStatsMap);
//                Map<String, Object> map = new HashMap<>();
//                map.put("purList", purchaseGoodsEntityList);
//                goods.setGoodsData(map);
//
//                goodsStatsMap.put("date", null);
//                goodsStatsMap.put("startDate", startDate);
//                goodsStatsMap.put("stopDate", stopDate);
//
//
//                List<Map<String, Object>> subtotalValue = new ArrayList<>();
//
//                Integer howManyDaysInPeriod = 0;
//                if (!startDate.equals(stopDate)) {
//                    howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
//                }
//                Map<String, Object> mapDay = new HashMap<>();
//                List<String> dateList = new ArrayList<>();
//
//                for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
//                    String whichDay = "";
//                    if (i == 0) {
//                        whichDay = startDate;
//                    } else {
//                        whichDay = afterWhatDay(startDate, i);
//                    }
//                    String substring = whichDay.substring(8, 10);
//                    dateList.add(substring);
//
//                    mapDay.put("date", whichDay);
//                    mapDay.put("disGoodsId", goods.getGbDistributerGoodsId());
//                    mapDay.put("buySubtotal", 0);
//
//                    System.out.println("eveyeedayyaaysupspspsliie1111sssssssss" + mapDay);
//
//                    int purCountDay = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
//                    if (purCountDay > 0) {
//                        Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);
//                        Double weightTotal = gbDpgService.queryPurchaseGoodsWeightTotal(mapDay);
//                        Map<String, Object> mapSub = new HashMap<>();
//                        mapSub.put("date", whichDay);
//                        mapSub.put("value", new BigDecimal(subTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        subtotalValue.add(mapSub);
//
//
//                    } else {
//                        Map<String, Object> mapSub = new HashMap<>();
//                        mapSub.put("date", whichDay);
//                        mapSub.put("value", 0);
//                        subtotalValue.add(mapSub);
//                    }
//
//                }
//
//
//                // 设置其他统计数据
//                setGoodsBusinessData(goods, goodsStatsMap);
//                Map<String, Object> result = new HashMap<>();
//                result.put("subtotalValueList", subtotalValue);
//                result.put("dateList", dateList);
//                goods.setPurEveryDay(result);
//            }
//
//            // 构建返回数据
//            Map<String, Object> result = new HashMap<>();
//            result.put("totalCount", totalCount);
//            result.put("totalPages", totalPages);
//            result.put("currentPage", page);
//            result.put("goodsList", goodsList);
//
//            return R.ok().put("data", result);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return R.error("获取商品列表失败：" + e.getMessage());
//        }
//    }
    private Map<String, Object> getGoodsChartData(Integer goodsId, String startDate, String stopDate, Integer supplierId, Integer purUserId, Integer greatId) {
        Map<String, Object> chartData = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<Map<String, Object>> dateDataList = new ArrayList<>();

        // 计算日期范围
        int howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);

        if (howManyDaysInPeriod > 0) {
            // 遍历每一天
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                System.out.println("homamnanamaama " + howManyDaysInPeriod);
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }

                // 1. 添加日期
                String substring = whichDay.substring(8, 10);
                System.out.println("homamnanamaama " + substring);

                dateList.add(substring);


                // 3. 每日采购数据
                Map<String, Object> mapDay = new HashMap<>();
                mapDay.put("date", whichDay);
                mapDay.put("disGoodsId", goodsId);
                if (purUserId != -1) {
                    mapDay.put("purUserId", purUserId);
                } else if (supplierId != -1) {
                    mapDay.put("supplierId", supplierId);
                } else if (greatId != -1) {
                    mapDay.put("disGoodsGreatId", greatId);
                }
                mapDay.put("typeNotEqual", 9);
                System.out.println("kakakanaknakaknakkanka" + mapDay);

                int purCountDay = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
                Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);

                if (purCountDay == 1) {
                    // 重新验证实际数据查询，确保条件一致
                    String price = gbDpgService.queryPurchaseGoodsPrice(mapDay);
                    String weight = gbDpgService.queryPurchaseGoodsWeight(mapDay);

                    // 如果查询结果为空，说明条件不一致，按0处理
                    if (price == null || weight == null) {
                        Map<String, Object> mapDayValue = new HashMap<>();
                        mapDayValue.put("dayPrice", "0.0");
                        mapDayValue.put("dayWeight", "0.0");
                        dateDataList.add(mapDayValue);
                    } else {
                        // 添加null检查，防止数据库字段值为null
                        if (price.trim().isEmpty()) {
                            price = "0";
                        }
                        if (weight.trim().isEmpty()) {
                            weight = "0";
                        }

                        Map<String, Object> mapDayValue = new HashMap<>();
                        mapDayValue.put("daySubtotal", new BigDecimal(subTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        mapDayValue.put("dayPrice", new BigDecimal(price).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        mapDayValue.put("dayWeight", new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        dateDataList.add(mapDayValue);
                    }
                } else if (purCountDay > 1) {
                    Double weightTotal = gbDpgService.queryPurchaseGoodsWeightTotal(mapDay);

                    double v1 = subTotal / weightTotal;
                    double v2 = weightTotal / purCountDay;

                    Map<String, Object> mapDayValue = new HashMap<>();
                    mapDayValue.put("daySubtotal", new BigDecimal(subTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    mapDayValue.put("dayPrice", new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    mapDayValue.put("dayWeight", new BigDecimal(v2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    dateDataList.add(mapDayValue);
                } else {
                    Map<String, Object> mapDayValue = new HashMap<>();
                    mapDayValue.put("dayPrice", 0);
                    mapDayValue.put("dayWeight", 0);
                    mapDayValue.put("daySubtotal", 0);
                    dateDataList.add(mapDayValue);
                }
            }
        } else {
            // 单日情况
            String substring = startDate.substring(8, 10);
            dateList.add(substring);

            Map<String, Object> mapDay = new HashMap<>();
            mapDay.put("date", startDate);
            mapDay.put("disGoodsId", goodsId);
            if (purUserId != -1) {
                mapDay.put("purUserId", purUserId);
            } else if (supplierId != -1) {
                mapDay.put("supplierId", supplierId);
            } else if (greatId != -1) {
                mapDay.put("disGoodsGreatId", greatId);
            }
            mapDay.put("typeNotEqual", 9);

            int purCountDay = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
            Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);

            if (purCountDay == 1) {
                // 重新验证实际数据查询，确保条件一致
                String price = gbDpgService.queryPurchaseGoodsPrice(mapDay);
                String weight = gbDpgService.queryPurchaseGoodsWeight(mapDay);

                // 如果查询结果为空，说明条件不一致，按0处理
                if (price == null || weight == null) {
                    Map<String, Object> mapDayValue = new HashMap<>();
                    mapDayValue.put("dayPrice", "0.0");
                    mapDayValue.put("dayWeight", "0.0");
                    mapDayValue.put("daySubtotal", "0.0");
                    dateDataList.add(mapDayValue);
                } else {
                    // 添加null检查，防止数据库字段值为null
                    if (price.trim().isEmpty()) {
                        price = "0";
                    }
                    if (weight.trim().isEmpty()) {
                        weight = "0";
                    }

                    Map<String, Object> mapDayValue = new HashMap<>();
                    mapDayValue.put("daySubtotal", new BigDecimal(subTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    mapDayValue.put("dayPrice", new BigDecimal(price).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    mapDayValue.put("dayWeight", new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    dateDataList.add(mapDayValue);
                }
            } else if (purCountDay > 1) {
                Double weightTotal = gbDpgService.queryPurchaseGoodsWeightTotal(mapDay);

                double v1 = subTotal / weightTotal;
                double v2 = weightTotal / purCountDay;

                Map<String, Object> mapDayValue = new HashMap<>();
                mapDayValue.put("daySubtotal", new BigDecimal(subTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                mapDayValue.put("dayPrice", new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                mapDayValue.put("dayWeight", new BigDecimal(v2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                dateDataList.add(mapDayValue);
            } else {
                Map<String, Object> mapDayValue = new HashMap<>();
                mapDayValue.put("daySubtotal", 0);
                mapDayValue.put("dayPrice", 0);
                mapDayValue.put("dayWeight", 0);
                dateDataList.add(mapDayValue);
            }


        }

        // 构建完整的图表数据，保持与原接口一致的结构
        chartData.put("dayValue", dateDataList);
        chartData.put("dateList", dateList);

        return chartData;
    }

    /**
     * 设置商品业务数据
     */
    private void setGoodsBusinessData(GbDistributerGoodsEntity goods, Map<String, Object> statsMap) {
        // 制作统计
        statsMap.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
        Integer produceCount = gbDepartmentStockReduceService.queryReduceTypeCount(statsMap);
        if (produceCount > 0) {
            double produceWeight = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(statsMap);
            double produceValue = gbDepartmentStockReduceService.queryReduceProduceTotal(statsMap);
            goods.setGoodsProduceWeightTotalString(String.format("%.1f", produceWeight));
            goods.setGoodsProduceTotalString(String.format("%.1f", produceValue));
        } else {
            goods.setGoodsProduceWeightTotalString("0.0");
            goods.setGoodsProduceTotalString("0.0");
        }

        // 损耗统计
        statsMap.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
        Integer lossCount = gbDepartmentStockReduceService.queryReduceTypeCount(statsMap);
        if (lossCount > 0) {
            double lossWeight = gbDepartmentStockReduceService.queryReduceLossWeightTotal(statsMap);
            double lossValue = gbDepartmentStockReduceService.queryReduceLossTotal(statsMap);
            goods.setGoodsLossWeightTotalString(String.format("%.1f", lossWeight));
            goods.setGoodsLossTotalString(String.format("%.1f", lossValue));
        } else {
            goods.setGoodsLossWeightTotalString("0.0");
            goods.setGoodsLossTotalString("0.0");
        }

        // 废弃统计
        statsMap.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        Integer wasteCount = gbDepartmentStockReduceService.queryReduceTypeCount(statsMap);
        if (wasteCount > 0) {
            double wasteWeight = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(statsMap);
            double wasteValue = gbDepartmentStockReduceService.queryReduceWasteTotal(statsMap);
            goods.setGoodsWasteWeightTotalString(String.format("%.1f", wasteWeight));
            goods.setGoodsWasteTotalString(String.format("%.1f", wasteValue));
        } else {
            goods.setGoodsWasteWeightTotalString("0.0");
            goods.setGoodsWasteTotalString("0.0");
        }

        // 退货统计
        statsMap.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
        Integer returnCount = gbDepartmentStockReduceService.queryReduceTypeCount(statsMap);
        if (returnCount > 0) {
            double returnWeight = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(statsMap);
            double returnValue = gbDepartmentStockReduceService.queryReduceReturnTotal(statsMap);
            goods.setGoodsReturnWeightTotalString(String.format("%.1f", returnWeight));
            goods.setGoodsReturnTotalString(String.format("%.1f", returnValue));
        } else {
            goods.setGoodsReturnWeightTotalString("0.0");
            goods.setGoodsReturnTotalString("0.0");
        }

        // 设置平均单价
        double costWeight = Double.parseDouble(goods.getGoodsCostWeightTotalString());
        double costValue = Double.parseDouble(goods.getGoodsCostTotalString());
        if (costWeight > 0) {
            double perPrice = costValue / costWeight;
            Map<String, Object> goodsData = new HashMap<>();
            goodsData.put("perPrice", String.format("%.1f", perPrice));
            goodsData.put("purCount", goods.getGoodsPurTotalCount());
            goods.setGoodsData(goodsData);
        }
    }


    @RequestMapping(value = "/getGbPurGoodsFenxi", method = RequestMethod.POST)
    @ResponseBody
    public R getGbPurGoodsFenxi(Integer disGoodsId, String startDate, String stopDate, Integer supplierId, Integer purUserId) {

        GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(disGoodsId);

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        Map<String, Object> map1 = new HashMap<>();
        map1.put("dayuStatus", 1);
        map1.put("disGoodsId", disGoodsId);
        map1.put("startDate", startDate);
        map1.put("stopDate", stopDate);
        map1.put("typeNotEqual", 9);
        System.out.println("wsupsspspspsp" + map1);
        Integer integerPur = gbDpgService.queryGbPurchaseGoodsCount(map1);
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map1);
        if (integer > 0 || integerPur > 0) {

            Map<String, Object> mapDay = new HashMap<>();
            List<String> dateList = new ArrayList<>();
            List<Map<String, Object>> spplierValueList = new ArrayList<>();
            List<Map<String, Object>> purUserValueList = new ArrayList<>();
            List<Map<String, Object>> purchaseDayValue = new ArrayList<>();

            List<String> produceDayValue = new ArrayList<>();
            List<String> wasteDayValue = new ArrayList<>();
            List<String> lossDayValue = new ArrayList<>();
            List<String> returnDayValue = new ArrayList<>();
            List<String> lowestPriceList = new ArrayList<>();
            List<String> highestPriceList = new ArrayList<>();

            List<String> searchProduceDayValue = new ArrayList<>();
            List<String> searchWasteDayValue = new ArrayList<>();
            List<String> searchLossDayValue = new ArrayList<>();
            List<String> searchReturnDayValue = new ArrayList<>();


            List<NxJrdhSupplierEntity> supplierEntities = new ArrayList<>();
            List<GbDepartmentUserEntity> purUserList = new ArrayList<>();


            double doubleCostWeight = 0;
            double doubleCostV = 0;
            double v = 0;
            String maxPrice = "0";
            String minPrice = "0";
            String perPrice = "0";
            int purCount = 0;
            double searchDoubleCostWeight = 0;
            double searchDoubleCostV = 0;
            double searchV = 0;
            String searchMaxPrice = "0";
            String searchMinPrice = "0";
            String searchPerPrice = "0";
            int searchPurCount = 0;
            String preWeight = "0";
            String preSubtotal = "0";
            Map<String, Object> searchItem = new HashMap<>();

            Map<String, Object> mapDaily = new HashMap<>();
            mapDaily.put("disGoodsId", disGoodsId);
            mapDaily.put("typeNotEqual", 9);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // 转换为 LocalDate
            LocalDate date = LocalDate.parse(startDate, formatter);
            // 计算前一天
            LocalDate prevDay = date.minusDays(1);
            mapDaily.put("date", prevDay);
            Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDaily);
            if (integer1 > 0) {
                Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailyRestWeight(mapDaily);
                Double aDoubleS = gbDepGoodsDailyService.queryDepGoodsDailyRestSubtotal(mapDaily);
                preWeight = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                preSubtotal = new BigDecimal(aDoubleS).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            }


            Map<String, Object> map = new HashMap<>();
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
            map.put("disGoodsId", disGoodsId);
            map.put("dayuStatus", 2);
            map.put("typeNotEqual", 9);
            System.out.println("whhahahhaha" + map);
            purCount = gbDpgService.queryGbPurchaseGoodsCount(map);
            searchItem.put("type", "none");
            if (purCount > 0) {
                System.out.println("subsososoososos" + map);
                doubleCostV = gbDpgService.queryPurchaseGoodsSubTotal(map);
                doubleCostWeight = gbDpgService.queryPurchaseGoodsWeightTotal(map);
                v = doubleCostV / doubleCostWeight;
                perPrice = new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                maxPrice = gbDpgService.queryPurGoodsMaxPrice(map);
                minPrice = gbDpgService.queryPurGoodsMinPrice(map);


                if (supplierId != -1 || purUserId != -1) {
                    if (supplierId != -1) {
                        searchItem.put("type", "supplier");
                        map.put("supplierId", supplierId);
                    } else {
                        searchItem.put("type", "purUser");
                        map.put("purUserId", purUserId);
                    }
                    System.out.println("searittmeee" + searchItem);
                    searchPurCount = gbDpgService.queryGbPurchaseGoodsCount(map);
                    if (searchPurCount > 0) {
                        System.out.println("subsososoososos" + map);
                        searchDoubleCostV = gbDpgService.queryPurchaseGoodsSubTotal(map);
                        searchDoubleCostWeight = gbDpgService.queryPurchaseGoodsWeightTotal(map);
                        searchV = searchDoubleCostV / searchDoubleCostWeight;
                        searchPerPrice = new BigDecimal(searchV).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                        searchMaxPrice = gbDpgService.queryPurGoodsMaxPrice(map);
                        searchMinPrice = gbDpgService.queryPurGoodsMinPrice(map);
                    }

                }

                supplierEntities = gbDpgService.queryDisPurGoodsSupplierList(map);
                purUserList = gbDpgService.queryPurUserList(map);

            }

            if (howManyDaysInPeriod > 0) {

                System.out.println("epsososososo==0");
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
                    mapDay.put("disGoodsId", disGoodsId);
                    mapDay.put("supplierId", null);
                    mapDay.put("purUserId", null);
                    mapDay.put("typeNotEqual", 9);
                    processDailyBusinessData(mapDay, produceDayValue, lossDayValue, wasteDayValue, returnDayValue);
                    processDailyBusinessDataSearch(mapDay, searchProduceDayValue, searchLossDayValue, searchWasteDayValue, searchReturnDayValue);
                    processDailyPurSubtotalData(mapDay, whichDay, purchaseDayValue);
                }

                // 处理供货商数据 - 修改后的逻辑
                if (supplierEntities.size() > 0) {

                    for (NxJrdhSupplierEntity supplierEntity : supplierEntities) {
                        Map<String, Object> supplierMap = new HashMap<>();
                        supplierMap.put("supplierId", supplierEntity.getNxJrdhSupplierId());
                        supplierMap.put("supplierName", supplierEntity.getNxJrdhsSupplierName());

                        // 为每个供货商创建独立的数据列表
                        List<String> thisSupplierPriceValue = new ArrayList<>();
                        List<String> thisSupplierWeightValue = new ArrayList<>();

                        // 重新计算这个供货商每天的数据
                        for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                            String whichDay = "";
                            if (i == 0) {
                                whichDay = startDate;
                            } else {
                                whichDay = afterWhatDay(startDate, i);
                            }

                            mapDay.put("date", whichDay);
                            mapDay.put("supplierId", supplierEntity.getNxJrdhSupplierId());
                            mapDay.put("disGoodsId", disGoodsId);
                            mapDay.put("typeNotEqual", 9);
                            System.out.println("eveyeedayyaaysupspspsliie" + mapDay);

                            // 调用方法获取这个供货商这一天的数据
                            processDailyPurchaseDataForSupplier(mapDay, thisSupplierPriceValue, thisSupplierWeightValue);
                        }

                        supplierMap.put("supplierPriceValue", thisSupplierPriceValue);
                        supplierMap.put("supplierWeightValue", thisSupplierWeightValue);

                        System.out.println("suplisiisisiisis" + spplierValueList.size());
                        spplierValueList.add(supplierMap);
                    }
                }
                if (purUserList.size() > 0) {
                    for (GbDepartmentUserEntity departmentUserEntity : purUserList) {
                        Map<String, Object> purUserMap = new HashMap<>();
                        purUserMap.put("purUserId", departmentUserEntity.getGbDepartmentUserId());
                        purUserMap.put("puUserName", departmentUserEntity.getGbDuWxNickName());

                        // 为每个供货商创建独立的数据列表
                        List<String> thisSupplierPriceValue = new ArrayList<>();
                        List<String> thisSupplierWeightValue = new ArrayList<>();

                        // 重新计算这个供货商每天的数据
                        for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                            String whichDay = "";
                            if (i == 0) {
                                whichDay = startDate;
                            } else {
                                whichDay = afterWhatDay(startDate, i);
                            }

                            mapDay.put("date", whichDay);
                            mapDay.put("supplierId", null);
                            mapDay.put("purUserId", departmentUserEntity.getGbDepartmentUserId());
                            mapDay.put("typeNotEqual", 9);
                            System.out.println("eveyeedayyaaysupspspsliie" + mapDay);

                            // 调用方法获取这个供货商这一天的数据
                            processDailyPurchaseDataForSupplier(mapDay, thisSupplierPriceValue, thisSupplierWeightValue);
                        }

                        purUserMap.put("supplierPriceValue", thisSupplierPriceValue);
                        purUserMap.put("supplierWeightValue", thisSupplierWeightValue);

                        System.out.println("suplisiisisiisis" + spplierValueList.size());
                        purUserValueList.add(purUserMap);
                    }
                }

            } else {

                String substring = startDate.substring(8, 10);
                dateList.add(substring);
                mapDay.put("date", startDate);
                mapDay.put("disGoodsId", disGoodsId);
                mapDay.put("typeNotEqual", 9);
                dateList.add(substring);

                System.out.println("wokkkkkkondaayayaayayayy" + mapDay);

                processDailyBusinessData(mapDay, produceDayValue, lossDayValue, wasteDayValue, returnDayValue);
                processDailyBusinessDataSearch(mapDay, searchProduceDayValue, searchLossDayValue, searchWasteDayValue, searchReturnDayValue);
                processDailyPurSubtotalData(mapDay, startDate, purchaseDayValue);
                // 处理单日供货商数据
                // 处理供货商数据 - 修改后的逻辑
                if (supplierEntities.size() > 0) {

                    for (NxJrdhSupplierEntity supplierEntity : supplierEntities) {
                        Map<String, Object> supplierMap = new HashMap<>();
                        supplierMap.put("supplierId", supplierEntity.getNxJrdhSupplierId());
                        supplierMap.put("supplierName", supplierEntity.getNxJrdhsSupplierName());

                        // 为每个供货商创建独立的数据列表
                        List<String> thisSupplierPriceValue = new ArrayList<>();
                        List<String> thisSupplierWeightValue = new ArrayList<>();

                        // 重新计算这个供货商每天的数据
                        for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                            String whichDay = "";
                            if (i == 0) {
                                whichDay = startDate;
                            } else {
                                whichDay = afterWhatDay(startDate, i);
                            }

                            mapDay.put("date", whichDay);
                            mapDay.put("supplierId", supplierEntity.getNxJrdhSupplierId());
                            mapDay.put("disGoodsId", disGoodsId);
                            mapDay.put("typeNotEqual", 9);
                            System.out.println("eveyeedayyaaysupspspsliie" + mapDay);

                            // 调用方法获取这个供货商这一天的数据
                            processDailyPurchaseDataForSupplier(mapDay, thisSupplierPriceValue, thisSupplierWeightValue);
                        }

                        supplierMap.put("supplierPriceValue", thisSupplierPriceValue);
                        supplierMap.put("supplierWeightValue", thisSupplierWeightValue);

                        System.out.println("suplisiisisiisis" + spplierValueList.size());
                        spplierValueList.add(supplierMap);
                    }
                }
                if (purUserList.size() > 0) {
                    for (GbDepartmentUserEntity departmentUserEntity : purUserList) {
                        Map<String, Object> purUserMap = new HashMap<>();
                        purUserMap.put("purUserId", departmentUserEntity.getGbDepartmentUserId());
                        purUserMap.put("puUserName", departmentUserEntity.getGbDuWxNickName());

                        // 为每个供货商创建独立的数据列表
                        List<String> thisSupplierPriceValue = new ArrayList<>();
                        List<String> thisSupplierWeightValue = new ArrayList<>();

                        // 重新计算这个供货商每天的数据
                        for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                            String whichDay = "";
                            if (i == 0) {
                                whichDay = startDate;
                            } else {
                                whichDay = afterWhatDay(startDate, i);
                            }

                            mapDay.put("date", whichDay);
                            mapDay.put("supplierId", null);
                            mapDay.put("purUserId", departmentUserEntity.getGbDepartmentUserId());
                            mapDay.put("typeNotEqual", 9);
                            System.out.println("eveyeedayyaaysupspspsliie" + mapDay);

                            // 调用方法获取这个供货商这一天的数据
                            processDailyPurchaseDataForSupplier(mapDay, thisSupplierPriceValue, thisSupplierWeightValue);
                        }

                        purUserMap.put("supplierPriceValue", thisSupplierPriceValue);
                        purUserMap.put("supplierWeightValue", thisSupplierWeightValue);

                        System.out.println("suplisiisisiisis" + spplierValueList.size());
                        purUserValueList.add(purUserMap);
                    }
                }
            }

            Map<String, Object> mapEveryDay = new HashMap<>();
            mapEveryDay.put("purchaseValue", purchaseDayValue);
            mapEveryDay.put("produceValue", produceDayValue);
            mapEveryDay.put("lossValue", lossDayValue);
            mapEveryDay.put("wasteValue", wasteDayValue);
            mapEveryDay.put("returnValue", returnDayValue);

            mapEveryDay.put("searchProduceValue", searchProduceDayValue);
            mapEveryDay.put("searchLossValue", searchLossDayValue);
            mapEveryDay.put("searchWasteValue", searchWasteDayValue);
            mapEveryDay.put("searchReturnValue", searchReturnDayValue);

            mapEveryDay.put("dateList", dateList);
            mapEveryDay.put("lowestList", lowestPriceList);
            mapEveryDay.put("highestList", highestPriceList);

            mapEveryDay.put("supplierListValue", spplierValueList);
            mapEveryDay.put("purUserListValue", purUserValueList);

            goodsEntity.setPurEveryDay(mapEveryDay);

            Map<String, Object> mapResult = new HashMap<>();
            mapResult.put("totalCost", new BigDecimal(doubleCostWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalCostSubtotal", new BigDecimal(doubleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("maxPrice", maxPrice);
            mapResult.put("minPrice", minPrice);
            mapResult.put("perPrice", perPrice);
            mapResult.put("purCount", purCount);
            mapResult.put("preWeight", preWeight);
            mapResult.put("preSubtotal", preSubtotal);

            mapResult.put("sTotalCost", new BigDecimal(searchDoubleCostWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("sTotalCostSubtotal", new BigDecimal(searchDoubleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("sMaxPrice", searchMaxPrice);
            mapResult.put("sMinPrice", searchMinPrice);
            mapResult.put("sPerPrice", searchPerPrice);
            mapResult.put("sPurCount", searchPurCount);

            mapResult.put("supplierList", supplierEntities);
            mapResult.put("purUserList", purUserList);
            mapResult.put("code", 0);

            goodsEntity.setGoodsData(mapResult);

            map1.put("dayuStatus", null);

            double goodsDoubleTotalWeight = 0;
            double goodsDoubleTotalSubtotal = 0;
            double goodsDoubleRestV = 0;
            double goodsDoubleRest = 0;
            double goodsDoubleLoss = 0;
            double goodsDoubleLossV = 0;
            double goodsDoubleWaste = 0;
            double goodsDoubleWasteV = 0;
            double goodsDoubleProduce = 0;
            double goodsDoubleProduceV = 0;
            double goodsDoubleReturnV = 0;
            double goodsDoubleReturn = 0;


            double searchGoodsDoubleTotalWeight = 0;
            double searchGoodsDoubleTotalSubtotal = 0;
            double searchGoodsDoubleRestV = 0;
            double searchGoodsDoubleRest = 0;
            double searchGoodsDoubleLoss = 0;
            double searchGoodsDoubleLossV = 0;
            double searchGoodsDoubleWaste = 0;
            double searchGoodsDoubleWasteV = 0;
            double searchGoodsDoubleProduce = 0;
            double searchGoodsDoubleProduceV = 0;
            double searchGoodsDoubleReturnV = 0;
            double searchGoodsDoubleReturn = 0;

            Map<String, Object> mapDepStock = new HashMap<>();
            Map<String, Object> mdapDepStockSearch = new HashMap<>();

            mapDepStock.put("disGoodsId", disGoodsId);
            mdapDepStockSearch.put("disGoodsId", disGoodsId);
            Integer integer3 = gbDepartmentGoodsStockService.queryGoodsStockCount(mapDepStock);
            if (integer3 > 0) {
                goodsDoubleRest = gbDepartmentGoodsStockService.queryDepStockRestWeightTotal(mapDepStock);
                goodsDoubleRestV = gbDepartmentGoodsStockService.queryDepStockRestSubtotal(mapDepStock);
                goodsEntity.setGoodsWeightTotalString(String.format("%.1f", goodsDoubleRest));
                goodsEntity.setGoodsStockTotalString(String.format("%.1f", goodsDoubleRestV));
                System.out.println("stoskckkckckc" + goodsDoubleRest + "vvvvv" + goodsDoubleRestV);

                //serchData
                if (supplierId != -1 || purUserId != -1) {
                    if (supplierId != -1) {
                        mdapDepStockSearch.put("supplierId", supplierId);
                    } else {
                        mdapDepStockSearch.put("purUserId", purUserId);
                    }
                    System.out.println("purrususuerr" + mdapDepStockSearch);
                    Integer integer3S = gbDepartmentGoodsStockService.queryGoodsStockCount(mdapDepStockSearch);
                    if (integer3S > 0) {
                        searchGoodsDoubleRest = gbDepartmentGoodsStockService.queryDepStockRestWeightTotal(mdapDepStockSearch);
                        searchGoodsDoubleRestV = gbDepartmentGoodsStockService.queryDepStockRestSubtotal(mdapDepStockSearch);
                    }

                }

            }


            //每日消耗图表数据
            mapDepStock.put("startDate", startDate);
            mapDepStock.put("stopDate", stopDate);
            mdapDepStockSearch.put("startDate", startDate);
            mdapDepStockSearch.put("stopDate", stopDate);
            Integer integer22 = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
            mapDepStock.put("dayuStatus", 2);
            mdapDepStockSearch.put("dayuStatus", 2);
            if (integer22 > 0) {
                mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                mdapDepStockSearch.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                if (integerProduce > 0) {
                    goodsDoubleProduceV = gbDepartmentStockReduceService.queryReduceProduceTotal(mapDepStock);
                    goodsDoubleProduce = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(mapDepStock);
                    goodsDoubleTotalWeight = goodsDoubleTotalWeight + goodsDoubleProduce;
                    goodsDoubleTotalSubtotal = goodsDoubleTotalSubtotal + goodsDoubleProduceV;

                    //serchData
                    if (supplierId != -1 || purUserId != -1) {
                        if (supplierId != -1) {
                            mdapDepStockSearch.put("supplierId", supplierId);
                        } else {
                            mdapDepStockSearch.put("purUserId", purUserId);
                        }
                        System.out.println("zazizzzoozo" + mdapDepStockSearch);
                        Integer integerProduceS = gbDepartmentStockReduceService.queryReduceTypeCount(mdapDepStockSearch);
                        if (integerProduceS > 0) {
                            searchGoodsDoubleProduceV = gbDepartmentStockReduceService.queryReduceProduceTotal(mdapDepStockSearch);
                            searchGoodsDoubleProduce = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(mdapDepStockSearch);
                            searchGoodsDoubleTotalWeight = searchGoodsDoubleTotalWeight + searchGoodsDoubleProduce;
                            searchGoodsDoubleTotalSubtotal = searchGoodsDoubleTotalSubtotal + searchGoodsDoubleProduceV;
                        }
                    }

                } else {
                    goodsDoubleProduceV = 0;
                    goodsDoubleProduce = 0;
                    searchGoodsDoubleProduceV = 0;
                    searchGoodsDoubleProduce = 0;
                }
                mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                mdapDepStockSearch.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                if (integerLoss > 0) {
                    goodsDoubleLossV = gbDepartmentStockReduceService.queryReduceLossTotal(mapDepStock);
                    goodsDoubleLoss = gbDepartmentStockReduceService.queryReduceLossWeightTotal(mapDepStock);
                    goodsDoubleTotalWeight = goodsDoubleTotalWeight + goodsDoubleLoss;
                    goodsDoubleTotalSubtotal = goodsDoubleTotalSubtotal + goodsDoubleLossV;
                    System.out.println("goodsDoubleCostgoodsDoubleCostllll" + goodsDoubleTotalWeight);

                    //serchData
                    if (supplierId != -1 || purUserId != -1) {
                        if (supplierId != -1) {
                            mdapDepStockSearch.put("supplierId", supplierId);
                        } else {
                            mdapDepStockSearch.put("purUserId", purUserId);
                        }
                        Integer integerLossS = gbDepartmentStockReduceService.queryReduceTypeCount(mdapDepStockSearch);
                        if (integerLossS > 0) {
                            searchGoodsDoubleLossV = gbDepartmentStockReduceService.queryReduceProduceTotal(mdapDepStockSearch);
                            searchGoodsDoubleLoss = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(mdapDepStockSearch);
                            searchGoodsDoubleTotalWeight = searchGoodsDoubleTotalWeight + searchGoodsDoubleLoss;
                            searchGoodsDoubleTotalSubtotal = searchGoodsDoubleTotalSubtotal + searchGoodsDoubleLossV;
                        }
                    }

                } else {
                    goodsDoubleLossV = 0;
                    goodsDoubleLoss = 0;
                    searchGoodsDoubleLossV = 0;
                    searchGoodsDoubleLoss = 0;
                }
                mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                mdapDepStockSearch.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                System.out.println("reedoddddmap" + mapDepStock);
                Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                if (integerWaste > 0) {
                    goodsDoubleWasteV = gbDepartmentStockReduceService.queryReduceWasteTotal(mapDepStock);
                    goodsDoubleWaste = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(mapDepStock);
                    goodsDoubleTotalWeight = goodsDoubleTotalWeight + goodsDoubleWaste;
                    goodsDoubleTotalSubtotal = goodsDoubleTotalSubtotal + goodsDoubleWasteV;

                    //serchData
                    if (supplierId != -1 || purUserId != -1) {
                        if (supplierId != -1) {
                            mdapDepStockSearch.put("supplierId", supplierId);
                        } else {
                            mdapDepStockSearch.put("purUserId", purUserId);
                        }
                        Integer integerLossS = gbDepartmentStockReduceService.queryReduceTypeCount(mdapDepStockSearch);
                        if (integerLossS > 0) {
                            searchGoodsDoubleWasteV = gbDepartmentStockReduceService.queryReduceProduceTotal(mdapDepStockSearch);
                            searchGoodsDoubleWaste = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(mdapDepStockSearch);
                            searchGoodsDoubleTotalWeight = searchGoodsDoubleTotalWeight + searchGoodsDoubleWaste;
                            searchGoodsDoubleTotalSubtotal = searchGoodsDoubleTotalSubtotal + searchGoodsDoubleWasteV;
                        }
                    }


                } else {
                    goodsDoubleWasteV = 0;
                    goodsDoubleWaste = 0;
                    searchGoodsDoubleWasteV = 0;
                    searchGoodsDoubleWaste = 0;

                }
                mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
                mdapDepStockSearch.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
                Integer integerReturn = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                if (integerReturn > 0) {
                    goodsDoubleReturnV = gbDepartmentStockReduceService.queryReduceReturnTotal(mapDepStock);
                    goodsDoubleReturn = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(mapDepStock);

                    //serchData
                    if (supplierId != -1 || purUserId != -1) {
                        if (supplierId != -1) {
                            mdapDepStockSearch.put("supplierId", supplierId);
                        } else {
                            mdapDepStockSearch.put("purUserId", purUserId);
                        }
                        Integer integerLossS = gbDepartmentStockReduceService.queryReduceTypeCount(mdapDepStockSearch);
                        if (integerLossS > 0) {
                            searchGoodsDoubleReturnV = gbDepartmentStockReduceService.queryReduceProduceTotal(mdapDepStockSearch);
                            searchGoodsDoubleReturn = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(mdapDepStockSearch);
                        }
                    }

                } else {
                    goodsDoubleReturnV = 0;
                }

                BigDecimal proPercent = new BigDecimal(0);
                BigDecimal lossPercent = new BigDecimal(0);
                BigDecimal wastePercent = new BigDecimal(0);
                BigDecimal retPercent = new BigDecimal(0);
                if (goodsDoubleProduce > 0) {
                    proPercent = new BigDecimal(goodsDoubleProduce).divide(new BigDecimal(goodsDoubleTotalWeight), 3, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    System.out.println("propenene" + goodsDoubleProduce + " ostweiicei" + goodsDoubleTotalWeight);
                    lossPercent = new BigDecimal(goodsDoubleLoss).divide(new BigDecimal(goodsDoubleTotalWeight), 3, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    wastePercent = new BigDecimal(goodsDoubleWaste).divide(new BigDecimal(goodsDoubleTotalWeight), 3, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    retPercent = new BigDecimal(goodsDoubleReturn).divide(new BigDecimal(goodsDoubleTotalWeight), 3, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP);
                }

                BigDecimal searchProPercent = new BigDecimal(0);
                BigDecimal searchLossPercent = new BigDecimal(0);
                BigDecimal searchWastePercent = new BigDecimal(0);
                BigDecimal searchRetPercent = new BigDecimal(0);
                if (searchGoodsDoubleTotalWeight > 0) {
                    searchProPercent = new BigDecimal(searchGoodsDoubleProduce).divide(new BigDecimal(searchGoodsDoubleTotalWeight), 3, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    searchLossPercent = new BigDecimal(searchGoodsDoubleLoss).divide(new BigDecimal(searchGoodsDoubleTotalWeight), 3, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    searchWastePercent = new BigDecimal(searchGoodsDoubleWaste).divide(new BigDecimal(searchGoodsDoubleTotalWeight), 3, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    searchRetPercent = new BigDecimal(searchGoodsDoubleReturn).divide(new BigDecimal(searchGoodsDoubleTotalWeight), 3, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP);

                }

                System.out.println("ggogoogogsaleepddid" + proPercent);
                goodsEntity.setGoodsProducePercent(proPercent.toString());
                goodsEntity.setGoodsLossPercent(lossPercent.toString());
                goodsEntity.setGoodsWastePercent(wastePercent.toString());
                goodsEntity.setGoodsReturnPercent(retPercent.toString());
                goodsEntity.setGoodsCostWeightTotalString(String.format("%.1f", goodsDoubleTotalWeight));
                goodsEntity.setGoodsCostTotalString(String.format("%.1f", goodsDoubleTotalSubtotal));
                goodsEntity.setGoodsProduceWeightTotalString(String.format("%.1f", goodsDoubleProduce));
                goodsEntity.setGoodsProduceTotalString(String.format("%.1f", goodsDoubleProduceV));
                goodsEntity.setGoodsLossWeightTotalString(String.format("%.1f", goodsDoubleLoss));
                goodsEntity.setGoodsLossTotalString(String.format("%.1f", goodsDoubleLossV));
                goodsEntity.setGoodsWasteWeightTotalString(String.format("%.1f", goodsDoubleWaste));
                goodsEntity.setGoodsWasteTotalString(String.format("%.1f", goodsDoubleWasteV));
                goodsEntity.setGoodsReturnWeightTotalString(String.format("%.1f", goodsDoubleReturn));
                goodsEntity.setGoodsReturnTotalString(String.format("%.1f", goodsDoubleReturnV));


                System.out.println("searpurororoortotaooa" + searchGoodsDoubleTotalSubtotal);
                searchItem.put("restSubtotal", String.format("%.1f", searchGoodsDoubleRestV));
                searchItem.put("restWeight", String.format("%.1f", searchGoodsDoubleRest));
                searchItem.put("produceSubtotal", String.format("%.1f", searchGoodsDoubleProduceV));
                searchItem.put("produceWeight", String.format("%.1f", searchGoodsDoubleProduce));
                searchItem.put("lossSubtotal", String.format("%.1f", searchGoodsDoubleLossV));
                searchItem.put("lossWeight", String.format("%.1f", searchGoodsDoubleLoss));
                searchItem.put("wasteSubtotal", String.format("%.1f", searchGoodsDoubleWasteV));
                searchItem.put("wasteWeight", String.format("%.1f", searchGoodsDoubleWaste));
                searchItem.put("returnSubtotal", String.format("%.1f", searchGoodsDoubleReturnV));
                searchItem.put("returnWeight", String.format("%.1f", searchGoodsDoubleReturn));

                searchItem.put("producePercent", String.format("%.1f", searchProPercent));
                searchItem.put("lossPercent", String.format("%.1f", searchLossPercent));
                searchItem.put("wastePercent", String.format("%.1f", searchWastePercent));
                searchItem.put("returnPercent", String.format("%.1f", searchRetPercent));
                searchItem.put("total", String.format("%.1f", searchGoodsDoubleTotalSubtotal));
                searchItem.put("totalWeight", String.format("%.1f", searchGoodsDoubleTotalWeight));
            }
            mapResult.put("searchItem", searchItem);
        } else {
            Map<String, Object> mapResult = new HashMap<>();

            mapResult.put("code", -1);
            goodsEntity.setGoodsData(mapResult);

            // 设置默认值，避免 null
            goodsEntity.setGoodsPurTotalWeight("0");
            goodsEntity.setGoodsPurTotalCount(0);
        }

        return R.ok().put("data", goodsEntity);
    }


    // 这个方法需要你实现，用于获取指定供货商在指定日期的采购数据
    private void processDailyPurchaseDataForSupplier(Map<String, Object> mapDay, List<String> supplierPriceValue, List<String> supplierWeightValue) {
        // 根据 mapDay 中的 date, disGoodsId, supplierId 查询该供货商在该日期的采购数据
        // 将价格和重量分别添加到 supplierPriceValue 和 supplierWeightValue 列表中
        int purCountDay = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
        if (purCountDay == 1) {
            String price = gbDpgService.queryPurchaseGoodsPrice(mapDay);
            String weight = gbDpgService.queryPurchaseGoodsWeight(mapDay);

            // 添加null检查，防止数据库字段值为null
            if (price == null || weight == null) {
                // 如果查询结果为空，按0处理
                supplierPriceValue.add("0.0");
                supplierWeightValue.add("0.0");
            } else {
                // 检查空字符串
                if (price.trim().isEmpty()) {
                    price = "0";
                }
                if (weight.trim().isEmpty()) {
                    weight = "0";
                }

                supplierPriceValue.add(new BigDecimal(price).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                supplierWeightValue.add(new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
        } else if (purCountDay > 1) {
            Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);
            Double weightTotal = gbDpgService.queryPurchaseGoodsWeightTotal(mapDay);

            // 添加null检查
            if (subTotal == null || weightTotal == null || weightTotal == 0) {
                supplierPriceValue.add("0.0");
                supplierWeightValue.add("0.0");
            } else {
                double v1 = subTotal / weightTotal;
                double v2 = weightTotal / purCountDay;

                supplierPriceValue.add(new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                supplierWeightValue.add(new BigDecimal(v2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
        } else {
            supplierPriceValue.add("0");
            supplierWeightValue.add("0");
        }
    }

    // 这个方法需要你实现，用于获取指定供货商在指定日期的采购数据
    private void processDailyPurchaseSubtotalForSupplier(Map<String, Object> mapDay, String date, List<Map<String, Object>> supplierSubtotalValue, List<Map<String, Object>> supplierWeightValue) {
        // 根据 mapDay 中的 date, disGoodsId, supplierId 查询该供货商在该日期的采购数据
        // 将价格和重量分别添加到 supplierPriceValue 和 supplierWeightValue 列表中
        int purCountDay = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
        if (purCountDay > 0) {
            Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);
            Double weightTotal = gbDpgService.queryPurchaseGoodsWeightTotal(mapDay);
            Map<String, Object> mapSub = new HashMap<>();
            mapSub.put("date", date);
            mapSub.put("value", new BigDecimal(subTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            supplierSubtotalValue.add(mapSub);

            Map<String, Object> mapWeight = new HashMap<>();
            mapWeight.put("date", date);
            mapWeight.put("value", new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            supplierWeightValue.add(mapWeight);
        } else {
            Map<String, Object> mapSub = new HashMap<>();
            mapSub.put("date", date);
            mapSub.put("value", 0);
            supplierSubtotalValue.add(mapSub);
            supplierWeightValue.add(mapSub);
        }
    }

    /**
     * 处理每日业务数据（生产、损耗、废弃、退货）
     */
    private void processDailyBusinessDataSearch(Map<String, Object> mapDay, List<String> produceDayValue,
                                                List<String> lossDayValue, List<String> wasteDayValue, List<String> returnDayValue) {
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDay);
        System.out.println("onda0101001010 " + mapDay);
        if (integer1 > 0) {
            Double gbDgdProduceWeight = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDay);
            Double gbDgdLossWeight = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDay);
            Double gbDgdWasteWeight = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDay);
            Double gbDgdReturnWeight = gbDepGoodsDailyService.queryDepGoodsDailyReturnWeight(mapDay);

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


    /**
     * 处理每日业务数据（生产、损耗、废弃、退货）
     */
    private void processDailyBusinessData(Map<String, Object> mapDay, List<String> produceDayValue,
                                          List<String> lossDayValue, List<String> wasteDayValue, List<String> returnDayValue) {
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDay);
        System.out.println("onda0101001010 " + mapDay);
        if (integer1 > 0) {
            Double gbDgdProduceWeight = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDay);
            Double gbDgdLossWeight = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDay);
            Double gbDgdWasteWeight = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDay);
            Double gbDgdReturnWeight = gbDepGoodsDailyService.queryDepGoodsDailyReturnWeight(mapDay);

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

    private void processDailyPurSubtotalData(Map<String, Object> mapDay, String whichDay, List<Map<String, Object>> purchaseDayValue) {
        Integer integer1 = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
        System.out.println("onda0101001010 " + mapDay);
        Map<String, Object> map = new HashMap<>();
        map.put("date", whichDay);
        if (integer1 > 0) {
            Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);
            map.put("purSubtotal", String.format("%.1f", subTotal));
        } else {
            map.put("purSubtotal", 0);
        }
        purchaseDayValue.add(map);
    }


    /**
     * 采购统计数据内部类
     */
    private static class PurchaseStats {
        private final double doubleCost;
        private final double doubleCostV;
        private final String maxPrice;
        private final String minPrice;
        private final String perPrice;
        private final int purCount;

        public PurchaseStats(double doubleCost, double doubleCostV, String maxPrice, String minPrice, String perPrice, int purCount) {
            this.doubleCost = doubleCost;
            this.doubleCostV = doubleCostV;
            this.maxPrice = maxPrice;
            this.minPrice = minPrice;
            this.perPrice = perPrice;
            this.purCount = purCount;
        }

        public double getDoubleCost() {
            return doubleCost;
        }

        public double getDoubleCostV() {
            return doubleCostV;
        }

        public String getMaxPrice() {
            return maxPrice;
        }

        public String getMinPrice() {
            return minPrice;
        }

        public String getPerPrice() {
            return perPrice;
        }

        public int getPurCount() {
            return purCount;
        }
    }


    @RequestMapping(value = "/disGetGbOrdersPurType", method = RequestMethod.POST)
    @ResponseBody
    public R disGetGbOrdersPurType(Integer gbDisId, String startDate, String stopDate, Integer type) {

        //购买采购商品 batchId == -1

        Map<String, Object> map = new HashMap<>();
        map.put("disId", gbDisId);
        map.put("dayuStatus", 1);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("payType", 0);
        map.put("purType", 2);
        System.out.println("zahsuishsissisiisisiisaaaaa" + map);
        Integer integer1 = gbDpgService.queryGbPurchaseGoodsCount(map);
        BigDecimal purchaseTotal = new BigDecimal(0);
        if (integer1 > 0) {
            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
            purchaseTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }


        map.put("payType", null);
//        map.put("purchaseType", 21);
        System.out.println("zahsuishsissisiisisiis21212121" + map);

        Integer integer2 = gbDpgService.queryGbPurchaseGoodsCount(map);
        BigDecimal orderTotal = new BigDecimal(0);
        if (integer2 > 0) {
            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
            orderTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            orderTotal = new BigDecimal(0);
        }

        //购买采购商品 batchId == -1
        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disId", gbDisId);
        mapG.put("startDate", startDate);
        mapG.put("stopDate", stopDate);
        mapG.put("purchaseType", 5);
        mapG.put("dayuStatus", 2);
        System.out.println("zashsuissiisiisiddddddmapGmapG" + mapG);
        Integer integer = gbDpgService.queryGbPurchaseGoodsCount(mapG);
        BigDecimal appTotal = new BigDecimal(0);
        if (integer > 0) {
            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(mapG);
            appTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        if (type == 2) {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("disId", gbDisId);
            map1.put("dayuStatus", 1);
            map1.put("purchaseType", 2);
            map1.put("startDate", startDate);
            map1.put("stopDate", stopDate);
            map1.put("batchId", -1);
            System.out.println("newnnwbbdabdd" + map1);
            List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDpgService.queryPurchaseGoodsWithDetailByParams(map1);
            Map<String, Object> map123 = new HashMap<>();
            map123.put("appTotal", appTotal);
            map123.put("purchaseTotal", purchaseTotal);
            map123.put("orderTotal", orderTotal);
            map123.put("arr", purchaseGoodsEntities);
            return R.ok().put("data", map123);
        } else if (type == 21) {
            Map<String, Object> mapB = new HashMap<>();
            mapB.put("disId", gbDisId);
            mapB.put("dayuStatus", 1);
            mapB.put("startDate", startDate);
            mapB.put("stopDate", stopDate);
            System.out.println("mapamapapaequalStatus" + mapB);
            List<GbDistributerPurchaseBatchEntity> batchEntities = gbDPBService.queryDisPurchaseBatch(mapB);
            Map<String, Object> map123 = new HashMap<>();
            map123.put("appTotal", appTotal);
            map123.put("purchaseTotal", purchaseTotal);
            map123.put("orderTotal", orderTotal);
            map123.put("arr", batchEntities);
            return R.ok().put("data", map123);
        } else {

            Map<String, Object> map4 = new HashMap<>();
            map4.put("disId", gbDisId);
            map4.put("dayuBuyStatus", 2);
            map4.put("nxDis", 1);
            map4.put("startDate", startDate);
            map4.put("stopDate", stopDate);
            System.out.println("map4444444=====" + map4);

            List<NxDistributerEntity> distributerEntities = gbDepartmentOrdersService.queryGbDepNxDistributerOrder(map4);
            if (distributerEntities.size() > 0) {
                for (int i = 0; i < distributerEntities.size(); i++) {
                    List<GbDistributerGoodsEntity> result = new ArrayList<>();
                    NxDistributerEntity distributerEntity = distributerEntities.get(i);
                    System.out.println("firstt" + distributerEntity.getNxDistributerName());
                    List<GbDistributerFatherGoodsEntity> gbFatherGoodsEntities = distributerEntity.getGbFatherGoodsEntities();
                    if (gbFatherGoodsEntities.size() > 0) {
                        for (int j = 0; j < gbFatherGoodsEntities.size(); j++) {

                            GbDistributerFatherGoodsEntity fatherGoodsEntity = gbFatherGoodsEntities.get(j);
                            System.out.println("goolevveee" + fatherGoodsEntity.getGbDfgFatherGoodsName() + "lve" + fatherGoodsEntity.getGbDfgFatherGoodsLevel());
                            if (fatherGoodsEntity.getFatherGoodsEntities().size() > 0) {
                                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = fatherGoodsEntity.getFatherGoodsEntities();
                                for (int m = 0; m < fatherGoodsEntities.size(); m++) {
                                    GbDistributerFatherGoodsEntity fatherGoodsEntity1 = fatherGoodsEntities.get(m);
                                    System.out.println("goolevveeefatherGoodsEntity1" + fatherGoodsEntity1.getGbDfgFatherGoodsName() + "lve" + fatherGoodsEntity1.getGbDfgFatherGoodsLevel());
                                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities1 = fatherGoodsEntity1.getFatherGoodsEntities();
                                    if (fatherGoodsEntities1.size() > 0) {
                                        for (int n = 0; n < fatherGoodsEntities1.size(); n++) {
                                            GbDistributerFatherGoodsEntity fatherGoodsEntity2 = fatherGoodsEntities1.get(n);
                                            List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = fatherGoodsEntity2.getGbDistributerGoodsEntities();
                                            result.addAll(gbDistributerGoodsEntities);

                                        }
                                    }

                                }
                            }
                            ;

                        }
                    }

                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("nxDisId", distributerEntity.getNxDistributerId());
                    mapS.put("startDate", startDate);
                    mapS.put("stopDate", stopDate);
                    System.out.println("maappasss" + mapS);

                    Integer integer3 = gbDpgService.queryGbPurchaseGoodsCount(mapS);
                    double total = 0.0;
                    if (integer3 > 0) {
                        total = gbDpgService.queryPurchaseGoodsSubTotal(mapS);
                    }
                    distributerEntity.setPurGoodsTotalString(String.format("%.2f", total));
                    distributerEntity.setGbDistributerGoods(result);
                }
            }

            Map<String, Object> map123 = new HashMap<>();
            map123.put("appTotal", appTotal);
            map123.put("purchaseTotal", purchaseTotal);
            map123.put("orderTotal", orderTotal);
            map123.put("arr", distributerEntities);
            return R.ok().put("data", map123);
        }

    }


    @RequestMapping(value = "/getGbPurGoods/{id}")
    @ResponseBody
    public R getGbPurGoods(@PathVariable Integer id) {

        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryPurGoodsWithOrders(id);
        return R.ok().put("data", purchaseGoodsEntity);
    }

    @RequestMapping(value = "/getDisGoodsPurList", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsPurList(Integer disGoodsId, String startDate, String stopDate, Integer depId) {

        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = new ArrayList<>();
        Double doutbleCost = null;
        Double doutbleCostV = null;
        double v = 0;
        String maxPrice = "0";
        String minPrice = "0";
        String perPrice = "0";
        int purCount = 0;

        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("disGoodsId", disGoodsId);
        map.put("dayuStatus", 2);
        map.put("typeNotEqual", 9);
        if (depId != -1) {
            map.put("purDepId", depId);
        }
        purCount = gbDpgService.queryGbPurchaseGoodsCount(map);
        if (purCount > 0) {
            System.out.println("caigoushushul" + map);
            doutbleCostV = gbDpgService.queryPurchaseGoodsSubTotal(map);
            doutbleCost = gbDpgService.queryPurchaseGoodsWeightTotal(map);
            v = doutbleCostV / doutbleCost;
            perPrice = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            maxPrice = gbDpgService.queryPurGoodsMaxPrice(map);
            minPrice = gbDpgService.queryPurGoodsMinPrice(map);
            System.out.println("whwhwhwhhwhw" + map);
            purchaseGoodsEntities = gbDpgService.queryOnlyPurGoods(map);
            if (purchaseGoodsEntities.size() > 0) {
                for (GbDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                    Integer purchaseGoodsId = purchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("purGoodsId", purchaseGoodsId);
                    System.out.println("mapssss00000" + mapS);
                    Double aDoubleRE = gbDepartmentGoodsStockService.queryDepStockRestWeightTotal(mapS);
                    Double aDoubleL = gbDepartmentGoodsStockService.queryDepStockLossWeightTotal(mapS);
                    Double aDoubleW = gbDepartmentGoodsStockService.queryDepGoodsWasteWeightTotal(mapS);
                    Double aDoubleR = gbDepartmentGoodsStockService.queryDepStockReturnWeightTotal(mapS);
                    Double aDoubleP = gbDepartmentGoodsStockService.queryDepStockProduceWeightTotal(mapS);
                    System.out.println("mapssss1111" + mapS);
                    purchaseGoodsEntity.setGbDpgStockRestWeight(String.format("%.1f", aDoubleRE));
                    purchaseGoodsEntity.setGbDpgStockProduceWeight(String.format("%.1f", aDoubleP));
                    purchaseGoodsEntity.setGbDpgStockLossWeight(String.format("%.1f", aDoubleL));
                    purchaseGoodsEntity.setGbDpgStockWasteWeight(String.format("%.1f", aDoubleW));
                    purchaseGoodsEntity.setGbDpgStockReturnWeightTotal(String.format("%.1f", aDoubleR));

                }
            }

        }

        Map<String, Object> mapResult = new HashMap<>();

        mapResult.put("totalCost", new BigDecimal(doutbleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapResult.put("totalCostSubtotal", new BigDecimal(doutbleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapResult.put("maxPrice", maxPrice);
        mapResult.put("minPrice", minPrice);
        mapResult.put("perPrice", perPrice);
        mapResult.put("purCount", purCount);
        mapResult.put("arr", purchaseGoodsEntities);


        System.out.println("mareereeeesullttl" + mapResult);

        return R.ok().put("data", mapResult);


    }

    @RequestMapping(value = "/getDisGoodsPurListJingjing", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsPurListJingjing(Integer disGoodsId, String startDate, String stopDate) {

        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = new ArrayList<>();
        Double doutbleCost = null;
        Double doutbleCostV = null;
        double v = 0;
        String maxPrice = "0";
        String minPrice = "0";
        String perPrice = "0";
        int purCount = 0;

        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("disGoodsId", disGoodsId);
        map.put("dayuStatus", 2);
        map.put("typeNotEqual", 9);
        purCount = gbDpgService.queryGbPurchaseGoodsCount(map);
        if (purCount > 0) {
            System.out.println("caigoushushul" + map);
            doutbleCostV = gbDpgService.queryPurchaseGoodsSubTotal(map);

            System.out.println("weiieemapap" + map);
            doutbleCost = gbDpgService.queryPurchaseGoodsWeightTotal(map);
            v = doutbleCostV / doutbleCost;
            perPrice = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            maxPrice = gbDpgService.queryPurGoodsMaxPrice(map);
            minPrice = gbDpgService.queryPurGoodsMinPrice(map);
            System.out.println("whwhwhwhhwhw" + map);
            purchaseGoodsEntities = gbDpgService.queryPurchaseGoodsWithDetailByParams(map);

            if (purchaseGoodsEntities.size() > 0) {
                for (GbDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                    Integer purchaseGoodsId = purchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("purGoodsId", purchaseGoodsId);
                    System.out.println("mapssss00000" + mapS);
                    Double aDoubleRE = gbDepartmentGoodsStockService.queryDepStockRestWeightTotal(mapS);
                    Double aDoubleL = gbDepartmentGoodsStockService.queryDepStockLossWeightTotal(mapS);
                    Double aDoubleW = gbDepartmentGoodsStockService.queryDepGoodsWasteWeightTotal(mapS);
                    Double aDoubleR = gbDepartmentGoodsStockService.queryDepStockReturnWeightTotal(mapS);
                    Double aDoubleP = gbDepartmentGoodsStockService.queryDepStockProduceWeightTotal(mapS);
                    System.out.println("mapssss1111" + mapS);
                    purchaseGoodsEntity.setGbDpgStockRestWeight(String.format("%.1f", aDoubleRE));
                    purchaseGoodsEntity.setGbDpgStockProduceWeight(String.format("%.1f", aDoubleP));
                    purchaseGoodsEntity.setGbDpgStockLossWeight(String.format("%.1f", aDoubleL));
                    purchaseGoodsEntity.setGbDpgStockWasteWeight(String.format("%.1f", aDoubleW));
                    purchaseGoodsEntity.setGbDpgStockReturnWeightTotal(String.format("%.1f", aDoubleR));
                    purchaseGoodsEntity.setGbDepartmentGoodsStockEntities(null);
                    purchaseGoodsEntity.setWasteDepartmentEntities(null);

                }
            }

            System.out.println("suppliermap" + map);
            List<NxJrdhSupplierEntity> supplierEntities = gbDpgService.queryDisPurGoodsSupplierList(map);

            Map<String, Object> mapResult = new HashMap<>();

            mapResult.put("totalCost", new BigDecimal(doutbleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalCostSubtotal", new BigDecimal(doutbleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("maxPrice", maxPrice);
            mapResult.put("minPrice", minPrice);
            mapResult.put("perPrice", perPrice);
            mapResult.put("purCount", purCount);
            mapResult.put("arr", purchaseGoodsEntities);
            mapResult.put("supplierList", supplierEntities);
            System.out.println("mareereeeesullttl" + mapResult);

            return R.ok().put("data", mapResult);

        } else {
            return R.error(-1, "无数据");
        }


    }

//    @RequestMapping(value = "/depGetToPrintPurGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R depGetToPrintPurGoods(Integer depId, Integer userId) {
//        Map<String, Object> map4 = new HashMap<>();
//        map4.put("purDepId", depId);
//        map4.put("buyStatus", 1);
//        map4.put("weightId", -1);
//        map4.put("purchaseType", 0);
//        System.out.println("map4444444" + map4);
//        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDpgService.queryDisPurchaseGoods(map4);
////
////        Map<String, Object> map = new HashMap<>();
////        map.put("depId", depId);
////        map.put("status", 1);
////        map.put("type", 2);
////        int countWeight = gbDistributerWeightTotalService.queryDepWeightCountByParams(map);
//
//        Map<String, Object> map3 = new HashMap<>();
//        map3.put("depId", depId);
//        map3.put("date", formatWhatDay(0));
//        map3.put("type", 2);
//        int count = gbDistributerWeightTotalService.queryDepWeightCountByParams(map3);
//        BigDecimal trade = new BigDecimal(count).add(new BigDecimal(1));
//        String name = "CGD";
//
//        String s = formatWhatDayString(0) + name + "-" + trade;
//
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("arr", fatherGoodsEntities);
////        map1.put("weightTotal", countWeight);
//        map1.put("tradeNo", s);
//
//        Map<String, Object> mapW = new HashMap<>();
//        mapW.put("depId", depId);
//        mapW.put("equalStatus", 0);
//        mapW.put("isSelf", -1);
//        List<GbDistributerWeightTotalEntity> weightTotalEntities = gbDisWeightTotalService.queryDepWeightListByParams(mapW);
//        map1.put("weightArr", weightTotalEntities);
//
//        return R.ok().put("data", map1);
//    }


//    @RequestMapping(value = "/getPurchasePurCash", method = RequestMethod.POST)
//    @ResponseBody
//    public R getPurchasePurCash(Integer userId, String date) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("userId", userId);
//        map.put("finishDate", date);
//        map.put("payType", 0);
//        Integer integer = gbDpgService.quu(map);
//
//        Map<String, Object> map1 = new HashMap<>();
//        if (goodsEntities.size() > 0) {
//            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//            map1.put("total", aDouble);
//            map1.put("arr", goodsEntities);
//
//        } else {
//            map1.put("total", 0);
//            map1.put("arr", new ArrayList<>());
//        }
//
//        return R.ok().put("data", map1);
//    }


    @RequestMapping(value = "/getDepPurchaserDateBill", method = RequestMethod.POST)
    @ResponseBody
    public R getDepPurchaserDateBill(Integer depId, String startDate, String stopDate, Integer type) {

        List<Map<String, Object>> itemList = new ArrayList<>();
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
                if (type != 2) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", whichDay);
                    map.put("purDepId", depId);
                    map.put("payType", type);
                    System.out.println("totototototoototot" + map);
                    Integer integer = gbDPBService.queryDisPurchaseBatchCount(map);
                    BigDecimal batchBillTotal = new BigDecimal(0);
                    if (integer > 0) {
                        Double aDouble = gbDPBService.queryPurchaserCashTotal(map);
                        batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    Map<String, Object> mapItem = new HashMap<>();
                    mapItem.put("day", whichDay);
                    mapItem.put("value", batchBillTotal);
                    itemList.add(mapItem);
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("purDepId", depId);
                    map.put("finishDate", whichDay);
                    map.put("batchId", -1);
                    map.put("dayuStatus", 1);
                    System.out.println("pauboodos" + map);
                    Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);

                    BigDecimal maileTotal = new BigDecimal(0);
                    if (integer > 0) {
                        Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                        maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    Map<String, Object> mapItem = new HashMap<>();
                    mapItem.put("day", whichDay);
                    mapItem.put("value", maileTotal);
                    itemList.add(mapItem);
                }

            }

        } else {
            if (type != 2) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", startDate);
                map.put("purDepId", depId);
                map.put("payType", type);
                System.out.println("totototototoototot" + map);
                Integer integer = gbDPBService.queryDisPurchaseBatchCount(map);
                BigDecimal batchBillTotal = new BigDecimal(0);
                if (integer > 0) {
                    Double aDouble = gbDPBService.queryPurchaserCashTotal(map);
                    batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("day", startDate);
                mapItem.put("value", batchBillTotal);
                itemList.add(mapItem);
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("purDepId", depId);
                map.put("finishDate", startDate);
                map.put("batchId", -1);
                map.put("dayuStatus", 1);
                System.out.println("pauboodos" + map);
                Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);

                BigDecimal maileTotal = new BigDecimal(0);
                if (integer > 0) {
                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                    maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("day", startDate);
                mapItem.put("value", maileTotal);
                itemList.add(mapItem);
            }

        }

        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", depId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("payType", 0);
        map.put("dayuStatus", 1);
        Integer integer1 = gbDPBService.queryDisPurchaseBatchCount(map);
        BigDecimal batchCashTotal = new BigDecimal(0);
        if (integer1 > 0) {
            Double aDouble = gbDPBService.queryPurchaserCashTotal(map);
            batchCashTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        map.put("payType", 1);
        Integer integer2 = gbDPBService.queryDisPurchaseBatchCount(map);
        BigDecimal batchBillTotal = new BigDecimal(0);
        if (integer2 > 0) {
            Double aDouble = gbDPBService.queryPurchaserCashTotal(map);
            batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            batchBillTotal = new BigDecimal(0);
        }

        //购买采购商品 batchId == -1
        Map<String, Object> mapG = new HashMap<>();
        mapG.put("purDepId", depId);
        mapG.put("startDate", startDate);
        mapG.put("stopDate", stopDate);
        mapG.put("payType", 0);
        mapG.put("batchId", -1);
        mapG.put("dayuStatus", 1);
        Integer integer = gbDpgService.queryGbPurchaseGoodsCount(mapG);
        BigDecimal maileTotal = new BigDecimal(0);
        if (integer > 0) {
            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(mapG);
            maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        Map<String, Object> map123 = new HashMap<>();
        map123.put("maileTotal", maileTotal);
        map123.put("batchCashTotal", batchCashTotal);
        map123.put("batchBillTotal", batchBillTotal);
        map123.put("arr", itemList);

        return R.ok().put("data", map123);
    }


    @RequestMapping(value = "/getDisPurchaserDateBill", method = RequestMethod.POST)
    @ResponseBody
    public R getDisPurchaserDateBill(Integer disId, String startDate, String stopDate, Integer type,
                                     String searchDepIds, String searchDepId) {

        List<Map<String, Object>> itemList = new ArrayList<>();
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
                if (type != 2) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", whichDay);
                    map.put("disId", disId);
                    if (!searchDepId.equals("-1")) {
                        map.put("purDepId", searchDepId);
                    } else {
                        if (!searchDepIds.equals("-1")) {
                            String[] arrGb = searchDepIds.split(",");
                            List<String> idsGb = new ArrayList<>();
                            for (String idGb : arrGb) {
                                idsGb.add(idGb);
                                if (idsGb.size() > 0) {
                                    map.put("purDepIds", idsGb);
                                }
                            }
                        }
                    }
                    map.put("payType", type);
                    Integer integer = gbDPBService.queryDisPurchaseBatchCount(map);
                    BigDecimal batchBillTotal = new BigDecimal(0);
                    if (integer > 0) {
                        Double aDouble = gbDPBService.queryPurchaserCashTotal(map);
                        batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    Map<String, Object> mapItem = new HashMap<>();
                    mapItem.put("day", whichDay);
                    mapItem.put("value", batchBillTotal);
                    itemList.add(mapItem);
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("disId", disId);
                    map.put("finishDate", whichDay);
                    map.put("batchId", -1);
                    map.put("dayuStatus", 1);
                    if (!searchDepId.equals("-1")) {
                        map.put("purDepId", searchDepId);
                    } else {
                        if (!searchDepIds.equals("-1")) {
                            String[] arrGb = searchDepIds.split(",");
                            List<String> idsGb = new ArrayList<>();
                            for (String idGb : arrGb) {
                                idsGb.add(idGb);
                                if (idsGb.size() > 0) {
                                    map.put("purDepIds", idsGb);
                                }
                            }
                        }
                    }

                    System.out.println("pauboodos" + map);
                    Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);

                    BigDecimal maileTotal = new BigDecimal(0);
                    if (integer > 0) {
                        Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                        maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    Map<String, Object> mapItem = new HashMap<>();
                    mapItem.put("day", whichDay);
                    mapItem.put("value", maileTotal);
                    itemList.add(mapItem);
                }

            }

        } else {
            if (type != 2) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", startDate);
                map.put("disId", disId);
                map.put("payType", type);
                if (!searchDepId.equals("-1")) {
                    map.put("purDepId", searchDepId);
                } else {
                    if (!searchDepIds.equals("-1")) {
                        String[] arrGb = searchDepIds.split(",");
                        List<String> idsGb = new ArrayList<>();
                        for (String idGb : arrGb) {
                            idsGb.add(idGb);
                            if (idsGb.size() > 0) {
                                map.put("purDepIds", idsGb);
                            }
                        }
                    }
                }

                System.out.println("totototototoototot" + map);
                Integer integer = gbDPBService.queryDisPurchaseBatchCount(map);
                BigDecimal batchBillTotal = new BigDecimal(0);
                if (integer > 0) {
                    Double aDouble = gbDPBService.queryPurchaserCashTotal(map);
                    batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("day", startDate);
                mapItem.put("value", batchBillTotal);
                itemList.add(mapItem);
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("disId", disId);
                map.put("finishDate", startDate);
                map.put("batchId", -1);
                map.put("dayuStatus", 1);
                if (!searchDepId.equals("-1")) {
                    map.put("purDepId", searchDepId);
                } else {
                    if (!searchDepIds.equals("-1")) {
                        String[] arrGb = searchDepIds.split(",");
                        List<String> idsGb = new ArrayList<>();
                        for (String idGb : arrGb) {
                            idsGb.add(idGb);
                            if (idsGb.size() > 0) {
                                map.put("purDepIds", idsGb);
                            }
                        }
                    }
                }

                System.out.println("pauboodos" + map);
                Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);

                BigDecimal maileTotal = new BigDecimal(0);
                if (integer > 0) {
                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                    maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("day", startDate);
                mapItem.put("value", maileTotal);
                itemList.add(mapItem);
            }

        }

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        if (!searchDepId.equals("-1")) {
            map.put("purDepId", searchDepId);
        } else {
            if (!searchDepIds.equals("-1")) {
                String[] arrGb = searchDepIds.split(",");
                List<String> idsGb = new ArrayList<>();
                for (String idGb : arrGb) {
                    idsGb.add(idGb);
                    if (idsGb.size() > 0) {
                        map.put("purDepIds", idsGb);
                    }
                }
            }
        }

        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("payType", 0);
        map.put("dayuStatus", 1);
        Integer integer1 = gbDPBService.queryDisPurchaseBatchCount(map);
        BigDecimal batchCashTotal = new BigDecimal(0);
        if (integer1 > 0) {
            Double aDouble = gbDPBService.queryPurchaserCashTotal(map);
            batchCashTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        map.put("payType", 1);
        Integer integer2 = gbDPBService.queryDisPurchaseBatchCount(map);
        BigDecimal batchBillTotal = new BigDecimal(0);
        if (integer2 > 0) {
            Double aDouble = gbDPBService.queryPurchaserCashTotal(map);
            batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            batchBillTotal = new BigDecimal(0);
        }

        //购买采购商品 batchId == -1
        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disId", disId);
        if (!searchDepId.equals("-1")) {
            mapG.put("purDepId", searchDepId);
        } else {
            if (!searchDepIds.equals("-1")) {
                String[] arrGb = searchDepIds.split(",");
                List<String> idsGb = new ArrayList<>();
                for (String idGb : arrGb) {
                    idsGb.add(idGb);
                    if (idsGb.size() > 0) {
                        mapG.put("purDepIds", idsGb);
                    }
                }
            }
        }

        mapG.put("startDate", startDate);
        mapG.put("stopDate", stopDate);
        mapG.put("payType", 0);
        mapG.put("batchId", -1);
        mapG.put("dayuStatus", 1);
        System.out.println("zashsuissiisiisi" + mapG);
        Integer integer = gbDpgService.queryGbPurchaseGoodsCount(mapG);
        BigDecimal maileTotal = new BigDecimal(0);
        if (integer > 0) {
            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(mapG);
            maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        Map<String, Object> map123 = new HashMap<>();
        map123.put("maileTotal", maileTotal);
        map123.put("batchCashTotal", batchCashTotal);
        map123.put("batchBillTotal", batchBillTotal);
        map123.put("arr", itemList);

        List<GbDepartmentEntity> deplist = new ArrayList<>();
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");

            for (String depId : arrGb) {
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(depId));
                Double aDoubleDep = 0.0;
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("startDate", startDate);
                mapDep.put("stopDate", stopDate);
                mapDep.put("payType", type);
                mapDep.put("purDepId", departmentEntity.getGbDepartmentId());
                if (type != 2) {
                    System.out.println("totototototoototot" + map);
                    Integer integerDep = gbDPBService.queryDisPurchaseBatchCount(mapDep);
                    if (integerDep > 0) {
                        aDoubleDep = gbDPBService.queryPurchaserCashTotal(mapDep);
                    }

                } else {
                    mapDep.put("payType", 0);
                    mapDep.put("batchId", -1);
                    mapDep.put("dayuStatus", 1);
                    Integer integerDepCash = gbDpgService.queryGbPurchaseGoodsCount(mapDep);
                    if (integerDepCash > 0) {
                        aDoubleDep = gbDpgService.queryPurchaseGoodsSubTotal(mapDep);
                    }
                }
                departmentEntity.setDepCostGoodsTotalString(new BigDecimal(aDoubleDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                deplist.add(departmentEntity);
            }
        }
        map123.put("depArr", deplist);

        return R.ok().put("data", map123);
    }




    @RequestMapping(value = "/disGetPurchaseDate", method = RequestMethod.POST)
    @ResponseBody
    public R disGetPurchaseDate(Integer disId, String startDate, String stopDate,
                                String purUserId, String supplierId) {


        Map<String, Object> mapH = new HashMap<>();
        mapH.put("disId", disId);
        mapH.put("startDate", startDate);
        mapH.put("stopDate", stopDate);
        System.out.println("mapapap" + mapH);
        Integer integer3 = gbDpgService.queryGbPurchaseGoodsCount(mapH);
        Integer integer2 = gbDepartmentStockReduceService.queryReduceTypeCount(mapH);
        System.out.println("inff2222 " + integer2);
        if (integer3 == 0 && integer2 == 0) {
            return R.error(-1, "没有数据");
        }

        List<Map<String, Object>> dayList = new ArrayList<>();
        Integer howManyDaysInPeriod = 0;

        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
            // dateList
            String whichDay = "";
            if (i == 0) {
                whichDay = startDate;
            } else {
                whichDay = afterWhatDay(startDate, i);
            }

            //dayMap
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("day", whichDay);

            Map<String, Object> map = new HashMap<>();
            map.put("date", whichDay);
            map.put("disId", disId);
            map.put("typeNotEqual", 9);
            map.put("dayuStatus", 2);
            if (!purUserId.equals("-1")) {
                map.put("purUserId", purUserId);
            }
            if (!supplierId.equals("-1")) {
                map.put("supplierId", supplierId);
            }

            //purData
            System.out.println("apapooaoaao" + map);
            Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
            BigDecimal batchBillTotal = new BigDecimal(0);
            if (integer > 0) {
                Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            dayMap.put("purTotal", batchBillTotal);

            map.put("supplierBuy", -1);
            map.put("dayuStatus", 2);
            if (!purUserId.equals("-1")) {
                map.put("purUserId", purUserId);
            }
            //自采数据
            System.out.println("orodidmdmmamam" + map);
            Integer integerZicai = gbDpgService.queryGbPurchaseGoodsCount(map);
            BigDecimal batchBillTotalZicai = new BigDecimal(0);
            if (integerZicai > 0) {
                Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                batchBillTotalZicai = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            dayMap.put("zicai", batchBillTotalZicai);

            //订货数据
            map.put("supplierBuy", 1);
            map.put("dayuStatus", 2);
            map.put("batchDayuStatus", 2);
            if (!supplierId.equals("-1")) {
                map.put("supplierId", supplierId);
                map.put("supplierBuy", null);
            }
            System.out.println("dinghuohuo" + map);
            Integer integerDh = gbDpgService.queryGbPurchaseGoodsCount(map);
            BigDecimal batchBillTotalDh = new BigDecimal(0);
            if (integerDh > 0) {
                Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                batchBillTotalDh = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            dayMap.put("dinghuo", batchBillTotalDh);

            Map<String, Object> mapStock = new HashMap<>();
            BigDecimal stockRest = new BigDecimal(0);
            mapStock.put("date", whichDay);
            mapStock.put("disId", disId);
            Integer integer1 = gbDepartmentGoodsStockService.queryGoodsStockCount(mapStock);
            if (integer1 > 0) {
                Double aDouble = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(mapStock);
                stockRest = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP);
            }
            dayMap.put("stockRest", stockRest);

            //支出数据
            double add = 0.0;
            Map<String, Object> mapDep = new HashMap<>();
            mapDep.put("disId", disId);
            mapDep.put("depType", getGbDepartmentTypeMendian());
            mapDep.put("date", whichDay);
            Double produceSubtotal = 0.0;
            Double wasteTotal = 0.0;
            Double lossTotal = 0.0;
            System.out.println("mapdiididididiid" + mapDep);
            Integer integerCost = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDep);
            if (integerCost > 0) {
                produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDep);
                wasteTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDep);
                lossTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDep);
                add = produceSubtotal + wasteTotal + lossTotal;

            }

            dayMap.put("costTotal", String.format("%.1f", add));
            dayMap.put("produceSubtotal", String.format("%.1f", produceSubtotal));
            dayMap.put("lossTotal", String.format("%.1f", lossTotal));
            dayMap.put("wasteTotal", String.format("%.1f", wasteTotal));

            dayList.add(dayMap);

        }

//        查询结果
        BigDecimal purchaseTotal = new BigDecimal(0);
        Double aDoubleAllPur = 0.0;
        List<GbDepartmentUserEntity> departmentUserEntities = new ArrayList<>();
        List<NxJrdhSupplierEntity> supplierEntities = new ArrayList<>();
        BigDecimal orderTotal = new BigDecimal(0);

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("typeNotEqual", 9);
        int count = gbDpgService.queryGbGoodsCount(map);

        if(count > 0){
            aDoubleAllPur = gbDpgService.queryPurchaseGoodsSubTotal(map);


            if (!purUserId.equals("-1")) {
                map.put("purUserId", purUserId);
            }
            if (!supplierId.equals("-1")) {
                map.put("supplierId", supplierId);
                map.put("supplierBuy", null);
            }
            map.put("supplierBuy", -1);
            map.put("dayuStatus", 2);
            Integer zicaiCount = gbDpgService.queryGbPurchaseGoodsCount(map);

            if (zicaiCount > 0) {
                Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                purchaseTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                departmentUserEntities = gbDpgService.queryPurUserList(map);
            }

            map.put("supplierBuy", 1);
            map.put("batchDayuStatus", 2);
            Integer dinghuoCount = gbDpgService.queryGbPurchaseGoodsCount(map);

            if (dinghuoCount > 0) {
                Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                orderTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                supplierEntities = gbDpgService.queryDisPurGoodsSupplierList(map);
            }

        }

        //所有支出
        Double costAll = 0.0;
        Integer costCount = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (costCount > 0) {
            costAll = gbDepartmentStockReduceService.queryReduceCostSubtotal(map);
        }

        //本期采购支出
        map.put("startDate",null);
        map.put("startPurchaseDate", startDate);
        Double costAllPerr = 0.0;
        Integer costPer = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (costPer > 0) {
            costAllPerr = gbDepartmentStockReduceService.queryReduceCostSubtotal(map);
        }

        Map<String, Object> map123 = new HashMap<>();
        map123.put("allTotal", new BigDecimal(aDoubleAllPur).setScale(1, BigDecimal.ROUND_HALF_UP));
        BigDecimal purchasePerDay = new BigDecimal(aDoubleAllPur).setScale(1, BigDecimal.ROUND_HALF_UP);
        BigDecimal bigDecimalCost = new BigDecimal(costAll).setScale(1, BigDecimal.ROUND_HALF_UP);
        if (howManyDaysInPeriod > 0) {
            purchasePerDay = new BigDecimal(aDoubleAllPur)
                    .divide(new BigDecimal(howManyDaysInPeriod), 1, BigDecimal.ROUND_HALF_UP);

            bigDecimalCost = new BigDecimal(costAll).divide(new BigDecimal(howManyDaysInPeriod), 1, BigDecimal.ROUND_HALF_UP);
        }

        System.out.println("purtoot" + aDoubleAllPur + "howmna" + howManyDaysInPeriod + " bidgdeeee" + purchasePerDay);
        map123.put("purchasePerDay", purchasePerDay);
        map123.put("orderTotal", orderTotal);
        map123.put("purchaseTotal", purchaseTotal);
        map123.put("costTotalPer", new BigDecimal(costAllPerr).setScale(1, BigDecimal.ROUND_HALF_UP));
        map123.put("costTotal", new BigDecimal(costAll).setScale(1, BigDecimal.ROUND_HALF_UP));
        map123.put("costPerDay", bigDecimalCost);
        map123.put("arr", dayList);
        map123.put("purUserList", departmentUserEntities);
        map123.put("supplierList", supplierEntities);
        return R.ok().put("data", map123);
    }


    @RequestMapping(value = "/disGetPurUserDate", method = RequestMethod.POST)
    @ResponseBody
    public R disGetPurUserDate(Integer disId, String startDate, String stopDate,
                                String purUserId) {


        Map<String, Object> mapH = new HashMap<>();
        mapH.put("disId", disId);
        mapH.put("startDate", startDate);
        mapH.put("stopDate", stopDate);
        mapH.put("purUserId", purUserId);
        System.out.println("mapapap" + mapH);
        Integer integer3 = gbDpgService.queryGbPurchaseGoodsCount(mapH);
        if (integer3 == 0) {
            return R.error(-1, "没有数据");
        }

        List<Map<String, Object>> dayList = new ArrayList<>();
        Integer howManyDaysInPeriod = 0;

        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }

        for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
            // dateList
            String whichDay = "";
            if (i == 0) {
                whichDay = startDate;
            } else {
                whichDay = afterWhatDay(startDate, i);
            }

            //dayMap
            Map<String, Object> dayMap = new HashMap<>();

            dayMap.put("day", whichDay);

            Map<String, Object> map = new HashMap<>();
            map.put("date", whichDay);
            map.put("disId", disId);
            map.put("typeNotEqual", 9);
            map.put("dayuStatus", 2);
            map.put("purUserId", purUserId);

            //自采数据
            System.out.println("orodidmdmmamam" + map);
            Integer integerZicai = gbDpgService.queryGbPurchaseGoodsCount(map);
            BigDecimal batchBillTotalZicai = new BigDecimal(0);
            if (integerZicai > 0) {
                Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                batchBillTotalZicai = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            dayMap.put("zicai", batchBillTotalZicai);

            Map<String, Object> mapStock = new HashMap<>();
            BigDecimal stockRest = new BigDecimal(0);
            mapStock.put("date", whichDay);
            mapStock.put("disId", disId);
            mapStock.put("purUserId", purUserId);
            Integer integer1 = gbDepartmentGoodsStockService.queryGoodsStockCount(mapStock);
            if (integer1 > 0) {
                Double aDouble = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(mapStock);
                stockRest = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP);
            }
            dayMap.put("stockRest", stockRest);

            if(integerZicai > 0){

                dayList.add(dayMap);
            }

        }


        return R.ok().put("data", dayList);
    }

    @RequestMapping(value = "/disGetPurchaseCata", method = RequestMethod.POST)
    @ResponseBody
    public R disGetPurchaseCata(Integer disId, String startDate, String stopDate,
                                String purUserId, String supplierId) {

        Map<String, Object> map123 = new HashMap<>();

        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("dayuStatus", 1);
        mapDep.put("typeNotEqual", 9);
        mapDep.put("startDate", startDate);
        mapDep.put("stopDate", stopDate);

        //采购总额
        System.out.println("purmapapapapa" + mapDep);
        Integer integerT = gbDpgService.queryGbPurchaseGoodsCount(mapDep);

        Integer integer3 = gbDepartmentStockReduceService.queryReduceTypeCount(mapDep);
        BigDecimal purchaseTotal = new BigDecimal(0);
        if (integerT == 0 && integer3 == 0) {
            return R.error(-1, "没有数据");
        } else {
            Integer integer = gbDpgService.queryGbPurchaseGoodsCount(mapDep);

            if(integer > 0){
                Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(mapDep);
                purchaseTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
            }

        }


        //支持总额
        Integer integer2 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDep);
        double totalCost = 0.0;
        if (integer2 > 0) {
            double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDep);
            double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDep);
            double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDep);
            totalCost = produceSubtotal + wasteSubtotal + lossSubtotal;
        }


        mapDep.put("startDate", startDate);
        mapDep.put("stopDate", stopDate);

        List<GbDepartmentUserEntity> departmentUserEntities = gbDpgService.queryPurUserList(mapDep);
        List<NxJrdhSupplierEntity> supplierEntities = new ArrayList<>();

        map123.put("purUserList", departmentUserEntities);
        map123.put("supplierList", supplierEntities);


        if (!purUserId.equals("-1")) {
            mapDep.put("purUserId", purUserId);
        }
        if (!supplierId.equals("-1")) {
            mapDep.put("supplierId", supplierId);
        }
        Integer count = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDep);
        if (count > 0) {
            System.out.println("========== 开始查询 TreeSet 数据 ==========");
            System.out.println("查询参数 mapDep: " + mapDep);
            
            TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoods = null;
            try {
                greatGrandGoods = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParamsTree(mapDep);
                
                System.out.println("查询成功，返回记录数: " + (greatGrandGoods != null ? greatGrandGoods.size() : 0));
                
                // 检查每个对象的ID是否为null
                if (greatGrandGoods != null) {
                    int index = 0;
                    for (GbDistributerFatherGoodsEntity entity : greatGrandGoods) {
                        index++;
                        Integer id = entity.getGbDistributerFatherGoodsId();
                        System.out.println("第" + index + "条记录 - ID: " + id + 
                                         ", Name: " + entity.getGbDfgFatherGoodsName() +
                                         ", Sort: " + entity.getGbDfgFatherGoodsSort() +
                                         ", Level: " + entity.getGbDfgFatherGoodsLevel());
                        
                        if (id == null) {
                            System.err.println("!!! 发现NULL ID !!! 第" + index + "条记录详情:");
                            System.err.println("  - Name: " + entity.getGbDfgFatherGoodsName());
                            System.err.println("  - FathersFatherId: " + entity.getGbDfgFathersFatherId());
                            System.err.println("  - Level: " + entity.getGbDfgFatherGoodsLevel());
                            System.err.println("  - Sort: " + entity.getGbDfgFatherGoodsSort());
                            System.err.println("  - Color: " + entity.getGbDfgFatherGoodsColor());
                            
                            // 检查子集合
                            if (entity.getFatherGoodsEntities() != null) {
                                System.err.println("  - 子分类数量: " + entity.getFatherGoodsEntities().size());
                                for (GbDistributerFatherGoodsEntity child : entity.getFatherGoodsEntities()) {
                                    System.err.println("    子分类 ID: " + child.getGbDistributerFatherGoodsId() + 
                                                     ", Name: " + child.getGbDfgFatherGoodsName());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("========== TreeSet 查询或遍历出错 ==========");
                System.err.println("错误信息: " + e.getMessage());
                e.printStackTrace();
                throw e; // 重新抛出异常
            }
            
            System.out.println("========== TreeSet 数据检查完成 ==========");
            
            for (GbDistributerFatherGoodsEntity greatEntity : greatGrandGoods) {
                //dayMap
                Map<String, Object> cataMap = new HashMap<>();

                mapDep.put("supplierBuy", null);
                mapDep.put("batchDayuStatus", null);
                mapDep.put("dayuStatus", -1);
                mapDep.put("disGoodsGreatId", greatEntity.getGbDistributerFatherGoodsId());
                System.out.println("mazzzuissisis" + mapDep);
                Integer integer = gbDpgService.queryGbPurchaseGoodsCount(mapDep);
                BigDecimal batchBillTotal = new BigDecimal(0);
                if (integer > 0) {
                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(mapDep);
                    batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                BigDecimal divide = new BigDecimal(0);
                if((purchaseTotal.compareTo(new BigDecimal(0) ) == 1)){
                    divide = batchBillTotal.divide(purchaseTotal, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                }

                cataMap.put("greatPurTotal", batchBillTotal);
                cataMap.put("greatPercent", divide);
                cataMap.put("purTotal", purchaseTotal);

                mapDep.put("supplierBuy", -1);
                mapDep.put("dayuStatus", 2);
                //自采数据
                Integer integerZicai = gbDpgService.queryGbPurchaseGoodsCount(mapDep);
                BigDecimal batchBillTotalZicai = new BigDecimal(0);
                if (integerZicai > 0) {
                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(mapDep);
                    batchBillTotalZicai = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                cataMap.put("zicai", batchBillTotalZicai);

                //订货数据
                mapDep.put("supplierBuy", 1);
                mapDep.put("dayuStatus", 2);
                mapDep.put("batchDayuStatus", 2);

                Integer integerDh = gbDpgService.queryGbPurchaseGoodsCount(mapDep);
                BigDecimal batchBillTotalDh = new BigDecimal(0);
                if (integerDh > 0) {
                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(mapDep);
                    batchBillTotalDh = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                cataMap.put("dinghuo", batchBillTotalDh);


                //支出数据
                double add = 0.0;
                Map<String, Object> mapDepCost = new HashMap<>();
                mapDepCost.put("startDate", startDate);
                mapDepCost.put("stopDate", stopDate);
                mapDepCost.put("disId", disId);
                mapDepCost.put("depType", getGbDepartmentTypeMendian());
                mapDepCost.put("dayuStatus", -1);
                mapDepCost.put("disGoodsGreatId", greatEntity.getGbDistributerFatherGoodsId());
                Double produceSubtotal = 0.0;
                Double wasteTotal = 0.0;
                Double lossTotal = 0.0;
                System.out.println("cosssoososososointeger4integer4" + mapDepCost);
                Integer integer4 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDepCost);
                if (integer4 > 0) {
                    produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapDepCost);

                    System.out.println("cossisisis" + produceSubtotal);
                    wasteTotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapDepCost);
                    lossTotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(mapDepCost);
                    add = produceSubtotal + wasteTotal + lossTotal;

                }


                //所有库存

                Double allDouble = 0.0;
                Integer integer1 = gbDepartmentGoodsStockService.queryGoodsStockCount(mapDepCost);
                if (integer1 > 0) {
                    allDouble = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(mapDepCost);
                }

                //本期库存
                Map<String, Object> mapDepCostThis = new HashMap<>();
                mapDepCostThis.put("disId", disId);
                mapDepCostThis.put("depType", getGbDepartmentTypeMendian());
                mapDepCostThis.put("dayuStatus", -1);
                mapDepCostThis.put("startDate", startDate);
                mapDepCostThis.put("stopDate", stopDate);
                mapDepCostThis.put("disGoodsGreatId", greatEntity.getGbDistributerFatherGoodsId());
                Double perStockDouble = 0.0;

                System.out.println("thisSttockckckckckkc" + mapDepCostThis);
                Integer integerPer = gbDepartmentGoodsStockService.queryGoodsStockCount(mapDepCostThis);
                if (integerPer > 0) {
                    perStockDouble = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(mapDepCostThis);
                }

                //上期库存
                Double lastStockDouble = 0.0;
                Map<String, Object> mapDepCostLast = new HashMap<>();
                mapDepCostLast.put("disId", disId);
                mapDepCostLast.put("depType", getGbDepartmentTypeMendian());
                mapDepCostLast.put("dayuStatus", -1);
                mapDepCostLast.put("stopDate", startDate);
                mapDepCostLast.put("disGoodsGreatId", greatEntity.getGbDistributerFatherGoodsId());
                System.out.println("lastStockckckk" + mapDepCostLast);
                Integer integerPerLast = gbDepartmentGoodsStockService.queryGoodsStockCount(mapDepCostLast);
                if (integerPerLast > 0) {
                    lastStockDouble = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(mapDepCostLast);
                }


//                cataMap.put("totalStock", String.format("%.1f", allDouble));
//                cataMap.put("lastStock1", String.format("%.1f", allDouble - perStockDouble));
                cataMap.put("lastStock", String.format("%.1f", lastStockDouble));
                cataMap.put("perStock", String.format("%.1f", perStockDouble));
                cataMap.put("costTotal", String.format("%.1f", add));
                cataMap.put("costAllTotal", String.format("%.1f", totalCost));
                double v = 0.0;
                if (totalCost > 0) {
                    v = add / totalCost * 100;
                }
                cataMap.put("costPercent", String.format("%.1f", v));
                cataMap.put("produceSubtotal", String.format("%.1f", produceSubtotal));
                cataMap.put("lossTotal", String.format("%.1f", lossTotal));
                cataMap.put("wasteTotal", String.format("%.1f", wasteTotal));

                greatEntity.setDailyData(cataMap);

            }
            map123.put("arr", greatGrandGoods);

        }


        return R.ok().put("data", map123);
    }

//
//    @RequestMapping(value = "/getDisPurchaserDateJingjing", method = RequestMethod.POST)
//    @ResponseBody
//    public R getDisPurchaserDateJingjing(Integer disId, String startDate, String stopDate, Integer type,
//                                         String purUserId, String supplierId) {
//
//        List<Map<String, Object>> itemList = new ArrayList<>();
//        Integer howManyDaysInPeriod = 0;
//        if (!startDate.equals(stopDate)) {
//            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
//        }
//        if (howManyDaysInPeriod > 0) {
//
//            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
//                // dateList
//                String whichDay = "";
//                if (i == 0) {
//                    whichDay = startDate;
//                } else {
//                    whichDay = afterWhatDay(startDate, i);
//                }
//                if (type == -1) {
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("date", whichDay);
//                    map.put("disId", disId);
//                    map.put("dayuStatus", 2);
//                    if (!purUserId.equals("-1")) {
//                        map.put("purUserId", purUserId);
//                    }
//                    if (!supplierId.equals("-1")) {
//                        map.put("supplierId", supplierId);
//                    }
//
//                    System.out.println("pauboodosnnxnxnnxnn22222mmmmmmm" + map);
//
//                    Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
//                    BigDecimal batchBillTotal = new BigDecimal(0);
//                    if (integer > 0) {
//                        Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                        batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    }
//                    Map<String, Object> mapItem = new HashMap<>();
//                    mapItem.put("day", whichDay);
//                    mapItem.put("value", batchBillTotal);
//
//                    //
//                    map.put("supplierBuy", -1);
//                    map.put("dayuStatus", 2);
//                    if (!purUserId.equals("-1")) {
//                        map.put("purUserId", purUserId);
//                    }
//
//                    System.out.println("pauboodosnnxnxnnxnn22222mmmmmmm" + map);
//
//                    Integer integerZicai = gbDpgService.queryGbPurchaseGoodsCount(map);
//                    BigDecimal batchBillTotalZicai = new BigDecimal(0);
//                    if (integerZicai > 0) {
//                        Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                        batchBillTotalZicai = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    }
//
//                    mapItem.put("zicai", batchBillTotalZicai);
//
//
//                    //
//                    map.put("supplierBuy", 1);
//                    map.put("dayuStatus", 2);
//                    map.put("batchDayuStatus", 2);
//                    if (!supplierId.equals("-1")) {
//                        map.put("supplierId", supplierId);
//                        map.put("supplierBuy", null);
//                    }
//                    System.out.println("pauboodosnnxnxnnxnnx52121211111211233333" + map);
//                    Integer integerDh = gbDpgService.queryGbPurchaseGoodsCount(map);
//                    BigDecimal batchBillTotalDh = new BigDecimal(0);
//                    if (integerDh > 0) {
//                        Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                        batchBillTotalDh = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    }
//                    mapItem.put("dinghuo", batchBillTotalDh);
//                    itemList.add(mapItem);
//                } else if (type == 0) {
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("date", whichDay);
//                    map.put("disId", disId);
//                    map.put("supplierBuy", -1);
//                    map.put("dayuStatus", 2);
//                    if (!purUserId.equals("-1")) {
//                        map.put("purUserId", purUserId);
//                    }
//
//                    System.out.println("pauboodosnnxnxnnxnn22222mmmmmmm" + map);
//
//                    Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
//                    BigDecimal batchBillTotal = new BigDecimal(0);
//                    if (integer > 0) {
//                        Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                        batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    }
//                    Map<String, Object> mapItem = new HashMap<>();
//                    mapItem.put("day", whichDay);
//                    mapItem.put("value", batchBillTotal);
//                    itemList.add(mapItem);
//                } else if (type == 1) {
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("date", whichDay);
//                    map.put("disId", disId);
//                    map.put("supplierBuy", 1);
//                    map.put("dayuStatus", 2);
//                    map.put("batchDayuStatus", 2);
//                    if (!supplierId.equals("-1")) {
//                        map.put("supplierId", supplierId);
//                        map.put("supplierBuy", null);
//                    }
//                    System.out.println("pauboodosnnxnxnnxnnx52121211111211233333" + map);
//                    Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
//                    BigDecimal batchBillTotal = new BigDecimal(0);
//                    if (integer > 0) {
//                        Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                        batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    }
//                    Map<String, Object> mapItem = new HashMap<>();
//                    mapItem.put("day", whichDay);
//                    mapItem.put("value", batchBillTotal);
//                    itemList.add(mapItem);
//                }
//
//            }
//
//        } else {
//            if (type == -1) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("date", startDate);
//                map.put("disId", disId);
//                map.put("dayuStatus", 2);
//                if (!purUserId.equals("-1")) {
//                    map.put("purUserId", purUserId);
//                }
//                System.out.println("pauboodosnnxnxnnxnn22222" + map);
//
//                Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
//                BigDecimal batchBillTotal = new BigDecimal(0);
//                if (integer > 0) {
//                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                    batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//                }
//                Map<String, Object> mapItem = new HashMap<>();
//                mapItem.put("day", startDate);
//                mapItem.put("value", batchBillTotal);
//                itemList.add(mapItem);
//            } else if (type == 0) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("date", startDate);
//                map.put("disId", disId);
//                map.put("supplierBuy", -1);
//                map.put("dayuStatus", 2);
//                if (!purUserId.equals("-1")) {
//                    map.put("purUserId", purUserId);
//                }
//                if (!supplierId.equals("-1")) {
//                    map.put("supplierId", supplierId);
//                    map.put("supplierBuy", null);
//                }
//                System.out.println("pauboodosnnxnxnnxnn22222" + map);
//
//                Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
//                BigDecimal batchBillTotal = new BigDecimal(0);
//                if (integer > 0) {
//                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                    batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//                }
//                Map<String, Object> mapItem = new HashMap<>();
//                mapItem.put("day", startDate);
//                mapItem.put("value", batchBillTotal);
//                itemList.add(mapItem);
//            } else if (type == 1) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("date", startDate);
//                map.put("disId", disId);
//                map.put("supplierBuy", 1);
//                map.put("dayuStatus", 2);
//                map.put("batchDayuStatus", 2);
//                if (!supplierId.equals("-1")) {
//                    map.put("supplierId", supplierId);
//                    map.put("supplierBuy", null);
//                }
//                Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
//                BigDecimal batchBillTotal = new BigDecimal(0);
//                if (integer > 0) {
//                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                    batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//                }
//                Map<String, Object> mapItem = new HashMap<>();
//                mapItem.put("day", startDate);
//                mapItem.put("value", batchBillTotal);
//                itemList.add(mapItem);
//            }
//
//        }
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", disId);
//        map.put("startDate", startDate);
//        map.put("stopDate", stopDate);
//        Double aDoubleAllPur = gbDpgService.queryPurchaseGoodsSubTotal(map);
//
//        Double costSubtotal = gbDepartmentStockReduceService.queryReduceCostSubtotal(map);
//
//
//        if (!purUserId.equals("-1")) {
//            map.put("purUserId", purUserId);
//        }
//        if (!supplierId.equals("-1")) {
//            map.put("supplierId", supplierId);
//            map.put("supplierBuy", null);
//        }
//        map.put("supplierBuy", -1);
//        map.put("dayuStatus", 2);
//        System.out.println("zahsuishsissisiisisiis1111" + map);
//        Integer integer1 = gbDpgService.queryGbPurchaseGoodsCount(map);
//        BigDecimal purchaseTotal = new BigDecimal(0);
//        List<GbDepartmentUserEntity> departmentUserEntities = new ArrayList<>();
//        List<NxJrdhSupplierEntity> supplierEntities = new ArrayList<>();
//        if (integer1 > 0) {
//            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//            purchaseTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//            departmentUserEntities = gbDpgService.queryPurUserList(map);
//        }
//
//        map.put("supplierBuy", 1);
//        map.put("batchDayuStatus", 2);
//        System.out.println("zahsuishsissisiisisiis21212121" + map);
//
//        Integer integer2 = gbDpgService.queryGbPurchaseGoodsCount(map);
//        BigDecimal orderTotal = new BigDecimal(0);
//        if (integer2 > 0) {
//            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//            orderTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//            supplierEntities = gbDpgService.queryDisPurGoodsSupplierList(map);
//        } else {
//            orderTotal = new BigDecimal(0);
//        }
//
//
//        Map<String, Object> map123 = new HashMap<>();
//        map123.put("allTotal", new BigDecimal(aDoubleAllPur).setScale(1, BigDecimal.ROUND_HALF_UP));
//        BigDecimal bigDecimal = new BigDecimal(aDoubleAllPur).divide(new BigDecimal(howManyDaysInPeriod)).setScale(1, BigDecimal.ROUND_HALF_UP);
//        System.out.println("purtoot" + aDoubleAllPur + "howmna" + howManyDaysInPeriod + " bidgdeeee" + bigDecimal);
//        map123.put("purchasePerDay", bigDecimal);
//
//        map123.put("orderTotal", orderTotal);
//        map123.put("purchaseTotal", purchaseTotal);
//        map123.put("costTotal", new BigDecimal(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP));
//        BigDecimal bigDecimalCost = new BigDecimal(costSubtotal).divide(new BigDecimal(howManyDaysInPeriod)).setScale(1, BigDecimal.ROUND_HALF_UP);
//
//        map123.put("costPerDay", bigDecimalCost);
//        System.out.println("purtootcccc" + costSubtotal + "howmna" + howManyDaysInPeriod + " bidgdeeee" + bigDecimalCost);
//
//        map123.put("arr", itemList);
//        map123.put("purUserList", departmentUserEntities);
//        map123.put("supplierList", supplierEntities);
//
//
//        return R.ok().put("data", map123);
//    }

//    @RequestMapping(value = "/getDisPurchaserDateJingjing", method = RequestMethod.POST)
//    @ResponseBody
//    public R getDisPurchaserDateJingjing(Integer disId, String startDate, String stopDate, Integer type,
//                                         String searchDepIds, String searchDepId) {
//
//        List<Map<String, Object>> itemList = new ArrayList<>();
//        Integer howManyDaysInPeriod = 0;
//        if (!startDate.equals(stopDate)) {
//            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
//        }
//        if (howManyDaysInPeriod > 0) {
//
//            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
//                // dateList
//                String whichDay = "";
//                if (i == 0) {
//                    whichDay = startDate;
//                } else {
//                    whichDay = afterWhatDay(startDate, i);
//                }
//                if (type == 0) {
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("date", whichDay);
//                    map.put("disId", disId);
//                    map.put("supplierBuy", -1);
//                    map.put("dayuStatus", 2);
//                    System.out.println("pauboodosnnxnxnnxnn22222mmmmmmm" + map);
//
//                    Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
//                    BigDecimal batchBillTotal = new BigDecimal(0);
//                    if (integer > 0) {
//                        Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                        batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    }
//                    Map<String, Object> mapItem = new HashMap<>();
//                    mapItem.put("day", whichDay);
//                    mapItem.put("value", batchBillTotal);
//                    itemList.add(mapItem);
//                } else if (type == 1) {
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("date", whichDay);
//                    map.put("disId", disId);
//                    map.put("supplierBuy", 1);
//                    map.put("dayuStatus", 2);
//                    map.put("batchDayuStatus", 2);
//                    System.out.println("pauboodosnnxnxnnxnnx521212111112112" + map);
//
//                    Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
//                    BigDecimal batchBillTotal = new BigDecimal(0);
//                    if (integer > 0) {
//                        Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                        batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    }
//                    Map<String, Object> mapItem = new HashMap<>();
//                    mapItem.put("day", whichDay);
//                    mapItem.put("value", batchBillTotal);
//                    itemList.add(mapItem);
//                }
//
//            }
//
//        } else {
//            if (type == 0) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("date", startDate);
//                map.put("disId", disId);
//                map.put("supplierBuy", -1);
//                map.put("dayuStatus", 2);
//                System.out.println("pauboodosnnxnxnnxnn22222" + map);
//
//                Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
//                BigDecimal batchBillTotal = new BigDecimal(0);
//                if (integer > 0) {
//                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                    batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//                }
//                Map<String, Object> mapItem = new HashMap<>();
//                mapItem.put("day", startDate);
//                mapItem.put("value", batchBillTotal);
//                itemList.add(mapItem);
//            } else if (type == 1) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("date", startDate);
//                map.put("disId", disId);
//                map.put("supplierBuy", 1);
//                map.put("dayuStatus", 2);
//                map.put("batchDayuStatus", 2);
//                Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
//                BigDecimal batchBillTotal = new BigDecimal(0);
//                if (integer > 0) {
//                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                    batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//                }
//                Map<String, Object> mapItem = new HashMap<>();
//                mapItem.put("day", startDate);
//                mapItem.put("value", batchBillTotal);
//                itemList.add(mapItem);
//            }
//
//
//        }
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", disId);
//        if (!searchDepId.equals("-1")) {
//            map.put("purDepId", searchDepId);
//        } else {
//            if (!searchDepIds.equals("-1")) {
//                String[] arrGb = searchDepIds.split(",");
//                List<String> idsGb = new ArrayList<>();
//                for (String idGb : arrGb) {
//                    idsGb.add(idGb);
//                    if (idsGb.size() > 0) {
//                        map.put("purDepIds", idsGb);
//                    }
//                }
//            }
//        }
//
//        map.put("startDate", startDate);
//        map.put("stopDate", stopDate);
//        map.put("supplierBuy", -1);
//        map.put("dayuStatus", 2);
//        System.out.println("zahsuishsissisiisisiis1111" + map);
//        Integer integer1 = gbDpgService.queryGbPurchaseGoodsCount(map);
//        BigDecimal purchaseTotal = new BigDecimal(0);
//        if (integer1 > 0) {
//            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//            purchaseTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//        }
//
//        map.put("supplierBuy", 1);
//        map.put("batchDayuStatus", 2);
//        System.out.println("zahsuishsissisiisisiis21212121" + map);
//
//        Integer integer2 = gbDpgService.queryGbPurchaseGoodsCount(map);
//        BigDecimal orderTotal = new BigDecimal(0);
//        if (integer2 > 0) {
//            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//            orderTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//        } else {
//            orderTotal = new BigDecimal(0);
//        }
//
//
//        Map<String, Object> map123 = new HashMap<>();
//        map123.put("purchaseTotal", purchaseTotal);
//        map123.put("orderTotal", orderTotal);
//        map123.put("arr", itemList);
//
//
//        return R.ok().put("data", map123);
//    }

    @RequestMapping(value = "/getUserPurchaserDateBill", method = RequestMethod.POST)
    @ResponseBody
    public R getUserPurchaserDateBill(Integer userId, String startDate, String stopDate, Integer type) {

        List<Map<String, Object>> itemList = new ArrayList<>();
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
                if (type != 2) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", whichDay);
                    map.put("purUserId", userId);
                    map.put("payType", type);
                    System.out.println("totototototoototot" + map);
                    Integer integer = gbDPBService.queryDisPurchaseBatchCount(map);
                    BigDecimal batchBillTotal = new BigDecimal(0);
                    if (integer > 0) {
                        Double aDouble = gbDPBService.queryPurchaserCashTotal(map);
                        batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    Map<String, Object> mapItem = new HashMap<>();
                    mapItem.put("day", whichDay);
                    mapItem.put("value", batchBillTotal);
                    itemList.add(mapItem);
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("purUserId", userId);
                    map.put("finishDate", whichDay);
                    map.put("batchId", -1);
                    map.put("dayuStatus", 1);
                    System.out.println("pauboodos" + map);
                    Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);

                    BigDecimal maileTotal = new BigDecimal(0);
                    if (integer > 0) {
                        Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                        maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    Map<String, Object> mapItem = new HashMap<>();
                    mapItem.put("day", whichDay);
                    mapItem.put("value", maileTotal);
                    itemList.add(mapItem);
                }

            }

        } else {
            if (type != 2) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", startDate);
                map.put("purUserId", userId);
                map.put("payType", type);
                System.out.println("totototototoototot" + map);
                Integer integer = gbDPBService.queryDisPurchaseBatchCount(map);
                BigDecimal batchBillTotal = new BigDecimal(0);
                if (integer > 0) {
                    Double aDouble = gbDPBService.queryPurchaserCashTotal(map);
                    batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("day", startDate);
                mapItem.put("value", batchBillTotal);
                itemList.add(mapItem);
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("purUserId", userId);
                map.put("finishDate", startDate);
                map.put("batchId", -1);
                map.put("dayuStatus", 1);
                System.out.println("pauboodos" + map);
                Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);

                BigDecimal maileTotal = new BigDecimal(0);
                if (integer > 0) {
                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                    maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("day", startDate);
                mapItem.put("value", maileTotal);
                itemList.add(mapItem);
            }

        }

        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("payType", 0);
        map.put("dayuStatus", 1);
        Integer integer1 = gbDPBService.queryDisPurchaseBatchCount(map);
        BigDecimal batchCashTotal = new BigDecimal(0);
        if (integer1 > 0) {
            Double aDouble = gbDPBService.queryPurchaserCashTotal(map);
            batchCashTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        map.put("payType", 1);
        Integer integer2 = gbDPBService.queryDisPurchaseBatchCount(map);
        BigDecimal batchBillTotal = new BigDecimal(0);
        if (integer2 > 0) {
            Double aDouble = gbDPBService.queryPurchaserCashTotal(map);
            batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            batchBillTotal = new BigDecimal(0);
        }

        //购买采购商品 batchId == -1
        Map<String, Object> mapG = new HashMap<>();
        mapG.put("purUserId", userId);
        mapG.put("startDate", startDate);
        mapG.put("stopDate", stopDate);
        mapG.put("payType", 0);
        mapG.put("batchId", -1);
        mapG.put("dayuStatus", 1);
        System.out.println("cakshskshskkskshsks");
        Integer integer = gbDpgService.queryGbPurchaseGoodsCount(mapG);
        BigDecimal maileTotal = new BigDecimal(0);
        if (integer > 0) {
            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(mapG);
            maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        Map<String, Object> map123 = new HashMap<>();
        map123.put("maileTotal", maileTotal);
        map123.put("batchCashTotal", batchCashTotal);
        map123.put("batchBillTotal", batchBillTotal);
        map123.put("arr", itemList);

        return R.ok().put("data", map123);
    }

    @RequestMapping(value = "/disGetDepPurchaserGoodsByDate", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDepPurchaserGoodsByDate(Integer depId, String date) {


        //购买采购商品 batchId == -1
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", depId);
        map.put("finishDate", date);
        map.put("payType", 0);
        map.put("batchId", -1);
        map.put("dayuStatus", 1);
        System.out.println("abcc" + map);
        List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();
        List<GbDistributerFatherGoodsEntity> greatGrandPurGoodsDetial = gbDpgService.queryGbFatherDisPurchaseGoods(map);
        if (greatGrandPurGoodsDetial.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrand : greatGrandPurGoodsDetial) {
                List<GbDistributerFatherGoodsEntity> grandFatherGoodsEntities = greatGrand.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grand : grandFatherGoodsEntities) {
                    result.addAll(grand.getFatherGoodsEntities());
                }

            }
        }

        return R.ok().put("data", result);
    }


//    @RequestMapping(value = "/disGetPurchaserGoodsByDate", method = RequestMethod.POST)
//    @ResponseBody
//    public R disGetPurchaserGoodsByDate(Integer disId, String date, String searchDepIds, String searchDepId) {
//
//        //购买采购商品 batchId == -1
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", disId);
//        map.put("finishDate", date);
//        map.put("payType", 0);
//        map.put("batchId", -1);
//        map.put("dayuStatus", 1);
//        if (!searchDepId.equals("-1")) {
//            map.put("purDepId", searchDepId);
//        } else {
//            if (!searchDepIds.equals("-1")) {
//                String[] arrGb = searchDepIds.split(",");
//                List<String> idsGb = new ArrayList<>();
//                for (String idGb : arrGb) {
//                    idsGb.add(idGb);
//                    if (idsGb.size() > 0) {
//                        map.put("purDepIds", idsGb);
//                    }
//                }
//            }
//        }
//
//        List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();
//        System.out.println("pururugoogogogooodod" + map);
////        List<GbDistributerFatherGoodsEntity> greatGrandPurGoodsDetial = gbDpgService.queryGreatGrandPurGoodsDetail(map);
//        List<GbDistributerFatherGoodsEntity> greatGrandPurGoodsDetial = gbDpgService.queryGbFatherDisPurchaseGoods(map);
//        if (greatGrandPurGoodsDetial.size() > 0) {
//            for (GbDistributerFatherGoodsEntity greatGrand : greatGrandPurGoodsDetial) {
//                List<GbDistributerFatherGoodsEntity> grandFatherGoodsEntities = greatGrand.getFatherGoodsEntities();
//                for (GbDistributerFatherGoodsEntity grand : grandFatherGoodsEntities) {
//                    result.addAll(grand.getFatherGoodsEntities());
//                }
//            }
//        }
//        List<GbDepartmentEntity> deplist = new ArrayList<>();
//        if (!searchDepIds.equals("-1")) {
//            String[] arrGb = searchDepIds.split(",");
//
//            for (String depId : arrGb) {
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(depId));
//                Double aDoubleDep = 0.0;
//                map.put("purDepId", departmentEntity.getGbDepartmentId());
//                map.put("purDepIds", null);
//                int count = gbDpgService.queryGbPurchaseGoodsCount(map);
//                if (count > 0) {
//                    aDoubleDep = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                }
//
//                departmentEntity.setDepCostGoodsTotalString(new BigDecimal(aDoubleDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                deplist.add(departmentEntity);
//            }
//        }
//        Map<String, Object> mapR = new HashMap<>();
//        mapR.put("arr", result);
//        mapR.put("depArr", deplist);
//        return R.ok().put("data", mapR);
//    }

    @RequestMapping(value = "/userGetDepPurchaserGoodsByDate", method = RequestMethod.POST)
    @ResponseBody
    public R userGetDepPurchaserGoodsByDate(Integer userId, String date) {

        //购买采购商品 batchId == -1
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("finishDate", date);
        map.put("payType", 0);
        map.put("batchId", -1);
        map.put("dayuStatus", 1);
        System.out.println("abcc" + map);
        List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();
        List<GbDistributerFatherGoodsEntity> greatGrandPurGoodsDetial;
//        greatGrandPurGoodsDetial = gbDpgService.queryGreatGrandPurGoodsDetail(map);
        greatGrandPurGoodsDetial = gbDpgService.queryGbFatherDisPurchaseGoods(map);
        if (greatGrandPurGoodsDetial.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrand : greatGrandPurGoodsDetial) {
                List<GbDistributerFatherGoodsEntity> grandFatherGoodsEntities = greatGrand.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grand : grandFatherGoodsEntities) {
                    result.addAll(grand.getFatherGoodsEntities());
                }

            }
        }

        return R.ok().put("data", result);
    }

//    @RequestMapping(value = "/getPurchaserDateBill", method = RequestMethod.POST)
//    @ResponseBody
//    public R getPurchaserDateBill(Integer purDepId, String startDate, String stopDate) {
//
//        //购买采购商品batchId == -1
//        Map<String, Object> map = new HashMap<>();
//        map.put("purDepId", purDepId);
//        map.put("startDate", startDate);
//        map.put("stopDate", stopDate);
//        map.put("batchId", -1);
//        map.put("payType", 0);
//        map.put("dayuStatus", 1);
//        Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
//        if (integer > 0) {
//        BigDecimal maileTotal = new BigDecimal(0);
//        if (integer > 0) {
//            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
//            maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//        } else {
//            maileTotal = new BigDecimal(0);
//        }
//
//
//        //现金订货
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("purDepId", purDepId);
//        map1.put("startDate", startDate);
//        map1.put("stopDate", stopDate);
//        map1.put("payType", 0);
//        List<GbDistributerPurchaseBatchEntity> entitiesCash = gbDPBService.queryDisPurchaseBatch(map1);
//        BigDecimal batchCashTotal = new BigDecimal(0);
//        if (entitiesCash.size() > 0) {
//            Double aDouble = gbDPBService.queryPurchaserCashTotal(map1);
//            batchCashTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//        } else {
//            batchCashTotal = new BigDecimal(0);
//        }
//
//
//        Map<String, Object> map2 = new HashMap<>();
//        map2.put("purDepId", purDepId);
//        map2.put("startDate", startDate);
//        map2.put("stopDate", stopDate);
//        map2.put("payType", 1);
//        List<GbDistributerPurchaseBatchEntity> entitiesBill = gbDPBService.queryDisPurchaseBatch(map2);
//        BigDecimal batchBillTotal = new BigDecimal(0);
//        if (entitiesBill.size() > 0) {
//            Double aDouble = gbDPBService.queryPurchaserCashTotal(map2);
//            batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
//        } else {
//            batchBillTotal = new BigDecimal(0);
//        }
//
//        BigDecimal add = maileTotal.add(batchCashTotal).add(batchBillTotal).setScale(2, BigDecimal.ROUND_HALF_UP);
//
//        Map<String, Object> map123 = new HashMap<>();
//        map123.put("total", add);
//        map123.put("maileTotal", maileTotal);
//        map123.put("batchCashTotal", batchCashTotal);
//        map123.put("batchBillTotal", batchBillTotal);
//        map123.put("maileArr", goodsEntities);
//        map123.put("batchCashArr", entitiesCash);
//        map123.put("batchBillArr", entitiesBill);
//
//        return R.ok().put("data", map123);
//    }


    @RequestMapping(value = "/getDisGoodsPriceDayData", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsPriceDayData(Integer disGoodsId, String startDate, String stopDate, String ids) {
        System.out.println(stopDate + "stopDatewhatis");
        String[] arr = ids.split(",");
        TreeSet<String> list = new TreeSet<>();
        List<GbDepartmentEntity> departmentEntities = new ArrayList<>();
        try {
            for (String fatherID : arr) {
                GbDepartmentEntity fatherDep = gbDepartmentService.queryObject(Integer.valueOf(fatherID));
                Integer howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
                List<String> aaa = new ArrayList<>();
                for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat format11 = new SimpleDateFormat("dd");
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
                    String aDouble = gbDpgService.queryPurchaseGoodsPrice(map1);
                    if (aDouble == null) {
                        aDouble = "0.0";
                    }
                    aaa.add(aDouble);
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
        return R.ok().put("data", map3);
    }


    @RequestMapping(value = "/getDistributerGoodsPurchaseList", method = RequestMethod.POST)
    @ResponseBody
    public R getDistributerGoodsPurchaseList(String startDate, String stopDate, String ids,
                                             Integer totalPurchase, Integer lowerPurchase, Integer higherPurchase) {

        String[] arr = ids.split(",");
        List<GbDepartmentEntity> dataDeps = new ArrayList<>();
        for (String id : arr) {
            GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(id));
            // totalPurchase
            if (totalPurchase == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("purDepId", id);
                map.put("startDate", startDate);
                map.put("stopDate", stopDate);
                System.out.println(map);
//                List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDpgService.querySimplePurGoods(map);
                Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
                if (integer > 0) {
                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                    departmentEntity.setDepPurchaseTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
            // higherPurchase
            if (higherPurchase == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("depId", id);
                map.put("startDate", startDate);
                map.put("stopDate", stopDate);
                map.put("purWhat", 1);
                int highestAmount = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
                if (highestAmount > 0) {
                    Double aDouble = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
                    departmentEntity.setDepPurchaseHigherTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
            // lowerPurchase
            if (lowerPurchase == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("depId", id);
                map.put("startDate", startDate);
                map.put("stopDate", stopDate);
                map.put("purWhat", 0);
                int lowerAmout = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
                if (lowerAmout > 0) {
                    Double aDouble = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
                    departmentEntity.setDepPurchaseLowerTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }

            dataDeps.add(departmentEntity);
        }

        return R.ok().put("data", dataDeps);
    }


    @RequestMapping(value = "/getDistributerGoodsPurchaseData", method = RequestMethod.POST)
    @ResponseBody
    public R getDistributerGoodsPurchaseData(String startDate, String stopDate, String ids,
                                             Integer totalPurchase, Integer lowerPurchase, Integer higherPurchase) {

        String[] arr = ids.split(",");
        List<GbDepartmentEntity> dataDeps = new ArrayList<>();
        for (String id : arr) {
            GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(id));
            // totalPurchase
            if (totalPurchase == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("purDepId", id);
                map.put("startDate", startDate);
                map.put("stopDate", stopDate);
                Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
                if (integer > 0) {
                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                    departmentEntity.setDepPurchaseTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
            // higherPurchase
            if (higherPurchase == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("depId", id);
                map.put("startDate", startDate);
                map.put("stopDate", stopDate);
                map.put("purWhat", 1);
                int highestAmount = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
                if (highestAmount > 0) {
                    Double aDouble = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
                    departmentEntity.setDepPurchaseHigherTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
            // lowerPurchase
            if (lowerPurchase == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("depId", id);
                map.put("startDate", startDate);
                map.put("stopDate", stopDate);
                map.put("purWhat", 0);
                int lowerAmout = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
                if (lowerAmout > 0) {
                    Double aDouble = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
                    departmentEntity.setDepPurchaseLowerTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }

            dataDeps.add(departmentEntity);
        }

        return R.ok().put("data", dataDeps);
    }


    @RequestMapping(value = "/getDistributerGoodsPurchaseManage", method = RequestMethod.POST)
    @ResponseBody
    public R getDistributerGoodsPurchaseManage(String startDate, String stopDate, Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("disId", disId);
        List<GbDistributerGoodsPriceEntity> entities = gbDistributerGoodsPriceService.queryPriceGoodsListByParams(map);


//        List<GbDepartmentEntity> dataDeps = new ArrayList<>();
//        Map<String, Object> map111 = new HashMap<>();
//        map111.put("disId", disId);
//        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(map111);
//        System.out.println(departmentEntities.size() + "denpsisiisis");
//        for (GbDepartmentEntity departmentEntity : departmentEntities) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("depId", departmentEntity.getGbDepartmentId());
//                map.put("startDate", startDate);
//                map.put("stopDate", stopDate);
//                map.put("purWhat", 1);
//                int highestAmount = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
//                if (highestAmount > 0) {
//                    Double aDouble = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
//                    departmentEntity.setDepPurchaseHigherTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
//                }
//
//            // lowerPurchase
//                Map<String, Object> map1 = new HashMap<>();
//                map1.put("depId", departmentEntity.getGbDepartmentId());
//                map1.put("startDate", startDate);
//                map1.put("stopDate", stopDate);
//                map1.put("purWhat", 0);
//                int lowerAmout = gbDistributerGoodsPriceService.queryPriceWhatAmount(map1);
//                if (lowerAmout > 0) {
//                    Double aDouble = gbDistributerGoodsPriceService.queryPriceWhatTotal(map1);
//                    departmentEntity.setDepPurchaseLowerTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
//                }
//
//         if(highestAmount > 0 || lowerAmout > 0){
//             dataDeps.add(departmentEntity);
//         }
//        }
//
        return R.ok().put("data", entities);
    }


    @RequestMapping(value = "/getJicaiPurchaseAccountData")
    @ResponseBody
    public R getJicaiPurchaseAccountData(Integer depId, String startDate, String stopDate) {

        Double oneTotal = getInventoryPurchaseData(depId, startDate, stopDate, 1);
        Double oneTotal1 = getInventoryPurchaseData(depId, startDate, stopDate, 2);
        Double oneTotal2 = getInventoryPurchaseData(depId, startDate, stopDate, 3);
        Double total = oneTotal + oneTotal1 + oneTotal2;

        String format = String.format("%.2f", total);
        Map<String, Object> map = new HashMap<>();
        map.put("total", format);
        map.put("one", String.format("%.2f", oneTotal));
        map.put("two", String.format("%.2f", oneTotal1));
        map.put("three", String.format("%.2f", oneTotal2));
        return R.ok().put("data", map);
    }

    private Double getInventoryPurchaseData(Integer depId, String startDate, String stopDate, Integer type) {

        Double total = 0.0;
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("type", type);
        Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
        if (integer > 0) {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("depId", depId);
            map1.put("startDate", startDate);
            map1.put("stopDate", stopDate);
            map1.put("type", type);
            total = gbDpgService.queryPurchaseInventoryGoodsSubTotal(map1);
        }
        return total;
    }


    @RequestMapping(value = "/getPurchasePurGoods/{userId}")
    @ResponseBody
    public R getPurchasePurGoods(@PathVariable Integer userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("month", formatWhatMonth(0));
        map.put("dayuStatus", 1);
        map.put("batchId", -1);
        System.out.println("purgogogoogo" + map);
        List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities = gbDpgService.queryPurchaseGoodsWithDetailByParams(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("month", formatWhatMonth(0));
        map1.put("arr", gbDistributerPurchaseGoodsEntities);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("purUserId", userId);
        map2.put("month", getLastMonth());
        map2.put("dayuStatus", 1);
        map2.put("batchId", -1);

        List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities2 = gbDpgService.queryPurchaseGoodsWithDetailByParams(map2);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("month", getLastMonth());
        map3.put("arr", gbDistributerPurchaseGoodsEntities2);

        Map<String, Object> map4 = new HashMap<>();
        map4.put("purUserId", userId);
        map4.put("month", getLastTwoMonth());
        map4.put("dayuStatus", 1);
        map4.put("batchId", -1);
        List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities3 = gbDpgService.queryPurchaseGoodsWithDetailByParams(map4);
        Map<String, Object> map5 = new HashMap<>();
        map5.put("month", getLastTwoMonth());
        map5.put("arr", gbDistributerPurchaseGoodsEntities3);

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(map1);
        result.add(map3);
        result.add(map5);
        return R.ok().put("data", result);
    }

    @RequestMapping(value = "/getGbDepartmentPurGoods/{depFatherId}")
    @ResponseBody
    public R getGbDepartmentPurGoods(@PathVariable Integer depFatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", depFatherId);
        map.put("month", formatWhatMonth(0));
        map.put("dayuStatus", 1);
        map.put("batchId", -1);
        List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities = gbDpgService.queryPurchaseGoodsWithDetailByParams(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("month", formatWhatMonth(0));
        map1.put("arr", gbDistributerPurchaseGoodsEntities);

        Map<String, Object> map2 = new HashMap<>();
        map.put("purDepId", depFatherId);
        map2.put("month", getLastMonth());
        map2.put("dayuStatus", 1);

        List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities2 = gbDpgService.queryPurchaseGoodsWithDetailByParams(map2);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("month", getLastMonth());
        map3.put("arr", gbDistributerPurchaseGoodsEntities2);

        Map<String, Object> map4 = new HashMap<>();
        map.put("purDepId", depFatherId);
        map4.put("month", getLastTwoMonth());
        map4.put("dayuStatus", 1);
        List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities3 = gbDpgService.queryPurchaseGoodsWithDetailByParams(map4);
        Map<String, Object> map5 = new HashMap<>();
        map5.put("month", getLastTwoMonth());
        map5.put("arr", gbDistributerPurchaseGoodsEntities3);

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(map1);
        result.add(map3);
        result.add(map5);
        return R.ok().put("data", result);
    }


    @RequestMapping(value = "/nxDisSavePurGoodsPriceSx", method = RequestMethod.POST)
    @ResponseBody
    public R nxDisSavePurGoodsPriceSx(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {
        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purGoods.getGbDistributerGoodsEntity().getGbDepartmentOrdersEntities();
        BigDecimal purWeight = new BigDecimal(0);
        for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
//            Integer gbDoNxDepartmentOrderId = gbDepartmentOrdersEntity.getGbDoNxDepartmentOrderId();
//            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
            NxDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersEntity.getNxDepartmentOrdersEntity();
            ordersEntity.setNxDoPrice(purGoods.getGbDpgBuyPrice());
            gbDepartmentOrdersEntity.setGbDoPrice(ordersEntity.getNxDoPrice());
            if (ordersEntity.getNxDoExpectPrice() != null && !ordersEntity.getNxDoExpectPrice().trim().isEmpty()) {
                BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpectPrice());
                BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                BigDecimal subtract = doPrice.subtract(expectPrice);
                System.out.println("exxxxxxxx" + subtract);
                ordersEntity.setNxDoPriceDifferent(subtract.toString());
                gbDepartmentOrdersEntity.setGbDoPriceDifferent(subtract.toString());

            }

            if (ordersEntity.getNxDoWeight() != null && new BigDecimal(ordersEntity.getNxDoWeight()).compareTo(BigDecimal.ZERO) == 1) {
                BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());
                BigDecimal subtotalB = weightB.multiply(new BigDecimal(purGoods.getGbDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoStatus(2);
                ordersEntity.setNxDoSubtotal(subtotalB.toString());
                gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());
                gbDepartmentOrdersEntity.setGbDoSubtotal(ordersEntity.getNxDoSubtotal());
                purWeight = purWeight.add(new BigDecimal(gbDepartmentOrdersEntity.getGbDoWeight()));

            }

            nxDepartmentOrdersService.update(ordersEntity);
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        }


        purGoods.setGbDpgBuyQuantity(purWeight.toString());

        if (purGoods.getGbDpgBuyPrice() != null && !purGoods.getGbDpgBuyPrice().trim().isEmpty()) {
            BigDecimal decimal1 = new BigDecimal(purGoods.getGbDpgBuyPrice());
            BigDecimal decimal2 = new BigDecimal(purGoods.getGbDpgBuyQuantity());
            BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
            purGoods.setGbDpgBuySubtotal(decimal3.toString());
        }

//        purGoods.setGbDpgStatus(getNxDisPurchaseGoodsIsPurchase());
        purGoods.setGbDpgPayType(1);
        purGoods.setGbDpgTime(formatWhatTime(0));
        purGoods.setGbDpgPurchaseDate(formatWhatDay(0));
        purGoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
        purGoods.setGbDpgPurchaseYear(formatWhatYear(0));
        purGoods.setGbDpgPurchaseWeek(getWeek(0));
        purGoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
        gbDpgService.update(purGoods);

        Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
        gbDistributerGoodsEntity.setGbDgNxDistributerGoodsPrice(purGoods.getGbDpgBuyPrice());
        gbDistributerGoodsService.update(gbDistributerGoodsEntity);

        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purGoods);
        }

        //判断是否有保鲜时间参数
        Integer gbDoDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
            int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
            purGoods.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
            gbDpgService.update(purGoods);
        }

        return R.ok();
    }

    @RequestMapping(value = "/nxDisSavePurGoodsPrice", method = RequestMethod.POST)
    @ResponseBody
    public R nxDisSavePurGoodsPrice(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {
        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purGoods.getGbDistributerGoodsEntity().getGbDepartmentOrdersEntities();
        System.out.println("orssisiisisiis" + gbDepartmentOrdersEntities.size());
        BigDecimal purWeight = new BigDecimal(0);
        for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
            NxDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersEntity.getNxDepartmentOrdersEntity();
            ordersEntity.setNxDoPrice(purGoods.getGbDpgBuyPrice());
            gbDepartmentOrdersEntity.setGbDoPrice(ordersEntity.getNxDoPrice());
            if (ordersEntity.getNxDoWeight() != null && new BigDecimal(ordersEntity.getNxDoWeight()).compareTo(BigDecimal.ZERO) == 1) {
                BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());
                BigDecimal subtotalB = weightB.multiply(new BigDecimal(purGoods.getGbDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());
                ordersEntity.setNxDoSubtotal(subtotalB.toString());
                gbDepartmentOrdersEntity.setGbDoSubtotal(ordersEntity.getNxDoSubtotal());
                purWeight = purWeight.add(new BigDecimal(gbDepartmentOrdersEntity.getGbDoWeight()));
            }

            nxDepartmentOrdersService.update(ordersEntity);
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        }


        purGoods.setGbDpgBuyQuantity(purWeight.toString());

        if (purGoods.getGbDpgBuyPrice() != null && !purGoods.getGbDpgBuyPrice().trim().isEmpty()) {
            BigDecimal decimal1 = new BigDecimal(purGoods.getGbDpgBuyPrice());
            BigDecimal decimal2 = new BigDecimal(purGoods.getGbDpgBuyQuantity());
            BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
            purGoods.setGbDpgBuySubtotal(decimal3.toString());
        }

        purGoods.setGbDpgPayType(1);
        purGoods.setGbDpgTime(formatWhatTime(0));
        purGoods.setGbDpgPurchaseDate(formatWhatDay(0));
        purGoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
        purGoods.setGbDpgPurchaseYear(formatWhatYear(0));
        purGoods.setGbDpgPurchaseWeek(getWeek(0));
        purGoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
        gbDpgService.update(purGoods);

        Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
        gbDistributerGoodsEntity.setGbDgNxDistributerGoodsPrice(purGoods.getGbDpgBuyPrice());
        gbDistributerGoodsService.update(gbDistributerGoodsEntity);

        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purGoods);
        }

        //判断是否有保鲜时间参数
        Integer gbDoDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
            int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
            purGoods.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
            gbDpgService.update(purGoods);
        }

        return R.ok();
    }


    @RequestMapping(value = "/nxDisGetPurchaseGoodsGb")
    @ResponseBody
    public R nxDisGetPurchaseGoodsGb(Integer disId, Integer nxDisId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("nxDisId", nxDisId);
        map4.put("purStatus", 3);
        map4.put("purchaseType", 5);
        System.out.println("abckckckc" + map4);
        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryGbFatherDisPurchaseGoods(map4);

        return R.ok().put("data", purchaseToday);
    }

    @RequestMapping(value = "/gbGetNxDisCoupon", method = RequestMethod.POST)
    @ResponseBody
    public R gbGetNxDisCoupon(Integer gbDisId, Integer nxDisId) {
        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDisId);

        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDisId);
        map.put("equalStatus", 0);
        map.put("cityId", gbDistributerEntity.getGbDistributerSysCityId());
        System.out.println("coupeoneoemap" + map);
        List<NxDistributerCouponEntity> couponEntities = nxDistributerCouponService.queryLoadDownListByParams(map);
        List<NxDistributerCouponEntity> resultCouList = new ArrayList<>();
        if (couponEntities.size() > 0) {
            for (NxDistributerCouponEntity couponEntity : couponEntities) {
                Map<String, Object> mapG = new HashMap<>();
                mapG.put("gbDisId", gbDisId);
                mapG.put("nxDisId", nxDisId);
                mapG.put("couponId", couponEntity.getNxDistributerCouponId());
                System.out.println("c" + mapG);
                List<NxGbDistibuterUserCouponEntity> userCouponEntities = nxGbDistibuterUserCouponService.queryGbCouponListByParams(mapG);
                if (userCouponEntities.size() == 0) {
                    resultCouList.add(couponEntity);
                }
            }
        }

        return R.ok().put("data", resultCouList);
    }


    /**
     * DISTRIBUTE
     * 批发商获取进货商品列表
     *
     * @param
     * @return 进货商品列表
     */
    @RequestMapping(value = "/getPurchaseGoodsGbWithTabCountWithNxDisId")
    @ResponseBody
    public R getPurchaseGoodsGbWithTabCountWithNxDisId(Integer depId, Integer disId, Integer appDepId, Integer nxDisId) {

        Map<String, Object> map4 = new HashMap<>();
//        map4.put("toDepId", depId);
        map4.put("buyStatus", 1);
        map4.put("disId", disId);
        map4.put("dayuBuyStatus", -2);
        map4.put("status", 3);
//        map4.put("shixianId", nxDisId);
        map4.put("type", 2);
        System.out.println("map444444444456666=====" + map4);
//        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryDisPurchaseGoods(map4);
        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryGbFatherDisPurchaseGoods(map4);
        List<GbDistributerPurchaseGoodsEntity> result = new ArrayList<>();
        if (purchaseToday.size() > 0) {
            for (GbDistributerFatherGoodsEntity fatherGoodsEntity : purchaseToday) {
                if (fatherGoodsEntity.getFatherGoodsEntities().size() > 0) {
                    for (GbDistributerFatherGoodsEntity fatherGoodsEntity1 : fatherGoodsEntity.getFatherGoodsEntities()) {
//                        List<GbDistributerPurchaseGoodsEntity> gbDistributerGoodsEntities = fatherGoodsEntity1.getGbDistributerPurchaseGoodsEntities();
//                        result.addAll(gbDistributerGoodsEntities);
                    }
                }
            }
        }


        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 1);
        map1.put("orderType", 2);
        map1.put("equalBuyStatus", 0);
        System.out.println("mapp111" + map1);
        int purCount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map1);

        map1.put("orderType", null);
        map1.put("notEqualOrderType", 5);
        map1.put("status", 4);
        map1.put("equalBuyStatus", null);
        map1.put("dayuBuyStatus", 0);
        map1.put("buyStatus", 5);
        System.out.println("mapp111oneoeneoene" + map1);
        int purCountOne = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map1);
        map1.put("status", 4);
        map1.put("dayuBuyStatus", null);
        map1.put("notEqualOrderType", 2);
        map1.put("orderType", 5);
        int count2 = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map1);


        //生成优惠卷
        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(disId);

        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDisId);
        map.put("equalStatus", 0);
        map.put("cityId", gbDistributerEntity.getGbDistributerSysCityId());
        System.out.println("coupeoneoemap" + map);
        List<NxDistributerCouponEntity> couponEntities = nxDistributerCouponService.queryLoadDownListByParams(map);
        List<NxDistributerCouponEntity> resultCouList = new ArrayList<>();
        if (couponEntities.size() > 0) {
            for (NxDistributerCouponEntity couponEntity : couponEntities) {
                Map<String, Object> mapG = new HashMap<>();
                mapG.put("gbDisId", disId);
                mapG.put("nxDisId", nxDisId);
                mapG.put("couponId", couponEntity.getNxDistributerCouponId());
                System.out.println("c" + mapG);
                List<NxGbDistibuterUserCouponEntity> userCouponEntities = nxGbDistibuterUserCouponService.queryGbCouponListByParams(mapG);
                if (userCouponEntities.size() == 0) {

                    resultCouList.add(couponEntity);
                }
            }
        }

        Map<String, Object> mapNG = new HashMap<>();
        mapNG.put("gbDisId", disId);
        mapNG.put("nxDisId", nxDisId);
        NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDistributerGbDistributerService.queryObjectByParams(mapNG);
        if (nxDistributerGbDistributerEntity == null) {
            NxDistributerGbDistributerEntity entity = new NxDistributerGbDistributerEntity();
            entity.setNxDgdNxDistributerId(Integer.valueOf(nxDisId));
            entity.setNxDgdGbDistributerId(disId);
            entity.setNxDgdGbPayMethod(0);
            entity.setNxDgdGbPayPeriodWeek(4);
            entity.setNxDgdStatus(0);
            entity.setNxDgdFromNxDisId(Integer.valueOf(nxDisId));
            entity.setNxDgdFromNxDepId(-1);
            entity.setNxDgdNxSupplierId(-1);
            entity.setNxDgdGbGoodsPrice(0);
            Map<String, Object> mapN = new HashMap<>();
            mapN.put("disId", disId);
            mapN.put("goodsType", getGbDepartmentTypeAppSupplier());
            List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(mapN);
            entity.setNxDgdGbDepId(departmentEntities.get(0).getGbDepartmentId());
            nxDistributerGbDistributerService.save(entity);

        }

        Map<String, Object> map3 = new HashMap<>();
        map3.put("arr", result);
        map3.put("orderAmount", purCount);
        map3.put("wxAmount", purCountOne);
        map3.put("appAmount", count2);
        map3.put("couponList", resultCouList);
        map3.put("disInfo", gbDistributerService.queryDistributerInfo(disId));
        return R.ok().put("data", map3);
    }


    @RequestMapping(value = "/gbGetCouponListBySalesUserId", method = RequestMethod.POST)
    @ResponseBody
    public R gbGetCouponListBySalesUserId(Integer gbDisId, Integer salesUserId, Integer nxDisId, String ids) {

        System.out.println("ids" + ids);
        String[] split = ids.split(",");
        if (split.length > 0) {
            for (String id : split) {
                NxDistributerCouponEntity couponEntity = nxDistributerCouponService.queryObject(Integer.valueOf(id));

                NxGbDistibuterUserCouponEntity userCouponEntity = new NxGbDistibuterUserCouponEntity();
                userCouponEntity.setNxGducCouponId(couponEntity.getNxDistributerCouponId());
                userCouponEntity.setNxGducGbDistributerId(gbDisId);
                userCouponEntity.setNxGducNxDistributerId(Integer.valueOf(nxDisId));
                userCouponEntity.setNxGducStatus(0);
                userCouponEntity.setNxGducStartDate(formatWhatDay(couponEntity.getNxDcStartWhichDay()));
                userCouponEntity.setNxGducStopDate(formatWhatDay(couponEntity.getNxDcStopWhichDay()));

                String startDate = formatWhatDay(couponEntity.getNxDcStartWhichDay());
                String stopDate = formatWhatDay(couponEntity.getNxDcStopWhichDay());
                String couponStartTime = "00:00:00";
                String couponStopTime = "23:59:59";
//
                String replaceStart = couponStartTime.replace(":", "-");
                String replaceStop = couponStopTime.replace(":", "-");
                String start = startDate + "-" + replaceStart;
                String stop = stopDate + "-" + replaceStop;
                System.out.println("dadfaf" + start + "stop=====" + stop);

                String[] splitStart = start.split("-");
                int year = Integer.parseInt(splitStart[0]);
                int month = Integer.parseInt(splitStart[1]);
                int day = Integer.parseInt(splitStart[2]);
                int hour = Integer.parseInt(splitStart[3]);
                int minute = Integer.parseInt(splitStart[4]);
                int haomiao = Integer.parseInt(splitStart[5]);
                LocalDateTime beginTime = LocalDateTime.of(year, month, day, hour, minute, haomiao);

                String[] splitStop = stop.split("-");
                int yearS = Integer.parseInt(splitStop[0]);
                int monthS = Integer.parseInt(splitStop[1]);
                int dayS = Integer.parseInt(splitStop[2]);
                int hourS = Integer.parseInt(splitStop[3]);
                int minuteS = Integer.parseInt(splitStop[4]);
                int haomiaoS = Integer.parseInt(splitStop[5]);
                LocalDateTime stopTime = LocalDateTime.of(yearS, monthS, dayS, hourS, minuteS, haomiaoS);
                System.out.println("adafasd" + beginTime + "stttt" + stopTime);

                userCouponEntity.setNxGducStartTimeZone(beginTime);
                userCouponEntity.setNxGducStopTimeZone(stopTime);
                userCouponEntity.setNxGducFromShareUserId(salesUserId);
                userCouponEntity.setNxGducShareTime(formatWhatDayTime(0));
                nxGbDistibuterUserCouponService.save(userCouponEntity);

                couponEntity.setNxDcDownCount(couponEntity.getNxDcDownCount() + 1);
                nxDistributerCouponService.update(couponEntity);
            }
        }

        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDisId);
        gbDistributerEntity.setGbDistributerBusinessType(11);
        gbDistributerEntity.setGbDistributerNxDisId(nxDisId);
        gbDistributerService.update(gbDistributerEntity);
        return R.ok();
    }


    /**
     * DISTRIBUTE
     * 批发商获取进货商品列表
     *
     * @param
     * @return 进货商品列表
     */
    @RequestMapping(value = "/getPurchaseGoodsGbWithTabCount/{disId}")
    @ResponseBody
    public R getPurchaseGoodsGbWithTabCount(@PathVariable Integer disId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("orderStatus", 3);
        map4.put("orderEqualBuyStatus", 0);
        map4.put("supplierBuy", -1);
        System.out.println("map4444444whyyyy111" + map4);
        List<GbDistributerPurchaseGoodsEntity> purchaseToday = gbDpgService.querySimplePurGoods(map4);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        map1.put("equalBuyStatus", 0);
        map1.put("notEqualOrderType", 9);
        System.out.println("mapp111aaaaaaa" + map1);

        
        int purCount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map1);

        map1.put("equalBuyStatus", null);
        map1.put("dayuBuyStatus", 0);
        map1.put("dayuStatus", -2);
        System.out.println("mapp111oneoeneoene11111" + map1);
        int purCountOne = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map1);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("arr", purchaseToday);
        map3.put("orderAmount", purCount);
        map3.put("wxAmount", purCountOne);
        map3.put("disInfo", gbDistributerService.queryDistributerInfo(disId));

        return R.ok().put("data", map3);
    }

    private Map<String, Object> packageDisOrderByDep(List<GbDepartmentEntity> departmentEntities, Integer which) {
        Map<String, Object> map = new HashMap<>();
        map.put("week", getWeek(which));
        map.put("hao", getJustHao(which));
        //根据部门是否有子部门组装部门订单
        //1,返回list
        List<Map<String, Object>> dataMap = new ArrayList<>();
        //2,有子部门的父部门
        TreeSet<GbDepartmentEntity> fatherDep = new TreeSet<>();
        //3，type是1的部门
        List<GbDepartmentEntity> subDepList = new ArrayList<>();


        for (GbDepartmentEntity dep : departmentEntities) {

            Integer fatherId = dep.getGbDepartmentFatherId();
            if (fatherId.equals(0)) {
                Map<String, Object> depMap = new HashMap<>();
                depMap.put("depHasSubs", 0);
                depMap.put("depId", dep.getGbDepartmentId());
                depMap.put("depName", dep.getGbDepartmentName());
                depMap.put("arrName", dep.getGbDepartmentAttrName());
                depMap.put("depOrders", dep.getGbDepartmentOrdersEntities());
                depMap.put("orderTotal", dep.getGbDepartmentOrdersEntities().size());
                depMap.put("choiceTotal", dep.getGbDepartmentOrdersEntities().size());
                dataMap.add(depMap);
            } else {
                Integer gbDepartmentFatherId = dep.getGbDepartmentFatherId();
                GbDepartmentEntity departmentEntity1 = gbDepartmentService.queryObject(gbDepartmentFatherId);
                fatherDep.add(departmentEntity1);
                subDepList.add(dep);
            }
        }

        for (GbDepartmentEntity father : fatherDep) {
            Map<String, Object> fatherMap = new HashMap<>();
            fatherMap.put("depHasSubs", 1);
            fatherMap.put("depId", father.getGbDepartmentId());
            fatherMap.put("depName", father.getGbDepartmentName());
            fatherMap.put("arrName", father.getGbDepartmentAttrName());

            List<GbDepartmentEntity> subDeps = new ArrayList<>();
            for (GbDepartmentEntity sub : subDepList) {
                if (father.getGbDepartmentId().equals(sub.getGbDepartmentFatherId())) {
                    subDeps.add(sub);
                }
            }
            fatherMap.put("subDeps", subDeps);
            dataMap.add(fatherMap);
        }
        map.put("arr", dataMap);
        return map;
    }


    @RequestMapping(value = "/markGbPurGoodsFinish", method = RequestMethod.POST)
    @ResponseBody
    public R markGbPurGoodsFinish(@RequestBody GbDistributerPurchaseGoodsEntity purgoods) {

        List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = purgoods.getGbDistributerGoodsEntity().getGbDepartmentOrdersEntities();
        List<GbDepartmentOrdersEntity> unChoiceOrderList = new ArrayList<>();
        int finishOrder = 0;
        BigDecimal purQuantity = new BigDecimal(0);
        for (GbDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            Boolean hasChoice = orders.getHasChoice();
            if (hasChoice) {
                finishOrder = finishOrder + 1;
                purQuantity = purQuantity.add(new BigDecimal(orders.getGbDoQuantity()).setScale(1, BigDecimal.ROUND_HALF_UP));
                orders.setGbDoStatus(getGbOrderStatusReceived());
                orders.setGbDoBuyStatus(getGbOrderBuyStatusHavePayFinish());
                orders.setGbDoPurchaseUserId(purgoods.getGbDpgPurUserId());
                gbDepartmentOrdersService.update(orders);

            } else {
                unChoiceOrderList.add(orders);
            }
        }

        purgoods.setGbDpgQuantity(purQuantity.toString());
        purgoods.setGbDpgOrdersAmount(finishOrder);
        purgoods.setGbDpgBuySubtotal("0");
        purgoods.setGbDpgBatchId(-1);
        purgoods.setGbDpgStatus(3);
        purgoods.setGbDpgPayType(-1);
        purgoods.setGbDpgTime(formatWhatTime(0));
        purgoods.setGbDpgPurchaseDate(formatWhatDay(0));
        purgoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
        purgoods.setGbDpgPurchaseYear(formatWhatYear(0));
        purgoods.setGbDpgPurchaseWeek(getWeek(0));
        purgoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
        purgoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
        Integer gbDoDisGoodsId = purgoods.getGbDpgDisGoodsId();

        //判断是否有保鲜时间参数
        GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
            int warnHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWarnHour());
            int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
            purgoods.setGbDpgWarnFullTime(formatWhatFullTime(warnHour));
            purgoods.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
        }
        gbDpgService.update(purgoods);

//        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(purgoods.getGbDpgDistributerId());
//        BigDecimal subtract = new BigDecimal(gbDistributerEntity.getGbDistributerBuyQuantity()).subtract(new BigDecimal(finishOrder));
//        gbDistributerEntity.setGbDistributerBuyQuantity(subtract.toString());
//        gbDistributerService.update(gbDistributerEntity);


        //给没有选择订单新生成一个采购商品
        if (unChoiceOrderList.size() > 0) {
            GbDistributerPurchaseGoodsEntity newPurGoods = new GbDistributerPurchaseGoodsEntity();
            newPurGoods.setGbDpgDistributerId(purgoods.getGbDpgDistributerId());
            newPurGoods.setGbDpgPayType(0);
            newPurGoods.setGbDpgPurchaseNxDistributerId(-1);
            newPurGoods.setGbDpgDisGoodsGrandId(purgoods.getGbDpgDisGoodsGrandId());
            newPurGoods.setGbDpgDisGoodsGreatId(purgoods.getGbDpgDisGoodsGreatId());
            newPurGoods.setGbDpgDisGoodsFatherId(unChoiceOrderList.get(0).getGbDoDisGoodsFatherId());
            newPurGoods.setGbDpgDisGoodsId(unChoiceOrderList.get(0).getGbDoDisGoodsId());
            newPurGoods.setGbDpgDistributerId(unChoiceOrderList.get(0).getGbDoDistributerId());
            newPurGoods.setGbDpgApplyDate(formatWhatDay(0));
            newPurGoods.setGbDpgStatus(0);
            newPurGoods.setGbDpgOrdersAmount(unChoiceOrderList.size());
            newPurGoods.setGbDpgOrdersFinishAmount(0);
            newPurGoods.setGbDpgOrdersWeightAmount(0);
            newPurGoods.setGbDpgOrdersBillAmount(0);
            newPurGoods.setGbDpgPurchaseWeek(getWeek(0));
            newPurGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            newPurGoods.setGbDpgIsCheck(0);
            newPurGoods.setGbDpgPurchaseDepartmentId(unChoiceOrderList.get(0).getGbDoToDepartmentId());
            newPurGoods.setGbDpgPurchaseNxDistributerId(unChoiceOrderList.get(0).getGbDoNxDistributerId());
            gbDpgService.save(newPurGoods);
            BigDecimal unPurQuantity = new BigDecimal(0);
            for (GbDepartmentOrdersEntity unChoiceOrder : unChoiceOrderList) {
                Integer gbDistributerPurchaseGoodsId = newPurGoods.getGbDistributerPurchaseGoodsId();
                unChoiceOrder.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                gbDepartmentOrdersService.update(unChoiceOrder);
                BigDecimal orderQuantity = new BigDecimal(unChoiceOrder.getGbDoQuantity());
                unPurQuantity = unPurQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);

            }
            newPurGoods.setGbDpgQuantity(unPurQuantity.toString());
            newPurGoods.setGbDpgStandard(unChoiceOrderList.get(0).getGbDoStandard());
            gbDpgService.update(newPurGoods);
        }

        return R.ok();
    }


    @RequestMapping(value = "/updateStockPurGoodsPrice", method = RequestMethod.POST)
    @ResponseBody
    public R updateStockPurGoodsPrice(Integer id, String price) {
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryObject(id);
        purchaseGoodsEntity.setGbDpgBuyPrice(price);
        if (purchaseGoodsEntity.getGbDpgBuyQuantity() != null && !purchaseGoodsEntity.getGbDpgBuyQuantity().trim().isEmpty()) {
            BigDecimal quantityB = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
            BigDecimal multiply = quantityB.multiply(new BigDecimal(price)).setScale(1, BigDecimal.ROUND_HALF_UP);
            purchaseGoodsEntity.setGbDpgBuySubtotal(multiply.toString());
            purchaseGoodsEntity.setGbDpgStatus(3);
        }
        gbDpgService.update(purchaseGoodsEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("purGoodsId", id);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
        for (GbDepartmentOrdersEntity orders : ordersEntities) {
            orders.setGbDoPrice(price);
            if (orders.getGbDoWeight() != null && !orders.getGbDoWeight().trim().isEmpty()) {
                BigDecimal weightB = new BigDecimal(orders.getGbDoWeight());
                BigDecimal multiply = weightB.multiply(new BigDecimal(price)).setScale(1, BigDecimal.ROUND_HALF_UP);
                orders.setGbDoSubtotal(multiply.toString());
                orders.setGbDoStatus(getGbOrderStatusHasFinished());
                orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
            }
            gbDepartmentOrdersService.update(orders);

        }

        Integer gbDpgDisGoodsId = purchaseGoodsEntity.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purchaseGoodsEntity);
        }

        return R.ok();
    }


    @RequestMapping(value = "/finishPurGoodsToStock", method = RequestMethod.POST)
    @ResponseBody
    public R finishPurGoodsToStock(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {
        List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = purGoods.getGbDistributerGoodsEntity().getGbDepartmentOrdersEntities();
        List<GbDepartmentOrdersEntity> unChoiceOrderList = new ArrayList<>();
        int finishOrder = 0;
        for (GbDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            Boolean hasChoice = orders.getIsNotice();
            System.out.println("isisiis" + hasChoice);
            if (hasChoice) {
                finishOrder = finishOrder + 1;
                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(orders.getGbDepartmentOrdersId());

                gbDepartmentOrdersEntity.setGbDoPrice(orders.getGbDoPrice());
                gbDepartmentOrdersEntity.setGbDoWeight(orders.getGbDoWeight());
                gbDepartmentOrdersEntity.setGbDoSubtotal(orders.getGbDoSubtotal());
                gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusReceived());
                gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHavePayFinish());
                gbDepartmentOrdersEntity.setGbDoPurchaseUserId(purGoods.getGbDpgPurUserId());
                gbDepartmentOrdersEntity.setGbDoArriveDate(formatWhatDay(0));
                gbDepartmentOrdersEntity.setGbDoArriveOnlyDate(formatWhatDate(0));
                gbDepartmentOrdersEntity.setGbDoApplyFullTime(formatFullTime());
                gbDepartmentOrdersEntity.setGbDoArriveWhatDay(getWeek(0));
                gbDepartmentOrdersEntity.setGbDoApplyOnlyTime(formatWhatTime(0));
                gbDepartmentOrdersEntity.setGbDoArriveWeeksYear(getWeekOfYear(0));

                gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);

            } else {
                System.out.println("unchoiciciic" +  orders.getGbDepartmentOrdersId());
                unChoiceOrderList.add(orders);
            }

        }

        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryObject(purGoods.getGbDistributerPurchaseGoodsId());

        purchaseGoodsEntity.setGbDpgBuyPrice(purGoods.getGbDpgBuyPrice());
        purchaseGoodsEntity.setGbDpgBuyQuantity(purGoods.getGbDpgBuyQuantity());
        purchaseGoodsEntity.setGbDpgBuySubtotal(purGoods.getGbDpgBuySubtotal());
        purchaseGoodsEntity.setGbDpgPurUserId(purGoods.getGbDpgPurUserId());
        purchaseGoodsEntity.setGbDpgPurchaseDepartmentId(purGoods.getGbDpgPurchaseDepartmentId());
        purchaseGoodsEntity.setGbDpgBatchId(-1);
        purchaseGoodsEntity.setGbDpgStatus(4);
        purchaseGoodsEntity.setGbDpgPayType(0);
        purchaseGoodsEntity.setGbDpgPurchaseType(1);
        purchaseGoodsEntity.setGbDpgTime(formatWhatTime(0));
        purchaseGoodsEntity.setGbDpgPurchaseDate(formatWhatDay(0));
        purchaseGoodsEntity.setGbDpgStockFinishDate(formatWhatDay(0));
        purchaseGoodsEntity.setGbDpgPurchaseMonth(formatWhatMonth(0));
        purchaseGoodsEntity.setGbDpgPurchaseYear(formatWhatYear(0));
        purchaseGoodsEntity.setGbDpgPurchaseWeek(getWeek(0));
        purchaseGoodsEntity.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
        purchaseGoodsEntity.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
        Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);

        System.out.println("apbccccc" + gbDistributerGoodsEntity.getGbDgControlPrice());
        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purGoods);
        }

        //判断是否有保鲜时间参数
        Integer gbDoDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
            System.out.println("wasteeieiee" + gbDisGoodsEntity.getGbDgControlFresh());
            int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
            purchaseGoodsEntity.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
        }
        gbDpgService.update(purchaseGoodsEntity);


//        //给没有选择订单新生成一个采购商品
        System.out.println("unspsosos" + unChoiceOrderList);
        if (unChoiceOrderList.size() > 0) {
            Integer gbDepartmentOrdersId = unChoiceOrderList.get(0).getGbDepartmentOrdersId();
            GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
            GbDistributerPurchaseGoodsEntity disGoods = new GbDistributerPurchaseGoodsEntity();
            disGoods.setGbDpgDistributerId(purchaseGoodsEntity.getGbDpgDistributerId());
            disGoods.setGbDpgPayType(0);
            disGoods.setGbDpgPurchaseNxDistributerId(-1);
            disGoods.setGbDpgDisGoodsGrandId(purchaseGoodsEntity.getGbDpgDisGoodsGrandId());
            disGoods.setGbDpgDisGoodsGreatId(purchaseGoodsEntity.getGbDpgDisGoodsGreatId());
            disGoods.setGbDpgDisGoodsFatherId(ordersEntity.getGbDoDisGoodsFatherId());
            disGoods.setGbDpgDisGoodsId(ordersEntity.getGbDoDisGoodsId());
            disGoods.setGbDpgApplyDate(formatWhatDay(0));
            disGoods.setGbDpgStatus(0);
            disGoods.setGbDpgBuyScale("-1");
            disGoods.setGbDpgStandard(ordersEntity.getGbDoStandard());
            disGoods.setGbDpgOrdersAmount(unChoiceOrderList.size());
            disGoods.setGbDpgOrdersFinishAmount(0);
            disGoods.setGbDpgOrdersWeightAmount(0);
            disGoods.setGbDpgOrdersBillAmount(0);
            disGoods.setGbDpgPurchaseWeek(getWeek(0));
            disGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            disGoods.setGbDpgIsCheck(0);
            disGoods.setGbDpgPurchaseType(1);
            disGoods.setGbDpgPurchaseNxSupplierId(-1);
            gbDpgService.save(disGoods);
            for (GbDepartmentOrdersEntity unChoiceOrder : unChoiceOrderList) {
                Integer gbDistributerPurchaseGoodsId = disGoods.getGbDistributerPurchaseGoodsId();
                Integer gbDepartmentOrdersId1 = unChoiceOrder.getGbDepartmentOrdersId();
                GbDepartmentOrdersEntity ordersEntity1 = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId1);
                ordersEntity1.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                gbDepartmentOrdersService.update(ordersEntity1);

                BigDecimal purQuantity = new BigDecimal(disGoods.getGbDpgQuantity());
                BigDecimal orderQuantity = new BigDecimal(ordersEntity1.getGbDoQuantity());
                BigDecimal add = purQuantity.add(orderQuantity).setScale(2, BigDecimal.ROUND_HALF_UP);
                disGoods.setGbDpgQuantity(add.toString());
                if(gbDistributerGoodsEntity.getGbDgGoodsStandardname().equals(ordersEntity.getGbDoStandard())){
                    disGoods.setGbDpgBuyQuantity(add.toString());
                }
                gbDpgService.update(disGoods);
            }

        }

        Map<String, Object> map = new HashMap<>();
        map.put("purGoodsId", purGoods.getGbDistributerPurchaseGoodsId());
        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
        saveDepStockDataByPurchase(gbDepartmentOrdersEntities, purGoods.getGbDistributerPurchaseGoodsId());

        return R.ok();
    }


    /**
     * DISTRIBUTE
     * 批发商获取进货商品列表
     *
     * @param
     * @return 进货商品列表
     */
    @RequestMapping(value = "/getStockPurchaseGoodsGb/{depId}")
    @ResponseBody
    public R getStockPurchaseGoodsGb(@PathVariable Integer depId) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("depFatherId", depId);
        map4.put("status", 3);
        map4.put("toDepId", depId);
        //
        System.out.println("mapdpdifda" + map4);
        List<GbDistributerGoodsShelfEntity> shelfEntities = gbDisGoodsShelfService.queryStockOrdersByParams(map4);
        List<GbDistributerGoodsShelfGoodsEntity> shelfGoodsEntities = new ArrayList<>();
        List<GbDistributerGoodsShelfEntity> shelfEntityList = new ArrayList<>();
        GbDistributerGoodsShelfEntity shelfGoodsEntity = new GbDistributerGoodsShelfEntity();
        for (GbDistributerGoodsShelfEntity shelf : shelfEntities) {
            Integer gbDistributerGoodsShelfId = shelf.getGbDistributerGoodsShelfId();
            if (gbDistributerGoodsShelfId == null) {
                shelfGoodsEntities.add(shelf.getGbDisGoodsShelfGoodsEntities().get(0));
            } else {
                shelfEntityList.add(shelf);
            }
        }
        if (shelfGoodsEntities.size() > 0) {
            shelfGoodsEntity.setGbDisGoodsShelfGoodsEntities(shelfGoodsEntities);
            shelfEntityList.add(shelfGoodsEntity);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("status", 1);
        map.put("type", 2);
        int count = gbDistributerWeightTotalService.queryDepWeightCountByParams(map);


        Map<String, Object> map1 = new HashMap<>();
        map1.put("arr", shelfEntityList);
        map1.put("weightTotal", count);

        Map<String, Object> map4B = new HashMap<>();
        map4B.put("depId", depId);
        map4B.put("equalStatus", -1);
        System.out.println("abdkdkdkdd" + map4B);
        List<GbDepartmentBillEntity> billEntities1 = gbDepartmentBillService.queryBillFromWhichDepartment(map4B);
        map1.put("receiveBills", billEntities1);


        return R.ok().put("data", map1);
    }

    public R saveDepStockDataByPurchase(List<GbDepartmentOrdersEntity> ordersEntityList, Integer purGoodsId) {
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryObject(purGoodsId);

        for (GbDepartmentOrdersEntity order : ordersEntityList) {


            System.out.println("upddodididufidfuaisf");
            Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
            Integer gbDoDepDisGoodsId = order.getGbDoDepDisGoodsId();
            GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDoDepDisGoodsId);
            GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
            Integer gbDoStatus = ordersEntity.getGbDoStatus();
            //判断没有被别人收货
            //0,修改订单上次价格涨幅
            if (departmentDisGoodsEntity.getGbDdgOrderDate() != null && !departmentDisGoodsEntity.getGbDdgOrderDate().trim().isEmpty()) {
                if (order.getGbDoPrice() != null && !order.getGbDoPrice().trim().isEmpty()) {
                    BigDecimal decimal = new BigDecimal(departmentDisGoodsEntity.getGbDdgOrderPrice());
                    BigDecimal decimal1 = new BigDecimal(order.getGbDoPrice());
                    BigDecimal subtract1 = decimal1.subtract(decimal);
                    order.setGbDoPriceDifferent(subtract1.toString());
                } else {
                    order.setGbDoPriceDifferent("0");
                }
            }
            GbDepartmentGoodsStockEntity stockEntity = new GbDepartmentGoodsStockEntity();
            stockEntity.setGbDgsGbDepartmentId(order.getGbDoDepartmentId());
            stockEntity.setGbDgsGbDepartmentFatherId(order.getGbDoDepartmentFatherId());
            stockEntity.setGbDgsGbPurGoodsId(order.getGbDoPurchaseGoodsId());
            stockEntity.setGbDgsGbDistributerId(order.getGbDoDistributerId());
            stockEntity.setGbDgsWeight(order.getGbDoWeight());
            stockEntity.setGbDgsPrice(order.getGbDoPrice());
            stockEntity.setGbDgsSubtotal(order.getGbDoSubtotal());
            stockEntity.setGbDgsRestWeight(order.getGbDoWeight());
            stockEntity.setGbDgsRestSubtotal(order.getGbDoSubtotal());
            stockEntity.setGbDgsGbDisGoodsId(order.getGbDoDisGoodsId());
            stockEntity.setGbDgsNxSupplierId(-1);
            stockEntity.setGbDgsPurUserId(purchaseGoodsEntity.getGbDpgPurUserId());
            stockEntity.setGbDgsNxDistributerId(-1);

            Integer gbDoDisGoodsId = order.getGbDoDisGoodsId();
            GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
            stockEntity.setGbDgsGbDisGoodsFatherId(goodsEntity.getGbDgDfgGoodsFatherId());
            stockEntity.setGbDgsGbDisGoodsGrandId(goodsEntity.getGbDgDfgGoodsGrandId());
            stockEntity.setGbDgsGbDisGoodsGreatId(goodsEntity.getGbDgDfgGoodsGreatId());
            stockEntity.setGbDgsGbDepDisGoodsId(order.getGbDoDepDisGoodsId());
            stockEntity.setGbDgsDate(formatWhatDay(0));
            stockEntity.setGbDgsTimeStamp(getTimeStamp());
            stockEntity.setGbDgsWeek(getWeek(0));
            stockEntity.setGbDgsMonth(formatWhatMonth(0));
            stockEntity.setGbDgsYear(formatWhatYear(0));
            stockEntity.setGbDgsFullTime(formatFullTime());
            stockEntity.setGbDgsLossWeight("0");
            stockEntity.setGbDgsLossSubtotal("0");
            stockEntity.setGbDgsReturnWeight("0");
            stockEntity.setGbDgsReturnSubtotal("0");
            stockEntity.setGbDgsProduceWeight("0");
            stockEntity.setGbDgsProduceSubtotal("0");
            stockEntity.setGbDgsWasteWeight("0");
            stockEntity.setGbDgsWasteSubtotal("0");

            stockEntity.setGbDgsAfterProfitSubtotal("0");
            stockEntity.setGbDgsBetweenPrice("0");
            stockEntity.setGbDgsCostRate("0");
            stockEntity.setGbDgsSellingSubtotal("0");
            stockEntity.setGbDgsProduceSellingSubtotal("0");
            stockEntity.setGbDgsProfitSubtotal("0");
            stockEntity.setGbDgsProfitWeight("0");
            stockEntity.setGbDgsSellingPrice("-1");

//        // showStandard
            if (departmentDisGoodsEntity.getGbDdgShowStandardId() != -1) {
                String gbDdgShowStandardScale = departmentDisGoodsEntity.getGbDdgShowStandardScale();
                BigDecimal divide = new BigDecimal(order.getGbDoWeight()).divide(new BigDecimal(gbDdgShowStandardScale), 1, BigDecimal.ROUND_HALF_UP);
                stockEntity.setGbDgsRestWeightShowStandard(divide.toString());
                stockEntity.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
            }

            //判断是否有保鲜时间参数
            if (order.getGbDoPurchaseGoodsId() != -1) {
                if (purchaseGoodsEntity.getGbDpgWasteFullTime() != null && !purchaseGoodsEntity.getGbDpgWasteFullTime().trim().isEmpty()) {
                    stockEntity.setGbDgsWasteFullTime(purchaseGoodsEntity.getGbDpgWasteFullTime());
                    String gbDpgWasteFullTime = purchaseGoodsEntity.getGbDpgWasteFullTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    // 设置日期字符串
                    // 解析日期字符串为Date对象
                    Date dateWaste = null;
                    try {
                        if (gbDpgWasteFullTime != null && !gbDpgWasteFullTime.trim().isEmpty()) {
                            dateWaste = dateFormat.parse(gbDpgWasteFullTime);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    // 获取时间戳
                    long timestampWaste = 0;
                    long timestampWarn = 0;
                    if (dateWaste != null) {
                        timestampWaste = dateWaste.getTime();
                    }
                    // 输出时间戳
                    stockEntity.setGbDgsWasteTimeQuantumName(String.valueOf(timestampWaste));
                }
                //判断是否价格异常商品
                if (purchaseGoodsEntity.getGbDpgDisGoodsPriceId() != null) {
                    GbDistributerGoodsPriceEntity goodsPriceEntity = gbDistributerGoodsPriceService.queryObject(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
                    String doWeight = order.getGbDoWeight();
                    Integer gbDgpPurWhat = goodsPriceEntity.getGbDgpPurWhat();
                    String whatSubtotal = "";
                    if (gbDgpPurWhat == 1) {
                        String gbDgpGoodsHighestPrice = goodsPriceEntity.getGbDgpGoodsHighestPrice();
                        String purPrice = goodsPriceEntity.getGbDgpPurPrice();
                        BigDecimal diffPrice = new BigDecimal(purPrice).subtract(new BigDecimal(gbDgpGoodsHighestPrice));
                        BigDecimal subtotal = diffPrice.multiply(new BigDecimal(doWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                        whatSubtotal = subtotal.toString();
                    }
                    if (gbDgpPurWhat == -1) {
                        String lowestPrice = goodsPriceEntity.getGbDgpGoodsLowestPrice();
                        String purPrice = goodsPriceEntity.getGbDgpPurPrice();
                        BigDecimal diffPrice = new BigDecimal(purPrice).subtract(new BigDecimal(lowestPrice));
                        BigDecimal subtotal = diffPrice.multiply(new BigDecimal(doWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                        whatSubtotal = subtotal.toString();
                    }
                    //价控最低价的成本
                    //实际成本与最低成本的差价
                    stockEntity.setGbDgsGbPriceSubtotal(whatSubtotal); // 相差了多少成本
                    stockEntity.setGbDgsGbPriceGoodsId(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
                    stockEntity.setGbDgsGbPriceSubtotalScale(goodsPriceEntity.getGbDgpPurScale());

                }
            }

            stockEntity.setGbDgsStatus(0);
            stockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());
            stockEntity.setGbDgsGbGoodsStockId(-1);
            stockEntity.setGbDgsGbFromDepartmentId(order.getGbDoToDepartmentId());
            stockEntity.setGbDgsReceiveUserId(order.getGbDoReceiveUserId());
            stockEntity.setGbDgsInventoryDate(formatWhatDay(0));
            stockEntity.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
            stockEntity.setGbDgsInventoryMonth(formatWhatMonth(0));
            stockEntity.setGbDgsInventoryYear(formatWhatYear(0));
            stockEntity.setGbDgsStars(5);
            gbDepartmentGoodsStockService.save(stockEntity);

            orderAddDepDisGoods(order, stockEntity, order.getGbDoDepDisGoodsId());
            updateDepGoodsDailyBusiness(stockEntity);

            //2，修改订单状态
            order.setGbDoStatus(getGbOrderStatusReceived());
            gbDepartmentOrdersService.update(order);

            //3，修改送货单收货单子数量
            if (order.getGbDoPurchaseGoodsId() != -1) {
                BigDecimal gbPgOrderAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersAmount());
                BigDecimal gbDbFinishAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersFinishAmount());
                if (gbDbFinishAmount.add(new BigDecimal(1)).compareTo(gbPgOrderAmount) == 0) {
                    purchaseGoodsEntity.setGbDpgOrdersFinishAmount(purchaseGoodsEntity.getGbDpgOrdersAmount());
                    purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusWaitReceive());
                } else {
                    BigDecimal add = gbDbFinishAmount.add(new BigDecimal(1));
                    purchaseGoodsEntity.setGbDpgOrdersFinishAmount(add.intValue());
                }
                gbDpgService.update(purchaseGoodsEntity);
            }
        }
        return R.ok();
    }


    @RequestMapping(value = "/puraserReturnPurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R puraserReturnPurGoods(@RequestBody GbDistributerPurchaseGoodsEntity disPurGoodsEntity) {
        System.out.println("purgoodssssss" + disPurGoodsEntity);

        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = disPurGoodsEntity.getGbDistributerGoodsEntity().getGbDepartmentOrdersEntities();
        if (gbDepartmentOrdersEntities.size() > 0) {

            disPurGoodsEntity.setGbDpgPurchaseDate(formatWhatDay(0));
            disPurGoodsEntity.setGbDpgPurchaseMonth(formatWhatMonth(0));
            disPurGoodsEntity.setGbDpgPurchaseYear(formatWhatYear(0));
            disPurGoodsEntity.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
            disPurGoodsEntity.setGbDpgPurchaseWeek(getWeek(0));
            disPurGoodsEntity.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            disPurGoodsEntity.setGbDpgBatchId(-1);
            disPurGoodsEntity.setGbDpgPurchaseWeek(getWeek(0));
            disPurGoodsEntity.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            disPurGoodsEntity.setGbDpgStatus(4);
            disPurGoodsEntity.setGbDpgStockFinishDate(formatWhatDay(0));
            gbDpgService.update(disPurGoodsEntity);

            for (GbDepartmentOrdersEntity ordersEntity : gbDepartmentOrdersEntities) {
                Integer gbDoDgsrReturnId = ordersEntity.getGbDoDgsrReturnId();
                GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepartmentStockReduceService.queryObject(gbDoDgsrReturnId);
                reduceEntity.setGbDgsrStatus(0);
                gbDepartmentStockReduceService.update(reduceEntity);
                System.out.println("reee" + reduceEntity);

                ordersEntity.setGbDoStatus(4);
                ordersEntity.setGbDoBuyStatus(6);
                ordersEntity.setGbDoArriveDate(formatWhatDay(0));
                gbDepartmentOrdersService.update(ordersEntity);
            }
        }


        return R.ok();
    }

    private void orderAddDepDisGoods(GbDepartmentOrdersEntity ordersEntity, GbDepartmentGoodsStockEntity stockEntity, Integer depDisGoodsId) {

        BigDecimal stockSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());
        BigDecimal stockWeight = new BigDecimal(stockEntity.getGbDgsWeight());
        BigDecimal subTotal = new BigDecimal(0);
        BigDecimal weight = new BigDecimal(0);
        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).add(stockSubtotal);
        weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).add(stockWeight);
        //updateOrder
        depDisGoodsEntity.setGbDdgOrderDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgOrderPrice(ordersEntity.getGbDoPrice());
        depDisGoodsEntity.setGbDdgOrderQuantity(ordersEntity.getGbDoQuantity());
        depDisGoodsEntity.setGbDdgOrderRemark(ordersEntity.getGbDoRemark());
        depDisGoodsEntity.setGbDdgOrderStandard(ordersEntity.getGbDoStandard());
        depDisGoodsEntity.setGbDdgOrderWeight(ordersEntity.getGbDoWeight());


        if (new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale()).compareTo(new BigDecimal(0)) == 1) {
            BigDecimal showScale = new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale());
            BigDecimal standardWeight = weight.divide(showScale, 1, BigDecimal.ROUND_HALF_UP);
            depDisGoodsEntity.setGbDdgShowStandardWeight(standardWeight.toString());
        }

        depDisGoodsEntity.setGbDdgStockTotalSubtotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgStockTotalWeight(weight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgInventoryDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgInventoryFullTime(formatWhatFullTime(0));
        gbDepartmentDisGoodsService.update(depDisGoodsEntity);

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
            BigDecimal restSubtotal = new BigDecimal(depGoodsDailyItem.getGbDgdRestSubtotal());
            BigDecimal totalWeight = new BigDecimal(stock.getGbDgsWeight()).add(weight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalSubtotal = new BigDecimal(stock.getGbDgsSubtotal()).add(total).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalRestWeight = new BigDecimal(stock.getGbDgsWeight()).add(restWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalRestSubtotal = new BigDecimal(stock.getGbDgsSubtotal()).add(restSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            depGoodsDailyItem.setGbDgdWeight(totalWeight.toString());
            depGoodsDailyItem.setGbDgdSubtotal(totalSubtotal.toString());
            depGoodsDailyItem.setGbDgdRestWeight(totalRestWeight.toString());
            depGoodsDailyItem.setGbDgdRestSubtotal(totalRestSubtotal.toString());
            depGoodsDailyItem.setGbDgdSellClearHour("-1");
            depGoodsDailyItem.setGbDgdSellClearMinute("-1");
            depGoodsDailyItem.setGbDgdStatus(0);
            gbDepGoodsDailyService.update(depGoodsDailyItem);

        } else {
            GbDepartmentGoodsDailyEntity dailyEntity = new GbDepartmentGoodsDailyEntity();
            dailyEntity.setGbDgdGbDistributerId(stock.getGbDgsGbDistributerId());
            dailyEntity.setGbDgdGbDepartmentId(stock.getGbDgsGbDepartmentId());
            dailyEntity.setGbDgdGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            dailyEntity.setGbDgdGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
            dailyEntity.setGbDgdGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
            dailyEntity.setGbDgdGbDisGoodsGrandId(stock.getGbDgsGbDisGoodsGrandId());
            GbDistributerFatherGoodsEntity fatherGoodsEntity = gbDistributerFatherGoodsService.queryObject(stock.getGbDgsGbDisGoodsGrandId());
            dailyEntity.setGbDgdGbDisGoodsGreatGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
            dailyEntity.setGbDgdGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
            dailyEntity.setGbDgdDate(formatWhatDay(0));
            dailyEntity.setGbDgdWeek(getWeekOfYear(0).toString());
            dailyEntity.setGbDgdMonth(formatWhatMonth(0));
            dailyEntity.setGbDgdYear(formatWhatYear(0));
            dailyEntity.setGbDgdDay(getWeek(0));
            dailyEntity.setGbDgdWeight(stock.getGbDgsWeight());
            dailyEntity.setGbDgdRestWeight(stock.getGbDgsWeight());
            dailyEntity.setGbDgdRestSubtotal(stock.getGbDgsSubtotal());
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
            dailyEntity.setGbDgdLastSubtotal("0");
            dailyEntity.setGbDgdLastProduceWeight("0");
            Integer gbDgdGbDisGoodsId = dailyEntity.getGbDgdGbDisGoodsId();
            GbDistributerGoodsEntity distributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDgdGbDisGoodsId);
            if (distributerGoodsEntity.getGbDgControlFresh() == 1) {
                dailyEntity.setGbDgdFreshRate("100");
            } else {
                dailyEntity.setGbDgdFreshRate("0");
            }
            dailyEntity.setGbDgdFullTime(formatFullTime());
            dailyEntity.setGbDgdStatus(0);
            gbDepGoodsDailyService.save(dailyEntity);
        }
    }



    @RequestMapping(value = "/supplierGetUnWeightOutPurGoods/{batchId}")
    @ResponseBody
    public R supplierGetUnWeightOutPurGoods(@PathVariable Integer batchId) {
        Map<String, Object> map = new HashMap<>();
        map.put("batchId", batchId);
        map.put("status", getGbPurchaseGoodsStatusWeightFinished());
        System.out.println("unweieiieieiieieieaaaa222" + map);
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntityList = gbDpgService.queryPurchaseGoodsWithDetailByParams(map);
        return R.ok().put("data", purchaseGoodsEntityList);
    }


    /**
     * 修改
     */
    @ResponseBody
    @RequestMapping("/sellerUpdatePurGoods")
    public R sellerUpdatePurGoods(@RequestBody GbDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        //如果供货商填写了重量，则判断商品是否有保鲜时间参数
        if (purchaseGoodsEntity.getGbDpgBuyQuantity() != null && !purchaseGoodsEntity.getGbDpgBuyQuantity().trim().isEmpty()) {
            Integer gbDoDisGoodsId = purchaseGoodsEntity.getGbDpgDisGoodsId();
            GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
            if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
                int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
                purchaseGoodsEntity.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
            }
        }

        gbDpgService.update(purchaseGoodsEntity);
        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purchaseGoodsEntity.getGbDistributerGoodsEntity().getGbDepartmentOrdersEntities();
        for (GbDepartmentOrdersEntity orders : gbDepartmentOrdersEntities) {
            System.out.println("wieieieieiieieiieie" + orders.getGbDoWeight());
            if(orders.getGbDoWeight() != null && !orders.getGbDoWeight().trim().isEmpty() && !orders.getGbDoWeight().equals("0.0")){
                BigDecimal decimal1 = new BigDecimal(orders.getGbDoPrice());
                BigDecimal decimal2 = new BigDecimal(orders.getGbDoWeight());
                BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                orders.setGbDoSubtotal(decimal3.toString());
                orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
            }else{
                orders.setGbDoBuyStatus(getGbOrderBuyStatusPrepareing());
            }
            gbDepartmentOrdersService.update(orders);
        }

        Map<String, Object> map = new HashMap<>();
        Integer gbDpgBatchId = purchaseGoodsEntity.getGbDpgBatchId();
        map.put("batchId", gbDpgBatchId);
        Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(map);
        GbDistributerPurchaseBatchEntity batchEntity = gbDPBService.queryObject(gbDpgBatchId);
        batchEntity.setGbDpbSubtotal(String.format("%.1f", subTotal));
        gbDPBService.update(batchEntity);

        return R.ok().put("data", purchaseGoodsEntity);

    }

    private GbDistributerPurchaseGoodsEntity checkPurGoodsPrice(GbDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        System.out.println("checkkckGoododopriidd" + purchaseGoodsEntity.getGbDpgDisGoodsId());
        Integer gbDpgDisGoodsId = purchaseGoodsEntity.getGbDpgDisGoodsId();
        BigDecimal buyPrice = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
        Integer gbDpgDisGoodsPriceId = purchaseGoodsEntity.getGbDpgDisGoodsPriceId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);

        BigDecimal weight = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
        BigDecimal goodsHighest = new BigDecimal(gbDistributerGoodsEntity.getGbDgGoodsHighestPrice());
        BigDecimal goodsLowest = new BigDecimal(gbDistributerGoodsEntity.getGbDgGoodsLowestPrice());
        String priceTotal = buyPrice.multiply(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString();


        if (buyPrice.compareTo(goodsHighest) == 1 && purchaseGoodsEntity.getGbDpgBuyQuantity() != null && !purchaseGoodsEntity.getGbDpgBuyQuantity().trim().isEmpty()) { //高于最高价

            BigDecimal higherWhatPrice = buyPrice.subtract(goodsHighest);

            BigDecimal highertotal = higherWhatPrice.multiply(weight).setScale(2, BigDecimal.ROUND_HALF_UP); //高出的单价部分 * 重量
            BigDecimal multiply = higherWhatPrice.divide(goodsHighest, 4, BigDecimal.ROUND_HALF_DOWN); // 高出的单价部门 除以 正常价格
            BigDecimal highestTotal = goodsHighest.multiply(weight).setScale(1, BigDecimal.ROUND_HALF_UP);

            if (gbDpgDisGoodsPriceId != null) {
                GbDistributerGoodsPriceEntity goodsPriceEntity = gbDistributerGoodsPriceService.queryObject(gbDpgDisGoodsPriceId);
                goodsPriceEntity.setGbDgpPurWhat(1);
                goodsPriceEntity.setGbDgpPurPrice(purchaseGoodsEntity.getGbDpgBuyPrice());
                goodsPriceEntity.setGbDgpPurWeight(purchaseGoodsEntity.getGbDpgBuyQuantity());
                goodsPriceEntity.setGbDgpPurScale(multiply.toString());
                goodsPriceEntity.setGbDgpPurWhatTotal(highertotal.toString());
                goodsPriceEntity.setGbDgpGoodsHighestTotal(highestTotal.toString());
                goodsPriceEntity.setGbDgpPurTotal(priceTotal);
                goodsPriceEntity.setGbDgpPurWeight(purchaseGoodsEntity.getGbDpgBuyQuantity());
                goodsPriceEntity.setGbDgpGoodsLowestTotal("0");
                goodsPriceEntity.setGbDgpPurDepartmentId(purchaseGoodsEntity.getGbDpgPurchaseDepartmentId());
                goodsPriceEntity.setGbDgpPurNxDistributerId(gbDistributerGoodsEntity.getGbDgNxDistributerId());
                goodsPriceEntity.setGbDgpGoodsPrice(gbDistributerGoodsEntity.getGbDgGoodsPrice());
                goodsPriceEntity.setGbDgpGoodsLowestPrice(gbDistributerGoodsEntity.getGbDgGoodsLowestPrice());
                goodsPriceEntity.setGbDgpGoodsHighestPrice(gbDistributerGoodsEntity.getGbDgGoodsHighestPrice());
                gbDistributerGoodsPriceService.update(goodsPriceEntity);
            } else {
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
                gbDistributerGoodsPriceService.save(goodsPriceEntity);
                purchaseGoodsEntity.setGbDpgDisGoodsPriceId(goodsPriceEntity.getGbDistributerGoodsPriceId());
            }
            gbDpgService.update(purchaseGoodsEntity);
        }

        if (buyPrice.compareTo(goodsLowest) == -1 && purchaseGoodsEntity.getGbDpgBuyQuantity() != null && !purchaseGoodsEntity.getGbDpgBuyQuantity().trim().isEmpty()) { //低于最低价
            BigDecimal lowerWhatPrice = goodsLowest.subtract(buyPrice);
            BigDecimal lowerTotal = lowerWhatPrice.multiply(weight).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal multiply = lowerWhatPrice.divide(goodsLowest, 4, BigDecimal.ROUND_HALF_DOWN);
            BigDecimal lowestTotal = goodsLowest.multiply(weight).setScale(1, BigDecimal.ROUND_HALF_UP);

            if (gbDpgDisGoodsPriceId != null) {
                GbDistributerGoodsPriceEntity goodsPriceEntity = gbDistributerGoodsPriceService.queryObject(gbDpgDisGoodsPriceId);
                goodsPriceEntity.setGbDgpPurWhat(-1);
                goodsPriceEntity.setGbDgpPurPrice(purchaseGoodsEntity.getGbDpgBuyPrice());
                goodsPriceEntity.setGbDgpPurWeight(purchaseGoodsEntity.getGbDpgBuyQuantity());
                goodsPriceEntity.setGbDgpPurScale(multiply.toString());
                goodsPriceEntity.setGbDgpPurWhatTotal(lowerTotal.toString());
                goodsPriceEntity.setGbDgpGoodsLowestTotal(lowestTotal.toString());
                goodsPriceEntity.setGbDgpGoodsHighestTotal("0");
                goodsPriceEntity.setGbDgpPurTotal(priceTotal);
                goodsPriceEntity.setGbDgpPurWeight(purchaseGoodsEntity.getGbDpgBuyQuantity());
                goodsPriceEntity.setGbDgpPurDepartmentId(purchaseGoodsEntity.getGbDpgPurchaseDepartmentId());
                goodsPriceEntity.setGbDgpPurNxDistributerId(gbDistributerGoodsEntity.getGbDgNxDistributerId());
                goodsPriceEntity.setGbDgpGoodsPrice(gbDistributerGoodsEntity.getGbDgGoodsPrice());
                goodsPriceEntity.setGbDgpGoodsLowestPrice(gbDistributerGoodsEntity.getGbDgGoodsLowestPrice());
                goodsPriceEntity.setGbDgpGoodsHighestPrice(gbDistributerGoodsEntity.getGbDgGoodsHighestPrice());

                gbDistributerGoodsPriceService.update(goodsPriceEntity);
            } else {
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
                gbDistributerGoodsPriceService.save(goodsPriceEntity);
                purchaseGoodsEntity.setGbDpgDisGoodsPriceId(goodsPriceEntity.getGbDistributerGoodsPriceId());
            }

            gbDpgService.update(purchaseGoodsEntity);
        }

        if (buyPrice.compareTo(goodsHighest) == -1 && buyPrice.compareTo(goodsLowest) == 1) {

            if (gbDpgDisGoodsPriceId != null) {

                gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);

                purchaseGoodsEntity.setGbDpgBuyPriceReason(null);
                purchaseGoodsEntity.setGbDpgDisGoodsPriceId(null);
                gbDpgService.update(purchaseGoodsEntity);

            }
        }
        if (buyPrice.compareTo(goodsHighest) == 0 || buyPrice.compareTo(goodsLowest) == 0) {
            if (gbDpgDisGoodsPriceId != null) {

                gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);

                purchaseGoodsEntity.setGbDpgBuyPriceReason(null);
                purchaseGoodsEntity.setGbDpgDisGoodsPriceId(null);
                gbDpgService.update(purchaseGoodsEntity);


            }
        }


        return purchaseGoodsEntity;

    }


}
