package com.nongxinle.service;

import com.nongxinle.entity.QyWxMessage;

import java.util.List;

/**
 * 消息处理服务
 * 
 * @author lpy
 * @date 2024-01-01
 */
public interface MessageHandlerService {
	
	/**
	 * 处理接收到的消息
	 * @param corpId 企业ID
	 * @param message 消息对象
	 */
	void handleMessage(String corpId, QyWxMessage message);
	
	/**
	 * 判断是否需要回复
	 * @param corpId 企业ID
	 * @param message 消息对象
	 * @return true需要回复，false不需要回复
	 */
	boolean shouldReply(String corpId, QyWxMessage message);
	
	/**
	 * 获取回复内容
	 * @param corpId 企业ID
	 * @param message 消息对象
	 * @return 回复内容，null表示不回复
	 */
	String getReplyContent(String corpId, QyWxMessage message);
	
	/**
	 * 保存聊天记录
	 * @param corpId 企业ID
	 * @param message 消息对象
	 * @param replyContent 回复内容
	 */
	void saveChatRecord(String corpId, QyWxMessage message, String replyContent);
}


