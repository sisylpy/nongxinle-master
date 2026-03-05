package com.nongxinle.controller;

/**
 * @author lpy
 * @date 10-11 17:01
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
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbPurchaseGoodsTypeForOrder;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.ParseObject.myRandom;
import static com.nongxinle.utils.PinYin4jUtils.getHeadStringByString;

//import static com.nongxinle.utils.CommonUtils.generateBillTradeNo;


@RestController
@RequestMapping("api/nxdepartmentbill0000")
public class NxDepartmentBillController000 {
    @Autowired
    private NxDepartmentBillService nxDepartmentBillService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDepartmentOrdersHistoryService nxDepOrdersHistoryService;
    @Autowired
    private NxDepartmentService nxDepartmentService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private NxDistributerGbDistributerService nxDisGbDisService;
    @Autowired
    private NxDistributerGoodsService dgService;
    @Autowired
    private NxDistributerFatherGoodsService distributerFatherGoodsService;
    @Autowired
    private NxDistributerPurchaseGoodsService nxDistributerPurchaseGoodsService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDPBService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;
    @Autowired
    private GbDistributerGoodsPriceService goodsPriceService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;
    @Autowired
    private GbDepartmentGoodsDailyService gbDepGoodsDailyService;
    @Autowired
    private NxJrdhUserService nxJrdhUserService;
    @Autowired
    private NxJrdhSupplierService jrdhSupplierService;
    @Autowired
    private NxDistributerPayListService payListService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    private GbDistributerGoodsPriceService gbDistributerGoodsPriceService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepartmentStockReduceService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDPGService;
    @Autowired
    private GbDistributerPurchaseBatchService gbDistributerPurchaseBatchService;



    @RequestMapping(value = "/saveGbReturn/{id}")
    @ResponseBody
    public R saveGbReturn(@PathVariable Integer id) {
        NxDepartmentBillEntity nxDepartmentBill = nxDepartmentBillService.queryObject(id);
        nxDepartmentBill.setNxDbStatus(0);

        GbDepartmentBillEntity gbDepartmentBill = new GbDepartmentBillEntity();
        gbDepartmentBill.setGbDbIssueNxDisId(nxDepartmentBill.getNxDbDisId());
        gbDepartmentBill.setGbDbDisId(nxDepartmentBill.getNxDbGbDisId());
        gbDepartmentBill.setGbDbDepId(nxDepartmentBill.getNxDbGbDepId());
        gbDepartmentBill.setGbDbTotal(nxDepartmentBill.getNxDbTotal());
        gbDepartmentBill.setGbDbPrintTimes(1);
        gbDepartmentBill.setGbDbIssueOrderType(5);
        gbDepartmentBill.setGbDbOrderAmount(1);
        gbDepartmentBill.setGbDbStatus(0);
        gbDepartmentBill.setGbDbDate(formatWhatDay(0));
        gbDepartmentBill.setGbDbTime(formatWhatYearDayTime(0));
        gbDepartmentBill.setGbDbMonth(formatWhatMonth(0));
        gbDepartmentBill.setGbDbWeek(getWeekOfYear(0).toString());
        gbDepartmentBill.setGbDbDay(getWeek(0));
        gbDepartmentBill.setGbDbTradeNo(nxDepartmentBill.getNxDbTradeNo());
        gbDepartmentBillService.save(gbDepartmentBill);

        nxDepartmentBill.setNxDbGbDepartmentBillId(gbDepartmentBill.getGbDepartmentBillId());
        nxDepartmentBillService.update(nxDepartmentBill);

        Map<String, Object> mapO = new HashMap<>();
        mapO.put("billId", nxDepartmentBill.getNxDepartmentBillId());
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(mapO);
        for (NxDepartmentOrdersEntity nxOrderEntity : ordersEntities) {
            Integer nxDoGbDepartmentOrderId = nxOrderEntity.getNxDoGbDepartmentOrderId();
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(nxDoGbDepartmentOrderId);
            gbDepartmentOrdersEntity.setGbDoBillId(gbDepartmentBill.getGbDepartmentBillId());
            gbDepartmentOrdersEntity.setGbDoStatus(4);
            gbDepartmentOrdersEntity.setGbDoBuyStatus(5);
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);

            Integer purchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();
            GbDistributerPurchaseGoodsEntity disPurGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
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
            gbDistributerPurchaseGoodsService.update(disPurGoodsEntity);

            Integer gbDoDgsrReturnId = gbDepartmentOrdersEntity.getGbDoDgsrReturnId();
            GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepartmentStockReduceService.queryObject(gbDoDgsrReturnId);
            reduceEntity.setGbDgsrStatus(0);
            gbDepartmentStockReduceService.update(reduceEntity);
        }

        Integer nxDbGbDisId = nxDepartmentBill.getNxDbGbDisId();
        Map<String, Object> map = new HashMap<>();
        map.put("type", getGbDepartmentTypeAppSupplier());
        map.put("disId", nxDbGbDisId);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
        gbDepartmentBill.setGbDbIssueDepId(departmentEntities.get(0).getGbDepartmentId());
        gbDepartmentBillService.update(gbDepartmentBill);
        return R.ok();
    }

    /**
     * 京京送货 部门保存订单
     * @param depFatherId
     * @param disId
     * @return
     */
    @RequestMapping(value = "/saveAccountBillPhoneSub", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPhoneSub(Integer depFatherId, Integer disId) {
        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depFatherId);
        if(departmentEntities.size() > 0){
            for(NxDepartmentEntity departmentEntity: departmentEntities){
                Map<String, Object> map = new HashMap<>();
                map.put("depId", departmentEntity.getNxDepartmentId());
                map.put("equalStatus", 2);
                map.put("equalPurStatus", 4);
                System.out.println("susnamap" + map);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                if(integer > 0){
                    Double aDouble = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                    NxDepartmentBillEntity nxDepartmentBill = new NxDepartmentBillEntity();
                    nxDepartmentBill.setNxDbDepId(departmentEntity.getNxDepartmentId());
                    nxDepartmentBill.setNxDbDepFatherId(departmentEntity.getNxDepartmentFatherId());
                    nxDepartmentBill.setNxDbDisId(departmentEntity.getNxDepartmentDisId());
                    nxDepartmentBill.setNxDbTotal(new BigDecimal(aDouble).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
                    nxDepartmentBill.setNxDbStatus(0);
                    nxDepartmentBill.setNxDbDate(formatWhatDay(0));
                    nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
                    nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
                    nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
                    nxDepartmentBill.setNxDbDay(getWeek(0));
                    nxDepartmentBill.setNxDbYear(formatWhatYear(0));
                    nxDepartmentBill.setNxDbGbDisId(-1);
                    nxDepartmentBill.setNxDbGbDepId(-1);
                    nxDepartmentBill.setNxDbGbDepFatherId(-1);
                    NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
                    String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
                    System.out.println("disnamme" + nxDistributerEntity.getNxDistributerName());
                    String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
                    nxDepartmentBill.setNxDbTradeNo(s);
                    nxDepartmentBillService.save(nxDepartmentBill);

                    saveOneSubBill(nxDepartmentBill);

                }

            }
        }


        return R.ok();
    }


    /**
     * 京京送货 gb部门保存订单
     * @param depFatherId
     * @param disId
     * @return
     */
    @RequestMapping(value = "/saveAccountBillPhoneSubGb", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPhoneSubGb(Integer depFatherId, Integer disId) {
        System.out.println("depid======Gb" + depFatherId + "disid" + disId);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(depFatherId);
        if(departmentEntities.size() > 0){
            System.out.println("deppsisiiissisisii" + departmentEntities.size());
            for(GbDepartmentEntity departmentEntity: departmentEntities){
                Map<String, Object> map = new HashMap<>();
                map.put("gbDepId", departmentEntity.getGbDepartmentId());
                map.put("equalStatus", 2);
                map.put("equalPurStatus", 4);
                System.out.println("susnamap" + map);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                if(integer > 0){

                    Double aDouble = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                    NxDepartmentBillEntity nxDepartmentBill = new NxDepartmentBillEntity();
                    nxDepartmentBill.setNxDbDepId(-1);
                    nxDepartmentBill.setNxDbDepFatherId(-1);
                    nxDepartmentBill.setNxDbDisId(disId);
                    nxDepartmentBill.setNxDbGbDepId(departmentEntity.getGbDepartmentId());
                    nxDepartmentBill.setNxDbGbDepFatherId(departmentEntity.getGbDepartmentFatherId());
                    nxDepartmentBill.setNxDbGbDisId(departmentEntity.getGbDepartmentDisId());
                    nxDepartmentBill.setNxDbTotal(new BigDecimal(aDouble).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
                    nxDepartmentBill.setNxDbStatus(0);
                    nxDepartmentBill.setNxDbPrintTimes(0);
                    nxDepartmentBill.setNxDbNxCommunityId(-1);
                    nxDepartmentBill.setNxDbNxRestrauntId(-1);
                    nxDepartmentBill.setNxDbDate(formatWhatDay(0));
                    nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
                    nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
                    nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
                    nxDepartmentBill.setNxDbDay(getWeek(0));
                    nxDepartmentBill.setNxDbYear(formatWhatYear(0));

                    NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
                    String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
                    System.out.println("disnamme" + nxDistributerEntity.getNxDistributerName());
                    String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();

                    System.out.println("savbesub" + nxDepartmentBill);
                    nxDepartmentBill.setNxDbTradeNo(s);
                    nxDepartmentBillService.save(nxDepartmentBill);

                    saveGbBillUpdateOrders(nxDepartmentBill);

                    saveOneSubBillGb(nxDepartmentBill);

                }

            }
        }

        return R.ok();
    }

    private void  saveOneSubBillGb(NxDepartmentBillEntity nxDepartmentBill){

        System.out.println("savesonnsuusbsilllGGGBBB" + nxDepartmentBill);
        BigDecimal billTotal = new BigDecimal(0);
        Map<String, Object> map = new HashMap<>();
        map.put("gbDepFatherId", nxDepartmentBill.getNxDbGbDepFatherId());
        if(!nxDepartmentBill.getNxDbGbDepFatherId().equals(nxDepartmentBill.getNxDbGbDepId())){
            map.put("gbDepId", nxDepartmentBill.getNxDbGbDepId());
        }

        map.put("status",3);
        map.put("equalPurStatus",4);
        map.put("disId", nxDepartmentBill.getNxDbDisId());

        System.out.println("nxoroossoss" + map);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        System.out.println("nxoroossoss" + ordersEntities.size());

        for (NxDepartmentOrdersEntity orders : ordersEntities) {
            //0 subtotal
            billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));
            orders.setNxDoStatus(3);
            orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
            orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
            nxDepartmentOrdersService.update(orders);

            //updata weight
            Integer doDisGoodsId = orders.getNxDoDisGoodsId();
            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
            BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
            BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
            BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
            dgService.update(distributerGoodsEntity);

            Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
            if (nxDoPurchaseGoodsId != -1) {
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                } else {
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                }

                nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        nxDepartmentBill.setNxDbTotal(billTotal.toString());
//        nxDepartmentBill.setNxDbProfitTotal(billProfit.toString());
//        BigDecimal decimal = billProfit.divide(billTotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//        nxDepartmentBill.setNxDbProfitScale(decimal.toString());
        nxDepartmentBillService.update(nxDepartmentBill);


        // 保存 gbBill
        nxDepartmentBill.setNxDepartmentOrdersEntities(ordersEntities);
        saveAccountBillGbDis(nxDepartmentBill);

        Integer nxDbDisId = nxDepartmentBill.getNxDbDisId();
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDbDisId);
        NxDistributerPayListEntity payListEntity = new NxDistributerPayListEntity();
        payListEntity.setNxNdplNxDisId(nxDbDisId);
        payListEntity.setNxNdplNxDepartmentId(-1);
        payListEntity.setNxNdplNxDepartmentFatherId(-1);
        payListEntity.setNxNdplGbDbId(nxDepartmentBill.getNxDbGbDepartmentBillId());
        payListEntity.setNxNdplGbDepartmentId(nxDepartmentBill.getNxDbGbDepId());
        payListEntity.setNxNdplGbDepartmentFatherId(nxDepartmentBill.getNxDbGbDepFatherId());
        int size = ordersEntities.size();
        payListEntity.setNxNdplPaySubtotal(Integer.valueOf(size).toString());
        payListEntity.setNxNdplPayTime(formatFullTime());
        payListEntity.setNxNdplPayDate(formatWhatDay(0));
        payListEntity.setNxNdplPayMonth(formatWhatMonth(0));
        payListEntity.setNxNdplPayYear(formatWhatYear(0));
        payListEntity.setNxNdplStatus(0);
        payListEntity.setNxNdplType(getNxDisPayListWeb());
        payListEntity.setNxNdplRestPoints(nxDistributerEntity.getNxDistributerBuyQuantity());
        payListEntity.setNxNdplNxDbId(nxDepartmentBill.getNxDepartmentBillId());
        payListService.save(payListEntity);

        BigDecimal decimal0 = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());
        BigDecimal decimal1 = new BigDecimal(ordersEntities.size());
        BigDecimal restPoints = decimal0.subtract(decimal1).setScale(0,BigDecimal.ROUND_HALF_UP);
        nxDistributerEntity.setNxDistributerBuyQuantity(restPoints.toString());
        nxDistributerService.update(nxDistributerEntity);

        Integer gbDpbDistributerId = nxDepartmentBill.getNxDbGbDisId();
        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDpbDistributerId);
        BigDecimal decimal = new BigDecimal(gbDistributerEntity.getGbDistributerBuyQuantity());
        BigDecimal add = decimal.subtract(decimal1);
        gbDistributerEntity.setGbDistributerBuyQuantity(add.toString());
        System.out.println("updabbbgbgb" + add);
        gbDistributerService.update(gbDistributerEntity);

    }


    public R saveDepStockDataByPurchase(@RequestBody GbDepartmentOrdersEntity order) {
        System.out.println("upddodididufidfuaisf");
        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
        Integer gbDoDepDisGoodsId = order.getGbDoDepDisGoodsId();
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDoDepDisGoodsId);

        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
        Integer gbDoStatus = ordersEntity.getGbDoStatus();
        //判断没有被别人收货
        System.out.println("eeeeeeststttttttt"+ gbDoStatus);
