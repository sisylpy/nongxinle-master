package com.nongxinle.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.dao.QyGbDisCorpMsgAuditDao;
import com.nongxinle.entity.QyGbDisCorpMsgAuditEntity;
import com.nongxinle.entity.QyWxMessage;
import com.nongxinle.service.QyGbDisCorpMsgAuditService;
import com.nongxinle.utils.MessageDecryptUtil;
import com.nongxinle.utils.QyWxTokenUtil;
import com.nongxinle.utils.WeChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信会话存档服务实现
 * 
 * @author lpy
 * @date 2024-01-01
 */
@Service("qyGbDisCorpMsgAuditService")
public class QyGbDisCorpMsgAuditServiceImpl implements QyGbDisCorpMsgAuditService {
	
	@Autowired
	private QyGbDisCorpMsgAuditDao qyGbDisCorpMsgAuditDao;
	
	@Autowired
	private QyWxTokenUtil qyWxTokenUtil;
	
	@Autowired
	private MessageDecryptUtil messageDecryptUtil;

	@Override
	public QyGbDisCorpMsgAuditEntity queryByCorpId(String corpId) {
		return qyGbDisCorpMsgAuditDao.queryByCorpId(corpId);
	}

	@Override
	public int updateAccessToken(String corpId, String accessToken, Date expireTime) {
		return qyGbDisCorpMsgAuditDao.updateAccessToken(corpId, accessToken, expireTime);
	}

	@Override
	public void save(QyGbDisCorpMsgAuditEntity config) {
		config.setQyGbDisCorpMsgAuditCreateDate(new Date());
		config.setQyGbDisCorpMsgAuditUpdateDate(new Date());
		qyGbDisCorpMsgAuditDao.save(config);
	}

	@Override
	public void update(QyGbDisCorpMsgAuditEntity config) {
		config.setQyGbDisCorpMsgAuditUpdateDate(new Date());
		qyGbDisCorpMsgAuditDao.update(config);
	}

	@Override
	public QyWxMessage getChatData(String corpId, String msgId) {
		try {
			// 获取access_token
			String accessToken = qyWxTokenUtil.getAccessToken(corpId);
			if (accessToken == null) {
				throw new RuntimeException("获取access_token失败");
			}

			// 构建请求参数
			Map<String, Object> params = new HashMap<>();
			params.put("msgid", msgId);

			// 调用会话存档API获取消息详情
			String url = "https://qyapi.weixin.qq.com/cgi-bin/msgaudit/groupchat/get?access_token=" + accessToken;
			String response = WeChatUtil.httpRequest(url, "POST", JSONObject.toJSONString(params));
			
			System.out.println("获取会话内容响应: " + response);
			
			JSONObject jsonObject = JSONObject.parseObject(response);
			if (jsonObject.getInteger("errcode") == 0) {
				// 解析消息内容
				QyWxMessage message = new QyWxMessage();
				message.setMsgid(jsonObject.getString("msgid"));
				message.setAction(jsonObject.getString("action"));
				message.setFrom(jsonObject.getString("from"));
				message.setRoomid(jsonObject.getString("roomid"));
				message.setMsgtime(jsonObject.getLong("msgtime"));
				message.setMsgtype(jsonObject.getString("msgtype"));
				message.setContent(jsonObject.getString("content"));
				
				return message;
			} else {
				String errorMsg = "获取会话内容失败: " + jsonObject.getString("errmsg");
				System.err.println(errorMsg);
				throw new RuntimeException(errorMsg);
			}
		} catch (Exception e) {
			System.err.println("获取会话内容异常: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("获取会话内容失败", e);
		}
	}

	@Override
	public String decryptMessage(String encrypted, String corpId) {
		try {
			// 获取配置
			QyGbDisCorpMsgAuditEntity config = queryByCorpId(corpId);
			if (config == null) {
				throw new RuntimeException("未找到企业微信会话存档配置");
			}

			// RSA解密消息内容
			return messageDecryptUtil.decryptMsgContent(encrypted, config.getQyGbDisCorpMsgAuditPrivateKey());
		} catch (Exception e) {
			System.err.println("解密消息失败: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
}
