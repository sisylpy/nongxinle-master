package com.nongxinle.controller;

/**
 * @author lpy
 * @date 10-18 12:14
 */

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import com.github.wxpay.sdk.WXPay;
import com.nongxinle.entity.*;
import com.nongxinle.service.NxDistributerPayListService;
import com.nongxinle.service.NxDistributerService;
import com.nongxinle.service.NxMarketPricePlanService;
import com.nongxinle.utils.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxDistributerPayService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.DateUtils.*;


@RestController
@RequestMapping("api/nxdistributerpay")
public class NxDistributerPayController {
    @Autowired
    private NxDistributerPayService nxDistributerPayService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private NxDistributerPayListService nxDistributerPayListService;
    @Autowired
    private NxMarketPricePlanService nxMarketPricePlanService;


    @RequestMapping(value = "/disGetBuyType", method = RequestMethod.POST)
    @ResponseBody
    public R disGetBuyType(Integer disId, Integer type) {
        List<NxDistributerPayEntity> list = new ArrayList<>();
        if (type == 0) {


            NxDistributerPayEntity payEntity = new NxDistributerPayEntity();
            payEntity.setNxNdpBuyQuantity("3");
//            payEntity.setNxNdpPaySubtotal("3");
            payEntity.setNxNdpPaySubtotal("3000");
            payEntity.setPerPrice("1");
            list.add(payEntity);
            NxDistributerPayEntity payEntity1 = new NxDistributerPayEntity();
            payEntity1.setNxNdpBuyQuantity("5");
            payEntity1.setNxNdpPaySubtotal("4500");
            payEntity1.setPerPrice("0.9");
            list.add(payEntity1);
            NxDistributerPayEntity payEntity3 = new NxDistributerPayEntity();
            payEntity3.setNxNdpBuyQuantity("10");
            payEntity3.setNxNdpPaySubtotal("8000");
            payEntity3.setPerPrice("0.8");
            list.add(payEntity3);

        }
        if (type == 1) {


            NxDistributerPayEntity payEntity = new NxDistributerPayEntity();
            payEntity.setNxNdpBuyQuantity("1");
//            payEntity.setNxNdpPaySubtotal("18000");
            payEntity.setNxNdpPaySubtotal("1.8");
            payEntity.setPerPrice("1500");
            list.add(payEntity);
            NxDistributerPayEntity payEntity1 = new NxDistributerPayEntity();
            payEntity1.setNxNdpBuyQuantity("2");
//            payEntity1.setNxNdpPaySubtotal("3.4");
            payEntity1.setNxNdpPaySubtotal("31200");
            payEntity1.setPerPrice("1300");
            list.add(payEntity1);
            NxDistributerPayEntity payEntity3 = new NxDistributerPayEntity();
            payEntity3.setNxNdpBuyQuantity("3");
//            payEntity3.setNxNdpPaySubtotal("4.860");
            payEntity3.setNxNdpPaySubtotal("39600");
            payEntity3.setPerPrice("1100");
            list.add(payEntity3);

        }
        if (type == 2) {
            NxDistributerPayEntity payEntity = new NxDistributerPayEntity();
            payEntity.setNxNdpBuyQuantity("1");
            payEntity.setNxNdpPaySubtotal("280");
//            payEntity.setNxNdpPaySubtotal("2.80");
            payEntity.setPerPrice("便携式小票打印机");
            payEntity.setNxNdpImgUrl("uploadImage/m_0.jpg");
            payEntity.setNxNdpSellDetail("订单打印、标签打印、收据打印。");
            list.add(payEntity);

            NxDistributerPayEntity payEntity1 = new NxDistributerPayEntity();
            payEntity1.setNxNdpBuyQuantity("1");
            payEntity1.setNxNdpPaySubtotal("750");
//            payEntity1.setNxNdpPaySubtotal("7.50");
            payEntity1.setPerPrice("小票针打");
            payEntity1.setNxNdpSellDetail("多联单据打印，针式打印机常用于打印送货单、发票等需要多份副本的单据，非常适合需要跟踪交易记录的商家。");
            payEntity1.setNxNdpImgUrl("uploadImage/m_1.jpg");
            list.add(payEntity1);


            NxDistributerPayEntity payEntity3 = new NxDistributerPayEntity();
            payEntity3.setNxNdpBuyQuantity("1");
//            payEntity3.setNxNdpPaySubtotal("450");
            payEntity3.setNxNdpPaySubtotal("4.5");
            payEntity3.setPerPrice("蓝牙称");
            payEntity3.setNxNdpImgUrl("uploadImage/m_3.jpg");
            payEntity3.setNxNdpSellDetail("配合蓝牙打印机，直接打印重量标签、条码、价格标签等，重量会实时传输到“京京出库，系统自动匹配对应订单里的商品，直接填入重量，无需手工输入，避免录错");
            list.add(payEntity3);


            NxDistributerPayEntity payEntity2 = new NxDistributerPayEntity();
            payEntity2.setNxNdpBuyQuantity("1");
            payEntity2.setNxNdpPaySubtotal("3500");
//            payEntity2.setNxNdpPaySubtotal("3.500");
            payEntity2.setPerPrice("顾客自助下单机");
            payEntity2.setNxNdpImgUrl("uploadImage/m_2.jpg");
            payEntity2.setNxNdpSellDetail("顾客可以通过自助POS机自行下单，减少了排队等候服务员的时间，尤其是在高峰时段，可以极大地提升店铺的运营效率，避免因排队导致顾客流失。");
            list.add(payEntity2);


            NxDistributerPayEntity payEntity4 = new NxDistributerPayEntity();
            payEntity4.setNxNdpBuyQuantity("1");
            payEntity4.setNxNdpPaySubtotal("1550");
            payEntity4.setPerPrice("手持称重标签");
            payEntity4.setNxNdpImgUrl("uploadImage/m_4.jpg");
            payEntity4.setNxNdpSellDetail("分拣称重标签机可以帮助配送中心的员工根据订单内容快速分拣货物，并为每个包装打印专属标签，设备将称重、打印、分拣等流程简化为一步，减少了传统分拣工作中多个步骤的切换，提升了操作的流畅性。特别是在繁忙的配送和仓储环境中，能够显著提升操作效率。");

            list.add(payEntity4);

        }

        Map<String, Object> map = new HashMap<>();
        Map<String, Object> mapP = new HashMap<>();
        mapP.put("disId", disId);
        mapP.put("type", type);
        mapP.put("equalStatus", -1);

        System.out.println("mappd" + mapP);
        List<NxDistributerPayEntity> payEntities = nxDistributerPayService.queryDisPayListByParams(mapP);

        map.put("list", list);
        map.put("payEntities", payEntities);

        return R.ok().put("data", map);
    }

