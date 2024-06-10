/**
 * com.nongxinle.service class
 *
 * @Author: peiyi li
 * @Date: 2020-03-11 12:09
 */


package com.nongxinle.service;


import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.entity.TemplateDataVo;
import com.nongxinle.entity.WxMssVo;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//7x-z_S7NQdD3wAn8qVmQAbAaCbK_j2aA7CRbZWDPGuw
@Slf4j
@Service
public class WeChatService {

   @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Autowired
    private RestTemplate restTemplate;





    public String pushOneUser(String openid, String templateId) {

        //如果access_token为空则从新获取
//        if(StringUtils.isEmpty(access_token)){
//            access_token = getAccess_token();
//        }
        String access_token = getAccess_token();

        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + access_token;

//        HttpHeaders headers = new HttpHeaders();
//        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
//        headers.setContentType(type);
//        headers.add("Accept", MediaType.APPLICATION_JSON.toString());



        //拼接推送的模版
        WxMssVo wxMssVo = new WxMssVo();
        wxMssVo.setTouser(openid);
        wxMssVo.setTemplate_id(templateId);
        wxMssVo.setPage("/pages/index/index");
        wxMssVo.setLang("zh_CN");
        wxMssVo.setMiniprogram_state("trial");
        Map<String, String> mapRR = new HashMap<>();
        mapRR.put("msg", "发消息了");
        wxMssVo.setData(mapRR);

        MultiValueMap<String, Object> m = new LinkedMultiValueMap<String, Object>();
        MultiValueMap<String, Object> km1 = new LinkedMultiValueMap<String, Object>();
        MultiValueMap<String, Object> km2 = new LinkedMultiValueMap<String, Object>();
        MultiValueMap<String, Object> km3 = new LinkedMultiValueMap<String, Object>();
        MultiValueMap<String, Object> km4 = new LinkedMultiValueMap<String, Object>();
        MultiValueMap<String, Object> km5 = new LinkedMultiValueMap<String, Object>();


        km1.add("value", "PL000001");
        km2.add("value", "999");
        km3.add("value", "已到货");
        km4.add("value", "caidi");
        km5.add("value", "13910825707");


//        m.add("character_string1", km1);
//        m.add("amount2", km2);
//        m.add("phrase3", km3);
//        m.add("thing6", km4);
//        m.add("number7", km5);
        m.add("thing3", km4);
        m.add("phone_number2", km5);

        if(restTemplate==null){
            restTemplate = new RestTemplate();
        }

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("touser", openid);
        map.add("template_id", templateId);
        map.add("page", "pages/index/index");
        map.add("miniprogram_state", "trial");
        map.add("lang", "zh_CN");
        map.add("data",m);


        System.out.println("00000000");
        System.out.println(url);
//        System.out.println(map);

        System.out.println("111111");



        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(url, wxMssVo, String.class);
        log.error("小程序推送结果={}", responseEntity.getBody());
        return responseEntity.getBody();
//        return "11";
    }


    public String getAccess_token() {
        //获取access_token
        String appid = "wx159c5a46d80e4500";
        String appsecret = "319555884ced0820af582ae2e622ee6b";
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential" +
                "&appid=" + appid + "&secret=" + appsecret;
        if(restTemplate==null){
            restTemplate = new RestTemplate();
        }
        String json = restTemplate.getForObject(url, String.class);
        JSONObject myJson = JSONObject.parseObject(json);
        return myJson.get("access_token").toString();
    }

    public static void main(String[] args) {

        WeChatService weChatUtil = new WeChatService();
//        String values[] ={"111","100","03-21","菜地","1"};
        weChatUtil.pushOneUser("orWDh5HwYg0CefPuW9r5wnVnuZsiw","Mq8GjNw0x3DpB08nySeGuMiglKOiR5yRSYHkLTgb0_A"
                );
    }


//    public String pushMessage() {
//        RestTemplate restTemplate = new RestTemplate();
//        String openid = "orWDh5HwYg0CefPuW9r5wnVnuZiw";
//
//        //  拼接推送的模板
//        WxMssVo wxMssVO = new WxMssVo();
//        wxMssVO.setTouser(openid);    // 用户的openId
//        wxMssVO.setTemplate_id("7x-z_S7NQdD3wAn8qVmQAbAaCbK_j2aA7CRbZWDPGuw");    //订阅消息模板id
//        wxMssVO.setLang("zh_CN");     //语言类型
//        wxMssVO.setMiniprogram_state("");   //跳转小程序类型
//        //TODO：指定跳转的页面
//        wxMssVO.setPage("pages/index/index");
//
//        // TODO: 推送的内容
//        Map<String, TemplateData> map = new HashMap<>();
//        map.put("msg", new TemplateData("发消息了"));
//        wxMssVO.setData(map);
//        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + access_token;
//
//        // 发送
//        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, wxMssVO, String.class);
//        return responseEntity.getBody();
//    }





}



