package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-27 09:44
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.formatWhatYearDayTime;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisUserPurchase;


@RestController
@RequestMapping("api/gbdistributeruser")
public class GbDistributerUserController {
    @Autowired
    private GbDistributerUserService gbDistributerUserService;
    @Autowired
    private GbDepartmentUserService gbDepartmentUserService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    private NxDistributerGbDistributerService nxDistributerGbDistributerService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private GbDistributerGoodsService gbDgService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private NxDistributerStandardService nxDistributerStandardService;
    @Autowired
    private GbDistributerStandardService dsService;
    @Autowired
    private GbDistributerFatherGoodsService dgfService;
    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private  NxAliasService nxAliasService;
    @Autowired
    private GbDistributerPayService gbDistributerPayService;
    @Autowired
    private NxJrdhUserService nxJrdhUserService;


    private static Logger logger=Logger.getLogger(GbDistributerUserController.class);   //获取Logger对象


    //



    @RequestMapping(value = "/gbRegisterWithFileInvite", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R gbRegisterWithFileInvite(@RequestParam("file") MultipartFile file,
                                @RequestParam("restaurantName") String restaurantName,
                                @RequestParam("code") String code,
                                @RequestParam("phone") String phone,
                                @RequestParam("address") String address,
                                 @RequestParam("disId") Integer disId,
                                HttpSession session) {

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
        map.put("admin", 2);
        GbDepartmentUserEntity depUserEntities = gbDepartmentUserService.queryDepUsersByOpenIdAndAdmin(map);

        if (depUserEntities == null) {
            GbDistributerEntity gbDistributerEntity = new GbDistributerEntity();
            gbDistributerEntity.setGbDistributerName(restaurantName);
            gbDistributerEntity.setGbDistributerPhone(phone);
            gbDistributerEntity.setGbDistributerAddress(address);
            gbDistributerEntity.setGbDistributerBusinessType(0);
            gbDistributerEntity.setGbDistributerPrintName("ApplyHalfPanel");
            gbDistributerEntity.setGbDistributerSysCityId(6);
            gbDistributerEntity.setGbDistributerBusinessType(0);
            gbDistributerEntity.setGbDistributerNxDisId(-1);

            GbDepartmentUserEntity purUser = new GbDepartmentUserEntity();
            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            //1 disuser save
            purUser.setGbDuWxOpenId(openid);
            purUser.setGbDuWxAvartraUrl(filePath);
            purUser.setGbDuWxNickName(restaurantName+ "采购员");
            gbDistributerEntity.setSingleDepartmentUser(purUser);
            Integer newDisId = gbDistributerService.saveSingleMendianDistributerGb(gbDistributerEntity);

            System.out.println("usidididid" + newDisId);
            if (newDisId != null) {

                GbDistributerEntity inviteGbDis = gbDistributerService.queryObject(disId);

                BigDecimal bitSet = new BigDecimal(inviteGbDis.getGbDistributerBuyQuantity());
                BigDecimal add = bitSet.add(new BigDecimal(1000)).setScale(0,BigDecimal.ROUND_HALF_UP);
                inviteGbDis.setGbDistributerBuyQuantity(add.toString());

                gbDistributerService.update(inviteGbDis);

                GbDistributerPayEntity gbDistributerPayEntity = new GbDistributerPayEntity();
                gbDistributerPayEntity.setGbGdpGbDisId(disId);
                gbDistributerPayEntity.setGbGdpGbNewDisId(newDisId);
                gbDistributerPayEntity.setGbGdpBuyQuantity("0.1");
                gbDistributerPayEntity.setGbGdpPaySubtotal("0");
                gbDistributerPayEntity.setGbGdpStatus(0);
                gbDistributerPayEntity.setGbGdpPayTime(formatWhatYearDayTime(0));
                gbDistributerPayEntity.setGbGdpType(2);

                gbDistributerPayService.save(gbDistributerPayEntity);

                Map<String, Object> mapRe = new HashMap<>();
                mapRe.put("disInfo", gbDistributerService.queryDistributerInfo(disId));
                mapRe.put("depUserInfo", gbDepartmentUserService.queryDepUserByOpenId(openid));
                return R.ok().put("data", mapRe);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }

    }

    @RequestMapping(value = "/gbRegisterWithFileInviteFromNx", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R gbRegisterWithFileInviteFromNx(@RequestParam("file") MultipartFile file,
                                      @RequestParam("restaurantName") String restaurantName,
                                      @RequestParam("code") String code,
                                      @RequestParam("phone") String phone,
                                      @RequestParam("address") String address,
                                      @RequestParam("disId") Integer disId,
                                      HttpSession session) {

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
        map.put("admin", 2);
        GbDepartmentUserEntity depUserEntities = gbDepartmentUserService.queryDepUsersByOpenIdAndAdmin(map);

        if (depUserEntities == null) {
            GbDistributerEntity gbDistributerEntity = new GbDistributerEntity();
            gbDistributerEntity.setGbDistributerName(restaurantName);
            gbDistributerEntity.setGbDistributerPhone(phone);
            gbDistributerEntity.setGbDistributerAddress(address);
            gbDistributerEntity.setGbDistributerBusinessType(0);
            gbDistributerEntity.setGbDistributerPrintName("ApplyHalfPanel");
            gbDistributerEntity.setGbDistributerSysCityId(6);
            gbDistributerEntity.setGbDistributerBusinessType(0);
            gbDistributerEntity.setGbDistributerNxDisId(-1);
            GbDepartmentUserEntity purUser = new GbDepartmentUserEntity();

            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            //1 disuser save
            purUser.setGbDuWxOpenId(openid);
            purUser.setGbDuWxAvartraUrl(filePath);
            purUser.setGbDuWxNickName(restaurantName+ "采购员");
            gbDistributerEntity.setSingleDepartmentUser(purUser);
            Integer newDisId = gbDistributerService.saveSingleMendianDistributerGb(gbDistributerEntity);

            System.out.println("usidididid" + newDisId);
            if (newDisId != null) {

                NxDistributerGbDistributerEntity entity = new NxDistributerGbDistributerEntity();
                entity.setNxDgdNxDistributerId(disId);
                entity.setNxDgdGbDistributerId(newDisId);
                entity.setNxDgdGbPayMethod(1);
                entity.setNxDgdGbPayPeriodWeek(4);
                entity.setNxDgdStatus(0);
                entity.setNxDgdFromNxDisId(disId);
                entity.setNxDgdFromNxDepId(-1);
                Map<String, Object> mapN = new HashMap<>();
                mapN.put("disId", newDisId);
                mapN.put("type", getGbDepartmentTypeAppSupplier());
                List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(mapN);
                entity.setNxDgdGbDepId(departmentEntities.get(0).getGbDepartmentId());
                nxDistributerGbDistributerService.save(entity);

                Map<String, Object> mapRe = new HashMap<>();
                mapRe.put("disInfo", gbDistributerService.queryDistributerInfo(newDisId));
                mapRe.put("depUserInfo", gbDepartmentUserService.queryDepUserByOpenId(openid));
                return R.ok().put("data", mapRe);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }

    }

    @RequestMapping(value = "/gbRegisterWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R gbRegisterWithFile(@RequestParam("file") MultipartFile file,
                                          @RequestParam("restaurantName") String restaurantName,
                                          @RequestParam("code") String code,
                                @RequestParam("phone") String phone,
                                @RequestParam("address") String address,
                                HttpSession session) {
        System.out.println("restaurantName" + restaurantName);

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
        map.put("admin", 2);
        GbDepartmentUserEntity depUserEntities = gbDepartmentUserService.queryDepUsersByOpenIdAndAdmin(map);

        if (depUserEntities == null) {
            GbDistributerEntity gbDistributerEntity = new GbDistributerEntity();
            gbDistributerEntity.setGbDistributerName(restaurantName);
            gbDistributerEntity.setGbDistributerPhone(phone);
            gbDistributerEntity.setGbDistributerAddress(address);
            gbDistributerEntity.setGbDistributerPrintName("ApplyHalfPanel");
            gbDistributerEntity.setGbDistributerSysCityId(6);
            gbDistributerEntity.setGbDistributerBusinessType(0);
            gbDistributerEntity.setGbDistributerNxDisId(-1);
            GbDepartmentUserEntity purUser = new GbDepartmentUserEntity();

            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
//            //1 disuser save
            purUser.setGbDuWxOpenId(openid);
            purUser.setGbDuWxAvartraUrl(filePath);
            purUser.setGbDuWxNickName(restaurantName+ "采购员");
            gbDistributerEntity.setSingleDepartmentUser(purUser);
            Integer disId = gbDistributerService.saveSingleMendianDistributerGb(gbDistributerEntity);
            System.out.println("usidididid" + disId);

            if (disId != null) {
                Map<String, Object> mapRe = new HashMap<>();
                mapRe.put("disInfo", gbDistributerService.queryDistributerInfo(disId));
                mapRe.put("depUserInfo", gbDepartmentUserService.queryDepUserByOpenId(openid));
                return R.ok().put("data", mapRe);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }

    }



    @RequestMapping(value = "/gbRegisterWithFileWithNxDisId", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R gbRegisterWithFileWithNxDisId(@RequestParam("file") MultipartFile file,
                                @RequestParam("userName") String userName,
                                @RequestParam("code") String code,
                                           @RequestParam("disId") Integer disId,
                                           @RequestParam("depFatherId") Integer depFatherId,
                                           @RequestParam("depId") Integer depId,
                                           @RequestParam("depName") String depName,
                                HttpSession session) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongCaigouAppId() +
                "&secret=" + myAPPIDConfig.getTexiansongCaigouScreat() + "&js_code=" + code + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);

        // 转成Json对象 获取openidjrdhUserRegister
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();

        GbDepartmentUserEntity depUserEntities = gbDepartmentUserService.queryDepUserByOpenId(openid);

        if (depUserEntities == null) {
            GbDistributerEntity gbDistributerEntity = new GbDistributerEntity();
            gbDistributerEntity.setGbDistributerName(depName);
            GbDistributerUserEntity distributerUserEntity = new GbDistributerUserEntity();
            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            //1 disuser save
            distributerUserEntity.setGbDiuWxAvartraUrl(filePath);
            distributerUserEntity.setGbDiuWxNickName(userName);
            distributerUserEntity.setGbDiuWxOpenId(openid);
            gbDistributerEntity.setGbDistributerBusinessType(0);
            gbDistributerEntity.setGbDistributerUserEntity(distributerUserEntity);
            Integer resUserId = gbDistributerService.saveSingleMendianDistributerGb(gbDistributerEntity);

            Integer fromDepId = 0;
            if(depFatherId != 0){
                fromDepId = depFatherId;
            }else{
                fromDepId = depId;
            }
            System.out.println("tosaveNxGbBusinessDatasaveNxGbBusinessData");
            saveNxGbBusinessData(disId, gbDistributerEntity, fromDepId);

            System.out.println("usidididid" + resUserId);
            if (resUserId != null) {
                Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(resUserId);
                GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openid);
                stringObjectMap.put("depUser", depUserEntity);
                return R.ok().put("data", stringObjectMap);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }


    }


   private  void  saveNxGbBusinessData(Integer disId, GbDistributerEntity gbDistributerEntity, Integer depId){

       Integer gbDistributerId = gbDistributerEntity.getGbDistributerId();

       NxDistributerGbDistributerEntity entity = new NxDistributerGbDistributerEntity();
        entity.setNxDgdNxDistributerId(disId);
        entity.setNxDgdGbDistributerId(gbDistributerId);
        entity.setNxDgdGbPayMethod(1);
        entity.setNxDgdGbPayPeriodWeek(4);
        entity.setNxDgdStatus(0);
        entity.setNxDgdFromNxDepId(depId);
        Map<String, Object> map = new HashMap<>();
       map.put("disId", gbDistributerId);
       map.put("type", getGbDepartmentTypeAppSupplier());
       List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
       entity.setNxDgdGbDepId(departmentEntities.get(0).getGbDepartmentId());
       nxDistributerGbDistributerService.save(entity);

       Map<String, Object> mapD = new HashMap<>();
       mapD.put("depFatherId", depId);
       List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(mapD);
       if(departmentDisGoodsEntities.size() > 0){
           for(NxDepartmentDisGoodsEntity depDisGoodsEntity : departmentDisGoodsEntities){
               Integer nxDdgDisGoodsId = depDisGoodsEntity.getNxDdgDisGoodsId();
               postDgnGoodsForNxData(nxDdgDisGoodsId, gbDistributerId, departmentEntities.get(0).getGbDepartmentId());
           }
       }


   }

    public R postDgnGoodsForNxData(Integer disGoodsId, Integer gbDisId, Integer depId) {

        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(disGoodsId);
        Integer nxDgNxGoodsId = nxDistributerGoodsEntity.getNxDgNxGoodsId();


        //判断是否已经下载
        Map<String, Object> map7 = new HashMap<>();
        map7.put("disId", gbDisId);
        map7.put("goodsId", nxDgNxGoodsId);
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDgService.queryDisGoodsByParams(map7);

        if (distributerGoodsEntities.size() > 0) {
            return R.error(-1, "已经下载");
        } else {
            GbDistributerGoodsEntity cgnGoods = new GbDistributerGoodsEntity();
            cgnGoods.setGbDgGoodsName(nxDistributerGoodsEntity.getNxDgGoodsName());
            cgnGoods.setGbDgGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
            cgnGoods.setGbDgGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
            cgnGoods.setGbDgGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
            cgnGoods.setGbDgGoodsStandardWeight(nxDistributerGoodsEntity.getNxDgGoodsStandardWeight());
            cgnGoods.setGbDgGoodsDetail(nxDistributerGoodsEntity.getNxDgGoodsDetail());
            cgnGoods.setGbDgGoodsBrand(nxDistributerGoodsEntity.getNxDgGoodsBrand());
            cgnGoods.setGbDgGoodsPlace(nxDistributerGoodsEntity.getNxDgGoodsPlace());
            cgnGoods.setGbDgGoodsSort(nxDistributerGoodsEntity.getNxDgGoodsSort());


            System.out.println("nxGoodsnameme==wiriii" + nxDistributerGoodsEntity.getNxDgGoodsName() + "== " + nxDistributerGoodsEntity.getNxDgWillPrice());
            cgnGoods.setGbDgNxDistributerGoodsPrice(nxDistributerGoodsEntity.getNxDgWillPrice());
            cgnGoods.setGbDgDistributerId(gbDisId);
            cgnGoods.setGbDgGoodsStatus(0);
            cgnGoods.setGbDgGoodsIsWeight(0);
            cgnGoods.setGbDgNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
            cgnGoods.setGbDgNxFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
            cgnGoods.setGbDgNxGrandId(nxDistributerGoodsEntity.getNxDgNxGrandId());
            cgnGoods.setGbDgNxGreatGrandId(nxDistributerGoodsEntity.getNxDgNxGreatGrandId());
            cgnGoods.setGbDgPullOff(0);
            cgnGoods.setGbDgGoodsType(5);
            cgnGoods.setGbDgNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
            cgnGoods.setGbDgNxDistributerGoodsId(disGoodsId);
            cgnGoods.setGbDgGbDepartmentId(depId);
            cgnGoods.setGbDgControlFresh(0);
            cgnGoods.setGbDgControlPrice(0);
            cgnGoods.setGbDgGoodsInventoryType(1);
            cgnGoods.setGbDgIsFranchisePrice(0);
            cgnGoods.setGbDgIsSelfControl(0);
            cgnGoods.setGbDgNxFatherImg(nxDistributerGoodsEntity.getNxDgNxFatherImg());
            cgnGoods.setGbDgNxFatherImgLarge(nxDistributerGoodsEntity.getNxDgGoodsFileLarge());
//            GbDistributerGoodsEntity disGoods = saveDisGoodsForNx(cgnGoods);
            GbDistributerGoodsEntity disGoods = saveDisGoods(cgnGoods);

            //添加部门商品
            GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
            disGoodsEntity.setGbDdgDepGoodsName(disGoods.getGbDgGoodsName());
            disGoodsEntity.setGbDdgDisGoodsId(disGoods.getGbDistributerGoodsId());
            disGoodsEntity.setGbDdgDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
            disGoodsEntity.setGbDdgDisGoodsGrandId(disGoods.getGbDgDfgGoodsGrandId());
            disGoodsEntity.setGbDdgDepGoodsPinyin(disGoods.getGbDgGoodsPinyin());
            disGoodsEntity.setGbDdgDepGoodsPy(disGoods.getGbDgGoodsPy());
            disGoodsEntity.setGbDdgDepGoodsStandardname(disGoods.getGbDgGoodsStandardname());
            disGoodsEntity.setGbDdgDepartmentId(disGoods.getGbDgGbDepartmentId());
            disGoodsEntity.setGbDdgDepartmentFatherId(disGoods.getGbDgGbDepartmentId());
            disGoodsEntity.setGbDdgGbDepartmentId(disGoods.getGbDgGbDepartmentId());
            disGoodsEntity.setGbDdgGbDisId(disGoods.getGbDgDistributerId());
            disGoodsEntity.setGbDdgGoodsType(disGoods.getGbDgGoodsType());
            disGoodsEntity.setGbDdgStockTotalWeight("0.0");
            disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
            disGoodsEntity.setGbDdgShowStandardId(-1);
            disGoodsEntity.setGbDdgShowStandardName(disGoods.getGbDgGoodsStandardname());
            disGoodsEntity.setGbDdgShowStandardScale("-1");
            disGoodsEntity.setGbDdgShowStandardWeight(null);
            disGoodsEntity.setGbDdgNxDistributerGoodsId(disGoodsId);
            disGoodsEntity.setGbDdgNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
            gbDepartmentDisGoodsService.save(disGoodsEntity);

            //2.2
            Integer nxCgGoodsId = disGoods.getGbDistributerGoodsId();

//            List<NxAliasEntity> aliasEntities = cgnGoods.getNxAliasEntities();
//            if (aliasEntities.size() > 0) {
//                for (NxAliasEntity aliasEntity : aliasEntities) {
//                    GbDistributerAliasEntity disAlias = new GbDistributerAliasEntity();
//                    disAlias.setGbDaDisGoodsId(nxCgGoodsId);
//                    disAlias.setGbDaAliasName(aliasEntity.getNxAliasName());
//                    disAliasService.save(disAlias);
//                }
//            }

            Integer gbDgNxDistributerGoodsId = cgnGoods.getGbDgNxDistributerGoodsId();
            List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByDisGoodsId(gbDgNxDistributerGoodsId);
            if(distributerStandardEntities.size() > 0){
                for(NxDistributerStandardEntity standardEntity: distributerStandardEntities){
                    GbDistributerStandardEntity distributerStandardEntity = new GbDistributerStandardEntity();
                    distributerStandardEntity.setGbDsDisGoodsId(nxCgGoodsId);
                    distributerStandardEntity.setGbDsStandardName(standardEntity.getNxDsStandardName());
                    dsService.save(distributerStandardEntity);
                }
            }


            //添加给门店
            //如果是餐饮商品，自动给门店添加部门商品
            if (disGoods.getGbDgGoodsType() < 20) {
                Map<String, Object> map = new HashMap<>();
                map.put("disId", disGoods.getGbDgDistributerId());
                map.put("type", getGbDepartmentTypeMendian());
                List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
                if (departmentEntities.size() > 0) {
                    for (GbDepartmentEntity dep : departmentEntities) {
                        GbDepartmentDisGoodsEntity disGoodsEntityDep = new GbDepartmentDisGoodsEntity();
                        disGoodsEntityDep.setGbDdgDepGoodsName(disGoods.getGbDgGoodsName());
                        disGoodsEntityDep.setGbDdgDisGoodsId(disGoods.getGbDistributerGoodsId());
                        disGoodsEntityDep.setGbDdgDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
                        disGoodsEntityDep.setGbDdgDisGoodsGrandId(disGoods.getGbDgDfgGoodsGrandId());
                        disGoodsEntityDep.setGbDdgDepGoodsPinyin(disGoods.getGbDgGoodsPinyin());
                        disGoodsEntityDep.setGbDdgDepGoodsPy(disGoods.getGbDgGoodsPy());
                        disGoodsEntityDep.setGbDdgDepGoodsStandardname(disGoods.getGbDgGoodsStandardname());
                        disGoodsEntityDep.setGbDdgDepartmentId(dep.getGbDepartmentId());
                        disGoodsEntityDep.setGbDdgDepartmentFatherId(dep.getGbDepartmentId());
                        disGoodsEntityDep.setGbDdgGbDepartmentId(disGoods.getGbDgGbDepartmentId());
                        disGoodsEntityDep.setGbDdgGbDisId(disGoods.getGbDgDistributerId());
                        disGoodsEntityDep.setGbDdgGoodsType(disGoods.getGbDgGoodsType());
                        disGoodsEntityDep.setGbDdgStockTotalWeight("0.0");
                        disGoodsEntityDep.setGbDdgStockTotalSubtotal("0.0");
                        disGoodsEntityDep.setGbDdgShowStandardId(-1);
                        disGoodsEntityDep.setGbDdgShowStandardName(disGoods.getGbDgGoodsStandardname());
                        disGoodsEntityDep.setGbDdgShowStandardScale("-1");
                        disGoodsEntityDep.setGbDdgShowStandardWeight(null);
                        disGoodsEntityDep.setGbDdgNxDistributerGoodsId(disGoodsId);
                        disGoodsEntityDep.setGbDdgNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
                        gbDepartmentDisGoodsService.save(disGoodsEntityDep);
                    }
                }
            }

            return R.ok().put("data", disGoods);
        }
    }




    private GbDistributerGoodsEntity saveDisGoods(GbDistributerGoodsEntity cgnGoods) {

        //queryGrandFatherId
        NxGoodsEntity fatherEntity = nxGoodsService.queryObject(cgnGoods.getGbDgNxFatherId());
        cgnGoods.setGbDgNxFatherName(fatherEntity.getNxGoodsName());
        Integer grandFatherId = fatherEntity.getNxGoodsFatherId();
        cgnGoods.setGbDgNxGrandId(grandFatherId);
        NxGoodsEntity grandEntity = nxGoodsService.queryObject(grandFatherId);
        cgnGoods.setGbDgNxGrandName(grandEntity.getNxGoodsName());

        //queryGreatGrandFatherId
        Integer greatGrandFatherId = grandEntity.getNxGoodsFatherId();
        cgnGoods.setGbDgNxGreatGrandId(greatGrandFatherId);
        cgnGoods.setGbDgNxGreatGrandName(nxGoodsService.queryObject(greatGrandFatherId).getNxGoodsName());
        System.out.println("nxgodosoososooosos" + cgnGoods.getGbDgNxGoodsId() +"fff=" +  cgnGoods.getGbDgNxFatherId() + "ggg=="+cgnGoods.getGbDgNxGrandId() +" grrr" + cgnGoods.getGbDgNxGreatGrandId());

        System.out.println("hereireire------------------"+ cgnGoods.getGbDgNxFatherName() + "gg" + cgnGoods.getGbDgNxGrandName()+ "grrrg" + cgnGoods.getGbDgNxGreatGrandName());
        Integer nxDgDistributerId = cgnGoods.getGbDgDistributerId();

        // 3， 查询父类
        Integer nxDgNxFatherId = cgnGoods.getGbDgNxFatherId();
        Map<String, Object> map11 = new HashMap<>();
        map11.put("nxGoodsId", nxDgNxFatherId);
        map11.put("disId", nxDgDistributerId);
        System.out.println("faehrhrhehehemap" + map11);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities1 = dgfService.queryDisFathersGoodsByParamsGb(map11);

        if (fatherGoodsEntities1.size() > 0) {
            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities1.get(0);
            Integer nxDfgGoodsAmount = fatherGoodsEntity.getGbDfgGoodsAmount();
            fatherGoodsEntity.setGbDfgGoodsAmount(nxDfgGoodsAmount + 1);
            dgfService.update(fatherGoodsEntity);

            //2，保存disId商品
            cgnGoods.setGbDgDfgGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
            cgnGoods.setGbDgDfgGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
            //1 ，先保存disGoods

            gbDgService.save(cgnGoods);
            //
        } else {
            //添加fatherGoods的第一个级别
            GbDistributerFatherGoodsEntity dgf = new GbDistributerFatherGoodsEntity();
            dgf.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
            dgf.setGbDfgFatherGoodsName(cgnGoods.getGbDgNxFatherName()  + "2");
            dgf.setGbDfgFatherGoodsLevel(2);
            dgf.setGbDfgGoodsAmount(1);
            dgf.setGbDfgPriceAmount(0);
            dgf.setGbDfgPriceTwoAmount(0);
            dgf.setGbDfgPriceThreeAmount(0);
            dgf.setGbDfgFatherGoodsColor(cgnGoods.getGbDgNxGoodsFatherColor());
            dgf.setGbDfgNxGoodsId(cgnGoods.getGbDgNxFatherId());
            dgf.setGbDfgFatherGoodsImg(cgnGoods.getGbDgNxFatherImg());
            dgf.setGbDfgFatherGoodsSort(cgnGoods.getGbDgGoodsSort());
            dgfService.save(dgf);
            //更新disGoods的fatherGoodsId
            Integer distributerFatherGoodsId = dgf.getGbDistributerFatherGoodsId();
            cgnGoods.setGbDgDfgGoodsFatherId(distributerFatherGoodsId);
            cgnGoods.setGbDgDfgGoodsGrandId(dgf.getGbDfgFathersFatherId());
            gbDgService.save(cgnGoods);
            //继续查询是否有GrandFather
            String grandName = cgnGoods.getGbDgNxGrandName();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("disId", nxDgDistributerId);
            map2.put("nxGoodsId", cgnGoods.getGbDgNxGrandId());
            map2.put("goodsLevel", 1);
            List<GbDistributerFatherGoodsEntity> grandGoodsFather = dgfService.queryHasDisFathersFather(map2);
            if (grandGoodsFather.size() > 0) {
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = grandGoodsFather.get(0);
                Integer nxDfgGoodsAmount = dgf.getGbDfgGoodsAmount();
                dgf.setGbDfgGoodsAmount(nxDfgGoodsAmount + 1);
                dgf.setGbDfgFathersFatherId(gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId());
                dgfService.update(dgf);
            } else {
                //tianjiaGrand
                GbDistributerFatherGoodsEntity grand = new GbDistributerFatherGoodsEntity();
                grand.setGbDfgFatherGoodsName(cgnGoods.getGbDgNxGrandName() + "1");
                grand.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
                grand.setGbDfgFatherGoodsLevel(1);
                grand.setGbDfgFatherGoodsColor(cgnGoods.getGbDgNxGoodsFatherColor());
                grand.setGbDfgNxGoodsId(cgnGoods.getGbDgNxGrandId());
                grand.setGbDfgFatherGoodsSort(grand.getGbDfgFatherGoodsSort());
                NxGoodsEntity nxGrand = nxGoodsService.queryObject(cgnGoods.getGbDgNxGrandId());
                grand.setGbDfgFatherGoodsImg(nxGrand.getNxGoodsFile());
                grand.setGbDfgFatherGoodsImgLarge(nxGrand.getNxGoodsFileBig());
                grand.setGbDfgGoodsAmount(1);
                dgfService.save(grand);
                dgf.setGbDfgFathersFatherId(grand.getGbDistributerFatherGoodsId());

                dgfService.update(dgf);


                //查询是否有greatGrand
                Map<String, Object> map3 = new HashMap<>();
                map3.put("disId", nxDgDistributerId);
                map3.put("nxGoodsId", cgnGoods.getGbDgNxGreatGrandId());
                map3.put("goodsLevel", 0);
                List<GbDistributerFatherGoodsEntity> greatGrandGoodsFather = dgfService.queryHasDisFathersFather(map3);
                if (greatGrandGoodsFather.size() > 0) {
                    GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = greatGrandGoodsFather.get(0);
                    Integer disFatherId = gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId();
                    grand.setGbDfgFathersFatherId(disFatherId);
                    Map<String, Object> map = new HashMap<>();
                    map.put("fathersFatherId", disFatherId);
                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(map);
                    grand.setGbDfgFatherGoodsSort(grandGoodsEntities.size() + 1);
                    Integer gbDfgGoodsAmount = grand.getGbDfgGoodsAmount();
                    grand.setGbDfgGoodsAmount(gbDfgGoodsAmount + 1);
                    dgfService.update(grand);
                } else {
                    GbDistributerFatherGoodsEntity greatGrand = new GbDistributerFatherGoodsEntity();
                    NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(cgnGoods.getGbDgNxGreatGrandId());
                    greatGrand.setGbDfgFatherGoodsName(cgnGoods.getGbDgNxGreatGrandName() + "0");
                    greatGrand.setGbDfgFatherGoodsImg(nxGoodsEntity.getNxGoodsFile());
                    greatGrand.setGbDfgFatherGoodsImgLarge(nxGoodsEntity.getNxGoodsFileBig());
                    greatGrand.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
                    greatGrand.setGbDfgFatherGoodsLevel(0);
                    greatGrand.setGbDfgFathersFatherId(0);
                    greatGrand.setGbDfgFatherGoodsColor(cgnGoods.getGbDgNxGoodsFatherColor());
                    greatGrand.setGbDfgNxGoodsId(cgnGoods.getGbDgNxGreatGrandId());
                    Map<String, Object> mapGreatGrand = new HashMap<>();
                    mapGreatGrand.put("fathersFatherId", 0);
                    mapGreatGrand.put("disId", nxDgDistributerId);
                    System.out.println(mapGreatGrand);
                    System.out.println("mapgreagtgrandndnndd");
                    List<GbDistributerFatherGoodsEntity> greatGrandFatherGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(mapGreatGrand);
                    greatGrand.setGbDfgFatherGoodsSort(greatGrandFatherGoodsEntities.size() + 1);
                    greatGrand.setGbDfgGoodsAmount(1);
                    dgfService.save(greatGrand);
                    System.out.println("greatanndnndn======" + greatGrand);
                    grand.setGbDfgFathersFatherId(greatGrand.getGbDistributerFatherGoodsId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("fathersFatherId", greatGrand.getGbDistributerFatherGoodsId());
                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(map);
                    grand.setGbDfgFatherGoodsSort(grandGoodsEntities.size() + 1);
                    dgfService.update(grand);
                }
            }
        }


        return cgnGoods;
    }

    @RequestMapping(value = "/gbLogin/{code}")
    @ResponseBody
    public R gbLogin(@PathVariable String code) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String maimaiAppID = myAPPIDConfig.getTexiansongCaigouAppId();
        String maimaiScreat = myAPPIDConfig.getTexiansongCaigouScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
                maimaiScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        String str = WeChatUtil.httpRequest(url, "GET", null);
        JSONObject jsonObject = JSONObject.parseObject(str);
        String openId = jsonObject.get("openid").toString();
        if (openId != null) {
            System.out.println("pepeeooppppp");
            GbDepartmentUserEntity departmentUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);

            if (departmentUserEntity != null) {
                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(departmentUserEntity.getGbDuDistributerId());
                GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);
                Map<String, Object> stringObjectMap = new HashMap<>();
                stringObjectMap.put("depUserInfo", depUserEntity);
                stringObjectMap.put("depInfo", gbDepartmentService.queryObject(departmentUserEntity.getGbDuDepartmentId()));
                stringObjectMap.put("disInfo", gbDistributerEntity);
                return R.ok().put("data", stringObjectMap);
            } else {
                return R.error(-1, "请向管理员索要注册邀请");
            }

        } else {
            return R.error(-1, "请进行注册");
        }

    }
    @RequestMapping(value = "/gbLoginIndex/{code}")
    @ResponseBody
    public R gbLoginIndex(@PathVariable String code) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String maimaiAppID = myAPPIDConfig.getTexiansongCaigouAppId();
        String maimaiScreat = myAPPIDConfig.getTexiansongCaigouScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
                maimaiScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        String str = WeChatUtil.httpRequest(url, "GET", null);
        JSONObject jsonObject = JSONObject.parseObject(str);
        String openId = jsonObject.get("openid").toString();
        if (openId != null) {
            GbDepartmentUserEntity departmentUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);

            if (departmentUserEntity != null) {
                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(departmentUserEntity.getGbDuDistributerId());

                GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);
                Map<String, Object> stringObjectMap = new HashMap<>();
                stringObjectMap.put("depUserInfo", depUserEntity);
                stringObjectMap.put("disInfo", gbDistributerEntity);
                return R.ok().put("data", stringObjectMap);
            } else {
                System.out.println("jrjrrjjrjrjr" + openId);
                NxJrdhUserEntity jrdhUserEntity = nxJrdhUserService.queryWhichUserByOpenId(openId);
                if(jrdhUserEntity != null){
                    return R.ok().put("data", "noBuyer");
                }
                return R.error(-1, "请向管理员索要注册邀请");
            }

        } else {
            return R.error(-1, "请进行注册");
        }

    }


    @RequestMapping(value = "/getGbMendianAndUserInfo", method = RequestMethod.POST)
    @ResponseBody
    public R getGbMendianAndUserInfo (Integer userId, Integer depId) {

         Map<String, Object> stringObjectMap = new HashMap<>();
        GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryDepUserInfoGb(userId);

        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(depId);
        stringObjectMap.put("depInfo",departmentEntity );
        stringObjectMap.put("userInfo",gbDepartmentUserEntity );
        return R.ok().put("data", stringObjectMap);
    }

    @RequestMapping(value = "/getDisUserInfo/{userId}")
    @ResponseBody
    public R getDisUserInfo(@PathVariable Integer userId) {
        Map<String, Object> stringObjectMap  = gbDistributerUserService.queryDisAndUserInfo(userId);
        return R.ok().put("data", stringObjectMap);
    }



    /**
     * 下载输入数字的语音文件
     * @param value
     * @param session
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/downLoadNumber/{value}")
    public ResponseEntity downLoadNumber(@PathVariable String value, HttpSession session) throws Exception {


        //1,获取文件路径
        ServletContext servletContext = session.getServletContext();
        String realPath = servletContext.getRealPath("numberRecord/" + value + ".mp3");

        System.out.println("kaknakreailpath" + value);
        //2,把文件读取程序当中
        InputStream io = new FileInputStream(realPath);
        byte[] body = new byte[io.available()];
        io.read(body);


        //3,创建相应头
        HttpHeaders httpHeaders = new HttpHeaders();
        System.out.println(httpHeaders);

        httpHeaders.add("Content-Disposition", "attachment; filename=" + value + ".mp3");
        httpHeaders.add("Content-Type", "audio/mpeg");
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);

        return responseEntity;
    }


    @RequestMapping(value = "/getDisManageUsers/{disId}")
    @ResponseBody
    public R getDisManageUsers(@PathVariable Integer disId) {
        List<GbDistributerUserEntity> userEntities = gbDistributerUserService.getAllUserByDisId(disId);

        return R.ok().put("data", userEntities);
    }

    @RequestMapping(value = "/deleteDisUser/{userId}")
    @ResponseBody
    public R deleteDisUser(@PathVariable Integer userId) {
        gbDistributerUserService.delete(userId);
        return R.ok();
    }



    /**
     * 批发商登陆
     * @param distributerUserEntity 批发商
     * @return 批发商
     */
