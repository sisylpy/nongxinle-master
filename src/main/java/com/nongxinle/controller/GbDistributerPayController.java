package com.nongxinle.controller;

/**
 * @author lpy
 * @date 01-08 12:27
 */

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import com.github.wxpay.sdk.WXPay;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.getHeadStringByString;


@RestController
@RequestMapping("api/gbdistributerpay")
public class GbDistributerPayController {
    @Autowired
    private GbDistributerPayService gbDistributerPayService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
	private GbDepartmentService gbDepartmentService;
    @Autowired
	private GbDistributerModuleService gbDistributerModuleService;



    @ResponseBody
    @RequestMapping(value = "/disPayUser", method = RequestMethod.POST)
    public R disPayUser(String subtotal, String openId, Integer payId) {

//        MyWxPayConfig config = new MyWxPayConfig();
        MyWxJJCGPayConfig config = new MyWxJJCGPayConfig();

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
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/gbdistributerpay/notify");
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

            GbDistributerPayEntity payEntity = gbDistributerPayService.queryPayItemByPayId(payId);
            payEntity.setGbGdpTradeNo(tradeNo);
            payEntity.setGbGdpPayTime(formatWhatTime(0));
            gbDistributerPayService.update(payEntity);
            reMap.put("orderId", payEntity.getGbDistributerPayId().toString());
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
        System.out.println("mappdaaa" + map);
        List<GbDistributerPayEntity> payEntities = gbDistributerPayService.queryDisPayListByParams(map);
        return R.ok().put("data", payEntities);

    }