    @RequestMapping(value = "/disGetBuyTypeMarket", method = RequestMethod.POST)
    @ResponseBody
    public R disGetBuyTypeMarket(Integer marketId, Integer type) {
        try {
            // 1. 参数验证
            if (marketId == null) {
                return R.error("市场ID不能为空");
            }
            if (type == null) {
                return R.error("方案类型不能为空");
            }
            
            System.out.println("获取价格方案：市场ID=" + marketId + ", 类型=" + type);
            
            // 2. 从数据库获取市场价格方案
            List<NxMarketPricePlanEntity> pricePlans = nxMarketPricePlanService.queryByMarketAndType(marketId, type);
            
            // 3. 转换为前端需要的格式
            List<NxDistributerPayEntity> list = new ArrayList<>();
            for (NxMarketPricePlanEntity plan : pricePlans) {
                NxDistributerPayEntity payEntity = new NxDistributerPayEntity();
                payEntity.setNxNdpBuyQuantity(plan.getNxMppQuantity());
                payEntity.setNxNdpPaySubtotal(plan.getNxMppPrice().toString());
                payEntity.setPerPrice(plan.getNxMppUnitPrice());
                payEntity.setNxNdpImgUrl(plan.getNxMppImageUrl());
                payEntity.setNxNdpSellDetail(plan.getNxMppDescription());
                list.add(payEntity);
            }
            
            // 4. 返回结果
            Map<String, Object> map = new HashMap<>();
            map.put("list", list);
            
            System.out.println("返回价格方案数量：" + list.size());
            
            return R.ok().put("data", map);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取价格方案失败：" + e.getMessage());
            return R.error("获取价格方案失败：" + e.getMessage());
        }
    }

