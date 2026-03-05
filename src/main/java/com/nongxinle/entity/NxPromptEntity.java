package com.nongxinle.entity;

/**
 * 系统 Prompt 实体类
 * 
 * @author lpy
 * @date 2025-01-XX
 */

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxPromptEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  Prompt ID
	 */
	private Integer nxPromptId;
	
	/**
	 *  Prompt 唯一键（如：OCR_IMAGE, OCR_EXCEL）
	 */
	private String nxPromptKey;
	
	/**
	 *  Prompt 显示名称
	 */
	private String nxPromptName;
	
	/**
	 *  Prompt 具体内容
	 */
	private String nxPromptContent;
	
	/**
	 *  Prompt 分类（如：OCR, EXCEL）
	 */
	private String nxPromptCategory;
	
	/**
	 *  关联的 API 接口路径（如：/api/ocr/recognizeOrder）
	 */
	private String nxPromptApiPath;
	
	/**
	 *  Prompt 版本号
	 */
	private Integer nxPromptVersion;
	
	/**
	 *  最后更新时间
	 */
	private Timestamp nxPromptLastUpdated;
	
	/**
	 *  创建时间
	 */
	private Timestamp nxPromptCreatedTime;
	
	/**
	 *  状态（1=启用，0=禁用）
	 */
	private Integer nxPromptStatus;
	
	/**
	 *  描述说明
	 */
	private String nxPromptDescription;
}

