package com.nongxinle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.QyGbDisCorpMsgAuditEntity;
import com.nongxinle.service.QyGbDisCorpMsgAuditService;
import com.nongxinle.utils.QyWxTokenUtil;
import com.nongxinle.utils.WeChatUtil;
import com.nongxinle.utils.WeworkFinanceSdkUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 客户群发现服务 - 通过会话存档自动发现群聊
 * 
 * @author lpy
 * @date 2024-10-19
 */
@Service
public class CustomerGroupDiscoveryService {

    @Autowired
    private QyGbDisCorpMsgAuditService qyGbDisCorpMsgAuditService;
    
    @Autowired
    private QyWxTokenUtil qyWxTokenUtil;
    
    @Autowired
    private WeworkFinanceSdkUtil weworkFinanceSdkUtil;

    /**
     * 从会话存档拉取消息并发现群聊
     * @param corpId 企业ID
     * @return 发现的群聊数量
     */
    public int discoverGroupsFromMsgAudit(String corpId) {
        try {
            System.out.println("=== 开始从会话存档发现群聊 ===");
            System.out.println("🔍 调试信息 - 方法开始执行，corpId=" + corpId);
            
            // 1. 检查配置
            System.out.println("🔍 调试信息 - 开始检查配置");
            QyGbDisCorpMsgAuditEntity config = qyGbDisCorpMsgAuditService.queryByCorpId(corpId);
            if (config == null) {
                System.err.println("未找到会话存档配置");
                return 0;
            }
            System.out.println("🔍 调试信息 - 配置检查完成");
            
            System.out.println("配置检查完成，Secret已配置: " + (config.getQyGbDisCorpMsgAuditSecret() != null));
            System.out.println("私钥已配置: " + (config.getQyGbDisCorpMsgAuditPrivateKey() != null));
            
            // 2. 获取access_token
            System.out.println("🔍 调试信息 - 开始获取access_token");
            String accessToken = qyWxTokenUtil.getAccessToken(corpId);
            if (accessToken == null) {
                System.err.println("获取access_token失败");
                return 0;
            }
            System.out.println("🔍 调试信息 - access_token获取成功");
            
            System.out.println("access_token获取成功");
            
            // 3. 获取上次拉取的seq
            long seq = getLastSeq(corpId);
            System.out.println("上次拉取的seq: " + seq);
            
            // 4. 使用SDK拉取会话存档
            System.out.println("使用SDK拉取会话存档消息...");
            
            // 初始化SDK
            System.out.println("🔍 调试信息 - 开始初始化SDK");
            if (!weworkFinanceSdkUtil.init(corpId, config.getQyGbDisCorpMsgAuditSecret())) {
                System.err.println("SDK初始化失败");
                return 0;
            }
            System.out.println("🔍 调试信息 - SDK初始化成功");
            
            // 🔑 设置RSA私钥（按版本管理）
            // 💡 企业微信会定期轮换公钥版本，需要同时保留多个版本的私钥
            System.out.println("🔍 调试信息 - 开始设置私钥");
            String privateKeyPem = config.getQyGbDisCorpMsgAuditPrivateKey();
            System.out.println("🔍 调试信息 - 从数据库获取私钥: " + (privateKeyPem != null ? "成功" : "失败"));
            
            if (privateKeyPem != null && !privateKeyPem.trim().isEmpty()) {
                // 添加版本12私钥（当前数据库中存储的是最新版本）
                System.out.println("🔍 调试信息 - 私钥状态:");
                System.out.println("  - 私钥长度: " + privateKeyPem.length() + " 字符");
                System.out.println("  - 私钥开头: " + privateKeyPem.substring(0, Math.min(50, privateKeyPem.length())));
                System.out.println("  - 私钥结尾: " + privateKeyPem.substring(Math.max(0, privateKeyPem.length() - 50)));
                
                // 🔥 临时调试：打印完整私钥内容
                System.out.println("🔥 完整私钥内容:");
                System.out.println("==================== 私钥开始 ====================");
                System.out.println(privateKeyPem);
                System.out.println("==================== 私钥结束 ====================");
                
                System.out.println("🔍 调试信息 - 准备调用 addPrivateKey(12, privateKeyPem)");
                weworkFinanceSdkUtil.addPrivateKey(12, privateKeyPem);
                System.out.println("🔍 调试信息 - addPrivateKey(12) 调用完成");
                System.out.println("✅ 已添加版本12私钥");
                
                // 🔑 只处理v12消息，跳过v1-v11历史消息
                System.out.println("ℹ️ 只配置v12私钥，v1-v11历史消息将被跳过");
                System.out.println("   等待企业微信发送v12版本的新消息");
            } else {
                System.err.println("❌ 未配置私钥");
                return 0;
            }
            
            // 拉取消息 - 增加数量以跳过更多历史v10消息
            String chatData = weworkFinanceSdkUtil.getChatData(seq, 200);
            if (chatData == null || chatData.isEmpty()) {
                System.out.println("暂无新消息");
                return 0;
            }
            
            System.out.println("获取到消息数据: " + chatData);
            
            // 解析消息
            JSONObject chatDataJson = JSON.parseObject(chatData);
            JSONArray chatList = chatDataJson.getJSONArray("chatdata");
            
            if (chatList == null || chatList.isEmpty()) {
                System.out.println("消息列表为空");
                return 0;
            }
            
            System.out.println("获取到 " + chatList.size() + " 条消息");
            
            // 处理每条消息
            Set<String> discoveredGroups = new HashSet<>();
            long maxSeq = seq;
            
            for (int i = 0; i < chatList.size(); i++) {
                try {
                    JSONObject msg = chatList.getJSONObject(i);
                    long currentSeq = msg.getLongValue("seq");
                    int publickeyVer = msg.getIntValue("publickey_ver");
                    String msgid = msg.getString("msgid");
                    maxSeq = Math.max(maxSeq, currentSeq);
                    
                    System.out.println("处理消息 seq=" + currentSeq + ", msgid=" + msgid + ", publickey_ver=" + publickeyVer);
                    // 🔑 只处理v12消息，跳过v1-v11历史消息
                    if (publickeyVer < 12) {
                        System.out.println("  ⏭️ 跳过v" + publickeyVer + "历史消息（seq=" + currentSeq + "）");
                        continue;
                    }
                    
                    // 解密消息
                    String encryptRandomKey = msg.getString("encrypt_random_key");
                    String encryptChatMsg = msg.getString("encrypt_chat_msg");
                    
                    System.out.println("  encrypt_random_key长度: " + encryptRandomKey.length());
                    System.out.println("  encrypt_chat_msg长度: " + encryptChatMsg.length());
                    
                    // 🔑 关键修复：传入 publickey_ver，让工具类按版本选择对应私钥
                    String decrypted = weworkFinanceSdkUtil.decryptData(encryptRandomKey, encryptChatMsg, publickeyVer);
                    if (decrypted != null && !decrypted.isEmpty()) {
                        System.out.println("  解密成功，内容: " + decrypted.substring(0, Math.min(200, decrypted.length())));
                        
                        // 提取群聊信息
                        JSONArray groups = weworkFinanceSdkUtil.extractGroupInfo(decrypted);
                        for (int j = 0; j < groups.size(); j++) {
                            JSONObject group = groups.getJSONObject(j);
                            String roomid = group.getString("roomid");
                            String roomname = group.getString("roomname");
                            
                            discoveredGroups.add(roomid);
                            System.out.println("  ✅ 发现群聊: " + roomid + " - " + roomname);
                            
                            // 保存群聊信息
                            saveGroupInfo(roomid, roomname, group.getString("from"), 
                                        group.getString("msgtype"), "SDK拉取", corpId);
                        }
                    } else {
                        System.out.println("  ❌ 解密失败或消息为空");
                    }
                } catch (Exception e) {
                    System.err.println("处理消息异常，继续处理下一条: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // 保存最新的seq
            saveLastSeq(corpId, maxSeq);
            System.out.println("已保存最新seq: " + maxSeq);
            
            System.out.println("=== SDK群聊发现完成，发现 " + discoveredGroups.size() + " 个群聊 ===");
            return discoveredGroups.size();
            
          
            
        } catch (Exception e) {
            System.err.println("从会话存档发现群聊异常: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 处理消息（简化版本，暂时不解密内容）
     */
    private boolean processMessage(JSONObject item, String corpId) {
        try {
            // 获取基础信息
            long seq = item.getLongValue("seq");
            String encryptRandomKey = item.getString("encrypt_random_key");
            String encryptChatMsg = item.getString("encrypt_chat_msg");
            
            System.out.println("处理消息 seq: " + seq);
            System.out.println("encrypt_random_key: " + (encryptRandomKey != null ? "存在" : "不存在"));
            System.out.println("encrypt_chat_msg: " + (encryptChatMsg != null ? "存在" : "不存在"));
            
            // 暂时不解密，直接模拟群聊发现
            // 在实际应用中，这里需要解密消息内容来获取群聊信息
            
            // 模拟发现群聊（实际应该从解密的消息中提取）
            String roomid = "wrOgQhDgAAMYQiS5ol9G7gK9JVAAAA"; // 模拟群聊ID
            String roomName = "九田田洋城"; // 模拟群聊名称
            
            // 保存群聊信息
            saveGroupInfo(roomid, roomName, "test_user", "text", "测试消息", corpId);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("处理消息异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 解密消息内容
     */
    private String decryptMessage(JSONObject item, String corpId) {
        try {
            String encryptRandomKey = item.getString("encrypt_random_key");
            String encryptChatMsg = item.getString("encrypt_chat_msg");
            
            if (encryptRandomKey == null || encryptChatMsg == null) {
                return null;
            }
            
            // 获取配置
            QyGbDisCorpMsgAuditEntity config = qyGbDisCorpMsgAuditService.queryByCorpId(corpId);
            if (config == null) {
                System.err.println("未找到会话存档配置");
                return null;
            }
            
            // 这里需要实现RSA解密和AES解密逻辑
            // 由于涉及复杂的加密解密，这里先返回占位符
            System.out.println("需要实现消息解密逻辑");
            return "解密后的消息内容";
            
        } catch (Exception e) {
            System.err.println("解密消息异常: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 从消息中提取群聊信息
     */
    private boolean extractGroupInfo(String decryptedMsg, String corpId) {
        try {
            // 解析JSON消息
            JSONObject msgObj = JSON.parseObject(decryptedMsg);
            
            // 检查是否是群消息
            String roomid = msgObj.getString("roomid");
            if (roomid != null && !roomid.isEmpty()) {
                System.out.println("发现群聊: " + roomid);
                
                // 提取群聊信息
                String roomName = msgObj.getString("room_name");
                String from = msgObj.getString("from");
                String msgType = msgObj.getString("msgtype");
                String content = msgObj.getString("content");
                
                // 这里可以将群聊信息保存到数据库
                saveGroupInfo(roomid, roomName, from, msgType, content, corpId);
                
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("提取群聊信息异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 保存群聊信息（输出到日志）
     */
    private void saveGroupInfo(String roomid, String roomName, String from, String msgType, String content, String corpId) {
        System.out.println("========================================");
        System.out.println("🎉 发现群聊信息:");
        System.out.println("  - roomid: " + roomid);
        System.out.println("  - roomName: " + roomName);
        System.out.println("  - owner: " + from);
        System.out.println("  - msgType: " + msgType);
        System.out.println("  - corpId: " + corpId);
        System.out.println("========================================");
    }
    
    /**
     * 获取上次拉取的seq
     */
    private long getLastSeq(String corpId) {
        // 这里应该从数据库读取上次的seq
        // 简化处理，返回0
        return 0;
    }
    
    /**
     * 保存最新的seq
     */
    private void saveLastSeq(String corpId, long seq) {
        // 这里应该将seq保存到数据库
        System.out.println("保存最新seq: " + seq);
    }
}