    @RequestMapping(value = "/buyMachines", method = RequestMethod.POST)
    @ResponseBody
    public R buyMachines(@RequestBody List<GbDistributerPayEntity> payEntityList) {

        System.out.println("dee" + payEntityList);

        MyWxJJCGPayConfig config = new MyWxJJCGPayConfig();
        String openId = "";
        Double aDouble = 0.0;
        for (GbDistributerPayEntity payEntity : payEntityList) {
            openId = payEntity.getPayUserOpenId();
            aDouble = aDouble + Double.parseDouble(payEntity.getGbGdpPaySubtotal()) * 100;
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
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/gbdistributerpay/notifyMachine");
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

            for (GbDistributerPayEntity payEntity : payEntityList) {

                payEntity.setGbGdpTradeNo(tradeNo);

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
                Date beginTTT = Date.from(beginTime.atZone(ZoneId.systemDefault()).toInstant());
                Date stopTTT = Date.from(stopTime.atZone(ZoneId.systemDefault()).toInstant());

                payEntity.setGbGdpFromTime(beginTTT);
                payEntity.setGbGdpStopTime(stopTTT);
                payEntity.setGbGdpStatus(-1);
                payEntity.setGbGdpImgUrl(payEntity.getGbGdpImgUrl());
                payEntity.setGbGdpSellDetail(payEntity.getGbGdpSellDetail());
                payEntity.setGbGdpPaySubtotal(payEntity.getGbGdpPaySubtotal());
                gbDistributerPayService.save(payEntity);
            }


            return R.ok().put("map", reMap);


        } catch (Exception e) {
            e.printStackTrace();
        }


        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/gbDisBuyUser", method = RequestMethod.POST)
    public R gbDisBuyUser(Integer disId, String subtotal, String openId, String quantity, Integer type) {

        System.out.println("lppdodoeoeoeeoeoogbgbbgbg");
        MyWxJJCGPayConfig config = new MyWxJJCGPayConfig();
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
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/gbdistributerpay/notify");
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

            GbDistributerPayEntity payEntity = new GbDistributerPayEntity();
            payEntity.setGbGdpGbDisId(disId);
            payEntity.setGbGdpPaySubtotal(subtotal);
            payEntity.setGbGdpType(type);
            payEntity.setGbGdpStatus(-1);
            payEntity.setGbGdpTradeNo(tradeNo);
            if (type == 1) {
                int multiply = new BigDecimal(quantity).multiply(new BigDecimal(10000)).intValue();
                payEntity.setGbGdpBuyQuantity(String.valueOf(multiply));
            } else {
                payEntity.setGbGdpBuyQuantity(String.valueOf(quantity));
            }

            payEntity.setGbGdpStatus(-1);
            gbDistributerPayService.save(payEntity);



            reMap.put("orderId", payEntity.getGbDistributerPayId().toString());

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
                Integer gbDisId = -1;
                List<GbDistributerPayEntity> list = gbDistributerPayService.queryListByTradeNo(ordersSn);
                if (list.size() > 0) {
                    for (GbDistributerPayEntity payEntity : list) {
                        gbDisId = payEntity.getGbGdpGbDisId();
                        payEntity.setGbGdpStatus(0);
                        payEntity.setGbGdpPayTime(formatWhatYearDayTime(0));
                        gbDistributerPayService.update(payEntity);

                        GbDistributerEntity nxDistributerEntity = gbDistributerService.queryObject(payEntity.getGbGdpGbDisId());

                        System.out.println("payEntity.getNxNdpType()===" + payEntity.getGbGdpType());
                        if (payEntity.getGbGdpType() == 0) {
                            BigDecimal decimal = new BigDecimal(nxDistributerEntity.getGbDistributerBuyQuantity());
                            System.out.println("decimdall========" + decimal);
                            BigDecimal decimal1 = new BigDecimal(payEntity.getGbGdpBuyQuantity()).multiply(BigDecimal.valueOf(10000));
                            System.out.println("nxdkkddkdk00" + nxDistributerEntity.getGbDistributerBuyQuantity());
                            BigDecimal add = decimal.add(decimal1);
                            nxDistributerEntity.setGbDistributerBuyQuantity(add.toString());
                            if(nxDistributerEntity.getGbDistributerBusinessType() == -1){
                                nxDistributerEntity.setGbDistributerBusinessType(0);
                            }
                            System.out.println("nxdkkddkdk11" + nxDistributerEntity.getGbDistributerBuyQuantity());
                            gbDistributerService.update(nxDistributerEntity);
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

    @RequestMapping("/notifyMachine")
    public String callBackMachine(HttpServletRequest request, HttpServletResponse response) {
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

                List<GbDistributerPayEntity> list = gbDistributerPayService.queryListByTradeNo(ordersSn);

                Integer gbDisId = -1;
                if (list.size() > 0) {
                    for (GbDistributerPayEntity payEntity : list) {

                        gbDisId = payEntity.getGbGdpGbDisId();
                        payEntity.setGbGdpStatus(0);
                        payEntity.setGbGdpPayTime(formatWhatYearDayTime(0));
                        gbDistributerPayService.update(payEntity);

                        GbDistributerEntity nxDistributerEntity = gbDistributerService.queryObject(payEntity.getGbGdpGbDisId());
                        nxDistributerEntity.setGbDistributerBusinessType(1);
                        gbDistributerService.update(nxDistributerEntity);
                        if(payEntity.getGbGdpType() != 0){
							savePayTypeDepartment(nxDistributerEntity,payEntity.getGbGdpType());
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


    private  void  savePayTypeDepartment(GbDistributerEntity  gbDistributerEntity, Integer type){
		GbDepartmentEntity departmentEntity = new GbDepartmentEntity();
		departmentEntity.setGbDepartmentDisId(gbDistributerEntity.getGbDistributerId());
		departmentEntity.setGbDepartmentFatherId(0);
		departmentEntity.setGbDepartmentType(type);
		gbDistributerEntity.setGbDistributerSettleDate(formatWhatDay(0));
		departmentEntity.setGbDepartmentSettleFullTime(formatFullTime());
		departmentEntity.setGbDepartmentSettleDate(formatWhatDay(0));
		departmentEntity.setGbDepartmentSettleMonth(formatWhatMonth(0));
		departmentEntity.setGbDepartmentSettleWeek(getWeekOfYear(0).toString());
		departmentEntity.setGbDepartmentSettleYear(formatWhatYear(0));
		departmentEntity.setGbDepartmentSettleTimes("0");
		departmentEntity.setGbDepartmentSubAmount(0);
		departmentEntity.setGbDepartmentIsGroupDep(1);
		departmentEntity.setGbDepartmentAttrName(gbDistributerEntity.getGbDistributerName());
		departmentEntity.setGbDepartmentName(gbDistributerEntity.getGbDistributerName());
		departmentEntity.setGbDepartmentPrintSet(0);
		String gbDepartmentName = departmentEntity.getGbDepartmentName();
		String headPinyin = getHeadStringByString(gbDepartmentName, false, null);
		departmentEntity.setGbDepartmentNamePy(headPinyin);
		gbDepartmentService.save(departmentEntity);

	   GbDistributerModuleEntity distributerModuleEntity =	gbDistributerModuleService.queryModelByDisId(gbDistributerEntity.getGbDistributerId());
	   if(type == 3){
	   	distributerModuleEntity.setGbDmStockNumber(0);
	   	gbDistributerModuleService.update(distributerModuleEntity);
	   }else if(type == 4){
			distributerModuleEntity.setGbDmCentralKitchenNumber(0);
			gbDistributerModuleService.update(distributerModuleEntity);
		}

	}

    @RequestMapping(value = "/gbDisGetBuyType", method = RequestMethod.POST)
    @ResponseBody
    public R gbDisGetBuyType(Integer disId, Integer type) {
        List<NxDistributerPayEntity> list = new ArrayList<>();
        if (type == 0) {
            NxDistributerPayEntity payEntity = new NxDistributerPayEntity();
            payEntity.setNxNdpBuyQuantity("2.0");
			payEntity.setNxNdpPaySubtotal("198");
//            payEntity.setNxNdpPaySubtotal("3.6");
            payEntity.setPerPrice("1");
            list.add(payEntity);
            NxDistributerPayEntity payEntity1 = new NxDistributerPayEntity();
            payEntity1.setNxNdpBuyQuantity("4.0");
			payEntity1.setNxNdpPaySubtotal("358");
//            payEntity1.setNxNdpPaySubtotal("6.58");
            payEntity1.setPerPrice("0.9");
            list.add(payEntity1);
            NxDistributerPayEntity payEntity2 = new NxDistributerPayEntity();
            payEntity2.setNxNdpBuyQuantity("6.0");
            payEntity2.setNxNdpPaySubtotal("480");
            payEntity2.setPerPrice("0.8");
            list.add(payEntity2);
        }

//        if (type == 1) {
//
//
//            NxDistributerPayEntity payEntity = new NxDistributerPayEntity();
//            payEntity.setNxNdpBuyQuantity("1");
//            payEntity.setNxNdpPaySubtotal("1800");
////            payEntity.setNxNdpPaySubtotal("1.8");
//            payEntity.setPerPrice("5");
//            list.add(payEntity);
//            NxDistributerPayEntity payEntity1 = new NxDistributerPayEntity();
//            payEntity1.setNxNdpBuyQuantity("2");
////            payEntity1.setNxNdpPaySubtotal("3.420");
//            payEntity1.setNxNdpPaySubtotal("3420");
//            payEntity1.setPerPrice("4.75");
//            list.add(payEntity1);
//            NxDistributerPayEntity payEntity3 = new NxDistributerPayEntity();
//            payEntity3.setNxNdpBuyQuantity("3");
////            payEntity3.setNxNdpPaySubtotal("4.860");
//            payEntity3.setNxNdpPaySubtotal("4860");
//            payEntity3.setPerPrice("4.5");
//            list.add(payEntity3);
//
//        }
        if (type == 1) {
            NxDistributerPayEntity payEntity = new NxDistributerPayEntity();
            payEntity.setNxNdpBuyQuantity("1");
			payEntity.setNxNdpPaySubtotal("2800");
            payEntity.setNxNdpType(0);
            payEntity.setPerPrice("连锁店管理端");
            payEntity.setNxNdpImgUrl("uploadImage/imPurchase/guanli.png");
            payEntity.setNxNdpSellDetail("实时监控 精准管理\n 管理者可以随时查看分店库存情况，掌握原料的使用状态、剩余数量，以及新鲜度等关键信息，能够对原料的采购、存储、使用等各环节进行精准的管理。");
            list.add(payEntity);

            NxDistributerPayEntity payEntity1 = new NxDistributerPayEntity();
            payEntity1.setNxNdpBuyQuantity("1");
			payEntity1.setNxNdpPaySubtotal("750");
//            payEntity1.setNxNdpPaySubtotal("7.50");
            payEntity1.setNxNdpType(3);
            payEntity1.setPerPrice("时鲜库房端");
            payEntity1.setNxNdpSellDetail("保持领先\n后厨人员直接参与库存管理和订货，他们可以根据实际使用情况调整库存数据。");
            payEntity1.setNxNdpImgUrl("uploadImage/imPurchase/kufang.png");
            list.add(payEntity1);


            NxDistributerPayEntity payEntity2 = new NxDistributerPayEntity();
            payEntity2.setNxNdpBuyQuantity("1");
            payEntity2.setNxNdpPaySubtotal("3500");
//            payEntity2.setNxNdpPaySubtotal("3.500");
            payEntity2.setPerPrice("时鲜制作");
            payEntity2.setNxNdpType(4);
            payEntity2.setNxNdpImgUrl("uploadImage/imPurchase/zhizuo.png");
            payEntity2.setNxNdpSellDetail("提升原材料新鲜度\n中央厨房能够实时监控原材料的库存情况，包括新鲜度、保质期和库存量。这样可以确保使用的原材料始终处于最佳状态，提高食品的整体质量");
            list.add(payEntity2);

            NxDistributerPayEntity payEntity3 = new NxDistributerPayEntity();
            payEntity3.setNxNdpBuyQuantity("1");
            payEntity3.setNxNdpPaySubtotal("2950");
            payEntity3.setPerPrice("时鲜窗口售卖");
            payEntity3.setNxNdpType(5);
            payEntity3.setNxNdpImgUrl("uploadImage/imPurchase/window.png");
            payEntity3.setNxNdpSellDetail("任何用户可以通过直观的图形界面快速完成下单、查询、结账等操作，无需复杂的培训，多联单打印功能可以为商家提供订单、收据等多份副本的打印需求。");
            list.add(payEntity3);

            NxDistributerPayEntity payEntity4 = new NxDistributerPayEntity();
            payEntity4.setNxNdpBuyQuantity("1");
            payEntity4.setNxNdpPaySubtotal("1550");
            payEntity4.setPerPrice("私域会员");
            payEntity4.setNxNdpType(7);
            payEntity4.setNxNdpImgUrl("uploadImage/imPurchase/member.png");
            payEntity4.setNxNdpSellDetail("解决有些客户的会员管理能力差和内容运营能力薄弱的问题，通过丰富的优惠券和会员卡活动，企业可以轻松开展各种促销活动，吸引更多新用户，并保持老用户的活跃度");

            list.add(payEntity4);

        }

        Map<String, Object> map = new HashMap<>();
        Map<String, Object> mapP = new HashMap<>();
        mapP.put("disId", disId);
        mapP.put("type", type);
        mapP.put("equalStatus", -1);

        System.out.println("mappd" + mapP);
        List<GbDistributerPayEntity> payEntities = gbDistributerPayService.queryDisPayListByParams(mapP);

        map.put("list", list);
        map.put("payEntities", payEntities);
        map.put("liwu", 1000);
        map.put("liwuDay", 3);

        return R.ok().put("data", map);
    }


}
