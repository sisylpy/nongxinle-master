package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 11-30 15:31
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxRestrauntUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  订货部门用户id
	 */
	private Integer nxRestrauntUserId;
	/**
	 *  订货部门id
	 */
	private Integer nxRuRestaurantId;
	/**
	 *  订货部门用户微信头像
	 */
	private String nxRuWxAvartraUrl;
	/**
	 *  订货部门用户微信昵称
	 */
	private String nxRuWxNickName;
	/**
	 *  订货部门用户微信openid
	 */
	private String nxRuWxOpenId;
	/**
	 *  订货部门用户微信手机号码
	 */
	private String nxRuWxPhone;
	/**
	 *  订货部门用户是否是管理员
	 */
	private Integer nxRuAdmin;
	/**
	 *  订货部门批发商id
	 */
	private Integer nxRuComId;
	/**
	 *  
	 */
	private Integer nxRuUrlChange;
	/**
	 *  
	 */
	private Integer nxRuRestaurantFatherId;
	/**
	 *  
	 */
	private String nxRuJoinDate;

	private String nxRuCode;


}
