package com.nongxinle.service;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.SubscribeMessage;
import com.nongxinle.entity.TemplateData;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.WeChatUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WeNoticeService {

//


    public static void jjSupplierBatchReceive(String openId, String page, Map<String, TemplateData> map) {
        System.out.println("sussnssnsnnsnsnsnnnnnnsnnsnnsnns" + openId +"===" + page + "pmapp" + map);
        try {

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getTexiansongAppID();
            String secret  = myAPPIDConfig.getTexiansongScreat();

            String templateId = "n42fBcXAZM1ol0OXB0TbDNDTK1hFISOUFmg0Fj9-4Vc";
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            System.out.println("str=====>>>>" + strPhone);
            // 转成Json对象 获取openid
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            System.out.println("jsonObject" + jsonObjectPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            // 拼接数据
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);//事先定义好的模板id
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue()).execute().body();
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void nxSupplierOrderSave(String openId, String page, Map<String, TemplateData> map) {
        System.out.println("sussnssnsnnsnsnsnnnnnnsnnsnnsnns" + openId +"===" + page + "pmapp" + map);
        try {

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getJinriDinghuoAppId();
            String secret  = myAPPIDConfig.getJinriDinghuoScreat();

            String templateId = "gpfT97ByNYJuMNC0hVZJ7MLwgQ0D6LyzEqn_1NSLJZs";
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            System.out.println("str=====>>>>" + strPhone);
            // 转成Json对象 获取openid
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            System.out.println("jsonObject" + jsonObjectPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            // 拼接数据
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);//事先定义好的模板id
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue()).execute().body();
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void nxCashDepSave(String openId, String page, Map<String, TemplateData> map) {
        System.out.println("sussnssnsnnsnsnsnnnnnnsnnsnnsnns" + openId +"===" + page + "pmapp" + map);
        try {

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getTexiansongAppID();
            String secret  = myAPPIDConfig.getTexiansongScreat();

            String templateId = "4IotlkPhRCenTt-34pE41bJfKoSxQrzzj0cIYhR8YbI";
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            System.out.println("str=====>>>>" + strPhone);
            // 转成Json对象 获取openid
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            System.out.println("jsonObject" + jsonObjectPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            // 拼接数据
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);//事先定义好的模板id
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue()).execute().body();
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void nxDistributerReceiveGbOrders(String openId, String page, Map<String, TemplateData> map) {
        System.out.println("sussnssnsnnsnsnsnnnnnnsnnsnnsnns" + openId +"===" + page + "pmapp" + map);
        try {

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getTexiansongAppID();
            String secret  = myAPPIDConfig.getTexiansongScreat();


            String templateId = "y2MrVCbjYHT83yRA7AY2Wy1G8nCUcQBrIrPg5M2v-VE";
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            System.out.println("str=====>>>>" + strPhone);
            // 转成Json对象 获取openid
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            System.out.println("jsonObject" + jsonObjectPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            // 拼接数据
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);//事先定义好的模板id
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue()).execute().body();
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tuihuoGbSuppliertixingMessageJj(String openId, String page, Map<String, TemplateData> map) {
        System.out.println("sussnssnsnnsnsnsnnnnnnsnnsnnsnns" + openId +"===" + page + "pmapp" + map);
        try {

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getTexiansongCaigouAppId();
            String secret  = myAPPIDConfig.getTexiansongCaigouScreat();

            String templateId = "TE6HIkd7LRQ08zdnQXowRjZu8OBK0eGEd368p2NtTeA";
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            System.out.println("str=====>>>>" + strPhone);
            // 转成Json对象 获取openid
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            System.out.println("jsonObject" + jsonObjectPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            // 拼接数据
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);//事先定义好的模板id
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue()).execute().body();
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void autoGbSuppliertixingMessageJj(String openId, String page, Map<String, TemplateData> map) {
        System.out.println("sussnssnsnnsnsnsnnnnnnsnnsnnsnns" + openId +"===" + page + "pmapp" + map);
        try {

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getTexiansongCaigouAppId();
            String secret  = myAPPIDConfig.getTexiansongCaigouScreat();

            String templateId = "CgludlqVZc_vmFaZUgVFC-iprkydrtOfF_GcODltpTc";
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            System.out.println("str=====>>>>" + strPhone);
            // 转成Json对象 获取openid
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            System.out.println("jsonObject" + jsonObjectPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            // 拼接数据
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);//事先定义好的模板id
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue()).execute().body();
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void autoGbSuppliertixingMessageMix(String openId, String page, Map<String, TemplateData> map) {
        System.out.println("sussnssnsnnsnsnsnnnnnnsnnsnnsnns" + openId +"===" + page + "pmapp" + map);
        try {

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getJinriDinghuoAppId();
            String secret  = myAPPIDConfig.getJinriDinghuoScreat();


            String templateId = "WEc47GOidrOTr5NB_mbGaMMj3BUHRpbAYtvbk4bqkCc";
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            System.out.println("str=====>>>>" + strPhone);
            // 转成Json对象 获取openid
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            System.out.println("jsonObject" + jsonObjectPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            // 拼接数据
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);//事先定义好的模板id
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue()).execute().body();
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gbStockGoodsChangeQuantityGb(String openId, String page, Map<String, TemplateData> map) {
        System.out.println("gbStockGoodsChangeQuantity-------" + openId +"===" + page + "pmapp" + map);
        try {

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getTexiansongCaigouAppId();
            String secret  = myAPPIDConfig.getTexiansongCaigouScreat();

            String templateId = "_KhWtCVg3fIBH-tHqSV0hUk5m_vuKmxw1CGn0PEv6D0";
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            System.out.println("str=====>>>>" + strPhone);
            // 转成Json对象 获取openid
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            System.out.println("jsonObject-gbStockGoodsChangeQuantityGb" + jsonObjectPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            System.out.println("acckcckkkd--gbStockGoodsChangeQuantityGb" + accessToken);
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            // 拼接数据
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);//事先定义好的模板id
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue()).execute().body();
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gbStockGoodsChangeQuantityNx(String openId, String page, Map<String, TemplateData> map) {
        System.out.println("sussnssnsnnsnsnsnnnnnnsnnsnnsnns" + openId +"===" + page + "pmapp" + map);
        try {

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getTexiansongAppID();
            String secret  = myAPPIDConfig.getTexiansongScreat();

            String templateId = "-1rsXSIB6mfB81RjNaxPTvt5j3KmvrYDI6K5ER3SqnQ";
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            System.out.println("str=====>>>>" + strPhone);
            // 转成Json对象 获取openid
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            System.out.println("jsonObject" + jsonObjectPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            System.out.println("acckcckkkd" + accessToken);
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            // 拼接数据
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);//事先定义好的模板id
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue()).execute().body();
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gbStockGoodsChangeQuantity(String openId, String page, Map<String, TemplateData> map) {
        System.out.println("sussnssnsnnsnsnsnnnnnnsnnsnnsnns" + openId +"===" + page + "pmapp" + map);
        try {

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getJinriDinghuoAppId();
            String secret  = myAPPIDConfig.getJinriDinghuoScreat();


            String templateId = "aqfUSJ_sKT_OEvwQucKspHevRCOqRpBrriMcLqAcXXM";
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            System.out.println("str=====>>>>" + strPhone);
            // 转成Json对象 获取openid
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            System.out.println("jsonObject" + jsonObjectPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            System.out.println("acckcckkkd" + accessToken);
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            // 拼接数据
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);//事先定义好的模板id
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue()).execute().body();
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void qucantixingMessageMix(String openId, String page, Map<String, TemplateData> map) {
        System.out.println("sussnssnsnnsnsnsnnnnnnsnnsnnsnns" + openId +"===" + page + "pmapp" + map);
        try {

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getQingqingxiangAppId();
            String secret  = myAPPIDConfig.getQingqingxiangScreat();


            String templateId = "45ck7c-dSjSRZczWw8shW-bjW9WKnXMG68gmvtPeBxo";
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            System.out.println("str=====>>>>" + strPhone);
            // 转成Json对象 获取openid
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            System.out.println("jsonObject" + jsonObjectPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            // 拼接数据
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);//事先定义好的模板id
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue()).execute().body();
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void qucantixingMessage(String openId, String page, Map<String, TemplateData> map) {
        System.out.println("sussnssnsnnsnsnsnnnnnnsnnsnnsnns" + openId +"===" + page + "pmapp" + map);
        try {

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getShixianLiliAppId();
            String secret  = myAPPIDConfig.getShixianLiliScreat();


            String templateId = "oLOnX55S-_7aWCCXgkLGfwt02vhZDiyTWlH8n3GwYEY";
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            System.out.println("str=====>>>>" + strPhone);
            // 转成Json对象 获取openid
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            System.out.println("jsonObject" + jsonObjectPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            // 拼接数据
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);//事先定义好的模板id
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue()).execute().body();
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void subscribeOrderFinishMessage(String openId, String page, Map<String, TemplateData> map) {
        System.out.println("sussnssnsnnsnsnsnnnnnnsnnsnnsnns" + openId +"===" + page + "pmapp" + map);
        try {

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getShixianLiliAppId();
            String secret  = myAPPIDConfig.getShixianLiliScreat();


            String templateId = "7x-z_S7NQdD3wAn8qVmQAbAaCbK_j2aA7CRbZWDPGuw";
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            System.out.println("str=====>>>>" + strPhone);
            // 转成Json对象 获取openid
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            System.out.println("jsonObject" + jsonObjectPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            // 拼接数据
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);//事先定义好的模板id
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue()).execute().body();
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Map<String, TemplateData> map = new HashMap<>();
        map.put("character_string1", new TemplateData(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date())));
        map.put("amount2", new TemplateData("12"));
        map.put("phrase3", new TemplateData("已到货"));
        map.put("thing6", new TemplateData("BJYSL-SN-500"));
        map.put("number7", new TemplateData("100"));
       String openId =  "orWDh5HwYg0CefPuW9r5wnVnuZiw";
        subscribeOrderFinishMessage(openId, "/pages/index/index", map);
    }
}
