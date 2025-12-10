package com.nongxinle.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 企业微信聊天记录表
 * 
 * @author lpy
 * @date 2024-01-01
 */
public class QyGbDisCorpChatRecordEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 主键ID
	 */
	private Long qyGbDisCorpChatRecordId;
	
	/**
	 * 企业微信企业ID
	 */
	private String qyGbDisQyCorpId;
	
	/**
	 * 消息ID
	 */
	private String qyGbDisCorpChatRecordMsgId;
	
	/**
	 * 群ID
	 */
	private String qyGbDisCorpChatRecordRoomId;
	
	/**
	 * 发送者
	 */
	private String qyGbDisCorpChatRecordFromUser;
	
	/**
	 * 消息类型
	 */
	private String qyGbDisCorpChatRecordMsgType;
	
	/**
	 * 消息内容
	 */
	private String qyGbDisCorpChatRecordContent;
	
	/**
	 * 消息时间戳
	 */
	private Long qyGbDisCorpChatRecordMsgTime;
	
	/**
	 * 是否已回复
	 */
	private Integer qyGbDisCorpChatRecordIsReplied;
	
	/**
	 * 回复内容
	 */
	private String qyGbDisCorpChatRecordReplyContent;
	
	/**
	 * 创建时间
	 */
	private Date qyGbDisCorpChatRecordCreateDate;

	/**
	 * 设置：主键ID
	 */
	public void setQyGbDisCorpChatRecordId(Long qyGbDisCorpChatRecordId) {
		this.qyGbDisCorpChatRecordId = qyGbDisCorpChatRecordId;
	}
	
	/**
	 * 获取：主键ID
	 */
	public Long getQyGbDisCorpChatRecordId() {
		return qyGbDisCorpChatRecordId;
	}

	/**
	 * 设置：企业微信企业ID
	 */
	public void setQyGbDisQyCorpId(String qyGbDisQyCorpId) {
		this.qyGbDisQyCorpId = qyGbDisQyCorpId;
	}
	
	/**
	 * 获取：企业微信企业ID
	 */
	public String getQyGbDisQyCorpId() {
		return qyGbDisQyCorpId;
	}

	/**
	 * 设置：消息ID
	 */
	public void setQyGbDisCorpChatRecordMsgId(String qyGbDisCorpChatRecordMsgId) {
		this.qyGbDisCorpChatRecordMsgId = qyGbDisCorpChatRecordMsgId;
	}
	
	/**
	 * 获取：消息ID
	 */
	public String getQyGbDisCorpChatRecordMsgId() {
		return qyGbDisCorpChatRecordMsgId;
	}

	/**
	 * 设置：群ID
	 */
	public void setQyGbDisCorpChatRecordRoomId(String qyGbDisCorpChatRecordRoomId) {
		this.qyGbDisCorpChatRecordRoomId = qyGbDisCorpChatRecordRoomId;
	}
	
	/**
	 * 获取：群ID
	 */
	public String getQyGbDisCorpChatRecordRoomId() {
		return qyGbDisCorpChatRecordRoomId;
	}

	/**
	 * 设置：发送者
	 */
	public void setQyGbDisCorpChatRecordFromUser(String qyGbDisCorpChatRecordFromUser) {
		this.qyGbDisCorpChatRecordFromUser = qyGbDisCorpChatRecordFromUser;
	}
	
	/**
	 * 获取：发送者
	 */
	public String getQyGbDisCorpChatRecordFromUser() {
		return qyGbDisCorpChatRecordFromUser;
	}

	/**
	 * 设置：消息类型
	 */
	public void setQyGbDisCorpChatRecordMsgType(String qyGbDisCorpChatRecordMsgType) {
		this.qyGbDisCorpChatRecordMsgType = qyGbDisCorpChatRecordMsgType;
	}
	
	/**
	 * 获取：消息类型
	 */
	public String getQyGbDisCorpChatRecordMsgType() {
		return qyGbDisCorpChatRecordMsgType;
	}

	/**
	 * 设置：消息内容
	 */
	public void setQyGbDisCorpChatRecordContent(String qyGbDisCorpChatRecordContent) {
		this.qyGbDisCorpChatRecordContent = qyGbDisCorpChatRecordContent;
	}
	
	/**
	 * 获取：消息内容
	 */
	public String getQyGbDisCorpChatRecordContent() {
		return qyGbDisCorpChatRecordContent;
	}

	/**
	 * 设置：消息时间戳
	 */
	public void setQyGbDisCorpChatRecordMsgTime(Long qyGbDisCorpChatRecordMsgTime) {
		this.qyGbDisCorpChatRecordMsgTime = qyGbDisCorpChatRecordMsgTime;
	}
	
	/**
	 * 获取：消息时间戳
	 */
	public Long getQyGbDisCorpChatRecordMsgTime() {
		return qyGbDisCorpChatRecordMsgTime;
	}

	/**
	 * 设置：是否已回复
	 */
	public void setQyGbDisCorpChatRecordIsReplied(Integer qyGbDisCorpChatRecordIsReplied) {
		this.qyGbDisCorpChatRecordIsReplied = qyGbDisCorpChatRecordIsReplied;
	}
	
	/**
	 * 获取：是否已回复
	 */
	public Integer getQyGbDisCorpChatRecordIsReplied() {
		return qyGbDisCorpChatRecordIsReplied;
	}

	/**
	 * 设置：回复内容
	 */
	public void setQyGbDisCorpChatRecordReplyContent(String qyGbDisCorpChatRecordReplyContent) {
		this.qyGbDisCorpChatRecordReplyContent = qyGbDisCorpChatRecordReplyContent;
	}
	
	/**
	 * 获取：回复内容
	 */
	public String getQyGbDisCorpChatRecordReplyContent() {
		return qyGbDisCorpChatRecordReplyContent;
	}

	/**
	 * 设置：创建时间
	 */
	public void setQyGbDisCorpChatRecordCreateDate(Date qyGbDisCorpChatRecordCreateDate) {
		this.qyGbDisCorpChatRecordCreateDate = qyGbDisCorpChatRecordCreateDate;
	}
	
	/**
	 * 获取：创建时间
	 */
	public Date getQyGbDisCorpChatRecordCreateDate() {
		return qyGbDisCorpChatRecordCreateDate;
	}
}
