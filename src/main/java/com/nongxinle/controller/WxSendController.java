/**
 * com.nongxinle.controller class
 *
 * @Author: peiyi li
 * @Date: 2020-03-11 16:30
 */

package com.nongxinle.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.NxCommunityOrdersEntity;
import com.nongxinle.entity.Value;
import com.nongxinle.community.customer.service.NxCustomerUserService;
import com.nongxinle.community.order.service.NxCommunityOrdersService;
import com.nongxinle.utils.HttpUtils;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.R;
import com.nongxinle.utils.WeChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.formatWhatDayTime;
import lombok.extern.slf4j.Slf4j;

/**
 *@author lpy
 *@date 2020-03-11 16:30
 */

@Slf4j
@RestController
@RequestMapping("/api/wxsend")
public class WxSendController {


    @Autowired
    private NxCustomerUserService nxCustomerUserService;

    @Autowired
    private NxCommunityOrdersService nxCommunityOrdersService;


    @RequestMapping(value = "/i7948FzJJ6.txt")
    @ResponseBody
    public String grainServiceClub( ) {
        return "bb7a0c73e61112c45ebd6ad3743bb05e";
    }

    /** 企业微信可信域名校验（需 Nginx 80/443 根路径可访问） */
    @RequestMapping(value = "/WW_verify_Vha8a9CuTEfZff67.txt")
    @ResponseBody
    public String wwVerifyDomain() {
        return "Vha8a9CuTEfZff67";
    }


    @RequestMapping(value = "/send/{nxOrdersId}")
    @ResponseBody
    public R sendPaymentWxNotice(@PathVariable Integer nxOrdersId){

        NxCommunityOrdersEntity ordersEntity = nxCommunityOrdersService.queryObject(nxOrdersId);

//        Integer nxOrdersUserId = ordersEntity.getNxCoUserId();
//        System.out.println( "enennenetyytyty" +ordersEntity);
//        String token = getToken();
//        String cuWxOpenId= nxCustomerUserService.queryOpenId(nxOrdersUserId);
//
//        Map<String,Object> param = new HashMap<>();
//        param.put("touser",cuWxOpenId);
//        param.put("template_id","n42fBcXAZM1ol0OXB0TbDNDTK1hFISOUFmg0Fj9");
//        param.put("page","/pages/index/index");
//        param.put("miniprogram_state","developer");
//
//        String nxOrdersAmount = ordersEntity.getNxCoAmount().toString();
//        String nxOrdersDate = ordersEntity.getNxCoDate();
//        String  nxOrdersId1 = ordersEntity.getNxCommunityOrdersId().toString();
//
//
//        Map<String,Object> data = new HashMap<>();
//        data.put("character_string1", new Value(nxOrdersId1));
//        data.put("amount2", new Value(nxOrdersAmount));
//        data.put("date3",new Value(nxOrdersDate));
//        data.put("phrase5",new Value("待支付"));
//        data.put("thing4",new Value("您的订单将于4小时失效"));
//        param.put("data",data);
//        // 注意检查参数的格式，很容易出现问题
//        System.out.println("param:" + JSON.toJSONString(param));
//
//        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
//        Map result = HttpUtils.post(url, param);
//        System.out.println("result=" + result);
//
//        if(result.get("errcode").equals(0)){
//            Map<String, Object> map = new HashMap<>();
//            map.put("nxOrdersId", nxOrdersId);
//            map.put("nxOrdersStatus",3);
//            map.put("nxOrdersPaymentStatus", 1);
//            map.put("nxOrdersPaymentSendTime", formatWhatDayTime(0));
//            nxCommunityOrdersService.updatePaymentStatus(map);
//        }
        return R.ok();
    }

    private String getToken(){
        String url = "https://api.weixin.qq.com/cgi-bin/token?appid=wxbc686226ccc443f1&secret=94973a07634b11e98c03ade8aeb4c213&grant_type=client_credential";
        String result = HttpUtils.get(url);
        Map<String,Object> map = JSON.parseObject(result);
        String access_token = map.get("access_token").toString();
        return access_token;
    }

