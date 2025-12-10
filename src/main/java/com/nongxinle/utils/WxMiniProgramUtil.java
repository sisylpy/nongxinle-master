package com.nongxinle.utils;

import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson.JSONObject;

/**
 * 微信小程序工具类
 * 用于获取 openid
 * 
 * @author lpy
 * @date 2025-10-14
 */
public class WxMiniProgramUtil {

    // TODO: 配置你的小程序 AppID 和 AppSecret
    private static final String APPID = "wxaad78fd4a9d898cf";
    private static final String SECRET = "190652f297cf9826ebb72c29aa1ff0f6";
    
    // 微信接口地址
    private static final String JSCODE2SESSION_URL = 
        "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    /**
     * 通过 code 换取 openid
     * 
     * @param code 前端 wx.login() 获取的 code
     * @return openid（失败返回 null）
     */
    public static String getOpenidByCode(String code) {
        try {
            // 1. 构造请求URL
            String url = String.format(JSCODE2SESSION_URL, APPID, SECRET, code);
            
            // 2. 发送HTTP GET请求
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);
            
            // 3. 解析返回结果
            JSONObject json = JSONObject.parseObject(response);
            
            // 4. 判断是否成功
            if (json.containsKey("openid")) {
                String openid = json.getString("openid");
                String sessionKey = json.getString("session_key");
                
                System.out.println("获取openid成功: " + openid);
                return openid;
            } else {
                // 获取失败，打印错误信息
                Integer errcode = json.getInteger("errcode");
                String errmsg = json.getString("errmsg");
                System.err.println("获取openid失败 [" + errcode + "]: " + errmsg);
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("获取openid异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取完整的用户信息（包含 openid、session_key、unionid）
     * 
     * @param code 前端 wx.login() 获取的 code
     * @return JSON对象，包含 openid、session_key、unionid
     */
    public static JSONObject getSessionInfo(String code) {
        try {
            String url = String.format(JSCODE2SESSION_URL, APPID, SECRET, code);
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);
            return JSONObject.parseObject(response);
        } catch (Exception e) {
            System.err.println("获取session信息异常: " + e.getMessage());
            return null;
        }
    }
}

