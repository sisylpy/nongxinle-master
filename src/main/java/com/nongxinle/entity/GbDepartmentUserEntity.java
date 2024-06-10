package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-16 11:26
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


@Setter@Getter@ToString
public class GbDepartmentUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  订货部门用户id
	 */
	private Integer gbDepartmentUserId;
	/**
	 *  订货部门id
	 */
	private Integer gbDuDepartmentId;
	/**
	 *  订货部门用户微信头像
	 */
	private String gbDuWxAvartraUrl;
	/**
	 *  订货部门用户微信昵称
	 */
	private String gbDuWxNickName;

	private String gbDuCode;
	/**
	 *  订货部门用户微信openid
	 */
	private String gbDuWxOpenId;
	/**
	 *  订货部门用户微信手机号码
	 */
	private String gbDuWxPhone;
	/**
	 *  订货部门用户是否是管理员
	 */
	private Integer gbDuAdmin;

	private String gbDuJoinDate;

	private Integer gbDuDistributerId;

	private Integer gbDuUrlChange;
	private Integer gbDuLoginTimes;

	private Integer gbDuDepartmentFatherId;
	private Integer gbDuCustomerService;

	private String gbDuPrintDeviceId;
	private String gbDuPrintBillDeviceId;

	private GbDepartmentEntity gbDepartmentEntity;
	private GbDistributerEntity gbDistributerEntity;


}
