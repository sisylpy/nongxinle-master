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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.nongxinle.utils.CommonUtils.generateBillTradeNo;
import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.getLastMonth;
import static com.nongxinle.utils.GbTypeUtils.*;


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



    @ResponseBody
    @RequestMapping(value = "/restrauntCashPay",method = RequestMethod.POST)
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
        params.put("body",  "订单支付");
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

            System.out.println("微信返回给回调函数的信息为："+xml);

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
    public R depSearchBillGoods (Integer depGoodsId,String startDate, String stopDate) {
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
    public R disGetNxDistributerUnPayBills (Integer nxDisId, Integer gbDisId, Integer status) {

        Map<String, Object> map = new HashMap<>();
        map.put("nxDisId", nxDisId);
        map.put("disId", gbDisId);
        map.put("status", status);
        List<GbDepartmentBillEntity> billEntities = gbDepartmentBillService.queryBillsByParamsGb(map);

        Double aDouble = 0.0;
        if(gbDepartmentBillService.queryDepartmentBillCount(map) > 0){
            aDouble = gbDepartmentBillService.queryGbDepBillsSubTotal(map);
        }


        Map<String, Object> map6 = new HashMap<>();
        map6.put("total", new BigDecimal(aDouble).setScale(1,BigDecimal.ROUND_HALF_UP));
        map6.put("arr", billEntities );

        return R.ok().put("data",map6);
    }



    @RequestMapping(value = "/disGetNxDistributerBills", method = RequestMethod.POST)
    @ResponseBody
    public R disGetNxDistributerBills (Integer nxDisId, Integer gbDisId, String startDate,
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
        if(gbDepartmentBillService.queryDepartmentBillCount(mapSub) > 0){
            aDouble = gbDepartmentBillService.queryGbDepBillsSubTotal(mapSub);
        }
        Double aDoubleOne = 0.0;
        mapSub.put("month", getLastMonth());
        if(gbDepartmentBillService.queryDepartmentBillCount(mapSub) > 0){
            aDoubleOne = gbDepartmentBillService.queryGbDepBillsSubTotal(mapSub);
        }
        Double aDoubleTwo = 0.0;
        mapSub.put("month", getLastTwoMonth());
        if(gbDepartmentBillService.queryDepartmentBillCount(mapSub) > 0){
            aDoubleTwo = gbDepartmentBillService.queryGbDepBillsSubTotal(mapSub);
        }


        Map<String, Object> mapSubHave = new HashMap<>();
        mapSubHave.put("nxDisId", nxDisId);
        mapSubHave.put("disId", gbDisId);
        mapSubHave.put("month", formatWhatMonth(0));
        mapSubHave.put("dayuStatus", 3);
        int count1 = gbDepartmentBillService.queryDepartmentBillCount(mapSubHave);
        Double aDoubleHave = 0.0;
        if(count1 > 0){
            aDoubleHave = gbDepartmentBillService.queryGbDepBillsSubTotal(mapSubHave);
        }

        mapSubHave.put("month", getLastMonth());
        int count2 = gbDepartmentBillService.queryDepartmentBillCount(mapSubHave);
        Double aDoubleHaveOne = 0.0;
        if(count2 > 0){
            aDoubleHaveOne = gbDepartmentBillService.queryGbDepBillsSubTotal(mapSubHave);
        }


        mapSubHave.put("month", getLastTwoMonth());
        int count3 = gbDepartmentBillService.queryDepartmentBillCount(mapSubHave);
        Double aDoubleHaveTwo = 0.0 ;
        if(count3 > 0){
            aDoubleHaveTwo = gbDepartmentBillService.queryGbDepBillsSubTotal(mapSubHave);
        }


        Map<String, Object> map3 = new HashMap<>();
        map3.put("arr", billEntities);
        map3.put("month", formatWhatMonth(0));
        map3.put("unSubtotal", new BigDecimal(aDouble).setScale(1,BigDecimal.ROUND_HALF_UP));
        map3.put("haveSubtotal", new BigDecimal(aDoubleHave).setScale(1,BigDecimal.ROUND_HALF_UP));

        Map<String, Object> map4 = new HashMap<>();
        map4.put("arr", billEntities1);
        map4.put("month", getLastMonth());
        map4.put("unSubtotal", new BigDecimal(aDoubleOne).setScale(1,BigDecimal.ROUND_HALF_UP));
        map4.put("haveSubtotal", new BigDecimal(aDoubleHaveOne).setScale(1,BigDecimal.ROUND_HALF_UP));
        Map<String, Object> map5 = new HashMap<>();
        map5.put("arr", billEntities2);
        map5.put("month", getLastTwoMonth());
        map5.put("unSubtotal", new BigDecimal(aDoubleTwo).setScale(1,BigDecimal.ROUND_HALF_UP));
        map5.put("haveSubtotal", new BigDecimal(aDoubleHaveTwo).setScale(1,BigDecimal.ROUND_HALF_UP));

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
        if(count > 0){
             aDoubleTotal = gbDepartmentBillService.queryGbDepBillsSubTotal(maptotal);
        }
        Map<String, Object> map6 = new HashMap<>();
        map6.put("total", new BigDecimal(aDoubleTotal).setScale(1,BigDecimal.ROUND_HALF_UP));
        map6.put("count", count);
        map6.put("bill", resultData );

        return R.ok().put("data",map6);
    }

    @RequestMapping(value = "/getDepBills", method = RequestMethod.POST)
    @ResponseBody
    public R getDepBills(Integer depFatherId, String startDate, String stopDate, Integer issueDepId, Integer nxDisId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depFatherId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        if(issueDepId != -1){
            map.put("issueDepId", issueDepId);
        }
        if(nxDisId != -1){
            map.put("nxDisId", nxDisId);
        }
        List<GbDepartmentBillEntity> billEntities = gbDepartmentBillService.queryBillsByParamsGb(map);
        Double aDouble = gbDepartmentBillService.queryGbDepBillsSubTotal(map);
        Map<String, Object> mapR = new HashMap<>();
        if(billEntities.size() > 0){
            mapR.put("arr",billEntities);
            mapR.put("billTotal", new BigDecimal(aDouble).setScale(1,BigDecimal.ROUND_HALF_UP) );
            return R.ok().put("data", mapR);

        }else{
            return R.error(-1,"没有数据");
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
        System.out.println("Zahsuifuda" + salesBill.getGbDbIssueOrderType());
        if (salesBill.getGbDbIssueNxDisId() != null) {
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

    @RequestMapping(value = "/getDepartmentAccountBills")
    @ResponseBody
    public R getDepartmentAccountBills(Integer depFatherId, Integer issueDepId, String startDate, String stopDate,
                                       Integer orderType, String[]  orderTypes) {



        //本月的账单
        Map<String, Object> map = new HashMap<>();
        if(depFatherId != -1){
            map.put("depId", depFatherId);
        }
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("dayuStatus", -1);
        if(orderType != -1){
            map.put("orderType", orderType);
        }
        if(orderType != -1){
            map.put("orderTypes", orderTypes);
        }
        if (issueDepId != -1) {
            map.put("issueDepId", issueDepId);
        }

        System.out.println("roodoososossos" + map);

        List<GbDepartmentBillEntity> billEntityList = gbDepartmentBillService.queryBillsByParamsGb(map);

        //查询总账款金额
        String formatSubTotal = "0";
        String formatSellSubTotal = "0";
        Map<String, Object> map12 = new HashMap<>();
        if(depFatherId != -1){
            map12.put("depId", depFatherId);
        }
        map12.put("startDate", startDate);
        map12.put("stopDate", stopDate);
        map12.put("dayuStatus", -1);
        if(orderType != -1){
            map12.put("orderType", orderType);
        }
        if(orderType != -1){
            map.put("orderTypes", orderTypes);
        }
        if (issueDepId != -1) {
            map12.put("issueDepId", issueDepId);
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
    public R getPurchaserDepartmentBill(Integer depId, Integer searchDepId, String[] orderTypes) {

        //第一个月
        Map<String, Object> map = new HashMap<>();

        if(searchDepId != -1){
            map.put("depId", searchDepId);
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
            gbDepartmentOrdersService.update(orders);
        }
        return R.ok();
    }


}
