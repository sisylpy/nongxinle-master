package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-27 09:44
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbDepartmentUserEntity;
import com.nongxinle.entity.SysUserEntity;
import com.nongxinle.service.GbDepartmentService;
import com.nongxinle.service.GbDepartmentUserService;
import com.nongxinle.service.SysUserService;
import com.nongxinle.utils.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDistributerUserEntity;
import com.nongxinle.service.GbDistributerUserService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("api/gbdistributeruser")
public class GbDistributerUserController {
    @Autowired
    private GbDistributerUserService gbDistributerUserService;
    @Autowired
    private GbDepartmentUserService gbDepartmentUserService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    private static Logger logger=Logger.getLogger(GbDistributerUserController.class);   //获取Logger对象

    @RequestMapping(value = "/getGbMendianAndUserInfo", method = RequestMethod.POST)
    @ResponseBody
    public R getGbMendianAndUserInfo (Integer userId, Integer depId) {

//        Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(userId);
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
    @RequestMapping(value = "/disLogin", method = RequestMethod.POST)
    @ResponseBody
    public R disLogin(@RequestBody GbDistributerUserEntity distributerUserEntity) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiansuocaigouguanliduanAppId() + "&secret=" +
                myAPPIDConfig.getLiansuocaigouguanliduanScreat() + "&js_code=" + distributerUserEntity.getGbDiuCode() +
                "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        System.out.println("didididaaa" + openid);
        logger.info(jsonObject);


        List<GbDistributerUserEntity> distributerUserEntities = gbDistributerUserService.queryUserByOpenId(openid);
        if (distributerUserEntities.size() > 0) {
            GbDistributerUserEntity gbDistributerUserEntity = distributerUserEntities.get(0);
            Integer gbDistributerUserId = gbDistributerUserEntity.getGbDistributerUserId();
            Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(gbDistributerUserId);
            System.out.println("dididid" + stringObjectMap);
            logger.info(stringObjectMap);
            gbDistributerUserEntity.setGbDiuLoginTimes(gbDistributerUserEntity.getGbDiuLoginTimes() + 1);
            gbDistributerUserService.update(gbDistributerUserEntity);
            return R.ok().put("data", stringObjectMap);
        } else {
            return R.error(-1, "用户不存在");
        }
    }
    

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
            return R.ok().put("data", stringObjectMap);

        }
    }



    /**
     * 批发商新管理者注册
     * @param distributerUserEntity 批发商用户
     * @return 批发商
     */
    @RequestMapping(value = "/disUserAdminSave", method = RequestMethod.POST)
    @ResponseBody
    public R disUserAdminSave(@RequestBody GbDistributerUserEntity distributerUserEntity) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiansuocaigouguanliduanAppId() + "&secret=" +
                myAPPIDConfig.getLiansuocaigouguanliduanScreat() + "&js_code=" + distributerUserEntity.getGbDiuCode() +
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

            //1 disuser save
            distributerUserEntity.setGbDiuPrintDeviceId("-1");
            distributerUserEntity.setGbDiuPrintBillDeviceId("-1");
            distributerUserEntity.setGbDiuWxOpenId(openid);
            distributerUserEntity.setGbDiuLoginTimes(0);
            Integer disUserId = gbDistributerUserService.save(distributerUserEntity);

            Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(disUserId);
            return R.ok().put("data", stringObjectMap);

        }
    }


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
        String gbDistributerFoodImg = userEntity.getGbDiuWxAvartraUrl();
        ServletContext servletContext = session.getServletContext();
        String realPath1 = servletContext.getRealPath(gbDistributerFoodImg);
        File file1 = new File(realPath1);
        if(file1.exists()) {
            file1.delete();
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
