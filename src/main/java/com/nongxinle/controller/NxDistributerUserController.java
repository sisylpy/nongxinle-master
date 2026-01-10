package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.DateUtils.formatWhatFullTime;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbDepUserAdminWindowdinghuo;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;


@RestController
@RequestMapping("api/nxdistributeruser")
public class NxDistributerUserController {
    @Autowired
    private NxDistributerUserService nxDistributerUserService;
    @Autowired
    private QyNxDisCropUserService qyNxDisCropUserService;
    @Autowired
    private QyNxDisCorpService qyNxDisCorpService;
    @Autowired
    private NxWeightUserService nxWeightUserService;




    @RequestMapping(value = "/i7948FzJJ6.txt")
    @ResponseBody
    public String nxStaffRegister() { return "bb7a0c73e61112c45ebd6ad3743bb05e"; }

    @RequestMapping(value = "/changeDisUser/{userId}")
    @ResponseBody
    public R changeDisUser(@PathVariable Integer userId) {
        if(userId == 2){
            NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryObject(userId);
            String nxDiuWxOpenId = nxDistributerUserEntity.getNxDiuWxOpenId();
            nxDistributerUserEntity.setNxDiuWxOpenId(nxDiuWxOpenId+ "-");
            nxDistributerUserService.update(nxDistributerUserEntity);

            NxDistributerUserEntity nxDistributerUserEntity1 = nxDistributerUserService.queryObject(1);
            String nxDiuWxOpenId1 = nxDistributerUserEntity1.getNxDiuWxOpenId();
            String substring = nxDiuWxOpenId1.substring(0, nxDiuWxOpenId1.length() - 1);
            nxDistributerUserEntity1.setNxDiuWxOpenId(substring);
            nxDistributerUserService.update(nxDistributerUserEntity1);

        }
        if(userId == 1){
            NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryObject(userId);
            String nxDiuWxOpenId = nxDistributerUserEntity.getNxDiuWxOpenId();
            nxDistributerUserEntity.setNxDiuWxOpenId(nxDiuWxOpenId+ "-");
            nxDistributerUserService.update(nxDistributerUserEntity);

            NxDistributerUserEntity nxDistributerUserEntity1 = nxDistributerUserService.queryObject(2);
            String nxDiuWxOpenId1 = nxDistributerUserEntity1.getNxDiuWxOpenId();
            String substring = nxDiuWxOpenId1.substring(0, nxDiuWxOpenId1.length() - 1);
            nxDistributerUserEntity1.setNxDiuWxOpenId(substring);
            nxDistributerUserService.update(nxDistributerUserEntity1);

        }

        if(userId == 6){
            NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryObject(userId);
            String nxDiuWxOpenId = nxDistributerUserEntity.getNxDiuWxOpenId();
            nxDistributerUserEntity.setNxDiuWxOpenId(nxDiuWxOpenId+ "-");
            nxDistributerUserService.update(nxDistributerUserEntity);

            NxDistributerUserEntity nxDistributerUserEntity1 = nxDistributerUserService.queryObject(16);
            String nxDiuWxOpenId1 = nxDistributerUserEntity1.getNxDiuWxOpenId();
            String substring = nxDiuWxOpenId1.substring(0, nxDiuWxOpenId1.length() - 1);
            nxDistributerUserEntity1.setNxDiuWxOpenId(substring);
            nxDistributerUserService.update(nxDistributerUserEntity1);

        }
        if(userId == 16){
            NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryObject(userId);
            String nxDiuWxOpenId = nxDistributerUserEntity.getNxDiuWxOpenId();
            nxDistributerUserEntity.setNxDiuWxOpenId(nxDiuWxOpenId+ "-");
            nxDistributerUserService.update(nxDistributerUserEntity);

            NxDistributerUserEntity nxDistributerUserEntity1 = nxDistributerUserService.queryObject(6);
            String nxDiuWxOpenId1 = nxDistributerUserEntity1.getNxDiuWxOpenId();
            String substring = nxDiuWxOpenId1.substring(0, nxDiuWxOpenId1.length() - 1);
            nxDistributerUserEntity1.setNxDiuWxOpenId(substring);
            nxDistributerUserService.update(nxDistributerUserEntity1);

        }

        //
        if(userId == 8){
            NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryObject(userId);
            String nxDiuWxOpenId = nxDistributerUserEntity.getNxDiuWxOpenId();
            nxDistributerUserEntity.setNxDiuWxOpenId(nxDiuWxOpenId+ "-");
            nxDistributerUserService.update(nxDistributerUserEntity);

            NxDistributerUserEntity nxDistributerUserEntity1 = nxDistributerUserService.queryObject(18);
            String nxDiuWxOpenId1 = nxDistributerUserEntity1.getNxDiuWxOpenId();
            String substring = nxDiuWxOpenId1.substring(0, nxDiuWxOpenId1.length() - 1);
            nxDistributerUserEntity1.setNxDiuWxOpenId(substring);
            nxDistributerUserService.update(nxDistributerUserEntity1);

        }
        if(userId == 18){
            NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryObject(userId);
            String nxDiuWxOpenId = nxDistributerUserEntity.getNxDiuWxOpenId();
            nxDistributerUserEntity.setNxDiuWxOpenId(nxDiuWxOpenId+ "-");
            nxDistributerUserService.update(nxDistributerUserEntity);

            NxDistributerUserEntity nxDistributerUserEntity1 = nxDistributerUserService.queryObject(8);
            String nxDiuWxOpenId1 = nxDistributerUserEntity1.getNxDiuWxOpenId();
            String substring = nxDiuWxOpenId1.substring(0, nxDiuWxOpenId1.length() - 1);
            nxDistributerUserEntity1.setNxDiuWxOpenId(substring);
            nxDistributerUserService.update(nxDistributerUserEntity1);

        }

        //
        if(userId == 11){
            NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryObject(userId);
            String nxDiuWxOpenId = nxDistributerUserEntity.getNxDiuWxOpenId();
            nxDistributerUserEntity.setNxDiuWxOpenId(nxDiuWxOpenId+ "-");
            nxDistributerUserService.update(nxDistributerUserEntity);

            NxDistributerUserEntity nxDistributerUserEntity1 = nxDistributerUserService.queryObject(19);
            String nxDiuWxOpenId1 = nxDistributerUserEntity1.getNxDiuWxOpenId();
            String substring = nxDiuWxOpenId1.substring(0, nxDiuWxOpenId1.length() - 1);
            nxDistributerUserEntity1.setNxDiuWxOpenId(substring);
            nxDistributerUserService.update(nxDistributerUserEntity1);

        }
        if(userId == 19){
            NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryObject(userId);
            String nxDiuWxOpenId = nxDistributerUserEntity.getNxDiuWxOpenId();
            nxDistributerUserEntity.setNxDiuWxOpenId(nxDiuWxOpenId+ "-");
            nxDistributerUserService.update(nxDistributerUserEntity);

            NxDistributerUserEntity nxDistributerUserEntity1 = nxDistributerUserService.queryObject(11);
            String nxDiuWxOpenId1 = nxDistributerUserEntity1.getNxDiuWxOpenId();
            String substring = nxDiuWxOpenId1.substring(0, nxDiuWxOpenId1.length() - 1);
            nxDistributerUserEntity1.setNxDiuWxOpenId(substring);
            nxDistributerUserService.update(nxDistributerUserEntity1);

        }

        //万里星空
        if(userId == 9){
            NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryObject(userId);
            String nxDiuWxOpenId = nxDistributerUserEntity.getNxDiuWxOpenId();
            nxDistributerUserEntity.setNxDiuWxOpenId(nxDiuWxOpenId+ "-");
            nxDistributerUserService.update(nxDistributerUserEntity);

            NxDistributerUserEntity nxDistributerUserEntity1 = nxDistributerUserService.queryObject(79);
            String nxDiuWxOpenId1 = nxDistributerUserEntity1.getNxDiuWxOpenId();
            String substring = nxDiuWxOpenId1.substring(0, nxDiuWxOpenId1.length() - 1);
            nxDistributerUserEntity1.setNxDiuWxOpenId(substring);
            nxDistributerUserService.update(nxDistributerUserEntity1);

        }
        if(userId == 79){
            NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryObject(userId);
            String nxDiuWxOpenId = nxDistributerUserEntity.getNxDiuWxOpenId();
            nxDistributerUserEntity.setNxDiuWxOpenId(nxDiuWxOpenId+ "-");
            nxDistributerUserService.update(nxDistributerUserEntity);

            NxDistributerUserEntity nxDistributerUserEntity1 = nxDistributerUserService.queryObject(9);
            String nxDiuWxOpenId1 = nxDistributerUserEntity1.getNxDiuWxOpenId();
            String substring = nxDiuWxOpenId1.substring(0, nxDiuWxOpenId1.length() - 1);
            nxDistributerUserEntity1.setNxDiuWxOpenId(substring);
            nxDistributerUserService.update(nxDistributerUserEntity1);

        }


        return R.ok();
    }