//        if (gbDoStatus.equals(getGbOrderStatusHasBill())) {
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
        stockEntity.setGbDgsNxSupplierId(-1);
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
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDPGService.queryObject(order.getGbDoPurchaseGoodsId());
            if (purchaseGoodsEntity.getGbDpgWasteFullTime() != null) {
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
                long timestampWarn = 0;
                if (dateWaste != null) {
                    timestampWaste = dateWaste.getTime();
                }
                if (dateWarn != null) {
                    timestampWarn = dateWarn.getTime();
                }
                // 输出时间戳
                stockEntity.setGbDgsWasteTimeQuantumName(String.valueOf(timestampWaste));
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
        stockEntity.setGbDgsStars(5);
        gbDepartmentGoodsStockService.save(stockEntity);


        orderAddDepDisGoods(order, stockEntity, gbDoDepDisGoodsId);
        updateDepGoodsDailyBusiness(stockEntity);

        //2，修改订单状态
        order.setGbDoStatus(getGbOrderStatusReceived());
        gbDepartmentOrdersService.update(order);

        //3，修改送货单收货单子数量
        if (order.getGbDoPurchaseGoodsId() != -1) {
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDPGService.queryObject(order.getGbDoPurchaseGoodsId());
            BigDecimal gbPgOrderAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersAmount());
            BigDecimal gbDbFinishAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersFinishAmount());
            if (gbDbFinishAmount.add(new BigDecimal(1)).compareTo(gbPgOrderAmount) == 0) {
                purchaseGoodsEntity.setGbDpgOrdersFinishAmount(purchaseGoodsEntity.getGbDpgOrdersAmount());
                purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusWaitReceive());
            } else {
                BigDecimal add = gbDbFinishAmount.add(new BigDecimal(1));
                purchaseGoodsEntity.setGbDpgOrdersFinishAmount(add.intValue());
            }
            gbDPGService.update(purchaseGoodsEntity);
        }

        //3，修改送货单收货单子数量
//            Integer gbDoBillId = order.getGbDoBillId();
//            GbDepartmentBillEntity billEntity = gbDepartmentBillService.queryObject(gbDoBillId);
//            BigDecimal gbDbOrderAmount = new BigDecimal(billEntity.getGbDbOrderAmount());
//            if (gbDbOrderAmount.compareTo(new BigDecimal("1")) == 1) {
//                BigDecimal subtract = gbDbOrderAmount.subtract(new BigDecimal("1"));
//                billEntity.setGbDbOrderAmount(subtract.intValue());
//                gbDepartmentBillService.update(billEntity);
//            } else {
//                billEntity.setGbDbOrderAmount(0);
//                billEntity.setGbDbStatus(0);
//                gbDepartmentBillService.update(billEntity);
//            }
        return R.ok();
//        } else {
//            return R.error(-1, "已经完成收货");
//        }

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


    private GbDistributerPurchaseGoodsEntity checkPurGoodsPrice(GbDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        System.out.println("checkkckGoododopriiddcheckPurGoodsPrice" + purchaseGoodsEntity.getGbDpgDisGoodsId());
        Integer gbDpgDisGoodsId = purchaseGoodsEntity.getGbDpgDisGoodsId();
        BigDecimal buyPrice = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
        Integer gbDpgDisGoodsPriceId = purchaseGoodsEntity.getGbDpgDisGoodsPriceId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);

        BigDecimal weight = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
        BigDecimal goodsHighest = new BigDecimal(gbDistributerGoodsEntity.getGbDgGoodsHighestPrice());
        BigDecimal goodsLowest = new BigDecimal(gbDistributerGoodsEntity.getGbDgGoodsLowestPrice());
        String priceTotal = buyPrice.multiply(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString();

        if (buyPrice.compareTo(goodsHighest) == 1 && purchaseGoodsEntity.getGbDpgBuyQuantity() != null) { //高于最高价

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
                goodsPriceEntity.setGbDgpPurNxDistributerId(purchaseGoodsEntity.getGbDpgPurchaseNxDistributerId());
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
                System.out.println("fdhfehfrekfe" + gbDistributerGoodsEntity.getGbDgNxDistributerId());
                gbDistributerGoodsPriceService.save(goodsPriceEntity);
                purchaseGoodsEntity.setGbDpgDisGoodsPriceId(goodsPriceEntity.getGbDistributerGoodsPriceId());
            }
            gbDPGService.update(purchaseGoodsEntity);
        }

        if (buyPrice.compareTo(goodsLowest) == -1 && purchaseGoodsEntity.getGbDpgBuyQuantity() != null) { //低于最低价
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
                goodsPriceEntity.setGbDgpPurNxDistributerId(purchaseGoodsEntity.getGbDpgPurchaseNxDistributerId());
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

            gbDPGService.update(purchaseGoodsEntity);
        }

        if (buyPrice.compareTo(goodsHighest) == -1 && buyPrice.compareTo(goodsLowest) == 1) {

            if (gbDpgDisGoodsPriceId != null) {

                gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);

                purchaseGoodsEntity.setGbDpgBuyPriceReason(null);
                purchaseGoodsEntity.setGbDpgDisGoodsPriceId(null);
                gbDPGService.update(purchaseGoodsEntity);

            }
        }
        if (buyPrice.compareTo(goodsHighest) == 0 || buyPrice.compareTo(goodsLowest) == 0) {
            if (gbDpgDisGoodsPriceId != null) {

                gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);

                purchaseGoodsEntity.setGbDpgBuyPriceReason(null);
                purchaseGoodsEntity.setGbDpgDisGoodsPriceId(null);
                gbDPGService.update(purchaseGoodsEntity);


            }
        }


        return purchaseGoodsEntity;

    }



    private void  saveOneSubBill(NxDepartmentBillEntity nxDepartmentBill){

        System.out.println("savesonnsuusbsilll" + nxDepartmentBill);
        BigDecimal billTotal = new BigDecimal(0);
        BigDecimal billProfit = new BigDecimal(0);

        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDepartmentBill.getNxDbDisId());
        map.put("depFatherId", nxDepartmentBill.getNxDbDepFatherId());
        if(!nxDepartmentBill.getNxDbDepFatherId().equals(nxDepartmentBill.getNxDbDepId())){
            map.put("depId", nxDepartmentBill.getNxDbDepId());
        }
        map.put("equalStatus",2);
        map.put("equalPurStatus",4);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        for (NxDepartmentOrdersEntity orders : ordersEntities) {
            //0 subtotal
            billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));

            //1，配送商自己的客户
            if(orders.getNxDoDepDisGoodsId() != null && orders.getNxDoDepDisGoodsId() != -1){
                Integer nxDoDepDisGoodsId = checkDepDisGoods(orders);
                orders.setNxDoDepDisGoodsId(nxDoDepDisGoodsId);
            }else{
                System.out.println("new Depdiiddid");
                NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(orders.getNxDoDisGoodsId());
                String nxDgGoodsName = nxDistributerGoodsEntity.getNxDgGoodsName();
                NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
                disGoodsEntity.setNxDdgDepGoodsName(nxDgGoodsName);
                disGoodsEntity.setNxDdgDisGoodsId(orders.getNxDoDisGoodsId());
                disGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
                disGoodsEntity.setNxDdgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());

                NxDistributerFatherGoodsEntity fatherGoodsEntity = distributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                Integer greatFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
                disGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);

                disGoodsEntity.setNxDdgDepGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
                disGoodsEntity.setNxDdgDepGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
                disGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
                disGoodsEntity.setNxDdgDepartmentId(orders.getNxDoDepartmentId());
                disGoodsEntity.setNxDdgDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                disGoodsEntity.setNxDdgNxDistributerId(orders.getNxDoDistributerId());
                //orderData
                disGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
                disGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
                disGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
                disGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
                disGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
                disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
                nxDepartmentDisGoodsService.save(disGoodsEntity);
                orders.setNxDoDepDisGoodsId(disGoodsEntity.getNxDepartmentDisGoodsId());
            }


            orders.setNxDoStatus(3);
            orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
            orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
            nxDepartmentOrdersService.update(orders);

            //updata weight
            Integer doDisGoodsId = orders.getNxDoDisGoodsId();
            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
            BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
            BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
            BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
            dgService.update(distributerGoodsEntity);

            //2. 添加订货历史
            NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
            historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
            historyEntity.setNxDohDepDisGoodsId(orders.getNxDoDepDisGoodsId());
            historyEntity.setNxDohDisGoodsId(orders.getNxDoDisGoodsId());
            historyEntity.setNxDohDistributerId(orders.getNxDoDistributerId());
            historyEntity.setNxDohQuantity(orders.getNxDoQuantity());
            historyEntity.setNxDohStandard(orders.getNxDoStandard());
            historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
            historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
            historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
            historyEntity.setNxDohOrderTimes(1);
            historyEntity.setNxDohOrder(orders.getNxDoTodayOrder());
            nxDepOrdersHistoryService.save(historyEntity);


            Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
            if (nxDoPurchaseGoodsId != -1) {
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                } else {
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                }
                nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        nxDepartmentBill.setNxDbTotal(billTotal.toString());
        nxDepartmentBill.setNxDbProfitTotal(billProfit.toString());
        BigDecimal decimal = billProfit.divide(billTotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
        nxDepartmentBill.setNxDbProfitScale(decimal.toString());
        nxDepartmentBillService.update(nxDepartmentBill);

        NxDepartmentEntity departmentEntity = nxDepartmentService.queryDepInfo(nxDepartmentBill.getNxDbDepId());
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String texiansongCaigouAppId = myAPPIDConfig.getTexiansongCaigouAppId();

        System.out.println("appidiidid"+ departmentEntity.getNxDepartmentAppId());
//        if (departmentEntity.getNxDepartmentAppId().equals(texiansongCaigouAppId)) {
            System.out.println("appidiidid"+ texiansongCaigouAppId);
            Integer nxDbDisId = nxDepartmentBill.getNxDbDisId();
            NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDbDisId);
            NxDistributerPayListEntity payListEntity = new NxDistributerPayListEntity();
            payListEntity.setNxNdplNxDisId(nxDbDisId);
            payListEntity.setNxNdplNxDepartmentId(nxDepartmentBill.getNxDbDepId());
            payListEntity.setNxNdplNxDepartmentFatherId(nxDepartmentBill.getNxDbDepFatherId());
            int size = ordersEntities.size();
            payListEntity.setNxNdplPaySubtotal(Integer.valueOf(size).toString());
            payListEntity.setNxNdplPayTime(formatFullTime());
            payListEntity.setNxNdplPayDate(formatWhatDay(0));
            payListEntity.setNxNdplPayMonth(formatWhatMonth(0));
            payListEntity.setNxNdplPayYear(formatWhatYear(0));
            payListEntity.setNxNdplStatus(0);
            payListEntity.setNxNdplType(getNxDisPayListWeb());
            payListEntity.setNxNdplRestPoints(nxDistributerEntity.getNxDistributerBuyQuantity());
            payListEntity.setNxNdplNxDbId(nxDepartmentBill.getNxDepartmentBillId());
            payListService.save(payListEntity);

            BigDecimal decimal0 = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());
            BigDecimal decimal1 = new BigDecimal(ordersEntities.size());
            BigDecimal restPoints = decimal0.subtract(decimal1).setScale(0,BigDecimal.ROUND_HALF_UP);
            nxDistributerEntity.setNxDistributerBuyQuantity(restPoints.toString());
            nxDistributerService.update(nxDistributerEntity);
