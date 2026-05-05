package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-16 11:26
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbDepUserAdminMendianjingli;


@RestController
@RequestMapping("api/gbdepartmentuser")
public class GbDepartmentUserController {
    @Autowired
    private GbDepartmentUserService gbDepartmentUserService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    private GbDistributerPurchaseBatchService gbDisPurchaseBatchService;
    @Autowired
    private NxJrdhUserService nxJrdhUserService;
    @Autowired
    private NxJrdhSupplierService nxJrdhSupplierService;






    @RequestMapping(value = "/whichJrdhUserLoginGbJj", method = RequestMethod.POST)
    @ResponseBody
    public R whichJrdhUserLoginGbJj(String code, Integer gbDisId, Integer batchId,
                                  Integer gbDepId, Integer buyUserId) {
        System.out.println("whiciciiicic");
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String maimaiAppID = myAPPIDConfig.getTexiansongCaigouAppId();
        String maimaiScreat = myAPPIDConfig.getTexiansongCaigouScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
                maimaiScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        String str = WeChatUtil.httpRequest(url, "GET", null);
        JSONObject jsonObject = JSONObject.parseObject(str);
        String openId = jsonObject.get("openid").toString();
        if (openId != null && !openId.trim().isEmpty()) {
            Map<String, Object> mapB = new HashMap<>();
            mapB.put("batchId", batchId);
            GbDistributerPurchaseBatchEntity batchEntity = gbDisPurchaseBatchService.queryBatchItemByParams(mapB);
            Map<String, Object> map = new HashMap<>();
            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDisId);

            map.put("disInfo", gbDistributerEntity);

            //首先判断是不是dis 用户
            Map<String, Object> mapUser = new HashMap<>();
            mapUser.put("openId", openId);
            mapUser.put("admin", 2);
            GbDepartmentUserEntity caigouUser = gbDepartmentUserService.queryDepUsersByOpenIdAndAdmin(mapUser);
            if (caigouUser != null) {
                map.put("userInfo", caigouUser);
                map.put("buyUser", true);
                map.put("supplierInfo", null);
                map.put("code", 1);
            } else {
                Map<String, Object> mapUserSell = new HashMap<>();
                mapUserSell.put("openId", openId);
                mapUserSell.put("admin", 3);
                NxJrdhUserEntity jrdhUserEntitySell = nxJrdhUserService.queryJrdhUserById(mapUserSell);
                if (jrdhUserEntitySell != null) {
                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("gbDisId", gbDisId);
                    mapS.put("userId", jrdhUserEntitySell.getNxJrdhUserId());
                    NxJrdhSupplierEntity nxJrdhSupplierEntity = nxJrdhSupplierService.querySellUserSupplier(mapS);
                    if (nxJrdhSupplierEntity == null) {
                        NxJrdhSupplierEntity supplierEntity = saveJrdhSupplerGb(jrdhUserEntitySell, gbDisId, gbDepId, buyUserId);
                        map.put("supplierInfo", supplierEntity);
                    } else {
                        map.put("supplierInfo", nxJrdhSupplierEntity);
                    }
                    map.put("buyUser", false);
                    map.put("userInfo", jrdhUserEntitySell);
                    map.put("code", 1);
                } else {
                    map.put("code", -1);
                    return R.ok().put("data", map);
                }
            }
            if(batchEntity != null){
                map.put("batch", batchEntity);
                return R.ok().put("data", map);
            }else{

                map.put("batch", -1);
                return R.ok().put("data", map);
            }

        } else {
            return R.error(-1, "注册失败");
        }
    }


    private NxJrdhSupplierEntity saveJrdhSupplerGb(NxJrdhUserEntity jrdhUserEntity, Integer gbDisId,
                                                   Integer gbDepId, Integer buyUserId) {
        NxJrdhSupplierEntity supplierEntity = new NxJrdhSupplierEntity();
        supplierEntity.setNxJrdhsUserId(jrdhUserEntity.getNxJrdhUserId());
        supplierEntity.setNxJrdhsGbDistributerId(gbDisId);
        supplierEntity.setNxJrdhsSupplierName(jrdhUserEntity.getNxJrdhWxNickName());
        supplierEntity.setNxJrdhsNxCommunityId(-1);
        supplierEntity.setNxJrdhsNxPurUserId(-1);
        supplierEntity.setNxJrdhsCommPurUserId(-1);
        supplierEntity.setNxJrdhsNxJrdhBuyUserId(buyUserId);
        supplierEntity.setNxJrdhsGbDepartmentId(gbDepId);
        supplierEntity.setNxJrdhsNxDistributerId(-1);
        supplierEntity.setNxJrdhsStatus(0);
        supplierEntity.setNxJrdhsSysCityId(6);
        supplierEntity.setNxJrdhsSysMarketId(-1);
        nxJrdhSupplierService.save(supplierEntity);
        return supplierEntity;
    }



    @ResponseBody
    @RequestMapping(value = "/depOrderUserSaveWithFileGb", produces = "text/html;charset=UTF-8")
    public R depOrderUserSaveWithFileGb(@RequestParam("file") MultipartFile file,
                                        @RequestParam("userName") String userName,
                                        @RequestParam("disId") Integer disId,
                                        @RequestParam("depFatherId") Integer depFatherId,
                                        @RequestParam("depId") Integer depId,
                                        @RequestParam("code") String code,
                                        HttpSession session) {

        System.out.println("aaa" + disId + userName + depFatherId + depId + code);
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

//        String orderAppID = myAPPIDConfig.getJingjingOrderAppID();
        String orderAppID = myAPPIDConfig.getTexiansongCaigouAppId();
//        String orderScreat = myAPPIDConfig.getJingjingOrderScreat();
        String orderScreat = myAPPIDConfig.getTexiansongCaigouScreat();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + orderAppID + "&secret=" + orderScreat + "&js_code=" + code + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        System.out.println("opeidiididid" + openid);
        Map<String, Object> map = new HashMap<>();
        map.put("openId", openid);
        map.put("admin", 2);
        GbDepartmentUserEntity depUserEntities = gbDepartmentUserService.queryDepUsersByOpenIdAndAdmin(map);
        NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryWhichUserByOpenId(openid);
        if (depUserEntities != null && nxJrdhUserEntity == null) {
            return R.error(-1, "请直接登陆");
        } else {
            GbDepartmentUserEntity gbDepartmentUserEntity = new GbDepartmentUserEntity();

            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            gbDepartmentUserEntity.setGbDuWxAvartraUrl(filePath);
            gbDepartmentUserEntity.setGbDuWxNickName(userName);
            gbDepartmentUserEntity.setGbDuUrlChange(1);
            gbDepartmentUserEntity.setGbDuDepartmentId(depId);
            gbDepartmentUserEntity.setGbDuDepartmentFatherId(depFatherId);
            gbDepartmentUserEntity.setGbDuDistributerId(disId);
            gbDepartmentUserEntity.setGbDuWxOpenId(openid);
            gbDepartmentUserEntity.setGbDuJoinDate(formatWhatDay(0));
            gbDepartmentUserEntity.setGbDuLoginTimes(0);
            gbDepartmentUserEntity.setGbDuAdmin(1);
            gbDepartmentUserService.save(gbDepartmentUserEntity);

            Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(gbDepartmentUserEntity.getGbDepartmentUserId());
            return R.ok().put("data", stringObjectMap);
        }
    }


    @RequestMapping(value = "/jjdhUserRegiste", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R jjdhUserRegiste(@RequestParam("file") MultipartFile file,
                             @RequestParam("userName") String userName,
                             @RequestParam("code") String code,
                             @RequestParam("depId") Integer depId,
                             @RequestParam("disId") Integer disId,
                             HttpSession session) {

        //wxApp
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String purchaseAppID = myAPPIDConfig.getJingjingOrderAppID();
        String purchaseScreat = myAPPIDConfig.getJingjingOrderScreat();


        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + purchaseAppID + "&secret=" + purchaseScreat + "&js_code=" + code + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();


        //1,上传图片
        String newUploadName = "uploadImage";
        String realPath = UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;

        GbDepartmentUserEntity userEntity = new GbDepartmentUserEntity();
        userEntity.setGbDuDistributerId(disId);
        userEntity.setGbDuDepartmentId(depId);
        userEntity.setGbDuDepartmentFatherId(0);
        userEntity.setGbDuWxAvartraUrl(filePath);
        userEntity.setGbDuUrlChange(1);
        userEntity.setGbDuWxOpenId(openid);
        userEntity.setGbDuWxNickName(userName);
        userEntity.setGbDuAdmin(1);
        userEntity.setGbDuJoinDate(formatWhatDay(0));
        userEntity.setGbDuLoginTimes(0);

        gbDepartmentUserService.save(userEntity);

        Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(userEntity.getGbDepartmentUserId());
        return R.ok().put("data", stringObjectMap);

    }


    @RequestMapping(value = "/jjdhUserLogin/{code}")
    @ResponseBody
    public R jjdhUserLogin(@PathVariable String code) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String orderAppID = myAPPIDConfig.getJingjingOrderAppID();
        String orderScreat = myAPPIDConfig.getJingjingOrderScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + orderAppID + "&secret=" +
                orderScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openId = jsonObject.get("openid").toString();
        System.out.println("logigngignigbyouseriid" + openId);
        GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);
        if (depUserEntity != null) {
            Integer nxDepartmentUserId = depUserEntity.getGbDepartmentUserId();
            Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(nxDepartmentUserId);
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
        }
    }


    //

    @RequestMapping(value = "/2w7dX7YekK.txt")
    @ResponseBody
    public String gbProductionUserRegiste() {
        return "b85d798423c3eb116ae3e76dffe129a0";
    }

    @RequestMapping(value = "/0isrRThinh.txt")
    @ResponseBody
    public String gbStockUserRegiste() {
        return "d5b77ff5aa86197e479a970f498a3fe0";
    }

    @RequestMapping(value = "/TGpnXXGziQ.txt")
    @ResponseBody
    public String gbPurchaseUserRegiste() {
        return "b7c49216235b3023912574cbde8a40c8";
    }


    @RequestMapping(value = "/saMNYilLfd.txt")
    @ResponseBody
    public String gbdepartmentUserRegiste() {
        return "af86d3eda626691cacd6b46fa555dc0d";
    }


    @RequestMapping(value = "/getDisDepAdminTypeUsers", method = RequestMethod.POST)
    @ResponseBody
    public R getDisDepAdminTypeUsers(Integer disId, Integer adminType) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("adminType", adminType);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepWithAdminUserByParams(map);
        return R.ok().put("data", departmentEntities);
    }


    @RequestMapping(value = "/depUserClock", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R depUserClock(@RequestParam("file") MultipartFile file,
                          @RequestParam("userId") Integer userId,
                          HttpSession session) {
        //1,上传图片
        String newUploadName = "uploadClock";
        String realPath = UploadFile.uploadClock(session, newUploadName, file);
        System.out.println(realPath);
        System.out.println(userId);


        return R.ok();

    }


    @RequestMapping(value = "/getGbDepAndUserInfo/{userId}")
    @ResponseBody
    public R getGbDepAndUserInfo(@PathVariable Integer userId) {
        Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(userId);
        return R.ok().put("data", stringObjectMap);
    }

    /**
     * 门店管理端，采购端，库房端注册用户
     * @param gbDepartmentUser 用户
     * @return 用户
     */
//    @RequestMapping(value = "/gbGroupDepartmentUserSave", method = RequestMethod.POST)
//    @ResponseBody
//    public R gbGroupDepartmentUserSave(@RequestBody GbDepartmentUserEntity gbDepartmentUser) {
//        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
//        String appId = "";
//        String secret  = "";
//
//        Integer gbDuAdmin = gbDepartmentUser.getGbDuAdmin();
//        System.out.println("guadin======" + gbDuAdmin);
//        if(gbDuAdmin.equals(getGbDepUserAdminMendianjingli())){
//            appId = myAPPIDConfig.getLiancaiMendianAppId();
//            secret = myAPPIDConfig.getLiancaiMendianScreat();
//            gbDepartmentUser.setGbDuAdmin(getGbDepUserAdminMendianjingli());
//        }
//        if(gbDuAdmin.equals(getGbDepUserAdminJicaiyuan())
//                || gbDuAdmin.equals(getGbDepUserAdminKufangcaigouyuan())
//                || gbDuAdmin.equals(getGbDepUserAdminMendiancaigouyuan())
//        ){
//            appId = myAPPIDConfig.getLiancaiCaigouAppId();
//            secret = myAPPIDConfig.getLiancaiCaigouScreat();
//
//        }
//
//        if(gbDuAdmin.equals(getGbDepUserAdminKufangguanliyuan() )){
//            appId = myAPPIDConfig.getLiancaiKufangAppId();
//            secret = myAPPIDConfig.getLiancaiKufangScreat();
//            gbDepartmentUser.setGbDuAdmin(getGbDepUserAdminKufangguanliyuan());
//        }
//        if(gbDuAdmin.equals(getGbDepUserAdminKitchenguanliyuan() )){
//            appId = myAPPIDConfig.getShixianLiliAppId();
//            secret = myAPPIDConfig.getShixianLiliScreat();
//            gbDepartmentUser.setGbDuAdmin(getGbDepUserAdminKitchenguanliyuan());
//
//        }
//
//        if(gbDuAdmin.equals(getGbDepUserAdminPeisongyuan() )){
//            appId = myAPPIDConfig.getShanghuoAppID();
//            secret = myAPPIDConfig.getShanghuoScreat();
//            gbDepartmentUser.setGbDuAdmin(getGbDepUserAdminPeisongyuan());
//
//        }
//
//        String code = gbDepartmentUser.getGbDuCode();
//        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId + "&secret=" +
//                secret + "&js_code=" + code +
//                "&grant_type=authorization_code";
//        // 发送请求，返回Json字符串
//        String str = WeChatUtil.httpRequest(url, "GET", null);
//
//        // 转成Json对象 获取openid
//        JSONObject jsonObject = JSONObject.parseObject(str);
//        System.out.println(jsonObject);
//
//        // 我们需要的openid，在一个小程序中，openid是唯一的
//        String openId = jsonObject.get("openid").toString();
//        Map<String, Object> map = new HashMap<>();
//        map.put("openId", openId);
//        map.put("admin", gbDuAdmin);
//        GbDepartmentUserEntity depUserEntities = gbDepartmentUserService.queryGroupDepartmentUserByParams(map);
//        if (depUserEntities != null) {
//            return R.error(-1, "请直接登陆");
//        } else {
//            //添加新用户
//            gbDepartmentUser.setGbDuWxOpenId(openId);
//            gbDepartmentUser.setGbDuJoinDate(formatWhatDay(0));
//            gbDepartmentUser.setGbDuPrintDeviceId("-1");
//            gbDepartmentUser.setGbDuPrintBillDeviceId("-1");
//            gbDepartmentUserService.save(gbDepartmentUser);
//            Integer gbDepartmentUserId = gbDepartmentUser.getGbDepartmentUserId();
//            Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(gbDepartmentUserId);
//            return R.ok().put("data", stringObjectMap);
//        }
//    }

    /**
     * 门店管理端，采购端，库房端注册用户
     *
     * @param
     * @return 用户
     */
    @RequestMapping(value = "/gbGroupDepartmentUserSaveWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R gbGroupDepartmentUserSaveWithFile(@RequestParam("file") MultipartFile file,
                                               @RequestParam("userName") String userName,
                                               @RequestParam("code") String code,
                                               @RequestParam("disId") Integer disId,
                                               @RequestParam("admin") Integer admin,
                                               @RequestParam("depFatherId") Integer depFatherId,
                                               @RequestParam("depId") Integer depId,
                                               HttpSession session) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String appId = "";
        String secret = "";

        GbDepartmentUserEntity gbDepartmentUser = new GbDepartmentUserEntity();
        gbDepartmentUser.setGbDuDistributerId(disId);
        gbDepartmentUser.setGbDuDepartmentFatherId(depFatherId);
        gbDepartmentUser.setGbDuDepartmentId(depId);
        gbDepartmentUser.setGbDuWxNickName(userName);

        if (admin.equals(getGbDepUserAdminMendianjingli())) {
            appId = myAPPIDConfig.getLiancaiMendianAppId();
            secret = myAPPIDConfig.getLiancaiMendianScreat();
            gbDepartmentUser.setGbDuAdmin(getGbDepUserAdminMendianjingli());
        }

        if (admin.equals(getGbDepUserAdminMendiandinghuoyuan())) {
            appId = myAPPIDConfig.getLiancaiCenterDepartmentAppId();
            secret = myAPPIDConfig.getLiancaiCenterDepartmentScreat();
            gbDepartmentUser.setGbDuAdmin(getGbDepUserAdminMendiandinghuoyuan());
        }
        if (admin.equals(getGbDepUserAdminJicaiyuan())
                || admin.equals(getGbDepUserAdminKufangcaigouyuan())
                || admin.equals(getGbDepUserAdminKitchencaigouyuan())
                || admin.equals(getGbDepUserAdminMendiancaigouyuan())

        ) {
            appId = myAPPIDConfig.getLiancaiCaigouAppId();
            secret = myAPPIDConfig.getLiancaiCaigouScreat();

        }

        if (admin.equals(getGbDepUserAdminKufangguanliyuan())) {
            appId = myAPPIDConfig.getLiancaiKufangAppId();
            secret = myAPPIDConfig.getLiancaiKufangScreat();
            gbDepartmentUser.setGbDuAdmin(getGbDepUserAdminKufangguanliyuan());
        }
        if (admin.equals(getGbDepUserAdminKitchenguanliyuan())) {
            appId = myAPPIDConfig.getShixianZhizuoAppId();
            secret = myAPPIDConfig.getShixianZhizuoScreat();
            gbDepartmentUser.setGbDuAdmin(getGbDepUserAdminKitchenguanliyuan());
        }
        System.out.println("admdmmdmmd" + admin);
        if (admin.equals(getGbDepUserAdminWindowdinghuo())) {
            appId = myAPPIDConfig.getShixianLiliAppId();
            secret = myAPPIDConfig.getShixianLiliScreat();
            gbDepartmentUser.setGbDuAdmin(getGbDepUserAdminWindowdinghuo());
        }

//        if(admin.equals(getGbDepUserAdminPeisongyuan() )){
//            appId = myAPPIDConfig.getShanghuoAppID();
//            secret = myAPPIDConfig.getShanghuoScreat();
//            gbDepartmentUser.setGbDuAdmin(getGbDepUserAdminPeisongyuan());
//
//        }

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId + "&secret=" +
                secret + "&js_code=" + code +
                "&grant_type=authorization_code";

        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println(jsonObject);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openId = jsonObject.get("openid").toString();
        Map<String, Object> map = new HashMap<>();
        map.put("openId", openId);
        map.put("admin", admin);
        GbDepartmentUserEntity depUserEntities = gbDepartmentUserService.queryGroupDepartmentUserByParams(map);
        if (depUserEntities != null) {
            return R.error(-1, "请直接登陆");
        } else {
            //添加新用户
            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            gbDepartmentUser.setGbDuWxAvartraUrl(filePath);
            gbDepartmentUser.setGbDuUrlChange(1);
            gbDepartmentUser.setGbDuWxOpenId(openId);
            gbDepartmentUser.setGbDuJoinDate(formatWhatDay(0));
            gbDepartmentUser.setGbDuCustomerService(0);
            gbDepartmentUser.setGbDuPrintDeviceId("-1");
            gbDepartmentUser.setGbDuPrintBillDeviceId("-1");
            gbDepartmentUser.setGbDuLoginTimes(0);
            gbDepartmentUser.setGbDuAdmin(admin);
            gbDepartmentUserService.save(gbDepartmentUser);
            return R.ok();
        }
    }

    @RequestMapping(value = "/gbJicaiUserLogin/{code}")
    @ResponseBody
    public R gbJicaiUserLogin(@PathVariable String code) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String liancaiKufangAppId = myAPPIDConfig.getLiancaiCaigouAppId();
        String liancaiKufangScreat = myAPPIDConfig.getLiancaiCaigouScreat();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + liancaiKufangAppId + "&secret=" +
                liancaiKufangScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openId = jsonObject.get("openid").toString();
        if (openId != null && !openId.trim().isEmpty()) {
            GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);
            if (depUserEntity != null) {
                Integer gbDepartmentUserId = depUserEntity.getGbDepartmentUserId();
                Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(gbDepartmentUserId);
//
                return R.ok().put("data", stringObjectMap);
            } else {
                return R.error(-1, "请向管理员索要注册邀请");
            }

        } else {
            return R.error(-1, "请向管理员索要注册邀请");
        }
    }


    @RequestMapping(value = "/gbShixianZhizuoUserLogin/{code}")
    @ResponseBody
    public R gbShixianZhizuoUserLogin(@PathVariable String code) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String liancaiKufangAppId = myAPPIDConfig.getShixianZhizuoAppId();
        String liancaiKufangScreat = myAPPIDConfig.getShixianZhizuoScreat();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + liancaiKufangAppId + "&secret=" +
                liancaiKufangScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        System.out.println("str=====>>>>" + str);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println("jsonObject" + jsonObject);

        // 我们需要的openid，在一个小程序中，openid是唯一的updateGbDepUserWithFile
        String openId = jsonObject.get("openid").toString();
        System.out.println("openId------>>>>>>>" + openId);
        if (openId != null) {
            GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);

            if (depUserEntity != null) {
                Integer gbDepartmentUserId = depUserEntity.getGbDepartmentUserId();
                Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(gbDepartmentUserId);

                return R.ok().put("data", stringObjectMap);
            } else {
                return R.error(-1, "请向管理员索要库房注册邀请");
            }

        } else {
            return R.error(-1, "请向管理员索要库房注册邀请");
        }
    }


    @RequestMapping(value = "/gbShixianLiliUserLogin/{code}")
    @ResponseBody
    public R gbShixianLiliUserLogin(@PathVariable String code) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String liancaiKufangAppId = myAPPIDConfig.getShixianLiliAppId();
        String liancaiKufangScreat = myAPPIDConfig.getShixianLiliScreat();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + liancaiKufangAppId + "&secret=" +
                liancaiKufangScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        System.out.println("str=====>>>>" + str);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println("jsonObject" + jsonObject);

        // 我们需要的openid，在一个小程序中，openid是唯一的updateGbDepUserWithFile
        String openId = jsonObject.get("openid").toString();
        System.out.println("openId------>>>>>>>" + openId);
        if (openId != null) {
            GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);
            if (depUserEntity != null) {
                Integer gbDepartmentUserId = depUserEntity.getGbDepartmentUserId();
                Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(gbDepartmentUserId);

                return R.ok().put("data", stringObjectMap);
            } else {
                return R.error(-1, "请向管理员索要库房注册邀请");
            }

        } else {
            return R.error(-1, "请向管理员索要库房注册邀请");
        }
    }

    @RequestMapping(value = "/gbStockRoomUserLogin/{code}")
    @ResponseBody
    public R gbStockRoomUserLogin(@PathVariable String code) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String liancaiKufangAppId = myAPPIDConfig.getLiancaiKufangAppId();
        String liancaiKufangScreat = myAPPIDConfig.getLiancaiKufangScreat();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + liancaiKufangAppId + "&secret=" +
                liancaiKufangScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        System.out.println("str=====>>>>" + str);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println("jsonObject" + jsonObject);

        // 我们需要的openid，在一个小程序中，openid是唯一的updateGbDepUserWithFile
        String openId = jsonObject.get("openid").toString();
        System.out.println("openId------>>>>>>>" + openId);
        if (openId != null) {
            GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);
            if (depUserEntity != null) {
                Integer gbDepartmentUserId = depUserEntity.getGbDepartmentUserId();
                Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(gbDepartmentUserId);
                Integer loginTimes = depUserEntity.getGbDuLoginTimes() + 1;
                depUserEntity.setGbDuLoginTimes(loginTimes);
                gbDepartmentUserService.update(depUserEntity);
                return R.ok().put("data", stringObjectMap);
            } else {
                return R.error(-1, "请向管理员索要库房注册邀请");
            }

        } else {
            return R.error(-1, "请向管理员索要库房注册邀请");
        }
    }

    //    @RequestMapping(value = "/gbKitchenUserLogin/{code}")
