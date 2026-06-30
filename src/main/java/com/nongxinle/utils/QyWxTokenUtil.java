package com.nongxinle.utils;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.QyGbDisCorpMsgAuditEntity;
import com.nongxinle.service.QyGbDisCorpMsgAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信Token管理工具
 * 
 * @author lpy
 * @date 2024-01-01
 */
@Component
public class QyWxTokenUtil {

    @Autowired
    private QyGbDisCorpMsgAuditService qyGbDisCorpMsgAuditService;

    /**
     * 获取access_token（自动刷新）
     * @param corpId 企业ID
     * @return access_token
     */
    public String getAccessToken(String corpId) {
        try {
            // 从数据库获取配置
            QyGbDisCorpMsgAuditEntity config = qyGbDisCorpMsgAuditService.queryByCorpId(corpId);
            if (config == null) {
                throw new RuntimeException("未找到企业微信会话存档配置，corpId: " + corpId);
            }

            // 检查token是否过期
            if (config.getQyGbDisCorpMsgAuditAccessToken() != null && 
                config.getQyGbDisCorpMsgAuditTokenExpireTime() != null &&
                config.getQyGbDisCorpMsgAuditTokenExpireTime().after(new Date())) {
                return config.getQyGbDisCorpMsgAuditAccessToken();
            }

            // Token过期，重新获取
            return refreshAccessToken(config);
        } catch (Exception e) {
            System.err.println("获取access_token失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 刷新access_token
     * @param config 会话存档配置
     * @return 新的access_token
     */
    private String refreshAccessToken(QyGbDisCorpMsgAuditEntity config) {
        try {
            // 调用企业微信API获取token（使用会话存档应用的Secret）
            String corpId = config.getQyGbDisQyCorpId();
            String secret = config.getQyGbDisCorpMsgAuditSecret();
            
            String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + corpId + "&corpsecret=" + secret;
            System.out.println("获取token，corpId=" + corpId);
            
            String response = WeChatUtil.httpRequest(url, "GET", null);
            
            JSONObject jsonObject = JSONObject.parseObject(response);
            System.out.println("获取token响应 errcode=" + jsonObject.getInteger("errcode"));
            if (jsonObject.getInteger("errcode") == 0) {
                String accessToken = jsonObject.getString("access_token");
                int expiresIn = jsonObject.getInteger("expires_in");
                
                // 计算过期时间（提前5分钟过期）
                Date expireTime = new Date(System.currentTimeMillis() + (expiresIn - 300) * 1000L);
                
                // 更新数据库
                qyGbDisCorpMsgAuditService.updateAccessToken(config.getQyGbDisQyCorpId(), accessToken, expireTime);
                
                System.out.println("Token刷新成功，过期时间=" + expireTime);
                return accessToken;
            } else {
                String errorMsg = "获取access_token失败: " + jsonObject.getString("errmsg");
                System.err.println(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        } catch (Exception e) {
            System.err.println("刷新access_token失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("刷新access_token失败", e);
        }
    }

    /**
     * 检查token是否有效
     * @param corpId 企业ID
     * @return true有效，false无效
     */
    public boolean isTokenValid(String corpId) {
        try {
            QyGbDisCorpMsgAuditEntity config = qyGbDisCorpMsgAuditService.queryByCorpId(corpId);
            if (config == null || config.getQyGbDisCorpMsgAuditAccessToken() == null) {
                return false;
            }
            
            if (config.getQyGbDisCorpMsgAuditTokenExpireTime() == null) {
                return false;
            }
            
            return config.getQyGbDisCorpMsgAuditTokenExpireTime().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}


