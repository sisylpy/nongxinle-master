package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-03-22 18:07:28
 */

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPay;
import com.nongxinle.dao.NxCustomerUserGoodsDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.CommonUtils.generatePickNumber;
import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatDay;


@RestController
@RequestMapping("api/nxorders")
public class NxCommunityOrdersController {
    @Autowired
    private NxCommunityOrdersService nxCommunityOrdersService;
    @Autowired
    private NxCommunityAdsenseService nxCommunityAdsenseService;
    @Autowired
    private NxCommunityOrdersSubService nxCommunityOrdersSubService;

    @Autowired
    private NxCommunitySplicingOrdersService nxCommunitySplicingOrdersService;
    @Autowired
    private NxCustomerUserService nxCustomerUserService;
    @Autowired
    private NxCustomerUserCouponService nxCustomerUserCouponService;
    @Autowired
    private NxCustomerUserCardService nxCustomerUserCardService;
    @Autowired
    private NxCommunityCouponService nxCommunityCouponService;

    @Autowired
    private NxCustomerUserGoodsDao nxCustomerUserGoodsDao;
    @Autowired
    private NxCommunityGoodsService nxCommunityGoodsService;
    @Autowired
    private NxCommunityCardService nxCommunityCardService;
    @Autowired
    private NxECommerceCommunityService nxECommerceCommunityService;
    @Autowired
    private NxCommunityDeskService nxCommunityDeskService;
    @Autowired
    private NxCommunityService nxCommunityService;
    @Autowired
    private NxCommunityPurchaseGoodsService nxComPurchaseGoodsService;

    public static final String URL = "http://api.feieyun.cn/Api/Open/";//不需要修改

    public static final String USER = "454926763@qq.com";//*必填*：账号名
    public static final String UKEY = "bDI55xfWTv5Fu6KU";//*必填*: 飞鹅云后台注册账号后生成的UKEY 【备注：这不是填打印机的KEY】
    public static final String SN = "924610660";//*必填*：打印机编号，必须要在管理后台里添加打印机或调用API接口添加之后，才能调用API


