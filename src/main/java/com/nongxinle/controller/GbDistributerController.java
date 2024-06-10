package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-21 20:42
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeAppSupplier;


@RestController
@RequestMapping("api/gbdistributer")
public class GbDistributerController {
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    private GbDistributerUserService gbDistributerUserService;
    @Autowired
    private NxDistributerUserService nxDistributerUserService;
    @Autowired
    private NxDepartmentUserService nxDepartmentUserService;
    @Autowired
    private NxBuyUserService nxBuyUserService;
    @Autowired
    private NxSellUserService nxSellUserService;
    @Autowired
    private GbDistributerModuleService gbDistributerModuleService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private NxDistributerGbDistributerService nxDisGbDisService;
    @Autowired
    private GbDistributerFatherGoodsService dgfService;



    @RequestMapping(value = "/updateGbDistributer", method = RequestMethod.POST)
    @ResponseBody
    public R updateGbDistributer (@RequestBody GbDistributerEntity distributer ) {
        GbDistributerModuleEntity gbDistributerModuleEntity = distributer.getGbDistributerModuleEntity();
        if(gbDistributerModuleEntity != null){
            Integer gbDmStockNumber = gbDistributerModuleEntity.getGbDmStockNumber();
            Integer purchaseNumber = gbDistributerModuleEntity.getGbDmPurchaseNumber();
            Integer appSupplierNumber = gbDistributerModuleEntity.getGbDmAppSupplierNumber();
            if(gbDmStockNumber == -1){
                Map<String, Object> map = new HashMap<>();
                map.put("disId", distributer.getGbDistributerId());
                map.put("type", getGbDepartmentTypeKufang());
                List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
                if(departmentEntities.size() > 0){
                    return R.error(-1, "先删除库房部门");
                }
            }
            if(purchaseNumber == -1){
                Map<String, Object> map = new HashMap<>();
                map.put("disId", distributer.getGbDistributerId());
                map.put("type", getGbDepartmentTypeJicai());
                List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
                if(departmentEntities.size() > 0){
                    return R.error(-1, "先删除集采部门");
                }
            }
            if(appSupplierNumber == -1){
                Map<String, Object> map = new HashMap<>();
                map.put("disId", distributer.getGbDistributerId());
                map.put("type", getGbDepartmentTypeAppSupplier());
                List<NxDistributerEntity> nxDistributerEntities = nxDisGbDisService.queryAllNxDistribtuer(distributer.getGbDistributerId());
                if(nxDistributerEntities.size() > 0){
                    return R.error(-1, "先删除配送商");
                }
            }
            System.out.println("pessososososs" + appSupplierNumber);
            if(appSupplierNumber == 0){
                Map<String, Object> map = new HashMap<>();
                map.put("disId", distributer.getGbDistributerId());
                map.put("type", getGbDepartmentTypeAppSupplier());
                List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
                if(departmentEntities.size() == 0){
                    System.out.println("pessososososs" + appSupplierNumber);

                    GbDepartmentEntity departmentEntity = new GbDepartmentEntity();
                    departmentEntity.setGbDepartmentDisId(distributer.getGbDistributerId());
                    departmentEntity.setGbDepartmentFatherId(0);
                    departmentEntity.setGbDepartmentType(getGbDepartmentTypeAppSupplier());
                    distributer.setGbDistributerSettleDate(formatWhatDay(0));
                    departmentEntity.setGbDepartmentSettleFullTime(formatFullTime());
                    departmentEntity.setGbDepartmentSettleDate(formatWhatDay(0));
                    departmentEntity.setGbDepartmentSettleMonth(formatWhatMonth(0));
                    departmentEntity.setGbDepartmentSettleWeek(getWeekOfYear(0).toString());
                    departmentEntity.setGbDepartmentSettleYear(formatWhatYear(0));
                    departmentEntity.setGbDepartmentSettleTimes("0");
                    departmentEntity.setGbDepartmentSubAmount(0);
                    departmentEntity.setGbDepartmentIsGroupDep(1);
                    departmentEntity.setGbDepartmentAttrName(distributer.getGbDistributerName() + "配送商管理部门");
                    departmentEntity.setGbDepartmentName(distributer.getGbDistributerName() + "配送商管理部门");
                    gbDepartmentService.save(departmentEntity);


                    GbDistributerFatherGoodsEntity grand = new GbDistributerFatherGoodsEntity();
                    grand.setGbDfgFatherGoodsName("配送商品");
                    grand.setGbDfgDistributerId(distributer.getGbDistributerId());
                    grand.setGbDfgFatherGoodsLevel(0);
                    grand.setGbDfgFatherGoodsColor("#187e6e");
                    grand.setGbDfgFatherGoodsImg("goodsImage/logo.jpg");
                    grand.setGbDfgNxGoodsId(-1);
                    System.out.println("pessosososossdddd" + appSupplierNumber);

                    dgfService.save(grand);


                }
            }
            gbDistributerModuleService.update(gbDistributerModuleEntity);
        }
        gbDistributerService.update(distributer);

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


    @RequestMapping(value = "/sisyDelteUser", method = RequestMethod.POST)
    @ResponseBody
    public R sisyDelteUser(Integer id, String type) {
        if (type.equals("dis")) {
            nxDistributerUserService.delete(id);
        }

        if (type.equals("dep")) {
            nxDepartmentUserService.delete(id);
        }

        if (type.equals("buy")) {
            nxBuyUserService.delete(id);
        }
        if (type.equals("sell")) {
            nxSellUserService.delete(id);
        }
        if (type.equals("gb")) {
            gbDistributerUserService.delete(id);
        }

        return R.ok();
    }

    @RequestMapping(value = "/sisyGetAllGb")
    @ResponseBody
    public R sisyGetAllGb() {
        List<GbDistributerEntity> gbDistributerEntityList = gbDistributerService.queryListAll();


        List<NxDistributerUserEntity> disUsers = nxDistributerUserService.getAllDisUsers();
        List<NxDepartmentUserEntity> depUsers = nxDepartmentUserService.queryAllDepUsers();
        List<GbDistributerUserEntity> gbDistributerUserEntities =  gbDistributerUserService.queryAllGbUsers();
        Map<String, Object> map = new HashMap<>();
        map.put("gbArr", gbDistributerEntityList);
        map.put("disUsers", disUsers);
        map.put("depUsers", depUsers);
        map.put("gbUsers", gbDistributerUserEntities);
        return R.ok().put("data", map);
    }


    @RequestMapping(value = "/kaitongDis/{id}")
    @ResponseBody
    public R kaitongDis(@PathVariable Integer id) {
        gbDistributerService.kaitongGbDis(id);
        return R.ok();
    }


    @RequestMapping(value = "/queryDisInfo/{disId}")
    @ResponseBody
    public R queryDisInfo(@PathVariable Integer disId) {
        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(disId);
        return R.ok().put("data", gbDistributerEntity);
    }

    @RequestMapping(value = "/disManRegisterNewGb", method = RequestMethod.POST)
    @ResponseBody
    public R disManRegisterNewGb(@RequestBody GbDistributerEntity gbDistributerEntity) {
//wxApp
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        GbDistributerUserEntity distributerUserEntity = gbDistributerEntity.getGbDistributerUserEntity();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiansuocaigouguanliduanAppId() +
                "&secret=" + myAPPIDConfig.getLiansuocaigouguanliduanScreat() + "&js_code=" + distributerUserEntity.getGbDiuCode() + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        GbDistributerUserEntity gbDistributerUserEntity = gbDistributerUserService.queryDisUserByOpenIdGb(openid);

        if (gbDistributerUserEntity == null) {
            gbDistributerEntity.getGbDistributerUserEntity().setGbDiuWxOpenId(openid);
            Integer resUserId = gbDistributerService.saveNewDistributerGb(gbDistributerEntity);
            if (resUserId != null) {
                Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(resUserId);
                return R.ok().put("data", stringObjectMap);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }
    }

    @RequestMapping(value = "/7dlJV7E0V9.txt")
    @ResponseBody
    public String nxGbDisRegist( ) {
        return "76318ecf60874e0f7d2c2642938343d0";
    }


    @RequestMapping(value = "/disManRegisterNewGbWithNxDis", method = RequestMethod.POST)
    @ResponseBody
    public R disManRegisterNewGbWithNxDis(@RequestBody GbDistributerEntity gbDistributerEntity) {
//wxApp
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        GbDistributerUserEntity distributerUserEntity = gbDistributerEntity.getGbDistributerUserEntity();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiansuocaigouguanliduanAppId() +
                "&secret=" + myAPPIDConfig.getLiansuocaigouguanliduanScreat() + "&js_code=" + distributerUserEntity.getGbDiuCode() + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        GbDistributerUserEntity gbDistributerUserEntity = gbDistributerUserService.queryDisUserByOpenIdGb(openid);

        if (gbDistributerUserEntity == null) {
            gbDistributerEntity.getGbDistributerUserEntity().setGbDiuWxOpenId(openid);
            Integer resUserId = gbDistributerService.saveNewDistributerGb(gbDistributerEntity);
            if (resUserId != null) {
                Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(resUserId);
                return R.ok().put("data", stringObjectMap);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }
    }

    @RequestMapping(value = "/disManRegisterNewGbSxWork", method = RequestMethod.POST)
    @ResponseBody
    public R disManRegisterNewGbSxWork(@RequestBody GbDistributerEntity gbDistributerEntity) {

        String suiteToken = getWxProperty(Constant.SUITE_TOKEN_RX);
        GbDistributerUserEntity distributerUserEntity = gbDistributerEntity.getGbDistributerUserEntity();
        String code = distributerUserEntity.getGbDiuCode();
        String userCropUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/miniprogram/jscode2session?suite_access_token="+suiteToken
                +"&js_code="+code+"&grant_type=authorization_code";

        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(userCropUrl, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println("jsonObjectjsonObjectwwwwwSSSS=====" + jsonObject);
        String openUserId = jsonObject.getString("open_userid");
        GbDistributerUserEntity gbDistributerUserEntity = gbDistributerUserService.queryDisUserByOpenIdGb(openUserId);

        if (gbDistributerUserEntity == null) {
            gbDistributerEntity.getGbDistributerUserEntity().setGbDiuWxOpenId(openUserId);
            Integer resUserId = gbDistributerService.saveNewDistributerGbWork(gbDistributerEntity ,jsonObject);
            if (resUserId != null) {
                Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(resUserId);
                return R.ok().put("data", stringObjectMap);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过");
        }
    }

    private   String getWxProperty(String key) {
        Properties pps = new Properties();
        InputStream is = NxDistributerUserController.class.getClassLoader().getResourceAsStream("wx.properties");
        try {
            pps.load(is);
            String value = pps.getProperty(key);
            System.out.println(key + " = " + value);
            is.close();
            System.out.println("getWxProperty---------------" + key + "===" + pps.get(key));
            return value;

        } catch (IOException e) {
            e.printStackTrace();
            return "-1";
        }
    }

    @RequestMapping(value = "/disManRegisterNewGbSxWithFile",  produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R disManRegisterNewGbSxWithFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam("userName") String userName,
                                           @RequestParam("disId") Integer disId,
                                           @RequestParam("phone") String phone,
                                           @RequestParam("code") String code,
                                           HttpSession session) {
        System.out.println("hpienei" + phone);

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();


        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getShixianGuanliAppId() +
                "&secret=" + myAPPIDConfig.getShixianGuanliScreat() + "&js_code=" + code + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        GbDistributerUserEntity gbDistributerUserEntity = gbDistributerUserService.queryDisUserByOpenIdGb(openid);

        if (gbDistributerUserEntity == null) {
            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            //1 disuser save
            GbDistributerUserEntity distributerUserEntity = new GbDistributerUserEntity();
            distributerUserEntity.setGbDiuPrintDeviceId("-1");
            distributerUserEntity.setGbDiuWxPhone(phone);
            distributerUserEntity.setGbDiuLoginTimes(0);
            distributerUserEntity.setGbDiuPrintBillDeviceId("-1");
            distributerUserEntity.setGbDiuLoginTimes(0);
            distributerUserEntity.setGbDiuWxOpenId(openid);
            distributerUserEntity.setGbDiuWxNickName(userName);
            distributerUserEntity.setGbDiuWxAvartraUrl(filePath);
            distributerUserEntity.setGbDiuUrlChange(1);
            distributerUserEntity.setGbDiuDistributerId(disId);
            Integer resUserId = gbDistributerService.saveNewDistributerGbForPeisong(distributerUserEntity, disId);
            return R.ok();

        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }
    }

    @RequestMapping(value = "/disManRegisterNewGbSx", method = RequestMethod.POST)
    @ResponseBody
    public R disManRegisterNewGbSx(@RequestBody GbDistributerEntity gbDistributerEntity) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        GbDistributerUserEntity distributerUserEntity = gbDistributerEntity.getGbDistributerUserEntity();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getShixianGuanliAppId() +
                "&secret=" + myAPPIDConfig.getShixianGuanliScreat() + "&js_code=" + distributerUserEntity.getGbDiuCode() + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        GbDistributerUserEntity gbDistributerUserEntity = gbDistributerUserService.queryDisUserByOpenIdGb(openid);

        if (gbDistributerUserEntity == null) {
            gbDistributerEntity.getGbDistributerUserEntity().setGbDiuWxOpenId(openid);
            Integer resUserId = gbDistributerService.saveNewDistributerGb(gbDistributerEntity);
            if (resUserId != null) {
                Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(resUserId);
                return R.ok().put("data", stringObjectMap);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }
    }




}
