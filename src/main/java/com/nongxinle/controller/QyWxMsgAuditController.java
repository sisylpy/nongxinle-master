package com.nongxinle.controller;

import com.nongxinle.entity.QyGbDisCorpMsgAuditEntity;
import com.nongxinle.entity.QyWxMessage;
import com.nongxinle.service.MessageHandlerService;
import com.nongxinle.service.QyGbDisCorpMsgAuditService;
import com.nongxinle.utils.MessageDecryptUtil;
import com.nongxinle.utils.MessageUtil;
import com.nongxinle.utils.QywechatInfo;
import com.nongxinle.utils.QywechatEnum;
import com.nongxinle.utils.aes.WXBizMsgCrypt;
import com.nongxinle.utils.R;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 企业微信会话存档回调接口
 * 
 * @author lpy
 * @date 2024-01-01
 */
@RestController
@RequestMapping("api/qywx/msgaudit")
public class QyWxMsgAuditController {

    @Autowired
    private QyGbDisCorpMsgAuditService qyGbDisCorpMsgAuditService;

    @Autowired
    private MessageHandlerService messageHandlerService;

    @Autowired
    private MessageDecryptUtil messageDecryptUtil;

    /**
     * 回调验证接口（GET）
     * 企业微信首次配置回调URL时会调用此接口进行验证
     */
    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    @ResponseBody
    public void verifyCallback(@RequestParam(name = "msg_signature") final String msgSignature,
                              @RequestParam(name = "timestamp") final String timestamp,
                              @RequestParam(name = "nonce") final String nonce,
                              @RequestParam(name = "echostr") final String echostr,
                              final HttpServletResponse response) throws Exception {
        System.out.println("=== 企业微信会话存档回调验证开始 ===");
        System.out.println("msg_signature: " + msgSignature);
        System.out.println("timestamp: " + timestamp);
        System.out.println("nonce: " + nonce);
        System.out.println("echostr: " + echostr);
        
        // 使用企业微信会话存档枚举
        QywechatEnum qywechatEnum = QywechatEnum.MSGAUDIT;
        qywechatEnum.setCorpid("ww9778dea409045fe6"); // 设置实际的企业ID
        
        // 使用WXBizMsgCrypt进行验证和解密
        WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(qywechatEnum);
        String sEchoStr = wxBizMsgCrypt.verifyURL(msgSignature, timestamp, nonce, echostr);
        
        PrintWriter out = response.getWriter();
        try {
            // 必须要返回解密之后的明文
            if (sEchoStr == null || sEchoStr.isEmpty()) {
                System.err.println("get验签URL验证失败");
            } else {
                System.out.println("get验签验证成功!");
            }
        } catch (Exception e) {
            System.err.println("get验签报错！");
            e.printStackTrace();
        }
        System.out.println("get验签的echo是: " + sEchoStr);
        System.out.println("=== 回调验证完成 ===");
        out.write(sEchoStr);
        out.flush();
    }

