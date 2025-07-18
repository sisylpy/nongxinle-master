package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-24 11:45
 */

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.Constant;
import com.sun.javafx.binding.StringFormatter;
import org.apache.poi.ss.formula.functions.FactDouble;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;


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
    private GbDistributerGoodsShelfService gbDisGoodsShelfService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerGoodsPriceService gbDistributerGoodsPriceService;
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private GbDistributerWeightTotalService gbDistributerWeightTotalService;
    @Autowired
    private GbDistributerWeightTotalService gbDisWeightTotalService;
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
    private NxDepartmentBillService nxDepartmentBillService;
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


    @RequestMapping(value = "/deleteDisPurAndNxDataItem/{id}")
    @ResponseBody
    public R deleteDisPurAndNxDataItem(@PathVariable Integer id) {
        GbDistributerPurchaseGoodsEntity purGoods = gbDpgService.queryObject(id);

//        if (purGoods.getGbDpgStatus() == 1) {
        Integer gbDoDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        Map<String, Object> mapD = new HashMap<>();
        mapD.put("disId", goodsEntity.getGbDgDistributerId());
        mapD.put("type", getGbDepartmentTypeJicai());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(mapD);
        goodsEntity.setGbDgGoodsType(2);
        goodsEntity.setGbDgGbSupplierId(-1);
        goodsEntity.setGbDgGbDepartmentId(departmentEntities.get(0).getGbDepartmentId());
        goodsEntity.setGbDgNxDistributerId(-1);
        goodsEntity.setGbDgNxDistributerGoodsId(-1);
        gbDistributerGoodsService.update(goodsEntity);

        Integer gbDpgDisGoodsPriceId = purGoods.getGbDpgDisGoodsPriceId();
        if (gbDpgDisGoodsPriceId != null) {
            gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);
        }
        purGoods.setGbDpgPurchaseType(2);
        purGoods.setGbDpgPurchaseDepartmentId(departmentEntities.get(0).getGbDepartmentId());
        purGoods.setGbDpgDisGoodsPriceId(null);
        purGoods.setGbDpgBatchId(null);
        purGoods.setGbDpgPurUserId(null);
        purGoods.setGbDpgStatus(0);
        purGoods.setGbDpgTime(null);
        purGoods.setGbDpgBuySubtotal("0");
        purGoods.setGbDpgBuyPrice(null);
//            purGoods.setGbDpgBuyQuantity(null);
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

            Integer toDepartmentId = orders.getGbDoToDepartmentId();
            Map<String, Object> mapDes = new HashMap<>();
            mapDes.put("depId", toDepartmentId);
            mapDes.put("disGoodsId", orders.getGbDoDisGoodsId());
            GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryDepGoodsItemByParams(mapDes);
            if (departmentDisGoodsEntity != null) {
                departmentDisGoodsEntity.setGbDdgDepartmentId(departmentEntities.get(0).getGbDepartmentId());
                departmentDisGoodsEntity.setGbDdgDepartmentFatherId(departmentEntities.get(0).getGbDepartmentId());
                departmentDisGoodsEntity.setGbDdgNxDistributerGoodsId(-1);
                departmentDisGoodsEntity.setGbDdgNxDistributerId(-1);
                gbDepartmentDisGoodsService.update(departmentDisGoodsEntity);
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
            orders.setGbDoToDepartmentId(departmentEntities.get(0).getGbDepartmentId());
            orders.setGbDoNxDepartmentOrderId(-1);
            orders.setGbDoNxDistributerGoodsId(-1);
            orders.setGbDoGoodsType(2);
            orders.setGbDoOrderType(2);
            gbDepartmentOrdersService.update(orders);


        }
        return R.ok();