//    @RequestMapping(value = "/disLogin", method = RequestMethod.POST)
//    @ResponseBody
//    public R disLogin(@RequestBody GbDistributerUserEntity distributerUserEntity) {
//
//        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
//        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiansuocaigouguanliduanAppId() + "&secret=" +
//                myAPPIDConfig.getLiansuocaigouguanliduanScreat() + "&js_code=" + distributerUserEntity.getGbDiuCode() +
//                "&grant_type=authorization_code";
//        // 发送请求，返回Json字符串
//        String str = WeChatUtil.httpRequest(url, "GET", null);
//        // 转成Json对象 获取openid
//        JSONObject jsonObject = JSONObject.parseObject(str);
//
//        // 我们需要的openid，在一个小程序中，openid是唯一的
//        String openid = jsonObject.get("openid").toString();
//        System.out.println("didididaaa" + openid);
//        logger.info(jsonObject);
//
//
//        List<GbDistributerUserEntity> distributerUserEntities = gbDistributerUserService.queryUserByOpenId(openid);
//        if (distributerUserEntities.size() > 0) {
//            GbDistributerUserEntity gbDistributerUserEntity = distributerUserEntities.get(0);
//            Integer gbDistributerUserId = gbDistributerUserEntity.getGbDistributerUserId();
//            Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(gbDistributerUserId);
//            System.out.println("dididid" + stringObjectMap);
//            logger.info(stringObjectMap);
//            gbDistributerUserEntity.setGbDiuLoginTimes(gbDistributerUserEntity.getGbDiuLoginTimes() + 1);
//            gbDistributerUserService.update(gbDistributerUserEntity);
//            return R.ok().put("data", stringObjectMap);
//        } else {
//            return R.error(-1, "用户不存在");
//        }
//    }
    

    /**
     * 批发商登陆
     * @param distributerUserEntity 批发商
     * @return 批发商
     */
    @RequestMapping(value = "/disLoginSxWork", method = RequestMethod.POST)
    @ResponseBody
    public R disLoginSxWork(@RequestBody GbDistributerUserEntity distributerUserEntity) {

        String suiteToken = getWxProperty(Constant.SUITE_TOKEN_RX);
        String code = distributerUserEntity.getGbDiuCode();
        String userCropUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/miniprogram/jscode2session?suite_access_token="+suiteToken+"&js_code="+code+"&grant_type=authorization_code";

        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(userCropUrl, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println("jsonObjectjsonObject" + jsonObject);
        String openUserId = jsonObject.getString("open_userid");

        List<GbDistributerUserEntity> distributerUserEntities = gbDistributerUserService.queryUserByOpenId(openUserId);
        if (distributerUserEntities.size() > 0) {
            GbDistributerUserEntity gbDistributerUserEntity = distributerUserEntities.get(0);
            Integer gbDistributerUserId = gbDistributerUserEntity.getGbDistributerUserId();
            Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(gbDistributerUserId);
            GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openUserId);
            stringObjectMap.put("depUser", depUserEntity);
//            gbDistributerUserEntity.setGbDiuLoginTimes(gbDistributerUserEntity.getGbDiuLoginTimes() + 1);
//            gbDistributerUserService.update(distributerUserEntity);
            System.out.println("dididid" + stringObjectMap);
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
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

    /**
     * 批发商登陆
     * @param distributerUserEntity 批发商
     * @return 批发商
     */
    @RequestMapping(value = "/disLoginSx", method = RequestMethod.POST)
    @ResponseBody
    public R disLoginSx(@RequestBody GbDistributerUserEntity distributerUserEntity) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getShixianGuanliAppId() + "&secret=" +
                myAPPIDConfig.getShixianGuanliScreat() + "&js_code=" + distributerUserEntity.getGbDiuCode() +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        System.out.println("didididaaa" + openid);

        List<GbDistributerUserEntity> distributerUserEntities = gbDistributerUserService.queryUserByOpenId(openid);
        if (distributerUserEntities.size() > 0) {
            GbDistributerUserEntity gbDistributerUserEntity = distributerUserEntities.get(0);
            Integer gbDiuLoginTimes = gbDistributerUserEntity.getGbDiuLoginTimes();
            gbDistributerUserEntity.setGbDiuLoginTimes(gbDiuLoginTimes + 1);
            gbDistributerUserService.update(gbDistributerUserEntity);

            Integer gbDistributerUserId = gbDistributerUserEntity.getGbDistributerUserId();
            Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(gbDistributerUserId);
            GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openid);
            stringObjectMap.put("depUser", depUserEntity);
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
        }
    }


    @RequestMapping(value = "/disUserAdminSaveSxWithFile",  produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R disUserAdminSaveSxWithFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("userName") String userName,
                                        @RequestParam("disId") Integer disId,
                                        @RequestParam("code") String code,
                                        HttpSession session) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getShixianGuanliAppId() + "&secret=" +
                myAPPIDConfig.getShixianGuanliScreat() + "&js_code=" + code +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        List<GbDistributerUserEntity> distributerUserEntities = gbDistributerUserService.queryUserByOpenId(openid);

        if (distributerUserEntities.size() > 0) {
            return R.error(-1, "用户已存在，请直接登陆");
        } else {

            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            //1 disuser save
            GbDistributerUserEntity distributerUserEntity = new GbDistributerUserEntity();
            distributerUserEntity.setGbDiuPrintDeviceId("-1");
            distributerUserEntity.setGbDiuLoginTimes(0);
            distributerUserEntity.setGbDiuPrintBillDeviceId("-1");
            distributerUserEntity.setGbDiuLoginTimes(0);
            distributerUserEntity.setGbDiuWxOpenId(openid);
            distributerUserEntity.setGbDiuWxNickName(userName);
            distributerUserEntity.setGbDiuWxAvartraUrl(filePath);
            distributerUserEntity.setGbDiuUrlChange(1);
            distributerUserEntity.setGbDiuDistributerId(disId);
            Integer disUserId = gbDistributerUserService.save(distributerUserEntity);
            return R.ok();
        }
    }


    /**
     * 批发商新管理者注册
     * @param distributerUserEntity 批发商用户
     * @return 批发商
     */
    @RequestMapping(value = "/disUserAdminSaveSxWork", method = RequestMethod.POST)
    @ResponseBody
    public R disUserAdminSaveSxWork(@RequestBody GbDistributerUserEntity distributerUserEntity) {
        String suiteToken = getWxProperty(Constant.SUITE_TOKEN_RX);
        String userCropUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/miniprogram/jscode2session?suite_access_token="+suiteToken+
                "&js_code=" + distributerUserEntity.getGbDiuCode() + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(userCropUrl, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println("jsonObjectjsonObject" + jsonObject);
        String openUserId = jsonObject.getString("open_userid");

        List<GbDistributerUserEntity> distributerUserEntities = gbDistributerUserService.queryUserByOpenId(openUserId);

        if (distributerUserEntities.size() > 0) {
            return R.error(-1, "用户已存在，请直接登陆");
        } else {

            //1 disuser save
            distributerUserEntity.setGbDiuPrintDeviceId("-1");
            distributerUserEntity.setGbDiuPrintBillDeviceId("-1");
            distributerUserEntity.setGbDiuLoginTimes(0);
            distributerUserEntity.setGbDiuWxOpenId(openUserId);
            Integer disUserId = gbDistributerUserService.save(distributerUserEntity);

            Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(disUserId);
            GbDepartmentUserEntity depUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openUserId);
            stringObjectMap.put("depUser", depUserEntity);
            return R.ok().put("data", stringObjectMap);

        }
    }



    /**
     * 批发商新管理者注册
     * @param distributerUserEntity 批发商用户
     * @return 批发商
//     */
//    @RequestMapping(value = "/disUserAdminSave", method = RequestMethod.POST)
//    @ResponseBody
//    public R disUserAdminSave(@RequestBody GbDistributerUserEntity distributerUserEntity) {
//
//        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
//        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiansuocaigouguanliduanAppId() + "&secret=" +
//                myAPPIDConfig.getLiansuocaigouguanliduanScreat() + "&js_code=" + distributerUserEntity.getGbDiuCode() +
//                "&grant_type=authorization_code";
//        // 发送请求，返回Json字符串
//        String str = WeChatUtil.httpRequest(url, "GET", null);
//        // 转成Json对象 获取openid
//        JSONObject jsonObject = JSONObject.parseObject(str);
//
//        // 我们需要的openid，在一个小程序中，openid是唯一的
//        String openid = jsonObject.get("openid").toString();
//
//        List<GbDistributerUserEntity> distributerUserEntities = gbDistributerUserService.queryUserByOpenId(openid);
//
//        if (distributerUserEntities.size() > 0) {
//            return R.error(-1, "用户已存在，请直接登陆");
//        } else {
//
//            //1 disuser save
//            distributerUserEntity.setGbDiuPrintDeviceId("-1");
//            distributerUserEntity.setGbDiuPrintBillDeviceId("-1");
//            distributerUserEntity.setGbDiuWxOpenId(openid);
//            distributerUserEntity.setGbDiuLoginTimes(0);
//            Integer disUserId = gbDistributerUserService.save(distributerUserEntity);
//
//            Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(disUserId);
//            return R.ok().put("data", stringObjectMap);
//
//        }
//    }


    /**
     * 保存
     */
    @ResponseBody
    @RequestMapping("/save")