//    @ResponseBody
//    public R gbKitchenUserLogin(@PathVariable String code) {
//        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
//        String liancaiKufangAppId = myAPPIDConfig.getShixianLiliAppId();
//        String liancaiKufangScreat = myAPPIDConfig.getShixianLiliScreat();
//
//        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + liancaiKufangAppId + "&secret=" +
//                liancaiKufangScreat + "&js_code=" + code +
//                "&grant_type=authorization_code";
//        // 发送请求，返回Json字符串
//        String str = WeChatUtil.httpRequest(url, "GET", null);
//        System.out.println("str=====>>>>" + str);
//        // 转成Json对象 获取openid
//        JSONObject jsonObject = JSONObject.parseObject(str);
//        System.out.println("jsonObject" + jsonObject);
//
//        // 我们需要的openid，在一个小程序中，openid是唯一的updateGbDepUserWithFile
//        String openId = jsonObject.get("openid").toString();
//        System.out.println("openId------>>>>>>>" + openId);
//        if (openId != null) {
//            GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);
//            if (depUserEntity != null) {
//                Integer gbDepartmentUserId = depUserEntity.getGbDepartmentUserId();
//                Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(gbDepartmentUserId);
//                return R.ok().put("data", stringObjectMap);
//            } else {
//                return R.error(-1, "请向管理员索要库房注册邀请");
//            }
//
//        } else {
//            return R.error(-1, "请向管理员索要库房注册邀请");
//        }
//    }
    @RequestMapping(value = "/gbPeisongUserLogin/{code}")
    @ResponseBody
    public R gbPeisongUserLogin(@PathVariable String code) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String liancaiKufangAppId = myAPPIDConfig.getShanghuoAppID();
        String liancaiKufangScreat = myAPPIDConfig.getShanghuoScreat();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + liancaiKufangAppId + "&secret=" +
                liancaiKufangScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        System.out.println("str=====>>>>" + str);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println("jsonObject" + jsonObject);

        // 我们需要的openid，在一个小程序中，openid是唯一的updateGbDepUserWithFile
        String openId = jsonObject.get("openid").toString();
        System.out.println("openId------>>>>>>>mmmmmmmmmmmmm" + openId);
        if (openId != null) {
            GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);
            if (depUserEntity != null) {
                Integer gbDepartmentUserId = depUserEntity.getGbDepartmentUserId();
                Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(gbDepartmentUserId);

                return R.ok().put("data", stringObjectMap);
            } else {
                return R.error(-1, "请向管理员索要库房注册邀请");
            }

        } else {
            return R.error(-1, "请向管理员索要库房注册邀请");
        }
    }


    @RequestMapping(value = "/updateDepUserAdminGb", method = RequestMethod.POST)
    @ResponseBody
    public R updateDepUserAdminGb(@RequestBody GbDepartmentUserEntity user) {

        gbDepartmentUserService.update(user);
        return R.ok().put("data", user);
    }


    /**
     * @param openId
     * @return
     */
    @RequestMapping(value = "/purchaserGetGroupInfo/{openId}")
    @ResponseBody
    public R purchaserGetGroupInfo(@PathVariable String openId) {
        if (openId != null) {
            List<GbDepartmentEntity> entities = gbDepartmentService.queryMultiGroupInfoGb(openId);
            return R.ok().put("data", entities);
        } else {
            return R.error(-1, "cuowu");
        }
    }


    /**
     * 修改订货用户信息
     *
     * @param userName 订货用户名称
     * @param userId   用户id
     * @param depId    部门id
     * @return ok
     */
    @RequestMapping(value = "/updateGbDepUser", method = RequestMethod.POST)
    @ResponseBody
    public R updateGbDepUser(String userName, Integer userId, Integer depId) {
        GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(userId);
        gbDepartmentUserEntity.setGbDuWxNickName(userName);
        gbDepartmentUserService.update(gbDepartmentUserEntity);

        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(depId);
        gbDepartmentService.update(gbDepartmentEntity);
        Map<String, Object> map = new HashMap<>();
        map.put("userInfo", gbDepartmentUserEntity);
        map.put("depInfo", gbDepartmentEntity);
        return R.ok().put("data", map);
    }


    /**
     * 部门用户修改用户信息
     *
     * @param file     用户头像
     * @param userName 用户名
     * @param userId   用户id
     * @param session  图片
     * @return ok
     */
    @RequestMapping(value = "/updateGbDepUserWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R updateGbDepUserWithFile(@RequestParam("file") MultipartFile file,
                                     @RequestParam("userName") String userName,
                                     @RequestParam("userId") Integer userId,
                                     @RequestParam("depId") Integer depId,
                                     HttpSession session) {
        //1,上传图片
        String newUploadName = "uploadImage";
        String realPath = UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;
        GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(userId);
        gbDepartmentUserEntity.setGbDuWxNickName(userName);
        gbDepartmentUserEntity.setGbDuWxAvartraUrl(filePath);
        gbDepartmentUserEntity.setGbDuUrlChange(1);
        gbDepartmentUserService.update(gbDepartmentUserEntity);

        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(depId);
        gbDepartmentService.update(gbDepartmentEntity);

        return R.ok().put("data", gbDepartmentUserEntity);

    }


    @RequestMapping(value = "/updateJjPurchaserUser", method = RequestMethod.POST)
    @ResponseBody
    public R updateJjPurchaserUser(@RequestBody GbDepartmentUserEntity userEntity) {
        gbDepartmentUserService.update(userEntity);
        return R.ok().put("data", userEntity);
    }

    @RequestMapping(value = "/jjPurchaserUserUpdateWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R jjPurchaserUserUpdateWithFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam("userName") String userName,
                                           @RequestParam("userId") Integer userId,
                                           HttpSession session) {
        //1,上传图片
        String newUploadName = "uploadImage";
        String realPath = UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;
        GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(userId);
        gbDepartmentUserEntity.setGbDuWxNickName(userName);
        gbDepartmentUserEntity.setGbDuWxAvartraUrl(filePath);
        gbDepartmentUserEntity.setGbDuUrlChange(1);
        gbDepartmentUserService.update(gbDepartmentUserEntity);

        return R.ok().put("data", gbDepartmentUserEntity);

    }

    @RequestMapping(value = "/gbPurchaserRegitsteWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R gbPurchaserRegitsteWithFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("userName") String userName,
                                         @RequestParam("code") String code,
                                         @RequestParam("admin") Integer admin,
                                         @RequestParam("depFatherId") Integer depFatherId,
                                         @RequestParam("depId") Integer depId,
                                         @RequestParam("disId") Integer disId,
                                         HttpSession session) {

        System.out.println("ddfafduaufudaffff" + userName);
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongCaigouAppId() +
                "&secret=" + myAPPIDConfig.getTexiansongCaigouScreat() + "&js_code=" + code + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);

        // 转成Json对象 获取openidjrdhUserRegister
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println(jsonObject);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();

        Map<String, Object> map = new HashMap<>();
        map.put("openId", openid);
        map.put("admin", admin);
        GbDepartmentUserEntity depUserEntities = gbDepartmentUserService.queryDepUsersByOpenIdAndAdmin(map);
        NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryWhichUserByOpenId(openid);

        if (depUserEntities == null && nxJrdhUserEntity == null) {

            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            //1 disuser save
            GbDepartmentUserEntity gbDepartmentUserEntity = new GbDepartmentUserEntity();
            gbDepartmentUserEntity.setGbDuUrlChange(1);
            gbDepartmentUserEntity.setGbDuWxNickName(userName);
            gbDepartmentUserEntity.setGbDuWxAvartraUrl(filePath);
            gbDepartmentUserEntity.setGbDuDepartmentId(depId);
            gbDepartmentUserEntity.setGbDuDepartmentFatherId(depFatherId);
            gbDepartmentUserEntity.setGbDuDistributerId(disId);
            gbDepartmentUserEntity.setGbDuAdmin(admin);
            gbDepartmentUserEntity.setGbDuWxOpenId(openid);
            gbDepartmentUserEntity.setGbDuJoinDate(formatWhatDay(0));
            gbDepartmentUserEntity.setGbDuLoginTimes(0);
            gbDepartmentUserEntity.setGbDuPrintDeviceId("-1");
            gbDepartmentUserEntity.setGbDuPrintBillDeviceId("-1");
            gbDepartmentUserService.save(gbDepartmentUserEntity);
            Map<String, Object> mapRe = new HashMap<>();
            mapRe.put("disInfo", gbDistributerService.queryDistributerInfo(disId));
            mapRe.put("depUserInfo", gbDepartmentUserService.queryDepUserByOpenId(openid));
            mapRe.put("depInfo", gbDepartmentService.queryDepInfoGb(depId));
            return R.ok().put("data", mapRe);

        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }

    }


    /**
     * PURCHASE,
     * 采购员登陆
     *
     * @param code 微信用户code
     * @return 用户信息和订货群信息
     */
    @RequestMapping(value = "/mendianUserLogin/{code}")
    @ResponseBody
    public R mendianUserLogin(@PathVariable String code) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String mendianAppId = myAPPIDConfig.getLiancaiMendianAppId();
        String mendianScreat = myAPPIDConfig.getLiancaiMendianScreat();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + mendianAppId + "&secret=" +
                mendianScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openId = jsonObject.get("openid").toString();
        if (openId != null) {
            GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);
            if (depUserEntity != null) {
                Integer gbDepartmentUserId = depUserEntity.getGbDepartmentUserId();
                Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(gbDepartmentUserId);

                return R.ok().put("data", stringObjectMap);
            } else {
                return R.error(-1, "请管理员发送注册邀请");
            }

        } else {
            return R.error(-1, "请管理员发送注册邀请");
        }
    }


    @RequestMapping(value = "/singleMendianUserLogin/{code}")
    @ResponseBody
    public R singleMendianUserLogin(@PathVariable String code) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String mendianAppId = myAPPIDConfig.getSingleMendianAppId();
        String mendianScreat = myAPPIDConfig.getSingleMendianScreat();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + mendianAppId + "&secret=" +
                mendianScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openId = jsonObject.get("openid").toString();
        if (openId != null) {
            GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);
            if (depUserEntity != null) {
                Integer gbDepartmentUserId = depUserEntity.getGbDepartmentUserId();
                Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(gbDepartmentUserId);

                return R.ok().put("data", stringObjectMap);
            } else {
                return R.error(-1, "请管理员发送注册邀请");
            }
        } else {
            return R.error(-1, "请管理员发送注册邀请");
        }
    }


    /**
     * ORDER
     * 订货用户注册
     *
     * @param gbDepartmentUser 订货用户
     * @return 用户id
     */
    @ResponseBody
    @RequestMapping("/depOrderUserSaveGb")
    public R depOrderUserSaveGb(@RequestBody GbDepartmentUserEntity gbDepartmentUser) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String orderAppID = myAPPIDConfig.getLiancaiCenterDepartmentAppId();
        String orderScreat = myAPPIDConfig.getLiancaiCenterDepartmentScreat();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + orderAppID + "&secret=" + orderScreat + "&js_code=" + gbDepartmentUser.getGbDuCode() + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();

        GbDepartmentUserEntity depUserEntities = gbDepartmentUserService.queryDepUserByOpenId(openid);
        if (depUserEntities != null) {
            return R.error(-1, "请直接登陆");

        } else {
            //添加新用户
            gbDepartmentUser.setGbDuWxOpenId(openid);
            gbDepartmentUser.setGbDuJoinDate(formatWhatDay(0));
            gbDepartmentUser.setGbDuCustomerService(0);
            gbDepartmentUser.setGbDuPrintDeviceId("-1");
            gbDepartmentUser.setGbDuLoginTimes(0);
            gbDepartmentUser.setGbDuPrintBillDeviceId("-1");
            gbDepartmentUserService.save(gbDepartmentUser);
            Integer gbDepartmentUserId = gbDepartmentUser.getGbDepartmentUserId();
            Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(gbDepartmentUserId);

            return R.ok().put("data", stringObjectMap);
        }
    }


