package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-16 11:26
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDepartmentUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  订货部门用户id
	 */
	private Integer nxDepartmentUserId;
	/**
	 *  订货部门id
	 */
	private Integer nxDuDepartmentId;
	/**
	 *  订货部门用户微信头像
	 */
	private String nxDuWxAvartraUrl;
	/**
	 *  订货部门用户微信昵称
	 */
	private String nxDuWxNickName;

	private String nxDuCode;
	/**
	 *  订货部门用户微信openid
	 */
	private String nxDuWxOpenId;
	/**
	 *  订货部门用户微信手机号码
	 */
	private String nxDuWxPhone;
	/**
	 *  订货部门用户是否是管理员
	 */
	private Integer nxDuAdmin;
	private Integer nxDuLoginTimes;

	private String nxDuJoinDate;

	private Integer nxDuDistributerId;

	private Integer nxDuUrlChange;

	private Integer nxDuDepartmentFatherId;

}
