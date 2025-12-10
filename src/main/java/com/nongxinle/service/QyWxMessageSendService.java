package com.nongxinle.service;

/**
 * 企业微信消息发送服务
 * 
 * @author lpy
 * @date 2024-01-01
 */
public interface QyWxMessageSendService {
	
	/**
	 * 发送文本消息到群
	 * @param corpId 企业ID
	 * @param roomId 群ID
	 * @param content 消息内容
	 * @return true发送成功，false发送失败
	 */
	boolean sendTextToGroup(String corpId, String roomId, String content);
	
	/**
	 * 发送图片消息到群
	 * @param corpId 企业ID
	 * @param roomId 群ID
	 * @param mediaId 图片媒体ID
	 * @return true发送成功，false发送失败
	 */
	boolean sendImageToGroup(String corpId, String roomId, String mediaId);
	
	/**
	 * 发送文件消息到群
	 * @param corpId 企业ID
	 * @param roomId 群ID
	 * @param mediaId 文件媒体ID
	 * @return true发送成功，false发送失败
	 */
	boolean sendFileToGroup(String corpId, String roomId, String mediaId);
}