    @RequestMapping(value = "/disPurchaserUserLogin", method = RequestMethod.POST)
    @ResponseBody
    public R disPurchaserUserLogin(String code) {

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
            NxDistributerUserEntity depUserEntity = nxDistributerUserService.queryUserByOpenId(openId);
            if (depUserEntity != null) {
                return R.ok().put("data", depUserEntity);
            } else {
                return R.error(-1, "请向管理员索要注册邀请");
            }

        } else {
            return R.error(-1, "请进行注册");
        }
    }



    @RequestMapping(value = "/disPurchaserRegisterWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R disPurchaserRegisterWithFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("userName") String userName,
                                         @RequestParam("code") String code,
                                         @RequestParam("disId") Integer disId,
                                         HttpSession session) {
        System.out.println("dissve" + file);

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

        //添加新用户
        NxDistributerUserEntity nxDistributerUserEntity = new NxDistributerUserEntity();
        nxDistributerUserEntity.setNxDiuWxOpenId(openid);
        nxDistributerUserEntity.setNxDiuDistributerId(disId);
        nxDistributerUserEntity.setNxDiuAdmin(getNxDisUserPurchase());
        nxDistributerUserEntity.setNxDiuPrintBillDeviceId("-1");
        nxDistributerUserEntity.setNxDiuPrintDeviceId("-1");
        nxDistributerUserEntity.setNxDiuLoginTimes(0);

        //1,上传图片
        String newUploadName = "uploadImage";
        UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;
        nxDistributerUserEntity.setNxDiuWxNickName(userName);
        nxDistributerUserEntity.setNxDiuWxAvartraUrl(filePath);
        nxDistributerUserEntity.setNxDiuUrlChange(1);
        nxDistributerUserService.save(nxDistributerUserEntity);


        return R.ok();

    }

    

    @RequestMapping(value = "/disDriverUserLogin", method = RequestMethod.POST)
    @ResponseBody
    public R disDriverUserLogin (@RequestBody NxDistributerUserEntity distributerUserEntity ) {
        System.out.println(distributerUserEntity);

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiziDriverAppID() + "&secret=" +
                myAPPIDConfig.getLiziDriverScreat() + "&js_code=" + distributerUserEntity.getNxDiuCode() +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        Map<String, Object> map = new HashMap<>();
        map.put("openId", openid);
        map.put("roleId", 5);
        System.out.println(map);
        NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryDisUserByRoleAndOpen(map);

        if(nxDistributerUserEntity != null){
            Integer distributerUserId = nxDistributerUserEntity.getNxDistributerUserId();
            Map<String, Object> stringObjectMap = nxDistributerUserService.queryNxDisAndUserInfo(distributerUserId);
            return R.ok().put("data", stringObjectMap);
        }else {
            return R.error(-1,"用户不存在");
        }
    }

    @RequestMapping(value = "/disUserDriverSave", method = RequestMethod.POST)
    @ResponseBody
    public R disUserDriverSave (@RequestBody NxDistributerUserEntity user) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiziDriverAppID() + "&secret=" +
                myAPPIDConfig.getLiziDriverScreat() + "&js_code=" + user.getNxDiuCode() +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openId = jsonObject.get("openid").toString();

        Map<String, Object> map1 = new HashMap<>();
        map1.put("openId", openId);
        map1.put("roleId", 5);
        NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryDisUserByRoleAndOpen(map1);
        if(nxDistributerUserEntity != null){
            return R.error(-1,"请直接登陆");
        }else{
            //添加新用户
            user.setNxDiuWxOpenId(openId);
            nxDistributerUserService.save(user);
            Integer disUserId = user.getNxDistributerUserId();
            Map<String, Object> stringObjectMap = nxDistributerUserService.queryNxDisAndUserInfo(disUserId);
            return R.ok().put("data",stringObjectMap);
        }
    }

    
