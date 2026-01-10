package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-16 11:26
 */

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.getWeek;
import static com.nongxinle.utils.GbTypeUtils.getGbPurchaseGoodsStatusProcurement;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisPurchaseGoodsUnBuy;


@RestController
@RequestMapping("api/nxdepartmentuser")
public class NxDepartmentUserController {
    @Autowired
    private NxDepartmentUserService nxDepartmentUserService;

    @Autowired
    private NxDepartmentService nxDepartmentService;

    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDPBService;

    @Autowired
    private NxDistributerPurchaseGoodsService nxDistributerPurchaseGoodsService;
    @Autowired
    private NxJrdhUserService nxJrdhUserService;
    @Autowired
    private NxJrdhSupplierService jrdhSupplierService;
    @Autowired
    private NxGoodsService nxGoodsService;


    @RequestMapping(value = "/machineGetUser/{id}")
    @ResponseBody
    public R machineGetUser(@PathVariable String  id) {
        NxDepartmentUserEntity  userEntity =  nxDepartmentUserService.queryDepUserByMachineId(id);
        if(userEntity != null){
            userEntity.setNxDuLoginCode("-1");
            nxDepartmentUserService.update(userEntity);
            Integer nxDepartmentUserId = userEntity.getNxDepartmentUserId();
            Map<String, Object> stringObjectMap = nxDepartmentService.queryDepAndUserInfo(nxDepartmentUserId);
            return R.ok().put("data", stringObjectMap);
        }else{
            return R.error(-1,"no");
        }

    }



    @RequestMapping(value = "/depUserLoginDaoDuDesk", method = RequestMethod.POST)
    @ResponseBody
    public R depUserLoginDaoDuDesk(String code, String machineId) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String orderAppID = myAPPIDConfig.getLaoDuDinghuoAppID();
        String orderScreat = myAPPIDConfig.getLaoDuDinghuoScreat();
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
        NxDepartmentUserEntity depUserEntity = nxDepartmentUserService.queryDepUserByOpenId(openId);
        if (depUserEntity != null) {
            depUserEntity.setNxDuLoginCode(machineId);
            nxDepartmentUserService.update(depUserEntity);
            Integer nxDepartmentUserId = depUserEntity.getNxDepartmentUserId();
            Map<String, Object> stringObjectMap = nxDepartmentService.queryDepAndUserInfo(nxDepartmentUserId);
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
        }
    }

