package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 11-30 21:47
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  批发商用户id
	 */
	private Integer nxCommunityUserId;
	/**
	 *  用户名
	 */
	private String nxCouWxAvartraUrl;
	/**
	 *  登陆密码
	 */
	private String nxCouWxNickName;
	/**
	 *  
	 */
	private String nxCouWxOpenId;
	/**
	 *  
	 */
	private String nxCouWxPhone;
	private String nxCouDeviceId;
	/**
	 *  
	 */
	private Integer nxCouCommunityId;
	/**
	 *  
	 */
	private Integer nxCouAdmin;

	private String nxCouCode;
	private Integer nxCouRoleId;
	private Integer nxCouWorkingStatus;
	private Integer nxCouUrlIsChange;


	private NxCommunityEntity nxCommunityEntity;

}
