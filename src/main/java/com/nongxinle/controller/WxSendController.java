/**
 * com.nongxinle.controller class
 *
 * @Author: peiyi li
 * @Date: 2020-03-11 16:30
 */

package com.nongxinle.controller;

import com.alibaba.fastjson.JSON;
import com.nongxinle.entity.NxCommunityOrdersEntity;
import com.nongxinle.entity.Value;
import com.nongxinle.service.NxCustomerUserService;
import com.nongxinle.service.NxCommunityOrdersService;
import com.nongxinle.utils.HttpUtils;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.formatWhatDayTime;

/**
 *@author lpy
 *@date 2020-03-11 16:30
 */

@RestController
@RequestMapping("/api/test")
public class WxSendController {


    @Autowired
    private NxCustomerUserService nxCustomerUserService;

    @Autowired
    private NxCommunityOrdersService nxCommunityOrdersService;


    @RequestMapping(value = "/send/{nxOrdersId}")
    @ResponseBody
    public R sendPaymentWxNotice(@PathVariable Integer nxOrdersId){

        NxCommunityOrdersEntity ordersEntity = nxCommunityOrdersService.queryObject(nxOrdersId);

        Integer nxOrdersUserId = ordersEntity.getNxCoUserId();
        System.out.println( "enennenetyytyty" +ordersEntity);
        String token = getToken();
        String cuWxOpenId= nxCustomerUserService.queryOpenId(nxOrdersUserId);

        Map<String,Object> param = new HashMap<>();
        param.put("touser",cuWxOpenId);
        param.put("template_id","n42fBcXAZM1ol0OXB0TbDNDTK1hFISOUFmg0Fj9");
        param.put("page","/pages/index/index");
        param.put("miniprogram_state","developer");

        String nxOrdersAmount = ordersEntity.getNxCoAmount().toString();
        String nxOrdersDate = ordersEntity.getNxCoDate();
        String  nxOrdersId1 = ordersEntity.getNxCommunityOrdersId().toString();


        Map<String,Object> data = new HashMap<>();
        data.put("character_string1", new Value(nxOrdersId1));
        data.put("amount2", new Value(nxOrdersAmount));
        data.put("date3",new Value(nxOrdersDate));
        data.put("phrase5",new Value("待支付"));
        data.put("thing4",new Value("您的订单将于4小时失效"));
        param.put("data",data);
        // 注意检查参数的格式，很容易出现问题
        System.out.println("param:" + JSON.toJSONString(param));

        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
        Map result = HttpUtils.post(url, param);
        System.out.println("result=" + result);

        if(result.get("errcode").equals(0)){
            Map<String, Object> map = new HashMap<>();
            map.put("nxOrdersId", nxOrdersId);
            map.put("nxOrdersStatus",3);
            map.put("nxOrdersPaymentStatus", 1);
            map.put("nxOrdersPaymentSendTime", formatWhatDayTime(0));
            nxCommunityOrdersService.updatePaymentStatus(map);
        }
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
}

