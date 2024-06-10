package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-21 21:51
 */

import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.CommonUtils.generateBillTradeNo;
import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatFullTime;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.getHeadStringByString;


@RestController
@RequestMapping("api/gbdepartmentorders")
public class GbDepartmentOrdersController {
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;
    @Autowired
    private GbDepartmentOrdersHistoryService gbDepOrdersHistoryService;
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private GbDistributerPurchaseBatchService gbDPBService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private GbDistributerSupplierService gbDistributerSupplierService;
    @Autowired
    private GbDepartmentUserService gbDepartmentUserService;
    @Autowired
    private GbDistributerGoodsPriceService goodsPriceService;
    @Autowired
    private GbDistributerWeightTotalService gbDisWeightTotalService;
    @Autowired
    private GbDistributerWeightGoodsService gbDisWeightGoodsService;
    @Autowired
    private GbDepartmentGoodsDailyService gbDepGoodsDailyService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepGoodsStockReduceService;
    @Autowired
    private NxJrdhSupplierService jrdhSupplierService;
    @Autowired
    private NxDepartmentBillService nxDepartmentBillService;
    @Autowired
    private NxDistributerGbDistributerService nxDisGbDisService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    private NxDistributerPurchaseGoodsService nxDistributerPurchaseGoodsService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDPBService;
    @Autowired
    private NxJrdhUserService nxJrdhUserService;


    @RequestMapping(value = "/peisongDepGetGbOrders", method = RequestMethod.POST)
    @ResponseBody
    public R peisongDepGetGbOrders(Integer gbDisId, Integer nxDisGoodsId, String startDate, String stopDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", gbDisId);
        map.put("nxDisGoodsId", nxDisGoodsId);
        if (!startDate.equals("-1")) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        }
        System.out.println("mappa" + map);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryPeisongOrdersByParams(map);

        return R.ok().put("data", ordersEntities);
    }


    /**
     * kufang 删除备货商品
     *
     * @param disGoods
     * @return
     */
    @RequestMapping(value = "/delPrepareGoods", method = RequestMethod.POST)
    @ResponseBody
    public R delPrepareGoods(@RequestBody GbDistributerGoodsEntity disGoods) {
        List<GbDepartmentOrdersEntity> prepareOrderEntities = disGoods.getPrepareOrderEntities();
        if (prepareOrderEntities.size() > 0) {
            for (GbDepartmentOrdersEntity order : prepareOrderEntities) {
                order.setGbDoPurchaseGoodsId(-1);
                order.setGbDoBuyStatus(getGbOrderBuyStatusNew());
                gbDepartmentOrdersService.update(order);
            }
        }
        GbDepartmentDisGoodsEntity gbDepartmentDisGoodsEntity = disGoods.getGbDepartmentDisGoodsEntity();
        gbDepartmentDisGoodsEntity.setGbDdgPrepareTotalWeight(null);
        gbDepartmentDisGoodsEntity.setGbDdgPrepareStatus(0);
        gbDepartmentDisGoodsService.update(gbDepartmentDisGoodsEntity);
        return R.ok();
    }

    @RequestMapping(value = "/depGetWeightDepDisGoods/{depId}")
    @ResponseBody
    public R depGetWeightDepDisGoods(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderDepId", depId);
        map.put("equalBuyStatus", 1);
        map.put("status", 3);
        List<GbDistributerGoodsShelfEntity> disGoodsEntities = gbDepartmentOrdersService.queryWeightGoodsByParams(map);

        return R.ok().put("data", disGoodsEntities);
    }


    @RequestMapping(value = "/getDeliveryOrders")
    @ResponseBody
    public R getDeliveryOrders(Integer toDepId, Integer orderType, Integer userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", toDepId);
        map.put("equalStatus", 2);
        map.put("orderType", orderType);

        System.out.println("mapappaapap" + map);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryDistributerTodayDepartments(map);
        Map<String, Object> todayData = packageDisOrderByDep(departmentEntities, 0);
        List<GbDepartmentEntity> arr = (List<GbDepartmentEntity>) todayData.get("arr");

        Map<String, Object> map11 = new HashMap<>();
        map11.put("purDepId", toDepId);
        map11.put("status", 1);
        int count0 = gbDistributerPurchaseGoodsService.queryPurchaseGoodsAmount(map11);

        Map<String, Object> map12 = new HashMap<>();
        map12.put("purUserId", userId);
        map12.put("status", 2);
        Integer count1 = gbDPBService.queryDisPurchaseBatchCount(map12);

        Map<String, Object> map13 = new HashMap<>();
        map13.put("purUserId", userId);
        map13.put("equalStatus", 1);
        map13.put("batchId", -1);
        Integer count2 = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map13);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("arr", todayData);
        map3.put("purGoodsAmount", count0);
        map3.put("haoyouAmount", count1);
        map3.put("finishAmount", count2);
        map3.put("deliveryAmount", arr.size());

        return R.ok().put("data", map3);
    }


    @RequestMapping(value = "/stockReceivePurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R stockReceivePurGoods(@RequestBody GbDepartmentOrdersEntity order) {
        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
        Integer gbDoDepDisGoodsId = order.getGbDoDepDisGoodsId();
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDoDepDisGoodsId);

        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
        Integer gbDoStatus = ordersEntity.getGbDoStatus();
        //判断没有被别人收货
        if (gbDoStatus.equals(getGbOrderStatusHasBill())) {
            //0,修改订单上次价格涨幅
            if (departmentDisGoodsEntity.getGbDdgOrderDate() != null) {
                if (order.getGbDoPrice() != null) {
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
            stockEntity.setGbDgsGbDisGoodsFatherId(order.getGbDoDisGoodsFatherId());
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
            String gbDdgSellingPrice = departmentDisGoodsEntity.getGbDdgSellingPrice();
            if (gbDdgSellingPrice != null && new BigDecimal(gbDdgSellingPrice).compareTo(new BigDecimal(0)) == 1) {
                stockEntity.setGbDgsAfterProfitSubtotal("0");
                stockEntity.setGbDgsBetweenPrice("0");
                stockEntity.setGbDgsCostRate("0");
                stockEntity.setGbDgsSellingSubtotal("0");
                stockEntity.setGbDgsProduceSellingSubtotal("0");
                stockEntity.setGbDgsProfitSubtotal("0");
                stockEntity.setGbDgsProfitWeight("0");
                stockEntity.setGbDgsSellingPrice(gbDdgSellingPrice);
            } else {
                stockEntity.setGbDgsSellingPrice("-1");
            }

            // showStandard
            if (departmentDisGoodsEntity.getGbDdgShowStandardId() != -1) {
                String gbDdgShowStandardScale = departmentDisGoodsEntity.getGbDdgShowStandardScale();
                BigDecimal divide = new BigDecimal(order.getGbDoWeight()).divide(new BigDecimal(gbDdgShowStandardScale), 1, BigDecimal.ROUND_HALF_UP);
                stockEntity.setGbDgsRestWeightShowStandard(divide.toString());
                stockEntity.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
            }

            //判断是否有保鲜时间参数
            if (order.getGbDoPurchaseGoodsId() != -1) {
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(order.getGbDoPurchaseGoodsId());
                if (purchaseGoodsEntity.getGbDpgWarnFullTime() != null && purchaseGoodsEntity.getGbDpgWasteFullTime() != null) {
                    stockEntity.setGbDgsWarnFullTime(purchaseGoodsEntity.getGbDpgWarnFullTime());
                    stockEntity.setGbDgsWasteFullTime(purchaseGoodsEntity.getGbDpgWasteFullTime());
                }
                //判断是否价格异常商品
                if (purchaseGoodsEntity.getGbDpgDisGoodsPriceId() != null) {
                    GbDistributerGoodsPriceEntity goodsPriceEntity = goodsPriceService.queryObject(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
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
            stockEntity.setGbDgsNxDistributerId(order.getGbDoNxDistributerId());
            stockEntity.setGbDgsReceiveUserId(order.getGbDoReceiveUserId());
            stockEntity.setGbDgsInventoryDate(formatWhatDay(0));
            stockEntity.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
            stockEntity.setGbDgsInventoryMonth(formatWhatMonth(0));
            stockEntity.setGbDgsInventoryYear(formatWhatYear(0));
            gbDepartmentGoodsStockService.save(stockEntity);


            orderAddDepDisGoods(order, stockEntity, gbDoDepDisGoodsId);
            updateDepGoodsDailyBusiness(stockEntity);

            //2，修改订单状态
            order.setGbDoStatus(getGbOrderStatusReceived());
            gbDepartmentOrdersService.update(order);

            //3，修改送货单收货单子数量
            if (order.getGbDoPurchaseGoodsId() != -1) {
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(order.getGbDoPurchaseGoodsId());
                BigDecimal gbPgOrderAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersAmount());
                BigDecimal gbDbFinishAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersFinishAmount());
                if (gbDbFinishAmount.add(new BigDecimal(1)).compareTo(gbPgOrderAmount) == 0) {
                    purchaseGoodsEntity.setGbDpgOrdersFinishAmount(purchaseGoodsEntity.getGbDpgOrdersAmount());
                    purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusReceive());
                } else {
                    BigDecimal add = gbDbFinishAmount.add(new BigDecimal(1));
                    purchaseGoodsEntity.setGbDpgOrdersFinishAmount(add.intValue());
                }
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }

            //3，修改送货单收货单子数量
            Integer gbDoBillId = order.getGbDoBillId();
            GbDepartmentBillEntity billEntity = gbDepartmentBillService.queryObject(gbDoBillId);
            BigDecimal gbDbOrderAmount = new BigDecimal(billEntity.getGbDbOrderAmount());
            if (gbDbOrderAmount.compareTo(new BigDecimal("1")) == 1) {
                BigDecimal subtract = gbDbOrderAmount.subtract(new BigDecimal("1"));
                billEntity.setGbDbOrderAmount(subtract.intValue());
                gbDepartmentBillService.update(billEntity);
            } else {
                billEntity.setGbDbOrderAmount(0);
                billEntity.setGbDbStatus(0);
                gbDepartmentBillService.update(billEntity);
            }
            return R.ok();
        } else {
            return R.error(-1, "已经完成收货");
        }

    }

    /**
     * 订货部门收货
     *
     * @param order
     * @return
     */
    @RequestMapping(value = "/departmentReceiveOutStock", method = RequestMethod.POST)
    @ResponseBody
    public R departmentReceiveOutStock(@RequestBody GbDepartmentOrdersEntity order) {
        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
        Integer gbDoDepDisGoodsId = order.getGbDoDepDisGoodsId();
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDoDepDisGoodsId);

        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
        Integer gbDoStatus = ordersEntity.getGbDoStatus();
        //判断没有被别人收货
        if (gbDoStatus.equals(getGbOrderStatusHasBill())) {
            //1,修改库存数据
            if (order.getGbDoOrderType().equals(getGbOrderTypeChuKu()) || order.getGbDoOrderType().equals(getGbOrderTypeKitchen())) {
                List<GbDepartmentGoodsStockEntity> goodsStockEntityList = order.getGoodsStockEntityList();
                for (GbDepartmentGoodsStockEntity stock : goodsStockEntityList) {

                    //判断是否价格异常商品
                    if (stock.getGbDgsGbPriceGoodsId() != null) {
                        GbDistributerGoodsPriceEntity goodsPriceEntity = goodsPriceService.queryObject(stock.getGbDgsGbPriceGoodsId());
                        String subtotal = stock.getGbDgsSubtotal();
                        BigDecimal whatSubtotal = new BigDecimal(subtotal).multiply(new BigDecimal(goodsPriceEntity.getGbDgpPurScale()));
                        stock.setGbDgsGbPriceSubtotal(whatSubtotal.toString());
                        stock.setGbDgsGbPriceGoodsId(goodsPriceEntity.getGbDistributerGoodsPriceId());
                        stock.setGbDgsGbPriceSubtotalScale(goodsPriceEntity.getGbDgpPurScale());
                    }
                    stock.setGbDgsFullTime(formatFullTime());
                    stock.setGbDgsDate(formatWhatDay(0));
                    stock.setGbDgsTimeStamp(getTimeStamp());
                    stock.setGbDgsWeek(getWeek(0));
                    stock.setGbDgsMonth(formatWhatMonth(0));
                    stock.setGbDgsYear(formatWhatYear(0));
                    stock.setGbDgsInventoryFullTime(formatFullTime());
                    stock.setGbDgsInventoryDate(formatWhatDay(0));
                    stock.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
                    stock.setGbDgsInventoryMonth(formatWhatMonth(0));
                    stock.setGbDgsInventoryYear(formatWhatYear(0));
                    stock.setGbDgsStatus(0);

                    // showStandard
                    if (departmentDisGoodsEntity.getGbDdgShowStandardId() != -1) {
                        String gbDdgShowStandardScale = departmentDisGoodsEntity.getGbDdgShowStandardScale();
                        BigDecimal divide = new BigDecimal(order.getGbDoWeight()).divide(new BigDecimal(gbDdgShowStandardScale), 1, BigDecimal.ROUND_HALF_UP);
                        stock.setGbDgsRestWeightShowStandard(divide.toString());
                        stock.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
                    }


                    gbDepartmentGoodsStockService.update(stock);

                    //depDisGoods
                    orderAddDepDisGoods(order, stock, gbDoDepDisGoodsId);

                    //add outStockProdeuce
                    Integer gbDoDisGoodsId = order.getGbDoDisGoodsId();
                    GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
                    if (gbDistributerGoodsEntity.getGbDgIsSelfControl() == 0) {
                        System.out.println("adddkkdkkdkdkdkdkkdkkdkdkdk");
                        addNewStockReduce(stock);
                        subtractOutGoodsDailyBusiness(stock);
                    }
                    // add departmentGoodsDaily
                    updateDepGoodsDailyBusiness(stock);
                }
            } else {
                //0,修改订单上次价格涨幅
                String gbDdgOrderDate = departmentDisGoodsEntity.getGbDdgOrderDate();

                if (gbDdgOrderDate != null && order.getGbDoPrice() != null) {
                    BigDecimal decimal = new BigDecimal(departmentDisGoodsEntity.getGbDdgOrderPrice());
                    BigDecimal decimal1 = new BigDecimal(order.getGbDoPrice());
                    BigDecimal subtract1 = decimal1.subtract(decimal);
                    order.setGbDoPriceDifferent(subtract1.toString());
                } else {
                    order.setGbDoPriceDifferent("0");
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
                stockEntity.setGbDgsGbDisGoodsFatherId(order.getGbDoDisGoodsFatherId());
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
                String gbDdgSellingPrice = departmentDisGoodsEntity.getGbDdgSellingPrice();
                if (gbDdgSellingPrice != null && new BigDecimal(gbDdgSellingPrice).compareTo(new BigDecimal(0)) == 1) {
                    stockEntity.setGbDgsAfterProfitSubtotal("0");
                    stockEntity.setGbDgsBetweenPrice("0");
                    stockEntity.setGbDgsCostRate("0");
                    stockEntity.setGbDgsSellingSubtotal("0");
                    stockEntity.setGbDgsProduceSellingSubtotal("0");
                    stockEntity.setGbDgsProfitSubtotal("0");
                    stockEntity.setGbDgsProfitWeight("0");
                    stockEntity.setGbDgsSellingPrice(gbDdgSellingPrice);
                } else {
                    stockEntity.setGbDgsSellingPrice("-1");
                }

                // showStandard
                if (departmentDisGoodsEntity.getGbDdgShowStandardId() != -1) {
                    String gbDdgShowStandardScale = departmentDisGoodsEntity.getGbDdgShowStandardScale();
                    BigDecimal divide = new BigDecimal(order.getGbDoWeight()).divide(new BigDecimal(gbDdgShowStandardScale), 1, BigDecimal.ROUND_HALF_UP);
                    stockEntity.setGbDgsRestWeightShowStandard(divide.toString());
                    stockEntity.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
                }

                //判断是否有保鲜时间参数
                if (order.getGbDoPurchaseGoodsId() != -1) {
                    GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(order.getGbDoPurchaseGoodsId());
                    if (purchaseGoodsEntity.getGbDpgWarnFullTime() != null && purchaseGoodsEntity.getGbDpgWasteFullTime() != null) {
                        stockEntity.setGbDgsWarnFullTime(purchaseGoodsEntity.getGbDpgWarnFullTime());
                        stockEntity.setGbDgsWasteFullTime(purchaseGoodsEntity.getGbDpgWasteFullTime());
                    }
                    //判断是否价格异常商品
                    if (purchaseGoodsEntity.getGbDpgDisGoodsPriceId() != null) {
                        GbDistributerGoodsPriceEntity goodsPriceEntity = goodsPriceService.queryObject(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
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
                stockEntity.setGbDgsNxDistributerId(order.getGbDoNxDistributerId());
                stockEntity.setGbDgsReceiveUserId(order.getGbDoReceiveUserId());
                stockEntity.setGbDgsInventoryDate(formatWhatDay(0));
                stockEntity.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
                stockEntity.setGbDgsInventoryMonth(formatWhatMonth(0));
                stockEntity.setGbDgsInventoryYear(formatWhatYear(0));
                gbDepartmentGoodsStockService.save(stockEntity);

                //库房收货不添加DepDaily
                if (order.getGbDoOrderType().equals(getGbOrderTypeJiCai())
                        || order.getGbDoOrderType().equals(getGbOrderTypeZiCai())
                        || order.getGbDoOrderType().equals(getGbOrderTypeAppSupplier())) {
                    updateDepGoodsDailyBusiness(stockEntity);
                }

                orderAddDepDisGoods(order, stockEntity, gbDoDepDisGoodsId);

            }

            //2，修改订单状态
            order.setGbDoStatus(getGbOrderStatusReceived());
            gbDepartmentOrdersService.update(order);

            //3，修改送货单收货单子数量
            if (order.getGbDoPurchaseGoodsId() != -1) {
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(order.getGbDoPurchaseGoodsId());
                BigDecimal gbPgOrderAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersAmount());
                BigDecimal gbDbFinishAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersFinishAmount());
                if (gbDbFinishAmount.add(new BigDecimal(1)).compareTo(gbPgOrderAmount) == 0) {
                    purchaseGoodsEntity.setGbDpgOrdersFinishAmount(purchaseGoodsEntity.getGbDpgOrdersAmount());
                    purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusReceive());
                } else {
                    BigDecimal add = gbDbFinishAmount.add(new BigDecimal(1));
                    purchaseGoodsEntity.setGbDpgOrdersFinishAmount(add.intValue());
                }
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }

            //3，修改送货单收货单子数量
            Integer gbDoBillId = order.getGbDoBillId();
            GbDepartmentBillEntity billEntity = gbDepartmentBillService.queryObject(gbDoBillId);
            BigDecimal gbDbOrderAmount = new BigDecimal(billEntity.getGbDbOrderAmount());
            if (gbDbOrderAmount.compareTo(new BigDecimal("1")) == 1) {
                BigDecimal subtract = gbDbOrderAmount.subtract(new BigDecimal("1"));
                billEntity.setGbDbOrderAmount(subtract.intValue());
                gbDepartmentBillService.update(billEntity);
            } else {
                billEntity.setGbDbOrderAmount(0);
                billEntity.setGbDbStatus(0);
                gbDepartmentBillService.update(billEntity);
            }
            return R.ok();
        } else {
            return R.error(-1, "已经完成收货");
        }

    }


    private void subtractOutGoodsDailyBusiness(GbDepartmentGoodsStockEntity stockEntity) {
        Integer gbDgsGbGoodsStockId = stockEntity.getGbDgsGbGoodsStockId();
        GbDepartmentGoodsStockEntity stock = gbDepartmentGoodsStockService.queryObject(gbDgsGbGoodsStockId);

        Map<String, Object> map = new HashMap<>();
        map.put("depGoodsId", stock.getGbDgsGbDepDisGoodsId());
        map.put("date", formatWhatDay(0));
        System.out.println("searchchdddialldydydyydyydydy" + map);
        GbDepartmentGoodsDailyEntity depGoodsDailyItem = gbDepGoodsDailyService.queryDepGoodsDailyItem(map);
        if (depGoodsDailyItem != null) {
            BigDecimal restWeight = new BigDecimal(depGoodsDailyItem.getGbDgdRestWeight());
            BigDecimal produceWeight = new BigDecimal(depGoodsDailyItem.getGbDgdProduceWeight());
            BigDecimal produceSubtotal = new BigDecimal(depGoodsDailyItem.getGbDgdProduceSubtotal());

            BigDecimal outWeight = new BigDecimal(stockEntity.getGbDgsWeight());
            BigDecimal outSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());
            BigDecimal produceAllWeight = produceWeight.add(outWeight);
            BigDecimal produceAllSubtotal = produceSubtotal.add(outSubtotal);
            BigDecimal totalRestWeight = restWeight.subtract(outWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            depGoodsDailyItem.setGbDgdRestWeight(totalRestWeight.toString());
            depGoodsDailyItem.setGbDgdProduceWeight(produceAllWeight.toString());
            depGoodsDailyItem.setGbDgdProduceSubtotal(produceAllSubtotal.toString());
            gbDepGoodsDailyService.update(depGoodsDailyItem);
        }

    }

    private void addNewStockReduce(GbDepartmentGoodsStockEntity stockEntity) {
        Integer gbDgsGbGoodsStockId = stockEntity.getGbDgsGbGoodsStockId();
        GbDepartmentGoodsStockEntity stock = gbDepartmentGoodsStockService.queryObject(gbDgsGbGoodsStockId);
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
        reduceEntity.setGbDgsrCostWeight(stockEntity.getGbDgsWeight());
        reduceEntity.setGbDgsrCostSubtotal(stockEntity.getGbDgsSubtotal());
        reduceEntity.setGbDgsrProduceWeight(stockEntity.getGbDgsWeight());
        reduceEntity.setGbDgsrProduceSubtotal(stockEntity.getGbDgsSubtotal());
        reduceEntity.setGbDgsrGbPurGoodsId(stock.getGbDgsGbPurGoodsId());
        reduceEntity.setGbDgsrReturnWeight("0");
        reduceEntity.setGbDgsrReturnSubtotal("0");
        reduceEntity.setGbDgsrWasteWeight("0");
        reduceEntity.setGbDgsrWasteSubtotal("0");
        reduceEntity.setGbDgsrLossWeight("0");
        reduceEntity.setGbDgsrLossSubtotal("0");
        reduceEntity.setGbDgsrGbGoodsInventoryType(1);
        gbDepGoodsStockReduceService.save(reduceEntity);

        BigDecimal myChangeWeight = new BigDecimal(stockEntity.getGbDgsWeight());
        BigDecimal myChangeSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());

        //update
        BigDecimal allWeight = new BigDecimal(stock.getGbDgsProduceWeight()).add(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
        BigDecimal allSubtotal = new BigDecimal(stock.getGbDgsProduceSubtotal()).add(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
        stock.setGbDgsProduceWeight(allWeight.toString());
        stock.setGbDgsProduceSubtotal(allSubtotal.toString());
        gbDepartmentGoodsStockService.update(stock);

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


    private void updateDepDisGoods(GbDepartmentGoodsStockEntity stockEntity, Integer depDisGoodsId, String what) {
        System.out.println("updateDepDisGoodsupdateDepDisGoods" + what);
        BigDecimal stockSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());
        BigDecimal stockWeight = new BigDecimal(stockEntity.getGbDgsWeight());
        System.out.println("sotoscksubd;dldl" + stockWeight);
        System.out.println("sotoscksubd;dldl" + stockSubtotal);
        BigDecimal subTotal = new BigDecimal(0);
        BigDecimal weight = new BigDecimal(0);
        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        System.out.println("depgoeoidididiid" + depDisGoodsEntity.getGbDepartmentDisGoodsId());
        if (what.equals("add")) {
            subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).add(stockSubtotal);
            weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).add(stockWeight);
            System.out.println("adddddd" + subTotal + "weight" + weight);
        }
        if (what.equals("subtract")) {
            subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).subtract(stockSubtotal);
            weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).subtract(stockWeight);

        }
        System.out.println("zahuishsihsis" + subTotal);
        if (new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale()).compareTo(new BigDecimal(0)) == 1) {
            BigDecimal showScale = new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale());
            BigDecimal standardWeight = weight.divide(showScale, 1, BigDecimal.ROUND_HALF_UP);
            depDisGoodsEntity.setGbDdgShowStandardWeight(standardWeight.toString());
        }
        System.out.println("suttootototo-------" + subTotal + "weithht=====" + weight);
        depDisGoodsEntity.setGbDdgStockTotalSubtotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgStockTotalWeight(weight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgInventoryDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgInventoryFullTime(formatWhatFullTime(0));

        gbDepartmentDisGoodsService.update(depDisGoodsEntity);

    }


    private void updateDepDisGoodsNoPrice(GbDepartmentGoodsStockEntity stockEntity, GbDepartmentGoodsStockEntity outStockEntity, String what) {
        BigDecimal stockWeight = new BigDecimal(stockEntity.getGbDgsWeight());
        BigDecimal fromStockPriceB = new BigDecimal(outStockEntity.getGbDgsPrice());
        BigDecimal stockSubtotal = fromStockPriceB.multiply(stockWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
        BigDecimal subTotal = new BigDecimal(0);
        BigDecimal weight = new BigDecimal(0);
        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(outStockEntity.getGbDgsGbDepDisGoodsId());
        if (what.equals("add")) {
            subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).add(stockSubtotal);
            weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).add(stockWeight);

        }
        if (what.equals("subtract")) {
            subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).subtract(stockSubtotal);
            weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).subtract(stockWeight);

        }
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