//        }

    }


    /**
     * 京京送货 gb门店保存
     * @param nxDepartmentBill
     * @return
     */
    @RequestMapping(value = "/saveAccountBillPhoneGb", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPhoneGb(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {

        System.out.println("prinsislGGGGGGGGGGG");
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBillService.save(nxDepartmentBill);

        saveOneSubBillGb(nxDepartmentBill);


        return R.ok();
    }


    /**
     * 京京送货 门店保存
     * @param
     * @return
     */
    @RequestMapping(value = "/saveAccountBillPhone", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPhone(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {
        System.out.println("savephoneacoucnccucnncn");
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBill.setNxDbGbDisId(-1);
        nxDepartmentBill.setNxDbGbDepId(-1);
        nxDepartmentBill.setNxDbGbDepFatherId(-1);
        nxDepartmentBillService.save(nxDepartmentBill);

        saveOneSubBill(nxDepartmentBill);

        return R.ok();
    }


    /**
     * 自助打印机门店保存
     * @param gbDepFatherId
     * @param gbDepId
     * @param tradeNo
     * @param userId
     * @param nxDisId
     * @return
     */
    @RequestMapping(value = "/saveAccountBillPrinterGb", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPrinterGb(Integer gbDepFatherId,Integer gbDepId, String tradeNo, Integer userId, Integer nxDisId) {

        System.out.println("whwhhwwhw" + gbDepFatherId);
        System.out.println("whwhhwwhw" + gbDepId);

        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(gbDepFatherId);
        Integer gbDepartmentDisId = departmentEntity.getGbDepartmentDisId();
        GbDepartmentBillEntity gbDepartmentBill = new GbDepartmentBillEntity();
        gbDepartmentBill.setGbDbDepId(gbDepId);
        gbDepartmentBill.setGbDbDepFatherId(gbDepFatherId);
        gbDepartmentBill.setGbDbDisId(gbDepartmentDisId);
        gbDepartmentBill.setGbDbIssueNxDisId(nxDisId);
        gbDepartmentBill.setGbDbIssueOrderType(5);
        gbDepartmentBill.setGbDbStatus(0);
        gbDepartmentBill.setGbDbDate(formatWhatDay(0));
        gbDepartmentBill.setGbDbTime(formatWhatYearDayTime(0));
        gbDepartmentBill.setGbDbMonth(formatWhatMonth(0));
        gbDepartmentBill.setGbDbWeek(getWeekOfYear(0).toString());
        gbDepartmentBill.setGbDbDay(getWeek(0));
        gbDepartmentBill.setGbDbYear(formatWhatYear(0));
        gbDepartmentBill.setGbDbTradeNo(tradeNo);
        gbDepartmentBill.setGbDbPrintTimes(1);
        gbDepartmentBill.setGbDbSellingTotal("0");
        Map<String, Object> map = new HashMap<>();
        map.put("type", getGbDepartmentTypeAppSupplier());
        map.put("disId", gbDepartmentDisId);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
        gbDepartmentBill.setGbDbIssueDepId(departmentEntities.get(0).getGbDepartmentId());
        BigDecimal billTotal = new BigDecimal(0);
        Map<String, Object> nxMap = new HashMap<>();
        nxMap.put("gbDepFatherId", gbDepFatherId);
        if(!gbDepFatherId.equals(gbDepId)){
            nxMap.put("gbDepId", gbDepId);
        }
        nxMap.put("status", 3);
        nxMap.put("disId", nxDisId);
        System.out.println("niamamammamammam" + nxMap);
        Double aDouble = nxDepartmentOrdersService.queryDepOrdersSubtotal(nxMap);
        gbDepartmentBill.setGbDbTotal(new BigDecimal(aDouble).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
        gbDepartmentBillService.save(gbDepartmentBill);

        NxDepartmentBillEntity nxDepartmentBill = new NxDepartmentBillEntity();
        String ss = generateBillTradeNo(gbDepartmentBill.getGbDbTradeNo());
        nxDepartmentBill.setNxDbTradeNo(ss);
        nxDepartmentBill.setNxDbDisId(nxDisId);
        nxDepartmentBill.setNxDbDepId(-1);
        nxDepartmentBill.setNxDbDepFatherId(-1);
        nxDepartmentBill.setNxDbTotal(gbDepartmentBill.getGbDbTotal());
        nxDepartmentBill.setNxDbPrintTimes(1);
        nxDepartmentBill.setNxDbGbDisId(gbDepartmentBill.getGbDbDisId());
        nxDepartmentBill.setNxDbGbDepId(gbDepartmentBill.getGbDbDepId());
        nxDepartmentBill.setNxDbGbDepFatherId(gbDepartmentBill.getGbDbDepFatherId());
        nxDepartmentBill.setNxDbNxCommunityId(-1);
        nxDepartmentBill.setNxDbNxRestrauntId(-1);
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(gbDepartmentBill.getGbDbDate());
        nxDepartmentBill.setNxDbTime(gbDepartmentBill.getGbDbTime());
        nxDepartmentBill.setNxDbMonth(gbDepartmentBill.getGbDbMonth());
        nxDepartmentBill.setNxDbWeek(gbDepartmentBill.getGbDbYear());
        nxDepartmentBill.setNxDbDay(gbDepartmentBill.getGbDbDay());
        nxDepartmentBill.setNxDbYear(gbDepartmentBill.getGbDbYear());
        nxDepartmentBill.setNxDbProfitScale("0");
        nxDepartmentBill.setNxDbProfitTotal("0");
        nxDepartmentBill.setNxDbIssueUserId(userId);
        nxDepartmentBill.setNxDbGbDepartmentBillId(gbDepartmentBill.getGbDepartmentBillId());
        nxDepartmentBillService.save(nxDepartmentBill);


        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(nxMap);

        if(ordersEntities.size() > 0){
            for (NxDepartmentOrdersEntity orders : ordersEntities) {
                //0 subtotal
                billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));

                orders.setNxDoStatus(3);
                orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
                orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
                nxDepartmentOrdersService.update(orders);

//                Integer nxDoGbDepartmentOrderId = orders.getNxDoGbDepartmentOrderId();
//
//                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(nxDoGbDepartmentOrderId);
//                gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusReceived());
//                gbDepartmentOrdersEntity.setGbDoBuyStatus(3);
//                gbDepartmentOrdersEntity.setGbDoArriveWeeksYear(getWeekOfYear(0));
//                gbDepartmentOrdersEntity.setGbDoArriveWhatDay(getWeek(0));
//                gbDepartmentOrdersEntity.setGbDoArriveOnlyDate(formatWhatDate(0));
//                gbDepartmentOrdersEntity.setGbDoArriveDate(formatWhatDay(0));
//                gbDepartmentOrdersEntity.setGbDoWeight(orders.getNxDoWeight());
//                gbDepartmentOrdersEntity.setGbDoSubtotal(orders.getNxDoSubtotal());
//                gbDepartmentOrdersEntity.setGbDoPrice(orders.getNxDoPrice());
//                gbDepartmentOrdersEntity.setGbDoBillId(gbDepartmentBill.getGbDepartmentBillId());
//                gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
//

//                Integer gbDoPurchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();
//                if (gbDoPurchaseGoodsId != -1) {
//                    GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
//                    System.out.println("putodiididididiididi" + purchaseGoodsEntity.getGbDpgOrdersFinishAmount());
//                    Integer nxDpgOrdersAmount = purchaseGoodsEntity.getGbDpgOrdersAmount();
//                    Integer nxDpgFinishAmount = purchaseGoodsEntity.getGbDpgOrdersFinishAmount();
//                    if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
//                        purchaseGoodsEntity.setGbDpgOrdersFinishAmount(nxDpgOrdersAmount);
//                        purchaseGoodsEntity.setGbDpgStatus(getNxDisPurchaseGoodsFinishPay());
//                    } else {
//                        purchaseGoodsEntity.setGbDpgOrdersFinishAmount(nxDpgFinishAmount + 1);
//                    }
//                    gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
//                }


                //updata weight
                Integer doDisGoodsId = orders.getNxDoDisGoodsId();
                NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
                BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
                BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
                BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
                dgService.update(distributerGoodsEntity);

                Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
                if (nxDoPurchaseGoodsId != -1) {
                    NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                    Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                    Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                    if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                        purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                    } else {
                        purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                    }
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                }
            }
        }

// 保存 gbBill
        nxDepartmentBill.setNxDepartmentOrdersEntities(ordersEntities);
        saveAccountBillGbDis(nxDepartmentBill);


        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDisId);
        BigDecimal disBuyQuantity = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());

        final int ORDERS_PER_SHEET = 24;
        final int SHEET_PRICE = 24;

//        int sheetCount = (int) Math.ceil((double) ordersEntities.size() / ORDERS_PER_SHEET);
//        System.out.println("kanakankayigongduoshs" + sheetCount);
//        int total = sheetCount * SHEET_PRICE;
        BigDecimal restQuantity = disBuyQuantity.subtract(new BigDecimal(ordersEntities.size())).setScale(0,BigDecimal.ROUND_HALF_UP);

        NxDistributerPayListEntity payListEntity = new NxDistributerPayListEntity();
        payListEntity.setNxNdplNxDisId(nxDisId);
        payListEntity.setNxNdplGbDepartmentId(gbDepartmentBill.getGbDbDepId());
        payListEntity.setNxNdplGbDepartmentFatherId(gbDepartmentBill.getGbDbDepFatherId());
        payListEntity.setNxNdplPaySubtotal(String.valueOf(String.valueOf(ordersEntities.size())));

        payListEntity.setNxNdplPayTime(formatFullTime());
        payListEntity.setNxNdplPayDate(formatWhatDay(0));
        payListEntity.setNxNdplPayMonth(formatWhatMonth(0));
        payListEntity.setNxNdplPayYear(formatWhatYear(0));
        payListEntity.setNxNdplStatus(0);
        payListEntity.setNxNdplType(getNxDisPayListPrinter());
        payListEntity.setNxNdplRestPoints(nxDistributerEntity.getNxDistributerBuyQuantity());
        payListEntity.setNxNdplGbDbId(gbDepartmentBill.getGbDepartmentBillId());
        payListService.save(payListEntity);

        nxDistributerEntity.setNxDistributerBuyQuantity(restQuantity.toString());
        System.out.println("dispayddyd" + restQuantity);
        nxDistributerService.update(nxDistributerEntity);

        return R.ok();
    }

    /**
     * 自助打印机 gb 门店保存
     * @param depFatherId
     * @param depId
     * @param tradeNo
     * @param userId
     * @return
     */
    @RequestMapping(value = "/saveAccountBillPrinter", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPrinter(Integer depFatherId,Integer depId, String tradeNo, Integer userId) {

        System.out.println("whwhhwwhw" + depFatherId);
        System.out.println("whwhhwwhw" + depId);

        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depId);
        Integer nxDepartmentDisId = departmentEntity.getNxDepartmentDisId();
        NxDepartmentBillEntity nxDepartmentBill = new NxDepartmentBillEntity();
        nxDepartmentBill.setNxDbDepId(depId);
        nxDepartmentBill.setNxDbDepFatherId(depFatherId);
        nxDepartmentBill.setNxDbDisId(nxDepartmentDisId);
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        if(!depFatherId.equals(depId)){
            map.put("depId", depId);
        }
        map.put("status", 3);
        Double aDouble = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        nxDepartmentBill.setNxDbTotal(new BigDecimal(aDouble).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBill.setNxDbTradeNo(tradeNo);
        nxDepartmentBill.setNxDbPrintTimes(1);
        nxDepartmentBill.setNxDbGbDisId(-1);
        nxDepartmentBill.setNxDbGbDepId(-1);
        nxDepartmentBill.setNxDbGbDepFatherId(-1);
        nxDepartmentBill.setNxDbNxRestrauntId(-1);
        nxDepartmentBill.setNxDbNxCommunityId(-1);
        nxDepartmentBill.setNxDbIssueUserId(userId);
        nxDepartmentBillService.save(nxDepartmentBill);

        BigDecimal billTotal = new BigDecimal(0);
        BigDecimal billProfit = new BigDecimal(0);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);

        if(ordersEntities.size() > 0){
            for (NxDepartmentOrdersEntity orders : ordersEntities) {
                //0 subtotal
                billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));

                //1，配送商自己的客户
                Integer nxDoDepDisGoodsId = checkDepDisGoods(orders);
                orders.setNxDoDepDisGoodsId(nxDoDepDisGoodsId);
                orders.setNxDoStatus(3);
                orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
                orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());

                nxDepartmentOrdersService.update(orders);

                //updata weight
                Integer doDisGoodsId = orders.getNxDoDisGoodsId();
                NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
                BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
                BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
                BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
                dgService.update(distributerGoodsEntity);

                //2. 添加订货历史
                NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
                historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
                historyEntity.setNxDohDepDisGoodsId(nxDoDepDisGoodsId);
                historyEntity.setNxDohDisGoodsId(orders.getNxDoDisGoodsId());
                historyEntity.setNxDohDistributerId(orders.getNxDoDistributerId());
                historyEntity.setNxDohQuantity(orders.getNxDoQuantity());
                historyEntity.setNxDohStandard(orders.getNxDoStandard());
                historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
                historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
                historyEntity.setNxDohOrderTimes(1);
                historyEntity.setNxDohOrder(orders.getNxDoTodayOrder());
                nxDepOrdersHistoryService.save(historyEntity);

                Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
                if (nxDoPurchaseGoodsId != -1) {
                    NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                    Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                    Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                    if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                        purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                    } else {
                        purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                    }
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                }
            }
        }


        nxDepartmentBill.setNxDbTotal(billTotal.toString());
        nxDepartmentBill.setNxDbProfitTotal(billProfit.toString());
        BigDecimal decimal = billProfit.divide(billTotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
        nxDepartmentBill.setNxDbProfitScale(decimal.toString());
        nxDepartmentBillService.update(nxDepartmentBill);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDepartmentDisId);
        BigDecimal disBuyQuantity = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());

        final int ORDERS_PER_SHEET = 24;
        final int SHEET_PRICE = 24;


