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


    @RequestMapping(value = "/disGetBuyType", method = RequestMethod.POST)
    @ResponseBody
    public R disGetBuyType(Integer disId, Integer type) {
        List<NxDistributerPayEntity> list = new ArrayList<>();
        if (type == 0) {
            NxDistributerPayEntity payEntity = new NxDistributerPayEntity();
            payEntity.setNxNdpBuyQuantity("10");
            payEntity.setNxNdpPaySubtotal("1000");
            payEntity.setPerPrice("1");
            list.add(payEntity);
            NxDistributerPayEntity payEntity1 = new NxDistributerPayEntity();
            payEntity1.setNxNdpBuyQuantity("22");
            payEntity1.setNxNdpPaySubtotal("2000");
            payEntity1.setPerPrice("0.9");
            list.add(payEntity1);
            NxDistributerPayEntity payEntity2 = new NxDistributerPayEntity();
            payEntity2.setNxNdpBuyQuantity("37.5");
            payEntity2.setNxNdpPaySubtotal("3000");
            payEntity2.setPerPrice("0.8");
            list.add(payEntity2);

        }
        if (type == 1) {


            NxDistributerPayEntity payEntity = new NxDistributerPayEntity();
            payEntity.setNxNdpBuyQuantity("1");
            payEntity.setNxNdpPaySubtotal("1800");
//            payEntity.setNxNdpPaySubtotal("1.8");
            payEntity.setPerPrice("5");
            list.add(payEntity);
            NxDistributerPayEntity payEntity1 = new NxDistributerPayEntity();
            payEntity1.setNxNdpBuyQuantity("2");
//            payEntity1.setNxNdpPaySubtotal("3.420");
            payEntity1.setNxNdpPaySubtotal("3420");
            payEntity1.setPerPrice("4.75");
            list.add(payEntity1);
            NxDistributerPayEntity payEntity3 = new NxDistributerPayEntity();
            payEntity3.setNxNdpBuyQuantity("3");
//            payEntity3.setNxNdpPaySubtotal("4.860");
            payEntity3.setNxNdpPaySubtotal("4860");
            payEntity3.setPerPrice("4.5");
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


            NxDistributerPayEntity payEntity2 = new NxDistributerPayEntity();
            payEntity2.setNxNdpBuyQuantity("1");
            payEntity2.setNxNdpPaySubtotal("3500");
//            payEntity2.setNxNdpPaySubtotal("3.500");
            payEntity2.setPerPrice("顾客自助下单机");
            payEntity2.setNxNdpImgUrl("uploadImage/m_2.jpg");
            payEntity2.setNxNdpSellDetail("顾客可以通过自助POS机自行下单，减少了排队等候服务员的时间，尤其是在高峰时段，可以极大地提升店铺的运营效率，避免因排队导致顾客流失。");
            list.add(payEntity2);

            NxDistributerPayEntity payEntity3 = new NxDistributerPayEntity();
            payEntity3.setNxNdpBuyQuantity("1");
            payEntity3.setNxNdpPaySubtotal("2950");
            payEntity3.setPerPrice("一体机");
            payEntity3.setNxNdpImgUrl("uploadImage/m_3.jpg");
            payEntity3.setNxNdpSellDetail("任何用户可以通过直观的图形界面快速完成下单、查询、结账等操作，无需复杂的培训，多联单打印功能可以为商家提供订单、收据等多份副本的打印需求。");
            list.add(payEntity3);

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

    @RequestMapping(value = "/disGetPayListLiuliang", method = RequestMethod.POST)
    @ResponseBody
    public R disGetPayListLiuliang(Integer disId, String startDate, String stopDate) {

        Integer howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);

        List<Map<String,Object>>  list =  new ArrayList<>();

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
				mapDay.put("date", whichDay);
                Map<String, Object> map = new HashMap<>();
                map.put("disId", disId);
                map.put("type", 1);
                map.put("date", whichDay);
                System.out.println("mappd" + map);


		double total = nxDistributerPayListService.queryDisPayListSubtotal(map);
		int count = nxDistributerPayListService.queryDisPayListCount(map);
				mapDay.put("total", total);
				mapDay.put("count", count);
				if(count > 0){
					list.add(mapDay);
				}

            }
        }

        return R.ok().put("data", list);

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
            if (type == 1) {
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
                            BigDecimal decimal = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());
                            System.out.println("decimdall========" + decimal);
                            BigDecimal decimal1 = new BigDecimal(payEntity.getNxNdpBuyQuantity()).multiply(BigDecimal.valueOf(10000));
                            System.out.println("nxdkkddkdk00" + nxDistributerEntity.getNxDistributerBuyQuantity());
                            BigDecimal add = decimal.add(decimal1).setScale(0,BigDecimal.ROUND_HALF_UP);
                            nxDistributerEntity.setNxDistributerBuyQuantity(add.toString());
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