//    private void subscribeDepDisGoodsData(GbDepartmentGoodsStockEntity stockEntity, Integer depDisGoodsId) {
//
//        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
//
//        BigDecimal subtotalB = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).subtract(new BigDecimal(stockEntity.getGbDgsSubtotal()));
//
//        BigDecimal changeWeight = new BigDecimal(stockEntity.getGbDgsWeight());
//        if (new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale()).compareTo(new BigDecimal(0)) == 1) {
//            BigDecimal showScale = new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale());
//            changeWeight = new BigDecimal(stockEntity.getGbDgsWeight()).divide(showScale, 1, BigDecimal.ROUND_HALF_UP);
//
//        }
//        BigDecimal weightB = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).subtract(changeWeight);
//        depDisGoodsEntity.setGbDdgStockTotalSubtotal(subtotalB.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//        depDisGoodsEntity.setGbDdgStockTotalWeight(weightB.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//        depDisGoodsEntity.setGbDdgInventoryDate(formatWhatDay(0));
//        depDisGoodsEntity.setGbDdgInventoryFullTime(formatWhatFullTime(0));
//        gbDepartmentDisGoodsService.update(depDisGoodsEntity);
//    }


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
            GbDistributerGoodsEntity distributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDgdGbDisGoodsId);
            if (distributerGoodsEntity.getGbDgControlFresh() == 1) {
                dailyEntity.setGbDgdFreshRate("100");
            } else {
                dailyEntity.setGbDgdFreshRate("0");
            }
            dailyEntity.setGbDgdFullTime(formatFullTime());
            gbDepGoodsDailyService.save(dailyEntity);
        }
    }

    private void addDepartmentOrderHistory(GbDepartmentOrdersEntity order) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depDisGoodsId", order.getGbDoDisGoodsId());
        map1.put("depId", order.getGbDoDepartmentId());
        List<GbDepartmentOrdersHistoryEntity> historyEntities = gbDepOrdersHistoryService.queryGbDepHistoryOrdersByParams(map1);
        String orderQuantity = "";
        String orderStandard = "";
        String orderStr = "";
        orderQuantity = order.getGbDoQuantity();
        orderStandard = order.getGbDoStandard();

        orderStr = orderQuantity + orderStandard;

        //如果有4个以内的历史记录
        if (historyEntities.size() > 0 && historyEntities.size() < 4) {

            int equalNumber = 0;
            for (GbDepartmentOrdersHistoryEntity orderHistory : historyEntities) {
                String historyStr = orderHistory.getGbDohQuantity() + orderHistory.getGbDohStandard();
                if (orderStr.equals(historyStr)) {
                    equalNumber = equalNumber + 1;
                }
            }
            if (equalNumber == 0 && historyEntities.size() < 3) {
                //添加新的
                GbDepartmentOrdersHistoryEntity historyEntity = new GbDepartmentOrdersHistoryEntity();
                historyEntity.setGbDohApplyDate(order.getGbDoApplyDate());
                historyEntity.setGbDohDepDisGoodsId(order.getGbDoDepDisGoodsId());
                historyEntity.setGbDohQuantity(orderQuantity);
                historyEntity.setGbDohStandard(orderStandard);
                historyEntity.setGbDohStandardId(order.getGbDoDsStandardId());
                historyEntity.setGbDohStandardScale(order.getGbDoDsStandardScale());
                historyEntity.setGbDohDepartmentId(order.getGbDoDepartmentId());
                historyEntity.setGbDohDepartmentFatherId(order.getGbDoDepartmentFatherId());
                historyEntity.setGbDohOrderUserId(order.getGbDoOrderUserId());
                gbDepOrdersHistoryService.save(historyEntity);
            } else if (equalNumber == 0 && historyEntities.size() == 3) {
                //删除最早一个
                GbDepartmentOrdersHistoryEntity first = historyEntities.get(0);
                Integer nxRestrauntOrdersHistoryId = first.getGbDepartmentOrdersHistoryId();
                gbDepOrdersHistoryService.delete(nxRestrauntOrdersHistoryId);
                //添加新的
                GbDepartmentOrdersHistoryEntity historyEntity = new GbDepartmentOrdersHistoryEntity();
                historyEntity.setGbDohApplyDate(order.getGbDoApplyDate());
                historyEntity.setGbDohDepDisGoodsId(order.getGbDoDepDisGoodsId());
                historyEntity.setGbDohQuantity(orderQuantity);
                historyEntity.setGbDohStandard(orderStandard);
                historyEntity.setGbDohStandardId(order.getGbDoDsStandardId());
                historyEntity.setGbDohStandardScale(order.getGbDoDsStandardScale());
                historyEntity.setGbDohDepartmentId(order.getGbDoDepartmentId());
                historyEntity.setGbDohDepartmentFatherId(order.getGbDoDepartmentFatherId());
                historyEntity.setGbDohOrderUserId(order.getGbDoOrderUserId());
                gbDepOrdersHistoryService.save(historyEntity);
            }

        } else {
            //添加新的
            GbDepartmentOrdersHistoryEntity historyEntity = new GbDepartmentOrdersHistoryEntity();
            historyEntity.setGbDohApplyDate(order.getGbDoApplyDate());
            historyEntity.setGbDohDepDisGoodsId(order.getGbDoDepDisGoodsId());
            historyEntity.setGbDohQuantity(orderQuantity);
            historyEntity.setGbDohStandard(orderStandard);
            historyEntity.setGbDohStandardId(order.getGbDoDsStandardId());
            historyEntity.setGbDohStandardScale(order.getGbDoDsStandardScale());
            historyEntity.setGbDohDepartmentId(order.getGbDoDepartmentId());
            historyEntity.setGbDohDepartmentFatherId(order.getGbDoDepartmentFatherId());
            historyEntity.setGbDohOrderUserId(order.getGbDoOrderUserId());
            gbDepOrdersHistoryService.save(historyEntity);
        }

    }


    @RequestMapping(value = "/cancelOrderOutWeight", method = RequestMethod.POST)
    @ResponseBody
    public R cancleOrderOutWeight(@RequestBody GbDepartmentOrdersEntity order) {

        List<GbDepartmentGoodsStockEntity> goodsStockEntityList = order.getGoodsStockEntityList();
        if (goodsStockEntityList.size() > 0) {

            for (GbDepartmentGoodsStockEntity stockEntity : goodsStockEntityList) {
                //修改出库部门数据
                Integer gbDgsGbGoodsStockId = stockEntity.getGbDgsGbGoodsStockId();
                GbDepartmentGoodsStockEntity outStockEntity = gbDepartmentGoodsStockService.queryObject(gbDgsGbGoodsStockId);
                BigDecimal outStockRestWeight = new BigDecimal(outStockEntity.getGbDgsRestWeight());
                BigDecimal outStockRestSubtotal = new BigDecimal(outStockEntity.getGbDgsRestSubtotal());
                BigDecimal newWeight = new BigDecimal(stockEntity.getGbDgsWeight());
                BigDecimal newSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());
                BigDecimal outTotalRestWeight = outStockRestWeight.add(newWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal outTotalRestSubtotal = outStockRestSubtotal.add(newSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                outStockEntity.setGbDgsRestWeight(outTotalRestWeight.toString());
                outStockEntity.setGbDgsRestSubtotal(outTotalRestSubtotal.toString());

                //
                if (outStockEntity.getGbDgsRestWeightShowStandard() != null) {
                    Integer gbDgsGbDepDisGoodsId = outStockEntity.getGbDgsGbDepDisGoodsId();
                    GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDgsGbDepDisGoodsId);
                    BigDecimal gbDdgShowStandardScale = new BigDecimal(departmentDisGoodsEntity.getGbDdgShowStandardScale());
                    BigDecimal newShowStandardWeight = outTotalRestWeight.divide(gbDdgShowStandardScale, 1, BigDecimal.ROUND_HALF_UP);
                    outStockEntity.setGbDgsRestWeightShowStandard(newShowStandardWeight.toString());
                    outStockEntity.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
                }
                gbDepartmentGoodsStockService.update(outStockEntity);

                System.out.println("outstocenene" + outStockEntity.getGbDgsGbDepDisGoodsId());
                updateDepDisGoods(stockEntity, outStockEntity.getGbDgsGbDepDisGoodsId(), "add");


                //删除自己
                gbDepartmentGoodsStockService.delete(stockEntity.getGbDepartmentGoodsStockId());
            }
        }

        order.setGbDoStatus(getGbOrderStatusNew());
        order.setGbDoBuyStatus(getGbOrderBuyStatusNew());
        gbDepartmentOrdersService.update(order);

        return R.ok();
    }


    @RequestMapping(value = "/cancelOrderOutWeightSelf", method = RequestMethod.POST)
    @ResponseBody
    public R cancelOrderOutWeightSelf(@RequestBody GbDepartmentOrdersEntity order) {


        List<GbDepartmentGoodsStockEntity> outGoodsStockEntityList = order.getGoodsStockEntityList();
        if (outGoodsStockEntityList.size() > 0) {
            for (GbDepartmentGoodsStockEntity stockEntity : outGoodsStockEntityList) {
                gbDepartmentGoodsStockService.delete(stockEntity.getGbDepartmentGoodsStockId());
            }
        }
        order.setGbDoWeight(null);
        order.setGbDoSellingPrice(null);
        order.setGbDoSellingSubtotal(null);
        order.setGbDoPrice(null);
        order.setGbDoPickUserId(null);
        order.setGbDoStatus(getGbOrderStatusNew());
        order.setGbDoBuyStatus(getGbOrderBuyStatusNew());
        gbDepartmentOrdersService.update(order);

        return R.ok();
    }

    //


    /**
     * 库房和自制公用保存订单出库数量
     *
     * @param order
     * @return
     */
    @RequestMapping(value = "/saveOrderOutWeightStock", method = RequestMethod.POST)
    @ResponseBody
    public R saveOrderOutWeightStock(@RequestBody GbDepartmentOrdersEntity order) {

        saveOutWeightStock(order);
        order.setGbDoStatus(getGbOrderStatusHasFinished());
        order.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
        gbDepartmentOrdersService.update(order);

        updateWeightOrderAmount(order);

        return R.ok();
    }

    /**
     * 库房和自制公用保存订单出库数量
     *
     * @param order
     * @return
     */
    @RequestMapping(value = "/saveOrderOutWeightStockNoPrice", method = RequestMethod.POST)
    @ResponseBody
    public R saveOrderOutWeightStockNoPrice(@RequestBody GbDepartmentOrdersEntity order) {


        if (order.getGbDoPrice() != null) {
            BigDecimal decimal = new BigDecimal(order.getGbDoWeight()).multiply(new BigDecimal(order.getGbDoPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
            order.setGbDoSubtotal(decimal.toString());
            order.setGbDoStatus(getGbOrderStatusHasFinished());
            order.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());

        } else {
            order.setGbDoStatus(getGbOrderStatusProcurement());
            order.setGbDoBuyStatus(getGbOrderBuyStatusPrepareing());
        }

        saveOutWeightStockNoPrice(order);

        gbDepartmentOrdersService.update(order);

        updateWeightOrderAmount(order);

        return R.ok();
    }


    @RequestMapping(value = "/saveNoPriceOrderPrice", method = RequestMethod.POST)
    @ResponseBody
    public R saveNoPriceOrderPrice(@RequestBody GbDepartmentOrdersEntity order) {

        //如果有重量
        List<GbDepartmentGoodsStockEntity> outGoodsStockEntityList = order.getGoodsStockEntityList();
        if (outGoodsStockEntityList.size() > 0) {
            BigDecimal decimal1 = new BigDecimal(order.getGbDoPrice()).multiply(new BigDecimal(order.getGbDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
            order.setGbDoSubtotal(decimal1.toString());
            order.setGbDoStatus(getGbOrderStatusHasFinished());
            order.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
            BigDecimal gbDoPriceB = new BigDecimal(order.getGbDoPrice());
            for (GbDepartmentGoodsStockEntity stockEntity : outGoodsStockEntityList) {
                BigDecimal weightB = new BigDecimal(stockEntity.getGbDgsWeight());
                BigDecimal decimal = gbDoPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                stockEntity.setGbDgsSubtotal(decimal.toString());
                stockEntity.setGbDgsPrice(order.getGbDoPrice());
                stockEntity.setGbDgsRestSubtotal(decimal.toString());
                gbDepartmentGoodsStockService.update(stockEntity);
            }
        }

        gbDepartmentOrdersService.update(order);

        return R.ok();
    }


    @RequestMapping(value = "/editOrderOutWeightStock", method = RequestMethod.POST)
    @ResponseBody
    public R editOrderOutWeightStock(@RequestBody GbDepartmentOrdersEntity order) {


        //subscribe depgoods weight
        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", gbDepartmentOrdersId);
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepartmentGoodsStockService.queryGoodsStockByParams(map);
        for (GbDepartmentGoodsStockEntity orderStock : stockEntities) {
            Integer gbDgsGbGoodsStockId = orderStock.getGbDgsGbGoodsStockId();
            GbDepartmentGoodsStockEntity stockEntity = gbDepartmentGoodsStockService.queryObject(gbDgsGbGoodsStockId);
            updateDepDisGoods(orderStock, stockEntity.getGbDgsGbDepDisGoodsId(), "add");
            gbDepartmentGoodsStockService.delete(orderStock.getGbDepartmentGoodsStockId());

        }


        saveOutWeightStock(order);

        gbDepartmentOrdersService.update(order);
        return R.ok();
    }

    @RequestMapping(value = "/editOrderOutWeightStockNoPrice", method = RequestMethod.POST)
    @ResponseBody
    public R editOrderOutWeightStockNoPrice(@RequestBody GbDepartmentOrdersEntity order) {


        //subscribe depgoods weight
        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", gbDepartmentOrdersId);
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepartmentGoodsStockService.queryGoodsStockByParams(map);
        for (GbDepartmentGoodsStockEntity orderStock : stockEntities) {
            Integer gbDepartmentGoodsStockId = orderStock.getGbDgsGbGoodsStockId();
            GbDepartmentGoodsStockEntity outStockEntity = gbDepartmentGoodsStockService.queryObject(gbDepartmentGoodsStockId);
            updateDepDisGoodsNoPrice(orderStock, outStockEntity, "add");
            gbDepartmentGoodsStockService.delete(orderStock.getGbDepartmentGoodsStockId());
        }

        saveOutWeightStockNoPrice(order);

        gbDepartmentOrdersService.update(order);
        return R.ok();
    }


    @RequestMapping(value = "/saveOrderOutWeightSelf", method = RequestMethod.POST)
    @ResponseBody
    public R saveOrderOutWeightSelf(@RequestBody GbDepartmentOrdersEntity order) {
        order.setGbDoStatus(getGbOrderStatusHasFinished());
        order.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
        gbDepartmentOrdersService.update(order);

        updateWeightOrderAmount(order);

        saveOutWeightSelf(order);

        return R.ok();
    }

    @RequestMapping(value = "/editOrderOutWeightSelf", method = RequestMethod.POST)
    @ResponseBody
    public R editOrderOutWeightSelf(@RequestBody GbDepartmentOrdersEntity order) {

        gbDepartmentOrdersService.update(order);

        List<GbDepartmentGoodsStockEntity> goodsStockEntityList = order.getGoodsStockEntityList();
        if (goodsStockEntityList.size() > 0) {
            for (GbDepartmentGoodsStockEntity stockEntity : goodsStockEntityList) {
                stockEntity.setGbDgsWeight(order.getGbDoWeight());
                stockEntity.setGbDgsPrice(order.getGbDoPrice());
                stockEntity.setGbDgsSubtotal(order.getGbDoSubtotal());
                stockEntity.setGbDgsRestSubtotal(order.getGbDoSubtotal());
                stockEntity.setGbDgsRestWeight(order.getGbDoWeight());
                stockEntity.setGbDgsProduceSubtotal("0");
                stockEntity.setGbDgsFullTime(formatFullTime());
                stockEntity.setGbDgsDate(formatWhatDay(0));
                stockEntity.setGbDgsTimeStamp(getTimeStamp());
                stockEntity.setGbDgsWeek(getWeek(0));
                stockEntity.setGbDgsMonth(formatWhatMonth(0));
                stockEntity.setGbDgsYear(formatWhatYear(0));
                stockEntity.setGbDgsProfitSubtotal("0");
                stockEntity.setGbDgsAfterProfitSubtotal("0");
                stockEntity.setGbDgsProfitWeight("0");
                stockEntity.setGbDgsProduceSellingSubtotal("0");

                String gbDgsSellingPrice = stockEntity.getGbDgsSellingPrice();
                if (!gbDgsSellingPrice.equals("-1")) {
                    GbDepartmentGoodsStockEntity stockEntity1 = order.getSelfControlStockEntity();
                    if (stockEntity1 != null) {
                        if (order.getGbDoSellingPrice() != null && new BigDecimal(order.getGbDoSellingPrice()).compareTo(BigDecimal.ZERO) == 1) {
                            BigDecimal subtract = new BigDecimal(order.getGbDoSellingPrice()).subtract(new BigDecimal(order.getGbDoPrice()));
                            BigDecimal divide = new BigDecimal(order.getGbDoPrice()).divide(new BigDecimal(order.getGbDoSellingPrice()), 2, BigDecimal.ROUND_HALF_UP);
                            stockEntity.setGbDgsBetweenPrice(subtract.toString());
                            stockEntity.setGbDgsCostRate(divide.toString());
                            stockEntity.setGbDgsSellingPrice(order.getGbDoSellingPrice());
                            stockEntity.setGbDgsSellingSubtotal(order.getGbDoSellingSubtotal());

                        } else {
                            stockEntity.setGbDgsBetweenPrice("0");
                            stockEntity.setGbDgsCostRate("0");
                            stockEntity.setGbDgsSellingPrice("0");
                        }
                    }
                } else {
                    stockEntity.setGbDgsSellingPrice("-1");
                    stockEntity.setGbDgsSellingSubtotal("0");
                }

                gbDepartmentGoodsStockService.update(stockEntity);
            }
        }


        return R.ok();
    }

    private void updateWeightOrderAmount(GbDepartmentOrdersEntity order) {

        //weightGoods
        Integer gbDoWeightGoodsId = order.getGbDoWeightGoodsId();
        if (gbDoWeightGoodsId != null) {
            GbDistributerWeightGoodsEntity gbWeightGoodsEntity = gbDisWeightGoodsService.queryObject(gbDoWeightGoodsId);
            BigDecimal gbGwtOrderFinishCount = new BigDecimal(gbWeightGoodsEntity.getGbDwgOrderFinishAmount());
            BigDecimal add = gbGwtOrderFinishCount.add(new BigDecimal(1));
            gbWeightGoodsEntity.setGbDwgOrderFinishAmount(add.toString());
            if (add.compareTo(new BigDecimal(gbWeightGoodsEntity.getGbDwgOrderAmount())) == 0) {
                gbWeightGoodsEntity.setGbDwgStatus(1);
            }
            gbDisWeightGoodsService.update(gbWeightGoodsEntity);
        }

        // weightTotal
        Integer gbDoWeightTotalId = order.getGbDoWeightTotalId();
        if (gbDoWeightTotalId != null) {
            GbDistributerWeightTotalEntity gbWeightTotalEntity = gbDisWeightTotalService.queryObject(gbDoWeightTotalId);
            BigDecimal gbGwtOrderFinishCount = new BigDecimal(gbWeightTotalEntity.getGbGwtOrderFinishCount());
            BigDecimal add = gbGwtOrderFinishCount.add(new BigDecimal(1));
            gbWeightTotalEntity.setGbGwtOrderFinishCount(add.toString());
            if (add.compareTo(new BigDecimal(gbWeightTotalEntity.getGbGwtOrderCount())) == 0) {
                gbWeightTotalEntity.setGbGwtStatus(getGbWeightTotalStatusFinished());
            }
            gbDisWeightTotalService.update(gbWeightTotalEntity);
        }
    }


    private void saveOutWeightSelf(GbDepartmentOrdersEntity order) {

        String gbDoWeight = order.getGbDoWeight();
        String gbDoPrice = order.getGbDoPrice();
        String gbDoSubtotal = order.getGbDoSubtotal();

        if (new BigDecimal(gbDoWeight).compareTo(BigDecimal.ZERO) == 1) {
            //2,添加订单出库批次
            GbDepartmentGoodsStockEntity depGoodsStockEntity = new GbDepartmentGoodsStockEntity();
            depGoodsStockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());
            depGoodsStockEntity.setGbDgsGbPurGoodsId(-1);
            depGoodsStockEntity.setGbDgsGbDisGoodsId(order.getGbDoDisGoodsId());
            depGoodsStockEntity.setGbDgsGbDisGoodsFatherId(order.getGbDoDisGoodsFatherId());
            depGoodsStockEntity.setGbDgsGbDepDisGoodsId(order.getGbDoDepDisGoodsId());
            depGoodsStockEntity.setGbDgsWeight(gbDoWeight);
            depGoodsStockEntity.setGbDgsPrice(gbDoPrice);
            depGoodsStockEntity.setGbDgsSubtotal(gbDoSubtotal);
            depGoodsStockEntity.setGbDgsRestSubtotal(gbDoSubtotal);
            depGoodsStockEntity.setGbDgsRestWeight(gbDoWeight);
            depGoodsStockEntity.setGbDgsGbDepartmentId(order.getGbDoDepartmentId());
            depGoodsStockEntity.setGbDgsGbDepartmentFatherId(order.getGbDoDepartmentFatherId());
            depGoodsStockEntity.setGbDgsGbDistributerId(order.getGbDoDistributerId());
            depGoodsStockEntity.setGbDgsGbGoodsStockId(-1);
            depGoodsStockEntity.setGbDgsGbFromDepartmentId(order.getGbDoToDepartmentId());
            depGoodsStockEntity.setGbDgsOutDate(formatWhatDay(0));
            depGoodsStockEntity.setGbDgsOutFullTime(formatFullTime());
            depGoodsStockEntity.setGbDgsOutHour(Integer.valueOf(formatWhatHour(0)));
            depGoodsStockEntity.setGbDgsStatus(-1);
            depGoodsStockEntity.setGbDgsLossWeight("0");
            depGoodsStockEntity.setGbDgsLossSubtotal("0");
            depGoodsStockEntity.setGbDgsReturnWeight("0");
            depGoodsStockEntity.setGbDgsReturnSubtotal("0");
            depGoodsStockEntity.setGbDgsProduceWeight("0");
            depGoodsStockEntity.setGbDgsProduceSubtotal("0");
            depGoodsStockEntity.setGbDgsWasteWeight("0");
            depGoodsStockEntity.setGbDgsWasteSubtotal("0");
            depGoodsStockEntity.setGbDgsWeek(getWeek(0));
            depGoodsStockEntity.setGbDgsMonth(formatWhatMonth(0));
            depGoodsStockEntity.setGbDgsYear(formatWhatYear(0));
            depGoodsStockEntity.setGbDgsProfitSubtotal("0");
            depGoodsStockEntity.setGbDgsAfterProfitSubtotal("0");
            depGoodsStockEntity.setGbDgsProduceSellingSubtotal("0");
            depGoodsStockEntity.setGbDgsProfitWeight("0");
            GbDepartmentGoodsStockEntity stockEntity = order.getSelfControlStockEntity();
            if (stockEntity != null) {
                if (stockEntity.getGbDgsWasteFullTime() != null) {
                    String gbDgsWasteFullTime = stockEntity.getGbDgsWasteFullTime();
                    int integer;
                    try {
                        integer = Integer.parseInt(gbDgsWasteFullTime);
                    } catch (NumberFormatException e) {
                        integer = 0;
                    }
                    depGoodsStockEntity.setGbDgsWarnFullTime(formatWhatFullTime(integer));
                    depGoodsStockEntity.setGbDgsWasteFullTime(formatWhatFullTime(integer));
                }

                if (order.getGbDoSellingPrice() != null && new BigDecimal(order.getGbDoSellingPrice()).compareTo(BigDecimal.ZERO) == 1) {
                    BigDecimal subtract = new BigDecimal(order.getGbDoSellingPrice()).subtract(new BigDecimal(order.getGbDoPrice()));
                    BigDecimal divide = new BigDecimal(gbDoPrice).divide(new BigDecimal(order.getGbDoSellingPrice()), 2, BigDecimal.ROUND_HALF_UP);
                    depGoodsStockEntity.setGbDgsBetweenPrice(subtract.toString());
                    depGoodsStockEntity.setGbDgsCostRate(divide.toString());
                    depGoodsStockEntity.setGbDgsSellingPrice(order.getGbDoSellingPrice());
                    depGoodsStockEntity.setGbDgsSellingSubtotal(order.getGbDoSellingSubtotal());

                } else {
                    depGoodsStockEntity.setGbDgsSellingPrice("-1");
                    depGoodsStockEntity.setGbDgsSellingSubtotal("0");
                }
            }

            //weightGoods
            Integer gbDoWeightGoodsId = order.getGbDoWeightGoodsId();
            if (gbDoWeightGoodsId != null) {
                depGoodsStockEntity.setGbDgsWeightGoodsId(gbDoWeightGoodsId);
            }
            gbDepartmentGoodsStockService.save(depGoodsStockEntity);
        }

    }

    private void editOutWeight(GbDepartmentGoodsStockEntity stockEntity, GbDepartmentOrdersEntity order) {

        String gbDgsInventoryWeight = stockEntity.getGbDgsInventoryWeight();
        String gbDgsPrice = stockEntity.getGbDgsPrice();
        BigDecimal subtotal = new BigDecimal(gbDgsInventoryWeight).multiply(new BigDecimal(gbDgsPrice)).setScale(2, BigDecimal.ROUND_HALF_UP);
        gbDepartmentGoodsStockService.update(stockEntity);
        if (new BigDecimal(gbDgsInventoryWeight).compareTo(BigDecimal.ZERO) == 1) {
            //2,添加订单出库批次
            GbDepartmentGoodsStockEntity depGoodsStockEntity = new GbDepartmentGoodsStockEntity();
            depGoodsStockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());
            depGoodsStockEntity.setGbDgsGbPurGoodsId(order.getGbDoPurchaseGoodsId());
            depGoodsStockEntity.setGbDgsGbDisGoodsId(order.getGbDoDisGoodsId());
            depGoodsStockEntity.setGbDgsGbDisGoodsFatherId(order.getGbDoDisGoodsFatherId());
            depGoodsStockEntity.setGbDgsGbDepDisGoodsId(order.getGbDoDepDisGoodsId());
            depGoodsStockEntity.setGbDgsWeight(gbDgsInventoryWeight);
            depGoodsStockEntity.setGbDgsPrice(gbDgsPrice);
            depGoodsStockEntity.setGbDgsSubtotal(subtotal.toString());
            depGoodsStockEntity.setGbDgsRestSubtotal(subtotal.toString());
            depGoodsStockEntity.setGbDgsRestWeight(gbDgsInventoryWeight);
            depGoodsStockEntity.setGbDgsGbDepartmentId(order.getGbDoDepartmentId());
            depGoodsStockEntity.setGbDgsGbDepartmentFatherId(order.getGbDoDepartmentFatherId());
            depGoodsStockEntity.setGbDgsGbDistributerId(order.getGbDoDistributerId());
            depGoodsStockEntity.setGbDgsGbGoodsStockId(stockEntity.getGbDepartmentGoodsStockId());
            depGoodsStockEntity.setGbDgsGbFromDepartmentId(order.getGbDoToDepartmentId());
            depGoodsStockEntity.setGbDgsOutDate(formatWhatDay(0));
            depGoodsStockEntity.setGbDgsOutFullTime(formatFullTime());
            depGoodsStockEntity.setGbDgsOutHour(Integer.valueOf(formatWhatHour(0)));
            depGoodsStockEntity.setGbDgsStatus(-1);
            depGoodsStockEntity.setGbDgsLossWeight("0");
            depGoodsStockEntity.setGbDgsLossSubtotal("0");
            depGoodsStockEntity.setGbDgsReturnWeight("0");
            depGoodsStockEntity.setGbDgsReturnSubtotal("0");
            depGoodsStockEntity.setGbDgsProduceWeight("0");
            depGoodsStockEntity.setGbDgsProduceSubtotal("0");
            depGoodsStockEntity.setGbDgsWasteWeight("0");
            depGoodsStockEntity.setGbDgsWasteSubtotal("0");
            depGoodsStockEntity.setGbDgsProfitSubtotal("0");
            depGoodsStockEntity.setGbDgsAfterProfitSubtotal("0");
            depGoodsStockEntity.setGbDgsProduceSellingSubtotal("0");
            depGoodsStockEntity.setGbDgsProfitWeight("0");

            if (stockEntity.getGbDgsWarnFullTime() != null) {
                depGoodsStockEntity.setGbDgsWarnFullTime(stockEntity.getGbDgsWarnFullTime());
            }
            if (stockEntity.getGbDgsWasteFullTime() != null) {
                depGoodsStockEntity.setGbDgsWasteFullTime(stockEntity.getGbDgsWasteFullTime());
            }

            //sellingSubtotal
            if (order.getGbDoSellingPrice() != null) {
                BigDecimal subtract = new BigDecimal(stockEntity.getGbDgsSellingPrice()).subtract(new BigDecimal(stockEntity.getGbDgsPrice()));
                BigDecimal divide = new BigDecimal(stockEntity.getGbDgsPrice()).divide(new BigDecimal(stockEntity.getGbDgsSellingPrice()), 2, BigDecimal.ROUND_HALF_UP);
                depGoodsStockEntity.setGbDgsSellingPrice(order.getGbDoSellingPrice());
                depGoodsStockEntity.setGbDgsBetweenPrice(subtract.toString());
                depGoodsStockEntity.setGbDgsCostRate(divide.toString());
                depGoodsStockEntity.setGbDgsSellingSubtotal(order.getGbDoSellingSubtotal());
            }
            gbDepartmentGoodsStockService.save(depGoodsStockEntity);
            updateDepDisGoods(depGoodsStockEntity, stockEntity.getGbDgsGbDepDisGoodsId(), "subtract");

        }

    }


    private void saveOutWeightStock(GbDepartmentOrdersEntity order) {
        System.out.println("ordldldlfd" + order.getOutGoodsStockEntityList().size());
        for (GbDepartmentGoodsStockEntity outStockEntity : order.getOutGoodsStockEntityList()) {
            //1，修改库房库存批次数据
            String gbDgsInventoryWeight = outStockEntity.getGbDgsInventoryWeight();
            String gbDgsPrice = outStockEntity.getGbDgsPrice();
            BigDecimal subtotal = new BigDecimal(gbDgsInventoryWeight).multiply(new BigDecimal(gbDgsPrice)).setScale(1, BigDecimal.ROUND_HALF_UP);
            gbDepartmentGoodsStockService.update(outStockEntity);

            if (new BigDecimal(gbDgsInventoryWeight).compareTo(BigDecimal.ZERO) == 1) {
                //2,添加订单出库批次
                GbDepartmentGoodsStockEntity depGoodsStockEntity = new GbDepartmentGoodsStockEntity();
                depGoodsStockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());
                depGoodsStockEntity.setGbDgsGbPurGoodsId(outStockEntity.getGbDgsGbPurGoodsId());
                depGoodsStockEntity.setGbDgsGbDisGoodsId(order.getGbDoDisGoodsId());
                depGoodsStockEntity.setGbDgsGbDisGoodsFatherId(order.getGbDoDisGoodsFatherId());
                depGoodsStockEntity.setGbDgsGbDepDisGoodsId(order.getGbDoDepDisGoodsId());
                depGoodsStockEntity.setGbDgsWeight(gbDgsInventoryWeight);
                depGoodsStockEntity.setGbDgsPrice(gbDgsPrice);
                depGoodsStockEntity.setGbDgsSubtotal(subtotal.toString());
                System.out.println("depgodostoootokckck" + subtotal.toString());
                depGoodsStockEntity.setGbDgsRestSubtotal(subtotal.toString());
                depGoodsStockEntity.setGbDgsRestWeight(gbDgsInventoryWeight);
                depGoodsStockEntity.setGbDgsGbDepartmentId(order.getGbDoDepartmentId());
                depGoodsStockEntity.setGbDgsGbDepartmentFatherId(order.getGbDoDepartmentFatherId());
                depGoodsStockEntity.setGbDgsGbDistributerId(order.getGbDoDistributerId());
                depGoodsStockEntity.setGbDgsGbGoodsStockId(outStockEntity.getGbDepartmentGoodsStockId());
                depGoodsStockEntity.setGbDgsGbFromDepartmentId(order.getGbDoToDepartmentId());
                depGoodsStockEntity.setGbDgsNxDistributerId(-1);
                depGoodsStockEntity.setGbDgsOutDate(formatWhatDay(0));
                depGoodsStockEntity.setGbDgsOutFullTime(formatFullTime());
                depGoodsStockEntity.setGbDgsOutHour(Integer.valueOf(formatWhatHour(0)));
                depGoodsStockEntity.setGbDgsStatus(-1);
                depGoodsStockEntity.setGbDgsLossWeight("0");
                depGoodsStockEntity.setGbDgsLossSubtotal("0");
                depGoodsStockEntity.setGbDgsReturnWeight("0");
                depGoodsStockEntity.setGbDgsReturnSubtotal("0");
                depGoodsStockEntity.setGbDgsProduceWeight("0");
                depGoodsStockEntity.setGbDgsProduceSubtotal("0");
                depGoodsStockEntity.setGbDgsWasteWeight("0");
                depGoodsStockEntity.setGbDgsWasteSubtotal("0");
                if (outStockEntity.getGbDgsWarnFullTime() != null) {
                    depGoodsStockEntity.setGbDgsWarnFullTime(outStockEntity.getGbDgsWarnFullTime());
                }
                if (outStockEntity.getGbDgsWasteFullTime() != null) {
                    depGoodsStockEntity.setGbDgsWasteFullTime(outStockEntity.getGbDgsWasteFullTime());
                }

                //sellingSubtotal
                if (order.getGbDoSellingPrice() != null && new BigDecimal(order.getGbDoSellingPrice()).compareTo(BigDecimal.ZERO) == 1) {
                    BigDecimal profit = new BigDecimal(order.getGbDoSellingPrice()).subtract(new BigDecimal(gbDgsPrice));
                    BigDecimal divide = new BigDecimal(gbDgsPrice).divide(new BigDecimal(order.getGbDoSellingPrice()), 2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal sellingSubtotal = new BigDecimal(order.getGbDoSellingPrice()).multiply(new BigDecimal(gbDgsInventoryWeight));
                    depGoodsStockEntity.setGbDgsBetweenPrice(profit.toString());
                    depGoodsStockEntity.setGbDgsCostRate(divide.toString());
                    depGoodsStockEntity.setGbDgsSellingPrice(order.getGbDoSellingPrice());
                    depGoodsStockEntity.setGbDgsSellingSubtotal(order.getGbDoSellingSubtotal());
                    depGoodsStockEntity.setGbDgsProfitSubtotal("0");
                    depGoodsStockEntity.setGbDgsProfitWeight("0");
                    depGoodsStockEntity.setGbDgsAfterProfitSubtotal("0");
                    depGoodsStockEntity.setGbDgsProduceSellingSubtotal("0");

                } else {
                    depGoodsStockEntity.setGbDgsSellingPrice("-1");
                }

                //weightGoods
                Integer gbDoWeightGoodsId = order.getGbDoWeightGoodsId();
                if (gbDoWeightGoodsId != null) {
                    depGoodsStockEntity.setGbDgsWeightGoodsId(gbDoWeightGoodsId);
                }

                gbDepartmentGoodsStockService.save(depGoodsStockEntity);

//                //update
                updateDepDisGoods(depGoodsStockEntity, outStockEntity.getGbDgsGbDepDisGoodsId(), "subtract");

            }
        }
    }


    private void saveOutWeightStockNoPrice(GbDepartmentOrdersEntity order) {
        System.out.println("ordldldlfd" + order.getOutGoodsStockEntityList().size());
        for (GbDepartmentGoodsStockEntity outStockEntity : order.getOutGoodsStockEntityList()) {
            //1，修改库房库存批次数据
            String gbDgsInventoryWeight = outStockEntity.getGbDgsInventoryWeight();
            gbDepartmentGoodsStockService.update(outStockEntity);

            if (new BigDecimal(gbDgsInventoryWeight).compareTo(BigDecimal.ZERO) == 1) {
                //2,添加订单出库批次
                GbDepartmentGoodsStockEntity depGoodsStockEntity = new GbDepartmentGoodsStockEntity();
                depGoodsStockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());
                depGoodsStockEntity.setGbDgsGbPurGoodsId(outStockEntity.getGbDgsGbPurGoodsId());
                depGoodsStockEntity.setGbDgsGbDisGoodsId(order.getGbDoDisGoodsId());
                depGoodsStockEntity.setGbDgsGbDisGoodsFatherId(order.getGbDoDisGoodsFatherId());
                depGoodsStockEntity.setGbDgsGbDepDisGoodsId(order.getGbDoDepDisGoodsId());
                depGoodsStockEntity.setGbDgsWeight(gbDgsInventoryWeight);
                if (order.getGbDoPrice() != null) {
                    BigDecimal gbDoPriceB = new BigDecimal(order.getGbDoPrice());
                    BigDecimal weightB = new BigDecimal(gbDgsInventoryWeight);
                    BigDecimal decimal = weightB.multiply(gbDoPriceB).setScale(1, BigDecimal.ROUND_HALF_UP);
                    depGoodsStockEntity.setGbDgsPrice(order.getGbDoPrice());
                    depGoodsStockEntity.setGbDgsSubtotal(decimal.toString());
                    depGoodsStockEntity.setGbDgsRestSubtotal(decimal.toString());


                }
                depGoodsStockEntity.setGbDgsRestWeight(gbDgsInventoryWeight);
                depGoodsStockEntity.setGbDgsGbDepartmentId(order.getGbDoDepartmentId());
                depGoodsStockEntity.setGbDgsGbDepartmentFatherId(order.getGbDoDepartmentFatherId());
                depGoodsStockEntity.setGbDgsGbDistributerId(order.getGbDoDistributerId());
                depGoodsStockEntity.setGbDgsGbGoodsStockId(outStockEntity.getGbDepartmentGoodsStockId());
                depGoodsStockEntity.setGbDgsGbFromDepartmentId(order.getGbDoToDepartmentId());
                depGoodsStockEntity.setGbDgsNxDistributerId(-1);
                depGoodsStockEntity.setGbDgsOutDate(formatWhatDay(0));
                depGoodsStockEntity.setGbDgsOutFullTime(formatFullTime());
                depGoodsStockEntity.setGbDgsOutHour(Integer.valueOf(formatWhatHour(0)));
                depGoodsStockEntity.setGbDgsStatus(-1);
                depGoodsStockEntity.setGbDgsLossWeight("0");
                depGoodsStockEntity.setGbDgsLossSubtotal("0");
                depGoodsStockEntity.setGbDgsReturnWeight("0");
                depGoodsStockEntity.setGbDgsReturnSubtotal("0");
                depGoodsStockEntity.setGbDgsProduceWeight("0");
                depGoodsStockEntity.setGbDgsProduceSubtotal("0");
                depGoodsStockEntity.setGbDgsWasteWeight("0");
                depGoodsStockEntity.setGbDgsWasteSubtotal("0");
                if (outStockEntity.getGbDgsWarnFullTime() != null) {
                    depGoodsStockEntity.setGbDgsWarnFullTime(outStockEntity.getGbDgsWarnFullTime());
                }
                if (outStockEntity.getGbDgsWasteFullTime() != null) {
                    depGoodsStockEntity.setGbDgsWasteFullTime(outStockEntity.getGbDgsWasteFullTime());
                }

                //sellingSubtotal
                if (order.getGbDoSellingPrice() != null && new BigDecimal(order.getGbDoSellingPrice()).compareTo(BigDecimal.ZERO) == 1) {
//                    BigDecimal profit = new BigDecimal(order.getGbDoSellingPrice()).subtract(new BigDecimal(gbDgsPrice));
//                    BigDecimal divide = new BigDecimal(gbDgsPrice).divide(new BigDecimal(order.getGbDoSellingPrice()), 2, BigDecimal.ROUND_HALF_UP);
//                    BigDecimal sellingSubtotal = new BigDecimal(order.getGbDoSellingPrice()).multiply(new BigDecimal(gbDgsInventoryWeight));
//                    depGoodsStockEntity.setGbDgsBetweenPrice(profit.toString());
//                    depGoodsStockEntity.setGbDgsCostRate(divide.toString());
                    depGoodsStockEntity.setGbDgsSellingPrice(order.getGbDoSellingPrice());
                    depGoodsStockEntity.setGbDgsSellingSubtotal(order.getGbDoSellingSubtotal());
                    depGoodsStockEntity.setGbDgsProfitSubtotal("0");
                    depGoodsStockEntity.setGbDgsProfitWeight("0");
                    depGoodsStockEntity.setGbDgsAfterProfitSubtotal("0");
                    depGoodsStockEntity.setGbDgsProduceSellingSubtotal("0");

                } else {
                    depGoodsStockEntity.setGbDgsSellingPrice("-1");
                }

                //weightGoods
                Integer gbDoWeightGoodsId = order.getGbDoWeightGoodsId();
                if (gbDoWeightGoodsId != null) {
                    depGoodsStockEntity.setGbDgsWeightGoodsId(gbDoWeightGoodsId);
                }

                gbDepartmentGoodsStockService.save(depGoodsStockEntity);

//                //update
                updateDepDisGoodsNoPrice(depGoodsStockEntity, outStockEntity, "subtract");

            }
        }
    }

    @RequestMapping(value = "/getDisTodayToDepartmentOrders/{disId}")
    @ResponseBody
    public R getDisTodayToDepartmentOrders(@PathVariable Integer disId) {

        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(disId);
        System.out.println("gbbgbg" + gbDistributerEntity);
        List<GbDepartmentEntity> result = new ArrayList<>();
        List<GbDepartmentEntity> stockDepartmentList = gbDistributerEntity.getStockDepartmentList();
        if (stockDepartmentList.size() > 0) {
            for (GbDepartmentEntity stockDep : stockDepartmentList) {
                Map<String, Object> map = new HashMap<>();
                map.put("toDepId", stockDep.getGbDepartmentId());
                map.put("status", 3);
                System.out.println("newowowow" + map);
                Integer newAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
                if (newAmount > 0) {
                    result.add(stockDep);
                }
            }
        }

        List<GbDepartmentEntity> purDepartmentList = gbDistributerEntity.getPurDepartmentList();
        if (purDepartmentList.size() > 0) {
            for (GbDepartmentEntity stockDep : purDepartmentList) {
                Map<String, Object> map = new HashMap<>();
                map.put("toDepId", stockDep.getGbDepartmentId());
                map.put("status", 3);
                System.out.println("newowowow" + map);
                Integer newAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
                if (newAmount > 0) {
                    result.add(stockDep);
                }
            }
        }
        List<GbDepartmentEntity> kitchenDepartmentList = gbDistributerEntity.getKitchenDepartmentList();
        if (purDepartmentList.size() > 0) {
            for (GbDepartmentEntity stockDep : kitchenDepartmentList) {
                Map<String, Object> map = new HashMap<>();
                map.put("toDepId", stockDep.getGbDepartmentId());
                map.put("status", 3);
                System.out.println("newowowow" + map);
                Integer newAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
                if (newAmount > 0) {
                    result.add(stockDep);
                }
            }
        }


        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        map1.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryFatherDepartment(map1);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("orderArr", departmentEntities);
        mapR.put("toDepArr", result);


        return R.ok().put("data", mapR);


    }

    @RequestMapping(value = "/getDisTodayOrders/{disId}")
    @ResponseBody
    public R getDisTodayOrders(@PathVariable Integer disId) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        map1.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryFatherDepartment(map1);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                Map<String, Object> map = new HashMap<>();
                map.put("disId", disId);
                map.put("equalBuyStatus", 0);
                map.put("depFatherId", department.getGbDepartmentId());
                map.put("status", 1);
                System.out.println("newowowow" + map);
                Integer newAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
                department.setNewOrderAmount(newAmount.toString());
                Map<String, Object> map22 = new HashMap<>();
                map22.put("depFatherId", department.getGbDepartmentId());
                map22.put("dayuStatus", 0);
                map22.put("status", 2);
                Integer preAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map22);
                department.setPrepareOrderAmount(preAmount.toString());
            }
        }

        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("equalStatus", -1);
        Integer amount = gbDepartmentBillService.queryBillsCountByParamsGb(map4);

        Map<String, Object> todayData = new HashMap<>();
        todayData.put("newAmount", amount);
        todayData.put("arr", departmentEntities);
        return R.ok().put("data", todayData);

    }

    @RequestMapping(value = "/getDisTodayFatherGoods/{disId}")
    @ResponseBody
    public R getDisTodayFatherGoods(@PathVariable Integer disId) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        map1.put("depType", getGbDepartmentTypeMendian());
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepartmentOrdersService.queryFatherGoods(map1);

        return R.ok().put("data", fatherGoodsEntities);

    }


    @RequestMapping(value = "/getDisStockDepartment", method = RequestMethod.POST)
    @ResponseBody
    public R getDisStockDepartment(Integer toDepId, String orderType) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("toDepId", toDepId);
        map1.put("equalStatus", -1);
