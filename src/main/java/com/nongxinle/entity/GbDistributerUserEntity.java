package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-27 09:44
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  批发商用户id
	 */
	private Integer gbDistributerUserId;
	/**
	 *  用户名
	 */
	private String gbDiuWxAvartraUrl;
	/**
	 *  登陆密码
	 */
	private String gbDiuWxNickName;
	/**
	 *  
	 */
	private String gbDiuWxOpenId;
	/**
	 *  
	 */
	private String gbDiuWxPhone;
	/**
	 *  
	 */
	private Integer gbDiuDistributerId;
	/**
	 *  
	 */
	private Integer gbDiuAdmin;
	private Integer gbDiuQyCorpUserId;
	/**
	 *  
	 */
	private String gbDiuPrintDeviceId;

	private Integer gbDiuUrlChange;
	private Integer gbDiuLoginTimes;

	private String gbDiuPrintBillDeviceId;

	private String gbDiuCode;

	private GbDistributerEntity gbDistributerEntity;
	private QyGbDisCorpUserEntity qyGbDisCorpUserEntity;
}
