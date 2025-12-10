package com.nongxinle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.QyGbDisCorpMsgAuditEntity;
import com.nongxinle.service.QyGbDisCorpMsgAuditService;
import com.nongxinle.utils.QyWxTokenUtil;
import com.nongxinle.utils.WeChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 智能客服服务 - 基于会话存档的自动回复
 * 
 * @author lpy
 * @date 2024-10-19
 */
@Service
public class SmartCustomerService {

    @Autowired
    private QyGbDisCorpMsgAuditService qyGbDisCorpMsgAuditService;
    
    @Autowired
    private QyWxTokenUtil qyWxTokenUtil;

    /**
     * 处理接收到的消息，判断是否需要自动回复
     * @param corpId 企业ID
     * @param roomid 群聊ID
     * @param from 发送者
     * @param content 消息内容
     * @param msgType 消息类型
     */
    public void handleMessage(String corpId, String roomid, String from, String content, String msgType) {
        try {
            System.out.println("=== 智能客服处理消息 ===");
            System.out.println("群聊ID: " + roomid);
            System.out.println("发送者: " + from);
            System.out.println("消息类型: " + msgType);
            System.out.println("消息内容: " + content);
            
            // 1. 检查是否需要回复
            if (!shouldReply(content, msgType)) {
                System.out.println("消息不需要回复");
                return;
            }
            
            // 2. 生成回复内容
            String replyContent = generateReply(content);
            if (replyContent == null || replyContent.isEmpty()) {
                System.out.println("无法生成合适的回复");
                return;
            }
            
            // 3. 发送回复消息
            sendReply(corpId, roomid, replyContent);
            
        } catch (Exception e) {
            System.err.println("处理消息异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 判断是否需要回复
     */
    private boolean shouldReply(String content, String msgType) {
        // 只处理文本消息
        if (!"text".equals(msgType)) {
            return false;
        }
        
        // 检查是否包含需要回复的关键词
        String[] keywords = {
            "价格", "多少钱", "费用", "收费",
            "怎么联系", "联系方式", "电话",
            "怎么购买", "如何购买", "下单",
            "客服", "人工", "帮助"
        };
        
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 生成回复内容
     */
    private String generateReply(String content) {
        if (content.contains("价格") || content.contains("多少钱") || content.contains("费用")) {
            return "您好！我们的产品价格非常优惠，具体价格请私聊我们的客服人员。";
        }
        
        if (content.contains("联系方式") || content.contains("怎么联系") || content.contains("电话")) {
            return "您好！我们的客服电话是：400-123-4567，工作时间：9:00-18:00。";
        }
        
        if (content.contains("怎么购买") || content.contains("如何购买") || content.contains("下单")) {
            return "您好！您可以通过我们的官方网站或小程序进行购买，也可以联系客服协助下单。";
        }
        
        if (content.contains("客服") || content.contains("人工") || content.contains("帮助")) {
            return "您好！我是智能客服，有什么问题可以随时咨询。如需人工客服，请回复人工客服。";
        }
        
        // 默认回复
        return "您好！感谢您的咨询，我们的客服人员会尽快为您解答。";
    }
    
    /**
     * 发送回复消息
     */
    private void sendReply(String corpId, String roomid, String content) {
        try {
            // 获取access_token
            String accessToken = qyWxTokenUtil.getAccessToken(corpId);
            if (accessToken == null) {
                System.err.println("获取access_token失败");
                return;
            }
            
            // 构建发送消息的请求
            String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken;
            
            Map<String, Object> params = new HashMap<>();
            params.put("touser", "@all"); // 发送给群内所有人
            params.put("msgtype", "text");
            params.put("agentid", "1000002"); // 需要替换为实际的应用ID
            
            Map<String, Object> text = new HashMap<>();
            text.put("content", content);
            params.put("text", text);
            
            String result = WeChatUtil.httpRequest(url, "POST", JSON.toJSONString(params));
            System.out.println("发送回复消息响应: " + result);
            
            JSONObject jsonObject = JSON.parseObject(result);
            Integer errcode = jsonObject.getInteger("errcode");
            
            if (errcode == null || errcode != 0) {
                System.err.println("发送回复消息失败 - errcode: " + errcode + ", errmsg: " + jsonObject.getString("errmsg"));
            } else {
                System.out.println("回复消息发送成功");
            }
            
        } catch (Exception e) {
            System.err.println("发送回复消息异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