//        map1.put("status", -1);
        map1.put("orderType", getGbOrderTypeTuihuo());
        System.out.println("mapap111111" + map1);
        List<GbDepartmentEntity> departmentEntitiesT = gbDepartmentOrdersService.queryFatherDepartment(map1);
        System.out.println("departmentEntitiesTdepartmentEntitiesT" + departmentEntitiesT.size());

        if (departmentEntitiesT.size() > 0) {
            for (GbDepartmentEntity department : departmentEntitiesT) {
                map1.put("depFatherId", department.getGbDepartmentId());
                System.out.println("mapap111111ddddddd" + map1);
                Integer newAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map1);
                department.setDepReturnGoodsTotalString(newAmount.toString());
            }
        }

        Map<String, Object> map11 = new HashMap<>();
        map11.put("toDepId", toDepId);
        map11.put("status", 3);
        map11.put("dayuBuyStatus", -1);
        map11.put("orderType", orderType);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryFatherDepartment(map11);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                Map<String, Object> map = new HashMap<>();
                map.put("toDepId", toDepId);
                map.put("equalBuyStatus", 0);
                map.put("depFatherId", department.getGbDepartmentId());
                map.put("status", 1);
                System.out.println("newowowow" + map);
                Integer newAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
                department.setNewOrderAmount(newAmount.toString());
                Map<String, Object> map22 = new HashMap<>();
                map22.put("toDepId", toDepId);
                map22.put("depFatherId", department.getGbDepartmentId());
                map22.put("buyStatus", 3);
                map22.put("status", 1);
                map22.put("dayuBuyStatus", 0);
                Integer preAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map22);
                department.setPrepareOrderAmount(preAmount.toString());
                Map<String, Object> map111 = new HashMap<>();
                map111.put("toDepId", toDepId);
                map111.put("depFatherId", department.getGbDepartmentId());
                map111.put("equalStatus", 2);
                Integer hasWeightAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map111);
                department.setHasWeightOrderAmount(hasWeightAmount.toString());
            }
        }


        System.out.println("ddidid" + departmentEntitiesT.size());
        departmentEntitiesT.addAll(departmentEntities);


        Map<String, Object> map2 = new HashMap<>();
        map2.put("toDepId", toDepId);
        map2.put("buyStatus", 1);
        map2.put("dayuStatus", -1);
        map2.put("status", 3);
        map2.put("isSelf", 0);
        map2.put("isNotSelf", 1);
        System.out.println("map222" + map2);
        Integer amount2 = gbDepartmentOrdersService.queryTotalByParams(map2);

        map2.put("isSelf", 1);
        Integer amount3 = gbDepartmentOrdersService.queryTotalByParams(map2);

        Map<String, Object> todayData = new HashMap<>();
        todayData.put("isSelfAmount", amount3);
        todayData.put("arr", departmentEntitiesT);
        todayData.put("amount", amount2);

        return R.ok().put("data", todayData);

    }


    @RequestMapping(value = "/getDisKitchenDepartment/{toDepId}")
    @ResponseBody
    public R getDisKitchenDepartment(@PathVariable Integer toDepId) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("toDepId", toDepId);
        map1.put("status", 3);
        map1.put("orderType", getGbOrderTypeKitchen());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryFatherDepartment(map1);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                Map<String, Object> map = new HashMap<>();
                map.put("toDepId", toDepId);
                map.put("depFatherId", department.getGbDepartmentId());
                map.put("status", 1);
                map.put("equalBuyStatus", 0);
                Integer newAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
                department.setNewOrderAmount(newAmount.toString());
                Map<String, Object> map22 = new HashMap<>();
                map22.put("toDepId", toDepId);
                map22.put("depFatherId", department.getGbDepartmentId());
                map22.put("buyStatus", 3);
                map22.put("status", 1);
                map22.put("dayuBuyStatus", 0);
                Integer preAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map22);
                department.setPrepareOrderAmount(preAmount.toString());
                Map<String, Object> map111 = new HashMap<>();
                map111.put("toDepId", toDepId);
                map111.put("depFatherId", department.getGbDepartmentId());
                map111.put("equalStatus", 2);
                Integer hasWeightAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map111);
                department.setHasWeightOrderAmount(hasWeightAmount.toString());
                Double total = 0.0;
                if (hasWeightAmount > 0) {
                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("equalStatus", 2);
                    map2.put("depFatherId", department.getGbDepartmentId());
                    map2.put("toDepId", toDepId);
                    total = gbDepartmentOrdersService.queryGbOrdersSellingSubtotal(map2);
                }
                department.setUpdateSubtotal(new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
        }

        Map<String, Object> map4 = new HashMap<>();
        map4.put("depId", toDepId);
        map4.put("equalStatus", -1);
        Integer amount = gbDepartmentBillService.queryBillsCountByParamsGb(map4);

        Map<String, Object> todayData = new HashMap<>();
        todayData.put("newAmount", amount);
        todayData.put("arr", departmentEntities);


        Map<String, Object> map2 = new HashMap<>();
        map2.put("depFatherId", toDepId);
        map2.put("toDepId", toDepId);
        map2.put("status", 3);
        map2.put("dayuStatus", -1);
        Integer amount2 = gbDepartmentOrdersService.queryTotalByParams(map2);
        todayData.put("applyAmount", amount2);


        return R.ok().put("data", todayData);

    }

    /**
     * 批发商给客户添加申请
     *
     * @param depFatherId 客户id
     * @return 订单
     */
    @RequestMapping(value = "/disGetDepTodayOrdersGb/{depFatherId}")
    @ResponseBody
    public R disGetDepTodayOrdersGb(@PathVariable Integer depFatherId) {
        System.out.println("zhemeeke" + depFatherId);

        //今天的数据
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("status", 3);
        map.put("orderBy", "time");
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.disQueryDisOrdersByParams(map);
        if ((ordersEntities.size() > 0)) {
            System.out.println("oreenmesiisis" + ordersEntities.size());
            return R.ok().put("data", ordersEntities);
        }
        return R.error(-1, "没有订单");
    }

    @RequestMapping(value = "/disGetToDepTodayOrdersGb/{depFatherId}")
    @ResponseBody
    public R disGetToDepTodayOrdersGb(@PathVariable Integer depFatherId) {
        System.out.println("zhemeeke" + depFatherId);

        //今天的数据
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", depFatherId);
        map.put("status", 3);
        map.put("orderBy", "time");
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.disQueryDisOrdersByParams(map);
        if ((ordersEntities.size() > 0)) {
            System.out.println("oreenmesiisis" + ordersEntities.size());
            return R.ok().put("data", ordersEntities);
        }
        return R.error(-1, "没有订单");
    }


    @RequestMapping(value = "/webGetOrderPage", method = RequestMethod.POST)
    @ResponseBody
    public R webGetOrderPage(Integer depFatherId,
                             String orderBy, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("orderBy", orderBy);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println("orderbyy" + map);

        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.disQueryDisOrdersByParams(map);

        //查询列表数据

//        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        Map<String, Object> mapC = new HashMap<>();
        mapC.put("depFatherId", depFatherId);
        mapC.put("status", 3);
        System.out.println("totootoacaoaooa" + mapC);
        Integer integerC = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(mapC);
        PageUtils pageUtil = new PageUtils(ordersEntities, integerC, limit, page);


        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depFatherId);
        map3.put("date", formatWhatDay(0));
        int count = gbDepartmentBillService.queryDepartmentBillCount(map3);
        int trade = count + 1;
        String no = "";
        if (trade < 100) {
            if (count < 10) {
                no = "00" + trade;
            } else {
                no = "0" + trade;
            }
        } else {
            no = String.valueOf(count);
        }

        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(depFatherId);
        String headPinyin = getHeadStringByString(gbDepartmentEntity.getGbDepartmentName(), true, null);
        String s = formatDayNumber(0) + headPinyin + no;
        double total = 0.0;

        map.put("subtotal", 0);
        Integer twoTotal = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
        if (twoTotal > 0) {
            total = gbDepartmentOrdersService.queryGbOrdersSubtotal(map);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("page", pageUtil);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("tradeNo", s);
        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/webGetToDepOrderPage", method = RequestMethod.POST)
    @ResponseBody
    public R webGetToDepOrderPage(Integer depFatherId,
                                  String orderBy, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("toDepId", depFatherId);
        map.put("orderBy", orderBy);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println("orderbyy" + map);

        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.disQueryDisOrdersByParams(map);

        //查询列表数据
        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);


        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depFatherId);
        map3.put("date", formatWhatDay(0));
        int count = gbDepartmentBillService.queryDepartmentBillCount(map3);
        int trade = count + 1;
        String no = "";
        if (trade < 100) {
            if (count < 10) {
                no = "00" + trade;
            } else {
                no = "0" + trade;
            }
        } else {
            no = String.valueOf(count);
        }

        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(depFatherId);
        String headPinyin = getHeadStringByString(gbDepartmentEntity.getGbDepartmentName(), true, null);
        String s = formatDayNumber(0) + headPinyin + no;
        double total = 0.0;

        map.put("subtotal", 0);
        Integer twoTotal = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
        if (twoTotal > 0) {
            total = gbDepartmentOrdersService.queryGbOrdersSubtotal(map);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("page", pageUtil);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("tradeNo", s);
        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/mendianGetDriveApplyGb/{depId}")
    @ResponseBody
    public R mendianGetDriveApplyGb(@PathVariable Integer depId) {

        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(depId);
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("arr", new ArrayList<>());

        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntities) {
                //明天的数据
                Map<String, Object> map31 = new HashMap<>();
                map31.put("depId", dep.getGbDepartmentId());
                map31.put("orderBy", "time");
                map31.put("status", 3);
                map31.put("orderTypeNotEqual", getGbOrderTypeZiCai());
                List<GbDepartmentOrdersEntity> ordersEntities3 = gbDepartmentOrdersService.queryMendianDisOrdersByParams(map31);
                if (ordersEntities3.size() > 0) {
                    Map<String, Object> map4 = new HashMap<>();
                    map4.put("depName", dep.getGbDepartmentName());
                    map4.put("arr", ordersEntities3);
                    list.add(map4);
                    map.put("arr", list);
                }

            }
        } else {
            //明天的数据
            Map<String, Object> map3 = new HashMap<>();
            map3.put("depId", depId);
            map3.put("orderBy", "time");
            map3.put("status", 3);
            map3.put("orderTypeNotEqual", getGbOrderTypeZiCai());
            System.out.println("kankana333" + map3);

            List<GbDepartmentOrdersEntity> ordersEntities3 = gbDepartmentOrdersService.queryMendianDisOrdersByParams(map3);
            map.put("arr", ordersEntities3);
        }


        Map<String, Object> map4 = new HashMap<>();
        map4.put("depId", depId);
        map4.put("equalStatus", -1);
        Integer amount = gbDepartmentBillService.queryBillsCountByParamsGb(map4);
        map.put("newAmount", amount);
        return R.ok().put("data", map);

    }


    @RequestMapping(value = "/supplierGetDepApply", method = RequestMethod.POST)
    @ResponseBody
    public R supplierGetApplyGb(Integer depFatherId, Integer supplierId) {
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depFatherId);
        Integer gbDepartmentSubAmount = departmentEntity.getGbDepartmentSubAmount();
        if (gbDepartmentSubAmount > 0) {
            List<GbDepartmentEntity> gbSubDepartments = gbDepartmentService.querySubDepartments(departmentEntity.getGbDepartmentId());
            if (gbSubDepartments.size() > 0) {
                List<Map<String, Object>> result = new ArrayList<>();
                for (GbDepartmentEntity dep : gbSubDepartments) {
                    Map<String, Object> map3 = new HashMap<>();
                    map3.put("supplierId", supplierId);
                    map3.put("depId", dep.getGbDepartmentId());
                    map3.put("orderBy", "time");
                    map3.put("status", 4);
                    List<GbDepartmentOrdersEntity> ordersEntities3 = gbDepartmentOrdersService.queryDisOrdersByParams(map3);
                    if ((ordersEntities3.size() > 0)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("depName", dep.getGbDepartmentName());
                        map.put("depOrders", ordersEntities3);
                        result.add(map);
                    }
                }
                return R.ok().put("data", result);
            } else {
                return R.error(-1, "没有订单000");
            }

        } else {
            Map<String, Object> map3 = new HashMap<>();
            map3.put("supplierId", supplierId);
            map3.put("depFatherId", depFatherId);
            map3.put("orderBy", "time");
            map3.put("status", 4);
            List<GbDepartmentOrdersEntity> ordersEntities3 = gbDepartmentOrdersService.queryDisOrdersByParams(map3);
            if ((ordersEntities3.size() > 0)) {
                return R.ok().put("data", ordersEntities3);
            } else {
                return R.error(-1, "没有订单1234");
            }
        }
    }

    @RequestMapping(value = "/stockGetDepApplyGbWindow")
    @ResponseBody
    public R stockGetDepApplyGbWindow(Integer depFatherId, Integer toDepId, Integer orderType) {

        Map<String, Object> map3 = new HashMap<>();
        map3.put("toDepId", toDepId);
        map3.put("depFatherId", depFatherId);
        map3.put("status", 3);
        map3.put("orderType", orderType);
        System.out.println("mapd3333" + map3);
        List<GbDistributerFatherGoodsEntity> ordersEntities3 = gbDepartmentOrdersService.stockGetDepApply(map3);
        System.out.println("siziiziz" + ordersEntities3.size());
        List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();

        if ((ordersEntities3.size() > 0)) {
            for (GbDistributerFatherGoodsEntity father : ordersEntities3) {
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = father.getFatherGoodsEntities();
                result.addAll(fatherGoodsEntities);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("arr", result);
            return R.ok().put("data", map);
        } else {
            return R.error(-1, "没有订单");

        }

    }


    @RequestMapping(value = "/stockGetDepApplyGb")
    @ResponseBody
    public R stockGetDepApplyGb(Integer depFatherId, Integer toDepId, Integer orderType) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("toDepId", toDepId);
        map3.put("depFatherId", depFatherId);
        map3.put("status", 3);
        map3.put("orderType", orderType);
        List<GbDistributerFatherGoodsEntity> ordersEntities3 = gbDepartmentOrdersService.stockGetDepApply(map3);
        List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();

        if ((ordersEntities3.size() > 0)) {
            for (GbDistributerFatherGoodsEntity father : ordersEntities3) {
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = father.getFatherGoodsEntities();
                result.addAll(fatherGoodsEntities);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("arr", result);
//            map.put("total", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP));

            return R.ok().put("data", map);
        } else {
            return R.error(-1, "没有订单");

        }

    }


    @RequestMapping(value = "/webStockGetDepApplyGb")
    @ResponseBody
    public R webStockGetDepApplyGb(Integer depFatherId, Integer toDepId) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("toDepId", toDepId);
        map3.put("depFatherId", depFatherId);
        map3.put("status", 3);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryMendianDisOrdersByParams(map3);


        double total = 0.0;
        map3.put("hasWeight", 1);
        Integer integer = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map3);
        if (integer > 0) {
            total = gbDepartmentOrdersService.queryGbOrdersSubtotal(map3);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("arr", ordersEntities);
        map.put("subtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

        return R.ok().put("data", map);

    }


    @RequestMapping(value = "/webStockGetDepApplyGbPage")
    @ResponseBody
    public R webStockGetDepApplyGbPage(Integer depFatherId, Integer toDepId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", toDepId);
        map.put("depFatherId", depFatherId);
        map.put("status", 3);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println("webebeboroorooroor" + map);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryMendianDisOrdersByParams(map);
        System.out.println("ororosiisi" + ordersEntities.size());
        //查询列表数据
        double total = 0.0;
        map.put("hasWeight", 1);
        Integer integer = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
        if (integer > 0) {
            total = gbDepartmentOrdersService.queryGbOrdersSubtotal(map);
        }

        Map<String, Object> mapC = new HashMap<>();
        mapC.put("toDepId", toDepId);
        mapC.put("depFatherId", depFatherId);
        mapC.put("status", 3);
        System.out.println("totootoacaoaooa" + mapC);
        Integer integerC = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(mapC);

        PageUtils pageUtil = new PageUtils(ordersEntities, integerC, limit, page);


        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depFatherId);
        map3.put("date", formatWhatDay(0));
        int count = gbDepartmentBillService.queryDepartmentBillCount(map3);
        int trade = count + 1;
        String no = "";
        if (trade < 100) {
            if (count < 10) {
                no = "00" + trade;
            } else {
                no = "0" + trade;
            }
        } else {
            no = String.valueOf(count);
        }
        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(depFatherId);
        String headPinyin = getHeadStringByString(gbDepartmentEntity.getGbDepartmentName(), true, null);
        String s = formatDayNumber(0) + headPinyin + no;

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("page", pageUtil);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("tradeNo", s);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/stockGetDepApplyGbIsSelfPrint")
    @ResponseBody
    public R stockGetDepApplyGbIsSelfPrint(Integer depFatherId, Integer toDepId) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("toDepId", toDepId);
        map3.put("depFatherId", depFatherId);
        map3.put("orderBy", "time");
        map3.put("status", 3);
        map3.put("weightId", -1);
        List<GbDistributerFatherGoodsEntity> ordersEntities3 = gbDepartmentOrdersService.stockGetDepApply(map3);
        List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();

        if ((ordersEntities3.size() > 0)) {
            for (GbDistributerFatherGoodsEntity father : ordersEntities3) {
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = father.getFatherGoodsEntities();
                result.addAll(fatherGoodsEntities);
            }

            return R.ok().put("data", result);
        } else {
            return R.error(-1, "没有订单");

        }
    }

    @RequestMapping(value = "/stockGetDepApplyGbIsSelf")
    @ResponseBody
    public R stockGetDepApplyGbIsSelf(Integer depFatherId, Integer toDepId) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("toDepId", toDepId);
        map3.put("depFatherId", depFatherId);
        map3.put("orderBy", "time");
        map3.put("status", 3);
        map3.put("isSelf", 1);
        List<GbDistributerFatherGoodsEntity> ordersEntities3 = gbDepartmentOrdersService.stockGetDepApply(map3);
        List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();

        if ((ordersEntities3.size() > 0)) {
            for (GbDistributerFatherGoodsEntity father : ordersEntities3) {
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = father.getFatherGoodsEntities();
                result.addAll(fatherGoodsEntities);
            }

            return R.ok().put("data", result);
        } else {
            return R.error(-1, "没有订单");

        }
    }


    @RequestMapping(value = "/depGetApplyGb/{depId}")
    @ResponseBody
    public R depGetApplyGb(@PathVariable Integer depId) {
        Map<String, Object> mapresult = new HashMap<>();
        mapresult.put("bill", -1);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depId);
        map3.put("orderBy", "time");
        map3.put("status", 4);
        List<GbDepartmentOrdersEntity> ordersEntities3 = gbDepartmentOrdersService.queryDisOrdersByParams(map3);
        mapresult.put("arr", ordersEntities3);