    /**
     * 接收消息推送接口（POST）
     * 企业微信会实时推送消息到此处
     */
    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    @ResponseBody
    public void receiveMessage(@RequestParam(name = "msg_signature") final String msgSignature,
                              @RequestParam(name = "timestamp") final String timestamp,
                              @RequestParam(name = "nonce") final String nonce,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        System.out.println("========== 企业微信会话存档消息推送开始 ==========");
        System.out.println("URL参数：");
        System.out.println("  msg_signature=[\"" + msgSignature + "\"]");
        System.out.println("  timestamp=[\"" + timestamp + "\"]");
        System.out.println("  nonce=[\"" + nonce + "\"]");
        System.out.println("===========================================");
        
        try {
            // 读取请求体（保持原样，不做任何转换）
            String postData = IOUtils.toString(request.getInputStream(), "UTF-8");
            System.out.println("========== 收到消息推送（原始POST数据） ==========");
            System.out.println(postData);
            System.out.println("POST数据长度=" + postData.length());
            System.out.println("POST数据是否包含+号=" + postData.contains("+"));
            System.out.println("POST数据是否包含空格=" + postData.contains(" "));
            System.out.println("===========================================");

            // 解析XML获取ToUserName（企业ID）
            Map<String, String> xmlMap = MessageUtil.parseXml(postData);
            String toUserName = xmlMap.get("ToUserName");
            System.out.println("ToUserName（企业ID）: " + toUserName);
            
            // 使用企业微信会话存档枚举
            QywechatEnum qywechatEnum = QywechatEnum.MSGAUDIT;
            qywechatEnum.setCorpid(toUserName != null ? toUserName : "ww9778dea409045fe6");
            
            // 设置解密参数
            QywechatInfo qywechatInfo = new QywechatInfo();
            qywechatInfo.setMsgSignature(msgSignature);
            qywechatInfo.setNonce(nonce);
            qywechatInfo.setQywechatEnum(qywechatEnum);
            qywechatInfo.setTimestamp(timestamp);
            qywechatInfo.setSPostData(postData);
            
            // 使用WXBizMsgCrypt解密消息
            WXBizMsgCrypt msgCrypt = new WXBizMsgCrypt(qywechatEnum);
            String decryptedMsg = msgCrypt.decryptMsg(qywechatInfo);
            System.out.println("解密后的消息: " + decryptedMsg);
            
            // 解析解密后的XML
            Map<String, String> dataMap = MessageUtil.parseXml(decryptedMsg);
            System.out.println("解析后的消息数据: " + dataMap);

            // 处理不同类型的消息
            String infoType = dataMap.get("InfoType");
            if (infoType != null) {
                System.out.println("InfoType: " + infoType);
            }

            // 处理消息推送事件
            String event = dataMap.get("Event");
            if (event != null) {
                System.out.println("Event: " + event);
            }

            // 返回success确认接收
            PrintWriter writer = response.getWriter();
            writer.write("success");
            writer.flush();

            // 异步处理消息
            String corpId = qywechatEnum.getCorpid();
            processMessageAsync(corpId, dataMap);
            
            System.out.println("=== 消息推送处理完成 ===");

        } catch (Exception e) {
            System.err.println("处理消息推送失败: " + e.getMessage());
            e.printStackTrace();
            try {
                PrintWriter writer = response.getWriter();
                writer.write("error");
                writer.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * 异步处理消息
     * @param corpId 企业ID
     * @param dataMap 消息数据
     */
    private void processMessageAsync(String corpId, Map<String, String> dataMap) {
        try {
            // 这里应该调用会话存档API获取具体的消息内容
            // 然后进行解密和业务处理
            
            // 示例：假设从回调中获取到了msgid
            String msgId = dataMap.get("MsgId");
            if (msgId != null) {
                System.out.println("开始处理消息: " + msgId);
                
                // 获取会话内容
                QyWxMessage message = qyGbDisCorpMsgAuditService.getChatData(corpId, msgId);
                if (message != null) {
                    // 解密消息内容
                    if (message.getContent() != null) {
                        String decryptedContent = qyGbDisCorpMsgAuditService.decryptMessage(message.getContent(), corpId);
                        if (decryptedContent != null) {
                            message.setContent(decryptedContent);
                        }
                    }
                    
                    // 处理消息
                    messageHandlerService.handleMessage(corpId, message);
                }
            }
        } catch (Exception e) {
            System.err.println("异步处理消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 手动触发消息处理（用于测试）
     */
    @RequestMapping(value = "/process", method = RequestMethod.POST)
    @ResponseBody
    public R processMessage(@RequestParam String corpId, @RequestParam String msgId) {
        try {
            System.out.println("手动处理消息 - corpId: " + corpId + ", msgId: " + msgId);
            
            // 获取会话内容
            QyWxMessage message = qyGbDisCorpMsgAuditService.getChatData(corpId, msgId);
            if (message == null) {
                return R.error("获取消息失败");
            }
            
            // 解密消息内容
            if (message.getContent() != null) {
                String decryptedContent = qyGbDisCorpMsgAuditService.decryptMessage(message.getContent(), corpId);
                if (decryptedContent != null) {
                    message.setContent(decryptedContent);
                }
            }
            
            // 处理消息
            messageHandlerService.handleMessage(corpId, message);
            
            return R.ok().put("message", "处理成功");
        } catch (Exception e) {
            System.err.println("手动处理消息失败: " + e.getMessage());
            e.printStackTrace();
            return R.error("处理失败: " + e.getMessage());
        }
    }
}
