package com.nongxinle.community.customer.service.impl;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.NxCustomerEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.entity.NxECommerceEntity;
import com.nongxinle.community.core.service.NxCommunityService;
import com.nongxinle.community.promotion.service.NxCustomerReferralService;
import com.nongxinle.community.customer.service.NxCustomerRegistrationService;
import com.nongxinle.community.customer.service.NxCustomerService;
import com.nongxinle.community.customer.service.NxCustomerUserService;
import com.nongxinle.utils.CustomerReferralConstants;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.WeChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.formatWhatDay;

@Service("nxCustomerRegistrationService")
public class NxCustomerRegistrationServiceImpl implements NxCustomerRegistrationService {

    @Autowired
    private NxCustomerService nxCustomerService;
    @Autowired
    private NxCommunityService nxCommunityService;
    @Autowired
    private NxCustomerUserService customerUserService;
    @Autowired
    private NxCustomerReferralService nxCustomerReferralService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> registerNewCustomerMix(String phoneCode, String openId, Integer commerceId,
                                                       Integer commId, String promotionCode) {
        Map<String, Object> mapU = new HashMap<>();
        mapU.put("openId", openId);
        mapU.put("commerceId", commerceId);
        NxCustomerUserEntity existing = customerUserService.queryUserByOpenIdAndCommerceId(mapU);
        if (existing != null) {
            throw new IllegalStateException("用户已注册，请直接登录");
        }

        String phone = fetchWxPhoneNumber(phoneCode);

        NxCustomerEntity nxCustomer = new NxCustomerEntity();
        nxCustomer.setNxCustomerCommerceId(commerceId);
        nxCustomerService.save(nxCustomer);

        NxECommerceEntity nxECommerceEntity = nxCommunityService.queryCommunityByECommerceId(commerceId);
        String nxECommerceName = nxECommerceEntity != null ? nxECommerceEntity.getNxECommerceName() : "";

        NxCustomerUserEntity userEntity = new NxCustomerUserEntity();
        userEntity.setNxCuCustomerId(nxCustomer.getNxCustomerId());
        userEntity.setNxCuWxOpenId(openId);
        userEntity.setNxCuWxPhoneNumber(phone);
        userEntity.setNxCuWxNickName(nxECommerceName + phone.substring(7, 11));
        userEntity.setNxCuWxAvatarUrl("userImage/myUrl.png");
        userEntity.setNxCuJoinDate(formatWhatDay(0));
        userEntity.setNxCuWxGender(0);
        userEntity.setNxCuOrderAmount("0");
        userEntity.setNxCuOrderTimes(0);
        userEntity.setNxCuCommerceId(commerceId);
        userEntity.setNxCuCommunityId(commId);
        customerUserService.save(userEntity);

        nxCustomerReferralService.processPromotionAfterRegister(userEntity, promotionCode,
                CustomerReferralConstants.SHARE_ENTRY_MINIPROGRAM);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("userInfo", userEntity);
        mapR.put("customerInfo", nxCustomer);
        return mapR;
    }

    private String fetchWxPhoneNumber(String phoneCode) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String appId = myAPPIDConfig.getQingqingxiangAppId();
        String secret = myAPPIDConfig.getQingqingxiangScreat();

        String urlPhone = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appId, secret);
        String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
        JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
        String accessToken = jsonObjectPhone.getString("access_token");

        String urlP = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token="
                + accessToken + "&code=" + phoneCode;
        Map<String, Object> map = new HashMap<>();
        map.put("code", phoneCode);
        String body = HttpRequest.post(urlP)
                .body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue())
                .execute()
                .body();

        JSONObject jsonObjectP = JSONObject.parseObject(body);
        String phoneI = jsonObjectP.getString("phone_info");
        JSONObject jsonObjectPInfo = JSONObject.parseObject(phoneI);
        return jsonObjectPInfo.getString("phoneNumber");
    }
}
