package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-22 15:35
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxRetailerUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  零售商用户id
	 */
	private Integer nxRetailerUserId;
	/**
	 *  零售商id
	 */
	private Integer nxRetuRetailerId;
	/**
	 *  零售商用户微信头像
	 */
	private String nxRetuWxAvartraUrl;
	/**
	 *  零售商用户微信昵称
	 */
	private String nxRetuWxNickName;
	/**
	 *  零售商用户微信openid
	 */
	private String nxRetuWxOpenId;
	/**
	 *  零售商用户微信手机号码
	 */
	private String nxRetuWxPhone;
	/**
	 *  零售商用户是否是管理员
	 */
	private Integer nxRetuAdmin;
	/**
	 *  零售商用户是否修改头像
	 */
	private Integer nxRetuUrlChange;
	/**
	 *  零售商用户加入日期
	 */
	private String nxRetuJoinDate;

	private String nxRetuCode;

	private NxRetailerEntity nxRetailerEntity;

}