//    @RequestMapping(value = "/i7948FzJJ6.txt")
//    @ResponseBody
//    public String nxStaffRegister() { return "bb7a0c73e61112c45ebd6ad3743bb05e"; }
//


    @RequestMapping(value = "/getDisUserInfo/{userId}")
    @ResponseBody
    public R getDisUserInfo(@PathVariable Integer userId) {
        NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryUserInfo(userId);
        return R.ok().put("data", nxDistributerUserEntity);
    }

    /**yishangAndUserSave
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
        httpHeaders.add("Content-Disposition", "attachment; filename=" + value + ".mp3");
        httpHeaders.add("Content-Type", "audio/mpeg");
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
        return responseEntity;
    }


    @RequestMapping(value = "/getDisUsers/{disId}")
    @ResponseBody
    public R getDisUsers(@PathVariable Integer disId) {


        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("admin",  getNxDisUserAdmin());
        System.out.println("aabc111" + map);
        List<NxDistributerUserEntity> zeroList = nxDistributerUserService.getAdminUserByParams(map);
        map.put("admin",  getNxDisUserStaff());
        System.out.println("aabc222" + map);
        List<NxDistributerUserEntity> oneList = nxDistributerUserService.getAdminUserByParams(map);
        zeroList.addAll(oneList);

        //        List<NxDistributerUserEntity> weighter = nxDistributerUserService.getAdminUserByParams(map);

        List<NxWeightUserEntity> nxWeightUserEntities = nxWeightUserService.queryUsersByDistributerId(disId);

//        map.put("admin",  getNxDisUserKufng());
//        List<NxDistributerUserEntity> kufang = nxDistributerUserService.getAdminUserByParams(map);
//        map.put("admin",  getNxDisUserDriver());
//        List<NxDistributerUserEntity> diriver = nxDistributerUserService.getAdminUserByParams(map);

        Map<String, Object> mapRe = new HashMap<>();
        mapRe.put("zero", zeroList);
        mapRe.put("one",  nxWeightUserEntities);
//        mapRe.put("two",  kufang);
//        mapRe.put("three",  diriver);

        return R.ok().put("data", mapRe);
    }

    @RequestMapping(value = "/updateDisUserAdmin", method = RequestMethod.POST)
    @ResponseBody
    public R updateDisUserAdmin (@RequestBody NxDistributerUserEntity user) {
        if(user.getNxDiuAdmin() == 1){
            Map<String, Object> map = new HashMap<>();
            map.put("disId", user.getNxDiuDistributerId());
            map.put("admin", 0);
            List<NxDistributerUserEntity> userEntities = nxDistributerUserService.queryRoleNxDisRoleUserList(map);
            if(userEntities.size() < 2){
                return R.error(-1,"必须有一个超级管理员");
            }else{
                nxDistributerUserService.update(user);
                return R.ok();
            }
        }else{
            nxDistributerUserService.update(user);
            return R.ok();
        }
    }

    @RequestMapping(value = "/deleteDisUser/{userId}")
    @ResponseBody
    public R deleteDisUser(@PathVariable Integer userId) {
        nxDistributerUserService.delete(userId);
        return R.ok();
    }


    @RequestMapping(value = "/deleteWeightUser/{userId}")
    @ResponseBody
    public R deleteWeightUser(@PathVariable Integer userId) {
        nxWeightUserService.delete(userId);
        return R.ok();
    }



    @RequestMapping(value = "/disAndroidLoginWork/{phone}")
    @ResponseBody
    public R disAndroidLoginWork(@PathVariable  String phone) {
        System.out.println("ohoneee" + phone);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByPhone(phone);
        if (distributerUser != null) {
            Integer nxDistributerUserId = distributerUser.getNxDistributerUserId();
            Map<String, Object> stringObjectMap = nxDistributerUserService.queryNxDisAndUserInfo(nxDistributerUserId);

            System.out.println("mapapapappa" + stringObjectMap);
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
        }
    }

    @RequestMapping(value = "/wxworkLogin", method = RequestMethod.POST)
    @ResponseBody
    public R disLoginWork(@RequestBody  NxDistributerUserEntity distributerUserEntity) {

        String suiteToken = getWxProperty(Constant.SUITE_TOKEN);
        String code = distributerUserEntity.getNxDiuCode();
        String userCropUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/miniprogram/jscode2session?suite_access_token="+suiteToken+"&js_code="+code+"&grant_type=authorization_code";

        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(userCropUrl, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println("jsonObjectjsonObject" + jsonObject);
        String openUserId = jsonObject.getString("open_userid");

        // 我们需要的openid，在一个小程序中，openid是唯一的
        NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openUserId);
        if (distributerUser != null) {
            String sessionKey = jsonObject.getString("session_key");
            Integer nxDiuQyCorpUserId = distributerUser.getNxDiuQyCorpUserId();
            QyNxDisCorpUserEntity qyNxDisCorpUserEntity = qyNxDisCropUserService.queryObject(nxDiuQyCorpUserId);
            qyNxDisCorpUserEntity.setQyNxDisCorpSessionKey(sessionKey);
            qyNxDisCropUserService.update(qyNxDisCorpUserEntity);

            Integer nxDistributerUserId = distributerUser.getNxDistributerUserId();
            Map<String, Object> stringObjectMap = nxDistributerUserService.queryNxDisAndUserInfo(nxDistributerUserId);
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
        }
    }


    @RequestMapping(value = "/workLogin/{code}", method = RequestMethod.GET)
    @ResponseBody
    public R workLogin(@PathVariable String code) {
        String suiteToken = getWxProperty(Constant.SUITE_TOKEN);
        String userCropUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/miniprogram/jscode2session?suite_access_token="+suiteToken+"&js_code="+code+"&grant_type=authorization_code";

        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(userCropUrl, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println("jsonObjectjsonObject" + jsonObject);
        String openUserId = jsonObject.getString("open_userid");
        String corpId = jsonObject.getString("corpid");
        String sessionKey = jsonObject.getString("session_key");

        //查询是否新用户
        QyNxDisCorpUserEntity qyNxDisCorpUserEntity = qyNxDisCropUserService.queryQyUserByUserId(openUserId);
         if(qyNxDisCorpUserEntity != null){
             test(qyNxDisCorpUserEntity,openUserId);
             return R.ok().put("data", qyNxDisCorpUserEntity);
         }else{
            QyNxDisCorpEntity qyNxDisCorpEntity =  qyNxDisCorpService.queryQyCropByCropId(corpId);
            if(qyNxDisCorpEntity != null){
                Integer qyNxDisQyCorpId = qyNxDisCorpEntity.getQyNxDisCorpId();
                List<QyNxDisCorpUserEntity> qyNxDisCorpUserEntities = qyNxDisCropUserService.queryCorpUserListByCorpId(qyNxDisQyCorpId);
                if(qyNxDisCorpUserEntities.size() > 0){
                    Integer qyNxDistributerId = qyNxDisCorpUserEntities.get(0).getQyNxDistributerId();
                    return R.error(-1,qyNxDistributerId + "," + qyNxDisQyCorpId);
                }else{
                    return R.error(-1,"企业没有用户");
                }

//                QyNxDisCorpUserEntity userEntity = new QyNxDisCorpUserEntity();
//                userEntity.setQyNxDisCorpOpenUserId(openUserId);
//                userEntity.setQyNxDisCorpQyCorpId(qyNxDisQyCorpId);
//                userEntity.setQyNxDisCorpSessionKey(sessionKey);
//
//                userEntity.setQyNxDisCorpSessionKey(sessionKey);
//
//                qyNxDisCropUserService.save(userEntity);
//                test(userEntity,openUserId);
//                return R.ok().put("data",userEntity);


            }else{
                return R.error(-1,"企业没有注册成功");
            }

         }

    }



    private JSONObject test(QyNxDisCorpUserEntity userEntity, String openUserId){

        QyNxDisCorpEntity qyNxDisCorpEntity = qyNxDisCorpService.queryObject(userEntity.getQyNxDisCorpQyCorpId());
        String qyNxDisCorpAccessToken = qyNxDisCorpEntity.getQyNxDisCorpAccessToken();

        //2获取通讯录列表
        String dep = "https://qyapi.weixin.qq.com/cgi-bin/department/simplelist?access_token=" + qyNxDisCorpAccessToken;
        String depUrl = WeChatUtil.httpRequest(dep, "GET", null);
        JSONObject jsonObjectDep = JSONObject.parseObject(depUrl);
        System.out.println("通讯录Test===jsonObjectDep" + jsonObjectDep);
        return jsonObjectDep;

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
    @RequestMapping(value = "/disLogin", method = RequestMethod.POST)
    @ResponseBody
    public R disLogin(@RequestBody NxDistributerUserEntity distributerUserEntity) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongAppID() + "&secret=" +
                myAPPIDConfig.getTexiansongScreat() + "&js_code=" + distributerUserEntity.getNxDiuCode() +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openid);
        if (distributerUser != null) {
            Integer nxDistributerUserId = distributerUser.getNxDistributerUserId();
            Map<String, Object> stringObjectMap = nxDistributerUserService.queryNxDisAndUserInfo(nxDistributerUserId);
            distributerUser.setNxDiuLoginTimes( distributerUser.getNxDiuLoginTimes() + 1);
            System.out.println("timememmemssss" + distributerUser.getNxDiuLoginTimes());
            nxDistributerUserService.update(distributerUser);
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
        }
    }

    @RequestMapping(value = "/printDisLogin", method = RequestMethod.POST)
    @ResponseBody
    public R printDisLogin(String sessionId,  String code) {
        System.out.println("cosisisidi" +sessionId + "coe==" + code);

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongAppID() + "&secret=" +
                myAPPIDConfig.getTexiansongScreat() + "&js_code=" + code +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openid);
        if (distributerUser != null) {
            Integer nxDistributerUserId = distributerUser.getNxDistributerUserId();
            Map<String, Object> stringObjectMap = nxDistributerUserService.queryNxDisAndUserInfo(nxDistributerUserId);
            distributerUser.setNxDiuLoginTimes( distributerUser.getNxDiuLoginTimes() + 1);
            System.out.println("timememmemssss" + distributerUser.getNxDiuLoginTimes());
            nxDistributerUserService.update(distributerUser);
            // 生成唯一的 sessionId（可以用 UUID）
//            String sessionId = UUID.randomUUID().toString();  // 生成一个唯一的 sessionId

//            storeSession(sessionId, distributerUser);
            distributerUser.setNxDiuWxPhone(sessionId);
            nxDistributerUserService.update(distributerUser);
            return R.ok().put("data", stringObjectMap).put("sessionId", sessionId);

        } else {
            return R.error(-1, "用户不存在");
        }
    }

    @RequestMapping(value = "/checkLoginStatus/{sessionId}")
    @ResponseBody
    public R checkLoginStatus(@PathVariable String sessionId) {
        // 根据 openId 查询用户是否登录
        NxDistributerUserEntity user = nxDistributerUserService.queryUserByPhone(sessionId);

        if (user != null) {
            return R.ok().put("loggedIn", true).put("user", user);
        } else {
            return R.ok().put("loggedIn", false);
        }
    }

    @RequestMapping(value = "/disLoginKf", method = RequestMethod.POST)
    @ResponseBody
    public R disLoginKf(@RequestBody NxDistributerUserEntity distributerUserEntity) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getShanghuoAppID() + "&secret=" +
                myAPPIDConfig.getShanghuoScreat() + "&js_code=" + distributerUserEntity.getNxDiuCode() +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openid);
        if (distributerUser != null) {
            Integer nxDistributerUserId = distributerUser.getNxDistributerUserId();
            Map<String, Object> stringObjectMap = nxDistributerUserService.queryNxDisAndUserInfo(nxDistributerUserId);
            distributerUser.setNxDiuLoginTimes( distributerUser.getNxDiuLoginTimes() + 1);
            System.out.println("timememmemssss" + distributerUser.getNxDiuLoginTimes());
            nxDistributerUserService.update(distributerUser);
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
        }
    }

    /**
     * 称重员工登录接口（支持配送商和供货商两种类型）
     * @param weightUserEntity 称重员工实体（包含微信登录code）
     * @return 登录结果，包含用户信息和配送商/供货商信息
     */
    @RequestMapping(value = "/weighterLoginKf", method = RequestMethod.POST)
    @ResponseBody
    public R weighterLoginKf(@RequestBody NxWeightUserEntity weightUserEntity) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getShanghuoAppID() + "&secret=" +
                myAPPIDConfig.getShanghuoScreat() + "&js_code=" + weightUserEntity.getNxWuLoginCode() +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 检查是否有错误
        if (jsonObject.containsKey("errcode")) {
            return R.error(-1, "微信登录失败：" + jsonObject.getString("errmsg"));
        }

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        
        // 查询称重员工
        NxWeightUserEntity weightUser = nxWeightUserService.queryUserByOpenId(openid);
        if (weightUser == null) {
            return R.error(-1, "用户不存在，请先注册");
        }

        // 检查用户状态
        if (weightUser.getNxWuStatus() != null && weightUser.getNxWuStatus() == 0) {
            return R.error(-1, "用户已被禁用，请联系管理员");
        }

        // 更新登录次数
        Integer loginTimes = weightUser.getNxWuLoginTimes();
        if (loginTimes == null) {
            loginTimes = 0;
        }
        weightUser.setNxWuLoginTimes(loginTimes + 1);
        nxWeightUserService.update(weightUser);

        // 查询用户及其关联的配送商或供货商信息
        Map<String, Object> userInfoMap = nxWeightUserService.queryWeightUserAndInfo(weightUser.getNxWeightUserId());
        
        if (userInfoMap == null) {
            return R.error(-1, "查询用户信息失败");
        }

        return R.ok().put("data", userInfoMap);
    }

    @RequestMapping(value = "/disLoginAdmin", method = RequestMethod.POST)
    @ResponseBody
    public R disLoginAdmin(@RequestBody NxDistributerUserEntity distributerUserEntity) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongAdminAppID() + "&secret=" +
                myAPPIDConfig.getTexiansongAdminScreat() + "&js_code=" + distributerUserEntity.getNxDiuCode() +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openid);
        if (distributerUser != null) {
            Integer nxDistributerUserId = distributerUser.getNxDistributerUserId();
            Map<String, Object> stringObjectMap = nxDistributerUserService.queryNxDisAndUserInfo(nxDistributerUserId);
            distributerUser.setNxDiuLoginTimes( distributerUser.getNxDiuLoginTimes() + 1);
            System.out.println("timememmemssss" + distributerUser.getNxDiuLoginTimes());
            nxDistributerUserService.update(distributerUser);
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
        }
    }



    /**
     * 批发商新管理者注册
     * @param distributerUserEntity 批发商用户disUserSave
     * @return 批发商
     */
    @RequestMapping(value = "/disUserSaveAdmin", method = RequestMethod.POST)
    @ResponseBody
    public R disUserSaveAdmin(@RequestBody NxDistributerUserEntity distributerUserEntity) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongAdminAppID() + "&secret=" +
                myAPPIDConfig.getTexiansongAdminScreat() + "&js_code=" + distributerUserEntity.getNxDiuCode() +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openid);
        if (distributerUser != null) {
            return R.error(-1, "用户已存在，请直接登陆");
        } else {

            distributerUserEntity.setNxDiuPrintDeviceId("-1");
            distributerUserEntity.setNxDiuPrintBillDeviceId("-1");
            distributerUserEntity.setNxDiuLoginTimes(0);
            distributerUserEntity.setNxDiuWxOpenId(openid);
            distributerUserEntity.setNxDiuAdmin(getNxDisUserKufng());
            nxDistributerUserService.save(distributerUserEntity);
            Integer nxDistributerUserId = distributerUserEntity.getNxDistributerUserId();
            Map<String, Object> stringObjectMap = nxDistributerUserService.queryNxDisAndUserInfo(nxDistributerUserId);
            return R.ok().put("data", stringObjectMap);
        }
    }


    /**
     * 批发商新管理者注册
     * @param distributerUserEntity 批发商用户disUserSave
     * @return 批发商
     */
    @RequestMapping(value = "/disUserSave", method = RequestMethod.POST)
    @ResponseBody
    public R disUserSave(@RequestBody NxDistributerUserEntity distributerUserEntity) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongAppID() + "&secret=" +
                myAPPIDConfig.getTexiansongScreat() + "&js_code=" + distributerUserEntity.getNxDiuCode() +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openid);
        if (distributerUser != null) {
            return R.error(-1, "用户已存在，请直接登陆");
        } else {

            distributerUserEntity.setNxDiuPrintDeviceId("-1");
            distributerUserEntity.setNxDiuPrintBillDeviceId("-1");
            distributerUserEntity.setNxDiuLoginTimes(0);
            distributerUserEntity.setNxDiuWxOpenId(openid);
            nxDistributerUserService.save(distributerUserEntity);
            System.out.println(distributerUserEntity);
            System.out.println("distributerUserEntitydistributerUserEntity");
            Integer nxDistributerUserId = distributerUserEntity.getNxDistributerUserId();
            Map<String, Object> stringObjectMap = nxDistributerUserService.queryNxDisAndUserInfo(nxDistributerUserId);
            return R.ok().put("data", stringObjectMap);
        }
    }

   @RequestMapping(value = "/disUserSaveWithFileWork", method = RequestMethod.POST)
   @ResponseBody
   public R disUserSaveWithFileWork (String userName,String userUrl,  String code, Integer disId, Integer corpId ) {
       String suiteToken = getWxProperty(Constant.SUITE_TOKEN);
       String userCropUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/miniprogram/jscode2session?suite_access_token="+suiteToken
               +"&js_code="+code+"&grant_type=authorization_code";
//
       // 发送请求，返回Json字符串
       String str = WeChatUtil.httpRequest(userCropUrl, "GET", null);
       // 转成Json对象 获取openid
       JSONObject jsonObject = JSONObject.parseObject(str);
       System.out.println("jsonObjectjsonObjectwwwww" + jsonObject);
       String openUserId = jsonObject.getString("open_userid");
       System.out.println("openeidiid" + openUserId);
       NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openUserId);
       if (distributerUser != null) {
           return R.error(-1, "用户已存在，请直接登陆");
       } else {
           NxDistributerUserEntity distributerUserEntity = new NxDistributerUserEntity();
           distributerUserEntity.setNxDiuPrintDeviceId("-1");
           distributerUserEntity.setNxDiuPrintBillDeviceId("-1");
           distributerUserEntity.setNxDiuLoginTimes(0);
           distributerUserEntity.setNxDiuWxOpenId(openUserId);
            distributerUserEntity.setNxDiuWxAvartraUrl(userUrl);
           distributerUserEntity.setNxDiuUrlChange(1);
           distributerUserEntity.setNxDiuWxNickName(userName);
           distributerUserEntity.setNxDiuDistributerId(disId);

           nxDistributerUserService.save(distributerUserEntity);

           // 3，如果没有注册过
           String sessionKey = jsonObject.getString("session_key");
           String userPinyin = jsonObject.getString("userid");
           QyNxDisCorpUserEntity userEntity = new QyNxDisCorpUserEntity();
           userEntity.setQyNxDisCorpOpenUserId(openUserId);
           userEntity.setQyNxDisCorpQyCorpId(corpId);
           userEntity.setQyNxDisCorpSessionKey(sessionKey);
           userEntity.setQyNxDistributerId(disId);
           userEntity.setQyNxDisCorpUserName(userPinyin);
            userEntity.setQyNxDisCorpUserUrl(userUrl);
           userEntity.setQyNxDisCorpUserJoinDate(formatWhatFullTime(0));
           qyNxDisCropUserService.save(userEntity);

           distributerUserEntity.setNxDiuQyCorpUserId(userEntity.getQyNxDisCorpUserId());
           nxDistributerUserService.update(distributerUserEntity);

           return R.ok();
       }
   }



    /**
     * 门店管理端，采购端，库房端注册用户
     * @param  @RequestParam("file") MultipartFile file,
     * @return 用户
     */
    @RequestMapping(value = "/disUserSaveWithFileWork1",produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R disUserSaveWithFileWork1(
                                 @RequestParam("userName") String userName,
                                 @RequestParam("code") String code,
                                 @RequestParam("disId") Integer disId,
                                     @RequestParam("corpId") Integer corpId,
                                 HttpSession session) {

       String suiteToken = getWxProperty(Constant.SUITE_TOKEN);
		String userCropUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/miniprogram/jscode2session?suite_access_token="+suiteToken
				+"&js_code="+code+"&grant_type=authorization_code";
//
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(userCropUrl, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);
		System.out.println("jsonObjectjsonObjectwwwww" + jsonObject);
		String openUserId = jsonObject.getString("open_userid");
		System.out.println("openeidiid" + openUserId);
		NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openUserId);
        if (distributerUser != null) {
            return R.error(-1, "用户已存在，请直接登陆");
        } else {
            NxDistributerUserEntity distributerUserEntity = new NxDistributerUserEntity();
            distributerUserEntity.setNxDiuPrintDeviceId("-1");
            distributerUserEntity.setNxDiuPrintBillDeviceId("-1");
            distributerUserEntity.setNxDiuLoginTimes(0);
            distributerUserEntity.setNxDiuWxOpenId(openUserId);
            String newUploadName = "uploadImage";
//            String realPath = UploadFile.upload(session, newUploadName, file);
//            String filename = file.getOriginalFilename();
//            String filePath = newUploadName + "/" + filename;
//            distributerUserEntity.setNxDiuWxAvartraUrl(filePath);
            distributerUserEntity.setNxDiuUrlChange(1);
            distributerUserEntity.setNxDiuWxNickName(userName);
            distributerUserEntity.setNxDiuDistributerId(disId);
            nxDistributerUserService.save(distributerUserEntity);

            // 3，如果没有注册过
            String sessionKey = jsonObject.getString("session_key");
            String userPinyin = jsonObject.getString("userid");
            QyNxDisCorpUserEntity userEntity = new QyNxDisCorpUserEntity();
            userEntity.setQyNxDisCorpOpenUserId(openUserId);
            userEntity.setQyNxDisCorpQyCorpId(corpId);
            userEntity.setQyNxDisCorpSessionKey(sessionKey);
            userEntity.setQyNxDistributerId(disId);
            userEntity.setQyNxDisCorpUserName(userPinyin);
//            userEntity.setQyNxDisCorpUserUrl(filePath);
            userEntity.setQyNxDisCorpUserJoinDate(formatWhatFullTime(0));
            qyNxDisCropUserService.save(userEntity);

            distributerUserEntity.setNxDiuQyCorpUserId(userEntity.getQyNxDisCorpUserId());
            nxDistributerUserService.update(distributerUserEntity);
            return R.ok();
        }



    }


    /**
     * 门店管理端，采购端，库房端注册用户
     * @param
     * @return 用户
     */
    @RequestMapping(value = "/disUserSaveWithFile",produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R disUserSaveWithFile(@RequestParam("file") MultipartFile file,
                                               @RequestParam("userName") String userName,
                                               @RequestParam("phone") String phone,
                                               @RequestParam("code") String code,
                                               @RequestParam("disId") Integer disId,
                                               @RequestParam("admin") Integer admin,
                                 HttpSession session) {

        System.out.println("fifiifieieieiee" + phone + "admin" + admin);
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongAppID() + "&secret=" +
                myAPPIDConfig.getTexiansongScreat() + "&js_code=" + code +  "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openid);
        if (distributerUser != null) {
            return R.error(-1, "用户已存在，请直接登陆");
        } else {
            NxDistributerUserEntity distributerUserEntity = new NxDistributerUserEntity();
            distributerUserEntity.setNxDiuPrintDeviceId("-1");
            distributerUserEntity.setNxDiuPrintBillDeviceId("-1");
            distributerUserEntity.setNxDiuLoginTimes(0);
            distributerUserEntity.setNxDiuWxOpenId(openid);
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            distributerUserEntity.setNxDiuWxAvartraUrl(filePath);
            distributerUserEntity.setNxDiuUrlChange(1);
            distributerUserEntity.setNxDiuWxOpenId(openid);
            distributerUserEntity.setNxDiuPrintDeviceId("-1");
            distributerUserEntity.setNxDiuPrintBillDeviceId("-1");
            distributerUserEntity.setNxDiuWxNickName(userName);
            distributerUserEntity.setNxDiuDistributerId(disId);
            distributerUserEntity.setNxDiuAdmin(admin);
            distributerUserEntity.setNxDiuWxPhone(phone);
            nxDistributerUserService.save(distributerUserEntity);
            System.out.println("phoneoeheoeneoe"+phone);

            return R.ok();
        }



    }


//    @RequestMapping(value = "/disUserSaveWithFileAdmin",produces = "text/html;charset=UTF-8")
//    @ResponseBody
//    public R disUserSaveWithFileAdmin(@RequestParam("file") MultipartFile file,
//                                 @RequestParam("userName") String userName,
//                                 @RequestParam("code") String code,
//                                 @RequestParam("disId") Integer disId,
//                                 @RequestParam("admin") Integer admin,
//                                 HttpSession session) {
//
//        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
//        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongAdminAppID() + "&secret=" +
//                myAPPIDConfig.getTexiansongAdminScreat() + "&js_code=" + code +  "&grant_type=authorization_code";
//        // 发送请求，返回Json字符串
//        String str = WeChatUtil.httpRequest(url, "GET", null);
//        // 转成Json对象 获取openid
//        JSONObject jsonObject = JSONObject.parseObject(str);
//
//        // 我们需要的openid，在一个小程序中，openid是唯一的
//        String openid = jsonObject.get("openid").toString();
//        NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openid);
//        if (distributerUser != null) {
//            return R.error(-1, "用户已存在，请直接登陆");
//        } else {
//            NxDistributerUserEntity distributerUserEntity = new NxDistributerUserEntity();
//            distributerUserEntity.setNxDiuPrintDeviceId("-1");
//            distributerUserEntity.setNxDiuPrintBillDeviceId("-1");
//            distributerUserEntity.setNxDiuLoginTimes(0);
//            distributerUserEntity.setNxDiuWxOpenId(openid);
//            String newUploadName = "uploadImage";
//            String realPath = UploadFile.upload(session, newUploadName, file);
//
//            String filename = file.getOriginalFilename();
//            String filePath = newUploadName + "/" + filename;
//            distributerUserEntity.setNxDiuWxAvartraUrl(filePath);
//            distributerUserEntity.setNxDiuUrlChange(1);
//            distributerUserEntity.setNxDiuWxOpenId(openid);
//            distributerUserEntity.setNxDiuPrintDeviceId("-1");
//            distributerUserEntity.setNxDiuPrintBillDeviceId("-1");
//            distributerUserEntity.setNxDiuWxNickName(userName);
//            distributerUserEntity.setNxDiuDistributerId(disId);
//            distributerUserEntity.setNxDiuAdmin(admin);
//            nxDistributerUserService.save(distributerUserEntity);
//
//            return R.ok();
//        }
//
//    }

    /**
     * 称重员工注册接口（支持配送商和供货商两种类型）
     * @param file 头像文件
     * @param userName 用户昵称
     * @param code 微信登录code
     * @param userType 用户类型：1=配送商员工, 2=供货商员工
     * @param disId 配送商ID（当userType=1时必填）
     * @param session HttpSession
     * @return 注册结果
     */
    @RequestMapping(value = "/weighterKfUserSaveWithFile",produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R weighterKfUserSaveWithFile(@RequestParam("file") MultipartFile file,
                                 @RequestParam("userName") String userName,
                                 @RequestParam("code") String code,
                                 @RequestParam(value = "userType", defaultValue = "1") Integer userType,
                                 @RequestParam(value = "disId", required = false) Integer disId,
                                 @RequestParam(value = "userId", required = false) Integer userId,
                                 HttpSession session) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getShanghuoAppID() + "&secret=" +
                myAPPIDConfig.getShanghuoScreat() + "&js_code=" + code +  "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 检查是否有错误
        if (jsonObject.containsKey("errcode")) {
            return R.error(-1, "微信登录失败：" + jsonObject.getString("errmsg"));
        }

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        
        // 检查称重员工是否已存在（通过openid）
        NxWeightUserEntity existWeightUser = nxWeightUserService.queryUserByOpenId(openid);
        if (existWeightUser != null) {
            return R.error(-1, "用户已存在，请直接登录");
        }
        
        // 同时检查配送商用户是否已存在（保持兼容性）
        NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openid);
        if (distributerUser != null) {
            return R.error(-1, "用户已存在，请直接登录");
        }

        // 验证参数
        if (userType == 1 && disId == null) {
            return R.error(-1, "配送商员工注册必须提供配送商ID");
        }
        if (userType == 2 && userId == null) {
            return R.error(-1, "供货商员工注册必须提供供货商ID");
        }
        if (userType != 1 && userType != 2) {
            return R.error(-1, "用户类型错误，必须是1（配送商员工）或2（供货商员工）");
        }

        // 创建称重员工实体
        NxWeightUserEntity weightUserEntity = new NxWeightUserEntity();
        
        // 设置基本信息
        weightUserEntity.setNxWuUserType(userType);
        weightUserEntity.setNxWuWxOpenId(openid);
        weightUserEntity.setNxWuWxNickName(userName);
        weightUserEntity.setNxWuLoginTimes(0);
        weightUserEntity.setNxWuStatus(1); // 默认启用
        weightUserEntity.setNxWuUrlChange(1);
        
        // 上传头像
        String newUploadName = "uploadImage";
        String realPath = UploadFile.upload(session, newUploadName, file);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;
        weightUserEntity.setNxWuWxAvartraUrl(filePath);
        
        // 根据用户类型设置关联ID
        if (userType == 1) {
            // 配送商员工
            weightUserEntity.setNxWuNxDistributerId(disId);
            weightUserEntity.setNxWuUserId(null);
        } else {
            // 供货商员工
            weightUserEntity.setNxWuUserId(userId);
            weightUserEntity.setNxWuNxDistributerId(null);
        }
        
        // 保存用户
        nxWeightUserService.save(weightUserEntity);

        return R.ok().put("data", weightUserEntity);
    }


