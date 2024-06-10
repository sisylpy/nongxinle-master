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
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisUserPurchase;


@RestController
@RequestMapping("api/nxdistributeruser")
public class NxDistributerUserController {
    @Autowired
    private NxDistributerUserService nxDistributerUserService;
    @Autowired
    private QyNxDisCropUserService qyNxDisCropUserService;
    @Autowired
    private QyNxDisCorpService qyNxDisCorpService;


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

    
    @RequestMapping(value = "/i7948FzJJ6.txt")
    @ResponseBody
    public String nxStaffRegister() { return "bb7a0c73e61112c45ebd6ad3743bb05e"; }



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

        List<NxDistributerUserEntity> oneUserEntities = nxDistributerUserService.getAllUserByDisId(disId);
        return R.ok().put("data", oneUserEntities);
    }

    @RequestMapping(value = "/deleteDisUser/{userId}")
    @ResponseBody
    public R deleteDisUser(@PathVariable Integer userId) {
        nxDistributerUserService.delete(userId);
        return R.ok();
    }


    @RequestMapping(value = "/disLoginWork", method = RequestMethod.POST)
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


    /**
     * 批发商新管理者注册
     * @param distributerUserEntity 批发商用户
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
                                               @RequestParam("code") String code,
                                               @RequestParam("disId") Integer disId,
                                               HttpSession session) {

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
            nxDistributerUserService.save(distributerUserEntity);

            return R.ok();
        }



    }

    @RequestMapping(value = "/disLoginOld", method = RequestMethod.POST)
    @ResponseBody
    public R disLoginOld(@RequestBody NxDistributerUserEntity distributerUserEntity) {

        System.out.println("osososoosodddod");
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiansuocaigouguanliduanAppId() + "&secret=" +
                myAPPIDConfig.getLiansuocaigouguanliduanScreat() + "&js_code=" + distributerUserEntity.getNxDiuCode() +
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
            nxDistributerUserService.update(distributerUser);
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
        }
    }

    @RequestMapping(value = "/updateDisUserDeviceId", method = RequestMethod.POST)
    @ResponseBody
    public R updateDisUserDeviceId(@RequestBody NxDistributerUserEntity userEntity) {

        nxDistributerUserService.update(userEntity);

        return R.ok();
    }


    @RequestMapping(value = "/updateDisUser", method = RequestMethod.POST)
    @ResponseBody
    public R updateDisUser(String userName, Integer userId) {
        NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryObject(userId);
        nxDistributerUserEntity.setNxDiuWxNickName(userName);
        nxDistributerUserService.update(nxDistributerUserEntity);

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


}