//        int sheetCount = (int) Math.ceil((double) ordersEntities.size() / ORDERS_PER_SHEET);
//        System.out.println("kanakankayigongduoshs" + sheetCount);
//        int total = sheetCount * SHEET_PRICE;
        BigDecimal restQuantity = disBuyQuantity.subtract(new BigDecimal(ordersEntities.size())).setScale(0,BigDecimal.ROUND_HALF_UP);

        NxDistributerPayListEntity payListEntity = new NxDistributerPayListEntity();
        payListEntity.setNxNdplNxDisId(nxDepartmentDisId);
        payListEntity.setNxNdplNxDepartmentId(nxDepartmentBill.getNxDbDepId());
        payListEntity.setNxNdplNxDepartmentFatherId(nxDepartmentBill.getNxDbDepFatherId());
        payListEntity.setNxNdplPaySubtotal(String.valueOf(ordersEntities.size()));

        payListEntity.setNxNdplPayTime(formatFullTime());
        payListEntity.setNxNdplPayDate(formatWhatDay(0));
        payListEntity.setNxNdplPayMonth(formatWhatMonth(0));
        payListEntity.setNxNdplPayYear(formatWhatYear(0));
        payListEntity.setNxNdplStatus(0);
        payListEntity.setNxNdplType(getNxDisPayListPrinter());
        payListEntity.setNxNdplRestPoints(nxDistributerEntity.getNxDistributerBuyQuantity());
        payListEntity.setNxNdplNxDbId(nxDepartmentBill.getNxDepartmentBillId());
        payListService.save(payListEntity);

        nxDistributerEntity.setNxDistributerBuyQuantity(restQuantity.toString());
        System.out.println("dispayddyd" + restQuantity);
        nxDistributerService.update(nxDistributerEntity);



        return R.ok();
    }





    @RequestMapping(value = "/disGetUnPayAccountBills/{id}")
    @ResponseBody
    public R disGetUnPayAccountBills(@PathVariable Integer id) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", id);
        map.put("settleType", 0);
        map.put("equalStatus", 0);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(map);
        return R.ok().put("data", billEntityList);
    }


    @ResponseBody
    @RequestMapping(value = "/restrauntCashPayLaodu", method = RequestMethod.POST)
    public R restrauntCashPayLaodu(@RequestBody NxDepartmentBillEntity billEntity) {
        System.out.println("billl" + billEntity);

        //转换总金额
        String nxRbTotal = billEntity.getNxDbTotal();
        Double aDouble = Double.parseDouble(nxRbTotal) * 100;
        int i = aDouble.intValue();
        String s1 = String.valueOf(i);

        //订单号
        String tradeNo = CommonUtils.generateOutTradeNo();
        //餐馆支付配置
        MyWxLaoduPayConfig config = new MyWxLaoduPayConfig();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxdepartmentbill/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", billEntity.getNxUserOpenId());

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

            billEntity.setNxDbWxOutTradeNo(tradeNo);
            nxDepartmentBillService.update(billEntity);

            return R.ok().put("map", reMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/restrauntCashPay", method = RequestMethod.POST)
    public R restrauntCashPay(@RequestBody NxDepartmentBillEntity billEntity) {

        Integer nxDbDisId = billEntity.getNxDbDisId();
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDbDisId);
        String nxDistributerPayUrl = nxDistributerEntity.getNxDistributerPayUrl();

        System.out.println("billl" + billEntity);

        //转换总金额
        String nxRbTotal = billEntity.getNxDbTotal();
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
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxdepartmentbill/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", billEntity.getNxUserOpenId());

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

            billEntity.setNxDbWxOutTradeNo(tradeNo);
            nxDepartmentBillService.update(billEntity);

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
                NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryDepartBillByTradeNo(ordersSn);
                billEntity.setNxDbStatus(4);
                nxDepartmentBillService.update(billEntity);

                Integer nxDbDepId = billEntity.getNxDbDepId();
                NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(nxDbDepId);

                savePromotion(departmentEntity);

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

    private void savePromotion(NxDepartmentEntity departmentEntity) {
        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(departmentEntity.getNxDepartmentPromotionGoodsId());

        NxDepartmentOrdersEntity ordersEntity = new NxDepartmentOrdersEntity();
        ordersEntity.setNxDoDepartmentId(departmentEntity.getNxDepartmentId());
        ordersEntity.setNxDoDepartmentFatherId(departmentEntity.getNxDepartmentId());
        ordersEntity.setNxDoArriveOnlyDate(formatWhatDate(0));
        ordersEntity.setNxDoDistributerId(departmentEntity.getNxDepartmentDisId());
        ordersEntity.setNxDoDisGoodsId(departmentEntity.getNxDepartmentPromotionGoodsId());
        ordersEntity.setNxDoQuantity("5");
        ordersEntity.setNxDoPrice("0");
        ordersEntity.setNxDoSubtotal("0");
        ordersEntity.setNxDoStatus(0);
        ordersEntity.setNxDoWeight("5");
        ordersEntity.setNxDoStandard(distributerGoodsEntity.getNxDgGoodsStandardname());
        ordersEntity.setNxDoNxGoodsId(distributerGoodsEntity.getNxDgNxGoodsId());
        ordersEntity.setNxDoNxGoodsFatherId(distributerGoodsEntity.getNxDgNxFatherId());
        ordersEntity.setNxDoProfitSubtotal("0");
        ordersEntity.setNxDoPurchaseStatus(1);
        ordersEntity.setNxDoPurchaseGoodsId(distributerGoodsEntity.getNxDgPurchaseAuto());
        ordersEntity.setNxDoCostPrice(distributerGoodsEntity.getNxDgBuyingPrice());
        ordersEntity.setNxDoCostPriceUpdate(distributerGoodsEntity.getNxDgBuyingPriceUpdate());
        BigDecimal decimal = new BigDecimal(distributerGoodsEntity.getNxDgBuyingPrice());
        BigDecimal decimal1 = new BigDecimal(ordersEntity.getNxDoWeight());
        BigDecimal decimal2 = decimal1.multiply(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
        ordersEntity.setNxDoCostSubtotal(decimal2.toString());
        ordersEntity.setNxDoProfitSubtotal("-" + decimal2.toString());
        ordersEntity.setNxDoProfitScale("0");
        ordersEntity.setNxDoDisGoodsGrandId(distributerGoodsEntity.getNxDgDfgGoodsGrandId());
        ordersEntity.setNxDoTodayOrder(1);
        ordersEntity.setNxDoIsAgent(-1);
        ordersEntity.setNxDoPrintStandard(distributerGoodsEntity.getNxDgGoodsStandardname());
        ordersEntity.setNxDoGoodsType(distributerGoodsEntity.getNxDgPurchaseAuto());


        ordersEntity.setNxDoApplyDate(formatWhatDay(0));
        ordersEntity.setNxDoArriveOnlyDate(formatWhatDate(0));
        ordersEntity.setNxDoArriveWeeksYear(getWeekOfYear(0));
        ordersEntity.setNxDoApplyFullTime(formatFullTime());
        ordersEntity.setNxDoApplyOnlyTime(formatWhatTime(0));
        ordersEntity.setNxDoArriveDate(formatWhatDay(0));
        ordersEntity.setNxDoGbDistributerId(-1);
        ordersEntity.setNxDoGbDepartmentId(-1);
        ordersEntity.setNxDoGbDepartmentFatherId(-1);
        ordersEntity.setNxDoNxCommunityId(-1);
        ordersEntity.setNxDoNxCommRestrauntFatherId(-1);
        ordersEntity.setNxDoNxCommRestrauntId(-1);
        ordersEntity.setNxDoDisGoodsFatherId(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
        ordersEntity.setNxDoDepDisGoodsId(-1);
        ordersEntity.setNxDoArriveWhatDay(getWeek(0));
//        ordersEntity.setNxDoCostPriceLevel("0");
        ordersEntity.setNxDoPriceDifferent("0.0");
        System.out.println("savvdororororororooro");
        nxDepartmentOrdersService.save(ordersEntity);

        //auto
        System.out.println("pudiididididipppppp" + distributerGoodsEntity.getNxDgPurchaseAuto());
        if (distributerGoodsEntity.getNxDgPurchaseAuto() != -1) {
            savePurGoodsAuto(ordersEntity);
        }

        Integer integer = checkDepDisGoods(ordersEntity);
        ordersEntity.setNxDoDepDisGoodsId(integer);
        nxDepartmentOrdersService.update(ordersEntity);

    }


    private void savePurGoodsAuto(NxDepartmentOrdersEntity ordersEntity) {


        Integer nxDistributerPurchaseGoodsId = 0;
        //判断是否有已经分的

        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
        NxDistributerGoodsEntity disGoods = dgService.queryObject(doDisGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", doDisGoodsId);
        map.put("equalStatus", 0);
        NxDistributerPurchaseGoodsEntity havePurGoods = nxDistributerPurchaseGoodsService.queryIfHavePurGoods(map);
        if (havePurGoods != null) {
            havePurGoods.setNxDpgOrdersAmount(havePurGoods.getNxDpgOrdersAmount() + 1);
            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                BigDecimal decimal = new BigDecimal(havePurGoods.getNxDpgQuantity());
                BigDecimal decimal1 = new BigDecimal(ordersEntity.getNxDoQuantity());
                BigDecimal totaoWeight = decimal.add(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(havePurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                havePurGoods.setNxDpgQuantity(totaoWeight.toString());
                havePurGoods.setNxDpgBuyQuantity(totaoWeight.toString());
                havePurGoods.setNxDpgBuySubtotal(decimal2.toString());
            }

            nxDistributerPurchaseGoodsService.update(havePurGoods);
            nxDistributerPurchaseGoodsId = havePurGoods.getNxDistributerPurchaseGoodsId();

        } else {

            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = new NxDistributerPurchaseGoodsEntity();
            purchaseGoodsEntity.setNxDpgDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
            purchaseGoodsEntity.setNxDpgDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
            purchaseGoodsEntity.setNxDpgDistributerId(disGoods.getNxDgDistributerId());
//            purchaseGoodsEntity.setNxDpgCostLevel(disGoods.getNxDgBuyingPriceIsGrade());
            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
            purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
            purchaseGoodsEntity.setNxDpgOrdersAmount(1);
            purchaseGoodsEntity.setNxDpgFinishAmount(0);
            purchaseGoodsEntity.setNxDpgPurchaseType(getGbPurchaseGoodsTypeForOrder());
            purchaseGoodsEntity.setNxDpgExpectPrice(disGoods.getNxDgBuyingPrice());
            purchaseGoodsEntity.setNxDpgBuyPrice(disGoods.getNxDgBuyingPrice());
            purchaseGoodsEntity.setNxDpgDisGoodsId(doDisGoodsId);
            purchaseGoodsEntity.setNxDpgInputType(disGoods.getNxDgPurchaseAuto());
            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
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
                System.out.println("enenenbebbebeebbebeb" + map);
                List<NxDistributerPurchaseBatchEntity> entities = nxDPBService.queryDisPurchaseBatch(mapBatch);
                System.out.println("enenenbebbebeebbebeb" + entities.size());

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
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                }
            }

        }
        ordersEntity.setNxDoPurchaseStatus(getNxDisPurchaseGoodsIsPurchase());
        ordersEntity.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
        nxDepartmentOrdersService.update(ordersEntity);


    }


    @RequestMapping(value = "/comReceivedepBill", method = RequestMethod.POST)
    @ResponseBody
    public R comReceivedepBill(@RequestBody NxDepartmentBillEntity bill) {
        bill.setNxDbStatus(0);
        nxDepartmentBillService.update(bill);
        return R.ok();
    }


    @RequestMapping(value = "/deleteBillReturn/{id}")
    @ResponseBody
    public R deleteBillReturn(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("returnBillId", id);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        for (NxDepartmentOrdersEntity orders : ordersEntities) {
            orders.setNxDoReturnWeight(null);
            orders.setNxDoReturnSubtotal(null);
            orders.setNxDoReturnStatus(null);
            nxDepartmentOrdersService.update(orders);
        }

        nxDepartmentBillService.delete(id);
        return R.ok();
    }
    //

    @RequestMapping(value = "/deleteBillAgain/{billId}")
    @ResponseBody
    public R deleteBillAgain(@PathVariable Integer billId) {

        //order
        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        for (NxDepartmentOrdersEntity orders : ordersEntities) {
            orders.setNxDoStatus(2);
            orders.setNxDoBillId(null);
            nxDepartmentOrdersService.update(orders);
        }

        nxDepartmentBillService.delete(billId);
        return R.ok();

    }


    @RequestMapping(value = "/deleteBill/{billId}")
    @ResponseBody
    public R deleteBill(@PathVariable Integer billId) {

        NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryObject(billId);
        Integer depId = billEntity.getNxDbDepId();
        Map<String, Object> mapO = new HashMap<>();
        mapO.put("depId", depId);
        mapO.put("status", 3);
        List<NxDepartmentOrdersEntity> ordersEntities1 = nxDepartmentOrdersService.queryDisOrdersByParams(mapO);
        if (ordersEntities1.size() > 0) {
            return R.error(-1, "删除账单的订单将和现有订单混在一起");
        } else {
            //order
            Map<String, Object> map = new HashMap<>();
            map.put("billId", billId);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
            for (NxDepartmentOrdersEntity orders : ordersEntities) {
                orders.setNxDoStatus(2);
                orders.setNxDoBillId(null);
                nxDepartmentOrdersService.update(orders);
            }

            nxDepartmentBillService.delete(billId);
            return R.ok();
        }

    }


    @RequestMapping(value = "/finishBill/{billId}")
    @ResponseBody
    public R finishBill(@PathVariable Integer billId) {
        //order
        NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryObject(billId);
        GbDepartmentBillEntity gbDepartmentBillEntity = gbDepartmentBillService.queryDepartBillByTsxTradeNo(billEntity.getNxDbTradeNo());
        gbDepartmentBillEntity.setGbDbStatus(4);
        gbDepartmentBillService.update(gbDepartmentBillEntity);

        return R.ok();
    }


    @RequestMapping(value = "/printBillMoreTimes/{billId}")
    @ResponseBody
    public R printBillMoreTimes(@PathVariable Integer billId) {
        NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryObject(billId);
        billEntity.setNxDbPrintTimes(billEntity.getNxDbPrintTimes() + 1);
        nxDepartmentBillService.update(billEntity);
        return R.ok();
    }



    @RequestMapping(value = "/getBillApplysDetail/{billId}")
    @ResponseBody
    public R getBillApplysDetail(@PathVariable Integer billId) {

        System.out.println("getBillApplysDetail" + billId);
        NxDepartmentBillEntity billEntity = nxDepartmentBillService.querySalesBillApplys(billId);
        return R.ok().put("data", billEntity);
    }

    @RequestMapping(value = "/getReturnBillApplys/{billId}")
    @ResponseBody
    public R getReturnBillApplys(@PathVariable Integer billId) {

        System.out.println("rerrururuuuruur" + billId);
        NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryReturnBillOrdersByBillId(billId);
        return R.ok().put("data", billEntity);
    }


    @RequestMapping(value = "/saveAccountReturnBill", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountReturnBill(@RequestBody NxDepartmentBillEntity bill) {
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(bill.getNxDbDisId());
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        bill.setNxDbStatus(0);
        bill.setNxDbDate(formatWhatDay(0));
        bill.setNxDbTime(formatWhatYearDayTime(0));
        bill.setNxDbMonth(formatWhatMonth(0));
        bill.setNxDbWeek(getWeekOfYear(0).toString());
        bill.setNxDbDay(getWeek(0));
        bill.setNxDbTradeNo(s);
        bill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBillService.save(bill);
        Integer nxDepartmentBillId = bill.getNxDepartmentBillId();

        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = bill.getNxDepartmentOrdersEntities();

        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            orders.setNxDoReturnBillId(nxDepartmentBillId);
            orders.setNxDoReturnStatus(1);
            nxDepartmentOrdersService.update(orders);
        }

        return R.ok();
    }

    /**
     * 结账
     *
     * @param bills
     * @return
     */
    @RequestMapping(value = "/settleDepBills", method = RequestMethod.POST)
    @ResponseBody
    public R settleDepBills(@RequestBody List<NxDepartmentBillEntity> bills) {
        for (NxDepartmentBillEntity bill : bills) {
            bill.setNxDbStatus(1);
            nxDepartmentBillService.update(bill);
            Map<String, Object> map = new HashMap<>();
            map.put("billId", bill.getNxDepartmentBillId());
            map.put("depId", bill.getNxDbDepFatherId());
            map.put("disId", bill.getNxDbDisId());
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
            if (ordersEntities.size() > 0) {
                for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                    ordersEntity.setNxDoStatus(4);
                    nxDepartmentOrdersService.update(ordersEntity);
                }
            }
        }
        return R.ok();
    }

    @RequestMapping(value = "/settleDepBillsGb", method = RequestMethod.POST)
    @ResponseBody
    public R settleDepBillsGb(@RequestBody List<NxDepartmentBillEntity> bills) {
        for (NxDepartmentBillEntity bill : bills) {
            NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryObject(bill.getNxDepartmentBillId());

            billEntity.setNxDbStatus(1);
            nxDepartmentBillService.update(bill);
            Map<String, Object> map = new HashMap<>();
            map.put("billId", billEntity.getNxDepartmentBillId());
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
            if (ordersEntities.size() > 0) {
                for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                    ordersEntity.setNxDoStatus(4);
                    nxDepartmentOrdersService.update(ordersEntity);
                }
            }
            //updateGbBill
            Integer nxDbGbDepartmentBillId = bill.getNxDbGbDepartmentBillId();
            GbDepartmentBillEntity gbDepartmentBillEntity = gbDepartmentBillService.queryObject(nxDbGbDepartmentBillId);
            gbDepartmentBillEntity.setGbDbStatus(1);
            gbDepartmentBillService.update(gbDepartmentBillEntity);
            Map<String, Object> mapGO = new HashMap<>();
            mapGO.put("billId", gbDepartmentBillEntity.getGbDepartmentBillId());
            List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(mapGO);
            if(gbDepartmentOrdersEntities.size() > 0){
                for(GbDepartmentOrdersEntity gbDepartmentOrdersEntity: gbDepartmentOrdersEntities){
                    gbDepartmentOrdersEntity.setGbDoStatus(4);
                    gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
                }
            }

        }
        return R.ok();
    }

    /**
     * 批发商获取未结账账单
     *
     * @param depId kehu_id
     * @return 订单列表
     */
    @RequestMapping(value = "/disGetUnSettleAccountBills/{depId}")
    @ResponseBody
    public R disGetUnSettleAccountBills(@PathVariable Integer depId) {

        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();


        map.put("depFatherId", depId);
        map.put("month", formatWhatMonth(0));
        map.put("year", formatWhatYear(0));
        map.put("status", 1);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapOne = new HashMap<>();
        mapOne.put("month", formatWhatMonth(0));
        mapOne.put("year", formatWhatYear(0));
        mapOne.put("arr", billEntityList);
        mapOne.put("choice", false);
        list.add(mapOne);

        map.put("month", getLastMonth());
        List<NxDepartmentBillEntity> billEntityListLast = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapLst = new HashMap<>();
        mapLst.put("month", getLastMonth());
        mapLst.put("arr", billEntityListLast);
        mapLst.put("choice", false);
        list.add(mapLst);


        map.put("depFatherId", depId);
        map.put("equalStatus", 0);
        map.put("month", getLastTwoMonth());

        List<NxDepartmentBillEntity> billEntityListTwo = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapTwo = new HashMap<>();
        mapTwo.put("month", getLastTwoMonth());
        mapTwo.put("arr", billEntityListTwo);
        mapTwo.put("choice", false);
        list.add(mapTwo);

        System.out.println("billlls");
        return R.ok().put("data", list);

    }

    @RequestMapping(value = "/disGetUnSettleAccountBillsGb",method = RequestMethod.POST)
    @ResponseBody
    public R disGetUnSettleAccountBillsGb(Integer gbDisId, Integer nxDisId) {

        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        map.put("disId", nxDisId);
        map.put("gbDisId", gbDisId);
        map.put("month", formatWhatMonth(0));
        map.put("year", formatWhatYear(0));
        map.put("status", 1);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapOne = new HashMap<>();
        mapOne.put("month", formatWhatMonth(0));
        mapOne.put("year", formatWhatYear(0));
        mapOne.put("arr", billEntityList);
        mapOne.put("choice", false);
        list.add(mapOne);

        map.put("month", getLastMonth());
        List<NxDepartmentBillEntity> billEntityListLast = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapLst = new HashMap<>();
        mapLst.put("month", getLastMonth());
        mapLst.put("arr", billEntityListLast);
        mapLst.put("choice", false);
        list.add(mapLst);


        map.put("equalStatus", 0);
        map.put("month", getLastTwoMonth());

        List<NxDepartmentBillEntity> billEntityListTwo = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapTwo = new HashMap<>();
        mapTwo.put("month", getLastTwoMonth());
        mapTwo.put("arr", billEntityListTwo);
        mapTwo.put("choice", false);
        list.add(mapTwo);

        System.out.println("billlls");
        return R.ok().put("data", list);

    }


    @RequestMapping(value = "/gbDisGetAllAccountBillsGb", method = RequestMethod.POST)
    @ResponseBody
    public R gbDisGetAllAccountBillsGb(Integer gbDisId, Integer disId, Integer nxCommId, Integer nxDepFatherId) {

        Map<String, Object> stringObjectMap = disQueryAccountBillByMonthGb(disId, gbDisId, nxCommId, nxDepFatherId, 2);
        Map<String, Object> lastObjectMap = disQueryAccountBillByMonthGb(disId, gbDisId, nxCommId, nxDepFatherId,1);
        Map<String, Object> lastTwoObjectMap = disQueryAccountBillByMonthGb(disId, gbDisId, nxCommId, nxDepFatherId,0);
        List<Map<String, Object>> dataMap = new ArrayList<>();
        dataMap.add(lastTwoObjectMap);
        dataMap.add(lastObjectMap);
        dataMap.add(stringObjectMap);


        //查询总账款金额
//
        //本月的账单
        Map<String, Object> map = new HashMap<>();
        map.put("nx_DB_dis_id", disId);
        map.put("nx_DB_gb_dis_id", gbDisId);
        map.put("nx_DB_nx_community_id", nxCommId);
        map.put("nx_DB_status", 0);

        Map<String, Object> mapz = new HashMap<>();
        mapz.put("nx_DB_dis_id", disId);
        mapz.put("nx_DB_gb_dis_id", -1);
        mapz.put("nx_DB_nx_community_id", -1);
        mapz.put("nx_DB_dep_father_id", nxDepFatherId);
        mapz.put("nx_DB_status", 0);
        Map<String, Object> params = new HashMap<>();
        params.put("map", map);
        params.put("map1", mapz);
        int total  = nxDepartmentBillService.queryCountBindMap(params);
        double subtotal = 0.0;
        if (total > 0) {
            subtotal = nxDepartmentBillService.querySubtoalBindMap(params);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("arr", dataMap);
        resultMap.put("total", new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        return R.ok().put("data", resultMap);
    }

    @RequestMapping(value = "/sellerAndBuyerGetAccountBillsGb", method = RequestMethod.POST)
    @ResponseBody
    public R sellerAndBuyerGetAccountBillsGb(Integer gbDisId, Integer disId, Integer nxCommId) {

        Map<String, Object> stringObjectMap = queryAccountBillByMonthGb(disId, gbDisId, nxCommId, 0);
        Map<String, Object> lastObjectMap = queryAccountBillByMonthGb(disId, gbDisId, nxCommId, 1);
        Map<String, Object> lastTwoObjectMap = queryAccountBillByMonthGb(disId, gbDisId, nxCommId, 2);
        List<Map<String, Object>> dataMap = new ArrayList<>();
        dataMap.add(lastTwoObjectMap);
        dataMap.add(lastObjectMap);
        dataMap.add(stringObjectMap);


        //查询总账款金额
        Map<String, Object> map = new HashMap<>();
        map.put("gbDisId", gbDisId);
        map.put("disId", disId);
        map.put("nxCommId", nxCommId);
        map.put("equalStatus", 0);
        System.out.println("subtma" + map);
        int total = nxDepartmentBillService.queryTotalByParams(map);
        Double subtotal = 0.0;
        if (total > 0) {
            subtotal = nxDepartmentBillService.queryBillCostSubtotalByParams(map);
//            subtotal = nxDepartmentBillService.queryTotal(map);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("arr", dataMap);
        resultMap.put("total", new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        return R.ok().put("data", resultMap);
    }


    @RequestMapping(value = "/sellerAndBuyerGetAccountBills", method = RequestMethod.POST)
    @ResponseBody
    public R sellerAndBuyerGetAccountBills(Integer depFatherId, Integer disId) {
        System.out.println("getabilllss");
        Map<String, Object> lastTwoObjectMap = queryAccountBillByMonth(disId, depFatherId, 2);
        Map<String, Object> lastObjectMap = queryAccountBillByMonth(disId, depFatherId, 1);
        Map<String, Object> stringObjectMap = queryAccountBillByMonth(disId, depFatherId, 0);

        List<Map<String, Object>> dataMap = new ArrayList<>();
        dataMap.add(stringObjectMap);
        dataMap.add(lastObjectMap);
        dataMap.add(lastTwoObjectMap);


        //查询总账款金额
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("equalStatus", 0);
        map.put("dayuMonth", getLastTwoMonth());
        map.put("xiaoyuMonth", formatWhatMonth(0));
        System.out.println("mappsoosososoosso" + map);
        int wxCountAuto = nxDepartmentBillService.queryBillsCount(map);
        double aDouble = 0.0;
        if(wxCountAuto > 0){
             aDouble =  nxDepartmentBillService.queryBillSubtotalByParams(map);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("arr", dataMap);
        resultMap.put("total", new BigDecimal(aDouble).setScale(1,BigDecimal.ROUND_HALF_UP));
        return R.ok().put("data", resultMap);
    }


//    @RequestMapping(value = "/getBillApplysGbDep")
//    @ResponseBody
//    public R getBillApplysGbDep(Integer billId, Integer depFatherId) {
//
//        //billRetrunNumber
//        Integer count = nxDepartmentBillService.queryReturnNumberByBillId(billId);
//
//        NxDepartmentBillEntity salesBill = nxDepartmentBillService.querySalesBillApplys(billId);
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("gbDepFatherId", depFatherId);
//        map.put("billId", billId);
//        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
//
//        if(salesBill.getNxDbGbDepId().equals(salesBill.getNxDbGbDepFatherId())){
//            List<Map<String, Object>> mapList = new ArrayList<>();
//            List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(depFatherId);
//
//            if (entities.size() > 0) {
//                for (GbDepartmentEntity dep : entities) {
//                    Map<String, Object> mapDep = new HashMap<>();
//                    mapDep.put("gbDepId", dep.getGbDepartmentId());
//                    mapDep.put("depName", dep.getGbDepartmentName());
//                    Map<String, Object> map1 = new HashMap<>();
//                    map1.put("billId", billId);
//                    map1.put("gbDepId", dep.getGbDepartmentId());
//
//                    System.out.println("mapp111111" + map1);
//                    List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
//                    mapDep.put("depOrders", depOrders);
//                    if (depOrders.size() > 0) {
//                        mapList.add(mapDep);
//                    }
//                }
//
//                Map<String, Object> map3 = new HashMap<>();
//                map3.put("arr", mapList);
//                map3.put("bill", salesBill);
//                map3.put("returnNumber", count);
//                return R.ok().put("data", map3);
//            }else{
//                Map<String, Object> map4 = new HashMap<>();
//                map4.put("arr", ordersEntities);
//                map4.put("bill", salesBill);
//                map4.put("returnNumber", count);
//                return R.ok().put("data", map4);
//            }
//        }else {
//            Map<String, Object> map4 = new HashMap<>();
//            map4.put("arr", ordersEntities);
//            map4.put("bill", salesBill);
//            map4.put("returnNumber", count);
//            return R.ok().put("data", map4);
//        }
//
//    }

    @RequestMapping(value = "/getBillApplysGb")
    @ResponseBody
    public R getBillApplysGb(Integer billId, Integer depFatherId) {

        //billRetrunNumber
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("billId", billId);
        mapR.put("equalReturnStatus", 0);
        double toReturnSubtotal = 0.0;
        Integer count = nxDepartmentOrdersService.queryReturnOrderCount(mapR);
        if (count > 0) {
            toReturnSubtotal = nxDepartmentOrdersService.queryReturnSubtotal(mapR);
        }
        double haveReturnSubtotal = 0.0;
        mapR.put("equalReturnStatus", 1);
        Integer countR = nxDepartmentOrdersService.queryReturnOrderCount(mapR);
        if (countR > 0) {
            haveReturnSubtotal = nxDepartmentOrdersService.queryReturnSubtotal(mapR);
        }

        System.out.println("gbbgbgbgorororooror" + depFatherId);

        NxDepartmentBillEntity salesBill = nxDepartmentBillService.querySalesBillApplys(billId);

        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
        map.put("orderBy", "time");
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);

        List<Map<String, Object>> mapList = new ArrayList<>();
        List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(depFatherId);

        System.out.println("gbdnenen" + entities.size());
        if (entities.size() > 0) {
            for (GbDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("gbDepId", dep.getGbDepartmentId());
                mapDep.put("depName", dep.getGbDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("billId", billId);
                map1.put("gbDepId", dep.getGbDepartmentId());
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);

                mapDep.put("depOrders", depOrders);

                if (depOrders.size() > 0) {
                    mapList.add(mapDep);
                }
            }

            Map<String, Object> map3 = new HashMap<>();
            map3.put("arr", mapList);
            map3.put("bill", salesBill);
            map3.put("returnNumber", count);
            map3.put("toReturnSubtotal", toReturnSubtotal);
            map3.put("haveReturnSubtotal", haveReturnSubtotal);

            return R.ok().put("data", map3);
        } else {
            Map<String, Object> map4 = new HashMap<>();
            map4.put("arr", ordersEntities);
            map4.put("bill", salesBill);
            map4.put("returnNumber", count);
            map4.put("toReturnSubtotal", toReturnSubtotal);
            map4.put("haveReturnSubtotal", haveReturnSubtotal);

            return R.ok().put("data", map4);
        }


    }


    @RequestMapping(value = "/getBillApplys")
    @ResponseBody
    public R getBillApplys(Integer billId, Integer depFatherId) {

        //billRetrunNumber
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("billId", billId);
        mapR.put("equalReturnStatus", 0);
        double toReturnSubtotal = 0.0;
        Integer count = nxDepartmentOrdersService.queryReturnOrderCount(mapR);
        if (count > 0) {
            toReturnSubtotal = nxDepartmentOrdersService.queryReturnSubtotal(mapR);
        }
        double haveReturnSubtotal = 0.0;
        mapR.put("equalReturnStatus", 1);
        Integer countR = nxDepartmentOrdersService.queryReturnOrderCount(mapR);
        if (countR > 0) {
            haveReturnSubtotal = nxDepartmentOrdersService.queryReturnSubtotal(mapR);
        }


        NxDepartmentBillEntity salesBill = nxDepartmentBillService.querySalesBillApplys(billId);

        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
        map.put("orderBy", "time");
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);

        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);

        if (entities.size() > 0) {
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("billId", billId);
                map1.put("depId", dep.getNxDepartmentId());
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);

                mapDep.put("depOrders", depOrders);

                if (depOrders.size() > 0) {
                    mapList.add(mapDep);
                }
            }

            Map<String, Object> map3 = new HashMap<>();
            map3.put("arr", mapList);
            map3.put("bill", salesBill);
            map3.put("returnNumber", count);
            map3.put("toReturnSubtotal", toReturnSubtotal);
            map3.put("haveReturnSubtotal", haveReturnSubtotal);

            return R.ok().put("data", map3);
        } else {
            Map<String, Object> map4 = new HashMap<>();
            map4.put("arr", ordersEntities);
            map4.put("bill", salesBill);
            map4.put("returnNumber", count);
            map4.put("toReturnSubtotal", toReturnSubtotal);
            map4.put("haveReturnSubtotal", haveReturnSubtotal);

            return R.ok().put("data", map4);
        }


    }


    private Map<String, Object> disQueryAccountBillByMonthGb(Integer disId, Integer gbDisId, Integer nxCommId, Integer nxDepFatherId,int which) {

        LocalDate today = LocalDate.now();
        today = today.minusMonths(which);
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM");
        String format = formatters.format(today);
        //本月的账单
        Map<String, Object> map = new HashMap<>();
        map.put("nx_DB_dis_id", disId);
        map.put("nx_DB_gb_dis_id", gbDisId);
        map.put("nx_DB_nx_community_id", nxCommId);
        map.put("nx_DB_month", format);

        Map<String, Object> mapz = new HashMap<>();
        mapz.put("nx_DB_dis_id", disId);
        mapz.put("nx_DB_gb_dis_id", -1);
        mapz.put("nx_DB_nx_community_id", -1);
        mapz.put("nx_DB_month", format);
        if(nxDepFatherId !=-1){
            mapz.put("nx_DB_dep_father_id", nxDepFatherId);
        }else{
            mapz.put("nx_DB_dep_father_id", nxDepFatherId);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("map", map);
        params.put("map1", mapz);
        System.out.println("gbdiidiidsaaalistlist" + map);

        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBindMap(params);
        System.out.println("billist" + billEntityList.size());

        //本月的未结账单数量
        map.put("nx_DB_status", 0);
        mapz.put("nx_DB_status", 0);
        Map<String, Object> paramsUn = new HashMap<>();
        paramsUn.put("map", map);
        paramsUn.put("map1", mapz);
        System.out.println("gbdiidiidsaaa" + map);
        int unSettle  = nxDepartmentBillService.queryCountBindMap(paramsUn);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("arr", billEntityList);
        dataMap.put("unSettleTotal",unSettle );
        dataMap.put("month", format);

        return dataMap;
    }

    private Map<String, Object> queryAccountBillByMonthGb(Integer disId, Integer gbDisId, Integer nxCommId,int which) {

        LocalDate today = LocalDate.now();
        today = today.minusMonths(which);
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM");
        String format = formatters.format(today);
        //本月的账单
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("gbDisId", gbDisId);
        map.put("nxCommId", nxCommId);
        map.put("month", format);
        map.put("status", 3);
        System.out.println("gbgbbgbgbgbgbgbg" + map);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryGbDepBillsByParams(map);

        //本月的未结账单数量
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("gbDisId", gbDisId);
        map1.put("nxCommId", nxCommId);
        map1.put("equalStatus", 0);
        map1.put("month", format);
        double whichMonthUnSettleTotal = 0.0;
        int wxCountAuto = nxDepartmentBillService.queryBillsCount(map1);
        if(wxCountAuto > 0){
            whichMonthUnSettleTotal =   nxDepartmentBillService.queryBillSubtotalByParams(map1);
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("arr", billEntityList);
        dataMap.put("unSettleTotal", whichMonthUnSettleTotal);
        dataMap.put("month", format);

        return dataMap;
    }

    private Map<String, Object> queryAccountBillByMonth(Integer disId, Integer depFatherId, int which) {

        LocalDate today = LocalDate.now();
        today = today.minusMonths(which);
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM");
        DateTimeFormatter year = DateTimeFormatter.ofPattern("yyyy");
        String format = formatters.format(today);
        System.out.println("formaaaa" + format);
        String yearString = year.format(today);
        //本月的账单
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("month", format);
        map.put("year", yearString);
        map.put("status", 3);
        System.out.println("yeeeyeyee" + map);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(map);

        //本月的未结账单数量
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depFatherId", depFatherId);
        map1.put("equalStatus", 0);
        map1.put("month", format);
        map1.put("year", yearString);
        System.out.println("whaaiaimap" + map);
        int wxCountAuto = nxDepartmentBillService.queryBillsCount(map1);
        Double aDouble = 0.0;
        if(wxCountAuto > 0){
            aDouble  = nxDepartmentBillService.queryBillSubtotalByParams(map1);
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("arr", billEntityList);
        dataMap.put("unSettleTotal", new BigDecimal(aDouble).setScale(1,BigDecimal.ROUND_HALF_UP));
        dataMap.put("month", format);

        return dataMap;
    }


    /**
     * 现金账单
     *
     * @param disId
     * @return
     */
    @RequestMapping(value = "/sellerAndBuyerGetSalesBills", method = RequestMethod.POST)
    @ResponseBody
    public R sellerAndBuyerGetSalesBills(Integer depFatherId, Integer disId) {

        Map<String, Object> stringObjectMap = querySalesBillByMonth(disId, depFatherId, 0);
        Map<String, Object> lastObjectMap = querySalesBillByMonth(disId, depFatherId, 1);
        Map<String, Object> lastTwoObjectMap = querySalesBillByMonth(disId, depFatherId, 2);
        List<Map<String, Object>> dataMap = new ArrayList<>();
        dataMap.add(lastTwoObjectMap);
        dataMap.add(lastObjectMap);
        dataMap.add(stringObjectMap);
        return R.ok().put("data", dataMap);

    }

    private Map<String, Object> querySalesBillByMonth(Integer disId, Integer depFatherId, int which) {

        LocalDate today = LocalDate.now();
        today = today.minusMonths(which);
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM");
        String format = formatters.format(today);
        //本月的账单
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depId", depFatherId);
        map.put("month", format);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(map);
        int total = nxDepartmentBillService.queryTotalByParams(map);
        Double aDouble = 0.0;
        if (total > 0) {
            aDouble = nxDepartmentBillService.queryBillSubtotalByParams(map);
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("arr", billEntityList);
        dataMap.put("month", format);
        dataMap.put("total", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP));

        return dataMap;
    }


    private NxDepartmentBillEntity saveSubDepBill(Integer depFatherId, Integer depId, Integer disId) {
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("status", 3);
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        NxDepartmentBillEntity nxDepartmentBill = new NxDepartmentBillEntity();
        nxDepartmentBill.setNxDbDepFatherId(depFatherId);
        nxDepartmentBill.setNxDbTradeNo(s);
        nxDepartmentBill.setNxDbDepId(depId);
        nxDepartmentBill.setNxDbDisId(disId);
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBillService.save(nxDepartmentBill);


        BigDecimal billTotal = new BigDecimal(0);
        BigDecimal billProfit = new BigDecimal(0);
        BigDecimal costTotal = new BigDecimal(0);
        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {

            if (orders.getNxDoSubtotal() != null && new BigDecimal(orders.getNxDoSubtotal()).compareTo(BigDecimal.ZERO) == 1) {
                //0 subtotal
                billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));
                billProfit = billProfit.add(new BigDecimal(orders.getNxDoProfitSubtotal()));
                costTotal = costTotal.add(new BigDecimal(orders.getNxDoCostSubtotal()));

            }

            //
            Integer nxDoDepDisGoodsId = checkDepDisGoods(orders);
            orders.setNxDoDepDisGoodsId(nxDoDepDisGoodsId);
            Integer nxDoDistributerId = orders.getNxDoDistributerId();

//updata weight
            Integer doDisGoodsId = orders.getNxDoDisGoodsId();
            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
            BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
            BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
            BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
            dgService.update(distributerGoodsEntity);
            //
            //增加订货历史
            Map<String, Object> map1 = new HashMap<>();
            map1.put("depDisGoodsId", nxDoDepDisGoodsId);
            map1.put("depId", orders.getNxDoDepartmentId());
            List<NxDepartmentOrdersHistoryEntity> historyEntities = nxDepOrdersHistoryService.queryDepHistoryOrdersByParams(map1);
            String orderQuantity = "";
            String orderStandard = "";
            String orderStr = "";
            orderQuantity = orders.getNxDoQuantity();
            orderStandard = orders.getNxDoStandard();

            orderStr = orderQuantity + orderStandard;

            //如果有4个以内的历史记录
            if (historyEntities.size() > 0 && historyEntities.size() < 4) {

                int equalNumber = 0;
                for (NxDepartmentOrdersHistoryEntity orderHistory : historyEntities) {
                    String historyStr = orderHistory.getNxDohQuantity() + orderHistory.getNxDohStandard();
                    if (orderStr.equals(historyStr)) {
                        equalNumber = equalNumber + 1;
                    }
                }
                if (equalNumber == 0 && historyEntities.size() < 3) {
                    //添加新的
                    NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
                    historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
                    historyEntity.setNxDohDepDisGoodsId(nxDoDepDisGoodsId);
                    historyEntity.setNxDohDisGoodsId(doDisGoodsId);
                    historyEntity.setNxDohDistributerId(nxDoDistributerId);
                    historyEntity.setNxDohQuantity(orderQuantity);
                    historyEntity.setNxDohStandard(orderStandard);
                    historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
                    historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                    historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
                    nxDepOrdersHistoryService.save(historyEntity);
                } else if (equalNumber == 0 && historyEntities.size() == 3) {
                    //删除最早一个
                    NxDepartmentOrdersHistoryEntity first = historyEntities.get(0);
                    Integer nxRestrauntOrdersHistoryId = first.getNxDepartmentOrdersHistoryId();
                    nxDepOrdersHistoryService.delete(nxRestrauntOrdersHistoryId);
                    //添加新的
                    NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
                    historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
                    historyEntity.setNxDohDepDisGoodsId(nxDoDepDisGoodsId);
                    historyEntity.setNxDohDisGoodsId(doDisGoodsId);
                    historyEntity.setNxDohDistributerId(nxDoDistributerId);
                    historyEntity.setNxDohQuantity(orderQuantity);
                    historyEntity.setNxDohStandard(orderStandard);
                    historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
                    historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                    historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
                    nxDepOrdersHistoryService.save(historyEntity);
                }

            } else {
                //添加新的
                NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
                historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
                historyEntity.setNxDohDepDisGoodsId(nxDoDepDisGoodsId);
                historyEntity.setNxDohDisGoodsId(doDisGoodsId);
                historyEntity.setNxDohDistributerId(nxDoDistributerId);
                historyEntity.setNxDohQuantity(orderQuantity);
                historyEntity.setNxDohStandard(orderStandard);
                historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
                historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
                nxDepOrdersHistoryService.save(historyEntity);
            }


            orders.setNxDoStatus(3);
            orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
            orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
            nxDepartmentOrdersService.update(orders);

            Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
            if (nxDoPurchaseGoodsId != -1) {
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                } else {
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                }
                nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        nxDepartmentBill.setNxDbCostTotal(costTotal.toString());
        nxDepartmentBill.setNxDbTotal(billTotal.toString());
        nxDepartmentBill.setNxDbProfitTotal(billProfit.toString());
        if (billTotal.compareTo(BigDecimal.ZERO) == 1) {
            BigDecimal decimal = billProfit.divide(billTotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            nxDepartmentBill.setNxDbProfitScale(decimal.toString());
            nxDepartmentBillService.update(nxDepartmentBill);
        }

        return nxDepartmentBill;
    }








    private void updateDepDisGoods(GbDepartmentOrdersEntity ordersEntity, GbDepartmentGoodsStockEntity stockEntity, Integer depDisGoodsId, String what) {

        BigDecimal stockSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());
        BigDecimal stockWeight = new BigDecimal(stockEntity.getGbDgsWeight());
        BigDecimal subTotal = new BigDecimal(0);
        BigDecimal weight = new BigDecimal(0);
        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        if (what.equals("add")) {
            subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).add(stockSubtotal);
            weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).add(stockWeight);
            //updateOrder
            depDisGoodsEntity.setGbDdgOrderDate(formatWhatDay(0));
            depDisGoodsEntity.setGbDdgOrderPrice(ordersEntity.getGbDoPrice());
            depDisGoodsEntity.setGbDdgOrderQuantity(ordersEntity.getGbDoQuantity());
            depDisGoodsEntity.setGbDdgOrderRemark(ordersEntity.getGbDoRemark());
            depDisGoodsEntity.setGbDdgOrderStandard(ordersEntity.getGbDoStandard());
            depDisGoodsEntity.setGbDdgOrderWeight(ordersEntity.getGbDoWeight());
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

    public void saveGbBillUpdateOrders(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {

        System.out.println("dfadkfaslfjsalfjdlasfjalsfjdalsfjdasl");
        Map<String, Object> mapO = new HashMap<>();
        mapO.put("gbDepId", nxDepartmentBill.getNxDbGbDepId());
        mapO.put("status", 3);
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(mapO);

        GbDepartmentBillEntity gbDepartmentBill = new GbDepartmentBillEntity();
        gbDepartmentBill.setGbDbIssueNxDisId(nxDepartmentBill.getNxDbDisId());
        gbDepartmentBill.setGbDbDisId(nxDepartmentBill.getNxDbGbDisId());
        gbDepartmentBill.setGbDbDepId(nxDepartmentBill.getNxDbGbDepId());
        gbDepartmentBill.setGbDbDepFatherId(nxDepartmentBill.getNxDbGbDepFatherId());
        gbDepartmentBill.setGbDbYear(formatWhatYear(0));
        gbDepartmentBill.setGbDbTotal(nxDepartmentBill.getNxDbTotal());
        gbDepartmentBill.setGbDbPrintTimes(0);
        gbDepartmentBill.setGbDbIssueOrderType(5);
        gbDepartmentBill.setGbDbOrderAmount(nxDepartmentOrdersEntities.size());
        gbDepartmentBill.setGbDbStatus(0);
        gbDepartmentBill.setGbDbDate(formatWhatDay(0));
        gbDepartmentBill.setGbDbTime(formatWhatYearDayTime(0));
        gbDepartmentBill.setGbDbMonth(formatWhatMonth(0));
        gbDepartmentBill.setGbDbWeek(getWeekOfYear(0).toString());
        gbDepartmentBill.setGbDbDay(getWeek(0));
        gbDepartmentBill.setGbDbTradeNo(nxDepartmentBill.getNxDbTradeNo());
        Map<String, Object> mapg = new HashMap<>();
        mapg.put("nxDisId", nxDepartmentBill.getNxDbDisId());
        mapg.put("gbDisId", nxDepartmentBill.getNxDbGbDisId());
        NxDistributerGbDistributerEntity entity = nxDisGbDisService.queryObjectByParams(mapg);
        Integer nxDgdGbPayMethod = entity.getNxDgdGbPayMethod();
        if (nxDgdGbPayMethod == 1) {
            Integer nxDgdGbPayPeriodWeek = entity.getNxDgdGbPayPeriodWeek();
            String willPayDate = getWillPayDate(nxDgdGbPayPeriodWeek, Calendar.WEDNESDAY);
            gbDepartmentBill.setGbDbWillPayDate(willPayDate);
        } else {
            gbDepartmentBill.setGbDbWillPayDate(formatWhatDate(1));
        }

        gbDepartmentBillService.save(gbDepartmentBill);

        nxDepartmentBill.setNxDbGbDepartmentBillId(gbDepartmentBill.getGbDepartmentBillId());
        nxDepartmentBillService.update(nxDepartmentBill);

        BigDecimal sellSubtotal = new BigDecimal(0);

        for (NxDepartmentOrdersEntity nxOrderEntity : nxDepartmentOrdersEntities) {
            Integer nxDoGbDepartmentOrderId = nxOrderEntity.getNxDoGbDepartmentOrderId();
            //update weight
            Integer doDisGoodsId = nxOrderEntity.getNxDoDisGoodsId();
            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
            BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
            BigDecimal orderWeight = new BigDecimal(nxOrderEntity.getNxDoWeight());
            BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
            dgService.update(distributerGoodsEntity);

            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(nxDoGbDepartmentOrderId);
            gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusReceived());
            gbDepartmentOrdersEntity.setGbDoBuyStatus(3);
            gbDepartmentOrdersEntity.setGbDoArriveWeeksYear(getWeekOfYear(0));
            gbDepartmentOrdersEntity.setGbDoArriveWhatDay(getWeek(0));
            gbDepartmentOrdersEntity.setGbDoArriveOnlyDate(formatWhatDate(0));
            gbDepartmentOrdersEntity.setGbDoArriveDate(formatWhatDay(0));
            gbDepartmentOrdersEntity.setGbDoWeight(nxOrderEntity.getNxDoWeight());
            gbDepartmentOrdersEntity.setGbDoSubtotal(nxOrderEntity.getNxDoSubtotal());
            gbDepartmentOrdersEntity.setGbDoPrice(nxOrderEntity.getNxDoPrice());
            gbDepartmentOrdersEntity.setGbDoBillId(gbDepartmentBill.getGbDepartmentBillId());
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
            System.out.println("putodiididididiididi" + gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId());

            Integer nxDoPurchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();
            if (nxDoPurchaseGoodsId != -1) {
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                System.out.println("putodiididididiididi" + purchaseGoodsEntity.getGbDpgOrdersFinishAmount());
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getGbDpgOrdersAmount();
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getGbDpgOrdersFinishAmount();
                if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                    purchaseGoodsEntity.setGbDpgOrdersFinishAmount(nxDpgOrdersAmount);
                    purchaseGoodsEntity.setGbDpgStatus(getNxDisPurchaseGoodsFinishPay());
                    Integer gbDpgBatchId = purchaseGoodsEntity.getGbDpgBatchId();

                    GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDistributerPurchaseBatchService.queryObject(gbDpgBatchId);
                    gbDistributerPurchaseBatchEntity.setGbDpbStatus(getGbDisPurchaseBatchDisUserWaitReceive());

                } else {
                    purchaseGoodsEntity.setGbDpgOrdersFinishAmount(nxDpgFinishAmount + 1);
                }



                GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDepartmentOrdersEntity.getGbDoDisGoodsId());

            if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
                checkPurGoodsPrice(purchaseGoodsEntity);
            }
              //  判断是否有保鲜时间参数
            if (gbDistributerGoodsEntity.getGbDgControlFresh() != null && gbDistributerGoodsEntity.getGbDgControlFresh() == 1) {
                int wasteHour = Integer.parseInt(gbDistributerGoodsEntity.getGbDgFreshWasteHour());
                purchaseGoodsEntity.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
            }
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }

        }
        gbDepartmentBill.setGbDbSellingTotal(sellSubtotal.toString());
        Integer nxDbGbDisId = nxDepartmentBill.getNxDbGbDisId();
        Map<String, Object> map = new HashMap<>();
        map.put("type", getGbDepartmentTypeAppSupplier());
        map.put("disId", nxDbGbDisId);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);

        gbDepartmentBill.setGbDbIssueDepId(departmentEntities.get(0).getGbDepartmentId());
        gbDepartmentBillService.update(gbDepartmentBill);

    }

    public void saveAccountBillGbDis(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {

        System.out.println("dfadkfaslfjsalfjdlasfjalsfjdalsfjdaslgbgbgbgbgb");
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentBill.getNxDepartmentOrdersEntities();
        GbDepartmentBillEntity gbDepartmentBill = new GbDepartmentBillEntity();
        gbDepartmentBill.setGbDbIssueNxDisId(nxDepartmentBill.getNxDbDisId());
        gbDepartmentBill.setGbDbDisId(nxDepartmentBill.getNxDbGbDisId());
        gbDepartmentBill.setGbDbDepId(nxDepartmentBill.getNxDbGbDepId());
        gbDepartmentBill.setGbDbTotal(nxDepartmentBill.getNxDbTotal());
        gbDepartmentBill.setGbDbPrintTimes(1);
        gbDepartmentBill.setGbDbIssueOrderType(5);
        gbDepartmentBill.setGbDbOrderAmount(nxDepartmentOrdersEntities.size());
        gbDepartmentBill.setGbDbStatus(-2);
        gbDepartmentBill.setGbDbDate(formatWhatDay(0));
        gbDepartmentBill.setGbDbTime(formatWhatYearDayTime(0));
        gbDepartmentBill.setGbDbMonth(formatWhatMonth(0));
        gbDepartmentBill.setGbDbWeek(getWeekOfYear(0).toString());
        gbDepartmentBill.setGbDbDay(getWeek(0));
        gbDepartmentBill.setGbDbTradeNo(nxDepartmentBill.getNxDbTradeNo());
        Map<String, Object> mapg = new HashMap<>();
        mapg.put("nxDisId", nxDepartmentBill.getNxDbDisId());
        mapg.put("gbDisId", nxDepartmentBill.getNxDbGbDisId());
        NxDistributerGbDistributerEntity entity = nxDisGbDisService.queryObjectByParams(mapg);
        Integer nxDgdGbPayMethod = entity.getNxDgdGbPayMethod();
        if (nxDgdGbPayMethod == 1) {
            Integer nxDgdGbPayPeriodWeek = entity.getNxDgdGbPayPeriodWeek();
            String willPayDate = getWillPayDate(nxDgdGbPayPeriodWeek, Calendar.WEDNESDAY);
            gbDepartmentBill.setGbDbWillPayDate(willPayDate);
        } else {
            gbDepartmentBill.setGbDbWillPayDate(formatWhatDate(1));
        }

        gbDepartmentBillService.save(gbDepartmentBill);

        BigDecimal sellSubtotal = new BigDecimal(0);

        for (NxDepartmentOrdersEntity nxOrderEntity : nxDepartmentOrdersEntities) {
            Integer nxDoGbDepartmentOrderId = nxOrderEntity.getNxDoGbDepartmentOrderId();
            //update weight
            Integer doDisGoodsId = nxOrderEntity.getNxDoDisGoodsId();
            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
            BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
            BigDecimal orderWeight = new BigDecimal(nxOrderEntity.getNxDoWeight());
            BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
            dgService.update(distributerGoodsEntity);

            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(nxDoGbDepartmentOrderId);
            gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusReceived());
            gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusUnPayFinish());
            gbDepartmentOrdersEntity.setGbDoArriveWeeksYear(getWeekOfYear(0));
            gbDepartmentOrdersEntity.setGbDoArriveWhatDay(getWeek(0));
            gbDepartmentOrdersEntity.setGbDoArriveOnlyDate(formatWhatDate(0));
            gbDepartmentOrdersEntity.setGbDoArriveDate(formatWhatDay(0));
            gbDepartmentOrdersEntity.setGbDoWeight(nxOrderEntity.getNxDoWeight());
            gbDepartmentOrdersEntity.setGbDoSubtotal(nxOrderEntity.getNxDoSubtotal());
            gbDepartmentOrdersEntity.setGbDoPrice(nxOrderEntity.getNxDoPrice());
            gbDepartmentOrdersEntity.setGbDoBillId(gbDepartmentBill.getGbDepartmentBillId());
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
            System.out.println("putodiididididiididisaveDepStockDataByPurchase" + gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId());

            saveDepStockDataByPurchase(gbDepartmentOrdersEntity);

        }
        gbDepartmentBill.setGbDbSellingTotal(sellSubtotal.toString());
        Integer nxDbGbDisId = nxDepartmentBill.getNxDbGbDisId();
        Map<String, Object> map = new HashMap<>();
        map.put("type", getGbDepartmentTypeAppSupplier());
        map.put("disId", nxDbGbDisId);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
        gbDepartmentBill.setGbDbIssueDepId(departmentEntities.get(0).getGbDepartmentId());
        gbDepartmentBillService.update(gbDepartmentBill);

        nxDepartmentBill.setNxDbGbDepartmentBillId(gbDepartmentBill.getGbDepartmentBillId());
        nxDepartmentBillService.update(nxDepartmentBill);

    }

    @RequestMapping(value = "/saveAccountBill000", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBill000(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentBill.getNxDepartmentOrdersEntities();
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBillService.save(nxDepartmentBill);


        BigDecimal billTotal = new BigDecimal(0);
        BigDecimal billProfit = new BigDecimal(0);
        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {

            if (!orders.getNxDoSubtotal().equals("0.0")) {
                //0 subtotal
                billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));
                billProfit = billProfit.add(new BigDecimal(orders.getNxDoProfitSubtotal()));

                //1，添加depDisGoods
                Integer nxDoDepDisGoodsId = checkDepDisGoods(orders);
                orders.setNxDoDepDisGoodsId(nxDoDepDisGoodsId);
                Integer doDisGoodsId = orders.getNxDoDisGoodsId();
                Integer nxDoDistributerId = orders.getNxDoDistributerId();
                //
                //2，增加订货历史
                Map<String, Object> map1 = new HashMap<>();
                map1.put("depDisGoodsId", nxDoDepDisGoodsId);
                map1.put("depId", orders.getNxDoDepartmentId());
                List<NxDepartmentOrdersHistoryEntity> historyEntities = nxDepOrdersHistoryService.queryDepHistoryOrdersByParams(map1);
                String orderQuantity = "";
                String orderStandard = "";
                String orderStr = "";
                orderQuantity = orders.getNxDoQuantity();
                orderStandard = orders.getNxDoStandard();

                orderStr = orderQuantity + orderStandard;

                //如果有4个以内的历史记录
                if (historyEntities.size() > 0 && historyEntities.size() < 4) {

                    int equalNumber = 0;
                    for (NxDepartmentOrdersHistoryEntity orderHistory : historyEntities) {
                        String historyStr = orderHistory.getNxDohQuantity() + orderHistory.getNxDohStandard();
                        if (orderStr.equals(historyStr)) {
                            equalNumber = equalNumber + 1;
                        }
                    }
                    if (equalNumber == 0 && historyEntities.size() < 3) {
                        //添加新的
                        NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
                        historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
                        historyEntity.setNxDohDepDisGoodsId(nxDoDepDisGoodsId);
                        historyEntity.setNxDohDisGoodsId(doDisGoodsId);
                        historyEntity.setNxDohDistributerId(nxDoDistributerId);
                        historyEntity.setNxDohQuantity(orderQuantity);
                        historyEntity.setNxDohStandard(orderStandard);
                        historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
                        historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                        historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
                        nxDepOrdersHistoryService.save(historyEntity);
                    } else if (equalNumber == 0 && historyEntities.size() == 3) {
                        //删除最早一个
                        NxDepartmentOrdersHistoryEntity first = historyEntities.get(0);
                        Integer nxRestrauntOrdersHistoryId = first.getNxDepartmentOrdersHistoryId();
                        nxDepOrdersHistoryService.delete(nxRestrauntOrdersHistoryId);
                        //添加新的
                        NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
                        historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
                        historyEntity.setNxDohDepDisGoodsId(nxDoDepDisGoodsId);
                        historyEntity.setNxDohDisGoodsId(doDisGoodsId);
                        historyEntity.setNxDohDistributerId(nxDoDistributerId);
                        historyEntity.setNxDohQuantity(orderQuantity);
                        historyEntity.setNxDohStandard(orderStandard);
                        historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
                        historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                        historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
                        nxDepOrdersHistoryService.save(historyEntity);
                    }

                } else {
                    System.out.println("diyicilyinggailalazhelieiel");
                    //添加新的
                    NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
                    historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
                    historyEntity.setNxDohDepDisGoodsId(nxDoDepDisGoodsId);
                    historyEntity.setNxDohDisGoodsId(doDisGoodsId);
                    historyEntity.setNxDohDistributerId(nxDoDistributerId);
                    historyEntity.setNxDohQuantity(orderQuantity);
                    historyEntity.setNxDohStandard(orderStandard);
                    historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
                    historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                    historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
                    nxDepOrdersHistoryService.save(historyEntity);
                }
            }
            orders.setNxDoStatus(3);
            orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
            nxDepartmentOrdersService.update(orders);
        }

        nxDepartmentBill.setNxDbTotal(billTotal.toString());
        nxDepartmentBill.setNxDbProfitTotal(billProfit.toString());
        BigDecimal decimal = billProfit.divide(billTotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
        nxDepartmentBill.setNxDbProfitScale(decimal.toString());
        nxDepartmentBillService.update(nxDepartmentBill);
        return R.ok();
    }


    /**
     * 保存
     */
    @ResponseBody
    @RequestMapping("/saveSalesBill")
    public R saveSalesBill(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {
        String areaCode = "1" + nxDepartmentBill.getNxDbDisId();
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentBill.getNxDepartmentOrdersEntities();
        nxDepartmentBill.setNxDbStatus(99);
        nxDepartmentBill.setNxDbDate(formatWhatDate(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBill.setNxDbTradeNo(generateBillTradeNo(areaCode));
        nxDepartmentBill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBillService.save(nxDepartmentBill);


        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            orders.setNxDoStatus(3);
            orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
            if (!orders.getNxDoSubtotal().equals("0.0")) {

                Integer nxDoDepDisGoodsId = checkDepDisGoods(orders);
                orders.setNxDoDepDisGoodsId(nxDoDepDisGoodsId);
                Integer doDisGoodsId = orders.getNxDoDisGoodsId();
                Integer nxDoDistributerId = orders.getNxDoDistributerId();


                //
                //增加订货历史
                Map<String, Object> map1 = new HashMap<>();
                map1.put("depDisGoodsId", nxDoDepDisGoodsId);
                map1.put("depId", orders.getNxDoDepartmentId());
                List<NxDepartmentOrdersHistoryEntity> historyEntities = nxDepOrdersHistoryService.queryDepHistoryOrdersByParams(map1);
                String orderQuantity = "";
                String orderStandard = "";
                String orderStr = "";
                orderQuantity = orders.getNxDoQuantity();
                orderStandard = orders.getNxDoStandard();

                orderStr = orderQuantity + orderStandard;

                //如果有4个以内的历史记录
                if (historyEntities.size() > 0 && historyEntities.size() < 4) {

                    int equalNumber = 0;
                    for (NxDepartmentOrdersHistoryEntity orderHistory : historyEntities) {
                        String historyStr = orderHistory.getNxDohQuantity() + orderHistory.getNxDohStandard();
                        if (orderStr.equals(historyStr)) {
                            equalNumber = equalNumber + 1;
                        }
                    }
                    if (equalNumber == 0 && historyEntities.size() < 3) {
                        //添加新的
                        NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
                        historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
                        historyEntity.setNxDohDepDisGoodsId(nxDoDepDisGoodsId);
                        historyEntity.setNxDohDisGoodsId(doDisGoodsId);
                        historyEntity.setNxDohDistributerId(nxDoDistributerId);
                        historyEntity.setNxDohQuantity(orderQuantity);
                        historyEntity.setNxDohStandard(orderStandard);
                        historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
                        historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                        historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
                        nxDepOrdersHistoryService.save(historyEntity);
                    } else if (equalNumber == 0 && historyEntities.size() == 3) {
                        //删除最早一个
                        NxDepartmentOrdersHistoryEntity first = historyEntities.get(0);
                        Integer nxRestrauntOrdersHistoryId = first.getNxDepartmentOrdersHistoryId();
                        nxDepOrdersHistoryService.delete(nxRestrauntOrdersHistoryId);
                        //添加新的
                        NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
                        historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
                        historyEntity.setNxDohDepDisGoodsId(nxDoDepDisGoodsId);
                        historyEntity.setNxDohDisGoodsId(doDisGoodsId);
                        historyEntity.setNxDohDistributerId(nxDoDistributerId);
                        historyEntity.setNxDohQuantity(orderQuantity);
                        historyEntity.setNxDohStandard(orderStandard);
                        historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
                        historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                        historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
                        nxDepOrdersHistoryService.save(historyEntity);
                    }

                } else {
                    //添加新的
                    NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
                    historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
                    historyEntity.setNxDohDepDisGoodsId(nxDoDepDisGoodsId);
                    historyEntity.setNxDohDisGoodsId(doDisGoodsId);
                    historyEntity.setNxDohDistributerId(nxDoDistributerId);
                    historyEntity.setNxDohQuantity(orderQuantity);
                    historyEntity.setNxDohStandard(orderStandard);
                    historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
                    historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                    historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
                    nxDepOrdersHistoryService.save(historyEntity);
                }
            }
            nxDepartmentOrdersService.update(orders);
            Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
            System.out.println("pugodidiididididiinxDoPurchaseGoodsId=" + nxDoPurchaseGoodsId);
            if (nxDoPurchaseGoodsId != -1) {
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());

                } else {
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                }
                nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

            }
        }

        return R.ok();
    }

    private Integer checkDepDisGoods(NxDepartmentOrdersEntity nxDepartmentOrders) {

        System.out.println("chehchcchchhchchcdididigogood");

        Integer depDisGoodsId = 0;
        //判断是否是部门商品
        Integer doDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        Integer nxDoDepartmentId1 = nxDepartmentOrders.getNxDoDepartmentId();
        //查询部门还是订货群是否添加过此商品
        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDoDepartmentId1);
        map.put("disGoodsId", doDisGoodsId);
        List<NxDepartmentDisGoodsEntity> disGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
        if (disGoodsEntities.size() == 0) {
            //添加部门商品
            NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(doDisGoodsId);
            String nxDgGoodsName = nxDistributerGoodsEntity.getNxDgGoodsName();
            NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
            disGoodsEntity.setNxDdgDepGoodsName(nxDgGoodsName);
            disGoodsEntity.setNxDdgDisGoodsId(doDisGoodsId);
            disGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
            disGoodsEntity.setNxDdgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
            NxDistributerFatherGoodsEntity fatherGoodsEntity = distributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
            Integer greatFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
            disGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);

            disGoodsEntity.setNxDdgDepGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
            disGoodsEntity.setNxDdgDepGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
            disGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
            disGoodsEntity.setNxDdgDepartmentId(nxDoDepartmentId1);
            disGoodsEntity.setNxDdgDepartmentFatherId(nxDepartmentOrders.getNxDoDepartmentFatherId());
            //orderData
            disGoodsEntity.setNxDdgOrderPrice(nxDepartmentOrders.getNxDoPrice());
            disGoodsEntity.setNxDdgOrderCostPrice(nxDepartmentOrders.getNxDoCostPrice());
            disGoodsEntity.setNxDdgOrderQuantity(nxDepartmentOrders.getNxDoQuantity());
            disGoodsEntity.setNxDdgOrderRemark(nxDepartmentOrders.getNxDoRemark());
            disGoodsEntity.setNxDdgOrderStandard(nxDepartmentOrders.getNxDoStandard());
            disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
            nxDepartmentDisGoodsService.save(disGoodsEntity);
            depDisGoodsId = disGoodsEntity.getNxDepartmentDisGoodsId();

        } else {

            depDisGoodsId = disGoodsEntities.get(0).getNxDepartmentDisGoodsId();
            NxDepartmentDisGoodsEntity disGoodsEntity = nxDepartmentDisGoodsService.queryObject(depDisGoodsId);
            disGoodsEntity.setNxDdgOrderPrice(nxDepartmentOrders.getNxDoPrice());
            disGoodsEntity.setNxDdgOrderCostPrice(nxDepartmentOrders.getNxDoCostPrice());
            disGoodsEntity.setNxDdgOrderQuantity(nxDepartmentOrders.getNxDoQuantity());
            disGoodsEntity.setNxDdgOrderRemark(nxDepartmentOrders.getNxDoRemark());
            disGoodsEntity.setNxDdgOrderStandard(nxDepartmentOrders.getNxDoStandard());
            disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDepartmentOrders.getPurchaseGoodsEntity();
            if (purchaseGoodsEntity != null) {
                if (purchaseGoodsEntity.getNxDpgSellUserId() != null) {
                    disGoodsEntity.setNxDdgOrderSellerUserId(purchaseGoodsEntity.getNxDpgSellUserId());
                }
                disGoodsEntity.setNxDdgOrderBuyerUserId(purchaseGoodsEntity.getNxDpgBuyUserId());
            }

            nxDepartmentDisGoodsService.update(disGoodsEntity);
        }
        return depDisGoodsId;
    }


    @RequestMapping(value = "/updateBillOrders", method = RequestMethod.POST)
    @ResponseBody
    public R updateBillOrders(Integer billId, Integer orderId, String billSubtotal,
                              String orderWeight, String orderPrice, String orderSubtotal) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObjectNew(orderId);
        ordersEntity.setNxDoWeight(orderWeight);
        ordersEntity.setNxDoPrice(orderPrice);
        ordersEntity.setNxDoSubtotal(orderSubtotal);
        System.out.println("whhwhwh" + ordersEntity.getNxDoExpectPrice());
        if (ordersEntity.getNxDoPriceDifferent() != null) {
            BigDecimal weightB = new BigDecimal(orderWeight);
            BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
            BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal profitB = new BigDecimal(orderSubtotal).subtract(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal scaleB = profitB.divide(new BigDecimal(orderSubtotal), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            ordersEntity.setNxDoProfitScale(scaleB.toString());
            ordersEntity.setNxDoProfitSubtotal(profitB.toString());
            BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpectPrice());
            BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
            BigDecimal subtract = doPrice.subtract(expectPrice);
            System.out.println("exxxxxxxx" + subtract);
            ordersEntity.setNxDoPriceDifferent(subtract.toString());
        }

        nxDepartmentOrdersService.update(ordersEntity);
        NxDepartmentBillEntity nxDepartmentBillEntity = nxDepartmentBillService.queryObject(billId);
        nxDepartmentBillEntity.setNxDbTotal(billSubtotal);
        nxDepartmentBillService.update(nxDepartmentBillEntity);
        return R.ok();
    }


}