//
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("orderType", 5);
        map.put("willDate", formatWhatDay(0));
        map.put("status", 4);
        System.out.println("mapappapapapp" + map);
        List<GbDepartmentBillEntity> billEntities = gbDepartmentBillService.queryDepartmentBillList(map);
        if (billEntities.size() > 0) {
            mapresult.put("bill", billEntities.get(0));
        }
        return R.ok().put("data", mapresult);
    }


    /**
     * DISTRIBUTER
     * 批发商获取今日订货群和订单
     *
     * @param supplierId gonghuoshang商id
     * @return 订货群
     */
    @RequestMapping(value = "/supplierGetTodayOrderCustomer/{supplierId}")
    @ResponseBody
    public R supplierGetTodayOrderCustomer(@PathVariable Integer supplierId) {
        GbDistributerSupplierEntity disSupplierEntity = gbDistributerSupplierService.queryObject(supplierId);
        Integer gbDsOrderType = disSupplierEntity.getGbDsOrderType();
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("supplierId", supplierId);
        map1.put("status", 4);
        map1.put("orderType", gbDsOrderType);
        map1.put("toDepId", disSupplierEntity.getGbDsGbDepartmentId());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryDistributerTodayDepartments(map1);
        Map<String, Object> todayData = packageDisOrderByDep(departmentEntities, 0);
        return R.ok().put("data", todayData);
    }


    /**
     * DISTRIBUTER -- 老的页面格式，废弃
     * 批发商获取今日订货群和订单
     *
     * @param disId 批发商id
     * @return 订货群
     */
    @RequestMapping(value = "/disGetTodayOrderCustomerGb/{disId}")
    @ResponseBody
    public R disGetTodayOrderCustomerGb(@PathVariable Integer disId) {

        List<Map<String, Object>> returnData = new ArrayList<>();
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 4);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryDistributerTodayDepartments(map1);
        Map<String, Object> todayData = packageDisOrderByDep(departmentEntities, 0);

        if (departmentEntities.size() > 0) {
            Map<String, Object> map3 = new HashMap<>();
            map3.put("disId", disId);
            map3.put("equalBuyStatus", 0);
            map3.put("status", 3);
            map3.put("purchaseAuto", 0);
            List<GbDepartmentOrdersEntity> tomUnPurchaseApplys = gbDepartmentOrdersService.queryDisOrdersByParams(map3);
            todayData.put("buyOrders", tomUnPurchaseApplys.size());
            returnData.add(todayData);
        }
        return R.ok().put("data", returnData);
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
            int orderTotal = 0;

            List<GbDepartmentEntity> subDeps = new ArrayList<>();
            for (GbDepartmentEntity sub : subDepList) {
                if (father.getGbDepartmentId().equals(sub.getGbDepartmentFatherId())) {
                    subDeps.add(sub);
                    List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = sub.getGbDepartmentOrdersEntities();
                    int size = gbDepartmentOrdersEntities.size();
                    orderTotal = orderTotal + size;
                }

            }
            fatherMap.put("subDeps", subDeps);
            fatherMap.put("orderTotal", orderTotal);
            fatherMap.put("choiceTotal", orderTotal);
            dataMap.add(fatherMap);
        }
        map.put("arr", dataMap);
        return map;
    }