//        } else {
//            return R.error(-1, "请刷新数据");
//        }
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
//                    if (nxDistributerPurchaseGoodsEntity.getNxDpgStatus() == getNxDisPurchaseGoodsUnBuy() || nxDistributerPurchaseGoodsEntity.getNxDpgStatus() == getNxDisPurchaseGoodsWithBatch()) {
                    Integer nxDpgBatchId = nxDistributerPurchaseGoodsEntity.getNxDpgBatchId();
                    List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByBatchId(nxDpgBatchId);
                    if (purchaseGoodsEntities.size() == 1) {
                        nxDPBService.delete(nxDpgBatchId);
                    } else {
                        String nxDpgBuySubtotal = nxDistributerPurchaseGoodsEntity.getNxDpgBuySubtotal();
                        NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryObject(nxDpgBatchId);
                        if (nxDistributerPurchaseBatchEntity.getNxDpbSellSubtotal() != null && !nxDistributerPurchaseBatchEntity.getNxDpbSellSubtotal().trim().isEmpty()) {
                            BigDecimal decimal = new BigDecimal(nxDistributerPurchaseBatchEntity.getNxDpbSellSubtotal());
                            BigDecimal decimal2 = new BigDecimal(nxDpgBuySubtotal);
                            BigDecimal decimal1 = decimal.subtract(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                            nxDistributerPurchaseBatchEntity.setNxDpbSellSubtotal(decimal1.toString());
                            nxDPBService.update(nxDistributerPurchaseBatchEntity);
                        }
                    }
//                    }

                    nxDistributerPurchaseGoodsService.delete(nxDistributerPurchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
                }
            }
        }
    }


    @RequestMapping(value = "/disGetNxDistributerPurGoodsDate", method = RequestMethod.POST)
    @ResponseBody
    public R disGetNxDistributerPurGoodsDate(Integer disId, String startDate, String stopDate) {


        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("dayuBuyStatus", 2);
        map4.put("nxDis", 1);
        map4.put("startDate", startDate);
        map4.put("stopDate", stopDate);
        System.out.println("map4444444=====" + map4);

        List<NxDistributerEntity> distributerEntities = gbDepartmentOrdersService.queryGbDepNxDistributerOrder(map4);
        List<Map<String, Object>> result = new ArrayList<>();

        if (distributerEntities.size() > 0) {
            for (int i = 0; i < distributerEntities.size(); i++) {
                NxDistributerEntity distributerEntity = distributerEntities.get(i);
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("nxDis", distributerEntity);
                Map<String, Object> mapB = new HashMap<>();

                mapB.put("startDate", startDate);
                mapB.put("stopDate", stopDate);
                mapB.put("disId", distributerEntity.getNxDistributerId());
                mapB.put("gbDisId", disId);
                mapB.put("dayuStatus", -1);
                System.out.println("wwhwhwhw" + mapB);
                List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryGbDepBillsByParams(mapB);
                mapItem.put("arr", billEntityList);

                result.add(mapItem);
            }
        }
        return R.ok().put("data", result);
    }


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
//        map1.put("purchaseType", 5);
        map1.put("startDate", startDate);
        map1.put("stopDate", stopDate);
        map1.put("buySubtotal", 0);
        System.out.println("mapapaudfadf" + map1);
        Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map1);

        TreeSet<GbDistributerGoodsEntity> goodsList = new TreeSet<>();
        if (integer > 0) {
            List<GbDistributerFatherGoodsEntity> greatGrand = gbDpgService.queryGreatGrandGoodsByDisGoods(map1);
            if (greatGrand.size() > 0) {
                for (GbDistributerFatherGoodsEntity greatGrandEntity : greatGrand) {
                    List<GbDistributerFatherGoodsEntity> grandEntityFatherGoodsEntities = greatGrandEntity.getFatherGoodsEntities();
                    if (grandEntityFatherGoodsEntities.size() > 0) {
                        for (GbDistributerFatherGoodsEntity grand : grandEntityFatherGoodsEntities) {
                            List<GbDistributerGoodsEntity> goodsEntities = grand.getGbDistributerGoodsEntities();
                            for (GbDistributerGoodsEntity goodsEntity : goodsEntities) {
                                System.out.println("gggg" + goodsEntity.getGbDgGoodsName());
                                double doutbleCost = 0;
                                double doutbleCostV = 0;
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
                                map.put("buySubtotal", 0);
                                map.put("nxDisId", nxDisId);
                                map.put("disId", gbDisId);
                                System.out.println("mappsspspspspspspps" + map);
                                purCount = gbDpgService.queryGbPurchaseGoodsCount(map);
                                if (purCount > 0) {
                                    System.out.println("caigoushushul" + map);
                                    doutbleCostV = gbDpgService.queryPurchaseGoodsSubTotal(map);
                                    doutbleCost = gbDpgService.queryPurchaseGoodsWeightTotal(map);
                                    v = doutbleCostV / doutbleCost;
                                    perPrice = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
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
                                    goodsEntity.setGoodsCostTotal(goodsDoutbleCostV);
                                    goodsEntity.setGoodsCostTotalString(String.format("%.1f", goodsDoutbleCostV));

                                    goodsEntity.setGoodsStockTotal(goodsDoutbleRestV);
                                    goodsEntity.setGoodsStockTotalString(String.format("%.1f", goodsDoutbleRestV));
                                    goodsEntity.setGoodsStockWeightTotal(goodsDoutbleRest);
                                    goodsEntity.setGoodsStockWeightTotalString(String.format("%.1f", goodsDoutbleRest));

                                    goodsEntity.setGoodsProduceWeightTotal(goodsDoutbleProduce);
                                    goodsEntity.setGoodsProduceWeightTotalString(String.format("%.1f", goodsDoutbleProduce));
                                    goodsEntity.setGoodsProduceTotal(goodsDoutbleProduceV);
                                    goodsEntity.setGoodsProduceTotalString(String.format("%.1f", goodsDoutbleProduceV));

                                    goodsEntity.setGoodsLossWeightTotal(goodsDoutbleLoss);
                                    goodsEntity.setGoodsLossWeightTotalString(String.format("%.1f", goodsDoutbleLoss));
                                    goodsEntity.setGoodsLossTotal(goodsDoutbleLossV);
                                    goodsEntity.setGoodsLossTotalString(String.format("%.1f", goodsDoutbleLossV));

                                    goodsEntity.setGoodsWasteWeightTotal(goodsDoutbleWaste);
                                    goodsEntity.setGoodsWasteWeightTotalString(String.format("%.1f", goodsDoutbleWaste));
                                    goodsEntity.setGoodsWasteTotal(goodsDoutbleWasteV);
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
                System.out.println("o1ccgetGoodsCostTotalgetGoodsCostTotal" + o1.getGoodsCostTotal());
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

    private TreeSet<GbDistributerGoodsEntity> abcGoodsLoss(TreeSet<GbDistributerGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                System.out.println("o1ccgetGoodsCostTotalgetGoodsCostTotal" + o1.getGoodsCostTotal());
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
        map1.put("buySubtotal", 0);
        System.out.println("wsupsspspspsp" + map1);
        Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map1);
        TreeSet<GbDistributerGoodsEntity> goodsList = new TreeSet<>();
        if (integer > 0) {
            System.out.println("wsupsspspspsp" + map1);
            List<GbDistributerFatherGoodsEntity> greatGrand = gbDpgService.queryGreatGrandGoodsByDisGoods(map1);
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

                                double doutbleCost = 0;
                                double doutbleCostV = 0;
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
                                map.put("buySubtotal", 0);

                                purCount = gbDpgService.queryGbPurchaseGoodsCount(map);
                                if (purCount > 0) {
                                    doutbleCostV = gbDpgService.queryPurchaseGoodsSubTotal(map);
                                    doutbleCost = gbDpgService.queryPurchaseGoodsWeightTotal(map);
                                    System.out.println("AAAAA" + doutbleCostV + "bbb" + doutbleCost);
                                    v = doutbleCostV / doutbleCost;
                                    perPrice = new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
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
                                        mapDay.put("buySubtotal", 0);
                                        System.out.println("wokkkkkk" + mapDay);
                                        int purCountDay = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
                                        if (purCountDay == 1) {
                                            String price = gbDpgService.queryPurchaseGoodsPrice(mapDay);
                                            String weight = gbDpgService.queryPurchaseGoodsWeight(mapDay);

                                            Map<String, Object> mapDayValue = new HashMap<>();
                                            mapDayValue.put("dayPrice", new BigDecimal(price).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                            mapDayValue.put("dayWeight", new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                            dateDataList.add(mapDayValue);
                                        } else if (purCountDay > 1) {
                                            Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);
                                            Double weightTotal = gbDpgService.queryPurchaseGoodsWeightTotal(mapDay);

                                            double v1 = subTotal / weightTotal;
                                            double v2 = weightTotal / purCountDay;

                                            Map<String, Object> mapDayValue = new HashMap<>();
                                            mapDayValue.put("dayPrice", new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                            mapDayValue.put("dayWeight", new BigDecimal(v2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                            dateDataList.add(mapDayValue);
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

                                        Map<String, Object> mapDayValue = new HashMap<>();
                                        mapDayValue.put("dayPrice", new BigDecimal(price).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                        mapDayValue.put("dayWeight", new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                        dateDataList.add(mapDayValue);
                                    } else if (purCountDay > 1) {
                                        Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);
                                        Double weightTotal = gbDpgService.queryPurchaseGoodsWeightTotal(mapDay);

                                        double v1 = subTotal / weightTotal;
                                        double v2 = weightTotal / purCountDay;

                                        Map<String, Object> mapDayValue = new HashMap<>();
                                        mapDayValue.put("dayPrice", new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                        mapDayValue.put("dayWeight", new BigDecimal(v2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                        dateDataList.add(mapDayValue);
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

    @RequestMapping(value = "/getGbPurGoodsFenxi", method = RequestMethod.POST)
    @ResponseBody
    public R getGbPurGoodsFenxi(Integer disGoodsId, String startDate, String stopDate) {

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
        map1.put("buySubtotal", 0);
        System.out.println("wsupsspspspsp" + map1);
        Integer integerPur = gbDpgService.queryGbPurchaseGoodsCount(map1);
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map1);
        if (integer > 0 || integerPur > 0) {

            Map<String, Object> mapDay = new HashMap<>();
            List<String> dateList = new ArrayList<>();

            List<String> priceDayValue = new ArrayList<>();
            List<String> weightDayValue = new ArrayList<>();
            List<String> produceDayValue = new ArrayList<>();
            List<String>  wasteDayValue = new ArrayList<>();
            List<String>  lossDayValue = new ArrayList<>();
            List<String>  returnDayValue = new ArrayList<>();

            List<String> lowestPriceList = new ArrayList<>();
            List<String> highestPriceList = new ArrayList<>();

            double doubleCost = 0;
            double doubleCostV = 0;
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
            map.put("buySubtotal", 0);

            purCount = gbDpgService.queryGbPurchaseGoodsCount(map);
            if (purCount > 0) {
                doubleCostV = gbDpgService.queryPurchaseGoodsSubTotal(map);
                doubleCost = gbDpgService.queryPurchaseGoodsWeightTotal(map);
                System.out.println("AAAAA" + doubleCostV + "bbb" + doubleCost);
                v = doubleCostV / doubleCost;
                perPrice = new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                maxPrice = gbDpgService.queryPurGoodsMaxPrice(map);
                minPrice = gbDpgService.queryPurGoodsMinPrice(map);
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
                    mapDay.put("buySubtotal", 0);
                    System.out.println("wokkkkkk" + mapDay);

                    processDailyBusinessData(mapDay, produceDayValue, lossDayValue, wasteDayValue, returnDayValue);
                    processDailyPurchaseData(mapDay, priceDayValue, weightDayValue);
                }
            } else {

                String substring = startDate.substring(8, 10);
                dateList.add(substring);
                mapDay.put("date", startDate);
                mapDay.put("disGoodsId", disGoodsId);
                mapDay.put("buySubtotal", 0);
                System.out.println("onedaoaayyayayyay"+ mapDay);
                dateList.add(substring);

                //4,supplier
                mapDay.put("date", startDate);
                mapDay.put("disGoodsId", disGoodsId);
                mapDay.put("buySubtotal", 0);
                System.out.println("wokkkkkkondaayayaayayayy" + mapDay);

                processDailyBusinessData(mapDay, produceDayValue, lossDayValue, wasteDayValue, returnDayValue);
                processDailyPurchaseData(mapDay, priceDayValue, weightDayValue);

            }



            Map<String, Object> mapEveryDay = new HashMap<>();
            mapEveryDay.put("produceValue", produceDayValue);
            mapEveryDay.put("lossValue", lossDayValue);
            mapEveryDay.put("wasteValue", wasteDayValue);
            mapEveryDay.put("returnValue", returnDayValue);
            mapEveryDay.put("priceValue", priceDayValue);
            mapEveryDay.put("weightValue", weightDayValue);
            mapEveryDay.put("dateList", dateList);
            mapEveryDay.put("lowestList", lowestPriceList);
            mapEveryDay.put("highestList", highestPriceList);
            System.out.println("whweheeekeeme" + mapEveryDay);
            goodsEntity.setPurEveryDay(mapEveryDay);
            System.out.println("goodosososo" + goodsEntity.getPurEveryDay());

            Map<String, Object> mapStars = new HashMap<>();
            mapStars.put("startDate", startDate);
            mapStars.put("stopDate", stopDate);
            mapStars.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
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
            mapResult.put("totalCost", new BigDecimal(doubleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalCostSubtotal", new BigDecimal(doubleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("maxPrice", maxPrice);
            mapResult.put("minPrice", minPrice);
            mapResult.put("perPrice", perPrice);
            mapResult.put("purCount", purCount);
            mapResult.put("starsCount", mapStar);
            mapResult.put("code", 0);


            goodsEntity.setGoodsData(mapResult);

            map1.put("dayuStatus", null);
            System.out.println("stoosososossososososos" + map1);

            double goodsDoubleTotalV = 0;
            double goodsDoubleTotal = 0;
            double goodsDoubleCost = 0;
            double goodsDoubleCostV = 0;
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

            Map<String, Object> mapDepStock = new HashMap<>();

            mapDepStock.put("disGoodsId", disGoodsId);
            System.out.println("mapDepStockmapDepStock" + mapDepStock);
            Integer integer3 = gbDepartmentGoodsStockService.queryGoodsStockCount(mapDepStock);
            if(integer3 > 0){
                goodsDoubleRest =  gbDepartmentGoodsStockService.queryDepStockRestWeightTotal(mapDepStock);
                 goodsDoubleRestV =  gbDepartmentGoodsStockService.queryDepStockRestSubtotal(mapDepStock);

                goodsEntity.setGoodsWeightTotalString(String.format("%.1f", goodsDoubleRest));
                goodsEntity.setGoodsStockTotalString(String.format("%.1f", goodsDoubleRestV));
                System.out.println("stoskckkckckc" + goodsDoubleRest + "vvvvv" + goodsDoubleRestV);


            }



            mapDepStock.put("startDate", startDate);
            mapDepStock.put("stopDate", stopDate);
            Integer integer22 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDepStock);

            mapDepStock.put("dayuStatus",2);
            Integer integer2 = gbDpgService.queryGbPurchaseGoodsCount(mapDepStock);
            if (integer2 > 0 || integer22 > 0) {

                if(integer2 > 0){
                    System.out.println("puttoototot" + mapDepStock);
                    goodsDoubleTotal =  gbDpgService.queryPurchaseGoodsWeightTotal(mapDepStock);
                    goodsDoubleTotalV =  gbDpgService.queryPurchaseGoodsSubTotal(mapDepStock);

                }

                if(integer22 > 0){

                    System.out.println("reststockkkkweiieieie" + goodsDoubleRest);

                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                    System.out.println("pururururuururu" + mapDepStock);
                    Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                    if (integerProduce > 0) {
                        goodsDoubleProduceV = gbDepartmentStockReduceService.queryReduceProduceTotal(mapDepStock);
                        goodsDoubleProduce = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(mapDepStock);
                        goodsDoubleCost = goodsDoubleCost + goodsDoubleProduce ;
                        System.out.println("goodsDoubleCostgoodsDoubleCostpppp" + goodsDoubleCost);
                    } else {
                        goodsDoubleProduceV = 0;
                        goodsDoubleProduce = 0;
                    }
                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                    Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                    if (integerLoss > 0) {
                        goodsDoubleLossV = gbDepartmentStockReduceService.queryReduceLossTotal(mapDepStock);
                        goodsDoubleLoss = gbDepartmentStockReduceService.queryReduceLossWeightTotal(mapDepStock);
                        goodsDoubleCost = goodsDoubleCost + goodsDoubleLoss ;
                        System.out.println("goodsDoubleCostgoodsDoubleCostllll" + goodsDoubleCost);
                    } else {
                        goodsDoubleLossV = 0;
                        goodsDoubleLoss = 0;
                    }
                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                    Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                    if (integerWaste > 0) {
                        goodsDoubleWasteV = gbDepartmentStockReduceService.queryReduceWasteTotal(mapDepStock);
                        goodsDoubleWaste = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(mapDepStock);
                        goodsDoubleCost = goodsDoubleCost + goodsDoubleWaste ;
                    } else {
                        goodsDoubleWasteV = 0;
                        goodsDoubleWaste = 0;
                    }
                    mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
                    Integer integerReturn = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
                    if (integerReturn > 0) {
                        goodsDoubleReturnV = gbDepartmentStockReduceService.queryReduceReturnTotal(mapDepStock);
                        goodsDoubleReturn = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(mapDepStock);
                    } else {
                        goodsDoubleReturnV = 0;
                    }
                }


                System.out.println("goodsDoubleCostgoodsDoubleCostllll33333" + goodsDoubleCost);

                BigDecimal proPercent = new BigDecimal(0);
                BigDecimal lossPercent = new BigDecimal(0);
                BigDecimal wastePercent = new BigDecimal(0);
                BigDecimal retPercent = new BigDecimal(0);
                if(doubleCost > 0){
                    proPercent  = new BigDecimal(goodsDoubleProduce).divide(new BigDecimal(doubleCost),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
                     lossPercent = new BigDecimal(goodsDoubleLoss).divide(new BigDecimal(doubleCost),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
                    wastePercent  = new BigDecimal(goodsDoubleWaste).divide(new BigDecimal(doubleCost),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
                    retPercent = new BigDecimal(goodsDoubleReturn).divide(new BigDecimal(doubleCost),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);

                }


                goodsEntity.setGoodsProducePercent(proPercent.toString());
                goodsEntity.setGoodsLossPercent(lossPercent.toString());
                goodsEntity.setGoodsWastePercent(wastePercent.toString());
                goodsEntity.setGoodsReturnPercent(retPercent.toString());

                goodsEntity.setGoodsPurTotalWeight(String.format("%.1f", goodsDoubleTotal));
                goodsEntity.setGoodsPurTotalSubtotal(String.format("%.1f", goodsDoubleTotalV));

                goodsEntity.setGoodsCostWeightTotalString(String.format("%.1f", goodsDoubleCost));
                BigDecimal totalPercent = new BigDecimal(0);
                BigDecimal stockPercent = new BigDecimal(0);
                if(goodsDoubleTotal > 0){
                    totalPercent   = new BigDecimal(goodsDoubleCost).divide(new BigDecimal(goodsDoubleTotal),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
                     stockPercent = new BigDecimal(goodsDoubleRest).divide(new BigDecimal(goodsDoubleTotal),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);

                }


                goodsEntity.setGoodsCostTotalString(totalPercent.toString());

                goodsEntity.setGoodsStockWeightTotalString(stockPercent.toString());

                goodsEntity.setGoodsProduceWeightTotalString(String.format("%.1f", goodsDoubleProduce));
                goodsEntity.setGoodsProduceTotalString(String.format("%.1f", goodsDoubleProduceV));

                goodsEntity.setGoodsLossWeightTotalString(String.format("%.1f", goodsDoubleLoss));
                goodsEntity.setGoodsLossTotalString(String.format("%.1f", goodsDoubleLossV));

                goodsEntity.setGoodsWasteWeightTotalString(String.format("%.1f", goodsDoubleWaste));
                goodsEntity.setGoodsWasteTotalString(String.format("%.1f", goodsDoubleWasteV));

                goodsEntity.setGoodsReturnWeightTotalString(String.format("%.1f", goodsDoubleReturn));
                goodsEntity.setGoodsReturnTotalString(String.format("%.1f", goodsDoubleReturnV));

                goodsEntity.setGoodsAverageOrderTimes(integer2.toString());
                Integer integer1 = gbDepartmentGoodsStockService.queryGoodsStockCount(mapDepStock);
                int total = 5;
                if(integer1 > 0 ){
                    total = gbDepartmentGoodsStockService.queryGoodsStockStars(mapDepStock);
                }

                System.out.println("totaltotaltotaltotal" + total);
                System.out.println("integer2integer2" + integer2);
                int i = 5;
                int remainder = 0;
                if(integer2 > 0){
                    i = total / integer2;
                    remainder = total % integer2;
                }


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

        }else{
            Map<String, Object> mapResult = new HashMap<>();

            mapResult.put("code", -1);
            goodsEntity.setGoodsData(mapResult);
            
            // 设置默认值，避免 null
            goodsEntity.setGoodsPurTotalWeight("0");
            goodsEntity.setGoodsPurTotalCount(0);
        }


        return R.ok().put("data", goodsEntity);
    }


    /**
     * 处理每日业务数据（生产、损耗、废弃、退货）
     */
    private void processDailyBusinessData(Map<String, Object> mapDay, List<String> produceDayValue,
                                        List<String> lossDayValue, List<String> wasteDayValue, List<String> returnDayValue) {
        Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDay);
        System.out.println("onda0101001010 " + mapDay);
        if(integer1 > 0) {
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
     * 处理每日采购数据（价格、重量）
     */
    private void processDailyPurchaseData(Map<String, Object> mapDay, List<String> priceDayValue, List<String> weightDayValue) {

        int purCountDay = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
        if (purCountDay == 1) {
            String price = gbDpgService.queryPurchaseGoodsPrice(mapDay);
            String weight = gbDpgService.queryPurchaseGoodsWeight(mapDay);

            priceDayValue.add(new BigDecimal(price).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            weightDayValue.add(new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        } else if (purCountDay > 1) {
            Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);
            Double weightTotal = gbDpgService.queryPurchaseGoodsWeightTotal(mapDay);

            double v1 = subTotal / weightTotal;
            double v2 = weightTotal / purCountDay;

            priceDayValue.add(new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            weightDayValue.add(new BigDecimal(v2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        } else {
            priceDayValue.add("0");
            weightDayValue.add("0");
        }
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

        public double getDoubleCost() { return doubleCost; }
        public double getDoubleCostV() { return doubleCostV; }
        public String getMaxPrice() { return maxPrice; }
        public String getMinPrice() { return minPrice; }
        public String getPerPrice() { return perPrice; }
        public int getPurCount() { return purCount; }
    }



//    @RequestMapping(value = "/getGbPurGoodsFenxi", method = RequestMethod.POST)
//    @ResponseBody
//    public R getGbPurGoodsFenxi(Integer disGoodsId, String startDate, String stopDate) {
//
//        GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(disGoodsId);
//
//        Integer howManyDaysInPeriod = 0;
//        if (!startDate.equals(stopDate)) {
//            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
//        }
//
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("dayuStatus", 1);
//        map1.put("disGoodsId", disGoodsId);
//        map1.put("startDate", startDate);
//        map1.put("stopDate", stopDate);
//        map1.put("buySubtotal", 0);
//        System.out.println("wsupsspspspsp" + map1);
//        Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map1);
//        if (integer > 0) {
//
//            Map<String, Object> mapDay = new HashMap<>();
//            List<String> dateList = new ArrayList<>();
//
//            List<String> priceDayValue = new ArrayList<>();
//            List<String> weightDayValue = new ArrayList<>();
//            List<String> produceDayValue = new ArrayList<>();
//            List<String>  wasteDayValue = new ArrayList<>();
//            List<String>  lossDayValue = new ArrayList<>();
//            List<String>  returnDayValue = new ArrayList<>();
//
//            List<String> lowestPriceList = new ArrayList<>();
//            List<String> highestPriceList = new ArrayList<>();
//
//            double doubleCost = 0;
//            double doubleCostV = 0;
//            double v = 0;
//            String maxPrice = "0";
//            String minPrice = "0";
//            String perPrice = "0";
//            int purCount = 0;
//
//            Map<String, Object> map = new HashMap<>();
//            map.put("startDate", startDate);
//            map.put("stopDate", stopDate);
//            map.put("disGoodsId", disGoodsId);
//            map.put("dayuStatus", 2);
//            map.put("buySubtotal", 0);
//
//            purCount = gbDpgService.queryGbPurchaseGoodsCount(map);
//            if (purCount > 0) {
//                doubleCostV = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                doubleCost = gbDpgService.queryPurchaseGoodsWeightTotal(map);
//                System.out.println("AAAAA" + doubleCostV + "bbb" + doubleCost);
//                v = doubleCostV / doubleCost;
//                perPrice = new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//                maxPrice = gbDpgService.queryPurGoodsMaxPrice(map);
//                minPrice = gbDpgService.queryPurGoodsMinPrice(map);
//            }
//            if (howManyDaysInPeriod > 0) {
//
//                System.out.println("epsososososo==0");
//                // top
//                for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
//                    // dateList
//                    String whichDay = "";
//                    if (i == 0) {
//                        whichDay = startDate;
//                    } else {
//                        whichDay = afterWhatDay(startDate, i);
//                    }
//                    //1.day
//                    String substring = whichDay.substring(8, 10);
//                    dateList.add(substring);
//
//                    //4,supplier
//                    mapDay.put("date", whichDay);
//                    mapDay.put("disGoodsId", disGoodsId);
//                    mapDay.put("buySubtotal", 0);
//                    System.out.println("wokkkkkk" + mapDay);
//
//                    Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDay);
////                    GbDepartmentGoodsDailyEntity gbDepartmentGoodsDailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapDay);
//                    if(integer1  > 0){
//                        Double gbDgdProduceWeight = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDay);
//                        Double gbDgdLossWeight = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDay);
//                        Double gbDgdWasteWeight = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDay);
//                        Double gbDgdReturnWeight = gbDepGoodsDailyService.queryDepGoodsDailyReturnWeight(mapDay);
//
//                        produceDayValue.add(String.format("%.1f", gbDgdProduceWeight));
//                        lossDayValue.add(String.format("%.1f", gbDgdLossWeight));
//                        wasteDayValue.add(String.format("%.1f", gbDgdWasteWeight));
//                        returnDayValue.add(String.format("%.1f", gbDgdReturnWeight));
//                    }else{
//                        produceDayValue.add("0");
//                        lossDayValue.add("0");
//                        wasteDayValue.add("0");
//                        returnDayValue.add("0");
//                    }
//
//                    int purCountDay = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
//                    if (purCountDay == 1) {
//
//                        String price = gbDpgService.queryPurchaseGoodsPrice(mapDay);
//                        String weight = gbDpgService.queryPurchaseGoodsWeight(mapDay);
//
//                        priceDayValue.add(new BigDecimal(price).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        weightDayValue.add(new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                    } else if (purCountDay > 1) {
//                        Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);
//                        Double weightTotal = gbDpgService.queryPurchaseGoodsWeightTotal(mapDay);
//
//                        double v1 = subTotal / weightTotal;
//                        double v2 = weightTotal / purCountDay;
//
//                        priceDayValue.add(new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        weightDayValue.add(new BigDecimal(v2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                    } else {
//                        priceDayValue.add("0");
//                        weightDayValue.add("0");
//                    }
//                }
//            } else {
//                String substring = startDate.substring(8, 10);
//                dateList.add(substring);
//                mapDay.put("date", startDate);
//                mapDay.put("disGoodsId", disGoodsId);
//                mapDay.put("buySubtotal", 0);
//
//                // 处理每日业务数据（生产、损耗、废弃、退货）
//                Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDay);
//                if(integer1 > 0) {
//                    Double gbDgdProduceWeight = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDay);
//                    Double gbDgdLossWeight = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDay);
//                    Double gbDgdWasteWeight = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDay);
//                    Double gbDgdReturnWeight = gbDepGoodsDailyService.queryDepGoodsDailyReturnWeight(mapDay);
//
//                    produceDayValue.add(String.format("%.1f", gbDgdProduceWeight));
//                    lossDayValue.add(String.format("%.1f", gbDgdLossWeight));
//                    wasteDayValue.add(String.format("%.1f", gbDgdWasteWeight));
//                    returnDayValue.add(String.format("%.1f", gbDgdReturnWeight));
//                } else {
//                    produceDayValue.add("0");
//                    lossDayValue.add("0");
//                    wasteDayValue.add("0");
//                    returnDayValue.add("0");
//                }
//
//                // 处理每日采购数据（价格、重量）
//                int purCountDay = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
//                if (purCountDay == 1) {
//                    String price = gbDpgService.queryPurchaseGoodsPrice(mapDay);
//                    String weight = gbDpgService.queryPurchaseGoodsWeight(mapDay);
//
//                    priceDayValue.add(new BigDecimal(price).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    weightDayValue.add(new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                } else if (purCountDay > 1) {
//                    Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);
//                    Double weightTotal = gbDpgService.queryPurchaseGoodsWeightTotal(mapDay);
//
//                    double v1 = subTotal / weightTotal;
//                    double v2 = weightTotal / purCountDay;
//
//                    priceDayValue.add(new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    weightDayValue.add(new BigDecimal(v2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                } else {
//                    priceDayValue.add("0");
//                    weightDayValue.add("0");
//                }
//            }
//
//
//            Map<String, Object> mapEveryDay = new HashMap<>();
//            mapEveryDay.put("produceValue", produceDayValue);
//            mapEveryDay.put("lossValue", lossDayValue);
//            mapEveryDay.put("wasteValue", wasteDayValue);
//            mapEveryDay.put("returnValue", returnDayValue);
//            mapEveryDay.put("priceValue", priceDayValue);
//            mapEveryDay.put("weightValue", weightDayValue);
//            mapEveryDay.put("dateList", dateList);
//            mapEveryDay.put("lowestList", lowestPriceList);
//            mapEveryDay.put("highestList", highestPriceList);
//            goodsEntity.setPurEveryDay(mapEveryDay);
//
//            Map<String, Object> mapStars = new HashMap<>();
//            mapStars.put("startDate", startDate);
//            mapStars.put("stopDate", stopDate);
//            mapStars.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//            mapStars.put("starsLevel", 5);
//            System.out.println("mapStarsmapStarsmapStars" + mapStars);
//            Integer starsFive = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
//            mapStars.put("starsLevel", 4);
//            Integer starsFour = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
//            mapStars.put("starsLevel", 3);
//            Integer starsThree = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
//            mapStars.put("starsLevel", 2);
//            Integer starsTwo = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
//            mapStars.put("starsLevel", 1);
//            Integer starsOne = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
//            Map<String, Object> mapStar = new HashMap<>();
//            mapStar.put("starsFive", starsFive);
//            mapStar.put("starsFour", starsFour);
//            mapStar.put("starsThree", starsThree);
//            mapStar.put("starsTwo", starsTwo);
//            mapStar.put("starsOne", starsOne);
//
//            Map<String, Object> mapResult = new HashMap<>();
//            mapResult.put("totalCost", new BigDecimal(doubleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//            mapResult.put("totalCostSubtotal", new BigDecimal(doubleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//            mapResult.put("maxPrice", maxPrice);
//            mapResult.put("minPrice", minPrice);
//            mapResult.put("perPrice", perPrice);
//            mapResult.put("purCount", purCount);
//            mapResult.put("starsCount", mapStar);
//
//            goodsEntity.setGoodsData(mapResult);
//
//            map1.put("dayuStatus", null);
//            System.out.println("stoosososossososososos" + map1);
//
//            double goodsDoubleTotalV = 0;
//            double goodsDoubleTotal = 0;
//            double goodsDoubleCost = 0;
//            double goodsDoubleCostV = 0;
//            double goodsDoubleRestV = 0;
//            double goodsDoubleRest = 0;
//            double goodsDoubleLoss = 0;
//            double goodsDoubleLossV = 0;
//            double goodsDoubleWaste = 0;
//            double goodsDoubleWasteV = 0;
//            double goodsDoubleProduce = 0;
//            double goodsDoubleProduceV = 0;
//            double goodsDoubleReturnV = 0;
//            double goodsDoubleReturn = 0;
//
//            Map<String, Object> mapDepStock = new HashMap<>();
//            mapDepStock.put("startDate", startDate);
//            mapDepStock.put("stopDate", stopDate);
//            mapDepStock.put("disGoodsId", disGoodsId);
//            System.out.println("mapDepStockmapDepStock" + mapDepStock);
//            Integer integer2 = gbDepartmentGoodsStockService.queryGoodsStockCount(mapDepStock);
//            if (integer2 > 0) {
//                goodsDoubleTotal = gbDepartmentGoodsStockService.queryDepStockWeightTotal(mapDepStock);
//                goodsDoubleTotalV= gbDepartmentGoodsStockService.queryDepGoodsSubtotal(mapDepStock);
//
//                goodsDoubleRest = gbDepartmentGoodsStockService.queryDepGoodsRestWeightTotal(mapDepStock);
//                System.out.println("reststockkkkweiieieie" + goodsDoubleRest);
//                goodsDoubleRestV = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(mapDepStock);
//
//                mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
//                System.out.println("pururururuururu" + mapDepStock);
//                Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
//                if (integerProduce > 0) {
//                    goodsDoubleProduceV = gbDepartmentStockReduceService.queryReduceProduceTotal(mapDepStock);
//                    goodsDoubleProduce = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(mapDepStock);
//                    goodsDoubleCost = goodsDoubleCost + goodsDoubleProduce ;
//                    System.out.println("goodsDoubleCostgoodsDoubleCostpppp" + goodsDoubleCost);
//                } else {
//                    goodsDoubleProduceV = 0;
//                    goodsDoubleProduce = 0;
//                }
//                mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
//                Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
//                if (integerLoss > 0) {
//                    goodsDoubleLossV = gbDepartmentStockReduceService.queryReduceLossTotal(mapDepStock);
//                    goodsDoubleLoss = gbDepartmentStockReduceService.queryReduceLossWeightTotal(mapDepStock);
//                    goodsDoubleCost = goodsDoubleCost + goodsDoubleLoss ;
//                    System.out.println("goodsDoubleCostgoodsDoubleCostllll" + goodsDoubleCost);
//                } else {
//                    goodsDoubleLossV = 0;
//                    goodsDoubleLoss = 0;
//                }
//                mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
//                Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
//                if (integerWaste > 0) {
//                    goodsDoubleWasteV = gbDepartmentStockReduceService.queryReduceWasteTotal(mapDepStock);
//                    goodsDoubleWaste = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(mapDepStock);
//                    goodsDoubleCost = goodsDoubleCost + goodsDoubleWaste ;
//                } else {
//                    goodsDoubleWasteV = 0;
//                    goodsDoubleWaste = 0;
//                }
//                mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
//                Integer integerReturn = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
//                if (integerReturn > 0) {
//                    goodsDoubleReturnV = gbDepartmentStockReduceService.queryReduceReturnTotal(mapDepStock);
//                    goodsDoubleReturn = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(mapDepStock);
//                } else {
//                    goodsDoubleReturnV = 0;
//                }
//
//
//                System.out.println("goodsDoubleCostgoodsDoubleCostllll33333" + goodsDoubleCost);
//
//
//                BigDecimal proPercent = new BigDecimal(goodsDoubleProduce).divide(new BigDecimal(doubleCost),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
//                BigDecimal lossPercent = new BigDecimal(goodsDoubleLoss).divide(new BigDecimal(doubleCost),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
//                BigDecimal wastePercent = new BigDecimal(goodsDoubleWaste).divide(new BigDecimal(doubleCost),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
//                BigDecimal retPercent = new BigDecimal(goodsDoubleReturn).divide(new BigDecimal(doubleCost),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
//
//                goodsEntity.setGoodsProducePercent(proPercent.toString());
//                goodsEntity.setGoodsLossPercent(lossPercent.toString());
//                goodsEntity.setGoodsWastePercent(wastePercent.toString());
//                goodsEntity.setGoodsReturnPercent(retPercent.toString());
//
//                goodsEntity.setGoodsPurTotalWeight(String.format("%.1f", goodsDoubleTotal));
//                goodsEntity.setGoodsPurTotalSubtotal(String.format("%.1f", goodsDoubleTotalV));
//
//                goodsEntity.setGoodsCostWeightTotalString(String.format("%.1f", goodsDoubleCost));
//
//                BigDecimal totalPercent = new BigDecimal(goodsDoubleCost).divide(new BigDecimal(goodsDoubleTotal),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
//
//                goodsEntity.setGoodsCostTotalString(totalPercent.toString());
//
//                goodsEntity.setGoodsStockTotalString(String.format("%.1f", goodsDoubleRest));
//
//
//                BigDecimal stockPercent = new BigDecimal(goodsDoubleRest).divide(new BigDecimal(goodsDoubleTotal),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
//
//
//                goodsEntity.setGoodsStockWeightTotalString(stockPercent.toString());
//
//                goodsEntity.setGoodsProduceWeightTotalString(String.format("%.1f", goodsDoubleProduce));
//                goodsEntity.setGoodsProduceTotalString(String.format("%.1f", goodsDoubleProduceV));
//
//                goodsEntity.setGoodsLossWeightTotalString(String.format("%.1f", goodsDoubleLoss));
//                goodsEntity.setGoodsLossTotalString(String.format("%.1f", goodsDoubleLossV));
//
//                goodsEntity.setGoodsWasteWeightTotalString(String.format("%.1f", goodsDoubleWaste));
//                goodsEntity.setGoodsWasteTotalString(String.format("%.1f", goodsDoubleWasteV));
//
//                goodsEntity.setGoodsReturnWeightTotalString(String.format("%.1f", goodsDoubleReturn));
//                goodsEntity.setGoodsReturnTotalString(String.format("%.1f", goodsDoubleReturnV));
//
//                BigDecimal divide = new BigDecimal(integer2).divide(new BigDecimal(howManyDaysInPeriod), 1, BigDecimal.ROUND_HALF_UP);
//
//                goodsEntity.setGoodsAverageOrderTimes(divide.toString());
//                int total = gbDepartmentGoodsStockService.queryGoodsStockStars(mapDepStock);
//                System.out.println("totaltotaltotaltotal" + total);
//                System.out.println("integer2integer2" + integer2);
//                int i = total / integer2;
//                int remainder = total % integer2;
//                int gray = 0;
//                if (remainder > 0) {
//                    remainder = 1;
//                }
//                gray = 5 - i - remainder;
//                System.out.println("abccbcbcbcbcbcbcb" + i + "gray" + gray + "rem" + remainder);
//                goodsEntity.setGoodsStarGreen(i);
//                goodsEntity.setGoodsStarHalf(remainder);
//                goodsEntity.setGoodsStarGray(gray);
//                goodsEntity.setGoodsAverageStars(i);
//                goodsEntity.setGoodsAverageStarsString(String.valueOf(i));
//                goodsEntity.setGoodsPurTotalCount(integer2);
//            }
//
//        }else{
//            Map<String, Object> mapResult = new HashMap<>();
//
//            mapResult.put("totalCost","0");
//            goodsEntity.setGoodsData(mapResult);
//        }
//
//
//        return R.ok().put("data", goodsEntity);
//    }



//    @RequestMapping(value = "/getGbPurGoodsFenxi", method = RequestMethod.POST)
//    @ResponseBody
//    public R getGbPurGoodsFenxi(Integer disGoodsId, String startDate, String stopDate) {
//
//        GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(disGoodsId);
//
//        Integer howManyDaysInPeriod = 0;
//        if (!startDate.equals(stopDate)) {
//            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
//        }
//
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("dayuStatus", 1);
//        map1.put("disGoodsId", disGoodsId);
//        map1.put("startDate", startDate);
//        map1.put("stopDate", stopDate);
//        map1.put("buySubtotal", 0);
//        System.out.println("wsupsspspspsp" + map1);
//        Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map1);
//        if (integer > 0) {
//
//            Map<String, Object> mapDay = new HashMap<>();
//            List<String> dateList = new ArrayList<>();
//
//            List<String> priceDayValue = new ArrayList<>();
//            List<String> weightDayValue = new ArrayList<>();
//            List<String> produceDayValue = new ArrayList<>();
//            List<String>  wasteDayValue = new ArrayList<>();
//            List<String>  lossDayValue = new ArrayList<>();
//            List<String>  returnDayValue = new ArrayList<>();
//
//            List<String> lowestPriceList = new ArrayList<>();
//            List<String> highestPriceList = new ArrayList<>();
//
//            double doutbleCost = 0;
//            double doutbleCostV = 0;
//            double v = 0;
//            String maxPrice = "0";
//            String minPrice = "0";
//            String perPrice = "0";
//            int purCount = 0;
//
//            Map<String, Object> map = new HashMap<>();
//            map.put("startDate", startDate);
//            map.put("stopDate", stopDate);
//            map.put("disGoodsId", disGoodsId);
//            map.put("dayuStatus", 2);
//            map.put("buySubtotal", 0);
//
//            purCount = gbDpgService.queryGbPurchaseGoodsCount(map);
//            if (purCount > 0) {
//                doutbleCostV = gbDpgService.queryPurchaseGoodsSubTotal(map);
//                doutbleCost = gbDpgService.queryPurchaseGoodsWeightTotal(map);
//                System.out.println("AAAAA" + doutbleCostV + "bbb" + doutbleCost);
//                v = doutbleCostV / doutbleCost;
//                perPrice = new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//                maxPrice = gbDpgService.queryPurGoodsMaxPrice(map);
//                minPrice = gbDpgService.queryPurGoodsMinPrice(map);
//            }
//            if (howManyDaysInPeriod > 0) {
//
//                System.out.println("epsososososo==0");
//                // top
//                for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
//                    // dateList
//                    String whichDay = "";
//                    if (i == 0) {
//                        whichDay = startDate;
//                    } else {
//                        whichDay = afterWhatDay(startDate, i);
//                    }
//                    //1.day
//                    String substring = whichDay.substring(8, 10);
//                    dateList.add(substring);
//
//                    //4,supplier
//                    mapDay.put("date", whichDay);
//                    mapDay.put("disGoodsId", disGoodsId);
//                    mapDay.put("buySubtotal", 0);
//                    System.out.println("wokkkkkk" + mapDay);
//
//                    Integer integer1 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDay);
////                    GbDepartmentGoodsDailyEntity gbDepartmentGoodsDailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapDay);
//                   if(integer1  > 0){
//                       Double gbDgdProduceWeight = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(mapDay);
//                       Double gbDgdLossWeight = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(mapDay);
//                       Double gbDgdWasteWeight = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(mapDay);
//                       Double gbDgdReturnWeight = gbDepGoodsDailyService.queryDepGoodsDailyReturnWeight(mapDay);
//
//                       produceDayValue.add(String.format("%.1f", gbDgdProduceWeight));
//                       lossDayValue.add(String.format("%.1f", gbDgdLossWeight));
//                       wasteDayValue.add(String.format("%.1f", gbDgdWasteWeight));
//                       returnDayValue.add(String.format("%.1f", gbDgdReturnWeight));
//                   }else{
//                       produceDayValue.add("0");
//                       lossDayValue.add("0");
//                       wasteDayValue.add("0");
//                       returnDayValue.add("0");
//                   }
//
//                    int purCountDay = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
//                    if (purCountDay == 1) {
//
//                        String price = gbDpgService.queryPurchaseGoodsPrice(mapDay);
//                        String weight = gbDpgService.queryPurchaseGoodsWeight(mapDay);
//
//                        priceDayValue.add(new BigDecimal(price).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        weightDayValue.add(new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                    } else if (purCountDay > 1) {
//                        Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);
//                        Double weightTotal = gbDpgService.queryPurchaseGoodsWeightTotal(mapDay);
//
//                        double v1 = subTotal / weightTotal;
//                        double v2 = weightTotal / purCountDay;
//
//                        priceDayValue.add(new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        weightDayValue.add(new BigDecimal(v2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                    } else {
//                        priceDayValue.add("0");
//                        weightDayValue.add("0");
//                    }
//                }
//            } else {
//                String substring = startDate.substring(8, 10);
//                dateList.add(substring);
//                mapDay.put("date", startDate);
//
//                int purCountDay = gbDpgService.queryGbPurchaseGoodsCount(mapDay);
//                if (purCountDay == 1) {
//                    String price = gbDpgService.queryPurchaseGoodsPrice(mapDay);
//                    String weight = gbDpgService.queryPurchaseGoodsWeight(mapDay);
//
//                    Map<String, Object> mapDayValue = new HashMap<>();
//                    mapDayValue.put("dayPrice", new BigDecimal(price).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    mapDayValue.put("dayWeight", new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                } else if (purCountDay > 1) {
//                    Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(mapDay);
//                    Double weightTotal = gbDpgService.queryPurchaseGoodsWeightTotal(mapDay);
//
//                    double v1 = subTotal / weightTotal;
//                    double v2 = weightTotal / purCountDay;
//
//                    Map<String, Object> mapDayValue = new HashMap<>();
//                    mapDayValue.put("dayPrice", new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    mapDayValue.put("dayWeight", new BigDecimal(v2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                } else {
//                    Map<String, Object> mapDayValue = new HashMap<>();
//                    mapDayValue.put("dayPrice", 0);
//                    mapDayValue.put("dayWeight", 0);
//                }
//
//            }
//
//
//            Map<String, Object> mapEveryDay = new HashMap<>();
//            mapEveryDay.put("produceValue", produceDayValue);
//            mapEveryDay.put("lossValue", lossDayValue);
//            mapEveryDay.put("wasteValue", wasteDayValue);
//            mapEveryDay.put("returnValue", returnDayValue);
//            mapEveryDay.put("priceValue", priceDayValue);
//            mapEveryDay.put("weightValue", weightDayValue);
//            mapEveryDay.put("dateList", dateList);
//            mapEveryDay.put("lowestList", lowestPriceList);
//            mapEveryDay.put("highestList", highestPriceList);
//            goodsEntity.setPurEveryDay(mapEveryDay);
//
//            Map<String, Object> mapStars = new HashMap<>();
//            mapStars.put("startDate", startDate);
//            mapStars.put("stopDate", stopDate);
//            mapStars.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
//            mapStars.put("starsLevel", 5);
//            System.out.println("mapStarsmapStarsmapStars" + mapStars);
//            Integer starsFive = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
//            mapStars.put("starsLevel", 4);
//            Integer starsFour = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
//            mapStars.put("starsLevel", 3);
//            Integer starsThree = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
//            mapStars.put("starsLevel", 2);
//            Integer starsTwo = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
//            mapStars.put("starsLevel", 1);
//            Integer starsOne = gbDepartmentGoodsStockService.queryGoodsStarsTimes(mapStars);
//            Map<String, Object> mapStar = new HashMap<>();
//            mapStar.put("starsFive", starsFive);
//            mapStar.put("starsFour", starsFour);
//            mapStar.put("starsThree", starsThree);
//            mapStar.put("starsTwo", starsTwo);
//            mapStar.put("starsOne", starsOne);
//
//            Map<String, Object> mapResult = new HashMap<>();
//            mapResult.put("totalCost", new BigDecimal(doutbleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//            mapResult.put("totalCostSubtotal", new BigDecimal(doutbleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//            mapResult.put("maxPrice", maxPrice);
//            mapResult.put("minPrice", minPrice);
//            mapResult.put("perPrice", perPrice);
//            mapResult.put("purCount", purCount);
//            mapResult.put("starsCount", mapStar);
//
//            goodsEntity.setGoodsData(mapResult);
//
//            map1.put("dayuStatus", null);
//            System.out.println("stoosososossososososos" + map1);
//
//            double goodsDoutbleTotalV = 0;
//            double goodsDoutbleTotal = 0;
//            double goodsDoutbleCost = 0;
//            double goodsDoutbleCostV = 0;
//            double goodsDoutbleRestV = 0;
//            double goodsDoutbleRest = 0;
//            double goodsDoutbleLoss = 0;
//            double goodsDoutbleLossV = 0;
//            double goodsDoutbleWaste = 0;
//            double goodsDoutbleWasteV = 0;
//            double goodsDoutbleProduce = 0;
//            double goodsDoutbleProduceV = 0;
//            double goodsDoutbleReturnV = 0;
//            double goodsDoutbleReturn = 0;
//
//            Map<String, Object> mapDepStock = new HashMap<>();
//            mapDepStock.put("startDate", startDate);
//            mapDepStock.put("stopDate", stopDate);
//            mapDepStock.put("disGoodsId", disGoodsId);
//            System.out.println("mapDepStockmapDepStock" + mapDepStock);
//            Integer integer2 = gbDepartmentGoodsStockService.queryGoodsStockCount(mapDepStock);
//            if (integer2 > 0) {
//                goodsDoutbleTotal = gbDepartmentGoodsStockService.queryDepStockWeightTotal(mapDepStock);
//                goodsDoutbleTotalV= gbDepartmentGoodsStockService.queryDepGoodsSubtotal(mapDepStock);
//
//                goodsDoutbleRest = gbDepartmentGoodsStockService.queryDepGoodsRestWeightTotal(mapDepStock);
//                System.out.println("reststockkkkweiieieie" + goodsDoutbleRest);
//                goodsDoutbleRestV = gbDepartmentGoodsStockService.queryDepGoodsRestTotal(mapDepStock);
//
//                mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
//                System.out.println("pururururuururu" + mapDepStock);
//                Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
//                if (integerProduce > 0) {
//                    goodsDoutbleProduceV = gbDepartmentStockReduceService.queryReduceProduceTotal(mapDepStock);
//                    goodsDoutbleProduce = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(mapDepStock);
//                    goodsDoutbleCost = goodsDoutbleCost + goodsDoutbleProduce ;
//                    System.out.println("goodsDoutbleCostgoodsDoutbleCostpppp" + goodsDoutbleCost);
//                } else {
//                    goodsDoutbleProduceV = 0;
//                    goodsDoutbleProduce = 0;
//                }
//                mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
//                Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
//                if (integerLoss > 0) {
//                    goodsDoutbleLossV = gbDepartmentStockReduceService.queryReduceLossTotal(mapDepStock);
//                    goodsDoutbleLoss = gbDepartmentStockReduceService.queryReduceLossWeightTotal(mapDepStock);
//                    goodsDoutbleCost = goodsDoutbleCost + goodsDoutbleLoss ;
//                    System.out.println("goodsDoutbleCostgoodsDoutbleCostllll" + goodsDoutbleCost);
//                } else {
//                    goodsDoutbleLossV = 0;
//                    goodsDoutbleLoss = 0;
//                }
//                mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
//                Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
//                if (integerWaste > 0) {
//                    goodsDoutbleWasteV = gbDepartmentStockReduceService.queryReduceWasteTotal(mapDepStock);
//                    goodsDoutbleWaste = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(mapDepStock);
//                    goodsDoutbleCost = goodsDoutbleCost + goodsDoutbleWaste ;
//                } else {
//                    goodsDoutbleWasteV = 0;
//                    goodsDoutbleWaste = 0;
//                }
//                mapDepStock.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
//                Integer integerReturn = gbDepartmentStockReduceService.queryReduceTypeCount(mapDepStock);
//                if (integerReturn > 0) {
//                    goodsDoutbleReturnV = gbDepartmentStockReduceService.queryReduceReturnTotal(mapDepStock);
//                    goodsDoutbleReturn = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(mapDepStock);
//                } else {
//                    goodsDoutbleReturnV = 0;
//                }
//
//
//                System.out.println("goodsDoutbleCostgoodsDoutbleCostllll33333" + goodsDoutbleCost);
//
//
//                BigDecimal proPercent = new BigDecimal(goodsDoutbleProduce).divide(new BigDecimal(doutbleCost),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
//                BigDecimal lossPercent = new BigDecimal(goodsDoutbleLoss).divide(new BigDecimal(doutbleCost),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
//                BigDecimal wastePercent = new BigDecimal(goodsDoutbleWaste).divide(new BigDecimal(doutbleCost),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
//                BigDecimal retPercent = new BigDecimal(goodsDoutbleReturn).divide(new BigDecimal(doutbleCost),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
//
//                goodsEntity.setGoodsProducePercent(proPercent.toString());
//                goodsEntity.setGoodsLossPercent(lossPercent.toString());
//                goodsEntity.setGoodsWastePercent(wastePercent.toString());
//                goodsEntity.setGoodsReturnPercent(retPercent.toString());
//
//                goodsEntity.setGoodsPurTotalWeight(String.format("%.1f", goodsDoutbleTotal));
//                goodsEntity.setGoodsPurTotalSubtotal(String.format("%.1f", goodsDoutbleTotalV));
//
//                goodsEntity.setGoodsCostWeightTotalString(String.format("%.1f", goodsDoutbleCost));
//
//                BigDecimal totalPercent = new BigDecimal(goodsDoutbleCost).divide(new BigDecimal(goodsDoutbleTotal),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
//
//                goodsEntity.setGoodsCostTotalString(totalPercent.toString());
//
//                goodsEntity.setGoodsStockTotalString(String.format("%.1f", goodsDoutbleRest));
//
//
//                BigDecimal stockPercent = new BigDecimal(goodsDoutbleRest).divide(new BigDecimal(goodsDoutbleTotal),3,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1,BigDecimal.ROUND_HALF_UP);
//
//
//                goodsEntity.setGoodsStockWeightTotalString(stockPercent.toString());
//
//                goodsEntity.setGoodsProduceWeightTotalString(String.format("%.1f", goodsDoutbleProduce));
//                goodsEntity.setGoodsProduceTotalString(String.format("%.1f", goodsDoutbleProduceV));
//
//                goodsEntity.setGoodsLossWeightTotalString(String.format("%.1f", goodsDoutbleLoss));
//                goodsEntity.setGoodsLossTotalString(String.format("%.1f", goodsDoutbleLossV));
//
//                goodsEntity.setGoodsWasteWeightTotalString(String.format("%.1f", goodsDoutbleWaste));
//                goodsEntity.setGoodsWasteTotalString(String.format("%.1f", goodsDoutbleWasteV));
//
//                goodsEntity.setGoodsReturnWeightTotalString(String.format("%.1f", goodsDoutbleReturn));
//                goodsEntity.setGoodsReturnTotalString(String.format("%.1f", goodsDoutbleReturnV));
//
//                BigDecimal divide = new BigDecimal(integer2).divide(new BigDecimal(howManyDaysInPeriod), 1, BigDecimal.ROUND_HALF_UP);
//
//                goodsEntity.setGoodsAverageOrderTimes(divide.toString());
//                int total = gbDepartmentGoodsStockService.queryGoodsStockStars(mapDepStock);
//                System.out.println("totaltotaltotaltotal" + total);
//                System.out.println("integer2integer2" + integer2);
//                int i = total / integer2;
//                int remainder = total % integer2;
//                int gray = 0;
//                if (remainder > 0) {
//                    remainder = 1;
//                }
//                gray = 5 - i - remainder;
//                System.out.println("abccbcbcbcbcbcbcb" + i + "gray" + gray + "rem" + remainder);
//                goodsEntity.setGoodsStarGreen(i);
//                goodsEntity.setGoodsStarHalf(remainder);
//                goodsEntity.setGoodsStarGray(gray);
//                goodsEntity.setGoodsAverageStars(i);
//                goodsEntity.setGoodsAverageStarsString(String.valueOf(i));
//                goodsEntity.setGoodsPurTotalCount(integer2);
//            }
//
//        }else{
//            Map<String, Object> mapResult = new HashMap<>();
//
//            mapResult.put("totalCost","0");
//            goodsEntity.setGoodsData(mapResult);
//        }
//
//
//        return R.ok().put("data", goodsEntity);
//    }


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
        map.put("purchaseType", 2);
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
        double doutbleCost = 0;
        double doutbleCostV = 0;
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
        map.put("buySubtotal", 0);
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
            purchaseGoodsEntities = gbDpgService.queryPurchaseGoodsByGoodsId(map);

            if (purchaseGoodsEntities.size() > 0) {
                for (GbDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                    Integer purchaseGoodsId = purchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("purGoodsId", purchaseGoodsId);
                    Double aDoubleRE = gbDepartmentGoodsStockService.queryDepStockRestWeightTotal(mapS);
                    Double aDoubleL = gbDepartmentGoodsStockService.queryDepStockLossWeightTotal(mapS);
                    Double aDoubleW = gbDepartmentGoodsStockService.queryDepGoodsWasteWeightTotal(mapS);
                    Double aDoubleR = gbDepartmentGoodsStockService.queryDepStockReturnWeightTotal(mapS);
                    Double aDoubleP = gbDepartmentGoodsStockService.queryDepStockProduceWeightTotal(mapS);
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


        return R.ok().put("data", mapResult);


    }

    @RequestMapping(value = "/depGetToPrintPurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R depGetToPrintPurGoods(Integer depId, Integer userId) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("purDepId", depId);
        map4.put("buyStatus", 1);
        map4.put("weightId", -1);
        map4.put("purchaseType", 0);
        System.out.println("map4444444" + map4);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDpgService.queryDisPurchaseGoods(map4);
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("depId", depId);
//        map.put("status", 1);
//        map.put("type", 2);
//        int countWeight = gbDistributerWeightTotalService.queryDepWeightCountByParams(map);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depId);
        map3.put("date", formatWhatDay(0));
        map3.put("type", 2);
        int count = gbDistributerWeightTotalService.queryDepWeightCountByParams(map3);
        BigDecimal trade = new BigDecimal(count).add(new BigDecimal(1));
        String name = "CGD";

        String s = formatWhatDayString(0) + name + "-" + trade;

        Map<String, Object> map1 = new HashMap<>();
        map1.put("arr", fatherGoodsEntities);
//        map1.put("weightTotal", countWeight);
        map1.put("tradeNo", s);

        Map<String, Object> mapW = new HashMap<>();
        mapW.put("depId", depId);
        mapW.put("equalStatus", 0);
        mapW.put("isSelf", -1);
        List<GbDistributerWeightTotalEntity> weightTotalEntities = gbDisWeightTotalService.queryDepWeightListByParams(mapW);
        map1.put("weightArr", weightTotalEntities);

        return R.ok().put("data", map1);
    }


    @RequestMapping(value = "/getPurchasePurCash", method = RequestMethod.POST)
    @ResponseBody
    public R getPurchasePurCash(Integer userId, String date) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("finishDate", date);
        map.put("payType", 0);
        List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDpgService.queryPurchaseGoodsByParams(map);

        Map<String, Object> map1 = new HashMap<>();
        if (goodsEntities.size() > 0) {
            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
            map1.put("total", aDouble);
            map1.put("arr", goodsEntities);

        } else {
            map1.put("total", 0);
            map1.put("arr", new ArrayList<>());
        }

        return R.ok().put("data", map1);
    }


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


    @RequestMapping(value = "/getDisPurchaserDateJingjing", method = RequestMethod.POST)
    @ResponseBody
    public R getDisPurchaserDateJingjing(Integer disId, String startDate, String stopDate, Integer type,
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
                if (type == 2) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", whichDay);
                    map.put("disId", disId);
                    map.put("purchaseType", type);
                    map.put("payType", 0);
                    map.put("dayuStatus", 2);
                    System.out.println("pauboodosnnxnxnnxnn22222mmmmmmm" + map);

                    Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
                    BigDecimal batchBillTotal = new BigDecimal(0);
                    if (integer > 0) {
                        Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                        batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    Map<String, Object> mapItem = new HashMap<>();
                    mapItem.put("day", whichDay);
                    mapItem.put("value", batchBillTotal);
                    itemList.add(mapItem);
                } else if (type == 21) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", whichDay);
                    map.put("disId", disId);
                    map.put("purchaseType", type);
                    map.put("dayuStatus", 2);
                    System.out.println("pauboodosnnxnxnnxnnx521212111112112" + map);

                    Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
                    BigDecimal batchBillTotal = new BigDecimal(0);
                    if (integer > 0) {
                        Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                        batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    Map<String, Object> mapItem = new HashMap<>();
                    mapItem.put("day", whichDay);
                    mapItem.put("value", batchBillTotal);
                    itemList.add(mapItem);
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("disId", disId);
                    map.put("date", whichDay);
                    System.out.println("pauboodosnnxnxnnxnnx55551111" + map);
                    int integer = gbDepartmentBillService.queryDepartmentBillCount(map);
                    BigDecimal maileTotal = new BigDecimal(0);
                    if (integer > 0) {
                        Double aDouble = gbDepartmentBillService.queryGbDepBillsSubTotal(map);
                        maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    Map<String, Object> mapItem = new HashMap<>();
                    mapItem.put("day", whichDay);
                    mapItem.put("value", maileTotal);
                    itemList.add(mapItem);
                }

            }

        } else {
            if (type == 2) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", startDate);
                map.put("disId", disId);
                map.put("purchaseType", type);
                map.put("payType", 0);
                map.put("dayuStatus", 2);
                System.out.println("pauboodosnnxnxnnxnn22222" + map);

                Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
                BigDecimal batchBillTotal = new BigDecimal(0);
                if (integer > 0) {
                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                    batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("day", startDate);
                mapItem.put("value", batchBillTotal);
                itemList.add(mapItem);
            } else if (type == 21) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", startDate);
                map.put("disId", disId);
                map.put("purchaseType", type);
                map.put("dayuStatus", 2);
                Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
                BigDecimal batchBillTotal = new BigDecimal(0);
                if (integer > 0) {
                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
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
                map.put("purchaseType", 5);
                map.put("dayuStatus", 2);
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

                System.out.println("pauboodosdddd" + map);
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
        map.put("purchaseType", 2);
        map.put("payType", 0);
        map.put("dayuStatus", 2);
        System.out.println("zahsuishsissisiisisiis" + map);
        Integer integer1 = gbDpgService.queryGbPurchaseGoodsCount(map);
        BigDecimal purchaseTotal = new BigDecimal(0);
        if (integer1 > 0) {
            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
            purchaseTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }


        map.put("payType", null);
        map.put("purchaseType", 21);
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
        mapG.put("disId", disId);
        mapG.put("startDate", startDate);
        mapG.put("stopDate", stopDate);
        System.out.println("zashsuissiisiisiddddddmapGmapG" + mapG);
        Integer integer = gbDepartmentBillService.queryDepartmentBillCount(mapG);
        BigDecimal appTotal = new BigDecimal(0);
        if (integer > 0) {
            Double aDouble = gbDepartmentBillService.queryGbDepBillsSubTotal(mapG);
            appTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        Map<String, Object> map123 = new HashMap<>();
        map123.put("appTotal", appTotal);
        map123.put("purchaseTotal", purchaseTotal);
        map123.put("orderTotal", orderTotal);
        map123.put("arr", itemList);


        return R.ok().put("data", map123);
    }

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
//        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDpgService.queryPurchaseGoodsByParams(map);
        List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();
        List<GbDistributerFatherGoodsEntity> greatGrandPurGoodsDetial = gbDpgService.queryGreatGrandPurGoodsDetail(map);
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


    @RequestMapping(value = "/disGetPurchaserGoodsByDate", method = RequestMethod.POST)
    @ResponseBody
    public R disGetPurchaserGoodsByDate(Integer disId, String date, String searchDepIds, String searchDepId) {

        //购买采购商品 batchId == -1
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("finishDate", date);
        map.put("payType", 0);
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

        List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();
        System.out.println("pururugoogogogooodod" + map);
        List<GbDistributerFatherGoodsEntity> greatGrandPurGoodsDetial = gbDpgService.queryGreatGrandPurGoodsDetail(map);
        if (greatGrandPurGoodsDetial.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrand : greatGrandPurGoodsDetial) {
                List<GbDistributerFatherGoodsEntity> grandFatherGoodsEntities = greatGrand.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grand : grandFatherGoodsEntities) {
                    result.addAll(grand.getFatherGoodsEntities());
                }
            }
        }
        List<GbDepartmentEntity> deplist = new ArrayList<>();
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");

            for (String depId : arrGb) {
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(depId));
                Double aDoubleDep = 0.0;
                map.put("purDepId", departmentEntity.getGbDepartmentId());
                map.put("purDepIds", null);
                int count = gbDpgService.queryGbPurchaseGoodsCount(map);
                if (count > 0) {
                    aDoubleDep = gbDpgService.queryPurchaseGoodsSubTotal(map);
                }

                departmentEntity.setDepCostGoodsTotalString(new BigDecimal(aDoubleDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                deplist.add(departmentEntity);
            }
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", result);
        mapR.put("depArr", deplist);
        return R.ok().put("data", mapR);
    }

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
        greatGrandPurGoodsDetial = gbDpgService.queryGreatGrandPurGoodsDetail(map);
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

    @RequestMapping(value = "/getPurchaserDateBill", method = RequestMethod.POST)
    @ResponseBody
    public R getPurchaserDateBill(Integer purDepId, String startDate, String stopDate) {

        //购买采购商品batchId == -1
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", purDepId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("batchId", -1);
        map.put("payType", 0);
        map.put("dayuStatus", 1);
        List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDpgService.queryPurchaseGoodsByParams(map);

        BigDecimal maileTotal = new BigDecimal(0);
        if (goodsEntities.size() > 0) {
            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
            maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            maileTotal = new BigDecimal(0);
        }


        //现金订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("purDepId", purDepId);
        map1.put("startDate", startDate);
        map1.put("stopDate", stopDate);
        map1.put("payType", 0);
        List<GbDistributerPurchaseBatchEntity> entitiesCash = gbDPBService.queryDisPurchaseBatch(map1);
        BigDecimal batchCashTotal = new BigDecimal(0);
        if (entitiesCash.size() > 0) {
            Double aDouble = gbDPBService.queryPurchaserCashTotal(map1);
            batchCashTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            batchCashTotal = new BigDecimal(0);
        }


        Map<String, Object> map2 = new HashMap<>();
        map2.put("purDepId", purDepId);
        map2.put("startDate", startDate);
        map2.put("stopDate", stopDate);
        map2.put("payType", 1);
        List<GbDistributerPurchaseBatchEntity> entitiesBill = gbDPBService.queryDisPurchaseBatch(map2);
        BigDecimal batchBillTotal = new BigDecimal(0);
        if (entitiesBill.size() > 0) {
            Double aDouble = gbDPBService.queryPurchaserCashTotal(map2);
            batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            batchBillTotal = new BigDecimal(0);
        }

        BigDecimal add = maileTotal.add(batchCashTotal).add(batchBillTotal).setScale(2, BigDecimal.ROUND_HALF_UP);

        Map<String, Object> map123 = new HashMap<>();
        map123.put("total", add);
        map123.put("maileTotal", maileTotal);
        map123.put("batchCashTotal", batchCashTotal);
        map123.put("batchBillTotal", batchBillTotal);
        map123.put("maileArr", goodsEntities);
        map123.put("batchCashArr", entitiesCash);
        map123.put("batchBillArr", entitiesBill);

        return R.ok().put("data", map123);
    }


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
                List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDpgService.queryPurchaseGoodsByParams(map);
                if (goodsEntities.size() > 0) {
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
                List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDpgService.queryPurchaseGoodsByParams(map);
                if (goodsEntities.size() > 0) {
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
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDpgService.queryPurchaseInventoryGoodsList(map);
        if (purchaseGoodsEntities.size() > 0) {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("depId", depId);
            map1.put("startDate", startDate);
            map1.put("stopDate", stopDate);
            map1.put("type", type);
            total = gbDpgService.queryPurchaseInventoryGoodsSubTotal(map1);
        }
        return total;
    }

    @RequestMapping(value = "/getJicaiMangeData")
    @ResponseBody
    public R getJicaiMangeData(Integer depId, Integer disId) {

        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depId);
        int settleWeek = Integer.parseInt(departmentEntity.getGbDepartmentSettleWeek());
        int weekOfYear = getWeekOfYear(0) + 1;
        int start = weekOfYear - 4;
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = start; i < weekOfYear; i++) {
            Map<String, Object> weekJicaiData = getWeekJicaiData(i, depId);
            result.add(weekJicaiData);
        }
        return R.ok().put("data", result);

    }


    private Map<String, Object> getWeekJicaiData(int i, Integer depId) {

        String formatSubtotal = "0.0";
        String formathighestTotal = "0.0";
        String formatlowestTotal = "0.0";
        Map<String, Object> map5 = new HashMap<>();

        //采购总额
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", depId);
        map.put("status", 4);
        map.put("week", i);
        int i1 = gbDpgService.queryPurchaseGoodsAmount(map);
        if (i1 > 0) {
            Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(map);
            formatSubtotal = String.format("%.2f", subTotal);
        }


        //2, highest
        //价格高总额
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depId", depId);
        map1.put("status", 3);
        map1.put("purWhat", 1);
        map1.put("week", i);
        int highestAmount = gbDistributerGoodsPriceService.queryPriceWhatAmount(map1);
        if (highestAmount > 0) {
            Double highestTotal = gbDistributerGoodsPriceService.queryPriceWhatTotal(map1);
            formathighestTotal = String.format("%.2f", highestTotal);
        }

        //3. lowest
        //价格低总额
        Map<String, Object> map2 = new HashMap<>();
        map2.put("depId", depId);
        map2.put("status", 3);
        map2.put("purWhat", -1);
        map2.put("week", i);
        int lowestAmount = gbDistributerGoodsPriceService.queryPriceWhatAmount(map2);
        if (lowestAmount > 0) {
            Double lowestTotal = gbDistributerGoodsPriceService.queryPriceWhatTotal(map2);
            formatlowestTotal = String.format("%.2f", lowestTotal);
        }

        map5.put("subtotal", formatSubtotal);
        map5.put("highestTotal", formathighestTotal);
        map5.put("lowestTotal", formatlowestTotal);

        Map<String, Object> map4 = new HashMap<>();
        map4.put("week", i);
        map4.put("arr", map5);

        return map4;

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

    @RequestMapping(value = "/mendainGetDateSelfPurchaseGoods", method = RequestMethod.POST)
    @ResponseBody
    public R mendainGetDateSelfPurchaseGoods(Integer depFatherId, String date, Integer payType) {
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", depFatherId);
        map.put("finishDate", date);
        map.put("payType", payType);

        List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities = gbDpgService.queryPurchaseGoodsWithDetailByParams(map);

        return R.ok().put("data", gbDistributerPurchaseGoodsEntities);
    }


//    @RequestMapping(value = "/disGetHaveFinishedPurGoodsGb/{disId}")
//    @ResponseBody
//    public R disGetHaveFinishedPurGoodsGb(@PathVariable Integer disId) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", disId);
//        map.put("equalStatus", 1);
//        map.put("purchaseType", 0);
//        List<GbDistributerPurchaseGoodsEntity> disPurGoodsEntities = gbDpgService.queryPurchaseGoodsWithDetailByParams(map);
//
//        return R.ok().put("data", disPurGoodsEntities);
//    }

    @RequestMapping(value = "/purchaserGetHaveFinishedPurGoodsGb")
    @ResponseBody
    public R purchaserGetHaveFinishedPurGoodsGb(Integer userId, Integer orderType, Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("equalStatus", 1);
        map.put("batchId", -1);
//        List<GbDistributerPurchaseGoodsEntity> disPurGoodsEntities = gbDpgService.queryPurchaseGoodsWithDetailByParams(map);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDpgService.queryDisPurchaseGoodsForNxDis(map);
        Map<String, Object> map11 = new HashMap<>();
        map11.put("purDepId", depId);
        map11.put("status", 1);
        int count0 = gbDpgService.queryPurchaseGoodsAmount(map11);

        Map<String, Object> map12 = new HashMap<>();
        map12.put("purUserId", userId);
        map12.put("status", 2);
        Integer count1 = gbDPBService.queryDisPurchaseBatchCount(map12);

        Map<String, Object> map13 = new HashMap<>();
        map13.put("purUserId", userId);
        map13.put("equalStatus", 1);
        map13.put("batchId", -1);
        map13.put("weightId", -1);
        System.out.println("mappapap13" + map13);
        Integer count2 = gbDpgService.queryGbPurchaseGoodsCount(map13);

        Map<String, Object> map14 = new HashMap<>();
        map14.put("toDepId", depId);
        map14.put("equalStatus", 2);
        map14.put("orderType", orderType);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryDistributerTodayDepartments(map14);
        Map<String, Object> todayData = packageDisOrderByDep(departmentEntities, 0);
        List<GbDepartmentEntity> arr = (List<GbDepartmentEntity>) todayData.get("arr");

        Map<String, Object> map3 = new HashMap<>();
        map3.put("arr", fatherGoodsEntities);
        map3.put("purGoodsAmount", count0);
        map3.put("haoyouAmount", count1);
        map3.put("finishAmount", count2);
        map3.put("deliveryAmount", arr.size());

        return R.ok().put("data", map3);
    }


    @RequestMapping(value = "/getDatePurchaseGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getDatePurchaseGoods(Integer disId, String date) {
        System.out.println(date);
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purchaseDate", date);
        List<GbDistributerFatherGoodsEntity> purchase = gbDpgService.queryDisPurchaseGoods(map);
        return R.ok().put("data", purchase);
    }

    @RequestMapping(value = "/supplierGetPurchaseGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R supplierGetPurchaseGoodsGb(Integer depId, Integer supplierId) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("toDepId", depId);
        map4.put("status", 3);
        map4.put("supplierId", supplierId);
        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryDisPurchaseGoods(map4);
        return R.ok().put("data", purchaseToday);
    }

    @RequestMapping(value = "/getPurchaseGoodsGbWithOutSupplier", method = RequestMethod.POST)
    @ResponseBody
    public R getPurchaseGoodsGbWithOutSupplier(Integer depId, Integer userId, Integer orderType) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("toDepId", depId);
        map4.put("status", 1);
        map4.put("supplierId", -1);
        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryDisPurchaseGoods(map4);
        return R.ok().put("data", purchaseToday);
    }

    @RequestMapping(value = "/getPurchaseGoodsGb/{depId}")
    @ResponseBody
    public R getPurchaseGoodsGb(@PathVariable Integer depId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("toDepId", depId);
        map4.put("buyStatus", 1);
        map4.put("type", 2);
        System.out.println("zahuisedepdid" + map4);

        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryDisPurchaseGoods(map4);

        return R.ok().put("data", purchaseToday);
    }


    @RequestMapping(value = "/getPurchaseGoodsGbSx/{depId}")
    @ResponseBody
    public R getPurchaseGoodsGbSx(@PathVariable Integer depId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("toDepId", depId);
        map4.put("buyStatus", 1);
        map4.put("type", 2);
        System.out.println("zahuisedepdid" + map4);

        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryGrandPurchaseGoods(map4);

        return R.ok().put("data", purchaseToday);
    }

    @RequestMapping(value = "/nxDisSavePurGoodsPriceSx", method = RequestMethod.POST)
    @ResponseBody
    public R nxDisSavePurGoodsPriceSx(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {
        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purGoods.getGbDepartmentOrdersEntities();
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
            int warnHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWarnHour());
            int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
            purGoods.setGbDpgWarnFullTime(formatWhatFullTime(warnHour));
            purGoods.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
            gbDpgService.update(purGoods);
        }

        return R.ok();
    }

    @RequestMapping(value = "/nxDisSavePurGoodsPrice", method = RequestMethod.POST)
    @ResponseBody
    public R nxDisSavePurGoodsPrice(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {
        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purGoods.getGbDepartmentOrdersEntities();
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
            int warnHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWarnHour());
            int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
            purGoods.setGbDpgWarnFullTime(formatWhatFullTime(warnHour));
            purGoods.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
            gbDpgService.update(purGoods);
        }

        return R.ok();
    }


    @RequestMapping(value = "/nxDisSavePurGoodsPrice1", method = RequestMethod.POST)
    @ResponseBody
    public R nxDisSavePurGoodsPrice1(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {
        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purGoods.getGbDepartmentOrdersEntities();
        BigDecimal weightTotal = new BigDecimal(0);
        for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
            NxDepartmentOrdersEntity nxDepartmentOrdersEntity = gbDepartmentOrdersEntity.getNxDepartmentOrdersEntity();
            Integer nxDoStatus = nxDepartmentOrdersEntity.getNxDoPurchaseStatus();
            if (nxDoStatus == 3) {
                weightTotal = weightTotal.add(new BigDecimal(nxDepartmentOrdersEntity.getNxDoWeight()));
                nxDepartmentOrdersEntity.setNxDoStatus(2);
            }
            nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);
        }
        purGoods.setGbDpgBatchId(-1);
        purGoods.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement()); // 1
        purGoods.setGbDpgPayType(1);
        purGoods.setGbDpgTime(formatWhatTime(0));
        purGoods.setGbDpgPurchaseDate(formatWhatDay(0));
        purGoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
        purGoods.setGbDpgPurchaseYear(formatWhatYear(0));
        purGoods.setGbDpgPurchaseWeek(getWeek(0));
        purGoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
        purGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
        purGoods.setGbDpgBuyQuantity(weightTotal.toString());
        BigDecimal decimal = new BigDecimal(purGoods.getGbDpgBuyPrice()).multiply(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP);
        purGoods.setGbDpgBuySubtotal(decimal.toString());
        purGoods.setGbDpgStatus(getGbPurchaseGoodsStatusFinished()); // 2
        Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purGoods);
        }

        gbDpgService.update(purGoods);

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
        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryDisPurchaseGoodsForNxDis(map4);

        return R.ok().put("data", purchaseToday);
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
        map4.put("shixianId", nxDisId);
        map4.put("type", 2);
        System.out.println("map4444444=====" + map4);
        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryDisPurchaseGoods(map4);
        List<GbDistributerPurchaseGoodsEntity> result = new ArrayList<>();
        if (purchaseToday.size() > 0) {
            for (GbDistributerFatherGoodsEntity fatherGoodsEntity : purchaseToday) {
                if (fatherGoodsEntity.getFatherGoodsEntities().size() > 0) {
                    for (GbDistributerFatherGoodsEntity fatherGoodsEntity1 : fatherGoodsEntity.getFatherGoodsEntities()) {
                        List<GbDistributerPurchaseGoodsEntity> gbDistributerGoodsEntities = fatherGoodsEntity1.getGbDistributerPurchaseGoodsEntities();
                        result.addAll(gbDistributerGoodsEntities);
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
                mapG.put("couponId", couponEntity.getNxDistributerCouponId());
                System.out.println("sucoudpdpd" + mapG);
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
            mapN.put("type", getGbDepartmentTypeAppSupplier());
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
        gbDistributerEntity.setGbDistributerBusinessType(1);
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
    @RequestMapping(value = "/getPurchaseGoodsGbWithTabCount")
    @ResponseBody
    public R getPurchaseGoodsGbWithTabCount(Integer depId, Integer disId, Integer appDepId, Integer nxDisId) {

        Map<String, Object> map4 = new HashMap<>();
//        map4.put("toDepId", depId);
        map4.put("buyStatus", 1);
        map4.put("disId", disId);
        map4.put("dayuBuyStatus", -2);
        map4.put("status", 3);
        map4.put("shixianId", nxDisId);
        map4.put("type", 2);
        System.out.println("map4444444=====" + map4);
        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryDisPurchaseGoods(map4);
        List<GbDistributerPurchaseGoodsEntity> result = new ArrayList<>();
        if (purchaseToday.size() > 0) {
            for (GbDistributerFatherGoodsEntity fatherGoodsEntity : purchaseToday) {
                if (fatherGoodsEntity.getFatherGoodsEntities().size() > 0) {
                    for (GbDistributerFatherGoodsEntity fatherGoodsEntity1 : fatherGoodsEntity.getFatherGoodsEntities()) {
                        List<GbDistributerPurchaseGoodsEntity> gbDistributerGoodsEntities = fatherGoodsEntity1.getGbDistributerPurchaseGoodsEntities();
                        result.addAll(gbDistributerGoodsEntities);
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


        Map<String, Object> map3 = new HashMap<>();
        map3.put("arr", result);
        map3.put("orderAmount", purCount);
        map3.put("wxAmount", purCountOne);
        map3.put("appAmount", count2);
        System.out.println("disinsofofooofofo" + disId);
        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(disId);
        System.out.println("gbdisiis" + gbDistributerEntity.getNxDistributerEntity());

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


    @RequestMapping(value = "/mendianGetSelfPurchaseGoods/{depFatherId}")
    @ResponseBody
    public R mendianGetSelfPurchaseGoods(@PathVariable Integer depFatherId) {

        Map<String, Object> map1 = new HashMap<>();

        map1.put("fatherDepId", depFatherId);
        map1.put("toDepId", depFatherId);
//        map1.put("orderType", getGbOrderTypeZiCai());
        map1.put("status", 3);
        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryDisPurchaseGoods(map1);
        Map<String, Object> map4 = new HashMap<>();
        map4.put("depId", depFatherId);
        map4.put("equalStatus", -1);
        Integer amount = gbDepartmentBillService.queryBillsCountByParamsGb(map4);
        Map<String, Object> map = new HashMap<>();
        map.put("arr", purchaseToday);
        map.put("newAmount", amount);
        return R.ok().put("data", map);
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

    @RequestMapping(value = "/getStockPurchaseGoodsGb0/{depId}")
    @ResponseBody
    public R getStockPurchaseGoodsGb0(@PathVariable Integer depId) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("depFatherId", depId);
        map4.put("status", 3);
        map4.put("toDepId", depId);
        System.out.println("roroorrr444444" + map4);
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
        map.put("equalStatus", -1);
        List<GbDepartmentBillEntity> billEntities = gbDepartmentBillService.queryBillsByParamsGb(map);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("depId", depId);
        mapR.put("status", 3);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(mapR);
        System.out.println("fdajaslfidasdniasfsalliangiweiwewieiei================");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("toDepId", depId);
        map2.put("buyStatus", 1);
        map2.put("dayuStatus", -1);
        map2.put("status", 3);
        map2.put("isNotSelf", 1);
        Integer amount2 = gbDepartmentOrdersService.queryTotalByParams(map2);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("arr", shelfEntityList);
        map1.put("billTotal", billEntities.size());
        map1.put("orderTotal", ordersEntities.size());
        map1.put("amount", amount2);


        Map<String, Object> map4B = new HashMap<>();
        map4B.put("depId", depId);
        map4B.put("equalStatus", -1);
        System.out.println("abdkdkdkdd" + map4B);
        List<GbDepartmentBillEntity> billEntities1 = gbDepartmentBillService.queryBillFromWhichDepartment(map4B);
        map1.put("receiveBills", billEntities1);


        return R.ok().put("data", map1);
    }


    @RequestMapping(value = "/cancleFinishPurGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R cancleFinishPurGoodsGb(@RequestBody GbDistributerPurchaseGoodsEntity purgoods) {
        List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = purgoods.getGbDepartmentOrdersEntities();

        for (GbDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            System.out.println("ordididiidieieiei" + orders.getGbDoStatus() + "bill" + orders.getGbDoBillId());
            if (orders.getGbDoStatus() < 4 && orders.getGbDoBillId() != null) {
                System.out.println("ordididiidieieiei" + orders.getGbDoStatus() + "bill" + orders.getGbDoBillId());
                Map<String, Object> map = new HashMap<>();
                map.put("billId", orders.getGbDoBillId());
                Integer orderAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
                if (orderAmount > 1) {
                    GbDepartmentBillEntity billEntity = gbDepartmentBillService.queryObject(orders.getGbDoBillId());
                    BigDecimal billtotal = new BigDecimal(billEntity.getGbDbTotal()).subtract(new BigDecimal(orders.getGbDoSubtotal())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    billEntity.setGbDbTotal(billtotal.toString());
                    gbDepartmentBillService.update(billEntity);
                } else {
                    gbDepartmentBillService.delete(orders.getGbDoBillId());
                }

                orders.setGbDoBillId(null);
            }

            orders.setGbDoStatus(getGbOrderStatusNew());
            orders.setGbDoBuyStatus(getGbOrderBuyStatusNew());
            orders.setGbDoPurchaseUserId(-1);
            orders.setGbDoPrice(null);
            orders.setGbDoWeight("0");
            orders.setGbDoSubtotal("0");
            orders.setGbDoScalePrice(null);
            orders.setGbDoScaleWeight(null);
            orders.setGbDoWeightGoodsId(null);
            orders.setGbDoWeightTotalId(null);
            gbDepartmentOrdersService.update(orders);

        }


// check goodsPrice

        Integer gbDpgDisGoodsPriceId = purgoods.getGbDpgDisGoodsPriceId();
        if (gbDpgDisGoodsPriceId != null) {
            System.out.println("dellelele" + purgoods.getGbDpgDisGoodsPriceId());
            gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);
        }
        purgoods.setGbDpgDisGoodsPriceId(null);
        purgoods.setGbDpgBatchId(-1);
        purgoods.setGbDpgBuyPrice(null);
//        purgoods.setGbDpgBuyQuantity(null);
        purgoods.setGbDpgBuySubtotal("0");
        purgoods.setGbDpgBuyPriceReason(null);
        purgoods.setGbDpgBuyScale(null);
        purgoods.setGbDpgStatus(0);
        purgoods.setGbDpgTime(null);
        purgoods.setGbDpgPurchaseDate(null);
        purgoods.setGbDpgPurchaseMonth(null);
        purgoods.setGbDpgPurchaseYear(null);
        purgoods.setGbDpgPurchaseWeek(null);
        purgoods.setGbDpgPurchaseWeekYear(null);
        purgoods.setGbDpgPurchaseFullTime(null);
        purgoods.setGbDpgBuyScaleQuantity(null);
        purgoods.setGbDpgBuyScalePrice(null);
        purgoods.setGbDpgWeightId(null);
        purgoods.setGbDpgPurUserId(null);
        gbDpgService.update(purgoods);


        return R.ok();
    }


    /**
     * 采购员完成采购
     *
     * @param purgoods
     * @return
     */
    @RequestMapping(value = "/markGbPurGoodsFinish", method = RequestMethod.POST)
    @ResponseBody
    public R markGbPurGoodsFinish(@RequestBody GbDistributerPurchaseGoodsEntity purgoods) {

        List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = purgoods.getGbDepartmentOrdersEntities();
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

        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(purgoods.getGbDpgDistributerId());
        BigDecimal subtract = new BigDecimal(gbDistributerEntity.getGbDistributerBuyQuantity()).subtract(new BigDecimal(finishOrder));
        gbDistributerEntity.setGbDistributerBuyQuantity(subtract.toString());
        gbDistributerService.update(gbDistributerEntity);


        //给没有选择订单新生成一个采购商品
        if (unChoiceOrderList.size() > 0) {
            GbDistributerPurchaseGoodsEntity newPurGoods = new GbDistributerPurchaseGoodsEntity();
            newPurGoods.setGbDpgDisGoodsFatherId(unChoiceOrderList.get(0).getGbDoDisGoodsFatherId());
            newPurGoods.setGbDpgDisGoodsId(unChoiceOrderList.get(0).getGbDoDisGoodsId());
            newPurGoods.setGbDpgDistributerId(unChoiceOrderList.get(0).getGbDoDistributerId());
            newPurGoods.setGbDpgApplyDate(formatWhatDay(0));
            newPurGoods.setGbDpgStatus(0);
            newPurGoods.setGbDpgOrdersAmount(unChoiceOrderList.size());
            newPurGoods.setGbDpgOrdersFinishAmount(0);
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

    @RequestMapping(value = "/giveStockPurGoodsPrice", method = RequestMethod.POST)
    @ResponseBody
    public R giveStockPurGoodsPrice(Integer id, String price) {
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryObject(id);
        purchaseGoodsEntity.setGbDpgBuyPrice(price);
        if (purchaseGoodsEntity.getGbDpgBuyQuantity() != null && !purchaseGoodsEntity.getGbDpgBuyQuantity().trim().isEmpty()) {
            BigDecimal quantityB = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
            BigDecimal multiply = quantityB.multiply(new BigDecimal(price)).setScale(1, BigDecimal.ROUND_HALF_UP);
            purchaseGoodsEntity.setGbDpgBuySubtotal(multiply.toString());
            purchaseGoodsEntity.setGbDpgStatus(3);
        } else {
            purchaseGoodsEntity.setGbDpgStatus(2);
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
                orders.setGbDoStatus(getGbOrderStatusHasBill());
                orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());

                Integer gbDoWeightTotalId = orders.getGbDoWeightTotalId();
                System.out.println("weitototoidiidd" + gbDoWeightTotalId);
                if (gbDoWeightTotalId != null) {
                    GbDistributerWeightTotalEntity gbWeightTotalEntity = gbDistributerWeightTotalService.queryObject(gbDoWeightTotalId);
                    BigDecimal gbGwtOrderFinishCount = new BigDecimal(gbWeightTotalEntity.getGbGwtOrderFinishCount());
                    BigDecimal add = gbGwtOrderFinishCount.add(new BigDecimal(1));
                    gbWeightTotalEntity.setGbGwtOrderFinishCount(add.toString());
                    if (add.compareTo(new BigDecimal(gbWeightTotalEntity.getGbGwtOrderCount())) == 0) {
                        gbWeightTotalEntity.setGbGwtStatus(getGbWeightTotalStatusFinished());
                    }
                    gbDistributerWeightTotalService.update(gbWeightTotalEntity);
                }

            } else {
                orders.setGbDoStatus(getGbOrderStatusProcurement());
                orders.setGbDoBuyStatus(getGbOrderStatusProcurement());
            }

            orders.setGbDoPurchaseUserId(purchaseGoodsEntity.getGbDpgPurUserId());
            gbDepartmentOrdersService.update(orders);

        }


        Integer gbDpgDisGoodsId = purchaseGoodsEntity.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purchaseGoodsEntity);
        }

        return R.ok();
    }


    @RequestMapping(value = "/giveStockPurGoodsWeight", method = RequestMethod.POST)
    @ResponseBody
    public R giveStockPurGoodsWeight(Integer id, String weight) {
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryObject(id);
        purchaseGoodsEntity.setGbDpgBuyQuantity(weight);
        if (purchaseGoodsEntity.getGbDpgBuyPrice() != null && !purchaseGoodsEntity.getGbDpgBuyPrice().trim().isEmpty()) {
            BigDecimal priceB = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
            BigDecimal multiply = priceB.multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            purchaseGoodsEntity.setGbDpgBuySubtotal(multiply.toString());
            purchaseGoodsEntity.setGbDpgStatus(3);
        } else {
            purchaseGoodsEntity.setGbDpgStatus(2);
        }
        gbDpgService.update(purchaseGoodsEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("purGoodsId", id);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
        for (GbDepartmentOrdersEntity orders : ordersEntities) {
            orders.setGbDoWeight(weight);
            if (orders.getGbDoPrice() != null && !orders.getGbDoPrice().trim().isEmpty()) {
                BigDecimal priceB = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
                BigDecimal multiply = priceB.multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                orders.setGbDoSubtotal(multiply.toString());
                orders.setGbDoStatus(getGbOrderStatusHasBill());
                orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());

                Integer gbDoWeightTotalId = orders.getGbDoWeightTotalId();
                System.out.println("weitototoidiidd" + gbDoWeightTotalId);
                if (gbDoWeightTotalId != null) {
                    GbDistributerWeightTotalEntity gbWeightTotalEntity = gbDistributerWeightTotalService.queryObject(gbDoWeightTotalId);
                    BigDecimal gbGwtOrderFinishCount = new BigDecimal(gbWeightTotalEntity.getGbGwtOrderFinishCount());
                    BigDecimal add = gbGwtOrderFinishCount.add(new BigDecimal(1));
                    gbWeightTotalEntity.setGbGwtOrderFinishCount(add.toString());
                    if (add.compareTo(new BigDecimal(gbWeightTotalEntity.getGbGwtOrderCount())) == 0) {
                        gbWeightTotalEntity.setGbGwtStatus(getGbWeightTotalStatusFinished());
                    }
                    gbDistributerWeightTotalService.update(gbWeightTotalEntity);
                }
            } else {
                orders.setGbDoStatus(getGbOrderStatusProcurement());
                orders.setGbDoBuyStatus(getGbOrderStatusProcurement());
            }

            orders.setGbDoPurchaseUserId(purchaseGoodsEntity.getGbDpgPurUserId());
            gbDepartmentOrdersService.update(orders);


        }

        return R.ok();
    }

    @RequestMapping(value = "/updateStockPurGoodsWeight", method = RequestMethod.POST)
    @ResponseBody
    public R updateStockPurGoodsWeight(Integer id, String weight) {
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryObject(id);
        purchaseGoodsEntity.setGbDpgBuyQuantity(weight);
        if (purchaseGoodsEntity.getGbDpgBuyPrice() != null && !purchaseGoodsEntity.getGbDpgBuyPrice().trim().isEmpty()) {
            BigDecimal priceB = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
            BigDecimal multiply = priceB.multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            purchaseGoodsEntity.setGbDpgBuySubtotal(multiply.toString());
            purchaseGoodsEntity.setGbDpgStatus(3);
        }

        gbDpgService.update(purchaseGoodsEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("purGoodsId", id);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
        for (GbDepartmentOrdersEntity orders : ordersEntities) {
            orders.setGbDoWeight(weight);
            if (orders.getGbDoPrice() != null && !orders.getGbDoPrice().trim().isEmpty()) {
                BigDecimal priceB = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
                BigDecimal multiply = priceB.multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                orders.setGbDoSubtotal(multiply.toString());
                orders.setGbDoStatus(getGbOrderStatusHasFinished());
                orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
            }
            gbDepartmentOrdersService.update(orders);
        }

        return R.ok();
    }

    /**
     * 采购员记录"完成采购"
     *
     * @param purGoods
     * @return
     */
    @RequestMapping(value = "/saveSelfPurGoodsOrdersCost", method = RequestMethod.POST)
    @ResponseBody
    public R saveSelfPurGoodsOrdersCost(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {

        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purGoods.getGbDepartmentOrdersEntities();
        for (GbDepartmentOrdersEntity orders : gbDepartmentOrdersEntities) {
            orders.setGbDoStatus(getGbOrderStatusHasFinished());
            orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
            orders.setGbDoPurchaseUserId(purGoods.getGbDpgPurUserId());
            gbDepartmentOrdersService.update(orders);

            Integer gbDoWeightTotalId = orders.getGbDoWeightTotalId();
            System.out.println("weitototoidiidd" + gbDoWeightTotalId);
            if (gbDoWeightTotalId != null) {
                GbDistributerWeightTotalEntity gbWeightTotalEntity = gbDistributerWeightTotalService.queryObject(gbDoWeightTotalId);
                BigDecimal gbGwtOrderFinishCount = new BigDecimal(gbWeightTotalEntity.getGbGwtOrderFinishCount());
                BigDecimal add = gbGwtOrderFinishCount.add(new BigDecimal(1));
                gbWeightTotalEntity.setGbGwtOrderFinishCount(add.toString());
                if (add.compareTo(new BigDecimal(gbWeightTotalEntity.getGbGwtOrderCount())) == 0) {
                    gbWeightTotalEntity.setGbGwtStatus(getGbWeightTotalStatusFinished());
                }
                gbDistributerWeightTotalService.update(gbWeightTotalEntity);
            }
        }

        purGoods.setGbDpgStatus(2);
        gbDpgService.update(purGoods);
        Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purGoods);
        }

        return R.ok();
    }


    /**
     * 采购员直接完成自采购商品，没有记录完成采购一步。
     * 暂时没有用到
     *
     * @param purGoods
     * @return
     */
    @RequestMapping(value = "/finishSelfPurGoodsOrdersCost", method = RequestMethod.POST)
    @ResponseBody
    public R finishSelfPurGoodsOrdersCost(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {
        System.out.println("dpgitititmemmmedpgitititmemmmedpgitititmemmme");
        List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = purGoods.getGbDepartmentOrdersEntities();
        List<GbDepartmentOrdersEntity> unChoiceOrderList = new ArrayList<>();
        int finishOrder = 0;
        for (GbDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            Boolean hasChoice = orders.getHasChoice();
            if (hasChoice) {
                finishOrder = finishOrder + 1;
                orders.setGbDoStatus(getGbOrderStatusHasFinished());
                orders.setGbDoBuyStatus(getGbOrderBuyStatusHavePayFinish());
                orders.setGbDoPurchaseUserId(purGoods.getGbDpgPurUserId());
                gbDepartmentOrdersService.update(orders);

            } else {
                unChoiceOrderList.add(orders);
            }

        }
        purGoods.setGbDpgOrdersAmount(finishOrder);
        purGoods.setGbDpgBatchId(-1);
        purGoods.setGbDpgStatus(2);
        purGoods.setGbDpgPayType(0);
        purGoods.setGbDpgTime(formatWhatTime(0));
        purGoods.setGbDpgPurchaseDate(formatWhatDay(0));
        purGoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
        purGoods.setGbDpgPurchaseYear(formatWhatYear(0));
        purGoods.setGbDpgPurchaseWeek(getWeek(0));
        purGoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
        purGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
        Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);

        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purGoods);
        }
        //判断是否有保鲜时间参数
        Integer gbDoDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
            int warnHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWarnHour());
            int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
            purGoods.setGbDpgWarnFullTime(formatWhatFullTime(warnHour));
            purGoods.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
        }
        gbDpgService.update(purGoods);


//        //给没有选择订单新生成一个采购商品
        if (unChoiceOrderList.size() > 0) {
            GbDistributerPurchaseGoodsEntity disGoods = new GbDistributerPurchaseGoodsEntity();
            disGoods.setGbDpgDisGoodsFatherId(unChoiceOrderList.get(0).getGbDoDisGoodsFatherId());
            disGoods.setGbDpgDisGoodsId(unChoiceOrderList.get(0).getGbDoDisGoodsId());
            disGoods.setGbDpgDistributerId(unChoiceOrderList.get(0).getGbDoDistributerId());
            disGoods.setGbDpgApplyDate(formatWhatDay(0));
            disGoods.setGbDpgStatus(0);
            disGoods.setGbDpgQuantity("0");
            disGoods.setGbDpgStandard(unChoiceOrderList.get(0).getGbDoStandard());
            disGoods.setGbDpgOrdersAmount(unChoiceOrderList.size());
            disGoods.setGbDpgOrdersFinishAmount(0);
            disGoods.setGbDpgOrdersBillAmount(0);
            disGoods.setGbDpgPurchaseWeek(getWeek(0));
            disGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            disGoods.setGbDpgIsCheck(0);
            disGoods.setGbDpgPurchaseType(1);
            gbDpgService.save(disGoods);
            for (GbDepartmentOrdersEntity unChoiceOrder : unChoiceOrderList) {
                Integer gbDistributerPurchaseGoodsId = disGoods.getGbDistributerPurchaseGoodsId();
                unChoiceOrder.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                gbDepartmentOrdersService.update(unChoiceOrder);

                BigDecimal purQuantity = new BigDecimal(disGoods.getGbDpgQuantity());
                BigDecimal orderQuantity = new BigDecimal(unChoiceOrder.getGbDoQuantity());
                BigDecimal add = purQuantity.add(orderQuantity).setScale(2, BigDecimal.ROUND_HALF_UP);
                disGoods.setGbDpgQuantity(add.toString());
                gbDpgService.update(disGoods);
            }

        }
        return R.ok();
    }


    @RequestMapping(value = "/finishPurGoodsToStock", method = RequestMethod.POST)
    @ResponseBody
    public R finishPurGoodsToStock(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {
        List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = purGoods.getGbDepartmentOrdersEntities();
        List<GbDepartmentOrdersEntity> unChoiceOrderList = new ArrayList<>();
        int finishOrder = 0;
        for (GbDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            Boolean hasChoice = orders.getHasChoice();
            if (hasChoice) {
                finishOrder = finishOrder + 1;
                orders.setGbDoStatus(getGbOrderStatusReceived());
                orders.setGbDoBuyStatus(getGbOrderBuyStatusHavePayFinish());
                orders.setGbDoPurchaseUserId(purGoods.getGbDpgPurUserId());
                gbDepartmentOrdersService.update(orders);

                saveDepStockDataByPurchase(orders);

            } else {
                unChoiceOrderList.add(orders);
            }

        }
        purGoods.setGbDpgBatchId(-1);
        purGoods.setGbDpgStatus(3);
        purGoods.setGbDpgPayType(0);
        purGoods.setGbDpgTime(formatWhatTime(0));
        purGoods.setGbDpgPurchaseDate(formatWhatDay(0));
        purGoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
        purGoods.setGbDpgPurchaseYear(formatWhatYear(0));
        purGoods.setGbDpgPurchaseWeek(getWeek(0));
        purGoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
        purGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
        Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);

        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purGoods);
        }

        //判断是否有保鲜时间参数
        Integer gbDoDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
            int warnHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWarnHour());
            int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
            purGoods.setGbDpgWarnFullTime(formatWhatFullTime(warnHour));
            purGoods.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
        }
        gbDpgService.update(purGoods);

        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(purGoods.getGbDpgDistributerId());
        BigDecimal subtract = new BigDecimal(gbDistributerEntity.getGbDistributerBuyQuantity()).subtract(new BigDecimal(finishOrder));
        gbDistributerEntity.setGbDistributerBuyQuantity(subtract.toString());
        gbDistributerService.update(gbDistributerEntity);


//        //给没有选择订单新生成一个采购商品
        if (unChoiceOrderList.size() > 0) {
            GbDistributerPurchaseGoodsEntity disGoods = new GbDistributerPurchaseGoodsEntity();
            disGoods.setGbDpgDisGoodsFatherId(unChoiceOrderList.get(0).getGbDoDisGoodsFatherId());
            disGoods.setGbDpgDisGoodsId(unChoiceOrderList.get(0).getGbDoDisGoodsId());
            disGoods.setGbDpgDistributerId(unChoiceOrderList.get(0).getGbDoDistributerId());
            disGoods.setGbDpgApplyDate(formatWhatDay(0));
            disGoods.setGbDpgStatus(0);
            disGoods.setGbDpgQuantity("0");
            disGoods.setGbDpgStandard(unChoiceOrderList.get(0).getGbDoStandard());
            disGoods.setGbDpgOrdersAmount(unChoiceOrderList.size());
            disGoods.setGbDpgOrdersFinishAmount(0);
            disGoods.setGbDpgOrdersBillAmount(0);
            disGoods.setGbDpgPurchaseWeek(getWeek(0));
            disGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            disGoods.setGbDpgIsCheck(0);
            disGoods.setGbDpgPurchaseType(1);
            gbDpgService.save(disGoods);
            for (GbDepartmentOrdersEntity unChoiceOrder : unChoiceOrderList) {
                Integer gbDistributerPurchaseGoodsId = disGoods.getGbDistributerPurchaseGoodsId();
                unChoiceOrder.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                gbDepartmentOrdersService.update(unChoiceOrder);

                BigDecimal purQuantity = new BigDecimal(disGoods.getGbDpgQuantity());
                BigDecimal orderQuantity = new BigDecimal(unChoiceOrder.getGbDoQuantity());
                BigDecimal add = purQuantity.add(orderQuantity).setScale(2, BigDecimal.ROUND_HALF_UP);
                disGoods.setGbDpgQuantity(add.toString());
                gbDpgService.update(disGoods);
            }

        }
        return R.ok();
    }


    public R saveDepStockDataByPurchase(@RequestBody GbDepartmentOrdersEntity order) {
        System.out.println("upddodididufidfuaisf");
        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();

        Integer gbDoDepDisGoodsId = order.getGbDoDepDisGoodsId();
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDoDepDisGoodsId);

        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
        Integer gbDoStatus = ordersEntity.getGbDoStatus();
        //判断没有被别人收货
        System.out.println("eeeeeeststttttttt" + gbDoStatus);
//        if (gbDoStatus.equals(getGbOrderStatusHasBill())) {
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
        stockEntity.setGbDgsNxDistributerId(-1);

        Integer gbDoDisGoodsId = order.getGbDoDisGoodsId();
        GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        stockEntity.setGbDgsGbDisGoodsFatherId(goodsEntity.getGbDgDfgGoodsFatherId());
        stockEntity.setGbDgsGbDisGoodsGrandId(goodsEntity.getGbDgDfgGoodsGrandId());
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
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryObject(order.getGbDoPurchaseGoodsId());
            if (purchaseGoodsEntity.getGbDpgWarnFullTime() != null && !purchaseGoodsEntity.getGbDpgWarnFullTime().trim().isEmpty()
                    && purchaseGoodsEntity.getGbDpgWasteFullTime() != null && !purchaseGoodsEntity.getGbDpgWasteFullTime().trim().isEmpty()) {
                stockEntity.setGbDgsWarnFullTime(purchaseGoodsEntity.getGbDpgWarnFullTime());
                stockEntity.setGbDgsWasteFullTime(purchaseGoodsEntity.getGbDpgWasteFullTime());
                String gbDpgWarnFullTime = purchaseGoodsEntity.getGbDpgWarnFullTime();
                String gbDpgWasteFullTime = purchaseGoodsEntity.getGbDpgWasteFullTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                // 设置日期字符串
                // 解析日期字符串为Date对象
                Date dateWaste = null;
                Date dateWarn = null;
                try {
                    dateWaste = dateFormat.parse(gbDpgWasteFullTime);
                    dateWarn = dateFormat.parse(gbDpgWarnFullTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // 获取时间戳
                long timestampWaste = dateWaste.getTime();
                long timestampWarn = dateWarn.getTime();
                // 输出时间戳
                stockEntity.setGbDgsWasteTimeQuantumName(String.valueOf(timestampWaste));
                stockEntity.setGbDgsWarnTimeQuantumName(String.valueOf(timestampWarn));
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
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryObject(order.getGbDoPurchaseGoodsId());
            BigDecimal gbPgOrderAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersAmount());
            BigDecimal gbDbFinishAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersFinishAmount());
            if (gbDbFinishAmount.add(new BigDecimal(1)).compareTo(gbPgOrderAmount) == 0) {
                purchaseGoodsEntity.setGbDpgOrdersFinishAmount(purchaseGoodsEntity.getGbDpgOrdersAmount());
                purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusReceive());
            } else {
                BigDecimal add = gbDbFinishAmount.add(new BigDecimal(1));
                purchaseGoodsEntity.setGbDpgOrdersFinishAmount(add.intValue());
            }
            gbDpgService.update(purchaseGoodsEntity);
        }

        return R.ok();
    }


    @RequestMapping(value = "/puraserReturnPurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R puraserReturnPurGoods(@RequestBody GbDistributerPurchaseGoodsEntity disPurGoodsEntity) {
        System.out.println("purgoodssssss" + disPurGoodsEntity);

        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = disPurGoodsEntity.getGbDepartmentOrdersEntities();
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
            disPurGoodsEntity.setGbDpgStatus(3);


            gbDpgService.update(disPurGoodsEntity);

            for (GbDepartmentOrdersEntity ordersEntity : gbDepartmentOrdersEntities) {
                Integer gbDoDgsrReturnId = ordersEntity.getGbDoDgsrReturnId();
                GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepartmentStockReduceService.queryObject(gbDoDgsrReturnId);
                reduceEntity.setGbDgsrStatus(0);
                gbDepartmentStockReduceService.update(reduceEntity);
                System.out.println("reee" + reduceEntity);

                ordersEntity.setGbDoStatus(3);
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
            BigDecimal totalWeight = new BigDecimal(stock.getGbDgsWeight()).add(weight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalSubtotal = new BigDecimal(stock.getGbDgsSubtotal()).add(total).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalRestWeight = new BigDecimal(stock.getGbDgsWeight()).add(restWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            depGoodsDailyItem.setGbDgdWeight(totalWeight.toString());
            depGoodsDailyItem.setGbDgdSubtotal(totalSubtotal.toString());
            depGoodsDailyItem.setGbDgdRestWeight(totalRestWeight.toString());
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

    @RequestMapping(value = "/updateSelfPurGoodsOrdersCost", method = RequestMethod.POST)
    @ResponseBody
    public R updateSelfPurGoodsOrdersCost(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {
        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purGoods.getGbDepartmentOrdersEntities();
        for (GbDepartmentOrdersEntity orders : gbDepartmentOrdersEntities) {
            if (orders.getGbDoStatus() < 4) {
                Integer gbDoBillId = orders.getGbDoBillId();
                if (gbDoBillId != null) {
                    GbDepartmentBillEntity billEntity = gbDepartmentBillService.queryObject(gbDoBillId);
                    BigDecimal billtotal = new BigDecimal(billEntity.getGbDbTotal());
                    Integer gbDepartmentOrdersId = orders.getGbDepartmentOrdersId();
                    GbDepartmentOrdersEntity orignalOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
                    BigDecimal oldSubtotal = new BigDecimal(orignalOrdersEntity.getGbDoSubtotal());
                    BigDecimal nowSubtotal = new BigDecimal(orders.getGbDoSubtotal());
                    BigDecimal changeTotal = oldSubtotal.subtract(nowSubtotal);
                    if (changeTotal.compareTo(BigDecimal.ZERO) == -1) { //小于0
                        BigDecimal add = billtotal.add(changeTotal.abs());
                        billEntity.setGbDbTotal(add.toString());
                        gbDepartmentBillService.update(billEntity);
                    }
                    if (changeTotal.compareTo(BigDecimal.ZERO) == 1) { //大于0
                        BigDecimal add = billtotal.subtract(changeTotal);
                        billEntity.setGbDbTotal(add.toString());
                        gbDepartmentBillService.update(billEntity);
                    }
                }
                gbDepartmentOrdersService.update(orders);
            }
        }

        if (gbDistributerGoodsService.queryObject(purGoods.getGbDpgDisGoodsId()).getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purGoods);
        }
        System.out.println(purGoods);
        gbDpgService.update(purGoods);
        return R.ok();
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
                int warnHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWarnHour());
                int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
                purchaseGoodsEntity.setGbDpgWarnFullTime(formatWhatFullTime(warnHour));
                purchaseGoodsEntity.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
            }
        }

        gbDpgService.update(purchaseGoodsEntity);
        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purchaseGoodsEntity.getGbDepartmentOrdersEntities();
        for (GbDepartmentOrdersEntity orders : gbDepartmentOrdersEntities) {
            orders.setGbDoStatus(getGbOrderStatusProcurement());
            orders.setGbDoBuyStatus(getGbOrderBuyStatusPrepareing());
            gbDepartmentOrdersService.update(orders);
        }

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


    @RequestMapping(value = "/finishSelfPurGoodsOrdersCostBundle", method = RequestMethod.POST)
    @ResponseBody
    public R finishSelfPurGoodsOrdersCostBundle(@RequestBody List<GbDistributerPurchaseGoodsEntity> purGoodsList) {
        System.out.println("dpgitititmemmmedpgitititmemmmedpgitititmemmme");
        for (GbDistributerPurchaseGoodsEntity purGoods : purGoodsList) {

            List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = purGoods.getGbDepartmentOrdersEntities();
            List<GbDepartmentOrdersEntity> unChoiceOrderList = new ArrayList<>();
            int finishOrder = 0;
            for (GbDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
                Boolean hasChoice = orders.getHasChoice();
                if (hasChoice) {
                    finishOrder = finishOrder + 1;
                    orders.setGbDoStatus(getGbOrderStatusReceived());
                    orders.setGbDoBuyStatus(getGbOrderBuyStatusHavePayFinish());
                    orders.setGbDoPurchaseUserId(purGoods.getGbDpgPurUserId());
                    gbDepartmentOrdersService.update(orders);

                } else {
                    unChoiceOrderList.add(orders);
                }

            }
            purGoods.setGbDpgOrdersAmount(finishOrder);
            purGoods.setGbDpgBatchId(-1);
            purGoods.setGbDpgStatus(2);
            purGoods.setGbDpgPayType(0);
            purGoods.setGbDpgTime(formatWhatTime(0));
            purGoods.setGbDpgPurchaseDate(formatWhatDay(0));
            purGoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
            purGoods.setGbDpgPurchaseYear(formatWhatYear(0));
            purGoods.setGbDpgPurchaseWeek(getWeek(0));
            purGoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
            purGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            purGoods.setGbDpgBuySubtotal("0");
            Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
//            GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);

//            if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
//                checkPurGoodsPrice(purGoods);
//            }
            //判断是否有保鲜时间参数
//            Integer gbDoDisGoodsId = purGoods.getGbDpgDisGoodsId();
//            GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
//            if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
//                int warnHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWarnHour());
//                int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
//                purGoods.setGbDpgWarnFullTime(formatWhatFullTime(warnHour));
//                purGoods.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
//            }
            gbDpgService.update(purGoods);


//        //给没有选择订单新生成一个采购商品
            if (unChoiceOrderList.size() > 0) {
                GbDistributerPurchaseGoodsEntity disGoods = new GbDistributerPurchaseGoodsEntity();
                disGoods.setGbDpgDisGoodsFatherId(unChoiceOrderList.get(0).getGbDoDisGoodsFatherId());
                disGoods.setGbDpgDisGoodsId(unChoiceOrderList.get(0).getGbDoDisGoodsId());
                disGoods.setGbDpgDistributerId(unChoiceOrderList.get(0).getGbDoDistributerId());
                disGoods.setGbDpgApplyDate(formatWhatDay(0));
                disGoods.setGbDpgStatus(0);
                disGoods.setGbDpgQuantity("0");
                disGoods.setGbDpgStandard(unChoiceOrderList.get(0).getGbDoStandard());
                disGoods.setGbDpgOrdersAmount(unChoiceOrderList.size());
                disGoods.setGbDpgOrdersFinishAmount(0);
                disGoods.setGbDpgOrdersBillAmount(0);
                disGoods.setGbDpgPurchaseWeek(getWeek(0));
                disGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                disGoods.setGbDpgIsCheck(0);
                disGoods.setGbDpgPurchaseType(1);
                gbDpgService.save(disGoods);
                for (GbDepartmentOrdersEntity unChoiceOrder : unChoiceOrderList) {
                    Integer gbDistributerPurchaseGoodsId = disGoods.getGbDistributerPurchaseGoodsId();
                    unChoiceOrder.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                    gbDepartmentOrdersService.update(unChoiceOrder);

                    BigDecimal purQuantity = new BigDecimal(disGoods.getGbDpgQuantity());
                    BigDecimal orderQuantity = new BigDecimal(unChoiceOrder.getGbDoQuantity());
                    BigDecimal add = purQuantity.add(orderQuantity).setScale(2, BigDecimal.ROUND_HALF_UP);
                    disGoods.setGbDpgQuantity(add.toString());
                    gbDpgService.update(disGoods);
                }

            }

        }

        return R.ok();
    }


    @RequestMapping(value = "/checkPurchaseGoodsStatus/{id}")
    @ResponseBody
    public R checkPurchaseGoodsStatus(@PathVariable Integer id) {

        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryObject(id);

        return R.ok().put("data", purchaseGoodsEntity.getGbDpgStatus());
    }

}
