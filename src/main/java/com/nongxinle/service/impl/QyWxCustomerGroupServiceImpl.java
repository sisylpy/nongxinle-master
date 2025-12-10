package com.nongxinle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.QyGbDisCorpMsgAuditEntity;
import com.nongxinle.service.QyGbDisCorpMsgAuditService;
import com.nongxinle.service.QyWxCustomerGroupService;
import com.nongxinle.utils.WeChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 企业微信客户群管理服务实现
 * 
 * @author lpy
 * @date 2024-01-01
 */
@Service
public class QyWxCustomerGroupServiceImpl implements QyWxCustomerGroupService {

    @Autowired
    private QyGbDisCorpMsgAuditService msgAuditService;

    @Override
    public List<Map<String, Object>> getCustomerGroupList(String corpId, Integer statusFilter, Integer offset, Integer limit) {
        try {
            // 获取access_token
            String accessToken = getAccessToken(corpId);
            if (accessToken == null) {
                System.err.println("获取access_token失败");
                return new ArrayList<>();
            }

            // 使用客户联系API获取外部客户群列表
            String url = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/groupchat/list?access_token=" + accessToken;
            
            Map<String, Object> params = new HashMap<>();
            params.put("status_filter", statusFilter != null ? statusFilter : 0);
            params.put("offset", offset);
            params.put("limit", limit);

            String result = WeChatUtil.httpRequest(url, "POST", JSON.toJSONString(params));
            System.out.println("获取客户群列表响应: " + result);

            JSONObject jsonObject = JSON.parseObject(result);
            Integer errcode = jsonObject.getInteger("errcode");
            
            if (errcode == null || errcode != 0) {
                System.err.println("获取客户群列表失败 - errcode: " + errcode + ", errmsg: " + jsonObject.getString("errmsg"));
                return new ArrayList<>();
            }
            
            // 解析群聊列表
            List<Map<String, Object>> list = new ArrayList<>();
            JSONArray chatIdList = jsonObject.getJSONArray("chat_id_list");
            
            if (chatIdList != null) {
                for (int i = 0; i < chatIdList.size(); i++) {
                    String chatId = chatIdList.getString(i);
                    Map<String, Object> group = new HashMap<>();
                    group.put("chat_id", chatId);
                    group.put("status", 0);
                    list.add(group);
                }
            }
            
            System.out.println("成功获取到 " + list.size() + " 个客户群");
            return list;
        } catch (Exception e) {
            System.err.println("获取客户群列表异常: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getCustomerGroupDetail(String corpId, String chatId) {
        try {
            // 获取access_token
            String accessToken = getAccessToken(corpId);
            if (accessToken == null) {
                System.err.println("获取access_token失败");
                return null;
            }

            // 调用企业微信API获取客户群详情
            String url = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/groupchat/get?access_token=" + accessToken;
            
            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", chatId);
            params.put("need_name", 1);

            String result = WeChatUtil.httpRequest(url, "POST", JSON.toJSONString(params));
            System.out.println("获取客户群详情响应: " + result);

            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getInteger("errcode") == 0) {
                JSONObject groupChat = jsonObject.getJSONObject("group_chat");
                Map<String, Object> detail = new HashMap<>();
                detail.put("chat_id", groupChat.getString("chat_id"));
                detail.put("name", groupChat.getString("name"));
                detail.put("owner", groupChat.getString("owner"));
                detail.put("create_time", groupChat.getLong("create_time"));
                detail.put("notice", groupChat.getString("notice"));
                
                JSONArray memberList = groupChat.getJSONArray("member_list");
                detail.put("member_count", memberList != null ? memberList.size() : 0);
                detail.put("member_list", memberList);
                
                return detail;
            } else {
                System.err.println("获取客户群详情失败: " + jsonObject.getString("errmsg"));
                return null;
            }
        } catch (Exception e) {
            System.err.println("获取客户群详情异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean saveMonitoredGroup(String corpId, String chatId, String chatName) {
        // TODO: 实现保存到数据库的逻辑
        // 这里需要创建对应的Dao和Mapper
        System.out.println("保存监控群到数据库: " + corpId + " - " + chatId + " - " + chatName);
        return true;
    }

    @Override
    public List<Map<String, Object>> getMonitoredGroups(String corpId) {
        // TODO: 从数据库查询已监控的群列表
        System.out.println("从数据库获取已监控群列表: " + corpId);
        return new ArrayList<>();
    }

    @Override
    public boolean removeMonitoredGroup(String corpId, String chatId) {
        // TODO: 从数据库删除监控的群
        System.out.println("从数据库删除监控群: " + corpId + " - " + chatId);
        return true;
    }

    /**
     * 获取access_token
     */
    @Override
    public String getAccessToken(String corpId) {
        try {
            QyGbDisCorpMsgAuditEntity config = msgAuditService.queryByCorpId(corpId);
            if (config == null) {
                System.err.println("未找到企业配置");
                return null;
            }

            // 检查token是否过期
            Date expireTime = config.getQyGbDisCorpMsgAuditTokenExpireTime();
            if (expireTime != null && expireTime.after(new Date())) {
                // token未过期，直接返回
                return config.getQyGbDisCorpMsgAuditAccessToken();
            }

            // token已过期或不存在，重新获取
            String secret = config.getQyGbDisCorpMsgAuditSecret();
            if (secret == null || secret.isEmpty()) {
                System.err.println("Secret未配置");
                return null;
            }

            String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + corpId + "&corpsecret=" + secret;
            String result = WeChatUtil.httpRequest(url, "GET", null);
            System.out.println("获取access_token响应: " + result);

            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getInteger("errcode") == 0) {
                String accessToken = jsonObject.getString("access_token");
                Integer expiresIn = jsonObject.getInteger("expires_in");

                // 更新到数据库
                config.setQyGbDisCorpMsgAuditAccessToken(accessToken);
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, expiresIn - 200); // 提前200秒过期
                config.setQyGbDisCorpMsgAuditTokenExpireTime(calendar.getTime());
                msgAuditService.update(config);

                return accessToken;
            } else {
                System.err.println("获取access_token失败: " + jsonObject.getString("errmsg"));
                return null;
            }
        } catch (Exception e) {
            System.err.println("获取access_token异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

