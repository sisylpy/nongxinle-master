package com.nongxinle.controller;

/**
 * @author lpy
 * @date 10-11 17:01
 */

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.github.wxpay.sdk.WXPay;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//import static com.nongxinle.utils.CommonUtils.generateBillTradeNo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.CommonUtils.generateBillTradeNo;
import static com.nongxinle.utils.CommonUtils.generateOutTradeNo;
import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.getLastTwoMonth;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.ParseObject.myRandom;
import static com.nongxinle.utils.PinYin4jUtils.getHeadStringByString;


@RestController
@RequestMapping("api/nxdepartmentbill")
public class NxDepartmentBillController {
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
    private NxDistributerPurchaseBatchService nxDistributerPurchaseBatchService;



    @ResponseBody
    @RequestMapping(value = "/restrauntCashPay",method = RequestMethod.POST)
    public R restrauntCashPay(@RequestBody NxDepartmentBillEntity billEntity) {
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
        params.put("body",  "订单支付");
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
            String tString = String.valueOf(time/1000);
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
            System.out.println("微信返回给回调函数的信息为："+xml);
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
        ordersEntity.setNxDoIsAgent(1);
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
        ordersEntity.setNxDoCostPriceLevel("0");
        ordersEntity.setNxDoPriceDifferent("0.0");
        System.out.println("savvdororororororooro");
        nxDepartmentOrdersService.save(ordersEntity);

        //auto
        System.out.println("pudiididididipppppp" + distributerGoodsEntity.getNxDgPurchaseAuto());
        if(distributerGoodsEntity.getNxDgPurchaseAuto() != -1){
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


    @RequestMapping(value = "/deleteBill/{billId}")
    @ResponseBody
    public R deleteBill(@PathVariable Integer billId) {
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


    @RequestMapping(value = "/getReturnBillApplys/{billId}")
    @ResponseBody
    public R getReturnBillApplys(@PathVariable Integer billId) {
        NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryReturnBillOrdersByBillId(billId);
        return R.ok().put("data", billEntity);
    }


    @RequestMapping(value = "/saveAccountReturnBill", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountReturnBill(@RequestBody NxDepartmentBillEntity bill) {
        String areaCode = "1" + bill.getNxDbDisId();

        bill.setNxDbStatus(0);
        bill.setNxDbDate(formatWhatDate(0));
        bill.setNxDbTime(formatWhatYearDayTime(0));
        bill.setNxDbMonth(formatWhatMonth(0));
        bill.setNxDbWeek(getWeekOfYear(0).toString());
        bill.setNxDbDay(getWeek(0));
        bill.setNxDbTradeNo(generateBillTradeNo(areaCode));
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
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapOne = new HashMap<>();
        mapOne.put("month", formatWhatMonth(0));
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
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(map);

        BigDecimal totalDec = new BigDecimal("0.0");
        for (NxDepartmentBillEntity bill : billEntityList) {
            BigDecimal bigDecimal = new BigDecimal(bill.getNxDbTotal());
            totalDec = totalDec.add(bigDecimal);

        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("arr", dataMap);
        resultMap.put("total", totalDec);
        return R.ok().put("data", resultMap);
    }


    @RequestMapping(value = "/getBillApplysGbDep")
    @ResponseBody
    public R getBillApplysGbDep(Integer billId, Integer depFatherId) {

        //billRetrunNumber
        Integer count = nxDepartmentBillService.queryReturnNumberByBillId(billId);

        NxDepartmentBillEntity salesBill = nxDepartmentBillService.querySalesBillApplys(billId);

        Map<String, Object> map = new HashMap<>();
        map.put("gbDepFatherId", depFatherId);
        map.put("billId", billId);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);

        List<Map<String, Object>> mapList = new ArrayList<>();
        List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(depFatherId);

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
            return R.ok().put("data", map3);
        } else {
            Map<String, Object> map4 = new HashMap<>();
            map4.put("arr", ordersEntities);
            map4.put("bill", salesBill);
            map4.put("returnNumber", count);
            return R.ok().put("data", map4);
        }
    }


    @RequestMapping(value = "/getBillApplys")
    @ResponseBody
    public R getBillApplys(Integer billId, Integer depFatherId) {

        //billRetrunNumber
        Integer count = nxDepartmentBillService.queryReturnNumberByBillId(billId);

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
            return R.ok().put("data", map3);
        } else {
            Map<String, Object> map4 = new HashMap<>();
            map4.put("arr", ordersEntities);
            map4.put("bill", salesBill);
            map4.put("returnNumber", count);
            return R.ok().put("data", map4);
        }


    }


    private Map<String, Object> queryAccountBillByMonthGb(Integer disId, Integer gbDisId, Integer nxCommId, int which) {

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
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryGbDepBillsByParams(map);

        //本月的未结账单数量
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("gbDisId", gbDisId);
        map1.put("nxCommId", nxCommId);
        map1.put("equalStatus", 0);
        map1.put("month", format);
        int whichMonthUnSettleTotal = nxDepartmentBillService.queryTotalByParams(map1);
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
        String format = formatters.format(today);
        //本月的账单
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("month", format);
        map.put("status", 3);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(map);

        //本月的未结账单数量
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depFatherId", depFatherId);
        map1.put("equalStatus", 0);
        map1.put("month", format);
        int whichMonthUnSettleTotal = nxDepartmentBillService.queryTotalByParams(map1);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("arr", billEntityList);
        dataMap.put("unSettleTotal", whichMonthUnSettleTotal);
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
        if(total > 0){
            aDouble  = nxDepartmentBillService.queryBillSubtotalByParams(map);
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("arr", billEntityList);
        dataMap.put("month", format);
        dataMap.put("total", new BigDecimal(aDouble).setScale(1,BigDecimal.ROUND_HALF_UP));

        return dataMap;
    }


    @RequestMapping(value = "/saveAccountBillGb", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillGb(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {

        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentBill.getNxDepartmentOrdersEntities();
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBillService.save(nxDepartmentBill);

        BigDecimal billTotal = new BigDecimal(0);
        BigDecimal billProfit = new BigDecimal(0);
        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {

            if (!orders.getNxDoSubtotal().equals("0.0")) {
                //0 subtotal
                billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));
                billProfit = billProfit.add(new BigDecimal(orders.getNxDoProfitSubtotal()));
            }

            orders.setNxDoStatus(3);
            orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
            nxDepartmentOrdersService.update(orders);
            Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
            System.out.println("pugodidiididididiinxDoPurchaseGoodsId=" + nxDoPurchaseGoodsId);
            if(nxDoPurchaseGoodsId != -1){
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                if(nxDpgOrdersAmount - nxDpgFinishAmount == 1){
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());

                }else{
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                }
                nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

            }
            if (orders.getNxDoNxRestrauntOrderId() != null) {


            }
            if (orders.getNxDoGbDepartmentOrderId() != null) {
                //更新gbDepOrder
                Integer nxDepartmentOrdersId = orders.getNxDepartmentOrdersId();
                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(nxDepartmentOrdersId);
                if (gbDepartmentOrdersEntity != null) {
                    Integer nxDbDisId = nxDepartmentBill.getNxDbDisId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("nxDisId", nxDbDisId);
                    map.put("gbDisId", nxDepartmentBill.getNxDbGbDisId());
                    NxDistributerGbDistributerEntity entity = nxDisGbDisService.queryObjectByParams(map);
                    gbDepartmentOrdersEntity.setGbDoStatus(2);
                    gbDepartmentOrdersEntity.setGbDoPurchaseUserId(entity.getNxDgdGbDepUserId());
                    gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
                }
            }

        }
        nxDepartmentBill.setNxDbTotal(billTotal.toString());
        nxDepartmentBill.setNxDbProfitTotal(billProfit.toString());
        BigDecimal decimal = billProfit.divide(billTotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
        nxDepartmentBill.setNxDbProfitScale(decimal.toString());
        nxDepartmentBillService.update(nxDepartmentBill);
        return R.ok();
    }



    @RequestMapping(value = "/saveAccountBill", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBill(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentBill.getNxDepartmentOrdersEntities();
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBillService.save(nxDepartmentBill);

        BigDecimal billTotal = new BigDecimal(0);
        BigDecimal billProfit = new BigDecimal(0);
        BigDecimal costTotal = new BigDecimal(0);
        System.out.println("ordersisiisis" + nxDepartmentOrdersEntities.size());
        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {

            if (orders.getNxDoSubtotal() != null && new BigDecimal(orders.getNxDoSubtotal()).compareTo(BigDecimal.ZERO) == 1) {
                //0 subtotal
                billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));
                billProfit = billProfit.add(new BigDecimal(orders.getNxDoProfitSubtotal()));
                costTotal = costTotal.add(new BigDecimal(orders.getNxDoCostSubtotal()));

                if (nxDepartmentBill.getNxDbNxCommunityId() == -1 && nxDepartmentBill.getNxDbGbDisId() == -1) {
                    System.out.println("savedepddididodoodgogodso");
                    //1，配送商自己的客户
                    Integer nxDoDepDisGoodsId = checkDepDisGoods(orders);
                    orders.setNxDoDepDisGoodsId(nxDoDepDisGoodsId);
                    //
                    //2，增加订货历史
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("depDisGoodsId", nxDoDepDisGoodsId);
                    map1.put("depId", orders.getNxDoDepartmentId());
                    List<NxDepartmentOrdersHistoryEntity> historyEntities = nxDepOrdersHistoryService.queryDepHistoryOrdersByParams(map1);
                    String orderQuantity = "";
                    String orderStandard = "";

                    orderQuantity = orders.getNxDoQuantity();
                    orderStandard = orders.getNxDoStandard();
                    int equal = 0;

                    if (historyEntities.size() > 0) {
                        for(NxDepartmentOrdersHistoryEntity ordersHistoryEntity: historyEntities){
                            String history = ordersHistoryEntity.getNxDohQuantity()+ ordersHistoryEntity.getNxDohStandard();
                            if(history.equals(orderQuantity+orderStandard)){
                               equal = equal + 1;
                            }
                        }
                        if(equal == 0){
                            if (historyEntities.size() == 3) {
                                NxDepartmentOrdersHistoryEntity first = historyEntities.get(0);
                                Integer nxRestrauntOrdersHistoryId = first.getNxDepartmentOrdersHistoryId();
                                nxDepOrdersHistoryService.delete(nxRestrauntOrdersHistoryId);
                            }
                            //添加新的
                            NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
                            historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
                            historyEntity.setNxDohDepDisGoodsId(nxDoDepDisGoodsId);
                            historyEntity.setNxDohQuantity(orderQuantity);
                            historyEntity.setNxDohStandard(orderStandard);
                            historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
                            historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                            historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
                            Map<String, Object> map = new HashMap<>();
                            map.put("depDisGoodsId", nxDoDepDisGoodsId);
                            map.put("order", orders.getNxDoTodayOrder());
                            int times =  nxDepOrdersHistoryService.queryOrderTimes(map);
                            historyEntity.setNxDohOrderTimes(times + 1);
                            historyEntity.setNxDohOrder(orders.getNxDoTodayOrder());
                            nxDepOrdersHistoryService.save(historyEntity);
                        }

                    } else {
                        System.out.println("diyicilyinggailalazhelieiel");
                        //添加新的
                        NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
                        historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
                        historyEntity.setNxDohDepDisGoodsId(nxDoDepDisGoodsId);
                        historyEntity.setNxDohQuantity(orderQuantity);
                        historyEntity.setNxDohStandard(orderStandard);
                        historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
                        historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                        historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
                        historyEntity.setNxDohOrderTimes(1);
                        historyEntity.setNxDohOrder(orders.getNxDoTodayOrder());
                        nxDepOrdersHistoryService.save(historyEntity);
                    }
                }
            }

            orders.setNxDoStatus(3);
            orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
            orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
            nxDepartmentOrdersService.update(orders);

            Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
            if(nxDoPurchaseGoodsId != -1){
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                if(nxDpgOrdersAmount - nxDpgFinishAmount == 1){
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                }else{
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                }
                nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

                //

                if(orders.getNxDoGoodsType() == 2){
                    //update auto
                    if(purchaseGoodsEntity.getNxDpgFinishAmount().equals(purchaseGoodsEntity.getNxDpgOrdersAmount())){
                        Integer nxDpgBatchId = purchaseGoodsEntity.getNxDpgBatchId();
                        NxDistributerPurchaseBatchEntity batchEntity = nxDistributerPurchaseBatchService.queryObject(nxDpgBatchId);
                        batchEntity.setNxDpbStatus(3);
                        nxDistributerPurchaseBatchService.update(batchEntity);
                    }
                }
            }
        }

        nxDepartmentBill.setNxDbCostTotal(costTotal.toString());
        nxDepartmentBill.setNxDbTotal(billTotal.toString());
        nxDepartmentBill.setNxDbProfitTotal(billProfit.toString());
        if(billTotal.compareTo(BigDecimal.ZERO) == 1){
            BigDecimal decimal = billProfit.divide(billTotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            nxDepartmentBill.setNxDbProfitScale(decimal.toString());
            nxDepartmentBillService.update(nxDepartmentBill);
        }
        
        if (nxDepartmentBill.getNxDbNxCommunityId() == -1 && nxDepartmentBill.getNxDbGbDisId() > 0) {
            saveAccountBillGbDis(nxDepartmentBill);
        }
        if (nxDepartmentBill.getNxDbNxCommunityId() > 0 && nxDepartmentBill.getNxDbGbDisId() == -1) {
            //saveRestbill
        }
        return R.ok();
    }



    @RequestMapping(value = "/nxDisQuickSaveAccountBill/{depFatherId}")
    @ResponseBody
    public R nxDisQuickSaveAccountBill(@PathVariable Integer depFatherId) {
        NxDepartmentEntity departmentEntity = nxDepartmentService.queryDepInfo(depFatherId);
        System.out.println("desisis"+ departmentEntity.getNxDepartmentEntities().size());
        System.out.println("desisis"+ departmentEntity.getNxDepartmentName());

        Integer nxDepartmentDisId = departmentEntity.getNxDepartmentDisId();

        if(departmentEntity.getNxDepartmentEntities().size() >0){
            for(NxDepartmentEntity subDep: departmentEntity.getNxDepartmentEntities()){
                Map<String, Object> map = new HashMap<>();
                map.put("depId", subDep.getNxDepartmentId());
                map.put("status", 3);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                if(integer > 0){
                    saveSubDepBill(depFatherId, subDep.getNxDepartmentId(), nxDepartmentDisId);
                }

            }

        }else{
            saveSubDepBill(depFatherId, depFatherId,nxDepartmentDisId );
        }
        return R.ok();
    }

    private void  saveSubDepBill(Integer depFatherId, Integer depId,Integer disId){
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
            if(nxDoPurchaseGoodsId != -1){
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                if(nxDpgOrdersAmount - nxDpgFinishAmount == 1){
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                }else{
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                }
                nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        nxDepartmentBill.setNxDbCostTotal(costTotal.toString());
        nxDepartmentBill.setNxDbTotal(billTotal.toString());
        nxDepartmentBill.setNxDbProfitTotal(billProfit.toString());
        if(billTotal.compareTo(BigDecimal.ZERO) == 1){
            BigDecimal decimal = billProfit.divide(billTotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            nxDepartmentBill.setNxDbProfitScale(decimal.toString());
            nxDepartmentBillService.update(nxDepartmentBill);
        }


    }

    @RequestMapping(value = "/saveAccountBillWeb", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillWeb(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBillService.save(nxDepartmentBill);

        BigDecimal billTotal = new BigDecimal(0);
        BigDecimal billProfit = new BigDecimal(0);

        String[] nxOrderIds = nxDepartmentBill.getNxOrderIds();
        for (String id : nxOrderIds) {
            NxDepartmentOrdersEntity orders = nxDepartmentOrdersService.queryObjectNew(Integer.valueOf(id));
            //0 subtotal
            billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));

            //1，配送商自己的客户
            Integer nxDoDepDisGoodsId = checkDepDisGoods(orders);
            orders.setNxDoDepDisGoodsId(nxDoDepDisGoodsId);

            //
            //2，增加订货历史
            Map<String, Object> map1 = new HashMap<>();
            map1.put("depDisGoodsId", nxDoDepDisGoodsId);
            map1.put("depId", orders.getNxDoDepartmentId());
            List<NxDepartmentOrdersHistoryEntity> historyEntities = nxDepOrdersHistoryService.queryDepHistoryOrdersByParams(map1);
            String orderQuantity = "";
            String orderStandard = "";

            orderQuantity = orders.getNxDoQuantity();
            orderStandard = orders.getNxDoStandard();
            int equal = 0;

            if (historyEntities.size() > 0) {
                for(NxDepartmentOrdersHistoryEntity ordersHistoryEntity: historyEntities){
                    String history = ordersHistoryEntity.getNxDohQuantity()+ ordersHistoryEntity.getNxDohStandard();
                    if(history.equals(orderQuantity+orderStandard)){
                        equal = equal + 1;
                    }
                }
                if(equal == 0){
                    if (historyEntities.size() == 3) {
                        NxDepartmentOrdersHistoryEntity first = historyEntities.get(0);
                        Integer nxRestrauntOrdersHistoryId = first.getNxDepartmentOrdersHistoryId();
                        nxDepOrdersHistoryService.delete(nxRestrauntOrdersHistoryId);
                    }
                    //添加新的
                    NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
                    historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
                    historyEntity.setNxDohDepDisGoodsId(nxDoDepDisGoodsId);
                    historyEntity.setNxDohQuantity(orderQuantity);
                    historyEntity.setNxDohStandard(orderStandard);
                    historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
                    historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                    historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("depDisGoodsId", nxDoDepDisGoodsId);
                    map.put("order", orders.getNxDoTodayOrder());
                    int times =  nxDepOrdersHistoryService.queryOrderTimes(map);
                    historyEntity.setNxDohOrderTimes(times + 1);
                    historyEntity.setNxDohOrder(orders.getNxDoTodayOrder());
                    nxDepOrdersHistoryService.save(historyEntity);
                }

            } else {
                System.out.println("diyicilyinggailalazhelieiel");
                //添加新的
                NxDepartmentOrdersHistoryEntity historyEntity = new NxDepartmentOrdersHistoryEntity();
                historyEntity.setNxDohApplyDate(orders.getNxDoApplyDate());
                historyEntity.setNxDohDepDisGoodsId(nxDoDepDisGoodsId);
                historyEntity.setNxDohQuantity(orderQuantity);
                historyEntity.setNxDohStandard(orderStandard);
                historyEntity.setNxDohDepartmentId(orders.getNxDoDepartmentId());
                historyEntity.setNxDohDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                historyEntity.setNxDohOrderUserId(orders.getNxDoOrderUserId());
                historyEntity.setNxDohOrderTimes(1);
                historyEntity.setNxDohOrder(orders.getNxDoTodayOrder());
                nxDepOrdersHistoryService.save(historyEntity);
            }


            orders.setNxDoStatus(3);
            orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
            orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
            nxDepartmentOrdersService.update(orders);

            Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
            if(nxDoPurchaseGoodsId != -1){
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                if(nxDpgOrdersAmount - nxDpgFinishAmount == 1){
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                }else{
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
        return R.ok();
    }


    public void saveAccountBillGbDisApp(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {
        System.out.println("savebillAppp");
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentBill.getNxDepartmentOrdersEntities();
        GbDepartmentBillEntity gbDepartmentBill = new GbDepartmentBillEntity();
        gbDepartmentBill.setGbDbIssueNxDisId(nxDepartmentBill.getNxDbDisId());
        gbDepartmentBill.setGbDbDisId(nxDepartmentBill.getNxDbGbDisId());
        gbDepartmentBill.setGbDbDepId(nxDepartmentBill.getNxDbGbDepId());
        gbDepartmentBill.setGbDbTotal(nxDepartmentBill.getNxDbTotal());
        gbDepartmentBill.setGbDbPrintTimes(1);
        gbDepartmentBill.setGbDbIssueOrderType(5);
        gbDepartmentBill.setGbDbOrderAmount(nxDepartmentOrdersEntities.size());
        gbDepartmentBill.setGbDbStatus(0);
        gbDepartmentBill.setGbDbDate(formatWhatDay(0));
        gbDepartmentBill.setGbDbTime(formatWhatYearDayTime(0));
        gbDepartmentBill.setGbDbMonth(formatWhatMonth(0));
        gbDepartmentBill.setGbDbWeek(getWeek(0));
        gbDepartmentBill.setGbDbTradeNo(nxDepartmentBill.getNxDbTradeNo());
        gbDepartmentBill.setGbDbSellingTotal("0");
        gbDepartmentBillService.save(gbDepartmentBill);

        for (NxDepartmentOrdersEntity nxOrderEntity : nxDepartmentOrdersEntities) {
            Integer nxDoGbDepartmentOrderId = nxOrderEntity.getNxDoGbDepartmentOrderId();
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(nxDoGbDepartmentOrderId);
            gbDepartmentOrdersEntity.setGbDoArriveWeeksYear(getWeekOfYear(0));
            gbDepartmentOrdersEntity.setGbDoArriveWhatDay(getWeek(0));
            gbDepartmentOrdersEntity.setGbDoArriveOnlyDate(formatWhatDate(0));
            gbDepartmentOrdersEntity.setGbDoArriveDate(formatWhatDay(0));
            gbDepartmentOrdersEntity.setGbDoWeight(nxOrderEntity.getNxDoWeight());
            gbDepartmentOrdersEntity.setGbDoPrice(nxOrderEntity.getNxDoPrice());
            gbDepartmentOrdersEntity.setGbDoSubtotal(nxOrderEntity.getNxDoSubtotal());
            gbDepartmentOrdersEntity.setGbDoBillId(gbDepartmentBill.getGbDepartmentBillId());
            Integer depDisGoodsId = gbDepartmentOrdersEntity.getGbDoDepDisGoodsId();
            GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
            String gbDdgOrderDate = departmentDisGoodsEntity.getGbDdgOrderDate();

            if (gbDdgOrderDate != null && gbDepartmentOrdersEntity.getGbDoPrice() != null) {
                BigDecimal decimal = new BigDecimal(departmentDisGoodsEntity.getGbDdgOrderPrice());
                BigDecimal decimal1 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoPrice());
                BigDecimal subtract1 = decimal1.subtract(decimal);
                gbDepartmentOrdersEntity.setGbDoPriceDifferent(subtract1.toString());
            } else {
                gbDepartmentOrdersEntity.setGbDoPriceDifferent("0");
            }

            GbDepartmentGoodsStockEntity stockEntity = new GbDepartmentGoodsStockEntity();
            stockEntity.setGbDgsGbDepartmentId(gbDepartmentOrdersEntity.getGbDoDepartmentId());
            stockEntity.setGbDgsGbDepartmentFatherId(gbDepartmentOrdersEntity.getGbDoDepartmentFatherId());
            stockEntity.setGbDgsGbPurGoodsId(gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId());
            stockEntity.setGbDgsGbDistributerId(gbDepartmentOrdersEntity.getGbDoDistributerId());
            stockEntity.setGbDgsWeight(gbDepartmentOrdersEntity.getGbDoWeight());
            stockEntity.setGbDgsPrice(gbDepartmentOrdersEntity.getGbDoPrice());
            stockEntity.setGbDgsSubtotal(gbDepartmentOrdersEntity.getGbDoSubtotal());
            stockEntity.setGbDgsRestWeight(gbDepartmentOrdersEntity.getGbDoWeight());
            stockEntity.setGbDgsRestSubtotal(gbDepartmentOrdersEntity.getGbDoSubtotal());
            stockEntity.setGbDgsGbDisGoodsId(gbDepartmentOrdersEntity.getGbDoDisGoodsId());
            stockEntity.setGbDgsGbDisGoodsFatherId(gbDepartmentOrdersEntity.getGbDoDisGoodsFatherId());
            stockEntity.setGbDgsGbDepDisGoodsId(gbDepartmentOrdersEntity.getGbDoDepDisGoodsId());
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
                BigDecimal divide = new BigDecimal(gbDepartmentOrdersEntity.getGbDoWeight()).divide(new BigDecimal(gbDdgShowStandardScale), 1, BigDecimal.ROUND_HALF_UP);
                stockEntity.setGbDgsRestWeightShowStandard(divide.toString());
                stockEntity.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
            }

            //判断是否有保鲜时间参数
            if (gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId() != -1) {
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId());
                if (purchaseGoodsEntity.getGbDpgWarnFullTime() != null && purchaseGoodsEntity.getGbDpgWasteFullTime() != null) {
                    stockEntity.setGbDgsWarnFullTime(purchaseGoodsEntity.getGbDpgWarnFullTime());
                    stockEntity.setGbDgsWasteFullTime(purchaseGoodsEntity.getGbDpgWasteFullTime());
                }
                //判断是否价格异常商品
                if (purchaseGoodsEntity.getGbDpgDisGoodsPriceId() != null) {
                    GbDistributerGoodsPriceEntity goodsPriceEntity = goodsPriceService.queryObject(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
                    String doWeight = gbDepartmentOrdersEntity.getGbDoWeight();
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
            stockEntity.setGbDgsGbDepartmentOrderId(gbDepartmentOrdersEntity.getGbDepartmentOrdersId());
            stockEntity.setGbDgsGbGoodsStockId(-1);
            stockEntity.setGbDgsGbFromDepartmentId(gbDepartmentOrdersEntity.getGbDoToDepartmentId());
            stockEntity.setGbDgsNxDistributerId(gbDepartmentOrdersEntity.getGbDoNxDistributerId());
            stockEntity.setGbDgsReceiveUserId(gbDepartmentOrdersEntity.getGbDoReceiveUserId());
            stockEntity.setGbDgsInventoryDate(formatWhatDay(0));
            stockEntity.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
            stockEntity.setGbDgsInventoryMonth(formatWhatMonth(0));
            stockEntity.setGbDgsInventoryYear(formatWhatYear(0));
            gbDepartmentGoodsStockService.save(stockEntity);

            updateDepGoodsDailyBusiness(stockEntity);

            Integer gbDepartmentDisGoodsId = departmentDisGoodsEntity.getGbDepartmentDisGoodsId();
            updateDepDisGoods(gbDepartmentOrdersEntity, stockEntity, gbDepartmentDisGoodsId, "add");

            gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusReceived());

            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        }
        Integer nxDbGbDisId = nxDepartmentBill.getNxDbGbDisId();
        Map<String, Object> map = new HashMap<>();
        map.put("type", getGbDepartmentTypeAppSupplier());
        map.put("disId", nxDbGbDisId);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);

        gbDepartmentBill.setGbDbIssueDepId(departmentEntities.get(0).getGbDepartmentId());
        gbDepartmentBillService.update(gbDepartmentBill);

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


    public void saveAccountBillGbDis(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {

        System.out.println("dfadkfaslfjsalfjdlasfjalsfjdalsfjdasl");
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
        gbDepartmentBill.setGbDbWeek(getWeek(0));
        gbDepartmentBill.setGbDbTradeNo(nxDepartmentBill.getNxDbTradeNo());
        Map<String, Object> mapg = new HashMap<>();
        mapg.put("nxDisId", nxDepartmentBill.getNxDbDisId());
        mapg.put("gbDisId", nxDepartmentBill.getNxDbGbDisId());
        NxDistributerGbDistributerEntity entity = nxDisGbDisService.queryObjectByParams(mapg);
        Integer nxDgdGbPayMethod = entity.getNxDgdGbPayMethod();
        if(nxDgdGbPayMethod == 1){
            Integer nxDgdGbPayPeriodWeek = entity.getNxDgdGbPayPeriodWeek();
            String willPayDate = getWillPayDate(nxDgdGbPayPeriodWeek, Calendar.WEDNESDAY);
            gbDepartmentBill.setGbDbWillPayDate(willPayDate);
        }else{
            gbDepartmentBill.setGbDbWillPayDate(formatWhatDate(1));
        }

        gbDepartmentBillService.save(gbDepartmentBill);

        BigDecimal sellSubtotal = new BigDecimal(0);

        for (NxDepartmentOrdersEntity nxOrderEntity : nxDepartmentOrdersEntities) {
            Integer nxDoGbDepartmentOrderId = nxOrderEntity.getNxDoGbDepartmentOrderId();
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(nxDoGbDepartmentOrderId);
            gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusHasBill());
            gbDepartmentOrdersEntity.setGbDoBuyStatus(3);
            gbDepartmentOrdersEntity.setGbDoArriveWeeksYear(getWeekOfYear(0));
            gbDepartmentOrdersEntity.setGbDoArriveWhatDay(getWeek(0));
            gbDepartmentOrdersEntity.setGbDoArriveOnlyDate(formatWhatDate(0));
            gbDepartmentOrdersEntity.setGbDoArriveDate(formatWhatDay(0));
            gbDepartmentOrdersEntity.setGbDoWeight(nxOrderEntity.getNxDoWeight());
            gbDepartmentOrdersEntity.setGbDoSubtotal(nxOrderEntity.getNxDoSubtotal());
            gbDepartmentOrdersEntity.setGbDoPrice(nxOrderEntity.getNxDoPrice());
            gbDepartmentOrdersEntity.setGbDoBillId(gbDepartmentBill.getGbDepartmentBillId());

            Integer depDisGoodsId = gbDepartmentOrdersEntity.getGbDoDepDisGoodsId();

            GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
            if (departmentDisGoodsEntity.getGbDdgSellingPrice() != null) {
                BigDecimal sellingPrice = new BigDecimal(departmentDisGoodsEntity.getGbDdgSellingPrice());
                BigDecimal weight = new BigDecimal(gbDepartmentOrdersEntity.getGbDoWeight());
                BigDecimal multiply = sellingPrice.multiply(weight);
                sellSubtotal = sellSubtotal.add(multiply);
                gbDepartmentOrdersEntity.setGbDoSellingPrice(departmentDisGoodsEntity.getGbDdgSellingPrice());
                gbDepartmentOrdersEntity.setGbDoSellingSubtotal(multiply.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }

            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
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
        nxDepartmentBillService.save(nxDepartmentBill);
        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            orders.setNxDoStatus(3);
            orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
            if (!orders.getNxDoSubtotal().equals("0.0")) {

                Integer nxDoDepDisGoodsId = checkDepDisGoods(orders);
                orders.setNxDoDepDisGoodsId(nxDoDepDisGoodsId);

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
            if(nxDoPurchaseGoodsId != -1){
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                if(nxDpgOrdersAmount - nxDpgFinishAmount == 1){
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());

                }else{
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
        nxDepartmentOrdersService.update(ordersEntity);
        NxDepartmentBillEntity nxDepartmentBillEntity = nxDepartmentBillService.queryObject(billId);
        nxDepartmentBillEntity.setNxDbTotal(billSubtotal);
        nxDepartmentBillService.update(nxDepartmentBillEntity);
        return R.ok();
    }


}
