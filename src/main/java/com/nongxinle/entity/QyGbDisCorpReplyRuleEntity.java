package com.nongxinle.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 企业微信消息回复规则表
 * 
 * @author lpy
 * @date 2024-01-01
 */
public class QyGbDisCorpReplyRuleEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 主键ID
	 */
	private Long qyGbDisCorpReplyRuleId;
	
	/**
	 * 企业微信企业ID
	 */
	private String qyGbDisQyCorpId;
	
	/**
	 * 规则名称
	 */
	private String qyGbDisCorpReplyRuleName;
	
	/**
	 * 关键词（多个用逗号分隔）
	 */
	private String qyGbDisCorpReplyRuleKeyword;
	
	/**
	 * 匹配类型 1完全匹配 2包含 3正则
	 */
	private Integer qyGbDisCorpReplyRuleMatchType;
	
	/**
	 * 回复类型 1文本 2图片 3图文
	 */
	private Integer qyGbDisCorpReplyRuleType;
	
	/**
	 * 回复内容
	 */
	private String qyGbDisCorpReplyRuleContent;
	
	/**
	 * 优先级（数字越大越优先）
	 */
	private Integer qyGbDisCorpReplyRulePriority;
	
	/**
	 * 状态 1启用 0禁用
	 */
	private Integer qyGbDisCorpReplyRuleStatus;
	
	/**
	 * 创建时间
	 */
	private Date qyGbDisCorpReplyRuleCreateDate;
	
	/**
	 * 更新时间
	 */
	private Date qyGbDisCorpReplyRuleUpdateDate;

	/**
	 * 设置：主键ID
	 */
	public void setQyGbDisCorpReplyRuleId(Long qyGbDisCorpReplyRuleId) {
		this.qyGbDisCorpReplyRuleId = qyGbDisCorpReplyRuleId;
	}
	
	/**
	 * 获取：主键ID
	 */
	public Long getQyGbDisCorpReplyRuleId() {
		return qyGbDisCorpReplyRuleId;
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
	 * 设置：规则名称
	 */
	public void setQyGbDisCorpReplyRuleName(String qyGbDisCorpReplyRuleName) {
		this.qyGbDisCorpReplyRuleName = qyGbDisCorpReplyRuleName;
	}
	
	/**
	 * 获取：规则名称
	 */
	public String getQyGbDisCorpReplyRuleName() {
		return qyGbDisCorpReplyRuleName;
	}

	/**
	 * 设置：关键词（多个用逗号分隔）
	 */
	public void setQyGbDisCorpReplyRuleKeyword(String qyGbDisCorpReplyRuleKeyword) {
		this.qyGbDisCorpReplyRuleKeyword = qyGbDisCorpReplyRuleKeyword;
	}
	
	/**
	 * 获取：关键词（多个用逗号分隔）
	 */
	public String getQyGbDisCorpReplyRuleKeyword() {
		return qyGbDisCorpReplyRuleKeyword;
	}

	/**
	 * 设置：匹配类型 1完全匹配 2包含 3正则
	 */
	public void setQyGbDisCorpReplyRuleMatchType(Integer qyGbDisCorpReplyRuleMatchType) {
		this.qyGbDisCorpReplyRuleMatchType = qyGbDisCorpReplyRuleMatchType;
	}
	
	/**
	 * 获取：匹配类型 1完全匹配 2包含 3正则
	 */
	public Integer getQyGbDisCorpReplyRuleMatchType() {
		return qyGbDisCorpReplyRuleMatchType;
	}

	/**
	 * 设置：回复类型 1文本 2图片 3图文
	 */
	public void setQyGbDisCorpReplyRuleType(Integer qyGbDisCorpReplyRuleType) {
		this.qyGbDisCorpReplyRuleType = qyGbDisCorpReplyRuleType;
	}
	
	/**
	 * 获取：回复类型 1文本 2图片 3图文
	 */
	public Integer getQyGbDisCorpReplyRuleType() {
		return qyGbDisCorpReplyRuleType;
	}

	/**
	 * 设置：回复内容
	 */
	public void setQyGbDisCorpReplyRuleContent(String qyGbDisCorpReplyRuleContent) {
		this.qyGbDisCorpReplyRuleContent = qyGbDisCorpReplyRuleContent;
	}
	
	/**
	 * 获取：回复内容
	 */
	public String getQyGbDisCorpReplyRuleContent() {
		return qyGbDisCorpReplyRuleContent;
	}

	/**
	 * 设置：优先级（数字越大越优先）
	 */
	public void setQyGbDisCorpReplyRulePriority(Integer qyGbDisCorpReplyRulePriority) {
		this.qyGbDisCorpReplyRulePriority = qyGbDisCorpReplyRulePriority;
	}
	
	/**
	 * 获取：优先级（数字越大越优先）
	 */
	public Integer getQyGbDisCorpReplyRulePriority() {
		return qyGbDisCorpReplyRulePriority;
	}

	/**
	 * 设置：状态 1启用 0禁用
	 */
	public void setQyGbDisCorpReplyRuleStatus(Integer qyGbDisCorpReplyRuleStatus) {
		this.qyGbDisCorpReplyRuleStatus = qyGbDisCorpReplyRuleStatus;
	}
	
	/**
	 * 获取：状态 1启用 0禁用
	 */
	public Integer getQyGbDisCorpReplyRuleStatus() {
		return qyGbDisCorpReplyRuleStatus;
	}

	/**
	 * 设置：创建时间
	 */
	public void setQyGbDisCorpReplyRuleCreateDate(Date qyGbDisCorpReplyRuleCreateDate) {
		this.qyGbDisCorpReplyRuleCreateDate = qyGbDisCorpReplyRuleCreateDate;
	}
	
	/**
	 * 获取：创建时间
	 */
	public Date getQyGbDisCorpReplyRuleCreateDate() {
		return qyGbDisCorpReplyRuleCreateDate;
	}

	/**
	 * 设置：更新时间
	 */
	public void setQyGbDisCorpReplyRuleUpdateDate(Date qyGbDisCorpReplyRuleUpdateDate) {
		this.qyGbDisCorpReplyRuleUpdateDate = qyGbDisCorpReplyRuleUpdateDate;
	}
	
	/**
	 * 获取：更新时间
	 */
	public Date getQyGbDisCorpReplyRuleUpdateDate() {
		return qyGbDisCorpReplyRuleUpdateDate;
	}
}
