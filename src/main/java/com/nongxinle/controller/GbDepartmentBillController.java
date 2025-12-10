package com.nongxinle.controller;

/**
 * @author lpy
 * @date 09-20 15:11
 */


import com.github.wxpay.sdk.WXPay;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.nongxinle.utils.CommonUtils.generateBillTradeNo;
import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.getLastMonth;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeKufang;


@RestController
@RequestMapping("api/gbdepartmentbill")
public class GbDepartmentBillController {
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;
    @Autowired
    private GbDistributerGoodsPriceService goodsPriceService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepGoodsStockReduceService;
    @Autowired
    private GbDepartmentGoodsDailyService gbDepGoodsDailyService;


    @RequestMapping(value = "/stockReceiveBill", method = RequestMethod.POST)
    @ResponseBody
    public R stockReceiveBill(@RequestBody GbDepartmentBillEntity billEntity) {

        Boolean ifCanReceiveTime = getIfCanReceiveTime();
        if(ifCanReceiveTime){
            List<GbDepartmentOrdersEntity> ordersEntities = billEntity.getGbDepartmentOrdersEntities();
            Integer gbDbIssueDepId = billEntity.getGbDbIssueDepId();
            GbDepartmentEntity issueDepartment = gbDepartmentService.queryObject(gbDbIssueDepId);
            if (ordersEntities.size() > 0) {
                for (GbDepartmentOrdersEntity ordersEntity : ordersEntities) {
                    if(ordersEntity.getHasChoice()){
                        departmentReceivePurBill(ordersEntity);
                    }else{
                        ordersEntity.setGbDoStatus(getGbOrderStatusHasFinished());
                        ordersEntity.setGbDoBillId(null);
                        gbDepartmentOrdersService.update(ordersEntity);
                    }
                }
            }
            billEntity.setGbDbStatus(0);
            gbDepartmentBillService.update(billEntity);
            return R.ok();
        }else{
            return R.error(-1,"5分钟之内是更新时间，请稍后操作");
        }
    }



    @RequestMapping(value = "/mendianReceiveBill", method = RequestMethod.POST)
    @ResponseBody
    public R mendianReceiveBill(@RequestBody GbDepartmentBillEntity billEntity) {
        Boolean ifCanReceiveTime = getIfCanReceiveTime();
        if(ifCanReceiveTime){
            List<GbDepartmentOrdersEntity> ordersEntities = billEntity.getGbDepartmentOrdersEntities();
            Integer gbDbIssueDepId = billEntity.getGbDbIssueDepId();
            GbDepartmentEntity issueDepartment = gbDepartmentService.queryObject(gbDbIssueDepId);
            Integer gbDepartmentType = issueDepartment.getGbDepartmentType();
            if (ordersEntities.size() > 0) {
                for (GbDepartmentOrdersEntity ordersEntity : ordersEntities) {
                    if(ordersEntity.getHasChoice()){
                        if (gbDepartmentType.equals(getGbDepartmentTypeKufang()) || gbDepartmentType.equals(getGbDepartmentTypeKitchen())) {
                            departmentReceiveOutStock(ordersEntity);
                        } else {
                            departmentReceivePurBill(ordersEntity);
                        }
                    }else{
                        ordersEntity.setGbDoStatus(getGbOrderStatusHasFinished());
                        ordersEntity.setGbDoBillId(null);
                        gbDepartmentOrdersService.update(ordersEntity);
                    }
                }
            }
            billEntity.setGbDbStatus(0);
            gbDepartmentBillService.update(billEntity);
            return R.ok();
        }else{
            return R.error(-1,"5分钟之内是更新时间，请稍后操作");
        }
    }