//    @RequestMapping(value = "/disKfUserSaveWithFile",produces = "text/html;charset=UTF-8")
//    @ResponseBody
//    public R disKfUserSaveWithFile(@RequestParam("file") MultipartFile file,
//                                   @RequestParam("userName") String userName,
//                                   @RequestParam("code") String code,
//                                   @RequestParam("disId") Integer disId,
//                                   HttpSession session) {
//
//        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
//        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getShanghuoAppID() + "&secret=" +
//                myAPPIDConfig.getShanghuoScreat() + "&js_code=" + code +  "&grant_type=authorization_code";
//        // 发送请求，返回Json字符串
//        String str = WeChatUtil.httpRequest(url, "GET", null);
//        // 转成Json对象 获取openid
//        JSONObject jsonObject = JSONObject.parseObject(str);
//
//        // 我们需要的openid，在一个小程序中，openid是唯一的
//        String openid = jsonObject.get("openid").toString();
//        NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openid);
//        if (distributerUser != null) {
//            return R.error(-1, "用户已存在，请直接登陆");
//        } else {
//            NxDistributerUserEntity distributerUserEntity = new NxDistributerUserEntity();
//            distributerUserEntity.setNxDiuPrintDeviceId("-1");
//            distributerUserEntity.setNxDiuPrintBillDeviceId("-1");
//            distributerUserEntity.setNxDiuLoginTimes(0);
//            distributerUserEntity.setNxDiuWxOpenId(openid);
//            String newUploadName = "uploadImage";
//            String realPath = UploadFile.upload(session, newUploadName, file);
//
//            String filename = file.getOriginalFilename();
//            String filePath = newUploadName + "/" + filename;
//            distributerUserEntity.setNxDiuWxAvartraUrl(filePath);
//            distributerUserEntity.setNxDiuUrlChange(1);
//            distributerUserEntity.setNxDiuWxOpenId(openid);
//            distributerUserEntity.setNxDiuPrintDeviceId("-1");
//            distributerUserEntity.setNxDiuPrintBillDeviceId("-1");
//            distributerUserEntity.setNxDiuWxNickName(userName);
//            distributerUserEntity.setNxDiuDistributerId(disId);
//            distributerUserEntity.setNxDiuAdmin(getNxDisUserWeighter());
//            nxDistributerUserService.save(distributerUserEntity);
//
//            return R.ok();
//        }
//
//
//
//    }