//
//    @RequestMapping(value = "/Q8bKzs8V1p.txt")
//    @ResponseBody
//    public String nxDepDeskUserLoginLaoDuDesk( ) {
//        return "7d7b94b7c94c3153c8b88c46c9d0f647";
//    }

    @RequestMapping(value = "/Q8bKzs8V1p.txt")
    @ResponseBody
    public String nxDepDeskUserLoginLaoDu( ) {
        return "7d7b94b7c94c3153c8b88c46c9d0f647";
    }

    @RequestMapping(value = "/updateDepUserAdmin", method = RequestMethod.POST)
    @ResponseBody
    public R updateDepUserAdmin(@RequestBody NxDepartmentUserEntity user) {

        nxDepartmentUserService.update(user);
        return R.ok();
    }


    /**
     *
     * @param openId
     * @return
     */
    @RequestMapping(value = "/purchaserGetGroupInfo/{openId}")
    @ResponseBody
    public R purchaserGetGroupInfo(@PathVariable String openId) {
        if (openId != null) {
            List<NxDepartmentEntity> entities = nxDepartmentService.queryMultiGroupInfo(openId);
            return R.ok().put("data", entities);
        } else {
            return R.error(-1, "cuowu");
        }
    }


    /**
     * 修改订货用户信息
     * @param userName 订货用户名称
     * @param userId 用户id
     * @return ok
     */
    @RequestMapping(value = "/updateDepUser", method = RequestMethod.POST)
    @ResponseBody
    public R updateDepUser(String userName, Integer userId) {
        NxDepartmentUserEntity nxDepartmentUserEntity = nxDepartmentUserService.queryObject(userId);
        nxDepartmentUserEntity.setNxDuWxNickName(userName);
        nxDepartmentUserService.update(nxDepartmentUserEntity);
        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(nxDepartmentUserEntity.getNxDuDepartmentId());

        Map<String, Object> map = new HashMap<>();
        map.put("userInfo", nxDepartmentUserEntity);
        map.put("depInfo", departmentEntity);
        return R.ok().put("data", map);
    }


    /**
     * 部门用户修改用户信息
     * @param file 用户头像
     * @param userName 用户名
     * @param userId 用户id
     * @param session 图片
     * @return ok
     */
    @RequestMapping(value = "/updateDepUserWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R updateDepUserWithFile(@RequestParam("file") MultipartFile file,
                                   @RequestParam("userName") String userName,
                                   @RequestParam("userId") Integer userId,
                                   HttpSession session) {


        //1,上传图片
        String newUploadName = "uploadImage";
        String realPath = UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;
        NxDepartmentUserEntity nxDepartmentUserEntity = nxDepartmentUserService.queryObject(userId);
        nxDepartmentUserEntity.setNxDuWxNickName(userName);
        nxDepartmentUserEntity.setNxDuWxAvartraUrl(filePath);
        nxDepartmentUserEntity.setNxDuUrlChange(1);
        nxDepartmentUserService.update(nxDepartmentUserEntity);

        return R.ok().put("data", nxDepartmentUserEntity);

    }

    /**
     * PURCHASE,
     * 采购员登陆
     * @param code 微信用户code
     * @return 用户信息和订货群信息
     */
    @RequestMapping(value = "/purchaserUserLogin/{code}")
    @ResponseBody
    public R purchaserUserLogin(@PathVariable String code) {
        System.out.println(" purchaserUserLogin----------" + code);

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String purchaseAppID = myAPPIDConfig.getPurchaseAppID();
        String purchaseScreat = myAPPIDConfig.getPurchaseScreat();


        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + purchaseAppID + "&secret=" +
                purchaseScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        System.out.println("str=====>>>>" + str);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println("jsonObject" + jsonObject);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openId = jsonObject.get("openid").toString();
        System.out.println("openId------>>>>>>>" + openId);
        if (openId != null) {
            NxDepartmentUserEntity depUserEntity = nxDepartmentUserService.queryDepUserByOpenId(openId);
            if (depUserEntity != null) {
                Integer nxDepartmentUserId = depUserEntity.getNxDepartmentUserId();
                Map<String, Object> stringObjectMap = nxDepartmentService.queryDepAndUserInfo(nxDepartmentUserId);

//			List<NxDepartmentEntity> departmentEntities = nxDepartmentService.queryGroupInfo(openId);
                return R.ok().put("data", stringObjectMap);
            } else {
                return R.error(-1, "请索要批发商二维码进行注册");
            }

        } else {
            return R.error(-1, "请索要批发商二维码进行注册");
        }
    }


    /**
     * 订货群采购员注册
     * @param nxDepartmentUser 订货群采购员用户
     * @return 用户信息和订货群信息
     */
    @RequestMapping(value = "/depPurchaseUserSave", method = RequestMethod.POST)
    @ResponseBody
    public R depPurchaseUserSave(@RequestBody NxDepartmentUserEntity nxDepartmentUser) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String purchaseAppID = myAPPIDConfig.getPurchaseAppID();
        String purchaseScreat = myAPPIDConfig.getPurchaseScreat();


        String code = nxDepartmentUser.getNxDuCode();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + purchaseAppID + "&secret=" +
                purchaseScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openId = jsonObject.get("openid").toString();
        NxDepartmentUserEntity depUserEntities = nxDepartmentUserService.queryDepUserByOpenId(openId);
        if (depUserEntities != null) {
            return R.error(-1, "请直接登陆");

        } else {
            //添加新用户
            nxDepartmentUser.setNxDuWxOpenId(openId);
            nxDepartmentUser.setNxDuJoinDate(formatWhatDay(0));
            nxDepartmentUser.setNxDuLoginTimes(0);
            nxDepartmentUserService.save(nxDepartmentUser);
            Integer nxDepartmentUserId = nxDepartmentUser.getNxDepartmentUserId();
            //todo
//		  List<NxDepartmentEntity> departmentEntities = nxDepartmentService.queryGroupInfo(openId);
            Map<String, Object> stringObjectMap = nxDepartmentService.queryDepAndUserInfo(nxDepartmentUserId);

            return R.ok().put("data", stringObjectMap);
        }
    }

    /**
     * ORDER
     * 订货用户注册
     * @param nxDepartmentUser 订货用户
     * @return 用户id
     */
    @ResponseBody
    @RequestMapping("/depOrderUserSave")
    public R depOrderUserSave(@RequestBody NxDepartmentUserEntity nxDepartmentUser) {


        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String orderAppID = myAPPIDConfig.getShixianLiliAppId();
        String orderScreat = myAPPIDConfig.getShixianLiliScreat();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + orderAppID + "&secret=" + orderScreat + "&js_code=" + nxDepartmentUser.getNxDuCode() + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();

        NxDepartmentUserEntity depUserEntities = nxDepartmentUserService.queryDepUserByOpenId(openid);
        if (depUserEntities != null) {
            return R.error(-1, "请直接登陆");

        } else {
            //添加新用户

            //第一个用户默认是isAdmin
            List<NxDepartmentUserEntity> userEntities = nxDepartmentUserService.queryAllUsersByDepId(nxDepartmentUser.getNxDuDepartmentId());
            if (userEntities.size() == 0) {
                nxDepartmentUser.setNxDuAdmin(1);
            }

            nxDepartmentUser.setNxDuWxOpenId(openid);
            nxDepartmentUser.setNxDuJoinDate(formatWhatDay(0));
            nxDepartmentUser.setNxDuLoginTimes(0);
            nxDepartmentUserService.save(nxDepartmentUser);
            Integer nxDepartmentUserId = nxDepartmentUser.getNxDepartmentUserId();
            return R.ok().put("data", nxDepartmentUserId);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/depOrderUserSaveWithFile", produces = "text/html;charset=UTF-8")
    public R depOrderUserSaveWithFile(@RequestParam("file") MultipartFile file,
                                      @RequestParam("userName") String userName,
                                      @RequestParam("disId") Integer disId,
                                      @RequestParam("depFatherId") Integer depFatherId,
                                      @RequestParam("depId") Integer depId,
                                      @RequestParam("code") String code,
                                      HttpSession session) {

        System.out.println("aaa" + disId + userName + depFatherId + depId + code);
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        String orderAppID = myAPPIDConfig.getJingjingOrderAppID();
        String orderScreat = myAPPIDConfig.getJingjingOrderScreat();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + orderAppID + "&secret=" + orderScreat + "&js_code=" + code + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        System.out.println("opeidiididid" + openid);
        NxDepartmentUserEntity depUserEntities = nxDepartmentUserService.queryDepUserByOpenId(openid);
        if (depUserEntities != null) {
            return R.error(-1, "请直接登陆");
        } else {
            //第一个用户默认是isAdmin
            NxDepartmentUserEntity nxDepartmentUser = new NxDepartmentUserEntity();
            if (depFatherId == -1) {
                NxDepartmentEntity entity = new NxDepartmentEntity();
                entity.setNxDepartmentName(userName);
                entity.setNxDepartmentAttrName(userName);
                entity.setNxDepartmentOrderCode(userName);
                entity.setNxDepartmentRecordMinutes(30);
                entity.setNxDepartmentDisId(disId);
                entity.setNxDepartmentIsGroupDep(1);
                entity.setNxDepartmentWorkingStatus(0);
                entity.setNxDepartmentSubAmount(0);
                entity.setNxDepartmentFatherId(depId);
                entity.setNxDepartmentSettleType(0);
                nxDepartmentService.saveJustDepartment(entity);

//                savePromotion(entity);
                depId = entity.getNxDepartmentId();
                nxDepartmentUser.setNxDuAdmin(1);
            } else {
                List<NxDepartmentUserEntity> userEntities = nxDepartmentUserService.queryAllUsersByDepId(depId);
                if (userEntities.size() == 0) {
                    nxDepartmentUser.setNxDuAdmin(1);
                } else {
                    nxDepartmentUser.setNxDuAdmin(0);
                }
            }

            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            nxDepartmentUser.setNxDuWxAvartraUrl(filePath);
            nxDepartmentUser.setNxDuWxNickName(userName);
            nxDepartmentUser.setNxDuUrlChange(1);
            nxDepartmentUser.setNxDuDepartmentId(depId);
            nxDepartmentUser.setNxDuDepartmentFatherId(depFatherId);
            nxDepartmentUser.setNxDuDistributerId(disId);
            nxDepartmentUser.setNxDuWxOpenId(openid);
            nxDepartmentUser.setNxDuJoinDate(formatWhatDay(0));
            nxDepartmentUser.setNxDuLoginTimes(0);
            nxDepartmentUserService.save(nxDepartmentUser);


            return R.ok();
        }
    }




    @ResponseBody
    @RequestMapping(value = "/depOrderUserSaveWithFileLaodu", produces = "text/html;charset=UTF-8")
    public R depOrderUserSaveWithFileLaodu(@RequestParam("file") MultipartFile file,
                                      @RequestParam("userName") String userName,
                                      @RequestParam("disId") Integer disId,
                                      @RequestParam("depFatherId") Integer depFatherId,
                                      @RequestParam("depId") Integer depId,
                                      @RequestParam("code") String code,
                                           @RequestParam("admin") Integer admin,
                                      HttpSession session) {

        System.out.println("aaa" + disId + userName + depFatherId + depId + code);
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        String orderAppID = myAPPIDConfig.getLaoDuDinghuoAppID();
        String orderScreat = myAPPIDConfig.getLaoDuDinghuoScreat();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + orderAppID + "&secret=" + orderScreat + "&js_code=" + code + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        System.out.println("jsonObjectjsonObject" + jsonObject);
        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        System.out.println("opeidiididid" + openid);
        NxDepartmentUserEntity depUserEntities = nxDepartmentUserService.queryDepUserByOpenId(openid);
        if (depUserEntities != null) {
            return R.error(-1, "请直接登陆");
        } else {
            //第一个用户默认是isAdmin
            NxDepartmentUserEntity nxDepartmentUser = new NxDepartmentUserEntity();

//            if (depFatherId == -1) {
//                NxDepartmentEntity entity = new NxDepartmentEntity();
//                entity.setNxDepartmentName(userName);
//                entity.setNxDepartmentAttrName(userName);
//                entity.setNxDepartmentDisId(disId);
//                entity.setNxDepartmentIsGroupDep(1);
//                entity.setNxDepartmentWorkingStatus(0);
//                entity.setNxDepartmentSubAmount(0);
//                entity.setNxDepartmentFatherId(0);
//                entity.setNxDepartmentSettleType(0);
//                entity.setNxDepartmentPromotionGoodsId(651);
//                System.out.println("ddafadaveeeee" + entity);
//                nxDepartmentService.saveJustDepartment(entity);

//                savePromotion(entity);
//                depId = entity.getNxDepartmentId();
                nxDepartmentUser.setNxDuAdmin(admin);


            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            nxDepartmentUser.setNxDuWxAvartraUrl(filePath);
            nxDepartmentUser.setNxDuWxNickName(userName);
            nxDepartmentUser.setNxDuUrlChange(1);
            nxDepartmentUser.setNxDuDepartmentId(depId);
            nxDepartmentUser.setNxDuDepartmentFatherId(depFatherId);
            nxDepartmentUser.setNxDuDistributerId(disId);
            nxDepartmentUser.setNxDuWxOpenId(openid);
            nxDepartmentUser.setNxDuJoinDate(formatWhatDay(0));
            nxDepartmentUser.setNxDuLoginTimes(0);
            nxDepartmentUserService.save(nxDepartmentUser);


            return R.ok();
        }
    }

    /**
     * ORDER
     * 部门用户登陆
     * @param code 微信code
     * @return 用户和部门信息
     */
    @RequestMapping(value = "/depUserLogin/{code}")
    @ResponseBody
    public R depUserLogin(@PathVariable String code) {

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
        NxDepartmentUserEntity depUserEntity = nxDepartmentUserService.queryDepUserByOpenId(openId);
        if (depUserEntity != null) {
            Integer nxDepartmentUserId = depUserEntity.getNxDepartmentUserId();
            Map<String, Object> stringObjectMap = nxDepartmentService.queryDepAndUserInfo(nxDepartmentUserId);
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
        }
    }



    @RequestMapping(value = "/depUserLoginDaoDu/{code}")
    @ResponseBody
    public R depUserLoginDaoDu(@PathVariable String code) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String orderAppID = myAPPIDConfig.getLaoDuDinghuoAppID();
        String orderScreat = myAPPIDConfig.getLaoDuDinghuoScreat();
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
        NxDepartmentUserEntity depUserEntity = nxDepartmentUserService.queryDepUserByOpenId(openId);
        if (depUserEntity != null) {
            Integer nxDepartmentUserId = depUserEntity.getNxDepartmentUserId();
            Map<String, Object> stringObjectMap = nxDepartmentService.queryDepAndUserInfoAll(nxDepartmentUserId);

            System.out.println("queryDepAndUserInfoAllqueryDepAndUserInfoAllqueryDepAndUserInfoAll");
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
        }
    }


//    private void savePromotion(NxDepartmentEntity departmentEntity) {
//        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(departmentEntity.getNxDepartmentPromotionGoodsId());
//
//        System.out.println("diidididi" + departmentEntity.getNxDepartmentId());
//
//        NxDepartmentOrdersEntity ordersEntity = new NxDepartmentOrdersEntity();
//        ordersEntity.setNxDoDepartmentId(departmentEntity.getNxDepartmentId());
//        ordersEntity.setNxDoDepartmentFatherId(departmentEntity.getNxDepartmentId());
//        ordersEntity.setNxDoArriveOnlyDate(formatWhatDate(0));
//        ordersEntity.setNxDoDistributerId(departmentEntity.getNxDepartmentDisId());
//        ordersEntity.setNxDoDisGoodsId(departmentEntity.getNxDepartmentPromotionGoodsId());
//
//
//        ordersEntity.setNxDoQuantity("5");
//        ordersEntity.setNxDoPrice("0");
//        ordersEntity.setNxDoSubtotal("0");
//        ordersEntity.setNxDoStatus(0);
//        ordersEntity.setNxDoWeight("5");
//
//
//        ordersEntity.setNxDoNxGoodsId(distributerGoodsEntity.getNxDgNxGoodsId());
//        ordersEntity.setNxDoNxGoodsFatherId(distributerGoodsEntity.getNxDgNxFatherId());
//        ordersEntity.setNxDoProfitSubtotal("0");
//        ordersEntity.setNxDoPurchaseStatus(1);
//        ordersEntity.setNxDoPurchaseGoodsId(distributerGoodsEntity.getNxDgPurchaseAuto());
//        ordersEntity.setNxDoCostPrice(distributerGoodsEntity.getNxDgBuyingPrice());
//        ordersEntity.setNxDoCostPriceUpdate(distributerGoodsEntity.getNxDgBuyingPriceUpdate());
//        BigDecimal decimal = new BigDecimal(distributerGoodsEntity.getNxDgBuyingPrice());
//        BigDecimal decimal1 = new BigDecimal(ordersEntity.getNxDoWeight());
//        BigDecimal decimal2 = decimal1.multiply(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
//        ordersEntity.setNxDoCostSubtotal(decimal2.toString());
//        ordersEntity.setNxDoProfitSubtotal("-" + decimal2.toString());
//        ordersEntity.setNxDoProfitScale("0");
//        ordersEntity.setNxDoDisGoodsGrandId(distributerGoodsEntity.getNxDgDfgGoodsGrandId());
//        ordersEntity.setNxDoTodayOrder(1);
//        ordersEntity.setNxDoIsAgent(1);
//        ordersEntity.setNxDoPrintStandard(distributerGoodsEntity.getNxDgGoodsStandardname());
//        ordersEntity.setNxDoGoodsType(distributerGoodsEntity.getNxDgPurchaseAuto());
//
//
//
//        ordersEntity.setNxDoStandard(distributerGoodsEntity.getNxDgGoodsStandardname());
//        ordersEntity.setNxDoApplyDate(formatWhatDay(0));
//        ordersEntity.setNxDoArriveOnlyDate(formatWhatDate(0));
//        ordersEntity.setNxDoArriveWeeksYear(getWeekOfYear(0));
//        ordersEntity.setNxDoApplyFullTime(formatFullTime());
//        ordersEntity.setNxDoApplyOnlyTime(formatWhatTime(0));
//        ordersEntity.setNxDoArriveDate(formatWhatDay(0));
//        ordersEntity.setNxDoGbDistributerId(-1);
//        ordersEntity.setNxDoGbDepartmentId(-1);
//        ordersEntity.setNxDoGbDepartmentFatherId(-1);
//        ordersEntity.setNxDoNxCommunityId(-1);
//        ordersEntity.setNxDoNxCommRestrauntFatherId(-1);
//        ordersEntity.setNxDoNxCommRestrauntId(-1);
//        ordersEntity.setNxDoDisGoodsFatherId(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
//        ordersEntity.setNxDoDepDisGoodsId(-1);
//        ordersEntity.setNxDoArriveWhatDay(getWeek(0));
//        ordersEntity.setNxDoCostPriceLevel("0");
//        ordersEntity.setNxDoPriceDifferent("0.0");
//        System.out.println("savvdororororororooro");
//        nxDepartmentOrdersService.save(ordersEntity);
//
//        //auto
//        System.out.println("pudiididididipppppp" + distributerGoodsEntity.getNxDgPurchaseAuto());
//        if(distributerGoodsEntity.getNxDgPurchaseAuto() != -1){
//            savePurGoodsAuto(ordersEntity);
//        }
//
//
//        Integer integer = checkDepDisGoods(ordersEntity);
//        ordersEntity.setNxDoDepDisGoodsId(integer);
//        nxDepartmentOrdersService.update(ordersEntity);
//
//    }

//
//    private Integer checkDepDisGoods(NxDepartmentOrdersEntity nxDepartmentOrders) {
//
//        System.out.println("chehchcchchhchchcdididigogood");
//
//        Integer depDisGoodsId = 0;
//        //判断是否是部门商品
//        Integer doDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
//        Integer nxDoDepartmentId1 = nxDepartmentOrders.getNxDoDepartmentId();
//        //查询部门还是订货群是否添加过此商品
//        Map<String, Object> map = new HashMap<>();
//        map.put("depId", nxDoDepartmentId1);
//        map.put("disGoodsId", doDisGoodsId);
//        List<NxDepartmentDisGoodsEntity> disGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
//        if (disGoodsEntities.size() == 0) {
//            //添加部门商品
//            NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
//            String nxDgGoodsName = nxDistributerGoodsEntity.getNxDgGoodsName();
//            NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
//            disGoodsEntity.setNxDdgDepGoodsName(nxDgGoodsName);
//            disGoodsEntity.setNxDdgDisGoodsId(doDisGoodsId);
//            disGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
//            disGoodsEntity.setNxDdgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
//            disGoodsEntity.setNxDdgDepGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
//            disGoodsEntity.setNxDdgDepGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
//            disGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
//            disGoodsEntity.setNxDdgDepartmentId(nxDoDepartmentId1);
//            disGoodsEntity.setNxDdgDepartmentFatherId(nxDepartmentOrders.getNxDoDepartmentFatherId());
//            //orderData
//            disGoodsEntity.setNxDdgOrderPrice(nxDepartmentOrders.getNxDoPrice());
//            disGoodsEntity.setNxDdgOrderCostPrice(nxDepartmentOrders.getNxDoCostPrice());
//            disGoodsEntity.setNxDdgOrderQuantity(nxDepartmentOrders.getNxDoQuantity());
//            disGoodsEntity.setNxDdgOrderRemark(nxDepartmentOrders.getNxDoRemark());
//            disGoodsEntity.setNxDdgOrderStandard(nxDepartmentOrders.getNxDoStandard());
//            disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
//            nxDepartmentDisGoodsService.save(disGoodsEntity);
//            depDisGoodsId = disGoodsEntity.getNxDepartmentDisGoodsId();
//
//        } else {
//
//            depDisGoodsId = disGoodsEntities.get(0).getNxDepartmentDisGoodsId();
//            NxDepartmentDisGoodsEntity disGoodsEntity = nxDepartmentDisGoodsService.queryObject(depDisGoodsId);
//            disGoodsEntity.setNxDdgOrderPrice(nxDepartmentOrders.getNxDoPrice());
//            disGoodsEntity.setNxDdgOrderCostPrice(nxDepartmentOrders.getNxDoCostPrice());
//            disGoodsEntity.setNxDdgOrderQuantity(nxDepartmentOrders.getNxDoQuantity());
//            disGoodsEntity.setNxDdgOrderRemark(nxDepartmentOrders.getNxDoRemark());
//            disGoodsEntity.setNxDdgOrderStandard(nxDepartmentOrders.getNxDoStandard());
//            disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
//            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDepartmentOrders.getPurchaseGoodsEntity();
//            if (purchaseGoodsEntity != null) {
//                if (purchaseGoodsEntity.getNxDpgSellUserId() != null) {
//                    disGoodsEntity.setNxDdgOrderSellerUserId(purchaseGoodsEntity.getNxDpgSellUserId());
//                }
//                disGoodsEntity.setNxDdgOrderBuyerUserId(purchaseGoodsEntity.getNxDpgBuyUserId());
//            }
//            nxDepartmentDisGoodsService.update(disGoodsEntity);
//
//        }
//        return depDisGoodsId;
//    }


    /**
     * ORDER
     * 订货首页获取用户和部门信息
     * @param userid 用户id
     * @return 用户和部门信息
     */
    @RequestMapping(value = "/getDepAndUserInfo/{userid}")
    @ResponseBody
    public R getDepAndUserInfo(@PathVariable Integer userid) {
        Map<String, Object> stringObjectMap = nxDepartmentService.queryDepAndUserInfo(userid);
        return R.ok().put("data", stringObjectMap);
    }

    /**
     * ORDER
     * 获取群用户部门和用户信息
     * @param userId 群用户id
     * @return 部门用户
     */
    @RequestMapping(value = "/getDepUserInfo/{userId}")
    @ResponseBody
    public R getDepUserInfo(@PathVariable Integer userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        NxDepartmentUserEntity nxDepartmentUserEntity = nxDepartmentUserService.queryDepUserInfo(map);
        return R.ok().put("data", nxDepartmentUserEntity);
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
        NxDepartmentUserEntity nxDepartmentUserEntity = nxDepartmentUserService.queryObject(userId);
        nxDepartmentUserEntity.setNxDuWxNickName(userName);
        nxDepartmentUserEntity.setNxDuWxAvartraUrl(filePath);
        nxDepartmentUserEntity.setNxDuUrlChange(1);
        nxDepartmentUserService.update(nxDepartmentUserEntity);

        NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryObject(depId);
        nxDepartmentEntity.setNxDepartmentName(groupName);
        nxDepartmentService.update(nxDepartmentEntity);


        return R.ok();

    }


    /**
     * PURCHASE,
     * 修改群信息
     * @param userName 用户名（没用）
     * @param groupName 群名称
     * @param userId 用户id
     * @param depId 群id
     * @return ok
     */
    @RequestMapping(value = "/updateGroupPurchase", method = RequestMethod.POST)
    @ResponseBody
    public R updateGroupPurchase(String userName, String groupName, Integer userId, Integer depId) {
        NxDepartmentUserEntity nxDepartmentUserEntity = nxDepartmentUserService.queryObject(userId);
        nxDepartmentUserEntity.setNxDuWxNickName(userName);
        nxDepartmentUserService.update(nxDepartmentUserEntity);

        NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryObject(depId);
        nxDepartmentEntity.setNxDepartmentName(groupName);
        nxDepartmentService.update(nxDepartmentEntity);
        return R.ok();
    }

    @RequestMapping(value = "/getDepUsersByDepId/{depId}")
    @ResponseBody
    public R getDepUsersByDepId(@PathVariable Integer depId) {
        List<NxDepartmentUserEntity> userEntities = nxDepartmentUserService.queryDepUsersByDepId(depId);
        return R.ok().put("data", userEntities);
    }

    /**
     * PURCHASE，DISTRIBUTE
     * 获取群用户
     * @param depId 群id
     * @return 用户列表
     */
    @RequestMapping(value = "/getDepUsersByFatherId/{depId}")
    @ResponseBody
    public R getDepUsersByFatherId(@PathVariable Integer depId) {
        List<NxDepartmentUserEntity> userEntities = nxDepartmentUserService.queryAllUsersByDepFatherId(depId);
        return R.ok().put("data", userEntities);
    }


    @RequestMapping(value = "/deleteDepUserWithDep/{userId}")
    @ResponseBody
    public R deleteDepUserWithDep(@PathVariable Integer userId) {
        NxDepartmentUserEntity userEntity = nxDepartmentUserService.queryObject(userId);
        Integer nxDuDepartmentId = userEntity.getNxDuDepartmentId();
        nxDepartmentService.delete(nxDuDepartmentId);
        nxDepartmentUserService.delete(userId);
        return R.ok();
    }


    /**
     * PURCHASE
     * 删除群用户
     * @param userId 用户id
     * @return ok
     */
    @RequestMapping(value = "/deleteDepUser/{userId}")
    @ResponseBody
    public R deleteDepUser(@PathVariable Integer userId) {

        nxDepartmentUserService.delete(userId);
        return R.ok();
//		NxDepartmentUserEntity nxDepartmentUserEntity = nxDepartmentUserService.queryObject(userId);
//		Integer nxDuAdmin = nxDepartmentUserEntity.getNxDuAdmin();
//		//删除采购员
//		if(nxDuAdmin.equals(1)){
//			Integer nxDuDepartmentId = nxDepartmentUserEntity.getNxDuDepartmentId();
//			List<NxDepartmentUserEntity> userEntities = nxDepartmentUserService.queryGroupAdminUserAmount(nxDuDepartmentId);
//			if(userEntities.size() == 1){
//				return R.error(-1,"这是唯一用户，不能删除。");
//			}else{
//				nxDepartmentUserService.delete(userId);
//				return R.ok();
//			}
//		}else{
//			//删除订货员
//			Map<String, Object> map = new HashMap<>();
//			map.put("userId",userId);
//			map.put("status", 4);
//			List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
//			if(ordersEntities.size() > 0){
//				return  R.error(-1, "还有未完成订货，不能删除");
//			}else{
//				nxDepartmentUserService.delete(userId);
//				return R.ok();
//			}
//
//		}
    }


}
