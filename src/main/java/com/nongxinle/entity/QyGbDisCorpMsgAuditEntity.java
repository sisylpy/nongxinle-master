package com.nongxinle.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 企业微信会话存档配置表
 * 
 * @author lpy
 * @date 2024-01-01
 */
public class QyGbDisCorpMsgAuditEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 主键ID
	 */
	private Long qyGbDisCorpMsgAuditId;
	
	/**
	 * 企业微信企业ID
	 */
	private String qyGbDisQyCorpId;
	
	/**
	 * 会话存档应用Secret
	 */
	private String qyGbDisCorpMsgAuditSecret;
	
	/**
	 * 回调Token
	 */
	private String qyGbDisCorpMsgAuditToken;
	
	/**
	 * 回调加密Key
	 */
	private String qyGbDisCorpMsgAuditEncodingAesKey;
	
	/**
	 * 会话存档私钥
	 */
	private String qyGbDisCorpMsgAuditPrivateKey;
	
	/**
	 * 访问令牌
	 */
	private String qyGbDisCorpMsgAuditAccessToken;
	
	/**
	 * Token过期时间
	 */
	private Date qyGbDisCorpMsgAuditTokenExpireTime;
	
	/**
	 * 状态 1启用 0禁用
	 */
	private Integer qyGbDisCorpMsgAuditStatus;
	
	/**
	 * 创建时间
	 */
	private Date qyGbDisCorpMsgAuditCreateDate;
	
	/**
	 * 更新时间
	 */
	private Date qyGbDisCorpMsgAuditUpdateDate;

	/**
	 * 设置：主键ID
	 */
	public void setQyGbDisCorpMsgAuditId(Long qyGbDisCorpMsgAuditId) {
		this.qyGbDisCorpMsgAuditId = qyGbDisCorpMsgAuditId;
	}
	
	/**
	 * 获取：主键ID
	 */
	public Long getQyGbDisCorpMsgAuditId() {
		return qyGbDisCorpMsgAuditId;
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
	 * 设置：会话存档应用Secret
	 */
	public void setQyGbDisCorpMsgAuditSecret(String qyGbDisCorpMsgAuditSecret) {
		this.qyGbDisCorpMsgAuditSecret = qyGbDisCorpMsgAuditSecret;
	}
	
	/**
	 * 获取：会话存档应用Secret
	 */
	public String getQyGbDisCorpMsgAuditSecret() {
		return qyGbDisCorpMsgAuditSecret;
	}

	/**
	 * 设置：回调Token
	 */
	public void setQyGbDisCorpMsgAuditToken(String qyGbDisCorpMsgAuditToken) {
		this.qyGbDisCorpMsgAuditToken = qyGbDisCorpMsgAuditToken;
	}
	
	/**
	 * 获取：回调Token
	 */
	public String getQyGbDisCorpMsgAuditToken() {
		return qyGbDisCorpMsgAuditToken;
	}

	/**
	 * 设置：回调加密Key
	 */
	public void setQyGbDisCorpMsgAuditEncodingAesKey(String qyGbDisCorpMsgAuditEncodingAesKey) {
		this.qyGbDisCorpMsgAuditEncodingAesKey = qyGbDisCorpMsgAuditEncodingAesKey;
	}
	
	/**
	 * 获取：回调加密Key
	 */
	public String getQyGbDisCorpMsgAuditEncodingAesKey() {
		return qyGbDisCorpMsgAuditEncodingAesKey;
	}

	/**
	 * 设置：会话存档私钥
	 */
	public void setQyGbDisCorpMsgAuditPrivateKey(String qyGbDisCorpMsgAuditPrivateKey) {
		this.qyGbDisCorpMsgAuditPrivateKey = qyGbDisCorpMsgAuditPrivateKey;
	}
	
	/**
	 * 获取：会话存档私钥
	 */
	public String getQyGbDisCorpMsgAuditPrivateKey() {
		return qyGbDisCorpMsgAuditPrivateKey;
	}

	/**
	 * 设置：访问令牌
	 */
	public void setQyGbDisCorpMsgAuditAccessToken(String qyGbDisCorpMsgAuditAccessToken) {
		this.qyGbDisCorpMsgAuditAccessToken = qyGbDisCorpMsgAuditAccessToken;
	}
	
	/**
	 * 获取：访问令牌
	 */
	public String getQyGbDisCorpMsgAuditAccessToken() {
		return qyGbDisCorpMsgAuditAccessToken;
	}

	/**
	 * 设置：Token过期时间
	 */
	public void setQyGbDisCorpMsgAuditTokenExpireTime(Date qyGbDisCorpMsgAuditTokenExpireTime) {
		this.qyGbDisCorpMsgAuditTokenExpireTime = qyGbDisCorpMsgAuditTokenExpireTime;
	}
	
	/**
	 * 获取：Token过期时间
	 */
	public Date getQyGbDisCorpMsgAuditTokenExpireTime() {
		return qyGbDisCorpMsgAuditTokenExpireTime;
	}

	/**
	 * 设置：状态 1启用 0禁用
	 */
	public void setQyGbDisCorpMsgAuditStatus(Integer qyGbDisCorpMsgAuditStatus) {
		this.qyGbDisCorpMsgAuditStatus = qyGbDisCorpMsgAuditStatus;
	}
	
	/**
	 * 获取：状态 1启用 0禁用
	 */
	public Integer getQyGbDisCorpMsgAuditStatus() {
		return qyGbDisCorpMsgAuditStatus;
	}

	/**
	 * 设置：创建时间
	 */
	public void setQyGbDisCorpMsgAuditCreateDate(Date qyGbDisCorpMsgAuditCreateDate) {
		this.qyGbDisCorpMsgAuditCreateDate = qyGbDisCorpMsgAuditCreateDate;
	}
	
	/**
	 * 获取：创建时间
	 */
	public Date getQyGbDisCorpMsgAuditCreateDate() {
		return qyGbDisCorpMsgAuditCreateDate;
	}

	/**
	 * 设置：更新时间
	 */
	public void setQyGbDisCorpMsgAuditUpdateDate(Date qyGbDisCorpMsgAuditUpdateDate) {
		this.qyGbDisCorpMsgAuditUpdateDate = qyGbDisCorpMsgAuditUpdateDate;
	}
	
	/**
	 * 获取：更新时间
	 */
	public Date getQyGbDisCorpMsgAuditUpdateDate() {
		return qyGbDisCorpMsgAuditUpdateDate;
	}
}
