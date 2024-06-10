package com.nongxinle.controller;

/**
 * @author lpy
 * @date 11-30 15:31
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.WeChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;


@RestController
@RequestMapping("api/nxrestraunt")
public class NxRestrauntController {
    @Autowired
    private NxRestrauntService nxRestrauntService;
    @Autowired
    private NxRestrauntUserService nxRestrauntUserService;
    @Autowired
    private NxRestrauntOrdersService nxRestrauntOrdersService;
    @Autowired
    private NxCommunityUserService nxCommunityUserService;
    @Autowired
    private NxCommunityService nxCommunityService;

    private static final String KEY = "C5HBZ-KEIW2-JXXUJ-COLGS-FQO47-WWFAK";


    @RequestMapping(value = "/getResAndUserInfo/{userId}")
    @ResponseBody
    public R getResAndUserInfo(@PathVariable Integer userId) {
        NxRestrauntUserEntity nxRestrauntUserEntity = nxRestrauntUserService.queryObject(userId);
        Integer restaurantId = nxRestrauntUserEntity.getNxRuRestaurantId();
        NxRestrauntEntity restrauntEntity = nxRestrauntService.queryResInfo(restaurantId);
        Map<String, Object> map = new HashMap<>();
        map.put("resInfo",restrauntEntity );
        map.put("userInfo", nxRestrauntUserEntity);

        return R.ok().put("data", map);
    }

    @RequestMapping(value = "/getResInfo/{resId}")
    @ResponseBody
    public R getResInfo(@PathVariable String resId) {
        NxRestrauntEntity restrauntEntity = nxRestrauntService.queryResInfo(Integer.valueOf(resId));
        return R.ok().put("data",restrauntEntity);
    }


    @RequestMapping(value = "/deliveryCompleteRestraunt", method = RequestMethod.POST)
    @ResponseBody
    public R deliveryCompleteRestraunt (@RequestBody NxRestrauntEntity restraunt ) {

        //更新订单
        Integer nxRestrauntId = restraunt.getNxRestrauntId();
        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", nxRestrauntId);
        map.put("equalStatus", 4);
        List<NxRestrauntOrdersEntity> nxRestrauntOrdersEntities = nxRestrauntOrdersService.queryResOrdersByParams(map);
        for (NxRestrauntOrdersEntity ordersEntity: nxRestrauntOrdersEntities){
            ordersEntity.setNxRoStatus(5);
            ordersEntity.setNxRoDeliveryDate(formatWhatDay(0));
//            ordersEntity.setNxRoArriveDate(formatWhatDay(0));
//            ordersEntity.setNxRoArriveOnlyDate(formatWhatOnlyDay(0));
//            ordersEntity.setNxRoArriveWeeksYear(getWeekOfYear(0));
//            ordersEntity.setNxRoArriveWhatDay(getWeek(0));
//            ordersEntity.setNxRoArriveMonth(formatWhatMonth(0));

            nxRestrauntOrdersService.update(ordersEntity);
        }
        //更新餐馆
        restraunt.setNxRestrauntWorkingStatus(4);
        nxRestrauntService.update(restraunt);
        return R.ok();
    }

    @RequestMapping(value = "/getDriverDeliveryRestraunts/{userId}")
    @ResponseBody
    public R getDriverDeliveryRestraunts(@PathVariable Integer userId ) {

       List<NxRestrauntEntity> restrauntEntities =  nxRestrauntService.queryDriverRestraunts(userId);

        //获取出发点坐标
        StringBuilder stringBuilder = new StringBuilder();
        for (NxRestrauntEntity restraunt : restrauntEntities) {
            String nxRestrauntLat = restraunt.getNxRestrauntLat();
            String nxRestrauntLng = restraunt.getNxRestrauntLng();
            String item  = nxRestrauntLat + "," + nxRestrauntLng;
            stringBuilder.append(item + ";");
        }
        String substring = stringBuilder.substring(0, stringBuilder.length() - 1);


        NxCommunityUserEntity nxCommunityUserEntity = nxCommunityUserService.queryObject(userId);
        Integer communityId = nxCommunityUserEntity.getNxCouCommunityId();
        NxCommunityEntity nxCommunityEntity = nxCommunityService.queryObject(communityId);
        String fromLat = nxCommunityEntity.getNxCommunityLat();
        String fromLng = nxCommunityEntity.getNxCommunityLng();

        String from = fromLat + "," + fromLng;
        String urlString = "http://apis.map.qq.com/ws/distance/v1/optimal_order?mode=driving&from="
                + from + "&to=" + substring + "&key=" + KEY;
        // 发送请求，返回Json字符串
        String result = "";
        try {
            URL url = new URL(urlString);
            System.out.println(url);
            System.out.println("----");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            // 腾讯地图使用GET
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            // 获取地址解析结果
            System.out.println(in);
            while ((line = in.readLine()) != null) {
                result += line + "\n";
            }
            in.close();
        } catch (Exception e) {
            e.getMessage();
        }


//		// 转JSON格式
        JSONObject jsonObject = JSONObject.parseObject(result);
        String optimal_order = (String)jsonObject.getString("result");

        //获取排序
        String order = JSONObject.parseObject(optimal_order).getString("optimal_order");
        System.out.println(order);
        String substring2 = order.substring(0);
        String substring3 = substring2.substring(1, substring2.length() - 1);
        String[] split = substring3.split(",");

        List<NxRestrauntEntity> treeSet = new ArrayList<>();

        String elements = JSONObject.parseObject(optimal_order).getString("elements");
        List<NxRestrauntEntity> list = new ArrayList<>();
        list = JSONObject.parseArray(elements, NxRestrauntEntity.class);

        System.out.println(list);
        System.out.println("list");

        for(int i =0; i < split.length; i++){
            Integer integer = Integer.valueOf(split[i]);

            NxRestrauntEntity nxRestrauntEntity = restrauntEntities.get(integer - 1);
            NxRestrauntEntity listEnitity = list.get(i);
            System.out.println(listEnitity.getDistance()+ "distancececicicicici");
            System.out.println(listEnitity.getDuration() + "durationtitonttino");
            String distance = listEnitity.getDistance();
            String duration = listEnitity.getDuration();
            nxRestrauntEntity.setDistance(distance);
            nxRestrauntEntity.setDuration(duration);
            treeSet.add(nxRestrauntEntity);
        }
        return R.ok().put("data", treeSet);
    }


    @RequestMapping(value = "/getPrepareDeliveryRestraunts/{comId}")
    @ResponseBody
    public R getPrepareDeliveryRestraunts(@PathVariable Integer comId) {
        List<NxRestrauntEntity> restrauntEntities = nxRestrauntService.queryPrepareDeliveryRestraunts(comId);
        return R.ok().put("data", restrauntEntities);
    }


    @RequestMapping(value = "/cancleDeliveryRestraunt/{resId}")
    @ResponseBody
    public R cancleDeliveryRestraunt(@PathVariable Integer resId) {
        NxRestrauntEntity res = nxRestrauntService.queryObject(resId);
        res.setNxRestrauntDriverId(-1);
        res.setNxRestrauntWorkingStatus(2);
        nxRestrauntService.update(res);
        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", resId);
        map.put("equalStatus", 4);
        List<NxRestrauntOrdersEntity> entities = nxRestrauntOrdersService.queryResOrdersByParams(map);
        for (NxRestrauntOrdersEntity ordersEntity :entities) {
            ordersEntity.setNxRoStatus(3);
            nxRestrauntOrdersService.update(ordersEntity);
        }
        return R.ok();
    }


    /**
     * 给司机分派送货餐馆
     * @param restraunts
     * @return
     */
    @RequestMapping(value = "/deliveryRestraunts", method = RequestMethod.POST)
    @ResponseBody
    public R deliveryRestraunts (@RequestBody List<NxRestrauntEntity> restraunts) {
        //更新司机状态
//        NxRestrauntEntity nxRestrauntEntity = restraunts.get(0);
//        Integer nxRestrauntDriverId = nxRestrauntEntity.getNxRestrauntDriverId();
//        NxCommunityUserEntity nxCommunityUserEntity = nxCommunityUserService.queryObject(nxRestrauntDriverId);
//        nxCommunityUserEntity.setNxCouWorkingStatus(1);
//        nxCommunityUserService.update(nxCommunityUserEntity);
        //修改餐馆状态
        for (NxRestrauntEntity res : restraunts) {
            res.setNxRestrauntWorkingStatus(3);
            nxRestrauntService.update(res);
            Integer nxRestrauntId = res.getNxRestrauntId();
            Map<String, Object> map = new HashMap<>();
            map.put("resFatherId", nxRestrauntId);
            map.put("equalStatus", 3);
            List<NxRestrauntOrdersEntity> entities = nxRestrauntOrdersService.queryResOrdersByParams(map);
            for (NxRestrauntOrdersEntity ordersEntity :entities) {
                ordersEntity.setNxRoStatus(4);
                nxRestrauntOrdersService.update(ordersEntity);
            }
        }
        return R.ok();
    }


    /**
     * 装框
     * @param restraunt
     * @return
     */
    @RequestMapping(value = "/saveDeliveryGoods", method = RequestMethod.POST)
    @ResponseBody
    public R saveDeliveryGoods (@RequestBody NxRestrauntEntity restraunt) {

        //1,update orders and make newBill
        List<NxRestrauntOrdersEntity> nxRestrauntOrdersEntities = restraunt.getNxRestrauntOrdersEntities();
        float total = 0;

        for (NxRestrauntOrdersEntity orders : nxRestrauntOrdersEntities) {
            orders.setNxRoStatus(3);
            nxRestrauntOrdersService.update(orders);
//            String nxRoSubtotal = orders.getNxRoSubtotal();
//            float sub =Float.parseFloat(nxRoSubtotal);
//            total = total + sub;

        }

        // 2,update restraunt
        //如果餐馆是第一次分拣
            if(restraunt.getNxRestrauntWorkingStatus() < 2){
                restraunt.setNxRestrauntWorkingStatus(2);
            }


        nxRestrauntService.update(restraunt);


        return R.ok();
    }

    /**
     * PURCHASE
     * 采购员注册
     * @param res 订货群
     * @return 群信息
     */
    @RequestMapping(value = "/resManRegisterNewRestraunt", method = RequestMethod.POST)
    @ResponseBody
    public R resManRegisterNewRestraunt(@RequestBody NxRestrauntEntity res) {
//wxApp
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        NxRestrauntUserEntity nxRestrauntUserEntity = res.getNxRestrauntUserEntity();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getComPurchaseAppID() +
                "&secret=" + myAPPIDConfig.getComPurchaseScreat() + "&js_code=" + nxRestrauntUserEntity.getNxRuCode() + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        NxRestrauntUserEntity nxResUserEntity = nxRestrauntUserService.queryResUserByOpenId(openid);

        if (nxResUserEntity == null) {
            res.getNxRestrauntUserEntity().setNxRuWxOpenId(openid);
            Integer resUserId = nxRestrauntService.saveNewRestraunt(res);

            if (resUserId != null) {
                Map<String, Object> stringObjectMap = nxRestrauntService.queryResAndUserInfo(resUserId);
                return R.ok().put("data", stringObjectMap);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }
    }

    /**
     * PURCHASE
     * 采购员注册
     * @param res 订货群
     * @return 群信息
     */
    @RequestMapping(value = "/chainResManRegisterNewRestraunt", method = RequestMethod.POST)
    @ResponseBody
    public R chainResManRegisterNewRestraunt(@RequestBody NxRestrauntEntity res) {
//wxApp
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        NxRestrauntUserEntity nxRestrauntUserEntity = res.getNxRestrauntUserEntity();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getComPurchaseAppID() +
                "&secret=" + myAPPIDConfig.getComPurchaseScreat() + "&js_code=" + nxRestrauntUserEntity.getNxRuCode() + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        NxRestrauntUserEntity nxResUserEntity = nxRestrauntUserService.queryResUserByOpenId(openid);

        if (nxResUserEntity == null) {
            res.getNxRestrauntUserEntity().setNxRuWxOpenId(openid);
            Integer resUserId = nxRestrauntService.saveNewChainRestraunt(res);

            if (resUserId != null) {
                Map<String, Object> stringObjectMap = nxRestrauntService.queryResAndUserInfo(resUserId);
                return R.ok().put("data", stringObjectMap);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }
    }
    @RequestMapping(value = "/AQEj8E6gAq.txt")
    @ResponseBody
    public String chainResManRes( ) {
        return "8fb2fd84cc1d93eff75db801df3d1040";
    }

    /**
     * PURCHASE
     * 采购员注册
     * @param res 订货群
     * @return 群信息
     */
    @RequestMapping(value = "/restrauntRegiste", method = RequestMethod.POST)
    @ResponseBody
    public R restrauntRegiste(@RequestBody NxRestrauntEntity res) {
//wxApp
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        NxRestrauntUserEntity nxRestrauntUserEntity = res.getNxRestrauntUserEntity();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getRestrauntAppID() +
                "&secret=" + myAPPIDConfig.getRestrauntScreat() + "&js_code=" + nxRestrauntUserEntity.getNxRuCode() + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        NxRestrauntUserEntity nxResUserEntity = nxRestrauntUserService.queryResUserByOpenId(openid);

        if (nxResUserEntity == null) {
            res.getNxRestrauntUserEntity().setNxRuWxOpenId(openid);
            Integer resUserId = nxRestrauntService.saveNewRestraunt(res);

            if (resUserId != null) {
                Map<String, Object> stringObjectMap = nxRestrauntService.queryResAndUserInfo(resUserId);
                return R.ok().put("data", stringObjectMap);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过订货员");
        }
    }


    /**
     *
     * 微信小程序扫描二维码校验文件
     * @return 校验内容
     */
    @RequestMapping(value = "/P6Khh4cKmX.txt")
    @ResponseBody
    public String resRegist( ) {
        return "9513a724a3a311899c561d1264e07457";
    }


    @RequestMapping(value = "/updateRestraunt", method = RequestMethod.POST)
    @ResponseBody
    public R updateRestraunt (@RequestBody NxRestrauntEntity restruant ) {

        nxRestrauntService.update(restruant);
        if(restruant.getNxRestrauntSubAmount() > 0){
            for (NxRestrauntEntity subs : restruant.getNxRestrauntEntities()) {
                nxRestrauntService.update(subs);
            }
        }
        return R.ok();
    }

    
    @RequestMapping(value = "/receiveOrder", method = RequestMethod.POST)
    @ResponseBody
    public R receiveOrder (@RequestBody NxRestrauntEntity restaunt  ) {
        restaunt.setNxRestrauntWorkingStatus(1);
        nxRestrauntService.update(restaunt);
        return R.ok();
    }
    
    
    


}