    public R departmentReceiveOutStock(@RequestBody GbDepartmentOrdersEntity order) {


        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
        Integer gbDoDepDisGoodsId = order.getGbDoDepDisGoodsId();
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDoDepDisGoodsId);

        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
        Integer gbDoStatus = ordersEntity.getGbDoStatus();
        //判断没有被别人收货
        if (gbDoStatus.equals(getGbOrderStatusHasBill())) {
            //1,修改库存数据
//            if (order.getGbDoOrderType().equals(getGbOrderTypeChuKu()) || order.getGbDoOrderType().equals(getGbOrderTypeKitchen())) {
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
                stock.setGbDgsStars(5);
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

            //2，修改订单状态
            order.setGbDoStatus(getGbOrderStatusReceived());
            gbDepartmentOrdersService.update(order);
            return R.ok();
        } else {
            return R.error(-1, "已经完成收货");
        }

    }


    public R departmentReceivePurBill(@RequestBody GbDepartmentOrdersEntity order) {
        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
        Integer gbDoDepDisGoodsId = order.getGbDoDepDisGoodsId();
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDoDepDisGoodsId);

        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
        Integer gbDoStatus = ordersEntity.getGbDoStatus();
        //判断没有被别人收货
        if (gbDoStatus.equals(getGbOrderStatusHasBill())) {


            //0,修改订单上次价格涨幅
            String gbDdgOrderDate = departmentDisGoodsEntity.getGbDdgOrderDate();

            if (gbDdgOrderDate != null && !gbDdgOrderDate.trim().isEmpty() && order.getGbDoPrice() != null && !order.getGbDoPrice().trim().isEmpty()) {
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
            stockEntity.setGbDgsGbDisGoodsGrandId(order.getGbDoDisGoodsGrandId());
            stockEntity.setGbDgsGbDisGoodsGreatId(order.getGbDoDisGoodsGreatId());
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
            if (gbDdgSellingPrice != null && !gbDdgSellingPrice.trim().isEmpty() && new BigDecimal(gbDdgSellingPrice).compareTo(new BigDecimal(0)) == 1) {
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
                if (purchaseGoodsEntity.getGbDpgWasteFullTime() != null && !purchaseGoodsEntity.getGbDpgWasteFullTime().trim().isEmpty()) {
                    stockEntity.setGbDgsWasteFullTime(purchaseGoodsEntity.getGbDpgWasteFullTime());
                    String gbDpgWasteFullTime = purchaseGoodsEntity.getGbDpgWasteFullTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    // 设置日期字符串
                    // 解析日期字符串为Date对象
                    Date dateWaste = null;
                    Date dateWarn = null;
                    try {
                        if (gbDpgWasteFullTime != null && !gbDpgWasteFullTime.trim().isEmpty()) {
                            dateWaste = dateFormat.parse(gbDpgWasteFullTime);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    // 获取时间戳
                    long timestampWaste = 0;
                    if (dateWaste != null) {
                        timestampWaste = dateWaste.getTime();
                    }

                    // 输出时间戳
                    System.out.println("zhelieiieieiieieiieeiie" + dateWaste + "abcc" + timestampWaste);
                    stockEntity.setGbDgsWasteTimeQuantumName(String.valueOf(timestampWaste));

                }
                //判断是否价格异常商品
                if (purchaseGoodsEntity.getGbDpgDisGoodsPriceId() != null && !purchaseGoodsEntity.getGbDpgDisGoodsPriceId().toString().trim().isEmpty()) {
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
            stockEntity.setGbDgsStars(5);
            stockEntity.setGbDgsNxSupplierId(-1);
            gbDepartmentGoodsStockService.save(stockEntity);

            updateDepGoodsDailyBusiness(stockEntity);

            orderAddDepDisGoods(order, stockEntity, gbDoDepDisGoodsId);

            //2，修改订单状态
            order.setGbDoStatus(getGbOrderStatusReceived());
            gbDepartmentOrdersService.update(order);

//            //3，修改送货单收货单子数量
            if (order.getGbDoPurchaseGoodsId() != -1) {
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(order.getGbDoPurchaseGoodsId());
                BigDecimal gbPgOrderAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersAmount());
                BigDecimal gbDbFinishAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersFinishAmount());
                if (gbDbFinishAmount.add(new BigDecimal(1)).compareTo(gbPgOrderAmount) == 0) {
                    purchaseGoodsEntity.setGbDpgOrdersFinishAmount(purchaseGoodsEntity.getGbDpgOrdersAmount());
                    purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusWaitReceive());
                } else {
                    BigDecimal add = gbDbFinishAmount.add(new BigDecimal(1));
                    purchaseGoodsEntity.setGbDpgOrdersFinishAmount(add.intValue());
                }
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }

            return R.ok();
        } else {
            return R.error(-1, "已经完成收货");
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
        reduceEntity.setGbDgsrStockNxSupplierId(stock.getGbDgsNxSupplierId());
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
            BigDecimal restSubtotal = new BigDecimal(depGoodsDailyItem.getGbDgdRestSubtotal());
            BigDecimal produceWeight = new BigDecimal(depGoodsDailyItem.getGbDgdProduceWeight());
            BigDecimal produceSubtotal = new BigDecimal(depGoodsDailyItem.getGbDgdProduceSubtotal());

            BigDecimal outWeight = new BigDecimal(stockEntity.getGbDgsWeight());
            BigDecimal outSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());
            BigDecimal produceAllWeight = produceWeight.add(outWeight);
            BigDecimal produceAllSubtotal = produceSubtotal.add(outSubtotal);
            BigDecimal totalRestWeight = restWeight.subtract(outWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalRestSubtotal = restSubtotal.subtract(outSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            depGoodsDailyItem.setGbDgdRestWeight(totalRestWeight.toString());
            depGoodsDailyItem.setGbDgdRestSubtotal(totalRestSubtotal.toString());
            depGoodsDailyItem.setGbDgdProduceWeight(produceAllWeight.toString());
            depGoodsDailyItem.setGbDgdProduceSubtotal(produceAllSubtotal.toString());
            gbDepGoodsDailyService.update(depGoodsDailyItem);
        }

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

    @ResponseBody
    @RequestMapping(value = "/restrauntCashPay", method = RequestMethod.POST)
    public R restrauntCashPay(@RequestBody GbDepartmentBillEntity billEntity) {
        System.out.println("billl" + billEntity);

        //转换总金额
        String nxRbTotal = billEntity.getGbDbTotal();
        Double aDouble = Double.parseDouble(nxRbTotal) * 100;
        int i = aDouble.intValue();
        String s1 = String.valueOf(i);


        //订单号

        String tradeNo = CommonUtils.generateOutTradeNo();
        //餐馆支付配置
        MyWxShixianliliPayConfig config = new MyWxShixianliliPayConfig();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/gbdepartmentbill/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", billEntity.getGbUserOpenId());
        System.out.println("s1111" + s1);

        //map转xml
        try {

            WXPay wxpay = new WXPay(config);
            long time = System.currentTimeMillis();
            String tString = String.valueOf(time / 1000);
            Map<String, String> resp = wxpay.unifiedOrder(params);
            System.out.println(resp);
            SortedMap<String, String> reMap = new TreeMap<>();
            reMap.put("appId", config.getAppID());
            reMap.put("nonceStr", resp.get("nonce_str"));
            reMap.put("package", "prepay_id=" + resp.get("prepay_id"));
            reMap.put("signType", "MD5");
            reMap.put("timeStamp", tString);
            String s = WxPayUtils.creatSign(reMap, config.getKey());
            reMap.put("paySign", s);

            billEntity.setGbDbWxOutTradeNo(tradeNo);
            gbDepartmentBillService.update(billEntity);

            return R.ok().put("map", reMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.ok();
    }


    /**
     * @Title: callBack
     * @Description: 支付完成的回调函数
     * @param:
     * @return:
     */
    @RequestMapping("/notify")
    public String callBack(HttpServletRequest request, HttpServletResponse response) {
        // System.out.println("微信支付成功,微信发送的callback信息,请注意修改订单信息");
        InputStream is = null;
        try {

            is = request.getInputStream();// 获取请求的流信息(这里是微信发的xml格式所有只能使用流来读)
            String xml = WxPayUtils.InputStream2String(is);
            Map<String, String> notifyMap = WxPayUtils.xmlToMap(xml);// 将微信发的xml转map

            System.out.println("微信返回给回调函数的信息为：" + xml);

            if (notifyMap.get("result_code").equals("SUCCESS")) {

                /*
                 * 以下是自己的业务处理------仅做参考 更新order对应字段/已支付金额/状态码
                 * 更新bill支付状态
                 */
                System.out.println("===notify===回调方法已经被调！！！");
                String ordersSn = notifyMap.get("out_trade_no");// 商户订单号
                GbDepartmentBillEntity billEntity = gbDepartmentBillService.queryDepartBillByTradeNo(ordersSn);
                billEntity.setGbDbStatus(4);
                gbDepartmentBillService.update(billEntity);

            }

            // 告诉微信服务器收到信息了，不要在调用回调action了========这里很重要回复微信服务器信息用流发送一个xml即可
            response.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }


    @RequestMapping(value = "/depSearchBillGoods", method = RequestMethod.POST)
    @ResponseBody
    public R depSearchBillGoods(Integer depGoodsId, String startDate, String stopDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("depGoodsId", depGoodsId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        System.out.println("map" + map);
        List<GbDepartmentBillEntity> billEntities = gbDepartmentBillService.queryBillGoodsByParams(map);
        return R.ok().put("data", billEntities);
    }


    @RequestMapping(value = "/disGetNxDistributerUnPayBills", method = RequestMethod.POST)
    @ResponseBody
    public R disGetNxDistributerUnPayBills(Integer nxDisId, Integer gbDisId, Integer status) {

        Map<String, Object> map = new HashMap<>();
        map.put("nxDisId", nxDisId);
        map.put("disId", gbDisId);
        map.put("status", status);
        List<GbDepartmentBillEntity> billEntities = gbDepartmentBillService.queryBillsByParamsGb(map);

        Double aDouble = 0.0;
        if (gbDepartmentBillService.queryDepartmentBillCount(map) > 0) {
            aDouble = gbDepartmentBillService.queryGbDepBillsSubTotal(map);
        }


        Map<String, Object> map6 = new HashMap<>();
        map6.put("total", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP));
        map6.put("arr", billEntities);

        return R.ok().put("data", map6);
    }


    @RequestMapping(value = "/disGetNxDistributerBills", method = RequestMethod.POST)
    @ResponseBody
    public R disGetNxDistributerBills(Integer nxDisId, Integer gbDisId, String startDate,
                                      String stopDate, Integer status) {
        //第一个月
        Map<String, Object> map = new HashMap<>();
        map.put("nxDisId", nxDisId);
        map.put("disId", gbDisId);
        map.put("month", formatWhatMonth(0));
        List<GbDepartmentBillEntity> billEntities = gbDepartmentBillService.queryBillsByParamsGb(map);

        map.put("month", getLastMonth());
        List<GbDepartmentBillEntity> billEntities1 = gbDepartmentBillService.queryBillsByParamsGb(map);

        map.put("month", getLastTwoMonth());
        List<GbDepartmentBillEntity> billEntities2 = gbDepartmentBillService.queryBillsByParamsGb(map);

        Map<String, Object> mapSub = new HashMap<>();
        mapSub.put("nxDisId", nxDisId);
        mapSub.put("disId", gbDisId);
        mapSub.put("month", formatWhatMonth(0));
        mapSub.put("status", 4);
        Double aDouble = 0.0;
        if (gbDepartmentBillService.queryDepartmentBillCount(mapSub) > 0) {
            aDouble = gbDepartmentBillService.queryGbDepBillsSubTotal(mapSub);
        }
        Double aDoubleOne = 0.0;
        mapSub.put("month", getLastMonth());
        if (gbDepartmentBillService.queryDepartmentBillCount(mapSub) > 0) {
            aDoubleOne = gbDepartmentBillService.queryGbDepBillsSubTotal(mapSub);
        }
        Double aDoubleTwo = 0.0;
        mapSub.put("month", getLastTwoMonth());
        if (gbDepartmentBillService.queryDepartmentBillCount(mapSub) > 0) {
            aDoubleTwo = gbDepartmentBillService.queryGbDepBillsSubTotal(mapSub);
        }


        Map<String, Object> mapSubHave = new HashMap<>();
        mapSubHave.put("nxDisId", nxDisId);
        mapSubHave.put("disId", gbDisId);
        mapSubHave.put("month", formatWhatMonth(0));
        mapSubHave.put("dayuStatus", 3);
        int count1 = gbDepartmentBillService.queryDepartmentBillCount(mapSubHave);
        Double aDoubleHave = 0.0;
        if (count1 > 0) {
            aDoubleHave = gbDepartmentBillService.queryGbDepBillsSubTotal(mapSubHave);
        }

        mapSubHave.put("month", getLastMonth());
        int count2 = gbDepartmentBillService.queryDepartmentBillCount(mapSubHave);
        Double aDoubleHaveOne = 0.0;
        if (count2 > 0) {
            aDoubleHaveOne = gbDepartmentBillService.queryGbDepBillsSubTotal(mapSubHave);
        }


        mapSubHave.put("month", getLastTwoMonth());
        int count3 = gbDepartmentBillService.queryDepartmentBillCount(mapSubHave);
        Double aDoubleHaveTwo = 0.0;
        if (count3 > 0) {
            aDoubleHaveTwo = gbDepartmentBillService.queryGbDepBillsSubTotal(mapSubHave);
        }


        Map<String, Object> map3 = new HashMap<>();
        map3.put("arr", billEntities);
        map3.put("month", formatWhatMonth(0));
        map3.put("unSubtotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP));
        map3.put("haveSubtotal", new BigDecimal(aDoubleHave).setScale(1, BigDecimal.ROUND_HALF_UP));

        Map<String, Object> map4 = new HashMap<>();
        map4.put("arr", billEntities1);
        map4.put("month", getLastMonth());
        map4.put("unSubtotal", new BigDecimal(aDoubleOne).setScale(1, BigDecimal.ROUND_HALF_UP));
        map4.put("haveSubtotal", new BigDecimal(aDoubleHaveOne).setScale(1, BigDecimal.ROUND_HALF_UP));
        Map<String, Object> map5 = new HashMap<>();
        map5.put("arr", billEntities2);
        map5.put("month", getLastTwoMonth());
        map5.put("unSubtotal", new BigDecimal(aDoubleTwo).setScale(1, BigDecimal.ROUND_HALF_UP));
        map5.put("haveSubtotal", new BigDecimal(aDoubleHaveTwo).setScale(1, BigDecimal.ROUND_HALF_UP));

        List<Map<String, Object>> resultData = new ArrayList<>();
        resultData.add(map3);
        resultData.add(map4);
        resultData.add(map5);

        Map<String, Object> maptotal = new HashMap<>();
        maptotal.put("status", 4);
        maptotal.put("nxDisId", nxDisId);
        maptotal.put("disId", gbDisId);
        Double aDoubleTotal = 0.0;
        int count = gbDepartmentBillService.queryDepartmentBillCount(maptotal);
        if (count > 0) {
            aDoubleTotal = gbDepartmentBillService.queryGbDepBillsSubTotal(maptotal);
        }
        Map<String, Object> map6 = new HashMap<>();
        map6.put("total", new BigDecimal(aDoubleTotal).setScale(1, BigDecimal.ROUND_HALF_UP));
        map6.put("count", count);
        map6.put("bill", resultData);

        return R.ok().put("data", map6);
    }

    @RequestMapping(value = "/getDepBills", method = RequestMethod.POST)
    @ResponseBody
    public R getDepBills(Integer depFatherId, String startDate, String stopDate, Integer issueDepId, Integer nxDisId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depFatherId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        if (issueDepId != -1) {
            map.put("issueDepId", issueDepId);
        }
        if (nxDisId != -1) {
            map.put("nxDisId", nxDisId);
        }
        List<GbDepartmentBillEntity> billEntities = gbDepartmentBillService.queryBillsByParamsGb(map);
        Double aDouble = gbDepartmentBillService.queryGbDepBillsSubTotal(map);
        Map<String, Object> mapR = new HashMap<>();
        if (billEntities.size() > 0) {
            mapR.put("arr", billEntities);
            mapR.put("billTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP));
            return R.ok().put("data", mapR);

        } else {
            return R.error(-1, "没有数据");
        }

    }


    @RequestMapping(value = "/settleDepBillsGb", method = RequestMethod.POST)
    @ResponseBody
    public R settleDepBillsGb(@RequestBody List<GbDepartmentBillEntity> bills) {
        for (GbDepartmentBillEntity bill : bills) {
            bill.setGbDbStatus(3);
            bill.setGbDbConfirmSettleTime(formatFullTime());
            gbDepartmentBillService.update(bill);
        }
        return R.ok();
    }


    @RequestMapping(value = "/disGetUnSettleAccountBillsGb/{depId}")
    @ResponseBody
    public R disGetUnSettleAccountBillsGb(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("equalStatus", 2);
        List<GbDepartmentBillEntity> billEntityList = gbDepartmentBillService.queryBillsByParamsGb(map);
        return R.ok().put("data", billEntityList);
    }


    @RequestMapping(value = "/confirmBillPrice", method = RequestMethod.POST)
    @ResponseBody
    public R confirmBillPrice(Integer userId, Integer billId) {
        GbDepartmentBillEntity billEntity = gbDepartmentBillService.queryObject(billId);
        billEntity.setGbDbConfirmGoodsUserId(userId);
        billEntity.setGbDbStatus(2);
        billEntity.setGbDbConfirmPriceTime(formatFullTime());
        gbDepartmentBillService.update(billEntity);
        return R.ok();
    }

    @RequestMapping(value = "/confirmBillGoods", method = RequestMethod.POST)
    @ResponseBody
    public R confirmBillGoods(Integer userId, Integer billId) {
        GbDepartmentBillEntity billEntity = gbDepartmentBillService.queryObject(billId);
        billEntity.setGbDbConfirmGoodsUserId(userId);
        billEntity.setGbDbStatus(1);
        billEntity.setGbDbConfirmGoodsTime(formatFullTime());
        gbDepartmentBillService.update(billEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryOrdersByBillId(map);
        for (GbDepartmentOrdersEntity orders : ordersEntities) {
            orders.setGbDoStatus(4);
            gbDepartmentOrdersService.update(orders);
        }

        return R.ok();
    }


//    @RequestMapping(value = "/retrurnSupplierBatch/{batchid}")
//    @ResponseBody
//    public R retrurnSupplierBatch(@PathVariable Integer batchid) {
//        GbDistributerPurchaseBatchEntity gbDisPurchaseBatchEntity = gbDisPurchaseBatchService.queryObject(batchid);
//        gbDisPurchaseBatchEntity.setGbDpbStatus(1);
//        gbDisPurchaseBatchService.update(gbDisPurchaseBatchEntity);
//        return R.ok();
//    }


    @RequestMapping(value = "/getIssueDepartmentBill", method = RequestMethod.POST)
    @ResponseBody
    public R getIssueDepartmentBill(Integer isssueDepId, Integer orderType) {

        Map<String, Object> map = new HashMap<>();
        map.put("issueDepId", isssueDepId);
        map.put("status", 3);
        map.put("dayuStatus", -1);
        map.put("orderType", orderType);
        List<GbDepartmentBillEntity> billEntities = gbDepartmentBillService.queryBillsByParamsGb(map);

        return R.ok().put("data", billEntities);
    }


//    @RequestMapping(value = "/getDepartmentOutStockBill")
//    @ResponseBody
//    public R getDepartmentOutStockBill(Integer depId, Integer issueDepId, String type, Integer orderType) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("depId", depId);
//        map.put("issueDepId", issueDepId);
////        map.put("orderType", orderType);
//        if (type.equals("all")) {
//            map.put("dayuStatus", -2);
//            map.put("status", 3);
//        }
//        if (type.equals("finish")) {
//            map.put("dayuStatus", -1);
//            map.put("status", 3);
//        }
//        if (type.equals("unReceive")) {
//            map.put("equalStatus", -1);
//        }
//        System.out.println("map"+ map);
//        List<GbDepartmentBillEntity> billEntities = gbDepartmentBillService.queryBillsByParamsGb(map);
//        return R.ok().put("data", billEntities);
//    }


    @RequestMapping(value = "/getAccountApplysGb")
    @ResponseBody
    public R getAccountApplysGb(Integer billId, Integer depFatherId) {

        GbDepartmentBillEntity salesBill = gbDepartmentBillService.queryGbDepartmentBillDetail(billId);
        String gbDepartmentName = "";
        String gbDuWxNickName = "";
//        System.out.println("Zahsuifuda" + salesBill.getGbDbIssueOrderType());
//        if (salesBill.getGbDbIssueNxDisId() != null) {
//            Integer gbDbIssueNxDisId = salesBill.getGbDbIssueNxDisId();
//            NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(gbDbIssueNxDisId);
//            gbDuWxNickName = nxDistributerEntity.getNxDistributerName();
//        } else {
//            gbDepartmentName = salesBill.getIssueDepartmentEntity().getGbDepartmentName();
//            gbDuWxNickName = salesBill.getIssueUserEntity().getGbDuWxNickName();
//        }


        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);

        List<Map<String, Object>> mapList = new ArrayList<>();
        List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(depFatherId);

        if (entities.size() > 0) {
            for (GbDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getGbDepartmentId());
                mapDep.put("depName", dep.getGbDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("billId", billId);
                map1.put("depId", dep.getGbDepartmentId());
                List<GbDepartmentOrdersEntity> depOrders = gbDepartmentOrdersService.queryDisOrdersByParams(map1);
                mapDep.put("depOrders", depOrders);
                if (depOrders.size() > 0) {
                    mapList.add(mapDep);
                }
            }

            Map<String, Object> map3 = new HashMap<>();
            map3.put("arr", mapList);
            map3.put("bill", salesBill);
            map3.put("toDepName", gbDepartmentName);
            map3.put("issueUser", gbDuWxNickName);
            map3.put("depHasSubs", entities.size());

            return R.ok().put("data", map3);
        } else {
            Map<String, Object> map4 = new HashMap<>();
            map4.put("arr", ordersEntities);
            map4.put("bill", salesBill);
            map4.put("toDepName", gbDepartmentName);
            map4.put("issueUser", gbDuWxNickName);
            map4.put("depHasSubs", 0);
            return R.ok().put("data", map4);
        }
    }


    @RequestMapping(value = "/getBillApplysGbWithStock", method = RequestMethod.POST)
    @ResponseBody
    public R getBillApplysGbWithStock(Integer billId, Integer depFatherId) {
        GbDepartmentBillEntity salesBill = gbDepartmentBillService.queryGbDepartmentBillDetail(billId);
        String gbDepartmentName = salesBill.getIssueDepartmentEntity().getGbDepartmentName();
        String gbDuWxNickName = "";
        if (salesBill.getGbDbIssueOrderType().equals(getGbOrderTypeAppSupplier())) {
            Integer gbDbIssueNxDisId = salesBill.getGbDbIssueNxDisId();
            NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(gbDbIssueNxDisId);
            gbDuWxNickName = nxDistributerEntity.getNxDistributerName();
        } else {
            gbDepartmentName = salesBill.getIssueDepartmentEntity().getGbDepartmentName();
            gbDuWxNickName = salesBill.getIssueUserEntity().getGbDuWxNickName();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);

        List<Map<String, Object>> mapList = new ArrayList<>();
        List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(depFatherId);

        if (entities.size() > 0) {
            for (GbDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getGbDepartmentId());
                mapDep.put("depName", dep.getGbDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("billId", billId);
                map1.put("depId", dep.getGbDepartmentId());
                List<GbDepartmentOrdersEntity> depOrders = gbDepartmentOrdersService.queryDisOrdersByParams(map1);

                mapDep.put("depOrders", depOrders);

                if (depOrders.size() > 0) {
                    mapList.add(mapDep);
                }
            }

            Map<String, Object> map3 = new HashMap<>();
            map3.put("arr", mapList);
            map3.put("bill", salesBill);
            map3.put("toDepName", gbDepartmentName);
            map3.put("issueUser", gbDuWxNickName);
            map3.put("depHasSubs", entities.size());
            return R.ok().put("data", map3);
        } else {
            Map<String, Object> map4 = new HashMap<>();
            map4.put("arr", ordersEntities);
            map4.put("bill", salesBill);
            map4.put("toDepName", gbDepartmentName);
            map4.put("issueUser", gbDuWxNickName);
            map4.put("depHasSubs", 0);

//
            return R.ok().put("data", map4);
        }
    }


    @RequestMapping(value = "/getBillApplysGb")
    @ResponseBody
    public R getBillApplysGb(Integer billId, Integer depFatherId) {

        GbDepartmentBillEntity salesBill = gbDepartmentBillService.queryGbDepartmentBillDetail(billId);
        String gbDepartmentName = salesBill.getIssueDepartmentEntity().getGbDepartmentName();
        String gbDuWxNickName = salesBill.getIssueUserEntity().getGbDuWxNickName();

        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
//        map.put("status", 5);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryOrdersByBillId(map);

        List<Map<String, Object>> mapList = new ArrayList<>();
        List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(depFatherId);

        if (entities.size() > 0) {
            for (GbDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getGbDepartmentId());
                mapDep.put("depName", dep.getGbDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("billId", billId);
                map1.put("depId", dep.getGbDepartmentId());
                map1.put("status", 4);
                List<GbDepartmentOrdersEntity> depOrders = gbDepartmentOrdersService.queryOrdersByBillId(map1);

                mapDep.put("depOrders", depOrders);

                if (depOrders.size() > 0) {
                    mapList.add(mapDep);
                }
            }

            Map<String, Object> map3 = new HashMap<>();
            map3.put("arr", mapList);
            map3.put("bill", salesBill);
            map3.put("toDepName", gbDepartmentName);
            map3.put("issueUser", gbDuWxNickName);
//            map3.put("returnNumber", count);
            return R.ok().put("data", map3);
        } else {
            Map<String, Object> map4 = new HashMap<>();
            map4.put("arr", ordersEntities);
            map4.put("bill", salesBill);
            map4.put("toDepName", gbDepartmentName);
            map4.put("issueUser", gbDuWxNickName);
            return R.ok().put("data", map4);
        }


    }

    @RequestMapping(value = "/getDepartmentUnReceiveBill/{depId}")
    @ResponseBody
    public R getDepartmentUnReceiveBill(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("equalStatus", -1);
        List<GbDepartmentBillEntity> billEntities = gbDepartmentBillService.queryBillsByParamsGb(map);
        if (billEntities.size() > 0) {
            return R.ok().put("data", billEntities);
        } else {
            return R.ok();
        }
    }


    @RequestMapping(value = "/disGetStatusForDepartmentBill", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStatusForDepartmentBill(Integer disId, Integer status) {
        //本月的账单
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("equalStatus", status);
        List<GbDepartmentBillEntity> billEntityList = gbDepartmentBillService.queryBillsByParamsGb(map);
        return R.ok().put("data", billEntityList);
    }


    //

    @RequestMapping(value = "/getPurchaseDepartmentBills")
    @ResponseBody
    public R getPurchaseDepartmentBills(Integer issueDepId, String startDate, String stopDate, String[] orderTypes, String searchDepIds
                                        ) {

        //本月的账单
        Map<String, Object> map = new HashMap<>();

        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
//        map.put("dayuStatus", -1);
        map.put("orderTypes", orderTypes);
        map.put("issueDepId", issueDepId);
        List<String> idsGb = new ArrayList<>();
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("depFatherIds", idsGb);
                }
            }
        }

        List<GbDepartmentBillEntity> billEntityList = gbDepartmentBillService.queryBillsByParamsGb(map);

        //查询总账款金额
        String formatSubTotal = "0";
        String formatSellSubTotal = "0";
        Map<String, Object> map12 = new HashMap<>();

        map12.put("startDate", startDate);
        map12.put("stopDate", stopDate);
//        map12.put("dayuStatus", -1);
        map12.put("orderTypes", orderTypes);
        map12.put("issueDepId", issueDepId);
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map12.put("depFatherIds", idsGb);
                }
            }
        }

        int count = gbDepartmentBillService.queryDepartmentBillCount(map12);
        if (count > 0) {
            Double subTotal = gbDepartmentBillService.queryGbDepBillsSubTotal(map12);
            formatSubTotal = String.format("%.2f", subTotal);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("arr", billEntityList);
        resultMap.put("total", formatSubTotal);
        return R.ok().put("data", resultMap);
    }


    @RequestMapping(value = "/getIssueDepartmentBills")
    @ResponseBody
    public R getIssueDepartmentBills(Integer issueDepId, String startDate, String stopDate, String[] orderTypes,
                                     String searchDepIds) {

        //本月的账单
        Map<String, Object> map = new HashMap<>();

        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
//        map.put("dayuStatus", -1);
        map.put("orderTypes", orderTypes);
        map.put("issueDepId", issueDepId);
        map.put("notEqualDepId", issueDepId);
        List<String> idsGb = new ArrayList<>();

        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("depFatherIds", idsGb);
                }
            }
        }

        System.out.println("roodoososossos" + map);

        List<GbDepartmentBillEntity> billEntityList = gbDepartmentBillService.queryBillsByParamsGb(map);

        //查询总账款金额
        String formatSubTotal = "0";
        String formatSellSubTotal = "0";
        Map<String, Object> map12 = new HashMap<>();

        map12.put("startDate", startDate);
        map12.put("stopDate", stopDate);
        map12.put("dayuStatus", -1);
        map12.put("orderTypes", orderTypes);
        map12.put("issueDepId", issueDepId);
        map12.put("notEqualDepId", issueDepId);
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map12.put("depFatherIds", idsGb);
                }
            }
        }
        int count = gbDepartmentBillService.queryDepartmentBillCount(map12);
        if (count > 0) {
            Double subTotal = gbDepartmentBillService.queryGbDepBillsSubTotal(map12);
            formatSubTotal = String.format("%.2f", subTotal);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("arr", billEntityList);
        resultMap.put("total", formatSubTotal);
        return R.ok().put("data", resultMap);
    }


    @RequestMapping(value = "/getDepartmentAccountBills")
    @ResponseBody
    public R getDepartmentAccountBills(Integer depFatherId, Integer issueDepId, String startDate, String stopDate,
                                       Integer orderType, String[] orderTypes) {


        //本月的账单
        Map<String, Object> map = new HashMap<>();
        if (depFatherId != -1) {
            map.put("depId", depFatherId);
        }
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("dayuStatus", -1);
        if (orderType != -1) {
            map.put("orderType", orderType);
        } else {
            map.put("orderTypes", orderTypes);
        }
        if (issueDepId != -1) {
            map.put("issueDepId", issueDepId);

        }


        List<GbDepartmentBillEntity> billEntityList = gbDepartmentBillService.queryBillsByParamsGb(map);

        //查询总账款金额
        String formatSubTotal = "0";
        String formatSellSubTotal = "0";
        Map<String, Object> map12 = new HashMap<>();
        if (depFatherId != -1) {
            map12.put("depId", depFatherId);
        }
        map12.put("startDate", startDate);
        map12.put("stopDate", stopDate);
        map12.put("dayuStatus", -1);
        if (orderType != -1) {
            map12.put("orderType", orderType);
        } else {
            map12.put("orderTypes", orderTypes);
        }

        if (issueDepId != -1) {
            map12.put("issueDepId", issueDepId);
//            map12.put("notEqualDepId", issueDepId);
        }
        int count = gbDepartmentBillService.queryDepartmentBillCount(map12);
        if (count > 0) {
            Double subTotal = gbDepartmentBillService.queryGbDepBillsSubTotal(map12);
            formatSubTotal = String.format("%.2f", subTotal);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("arr", billEntityList);
        resultMap.put("total", formatSubTotal);
        return R.ok().put("data", resultMap);
    }


    @RequestMapping(value = "/getDepartmentBillDetail/{billId}")
    @ResponseBody
    public R getDepartmentBillDetail(@PathVariable Integer billId) {
        GbDepartmentBillEntity billEntity = gbDepartmentBillService.queryGbDepartmentBillDetail(billId);
        return R.ok().put("data", billEntity);
    }


    @RequestMapping(value = "/getPurchaserDepartmentBill", method = RequestMethod.POST)
    @ResponseBody
    public R getPurchaserDepartmentBill(Integer depId, Integer searchDepIds, String[] orderTypes) {

        //第一个月
        Map<String, Object> map = new HashMap<>();

        if (searchDepIds != -1) {
            map.put("depId", searchDepIds);
        }
        map.put("issueDepId", depId);
        map.put("month", formatWhatMonth(0));
        map.put("orderTypes", orderTypes);
        System.out.println("ewhwhwhwhwh" + map);
        List<GbDepartmentBillEntity> billEntities = gbDepartmentBillService.queryBillsByParamsGb(map);

        //第二个月
        map.put("month", getLastMonth());
        List<GbDepartmentBillEntity> billEntities1 = gbDepartmentBillService.queryBillsByParamsGb(map);
        //第三个月
        map.put("month", getLastTwoMonth());
        List<GbDepartmentBillEntity> billEntities2 = gbDepartmentBillService.queryBillsByParamsGb(map);


        Map<String, Object> map3 = new HashMap<>();
        map3.put("arr", billEntities);
        map3.put("month", formatWhatMonth(0));
        Map<String, Object> map4 = new HashMap<>();
        map4.put("arr", billEntities1);
        map4.put("month", getLastMonth());
        Map<String, Object> map5 = new HashMap<>();
        map5.put("arr", billEntities2);
        map5.put("month", getLastTwoMonth());

        List<Map<String, Object>> resultData = new ArrayList<>();
        resultData.add(map3);
        resultData.add(map4);
        resultData.add(map5);

        return R.ok().put("data", resultData);
    }

    @RequestMapping(value = "/saveAccountBillStockWindow", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillStockWindow(@RequestBody GbDepartmentBillEntity gbDepartmentBill) {

        List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = gbDepartmentBill.getGbDepartmentOrdersEntities();
        gbDepartmentBill.setGbDbStatus(-1);
        gbDepartmentBill.setGbDbDate(formatWhatDay(0));
        gbDepartmentBill.setGbDbTime(formatWhatYearDayTime(0));
        gbDepartmentBill.setGbDbMonth(formatWhatMonth(0));
        gbDepartmentBill.setGbDbWeek(getWeek(0));
        gbDepartmentBillService.save(gbDepartmentBill);
        for (GbDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            orders.setGbDoStatus(getGbOrderStatusHasBill());
            orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
            orders.setGbDoArriveWeeksYear(getWeekOfYear(0));
            orders.setGbDoArriveWhatDay(getWeek(0));
            orders.setGbDoArriveOnlyDate(formatWhatDate(0));
            orders.setGbDoArriveDate(formatWhatDay(0));
            orders.setGbDoBillId(gbDepartmentBill.getGbDepartmentBillId());
            gbDepartmentOrdersService.update(orders);

            if (orders.getGoodsStockEntityList().size() > 0) {
                for (GbDepartmentGoodsStockEntity stockEntity : orders.getGoodsStockEntityList()) {
                    gbDepartmentGoodsStockService.update(stockEntity);
                }
            }

        }
        return R.ok();
    }


    @RequestMapping(value = "/saveAccountBillGb", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillGb(@RequestBody GbDepartmentBillEntity gbDepartmentBill) {

        List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = gbDepartmentBill.getGbDepartmentOrdersEntities();
        gbDepartmentBill.setGbDbStatus(-1);
        gbDepartmentBill.setGbDbDate(formatWhatDay(0));
        gbDepartmentBill.setGbDbTime(formatWhatYearDayTime(0));
        gbDepartmentBill.setGbDbMonth(formatWhatMonth(0));
        gbDepartmentBill.setGbDbWeek(getWeek(0));
        gbDepartmentBillService.save(gbDepartmentBill);

        for (GbDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            orders.setGbDoStatus(getGbOrderStatusHasBill());
            orders.setGbDoArriveWeeksYear(getWeekOfYear(0));
            orders.setGbDoArriveWhatDay(getWeek(0));
            orders.setGbDoArriveOnlyDate(formatWhatDate(0));
            orders.setGbDoArriveDate(formatWhatDay(0));
            orders.setGbDoBillId(gbDepartmentBill.getGbDepartmentBillId());
            orders.setGbDoWeightGoodsId(null);
            orders.setGbDoWeightTotalId(null);
            gbDepartmentOrdersService.update(orders);

            //3，修改送货单收货单子数量
            if (orders.getGbDoPurchaseGoodsId() != -1) {
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(orders.getGbDoPurchaseGoodsId());
                BigDecimal  billAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersBillAmount());
                BigDecimal add = billAmount.add(new BigDecimal(1));
                purchaseGoodsEntity.setGbDpgOrdersBillAmount(add.intValue());
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }
        return R.ok();
    }


}