//    @RequestMapping(value = "/disLoginOld", method = RequestMethod.POST)
//    @ResponseBody
//    public R disLoginOld(@RequestBody NxDistributerUserEntity distributerUserEntity) {
//
//        System.out.println("osososoosodddod");
//        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
//        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiansuocaigouguanliduanAppId() + "&secret=" +
//                myAPPIDConfig.getLiansuocaigouguanliduanScreat() + "&js_code=" + distributerUserEntity.getNxDiuCode() +
//                "&grant_type=authorization_code";
//        // 发送请求，返回Json字符串
//        String str = WeChatUtil.httpRequest(url, "GET", null);
//        // 转成Json对象 获取openid
//        JSONObject jsonObject = JSONObject.parseObject(str);
//
//        // 我们需要的openid，在一个小程序中，openid是唯一的
//        String openid = jsonObject.get("openid").toString();
//        NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openid);
//        if (distributerUser != null) {
//            Integer nxDistributerUserId = distributerUser.getNxDistributerUserId();
//            Map<String, Object> stringObjectMap = nxDistributerUserService.queryNxDisAndUserInfo(nxDistributerUserId);
//            distributerUser.setNxDiuLoginTimes( distributerUser.getNxDiuLoginTimes() + 1);
//            nxDistributerUserService.update(distributerUser);
//            return R.ok().put("data", stringObjectMap);
//        } else {
//            return R.error(-1, "用户不存在");
//        }
//    }

    @RequestMapping(value = "/updateDisUserDeviceId", method = RequestMethod.POST)
    @ResponseBody
    public R updateDisUserDeviceId(@RequestBody NxDistributerUserEntity userEntity) {

        nxDistributerUserService.update(userEntity);

        return R.ok();
    }


    @RequestMapping(value = "/updateDisUser", method = RequestMethod.POST)
    @ResponseBody
    public R updateDisUser(String userName, Integer userId, String phone, String deviceId) {
        NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryObject(userId);
        nxDistributerUserEntity.setNxDiuWxNickName(userName);
        nxDistributerUserEntity.setNxDiuWxPhone(phone);
        nxDistributerUserEntity.setNxDiuPrintDeviceId(deviceId);
        System.out.println("apddd" + phone);
        nxDistributerUserService.update(nxDistributerUserEntity);

        NxDistributerUserEntity nxDistributerUserEntity1 = nxDistributerUserService.queryUserInfo(userId);
        return R.ok().put("data", nxDistributerUserEntity1);
    }

    @RequestMapping(value = "/updateWeightUser", method = RequestMethod.POST)
    @ResponseBody
    public R updateWeightUser(String userName, Integer userId, String phone, String deviceId) {
        NxWeightUserEntity nxDistributerUserEntity = nxWeightUserService.queryObject(userId);
        nxDistributerUserEntity.setNxWuWxNickName(userName);
        nxDistributerUserEntity.setNxWuWxPhone(phone);
        System.out.println("apddd" + phone);
        nxWeightUserService.update(nxDistributerUserEntity);

        NxDistributerUserEntity nxDistributerUserEntity1 = nxDistributerUserService.queryUserInfo(userId);
        return R.ok().put("data", nxDistributerUserEntity1);
    }


    @RequestMapping(value = "/updateDisUserWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R updateDisUserWithFile(@RequestParam("file") MultipartFile file,
                                   @RequestParam("userName") String userName,
                                   @RequestParam("userId") Integer userId,
                                   HttpSession session) {
        //1,上传图片
        String newUploadName = "uploadImage";
        String realPath = UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;
        NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryObject(userId);
        nxDistributerUserEntity.setNxDiuWxNickName(userName);
        nxDistributerUserEntity.setNxDiuWxAvartraUrl(filePath);
        nxDistributerUserEntity.setNxDiuUrlChange(1);
        nxDistributerUserService.update(nxDistributerUserEntity);

        return R.ok();

    }


    @RequestMapping(value = "/updateWeighterWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R updateWeighterWithFile(@RequestParam("file") MultipartFile file,
                                   @RequestParam("userName") String userName,
                                   @RequestParam("userId") Integer userId,
                                   HttpSession session) {
        //1,上传图片
        String newUploadName = "uploadImage";
        String realPath = UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;
        NxWeightUserEntity nxDistributerUserEntity = nxWeightUserService.queryObject(userId);
        nxDistributerUserEntity.setNxWuWxNickName(userName);
        nxDistributerUserEntity.setNxWuWxAvartraUrl(filePath);
        nxDistributerUserEntity.setNxWuUrlChange(1);
        nxWeightUserService.update(nxDistributerUserEntity);

        return R.ok();

    }


}