    @RequestMapping(value = "/printDeskOrder/{id}")
    @ResponseBody
    public R printDeskOrder(@PathVariable Integer id) {

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        NxCommunityOrdersEntity ordersEntity = nxCommunityOrdersService.queryOrdersItemDetail(map);
        Integer nxCoCommunityId = ordersEntity.getNxCoCommunityId();
        NxCommunityEntity communityEntity = nxCommunityService.queryObject(nxCoCommunityId);
        String sn = communityEntity.getNxCommunityBillPrintSn();

        Integer nxCosDeskId = ordersEntity.getNxCoDeskId();
        NxCommunityDeskEntity deskEntity = new NxCommunityDeskEntity();
        if (nxCosDeskId != -1 && nxCosDeskId != 99) {
            deskEntity = nxCommunityDeskService.queryObject(nxCosDeskId);
        } else {
            deskEntity.setNxCommunityDeskName("堂食");
        }

        String content;
        content = "<CB>" + communityEntity.getNxCommunityName() + "</CB><BR>";
        Map<String, Object> mapSub = new HashMap<>();
        mapSub.put("orderId", id);
        System.out.println("osroooeeoesssnssn" + mapSub);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapSub);
        if (subEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                Integer nxCosCommunityGoodsId = subEntity.getNxCosCommunityGoodsId();
                NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(nxCosCommunityGoodsId);
                content += goodsEntity.getNxCgGoodsName() + subEntity.getNxCosQuantity() + "<BR>";
                content += "--------------------------------<BR>";
            }
        }
        content += "--------------------------------<BR>";
        content += "桌号：" + deskEntity.getNxCommunityDeskName() + "<BR>";
        content += "合计：" + ordersEntity.getNxCoTotal() + "元<BR>";
        content += "店铺地址：" + communityEntity.getNxCommunityDeliveryAddress() + "<BR>";
        content += "联系电话：" + communityEntity.getNxCommunityBusinessPhone() + "<BR>";
        content += "营业时间：" + communityEntity.getNxCommunityOpenTime() + "-" + communityEntity.getNxCommunityCloseTime() + "<BR>";

        //通过POST请求，发送打印信息到服务器
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(30000)//读取超时
                .setConnectTimeout(30000)//连接超时
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpPost post = new HttpPost(URL);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("user", USER));
        String STIME = String.valueOf(System.currentTimeMillis() / 1000);
        nvps.add(new BasicNameValuePair("stime", STIME));
        nvps.add(new BasicNameValuePair("sig", signature(USER, UKEY, STIME)));
        nvps.add(new BasicNameValuePair("apiname", "Open_printMsg"));//固定值,不需要修改
        nvps.add(new BasicNameValuePair("sn", sn));
        nvps.add(new BasicNameValuePair("content", content));
        nvps.add(new BasicNameValuePair("times", "1"));//打印联数

        CloseableHttpResponse response = null;
        String result = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
            response = httpClient.execute(post);
            int statecode = response.getStatusLine().getStatusCode();
            if (statecode == 200) {
                HttpEntity httpentity = response.getEntity();
                if (httpentity != null) {
                    //服务器返回的JSON字符串，建议要当做日志记录起来
                    result = EntityUtils.toString(httpentity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                post.abort();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("resulslsl" + result);

        return R.ok();
    }


    @RequestMapping(value = "/deskGetUnPayOrders", method = RequestMethod.POST)
    @ResponseBody
    public R deskGetUnPayOrders(Integer deskId, String startDate, String stopDate) {

        System.out.println("s");
        Map<String, Object> map = new HashMap<>();
        map.put("deskId", deskId);
        map.put("status", 0);
        System.out.println("mapapappapappap" + map);
        List<NxCommunityOrdersEntity> ordersEntities = nxCommunityOrdersService.queryCustomerOrder(map);

        double subtotal = nxCommunityOrdersService.queryCommOrderSubtotal(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", ordersEntities);
        mapR.put("subtotal", subtotal);

        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/deskGetOrders", method = RequestMethod.POST)
    @ResponseBody
    public R deskGetOrders(Integer deskId, String startDate, String stopDate) {

        System.out.println("s");
        Map<String, Object> map = new HashMap<>();
        map.put("deskId", deskId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        List<NxCommunityOrdersEntity> ordersEntities = nxCommunityOrdersService.queryCustomerOrder(map);

        return R.ok().put("data", ordersEntities);
    }


    @RequestMapping(value = "/orderDesk/4pqhDTY6LK.txt")
    @ResponseBody
    public String orderDesk() {
        return "ae662f0ace3d80f5eac9f1bcce8b71b4";
    }


    @RequestMapping(value = "/updateAllSplicing", method = RequestMethod.POST)
    @ResponseBody
    public R updateAllSplicing(@RequestBody NxCommunityOrdersEntity ordersEntity) {

        List<NxCommunitySplicingOrdersEntity> allSplicingOrders = ordersEntity.getAllSplicingOrders();

        for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : allSplicingOrders) {
            BigDecimal total = new BigDecimal(0);
            BigDecimal huaxianTotal = new BigDecimal(0);
            List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = splicingOrdersEntity.getNxCommunityOrdersSubEntities();

            if (nxCommunityOrdersSubEntities.size() > 0) {

                for (NxCommunityOrdersSubEntity subEntity : nxCommunityOrdersSubEntities) {
                    total = total.add(new BigDecimal(subEntity.getNxCosSubtotal()));
                    if (subEntity.getNxCosHuaxianSubtotal() != null) {
                        huaxianTotal = huaxianTotal.add(new BigDecimal(subEntity.getNxCosHuaxianSubtotal()));
                    }
                }
                splicingOrdersEntity.setNxCsoTotal(total.toString());
                splicingOrdersEntity.setNxCsoYouhuiTotal(huaxianTotal.toString());
                nxCommunitySplicingOrdersService.update(splicingOrdersEntity);
            }

        }

        return R.ok();
    }


    @RequestMapping(value = "/getDate")
    @ResponseBody
    public R getToday() {

        Map<String, Object> map = new HashMap<>();


        // day
        Map<String, Object> day = new HashMap<>();
        Map<String, Object> mapYesterday = new HashMap<>();
        mapYesterday.put("yesterdayDate", formatWhatDay(-1));
        mapYesterday.put("yesterdayStartDate", formatWhatDay(-1));
        mapYesterday.put("yesterdayStopDate", formatWhatDay(-1));
        mapYesterday.put("yesterdayString", formatWhatDayString(-1));
        mapYesterday.put("yesterdayWeek", getWeek(-1));
        day.put("yesterday", mapYesterday);

        Map<String, Object> mapToday = new HashMap<>();
        mapToday.put("todayDate", formatWhatDay(0));
        mapToday.put("todayStartDate", formatWhatDay(0));
        mapToday.put("todayStopDate", formatWhatDay(0));
        mapToday.put("todayString", formatWhatDayString(0));
        mapToday.put("todayWeek", getWeek(0));
        day.put("today", mapToday);


        // week
        Map<String, Object> week = new HashMap<>();
        Map<String, Object> lastSevenDay = new HashMap<>();
        lastSevenDay.put("lastSevenDayStartDate", formatWhatDay(-7));
        lastSevenDay.put("lastSevenDayStartDateString", formatWhatDayString(-7));
        lastSevenDay.put("lastSevenDayStopDate", formatWhatDay(-1));
        lastSevenDay.put("lastSevenDayStopDateString", formatWhatDayString(-1));
        week.put("lastSevenDay", lastSevenDay);

        Map<String, Object> thisWeek = new HashMap<>();
        thisWeek.put("thisWeekStartDate", thisWeekMonday());
        thisWeek.put("thisWeekStartString", thisWeekMondayString());
        thisWeek.put("thisWeekStopDate", thisWeekSunday());
        thisWeek.put("thisWeekStopString", thisWeekSundayString());
        week.put("thisWeek", thisWeek);

        Map<String, Object> lastWeek = new HashMap<>();
        lastWeek.put("lastWeekStartDate", getLastWeek());
        lastWeek.put("lastWeekStartString", thisWeekMondayString());
        lastWeek.put("lastWeekStopDate", thisWeekSunday());
        lastWeek.put("lastWeekStopString", thisWeekSundayString());
        week.put("lastWeek", lastWeek);

        // month
        Map<String, Object> month = new HashMap<>();
        Map<String, Object> lastThirtyDay = new HashMap<>();
        lastThirtyDay.put("lastThirtyDayStartDate", formatWhatDay(-30));
        lastThirtyDay.put("lastThirtyDayStartDateString", formatWhatDayString(-30));
        lastThirtyDay.put("lastThirtyDayStopDate", formatWhatDay(0));
        lastThirtyDay.put("lastThirtyDayStopDateString", formatWhatDayString(0));
        month.put("lastThirtyDay", lastThirtyDay);

        Map<String, Object> thisMonth = new HashMap<>();
        thisMonth.put("thisMonthStartDate", getThisMonthFirstDay());
        thisMonth.put("thisMonthStartDateString", formatWhatMonthString(0));
        thisMonth.put("thisMonthStopDate", getThisMonthLastDay());
        thisMonth.put("thisMonthStopDateString", formatWhatDayString(-1));
        month.put("thisMonth", thisMonth);
        Map<String, Object> lastMonth = new HashMap<>();
        lastMonth.put("lastMonthStartDate", getLastMonthFirstDay());
        lastMonth.put("lastMonthStartDateString", getLastMonthString());
        lastMonth.put("lastMonthStopDate", getLastMonthLastDay());
        lastMonth.put("lastMonthStopDateString", formatWhatDayString(-1));
        month.put("lastMonth", lastMonth);

        map.put("day", day);
        map.put("week", week);
        map.put("month", month);
        return R.ok().put("data", map);
    }


    @RequestMapping(value = "/getDayOrder", method = RequestMethod.POST)
    @ResponseBody
    public R getDayOrder(Integer commId, String date) {

        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("date", date);
        map.put("status", 5);
        System.out.println("dateeee" + map);
        List<NxCommunityOrdersEntity> ordersEntities = nxCommunityOrdersService.queryCustomerOrder(map);

        return R.ok().put("data", ordersEntities);
    }


    @RequestMapping(value = "/getSalesCommerceEveryDay", method = RequestMethod.POST)
    @ResponseBody
    private Map<String, Object> getSalesCommerceEveryDay(String startDate, String stopDate, Integer commerceId) {

        System.out.println("getfrisheeieieidydyydydydyydydyydydydyy");
        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> itemList = new ArrayList<>();
        List<String> dateList = new ArrayList<>();
        List<String> totalList = new ArrayList<>();
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
                Map<String, Object> map = new HashMap<>();
                map.put("date", whichDay);
                map.put("commerceId", commerceId);
                map.put("status", 5);
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                String dailyFresh = "0";
                System.out.println("mappapap" + map);
                Integer integer = nxCommunityOrdersService.queryCommOrderCount(map);
                if (integer > 0) {
                    double subtotal = nxCommunityOrdersService.queryCommOrderSubtotal(map);
                    System.out.println("suususuusuususuusuusu" + subtotal);
                    dailyFresh = new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString();

                }
                totalList.add(dailyFresh);
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("day", whichDay);
                mapItem.put("value", dailyFresh);
                System.out.println("oeoneoenoeeooeoe" + dailyFresh);
                itemList.add(mapItem);
                mapR.put("date", dateList);
                mapR.put("list", totalList);
                mapR.put("arr", itemList);

            }

        }
        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/getSalesEveryDay", method = RequestMethod.POST)
    @ResponseBody
    private Map<String, Object> getSalesEveryDay(String startDate, String stopDate, Integer commId) {

        System.out.println("getfrisheeieieidydyydydydyydydyydydydyy");
        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> itemList = new ArrayList<>();
        List<String> dateList = new ArrayList<>();
        List<String> totalList = new ArrayList<>();
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        if (howManyDaysInPeriod > 0) {

            for (int i = 0; i < howManyDaysInPeriod + 2; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                Map<String, Object> map = new HashMap<>();
                map.put("date", whichDay);
                map.put("commId", commId);
                map.put("status", 5);
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                String dailyFresh = "0";
                System.out.println("mappapap" + map);
                Integer integer = nxCommunityOrdersService.queryCommOrderCount(map);
                if (integer > 0) {
                    double subtotal = nxCommunityOrdersService.queryCommOrderSubtotal(map);
                    dailyFresh = new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString();

                }
                totalList.add(dailyFresh);
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("day", whichDay);
                mapItem.put("value", dailyFresh);
                itemList.add(mapItem);
                mapR.put("date", dateList);
                mapR.put("list", totalList);
                mapR.put("arr", itemList);

            }

        }
        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/changeServiceTime/{id}")
    @ResponseBody
    public R changeServiceTime(@PathVariable Integer id) {
        System.out.println("dchhchhc");
        NxCommunityOrdersEntity nxCommunityOrdersEntity = nxCommunityOrdersService.queryObject(id);
        nxCommunityOrdersEntity.setNxCoService("0");
        nxCommunityOrdersEntity.setNxCoServiceDate(formatWhatDay(0));
        nxCommunityOrdersEntity.setNxCoServiceHour(formatWhatHour(0));
        nxCommunityOrdersEntity.setNxCoServiceMinute(formatWhatMinute(0));
        BigDecimal multiply = new BigDecimal(nxCommunityOrdersEntity.getNxCoServiceHour()).multiply(new BigDecimal(60));
        BigDecimal add = multiply.add(new BigDecimal(nxCommunityOrdersEntity.getNxCoServiceMinute()));
        nxCommunityOrdersEntity.setNxCoServiceTime(add.toString());
        nxCommunityOrdersService.update(nxCommunityOrdersEntity);


        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
        if (subEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                subEntity.setNxCosService("0");
                subEntity.setNxCosServiceDate(formatWhatDate(0));
                subEntity.setNxCosServiceTime(add.toString());
                nxCommunityOrdersSubService.update(subEntity);
            }
        }
        return R.ok();
    }


    @RequestMapping(value = "/getStatusCommOrder", method = RequestMethod.POST)
    @ResponseBody
    public R getStatusCommOrder(Integer commId, Integer status) {

        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("status", status);
        System.out.println("ststmap" + map);
        List<NxCommunityOrdersEntity> nxCommunityOrdersEntities = nxCommunityOrdersService.queryOrdersDetail(map);

        return R.ok().put("data", nxCommunityOrdersEntities);
    }


    @RequestMapping(value = "/rePrintOrder", method = RequestMethod.POST)
    @ResponseBody
    public R rePrintOrder(@RequestBody NxCommunityOrdersEntity orders) {
        Integer nxCommunityOrdersId = orders.getNxCommunityOrdersId();

        if (orders.getNxCoType() == 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", nxCommunityOrdersId);
            List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);

            if (subEntities.size() > 0) {
                for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                    subEntity.setNxCosStatus(1);
                    nxCommunityOrdersSubService.update(subEntity);
                }
            }
            orders.setNxCoStatus(2);
            nxCommunityOrdersService.update(orders);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("id", nxCommunityOrdersId);
            List<NxCommunitySplicingOrdersEntity> nxCommunitySplicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(map);
            if (nxCommunitySplicingOrdersEntities.size() > 0) {
                for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : nxCommunitySplicingOrdersEntities) {
                    Integer nxCommunitySplicingOrdersId = splicingOrdersEntity.getNxCommunitySplicingOrdersId();
                    Map<String, Object> mapSp = new HashMap<>();
                    mapSp.put("splicingOrderId", nxCommunitySplicingOrdersId);
                    List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapSp);
                    if (subEntities.size() > 0) {
                        for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                            subEntity.setNxCosStatus(1);
                            nxCommunityOrdersSubService.update(subEntity);
                        }
                    }
                }
            }
            orders.setNxCoStatus(2);
            nxCommunityOrdersService.update(orders);
        }
        return R.ok();
    }

    /**
     * 顾客已取货
     *
     * @param orders
     * @return
     */
    @RequestMapping(value = "/customerFetchOrder", method = RequestMethod.POST)
    @ResponseBody
    public R customerFetchOrder(@RequestBody NxCommunityOrdersEntity orders) {
        Integer nxCommunityOrdersId = orders.getNxCommunityOrdersId();
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", nxCommunityOrdersId);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
        if (subEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                subEntity.setNxCosStatus(4);
                nxCommunityOrdersSubService.update(subEntity);
                if (subEntity.getNxCosCucId() != null) {
                    NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                    customerUserCouponEntity.setNxCucStatus(3);
                    nxCustomerUserCouponService.update(customerUserCouponEntity);

                    Integer nxCucCouponId = customerUserCouponEntity.getNxCucCouponId();
                    NxCommunityCouponEntity communityCouponEntity = nxCommunityCouponService.queryObject(nxCucCouponId);
                    communityCouponEntity.setNxCpUseCount(communityCouponEntity.getNxCpUseCount() + 1);
                    nxCommunityCouponService.update(communityCouponEntity);
                }
            }
        }
        orders.setNxCoStatus(5);
        nxCommunityOrdersService.update(orders);


        if (orders.getNxCoType() == 0) {

            Map<String, Object> mapC = new HashMap<>();
            mapC.put("userId", orders.getNxCoUserId());
            mapC.put("status", 0);
            System.out.println("mapciciccccccccc" + mapC);
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
            if (cardEntities.size() > 0) {
                for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                    userCardEntity.setNxCucaStatus(1);
                    userCardEntity.setNxCucaIsSelected(null);
                    nxCustomerUserCardService.update(userCardEntity);
                    Integer nxCucaCardId = userCardEntity.getNxCucaCardId();
                    NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(nxCucaCardId);
                    cardEntity.setNxCcUserCount(cardEntity.getNxCcUserCount() + 1);
                    nxCommunityCardService.update(cardEntity);

                    Map<String, Object> mapCD = new HashMap<>();
                    mapCD.put("userId", orders.getNxCoUserId());
                    mapCD.put("status", -1);
                    mapCD.put("cardId", userCardEntity.getNxCucaCardId());
                    List<NxCustomerUserCardEntity> cardEntitiesD = nxCustomerUserCardService.queryUserCardByParams(mapCD);
                    if (cardEntitiesD.size() > 0) {
                        for (NxCustomerUserCardEntity card : cardEntitiesD) {
                            nxCustomerUserCardService.delete(card.getNxCustomerUserCardId());
                        }
                    }

                }
            }
            //update userInfo
            NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(orders.getNxCoUserId());
            BigDecimal decimal = new BigDecimal(userEntity.getNxCuOrderAmount()).add(new BigDecimal(orders.getNxCoTotal())).setScale(1, BigDecimal.ROUND_HALF_UP);
            userEntity.setNxCuOrderAmount(decimal.toString());
            userEntity.setNxCuOrderTimes(userEntity.getNxCuOrderTimes() + 1);
            nxCustomerUserService.update(userEntity);


        } else {
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("id", orders.getNxCommunityOrdersId());
            List<NxCommunitySplicingOrdersEntity> nxCommunitySplicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(mapS);
            if (nxCommunitySplicingOrdersEntities.size() > 0) {
                for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : nxCommunitySplicingOrdersEntities) {
                    Map<String, Object> mapC = new HashMap<>();
                    mapC.put("userId", splicingOrdersEntity.getNxCsoUserId());
                    mapC.put("status", 0);
                    List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
                    if (cardEntities.size() > 0) {
                        for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                            userCardEntity.setNxCucaStatus(1);
                            userCardEntity.setNxCucaIsSelected(1);
                            userCardEntity.setNxCucaIsSelected(null);
                            nxCustomerUserCardService.update(userCardEntity);

                            Map<String, Object> mapCD = new HashMap<>();
                            mapCD.put("userId", orders.getNxCoUserId());
                            mapCD.put("status", -1);
                            mapCD.put("cardId", userCardEntity.getNxCucaCardId());
                            List<NxCustomerUserCardEntity> cardEntitiesD = nxCustomerUserCardService.queryUserCardByParams(mapCD);
                            if (cardEntitiesD.size() > 0) {
                                for (NxCustomerUserCardEntity card : cardEntitiesD) {
                                    nxCustomerUserCardService.delete(card.getNxCustomerUserCardId());
                                }
                            }
                        }
                    }

                    //update userInfo
                    NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(splicingOrdersEntity.getNxCsoUserId());
                    BigDecimal decimal = new BigDecimal(userEntity.getNxCuOrderAmount()).add(new BigDecimal(splicingOrdersEntity.getNxCsoTotal())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    userEntity.setNxCuOrderAmount(decimal.toString());
                    userEntity.setNxCuOrderTimes(userEntity.getNxCuOrderTimes() + 1);
                    nxCustomerUserService.update(userEntity);


                }
            }
        }


        return R.ok();
    }


    @RequestMapping(value = "/printWholeOrder", method = RequestMethod.POST)
    @ResponseBody
    public R printWholeOrder(@RequestBody NxCommunityOrdersEntity orders) {
        Integer nxCommunityOrdersId = orders.getNxCommunityOrdersId();

        String orderDetail = "";

        if (orders.getNxCoType() == 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", nxCommunityOrdersId);
            List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
            if (subEntities.size() > 0) {
                for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                    subEntity.setNxCosStatus(3);
                    nxCommunityOrdersSubService.update(subEntity);
                    NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(subEntity.getNxCosCommunityGoodsId());
                    orderDetail = orderDetail + goodsEntity.getNxCgGoodsName() + ",";
                }
            }

        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("id", nxCommunityOrdersId);
            System.out.println("iddiididid" + map);
            List<NxCommunitySplicingOrdersEntity> splicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(map);
            if (splicingOrdersEntities.size() > 0) {
                for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : splicingOrdersEntities) {
                    Map<String, Object> mapSub = new HashMap<>();
                    mapSub.put("orderId", splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                    List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapSub);
                    if (subEntities.size() > 0) {
                        for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                            subEntity.setNxCosStatus(3);
                            nxCommunityOrdersSubService.update(subEntity);
                            NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(subEntity.getNxCosCommunityGoodsId());
                            orderDetail = orderDetail + goodsEntity.getNxCgGoodsName() + ",";
                        }
                    }
                }
            }
        }

        orders.setNxCoStatus(4);
        nxCommunityOrdersService.update(orders);


        Integer nxCoUserId = orders.getNxCoUserId();
        NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(nxCoUserId);
        String nxCuWxOpenId = userEntity.getNxCuWxOpenId();

        Integer nxCoCommunityId = orders.getNxCoCommunityId();
        NxCommunityEntity nxCommunityEntity = nxCommunityService.queryObject(nxCoCommunityId);

        Map<String, TemplateData> mapNotice = new HashMap<>();
        mapNotice.put("thing2", new TemplateData(nxCommunityEntity.getNxCommunityName()));
        mapNotice.put("character_string4", new TemplateData(orders.getNxCoWeighNumber()));
        mapNotice.put("thing5", new TemplateData(orderDetail));
        mapNotice.put("time6", new TemplateData(orders.getNxCoServiceDate() + " " + orders.getNxCoServiceHour() + ":" +
                orders.getNxCoServiceMinute()));
        mapNotice.put("phone_number12", new TemplateData(nxCommunityEntity.getNxCommunityBusinessPhone()));
        System.out.println("nociiciiiicic" + mapNotice);
        WeNoticeService.qucantixingMessageMix(nxCuWxOpenId, "pages/index/index", mapNotice);


        return R.ok();
    }


    @RequestMapping(value = "/getCommOrder/{comId}")
    @ResponseBody
    public R getCommOrder(@PathVariable String comId) {
        Map<String, Object> map = new HashMap<>();
        map.put("commId", comId);
        map.put("xiaoyuStatus", 3);
        map.put("type", 0);
        System.out.println("cociicigetororororoor" + map);
        List<NxCommunityOrdersEntity> entities = nxCommunityOrdersService.queryOrdersDetail(map);

        return R.ok().put("data", entities);
    }


    @RequestMapping(value = "/delPindan/{id}")
    @ResponseBody
    public R delPindan(@PathVariable Integer id) {
        nxCommunityOrdersService.delete(id);
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        System.out.println("iddiididid" + map);
        List<NxCommunitySplicingOrdersEntity> splicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(map);
        if (splicingOrdersEntities.size() > 0) {
            for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : splicingOrdersEntities) {
                nxCommunitySplicingOrdersService.delete(splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                Map<String, Object> mapSub = new HashMap<>();
                mapSub.put("splicingOrderId", splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapSub);
                if (subEntities.size() > 0) {
                    for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                        nxCommunityOrdersSubService.delete(subEntity.getNxCommunityOrdersSubId());
                    }
                }
                Map<String, Object> mapC = new HashMap<>();
                mapC.put("status", -1);
                mapC.put("type", 1);
                mapC.put("userId", splicingOrdersEntity.getNxCsoUserId());
                List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
                if (cardEntities.size() > 0) {
                    for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                        nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
                    }
                }
            }
        }
        return R.ok();
    }


    @RequestMapping(value = "/customerIndexData", method = RequestMethod.POST)
    @ResponseBody
    public R customerIndexData(Integer commId, Integer orderUserId) {

        Map<String, Object> mapA = new HashMap<>();
        mapA.put("commId", commId);
        mapA.put("nowMinute", getNowMinute());
        System.out.println("ampapappa" + mapA);
        List<NxCommunityAdsenseEntity> adsenseEntities = nxCommunityAdsenseService.queryAdsenseByParams(mapA);
        List<NxCommunityOrdersEntity> nxCommunityOrdersEntities = new ArrayList<>();
        if (orderUserId != -1) {
            Map<String, Object> mapU = new HashMap<>();
            mapU.put("orderUserId", orderUserId);
            mapU.put("xiaoyuStatus", 5);
            mapU.put("commId", commId);
            nxCommunityOrdersEntities = nxCommunityOrdersService.queryOrderWithUserInfo(mapU);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("adsense", adsenseEntities);
        map.put("orders", nxCommunityOrdersEntities);


        //check1
        Map<String, Object> mapD = new HashMap<>();
        mapD.put("userId", orderUserId);
        mapD.put("type", 0);
        mapD.put("commId", commId);
        mapD.put("xiaoyuStatus", 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedNow = LocalDateTime.now().format(formatter);
        mapD.put("nowTime", formattedNow);
        System.out.println("delleleleNowtiem" + mapD);
        List<NxCustomerUserCouponEntity> delCouponEntities = nxCustomerUserCouponService.queryUserCouponListByParams(mapD);
        System.out.println("sssssssssss" + delCouponEntities.size());
        if (delCouponEntities.size() > 0) {
            for (NxCustomerUserCouponEntity userCouponEntity : delCouponEntities) {
                userCouponEntity.setNxCucStatus(-2);
                nxCustomerUserCouponService.update(userCouponEntity);
            }
        }

        //check2

        Map<String, Object> mapCARD = new HashMap<>();
        mapCARD.put("userId", orderUserId);
        mapCARD.put("status", 1);
        mapCARD.put("xiaoyuStopTime", formatWhatDay(0));
        System.out.println("getupodddi");

        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCARD);
        if (cardEntities.size() > 0) {
            for (NxCustomerUserCardEntity cardEntity : cardEntities) {
                System.out.println("deleleleleleleleellelelleelle" + cardEntity.getNxCucaStopDate());
                nxCustomerUserCardService.delete(cardEntity.getNxCustomerUserCardId());
            }
        }

        Map<String, Object> mapC = new HashMap<>();
        mapC.put("commId", commId);
        mapC.put("status", 0);
        System.out.println("mapcccccc" + mapC);
        List<NxCommunityCouponEntity> nxCommunityCouponEntityList = nxCommunityCouponService.queryCustomerShowCoupon(mapC);
        List<NxCommunityCouponEntity> downCoupons = new ArrayList<>();
        if (nxCommunityCouponEntityList.size() > 0) {
            for (NxCommunityCouponEntity communityCouponEntity : nxCommunityCouponEntityList) {
                Map<String, Object> mapIF = new HashMap<>();
                mapIF.put("coupId", communityCouponEntity.getNxCommunityCouponId());
                mapIF.put("userId", orderUserId);
                mapIF.put("xiaoyuStatus", 4);
                mapIF.put("fromShareUserId", 0);
                System.out.println("xiaoysuuss" + mapIF);
                List<NxCustomerUserCouponEntity> nxCustomerUserCouponEntities = nxCustomerUserCouponService.queryUserCouponListByParams(mapIF);
                if (nxCustomerUserCouponEntities.size() == 0) {
                    downCoupons.add(communityCouponEntity);
                }
            }
            if (downCoupons.size() > 0) {
                map.put("coupon", downCoupons.get(0));
                map.put("couponList", JSON.toJSONString(downCoupons));
            } else {
                map.put("coupon", null);
                map.put("couponList", null);
            }

        } else {
            map.put("coupon", null);
            map.put("couponList", null);
        }

//        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
//        String shipinUserName = myAPPIDConfig.getShipinUserName();
//        String shipinId = myAPPIDConfig.getShipinId();
//        NxCommunityVideoEntity videoEntity = new NxCommunityVideoEntity();
//        videoEntity.setNxCommunityVideoUserName(shipinUserName);
//        videoEntity.setNxCommunityVideoId(shipinId);
//        map.put("shipin", videoEntity);
        map.put("shipin", null);

        //1,
        Map<String, Object> mapUC = new HashMap<>();
        mapUC.put("userId", orderUserId);
        mapUC.put("type", 0);
        mapUC.put("commId", commId);
        mapUC.put("xiaoyuStatus", 1);
        mapUC.put("dayuStatus", -2);
        mapUC.put("shareUserId", 0);
        int count = nxCustomerUserCouponService.queryUserCouponCount(mapUC);
        map.put("couponCount", count);


        //3,
        Map<String, Object> mapUG = new HashMap<>();
        mapUG.put("userId", orderUserId);
        mapUG.put("commId", commId);
        mapUG.put("type", 1);
        System.out.println("maudud" + mapUG);
        int userGoodsCount = nxCustomerUserGoodsDao.queryUserGoodsCount(mapUG);

        map.put("userGoodsCount", userGoodsCount);


        //4
//        Map<String, Object> mapCARD2 = new HashMap<>();
//        mapCARD2.put("userId", orderUserId);
//        mapCARD2.put("status", 1);
//        mapCARD2.put("stopTime", formatWhatDay(0));
//        System.out.println("carkrkrkkr" + mapCARD2);
//        int cardCount = nxCustomerUserCardService.queryUserCardCount(mapCARD2);

        System.out.println("orderusue" + orderUserId);
        NxCustomerUserEntity userEntity = nxCustomerUserService.queryUserWithAddress(orderUserId);

        map.put("userInfo", userEntity);

        return R.ok().put("data", map);
    }


    /**
     * 删除订单
     *
     * @param nxOrdersId 订单id
     * @return o
     */
    @RequestMapping(value = "/deleteOrder/{nxOrdersId}")
    @ResponseBody
    public R deleteOrder(@PathVariable Integer nxOrdersId) {

        Map<String, Object> map = new HashMap<>();
        map.put("orderId", nxOrdersId);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
        if (subEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                if (subEntity.getNxCosCucId() != null) {
                    NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                    customerUserCouponEntity.setNxCucStatus(0);
                    nxCustomerUserCouponService.update(customerUserCouponEntity);
                }
                subEntity.setNxCosStatus(99);
//                nxCommunityOrdersSubService.update(subEntity);


            }
        }

        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(map);
        if (cardEntities.size() > 0) {
            for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
//                nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
            }
        }

        NxCommunityOrdersEntity nxCommunityOrdersEntity = nxCommunityOrdersService.queryObject(nxOrdersId);
        nxCommunityOrdersEntity.setNxCoStatus(99);
