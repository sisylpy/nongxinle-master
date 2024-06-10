package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 04-14 17:42
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCustomerUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  客户用户id
	 */
	private Integer nxCuUserId;
	/**
	 *  客户用户微信昵称
	 */
	private String nxCuWxNickName;
	/**
	 *  微信头像
	 */
	private String nxCuWxAvatarUrl;
	/**
	 *  微信性别
	 */
	private Integer nxCuWxGender;
	/**
	 *  客户id
	 */
	private Integer nxCuCustomerId;
	/**
	 *  微信openid
	 */
	private String nxCuWxOpenId;
	/**
	 *  微信手机号
	 */
	private String nxCuWxPhoneNumber;
	/**
	 *
	 */
	private Date nxCuJoinDate;

	/**
	 *
	 */
	private Float nxCuOrderAmount;

	/**
	 *
	 */
	private Integer nxCuOrderTimes;

	private NxCustomerEntity nxCustomerEntity;


}