    @RequestMapping(value = "/buyMachines", method = RequestMethod.POST)
    @ResponseBody
    public R buyMachines(@RequestBody List<NxDistributerPayEntity> payEntityList) {

        System.out.println("dee" + payEntityList);

        MyWxPayConfig config = new MyWxPayConfig();
        String openId = "";
        Double aDouble = 0.0;
        for (NxDistributerPayEntity payEntity : payEntityList) {
            aDouble = aDouble + Double.parseDouble(payEntity.getNxNdpPaySubtotal()) * 100;
            openId = payEntity.getPayUserOpenId();
        }


        System.out.println("subsosososoososo" + aDouble);
        int i = aDouble.intValue();
        String s1 = String.valueOf(i);
        String tradeNo = CommonUtils.generateOutTradeNo();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxdistributerpay/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", openId);

        System.out.println("paassss" + params);

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

            for (NxDistributerPayEntity payEntity : payEntityList) {

                payEntity.setNxNdpTradeNo(tradeNo);
                
                // 确保设置了配送商ID（从第一个payEntity中获取）
                if (payEntity.getNxNdpNxDisId() == null && !payEntityList.isEmpty()) {
                    // 尝试从其他字段获取配送商ID，或者设置为默认值
                    System.out.println("警告：payEntity缺少配送商ID，请检查disGetBuyType方法");
                }

                String startDate = formatWhatDay(0);
                String stopDate = afterWhatDay(startDate, 365);
                String couponStartTime = "00:00:00:00";
                String couponStopTime = "23:59:59:00";
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

                payEntity.setNxNdpFromTime(beginTime);
                payEntity.setNxNdpStopTime(stopTime);
                payEntity.setNxNdpStatus(-1);
                payEntity.setNxNdpOrderQuantity(0);
                payEntity.setNxNdpImgUrl(payEntity.getNxNdpImgUrl());
                payEntity.setNxNdpSellDetail(payEntity.getNxNdpSellDetail());
                payEntity.setPerPrice(payEntity.getPerPrice());
                nxDistributerPayService.save(payEntity);
            }


            return R.ok().put("map", reMap);


        } catch (Exception e) {
            e.printStackTrace();
        }


