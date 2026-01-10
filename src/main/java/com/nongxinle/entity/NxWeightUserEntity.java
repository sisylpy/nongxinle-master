package com.nongxinle.entity;

/**
 * 称重员工实体类
 * @author lpy
 * @date 2025-01-XX
 */

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter@Getter@ToString
public class NxWeightUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 称重员工id
	 */
	private Integer nxWeightUserId;
	
	/**
	 * 员工类型：1=配送商员工, 2=供货商员工
	 */
	private Integer nxWuUserType;
	
	/**
	 * 微信openid
	 */
	private String nxWuWxOpenId;
	
	/**
	 * 微信昵称
	 */
	private String nxWuWxNickName;
	
	/**
	 * 微信头像
	 */
	private String nxWuWxAvartraUrl;
	
	/**
	 * 微信手机号
	 */
	private String nxWuWxPhone;
	
	/**
	 * 登录手机号
	 */
	private String nxWuLoginPhone;
	
	/**
	 * 登录次数
	 */
	private Integer nxWuLoginTimes;
	
	/**
	 * 登录验证码
	 */
	private String nxWuLoginCode;
	
	/**
	 * 关联配送商ID（当user_type=1时使用）
	 */
	private Integer nxWuNxDistributerId;
	
	/**
	 * 关联供货商用户ID（当user_type=2时使用）
	 */
	private Integer nxWuUserId;
	
	/**
	 * 状态：0=禁用，1=启用
	 */
	private Integer nxWuStatus;
	
	/**
	 * 加入日期
	 */
	private String nxWuJoinDate;
	
	/**
	 * URL变更标识
	 */
	private Integer nxWuUrlChange;
	
	/**
	 * 关联的配送商实体（当user_type=1时）
	 */
	private NxDistributerEntity nxDistributerEntity;
	
	/**
	 * 关联的供货商实体（当user_type=2时）
	 */
	private NxJrdhUserEntity nxJrdhUserEntity;
}