//    public String getAccessToken() {
//        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
//
//        String guanliduanAppId = myAPPIDConfig.getLiansuocaigouguanliduanAppId();
//        String guanliduanScreat = myAPPIDConfig.getLiansuocaigouguanliduanScreat();
//        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + guanliduanAppId + "&secret=" +
//                guanliduanScreat;
//        String str = WeChatUtil.httpRequest(url, "GET", null);
//
//        System.out.println(str);
//        JSONObject jsonObject = JSONObject.parseObject(str);
//        String token = jsonObject.get("access_token").toString();
//        System.out.println(token);
//
//        return token;
//    }
//
//    @RequestMapping("/getCode")
//    @ResponseBody
//    public R getQrCode(String param1, String page, HttpServletResponse response,HttpSession session) {
//
//        String accessToken = getAccessToken();
//        RestTemplate rest = new RestTemplate();
//        InputStream inputStream = null;
//        OutputStream outputStream = null;
//        try {
//            String url = "https://api.weixin.qq.com/wxa/getwxacode?access_token=" + accessToken;
//            Map<String, Object> param = new HashMap<>();
//            param.put("path", "pages/index/index") ; //param.put("page", "pages/index/index"); //无限
////            param.put("width", 450);
//            //自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
////            param.put("autoColor", false);
//            //是否需要透明底色， is_hyaline 为true时，生成透明底色的小程序码
////            param.put("isHyaline", false);
////            param.put("lineColor", line_color);
//            System.out.println("parnmm:" + param);
//            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
//            HttpEntity requestEntity = new HttpEntity(JSON.toJSONString(param), headers);
//            ResponseEntity<byte[]> entity = rest.exchange(url, HttpMethod.POST, requestEntity, byte[].class, new Object[0]);
//            System.out.println("调用小程序生成微信永久小程序码URL接口返回结果entity:" + entity.getBody());
//            byte[] result = entity.getBody();
//            inputStream = new ByteArrayInputStream(result);
//
//            ServletContext servletContext = session.getServletContext();
//            String realPath = servletContext.getRealPath("/uploadImage");
//            File file = new File(realPath + "/hsk3.png");
//            if (!file.exists()){
//                file.createNewFile();
//            }
//            outputStream = new FileOutputStream(file);
//
//
//            int len = 0;
//            byte[] buf = new byte[1024];
//            while ((len = inputStream.read(buf, 0, 1024)) != -1) {
//                outputStream.write(buf, 0, len);
//            }
//            outputStream.flush();
//
//            return R.ok().put("data", file);
//
//
//        } catch (Exception e) {
//            System.out.println("调用小程序生成微信永久小程序码URL接口返回结果L接口异常," + e);
//        }finally {
//            if(inputStream != null){
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if(outputStream != null){
//                try {
//                    outputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        return null;
//    }
//    @RequestMapping("/getCode2")
//    @ResponseBody
//    public ResponseEntity getQrCode2(String param1, String page, HttpServletResponse response,HttpSession session) {
//
//        String accessToken = getAccessToken();
//        RestTemplate rest = new RestTemplate();
//        InputStream inputStream = null;
//        OutputStream outputStream = null;
//        try {
//            String url = "https://api.weixin.qq.com/wxa/getwxacode?access_token=" + accessToken;
//            Map<String, Object> param = new HashMap<>();
//            param.put("path", "pages/index/index") ; //param.put("page", "pages/index/index"); //无限
////            param.put("width", 450);
//            //自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
////            param.put("autoColor", false);
//            //是否需要透明底色， is_hyaline 为true时，生成透明底色的小程序码
////            param.put("isHyaline", false);
////            param.put("lineColor", line_color);
//            System.out.println("parnmm:" + param);
//            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
//            HttpEntity requestEntity = new HttpEntity(JSON.toJSONString(param), headers);
//            ResponseEntity<byte[]> entity = rest.exchange(url, HttpMethod.POST, requestEntity, byte[].class, new Object[0]);
//            System.out.println("调用小程序生成微信永久小程序码URL接口返回结果entity:" + entity.getBody());
//            byte[] result = entity.getBody();
//            inputStream = new ByteArrayInputStream(result);
//
//            ServletContext servletContext = session.getServletContext();
//            String realPath = servletContext.getRealPath("/uploadImage");
//            File file = new File(realPath + "/hsk3.png");
//            if (!file.exists()){
//                file.createNewFile();
//            }
//            outputStream = new FileOutputStream(file);
//
//            int len = 0;
//            byte[] buf = new byte[1024];
//            while ((len = inputStream.read(buf, 0, 1024)) != -1) {
//                outputStream.write(buf, 0, len);
//            }
//            outputStream.flush();
//            //3,创建相应头
//            HttpHeaders headers_1 = new HttpHeaders();
//            headers_1.setContentDispositionFormData("attachment","下载.png");
//            //application/octet-stream:二进制流数据(最常见的文件下载方式)
//            headers_1.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//            ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(org.apache.commons.io.FileUtils.readFileToByteArray(file),headers_1, HttpStatus.OK);
//            System.out.println(responseEntity);
//            return responseEntity;
//
//
//        } catch (Exception e) {
//            System.out.println("调用小程序生成微信永久小程序码URL接口返回结果L接口异常," + e);
//        }finally {
//            if(inputStream != null){
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if(outputStream != null){
//                try {
//                    outputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        return null;
//    }
//    @RequestMapping("/getCode1")
//    @ResponseBody
//    public void smallProgramCode1(String param, String page, HttpServletResponse response) {
//        OutputStream stream = null;
//        try {
//            //获取AccessToken
//            String accessToken = getAccessToken();
//            //设置响应类型
//            response.setContentType("image/png");
//            String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken;
//            //组装参数
//            Map<String, Object> paraMap = new HashMap<>();
//            //二维码携带参数 不超过32位 参数类型必须是字符串
//            paraMap.put("scene", param);
//            //二维码跳转页面
//            paraMap.put("page", page);
//            //二维码的宽度
//            paraMap.put("width", 450);
//            //自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
//            paraMap.put("auto_color", false);
//            //是否需要透明底色， is_hyaline 为true时，生成透明底色的小程序码
//            paraMap.put("is_hyaline", false);
//            //执行post 获取数据流
//            byte[] result = WxUtil.doImgPost(url, paraMap);
//            //输出图片到页面
//            response.setContentType("image/png");
//            try {
//                stream = response.getOutputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            stream.write(result);
//            stream.flush();
//
//            stream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * ORDER
     * 部门用户登陆
     *
     * @param code 微信code
     * @return 用户和部门信息
     */
    @RequestMapping(value = "/depUserLoginGb/{code}")
    @ResponseBody
    public R depUserLoginGb(@PathVariable String code) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String orderAppID = myAPPIDConfig.getJingjingOrderAppID();
        String orderScreat = myAPPIDConfig.getJingjingOrderScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + orderAppID + "&secret=" +
                orderScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        String str = WeChatUtil.httpRequest(url, "GET", null);
        JSONObject jsonObject = JSONObject.parseObject(str);
        String openId = jsonObject.get("openid").toString();
        GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);
        if (depUserEntity != null) {
            Integer gbDepartmentUserId = depUserEntity.getGbDepartmentUserId();
            Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(gbDepartmentUserId);
            System.out.println();
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
        }
    }

    @RequestMapping(value = "/depUserLoginGbSx/{code}")
    @ResponseBody
    public R depUserLoginGbSx(@PathVariable String code) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String orderAppID = myAPPIDConfig.getLiancaiCenterDepartmentAppId();
        String orderScreat = myAPPIDConfig.getLiancaiCenterDepartmentScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + orderAppID + "&secret=" +
                orderScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        System.out.println("url" + url);
        String str = WeChatUtil.httpRequest(url, "GET", null);
        System.out.println("str" + str);
        JSONObject jsonObject = JSONObject.parseObject(str);
        String openId = jsonObject.get("openid").toString();
        GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);
        if (depUserEntity != null) {
            Integer gbDepartmentUserId = depUserEntity.getGbDepartmentUserId();
            Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(gbDepartmentUserId);
            depUserEntity.setGbDuLoginTimes(depUserEntity.getGbDuLoginTimes() + 1);
            gbDepartmentUserService.update(depUserEntity);
            System.out.println();
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
        }
    }

    /**
     * ORDER
     * 订货首页获取用户和部门信息
     *
     * @param userid 用户id
     * @return 用户和部门信息
     */
    @RequestMapping(value = "/getDepAndUserInfoGb/{userid}")
    @ResponseBody
    public R getDepAndUserInfoGb(@PathVariable Integer userid) {
        Map<String, Object> stringObjectMap = gbDepartmentService.queryDepAndUserInfoGb(userid);
        return R.ok().put("data", stringObjectMap);
    }


    /**
     * ORDER
     * 获取群用户部门和用户信息
     *
     * @param userId 群用户id
     * @return 部门用户
     */
    @RequestMapping(value = "/getDepUserInfoGb/{userId}")
    @ResponseBody
    public R getDepUserInfoGb(@PathVariable Integer userId) {
        System.out.println(userId + "idididiididi");
//        GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(userId);
        GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryDepUserInfoGb(userId);
        return R.ok().put("data", gbDepartmentUserEntity);
    }

    @RequestMapping(value = "/updateGroupPurchaseWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R updateGroupPurchaseWithFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("userName") String userName,
                                         @RequestParam("groupName") String groupName,
                                         @RequestParam("userId") Integer userId,
                                         @RequestParam("depId") Integer depId,
                                         HttpSession session) {
        //1,上传图片
        String newUploadName = "uploadImage";
        String realPath = UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;
        GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(userId);
        gbDepartmentUserEntity.setGbDuWxNickName(userName);
        gbDepartmentUserEntity.setGbDuWxAvartraUrl(filePath);
        gbDepartmentUserEntity.setGbDuUrlChange(1);
        gbDepartmentUserService.update(gbDepartmentUserEntity);

        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(depId);
        gbDepartmentEntity.setGbDepartmentName(groupName);
        gbDepartmentService.update(gbDepartmentEntity);


        return R.ok();

    }


    /**
     * PURCHASE,
     * 修改群信息
     *
     * @param userName  用户名（没用）
     * @param groupName 群名称
     * @param userId    用户id
     * @param depId     群id
     * @return ok
     */
    @RequestMapping(value = "/updateGroupPurchase", method = RequestMethod.POST)
    @ResponseBody
    public R updateGroupPurchase(String userName, String groupName, Integer userId, Integer depId) {
        GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(userId);
        gbDepartmentUserEntity.setGbDuWxNickName(userName);
        gbDepartmentUserService.update(gbDepartmentUserEntity);

        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(depId);
        gbDepartmentEntity.setGbDepartmentName(groupName);
        gbDepartmentService.update(gbDepartmentEntity);
        return R.ok();
    }

    @RequestMapping(value = "/getDepUsersByDepIdGb")
    @ResponseBody
    public R getDepUsersByDepIdGb(Integer depId, Integer admin) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("admin", admin);
        System.out.println("");
        List<GbDepartmentUserEntity> userEntities = gbDepartmentUserService.queryDepUsersByDepIdAndAdmin(map);
        return R.ok().put("data", userEntities);
    }



    /**
     * PURCHASE
     * 删除群用户
     *
     * @param userId 用户id
     * @return ok
     */
    @RequestMapping(value = "/deleteDepUser/{userId}")
    @ResponseBody
    public R deleteDepUser(@PathVariable Integer userId) {
        GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(userId);
        Integer gbDuAdmin = gbDepartmentUserEntity.getGbDuAdmin();
        //删除采购员
        if (gbDuAdmin.equals(getGbDepUserAdminMendiancaigouyuan()) ||
                gbDuAdmin.equals(getGbDepUserAdminJicaiyuan()) ||
                gbDuAdmin.equals(getGbDepUserAdminKufangcaigouyuan()) ||
                gbDuAdmin.equals(getGbDepUserAdminKitchencaigouyuan())
        ) {
            Map<String, Object> map = new HashMap<>();
            map.put("purUserId", userId);
            map.put("status", 4);
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
            if (ordersEntities.size() > 0) {
                return R.error(-1, "还有未完成采购，不能删除");
            } else {
//                gbDepartmentUserEntity.setGbDuDepartmentId();
//                gbDepartmentUserEntity.setGbDuDepartmentFatherId(-1);
//                gbDepartmentUserEntity.setGbDuDistributerId(-1);
//                gbDepartmentUserEntity.setGbDuAdmin(-1);
                gbDepartmentUserEntity.setGbDuWxOpenId("-1");
                gbDepartmentUserService.update(gbDepartmentUserEntity);
                return R.ok();
            }
        } else if (gbDuAdmin.equals(getGbDepUserAdminMendiandinghuoyuan())) {
            //删除订货员
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("status", 4);
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
            if (ordersEntities.size() > 0) {
                return R.error(-1, "还有未完成订货，不能删除");
            } else {
//                gbDepartmentUserEntity.setGbDuDepartmentId(-1);
//                gbDepartmentUserEntity.setGbDuDepartmentFatherId(-1);
//                gbDepartmentUserEntity.setGbDuDistributerId(-1);
//                gbDepartmentUserEntity.setGbDuAdmin(-1);
                gbDepartmentUserEntity.setGbDuWxOpenId("-1");
                gbDepartmentUserService.update(gbDepartmentUserEntity);
                return R.ok();
            }

        } else if (gbDuAdmin.equals(getGbDepUserAdminKufangguanliyuan())) {
            //删除订货员
            Map<String, Object> map = new HashMap<>();
            map.put("pickerUserId", userId);
            map.put("status", 4);
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
            if (ordersEntities.size() > 0) {
                return R.error(-1, "还有未完成订货，不能删除");
            } else {
//                gbDepartmentUserEntity.setGbDuDepartmentId(-1);
//                gbDepartmentUserEntity.setGbDuDepartmentFatherId(-1);
//                gbDepartmentUserEntity.setGbDuDistributerId(-1);
//                gbDepartmentUserEntity.setGbDuAdmin(-1);
                gbDepartmentUserEntity.setGbDuWxOpenId("-1");
                gbDepartmentUserService.update(gbDepartmentUserEntity);
                return R.ok();
            }

        } else {
            gbDepartmentUserEntity.setGbDuDepartmentId(-1);
            gbDepartmentUserEntity.setGbDuDepartmentFatherId(-1);
            gbDepartmentUserEntity.setGbDuDistributerId(-1);
            gbDepartmentUserEntity.setGbDuAdmin(-1);
            gbDepartmentUserEntity.setGbDuWxOpenId("-1");
            gbDepartmentUserService.update(gbDepartmentUserEntity);
            return R.ok();
        }
    }


}