//	@RequiresPermissions("gbDistributeruser:save")
    public R save(@RequestBody GbDistributerUserEntity gbDistributerUser) {
        gbDistributerUserService.save(gbDistributerUser);
        Integer gbDistributerUserId = gbDistributerUser.getGbDistributerUserId();
        GbDistributerUserEntity gbDistributerUserEntity = gbDistributerUserService.queryObject(gbDistributerUserId);

        return R.ok().put("data", gbDistributerUserEntity);
    }


    @RequestMapping(value = "/updateDisUserDeviceId", method = RequestMethod.POST)
    @ResponseBody
    public R updateDisUserDeviceId(@RequestBody GbDistributerUserEntity userEntity) {

        gbDistributerUserService.update(userEntity);

        return R.ok();
    }


    @RequestMapping(value = "/updateDisUser", method = RequestMethod.POST)
    @ResponseBody
    public R updateDisUser(String userName, Integer userId) {
        GbDistributerUserEntity gbDistributerUserEntity = gbDistributerUserService.queryObject(userId);
        gbDistributerUserEntity.setGbDiuWxNickName(userName);
        gbDistributerUserService.update(gbDistributerUserEntity);

        GbDistributerUserEntity gbDistributerUserEntity1 = gbDistributerUserService.queryUserInfo(userId);
        return R.ok().put("data", gbDistributerUserEntity1);
    }


    @RequestMapping(value = "/updateDisUserWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R updateDisUserWithFile(@RequestParam("file") MultipartFile file,
                                   @RequestParam("userName") String userName,
                                   @RequestParam("userId") Integer userId,
                                   HttpSession session) {

        GbDistributerUserEntity userEntity = gbDistributerUserService.queryObject(userId);
        String oldPath = userEntity.getGbDiuWxAvartraUrl();
        if (oldPath != null && !oldPath.trim().isEmpty()) {
            String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
            File file1 = new File(oldAbsolutePath);
            if (file1.exists()) {
                file1.delete();
            }
        }
        
        //1,上传图片
        String newUploadName = "uploadImage";
        String realPath = UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;
        GbDistributerUserEntity gbDistributerUserEntity = gbDistributerUserService.queryObject(userId);
        gbDistributerUserEntity.setGbDiuWxNickName(userName);
        gbDistributerUserEntity.setGbDiuWxAvartraUrl(filePath);
        gbDistributerUserEntity.setGbDiuUrlChange(1);
        gbDistributerUserService.update(gbDistributerUserEntity);

        return R.ok();

    }


}