//
//    @RequestMapping(value = "/stockRoomSaveOrdersGb", method = RequestMethod.POST)
//    @ResponseBody
//    public R stockRoomSaveOrdersGb(@RequestBody GbDepartmentOrdersEntity gbDepartmentOrders) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
//        map.put("equalStatus", 0);
//        map.put("purDepId", gbDepartmentOrders.getGbDoDepartmentFatherId());
//        map.put("standard", gbDepartmentOrders.getGbDoStandard());
//        List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
//
//        if (goodsEntities.size() < 1) {
//            //是个新采购商品
//            GbDistributerPurchaseGoodsEntity disGoods = new GbDistributerPurchaseGoodsEntity();
//            disGoods.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
//            disGoods.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
//            disGoods.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
//            disGoods.setGbDpgApplyDate(formatWhatDay(0));
//            disGoods.setGbDpgStatus(0);
//            disGoods.setGbDpgOrdersAmount(1);
//            disGoods.setGbDpgOrdersFinishAmount(0);
//
////            Integer gbDoDisGoodsId = gbDepartmentOrders.getGbDoDisGoodsId();
////            GbDistributerGoodsEntity distributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
////            String standardname = distributerGoodsEntity.getGbDgGoodsStandardname();
////            if(standardname.equals(gbDepartmentOrders.getGbDoStandard())){
////                disGoods.setGbDpgBuyQuantity(gbDepartmentOrders.getGbDoQuantity());
////            }
////            if(gbDepartmentOrders.getGbDoPrice() != null){
////                BigDecimal priceB = new BigDecimal(gbDepartmentOrders.getGbDoPrice());
////                BigDecimal priceQ = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
////                BigDecimal decimal = priceB.multiply(priceQ).setScale(1, BigDecimal.ROUND_HALF_UP);
////                disGoods.setGbDpgBuyPrice(gbDepartmentOrders.getGbDoPrice());
////                disGoods.setGbDpgBuySubtotal(decimal.toString());
////            }
//            disGoods.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
//            disGoods.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
//            disGoods.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
//            disGoods.setGbDpgPurchaseDepartmentId(gbDepartmentOrders.getGbDoDepartmentFatherId());
//            disGoods.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
//            disGoods.setGbDpgPurchaseType(getGbOrderTypeZiCai());
//            gbDistributerPurchaseGoodsService.save(disGoods);
//            Integer gbDistributerPurchaseGoodsId = disGoods.getGbDistributerPurchaseGoodsId();
//            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
//        } else {
//            //给老采购商品添加新订单
//            GbDistributerPurchaseGoodsEntity gbDisPurGoodsEntity = goodsEntities.get(0);
//            Integer gbDistributerPurchaseGoodsId = gbDisPurGoodsEntity.getGbDistributerPurchaseGoodsId();
//            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
//            //采购商品订单数量更新
//            Integer gbDpgOrdersAmount = gbDisPurGoodsEntity.getGbDpgOrdersAmount();
//            gbDisPurGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
//            BigDecimal purQuantity = new BigDecimal(gbDisPurGoodsEntity.getGbDpgQuantity());
//            BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
//            BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
//            gbDisPurGoodsEntity.setGbDpgQuantity(add.toString());
//            gbDistributerPurchaseGoodsService.update(gbDisPurGoodsEntity);
//        }
//
//        gbDepartmentOrdersService.save(gbDepartmentOrders);
//
//        Integer gbDoDisGoodsId = gbDepartmentOrders.getGbDoDisGoodsId();
//        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
//
//        if (gbDistributerGoodsEntity.getGbDgGbSupplierId() != null) {
//            Map<String, Object> mapData = autoAddPurchaseBatch(gbDepartmentOrders, gbDistributerGoodsEntity);
//            Integer purUserId = (Integer) mapData.get("purUserId");
//            gbDepartmentOrders.setGbDoPurchaseUserId(purUserId);
//            gbDepartmentOrders.setGbDoBuyStatus(getGbOrderBuyStatusProcurement());
//            gbDepartmentOrdersService.update(gbDepartmentOrders);
//        }
//        return R.ok().put("data", gbDepartmentOrdersService.queryObject(gbDepartmentOrders.getGbDepartmentOrdersId()));
//    }

    /**
     * ORDER,DISTRIBUTER
     * 添加订货申请
     *
     * @param gbDepartmentOrders 订货申请
     * @return ok
     */
    @ResponseBody
    @RequestMapping("/saveOrdersGb")
    public R saveOrdersGb(@RequestBody GbDepartmentOrdersEntity gbDepartmentOrders) {

        // add purchaseGoods
        Integer gbDoDisGoodsId = gbDepartmentOrders.getGbDoDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        Integer gbDoOrderType = gbDepartmentOrders.getGbDoOrderType();
        gbDepartmentOrders.setGbDoPurchaseGoodsId(-1);
        GbDistributerPurchaseGoodsEntity gbPurchaseGoodsEntity =  new GbDistributerPurchaseGoodsEntity();
        if (gbDoOrderType.equals(getGbOrderTypeJiCai())
                || gbDoOrderType.equals(getGbOrderTypeZiCai())
                || gbDoOrderType.equals(getGbOrderTypeChuKuCaiGou())
                || gbDoOrderType.equals(getGbOrderTypeAppSupplier())) {

            //查询是否有采购的同一个商品
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
            if (gbDistributerGoodsEntity.getGbDgGbSupplierId() != null) {
                map.put("status", 2); //有供货商的商品
            } else {
                map.put("equalStatus", 0); //没有供货商的商品
            }
            map.put("purDepId", gbDepartmentOrders.getGbDoToDepartmentId());
            map.put("standard", gbDepartmentOrders.getGbDoStandard());
            List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);

            if (goodsEntities.size() == 0) {
                //是个新采购商品
//                GbDistributerPurchaseGoodsEntity disGoods = new GbDistributerPurchaseGoodsEntity();
                gbPurchaseGoodsEntity.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
                gbPurchaseGoodsEntity.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
                gbPurchaseGoodsEntity.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
                gbPurchaseGoodsEntity.setGbDpgApplyDate(formatWhatDay(0));
                gbPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
                gbPurchaseGoodsEntity.setGbDpgOrdersAmount(1);
                gbPurchaseGoodsEntity.setGbDpgOrdersFinishAmount(0);
                gbPurchaseGoodsEntity.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
                gbPurchaseGoodsEntity.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
                gbPurchaseGoodsEntity.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
                gbPurchaseGoodsEntity.setGbDpgPurchaseDepartmentId(gbDepartmentOrders.getGbDoToDepartmentId());
                gbPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
                gbPurchaseGoodsEntity.setGbDpgPurchaseType(5);

                System.out.println("nenenenneennenene" + gbPurchaseGoodsEntity.getGbDpgQuantity());
                gbDistributerPurchaseGoodsService.save(gbPurchaseGoodsEntity);
                Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
                gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
            } else {
                //给老采购商品添加新订单
                gbPurchaseGoodsEntity = goodsEntities.get(0);
                Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
                gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                //采购商品订单数量更新
                Integer gbDpgOrdersAmount = gbPurchaseGoodsEntity.getGbDpgOrdersAmount();
                gbPurchaseGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
                BigDecimal purQuantity = new BigDecimal(gbPurchaseGoodsEntity.getGbDpgQuantity());
                BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
                BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                gbPurchaseGoodsEntity.setGbDpgQuantity(add.toString());
                gbDistributerPurchaseGoodsService.update(gbPurchaseGoodsEntity);
            }
        }

        //判断是否是配送商，自动生成配送供货商NxDistributer一个订单