        return R.ok();
    }

    @RequestMapping(value = "/disBuyApp", method = RequestMethod.POST)
    @ResponseBody
    public R disBuyApp(Integer disId, String subtotal, String openId, String quantity, Integer type) {


        MyWxPayConfig config = new MyWxPayConfig();

        Double aDouble = Double.parseDouble(subtotal) * 100;
        int i = aDouble.intValue();
        String s1 = String.valueOf(i);
        String tradeNo = CommonUtils.generateOutTradeNo();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxdistributerpay/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", openId);

        System.out.println("paassss" + params);

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


            NxDistributerPayEntity payEntity = new NxDistributerPayEntity();
            payEntity.setNxNdpNxDisId(disId);
            payEntity.setNxNdpPaySubtotal(subtotal);
            payEntity.setNxNdpType(type);
            payEntity.setNxNdpStatus(-1);
            payEntity.setNxNdpTradeNo(tradeNo);
            String startDate = formatWhatDay(0);
            int  haoLong = Integer.valueOf(quantity) * 365;
            String stopDate = afterWhatDay(startDate, haoLong);
            String couponStartTime = "00:00:00:00";
            String couponStopTime = "23:59:59:00";
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

            payEntity.setNxNdpFromTime(beginTime);
            payEntity.setNxNdpStopTime(stopTime);
            payEntity.setNxNdpOrderQuantity(0);
            payEntity.setNxNdpBuyQuantity(quantity); // 设置充值点数
            payEntity.setNxNdpImgUrl(payEntity.getNxNdpImgUrl());
            payEntity.setNxNdpSellDetail(payEntity.getNxNdpSellDetail());
            payEntity.setPerPrice(payEntity.getPerPrice());
            nxDistributerPayService.save(payEntity);

            return R.ok().put("map", reMap);


        } catch (Exception e) {
            e.printStackTrace();
        }


        return R.ok();
    }

    @RequestMapping(value = "/setPayForDep", method = RequestMethod.POST)
    @ResponseBody
    public R setPayForDep(@RequestBody NxDistributerPayEntity payEntity) {

        System.out.println("dee" + payEntity);

        MyWxPayConfig config = new MyWxPayConfig();
        Double aDouble = Double.parseDouble(payEntity.getNxNdpPaySubtotal()) * 100;
        int i = aDouble.intValue();
        String s1 = String.valueOf(i);
        String tradeNo = CommonUtils.generateOutTradeNo();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxdistributerpay/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", payEntity.getPayUserOpenId());

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

            payEntity.setNxNdpTradeNo(tradeNo);

            String startDate = formatWhatDay(0);
            int multiply = new BigDecimal(payEntity.getNxNdpBuyQuantity()).multiply(new BigDecimal(30)).intValue();
            String stopDate = afterWhatDay(startDate, multiply);
            String couponStartTime = "00:00:00:00";
            String couponStopTime = "23:59:59:00";
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

            payEntity.setNxNdpFromTime(beginTime);
            payEntity.setNxNdpStopTime(stopTime);
            payEntity.setNxNdpStatus(-1);
            payEntity.setNxNdpOrderQuantity(0);
            nxDistributerPayService.save(payEntity);

            reMap.put("orderId", payEntity.getNxDistributerPayId().toString());

            return R.ok().put("map", reMap);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return R.ok();
    }


    @RequestMapping(value = "/disGetPayList", method = RequestMethod.POST)
    @ResponseBody
    public R disGetPayList(Integer disId, Integer type) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);

        System.out.println("mappd" + map);
        List<NxDistributerPayEntity> payEntities = nxDistributerPayService.queryDisPayListByParams(map);
        return R.ok().put("data", payEntities);

    }

    /**
     * 查询市场充值记录列表
     * 获取某市场的所有充值记录，包含配送商信息
     * 
     * @param marketId 市场ID（必填）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param stopDate 结束日期（可选，格式：yyyy-MM-dd）
     * @param offset 分页偏移量（可选，默认0）
     * @param limit 分页大小（可选，默认20）
     * @return 充值记录列表
     */
    @RequestMapping(value = "/marketRechargeStats", method = RequestMethod.GET)
    @ResponseBody
    public R marketRechargeStats(@RequestParam("marketId") Integer marketId,
                                 @RequestParam(value = "startDate", required = false) String startDate,
                                 @RequestParam(value = "stopDate", required = false) String stopDate,
                                 @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
                                 @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit) {
        try {
            System.out.println("========== 查询市场充值记录开始 ==========");
            System.out.println("请求参数 - marketId: " + marketId);
            System.out.println("请求参数 - startDate: " + startDate);
            System.out.println("请求参数 - stopDate: " + stopDate);
            System.out.println("请求参数 - offset: " + offset);
            System.out.println("请求参数 - limit: " + limit);
            
            // 1. 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("marketId", marketId); // 直接按市场ID查询
            params.put("type", 1); // nx_ndp_type = 1 (购买点数)
            params.put("equalStatus", 0); // nx_ndp_status = 0 (支付成功)
            
            // 日期参数处理
            if (startDate != null && !startDate.isEmpty()) {
                params.put("startDate", startDate);
            }
            if (stopDate != null && !stopDate.isEmpty()) {
                params.put("stopDate", stopDate);
            }
            
            System.out.println("查询参数 params: " + params);

            // 2. 直接查询该市场的充值记录（已包含配送商信息）
            List<Map<String, Object>> rechargeRecords = nxDistributerPayService.queryRechargeByMarketId(params);
            
            System.out.println("查询到充值记录数量: " + (rechargeRecords != null ? rechargeRecords.size() : "null"));
            
            if (rechargeRecords == null || rechargeRecords.isEmpty()) {
                System.out.println("没有查询到充值记录，返回空结果");
                return R.ok()
                    .put("list", new ArrayList<>())
                    .put("total", 0)
                    .put("summary", new HashMap<String, Object>() {{
                        put("totalAmount", 0.00);
                        put("totalPoints", 0);
                        put("rechargeCount", 0);
                        put("distributerCount", 0);
                    }});
            }

            // 3. 计算统计信息
            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalPoints = 0;
            int distributerCount = 0;
            
            for (Map<String, Object> record : rechargeRecords) {
                // 统计金额
                String payAmount = (String) record.get("payAmount");
                if (payAmount != null && !payAmount.isEmpty()) {
                    try {
                        totalAmount = totalAmount.add(new BigDecimal(payAmount));
                    } catch (NumberFormatException e) {
                        System.out.println("金额格式错误: " + payAmount);
                    }
                }
                
                // 统计点数
                String rechargePoints = (String) record.get("rechargePoints");
                if (rechargePoints != null && !rechargePoints.isEmpty()) {
                    try {
                        totalPoints += Integer.parseInt(rechargePoints);
                    } catch (NumberFormatException e) {
                        System.out.println("点数格式错误: " + rechargePoints);
                    }
                }
            }

            // 4. 分页处理
            int total = rechargeRecords.size();
            int startIndex = offset;
            int endIndex = Math.min(offset + limit, total);
            
            List<Map<String, Object>> pagedRecords = new ArrayList<>();
            if (startIndex < total) {
                pagedRecords = rechargeRecords.subList(startIndex, endIndex);
            }

            // 5. 构建汇总信息
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalAmount", totalAmount.doubleValue());
            summary.put("totalPoints", totalPoints);
            summary.put("rechargeCount", total);
            summary.put("distributerCount", 1); // 简化：假设市场只有一个配送商
            
            System.out.println("统计汇总 - totalAmount: " + totalAmount.doubleValue());
            System.out.println("统计汇总 - totalPoints: " + totalPoints);
            System.out.println("统计汇总 - rechargeCount: " + total);
            System.out.println("分页后记录数: " + pagedRecords.size() + "/" + total);
            System.out.println("========== 查询市场充值记录结束 ==========");

            return R.ok()
                .put("list", pagedRecords)
                .put("total", total)
                .put("summary", summary);

        } catch (Exception e) {
            System.out.println("查询失败异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询市场配送商列表（含剩余点数）
     * 
     * @param marketId 市场ID（必填）
     * @return 配送商列表，包含剩余点数信息
     */
    @RequestMapping(value = "/marketDistributers", method = RequestMethod.GET)
    @ResponseBody
    public R marketDistributers(@RequestParam("marketId") Integer marketId) {
        try {
            List<NxDistributerEntity> distributers = nxDistributerService.queryByMarketId(marketId);
            return R.ok().put("list", distributers);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }


    @ResponseBody
    @RequestMapping(value = "/disPayUser", method = RequestMethod.POST)
    public R disPayUser(String subtotal, String openId, Integer payId) {

        MyWxPayConfig config = new MyWxPayConfig();
        Double aDouble = Double.parseDouble(subtotal) * 100;
        int i = aDouble.intValue();
        String s1 = String.valueOf(i);
        String tradeNo = CommonUtils.generateOutTradeNo();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxdistributerpay/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", openId);

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

            NxDistributerPayEntity payEntity = nxDistributerPayService.queryPayItemByPayId(payId);
            payEntity.setNxNdpTradeNo(tradeNo);
            payEntity.setNxNdpPayTime(formatWhatTime(0));
            nxDistributerPayService.update(payEntity);
            reMap.put("orderId", payEntity.getNxDistributerPayId().toString());
            return R.ok().put("map", reMap);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return R.ok();
    }

    @ResponseBody
    @RequestMapping(value = "/disBuyUser", method = RequestMethod.POST)
    public R disBuyUser(Integer disId, String subtotal, String openId, String quantity, Integer type) {

        MyWxPayConfig config = new MyWxPayConfig();
        Double aDouble = Double.parseDouble(subtotal) * 100;
        int i = aDouble.intValue();
        String s1 = String.valueOf(i);
        String tradeNo = CommonUtils.generateOutTradeNo();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxdistributerpay/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", openId);

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

            NxDistributerPayEntity payEntity = new NxDistributerPayEntity();
            payEntity.setNxNdpNxDisId(disId);
            payEntity.setNxNdpPaySubtotal(subtotal);
            payEntity.setNxNdpType(type);
            payEntity.setNxNdpStatus(-1);
            payEntity.setNxNdpTradeNo(tradeNo);
            // 检查quantity参数
            if (quantity == null || quantity.trim().isEmpty()) {
                quantity = "0";
                System.out.println("警告：quantity参数为null或空，设为0");
            }
            
            if (type == 0) {
                int multiply = new BigDecimal(quantity).multiply(new BigDecimal(10000)).intValue();
                payEntity.setNxNdpBuyQuantity(String.valueOf(multiply));
            } else {
                payEntity.setNxNdpBuyQuantity(String.valueOf(quantity));
            }

            payEntity.setNxNdpStatus(-1);
            payEntity.setNxNdpOrderQuantity(0);
            nxDistributerPayService.save(payEntity);

            reMap.put("orderId", payEntity.getNxDistributerPayId().toString());

            return R.ok().put("map", reMap);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return R.ok();

    }

    @ResponseBody
    @RequestMapping(value = "/disBuyUserMarket", method = RequestMethod.POST)
    public R disBuyUserMarket(Integer disId, String subtotal, String openId, String quantity, Integer type, Integer marketId) {

        MyWxPayConfig config = new MyWxPayConfig();
        Double aDouble = Double.parseDouble(subtotal) * 100;
        int i = aDouble.intValue();
        String s1 = String.valueOf(i);
        String tradeNo = CommonUtils.generateOutTradeNo();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxdistributerpay/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", openId);

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

            NxDistributerPayEntity payEntity = new NxDistributerPayEntity();
            payEntity.setNxNdpNxDisId(disId);
            payEntity.setNxNdpPaySubtotal(subtotal);
            payEntity.setNxNdpType(type);
            payEntity.setNxNdpStatus(-1);
            payEntity.setNxNdpTradeNo(tradeNo);
            // 检查quantity参数
            if (quantity == null || quantity.trim().isEmpty()) {
                quantity = "0";
                System.out.println("警告：quantity参数为null或空，设为0");
            }

            if (type == 0) {
                int multiply = new BigDecimal(quantity).multiply(new BigDecimal(10000)).intValue();
                payEntity.setNxNdpBuyQuantity(String.valueOf(multiply));
            } else {
                payEntity.setNxNdpBuyQuantity(String.valueOf(quantity));
            }

            payEntity.setNxNdpStatus(-1);
            payEntity.setNxNdpOrderQuantity(0);
            nxDistributerPayService.save(payEntity);

            reMap.put("orderId", payEntity.getNxDistributerPayId().toString());

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

                List<NxDistributerPayEntity> list = nxDistributerPayService.queryItemListByTradeNo(ordersSn);
                if (list.size() > 0) {
                    for (NxDistributerPayEntity payEntity : list) {
                        payEntity.setNxNdpStatus(0);
                        payEntity.setNxNdpPayTime(formatWhatYearDayTime(0));
                        nxDistributerPayService.update(payEntity);

                        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(payEntity.getNxNdpNxDisId());

                        System.out.println("payEntity.getNxNdpType()===" + payEntity.getNxNdpType());
                        if (payEntity.getNxNdpType() == 0) {
                            // 检查配送商当前点数，如果为null则设为0
                            String currentQuantity = nxDistributerEntity.getNxDistributerBuyQuantity();
                            if (currentQuantity == null || currentQuantity.trim().isEmpty()) {
                                currentQuantity = "0";
                            }
                            BigDecimal decimal = new BigDecimal(currentQuantity);
                            System.out.println("decimdall========" + decimal);
                            
                            // 检查充值点数，如果为null则设为0
                            String buyQuantity = payEntity.getNxNdpBuyQuantity();
                            if (buyQuantity == null || buyQuantity.trim().isEmpty()) {
                                buyQuantity = "0";
                                System.out.println("警告：充值记录的点数为null或空，设为0");
                            }
                            BigDecimal decimal1 = new BigDecimal(buyQuantity).multiply(BigDecimal.valueOf(10000));
                            System.out.println("nxdkkddkdk00" + currentQuantity);
                            BigDecimal add = decimal.add(decimal1).setScale(0, BigDecimal.ROUND_HALF_UP);
                            nxDistributerEntity.setNxDistributerBuyQuantity(add.toString());
                            System.out.println("nxdkkddkdk11" + nxDistributerEntity.getNxDistributerBuyQuantity());
                            nxDistributerService.update(nxDistributerEntity);
                        }
                        if (payEntity.getNxNdpType() == 1) {
                            // 检查配送商当前点数，如果为null则设为0
                            String currentQuantity = nxDistributerEntity.getNxDistributerBuyQuantity();
                            if (currentQuantity == null || currentQuantity.trim().isEmpty()) {
                                currentQuantity = "0";
                            }
                            BigDecimal decimal = new BigDecimal(currentQuantity);
                            System.out.println("decimdall========" + decimal);
                            
                            // 检查充值点数，如果为null则设为0
                            String buyQuantity = payEntity.getNxNdpBuyQuantity();
                            if (buyQuantity == null || buyQuantity.trim().isEmpty()) {
                                buyQuantity = "0";
                                System.out.println("警告：充值记录的点数为null或空，设为0");
                            }
                            BigDecimal decimal1 = new BigDecimal(buyQuantity).multiply(BigDecimal.valueOf(10000));
                            System.out.println("nxdkkddkdk00" + currentQuantity);
                            BigDecimal add = decimal.add(decimal1).setScale(0, BigDecimal.ROUND_HALF_UP);
                            nxDistributerEntity.setNxDistributerBuyQuantity(add.toString());
                            nxDistributerEntity.setNxDistributerType(0);
                            nxDistributerEntity.setNxDistributerBusinessTypeId(2);
                            System.out.println("nxdkkddkdk11" + nxDistributerEntity.getNxDistributerBuyQuantity());
                            nxDistributerService.update(nxDistributerEntity);
                        }
                    }
                }
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


}
