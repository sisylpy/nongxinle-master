package com.nongxinle.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.dao.QyGbDisCorpChatRecordDao;
import com.nongxinle.dao.QyGbDisCorpReplyRuleDao;
import com.nongxinle.entity.QyGbDisCorpChatRecordEntity;
import com.nongxinle.entity.QyGbDisCorpReplyRuleEntity;
import com.nongxinle.entity.QyWxMessage;
import com.nongxinle.service.MessageHandlerService;
import com.nongxinle.service.QyWxMessageSendService;
import com.nongxinle.service.TencentCloudAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 消息处理服务实现
 * 
 * @author lpy
 * @date 2024-01-01
 */
@Service("messageHandlerService")
public class MessageHandlerServiceImpl implements MessageHandlerService {
	
	@Autowired
	private QyGbDisCorpReplyRuleDao qyGbDisCorpReplyRuleDao;
	
	@Autowired
	private QyGbDisCorpChatRecordDao qyGbDisCorpChatRecordDao;
	
	@Autowired
	private QyWxMessageSendService qyWxMessageSendService;
	
	@Autowired
	private TencentCloudAgentService tencentCloudAgentService;

	@Override
	public void handleMessage(String corpId, QyWxMessage message) {
		try {
			System.out.println("处理消息: " + message);
			
			// 只处理文本消息
			if (!"text".equals(message.getMsgtype())) {
				System.out.println("跳过非文本消息: " + message.getMsgtype());
				return;
			}
			
			// 只处理发送的消息（不是撤回）
			if (!"send".equals(message.getAction())) {
				System.out.println("跳过非发送消息: " + message.getAction());
				return;
			}
			
			// 判断是否需要回复
			if (!shouldReply(corpId, message)) {
				System.out.println("消息不需要回复");
				saveChatRecord(corpId, message, null);
				return;
			}
			
			// 获取回复内容
			String replyContent = getReplyContent(corpId, message);
			if (replyContent == null || replyContent.trim().isEmpty()) {
				System.out.println("没有找到匹配的回复规则");
				saveChatRecord(corpId, message, null);
				return;
			}
			
			// 发送回复
			boolean sendResult = qyWxMessageSendService.sendTextToGroup(corpId, message.getRoomid(), replyContent);
			if (sendResult) {
				System.out.println("回复发送成功: " + replyContent);
			} else {
				System.err.println("回复发送失败");
			}
			
			// 保存聊天记录
			saveChatRecord(corpId, message, replyContent);
			
		} catch (Exception e) {
			System.err.println("处理消息异常: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public boolean shouldReply(String corpId, QyWxMessage message) {
		try {
			// 解析消息内容
			String content = parseTextContent(message.getContent());
			if (content == null || content.trim().isEmpty()) {
				return false;
			}
			
			// 获取启用的回复规则
			List<QyGbDisCorpReplyRuleEntity> rules = qyGbDisCorpReplyRuleDao.queryEnabledRulesByCorpId(corpId);
			if (rules == null || rules.isEmpty()) {
				System.out.println("没有找到启用的回复规则");
				return false;
			}
			
			// 遍历规则进行匹配
			for (QyGbDisCorpReplyRuleEntity rule : rules) {
				if (matchRule(content, rule)) {
					System.out.println("匹配到规则: " + rule.getQyGbDisCorpReplyRuleName());
					return true;
				}
			}
			
			return false;
		} catch (Exception e) {
			System.err.println("判断是否需要回复异常: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String getReplyContent(String corpId, QyWxMessage message) {
		try {
			// 解析消息内容
			String content = parseTextContent(message.getContent());
			if (content == null || content.trim().isEmpty()) {
				return null;
			}
			
			// 第一层：快速规则匹配
			String quickReply = getQuickReply(corpId, content);
			if (quickReply != null) {
				System.out.println("快速规则匹配成功: " + quickReply);
				return quickReply;
			}
			
			// 第二层：腾讯云智能体
			String aiReply = tencentCloudAgentService.getAgentReply(content, message.getRoomid());
			if (aiReply != null && !aiReply.trim().isEmpty()) {
				System.out.println("智能体回复: " + aiReply);
				return aiReply;
			}
			
			// 第三层：兜底回复
			return "抱歉，我暂时无法回答您的问题，已为您转接人工客服。";
			
		} catch (Exception e) {
			System.err.println("获取回复内容异常: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 快速规则匹配
	 */
	private String getQuickReply(String corpId, String content) {
		try {
			// 获取启用的回复规则
			List<QyGbDisCorpReplyRuleEntity> rules = qyGbDisCorpReplyRuleDao.queryEnabledRulesByCorpId(corpId);
			if (rules == null || rules.isEmpty()) {
				return null;
			}
			
			// 遍历规则进行匹配（按优先级排序）
			for (QyGbDisCorpReplyRuleEntity rule : rules) {
				if (matchRule(content, rule)) {
					return rule.getQyGbDisCorpReplyRuleContent();
				}
			}
			
			return null;
		} catch (Exception e) {
			System.err.println("快速规则匹配异常: " + e.getMessage());
			return null;
		}
	}

	@Override
	public void saveChatRecord(String corpId, QyWxMessage message, String replyContent) {
		try {
			// 检查是否已存在记录
			QyGbDisCorpChatRecordEntity existingRecord = qyGbDisCorpChatRecordDao.queryByMsgId(message.getMsgid());
			if (existingRecord != null) {
				System.out.println("聊天记录已存在: " + message.getMsgid());
				return;
			}
			
			// 创建新记录
			QyGbDisCorpChatRecordEntity record = new QyGbDisCorpChatRecordEntity();
			record.setQyGbDisQyCorpId(corpId);
			record.setQyGbDisCorpChatRecordMsgId(message.getMsgid());
			record.setQyGbDisCorpChatRecordRoomId(message.getRoomid());
			record.setQyGbDisCorpChatRecordFromUser(message.getFrom());
			record.setQyGbDisCorpChatRecordMsgType(message.getMsgtype());
			record.setQyGbDisCorpChatRecordContent(message.getContent());
			record.setQyGbDisCorpChatRecordMsgTime(message.getMsgtime());
			record.setQyGbDisCorpChatRecordIsReplied(replyContent != null ? 1 : 0);
			record.setQyGbDisCorpChatRecordReplyContent(replyContent);
			record.setQyGbDisCorpChatRecordCreateDate(new Date());
			
			// 保存记录
			qyGbDisCorpChatRecordDao.save(record);
			System.out.println("聊天记录保存成功: " + message.getMsgid());
			
		} catch (Exception e) {
			System.err.println("保存聊天记录异常: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 解析文本消息内容
	 * @param content 消息内容JSON字符串
	 * @return 解析后的文本内容
	 */
	private String parseTextContent(String content) {
		try {
			if (content == null || content.trim().isEmpty()) {
				return null;
			}
			
			JSONObject contentJson = JSONObject.parseObject(content);
			return contentJson.getString("content");
		} catch (Exception e) {
			System.err.println("解析文本内容失败: " + e.getMessage());
			return content; // 如果解析失败，返回原始内容
		}
	}
	
	/**
	 * 匹配规则
	 * @param content 消息内容
	 * @param rule 回复规则
	 * @return true匹配成功，false匹配失败
	 */
	private boolean matchRule(String content, QyGbDisCorpReplyRuleEntity rule) {
		try {
			String keywords = rule.getQyGbDisCorpReplyRuleKeyword();
			if (keywords == null || keywords.trim().isEmpty()) {
				return false;
			}
			
			Integer matchType = rule.getQyGbDisCorpReplyRuleMatchType();
			if (matchType == null) {
				matchType = 2; // 默认包含匹配
			}
			
			// 按逗号分割关键词
			String[] keywordArray = keywords.split(",");
			for (String keyword : keywordArray) {
				keyword = keyword.trim();
				if (keyword.isEmpty()) {
					continue;
				}
				
				boolean matched = false;
				switch (matchType) {
					case 1: // 完全匹配
						matched = content.equals(keyword);
						break;
					case 2: // 包含匹配
						matched = content.contains(keyword);
						break;
					case 3: // 正则匹配
						matched = Pattern.matches(keyword, content);
						break;
					default:
						matched = content.contains(keyword);
						break;
				}
				
				if (matched) {
					System.out.println("关键词匹配成功: " + keyword + " (匹配类型: " + matchType + ")");
					return true;
				}
			}
			
			return false;
		} catch (Exception e) {
			System.err.println("匹配规则异常: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
}