    private String getOpenId(String code){
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=wxbc686226ccc443f1" +
                "&secret=94973a07634b11e98c03ade8aeb4c213" +
                "&js_code="+code+"&grant_type=authorization_code";
        String result = HttpUtils.get(url);
        Map<String,Object> map = JSON.parseObject(result);
        String openid = map.get("openid").toString();
        System.out.println("openid:" + openid);
        return openid;
    }

    /**
     * 检查用户是否关注了粒子订货系统公众号
     * 前端传 code，后端用 code 获取公众号 openId 并返回关注状态
     * @param code 小程序 wx.login() 获得的 code
     * @return R 返回是否关注的结果
     */
    @RequestMapping(value = "/checkIfUserFollow")
    @ResponseBody
    public R checkIfUserFollow(@RequestParam String code) {
        try {
            log.info("========== 开始检查用户关注状态 ==========");
            log.info("用户code: {}", code);

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getLiziDinghuoAppId();
            String secret = myAPPIDConfig.getLiziDinghuoSecret();
            log.info("AppId: {}", appId);

            // 第一步：用 code 换取公众号的 openId（网页授权接口）
            String jscode2sessionUrl = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId +
                    "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
            log.info("jscode2session URL: {}", jscode2sessionUrl);
            String sessionResult = WeChatUtil.httpRequest(jscode2sessionUrl, "GET", null);
            log.info("jscode2session 结果: {}", sessionResult);
            JSONObject sessionJson = JSONObject.parseObject(sessionResult);

            if (sessionJson.containsKey("openid")) {
                String openId = sessionJson.getString("openid");
                log.info("获取到公众号openId: {}", openId);

                // 第二步：获取 access_token
                String tokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential" +
                        "&appid=" + appId + "&secret=" + secret;
                log.info("请求token URL: {}", tokenUrl);
                String tokenResult = WeChatUtil.httpRequest(tokenUrl, "GET", null);
                log.info("token请求结果: {}", tokenResult);
                JSONObject tokenJson = JSONObject.parseObject(tokenResult);

                if (tokenJson.containsKey("access_token")) {
                    String accessToken = tokenJson.getString("access_token");
                    log.info("获取access_token成功: {}", accessToken);

                    // 第三步：调用微信接口获取用户信息，检查关注状态
                    String userInfoUrl = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" +
                            accessToken + "&openid=" + openId + "&lang=zh_CN";
                    log.info("请求用户信息URL: {}", userInfoUrl);
                    String userInfoResult = WeChatUtil.httpRequest(userInfoUrl, "GET", null);
                    log.info("用户信息请求结果: {}", userInfoResult);
                    JSONObject userInfoJson = JSONObject.parseObject(userInfoResult);

                    if (userInfoJson.containsKey("subscribe")) {
                        Integer subscribe = userInfoJson.getInteger("subscribe");
                        // subscribe=1 表示已关注，subscribe=0 表示未关注
                        log.info("subscribe值: {} (1=已关注, 0=未关注)", subscribe);
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("isFollowed", subscribe == 1);
                        resultMap.put("subscribe", subscribe);
                        log.info("========== 检查用户关注状态结束 ==========");
                        return R.ok().put("data", resultMap);
                    } else {
                        log.error("获取用户信息失败: {}", userInfoJson.getString("errmsg"));
                        return R.error("获取用户信息失败: " + userInfoJson.getString("errmsg"));
                    }
                } else {
                    log.error("获取access_token失败: {}", tokenJson.getString("errmsg"));
                    return R.error("获取access_token失败: " + tokenJson.getString("errmsg"));
                }
            } else {
                log.error("获取openId失败: {}", sessionJson.getString("errmsg"));
                return R.error("获取openId失败: " + sessionJson.getString("errmsg"));
            }
        } catch (Exception e) {
            log.error("检查关注状态异常: {}", e.getMessage());
            e.printStackTrace();
            return R.error("检查关注状态异常: " + e.getMessage());
        }
    }
}