//        if (gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId() != -1) {
        if (gbDoOrderType.equals(getGbOrderTypeAppSupplier())) {
            Integer nxDoDistributerId = gbDepartmentOrders.getGbDoNxDistributerId();
            Integer gbDoNxDistributerGoodsId = gbDepartmentOrders.getGbDoNxDistributerGoodsId();
            NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(gbDoNxDistributerGoodsId);
            String gbDoQuantity = gbDepartmentOrders.getGbDoQuantity();
            String gbDoStandard = gbDepartmentOrders.getGbDoStandard();
            String gbDoRemark = gbDepartmentOrders.getGbDoRemark();
            gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
            gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
            gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
            gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDayTime(0));
            gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));
            String gbDoApplyArriveDate = gbDepartmentOrders.getGbDoApplyArriveDate();
            Integer gbDoDepartmentId = gbDepartmentOrders.getGbDoDepartmentId();
            Integer gbDoDistributerId = gbDepartmentOrders.getGbDoDistributerId();
            Integer gbDoDepartmentFatherId = gbDepartmentOrders.getGbDoDepartmentFatherId();
            Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
            Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
            //
            NxDepartmentOrdersEntity ordersEntity = new NxDepartmentOrdersEntity();
            ordersEntity.setNxDoDistributerId(nxDoDistributerId);
            ordersEntity.setNxDoDisGoodsId(gbDoNxDistributerGoodsId);
            ordersEntity.setNxDoQuantity(gbDoQuantity);
            ordersEntity.setNxDoStandard(gbDoStandard);
            ordersEntity.setNxDoRemark(gbDoRemark);
            ordersEntity.setNxDoApplyDate(formatWhatDay(0));
            ordersEntity.setNxDoArriveOnlyDate(formatWhatDay(0));
            ordersEntity.setNxDoArriveWeeksYear(getWeekOfYear(0));
            ordersEntity.setNxDoApplyFullTime(formatWhatYearDayTime(0));
            ordersEntity.setNxDoApplyOnlyTime(formatWhatTime(0));
            ordersEntity.setNxDoArriveDate(gbDoApplyArriveDate);
            ordersEntity.setNxDoGbDistributerId(gbDoDistributerId);
            ordersEntity.setNxDoGbDepartmentId(gbDoDepartmentId);
            ordersEntity.setNxDoGbDepartmentFatherId(gbDoDepartmentFatherId);
            ordersEntity.setNxDoDepartmentId(-1);
            ordersEntity.setNxDoDepartmentFatherId(-1);
            ordersEntity.setNxDoNxCommunityId(-1);
            ordersEntity.setNxDoNxCommRestrauntFatherId(-1);
            ordersEntity.setNxDoNxCommRestrauntId(-1);
            ordersEntity.setNxDoNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
            ordersEntity.setNxDoNxGoodsFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
            ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
            ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
            ordersEntity.setNxDoDepDisGoodsId(-1);
            ordersEntity.setNxDoArriveWhatDay(getWeek(0));
            ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());

            ordersEntity.setNxDoGoodsType(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
            ordersEntity.setNxDoIsAgent(-1);
            ordersEntity.setNxDoPrintStandard(nxDistributerGoodsEntity.getNxDgGoodsStandardname());

            if (nxDistributerGoodsEntity.getNxDgPurchaseAuto() == -1) {
                ordersEntity.setNxDoPurchaseGoodsId(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
            } else {
                savePurGoodsAuto(ordersEntity);
            }


            if (ordersEntity.getNxDoStandard().equals(nxDistributerGoodsEntity.getNxDgGoodsStandardname())) {
                ordersEntity.setNxDoWeight(gbDepartmentOrders.getGbDoQuantity());
                gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                BigDecimal orderWeight = new BigDecimal(gbDepartmentOrders.getGbDoWeight());
                BigDecimal willPrice = new BigDecimal(0);
                BigDecimal buyingPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgBuyingPrice());
                String buyingPriceLevel = "0";
                String update = nxDistributerGoodsEntity.getNxDgBuyingPriceUpdate();
                if (nxDistributerGoodsEntity.getNxDgWillPriceOneWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOneWeight()).compareTo(BigDecimal.ZERO) == 1) {
                    BigDecimal nxOneWeight = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOneWeight());
                    if (orderWeight.compareTo(nxOneWeight) < 1) {
                        willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                        buyingPriceLevel = "1";
                    } else {
                        if (nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight()).compareTo(BigDecimal.ZERO) == 1) {
                            BigDecimal nxTwoWeight = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight());
                            if (orderWeight.compareTo(nxTwoWeight) < 1) {
                                willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                                buyingPriceLevel = "2";
                            } else {
                                if (nxDistributerGoodsEntity.getNxDgWillPriceThreeWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceThreeWeight()).compareTo(BigDecimal.ZERO) == 1){
                                    willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceThree());
                                    buyingPriceLevel = "3";
                                }else{
                                    willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                                    buyingPriceLevel = "2";
                                }
                            }
                        }else{
                            willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                            buyingPriceLevel = "1";
                        }

                    }
                }


                BigDecimal  profitB = willPrice.subtract(buyingPrice).setScale(1,BigDecimal.ROUND_HALF_UP);
                gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                BigDecimal costSubtotalB = buyingPrice.multiply(new BigDecimal(gbDepartmentOrders.getGbDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profitSubtotal = profitB.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal orderSubtotal = willPrice.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoCostSubtotal(costSubtotalB.toString());
                ordersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());
                ordersEntity.setNxDoSubtotal(orderSubtotal.toString());
                ordersEntity.setNxDoCostPriceLevel(buyingPriceLevel);
                ordersEntity.setNxDoCostPrice(buyingPrice.toString());
                ordersEntity.setNxDoCostPriceUpdate(update);

                ordersEntity.setNxDoPrice(willPrice.toString());
                gbDepartmentOrders.setGbDoPrice(willPrice.toString());

                //profit
                BigDecimal scaleB = profitB.divide(willPrice, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                ordersEntity.setNxDoProfitScale(scaleB.toString());

                //updateGbPurGoods
                gbPurchaseGoodsEntity.setGbDpgBuyPrice(willPrice.toString());
                gbPurchaseGoodsEntity.setGbDpgBuyQuantity(gbPurchaseGoodsEntity.getGbDpgQuantity());
                if(gbDepartmentOrders.getGbDoStandard().equals(gbDistributerGoodsEntity.getGbDgGoodsStandardname())){
                    BigDecimal quantity = new BigDecimal(gbPurchaseGoodsEntity.getGbDpgQuantity());
                    BigDecimal subtotal = quantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    gbPurchaseGoodsEntity.setGbDpgBuySubtotal(subtotal.toString());
                }
                gbDistributerPurchaseGoodsService.update(gbPurchaseGoodsEntity);

                BigDecimal decimal = willPrice.multiply(new BigDecimal(gbPurchaseGoodsEntity.getGbDpgBuyQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
                gbPurchaseGoodsEntity.setGbDpgBuySubtotal(decimal.toString());


            } else {
                ordersEntity.setNxDoCostSubtotal("0");
                ordersEntity.setNxDoProfitSubtotal("0");
            }

            ordersEntity.setNxDoStatus(getNxOrderStatusNew());
            nxDepartmentOrdersService.saveForGb(ordersEntity);
            Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
            gbDepartmentOrders.setGbDoNxDepartmentOrderId(nxDepartmentOrdersId);
        }

        gbDepartmentOrdersService.save(gbDepartmentOrders);

        //给NxDepartorder更新gbDepartmentOrderid
        if (gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId() != -1) {
            Integer gbDoNxDepartmentOrderId = gbDepartmentOrders.getGbDoNxDepartmentOrderId();
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
            ordersEntity.setNxDoGbDepartmentOrderId(gbDepartmentOrders.getGbDepartmentOrdersId());
            ordersEntity.setNxDoNxRestrauntOrderId(-1);
            nxDepartmentOrdersService.update(ordersEntity);
        }

        //给autoBatch更新gbDepartmentOrderid
        if (gbDistributerGoodsEntity.getGbDgGbSupplierId() != null) {
            Map<String, Object> mapData = autoAddPurchaseBatch(gbDepartmentOrders, gbDistributerGoodsEntity);
            Integer purUserId = (Integer) mapData.get("purUserId");
            gbDepartmentOrders.setGbDoPurchaseUserId(purUserId);
            gbDepartmentOrders.setGbDoBuyStatus(getGbOrderBuyStatusProcurement());
            gbDepartmentOrdersService.update(gbDepartmentOrders);
        }
        return R.ok().put("data", gbDepartmentOrdersService.queryObject(gbDepartmentOrders.getGbDepartmentOrdersId()));
    }

    private void savePurGoodsAuto(NxDepartmentOrdersEntity ordersEntity) {


        Integer nxDistributerPurchaseGoodsId = 0;
        //判断是否有已经分的

        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
        NxDistributerGoodsEntity disGoods = nxDistributerGoodsService.queryObject(doDisGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", doDisGoodsId);
        map.put("status", 2);
        System.out.println("purgogogo" + map);
        NxDistributerPurchaseGoodsEntity havePurGoods = nxDistributerPurchaseGoodsService.queryIfHavePurGoods(map);
        if (havePurGoods != null) {
            havePurGoods.setNxDpgOrdersAmount(havePurGoods.getNxDpgOrdersAmount() + 1);
            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                if (havePurGoods.getNxDpgBuyQuantity() != null) {
                    BigDecimal decimal = new BigDecimal(havePurGoods.getNxDpgQuantity());
                    BigDecimal decimal1 = new BigDecimal(ordersEntity.getNxDoQuantity());
                    BigDecimal totaoWeight = decimal.add(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(havePurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    havePurGoods.setNxDpgQuantity(totaoWeight.toString());
                    havePurGoods.setNxDpgBuyQuantity(totaoWeight.toString());
                    havePurGoods.setNxDpgBuySubtotal(decimal2.toString());
                }
            }

            nxDistributerPurchaseGoodsService.update(havePurGoods);
            nxDistributerPurchaseGoodsId = havePurGoods.getNxDistributerPurchaseGoodsId();

        } else {

            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = new NxDistributerPurchaseGoodsEntity();
            purchaseGoodsEntity.setNxDpgDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
            purchaseGoodsEntity.setNxDpgDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
            purchaseGoodsEntity.setNxDpgDistributerId(disGoods.getNxDgDistributerId());
            purchaseGoodsEntity.setNxDpgApplyDate(formatWhatDay(0));
            purchaseGoodsEntity.setNxDpgCostLevel(disGoods.getNxDgBuyingPriceIsGrade());
            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
            purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
            purchaseGoodsEntity.setNxDpgOrdersAmount(1);
            purchaseGoodsEntity.setNxDpgFinishAmount(0);
            purchaseGoodsEntity.setNxDpgPurchaseType(1);
            purchaseGoodsEntity.setNxDpgExpectPrice(disGoods.getNxDgBuyingPrice());
            purchaseGoodsEntity.setNxDpgBuyPrice(disGoods.getNxDgBuyingPrice());
            purchaseGoodsEntity.setNxDpgDisGoodsId(doDisGoodsId);
            purchaseGoodsEntity.setNxDpgInputType(disGoods.getNxDgPurchaseAuto());
            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                purchaseGoodsEntity.setNxDpgQuantity(ordersEntity.getNxDoQuantity());
                BigDecimal totaoWeight = new BigDecimal(ordersEntity.getNxDoQuantity());
                purchaseGoodsEntity.setNxDpgStandard(ordersEntity.getNxDoStandard());
                BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(purchaseGoodsEntity.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                purchaseGoodsEntity.setNxDpgQuantity(totaoWeight.toString());
                purchaseGoodsEntity.setNxDpgBuyQuantity(totaoWeight.toString());
                purchaseGoodsEntity.setNxDpgBuySubtotal(decimal2.toString());
            }
            nxDistributerPurchaseGoodsService.save(purchaseGoodsEntity);
            nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();

            //给autoBatch更新gbDepartmentOrderid
            if (disGoods.getNxDgSupplierId() != null) {
                //
                Map<String, Object> mapBatch = new HashMap<>();
                Integer gbDgGbSupplierId = disGoods.getNxDgSupplierId();
                mapBatch.put("supplierId", gbDgGbSupplierId);
                mapBatch.put("status", 1);
                mapBatch.put("purchaseType", 2);
                List<NxDistributerPurchaseBatchEntity> entities = nxDPBService.queryDisPurchaseBatch(mapBatch);

                if (entities.size() == 0) {
                    //
                    NxDistributerPurchaseBatchEntity batchEntity = new NxDistributerPurchaseBatchEntity();
                    batchEntity.setNxDpbDate(formatWhatDay(0));
                    batchEntity.setNxDpbTime(formatWhatTime(0));
                    batchEntity.setNxDpbMonth(formatWhatMonth(0));
                    batchEntity.setNxDpbPruchaseWeek(getWeek(0));
                    batchEntity.setNxDpbYear(formatWhatYear(0));
                    batchEntity.setNxDpbPayFullTime(formatWhatYearDayTime(0));
                    batchEntity.setNxDpbStatus(-1);
                    batchEntity.setNxDpbPurchaseType(2);
                    batchEntity.setNxDpbSupplierId(gbDgGbSupplierId);
                    NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDgGbSupplierId);
                    batchEntity.setNxDpbSellUserId(supplierEntity.getNxJrdhsUserId());
                    batchEntity.setNxDpbDistributerId(ordersEntity.getNxDoDistributerId());
                    batchEntity.setNxDpbPurUserId(supplierEntity.getNxJrdhsNxPurUserId());
                    batchEntity.setNxDpbBuyUserId(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                    NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                    batchEntity.setNxDpbBuyUserOpenId(nxJrdhUserEntity.getNxJrdhWxOpenId());
                    nxDPBService.save(batchEntity);

                    purchaseGoodsEntity.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                    purchaseGoodsEntity.setNxDpgStatus(getGbPurchaseGoodsStatusProcurement());
                    purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
                    purchaseGoodsEntity.setNxDpgTime(formatWhatYearDayTime(0));
                    purchaseGoodsEntity.setNxDpgDistributerId(batchEntity.getNxDpbDistributerId());
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                } else {
                    NxDistributerPurchaseBatchEntity batchEntity = entities.get(0);
                    purchaseGoodsEntity.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                    purchaseGoodsEntity.setNxDpgStatus(getGbPurchaseGoodsStatusProcurement());
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                }
                ordersEntity.setNxDoPurchaseStatus(2);
            }

        }
        ordersEntity.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
        nxDepartmentOrdersService.update(ordersEntity);


    }
    private void savePurGoodsAuto1(NxDepartmentOrdersEntity ordersEntity) {

        Integer nxDistributerPurchaseGoodsId = 0;
        //判断是否有已经分的
        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
        NxDistributerGoodsEntity disGoods = nxDistributerGoodsService.queryObject(doDisGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", doDisGoodsId);
        map.put("equalStatus", 0);
        NxDistributerPurchaseGoodsEntity havePurGoods = nxDistributerPurchaseGoodsService.queryIfHavePurGoods(map);
        if (havePurGoods != null) {
            havePurGoods.setNxDpgOrdersAmount(havePurGoods.getNxDpgOrdersAmount() + 1);
            nxDistributerPurchaseGoodsService.update(havePurGoods);
            nxDistributerPurchaseGoodsId = havePurGoods.getNxDistributerPurchaseGoodsId();

        } else {

            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = new NxDistributerPurchaseGoodsEntity();
            purchaseGoodsEntity.setNxDpgDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
            purchaseGoodsEntity.setNxDpgDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
            purchaseGoodsEntity.setNxDpgDistributerId(disGoods.getNxDgDistributerId());
            purchaseGoodsEntity.setNxDpgApplyDate(formatWhatDay(0));
            purchaseGoodsEntity.setNxDpgCostLevel(disGoods.getNxDgBuyingPriceIsGrade());
            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
            purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
            purchaseGoodsEntity.setNxDpgOrdersAmount(1);
            purchaseGoodsEntity.setNxDpgFinishAmount(0);
            purchaseGoodsEntity.setNxDpgPurchaseType(1);
            purchaseGoodsEntity.setNxDpgExpectPrice(disGoods.getNxDgBuyingPrice());
            purchaseGoodsEntity.setNxDpgBuyPrice(disGoods.getNxDgBuyingPrice());
            purchaseGoodsEntity.setNxDpgDisGoodsId(doDisGoodsId);
            purchaseGoodsEntity.setNxDpgInputType(disGoods.getNxDgPurchaseAuto());
            nxDistributerPurchaseGoodsService.save(purchaseGoodsEntity);
            nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();
        }
        ordersEntity.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
        nxDepartmentOrdersService.update(ordersEntity);

    }

    private Map<String, Object> autoAddPurchaseBatch(GbDepartmentOrdersEntity ordersEntity, GbDistributerGoodsEntity goodsEntity) {
        Map<String, Object> mapData = new HashMap<>();
        //
        System.out.println("autotoototoba");
        Integer gbDgGbSupplierId = goodsEntity.getGbDgGbSupplierId();
        NxJrdhSupplierEntity nxJrdhSupplierEntity = jrdhSupplierService.queryObject(gbDgGbSupplierId);
        Integer nxJrdhsUserId = nxJrdhSupplierEntity.getNxJrdhsUserId();
        Integer nxJrdhsGbPurUserId = nxJrdhSupplierEntity.getNxJrdhsGbPurUserId();
        Integer nxJrdhsGbDepartmentId = nxJrdhSupplierEntity.getNxJrdhsGbDepartmentId();
        Map<String, Object> map = new HashMap<>();
        mapData.put("purUserId", nxJrdhsGbPurUserId);

        map.put("supplierId", gbDgGbSupplierId);
        map.put("status", 1);
        List<GbDistributerPurchaseBatchEntity> entities = gbDPBService.queryDisPurchaseBatch(map);

        if (entities.size() == 0) {
            //
            GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(nxJrdhsGbPurUserId);
            Integer gbDuAdmin = gbDepartmentUserEntity.getGbDuAdmin();

            GbDistributerPurchaseBatchEntity batchEntity = new GbDistributerPurchaseBatchEntity();
            batchEntity.setGbDpbDate(formatWhatDay(0));
            batchEntity.setGbDpbHour(formatWhatHour(0));
            batchEntity.setGbDpbMinute(formatWhatMinute(0));
            batchEntity.setGbDpbTime(formatWhatTime(0));
            batchEntity.setGbDpbPurchaseMonth(formatWhatMonth(0));
            batchEntity.setGbDpbPurchaseWeek(getWeek(0));
            batchEntity.setGbDpbPurchaseYear(formatWhatYear(0));
            batchEntity.setGbDpbPurchaseFullTime(formatWhatYearDayTime(0));
            batchEntity.setGbDpbStatus(-1);
            batchEntity.setGbDpbSupplierUserId(nxJrdhsUserId);
            batchEntity.setGbDpbPurUserId(nxJrdhsGbPurUserId);
            batchEntity.setGbDpbDistributerId(ordersEntity.getGbDoDistributerId());
            batchEntity.setGbDpbPurDepartmentId(nxJrdhsGbDepartmentId);
            batchEntity.setGbDpbUserAdminType(gbDuAdmin);
            batchEntity.setGbDpbSupplierId(gbDgGbSupplierId);
            batchEntity.setGbDpbSellUserId(nxJrdhsUserId);
            batchEntity.setGbDpbBuyUserId(nxJrdhSupplierEntity.getNxJrdhsNxJrdhBuyUserId());

            gbDPBService.save(batchEntity);

            Integer gbDoPurchaseGoodsId = ordersEntity.getGbDoPurchaseGoodsId();
            GbDistributerPurchaseGoodsEntity gbDistributerPurchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
            gbDistributerPurchaseGoodsEntity.setGbDpgBatchId(batchEntity.getGbDistributerPurchaseBatchId());
            gbDistributerPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseDate(formatWhatDay(0));
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseMonth(formatWhatMonth(0));
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseYear(formatWhatYear(0));
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseWeek(getWeek(0));
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseDepartmentId(batchEntity.getGbDpbPurDepartmentId());
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(batchEntity.getGbDpbDistributerId());
            gbDistributerPurchaseGoodsEntity.setGbDpgTime(formatWhatTime(0));
            gbDistributerPurchaseGoodsEntity.setGbDpgPurUserId(nxJrdhsGbPurUserId);
            gbDistributerPurchaseGoodsService.update(gbDistributerPurchaseGoodsEntity);
            mapData.put("batchId", gbDistributerPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId());
            return mapData;
        } else {
            GbDistributerPurchaseBatchEntity batchEntity = entities.get(0);
            Integer gbDoPurchaseGoodsId = ordersEntity.getGbDoPurchaseGoodsId();
            GbDistributerPurchaseGoodsEntity gbDistributerPurchaseGoodsEntity1 = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
            gbDistributerPurchaseGoodsEntity1.setGbDpgBatchId(batchEntity.getGbDistributerPurchaseBatchId());
            gbDistributerPurchaseGoodsService.update(gbDistributerPurchaseGoodsEntity1);
            mapData.put("batchId", batchEntity.getGbDistributerPurchaseBatchId());
            return mapData;
        }

    }

    @RequestMapping(value = "/receiveReturnApply", method = RequestMethod.POST)
    @ResponseBody
    public R receiveReturnApply(@RequestBody GbDepartmentOrdersEntity order) {
        System.out.println("orderst" + order.getGbDoStatus());
        //
        if (order.getGbDoStatus().equals(getGbOrderStatusReceived())) {
            GbDepartmentBillEntity billEntity = new GbDepartmentBillEntity();
            String areaCode = "1" + order.getGbDoDistributerId();
            billEntity.setGbDbStatus(0);
            billEntity.setGbDbDisId(order.getGbDoDistributerId());
            billEntity.setGbDbDepId(order.getGbDoDepartmentId());
            billEntity.setGbDbDate(formatWhatDay(0));
            billEntity.setGbDbTime(formatWhatYearDayTime(0));
            billEntity.setGbDbMonth(formatWhatMonth(0));
            billEntity.setGbDbWeek(getWeek(0));
            billEntity.setGbDbTradeNo(generateBillTradeNo(areaCode));
            billEntity.setGbDbIssueDepId(order.getGbDoToDepartmentId());
            billEntity.setGbDbIssueOrderType(getGbOrderTypeTuihuo());
            billEntity.setGbDbIssueUserId(order.getGbDoReturnUserId());
            billEntity.setGbDbPrintTimes(0);
            billEntity.setGbDbTotal("-" + order.getGbDoSubtotal());
            billEntity.setGbDbSellingTotal(order.getGbDoSellingSubtotal());
            billEntity.setGbDbOrderAmount(1);
            gbDepartmentBillService.save(billEntity);

            order.setGbDoBillId(billEntity.getGbDepartmentBillId());
            order.setGbDoStatus(getGbOrderStatusReceived());

            Integer gbDoDgsrReturnId = order.getGbDoDgsrReturnId();
            GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepGoodsStockReduceService.queryObject(gbDoDgsrReturnId);
            reduceEntity.setGbDgsrStatus(0);
            gbDepGoodsStockReduceService.update(reduceEntity);

            order.setGbDoArriveWeeksYear(getWeekOfYear(0));
            order.setGbDoArriveWhatDay(getWeek(0));
            order.setGbDoArriveOnlyDate(formatWhatDate(0));
            order.setGbDoArriveDate(formatWhatDay(0));
            gbDepartmentOrdersService.update(order);

            //如果是库存商品退货，还要update退货时候添加到退货部门的stock
            GbDepartmentGoodsStockEntity stockEntity = gbDepartmentGoodsStockService.queryReturnStockItemByOrderId(order.getGbDepartmentOrdersId());
            System.out.println("stockckckkde" + stockEntity);
            if (stockEntity != null) {
                stockEntity.setGbDgsStatus(0);
                System.out.println("stoenweieie" + stockEntity.getGbDgsWeight());
                System.out.println("stoenweieie" + stockEntity.getGbDepartmentGoodsStockId());
                stockEntity.setGbDgsRestWeight(stockEntity.getGbDgsWeight());
                stockEntity.setGbDgsRestSubtotal(stockEntity.getGbDgsSubtotal());
                gbDepartmentGoodsStockService.update(stockEntity);


                Integer gbDgsrGbGoodsStockId = reduceEntity.getGbDgsrGbGoodsStockId();
                GbDepartmentGoodsStockEntity orignalStock = gbDepartmentGoodsStockService.queryObject(gbDgsrGbGoodsStockId);
                Integer gbDgsGbGoodsStockId = orignalStock.getGbDgsGbGoodsStockId();
                GbDepartmentGoodsStockEntity fromStock = gbDepartmentGoodsStockService.queryObject(gbDgsGbGoodsStockId);
                System.out.println("formssotokkdkdk" + fromStock.getGbDepartmentGoodsStockId());
                System.out.println("formssotokkdkdk" + fromStock.getGbDgsGbDepDisGoodsId());
                updateDepDisGoods(stockEntity, fromStock.getGbDgsGbDepDisGoodsId(), "add");

                updateDepGoodsDailyBusiness(stockEntity);
            }
        } else {
            gbDepartmentOrdersService.update(order);
        }
        return R.ok();
    }


    @RequestMapping(value = "/receiveReturnApplyNx", method = RequestMethod.POST)
    @ResponseBody
    public R receiveReturnApplyNx(@RequestBody NxDepartmentOrdersEntity nxOrder) {
        Integer gbDepartmentOrderId = nxOrder.getNxDoGbDepartmentOrderId();
        GbDepartmentOrdersEntity order = gbDepartmentOrdersService.queryObject(gbDepartmentOrderId);

        GbDepartmentBillEntity billEntity = new GbDepartmentBillEntity();
        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", order.getGbDoDepartmentId());
        map3.put("date", formatWhatDay(0));
        int count = gbDepartmentBillService.queryDepartmentBillCount(map3);
        int trade = count + 1;
        String no = "";
        if (trade < 100) {
            if (count < 10) {
                no = "00" + trade;
            } else {
                no = "0" + trade;
            }
        } else {
            no = String.valueOf(count);
        }
        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(order.getGbDoDepartmentId());
        String headPinyin = getHeadStringByString(gbDepartmentEntity.getGbDepartmentName(), true, null);
        String s = formatDayNumber(0) + headPinyin + no;
        billEntity.setGbDbStatus(0);
        billEntity.setGbDbDisId(order.getGbDoDistributerId());
        billEntity.setGbDbDepId(order.getGbDoDepartmentId());
        billEntity.setGbDbDate(formatWhatDay(0));
        billEntity.setGbDbTime(formatWhatYearDayTime(0));
        billEntity.setGbDbMonth(formatWhatMonth(0));
        billEntity.setGbDbWeek(getWeek(0));
        billEntity.setGbDbTradeNo(generateBillTradeNo(s));
        billEntity.setGbDbIssueDepId(order.getGbDoToDepartmentId());
        billEntity.setGbDbIssueOrderType(getGbOrderTypeTuihuo());
        billEntity.setGbDbIssueUserId(order.getGbDoReturnUserId());
        billEntity.setGbDbPrintTimes(0);
        billEntity.setGbDbTotal(order.getGbDoSubtotal());
        billEntity.setGbDbIssueNxDisId(order.getGbDoNxDistributerId());
        billEntity.setGbDbOrderAmount(1);
        gbDepartmentBillService.save(billEntity);

        order.setGbDoBillId(billEntity.getGbDepartmentBillId());
        order.setGbDoStatus(getGbOrderStatusReceived());
        order.setGbDoArriveWeeksYear(getWeekOfYear(0));
        order.setGbDoArriveWhatDay(getWeek(0));
        order.setGbDoArriveOnlyDate(formatWhatDate(0));
        order.setGbDoArriveDate(formatWhatDay(0));
        gbDepartmentOrdersService.update(order);


        //
        NxDepartmentBillEntity nxDepartmentBill = new NxDepartmentBillEntity();
        String areaCode = "1" + order.getGbDoNxDistributerId();
        String ss = generateBillTradeNo(areaCode);
        nxDepartmentBill.setNxDbTradeNo(ss);
        nxDepartmentBill.setNxDbDisId(order.getGbDoNxDistributerId());
        nxDepartmentBill.setNxDbDepId(-1);
        nxDepartmentBill.setNxDbTotal(order.getGbDoSubtotal());
        nxDepartmentBill.setNxDbPrintTimes(1);
        nxDepartmentBill.setNxDbGbDisId(order.getGbDoDistributerId());
        nxDepartmentBill.setNxDbGbDepId(order.getGbDoDepartmentId());
        nxDepartmentBill.setNxDbNxCommunityId(-1);
        nxDepartmentBill.setNxDbNxRestrauntId(-1);
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBillService.save(nxDepartmentBill);

        nxOrder.setNxDoStatus(3);
        nxOrder.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
        nxOrder.setNxDoReturnBillId(nxDepartmentBill.getNxDepartmentBillId());
        System.out.println("nxdretunntbiilllllssss" + nxOrder.getNxDoStatus());
        nxDepartmentOrdersService.update(nxOrder);


        Integer gbDoDgsrReturnId = order.getGbDoDgsrReturnId();
        GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepGoodsStockReduceService.queryObject(gbDoDgsrReturnId);
        reduceEntity.setGbDgsrStatus(0);
        gbDepGoodsStockReduceService.update(reduceEntity);

        return R.ok();
    }

    /**
     * ORDER
     * 修改申请
     *
     * @param gbDepartmentOrders 订货申请
     * @return ok
     */
    @ResponseBody
    @RequestMapping("/updateOrderGb")
    public R updateOrderGb(@RequestBody GbDepartmentOrdersEntity gbDepartmentOrders) {

        //检查修改规格
        Integer gbDepartmentOrdersId = gbDepartmentOrders.getGbDepartmentOrdersId();
        GbDepartmentOrdersEntity oldOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
        String oldStandard = oldOrdersEntity.getGbDoStandard();
        String gbDoStandard1 = gbDepartmentOrders.getGbDoStandard();

        Integer oldGbDoToDepartmentId = oldOrdersEntity.getGbDoToDepartmentId();
        Integer gbDoToDepartmentId = gbDepartmentOrders.getGbDoToDepartmentId();

        //修改订单采购的单位
        if (!oldGbDoToDepartmentId.equals(gbDoToDepartmentId)) {
            Integer gbDoDepartmentId = gbDepartmentOrders.getGbDoDepartmentId();

            //新修改为自采订单
            if (gbDoToDepartmentId.equals(gbDoDepartmentId)) {
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
                map.put("equalStatus", 0);
                map.put("purDepId", gbDepartmentOrders.getGbDoToDepartmentId());
                map.put("standard", gbDepartmentOrders.getGbDoStandard());
                List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
                if (goodsEntities.size() == 0) {
                    //是个新采购商品
                    GbDistributerPurchaseGoodsEntity newPurGoods = new GbDistributerPurchaseGoodsEntity();
                    newPurGoods.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
                    newPurGoods.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
                    newPurGoods.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
                    newPurGoods.setGbDpgApplyDate(formatWhatDay(0));
                    newPurGoods.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
                    newPurGoods.setGbDpgOrdersAmount(1);
                    newPurGoods.setGbDpgOrdersFinishAmount(0);
                    newPurGoods.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
                    newPurGoods.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
                    newPurGoods.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
                    newPurGoods.setGbDpgPurchaseDepartmentId(gbDepartmentOrders.getGbDoToDepartmentId());
                    newPurGoods.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
                    newPurGoods.setGbDpgPurchaseType(1);
                    newPurGoods.setGbDpgPurchaseWeek(getWeek(0));
                    newPurGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                    gbDistributerPurchaseGoodsService.save(newPurGoods);
                    Integer gbDistributerPurchaseGoodsId = newPurGoods.getGbDistributerPurchaseGoodsId();
                    gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                } else {
                    // 3， 给老采购商品添加新订单
                    GbDistributerPurchaseGoodsEntity gbDisPurGoodsEntity = goodsEntities.get(0);
                    Integer gbDistributerPurchaseGoodsId = gbDisPurGoodsEntity.getGbDistributerPurchaseGoodsId();
                    gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                    //采购商品订单数量更新
                    Integer gbDpgOrdersAmount = gbDisPurGoodsEntity.getGbDpgOrdersAmount();
                    gbDisPurGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
                    BigDecimal purQuantity = new BigDecimal(gbDisPurGoodsEntity.getGbDpgQuantity());
                    BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
                    BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                    gbDisPurGoodsEntity.setGbDpgQuantity(add.toString());
                    gbDistributerPurchaseGoodsService.update(gbDisPurGoodsEntity);
                }

            } else {
                //修改自采订单的purGoods
                Integer gbDoPurchaseGoodsId = gbDepartmentOrders.getGbDoPurchaseGoodsId();

                if (gbDoPurchaseGoodsId != -1) {
                    GbDistributerPurchaseGoodsEntity gbDistributerPurchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
                    Integer gbDpgOrdersAmount = gbDistributerPurchaseGoodsEntity.getGbDpgOrdersAmount();
                    if (gbDpgOrdersAmount > 1) {
                        gbDistributerPurchaseGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount - 1);
                        gbDistributerPurchaseGoodsService.update(gbDistributerPurchaseGoodsEntity);
                    } else {
                        //订货批次是否是最后一个采购商品
                        Integer gbDpgBatchId = gbDistributerPurchaseGoodsEntity.getGbDpgBatchId();
                        Map<String, Object> mapBatch = new HashMap<>();
                        mapBatch.put("batchId", gbDpgBatchId);
                        List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(mapBatch);
                        if (goodsEntities.size() == 1) {
                            gbDPBService.delete(gbDistributerPurchaseGoodsEntity.getGbDpgBatchId());
                        }
                        gbDistributerPurchaseGoodsService.delete(gbDoPurchaseGoodsId);
                    }
                }


                //将自采订单返回原采购部门订单
                //1 如果商品是集采商品，则添加 purGooods
                Integer gbDoDisGoodsId = gbDepartmentOrders.getGbDoDisGoodsId();
                GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
                if (gbDistributerGoodsEntity.getGbDgGoodsType().equals(getGbDisGoodsTypeJicai())) {
                    //查询 purGoods
                    Map<String, Object> map = new HashMap<>();
                    map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
                    map.put("equalStatus", 0);
                    map.put("purDepId", gbDepartmentOrders.getGbDoToDepartmentId());
                    map.put("standard", gbDepartmentOrders.getGbDoStandard());
                    List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
                    if (goodsEntities.size() == 0) {
                        //是个新采购商品
                        GbDistributerPurchaseGoodsEntity newPurGoods = new GbDistributerPurchaseGoodsEntity();
                        newPurGoods.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
                        newPurGoods.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
                        newPurGoods.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
                        newPurGoods.setGbDpgApplyDate(formatWhatDay(0));
                        newPurGoods.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
                        newPurGoods.setGbDpgOrdersAmount(1);
                        newPurGoods.setGbDpgOrdersFinishAmount(0);
                        newPurGoods.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
                        newPurGoods.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
                        newPurGoods.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
                        newPurGoods.setGbDpgPurchaseDepartmentId(gbDepartmentOrders.getGbDoToDepartmentId());
                        newPurGoods.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
                        newPurGoods.setGbDpgPurchaseType(1);
                        newPurGoods.setGbDpgPurchaseWeek(getWeek(0));
                        newPurGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                        gbDistributerPurchaseGoodsService.save(newPurGoods);
                        Integer gbDistributerPurchaseGoodsId = newPurGoods.getGbDistributerPurchaseGoodsId();
                        gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                    } else {
                        // 3， 给老采购商品添加新订单
                        GbDistributerPurchaseGoodsEntity gbDisPurGoodsEntity = goodsEntities.get(0);
                        Integer gbDistributerPurchaseGoodsId = gbDisPurGoodsEntity.getGbDistributerPurchaseGoodsId();
                        gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                        //采购商品订单数量更新
                        Integer gbDpgOrdersAmount = gbDisPurGoodsEntity.getGbDpgOrdersAmount();
                        gbDisPurGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
                        BigDecimal purQuantity = new BigDecimal(gbDisPurGoodsEntity.getGbDpgQuantity());
                        BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
                        BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                        gbDisPurGoodsEntity.setGbDpgQuantity(add.toString());
                        gbDistributerPurchaseGoodsService.update(gbDisPurGoodsEntity);
                    }

                }


            }


        }

        if (!oldStandard.equals(gbDoStandard1)) {

            // 1，修改原来的purGoods
            Integer gbDoPurchaseGoodsId = gbDepartmentOrders.getGbDoPurchaseGoodsId();
            if (gbDoPurchaseGoodsId != -1) {
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
                Integer oldOrdersAmount = purchaseGoodsEntity.getGbDpgOrdersAmount();
                if (oldOrdersAmount > 1) {
                    purchaseGoodsEntity.setGbDpgOrdersAmount(oldOrdersAmount - 1);
                    gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                } else {
                    gbDistributerPurchaseGoodsService.delete(gbDoPurchaseGoodsId);
                }
            }


            // 2，查询是否有采购的同一个商品
            //有采购商品
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
            map.put("equalStatus", 0);
            map.put("purDepId", gbDepartmentOrders.getGbDoToDepartmentId());
            map.put("standard", gbDepartmentOrders.getGbDoStandard());
            List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
            if (goodsEntities.size() == 0) {
                //是个新采购商品
                GbDistributerPurchaseGoodsEntity newPurGoods = new GbDistributerPurchaseGoodsEntity();
                newPurGoods.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
                newPurGoods.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
                newPurGoods.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
                newPurGoods.setGbDpgApplyDate(formatWhatDay(0));
                newPurGoods.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
                newPurGoods.setGbDpgOrdersAmount(1);
                newPurGoods.setGbDpgOrdersFinishAmount(0);
                newPurGoods.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
                newPurGoods.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
                newPurGoods.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
                newPurGoods.setGbDpgPurchaseDepartmentId(gbDepartmentOrders.getGbDoToDepartmentId());
                newPurGoods.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
                newPurGoods.setGbDpgPurchaseType(1);
                newPurGoods.setGbDpgPurchaseWeek(getWeek(0));
                newPurGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                gbDistributerPurchaseGoodsService.save(newPurGoods);
                Integer gbDistributerPurchaseGoodsId = newPurGoods.getGbDistributerPurchaseGoodsId();
                gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
            } else {
                // 3， 给老采购商品添加新订单
                GbDistributerPurchaseGoodsEntity gbDisPurGoodsEntity = goodsEntities.get(0);
                Integer gbDistributerPurchaseGoodsId = gbDisPurGoodsEntity.getGbDistributerPurchaseGoodsId();
                gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                //采购商品订单数量更新
                Integer gbDpgOrdersAmount = gbDisPurGoodsEntity.getGbDpgOrdersAmount();
                gbDisPurGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
                BigDecimal purQuantity = new BigDecimal(gbDisPurGoodsEntity.getGbDpgQuantity());
                BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
                BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                gbDisPurGoodsEntity.setGbDpgQuantity(add.toString());
                gbDistributerPurchaseGoodsService.update(gbDisPurGoodsEntity);
            }
        }

        if (gbDepartmentOrders.getGbDoNxDepartmentOrderId() != null) {
            Integer gbDoNxDepartmentOrderId = gbDepartmentOrders.getGbDoNxDepartmentOrderId();
            NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
            nxDepartmentOrdersEntity.setNxDoQuantity(gbDepartmentOrders.getGbDoQuantity());

            Integer gbDoNxDistributerGoodsId = gbDepartmentOrders.getGbDoNxDistributerGoodsId();
            NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(gbDoNxDistributerGoodsId);

            if (nxDepartmentOrdersEntity.getNxDoStandard().equals(nxDistributerGoodsEntity.getNxDgGoodsStandardname())) {
                nxDepartmentOrdersEntity.setNxDoWeight(gbDepartmentOrders.getGbDoQuantity());
                gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                BigDecimal orderWeight = new BigDecimal(gbDepartmentOrders.getGbDoWeight());
                BigDecimal willPrice = new BigDecimal(0);
                BigDecimal buyingPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgBuyingPrice());
                String buyingPriceLevel = "0";
                String update = nxDistributerGoodsEntity.getNxDgBuyingPriceUpdate();
                if (nxDistributerGoodsEntity.getNxDgWillPriceOneWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOneWeight()).compareTo(BigDecimal.ZERO) == 1) {
                    BigDecimal nxOneWeight = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOneWeight());
                    if (orderWeight.compareTo(nxOneWeight) < 1) {
                        willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                        buyingPriceLevel = "1";

                    } else {
                        if (nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight()).compareTo(BigDecimal.ZERO) == 1) {
                            BigDecimal nxTwoWeight = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight());
                            if (orderWeight.compareTo(nxTwoWeight) < 1) {
                                willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                                buyingPriceLevel = "2";
                            } else {
                                if (nxDistributerGoodsEntity.getNxDgWillPriceThreeWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceThreeWeight()).compareTo(BigDecimal.ZERO) == 1){
                                    willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceThree());
                                    buyingPriceLevel = "3";
                                }else{
                                    willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                                    buyingPriceLevel = "2";
                                }
                            }
                        }else{
                            willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                            buyingPriceLevel = "1";
                        }

                    }
                }


                nxDepartmentOrdersEntity.setNxDoCostPriceLevel(buyingPriceLevel);
                nxDepartmentOrdersEntity.setNxDoCostPrice(buyingPrice.toString());
                nxDepartmentOrdersEntity.setNxDoCostPriceUpdate(update);
                nxDepartmentOrdersEntity.setNxDoPrice(willPrice.toString());
                gbDepartmentOrders.setGbDoPrice(willPrice.toString());

                //profit
                BigDecimal profitB = willPrice.subtract(buyingPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profitB.divide(willPrice, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                nxDepartmentOrdersEntity.setNxDoProfitScale(scaleB.toString());

                if (nxDepartmentOrdersEntity.getNxDoStandard().equals(nxDistributerGoodsEntity.getNxDgGoodsStandardname())) {
                    nxDepartmentOrdersEntity.setNxDoWeight(gbDepartmentOrders.getGbDoQuantity());
                    gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                    BigDecimal costSubtotalB = buyingPrice.multiply(new BigDecimal(gbDepartmentOrders.getGbDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal profitSubtotal = profitB.multiply(new BigDecimal(nxDepartmentOrdersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal orderSubtotal = willPrice.multiply(new BigDecimal(nxDepartmentOrdersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    nxDepartmentOrdersEntity.setNxDoCostSubtotal(costSubtotalB.toString());
                    nxDepartmentOrdersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());
                    nxDepartmentOrdersEntity.setNxDoSubtotal(orderSubtotal.toString());
                } else {
                    nxDepartmentOrdersEntity.setNxDoCostSubtotal("0");
                    nxDepartmentOrdersEntity.setNxDoProfitSubtotal("0");
                }



            }
            nxDepartmentOrdersEntity.setNxDoRemark(gbDepartmentOrders.getGbDoRemark());
            nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);


        }

        gbDepartmentOrdersService.update(gbDepartmentOrders);
        return R.ok().put("data", gbDepartmentOrders);
    }

    @ResponseBody
    @RequestMapping("/justUpdateOrderContent")
    public R justUpdateOrderContent(@RequestBody GbDepartmentOrdersEntity gbDepartmentOrders) {

        gbDepartmentOrdersService.update(gbDepartmentOrders);
        return R.ok().put("data", gbDepartmentOrders);
    }

    @ResponseBody
    @RequestMapping("/outDepDelOrder/{id}")
    public R outDepDelOrder(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", id);
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepartmentGoodsStockService.queryGoodsStockByParams(map);
        if (stockEntities.size() > 0) {
            for (GbDepartmentGoodsStockEntity stock : stockEntities) {
                gbDepartmentGoodsStockService.delete(stock.getGbDepartmentGoodsStockId());
            }
        }

        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(id);
        Integer gbDoWeightTotalId = ordersEntity.getGbDoWeightTotalId();
        if (gbDoWeightTotalId != null) {
            GbDistributerWeightTotalEntity gbDistributerWeightTotalEntity = gbDisWeightTotalService.queryObject(gbDoWeightTotalId);
            BigDecimal gbGwtOrderCount = new BigDecimal(gbDistributerWeightTotalEntity.getGbGwtOrderCount());
            BigDecimal gbGwtFinishCount = new BigDecimal(gbDistributerWeightTotalEntity.getGbGwtOrderFinishCount());
            BigDecimal restCount = gbGwtOrderCount.subtract(gbGwtFinishCount);
            gbDistributerWeightTotalEntity.setGbGwtOrderCount(gbGwtOrderCount.subtract(new BigDecimal(1)).toString());
            if (restCount.compareTo(new BigDecimal(1)) == 0) {
                gbDistributerWeightTotalEntity.setGbGwtStatus(1);
            }
            gbDisWeightTotalService.update(gbDistributerWeightTotalEntity);
        }


        gbDepartmentOrdersService.delete(id);
        return R.ok();
    }


    /**
     * ORDER
     * 删除申请
     *
     * @param gbDepartmentOrdersId 订货申请id
     * @return ok
     */
    @ResponseBody
    @RequestMapping("/deleteOrderGb/{gbDepartmentOrdersId}")
    public R deleteOrderGb(@PathVariable Integer gbDepartmentOrdersId) {
        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
        Integer gbDoPurchaseGoodsId = ordersEntity.getGbDoPurchaseGoodsId();

        if (gbDoPurchaseGoodsId != -1) {

            GbDistributerPurchaseGoodsEntity gbDistributerPurchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
            if(gbDistributerPurchaseGoodsEntity != null){
                Integer gbDpgOrdersAmount = gbDistributerPurchaseGoodsEntity.getGbDpgOrdersAmount();
                if (gbDpgOrdersAmount > 1) {
                    gbDistributerPurchaseGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount - 1);
                    gbDistributerPurchaseGoodsService.update(gbDistributerPurchaseGoodsEntity);
                } else {
                    //订货批次是否是最后一个采购商品
                    Integer gbDpgBatchId = gbDistributerPurchaseGoodsEntity.getGbDpgBatchId();
                    Map<String, Object> mapBatch = new HashMap<>();
                    mapBatch.put("batchId", gbDpgBatchId);
                    List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(mapBatch);
                    if (goodsEntities.size() == 1) {
                        gbDPBService.delete(gbDistributerPurchaseGoodsEntity.getGbDpgBatchId());
                    }
                    gbDistributerPurchaseGoodsService.delete(gbDoPurchaseGoodsId);
                }
            }
        }
        if (ordersEntity.getGbDoNxDepartmentOrderId() != null) {
            Integer gbDoNxDepartmentOrderId = ordersEntity.getGbDoNxDepartmentOrderId();
            NxDepartmentOrdersEntity ordersEntity1 = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
            if(ordersEntity1 != null){
                delNxPurGoods(ordersEntity1);
            }

            nxDepartmentOrdersService.delete(ordersEntity1.getNxDepartmentOrdersId());
        }


        gbDepartmentOrdersService.delete(gbDepartmentOrdersId);
        return R.ok();

    }

    private void delNxPurGoods(NxDepartmentOrdersEntity ordersEntity) {
        if (ordersEntity.getNxDoPurchaseGoodsId() != null && ordersEntity.getNxDoPurchaseGoodsId() != -1
                && ordersEntity.getNxDoPurchaseStatus() < getNxDepOrderBuyStatusFinishPurchase()) {
            NxDistributerPurchaseGoodsEntity nxDistributerPurchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(ordersEntity.getNxDoPurchaseGoodsId());
            if(nxDistributerPurchaseGoodsEntity != null){
                Integer nxDpgOrdersAmount = nxDistributerPurchaseGoodsEntity.getNxDpgOrdersAmount();
                if (nxDpgOrdersAmount > 1) {
                    nxDistributerPurchaseGoodsEntity.setNxDpgOrdersAmount(nxDpgOrdersAmount - 1);
                    nxDistributerPurchaseGoodsService.update(nxDistributerPurchaseGoodsEntity);
                } else {
                    if (nxDistributerPurchaseGoodsEntity.getNxDpgStatus() == getNxDisPurchaseGoodsWithBatch()) {
                        Integer nxDpgBatchId = nxDistributerPurchaseGoodsEntity.getNxDpgBatchId();
                        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByBatchId(nxDpgBatchId);
                        if (purchaseGoodsEntities.size() == 1) {
                            nxDPBService.delete(nxDpgBatchId);
                        }
                        else {
                            String nxDpgBuySubtotal = nxDistributerPurchaseGoodsEntity.getNxDpgBuySubtotal();
                            NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryObject(nxDpgBatchId);
                            if(nxDistributerPurchaseBatchEntity.getNxDpbSellSubtotal() !=  null){
                                BigDecimal decimal = new BigDecimal(nxDistributerPurchaseBatchEntity.getNxDpbSellSubtotal());
                                BigDecimal decimal2 = new BigDecimal(nxDpgBuySubtotal);
                                BigDecimal decimal1 = decimal.subtract(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                                nxDistributerPurchaseBatchEntity.setNxDpbSellSubtotal(decimal1.toString());
                                nxDPBService.update(nxDistributerPurchaseBatchEntity);
                            }
                        }
                    }

                    nxDistributerPurchaseGoodsService.delete(nxDistributerPurchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
                }
            }
        }
    }


////////**************************************************

    /**
     * PURCHASE,
     * 采购员复制自采购申请
     *
     * @param depOrders 自采购申请
     * @return ok
     */
    @RequestMapping(value = "/purchaserCopyOrderContent", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserCopyOrderContent(@RequestBody List<GbDepartmentOrdersEntity> depOrders) {
        for (GbDepartmentOrdersEntity ordersEntity : depOrders) {
            ordersEntity.setGbDoOperationTime(formatWhatTime(0));
            ordersEntity.setGbDoStatus(getGbOrderStatusProcurement());
            gbDepartmentOrdersService.update(ordersEntity);
        }
        return R.ok();
    }

    /**
     * PURCHASE,
     *
     * @param depId 订货群id
     * @param disId 批发商id
     * @return 订货申请列表
     */
    @RequestMapping(value = "/purchaserGetDisOrders", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserGetDisOrders(Integer depId, Integer disId, Integer arriveDate) {

        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depId);
        map.put("disId", disId);
        map.put("orderBy", "time");
        map.put("status", 3);
        map.put("arriveDate", formatWhatDay(arriveDate));

        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
        //按每天显示订单
        Map<String, Object> stringObjectMap = countSwiperHeight((ordersEntities));

        return R.ok().put("data", stringObjectMap);
    }

    private Map<String, Object> countSwiperHeight(List<GbDepartmentOrdersEntity> ordersEntities) {
        Map<String, Object> map = new HashMap<>();
        Integer countRemark = 0;
        for (GbDepartmentOrdersEntity order : ordersEntities) {
            String nxDoRemark = order.getGbDoRemark();
            if (nxDoRemark.length() > 0) {
                countRemark = countRemark + 1;
            }
        }
        map.put("remarkCount", countRemark);
        map.put("orderArr", ordersEntities);

        return map;
    }

    /**
     * PURCHASE,
     * //
     *
     * @param depId 订货群id
     * @return 自采购商品
     */
    @RequestMapping(value = "/purchaserGetIndependentOrders", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserGetIndependentOrders(Integer depId, Integer arriveDate) {

        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depId);
        map.put("goodsType", 1);
        map.put("status", 4);
        map.put("arriveDate", formatWhatDay(arriveDate));
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
        return R.ok().put("data", ordersEntities);
    }


    /**
     * PURCHASE,
     * 采购员完成自采购申请
     *
     * @param depOrders 自采购申请
     * @return ok
     */
    @RequestMapping(value = "/purchaserFinishOrderContent", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserFinishOrderContent(@RequestBody List<GbDepartmentOrdersEntity> depOrders) {
        for (GbDepartmentOrdersEntity ordersEntity : depOrders) {
            ordersEntity.setGbDoStatus(4);
            gbDepartmentOrdersService.update(ordersEntity);
        }
        return R.ok();
    }

    /**
     * 9-11
     * DISTRIBUTER
     * 获取需要填写数量和价格的订单
     *
     * @param depFatherId 群id
     * @return 订单
     */
    @RequestMapping(value = "/typePurchasrGetToFinishDepOrders")
    @ResponseBody
    public R typePurchasrGetToFinishDepOrders(Integer depFatherId, Integer orderType, Integer toDepId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("equalStatus", 2);
        map.put("orderType", orderType);
        map.put("toDepId", toDepId);

        System.out.println("maop" + map);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(depFatherId);

        if (entities.size() > 0) {
            for (GbDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getGbDepartmentId());
                mapDep.put("depName", dep.getGbDepartmentName());

                Map<String, Object> map1 = new HashMap<>();
                map1.put("orderType", orderType);
                map1.put("equalStatus", 2);
                map1.put("toDepId", toDepId);
                map1.put("depId", dep.getGbDepartmentId());
                List<GbDepartmentOrdersEntity> depOrders = gbDepartmentOrdersService.queryDisOrdersByParams(map1);
                mapDep.put("depOrders", depOrders);
                if (depOrders.size() > 0) {
                    mapList.add(mapDep);
                }
            }

            return R.ok().put("data", mapList);
        } else {
            Map<String, Object> map3 = new HashMap<>();
            map3.put("depId", depFatherId);
            map3.put("date", formatWhatDay(0));
            int count = gbDepartmentBillService.queryDepartmentBillCount(map3);
            int trade = count + 1;
            String no = "";
            if (trade < 100) {
                if (count < 10) {
                    no = "00" + trade;
                } else {
                    no = "0" + trade;
                }
            } else {
                no = String.valueOf(count);
            }

            GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(depFatherId);
            String headPinyin = getHeadStringByString(gbDepartmentEntity.getGbDepartmentName(), true, null);
            String s = formatDayNumber(0) + headPinyin + no;
            Map<String, Object> mapR = new HashMap<>();
            mapR.put("arr", ordersEntities);
            mapR.put("tradeNo", s);
            return R.ok().put("data", mapR);
        }


    }


    /**
     * 库房，中央厨房，txsDistributer
     * 保存订单的重量
     *
     * @param depOrders 订单
     * @return ok
     */
    @RequestMapping(value = "/saveToFillWeight", method = RequestMethod.POST)
    @ResponseBody
    public R saveToFillWeight(@RequestBody List<GbDepartmentOrdersEntity> depOrders) {
        for (GbDepartmentOrdersEntity ordersEntity : depOrders) {
            ordersEntity.setGbDoStatus(getGbOrderStatusProcurement());
            gbDepartmentOrdersService.update(ordersEntity);
        }

        return R.ok();
    }

    /**
     * tsxDistributer
     *
     * @param ordersEntity
     * @return
     */
    @RequestMapping(value = "/saveToFillPriceOrderGb", method = RequestMethod.POST)
    @ResponseBody
    public R saveToFillPriceOrderGb(@RequestBody GbDepartmentOrdersEntity ordersEntity) {
        if (ordersEntity.getGbDoPrice().equals("0.0") || ordersEntity.getGbDoPrice().equals("0.") || ordersEntity.getGbDoPrice().length() == 0) {
            ordersEntity.setGbDoPrice("-1");
            ordersEntity.setGbDoSubtotal("-1");
        }

        ordersEntity.setGbDoStatus(getGbOrderStatusProcurement());
        gbDepartmentOrdersService.update(ordersEntity);
        return R.ok();
    }


    /**
     * kufang出库
     *
     * @param ordersEntity
     * @return
     */
    @RequestMapping(value = "/saveToFillWeightOrderGb", method = RequestMethod.POST)
    @ResponseBody
    public R saveToFillWeightOrderGb(@RequestBody GbDepartmentOrdersEntity ordersEntity) {
        if (ordersEntity.getGbDoWeight().equals("0.0") || ordersEntity.getGbDoWeight().equals("0.") || ordersEntity.getGbDoWeight().equals("0") || ordersEntity.getGbDoWeight().length() == 0) {
            ordersEntity.setGbDoWeight("-1");
            ordersEntity.setGbDoSubtotal("-1");
        }
        ordersEntity.setGbDoStatus(getGbOrderStatusProcurement());
        gbDepartmentOrdersService.update(ordersEntity);

        return R.ok();
    }


    /**
     * txsDistributer
     *
     * @param depOrders
     * @return
     */
    @RequestMapping(value = "/saveToFillPrice", method = RequestMethod.POST)
    @ResponseBody
    public R saveToFillPrice(@RequestBody List<GbDepartmentOrdersEntity> depOrders) {
        for (GbDepartmentOrdersEntity ordersEntity : depOrders) {
            ordersEntity.setGbDoStatus(getGbOrderStatusProcurement());
            gbDepartmentOrdersService.update(ordersEntity);
        }
        return R.ok();
    }


    @RequestMapping(value = "/disGetTodayGoodsOrder/{goodsFatherId}")
    @ResponseBody
    public R disGetTodayGoodsOrder(@PathVariable Integer goodsFatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("goodsFatherId", goodsFatherId);
        map.put("notOrderType", 10);
        System.out.println("whatismpa" + map);
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepartmentOrdersService.disGetTodayGoodsOrder(map);

        return R.ok().put("data", distributerGoodsEntities);
    }

    @RequestMapping(value = "/depGetToPlanPurchaseGoods", method = RequestMethod.POST)
    @ResponseBody
    public R depGetToPlanPurchaseGoods(Integer depId, Integer isSelf, String orderType) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", depId);
        map.put("buyStatus", 2);
        map.put("dayuStatus", -1);
        map.put("status", 2);
        map.put("orderType", orderType);
        map.put("isSelf", isSelf);
        System.out.println("akfa" + map);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepartmentOrdersService.disGetUnPlanPurchaseApplys(map);
        Map<String, Object> map33 = new HashMap<>();
        map33.put("depId", depId);
        map33.put("equalStatus", -1);
        map33.put("isSelf", isSelf);
        int weightGoodsAcount = gbDisWeightGoodsService.queryWeightGoodsAccount(map33);
        map33.put("equalStatus", 0);
        int weightTotalAcount = gbDisWeightTotalService.queryDepWeightCountByParams(map33);


        Map<String, Object> map2 = new HashMap<>();
        map2.put("toDepId", depId);
        map2.put("buyStatus", 1);
        map2.put("isSelf", 0);
        map2.put("dayuStatus", -1);
        map2.put("status", 3);
        System.out.println("map22222" + map2);
        Integer amount2 = gbDepartmentOrdersService.queryTotalByParams(map2);

        map2.put("isSelf", 1);
        Integer amount3 = gbDepartmentOrdersService.queryTotalByParams(map2);


        Map<String, Object> todayData = new HashMap<>();
        todayData.put("isSelfAmount", amount3);
        todayData.put("amount", amount2);

        todayData.put("arr", fatherGoodsEntities);
        todayData.put("weightTotal", weightTotalAcount);
        todayData.put("weightGoodsTotal", weightGoodsAcount);

        return R.ok().put("data", todayData);
    }


    @RequestMapping(value = "/depGetToPlanPurchaseGoodsStock", method = RequestMethod.POST)
    @ResponseBody
    public R depGetToPlanPurchaseGoodsStock(Integer depId, Integer orderType) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", depId);
        map.put("buyStatus", 2);
        map.put("isSelf", 0);
        map.put("orderType", orderType);
        map.put("shelfId", 1);
        System.out.println("map=orderTypeorderTypeorderType=" + map);
        List<GbDistributerGoodsShelfEntity> shelfEntities = gbDepartmentOrdersService.disGetUnPlanPurchaseApplysStock(map);

        Map<String, Object> map33 = new HashMap<>();
        map33.put("depId", depId);
        map33.put("equalStatus", -1);
        map33.put("isSelf", 0);
        int weightGoodsAcount = gbDisWeightGoodsService.queryWeightGoodsAccount(map33);
        map33.put("equalStatus", 0);
        int i = gbDisWeightTotalService.queryDepWeightCountByParams(map33);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("toDepId", depId);
        map2.put("buyStatus", 1);
        map2.put("dayuStatus", -1);
        map2.put("status", 3);
        map2.put("isSelf", 0);
        map2.put("isNotSelf", 1);
        Integer amount2 = gbDepartmentOrdersService.queryTotalByParams(map2);

        map2.put("isSelf", 1);
        Integer amount3 = gbDepartmentOrdersService.queryTotalByParams(map2);

        Map<String, Object> todayData = new HashMap<>();
        todayData.put("isSelfAmount", amount3);
        todayData.put("amount", amount2);
        todayData.put("arr", shelfEntities);
        todayData.put("weightTotal", i);
        todayData.put("weightGoodsTotal", weightGoodsAcount);


        return R.ok().put("data", todayData);
    }

    @RequestMapping(value = "/kitchenGetToPlanPurchaseGoods/{depId}")
    @ResponseBody
    public R kitchenGetToPlanPurchaseGoods(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", depId);
        map.put("status", 3);
        map.put("orderType", getGbOrderTypeKitchen());
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepartmentOrdersService.disGetUnPlanPurchaseApplys(map);

        return R.ok().put("data", fatherGoodsEntities);
    }


}