//        nxCommunityOrdersService.update(nxCommunityOrdersEntity);


        /*
         * 参数说明：
         * - transactionId：微信支付订单号，可选；如果有则优先使用
         * - outTradeNo：商户订单号（支付时的订单号），可选；如果transactionId为空，则必须提供outTradeNo
         * - refundAmount：退款金额（单位：元）
         * - totalAmount：原订单总金额（单位：元）
         *
         * 注意：退款请求中金额（total和refund）单位均为分，所以需要进行换算
         */

        // 生成退款单号（确保唯一，可以参考支付接口中生成商户订单号的方式）
        String outRefundNo = CommonUtils.generateOutTradeNo();

        // 创建退款接口所需的配置对象（与支付配置类似）
        MyWxQingqingxiangPayConfig config = new MyWxQingqingxiangPayConfig();

        // 构造退款请求 JSON
        JSONObject reqJson = new JSONObject();

        // 如果提供了微信支付订单号则优先使用
        String outTradeNo = nxCommunityOrdersEntity.getNxCoWxOutTradeNo();
        reqJson.put("out_trade_no", outTradeNo);
//        String transactionId = null;
//        if (transactionId != null && !transactionId.trim().isEmpty()) {
//            reqJson.put("transaction_id", transactionId);
//        } else if (outTradeNo != null && !outTradeNo.trim().isEmpty()) {
//            reqJson.put("out_trade_no", outTradeNo);
//        } else {
//            return R.error("交易ID和商户订单号不能同时为空");
//        }

        reqJson.put("out_refund_no", outRefundNo);
        // 退款结果异步通知的URL，需要保证该地址可公网访问
        reqJson.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxorders/refund"); // 请替换为你自己的退款通知地址

        // 构造金额对象（单位：分）
        String refundAmountString = nxCommunityOrdersEntity.getNxCoTotal();
        if (refundAmountString == null || refundAmountString.trim().isEmpty()) {
            return R.error("订单总金额为空，无法处理退款");
        }
        double doubleVal = Double.parseDouble(refundAmountString);          // => 18.0
        int totalFen = (int) Math.round(doubleVal * 100);                   // => 1800
        int funFen = totalFen / 2;
        JSONObject amount = new JSONObject();
        amount.put("total", totalFen);   // 原订单总金额: 1800 分
        amount.put("refund", funFen);  // 退款金额: 1800 分（全额退款）
        amount.put("currency", "CNY");


        reqJson.put("amount", amount);

        System.out.println("退款请求JSON: " + reqJson.toString());

        try {
            // 微信退款V3接口地址
            String url = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";

            // 发起退款请求，使用与支付相同的带证书Post请求工具方法
            String response = WxV3Util.sendPostWithCert(url, reqJson.toString());
            System.out.println("微信退款返回内容: " + response);

            // 将响应结果解析成JSON对象（可根据微信返回的实际字段进行处理）
            JSONObject respJson = JSON.parseObject(response);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("refund_id", respJson.getString("refund_id"));
            resultMap.put("out_refund_no", outRefundNo);

            // 此处可加入更新订单退款状态的代码


            return R.ok().put("map", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("退款失败");
        }


    }

    @RequestMapping("/refund")
    public Map<String, String> refund(HttpServletRequest request) {
        System.out.println("refundrefundrefund---------");
        try {
            // 1. 读取微信回调的 JSON 字符串
            String body = WxV3Util.readRequestBody(request);
            System.out.println("【微信V3退款回调】收到的原始JSON: " + body);

            // 2. 验签（强烈建议一定要做！），此处只是示例调用
            //    你需要提供正确的微信平台证书内容（platformCertificate）
            //    或者先在 WxV3Util 里真正实现 verifySignature 逻辑
            boolean verified = WxV3Util.verifySignature(body, request, "YOUR_PLATFORM_CERT_CONTENT");
            if (!verified) {
                System.out.println("【微信V3退款回调】签名验签失败！");
                return WxV3Util.errorReturn("Signature verification failed");
            }

            // 3. 解析 JSON
            JSONObject jsonObj = JSON.parseObject(body);

            // 3.1 可判断一下 event_type，通常是 REFUND.SUCCESS / REFUND.ABNORMAL / REFUND.CLOSED 等
            String eventType = jsonObj.getString("event_type");
            System.out.println("【微信V3退款回调】event_type = " + eventType);

            // 3.2 拿到 resource 对象
            JSONObject resource = jsonObj.getJSONObject("resource");
            if (resource == null) {
                System.out.println("【微信V3退款回调】resource 字段为空，无法解密退款信息");
                return WxV3Util.errorReturn("Resource is null");
            }
            String ciphertext = resource.getString("ciphertext");
            String nonce = resource.getString("nonce");
            String associatedData = resource.getString("associated_data");

            // 4. 使用 APIv3Key 解密 ciphertext 得到退款明文信息
            //    注意：一定要保证与商户平台上的 APIv3 Key 一致
//            String apiV3Key = "你的APIv3密钥";
            String apiV3Key = "sisy112578sisy112578sisy112578cf";
            String plainText = WxV3Util.aesGcmDecrypt(apiV3Key, associatedData, nonce, ciphertext);
            System.out.println("【微信V3退款回调】解密得到的退款信息明文: " + plainText);

            // 5. 解析退款明文 JSON，根据字段做业务处理
            JSONObject refundInfo = JSON.parseObject(plainText);

            // 微信退款通知里会包含 out_refund_no、refund_id、refund_status 等
            String outRefundNo = refundInfo.getString("out_refund_no");
            String refundId = refundInfo.getString("refund_id");
            String refundStatus = refundInfo.getString("refund_status"); // SUCCESS、CLOSED等

            System.out.println("【微信V3退款回调】out_refund_no = " + outRefundNo
                    + ", refund_id = " + refundId
                    + ", refund_status = " + refundStatus);

            // 6. 如果 refundStatus = "SUCCESS" 表示退款成功，可更新数据库订单状态
            if ("SUCCESS".equalsIgnoreCase(refundStatus)) {
                // =========================
                // 下面是你原先 V2 回调中的业务逻辑，根据 out_refund_no 查询订单并更新
                // 例如：
                /*
                // GbDepartmentBillEntity gbDepartmentBillEntity = gbDepartmentBillService.queryDepartBillByTradeNo(outRefundNo);
                // gbDepartmentBillEntity.setGbDbStatus(3);
                // gbDepartmentBillService.update(gbDepartmentBillEntity);

                // ... 更新对应 Orders 数据
                // ... 更新 NxDepartmentBillEntity 状态
                */
                // 这里省略，自己把旧代码迁移过来即可
            }

            // 7. 回应微信：收到通知，处理成功（注意 v3 必须返回 JSON）
            Map<String, String> successResp = new HashMap<>();
            successResp.put("code", "SUCCESS");
            successResp.put("message", "成功");
            return successResp;

        } catch (Exception e) {
            e.printStackTrace();
            // 如果解析或处理失败，返回错误信息
            return WxV3Util.errorReturn("Exception: " + e.getMessage());
        }
    }


    @RequestMapping(value = "/customerGetOrders", method = RequestMethod.POST)
    @ResponseBody
    public R customerGetOrders(Integer nxOrdersUserId, Integer page, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("orderUserId", nxOrdersUserId);
        List<NxCommunityOrdersEntity> ordersEntityList = nxCommunityOrdersService.queryCustomerOrder(map);
        int total = nxCommunityOrdersService.queryTotal(map);


        PageUtils pageUtil = new PageUtils(ordersEntityList, total, limit, page);
        System.out.println("paapa" + pageUtil);
        return R.ok().put("page", pageUtil);

    }


    /**
     * 以下是订单接口
     */


    @RequestMapping(value = "/getIsDeliveryOrders/{deliveryUserId}")
    @ResponseBody
    public R getIsDeliveryOrders(@PathVariable Integer deliveryUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("deliveryUserId", deliveryUserId);
        map.put("status", 4);
        List<NxCommunityOrdersEntity> ordersEntityList = nxCommunityOrdersService.queryDeliveryOrder(map);

        return R.ok().put("data", ordersEntityList);
    }


    /**
     * 称重中
     *
     * @param disId 批发商id
     * @return 称重中订单
     */
    @RequestMapping(value = "/getWeighingOrder/{disId}")
    @ResponseBody
    public R getWeighingOrder(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 1);
        List<NxCommunityOrdersEntity> entities = nxCommunityOrdersService.queryOrdersDetail(map);

        return R.ok().put("data", entities);
    }

    /**
     * 未称重
     *
     * @param disId 批发商id
     * @return 未称重订单
     */
    @RequestMapping(value = "/getUnWeightOrder", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderList(Integer disId, String serviceDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("serviceDate", serviceDate);
        map.put("status", 0);
        List<NxCommunityOrdersEntity> entities = nxCommunityOrdersService.queryOrdersDetail(map);
        return R.ok().put("data", entities);
    }


    /**
     * 获取订单详细
     *
     * @param orderId 订单ids
     * @return 订单
     */
    @RequestMapping(value = "/getOrderDetail/{orderId}")
    @ResponseBody
    public R getOrderDetail(@PathVariable Integer orderId) {

        Map<String, Object> map = new HashMap<>();
        map.put("id", orderId);
        System.out.println("dfodaof" + map);
        NxCommunityOrdersEntity ordersEntity = nxCommunityOrdersService.queryOrdersItemDetail(map);
        System.out.println("wwwhwhhwhwhwhhwhw" + ordersEntity);
        return R.ok().put("data", ordersEntity);
    }

    @RequestMapping(value = "/getOrderDetailPindan/{orderId}")
    @ResponseBody
    public R getOrderDetailPindan(@PathVariable Integer orderId) {

        Map<String, Object> map = new HashMap<>();
        map.put("id", orderId);
        map.put("orderType", 1);
        System.out.println("pinddnddid" + map);
        NxCommunityOrdersEntity ordersEntity = nxCommunityOrdersService.queryPindanDetail(map);
        return R.ok().put("data", ordersEntity);
    }


    @RequestMapping(value = "/deliverySavePindan", method = RequestMethod.POST)
    @ResponseBody
    public R deliverySavePindan(Integer commId, Integer deliveryUserId, Integer serviceType) {

        NxECommerceCommunityEntity eCommerceCommunityEntity = nxECommerceCommunityService.queryByCommunityId(commId);
        Integer nxEccEId = eCommerceCommunityEntity.getNxEccEId();
        NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(deliveryUserId);
        NxCommunityOrdersEntity ordersEntity = new NxCommunityOrdersEntity();
        ordersEntity.setNxCoUserId(deliveryUserId);
        ordersEntity.setNxCoDeliveryUserId(deliveryUserId);
        ordersEntity.setNxCoCommunityId(commId);
        ordersEntity.setNxCoCommerceId(nxEccEId);
        ordersEntity.setNxCoCustomerId(userEntity.getNxCuCustomerId());
        ordersEntity.setNxCoType(1);
        ordersEntity.setNxCoServiceType(serviceType);
        ordersEntity.setNxCoStatus(-1);
        nxCommunityOrdersService.justSave(ordersEntity);


        NxCommunitySplicingOrdersEntity splicingOrdersEntity = new NxCommunitySplicingOrdersEntity();
        splicingOrdersEntity.setNxCsoUserId(deliveryUserId);
        splicingOrdersEntity.setNxCsoCoOrderId(ordersEntity.getNxCommunityOrdersId());
        splicingOrdersEntity.setNxCsoStatus(0);
        splicingOrdersEntity.setNxCsoCommunityId(commId);
        splicingOrdersEntity.setNxCsoCustomerId(userEntity.getNxCuCustomerId());
        splicingOrdersEntity.setNxCsoTotal("0");
        splicingOrdersEntity.setNxCsoYouhuiTotal("0");
        splicingOrdersEntity.setNxCsoDate(formatWhatDate(0));
        splicingOrdersEntity.setNxCsoServiceType(serviceType);
        splicingOrdersEntity.setNxCsoCommerceId(nxEccEId);
        nxCommunitySplicingOrdersService.save(splicingOrdersEntity);
        splicingOrdersEntity.setOrderUser(userEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("id", ordersEntity.getNxCommunityOrdersId());
        System.out.println("dfadfasfsa" + map);
        ordersEntity = nxCommunityOrdersService.queryPindanDetail(map);
        ordersEntity.setOrderUserSplicingOrder(splicingOrdersEntity);

        return R.ok().put("data", ordersEntity);
    }


    @RequestMapping(value = "/getMyPindanDetail", method = RequestMethod.POST)
    @ResponseBody
    public R getMyPindanDetail(Integer pindanId, Integer userId, Integer serviceType) {

        NxCommunitySplicingOrdersEntity splicingOrdersEntity = new NxCommunitySplicingOrdersEntity();
        Map<String, Object> map = new HashMap<>();
        map.put("id", pindanId);
        map.put("orderType", 1);
        map.put("serviceType", serviceType);
        System.out.println("pindfiaifajfamamma" + map);
        NxCommunityOrdersEntity nxCommunityOrdersEntity = nxCommunityOrdersService.queryPindanDetail(map);
        if (nxCommunityOrdersEntity != null) {
            Map<String, Object> mapO = new HashMap<>();
            mapO.put("id", pindanId);
            mapO.put("orderUserId", userId);
            mapO.put("orderType", 1);
            System.out.println("spsoosmappasss" + mapO);
            NxCommunitySplicingOrdersEntity deliverySplicingOrder = nxCommunitySplicingOrdersService.queryNewPindan(mapO);
            if (deliverySplicingOrder == null) {
                NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(userId);
                splicingOrdersEntity.setNxCsoCustomerId(userEntity.getNxCuCustomerId());
                splicingOrdersEntity.setNxCsoUserId(userId);
                splicingOrdersEntity.setNxCsoCoOrderId(pindanId);
                splicingOrdersEntity.setNxCsoStatus(0);
                splicingOrdersEntity.setNxCsoCustomerId(userEntity.getNxCuCustomerId());
                splicingOrdersEntity.setNxCsoYouhuiTotal("0");
                splicingOrdersEntity.setNxCsoTotal("0");
                splicingOrdersEntity.setNxCsoCommunityId(nxCommunityOrdersEntity.getNxCoCommunityId());
                splicingOrdersEntity.setNxCsoDate(formatWhatDate(0));
                nxCommunitySplicingOrdersService.save(splicingOrdersEntity);

                splicingOrdersEntity.setOrderUser(userEntity);
                nxCommunityOrdersEntity = nxCommunityOrdersService.queryPindanDetail(map);
                nxCommunityOrdersEntity.setOrderUserSplicingOrder(splicingOrdersEntity);

            } else {
                NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(userId);
                deliverySplicingOrder.setOrderUser(userEntity);
                nxCommunityOrdersEntity.setOrderUserSplicingOrder(deliverySplicingOrder);
            }

            return R.ok().put("data", nxCommunityOrdersEntity);
        } else {
            return R.error(-1, "拼单取消");
        }


    }


    @ResponseBody
    @RequestMapping(value = "/pindanSaveOrder", method = RequestMethod.POST)
    public R pindanSaveOrder(@RequestBody NxCommunityOrdersEntity nxOrders) {

        System.out.println(nxOrders);
        nxOrders.setNxCoDate(formatWhatYearDayTime(0));
        nxOrders.setNxCoStatus(0);
        String pickUpCode = generatePickNumber(3);
        nxOrders.setNxCoWeighNumber(pickUpCode);
        BigDecimal multiply = new BigDecimal(nxOrders.getNxCoServiceHour()).multiply(new BigDecimal(60));
        BigDecimal add = multiply.add(new BigDecimal(nxOrders.getNxCoServiceMinute()));
        nxOrders.setNxCoServiceTime(add.toString());
        nxOrders.setNxCoDate(formatWhatYearDayTime(0));
        nxCommunityOrdersService.update(nxOrders);


        Map<String, Object> map = new HashMap<>();
        map.put("id", nxOrders.getNxCommunityOrdersId());

        List<NxCommunitySplicingOrdersEntity> nxCommunitySplicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(map);
        System.out.println("mapappaidid" + map);
        if (nxCommunitySplicingOrdersEntities.size() > 0) {
            for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : nxCommunitySplicingOrdersEntities) {

                splicingOrdersEntity.setNxCsoStatus(3);
                nxCommunitySplicingOrdersService.update(splicingOrdersEntity);

                //gegnixnusergoods
                Map<String, Object> mapSp = new HashMap<>();
                mapSp.put("splicingOrderId", splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                List<NxCommunityOrdersSubEntity> nxOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapSp);
                if (nxOrdersSubEntities.size() > 0) {
                    for (NxCommunityOrdersSubEntity subEntity : nxOrdersSubEntities) {
                        subEntity.setNxCosStatus(0);
                        nxCommunityOrdersSubService.update(subEntity);

                    }
                }

            }
        }


        return R.ok().put("data", nxOrders.getNxCommunityOrdersId());

    }


    private void saveUserGoods(NxCommunityOrdersSubEntity subEntity) {

        Integer nxOsCommunityGoodsId = subEntity.getNxCosCommunityGoodsId();
        Map<String, Object> mapUG = new HashMap<>();
        mapUG.put("nxOsCommunityGoodsId", nxOsCommunityGoodsId);
        mapUG.put("nxCugUserId", subEntity.getNxCosOrderUserId());
        NxCustomerUserGoodsEntity userGoodsEntity = nxCustomerUserGoodsDao.queryByCommunityGoodsId(mapUG);

        if (userGoodsEntity != null) {
            userGoodsEntity.setNxCugLastOrderTime(formatWhatDayTime(0));
            userGoodsEntity.setNxCugLastOrderQuantity(subEntity.getNxCosQuantity());
            userGoodsEntity.setNxCugLastOrderStandard(subEntity.getNxCosStandard());
            userGoodsEntity.setNxCugLastOrderTime(formatWhatDay(0));
            userGoodsEntity.setNxCugJoinMyTemplate(0);
            Integer nxCugOrderTimes = userGoodsEntity.getNxCugOrderTimes();
            userGoodsEntity.setNxCugOrderTimes(nxCugOrderTimes + 1);
            String nxCugOrderAmount = userGoodsEntity.getNxCugOrderAmount();
            String nxOsQuantity = subEntity.getNxCosQuantity();
            BigDecimal addS = new BigDecimal(nxCugOrderAmount).add(new BigDecimal(nxOsQuantity));
            userGoodsEntity.setNxCugOrderAmount(addS.toString());
            nxCustomerUserGoodsDao.update(userGoodsEntity);
        } else {
            NxCustomerUserGoodsEntity newUserGoodsEntity = new NxCustomerUserGoodsEntity();
            newUserGoodsEntity.setNxCugFirstOrderTime(formatWhatDay(0));
            newUserGoodsEntity.setNxCugOrderAmount(subEntity.getNxCosQuantity());
            newUserGoodsEntity.setNxCugCommunityGoodsId(subEntity.getNxCosCommunityGoodsId());
            newUserGoodsEntity.setNxCugOrderTimes(1);
            newUserGoodsEntity.setNxCugUserId(subEntity.getNxCosOrderUserId());
            newUserGoodsEntity.setNxCugLastOrderTime(formatWhatDay(0));
            newUserGoodsEntity.setNxCugJoinMyTemplate(0);
            newUserGoodsEntity.setNxCugLastOrderQuantity(subEntity.getNxCosQuantity());
            newUserGoodsEntity.setNxCugLastOrderStandard(subEntity.getNxCosStandard());
            newUserGoodsEntity.setNxCugType(0);
            nxCustomerUserGoodsDao.save(newUserGoodsEntity);
        }
    }


    @ResponseBody
    @RequestMapping(value = "/customerSaveOrder", method = RequestMethod.POST)
    public R customerSaveOrder(@RequestBody NxCommunityOrdersEntity nxOrders) {

        System.out.println(nxOrders);
        nxOrders.setNxCoDate(formatWhatYearDayTime(0));
        nxOrders.setNxCoStatus(0);
        nxOrders.setNxCoType(0);
        String pickUpCode = generatePickNumber(3);
        nxOrders.setNxCoWeighNumber(pickUpCode);
        BigDecimal multiply = new BigDecimal(nxOrders.getNxCoServiceHour()).multiply(new BigDecimal(60));
        BigDecimal add = multiply.add(new BigDecimal(nxOrders.getNxCoServiceMinute()));
        nxOrders.setNxCoServiceTime(add.toString());
        nxOrders.setNxCoDate(formatWhatYearDayTime(0));
//        nxCommunityOrdersService.justSaveWithUserGoods(nxOrders);
        nxCommunityOrdersService.justSave(nxOrders);

        List<NxCommunityOrdersSubEntity> nxOrdersSubEntities = nxOrders.getNxOrdersSubEntities();
        if (nxOrdersSubEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : nxOrdersSubEntities) {
                subEntity.setNxCosOrdersId(nxOrders.getNxCommunityOrdersId());
                subEntity.setNxCosStatus(0);
                // Normalize empty strings to null for integer fields
                if (subEntity.getNxCosServiceTime() != null && subEntity.getNxCosServiceTime().trim().isEmpty()) {
                    subEntity.setNxCosServiceTime(null);
                }
                nxCommunityOrdersSubService.update(subEntity);
            }
        }

        return R.ok().put("data", nxOrders.getNxCommunityOrdersId());

    }

    @ResponseBody
    @RequestMapping(value = "/staffSaveOrder", method = RequestMethod.POST)
    public R staffSaveOrder(@RequestBody NxCommunityOrdersEntity nxOrders) {

        System.out.println(nxOrders);
        nxOrders.setNxCoDate(formatWhatYearDayTime(0));
        nxOrders.setNxCoStatus(0);
        nxOrders.setNxCoType(0);
        String pickUpCode = generatePickNumber(3);
        nxOrders.setNxCoWeighNumber(pickUpCode);
        BigDecimal multiply = new BigDecimal(nxOrders.getNxCoServiceHour()).multiply(new BigDecimal(60));
        BigDecimal add = multiply.add(new BigDecimal(nxOrders.getNxCoServiceMinute()));
        nxOrders.setNxCoServiceTime(add.toString());
        nxOrders.setNxCoDate(formatWhatYearDayTime(0));
//        nxCommunityOrdersService.justSaveWithUserGoods(nxOrders);

        System.out.println("jjsususuave" + nxOrders);
        nxCommunityOrdersService.justSave(nxOrders);

        Integer nxCoDeskId = nxOrders.getNxCoDeskId();

        if (nxCoDeskId != -1 && nxCoDeskId != 99) {
            NxCommunityDeskEntity deskEntity = nxCommunityDeskService.queryObject(nxCoDeskId);
            deskEntity.setNxCdStatus(1);
            nxCommunityDeskService.update(deskEntity);
        }


        List<NxCommunityOrdersSubEntity> nxOrdersSubEntities = nxOrders.getNxOrdersSubEntities();
        if (nxOrdersSubEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : nxOrdersSubEntities) {
                subEntity.setNxCosOrdersId(nxOrders.getNxCommunityOrdersId());
                subEntity.setNxCosStatus(0);
                // Normalize empty strings to null for integer fields
                if (subEntity.getNxCosServiceTime() != null && subEntity.getNxCosServiceTime().trim().isEmpty()) {
                    subEntity.setNxCosServiceTime(null);
                }
                nxCommunityOrdersSubService.update(subEntity);

                printSubOrderGoodsDesk(subEntity);
            }
        }

        return R.ok().put("data", nxOrders.getNxCommunityOrdersId());

    }

    public static String getAccessToken() {
        long currentTime = System.currentTimeMillis();
// 缓存 access_token 和过期时间（简单缓存示例）
        String accessToken = null;
        long expireTime = 0;
        // 如果 token 还没过期就直接返回
        if (accessToken != null && currentTime < expireTime) {
            return accessToken;
        }

        // 否则调用微信接口获取新的 access_token
        MyAPPIDConfig config = new MyAPPIDConfig();
        String appId = config.getQingqingxiangAppId();
        String secret = config.getQingqingxiangScreat();

        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
                + appId + "&secret=" + secret;

        try {
            String response = WeChatUtil.httpRequest(url, "GET", null);
            JSONObject jsonObject = JSONObject.parseObject(response);

            if (jsonObject.containsKey("access_token")) {
                accessToken = jsonObject.getString("access_token");
                int expiresIn = jsonObject.getIntValue("expires_in"); // 一般是 7200 秒
                expireTime = currentTime + (expiresIn - 200) * 1000L; // 提前 200 秒过期
                return accessToken;
            } else {
                System.out.println("获取 access_token 失败：" + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 餐桌发起结账
     *
     * @param orderId
     * @param code
     * @return
     */
    @RequestMapping(value = "/customerCashPayMixDesk", method = RequestMethod.POST)
    public R customerCashPayMixDesk(Integer orderId, String code) {

        System.out.println("customerUserLogincodee" + code);
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String liancaiKufangAppId = myAPPIDConfig.getQingqingxiangAppId();
        String liancaiKufangScreat = myAPPIDConfig.getQingqingxiangScreat();

        String urlCode = "https://api.weixin.qq.com/sns/jscode2session?appid=" + liancaiKufangAppId + "&secret=" +
                liancaiKufangScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(urlCode, "GET", null);
        System.out.println("strrr" + str);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println("jsonObjectjsonObject" + jsonObject);
        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openId = jsonObject.get("openid").toString();

        System.out.println("orderreer" + orderId);
        NxCommunityOrdersEntity nxOrders = nxCommunityOrdersService.queryObject(orderId);

        MyWxQingqingxiangPayConfig config = new MyWxQingqingxiangPayConfig();
        String tradeNo = CommonUtils.generateOutTradeNo();

//        // 实际支付金额（单位：分）
        Double amountYuan = Double.parseDouble(nxOrders.getNxCoTotal());
        int totalFee = (int) (amountYuan * 100);
        System.out.println("tororor" + totalFee);

        try {
            // 构造请求 JSON
            JSONObject reqJson = new JSONObject();
            reqJson.put("appid", config.getAppID());
            reqJson.put("mchid", config.getMchID());
            reqJson.put("description", "订单支付");
            reqJson.put("out_trade_no", tradeNo);
            reqJson.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxorders/notifyDesk");
            JSONObject amount = new JSONObject();
            amount.put("total", totalFee);
            amount.put("currency", "CNY");
            reqJson.put("amount", amount);

            JSONObject payer = new JSONObject();
            payer.put("openid", openId);
            reqJson.put("payer", payer);

            System.out.println("resJson" + reqJson);
            // 发起请求
            String url = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
            String response = WxV3Util.sendPostWithCert(url, reqJson.toString());
            System.out.println("微信返回内容：" + response);
            JSONObject respJson = JSON.parseObject(response);


            String prepayId = respJson.getString("prepay_id");

            // 二次签名参数
            long timestamp = System.currentTimeMillis() / 1000;
            String nonceStr = CommonUtils.generateUUID();
            String packageStr = "prepay_id=" + prepayId;

            // 签名用字符串拼接
            String message = config.getAppID() + "\n"
                    + timestamp + "\n"
                    + nonceStr + "\n"
                    + packageStr + "\n";

            String paySign = WxV3Util.signMessage(message, config.getPrivateKey());

            // 封装返回参数
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("appId", config.getAppID());
            resultMap.put("timeStamp", String.valueOf(timestamp));
            resultMap.put("nonceStr", nonceStr);
            resultMap.put("package", packageStr);
            resultMap.put("signType", "RSA");
            resultMap.put("paySign", paySign);
            resultMap.put("orderId", nxOrders.getNxCommunityOrdersId());

            // 更新订单状态
            nxOrders.setNxCoStatus(1);
            nxOrders.setNxCoPaymentStatus(0);
            nxOrders.setNxCoWxOutTradeNo(tradeNo);
            nxCommunityOrdersService.update(nxOrders);

            return R.ok().put("map", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            return R.error("下单失败");
        }
    }

    @RequestMapping(value = "/refundOrder", method = RequestMethod.POST)
    public R refundOrder(String orderId, Double refundAmount) {
        /*
         * 参数说明：
         * - transactionId：微信支付订单号，可选；如果有则优先使用
         * - outTradeNo：商户订单号（支付时的订单号），可选；如果transactionId为空，则必须提供outTradeNo
         * - refundAmount：退款金额（单位：元）
         * - totalAmount：原订单总金额（单位：元）
         *
         * 注意：退款请求中金额（total和refund）单位均为分，所以需要进行换算
         */

        // 生成退款单号（确保唯一，可以参考支付接口中生成商户订单号的方式）
        String outRefundNo = CommonUtils.generateOutTradeNo();

        // 创建退款接口所需的配置对象（与支付配置类似）
        MyWxQingqingxiangPayConfig config = new MyWxQingqingxiangPayConfig();

        // 构造退款请求 JSON
        JSONObject reqJson = new JSONObject();

        // 如果提供了微信支付订单号则优先使用
//        if (transactionId != null && !transactionId.trim().isEmpty()) {
//            reqJson.put("transaction_id", transactionId);
//        } else if (outTradeNo != null && !outTradeNo.trim().isEmpty()) {
//            reqJson.put("out_trade_no", outTradeNo);
//        } else {
//            return R.error("交易ID和商户订单号不能同时为空");
//        }

        reqJson.put("out_refund_no", outRefundNo);
        // 退款结果异步通知的URL，需要保证该地址可公网访问
        reqJson.put("notify_url", "https://your.notify.url/refund"); // 请替换为你自己的退款通知地址

        // 构造金额对象（单位：分）
        JSONObject amount = new JSONObject();
        int total = (int) (refundAmount * 100);
        int refund = (int) (refundAmount * 100);
        amount.put("total", total);
        amount.put("refund", refund);
        amount.put("currency", "CNY");

        reqJson.put("amount", amount);

        System.out.println("退款请求JSON: " + reqJson.toString());

        try {
            // 微信退款V3接口地址
            String url = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";

            // 发起退款请求，使用与支付相同的带证书Post请求工具方法
            String response = WxV3Util.sendPostWithCert(url, reqJson.toString());
            System.out.println("微信退款返回内容: " + response);

            // 将响应结果解析成JSON对象（可根据微信返回的实际字段进行处理）
            JSONObject respJson = JSON.parseObject(response);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("refund_id", respJson.getString("refund_id"));
            resultMap.put("out_refund_no", outRefundNo);

            // 此处可加入更新订单退款状态的代码


            return R.ok().put("map", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("退款失败");
        }
    }


    /**
     * 团购顾客发起支付
     *
     * @param orderId
     * @param openId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/customerCashPayMix", method = RequestMethod.POST)
    public R customerCashPayMix(Integer orderId, String openId) {

        NxCommunityOrdersEntity nxOrders = nxCommunityOrdersService.queryObject(orderId);
        MyWxQingqingxiangPayConfig config = new MyWxQingqingxiangPayConfig();
        String nxRbTotal = nxOrders.getNxCoTotal();
        Double aDouble = Double.parseDouble(nxRbTotal) * 100;
        int i = aDouble.intValue();
        System.out.println("dfdafkdaksfas" + nxOrders.getNxCoTotal());
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
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxorders/notify");
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

            nxOrders.setNxCoStatus(1);
            nxOrders.setNxCoPaymentStatus(0);
            nxOrders.setNxCoWxOutTradeNo(tradeNo);
            nxCommunityOrdersService.update(nxOrders);

            if (nxOrders.getNxCoType() == 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderId", nxOrders.getNxCommunityOrdersId());
                List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
                if (nxCommunityOrdersSubEntities.size() > 0) {
                    for (NxCommunityOrdersSubEntity subEntity : nxCommunityOrdersSubEntities) {
                        //检查 Adsense
                        checkAdsenseGoods(subEntity.getNxCosCommunityGoodsId(), subEntity);

                        subEntity.setNxCosStatus(1);
                        subEntity.setNxCosServiceTime(nxOrders.getNxCoServiceTime());
                        subEntity.setNxCosPickUpCode(nxOrders.getNxCoWeighNumber());
                        subEntity.setNxCosService(nxOrders.getNxCoService());
                        subEntity.setNxCosServiceDate(nxOrders.getNxCoServiceDate());
                        subEntity.setNxCosServiceTime(nxOrders.getNxCoServiceTime());

                        //保存为 comPurGoods
                        Integer comGoodsId = subEntity.getNxCosCommunityGoodsId();

                        //查询是否有采购的同一个商品
                        Map<String, Object> mapPur = new HashMap<>();
                        mapPur.put("comGoodsId", comGoodsId);
                        mapPur.put("equalStatus", 0);
                        NxCommunityPurchaseGoodsEntity purchaseGoodsEntity = nxComPurchaseGoodsService.queryPurchaseGoodsByStatus(mapPur);
                        if (purchaseGoodsEntity == null) {
                            //是个新采购商品
                            NxCommunityPurchaseGoodsEntity comPurchaseGoodsEntity = new NxCommunityPurchaseGoodsEntity();
                            comPurchaseGoodsEntity.setNxCpgComGoodsFatherId(subEntity.getNxCosCommunityGoodsFatherId());
                            comPurchaseGoodsEntity.setNxCpgComGoodsId(subEntity.getNxCosCommunityGoodsId());
                            comPurchaseGoodsEntity.setNxCpgCommunityId(subEntity.getNxCosCommunityId());
                            comPurchaseGoodsEntity.setNxCpgApplyDate(formatWhatDay(0));
                            comPurchaseGoodsEntity.setNxCpgStatus(0);
                            comPurchaseGoodsEntity.setNxCpgOrdersAmount(1);
                            comPurchaseGoodsEntity.setNxCpgStandard(subEntity.getNxCosStandard());
                            comPurchaseGoodsEntity.setNxCpgQuantity(subEntity.getNxCosQuantity());
                            comPurchaseGoodsEntity.setNxCpgPurchaseType(1);
                            nxComPurchaseGoodsService.save(comPurchaseGoodsEntity);
                            Integer gbDistributerPurchaseGoodsId = comPurchaseGoodsEntity.getNxCommunityPurchaseGoodsId();
                            subEntity.setNxCosCommPurGoodsId(gbDistributerPurchaseGoodsId);
                            subEntity.setNxCosBuyStatus(0);

                        } else {
                            //给老采购商品添加新订单
                            Integer gbDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxCommunityPurchaseGoodsId();
                            subEntity.setNxCosCommPurGoodsId(gbDistributerPurchaseGoodsId);
                            subEntity.setNxCosBuyStatus(0);
                            //采购商品订单数量更新
                            Integer gbDpgOrdersAmount = purchaseGoodsEntity.getNxCpgOrdersAmount();
                            purchaseGoodsEntity.setNxCpgOrdersAmount(gbDpgOrdersAmount + 1);
                            BigDecimal purQuantity = new BigDecimal(purchaseGoodsEntity.getNxCpgQuantity());
                            BigDecimal orderQuantity = new BigDecimal(subEntity.getNxCosQuantity());
                            BigDecimal add = purQuantity.add(orderQuantity).setScale(2, BigDecimal.ROUND_HALF_UP);
                            purchaseGoodsEntity.setNxCpgQuantity(add.toString());
                            nxComPurchaseGoodsService.update(purchaseGoodsEntity);
                        }

                        nxCommunityOrdersSubService.update(subEntity);

                        saveUserGoods(subEntity);

                        if (subEntity.getNxCosCucId() != null) {
                            NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                            customerUserCouponEntity.setNxCucStatus(2);
                            nxCustomerUserCouponService.update(customerUserCouponEntity);
                        }
                    }
                }
                Map<String, Object> mapC = new HashMap<>();
                mapC.put("userId", nxOrders.getNxCoUserId());
                mapC.put("status", -1);
                List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
                if (cardEntities.size() > 0) {
                    for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                        if (userCardEntity.getNxCucaIsSelected() == 1) {
                            userCardEntity.setNxCucaStatus(0);
                            userCardEntity.setNxCucaComOrderId(nxOrders.getNxCommunityOrdersId());
                            nxCustomerUserCardService.update(userCardEntity);
                            //给订单添加购买卡的费用
                            Integer nxCucaCardId = userCardEntity.getNxCucaCardId();
                            NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(nxCucaCardId);
                            String nxCcPrice = cardEntity.getNxCcPrice();
                            nxOrders.setNxCoBuyMemberCardSubtotal(nxCcPrice);
                            nxCommunityOrdersService.update(nxOrders);
                        } else {
                            nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
                        }
                    }
                }

            } else {
                Map<String, Object> mapS = new HashMap<>();
                mapS.put("id", nxOrders.getNxCommunityOrdersId());
                System.out.println("bilosororororororoorro" + mapS);
                List<NxCommunitySplicingOrdersEntity> nxCommunitySplicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(mapS);
                if (nxCommunitySplicingOrdersEntities.size() > 0) {
                    for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : nxCommunitySplicingOrdersEntities) {

                        Map<String, Object> map = new HashMap<>();
                        map.put("splicingOrderId", splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                        System.out.println("pspsosoosossos" + splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
                        if (nxCommunityOrdersSubEntities.size() > 0) {
                            for (NxCommunityOrdersSubEntity subEntity : nxCommunityOrdersSubEntities) {

                                //检查 Adsense
                                checkAdsenseGoods(subEntity.getNxCosCommunityGoodsId(), subEntity);

                                subEntity.setNxCosStatus(1);
                                subEntity.setNxCosServiceTime(nxOrders.getNxCoServiceTime());
                                subEntity.setNxCosPickUpCode(nxOrders.getNxCoWeighNumber());
                                subEntity.setNxCosService(nxOrders.getNxCoService());
                                subEntity.setNxCosServiceDate(nxOrders.getNxCoServiceDate());
                                subEntity.setNxCosServiceTime(nxOrders.getNxCoServiceTime());
                                nxCommunityOrdersSubService.update(subEntity);
                                saveUserGoods(subEntity);

                                if (subEntity.getNxCosCucId() != null) {
                                    NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                                    customerUserCouponEntity.setNxCucStatus(2);
                                    nxCustomerUserCouponService.update(customerUserCouponEntity);
                                }


                            }
                        }

                        Map<String, Object> mapC = new HashMap<>();
                        mapC.put("userId", splicingOrdersEntity.getNxCsoUserId());
                        mapC.put("status", -1);
                        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
                        if (cardEntities.size() > 0) {
                            for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                                if (userCardEntity.getNxCucaIsSelected() == 1) {
                                    userCardEntity.setNxCucaStatus(0);
                                    userCardEntity.setNxCucaComOrderId(nxOrders.getNxCommunityOrdersId());
                                    nxCustomerUserCardService.update(userCardEntity);

                                    //给订单添加购买卡的费用
                                    Integer nxCucaCardId = userCardEntity.getNxCucaCardId();
                                    NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(nxCucaCardId);
                                    String nxCcPrice = cardEntity.getNxCcPrice();
                                    nxOrders.setNxCoBuyMemberCardSubtotal(nxCcPrice);
                                    nxCommunityOrdersService.update(nxOrders);
                                } else {
                                    nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
                                }
                            }

                        }

                    }

                }
            }
            reMap.put("orderId", nxOrders.getNxCommunityOrdersId().toString());

            return R.ok().put("map", reMap);


        } catch (Exception e) {
            e.printStackTrace();
        }


        return R.ok();

    }


    /**
     * 京采下单app 结账 已作废也许
     *
     * @param orderId
     * @param openId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/customerCashPay", method = RequestMethod.POST)
    public R customerCashPay(Integer orderId, String openId) {

        NxCommunityOrdersEntity nxOrders = nxCommunityOrdersService.queryObject(orderId);

        MyWxShixianliliPayConfig config = new MyWxShixianliliPayConfig();

        String nxRbTotal = nxOrders.getNxCoTotal();
        Double aDouble = Double.parseDouble(nxRbTotal) * 100;
        int i = aDouble.intValue();
        System.out.println("dfdafkdaksfas" + nxOrders.getNxCoTotal());
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
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxorders/notify");
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

            nxOrders.setNxCoStatus(1);
            nxOrders.setNxCoPaymentStatus(0);
            nxOrders.setNxCoWxOutTradeNo(tradeNo);
            nxCommunityOrdersService.update(nxOrders);


            reMap.put("orderId", nxOrders.getNxCommunityOrdersId().toString());

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
            System.out.println("微信返回给回调函数的信息为NxcommmOrders：" + xml);
            if (notifyMap.get("result_code").equals("SUCCESS")) {
                /*
                 * 以下是自己的业务处理------仅做参考 更新order对应字段/已支付金额/状态码
                 * 更新bill支付状态
                 */
                System.out.println("===NxcommmOrdersnotify===回调方法已经被调！！！" + notifyMap);

                String ordersSn = notifyMap.get("out_trade_no");// 商户订单号
                NxCommunityOrdersEntity billEntity = nxCommunityOrdersService.queryOrderByTradeNo(ordersSn);
                billEntity.setNxCoStatus(2);
                billEntity.setNxCoPaymentStatus(1);
                nxCommunityOrdersService.update(billEntity);


                if (billEntity.getNxCoType() == 0) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("orderId", billEntity.getNxCommunityOrdersId());
                    List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
                    if (nxCommunityOrdersSubEntities.size() > 0) {
                        for (NxCommunityOrdersSubEntity subEntity : nxCommunityOrdersSubEntities) {
                            subEntity.setNxCosStatus(2);
                            nxCommunityOrdersSubService.update(subEntity);
                        }
                    }

                } else {
                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("id", billEntity.getNxCommunityOrdersId());
                    System.out.println("bilosororororororoorro" + mapS);
                    List<NxCommunitySplicingOrdersEntity> nxCommunitySplicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(mapS);
                    if (nxCommunitySplicingOrdersEntities.size() > 0) {
                        for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : nxCommunitySplicingOrdersEntities) {

                            Map<String, Object> map = new HashMap<>();
                            map.put("splicingOrderId", splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                            System.out.println("pspsosoosossos" + splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                            List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
                            if (nxCommunityOrdersSubEntities.size() > 0) {
                                for (NxCommunityOrdersSubEntity subEntity : nxCommunityOrdersSubEntities) {
                                    subEntity.setNxCosStatus(2);
                                    nxCommunityOrdersSubService.update(subEntity);
                                }
                            }

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


    /**
     * @Title: callBack
     * @Description: 支付完成的回调函数
     * @param:
     * @return:
     */
    @RequestMapping("/notifyDesk")
    public String callBackDesk(HttpServletRequest request, HttpServletResponse response) {
        // System.out.println("微信支付成功,微信发送的callback信息,请注意修改订单信息");
        InputStream is = null;
        try {

            is = request.getInputStream();// 获取请求的流信息(这里是微信发的xml格式所有只能使用流来读)
            String xml = WxPayUtils.InputStream2String(is);
            Map<String, String> notifyMap = WxPayUtils.xmlToMap(xml);// 将微信发的xml转map
            System.out.println("微信返回给回调函数的信息为NxcommmOrders：" + xml);
            if (notifyMap.get("result_code").equals("SUCCESS")) {
                /*
                 * 以下是自己的业务处理------仅做参考 更新order对应字段/已支付金额/状态码
                 * 更新bill支付状态
                 */
                System.out.println("===NxcommmOrdersnotify===回调方法已经被调！！！" + notifyMap);

                String ordersSn = notifyMap.get("out_trade_no");// 商户订单号
                NxCommunityOrdersEntity billEntity = nxCommunityOrdersService.queryOrderByTradeNo(ordersSn);
                billEntity.setNxCoStatus(2);
                billEntity.setNxCoPaymentStatus(1);
                nxCommunityOrdersService.update(billEntity);


                // 更新桌子状态
                Integer nxCoDeskId = billEntity.getNxCoDeskId();
                System.out.println("dsisiissis" + billEntity.getNxCoDeskId());
                if (nxCoDeskId != -1 && nxCoDeskId != 99) {
                    NxCommunityDeskEntity deskEntity = nxCommunityDeskService.queryObject(nxCoDeskId);
                    deskEntity.setNxCdStatus(0);
                    nxCommunityDeskService.update(deskEntity);
                }


                if (billEntity.getNxCoType() == 0) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("orderId", billEntity.getNxCommunityOrdersId());
                    List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
                    if (nxCommunityOrdersSubEntities.size() > 0) {
                        for (NxCommunityOrdersSubEntity subEntity : nxCommunityOrdersSubEntities) {
                            //检查 Adsense
                            checkAdsenseGoods(subEntity.getNxCosCommunityGoodsId(), subEntity);

                            subEntity.setNxCosStatus(1);
                            subEntity.setNxCosServiceTime(billEntity.getNxCoServiceTime());
                            subEntity.setNxCosPickUpCode(billEntity.getNxCoWeighNumber());
                            subEntity.setNxCosService(billEntity.getNxCoService());
                            subEntity.setNxCosServiceDate(billEntity.getNxCoServiceDate());
                            subEntity.setNxCosServiceTime(billEntity.getNxCoServiceTime());
                            nxCommunityOrdersSubService.update(subEntity);

                            saveUserGoods(subEntity);

                            printSubOrderGoods(subEntity);

                            if (subEntity.getNxCosCucId() != null) {
                                NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                                customerUserCouponEntity.setNxCucStatus(2);
                                nxCustomerUserCouponService.update(customerUserCouponEntity);
                            }
                        }
                    }
                    Map<String, Object> mapC = new HashMap<>();
                    mapC.put("userId", billEntity.getNxCoUserId());
                    mapC.put("status", -1);
                    List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
                    if (cardEntities.size() > 0) {
                        for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                            if (userCardEntity.getNxCucaIsSelected() == 1) {
                                userCardEntity.setNxCucaStatus(0);
                                userCardEntity.setNxCucaComOrderId(billEntity.getNxCommunityOrdersId());
                                nxCustomerUserCardService.update(userCardEntity);
                                //给订单添加购买卡的费用
                                Integer nxCucaCardId = userCardEntity.getNxCucaCardId();
                                NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(nxCucaCardId);
                                String nxCcPrice = cardEntity.getNxCcPrice();
                                billEntity.setNxCoBuyMemberCardSubtotal(nxCcPrice);
                                nxCommunityOrdersService.update(billEntity);
                            } else {
                                nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
                            }
                        }
                    }

                } else {
                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("id", billEntity.getNxCommunityOrdersId());
                    System.out.println("bilosororororororoorro" + mapS);
                    List<NxCommunitySplicingOrdersEntity> nxCommunitySplicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(mapS);
                    if (nxCommunitySplicingOrdersEntities.size() > 0) {
                        for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : nxCommunitySplicingOrdersEntities) {

                            Map<String, Object> map = new HashMap<>();
                            map.put("splicingOrderId", splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                            System.out.println("pspsosoosossos" + splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                            List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
                            if (nxCommunityOrdersSubEntities.size() > 0) {
                                for (NxCommunityOrdersSubEntity subEntity : nxCommunityOrdersSubEntities) {

                                    //检查 Adsense
                                    checkAdsenseGoods(subEntity.getNxCosCommunityGoodsId(), subEntity);

                                    subEntity.setNxCosStatus(1);
                                    subEntity.setNxCosServiceTime(billEntity.getNxCoServiceTime());
                                    subEntity.setNxCosPickUpCode(billEntity.getNxCoWeighNumber());
                                    subEntity.setNxCosService(billEntity.getNxCoService());
                                    subEntity.setNxCosServiceDate(billEntity.getNxCoServiceDate());
                                    subEntity.setNxCosServiceTime(billEntity.getNxCoServiceTime());
                                    nxCommunityOrdersSubService.update(subEntity);
                                    saveUserGoods(subEntity);

                                    if (subEntity.getNxCosCucId() != null) {
                                        NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                                        customerUserCouponEntity.setNxCucStatus(2);
                                        nxCustomerUserCouponService.update(customerUserCouponEntity);
                                    }


                                }
                            }

                            Map<String, Object> mapC = new HashMap<>();
                            mapC.put("userId", splicingOrdersEntity.getNxCsoUserId());
                            mapC.put("status", -1);
                            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
                            if (cardEntities.size() > 0) {
                                for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                                    if (userCardEntity.getNxCucaIsSelected() == 1) {
                                        userCardEntity.setNxCucaStatus(0);
                                        userCardEntity.setNxCucaComOrderId(billEntity.getNxCommunityOrdersId());
                                        nxCustomerUserCardService.update(userCardEntity);

                                        //给订单添加购买卡的费用
                                        Integer nxCucaCardId = userCardEntity.getNxCucaCardId();
                                        NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(nxCucaCardId);
                                        String nxCcPrice = cardEntity.getNxCcPrice();
                                        billEntity.setNxCoBuyMemberCardSubtotal(nxCcPrice);
                                        nxCommunityOrdersService.update(billEntity);


                                    } else {
                                        nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
                                    }
                                }

                            }

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


    /**
     * 飞鹅打印内容
     *
     * @param subEntity
     */
    private void printSubOrderGoodsDesk(NxCommunityOrdersSubEntity subEntity) {

        System.out.println("pringoods" + subEntity);
        Integer nxCosNxGoodsId = subEntity.getNxCosCommunityGoodsId();
        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(nxCosNxGoodsId);
        String sn = goodsEntity.getNxCgPrintSn();

        Integer nxCosDeskId = subEntity.getNxCosDeskId();
        NxCommunityDeskEntity deskEntity = new NxCommunityDeskEntity();
        if (nxCosDeskId != -1 && nxCosDeskId != 99) {
            deskEntity = nxCommunityDeskService.queryObject(nxCosDeskId);
        } else {
            deskEntity.setNxCommunityDeskName("堂食");
        }


        String content;
        content = "<CB>" + deskEntity.getNxCommunityDeskName() + "</CB><BR>";
        content += "<CB>" + goodsEntity.getNxCgGoodsName() + subEntity.getNxCosQuantity() + "</CB><BR>";
//        content += "--------------------------------<BR>";

        //通过POST请求，发送打印信息到服务器
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(30000)//读取超时
                .setConnectTimeout(30000)//连接超时
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpPost post = new HttpPost(URL);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("user", USER));
        String STIME = String.valueOf(System.currentTimeMillis() / 1000);
        nvps.add(new BasicNameValuePair("stime", STIME));
        nvps.add(new BasicNameValuePair("sig", signature(USER, UKEY, STIME)));
        nvps.add(new BasicNameValuePair("apiname", "Open_printMsg"));//固定值,不需要修改
        nvps.add(new BasicNameValuePair("sn", sn));
        nvps.add(new BasicNameValuePair("content", content));
        nvps.add(new BasicNameValuePair("times", "1"));//打印联数

        CloseableHttpResponse response = null;
        String result = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
            response = httpClient.execute(post);
            int statecode = response.getStatusLine().getStatusCode();
            if (statecode == 200) {
                HttpEntity httpentity = response.getEntity();
                if (httpentity != null) {
                    //服务器返回的JSON字符串，建议要当做日志记录起来
                    result = EntityUtils.toString(httpentity);
                    subEntity.setNxCosStatus(2);
                    nxCommunityOrdersSubService.update(subEntity);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                post.abort();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("resulslsl" + result);

    }

    private void printSubOrderGoods(NxCommunityOrdersSubEntity subEntity) {

        System.out.println("pringoods" + subEntity);
        Integer nxCosNxGoodsId = subEntity.getNxCosCommunityGoodsId();
        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(nxCosNxGoodsId);
        String sn = goodsEntity.getNxCgPrintSn();

        Integer nxCosDeskId = subEntity.getNxCosDeskId();
        NxCommunityDeskEntity deskEntity = new NxCommunityDeskEntity();
        if (nxCosDeskId != -1 && nxCosDeskId != 99) {
            deskEntity = nxCommunityDeskService.queryObject(nxCosDeskId);
        } else {
            deskEntity.setNxCommunityDeskName("堂食");
        }


        String content;
        content = "<CB>" + deskEntity.getNxCommunityDeskName() + "</CB><BR>";
        content += "<CB>" + goodsEntity.getNxCgGoodsName() + subEntity.getNxCosQuantity() + "</CB><BR>";
//        content += "--------------------------------<BR>";

        //通过POST请求，发送打印信息到服务器
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(30000)//读取超时
                .setConnectTimeout(30000)//连接超时
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpPost post = new HttpPost(URL);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("user", USER));
        String STIME = String.valueOf(System.currentTimeMillis() / 1000);
        nvps.add(new BasicNameValuePair("stime", STIME));
        nvps.add(new BasicNameValuePair("sig", signature(USER, UKEY, STIME)));
        nvps.add(new BasicNameValuePair("apiname", "Open_printMsg"));//固定值,不需要修改
        nvps.add(new BasicNameValuePair("sn", sn));
        nvps.add(new BasicNameValuePair("content", content));
        nvps.add(new BasicNameValuePair("times", "1"));//打印联数

        CloseableHttpResponse response = null;
        String result = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
            response = httpClient.execute(post);
            int statecode = response.getStatusLine().getStatusCode();
            if (statecode == 200) {
                HttpEntity httpentity = response.getEntity();
                if (httpentity != null) {
                    //服务器返回的JSON字符串，建议要当做日志记录起来
                    result = EntityUtils.toString(httpentity);
                    subEntity.setNxCosStatus(2);
                    nxCommunityOrdersSubService.update(subEntity);

                    Integer nxCosOrdersId = subEntity.getNxCosOrdersId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("orderId", nxCosOrdersId);
                    map.put("status", 1);
                    System.out.println("orderid=" + subEntity.getNxCommunityOrdersSubId() + "ordsusssnummmmmmmmmmm" + map);
                    List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
                    System.out.println("sisisisiisisisisiziziz" + subEntities.size());
                    if (subEntities.size() == 0) {
                        NxCommunityOrdersEntity nxCommunityOrdersEntity = nxCommunityOrdersService.queryObject(nxCosOrdersId);
                        nxCommunityOrdersEntity.setNxCoStatus(3);
                        nxCommunityOrdersService.update(nxCommunityOrdersEntity);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                post.abort();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("resulslsl" + result);

    }

    //生成签名字符串
    private static String signature(String USER, String UKEY, String STIME) {
        String s = DigestUtils.sha1Hex(USER + UKEY + STIME);
        return s;
    }


    private void checkAdsenseGoods(Integer goodsId, NxCommunityOrdersSubEntity subEntity) {
        System.out.println("checkckkckckckckckc");
        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        if (goodsEntity.getNxCgIsOpenAdsense() == 1) {

            System.out.println("minieiieieiieiieieei");
            int nowMinute = getNowMinute();
            int nxCgAdsenseStartTimeZone = Integer.parseInt(goodsEntity.getNxCgAdsenseStartTimeZone());
            int nxCgAdsenseStopTimeZone = Integer.parseInt(goodsEntity.getNxCgAdsenseStopTimeZone());
            if (nowMinute > nxCgAdsenseStartTimeZone && nowMinute < nxCgAdsenseStopTimeZone) {
                System.out.println("minieiieieiieiieieei" + nowMinute);
                BigDecimal adQuantity = new BigDecimal(goodsEntity.getNxCgAdsenseRestQuantity());
                BigDecimal subtract = adQuantity.subtract(new BigDecimal(subEntity.getNxCosQuantity()));
                goodsEntity.setNxCgAdsenseRestQuantity(Integer.valueOf(subtract.toString()));
                nxCommunityGoodsService.update(goodsEntity);
            }

        }

    }


}